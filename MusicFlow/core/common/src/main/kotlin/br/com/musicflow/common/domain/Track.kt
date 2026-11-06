package br.com.musicflow.common.domain

import java.time.LocalDate

data class Track(
    val id: TrackId,
    val title: String,
    val artist: ArtistRef,
    val album: AlbumRef,
    val artworkUrl: String?,
    val durationMs: Long,
    val streamUrl: String?,
    val licenseUrl: String,
    val source: ContentSource,
    val releaseDate: LocalDate?,
) {
    init {
        require(title.isNotBlank() && title == title.trim()) { "Track title must not be blank." }
        require(durationMs >= 0) { "Track duration must not be negative." }
        require(artworkUrl == null || artworkUrl.isNotBlank()) { "Track artwork URL must not be blank." }
        require(streamUrl == null || streamUrl.isNotBlank()) { "Track stream URL must not be blank." }
        require(licenseUrl.isNotBlank() && licenseUrl == licenseUrl.trim()) {
            "Track license URL must not be blank."
        }
    }
}
