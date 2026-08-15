package com.ditolabs.pwvault.data

/**
 * Heuristic-only — this is not a breach-database check (that would need a
 * network call, which this app deliberately never makes). It flags the two
 * things detectable purely from the vault's own contents: short/low-variety
 * passwords, and passwords reused across more than one entry.
 */
object PasswordAudit {

    enum class Issue { WEAK, DUPLICATE, EMPTY }

    data class Finding(val entry: Entry, val issues: Set<Issue>)

    fun audit(entries: List<Entry>): List<Finding> {
        val passwordCounts = entries.filter { it.password.isNotBlank() }
            .groupingBy { it.password }.eachCount()

        return entries.mapNotNull { entry ->
            val issues = mutableSetOf<Issue>()
            if (entry.password.isBlank()) issues.add(Issue.EMPTY)
            else {
                if (isWeak(entry.password)) issues.add(Issue.WEAK)
                if ((passwordCounts[entry.password] ?: 0) > 1) issues.add(Issue.DUPLICATE)
            }
            if (issues.isEmpty()) null else Finding(entry, issues)
        }
    }

    private fun isWeak(password: String): Boolean {
        if (password.length < 8) return true
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        val varietyCount = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }
        return varietyCount < 2
    }
}
