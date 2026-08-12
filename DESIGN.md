# DESIGN.md — PwVault

## Identity

PwVault is a local-only Android password manager (ditolabs). No cloud sync, no
account, no server — everything lives encrypted on-device. Positioning: quiet,
functional, trustworthy utility, not a fintech-style "wow" app.

Built with Jetpack Compose. Shares the ditolabs "ink" dark-surface family with
sibling projects (Berkas) for brand consistency, with its own accent + motif.

## Direction

- Dark ink background (`ink-900/800/700`) AND a light "paper" variant, both
  fully functional, user-switchable in Settings (system/light/dark) — reason:
  requested directly; a security tool shouldn't force a theme on someone who
  finds dark-only harder to read.
- Single accent: muted vault green, darkened for AA contrast on the light
  variant — reason: green reads "safe/unlocked" without a literal padlock motif.
- Identity motif: entry passwords and the master-password/PIN fields render in
  **monospace** — reason: disambiguates similar characters (`1/l/I`, `0/O`),
  a functional need for a password tool.
- Border radius: 4dp (inline controls: buttons, inputs) / 12dp (containers:
  rows, dialogs) / circular (FAB, PIN keypad, biometric icon).
- Icon set: `material-icons-core` only, hand-picked. Not `-extended` — that
  package ships ~10,600 icon classes unminified, which is what bloated an
  earlier build (from Gemini) to 54MB for a 15-icon app.

## Palette

```
Dark:
--ink-900:#0B0D12  background      --paper:#E9EAE2      text
--ink-800:#12141B  surface         --dim:#8B8F9C        secondary text
--ink-700:#171A24  raised/inputs   --vault-green:#3EA87C primary accent
--ink-line:#262B38 borders         --danger:#C96A5A     destructive

Light:
--light-bg:#F4F3EE      background   --light-text:#14161C   text
--light-surface:#FFFFFF surface      --light-dim:#6B6F7A    secondary text
--light-line:#DEDCD3    borders      --green-on-light:#2C7A5A primary accent
                                     --danger-on-light:#A9503F destructive
```

## Tone

Copy is plain, functional — Indonesian and English both fully supported and
switchable in Settings (not just translated strings bolted on: button labels,
lockout messages, and the export-file warning all exist in both). No
"seamless", "aman 100%", "powerful" — a password tool overclaiming security is
counterproductive to trust.

## Dial

Dial: ENERGY 1 / RHYTHM 1 / MOTION 1
Utility/security tool — calm, predictable, no decorative motion.

## Known open items / honest limitations

- Category list is currently a fixed set (Sosmed/Email/Kerja/E-commerce/
  Lainnya) — "Kelola kategori" in the drawer is a placeholder, custom
  categories aren't built yet.
- Autofill Service matches a vault entry to the requesting app by a simple
  substring match against the package name — no domain/public-suffix
  verification like a browser does. Useful, not a security guarantee against
  a lookalike package.
- Autofill "save" (capturing new logins typed in other apps) is intentionally
  not implemented — silently saving typos/junk from another app's form is
  worse than making the user add it manually in PwVault.
- CSV export/import uses a generic `name,url,username,password,notes,category`
  schema — it's the closest common denominator across most managers' CSV
  import, not a guaranteed byte-for-byte match with any specific competitor's
  column names.
- App launcher icon is still a stock Android drawable placeholder (R-23).
