package br.com.musicflow.feature.home

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun successShowsEverySectionAndOpensIds() {
        val openedTracks = mutableListOf<Long>()
        val played = mutableListOf<String>()
        var openedArtist = 0L
        var openedAlbum = 0L
        var openedPlaylist = 0L
        composeRule.setContent {
            MusicFlowTheme {
                SampleHome(
                    state = readyState(),
                    onOpenTrack = { openedTracks += it },
                    onPlayTrack = { _, title, _, _, _ -> played += title },
                    onOpenArtist = { openedArtist = it },
                    onOpenAlbum = { openedAlbum = it },
                    onOpenPlaylist = { openedPlaylist = it },
                )
            }
        }

        composeRule.onNodeWithText("Bom dia").assertIsDisplayed()
        composeRule.onNodeWithText("Músicas populares").assertIsDisplayed()
        scrollTo("Aurora").performClick()
        scrollTo("Estúdio Norte").performClick()
        scrollTo("Noite", substring = false).performClick()
        scrollTo("Maré").performClick()
        scrollTo("Noite violeta").performClick()
        scrollTo("18 faixas").assertIsDisplayed()

        assertEquals(listOf(1L, 8L), openedTracks)
        assertEquals(listOf("Aurora", "Maré"), played)
        assertEquals(7L, openedArtist)
        assertEquals(3L, openedAlbum)
        assertEquals(4L, openedPlaylist)
    }

    @Test
    fun emptyAndErrorSectionsStayIndependent() {
        var retried = false
        composeRule.setContent {
            MusicFlowTheme {
                SampleHome(
                    state = HomeUiState(
                        greeting = GreetingPeriod.Night,
                        artists = SectionState.Failed(DomainError.Offline),
                        albums = SectionState.Empty,
                        recent = SectionState.Empty,
                        playlists = SectionState.Loading,
                    ),
                    onRetryArtists = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText("Boa noite").assertIsDisplayed()
        scrollTo("Sem conexão. Tente de novo quando a rede voltar.").assertIsDisplayed()
        scrollTo("Nenhum álbum").assertIsDisplayed()
        scrollTo("Nada por aqui ainda").assertIsDisplayed()
        scrollTo("Suas playlists")
        composeRule.onNodeWithContentDescription("Carregando").assertIsDisplayed()
        scrollTo("Tentar de novo").performClick()
        assertEquals(true, retried)
    }

    private fun scrollTo(text: String, substring: Boolean = true) =
        composeRule.onNodeWithTag(HomeTags.screen)
            .performScrollToNode(hasText(text, substring = substring))
            .run { composeRule.onNodeWithText(text, substring = substring) }

    @Composable
    private fun SampleHome(
        state: HomeUiState,
        onOpenTrack: (Long) -> Unit = {},
        onOpenArtist: (Long) -> Unit = {},
        onOpenAlbum: (Long) -> Unit = {},
        onOpenPlaylist: (Long) -> Unit = {},
        onRetryArtists: () -> Unit = {},
        onPlayTrack: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    ) {
        val popular = flowOf(PagingData.from(listOf(popularTrack()))).collectAsLazyPagingItems()
        HomeScreen(
            state = state,
            popularTracks = popular,
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
            onOpenAlbum = onOpenAlbum,
            onOpenPlaylist = onOpenPlaylist,
            onRetryArtists = onRetryArtists,
            onRetryAlbums = {},
            onRetryRecent = {},
            onRetryPlaylists = {},
            onPlayTrack = onPlayTrack,
        )
    }
}

private fun readyState() = HomeUiState(
    greeting = GreetingPeriod.Morning,
    artists = SectionState.Ready(listOf(ArtistCardModel(7, "Estúdio Norte", "https://example.com/artist.jpg"))),
    albums = SectionState.Ready(listOf(AlbumCardModel(3, "Noite", "Luna Park", "https://example.com/album.jpg"))),
    recent = SectionState.Ready(listOf(TrackCardModel(8, "Maré", "Luna Park", "https://example.com/mare.jpg"))),
    playlists = SectionState.Ready(listOf(PlaylistCardModel(4, "Noite violeta", 18))),
)

private fun popularTrack() = Track(
    id = TrackId(1),
    title = "Aurora",
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = AlbumRef(br.com.musicflow.common.domain.AlbumId(3), "Noite"),
    artworkUrl = "https://example.com/art.jpg",
    durationMs = 180_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    source = ContentSource("Jamendo", "https://www.jamendo.com/track/1"),
    releaseDate = null,
)
