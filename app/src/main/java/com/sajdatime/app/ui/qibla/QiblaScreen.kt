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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
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

        // Worked out once, here, and handed to both halves. The dial and the sentence are
        // two renderings of the same fact, and with hysteresis "aligned" now depends on
        // what it was a moment ago — so two independent copies could disagree, and the
        // screen would say you had arrived under a dial that said you had not.
        //
        // The buzz is decided in this same effect, deliberately. It used to live down in
        // GuidanceText, and when `aligned` moved up here that put one effect cycle between
        // the heading landing and the flag catching up. In that gap GuidanceText saw a real
        // heading with `aligned` still false, wrote down "not arrived yet", and then read
        // the flag turning true as an arrival — so the phone buzzed every single time the
        // screen was opened while already facing the Kaaba. Measured on the S23 Ultra:
        // 00:29:18.393, 0.4 s after the tab was tapped. Deciding both together removes the
        // gap that the bug lived in.
        val heading = state.compassHeading
        val haptics = LocalHapticFeedback.current
        var aligned by rememberSaveable { mutableStateOf(false) }
        // False until the first heading of the session has been read. Opening the screen
        // already facing the Kaaba is not an arrival, and must not be announced as one.
        var haveRead by rememberSaveable { mutableStateOf(false) }
        // And never twice in quick succession. Hysteresis stops the endless buzzing of a
        // phone at rest, but a compass that has just been switched on genuinely swings
        // while it settles, and those swings are wider than any sensible release angle:
        // with hysteresis alone the S23 Ultra still buzzed 4 times in the 26 seconds after
        // the screen opened, then fell silent for the rest of the run. One arrival is worth
        // announcing; four in half a minute is a fault the user feels in their hand.
        var lastBuzzAt by rememberSaveable { mutableLongStateOf(0L) }
        LaunchedEffect(heading, qibla) {
            if (heading == null) return@LaunchedEffect
            val now = QiblaEngine.staysAligned(aligned, heading, qibla)
            val since = SystemClock.elapsedRealtime() - lastBuzzAt
            if (now && !aligned && haveRead && since > ARRIVAL_QUIET_MILLIS) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                lastBuzzAt = SystemClock.elapsedRealtime()
            }
            aligned = now
            haveRead = true
        }

        Spacer(Modifier.height(24.dp))
        CompassDial(state = state, qiblaTrueBearing = qibla, aligned = aligned)
        Spacer(Modifier.height(24.dp))
        GuidanceText(state = state, qiblaTrueBearing = qibla, aligned = aligned)
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
private fun CompassDial(state: UiState, qiblaTrueBearing: Double, aligned: Boolean) {
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
    val measurer = rememberTextMeasurer()

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

            // Aligned fills the dial and switches its edge to the accent, and the edge
            // thickens from 2 to 6. That, the vanishing arc and the changed wording are
            // independent signals of the same fact, none of which is colour on its own —
            // a user who cannot separate green from grey still sees a ring get heavier.
            val face = if (aligned) scheme.primaryContainer else scheme.surfaceVariant
            drawCircle(color = face, radius = radius, center = centre)
            drawCircle(
                color = if (aligned) scheme.primary else scheme.outlineVariant,
                radius = radius,
                center = centre,
                style = Stroke(width = if (aligned) 6f else 2f),
            )

            if (heading != null && !aligned) {
                drawTurnArc(centre, radius, turn.toFloat(), scheme.primary)
            }

            drawTicks(centre, radius, dialRotation, scheme.outline)

            // The four letters, upright at every heading rather than rotating with the
            // ring. A compass whose "W" is upside down when you face south is a compass
            // people have to tilt their head at, and the whole point of the screen is to
            // be read while turning. North is the app's green and heavier than the other
            // three: it is the one bearing that anchors the rest.
            drawCardinals(centre, radius, dialRotation, measurer, scheme.onSurfaceVariant, scheme.primary)

            // The needle: fixed at the top with a compass ("you"), at the bearing without
            // one. Screen space, not dial space, which is the whole point.
            rotate(degrees = needleRotation, pivot = centre) {
                drawNeedle(centre, radius, scheme.primary)
            }

            // Last, so it covers whatever it lands on: a cardinal letter when the Qibla is
            // near due north, and the needle's tip when you are facing the Qibla — the
            // moment the two marks are meant to meet.
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

/**
 * The tick ring, kept out at the rim so the cardinal letters have a band of their own.
 *
 * Takes the rotation itself rather than sitting inside a `rotate` block, because
 * [drawCardinals] cannot use one — the letters have to stay upright — and having the two
 * halves of the same ring computed the same way is what stops them drifting apart.
 */
private fun DrawScope.drawTicks(centre: Offset, radius: Float, rotation: Float, colour: Color) {
    // A tick every 15 degrees, longer and heavier every 90 so the quarters read at a
    // glance. They span 0.91..0.99 of the radius: outside the letters, inside the rim.
    for (degrees in 0 until 360 step 15) {
        val major = degrees % 90 == 0
        val length = if (major) radius * 0.09f else radius * 0.05f
        val radians = Math.toRadians(degrees + rotation.toDouble() - 90.0)
        val outer = Offset(
            centre.x + radius * 0.99f * cos(radians).toFloat(),
            centre.y + radius * 0.99f * sin(radians).toFloat(),
        )
        val inner = Offset(
            centre.x + (radius * 0.99f - length) * cos(radians).toFloat(),
            centre.y + (radius * 0.99f - length) * sin(radians).toFloat(),
        )
        drawLine(
            color = colour,
            start = inner,
            end = outer,
            strokeWidth = if (major) 4f else 2f,
        )
    }
}

/**
 * N, E, S and W, placed on the rotating ring but drawn the right way up.
 *
 * This replaced a small filled wedge that marked north and nothing else. The wedge was
 * honest and useless: it told you where north was only if you already knew it was the
 * north marker, and it said nothing at all about the other three quarters. Letters are
 * what every compass anyone has held already uses.
 *
 * **Positions rotate, glyphs do not.** Each letter's position is computed at its own
 * bearing minus the heading, so it sits over the right part of the ring, but it is drawn
 * upright rather than inside a `rotate` block. Rotating the glyphs too is what a physical
 * bezel does, and on a screen it means reading "M" for W while facing south.
 *
 * Sits at 0.83 of the radius, between the Kaaba mark (out to 0.81) and the ticks (in to
 * 0.91). Those three numbers are one budget: move any and check the others, and see
 * [KAABA_DISTANCE] for the fourth term, the needle tip.
 *
 * ponytail: `Char` per point rather than a localised string array. These four glyphs are
 * the standard compass abbreviations, not prose, and the day a translated build ships they
 * are what `values-<lang>/` would carry — at which point this takes a `List<String>`.
 */
private fun DrawScope.drawCardinals(
    centre: Offset,
    radius: Float,
    rotation: Float,
    measurer: TextMeasurer,
    colour: Color,
    northColour: Color,
) {
    val points = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
    for ((label, bearing) in points) {
        val north = bearing == 0
        val radians = Math.toRadians(bearing + rotation.toDouble() - 90.0)
        val laid = measurer.measure(
            text = label,
            style = TextStyle(
                color = if (north) northColour else colour,
                fontSize = (radius * 0.115f).toSp(),
                fontWeight = if (north) FontWeight.Bold else FontWeight.Medium,
            ),
        )
        // Centre the glyph on the point rather than hanging it off the top-left corner,
        // which is where drawText would otherwise put it.
        drawText(
            textLayoutResult = laid,
            topLeft = Offset(
                centre.x + radius * 0.83f * cos(radians).toFloat() - laid.size.width / 2f,
                centre.y + radius * 0.83f * sin(radians).toFloat() - laid.size.height / 2f,
            ),
        )
    }
}

/**
 * The needle: a long tapered arrow from the centre outward. With a compass it is fixed at
 * the top and means "the way you are pointing"; without one it is turned to the Qibla
 * bearing. Its tip at 0.74 sits inside the Kaaba mark's box when the two coincide, see
 * [KAABA_DISTANCE], so on alignment the arrow visibly ends at the Kaaba.
 */
private fun DrawScope.drawNeedle(centre: Offset, radius: Float, colour: Color) {
    val tip = Offset(centre.x, centre.y - radius * 0.74f)
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
 * so the corner reaching furthest out is a different corner at every bearing: at 0.66 and
 * 0.30 the worst of them lands at 0.822 of the radius.
 *
 * **Pulled in from 0.72/0.32 on 15 Aug 2026 to make room for the cardinal letters.** N, E,
 * S and W now sit at 0.83 and the ticks were moved out to 0.91..0.99, so the dial reads
 * outside-in as rim, ticks, letters, then the Kaaba. Before that the mark ran out to 0.892
 * and the ring began at 0.915, which left no band for a letter at all.
 *
 * The needle tip, now 0.74, is *inside* the cube when the two coincide, which is what makes
 * the arrow end at the Kaaba on alignment rather than beside it or through it. So it is
 * four numbers, not two — mark distance, mark size, needle tip, letter ring — and they move
 * together: change any and check the others. The watch draws the same mark at the same
 * shares but keeps its own needle length, because it has a readout in the middle that the
 * phone does not; see `WearApp.kt`.
 */
/**
 * The shortest gap between two arrival buzzes. Long enough to swallow a compass settling
 * after the screen opens, short enough that a user who really does turn away and back is
 * told again. Shared with the watch by copy rather than by a common constant, because the
 * modules share only `core` and this is a feel value, not a rule.
 */
private const val ARRIVAL_QUIET_MILLIS = 5_000L

private const val KAABA_DISTANCE = 0.66f
private const val KAABA_SIZE = 0.30f

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
 *
 * **Arriving is now an event, not a change of caption.** Until 15 Aug 2026 facing the
 * Kaaba swapped one line of the same size and weight for another, which is the least a
 * screen can do to mark the thing the user opened it for. It now also turns green and
 * buzzes — though the buzz itself is fired by [QiblaScreen], where `aligned` is decided,
 * because the two have to be worked out in the same breath. The comment there says why.
 *
 * **An edge alone is not enough on real hardware**, which the emulator could never have
 * shown, because injected sensor values do not wobble. Measured on the owner's S23 Ultra
 * on 16 Aug 2026, lying untouched on a desk: 26 buzzes in 76 seconds, and a further 6 in
 * the 14 seconds from 00:17:52, during which *every* sample of this very caption read "You
 * are now facing the Kaaba". The dropouts were shorter than the screen could be sampled,
 * so the sentence looked settled while the phone went off in the user's hand every three
 * seconds. [QiblaEngine.staysAligned] is what fixed it, and the same four-minute test
 * afterwards recorded no buzz at all.
 */
@Composable
private fun GuidanceText(state: UiState, qiblaTrueBearing: Double, aligned: Boolean) {
    val heading = state.compassHeading

    val message = when {
        heading == null -> stringResource(R.string.qibla_no_compass, qiblaTrueBearing.roundToInt())
        aligned -> stringResource(R.string.qibla_facing)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (aligned) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = message,
                // Bigger and green on arrival. Weight and size carry the moment for anyone
                // who cannot see the colour change.
                style = if (aligned) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = if (aligned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = message
                },
            )
        }
        // The raw heading is hidden once you have arrived. It is a debugging number that
        // helps while you are still turning and competes with the answer once you are not.
        if (heading != null && !aligned) {
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
