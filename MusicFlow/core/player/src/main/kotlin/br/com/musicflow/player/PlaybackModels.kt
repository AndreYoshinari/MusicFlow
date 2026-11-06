package br.com.musicflow.player

import br.com.musicflow.common.domain.TrackId

data class PlaybackTrack(
    val id: TrackId,
    val title: String,
    val artistName: String,
    val artworkUrl: String?,
    val streamUrl: String,
)

enum class RepeatMode {
    Off,
    One,
    All,
}

enum class PlaybackPhase {
    Idle,
    Buffering,
    Ready,
    Ended,
}

sealed interface PlaybackFailure {
    data object InvalidSource : PlaybackFailure

    data object Connection : PlaybackFailure

    data object Playback : PlaybackFailure
}

data class PlaybackState(
    val phase: PlaybackPhase = PlaybackPhase.Idle,
    val isPlaying: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val volume: Float = 1f,
    val failure: PlaybackFailure? = null,
)

data class PlaybackPosition(
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val bufferedPositionMs: Long = 0,
)

data class PlaybackQueue(
    val tracks: List<PlaybackTrack> = emptyList(),
    val currentIndex: Int = -1,
)
