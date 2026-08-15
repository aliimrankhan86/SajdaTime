package com.sajdatime.app.ui.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.R as CoreR
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.kiswahGold
import com.sajdatime.app.ui.theme.sajdaSurface
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(state: UiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.qibla_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(6.dp))

        val qibla = state.qiblaBearing
        if (qibla == null) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.qibla_needs_location),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        Text(
            text = stringResource(
                R.string.qibla_subtitle,
                qibla.roundToInt(),
                state.qiblaDistanceKm?.roundToInt() ?: 0,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        CompassDial(state = state, qiblaTrueBearing = qibla)
        Spacer(Modifier.height(24.dp))
        GuidanceText(state = state, qiblaTrueBearing = qibla)
        if (state.compassHeading != null) {
            Spacer(Modifier.height(14.dp))
            DialLegend()
        }
        Spacer(Modifier.height(16.dp))
        AccuracyNotice(state.compassAccuracy)
    }
}

/**
 * How wide the dial is allowed to be.
 *
 * 320dp is wider than a portrait phone, so portrait keeps the full-width dial it always
 * had. The height term is what matters in landscape: the dial is square, so its width is
 * also its height, and it has to leave room for the turn instruction underneath. Roughly
 * a third of the screen height does that on every landscape phone size, and it also keeps
 * the dial sane at a large font scale, where the text around it grows and the dial cannot.
 */
@Composable
private fun dialMaxWidth(): Dp =
    minOf(320.dp, (LocalConfiguration.current.screenHeightDp * 0.36f).dp)

@Composable
private fun CompassDial(state: UiState, qiblaTrueBearing: Double) {
    val heading = state.compassHeading
    val scheme = MaterialTheme.colorScheme

    // The dial rotates opposite to the device so north stays physically north, and the
    // Kaaba mark rides on it at the Qibla bearing — so the mark is always "where the Kaaba
    // is" in the room. When there is no compass the dial is held still with north at the
    // top, and the needle swings round to the bearing instead (see below).
    val dialRotation by animateFloatAsState(
        targetValue = if (heading != null) -heading.toFloat() else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "dial",
    )

    val aligned = heading != null && QiblaEngine.isAligned(heading, qiblaTrueBearing, 5.0)

    // The arc is the turn still owed, measured from the needle at the top round to the
    // Kaaba. Signed, so a left turn sweeps anticlockwise and the user is never told to walk
    // the long way round. It is the same number GuidanceText speaks aloud, drawn instead
    // of said.
    val turn = if (heading != null) QiblaEngine.relativeTurn(heading, qiblaTrueBearing) else 0.0

    // **The needle is you, not the Qibla.** It points the way the phone points — straight
    // up the screen — and stays there while the dial and the Kaaba turn round it. Turn
    // until the needle touches the Kaaba.
    //
    // It used to be the other way round: the needle swung to the Qibla bearing with the
    // Kaaba mark sitting on its tip, and "you" was a short grey tick at the rim. Read cold,
    // that dial showed a compass needle already pointing at the Kaaba, so the two marks
    // looked like one thing and the question "am I facing it?" had no visible answer — the
    // owner's report on 15 Aug 2026 was that the needle and the Kaaba were "attached". A
    // compass needle is read as "me" by everyone who has held a compass, so that is what it
    // is now. Without a compass there is no "me", and the needle does the only useful thing
    // left: it points at the Kaaba, which on a north-up dial is the bearing from north.
    val needleRotation = if (heading != null) 0f else qiblaTrueBearing.toFloat()

    // Loaded here rather than inside the Canvas: painterResource is a composable and the
    // draw lambda is not one. Same for the gold — kiswahGold reads a CompositionLocal.
    val kaaba = painterResource(CoreR.drawable.ic_kaaba)
    val kaabaDetail = painterResource(CoreR.drawable.ic_kaaba_detail)
    val gold = kiswahGold()

    Box(
        modifier = Modifier
            // widthIn before fillMaxWidth, and the order is the whole point: fillMaxWidth
            // pins minWidth to the full width, after which a widthIn cap can never win.
            //
            // Uncapped, fillMaxWidth + aspectRatio(1f) makes the dial as tall as the
            // screen is wide. In landscape that is taller than the display, so
            // "Turn right 118°" — the part that actually tells you where to stand — sat
            // off the bottom of a scroll nobody would think to perform.
            .widthIn(max = dialMaxWidth())
            .fillMaxWidth()
            .aspectRatio(1f)
            // The dial is decorative; the spoken direction lives in GuidanceText.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val centre = Offset(size.width / 2f, size.height / 2f)

            // Aligned fills the dial and switches its edge to the accent. That, the
            // vanishing arc and the changed wording are three independent signals of the
            // same fact, none of which is colour on its own.
            val face = if (aligned) scheme.primaryContainer else scheme.surfaceVariant
            drawCircle(color = face, radius = radius, center = centre)
            drawCircle(
                color = if (aligned) scheme.primary else scheme.outlineVariant,
                radius = radius,
                center = centre,
                style = Stroke(width = 2f),
            )

            if (heading != null && !aligned) {
                drawTurnArc(centre, radius, turn.toFloat(), scheme.primary)
            }

            rotate(degrees = dialRotation, pivot = centre) {
                drawTicks(centre, radius, scheme.outline)
                drawCardinalMarker(centre, radius, scheme.onSurface)
            }

            // The needle: fixed at the top with a compass ("you"), at the bearing without
            // one. Screen space, not dial space, which is the whole point.
            rotate(degrees = needleRotation, pivot = centre) {
                drawNeedle(centre, radius, scheme.primary)
            }

            // Last, so it covers whatever it lands on: the north wedge when the Qibla is
            // due north, and the needle's tip when you are facing the Qibla — the moment
            // the two marks are meant to meet.
            drawKaaba(
                centre = centre,
                radius = radius,
                bearingDegrees = dialRotation + qiblaTrueBearing.toFloat(),
                silhouette = kaaba,
                detail = kaabaDetail,
                tint = scheme.onSurface,
                gold = gold,
            )
        }
        // Nothing is drawn over the centre. An icon and a heading readout there sat
        // directly on top of the needle and made both harder to read.
    }
}

/**
 * The turn still owed, as a band just inside the rim.
 *
 * `sweep` is signed: positive turns clockwise from the top, negative anticlockwise, so
 * the band always shows the shorter way round rather than a full lap. Compose measures
 * arcs from three o'clock, hence the -90 start.
 */
private fun DrawScope.drawTurnArc(centre: Offset, radius: Float, sweep: Float, colour: Color) {
    val thickness = radius * 0.13f
    val inset = thickness / 2f + 3f
    drawArc(
        color = colour,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(centre.x - radius + inset, centre.y - radius + inset),
        size = Size((radius - inset) * 2f, (radius - inset) * 2f),
        style = Stroke(width = thickness),
    )
}

private fun DrawScope.drawTicks(centre: Offset, radius: Float, colour: Color) {
    // A tick every 15 degrees, longer every 90, so the dial reads at a glance.
    for (degrees in 0 until 360 step 15) {
        val major = degrees % 90 == 0
        val length = if (major) radius * 0.12f else radius * 0.06f
        val radians = Math.toRadians(degrees.toDouble() - 90.0)
        val outer = Offset(
            centre.x + (radius - 10f) * cos(radians).toFloat(),
            centre.y + (radius - 10f) * sin(radians).toFloat(),
        )
        val inner = Offset(
            centre.x + (radius - 10f - length) * cos(radians).toFloat(),
            centre.y + (radius - 10f - length) * sin(radians).toFloat(),
        )
        drawLine(
            color = colour,
            start = inner,
            end = outer,
            strokeWidth = if (major) 4f else 2f,
        )
    }
}

/** A small filled wedge marking north on the rotating dial. */
private fun DrawScope.drawCardinalMarker(centre: Offset, radius: Float, colour: Color) {
    val tip = Offset(centre.x, centre.y - radius * 0.74f)
    val left = Offset(centre.x - radius * 0.05f, centre.y - radius * 0.64f)
    val right = Offset(centre.x + radius * 0.05f, centre.y - radius * 0.64f)
    drawPath(
        path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(left.x, left.y)
            lineTo(right.x, right.y)
            close()
        },
        color = colour,
    )
}

/**
 * The needle: a long tapered arrow from the centre outward. With a compass it is fixed at
 * the top and means "the way you are pointing"; without one it is turned to the Qibla
 * bearing. Its tip at 0.82 sits inside the Kaaba mark's box when the two coincide — see
 * [KAABA_DISTANCE] — so on alignment the arrow visibly ends at the Kaaba.
 */
private fun DrawScope.drawNeedle(centre: Offset, radius: Float, colour: Color) {
    val tip = Offset(centre.x, centre.y - radius * 0.82f)
    val baseLeft = Offset(centre.x - radius * 0.075f, centre.y)
    val baseRight = Offset(centre.x + radius * 0.075f, centre.y)

    drawPath(
        path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(baseLeft.x, baseLeft.y)
            lineTo(baseRight.x, baseRight.y)
            close()
        },
        color = colour,
    )
    drawCircle(color = colour, radius = radius * 0.055f, center = centre)
}

/**
 * How far out the Kaaba mark sits, and how big it is, both as a share of the dial radius.
 *
 * Shares rather than dp because the dial is not a fixed size: it shrinks in landscape and
 * at a large font scale (see [dialMaxWidth]), and a mark measured in dp would grow into
 * the tick ring as the dial got smaller.
 *
 * The two numbers are one decision. The mark stays upright while its position goes round,
 * so the corner reaching furthest out is a different corner at every bearing: at 0.72 and
 * 0.32 the worst of them lands at 0.892 of the radius. The tick ring starts at 0.915 here
 * and at 0.897 on the 1.2" watch — so on the phone there is room, and on the small watch
 * the margin is under a pixel. That is not a problem, because the mark is drawn last and
 * would simply cover a tick, but it is the reason the mark was made *larger where it is*
 * rather than pushed further out: distance is the term with no headroom left.
 *
 * The needle tip at 0.82 is *inside* the cube when the two coincide, which is what makes
 * the arrow end at the Kaaba on alignment rather than beside it or through it. So it is
 * really three numbers, not two: change any and check the others. The watch draws the
 * same mark with the same shares — see `WearApp.kt`.
 */
private const val KAABA_DISTANCE = 0.72f
private const val KAABA_SIZE = 0.32f

/**
 * The Kaaba itself, on the ring at the Qibla bearing: "it is over there".
 *
 * A bearing in degrees answers "which way is it" only for someone who already thinks in
 * bearings, and the turn instruction underneath answers it only for someone who reads
 * English. A picture of the building answers it for everyone else, which is most of the
 * people this app was built for.
 *
 * It is drawn upright at every bearing rather than rotated with the dial, and that is the
 * one thing here worth defending. Rotating it with the dial is the obvious implementation
 * — it is what the needle does, one more `rotate` block and no trigonometry — but it puts
 * the Kaaba upside down whenever the Qibla is behind you, and an upside-down building
 * stops being recognised and goes back to being a shape. The whole value of the mark is
 * that it is understood before it is read, so it stays the right way up and its position
 * is computed instead.
 */
private fun DrawScope.drawKaaba(
    centre: Offset,
    radius: Float,
    bearingDegrees: Float,
    silhouette: Painter,
    detail: Painter,
    tint: Color,
    gold: Color,
) {
    // -90 because the dial's zero is straight up and trigonometry's is three o'clock.
    val radians = Math.toRadians(bearingDegrees.toDouble() - 90.0)
    val box = radius * KAABA_SIZE
    translate(
        left = centre.x + radius * KAABA_DISTANCE * cos(radians).toFloat() - box / 2f,
        top = centre.y + radius * KAABA_DISTANCE * sin(radians).toFloat() - box / 2f,
    ) {
        // Solid first, detail over it, both opaque. The band and door were once holes in
        // a single path, which was neater and wrong: a hole shows what is behind the mark,
        // and what is behind the mark is the needle, so the doorway filled up with green.
        //
        // The detail is gold and no longer the dial's face colour. Painted in the face it
        // was not a band but a slot cut through the cube, and the mark read as a shopfront
        // — see ic_kaaba_detail.xml. Because both shapes sit wholly inside the silhouette,
        // gold only ever has to hold against `tint`, never against the dial.
        with(silhouette) { draw(size = Size(box, box), colorFilter = ColorFilter.tint(tint)) }
        with(detail) { draw(size = Size(box, box), colorFilter = ColorFilter.tint(gold)) }
    }
}

/**
 * The spoken and written instruction. This, not the dial, is what a screen reader user
 * relies on, so it is a polite live region rather than a per-frame announcement.
 */
@Composable
private fun GuidanceText(state: UiState, qiblaTrueBearing: Double) {
    val heading = state.compassHeading

    val message = when {
        heading == null -> stringResource(R.string.qibla_no_compass, qiblaTrueBearing.roundToInt())
        QiblaEngine.isAligned(heading, qiblaTrueBearing, 5.0) -> stringResource(R.string.qibla_facing)
        else -> {
            val turn = QiblaEngine.relativeTurn(heading, qiblaTrueBearing)
            if (turn > 0) {
                stringResource(R.string.qibla_turn_right, turn.roundToInt())
            } else {
                stringResource(R.string.qibla_turn_left, (-turn).roundToInt())
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = message
                },
        )
        if (heading != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.qibla_heading_now, heading.roundToInt()),
                style = PrayerTimeTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The two marks on the dial, drawn small and named. Without it the arrow and the cube are
 * just shapes, and "which one am I?" is the first thing anyone asks. The keys are the marks
 * themselves in miniature — a needle and a Kaaba — rather than colour swatches, so the
 * legend still works for someone who cannot tell green from grey. Hidden when there is no
 * compass, because then there is no "you" on the dial to explain.
 */
@Composable
private fun DialLegend() {
    val scheme = MaterialTheme.colorScheme
    val kaaba = painterResource(CoreR.drawable.ic_kaaba)
    val kaabaDetail = painterResource(CoreR.drawable.ic_kaaba_detail)

    @Composable
    fun Key(label: String, mark: @Composable () -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) { mark() }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { },
    ) {
        Key(stringResource(R.string.qibla_legend_kaaba)) {
            // Both layers, as on the dial: silhouette, then the hizam and door in gold on
            // top of it. The legend has to match the dial exactly — a key drawn in
            // different colours from the thing it explains is worse than no key.
            Box(Modifier.size(16.dp)) {
                Icon(painter = kaaba, contentDescription = null, tint = scheme.onSurface, modifier = Modifier.size(16.dp))
                Icon(painter = kaabaDetail, contentDescription = null, tint = kiswahGold(), modifier = Modifier.size(16.dp))
            }
        }
        Key(stringResource(R.string.qibla_legend_facing)) {
            // A stubbier arrow than the dial's: the real needle's proportions are a hair's
            // width at 18dp.
            Canvas(Modifier.size(width = 12.dp, height = 18.dp)) {
                drawPath(
                    path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height)
                        close()
                    },
                    color = scheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AccuracyNotice(accuracy: CompassAccuracy) {
    // Only surfaced when it changes what the user should do. A "high accuracy" badge on
    // every screen is noise; "calibrate before you trust this" is not.
    val messageRes = when (accuracy) {
        CompassAccuracy.LOW -> R.string.qibla_calibrate
        CompassAccuracy.UNAVAILABLE -> R.string.qibla_sensor_missing
        CompassAccuracy.MEDIUM -> R.string.qibla_accuracy_medium
        CompassAccuracy.HIGH -> return
    }

    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(16.dp), scheme.tertiaryContainer)
            .border(1.dp, scheme.tertiary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = scheme.tertiary,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onTertiaryContainer,
        )
    }
}
