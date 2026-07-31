package com.sajdatime.app.notify

import android.content.Context
import android.text.format.DateFormat
import com.sajdatime.app.R
import com.sajdatime.core.AppLocale
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Locale- and 12/24-hour-aware formatting, shared by the UI and notifications.
 *
 * The locale is [AppLocale], the language of the app's own strings, and not the device
 * language. 12/24-hour, by contrast, *is* a device setting and is read from the device.
 * See AppLocale.kt.
 */
object TimeFormat {

    /**
     * Clock time, honouring the device's 12/24-hour setting.
     *
     * The `applicationContext` here is load-bearing, not tidying. When the user has never
     * touched the 12/24 toggle, `is24HourFormat` falls back to the convention of the
     * locale in whichever context it is handed — and every context in the UI is now pinned
     * to en-GB, which is a 24-hour locale. Asking the pinned context turned every time in
     * the app into "13:10" for users who had always seen "1:10 pm". The application
     * context is deliberately left unpinned so this one question can still be put to the
     * device.
     */
    fun clock(context: Context, instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val pattern = if (DateFormat.is24HourFormat(context.applicationContext)) "HH:mm" else "h:mm a"
        return DateTimeFormatter.ofPattern(pattern, AppLocale.of(context))
            .format(instant.atZone(zone))
    }

    /**
     * Calendar date without the year, e.g. "Thu 31 Jul".
     *
     * The pattern is asked for rather than written out. Field order is not universal —
     * en-GB wants "Thu 31 Jul" and en-US wants "Thu, Jul 31" — and a hardcoded pattern
     * would be wrong for one of them the moment a translation ships. The skeleton
     * "EEEdMMM" names the fields wanted and lets the platform order and punctuate them
     * for the locale.
     *
     * The locale is [AppLocale], not the device's, for the reason set out in AppLocale.kt:
     * formatting in a language the app is not written in is what turned a Hijri date into
     * "١٤٤٨ Safar ١٧" on an Arabic phone.
     *
     * No year, deliberately. The line answers "what is today", and the reader's own
     * calendar already says which year it is; the Hijri year beside it is the one they are
     * actually unsure of.
     */
    fun date(context: Context, date: LocalDate): String {
        val locale = AppLocale.of(context)
        val pattern = DateFormat.getBestDateTimePattern(locale, "EEEdMMM")
        return DateTimeFormatter.ofPattern(pattern, locale).format(date)
    }

    /**
     * Human countdown, e.g. "2h 14m" or "48s". Kept short so it fits a notification line
     * and the hero card without wrapping.
     */
    fun remaining(context: Context, from: Instant, to: Instant): String {
        val duration = Duration.between(from, to).coerceAtLeast(Duration.ZERO)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return when {
            hours > 0 -> context.getString(R.string.duration_h_m, hours, minutes)
            minutes > 0 -> context.getString(R.string.duration_m, minutes)
            else -> context.getString(R.string.duration_s, seconds)
        }
    }

    /** Full precision countdown for the hero card: "01:23:45". */
    fun countdownClock(context: Context, from: Instant, to: Instant): String {
        val duration = Duration.between(from, to).coerceAtLeast(Duration.ZERO)
        return String.format(
            AppLocale.of(context),
            "%02d:%02d:%02d",
            duration.toHours(),
            duration.toMinutes() % 60,
            duration.seconds % 60,
        )
    }
}
