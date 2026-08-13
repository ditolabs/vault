# DESIGN.md — PwVault

## Identity

PwVault is a local-only Android password manager (ditolabs). No cloud sync, no
account, no server — everything lives encrypted on-device. Positioning: quiet,
functional, trustworthy utility, not a fintech-style "wow" app.

Built with Jetpack Compose. Shares the ditolabs "ink" dark-surface family with
sibling projects (Berkas) for brand consistency, with its own accent + motif.

## Direction (updated — brutalist structure, toned-down color)

A neo-brutalist mockup (`pwvault_mockup_brutalist.jsx`) was explored as a
reference for structure and tactility. Its raw neon palette (yellow / pink /
lime / lavender) is dropped — it fails R-29 (max 2-3 core colors + 1 accent)
and reads as a marketing surface, not a security tool holding real
credentials. What's kept is the *structural* language: thick borders and a
hard offset shadow that reads as a physical, stackable card — reason: a
tactile "this is a solid object, not a hover-glow web card" feel suits an
offline vault better than soft elevation, and it gives free, honest tap
feedback (the shadow compresses on press instead of a color/opacity fade).

- Dark ink background (`ink-900/800/700`) AND a light "paper" variant, both
  fully functional, user-switchable in Settings (system/light/dark) — reason:
  requested directly; a security tool shouldn't force a theme on someone who
  finds dark-only harder to read.
- Single accent: muted vault green, darkened for AA contrast on the light
  variant — reason: green reads "safe/unlocked" without a literal padlock
  motif. No second decorative color was added when merging in the brutalist
  structure — R-29 stays satisfied (green = primary accent, danger red =
  functional-only, never decorative).
- **New — hard card edge + offset shadow**: every interactive card/button
  renders a 2.5dp ink-line border plus a solid (non-blurred) 3dp offset
  "shadow" in `ink-900` (dark) / `light-line` (light), not a soft blur —
  reason: borrowed from the brutalist reference's tactility without its
  color noise; a blurred Material shadow would clash with the flat, inky
  palette. On press, the shadow offset shrinks to 1dp so the card visibly
  "sits down" — this is the app's only motion beyond screen transitions.
- Identity motif: entry passwords and the master-password/PIN fields render in
  **monospace** — reason: disambiguates similar characters (`1/l/I`, `0/O`),
  a functional need for a password tool. TOTP codes also render monospace at
  larger scale (borrowed directly from the brutalist mockup's big mono digit
  block) — reason: a 6-digit code someone is typing under a 30s countdown
  benefits from the same legibility case as a password.
- Border radius: 4dp (inline controls: buttons, inputs, tags) / 12dp
  (containers: rows, dialogs) / circular (FAB, PIN keypad, biometric icon) —
  unchanged from the original scale; the brutalist reference's tighter 6px
  radius was not adopted, to keep R-11 (one consistent scale) rather than
  running two.
- Icon set: `material-icons-core` only, hand-picked. Not `-extended` — that
  package ships ~10,600 icon classes unminified, which is what bloated an
  earlier build (from Gemini) to 54MB for a 15-icon app.

## Palette

```
Dark:
--ink-900:#0B0D12  background      --paper:#E9EAE2      text
--ink-800:#12141B  surface         --dim:#8B8F9C        secondary text
--ink-700:#171A24  raised/inputs   --vault-green:#3EA87C primary accent
--ink-line:#262B38 borders/shadow  --danger:#C96A5A     destructive

Light:
--light-bg:#F4F3EE      background   --light-text:#14161C   text
--light-surface:#FFFFFF surface      --light-dim:#6B6F7A    secondary text
--light-line:#DEDCD3    borders/shadow --green-on-light:#2C7A5A primary accent
                                     --danger-on-light:#A9503F destructive
```

No colors were added versus the pre-brutalist palette — the merge is
structural (borders/shadow/press-state), not chromatic.

## Tone

Copy is plain, functional — Indonesian and English both fully supported and
switchable in Settings (not just translated strings bolted on: button labels,
lockout messages, and the export-file warning all exist in both, routed
through `i18n/Strings.kt`). No "seamless", "aman 100%", "powerful" — a
password tool overclaiming security is counterproductive to trust.

## Dial

Dial: ENERGY 2 / RHYTHM 2 / MOTION 2.
RHYTHM moved 1 → 2 and MOTION moved 1 → 2 versus the earlier pass — reason:
the brutalist card's thick border + press-compress shadow reads as more
rhythmic/structural than flat Material cards, and the press animation is a
real (if small) motion increase. ENERGY stays at 2 — the palette itself did
not get louder, only more structural.

## First-launch onboarding

A one-time welcome screen (shown only when no vault exists yet) borrows the
visual warmth of a reference login-screen image — illustration, friendly
copy, generous spacing — but deliberately drops its actual mechanism: no
phone number, no OTP, no Google/Apple sign-in. PwVault has no accounts and no
server; "Mulai" leads straight into creating a local vault, the only "login"
this app has. Adopting the visual language without the auth mechanism it was
built for is the point — see R-30 (don't clone) and R-20 (swap-the-logo
test).

## Backup

"Backup Vault" writes the RAW ENCRYPTED `vault.enc` bytes (never decrypted
plaintext) through Android's own file picker/share sheet — Drive and email
typically appear there without any Drive API or OAuth integration. This does
not compromise the offline positioning: PwVault contains no networking code
either way; the OS picker is a manual, user-initiated action, not something
the app does on its own or on a schedule. Restore requires a
destructive-action confirmation (R-26 — this action overwrites the whole
vault, so it can't be a single unconfirmed tap).

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
  schema — closest common denominator across most managers' CSV import, not
  a guaranteed byte-for-byte match with any specific competitor.
- This pass (Lock / Vault List / Entry Detail) is UI scaffold only: no
  crypto/storage is wired up yet. Copy/reveal/navigation are real, functional
  UI state; the master key check, encrypted storage, and TOTP generation are
  stubbed with clearly-fake sample data pending the vault engine — flagged
  here per R-38 rather than left undocumented.
