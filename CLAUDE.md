# CLAUDE.md — PwVault

For any UI/layout/copy work in this repo: read `DESIGN.md` first for
direction, then `ANTISLOP.md` as the filter before delivering. Run the
Delivery Gate in `ANTISLOP.md` before calling any UI change done.

Stack: Jetpack Compose (Kotlin), Material3. No XML layouts except the single
launch theme (`res/values/themes.xml`) and the autofill service descriptor.
"UI work" includes anything under `ui/screens/`, `ui/theme/`, `i18n/Strings.kt`,
and button/dialog copy anywhere else in `*.kt`.

Icon rule (non-negotiable, see DESIGN.md): only
`androidx.compose.material:material-icons-core`, never `-extended`.
