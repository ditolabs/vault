package com.ditolabs.pwvault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ==========================================
// KAMUS BAHASA (i18n)
// ==========================================
data class AppStrings(
    val search: String, val newData: String, val noData: String, val detail: String,
    val close: String, val selectData: String, val selectDataDesc: String,
    val usernameEmail: String, val password: String, val lockVault: String,
    val category: String, val catAll: String, val catSosmed: String,
    val catEmail: String, val catKerja: String, val catLainnya: String,
    val unlockPin: String, val unlockPass: String, val unlockBio: String,
    val pin: String, val pass: String, val bio: String, val delete: String,
    val next: String, val scan: String, val scanning: String, val offlineMode: String,
    val wrongPin: String, val wrongPass: String, val inputPass: String,
    val backup: String, val exportJson: String, val importVault: String,
    val serviceName: String, val save: String, val cancel: String, val editData: String,
    val cleanEmpty: String, val selectCategory: String
)

val dictID = AppStrings(
    search = "Cari kredensial...", newData = "Data Baru", noData = "Tidak ada data.",
    detail = "Detail Kredensial", close = "Tutup", selectData = "Pilih Data",
    selectDataDesc = "Pilih akun di daftar untuk melihat detail.",
    usernameEmail = "Nama Pengguna / Email", password = "Kata Sandi", lockVault = "Kunci Brankas",
    category = "Kategori", catAll = "Semua Sandi", catSosmed = "Sosial Media",
    catEmail = "Email", catKerja = "Pekerjaan", catLainnya = "Lainnya",
    unlockPin = "Akses dengan PIN 6 digit.", unlockPass = "Akses dengan kata sandi utama.",
    unlockBio = "Sentuh sensor biometrik perangkat.", pin = "PIN", pass = "Sandi",
    bio = "Biometrik", delete = "Hapus", next = "Lanjut", scan = "Pindai", scanning = "Memindai...",
    offlineMode = "Mode Offline Lokal", wrongPin = "PIN tidak sesuai.", wrongPass = "Kata sandi salah.",
    inputPass = "Masukkan kata sandi utama", backup = "Cadangan Data",
    exportJson = "Export Vault (JSON)", importVault = "Import Vault",
    serviceName = "Nama Layanan", save = "Simpan", cancel = "Batal", editData = "Edit Data",
    cleanEmpty = "Bersihkan Data Kosong", selectCategory = "Pilih Kategori"
)

val dictEN = AppStrings(
    search = "Search credentials...", newData = "New Data", noData = "No data found.",
    detail = "Credential Details", close = "Close", selectData = "Select Data",
    selectDataDesc = "Select an account to view details.",
    usernameEmail = "Username / Email", password = "Password", lockVault = "Lock Vault",
    category = "Category", catAll = "All Passwords", catSosmed = "Social Media",
    catEmail = "Email", catKerja = "Work", catLainnya = "Others",
    unlockPin = "Access with 6-digit PIN.", unlockPass = "Access with master password.",
    unlockBio = "Touch device biometric sensor.", pin = "PIN", pass = "Password",
    bio = "Biometrics", delete = "Clear", next = "Next", scan = "Scan", scanning = "Scanning...",
    offlineMode = "Local Offline Mode", wrongPin = "Incorrect PIN.", wrongPass = "Incorrect password.",
    inputPass = "Enter master password", backup = "Data Backup",
    exportJson = "Export Vault (JSON)", importVault = "Import Vault",
    serviceName = "Service Name", save = "Save", cancel = "Cancel", editData = "Edit Data",
    cleanEmpty = "Clean Empty Passwords", selectCategory = "Select Category"
)

class MainActivity : AppCompatActivity() {

    private lateinit var repo: VaultRepository
    private lateinit var biometricUnlock: BiometricVaultUnlock
    private var masterPassword: CharArray? = null
    
    private var vaultEntries = mutableStateListOf<Entry>()
    private var isUnlocked = mutableStateOf(false)
    private var showBiometricOffer = mutableStateOf(false)
    private var tempPwForBiometric: CharArray? = null

    private lateinit var exportLauncher: ActivityResultLauncher<String>
    private lateinit var importLauncher: ActivityResultLauncher<Array<String>>

    private val clipboardClearHandler = Handler(Looper.getMainLooper())
    private var pendingClearRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repo = VaultRepository(this)
        biometricUnlock = BiometricVaultUnlock(this)

        exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                contentResolver.openOutputStream(it)?.use { out ->
                    val jsonArray = JSONArray()
                    vaultEntries.forEach { e ->
                        val obj = JSONObject().apply {
                            put("title", e.title)
                            put("username", e.username)
                            put("password", e.password)
                            put("category", e.category)
                        }
                        jsonArray.put(obj)
                    }
                    out.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8))
                    Toast.makeText(this, "Vault diexport ke JSON", Toast.LENGTH_SHORT).show()
                }
            }
        }

        importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    contentResolver.openInputStream(it)?.use { input ->
                        val text = input.reader().readText().trim()
                        if (text.startsWith("[")) {
                            val jsonArray = JSONArray(text)
                            var importedCount = 0
                            
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val title = obj.optString("title", obj.optString("name", obj.optString("url", "Tanpa Judul")))
                                val user = obj.optString("username", obj.optString("login", obj.optString("email", "-")))
                                val pass = obj.optString("password", obj.optString("pass", ""))
                                
                                val catFallback = when {
                                    title.lowercase().contains(Regex("ig|insta|twitter|x|fb|facebook|tiktok|sosmed")) -> "sosmed"
                                    title.lowercase().contains(Regex("gmail|yahoo|outlook|email")) -> "email"
                                    title.lowercase().contains(Regex("kerja|slack|github|gitlab|jira|office")) -> "kerja"
                                    else -> "lainnya"
                                }
                                val category = obj.optString("category", catFallback).ifEmpty { catFallback }
                                
                                vaultEntries.add(Entry(title, user, pass, category))
                                importedCount++
                            }
                            
                            if (importedCount > 0) {
                                persistVault()
                                Toast.makeText(this, "$importedCount data berhasil diimpor!", Toast.LENGTH_LONG).show()
                            }
                        } else { Toast.makeText(this, "Gagal: Format bukan JSON Array", Toast.LENGTH_LONG).show() }
                    }
                } catch (e: Exception) { Toast.makeText(this, "Format file JSON tidak dikenali", Toast.LENGTH_SHORT).show() }
            }
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            var lang by remember { mutableStateOf("ID") }
            val t = if (lang == "ID") dictID else dictEN

            val vaultGreen = Color(0xFF3EA87C)
            val danger = Color(0xFFC96A5A)
            
            val DarkColorScheme = darkColorScheme(
                primary = vaultGreen, background = Color(0xFF0B0D12), surface = Color(0xFF12141B),
                onBackground = Color(0xFFE9EAE2), onSurface = Color(0xFFE9EAE2), onPrimary = Color(0xFF0B0D12),
                outline = Color(0xFF262B38), error = danger
            )
            val LightColorScheme = lightColorScheme(
                primary = vaultGreen, background = Color(0xFFFAFAFA), surface = Color.White,
                onBackground = Color(0xFF111827), onSurface = Color(0xFF111827), onPrimary = Color.White,
                outline = Color(0xFFD1D5DB), error = danger
            )

            MaterialTheme(colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (!isUnlocked.value) {
                        LockScreen(
                            vaultExists = repo.vaultExists(), 
                            isBiometricAvailable = isBiometricAvailable() && biometricUnlock.isEnabled(),
                            t = t, lang = lang, isDark = isDarkTheme,
                            onToggleLang = { lang = if (lang == "ID") "EN" else "ID" }, 
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onUnlockPassword = { pw -> attemptUnlock(pw.toCharArray(), offerBiometricSetup = true) },
                            onUnlockBiometric = { showBiometricPrompt() }
                        )
                    } else {
                        MainVaultScreen(
                            entries = vaultEntries, 
                            t = t, lang = lang, isDark = isDarkTheme,
                            onToggleLang = { lang = if (lang == "ID") "EN" else "ID" }, 
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onLock = { lockVault() }, 
                            onExport = { exportLauncher.launch("pwvault_backup.json") },
                            onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            onCleanEmpty = { cleanEmptyPasswords() },
                            onAddEntry = { title, u, p, cat -> addEntry(title, u, p, cat) },
                            onEditEntry = { old, title, u, p, cat -> editEntry(old, title, u, p, cat) },
                            onDeleteEntry = { e -> deleteEntry(e) },
                            onCopyPassword = { e -> copyPasswordToClipboard(e) },
                            onCopyUsername = { e -> copyUsernameToClipboard(e) }
                        )
                    }

                    if (showBiometricOffer.value) {
                        AlertDialog(
                            onDismissRequest = { showBiometricOffer.value = false },
                            title = { Text("Aktifkan unlock sidik jari?") },
                            text = { Text("Password tetap tersimpan terenkripsi. Sidik jari cuma jadi jalan pintas buka vault di HP ini.") },
                            confirmButton = {
                                TextButton(onClick = { showBiometricOffer.value = false; tempPwForBiometric?.let { setupBiometricEncryption(it) } }) { Text("Aktifkan") }
                            },
                            dismissButton = { TextButton(onClick = { showBiometricOffer.value = false }) { Text("Nanti aja") } }
                        )
                    }
                }
            }
        }
    }

    private fun isBiometricAvailable(): Boolean = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    private fun attemptUnlock(pwChars: CharArray, offerBiometricSetup: Boolean) {
        try {
            if (!repo.vaultExists()) { repo.createVault(pwChars); Toast.makeText(this, "Vault baru dibuat", Toast.LENGTH_SHORT).show() }
            val loaded = repo.unlock(pwChars)
            masterPassword = pwChars
            vaultEntries.clear()
            vaultEntries.addAll(loaded)
            isUnlocked.value = true

            if (offerBiometricSetup && isBiometricAvailable() && !biometricUnlock.isEnabled()) {
                tempPwForBiometric = pwChars
                showBiometricOffer.value = true
            }
        } catch (e: Exception) { Toast.makeText(this, "Password salah atau vault rusak", Toast.LENGTH_SHORT).show() }
    }

    private fun setupBiometricEncryption(pwChars: CharArray) {
        val cipher = biometricUnlock.prepareEncryptCipher()
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                result.cryptoObject?.cipher?.let { biometricUnlock.persistEncrypted(it, pwChars); Toast.makeText(this@MainActivity, "Unlock sidik jari aktif", Toast.LENGTH_SHORT).show() }
            }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Konfirmasi sidik jari").setNegativeButtonText("Batal").build(), BiometricPrompt.CryptoObject(cipher))
    }

    private fun showBiometricPrompt() {
        val cipher = try { biometricUnlock.prepareDecryptCipher() } catch (e: Exception) {
            Toast.makeText(this, "Sidik jari tidak valid, gunakan password", Toast.LENGTH_LONG).show(); biometricUnlock.disable(); return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                result.cryptoObject?.cipher?.let { attemptUnlock(biometricUnlock.recoverPassword(it), false) }
            }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock PwVault").setNegativeButtonText("Pakai password").build(), BiometricPrompt.CryptoObject(cipher))
    }

    private fun persistVault() { repo.save(vaultEntries.toList(), masterPassword ?: return) }
    private fun addEntry(t: String, u: String, p: String, c: String) { vaultEntries.add(Entry(t, u, p, c)); persistVault() }
    private fun editEntry(old: Entry, t: String, u: String, p: String, c: String) {
        val idx = vaultEntries.indexOf(old)
        if (idx != -1) { vaultEntries[idx] = Entry(t, u, p, c); persistVault() }
    }
    private fun deleteEntry(e: Entry) { vaultEntries.remove(e); persistVault() }

    private fun cleanEmptyPasswords() {
        val initialSize = vaultEntries.size
        vaultEntries.removeAll { it.password.isEmpty() }
        val removedCount = initialSize - vaultEntries.size
        if (removedCount > 0) {
            persistVault()
            Toast.makeText(this, "$removedCount data tanpa sandi berhasil dibersihkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Tidak ada data dengan sandi kosong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyPasswordToClipboard(entry: Entry) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("password", entry.password))
        Toast.makeText(this, "Password disalin (auto-clear 30 dtk)", Toast.LENGTH_SHORT).show()

        pendingClearRunnable?.let { clipboardClearHandler.removeCallbacks(it) }
        val clearRunnable = Runnable { if (clipboard.primaryClip?.getItemAt(0)?.text == entry.password) clipboard.setPrimaryClip(ClipData.newPlainText("", "")) }
        pendingClearRunnable = clearRunnable
        clipboardClearHandler.postDelayed(clearRunnable, 30_000)
    }

    private fun copyUsernameToClipboard(entry: Entry) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("username", entry.username))
        Toast.makeText(this, "Username disalin", Toast.LENGTH_SHORT).show()
    }

    private fun lockVault() { masterPassword?.fill('\u0000'); masterPassword = null; vaultEntries.clear(); isUnlocked.value = false }
    override fun onDestroy() { super.onDestroy(); lockVault() }
}

@Composable
fun VaultLogo(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(modifier = modifier.size(32.dp)) {
        scale(scale = size.width / 100f, pivot = Offset.Zero) {
            drawCircle(color = color, radius = 42f, center = Offset(50f, 50f), style = Stroke(width = 7f))
            drawLine(color = color, start = Offset(50f, 12f), end = Offset(50f, 20f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(12f, 50f), end = Offset(20f, 50f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(80f, 50f), end = Offset(88f, 50f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(50f, 80f), end = Offset(50f, 88f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawPath(path = Path().apply { moveTo(30f, 36f); lineTo(50f, 66f); lineTo(70f, 36f) }, color = color, style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(color = color, radius = 5f, center = Offset(50f, 46f))
        }
    }
}

@Composable
fun LockScreen(
    vaultExists: Boolean, isBiometricAvailable: Boolean,
    t: AppStrings, lang: String, isDark: Boolean,
    onToggleLang: () -> Unit, onToggleTheme: () -> Unit,
    onUnlockPassword: (String) -> Unit, onUnlockBiometric: () -> Unit
) {
    var showPasswordForm by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50), color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                onClick = onToggleLang
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(lang, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Surface(
                shape = CircleShape, color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                onClick = onToggleTheme
            ) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    Icon(if (isDark) Icons.Default.WbSunny else Icons.Default.DarkMode, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            VaultLogo(modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(16.dp))
            Text("Vault", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(if (vaultExists) t.unlockPass else "Buat Brankas Baru", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showPasswordForm) {
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        placeholder = { Text(if (vaultExists) t.inputPass else "Buat master password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(),
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Button(
                        onClick = { onUnlockPassword(password) },
                        enabled = password.isNotEmpty(),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(if (vaultExists) t.next else "Buat", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showPasswordForm = true },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface, 
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(t.pass, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (isBiometricAvailable) {
                    OutlinedButton(
                        onClick = { onUnlockBiometric() },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.bio, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MainVaultScreen(
    entries: List<Entry>, t: AppStrings, lang: String, isDark: Boolean,
    onToggleLang: () -> Unit, onToggleTheme: () -> Unit, onLock: () -> Unit,
    onExport: () -> Unit, onImport: () -> Unit, onCleanEmpty: () -> Unit,
    onAddEntry: (String, String, String, String) -> Unit, onEditEntry: (Entry, String, String, String, String) -> Unit,
    onDeleteEntry: (Entry) -> Unit, onCopyPassword: (Entry) -> Unit, onCopyUsername: (Entry) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Entry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Entry?>(null) }

    val filteredList = entries.filter { entry ->
        val matchCat = when (activeCategory) {
            "all" -> true
            else -> entry.category == activeCategory
        }
        val matchSearch = entry.title.contains(searchQuery, true) || entry.username.contains(searchQuery, true)
        matchCat && matchSearch
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp), drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                        VaultLogo(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("PwVault", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(t.category.uppercase(), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    val categories = listOf(
                        "all" to t.catAll, "sosmed" to t.catSosmed, "email" to t.catEmail,
                        "kerja" to t.catKerja, "lainnya" to t.catLainnya
                    )
                    categories.forEach { (id, label) ->
                        val isSelected = activeCategory == id
                        Surface(
                            onClick = { activeCategory = id; scope.launch { drawerState.close() } },
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(label, modifier = Modifier.padding(12.dp), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("PEMELIHARAAN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { scope.launch { drawerState.close() }; onCleanEmpty() }, 
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Text(t.cleanEmpty, color = MaterialTheme.colorScheme.error) 
                    }

                    Spacer(Modifier.weight(1f))
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))

                    Text(t.backup.uppercase(), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { scope.launch { drawerState.close() }; onExport() }, modifier = Modifier.fillMaxWidth()) { Text(t.exportJson) }
                    TextButton(onClick = { scope.launch { drawerState.close() }; onImport() }, modifier = Modifier.fillMaxWidth()) { Text(t.importVault) }

                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).padding(4.dp)) {
                        TextButton(onClick = onToggleLang, modifier = Modifier.weight(1f)) { Text(lang, fontWeight = FontWeight.Bold) }
                        TextButton(onClick = onToggleTheme, modifier = Modifier.weight(1f)) { Text(if (isDark) "Dark" else "Light", fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onLock, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(t.lockVault, fontWeight = FontWeight.Bold) }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") }
                    Spacer(Modifier.width(8.dp))
                    Text(categoriesLabel(activeCategory, t), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text(t.search) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true,
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
                )
                Spacer(Modifier.height(16.dp))

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(t.noData, color = Color.Gray) }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredList) { item ->
                            Surface(onClick = { selectedItem = item }, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(48.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { 
                                        Icon(if (item.password.isEmpty()) Icons.Default.Warning else Icons.Default.Key, null, tint = if (item.password.isEmpty()) Color.Red else MaterialTheme.colorScheme.primary) 
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(item.username, color = Color.Gray, fontSize = 13.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.background) { 
                Icon(Icons.Default.Add, t.newData) 
            }

            AnimatedVisibility(visible = selectedItem != null, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it }), modifier = Modifier.fillMaxSize()) {
                selectedItem?.let { item ->
                    DetailScreen(
                        entry = item, t = t, onClose = { selectedItem = null },
                        onCopyPassword = { onCopyPassword(item) }, onCopyUsername = { onCopyUsername(item) },
                        onEdit = { editingItem = item; showAddDialog = true },
                        onDelete = { onDeleteEntry(item); selectedItem = null }
                    )
                }
            }

            if (showAddDialog) {
                AddEditDialog(
                    entry = editingItem, t = t,
                    onDismiss = { showAddDialog = false; editingItem = null },
                    onSave = { title, u, p, cat ->
                        if (editingItem != null) { onEditEntry(editingItem!!, title, u, p, cat); selectedItem = Entry(title, u, p, cat) } 
                        else { onAddEntry(title, u, p, cat) }
                        showAddDialog = false; editingItem = null
                    }
                )
            }
        }
    }
}

fun categoriesLabel(id: String, t: AppStrings) = when(id) { "sosmed" -> t.catSosmed "email" -> t.catEmail "kerja" -> t.catKerja "lainnya" -> t.catLainnya else -> t.catAll }

@Composable
fun DetailScreen(
    entry: Entry, t: AppStrings, onClose: () -> Unit, onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, t.close) }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, t.delete, tint = Color.Red) }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.size(72.dp).border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) { 
                Icon(if (entry.password.isEmpty()) Icons.Default.Warning else Icons.Default.Key, null, modifier = Modifier.size(32.dp), tint = if(entry.password.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface) 
            }
            Spacer(Modifier.height(16.dp))
            Text(entry.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            
            Spacer(Modifier.height(48.dp))
            Text(t.usernameEmail, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(entry.username, fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                IconButton(onClick = onCopyUsername, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.ContentCopy, "Copy") }
            }
            
            Spacer(Modifier.height(24.dp))
            Text(t.password, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (showPassword) entry.password else if(entry.password.isEmpty()) "-" else "••••••••••••", fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(24.dp)) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Toggle") }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = onCopyPassword, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.ContentCopy, "Copy") }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(t.delete + "?") },
                text = { Text("Data '${entry.title}' akan dihapus permanen.") },
                confirmButton = { TextButton(onClick = onDelete) { Text(t.delete, color = Color.Red) } },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(t.close) } }
            )
        }
    }
}

@Composable
fun AddEditDialog(entry: Entry?, t: AppStrings, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var username by remember { mutableStateOf(entry?.username ?: "") }
    var password by remember { mutableStateOf(entry?.password ?: "") }
    var category by remember { mutableStateOf(entry?.category ?: "lainnya") }

    val categories = listOf(
        "sosmed" to t.catSosmed, "email" to t.catEmail,
        "kerja" to t.catKerja, "lainnya" to t.catLainnya
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) t.newData else t.editData) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(t.serviceName) }, singleLine = true)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(t.usernameEmail) }, singleLine = true)
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(t.password) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                
                Text(t.selectCategory, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (id, label) ->
                        val isSelected = category == id
                        Surface(
                            onClick = { category = id },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = label, fontSize = 12.sp, 
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, 
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(title, username, password, category) }) { Text(t.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t.cancel) } }
    )
}