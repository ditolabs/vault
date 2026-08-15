package com.ditolabs.pwvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ditolabs.pwvault.data.ThemeMode

// Neon-brutalist palette — adopted from the Gemini mockup direction (see
// DESIGN.md "Brutalist structure merge" addendum; palette override pending
// a DESIGN.md rewrite, tracked separately). Flat, saturated, functional
// color-coding rather than a single muted accent.
val Lime = Color(0xFFB4FF39)
val Lavender = Color(0xFFE5D4FF)
val CyanBg = Color(0xFFE5F9FF)
val Red = Color(0xFFFF4D4D)
val Pink = Color(0xFFEF476F)
val Yellow = Color(0xFFFFD166)
val Blue = Color(0xFF118AB2)
val Green = Color(0xFF06D6A0)
val CreamBg = Color(0xFFFFF9E6)

// Category tag colors — flat across both themes on purpose (informational
// color-coding, not theme-adaptive). Keys match Categories.all id strings
// in data/Entry.kt.
val CategoryColors = mapOf(
    "sosmed" to Lavender,
    "email" to Lime,
    "kerja" to CyanBg,
    "ecommerce" to Yellow,
    "lainnya" to Color(0xFFE8E8E8),
)

private val DarkScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Color.Black,
    background = Color(0xFF0B0B0F),
    onBackground = Color(0xFFF2F2ED),
    surface = Color(0xFF19191F),
    onSurface = Color(0xFFF2F2ED),
    surfaceVariant = Color(0xFF20232A),
    onSurfaceVariant = Color(0xFFB8BAC2),
    outline = Color(0xFFF2F2ED), // border/shadow color — light so it reads against a dark surface
    secondary = Lavender,
    onSecondary = Color.Black,
    tertiary = Yellow,
    onTertiary = Color.Black,
    error = Red,
    onError = Color.Black,
)

private val LightScheme = lightColorScheme(
    primary = Lime,
    onPrimary = Color.Black,
    background = CreamBg,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = CyanBg,
    onSurfaceVariant = Color(0xFF3A3A3A),
    outline = Color.Black,
    secondary = Lavender,
    onSecondary = Color.Black,
    tertiary = Yellow,
    onTertiary = Color.Black,
    error = Red,
    onError = Color.White,
)

@Composable
fun PwVaultTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkScheme else LightScheme,
        content = content
    )
}

