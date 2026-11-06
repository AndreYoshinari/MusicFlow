package br.com.musicflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.map
import br.com.musicflow.common.domain.usecase.GetFeaturedArtistsUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularAlbumsUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.ObservePopularTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

class HomeViewModel(
    observePopularTracks: ObservePopularTracksUseCase,
    private val featuredArtists: GetFeaturedArtistsUseCase,
    private val popularAlbums: GetPopularAlbumsUseCase,
    private val recentTracks: GetRecentTracksUseCase,
    private val playlists: GetPlaylistsUseCase,
    clock: () -> LocalTime = LocalTime::now,
) : ViewModel() {
    val popularTracks: Flow<PagingData<Track>> = observePopularTracks().cachedIn(viewModelScope)

    private val _state = MutableStateFlow(HomeUiState(greeting = greetingPeriod(clock())))
    internal val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var artistsJob: Job? = null
    private var albumsJob: Job? = null
    private var recentJob: Job? = null
    private var playlistsJob: Job? = null

    init {
        retryArtists()
        retryAlbums()
        retryRecent()
        retryPlaylists()
    }

    fun retryArtists() {
        artistsJob = replace(artistsJob) {
            _state.update { it.copy(artists = SectionState.Loading) }
            val section = featuredArtists().map { artists -> artists.map { it.toCard() } }.toSection()
            _state.update { it.copy(artists = section) }
        }
    }

    fun retryAlbums() {
        albumsJob = replace(albumsJob) {
            _state.update { it.copy(albums = SectionState.Loading) }
            val section = popularAlbums().map { albums -> albums.map { it.toCard() } }.toSection()
            _state.update { it.copy(albums = section) }
        }
    }

    fun retryRecent() {
        recentJob = replace(recentJob) {
            _state.update { it.copy(recent = SectionState.Loading) }
            val section = recentTracks().map { tracks -> tracks.map { it.toCard() } }.toSection()
            _state.update { it.copy(recent = section) }
        }
    }

    fun retryPlaylists() {
        playlistsJob = replace(playlistsJob) {
            _state.update { it.copy(playlists = SectionState.Loading) }
            val section = playlists().map { lists -> lists.map { it.toCard() } }.toSection()
            _state.update { it.copy(playlists = section) }
        }
    }

    private fun replace(current: Job?, block: suspend () -> Unit): Job {
        current?.cancel()
        return viewModelScope.launch { block() }
    }
}
