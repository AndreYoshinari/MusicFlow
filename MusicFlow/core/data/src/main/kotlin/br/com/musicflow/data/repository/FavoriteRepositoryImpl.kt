package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.data.local.FavoriteLocalDataSource
import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal class FavoriteRepositoryImpl(
    private val local: FavoriteLocalDataSource,
    private val clock: () -> Instant = Instant::now,
) : FavoriteRepository {
    override suspend fun getFavorites(): DomainResult<List<Track>> =
        DomainResult.Success(local.getAll(clock()))

    override suspend fun isFavorite(trackId: TrackId): DomainResult<Boolean> =
        DomainResult.Success(local.isFavorite(trackId))

    override fun observeFavorites(): Flow<List<Track>> = local.observe(clock)

    override suspend fun addFavorite(trackId: TrackId): DomainResult<Unit> {
        if (!local.containsTrack(trackId)) return DomainResult.Failure(DomainError.NotFound)
        local.add(trackId, clock())
        return DomainResult.Success(Unit)
    }

    override suspend fun removeFavorite(trackId: TrackId): DomainResult<Unit> {
        local.remove(trackId)
        return DomainResult.Success(Unit)
    }
}
