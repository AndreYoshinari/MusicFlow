package br.com.musicflow.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.designsystem.component.MusicFlowAlbumArtwork
import br.com.musicflow.designsystem.component.MusicFlowEmptyState
import br.com.musicflow.designsystem.component.MusicFlowIconButton
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.player.PlaybackFailure
import br.com.musicflow.player.PlaybackPhase
import br.com.musicflow.player.PlaybackPosition
import br.com.musicflow.player.PlaybackQueue
import br.com.musicflow.player.PlaybackState
import br.com.musicflow.player.PlaybackTrack
import br.com.musicflow.player.RepeatMode
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

internal object NowPlayingTags {
    const val screen = "destination_player"
    const val queue = "player_queue"
}

@Composable
fun NowPlayingRoute(
    playback: MusicPlayback,
    viewModel: NowPlayingFavoriteViewModel = koinViewModel(),
) {
    val track by playback.currentTrack.collectAsStateWithLifecycle()
    val state by playback.playbackState.collectAsStateWithLifecycle()
    val queue by playback.queue.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteError by viewModel.actionError.collectAsStateWithLifecycle()
    NowPlayingScreen(
        track = track,
        state = state,
        queue = queue,
        position = playback.position,
        favorite = track?.id?.value?.let { it in favorites } == true,
        favoriteError = favoriteError,
        onToggleFavorite = { id -> viewModel.toggle(id) },
        onPlayPause = {
            if (state.isPlaying) playback.pause() else playback.resume()
        },
        onPrevious = playback::previous,
        onNext = playback::next,
        onSeek = playback::seekTo,
        onShuffle = { playback.setShuffleEnabled(!state.shuffleEnabled) },
        onRepeat = { playback.setRepeatMode(state.repeatMode.cycled()) },
        onPlayFromQueue = { index -> playback.play(queue.tracks, index) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NowPlayingScreen(
    track: PlaybackTrack?,
    state: PlaybackState,
    queue: PlaybackQueue,
    position: StateFlow<PlaybackPosition>,
    favorite: Boolean,
    favoriteError: DomainError? = null,
    onToggleFavorite: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onPlayFromQueue: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var queueOpen by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(NowPlayingTags.screen),
    ) {
        val current = track
        if (current == null) {
            MusicFlowEmptyState(
                title = stringResource(R.string.player_empty_title),
                message = stringResource(R.string.player_empty_message),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val wide = maxWidth >= 600.dp
                if (wide) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MusicFlowSpacing.large),
                        horizontalArrangement = Arrangement.spacedBy(MusicFlowSpacing.large),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerArtwork(
                            track = current,
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 480.dp),
                        )
                        PlayerControls(
                            track = current,
                            state = state,
                            position = position,
                            favorite = favorite,
                            favoriteError = favoriteError,
                            onToggleFavorite = onToggleFavorite,
                            onPlayPause = onPlayPause,
                            onPrevious = onPrevious,
                            onNext = onNext,
                            onSeek = onSeek,
                            onShuffle = onShuffle,
                            onRepeat = onRepeat,
                            onOpenQueue = { queueOpen = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerArtwork(
                            track = current,
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 420.dp),
                        )
                        Spacer(modifier = Modifier.height(MusicFlowSpacing.large))
                        PlayerControls(
                            track = current,
                            state = state,
                            position = position,
                            favorite = favorite,
                            favoriteError = favoriteError,
                            onToggleFavorite = onToggleFavorite,
                            onPlayPause = onPlayPause,
                            onPrevious = onPrevious,
                            onNext = onNext,
                            onSeek = onSeek,
                            onShuffle = onShuffle,
                            onRepeat = onRepeat,
                            onOpenQueue = { queueOpen = true },
                        )
                    }
                }
            }
        }
    }
    if (queueOpen) {
        ModalBottomSheet(
            onDismissRequest = { queueOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            QueueSheet(
                queue = queue,
                onPlayFromQueue = { index ->
                    onPlayFromQueue(index)
                    queueOpen = false
                },
            )
        }
    }
}

@Composable
private fun PlayerArtwork(track: PlaybackTrack, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = track.id.value to track.artworkUrl,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(420)) + scaleIn(initialScale = 0.94f, animationSpec = tween(420)))
                .togetherWith(fadeOut(tween(180)))
        },
        label = "playerArtwork",
    ) { (_, artworkUrl) ->
        MusicFlowAlbumArtwork(
            imageUrl = artworkUrl,
            contentDescription = track.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}

@Composable
private fun PlayerControls(
    track: PlaybackTrack,
    state: PlaybackState,
    position: StateFlow<PlaybackPosition>,
    favorite: Boolean,
    favoriteError: DomainError? = null,
    onToggleFavorite: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            MusicFlowIconButton(
                icon = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (favorite) R.string.player_unfavorite else R.string.player_favorite,
                    track.title,
                ),
                onClick = { onToggleFavorite(track.id.value) },
            )
        }
        state.failure?.let { failure ->
            Text(
                text = failure.playbackMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        favoriteError?.let { error ->
            Text(
                text = error.favoriteMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(MusicFlowSpacing.medium))
        PlaybackProgress(position = position, onSeek = onSeek)
        Spacer(modifier = Modifier.height(MusicFlowSpacing.small))
        TransportRow(
            state = state,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext,
        )
        Spacer(modifier = Modifier.height(MusicFlowSpacing.small))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeButton(
                selected = state.shuffleEnabled,
                description = stringResource(
                    if (state.shuffleEnabled) R.string.player_shuffle_on else R.string.player_shuffle_off,
                ),
                onClick = onShuffle,
                icon = { tint ->
                    Icon(Icons.Filled.Shuffle, contentDescription = null, tint = tint)
                },
            )
            ModeButton(
                selected = state.repeatMode != RepeatMode.Off,
                description = stringResource(state.repeatMode.label()),
                onClick = onRepeat,
                icon = { tint ->
                    Icon(
                        imageVector = if (state.repeatMode == RepeatMode.One) {
                            Icons.Filled.RepeatOne
                        } else {
                            Icons.Filled.Repeat
                        },
                        contentDescription = null,
                        tint = tint,
                    )
                },
            )
            MusicFlowIconButton(
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = stringResource(R.string.player_queue),
                onClick = onOpenQueue,
            )
        }
    }
}

@Composable
private fun TransportRow(
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val buffering = state.phase == PlaybackPhase.Buffering && state.isPlaying
    val playDescription = stringResource(
        when {
            buffering -> R.string.player_buffering
            state.isPlaying -> br.com.musicflow.designsystem.R.string.musicflow_pause
            else -> br.com.musicflow.designsystem.R.string.musicflow_play
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MusicFlowIconButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = stringResource(R.string.player_previous),
            onClick = onPrevious,
        )
        Crossfade(targetState = buffering to state.isPlaying, label = "playPause") { (waiting, playing) ->
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(72.dp)
                    .semantics { contentDescription = playDescription },
            ) {
                if (waiting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        MusicFlowIconButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = stringResource(R.string.player_next),
            onClick = onNext,
        )
    }
}

@Composable
private fun ModeButton(
    selected: Boolean,
    description: String,
    onClick: () -> Unit,
    icon: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = description },
    ) {
        icon(tint)
    }
}

@Composable
internal fun PlaybackProgress(
    position: StateFlow<PlaybackPosition>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by position.collectAsStateWithLifecycle()
    var seeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }
    val duration = progress.durationMs
    val fraction = if (duration <= 0L) 0f else progress.positionMs.toFloat() / duration.toFloat()
    val shown = if (seeking) seekFraction else fraction.coerceIn(0f, 1f)
    val shownPosition = if (seeking) (seekFraction * duration).toLong() else progress.positionMs
    val description = stringResource(R.string.player_position)
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = shown,
            onValueChange = { value ->
                seeking = true
                seekFraction = value
            },
            onValueChangeFinished = {
                if (duration > 0L) onSeek((seekFraction * duration).toLong())
                seeking = false
            },
            enabled = duration > 0L,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = description },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = formatPlaybackTime(shownPosition), style = MaterialTheme.typography.labelMedium)
            Text(
                text = formatPlaybackTime(duration),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun QueueSheet(queue: PlaybackQueue, onPlayFromQueue: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MusicFlowSpacing.large)
            .testTag(NowPlayingTags.queue),
    ) {
        Text(
            text = stringResource(R.string.player_queue_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.small),
        )
        LazyColumn {
            itemsIndexed(queue.tracks, key = { _, track -> track.id.value }) { index, track ->
                val current = index == queue.currentIndex
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !current) { onPlayFromQueue(index) }
                        .padding(horizontal = MusicFlowSpacing.screenHorizontal, vertical = MusicFlowSpacing.medium),
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackFailure.playbackMessage(): String = stringResource(
    when (this) {
        PlaybackFailure.Connection -> R.string.player_error_offline
        PlaybackFailure.InvalidSource -> R.string.player_error_source
        PlaybackFailure.Playback -> R.string.player_error_generic
    },
)

@Composable
private fun DomainError.favoriteMessage(): String = stringResource(
    if (this == DomainError.NotFound) R.string.player_favorite_missing else R.string.player_favorite_error,
)

@Composable
private fun RepeatMode.label(): Int = when (this) {
    RepeatMode.Off -> R.string.player_repeat_off
    RepeatMode.All -> R.string.player_repeat_all
    RepeatMode.One -> R.string.player_repeat_one
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Preview(showBackground = true, widthDp = 840, heightDp = 480)
@Composable
private fun NowPlayingPreview() {
    MusicFlowTheme {
        NowPlayingScreen(
            track = previewTrack,
            state = PlaybackState(phase = PlaybackPhase.Ready, isPlaying = true, repeatMode = RepeatMode.All),
            queue = PlaybackQueue(listOf(previewTrack), 0),
            position = kotlinx.coroutines.flow.MutableStateFlow(PlaybackPosition(90_000, 180_000, 120_000)),
            favorite = true,
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

private val previewTrack = PlaybackTrack(
    id = br.com.musicflow.common.domain.TrackId(1),
    title = "Aurora",
    artistName = "Luna Park",
    artworkUrl = null,
    streamUrl = "https://example.com/aurora.mp3",
)
