package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
fun MusicFlowMusicCard(
    title: String,
    artist: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onClickLabel: String? = null,
) {
    MusicFlowPosterCard(
        title = title,
        subtitle = artist,
        onClick = onClick,
        modifier = modifier,
        imageUrl = imageUrl,
        onClickLabel = onClickLabel,
    )
}

@Composable
fun MusicFlowArtistCard(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onClickLabel: String? = null,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Column(
        modifier = modifier
            .width(MusicFlowComponentDefaults.artistCardWidth(screenWidth))
            .musicFlowPressable(onClick = onClick, onClickLabel = onClickLabel),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MusicFlowAlbumArtwork(
            imageUrl = imageUrl,
            contentDescription = null,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(modifier = Modifier.height(MusicFlowSpacing.small))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MusicFlowPlaylistCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onClickLabel: String? = null,
) {
    MusicFlowPosterCard(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        modifier = modifier,
        imageUrl = imageUrl,
        artworkShape = MaterialTheme.shapes.large,
        bordered = true,
        onClickLabel = onClickLabel,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun MusicFlowCardsPreview() {
    MusicFlowPreview {
        Column {
            MusicFlowMusicCard(title = "Aurora", artist = "Luna Park", onClick = {})
            Spacer(modifier = Modifier.height(MusicFlowSpacing.medium))
            MusicFlowArtistCard(name = "Luna Park", onClick = {})
            Spacer(modifier = Modifier.height(MusicFlowSpacing.medium))
            MusicFlowPlaylistCard(title = "Noite violeta", subtitle = "18 faixas", onClick = {})
        }
    }
}
