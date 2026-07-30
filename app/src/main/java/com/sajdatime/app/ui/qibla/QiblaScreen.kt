package com.sajdatime.app.ui.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
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

    // The dial rotates opposite to the device so north stays physically north. When there
    // is no compass the dial is held still and only the Qibla arrow is drawn, pointing at
    // the true bearing from north.
    val dialRotation by animateFloatAsState(
        targetValue = if (heading != null) -heading.toFloat() else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "dial",
    )

    val aligned = heading != null && QiblaEngine.isAligned(heading, qiblaTrueBearing, 5.0)
    val needleColour = if (aligned) scheme.primary else scheme.tertiary

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

            drawCircle(
                color = scheme.surfaceVariant,
                radius = radius,
                center = centre,
            )
            drawCircle(
                color = scheme.outlineVariant,
                radius = radius,
                center = centre,
                style = Stroke(width = 2f),
            )

            rotate(degrees = dialRotation, pivot = centre) {
                drawTicks(centre, radius, scheme.outline)
                drawCardinalMarker(centre, radius, scheme.onSurface)

                // Qibla needle, drawn in dial space so it tracks true north correctly.
                rotate(degrees = qiblaTrueBearing.toFloat(), pivot = centre) {
                    drawNeedle(centre, radius, needleColour)
                }
            }

            // Fixed reference mark at the top: "you are pointing here".
            drawCircle(
                color = scheme.onSurface,
                radius = 5f,
                center = Offset(centre.x, centre.y - radius + 14f),
            )
        }
        // Nothing is drawn over the centre. An icon and a heading readout there sat
        // directly on top of the needle and made both harder to read.
    }
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

/** The Qibla pointer: a long tapered arrow from the centre outward. */
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

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
