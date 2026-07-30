package com.sajdatime.app.data

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Publishes the settings a paired Wear OS watch needs to calculate prayer times itself.
 *
 * On privacy: this moves data between two devices the same person owns, over Google's
 * local Data Layer. It does not reach a server and it is not analytics. If the user has
 * no watch, nothing is ever published and the call is a no-op.
 *
 * ponytail: fire and forget. A failed sync is not worth surfacing, because the watch
 * falls back to its own stored settings and keeps working. The next settings change
 * publishes again.
 */
object WatchSync {

    private const val PATH_SETTINGS = "/sajdatime/settings"

    suspend fun publish(context: Context, settings: AppSettings) = withContext(Dispatchers.IO) {
        runCatching {
            val request = PutDataMapRequest.create(PATH_SETTINGS).apply {
                dataMap.putString("sect", settings.sect.name)
                dataMap.putString("madhab", settings.madhab.name)
                dataMap.putString("method", settings.method.name)
                settings.coordinates?.let {
                    dataMap.putDouble("latitude", it.latitude)
                    dataMap.putDouble("longitude", it.longitude)
                }
                dataMap.putString("city", settings.cityName)
                // The Data Layer skips items whose contents are unchanged, so a
                // timestamp is needed for a re-send to actually go out.
                dataMap.putLong("updated_at", System.currentTimeMillis())
            }

            Wearable.getDataClient(context).putDataItem(
                request.asPutDataRequest().setUrgent(),
            )
        }
        Unit
    }
}
