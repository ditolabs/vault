package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.data.Category
import com.ditolabs.pwvault.data.VaultEntry
import com.ditolabs.pwvault.i18n.StringSet
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.Tag
import com.ditolabs.pwvault.ui.theme.LocalPwVaultColors
import com.ditolabs.pwvault.ui.theme.PwVaultTypography

private fun categoryLabel(c: Category) = when (c) {
    Category.DEV -> "DEV"
    Category.FINANCE -> "FINANCE"
    Category.SOCIAL -> "SOCIAL"
    Category.EMAIL -> "EMAIL"
    Category.OTHER -> "LAINNYA"
}

@Composable
fun VaultListScreen(
    strings: StringSet,
    entries: List<VaultEntry>,
    onOpenEntry: (VaultEntry) -> Unit,
    onNewEntry: () -> Unit,
    onCopyUsername: (VaultEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPwVaultColors.current

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.appName, style = PwVaultTypography.titleLarge, color = colors.text)
            BrutalCard(background = colors.accent, cornerRadius = 4.dp, borderWidth = 2.dp, onClick = onNewEntry) {
                Text(
                    strings.vaultListNewEntry,
                    style = PwVaultTypography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        if (entries.isEmpty()) {
            // R-27: explicit empty state, not just a blank screen.
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(strings.vaultListEmptyTitle, style = PwVaultTypography.titleMedium, color = colors.text)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(strings.vaultListEmptyBody, style = PwVaultTypography.bodyMedium, color = colors.textDim)
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(entries, key = { it.id }) { entry ->
                BrutalCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenEntry(entry) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.title, style = PwVaultTypography.titleMedium, color = colors.text)
                                Spacer(modifier = Modifier.width(6.dp))
                                Tag(categoryLabel(entry.category), colors.raised, colors.text, colors.line)
                                if (entry.hasTotp) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Tag("2FA", colors.accent.copy(alpha = 0.18f), colors.accent, colors.accent)
                                }
                            }
                            Column(modifier = Modifier.padding(top = 6.dp)) {
                                Text(
                                    entry.username,
                                    style = PwVaultTypography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = colors.textDim,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        BrutalCard(
                            background = colors.accent,
                            cornerRadius = 4.dp,
                            borderWidth = 2.dp,
                            onClick = { onCopyUsername(entry) },
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = strings.copiedToast,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp).size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
