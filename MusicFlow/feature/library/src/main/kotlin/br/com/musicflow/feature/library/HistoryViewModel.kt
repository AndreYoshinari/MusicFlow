package br.com.musicflow.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val recentTracks: GetRecentTracksUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryUiState())
    internal val state: StateFlow<HistoryUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(tracks = LibraryListState.Loading) }
            _state.update { it.copy(tracks = recentTracks().toListState { track -> track.toLibraryTrack() }) }
        }
    }
}
