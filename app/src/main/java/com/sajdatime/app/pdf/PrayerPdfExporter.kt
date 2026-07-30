package com.sajdatime.app.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.notify.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    enum class Range(val label: String) {
        TODAY("Today"),
        NEXT_7_DAYS("Next 7 days"),
        THIS_MONTH("This month"),
    }

    /** Generates the PDF and returns a shareable content:// Uri. */
    suspend fun export(
        range: Range,
        coordinates: Coordinates,
        cityName: String,
        prefs: CalculationPrefs,
        today: LocalDate = LocalDate.now(),
    ): Uri = withContext(Dispatchers.IO) {
        val (start, days) = when (range) {
            Range.TODAY -> today to 1
            Range.NEXT_7_DAYS -> today to 7
            Range.THIS_MONTH -> today.withDayOfMonth(1) to today.lengthOfMonth()
        }

        val rows = PrayerEngine.computeRange(coordinates, start, days, prefs)
        val file = File(exportDir(), fileName(range, start))
        writePdf(file, rows, cityName, range, start)

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // --- drawing ---------------------------------------------------------------------

    private fun writePdf(
        file: File,
        rows: List<DayPrayerTimes>,
        cityName: String,
        range: Range,
        start: LocalDate,
    ) {
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

            file.parentFile?.mkdirs()
            file.outputStream().use { document.writeTo(it) }
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
    private val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
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
