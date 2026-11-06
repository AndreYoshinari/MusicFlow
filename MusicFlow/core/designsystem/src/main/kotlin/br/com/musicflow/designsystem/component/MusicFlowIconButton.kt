package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

enum class MusicFlowIconButtonStyle {
    Standard,
    Tonal,
}

@Composable
fun MusicFlowIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: MusicFlowIconButtonStyle = MusicFlowIconButtonStyle.Standard,
) {
    val iconContent: @Composable () -> Unit = {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
    when (style) {
        MusicFlowIconButtonStyle.Standard -> IconButton(
            onClick = onClick,
            modifier = modifier.size(MusicFlowComponentDefaults.minTouchTarget),
            enabled = enabled,
            colors = MusicFlowComponentDefaults.standardIconButtonColors(),
            content = iconContent,
        )

        MusicFlowIconButtonStyle.Tonal -> FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier.size(MusicFlowComponentDefaults.minTouchTarget),
            enabled = enabled,
            colors = MusicFlowComponentDefaults.tonalIconButtonColors(),
            content = iconContent,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun MusicFlowIconButtonPreview() {
    MusicFlowPreview {
        MusicFlowIconButton(
            icon = Icons.Filled.PlayArrow,
            contentDescription = "Reproduzir",
            onClick = {},
            style = MusicFlowIconButtonStyle.Tonal,
        )
    }
}
