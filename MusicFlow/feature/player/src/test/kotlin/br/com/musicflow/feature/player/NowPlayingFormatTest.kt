package br.com.musicflow.feature.player

import br.com.musicflow.player.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingFormatTest {
    @Test
    fun repeatCyclesThroughTheThreeModes() {
        assertEquals(RepeatMode.All, RepeatMode.Off.cycled())
        assertEquals(RepeatMode.One, RepeatMode.All.cycled())
        assertEquals(RepeatMode.Off, RepeatMode.One.cycled())
    }

    @Test
    fun playbackTimeUsesMinutesUntilAnHour() {
        assertEquals("0:00", formatPlaybackTime(0))
        assertEquals("0:05", formatPlaybackTime(5_000))
        assertEquals("3:00", formatPlaybackTime(180_000))
        assertEquals("1:01:01", formatPlaybackTime(3_661_000))
    }
}
