package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.data.VaultEntry
import com.ditolabs.pwvault.i18n.StringSet
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.PwVaultIcons
import com.ditolabs.pwvault.ui.theme.LocalPwVaultColors
import com.ditolabs.pwvault.ui.theme.PwVaultTypography
import com.ditolabs.pwvault.ui.theme.SecretTextStyle
import com.ditolabs.pwvault.ui.theme.TotpDigitsStyle
import kotlinx.coroutines.delay

@Composable
fun EntryDetailScreen(
    strings: StringSet,
    entry: VaultEntry,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onEdit: (VaultEntry) -> Unit,
    onDelete: (VaultEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPwVaultColors.current
    var passwordRevealed by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                PwVaultIcons.ArrowBack,
                contentDescription = null,
                tint = colors.text,
                modifier = Modifier.size(22.dp).padding(end = 4.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.appName, style = PwVaultTypography.titleMedium, color = colors.text)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row {
                com.ditolabs.pwvault.ui.components.Tag(
                    entry.category.name,
                    colors.raised,
                    colors.text,
                    colors.line,
                )
            }
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)) {
                Text(entry.title, style = PwVaultTypography.headlineLarge, color = colors.text)
            }

            SecretRow(
                label = strings.usernameLabel,
                value = entry.username,
                masked = false,
                colors = colors,
                onCopy = { onCopy(entry.username) },
            )
            Spacer(modifier = Modifier.height(16.dp))

            SecretRow(
                label = strings.passwordLabel,
                value = entry.password,
                masked = !passwordRevealed,
                colors = colors,
                onToggleReveal = { passwordRevealed = !passwordRevealed },
                onCopy = { onCopy(entry.password) },
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (entry.hasTotp) {
                TotpCard(strings = strings, onCopy = onCopy)
                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BrutalCard(modifier = Modifier.weight(1f), onClick = { onEdit(entry) }) {
                    Text(
                        strings.editEntry,
                        style = PwVaultTypography.titleMedium,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                    )
                }
                BrutalCard(background = colors.danger, modifier = Modifier.weight(1f), onClick = { confirmingDelete = true }) {
                    Text(
                        strings.deleteEntry,
                        style = PwVaultTypography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                    )
                }
            }
        }
    }

    // R-26: delete is destructive and irreversible for this entry, so it
    // cannot be a single unconfirmed tap (mirrors the same rule DESIGN.md
    // applies to vault restore).
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(strings.deleteEntry) },
            text = { Text(entry.title) },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDelete = false
                    onDelete(entry)
                }) { Text(strings.deleteEntry, color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun SecretRow(
    label: String,
    value: String,
    masked: Boolean,
    colors: com.ditolabs.pwvault.ui.theme.PwVaultColors,
    onToggleReveal: (() -> Unit)? = null,
    onCopy: () -> Unit,
) {
    Column {
        Text(label, style = PwVaultTypography.labelLarge, color = colors.textDim)
        Spacer(modifier = Modifier.height(6.dp))
        BrutalCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (masked) "•".repeat(value.length.coerceAtLeast(8)) else value,
                    style = SecretTextStyle,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleReveal != null) {
                        Icon(
                            imageVector = if (masked) PwVaultIcons.Visibility else PwVaultIcons.VisibilityOff,
                            contentDescription = if (masked) "Tampilkan password" else "Sembunyikan password",
                            tint = colors.textDim,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onToggleReveal,
                                ),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    BrutalCard(background = colors.accent, cornerRadius = 4.dp, borderWidth = 2.dp, onClick = onCopy) {
                        Icon(
                            PwVaultIcons.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotpCard(strings: StringSet, onCopy: (String) -> Unit) {
    val colors = LocalPwVaultColors.current
    // Placeholder period countdown for the scaffold — real TOTP generation
    // is pending the vault engine (see DESIGN.md known open items). The
    // countdown itself is real UI state, not a static fake number.
    var secondsLeft by remember { mutableIntStateOf(30) }
    val code = "829104"
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsLeft = if (secondsLeft <= 1) 30 else secondsLeft - 1
        }
    }
    Column {
        Text(strings.totpLabel, style = PwVaultTypography.labelLarge, color = colors.textDim)
        Spacer(modifier = Modifier.height(6.dp))
        BrutalCard(background = colors.raised, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                BrutalCard(background = colors.surface, cornerRadius = 4.dp, borderWidth = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${code.take(3)} ${code.takeLast(3)}",
                        style = TotpDigitsStyle,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrutalCard(background = colors.accent, cornerRadius = 4.dp, borderWidth = 2.dp, modifier = Modifier.weight(1f)) {
                        Text(
                            "⏱ ${secondsLeft}s",
                            style = PwVaultTypography.labelLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                        )
                    }
                    BrutalCard(cornerRadius = 4.dp, borderWidth = 2.dp, onClick = { onCopy(code) }) {
                        Icon(
                            PwVaultIcons.ContentCopy,
                            contentDescription = null,
                            tint = colors.text,
                            modifier = Modifier.padding(8.dp).size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
