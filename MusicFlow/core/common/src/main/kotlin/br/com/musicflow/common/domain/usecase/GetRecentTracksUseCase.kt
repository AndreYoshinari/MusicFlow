package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId

class GetRecentTracksUseCase(
    private val history: GetRecentlyPlayedUseCase,
    private val details: GetTrackDetailsUseCase,
) {
    suspend operator fun invoke(): DomainResult<List<Track>> {
        return when (val ids = history()) {
            is DomainResult.Failure -> ids
            is DomainResult.Success -> resolve(ids.value)
        }
    }

    private suspend fun resolve(ids: List<TrackId>): DomainResult<List<Track>> {
        val tracks = mutableListOf<Track>()
        for (id in ids) {
            when (val track = details(id)) {
                is DomainResult.Success -> tracks += track.value
                is DomainResult.Failure -> if (track.error != DomainError.NotFound) return track
            }
        }
        return DomainResult.Success(tracks)
    }
}
