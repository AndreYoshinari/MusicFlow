package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.repository.PlaylistRepository

class RenamePlaylistUseCase(
    private val playlists: PlaylistRepository,
) {
    suspend operator fun invoke(id: PlaylistId, name: String): DomainResult<Playlist> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return DomainResult.Failure(DomainError.InvalidName)
        return playlists.renamePlaylist(id, trimmed)
    }
}
