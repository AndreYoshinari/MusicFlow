package br.com.musicflow.player

import br.com.musicflow.common.domain.TrackId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoadedTrack(
    val id: TrackId,
    val title: String,
    val artistName: String,
    val artworkUrl: String? = null,
)

data class PlaybackSnapshot(
    val track: LoadedTrack,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
)

class PlaybackSession {
    private val state = MutableStateFlow<PlaybackSnapshot?>(null)
    val snapshot: StateFlow<PlaybackSnapshot?> = state.asStateFlow()

    // The same track keeps its play state. A repeated load must not restart playback.
    fun load(track: LoadedTrack) {
        state.update { current ->
            if (current != null && current.track.id == track.id) {
                current.copy(track = track)
            } else {
                PlaybackSnapshot(track = track)
            }
        }
    }

    fun play(track: LoadedTrack) {
        state.update { current ->
            if (current != null && current.track.id == track.id) {
                current.copy(track = track, isPlaying = true)
            } else {
                PlaybackSnapshot(track = track, isPlaying = true)
            }
        }
    }

    fun togglePlayPause() {
        state.update { current -> current?.copy(isPlaying = !current.isPlaying) }
    }

    fun render(track: PlaybackTrack?, isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        val progress = if (durationMs <= 0L) {
            0f
        } else {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }
        state.value = track?.let { current ->
            PlaybackSnapshot(
                track = LoadedTrack(current.id, current.title, current.artistName, current.artworkUrl),
                isPlaying = isPlaying,
                progress = progress,
            )
        }
    }
}
