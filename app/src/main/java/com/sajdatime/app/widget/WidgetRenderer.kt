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
 * row, built at several sizes from the one layout ([stripSizes]); the Next prayer and Today widgets share its body
 * ([stripBody]) since 1.7.0.
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
    // Heights are what the layouts WANT, measured on 3 Nov 2026 with AppWidgetHostView and an unspecified height
    // (WidgetRenderEvidenceTest, needed_heights): small 131.8dp at normal text (134.9, 140.6, 161.5 at 1.15x, 1.3x, 2.0),
    // wide 149.0 and 156.2 at 1.15x. The wide entry was 150 and is now 158. The small entry stays 110 ON PURPOSE: it is the
    // smallest, so the launcher uses it whatever its height, and the widget's minHeight (widget_next_prayer_info.xml) is
    // 110 because a bigger one makes One UI (two rows are about 158dp) and every launcher below Android 12 open it three
    // rows tall (Opus review, 3 Nov 2026). So the 110dp entry hides the day bars instead (Ali, 3 Nov 2026, "drop the bars
    // when short"); see nextSizes. Below Android 12 only the first entry is used, so the bars never show there.
    // Urdu, Arabic and Bengali sit taller (measured the same day): the wide layout wants 165.3dp in Urdu and Arabic and
    // 188.2 at 1.15x, Bengali 150.9 and 169.9; the small one fits 162 in all three.
    // Measured needs of the full small layout: 131.8dp (134.9 at 1.15x) in Latin script, 137.1 (141.3) in Urdu and Arabic,
    // 133.7 (137.5) in Bengali, so the bars come back from 140 (137.1 and 138.3 at 1.2x and 1.25x, measured) and from 150 in those three scripts.
    private fun nextBarsHeight(context: Context): Float = if (AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES) 150f else 140f

    private fun nextSizes(context: Context): List<Pair<SizeF, Int>> {
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        val grown = context.resources.configuration.fontScale >= 1.1f
        val wideHeight = if (tall) (if (grown) 192f else 170f) else if (grown) 166f else 158f
        // Two entries of the small layout: from 140dp (150 in Urdu, Arabic and Bengali) it shows the five day bars; below that it hides them so the town
        // chip is not cut (Ali, 3 Nov 2026: "drop the bars when short").
        return listOf(
            SizeF(180f, 110f) to R.layout.widget_next_prayer,
            SizeF(180f, nextBarsHeight(context)) to R.layout.widget_next_prayer,
            SizeF(270f, wideHeight) to R.layout.widget_next_prayer_wide,
        )
    }

    /**
     * Today: the compact list from its minimum, the large card (date, countdown, the day) from 306dp (see [todaySizes] for the measured heights)
     * tall, which is what the header, the card and six rows need at the normal text size (rendered).
     */
    private fun todaySizes(context: Context): List<Pair<SizeF, Int>> {
        // Urdu and Arabic script sits taller than Latin at the same size: rendered on 18 Oct 2026, the
        // large card at 280dp held four of their six rows, and all six from 330dp. Bengali joined them on
        // 30 Oct 2026: its vowel signs sit above and below the line, so its rows are as tall.
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        // Measured 3 Nov 2026 (needed_heights): the large card wants 296dp at normal text and 322dp at 1.15x, since the
        // 15sp rows and the band's extra room (08bb2b8); the 280dp it used to take clipped the last row. Urdu, Arabic and
        // Bengali: measured the same day, the large card wants 340.6dp (Urdu), 339.8 (Arabic), 309.0 (Bengali) and 369.9, 365.0,
        // 336.8 at 1.15x, (daytime figures, replaced by the evening maxima below).
        val grown = context.resources.configuration.fontScale >= 1.1f
        // Re-measured 4 Nov 2026 at four times of day (compact_today_windows): after Isha the card shows tomorrow's countdown and
        // wants 10 to 19dp more than in the day (302.5 at 1.0x, 347.8 at 1.25x in English; 354.7 and 408.4 in Urdu, 353.9 and 402.3
        // in Arabic, 320.4 and 366.9 in Bengali), so every evening a widget at the old heights cut its last row. These are those
        // maxima plus about 4dp.
        val largeHeight = if (tall) (if (grown) 412f else 358f) else (if (grown) 352f else 306f)
        // The first compact entry shows as many rows as 180dp holds; from the second (the height six rows need, see
        // [compactSixRowsHeight]) all six show. Same layout, so todayViews is given the size.
        val six = compactSixRowsHeight(context)
        return listOfNotNull(
            SizeF(250f, 180f) to R.layout.widget_today,
            if (six > 180f) SizeF(250f, six) to R.layout.widget_today else null,
            SizeF(250f, maxOf(largeHeight, six + 1f)) to R.layout.widget_today_large,
        )
    }

    private val TALL_SCRIPT_LANGUAGES = setOf("ur", "ar", "bn")

    /**
     * The compact Today list is 180dp tall and six rows no longer fit from 1.1x text or in the tall scripts (measured 3 Nov 2026:
     * six rows want 193dp at 1.15x, 206 at 1.25x, 282 at 2.0, 203 in Urdu). With these counts the first entry measures 126 to 180dp
     * in English, Urdu, Arabic and Bengali from 1.0x to 2.0x, and [compactSixRowsHeight] (measured, with margin) gives six rows to a taller widget.
     * already passed, so nothing is cut half way: the window starts at the next prayer (sunrise is never next) and, when that leaves too
     * few rows, starts earlier. Row counts follow the measured heights per row; see HANDOVER.
     */
    /** The height six compact rows need, so a taller widget keeps all of them (measured 3 Nov 2026, see HANDOVER). */
    private fun compactSixRowsHeight(context: Context): Float {
        val scale = context.resources.configuration.fontScale
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return when {
            scale >= 1.6f -> if (tall) 345f else 300f
            scale >= LARGE_TEXT -> if (tall) 300f else 250f
            scale >= 1.1f -> if (tall) 250f else 210f
            else -> if (tall) 210f else 180f
        }
    }

    private fun compactSlots(context: Context, state: WidgetState, size: SizeF): List<PrayerSlot> {
        // Below Android 12 only the first entry exists and its real height is unknown, so all six rows stay (as before 3 Nov 2026).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || size.height >= compactSixRowsHeight(context)) return PrayerSlot.entries
        val scale = context.resources.configuration.fontScale
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        val count = when {
            tall -> when {
                scale >= 1.6f -> 2
                scale >= LARGE_TEXT -> 3
                scale >= 1.1f -> 4
                else -> 5
            }
            scale >= 1.6f -> 3
            scale >= LARGE_TEXT -> 4
            scale >= 1.1f -> 5
            else -> 6
        }
        val all = PrayerSlot.entries
        val start = state.next?.takeIf { !it.isTomorrow }?.let { all.indexOf(it.slot) } ?: (all.size - count)
        return all.subList(start.coerceAtMost(all.size - count), (start.coerceAtMost(all.size - count)) + count)
    }

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
        return responsive(forText(context, nextSizes(context))) { layout, size -> nextViews(context, state, now, layout, size) }
    }

    fun today(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        // At large text the large card is dropped (see LARGE_TEXT) but every compact entry stays, so a tall widget keeps all six rows.
        val sizes = todaySizes(context).let { all ->
            if (context.resources.configuration.fontScale >= LARGE_TEXT) all.filter { it.second == R.layout.widget_today } else all
        }
        return responsive(sizes) { layout, size -> todayViews(context, state, now, layout, size) }
    }

    /**
     * Today strip, 4 cells by 1: thin (no glyph row) or with glyphs, and without or with the town chip
     * (from 380dp). All four are the one layout, so the builder is given the size, not a layout id
     * (STEP-8-DESIGN section 14, item 2). The launcher picks the entry that fits best, the smallest when none does.
     */
    // 1.7.0 (Ali, 3 Nov 2026: "row level widget with icons and prayer time ... adjusted to half a row"): the glyph
    // now sits beside the name, so it fits one cell tall (design E2); from 300dp wide it shows, below that names and
    // times only. Under 250dp (half a row, two cells) the next prayer alone: glyph, name, time.
    private fun stripSizes(context: Context): List<SizeF> {
        val tall = stripTallHeight(context)
        val five = stripFiveHeight(context)
        // The FIRST entry is what Android below 12 gets at every size, so it stays the five column thin form (as before).
        // The 40dp entries are the floor: the five columns want 45 to 49.5dp, so under `five` the next prayer alone shows
        // (Ali, 4 Nov 2026: "next prayer only" for the thinnest strip); from `five` the five columns return.
        return listOf(
            SizeF(250f, five), SizeF(250f, 40f), SizeF(110f, 40f), SizeF(300f, 40f), SizeF(300f, five), SizeF(300f, tall), SizeF(300f, STRIP_HERO_HEIGHT_DP), SizeF(300f, STRIP_HERO_BIG_DP),
            SizeF(400f, 40f), SizeF(400f, five), SizeF(400f, tall), SizeF(400f, STRIP_HERO_HEIGHT_DP), SizeF(400f, STRIP_HERO_BIG_DP),
        )
    }
    private const val STRIP_GLYPH_WIDTH_DP = 300f
    // Measured 3 Nov 2026 (needed_heights): the glyph row wants 72.8dp (77.3 at 1.15x) and the hero tier 124.6dp
    // (130.3 at 1.15x); they were 64 and 110, which cut the times and the underline off. Seen at 124dp: complete.
    // The five column thin tier wants 45 to 49.5 (51 in Urdu and Arabic), so under 50 (56) the next prayer alone shows instead
    // (Ali, 4 Nov 2026); below Android 12 the first entry (five columns) is used at every size and can still be cut at 40 to 45dp.
    // Urdu and Arabic want 78.9 and 84.2 (1.15x), 139.0 and 146.7, 145.5 and 151.6: so 86 (Latin script 78), 148 and 152.
    private const val STRIP_TALL_HEIGHT_DP = 78f
    // One UI gives a one row strip about 79dp: the glyph row must still show there (it wants 72.8, 77.3 at 1.15x in Latin
    // script), so the Latin value is 78, not 86. Urdu, Arabic and Bengali want 84.2 at 1.15x.
    private fun stripTallHeight(context: Context): Float =
        if (AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES) 86f else STRIP_TALL_HEIGHT_DP
    // The five column thin form (names and times, no glyphs): measured 4 Nov 2026 in strip_thin_windows, see HANDOVER for the
    // figures. Under this height the strip shows the next prayer alone.
    private const val STRIP_FIVE_HEIGHT_DP = 50f
    private fun stripFiveHeight(context: Context): Float =
        if (AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES) 56f else STRIP_FIVE_HEIGHT_DP
    private const val STRIP_HALF_WIDTH_DP = 250f
    private const val STRIP_HERO_HEIGHT_DP = 148f
    private const val STRIP_HERO_BIG_DP = 152f
    private const val STRIP_CHIP_WIDTH_DP = 400f
    private const val STRIP_MAX_SP = 12f
    private const val STRIP_MIN_SP = 9f
    private const val STRIP_CHIP_ALLOWANCE_DP = 105f

    private fun forText(context: Context, sizes: List<Pair<SizeF, Int>>) =
        if (context.resources.configuration.fontScale >= LARGE_TEXT) sizes.take(1) else sizes

    /** Below Android 12 a launcher takes one RemoteViews, so it gets the first entry (the five column thin one) at every size. */
    fun strip(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RemoteViews(stripSizes(context).associateWith { stripViews(context, state, now, it) })
        } else {
            stripViews(context, state, now, stripSizes(context).first())
        }
    }

    private fun responsive(sizes: List<Pair<SizeF, Int>>, build: (Int, SizeF) -> RemoteViews): RemoteViews =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sizes.size > 1) {
            RemoteViews(sizes.associate { (size, layout) -> size to build(layout, size) })
        } else {
            build(sizes.first().second, sizes.first().first)
        }

    private fun nextViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int, size: SizeF): RemoteViews {
        val views = themedViews(context, state, layout)
        val wide = layout == R.layout.widget_next_prayer_wide
        applyDirection(context, views)
        applyTap(context, views)
        // The short small layout (the 110dp entry, normal text): no day bars, a 32 instead of 36 countdown and 8dp top and
        // bottom padding instead of 12, so it needs about 106dp (measured 118.9 with only the bars gone) and the town chip is
        // not cut. Padding is written on EVERY render: both entries share one layout, so the launcher reapplies actions onto
        // the view it already has and a value left from the other entry would stay.
        val short = !wide && size.height < nextBarsHeight(context) && context.resources.configuration.fontScale < LARGE_TEXT
        // Urdu, Arabic and Bengali sit taller: the short variant wanted 111.2dp (115.4 at 1.15x) with the Latin 32 and 8, so
        // they get a 28 countdown and 6dp padding (measured 102.5 and 106.7 in Urdu and Arabic, 99.0 and 102.9 in Bengali).
        val tallScript = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        val padV = if (short) (if (tallScript) 6f else 8f) else 12f
        if (!wide) views.setViewPadding(R.id.widget_root, dp(context, 14f), dp(context, padV), dp(context, 14f), dp(context, padV))
        // The body is the period's sky in light and the app's green in dark (stripBody), written after the paint
        // so it wins over the paper body the paint pins, and on every render so a period that ends is replaced.
        // The chip gets the rimmed pill: on this body the plain pill can be the body's own colour.
        views.setInt(R.id.widget_root, "setBackgroundResource", skin(state, stripBody(state.period)))
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

        fillHero(context, views, state, next, now, if (wide) WIDE_COUNTDOWN_DP else if (short) (if (tallScript) SHORT_TALL_COUNTDOWN_DP else SHORT_COUNTDOWN_DP) else SMALL_COUNTDOWN_DP, sky = true)
        // Written back: a recycled view that showed the no location message had the name at 14sp and two lines.
        views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, NAME_SP)
        views.setInt(R.id.widget_name, "setMaxLines", 1)
        // From 1.3x text the first entry is the only one, so the bars stay (they are cut there anyway).
        if (short) views.setViewVisibility(R.id.widget_progress, View.GONE)
        if (wide) {
            views.setViewVisibility(R.id.widget_divider, View.VISIBLE)
            views.setImageViewResource(R.id.widget_divider, skin(state, R.drawable.widget_divider_sky))
            fillRows(context, views, state, labels = false, band = true, bandSideDp = 4f)
        }

        views.setContentDescription(R.id.widget_root, withPlace(context, state, nextSentence(context, next, now)))
        return views
    }

    private fun todayViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int, size: SizeF): RemoteViews {
        val views = themedViews(context, state, layout)
        val large = layout == R.layout.widget_today_large
        applyDirection(context, views)
        applyTap(context, views)
        // 1.7.0: the same body as the strip (period sky in light, the app's dark green in dark), written after the paint.
        views.setInt(R.id.widget_root, "setBackgroundResource", skin(state, stripBody(state.period)))
        fillChip(context, views, state)
        views.setInt(R.id.widget_place_chip, "setBackgroundResource", skin(state, R.drawable.widget_pill_sky))
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

        val spoken = fillRows(context, views, state, labels = true, band = large, bandSideDp = 8f, shown = if (large) PrayerSlot.entries else compactSlots(context, state, size))

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
     * The Today strip at one of its sizes. Written both ways for everything that differs between
     * them (the glyph row, the chip, the padding, which of columns and the large text row shows).
     */
    private fun stripViews(context: Context, state: WidgetState, now: Instant, size: SizeF): RemoteViews {
        val views = themedViews(context, state, R.layout.widget_strip)
        val tall = size.height >= stripTallHeight(context)
        // Glyph above name above time (Opus audit, 3 Nov 2026: glyph beside the name put the name off the time's axis and
        // forced per column autosize). Needs 78dp tall (86 in Urdu, Arabic, Bengali) and 300dp wide (measured: 72.8dp, 77.3 at 1.15x, 84.2 in Urdu).
        val glyphs = tall && size.width >= STRIP_GLYPH_WIDTH_DP
        applyDirection(context, views)
        applyTap(context, views)
        // The thin entries give the content 2dp above and below to fit 40dp (the next prayer alone, or five columns from 50); the tall one has room for 4dp.
        val side = dp(context, 4f)
        val edge = dp(context, if (tall) 4f else 2f)
        val heroEntry = size.height >= STRIP_HERO_HEIGHT_DP
        // A tall entry puts the next prayer at the top and the five columns at the foot, the gap between them flexible, so no
        // space is wasted above (Ali, 3 Nov 2026: "too much top spacing").
        views.setViewPadding(R.id.widget_root, side, if (heroEntry) dp(context, 10f) else edge, side, if (heroEntry) dp(context, 8f) else edge)
        // 1.7.0 (Ali, 3 Nov 2026, from a Pillars screenshot: "clean and easy to understand"): on the period's sky, like
        // the Next prayer widget, with the next prayer big on top once the widget is two cells tall.
        views.setInt(R.id.widget_root, "setBackgroundResource", skin(state, stripBody(state.period)))
        // From 1.3 text the five columns would cut names and times (STEP-8-DESIGN section 3), so the next
        // prayer alone is shown at the user's size, at every entry.
        // Half a row (under 250dp wide) shows the next prayer alone too: five columns cannot fit two cells.
        val large = context.resources.configuration.fontScale >= LARGE_TEXT || size.width < STRIP_HALF_WIDTH_DP || size.height < stripFiveHeight(context)
        // fillChip shows or hides by whether there is a town; the width then decides whether there is room, and
        // large text never has it (Opus review, 25 Oct 2026: it squeezed the one prayer that is left).
        fillChip(context, views, state)
        if (size.width < STRIP_CHIP_WIDTH_DP || large) views.setViewVisibility(R.id.widget_place_chip, View.GONE)

        val today = state.today
        val next = state.next
        if (!state.hasLocation || today == null || next == null) {
            for (slot in WidgetState.PRAYERS) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            // widget_message lives inside the row, which a thin or large text entry hides when there is a location: written both ways.
            views.setViewVisibility(R.id.widget_strip_row, View.VISIBLE)
            views.setViewVisibility(R.id.widget_strip_next, View.GONE)
            views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_message, View.VISIBLE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }
        views.setViewVisibility(R.id.widget_message, View.GONE)

        val hero = large || size.height >= STRIP_HERO_HEIGHT_DP
        val spoken = fillColumns(context, views, state, today, glyphs, visible = !large)
        // One size for all ten texts, so no column is smaller than its neighbour (Ali, 3 Nov 2026, "uneven").
        val textSp = stripTextSp(context, today, size.width, chip = size.width >= STRIP_CHIP_WIDTH_DP && !large)
        for (slot in WidgetState.PRAYERS) {
            val ids = ROWS.getValue(slot)
            views.setTextViewTextSize(ids.name, TypedValue.COMPLEX_UNIT_SP, textSp)
            views.setTextViewTextSize(ids.time, TypedValue.COMPLEX_UNIT_SP, textSp)
        }
        views.setViewVisibility(R.id.widget_strip_row, if (large) View.GONE else View.VISIBLE)
        if (hero) {
            // The next prayer grows with the widget, so a tall one is not a small line in a big empty box.
            val sp = when {
                size.height >= STRIP_HERO_BIG_DP -> 28f
                size.height >= STRIP_HERO_HEIGHT_DP -> 24f
                else -> 14f
            }
            views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, sp)
            views.setTextViewTextSize(R.id.widget_time, TypedValue.COMPLEX_UNIT_SP, sp)
            views.setImageViewResource(R.id.widget_glyph, glyph(next.slot))
            // The app's own mark at the top start corner, so the widget still says which app it is (Ali, 3 Nov 2026: a
            // separate small logo, top left, top right in right to left). Only on the 148dp and 152dp entries at the
            // normal text size: the Opus review estimated the hero line overflows 300dp with the logo from 1.15x text,
            // and the thin and 78dp entries have no room. Seen on 3 Nov 2026 in a render at 300x148 and 300x150 and on the emulator launcher at about 225dp tall.
            val room = size.height >= STRIP_HERO_HEIGHT_DP && context.resources.configuration.fontScale <= 1.0f
            views.setViewVisibility(R.id.widget_corner_logo, if (room) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.widget_name, next.slot.label(context))
            views.setTextViewText(R.id.widget_time, timeText(TimeFormat.clock(context, next.at), emphasis = false))
            views.setTextViewText(R.id.widget_sublabel, context.getString(R.string.widget_tomorrow))
            views.setViewVisibility(R.id.widget_sublabel, if (next.isTomorrow) View.VISIBLE else View.GONE)
        }
        views.setViewVisibility(R.id.widget_strip_next, if (hero) View.VISIBLE else View.GONE)

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
            // No box (Ali, 3 Nov 2026, from a Pillars screenshot): the current prayer is marked by the app's own mark in place of
            // its sun glyph, bold text, and an underline, like the reference's pointer and bar.
            views.setInt(ids.row, "setBackgroundResource", skin(state, R.drawable.widget_row_plain))
            views.setImageViewResource(ids.glyph, glyph(slot))
            views.setViewVisibility(ids.glyph, if (glyphs && !highlighted) View.VISIBLE else View.GONE)
            val extra = STRIP_MARKS.getValue(slot)
            views.setViewVisibility(extra.first, if (glyphs && highlighted) View.VISIBLE else View.GONE)
            views.setViewVisibility(extra.second, if (highlighted) View.VISIBLE else View.INVISIBLE)
            views.setTextViewText(ids.name, if (highlighted) bold(heading) else heading)
            views.setTextViewText(ids.time, timeText(time, highlighted))

            val spokenWord = word?.let { wordText(context, it) }
            spoken += context.getString(
                R.string.widget_a11y_row,
                if (spokenWord != null) context.getString(R.string.settings_value_pair, name, spokenWord) else name,
                time,
            )
        }
        return spoken
    }

    /**
     * The largest size up to 12sp (floor 9sp, steps of 0.5) at which the widest of the ten column texts fits one
     * column, measured in bold at the user's font scale. The same size goes to every column.
     */
    private fun stripTextSp(context: Context, today: DayPrayerTimes, widthDp: Float, chip: Boolean): Float {
        val d = context.resources.displayMetrics.density
        val scale = context.resources.configuration.fontScale
        val column = ((widthDp - 8f - (if (chip) STRIP_CHIP_ALLOWANCE_DP else 0f)) / WidgetState.PRAYERS.size - 4f) * d
        val texts = WidgetState.PRAYERS.flatMap { listOf(it.label(context), TimeFormat.clock(context, today[it])) }
        val paint = android.graphics.Paint().apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        var sp = STRIP_MAX_SP
        while (sp > STRIP_MIN_SP) {
            paint.textSize = sp * d * scale
            if (texts.maxOf { paint.measureText(it) } <= column) break
            sp -= 0.5f
        }
        return sp
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
        views.setTextViewText(R.id.widget_time, timeText(time, false))
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
     * The six rows. Returns one spoken phrase per row for the widget's single description. [band] is the larger
     * forms (wide Next, large Today), whose highlighted row takes the rimmed band; otherwise the 4dp underline marks it.
     */
    private fun fillRows(
        context: Context, views: RemoteViews, state: WidgetState, labels: Boolean, band: Boolean, bandSideDp: Float = 0f,
        shown: List<PrayerSlot> = PrayerSlot.entries,
    ): List<String> {
        val today = state.today ?: return emptyList()
        val spoken = mutableListOf<String>()
        for (slot in PrayerSlot.entries) {
            val ids = ROWS.getValue(slot)
            // Rows left out still get their text (the spoken sentence names all six); only the view goes. Written both ways,
            // since the launcher reuses the view.
            if (slot !in shown) {
                views.setViewVisibility(ids.row, View.GONE)
                spoken += context.getString(R.string.widget_a11y_row, slot.label(context), TimeFormat.clock(context, today[slot]))
                continue
            }
            val name = slot.label(context)
            val time = TimeFormat.clock(context, today[slot])
            val highlighted = state.isHighlighted(slot)
            val word = state.ramadanWordFor(slot, state.ramadanToday)?.let { wordText(context, it) }

            views.setViewVisibility(ids.row, View.VISIBLE)
            // The current row is marked by size (Ali, 3 Nov 2026): a highlight band behind it on the larger forms (Next prayer
            // wide, Today with the countdown card), a 4dp underline on the compact Today. Both marks carry the app's logo and
            // bold. The underline sits in the marked row's own 4dp of bottom padding (below the text, not over it), so that row is 3dp taller and the weighted gaps absorb it. Both states are written
            // every time because the launcher reuses these views.
            views.setViewVisibility(ids.underline, if (highlighted && !band) View.VISIBLE else View.GONE)
            views.setInt(ids.row, "setBackgroundResource", skin(state, if (highlighted && band) R.drawable.widget_pill_sky else R.drawable.widget_row_plain))
            // The band row gets 4dp above and below the text so the current prayer is not squashed against its rim (Ali, 3 Nov
            // 2026: "the active one needs to be nicely visible"). Only that row grows; the weighted gaps between the rows
            // absorb it, so no neighbour moves. Written for every row because the launcher reuses views. The sides repeat
            // the layout's own padding, which setViewPadding replaces.
            if (band) {
                val side = dp(context, bandSideDp)
                val vertical = if (highlighted) dp(context, 4f) else 0
                views.setViewPadding(ids.row, side, vertical, side, vertical)
            } else {
                // The 4dp underline sits at the row's bottom (layout margin -4dp, row clipToPadding false). With no padding below it cut into the text and the
                // logo tile on a real Redmi (HyperOS font sits lower in its line box than the emulator's Roboto, seen 4 Nov
                // 2026), so the marked row gets 4dp below it for the line to sit in. The other rows give up their 1dp below
                // (the layout's) so the whole widget stays near its measured height: with 5dp on the marked row alone, 21 of
                // 640 measured sizes ran 1 to 8dp over. Written for every row both ways (the launcher reuses views); the sides
                // and top repeat the layout's own 8dp and 1dp.
                views.setViewPadding(ids.row, dp(context, 8f), dp(context, 1f), dp(context, 8f), dp(context, if (highlighted) 4f else 0f))
            }
            // The mark is its own view (the strip's widget_logo_*), never the glyph view with a filter or tint removed: under a theme
            // chosen in the app the glyph carries a colour filter that turned the mark into a flat grey tile (seen on the emulator).
            views.setImageViewResource(ids.glyph, glyph(slot))
            STRIP_MARKS[slot]?.first?.let { logo ->
                views.setViewVisibility(ids.glyph, if (highlighted) View.GONE else View.VISIBLE)
                views.setViewVisibility(logo, if (highlighted) View.VISIBLE else View.GONE)
            }
            views.setTextViewText(ids.name, if (highlighted) bold(name) else name)
            views.setTextViewText(ids.time, timeText(time, highlighted))

            // "Now", a Ramadan word, both, or nothing. Written every time, so a row that stops being
            // current or stops carrying a word is cleared. Empty on the wide Next widget, where the label
            // column is only the space that holds the time at the far end. (4 Nov 2026: Now, as the app.)
            val marker = if (highlighted) context.getString(R.string.home_now_marker) else null
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
     * widget's 110dp minimum the short variant hides the day bars and uses [SHORT_COUNTDOWN_DP] (measured 3 Nov 2026: 105.9dp
     * needed). From 140dp the bars show; One UI gives about 158dp and shows everything.
     */
    private const val SMALL_COUNTDOWN_DP = 36f
    private const val SHORT_COUNTDOWN_DP = 32f
    private const val SHORT_TALL_COUNTDOWN_DP = 28f
    private const val WIDE_COUNTDOWN_DP = 34f
    private const val LARGE_COUNTDOWN_DP = 38f

    /** Everything the countdown block owns, hidden together when there is nothing to count down to. */
    private val HERO_IDS = listOf(
        R.id.widget_glyph, R.id.widget_name, R.id.widget_time, R.id.widget_countdown, R.id.widget_hero_time, R.id.widget_progress,
    )

    private val SEGMENTS = listOf(R.id.widget_seg_1, R.id.widget_seg_2, R.id.widget_seg_3, R.id.widget_seg_4, R.id.widget_seg_5)

    /** The view ids of one row. Both Today layouts and the wide Next layout use the same ones. */
    private class RowIds(val row: Int, val glyph: Int, val name: Int, val label: Int, val time: Int, val underline: Int)

    /** The strip's per column mark (the app's logo, in place of the glyph) and underline. */
    private val STRIP_MARKS = mapOf(
        PrayerSlot.FAJR to (R.id.widget_logo_fajr to R.id.widget_bar_fajr),
        PrayerSlot.DHUHR to (R.id.widget_logo_dhuhr to R.id.widget_bar_dhuhr),
        PrayerSlot.ASR to (R.id.widget_logo_asr to R.id.widget_bar_asr),
        PrayerSlot.MAGHRIB to (R.id.widget_logo_maghrib to R.id.widget_bar_maghrib),
        PrayerSlot.ISHA to (R.id.widget_logo_isha to R.id.widget_bar_isha),
    )

    private val ROWS = mapOf(
        PrayerSlot.FAJR to RowIds(R.id.widget_row_fajr, R.id.widget_glyph_fajr, R.id.widget_name_fajr, R.id.widget_label_fajr, R.id.widget_time_fajr, R.id.widget_row_underline_fajr),
        PrayerSlot.SUNRISE to RowIds(R.id.widget_row_sunrise, R.id.widget_glyph_sunrise, R.id.widget_name_sunrise, R.id.widget_label_sunrise, R.id.widget_time_sunrise, R.id.widget_row_underline_sunrise),
        PrayerSlot.DHUHR to RowIds(R.id.widget_row_dhuhr, R.id.widget_glyph_dhuhr, R.id.widget_name_dhuhr, R.id.widget_label_dhuhr, R.id.widget_time_dhuhr, R.id.widget_row_underline_dhuhr),
        PrayerSlot.ASR to RowIds(R.id.widget_row_asr, R.id.widget_glyph_asr, R.id.widget_name_asr, R.id.widget_label_asr, R.id.widget_time_asr, R.id.widget_row_underline_asr),
        PrayerSlot.MAGHRIB to RowIds(R.id.widget_row_maghrib, R.id.widget_glyph_maghrib, R.id.widget_name_maghrib, R.id.widget_label_maghrib, R.id.widget_time_maghrib, R.id.widget_row_underline_maghrib),
        PrayerSlot.ISHA to RowIds(R.id.widget_row_isha, R.id.widget_glyph_isha, R.id.widget_name_isha, R.id.widget_label_isha, R.id.widget_time_isha, R.id.widget_row_underline_isha),
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
        R.drawable.widget_underline to (R.drawable.widget_underline_light to R.drawable.widget_underline_dark),
        R.drawable.widget_strip_dawn to (R.drawable.widget_strip_dawn_light to R.drawable.widget_strip_dawn_dark),
        R.drawable.widget_strip_day to (R.drawable.widget_strip_day_light to R.drawable.widget_strip_day_dark),
        R.drawable.widget_strip_evening to (R.drawable.widget_strip_evening_light to R.drawable.widget_strip_evening_dark),
        R.drawable.widget_strip_night to (R.drawable.widget_strip_night_light to R.drawable.widget_strip_night_dark),
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

    /** The Today strip's own body: the period's sky in light, the app's dark green in dark (1.7.0). */
    @DrawableRes
    private fun stripBody(period: SkyPeriod): Int = when (period) {
        SkyPeriod.DAWN -> R.drawable.widget_strip_dawn
        SkyPeriod.DAY -> R.drawable.widget_strip_day
        SkyPeriod.EVENING -> R.drawable.widget_strip_evening
        SkyPeriod.NIGHT -> R.drawable.widget_strip_night
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

    /** The compact Today's overlay underlines; the larger forms mark the current row with a band instead. */
    private val ROW_UNDERLINE_PAINTS = ROWS.values.map { Paint(it.underline, Ink.IMAGE, R.drawable.widget_underline) }

    /** The strip's five columns: the Today row's inks without the label column or sunrise. */
    private val STRIP_COLUMN_PAINTS = WidgetState.PRAYERS.flatMap { slot ->
        val ids = ROWS.getValue(slot)
        listOf(
            Paint(ids.row, Ink.BACKGROUND, R.drawable.widget_row_plain),
            Paint(ids.glyph, Ink.TINT, R.color.widget_on_surface_variant),
            Paint(ids.name, Ink.TEXT, R.color.widget_on_surface),
            Paint(ids.time, Ink.TEXT, R.color.widget_on_surface),
            Paint(STRIP_MARKS.getValue(slot).second, Ink.IMAGE, R.drawable.widget_underline),
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
        ) + ROW_PAINTS + ROW_UNDERLINE_PAINTS + CHIP_PAINTS,
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

    /**
     * The time with its am or pm (any letters: "pm", "م", "PM") set smaller, so the digits lead (Ali, 3 Nov 2026: "make the
     * am/pm small text"). [emphasis] adds bold. A 24 hour clock has no letters and comes back unchanged.
     */
    private fun timeText(text: String, emphasis: Boolean): CharSequence = SpannableString(text).apply {
        if (emphasis) setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        var i = 0
        while (i < length) {
            if (text[i].isLetter()) {
                var j = i
                while (j < length && text[j].isLetter()) j++
                setSpan(android.text.style.RelativeSizeSpan(MERIDIEM_SCALE), i, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                i = j
            } else {
                i++
            }
        }
    }

    private const val MERIDIEM_SCALE = 0.7f

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
