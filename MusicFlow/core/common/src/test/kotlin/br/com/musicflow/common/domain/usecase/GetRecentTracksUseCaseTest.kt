package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.testTrack
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRecentTracksUseCaseTest {
    private val history = mockk<GetRecentlyPlayedUseCase>()
    private val details = mockk<GetTrackDetailsUseCase>()
    private val useCase = GetRecentTracksUseCase(history, details)

    @Test
    fun recentTracksKeepHistoryOrderAndSkipMissingOnes() = runTest {
        val first = testTrack(id = 1, title = "Aurora")
        val third = testTrack(id = 3, title = "Maré")
        coEvery { history() } returns DomainResult.Success(listOf(TrackId(1), TrackId(2), TrackId(3)))
        coEvery { details(TrackId(1)) } returns DomainResult.Success(first)
        coEvery { details(TrackId(2)) } returns DomainResult.Failure(DomainError.NotFound)
        coEvery { details(TrackId(3)) } returns DomainResult.Success(third)

        val result = useCase()

        assertEquals(DomainResult.Success(listOf(first, third)), result)
    }

    @Test
    fun anEmptyHistoryStaysEmpty() = runTest {
        coEvery { history() } returns DomainResult.Success(emptyList())

        assertEquals(DomainResult.Success(emptyList<Nothing>()), useCase())
    }

    @Test
    fun aLookupFailureStopsTheList() = runTest {
        coEvery { history() } returns DomainResult.Success(listOf(TrackId(1)))
        coEvery { details(TrackId(1)) } returns DomainResult.Failure(DomainError.Offline)

        assertEquals(DomainResult.Failure(DomainError.Offline), useCase())
    }
}
