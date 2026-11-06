package br.com.musicflow.common.domain.repository

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun getFavorites(): DomainResult<List<Track>>

    suspend fun isFavorite(trackId: TrackId): DomainResult<Boolean>

    fun observeFavorites(): Flow<List<Track>>

    suspend fun addFavorite(trackId: TrackId): DomainResult<Unit>

    suspend fun removeFavorite(trackId: TrackId): DomainResult<Unit>
}
