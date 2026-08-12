package com.ditolabs.pwvault.data

import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val id: String,
    var title: String,
    var username: String,
    var password: String,
    var category: String = "lainnya",
    var url: String = "",
    var notes: String = "",
    var updatedAtMillis: Long = System.currentTimeMillis()
)

@Serializable
data class VaultData(
    val entries: List<Entry> = emptyList()
)

object Categories {
    // id to (id-label, en-label) — kept small and fixed for now; "Kelola kategori"
    // in the drawer is a placeholder for a future custom-category editor.
    val all = listOf(
        "semua" to ("Semua" to "All"),
        "sosmed" to ("Sosial Media" to "Social Media"),
        "email" to ("Email" to "Email"),
        "kerja" to ("Pekerjaan" to "Work"),
        "ecommerce" to ("E-commerce" to "E-commerce"),
        "lainnya" to ("Lainnya" to "Other"),
    )
}
