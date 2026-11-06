package br.com.musicflow.feature.player

import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingFavoriteViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleUpdatesTheFavoriteSetFromTheLocalFlow() {
        val favorites = MemoryFavorites(listOf(sample(1, "Aurora")))
        val viewModel = NowPlayingFavoriteViewModel(
            observeFavorites = GetFavoritesUseCase(favorites),
            toggleFavorite = ToggleFavoriteUseCase(favorites),
        )

        assertEquals(setOf(1L), viewModel.favoriteIds.value)
        viewModel.toggle(1)
        assertEquals(emptySet<Long>(), viewModel.favoriteIds.value)
        viewModel.toggle(1)
        assertEquals(setOf(1L), viewModel.favoriteIds.value)
        assertEquals(null, viewModel.actionError.value)
    }

    @Test
    fun missingTrackSurfacesTheFavoriteError() {
        val viewModel = NowPlayingFavoriteViewModel(
            observeFavorites = GetFavoritesUseCase(MemoryFavorites(emptyList())),
            toggleFavorite = ToggleFavoriteUseCase(MemoryFavorites(emptyList())),
        )

        viewModel.toggle(9)

        assertEquals(DomainError.NotFound, viewModel.actionError.value)
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

private fun sample(id: Long, title: String): Track = Track(
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
