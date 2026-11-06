package br.com.musicflow.common.domain.usecase

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
import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalDate

class CatalogUseCaseTest {
    @Test
    fun featuredArtistsAndPopularAlbumsReturnTheCatalog() = runTest {
        val artists = mockk<ArtistRepository>()
        val artist = artist()
        val album = album()
        coEvery { artists.getFeaturedArtists() } returns DomainResult.Success(listOf(artist))
        coEvery { artists.getPopularAlbums() } returns DomainResult.Success(listOf(album))

        assertEquals(listOf(artist), (GetFeaturedArtistsUseCase(artists)() as DomainResult.Success).value)
        assertEquals(listOf(album), (GetPopularAlbumsUseCase(artists)() as DomainResult.Success).value)
    }

    @Test
    fun featuredArtistsAndPopularAlbumsPropagateOffline() = runTest {
        val artists = mockk<ArtistRepository>()
        coEvery { artists.getFeaturedArtists() } returns DomainResult.Failure(DomainError.Offline)
        coEvery { artists.getPopularAlbums() } returns DomainResult.Failure(DomainError.Offline)

        assertEquals(DomainError.Offline, (GetFeaturedArtistsUseCase(artists)() as DomainResult.Failure).error)
        assertEquals(DomainError.Offline, (GetPopularAlbumsUseCase(artists)() as DomainResult.Failure).error)
    }

    @Test
    fun playlistLookupReturnsThePlaylistOrNotFound() = runTest {
        val playlists = mockk<PlaylistRepository>()
        val playlist = Playlist(PlaylistId(4), "Noite", emptyList())
        coEvery { playlists.getPlaylistById(PlaylistId(4)) } returns DomainResult.Success(playlist)
        coEvery { playlists.getPlaylistById(PlaylistId(9)) } returns DomainResult.Failure(DomainError.NotFound)

        assertEquals(playlist, (GetPlaylistUseCase(playlists)(PlaylistId(4)) as DomainResult.Success).value)
        assertEquals(DomainError.NotFound, (GetPlaylistUseCase(playlists)(PlaylistId(9)) as DomainResult.Failure).error)
    }

    @Test
    fun popularTracksExposeThePagingFlow() {
        val paging = mockk<TrackPagingRepository>()
        val pages = flowOf(PagingData.empty<Track>())
        every { paging.popularTracks() } returns pages

        assertSame(pages, ObservePopularTracksUseCase(paging)())
    }
}

private fun artist() = Artist(
    id = ArtistId(7),
    name = "Luna Park",
    imageUrl = null,
    source = ContentSource("Jamendo", "https://www.jamendo.com/artist/7"),
)

private fun album() = Album(
    id = AlbumId(3),
    title = "Noite",
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    artworkUrl = null,
    releaseDate = LocalDate.of(2024, 5, 2),
    source = ContentSource("Jamendo", "https://www.jamendo.com/album/3"),
)
