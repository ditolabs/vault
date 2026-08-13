package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.ui.theme.PwVaultTypography

/** Small labeled chip for entry category / 2FA indicator. Inline radius (4dp) — see DESIGN.md scale. */
@Composable
fun Tag(text: String, background: Color, textColor: Color, borderColor: Color) {
    Text(
        text = text,
        style = PwVaultTypography.labelSmall,
        color = textColor,
        modifier = Modifier
            .background(background, RoundedCornerShape(4.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}
