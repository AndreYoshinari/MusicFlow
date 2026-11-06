package br.com.musicflow.database

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import br.com.musicflow.database.dao.PlaylistTrackWrite
import br.com.musicflow.database.entity.CachedAlbumEntity
import br.com.musicflow.database.entity.CachedArtistEntity
import br.com.musicflow.database.entity.CachedTrackEntity
import br.com.musicflow.database.entity.CatalogPageEntity
import br.com.musicflow.database.entity.CatalogPageTrackEntity
import br.com.musicflow.database.entity.FavoriteTrackEntity
import br.com.musicflow.database.entity.PlaybackHistoryEntity
import br.com.musicflow.database.entity.PlaylistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MusicFlowDatabaseTest {
    private lateinit var database: MusicFlowDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MusicFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun storedTracksCanBeSearchedByTitleArtistAndAlbum() = runTest {
        val tracks = database.cachedTrackDao()
        tracks.upsertAll(
            listOf(
                cachedTrack(id = 1, title = "Aurora", artistName = "Luna", albumTitle = "Noite"),
                cachedTrack(id = 2, title = "Sol", artistName = "Luna", albumTitle = "Dia"),
            ),
        )

        assertEquals(listOf(1L, 2L), tracks.getAll().map { it.id })
        assertEquals(listOf(1L), tracks.search("aurora").map { it.id })
        assertEquals(listOf(1L, 2L), tracks.search("luna").map { it.id })
        assertEquals(listOf(2L), tracks.search("dia").map { it.id })
        assertEquals(emptyList<Long>(), tracks.search("nenhuma").map { it.id })
    }

    @Test
    fun upsertRefreshesMetadataWithoutDuplicatingTheTrack() = runTest {
        val tracks = database.cachedTrackDao()
        tracks.upsertAll(listOf(cachedTrack(id = 1, title = "Aurora", streamUrl = "https://old")))
        tracks.upsertAll(listOf(cachedTrack(id = 1, title = "Aurora nova", streamUrl = "https://new")))

        val stored = tracks.getById(1)
        assertEquals("Aurora nova", stored?.title)
        assertEquals("https://new", stored?.streamUrl)
        assertEquals(1, tracks.getAll().size)
    }

    @Test
    fun favoritesAreObservedFromNewestToOldestAndRejectDuplicates() = runTest {
        val tracks = database.cachedTrackDao()
        val favorites = database.favoriteDao()
        tracks.upsertAll(listOf(cachedTrack(1), cachedTrack(2)))

        favorites.observe().test {
            assertEquals(emptyList<Long>(), awaitItem().map { it.id })
            favorites.insert(FavoriteTrackEntity(trackId = 1, addedAt = 10))
            assertEquals(listOf(1L), awaitItem().map { it.id })
            favorites.insert(FavoriteTrackEntity(trackId = 2, addedAt = 20))
            assertEquals(listOf(2L, 1L), awaitItem().map { it.id })
            favorites.insert(FavoriteTrackEntity(trackId = 1, addedAt = 30))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(2, favorites.count())
        assertEquals(true, favorites.isFavorite(1))
    }

    @Test
    fun removingAFavoriteKeepsTheCachedTrack() = runTest {
        val tracks = database.cachedTrackDao()
        val favorites = database.favoriteDao()
        tracks.upsertAll(listOf(cachedTrack(1)))
        favorites.insert(FavoriteTrackEntity(trackId = 1, addedAt = 10))

        favorites.delete(1)

        assertEquals(false, favorites.isFavorite(1))
        assertEquals(1L, tracks.getById(1)?.id)
    }

    @Test
    fun favoriteRequiresACachedTrack() = runTest {
        val result = runCatching {
            database.favoriteDao().insert(FavoriteTrackEntity(trackId = 9, addedAt = 1))
        }

        assertEquals(SQLiteConstraintException::class, result.exceptionOrNull()?.let { it::class })
        assertEquals(0, database.favoriteDao().count())
    }

    @Test
    fun playlistTracksStayOrderedUniqueAndAreRemovedWithThePlaylist() = runTest {
        val tracks = database.cachedTrackDao()
        val playlists = database.playlistDao()
        tracks.upsertAll(listOf(cachedTrack(1), cachedTrack(2), cachedTrack(3)))
        val playlistId = playlists.insertPlaylist(PlaylistEntity(name = "Noite", createdAt = 5))

        assertEquals(PlaylistTrackWrite.Saved, playlists.addTrack(playlistId, 1))
        assertEquals(PlaylistTrackWrite.Saved, playlists.addTrack(playlistId, 2))
        assertEquals(PlaylistTrackWrite.Saved, playlists.addTrack(playlistId, 3))
        assertEquals(PlaylistTrackWrite.Duplicate, playlists.addTrack(playlistId, 2))
        assertEquals(listOf(0, 1, 2), playlists.tracksOf(playlistId).map { it.position })
        assertEquals(listOf(1L, 2L, 3L), playlists.tracksOf(playlistId).map { it.trackId })

        assertEquals(true, playlists.removeTrack(playlistId, 2))
        assertEquals(listOf(1L, 3L), playlists.tracksOf(playlistId).map { it.trackId })
        assertEquals(listOf(0, 1), playlists.tracksOf(playlistId).map { it.position })

        assertEquals(PlaylistTrackWrite.MissingTrack, playlists.addTrack(playlistId, 99))
        assertEquals(PlaylistTrackWrite.MissingPlaylist, playlists.addTrack(playlistId = 99, trackId = 1))
        assertEquals(false, playlists.removeTrack(playlistId = 99, trackId = 1))

        assertEquals(1, playlists.delete(playlistId))
        assertNull(playlists.getPlaylist(playlistId))
        assertEquals(emptyList<Long>(), playlists.tracksOf(playlistId).map { it.trackId })
        assertEquals(3, tracks.getAll().size)
    }

    @Test
    fun historyKeepsChronologicalPlaysAndRecentTracksFollowTheLatestPlay() = runTest {
        val tracks = database.cachedTrackDao()
        val history = database.playbackHistoryDao()
        tracks.upsertAll(listOf(cachedTrack(1), cachedTrack(2)))

        history.insert(PlaybackHistoryEntity(trackId = 1, playedAt = 1_000))
        history.insert(PlaybackHistoryEntity(trackId = 2, playedAt = 2_000))
        history.insert(PlaybackHistoryEntity(trackId = 1, playedAt = 3_000))
        history.insert(PlaybackHistoryEntity(trackId = 1, playedAt = 3_000))

        assertEquals(3, history.count())
        assertEquals(listOf(1_000L, 2_000L, 3_000L), history.chronological().map { it.playedAt })
        assertEquals(listOf(1L, 2L), history.recentlyPlayed(limit = 10))
        assertEquals(listOf(1L), history.recentlyPlayed(limit = 1))

        history.clear()

        assertEquals(0, history.count())
        assertEquals(emptyList<Long>(), history.recentlyPlayed(limit = 10))
    }

    @Test
    fun historyRequiresACachedTrack() = runTest {
        val result = runCatching {
            database.playbackHistoryDao().insert(PlaybackHistoryEntity(trackId = 4, playedAt = 1))
        }

        assertEquals(SQLiteConstraintException::class, result.exceptionOrNull()?.let { it::class })
        assertEquals(0, database.playbackHistoryDao().count())
    }

    @Test
    fun catalogPagesKeepOrderAndDropDuplicateTracks() = runTest {
        val pages = database.catalogPageDao()
        val key = "tracks:popular"
        pages.write(
            header = CatalogPageEntity(key, fetchedAt = 10, nextOffset = 2),
            cached = listOf(cachedTrack(1), cachedTrack(2)),
            links = listOf(
                CatalogPageTrackEntity(key, 1, 0),
                CatalogPageTrackEntity(key, 2, 1),
            ),
            replace = true,
        )
        pages.write(
            header = CatalogPageEntity(key, fetchedAt = 10, nextOffset = null),
            cached = listOf(cachedTrack(2), cachedTrack(3)),
            links = listOf(
                CatalogPageTrackEntity(key, 2, 2),
                CatalogPageTrackEntity(key, 3, 3),
            ),
            replace = false,
        )

        val loaded = loadPage(key)
        assertEquals(listOf(1L, 2L, 3L), loaded)

        pages.write(
            header = CatalogPageEntity(key, fetchedAt = 20, nextOffset = null),
            cached = listOf(cachedTrack(4)),
            links = listOf(CatalogPageTrackEntity(key, 4, 0)),
            replace = true,
        )
        assertEquals(listOf(4L), loadPage(key))
    }

    @Test
    fun artistsAndAlbumsAreUpsertedWithoutDuplicates() = runTest {
        val artists = database.cachedArtistDao()
        val albums = database.cachedAlbumDao()
        artists.upsertAll(listOf(artist(7, "Luna")))
        artists.upsertAll(listOf(artist(7, "Luna Park"), artist(8, "Norte")))
        albums.upsertAll(listOf(album(3, "Noite")))
        albums.upsertAll(listOf(album(3, "Noite nova"), album(4, "Dia")))

        assertEquals("Luna Park", artists.getById(7)?.name)
        assertEquals(listOf(7L, 8L), artists.getByIds(listOf(8, 7)).map { it.id }.sorted())
        assertEquals(listOf("Dia", "Noite nova"), albums.getByIds(listOf(4, 3)).map { it.title }.sorted())
    }

    private suspend fun loadPage(key: String): List<Long> {
        val result = database.catalogPageDao().tracks(key).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        )
        val page = result as PagingSource.LoadResult.Page
        return page.data.map { it.id }
    }
}

private fun artist(id: Long, name: String) = CachedArtistEntity(
    id = id,
    name = name,
    imageUrl = "https://example.com/artist/$id",
    provider = "Jamendo",
    pageUrl = "https://www.jamendo.com/artist/$id",
    cachedAt = 1_000,
)

private fun album(id: Long, title: String) = CachedAlbumEntity(
    id = id,
    title = title,
    artistId = 7,
    artistName = "Luna Park",
    artworkUrl = "https://example.com/album/$id",
    releaseDate = "2024-05-02",
    provider = "Jamendo",
    pageUrl = "https://www.jamendo.com/album/$id",
    cachedAt = 1_000,
)

private fun cachedTrack(
    id: Long,
    title: String = "Faixa $id",
    artistName: String = "Artista",
    albumTitle: String = "Álbum",
    streamUrl: String? = "https://example.com/stream/$id",
): CachedTrackEntity = CachedTrackEntity(
    id = id,
    title = title,
    artistId = 7,
    artistName = artistName,
    albumId = 3,
    albumTitle = albumTitle,
    artworkUrl = "https://example.com/art/$id",
    durationMs = 180_000,
    streamUrl = streamUrl,
    streamObservedAt = 1_000,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    provider = "Jamendo",
    pageUrl = "https://www.jamendo.com/track/$id",
    releaseDate = "2024-05-02",
    cachedAt = 1_000,
)
