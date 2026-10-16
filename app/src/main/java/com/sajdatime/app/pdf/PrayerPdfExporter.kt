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
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.RamadanInfo
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.label
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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

    enum class Range { TODAY, NEXT_7_DAYS, THIS_MONTH, RAMADAN }

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
        val span = spanOf(range, today, prefs)

        val rows = PrayerEngine.computeRange(coordinates, span.start, span.days, prefs)
        val name = fileNameFor(range, span)
        val bytes = renderPdf(rows, cityName, range, span)

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
        span: Span,
    ): ByteArray {
        val columns = columnsFor(range)
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
                    y = drawBrandHeader(canvas, cityName, range, span)
                    // Notes under the masthead, each wrapped to the table width. The Ramadan
                    // one says what the two highlighted columns are and defers to the mosque.
                    // The approximate one only appears when a day in this range was actually
                    // projected: above the polar circles the home screen shows a banner, the PDF
                    // is the copy that leaves the app and gets pinned to a wall, and it had no
                    // such marking at all.
                    val notes = listOfNotNull(
                        if (range == Range.RAMADAN) context.getString(R.string.pdf_ramadan_note) else null,
                        approximateNote(rows),
                    )
                    notes.forEach { note ->
                        wrapped(note, PAGE_WIDTH - 2 * MARGIN, subtitlePaint).forEach { line ->
                            canvas.drawText(line, MARGIN, y, subtitlePaint)
                            y += NOTE_LINE
                        }
                        y += 4f
                    }
                    y += BEFORE_TABLE
                } else {
                    y += 6f
                }

                y = drawHeaderRow(canvas, y, columns)

                while (index < rows.size && y + ROW_HEIGHT <= PAGE_HEIGHT - MARGIN - FOOTER_SPACE) {
                    y = drawDataRow(canvas, y, rows[index], columns, span.ramadan)
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

    /**
     * The column titles and, for the Ramadan range, a small second line under Fajr and Maghrib
     * saying what they are for the month: "Suhoor ends" and "Iftar". Both stay plain slot names
     * on the first line, so the table still reads as the prayer timetable it is.
     */
    private fun drawHeaderRow(canvas: Canvas, top: Float, columns: List<Column>): Float {
        val twoLine = columns.any { it.sub != null }
        val height = ROW_HEIGHT + if (twoLine) SUB_LINE else 0f
        val baseline = top + ROW_HEIGHT - 6f
        var x = MARGIN
        columns.forEach { column ->
            canvas.drawText(column.title, x + CELL_PADDING, baseline, headerPaint)
            column.sub?.let { sub ->
                drawFitted(canvas, sub, x + CELL_PADDING, baseline + SUB_LINE, subLabelPaint, column.width - 2 * CELL_PADDING)
            }
            x += column.width
        }
        canvas.drawLine(MARGIN, top + height, MARGIN + tableWidth, top + height, rulePaint)
        return top + height + 2f
    }

    private fun drawDataRow(
        canvas: Canvas,
        top: Float,
        day: DayPrayerTimes,
        columns: List<Column>,
        ramadan: RamadanInfo?,
    ): Float {
        val baseline = top + ROW_HEIGHT - 6f
        val values = listOfNotNull(
            // The day of the month, 1 to 29 or 30, counted from the first day on the user's own
            // calendar, so it agrees with the Day number on the card.
            ramadan?.let { (ChronoUnit.DAYS.between(it.firstDay, day.date) + 1).toString() },
            day.date.format(dayNameFormat),
            day.date.format(dateFormat),
        ) + TABLE_SLOTS.map { slot -> TimeFormat.clock(context, day[slot]) }

        var x = MARGIN
        values.forEachIndexed { i, value ->
            canvas.drawText(value, x + CELL_PADDING, baseline, if (columns[i].strong) cellBoldPaint else cellPaint)
            x += columns[i].width
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

    /**
     * Draws [text] at [paint]'s size, or smaller if it would not fit in [maxWidth]. A second
     * language's longer word for "Suhoor ends" must shrink a little, not run into the next column.
     */
    private fun drawFitted(canvas: Canvas, text: String, x: Float, baseline: Float, paint: Paint, maxWidth: Float) {
        val fitted = Paint(paint)
        while (fitted.textSize > MIN_FITTED_SIZE && fitted.measureText(text) > maxWidth) {
            fitted.textSize -= 0.25f
        }
        canvas.drawText(text, x, baseline, fitted)
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
        span: Span,
    ): Float {
        val top = MARGIN
        drawLogo(canvas, MARGIN, top, LOGO_SIZE)

        // The two lines are centred on the mark, wordmark baseline 21 and tagline 34 below the
        // top edge, so the pair reads as one block beside it and not as a title with a caption
        // pushed up under it.
        val textLeft = MARGIN + LOGO_SIZE + 12f
        canvas.drawText(context.getString(R.string.app_name), textLeft, top + 21f, wordmarkPaint)
        canvas.drawText(context.getString(R.string.pdf_tagline), textLeft, top + 34f, taglinePaint)

        // The rule sits 14 below the mark and the place name 22 below the rule. They were 8 and 0,
        // which put the largest text on the page against the line above it, directly beneath the
        // wordmark. Each step down the masthead now has its own air: mark, rule, place, qualifier,
        // hairline, content.
        val ruleY = top + LOGO_SIZE + 14f
        canvas.drawLine(MARGIN, ruleY, MARGIN + tableWidth, ruleY, rulePaint)
        var y = ruleY + 22f

        // Isolated because this is the line a user prints and pins to a wall, so a place
        // name reordered by the text around it survives longer here than on a screen they
        // can refresh. See core/Bidi.kt. The isolate characters are zero-width and
        // Canvas.drawText does not draw them.
        val place = cityName.bidiIsolated().ifBlank { context.getString(R.string.pdf_place_unknown) }
        canvas.drawText(place, MARGIN, y, placePaint)
        y += 15f

        val period = when (range) {
            Range.TODAY -> span.start.format(dateFormat)
            Range.NEXT_7_DAYS ->
                context.getString(R.string.pdf_period_onwards, span.start.format(dateFormat))
            Range.THIS_MONTH -> span.start.format(monthYearFormat)
            Range.RAMADAN ->
                context.getString(R.string.pdf_period_ramadan, requireNotNull(span.ramadan).hijriYear)
        }
        canvas.drawText(
            context.getString(R.string.pdf_header_meta, context.getString(R.string.pdf_area_label), period),
            MARGIN,
            y,
            subtitlePaint,
        )
        y += 10f
        canvas.drawLine(MARGIN, y, MARGIN + tableWidth, y, hairlinePaint)
        return y + 16f
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

    private fun exportDir(): File = File(context.cacheDir, "exports").apply { mkdirs() }

    // --- paints & layout -------------------------------------------------------------

    /**
     * One table column. [sub] is a small second header line (Ramadan only) and [strong] draws the
     * cells in bold, which is how the two columns that bound the fast stand out on a black and
     * white printout where colour cannot be used.
     */
    private data class Column(
        val title: String,
        val width: Float,
        val sub: String? = null,
        val strong: Boolean = false,
    )

    private val TABLE_SLOTS = listOf(
        PrayerSlot.FAJR,
        PrayerSlot.SUNRISE,
        PrayerSlot.DHUHR,
        PrayerSlot.ASR,
        PrayerSlot.MAGHRIB,
        PrayerSlot.ISHA,
    )

    private fun columnsFor(range: Range): List<Column> = when (range) {
        Range.RAMADAN -> listOf(
            Column(context.getString(R.string.pdf_column_ramadan_day), RAMADAN_WIDTHS[0]),
            Column(context.getString(R.string.pdf_column_day), RAMADAN_WIDTHS[1]),
            Column(context.getString(R.string.pdf_column_date), RAMADAN_WIDTHS[2]),
        ) + TABLE_SLOTS.mapIndexed { i, slot ->
            Column(
                title = slot.label(context),
                width = RAMADAN_WIDTHS[3 + i],
                sub = when (slot) {
                    PrayerSlot.FAJR -> context.getString(R.string.ramadan_suhoor_ends)
                    PrayerSlot.MAGHRIB -> context.getString(R.string.ramadan_iftar)
                    else -> null
                },
                strong = slot == PrayerSlot.FAJR || slot == PrayerSlot.MAGHRIB,
            )
        }

        else -> listOf(
            Column(context.getString(R.string.pdf_column_day), ORDINARY_WIDTHS[0]),
            Column(context.getString(R.string.pdf_column_date), ORDINARY_WIDTHS[1]),
        ) + TABLE_SLOTS.mapIndexed { i, slot -> Column(slot.label(context), ORDINARY_WIDTHS[2 + i]) }
    }

    private val tableWidth = (PAGE_WIDTH - 2 * MARGIN)

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
    private val cellBoldPaint = Paint().apply {
        isAntiAlias = true
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val subLabelPaint = Paint().apply {
        isAntiAlias = true
        textSize = 7.5f
        color = 0xFF555555.toInt()
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

    companion object {
        // A4 at 72 dpi.
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 36f
        private const val ROW_HEIGHT = 19f
        private const val CELL_PADDING = 3f
        private const val FOOTER_SPACE = 16f
        private const val LOGO_SIZE = 40f

        /** Line pitch of the 9pt notes under the masthead. */
        private const val NOTE_LINE = 11f

        /** Space between the last note (or the masthead) and the table's header row. */
        private const val BEFORE_TABLE = 6f

        /** Extra height of the header row when it carries the second line, "Suhoor ends". */
        private const val SUB_LINE = 9f

        private const val MIN_FITTED_SIZE = 5.5f

        /** 108/72: an adaptive icon draws at 108 units and only the middle 72 are shown. */
        private const val LOGO_FOREGROUND_SCALE = 1.5f

        /** LightPrimary, the same #0E6B4F as ic_launcher_background.xml. Keep them together. */
        private const val LOGO_GREEN = 0xFF0E6B4F.toInt()

        /**
         * Column widths in points. Both rows add up to the 523 points between the margins
         * (`PdfWidthsTest`), so the table is flush with the rules above it. Ramadan has a narrow
         * Day column, a day name, a date and six times at 57.5.
         */
        internal val ORDINARY_WIDTHS = listOf(72f, 91f, 60f, 60f, 60f, 60f, 60f, 60f)
        internal val RAMADAN_WIDTHS = listOf(31f, 62f, 85f, 57.5f, 57.5f, 57.5f, 57.5f, 57.5f, 57.5f)

        /**
         * The days a range covers, and for Ramadan the month they belong to. [ramadan] is the
         * Ramadan whose first day is [start], on the user's own shifted calendar, or null for the
         * other ranges.
         */
        internal data class Span(val start: LocalDate, val days: Int, val ramadan: RamadanInfo? = null)

        /**
         * Pure, so the arithmetic is tested without a Canvas. Ramadan is the current month or, if
         * today is before it, the next one, on the user's calendar (their shift applies through
         * [CalculationPrefs.hijriOffset]). The export sheet only offers it from 30 days before day 1
         * to the last day, so the "next one" case is the weeks before the month, which is what
         * lets a family print the timetable ahead of time. Throws only outside the calendar's
         * range, which the caller turns into the same failed export message as any other error.
         */
        internal fun spanOf(range: Range, today: LocalDate, prefs: CalculationPrefs): Span = when (range) {
            Range.TODAY -> Span(today, 1)
            Range.NEXT_7_DAYS -> Span(today, 7)
            Range.THIS_MONTH -> Span(today.withDayOfMonth(1), today.lengthOfMonth())
            Range.RAMADAN -> {
                val info = checkNotNull(HijriCalendar.ramadan(today, prefs.hijriOffset)) {
                    "No Ramadan within the supported calendar range for $today"
                }
                Span(info.firstDay, info.length, info)
            }
        }

        // PrayerTimes_July2026.pdf / PrayerTimes_30Jul2026.pdf / PrayerTimes_Ramadan1448.pdf.
        // Locale.US is deliberate and is the one place in the app that ignores the user's
        // language: this string is a file name in a shared Downloads folder, and it has to stay
        // sortable, ASCII, and safe on the FAT-derived filesystems that USB and SD transfers
        // still land on.
        internal fun fileNameFor(range: Range, span: Span): String {
            val stamp = when (range) {
                Range.THIS_MONTH -> span.start.format(DateTimeFormatter.ofPattern("MMMMyyyy", Locale.US))
                Range.RAMADAN -> "Ramadan${requireNotNull(span.ramadan).hijriYear}"
                else -> span.start.format(DateTimeFormatter.ofPattern("ddMMMyyyy", Locale.US))
            }
            return "PrayerTimes_$stamp.pdf"
        }
    }
}
