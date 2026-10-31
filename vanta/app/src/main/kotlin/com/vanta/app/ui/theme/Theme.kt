package com.vanta.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VantaDarkScheme = darkColorScheme(
    primary = VantaSilver,
    onPrimary = VantaBlack,
    secondary = VantaMuted,
    onSecondary = VantaText,
    background = VantaBlack,
    onBackground = VantaText,
    surface = VantaSurface,
    onSurface = VantaText,
    surfaceVariant = VantaSurfaceElevated,
    onSurfaceVariant = VantaMuted,
    outline = VantaDivider
)

@Composable
fun VantaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VantaDarkScheme,
        typography = VantaTypography,
        content = content
    )
}
