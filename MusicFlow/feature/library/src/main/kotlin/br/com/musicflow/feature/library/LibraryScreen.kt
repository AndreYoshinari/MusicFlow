package br.com.musicflow.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.designsystem.component.MusicFlowAlbumArtwork
import br.com.musicflow.designsystem.component.MusicFlowButton
import br.com.musicflow.designsystem.component.MusicFlowEmptyState
import br.com.musicflow.designsystem.component.MusicFlowErrorState
import br.com.musicflow.designsystem.component.MusicFlowIconButton
import br.com.musicflow.designsystem.component.MusicFlowLoading
import br.com.musicflow.designsystem.component.MusicFlowTextField
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import org.koin.androidx.compose.koinViewModel

internal object LibraryTags {
    const val screen = "destination_library"
}

@Composable
fun LibraryRoute(
    onOpenTrack: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    favoritesViewModel: FavoritesViewModel = koinViewModel(),
    playlistsViewModel: PlaylistsViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
) {
    val favorites by favoritesViewModel.state.collectAsStateWithLifecycle()
    val playlists by playlistsViewModel.state.collectAsStateWithLifecycle()
    val history by historyViewModel.state.collectAsStateWithLifecycle()
    val favoriteIds = (favorites.tracks as? LibraryListState.Ready)?.items?.map { it.id }?.toSet().orEmpty()
    LibraryScreen(
        favorites = favorites,
        playlists = playlists,
        history = history,
        favoriteIds = favoriteIds,
        onOpenTrack = onOpenTrack,
        onOpenPlaylist = onOpenPlaylist,
        onToggleFavorite = favoritesViewModel::toggle,
        onRetryFavorites = favoritesViewModel::retry,
        onCreatePlaylist = playlistsViewModel::create,
        onRetryPlaylists = playlistsViewModel::refresh,
        onRetryHistory = historyViewModel::refresh,
    )
}

@Composable
internal fun LibraryScreen(
    favorites: FavoritesUiState,
    playlists: PlaylistsUiState,
    history: HistoryUiState,
    favoriteIds: Set<Long>,
    onOpenTrack: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRetryFavorites: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRetryPlaylists: () -> Unit,
    onRetryHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(LibraryTags.screen),
    ) {
        PrimaryTabRow(selectedTabIndex = selected) {
            LibraryTab(selected = selected == 0, label = stringResource(R.string.library_tab_favorites)) {
                selected = 0
            }
            LibraryTab(selected = selected == 1, label = stringResource(R.string.library_tab_playlists)) {
                selected = 1
            }
            LibraryTab(selected = selected == 2, label = stringResource(R.string.library_tab_recent)) {
                selected = 2
            }
        }
        when (selected) {
            0 -> FavoritesPane(
                state = favorites,
                onOpenTrack = onOpenTrack,
                onToggleFavorite = onToggleFavorite,
                onRetry = onRetryFavorites,
            )
            1 -> PlaylistsPane(
                state = playlists,
                onOpenPlaylist = onOpenPlaylist,
                onCreate = onCreatePlaylist,
                onRetry = onRetryPlaylists,
            )
            else -> HistoryPane(
                state = history,
                favoriteIds = favoriteIds,
                onOpenTrack = onOpenTrack,
                onToggleFavorite = onToggleFavorite,
                onRetry = onRetryHistory,
            )
        }
    }
}

@Composable
private fun LibraryTab(selected: Boolean, label: String, onClick: () -> Unit) {
    Tab(selected = selected, onClick = onClick, text = { Text(label) })
}

@Composable
private fun FavoritesPane(
    state: FavoritesUiState,
    onOpenTrack: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        state.actionError?.let { error ->
            Text(
                text = error.libraryActionMessage(),
                modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.small),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        TrackSection(
            section = state.tracks,
            emptyTitle = stringResource(R.string.library_empty_favorites_title),
            emptyMessage = stringResource(R.string.library_empty_favorites_message),
            onRetry = onRetry,
            onOpenTrack = onOpenTrack,
            trailing = { track ->
                MusicFlowIconButton(
                    icon = Icons.Outlined.Favorite,
                    contentDescription = stringResource(R.string.library_remove_favorite, track.title),
                    onClick = { onToggleFavorite(track.id) },
                )
            },
        )
    }
}

@Composable
private fun HistoryPane(
    state: HistoryUiState,
    favoriteIds: Set<Long>,
    onOpenTrack: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    TrackSection(
        section = state.tracks,
        emptyTitle = stringResource(R.string.library_empty_recent_title),
        emptyMessage = stringResource(R.string.library_empty_recent_message),
        onRetry = onRetry,
        onOpenTrack = onOpenTrack,
        trailing = { track ->
            val saved = track.id in favoriteIds
            MusicFlowIconButton(
                icon = if (saved) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (saved) R.string.library_remove_favorite else R.string.library_add_favorite,
                    track.title,
                ),
                onClick = { onToggleFavorite(track.id) },
            )
        },
    )
}

@Composable
private fun TrackSection(
    section: LibraryListState<LibraryTrackModel>,
    emptyTitle: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    onOpenTrack: (Long) -> Unit,
    trailing: @Composable (LibraryTrackModel) -> Unit,
) {
    when (section) {
        LibraryListState.Loading -> MusicFlowLoading(modifier = Modifier.fillMaxHeight())
        LibraryListState.Empty -> MusicFlowEmptyState(
            title = emptyTitle,
            message = emptyMessage,
            modifier = Modifier.fillMaxHeight(),
        )
        is LibraryListState.Failed -> MusicFlowErrorState(
            title = stringResource(R.string.library_error_title),
            message = section.error.libraryMessage(),
            onRetry = onRetry,
            modifier = Modifier.fillMaxHeight(),
        )
        is LibraryListState.Ready -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = MusicFlowSpacing.small),
        ) {
            items(section.items, key = { it.id }) { track ->
                LibraryTrackRow(track = track, onOpenTrack = onOpenTrack, trailing = trailing)
            }
        }
    }
}

@Composable
private fun PlaylistsPane(
    state: PlaylistsUiState,
    onOpenPlaylist: (Long) -> Unit,
    onCreate: (String) -> Unit,
    onRetry: () -> Unit,
) {
    var creating by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var baseline by rememberSaveable { mutableIntStateOf(state.savedRevision) }
    LaunchedEffect(state.savedRevision) {
        if (creating && state.savedRevision != baseline) {
            creating = false
            name = ""
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        when (val section = state.playlists) {
            LibraryListState.Loading -> MusicFlowLoading(modifier = Modifier.weight(1f).fillMaxHeight())
            LibraryListState.Empty -> MusicFlowEmptyState(
                title = stringResource(R.string.library_empty_playlists_title),
                message = stringResource(R.string.library_empty_playlists_message),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                actionLabel = stringResource(R.string.library_create_playlist),
                onAction = {
                    baseline = state.savedRevision
                    creating = true
                },
            )
            is LibraryListState.Failed -> MusicFlowErrorState(
                title = stringResource(R.string.library_error_title),
                message = section.error.libraryMessage(),
                onRetry = onRetry,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            is LibraryListState.Ready -> {
                MusicFlowButton(
                    text = stringResource(R.string.library_create_playlist),
                    onClick = {
                        baseline = state.savedRevision
                        creating = true
                    },
                    modifier = Modifier
                        .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.small)
                        .fillMaxWidth(),
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = MusicFlowSpacing.medium),
                ) {
                    items(section.items, key = { it.id }) { playlist ->
                        PlaylistRow(playlist = playlist, onOpenPlaylist = onOpenPlaylist)
                    }
                }
            }
        }
    }
    if (creating) {
        PlaylistNameDialog(
            title = stringResource(R.string.library_create_playlist),
            name = name,
            error = if (state.actionError == DomainError.InvalidName) {
                stringResource(R.string.library_invalid_name)
            } else {
                null
            },
            onNameChange = { name = it },
            onConfirm = { onCreate(name) },
            onDismiss = { creating = false },
        )
    }
}

@Composable
private fun PlaylistRow(playlist: PlaylistSummaryModel, onOpenPlaylist: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPlaylist(playlist.id) }
            .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.medium),
    ) {
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pluralStringResource(R.plurals.library_track_count, playlist.trackCount, playlist.trackCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LibraryTrackRow(
    track: LibraryTrackModel,
    onOpenTrack: (Long) -> Unit,
    trailing: @Composable (LibraryTrackModel) -> Unit,
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
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = track.duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        trailing(track)
    }
}

@Composable
internal fun PlaylistNameDialog(
    title: String,
    name: String,
    error: String?,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small)) {
                MusicFlowTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.library_playlist_name),
                )
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            MusicFlowButton(text = stringResource(R.string.library_save), onClick = onConfirm)
        },
        dismissButton = {
            MusicFlowButton(text = stringResource(R.string.library_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun DomainError.libraryMessage(): String = when (this) {
    DomainError.Offline, DomainError.Timeout -> stringResource(R.string.library_error_offline)
    else -> stringResource(R.string.library_error_generic)
}

@Composable
private fun DomainError.libraryActionMessage(): String = when (this) {
    DomainError.NotFound -> stringResource(R.string.library_favorite_missing)
    DomainError.Offline, DomainError.Timeout -> stringResource(R.string.library_error_offline)
    else -> stringResource(R.string.library_error_generic)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LibraryFavoritesPreview() {
    MusicFlowTheme {
        LibraryScreen(
            favorites = FavoritesUiState(
                tracks = LibraryListState.Ready(
                    listOf(LibraryTrackModel(1, "Aurora", "Luna Park", null, "3:00")),
                ),
            ),
            playlists = PlaylistsUiState(),
            history = HistoryUiState(),
            favoriteIds = setOf(1),
            onOpenTrack = {},
            onOpenPlaylist = {},
            onToggleFavorite = {},
            onRetryFavorites = {},
            onCreatePlaylist = {},
            onRetryPlaylists = {},
            onRetryHistory = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LibraryPlaylistsEmptyPreview() {
    MusicFlowTheme {
        LibraryScreen(
            favorites = FavoritesUiState(tracks = LibraryListState.Empty),
            playlists = PlaylistsUiState(playlists = LibraryListState.Empty),
            history = HistoryUiState(tracks = LibraryListState.Empty),
            favoriteIds = emptySet(),
            onOpenTrack = {},
            onOpenPlaylist = {},
            onToggleFavorite = {},
            onRetryFavorites = {},
            onCreatePlaylist = {},
            onRetryPlaylists = {},
            onRetryHistory = {},
        )
    }
}
