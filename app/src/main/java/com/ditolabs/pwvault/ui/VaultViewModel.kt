package com.ditolabs.pwvault.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ditolabs.pwvault.crypto.BiometricVaultUnlock
import com.ditolabs.pwvault.crypto.PinUnlock
import com.ditolabs.pwvault.data.AppLanguage
import com.ditolabs.pwvault.data.AutoLockDelay
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.data.SettingsStore
import com.ditolabs.pwvault.data.ThemeMode
import com.ditolabs.pwvault.data.VaultRepository
import com.ditolabs.pwvault.data.VaultSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    val repo = VaultRepository(application)
    val pinUnlock = PinUnlock(application)
    val biometricUnlock = BiometricVaultUnlock(application)
    private val settings = SettingsStore(application)

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var masterPassword: CharArray? = null

    val language: StateFlow<AppLanguage> get() = languageState
    val themeMode: StateFlow<ThemeMode> get() = themeModeState
    val autoLockDelay: StateFlow<AutoLockDelay> get() = autoLockState
    private val languageState = MutableStateFlow(AppLanguage.ID)
    private val themeModeState = MutableStateFlow(ThemeMode.SYSTEM)
    private val autoLockState = MutableStateFlow(AutoLockDelay.FIVE)

    init {
        viewModelScope.launch { settings.language.collect { languageState.value = it } }
        viewModelScope.launch { settings.themeMode.collect { themeModeState.value = it } }
        viewModelScope.launch { settings.autoLockDelay.collect { autoLockState.value = it } }
    }

    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { settings.setLanguage(lang) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setAutoLockDelay(delay: AutoLockDelay) = viewModelScope.launch { settings.setAutoLockDelay(delay) }

    fun securityFindings() = com.ditolabs.pwvault.data.PasswordAudit.audit(_entries.value)

    fun vaultExists() = repo.vaultExists()

    /** Returns true on success. */
    fun unlockWithPassword(password: CharArray): Boolean {
        return try {
            if (!repo.vaultExists()) repo.createVault(password)
            val loaded = repo.unlock(password)
            masterPassword = password
            _entries.value = loaded
            _unlocked.value = true
            VaultSession.unlock(password, loaded)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        val recovered = pinUnlock.tryUnlock(pin) ?: run { pinUnlock.recordFailure(); return false }
        return unlockWithPassword(recovered)
    }

    fun lock() {
        masterPassword?.fill('\u0000')
        masterPassword = null
        _unlocked.value = false
        _entries.value = emptyList()
        VaultSession.lock()
    }

    fun addOrUpdateEntry(entry: Entry) {
        val current = _entries.value.toMutableList()
        val idx = current.indexOfFirst { it.id == entry.id }
        if (idx >= 0) current[idx] = entry else current.add(entry)
        persist(current)
    }

    fun deleteEntry(id: String) {
        persist(_entries.value.filterNot { it.id == id })
    }

    /** Returns how many were removed, so the caller can show a specific toast. */
    fun cleanEmptyPasswords(): Int {
        val before = _entries.value.size
        persist(_entries.value.filterNot { it.password.isBlank() })
        return before - _entries.value.size
    }

    fun importEntries(newEntries: List<Entry>) {
        persist(_entries.value + newEntries)
    }

    private fun persist(entries: List<Entry>) {
        _entries.value = entries
        masterPassword?.let { repo.save(entries, it) }
        VaultSession.updateEntries(entries)
    }

    fun currentMasterPassword(): CharArray? = masterPassword
}
