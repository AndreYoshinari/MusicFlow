package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.database.dao.PlaylistDao
import br.com.musicflow.database.dao.PlaylistTrackWrite
import br.com.musicflow.database.entity.PlaylistEntity
import java.time.Instant

internal interface PlaylistLocalDataSource {
    suspend fun getAll(): List<Playlist>

    suspend fun get(id: PlaylistId): Playlist?

    suspend fun create(name: String, createdAt: Instant): Playlist

    suspend fun rename(id: PlaylistId, name: String): Playlist?

    suspend fun delete(id: PlaylistId): Boolean

    suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): PlaylistTrackWrite

    suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): Boolean
}

internal class RoomPlaylistLocalDataSource(
    private val playlists: PlaylistDao,
) : PlaylistLocalDataSource {
    override suspend fun getAll(): List<Playlist> = playlists.getPlaylists().map { it.toPlaylist() }

    override suspend fun get(id: PlaylistId): Playlist? = playlists.getPlaylist(id.value)?.toPlaylist()

    override suspend fun create(name: String, createdAt: Instant): Playlist {
        val id = playlists.insertPlaylist(PlaylistEntity(name = name, createdAt = createdAt.toEpochMilli()))
        return Playlist(PlaylistId(id), name)
    }

    override suspend fun rename(id: PlaylistId, name: String): Playlist? {
        if (playlists.rename(id.value, name) == 0) return null
        return get(id)
    }

    override suspend fun delete(id: PlaylistId): Boolean = playlists.delete(id.value) > 0

    override suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): PlaylistTrackWrite =
        playlists.addTrack(playlistId.value, trackId.value)

    override suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): Boolean =
        playlists.removeTrack(playlistId.value, trackId.value)

    private suspend fun PlaylistEntity.toPlaylist(): Playlist = Playlist(
        id = PlaylistId(id),
        name = name,
        trackIds = playlists.tracksOf(id).map { TrackId(it.trackId) },
    )
}
