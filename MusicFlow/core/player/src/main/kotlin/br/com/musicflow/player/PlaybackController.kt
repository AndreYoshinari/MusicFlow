package br.com.musicflow.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

internal class PlaybackController(
    private val engine: AudioEngine,
    private val random: (Int) -> Int = { bound -> Random.nextInt(bound) },
) : MusicPlayback, AudioEngineListener {
    private val playback = MutableStateFlow(PlaybackState())
    private val track = MutableStateFlow<PlaybackTrack?>(null)
    private val progress = MutableStateFlow(PlaybackPosition())
    private val playlist = MutableStateFlow(PlaybackQueue())

    override val playbackState: StateFlow<PlaybackState> = playback.asStateFlow()
    override val currentTrack: StateFlow<PlaybackTrack?> = track.asStateFlow()
    override val position: StateFlow<PlaybackPosition> = progress.asStateFlow()
    override val queue: StateFlow<PlaybackQueue> = playlist.asStateFlow()

    private var released = false
    private var handlingEnd = false
    private var tracks = listOf<PlaybackTrack>()
    private var currentIndex = -1
    private var order = listOf<Int>()

    init {
        engine.addListener(this)
    }

    override fun play(tracks: List<PlaybackTrack>, startIndex: Int) {
        if (released || tracks.isEmpty()) return
        val requested = tracks.getOrNull(startIndex)
        val playable = tracks.filter { isPlayableStream(it.streamUrl) }
        // A rejected selection must not stop audio that is already playing.
        if (playable.isEmpty() || (requested != null && !isPlayableStream(requested.streamUrl))) {
            playback.update { it.copy(failure = PlaybackFailure.InvalidSource) }
            return
        }
        this.tracks = playable
        currentIndex = if (requested == null) 0 else playable.indexOf(requested).coerceAtLeast(0)
        rebuildOrder()
        playIndex(currentIndex)
    }

    override fun pause() {
        if (released || track.value == null || !playback.value.isPlaying) return
        playback.update { it.copy(isPlaying = false) }
        engine.pause()
    }

    override fun resume() {
        if (released || track.value == null) return
        if (playback.value.phase == PlaybackPhase.Ended) {
            playIndex(currentIndex)
            return
        }
        playback.update { current ->
            current.copy(
                isPlaying = true,
                failure = null,
                phase = if (current.phase == PlaybackPhase.Idle) PlaybackPhase.Buffering else current.phase,
            )
        }
        engine.resume()
    }

    override fun stop() {
        if (released || track.value == null) return
        engine.stop()
        progress.value = PlaybackPosition()
        playback.update { it.copy(phase = PlaybackPhase.Idle, isPlaying = false) }
    }

    override fun next() {
        if (released || tracks.isEmpty()) return
        val next = nextIndex() ?: return
        playIndex(next)
    }

    override fun previous() {
        if (released || tracks.isEmpty()) return
        if (progress.value.positionMs > RESTART_PREVIOUS_AFTER_MS) {
            seekTo(0)
            return
        }
        val position = order.indexOf(currentIndex)
        when {
            position > 0 -> playIndex(order[position - 1])
            playback.value.repeatMode == RepeatMode.All && order.isNotEmpty() -> playIndex(order.last())
            else -> seekTo(0)
        }
    }

    override fun seekTo(positionMs: Long) {
        if (released || track.value == null) return
        val duration = progress.value.durationMs
        val target = if (duration > 0) positionMs.coerceIn(0, duration) else positionMs.coerceAtLeast(0)
        progress.update { it.copy(positionMs = target) }
        engine.seekTo(target)
    }

    override fun setVolume(volume: Float) {
        if (released) return
        val coerced = volume.coerceIn(0f, 1f)
        playback.update { it.copy(volume = coerced) }
        engine.setVolume(coerced)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        if (released) return
        playback.update { it.copy(repeatMode = mode) }
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        if (released || playback.value.shuffleEnabled == enabled) return
        playback.update { it.copy(shuffleEnabled = enabled) }
        if (tracks.isNotEmpty()) rebuildOrder()
    }

    override fun release() {
        if (released) return
        released = true
        engine.removeListener(this)
        engine.release()
    }

    override fun onPhase(phase: PlaybackPhase, isPlaying: Boolean) {
        if (released) return
        if (phase == PlaybackPhase.Ended) {
            onEnded()
            return
        }
        playback.update { it.copy(phase = phase, isPlaying = isPlaying) }
    }

    override fun onPosition(positionMs: Long, durationMs: Long, bufferedPositionMs: Long) {
        if (released) return
        progress.value = PlaybackPosition(
            positionMs = positionMs.coerceAtLeast(0),
            durationMs = durationMs.coerceAtLeast(0),
            bufferedPositionMs = bufferedPositionMs.coerceAtLeast(0),
        )
    }

    override fun onFailure(failure: PlaybackFailure) {
        if (released) return
        fail(failure)
    }

    private fun onEnded() {
        if (handlingEnd) return
        handlingEnd = true
        try {
            when (playback.value.repeatMode) {
                RepeatMode.One -> playIndex(currentIndex)
                RepeatMode.All -> nextIndex()?.let(::playIndex)
                RepeatMode.Off -> {
                    val next = nextIndex()
                    if (next != null) playIndex(next) else {
                        playback.update { it.copy(phase = PlaybackPhase.Ended, isPlaying = false) }
                    }
                }
            }
        } finally {
            handlingEnd = false
        }
    }

    private fun playIndex(index: Int) {
        val item = tracks.getOrNull(index) ?: return
        currentIndex = index
        publishQueue()
        progress.value = PlaybackPosition()
        playback.update { it.copy(phase = PlaybackPhase.Buffering, isPlaying = true, failure = null) }
        engine.play(
            PlayableMedia(
                uri = item.streamUrl,
                mediaId = item.id.value.toString(),
                title = item.title,
                artist = item.artistName,
                artworkUrl = item.artworkUrl,
            ),
        )
    }

    private fun nextIndex(): Int? {
        if (order.isEmpty()) return null
        val position = order.indexOf(currentIndex)
        val nextPosition = if (position < 0) 0 else position + 1
        return when {
            nextPosition <= order.lastIndex -> order[nextPosition]
            playback.value.repeatMode == RepeatMode.All -> order.first()
            else -> null
        }
    }

    private fun rebuildOrder() {
        order = if (playback.value.shuffleEnabled) {
            shuffledOrder(tracks.size, currentIndex.coerceAtLeast(0), random)
        } else {
            tracks.indices.toList()
        }
    }

    private fun publishQueue() {
        playlist.value = PlaybackQueue(tracks, currentIndex)
        track.value = tracks.getOrNull(currentIndex)
    }

    private fun fail(failure: PlaybackFailure) {
        playback.update { it.copy(phase = PlaybackPhase.Idle, isPlaying = false, failure = failure) }
    }

    private companion object {
        const val RESTART_PREVIOUS_AFTER_MS = 3_000L
    }
}
