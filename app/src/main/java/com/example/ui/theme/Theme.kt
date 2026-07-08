package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = CharcoalBackground,
    surface = CharcoalSurface,
    onPrimary = CharcoalOnPrimary,
    onSecondary = CharcoalOnSecondary,
    onTertiary = CharcoalOnSecondary,
    onBackground = CharcoalOnBackground,
    onSurface = CharcoalOnSurface,
    onSurfaceVariant = CharcoalOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = TealTertiary,
    background = CalmingBackground,
    surface = CalmingSurface,
    onPrimary = CalmingSurface,
    onSecondary = CalmingSurface,
    onTertiary = CalmingSurface,
    onBackground = CalmingOnBackground,
    onSurface = CalmingOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our highly customized, calming color theme instead of random wallpaper schemes
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
