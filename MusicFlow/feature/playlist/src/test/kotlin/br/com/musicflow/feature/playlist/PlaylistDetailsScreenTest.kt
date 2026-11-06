package br.com.musicflow.feature.playlist

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PlaylistDetailsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readyPlaylistPlaysOpensAndRemovesATrack() {
        val opened = mutableListOf<Long>()
        val removed = mutableListOf<Long>()
        var played = false
        show(
            state = ready(listOf(PlaylistTrackModel(1, "Aurora", "Luna Park", null, "3:00"))),
            onOpenTrack = { opened += it },
            onPlay = { played = true },
            onRemove = { removed += it },
        )

        composeRule.onNodeWithText("Noite").assertIsDisplayed()
        composeRule.onNodeWithText("Aurora").performClick()
        composeRule.onNodeWithText("Reproduzir").performClick()
        composeRule.onNodeWithContentDescription("Remover Aurora").performClick()

        assertEquals(listOf(1L), opened)
        assertEquals(true, played)
        assertEquals(listOf(1L), removed)
    }

    @Test
    fun emptyPlaylistExplainsThatNothingCanPlay() {
        show(state = ready(emptyList(), notice = PlaylistNotice.EmptyPlayback))

        composeRule.onNodeWithText("Playlist vazia").assertIsDisplayed()
        composeRule.onNodeWithText("Adicione músicas para reproduzir esta playlist.").assertIsDisplayed()
    }

    @Test
    fun playlistShowsLoading() {
        show(state = PlaylistDetailsUiState(content = PlaylistContentState.Loading))
        composeRule.onNodeWithContentDescription("Carregando").assertIsDisplayed()
    }

    @Test
    fun playlistErrorsCanBeRetried() {
        var retried = false
        show(
            state = PlaylistDetailsUiState(content = PlaylistContentState.Failed(DomainError.Offline)),
            onRetry = { retried = true },
        )
        composeRule.onNodeWithText("Sem conexão. Esta playlist continua salva neste aparelho.").assertIsDisplayed()
        composeRule.onNodeWithText("Tentar de novo").performClick()
        assertEquals(true, retried)
    }

    private fun show(
        state: PlaylistDetailsUiState,
        onOpenTrack: (Long) -> Unit = {},
        onPlay: () -> Unit = {},
        onRemove: (Long) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            MusicFlowTheme {
                PlaylistDetailsScreen(
                    state = state,
                    onOpenTrack = onOpenTrack,
                    onPlay = onPlay,
                    onRename = {},
                    onDelete = {},
                    onAdd = {},
                    onRemove = onRemove,
                    onRetry = onRetry,
                )
            }
        }
    }

    private fun ready(tracks: List<PlaylistTrackModel>, notice: PlaylistNotice? = null) = PlaylistDetailsUiState(
        content = PlaylistContentState.Ready(
            PlaylistContent(id = 4, name = "Noite", tracks = tracks),
        ),
        notice = notice,
    )
}
