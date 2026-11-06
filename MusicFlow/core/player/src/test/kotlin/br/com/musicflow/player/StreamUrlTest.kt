package br.com.musicflow.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamUrlTest {
    @Test
    fun acceptsHttpAndHttpsStreams() {
        assertTrue(isPlayableStream("https://cdn.example.com/aurora.mp3"))
        assertTrue(isPlayableStream("http://cdn.example.com/aurora.mp3"))
    }

    @Test
    fun rejectsBlankMalformedAndUnsupportedSchemes() {
        assertFalse(isPlayableStream(""))
        assertFalse(isPlayableStream("   "))
        assertFalse(isPlayableStream("notaurl"))
        assertFalse(isPlayableStream("ftp://cdn.example.com/aurora.mp3"))
        assertFalse(isPlayableStream("https://"))
        assertFalse(isPlayableStream(" https://cdn.example.com/aurora.mp3"))
    }
}
