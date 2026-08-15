package com.ditolabs.pwvault.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Export files are PLAINTEXT by nature (that's the point: another app needs to
 * read them). The UI must warn about this before writing to disk — this class
 * only does the format conversion, it doesn't decide when it's safe to call.
 */
object ExportImport {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun toJson(entries: List<Entry>): String = json.encodeToString(VaultData(entries))

    fun fromJson(text: String): List<Entry> =
        json.decodeFromString(VaultData.serializer(), text).entries

    /**
     * Generic CSV: name,url,username,password,notes,category — this is the closest
     * common denominator across Chrome/Bitwarden/most managers' CSV import, but
     * exact column names vary by target app; the user may need to remap headers
     * on the receiving end. We don't claim byte-for-byte compatibility with any
     * specific competitor's format.
     */
    fun toCsv(entries: List<Entry>): String {
        val header = listOf("name", "url", "username", "password", "notes", "category")
        val rows = entries.map { e ->
            listOf(e.title, e.url, e.username, e.password, e.notes, e.category)
        }
        return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(",") { csvEscape(it) } }
    }

    fun fromCsv(text: String): List<Entry> {
        val lines = parseCsvLines(text)
        if (lines.isEmpty()) return emptyList()
        val header = lines.first().map { it.trim().lowercase() }
        val nameIdx = header.indexOfFirst { it in listOf("name", "title") }
        val urlIdx = header.indexOfFirst { it == "url" }
        val userIdx = header.indexOfFirst { it in listOf("username", "login_username", "user") }
        val passIdx = header.indexOfFirst { it in listOf("password", "login_password") }
        val notesIdx = header.indexOfFirst { it in listOf("notes", "note") }
        val catIdx = header.indexOfFirst { it in listOf("category", "folder") }

        return lines.drop(1).filter { it.isNotEmpty() }.mapIndexed { i, row ->
            val title = row.getOrNull(nameIdx).orEmpty().ifBlank { "Tanpa nama" }
            val rawCategory = row.getOrNull(catIdx).orEmpty()
            Entry(
                id = "import-${System.currentTimeMillis()}-$i",
                title = title,
                username = row.getOrNull(userIdx).orEmpty(),
                password = row.getOrNull(passIdx).orEmpty(),
                url = row.getOrNull(urlIdx).orEmpty(),
                notes = row.getOrNull(notesIdx).orEmpty(),
                category = rawCategory.ifBlank { guessCategory(title) }
            )
        }
    }

    /** Best-effort category guess from the entry title, used only when the
     * imported file has no category column at all. Never overrides a category
     * the source file actually specified. */
    private fun guessCategory(title: String): String {
        val t = title.lowercase()
        return when {
            Regex("ig|insta|twitter|\\bx\\b|fb|facebook|tiktok|sosmed|linkedin").containsMatchIn(t) -> "sosmed"
            Regex("gmail|yahoo|outlook|email|mail").containsMatchIn(t) -> "email"
            Regex("kerja|slack|github|gitlab|jira|office|notion|figma").containsMatchIn(t) -> "kerja"
            Regex("shop|toko|market|belanja|store").containsMatchIn(t) -> "ecommerce"
            else -> "lainnya"
        }
    }

    private fun csvEscape(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else field
    }

    /** Minimal RFC4180-ish parser: handles quoted fields, escaped quotes, commas/newlines inside quotes. */
    private fun parseCsvLines(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var field = StringBuilder()
        var row = mutableListOf<String>()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < text.length && text[i + 1] == '"') { field.append('"'); i++ }
                        else inQuotes = false
                    } else field.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> { row.add(field.toString()); field = StringBuilder() }
                c == '\n' || c == '\r' -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(field.toString()); field = StringBuilder()
                    rows.add(row); row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows
    }
}
