package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicFlowTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationContentDescription: String? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                MusicFlowIconButton(
                    icon = navigationIcon,
                    contentDescription = navigationContentDescription.orEmpty(),
                    onClick = onNavigationClick,
                )
            }
        },
        actions = actions,
        colors = MusicFlowComponentDefaults.topBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun MusicFlowTopBarPreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowTopBar(
            title = "MusicFlow",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            navigationContentDescription = "Voltar",
            onNavigationClick = {},
        )
    }
}
