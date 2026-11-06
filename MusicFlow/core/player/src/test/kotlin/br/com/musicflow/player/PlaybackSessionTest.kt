package br.com.musicflow.player

import br.com.musicflow.common.domain.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSessionTest {
    private val session = PlaybackSession()
    private val track = LoadedTrack(TrackId(4), "Aurora", "Luna Park")

    @Test
    fun startsWithoutALoadedTrack() {
        assertNull(session.snapshot.value)
        session.togglePlayPause()
        assertNull(session.snapshot.value)
    }

    @Test
    fun reloadingTheSameTrackKeepsPlayback() {
        session.load(track)
        session.togglePlayPause()

        session.load(track.copy(title = "Aurora (ao vivo)"))

        val snapshot = session.snapshot.value
        assertEquals("Aurora (ao vivo)", snapshot?.track?.title)
        assertTrue(snapshot!!.isPlaying)
        assertEquals(0f, snapshot.progress)
    }

    @Test
    fun playStartsTheTrack() {
        session.play(track)

        val snapshot = session.snapshot.value
        assertEquals(track.id, snapshot?.track?.id)
        assertTrue(snapshot!!.isPlaying)
    }

    @Test
    fun playingTheLoadedTrackKeepsItPlaying() {
        session.load(track)

        session.play(track.copy(title = "Aurora (ao vivo)"))

        val snapshot = session.snapshot.value
        assertEquals("Aurora (ao vivo)", snapshot?.track?.title)
        assertTrue(snapshot!!.isPlaying)
    }

    @Test
    fun loadingAnotherTrackReplacesTheSnapshot() {
        session.load(track)
        session.togglePlayPause()

        session.load(LoadedTrack(TrackId(9), "Maré", "Luna Park"))

        val snapshot = session.snapshot.value
        assertEquals(TrackId(9), snapshot?.track?.id)
        assertFalse(snapshot!!.isPlaying)
    }
}
