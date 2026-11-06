package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.common.domain.testTrack
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackUseCaseTest {
    private val repository = mockk<TrackRepository>()

    @Test
    fun popularTracksReturnTheCatalog() = runTest {
        val tracks = listOf(testTrack())
        coEvery { repository.getPopularTracks() } returns DomainResult.Success(tracks)

        val result = GetPopularTracksUseCase(repository)()

        assertEquals(DomainResult.Success(tracks), result)
    }

    @Test
    fun popularTracksCanBeEmpty() = runTest {
        coEvery { repository.getPopularTracks() } returns DomainResult.Success(emptyList())

        val result = GetPopularTracksUseCase(repository)()

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
    }

    @Test
    fun popularTracksPropagateOffline() = runTest {
        coEvery { repository.getPopularTracks() } returns DomainResult.Failure(DomainError.Offline)

        val result = GetPopularTracksUseCase(repository)()

        assertEquals(DomainResult.Failure(DomainError.Offline), result)
    }

    @Test
    fun searchReturnsMatchingTracks() = runTest {
        val tracks = listOf(testTrack(title = "Aurora"))
        coEvery { repository.searchTracks("aurora") } returns DomainResult.Success(tracks)

        val result = SearchTracksUseCase(repository)("  aurora  ")

        assertEquals(DomainResult.Success(tracks), result)
    }

    @Test
    fun searchReturnsAnEmptyListWhenNothingMatches() = runTest {
        coEvery { repository.searchTracks("nada") } returns DomainResult.Success(emptyList())

        val result = SearchTracksUseCase(repository)("nada")

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
    }

    @Test
    fun blankSearchDoesNotCallTheRepository() = runTest {
        val result = SearchTracksUseCase(repository)("   ")

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
        coVerify(exactly = 0) { repository.searchTracks(any()) }
    }

    @Test
    fun searchPropagatesUnavailable() = runTest {
        coEvery { repository.searchTracks("aurora") } returns DomainResult.Failure(DomainError.Unavailable)

        val result = SearchTracksUseCase(repository)("aurora")

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }

    @Test
    fun trackDetailsReturnTheTrack() = runTest {
        val track = testTrack()
        coEvery { repository.getTrackById(track.id) } returns DomainResult.Success(track)

        val result = GetTrackDetailsUseCase(repository)(track.id)

        assertEquals(DomainResult.Success(track), result)
    }

    @Test
    fun missingTrackDetailsAreNotFound() = runTest {
        val id = TrackId(9)
        coEvery { repository.getTrackById(id) } returns DomainResult.Failure(DomainError.NotFound)

        val result = GetTrackDetailsUseCase(repository)(id)

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }

    @Test
    fun tracksByArtistReturnTheCatalog() = runTest {
        val artistId = ArtistId(7)
        val tracks = listOf(testTrack())
        coEvery { repository.getTracksByArtist(artistId) } returns DomainResult.Success(tracks)

        val result = GetTracksByArtistUseCase(repository)(artistId)

        assertEquals(DomainResult.Success(tracks), result)
    }

    @Test
    fun tracksByArtistCanBeEmpty() = runTest {
        val artistId = ArtistId(7)
        coEvery { repository.getTracksByArtist(artistId) } returns DomainResult.Success(emptyList())

        val result = GetTracksByArtistUseCase(repository)(artistId)

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
    }

    @Test
    fun tracksByArtistPropagateOffline() = runTest {
        val artistId = ArtistId(7)
        coEvery { repository.getTracksByArtist(artistId) } returns DomainResult.Failure(DomainError.Offline)

        val result = GetTracksByArtistUseCase(repository)(artistId)

        assertEquals(DomainResult.Failure(DomainError.Offline), result)
    }

    @Test
    fun streamUrlReturnsTheAddress() = runTest {
        val id = TrackId(1)
        coEvery { repository.getTrackStreamUrl(id) } returns DomainResult.Success("https://example.com/stream/1")

        val result = GetTrackStreamUrlUseCase(repository)(id)

        assertEquals(DomainResult.Success("https://example.com/stream/1"), result)
    }

    @Test
    fun missingStreamIsUnavailable() = runTest {
        val id = TrackId(1)
        coEvery { repository.getTrackStreamUrl(id) } returns DomainResult.Failure(DomainError.StreamUnavailable)

        val result = GetTrackStreamUrlUseCase(repository)(id)

        assertEquals(DomainResult.Failure(DomainError.StreamUnavailable), result)
    }

    @Test
    fun missingTrackHasNoStream() = runTest {
        val id = TrackId(1)
        coEvery { repository.getTrackStreamUrl(id) } returns DomainResult.Failure(DomainError.NotFound)

        val result = GetTrackStreamUrlUseCase(repository)(id)

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }
}
