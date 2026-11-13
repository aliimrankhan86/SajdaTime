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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.sajdatime.app.ui.theme.dialBezel
import com.sajdatime.app.data.placeName
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.buzz
import com.sajdatime.app.ui.components.shrinkToFit
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.components.SetLocationPrompt
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
import com.sajdatime.core.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.theme.kiswahGold
import com.sajdatime.app.ui.theme.sajdaSurface
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    state: UiState,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    // Reserve the measured bar below the scroll area. On short screens, a button
    // partially in view must stay clear of the floating tabs before it is scrolled.
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ScreenTitle(stringResource(R.string.qibla_title))
            QiblaBody(state, onUseGps, onSearchCity)
            Spacer(Modifier.height(Space.Section))
        }
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
            SetLocationPrompt(stringResource(R.string.qibla_needs_location), onClick = { locationSheet = true })
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
    val accuracy = state.compassAccuracy
    val heading = state.compassHeading.takeIf { accuracy.allowsHeading }
    val arrivalHeading = heading.takeIf { accuracy.allowsArrival }
    var wasAligned by remember { mutableStateOf(false) }
    var buzzArmed by remember { mutableStateOf(false) }
    var lastBuzzAt by remember { mutableLongStateOf(0L) }
    // Gate the dial and text on the current reading, including the frame before the effect runs.
    val aligned = arrivalHeading != null && QiblaEngine.staysAligned(wasAligned, arrivalHeading, qibla)
    LaunchedEffect(arrivalHeading, qibla) {
        wasAligned = aligned
        if (arrivalHeading == null) {
            buzzArmed = false
        } else if (aligned) {
            if (buzzArmed && SystemClock.elapsedRealtime() - lastBuzzAt > ARRIVAL_QUIET_MILLIS) {
                arrivalBuzz(context)
                lastBuzzAt = SystemClock.elapsedRealtime()
            }
            buzzArmed = false
        } else if (QiblaEngine.hasLeftArrivalZone(arrivalHeading, qibla)) {
            // A stationary compass can wander past the 4° visual release. Rearm only
            // after a larger turn, so that wobble and calibration recovery stay quiet.
            buzzArmed = true
        }
    }

    Spacer(Modifier.height(Space.Paragraph))
    if (accuracy.needsCalibration) {
        Notice(brief = stringResource(R.string.qibla_calibrate))
    } else {
        val displayState = state.copy(compassHeading = heading)
        DirectionBlock(state = displayState, qiblaTrueBearing = qibla, aligned = aligned)
        if (accuracy == CompassAccuracy.UNAVAILABLE) {
            Spacer(Modifier.height(Space.Group))
            Notice(brief = stringResource(R.string.qibla_sensor_missing))
        }
        if (accuracy == CompassAccuracy.MEDIUM) {
            Spacer(Modifier.height(Space.Group))
            Text(
                text = stringResource(R.string.qibla_accuracy_medium),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Space.Section))
        CompassDial(state = displayState, qiblaTrueBearing = qibla, aligned = aligned)
        Spacer(Modifier.height(Space.Section))
        if (heading != null) DialLegend()
    }
    val place = state.settings.placeName(context)
    if (place.isNotBlank()) {
        if (heading != null || accuracy.needsCalibration) Spacer(Modifier.height(Space.Paragraph))
        PlaceRow(place)
    }
    Spacer(Modifier.height(Space.Group))
    DetailsBlock(state = state, hasCompass = heading != null)

}

@Composable
private fun PlaceRow(place: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            // Sized from the text, so it grows with it (21dp at 1.0, about 34dp at 2.0: Compose scales large text non-linearly); the glyph fills about 0.8 of its box.
            modifier = Modifier.size(textLineHeight()).testTag("qibla_place_pin"),
        )
        Spacer(Modifier.width(Space.Tight))
        Text(
            text = place.bidiIsolated(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = shrinkToFit(MaterialTheme.typography.bodyMedium.fontSize),
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
                    text = stringResource(R.string.qibla_bearing, QiblaEngine.wholeDegrees(state.qiblaBearing)),
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
    //
    // The target is carried on from the last one by the shortest way round ([continuousTurn]), never
    // set to minus the heading outright: a heading stepping from 359.9 to 0.1 would otherwise send the
    // ring, the letters, the hand and the Kaaba round a whole turn in 120 ms. Anyone whose Qibla is near
    // north (the Kenyan coast) would see that every time they came to rest on it. There since the first
    // dial; found by review on 12 Nov 2026.
    val turned = remember { FloatArray(1) { -(heading ?: 0.0).toFloat() } }
    turned[0] = continuousTurn(turned[0], -(heading ?: 0.0).toFloat())
    val dialRotation by animateFloatAsState(
        targetValue = turned[0],
        animationSpec = tween(durationMillis = 120),
        label = "dial",
    )

    // **The hand points at the Kaaba.** Its angle on the screen is the Qibla bearing minus the heading,
    // the same number the sentence above the dial prints ("Turn right 39°"), and the Kaaba mark rides on
    // its point, so hand and mark can never say two different things. "You" is the small fixed mark at
    // the top of the dial ([drawTopMark]): turn until the hand stands straight up into it.
    //
    // This is the third meaning the hand has had, and the history matters:
    //  1. Until 15 Aug 2026 it pointed at the Kaaba with the mark on its tip, and "you" was a short grey
    //     tick at the rim. The owner said hand and mark looked "attached": with nothing strong marking
    //     "you", the dial looked as if it were already pointing at the Kaaba whichever way he stood.
    //  2. From then until 12 Nov 2026 the hand was "you": fixed straight up, with the Kaaba mark turning
    //     round the ring. Correct, and read wrongly: on 12 Nov, on his Redmi, the screen said "Facing 80",
    //     "Turn right 39" and the mark sat 39 degrees to the right, all true, and he followed the big
    //     green hand, which pointed straight ahead, the wrong way. Every Qibla app teaches people that the
    //     arrow is the Qibla, and many of our users cannot read the sentence. This was the cause of his
    //     "seems a bit off" reports since 1.6.0. Ali: "we can't afford it telling the wrong direction".
    //  3. Now: the hand is the Kaaba again, and what was missing in 1 is supplied: a fixed top mark,
    //     named in the key, and an arrival that changes the whole dial, the sentence and buzzes.
    // Do not make the hand "you" again.
    //
    // Without a compass the ring is held with north at the top, the heading counts as zero, and the same
    // sum puts the hand at the bearing from north.
    val needleRotation = handRotation(dialRotation, qiblaTrueBearing)

    // Loaded here rather than inside the Canvas: painterResource is a composable and the
    // draw lambda is not one. Same for the gold — kiswahGold reads a CompositionLocal.
    val kaaba = painterResource(CoreR.drawable.ic_kaaba)
    val kaabaDetail = painterResource(CoreR.drawable.ic_kaaba_detail)
    val gold = kiswahGold()
    val measurer = rememberTextMeasurer()

    // Face, bezel colour and bezel width fade together: 250 ms on arrival, 150 ms on leaving (a reversal
    // is an exit, DESIGN_SYSTEM section 2). The bezel colour is read here, in composition, because
    // sajdaSurface takes a colour; the dial already recomposes on every compass sample (it reads
    // compassHeading here), so this adds nothing. The face and the width are read inside the draw lambda.
    val restingBezel = dialBezel()
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
            // The dial is decorative; the spoken direction lives in DirectionBlock. The tag is the only
            // thing left on it: QiblaDialDeviceTest finds the dial by it to measure where the marks land.
            .clearAndSetSemantics { testTag = "qibla_dial" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val centre = Offset(size.width / 2f, size.height / 2f)

            // Aligned fills the face, turns the bezel to the accent and thickens it. That, the changed
            // wording and the buzz are independent signals of the same fact, three of them not colour.
            // The bezel's outer edge stays at 1.0 of the radius and the face is what gives way.
            drawCircle(color = face, radius = radius * (1f - bezelWidth), center = centre)

            drawTicks(centre, radius, dialRotation, scheme.outline)

            // The four letters, upright at every heading rather than rotating with the
            // ring. A compass whose "W" is upside down when you face south is a compass
            // people have to tilt their head at, and the whole point of the screen is to
            // be read while turning. North is the app's green and heavier than the other
            // three: it is the one bearing that anchors the rest.
            drawCardinals(centre, radius, dialRotation, measurer, scheme.onSurfaceVariant, scheme.onSurface)

            // "The top of your phone": fixed, quiet, and only with a compass (without one the top is north
            // and there is no "you" to mark). On the bezel, so it takes the bezel's opposite on arrival.
            if (heading != null) drawTopMark(centre, radius, if (aligned) scheme.onPrimary else scheme.onSurfaceVariant)

            // The hand, at the Qibla. It shares the ring's animated turn, so hand, mark and letters move
            // as one piece.
            rotate(degrees = needleRotation, pivot = centre) {
                drawNeedle(centre, radius, scheme.primary, hole = face)
            }

            // Last, so it covers whatever it lands on: a cardinal letter when the Qibla is
            // near due north, and always the hand's point, which ends inside it.
            drawKaaba(
                centre = centre,
                radius = radius,
                bearingDegrees = needleRotation,
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
 * The needle's shape, in shares of the dial radius, dial centre at the origin. Internal so
 * `QiblaDialGeometryTest` can hold the numbers to the Kaaba mark's.
 *
 * **A watch hand, since 12 Nov 2026: a slim blade from a ring at the centre to a fine point, with a short
 * tail behind the ring.** Two reports from Ali in one night made it:
 * 1. On the 1.6.0 he had just received (11 Nov): the needle had been made thicker, it "seems a bit off",
 *    "not as accurate as before". The direction maths had not changed since 1.5.0 (`CompassRepository` and
 *    `QiblaEngine` are byte for byte the same); what changed in 1.6.0 was the pointer. The step 6 teardrop
 *    was 0.24 of the radius across, ended at 0.64 with a round stroked tip, and stopped under the Kaaba
 *    mark's lower edge, while the mark itself shrank from 0.30 to 0.22. A blunt tip resting under a small
 *    mark can sit anywhere along the mark's base and look "arrived", so the dial could not be read to
 *    better than several degrees.
 * 2. On the slimmed teardrop that answered it (12 Nov): "not looking nice to me ... make it better looking
 *    but accuracy is the most important criteria ... Clean design." He left the look to us.
 *
 * So the shape is the one instrument makers settled on for exactly this job, reading a direction against a
 * mark: straight edges that the eye can extend, a point that ends at [NEEDLE_TIP] (inside the Kaaba mark,
 * one hundredth short of its centre, [KAABA_DISTANCE]), a ring that says where the pivot is, and a tail that
 * balances it. Where the blade enters the mark it is under a tenth of the cube's width, so "is it centred?"
 * is answered by eye. Chosen from four drawn side by side (the slim teardrop, this hand, a compass lozenge,
 * a shaft with an arrowhead): the lozenge is as readable but heavier; the arrowhead hides the very point
 * that has to be read.
 *
 * Do not fatten, shorten or round it again: the test fails if you do.
 */
internal const val NEEDLE_TIP = 0.69f

/** Half the blade's width at its widest, where it meets the ring. The whole blade is 0.064 across. */
internal const val NEEDLE_HALF_WIDTH = 0.032f

/** How far above the centre the blade is at its widest; from there it runs straight to the point. */
internal const val NEEDLE_SHOULDER = 0.06f

/** The tail's length behind the centre, and its half width at the end (a little narrower than the blade). */
internal const val NEEDLE_TAIL = 0.16f
internal const val NEEDLE_TAIL_HALF_WIDTH = 0.026f

/** The ring at the pivot, and the hole in it. The widest thing about the needle: 0.14 across. */
internal const val NEEDLE_HUB = 0.07f
internal const val NEEDLE_HUB_HOLE = 0.028f

/**
 * Half the blade's width at [up] shares of the radius above the centre: straight from the shoulder to
 * nothing at the tip. The path below is drawn from the same numbers; the test reads the shape from here.
 */
internal fun needleHalfWidthAt(up: Float): Float = when {
    up >= NEEDLE_TIP -> 0f
    up >= NEEDLE_SHOULDER -> NEEDLE_HALF_WIDTH * (NEEDLE_TIP - up) / (NEEDLE_TIP - NEEDLE_SHOULDER)
    else -> NEEDLE_HALF_WIDTH
}

/**
 * Where the hand and the Kaaba mark stand on the screen, in degrees clockwise from straight up: the Qibla
 * bearing minus the heading. [dialRotation] is the ring's turn, which is minus the heading (zero without a
 * compass), so this is also "the bearing, on a ring turned that far". `QiblaDialGeometryTest` holds it.
 */
internal fun handRotation(dialRotation: Float, qiblaBearing: Double): Float = dialRotation + qiblaBearing.toFloat()

/**
 * The ring's next turn: [target] reached from [previous] by the shortest way round, so the result may lie
 * outside 0 to 360 and is equal to [target] only as an angle. See `CompassDial`.
 */
internal fun continuousTurn(previous: Float, target: Float): Float =
    previous + QiblaEngine.relativeTurn(previous.toDouble(), target.toDouble()).toFloat()

/**
 * The fixed mark at the top of the dial, "the top of your phone": a small wedge on the bezel pointing in.
 * Base at [TOP_MARK_BASE] of the radius, point at [TOP_MARK_TIP], just past the tick ring's outer end
 * (0.877) and well clear of the Kaaba mark when the hand stands under it (its ink ends at 0.816).
 * Deliberately small: the hand is the instruction, this is only where the hand has to end up.
 */
internal const val TOP_MARK_BASE = 0.985f
internal const val TOP_MARK_TIP = 0.875f
internal const val TOP_MARK_HALF_WIDTH = 0.05f

private fun topMarkPath(cx: Float, cy: Float, r: Float): Path = Path().apply {
    moveTo(cx, cy - TOP_MARK_TIP * r)
    lineTo(cx - TOP_MARK_HALF_WIDTH * r, cy - TOP_MARK_BASE * r)
    lineTo(cx + TOP_MARK_HALF_WIDTH * r, cy - TOP_MARK_BASE * r)
    close()
}

private fun DrawScope.drawTopMark(centre: Offset, radius: Float, colour: Color) =
    drawPath(path = topMarkPath(centre.x, centre.y, radius), color = colour)

/**
 * The blade and tail as one outline, dial centre at (`cx`, `cy`), y down: the point, down the left edge
 * to the shoulder, on to the tail's end, across, and back up the right edge. Every x on the right is the
 * left's negated, which is what keeps the point on the dial's centre line.
 */
private fun needlePath(cx: Float, cy: Float, r: Float): Path = Path().apply {
    moveTo(cx, cy - NEEDLE_TIP * r)
    lineTo(cx - NEEDLE_HALF_WIDTH * r, cy - NEEDLE_SHOULDER * r)
    lineTo(cx - NEEDLE_TAIL_HALF_WIDTH * r, cy + NEEDLE_TAIL * r)
    lineTo(cx + NEEDLE_TAIL_HALF_WIDTH * r, cy + NEEDLE_TAIL * r)
    lineTo(cx + NEEDLE_HALF_WIDTH * r, cy - NEEDLE_SHOULDER * r)
    close()
}

/**
 * The needle: the blade, then the ring over it, then the hole in the ring in [hole] (whatever is behind
 * the needle: the dial's face, or the page in the key). Drawn pointing up; the dial turns it to the Qibla
 * ([handRotation]). Its point sits inside the Kaaba mark, see [KAABA_DISTANCE], so the hand always runs
 * into the middle of the Kaaba.
 */
private fun DrawScope.drawNeedle(centre: Offset, radius: Float, colour: Color, hole: Color) {
    drawPath(path = needlePath(centre.x, centre.y, radius), color = colour)
    drawCircle(color = colour, radius = radius * NEEDLE_HUB, center = centre)
    drawCircle(color = hole, radius = radius * NEEDLE_HUB_HOLE, center = centre)
}

/**
 * How far out the Kaaba mark sits, and how big it is, both as a share of the dial radius.
 *
 * Shares rather than dp because the dial is not a fixed size: it shrinks in landscape and
 * at a large font scale (see [dialMaxWidth]), and a mark measured in dp would grow into
 * the tick ring as the dial got smaller.
 *
 * **One budget, five numbers, outside in (STEP-6-DESIGN 16.1): bezel inner edge 0.92 (0.89 aligned); ticks
 * 0.845 to 0.87 (cardinal from 0.80); letters 0.695 to 0.785; Kaaba ink 0.59 to 0.816; needle tip 0.69.**
 * Move one and check the others (`QiblaDialGeometryTest` holds the needle's and the mark's together). The
 * mark's box is 0.59 to 0.81 at a cardinal bearing and its corner reaches 0.856 at a diagonal one, crossing
 * the minor ticks, but the drawn artwork (the plinth of `ic_kaaba.xml`) reaches only 0.816 at worst, under
 * the ticks and the aligned bezel. The mark overlaps the letters' band and is drawn last.
 *
 * The hand's point is *inside* the box when the two coincide, one hundredth short of the mark's centre,
 * which is what makes the hand run into the middle of the Kaaba on alignment rather than stop beside it
 * or under it. It stays just inside the letters' band (0.695), so it never touches a letter. The watch
 * keeps its own dial and its own shares (0.66 and 0.30 for the mark); see `WearApp.kt`.
 */
internal const val KAABA_DISTANCE = 0.70f
internal const val KAABA_SIZE = 0.22f

/**
 * The shortest gap between two arrival buzzes. Long enough to swallow a compass settling
 * after the screen opens, short enough that a user who really does turn away and back is
 * told again. Shared with the watch by copy rather than by a common constant, because the
 * modules share only `core` and this is a feel value, not a rule.
 */
private const val ARRIVAL_QUIET_MILLIS = 5_000L

/** The arrival buzz: one tap, 70 ms. Why it goes through the Vibrator and not a haptic constant: [buzz]. */
private fun arrivalBuzz(context: android.content.Context) = buzz(context, 70)

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
        heading == null -> stringResource(R.string.qibla_no_compass, QiblaEngine.wholeDegrees(qiblaTrueBearing))
        aligned -> stringResource(R.string.qibla_facing)
        else -> {
            // One number, derived from the other, so the two lines on screen always add up: a reader
            // who subtracts the facing from the Qibla bearing must get the turn printed beside it. Rounding
            // the turn on its own beat the facing by up to a degree before (12 Nov 2026).
            val shownBearing = QiblaEngine.wholeDegrees(qiblaTrueBearing)
            val shownHeading = QiblaEngine.wholeDegrees(heading)
            val turn = QiblaEngine.relativeTurn(shownHeading.toDouble(), shownBearing.toDouble())
            val degrees = kotlin.math.abs(turn).roundToInt()
            when {
                degrees == 0 -> stringResource(R.string.qibla_bearing, shownBearing)
                turn > 0 -> stringResource(R.string.qibla_turn_right, degrees)
                else -> stringResource(R.string.qibla_turn_left, degrees)
            }
        }
    }

    // Start aligned under the title like the city and date on Times.
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
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
                textAlign = TextAlign.Start,
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
                text = stringResource(R.string.qibla_heading_now, QiblaEngine.wholeDegrees(heading)),
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

/**
 * The two marks on the dial, drawn small and named: the green hand is the Kaaba, the wedge is the top of
 * your phone. Without it they are just shapes, and "which one am I?" is the first thing anyone asks. The
 * keys are the marks themselves in miniature rather than colour swatches, so the key still works for
 * someone who cannot tell green from grey. Hidden when there is no compass, because then there is no "you"
 * on the dial to explain.
 */
@Composable
private fun DialLegend() {
    val scheme = MaterialTheme.colorScheme

    // Marks sized from the text line, so they grow with it.
    val box = textLineHeight()

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
            // The dial's own hand in miniature, from the same numbers, so the key matches what it explains:
            // the green hand is the Kaaba (the mark on its point is a picture and needs no key). It takes
            // the whole line height: the hand is slim, and at 0.75 its blade was under a dp wide.
            val page = scheme.background
            val green = scheme.primary
            Canvas(Modifier.size(box)) {
                // The shape spans the tip above the centre to the tail below it, so r is the box over that
                // span and the dial centre sits half the difference of the two below the middle.
                val r = size.height / (NEEDLE_TIP + NEEDLE_TAIL)
                drawNeedle(
                    centre = Offset(size.width / 2f, size.height / 2f + (NEEDLE_TIP - NEEDLE_TAIL) / 2f * r),
                    radius = r,
                    colour = green,
                    hole = page,
                )
            }
        }
        Key(stringResource(R.string.qibla_legend_top)) {
            // The top mark, enlarged: on the dial's own scale it would be a speck here.
            val ink = scheme.onSurfaceVariant
            Canvas(Modifier.size(box)) {
                val r = size.height * 0.6f / (TOP_MARK_BASE - TOP_MARK_TIP)
                drawTopMark(Offset(size.width / 2f, size.height * 0.2f + TOP_MARK_BASE * r), r, ink)
            }
        }
    }
}
