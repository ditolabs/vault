package com.ditolabs.pwvault.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Wraps the master password with an Android Keystore AES key gated by biometric
 * auth. The password never leaves the device; the key never leaves secure hardware.
 * Covers fingerprint AND face unlock — both go through the same BiometricPrompt API,
 * there is no separate "face unlock" implementation needed.
 */
class BiometricVaultUnlock(context: Context) {
    private val keyAlias = "pwvault_biometric_key"
    private val storeFile = File(context.filesDir, "biometric.enc")

    fun isEnabled(): Boolean = storeFile.exists()

    fun disable() {
        if (storeFile.exists()) storeFile.delete()
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(keyAlias)) ks.deleteEntry(keyAlias)
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    fun prepareEncryptCipher(): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }

    fun persistEncrypted(cipher: Cipher, password: CharArray) {
        val ciphertext = cipher.doFinal(String(password).toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        storeFile.writeBytes(byteArrayOf(iv.size.toByte()) + iv + ciphertext)
    }

    fun prepareDecryptCipher(): Cipher {
        val bytes = storeFile.readBytes()
        val ivLen = bytes[0].toInt()
        val iv = bytes.copyOfRange(1, 1 + ivLen)
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        }
    }

    fun recoverPassword(cipher: Cipher): CharArray {
        val bytes = storeFile.readBytes()
        val ivLen = bytes[0].toInt()
        val ciphertext = bytes.copyOfRange(1 + ivLen, bytes.size)
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8).toCharArray()
    }
}
