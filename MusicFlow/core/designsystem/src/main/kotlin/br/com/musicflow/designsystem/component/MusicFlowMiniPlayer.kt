package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import br.com.musicflow.designsystem.R
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
fun MusicFlowMiniPlayer(
    title: String,
    artist: String,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    progress: Float = 0f,
    progressContent: (@Composable () -> Unit)? = null,
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    val playPauseDescription = stringResource(
        if (isPlaying) R.string.musicflow_pause else R.string.musicflow_play,
    )
    val progressDescription = stringResource(R.string.musicflow_playback_progress)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MusicFlowSpacing.medium, vertical = MusicFlowSpacing.small),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(MusicFlowSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MusicFlowAlbumArtwork(
                    imageUrl = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(MusicFlowComponentDefaults.minTouchTarget),
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = MusicFlowSpacing.small)
                        .weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                MusicFlowIconButton(
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = playPauseDescription,
                    onClick = onPlayPauseClick,
                    style = MusicFlowIconButtonStyle.Tonal,
                )
            }
            if (progressContent != null) {
                progressContent()
            } else {
                LinearProgressIndicator(
                    progress = { coercedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress, 0f..1f)
                            contentDescription = progressDescription
                        },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun MusicFlowMiniPlayerPreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowMiniPlayer(
            title = "Aurora",
            artist = "Luna Park",
            isPlaying = true,
            progress = 0.35f,
            onPlayPauseClick = {},
            onClick = {},
            modifier = Modifier.width(360.dp),
        )
    }
}
