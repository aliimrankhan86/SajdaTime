package com.sajdatime.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives [WidgetRefreshScheduler]'s alarm and redraws both widgets. The redraw sets the next alarm
 * itself (see [WidgetUpdater.refreshAll]), so this is the whole chain: one alarm, one redraw, the next
 * alarm. Not exported: only this app's own PendingIntent can reach it.
 */
class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WidgetRefreshScheduler.ACTION_REFRESH) return
        WidgetUpdater.refreshFromProvider(this, context)
    }
}
