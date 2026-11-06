package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackRepository

class SearchTracksUseCase(
    private val tracks: TrackRepository,
) {
    suspend operator fun invoke(query: String): DomainResult<List<Track>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return DomainResult.Success(emptyList())
        return tracks.searchTracks(trimmed)
    }
}
