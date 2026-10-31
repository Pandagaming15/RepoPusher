package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GitHubGreen,
    onPrimary = Color.White,
    primaryContainer = GitHubGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GitHubBlue,
    onSecondary = Color.White,
    secondaryContainer = GitHubBlueDark,
    onSecondaryContainer = Color.White,
    tertiary = GitHubPurple,
    onTertiary = Color.White,
    background = GitHubDarkBackground,
    onBackground = GitHubTextPrimary,
    surface = GitHubDarkSurface,
    onSurface = GitHubTextPrimary,
    surfaceVariant = GitHubDarkSurfaceVariant,
    onSurfaceVariant = GitHubTextSecondary,
    outline = GitHubDarkBorder,
    error = GitHubRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Always use the developer dark theme for RepoStudio per design requirements

@Composable
fun RepoStudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
