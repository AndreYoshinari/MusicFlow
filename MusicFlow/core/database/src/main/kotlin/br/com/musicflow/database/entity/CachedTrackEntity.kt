package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_tracks")
data class CachedTrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val artworkUrl: String?,
    val durationMs: Long,
    val streamUrl: String?,
    val streamObservedAt: Long?,
    val licenseUrl: String,
    val provider: String,
    val pageUrl: String,
    val releaseDate: String?,
    val cachedAt: Long,
)
