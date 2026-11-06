package br.com.musicflow.common.domain

sealed interface PlaybackState {
    data object Idle : PlaybackState

    data object Buffering : PlaybackState

    data object Playing : PlaybackState

    data object Paused : PlaybackState

    data object Ended : PlaybackState

    data class Error(val failure: PlaybackFailure) : PlaybackState
}

enum class PlaybackFailure {
    SourceUnavailable,
    Unknown,
}
