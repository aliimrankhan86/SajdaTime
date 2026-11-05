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
import android.view.Gravity
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
 * **Several layouts per widget since 18 Oct 2026 (the hero countdown design), a ladder of steps since 4 Nov 2026.** On Android 12 and later the
 * launcher is handed all of them and picks by the widget's size without waking the app ([nextSizes], [todaySizes]): Next prayer from a one-line
 * form up to the wide card, Today from 2 compact rows up to the large card. Below 12 only one is used: the short Next card, the compact Today list. The 1.4.0 widgets had one fixed
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
     * Next prayer, a ladder of steps (Android 12 and later; the launcher uses the largest entry that fits inside the widget, and the smallest when
     * none does, which is why the entry widths are under the minimum widths): TINY (name and time, one line), SHORT (town chip, no day bars),
     * the full small card with the five bars, and the wide card (countdown beside the day). Heights are what each form needs, measured with
     * AppWidgetHostView and an unspecified height (WidgetRenderEvidenceTest); every_slot_height_fits is run by release-1.7.0.sh and checks the HEIGHT of every slot from 40 to 260dp, in
     * English, Urdu, Arabic and Bengali at seven text sizes, exact alarms on and off (not centring, order or width: see next_card_pictures).
     */
    private fun nextBarsHeight(context: Context): Float {
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        val scale = context.resources.configuration.fontScale
        // Measured (every_slot_height_fits, 4 Nov 2026) needs of the full card (it is sized to the widest of the countdown and the time): 141.3 and 142.9 (en 1.1, 1.15), 151.2 (en 1.5), 167.6 (en 2.0),
        // 158.9 (ur 1.5), 179 (ur, ar 2.0), 170.3 (bn 2.0), each plus about 4dp; Arabic with exact alarms off (the clock time in the big place,
        // its letters taller than digits) needs 158.9 at 1.0x, 168 at 1.3x, 175.6 at 1.5x, 195.8 at 2.0x (every_slot_height_fits, 5 Nov 2026).
        return when {
            scale >= 1.6f -> if (tall) 202f else 172f
            scale >= 1.4f -> if (tall) 181f else 156f
            scale >= 1.1f -> if (tall) 172f else 148f
            else -> if (tall) 164f else 140f
        }
    }

    private fun nextSizes(context: Context): List<Pair<SizeF, Int>> {
        val wideHeight = wideNextHeight(context)
        // Three entries of the small layout (Opus review, 4 Nov 2026: a launcher's one row is 54 to 79dp on Xiaomi and Samsung, far under
        // the 97dp the short card needs, and the list had no step below 110): the TINY one (name and time on one line, nothing cut in
        // 40dp), the SHORT one (town chip, no day bars) from [nextShortHeight], and the full one with the five bars from [nextBarsHeight].
        // The launcher uses the largest entry that fits, so each step is only used when the slot holds it.
        return listOf(
            SizeF(NEXT_ENTRY_WIDTH_DP, TINY_NEXT_DP) to R.layout.widget_next_prayer,
            SizeF(NEXT_ENTRY_WIDTH_DP, nextShortHeight(context)) to R.layout.widget_next_prayer,
            SizeF(NEXT_ENTRY_WIDTH_DP, nextBarsHeight(context)) to R.layout.widget_next_prayer,
            SizeF(270f, wideHeight) to R.layout.widget_next_prayer_wide,
        )
    }

    /**
     * Widths of the entries below the wide one. Deliberately UNDER the widgets' minimum widths (180 and 250dp) by more than a host's default
     * padding (8dp a side): when no entry fits the slot Android uses the smallest card, so a launcher that reports 245dp for a four cell widget,
     * or a host that takes 16dp of padding, would otherwise have got the one-line card or two rows (found 4 Nov 2026).
     */
    private const val NEXT_ENTRY_WIDTH_DP = 160f
    private const val TODAY_ENTRY_WIDTH_DP = 226f

    /** What the tiny line shows: its size, which extras ride after the time, and whether the mark is dropped to make room. */
    private class TinyPlan(val sp: Float, val extras: List<String>, val hideGlyph: Boolean)

    /**
     * Plans the tiny card's one line so it always fits the NARROWEST real slot (a 180dp widget in a host with 8dp of padding a side leaves about
     * 140dp of row): first the largest size from [capSp] down to 10sp with every extra ("Tomorrow", the approximate marker, the Ramadan word),
     * then with fewer extras (the last goes first), then without the 22dp mark. The name is measured with the time and extras because the name
     * keeps its width and the time takes what is left: a line that does not fit would lose its end, not show an ellipsis (Opus final review, 5 Nov
     * 2026). Floor 10sp, below the 12sp widget floor on purpose and only here: at 1.5x text and more a 40dp slot (a 70n-30 launcher's one row)
     * cannot hold more.
     */
    private fun tinyPlan(context: Context, next: NextPrayer, extras: List<String>, capSp: Float): TinyPlan {
        val metrics = context.resources.displayMetrics
        val name = next.slot.label(context)
        val time = TimeFormat.clock(context, next.at)
        val paint = android.graphics.Paint().apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        fun fits(sp: Float, shown: List<String>, glyph: Boolean): Boolean {
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics)
            val line = name + "  " + time + (if (shown.isEmpty()) "" else " · " + shown.joinToString(" · "))
            val room = (140f - (if (glyph) 22f else 0f)) * metrics.density
            return paint.measureText(line) <= room
        }
        val top = minOf(NAME_SP, capSp).coerceAtLeast(10f)
        for (glyph in listOf(true, false)) for (count in extras.size downTo 0) {
            val shown = extras.take(count)
            var sp = top
            while (sp >= 10f) {
                if (fits(sp, shown, glyph)) return TinyPlan(sp, shown, !glyph)
                sp -= 0.5f
            }
        }
        return TinyPlan(10f, emptyList(), true)
    }

    /** The one-line next prayer card (no town chip, no countdown): the height it is drawn for. */
    private const val TINY_NEXT_DP = 40f

    /**
     * Height from which the SHORT small card (town chip, no day bars) is used; below it the tiny one. Measured needs of the short card
     * (padding 6dp, or 2dp in tall scripts and from 1.3x): 96.8 (en 1.0), 100.2 (1.15), 96.8 (1.3), 97.9 (ur 1.3), 105.1 (ur 1.5),
     * 119.2 (en 2.0), each plus about 3dp; Arabic with exact alarms off (letters taller than digits in the big time) needs 109 at 1.3x, 116.2
     * at 1.5x and 134.9 at 2.0x (every_slot_height_fits, 5 Nov 2026).
     */
    private fun nextShortHeight(context: Context): Float {
        val scale = context.resources.configuration.fontScale
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return when {
            scale >= 1.6f -> if (tall) 140f else 124f
            scale >= 1.4f -> if (tall) 121f else 110f
            scale >= 1.1f -> if (tall) 112f else 104f
            else -> if (tall) 102f else 100f
        }
    }

    /**
     * Today: the compact list from its minimum, the large card (date, countdown, the day) from its measured height ([largeTodayHeight])
     * tall, which is what the header, the card and six rows need at the normal text size (rendered).
     */
    private fun todaySizes(context: Context): List<Pair<SizeF, Int>> {
        // The large card's height is measured per text size and script (largeTodayHeight); the old 3 Nov figures here (296, 322, 340) are gone.
        val largeHeight = largeTodayHeight(context)
        // The first compact entry shows as many rows as 180dp holds; from the second (the height six rows need, see
        // [compactSixRowsHeight]) all six show. Same layout, so todayViews is given the size.
        val steps = compactSteps(context)
        val six = steps.last().second
        // Steps for 3, 4, 5 and 6 compact rows (Opus review, 4 Nov 2026: the provider floor is 240dp, not the 290 the old single entry
        // assumed, so a 250dp slot cut its sixth row): each is used only when the slot holds it, and compactSlots shows that many rows.
        return steps.map { (_, h) -> SizeF(TODAY_ENTRY_WIDTH_DP, h) to R.layout.widget_today } +
            (SizeF(TODAY_ENTRY_WIDTH_DP, maxOf(largeHeight, six + 1f)) to R.layout.widget_today_large)
    }

    /** Height the large Today card is offered from: measured need at this text size and script, plus 5dp (WidgetRenderEvidenceTest#release_entry_needs). */
    internal fun largeTodayHeight(context: Context): Float {
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return interpolate(context.resources.configuration.fontScale, if (tall) LARGE_TODAY_TALL_NEED else LARGE_TODAY_NEED) + 5f
    }

    /** Height the wide Next card is offered from: measured need plus 5dp. Only offered below 1.3x text (see [nextForText]). */
    internal fun wideNextHeight(context: Context): Float {
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return interpolate(context.resources.configuration.fontScale, if (tall) WIDE_NEXT_TALL_NEED else WIDE_NEXT_NEED) + 5f
    }

    // Measured 4 Nov 2026 (release_entry_needs: every language, six times of day, Ramadan; the maximum of each text size). Between the
    // measured sizes the need is interpolated, so an in-between text size (an OEM step) is never left short. English; Urdu and Arabic together.
    private val LARGE_TODAY_NEED = listOf(1.0f to 401f, 1.1f to 416f, 1.15f to 425.5f, 1.25f to 438.1f, 1.3f to 447.2f, 1.5f to 477.3f, 1.6f to 492f)
    private val LARGE_TODAY_TALL_NEED = listOf(1.0f to 434f, 1.1f to 452f, 1.15f to 462f, 1.25f to 484f, 1.3f to 492f, 1.5f to 534f, 1.6f to 552f)
    private val WIDE_NEXT_NEED = listOf(1.0f to 214.1f, 1.1f to 225.5f, 1.15f to 230.1f, 1.25f to 241.5f, 1.28f to 248.4f, 1.3f to 252f)
    private val WIDE_NEXT_TALL_NEED = listOf(1.0f to 214.9f, 1.1f to 221f, 1.15f to 227.8f, 1.25f to 243.8f, 1.28f to 250.7f, 1.3f to 255f)

    private fun interpolate(x: Float, knots: List<Pair<Float, Float>>): Float {
        if (x <= knots.first().first) return knots.first().second
        for (i in 1 until knots.size) {
            val (x1, y1) = knots[i]
            if (x <= x1) {
                val (x0, y0) = knots[i - 1]
                return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
            }
        }
        return knots.last().second
    }

    private val TALL_SCRIPT_LANGUAGES = setOf("ur", "ar", "bn")

    /**
     * What each compact Today step needs, per text size [COMPACT_SCALES]: rows 3, 4, 5, 6; English, then Urdu, Arabic and Bengali (the
     * maximum of the three). Measured 4 Nov 2026 by WidgetRenderEvidenceTest#compact_rows_need (evening, tomorrow's countdown, the
     * worst case); the entry is the need plus 3dp, interpolated between the sizes.
     */
    private val COMPACT_SCALES = floatArrayOf(1.0f, 1.1f, 1.15f, 1.25f, 1.3f, 1.5f, 1.6f, 2.0f)
    private val COMPACT_NEED_EN = mapOf(
        2 to floatArrayOf(131.8f, 135.6f, 138.3f, 142.1f, 145.5f, 155.8f, 160.4f, 178.7f),
        3 to floatArrayOf(171.8f, 177.5f, 181.3f, 186.3f, 190.9f, 205.0f, 211.0f, 235.0f),
        4 to floatArrayOf(211.8f, 219.4f, 224.4f, 230.5f, 236.2f, 254.1f, 261.7f, 291.4f),
        5 to floatArrayOf(251.8f, 261.3f, 267.4f, 274.7f, 281.5f, 303.2f, 312.4f, 347.8f),
        6 to floatArrayOf(285.7f, 297.1f, 304.4f, 312.8f, 320.8f, 346.3f, 357.0f, 398.1f),
    )
    private val COMPACT_NEED_TALL = mapOf(
        2 to floatArrayOf(131.0f, 135.6f, 138.3f, 144.4f, 148.6f, 159.6f, 165.0f, 186.3f),
        3 to floatArrayOf(170.7f, 177.5f, 181.3f, 189.7f, 195.4f, 210.7f, 217.9f, 246.5f),
        4 to floatArrayOf(210.3f, 219.4f, 224.4f, 235.0f, 242.3f, 261.7f, 270.9f, 306.7f),
        5 to floatArrayOf(249.9f, 261.3f, 267.4f, 280.4f, 289.1f, 312.8f, 323.8f, 366.9f),
        6 to floatArrayOf(283.4f, 297.1f, 304.4f, 319.6f, 329.9f, 357.7f, 370.7f, 421.0f),
    )

    /** The compact steps: rows to the height they are offered from, ascending (3, 4, 5, 6). */
    private fun compactSteps(context: Context): List<Pair<Int, Float>> {
        val scale = context.resources.configuration.fontScale
        val table = if (AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES) COMPACT_NEED_TALL else COMPACT_NEED_EN
        return (2..6).map { rows -> rows to interpolate(scale, COMPACT_SCALES.toList().zip(table.getValue(rows).toList())) + 3f }
    }

    private fun compactSixRowsHeight(context: Context): Float = compactSteps(context).last().second

    private fun compactSlots(context: Context, state: WidgetState, size: SizeF): List<PrayerSlot> {
        // Below Android 12 only the first entry exists and its real height is unknown, so all six rows stay (as before 3 Nov 2026).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || size.height >= compactSixRowsHeight(context) - 0.5f) return PrayerSlot.entries
        val steps = compactSteps(context)
        val count = steps.lastOrNull { it.second <= size.height + 0.5f }?.first?.coerceAtMost(5) ?: 2
        val all = PrayerSlot.entries
        val start = state.current?.let(all::indexOf) ?: state.next?.takeIf { !it.isTomorrow }?.let { all.indexOf(it.slot) } ?: (all.size - count)
        return all.subList(start.coerceAtMost(all.size - count), (start.coerceAtMost(all.size - count)) + count)
    }

    /**
     * From this text size (1.3x) the wide Next card is dropped (its rows no longer fit beside the countdown: [nextForText]); every small step
     * stays. The large Today card is still offered up to [LARGE_TEXT_NO_CARD] when the slot holds it, with the compact list as the fallback.
     * The font scale is read at each redraw, so a change reaches the widget at the next one (the next prayer, midnight, or opening the app).
     */
    private const val LARGE_TEXT = 1.3f

    /** From this text size the large Today card is not offered at all (it wants about 470dp); the compact list keeps all six rows. */
    private const val LARGE_TEXT_NO_CARD = 1.6f

    fun nextPrayer(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        // Android 11 and below take ONE layout, the short card (the pre-12 provider file asks for a 110dp slot): index 1, never the tiny one.
        return responsive(nextForText(context, nextSizes(context)), preSIndex = 1) { layout, size -> nextViews(context, state, now, layout, size) }
    }

    /** Test hook (WidgetRenderEvidenceTest): the compact Today card showing exactly [rows] rows (3 to 6), whatever the slot. */
    internal fun todayCompactRows(base: Context, state: WidgetState, now: Instant, rows: Int): RemoteViews {
        val context = AppLocale.wrap(base)
        return todayViews(context, state, now, R.layout.widget_today, SizeF(250f, compactSteps(context).first { it.first == rows }.second))
    }

    fun today(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        // At large text the large card is dropped (from LARGE_TEXT_NO_CARD, 1.6x, it is not offered at all) but every compact entry stays, so a tall widget keeps all six rows.
        val sizes = todaySizes(context).let { all ->
            if (context.resources.configuration.fontScale >= LARGE_TEXT_NO_CARD) all.filter { it.second == R.layout.widget_today } else all
        }
        return responsive(sizes) { layout, size -> todayViews(context, state, now, layout, size) }
    }

    /**
     * Today strip, 4 cells by 1. The launcher picks a size entry, and the renderer keeps five
     * columns only when their text fits at the shared type size.
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
            SizeF(350f, five), SizeF(350f, tall), SizeF(350f, STRIP_HERO_HEIGHT_DP),
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
    // 14sp, not 16 (Ali, 4 Nov 2026: the strip text read a little large next to the highlight).
    private const val STRIP_MAX_SP = 14f
    // 12sp floor (Ali, 4 Nov 2026): at 14sp the five columns did not fit 380dp in Urdu and the strip fell to one prayer.
    private const val STRIP_MIN_SP = 12f


    /** Below Android 12 a launcher takes one RemoteViews, so it gets the first entry (the five column thin one) at every size. */
    fun strip(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RemoteViews(stripSizes(context).associateWith { stripViews(context, state, now, it) })
        } else {
            stripViews(context, state, now, stripSizes(context).first())
        }
    }

    /** At large text the wide Next card (rows beside the countdown) is dropped; every small step stays, so a tall slot still shows the town and the countdown. */
    private fun nextForText(context: Context, sizes: List<Pair<SizeF, Int>>) =
        if (context.resources.configuration.fontScale >= LARGE_TEXT) sizes.filter { it.second != R.layout.widget_next_prayer_wide } else sizes

    private fun responsive(sizes: List<Pair<SizeF, Int>>, preSIndex: Int = 0, build: (Int, SizeF) -> RemoteViews): RemoteViews =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sizes.size > 1) {
            RemoteViews(sizes.associate { (size, layout) -> size to build(layout, size) })
        } else {
            val entry = sizes[preSIndex.coerceAtMost(sizes.lastIndex)]
            build(entry.second, entry.first)
        }

    private fun nextViews(context: Context, state: WidgetState, now: Instant, @LayoutRes layout: Int, size: SizeF): RemoteViews {
        val views = themedViews(context, state, layout)
        val wide = layout == R.layout.widget_next_prayer_wide
        applyDirection(context, views)
        applyTap(context, views)
        // The short small layout (from nextShortHeight, about 100dp): no day bars, a 26 instead of 36 countdown and 6dp (2dp in tall
        // scripts and from 1.3x) top and bottom padding instead of 12, so it needs 96.8 to 105dp (measured) and the town chip is not cut. Padding is written on EVERY render: the entries share one layout, so the launcher reapplies actions onto
        // the view it already has and a value left from the other entry would stay.
        val tiny = !wide && size.height < nextShortHeight(context)
        val short = !wide && !tiny && size.height < nextBarsHeight(context)
        // Urdu, Arabic and Bengali sit taller: the short variant wanted 111.2dp (115.4 at 1.15x) with the Latin 32 and 8, so
        // they get a 22 countdown, 2dp padding and a 16sp name (measured needs in nextShortHeight).
        val tallScript = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        val padV = if (tiny) 2f else if (short) (if (tallScript || context.resources.configuration.fontScale >= LARGE_TEXT) 2f else 6f) else 12f
        if (!wide) views.setViewPadding(R.id.widget_root, dp(context, 8f), dp(context, padV), dp(context, 8f), dp(context, padV))
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
            for (id in HERO_IDS) if (id != R.id.widget_name && id != R.id.widget_glyph) views.setViewVisibility(id, View.GONE)
            if (wide) {
                views.setViewVisibility(R.id.widget_divider, View.GONE)
                for (slot in PrayerSlot.entries) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            }
            views.setViewVisibility(R.id.widget_chip_row, View.VISIBLE)
            views.setViewVisibility(R.id.widget_spacer, View.VISIBLE)
            views.setInt(R.id.widget_root, "setGravity", Gravity.TOP or Gravity.START)
            views.setInt(R.id.widget_title_row, "setGravity", Gravity.START or Gravity.CENTER_VERTICAL)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }

        val baseDp = if (wide) WIDE_COUNTDOWN_DP else if (short || tiny) (if (tallScript) SHORT_TALL_COUNTDOWN_DP else SHORT_COUNTDOWN_DP) else SMALL_COUNTDOWN_DP
        // What the big number will show: the countdown, or the clock time when exact alarms are off. Sized to the widest of it, never to the
        // clock time alone ("3:14:59" is wider than "12:15"), and in the wide card to its own column, not the whole width.
        // Both the countdown and the clock time can take the big place (the time when exact alarms are off), so the size fits the widest of the
        // two: sizing to the shorter clock time alone made the full card 20dp taller than measured (Opus re-review, 4 Nov 2026).
        val heroTexts = listOf(COUNTDOWN_TEMPLATE, TimeFormat.clock(context, next.at))
        // The spare height goes to the time, not to an empty band (owner, 4 Nov 2026: "wasting too much space").
        val heroDp = when {
            wide -> heroDp(context, size, baseDp, heroTexts, roomDp = wideColumnDp(size.width), grow = false)
            short || tiny -> baseDp
            else -> heroDp(context, size, baseDp, heroTexts)
        }
        fillHero(context, views, state, next, now, heroDp, sky = true)
        // Written back: a recycled view that showed the no location message had the name at 14sp and two lines.
        // Arabic and Urdu names are 30dp tall at 18sp: on the 104dp default the chip's foot was cut (emulator, 4 Nov 2026), so 16sp there.
        views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, if (short && tallScript) NAME_SP - 2f else NAME_SP)
        views.setInt(R.id.widget_name, "setMaxLines", 1)
        if (short || tiny) views.setViewVisibility(R.id.widget_progress, View.GONE)
        // The tiny card: the title row alone, name and the time at its end, bigger. The chip row, the countdown and the big time are
        // hidden; written both ways because the launcher reuses the view when the slot grows.
        views.setViewVisibility(R.id.widget_chip_row, if (tiny) View.GONE else View.VISIBLE)
        // The tiny line's size: the largest up to 18sp whose drawn height stays about 26dp (20 in the tall scripts), so the line keeps the same
        // drawn size from 1.4x text instead of shrinking below the 1.0x one; then narrowed until "name time" fits the width.
        val scaleNow = context.resources.configuration.fontScale
        val tinyExtras = listOfNotNull(
            if (next.isTomorrow) context.getString(R.string.widget_tomorrow) else null,
            if (state.nextIsApproximate) context.getString(R.string.notif_approximate)
            else state.ramadanWordFor(next.slot, state.ramadanOnNextDay)?.let { wordText(context, it) },
        )
        val plan = if (tiny) tinyPlan(context, next, tinyExtras, (if (tallScript) 20f else 26f) / scaleNow) else null
        val tinySp = plan?.sp ?: NAME_SP
        views.setTextViewTextSize(R.id.widget_time, TypedValue.COMPLEX_UNIT_SP, if (tiny) tinySp else TIME_SP)
        if (tiny) views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, tinySp)
        // The line sits in the middle of a slot taller than itself (a Samsung row is 79dp); every other form starts at the top.
        views.setInt(R.id.widget_root, "setGravity", if (tiny) Gravity.CENTER_VERTICAL else (Gravity.TOP or Gravity.START))
        // The weighted spacer would take all the spare height and leave nothing to centre (Opus re-review): gone in the one-line form.
        views.setViewVisibility(R.id.widget_spacer, if (tiny) View.GONE else View.VISIBLE)
        if (tiny) {
            views.setViewVisibility(R.id.widget_time, View.VISIBLE)
            views.setViewVisibility(R.id.widget_countdown, View.GONE)
            views.setViewVisibility(R.id.widget_hero_time, View.GONE)
            // What the sublabel would say rides after the time, as many as fit (tinyPlan); the mark goes first when there is no room.
            val timeWithContext = android.text.SpannableStringBuilder(timeText(TimeFormat.clock(context, next.at), false))
            if (plan!!.extras.isNotEmpty()) timeWithContext.append(" · ").append(plan.extras.joinToString(" · "))
            if (plan.hideGlyph) views.setViewVisibility(R.id.widget_glyph, View.GONE)
            views.setTextViewText(R.id.widget_time, timeWithContext)
        }
        // Right to left, small card (Ali, 4 Nov 2026: "balance it"): the countdown or time hung off the right edge and left the left 60 percent of
        // the one-row card empty, so it read wider than the English one. Centred there; English keeps its start edge. NOTE (Opus re-review): the title
        // row (name and the small time) is centred only when the small time is hidden (exact alarms off); while it shows it takes the spare width,
        // so the name stays at the start. That is what Ali saw and approved. Written both ways because the launcher reuses the view.
        if (!wide) {
            val centre = AppLocale.of(context).layoutDirection == View.LAYOUT_DIRECTION_RTL
            val gravity = if (centre) Gravity.CENTER_HORIZONTAL else Gravity.START
            views.setInt(R.id.widget_title_row, "setGravity", gravity or Gravity.CENTER_VERTICAL)
            views.setInt(R.id.widget_countdown, "setGravity", gravity)
            views.setInt(R.id.widget_hero_time, "setGravity", gravity)
        }
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
        else views.setViewVisibility(R.id.widget_compact_countdown, View.GONE)

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
        // The compact card has no countdown line: the location chip alone (Ali, 4 Nov 2026: "we don't need" the next
        // prayer and its countdown here). Hidden every time, because a launcher may reuse the view.
        views.setViewVisibility(R.id.widget_compact_countdown, View.GONE)

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
        val today = state.today
        val next = state.next
        val textSp = today?.let { stripTextSp(context, state, it, size.width) }
        val large = context.resources.configuration.fontScale >= LARGE_TEXT ||
            size.width < STRIP_HALF_WIDTH_DP || size.height < stripFiveHeight(context) || textSp == null
        // The strip gives the available width to the prayer times.
        views.setViewVisibility(R.id.widget_place_chip, View.GONE)
        if (!state.hasLocation || today == null || next == null) {
            for (slot in WidgetState.PRAYERS) views.setViewVisibility(ROWS.getValue(slot).row, View.GONE)
            // widget_message lives inside the row, which a thin or large text entry hides when there is a location: written both ways.
            views.setViewVisibility(R.id.widget_strip_row, View.VISIBLE)
            views.setViewVisibility(R.id.widget_strip_logo, View.VISIBLE)
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
        views.setViewVisibility(R.id.widget_strip_logo, if (hero) View.GONE else View.VISIBLE)
        if (textSp != null) for (slot in WidgetState.PRAYERS) {
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
                large -> nextLineSp(context, size.width, size.height, next, today)
                else -> 14f
            }
            views.setTextViewTextSize(R.id.widget_name, TypedValue.COMPLEX_UNIT_SP, sp)
            views.setTextViewTextSize(R.id.widget_time, TypedValue.COMPLEX_UNIT_SP, sp)
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
            // One cue for the current prayer: its filled pill (with the bold text below). Plain cells have no outline or
            // fill, so the five columns read as one row with equal gaps (owner, 4 Nov 2026: "not clean", "gaps not consistent").
            if (highlighted) {
                views.setInt(ids.row, "setBackgroundResource", skin(state, R.drawable.widget_strip_current))
            } else {
                views.setInt(ids.row, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
            }
            views.setImageViewResource(ids.glyph, glyph(slot))
            views.setViewVisibility(ids.glyph, if (glyphs) View.VISIBLE else View.GONE)
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
     * The next prayer's time at a size that uses the height left over by the header, the town chip and the day bars,
     * never past what fits the width. Falls back to [baseDp] when the widget is no taller than the content needs.
     */
    private fun heroDp(context: Context, size: SizeF, baseDp: Float, texts: List<String>, roomDp: Float = size.width - 28f, grow: Boolean = true): Float {
        val metrics = context.resources.displayMetrics
        val spare = size.height - NEXT_CHROME_DP
        var dp = if (grow) minOf(baseDp + spare * 0.8f, baseDp * 2.4f) else baseDp
        val paint = android.graphics.Paint().apply { typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL) }
        val room = roomDp * metrics.density
        val floor = if (grow) baseDp else HERO_FLOOR_DP
        while (dp > floor) {
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics)
            if (texts.all { paint.measureText(it) <= room }) return dp
            dp -= 1f
        }
        return floor
    }

    /** The widest countdown the Chronometer shows ("h:mm:ss", up to two hour digits): the size is chosen so it never wraps or is cut (Opus review, 4 Nov 2026). */
    private const val COUNTDOWN_TEMPLATE = "10:12:34"

    /** The smallest time size the wide Next card's countdown column may shrink to. */
    private const val HERO_FLOOR_DP = 16f

    /** Width of the countdown column of the wide Next card: the layout weighs it 1 against 1.3 for the rows, minus the card's padding and the divider. */
    private fun wideColumnDp(widthDp: Float): Float = (widthDp - 28f - 8f) / 2.3f - 4f

    /** Height the header, the town chip and the day bars take on the next prayer card, from the measured layouts. */
    private const val NEXT_CHROME_DP = 100f

    /**
     * The next prayer alone on a short strip at large text. It grows with the cell's height so the line does not float
     * in an empty box (owner, 4 Nov 2026: "fix it"), but never past what fits the width: the largest size from 14 to 22sp
     * at which "Fajr 5:07 am Tomorrow" stays on one line.
     */
    private fun nextLineSp(context: Context, widthDp: Float, heightDp: Float, next: NextPrayer, today: DayPrayerTimes): Float {
        val metrics = context.resources.displayMetrics
        val line = listOfNotNull(
            next.slot.label(context),
            TimeFormat.clock(context, next.at),
            if (next.isTomorrow) context.getString(R.string.widget_tomorrow) else null,
        ).joinToString(" ")
        val paint = android.graphics.Paint().apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        var sp = (heightDp * 0.22f).coerceIn(14f, 22f)
        while (sp > 14f) {
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics)
            if (paint.measureText(line) <= (widthDp - 16f) * metrics.density) return sp
            sp -= 0.5f
        }
        return 14f
    }

    /** One size for all columns, or the single prayer layout when the full text needs more room. */
    private fun stripTextSp(context: Context, state: WidgetState, today: DayPrayerTimes, widthDp: Float): Float? {
        val metrics = context.resources.displayMetrics
        val column = ((widthDp - 8f - 20f) / WidgetState.PRAYERS.size - 4f) * metrics.density
        val texts = WidgetState.PRAYERS.flatMap { slot ->
            val label = when (state.ramadanWordFor(slot, state.ramadanToday)) {
                RamadanWord.IFTAR -> context.getString(R.string.widget_iftar)
                RamadanWord.SUHOOR_ENDS -> context.getString(R.string.widget_strip_suhoor)
                null -> slot.label(context)
            }
            listOf(label, TimeFormat.clock(context, today[slot]))
        }
        val paint = android.graphics.Paint().apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
        var sp = STRIP_MAX_SP
        while (sp >= STRIP_MIN_SP) {
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics)
            if (texts.maxOf { paint.measureText(it) } <= column) return sp
            sp -= 0.5f
        }
        return null
    }

    private fun rowSurface(state: WidgetState, highlighted: Boolean): Int =
        skin(state, if (highlighted) R.drawable.widget_row_highlight else R.drawable.widget_row_plain)

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
        views.setImageViewResource(R.id.widget_glyph, if (sky) R.drawable.widget_mark else glyph(next.slot))
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
            views.setInt(ids.row, "setBackgroundResource", rowSurface(state, highlighted))
            if (!band) views.setViewVisibility(ids.underline, View.GONE)
            val side = dp(context, bandSideDp)
            // 6dp above and below each prayer: the rows read as full bars, not squashed lines (Ali, 4 Nov 2026: too cramped at 0;
            // 4 Nov 2026 Redmi check: 4dp still looked squashed at 16sp). Six of these plus 6dp gaps fit the 290dp minimum.
            // Urdu, Arabic and Bengali lines are about 27dp tall at 15sp (emulator, 4 Nov 2026: Arabic Isha row cut at the card's foot
            // with 6dp), so their rows take 3dp and the highlight outline always has the room to draw.
            val padV = dp(context, if (AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES) 3f else 6f)
            views.setViewPadding(ids.row, side, padV, side, padV)
            // The mark is its own view (the strip's widget_logo_*), never the glyph view with a filter or tint removed: under a theme
            // chosen in the app the glyph carries a colour filter that turned the mark into a flat grey tile (seen on the emulator).
            views.setImageViewResource(ids.glyph, glyph(slot))
            ROW_LOGOS[slot]?.let { logo ->
                views.setViewVisibility(ids.glyph, if (highlighted) View.GONE else View.VISIBLE)
                views.setViewVisibility(logo, if (highlighted) View.VISIBLE else View.GONE)
            }
            // Same weight and size for every row (Ali, 4 Nov 2026: the bold current row read bigger than the rest).
            // The current prayer is shown by its fill, its border and the "Now" word.
            views.setTextViewText(ids.name, name)
            views.setTextViewText(ids.time, timeText(time, emphasis = false))

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

    /** The small time in the title row beside the name (the layout's own 14sp). */
    private const val TIME_SP = 14f
    private const val MESSAGE_SP = 14f

    /** The Next prayer widgets' prayer name, 16sp in both layouts; rewritten after the message size. */
    private const val NAME_SP = 18f

    /**
     * The countdown sizes, in dp, judged from renders at each layout's smallest size. At the small Next
     * widget's 110dp minimum the short variant hides the day bars and uses [SHORT_COUNTDOWN_DP] (measured 3 Nov 2026: 105.9dp
     * needed). From 140dp the bars show; One UI gives about 158dp and shows everything.
     */
    private const val SMALL_COUNTDOWN_DP = 36f
    // Ali, 4 Nov 2026 ("make it smaller"): the one-row Medium is about 103dp on the emulator, so the short countdown
    // steps down from 32 to 26 (22 in tall scripts) to fit under 100dp. Measure again on a phone before changing.
    private const val SHORT_COUNTDOWN_DP = 26f
    private const val SHORT_TALL_COUNTDOWN_DP = 22f
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
    /** The app mark that stands in for a prayer's glyph on the current row of the larger layouts. */
    private val ROW_LOGOS = mapOf(
        PrayerSlot.FAJR to R.id.widget_logo_fajr,
        PrayerSlot.DHUHR to R.id.widget_logo_dhuhr,
        PrayerSlot.ASR to R.id.widget_logo_asr,
        PrayerSlot.MAGHRIB to R.id.widget_logo_maghrib,
        PrayerSlot.ISHA to R.id.widget_logo_isha,
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
        R.drawable.widget_row_plain to (R.drawable.widget_row_plain_light to R.drawable.widget_row_plain_dark),
        R.drawable.widget_row_highlight to (R.drawable.widget_row_highlight_light to R.drawable.widget_row_highlight_dark),
        R.drawable.widget_strip_current to (R.drawable.widget_strip_current_light to R.drawable.widget_strip_current_dark),
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
            Paint(R.id.widget_compact_countdown, Ink.TEXT, R.color.widget_on_surface),
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
        ) + heroPaints(R.color.widget_primary, R.color.widget_on_surface) +
            Paint(R.id.widget_glyph, Ink.TINT, R.color.widget_primary) + ROW_PAINTS + CHIP_PAINTS,
        R.layout.widget_strip to listOf(
            ROOT_PAINT,
            Paint(R.id.widget_message, Ink.TEXT, R.color.widget_on_surface),
            // The large text row: the next prayer alone (the ids are the hero's, in a row of their own).
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
