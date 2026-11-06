package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_snapshots")
data class CatalogSnapshotEntity(
    @PrimaryKey val key: String,
    val ids: String,
    val fetchedAt: Long,
)
