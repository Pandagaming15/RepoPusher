package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GitHubGreen,
    onPrimary = Color.White,
    primaryContainer = GitHubGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GitHubBlue,
    onSecondary = Color.White,
    secondaryContainer = GitHubDarkSurfaceVariant,
    onSecondaryContainer = GitHubTextPrimary,
    tertiary = GitHubPurple,
    background = GitHubDarkBackground,
    onBackground = GitHubTextPrimary,
    surface = GitHubDarkSurface,
    onSurface = GitHubTextPrimary,
    surfaceVariant = GitHubDarkSurfaceVariant,
    onSurfaceVariant = GitHubTextSecondary,
    outline = GitHubDarkBorder
)

@Composable
fun RepoStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
