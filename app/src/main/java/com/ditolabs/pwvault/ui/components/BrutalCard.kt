package com.ditolabs.pwvault.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's repeated structural motif: a thick border plus a solid,
 * non-blurred offset "shadow" instead of Material elevation. On press the
 * offset shrinks from 3dp to 1dp so the card visibly sits down — real tap
 * feedback, not decoration. Colors come from MaterialTheme.colorScheme
 * (outline = border/shadow, surface = fill) rather than a separate color
 * system, since this app already has one and the two would drift.
 *
 * Applied selectively, not everywhere: primary cards, buttons, and pill
 * badges get the full border+shadow treatment. Small repeated circular
 * controls (PIN keys, biometric icon) keep a plain thick border with no
 * shadow — a hard offset shadow on a dozen small tappable circles reads as
 * noisy rather than tactile (see DESIGN.md's brutalist-merge note).
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    background: Color? = null,
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 2.5.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.outline
    val fillColor = background ?: MaterialTheme.colorScheme.surface
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shadowOffset by animateDpAsState(
        targetValue = if (pressed && enabled) 1.dp else 3.dp,
        animationSpec = tween(90),
        label = "brutalShadowOffset",
    )
    val shape = RoundedCornerShape(cornerRadius)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    } else Modifier

    Box(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(lineColor, shape)
        )
        Box(
            modifier = Modifier
                .then(clickableModifier)
                .background(fillColor, shape)
                .border(borderWidth, lineColor, shape)
        ) {
            content()
        }
    }
}
