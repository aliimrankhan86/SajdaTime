package com.sajdatime.app.ui.components

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.core.TickState

/**
 * One prayer's tick in "My prayers" (DESIGN_SYSTEM §4): a 24dp mark in a 48dp target, drawn on a
 * Canvas so the fill and the tick can animate (BUILD-INSTRUCTIONS §6 row 1: `Standard` in, `Quick`
 * out on an untick, none when the screen opens).
 *
 * An unticked prayer is a plain outline circle in every case, never red, never a badge, never
 * counted: the app records, it does not judge (Ali, 16 Oct 2026). A prayer whose time has not come
 * is the same circle at 38 percent and cannot be ticked. A paused day is the circle with two bars
 * at full contrast, also not tickable. Its own screen reader stop, "Fajr, prayed", because it sits
 * inside a row that is otherwise read as one.
 *
 * [label] is what it is spoken as before the state: the prayer, or on the history sheet the date and
 * the prayer.
 */
@Composable
fun Tick(
    state: TickState,
    label: String,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    /** A record from a newer version of the app: shown, never changed. */
    readOnly: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val ticked = state == TickState.TICKED
    val enabled = !readOnly && (state == TickState.TICKED || state == TickState.NOT_TICKED)
    val spoken = stringResource(
        R.string.tracker_tick_a11y,
        label,
        stringResource(
            when (state) {
                TickState.TICKED -> R.string.tracker_state_prayed
                TickState.NOT_DUE_YET -> R.string.tracker_state_not_due
                TickState.PAUSED -> R.string.tracker_state_paused
                TickState.NOT_TICKED -> R.string.tracker_state_not_ticked
            },
        ),
    )
    // Ponytail: one progress value for fill and tick, with the shorter token on the way back (an
    // untick is an exit, section 6). Read inside the draw lambda, so only drawing reruns.
    val progress by animateFloatAsState(
        targetValue = if (ticked) 1f else 0f,
        animationSpec = if (ticked) Motion.standard() else Motion.quick(),
        label = "tick",
    )
    val ring = scheme.outline
    val fill = scheme.primary
    val mark = scheme.onPrimary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            // Round, so the press ripple matches the round mark, as Material's IconButton does.
            .clip(CircleShape)
            .toggleable(value = ticked, enabled = enabled, role = Role.Checkbox) { on ->
                haptics.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                onChange(on)
            }
            // toggleable already says checked or not, and disabled; this adds which prayer.
            .semantics { contentDescription = spoken },
    ) {
        Canvas(Modifier.size(24.dp)) {
            val stroke = 2.dp.toPx()
            val radius = (size.minDimension - stroke) / 2
            val ringColor = lerp(
                if (state == TickState.NOT_DUE_YET) ring.copy(alpha = 0.38f) else ring,
                fill,
                progress,
            )
            if (progress > 0f) drawCircle(fill.copy(alpha = progress), radius = radius + stroke / 2)
            drawCircle(ringColor, radius = radius, style = Stroke(stroke))
            if (progress > 0f) drawCheck(mark, progress, stroke)
            if (state == TickState.PAUSED) drawPauseBars(ring, stroke)
        }
    }
}

/**
 * The tick, drawn from its short stroke to its long one as [progress] goes 0 to 1 (PathMeasure), inside
 * the square of [side] at [origin]. Shared with the week and grid rings.
 */
internal fun DrawScope.drawCheck(
    color: Color,
    progress: Float,
    stroke: Float,
    origin: Offset = Offset.Zero,
    side: Float = size.minDimension,
) {
    val full = Path().apply {
        moveTo(origin.x + side * 0.28f, origin.y + side * 0.52f)
        lineTo(origin.x + side * 0.44f, origin.y + side * 0.68f)
        lineTo(origin.x + side * 0.74f, origin.y + side * 0.34f)
    }
    val part = Path()
    val measure = PathMeasure().apply { setPath(full, false) }
    measure.getSegment(0f, measure.length * progress, part, true)
    drawPath(part, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** Two upright bars either side of [centre]: the paused glyph, in the ring's own colour. */
internal fun DrawScope.drawPauseBars(
    color: Color,
    stroke: Float,
    centre: Offset = center,
    half: Float = size.height * 0.18f,
    gap: Float = size.width * 0.14f,
) {
    for (x in listOf(centre.x - gap, centre.x + gap)) {
        drawLine(color, Offset(x, centre.y - half), Offset(x, centre.y + half), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
