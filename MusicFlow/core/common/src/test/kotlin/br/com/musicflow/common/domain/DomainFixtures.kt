package br.com.musicflow.common.domain

import java.time.LocalDate

internal fun testSource(trackId: Long = 1): ContentSource = ContentSource(
    provider = "Jamendo",
    pageUrl = "https://www.jamendo.com/track/$trackId",
)

internal fun testTrack(
    id: Long = 1,
    title: String = "Aurora",
    durationMs: Long = 180_000,
    streamUrl: String? = "https://example.com/stream/$id",
    licenseUrl: String = "https://creativecommons.org/licenses/by-nc-nd/3.0/",
): Track = Track(
    id = TrackId(id),
    title = title,
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = "https://example.com/art/$id",
    durationMs = durationMs,
    streamUrl = streamUrl,
    licenseUrl = licenseUrl,
    source = testSource(id),
    releaseDate = LocalDate.of(2024, 5, 2),
)
