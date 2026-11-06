package br.com.musicflow.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.musicflow.R
import br.com.musicflow.common.domain.Track
import br.com.musicflow.player.LoadedTrack
import br.com.musicflow.designsystem.component.MusicFlowBottomNavigation
import br.com.musicflow.designsystem.component.MusicFlowDestination as BottomDestination
import br.com.musicflow.designsystem.component.MusicFlowMiniPlayer
import br.com.musicflow.designsystem.component.MusicFlowTopBar
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.navigation.ComposeMusicFlowNavigator
import br.com.musicflow.navigation.MusicFlowChrome
import br.com.musicflow.navigation.MusicFlowDestination
import br.com.musicflow.navigation.MusicFlowNavHost
import br.com.musicflow.navigation.MusicFlowTags
import br.com.musicflow.navigation.musicFlowRoute
import br.com.musicflow.navigation.openTopLevel
import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.LibraryRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.navigation.route.TrackDetailRoute
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.player.PlaybackFailure
import br.com.musicflow.player.PlaybackPosition
import br.com.musicflow.player.PlaybackSession
import br.com.musicflow.player.PlaybackTrack
import kotlinx.coroutines.flow.StateFlow

// The session and the mini player stay outside the NavHost, so a new destination does not dispose them.
@Composable
fun MusicFlowRoot(
    playbackSession: PlaybackSession,
    navController: NavHostController = rememberNavController(),
    onTogglePlayback: () -> Unit = playbackSession::togglePlayPause,
    playback: MusicPlayback? = null,
) {
    val navigator = remember(navController) { ComposeMusicFlowNavigator(navController) }
    val play: (List<PlaybackTrack>) -> Unit = { tracks ->
        val active = playback
        if (active != null) {
            if (tracks.isNotEmpty()) active.play(tracks)
        } else {
            val first = tracks.firstOrNull()
            if (first != null) {
                playbackSession.play(
                    LoadedTrack(
                        id = first.id,
                        title = first.title,
                        artistName = first.artistName,
                        artworkUrl = first.artworkUrl,
                    ),
                )
            }
        }
    }
    val backStack by navController.currentBackStack.collectAsStateWithLifecycle()
    val routes = backStack.mapNotNull { it.musicFlowRoute() }
    val current = routes.lastOrNull()
    val selected = MusicFlowChrome.selectedDestination(routes)
    val showBack = MusicFlowChrome.showsBack(current)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MusicFlowTopBar(
                title = titleFor(current),
                navigationIcon = if (showBack) Icons.AutoMirrored.Filled.ArrowBack else null,
                navigationContentDescription = stringResource(R.string.navigate_back),
                onNavigationClick = if (showBack) navigator::back else null,
            )
        },
        bottomBar = {
            Column {
                val activePlayback = playback
                if (activePlayback != null) {
                    LiveMiniPlayer(
                        playback = activePlayback,
                        visible = MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = true, route = current),
                        onTogglePlayback = onTogglePlayback,
                        onOpenPlayer = navigator::openPlayer,
                    )
                } else {
                    SessionMiniPlayer(
                        session = playbackSession,
                        visible = MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = true, route = current),
                        onTogglePlayback = onTogglePlayback,
                        onOpenPlayer = navigator::openPlayer,
                    )
                }
                if (MusicFlowChrome.showsBottomBar(current)) {
                    val destinations = MusicFlowDestination.entries.map { it.bottomDestination() }
                    MusicFlowBottomNavigation(
                        destinations = destinations,
                        selectedIndex = MusicFlowDestination.entries.indexOf(selected),
                        onDestinationSelected = { index ->
                            navController.openTopLevel(MusicFlowDestination.entries[index])
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        MusicFlowNavHost(
            navController = navController,
            onOpenTrack = navigator::openTrack,
            onOpenArtist = navigator::openArtist,
            onOpenAlbum = navigator::openAlbum,
            onOpenPlaylist = navigator::openPlaylist,
            onPlayTracks = { tracks -> play(tracks.map { it.toPlaybackTrack() }) },
            onPlayTrack = { track -> play(listOf(track.toPlaybackTrack())) },
            onPlayHomeTrack = { id, title, artist, artworkUrl, streamUrl ->
                play(
                    listOf(
                        PlaybackTrack(
                            id = TrackId(id),
                            title = title,
                            artistName = artist,
                            artworkUrl = artworkUrl,
                            streamUrl = streamUrl.orEmpty(),
                        ),
                    ),
                )
            },
            onPlaylistDeleted = navigator::back,
            playback = playback,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun titleFor(route: Any?): String = when (route) {
    SearchRoute -> stringResource(R.string.destination_search)
    LibraryRoute -> stringResource(R.string.destination_library)
    is PlaylistDetailRoute -> stringResource(R.string.destination_playlist)
    is ArtistDetailRoute -> stringResource(R.string.destination_artist)
    is TrackDetailRoute -> stringResource(R.string.destination_track)
    is AlbumDetailRoute -> stringResource(R.string.destination_album)
    PlayerRoute -> stringResource(R.string.destination_player)
    HomeRoute, null -> stringResource(R.string.destination_home)
    else -> stringResource(R.string.app_name)
}

@Composable
private fun MusicFlowDestination.bottomDestination(): BottomDestination = when (this) {
    MusicFlowDestination.Home -> barItem(
        label = stringResource(R.string.destination_home),
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
    )
    MusicFlowDestination.Search -> barItem(
        label = stringResource(R.string.destination_search),
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search,
    )
    MusicFlowDestination.Library -> barItem(
        label = stringResource(R.string.destination_library),
        icon = Icons.Outlined.LibraryMusic,
        selectedIcon = Icons.Filled.LibraryMusic,
    )
}

@Composable
private fun LiveMiniPlayer(
    playback: MusicPlayback,
    visible: Boolean,
    onTogglePlayback: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val track by playback.currentTrack.collectAsStateWithLifecycle()
    val state by playback.playbackState.collectAsStateWithLifecycle()
    state.failure?.let { failure ->
        Text(
            text = failure.shellMessage(),
            modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    AnimatedVisibility(
        visible = visible && track != null,
        enter = expandVertically(tween(280)) + fadeIn(tween(220)),
        exit = shrinkVertically(tween(240)) + fadeOut(tween(160)),
    ) {
        val current = track ?: return@AnimatedVisibility
        MusicFlowMiniPlayer(
            title = current.title,
            artist = current.artistName,
            imageUrl = current.artworkUrl,
            isPlaying = state.isPlaying,
            onPlayPauseClick = onTogglePlayback,
            onClick = onOpenPlayer,
            modifier = Modifier.testTag(MusicFlowTags.miniPlayer),
            progressContent = { MiniPlayerProgress(playback.position) },
        )
    }
}

@Composable
private fun SessionMiniPlayer(
    session: PlaybackSession,
    visible: Boolean,
    onTogglePlayback: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val snapshot by session.snapshot.collectAsStateWithLifecycle()
    val playing = snapshot
    AnimatedVisibility(
        visible = visible && playing != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        val loaded = playing ?: return@AnimatedVisibility
        MusicFlowMiniPlayer(
            title = loaded.track.title,
            artist = loaded.track.artistName,
            imageUrl = loaded.track.artworkUrl,
            isPlaying = loaded.isPlaying,
            progress = loaded.progress,
            onPlayPauseClick = onTogglePlayback,
            onClick = onOpenPlayer,
            modifier = Modifier.testTag(MusicFlowTags.miniPlayer),
        )
    }
}

@Composable
private fun PlaybackFailure.shellMessage(): String = stringResource(
    when (this) {
        PlaybackFailure.Connection -> R.string.playback_error_offline
        PlaybackFailure.InvalidSource -> R.string.playback_error_source
        PlaybackFailure.Playback -> R.string.playback_error_generic
    },
)

@Composable
private fun MiniPlayerProgress(position: StateFlow<PlaybackPosition>) {
    val value by position.collectAsStateWithLifecycle()
    val fraction = if (value.durationMs <= 0L) 0f else value.positionMs.toFloat() / value.durationMs.toFloat()
    val animated by animateFloatAsState(targetValue = fraction.coerceIn(0f, 1f), animationSpec = tween(240), label = "miniProgress")
    val description = stringResource(br.com.musicflow.designsystem.R.string.musicflow_playback_progress)
    LinearProgressIndicator(
        progress = { animated },
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(animated, 0f..1f)
                contentDescription = description
            },
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

private fun barItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
) = BottomDestination(
    label = label,
    icon = icon,
    selectedIcon = selectedIcon,
)
