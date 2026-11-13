package com.sajdatime.app.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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
 * city row: a glyph for the period riding an arc of five dots, the five prayers. Since 1.7 it is
 * the full width band [SkyBand] under the date line.
 */


// Once per process the passed arc draws in (Q11); later returns to Times start at the end.
private var skyDrawn = false

/**
 * The sky's colours as a lambda read inside the draw. On a period change they cross-fade over
 * [Motion.draw] (row 6), from wherever the sky is on screen right now, so a second change in the
 * middle of a fade starts from what is showing. First composition draws at once, no entrance.
 * A theme change alone snaps with the rest of the page; theme and period changing in the same frame
 * fade from the old theme's colours, deliberately (one rare frame, not worth a second path).
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
    var shownPeriod by remember { mutableStateOf(period) }
    val t by animateFloatAsState(generation.toFloat(), Motion.draw(), label = "sky")
    fun fraction(): Float =
        if (generation.toFloat() == base) 1f else ((t - base) / (generation - base)).coerceIn(0f, 1f)
    LaunchedEffect(target) {
        if (period == shownPeriod) {
            // Same period, new colours: the theme changed. Snap with the rest of the page, never fade (review, 24 Oct).
            from = target
            to = target
        } else if (target != to) {
            shownPeriod = period
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

/** The period's glyph: the app's own widget drawings (Ali chose them over the row icons, 24 Oct 2026, Q2). */
@Composable
private fun skyGlyphPainter(period: SkyPeriod): Painter {
    val drawable = when (period) {
        SkyPeriod.DAWN -> R.drawable.widget_glyph_fajr
        SkyPeriod.DAY -> R.drawable.sky_glyph_day
        SkyPeriod.EVENING -> R.drawable.widget_glyph_maghrib
        SkyPeriod.NIGHT -> R.drawable.widget_glyph_isha
    }
    return painterResource(drawable)
}

private val BandHeight = 52.dp
private val BandInset = 20.dp // arc ends this far from each side
private val DiscSize = 32.dp
private val GlyphSize = 20.dp
private val TickSize = 8.dp
private const val RISE = 20f // dp, ends to apex
private const val ENDS_Y = 34f // dp from the band's top
private const val GAP_TO_MARKER = 18f // the disc's 16dp radius plus a 2dp gap

/**
 * The sky band (1.7, design G, Ali 3 Nov 2026: the old corner mark was "too small and easier to miss"): a full width
 * arc under the date line, five ticks for the five prayers, and a lit disc with the period's glyph riding it at
 * [progress] ([com.sajdatime.core.Sky.progress], 0 Fajr end, 1 Isha end). Ticks already reached are filled, ticks
 * to come are rings, so the difference is not colour alone. One spoken node ([spoken], "Day, until 3:12 pm"),
 * never actionable, no words drawn. Right to left mirrors once, in x, as the old mark did.
 */
@Composable
internal fun SkyBand(period: SkyPeriod, progress: Float, spoken: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val drawIn = remember { Animatable(if (skyDrawn) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!skyDrawn) {
            skyDrawn = true // before the animation, so leaving Times mid draw does not replay it
            drawIn.animateTo(1f, Motion.draw())
        }
    }
    val glyph = skyGlyphPainter(period)
    val track = scheme.onSurfaceVariant
    val ink = scheme.primary
    val paper = scheme.surface
    Box(modifier.fillMaxWidth().height(BandHeight).semantics { contentDescription = spoken }) {
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
            val d = density
            val chord = size.width - 2 * BandInset.toPx()
            val rise = RISE * d
            val r = chord * chord / (8f * rise) + rise / 2f
            val half = asin(chord / 2f / r)
            val cx = size.width / 2f
            val cy = (ENDS_Y - RISE) * d + r // circle centre; apex sits at y = ENDS_Y - RISE
            fun angle(u: Float) = -half + u * 2f * half
            fun sweep(u0: Float, u1: Float, colour: Color, width: Dp) {
                // Mirroring once: in right to left the angle flips about the arc's vertical axis.
                val a = if (rtl) -angle(u1) else angle(u0)
                val b = if (rtl) -angle(u0) else angle(u1)
                drawArc(
                    color = colour,
                    startAngle = -90f + a * 180f / PI.toFloat(),
                    sweepAngle = (b - a) * 180f / PI.toFloat(),
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(2 * r, 2 * r),
                    style = Stroke(width = width.toPx(), cap = StrokeCap.Round),
                )
            }
            fun point(u: Float): Offset {
                val th = angle(u)
                val x = cx + r * sin(th)
                return Offset(if (rtl) size.width - x else x, cy - r * cos(th))
            }
            val shown = progress * drawIn.value
            val arcLength = r * 2f * half
            sweep(0f, 1f, track, 1.5.dp)
            val passedEnd = shown - GAP_TO_MARKER * d / arcLength
            if (passedEnd > 0f) sweep(0f, passedEnd, ink, 3.dp)
            for (i in 0..4) {
                val u = i / 4f
                val at = point(u)
                if (u <= shown) {
                    drawCircle(ink, radius = TickSize.toPx() / 2f, center = at)
                } else {
                    drawCircle(paper, radius = TickSize.toPx() / 2f, center = at)
                    drawCircle(track, radius = TickSize.toPx() / 2f, center = at, style = Stroke(1.5.dp.toPx()))
                }
            }
            val m = point(shown)
            val glow = 30.dp.toPx()
            drawCircle(
                Brush.radialGradient(
                    0f to ink.copy(alpha = 0.32f), 0.6f to ink.copy(alpha = 0.10f), 1f to Color.Transparent,
                    center = m, radius = glow,
                ),
                radius = glow,
                center = m,
            )
            drawCircle(paper, radius = DiscSize.toPx() / 2f, center = m)
            drawCircle(ink, radius = DiscSize.toPx() / 2f, center = m, style = Stroke(1.5.dp.toPx()))
            val g = GlyphSize.toPx()
            translate(m.x - g / 2f, m.y - g / 2f) {
                with(glyph) { draw(Size(g, g), colorFilter = ColorFilter.tint(ink)) }
            }
        }
    }
}
