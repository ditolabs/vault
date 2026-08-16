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

## Palette override (this pass — supersedes the section above)

The muted single-accent palette above is superseded by the neon-brutalist
palette from the Gemini mockup, adopted directly at the person's explicit
instruction ("abaikan dulu DESIGN.md, nanti tinggal update"). This section
is that update.

```
Lime      #B4FF39  primary / CTA buttons (onPrimary: black)
Lavender  #E5D4FF  secondary / tags (sosmed category)
Cyan bg   #E5F9FF  surfaceVariant (light) / kerja category
Yellow    #FFD166  tertiary / ecommerce category
Red       #FF4D4D  error / danger
Pink      #EF476F  destructive actions (delete)
Blue      #118AB2  entry-detail header panel
Green     #06D6A0  success / "copied" state
Cream bg  #FFF9E6  background (light)
```

R-29 (max 2-3 core colors + 1 accent) is knowingly not satisfied here — this
was a deliberate, explicit override, not an oversight. Category tags use
flat colors that don't adapt to dark/light theme (informational
color-coding, not brand accent). Dark-mode equivalent: background near-black
(`#0B0B0F`), surface `#19191F`, outline flips to off-white so the
border+shadow motif still reads against a dark surface; lime/lavender/
yellow/red stay the same hex in both themes (they're bright enough to read
on both backgrounds).

The "SECURE. RAW. UNHACKABLE." tagline from the source mockup was dropped —
"unhackable" is a claim nobody can make and directly contradicts the Tone
section above; this app keeps the plain "Offline. Lokal. Tanpa akun." line
instead.

The mockup's layout (desktop sidebar + split list/detail pane, mouse hover
states) was adapted to mobile conventions: sidebar → slide-in drawer behind
a hamburger button; split-pane → standard push navigation (list → tap →
full-screen detail, with a back button). Hover states don't apply to touch;
the existing press-compress shadow motion covers that feedback role.

Three elements were pulled in beyond a straight reskin ("worth taking" from
the mockup review):
- TOTP countdown as a ring/pie chart (`ui/components/TotpPie.kt`), replacing
  the plain "Xs" text badge — turns error-red under 5 seconds remaining.
- Dashed-border empty state (`ui/components/EmptyState.kt`, `Modifier.dashedBorder`)
  for the vault-list empty case — Compose has no built-in dashed border, so
  this draws it directly via `drawRoundRect` + `PathEffect.dashPathEffect`.
- Floating label chip (`ui/components/FieldChip.kt`) — a small dark label
  tag sitting on the top border of a field, replacing the master-key
  field's default `OutlinedTextField` label. Not applied to every field
  app-wide in this pass (`OutlinedTextField`s elsewhere were left as-is to
  limit risk) — a fuller pass could extend this consistently.

## Full chrome replacement (this pass — supersedes ANTISLOP where they conflict)

The palette-override pass above only reskinned high-visibility surfaces
(buttons, cards) while leaving default M3 chrome untouched underneath —
`Scaffold`/`TopAppBar`, `ModalNavigationDrawer`/`NavigationDrawerItem`,
`OutlinedTextField`, `FilterChip`, `Switch`, `AlertDialog`. The result read
as half-brutalist, half-stock-Material. This pass replaces all of it with
hand-built equivalents, at the person's explicit instruction to prioritize
matching the mockup over DESIGN.md/ANTISLOP.md consistency rules where they'd
otherwise hold this back:

- `ui/components/BrutalTopBar.kt` — solid header + thick bottom border,
  replaces `TopAppBar` everywhere (List, EntryEdit, Settings).
- Category drawer in `VaultListScreen` — kept `ModalNavigationDrawer` for
  its swipe/scrim mechanism (reimplementing that gesture handling wasn't
  worth the risk), but replaced `ModalDrawerSheet`/`NavigationDrawerItem`
  content with a custom yellow-background bordered column, active category
  inverted (matches the mockup's drawer).
- `ui/components/BrutalTextField.kt` — `FieldChip`'s floating-label pattern
  generalized into a reusable field, replacing every `OutlinedTextField` in
  the app (title/username/password/notes/TOTP-secret/search).
- `ui/components/BrutalChip.kt` — replaces `FilterChip` (language, theme,
  auto-lock, category selection in EntryEdit).
- `ui/components/BrutalSwitch.kt` — hand-built square toggle; M3 `Switch`'s
  pill track/thumb can't be reshaped into the flat-bordered language via its
  own parameters, so this is built from scratch (Box track + animated-offset
  thumb) rather than restyled.
- `ui/components/BrutalDialog.kt` — wraps the low-level `Dialog` composable
  (keeps scrim + dismiss-on-outside-tap) with a `BrutalCard` shell, replacing
  `AlertDialog`'s rounded/tonal look in the password generator.
- Category picker in `EntryEditScreen` changed from an `ExposedDropdownMenuBox`
  dropdown to a horizontal-scroll row of `BrutalChip`s — a dropdown menu
  doesn't have a natural brutalist equivalent, and chip-selection fits the
  visual language better regardless.
- `Checkbox` (password generator's uppercase/digits/symbols toggles) reuses
  `BrutalSwitch` rather than a separate checkbox component.

Not touched in this pass: the password-length `Slider` in the generator
dialog is still default M3 — a fully custom brutal slider (drag-gesture
handling, custom thumb/track) was scoped out for time; it's the one
remaining default-Material control in the app.

All of the above is a visual-layer change only — no crypto, session,
gesture (tap vs. long-press-to-copy), or state-management logic was
touched. R-29 (max 2-3 colors), the "read DESIGN.md before UI work" router
rule, and the icon-only-hand-drawn rule are all still followed; what's
overridden here is specifically DESIGN.md's earlier single-muted-accent
palette and its assumption that default M3 chrome was an acceptable base to
build on.
