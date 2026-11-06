package br.com.musicflow.common.domain.repository

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import java.time.Instant

interface PlaybackHistoryRepository {
    suspend fun getRecentlyPlayed(limit: Int): DomainResult<List<TrackId>>

    suspend fun addToHistory(trackId: TrackId, playedAt: Instant): DomainResult<Unit>

    suspend fun clearHistory(): DomainResult<Unit>
}
