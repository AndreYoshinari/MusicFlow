package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
fun MusicFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MusicFlowComponentDefaults.minTouchTarget),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = MusicFlowComponentDefaults.buttonColors(),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(MusicFlowComponentDefaults.iconSize),
                )
                Spacer(modifier = Modifier.width(MusicFlowSpacing.small))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun MusicFlowButtonPreview() {
    MusicFlowPreview {
        MusicFlowButton(text = "Ouvir agora", onClick = {}, icon = Icons.Filled.PlayArrow)
    }
}
