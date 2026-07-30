package com.sajdatime.app.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
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
class PrayerPdfExporter(private val context: Context) {

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
                    canvas.drawText(title(cityName, range, start), MARGIN, y, titlePaint)
                    y += 20f
                    canvas.drawText(subtitle(), MARGIN, y, subtitlePaint)
                    y += 26f
                } else {
                    y += 6f
                }

                y = drawHeaderRow(canvas, y)

                while (index < rows.size && y + ROW_HEIGHT <= PAGE_HEIGHT - MARGIN - FOOTER_SPACE) {
                    y = drawDataRow(canvas, y, rows[index])
                    index++
                }

                canvas.drawText(FOOTER, MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
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

    private fun title(cityName: String, range: Range, start: LocalDate): String {
        val place = cityName.ifBlank { "your location" }
        val period = when (range) {
            Range.TODAY -> start.format(dateFormat)
            Range.NEXT_7_DAYS -> "${start.format(dateFormat)} onwards"
            Range.THIS_MONTH -> start.format(monthYearFormat)
        }
        return "Prayer Times for $place, $period"
    }

    private fun subtitle(): String = "Generated by SajdaTime"

    private fun fileName(range: Range, start: LocalDate): String {
        val stamp = when (range) {
            Range.THIS_MONTH -> start.format(DateTimeFormatter.ofPattern("MMMM_yyyy", Locale.US))
            else -> start.format(DateTimeFormatter.ofPattern("dd_MMM_yyyy", Locale.US))
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
        Column("Day", 72f),
        Column("Date", 91f),
    ) + TABLE_SLOTS.map { Column(it.label, 60f) }

    private val tableWidth = COLUMNS.sumOf { it.width.toDouble() }.toFloat()

    private val titlePaint = Paint().apply {
        isAntiAlias = true
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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

    private val dayNameFormat = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())

    /**
     * Abbreviated month, not the full name. The Date column is 91pt wide and the cell text
     * is 10pt, so "30 September 2026" ran within a few points of the Fajr column and a
     * longer month name in another language ran straight into it.
     */
    private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    private val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    private companion object {
        // A4 at 72 dpi.
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 36f
        const val ROW_HEIGHT = 19f
        const val CELL_PADDING = 3f
        const val FOOTER_SPACE = 16f
        const val FOOTER = "Calculated offline on your device. SajdaTime is free, forever."
    }
}
