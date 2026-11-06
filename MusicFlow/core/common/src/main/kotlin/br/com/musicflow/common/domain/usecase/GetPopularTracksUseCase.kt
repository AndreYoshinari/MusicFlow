package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackRepository

class GetPopularTracksUseCase(
    private val tracks: TrackRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Track>> = tracks.getPopularTracks()
}
