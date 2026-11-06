package br.com.musicflow.feature.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.usecase.AddTrackToHistoryUseCase
import br.com.musicflow.common.domain.usecase.AddTrackToPlaylistUseCase
import br.com.musicflow.common.domain.usecase.DeletePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.RemoveTrackFromPlaylistUseCase
import br.com.musicflow.common.domain.usecase.RenamePlaylistUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class PlaylistDetailsViewModel(
    private val playlistId: Long,
    private val playlist: GetPlaylistUseCase,
    private val renamePlaylist: RenamePlaylistUseCase,
    private val deletePlaylist: DeletePlaylistUseCase,
    private val addTrack: AddTrackToPlaylistUseCase,
    private val removeTrack: RemoveTrackFromPlaylistUseCase,
    favorites: GetFavoritesUseCase,
    private val details: GetTrackDetailsUseCase,
    private val history: AddTrackToHistoryUseCase,
    private val now: () -> Instant = Instant::now,
) : ViewModel() {
    private val _state = MutableStateFlow(PlaylistDetailsUiState())
    internal val state: StateFlow<PlaylistDetailsUiState> = _state.asStateFlow()
    private val _playback = MutableStateFlow(PlaybackRequest())
    internal val playback: StateFlow<PlaybackRequest> = _playback.asStateFlow()

    private var resolved = listOf<Track>()
    private var favoriteTracks = listOf<Track>()
    private var playlistTrackIds = emptySet<Long>()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            favorites().collect { tracks ->
                favoriteTracks = tracks
                publishCandidates()
            }
        }
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(content = PlaylistContentState.Loading) }
            when (val result = playlist(PlaylistId(playlistId))) {
                is DomainResult.Failure -> _state.update { it.copy(content = PlaylistContentState.Failed(result.error)) }
                is DomainResult.Success -> show(result.value)
            }
        }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            when (val result = renamePlaylist(PlaylistId(playlistId), name)) {
                is DomainResult.Failure -> _state.update { it.copy(notice = PlaylistNotice.Failed(result.error)) }
                is DomainResult.Success -> {
                    show(result.value)
                    bumpEdit()
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val result = deletePlaylist(PlaylistId(playlistId))) {
                is DomainResult.Failure -> _state.update { it.copy(notice = PlaylistNotice.Failed(result.error)) }
                is DomainResult.Success -> _state.update { it.copy(deleted = true, notice = null) }
            }
        }
    }

    fun add(trackId: Long) {
        viewModelScope.launch {
            when (val result = addTrack(PlaylistId(playlistId), TrackId(trackId))) {
                is DomainResult.Failure -> _state.update { it.copy(notice = PlaylistNotice.Failed(result.error)) }
                is DomainResult.Success -> {
                    show(result.value)
                    bumpEdit()
                }
            }
        }
    }

    fun remove(trackId: Long) {
        viewModelScope.launch {
            when (val result = removeTrack(PlaylistId(playlistId), TrackId(trackId))) {
                is DomainResult.Failure -> _state.update { it.copy(notice = PlaylistNotice.Failed(result.error)) }
                is DomainResult.Success -> {
                    show(result.value)
                    bumpEdit()
                }
            }
        }
    }

    fun play() {
        val tracks = resolved
        if (tracks.isEmpty()) {
            _state.update { it.copy(notice = PlaylistNotice.EmptyPlayback) }
            return
        }
        viewModelScope.launch {
            history(tracks.first().id, now())
            _playback.update { PlaybackRequest(revision = it.revision + 1, tracks = tracks) }
        }
    }

    private suspend fun show(playlist: Playlist) {
        val known = resolved.associateBy { it.id }
        val tracks = mutableListOf<Track>()
        var missing: DomainError? = null
        for (id in playlist.trackIds) {
            val existing = known[id]
            if (existing != null) {
                tracks += existing
                continue
            }
            when (val loaded = details(id)) {
                is DomainResult.Success -> tracks += loaded.value
                is DomainResult.Failure -> if (missing == null) missing = loaded.error
            }
        }
        resolved = tracks
        playlistTrackIds = playlist.trackIds.map { it.value }.toSet()
        publishCandidates()
        _state.update { current ->
            current.copy(
                content = PlaylistContentState.Ready(
                    PlaylistContent(
                        id = playlist.id.value,
                        name = playlist.name,
                        tracks = tracks.map { it.toPlaylistTrack() },
                    ),
                ),
                notice = missing?.let { PlaylistNotice.Failed(it) },
            )
        }
    }

    private fun publishCandidates() {
        val candidates = favoriteTracks
            .filter { it.id.value !in playlistTrackIds }
            .map { it.toPlaylistTrack() }
        _state.update { it.copy(candidates = candidates) }
    }

    private fun bumpEdit() {
        _state.update { it.copy(editRevision = it.editRevision + 1) }
    }
}
