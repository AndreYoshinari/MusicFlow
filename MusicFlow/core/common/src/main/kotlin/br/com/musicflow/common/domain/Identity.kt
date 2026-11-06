package br.com.musicflow.common.domain

@JvmInline
value class TrackId(val value: Long) {
    init {
        require(value > 0) { "Track id must be positive." }
    }
}

@JvmInline
value class ArtistId(val value: Long) {
    init {
        require(value > 0) { "Artist id must be positive." }
    }
}

@JvmInline
value class AlbumId(val value: Long) {
    init {
        require(value > 0) { "Album id must be positive." }
    }
}

@JvmInline
value class PlaylistId(val value: Long) {
    init {
        require(value > 0) { "Playlist id must be positive." }
    }
}
