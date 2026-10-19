package com.sajdatime.wear

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.sajdatime.core.AppLocale
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * The tile the user swipes to from the watch face: next prayer, its time, and how long
 * is left.
 *
 * Tiles are static snapshots, not live views, and the watch keeps one until it asks again.
 * So the countdown is handed over as a timeline, one entry per minute for up to an hour
 * (TileFormat.countdownSteps), and the watch swaps the text on the minute without waking
 * the app. It asks again a minute after the prayer, or after the hour, whichever is first.
 * Until 19 Oct 2026 there was one entry, and the tile said "in 41m" for 41 minutes.
 */
class NextPrayerTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Every string on the tile is built from this rather than from the service itself, so
     * the countdown is formatted in the app's own language and not the watch's. Without it
     * the tile read "in ٢h ١٤m" on an Arabic watch, in a layout sized for Latin digits.
     * See AppLocale.kt.
     *
     * Made once per tile request, not held: the system keeps this service process alive between
     * requests, so a context made once (it was `by lazy`) would go on showing the old language
     * after the watch's language changed. (8 Oct 2026 branch, brought into 1.5.0 on 19 Oct 2026.)
     *
     * Run on the watch emulator, 19 Oct 2026: a tile drawn by a new process is in the app's
     * language (Urdu, and Arabic right to left with Western digits). A per-app language switch
     * while this service is still bound leaves its own context in the old language until the
     * service is created again; `applicationContext` was tried and is worse (it ignored the
     * app's language altogether), so it stays `this`. A change of the watch's own language was
     * not run.
     */
    private fun localised(): Context = AppLocale.wrap(this)

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> = scope.future {
        val settings = WearSettingsStore(applicationContext).settings.first()
        val coordinates = settings.coordinates
        val now = Instant.now()
        val localised = localised()

        // Each detail line with the moment it becomes true; the first is now.
        val (headline, details, refreshAfter) = if (coordinates == null) {
            Triple(
                localised.getString(R.string.tile_label),
                listOf(now to localised.getString(R.string.tile_needs_setup)),
                Duration.ofHours(1),
            )
        } else {
            val next = PrayerEngine.nextPrayer(
                coordinates = coordinates,
                prefs = settings.calculationPrefs,
                now = now,
                zone = ZoneId.systemDefault(),
            )
            val remaining = Duration.between(now, next.at)
            // The tile is the surface most likely to be the only one a watch user ever
            // reads, so a projected time has to say so here too. The phone's home screen
            // and the PDF have carried this marking for a while; the tile and the phone's
            // notifications were the two places it was missing, which were precisely the
            // two that reach the user without the app being open.
            val approximate = runCatching {
                PrayerEngine.compute(
                    coordinates,
                    next.at.atZone(ZoneId.systemDefault()).toLocalDate(),
                    settings.calculationPrefs,
                ).approximatedFrom != null
            }.getOrDefault(false)
            val clock = TileFormat.clock(localised, next.at)
            Triple(
                next.slot.label(localised),
                TileFormat.countdownSteps(now, next.at).map { (start, shown) ->
                    start to localised.getString(
                        if (approximate) R.string.wear_tile_detail_approx else R.string.wear_tile_detail,
                        clock,
                        TileFormat.humanise(localised, shown),
                    )
                },
                TileFormat.freshness(remaining),
            )
        }

        val timeline = TimelineBuilders.Timeline.Builder()
        details.forEachIndexed { index, (start, detail) ->
            val entry = TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder()
                    .setRoot(layout(headline, detail, requestParams.deviceConfiguration, localised))
                    .build(),
            )
            // Each entry runs until the next one starts. The last has no window, which makes it
            // the one the watch shows once the others have run out.
            details.getOrNull(index + 1)?.let { (end, _) ->
                entry.setValidity(
                    TimelineBuilders.TimeInterval.Builder()
                        .setStartMillis(start.toEpochMilli())
                        .setEndMillis(end.toEpochMilli())
                        .build(),
                )
            }
            timeline.addTimelineEntry(entry.build())
        }

        TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(refreshAfter.toMillis())
            .setTileTimeline(timeline.build())
            .build()
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> = Futures.immediateFuture(
        ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build(),
    )

    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    private fun layout(
        headline: String,
        detail: String,
        @Suppress("UNUSED_PARAMETER") device: DeviceParameters,
        localised: Context,
    ): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Box.Builder()
            .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
            .setHeight(androidx.wear.protolayout.DimensionBuilders.expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(14f))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                Column.Builder()
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            // Uppercased in the app's own language: Locale.getDefault()
                            // would apply Turkish casing rules to English words on a
                            // Turkish watch. See AppLocale.kt.
                            .setText(
                                localised.getString(R.string.tile_label)
                                    .uppercase(AppLocale.of(localised)),
                            )
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setSize(androidx.wear.protolayout.DimensionBuilders.sp(11f))
                                    .setColor(argb(COLOUR_MUTED))
                                    .build(),
                            )
                            .build(),
                    )
                    .addContent(Spacer.Builder().setHeight(dp(6f)).build())
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText(headline)
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setSize(androidx.wear.protolayout.DimensionBuilders.sp(24f))
                                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_BOLD)
                                    .setColor(argb(COLOUR_PRIMARY))
                                    .build(),
                            )
                            .build(),
                    )
                    .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText(detail)
                            .setMaxLines(2)
                            .setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder()
                                    .setSize(androidx.wear.protolayout.DimensionBuilders.sp(14f))
                                    .setColor(argb(COLOUR_ON_SURFACE))
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

    private companion object {
        const val RESOURCES_VERSION = "1"

        // The tile draws on the watch's own black background, so these are the dark
        // theme values from the phone palette.
        const val COLOUR_PRIMARY = 0xFF7FD1AE.toInt()
        const val COLOUR_ON_SURFACE = 0xFFE8EEEA.toInt()
        const val COLOUR_MUTED = 0xFFB3C2BA.toInt()
    }
}

/**
 * Asks the watch to draw the tile again now. Otherwise it keeps its last drawing until its own
 * refresh, up to an hour away, so new settings from the phone or the wrist, or a language
 * changed before the app was opened, would wait for it (watch emulator, 19 Oct 2026).
 */
internal fun refreshTile(context: Context) {
    // The application context: the updater binds with the context it is given and unbinds later.
    TileService.getUpdater(context.applicationContext).requestUpdate(NextPrayerTileService::class.java)
}
