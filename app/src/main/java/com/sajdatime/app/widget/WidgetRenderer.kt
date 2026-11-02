package com.sajdatime.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
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
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.widget.WidgetState.RamadanWord
import com.sajdatime.app.widget.WidgetState.Segment
import com.sajdatime.core.AppLocale
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.SkyPeriod
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.label
import java.time.Instant
import kotlin.math.roundToInt

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
 *  3. **Under the app's Theme "System", colours are not set here.** A colour chosen while drawing is
 *     baked in at that moment and is wrong after the phone changes theme until the next refresh. The
 *     layouts' own colours are resolved by the launcher at each draw. Only resource ids are switched:
 *     the row background, the glyph (drawn white, tinted by its ImageView) and the day's bars.
 *     **Under Light or Dark** (owner's request, 19 Oct 2026) the widget must not follow the phone, so
 *     every colour is pinned from the fixed set ([PAINTS], [paint]) on a second layout id, the
 *     `_fixed` layout, which is the same layout inside a frame. Why a second id: see that file.
 *
 * **Two layouts per widget since 18 Oct 2026 (the hero countdown design).** On Android 12 and later the
 * launcher is handed both and picks by the widget's size without waking the app ([NEXT_SIZES],
 * [todaySizes]); below 12 the first, smaller one is the only one. The 1.4.0 widgets had one fixed
 * layout each, which left the content stranded in a corner of the larger widget One UI gives.
 *
 * **Three widgets since step 8 (1.6.0).** The Today strip (`strip`, `widget_strip.xml`) is five columns in one
 * row, built four ways from the one layout ([STRIP_SIZES]); the Next prayer widget sits on the sky of the
 * current period ([skyDrawable]) instead of the paper body.
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
        // large card at 280dp held four of their six rows, and all six from 330dp. Bengali joined them on
        // 30 Oct 2026: its vowel signs sit above and below the line, so its rows are as tall.
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return listOf(SizeF(250f, 180f) to R.layout.widget_today, SizeF(250f, if (tall) 330f else 280f) to R.layout.widget_today_large)
    }

    private val TALL_SCRIPT_LANGUAGES = setOf("ur", "ar", "bn")

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

    /**
     * Today strip, 4 cells by 1: thin (no glyph row) or with glyphs, and without or with the town chip
     * (from 380dp). All four are the one layout, so the builder is given the size, not a layout id
     * (STEP-8-DESIGN section 14, item 2). The launcher picks the largest that fits, the smallest when none does.
     */
    private val STRIP_SIZES = listOf(SizeF(250f, 40f), SizeF(250f, 64f), SizeF(380f, 40f), SizeF(380f, 64f))
    private const val STRIP_GLYPH_HEIGHT_DP = 64f
    private const val STRIP_CHIP_WIDTH_DP = 380f

    private fun forText(context: Context, sizes: List<Pair<SizeF, Int>>) =
        if (context.resources.configuration.fontScale >= LARGE_TEXT) sizes.take(1) else sizes

    /** Below Android 12 a launcher takes one RemoteViews, so it gets the smallest (thin) entry at every size. */
    fun strip(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RemoteViews(STRIP_SIZES.associateWith { stripViews(context, state, now, it) })
        } else {
            stripViews(context, state, now, STRIP_SIZES.first())
        }
    }

    private fun responsive(sizes: List<Pair<SizeF, Int>>, build: (Int) -> RemoteViews): RemoteViews =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sizes.size > 1) {
            RemoteViews(sizes.associate { (size, layout) -> size to build(layout) })
        } else {
            build(sizes.first().second)
        }

    private fun nextViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int): RemoteViews {
        val views = themedViews(context, state, layout)
        val wide = layout == R.layout.widget_next_prayer_wide
        applyDirection(context, views)
        applyTap(context, views)
        // The body is the sky of the current period (step 8), written after the paint so it wins over
        // the paper body the paint pins, and on every render so a period that ends is replaced. The chip
        // gets the rimmed pill for the same reason as the highlighted row below: on a sky the plain pill
        // can be the sky's own colour.
        views.setInt(R.id.widget_root, "setBackgroundResource", skin(state, skyDrawable(state.period)))
        fillChip(context, views, state)
        views.setInt(R.id.widget_place_chip, "setBackgroundResource", skin(state, R.drawable.widget_pill_sky))

        val next = state.next
        if (!state.hasLocation || next == null) {
            // The one message, in the name's place (on_surface ink, 14sp, up to two lines), with the glyph and the
            // time hidden: in the grey foot line it read as an afterthought on the sky. Fable, 25 Oct 2026.
            views.setTextViewText(R.id.widget_name, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_name, View.VISIBLE)
            views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, MESSAGE_SP)
            views.setInt(R.id.widget_name, "setMaxLines", NO_LOCATION_LINES)
            views.setViewVisibility(R.id.widget_sublabel, View.GONE)
            for (id in HERO_IDS) if (id != R.id.widget_name) views.setViewVisibility(id, View.GONE)
            if (wide) {
                views.setViewVisibility(R.id.widget_divider, View.GONE)
                for (slot in PrayerSlot.entries) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            }
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }

        fillHero(context, views, state, next, now, if (wide) WIDE_COUNTDOWN_DP else SMALL_COUNTDOWN_DP, sky = true)
        // Written back: a recycled view that showed the no location message had the name at 14sp and two lines.
        views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, NAME_SP)
        views.setInt(R.id.widget_name, "setMaxLines", 1)
        if (wide) {
            views.setViewVisibility(R.id.widget_divider, View.VISIBLE)
            views.setImageViewResource(R.id.widget_divider, skin(state, R.drawable.widget_divider_sky))
            fillRows(context, views, state, labels = false, sky = true)
        }

        views.setContentDescription(R.id.widget_root, withPlace(context, state, nextSentence(context, next, now)))
        return views
    }

    private fun todayViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int): RemoteViews {
        val views = themedViews(context, state, layout)
        val large = layout == R.layout.widget_today_large
        applyDirection(context, views)
        applyTap(context, views)
        fillChip(context, views, state)
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
            fillHero(context, views, state, next, now, LARGE_COUNTDOWN_DP)
        }

        val spoken = fillRows(context, views, state, labels = true)

        if (today.approximated) {
            views.setTextViewText(R.id.widget_footer, context.getString(R.string.notif_approximate))
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

    /**
     * The Today strip at one of its four sizes. Written both ways for everything that differs between
     * them (the glyph row, the chip, the padding, which of columns and the large text row shows).
     */
    private fun stripViews(context: Context, state: WidgetState, now: Instant, size: SizeF): RemoteViews {
        val views = themedViews(context, state, R.layout.widget_strip)
        val glyphs = size.height >= STRIP_GLYPH_HEIGHT_DP
        applyDirection(context, views)
        applyTap(context, views)
        // The thin entry gives the content 2dp above and below to fit 40dp; the tall one has room for 4dp.
        val side = dp(context, 4f)
        val edge = dp(context, if (glyphs) 4f else 2f)
        views.setViewPadding(R.id.widget_root, side, edge, side, edge)
        // From 1.3 text the five columns would cut names and times (STEP-8-DESIGN section 3), so the next
        // prayer alone is shown at the user's size, at every one of the four entries.
        val large = context.resources.configuration.fontScale >= LARGE_TEXT
        // fillChip shows or hides by whether there is a town; the width then decides whether there is room, and
        // large text never has it (Opus review, 25 Oct 2026: it squeezed the one prayer that is left).
        fillChip(context, views, state)
        if (size.width < STRIP_CHIP_WIDTH_DP || large) views.setViewVisibility(R.id.widget_place_chip, View.GONE)

        val today = state.today
        val next = state.next
        if (!state.hasLocation || today == null || next == null) {
            for (slot in WidgetState.PRAYERS) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            views.setViewVisibility(R.id.widget_strip_next, View.GONE)
            views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_message, View.VISIBLE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }
        views.setViewVisibility(R.id.widget_message, View.GONE)

        val spoken = fillColumns(context, views, state, today, glyphs, visible = !large)
        if (large) {
            views.setImageViewResource(R.id.widget_glyph, glyph(next.slot))
            views.setTextViewText(R.id.widget_name, next.slot.label(context))
            views.setTextViewText(R.id.widget_time, TimeFormat.clock(context, next.at))
            views.setTextViewText(R.id.widget_sublabel, context.getString(R.string.widget_tomorrow))
            views.setViewVisibility(R.id.widget_sublabel, if (next.isTomorrow) View.VISIBLE else View.GONE)
        }
        views.setViewVisibility(R.id.widget_strip_next, if (large) View.VISIBLE else View.GONE)

        // Sunrise is not shown, so it is not spoken. A projected polar day has no visible marker (no room
        // in one row), so the sentence carries it.
        if (today.approximated) spoken += context.getString(R.string.notif_approximate)
        views.setContentDescription(
            R.id.widget_root,
            withPlace(context, state, context.getString(R.string.widget_a11y_today, nextSentence(context, next, now) + ". " + spoken.joinToString(". "))),
        )
        return views
    }

    /**
     * The five columns, Fajr to Isha. Returns the spoken row for each, with the full Ramadan phrase
     * ("Suhoor ends") even where the column shows the short word. Not [fillRows]: the strip has no label
     * column and no sunrise, and [ROWS] ids for them are absent from its layout.
     */
    private fun fillColumns(
        context: Context,
        views: RemoteViews,
        state: WidgetState,
        today: DayPrayerTimes,
        glyphs: Boolean,
        visible: Boolean,
    ): MutableList<String> {
        val spoken = mutableListOf<String>()
        for (slot in WidgetState.PRAYERS) {
            val ids = ROWS.getValue(slot)
            val name = slot.label(context)
            val time = TimeFormat.clock(context, today[slot])
            val highlighted = state.isHighlighted(slot)
            val word = state.ramadanWordFor(slot, state.ramadanToday)
            // The column heading a printed Ramadan timetable uses; the spoken row keeps "Suhoor ends".
            val heading = if (word != null) {
                context.getString(if (word == RamadanWord.IFTAR) R.string.widget_iftar else R.string.widget_strip_suhoor)
            } else {
                name
            }

            views.setViewVisibility(ids.row, if (visible) View.VISIBLE else View.GONE)
            views.setInt(ids.row, "setBackgroundResource", skin(state, if (highlighted) R.drawable.widget_row_highlight else R.drawable.widget_row_plain))
            views.setImageViewResource(ids.glyph, glyph(slot))
            views.setViewVisibility(ids.glyph, if (glyphs) View.VISIBLE else View.GONE)
            views.setTextViewText(ids.name, if (highlighted) bold(heading) else heading)
            views.setTextViewText(ids.time, if (highlighted) bold(time) else time)

            val spokenWord = word?.let { wordText(context, it) }
            spoken += context.getString(
                R.string.widget_a11y_row,
                if (spokenWord != null) context.getString(R.string.settings_value_pair, name, spokenWord) else name,
                time,
            )
        }
        return spoken
    }

    private fun dp(context: Context, value: Float): Int = (value * context.resources.displayMetrics.density).roundToInt()

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
        sky: Boolean = false,
    ) {
        val time = TimeFormat.clock(context, next.at)
        views.setImageViewResource(R.id.widget_glyph, glyph(next.slot))
        views.setTextViewText(R.id.widget_name, next.slot.label(context))
        views.setTextViewText(R.id.widget_time, time)
        views.setViewVisibility(R.id.widget_glyph, View.VISIBLE)
        views.setViewVisibility(R.id.widget_name, View.VISIBLE)

        // One small line of context, beside the location chip: "Tomorrow", then either the approximate
        // marker or the Ramadan word, joined when both apply (after Isha in Ramadan, the usual case).
        // A projected polar time is marked before the Ramadan word: a guess presented as a measurement
        // is the worse mistake, and the word for Fajr or Maghrib is a convenience.
        val parts = listOfNotNull(
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
                skin(
                    state,
                    when (segment) {
                        Segment.DONE -> R.drawable.widget_seg_done
                        Segment.NEXT -> R.drawable.widget_seg_next
                        // A translucent track on a sky: the paper one is 1.2:1 on the day sky.
                        Segment.TODO -> if (sky) R.drawable.widget_seg_todo_sky else R.drawable.widget_seg_todo
                    },
                ),
            )
        }
    }

    /**
     * The six rows. Returns one spoken phrase per row for the widget's single description. [sky] is the
     * wide Next widget, whose highlighted row takes the rimmed pill (see [nextViews]).
     */
    private fun fillRows(context: Context, views: RemoteViews, state: WidgetState, labels: Boolean, sky: Boolean = false): List<String> {
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
                skin(
                    state,
                    when {
                        !highlighted -> R.drawable.widget_row_plain
                        sky -> R.drawable.widget_pill_sky
                        else -> R.drawable.widget_row_highlight
                    },
                ),
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
        // The Times screen's short date ("Fri 19 Feb"). The full "Friday 19 February" was cut short
        // beside the location chip at the large widget's 250dp minimum (rendered, 18 Oct 2026).
        views.setTextViewText(R.id.widget_date, TimeFormat.date(context, date))

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
    private const val NO_LOCATION_LINES = 2

    /** The context line is 12sp in the layouts; the no location message uses 14sp. Both are written every time. */
    private const val CONTEXT_SP = 12f
    private const val MESSAGE_SP = 14f

    /** The Next prayer widgets' prayer name, 16sp in both layouts; rewritten after the message size. */
    private const val NAME_SP = 16f

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

    // --- a theme chosen in the app ---------------------------------------------------------

    /** Light or Dark from the app's Theme row, or null for System, where the phone decides. */
    private fun chosenDark(state: WidgetState): Boolean? = when (state.theme) {
        ThemeChoice.SYSTEM -> null
        ThemeChoice.LIGHT -> false
        ThemeChoice.DARK -> true
    }

    /** The layout itself under System; under a chosen theme the same layout in a frame, a new id. */
    private fun themedViews(context: Context, state: WidgetState, @LayoutRes layout: Int): RemoteViews {
        val dark = chosenDark(state) ?: return RemoteViews(context.packageName, layout)
        return RemoteViews(context.packageName, FIXED_LAYOUTS.getValue(layout)).also { paint(context, it, layout, dark) }
    }

    /**
     * Pins every colour [layout] declares to its fixed light or dark value. Written first, so the
     * backgrounds the renderer switches afterwards (a highlighted row, the day's bars) come after it
     * and win. A tint is pinned with a colour filter, which an ImageView draws in place of its
     * layout tint: setImageTintList cannot be sent to a launcher below Android 12.
     */
    private fun paint(context: Context, views: RemoteViews, @LayoutRes layout: Int, dark: Boolean) {
        for (p in PAINTS.getValue(layout)) {
            when (p.ink) {
                Ink.TEXT -> views.setTextColor(p.view, context.getColor(fixedColour(p.resource, dark)))
                Ink.TINT -> views.setInt(p.view, "setColorFilter", context.getColor(fixedColour(p.resource, dark)))
                Ink.BACKGROUND -> views.setInt(p.view, "setBackgroundResource", fixedDrawable(p.resource, dark))
                Ink.IMAGE -> views.setImageViewResource(p.view, fixedDrawable(p.resource, dark))
            }
        }
    }

    /** [drawable] as the layout names it under System, its fixed copy under a chosen theme. */
    @DrawableRes
    private fun skin(state: WidgetState, @DrawableRes drawable: Int): Int =
        chosenDark(state)?.let { fixedDrawable(drawable, it) } ?: drawable

    private fun fixedColour(colour: Int, dark: Boolean): Int = FIXED_COLOURS.getValue(colour).let { if (dark) it.second else it.first }

    /** A drawable with no colour of its own (the plain row is transparent) has no copies and is itself. */
    private fun fixedDrawable(drawable: Int, dark: Boolean): Int =
        FIXED_DRAWABLES[drawable]?.let { if (dark) it.second else it.first } ?: drawable

    private val FIXED_LAYOUTS = mapOf(
        R.layout.widget_next_prayer to R.layout.widget_next_prayer_fixed,
        R.layout.widget_next_prayer_wide to R.layout.widget_next_prayer_wide_fixed,
        R.layout.widget_today to R.layout.widget_today_fixed,
        R.layout.widget_today_large to R.layout.widget_today_large_fixed,
        R.layout.widget_strip to R.layout.widget_strip_fixed,
    )

    /** The three inks the layouts use, each to its fixed light and dark value (values/colors.xml). */
    internal val FIXED_COLOURS = mapOf(
        R.color.widget_on_surface to (R.color.widget_light_on_surface to R.color.widget_dark_on_surface),
        R.color.widget_on_surface_variant to (R.color.widget_light_on_surface_variant to R.color.widget_dark_on_surface_variant),
        R.color.widget_primary to (R.color.widget_light_primary to R.color.widget_dark_primary),
    )

    internal val FIXED_DRAWABLES = mapOf(
        R.drawable.widget_background to (R.drawable.widget_background_light to R.drawable.widget_background_dark),
        R.drawable.widget_hero_background to (R.drawable.widget_hero_background_light to R.drawable.widget_hero_background_dark),
        R.drawable.widget_chip to (R.drawable.widget_chip_light to R.drawable.widget_chip_dark),
        R.drawable.widget_divider to (R.drawable.widget_divider_light to R.drawable.widget_divider_dark),
        R.drawable.widget_row_highlight to (R.drawable.widget_row_highlight_light to R.drawable.widget_row_highlight_dark),
        R.drawable.widget_seg_done to (R.drawable.widget_seg_done_light to R.drawable.widget_seg_done_dark),
        R.drawable.widget_seg_next to (R.drawable.widget_seg_next_light to R.drawable.widget_seg_next_dark),
        R.drawable.widget_seg_todo to (R.drawable.widget_seg_todo_light to R.drawable.widget_seg_todo_dark),
        // Step 8: the Next prayer widget's four skies, its rimmed pill, and the track and hairline on a sky.
        R.drawable.widget_sky_dawn to (R.drawable.widget_sky_dawn_light to R.drawable.widget_sky_dawn_dark),
        R.drawable.widget_sky_day to (R.drawable.widget_sky_day_light to R.drawable.widget_sky_day_dark),
        R.drawable.widget_sky_evening to (R.drawable.widget_sky_evening_light to R.drawable.widget_sky_evening_dark),
        R.drawable.widget_sky_night to (R.drawable.widget_sky_night_light to R.drawable.widget_sky_night_dark),
        R.drawable.widget_pill_sky to (R.drawable.widget_pill_sky_light to R.drawable.widget_pill_sky_dark),
        R.drawable.widget_seg_todo_sky to (R.drawable.widget_seg_todo_sky_light to R.drawable.widget_seg_todo_sky_dark),
        R.drawable.widget_divider_sky to (R.drawable.widget_divider_sky_light to R.drawable.widget_divider_sky_dark),
    )

    /** The four skies are the phone's (Color.kt), one per period of the day. */
    @DrawableRes
    private fun skyDrawable(period: SkyPeriod): Int = when (period) {
        SkyPeriod.DAWN -> R.drawable.widget_sky_dawn
        SkyPeriod.DAY -> R.drawable.widget_sky_day
        SkyPeriod.EVENING -> R.drawable.widget_sky_evening
        SkyPeriod.NIGHT -> R.drawable.widget_sky_night
    }

    internal enum class Ink { TEXT, TINT, BACKGROUND, IMAGE }

    /** One coloured property of one view, named by the resource the layout gives it. */
    internal data class Paint(val view: Int, val ink: Ink, val resource: Int)

    private val CHIP_PAINTS = listOf(
        Paint(R.id.widget_place_chip, Ink.BACKGROUND, R.drawable.widget_chip),
        Paint(R.id.widget_place_pin, Ink.TINT, R.color.widget_primary),
        Paint(R.id.widget_place, Ink.TEXT, R.color.widget_primary),
        Paint(R.id.widget_place_update, Ink.TINT, R.color.widget_primary),
    )

    /** The countdown block. The large Today card names the prayer in green and its time in full ink. */
    private fun heroPaints(name: Int, time: Int) = listOf(
        Paint(R.id.widget_glyph, Ink.TINT, R.color.widget_primary),
        Paint(R.id.widget_name, Ink.TEXT, name),
        Paint(R.id.widget_time, Ink.TEXT, time),
        Paint(R.id.widget_sublabel, Ink.TEXT, R.color.widget_on_surface_variant),
        Paint(R.id.widget_countdown, Ink.TEXT, R.color.widget_on_surface),
        Paint(R.id.widget_hero_time, Ink.TEXT, R.color.widget_on_surface),
    ) + SEGMENTS.map { Paint(it, Ink.BACKGROUND, R.drawable.widget_seg_todo) }

    /** The six rows. Sunrise is dimmed, as on the Times screen. */
    private val ROW_PAINTS = ROWS.flatMap { (slot, ids) ->
        val ink = if (slot == PrayerSlot.SUNRISE) R.color.widget_on_surface_variant else R.color.widget_on_surface
        listOf(
            Paint(ids.row, Ink.BACKGROUND, R.drawable.widget_row_plain),
            Paint(ids.glyph, Ink.TINT, R.color.widget_on_surface_variant),
            Paint(ids.name, Ink.TEXT, ink),
            Paint(ids.label, Ink.TEXT, R.color.widget_on_surface_variant),
            Paint(ids.time, Ink.TEXT, ink),
        )
    }

    /** The strip's five columns: the Today row's inks without the label column or sunrise. */
    private val STRIP_COLUMN_PAINTS = WidgetState.PRAYERS.flatMap { slot ->
        val ids = ROWS.getValue(slot)
        listOf(
            Paint(ids.row, Ink.BACKGROUND, R.drawable.widget_row_plain),
            Paint(ids.glyph, Ink.TINT, R.color.widget_on_surface_variant),
            Paint(ids.name, Ink.TEXT, R.color.widget_on_surface),
            Paint(ids.time, Ink.TEXT, R.color.widget_on_surface),
        )
    }

    private val ROOT_PAINT = Paint(R.id.widget_root, Ink.BACKGROUND, R.drawable.widget_background)

    /**
     * Every colour each layout declares, as the layout declares it. WidgetThemeTest reads the layout
     * files and fails the build if a coloured view is missing here, so a colour added to a layout
     * cannot be left following the phone under a chosen theme.
     */
    internal val PAINTS: Map<Int, List<Paint>> = mapOf(
        R.layout.widget_next_prayer to listOf(ROOT_PAINT) + heroPaints(R.color.widget_on_surface, R.color.widget_on_surface_variant) + CHIP_PAINTS,
        R.layout.widget_next_prayer_wide to listOf(ROOT_PAINT, Paint(R.id.widget_divider, Ink.IMAGE, R.drawable.widget_divider)) +
            heroPaints(R.color.widget_on_surface, R.color.widget_on_surface_variant) + CHIP_PAINTS + ROW_PAINTS,
        R.layout.widget_today to listOf(
            ROOT_PAINT,
            Paint(R.id.widget_message, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_footer, Ink.TEXT, R.color.widget_on_surface_variant),
        ) + ROW_PAINTS + CHIP_PAINTS,
        R.layout.widget_today_large to listOf(
            ROOT_PAINT,
            Paint(R.id.widget_date, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_hijri, Ink.TEXT, R.color.widget_on_surface_variant),
            Paint(R.id.widget_message, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_hero, Ink.BACKGROUND, R.drawable.widget_hero_background),
            Paint(R.id.widget_footer, Ink.TEXT, R.color.widget_on_surface_variant),
        ) + heroPaints(R.color.widget_primary, R.color.widget_on_surface) + ROW_PAINTS + CHIP_PAINTS,
        R.layout.widget_strip to listOf(
            ROOT_PAINT,
            Paint(R.id.widget_message, Ink.TEXT, R.color.widget_on_surface),
            // The large text row: the next prayer alone (the ids are the hero's, in a row of their own).
            Paint(R.id.widget_glyph, Ink.TINT, R.color.widget_on_surface_variant),
            Paint(R.id.widget_name, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_time, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_sublabel, Ink.TEXT, R.color.widget_on_surface_variant),
        ) + STRIP_COLUMN_PAINTS + CHIP_PAINTS,
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
        views.setOnClickPendingIntent(R.id.widget_root, openApp(context, TAP_REQUEST_CODE))
    }

    private fun openApp(context: Context, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /**
     * The location chip: pin, the town the times are for, an update arrow (owner's request, 18 Oct
     * 2026). Pressing it opens the app, and opening the app is what updates the location: every
     * foreground visit fetches the current position and recalculates, which redraws the widgets
     * (MainActivity.onResume, SajdaViewModel.refreshLocation). It cannot update without opening the
     * app, because location is read only while the app is in the foreground (CLAUDE.md, a hard rule).
     * Its own tap and spoken label make it a button TalkBack can find; the action is the same as the
     * rest of the widget. Hidden with no location: the widget then says how to set one.
     */
    private fun fillChip(context: Context, views: RemoteViews, state: WidgetState) {
        val place = if (state.hasLocation) place(context, state) else null
        if (place == null) {
            views.setViewVisibility(R.id.widget_place_chip, View.GONE)
            return
        }
        views.setTextViewText(R.id.widget_place, place)
        views.setViewVisibility(R.id.widget_place_chip, View.VISIBLE)
        views.setContentDescription(R.id.widget_place_chip, context.getString(R.string.widget_update_location, place.bidiIsolated()))
        views.setOnClickPendingIntent(R.id.widget_place_chip, openApp(context, CHIP_REQUEST_CODE))
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

    /** The chip's own open intent. Same destination as [TAP_REQUEST_CODE]; distinct so neither replaces the other. */
    private const val CHIP_REQUEST_CODE = 2
}
