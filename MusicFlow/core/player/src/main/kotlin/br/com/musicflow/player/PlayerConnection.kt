package br.com.musicflow.player

internal class PlayerConnection<T : Any> {
    private var current: T? = null
    private val pending = ArrayDeque<(T) -> Unit>()

    fun current(): T? = current

    fun attach(player: T) {
        current = player
        while (pending.isNotEmpty()) {
            pending.removeFirst().invoke(player)
        }
    }

    fun detach(player: T) {
        if (current === player) current = null
    }

    fun whenReady(block: (T) -> Unit) {
        val player = current
        if (player != null) block(player) else pending.addLast(block)
    }

    fun clearPending() {
        pending.clear()
    }
}
