package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.data.AppLanguage
import com.ditolabs.pwvault.data.ThemeMode
import com.ditolabs.pwvault.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    onBack: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
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
                OutlinedButton(onClick = onExportJson) { Text("${s["export_vault"]} (JSON)") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onExportCsv) { Text("${s["export_vault"]} (CSV)") }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImport) { Text(s["import_vault"]) }

            Spacer(Modifier.height(28.dp))
            SectionLabel(s["backup_vault"])
            Text(s["backup_note"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
            Row {
                Button(onClick = onBackup) { Text(s["backup_vault"]) }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onRestore,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(s["restore_backup"]) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
}
