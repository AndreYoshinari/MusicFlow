package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_pages")
data class CatalogPageEntity(
    @PrimaryKey val catalogKey: String,
    val fetchedAt: Long,
    val nextOffset: Int?,
)
