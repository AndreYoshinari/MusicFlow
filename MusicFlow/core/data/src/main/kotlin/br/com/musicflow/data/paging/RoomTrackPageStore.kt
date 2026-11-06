package br.com.musicflow.data.paging

import androidx.paging.PagingSource
import br.com.musicflow.common.domain.Track
import br.com.musicflow.data.local.toCachedEntity
import br.com.musicflow.database.dao.CatalogPageDao
import br.com.musicflow.database.entity.CatalogPageEntity
import br.com.musicflow.database.entity.CatalogPageTrackEntity
import java.time.Instant

internal class RoomTrackPageStore(
    private val pages: CatalogPageDao,
    private val clock: () -> Instant = Instant::now,
) : TrackPageStore {
    override suspend fun header(catalogKey: String): CatalogPageHeader? =
        pages.header(catalogKey)?.let { header ->
            CatalogPageHeader(
                fetchedAt = Instant.ofEpochMilli(header.fetchedAt),
                nextOffset = header.nextOffset,
            )
        }

    override suspend fun write(request: PageWrite) {
        val existing = if (request.replace) {
            emptySet()
        } else {
            pages.trackIds(request.catalogKey).toSet()
        }
        val placed = placeTracks(request.offset, request.tracks, existing)
        val observedAt = request.observedAt.toEpochMilli()
        pages.write(
            header = CatalogPageEntity(
                catalogKey = request.catalogKey,
                fetchedAt = pageFetchedAt(
                    replace = request.replace,
                    previous = pages.header(request.catalogKey)?.fetchedAt,
                    observedAt = observedAt,
                ),
                nextOffset = request.nextOffset,
            ),
            cached = placed.map { it.track.toCachedEntity(request.observedAt) },
            links = placed.map { placedTrack ->
                CatalogPageTrackEntity(
                    catalogKey = request.catalogKey,
                    trackId = placedTrack.track.id.value,
                    position = placedTrack.position,
                )
            },
            replace = request.replace,
        )
    }

    override fun pagingSource(catalogKey: String): PagingSource<Int, Track> =
        TrackEntityPagingSource(pages.tracks(catalogKey), clock)
}
