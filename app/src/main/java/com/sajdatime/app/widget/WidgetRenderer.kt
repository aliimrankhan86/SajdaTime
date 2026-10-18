package com.sajdatime.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.text.layoutDirection
import com.sajdatime.app.MainActivity
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.widget.WidgetState.RamadanWord
import com.sajdatime.app.widget.WidgetState.Segment
import com.sajdatime.core.AppLocale
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.label
import java.time.Instant
import java.time.format.DateTimeFormatter

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
 *     resolved by the launcher at each draw. Only resource ids are switched: the row background, the
 *     glyph (drawn white, tinted by its ImageView) and the day's bars.
 *
 * **Two layouts per widget since 18 Oct 2026 (the hero countdown design).** On Android 12 and later the
 * launcher is handed both and picks by the widget's size without waking the app ([NEXT_SIZES],
 * [todaySizes]); below 12 the first, smaller one is the only one. The 1.4.0 widgets had one fixed
 * layout each, which left the content stranded in a corner of the larger widget One UI gives.
 *
 * The countdown is a `Chronometer`, which the launcher's process ticks, so it costs this app no wake
 * ups. Its digits are formatted in the PHONE's language, not the app's: nothing in a RemoteViews can
 * change that, and it is recorded as a known limit, not hidden.
 */
internal object WidgetRenderer {

    /**
     * Next prayer: the small layout from the widget's own minimum, the wide one (countdown beside the
     * day) once it is both wide and tall enough for six rows beside the countdown. The launcher uses
     * the largest entry that fits inside the widget, and the smallest when none does.
     */
    private val NEXT_SIZES = listOf(SizeF(180f, 110f) to R.layout.widget_next_prayer, SizeF(270f, 150f) to R.layout.widget_next_prayer_wide)

    /**
     * Today: the compact list from its minimum, the large card (date, countdown, the day) from 280dp
     * tall, which is what the header, the card and six rows need at the normal text size (rendered).
     */
    private fun todaySizes(context: Context): List<Pair<SizeF, Int>> {
        // Urdu and Arabic script sits taller than Latin at the same size: rendered on 18 Oct 2026, the
        // large card at 280dp held four of their six rows, and all six from 330dp.
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return listOf(SizeF(250f, 180f) to R.layout.widget_today, SizeF(250f, if (tall) 330f else 280f) to R.layout.widget_today_large)
    }

    private val TALL_SCRIPT_LANGUAGES = setOf("ur", "ar")

    /**
     * From this text size the second layout of each widget stops showing the whole day (rendered at 1.3
     * and 2.0 on 18 Oct 2026): the wide Next layout's times no longer fit beside the countdown, and the
     * large Today card leaves room for three of the six rows. So each widget keeps its first layout at
     * every size: the Next widget its countdown, the Today widget all six times. The font
     * scale is read at each redraw, so a change of it reaches the widget at the next one (the next prayer,
     * midnight, or opening the app), not at once. Not seen on a device.
     */
    private const val LARGE_TEXT = 1.3f

    fun nextPrayer(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        return responsive(forText(context, NEXT_SIZES)) { layout -> nextViews(context, state, now, layout) }
    }

    fun today(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        return responsive(forText(context, todaySizes(context))) { layout -> todayViews(context, state, now, layout) }
    }

    private fun forText(context: Context, sizes: List<Pair<SizeF, Int>>) =
        if (context.resources.configuration.fontScale >= LARGE_TEXT) sizes.take(1) else sizes

    private fun responsive(sizes: List<Pair<SizeF, Int>>, build: (Int) -> RemoteViews): RemoteViews =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sizes.size > 1) {
            RemoteViews(sizes.associate { (size, layout) -> size to build(layout) })
        } else {
            build(sizes.first().second)
        }

    private fun nextViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int): RemoteViews {
        val views = RemoteViews(context.packageName, layout)
        val wide = layout == R.layout.widget_next_prayer_wide
        applyDirection(context, views)
        applyTap(context, views)

        val next = state.next
        if (!state.hasLocation || next == null) {
            // The one message, in the small line, allowed to wrap. Everything else is hidden.
            views.setTextViewText(R.id.widget_sublabel, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_sublabel, View.VISIBLE)
            views.setInt(R.id.widget_sublabel, "setMaxLines", NO_LOCATION_LINES)
            // A sentence the person has to read and act on, so it is larger than the context line it borrows.
            views.setTextViewTextSize(R.id.widget_sublabel, TypedValue.COMPLEX_UNIT_SP, MESSAGE_SP)
            for (id in HERO_IDS) views.setViewVisibility(id, View.GONE)
            if (wide) {
                views.setViewVisibility(R.id.widget_divider, View.GONE)
                for (slot in PrayerSlot.entries) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            }
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }

        fillHero(context, views, state, next, now, if (wide) WIDE_COUNTDOWN_DP else SMALL_COUNTDOWN_DP, place(context, state))
        if (wide) {
            views.setViewVisibility(R.id.widget_divider, View.VISIBLE)
            fillRows(context, views, state, labels = false)
        }

        views.setContentDescription(R.id.widget_root, withPlace(context, state, nextSentence(context, next, now)))
        return views
    }

    private fun todayViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int): RemoteViews {
        val views = RemoteViews(context.packageName, layout)
        val large = layout == R.layout.widget_today_large
        applyDirection(context, views)
        applyTap(context, views)
        if (large) fillHeader(context, views, state)

        val today = state.today
        val next = state.next
        if (!state.hasLocation || today == null || next == null) {
            // The rows (and the countdown card) are hidden, which leaves an honest card with one
            // message in it. The large widget keeps its header: the date needs no location.
            for (slot in PrayerSlot.entries) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            if (large) views.setViewVisibility(R.id.widget_hero, View.GONE)
            views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_message, View.VISIBLE)
            views.setViewVisibility(R.id.widget_footer, View.GONE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }
        views.setViewVisibility(R.id.widget_message, View.GONE)
        if (large) {
            views.setViewVisibility(R.id.widget_hero, View.VISIBLE)
            fillHero(context, views, state, next, now, LARGE_COUNTDOWN_DP, place = null)
        }

        val spoken = fillRows(context, views, state, labels = true)

        // The compact list has no header, so its footer names the town, joined with the approximate
        // marker on a projected polar day. The large widget names the town in its header instead.
        val footer = listOfNotNull(
            if (large) null else place(context, state)?.bidiIsolated(),
            if (today.approximated) context.getString(R.string.notif_approximate) else null,
        )
        if (footer.isNotEmpty()) {
            views.setTextViewText(R.id.widget_footer, footer.reduce { a, b -> context.getString(R.string.settings_value_pair, a, b) })
            views.setViewVisibility(R.id.widget_footer, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_footer, View.GONE)
        }

        // One sentence for the whole widget: which prayer is next and in how long, then the six rows.
        views.setContentDescription(
            R.id.widget_root,
            withPlace(context, state, context.getString(R.string.widget_a11y_today, nextSentence(context, next, now) + ". " + spoken.joinToString(". "))),
        )
        return views
    }

    // --- the parts ------------------------------------------------------------------------

    /**
     * The countdown block, shared by both Next layouts and the large Today card: the next prayer's
     * glyph, name and time, one small line of context, the countdown (or the time in its place) and
     * the day's five bars.
     */
    private fun fillHero(
        context: Context,
        views: RemoteViews,
        state: WidgetState,
        next: NextPrayer,
        now: Instant,
        countdownDp: Float,
        place: String?,
    ) {
        val time = TimeFormat.clock(context, next.at)
        views.setImageViewResource(R.id.widget_glyph, glyph(next.slot))
        views.setTextViewText(R.id.widget_name, next.slot.label(context))
        views.setTextViewText(R.id.widget_time, time)
        views.setViewVisibility(R.id.widget_glyph, View.VISIBLE)
        views.setViewVisibility(R.id.widget_name, View.VISIBLE)

        // One small line of context: the town the times are for (not on the large Today card, whose
        // header names it), "Tomorrow", then either the approximate marker or the Ramadan word, joined
        // when more than one applies (after Isha in Ramadan, the usual case). A projected polar time is
        // marked before the Ramadan word: a guess presented as a measurement is the worse mistake, and
        // the word for Fajr or Maghrib is a convenience. The town is isolated: it comes from the
        // geocoder and stays Latin in an Urdu or Arabic line (core/Bidi.kt).
        val parts = listOfNotNull(
            place?.bidiIsolated(),
            if (next.isTomorrow) context.getString(R.string.widget_tomorrow) else null,
            if (state.nextIsApproximate) {
                context.getString(R.string.notif_approximate)
            } else {
                state.ramadanWordFor(next.slot, state.ramadanOnNextDay)?.let { wordText(context, it) }
            },
        )
        views.setInt(R.id.widget_sublabel, "setMaxLines", 1)
        views.setTextViewTextSize(R.id.widget_sublabel, TypedValue.COMPLEX_UNIT_SP, CONTEXT_SP)
        if (parts.isNotEmpty()) {
            views.setTextViewText(R.id.widget_sublabel, parts.reduce { a, b -> context.getString(R.string.settings_value_pair, a, b) })
            views.setViewVisibility(R.id.widget_sublabel, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_sublabel, View.GONE)
        }

        // dp, not sp: see the note in the layouts. Both the countdown and the time that stands in for it.
        views.setTextViewTextSize(R.id.widget_countdown, TypedValue.COMPLEX_UNIT_DIP, countdownDp)
        views.setTextViewTextSize(R.id.widget_hero_time, TypedValue.COMPLEX_UNIT_DIP, countdownDp)
        if (state.countdownAllowed) {
            applyCountdown(views, R.id.widget_countdown, next.at, now)
            views.setViewVisibility(R.id.widget_hero_time, View.GONE)
            views.setViewVisibility(R.id.widget_time, View.VISIBLE)
        } else {
            // Without exact alarms the redraw can be late and a countdown left running past zero would
            // be wrong in plain sight, so the time itself takes the large place and is not repeated.
            views.setViewVisibility(R.id.widget_countdown, View.GONE)
            views.setTextViewText(R.id.widget_hero_time, time)
            views.setViewVisibility(R.id.widget_hero_time, View.VISIBLE)
            views.setViewVisibility(R.id.widget_time, View.GONE)
        }

        views.setViewVisibility(R.id.widget_progress, if (state.progress.size == SEGMENTS.size) View.VISIBLE else View.GONE)
        state.progress.zip(SEGMENTS).forEach { (segment, id) ->
            views.setInt(
                id,
                "setBackgroundResource",
                when (segment) {
                    Segment.DONE -> R.drawable.widget_seg_done
                    Segment.NEXT -> R.drawable.widget_seg_next
                    Segment.TODO -> R.drawable.widget_seg_todo
                },
            )
        }
    }

    /** The six rows. Returns one spoken phrase per row for the widget's single description. */
    private fun fillRows(context: Context, views: RemoteViews, state: WidgetState, labels: Boolean): List<String> {
        val today = state.today ?: return emptyList()
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
            views.setImageViewResource(ids.glyph, glyph(slot))
            views.setTextViewText(ids.name, if (highlighted) bold(name) else name)
            views.setTextViewText(ids.time, if (highlighted) bold(time) else time)

            // "Next", a Ramadan word, both, or nothing. Written every time, so a row that stops being
            // next or stops carrying a word is cleared. Empty on the wide Next widget, where the label
            // column is only the space that holds the time at the far end.
            val marker = if (highlighted) context.getString(R.string.home_next_marker) else null
            val label = when {
                !labels -> ""
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
        return spoken
    }

    /**
     * The large Today widget's header: the date in full and the Hijri date under it. Keyed on the
     * calendar day in the state, which the redraw alarm already moves on at midnight.
     */
    private fun fillHeader(context: Context, views: RemoteViews, state: WidgetState) {
        val date = state.date
        if (date == null) {
            views.setViewVisibility(R.id.widget_header, View.GONE)
            return
        }
        views.setViewVisibility(R.id.widget_header, View.VISIBLE)
        val place = if (state.hasLocation) place(context, state) else null
        if (place != null) {
            views.setTextViewText(R.id.widget_place, place)
            views.setViewVisibility(R.id.widget_place, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_place, View.GONE)
        }
        val locale = AppLocale.of(context)
        // Asked for, not written out, as in TimeFormat.date: field order differs between languages.
        val pattern = DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM")
        views.setTextViewText(R.id.widget_date, DateTimeFormatter.ofPattern(pattern, locale).format(date))

        // The same construction as the Times screen's header (HomeScreen.hijriToday): month names from
        // the string array, and only the month isolated, which is what keeps "7 Jumada al-Ula 1448" in
        // order for a right to left reader (BidiTest).
        val hijri = state.hijri
        val month = hijri?.let { context.resources.getStringArray(R.array.hijri_months).getOrNull(it.month - 1) }
        if (hijri != null && month != null) {
            views.setTextViewText(R.id.widget_hijri, context.getString(R.string.hijri_date_format, hijri.day, month.bidiIsolated(), hijri.year))
            views.setViewVisibility(R.id.widget_hijri, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_hijri, View.GONE)
        }
    }

    // --- shared helpers -------------------------------------------------------------------

    /** How many lines the "open SajdaTime" message may take before it is cut. Normal text takes one. */
    private const val NO_LOCATION_LINES = 4

    /** The context line is 12sp in the layouts; the no location message uses 14sp. Both are written every time. */
    private const val CONTEXT_SP = 12f
    private const val MESSAGE_SP = 14f

    /**
     * The countdown sizes, in dp, judged from renders at each layout's smallest size. At the small Next
     * widget's 110dp minimum with a context line showing ("Iftar"), the day's bars are the part that is
     * clipped; the countdown never is. Taller launchers (One UI gives about 158dp) show everything.
     */
    private const val SMALL_COUNTDOWN_DP = 36f
    private const val WIDE_COUNTDOWN_DP = 34f
    private const val LARGE_COUNTDOWN_DP = 38f

    /** Everything the countdown block owns, hidden together when there is nothing to count down to. */
    private val HERO_IDS = listOf(
        R.id.widget_glyph, R.id.widget_name, R.id.widget_time, R.id.widget_countdown, R.id.widget_hero_time, R.id.widget_progress,
    )

    private val SEGMENTS = listOf(R.id.widget_seg_1, R.id.widget_seg_2, R.id.widget_seg_3, R.id.widget_seg_4, R.id.widget_seg_5)

    /** The view ids of one row. Both Today layouts and the wide Next layout use the same ones. */
    private class RowIds(val row: Int, val glyph: Int, val name: Int, val label: Int, val time: Int)

    private val ROWS = mapOf(
        PrayerSlot.FAJR to RowIds(R.id.widget_row_fajr, R.id.widget_glyph_fajr, R.id.widget_name_fajr, R.id.widget_label_fajr, R.id.widget_time_fajr),
        PrayerSlot.SUNRISE to RowIds(R.id.widget_row_sunrise, R.id.widget_glyph_sunrise, R.id.widget_name_sunrise, R.id.widget_label_sunrise, R.id.widget_time_sunrise),
        PrayerSlot.DHUHR to RowIds(R.id.widget_row_dhuhr, R.id.widget_glyph_dhuhr, R.id.widget_name_dhuhr, R.id.widget_label_dhuhr, R.id.widget_time_dhuhr),
        PrayerSlot.ASR to RowIds(R.id.widget_row_asr, R.id.widget_glyph_asr, R.id.widget_name_asr, R.id.widget_label_asr, R.id.widget_time_asr),
        PrayerSlot.MAGHRIB to RowIds(R.id.widget_row_maghrib, R.id.widget_glyph_maghrib, R.id.widget_name_maghrib, R.id.widget_label_maghrib, R.id.widget_time_maghrib),
        PrayerSlot.ISHA to RowIds(R.id.widget_row_isha, R.id.widget_glyph_isha, R.id.widget_name_isha, R.id.widget_label_isha, R.id.widget_time_isha),
    )

    /** Where the sun is at each time. The meanings are in docs/plans/2026-10-widget-redesign/RESEARCH.md. */
    @DrawableRes
    private fun glyph(slot: PrayerSlot): Int = when (slot) {
        PrayerSlot.FAJR -> R.drawable.widget_glyph_fajr
        PrayerSlot.SUNRISE -> R.drawable.widget_glyph_sunrise
        PrayerSlot.DHUHR -> R.drawable.widget_glyph_dhuhr
        PrayerSlot.ASR -> R.drawable.widget_glyph_asr
        PrayerSlot.MAGHRIB -> R.drawable.widget_glyph_maghrib
        PrayerSlot.ISHA -> R.drawable.widget_glyph_isha
    }

    /**
     * The town the times are for, as the person would say it: the Makkah fallback in the app's own
     * language (AppSettings.placeName), otherwise the town part of the saved label. Null when there is
     * nothing to show, so no widget ever shows an empty dot.
     */
    private fun place(context: Context, state: WidgetState): String? =
        if (state.usingDefaultLocation) context.getString(R.string.location_makkah) else state.town.ifBlank { null }

    /** The spoken description, led by the town when there is one, as the Times screen's header leads. */
    private fun withPlace(context: Context, state: WidgetState, sentence: String): String =
        place(context, state)?.let { context.getString(R.string.settings_value_pair, it.bidiIsolated(), sentence) } ?: sentence

    private fun nextSentence(context: Context, next: NextPrayer, now: Instant): String = context.getString(
        if (next.isTomorrow) R.string.widget_a11y_next_tomorrow else R.string.widget_a11y_next,
        next.slot.label(context),
        TimeFormat.clock(context, next.at),
        TimeFormat.remaining(context, now, next.at),
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
     * Counts down to [at]. The base is on the elapsed realtime clock, which the launcher's Chronometer
     * uses, so it is the distance to [at] from now added to that clock's reading. Only called when the
     * refresh alarm is exact (see [fillHero]).
     */
    private fun applyCountdown(views: RemoteViews, id: Int, at: Instant, now: Instant) {
        views.setViewVisibility(id, View.VISIBLE)
        views.setChronometerCountDown(id, true)
        views.setChronometer(id, SystemClock.elapsedRealtime() + (at.toEpochMilli() - now.toEpochMilli()), null, true)
    }

    /** Distinct from the scheduler's open intent (request code 0), so neither replaces the other. */
    private const val TAP_REQUEST_CODE = 1
}
