package com.sajdatime.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A tasbeeh: a loop of eleven beads, the larger bead that closes it, and a tassel (Ali, 12 Nov 2026: "add
 * an icon there of tasbeeh"). Drawn here because Material has no prayer beads, and a tapping hand, the
 * first choice, says "tap" and not "tasbeeh".
 *
 * A picture of the object, not of any school's count: the number of beads is what reads clearly at 24dp,
 * and means nothing. One colour, filled, tinted by the caller like every other icon. In code rather than
 * a drawable so it adds no resource to the widget and asset inventories.
 */
val TasbeehIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Tasbeeh",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            fun bead(x: Float, y: Float, r: Float) {
                moveTo(x + r, y)
                arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
                arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
                close()
            }
            // Twelve places round a circle; the bottom one is the larger closing bead, set a little lower.
            for (place in 0 until 12) {
                if (place == 6) continue
                val angle = Math.toRadians(place * 30.0)
                bead(12f + 6.5f * sin(angle).toFloat(), 9.2f - 6.5f * cos(angle).toFloat(), 1.25f)
            }
            bead(12f, 15.9f, 1.75f)
            // The cord, then the tassel.
            moveTo(11.55f, 17.3f)
            lineTo(12.45f, 17.3f)
            lineTo(12.45f, 19.4f)
            lineTo(11.55f, 19.4f)
            close()
            moveTo(12f, 19f)
            lineTo(13.7f, 23f)
            lineTo(10.3f, 23f)
            close()
        }
    }.build()
}
