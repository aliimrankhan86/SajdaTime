package com.sajdatime.core

/**
 * The wire format for the phone-to-watch settings handover.
 *
 * These names are a contract between two modules that never see each other's code: the
 * phone writes them into a Data Layer item, the watch reads them back out. Held here so
 * the two sides cannot drift, because a renamed key does not break the build, it just
 * silently stops syncing.
 */
object WatchSyncContract {
    const val PATH_SETTINGS = "/sajdatime/settings"

    const val KEY_SECT = "sect"
    const val KEY_MADHAB = "madhab"
    const val KEY_METHOD = "method"
    const val KEY_LATITUDE = "latitude"
    const val KEY_LONGITUDE = "longitude"
    const val KEY_CITY = "city"

    /**
     * The Data Layer drops an update whose payload is byte-identical to the last one, so
     * a re-send needs something that always changes.
     */
    const val KEY_UPDATED_AT = "updated_at"
}
