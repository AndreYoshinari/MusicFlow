package br.com.musicflow.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.usecase.CreatePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlists: GetPlaylistsUseCase,
    private val createPlaylist: CreatePlaylistUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PlaylistsUiState())
    internal val state: StateFlow<PlaylistsUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(playlists = LibraryListState.Loading) }
            _state.update { it.copy(playlists = playlists().toListState { playlist -> playlist.toSummary() }) }
        }
    }

    fun create(name: String) {
        viewModelScope.launch {
            when (val result = createPlaylist(name)) {
                is DomainResult.Failure -> _state.update { it.copy(actionError = result.error) }
                is DomainResult.Success -> _state.update { current ->
                    val existing = (current.playlists as? LibraryListState.Ready)?.items.orEmpty()
                    current.copy(
                        playlists = LibraryListState.Ready(existing + result.value.toSummary()),
                        actionError = null,
                        savedRevision = current.savedRevision + 1,
                    )
                }
            }
        }
    }
}
