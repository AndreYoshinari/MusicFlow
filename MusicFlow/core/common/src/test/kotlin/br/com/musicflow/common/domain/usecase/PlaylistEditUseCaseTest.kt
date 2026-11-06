package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistEditUseCaseTest {
    private val repository = mockk<PlaylistRepository>()
    private val id = PlaylistId(4)

    @Test
    fun renameTrimsTheNameBeforeSaving() = runTest {
        val renamed = Playlist(id, "Noite violeta")
        coEvery { repository.renamePlaylist(id, "Noite violeta") } returns DomainResult.Success(renamed)

        val result = RenamePlaylistUseCase(repository)(id, "  Noite violeta  ")

        assertEquals(DomainResult.Success(renamed), result)
    }

    @Test
    fun blankRenameDoesNotCallTheRepository() = runTest {
        val result = RenamePlaylistUseCase(repository)(id, "   ")

        assertEquals(DomainResult.Failure(DomainError.InvalidName), result)
        coVerify(exactly = 0) { repository.renamePlaylist(id, any()) }
    }

    @Test
    fun deleteRemovesThePlaylist() = runTest {
        coEvery { repository.deletePlaylist(id) } returns DomainResult.Success(Unit)

        val result = DeletePlaylistUseCase(repository)(id)

        assertEquals(DomainResult.Success(Unit), result)
    }

    @Test
    fun missingPlaylistIsReturnedAsNotFound() = runTest {
        coEvery { repository.getPlaylistById(id) } returns DomainResult.Failure(DomainError.NotFound)

        val result = GetPlaylistUseCase(repository)(id)

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }
}
