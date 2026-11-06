package br.com.musicflow.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.LibraryRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.navigation.route.TrackDetailRoute

internal fun NavBackStackEntry.musicFlowRoute(): Any? {
    val destination = destination
    return when {
        destination.hasRoute<HomeRoute>() -> HomeRoute
        destination.hasRoute<SearchRoute>() -> SearchRoute
        destination.hasRoute<LibraryRoute>() -> LibraryRoute
        destination.hasRoute<TrackDetailRoute>() -> toRoute<TrackDetailRoute>()
        destination.hasRoute<ArtistDetailRoute>() -> toRoute<ArtistDetailRoute>()
        destination.hasRoute<PlaylistDetailRoute>() -> toRoute<PlaylistDetailRoute>()
        destination.hasRoute<AlbumDetailRoute>() -> toRoute<AlbumDetailRoute>()
        destination.hasRoute<PlayerRoute>() -> PlayerRoute
        else -> null
    }
}

internal fun NavHostController.musicFlowBackStack(): List<Any> =
    currentBackStack.value.mapNotNull { it.musicFlowRoute() }
