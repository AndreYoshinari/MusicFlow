package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.musicflow.database.entity.CachedTrackEntity

@Dao
interface CachedTrackDao {
    @Upsert
    suspend fun upsertAll(tracks: List<CachedTrackEntity>)

    @Query("SELECT * FROM cached_tracks WHERE id = :id")
    suspend fun getById(id: Long): CachedTrackEntity?

    @Query("SELECT * FROM cached_tracks WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<CachedTrackEntity>

    @Query("SELECT * FROM cached_tracks ORDER BY title COLLATE NOCASE, id")
    suspend fun getAll(): List<CachedTrackEntity>

    @Query(
        """
        SELECT * FROM cached_tracks
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
           OR artistName LIKE '%' || :query || '%' COLLATE NOCASE
           OR albumTitle LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY title COLLATE NOCASE, id
        """,
    )
    suspend fun search(query: String): List<CachedTrackEntity>
}
