package com.sajdatime.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Redraws the tile when the watch's time zone or clock changes.
 *
 * The tile is a snapshot with its prayer time written in as text, and the watch keeps it until
 * its own refresh, up to an hour away. After a zone change it went on showing the old zone's
 * time (7:32 pm for a 5:32 pm Maghrib) while the countdown, which counts from absolute
 * instants, stayed right, so the two halves of one line disagreed. Found by the independent
 * review on 19 Oct 2026; the owner chose to fix it for 1.5.0 the same day. The phone app has
 * done the same for its alarms and widgets for a long time (SystemEventReceiver).
 *
 * Android resets each running process's default time zone as it sends this broadcast, and the
 * tile request comes later still (the watch binds the tile service first), so that request
 * computes in the new zone. TIME_SET also arrives when the user switches between 12 and 24 hour
 * time, so the tile's clock format follows at once too.
 */
class ClockChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in HANDLED_ACTIONS) refreshTile(context)
    }

    internal companion object {
        /** Every action the manifest filter delivers here; ClockChangeActionsTest holds the two together. */
        internal val HANDLED_ACTIONS = setOf(
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
        )
    }
}
