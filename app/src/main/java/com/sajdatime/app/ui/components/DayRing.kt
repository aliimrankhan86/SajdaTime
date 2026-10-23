package com.sajdatime.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.core.TRACKED_SLOTS
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** What a day's ring shows. A day before tracking began has no ring at all, so it is not a case here. */
enum class RingKind { SHARE, PAUSED, FUTURE }

/** 36dp rings, or 32dp when seven of them plus 4dp gaps (Space.Tight) do not fit [innerWidthDp]. */
internal fun weekRingDp(innerWidthDp: Float): Float = if (innerWidthDp >= 36f * 7 + 4f * 6) 36f else 32f

/**
 * One day as a ring (STEP-1-DESIGN 3a and 3c): a 3dp track in `outlineVariant` and an arc in `primary`
 * from 12 o'clock, clockwise in both reading directions (deliberate, BUILD-INSTRUCTIONS section 6),
 * as long as the share of five prayers ticked. Five of five fills the ring and draws the tick. A
 * paused day shows the pause glyph; a day still to come shows the track at 38 percent and nothing
 * else (disabled, so exempt from contrast). [halo] is the 2dp `outline` circle round it: today in the
 * week card, the chosen day in the grid.
 *
 * The geometry is the same with or without the halo, so rings never change size. The share grows with
 * `Draw` when a tick changes it and is drawn at its value, with no animation, when the screen opens
 * (section 6 row 2: `animateFloatAsState` starts at its target). Speech is the caller's, because it
 * says the day's name.
 */
@Composable
fun DayRing(
    ticked: Int,
    kind: RingKind,
    size: Dp,
    halo: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val share by animateFloatAsState(
        targetValue = ticked.toFloat() / TRACKED_SLOTS.size,
        animationSpec = Motion.draw(),
        label = "day ring",
    )
    val track = when (kind) {
        RingKind.FUTURE -> scheme.outlineVariant.copy(alpha = 0.38f)
        RingKind.PAUSED -> scheme.outline
        RingKind.SHARE -> scheme.outlineVariant
    }
    Canvas(modifier.size(size)) {
        val haloStroke = 2.dp.toPx()
        val ringStroke = 3.dp.toPx()
        val markStroke = 2.dp.toPx()
        // Halo at the very edge, a 2dp gap, then the ring: the same ring radius with or without the halo.
        val radius = this.size.minDimension / 2 - haloStroke - 2.dp.toPx() - ringStroke / 2
        if (halo) drawCircle(scheme.outline, radius = this.size.minDimension / 2 - haloStroke / 2, style = Stroke(haloStroke))
        if (kind == RingKind.SHARE && share >= 1f) {
            drawCircle(scheme.primary, radius = radius + ringStroke / 2)
            val side = radius * 1.5f
            drawCheck(scheme.onPrimary, 1f, markStroke, Offset(center.x - side / 2, center.y - side / 2), side)
            return@Canvas
        }
        drawCircle(track, radius = radius, style = Stroke(ringStroke))
        when (kind) {
            RingKind.SHARE -> if (share > 0f) {
                drawArc(
                    color = scheme.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * share,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(ringStroke, cap = StrokeCap.Round),
                )
            }
            RingKind.PAUSED -> drawPauseBars(scheme.outline, markStroke, half = radius * 0.4f, gap = radius * 0.3f)
            RingKind.FUTURE -> Unit
        }
    }
}
