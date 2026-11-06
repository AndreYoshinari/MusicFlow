package br.com.musicflow.data.local

import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CachedTrackMapperTest {
    private val observedAt = Instant.parse("2026-11-05T18:00:00Z")

    @Test
    fun freshStreamUrlIsAvailableWithTheMetadata() {
        val track = sample(streamUrl = "https://example.com/stream").toCachedEntity(observedAt)
            .toTrack(observedAt.plusMillis(STREAM_URL_MAX_AGE_MILLIS))

        assertEquals("https://example.com/stream", track.streamUrl)
        assertEquals("Aurora", track.title)
        assertEquals(LocalDate.of(2024, 5, 2), track.releaseDate)
    }

    @Test
    fun expiredStreamUrlIsOmittedAndTheMetadataRemains() {
        val track = sample(streamUrl = "https://example.com/stream").toCachedEntity(observedAt)
            .toTrack(observedAt.plusMillis(STREAM_URL_MAX_AGE_MILLIS + 1))

        assertNull(track.streamUrl)
        assertEquals("Aurora", track.title)
        assertEquals("https://example.com/art", track.artworkUrl)
        assertEquals("https://creativecommons.org/licenses/by/4.0/", track.licenseUrl)
    }

    private fun sample(streamUrl: String?): Track = Track(
        id = TrackId(1),
        title = "Aurora",
        artist = ArtistRef(ArtistId(7), "Luna"),
        album = AlbumRef(AlbumId(3), "Noite"),
        artworkUrl = "https://example.com/art",
        durationMs = 180_000,
        streamUrl = streamUrl,
        licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
        source = ContentSource("Jamendo", "https://www.jamendo.com/track/1"),
        releaseDate = LocalDate.of(2024, 5, 2),
    )
}
