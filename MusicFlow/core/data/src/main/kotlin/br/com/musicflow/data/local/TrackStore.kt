package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Track
import java.time.Instant

internal interface TrackStore {
    suspend fun read(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Track>>

    suspend fun readTrack(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Track>

    suspend fun freshStreamUrl(id: Long, now: Instant): String?

    suspend fun save(key: String, tracks: List<Track>, observedAt: Instant)

    suspend fun saveTrack(track: Track, observedAt: Instant)
}
