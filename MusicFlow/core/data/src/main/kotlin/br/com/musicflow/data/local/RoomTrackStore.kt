package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Track
import br.com.musicflow.database.dao.CachedTrackDao
import br.com.musicflow.database.dao.CatalogCacheDao
import br.com.musicflow.database.dao.CatalogSnapshotDao
import br.com.musicflow.database.entity.CatalogSnapshotEntity
import java.time.Instant

internal class RoomTrackStore(
    private val tracks: CachedTrackDao,
    private val snapshots: CatalogSnapshotDao,
    private val cache: CatalogCacheDao,
) : TrackStore {
    override suspend fun read(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Track>> {
        val snapshot = snapshots.get(key) ?: return CatalogRead.Missing
        val ordered = loadOrdered(decodeIds(snapshot.ids), now) ?: return CatalogRead.Missing
        return classify(snapshot.fetchedAt, now, maxAgeMillis, ordered)
    }

    override suspend fun readTrack(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Track> {
        val entity = tracks.getById(id) ?: return CatalogRead.Missing
        return classify(entity.cachedAt, now, maxAgeMillis, entity.toTrack(now))
    }

    override suspend fun freshStreamUrl(id: Long, now: Instant): String? =
        tracks.getById(id)?.toTrack(now)?.streamUrl

    override suspend fun save(key: String, tracks: List<Track>, observedAt: Instant) {
        cache.saveTracks(
            tracks.map { it.toCachedEntity(observedAt) },
            CatalogSnapshotEntity(
                key = key,
                ids = tracks.joinToString(",") { it.id.value.toString() },
                fetchedAt = observedAt.toEpochMilli(),
            ),
        )
    }

    override suspend fun saveTrack(track: Track, observedAt: Instant) {
        tracks.upsertAll(listOf(track.toCachedEntity(observedAt)))
    }

    private suspend fun loadOrdered(ids: List<Long>, now: Instant): List<Track>? {
        if (ids.isEmpty()) return emptyList()
        val stored = tracks.getByIds(ids).associateBy { it.id }
        if (!stored.keys.containsAll(ids)) return null
        return ids.map { stored.getValue(it).toTrack(now) }
    }
}

private fun <T> classify(observedAt: Long, now: Instant, maxAgeMillis: Long, value: T): CatalogRead<T> =
    if (isFresh(observedAt, now.toEpochMilli(), maxAgeMillis)) {
        CatalogRead.Fresh(value)
    } else {
        CatalogRead.Stale(value)
    }

private fun decodeIds(ids: String): List<Long> =
    if (ids.isBlank()) emptyList() else ids.split(",").map { it.toLong() }
