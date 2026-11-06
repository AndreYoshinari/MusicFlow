package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.TrackRepository

class GetTrackStreamUrlUseCase(
    private val tracks: TrackRepository,
) {
    suspend operator fun invoke(id: TrackId): DomainResult<String> = tracks.getTrackStreamUrl(id)
}
