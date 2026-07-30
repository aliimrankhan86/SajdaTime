package com.sajdatime.wear

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
import com.sajdatime.core.PrayerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * The tile the user swipes to from the watch face: next prayer, its time, and how long
 * is left.
 *
 * Tiles are static snapshots, not live views. Rather than push updates every second, the
 * tile asks to be refreshed shortly after the next prayer starts, so it is correct
 * whenever the user actually looks at it and costs nothing in between.
 */
class NextPrayerTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> = scope.future {
        val settings = WearSettingsStore(applicationContext).settings.first()
        val coordinates = settings.coordinates
        val now = Instant.now()

        val (headline, detail, refreshAfter) = if (coordinates == null) {
            Triple(
                getString(R.string.tile_label),
                getString(R.string.tile_needs_setup),
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
            Triple(
                next.slot.label,
                getString(
                    R.string.wear_tile_detail,
                    TileFormat.clock(applicationContext, next.at),
                    TileFormat.humanise(applicationContext, remaining),
                ),
                TileFormat.freshness(remaining),
            )
        }

        TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(refreshAfter.toMillis())
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(
                                        layout(
                                            headline = headline,
                                            detail = detail,
                                            device = requestParams.deviceConfiguration,
                                        ),
                                    )
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
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
                            .setText(getString(R.string.tile_label).uppercase(Locale.getDefault()))
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
