package br.com.musicflow.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import br.com.musicflow.database.dao.CatalogCacheDao
import br.com.musicflow.database.dao.CatalogPageDao
import br.com.musicflow.database.dao.CachedAlbumDao
import br.com.musicflow.database.dao.CachedArtistDao
import br.com.musicflow.database.dao.CachedTrackDao
import br.com.musicflow.database.dao.CatalogSnapshotDao
import br.com.musicflow.database.dao.FavoriteDao
import br.com.musicflow.database.dao.PlaybackHistoryDao
import br.com.musicflow.database.dao.PlaylistDao
import br.com.musicflow.database.entity.CachedAlbumEntity
import br.com.musicflow.database.entity.CachedArtistEntity
import br.com.musicflow.database.entity.CachedTrackEntity
import br.com.musicflow.database.entity.CatalogPageEntity
import br.com.musicflow.database.entity.CatalogPageTrackEntity
import br.com.musicflow.database.entity.CatalogSnapshotEntity
import br.com.musicflow.database.entity.FavoriteTrackEntity
import br.com.musicflow.database.entity.PlaybackHistoryEntity
import br.com.musicflow.database.entity.PlaylistEntity
import br.com.musicflow.database.entity.PlaylistTrackEntity

@Database(
    entities = [
        CachedTrackEntity::class,
        CachedArtistEntity::class,
        CachedAlbumEntity::class,
        CatalogSnapshotEntity::class,
        CatalogPageEntity::class,
        CatalogPageTrackEntity::class,
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        PlaybackHistoryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class MusicFlowDatabase : RoomDatabase() {
    abstract fun cachedTrackDao(): CachedTrackDao

    abstract fun cachedArtistDao(): CachedArtistDao

    abstract fun cachedAlbumDao(): CachedAlbumDao

    abstract fun catalogSnapshotDao(): CatalogSnapshotDao

    abstract fun catalogCacheDao(): CatalogCacheDao

    abstract fun catalogPageDao(): CatalogPageDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        const val NAME = "musicflow.db"

        // No destructive fallback: an upgrade without a Migration must fail
        // instead of deleting the user's library.
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
