package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.data.local.FavoriteLocalDataSource
import br.com.musicflow.data.local.PlaybackHistoryLocalDataSource
import br.com.musicflow.data.local.PlaylistLocalDataSource
import br.com.musicflow.database.dao.PlaylistTrackWrite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LibraryRepositoryImplTest {
    @Test
    fun favoriteIsPersistedOnlyWhenTheTrackIsCached() = runTest {
        val local = FakeFavorites()
        val repository = FavoriteRepositoryImpl(local) { Instant.parse("2026-11-05T18:00:00Z") }

        val missing = repository.addFavorite(TrackId(1))
        local.cached += 1L
        val saved = repository.addFavorite(TrackId(1))
        val again = repository.addFavorite(TrackId(1))

        assertEquals(DomainResult.Failure(DomainError.NotFound), missing)
        assertEquals(DomainResult.Success(Unit), saved)
        assertEquals(DomainResult.Success(Unit), again)
        assertEquals(listOf(1L, 1L), local.added)
        assertEquals(true, (repository.isFavorite(TrackId(1)) as DomainResult.Success).value)
    }

    @Test
    fun removingAFavoriteIsImmediate() = runTest {
        val local = FakeFavorites().apply { cached += 1L }
        val repository = FavoriteRepositoryImpl(local)
        repository.addFavorite(TrackId(1))

        val result = repository.removeFavorite(TrackId(1))

        assertEquals(DomainResult.Success(Unit), result)
        assertEquals(false, (repository.isFavorite(TrackId(1)) as DomainResult.Success).value)
    }

    @Test
    fun playlistChangesAreWrittenImmediately() = runTest {
        val local = FakePlaylists()
        val repository = PlaylistRepositoryImpl(local) { Instant.parse("2026-11-05T18:00:00Z") }

        val created = repository.createPlaylist("  Noite  ") as DomainResult.Success
        val withTrack = repository.addTrack(created.value.id, TrackId(1)) as DomainResult.Success
        val duplicate = repository.addTrack(created.value.id, TrackId(1))
        val removed = repository.removeTrack(created.value.id, TrackId(1)) as DomainResult.Success

        assertEquals("Noite", created.value.name)
        assertEquals(listOf(TrackId(1)), withTrack.value.trackIds)
        assertEquals(DomainResult.Failure(DomainError.DuplicateTrack), duplicate)
        assertTrue(removed.value.trackIds.isEmpty())
        assertEquals(3, local.writes)
    }

    @Test
    fun blankPlaylistNameIsRejectedBeforeWriting() = runTest {
        val local = FakePlaylists()
        val repository = PlaylistRepositoryImpl(local)

        val result = repository.createPlaylist("   ")

        assertEquals(DomainResult.Failure(DomainError.InvalidName), result)
        assertEquals(0, local.writes)
    }

    @Test
    fun missingPlaylistIsNotFound() = runTest {
        val repository = PlaylistRepositoryImpl(FakePlaylists())

        val result = repository.addTrack(PlaylistId(4), TrackId(1))

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }

    @Test
    fun historyKeepsANewPlayAndSkipsTheSameTrackTwiceInARow() = runTest {
        val local = FakeHistory()
        val repository = PlaybackHistoryRepositoryImpl(local)
        val first = Instant.parse("2026-11-05T18:00:00Z")
        val second = first.plusSeconds(30)

        val missing = repository.addToHistory(TrackId(1), first)
        local.cached += 1L
        val recorded = repository.addToHistory(TrackId(1), first)
        val repeated = repository.addToHistory(TrackId(1), second)
        local.cached += 2L
        val next = repository.addToHistory(TrackId(2), second.plusSeconds(10))

        assertEquals(DomainResult.Failure(DomainError.NotFound), missing)
        assertEquals(DomainResult.Success(Unit), recorded)
        assertEquals(DomainResult.Success(Unit), repeated)
        assertEquals(DomainResult.Success(Unit), next)
        assertEquals(listOf(TrackId(2), TrackId(1)), repository.getRecentlyPlayed(10).let {
            (it as DomainResult.Success).value
        })
        assertEquals(2, local.plays.size)
    }

    @Test
    fun clearingHistoryRemovesTheStoredPlays() = runTest {
        val local = FakeHistory().apply { cached += 1L }
        val repository = PlaybackHistoryRepositoryImpl(local)
        repository.addToHistory(TrackId(1), Instant.parse("2026-11-05T18:00:00Z"))

        repository.clearHistory()

        assertEquals(DomainResult.Success(emptyList<TrackId>()), repository.getRecentlyPlayed(10))
    }

    @Test
    fun aNonPositiveHistoryLimitIsRejected() = runTest {
        val result = PlaybackHistoryRepositoryImpl(FakeHistory()).getRecentlyPlayed(0)

        assertEquals(DomainResult.Failure(DomainError.InvalidArgument), result)
    }
}

private class FakeFavorites : FavoriteLocalDataSource {
    val cached = mutableSetOf<Long>()
    val added = mutableListOf<Long>()
    private val favorites = linkedSetOf<Long>()
    private val state = MutableStateFlow<List<Track>>(emptyList())

    override fun observe(now: () -> Instant): Flow<List<Track>> = state

    override suspend fun getAll(now: Instant): List<Track> = emptyList()

    override suspend fun isFavorite(trackId: TrackId): Boolean = trackId.value in favorites

    override suspend fun containsTrack(trackId: TrackId): Boolean = trackId.value in cached

    override suspend fun add(trackId: TrackId, addedAt: Instant) {
        added += trackId.value
        favorites += trackId.value
    }

    override suspend fun remove(trackId: TrackId) {
        favorites -= trackId.value
    }
}

private class FakePlaylists : PlaylistLocalDataSource {
    var writes = 0
    private val playlists = mutableMapOf<Long, Playlist>()
    private var nextId = 1L

    override suspend fun getAll(): List<Playlist> = playlists.values.toList()

    override suspend fun get(id: PlaylistId): Playlist? = playlists[id.value]

    override suspend fun create(name: String, createdAt: Instant): Playlist {
        writes += 1
        val playlist = Playlist(PlaylistId(nextId++), name)
        playlists[playlist.id.value] = playlist
        return playlist
    }

    override suspend fun rename(id: PlaylistId, name: String): Playlist? {
        val current = playlists[id.value] ?: return null
        return current.rename(name).also { playlists[id.value] = it }
    }

    override suspend fun delete(id: PlaylistId): Boolean = playlists.remove(id.value) != null

    override suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): PlaylistTrackWrite {
        val current = playlists[playlistId.value] ?: return PlaylistTrackWrite.MissingPlaylist
        if (trackId in current.trackIds) return PlaylistTrackWrite.Duplicate
        writes += 1
        playlists[playlistId.value] = current.copy(trackIds = current.trackIds + trackId)
        return PlaylistTrackWrite.Saved
    }

    override suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): Boolean {
        val current = playlists[playlistId.value] ?: return false
        writes += 1
        playlists[playlistId.value] = current.copy(trackIds = current.trackIds.filterNot { it == trackId })
        return true
    }
}

private class FakeHistory : PlaybackHistoryLocalDataSource {
    val cached = mutableSetOf<Long>()
    val plays = mutableListOf<Pair<TrackId, Instant>>()

    override suspend fun trackExists(trackId: TrackId): Boolean = trackId.value in cached

    override suspend fun latestTrackId(): TrackId? = plays.maxByOrNull { it.second }?.first

    override suspend fun add(trackId: TrackId, playedAt: Instant) {
        plays += trackId to playedAt
    }

    override suspend fun recentlyPlayed(limit: Int): List<TrackId> =
        plays.groupBy { it.first }
            .map { (_, entries) -> entries.maxBy { it.second } }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }

    override suspend fun clear() {
        plays.clear()
    }
}
