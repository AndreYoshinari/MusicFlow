package br.com.musicflow.common.domain

import java.time.LocalDate

data class AlbumRef(
    val id: AlbumId,
    val title: String,
) {
    init {
        require(title.isNotBlank() && title == title.trim()) { "Album title must not be blank." }
    }
}

data class Album(
    val id: AlbumId,
    val title: String,
    val artist: ArtistRef,
    val artworkUrl: String?,
    val releaseDate: LocalDate?,
    val source: ContentSource,
) {
    init {
        require(title.isNotBlank() && title == title.trim()) { "Album title must not be blank." }
        require(artworkUrl == null || artworkUrl.isNotBlank()) { "Album artwork URL must not be blank." }
    }

    fun toRef(): AlbumRef = AlbumRef(id, title)
}
