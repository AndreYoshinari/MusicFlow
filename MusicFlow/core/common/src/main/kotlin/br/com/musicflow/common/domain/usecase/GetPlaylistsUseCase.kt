package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.repository.PlaylistRepository

class GetPlaylistsUseCase(
    private val playlists: PlaylistRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Playlist>> = playlists.getPlaylists()
}
