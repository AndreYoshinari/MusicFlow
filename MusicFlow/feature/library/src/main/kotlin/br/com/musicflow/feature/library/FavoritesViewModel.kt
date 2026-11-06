package br.com.musicflow.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val observeFavorites: GetFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FavoritesUiState())
    internal val state: StateFlow<FavoritesUiState> = _state.asStateFlow()
    private var observeJob: Job? = null

    init {
        observe()
    }

    fun toggle(trackId: Long) {
        viewModelScope.launch {
            when (val result = toggleFavorite(TrackId(trackId))) {
                is DomainResult.Failure -> _state.update { it.copy(actionError = result.error) }
                is DomainResult.Success -> _state.update { it.copy(actionError = null) }
            }
        }
    }

    fun retry() {
        _state.update { it.copy(tracks = LibraryListState.Loading, actionError = null) }
        observe()
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeFavorites()
                .map { tracks -> tracks.toSection() }
                .catch { emit(LibraryListState.Failed(DomainError.Unavailable)) }
                .collect { section ->
                    _state.update { it.copy(tracks = section) }
                }
        }
    }

    private fun List<Track>.toSection(): LibraryListState<LibraryTrackModel> = if (isEmpty()) {
        LibraryListState.Empty
    } else {
        LibraryListState.Ready(map { it.toLibraryTrack() })
    }
}
