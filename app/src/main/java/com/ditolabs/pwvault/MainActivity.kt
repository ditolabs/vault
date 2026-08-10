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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var repo: VaultRepository
    private var masterPassword: CharArray? = null
    private val entries = mutableListOf<Entry>()
    private lateinit var adapter: ArrayAdapter<String>

    private val clipboardClearHandler = Handler(Looper.getMainLooper())
    private var pendingClearRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repo = VaultRepository(this)

        val lockContainer = findViewById<View>(R.id.lockContainer)
        val vaultContainer = findViewById<View>(R.id.vaultContainer)
        val passwordInput = findViewById<TextInputEditText>(R.id.masterPasswordInput)
        val unlockButton = findViewById<MaterialButton>(R.id.unlockButton)
        val statusText = findViewById<TextView>(R.id.lockStatusText)
        val listView = findViewById<ListView>(R.id.entryListView)
        val fab = findViewById<View>(R.id.addEntryFab)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1)
        listView.adapter = adapter

        unlockButton.setOnClickListener {
            val pw = passwordInput.text?.toString().orEmpty()
            if (pw.isEmpty()) {
                statusText.text = "Password nggak boleh kosong"
                return@setOnClickListener
            }
            val pwChars = pw.toCharArray()
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
            } catch (e: Exception) {
                statusText.text = "Password salah atau vault rusak"
            }
        }

        fab.setOnClickListener { showAddEntryDialog() }

        listView.setOnItemClickListener { _, _, position, _ ->
            copyPasswordToClipboard(entries[position])
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            confirmDelete(position)
            true
        }
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

        // Auto-clear clipboard after 30s so the password doesn't linger.
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
