package br.com.musicflow.common.domain

data class PlaybackQueue(
    val trackIds: List<TrackId>,
    val currentIndex: Int,
) {
    val currentTrackId: TrackId? = trackIds.getOrNull(currentIndex)

    init {
        if (trackIds.isEmpty()) {
            require(currentIndex == EMPTY_INDEX) { "An empty queue has no current track." }
        } else {
            require(currentIndex in trackIds.indices) { "Current index is outside the queue." }
        }
    }

    fun select(index: Int): PlaybackQueue {
        require(index in trackIds.indices) { "Selected index is outside the queue." }
        return copy(currentIndex = index)
    }

    fun next(repeatMode: RepeatMode): QueueAdvance {
        if (trackIds.isEmpty()) return QueueAdvance.Ended
        return when (repeatMode) {
            RepeatMode.One -> QueueAdvance.Moved(this)
            RepeatMode.Off -> {
                if (currentIndex == trackIds.lastIndex) {
                    QueueAdvance.Ended
                } else {
                    QueueAdvance.Moved(copy(currentIndex = currentIndex + 1))
                }
            }
            RepeatMode.All -> {
                val nextIndex = if (currentIndex == trackIds.lastIndex) 0 else currentIndex + 1
                QueueAdvance.Moved(copy(currentIndex = nextIndex))
            }
        }
    }

    fun previous(repeatMode: RepeatMode): PlaybackQueue {
        if (trackIds.isEmpty()) return this
        val targetIndex = when (repeatMode) {
            RepeatMode.All -> if (currentIndex == 0) trackIds.lastIndex else currentIndex - 1
            RepeatMode.Off, RepeatMode.One -> (currentIndex - 1).coerceAtLeast(0)
        }
        return copy(currentIndex = targetIndex)
    }

    companion object {
        const val EMPTY_INDEX = -1

        fun empty(): PlaybackQueue = PlaybackQueue(emptyList(), EMPTY_INDEX)

        fun of(trackIds: List<TrackId>, startIndex: Int = 0): PlaybackQueue {
            require(trackIds.isNotEmpty()) { "Queue requires at least one track." }
            return PlaybackQueue(trackIds, startIndex)
        }
    }
}

sealed interface QueueAdvance {
    data class Moved(val queue: PlaybackQueue) : QueueAdvance

    data object Ended : QueueAdvance
}
