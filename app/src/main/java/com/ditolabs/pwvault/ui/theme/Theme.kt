package com.ditolabs.pwvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/** User-facing theme choice (DESIGN.md: system/light/dark, switchable in Settings). */
enum class ThemePreference { SYSTEM, LIGHT, DARK }

val LocalPwVaultColors = compositionLocalOf { DarkPwVaultColors }

@Composable
fun PwVaultTheme(
    preference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDark = when (preference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val pwColors = if (useDark) DarkPwVaultColors else LightPwVaultColors

    // Material3's own color scheme is kept minimal — most surfaces pull from
    // LocalPwVaultColors directly so the brutal hard-border/shadow look does
    // not get diluted by default Material elevation/tonal-overlay behavior.
    val materialScheme = if (useDark) {
        darkColorScheme(
            primary = pwColors.accent,
            background = pwColors.background,
            surface = pwColors.surface,
            onBackground = pwColors.text,
            onSurface = pwColors.text,
            error = pwColors.danger,
        )
    } else {
        lightColorScheme(
            primary = pwColors.accent,
            background = pwColors.background,
            surface = pwColors.surface,
            onBackground = pwColors.text,
            onSurface = pwColors.text,
            error = pwColors.danger,
        )
    }

    CompositionLocalProvider(LocalPwVaultColors provides pwColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = PwVaultTypography,
            content = content,
        )
    }
}
