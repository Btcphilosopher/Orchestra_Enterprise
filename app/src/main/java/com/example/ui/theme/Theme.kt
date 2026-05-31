package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Enterprise Dark Palette (Default/Focal theme)
private val DarkColorScheme = darkColorScheme(
    primary = NeonTeal,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = CorporateSlateAccent,
    onPrimaryContainer = Color.White,
    secondary = NeonOrange,
    onSecondary = Color(0xFF0F172A),
    background = CorporateSlateDarkest,
    onBackground = Color(0xFFF1F5F9),
    surface = CorporateSlateDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CorporateSlateAccent,
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = EnterpriseTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = EnterpriseTealDark,
    secondary = AccentOrange,
    onSecondary = Color.White,
    background = SlateBackground,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF94A3B8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for premium executive look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
