package com.sajdatime.wear

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.labelRes
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.R as CoreR
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

/**
 * Content padding for a [ScalingLazyColumn] on a round watch, used with `autoCentering = null`.
 *
 * ScalingLazyColumn's default auto-centring reserves roughly half a screen of trailing space
 * so the final item can come to rest in the middle. At the bottom of a list that space has to
 * come from somewhere, and where it comes from is the top: everything above the last item is
 * hoisted into the narrow cap of the circle, which is also where AppScaffold paints the watch
 * face clock. On the 227dp round at font scale 1.3 that put "Sunni · Shafi'i" directly
 * underneath the time — grey on green, unreadable — and pressed the top of the button flat
 * against the rim. It was wrong on the 192dp round too, just less legibly so.
 *
 * With auto-centring off the list simply stops when its last item reaches the bottom inset,
 * so nothing is lifted anywhere. The insets then have to do the work auto-centring was doing:
 * keep the first and last items off the rim.
 *
 * The bottom inset is a *share of the screen*, not a fixed dp, and that is the whole point. A
 * round display loses width fastest near its edge — on a 227dp watch the usable chord is
 * 199dp at 26% up from the bottom, 123dp at 8%, and 77dp at 3% — so the inset that keeps a
 * wide item inside the glass has to be proportional to the diameter, not constant.
 *
 * 26% is a verified value, not a derived one: it is what was screenshotted clean on both
 * 192dp and 227dp at font scale 1.0 and 1.3, with `tools/wear-round-check.py` confirming
 * nothing lands behind the bezel. It is not claimed to be the minimum, and it should not be
 * trimmed on arithmetic alone — pull it down and the last item drifts towards the rim, push
 * it up and the list scrolls further, which is what put the button under the clock to begin
 * with. Both directions fail, so re-run `tools/wear-verify.sh` before changing it.
 *
 * ponytail: a plain function over LocalConfiguration rather than Horologist's responsive
 * padding helpers — two numbers do not justify the dependency, and the Wear Compose guidance
 * is explicit that the Horologist layout libraries should not be used with Material3.
 */
@Composable
private fun roundListPadding(fromScaffold: PaddingValues): PaddingValues = PaddingValues(
    top = fromScaffold.calculateTopPadding(),
    bottom = (LocalConfiguration.current.screenHeightDp * 0.26f).dp,
)

@Composable
fun WearApp(viewModel: WearViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Two pages, swiped left and right. On a small round screen this beats a menu.
    val pager = rememberPagerState(pageCount = { 2 })
    var choosingSchool by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pager.currentPage) {
        viewModel.setQiblaVisible(pager.currentPage == PAGE_QIBLA)
    }

    AppScaffold {
        if (choosingSchool) {
            // A full screen rather than a third pager page. Two pages are already the most
            // a user will discover by swiping; a setting they touch once does not deserve
            // to sit permanently between the times and the Qibla.
            SchoolPage(
                sect = state.settings.sect,
                madhab = state.settings.madhab,
                onSelectSect = viewModel::setSect,
                onSelectMadhab = viewModel::setMadhab,
                onDone = { choosingSchool = false },
            )
            return@AppScaffold
        }
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                PAGE_TIMES -> TimesPage(
                    state = state,
                    onUseDefaultLocation = viewModel::useDefaultLocation,
                    onChooseSchool = { choosingSchool = true },
                )

                else -> QiblaPage(state)
            }
        }
    }
}

@Composable
private fun TimesPage(
    state: WearUiState,
    onUseDefaultLocation: () -> Unit,
    onChooseSchool: () -> Unit,
) {
    // initialCenterItemIndex = 0, not the default 1: ScalingLazyColumn otherwise opens
    // centred on the second item, which scrolls the countdown card up under the watch face
    // clock. With autoCentering off (see roundListPadding) the list already opens at the
    // top and this is belt and braces, but it costs nothing and it is the setting that
    // matches the padding. This is the first thing a user sees when they raise their
    // wrist, so it has to be right on launch and not one flick later.
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
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
            autoCentering = null,
            contentPadding = roundListPadding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Clears the watch face clock that AppScaffold draws along the top.
                    // Without this the prayer name sits underneath it and is unreadable.
                    //
                    // 16dp, not the 44dp this used to be. The larger figure was paying for
                    // auto-centring, which opened the list already scrolled and ate most of
                    // it; with auto-centring off the list starts where it is put, and 44dp
                    // on top of the scaffold's own inset pushed the first screen down far
                    // enough to lose two prayer rows off the bottom of a 192dp watch. On a
                    // screen this small, rows the user can see without scrolling are the
                    // whole point.
                    modifier = Modifier.padding(top = 16.dp),
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

                // Always visible, at the end of the times it produced. A watch that never
                // received the phone's settings is otherwise indistinguishable from one
                // that did, and the difference is over an hour of Asr.
                //
                // Last, not second-to-last, and that ordering is load-bearing. At the
                // bottom of the scroll the list is anchored from below: the final item
                // rests on the bottom inset and everything else stacks upwards from it, so
                // whichever item is second-to-last is the one that ends up against the top
                // of the screen — under the clock AppScaffold paints there, in the narrow
                // cap of the circle. With the button there, a 192dp watch at font scale 1.3
                // printed the time straight through "Sunni · Hanafi".
                //
                // Padding cannot fix this from either side: above the button it changes
                // nothing (the anchor is below it) and below the button it pushes the
                // button further up. The only thing that moves the button down is putting
                // something taller after it. The disclaimer is that something — three or
                // four wrapped lines against the button's one — and it is the better
                // casualty of the two: it is read on the way past rather than aimed at,
                // and it is still shown in full, in the same list, immediately above.
                // What must never end up illegible is the control the user has to hit.
                item {
                    Button(
                        onClick = onChooseSchool,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        label = {
                            Text(
                                text = schoolSummary(state.settings.sect, state.settings.madhab),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun schoolSummary(sect: Sect, madhab: Madhab): String = when (sect) {
    Sect.SHIA -> stringResource(R.string.wear_sect_shia)
    Sect.SUNNI -> stringResource(
        R.string.wear_school_pair,
        stringResource(R.string.wear_sect_sunni),
        stringResource(madhabLabelRes(madhab)),
    )
}

private fun madhabLabelRes(madhab: Madhab): Int = when (madhab) {
    Madhab.HANAFI -> R.string.wear_madhab_hanafi
    Madhab.SHAFII -> R.string.wear_madhab_shafii
    Madhab.MALIKI -> R.string.wear_madhab_maliki
    Madhab.HANBALI -> R.string.wear_madhab_hanbali
}

/**
 * Sect and madhab, chosen on the watch. Same two-step shape as the phone: pick Sunni or
 * Shia, and only Sunni goes on to offer a madhab, because Jafari has no madhab to pick.
 */
@Composable
private fun SchoolPage(
    sect: Sect,
    madhab: Madhab,
    onSelectSect: (Sect) -> Unit,
    onSelectMadhab: (Madhab) -> Unit,
    onDone: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            autoCentering = null,
            contentPadding = roundListPadding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    text = stringResource(R.string.wear_school),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 44.dp, bottom = 4.dp),
                )
            }
            items(Sect.entries.toList()) { entry ->
                ChoiceChip(
                    label = stringResource(
                        if (entry == Sect.SUNNI) R.string.wear_sect_sunni else R.string.wear_sect_shia,
                    ),
                    isSelected = sect == entry,
                    onClick = { onSelectSect(entry) },
                )
            }
            if (sect == Sect.SUNNI) {
                item {
                    Text(
                        text = stringResource(R.string.wear_madhab_note),
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(Madhab.entries.toList()) { entry ->
                    ChoiceChip(
                        label = stringResource(madhabLabelRes(entry)),
                        isSelected = madhab == entry,
                        onClick = { onSelectMadhab(entry) },
                    )
                }
            }
            item {
                Button(
                    onClick = onDone,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    label = {
                        Text(
                            text = stringResource(android.R.string.ok),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
    }
}

/**
 * A full-width selectable row. Wear has no radio button worth using at 192dp, so the
 * selected option is carried by the filled versus outlined treatment instead.
 */
@Composable
private fun ChoiceChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colours = if (isSelected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    Button(
        onClick = onClick,
        colors = colours,
        // Without the border the unselected options render as bare text on black and stop
        // looking tappable at all, which on a 192dp screen is the difference between an
        // option and a label.
        border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder(enabled = true),
        // Filled versus outlined is the only visual cue, so the selected state has to be
        // stated outright for a screen reader rather than left to the colours.
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .semantics { selected = isSelected },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
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
            // Green, aligned or not — the same rule as the phone. The arrow's job is
            // "the Qibla is that way", and a colour that changes underneath it invites
            // the reading that the direction itself has changed.
            val needleColour = MaterialTheme.colorScheme.primary
            val dialColour = if (aligned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
            val tickColour = MaterialTheme.colorScheme.onSurfaceVariant
            val arcColour = MaterialTheme.colorScheme.primary
            val turn = if (heading != null) QiblaEngine.relativeTurn(heading, qibla) else 0.0

            // Read outside the Canvas because both are composable calls and the draw
            // lambda is not one.
            val kaaba = painterResource(CoreR.drawable.ic_kaaba)
            val kaabaDetail = painterResource(CoreR.drawable.ic_kaaba_detail)
            val kaabaColour = MaterialTheme.colorScheme.onSurface

            Canvas(
                Modifier
                    .fillMaxSize()
                    .padding(18.dp),
            ) {
                val radius = size.minDimension / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = dialColour, radius = radius, center = centre)

                // The turn still owed, from the fixed tick at the top round to the
                // arrow. Same idea as the phone dial, at watch scale.
                if (heading != null && !aligned) {
                    val thickness = radius * 0.12f
                    val inset = thickness / 2f
                    drawArc(
                        color = arcColour,
                        startAngle = -90f,
                        sweepAngle = turn.toFloat(),
                        useCenter = false,
                        topLeft = Offset(centre.x - radius + inset, centre.y - radius + inset),
                        size = Size((radius - inset) * 2f, (radius - inset) * 2f),
                        style = Stroke(width = thickness),
                    )
                }

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
                    //
                    // The tip stops at 0.60 rather than the 0.86 it used to, which is just
                    // short of the Kaaba mark rather than inside it. Buried, what was left
                    // showing was a stubby trapezoid squeezed between the readout and the
                    // cube, and it stopped reading as an arrow at all; stopping short
                    // leaves a whole triangle that points at the mark.
                    //
                    // "Just short" moves with the bearing, and there is no single number
                    // that is exact: the mark stays upright while the needle goes round,
                    // so the needle arrives at its flat side at some angles and at a
                    // corner at others, and the near edge sits at 0.60 of the radius in
                    // the first case and about 0.55 in the second. 0.60 splits it — a
                    // hair's gap at the flats, a hair's overlap at the corners, and
                    // neither is visible at watch size.
                    rotate(degrees = qibla.toFloat(), pivot = centre) {
                        val tip = Offset(centre.x, centre.y - radius * 0.60f)
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

                // Fixed "you are pointing here" tick, the origin the arc is measured
                // from. Drawn outside the rotate block so it stays at the top.
                if (heading != null) {
                    drawLine(
                        color = tickColour,
                        start = Offset(centre.x, centre.y - radius),
                        end = Offset(centre.x, centre.y - radius * 0.74f),
                        strokeWidth = 4f,
                    )
                }

                // Last, so it covers the facing tick on the one occasion they coincide:
                // when you are already facing the Qibla, and the tick has nothing left
                // to tell you.
                drawKaaba(
                    centre = centre,
                    radius = radius,
                    bearingDegrees = (qibla - (heading ?: 0.0)).toFloat(),
                    silhouette = kaaba,
                    detail = kaabaDetail,
                    tint = kaabaColour,
                    face = dialColour,
                )
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
 * Where the Kaaba mark sits on the dial and how big it is, as shares of the dial radius.
 *
 * The same two numbers as the phone's `QiblaScreen.kt`, and they have to stay the same:
 * this is one dial drawn twice, and the two watch sizes plus every phone size mean shares
 * are the only form these can take. The 1.2" watch is the tightest case in the app: its
 * tick ring starts at 0.897 of the radius and the mark's furthest corner reaches 0.892, a
 * margin of well under a pixel. It costs nothing today — the mark is drawn last, so at
 * worst it hides a tick — but it does mean the distance has no headroom, and this is the
 * screen to check on if either number is ever changed.
 */
private const val KAABA_DISTANCE = 0.72f
private const val KAABA_SIZE = 0.32f

/**
 * The Kaaba itself, at the far end of the needle. The phone draws the same mark from the
 * same shared artwork; see `QiblaScreen.kt` for why it stays upright instead of rotating
 * with the dial, and `ic_kaaba.xml` for why the band and door are painted over the
 * silhouette rather than cut out of it.
 *
 * It earns its space here more than it does on the phone. The watch has no room for the
 * turn instruction the phone prints under the dial, so before this the only thing on the
 * screen naming a direction was a number of degrees.
 */
private fun DrawScope.drawKaaba(
    centre: Offset,
    radius: Float,
    bearingDegrees: Float,
    silhouette: Painter,
    detail: Painter,
    tint: Color,
    face: Color,
) {
    // -90 because the dial's zero is straight up and trigonometry's is three o'clock.
    val radians = Math.toRadians(bearingDegrees.toDouble() - 90.0)
    val box = radius * KAABA_SIZE
    translate(
        left = centre.x + radius * KAABA_DISTANCE * cos(radians).toFloat() - box / 2f,
        top = centre.y + radius * KAABA_DISTANCE * sin(radians).toFloat() - box / 2f,
    ) {
        with(silhouette) { draw(size = Size(box, box), colorFilter = ColorFilter.tint(tint)) }
        with(detail) { draw(size = Size(box, box), colorFilter = ColorFilter.tint(face)) }
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
