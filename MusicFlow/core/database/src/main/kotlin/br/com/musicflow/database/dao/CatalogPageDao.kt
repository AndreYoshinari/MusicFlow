package br.com.musicflow.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import br.com.musicflow.database.entity.CachedTrackEntity
import br.com.musicflow.database.entity.CatalogPageEntity
import br.com.musicflow.database.entity.CatalogPageTrackEntity

@Dao
abstract class CatalogPageDao {
    @Query("SELECT * FROM catalog_pages WHERE catalogKey = :catalogKey")
    abstract suspend fun header(catalogKey: String): CatalogPageEntity?

    @Query("SELECT trackId FROM catalog_page_tracks WHERE catalogKey = :catalogKey")
    abstract suspend fun trackIds(catalogKey: String): List<Long>

    @Query(
        """
        SELECT t.* FROM cached_tracks AS t
        INNER JOIN catalog_page_tracks AS p ON p.trackId = t.id
        WHERE p.catalogKey = :catalogKey
        ORDER BY p.position
        """,
    )
    abstract fun tracks(catalogKey: String): PagingSource<Int, CachedTrackEntity>

    @Query("DELETE FROM catalog_page_tracks WHERE catalogKey = :catalogKey")
    abstract suspend fun deleteTracks(catalogKey: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTracks(tracks: List<CatalogPageTrackEntity>)

    @Upsert
    abstract suspend fun upsertHeader(header: CatalogPageEntity)

    @Upsert
    abstract suspend fun upsertCached(tracks: List<CachedTrackEntity>)

    @Transaction
    open suspend fun write(
        header: CatalogPageEntity,
        cached: List<CachedTrackEntity>,
        links: List<CatalogPageTrackEntity>,
        replace: Boolean,
    ) {
        if (replace) deleteTracks(header.catalogKey)
        if (cached.isNotEmpty()) upsertCached(cached)
        if (links.isNotEmpty()) insertTracks(links)
        upsertHeader(header)
    }
}
