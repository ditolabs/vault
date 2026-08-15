# PwVault 🔐

A privacy-first, fully offline Android password manager built with Jetpack Compose.

PwVault is designed for people who want full control over their own data. There's no account to create, no server involved, and your passwords never touch the internet unless you explicitly move them yourself.

## ✨ Why PwVault

- **100% offline** — no accounts, no OTP, no telemetry. The vault lives strictly on your device; PwVault contains no networking code at all.
- **Real encryption, plainly stated** — entries are stored in an AES-GCM encrypted vault. Keys are derived from your Master Password or PIN via PBKDF2 with 210,000 iterations.
- **Native Android Autofill** — autofills across other apps and browsers using the official Android Autofill Framework, not an Accessibility Service workaround.
- **Unlock your way** — Master Password, PIN (with a 5-attempt lockout), or biometrics (fingerprint/face — both go through the same `BiometricPrompt` API).
- **Genuinely lightweight** — the app deliberately avoids `material-icons-extended`, a dependency that ships ~10,600 icon classes unminified. An earlier build that included it came out to 54MB for an app that uses about 15 icons. Every icon here is hand-drawn with Compose Canvas instead.
- **Switch instantly** — English/Indonesian and Light/Dark/System theme, no restart required.
- **Smart interactions** — tap an entry to edit it, hold for 3 seconds to copy the password. Entries with an empty password get flagged with a warning icon, plus a one-tap cleanup action.

## 🛡️ Backup vs. Export

- **Encrypted backup (`.enc`)** — a raw copy of the AES-GCM encrypted vault file. Safe to send to Google Drive, email, or local storage through Android's native share sheet — it can't be opened without this vault's password/PIN.
- **Plaintext export (JSON/CSV)** — for migrating to another manager. These files are *not* encrypted; delete them once you're done.

## 🚀 Get Started

**Download:** grab the latest APK from the [Releases](../../releases) tab.

**Bleeding edge:** the **Actions** tab → `Build APK` → download the `pwvault-debug-apk` artifact from the latest run.

**A note on signing:** every APK above is debug-signed (Gradle's auto-generated debug key), not release-signed. Fine to install yourself or share with people you trust — not the same thing as a Google Play release, which would need its own signing keystore.

## For developers

```bash
gradle assembleDebug        # build locally, output in app/build/outputs/apk/debug/
git tag v0.3.0 && git push origin v0.3.0   # cuts a new GitHub Release automatically
```

Project structure, design rationale, and known limitations are documented in
`DESIGN.md` and `CLAUDE.md`.
