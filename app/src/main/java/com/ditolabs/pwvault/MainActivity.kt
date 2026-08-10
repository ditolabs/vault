package com.ditolabs.pwvault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var repo: VaultRepository
    private lateinit var biometricUnlock: BiometricVaultUnlock
    private var masterPassword: CharArray? = null
    
    // State UI Compose
    private var vaultEntries = mutableStateListOf<Entry>()
    private var isUnlocked = mutableStateOf(false)
    private var showBiometricOffer = mutableStateOf(false)
    private var tempPwForBiometric: CharArray? = null

    // Handler Auto-Clear Clipboard
    private val clipboardClearHandler = Handler(Looper.getMainLooper())
    private var pendingClearRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repo = VaultRepository(this)
        biometricUnlock = BiometricVaultUnlock(this)

        setContent {
            val isDark = isSystemInDarkTheme()
            val colors = if (isDark) darkColorScheme(
                background = Color(0xFF030712), surface = Color(0xFF111827), onSurface = Color(0xFFF9FAFB)
            ) else lightColorScheme(
                background = Color(0xFFFAFAFA), surface = Color.White, onSurface = Color(0xFF111827)
            )

            MaterialTheme(colorScheme = colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    
                    if (!isUnlocked.value) {
                        LockScreen(
                            vaultExists = repo.vaultExists(),
                            isBiometricAvailable = isBiometricAvailable() && biometricUnlock.isEnabled(),
                            onUnlockPassword = { pw -> attemptUnlock(pw.toCharArray(), offerBiometricSetup = true) },
                            onUnlockBiometric = { showBiometricPrompt() }
                        )
                    } else {
                        MainVaultScreen(
                            entries = vaultEntries,
                            onLock = { lockVault() },
                            onAddEntry = { t, u, p -> addEntry(t, u, p) },
                            onEditEntry = { old, t, u, p -> editEntry(old, t, u, p) },
                            onDeleteEntry = { e -> deleteEntry(e) },
                            onCopyPassword = { e -> copyPasswordToClipboard(e) },
                            onCopyUsername = { e -> copyUsernameToClipboard(e) }
                        )
                    }

                    // Dialog Tawarkan Biometrik
                    if (showBiometricOffer.value) {
                        AlertDialog(
                            onDismissRequest = { showBiometricOffer.value = false },
                            title = { Text("Aktifkan unlock sidik jari?") },
                            text = { Text("Password tetap tersimpan terenkripsi. Sidik jari cuma jadi jalan pintas buka vault di HP ini.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showBiometricOffer.value = false
                                    tempPwForBiometric?.let { setupBiometricEncryption(it) }
                                }) { Text("Aktifkan") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBiometricOffer.value = false }) { Text("Nanti aja") }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- LOGIKA BACKEND LAMA YANG DIPERTAHANKAN --- //

    private fun isBiometricAvailable(): Boolean {
        return BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun attemptUnlock(pwChars: CharArray, offerBiometricSetup: Boolean) {
        try {
            if (!repo.vaultExists()) {
                repo.createVault(pwChars)
                Toast.makeText(this, "Vault baru dibuat", Toast.LENGTH_SHORT).show()
            }
            val loaded = repo.unlock(pwChars)
            masterPassword = pwChars
            vaultEntries.clear()
            vaultEntries.addAll(loaded)
            isUnlocked.value = true

            if (offerBiometricSetup && isBiometricAvailable() && !biometricUnlock.isEnabled()) {
                tempPwForBiometric = pwChars
                showBiometricOffer.value = true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Password salah atau vault rusak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBiometricEncryption(pwChars: CharArray) {
        val cipher = biometricUnlock.prepareEncryptCipher()
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Konfirmasi sidik jari")
            .setSubtitle("Buat mengaktifkan unlock cepat")
            .setNegativeButtonText("Batal")
            .build()

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val resultCipher = result.cryptoObject?.cipher ?: return
                biometricUnlock.persistEncrypted(resultCipher, pwChars)
                Toast.makeText(this@MainActivity, "Unlock sidik jari aktif", Toast.LENGTH_SHORT).show()
            }
        })
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun showBiometricPrompt() {
        val cipher = try {
            biometricUnlock.prepareDecryptCipher()
        } catch (e: Exception) {
            Toast.makeText(this, "Unlock sidik jari nggak valid lagi, pakai password", Toast.LENGTH_LONG).show()
            biometricUnlock.disable()
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock PwVault")
            .setNegativeButtonText("Pakai password")
            .build()

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val resultCipher = result.cryptoObject?.cipher ?: return
                val pwChars = biometricUnlock.recoverPassword(resultCipher)
                attemptUnlock(pwChars, offerBiometricSetup = false)
            }
        })
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun persistVault() {
        val pw = masterPassword ?: return
        repo.save(vaultEntries.toList(), pw)
    }

    private fun addEntry(title: String, user: String, pass: String) {
        vaultEntries.add(Entry(title, user, pass))
        persistVault()
    }

    private fun editEntry(oldEntry: Entry, title: String, user: String, pass: String) {
        val index = vaultEntries.indexOf(oldEntry)
        if (index != -1) {
            vaultEntries[index] = Entry(title, user, pass)
            persistVault()
        }
    }

    private fun deleteEntry(entry: Entry) {
        vaultEntries.remove(entry)
        persistVault()
    }

    private fun copyPasswordToClipboard(entry: Entry) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", entry.password)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Password '${entry.title}' disalin (auto-clear 30 detik)", Toast.LENGTH_SHORT).show()

        pendingClearRunnable?.let { clipboardClearHandler.removeCallbacks(it) }
        val clearRunnable = Runnable {
            val current = clipboard.primaryClip
            val currentText = current?.getItemAt(0)?.text
            if (currentText == entry.password) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
        pendingClearRunnable = clearRunnable
        clipboardClearHandler.postDelayed(clearRunnable, 30_000)
    }

    private fun copyUsernameToClipboard(entry: Entry) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("username", entry.username)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Username disalin", Toast.LENGTH_SHORT).show()
    }

    private fun lockVault() {
        masterPassword?.fill('\u0000')
        masterPassword = null
        vaultEntries.clear()
        isUnlocked.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        lockVault()
    }
}

// =========================================
// UI COMPOSABLES
// =========================================

@Composable
fun LockScreen(
    vaultExists: Boolean,
    isBiometricAvailable: Boolean,
    onUnlockPassword: (String) -> Unit,
    onUnlockBiometric: () -> Unit
) {
    var method by remember { mutableStateOf(if (isBiometricAvailable && vaultExists) "bio" else "pass") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Vault", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(if (vaultExists) "Buka Brankas Aman" else "Buat Brankas Baru", color = Color.Gray)

        Spacer(Modifier.height(48.dp))

        // Toggle Tabs
        Row(
            modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(50)).padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton(text = "Sandi", selected = method == "pass") { method = "pass" }
            if (isBiometricAvailable) {
                TabButton(text = "Biometrik", selected = method == "bio") { method = "bio" }
            }
        }

        Spacer(Modifier.height(48.dp))

        if (method == "pass") {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(if (vaultExists) "Master password" else "Buat master password") },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(0.9f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onUnlockPassword(password) },
                enabled = password.isNotEmpty(),
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(150.dp).height(50.dp)
            ) {
                Text(if (vaultExists) "Buka" else "Buat Vault", fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier.size(100.dp).border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape).clip(CircleShape).clickable { onUnlockBiometric() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onUnlockBiometric, shape = RoundedCornerShape(50), modifier = Modifier.width(150.dp).height(50.dp)) {
                Text("Pindai", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RowScope.TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    val tc = if (selected) MaterialTheme.colorScheme.background else Color.Gray
    Box(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50)).background(bg).clickable(onClick = onClick).padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, fontWeight = FontWeight.Bold, color = tc) }
}

@Composable
fun MainVaultScreen(
    entries: List<Entry>,
    onLock: () -> Unit,
    onAddEntry: (String, String, String) -> Unit,
    onEditEntry: (Entry, String, String, String) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
    onCopyPassword: (Entry) -> Unit,
    onCopyUsername: (Entry) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Entry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Entry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Entry?>(null) }

    val filteredList = entries.filter { 
        it.title.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) 
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PwVault", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onLock) { Icon(Icons.Default.Lock, contentDescription = "Lock") }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari kredensial...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(16.dp))

            // List
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        Surface(
                            onClick = { selectedItem = item },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Key, contentDescription = null) }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(item.username, color = Color.Gray, fontSize = 13.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.background
        ) { Icon(Icons.Default.Add, contentDescription = "Tambah") }

        // Detail Overlay
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedItem?.let { item ->
                DetailScreen(
                    entry = item,
                    onClose = { selectedItem = null },
                    onCopyPassword = { onCopyPassword(item) },
                    onCopyUsername = { onCopyUsername(item) },
                    onEdit = { editingItem = item; showAddDialog = true },
                    onDelete = { showDeleteConfirm = item }
                )
            }
        }

        // Dialog Add/Edit
        if (showAddDialog) {
            AddEditDialog(
                entry = editingItem,
                onDismiss = { showAddDialog = false; editingItem = null },
                onSave = { t, u, p ->
                    if (editingItem != null) {
                        onEditEntry(editingItem!!, t, u, p)
                        selectedItem = Entry(t, u, p)
                    } else { onAddEntry(t, u, p) }
                    showAddDialog = false
                    editingItem = null
                }
            )
        }

        // Confirm Delete
        if (showDeleteConfirm != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Hapus entry?") },
                text = { Text("Data '${showDeleteConfirm?.title}' akan dihapus permanen.") },
                confirmButton = {
                    TextButton(onClick = { 
                        onDeleteEntry(showDeleteConfirm!!)
                        if (selectedItem == showDeleteConfirm) selectedItem = null
                        showDeleteConfirm = null
                    }) { Text("Hapus", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun DetailScreen(
    entry: Entry,
    onClose: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Kembali") }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Hapus", tint = Color.Red) }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier.size(72.dp).border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(16.dp))
            Text(entry.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            
            Spacer(Modifier.height(48.dp))
            
            Text("Nama Pengguna / Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.username, fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                IconButton(onClick = onCopyUsername, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.ContentCopy, "Copy") }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text("Kata Sandi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showPassword) entry.password else "••••••••••••",
                    fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(24.dp)) { 
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle") 
                    }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = onCopyPassword, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.ContentCopy, "Copy") }
                }
            }
        }
    }
}

@Composable
fun AddEditDialog(entry: Entry?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var username by remember { mutableStateOf(entry?.username ?: "") }
    var password by remember { mutableStateOf(entry?.password ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Data Baru" else "Edit Data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Layanan") }, singleLine = true)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username/Email") }, singleLine = true)
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Password") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank() && password.isNotBlank()) onSave(title, username, password) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}