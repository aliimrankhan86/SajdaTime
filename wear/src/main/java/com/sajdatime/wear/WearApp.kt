package com.sajdatime.wear

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.labelRes
import com.sajdatime.core.QiblaEngine
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val PAGE_TIMES = 0
private const val PAGE_QIBLA = 1

@Composable
fun WearApp(viewModel: WearViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Two pages, swiped left and right. On a small round screen this beats a menu.
    val pager = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(pager.currentPage) {
        viewModel.setQiblaVisible(pager.currentPage == PAGE_QIBLA)
    }

    AppScaffold {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                PAGE_TIMES -> TimesPage(state, viewModel::useDefaultLocation)
                else -> QiblaPage(state)
            }
        }
    }
}

@Composable
private fun TimesPage(state: WearUiState, onUseDefaultLocation: () -> Unit) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current

    ScreenScaffold(scrollState = listState) { contentPadding ->
        if (state.locating) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.wear_locating),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            return@ScreenScaffold
        }

        if (state.needsLocation) {
            NoLocation(onUseDefaultLocation)
            return@ScreenScaffold
        }

        val today = state.today
        val next = state.next

        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Clears the watch face clock that AppScaffold draws along the top.
                    // Without this the prayer name sits underneath it and is unreadable.
                    modifier = Modifier.padding(top = 44.dp),
                ) {
                    Text(
                        text = next?.slot?.let { stringResource(it.labelRes) } ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = next?.let { countdown(state.now, it.at) } ?: "",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        text = next?.let { clock(context, it.at) } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (today != null) {
                items(PrayerSlot.entries.filter { it.isPrayer }) { slot ->
                    val at = today.times[slot] ?: return@items
                    val isNext = slot == next?.slot && next.isTomorrow == false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(slot.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isNext) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        Text(
                            text = clock(context, at),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isNext) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.wear_disclaimer),
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

/**
 * The dead end this replaces: a watch with no fix and no paired phone showed a prompt
 * the user had no way to satisfy from the watch. Makkah is offered as an honest,
 * clearly-labelled fallback rather than nothing at all.
 */
@Composable
private fun NoLocation(onUseDefaultLocation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.wear_no_location_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.wear_no_location_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onUseDefaultLocation,
            label = {
                Text(
                    text = stringResource(R.string.wear_use_makkah),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
        )
    }
}

@Composable
private fun QiblaPage(state: WearUiState) {
    ScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val qibla = state.qiblaBearing
            if (qibla == null) {
                Text(
                    text = stringResource(R.string.wear_qibla_needs_location),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                return@Box
            }

            val heading = state.heading
            val aligned = heading != null && QiblaEngine.isAligned(heading, qibla, 5.0)
            val needleColour = if (aligned) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.tertiary
            }
            val dialColour = MaterialTheme.colorScheme.surfaceContainer
            val tickColour = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(
                Modifier
                    .fillMaxSize()
                    .padding(18.dp),
            ) {
                val radius = size.minDimension / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = dialColour, radius = radius, center = centre)

                rotate(degrees = -(heading ?: 0.0).toFloat(), pivot = centre) {
                    for (degrees in 0 until 360 step 30) {
                        val radians = Math.toRadians(degrees.toDouble() - 90.0)
                        val outer = Offset(
                            centre.x + (radius - 6f) * cos(radians).toFloat(),
                            centre.y + (radius - 6f) * sin(radians).toFloat(),
                        )
                        val inner = Offset(
                            centre.x + (radius - 16f) * cos(radians).toFloat(),
                            centre.y + (radius - 16f) * sin(radians).toFloat(),
                        )
                        drawLine(tickColour, inner, outer, strokeWidth = 2f)
                    }

                    // The needle floats between the rim and the middle rather than
                    // starting at the centre, so it never lies across the readout.
                    rotate(degrees = qibla.toFloat(), pivot = centre) {
                        val tip = Offset(centre.x, centre.y - radius * 0.86f)
                        val base = centre.y - radius * 0.40f
                        drawPath(
                            path = Path().apply {
                                moveTo(tip.x, tip.y)
                                lineTo(centre.x - radius * 0.09f, base)
                                lineTo(centre.x + radius * 0.09f, base)
                                close()
                            },
                            color = needleColour,
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (aligned) {
                        stringResource(R.string.wear_qibla_facing)
                    } else {
                        stringResource(R.string.wear_qibla_bearing, qibla.roundToInt())
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = needleColour,
                )
                if (heading == null) {
                    Text(
                        text = stringResource(R.string.wear_qibla_from_true_north),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Clock time, honouring the watch's own 12/24-hour setting. This used to be hardcoded to
 * 24-hour, which showed "17:14" to users whose watch was set to show "5:14 PM".
 */
private fun clock(context: Context, instant: Instant): String {
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
    return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        .format(instant.atZone(ZoneId.systemDefault()))
}

@Composable
private fun countdown(from: Instant, to: Instant): String {
    val duration = Duration.between(from, to).let { if (it.isNegative) Duration.ZERO else it }
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return if (hours > 0) {
        stringResource(R.string.wear_duration_h_m, hours, minutes)
    } else {
        stringResource(R.string.wear_duration_m, minutes)
    }
}
