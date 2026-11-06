package br.com.musicflow.player

import br.com.musicflow.common.domain.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackControllerTest {
    private val engine = FakeAudioEngine()
    private val playback = PlaybackController(engine, random = { 0 })
    private val aurora = track(1, "Aurora", "https://cdn.example.com/aurora.mp3")
    private val maré = track(2, "Maré", "https://cdn.example.com/mare.mp3")
    private val noite = track(3, "Noite", "https://cdn.example.com/noite.mp3")

    @Test
    fun playPublishesTheQueueAndStartsTheSelectedTrack() {
        playback.play(listOf(aurora, maré), startIndex = 1)

        assertEquals(maré, playback.currentTrack.value)
        assertEquals(listOf(aurora, maré), playback.queue.value.tracks)
        assertEquals(1, playback.queue.value.currentIndex)
        assertEquals(PlaybackPhase.Buffering, playback.playbackState.value.phase)
        assertTrue(playback.playbackState.value.isPlaying)
        assertEquals(listOf(maré.streamUrl), engine.played)
        assertEquals("Maré", engine.lastMedia?.title)
        assertEquals("Luna Park", engine.lastMedia?.artist)
    }

    @Test
    fun invalidSelectionKeepsTheTrackThatIsAlreadyPlaying() {
        playback.play(listOf(aurora))
        playback.play(listOf(aurora.copy(streamUrl = "notaurl")))

        assertEquals(aurora, playback.currentTrack.value)
        assertTrue(playback.playbackState.value.isPlaying)
        assertEquals(PlaybackFailure.InvalidSource, playback.playbackState.value.failure)
        assertEquals(listOf(aurora.streamUrl), engine.played)
    }

    @Test
    fun invalidAddressDoesNotReachTheEngine() {
        playback.play(listOf(aurora.copy(streamUrl = "notaurl")))

        assertEquals(PlaybackFailure.InvalidSource, playback.playbackState.value.failure)
        assertFalse(playback.playbackState.value.isPlaying)
        assertTrue(engine.played.isEmpty())
    }

    @Test
    fun queueDropsUnusableAddressesAndKeepsTheRequestedTrack() {
        playback.play(listOf(aurora, maré.copy(streamUrl = "ftp://cdn.example.com/mare.mp3"), noite), startIndex = 2)

        assertEquals(listOf(aurora, noite), playback.queue.value.tracks)
        assertEquals(noite, playback.currentTrack.value)
        assertEquals(listOf(noite.streamUrl), engine.played)
    }

    @Test
    fun pauseResumeStopSeekAndVolumeStayOnTheSharedEngine() {
        playback.play(listOf(aurora))
        engine.emitPosition(1_500, 10_000, 4_000)

        playback.pause()
        assertFalse(playback.playbackState.value.isPlaying)
        assertEquals("pause", engine.commands.last())

        playback.resume()
        assertTrue(playback.playbackState.value.isPlaying)
        assertEquals("resume", engine.commands.last())

        playback.seekTo(50_000)
        assertEquals(10_000, playback.position.value.positionMs)
        playback.seekTo(-20)
        assertEquals(0, playback.position.value.positionMs)

        playback.setVolume(2f)
        playback.setVolume(-1f)
        assertEquals(0f, playback.playbackState.value.volume)
        assertEquals(0f, engine.appliedVolume)

        playback.stop()
        assertEquals(PlaybackPhase.Idle, playback.playbackState.value.phase)
        assertEquals(0, playback.position.value.positionMs)
        assertEquals(aurora, playback.currentTrack.value)
        assertEquals("stop", engine.commands.last())
    }

    @Test
    fun nextAndPreviousFollowTheQueue() {
        playback.play(listOf(aurora, maré, noite))

        playback.next()
        assertEquals(maré, playback.currentTrack.value)

        engine.emitPosition(4_000, 10_000, 4_000)
        playback.previous()
        assertEquals(maré, playback.currentTrack.value)
        assertEquals(0, playback.position.value.positionMs)
        assertEquals(0L, engine.seeked)

        playback.previous()
        assertEquals(aurora, playback.currentTrack.value)
    }

    @Test
    fun repeatOffStopsAtTheEndAndRepeatAllWraps() {
        playback.play(listOf(aurora, maré), startIndex = 1)
        playback.next()
        assertEquals(maré, playback.currentTrack.value)

        engine.emitPhase(PlaybackPhase.Ended, isPlaying = false)
        assertEquals(PlaybackPhase.Ended, playback.playbackState.value.phase)
        assertFalse(playback.playbackState.value.isPlaying)

        playback.setRepeatMode(RepeatMode.All)
        playback.next()
        assertEquals(aurora, playback.currentTrack.value)
    }

    @Test
    fun repeatOneReplaysTheCurrentTrackWhenItEnds() {
        playback.play(listOf(aurora, maré))
        playback.setRepeatMode(RepeatMode.One)
        engine.played.clear()

        engine.emitPhase(PlaybackPhase.Ended, isPlaying = false)

        assertEquals(listOf(aurora.streamUrl), engine.played)
        assertEquals(aurora, playback.currentTrack.value)
        playback.next()
        assertEquals(maré, playback.currentTrack.value)
    }

    @Test
    fun shuffleUsesTheSuppliedOrder() {
        playback.setShuffleEnabled(true)
        playback.play(listOf(aurora, maré, noite))

        playback.next()

        assertEquals(noite, playback.currentTrack.value)
        assertTrue(playback.playbackState.value.shuffleEnabled)
    }

    @Test
    fun connectionFailureStopsPlaybackAndKeepsTheTrack() {
        playback.play(listOf(aurora))
        engine.emitPhase(PlaybackPhase.Ready, isPlaying = true)

        engine.emitFailure(PlaybackFailure.Connection)

        assertEquals(PlaybackFailure.Connection, playback.playbackState.value.failure)
        assertEquals(PlaybackPhase.Idle, playback.playbackState.value.phase)
        assertFalse(playback.playbackState.value.isPlaying)
        assertEquals(aurora, playback.currentTrack.value)
    }

    @Test
    fun bufferingAndPositionComeFromTheEngine() {
        playback.play(listOf(aurora))
        engine.emitPhase(PlaybackPhase.Buffering, isPlaying = true)
        engine.emitPhase(PlaybackPhase.Ready, isPlaying = true)
        engine.emitPosition(2_000, 8_000, 5_000)

        assertEquals(PlaybackPhase.Ready, playback.playbackState.value.phase)
        assertEquals(2_000, playback.position.value.positionMs)
        assertEquals(8_000, playback.position.value.durationMs)
        assertEquals(5_000, playback.position.value.bufferedPositionMs)
    }

    @Test
    fun releaseDropsLaterCommands() {
        playback.release()
        playback.play(listOf(aurora))
        playback.release()

        assertNull(playback.currentTrack.value)
        assertTrue(engine.played.isEmpty())
        assertEquals(1, engine.releaseCount)
    }

    @Test
    fun turningShuffleOffRestoresSequentialOrder() {
        playback.setShuffleEnabled(true)
        playback.play(listOf(aurora, maré, noite))
        playback.next()
        assertEquals(noite, playback.currentTrack.value)

        playback.setShuffleEnabled(false)
        playback.next()

        assertFalse(playback.playbackState.value.shuffleEnabled)
        assertEquals(noite, playback.currentTrack.value)
    }

    @Test
    fun previousAtTheStartRestartsAndRepeatAllWrapsBackward() {
        playback.play(listOf(aurora, maré))
        engine.emitPosition(1_000, 10_000, 1_000)

        playback.previous()

        assertEquals(aurora, playback.currentTrack.value)
        assertEquals(0L, engine.seeked)

        playback.setRepeatMode(RepeatMode.All)
        playback.previous()

        assertEquals(maré, playback.currentTrack.value)
    }

    @Test
    fun resumeAfterTheTrackEndsPlaysItAgain() {
        playback.play(listOf(aurora))
        engine.emitPhase(PlaybackPhase.Ended, isPlaying = false)
        engine.played.clear()

        playback.resume()

        assertEquals(listOf(aurora.streamUrl), engine.played)
        assertTrue(playback.playbackState.value.isPlaying)
        assertEquals(null, playback.playbackState.value.failure)
    }

    @Test
    fun emptyQueueDoesNotStartPlayback() {
        playback.play(emptyList())

        assertNull(playback.currentTrack.value)
        assertTrue(engine.played.isEmpty())
    }
}

private class FakeAudioEngine : AudioEngine {
    val played = mutableListOf<String>()
    var lastMedia: PlayableMedia? = null
    val commands = mutableListOf<String>()
    var appliedVolume = 1f
    var seeked: Long? = null
    var releaseCount = 0
    private var listener: AudioEngineListener? = null
    private var released = false

    override fun play(media: PlayableMedia, positionMs: Long) {
        if (released) return
        lastMedia = media
        played += media.uri
        commands += "play"
    }

    override fun pause() {
        commands += "pause"
    }

    override fun resume() {
        commands += "resume"
    }

    override fun stop() {
        commands += "stop"
    }

    override fun seekTo(positionMs: Long) {
        seeked = positionMs
        commands += "seek"
    }

    override fun setVolume(volume: Float) {
        appliedVolume = volume
    }

    override fun addListener(listener: AudioEngineListener) {
        this.listener = listener
    }

    override fun removeListener(listener: AudioEngineListener) {
        if (this.listener === listener) this.listener = null
    }

    override fun release() {
        released = true
        releaseCount += 1
    }

    fun emitPhase(phase: PlaybackPhase, isPlaying: Boolean) {
        listener?.onPhase(phase, isPlaying)
    }

    fun emitPosition(positionMs: Long, durationMs: Long, bufferedPositionMs: Long) {
        listener?.onPosition(positionMs, durationMs, bufferedPositionMs)
    }

    fun emitFailure(failure: PlaybackFailure) {
        listener?.onFailure(failure)
    }
}

private fun track(id: Long, title: String, streamUrl: String) = PlaybackTrack(
    id = TrackId(id),
    title = title,
    artistName = "Luna Park",
    artworkUrl = null,
    streamUrl = streamUrl,
)
