package br.com.musicflow.common.domain

import java.time.Instant

data class PlaybackHistoryEntry(
    val trackId: TrackId,
    val playedAt: Instant,
)

data class PlaybackHistory(
    val entries: List<PlaybackHistoryEntry> = emptyList(),
) {
    init {
        entries.zipWithNext().forEach { (previous, next) ->
            require(!next.playedAt.isBefore(previous.playedAt)) {
                "History entries must be in chronological order."
            }
        }
    }

    fun record(trackId: TrackId, playedAt: Instant): PlaybackHistory {
        val updated = (entries + PlaybackHistoryEntry(trackId, playedAt)).sortedBy { it.playedAt }
        return copy(entries = updated)
    }

    fun recentlyPlayed(limit: Int): List<TrackId> {
        require(limit > 0) { "Recent track limit must be positive." }
        return entries
            .groupBy { it.trackId }
            .map { (_, plays) -> plays.maxBy { it.playedAt } }
            .sortedByDescending { it.playedAt }
            .take(limit)
            .map { it.trackId }
    }
}
