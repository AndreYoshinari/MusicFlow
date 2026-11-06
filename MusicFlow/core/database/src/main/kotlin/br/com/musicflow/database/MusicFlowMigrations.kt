package br.com.musicflow.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_artists` (`id` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, `imageUrl` TEXT, `provider` TEXT NOT NULL, " +
                "`pageUrl` TEXT NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_albums` (`id` INTEGER NOT NULL, " +
                "`title` TEXT NOT NULL, `artistId` INTEGER NOT NULL, `artistName` TEXT NOT NULL, " +
                "`artworkUrl` TEXT, `releaseDate` TEXT, `provider` TEXT NOT NULL, " +
                "`pageUrl` TEXT NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `catalog_snapshots` (`key` TEXT NOT NULL, " +
                "`ids` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`key`))",
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `catalog_pages` (`catalogKey` TEXT NOT NULL, " +
                "`fetchedAt` INTEGER NOT NULL, `nextOffset` INTEGER, PRIMARY KEY(`catalogKey`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `catalog_page_tracks` (`catalogKey` TEXT NOT NULL, " +
                "`trackId` INTEGER NOT NULL, `position` INTEGER NOT NULL, " +
                "PRIMARY KEY(`catalogKey`, `trackId`), " +
                "FOREIGN KEY(`trackId`) REFERENCES `cached_tracks`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_catalog_page_tracks_trackId` " +
                "ON `catalog_page_tracks` (`trackId`)",
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_catalog_page_tracks_catalogKey_position` " +
                "ON `catalog_page_tracks` (`catalogKey`, `position`)",
        )
    }
}
