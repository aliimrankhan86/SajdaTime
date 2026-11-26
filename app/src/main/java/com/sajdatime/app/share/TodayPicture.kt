package com.sajdatime.app.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import androidx.core.content.FileProvider
import com.sajdatime.app.R
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.placeName
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

/**
 * Today's times as a square picture to send to family or a mosque group (1.8.0): a free app with no marketing is
 * found that way. Drawn on the phone with Android's own Canvas, written to the cache and shared through the PDF's
 * FileProvider path: no library, no storage permission, no network, no usage count. Nothing is written or sent
 * until the person presses Share on the preview.
 *
 * ponytail: fixed pixel sizes on a fixed 1080 square, so the picture is the same whatever the phone's text size;
 * the preview around it is what scales.
 */
object TodayPicture {

    const val SIZE = 1080

    /** One line of the picture: Dhuhr's line is Jumu'ah, at the user's own time, on a Friday when one is set. */
    data class Line(val slot: PrayerSlot, val at: Instant, val jumuah: Boolean = false)

    /** The six lines in order. [jumuahAt] is today's Jumu'ah when JamaahPlan shows one, else null. Pure, so tested. */
    fun lines(day: DayPrayerTimes, jumuahAt: Instant?): List<Line> =
        listOf(PrayerSlot.FAJR, PrayerSlot.SUNRISE, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.MAGHRIB, PrayerSlot.ISHA).map { slot ->
            if (slot == PrayerSlot.DHUHR && jumuahAt != null) Line(slot, jumuahAt, jumuah = true) else Line(slot, day[slot])
        }

    /** The Material roles the picture is drawn in, read from the theme in use (light or dark). */
    data class Colours(val page: Int, val card: Int, val ink: Int, val quiet: Int, val accent: Int, val line: Int)

    /** [context] must be in the app's language (the activity is): every word and digit comes from it. */
    fun render(context: Context, settings: AppSettings, day: DayPrayerTimes, jumuahAt: Instant?, colours: Colours): Bitmap {
        val rtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(colours.page)

        val card = RectF(56f, 56f, SIZE - 56f, 968f)
        canvas.drawRoundRect(card, 56f, 56f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colours.card })
        canvas.drawRoundRect(card, 56f, 56f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colours.line
            style = Paint.Style.STROKE
            strokeWidth = 2f
        })

        val left = 120f
        val right = SIZE - 120f
        val width = right - left
        val place = settings.placeName(context).bidiIsolated().ifBlank { context.getString(R.string.pdf_place_unknown) }
        canvas.line(place, true, 176f, paint(64f, colours.ink, bold = true), left, right, width, rtl)
        canvas.line(dateLine(context, settings, day), true, 236f, paint(34f, colours.quiet), left, right, width, rtl)

        lines(day, jumuahAt).forEachIndexed { i, line ->
            val y = 336f + i * 96f
            val sunrise = line.slot == PrayerSlot.SUNRISE
            val time = TimeFormat.clock(context, line.at)
            val timePaint = paint(if (sunrise) 40f else 48f, if (sunrise) colours.quiet else colours.ink, bold = !sunrise).apply {
                fontFeatureSettings = "tnum"
            }
            val label = if (line.jumuah) context.getString(R.string.jumuah) else line.slot.label(context)
            val labelWidth = width - timePaint.measureText(time) - 32f
            canvas.line(label, true, y, paint(if (sunrise) 40f else 44f, if (sunrise) colours.quiet else colours.ink), left, right, labelWidth, rtl)
            canvas.line(time, false, y, timePaint, left, right, width, rtl)
        }

        val method = methodAndSchool(context, settings)
        val methodLine = if (day.approximated) {
            context.getString(R.string.home_date_line, method, context.getString(R.string.ramadan_approx_marker))
        } else {
            method
        }
        canvas.line(methodLine, true, 900f, paint(30f, colours.quiet), left, right, width, rtl)

        val footer = paint(32f, colours.accent, bold = true).apply { textAlign = Paint.Align.CENTER }
        val words = context.getString(R.string.share_picture_footer)
        canvas.drawText(marked(TextUtils.ellipsize(words, footer, SIZE - 112f, TextUtils.TruncateAt.END).toString(), rtl), SIZE / 2f, 1034f, footer)
        return bitmap
    }

    /** Writes [bitmap] over the last one (one file, so places and times never pile up), then opens the share sheet. */
    suspend fun share(context: Context, bitmap: Bitmap) {
        val uri = withContext(Dispatchers.IO) {
            val file = File(File(context.cacheDir, "exports").apply { mkdirs() }, "sajdatime-today.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val title = context.getString(R.string.share_picture_title)
        val send = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            // As for the PDF: ClipData is what lets the share sheet read the file and show its preview.
            .apply { clipData = android.content.ClipData.newRawUri(title, uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, title))
    }

    /**
     * "Muslim World League · Shafi'i", the Times screen's own line under the list (HomeScreen.WhyTheseTimes) without its
     * "adjusted" note, which a recipient cannot act on. The school matters on a picture sent to a mosque group: Hanafi Asr
     * is later (ux-lead, 26 Nov 2026). ponytail: the madhab names repeat madhabLabel's four lines, which is composable only.
     */
    private fun methodAndSchool(context: Context, settings: AppSettings): String {
        val prefs = settings.calculationPrefs
        val resolved = PrayerEngine.resolveMethod(prefs)
        if (prefs.sect == Sect.SHIA && resolved == CalcMethod.JAFARI) return context.getString(R.string.why_times_shia_caption)
        val method = resolved.label(context).bidiIsolated()
        if (prefs.sect == Sect.SHIA) return method
        val school = context.getString(
            when (prefs.madhab) {
                Madhab.HANAFI -> R.string.madhab_hanafi
                Madhab.SHAFII -> R.string.madhab_shafii
                Madhab.MALIKI -> R.string.madhab_maliki
                Madhab.HANBALI -> R.string.madhab_hanbali
            },
        )
        return context.getString(R.string.settings_value_pair, method, school)
    }

    /** "Thu 26 Nov · 25 Jumada al-Ula 1448", built as the Times screen's header builds it (HomeScreen.hijriToday). */
    private fun dateLine(context: Context, settings: AppSettings, day: DayPrayerTimes): String {
        val gregorian = TimeFormat.date(context, day.date)
        val hijri = HijriCalendar.of(day.date, settings.calculationPrefs.hijriOffset)?.let { h ->
            context.resources.getStringArray(R.array.hijri_months).getOrNull(h.month - 1)?.let { month ->
                context.getString(R.string.hijri_date_format, h.day, month.bidiIsolated(), h.year)
            }
        } ?: return gregorian
        return context.getString(R.string.home_date_line, gregorian, hijri)
    }

    private fun paint(size: Float, colour: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        color = colour
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    /**
     * One line at the start side ([start]) or the end side, cut with an ellipsis to [max]. Start is left in English
     * and right in Arabic and Urdu, as on screen. Plain drawText with a right to left mark in front of text that
     * has right to left letters: the PDF's lesson (PrayerPdfExporter.drawStart), where declaring a whole run right
     * to left printed "SajdaTime" backwards and marking an all Latin time printed "PM 5:03".
     */
    private fun Canvas.line(text: String, start: Boolean, y: Float, paint: TextPaint, left: Float, right: Float, max: Float, rtl: Boolean) {
        val atLeft = start != rtl
        paint.textAlign = if (atLeft) Paint.Align.LEFT else Paint.Align.RIGHT
        val cut = TextUtils.ellipsize(text, paint, max, TextUtils.TruncateAt.END).toString()
        drawText(marked(cut, rtl), if (atLeft) left else right, y, paint)
    }

    private fun marked(text: String, rtl: Boolean) = if (rtl && text.any(::isRightToLeft)) "‏$text" else text

    private fun isRightToLeft(c: Char): Boolean = when (Character.getDirectionality(c)) {
        Character.DIRECTIONALITY_RIGHT_TO_LEFT, Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> true
        else -> false
    }
}
