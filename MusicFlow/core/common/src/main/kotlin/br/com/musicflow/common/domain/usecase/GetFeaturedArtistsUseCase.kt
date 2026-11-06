package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.repository.ArtistRepository

class GetFeaturedArtistsUseCase(
    private val artists: ArtistRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Artist>> = artists.getFeaturedArtists()
}
