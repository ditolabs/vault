package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.background
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
import com.ditolabs.pwvault.crypto.PasswordGenerator
import com.ditolabs.pwvault.crypto.Totp
import com.ditolabs.pwvault.data.Categories
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.IconContentCopy
import com.ditolabs.pwvault.ui.components.IconRefresh
import com.ditolabs.pwvault.ui.components.IconVisibility
import com.ditolabs.pwvault.ui.components.BrutalCard
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
    var totpSecret by remember { mutableStateOf(existing?.totpSecret ?: "") }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var showGenerator by remember { mutableStateOf(false) }

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
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { showGenerator = true }) {
                IconRefresh(modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(s["generate_password"], style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(6.dp))

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

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = totpSecret, onValueChange = { totpSecret = it },
                label = { Text(s["totp_secret_label"]) },
                placeholder = { Text("JBSWY3DPEHPK3PXP") },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )
            if (totpSecret.isNotBlank() && Totp.isValidSecret(totpSecret)) {
                Spacer(Modifier.height(8.dp))
                TotpCodeCard(totpSecret, clipboard)
            } else if (totpSecret.isNotBlank()) {
                Text(s["totp_invalid"], color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(24.dp))
            Row {
                BrutalCard(
                    onClick = {
                        onSave(
                            Entry(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                title = title, username = username, password = password, category = category,
                                totpSecret = totpSecret,
                                updatedAtMillis = System.currentTimeMillis(),
                            )
                        )
                        onBack()
                    },
                    enabled = title.isNotBlank() && password.isNotBlank(),
                    background = MaterialTheme.colorScheme.primary,
                    cornerRadius = 4.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        s["save"],
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    )
                }

                if (existing != null) {
                    Spacer(Modifier.width(10.dp))
                    BrutalCard(
                        onClick = { onDelete(existing.id); onBack() },
                        background = MaterialTheme.colorScheme.error,
                        cornerRadius = 4.dp,
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(s["delete"], color = MaterialTheme.colorScheme.onError, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showGenerator) {
        PasswordGeneratorDialog(
            onDismiss = { showGenerator = false },
            onUse = { generated -> password = generated; showGenerator = false },
        )
    }
}

@Composable
private fun TotpCodeCard(secret: String, clipboard: ClipboardManager) {
    val s = LocalStrings.current
    var code by remember { mutableStateOf(Totp.currentCode(secret)) }
    var secondsLeft by remember { mutableStateOf(Totp.secondsRemaining()) }

    LaunchedEffect(secret) {
        while (true) {
            code = Totp.currentCode(secret)
            secondsLeft = Totp.secondsRemaining()
            kotlinx.coroutines.delay(1000)
        }
    }

    BrutalCard(cornerRadius = 4.dp, borderWidth = 2.dp, background = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(s["totp_code_label"], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    code.chunked(3).joinToString(" "),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text("${secondsLeft}s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
                IconContentCopy(modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PasswordGeneratorDialog(onDismiss: () -> Unit, onUse: (String) -> Unit) {
    val s = LocalStrings.current
    var length by remember { mutableStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var generated by remember { mutableStateOf(PasswordGenerator.generate(16, true, true, true)) }

    fun regenerate() { generated = PasswordGenerator.generate(length.toInt(), useUpper, useDigits, useSymbols) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s["generate_password"]) },
        text = {
            Column {
                BrutalCard(cornerRadius = 4.dp, borderWidth = 2.dp, background = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(generated, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { regenerate() }) { IconRefresh(modifier = Modifier.size(18.dp)) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("${s["password_length"]}: ${length.toInt()}", style = MaterialTheme.typography.labelMedium)
                Slider(value = length, onValueChange = { length = it; regenerate() }, valueRange = 8f..32f, steps = 23)
                CheckboxRow(s["use_uppercase"], useUpper) { useUpper = it; regenerate() }
                CheckboxRow(s["use_digits"], useDigits) { useDigits = it; regenerate() }
                CheckboxRow(s["use_symbols"], useSymbols) { useSymbols = it; regenerate() }
            }
        },
        confirmButton = { TextButton(onClick = { onUse(generated) }) { Text(s["use_password"]) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s["cancel"]) } },
    )
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
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
