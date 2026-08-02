package com.sajdatime.app.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sajdatime.core.AppLocale
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.label
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Exports prayer timetables as a plain black-and-white PDF table.
 *
 * ponytail: Android's built-in PdfDocument drawing straight onto a Canvas. No PDF
 * library, no licence cost, no APK weight — and the output is a simple table, which is
 * exactly what PdfDocument is good at.
 */
class PrayerPdfExporter(base: Context) {

    // Pinned to the app's own language: the PDF is a document the user keeps and may
    // print or send on, and its headings, day names and dates all have to be in one
    // language. See AppLocale.kt.
    private val context = AppLocale.wrap(base)

    enum class Range { TODAY, NEXT_7_DAYS, THIS_MONTH }

    /** Where the finished PDF ended up, which decides what the app tells the user. */
    sealed interface Outcome {
        /** Written into the public Downloads folder, where the user can go and find it. */
        data class SavedToDownloads(val uri: Uri, val fileName: String) : Outcome

        /** No Downloads collection available, so the file is offered through a share sheet. */
        data class ReadyToShare(val uri: Uri) : Outcome
    }

    /**
     * Generates the PDF. Throws if it cannot be written — the caller turns that into a
     * message rather than letting the tap appear to do nothing.
     */
    suspend fun export(
        range: Range,
        coordinates: Coordinates,
        cityName: String,
        prefs: CalculationPrefs,
        today: LocalDate = LocalDate.now(),
    ): Outcome = withContext(Dispatchers.IO) {
        val (start, days) = when (range) {
            Range.TODAY -> today to 1
            Range.NEXT_7_DAYS -> today to 7
            Range.THIS_MONTH -> today.withDayOfMonth(1) to today.lengthOfMonth()
        }

        val rows = PrayerEngine.computeRange(coordinates, start, days, prefs)
        val name = fileName(range, start)
        val bytes = renderPdf(rows, cityName, range, start)

        // Downloads first: the button says "save", so the file should be somewhere the
        // user can actually go and look for it afterwards. The share sheet alone left
        // people wondering where the timetable had gone.
        saveToDownloads(name, bytes)?.let { return@withContext Outcome.SavedToDownloads(it, name) }

        val file = File(exportDir(), name)
        file.writeBytes(bytes)
        Outcome.ReadyToShare(
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
        )
    }

    /**
     * Writes into the public Downloads collection. Returns null below API 29, where doing
     * this would mean asking for WRITE_EXTERNAL_STORAGE — a permission this app has no
     * business holding just to save a timetable. Those devices fall back to the share
     * sheet, which needs no permission at all.
     */
    private fun saveToDownloads(name: String, bytes: ByteArray): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val resolver = context.contentResolver
        return runCatching {
            val pending = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                // Marked pending so nothing can open a half-written file, and so
                // MediaStore renames rather than clobbers an existing timetable.
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, pending)
                ?: return null
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return null
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                null,
                null,
            )
            uri
        }.getOrNull()
    }

    // --- drawing ---------------------------------------------------------------------

    private fun renderPdf(
        rows: List<DayPrayerTimes>,
        cityName: String,
        range: Range,
        start: LocalDate,
    ): ByteArray {
        val document = PdfDocument()
        try {
            var index = 0
            var pageNumber = 1
            while (index < rows.size) {
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
                )
                val canvas = page.canvas

                var y = MARGIN + 28f
                if (pageNumber == 1) {
                    y = drawBrandHeader(canvas, cityName, range, start)
                    // Only when a day in this range was actually projected. Above the polar
                    // circles the home screen shows a banner; the PDF is the copy that leaves
                    // the app and gets pinned to a wall, and it had no such marking at all.
                    approximateNote(rows)?.let { note ->
                        wrapped(note, PAGE_WIDTH - 2 * MARGIN, subtitlePaint).forEach { line ->
                            canvas.drawText(line, MARGIN, y, subtitlePaint)
                            y += 11f
                        }
                        y += 4f
                    }
                    y += 12f
                } else {
                    y += 6f
                }

                y = drawHeaderRow(canvas, y)

                while (index < rows.size && y + ROW_HEIGHT <= PAGE_HEIGHT - MARGIN - FOOTER_SPACE) {
                    y = drawDataRow(canvas, y, rows[index])
                    index++
                }

                canvas.drawText(footer, MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
                document.finishPage(page)
                pageNumber++
            }

            return ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun drawHeaderRow(canvas: android.graphics.Canvas, top: Float): Float {
        val baseline = top + ROW_HEIGHT - 6f
        var x = MARGIN
        COLUMNS.forEach { column ->
            canvas.drawText(column.title, x + CELL_PADDING, baseline, headerPaint)
            x += column.width
        }
        canvas.drawLine(MARGIN, top + ROW_HEIGHT, MARGIN + tableWidth, top + ROW_HEIGHT, rulePaint)
        return top + ROW_HEIGHT + 2f
    }

    private fun drawDataRow(
        canvas: android.graphics.Canvas,
        top: Float,
        day: DayPrayerTimes,
    ): Float {
        val baseline = top + ROW_HEIGHT - 6f
        val values = listOf(
            day.date.format(dayNameFormat),
            day.date.format(dateFormat),
        ) + TABLE_SLOTS.map { slot -> TimeFormat.clock(context, day[slot]) }

        var x = MARGIN
        values.forEachIndexed { i, value ->
            canvas.drawText(value, x + CELL_PADDING, baseline, cellPaint)
            x += COLUMNS[i].width
        }
        canvas.drawLine(
            MARGIN,
            top + ROW_HEIGHT,
            MARGIN + tableWidth,
            top + ROW_HEIGHT,
            hairlinePaint,
        )
        return top + ROW_HEIGHT
    }

    // --- naming ----------------------------------------------------------------------

    /**
     * The masthead: mark, wordmark, then the place and period as their own block.
     *
     * The place used to be buried mid-sentence in a single bold line ("Prayer times for
     * Slough, United Kingdom, August 2026"), which is the hardest possible thing to find at
     * a glance on a sheet pinned to a kitchen wall — the one place this file's output
     * actually gets read. It now gets a line of its own at the largest size on the page,
     * with the qualifier underneath rather than inside it.
     *
     * "Approximate area" is not modesty, it is the truth and it is load-bearing: the app
     * only ever asks for coarse location, so the name really is a district rather than an
     * address. docs/privacy.html says so publicly, the location sheet says so on screen, and
     * a document the user may hand to someone else is the last place that promise should
     * quietly stop being repeated.
     *
     * Returns the y to carry on drawing from.
     */
    private fun drawBrandHeader(
        canvas: Canvas,
        cityName: String,
        range: Range,
        start: LocalDate,
    ): Float {
        val top = MARGIN
        drawLogo(canvas, MARGIN, top, LOGO_SIZE)

        val textLeft = MARGIN + LOGO_SIZE + 12f
        canvas.drawText(context.getString(R.string.app_name), textLeft, top + 19f, wordmarkPaint)
        canvas.drawText(context.getString(R.string.pdf_tagline), textLeft, top + 33f, taglinePaint)

        var y = top + LOGO_SIZE + 20f
        canvas.drawLine(MARGIN, y - 12f, MARGIN + tableWidth, y - 12f, rulePaint)

        // Isolated because this is the line a user prints and pins to a wall, so a place
        // name reordered by the text around it survives longer here than on a screen they
        // can refresh. See core/Bidi.kt. The isolate characters are zero-width and
        // Canvas.drawText does not draw them.
        val place = cityName.bidiIsolated().ifBlank { context.getString(R.string.pdf_place_unknown) }
        canvas.drawText(place, MARGIN, y, placePaint)
        y += 13f

        val period = when (range) {
            Range.TODAY -> start.format(dateFormat)
            Range.NEXT_7_DAYS ->
                context.getString(R.string.pdf_period_onwards, start.format(dateFormat))
            Range.THIS_MONTH -> start.format(monthYearFormat)
        }
        canvas.drawText(
            context.getString(R.string.pdf_header_meta, context.getString(R.string.pdf_area_label), period),
            MARGIN,
            y,
            subtitlePaint,
        )
        y += 8f
        canvas.drawLine(MARGIN, y, MARGIN + tableWidth, y, hairlinePaint)
        return y + 14f
    }

    /**
     * The launcher mark, drawn as its two adaptive-icon layers rather than as
     * `R.mipmap.ic_launcher`.
     *
     * ponytail: the rounded square is one `drawRoundRect` in the icon's own background
     * colour, so there is no bitmap, no mask and no density bucket to pick. Asking for the
     * AdaptiveIconDrawable instead would work, but it would draw its background as a full
     * square and leave the corners to a mask this Canvas does not have.
     *
     * The foreground is inflated by [LOGO_FOREGROUND_SCALE] about its own centre because an
     * adaptive icon's artwork is drawn at 108 units while only the middle 72 are ever shown
     * — dropping it into the box unscaled renders a correct but conspicuously shrunken mark.
     */
    private fun drawLogo(canvas: Canvas, left: Float, top: Float, size: Float) {
        canvas.drawRoundRect(
            RectF(left, top, left + size, top + size),
            size * 0.22f,
            size * 0.22f,
            logoBackPaint,
        )
        val foreground = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
            ?: return
        val bleed = size * (LOGO_FOREGROUND_SCALE - 1f) / 2f
        foreground.setBounds(
            (left - bleed).toInt(),
            (top - bleed).toInt(),
            (left + size + bleed).toInt(),
            (top + size + bleed).toInt(),
        )
        canvas.save()
        canvas.clipRect(left, top, left + size, top + size)
        foreground.draw(canvas)
        canvas.restore()
    }

    /**
     * The approximation notice, or null when every day in the range is the user's own
     * astronomy — which is very nearly every user, so most PDFs are unchanged.
     *
     * The latitude is read from the days themselves rather than recomputed, because it is
     * not a constant: it is 45 degrees under the Islamic Fiqh Council's ruling and 60 under
     * Moonsighting's, so it depends on the method the user picked. Printing the actual number
     * is what lets someone check this against their mosque instead of taking its word.
     */
    private fun approximateNote(rows: List<DayPrayerTimes>): String? {
        val from = rows.firstNotNullOfOrNull { it.approximatedFrom } ?: return null
        return context.getString(R.string.pdf_approximate_note, from.toInt())
    }

    /**
     * Greedy word wrap against the actual measured text width.
     *
     * ponytail: `Paint.measureText` and a fold, not a StaticLayout. StaticLayout wants a
     * width in pixels and a whole layout pass to draw one paragraph of small print onto a
     * Canvas that is already being drawn line by line, and this file draws every other row
     * by baseline arithmetic already.
     */
    private fun wrapped(text: String, maxWidth: Float, paint: Paint): List<String> =
        text.split(" ").fold(mutableListOf<String>()) { lines, word ->
            val candidate = if (lines.isEmpty()) word else "${lines.last()} $word"
            if (lines.isNotEmpty() && paint.measureText(candidate) <= maxWidth) {
                lines[lines.lastIndex] = candidate
            } else {
                lines.add(word)
            }
            lines
        }

    // PrayerTimes_July2026.pdf / PrayerTimes_30Jul2026.pdf. Locale.US is deliberate and is
    // the one place in the app that ignores the user's language: this string is a file name
    // in a shared Downloads folder, and it has to stay sortable, ASCII, and safe on the
    // FAT-derived filesystems that USB and SD transfers still land on.
    private fun fileName(range: Range, start: LocalDate): String {
        val stamp = when (range) {
            Range.THIS_MONTH -> start.format(DateTimeFormatter.ofPattern("MMMMyyyy", Locale.US))
            else -> start.format(DateTimeFormatter.ofPattern("ddMMMyyyy", Locale.US))
        }
        return "PrayerTimes_$stamp.pdf"
    }

    private fun exportDir(): File = File(context.cacheDir, "exports").apply { mkdirs() }

    // --- paints & layout -------------------------------------------------------------

    private data class Column(val title: String, val width: Float)

    private val TABLE_SLOTS = listOf(
        PrayerSlot.FAJR,
        PrayerSlot.SUNRISE,
        PrayerSlot.DHUHR,
        PrayerSlot.ASR,
        PrayerSlot.MAGHRIB,
        PrayerSlot.ISHA,
    )

    private val COLUMNS = listOf(
        Column(context.getString(R.string.pdf_column_day), 72f),
        Column(context.getString(R.string.pdf_column_date), 91f),
    ) + TABLE_SLOTS.map { Column(it.label(context), 60f) }

    private val tableWidth = COLUMNS.sumOf { it.width.toDouble() }.toFloat()

    // The wordmark is the app's own name at the top of a document that leaves the app, so
    // it is set larger and looser than anything else on the page and never competes with
    // the place name below it for the same weight.
    private val wordmarkPaint = Paint().apply {
        isAntiAlias = true
        textSize = 19f
        letterSpacing = 0.01f
        color = LOGO_GREEN
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val taglinePaint = Paint().apply {
        isAntiAlias = true
        textSize = 8.5f
        color = 0xFF6B6B6B.toInt()
    }
    private val placePaint = Paint().apply {
        isAntiAlias = true
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val logoBackPaint = Paint().apply {
        isAntiAlias = true
        color = LOGO_GREEN
    }
    private val subtitlePaint = Paint().apply {
        isAntiAlias = true
        textSize = 9f
        color = 0xFF555555.toInt()
    }
    private val headerPaint = Paint().apply {
        isAntiAlias = true
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val cellPaint = Paint().apply {
        isAntiAlias = true
        textSize = 10f
    }
    private val rulePaint = Paint().apply { strokeWidth = 1f }
    private val hairlinePaint = Paint().apply {
        strokeWidth = 0.4f
        color = 0xFFAAAAAA.toInt()
    }
    private val footerPaint = Paint().apply {
        isAntiAlias = true
        textSize = 8f
        color = 0xFF777777.toInt()
    }

    private val footer: String get() = context.getString(R.string.pdf_footer)

    // AppLocale, not the device locale: the headings and footer beside these come from
    // string resources, so the day names have to be in the same language as the words
    // they sit next to. See AppLocale.kt.
    private val dayNameFormat = DateTimeFormatter.ofPattern("EEEE", AppLocale.of(context))

    /**
     * Abbreviated month, not the full name. The Date column is 91pt wide and the cell text
     * is 10pt, so "30 September 2026" ran within a few points of the Fajr column and a
     * longer month name in another language ran straight into it.
     */
    private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", AppLocale.of(context))
    private val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", AppLocale.of(context))

    private companion object {
        // A4 at 72 dpi.
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 36f
        const val ROW_HEIGHT = 19f
        const val CELL_PADDING = 3f
        const val FOOTER_SPACE = 16f
        const val LOGO_SIZE = 40f

        /** 108/72: an adaptive icon draws at 108 units and only the middle 72 are shown. */
        const val LOGO_FOREGROUND_SCALE = 1.5f

        /** LightPrimary, the same #0E6B4F as ic_launcher_background.xml. Keep them together. */
        const val LOGO_GREEN = 0xFF0E6B4F.toInt()
    }
}
