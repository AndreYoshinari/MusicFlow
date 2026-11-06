package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.musicflow.database.entity.CachedArtistEntity

@Dao
interface CachedArtistDao {
    @Upsert
    suspend fun upsertAll(artists: List<CachedArtistEntity>)

    @Query("SELECT * FROM cached_artists WHERE id = :id")
    suspend fun getById(id: Long): CachedArtistEntity?

    @Query("SELECT * FROM cached_artists WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<CachedArtistEntity>
}
