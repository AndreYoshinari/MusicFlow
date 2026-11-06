package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.map
import br.com.musicflow.common.domain.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val favorites: FavoriteRepository,
) {
    suspend operator fun invoke(trackId: TrackId): DomainResult<Boolean> {
        return when (val current = favorites.isFavorite(trackId)) {
            is DomainResult.Failure -> current
            is DomainResult.Success -> if (current.value) {
                favorites.removeFavorite(trackId).map { false }
            } else {
                favorites.addFavorite(trackId).map { true }
            }
        }
    }
}
