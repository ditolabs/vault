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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.ui.theme.LocalPwVaultColors

/**
 * The app's one repeated identity motif (DESIGN.md): a thick border plus a
 * solid, non-blurred offset "shadow" instead of Material elevation. On press
 * the offset shrinks from 3dp to 1dp so the card visibly sits down — this is
 * honest tap feedback, not decoration (ANTISLOP purpose-gate).
 *
 * @param onClick null renders a static (non-interactive) card — per R-26,
 *   a card with no real action should not fake being tappable.
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    background: Color? = null,
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 2.5.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalPwVaultColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shadowOffset by animateDpAsState(
        targetValue = if (pressed) 1.dp else 3.dp,
        animationSpec = tween(90),
        label = "brutalShadowOffset",
    )
    val shape = RoundedCornerShape(cornerRadius)
    val fillColor = background ?: colors.surface

    // indication = null: the shadow-compress *is* the press feedback, a
    // Material ripple on top of it would be a second, redundant signal.
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else Modifier

    Box(modifier = modifier) {
        // Hard offset shadow layer — flat color, no blur.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(colors.line, shape)
        )
        // Foreground card.
        Box(
            modifier = Modifier
                .then(clickableModifier)
                .background(fillColor, shape)
                .border(borderWidth, colors.line, shape)
        ) {
            content()
        }
    }
}
