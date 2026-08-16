package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Dialog shell using the low-level [Dialog] composable (scrim + dismiss-on-
 * outside-tap for free) with a [BrutalCard] as its content, replacing
 * default M3 AlertDialog's rounded/tonal/borderless look.
 */
@Composable
fun BrutalDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    body: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        val colors = MaterialTheme.colorScheme
        BrutalCard(cornerRadius = 8.dp, borderWidth = 3.dp, background = colors.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = colors.onSurface)
                Column(Modifier.padding(top = 12.dp, bottom = 16.dp)) { body() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    BrutalCard(onClick = onDismissRequest, cornerRadius = 4.dp, borderWidth = 2.dp, background = colors.surfaceVariant) {
                        Text(dismissLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    BrutalCard(onClick = onConfirm, cornerRadius = 4.dp, borderWidth = 2.dp, background = colors.primary) {
                        Text(confirmLabel, fontWeight = FontWeight.Black, fontSize = 12.sp, color = colors.onPrimary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}
