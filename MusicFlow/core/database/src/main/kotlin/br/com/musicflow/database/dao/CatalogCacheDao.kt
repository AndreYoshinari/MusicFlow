package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert
import br.com.musicflow.database.entity.CachedAlbumEntity
import br.com.musicflow.database.entity.CachedArtistEntity
import br.com.musicflow.database.entity.CachedTrackEntity
import br.com.musicflow.database.entity.CatalogSnapshotEntity

@Dao
abstract class CatalogCacheDao {
    @Upsert
    abstract suspend fun upsertTracks(tracks: List<CachedTrackEntity>)

    @Upsert
    abstract suspend fun upsertArtists(artists: List<CachedArtistEntity>)

    @Upsert
    abstract suspend fun upsertAlbums(albums: List<CachedAlbumEntity>)

    @Upsert
    abstract suspend fun upsertSnapshot(snapshot: CatalogSnapshotEntity)

    @Transaction
    open suspend fun saveTracks(tracks: List<CachedTrackEntity>, snapshot: CatalogSnapshotEntity) {
        if (tracks.isNotEmpty()) upsertTracks(tracks)
        upsertSnapshot(snapshot)
    }

    @Transaction
    open suspend fun saveArtists(artists: List<CachedArtistEntity>, snapshot: CatalogSnapshotEntity) {
        if (artists.isNotEmpty()) upsertArtists(artists)
        upsertSnapshot(snapshot)
    }

    @Transaction
    open suspend fun saveAlbums(albums: List<CachedAlbumEntity>, snapshot: CatalogSnapshotEntity) {
        if (albums.isNotEmpty()) upsertAlbums(albums)
        upsertSnapshot(snapshot)
    }
}
