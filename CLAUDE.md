# CLAUDE.md — PwVault

For any UI/layout/copy work in this repo: read `DESIGN.md` first for
direction, then `ANTISLOP.md` as the filter before delivering. Run the
Delivery Gate in `ANTISLOP.md` before calling any UI change done.

Stack: Jetpack Compose (Kotlin), Material3. No XML layouts except the single
launch theme (`res/values/themes.xml`) and the autofill service descriptor.
"UI work" includes anything under `ui/screens/`, `ui/theme/`, `i18n/Strings.kt`,
and button/dialog copy anywhere else in `*.kt`.

Icon rule (non-negotiable, see DESIGN.md): no icon-font dependency at all.
Every glyph is hand-drawn in `ui/components/PwVaultIcons.kt`. (Started as
"core only, never -extended"; the needed glyphs turned out to live in
-extended, so all icons were hand-drawn instead rather than adding it back.)

Build/release: `.github/workflows/android-build.yml` runs on every push
(compile check only, no APK published). `.github/workflows/release.yml`
only runs when a `v*` tag is pushed — builds the debug APK and attaches it
to a GitHub Release automatically. Tagging is manual:
    git tag v0.3.2 && git push origin v0.3.2
