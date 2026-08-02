package com.sajdatime.app.notify

import android.content.Context
import com.sajdatime.core.PrayerEngine
import com.sajdatime.app.data.AppSettings
import java.time.Instant
import java.time.ZoneId

/**
 * The optional silent "next prayer" badge in the notification shade.
 *
 * ponytail: refreshed when something already woke us (alarm fires, app opens, daily job)
 * rather than ticking every minute. A live-updating badge would need a foreground
 * service and a permanent wake cost for a countdown the user can already see in the app.
 */
object OngoingBadge {

    fun refresh(context: Context, settings: AppSettings) {
        if (!settings.ongoingBadge) {
            Notifications.clearOngoingBadge(context)
            return
        }
        val coordinates = settings.coordinates ?: return
        val now = Instant.now()
        val next = PrayerEngine.nextPrayer(
            coordinates = coordinates,
            prefs = settings.calculationPrefs,
            now = now,
            zone = ZoneId.systemDefault(),
        )
        // Recomputed rather than carried on NextPrayer: the badge shows tomorrow's Fajr
        // once Isha has passed, and tomorrow can be a projected day when today was not.
        val approximate = PrayerEngine.compute(
            coordinates,
            next.at.atZone(ZoneId.systemDefault()).toLocalDate(),
            settings.calculationPrefs,
        ).approximatedFrom != null
        Notifications.postOngoingBadge(context, next.slot, next.at, now, approximate)
    }
}
