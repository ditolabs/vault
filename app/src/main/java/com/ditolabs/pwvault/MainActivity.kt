package com.ditolabs.pwvault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ditolabs.pwvault.data.AppLanguage
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.data.ExportImport
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.i18n.Strings
import com.ditolabs.pwvault.ui.VaultViewModel
import com.ditolabs.pwvault.ui.screens.EntryEditScreen
import com.ditolabs.pwvault.ui.screens.LockScreen
import com.ditolabs.pwvault.ui.screens.SettingsScreen
import com.ditolabs.pwvault.ui.screens.VaultListScreen
import com.ditolabs.pwvault.ui.theme.PwVaultTheme

class MainActivity : FragmentActivity() {

    companion object {
        const val EXTRA_AUTOFILL_UNLOCK = "autofill_unlock"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blocks screenshots and screen recording of the whole app (including
        // the recent-apps thumbnail) — a password manager's screens shouldn't
        // be capturable, no toggle needed for this one.
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        setContent { PwVaultApp() }
    }
}

@Composable
private fun PwVaultApp() {
    val vm: VaultViewModel = viewModel()
    val context = LocalContext.current
    val nav = rememberNavController()

    val language by vm.language.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val unlocked by vm.unlocked.collectAsState()
    val entries by vm.entries.collectAsState()
    val autoLockDelay by vm.autoLockDelay.collectAsState()

    // Auto-lock: remember when the app went to background, and if we come
    // back after the configured delay (or immediately, for IMMEDIATE), force
    // a re-unlock. NEVER disables this entirely.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, autoLockDelay, unlocked) {
        var backgroundedAt = 0L
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    backgroundedAt = System.currentTimeMillis()
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    if (unlocked && backgroundedAt > 0 && autoLockDelay != com.ditolabs.pwvault.data.AutoLockDelay.NEVER) {
                        val elapsedMinutes = (System.currentTimeMillis() - backgroundedAt) / 60000.0
                        val threshold = autoLockDelay.minutes
                        if (elapsedMinutes >= threshold) {
                            vm.lock()
                            nav.navigate("lock") { popUpTo(0) }
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PwVaultTheme(themeMode) {
        CompositionLocalProvider(LocalStrings provides Strings(language)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                NavHost(navController = nav, startDestination = if (vm.vaultExists()) "lock" else "onboarding") {
                    composable("onboarding") {
                        com.ditolabs.pwvault.ui.screens.OnboardingScreen(onGetStarted = { nav.navigate("lock") { popUpTo("onboarding") { inclusive = true } } })
                    }
                    composable("lock") {
                        val pinAttempts = vm.pinUnlock.currentLockout()
                        LockScreen(
                            isCreatingVault = !vm.vaultExists(),
                            pinEnabled = vm.pinUnlock.isEnabled(),
                            biometricEnabled = vm.biometricUnlock.isEnabled(),
                            pinAttemptsLeft = pinAttempts.attemptsLeft,
                            pinLockedUntil = pinAttempts.lockedUntilMillis,
                            language = language,
                            themeMode = themeMode,
                            onToggleLanguage = { vm.setLanguage(if (language == AppLanguage.ID) AppLanguage.EN else AppLanguage.ID) },
                            onToggleTheme = { vm.setThemeMode(if (themeMode == com.ditolabs.pwvault.data.ThemeMode.DARK) com.ditolabs.pwvault.data.ThemeMode.LIGHT else com.ditolabs.pwvault.data.ThemeMode.DARK) },
                            onUnlockPassword = { pw ->
                                val ok = vm.unlockWithPassword(pw.toCharArray())
                                if (ok) nav.navigate("list") { popUpTo("lock") { inclusive = true } }
                                ok
                            },
                            onUnlockPin = { pin ->
                                val ok = vm.unlockWithPin(pin)
                                if (ok) nav.navigate("list") { popUpTo("lock") { inclusive = true } }
                                ok
                            },
                            onUnlockBiometric = {
                                triggerBiometricUnlock(context, vm) {
                                    nav.navigate("list") { popUpTo("lock") { inclusive = true } }
                                }
                            },
                        )
                    }
                    composable("list") {
                        VaultListScreen(
                            entries = entries,
                            onOpenEntry = { nav.navigate("edit/${it.id}") },
                            onAddEntry = { nav.navigate("edit/new") },
                            onOpenSettings = { nav.navigate("settings") },
                            onCopyPassword = { entry -> copyToClipboard(context, entry.password) },
                            onCleanEmptyPasswords = {
                                val removed = vm.cleanEmptyPasswords()
                                android.widget.Toast.makeText(context, "$removed ${Strings(language)["cleaned_empty_toast"]}", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                    composable(
                        "edit/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")
                        val existing = entries.firstOrNull { it.id == id }
                        EntryEditScreen(
                            existing = existing,
                            onBack = { nav.popBackStack() },
                            onSave = { vm.addOrUpdateEntry(it) },
                            onDelete = { vm.deleteEntry(it) },
                        )
                    }
                    composable("settings") {
                        SettingsScreenRoute(vm, nav, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreenRoute(vm: VaultViewModel, nav: androidx.navigation.NavHostController, onBack: () -> Unit) {
    val context = LocalContext.current
    val language by vm.language.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val autoLockDelay by vm.autoLockDelay.collectAsState()
    var pinEnabled by remember { mutableStateOf(vm.pinUnlock.isEnabled()) }
    var biometricEnabled by remember { mutableStateOf(vm.biometricUnlock.isEnabled()) }
    var showPinSetup by remember { mutableStateOf(false) }
    val entries by vm.entries.collectAsState()
    val securityFindings = remember(entries) { vm.securityFindings() }

    val exportJsonLauncher = rememberLauncherForExport(context, "vault-export.json") {
        ExportImport.toJson(entries)
    }
    val exportCsvLauncher = rememberLauncherForExport(context, "vault-export.csv") {
        ExportImport.toCsv(entries)
    }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val text = stream.readBytes().toString(Charsets.UTF_8)
            val imported = if (text.trimStart().startsWith("{")) ExportImport.fromJson(text) else ExportImport.fromCsv(text)
            vm.importEntries(imported)
        }
    }

    // Backup writes the RAW ENCRYPTED vault.enc bytes — never decrypted plaintext.
    // Safe to hand to Drive/email precisely because it stays unreadable without
    // this vault's master password/PIN.
    val backupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.use { it.write(vm.repo.rawEncryptedBytes()) }
    }
    var pendingRestoreBytes by remember { mutableStateOf<ByteArray?>(null) }
    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)?.use { pendingRestoreBytes = it.readBytes() }
    }

    SettingsScreen(
        language = language,
        themeMode = themeMode,
        pinEnabled = pinEnabled,
        biometricEnabled = biometricEnabled,
        autoLockDelay = autoLockDelay,
        securityFindings = securityFindings,
        onBack = onBack,
        onLanguageChange = { vm.setLanguage(it) },
        onThemeChange = { vm.setThemeMode(it) },
        onAutoLockChange = { vm.setAutoLockDelay(it) },
        onTogglePin = { enabled ->
            if (enabled) showPinSetup = true
            else { vm.pinUnlock.disable(); pinEnabled = false }
        },
        onToggleBiometric = { enabled ->
            if (enabled) {
                val pw = vm.currentMasterPassword()
                if (pw != null) {
                    val activity = context as FragmentActivity
                    val cipher = vm.biometricUnlock.prepareEncryptCipher()
                    val prompt = BiometricPrompt(
                        activity, ContextCompat.getMainExecutor(context),
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                result.cryptoObject?.cipher?.let { vm.biometricUnlock.persistEncrypted(it, pw) }
                                biometricEnabled = true
                            }
                        }
                    )
                    val info = BiometricPrompt.PromptInfo.Builder().setTitle("Konfirmasi").setNegativeButtonText("Batal").build()
                    prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
                }
            } else { vm.biometricUnlock.disable(); biometricEnabled = false }
        },
        onExportJson = { exportJsonLauncher() },
        onExportCsv = { exportCsvLauncher() },
        onImport = { importLauncher.launch(arrayOf("*/*")) },
        onBackup = { backupLauncher.launch("pwvault-backup.enc") },
        onRestore = { restoreLauncher.launch(arrayOf("*/*")) },
    )

    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirm = { pin ->
                vm.currentMasterPassword()?.let { vm.pinUnlock.setup(pin, it) }
                pinEnabled = true
                showPinSetup = false
            }
        )
    }

    pendingRestoreBytes?.let { bytes ->
        val s = LocalStrings.current
        AlertDialog(
            onDismissRequest = { pendingRestoreBytes = null },
            title = { Text(s["restore_confirm_title"]) },
            text = { Text(s["restore_confirm_message"]) },
            confirmButton = {
                TextButton(onClick = {
                    vm.repo.restoreFromRawBytes(bytes)
                    pendingRestoreBytes = null
                    vm.lock()
                    nav.navigate("lock") { popUpTo(0) }
                }) { Text(s["restore_confirm_action"], color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingRestoreBytes = null }) { Text(s["cancel"]) } },
        )
    }
}

@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s["set_pin"]) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                label = { Text("PIN (6 digit)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
            )
        },
        confirmButton = { TextButton(onClick = { if (pin.length == 6) onConfirm(pin) }, enabled = pin.length == 6) { Text(s["save"]) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun rememberLauncherForExport(context: android.content.Context, defaultName: String, content: () -> String): () -> Unit {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.use { it.write(content().toByteArray(Charsets.UTF_8)) }
    }
    return { launcher.launch(defaultName) }
}

private fun copyToClipboard(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("password", text))
}

private fun triggerBiometricUnlock(context: android.content.Context, vm: VaultViewModel, onSuccess: () -> Unit) {
    val activity = context as? FragmentActivity ?: return
    val cipher = try { vm.biometricUnlock.prepareDecryptCipher() } catch (e: Exception) { return }
    val prompt = BiometricPrompt(
        activity, ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val resultCipher = result.cryptoObject?.cipher ?: return
                val pw = vm.biometricUnlock.recoverPassword(resultCipher)
                if (vm.unlockWithPassword(pw)) onSuccess()
            }
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder().setTitle("Unlock PwVault").setNegativeButtonText("Batal").build()
    prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
}
