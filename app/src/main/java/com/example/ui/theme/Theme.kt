package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AnnivoSaffron,
    onPrimary = Color.White,
    primaryContainer = AnnivoNavy,
    onPrimaryContainer = Color.White,
    secondary = EmeraldLight,
    onSecondary = AnnivoNavy,
    secondaryContainer = EmeraldGreen,
    onSecondaryContainer = Color.White,
    tertiary = GoldYellow,
    onTertiary = AnnivoNavy,
    background = AnnivoNavy,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AnnivoSaffron,
    onPrimary = Color.White,
    primaryContainer = AnnivoSaffronAccent,
    onPrimaryContainer = AnnivoNavy,
    secondary = AnnivoNavy,
    onSecondary = Color.White,
    secondaryContainer = AnnivoNavyBg,
    onSecondaryContainer = AnnivoNavy,
    tertiary = GoldYellow,
    onTertiary = AnnivoNavy,
    background = AnnivoNavyBg,
    surface = LightSurface,
    onBackground = AnnivoNavy,
    onSurface = AnnivoNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic system color to preserve our custom ANNIVO brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
