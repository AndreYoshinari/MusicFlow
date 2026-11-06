package br.com.musicflow.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

@Composable
internal fun Modifier.musicFlowPressable(
    onClick: () -> Unit,
    onClickLabel: String? = null,
    enabled: Boolean = true,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) MusicFlowComponentDefaults.pressedScale else 1f,
        animationSpec = tween(durationMillis = MusicFlowComponentDefaults.pressAnimationMillis),
        label = "musicflowPress",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = Role.Button,
            onClick = onClick,
        )
}
