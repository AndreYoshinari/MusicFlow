package br.com.musicflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.PageLoadException
import br.com.musicflow.common.domain.Track
import br.com.musicflow.designsystem.component.MusicFlowArtistCard
import br.com.musicflow.designsystem.component.MusicFlowButton
import br.com.musicflow.designsystem.component.MusicFlowEmptyState
import br.com.musicflow.designsystem.component.MusicFlowErrorState
import br.com.musicflow.designsystem.component.MusicFlowLoading
import br.com.musicflow.designsystem.component.MusicFlowMusicCard
import br.com.musicflow.designsystem.component.MusicFlowPlaylistCard
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import org.koin.androidx.compose.koinViewModel

internal object HomeTags {
    const val screen = "destination_home"
    const val greeting = "home_greeting"
    const val popular = "home_popular"
    const val artists = "home_artists"
    const val albums = "home_albums"
    const val recent = "home_recent"
    const val playlists = "home_playlists"
}

@Composable
fun HomeRoute(
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onPlayTrack: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val popularTracks = viewModel.popularTracks.collectAsLazyPagingItems()
    HomeScreen(
        state = state,
        popularTracks = popularTracks,
        onOpenTrack = onOpenTrack,
        onOpenArtist = onOpenArtist,
        onOpenAlbum = onOpenAlbum,
        onOpenPlaylist = onOpenPlaylist,
        onPlayTrack = onPlayTrack,
        onRetryArtists = viewModel::retryArtists,
        onRetryAlbums = viewModel::retryAlbums,
        onRetryRecent = viewModel::retryRecent,
        onRetryPlaylists = viewModel::retryPlaylists,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    popularTracks: LazyPagingItems<Track>,
    onOpenTrack: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onRetryArtists: () -> Unit,
    onRetryAlbums: () -> Unit,
    onRetryRecent: () -> Unit,
    onRetryPlaylists: () -> Unit,
    onPlayTrack: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(HomeTags.screen),
        contentPadding = PaddingValues(vertical = MusicFlowSpacing.large),
        verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.large),
    ) {
        item(key = HomeTags.greeting) {
            HomeGreeting(period = state.greeting)
        }
        item(key = HomeTags.popular) {
            HomePopularSection(tracks = popularTracks, onOpenTrack = onOpenTrack, onPlayTrack = onPlayTrack)
        }
        item(key = HomeTags.artists) {
            HomeCardSection(
                title = stringResource(R.string.home_artists),
                state = state.artists,
                emptyTitle = stringResource(R.string.home_empty_artists_title),
                emptyMessage = stringResource(R.string.home_empty_artists_message),
                onRetry = onRetryArtists,
                tag = HomeTags.artists,
            ) { artists ->
                LazyRow(
                    contentPadding = rowPadding(),
                    horizontalArrangement = rowSpacing(),
                ) {
                    items(items = artists, key = { it.id }) { artist ->
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
        item(key = HomeTags.albums) {
            HomeCardSection(
                title = stringResource(R.string.home_albums),
                state = state.albums,
                emptyTitle = stringResource(R.string.home_empty_albums_title),
                emptyMessage = stringResource(R.string.home_empty_albums_message),
                onRetry = onRetryAlbums,
                tag = HomeTags.albums,
            ) { albums ->
                LazyRow(
                    contentPadding = rowPadding(),
                    horizontalArrangement = rowSpacing(),
                ) {
                    items(items = albums, key = { it.id }) { album ->
                        MusicFlowMusicCard(
                            title = album.title,
                            artist = album.artist,
                            imageUrl = album.artworkUrl,
                            onClick = { onOpenAlbum(album.id) },
                            onClickLabel = album.title,
                        )
                    }
                }
            }
        }
        item(key = HomeTags.recent) {
            HomeCardSection(
                title = stringResource(R.string.home_recent),
                state = state.recent,
                emptyTitle = stringResource(R.string.home_empty_recent_title),
                emptyMessage = stringResource(R.string.home_empty_recent_message),
                onRetry = onRetryRecent,
                tag = HomeTags.recent,
            ) { tracks ->
                LazyRow(
                    contentPadding = rowPadding(),
                    horizontalArrangement = rowSpacing(),
                ) {
                    items(items = tracks, key = { it.id }) { track ->
                        MusicFlowMusicCard(
                            title = track.title,
                            artist = track.subtitle,
                            imageUrl = track.artworkUrl,
                            onClick = {
                                onPlayTrack(track.id, track.title, track.subtitle, track.artworkUrl, track.streamUrl)
                                onOpenTrack(track.id)
                            },
                            onClickLabel = track.title,
                        )
                    }
                }
            }
        }
        item(key = HomeTags.playlists) {
            HomeCardSection(
                title = stringResource(R.string.home_playlists),
                state = state.playlists,
                emptyTitle = stringResource(R.string.home_empty_playlists_title),
                emptyMessage = stringResource(R.string.home_empty_playlists_message),
                onRetry = onRetryPlaylists,
                tag = HomeTags.playlists,
            ) { playlists ->
                LazyRow(
                    contentPadding = rowPadding(),
                    horizontalArrangement = rowSpacing(),
                ) {
                    items(items = playlists, key = { it.id }) { playlist ->
                        MusicFlowPlaylistCard(
                            title = playlist.name,
                            subtitle = pluralStringResource(
                                R.plurals.home_track_count,
                                playlist.trackCount,
                                playlist.trackCount,
                            ),
                            onClick = { onOpenPlaylist(playlist.id) },
                            onClickLabel = playlist.name,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeGreeting(period: GreetingPeriod, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(period.label()),
        modifier = modifier
            .padding(horizontal = MusicFlowSpacing.screenHorizontal)
            .testTag(HomeTags.greeting),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun HomePopularSection(
    tracks: LazyPagingItems<Track>,
    onOpenTrack: (Long) -> Unit,
    onPlayTrack: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    HomeSectionFrame(title = stringResource(R.string.home_popular), tag = HomeTags.popular, modifier = modifier) {
        when (val kind = popularRowKind(tracks.loadState.refresh, tracks.itemCount)) {
            PopularRowKind.Loading -> MusicFlowLoading()
            PopularRowKind.Empty -> MusicFlowEmptyState(
                title = stringResource(R.string.home_empty_popular_title),
                message = stringResource(R.string.home_empty_popular_message),
            )
            PopularRowKind.Failed -> {
                val error = (tracks.loadState.refresh as? LoadState.Error)?.error as? PageLoadException
                MusicFlowErrorState(
                    title = stringResource(R.string.home_error_title),
                    message = (error?.error ?: DomainError.Unavailable).homeMessage(),
                    onRetry = tracks::retry,
                )
            }
            PopularRowKind.Ready -> Column(verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small)) {
                val cachedError = (tracks.loadState.refresh as? LoadState.Error)?.error
                if (cachedError != null) {
                    val domainError = (cachedError as? PageLoadException)?.error ?: DomainError.Unavailable
                    Text(
                        text = domainError.homeMessage(),
                        modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                LazyRow(
                    contentPadding = rowPadding(),
                    horizontalArrangement = rowSpacing(),
                ) {
                items(
                    count = tracks.itemCount,
                    key = { index -> tracks.peek(index)?.id?.value ?: "popular-$index" },
                ) { index ->
                    val item = tracks[index] ?: return@items
                    val track = item.toCard()
                    MusicFlowMusicCard(
                        title = track.title,
                        artist = track.subtitle,
                        imageUrl = track.artworkUrl,
                        onClick = {
                            onPlayTrack(item.id.value, item.title, item.artist.name, item.artworkUrl, item.streamUrl)
                            onOpenTrack(track.id)
                        },
                        onClickLabel = track.title,
                    )
                }
                if (tracks.loadState.append is LoadState.Loading) {
                    item(key = "popular-append") { MusicFlowLoading() }
                }
                if (tracks.loadState.append is LoadState.Error) {
                    item(key = "popular-retry") {
                        MusicFlowButton(
                            text = stringResource(R.string.home_retry),
                            onClick = tracks::retry,
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun <T> HomeCardSection(
    title: String,
    state: SectionState<T>,
    emptyTitle: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    tag: String,
    ready: @Composable (List<T>) -> Unit,
) {
    HomeSectionFrame(title = title, tag = tag) {
        when (state) {
            SectionState.Loading -> MusicFlowLoading()
            SectionState.Empty -> MusicFlowEmptyState(title = emptyTitle, message = emptyMessage)
            is SectionState.Failed -> MusicFlowErrorState(
                title = stringResource(R.string.home_error_title),
                message = state.error.homeMessage(),
                onRetry = onRetry,
            )
            is SectionState.Ready -> ready(state.items)
        }
    }
}

@Composable
private fun HomeSectionFrame(
    title: String,
    tag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.testTag(tag),
        verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Composable
private fun GreetingPeriod.label(): Int = when (this) {
    GreetingPeriod.Morning -> R.string.home_greeting_morning
    GreetingPeriod.Afternoon -> R.string.home_greeting_afternoon
    GreetingPeriod.Night -> R.string.home_greeting_night
}

@Composable
private fun DomainError.homeMessage(): String = stringResource(
    when (this) {
        DomainError.Offline,
        DomainError.Timeout,
        DomainError.Unavailable,
        -> R.string.home_error_offline
        else -> R.string.home_error_generic
    },
)

private fun rowPadding() = PaddingValues(horizontal = MusicFlowSpacing.screenHorizontal)

private fun rowSpacing() = Arrangement.spacedBy(MusicFlowSpacing.medium)

internal enum class PopularRowKind {
    Loading,
    Empty,
    Failed,
    Ready,
}

internal fun popularRowKind(refresh: LoadState, itemCount: Int): PopularRowKind = when {
    itemCount > 0 -> PopularRowKind.Ready
    refresh is LoadState.Loading -> PopularRowKind.Loading
    refresh is LoadState.Error -> PopularRowKind.Failed
    else -> PopularRowKind.Empty
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 1400)
@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 840, heightDp = 1200, name = "Largo")
@Composable
private fun HomeScreenPreview() {
    MusicFlowTheme {
        HomePreviewScreen(sampleState())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 900)
@Composable
private fun HomeScreenLoadingPreview() {
    MusicFlowTheme {
        HomePreviewScreen(
            state = sampleState().copy(
                artists = SectionState.Loading,
                albums = SectionState.Loading,
                recent = SectionState.Loading,
                playlists = SectionState.Loading,
            ),
            popular = SectionState.Loading,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 1200)
@Composable
private fun HomeScreenEmptyPreview() {
    MusicFlowTheme {
        HomePreviewScreen(
            state = HomeUiState(
                greeting = GreetingPeriod.Night,
                artists = SectionState.Empty,
                albums = SectionState.Empty,
                recent = SectionState.Empty,
                playlists = SectionState.Empty,
            ),
            popular = SectionState.Empty,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360, heightDp = 1200)
@Composable
private fun HomeScreenErrorPreview() {
    MusicFlowTheme {
        HomePreviewScreen(
            state = sampleState().copy(
                artists = SectionState.Failed(DomainError.Offline),
                albums = SectionState.Failed(DomainError.Unavailable),
            ),
            popular = SectionState.Failed(DomainError.Offline),
        )
    }
}

@Composable
private fun HomePreviewScreen(
    state: HomeUiState,
    popular: SectionState<TrackCardModel> = SectionState.Ready(listOf(sampleTrack())),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = MusicFlowSpacing.large),
        verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.large),
    ) {
        item { HomeGreeting(state.greeting) }
        item {
            PreviewRow(title = "Músicas populares", state = popular, tag = HomeTags.popular) { track ->
                MusicFlowMusicCard(
                    title = track.title,
                    artist = track.subtitle,
                    imageUrl = track.artworkUrl,
                    onClick = {},
                )
            }
        }
        item {
            PreviewRow(title = "Artistas em destaque", state = state.artists, tag = HomeTags.artists) { artist ->
                MusicFlowArtistCard(name = artist.name, imageUrl = artist.imageUrl, onClick = {})
            }
        }
        item {
            PreviewRow(title = "Álbuns", state = state.albums, tag = HomeTags.albums) { album ->
                MusicFlowMusicCard(
                    title = album.title,
                    artist = album.artist,
                    imageUrl = album.artworkUrl,
                    onClick = {},
                )
            }
        }
        item {
            PreviewRow(title = "Tocadas recentemente", state = state.recent, tag = HomeTags.recent) { track ->
                MusicFlowMusicCard(
                    title = track.title,
                    artist = track.subtitle,
                    imageUrl = track.artworkUrl,
                    onClick = {},
                )
            }
        }
        item {
            PreviewRow(title = "Suas playlists", state = state.playlists, tag = HomeTags.playlists) { playlist ->
                MusicFlowPlaylistCard(
                    title = playlist.name,
                    subtitle = "${playlist.trackCount} faixas",
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun <T> PreviewRow(
    title: String,
    state: SectionState<T>,
    tag: String,
    card: @Composable (T) -> Unit,
) {
    HomeCardSection(
        title = title,
        state = state,
        emptyTitle = "Nada por aqui",
        emptyMessage = "Esta fileira fica vazia até o catálogo responder.",
        onRetry = {},
        tag = tag,
    ) { entries ->
        LazyRow(contentPadding = rowPadding(), horizontalArrangement = rowSpacing()) {
            items(entries.size) { index -> card(entries[index]) }
        }
    }
}

private fun sampleState() = HomeUiState(
    greeting = GreetingPeriod.Morning,
    artists = SectionState.Ready(
        listOf(ArtistCardModel(7, "Luna Park", "https://example.com/artist.jpg")),
    ),
    albums = SectionState.Ready(
        listOf(AlbumCardModel(3, "Noite", "Luna Park", "https://example.com/album.jpg")),
    ),
    recent = SectionState.Ready(listOf(sampleTrack())),
    playlists = SectionState.Ready(listOf(PlaylistCardModel(4, "Noite violeta", 18))),
)

private fun sampleTrack() = TrackCardModel(
    id = 1,
    title = "Aurora",
    subtitle = "Luna Park",
    artworkUrl = "https://example.com/art.jpg",
)
