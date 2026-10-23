package com.sajdatime.app.data

import com.sajdatime.core.DayRecord
import com.sajdatime.core.PauseRange
import com.sajdatime.core.TRACKED_SLOTS
import com.sajdatime.core.TrackerLogic
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * The save file for the prayer record: the only way it reaches a new phone, because backup is off
 * for everything this app stores (data_extraction_rules.xml).
 *
 * Plain JSON with prayer names, not bitmasks, so the person it belongs to can read it:
 *
 * ```
 * {"kind":"sajdatime-prayer-tracker","version":1,"exported":"2027-02-20",
 *  "days":{"2027-02-08":["FAJR","DHUHR"]},"pauses":[{"start":"2027-02-09","end":"2027-02-12"}]}
 * ```
 *
 * Dates and ticks only: no place, no settings, nothing else. Unknown prayer names are dropped one by
 * one, as AlertCodec does, so a file from a later version loses an entry rather than failing whole.
 */
object TrackerFileCodec {
    const val KIND = "sajdatime-prayer-tracker"
    const val VERSION = 1

    /** Why a file could not be loaded. The screen shows a different sentence for each. */
    class Unreadable(val tooNew: Boolean) : Exception()

    fun fileName(exportedOn: LocalDate): String = "sajdatime-prayers-$exportedOn.json"

    fun encode(data: TrackerData, exportedOn: LocalDate): String {
        val days = JSONObject()
        data.records.toSortedMap().forEach { (date, record) ->
            if (record.ticked.isNotEmpty()) {
                days.put(date.toString(), JSONArray(TRACKED_SLOTS.filter { it in record.ticked }.map { it.name }))
            }
        }
        val pauses = JSONArray(
            TrackerLogic.normalise(data.pauses).map { p ->
                JSONObject().put("start", p.start.toString()).apply { p.end?.let { put("end", it.toString()) } }
            },
        )
        return JSONObject()
            .put("kind", KIND)
            .put("version", VERSION)
            .put("exported", exportedOn.toString())
            .put("days", days)
            .put("pauses", pauses)
            .toString(2)
    }

    /** Throws [Unreadable] for a file that is not ours, is damaged, or comes from a newer version. */
    fun decode(text: String): TrackerData {
        val root = runCatching { JSONObject(text) }.getOrNull() ?: throw Unreadable(tooNew = false)
        if (root.optString("kind") != KIND) throw Unreadable(tooNew = false)
        val version = root.optInt("version", -1)
        if (version > VERSION) throw Unreadable(tooNew = true)
        if (version < 1) throw Unreadable(tooNew = false)

        val records = mutableMapOf<LocalDate, DayRecord>()
        root.optJSONObject("days")?.let { days ->
            for (key in days.keys()) {
                val date = runCatching { LocalDate.parse(key) }.getOrNull() ?: continue
                val names = days.optJSONArray(key) ?: continue
                val ticked = (0 until names.length())
                    .mapNotNull { i -> TRACKED_SLOTS.firstOrNull { it.name == names.optString(i) } }
                    .toSet()
                if (ticked.isNotEmpty()) records[date] = DayRecord(ticked)
            }
        }
        // A pause still on when the file was saved is closed on the day it was saved. Loaded open, it
        // would quietly pause a new phone that the person is already ticking on.
        val exported = runCatching { LocalDate.parse(root.optString("exported")) }.getOrNull()
        val pauses = mutableListOf<PauseRange>()
        root.optJSONArray("pauses")?.let { list ->
            for (i in 0 until list.length()) {
                val o = list.optJSONObject(i) ?: continue
                val start = runCatching { LocalDate.parse(o.optString("start")) }.getOrNull() ?: continue
                val end = if (o.has("end")) runCatching { LocalDate.parse(o.optString("end")) }.getOrNull() ?: continue else exported
                if (end != null && !end.isBefore(start)) pauses += PauseRange(start, end)
            }
        }
        return TrackerData(records, TrackerLogic.normalise(pauses))
    }
}
