package com.ditolabs.pwvault.data

enum class Category { DEV, FINANCE, SOCIAL, EMAIL, OTHER }

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val category: Category,
    val hasTotp: Boolean,
)

/**
 * UI-scaffold sample data only (no vault engine wired up yet — see DESIGN.md
 * "Known open items"). Clearly placeholder values, not presented as real
 * user credentials anywhere in the UI.
 */
object SampleVault {
    val entries = listOf(
        VaultEntry("1", "github_pro", "contoh_user", "s4mpl3_p4ssw0rd!", Category.DEV, hasTotp = true),
        VaultEntry("2", "aws_server_01", "admin_demo", "c0ntoh_ganti_ini", Category.DEV, hasTotp = true),
        VaultEntry("3", "bank_contoh", "demo_corp", "j4ngan_p4k4i_ini", Category.FINANCE, hasTotp = false),
        VaultEntry("4", "twitter_alt", "@contoh_akun", "ganti_setelah_login", Category.SOCIAL, hasTotp = false),
    )
}
