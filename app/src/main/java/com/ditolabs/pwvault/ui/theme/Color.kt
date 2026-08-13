package com.ditolabs.pwvault.ui.theme

import androidx.compose.ui.graphics.Color

// Dark ("ink") — see DESIGN.md > Palette
val Ink900 = Color(0xFF0B0D12) // background
val Ink800 = Color(0xFF12141B) // surface
val Ink700 = Color(0xFF171A24) // raised / inputs
val InkLine = Color(0xFF262B38) // borders + hard-shadow color in dark mode
val Paper = Color(0xFFE9EAE2) // text on dark
val Dim = Color(0xFF8B8F9C) // secondary text on dark
val VaultGreen = Color(0xFF3EA87C) // primary accent, dark
val DangerDark = Color(0xFFC96A5A) // destructive, dark

// Light ("paper") — see DESIGN.md > Palette
val LightBg = Color(0xFFF4F3EE)
val LightSurface = Color(0xFFFFFFFF)
val LightLine = Color(0xFFDEDCD3) // borders + hard-shadow color in light mode
val LightText = Color(0xFF14161C)
val LightDim = Color(0xFF6B6F7A)
val GreenOnLight = Color(0xFF2C7A5A)
val DangerOnLight = Color(0xFFA9503F)

/**
 * One semantic set so screens never reach for a raw Color(...) constant directly.
 * Keeps the "max 2-3 core colors + 1 accent" rule (ANTISLOP R-29) enforceable —
 * there is exactly one place accent/danger can be introduced.
 */
data class PwVaultColors(
    val background: Color,
    val surface: Color,
    val raised: Color,
    val line: Color, // also used as the hard-shadow color
    val text: Color,
    val textDim: Color,
    val accent: Color,
    val danger: Color,
    val isDark: Boolean,
)

val DarkPwVaultColors = PwVaultColors(
    background = Ink900,
    surface = Ink800,
    raised = Ink700,
    line = InkLine,
    text = Paper,
    textDim = Dim,
    accent = VaultGreen,
    danger = DangerDark,
    isDark = true,
)

val LightPwVaultColors = PwVaultColors(
    background = LightBg,
    surface = LightSurface,
    raised = LightSurface,
    line = LightLine,
    text = LightText,
    textDim = LightDim,
    accent = GreenOnLight,
    danger = DangerOnLight,
    isDark = false,
)
