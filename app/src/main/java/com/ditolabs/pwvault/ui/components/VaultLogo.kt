package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

/**
 * Simple compass-and-checkmark vault mark: a ring (the vault) with four tick
 * marks (a dial) and a checkmark inside (verified/unlocked). Replaces the
 * stock ic_lock_lock placeholder noted as an open item in DESIGN.md — this is
 * still a placeholder for a real designed icon, just a less generic one.
 */
@Composable
fun VaultLogo(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier = modifier) {
        scale(scale = size.width / 100f, pivot = Offset.Zero) {
            drawCircle(color = color, radius = 42f, center = Offset(50f, 50f), style = Stroke(width = 7f))
            drawLine(color = color, start = Offset(50f, 12f), end = Offset(50f, 20f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(12f, 50f), end = Offset(20f, 50f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(80f, 50f), end = Offset(88f, 50f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(50f, 80f), end = Offset(50f, 88f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawPath(
                path = Path().apply { moveTo(30f, 36f); lineTo(50f, 66f); lineTo(70f, 36f) },
                color = color,
                style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
