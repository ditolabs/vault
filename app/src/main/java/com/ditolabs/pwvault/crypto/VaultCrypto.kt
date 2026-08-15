package com.ditolabs.pwvault.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Standard JCA primitives only: PBKDF2WithHmacSHA256 + AES/GCM/NoPadding. No invented crypto. */
object VaultCrypto {
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val PBKDF2_ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256

    fun generateSalt(): ByteArray = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }

    private fun deriveKey(secret: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(secret, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plaintext: ByteArray, secret: CharArray, salt: ByteArray): ByteArray {
        val key = deriveKey(secret, salt)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        return salt + iv + cipher.doFinal(plaintext)
    }

    fun decrypt(blob: ByteArray, secret: CharArray): ByteArray {
        val salt = blob.copyOfRange(0, SALT_SIZE)
        val iv = blob.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
        val ciphertext = blob.copyOfRange(SALT_SIZE + IV_SIZE, blob.size)
        val key = deriveKey(secret, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    fun extractSalt(blob: ByteArray): ByteArray = blob.copyOfRange(0, SALT_SIZE)
}
