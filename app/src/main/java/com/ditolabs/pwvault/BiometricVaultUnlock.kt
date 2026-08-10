package com.ditolabs.pwvault

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
 * Stores the master password wrapped by an Android Keystore AES key that can only
 * be used after a successful biometric prompt. The password itself never leaves
 * the device and the wrapping key never leaves the secure hardware (StrongBox/TEE
 * where available).
 *
 * If the user adds/removes a fingerprint, the Keystore key is auto-invalidated
 * (setInvalidatedByBiometricEnrollment) and biometric unlock stops working until
 * re-enabled with the master password — this is intentional, not a bug.
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

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    /** Cipher ready for BiometricPrompt.CryptoObject, to be used to *encrypt and store* the password. */
    fun prepareEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher
    }

    /** Call after biometric auth succeeds on the encrypt cipher, to persist the wrapped password. */
    fun persistEncrypted(cipher: Cipher, password: CharArray) {
        val plaintext = String(password).toByteArray(Charsets.UTF_8)
        val ciphertext = cipher.doFinal(plaintext)
        val iv = cipher.iv
        // layout: [1 byte iv length][iv][ciphertext]
        val out = byteArrayOf(iv.size.toByte()) + iv + ciphertext
        storeFile.writeBytes(out)
    }

    /** Cipher ready for BiometricPrompt.CryptoObject, to be used to *decrypt* the stored password. */
    fun prepareDecryptCipher(): Cipher {
        val bytes = storeFile.readBytes()
        val ivLen = bytes[0].toInt()
        val iv = bytes.copyOfRange(1, 1 + ivLen)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher
    }

    /** Call after biometric auth succeeds on the decrypt cipher, to recover the password. */
    fun recoverPassword(cipher: Cipher): CharArray {
        val bytes = storeFile.readBytes()
        val ivLen = bytes[0].toInt()
        val ciphertext = bytes.copyOfRange(1 + ivLen, bytes.size)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8).toCharArray()
    }
}
