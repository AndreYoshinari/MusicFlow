package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_albums")
data class CachedAlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val artworkUrl: String?,
    val releaseDate: String?,
    val provider: String,
    val pageUrl: String,
    val cachedAt: Long,
)
