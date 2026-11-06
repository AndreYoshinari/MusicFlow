package br.com.musicflow.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PlaylistTest {
    private val playlistId = PlaylistId(1)
    private val first = TrackId(10)
    private val second = TrackId(11)
    private val third = TrackId(12)

    @Test
    fun addAppendsTrackAndDerivesPositionFromOrder() {
        val playlist = Playlist(playlistId, "Noite").add(first).add(second)

        assertEquals(listOf(first, second), playlist.trackIds)
        assertEquals(
            listOf(PlaylistTrack(first, 0), PlaylistTrack(second, 1)),
            playlist.tracks,
        )
    }

    @Test
    fun addRejectsDuplicateTrack() {
        val playlist = Playlist(playlistId, "Noite").add(first)

        assertThrows(IllegalArgumentException::class.java) {
            playlist.add(first)
        }
    }

    @Test
    fun removeReindexesRemainingTracks() {
        val playlist = Playlist(playlistId, "Noite", listOf(first, second, third)).remove(second)

        assertEquals(
            listOf(PlaylistTrack(first, 0), PlaylistTrack(third, 1)),
            playlist.tracks,
        )
    }

    @Test
    fun removeMissingTrackKeepsThePlaylist() {
        val playlist = Playlist(playlistId, "Noite", listOf(first))

        assertEquals(playlist, playlist.remove(second))
    }

    @Test
    fun moveDownShiftsTheChosenTrack() {
        val playlist = Playlist(playlistId, "Noite", listOf(first, second, third)).move(0, 2)

        assertEquals(listOf(second, third, first), playlist.trackIds)
    }

    @Test
    fun moveUpShiftsTheChosenTrack() {
        val playlist = Playlist(playlistId, "Noite", listOf(first, second, third)).move(2, 0)

        assertEquals(listOf(third, first, second), playlist.trackIds)
    }

    @Test
    fun moveRejectsPositionOutsideThePlaylist() {
        val playlist = Playlist(playlistId, "Noite", listOf(first))

        assertThrows(IllegalArgumentException::class.java) {
            playlist.move(0, 1)
        }
    }

    @Test
    fun renameTrimsTheName() {
        val playlist = Playlist(playlistId, "Noite").rename("  Madrugada  ")

        assertEquals("Madrugada", playlist.name)
    }

    @Test
    fun blankNameIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Playlist(playlistId, "  ")
        }
    }

    @Test
    fun fromAssociationsRequiresContiguousPositionsStartingAtZero() {
        assertThrows(IllegalArgumentException::class.java) {
            Playlist.fromAssociations(
                id = playlistId,
                name = "Noite",
                tracks = listOf(PlaylistTrack(first, 0), PlaylistTrack(second, 2)),
            )
        }
    }

    @Test
    fun fromAssociationsSortsByPosition() {
        val playlist = Playlist.fromAssociations(
            id = playlistId,
            name = "Noite",
            tracks = listOf(PlaylistTrack(second, 1), PlaylistTrack(first, 0)),
        )

        assertEquals(listOf(first, second), playlist.trackIds)
    }

    @Test
    fun negativeAssociationPositionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PlaylistTrack(first, -1)
        }
    }
}
