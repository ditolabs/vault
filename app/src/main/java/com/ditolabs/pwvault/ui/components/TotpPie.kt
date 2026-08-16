package com.ditolabs.pwvault.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Ring countdown for a TOTP code's remaining seconds in its 30s period —
 * "worth taking" #3 from the neon-brutalist mockup, replacing the earlier
 * plain "Xs" text badge. Turns [dangerColor] under 5 seconds remaining, same
 * threshold as the mockup.
 */
@Composable
fun TotpPie(
    secondsLeft: Int,
    periodSeconds: Int = 30,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 36.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp,
) {
    val colors = MaterialTheme.colorScheme
    val dangerColor = colors.error
    val normalColor = colors.onSurface
    val trackColor = colors.outline.copy(alpha = 0.18f)
    val isDanger = secondsLeft <= 5
    val target = secondsLeft.toFloat() / periodSeconds.toFloat()
    val progress by animateFloatAsState(targetValue = target, animationSpec = tween(280), label = "totpProgress")

    Canvas(modifier = modifier.size(diameter)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
        )
        drawArc(
            color = if (isDanger) dangerColor else normalColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = stroke,
        )
    }
}
