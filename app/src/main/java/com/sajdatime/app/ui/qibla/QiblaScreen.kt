package com.sajdatime.app.ui.qibla

import com.sajdatime.app.ui.theme.Space
import androidx.core.app.LocaleManagerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.sajdatime.app.ui.theme.LocalDarkTheme
import com.sajdatime.app.ui.theme.dialBezel
import com.sajdatime.app.data.placeName
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.core.MeasurementSystem
import com.sajdatime.core.bidiIsolated
import com.sajdatime.app.ui.components.BottomBarClearance
import com.sajdatime.app.ui.components.ScreenTitle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import com.sajdatime.app.ui.theme.kiswahGold
import com.sajdatime.app.ui.theme.sajdaSurface
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// TEMPORARY step 6 Q1, Q2, Q4: comparison switches for Ali's pictures. All default to the recommendations; delete
// all of them, and the branches that read them, when he has chosen (STEP-6-DESIGN sections 13 and 16).
// TEMPORARY step 6 Q1: true centres the direction block over the dial instead of starting it under the title.
internal var qiblaCentreInstruction = false
// TEMPORARY step 6 Q4: true draws the turn still owed as the bezel band in primary, from the needle round to the Kaaba.
internal var qiblaShowArc = false
// TEMPORARY step 6 Q2: true makes the light bezel surfaceContainerHighest (sand) instead of surface (white).
internal var qiblaSandBezel = false
// TEMPORARY step 6 dark picture: true makes the dark bezel surfaceBright instead of surfaceContainerHighest.
internal var qiblaDarkBezelBright = false

@Composable
fun QiblaScreen(
    state: UiState,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    // Same large start aligned title as the other two tabs (Quiet Glass), then the direction
    // block and the dial under it, then room to scroll clear of the floating tab bar.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle(stringResource(R.string.qibla_title))
        QiblaBody(state, onUseGps, onSearchCity)
        Spacer(Modifier.height(Space.Section))
        BottomBarClearance()
    }
}

@Composable
internal fun QiblaBody(
    state: UiState,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    val context = LocalContext.current
    var locationSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val qibla = state.qiblaBearing
        // The Makkah fallback counts as no location here: the bearing from Makkah to Makkah is
        // 0 degrees and the distance 0 km, so the screen would say "You are now facing the Kaaba"
        // whenever the phone pointed north (STEP-6-DESIGN section 14).
        if (qibla == null || state.settings.usingDefaultLocation) {
            Spacer(Modifier.height(Space.Section))
            Notice(
                brief = stringResource(R.string.qibla_needs_location),
                actionLabel = stringResource(R.string.action_set_location),
                onAction = { locationSheet = true },
            )
        } else {
            QiblaLocated(state, qibla, context)
        }

        if (locationSheet) {
            // Closes as soon as a location lands, as on My prayers and Times.
            val opened = remember { state.settings.coordinates to state.settings.cityName }
            val current = state.settings.coordinates to state.settings.cityName
            LaunchedEffect(current) { if (current != opened) locationSheet = false }
            LocationSheet(
                state = state,
                onDismiss = { locationSheet = false },
                onUseGps = onUseGps,
                onSearchCity = onSearchCity,
            )
        }
    }
}

@Composable
private fun QiblaLocated(state: UiState, qibla: Double, context: android.content.Context) {
    // Worked out once, here, and handed to both halves. The dial and the sentence are
    // two renderings of the same fact, and with hysteresis "aligned" now depends on
    // what it was a moment ago — so two independent copies could disagree, and the
    // screen would say you had arrived under a dial that said you had not.
    //
    // The buzz is decided in this same effect, deliberately. It used to live down in
    // GuidanceText (now DirectionBlock), and when `aligned` moved up here that put one effect cycle between
    // the heading landing and the flag catching up. In that gap GuidanceText saw a real
    // heading with `aligned` still false, wrote down "not arrived yet", and then read
    // the flag turning true as an arrival — so the phone buzzed every single time the
    // screen was opened while already facing the Kaaba. Measured on the S23 Ultra:
    // 00:29:18.393, 0.4 s after the tab was tapped. Deciding both together removes the
    // gap that the bug lived in.
    val heading = state.compassHeading
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
            arrivalBuzz(context)
            lastBuzzAt = SystemClock.elapsedRealtime()
        }
        aligned = now
        haveRead = true
    }

    Spacer(Modifier.height(Space.Paragraph))
    DirectionBlock(state = state, qiblaTrueBearing = qibla, aligned = aligned)
    // Permanent, so it can sit above the dial beside the number it explains without anything
    // ever moving. Calibration and "may wander" come and go with the phone in the hand, so
    // they go below Details instead (AccuracyNotice).
    if (state.compassAccuracy == CompassAccuracy.UNAVAILABLE) {
        Spacer(Modifier.height(Space.Group))
        Notice(brief = stringResource(R.string.qibla_sensor_missing))
    }

    Spacer(Modifier.height(Space.Section))
    CompassDial(state = state, qiblaTrueBearing = qibla, aligned = aligned)
    Spacer(Modifier.height(Space.Section))
    if (heading != null) DialLegend()
    val place = state.settings.placeName(context)
    if (place.isNotBlank()) {
        if (heading != null) Spacer(Modifier.height(Space.Paragraph))
        PlaceRow(place)
    }
    Spacer(Modifier.height(Space.Group))
    DetailsBlock(state = state, hasCompass = heading != null)
    if (state.compassAccuracy == CompassAccuracy.LOW || state.compassAccuracy == CompassAccuracy.MEDIUM) {
        Spacer(Modifier.height(Space.Group))
        AccuracyNotice(state.compassAccuracy)
    }
}

@Composable
private fun PlaceRow(place: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            // Sized from the text, so it grows with it (21dp at 1.0, 42dp at 2.0); the glyph fills about 0.8 of its box.
            modifier = Modifier.size(textLineHeight()).testTag("qibla_place_pin"),
        )
        Spacer(Modifier.width(Space.Tight))
        Text(
            text = place.bidiIsolated(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The body line height in dp, which scales with the text size: what the pin and the key marks are sized from. */
@Composable
private fun textLineHeight(): Dp = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.lineHeight.toDp() }

/**
 * Distance and bearing, behind a labelled compact button (Ali's decision 23 Oct 2026, Q12): the numbers
 * are the least useful line for the most people, so they cost nothing and are one tap away. The chevron
 * and the open label are the pattern Notice's own Details button already uses.
 *
 * Miles or km by the region of the phone's own language setting (en-GB, ur-GB: miles), never the
 * language picked inside the app. Not `Resources.getSystem()`: on Android 13+ the app's own language
 * ("en", no region) reaches that configuration too, so it said km on an en-US phone (emulator, 24 Oct).
 * `LocaleManagerCompat.getSystemLocales` asks LocaleManager on 13+ and reads the system list below.
 */
@Composable
private fun DetailsBlock(state: UiState, hasCompass: Boolean) {
    var open by rememberSaveable { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, Motion.quick(), label = "qibla details fold")
    val stateText = stringResource(if (open) R.string.state_expanded else R.string.state_collapsed)
    val km = state.qiblaDistanceKm ?: 0.0
    val miles = MeasurementSystem.usesMiles(LocaleManagerCompat.getSystemLocales(LocalContext.current)[0]?.country.orEmpty())

    // ponytail: a compact button standing alone, centred, is a disclosure under a picture (4.1 width row).
    CtaButton(
        onClick = { open = !open },
        compact = true,
        modifier = Modifier.semantics { stateDescription = stateText },
    ) {
        Text(stringResource(if (open) R.string.notice_hide_details else R.string.notice_show_details))
        Spacer(Modifier.width(Space.Tight))
        Icon(
            Icons.Rounded.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(20.dp).rotate(turn),
        )
    }
    AnimatedVisibility(open) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(Space.Related))
            val style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
            val ink = MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = if (miles) {
                    stringResource(R.string.qibla_distance_miles, MeasurementSystem.milesFromKm(km).roundToInt())
                } else {
                    stringResource(R.string.qibla_distance_km, km.roundToInt())
                },
                style = style,
                color = ink,
                textAlign = TextAlign.Center,
            )
            // No compass: the bearing is already the headline.
            if (hasCompass && state.qiblaBearing != null) {
                Spacer(Modifier.height(Space.Tight))
                Text(
                    text = stringResource(R.string.qibla_bearing, state.qiblaBearing.roundToInt()),
                    style = style,
                    color = ink,
                    textAlign = TextAlign.Center,
                )
            }
        }
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

    // The turn still owed, from the needle at the top round to the Kaaba, signed so a left turn goes
    // anticlockwise. Only the Q4 picture draws it (qiblaShowArc); DirectionBlock says the same number aloud.
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

    // Face, bezel colour and bezel width fade together: 250 ms on arrival, 150 ms on leaving (a reversal
    // is an exit, DESIGN_SYSTEM section 2). The bezel colour is read here, in composition, because
    // sajdaSurface takes a colour, so the dial Box recomposes for the length of an arrival and at no other
    // time; the face and the width are read inside the draw lambda, so a frame recomposes nothing.
    val dark = LocalDarkTheme.current
    val restingBezel = when {
        dark && qiblaDarkBezelBright -> scheme.surfaceBright
        !dark && qiblaSandBezel -> scheme.surfaceContainerHighest
        else -> dialBezel()
    }
    // Motion's specs are generic, so each state takes its own (colour twice, float once).
    val face by animateColorAsState(
        if (aligned) scheme.primaryContainer else scheme.surfaceVariant,
        if (aligned) Motion.standard() else Motion.quick(),
        label = "dialFace",
    )
    val bezel by animateColorAsState(
        if (aligned) scheme.primary else restingBezel,
        if (aligned) Motion.standard() else Motion.quick(),
        label = "dialBezel",
    )
    // Thickness as well as colour, so a user who cannot tell green from grey still sees the bezel get heavier.
    val bezelWidth by animateFloatAsState(
        if (aligned) 0.11f else 0.08f,
        if (aligned) Motion.standard() else Motion.quick(),
        label = "dialBezelWidth",
    )

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
            // The bezel is the lifted disc (shadow in light, hairline in dark, the lift every card has);
            // the Canvas draws the face over it, inside the bezel band.
            .sajdaSurface(CircleShape, bezel)
            // The dial is decorative; the spoken direction lives in DirectionBlock.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val centre = Offset(size.width / 2f, size.height / 2f)

            // Aligned fills the face, turns the bezel to the accent and thickens it. That, the changed
            // wording and the buzz are independent signals of the same fact, three of them not colour.
            // The bezel's outer edge stays at 1.0 of the radius and the face is what gives way.
            drawCircle(color = face, radius = radius * (1f - bezelWidth), center = centre)

            if (qiblaShowArc && heading != null && !aligned) {
                drawTurnArc(centre, radius, bezelWidth, turn.toFloat(), scheme.primary)
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
 * TEMPORARY step 6 Q4: the turn still owed, drawn over the bezel band itself in the accent.
 *
 * `sweep` is signed: positive turns clockwise from the top, negative anticlockwise, so
 * the band always shows the shorter way round rather than a full lap. Compose measures
 * arcs from three o'clock, hence the -90 start.
 */
private fun DrawScope.drawTurnArc(centre: Offset, radius: Float, bezelWidth: Float, sweep: Float, colour: Color) {
    val thickness = radius * bezelWidth
    val inset = thickness / 2f
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
 * The tick ring: 60 ticks, one every 6 degrees, longer and heavier every 90 so the quarters read at a
 * glance. Minor 0.845 to 0.87, cardinal 0.80 to 0.87, widths as shares of the radius, round caps (so
 * the ends reach 0.877, clear of the aligned bezel's inner edge at 0.89).
 *
 * Takes the rotation itself rather than sitting inside a `rotate` block, because
 * [drawCardinals] cannot use one — the letters have to stay upright — and having the two
 * halves of the same ring computed the same way is what stops them drifting apart.
 */
private fun DrawScope.drawTicks(centre: Offset, radius: Float, rotation: Float, colour: Color) {
    for (degrees in 0 until 360 step 6) {
        val major = degrees % 90 == 0
        val inner = radius * (if (major) 0.80f else 0.845f)
        val outer = radius * 0.87f
        val radians = Math.toRadians(degrees + rotation.toDouble() - 90.0)
        val dx = cos(radians).toFloat()
        val dy = sin(radians).toFloat()
        drawLine(
            color = colour,
            start = Offset(centre.x + inner * dx, centre.y + inner * dy),
            end = Offset(centre.x + outer * dx, centre.y + outer * dy),
            strokeWidth = radius * (if (major) 0.014f else 0.007f),
            cap = StrokeCap.Round,
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
 * Centred at 0.74 of the radius, size 0.09r: a band of 0.695 to 0.785. The Kaaba mark overlaps it and is
 * drawn last, so it covers a letter when the Qibla is within about 15 degrees of a cardinal, which is
 * accepted: the Kaaba is the more useful mark there. One budget with the ticks, the Kaaba, the needle tip
 * and the bezel: see [KAABA_DISTANCE].
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
                fontSize = (radius * 0.09f).toSp(),
                fontWeight = if (north) FontWeight.Medium else FontWeight.Normal,
            ),
        )
        // Centre the glyph on the point rather than hanging it off the top-left corner,
        // which is where drawText would otherwise put it.
        drawText(
            textLayoutResult = laid,
            topLeft = Offset(
                centre.x + radius * 0.74f * cos(radians).toFloat() - laid.size.width / 2f,
                centre.y + radius * 0.74f * sin(radians).toFloat() - laid.size.height / 2f,
            ),
        )
    }
}

/**
 * The needle path, a teardrop: tip at (0, -0.64), a round end centred at (0, 0.10) with radius 0.12, bottom
 * at 0.22 (STEP-6-DESIGN 15.2 and 16.4), all in shares of `r`, dial centre at (`cx`, `cy`), y down. The
 * left flank lands tangent to the round end, the arc runs through the bottom, the right flank returns
 * to the tip. The key under the dial draws the same path.
 */
private fun needlePath(cx: Float, cy: Float, r: Float): Path = Path().apply {
    moveTo(cx, cy - 0.64f * r)
    cubicTo(cx - 0.05f * r, cy - 0.38f * r, cx - 0.12f * r, cy - 0.12f * r, cx - 0.12f * r, cy + 0.10f * r)
    arcTo(
        Rect(cx - 0.12f * r, cy - 0.02f * r, cx + 0.12f * r, cy + 0.22f * r),
        startAngleDegrees = 180f,
        sweepAngleDegrees = -180f,
        forceMoveTo = false,
    )
    cubicTo(cx + 0.12f * r, cy - 0.12f * r, cx + 0.05f * r, cy - 0.38f * r, cx, cy - 0.64f * r)
    close()
}

/**
 * The needle: a teardrop from the tip to a round end past the centre. With a compass it is fixed at
 * the top and means "the way you are pointing"; without one it is turned to the Qibla bearing. Its tip
 * (0.64, 0.6525 with the stroke) sits inside the Kaaba mark's box when the two coincide, see
 * [KAABA_DISTANCE], so on alignment the arrow visibly ends at the Kaaba. No hub: the round end is the pivot.
 */
private fun DrawScope.drawNeedle(centre: Offset, radius: Float, colour: Color) {
    val teardrop = needlePath(centre.x, centre.y, radius)
    drawPath(path = teardrop, color = colour)
    // Stroked as well as filled, with round joins, so the tip rounds off.
    drawPath(
        path = teardrop,
        color = colour,
        style = Stroke(width = radius * 0.025f, join = StrokeJoin.Round, cap = StrokeCap.Round),
    )
}

/**
 * How far out the Kaaba mark sits, and how big it is, both as a share of the dial radius.
 *
 * Shares rather than dp because the dial is not a fixed size: it shrinks in landscape and
 * at a large font scale (see [dialMaxWidth]), and a mark measured in dp would grow into
 * the tick ring as the dial got smaller.
 *
 * **One budget, five numbers, outside in (STEP-6-DESIGN 16.1): bezel inner edge 0.92 (0.89 aligned); ticks
 * 0.845 to 0.87 (cardinal from 0.80); letters 0.695 to 0.785; Kaaba ink 0.59 to 0.816; needle tip 0.6525.**
 * Move one and check the others. The mark's box is 0.59 to 0.81 at a cardinal bearing and its corner
 * reaches 0.856 at a diagonal one, crossing the minor ticks, but the drawn artwork (the plinth of
 * `ic_kaaba.xml`) reaches only 0.816 at worst, under the ticks and the aligned bezel. The mark overlaps
 * the letters' band and is drawn last.
 *
 * The needle tip is *inside* the box when the two coincide, which is what makes the arrow end at the
 * Kaaba on alignment rather than beside it or through it. The watch keeps its own dial and its own
 * shares (0.66 and 0.30 for the mark); see `WearApp.kt`.
 */
private const val KAABA_DISTANCE = 0.70f
private const val KAABA_SIZE = 0.22f

/**
 * The shortest gap between two arrival buzzes. Long enough to swallow a compass settling
 * after the screen opens, short enough that a user who really does turn away and back is
 * told again. Shared with the watch by copy rather than by a common constant, because the
 * modules share only `core` and this is a feel value, not a rule.
 */
private const val ARRIVAL_QUIET_MILLIS = 5_000L

/**
 * The arrival buzz, straight from the Vibrator. `performHapticFeedback(LongPress)` until step 6 was
 * silent on any phone whose touch feedback setting is off, which is the default on many Samsung and
 * Xiaomi phones, so the message appeared and nothing was felt (Ali, 24 Oct 2026: the vibration must
 * happen). A vibration with no attributes is still filed as touch and muted the same way (emulator,
 * 24 Oct: "ignored_for_settings" with touch feedback off), so it is marked media, which that switch
 * does not govern; the phone's own vibration settings still apply, as they should. 70 ms: one tap.
 */
private fun arrivalBuzz(context: android.content.Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(android.os.VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.os.Vibrator::class.java)
    }
    if (vibrator == null || !vibrator.hasVibrator()) return
    when {
        android.os.Build.VERSION.SDK_INT >= 33 -> vibrator.vibrate(
            android.os.VibrationEffect.createOneShot(70, android.os.VibrationEffect.DEFAULT_AMPLITUDE),
            android.os.VibrationAttributes.createForUsage(android.os.VibrationAttributes.USAGE_MEDIA),
        )
        android.os.Build.VERSION.SDK_INT >= 26 -> @Suppress("DEPRECATION") vibrator.vibrate(
            android.os.VibrationEffect.createOneShot(70, android.os.VibrationEffect.DEFAULT_AMPLITUDE),
            android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).build(),
        )
        else -> @Suppress("DEPRECATION") vibrator.vibrate(70)
    }
}

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
private fun DirectionBlock(state: UiState, qiblaTrueBearing: Double, aligned: Boolean) {
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

    // Start aligned under the title like the city and date on Times; TEMPORARY step 6 Q1: centred over the dial.
    val centred = qiblaCentreInstruction
    Column(
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = if (centred) Arrangement.Center else Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (aligned) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(Space.Related))
            }
            Text(
                text = message,
                // The same size aligned or not, so the line does not jump when the dial animates;
                // the green and the tick carry the moment. Tabular figures so the degrees change in place.
                style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                color = if (aligned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = if (centred) TextAlign.Center else TextAlign.Start,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = message
                    },
            )
        }
        // Kept on arrival, unlike before step 6: it now sits above the dial, and a line vanishing
        // there would make the dial jump at the very moment it animates. Only with a compass.
        if (heading != null) {
            Spacer(Modifier.height(Space.Tight))
            Text(
                text = stringResource(R.string.qibla_heading_now, heading.roundToInt()),
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (centred) TextAlign.Center else TextAlign.Start,
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

    // Marks sized from the text line, so they grow with it; the Kaaba and the teardrop fill 0.75 of the box.
    val box = textLineHeight()
    val markSize = box * 0.75f

    @Composable
    fun Key(label: String, mark: @Composable () -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(box), contentAlignment = Alignment.Center) { mark() }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }

    // A FlowRow: at 2.0 text on a 320dp phone "Where you are facing" is wider than the row and would
    // clip; it drops under "The Kaaba" instead.
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Space.Related),
        modifier = Modifier.clearAndSetSemantics { },
    ) {
        Key(stringResource(R.string.qibla_legend_kaaba)) {
            // Both layers, as on the dial: silhouette, then the hizam and door in gold on
            // top of it. The legend has to match the dial exactly — a key drawn in
            // different colours from the thing it explains is worse than no key.
            Box(Modifier.size(markSize)) {
                Icon(painter = kaaba, contentDescription = null, tint = scheme.onSurface, modifier = Modifier.size(markSize))
                Icon(painter = kaabaDetail, contentDescription = null, tint = kiswahGold(), modifier = Modifier.size(markSize))
            }
        }
        Key(stringResource(R.string.qibla_legend_facing)) {
            // The dial's own teardrop in miniature, from the same path, so the key matches what it explains.
            Canvas(Modifier.size(markSize)) {
                // The shape spans -0.64r to 0.22r, so r is the box over 0.86 and the centre sits 0.21r below the middle.
                val r = size.height / 0.86f
                drawPath(path = needlePath(size.width / 2f, size.height / 2f + 0.21f * r, r), color = scheme.primary)
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
        // UNAVAILABLE is permanent and has its own notice above the dial.
        CompassAccuracy.UNAVAILABLE -> return
        CompassAccuracy.MEDIUM -> R.string.qibla_accuracy_medium
        CompassAccuracy.HIGH -> return
    }

    // A warning Notice with no title (17 Oct 2026): the one sentence is the whole message.
    Notice(brief = stringResource(messageRes))
}
