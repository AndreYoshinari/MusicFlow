package br.com.musicflow.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class PlaybackHistoryTest {
    private val first = TrackId(10)
    private val second = TrackId(11)
    private val noon = Instant.parse("2026-05-01T12:00:00Z")

    @Test
    fun recordKeepsEntriesInChronologicalOrder() {
        val history = PlaybackHistory()
            .record(second, noon.plusSeconds(60))
            .record(first, noon)

        assertEquals(
            listOf(
                PlaybackHistoryEntry(first, noon),
                PlaybackHistoryEntry(second, noon.plusSeconds(60)),
            ),
            history.entries,
        )
    }

    @Test
    fun directConstructionRejectsOutOfOrderEntries() {
        assertThrows(IllegalArgumentException::class.java) {
            PlaybackHistory(
                listOf(
                    PlaybackHistoryEntry(second, noon.plusSeconds(60)),
                    PlaybackHistoryEntry(first, noon),
                ),
            )
        }
    }

    @Test
    fun recentlyPlayedKeepsTheLatestPlayOfEachTrack() {
        val history = PlaybackHistory()
            .record(first, noon)
            .record(second, noon.plusSeconds(10))
            .record(first, noon.plusSeconds(20))

        assertEquals(listOf(first, second), history.recentlyPlayed(limit = 10))
    }

    @Test
    fun recentlyPlayedRespectsTheLimit() {
        val history = PlaybackHistory()
            .record(first, noon)
            .record(second, noon.plusSeconds(10))

        assertEquals(listOf(second), history.recentlyPlayed(limit = 1))
    }

    @Test
    fun recentlyPlayedRejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            PlaybackHistory().recentlyPlayed(limit = 0)
        }
    }
}
