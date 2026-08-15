package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ditolabs.pwvault.data.AppLanguage
import com.ditolabs.pwvault.data.AutoLockDelay
import com.ditolabs.pwvault.data.PasswordAudit
import com.ditolabs.pwvault.data.ThemeMode
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.IconShieldCheck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    autoLockDelay: AutoLockDelay,
    securityFindings: List<PasswordAudit.Finding>,
    onBack: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onAutoLockChange: (AutoLockDelay) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImport: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    val s = LocalStrings.current
    Scaffold(topBar = {
        TopAppBar(title = { Text(s["settings"]) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } })
    }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp)) {
            SectionLabel(s["language"])
            Row {
                FilterChip(selected = language == AppLanguage.ID, onClick = { onLanguageChange(AppLanguage.ID) }, label = { Text("Indonesia") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = language == AppLanguage.EN, onClick = { onLanguageChange(AppLanguage.EN) }, label = { Text("English") })
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(s["theme"])
            Row {
                FilterChip(selected = themeMode == ThemeMode.SYSTEM, onClick = { onThemeChange(ThemeMode.SYSTEM) }, label = { Text(s["theme_system"]) })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = themeMode == ThemeMode.LIGHT, onClick = { onThemeChange(ThemeMode.LIGHT) }, label = { Text(s["theme_light"]) })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = themeMode == ThemeMode.DARK, onClick = { onThemeChange(ThemeMode.DARK) }, label = { Text(s["theme_dark"]) })
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(s["auto_lock"])
            Row {
                listOf(
                    AutoLockDelay.IMMEDIATE to s["auto_lock_immediate"],
                    AutoLockDelay.ONE to s["auto_lock_1min"],
                    AutoLockDelay.FIVE to s["auto_lock_5min"],
                    AutoLockDelay.NEVER to s["auto_lock_never"],
                ).forEach { (delay, label) ->
                    FilterChip(
                        selected = autoLockDelay == delay, onClick = { onAutoLockChange(delay) },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(s["settings"])
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s["pin_unlock_toggle"])
                Switch(checked = pinEnabled, onCheckedChange = onTogglePin)
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s["biometric_unlock_toggle"])
                Switch(checked = biometricEnabled, onCheckedChange = onToggleBiometric)
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("${s["export_vault"]} / ${s["import_vault"]}")
            Text(s["export_warning"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 10.dp))
            Row {
                BrutalButtonSmall(onClick = onExportJson, label = "${s["export_vault"]} (JSON)")
                Spacer(Modifier.width(8.dp))
                BrutalButtonSmall(onClick = onExportCsv, label = "${s["export_vault"]} (CSV)")
            }
            Spacer(Modifier.height(8.dp))
            BrutalButtonSmall(onClick = onImport, label = s["import_vault"])

            Spacer(Modifier.height(28.dp))
            SectionLabel(s["backup_vault"])
            Text(s["backup_note"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
            Row {
                BrutalCard(onClick = onBackup, background = MaterialTheme.colorScheme.primary, cornerRadius = 4.dp) {
                    Text(
                        s["backup_vault"], color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                BrutalCard(onClick = onRestore, background = MaterialTheme.colorScheme.error, cornerRadius = 4.dp) {
                    Text(
                        s["restore_backup"], color = MaterialTheme.colorScheme.onError,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                IconShieldCheck(modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(s["security_audit"], style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (securityFindings.isEmpty()) {
                Text(s["security_audit_clean"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            } else {
                securityFindings.forEach { finding ->
                    val issueLabels = finding.issues.joinToString(" · ") {
                        when (it) {
                            PasswordAudit.Issue.WEAK -> s["security_issue_weak"]
                            PasswordAudit.Issue.DUPLICATE -> s["security_issue_duplicate"]
                            PasswordAudit.Issue.EMPTY -> s["security_issue_empty"]
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(finding.entry.title, style = MaterialTheme.typography.bodyMedium)
                        Text(issueLabels, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun BrutalButtonSmall(onClick: () -> Unit, label: String) {
    BrutalCard(onClick = onClick, cornerRadius = 4.dp, borderWidth = 2.dp) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
    }
}
