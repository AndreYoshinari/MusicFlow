package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository

class GetRecentlyPlayedUseCase(
    private val history: PlaybackHistoryRepository,
) {
    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): DomainResult<List<TrackId>> {
        if (limit <= 0) return DomainResult.Failure(DomainError.InvalidArgument)
        return history.getRecentlyPlayed(limit)
    }

    private companion object {
        const val DEFAULT_LIMIT = 20
    }
}
