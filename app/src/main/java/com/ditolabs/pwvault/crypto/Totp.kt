package com.ditolabs.pwvault.crypto

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * RFC 6238 TOTP (the same algorithm Google Authenticator, Authy, etc. use).
 * No external library — just HMAC-SHA1 (javax.crypto) and a small Base32
 * decoder, since that's all the spec actually needs.
 */
object Totp {
    private const val STEP_SECONDS = 30L
    private const val DIGITS = 6

    /** secretBase32 is what services show you when you set up 2FA (e.g. "JBSWY3DPEHPK3PXP"). */
    fun currentCode(secretBase32: String, timeMillis: Long = System.currentTimeMillis()): String {
        val key = base32Decode(secretBase32)
        val counter = timeMillis / 1000 / STEP_SECONDS
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(counterBytes)

        val offset = (hash[hash.size - 1].toInt() and 0x0F)
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val code = binary % 1_000_000
        return code.toString().padStart(DIGITS, '0')
    }

    /** Seconds remaining until the current code expires — drives the countdown ring in the UI. */
    fun secondsRemaining(timeMillis: Long = System.currentTimeMillis()): Int {
        val secondsIntoStep = (timeMillis / 1000) % STEP_SECONDS
        return (STEP_SECONDS - secondsIntoStep).toInt()
    }

    fun isValidSecret(secretBase32: String): Boolean =
        try { base32Decode(secretBase32).isNotEmpty() } catch (e: Exception) { false }

    private val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    private fun base32Decode(input: String): ByteArray {
        val clean = input.trim().uppercase().replace(" ", "").replace("=", "")
        var bits = 0
        var value = 0
        val output = ArrayList<Byte>()
        for (c in clean) {
            val idx = BASE32_ALPHABET.indexOf(c)
            require(idx >= 0) { "Invalid Base32 character: $c" }
            value = (value shl 5) or idx
            bits += 5
            if (bits >= 8) {
                output.add(((value shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }
        return output.toByteArray()
    }
}
