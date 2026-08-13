package com.ditolabs.pwvault.i18n

/**
 * All UI copy lives here, in both languages, rather than bolted-on
 * res/values-en string overrides — see DESIGN.md > Tone. Settings-driven
 * language switching is not wired up yet (tracked in DESIGN.md's known
 * open items); screens currently read [Strings.id] directly.
 */
enum class Lang { ID, EN }

data class StringSet(
    val appName: String,
    val lockOfflineBadge: String,
    val lockTitle: String,
    val masterKeyLabel: String,
    val lockWarning: String,
    val unlockCta: String,
    val vaultListNewEntry: String,
    val vaultListEmptyTitle: String,
    val vaultListEmptyBody: String,
    val usernameLabel: String,
    val passwordLabel: String,
    val totpLabel: String,
    val editEntry: String,
    val deleteEntry: String,
    val copiedToast: String,
)

object Strings {
    val id = StringSet(
        appName = "PWVAULT",
        lockOfflineBadge = "OFFLINE. LOKAL. TANPA AKUN.",
        lockTitle = "SISTEM TERKUNCI",
        masterKeyLabel = "MASTER KEY",
        lockWarning = "Lupa master key = data hilang permanen. Tidak ada \"reset password\".",
        unlockCta = "BUKA VAULT",
        vaultListNewEntry = "+ BARU",
        vaultListEmptyTitle = "VAULT MASIH KOSONG",
        vaultListEmptyBody = "Tambahkan entri pertama untuk mulai menyimpan kredensial secara lokal.",
        usernameLabel = "USERNAME / ID",
        passwordLabel = "PASSWORD",
        totpLabel = "2FA / AUTHENTICATOR",
        editEntry = "EDIT DATA",
        deleteEntry = "HAPUS",
        copiedToast = "Disalin ke clipboard",
    )

    val en = StringSet(
        appName = "PWVAULT",
        lockOfflineBadge = "OFFLINE. LOCAL. NO ACCOUNT.",
        lockTitle = "VAULT LOCKED",
        masterKeyLabel = "MASTER KEY",
        lockWarning = "Forget your master key and the data is gone for good. There is no \"reset password\".",
        unlockCta = "OPEN VAULT",
        vaultListNewEntry = "+ NEW",
        vaultListEmptyTitle = "VAULT IS EMPTY",
        vaultListEmptyBody = "Add your first entry to start storing credentials locally.",
        usernameLabel = "USERNAME / ID",
        passwordLabel = "PASSWORD",
        totpLabel = "2FA / AUTHENTICATOR",
        editEntry = "EDIT",
        deleteEntry = "DELETE",
        copiedToast = "Copied to clipboard",
    )

    fun forLang(lang: Lang): StringSet = if (lang == Lang.ID) id else en
}
