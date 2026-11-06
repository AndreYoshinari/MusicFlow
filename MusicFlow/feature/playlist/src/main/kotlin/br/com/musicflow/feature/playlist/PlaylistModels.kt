package br.com.musicflow.feature.playlist

import androidx.compose.runtime.Immutable
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.Track

@Immutable
internal data class PlaylistTrackModel(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val duration: String,
)

@Immutable
internal data class PlaylistContent(
    val id: Long,
    val name: String,
    val tracks: List<PlaylistTrackModel>,
)

@Immutable
internal sealed interface PlaylistContentState {
    data object Loading : PlaylistContentState

    data class Ready(val playlist: PlaylistContent) : PlaylistContentState

    data class Failed(val error: DomainError) : PlaylistContentState
}

@Immutable
internal sealed interface PlaylistNotice {
    data class Failed(val error: DomainError) : PlaylistNotice

    data object EmptyPlayback : PlaylistNotice
}

@Immutable
internal data class PlaylistDetailsUiState(
    val content: PlaylistContentState = PlaylistContentState.Loading,
    val candidates: List<PlaylistTrackModel> = emptyList(),
    val notice: PlaylistNotice? = null,
    val editRevision: Int = 0,
    val deleted: Boolean = false,
)

internal data class PlaybackRequest(
    val revision: Int = 0,
    val tracks: List<Track> = emptyList(),
)

internal fun Track.toPlaylistTrack(): PlaylistTrackModel = PlaylistTrackModel(
    id = id.value,
    title = title,
    artist = artist.name,
    artworkUrl = artworkUrl,
    duration = formatTrackDuration(durationMs),
)

internal fun formatTrackDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    val minuteText = if (hours > 0) minutes.toString().padStart(2, '0') else minutes.toString()
    val secondText = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:$minuteText:$secondText" else "$minuteText:$secondText"
}
