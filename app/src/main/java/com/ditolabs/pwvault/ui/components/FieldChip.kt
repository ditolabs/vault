package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A small dark label chip that sits cut into the top border of a bordered
 * field — the floating-label-chip motif from the neon-brutalist mockup
 * ("worth taking" #1). Wraps [content] in a BrutalCard-style bordered box
 * with the chip overlapping the top edge.
 */
@Composable
fun FieldChip(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.padding(top = 10.dp)) {
        Box(
            Modifier
                .border(2.5.dp, colors.outline, RoundedCornerShape(4.dp))
                .background(colors.surface, RoundedCornerShape(4.dp))
                .padding(top = 12.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
        ) {
            content()
        }
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = (-10).dp)
                .background(colors.outline, RoundedCornerShape(2.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                label.uppercase(),
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = colors.background, // background color reads as "cutout" against the chip
            )
        }
    }
}
