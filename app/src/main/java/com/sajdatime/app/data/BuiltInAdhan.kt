package com.sajdatime.app.data

import androidx.annotation.StringRes
import com.sajdatime.app.R
import com.sajdatime.core.Sect

/**
 * The adhan recording that ships inside the app (1.5.0, owner decision 17 Oct 2026), offered as an
 * alarm sound to Sunni users only. Source, licence and hash: docs/licences/ADHAN.md.
 *
 * One recording, not two: the owner asked for one that is safe to use, plus any sound from the
 * phone for everything else. This one is CC0 (nothing to attribute, nothing that can be withdrawn),
 * recorded by its uploader, unchanged since 2022. The CC BY-SA voice that was built alongside it was
 * dropped: its file was replaced in 2025 and its page credits the uploader while naming a reciter.
 * An enum, so a commissioned Shia or Fajr recording is one more entry.
 *
 * It is a Sunni adhan. A Shia user is shown a notice in the sound picker instead and keeps
 * choosing a sound from the phone, until a Shia recording with a written licence exists. Nothing
 * free and clean was found (HANDOVER §11, rejected sources). Do not add a recording here without
 * a licence that allows it in an app, checked on its source page.
 *
 * A choice is stored as an ordinary sound address, `android.resource://<package>/raw/<name>`, in
 * the same setting as a phone sound, so the alarm channel, Do Not Disturb, volume and dismissing
 * all work exactly as they do for a phone sound: the channel id is a hash of the
 * address (Notifications.alarmChannelId), so a new choice is a new channel, the existing pattern.
 * By name, not by resource id, because ids move between builds and the address is persisted. The
 * name is kept from resource shrinking by res/raw/keep.xml, since only this string refers to them.
 */
enum class BuiltInAdhan(val resourceName: String, @StringRes val labelRes: Int) {
    ADHAN("adhan", R.string.adhan_builtin),
    ;

    fun uri(packageName: String): String = "android.resource://$packageName/raw/$resourceName"

    companion object {
        /** The built-in adhan [uri] points at, or null for a phone sound or the default. */
        fun of(uri: String): BuiltInAdhan? =
            entries.firstOrNull { uri.startsWith("android.resource://") && uri.endsWith("/raw/${it.resourceName}") }

        fun offeredTo(sect: Sect): Boolean = sect == Sect.SUNNI
    }
}

/**
 * The alarm sound after a change of school: a built-in adhan does not survive a move to a school
 * it is not offered to, and falls back to the phone sound chosen before it, or the default.
 * Anything else is left exactly as it was. Pure, so it is tested.
 */
internal fun alarmSoundForSect(sect: Sect, current: String, phoneSound: String): String =
    if (BuiltInAdhan.of(current) != null && !BuiltInAdhan.offeredTo(sect)) phoneSound else current
