package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackRepository

class GetTracksByArtistUseCase(
    private val tracks: TrackRepository,
) {
    suspend operator fun invoke(artistId: ArtistId): DomainResult<List<Track>> =
        tracks.getTracksByArtist(artistId)
}
