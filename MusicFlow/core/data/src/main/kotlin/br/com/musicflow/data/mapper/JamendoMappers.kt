package br.com.musicflow.data.mapper

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.network.NetworkError
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoAlbumDto
import br.com.musicflow.network.dto.JamendoArtistDto
import br.com.musicflow.network.dto.JamendoTrackDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val JAMENDO_PROVIDER = "Jamendo"

internal fun NetworkError.toDomainError(): DomainError = when (this) {
    NetworkError.Offline -> DomainError.Offline
    NetworkError.Timeout -> DomainError.Timeout
    NetworkError.InvalidJson -> DomainError.InvalidResponse
    NetworkError.MissingClientId -> DomainError.Unauthorized
    NetworkError.Unavailable -> DomainError.Unavailable
    is NetworkError.Http -> when (code) {
        400 -> DomainError.InvalidArgument
        401 -> DomainError.Unauthorized
        403 -> DomainError.Forbidden
        404 -> DomainError.NotFound
        429 -> DomainError.RateLimited
        in 500..599 -> DomainError.Unavailable
        else -> DomainError.Unavailable
    }
    is NetworkError.Api -> when (code) {
        3, 4 -> DomainError.InvalidArgument
        5 -> DomainError.Unauthorized
        6 -> DomainError.RateLimited
        7 -> DomainError.NotFound
        11 -> DomainError.Forbidden
        else -> DomainError.Unavailable
    }
}

internal fun NetworkResult<List<JamendoTrackDto>>.toTrackList(): DomainResult<List<Track>> =
    when (this) {
        is NetworkResult.Failure -> DomainResult.Failure(error.toDomainError())
        is NetworkResult.Success -> value.toTracks()
    }

internal fun NetworkResult<List<JamendoTrackDto>>.toSingleTrack(): DomainResult<Track> =
    when (this) {
        is NetworkResult.Failure -> DomainResult.Failure(error.toDomainError())
        is NetworkResult.Success -> when {
            value.isEmpty() -> DomainResult.Failure(DomainError.NotFound)
            else -> value.firstNotNullOfOrNull { it.toTrackOrNull() }
                ?.let { DomainResult.Success(it) }
                ?: DomainResult.Failure(DomainError.InvalidResponse)
        }
    }

internal fun NetworkResult<List<JamendoArtistDto>>.toArtistList(): DomainResult<List<Artist>> =
    when (this) {
        is NetworkResult.Failure -> DomainResult.Failure(error.toDomainError())
        is NetworkResult.Success -> value.toArtists()
    }

internal fun NetworkResult<List<JamendoArtistDto>>.toSingleArtist(): DomainResult<Artist> =
    when (this) {
        is NetworkResult.Failure -> DomainResult.Failure(error.toDomainError())
        is NetworkResult.Success -> when {
            value.isEmpty() -> DomainResult.Failure(DomainError.NotFound)
            else -> value.firstNotNullOfOrNull { it.toArtistOrNull() }
                ?.let { DomainResult.Success(it) }
                ?: DomainResult.Failure(DomainError.InvalidResponse)
        }
    }

internal fun NetworkResult<List<JamendoAlbumDto>>.toAlbumList(): DomainResult<List<Album>> =
    when (this) {
        is NetworkResult.Failure -> DomainResult.Failure(error.toDomainError())
        is NetworkResult.Success -> value.toAlbums()
    }

private fun List<JamendoTrackDto>.toTracks(): DomainResult<List<Track>> {
    if (isEmpty()) return DomainResult.Success(emptyList())
    val tracks = mapNotNull { it.toTrackOrNull() }
    return if (tracks.isEmpty()) {
        DomainResult.Failure(DomainError.InvalidResponse)
    } else {
        DomainResult.Success(tracks)
    }
}

private fun List<JamendoArtistDto>.toArtists(): DomainResult<List<Artist>> {
    if (isEmpty()) return DomainResult.Success(emptyList())
    val artists = mapNotNull { it.toArtistOrNull() }
    return if (artists.isEmpty()) {
        DomainResult.Failure(DomainError.InvalidResponse)
    } else {
        DomainResult.Success(artists)
    }
}

private fun List<JamendoAlbumDto>.toAlbums(): DomainResult<List<Album>> {
    if (isEmpty()) return DomainResult.Success(emptyList())
    val albums = mapNotNull { it.toAlbumOrNull() }
    return if (albums.isEmpty()) {
        DomainResult.Failure(DomainError.InvalidResponse)
    } else {
        DomainResult.Success(albums)
    }
}

internal fun JamendoTrackDto.toTrackOrNull(): Track? {
    val trackId = id.toPositiveLong() ?: return null
    val mappedArtistId = artistId.toPositiveLong() ?: return null
    val mappedAlbumId = albumId.toPositiveLong() ?: return null
    val title = name.trimmedOrNull() ?: return null
    val mappedArtistName = artistName.trimmedOrNull() ?: return null
    val albumTitle = albumName.trimmedOrNull() ?: return null
    val license = licenseUrl.trimmedOrNull() ?: return null
    val pageUrl = shareUrl.trimmedOrNull() ?: return null
    val durationSeconds = duration.toNonNegativeLong() ?: return null
    return Track(
        id = TrackId(trackId),
        title = title,
        artist = ArtistRef(ArtistId(mappedArtistId), mappedArtistName),
        album = AlbumRef(AlbumId(mappedAlbumId), albumTitle),
        artworkUrl = albumImage.trimmedOrNull() ?: image.trimmedOrNull(),
        durationMs = durationSeconds * MILLIS_PER_SECOND,
        streamUrl = audio.trimmedOrNull(),
        licenseUrl = license,
        source = ContentSource(JAMENDO_PROVIDER, pageUrl),
        releaseDate = releaseDate.toLocalDateOrNull(),
    )
}

internal fun JamendoArtistDto.toArtistOrNull(): Artist? {
    val artistId = id.toPositiveLong() ?: return null
    val artistName = name.trimmedOrNull() ?: return null
    val pageUrl = shareUrl.trimmedOrNull() ?: return null
    return Artist(
        id = ArtistId(artistId),
        name = artistName,
        imageUrl = image.trimmedOrNull(),
        source = ContentSource(JAMENDO_PROVIDER, pageUrl),
    )
}

internal fun JamendoAlbumDto.toAlbumOrNull(): Album? {
    val albumId = id.toPositiveLong() ?: return null
    val artistId = this.artistId.toPositiveLong() ?: return null
    val title = name.trimmedOrNull() ?: return null
    val artistName = this.artistName.trimmedOrNull() ?: return null
    val pageUrl = shareUrl.trimmedOrNull() ?: return null
    return Album(
        id = AlbumId(albumId),
        title = title,
        artist = ArtistRef(ArtistId(artistId), artistName),
        artworkUrl = image.trimmedOrNull(),
        releaseDate = releaseDate.toLocalDateOrNull(),
        source = ContentSource(JAMENDO_PROVIDER, pageUrl),
    )
}

private fun String.toPositiveLong(): Long? = trim().toLongOrNull()?.takeIf { it > 0 }

private fun String.toNonNegativeLong(): Long? = trim().toLongOrNull()?.takeIf { it >= 0 }

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotEmpty() }

private fun String.toLocalDateOrNull(): LocalDate? = try {
    trimmedOrNull()?.let(LocalDate::parse)
} catch (_: DateTimeParseException) {
    null
}

private const val MILLIS_PER_SECOND = 1_000L
