package com.sajdatime.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.pdf.PrayerPdfExporter.Range
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * The PDF export on a real Android runtime, for every range, with the pages drawn out as pictures.
 *
 * PdfDocument is a platform class that no JVM test can run, and the masthead spacing changed for every
 * range in this release, so this is the check that each export produces a readable file, that the
 * Ramadan one has the right name, and that the first page of each is not blank. The pictures are saved
 * to `device-renders/` in the app's external files folder for the release script to pull, because
 * whether the typography and spacing are neat is a judgement for a person looking at the page, not for
 * an assertion.
 *
 * Each exported file is removed from Downloads afterwards.
 */
@RunWith(AndroidJUnit4::class)
class RamadanPdfDeviceTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val london = Coordinates(51.5074, -0.1278)
    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)

    private fun export(range: Range, today: LocalDate): Pair<Uri, String> {
        val outcome = runBlocking { PrayerPdfExporter(context).export(range, london, "London", prefs, today) }
        return when (outcome) {
            is PrayerPdfExporter.Outcome.SavedToDownloads -> outcome.uri to outcome.fileName
            is PrayerPdfExporter.Outcome.ReadyToShare -> outcome.uri to (outcome.uri.lastPathSegment ?: "")
        }
    }

    private fun renderFirstPage(uri: Uri, picture: String): Pair<Int, Int> {
        context.contentResolver.openFileDescriptor(uri, "r")!!.use { fd: ParcelFileDescriptor ->
            PdfRenderer(fd).use { renderer ->
                assertTrue("the PDF has no pages", renderer.pageCount >= 1)
                renderer.openPage(0).use { page ->
                    val scale = 2
                    val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    FileOutputStream(File(DeviceSupport.renderDir(), "$picture.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    var ink = 0
                    for (y in 0 until bitmap.height step 4) for (x in 0 until bitmap.width step 4) if (bitmap.getPixel(x, y) != Color.WHITE) ink++
                    val sampled = (bitmap.width / 4) * (bitmap.height / 4)
                    assertTrue("the first page looks blank ($ink of $sampled samples inked)", ink * 1000 > sampled)
                    return renderer.pageCount to ink
                }
            }
        }
    }

    private fun remove(uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }

    @Test
    fun the_ramadan_pdf_is_named_for_the_year_and_has_a_page_that_is_not_blank() {
        val (uri, name) = export(Range.RAMADAN, LocalDate.of(2027, 2, 22))
        try {
            assertTrue("file name was $name", name.startsWith("PrayerTimes_Ramadan1448"))
            assertTrue(name.endsWith(".pdf"))
            val (pages, _) = renderFirstPage(uri, "pdf-ramadan-page1")
            // 29 days at this row height fit on one page in portrait or two at most.
            assertTrue("$pages pages", pages in 1..2)
        } finally {
            remove(uri)
        }
    }

    @Test
    fun every_other_range_still_exports_a_readable_first_page() {
        val today = LocalDate.of(2027, 2, 22)
        for ((range, tag) in listOf(Range.TODAY to "today", Range.NEXT_7_DAYS to "week", Range.THIS_MONTH to "month")) {
            val (uri, _) = export(range, today)
            try {
                renderFirstPage(uri, "pdf-$tag-page1")
            } finally {
                remove(uri)
            }
        }
        assertEquals(true, DeviceSupport.renderDir().listFiles()!!.any { it.name == "pdf-today-page1.png" })
    }
}
