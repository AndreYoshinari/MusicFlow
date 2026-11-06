package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import java.time.Instant

class AddTrackToHistoryUseCase(
    private val history: PlaybackHistoryRepository,
) {
    suspend operator fun invoke(trackId: TrackId, playedAt: Instant): DomainResult<Unit> =
        history.addToHistory(trackId, playedAt)
}
