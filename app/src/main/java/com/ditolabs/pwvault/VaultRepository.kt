package com.ditolabs.pwvault

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Entry(
    var title: String,
    var username: String,
    var password: String
)

/**
 * Loads and saves the vault as a single encrypted file on internal (app-private)
 * storage. The JSON plaintext only ever exists in memory while unlocked.
 */
class VaultRepository(context: Context) {

    private val vaultFile = File(context.filesDir, "vault.enc")

    fun vaultExists(): Boolean = vaultFile.exists()

    /** Creates a brand-new empty vault encrypted with [masterPassword]. */
    fun createVault(masterPassword: CharArray) {
        val salt = VaultCrypto.generateSalt()
        val emptyJson = JSONArray().toString().toByteArray(Charsets.UTF_8)
        val blob = VaultCrypto.encrypt(emptyJson, masterPassword, salt)
        vaultFile.writeBytes(blob)
    }

    /** Decrypts and parses the vault. Throws if the password is wrong. */
    fun unlock(masterPassword: CharArray): MutableList<Entry> {
        val blob = vaultFile.readBytes()
        val plaintext = VaultCrypto.decrypt(blob, masterPassword)
        val json = JSONArray(String(plaintext, Charsets.UTF_8))
        val entries = mutableListOf<Entry>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            entries.add(
                Entry(
                    title = obj.getString("title"),
                    username = obj.getString("username"),
                    password = obj.getString("password")
                )
            )
        }
        return entries
    }

    /** Re-encrypts and writes [entries] back to disk using the same salt/password. */
    fun save(entries: List<Entry>, masterPassword: CharArray) {
        val salt = VaultCrypto.extractSalt(vaultFile.readBytes())
        val array = JSONArray()
        for (e in entries) {
            val obj = JSONObject()
            obj.put("title", e.title)
            obj.put("username", e.username)
            obj.put("password", e.password)
            array.put(obj)
        }
        val plaintext = array.toString().toByteArray(Charsets.UTF_8)
        val blob = VaultCrypto.encrypt(plaintext, masterPassword, salt)
        vaultFile.writeBytes(blob)
    }
}
