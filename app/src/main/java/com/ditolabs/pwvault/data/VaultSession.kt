package com.ditolabs.pwvault.data

/**
 * Holds the derived master password + decrypted entries in memory only while the
 * vault is "unlocked", so the Autofill Service (same process) can serve
 * suggestions without re-prompting every keystroke. Cleared on lock/expiry —
 * nothing here ever touches disk.
 */
object VaultSession {
    private const val SESSION_TIMEOUT_MILLIS = 5 * 60 * 1000L // 5 minutes of inactivity

    private var masterPassword: CharArray? = null
    private var entriesCache: List<Entry> = emptyList()
    private var lastTouchedAt: Long = 0

    val isUnlocked: Boolean
        get() {
            if (masterPassword == null) return false
            if (System.currentTimeMillis() - lastTouchedAt > SESSION_TIMEOUT_MILLIS) {
                lock()
                return false
            }
            return true
        }

    fun unlock(password: CharArray, entries: List<Entry>) {
        masterPassword = password
        entriesCache = entries
        lastTouchedAt = System.currentTimeMillis()
    }

    fun updateEntries(entries: List<Entry>) {
        entriesCache = entries
        lastTouchedAt = System.currentTimeMillis()
    }

    fun currentPassword(): CharArray? {
        if (!isUnlocked) return null
        lastTouchedAt = System.currentTimeMillis()
        return masterPassword
    }

    fun currentEntries(): List<Entry> {
        if (!isUnlocked) return emptyList()
        lastTouchedAt = System.currentTimeMillis()
        return entriesCache
    }

    fun lock() {
        masterPassword?.fill('\u0000')
        masterPassword = null
        entriesCache = emptyList()
    }
}
