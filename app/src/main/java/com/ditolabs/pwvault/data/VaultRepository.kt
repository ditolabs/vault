package com.ditolabs.pwvault.data

import android.content.Context
import com.ditolabs.pwvault.crypto.VaultCrypto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class VaultRepository(context: Context) {
    private val vaultFile = File(context.filesDir, "vault.enc")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun vaultExists(): Boolean = vaultFile.exists()

    fun createVault(masterPassword: CharArray) {
        val salt = VaultCrypto.generateSalt()
        val empty = json.encodeToString(VaultData())
        vaultFile.writeBytes(VaultCrypto.encrypt(empty.toByteArray(Charsets.UTF_8), masterPassword, salt))
    }

    fun unlock(masterPassword: CharArray): MutableList<Entry> {
        val blob = vaultFile.readBytes()
        val plaintext = VaultCrypto.decrypt(blob, masterPassword)
        val data = json.decodeFromString(VaultData.serializer(), String(plaintext, Charsets.UTF_8))
        return data.entries.toMutableList()
    }

    fun save(entries: List<Entry>, masterPassword: CharArray) {
        val salt = VaultCrypto.extractSalt(vaultFile.readBytes())
        val plaintext = json.encodeToString(VaultData(entries))
        vaultFile.writeBytes(VaultCrypto.encrypt(plaintext.toByteArray(Charsets.UTF_8), masterPassword, salt))
    }

    /** Raw encrypted bytes for backup — never decrypted, safe to hand to any
     * external storage (Drive, email) since it's unreadable without the
     * master password/PIN that was used to create this vault. */
    fun rawEncryptedBytes(): ByteArray = vaultFile.readBytes()

    /** Overwrites the current vault with a previously backed-up encrypted blob.
     * Caller is responsible for locking/forcing re-unlock afterward, since the
     * in-memory master password may no longer match. */
    fun restoreFromRawBytes(bytes: ByteArray) {
        vaultFile.writeBytes(bytes)
    }
}
