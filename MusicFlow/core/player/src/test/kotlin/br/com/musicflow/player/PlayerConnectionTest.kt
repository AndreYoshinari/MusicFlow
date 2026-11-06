package br.com.musicflow.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PlayerConnectionTest {
    @Test
    fun commandsWaitUntilTheSinglePlayerIsAttached() {
        val connection = PlayerConnection<Any>()
        val first = Any()
        val played = mutableListOf<Any>()

        connection.whenReady { played += it }
        assertEquals(emptyList<Any>(), played)

        connection.attach(first)
        connection.whenReady { played += it }

        assertEquals(listOf(first, first), played)
        assertSame(first, connection.current())
    }

    @Test
    fun detachReleasesOnlyTheAttachedPlayer() {
        val connection = PlayerConnection<Any>()
        val first = Any()
        val second = Any()
        connection.attach(first)

        connection.detach(first)
        connection.attach(second)
        connection.detach(first)

        assertSame(second, connection.current())
        connection.detach(second)
        assertNull(connection.current())
    }
}
