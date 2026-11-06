package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.musicflow.database.entity.CachedTrackEntity
import br.com.musicflow.database.entity.FavoriteTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query(
        """
        SELECT cached_tracks.* FROM cached_tracks
        INNER JOIN favorite_tracks ON favorite_tracks.trackId = cached_tracks.id
        ORDER BY favorite_tracks.addedAt DESC, favorite_tracks.trackId ASC
        """,
    )
    fun observe(): Flow<List<CachedTrackEntity>>

    @Query(
        """
        SELECT cached_tracks.* FROM cached_tracks
        INNER JOIN favorite_tracks ON favorite_tracks.trackId = cached_tracks.id
        ORDER BY favorite_tracks.addedAt DESC, favorite_tracks.trackId ASC
        """,
    )
    suspend fun getAll(): List<CachedTrackEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean

    @Query("SELECT COUNT(*) FROM favorite_tracks")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteTrackEntity): Long

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: Long)
}
