package com.Lyno.matchmindai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cyber-Minimalism Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = ConfidenceMedium,
    background = Background,
    surface = Surface,
    onPrimary = Color.Black,  // Black text on neon green
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextHigh,
    onSurface = TextHigh,
    error = Error,
    onError = Color.White
)

// Light Color Scheme (not used but required for MaterialTheme)
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = ConfidenceMedium,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Error,
    onError = Color.White
)

@Composable
fun MatchMindAITheme(
    darkTheme: Boolean = true,  // Force Dark Mode for Cyber-Minimalism
    content: @Composable () -> Unit
) {
    // Always use dark theme for Cyber-Minimalism design
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
