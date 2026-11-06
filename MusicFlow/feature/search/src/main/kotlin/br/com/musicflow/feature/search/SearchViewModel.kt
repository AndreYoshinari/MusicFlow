@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.com.musicflow.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.usecase.ObserveTrackSearchUseCase
import br.com.musicflow.common.domain.usecase.SearchArtistsUseCase
import br.com.musicflow.common.domain.usecase.TRACK_SEARCH_DEBOUNCE_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    observeTrackSearch: ObserveTrackSearchUseCase,
    private val searchArtists: SearchArtistsUseCase,
) : ViewModel() {
    private val queries = MutableStateFlow("")
    private val artistAttempts = MutableStateFlow(0)
    private var settledQuery: String? = null

    private val _state = MutableStateFlow(SearchUiState())
    internal val state: StateFlow<SearchUiState> = _state.asStateFlow()

    val tracks: Flow<PagingData<Track>> = observeTrackSearch(queries).cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(queries, artistAttempts) { query, attempt -> query.trim() to attempt }
                .distinctUntilChanged()
                .flatMapLatest { (query, _) -> artistMatches(query) }
                .collect { matches -> _state.update { it.copy(artists = matches) } }
        }
    }

    fun onQueryChange(value: String) {
        queries.value = value
        _state.update { it.copy(query = value) }
    }

    fun clear() {
        onQueryChange("")
    }

    fun retryArtists() {
        artistAttempts.update { it + 1 }
    }

    private fun artistMatches(query: String): Flow<ArtistMatches> {
        if (query.isEmpty()) {
            settledQuery = null
            return flowOf(ArtistMatches.Hidden)
        }
        return flow {
            if (query != settledQuery) delay(TRACK_SEARCH_DEBOUNCE_MILLIS)
            settledQuery = query
            emit(ArtistMatches.Loading)
            emit(searchArtists(query).toMatches())
        }
    }
}
