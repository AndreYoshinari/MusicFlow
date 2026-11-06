package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.data.remote.JamendoArtistDataSource
import br.com.musicflow.network.NetworkError
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoAlbumDto
import br.com.musicflow.network.dto.JamendoArtistDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ArtistRepositoryImplTest {
    private val remote = mockk<JamendoArtistDataSource>()
    private val local = MemoryArtistStore()
    private var now = Instant.parse("2026-11-05T18:00:00Z")
    private val repository = ArtistRepositoryImpl(remote, local, RequestGate()) { now }

    @Test
    fun artistDetailIsCachedUntilItExpires() = runTest {
        val id = ArtistId(7)
        coEvery { remote.getArtist(id) } returns NetworkResult.Success(listOf(artistDto()))

        repository.getArtistById(id)
        val cached = repository.getArtistById(id)

        assertEquals("Luna", (cached as DomainResult.Success).value.name)
        coVerify(exactly = 1) { remote.getArtist(id) }
    }

    @Test
    fun offlineArtistSearchWithoutCacheReturnsTheFailure() = runTest {
        coEvery { remote.searchArtists("luna") } returns NetworkResult.Failure(NetworkError.Timeout)

        val result = repository.searchArtists("luna")

        assertEquals(DomainResult.Failure(DomainError.Timeout), result)
    }

    @Test
    fun aPreviousArtistSearchSurvivesALaterOutage() = runTest {
        coEvery { remote.searchArtists("luna") } returns NetworkResult.Success(listOf(artistDto()))
        repository.searchArtists("luna")
        now = now.plusMillis(15 * 60 * 1_000L + 1)
        coEvery { remote.searchArtists("luna") } returns NetworkResult.Failure(NetworkError.Offline)

        val result = repository.searchArtists("luna")

        assertEquals("Luna", (result as DomainResult.Success).value.single().name)
    }

    @Test
    fun albumsAreStoredAndNotRequestedAgainWhileFresh() = runTest {
        val artistId = ArtistId(7)
        coEvery { remote.getAlbums(artistId) } returns NetworkResult.Success(listOf(albumDto()))

        val first = repository.getAlbumsByArtist(artistId)
        val second = repository.getAlbumsByArtist(artistId)

        assertEquals("Noite", (first as DomainResult.Success).value.single().title)
        assertEquals(first, second)
        coVerify(exactly = 1) { remote.getAlbums(artistId) }
    }

    @Test
    fun featuredArtistsStayCachedUntilTheyExpire() = runTest {
        coEvery { remote.getFeaturedArtists() } returns NetworkResult.Success(listOf(artistDto()))

        repository.getFeaturedArtists()
        val cached = repository.getFeaturedArtists()

        assertEquals("Luna", (cached as DomainResult.Success).value.single().name)
        coVerify(exactly = 1) { remote.getFeaturedArtists() }
    }

    @Test
    fun popularAlbumsStayCachedUntilTheyExpire() = runTest {
        coEvery { remote.getPopularAlbums() } returns NetworkResult.Success(listOf(albumDto()))

        val first = repository.getPopularAlbums()
        val second = repository.getPopularAlbums()

        assertEquals("Noite", (first as DomainResult.Success).value.single().title)
        assertEquals(first, second)
        coVerify(exactly = 1) { remote.getPopularAlbums() }
    }
}

private fun artistDto() = JamendoArtistDto(
    id = "7",
    name = "Luna",
    image = "https://example.com/artist.jpg",
    shareUrl = "https://www.jamendo.com/artist/7",
)

private fun albumDto() = JamendoAlbumDto(
    id = "3",
    name = "Noite",
    releaseDate = "2024-05-02",
    artistId = "7",
    artistName = "Luna",
    image = "https://example.com/album.jpg",
    shareUrl = "https://www.jamendo.com/album/3",
)
