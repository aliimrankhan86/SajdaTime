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
     * The per-prayer corrections, as one [AdjustmentCodec] string.
     *
     * Synced, and that is the point. A user who nudges Maghrib to match their mosque and
     * then sees the watch on their wrist disagree with the phone in their pocket has been
     * given two answers to a religious question by one app.
     */
    const val KEY_ADJUSTMENTS = "adjustments"

    /** Days the Hijri date is shifted by. Same reasoning as [KEY_ADJUSTMENTS]. */
    const val KEY_HIJRI_OFFSET = "hijri_offset"

    /**
     * The Data Layer drops an update whose payload is byte-identical to the last one, so
     * a re-send needs something that always changes.
     */
    const val KEY_UPDATED_AT = "updated_at"
}
