package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn replacements for the handful of icons that turned out NOT to be in
 * material-icons-core (Backspace, ContentCopy, DarkMode, Fingerprint, Shield,
 * Visibility, VisibilityOff, WbSunny all failed to resolve there — confirmed by
 * a real compile, not guessed). Drawing these ourselves means no dependency on
 * -extended (which is what bloated the Gemini build) and no more icon-name
 * roulette. Coordinates are on a 24x24 grid to match standard Material sizing.
 */
private fun scale(size: Float) = size / 24f

@Composable
fun IconShield(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val path = Path().apply {
            moveTo(12f * s, 2.5f * s)
            lineTo(19.5f * s, 6f * s)
            lineTo(19.5f * s, 12f * s)
            cubicTo(19.5f * s, 17f * s, 16.3f * s, 20.2f * s, 12f * s, 21.5f * s)
            cubicTo(7.7f * s, 20.2f * s, 4.5f * s, 17f * s, 4.5f * s, 12f * s)
            lineTo(4.5f * s, 6f * s)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round))
    }
}

@Composable
fun IconFingerprint(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val center = Offset(12f * s, 13f * s)
        for (i in 0..3) {
            val r = (4f + i * 2.6f) * s
            drawArc(
                color = tint,
                startAngle = 200f - i * 10f,
                sweepAngle = 220f + i * 8f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = 1.4f * s, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun IconVisibility(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, crossedOut: Boolean = false) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val eyePath = Path().apply {
            moveTo(2f * s, 12f * s)
            cubicTo(4.5f * s, 6.5f * s, 19.5f * s, 6.5f * s, 22f * s, 12f * s)
            cubicTo(19.5f * s, 17.5f * s, 4.5f * s, 17.5f * s, 2f * s, 12f * s)
            close()
        }
        drawPath(eyePath, color = tint, style = Stroke(width = 1.5f * s, cap = StrokeCap.Round))
        drawCircle(color = tint, radius = 2.6f * s, center = Offset(12f * s, 12f * s), style = Stroke(width = 1.5f * s))
        if (crossedOut) {
            drawLine(
                color = tint,
                start = Offset(3f * s, 3f * s),
                end = Offset(21f * s, 21f * s),
                strokeWidth = 1.6f * s,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun IconContentCopy(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        drawRoundRect(
            color = tint,
            topLeft = Offset(8.5f * s, 2.5f * s),
            size = androidx.compose.ui.geometry.Size(13f * s, 13f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * s, 2f * s),
            style = Stroke(width = 1.4f * s),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(2.5f * s, 8.5f * s),
            size = androidx.compose.ui.geometry.Size(13f * s, 13f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * s, 2f * s),
            style = Stroke(width = 1.4f * s),
        )
    }
}

@Composable
fun IconBackspace(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val path = Path().apply {
            moveTo(9f * s, 4f * s)
            lineTo(21f * s, 4f * s)
            lineTo(21f * s, 20f * s)
            lineTo(9f * s, 20f * s)
            lineTo(2f * s, 12f * s)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 1.5f * s, cap = StrokeCap.Round))
        drawLine(color = tint, start = Offset(12f * s, 9f * s), end = Offset(18f * s, 15f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(18f * s, 9f * s), end = Offset(12f * s, 15f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
    }
}

@Composable
fun IconSun(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val center = Offset(12f * s, 12f * s)
        drawCircle(color = tint, radius = 4f * s, center = center, style = Stroke(width = 1.5f * s))
        val rayLen = 2.5f * s
        for (angleDeg in listOf(0, 45, 90, 135, 180, 225, 270, 315)) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val inner = Offset(center.x + (6.5f * s * Math.cos(rad)).toFloat(), center.y + (6.5f * s * Math.sin(rad)).toFloat())
            val outer = Offset(center.x + ((6.5f * s + rayLen) * Math.cos(rad)).toFloat(), center.y + ((6.5f * s + rayLen) * Math.sin(rad)).toFloat())
            drawLine(color = tint, start = inner, end = outer, strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun IconMoon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val path = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(Offset(3f * s, 3f * s), androidx.compose.ui.geometry.Size(16f * s, 16f * s)))
            fillType = PathFillType.EvenOdd
            addOval(androidx.compose.ui.geometry.Rect(Offset(7f * s, 2f * s), androidx.compose.ui.geometry.Size(16f * s, 16f * s)))
        }
        drawPath(path, color = tint)
    }
}

@Composable
fun IconKey(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        drawCircle(color = tint, radius = 3.2f * s, center = Offset(6.5f * s, 12f * s), style = Stroke(width = 1.5f * s))
        drawLine(color = tint, start = Offset(9.5f * s, 12f * s), end = Offset(21f * s, 12f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(16.5f * s, 12f * s), end = Offset(16.5f * s, 16f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(19.5f * s, 12f * s), end = Offset(19.5f * s, 15f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
    }
}

@Composable
fun IconBackupCloud(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier) {
        val s = scale(this.size.minDimension)
        val path = Path().apply {
            moveTo(7f * s, 18f * s)
            cubicTo(4f * s, 18f * s, 2f * s, 16f * s, 2f * s, 13.3f * s)
            cubicTo(2f * s, 10.9f * s, 3.8f * s, 9f * s, 6f * s, 8.7f * s)
            cubicTo(6.7f * s, 5.9f * s, 9.2f * s, 4f * s, 12f * s, 4f * s)
            cubicTo(15.3f * s, 4f * s, 18f * s, 6.6f * s, 18.2f * s, 9.8f * s)
            cubicTo(20.4f * s, 10.3f * s, 22f * s, 12.1f * s, 22f * s, 14.3f * s)
            cubicTo(22f * s, 16.6f * s, 20f * s, 18f * s, 17.5f * s, 18f * s)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 1.5f * s, cap = StrokeCap.Round))
        drawLine(color = tint, start = Offset(12f * s, 11f * s), end = Offset(12f * s, 20f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(9f * s, 14f * s), end = Offset(12f * s, 11f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(15f * s, 14f * s), end = Offset(12f * s, 11f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
    }
}
