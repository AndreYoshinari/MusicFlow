package br.com.musicflow.common.domain.usecase

import androidx.paging.PagingData
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import kotlinx.coroutines.flow.Flow

class ObservePopularTracksUseCase(
    private val paging: TrackPagingRepository,
) {
    operator fun invoke(): Flow<PagingData<Track>> = paging.popularTracks()
}
