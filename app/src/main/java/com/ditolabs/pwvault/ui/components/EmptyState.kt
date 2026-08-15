package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Dashed rectangle border — Compose has no built-in dashed Modifier.border, so this draws it directly. */
fun Modifier.dashedBorder(color: Color, strokeWidthDp: androidx.compose.ui.unit.Dp = 3.dp, cornerRadiusDp: androidx.compose.ui.unit.Dp = 8.dp) =
    this.drawBehind {
        val strokeWidthPx = strokeWidthDp.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
            ),
            cornerRadius = CornerRadius(cornerRadiusDp.toPx()),
        )
    }

/**
 * Empty-state block with a dashed border — "worth taking" #2 from the
 * neon-brutalist mockup. Used when the vault (or a filtered view of it)
 * has no entries.
 */
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .dashedBorder(colors.outline)
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrutalCard(cornerRadius = 4.dp, borderWidth = 2.5.dp, background = colors.secondary, modifier = Modifier.size(56.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { icon() }
        }
        Column(Modifier.padding(top = 16.dp)) {
            Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.Center, color = colors.onBackground)
        }
        Column(Modifier.padding(top = 6.dp, bottom = if (ctaLabel != null) 20.dp else 0.dp)) {
            Text(body, fontSize = 12.sp, textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
        }
        if (ctaLabel != null && onCta != null) {
            BrutalCard(onClick = onCta, cornerRadius = 4.dp, borderWidth = 2.dp, background = colors.primary) {
                Text(
                    ctaLabel, fontWeight = FontWeight.Black, fontSize = 12.sp, color = colors.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
