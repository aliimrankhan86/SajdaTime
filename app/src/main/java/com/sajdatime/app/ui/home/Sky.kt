package com.sajdatime.app.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.app.ui.theme.SkyColours
import com.sajdatime.app.ui.theme.skyStyle
import com.sajdatime.core.SkyPeriod
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/*
 * The skies of Times (1.6, step 5; docs/plans/2026-10-release-1.6.0/STEP-5-DESIGN.md). A fixed
 * three stop sky behind the header and the next prayer card, and a small mark at the end of the
 * city row: a glyph for the period riding an arc of five dots, the five prayers.
 */

// ponytail: for the Q2 comparison picture only (the widget drawings against the Material icons the rows use).
// Flipped by a temporary edit, never by the user; removed once Ali has answered Q2.
internal var skyUseMaterialGlyphs = false

// Once per process the passed arc draws in (Q11); later returns to Times start at the end.
private var skyDrawn = false

private val MarkWidth = 104.dp
private val MarkHeight = 36.dp
private val GlyphSize = 18.dp

// The arc: chord 96dp, ends at y 28, apex at y 12 -> radius 80dp, centre (52, 92).
private const val ARC_RADIUS = 80f
private const val ARC_CENTRE_X = 52f
private const val ARC_CENTRE_Y = 92f
private val ARC_HALF_ANGLE = asin(48f / ARC_RADIUS) // radians, half the arc's sweep
private val ARC_LENGTH = ARC_RADIUS * 2f * ARC_HALF_ANGLE // dp
private const val GAP_TO_GLYPH = 11f // the glyph's 9dp radius plus a 2dp gap

/**
 * The sky's colours as a lambda read inside the draw. On a period change they cross-fade over
 * [Motion.draw] (row 6), from wherever the sky is on screen right now, so a second change in the
 * middle of a fade starts from what is showing. First composition draws at once, no entrance.
 * Not `Modifier.skyBackdrop(period)` with `composed`: a state holder plus a plain modifier keeps
 * to the current Compose guidance and reads the animation only in the draw phase.
 */
@Composable
internal fun rememberSkyColours(period: SkyPeriod): () -> SkyColours {
    val target = skyStyle(period)
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    // The animated value runs towards a counter, and base is where it was when the sky last changed,
    // so (t - base) / (generation - base) is 0 to 1 over every fade, mid fade or not.
    var generation by remember { mutableIntStateOf(0) }
    var base by remember { mutableFloatStateOf(0f) }
    val t by animateFloatAsState(generation.toFloat(), Motion.draw(), label = "sky")
    fun fraction(): Float =
        if (generation.toFloat() == base) 1f else ((t - base) / (generation - base)).coerceIn(0f, 1f)
    LaunchedEffect(target) {
        if (target != to) {
            val now = fraction()
            from = SkyColours(lerp(from.top, to.top, now), lerp(from.mid, to.mid, now), lerp(from.foot, to.foot, now))
            base = t
            to = target
            generation += 1
        }
    }
    // Remembered, so the per second clock tick does not hand the backdrop a new lambda (and a redraw).
    return remember {
        {
            val f = fraction()
            SkyColours(lerp(from.top, to.top, f), lerp(from.mid, to.mid, f), lerp(from.foot, to.foot, f))
        }
    }
}

/** One vertical gradient, top under the status bar to the page at the card's foot, drawn as one rect. */
internal fun Modifier.skyBackdrop(colours: () -> SkyColours): Modifier = drawBehind {
    val c = colours()
    drawRect(Brush.verticalGradient(0f to c.top, 0.55f to c.mid, 1f to c.foot, startY = 0f, endY = size.height))
}

/** The period's glyph: the app's own widget drawings, or (Q2 picture only) the Material icons the rows use. */
@Composable
private fun SkyGlyph(period: SkyPeriod, tint: Color, modifier: Modifier) {
    if (skyUseMaterialGlyphs) {
        val vector: ImageVector = when (period) {
            SkyPeriod.DAWN, SkyPeriod.EVENING -> Icons.Rounded.WbTwilight
            SkyPeriod.DAY -> Icons.Rounded.WbSunny
            SkyPeriod.NIGHT -> Icons.Rounded.NightsStay
        }
        Icon(vector, contentDescription = null, tint = tint, modifier = modifier)
    } else {
        val drawable = when (period) {
            SkyPeriod.DAWN -> R.drawable.widget_glyph_fajr
            SkyPeriod.DAY -> R.drawable.sky_glyph_day
            SkyPeriod.EVENING -> R.drawable.widget_glyph_maghrib
            SkyPeriod.NIGHT -> R.drawable.widget_glyph_isha
        }
        Icon(painterResource(drawable), contentDescription = null, tint = tint, modifier = modifier)
    }
}

/** Angle from the arc's vertical, in radians, at [u] (0 Fajr end, 1 Isha end) along the arc. */
private fun arcAngle(u: Float) = -ARC_HALF_ANGLE + u * 2f * ARC_HALF_ANGLE

/**
 * The sky mark: one spoken node, never actionable. [progress] is [com.sajdatime.core.Sky.progress];
 * [spoken] is built by the caller in composition ("Dawn, until 7:40 am").
 */
@Composable
internal fun SkyMark(period: SkyPeriod, progress: Float, spoken: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val drawIn = remember { Animatable(if (skyDrawn) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!skyDrawn) {
            skyDrawn = true // before the animation, so leaving Times mid draw does not replay it
            drawIn.animateTo(1f, Motion.draw())
        }
    }
    val arcInk = scheme.onSurfaceVariant
    val passedInk = scheme.primary
    // AbsoluteAlignment.TopLeft: the default TopStart puts the glyph at the right edge in right to left,
    // and the mirrored absoluteOffset would then add on top of it (review, 24 Oct).
    Box(
        modifier.size(MarkWidth, MarkHeight).semantics { contentDescription = spoken },
        contentAlignment = AbsoluteAlignment.TopLeft,
    ) {
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
            val d = density
            val r = ARC_RADIUS * d
            // The circle's centre sits on the mark's vertical axis, so mirroring leaves its box where it is.
            val topLeft = Offset((ARC_CENTRE_X - ARC_RADIUS) * d, (ARC_CENTRE_Y - ARC_RADIUS) * d)
            fun sweep(u0: Float, u1: Float, ink: Color, width: Dp) {
                // Mirroring once: in right to left the angle flips about the arc's vertical axis.
                val a = if (rtl) -arcAngle(u1) else arcAngle(u0)
                val b = if (rtl) -arcAngle(u0) else arcAngle(u1)
                drawArc(
                    color = ink,
                    startAngle = (-90f + a * 180f / PI.toFloat()),
                    sweepAngle = (b - a) * 180f / PI.toFloat(),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(2 * r, 2 * r),
                    style = Stroke(width = width.toPx(), cap = StrokeCap.Round),
                )
            }
            sweep(0f, 1f, arcInk, 1.5.dp)
            val shown = progress * drawIn.value
            val passedEnd = shown - GAP_TO_GLYPH / ARC_LENGTH
            if (passedEnd > 0f) sweep(0f, passedEnd, passedInk, 2.5.dp)
            for (i in 0..4) {
                val th = arcAngle(i / 4f)
                val x = (ARC_CENTRE_X + ARC_RADIUS * sin(th)) * d
                val y = (ARC_CENTRE_Y - ARC_RADIUS * cos(th)) * d
                drawCircle(arcInk, radius = 2.dp.toPx(), center = Offset(if (rtl) size.width - x else x, y))
            }
        }
        SkyGlyph(
            period = period,
            tint = passedInk,
            modifier = Modifier
                .size(GlyphSize)
                .absoluteOffset {
                    val th = arcAngle(progress * drawIn.value)
                    val x = (ARC_CENTRE_X + ARC_RADIUS * sin(th)).dp.toPx()
                    val y = (ARC_CENTRE_Y - ARC_RADIUS * cos(th)).dp.toPx()
                    val half = GlyphSize.toPx() / 2f
                    // The one mirror: absoluteOffset does not flip with the layout direction.
                    val mx = if (rtl) MarkWidth.toPx() - x else x
                    IntOffset((mx - half).toInt(), (y - half).toInt())
                }
                .clearAndSetSemantics {},
        )
    }
}
