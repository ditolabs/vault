# PwVault

Password manager Android lokal (offline, tanpa akun/server), ditulis pakai
Jetpack Compose. Vault terenkripsi AES-GCM, key diturunkan dari master
password/PIN pakai PBKDF2 (210k iterasi).

## Fitur

- **Onboarding** sekali muncul di awal (tanpa akun/OTP — PwVault memang nggak
  punya sistem login berbasis server)
- Unlock via **Password**, **PIN** (lockout setelah 5x salah), atau
  **Biometrik** (sidik jari/wajah — satu API yang sama, `BiometricPrompt`)
- Kategori entry (drawer menu) + search
- Tap entry = edit langsung (dengan show/hide password), tahan 3 detik =
  auto-copy password
- Warning icon buat entry yang passwordnya kosong + tombol "Bersihkan Data
  Kosong"
- **Autofill Service** asli Android (bukan Accessibility Service) — muncul
  sebagai saran isi otomatis di app/browser lain
- Export/Import JSON & CSV (plaintext — lihat catatan di bawah)
- **Backup Vault terenkripsi** lewat file picker/share-sheet Android (Drive,
  email, dst — file-nya tetap AES-GCM, PwVault sendiri tidak pernah
  konek ke internet)
- Bahasa Indonesia/English, tema Terang/Gelap/Ikuti sistem — semua switchable
  langsung di app, nggak perlu restart

## ⚠️ Soal export vs backup

- **Export** (JSON/CSV) = plaintext, buat pindah ke password manager lain.
  Jangan disimpan sembarangan, hapus setelah dipakai kalau bisa.
- **Backup** = file `.enc` mentah, tetap terenkripsi, aman disimpan di
  Drive/email — cuma bisa dibuka pakai master password/PIN vault ini.

## Download

Cara paling gampang: buka tab **Releases** di repo ini, download APK dari
rilis terbaru.

Kalau belum ada Release, atau mau versi paling baru dari commit terakhir:
tab **Actions** → job `Build APK` → download artifact `pwvault-debug-apk`.

### Soal signing

APK yang dihasilkan (baik dari Release maupun Actions artifact) itu
**debug-signed** (pakai debug keystore otomatis dari Gradle) — aman
diinstall sendiri atau dibagi ke orang yang dipercaya, tapi ini **beda**
dari signing rilis asli yang dibutuhkan kalau suatu saat mau naik ke Google
Play (itu butuh keystore rilis sendiri + akun Play Console, proses terpisah
yang belum di-setup di sini).

## Cara bikin Release baru

```bash
git tag v0.3.0
git push origin v0.3.0
```

Push tag otomatis memicu workflow `release.yml` — build APK, lalu attach ke
GitHub Release baru dengan nama tag itu.

## Cara build manual (opsional, tanpa Android Studio)

Butuh JDK 17 + Android SDK command-line tools, lalu:

```bash
gradle assembleDebug
```

Hasil APK ada di `app/build/outputs/apk/debug/app-debug.apk`.

## Struktur

```
crypto/       VaultCrypto (PBKDF2+AES-GCM), PinUnlock, BiometricVaultUnlock
data/         Entry model, VaultRepository, ExportImport, SettingsStore, VaultSession
autofill/     PwVaultAutofillService — real Android Autofill Framework
i18n/         Strings.kt — runtime ID/EN switch (no activity restart needed)
ui/screens/   Onboarding, Lock, VaultList, EntryEdit, Settings
ui/components/ Hand-drawn Canvas icons (see "Soal icon" di bawah) + VaultLogo
ui/theme/     Light/dark color schemes
```

## Soal icon

Semua icon digambar manual pakai Compose Canvas (`ui/components/AppIcons.kt`,
`VaultLogo.kt`) — bukan dari `material-icons-extended`. Itu keputusan sadar:
`-extended` isinya ~10.600 kelas icon unminified, yang pernah bikin build
awal (dari referensi Gemini) membengkak jadi 54MB untuk app yang cuma butuh
belasan icon. Kalau nambah fitur baru butuh icon yang belum ada, gambar
manual di `AppIcons.kt` mengikuti pola yang sudah ada, jangan tambah
dependency `-extended`.

## Roadmap / keterbatasan jujur

Lihat bagian "Known open items" di `DESIGN.md` — di antaranya: kategori masih
fixed (belum ada custom category), autofill matching masih heuristik
sederhana (bukan verifikasi domain kayak browser), autofill "save" (nangkep
password baru dari app lain) sengaja belum diimplementasi, dan APK belum
release-signed (lihat "Soal signing" di atas).
