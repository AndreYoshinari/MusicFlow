package br.com.musicflow.player

internal fun shuffledOrder(count: Int, anchor: Int, random: (Int) -> Int): List<Int> {
    if (count <= 0) return emptyList()
    val safeAnchor = anchor.coerceIn(0, count - 1)
    val rest = (0 until count).filter { it != safeAnchor }.toMutableList()
    for (index in rest.lastIndex downTo 1) {
        val swap = random(index + 1)
        val current = rest[index]
        rest[index] = rest[swap]
        rest[swap] = current
    }
    return listOf(safeAnchor) + rest
}
