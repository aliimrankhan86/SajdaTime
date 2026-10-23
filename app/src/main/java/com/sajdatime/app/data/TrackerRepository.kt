package com.sajdatime.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.DayRecord
import com.sajdatime.core.PauseCodec
import com.sajdatime.core.PauseRange
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TrackerLogic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * The prayer record, in a DataStore file of its own (`sajdatime_tracker`), apart from the settings.
 *
 * Its own file so a settings write never rewrites it and so "Delete my prayer record" is one clear.
 * It sits in the `file` domain, which data_extraction_rules.xml already keeps out of cloud backup and
 * device transfer (TrackerPrivacyTest holds that), so nothing here leaves the phone. That is also why
 * the save and load file exists: without it a new phone would start from nothing.
 *
 * One Int per day ("d:2027-02-08" → bitmask), never pruned: ten years is under 100 KB. The day is a
 * LocalDate, never an instant, so a time zone or clock change cannot move a tick to another day.
 */
private val Context.trackerStore: DataStore<Preferences> by preferencesDataStore(
    name = "sajdatime_tracker",
    corruptionHandler = settingsCorruptionHandler(),
)

data class TrackerData(
    val records: Map<LocalDate, DayRecord> = emptyMap(),
    val pauses: List<PauseRange> = emptyList(),
    /** Written by a newer version of the app: shown, never changed (TrackerFileCodec says the same). */
    val readOnly: Boolean = false,
    /** The day tracking was first switched on; in this store, so Delete clears it with the record. */
    val startedOn: LocalDate? = null,
) {
    val isEmpty: Boolean get() = records.values.all { it.ticked.isEmpty() } && pauses.isEmpty()
}

data class MergeResult(val daysAdded: Int, val daysUpdated: Int)

class TrackerRepository(private val context: Context) {

    val data: Flow<TrackerData> = context.trackerStore.data.map { it.toTrackerData() }

    suspend fun current(): TrackerData = data.first()

    suspend fun setTicked(date: LocalDate, slot: PrayerSlot, ticked: Boolean) = edit { prefs ->
        val key = dayKey(date)
        val old = DayRecord.fromMask(prefs[key] ?: 0)
        val new = DayRecord(if (ticked) old.ticked + slot else old.ticked - slot)
        if (new.ticked.isEmpty()) prefs.remove(key) else prefs[key] = new.toMask()
    }

    /** Records where the rings begin, each time tracking is switched on ([TrackerLogic.startOnSwitchOn]). */
    suspend fun markStarted(today: LocalDate) = edit { prefs ->
        prefs[STARTED] = TrackerLogic.startOnSwitchOn(prefs.toTrackerData().records, today).toString()
    }

    suspend fun pause(from: LocalDate) = edit { prefs ->
        prefs[PAUSES] = PauseCodec.encode(TrackerLogic.pause(PauseCodec.decode(prefs[PAUSES]), from))
    }

    suspend fun resume(today: LocalDate) = edit { prefs ->
        prefs[PAUSES] = PauseCodec.encode(TrackerLogic.resume(PauseCodec.decode(prefs[PAUSES]), today))
    }

    /**
     * Loading a saved file adds to what is here and never removes anything: ticks are joined day by
     * day and pauses are joined range by range. A file from an old phone can therefore be loaded onto
     * a new one that already has a week of ticks without losing that week.
     */
    suspend fun merge(incoming: TrackerData): MergeResult {
        var added = 0
        var updated = 0
        edit { prefs ->
            for ((date, record) in incoming.records) {
                if (record.ticked.isEmpty()) continue
                val key = dayKey(date)
                val old = prefs[key]
                val joined = (old ?: 0) or record.toMask()
                when {
                    old == null -> added++
                    joined != old -> updated++
                }
                prefs[key] = joined
            }
            prefs[PAUSES] = PauseCodec.encode(PauseCodec.decode(prefs[PAUSES]) + incoming.pauses)
            // A file with older days moves the start back, so those days show (9.11).
            val oldest = (incoming.records.filterValues { it.ticked.isNotEmpty() }.keys + incoming.pauses.map { it.start }).minOrNull()
            val stored = prefs[STARTED]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (oldest != null && stored != null && oldest.isBefore(stored)) prefs[STARTED] = oldest.toString()
        }
        return MergeResult(added, updated)
    }

    /** Clears the whole record. The only destructive action in the app, behind one confirmation. */
    suspend fun deleteAll() {
        context.trackerStore.edit { it.clear() }
    }

    /** Refuses to write over a record from a newer version, so a downgrade cannot damage it. */
    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.trackerStore.edit { prefs ->
            val version = prefs[VERSION_KEY] ?: VERSION
            if (version > VERSION) return@edit
            prefs[VERSION_KEY] = VERSION
            block(prefs)
        }
    }

    private fun Preferences.toTrackerData(): TrackerData = TrackerData(
        records = asMap().mapNotNull { (key, value) ->
            val date = key.name.takeIf { it.startsWith(DAY_PREFIX) }
                ?.let { runCatching { LocalDate.parse(it.removePrefix(DAY_PREFIX)) }.getOrNull() }
            val mask = value as? Int
            if (date == null || mask == null) null else date to DayRecord.fromMask(mask)
        }.toMap(),
        pauses = PauseCodec.decode(this[PAUSES]),
        readOnly = (this[VERSION_KEY] ?: VERSION) > VERSION,
        startedOn = this[STARTED]?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )

    private companion object {
        const val VERSION = 1
        const val DAY_PREFIX = "d:"
        val VERSION_KEY = intPreferencesKey("tracker_version")
        val PAUSES = stringPreferencesKey("pauses")
        // A plain ISO date, not a "d:" day key, so toTrackerData never reads it as a day of ticks.
        val STARTED = stringPreferencesKey("tracker_started")
        fun dayKey(date: LocalDate) = intPreferencesKey(DAY_PREFIX + date)
    }
}
