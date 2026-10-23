package com.sajdatime.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.OngoingBadge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives [WidgetRefreshScheduler]'s alarm, redraws both widgets and reposts the next prayer badge. The redraw sets the next alarm
 * itself (see [WidgetUpdater.refreshAll]), so this is the whole chain: one alarm, one redraw, the next
 * alarm. Not exported: only this app's own PendingIntent can reach it.
 */
class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WidgetRefreshScheduler.ACTION_REFRESH) return
        // Widgets and the next prayer badge, both due at the same boundary. The badge is reposted
        // here because a prayer with alerts off has no alarm of its own to do it.
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                WidgetUpdater.refreshAll(appContext, settings)
                OngoingBadge.refresh(appContext, settings)
            } finally {
                pending.finish()
            }
        }
    }
}
