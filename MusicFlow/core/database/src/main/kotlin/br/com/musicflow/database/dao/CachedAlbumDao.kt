package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.musicflow.database.entity.CachedAlbumEntity

@Dao
interface CachedAlbumDao {
    @Upsert
    suspend fun upsertAll(albums: List<CachedAlbumEntity>)

    @Query("SELECT * FROM cached_albums WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<CachedAlbumEntity>
}
