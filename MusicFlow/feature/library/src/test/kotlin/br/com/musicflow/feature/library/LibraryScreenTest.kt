package br.com.musicflow.feature.library

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class LibraryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun favoritesShowTheTrackAndTheRemoveAction() {
        val opened = mutableListOf<Long>()
        val removed = mutableListOf<Long>()
        show(
            favorites = FavoritesUiState(
                tracks = LibraryListState.Ready(listOf(LibraryTrackModel(1, "Aurora", "Luna Park", null, "3:00"))),
            ),
            onOpenTrack = { opened += it },
            onToggleFavorite = { removed += it },
        )

        composeRule.onNodeWithText("Aurora").performClick()
        composeRule.onNodeWithContentDescription("Remover Aurora dos favoritos").performClick()

        assertEquals(listOf(1L), opened)
        assertEquals(listOf(1L), removed)
    }

    @Test
    fun favoritesShowLoading() {
        show(favorites = FavoritesUiState(tracks = LibraryListState.Loading))
        composeRule.onNodeWithContentDescription("Carregando").assertIsDisplayed()
    }

    @Test
    fun favoritesShowTheEmptyState() {
        show(favorites = FavoritesUiState(tracks = LibraryListState.Empty))
        composeRule.onNodeWithText("Nenhum favorito").assertIsDisplayed()
    }

    @Test
    fun favoriteErrorsCanBeRetried() {
        var retried = false
        show(
            favorites = FavoritesUiState(tracks = LibraryListState.Failed(DomainError.Offline)),
            onRetryFavorites = { retried = true },
        )
        composeRule.onNodeWithText("Sem conexão. Os dados salvos neste aparelho continuam disponíveis.").assertIsDisplayed()
        composeRule.onNodeWithText("Tentar de novo").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun playlistsCanBeCreatedAndRecentTracksCanBeFavorited() {
        val created = mutableListOf<String>()
        val favorited = mutableListOf<Long>()
        show(
            playlists = PlaylistsUiState(playlists = LibraryListState.Empty),
            history = HistoryUiState(
                tracks = LibraryListState.Ready(listOf(LibraryTrackModel(8, "Maré", "Luna Park", null, "2:10"))),
            ),
            onCreatePlaylist = { created += it },
            onToggleFavorite = { favorited += it },
        )

        composeRule.onNodeWithText("Playlists").performClick()
        composeRule.onNodeWithText("Nenhuma playlist").assertIsDisplayed()
        composeRule.onNodeWithText("Nova playlist").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Noite")
        composeRule.onNodeWithText("Salvar").performClick()
        assertEquals(listOf("Noite"), created)

        composeRule.onNodeWithText("Recentes").performClick()
        composeRule.onNodeWithText("Maré").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Favoritar Maré").performClick()
        assertEquals(listOf(8L), favorited)
    }

    private fun show(
        favorites: FavoritesUiState = FavoritesUiState(tracks = LibraryListState.Empty),
        playlists: PlaylistsUiState = PlaylistsUiState(playlists = LibraryListState.Empty),
        history: HistoryUiState = HistoryUiState(tracks = LibraryListState.Empty),
        onOpenTrack: (Long) -> Unit = {},
        onToggleFavorite: (Long) -> Unit = {},
        onRetryFavorites: () -> Unit = {},
        onCreatePlaylist: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            MusicFlowTheme {
                LibraryScreen(
                    favorites = favorites,
                    playlists = playlists,
                    history = history,
                    favoriteIds = emptySet(),
                    onOpenTrack = onOpenTrack,
                    onOpenPlaylist = {},
                    onToggleFavorite = onToggleFavorite,
                    onRetryFavorites = onRetryFavorites,
                    onCreatePlaylist = onCreatePlaylist,
                    onRetryPlaylists = {},
                    onRetryHistory = {},
                )
            }
        }
    }
}
