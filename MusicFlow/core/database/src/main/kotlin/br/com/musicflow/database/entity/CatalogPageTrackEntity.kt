package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "catalog_page_tracks",
    primaryKeys = ["catalogKey", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = CachedTrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("trackId"),
        Index(value = ["catalogKey", "position"], unique = true),
    ],
)
data class CatalogPageTrackEntity(
    val catalogKey: String,
    val trackId: Long,
    val position: Int,
)
