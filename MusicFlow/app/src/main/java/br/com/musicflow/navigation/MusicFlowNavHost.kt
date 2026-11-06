package br.com.musicflow.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import br.com.musicflow.R
import br.com.musicflow.common.domain.Track
import br.com.musicflow.feature.home.HomeRoute as HomeScreen
import br.com.musicflow.feature.library.LibraryRoute as LibraryScreen
import br.com.musicflow.feature.player.NowPlayingRoute
import br.com.musicflow.feature.playlist.PlaylistDetailRoute as PlaylistScreen
import br.com.musicflow.feature.search.SearchRoute as SearchScreen
import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.LibraryRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.navigation.route.TrackDetailRoute

internal object MusicFlowTags {
    const val miniPlayer = "mini_player"
    const val home = "destination_home"
    const val search = "destination_search"
    const val library = "destination_library"
    const val playlist = "destination_playlist"
    const val artist = "destination_artist"
    const val track = "destination_track"
    const val album = "destination_album"
    const val player = "destination_player"
}

@Composable
internal fun MusicFlowNavHost(
    navController: NavHostController,
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onPlayTracks: (List<Track>) -> Unit,
    onPlayTrack: (Track) -> Unit = {},
    onPlayHomeTrack: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    onPlaylistDeleted: () -> Unit,
    playback: MusicPlayback? = null,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onOpenAlbum = onOpenAlbum,
                onOpenPlaylist = onOpenPlaylist,
                onPlayTrack = onPlayHomeTrack,
            )
        }
        composable<SearchRoute> {
            SearchScreen(
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onPlayTrack = onPlayTrack,
            )
        }
        composable<LibraryRoute> {
            LibraryScreen(
                onOpenTrack = onOpenTrack,
                onOpenPlaylist = onOpenPlaylist,
            )
        }
        composable<PlaylistDetailRoute> { entry ->
            PlaylistScreen(
                playlistId = entry.toRoute<PlaylistDetailRoute>().playlistId,
                onOpenTrack = onOpenTrack,
                onPlay = onPlayTracks,
                onDeleted = onPlaylistDeleted,
            )
        }
        composable<ArtistDetailRoute> { entry ->
            DetailPane(
                tag = MusicFlowTags.artist,
                labelRes = R.string.artist_details_label,
                id = entry.toRoute<ArtistDetailRoute>().artistId,
            )
        }
        composable<TrackDetailRoute> { entry ->
            DetailPane(
                tag = MusicFlowTags.track,
                labelRes = R.string.track_details_label,
                id = entry.toRoute<TrackDetailRoute>().trackId,
            )
        }
        composable<PlayerRoute>(
            enterTransition = { slideInVertically(tween(380)) { it } + fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(160)) },
            popExitTransition = { slideOutVertically(tween(320)) { it } + fadeOut(tween(180)) },
        ) {
            val active = playback
            if (active == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(MusicFlowTags.player),
                )
            } else {
                NowPlayingRoute(playback = active)
            }
        }
        composable<AlbumDetailRoute> { entry ->
            DetailPane(
                tag = MusicFlowTags.album,
                labelRes = R.string.album_details_label,
                id = entry.toRoute<AlbumDetailRoute>().albumId,
            )
        }
    }
}

@Composable
private fun DetailPane(tag: String, labelRes: Int, id: Long) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(labelRes, id),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
