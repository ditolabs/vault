package com.ditolabs.pwvault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Identity motif (DESIGN.md): entry passwords, master key / PIN fields, and
// TOTP codes render in monospace — disambiguates 1/l/I and 0/O, a functional
// need for a tool whose whole job is displaying secrets accurately.
val SecretMono = FontFamily.Monospace

val PwVaultTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Black, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Black, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 0.3.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp),
)

// Secrets get their own style rather than swapping the whole Typography's
// fontFamily to monospace app-wide — headings/body copy stay in the default
// UI font; only actual secret/code content uses SecretMono (see DESIGN.md).
val SecretTextStyle = TextStyle(fontFamily = SecretMono, fontSize = 15.sp)
val TotpDigitsStyle = TextStyle(fontFamily = SecretMono, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = 4.sp)
