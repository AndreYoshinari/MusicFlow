package br.com.musicflow.feature.player

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import br.com.musicflow.player.PlaybackFailure
import br.com.musicflow.player.PlaybackPhase
import br.com.musicflow.player.PlaybackPosition
import br.com.musicflow.player.PlaybackQueue
import br.com.musicflow.player.PlaybackState
import br.com.musicflow.player.PlaybackTrack
import br.com.musicflow.player.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class NowPlayingScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun controlsSendCommandsAndFollowThePlayerState() {
        var playing by mutableStateOf(true)
        var shuffle by mutableStateOf(false)
        var repeat by mutableStateOf(RepeatMode.Off)
        var favorite by mutableStateOf(false)
        val commands = mutableListOf<String>()
        composeRule.setContent {
            MusicFlowTheme {
                NowPlayingScreen(
                    track = aurora,
                    state = PlaybackState(
                        phase = PlaybackPhase.Ready,
                        isPlaying = playing,
                        repeatMode = repeat,
                        shuffleEnabled = shuffle,
                    ),
                    queue = PlaybackQueue(listOf(aurora, mare), currentIndex = 0),
                    position = MutableStateFlow(PlaybackPosition(30_000, 180_000, 60_000)),
                    favorite = favorite,
                    onToggleFavorite = { favorite = !favorite },
                    onPlayPause = { playing = !playing },
                    onPrevious = { commands += "previous" },
                    onNext = { commands += "next" },
                    onSeek = {},
                    onShuffle = { shuffle = !shuffle },
                    onRepeat = { repeat = repeat.cycled() },
                    onPlayFromQueue = { commands += "queue-$it" },
                )
            }
        }

        composeRule.onNodeWithText("Aurora").assertIsDisplayed()
        composeRule.onNodeWithText("Luna Park").assertIsDisplayed()
        composeRule.onNodeWithText("0:30").assertIsDisplayed()
        composeRule.onNodeWithText("3:00").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Pausar").performClick()
        composeRule.onNodeWithContentDescription("Reproduzir").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Reproduzir").performClick()
        composeRule.onNodeWithContentDescription("Pausar").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Música anterior").performClick()
        composeRule.onNodeWithContentDescription("Próxima música").performClick()
        composeRule.onNodeWithContentDescription("Ativar ordem aleatória").performClick()
        composeRule.onNodeWithContentDescription("Desativar ordem aleatória").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Ativar repetição").performClick()
        composeRule.onNodeWithContentDescription("Repetir a fila").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Favoritar Aurora").performClick()
        composeRule.onNodeWithContentDescription("Remover Aurora dos favoritos").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Fila de reprodução").performClick()
        composeRule.onNodeWithText("Fila").assertIsDisplayed()
        composeRule.onNodeWithText("Maré").performClick()

        assertEquals(listOf("previous", "next", "queue-1"), commands)
    }

    @Test
    fun emptyPlayerExplainsThatNothingIsPlaying() {
        composeRule.setContent {
            MusicFlowTheme {
                NowPlayingScreen(
                    track = null,
                    state = PlaybackState(),
                    queue = PlaybackQueue(),
                    position = MutableStateFlow(PlaybackPosition()),
                    favorite = false,
                    onToggleFavorite = {},
                    onPlayPause = {},
                    onPrevious = {},
                    onNext = {},
                    onSeek = {},
                    onShuffle = {},
                    onRepeat = {},
                    onPlayFromQueue = {},
                )
            }
        }
        composeRule.onNodeWithText("Nada tocando").assertIsDisplayed()
    }

    @Test
    fun playbackAndFavoriteErrorsStayVisible() {
        composeRule.setContent {
            MusicFlowTheme {
                NowPlayingScreen(
                    track = aurora,
                    state = PlaybackState(failure = PlaybackFailure.Connection),
                    queue = PlaybackQueue(listOf(aurora), 0),
                    position = MutableStateFlow(PlaybackPosition()),
                    favorite = false,
                    favoriteError = br.com.musicflow.common.domain.DomainError.NotFound,
                    onToggleFavorite = {},
                    onPlayPause = {},
                    onPrevious = {},
                    onNext = {},
                    onSeek = {},
                    onShuffle = {},
                    onRepeat = {},
                    onPlayFromQueue = {},
                )
            }
        }
        composeRule.onNodeWithText("Sem conexão para reproduzir esta música.").assertIsDisplayed()
        composeRule.onNodeWithText("Esta música não está salva no aparelho e não pôde ser favoritada.").assertIsDisplayed()
    }

    private val aurora = PlaybackTrack(
        id = TrackId(1),
        title = "Aurora",
        artistName = "Luna Park",
        artworkUrl = null,
        streamUrl = "https://example.com/aurora.mp3",
    )
    private val mare = PlaybackTrack(
        id = TrackId(2),
        title = "Maré",
        artistName = "Luna Park",
        artworkUrl = null,
        streamUrl = "https://example.com/mare.mp3",
    )
}
