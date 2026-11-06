package br.com.musicflow.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TrackTest {
    @Test
    fun trackRejectsBlankTitle() {
        assertThrows(IllegalArgumentException::class.java) {
            testTrack(title = "  ")
        }
    }

    @Test
    fun trackRejectsNegativeDuration() {
        assertThrows(IllegalArgumentException::class.java) {
            testTrack(durationMs = -1)
        }
    }

    @Test
    fun trackRejectsBlankLicense() {
        assertThrows(IllegalArgumentException::class.java) {
            testTrack(licenseUrl = " ")
        }
    }

    @Test
    fun trackAllowsMissingStreamUntilPlayback() {
        val track = testTrack(streamUrl = null)
        assertEquals(null, track.streamUrl)
    }

    @Test
    fun idsRejectZero() {
        assertThrows(IllegalArgumentException::class.java) { TrackId(0) }
        assertThrows(IllegalArgumentException::class.java) { ArtistId(-1) }
        assertThrows(IllegalArgumentException::class.java) { AlbumId(0) }
        assertThrows(IllegalArgumentException::class.java) { PlaylistId(0) }
    }

    @Test
    fun sameIdValueIsEqual() {
        assertEquals(TrackId(10), TrackId(10))
    }

    @Test
    fun contentSourceRejectsBlankProvider() {
        assertThrows(IllegalArgumentException::class.java) {
            ContentSource(provider = " ", pageUrl = "https://www.jamendo.com/track/1")
        }
    }
}
