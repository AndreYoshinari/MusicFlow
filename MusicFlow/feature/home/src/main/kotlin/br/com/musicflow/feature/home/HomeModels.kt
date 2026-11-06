package br.com.musicflow.feature.home

import androidx.compose.runtime.Immutable
import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.Track

@Immutable
internal data class TrackCardModel(
    val id: Long,
    val title: String,
    val subtitle: String,
    val artworkUrl: String?,
    val streamUrl: String? = null,
)

@Immutable
internal data class ArtistCardModel(
    val id: Long,
    val name: String,
    val imageUrl: String?,
)

@Immutable
internal data class AlbumCardModel(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
)

@Immutable
internal data class PlaylistCardModel(
    val id: Long,
    val name: String,
    val trackCount: Int,
)

@Immutable
internal sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>

    data object Empty : SectionState<Nothing>

    data class Ready<T>(val items: List<T>) : SectionState<T>

    data class Failed(val error: DomainError) : SectionState<Nothing>
}

@Immutable
internal data class HomeUiState(
    val greeting: GreetingPeriod,
    val artists: SectionState<ArtistCardModel> = SectionState.Loading,
    val albums: SectionState<AlbumCardModel> = SectionState.Loading,
    val recent: SectionState<TrackCardModel> = SectionState.Loading,
    val playlists: SectionState<PlaylistCardModel> = SectionState.Loading,
)

internal fun Track.toCard(): TrackCardModel = TrackCardModel(
    id = id.value,
    title = title,
    subtitle = artist.name,
    artworkUrl = artworkUrl,
    streamUrl = streamUrl,
)

internal fun Artist.toCard(): ArtistCardModel = ArtistCardModel(
    id = id.value,
    name = name,
    imageUrl = imageUrl,
)

internal fun Album.toCard(): AlbumCardModel = AlbumCardModel(
    id = id.value,
    title = title,
    artist = artist.name,
    artworkUrl = artworkUrl,
)

internal fun Playlist.toCard(): PlaylistCardModel = PlaylistCardModel(
    id = id.value,
    name = name,
    trackCount = trackIds.size,
)

internal fun <T> DomainResult<List<T>>.toSection(): SectionState<T> = when (this) {
    is DomainResult.Failure -> SectionState.Failed(error)
    is DomainResult.Success -> if (value.isEmpty()) SectionState.Empty else SectionState.Ready(value)
}
