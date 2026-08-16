package com.ditolabs.pwvault.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Square bordered toggle, hand-built — replaces default M3 Switch, whose
 * pill-shaped track/thumb can't be reshaped into the app's flat-bordered
 * language via its own color/shape parameters.
 */
@Composable
fun BrutalSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val thumbOffset by animateDpAsState(targetValue = if (checked) 24.dp else 2.dp, animationSpec = tween(150), label = "switchThumb")
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier
            .size(width = 50.dp, height = 28.dp)
            .clickable(interactionSource = interactionSource, indication = null) { onCheckedChange(!checked) }
            .background(if (checked) colors.primary else colors.surfaceVariant, RoundedCornerShape(4.dp))
            .border(2.5.dp, colors.outline, RoundedCornerShape(4.dp)),
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset, y = 2.dp)
                .size(20.dp)
                .background(if (checked) colors.onPrimary else colors.background, RoundedCornerShape(2.dp))
                .border(2.dp, colors.outline, RoundedCornerShape(2.dp)),
        )
    }
}
