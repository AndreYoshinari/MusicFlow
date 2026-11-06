package br.com.musicflow.feature.playlist

import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.common.domain.usecase.AddTrackToHistoryUseCase
import br.com.musicflow.common.domain.usecase.AddTrackToPlaylistUseCase
import br.com.musicflow.common.domain.usecase.DeletePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.RemoveTrackFromPlaylistUseCase
import br.com.musicflow.common.domain.usecase.RenamePlaylistUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailsViewModelTest {
    private val playedAt = Instant.parse("2026-11-05T20:00:00Z")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun renameDeleteAddAndRemoveUpdateThePlaylist() {
        val aurora = sampleTrack(1, "Aurora")
        val tarde = sampleTrack(3, "Tarde")
        val playlists = MemoryPlaylists()
        playlists.create(Playlist(PlaylistId(4), "Noite", listOf(aurora.id)))
        val history = RecordingHistory()
        val viewModel = viewModel(
            playlists = playlists,
            favorites = MemoryFavorites(listOf(aurora, tarde)),
            tracks = listOf(aurora, tarde),
            history = history,
        )

        val opened = viewModel.state.value.content as PlaylistContentState.Ready
        assertEquals("Noite", opened.playlist.name)
        assertEquals(listOf("Aurora"), opened.playlist.tracks.map { it.title })
        assertEquals(listOf("Tarde"), viewModel.state.value.candidates.map { it.title })

        viewModel.rename("   ")
        assertEquals(PlaylistNotice.Failed(DomainError.InvalidName), viewModel.state.value.notice)
        assertEquals("Noite", (viewModel.state.value.content as PlaylistContentState.Ready).playlist.name)

        viewModel.rename("  Manhã  ")
        assertEquals("Manhã", (viewModel.state.value.content as PlaylistContentState.Ready).playlist.name)
        assertEquals(null, viewModel.state.value.notice)

        viewModel.add(aurora.id.value)
        assertEquals(PlaylistNotice.Failed(DomainError.DuplicateTrack), viewModel.state.value.notice)
        assertEquals(listOf("Aurora"), readyTracks(viewModel))

        viewModel.add(tarde.id.value)
        assertEquals(listOf("Aurora", "Tarde"), readyTracks(viewModel))
        assertEquals(emptyList<String>(), viewModel.state.value.candidates.map { it.title })
        assertEquals(null, viewModel.state.value.notice)

        viewModel.remove(aurora.id.value)
        assertEquals(listOf("Tarde"), readyTracks(viewModel))

        viewModel.play()
        assertEquals(listOf(tarde.id), history.recorded.map { it.first })
        assertEquals(playedAt, history.recorded.single().second)
        assertEquals(listOf("Tarde"), viewModel.playback.value.tracks.map { it.title })
        assertTrue(viewModel.playback.value.revision > 0)

        viewModel.delete()
        assertTrue(viewModel.state.value.deleted)
        assertEquals(null, playlists.stored(PlaylistId(4)))
    }

    @Test
    fun emptyPlaylistDoesNotStartPlayback() {
        val playlists = MemoryPlaylists()
        playlists.create(Playlist(PlaylistId(4), "Noite"))
        val history = RecordingHistory()
        val viewModel = viewModel(
            playlists = playlists,
            favorites = MemoryFavorites(emptyList()),
            tracks = emptyList(),
            history = history,
        )

        viewModel.play()

        assertEquals(PlaylistNotice.EmptyPlayback, viewModel.state.value.notice)
        assertEquals(0, viewModel.playback.value.revision)
        assertTrue(history.recorded.isEmpty())
    }

    @Test
    fun playlistStaysAvailableWhenTrackDetailsAreOffline() {
        val playlists = MemoryPlaylists()
        playlists.create(Playlist(PlaylistId(4), "Noite", listOf(TrackId(9))))
        val viewModel = viewModel(
            playlists = playlists,
            favorites = MemoryFavorites(emptyList()),
            tracks = emptyList(),
            history = RecordingHistory(),
            detailsError = DomainError.Offline,
        )

        val content = viewModel.state.value.content as PlaylistContentState.Ready
        assertEquals("Noite", content.playlist.name)
        assertTrue(content.playlist.tracks.isEmpty())
        assertEquals(PlaylistNotice.Failed(DomainError.Offline), viewModel.state.value.notice)
    }

    private fun viewModel(
        playlists: MemoryPlaylists,
        favorites: MemoryFavorites,
        tracks: List<Track>,
        history: RecordingHistory,
        detailsError: DomainError? = null,
    ): PlaylistDetailsViewModel = PlaylistDetailsViewModel(
        playlistId = 4,
        playlist = GetPlaylistUseCase(playlists),
        renamePlaylist = RenamePlaylistUseCase(playlists),
        deletePlaylist = DeletePlaylistUseCase(playlists),
        addTrack = AddTrackToPlaylistUseCase(playlists),
        removeTrack = RemoveTrackFromPlaylistUseCase(playlists),
        favorites = GetFavoritesUseCase(favorites),
        details = GetTrackDetailsUseCase(Catalog(tracks, detailsError)),
        history = AddTrackToHistoryUseCase(history),
        now = { playedAt },
    )

    private fun readyTracks(viewModel: PlaylistDetailsViewModel): List<String> =
        (viewModel.state.value.content as PlaylistContentState.Ready).playlist.tracks.map { it.title }
}

private class MemoryFavorites(tracks: List<Track>) : FavoriteRepository {
    private val tracksById = tracks.associateBy { it.id }
    private val ids = MutableStateFlow(tracks.map { it.id })

    override suspend fun getFavorites(): DomainResult<List<Track>> =
        DomainResult.Success(ids.value.mapNotNull { tracksById[it] })

    override suspend fun isFavorite(trackId: TrackId): DomainResult<Boolean> =
        DomainResult.Success(trackId in ids.value)

    override fun observeFavorites(): Flow<List<Track>> =
        ids.map { current -> current.mapNotNull { tracksById[it] } }

    override suspend fun addFavorite(trackId: TrackId): DomainResult<Unit> {
        if (tracksById[trackId] == null) return DomainResult.Failure(DomainError.NotFound)
        if (trackId !in ids.value) ids.value = listOf(trackId) + ids.value
        return DomainResult.Success(Unit)
    }

    override suspend fun removeFavorite(trackId: TrackId): DomainResult<Unit> {
        ids.value = ids.value.filterNot { it == trackId }
        return DomainResult.Success(Unit)
    }
}

private class MemoryPlaylists : PlaylistRepository {
    private val items = linkedMapOf<PlaylistId, Playlist>()

    fun create(playlist: Playlist) {
        items[playlist.id] = playlist
    }

    fun stored(id: PlaylistId): Playlist? = items[id]

    override suspend fun getPlaylists(): DomainResult<List<Playlist>> = DomainResult.Success(items.values.toList())

    override suspend fun getPlaylistById(id: PlaylistId): DomainResult<Playlist> =
        items[id]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)

    override suspend fun createPlaylist(name: String): DomainResult<Playlist> =
        DomainResult.Failure(DomainError.InvalidName)

    override suspend fun renamePlaylist(id: PlaylistId, name: String): DomainResult<Playlist> {
        val current = items[id] ?: return DomainResult.Failure(DomainError.NotFound)
        val renamed = current.rename(name)
        items[id] = renamed
        return DomainResult.Success(renamed)
    }

    override suspend fun deletePlaylist(id: PlaylistId): DomainResult<Unit> {
        if (items.remove(id) == null) return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(Unit)
    }

    override suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> {
        val current = items[playlistId] ?: return DomainResult.Failure(DomainError.NotFound)
        if (trackId in current.trackIds) return DomainResult.Failure(DomainError.DuplicateTrack)
        val updated = current.add(trackId)
        items[playlistId] = updated
        return DomainResult.Success(updated)
    }

    override suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> {
        val current = items[playlistId] ?: return DomainResult.Failure(DomainError.NotFound)
        val updated = current.remove(trackId)
        items[playlistId] = updated
        return DomainResult.Success(updated)
    }
}

private class RecordingHistory : PlaybackHistoryRepository {
    val recorded = mutableListOf<Pair<TrackId, Instant>>()

    override suspend fun getRecentlyPlayed(limit: Int): DomainResult<List<TrackId>> =
        DomainResult.Success(emptyList())

    override suspend fun addToHistory(trackId: TrackId, playedAt: Instant): DomainResult<Unit> {
        recorded += trackId to playedAt
        return DomainResult.Success(Unit)
    }

    override suspend fun clearHistory(): DomainResult<Unit> = DomainResult.Success(Unit)
}

private class Catalog(
    tracks: List<Track>,
    private val failure: DomainError? = null,
) : TrackRepository {
    private val tracks = tracks.associateBy { it.id }

    override suspend fun getPopularTracks(): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun searchTracks(query: String): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun getTrackById(id: TrackId): DomainResult<Track> {
        failure?.let { return DomainResult.Failure(it) }
        return tracks[id]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)
    }

    override suspend fun getTracksByArtist(artistId: ArtistId): DomainResult<List<Track>> =
        DomainResult.Success(emptyList())

    override suspend fun getTrackStreamUrl(id: TrackId): DomainResult<String> =
        DomainResult.Failure(DomainError.NotFound)
}

private fun sampleTrack(id: Long, title: String): Track = Track(
    id = TrackId(id),
    title = title,
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = null,
    durationMs = 180_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by-nc-nd/3.0/",
    source = ContentSource(provider = "Jamendo", pageUrl = "https://www.jamendo.com/track/$id"),
    releaseDate = LocalDate.of(2024, 5, 2),
)
