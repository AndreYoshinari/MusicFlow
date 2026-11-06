package br.com.musicflow.data.paging

import androidx.paging.PagingSource
import br.com.musicflow.common.domain.Track
import java.time.Instant

internal data class CatalogPageHeader(
    val fetchedAt: Instant,
    val nextOffset: Int?,
)

internal data class PageWrite(
    val catalogKey: String,
    val offset: Int,
    val tracks: List<Track>,
    val replace: Boolean,
    val nextOffset: Int?,
    val observedAt: Instant,
)

internal data class PlacedTrack(
    val track: Track,
    val position: Int,
)

internal fun placeTracks(
    offset: Int,
    tracks: List<Track>,
    alreadyStored: Set<Long>,
): List<PlacedTrack> {
    val seen = alreadyStored.toMutableSet()
    return buildList {
        tracks.forEachIndexed { index, track ->
            if (seen.add(track.id.value)) {
                add(PlacedTrack(track, offset + index))
            }
        }
    }
}

internal fun pageFetchedAt(replace: Boolean, previous: Long?, observedAt: Long): Long =
    if (replace || previous == null) observedAt else previous

internal interface TrackPageStore {
    suspend fun header(catalogKey: String): CatalogPageHeader?

    suspend fun write(request: PageWrite)

    fun pagingSource(catalogKey: String): PagingSource<Int, Track>
}
