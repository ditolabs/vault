package com.ditolabs.pwvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ditolabs.pwvault.data.ThemeMode

// Dark tokens — same as original DESIGN.md
val Ink900 = Color(0xFF0B0D12)
val Ink800 = Color(0xFF12141B)
val Ink700 = Color(0xFF171A24)
val InkLine = Color(0xFF262B38)
val Paper = Color(0xFFE9EAE2)
val Dim = Color(0xFF8B8F9C)
val VaultGreen = Color(0xFF3EA87C)
val VaultGreenDim = Color(0xFF2C7A5A)
val Danger = Color(0xFFC96A5A)

// Light tokens — same identity (paper surface, green accent), inverted for contrast.
// Not a deferred toggle: chosen because "brief on the go" use fits both contexts.
val LightBg = Color(0xFFF4F3EE)
val LightSurface = Color(0xFFFFFFFF)
val LightLine = Color(0xFFDEDCD3)
val LightText = Color(0xFF14161C)
val LightDim = Color(0xFF6B6F7A)
val GreenOnLight = Color(0xFF2C7A5A) // darker green for AA contrast on light bg
val DangerOnLight = Color(0xFFA9503F)

private val DarkScheme = darkColorScheme(
    primary = VaultGreen,
    onPrimary = Ink900,
    background = Ink900,
    onBackground = Paper,
    surface = Ink800,
    onSurface = Paper,
    surfaceVariant = Ink700,
    onSurfaceVariant = Dim,
    outline = InkLine,
    error = Danger,
    onError = Ink900,
)

private val LightScheme = lightColorScheme(
    primary = GreenOnLight,
    onPrimary = Color.White,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFEDEBE3),
    onSurfaceVariant = LightDim,
    outline = LightLine,
    error = DangerOnLight,
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
