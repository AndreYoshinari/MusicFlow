package br.com.musicflow.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.Track
import br.com.musicflow.designsystem.component.MusicFlowAlbumArtwork
import br.com.musicflow.designsystem.component.MusicFlowArtistCard
import br.com.musicflow.designsystem.component.MusicFlowButton
import br.com.musicflow.designsystem.component.MusicFlowEmptyState
import br.com.musicflow.designsystem.component.MusicFlowErrorState
import br.com.musicflow.designsystem.component.MusicFlowLoading
import br.com.musicflow.designsystem.component.MusicFlowTextField
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import org.koin.androidx.compose.koinViewModel

internal object SearchTags {
    const val screen = "destination_search"
    const val results = "search_results"
}

@Composable
fun SearchRoute(
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onPlayTrack: (Track) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    SearchScreen(
        state = state,
        tracks = tracks,
        onQueryChange = viewModel::onQueryChange,
        onClear = viewModel::clear,
        onRetryTracks = tracks::retry,
        onRetryArtists = viewModel::retryArtists,
        onOpenTrack = onOpenTrack,
        onOpenArtist = onOpenArtist,
        onPlayTrack = onPlayTrack,
    )
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    tracks: LazyPagingItems<Track>,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRetryTracks: () -> Unit,
    onRetryArtists: () -> Unit,
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onPlayTrack: (Track) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val kind = searchResultsKind(state.query, tracks.loadState.refresh, tracks.itemCount)
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(SearchTags.screen),
    ) {
        SearchField(query = state.query, onQueryChange = onQueryChange, onClear = onClear)
        when (kind) {
            SearchResultsKind.Idle -> SearchMessage(
                title = stringResource(R.string.search_idle_title),
                message = stringResource(R.string.search_idle_message),
                failed = false,
                onRetry = {},
            )
            SearchResultsKind.Failed -> SearchMessage(
                title = stringResource(R.string.search_error_title),
                message = searchFailure(tracks.loadState.refresh).searchMessage(),
                failed = true,
                onRetry = {
                    onRetryTracks()
                    onRetryArtists()
                },
            )
            SearchResultsKind.Loading -> if (state.artists.keepsTheList) {
                SearchResults(
                    modifier = Modifier.weight(1f),
                    state = state,
                    tracks = tracks,
                    kind = kind,
                    onRetryArtists = onRetryArtists,
                    onOpenTrack = onOpenTrack,
                    onOpenArtist = onOpenArtist,
                    onPlayTrack = onPlayTrack,
                )
            } else {
                MusicFlowLoading(modifier = Modifier.weight(1f).fillMaxHeight())
            }
            SearchResultsKind.Empty -> if (state.artists.keepsTheList) {
                SearchResults(
                    modifier = Modifier.weight(1f),
                    state = state,
                    tracks = tracks,
                    kind = kind,
                    onRetryArtists = onRetryArtists,
                    onOpenTrack = onOpenTrack,
                    onOpenArtist = onOpenArtist,
                    onPlayTrack = onPlayTrack,
                )
            } else {
                SearchMessage(
                    title = stringResource(R.string.search_empty_title),
                    message = stringResource(R.string.search_empty_message),
                    failed = false,
                    onRetry = {},
                )
            }
            SearchResultsKind.Ready -> SearchResults(
                modifier = Modifier.weight(1f),
                state = state,
                tracks = tracks,
                kind = kind,
                onRetryArtists = onRetryArtists,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onPlayTrack = onPlayTrack,
            )
        }
    }
}

private val ArtistMatches.keepsTheList: Boolean
    get() = this is ArtistMatches.Ready || this is ArtistMatches.Failed

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    val canClear = query.isNotEmpty()
    MusicFlowTextField(
        value = query,
        onValueChange = onQueryChange,
        label = stringResource(R.string.search_label),
        placeholder = stringResource(R.string.search_placeholder),
        leadingIcon = Icons.Outlined.Search,
        trailingIcon = if (canClear) Icons.Outlined.Close else null,
        trailingIconDescription = stringResource(R.string.search_clear),
        onTrailingIconClick = if (canClear) onClear else null,
        modifier = Modifier.padding(
            horizontal = MusicFlowSpacing.screenHorizontal,
            vertical = MusicFlowSpacing.medium,
        ),
    )
}

@Composable
private fun ColumnScope.SearchMessage(
    title: String,
    message: String,
    failed: Boolean,
    onRetry: () -> Unit,
) {
    if (failed) {
        MusicFlowErrorState(
            title = title,
            message = message,
            onRetry = onRetry,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    } else {
        MusicFlowEmptyState(
            title = title,
            message = message,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    tracks: LazyPagingItems<Track>,
    kind: SearchResultsKind,
    onRetryArtists: () -> Unit,
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onPlayTrack: (Track) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(SearchTags.results),
        contentPadding = PaddingValues(bottom = MusicFlowSpacing.large),
        verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
    ) {
        item(key = "artists") {
            SearchArtists(
                artists = state.artists,
                showLoading = kind != SearchResultsKind.Loading,
                onRetry = onRetryArtists,
                onOpenArtist = onOpenArtist,
            )
        }
        if (kind == SearchResultsKind.Ready && tracks.loadState.refresh is LoadState.Error) {
            item(key = "cached-error") {
                Text(
                    text = searchFailure(tracks.loadState.refresh).searchMessage(),
                    modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        when (kind) {
            SearchResultsKind.Loading -> item(key = "loading") { MusicFlowLoading() }
            SearchResultsKind.Empty -> item(key = "empty") {
                MusicFlowEmptyState(
                    title = stringResource(R.string.search_empty_title),
                    message = stringResource(R.string.search_empty_message),
                )
            }
            SearchResultsKind.Ready -> {
                item(key = "tracks-title") { SectionTitle(stringResource(R.string.search_tracks)) }
                items(
                    count = tracks.itemCount,
                    key = { index -> tracks.peek(index)?.id?.value ?: "search-$index" },
                ) { index ->
                    val track = tracks[index] ?: return@items
                    SearchTrackRow(
                        title = track.title,
                        artist = track.artist.name,
                        artworkUrl = track.artworkUrl,
                        duration = formatTrackDuration(track.durationMs),
                        onClick = {
                            onPlayTrack(track)
                            onOpenTrack(track.id.value)
                        },
                    )
                }
                if (tracks.loadState.append is LoadState.Loading) {
                    item(key = "append-loading") { MusicFlowLoading() }
                }
                if (tracks.loadState.append is LoadState.Error) {
                    item(key = "append-retry") {
                        MusicFlowButton(
                            text = stringResource(R.string.search_retry),
                            onClick = tracks::retry,
                            modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
                        )
                    }
                }
            }
            SearchResultsKind.Idle,
            SearchResultsKind.Failed,
            -> Unit
        }
    }
}

@Composable
private fun SearchArtists(
    artists: ArtistMatches,
    showLoading: Boolean,
    onRetry: () -> Unit,
    onOpenArtist: (Long) -> Unit,
) {
    when (artists) {
        ArtistMatches.Hidden -> Unit
        ArtistMatches.Loading -> if (showLoading) MusicFlowLoading()
        is ArtistMatches.Failed -> MusicFlowErrorState(
            title = stringResource(R.string.search_artist_error_title),
            message = artists.error.searchMessage(),
            onRetry = onRetry,
        )
        is ArtistMatches.Ready -> Column(verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium)) {
            SectionTitle(stringResource(R.string.search_artists))
            LazyRow(
                contentPadding = PaddingValues(horizontal = MusicFlowSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
            ) {
                items(items = artists.artists, key = { it.id }) { artist ->
                    MusicFlowArtistCard(
                        name = artist.name,
                        imageUrl = artist.imageUrl,
                        onClick = { onOpenArtist(artist.id) },
                        onClickLabel = artist.name,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun SearchTrackRow(
    title: String,
    artist: String,
    artworkUrl: String?,
    duration: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
    ) {
        MusicFlowAlbumArtwork(
            imageUrl = artworkUrl,
            contentDescription = title,
            modifier = Modifier.size(56.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DomainError.searchMessage(): String = stringResource(
    when (this) {
        DomainError.Offline,
        DomainError.Timeout,
        DomainError.Unavailable,
        -> R.string.search_error_offline
        else -> R.string.search_error_generic
    },
)

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 800)
@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 840, heightDp = 800, name = "Largo")
@Composable
private fun SearchScreenPreview() {
    MusicFlowTheme {
        SearchPreview(sampleState(), sampleTracks())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 640)
@Composable
private fun SearchIdlePreview() {
    MusicFlowTheme { SearchPreview(SearchUiState(), emptyList()) }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 640)
@Composable
private fun SearchLoadingPreview() {
    MusicFlowTheme {
        SearchPreview(SearchUiState(query = "aurora", artists = ArtistMatches.Loading), emptyList(), loading = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 640)
@Composable
private fun SearchEmptyPreview() {
    MusicFlowTheme { SearchPreview(SearchUiState(query = "nada"), emptyList()) }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 640)
@Composable
private fun SearchErrorPreview() {
    MusicFlowTheme {
        SearchPreview(
            SearchUiState(query = "aurora", artists = ArtistMatches.Failed(DomainError.Offline)),
            emptyList(),
            failed = true,
        )
    }
}

@Composable
private fun SearchPreview(
    state: SearchUiState,
    tracks: List<PreviewTrack>,
    loading: Boolean = false,
    failed: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(query = state.query, onQueryChange = {}, onClear = {})
        when {
            state.query.isBlank() -> SearchMessage(
                title = "O que você quer ouvir?",
                message = "Digite uma música ou um artista.",
                failed = false,
                onRetry = {},
            )
            failed -> SearchMessage(
                title = "Não foi possível buscar",
                message = "Sem conexão. Tente de novo quando a rede voltar.",
                failed = true,
                onRetry = {},
            )
            loading -> MusicFlowLoading(modifier = Modifier.fillMaxSize())
            tracks.isEmpty() -> SearchMessage(
                title = "Nenhum resultado",
                message = "Não encontramos músicas para essa busca.",
                failed = false,
                onRetry = {},
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = MusicFlowSpacing.large)) {
                item {
                    SearchArtists(
                        artists = state.artists,
                        showLoading = true,
                        onRetry = {},
                        onOpenArtist = {},
                    )
                }
                item { SectionTitle("Músicas") }
                items(tracks, key = { it.title }) { track ->
                    SearchTrackRow(
                        title = track.title,
                        artist = track.artist,
                        artworkUrl = track.artworkUrl,
                        duration = track.duration,
                        onClick = {},
                    )
                }
            }
        }
    }
}

private fun sampleState() = SearchUiState(
    query = "aurora",
    artists = ArtistMatches.Ready(
        listOf(SearchArtistModel(7, "Estúdio Norte", "https://example.com/artist.jpg")),
    ),
)

private fun sampleTracks() = listOf(
    PreviewTrack("Aurora", "Luna Park", "https://example.com/art.jpg", "3:00"),
    PreviewTrack("Maré", "Luna Park", "https://example.com/mare.jpg", "4:12"),
)

private data class PreviewTrack(
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val duration: String,
)
