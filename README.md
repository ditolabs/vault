# PwVault

Password manager Android sederhana. Vault terenkripsi AES-GCM, key diturunkan
dari master password pakai PBKDF2 (210k iterasi). Belum ada autofill service —
saat ini akses password lewat clipboard (auto-clear 30 detik).

## Cara pakai repo ini

1. Push folder ini ke repo GitHub kamu (`git init`, `git add .`, `git commit`, `git push`).
2. GitHub Actions (`.github/workflows/build.yml`) otomatis build APK debug tiap
   push ke branch `main`. Cek tab **Actions** → job selesai → download artifact
   `pwvault-debug-apk`.
3. APK debug itu belum di-sign untuk release, tapi bisa langsung di-install ke
   HP kamu sendiri (enable "install dari sumber tidak dikenal" / unknown sources).

## Kalau mau build manual (opsional, tanpa Android Studio)

Butuh: JDK 17 dan Android SDK command-line tools terpasang, lalu:

```
gradle assembleDebug
```

Hasil APK ada di `app/build/outputs/apk/debug/app-debug.apk`.

## Struktur

- `VaultCrypto.kt` — derivasi key (PBKDF2) + enkripsi/dekripsi (AES/GCM)
- `VaultRepository.kt` — baca/tulis file vault terenkripsi di storage privat app
- `MainActivity.kt` — UI: unlock/create vault, list entry, tambah/hapus, copy password

## Roadmap

- [ ] Autofill Service (biar bisa isi password otomatis di app/browser lain)
- [ ] Edit entry yang sudah ada
- [ ] Export/import vault terenkripsi buat backup
- [ ] Biometric unlock (opsional, di atas master password)
