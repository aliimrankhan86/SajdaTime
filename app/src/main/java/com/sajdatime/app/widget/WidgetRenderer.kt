package com.sajdatime.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.core.text.layoutDirection
import com.sajdatime.app.MainActivity
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.widget.WidgetState.RamadanWord
import com.sajdatime.core.AppLocale
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.label
import java.time.Instant

/**
 * Turns a [WidgetState] into the views a launcher draws. All the decisions are already in the state;
 * this only writes them down.
 *
 * Three rules that are easy to break and cannot be seen on a developer's phone:
 *
 *  1. **Every word is set here, from a context pinned to the app's language.** The layouts contain no
 *     text, because a launcher inflates them and would resolve `@string/` in the phone's language.
 *  2. **Every property that can change is written on every render, in both directions.** A launcher
 *     may re-apply a new set of actions to the views it already has instead of inflating again, so a
 *     value set last time (a hidden countdown, a highlighted row, a four line message) survives
 *     unless it is explicitly put back.
 *  3. **Colours are not set here.** A colour chosen while drawing is baked in at that moment and is
 *     wrong after the phone changes theme until the next refresh. The layouts' own colours are
 *     resolved by the launcher at each draw. Only the row background, a resource id, is switched.
 *
 * The countdown is a `Chronometer`, which the launcher's process ticks, so it costs this app no wake
 * ups. Its digits are formatted in the PHONE's language, not the app's: nothing in a RemoteViews can
 * change that, and it is recorded as a known limit, not hidden.
 */
internal object WidgetRenderer {

    fun nextPrayer(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        val views = RemoteViews(context.packageName, R.layout.widget_next_prayer)
        applyDirection(context, views)
        applyTap(context, views)

        val next = state.next
        if (!state.hasLocation || next == null) {
            // The one message, in the small line, allowed to wrap. Everything else is hidden.
            views.setTextViewText(R.id.widget_sublabel, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_sublabel, View.VISIBLE)
            views.setInt(R.id.widget_sublabel, "setMaxLines", NO_LOCATION_LINES)
            for (id in listOf(R.id.widget_name, R.id.widget_time, R.id.widget_countdown)) {
                views.setViewVisibility(id, View.GONE)
            }
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }

        val name = next.slot.label(context)
        val time = TimeFormat.clock(context, next.at)

        views.setTextViewText(R.id.widget_name, name)
        views.setTextViewText(R.id.widget_time, time)
        views.setViewVisibility(R.id.widget_name, View.VISIBLE)
        views.setViewVisibility(R.id.widget_time, View.VISIBLE)

        // One small line of context under the time: "Tomorrow", then either the approximate marker or
        // the Ramadan word, joined when both apply (after Isha in Ramadan, the usual case). A projected
        // polar time is marked before the Ramadan word: a guess presented as a measurement is the worse
        // mistake, and the word for Fajr or Maghrib is a convenience.
        val parts = listOfNotNull(
            if (next.isTomorrow) context.getString(R.string.widget_tomorrow) else null,
            if (state.nextIsApproximate) {
                context.getString(R.string.notif_approximate)
            } else {
                state.ramadanWordFor(next.slot, state.ramadanOnNextDay)?.let { wordText(context, it) }
            },
        )
        views.setInt(R.id.widget_sublabel, "setMaxLines", 1)
        if (parts.isNotEmpty()) {
            views.setTextViewText(R.id.widget_sublabel, parts.reduce { a, b -> context.getString(R.string.settings_value_pair, a, b) })
            views.setViewVisibility(R.id.widget_sublabel, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_sublabel, View.GONE)
        }

        applyCountdown(views, R.id.widget_countdown, next.at, now, state.countdownAllowed)

        views.setContentDescription(
            R.id.widget_root,
            context.getString(
                if (next.isTomorrow) R.string.widget_a11y_next_tomorrow else R.string.widget_a11y_next,
                name,
                time,
                TimeFormat.remaining(context, now, next.at),
            ),
        )
        return views
    }

    fun today(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        val views = RemoteViews(context.packageName, R.layout.widget_today)
        applyDirection(context, views)
        applyTap(context, views)

        val today = state.today
        val next = state.next
        if (!state.hasLocation || today == null || next == null) {
            // Same message as the Next widget, in the first row's name slot so it sits at the top.
            // The other rows are hidden, which leaves an honest empty card.
            for (slot in PrayerSlot.entries) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_message, View.VISIBLE)
            views.setViewVisibility(R.id.widget_footer, View.GONE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }
        views.setViewVisibility(R.id.widget_message, View.GONE)

        val spoken = mutableListOf<String>()
        for (slot in PrayerSlot.entries) {
            val ids = ROWS.getValue(slot)
            val name = slot.label(context)
            val time = TimeFormat.clock(context, today[slot])
            val highlighted = state.isHighlighted(slot)
            val word = state.ramadanWordFor(slot, state.ramadanToday)?.let { wordText(context, it) }

            views.setViewVisibility(ids.row, View.VISIBLE)
            views.setInt(
                ids.row,
                "setBackgroundResource",
                if (highlighted) R.drawable.widget_row_highlight else R.drawable.widget_row_plain,
            )
            views.setTextViewText(ids.name, if (highlighted) bold(name) else name)
            views.setTextViewText(ids.time, if (highlighted) bold(time) else time)

            // "Next", a Ramadan word, both, or nothing. Written every time, so a row that stops being
            // next or stops carrying a word is cleared.
            val marker = if (highlighted) context.getString(R.string.home_next_marker) else null
            val label = when {
                marker != null && word != null -> context.getString(R.string.settings_value_pair, marker, word)
                else -> marker ?: word ?: ""
            }
            views.setTextViewText(ids.label, label)

            spoken += context.getString(
                R.string.widget_a11y_row,
                if (word != null) context.getString(R.string.settings_value_pair, name, word) else name,
                time,
            )
        }

        if (today.approximated) {
            views.setTextViewText(R.id.widget_footer, context.getString(R.string.notif_approximate))
            views.setViewVisibility(R.id.widget_footer, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_footer, View.GONE)
        }

        // One sentence for the whole widget: which prayer is next and in how long, then the six rows.
        val nextSentence = context.getString(
            if (next.isTomorrow) R.string.widget_a11y_next_tomorrow else R.string.widget_a11y_next,
            next.slot.label(context),
            TimeFormat.clock(context, next.at),
            TimeFormat.remaining(context, now, next.at),
        )
        views.setContentDescription(
            R.id.widget_root,
            context.getString(R.string.widget_a11y_today, nextSentence + ". " + spoken.joinToString(". ")),
        )
        return views
    }

    // --- shared helpers -------------------------------------------------------------------

    /** How many lines the "open SajdaTime" message may take before it is cut. Normal text takes one. */
    private const val NO_LOCATION_LINES = 4

    /** The four view ids of one row of the Today widget. */
    private class RowIds(val row: Int, val name: Int, val label: Int, val time: Int)

    private val ROWS = mapOf(
        PrayerSlot.FAJR to RowIds(R.id.widget_row_fajr, R.id.widget_name_fajr, R.id.widget_label_fajr, R.id.widget_time_fajr),
        PrayerSlot.SUNRISE to RowIds(R.id.widget_row_sunrise, R.id.widget_name_sunrise, R.id.widget_label_sunrise, R.id.widget_time_sunrise),
        PrayerSlot.DHUHR to RowIds(R.id.widget_row_dhuhr, R.id.widget_name_dhuhr, R.id.widget_label_dhuhr, R.id.widget_time_dhuhr),
        PrayerSlot.ASR to RowIds(R.id.widget_row_asr, R.id.widget_name_asr, R.id.widget_label_asr, R.id.widget_time_asr),
        PrayerSlot.MAGHRIB to RowIds(R.id.widget_row_maghrib, R.id.widget_name_maghrib, R.id.widget_label_maghrib, R.id.widget_time_maghrib),
        PrayerSlot.ISHA to RowIds(R.id.widget_row_isha, R.id.widget_name_isha, R.id.widget_label_isha, R.id.widget_time_isha),
    )

    private fun wordText(context: Context, word: RamadanWord): String = context.getString(
        when (word) {
            RamadanWord.SUHOOR_ENDS -> R.string.widget_suhoor_ends
            RamadanWord.IFTAR -> R.string.widget_iftar
        },
    )

    /** Bold for the highlighted row, so "which prayer is next" is not carried by a background tint alone. */
    private fun bold(text: String): CharSequence =
        SpannableString(text).apply { setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }

    /**
     * The launcher inflates the layout with ITS configuration, so the layout direction has to be set
     * from the app's own language, and set both ways. Without it an Urdu widget on an English phone
     * would draw left to right with right to left text in it.
     */
    private fun applyDirection(context: Context, views: RemoteViews) {
        val rtl = AppLocale.of(context).layoutDirection == View.LAYOUT_DIRECTION_RTL
        views.setInt(
            R.id.widget_root,
            "setLayoutDirection",
            if (rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR,
        )
    }

    /** A tap anywhere on either widget opens the app. FLAG_IMMUTABLE: nothing needs to fill it in. */
    private fun applyTap(context: Context, views: RemoteViews) {
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        views.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getActivity(
                context,
                TAP_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

    /**
     * Counts down to [at] when allowed, hidden otherwise. The base is on the elapsed realtime clock,
     * which the launcher's Chronometer uses, so it is the distance to [at] from now added to that
     * clock's reading. Hidden without exact alarms: then the refresh alarm can be late, and a countdown
     * left running past zero would be wrong in plain sight.
     */
    private fun applyCountdown(views: RemoteViews, @IdRes id: Int, at: Instant, now: Instant, allowed: Boolean) {
        if (!allowed) {
            views.setViewVisibility(id, View.GONE)
            return
        }
        views.setViewVisibility(id, View.VISIBLE)
        views.setChronometerCountDown(id, true)
        views.setChronometer(id, SystemClock.elapsedRealtime() + (at.toEpochMilli() - now.toEpochMilli()), null, true)
    }

    /** Distinct from the scheduler's open intent (request code 0), so neither replaces the other. */
    private const val TAP_REQUEST_CODE = 1
}
