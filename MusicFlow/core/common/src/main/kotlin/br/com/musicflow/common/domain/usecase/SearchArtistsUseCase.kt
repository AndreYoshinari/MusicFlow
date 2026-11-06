package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.repository.ArtistRepository

class SearchArtistsUseCase(
    private val artists: ArtistRepository,
) {
    suspend operator fun invoke(query: String): DomainResult<List<Artist>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return DomainResult.Success(emptyList())
        return artists.searchArtists(trimmed)
    }
}
