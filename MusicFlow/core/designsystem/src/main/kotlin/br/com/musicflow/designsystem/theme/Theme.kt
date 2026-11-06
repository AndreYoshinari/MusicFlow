package br.com.musicflow.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = VioletOnPrimary,
    primaryContainer = VioletPrimaryContainer,
    onPrimaryContainer = VioletOnPrimaryContainer,
    secondary = OnSurfaceMuted,
    onSecondary = InkBackground,
    secondaryContainer = GraphiteContainerHigh,
    onSecondaryContainer = OnSurface,
    tertiary = VioletTertiary,
    onTertiary = VioletOnTertiary,
    background = InkBackground,
    onBackground = OnSurface,
    surface = InkSurface,
    onSurface = OnSurface,
    surfaceVariant = GraphiteSurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    surfaceContainerLowest = InkBackground,
    surfaceContainerLow = InkSurface,
    surfaceContainer = GraphiteContainer,
    surfaceContainerHigh = GraphiteContainerHigh,
    surfaceContainerHighest = GraphiteContainerHighest,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    error = ErrorContainer,
    onError = OnErrorContainer,
    errorContainer = Error,
    onErrorContainer = OnError,
)

@Composable
fun MusicFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = MusicFlowTypography,
        shapes = MusicFlowShapes,
        content = content,
    )
}
