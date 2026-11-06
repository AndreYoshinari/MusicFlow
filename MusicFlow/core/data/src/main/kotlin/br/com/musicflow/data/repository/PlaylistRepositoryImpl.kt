package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.data.local.PlaylistLocalDataSource
import br.com.musicflow.database.dao.PlaylistTrackWrite
import java.time.Instant

internal class PlaylistRepositoryImpl(
    private val local: PlaylistLocalDataSource,
    private val clock: () -> Instant = Instant::now,
) : PlaylistRepository {
    override suspend fun getPlaylists(): DomainResult<List<Playlist>> =
        DomainResult.Success(local.getAll())

    override suspend fun getPlaylistById(id: PlaylistId): DomainResult<Playlist> =
        local.get(id)?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)

    override suspend fun createPlaylist(name: String): DomainResult<Playlist> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return DomainResult.Failure(DomainError.InvalidName)
        return DomainResult.Success(local.create(trimmed, clock()))
    }

    override suspend fun renamePlaylist(id: PlaylistId, name: String): DomainResult<Playlist> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return DomainResult.Failure(DomainError.InvalidName)
        return local.rename(id, trimmed)?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)
    }

    override suspend fun deletePlaylist(id: PlaylistId): DomainResult<Unit> {
        if (!local.delete(id)) return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(Unit)
    }

    override suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> {
        return when (local.addTrack(playlistId, trackId)) {
            PlaylistTrackWrite.MissingPlaylist,
            PlaylistTrackWrite.MissingTrack,
            -> DomainResult.Failure(DomainError.NotFound)
            PlaylistTrackWrite.Duplicate -> DomainResult.Failure(DomainError.DuplicateTrack)
            PlaylistTrackWrite.Saved -> local.get(playlistId)?.let { DomainResult.Success(it) }
                ?: DomainResult.Failure(DomainError.NotFound)
        }
    }

    override suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> {
        if (!local.removeTrack(playlistId, trackId)) return DomainResult.Failure(DomainError.NotFound)
        return local.get(playlistId)?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)
    }
}
