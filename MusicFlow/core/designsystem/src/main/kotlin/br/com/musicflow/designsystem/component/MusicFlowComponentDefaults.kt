package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.musicflow.designsystem.theme.MusicFlowSpacing
import br.com.musicflow.designsystem.theme.MusicFlowTheme

internal object MusicFlowComponentDefaults {
    const val pressedScale = 0.98f
    const val pressAnimationMillis = 160
    const val artworkCrossfadeMillis = 220
    val iconSize = 20.dp
    val minTouchTarget = 48.dp
    val artworkIconFraction = 0.34f
    val stateContentMaxWidth = 420.dp

    fun musicCardWidth(screenWidth: Dp): Dp = when {
        screenWidth < 360.dp -> 132.dp
        screenWidth < 600.dp -> 156.dp
        else -> 184.dp
    }

    fun artistCardWidth(screenWidth: Dp): Dp = when {
        screenWidth < 600.dp -> 112.dp
        else -> 136.dp
    }

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun tonalIconButtonColors(): IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun standardIconButtonColors(): IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun topBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
    )

    @Composable
    fun navigationItemColors() = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun MusicFlowPreview(
    padding: Dp = MusicFlowSpacing.medium,
    content: @Composable () -> Unit,
) {
    MusicFlowTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier.padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
