package com.ditolabs.pwvault.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Text input wrapped in a [FieldChip] floating label, replacing
 * `OutlinedTextField` everywhere in the app — its thin rounded M3 border
 * clashed with the thick-border/hard-shadow language used elsewhere.
 */
@Composable
fun BrutalTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    monospace: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: (@Composable () -> Unit)? = null,
) {
    var revealed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    FieldChip(label = label, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                    color = colors.onSurface,
                    fontSize = 14.sp,
                ),
                visualTransformation = if (isPassword && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
                cursorBrush = SolidColor(colors.onSurface),
                modifier = Modifier.weight(1f),
            )
            if (isPassword) {
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { revealed = !revealed }, modifier = Modifier.size(22.dp)) {
                    IconVisibility(crossedOut = !revealed, modifier = Modifier.size(20.dp))
                }
            }
            trailing?.let {
                Spacer(Modifier.width(6.dp))
                it()
            }
        }
    }
}
