package com.sajdatime.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
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
import java.time.Duration
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
 * launcher is handed all of them and picks by the widget's size without waking the app ([nextSizes], [todayForms]): Next prayer from a one-line
 * form up to the wide card, Today from its list form (1 to 6 rows) up to the card (since 1.7.1 one layout, [todayForms]). Below 12 only one is used: the short Next card, the six row Today list. The 1.4.0 widgets had one fixed
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
     * Today, one layout in two forms since 1.7.1 (Ali approved the mockup 24 Nov 2026; [todayViews]). The LIST form (the date and the
     * town chip over one list card of 1 to 6 rows) from the provider's floor, then the CARD form (the header with the Hijri date, the
     * countdown card, then six rows, or Now, Next and the one after from [LARGE_TEXT]) from its measured height, at three widths
     * ([CARD_WIDTHS]): the narrower the slot, the sooner the town chip, the prayer's time and "until it begins" each move to a line of
     * their own. The launcher uses the fitting entry nearest the slot's size (HANDOVER §15 lesson 197).
     */
    private class TodayForm(val card: Boolean, val rows: Int, val entry: SizeF, val widthDp: Float) {
        fun atLeast(dp: Float) = if (dp > widthDp) TodayForm(card, rows, entry, dp) else this
    }

    private fun todayForms(context: Context): List<TodayForm> {
        val scale = context.resources.configuration.fontScale
        val group = needGroup(context)
        val list = LIST_WIDTHS.flatMapIndexed { i, (entryWidth, width) ->
            // From one row: at 2x text even two rows need more than the 240dp floor (Opus review, 24 Nov 2026), so the smallest
            // entry is the date and the chip over the next prayer alone.
            (1..6).map { rows ->
                val need = interpolate(scale, TODAY_SCALES.zip(LIST_NEEDS[group][i].getValue(rows).toList()))
                TodayForm(card = false, rows = rows, entry = SizeF(entryWidth, need + 3f), widthDp = width)
            }
        }
        val rows = cardRows(context)
        val cards = CARD_WIDTHS.mapIndexed { i, (entryWidth, width) ->
            // Only the text sizes that show the same number of rows (six below LARGE_TEXT, three from it): between 1.28 and 1.3 an
            // interpolation across the two would offer a six row card from a three row height (Opus review, 24 Nov 2026).
            val knots = TODAY_SCALES.zip(CARD_NEEDS[group][i].toList()).filter { (s, _) -> (s >= LARGE_TEXT) == (scale >= LARGE_TEXT) }
            val need = interpolate(scale, knots)
            TodayForm(card = true, rows = rows, entry = SizeF(entryWidth, need + rows * ROW_AIR_DP + 3f), widthDp = width)
        }
        return list + cards
    }

    /** Which table: English, the other Latin script languages (Turkish, Indonesian: longer pill words), or Urdu, Arabic and Bengali. */
    private fun needGroup(context: Context): Int = when (AppLocale.of(context).language) {
        "en" -> 0
        in TALL_SCRIPT_LANGUAGES -> 2
        else -> 1
    }

    /** Six rows on the card, or from [LARGE_TEXT] Now, Next and the prayer after (the spec's large text rule), so nothing is cut. */
    private fun cardRows(context: Context): Int = if (context.resources.configuration.fontScale >= LARGE_TEXT) 3 else 6

    /**
     * The card's widths: (entry width, the narrowest real width that entry can be drawn at). The renderer decides what fits on one
     * line at that real width, never at the slot's own (a RemoteViews entry is chosen by the launcher without asking the app, the
     * fitting entry nearest the slot's size). Most phones give a four cell widget 320 to 390dp. Fifteen entries with the list's
     * twelve, under the 16 a RemoteViews may hold.
     */
    private val CARD_WIDTHS = listOf(TODAY_ENTRY_WIDTH_DP to 245f, 300f to 300f, 350f to 350f)

    /**
     * The list's widths, the same idea: from 245dp (a four cell widget in a host that takes 8dp a side, 4 Nov 2026) the next row's pill
     * goes under "Maghrib" in English, which costs a line; from 300dp it stays beside it, so six rows still fit a 4 by 4.
     */
    private val LIST_WIDTHS = listOf(TODAY_ENTRY_WIDTH_DP to 245f, 300f to 300f)

    /**
     * Height added to each card row over its measured need: the rows are weighted, so this is where the spare height goes and what
     * makes a row about 46dp tall (the mockup). It also absorbs a header or countdown line that moves under its neighbour in a state
     * the measurement did not see (a long town name, a long gap before the next prayer), so nothing is cut.
     */
    private const val ROW_AIR_DP = 16f

    /**
     * Measured needs (WidgetRenderEvidenceTest#today_needs, 24 Nov 2026, the maximum over eight times of day including three in
     * Ramadan and the hour before Maghrib), per text size [TODAY_SCALES], for each [needGroup]: English; Turkish and Indonesian;
     * Urdu, Arabic and Bengali (the maximum of each group). The list by width ([LIST_WIDTHS]) and rows (1 to 6); the card by width
     * ([CARD_WIDTHS]) with [cardRows] rows and no air.
     */
    private val TODAY_SCALES = listOf(1.0f, 1.1f, 1.15f, 1.25f, 1.28f, 1.3f, 1.5f, 1.6f, 2.0f)
    private val LIST_NEEDS = listOf(
        // English
        listOf(
            mapOf(
                1 to floatArrayOf(128.4f, 131.8f, 133.3f, 136.8f, 138.7f, 139.0f, 149.0f, 152.8f, 166.1f),
                2 to floatArrayOf(162.7f, 167.6f, 197.3f, 204.2f, 208.0f, 208.8f, 226.7f, 233.1f, 256.0f),
                3 to floatArrayOf(197.0f, 203.4f, 233.9f, 242.3f, 246.9f, 248.0f, 269.3f, 277.3f, 305.9f),
                4 to floatArrayOf(231.2f, 239.2f, 270.5f, 280.4f, 285.7f, 287.2f, 312.0f, 321.5f, 355.8f),
                5 to floatArrayOf(265.5f, 275.0f, 307.0f, 318.5f, 324.6f, 326.5f, 354.7f, 365.7f, 405.7f),
                6 to floatArrayOf(299.8f, 310.9f, 343.6f, 356.6f, 363.4f, 365.7f, 397.3f, 409.9f, 455.6f),
            ),
            mapOf(
                1 to floatArrayOf(103.6f, 105.1f, 105.9f, 107.4f, 138.7f, 139.0f, 149.0f, 152.8f, 166.1f),
                2 to floatArrayOf(137.9f, 141.0f, 142.5f, 145.5f, 177.5f, 178.3f, 192.0f, 197.0f, 256.0f),
                3 to floatArrayOf(172.2f, 176.8f, 179.0f, 183.6f, 216.4f, 217.5f, 234.7f, 241.1f, 305.9f),
                4 to floatArrayOf(206.5f, 212.6f, 215.6f, 221.7f, 255.2f, 256.8f, 277.3f, 285.3f, 355.8f),
                5 to floatArrayOf(240.8f, 248.4f, 252.2f, 259.8f, 294.1f, 296.0f, 320.0f, 329.5f, 405.7f),
                6 to floatArrayOf(275.0f, 284.2f, 288.8f, 297.9f, 333.0f, 335.2f, 362.7f, 373.7f, 455.6f),
            ),
        ),
        // Turkish and Indonesian
        listOf(
            mapOf(
                1 to floatArrayOf(128.4f, 131.8f, 133.3f, 136.8f, 138.7f, 139.0f, 149.0f, 152.8f, 166.1f),
                2 to floatArrayOf(187.4f, 194.3f, 197.3f, 204.2f, 208.0f, 208.8f, 226.7f, 233.1f, 256.0f),
                3 to floatArrayOf(221.7f, 230.1f, 233.9f, 242.3f, 246.9f, 248.0f, 269.3f, 277.3f, 305.9f),
                4 to floatArrayOf(256.0f, 265.9f, 270.5f, 280.4f, 285.7f, 287.2f, 312.0f, 321.5f, 355.8f),
                5 to floatArrayOf(290.3f, 301.7f, 307.0f, 318.5f, 324.6f, 326.5f, 354.7f, 365.7f, 405.7f),
                6 to floatArrayOf(324.6f, 337.5f, 343.6f, 356.6f, 363.4f, 365.7f, 397.3f, 409.9f, 455.6f),
            ),
            mapOf(
                1 to floatArrayOf(103.6f, 131.8f, 133.3f, 136.8f, 138.7f, 139.0f, 149.0f, 152.8f, 166.1f),
                2 to floatArrayOf(137.9f, 167.6f, 169.9f, 204.2f, 208.0f, 208.8f, 226.7f, 233.1f, 256.0f),
                3 to floatArrayOf(172.2f, 203.4f, 206.5f, 242.3f, 246.9f, 248.0f, 269.3f, 277.3f, 305.9f),
                4 to floatArrayOf(206.5f, 239.2f, 243.0f, 280.4f, 285.7f, 287.2f, 312.0f, 321.5f, 355.8f),
                5 to floatArrayOf(240.8f, 275.0f, 279.6f, 318.5f, 324.6f, 326.5f, 354.7f, 365.7f, 405.7f),
                6 to floatArrayOf(275.0f, 310.9f, 316.2f, 356.6f, 363.4f, 365.7f, 397.3f, 409.9f, 455.6f),
            ),
        ),
        // Urdu, Arabic and Bengali
        listOf(
            mapOf(
                1 to floatArrayOf(126.5f, 129.9f, 139.8f, 144.8f, 146.3f, 147.0f, 159.2f, 163.4f, 179.8f),
                2 to floatArrayOf(157.0f, 190.5f, 195.0f, 220.2f, 223.2f, 224.8f, 247.2f, 254.5f, 283.4f),
                3 to floatArrayOf(187.4f, 222.5f, 228.2f, 259.8f, 263.2f, 265.5f, 292.2f, 301.3f, 337.5f),
                4 to floatArrayOf(217.9f, 254.5f, 261.3f, 299.4f, 303.2f, 306.3f, 337.1f, 348.2f, 391.6f),
                5 to floatArrayOf(248.0f, 286.5f, 294.1f, 339.0f, 343.2f, 347.0f, 382.1f, 395.0f, 445.7f),
                6 to floatArrayOf(278.5f, 318.5f, 327.2f, 378.7f, 383.2f, 387.8f, 427.0f, 441.9f, 499.8f),
            ),
            mapOf(
                1 to floatArrayOf(103.6f, 105.1f, 106.7f, 109.0f, 109.3f, 110.1f, 149.3f, 153.1f, 179.8f),
                2 to floatArrayOf(137.9f, 141.0f, 144.0f, 148.6f, 149.3f, 150.9f, 227.4f, 233.9f, 283.4f),
                3 to floatArrayOf(172.2f, 176.8f, 181.3f, 188.2f, 189.3f, 191.6f, 267.4f, 275.4f, 337.5f),
                4 to floatArrayOf(206.5f, 212.6f, 218.7f, 227.8f, 229.3f, 232.4f, 307.4f, 317.0f, 391.6f),
                5 to floatArrayOf(240.8f, 248.4f, 256.0f, 267.4f, 269.3f, 273.1f, 347.0f, 358.5f, 445.7f),
                6 to floatArrayOf(275.0f, 284.2f, 293.3f, 307.0f, 309.3f, 313.9f, 387.0f, 400.0f, 499.8f),
            ),
        ),
    )
    private val CARD_NEEDS = listOf(
        // English
        listOf(
            floatArrayOf(568.8f, 591.2f, 626.7f, 680.8f, 694.9f, 557.0f, 605.3f, 623.2f, 717.0f),
            floatArrayOf(502.1f, 522.7f, 532.2f, 597.3f, 608.4f, 498.3f, 539.0f, 553.9f, 688.8f),
            floatArrayOf(454.1f, 471.2f, 479.2f, 526.1f, 536.4f, 425.9f, 539.0f, 553.9f, 609.9f),
        ),
        // Turkish and Indonesian
        listOf(
            floatArrayOf(613.7f, 639.6f, 651.0f, 696.0f, 710.5f, 572.6f, 656.8f, 677.0f, 751.2f),
            floatArrayOf(502.1f, 547.0f, 557.3f, 653.7f, 667.0f, 528.8f, 601.5f, 643.8f, 723.0f),
            floatArrayOf(502.1f, 522.7f, 532.2f, 553.1f, 564.2f, 454.1f, 544.0f, 590.1f, 684.2f),
        ),
        // Urdu, Arabic and Bengali
        listOf(
            floatArrayOf(621.7f, 651.0f, 694.5f, 763.0f, 773.7f, 609.1f, 665.5f, 684.2f, 850.3f),
            floatArrayOf(548.2f, 569.5f, 619.4f, 649.5f, 657.9f, 528.0f, 622.5f, 640.0f, 762.3f),
            floatArrayOf(518.9f, 569.5f, 586.3f, 613.7f, 621.0f, 491.0f, 576.4f, 592.8f, 712.8f),
        ),
    )

    /** Height the wide Next card is offered from: measured need plus 5dp. Only offered below 1.3x text (see [nextForText]). */
    internal fun wideNextHeight(context: Context): Float {
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        return interpolate(context.resources.configuration.fontScale, if (tall) WIDE_NEXT_TALL_NEED else WIDE_NEXT_NEED) + 5f
    }

    // Measured 4 Nov 2026 (release_entry_needs: every language, six times of day, Ramadan; the maximum of each text size). Between the
    // measured sizes the need is interpolated, so an in-between text size (an OEM step) is never left short. English; Urdu and Arabic together.
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
     * From this text size (1.3x) the wide Next card is dropped (its rows no longer fit beside the countdown: [nextForText]); every small step
     * stays. The Today card shows three rows from here ([cardRows]). The font scale is read at each redraw, so a change reaches the
     * widget at the next one (the next prayer, midnight, or opening the app).
     */
    private const val LARGE_TEXT = 1.3f

    fun nextPrayer(base: Context, state: WidgetState, now: Instant): RemoteViews {
        val context = AppLocale.wrap(base)
        // Android 11 and below take ONE layout, the short card (the pre-12 provider file asks for a 110dp slot): index 1, never the tiny one.
        return responsive(nextForText(context, nextSizes(context)), preSIndex = 1) { layout, size -> nextViews(context, state, now, layout, size) }
    }

    /** Test hook (WidgetRenderEvidenceTest): one Today form by its [todayKey], whatever the slot. */
    internal fun todayForm(base: Context, state: WidgetState, now: Instant, key: String): RemoteViews {
        val context = AppLocale.wrap(base)
        return todayViews(context, state, now, todayForms(context).first { todayKey(it) == key })
    }

    /** Test hook: each Today entry by [todayKey] to (the height it is offered from, the real width its decisions assume). */
    internal fun todayEntries(base: Context): Map<String, Pair<Float, Float>> =
        todayForms(AppLocale.wrap(base)).associate { todayKey(it) to (it.entry.height to it.widthDp) }

    /** "list 300 6" (the list at the 300dp entry width, six rows), "card 350". */
    private fun todayKey(form: TodayForm): String =
        if (form.card) "card ${form.entry.width.toInt()}" else "list ${form.entry.width.toInt()} ${form.rows}"

    /**
     * [minWidthDp] is the width the launcher reports for this instance (OPTION_APPWIDGET_MIN_WIDTH, 0 when unknown). It only ever
     * widens what each form assumes when it decides whether a line fits ([TodayForm.widthDp]), so a wide slot keeps a pill beside
     * its name at large text (Fable, 24 Nov 2026). It cannot add a line, so the measured entry heights stay upper bounds.
     */
    fun today(base: Context, state: WidgetState, now: Instant, minWidthDp: Float = 0f): RemoteViews {
        val context = AppLocale.wrap(base)
        // 16dp under the reported width: a host may still pad the widget by 8dp a side (the device test's host does).
        val forms = todayForms(context).map { it.atLeast(minWidthDp - 16f) }
        // Below Android 12 a launcher takes one RemoteViews: the six row list, at every size (the pre-12 provider asks for 290dp).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return todayViews(context, state, now, forms.first { !it.card && it.rows == 6 })
        return RemoteViews(forms.associate { it.entry to todayViews(context, state, now, it) })
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
            fillRows(context, views, state, labels = false, bandSideDp = 4f)
        }

        views.setContentDescription(R.id.widget_root, withPlace(context, state, nextSentence(context, next, now)))
        return views
    }

    /**
     * The Today widget in one of its forms ([todayForms]). Everything that differs between the forms and between states is written on
     * every render, both ways, because the entries share one layout and the launcher reuses the views it already has.
     */
    private fun todayViews(context: Context, state: WidgetState, now: Instant, form: TodayForm): RemoteViews {
        val views = themedViews(context, state, R.layout.widget_today)
        applyDirection(context, views)
        applyTap(context, views)
        // The same body as the strip and the Next widget (period sky in light, the app's dark green in dark), written after the paint.
        views.setInt(R.id.widget_root, "setBackgroundResource", skin(state, stripBody(state.period)))
        // The list form keeps 8dp above and below, so six rows still fit a plain 4 by 4 (279dp on the Redmi, Ali 6 Nov 2026).
        val edge = dp(context, if (form.card) 12f else 8f)
        views.setViewPadding(R.id.widget_root, dp(context, 12f), edge, dp(context, 12f), edge)
        fillTodayHeader(context, views, state, form)

        val today = state.today
        val next = state.next
        if (!state.hasLocation || today == null || next == null) {
            // One honest message under the date, which needs no location.
            views.setViewVisibility(R.id.widget_hero, View.GONE)
            views.setViewVisibility(R.id.widget_list, View.GONE)
            views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_location))
            views.setViewVisibility(R.id.widget_message, View.VISIBLE)
            views.setViewVisibility(R.id.widget_footer, View.GONE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_location))
            return views
        }
        views.setViewVisibility(R.id.widget_message, View.GONE)
        views.setViewVisibility(R.id.widget_list, View.VISIBLE)
        views.setViewVisibility(R.id.widget_hero, if (form.card) View.VISIBLE else View.GONE)
        if (form.card) fillTodayHero(context, views, state, next, now, form.widthDp)

        val shown = if (form.card) state.cardSlots(form.rows) else listSlots(state, form.rows)
        val spoken = fillTodayRows(context, views, state, today, next, shown, form)

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

    /** The list's rows ([WidgetState.listSlots]); all six below Android 12, where the slot's height is unknown. */
    private fun listSlots(state: WidgetState, rows: Int): List<PrayerSlot> =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) PrayerSlot.entries else state.listSlots(rows)

    /**
     * The header: the date over the Hijri date (the card only), the town chip at the end. On the card the chip moves under the date
     * when the date or the Hijri date would not fit one line beside it at the form's narrowest width (the spec: "it wraps under the
     * date"). The list form's height is measured with one header line, so there the chip never moves: the date gives way instead.
     */
    private fun fillTodayHeader(context: Context, views: RemoteViews, state: WidgetState, form: TodayForm) {
        val date = state.date?.let { TimeFormat.date(context, it) }
        val hijri = if (form.card) hijriText(context, state) else null
        val place = if (state.hasLocation) place(context, state) else null
        fillChip(context, views, state)
        fillChip(context, views, state, R.id.widget_place_chip_below, R.id.widget_place_below)
        val chipDp = place?.let {
            CHIP_CHROME_DP + CHIP_ICONS_SP * context.resources.configuration.fontScale + minOf(textDp(context, it, 14f, Typeface.DEFAULT_BOLD), CHIP_PLACE_MAX_DP)
        } ?: 0f
        val textDp = listOfNotNull(date?.let { textDp(context, it, 17f, MEDIUM) }, hijri?.let { textDp(context, it, 14f) }).maxOrNull() ?: 0f
        val fits = place == null || textDp + 8f + chipDp <= form.widthDp - 24f
        val below = form.card && !fits
        views.setViewVisibility(R.id.widget_place_chip, if (place != null && !below) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_place_chip_below, if (place != null && below) View.VISIBLE else View.GONE)
        val showDate = date != null && (form.card || fits)
        views.setTextViewText(R.id.widget_date, date.orEmpty())
        views.setViewVisibility(R.id.widget_date, if (showDate) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.widget_hijri, hijri.orEmpty())
        views.setViewVisibility(R.id.widget_hijri, if (hijri != null) View.VISIBLE else View.GONE)
    }

    /** The chip's own width without the town and its icons: padding 11 and 12, the 5dp and 6dp gaps. */
    private const val CHIP_CHROME_DP = 34f

    /** The chip's pin (16sp) and arrow (14sp): in sp since 24 Nov 2026, so they grow with the town's text. */
    private const val CHIP_ICONS_SP = 30f

    /** The town's widest, the layout's maxWidth. */
    private const val CHIP_PLACE_MAX_DP = 150f

    /**
     * The countdown card: "Next prayer" (with "Tomorrow" after Isha, as the app's card), the name and its time on one line, the
     * approximate marker or the Ramadan word, then the countdown with "until it begins" beside it. The time and "until it begins" each
     * move under their neighbour when the two do not fit one line at the form's narrowest width ([widthDp]).
     *
     * The countdown stays the Chronometer the launcher ticks (24 Nov 2026, widget-review.md: dependable and no wake ups, rather than
     * the mockup's "4 min", which would need a redraw every minute). It reads "04:34", "00:45" under a minute and "1:04:34" over an hour.
     * Without exact alarms the time takes the big place instead, and "until it begins" goes, as before (see [fillHero]).
     */
    private fun fillTodayHero(context: Context, views: RemoteViews, state: WidgetState, next: NextPrayer, now: Instant, widthDp: Float) {
        val inner = widthDp - 24f - 36f
        val name = next.slot.label(context)
        val clock = TimeFormat.clock(context, next.at)
        views.setTextViewText(
            R.id.widget_hero_label,
            if (next.isTomorrow) {
                context.getString(R.string.settings_value_pair, context.getString(R.string.home_next_prayer), context.getString(R.string.widget_tomorrow))
            } else {
                context.getString(R.string.home_next_prayer)
            },
        )
        views.setTextViewText(R.id.widget_name, name)
        // A projected polar time is marked before the Ramadan word: a guess presented as a measurement is the worse mistake.
        val context2 = if (state.nextIsApproximate) {
            context.getString(R.string.notif_approximate)
        } else {
            state.ramadanWordFor(next.slot, state.ramadanOnNextDay)?.let { wordText(context, it) }
        }
        views.setTextViewText(R.id.widget_sublabel, context2.orEmpty())
        views.setViewVisibility(R.id.widget_sublabel, if (context2 != null) View.VISIBLE else View.GONE)

        views.setTextViewTextSize(R.id.widget_countdown, TypedValue.COMPLEX_UNIT_DIP, TODAY_COUNTDOWN_DP)
        views.setTextViewTextSize(R.id.widget_hero_time, TypedValue.COMPLEX_UNIT_DIP, TODAY_COUNTDOWN_DP)
        val until = context.getString(R.string.home_until)
        views.setTextViewText(R.id.widget_until, until)
        views.setTextViewText(R.id.widget_until_below, until)
        views.setTextViewText(R.id.widget_time, timeText(clock, emphasis = false))
        views.setTextViewText(R.id.widget_time_below, timeText(clock, emphasis = false))
        if (state.countdownAllowed) {
            applyCountdown(views, R.id.widget_countdown, next.at, now)
            views.setViewVisibility(R.id.widget_hero_time, View.GONE)
            val timeBeside = textDp(context, name, 24f, MEDIUM) + 10f + textDp(context, widestClock(context), 17f, tabular = true) <= inner
            views.setViewVisibility(R.id.widget_time, if (timeBeside) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_time_below, if (timeBeside) View.GONE else View.VISIBLE)
            // The Chronometer only gets shorter until the next redraw (at the prayer), so its text now is the widest it will be.
            val counting = DateUtils.formatElapsedTime(Duration.between(now, next.at).seconds.coerceAtLeast(0))
            val untilBeside = textDp(context, counting, TODAY_COUNTDOWN_DP, LIGHT, TypedValue.COMPLEX_UNIT_DIP) + 10f +
                textDp(context, until, 17f) <= inner
            views.setViewVisibility(R.id.widget_until, if (untilBeside) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_until_below, if (untilBeside) View.GONE else View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_countdown, View.GONE)
            views.setTextViewText(R.id.widget_hero_time, timeText(clock, emphasis = false))
            views.setViewVisibility(R.id.widget_hero_time, View.VISIBLE)
            for (id in listOf(R.id.widget_time, R.id.widget_time_below, R.id.widget_until, R.id.widget_until_below)) views.setViewVisibility(id, View.GONE)
        }
    }

    /** The countdown card's big number, in dp (see the layout): the mockup's 46. */
    private const val TODAY_COUNTDOWN_DP = 46f

    /**
     * The Today rows (1.7.1). The next prayer's row (today's, not tomorrow's Fajr after Isha) gets the fill with the bar on its leading
     * edge, the filled "Next" pill and the medium weight; the current prayer the quieter "Now" pill; Sunrise its dimmed fill (its
     * smaller, paler type is in the layout). A row whose name, pill and time do not fit one line puts the pill (on the card, with the
     * Ramadan word) on a second line, so no pill cuts the time; in the list the word stays beside the name and shortens, or drops out
     * when there is no room at all. A hairline sits under every shown row but the last shown. Returns one spoken phrase per row, all
     * six, for the widget's single description.
     */
    private fun fillTodayRows(
        context: Context, views: RemoteViews, state: WidgetState, today: DayPrayerTimes, next: NextPrayer, shown: List<PrayerSlot>, form: TodayForm,
    ): List<String> {
        val all = PrayerSlot.entries
        val nextSlot = state.nextToday
        val inner = form.widthDp - 24f - 8f - 28f
        val tall = AppLocale.of(context).language in TALL_SCRIPT_LANGUAGES
        // The card's rows take 4dp and share the spare height (ROW_AIR_DP); the list's keep 5dp (2dp in the taller scripts) so six fit 4 by 4.
        val padV = dp(context, if (form.card) 4f else if (tall) 2f else 5f)
        val widest = widestClock(context)
        val side = dp(context, 14f)
        // A hairline under every shown row but the last shown: the three row card leaves Sunrise out, so the hairline under Fajr
        // is the one between Fajr and Dhuhr there.
        for (i in 0 until all.lastIndex) {
            val hair = TODAY_ROWS.getValue(all[i]).hair ?: continue
            views.setViewVisibility(hair, if (all[i] in shown && all.drop(i + 1).any { it in shown }) View.VISIBLE else View.GONE)
        }
        val spoken = mutableListOf<String>()
        for (slot in all) {
            val ids = ROWS.getValue(slot)
            val extra = TODAY_ROWS.getValue(slot)
            val name = slot.label(context)
            val time = TimeFormat.clock(context, today[slot])
            val word = state.ramadanWordFor(slot, state.ramadanToday)?.let { wordText(context, it) }
            spoken += context.getString(R.string.widget_a11y_row, if (word != null) context.getString(R.string.settings_value_pair, name, word) else name, time)
            if (slot !in shown) {
                views.setViewVisibility(ids.row, View.GONE)
                continue
            }
            val isNext = slot == nextSlot
            val isNow = !isNext && state.isHighlighted(slot)
            views.setViewVisibility(ids.row, View.VISIBLE)
            views.setViewPadding(ids.row, side, padV, side, padV)
            when {
                isNext -> views.setInt(ids.row, "setBackgroundResource", skin(state, R.drawable.widget_row_next))
                slot == PrayerSlot.SUNRISE -> views.setInt(ids.row, "setBackgroundResource", skin(state, R.drawable.widget_row_sunrise))
                else -> views.setInt(ids.row, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
            }
            views.setTextViewText(ids.name, if (isNext) medium(name) else name)
            if (slot == PrayerSlot.SUNRISE) views.setTextViewText(R.id.widget_strut_sunrise, name)
            views.setTextViewText(ids.time, if (isNext) medium(timeText(time, emphasis = false)) else timeText(time, emphasis = false))

            val pill = when {
                isNext -> context.getString(R.string.home_next_marker)
                isNow -> context.getString(R.string.home_now_marker)
                else -> null
            }
            val sp = if (slot == PrayerSlot.SUNRISE) 15f else 17f
            // Measured with the widest time the clock can show, not this row's: London's "4:28 pm" fitted one line at 1.3x where
            // Karachi's "5:41 pm" did not, so heights measured in London were short in Karachi (fit test, 24 Nov 2026). With the widest
            // time the choice depends only on the language, the text size and the width, which the tables are measured for.
            // The list never gives a Ramadan word a line of its own: its heights are the year's (six rows in a 4 by 4, Ali 6 Nov 2026),
            // so in the list the word stays beside the name and shortens with an ellipsis when there is no room, as in 1.7.0; the card
            // has the air for it. A pill that does not fit moves under the name in both forms.
            val wordDp = if (form.card) word?.let { textDp(context, it, 14f) } ?: 0f else 0f
            val needDp = textDp(context, name, sp, if (isNext) MEDIUM else Typeface.DEFAULT) + textDp(context, widest, sp, tabular = true) + 16f +
                (pill?.let { 8f + 18f + textDp(context, it, 14f, MEDIUM) } ?: 0f) + wordDp
            val inline = (pill == null && (word == null || !form.card)) || needDp <= inner
            for (id in listOf(extra.pillNext, extra.pillNow, extra.pillNext2, extra.pillNow2)) views.setTextViewText(id, pill.orEmpty())
            views.setViewVisibility(extra.pillNext, if (isNext && inline) View.VISIBLE else View.GONE)
            views.setViewVisibility(extra.pillNow, if (isNow && inline) View.VISIBLE else View.GONE)
            views.setViewVisibility(extra.pillNext2, if (isNext && !inline) View.VISIBLE else View.GONE)
            views.setViewVisibility(extra.pillNow2, if (isNow && !inline) View.VISIBLE else View.GONE)
            views.setTextViewText(ids.label, if (inline) word.orEmpty() else "")
            views.setTextViewText(extra.label2, if (inline) "" else word.orEmpty())
            views.setViewVisibility(extra.line2, if (inline) View.GONE else View.VISIBLE)
        }
        return spoken
    }

    /** The Today rows' own views, beyond the [ROWS] ids they share with the wide Next card. No hairline after Isha. */
    private class TodayRowIds(val pillNext: Int, val pillNow: Int, val pillNext2: Int, val pillNow2: Int, val label2: Int, val line2: Int, val hair: Int?)

    private val TODAY_ROWS = mapOf(
        PrayerSlot.FAJR to TodayRowIds(R.id.widget_pill_next_fajr, R.id.widget_pill_now_fajr, R.id.widget_pill_next2_fajr, R.id.widget_pill_now2_fajr, R.id.widget_label2_fajr, R.id.widget_line2_fajr, R.id.widget_hair_fajr),
        PrayerSlot.SUNRISE to TodayRowIds(R.id.widget_pill_next_sunrise, R.id.widget_pill_now_sunrise, R.id.widget_pill_next2_sunrise, R.id.widget_pill_now2_sunrise, R.id.widget_label2_sunrise, R.id.widget_line2_sunrise, R.id.widget_hair_sunrise),
        PrayerSlot.DHUHR to TodayRowIds(R.id.widget_pill_next_dhuhr, R.id.widget_pill_now_dhuhr, R.id.widget_pill_next2_dhuhr, R.id.widget_pill_now2_dhuhr, R.id.widget_label2_dhuhr, R.id.widget_line2_dhuhr, R.id.widget_hair_dhuhr),
        PrayerSlot.ASR to TodayRowIds(R.id.widget_pill_next_asr, R.id.widget_pill_now_asr, R.id.widget_pill_next2_asr, R.id.widget_pill_now2_asr, R.id.widget_label2_asr, R.id.widget_line2_asr, R.id.widget_hair_asr),
        PrayerSlot.MAGHRIB to TodayRowIds(R.id.widget_pill_next_maghrib, R.id.widget_pill_now_maghrib, R.id.widget_pill_next2_maghrib, R.id.widget_pill_now2_maghrib, R.id.widget_label2_maghrib, R.id.widget_line2_maghrib, R.id.widget_hair_maghrib),
        PrayerSlot.ISHA to TodayRowIds(R.id.widget_pill_next_isha, R.id.widget_pill_now_isha, R.id.widget_pill_next2_isha, R.id.widget_pill_now2_isha, R.id.widget_label2_isha, R.id.widget_line2_isha, null),
    )

    // Lazy: the unit tests load this object on the JVM, where Typeface cannot be created.
    private val MEDIUM: Typeface by lazy { Typeface.create("sans-serif-medium", Typeface.NORMAL) }
    private val LIGHT: Typeface by lazy { Typeface.create("sans-serif-light", Typeface.NORMAL) }

    /**
     * [text]'s drawn width in dp at [size] sp (scaled by the font setting as the launcher draws it, non linearly from Android 14), or
     * at [size] dp with [unit] COMPLEX_UNIT_DIP (the countdown, never scaled).
     */
    private fun textDp(
        context: Context, text: CharSequence, size: Float, typeface: Typeface = Typeface.DEFAULT, unit: Int = TypedValue.COMPLEX_UNIT_SP,
        tabular: Boolean = false,
    ): Float {
        val metrics = context.resources.displayMetrics
        val paint = android.graphics.Paint().apply {
            this.typeface = typeface
            textSize = TypedValue.applyDimension(unit, size, metrics)
            // The layouts draw times with tabular figures (fontFeatureSettings tnum), so every digit is as wide as the widest.
            if (tabular) fontFeatureSettings = "tnum"
        }
        return paint.measureText(text.toString()) / metrics.density
    }

    /**
     * The widest time this phone's clock can show: two hour digits ("12:58 pm", or "12:58" on a 24 hour clock, as wide as "23:58" in
     * tabular figures), in the app's language. Line fit decisions use it so they do not change with the place or the hour.
     */
    private fun widestClock(context: Context): String =
        TimeFormat.clock(context, java.time.LocalTime.of(12, 58))

    /** The medium weight (the mockup's semibold) for the next prayer's row. A span, because a RemoteViews cannot set a weight. */
    private fun medium(text: CharSequence): CharSequence =
        SpannableString(text).apply { setSpan(TypefaceSpan("sans-serif-medium"), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }

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
     * The countdown block of both Next layouts (the Today widget has its own, [fillTodayHero]): the next prayer's
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
     * The wide Next card's six rows (the Today widget has its own, [fillTodayRows]). Returns one spoken phrase per row for the
     * widget's single description. The highlighted row takes the rimmed band.
     */
    private fun fillRows(
        context: Context, views: RemoteViews, state: WidgetState, labels: Boolean, bandSideDp: Float = 0f,
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
     * The Hijri date as the Times screen's header builds it (HomeScreen.hijriToday): month names from the string array, and only the
     * month isolated, which is what keeps "7 Jumada al-Ula 1448" in order for a right to left reader (BidiTest). Null when there is none.
     */
    private fun hijriText(context: Context, state: WidgetState): String? {
        val hijri = state.hijri ?: return null
        val month = context.resources.getStringArray(R.array.hijri_months).getOrNull(hijri.month - 1) ?: return null
        return context.getString(R.string.hijri_date_format, hijri.day, month.bidiIsolated(), hijri.year)
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

    /** Everything the countdown block owns, hidden together when there is nothing to count down to. */
    private val HERO_IDS = listOf(
        R.id.widget_glyph, R.id.widget_name, R.id.widget_time, R.id.widget_countdown, R.id.widget_hero_time, R.id.widget_progress,
    )

    private val SEGMENTS = listOf(R.id.widget_seg_1, R.id.widget_seg_2, R.id.widget_seg_3, R.id.widget_seg_4, R.id.widget_seg_5)

    /** The view ids of one row. The Today layout and the wide Next layout use the same ones; the strip uses some (no Sunrise, no label). */
    private class RowIds(val row: Int, val glyph: Int, val name: Int, val label: Int, val time: Int)

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
        R.layout.widget_strip to R.layout.widget_strip_fixed,
    )

    /** The three inks the layouts use, each to its fixed light and dark value (values/colors.xml). */
    internal val FIXED_COLOURS = mapOf(
        R.color.widget_on_surface to (R.color.widget_light_on_surface to R.color.widget_dark_on_surface),
        R.color.widget_on_surface_variant to (R.color.widget_light_on_surface_variant to R.color.widget_dark_on_surface_variant),
        R.color.widget_primary to (R.color.widget_light_primary to R.color.widget_dark_primary),
        // The Today widget's pills and countdown card (1.7.1).
        R.color.widget_on_primary to (R.color.widget_light_on_primary to R.color.widget_dark_on_primary),
        R.color.widget_on_secondary_container to (R.color.widget_light_on_secondary_container to R.color.widget_dark_on_secondary_container),
        R.color.widget_hero_ink to (R.color.widget_light_hero_ink to R.color.widget_dark_hero_ink),
        R.color.widget_hero_label to (R.color.widget_light_hero_label to R.color.widget_dark_hero_label),
        R.color.widget_hero_soft to (R.color.widget_light_hero_soft to R.color.widget_dark_hero_soft),
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
        // 1.7.1: the Today widget's list card, its row fills, the two pills and the outlined town chip.
        R.drawable.widget_list_card to (R.drawable.widget_list_card_light to R.drawable.widget_list_card_dark),
        R.drawable.widget_row_next to (R.drawable.widget_row_next_light to R.drawable.widget_row_next_dark),
        R.drawable.widget_row_sunrise to (R.drawable.widget_row_sunrise_light to R.drawable.widget_row_sunrise_dark),
        R.drawable.widget_pill_next to (R.drawable.widget_pill_next_light to R.drawable.widget_pill_next_dark),
        R.drawable.widget_pill_now to (R.drawable.widget_pill_now_light to R.drawable.widget_pill_now_dark),
        R.drawable.widget_chip_outline to (R.drawable.widget_chip_outline_light to R.drawable.widget_chip_outline_dark),
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

    /** The countdown block of the two Next layouts. */
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

    /**
     * The Today widget (1.7.1): the header and its two town chips, the countdown card, the list card and its rows. The rows' fills
     * (next, Sunrise) are switched by the renderer after the paint, so they are not here.
     */
    private fun todayChipPaints(chip: Int, pin: Int, place: Int, update: Int) = listOf(
        Paint(chip, Ink.BACKGROUND, R.drawable.widget_chip_outline),
        Paint(pin, Ink.TINT, R.color.widget_primary),
        Paint(place, Ink.TEXT, R.color.widget_primary),
        Paint(update, Ink.TINT, R.color.widget_primary),
    )

    private val TODAY_PAINTS: List<Paint> by lazy {
        listOf(
            ROOT_PAINT,
            Paint(R.id.widget_date, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_hijri, Ink.TEXT, R.color.widget_on_surface_variant),
            Paint(R.id.widget_message, Ink.TEXT, R.color.widget_on_surface),
            Paint(R.id.widget_footer, Ink.TEXT, R.color.widget_on_surface_variant),
            Paint(R.id.widget_hero, Ink.BACKGROUND, R.drawable.widget_hero_background),
            Paint(R.id.widget_hero_label, Ink.TEXT, R.color.widget_hero_label),
            Paint(R.id.widget_name, Ink.TEXT, R.color.widget_hero_ink),
            Paint(R.id.widget_time, Ink.TEXT, R.color.widget_hero_soft),
            Paint(R.id.widget_time_below, Ink.TEXT, R.color.widget_hero_soft),
            Paint(R.id.widget_sublabel, Ink.TEXT, R.color.widget_hero_soft),
            Paint(R.id.widget_countdown, Ink.TEXT, R.color.widget_hero_ink),
            Paint(R.id.widget_hero_time, Ink.TEXT, R.color.widget_hero_ink),
            Paint(R.id.widget_until, Ink.TEXT, R.color.widget_hero_soft),
            Paint(R.id.widget_until_below, Ink.TEXT, R.color.widget_hero_soft),
            Paint(R.id.widget_list, Ink.BACKGROUND, R.drawable.widget_list_card),
        ) + todayChipPaints(R.id.widget_place_chip, R.id.widget_place_pin, R.id.widget_place, R.id.widget_place_update) +
            todayChipPaints(R.id.widget_place_chip_below, R.id.widget_place_pin_below, R.id.widget_place_below, R.id.widget_place_update_below) +
            ROWS.flatMap { (slot, ids) ->
                val ink = if (slot == PrayerSlot.SUNRISE) R.color.widget_on_surface_variant else R.color.widget_on_surface
                val extra = TODAY_ROWS.getValue(slot)
                listOfNotNull(
                    Paint(ids.name, Ink.TEXT, ink),
                    Paint(ids.label, Ink.TEXT, R.color.widget_on_surface_variant),
                    Paint(ids.time, Ink.TEXT, ink),
                    Paint(extra.label2, Ink.TEXT, R.color.widget_on_surface_variant),
                    extra.hair?.let { Paint(it, Ink.IMAGE, R.drawable.widget_divider) },
                ) + listOf(extra.pillNext, extra.pillNext2).flatMap {
                    listOf(Paint(it, Ink.TEXT, R.color.widget_on_primary), Paint(it, Ink.BACKGROUND, R.drawable.widget_pill_next))
                } + listOf(extra.pillNow, extra.pillNow2).flatMap {
                    listOf(Paint(it, Ink.TEXT, R.color.widget_on_secondary_container), Paint(it, Ink.BACKGROUND, R.drawable.widget_pill_now))
                }
            }
    }

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
        R.layout.widget_today to TODAY_PAINTS,
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
    private fun fillChip(
        context: Context, views: RemoteViews, state: WidgetState, chip: Int = R.id.widget_place_chip, text: Int = R.id.widget_place,
    ) {
        val place = if (state.hasLocation) place(context, state) else null
        if (place == null) {
            views.setViewVisibility(chip, View.GONE)
            return
        }
        views.setTextViewText(text, place)
        views.setViewVisibility(chip, View.VISIBLE)
        views.setContentDescription(chip, context.getString(R.string.widget_update_location, place.bidiIsolated()))
        views.setOnClickPendingIntent(chip, openApp(context, CHIP_REQUEST_CODE))
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
