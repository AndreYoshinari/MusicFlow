package br.com.musicflow.common.domain.usecase

import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class GetFavoritesUseCase(
    private val favorites: FavoriteRepository,
) {
    operator fun invoke(): Flow<List<Track>> = favorites.observeFavorites()
}
