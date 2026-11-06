package br.com.musicflow.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtworkUrlTest {
    @Test
    fun keepsAnHttpArtworkAddress() {
        assertEquals(
            "https://cdn.example.com/aurora.jpg",
            artworkUrlOrNull("https://cdn.example.com/aurora.jpg"),
        )
    }

    @Test
    fun dropsAMissingOrUnsupportedArtworkAddress() {
        assertNull(artworkUrlOrNull(null))
        assertNull(artworkUrlOrNull("notaurl"))
    }
}
