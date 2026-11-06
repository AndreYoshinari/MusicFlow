@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.com.musicflow.common.domain.usecase

import androidx.paging.PagingData
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

const val TRACK_SEARCH_DEBOUNCE_MILLIS = 300L

class ObserveTrackSearchUseCase(
    private val paging: TrackPagingRepository,
    private val debounceMillis: Long = TRACK_SEARCH_DEBOUNCE_MILLIS,
) {
    operator fun invoke(queries: Flow<String>): Flow<PagingData<Track>> =
        queries
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isEmpty()) {
                    flowOf(PagingData.empty())
                } else {
                    flow {
                        delay(debounceMillis)
                        emitAll(paging.searchTracks(query))
                    }
                }
            }
}
