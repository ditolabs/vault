package com.ditolabs.pwvault.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn icons for the glyphs `material-icons-core` doesn't ship (copy,
 * eye-open, eye-closed). Kept custom on purpose — pulling in
 * `material-icons-extended` for 3 icons was exactly the ~10,600-class bloat
 * this project's ANTISLOP rule rejects (see app/build.gradle.kts comment
 * and DESIGN.md). Paths use a 24x24 viewport, matching the material-icons-
 * core icons already in use so sizing stays consistent. Geometry is kept
 * deliberately simple (straight lines + circle approximations) rather than
 * chasing pixel-exact Material icon curves, to keep this file easy to
 * eyeball-verify.
 */
object PwVaultIcons {

    val ContentCopy: ImageVector by lazy {
        ImageVector.Builder(
            name = "ContentCopy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Back sheet (peeking out top-left)
                moveTo(4f, 2f)
                lineTo(15f, 2f)
                lineTo(15f, 4f)
                lineTo(6f, 4f)
                lineTo(6f, 16f)
                lineTo(4f, 16f)
                close()
                // Front sheet outline (with a hole cut out for the inside)
                moveTo(8f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 22f)
                lineTo(8f, 22f)
                close()
                moveTo(10f, 8f)
                lineTo(10f, 20f)
                lineTo(18f, 20f)
                lineTo(18f, 8f)
                close()
            }
        }.build()
    }

    val Visibility: ImageVector by lazy {
        ImageVector.Builder(
            name = "Visibility",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Almond-shaped eye outline
                moveTo(12f, 5f)
                curveTo(6.8f, 5f, 2.3f, 8.1f, 0.5f, 12.5f)
                curveTo(2.3f, 16.9f, 6.8f, 20f, 12f, 20f)
                curveTo(17.2f, 20f, 21.7f, 16.9f, 23.5f, 12.5f)
                curveTo(21.7f, 8.1f, 17.2f, 5f, 12f, 5f)
                close()
                // Iris ring (cut out via a smaller reverse path)
                moveTo(12f, 16.5f)
                curveTo(9.79f, 16.5f, 8f, 14.71f, 8f, 12.5f)
                curveTo(8f, 10.29f, 9.79f, 8.5f, 12f, 8.5f)
                curveTo(14.21f, 8.5f, 16f, 10.29f, 16f, 12.5f)
                curveTo(16f, 14.71f, 14.21f, 16.5f, 12f, 16.5f)
                close()
                moveTo(12f, 10.2f)
                curveTo(10.73f, 10.2f, 9.7f, 11.23f, 9.7f, 12.5f)
                curveTo(9.7f, 13.77f, 10.73f, 14.8f, 12f, 14.8f)
                curveTo(13.27f, 14.8f, 14.3f, 13.77f, 14.3f, 12.5f)
                curveTo(14.3f, 11.23f, 13.27f, 10.2f, 12f, 10.2f)
                close()
            }
        }.build()
    }

    val VisibilityOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "VisibilityOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Same eye shape as Visibility — the strike-through band below
            // is what reads as "hidden".
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 5f)
                curveTo(6.8f, 5f, 2.3f, 8.1f, 0.5f, 12.5f)
                curveTo(2.3f, 16.9f, 6.8f, 20f, 12f, 20f)
                curveTo(17.2f, 20f, 21.7f, 16.9f, 23.5f, 12.5f)
                curveTo(21.7f, 8.1f, 17.2f, 5f, 12f, 5f)
                close()
                moveTo(12f, 16.5f)
                curveTo(9.79f, 16.5f, 8f, 14.71f, 8f, 12.5f)
                curveTo(8f, 10.29f, 9.79f, 8.5f, 12f, 8.5f)
                curveTo(14.21f, 8.5f, 16f, 10.29f, 16f, 12.5f)
                curveTo(16f, 14.71f, 14.21f, 16.5f, 12f, 16.5f)
                close()
                moveTo(12f, 10.2f)
                curveTo(10.73f, 10.2f, 9.7f, 11.23f, 9.7f, 12.5f)
                curveTo(9.7f, 13.77f, 10.73f, 14.8f, 12f, 14.8f)
                curveTo(13.27f, 14.8f, 14.3f, 13.77f, 14.3f, 12.5f)
                curveTo(14.3f, 11.23f, 13.27f, 10.2f, 12f, 10.2f)
                close()
            }
            // Diagonal strike-through band, top-left to bottom-right.
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 2.5f)
                lineTo(5.5f, 1f)
                lineTo(21.5f, 21.5f)
                lineTo(19f, 23f)
                close()
            }
        }.build()
    }

    // Standard back-chevron path — pure straight lines, no curve-fitting risk.
    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                lineTo(7.83f, 11f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                lineTo(20f, 13f)
                close()
            }
        }.build()
    }

    // Simple padlock: rectangular body + a U-shaped shackle band.
    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Shackle (band with an inner and outer curve)
                moveTo(8f, 10f)
                curveTo(8f, 6.69f, 9.79f, 4f, 12f, 4f)
                curveTo(14.21f, 4f, 16f, 6.69f, 16f, 10f)
                lineTo(14f, 10f)
                curveTo(14f, 7.79f, 13.1f, 6f, 12f, 6f)
                curveTo(10.9f, 6f, 10f, 7.79f, 10f, 10f)
                close()
                // Body
                moveTo(5f, 10f)
                lineTo(19f, 10f)
                lineTo(19f, 21f)
                lineTo(5f, 21f)
                close()
            }
        }.build()
    }

    // Solid warning triangle — simplest reliable shape, no fill-hole tricks.
    val Warning: ImageVector by lazy {
        ImageVector.Builder(
            name = "Warning",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                lineTo(23f, 21f)
                lineTo(1f, 21f)
                close()
            }
        }.build()
    }
}
