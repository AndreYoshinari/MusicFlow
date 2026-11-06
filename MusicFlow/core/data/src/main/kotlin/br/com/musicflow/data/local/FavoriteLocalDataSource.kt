package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.database.dao.CachedTrackDao
import br.com.musicflow.database.dao.FavoriteDao
import br.com.musicflow.database.entity.FavoriteTrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

internal interface FavoriteLocalDataSource {
    fun observe(now: () -> Instant): Flow<List<Track>>

    suspend fun getAll(now: Instant): List<Track>

    suspend fun isFavorite(trackId: TrackId): Boolean

    suspend fun containsTrack(trackId: TrackId): Boolean

    suspend fun add(trackId: TrackId, addedAt: Instant)

    suspend fun remove(trackId: TrackId)
}

internal class RoomFavoriteLocalDataSource(
    private val favorites: FavoriteDao,
    private val tracks: CachedTrackDao,
) : FavoriteLocalDataSource {
    override fun observe(now: () -> Instant): Flow<List<Track>> =
        favorites.observe().map { entities -> entities.map { it.toTrack(now()) } }

    override suspend fun getAll(now: Instant): List<Track> =
        favorites.getAll().map { it.toTrack(now) }

    override suspend fun isFavorite(trackId: TrackId): Boolean = favorites.isFavorite(trackId.value)

    override suspend fun containsTrack(trackId: TrackId): Boolean = tracks.getById(trackId.value) != null

    override suspend fun add(trackId: TrackId, addedAt: Instant) {
        favorites.insert(FavoriteTrackEntity(trackId = trackId.value, addedAt = addedAt.toEpochMilli()))
    }

    override suspend fun remove(trackId: TrackId) {
        favorites.delete(trackId.value)
    }
}
