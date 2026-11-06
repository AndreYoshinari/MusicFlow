package br.com.musicflow.common.domain.usecase

import app.cash.turbine.test
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.testTrack
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteUseCaseTest {
    private val repository = mockk<FavoriteRepository>()
    private val trackId = TrackId(1)

    @Test
    fun favoritesEmitTheCurrentList() = runTest {
        val favorites = listOf(testTrack())
        every { repository.observeFavorites() } returns flowOf(favorites)

        GetFavoritesUseCase(repository)().test {
            assertEquals(favorites, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun favoritesCanBeEmpty() = runTest {
        every { repository.observeFavorites() } returns flowOf(emptyList())

        GetFavoritesUseCase(repository)().test {
            assertEquals(emptyList<Nothing>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun toggleAddsATrackThatIsNotFavorite() = runTest {
        coEvery { repository.isFavorite(trackId) } returns DomainResult.Success(false)
        coEvery { repository.addFavorite(trackId) } returns DomainResult.Success(Unit)

        val result = ToggleFavoriteUseCase(repository)(trackId)

        assertEquals(DomainResult.Success(true), result)
        coVerify(exactly = 0) { repository.removeFavorite(trackId) }
    }

    @Test
    fun toggleRemovesAFavoriteTrack() = runTest {
        coEvery { repository.isFavorite(trackId) } returns DomainResult.Success(true)
        coEvery { repository.removeFavorite(trackId) } returns DomainResult.Success(Unit)

        val result = ToggleFavoriteUseCase(repository)(trackId)

        assertEquals(DomainResult.Success(false), result)
        coVerify(exactly = 0) { repository.addFavorite(trackId) }
    }

    @Test
    fun toggleStopsWhenTheCurrentStateCannotBeRead() = runTest {
        coEvery { repository.isFavorite(trackId) } returns DomainResult.Failure(DomainError.Unavailable)

        val result = ToggleFavoriteUseCase(repository)(trackId)

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
        coVerify(exactly = 0) { repository.addFavorite(trackId) }
        coVerify(exactly = 0) { repository.removeFavorite(trackId) }
    }

    @Test
    fun togglePropagatesAFailedWrite() = runTest {
        coEvery { repository.isFavorite(trackId) } returns DomainResult.Success(false)
        coEvery { repository.addFavorite(trackId) } returns DomainResult.Failure(DomainError.Unavailable)

        val result = ToggleFavoriteUseCase(repository)(trackId)

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }
}
