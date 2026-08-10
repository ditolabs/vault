package com.ditolabs.pwvault

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles deriving an AES key from the master password and encrypting/decrypting
 * the vault blob. File layout on disk: [16 bytes salt][12 bytes IV][ciphertext+tag]
 *
 * Nothing here is invented crypto: PBKDF2WithHmacSHA256 for key derivation,
 * AES/GCM/NoPadding for authenticated encryption. Both are standard JCA primitives.
 */
object VaultCrypto {

    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val PBKDF2_ITERATIONS = 210_000 // OWASP 2023+ recommendation for SHA-256
    private const val KEY_LENGTH_BITS = 256

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }

    /** Encrypts [plaintext] and returns salt+iv+ciphertext ready to write to disk. */
    fun encrypt(plaintext: ByteArray, password: CharArray, salt: ByteArray): ByteArray {
        val key = deriveKey(password, salt)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext)
        return salt + iv + ciphertext
    }

    /**
     * Decrypts a blob previously produced by [encrypt]. Throws if the password is
     * wrong or the data was tampered with (GCM auth tag fails).
     */
    fun decrypt(blob: ByteArray, password: CharArray): ByteArray {
        val salt = blob.copyOfRange(0, SALT_SIZE)
        val iv = blob.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
        val ciphertext = blob.copyOfRange(SALT_SIZE + IV_SIZE, blob.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    fun extractSalt(blob: ByteArray): ByteArray = blob.copyOfRange(0, SALT_SIZE)
}
