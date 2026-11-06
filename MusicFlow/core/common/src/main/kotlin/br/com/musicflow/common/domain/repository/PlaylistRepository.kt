package br.com.musicflow.common.domain.repository

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.TrackId

interface PlaylistRepository {
    suspend fun getPlaylists(): DomainResult<List<Playlist>>

    suspend fun getPlaylistById(id: PlaylistId): DomainResult<Playlist>

    suspend fun createPlaylist(name: String): DomainResult<Playlist>

    suspend fun renamePlaylist(id: PlaylistId, name: String): DomainResult<Playlist>

    suspend fun deletePlaylist(id: PlaylistId): DomainResult<Unit>

    suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist>

    suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist>
}
