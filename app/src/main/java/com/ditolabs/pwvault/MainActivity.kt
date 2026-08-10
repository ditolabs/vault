package com.ditolabs.pwvault

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var repo: VaultRepository
    private lateinit var biometricUnlock: BiometricVaultUnlock
    private var masterPassword: CharArray? = null
    private val entries = mutableListOf<Entry>()
    private lateinit var adapter: ArrayAdapter<String>

    private val clipboardClearHandler = Handler(Looper.getMainLooper())
    private var pendingClearRunnable: Runnable? = null

    private lateinit var lockContainer: View
    private lateinit var vaultContainer: View
    private lateinit var statusText: TextView
    private lateinit var biometricButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repo = VaultRepository(this)
        biometricUnlock = BiometricVaultUnlock(this)

        lockContainer = findViewById(R.id.lockContainer)
        vaultContainer = findViewById(R.id.vaultContainer)
        val passwordInput = findViewById<TextInputEditText>(R.id.masterPasswordInput)
        val unlockButton = findViewById<MaterialButton>(R.id.unlockButton)
        statusText = findViewById(R.id.lockStatusText)
        biometricButton = findViewById(R.id.biometricUnlockButton)
        val listView = findViewById<ListView>(R.id.entryListView)
        val fab = findViewById<View>(R.id.addEntryFab)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1)
        listView.adapter = adapter
        listView.emptyView = findViewById(R.id.emptyStateText)

        // R-15: label the specific action instead of a combined generic "Unlock/Create" button.
        unlockButton.text = if (repo.vaultExists()) "Buka Vault" else "Buat Vault Baru"
        passwordInput.hint = if (repo.vaultExists()) "Master password" else "Bikin master password baru"

        unlockButton.setOnClickListener {
            val pw = passwordInput.text?.toString().orEmpty()
            if (pw.isEmpty()) {
                statusText.text = "Password nggak boleh kosong"
                return@setOnClickListener
            }
            attemptUnlock(pw.toCharArray(), offerBiometricSetup = true)
        }

        biometricButton.setOnClickListener { showBiometricPrompt() }

        fab.setOnClickListener { showAddEntryDialog() }

        listView.setOnItemClickListener { _, _, position, _ ->
            showEntryDetailDialog(position)
        }
    }

    override fun onStart() {
        super.onStart()
        // Show the fingerprint option only if hardware+enrollment is available AND
        // the user previously opted in.
        val canUseBiometric = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
        biometricButton.visibility =
            if (canUseBiometric && biometricUnlock.isEnabled()) View.VISIBLE else View.GONE
    }

    private fun attemptUnlock(pwChars: CharArray, offerBiometricSetup: Boolean) {
        try {
            if (!repo.vaultExists()) {
                repo.createVault(pwChars)
                Toast.makeText(this, "Vault baru dibuat", Toast.LENGTH_SHORT).show()
            }
            val loaded = repo.unlock(pwChars)
            masterPassword = pwChars
            entries.clear()
            entries.addAll(loaded)
            refreshList()
            lockContainer.visibility = View.GONE
            vaultContainer.visibility = View.VISIBLE

            if (offerBiometricSetup) maybeOfferBiometricSetup(pwChars)
        } catch (e: Exception) {
            statusText.text = "Password salah atau vault rusak"
        }
    }

    private fun maybeOfferBiometricSetup(pwChars: CharArray) {
        val canUseBiometric = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
        if (!canUseBiometric || biometricUnlock.isEnabled()) return

        AlertDialog.Builder(this)
            .setTitle("Aktifkan unlock sidik jari?")
            .setMessage("Password tetap tersimpan terenkripsi. Sidik jari cuma jadi jalan pintas buka vault di HP ini.")
            .setPositiveButton("Aktifkan") { _, _ -> setupBiometricEncryption(pwChars) }
            .setNegativeButton("Nanti aja", null)
            .show()
    }

    private fun setupBiometricEncryption(pwChars: CharArray) {
        val cipher = biometricUnlock.prepareEncryptCipher()
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Konfirmasi sidik jari")
            .setSubtitle("Buat mengaktifkan unlock cepat")
            .setNegativeButtonText("Batal")
            .build()

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val resultCipher = result.cryptoObject?.cipher ?: return
                    biometricUnlock.persistEncrypted(resultCipher, pwChars)
                    biometricButton.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity, "Unlock sidik jari aktif", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(this@MainActivity, "Dibatalkan: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun showBiometricPrompt() {
        val cipher = try {
            biometricUnlock.prepareDecryptCipher()
        } catch (e: Exception) {
            Toast.makeText(this, "Unlock sidik jari nggak valid lagi, pakai password", Toast.LENGTH_LONG).show()
            biometricUnlock.disable()
            biometricButton.visibility = View.GONE
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock PwVault")
            .setNegativeButtonText("Pakai password")
            .build()

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val resultCipher = result.cryptoObject?.cipher ?: return
                    val pwChars = biometricUnlock.recoverPassword(resultCipher)
                    attemptUnlock(pwChars, offerBiometricSetup = false)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // User cancelled or fell back to password — nothing to do.
                }
            }
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun refreshList() {
        adapter.clear()
        for (e in entries) {
            adapter.add("${e.title}\n${e.username}")
        }
        adapter.notifyDataSetChanged()
    }

    private fun showAddEntryDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_entry, null)
        val titleInput = view.findViewById<EditText>(R.id.titleInput)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)

        AlertDialog.Builder(this)
            .setTitle("Entry baru")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val title = titleInput.text?.toString().orEmpty()
                val username = usernameInput.text?.toString().orEmpty()
                val password = passwordInput.text?.toString().orEmpty()
                if (title.isNotBlank() && password.isNotBlank()) {
                    entries.add(Entry(title, username, password))
                    persistVault()
                    refreshList()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /** Tap on an entry opens this: copy / edit / delete, so it's discoverable without guessing gestures. */
    private fun showEntryDetailDialog(position: Int) {
        val entry = entries[position]
        val options = arrayOf("Salin password", "Edit", "Hapus")
        AlertDialog.Builder(this)
            .setTitle(entry.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> copyPasswordToClipboard(entry)
                    1 -> showEditEntryDialog(position)
                    2 -> confirmDelete(position)
                }
            }
            .show()
    }

    private fun showEditEntryDialog(position: Int) {
        val entry = entries[position]
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_entry, null)
        val titleInput = view.findViewById<EditText>(R.id.titleInput)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        titleInput.setText(entry.title)
        usernameInput.setText(entry.username)
        passwordInput.setText(entry.password)

        AlertDialog.Builder(this)
            .setTitle("Edit entry")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val title = titleInput.text?.toString().orEmpty()
                val username = usernameInput.text?.toString().orEmpty()
                val password = passwordInput.text?.toString().orEmpty()
                if (title.isNotBlank() && password.isNotBlank()) {
                    entries[position] = Entry(title, username, password)
                    persistVault()
                    refreshList()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete(position: Int) {
        val entry = entries[position]
        AlertDialog.Builder(this)
            .setTitle("Hapus entry?")
            .setMessage(entry.title)
            .setPositiveButton("Hapus") { _, _ ->
                entries.removeAt(position)
                persistVault()
                refreshList()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun persistVault() {
        val pw = masterPassword ?: return
        repo.save(entries, pw)
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

    override fun onDestroy() {
        super.onDestroy()
        masterPassword?.fill('\u0000')
        masterPassword = null
    }
}
