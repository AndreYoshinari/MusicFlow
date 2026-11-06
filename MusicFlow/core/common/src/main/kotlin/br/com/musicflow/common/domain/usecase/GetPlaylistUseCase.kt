package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.repository.PlaylistRepository

class GetPlaylistUseCase(
    private val playlists: PlaylistRepository,
) {
    suspend operator fun invoke(id: PlaylistId): DomainResult<Playlist> = playlists.getPlaylistById(id)
}
