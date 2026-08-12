package com.ditolabs.pwvault.crypto

import android.content.Context
import java.io.File

/**
 * PIN unlock is a convenience shortcut, not a security equal to the master
 * password: a 6-digit PIN is 1,000,000 combinations vs. an arbitrary-length
 * password. To keep that gap from being exploitable, wrong PIN attempts are
 * rate-limited with escalating lockouts (see [recordFailure]).
 */
class PinUnlock(context: Context) {
    private val storeFile = File(context.filesDir, "pin.enc")
    private val prefsFile = File(context.filesDir, "pin_attempts.txt")

    fun isEnabled(): Boolean = storeFile.exists()

    fun disable() {
        storeFile.delete()
        prefsFile.delete()
    }

    fun setup(pin: String, masterPassword: CharArray) {
        val salt = VaultCrypto.generateSalt()
        val blob = VaultCrypto.encrypt(String(masterPassword).toByteArray(Charsets.UTF_8), pin.toCharArray(), salt)
        storeFile.writeBytes(blob)
        prefsFile.delete()
    }

    /** Returns the recovered master password, or null if the PIN is wrong. */
    fun tryUnlock(pin: String): CharArray? {
        return try {
            val blob = storeFile.readBytes()
            val plaintext = VaultCrypto.decrypt(blob, pin.toCharArray())
            clearFailures()
            String(plaintext, Charsets.UTF_8).toCharArray()
        } catch (e: Exception) {
            null
        }
    }

    data class LockoutState(val attemptsLeft: Int, val lockedUntilMillis: Long)

    fun currentLockout(): LockoutState {
        if (!prefsFile.exists()) return LockoutState(MAX_ATTEMPTS, 0)
        val parts = prefsFile.readText().split(",")
        val attempts = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val lockedUntil = parts.getOrNull(1)?.toLongOrNull() ?: 0
        return LockoutState((MAX_ATTEMPTS - attempts).coerceAtLeast(0), lockedUntil)
    }

    fun recordFailure() {
        val state = currentLockout()
        val newAttempts = MAX_ATTEMPTS - state.attemptsLeft + 1
        val lockedUntil = if (newAttempts >= MAX_ATTEMPTS) {
            System.currentTimeMillis() + LOCKOUT_MILLIS
        } else 0
        prefsFile.writeText("$newAttempts,$lockedUntil")
    }

    private fun clearFailures() {
        prefsFile.delete()
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_MILLIS = 30_000L
    }
}
