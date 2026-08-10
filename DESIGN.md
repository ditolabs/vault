# DESIGN.md — PwVault

## Identity

PwVault is a local-only Android password manager (ditolabs). No cloud sync, no
account, no server — everything lives encrypted on-device. Positioning: quiet,
functional, trustworthy utility. Not a fintech-style "wow" app; the product's
whole pitch is that it does *less* (less surface area, less trust required),
so the UI shouldn't oversell with flashy motion or decoration.

Shares the ditolabs "ink" dark-surface family with sibling projects (Berkas)
for brand consistency across the org, but uses its own accent and motif so it
doesn't read as a reskin of Berkas.

## Direction

- Dark ink background, same family as Berkas (`--ink-900/800/700`) — reason:
  org-level visual consistency across ditolabs tools, both are "serious utility"
  apps meant to be trusted with sensitive files/data.
- Single accent: muted vault green (`--vault-green`) used only for the primary
  unlock/save actions and the biometric icon — reason: green reads as
  "safe/unlocked" without resorting to a literal padlock-everywhere motif.
- Identity motif: entry passwords and the master-password field are rendered
  in **monospace** — reason: monospace disambiguates similar characters
  (`1/l/I`, `0/O`) which is a real functional need for a password tool, not
  decoration.
- Border radius: single scale, 4dp (inputs/list rows) / 12dp (cards, dialogs) /
  28dp (FAB, which is a circle) — reason: distinguishes "inline control" from
  "container" without making everything a pill.
- No card grids, no feature-tour screens, no onboarding carousel — reason: a 4
  screen-state app (lock, list, add/edit, detail) doesn't need template
  marketing-app scaffolding.

## Palette

```
--ink-900:#0b0d12   background
--ink-800:#12141b   surface (cards, dialogs)
--ink-700:#171a24   raised surface (inputs)
--ink-line:#262b38  borders/dividers
--paper:#e9eae2     primary text on dark surfaces
--dim:#8b8f9c        secondary text (usernames, hints)
--vault-green:#3ea87c   primary accent (buttons, biometric icon, focus ring)
--vault-green-dim:#2c7a5a  pressed/disabled state of accent
--danger:#c96a5a     delete/destructive actions only
```

Neutrals (`ink-*`, `paper`, `dim`) don't count against the R-29 palette cap;
active palette is green (1 accent) + danger (functional, not decorative) = within cap.

## Tone

Copy is plain, functional Indonesian. Buttons say what they do to *this* vault
("Buat Vault Baru", "Buka Vault", "Simpan Entry"), not generic verbs. No
"seamless", "aman 100%", "powerful" — a password tool overclaiming security is
actively counterproductive to trust.

## Dial

Dial: ENERGY 1 / RHYTHM 1 / MOTION 1
(Utility/security tool for a single user — calm, predictable, no decorative
motion. A vault app that "performs" liveliness undermines its own pitch.)

## Known open items

- Icons are currently stock Android drawables (`ic_lock_lock`, `ic_input_add`)
  as honest placeholders (R-23) — swap for a custom mark when there's a real
  app icon design, don't treat these as final branding.
- No light theme yet — dark-only is a deliberate choice per R-21 (this is a
  utility tool used briefly and often in low-light/on-the-go contexts), not a
  deferred toggle.
