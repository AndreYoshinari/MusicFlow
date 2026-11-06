package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistUseCaseTest {
    private val repository = mockk<PlaylistRepository>()
    private val playlistId = PlaylistId(4)
    private val trackId = TrackId(1)

    @Test
    fun playlistsReturnTheLibrary() = runTest {
        val playlists = listOf(Playlist(playlistId, "Noite"))
        coEvery { repository.getPlaylists() } returns DomainResult.Success(playlists)

        val result = GetPlaylistsUseCase(repository)()

        assertEquals(DomainResult.Success(playlists), result)
    }

    @Test
    fun playlistsCanBeEmpty() = runTest {
        coEvery { repository.getPlaylists() } returns DomainResult.Success(emptyList())

        val result = GetPlaylistsUseCase(repository)()

        assertEquals(DomainResult.Success(emptyList<Playlist>()), result)
    }

    @Test
    fun playlistsPropagateUnavailable() = runTest {
        coEvery { repository.getPlaylists() } returns DomainResult.Failure(DomainError.Unavailable)

        val result = GetPlaylistsUseCase(repository)()

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }

    @Test
    fun createPlaylistTrimsTheName() = runTest {
        val created = Playlist(playlistId, "Noite")
        coEvery { repository.createPlaylist("Noite") } returns DomainResult.Success(created)

        val result = CreatePlaylistUseCase(repository)("  Noite  ")

        assertEquals(DomainResult.Success(created), result)
    }

    @Test
    fun blankPlaylistNameIsRejected() = runTest {
        val result = CreatePlaylistUseCase(repository)("   ")

        assertEquals(DomainResult.Failure(DomainError.InvalidName), result)
        coVerify(exactly = 0) { repository.createPlaylist(any()) }
    }

    @Test
    fun createPlaylistPropagatesUnavailable() = runTest {
        coEvery { repository.createPlaylist("Noite") } returns DomainResult.Failure(DomainError.Unavailable)

        val result = CreatePlaylistUseCase(repository)("Noite")

        assertEquals(DomainResult.Failure(DomainError.Unavailable), result)
    }

    @Test
    fun addTrackAppendsItToThePlaylist() = runTest {
        val current = Playlist(playlistId, "Noite")
        val updated = current.add(trackId)
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Success(current)
        coEvery { repository.addTrack(playlistId, trackId) } returns DomainResult.Success(updated)

        val result = AddTrackToPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Success(updated), result)
    }

    @Test
    fun addTrackRejectsADuplicate() = runTest {
        val current = Playlist(playlistId, "Noite", listOf(trackId))
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Success(current)

        val result = AddTrackToPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Failure(DomainError.DuplicateTrack), result)
        coVerify(exactly = 0) { repository.addTrack(playlistId, trackId) }
    }

    @Test
    fun addTrackFailsWhenThePlaylistDoesNotExist() = runTest {
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Failure(DomainError.NotFound)

        val result = AddTrackToPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
        coVerify(exactly = 0) { repository.addTrack(playlistId, trackId) }
    }

    @Test
    fun removeTrackUpdatesThePlaylist() = runTest {
        val current = Playlist(playlistId, "Noite", listOf(trackId))
        val updated = current.remove(trackId)
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Success(current)
        coEvery { repository.removeTrack(playlistId, trackId) } returns DomainResult.Success(updated)

        val result = RemoveTrackFromPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Success(updated), result)
    }

    @Test
    fun removeTrackKeepsThePlaylistWhenTheTrackIsAbsent() = runTest {
        val current = Playlist(playlistId, "Noite")
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Success(current)

        val result = RemoveTrackFromPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Success(current), result)
        coVerify(exactly = 0) { repository.removeTrack(playlistId, trackId) }
    }

    @Test
    fun removeTrackFailsWhenThePlaylistDoesNotExist() = runTest {
        coEvery { repository.getPlaylistById(playlistId) } returns DomainResult.Failure(DomainError.NotFound)

        val result = RemoveTrackFromPlaylistUseCase(repository)(playlistId, trackId)

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }
}
