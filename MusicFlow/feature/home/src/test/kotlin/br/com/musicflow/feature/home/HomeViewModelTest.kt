package br.com.musicflow.feature.home

import androidx.paging.PagingData
import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.usecase.GetFeaturedArtistsUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularAlbumsUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.ObservePopularTracksUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val featured = mockk<GetFeaturedArtistsUseCase>()
    private val albums = mockk<GetPopularAlbumsUseCase>()
    private val recent = mockk<GetRecentTracksUseCase>()
    private val playlists = mockk<GetPlaylistsUseCase>()
    private val paging = ObservePopularTracksUseCase(
        mockk(relaxed = true) {
            io.mockk.every { popularTracks() } returns flowOf(PagingData.empty())
        },
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sectionsExposeReadyCardsAndTheGreetingUsesTheClock() {
        coEvery { featured() } returns DomainResult.Success(listOf(artist()))
        coEvery { albums() } returns DomainResult.Success(listOf(album()))
        coEvery { recent() } returns DomainResult.Success(listOf(track()))
        coEvery { playlists() } returns DomainResult.Success(
            listOf(Playlist(PlaylistId(4), "Noite violeta", emptyList())),
        )

        val state = viewModel().state.value

        assertEquals(GreetingPeriod.Morning, state.greeting)
        assertEquals(ArtistCardModel(7, "Luna Park", "https://example.com/artist.jpg"), ready(state.artists).single())
        assertEquals("Noite", ready(state.albums).single().title)
        assertEquals("https://example.com/album.jpg", ready(state.albums).single().artworkUrl)
        assertEquals("Aurora", ready(state.recent).single().title)
        assertEquals(0, ready(state.playlists).single().trackCount)
    }

    @Test
    fun anEmptyCatalogBecomesTheEmptyState() {
        coEvery { featured() } returns DomainResult.Success(emptyList())
        coEvery { albums() } returns DomainResult.Success(emptyList())
        coEvery { recent() } returns DomainResult.Success(emptyList())
        coEvery { playlists() } returns DomainResult.Success(emptyList())

        val state = viewModel().state.value

        assertEquals(SectionState.Empty, state.artists)
        assertEquals(SectionState.Empty, state.albums)
        assertEquals(SectionState.Empty, state.recent)
        assertEquals(SectionState.Empty, state.playlists)
    }

    @Test
    fun aFailureStaysInThatSection() {
        coEvery { featured() } returns DomainResult.Failure(DomainError.Offline)
        coEvery { albums() } returns DomainResult.Success(listOf(album()))
        coEvery { recent() } returns DomainResult.Success(emptyList())
        coEvery { playlists() } returns DomainResult.Success(emptyList())

        val viewModel = viewModel()

        assertEquals(DomainError.Offline, (viewModel.state.value.artists as SectionState.Failed).error)
        assertTrue(viewModel.state.value.albums is SectionState.Ready)

        coEvery { featured() } returns DomainResult.Success(listOf(artist()))
        viewModel.retryArtists()

        assertEquals("Luna Park", ready(viewModel.state.value.artists).single().name)
        assertTrue(viewModel.state.value.albums is SectionState.Ready)
    }

    private fun viewModel() = HomeViewModel(
        observePopularTracks = paging,
        featuredArtists = featured,
        popularAlbums = albums,
        recentTracks = recent,
        playlists = playlists,
        clock = { LocalTime.of(9, 30) },
    )

    private fun <T> ready(state: SectionState<T>): List<T> = (state as SectionState.Ready).items
}

private fun artist() = Artist(
    id = ArtistId(7),
    name = "Luna Park",
    imageUrl = "https://example.com/artist.jpg",
    source = ContentSource("Jamendo", "https://www.jamendo.com/artist/7"),
)

private fun album() = Album(
    id = AlbumId(3),
    title = "Noite",
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    artworkUrl = "https://example.com/album.jpg",
    releaseDate = LocalDate.of(2024, 5, 2),
    source = ContentSource("Jamendo", "https://www.jamendo.com/album/3"),
)

private fun track() = Track(
    id = br.com.musicflow.common.domain.TrackId(1),
    title = "Aurora",
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = br.com.musicflow.common.domain.AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = "https://example.com/art.jpg",
    durationMs = 180_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    source = ContentSource("Jamendo", "https://www.jamendo.com/track/1"),
    releaseDate = null,
)
