package com.sajdatime.app.data

import androidx.annotation.StringRes
import com.sajdatime.app.R
import com.sajdatime.core.Sect

/**
 * The adhan recordings that ship inside the app (1.5.0, owner decisions 17 Oct 2026), one per school,
 * offered as an alarm sound. Sources, licences and hashes: docs/licences/ADHAN.md.
 *
 *  - [ADHAN], Sunni: "Beautiful adhan.ogg", CC0, recorded by its uploader. The owner listened and
 *    approved it.
 *  - [SHIA_ADHAN], Shia: "AzaanMaahur.ogg", CC BY-SA 4.0, sung by its uploader, Saeed
 *    Hatamzadeh-Varmazyar. Checked by transcription (whisper.cpp, medium model) to be a complete Shia
 *    adhan in order: "ashhadu anna aliyyan waliyyullah", "hayya ala khayr al-amal" twice, closing
 *    "la ilaha illallah" twice. Studio clean (noise floor about -92 dB, no clipping). CC BY-SA needs the
 *    credit and a link to the licence, which About gives; the file is unmodified.
 *
 * Each school sees only its own, under the one label "Adhan". Do not add a recording without a licence
 * that allows it in an app, checked on its source page, and without the owner's approval: he listened
 * to the Sunni one; for the Shia one he delegated the choice ("use the best possible, high quality and
 * free to use adhans", 17 Oct 2026), so it was checked by transcription and measurement, and a Shia
 * listener should hear it before release. Sources already rejected are in HANDOVER §11.
 *
 * A choice is stored as an ordinary sound address, `android.resource://<package>/raw/<name>`, in
 * the same setting as a phone sound, so the alarm channel, Do Not Disturb, "Play on silent", volume
 * and dismissing all work exactly as they do for a phone sound: the channel id is a hash of the
 * address (Notifications.alarmChannelId), so a new choice is a new channel, the existing pattern.
 * By name, not by resource id, because ids move between builds and the address is persisted. The
 * names are kept from resource shrinking by res/raw/keep.xml, since only this string refers to them.
 */
enum class BuiltInAdhan(val resourceName: String, @StringRes val labelRes: Int, val sect: Sect) {
    ADHAN("adhan", R.string.adhan_builtin, Sect.SUNNI),
    SHIA_ADHAN("adhan_shia", R.string.adhan_builtin, Sect.SHIA),
    ;

    fun uri(packageName: String): String = "android.resource://$packageName/raw/$resourceName"

    companion object {
        /** The built-in adhan [uri] points at, or null for a phone sound or the default. */
        fun of(uri: String): BuiltInAdhan? =
            entries.firstOrNull { uri.startsWith("android.resource://") && uri.endsWith("/raw/${it.resourceName}") }

        /** The built-in adhans a user of [sect] is offered. */
        fun forSect(sect: Sect): List<BuiltInAdhan> = entries.filter { it.sect == sect }
    }
}

/**
 * The alarm sound after a change of school. A built-in adhan chosen under the old school becomes the
 * new school's adhan: the person asked for "the adhan", not for one school's recording. If the new
 * school had none it would fall back to the phone sound chosen before it, or the default. Anything else
 * is left exactly as it was. Pure, so it is tested.
 */
internal fun alarmSoundForSect(sect: Sect, current: String, phoneSound: String, packageName: String): String {
    val chosen = BuiltInAdhan.of(current) ?: return current
    if (chosen.sect == sect) return current
    return BuiltInAdhan.forSect(sect).firstOrNull()?.uri(packageName) ?: phoneSound
}
