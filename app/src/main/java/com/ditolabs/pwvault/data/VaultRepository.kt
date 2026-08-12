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
}
