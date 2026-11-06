package br.com.musicflow.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NowPlayingFavoriteViewModel(
    observeFavorites: GetFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    val favoriteIds: StateFlow<Set<Long>> = observeFavorites()
        .map { tracks -> tracks.map { it.id.value }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _actionError = MutableStateFlow<DomainError?>(null)
    val actionError: StateFlow<DomainError?> = _actionError.asStateFlow()

    fun toggle(trackId: Long) {
        viewModelScope.launch {
            when (val result = toggleFavorite(TrackId(trackId))) {
                is DomainResult.Failure -> _actionError.value = result.error
                is DomainResult.Success -> _actionError.value = null
            }
        }
    }
}
