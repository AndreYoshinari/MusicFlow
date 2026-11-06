package br.com.musicflow.feature.library

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
import br.com.musicflow.common.domain.usecase.CreatePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentlyPlayedUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
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
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addingAndRemovingAFavoriteUpdatesTheListImmediately() {
        val aurora = sampleTrack(1, "Aurora")
        val favorites = MemoryFavorites(listOf(aurora))
        val viewModel = FavoritesViewModel(
            observeFavorites = GetFavoritesUseCase(favorites),
            toggleFavorite = ToggleFavoriteUseCase(favorites),
        )

        viewModel.toggle(aurora.id.value)
        assertEquals(LibraryListState.Empty, viewModel.state.value.tracks)

        viewModel.toggle(aurora.id.value)
        val ready = viewModel.state.value.tracks as LibraryListState.Ready
        assertEquals(listOf("Aurora"), ready.items.map { it.title })
        assertEquals(null, viewModel.state.value.actionError)
    }

    @Test
    fun favoriteMissingFromTheDeviceSurfacesTheError() {
        val favorites = MemoryFavorites(emptyList())
        val viewModel = FavoritesViewModel(
            observeFavorites = GetFavoritesUseCase(favorites),
            toggleFavorite = ToggleFavoriteUseCase(favorites),
        )

        viewModel.toggle(9)

        assertEquals(LibraryListState.Empty, viewModel.state.value.tracks)
        assertEquals(DomainError.NotFound, viewModel.state.value.actionError)
    }

    @Test
    fun creatingAPlaylistShowsItWithoutAnotherRefresh() {
        val repository = MemoryPlaylists()
        val viewModel = PlaylistsViewModel(
            playlists = GetPlaylistsUseCase(repository),
            createPlaylist = CreatePlaylistUseCase(repository),
        )

        viewModel.create("   ")
        assertEquals(DomainError.InvalidName, viewModel.state.value.actionError)
        assertEquals(LibraryListState.Empty, viewModel.state.value.playlists)

        viewModel.create("  Noite violeta  ")
        val ready = viewModel.state.value.playlists as LibraryListState.Ready
        assertEquals(listOf("Noite violeta"), ready.items.map { it.name })
        assertEquals(null, viewModel.state.value.actionError)

        viewModel.create("Manhã")
        val both = viewModel.state.value.playlists as LibraryListState.Ready
        assertEquals(listOf("Noite violeta", "Manhã"), both.items.map { it.name })
    }

    @Test
    fun recentTracksStayInTheDateOrderReturnedByHistory() {
        val history = FixedHistory(listOf(TrackId(3), TrackId(1)))
        val tracks = mapOf(
            TrackId(1) to sampleTrack(1, "Aurora"),
            TrackId(3) to sampleTrack(3, "Tarde"),
        )
        val viewModel = HistoryViewModel(
            recentTracks = GetRecentTracksUseCase(
                history = GetRecentlyPlayedUseCase(history),
                details = GetTrackDetailsUseCase(Catalog(tracks)),
            ),
        )

        val ready = viewModel.state.value.tracks as LibraryListState.Ready
        assertEquals(listOf("Tarde", "Aurora"), ready.items.map { it.title })
    }

    @Test
    fun emptyAndFailedHistoryStayVisible() {
        val empty = HistoryViewModel(
            recentTracks = GetRecentTracksUseCase(
                history = GetRecentlyPlayedUseCase(FixedHistory(emptyList())),
                details = GetTrackDetailsUseCase(Catalog(emptyMap())),
            ),
        )
        assertEquals(LibraryListState.Empty, empty.state.value.tracks)

        val failed = HistoryViewModel(
            recentTracks = GetRecentTracksUseCase(
                history = GetRecentlyPlayedUseCase(FixedHistory(failure = DomainError.Offline)),
                details = GetTrackDetailsUseCase(Catalog(emptyMap())),
            ),
        )
        assertEquals(LibraryListState.Failed(DomainError.Offline), failed.state.value.tracks)
    }
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
    private var nextId = 1L

    override suspend fun getPlaylists(): DomainResult<List<Playlist>> = DomainResult.Success(items.values.toList())

    override suspend fun getPlaylistById(id: PlaylistId): DomainResult<Playlist> =
        items[id]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)

    override suspend fun createPlaylist(name: String): DomainResult<Playlist> {
        val playlist = Playlist(PlaylistId(nextId++), name)
        items[playlist.id] = playlist
        return DomainResult.Success(playlist)
    }

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

private class FixedHistory(
    private val ids: List<TrackId> = emptyList(),
    private val failure: DomainError? = null,
) : PlaybackHistoryRepository {
    override suspend fun getRecentlyPlayed(limit: Int): DomainResult<List<TrackId>> =
        failure?.let { DomainResult.Failure(it) } ?: DomainResult.Success(ids.take(limit))

    override suspend fun addToHistory(trackId: TrackId, playedAt: Instant): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun clearHistory(): DomainResult<Unit> = DomainResult.Success(Unit)
}

private class Catalog(private val tracks: Map<TrackId, Track>) : TrackRepository {
    override suspend fun getPopularTracks(): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun searchTracks(query: String): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun getTrackById(id: TrackId): DomainResult<Track> =
        tracks[id]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)

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
