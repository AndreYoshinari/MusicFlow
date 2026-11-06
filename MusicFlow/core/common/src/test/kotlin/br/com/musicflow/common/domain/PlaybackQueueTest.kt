package br.com.musicflow.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PlaybackQueueTest {
    private val first = TrackId(10)
    private val second = TrackId(11)
    private val third = TrackId(12)
    private val queue = PlaybackQueue.of(listOf(first, second, third))

    @Test
    fun emptyQueueHasNoCurrentTrack() {
        val empty = PlaybackQueue.empty()

        assertEquals(null, empty.currentTrackId)
        assertEquals(QueueAdvance.Ended, empty.next(RepeatMode.All))
    }

    @Test
    fun nextWithoutRepeatAdvancesUntilTheEnd() {
        val advanced = queue.next(RepeatMode.Off) as QueueAdvance.Moved
        val ended = advanced.queue
            .select(2)
            .next(RepeatMode.Off)

        assertEquals(second, advanced.queue.currentTrackId)
        assertEquals(QueueAdvance.Ended, ended)
    }

    @Test
    fun nextWithRepeatOneStaysOnTheCurrentTrack() {
        val advanced = queue.next(RepeatMode.One) as QueueAdvance.Moved

        assertEquals(first, advanced.queue.currentTrackId)
    }

    @Test
    fun nextWithRepeatAllWrapsToTheFirstTrack() {
        val advanced = queue.select(2).next(RepeatMode.All) as QueueAdvance.Moved

        assertEquals(first, advanced.queue.currentTrackId)
    }

    @Test
    fun previousWithoutRepeatStopsAtTheFirstTrack() {
        val stayed = queue.previous(RepeatMode.Off)

        assertEquals(first, stayed.currentTrackId)
    }

    @Test
    fun previousWithRepeatOneStillMovesToThePreviousTrack() {
        val moved = queue.select(1).previous(RepeatMode.One)

        assertEquals(first, moved.currentTrackId)
    }

    @Test
    fun previousWithRepeatAllWrapsToTheLastTrack() {
        val wrapped = queue.previous(RepeatMode.All)

        assertEquals(third, wrapped.currentTrackId)
    }

    @Test
    fun queueRejectsIndexOutsideItsTracks() {
        assertThrows(IllegalArgumentException::class.java) {
            PlaybackQueue.of(listOf(first), startIndex = 1)
        }
    }
}
