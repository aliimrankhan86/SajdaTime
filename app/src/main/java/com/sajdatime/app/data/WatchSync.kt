package com.sajdatime.app.data

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.sajdatime.core.WatchSyncContract
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

    suspend fun publish(context: Context, settings: AppSettings) = withContext(Dispatchers.IO) {
        runCatching {
            val request = PutDataMapRequest.create(WatchSyncContract.PATH_SETTINGS).apply {
                dataMap.putString(WatchSyncContract.KEY_SECT, settings.sect.name)
                dataMap.putString(WatchSyncContract.KEY_MADHAB, settings.madhab.name)
                dataMap.putString(WatchSyncContract.KEY_METHOD, settings.method.name)
                settings.coordinates?.let {
                    dataMap.putDouble(WatchSyncContract.KEY_LATITUDE, it.latitude)
                    dataMap.putDouble(WatchSyncContract.KEY_LONGITUDE, it.longitude)
                }
                dataMap.putString(WatchSyncContract.KEY_CITY, settings.cityName)
                dataMap.putLong(WatchSyncContract.KEY_UPDATED_AT, System.currentTimeMillis())
            }

            Wearable.getDataClient(context).putDataItem(
                request.asPutDataRequest().setUrgent(),
            )
        }
        Unit
    }
}
