package br.com.musicflow.player

import kotlinx.coroutines.flow.StateFlow

interface PlaybackObserver {
    val playbackState: StateFlow<PlaybackState>

    val currentTrack: StateFlow<PlaybackTrack?>

    val position: StateFlow<PlaybackPosition>

    val queue: StateFlow<PlaybackQueue>
}

interface PlaybackCommands {
    fun play(tracks: List<PlaybackTrack>, startIndex: Int = 0)

    fun pause()

    fun resume()

    fun stop()

    fun next()

    fun previous()

    fun seekTo(positionMs: Long)

    fun setVolume(volume: Float)

    fun setRepeatMode(mode: RepeatMode)

    fun setShuffleEnabled(enabled: Boolean)

    fun release()
}

interface MusicPlayback : PlaybackObserver, PlaybackCommands
