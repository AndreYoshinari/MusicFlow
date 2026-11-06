package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.repository.PlaylistRepository

class DeletePlaylistUseCase(
    private val playlists: PlaylistRepository,
) {
    suspend operator fun invoke(id: PlaylistId): DomainResult<Unit> = playlists.deletePlaylist(id)
}
