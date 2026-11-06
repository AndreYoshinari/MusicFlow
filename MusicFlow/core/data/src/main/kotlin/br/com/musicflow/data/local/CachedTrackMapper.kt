package br.com.musicflow.data.local

import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.database.entity.CachedTrackEntity
import java.time.Instant
import java.time.LocalDate

internal const val STREAM_URL_MAX_AGE_MILLIS = 15 * 60 * 1_000L

internal fun Track.toCachedEntity(observedAt: Instant): CachedTrackEntity {
    val stream = streamUrl?.takeIf { it.isNotBlank() }
    return CachedTrackEntity(
        id = id.value,
        title = title,
        artistId = artist.id.value,
        artistName = artist.name,
        albumId = album.id.value,
        albumTitle = album.title,
        artworkUrl = artworkUrl,
        durationMs = durationMs,
        streamUrl = stream,
        streamObservedAt = stream?.let { observedAt.toEpochMilli() },
        licenseUrl = licenseUrl,
        provider = source.provider,
        pageUrl = source.pageUrl,
        releaseDate = releaseDate?.toString(),
        cachedAt = observedAt.toEpochMilli(),
    )
}

internal fun CachedTrackEntity.toTrack(now: Instant): Track {
    val observedAt = streamObservedAt?.let(Instant::ofEpochMilli)
    val streamIsFresh = observedAt != null && !now.isAfter(observedAt.plusMillis(STREAM_URL_MAX_AGE_MILLIS))
    return Track(
        id = TrackId(id),
        title = title,
        artist = ArtistRef(ArtistId(artistId), artistName),
        album = AlbumRef(AlbumId(albumId), albumTitle),
        artworkUrl = artworkUrl?.takeIf { it.isNotBlank() },
        durationMs = durationMs,
        streamUrl = streamUrl?.takeIf { it.isNotBlank() && streamIsFresh },
        licenseUrl = licenseUrl,
        source = ContentSource(provider, pageUrl),
        releaseDate = releaseDate?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
    )
}
