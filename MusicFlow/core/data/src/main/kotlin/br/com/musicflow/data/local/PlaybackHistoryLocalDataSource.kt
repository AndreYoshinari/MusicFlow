package br.com.musicflow.data.local

import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.database.dao.CachedTrackDao
import br.com.musicflow.database.dao.PlaybackHistoryDao
import br.com.musicflow.database.entity.PlaybackHistoryEntity
import java.time.Instant

internal interface PlaybackHistoryLocalDataSource {
    suspend fun trackExists(trackId: TrackId): Boolean

    suspend fun latestTrackId(): TrackId?

    suspend fun add(trackId: TrackId, playedAt: Instant)

    suspend fun recentlyPlayed(limit: Int): List<TrackId>

    suspend fun clear()
}

internal class RoomPlaybackHistoryLocalDataSource(
    private val history: PlaybackHistoryDao,
    private val tracks: CachedTrackDao,
) : PlaybackHistoryLocalDataSource {
    override suspend fun trackExists(trackId: TrackId): Boolean = tracks.getById(trackId.value) != null

    override suspend fun latestTrackId(): TrackId? = history.latest()?.let { TrackId(it.trackId) }

    override suspend fun add(trackId: TrackId, playedAt: Instant) {
        history.insert(
            PlaybackHistoryEntity(
                trackId = trackId.value,
                playedAt = playedAt.toEpochMilli(),
            ),
        )
    }

    override suspend fun recentlyPlayed(limit: Int): List<TrackId> =
        history.recentlyPlayed(limit).map(::TrackId)

    override suspend fun clear() {
        history.clear()
    }
}
