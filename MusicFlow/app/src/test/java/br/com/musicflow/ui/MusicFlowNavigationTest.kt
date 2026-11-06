package br.com.musicflow.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import br.com.musicflow.navigation.ComposeMusicFlowNavigator
import br.com.musicflow.navigation.MusicFlowTags
import br.com.musicflow.navigation.musicFlowBackStack
import br.com.musicflow.navigation.route.AlbumDetailRoute
import br.com.musicflow.navigation.route.ArtistDetailRoute
import br.com.musicflow.navigation.route.HomeRoute
import br.com.musicflow.navigation.route.PlayerRoute
import br.com.musicflow.navigation.route.PlaylistDetailRoute
import br.com.musicflow.navigation.route.SearchRoute
import br.com.musicflow.navigation.route.TrackDetailRoute
import br.com.musicflow.player.LoadedTrack
import br.com.musicflow.player.PlaybackSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MusicFlowNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val session = PlaybackSession()

    @Before
    fun startHome() {
        startKoin {
            modules(
                module {
                    viewModel { emptyHomeViewModel() }
                    viewModel { emptySearchViewModel() }
                    viewModel { emptyFavoritesViewModel() }
                    viewModel { emptyPlaylistsViewModel() }
                    viewModel { emptyHistoryViewModel() }
                    viewModel { (playlistId: Long) -> emptyPlaylistDetailsViewModel(playlistId) }
                },
            )
        }
    }

    @After
    fun stopHome() {
        stopKoin()
    }

    @Test
    fun tabsKeepTheirBackStackAndDetailsUseIds() {
        val navController = showShell()
        val navigator = ComposeMusicFlowNavigator(navController)

        composeRule.runOnIdle { navigator.openTrack(15) }
        composeRule.onNodeWithText("Faixa 15").assertIsDisplayed()

        composeRule.onNode(clickableText("Busca")).performClick()
        composeRule.onNodeWithTag(MusicFlowTags.search).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag(MusicFlowTags.track).fetchSemanticsNodes().isEmpty())

        composeRule.onNode(clickableText("Início")).performClick()
        composeRule.onNodeWithText("Faixa 15").assertIsDisplayed()
        assertEquals(TrackDetailRoute(15), navController.musicFlowBackStack().last())

        composeRule.onNode(clickableText("Início")).performClick()
        composeRule.onNodeWithTag(MusicFlowTags.home).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag(MusicFlowTags.track).fetchSemanticsNodes().isEmpty())

        composeRule.runOnIdle {
            navigator.openArtist(7)
            navigator.openPlaylist(3)
        }
        composeRule.onNodeWithText("Playlist 3").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        composeRule.onNodeWithText("Artista 7").assertIsDisplayed()
        assertEquals(ArtistDetailRoute(7), navController.musicFlowBackStack().last())

        composeRule.runOnIdle { navigator.back() }
        composeRule.onNodeWithTag(MusicFlowTags.home).assertIsDisplayed()
        assertEquals(HomeRoute, navController.musicFlowBackStack().last())

        composeRule.runOnIdle { navigator.openAlbum(6) }
        composeRule.onNodeWithText("Álbum 6").assertIsDisplayed()
        assertEquals(AlbumDetailRoute(6), navController.musicFlowBackStack().last())
    }

    @Test
    fun miniPlayerStaysWhileNavigationChanges() {
        val navController = showShell()
        val navigator = ComposeMusicFlowNavigator(navController)
        session.load(LoadedTrack(TrackId(4), "Aurora", "Luna Park"))
        session.togglePlayPause()

        composeRule.onNodeWithTag(MusicFlowTags.miniPlayer).assertIsDisplayed()
        composeRule.onNodeWithText("Aurora").assertIsDisplayed()
        composeRule.onNode(clickableText("Biblioteca")).performClick()
        composeRule.onNodeWithTag(MusicFlowTags.library).assertIsDisplayed()
        composeRule.onNodeWithTag(MusicFlowTags.miniPlayer).assertIsDisplayed()
        assertTrue(session.snapshot.value!!.isPlaying)

        composeRule.runOnIdle { navigator.openTrack(4) }
        composeRule.onNodeWithText("Faixa 4").assertIsDisplayed()
        val entryId = navController.currentBackStackEntry?.id
        composeRule.onNodeWithContentDescription("Pausar").performClick()
        composeRule.onNodeWithTag(MusicFlowTags.miniPlayer).performClick()

        assertFalse(session.snapshot.value!!.isPlaying)
        assertEquals(TrackId(4), session.snapshot.value?.track?.id)
        assertEquals(PlayerRoute, navController.musicFlowBackStack().last())
        assertEquals(TrackDetailRoute(4), navController.musicFlowBackStack()[navController.musicFlowBackStack().lastIndex - 1])
        assertEquals(entryId, navController.previousBackStackEntry?.id)
    }

    @Test
    fun miniPlayerPlayPauseKeepsTheLoadedTrackWhileNavigating() {
        showShell()
        session.load(LoadedTrack(TrackId(4), "Aurora", "Luna Park"))
        session.togglePlayPause()

        composeRule.onNodeWithContentDescription("Pausar").performClick()
        composeRule.onNodeWithContentDescription("Reproduzir").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Reproduzir").performClick()
        composeRule.onNodeWithContentDescription("Pausar").assertIsDisplayed()
        composeRule.onNode(clickableText("Busca")).performClick()

        composeRule.onNodeWithTag(MusicFlowTags.miniPlayer).assertIsDisplayed()
        composeRule.onNodeWithText("Aurora").assertIsDisplayed()
        assertTrue(session.snapshot.value!!.isPlaying)
        assertEquals(TrackId(4), session.snapshot.value?.track?.id)
    }

    @Test
    fun searchTabRestoresItsOwnDetail() {
        val navController = showShell()
        val navigator = ComposeMusicFlowNavigator(navController)

        composeRule.onNode(clickableText("Busca")).performClick()
        composeRule.runOnIdle { navigator.openPlaylist(12) }
        composeRule.onNodeWithText("Playlist 12").assertIsDisplayed()

        composeRule.onNode(clickableText("Biblioteca")).performClick()
        composeRule.onNodeWithTag(MusicFlowTags.library).assertIsDisplayed()

        composeRule.onNode(clickableText("Busca")).performClick()
        composeRule.onNodeWithText("Playlist 12").assertIsDisplayed()
        val stack = navController.musicFlowBackStack()
        assertEquals(SearchRoute, stack[stack.lastIndex - 1])
        assertEquals(PlaylistDetailRoute(12), stack.last())
    }

    private fun showShell(): NavHostController {
        lateinit var navController: NavHostController
        composeRule.setContent {
            val controller = rememberNavController()
            SideEffect { navController = controller }
            MusicFlowTheme {
                MusicFlowRoot(playbackSession = session, navController = controller)
            }
        }
        composeRule.waitForIdle()
        return navController
    }
}

private fun clickableText(text: String): SemanticsMatcher = hasText(text) and hasClickAction()
