package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.TrackRepository

class GetTrackDetailsUseCase(
    private val tracks: TrackRepository,
) {
    suspend operator fun invoke(id: TrackId): DomainResult<Track> = tracks.getTrackById(id)
}
