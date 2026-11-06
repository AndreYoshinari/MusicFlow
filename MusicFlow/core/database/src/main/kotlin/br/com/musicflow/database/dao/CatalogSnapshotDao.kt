package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.musicflow.database.entity.CatalogSnapshotEntity

@Dao
interface CatalogSnapshotDao {
    @Query("SELECT * FROM catalog_snapshots WHERE `key` = :key")
    suspend fun get(key: String): CatalogSnapshotEntity?

    @Upsert
    suspend fun upsert(snapshot: CatalogSnapshotEntity)
}
