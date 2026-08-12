package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.data.Categories
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.IconContentCopy
import com.ditolabs.pwvault.ui.components.IconVisibility
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditScreen(
    existing: Entry?,
    onBack: () -> Unit,
    onSave: (Entry) -> Unit,
    onDelete: (String) -> Unit,
) {
    val s = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var username by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf(existing?.password ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "lainnya") }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "+" else "${s["edit_title"]} — ${existing.title}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(title, { title = it }, label = { Text(s["title_label"]) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            FieldWithCopy(s["username_label"], username, { username = it }, clipboard, mono = false)
            Spacer(Modifier.height(12.dp))
            FieldWithCopy(s["password_label"], password, { password = it }, clipboard, mono = true, isSecret = true)
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = categoryMenuOpen, onExpandedChange = { categoryMenuOpen = it }) {
                OutlinedTextField(
                    value = Categories.all.firstOrNull { it.first == category }?.second?.first ?: category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s["category_label"]) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = categoryMenuOpen, onDismissRequest = { categoryMenuOpen = false }) {
                    Categories.all.filter { it.first != "semua" }.forEach { (id, labels) ->
                        DropdownMenuItem(text = { Text(labels.first) }, onClick = { category = id; categoryMenuOpen = false })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Button(
                    onClick = {
                        onSave(
                            Entry(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                title = title, username = username, password = password, category = category,
                                updatedAtMillis = System.currentTimeMillis(),
                            )
                        )
                        onBack()
                    },
                    enabled = title.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) { Text(s["save"]) }

                if (existing != null) {
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = { onDelete(existing.id); onBack() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s["delete"])
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldWithCopy(label: String, value: String, onChange: (String) -> Unit, clipboard: ClipboardManager, mono: Boolean, isSecret: Boolean = false) {
    var copied by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(!isSecret) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
            visualTransformation = if (isSecret && !revealed) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default),
            trailingIcon = if (isSecret) {
                {
                    IconButton(onClick = { revealed = !revealed }) {
                        IconVisibility(crossedOut = !revealed, modifier = Modifier.size(22.dp))
                    }
                }
            } else null,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {
            clipboard.setText(AnnotatedString(value))
            copied = true
        }) {
            if (copied) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
            else IconContentCopy(modifier = Modifier.size(20.dp))
        }
    }
    if (copied) LaunchedEffect(value) { kotlinx.coroutines.delay(1500); copied = false }
}
