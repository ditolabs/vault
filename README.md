# PwVault

Password manager Android, ditulis pakai Jetpack Compose. Vault terenkripsi
AES-GCM, key diturunkan dari master password/PIN pakai PBKDF2 (210k iterasi).

## Fitur

- Unlock via **Password**, **PIN** (dengan lockout setelah 5x salah), atau
  **Biometrik** (sidik jari/wajah — satu API yang sama, `BiometricPrompt`)
- Kategori entry (drawer menu) + search
- Tap entry = edit langsung, tahan 3 detik = auto-copy password
- **Autofill Service** asli Android (bukan Accessibility Service) — muncul
  sebagai saran isi otomatis di app/browser lain
- Export/Import JSON & CSV (lihat catatan keamanan di bawah)
- Bahasa Indonesia/English, tema Terang/Gelap/Ikuti sistem — semua switchable
  langsung di app, nggak perlu restart

## ⚠️ Soal export

File hasil export **TIDAK terenkripsi** — itu situasinya inheren kalau mau
dibaca app lain. Jangan simpan sembarangan, hapus setelah dipakai kalau bisa.

## Cara build

Push ke GitHub → tab **Actions** → job `Build APK` jalan otomatis → download
artifact `pwvault-debug-apk`.

Manual (tanpa Android Studio): `gradle assembleDebug`, hasil ada di
`app/build/outputs/apk/debug/app-debug.apk`.

## Struktur

```
crypto/   VaultCrypto (PBKDF2+AES-GCM), PinUnlock, BiometricVaultUnlock
data/     Entry model, VaultRepository, ExportImport, SettingsStore, VaultSession
autofill/ PwVaultAutofillService — real Android Autofill Framework
i18n/     Strings.kt — runtime ID/EN switch (no activity restart needed)
ui/       Compose screens (Lock, VaultList, EntryEdit, Settings) + theme
```

## Roadmap / keterbatasan jujur

Lihat bagian "Known open items" di `DESIGN.md` — di antaranya: kategori masih
fixed (belum ada custom category), autofill matching masih heuristik
sederhana (bukan verifikasi domain kayak browser), autofill "save" (nangkep
password baru dari app lain) sengaja belum diimplementasi.
