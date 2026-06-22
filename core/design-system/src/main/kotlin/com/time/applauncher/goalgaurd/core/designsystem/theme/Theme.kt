package com.time.applauncher.goalgaurd.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoalGuardColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryLight,
    secondary = PrimaryLight,
    onSecondary = Color.White,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = Color.White,
    outline = BorderSubtle,
    outlineVariant = BorderPrimary,
)

@Composable
fun GoalGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GoalGuardColorScheme,
        typography = GoalGuardTypography,
        content = content,
    )
}
