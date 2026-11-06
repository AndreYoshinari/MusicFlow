package br.com.musicflow.navigation

import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.LibraryRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.navigation.route.TrackDetailRoute

enum class MusicFlowDestination {
    Home,
    Search,
    Library,
    ;

    fun route(): Any = when (this) {
        Home -> HomeRoute
        Search -> SearchRoute
        Library -> LibraryRoute
    }
}

object MusicFlowChrome {
    fun selectedDestination(backStack: List<Any>): MusicFlowDestination =
        backStack.asReversed().firstNotNullOfOrNull { it.topLevelDestination() }
            ?: MusicFlowDestination.Home

    fun showsBack(route: Any?): Boolean = when (route) {
        is TrackDetailRoute,
        is ArtistDetailRoute,
        is PlaylistDetailRoute,
        is AlbumDetailRoute,
        PlayerRoute,
        -> true
        else -> false
    }

    fun showsBottomBar(route: Any?): Boolean = route !is PlayerRoute

    fun showsMiniPlayer(hasLoadedTrack: Boolean, route: Any?): Boolean =
        hasLoadedTrack && route !is PlayerRoute
}

private fun Any.topLevelDestination(): MusicFlowDestination? = when (this) {
    HomeRoute -> MusicFlowDestination.Home
    SearchRoute -> MusicFlowDestination.Search
    LibraryRoute -> MusicFlowDestination.Library
    else -> null
}
