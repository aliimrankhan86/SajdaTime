package com.sajdatime.app.notify

import android.content.Context
import android.text.format.DateFormat
import com.sajdatime.app.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Locale- and 12/24-hour-aware formatting, shared by the UI and notifications. */
object TimeFormat {

    /** Clock time, honouring the device's 12/24-hour setting. */
    fun clock(context: Context, instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            .format(instant.atZone(zone))
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
    fun countdownClock(from: Instant, to: Instant): String {
        val duration = Duration.between(from, to).coerceAtLeast(Duration.ZERO)
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            duration.toHours(),
            duration.toMinutes() % 60,
            duration.seconds % 60,
        )
    }
}
