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

Dial: ENERGY 2 / RHYTHM 1 / MOTION 1 (bumped from ENERGY 1 on request — the
onboarding screen and pill-shaped primary CTA read as more welcoming than the
original strictly-utilitarian pass; RHYTHM/MOTION stay calm since this is
still a security tool, not a marketing surface).

## First-launch onboarding

A one-time welcome screen (shown only when no vault exists yet) borrows the
visual warmth of a reference login-screen image — illustration, friendly
copy, generous spacing — but deliberately drops its actual mechanism: no
phone number, no OTP, no Google/Apple sign-in. PwVault has no accounts and no
server; "Mulai" leads straight into creating a local vault, the only "login"
this app has. Adopting the visual language without the auth mechanism it was
built for is the point — see R-30 (don't clone) and R-20 (swap-the-logo test).

## Backup

"Backup Vault" writes the RAW ENCRYPTED `vault.enc` bytes (never decrypted
plaintext) through Android's own file picker/share sheet — Drive and email
typically appear there without any Drive API or OAuth integration. This does
not compromise the offline positioning: PwVault contains no networking code
either way; the OS picker is a manual, user-initiated action, not something
the app does on its own or on a schedule. Restore requires a destructive-action
confirmation (R-26 — this action overwrites the whole vault, so it can't be a
single unconfirmed tap).

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

## Brutalist structure merge (this pass)

A neo-brutalist mockup (`pwvault_mockup_brutalist.jsx`) was explored for a UI
refresh. Its raw neon palette (yellow/pink/lime/lavender) was dropped — it
fails R-29 (max 2-3 core colors + 1 accent) and reads as a marketing surface,
not a tool holding real credentials. What's kept is the *structural*
language: a thick border plus a hard, non-blurred offset shadow that reads
as a physical, stackable card — reason: tactile "this is a solid object, not
a hover-glow web card" feel suits an offline vault, and it gives free, honest
tap feedback (the shadow compresses on press instead of a color/opacity
fade). No new colors were introduced — the merge is structural, not
chromatic; the existing palette above is unchanged.

Implementation: `ui/components/BrutalCard.kt`, reading `MaterialTheme.colorScheme`
(outline = border/shadow color, surface = fill) rather than a separate color
system, since the app already has one. Applied to: primary buttons (unlock,
save, delete, backup/restore, export/import, onboarding CTA), the language/
theme toggle chips, the offline-mode badge, the TOTP code card, the password-
generator preview, and the copied-to-clipboard toast. NOT applied to: PIN
digit boxes, the PIN keypad, and the biometric circle (thickened border only,
no shadow — a hard offset shadow across a dozen small repeated controls reads
as noisy rather than tactile); the vault-list entry row (kept its existing
tap/long-press-to-copy gesture — added the shadow layer manually behind it
rather than wrapping it in BrutalCard's own clickable, which would have
fought the custom gesture detector); `OutlinedTextField`s (left as Material
default — reimplementing floating labels/error states by hand wasn't worth
the risk for this pass); and `FilterChip`/`Switch` rows in Settings (already
a clean, low-risk list pattern).

Dial: unchanged at ENERGY 2 / RHYTHM 1 / MOTION 1 for now — the press-
compress shadow is a small motion increase, but not enough on its own to
warrant bumping the dial; revisit if more motion gets added elsewhere.
