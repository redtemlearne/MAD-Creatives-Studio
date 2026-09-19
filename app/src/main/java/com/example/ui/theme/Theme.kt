package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = StudioAccent,
    onPrimary = StudioOnAccent,
    primaryContainer = StudioAccentMuted,
    onPrimaryContainer = StudioAccent,
    surface = StudioSurfacePrimary,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceSecondary,
    onSurfaceVariant = StudioTextSecondary,
    background = StudioBackground,
    onBackground = StudioTextPrimary,
    outline = StudioBorder,
    outlineVariant = StudioBorderSubtle,
    error = StudioDestructive
)

data class StudioCustomColors(
    val background: androidx.compose.ui.graphics.Color = StudioBackground,
    val surfacePrimary: androidx.compose.ui.graphics.Color = StudioSurfacePrimary,
    val surfaceSecondary: androidx.compose.ui.graphics.Color = StudioSurfaceSecondary,
    val surfaceElevated: androidx.compose.ui.graphics.Color = StudioSurfaceElevated,
    val textPrimary: androidx.compose.ui.graphics.Color = StudioTextPrimary,
    val textSecondary: androidx.compose.ui.graphics.Color = StudioTextSecondary,
    val textMuted: androidx.compose.ui.graphics.Color = StudioTextMuted,
    val border: androidx.compose.ui.graphics.Color = StudioBorder,
    val borderSubtle: androidx.compose.ui.graphics.Color = StudioBorderSubtle,
    val accent: androidx.compose.ui.graphics.Color = StudioAccent,
    val accentMuted: androidx.compose.ui.graphics.Color = StudioAccentMuted,
    val onAccent: androidx.compose.ui.graphics.Color = StudioOnAccent,
    val destructive: androidx.compose.ui.graphics.Color = StudioDestructive,
    val disabledSurface: androidx.compose.ui.graphics.Color = StudioDisabledSurface,
    val disabledContent: androidx.compose.ui.graphics.Color = StudioDisabledContent
)

val LocalStudioColors = staticCompositionLocalOf { StudioCustomColors() }

object StudioTheme {
    val colors: StudioCustomColors
        @Composable
        get() = LocalStudioColors.current

    val spacing: StudioSpacing
        get() = StudioSpacing

    val radius: StudioRadius
        get() = StudioRadius
}

@Composable
fun MADCreativesTheme(
    content: @Composable () -> Unit
) {
    // MAD Creatives Studio uses a dedicated dark creative editor theme
    val customColors = StudioCustomColors()

    CompositionLocalProvider(
        LocalStudioColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = StudioTypography,
            content = content
        )
    }
}
