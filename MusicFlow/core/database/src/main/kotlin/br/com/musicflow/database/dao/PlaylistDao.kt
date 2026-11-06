package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.musicflow.database.entity.PlaylistEntity
import br.com.musicflow.database.entity.PlaylistTrackEntity

enum class PlaylistTrackWrite {
    Saved,
    MissingPlaylist,
    MissingTrack,
    Duplicate,
}

@Dao
abstract class PlaylistDao {
    @Insert
    abstract suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists ORDER BY createdAt ASC, id ASC")
    abstract suspend fun getPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract suspend fun getPlaylist(id: Long): PlaylistEntity?

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    abstract suspend fun rename(id: Long, name: String): Int

    @Query("DELETE FROM playlists WHERE id = :id")
    abstract suspend fun delete(id: Long): Int

    @Query(
        """
        SELECT * FROM playlist_tracks
        WHERE playlistId = :playlistId
        ORDER BY position ASC
        """,
    )
    abstract suspend fun tracksOf(playlistId: Long): List<PlaylistTrackEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM cached_tracks WHERE id = :trackId)")
    abstract suspend fun trackExists(trackId: Long): Boolean

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM playlist_tracks
            WHERE playlistId = :playlistId AND trackId = :trackId
        )
        """,
    )
    abstract suspend fun contains(playlistId: Long, trackId: Long): Boolean

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) + 1
        FROM playlist_tracks
        WHERE playlistId = :playlistId
        """,
    )
    abstract suspend fun nextPosition(playlistId: Long): Int

    @Insert
    abstract suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    abstract suspend fun deleteTrack(playlistId: Long, trackId: Long)

    @Query(
        """
        UPDATE playlist_tracks
        SET position = :position
        WHERE playlistId = :playlistId AND trackId = :trackId
        """,
    )
    abstract suspend fun updatePosition(playlistId: Long, trackId: Long, position: Int)

    @Transaction
    open suspend fun addTrack(playlistId: Long, trackId: Long): PlaylistTrackWrite {
        if (getPlaylist(playlistId) == null) return PlaylistTrackWrite.MissingPlaylist
        if (!trackExists(trackId)) return PlaylistTrackWrite.MissingTrack
        if (contains(playlistId, trackId)) return PlaylistTrackWrite.Duplicate
        insertTrack(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = trackId,
                position = nextPosition(playlistId),
            ),
        )
        return PlaylistTrackWrite.Saved
    }

    @Transaction
    open suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean {
        if (getPlaylist(playlistId) == null) return false
        val current = tracksOf(playlistId)
        val remaining = current.filterNot { it.trackId == trackId }
        if (remaining.size == current.size) return true
        deleteTrack(playlistId, trackId)
        remaining.forEachIndexed { index, track ->
            if (track.position != index) {
                updatePosition(playlistId, track.trackId, index)
            }
        }
        return true
    }
}
