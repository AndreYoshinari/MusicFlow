package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.repository.ArtistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchArtistsUseCaseTest {
    private val repository = mockk<ArtistRepository>()

    @Test
    fun searchReturnsMatchingArtists() = runTest {
        val artists = listOf(artist())
        coEvery { repository.searchArtists("luna") } returns DomainResult.Success(artists)

        val result = SearchArtistsUseCase(repository)("  luna  ")

        assertEquals(DomainResult.Success(artists), result)
    }

    @Test
    fun blankSearchDoesNotCallTheRepository() = runTest {
        val result = SearchArtistsUseCase(repository)("   ")

        assertEquals(DomainResult.Success(emptyList<Artist>()), result)
        coVerify(exactly = 0) { repository.searchArtists(any()) }
    }

    @Test
    fun searchPropagatesUnavailable() = runTest {
        coEvery { repository.searchArtists("luna") } returns DomainResult.Failure(DomainError.Unavailable)

        val result = SearchArtistsUseCase(repository)("luna")

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }

    private fun artist() = Artist(
        id = ArtistId(7),
        name = "Luna Park",
        imageUrl = "https://example.com/artist.jpg",
        source = ContentSource("Jamendo", "https://www.jamendo.com/artist/7"),
    )
}
