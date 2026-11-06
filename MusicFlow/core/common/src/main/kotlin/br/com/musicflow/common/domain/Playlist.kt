package br.com.musicflow.common.domain

data class PlaylistTrack(
    val trackId: TrackId,
    val position: Int,
) {
    init {
        require(position >= 0) { "Playlist track position must not be negative." }
    }
}

data class Playlist(
    val id: PlaylistId,
    val name: String,
    val trackIds: List<TrackId> = emptyList(),
) {
    val tracks: List<PlaylistTrack> = trackIds.mapIndexed { index, trackId ->
        PlaylistTrack(trackId, index)
    }

    init {
        require(name.isNotBlank() && name == name.trim()) { "Playlist name must not be blank." }
        require(trackIds.distinct().size == trackIds.size) {
            "Playlist cannot contain the same track twice."
        }
    }

    fun rename(name: String): Playlist = copy(name = name.trim())

    fun add(trackId: TrackId): Playlist {
        require(trackId !in trackIds) { "Track is already in the playlist." }
        return copy(trackIds = trackIds + trackId)
    }

    fun remove(trackId: TrackId): Playlist = copy(trackIds = trackIds.filterNot { it == trackId })

    fun move(fromPosition: Int, toPosition: Int): Playlist {
        require(fromPosition in trackIds.indices) { "Origin position is outside the playlist." }
        require(toPosition in trackIds.indices) { "Target position is outside the playlist." }
        if (fromPosition == toPosition) return this
        val reordered = trackIds.toMutableList()
        val trackId = reordered.removeAt(fromPosition)
        reordered.add(toPosition, trackId)
        return copy(trackIds = reordered)
    }

    companion object {
        fun fromAssociations(
            id: PlaylistId,
            name: String,
            tracks: List<PlaylistTrack>,
        ): Playlist {
            val sorted = tracks.sortedBy { it.position }
            require(sorted.map { it.position } == sorted.indices.toList()) {
                "Playlist positions must be contiguous and start at zero."
            }
            return Playlist(
                id = id,
                name = name,
                trackIds = sorted.map { it.trackId },
            )
        }
    }
}
