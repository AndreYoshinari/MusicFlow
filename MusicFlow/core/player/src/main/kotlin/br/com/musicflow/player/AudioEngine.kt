package br.com.musicflow.player

internal data class PlayableMedia(
    val uri: String,
    val mediaId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
)

internal fun artworkUrlOrNull(url: String?): String? = url?.takeIf { isPlayableStream(it) }

internal interface AudioEngine {
    fun play(media: PlayableMedia, positionMs: Long = 0)

    fun pause()

    fun resume()

    fun stop()

    fun seekTo(positionMs: Long)

    fun setVolume(volume: Float)

    fun addListener(listener: AudioEngineListener)

    fun removeListener(listener: AudioEngineListener)

    fun release()
}

internal interface AudioEngineListener {
    fun onPhase(phase: PlaybackPhase, isPlaying: Boolean)

    fun onPosition(positionMs: Long, durationMs: Long, bufferedPositionMs: Long)

    fun onFailure(failure: PlaybackFailure)
}
