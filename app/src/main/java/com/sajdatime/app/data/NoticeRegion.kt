package com.sajdatime.app.data

import android.content.Context
import android.telephony.TelephonyManager
import com.sajdatime.app.R
import java.util.TimeZone

/**
 * Decides whether this phone is shown the on-by-default NOTICE (counts start after the user has
 * seen it, with an equal weight "Turn this off") or the opt-in CONSENT question.
 *
 * **The default is to ask.** The notice is shown only when the phone is positively in a country
 * listed in `notice_only_countries` (today: Pakistan, which has no enacted data protection law as
 * of 4 Oct 2026) and no signal contradicts it. Why this exists and why the list is so short:
 * docs/HANDOVER.md section 10, 4 Oct 2026. The UK and EU need consent before the SDK stores its ID,
 * and the ICO statistical purposes exception does not fit Firebase's retention. Do not add a
 * country without a written source that it has no consent requirement for this.
 *
 * Signals are the SIM country, the network country, the language region and the time zone. All
 * of them can be wrong (a traveller, a foreign SIM), so any disagreement, any restricted
 * language region, an unknown country or a missing signal means "ask".
 */
object NoticeRegion {
    // UK, EEA and Switzerland: a language region here always means "ask", whatever the SIM says.
    private val restrictedLocales = setOf(
        "GB", "IE", "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU",
        "IS", "IT", "LV", "LI", "LT", "LU", "MT", "NL", "NO", "PL", "PT", "RO", "SK", "SI", "ES",
        "SE", "CH",
    )

    /** Pure, so a test can try every combination. */
    fun noticeOnly(
        simCountry: String?,
        networkCountry: String?,
        localeRegion: String?,
        timeZoneId: String,
        countries: Set<String>,
        timeZones: Set<String>,
    ): Boolean {
        val telephony = listOf(simCountry, networkCountry)
            .mapNotNull { it?.trim()?.uppercase()?.takeIf { c -> c.isNotEmpty() } }
        if (telephony.isEmpty()) return false
        if (telephony.any { it !in countries }) return false
        if (localeRegion?.trim()?.uppercase() in restrictedLocales) return false
        return timeZoneId in timeZones
    }

    fun forDevice(context: Context): Boolean = runCatching {
        val tm = context.getSystemService(TelephonyManager::class.java)
        noticeOnly(
            simCountry = tm?.simCountryIso,
            networkCountry = tm?.networkCountryIso,
            // Region only, never formatting: LocaleDisciplineTest forbids Locale.getDefault().
            localeRegion = context.resources.configuration.locales.get(0)?.country,
            timeZoneId = TimeZone.getDefault().id,
            countries = context.resources.getStringArray(R.array.notice_only_countries).toSet(),
            timeZones = context.resources.getStringArray(R.array.notice_only_timezones).toSet(),
        )
    }.getOrDefault(false)
}
