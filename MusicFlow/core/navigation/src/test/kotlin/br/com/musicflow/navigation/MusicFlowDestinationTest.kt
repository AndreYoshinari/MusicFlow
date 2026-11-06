package br.com.musicflow.navigation

import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.LibraryRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.navigation.route.TrackDetailRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicFlowDestinationTest {
    @Test
    fun topLevelDestinationsUseTheirOwnRoutes() {
        assertEquals(HomeRoute, MusicFlowDestination.Home.route())
        assertEquals(SearchRoute, MusicFlowDestination.Search.route())
        assertEquals(LibraryRoute, MusicFlowDestination.Library.route())
    }

    @Test
    fun selectedTabFollowsTheLastTopLevelRoute() {
        assertEquals(
            MusicFlowDestination.Home,
            MusicFlowChrome.selectedDestination(listOf(HomeRoute, TrackDetailRoute(4))),
        )
        assertEquals(
            MusicFlowDestination.Library,
            MusicFlowChrome.selectedDestination(listOf(HomeRoute, LibraryRoute, PlaylistDetailRoute(2))),
        )
        assertEquals(MusicFlowDestination.Home, MusicFlowChrome.selectedDestination(emptyList()))
    }

    @Test
    fun detailsUseTheBackButtonAndKeepTheShell() {
        assertTrue(MusicFlowChrome.showsBack(TrackDetailRoute(1)))
        assertTrue(MusicFlowChrome.showsBack(ArtistDetailRoute(2)))
        assertTrue(MusicFlowChrome.showsBack(PlaylistDetailRoute(3)))
        assertTrue(MusicFlowChrome.showsBack(PlayerRoute))
        assertFalse(MusicFlowChrome.showsBack(SearchRoute))
        assertTrue(MusicFlowChrome.showsBottomBar(LibraryRoute))
        assertTrue(MusicFlowChrome.showsBottomBar(TrackDetailRoute(1)))
    }

    @Test
    fun miniPlayerFollowsTheLoadedTrackAndStaysOffTheFullPlayer() {
        assertFalse(MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = false, route = HomeRoute))
        assertTrue(MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = true, route = SearchRoute))
        assertTrue(MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = true, route = TrackDetailRoute(8)))
        assertFalse(MusicFlowChrome.showsMiniPlayer(hasLoadedTrack = true, route = PlayerRoute))
        assertFalse(MusicFlowChrome.showsBottomBar(PlayerRoute))
    }
}
