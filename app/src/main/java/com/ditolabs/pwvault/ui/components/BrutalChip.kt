package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Bordered, hard-shadow selectable chip — replaces default M3 FilterChip's soft pill. */
@Composable
fun BrutalChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    BrutalCard(
        onClick = onClick,
        cornerRadius = 4.dp,
        borderWidth = 2.dp,
        background = if (selected) colors.outline else colors.surface,
        modifier = modifier,
    ) {
        Text(
            label,
            color = if (selected) colors.background else colors.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
