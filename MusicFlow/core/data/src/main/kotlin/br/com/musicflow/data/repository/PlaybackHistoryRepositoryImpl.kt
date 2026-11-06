package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.data.local.PlaybackHistoryLocalDataSource
import java.time.Instant

internal class PlaybackHistoryRepositoryImpl(
    private val local: PlaybackHistoryLocalDataSource,
) : PlaybackHistoryRepository {
    override suspend fun getRecentlyPlayed(limit: Int): DomainResult<List<TrackId>> {
        if (limit <= 0) return DomainResult.Failure(DomainError.InvalidArgument)
        return DomainResult.Success(local.recentlyPlayed(limit))
    }

    override suspend fun addToHistory(trackId: TrackId, playedAt: Instant): DomainResult<Unit> {
        if (!local.trackExists(trackId)) return DomainResult.Failure(DomainError.NotFound)
        if (local.latestTrackId() == trackId) return DomainResult.Success(Unit)
        local.add(trackId, playedAt)
        return DomainResult.Success(Unit)
    }

    override suspend fun clearHistory(): DomainResult<Unit> {
        local.clear()
        return DomainResult.Success(Unit)
    }
}
