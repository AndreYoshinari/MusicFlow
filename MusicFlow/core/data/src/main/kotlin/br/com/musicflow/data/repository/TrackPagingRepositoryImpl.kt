@file:OptIn(androidx.paging.ExperimentalPagingApi::class)

package br.com.musicflow.data.repository

import androidx.paging.PagingData
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.data.local.CATALOG_MAX_AGE_MILLIS
import br.com.musicflow.data.local.SEARCH_MAX_AGE_MILLIS
import br.com.musicflow.data.mapper.toTrackList
import br.com.musicflow.data.paging.TrackPageStore
import br.com.musicflow.data.paging.trackPager
import br.com.musicflow.data.remote.JamendoTrackDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

internal class TrackPagingRepositoryImpl(
    private val remote: JamendoTrackDataSource,
    private val pages: TrackPageStore,
    private val clock: () -> Instant = Instant::now,
) : TrackPagingRepository {
    override fun popularTracks(): Flow<PagingData<Track>> = trackPager(
        catalogKey = POPULAR,
        maxAgeMillis = CATALOG_MAX_AGE_MILLIS,
        fetch = { offset -> remote.getPopularTracks(offset).toTrackList() },
        store = pages,
        clock = clock,
    )

    override fun searchTracks(query: String): Flow<PagingData<Track>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return flowOf(PagingData.empty())
        return trackPager(
            catalogKey = "$SEARCH_PREFIX$trimmed",
            maxAgeMillis = SEARCH_MAX_AGE_MILLIS,
            fetch = { offset -> remote.searchTracks(trimmed, offset).toTrackList() },
            store = pages,
            clock = clock,
        )
    }

    override fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>> = trackPager(
        catalogKey = "$ARTIST_PREFIX${artistId.value}",
        maxAgeMillis = CATALOG_MAX_AGE_MILLIS,
        fetch = { offset -> remote.getTracksByArtist(artistId, offset).toTrackList() },
        store = pages,
        clock = clock,
    )

    private companion object {
        const val POPULAR = "tracks:popular"
        const val SEARCH_PREFIX = "tracks:search:"
        const val ARTIST_PREFIX = "tracks:artist:"
    }
}
