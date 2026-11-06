package br.com.musicflow.feature.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchDurationTest {
    @Test
    fun formatsMinutesAndHours() {
        assertEquals("0:00", formatTrackDuration(0))
        assertEquals("0:05", formatTrackDuration(5_000))
        assertEquals("1:05", formatTrackDuration(65_000))
        assertEquals("3:00", formatTrackDuration(180_000))
        assertEquals("1:01:01", formatTrackDuration(3_661_000))
    }
}
