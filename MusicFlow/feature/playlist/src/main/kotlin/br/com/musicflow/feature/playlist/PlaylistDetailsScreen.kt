package br.com.musicflow.feature.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.Track
import br.com.musicflow.designsystem.component.MusicFlowAlbumArtwork
import br.com.musicflow.designsystem.component.MusicFlowButton
import br.com.musicflow.designsystem.component.MusicFlowEmptyState
import br.com.musicflow.designsystem.component.MusicFlowErrorState
import br.com.musicflow.designsystem.component.MusicFlowIconButton
import br.com.musicflow.designsystem.component.MusicFlowLoading
import br.com.musicflow.designsystem.component.MusicFlowTextField
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal object PlaylistTags {
    const val screen = "destination_playlist"
}

@Composable
fun PlaylistDetailRoute(
    playlistId: Long,
    onOpenTrack: (Long) -> Unit,
    onPlay: (List<Track>) -> Unit,
    onDeleted: () -> Unit,
    viewModel: PlaylistDetailsViewModel = koinViewModel { parametersOf(playlistId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }
    LaunchedEffect(playback.revision) {
        if (playback.revision > 0) onPlay(playback.tracks)
    }
    PlaylistDetailsScreen(
        state = state,
        onOpenTrack = onOpenTrack,
        onPlay = viewModel::play,
        onRename = viewModel::rename,
        onDelete = viewModel::delete,
        onAdd = viewModel::add,
        onRemove = viewModel::remove,
        onRetry = viewModel::refresh,
    )
}

@Composable
internal fun PlaylistDetailsScreen(
    state: PlaylistDetailsUiState,
    onOpenTrack: (Long) -> Unit,
    onPlay: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(PlaylistTags.screen),
    ) {
        when (val content = state.content) {
            PlaylistContentState.Loading -> MusicFlowLoading(modifier = Modifier.fillMaxHeight())
            is PlaylistContentState.Failed -> MusicFlowErrorState(
                title = stringResource(R.string.playlist_error_title),
                message = content.error.playlistMessage(),
                onRetry = onRetry,
                modifier = Modifier.fillMaxHeight(),
            )
            is PlaylistContentState.Ready -> PlaylistBody(
                playlist = content.playlist,
                notice = state.notice,
                candidates = state.candidates,
                editRevision = state.editRevision,
                onOpenTrack = onOpenTrack,
                onPlay = onPlay,
                onRename = onRename,
                onDelete = onDelete,
                onAdd = onAdd,
                onRemove = onRemove,
            )
        }
    }
}

@Composable
private fun PlaylistBody(
    playlist: PlaylistContent,
    notice: PlaylistNotice?,
    candidates: List<PlaylistTrackModel>,
    editRevision: Int,
    onOpenTrack: (Long) -> Unit,
    onPlay: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var renaming by rememberSaveable { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf(playlist.name) }
    var baseline by rememberSaveable { mutableIntStateOf(editRevision) }
    LaunchedEffect(editRevision) {
        if (editRevision != baseline) {
            renaming = false
            adding = false
            draft = playlist.name
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MusicFlowSpacing.large),
        verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small),
            ) {
                Text(text = playlist.name, style = MaterialTheme.typography.headlineSmall)
                notice?.let { current ->
                    Text(
                        text = current.noticeMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                MusicFlowButton(
                    text = stringResource(R.string.playlist_play),
                    onClick = onPlay,
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.fillMaxWidth(),
                )
                MusicFlowButton(
                    text = stringResource(R.string.playlist_add),
                    onClick = {
                        baseline = editRevision
                        adding = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small)) {
                    MusicFlowButton(
                        text = stringResource(R.string.playlist_rename),
                        onClick = {
                            draft = playlist.name
                            baseline = editRevision
                            renaming = true
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MusicFlowButton(
                        text = stringResource(R.string.playlist_delete),
                        onClick = { confirmingDelete = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (playlist.tracks.isEmpty()) {
            item {
                MusicFlowEmptyState(
                    title = stringResource(R.string.playlist_empty_title),
                    message = stringResource(R.string.playlist_empty_message),
                )
            }
        } else {
            items(playlist.tracks, key = { it.id }) { track ->
                PlaylistTrackRow(track = track, onOpenTrack = onOpenTrack, onRemove = onRemove)
            }
        }
    }
    if (renaming) {
        NameDialog(
            name = draft,
            error = (notice as? PlaylistNotice.Failed)?.error,
            onNameChange = { draft = it },
            onConfirm = { onRename(draft) },
            onDismiss = { renaming = false },
        )
    }
    if (adding) {
        AddDialog(
            candidates = candidates,
            notice = notice,
            onAdd = onAdd,
            onDismiss = { adding = false },
        )
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.playlist_delete_title)) },
            text = { Text(stringResource(R.string.playlist_delete_message, playlist.name)) },
            confirmButton = {
                MusicFlowButton(text = stringResource(R.string.playlist_delete), onClick = onDelete)
            },
            dismissButton = {
                MusicFlowButton(
                    text = stringResource(R.string.playlist_cancel),
                    onClick = { confirmingDelete = false },
                )
            },
        )
    }
}

@Composable
private fun PlaylistTrackRow(
    track: PlaylistTrackModel,
    onOpenTrack: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTrack(track.id) }
            .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MusicFlowAlbumArtwork(
            imageUrl = track.artworkUrl,
            contentDescription = track.title,
            modifier = Modifier.size(56.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${track.artist} · ${track.duration}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        MusicFlowIconButton(
            icon = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.playlist_remove_track, track.title),
            onClick = { onRemove(track.id) },
        )
    }
}

@Composable
private fun NameDialog(
    name: String,
    error: DomainError?,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.playlist_rename)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small)) {
                MusicFlowTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.playlist_name),
                )
                if (error == DomainError.InvalidName) {
                    Text(
                        text = stringResource(R.string.playlist_invalid_name),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            MusicFlowButton(text = stringResource(R.string.playlist_save), onClick = onConfirm)
        },
        dismissButton = {
            MusicFlowButton(text = stringResource(R.string.playlist_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun AddDialog(
    candidates: List<PlaylistTrackModel>,
    notice: PlaylistNotice?,
    onAdd: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.playlist_add)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small),
            ) {
                if (notice is PlaylistNotice.Failed && notice.error == DomainError.DuplicateTrack) {
                    Text(
                        text = stringResource(R.string.playlist_duplicate),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (candidates.isEmpty()) {
                    Text(text = stringResource(R.string.playlist_no_candidates))
                } else {
                    candidates.forEach { track ->
                        Text(
                            text = track.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdd(track.id) }
                                .padding(vertical = MusicFlowSpacing.small),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            MusicFlowButton(text = stringResource(R.string.playlist_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun DomainError.playlistMessage(): String = when (this) {
    DomainError.Offline, DomainError.Timeout -> stringResource(R.string.playlist_error_offline)
    else -> stringResource(R.string.playlist_error_generic)
}

@Composable
private fun PlaylistNotice.noticeMessage(): String = when (this) {
    PlaylistNotice.EmptyPlayback -> stringResource(R.string.playlist_empty_playback)
    is PlaylistNotice.Failed -> when (error) {
        DomainError.DuplicateTrack -> stringResource(R.string.playlist_duplicate)
        DomainError.InvalidName -> stringResource(R.string.playlist_invalid_name)
        DomainError.Offline, DomainError.Timeout -> stringResource(R.string.playlist_error_offline)
        else -> stringResource(R.string.playlist_error_generic)
    }
}
