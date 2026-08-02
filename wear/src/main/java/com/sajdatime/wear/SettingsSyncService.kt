package com.sajdatime.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.sajdatime.core.WatchSyncContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the settings the phone publishes.
 *
 * The transfer is one way, phone to watch, and travels over the Data Layer between two
 * devices the same person owns. No server is involved and nothing reaches the network.
 * If the phone is never paired the watch simply keeps its own local settings.
 */
class SettingsSyncService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val store = WearSettingsStore(applicationContext)

        events.filter { it.type == DataEvent.TYPE_CHANGED }
            .filter { it.dataItem.uri.path == WatchSyncContract.PATH_SETTINGS }
            .forEach { event ->
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                CoroutineScope(Dispatchers.IO).launch {
                    store.applyFromPhone(
                        sect = map.getString(WatchSyncContract.KEY_SECT),
                        madhab = map.getString(WatchSyncContract.KEY_MADHAB),
                        method = map.getString(WatchSyncContract.KEY_METHOD),
                        // getDouble returns 0.0 for a missing key, which is a real
                        // coordinate off West Africa, so absence is checked explicitly.
                        latitude = if (map.containsKey(WatchSyncContract.KEY_LATITUDE)) {
                            map.getDouble(WatchSyncContract.KEY_LATITUDE)
                        } else {
                            null
                        },
                        longitude = if (map.containsKey(WatchSyncContract.KEY_LONGITUDE)) {
                            map.getDouble(WatchSyncContract.KEY_LONGITUDE)
                        } else {
                            null
                        },
                        cityName = map.getString(WatchSyncContract.KEY_CITY),
                        adjustments = map.getString(WatchSyncContract.KEY_ADJUSTMENTS),
                        // getInt also returns 0 for a missing key, but here 0 *is* the
                        // correct meaning of absence — no shift — so it needs no guard.
                        hijriOffsetDays = map.getInt(WatchSyncContract.KEY_HIJRI_OFFSET),
                    )
                }
            }
        events.release()
    }
}
