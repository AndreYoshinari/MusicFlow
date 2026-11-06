package br.com.musicflow.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_tracks",
    foreignKeys = [
        ForeignKey(
            entity = CachedTrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("addedAt")],
)
data class FavoriteTrackEntity(
    @PrimaryKey val trackId: Long,
    val addedAt: Long,
)
