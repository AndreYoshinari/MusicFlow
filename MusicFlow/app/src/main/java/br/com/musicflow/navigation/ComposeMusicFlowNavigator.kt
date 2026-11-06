package br.com.musicflow.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.TrackDetailRoute

internal class ComposeMusicFlowNavigator(
    private val navController: NavHostController,
) : MusicFlowNavigator {
    override fun openTrack(trackId: Long) {
        navController.navigate(TrackDetailRoute(trackId)) { launchSingleTop = true }
    }

    override fun openArtist(artistId: Long) {
        navController.navigate(ArtistDetailRoute(artistId)) { launchSingleTop = true }
    }

    override fun openAlbum(albumId: Long) {
        navController.navigate(AlbumDetailRoute(albumId)) { launchSingleTop = true }
    }

    override fun openPlaylist(playlistId: Long) {
        navController.navigate(PlaylistDetailRoute(playlistId)) { launchSingleTop = true }
    }

    override fun openPlayer() {
        navController.navigate(PlayerRoute) { launchSingleTop = true }
    }

    override fun back() {
        navController.popBackStack()
    }
}

internal fun NavHostController.openTopLevel(destination: MusicFlowDestination) {
    val selected = MusicFlowChrome.selectedDestination(musicFlowBackStack())
    if (selected == destination) {
        popBackStack(destination.route(), inclusive = false)
        return
    }
    // saveState and restoreState keep each tab's back stack across switches.
    navigate(destination.route()) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
