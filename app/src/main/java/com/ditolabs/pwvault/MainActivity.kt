package com.ditolabs.pwvault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ditolabs.pwvault.data.SampleVault
import com.ditolabs.pwvault.data.VaultEntry
import com.ditolabs.pwvault.i18n.Strings
import com.ditolabs.pwvault.ui.screens.EntryDetailScreen
import com.ditolabs.pwvault.ui.screens.LockScreen
import com.ditolabs.pwvault.ui.screens.VaultListScreen
import com.ditolabs.pwvault.ui.theme.LocalPwVaultColors
import com.ditolabs.pwvault.ui.theme.PwVaultTheme

private sealed interface Screen {
    data object Lock : Screen
    data object List : Screen
    data class Detail(val entry: VaultEntry) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PwVaultTheme {
                PwVaultApp()
            }
        }
    }
}

@Composable
private fun PwVaultApp() {
    val context = LocalContext.current
    val colors = LocalPwVaultColors.current
    var screen by remember { mutableStateOf<Screen>(Screen.Lock) }
    val strings = Strings.id // language switching not wired up yet — see DESIGN.md

    fun copyToClipboard(value: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("pwvault", value))
        Toast.makeText(context, strings.copiedToast, Toast.LENGTH_SHORT).show()
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
    ) {
        when (val s = screen) {
            is Screen.Lock -> LockScreen(
                strings = strings,
                onUnlock = { screen = Screen.List },
            )
            is Screen.List -> VaultListScreen(
                strings = strings,
                entries = SampleVault.entries,
                onOpenEntry = { screen = Screen.Detail(it) },
                onNewEntry = { /* TODO: wire up once the vault engine exists */ },
                onCopyUsername = { copyToClipboard(it.username) },
            )
            is Screen.Detail -> EntryDetailScreen(
                strings = strings,
                entry = s.entry,
                onBack = { screen = Screen.List },
                onCopy = { copyToClipboard(it) },
                onEdit = { /* TODO: wire up once the vault engine exists */ },
                onDelete = { screen = Screen.List },
            )
        }
    }
}
