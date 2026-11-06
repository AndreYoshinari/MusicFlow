package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_artists")
data class CachedArtistEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val imageUrl: String?,
    val provider: String,
    val pageUrl: String,
    val cachedAt: Long,
)
