package br.com.musicflow.feature.search

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.PageLoadException
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SearchScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resultsShowCoverDurationArtistAndOpenIds() {
        var openedTrack = 0L
        var playedTrack = 0L
        var openedArtist = 0L
        var cleared = false
        composeRule.setContent {
            MusicFlowTheme {
                ReadySearch(
                    onOpenTrack = { openedTrack = it },
                    onPlayTrack = { playedTrack = it.id.value },
                    onOpenArtist = { openedArtist = it },
                    onClear = { cleared = true },
                )
            }
        }

        composeRule.onNodeWithText("Aurora").assertIsDisplayed()
        composeRule.onNodeWithText("3:00").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Aurora").assertIsDisplayed()
        scrollTo("Estúdio Norte").performClick()
        composeRule.onNodeWithText("Aurora").performClick()
        composeRule.onNodeWithContentDescription("Limpar busca").performClick()

        assertEquals(7L, openedArtist)
        assertEquals(1L, openedTrack)
        assertEquals(1L, playedTrack)
        assertEquals(true, cleared)
    }

    @Test
    fun blankQueryShowsThePrompt() {
        composeRule.setContent {
            MusicFlowTheme {
                PagedSearch(query = "", pages = flowOf(PagingData.empty()))
            }
        }
        composeRule.onNodeWithText("O que você quer ouvir?").assertIsDisplayed()
    }

    @Test
    fun emptyResultsShowTheEmptyState() {
        composeRule.setContent {
            MusicFlowTheme {
                EmptySearch()
            }
        }
        composeRule.onNodeWithText("Nenhum resultado").assertIsDisplayed()
        composeRule.onNodeWithText("Não encontramos músicas para essa busca.").assertIsDisplayed()
    }

    @Test
    fun loadingShowsTheProgressIndicator() {
        composeRule.setContent {
            MusicFlowTheme {
                LoadingSearch()
            }
        }
        composeRule.onNodeWithContentDescription("Carregando").assertIsDisplayed()
    }

    @Test
    fun errorCanBeRetried() {
        var retriedArtists = false
        composeRule.setContent {
            MusicFlowTheme {
                FailedSearch(onRetryArtists = { retriedArtists = true })
            }
        }
        composeRule.onNodeWithText("Sem conexão. Tente de novo quando a rede voltar.").assertIsDisplayed()
        composeRule.onNodeWithText("Tentar de novo").performClick()
        assertEquals(true, retriedArtists)
    }

    private fun scrollTo(text: String) =
        composeRule.onNodeWithTag(SearchTags.results)
            .performScrollToNode(hasText(text))
            .run { composeRule.onNodeWithText(text) }

    @Composable
    private fun ReadySearch(
        onOpenTrack: (Long) -> Unit,
        onOpenArtist: (Long) -> Unit,
        onClear: () -> Unit,
        onPlayTrack: (Track) -> Unit = {},
    ) {
        PagedSearch(
            query = "aurora",
            pages = flowOf(PagingData.from(listOf(track()))),
            artists = ArtistMatches.Ready(
                listOf(SearchArtistModel(7, "Estúdio Norte", "https://example.com/artist.jpg")),
            ),
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
            onClear = onClear,
            onPlayTrack = onPlayTrack,
        )
    }

    @Composable
    private fun PagedSearch(
        query: String,
        pages: kotlinx.coroutines.flow.Flow<PagingData<Track>>,
        artists: ArtistMatches = ArtistMatches.Hidden,
        onOpenTrack: (Long) -> Unit = {},
        onOpenArtist: (Long) -> Unit = {},
        onClear: () -> Unit = {},
        onPlayTrack: (Track) -> Unit = {},
    ) {
        val tracks = pages.collectAsLazyPagingItems()
        SearchScreen(
            state = SearchUiState(query = query, artists = artists),
            tracks = tracks,
            onQueryChange = {},
            onClear = onClear,
            onRetryTracks = {},
            onRetryArtists = {},
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
            onPlayTrack = onPlayTrack,
        )
    }

    @Composable
    private fun EmptySearch() {
        val pages = remember {
            Pager(PagingConfig(pageSize = 10)) {
                object : PagingSource<Int, Track>() {
                    override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> =
                        LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                }
            }.flow
        }
        PagedSearch(query = "nada", pages = pages)
    }

    @Composable
    private fun LoadingSearch() {
        val pages = remember {
            Pager(PagingConfig(pageSize = 10)) {
                object : PagingSource<Int, Track>() {
                    override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
                        suspendCancellableCoroutine<Nothing> { }
                    }
                }
            }.flow
        }
        PagedSearch(query = "aurora", pages = pages)
    }

    @Composable
    private fun FailedSearch(onRetryArtists: () -> Unit) {
        val pages = remember {
            Pager(PagingConfig(pageSize = 10)) {
                object : PagingSource<Int, Track>() {
                    override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> =
                        LoadResult.Error(PageLoadException(DomainError.Offline))
                }
            }.flow
        }
        val tracks = pages.collectAsLazyPagingItems()
        SearchScreen(
            state = SearchUiState(query = "aurora"),
            tracks = tracks,
            onQueryChange = {},
            onClear = {},
            onRetryTracks = tracks::retry,
            onRetryArtists = onRetryArtists,
            onOpenTrack = {},
            onOpenArtist = {},
        )
    }
}

private fun track() = Track(
    id = TrackId(1),
    title = "Aurora",
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = "https://example.com/art.jpg",
    durationMs = 180_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    source = ContentSource("Jamendo", "https://www.jamendo.com/track/1"),
    releaseDate = null,
)
