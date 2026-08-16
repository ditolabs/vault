package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Solid-color header with a thick bottom border, replacing the default
 * (tonal, borderless) M3 TopAppBar everywhere in the app. A leading and/or
 * trailing icon slot render as small bordered squares (same "cutout" icon-
 * button treatment used elsewhere), matching the mockup's header bar.
 */
@Composable
fun BrutalTopBar(
    title: String,
    background: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(background)
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) leading() else Box(Modifier.size(40.dp))
            Text(
                title,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                color = contentColor,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                maxLines = 1,
            )
            if (trailing != null) trailing() else Box(Modifier.size(40.dp))
        }
        Box(Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomStart).background(MaterialTheme.colorScheme.outline))
    }
}

/** Small bordered icon-button square used inside [BrutalTopBar] and similar chrome. */
@Composable
fun BrutalIconSlot(
    onClick: () -> Unit,
    background: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    BrutalCard(onClick = onClick, background = background, cornerRadius = 4.dp, borderWidth = 2.dp) {
        Box(Modifier.size(40.dp).padding(9.dp)) { content() }
    }
}
