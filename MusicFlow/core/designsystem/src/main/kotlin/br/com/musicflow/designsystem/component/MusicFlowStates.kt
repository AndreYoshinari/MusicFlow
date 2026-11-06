package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import br.com.musicflow.designsystem.R
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
fun MusicFlowLoading(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val description = message ?: stringResource(R.string.musicflow_loading)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MusicFlowSpacing.large)
            .semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.medium),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun MusicFlowEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.MusicNote,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MusicFlowStateMessage(
        title = title,
        message = message,
        icon = icon,
        iconTint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
        action = if (actionLabel != null && onAction != null) {
            { MusicFlowButton(text = actionLabel, onClick = onAction) }
        } else {
            null
        },
    )
}

@Composable
fun MusicFlowErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.musicflow_retry),
) {
    MusicFlowStateMessage(
        title = title,
        message = message,
        icon = Icons.Outlined.ErrorOutline,
        iconTint = MaterialTheme.colorScheme.error,
        modifier = modifier,
        action = { MusicFlowButton(text = retryLabel, onClick = onRetry) },
    )
}

@Composable
private fun MusicFlowStateMessage(
    title: String,
    message: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MusicFlowSpacing.large),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = MusicFlowComponentDefaults.stateContentMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MusicFlowSpacing.small),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(MusicFlowSpacing.extraLarge),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Box(modifier = Modifier.padding(top = MusicFlowSpacing.small)) {
                    action()
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun MusicFlowLoadingPreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowLoading(message = "Buscando músicas")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun MusicFlowEmptyStatePreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowEmptyState(
            title = "Nada por aqui",
            message = "Quando você favoritar uma música, ela aparece neste espaço.",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 840)
@Composable
private fun MusicFlowErrorStatePreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowErrorState(
            title = "Não foi possível carregar",
            message = "Verifique a conexão e tente outra vez.",
            onRetry = {},
        )
    }
}
