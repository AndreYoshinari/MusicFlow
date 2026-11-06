package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PlaybackHistoryUseCaseTest {
    private val repository = mockk<PlaybackHistoryRepository>()
    private val trackId = TrackId(1)
    private val playedAt = Instant.parse("2026-11-05T18:00:00Z")

    @Test
    fun recentlyPlayedReturnsDistinctTracks() = runTest {
        val tracks = listOf(trackId, TrackId(2))
        coEvery { repository.getRecentlyPlayed(20) } returns DomainResult.Success(tracks)

        val result = GetRecentlyPlayedUseCase(repository)()

        assertEquals(DomainResult.Success(tracks), result)
    }

    @Test
    fun recentlyPlayedCanBeEmpty() = runTest {
        coEvery { repository.getRecentlyPlayed(20) } returns DomainResult.Success(emptyList())

        val result = GetRecentlyPlayedUseCase(repository)()

        assertEquals(DomainResult.Success(emptyList<TrackId>()), result)
    }

    @Test
    fun recentlyPlayedRejectsANonPositiveLimit() = runTest {
        val result = GetRecentlyPlayedUseCase(repository)(limit = 0)

        assertEquals(DomainResult.Failure(DomainError.InvalidArgument), result)
        coVerify(exactly = 0) { repository.getRecentlyPlayed(any()) }
    }

    @Test
    fun recentlyPlayedPropagatesUnavailable() = runTest {
        coEvery { repository.getRecentlyPlayed(20) } returns DomainResult.Failure(DomainError.Unavailable)

        val result = GetRecentlyPlayedUseCase(repository)()

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }

    @Test
    fun addToHistoryRecordsThePlay() = runTest {
        coEvery { repository.addToHistory(trackId, playedAt) } returns DomainResult.Success(Unit)

        val result = AddTrackToHistoryUseCase(repository)(trackId, playedAt)

        assertEquals(DomainResult.Success(Unit), result)
    }

    @Test
    fun addToHistoryPropagatesUnavailable() = runTest {
        coEvery { repository.addToHistory(trackId, playedAt) } returns DomainResult.Failure(DomainError.Unavailable)

        val result = AddTrackToHistoryUseCase(repository)(trackId, playedAt)

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }
}
