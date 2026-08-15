package com.ditolabs.pwvault.crypto

import java.security.SecureRandom

object PasswordGenerator {
    private const val LOWER = "abcdefghijkmnopqrstuvwxyz" // no 'l' — visually ambiguous with '1'/'I'
    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ" // no 'I','O' — ambiguous with '1'/'0'
    private const val DIGITS = "23456789" // no '0','1' — ambiguous with 'O'/'l'
    private const val SYMBOLS = "!@#\$%^&*-_=+?"

    fun generate(length: Int, useUpper: Boolean, useDigits: Boolean, useSymbols: Boolean): String {
        val pool = buildString {
            append(LOWER)
            if (useUpper) append(UPPER)
            if (useDigits) append(DIGITS)
            if (useSymbols) append(SYMBOLS)
        }
        val random = SecureRandom()
        return (1..length).map { pool[random.nextInt(pool.length)] }.joinToString("")
    }
}
