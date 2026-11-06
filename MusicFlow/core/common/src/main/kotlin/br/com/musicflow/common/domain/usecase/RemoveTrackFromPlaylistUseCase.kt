package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaylistRepository

class RemoveTrackFromPlaylistUseCase(
    private val playlists: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> {
        return when (val current = playlists.getPlaylistById(playlistId)) {
            is DomainResult.Failure -> current
            is DomainResult.Success -> if (trackId !in current.value.trackIds) {
                current
            } else {
                playlists.removeTrack(playlistId, trackId)
            }
        }
    }
}
