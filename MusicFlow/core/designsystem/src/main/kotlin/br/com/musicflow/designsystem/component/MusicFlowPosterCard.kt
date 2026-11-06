package br.com.musicflow.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
internal fun MusicFlowPosterCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    artworkShape: Shape = MaterialTheme.shapes.medium,
    bordered: Boolean = false,
    onClickLabel: String? = null,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Column(
        modifier = modifier
            .width(MusicFlowComponentDefaults.musicCardWidth(screenWidth))
            .musicFlowPressable(onClick = onClick, onClickLabel = onClickLabel),
    ) {
        val borderModifier = if (bordered) {
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                shape = artworkShape,
            )
        } else {
            Modifier
        }
        MusicFlowAlbumArtwork(
            imageUrl = imageUrl,
            contentDescription = null,
            shape = artworkShape,
            modifier = borderModifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(modifier = Modifier.height(MusicFlowSpacing.small))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
