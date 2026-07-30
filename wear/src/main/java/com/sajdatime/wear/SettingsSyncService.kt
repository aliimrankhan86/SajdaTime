package com.sajdatime.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
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
            .filter { it.dataItem.uri.path == PATH_SETTINGS }
            .forEach { event ->
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                CoroutineScope(Dispatchers.IO).launch {
                    store.applyFromPhone(
                        sect = map.getString(KEY_SECT),
                        madhab = map.getString(KEY_MADHAB),
                        method = map.getString(KEY_METHOD),
                        // getDouble returns 0.0 for a missing key, which is a real
                        // coordinate off West Africa, so absence is checked explicitly.
                        latitude = if (map.containsKey(KEY_LATITUDE)) {
                            map.getDouble(KEY_LATITUDE)
                        } else {
                            null
                        },
                        longitude = if (map.containsKey(KEY_LONGITUDE)) {
                            map.getDouble(KEY_LONGITUDE)
                        } else {
                            null
                        },
                        cityName = map.getString(KEY_CITY),
                    )
                }
            }
        events.release()
    }

    companion object {
        const val PATH_SETTINGS = "/sajdatime/settings"
        const val KEY_SECT = "sect"
        const val KEY_MADHAB = "madhab"
        const val KEY_METHOD = "method"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_CITY = "city"
        const val KEY_UPDATED_AT = "updated_at"
    }
}
