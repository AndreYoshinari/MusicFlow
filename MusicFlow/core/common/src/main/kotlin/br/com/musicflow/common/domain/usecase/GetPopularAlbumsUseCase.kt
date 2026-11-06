package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.repository.ArtistRepository

class GetPopularAlbumsUseCase(
    private val artists: ArtistRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Album>> = artists.getPopularAlbums()
}
