package br.com.musicflow.feature.library

import androidx.compose.runtime.Immutable
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.Track

@Immutable
internal data class LibraryTrackModel(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val duration: String,
)

@Immutable
internal data class PlaylistSummaryModel(
    val id: Long,
    val name: String,
    val trackCount: Int,
)

@Immutable
internal sealed interface LibraryListState<out T> {
    data object Loading : LibraryListState<Nothing>

    data object Empty : LibraryListState<Nothing>

    data class Ready<T>(val items: List<T>) : LibraryListState<T>

    data class Failed(val error: DomainError) : LibraryListState<Nothing>
}

@Immutable
internal data class FavoritesUiState(
    val tracks: LibraryListState<LibraryTrackModel> = LibraryListState.Loading,
    val actionError: DomainError? = null,
)

@Immutable
internal data class PlaylistsUiState(
    val playlists: LibraryListState<PlaylistSummaryModel> = LibraryListState.Loading,
    val actionError: DomainError? = null,
    val savedRevision: Int = 0,
)

@Immutable
internal data class HistoryUiState(
    val tracks: LibraryListState<LibraryTrackModel> = LibraryListState.Loading,
)

internal fun Track.toLibraryTrack(): LibraryTrackModel = LibraryTrackModel(
    id = id.value,
    title = title,
    artist = artist.name,
    artworkUrl = artworkUrl,
    duration = formatTrackDuration(durationMs),
)

internal fun Playlist.toSummary(): PlaylistSummaryModel = PlaylistSummaryModel(
    id = id.value,
    name = name,
    trackCount = trackIds.size,
)

internal fun <T, R> DomainResult<List<T>>.toListState(map: (T) -> R): LibraryListState<R> = when (this) {
    is DomainResult.Failure -> LibraryListState.Failed(error)
    is DomainResult.Success -> if (value.isEmpty()) LibraryListState.Empty else LibraryListState.Ready(value.map(map))
}

internal fun formatTrackDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    val minuteText = if (hours > 0) minutes.toString().padStart(2, '0') else minutes.toString()
    val secondText = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:$minuteText:$secondText" else "$minuteText:$secondText"
}
