package com.sajdatime.app

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.widget.WidgetRenderer
import com.sajdatime.app.widget.WidgetState
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId

/**
 * Evidence, not an assertion: draws the three widgets the way a launcher does at a matrix of sizes, text
 * scales and themes, saves a PNG of each and writes `report.csv` listing what clips. Always passes unless it
 * crashes. Output: the app's external files folder, `widget-evidence/`.
 *
 * It is SKIPPED in a normal device suite run (it renders about a thousand widgets and writes PNGs): run it on purpose with
 * `adb shell am instrument -w -e widgetEvidence true -e class com.sajdatime.app.WidgetRenderEvidenceTest
 * com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`. Android 12 and later only (the launcher's size
 * choice, `updateAppWidgetSize` with a list of sizes, does not exist before). Emulator, never Ali's phone.
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 31)
class WidgetRenderEvidenceTest {

    @Before
    fun onlyWhenAsked() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("widgetEvidence") == "true")
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val base: Context = instrumentation.targetContext
    private val zone = ZoneId.of("Europe/London")

    // 22 Feb 2027 is day 15 of Ramadan 1448, so the longest labels (Suhoor ends, Iftar) are on screen.
    private val now = Instant.parse("2027-02-22T09:00:00Z")

    private class Case(val widget: String, val sizes: List<Pair<Int, Int>>, val build: (Context, WidgetState, Instant) -> RemoteViews)

    @Test
    fun render_every_widget_size_scale_and_theme() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), now, zone, true) }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { deleteRecursively(); mkdirs() }

        val cases = listOf(
            Case("strip", listOf(250 to 64, 300 to 64, 300 to 110, 300 to 150, 360 to 110, 150 to 110), WidgetRenderer::strip),
            Case("next", listOf(110 to 110, 180 to 110, 270 to 110, 270 to 150, 180 to 150), WidgetRenderer::nextPrayer),
            Case("today", listOf(180 to 180, 250 to 180, 250 to 250, 330 to 250, 330 to 330), WidgetRenderer::today),
        )
        val csv = StringBuilder("widget,wDp,hDp,fontScale,theme,problems\n")
        for (case in cases) for ((w, h) in case.sizes) for (scale in listOf(1.0f, 1.15f, 1.3f, 2.0f)) for (dark in listOf(false, true)) {
            val theme = if (dark) "dark" else "light"
            val config = Configuration(base.resources.configuration).apply {
                fontScale = scale
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    (if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
            }
            val ctx = base.createConfigurationContext(config)
            val density = ctx.resources.displayMetrics.density
            val wPx = (w * density).toInt()
            val hPx = (h * density).toInt()
            val problems = mutableListOf<String>()
            instrumentation.runOnMainSync {
                try {
                    val views = case.build(ctx, state, now)
                    val host = AppWidgetHostView(ctx)
                    host.updateAppWidgetSize(Bundle(), listOf(SizeF(w.toFloat(), h.toFloat())))
                    host.updateAppWidget(views)
                    host.measure(
                        View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY),
                    )
                    host.layout(0, 0, wPx, hPx)
                    val bitmap = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
                    Canvas(bitmap).apply { drawColor(if (dark) Color.parseColor("#202020") else Color.parseColor("#DDDDDD")); host.draw(this) }
                    val name = "${case.widget}_${w}x${h}_fs${scale}_$theme"
                    FileOutputStream(File(dir, "$name.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    if (host.childCount == 0) problems += "no content inflated"
                    inspect(host, problems)
                } catch (t: Throwable) {
                    problems += "CRASH ${t.javaClass.simpleName}: ${t.message}"
                }
            }
            val line = "${case.widget},$w,$h,$scale,$theme,\"${problems.joinToString("; ").replace("\"", "'")}\""
            csv.append(line).append('\n')
            Log.i("WidgetEvidence", line)
        }
        File(dir, "report.csv").writeText(csv.toString())
    }

    // ---- Minimum clean heights --------------------------------------------------------------------

    private class Sweep(val widget: String, val widths: List<Int>, val heights: List<Int>, val build: (Context, WidgetState, Instant) -> RemoteViews)

    /** Per leaf geometry and text problems at one size; empty list = clean. Pair = (score, description). */
    private fun leafProblems(host: View, wPx: Int, hPx: Int): List<Pair<Int, String>> {
        val out = mutableListOf<Pair<Int, String>>()
        fun name(v: View) = (v as? TextView)?.text?.toString()?.take(20)?.let { "\"$it\"" }
            ?: runCatching { v.resources.getResourceEntryName(v.id) }.getOrNull() ?: v.javaClass.simpleName
        fun walk(v: View, left: Int, top: Int) {
            if (v.visibility != View.VISIBLE || v.alpha == 0f) return
            val l = left + v.left - v.scrollX
            val t = top + v.top - v.scrollY
            val leaf = v is TextView || v is android.widget.ImageView || (v as? ViewGroup)?.childCount in listOf(null, 0)
            if (leaf && v.width > 0 && v.height > 0) {
                val o = maxOf(t + v.height - hPx, -t, -l, l + v.width - wPx)
                if (o > 1) out += o to "${name(v)} out by ${o}px"
                if (v is TextView && v.text.isNotEmpty()) {
                    val layout = v.layout
                    if (layout != null) {
                        val avail = v.width - v.compoundPaddingLeft - v.compoundPaddingRight
                        if ((0 until layout.lineCount).any { layout.getEllipsisCount(it) > 0 }) out += 1 to "${name(v)} ellipsized"
                        if ((0 until layout.lineCount).any { layout.getLineWidth(it) > avail + 0.5f }) out += 1 to "${name(v)} too wide"
                        if (layout.lineCount > v.maxLines) out += 1 to "${name(v)} over maxLines"
                    }
                }
            }
            if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i), l, t)
        }
        walk(host, 0, 0)
        return out
    }

    @Test
    fun sweep_minimum_clean_heights() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), now, zone, true) }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { mkdirs() }
        val sweeps = listOf(
            Sweep("strip", listOf(300, 360), (56..240 step 4).toList(), WidgetRenderer::strip),
            Sweep("next", listOf(180, 270), (90..260 step 4).toList(), WidgetRenderer::nextPrayer),
            Sweep("next", listOf(110), (90..200 step 10).toList(), WidgetRenderer::nextPrayer),
            Sweep("today", listOf(180, 250, 330), (110..420 step 10).toList(), WidgetRenderer::today),
        )
        val csv = StringBuilder("widget,wDp,fontScale,cleanHeights,stableFrom,firstDirtyBelowStable,layoutAtMinHeight,layoutAtStable\n")
        for (sw in sweeps) for (w in sw.widths) for (scale in listOf(1.0f, 1.15f, 1.3f, 2.0f)) {
            val config = Configuration(base.resources.configuration).apply {
                fontScale = scale
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            val ctx = base.createConfigurationContext(config)
            val density = ctx.resources.displayMetrics.density
            val wPx = (w * density).toInt()
            val clean = mutableMapOf<Int, Boolean>()
            val dirty = mutableMapOf<Int, String>()
            val layouts = mutableMapOf<Int, String>()
            for (h in sw.heights) {
                val hPx = (h * density).toInt()
                instrumentation.runOnMainSync {
                    try {
                        val host = AppWidgetHostView(ctx)
                        host.updateAppWidgetSize(Bundle(), listOf(SizeF(w.toFloat(), h.toFloat())))
                        host.updateAppWidget(sw.build(ctx, state, now))
                        host.measure(
                            View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY),
                        )
                        host.layout(0, 0, wPx, hPx)
                        val root = host.getChildAt(0)
                        // The layout resource name is not public API; the root's id plus which marker views are shown tells them apart.
                        layouts[h] = if (root == null) "none" else
                            (runCatching { root.resources.getResourceEntryName(root.id) }.getOrNull() ?: "?") + "[" +
                                listOf("widget_progress", "widget_place_chip", "widget_row_isha", "widget_name_dhuhr", "widget_countdown", "widget_bar_dhuhr")
                                    .filter { n -> root.findViewById<View>(root.resources.getIdentifier(n, "id", base.packageName))?.visibility == View.VISIBLE }
                                    .joinToString("+") + "]"
                        val p = leafProblems(host, wPx, hPx)
                        clean[h] = p.isEmpty()
                        if (p.isNotEmpty()) dirty[h] = p.maxBy { it.first }.second
                    } catch (t: Throwable) {
                        clean[h] = false
                        dirty[h] = "CRASH ${t.javaClass.simpleName}"
                    }
                }
            }
            val hs = sw.heights
            val step = hs[1] - hs[0]
            val intervals = mutableListOf<String>()
            var i = 0
            while (i < hs.size) {
                if (clean[hs[i]] == true) {
                    var j = i
                    while (j + 1 < hs.size && clean[hs[j + 1]] == true) j++
                    intervals += if (i == j) "${hs[i]}" else "${hs[i]}-${hs[j]}"
                    i = j + 1
                } else i++
            }
            var k = hs.size
            while (k > 0 && clean[hs[k - 1]] == true) k--
            val stable = if (k < hs.size) hs[k] else null
            val below = if (k > 0) dirty[hs[k - 1]] else null
            val line = "${sw.widget},$w,$scale,\"clean: ${intervals.joinToString(", ").ifEmpty { "none" }}\"," +
                "${stable ?: "never"},\"${(if (k > 0) "${hs[k - 1]}: " else "") + (below ?: "-")}\"," +
                "${layouts[hs.first()]},${stable?.let { layouts[it] } ?: "-"}"
            csv.append(line).append('\n')
            Log.i("WidgetEvidence", line)
        }
        File(dir, "sweep.csv").writeText(csv.toString())
    }

    // ---- Height the layout wants ------------------------------------------------------------------

    @Test
    fun needed_heights() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { mkdirs() }
        val stripDir = File(dir, "strip-h").apply { deleteRecursively(); mkdirs() }
        val dates = listOf("ramadan" to Instant.parse("2027-02-22T09:00:00Z"), "normal" to Instant.parse("2026-11-04T09:00:00Z"))
        val cases = listOf(
            Triple("strip", listOf(300 to 40, 300 to 64, 300 to 110, 300 to 150, 400 to 110, 400 to 150, 360 to 110, 360 to 150), WidgetRenderer::strip),
            Triple("next", listOf(180 to 110, 270 to 150, 270 to 110, 180 to 150), WidgetRenderer::nextPrayer),
            Triple("today", listOf(250 to 180, 250 to 280, 330 to 330), WidgetRenderer::today),
        )
        val csv = StringBuilder("widget,W,Hnominal,fs,date,rootNeededDp,hostNeededDp\n")
        for ((dateName, at) in dates) {
            val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), at, zone, true) }
            for ((widget, sizes, build) in cases) for ((w, h) in sizes) for (scale in listOf(1.0f, 1.15f, 1.3f, 2.0f)) {
                val config = Configuration(base.resources.configuration).apply {
                    fontScale = scale
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
                }
                val ctx = base.createConfigurationContext(config)
                val density = ctx.resources.displayMetrics.density
                val wPx = (w * density).toInt()
                val hPx = (h * density).toInt()
                var rootDp = "?"
                var hostDp = "?"
                instrumentation.runOnMainSync {
                    try {
                        val host = AppWidgetHostView(ctx)
                        host.updateAppWidgetSize(Bundle(), listOf(SizeF(w.toFloat(), h.toFloat())))
                        host.updateAppWidget(build(ctx, state, at))
                        val exactW = View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY)
                        host.measure(exactW, View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY))
                        host.layout(0, 0, wPx, hPx)
                        val free = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        host.measure(exactW, free)
                        hostDp = "%.1f".format(host.measuredHeight / density)
                        host.getChildAt(0)?.let {
                            it.measure(exactW, free)
                            rootDp = "%.1f".format(it.measuredHeight / density)
                        }
                    } catch (t: Throwable) {
                        rootDp = "CRASH ${t.javaClass.simpleName}"
                    }
                }
                val line = "$widget,$w,$h,$scale,$dateName,$rootDp,$hostDp" + if (rootDp != hostDp) " (differ)" else ""
                csv.append(line).append('\n')
                Log.i("WidgetEvidence", line)
            }
        }
        File(dir, "needed.csv").writeText(csv.toString())

        // Pictures to see where the strip's clip stops.
        val normal = runBlocking { WidgetState.build(SettingsRepository(base).current(), dates[1].second, zone, true) }
        for (scale in listOf(1.0f, 1.15f)) for (h in listOf(110, 120, 124, 128, 132, 140)) {
            val config = Configuration(base.resources.configuration).apply {
                fontScale = scale
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            val ctx = base.createConfigurationContext(config)
            val density = ctx.resources.displayMetrics.density
            val wPx = (300 * density).toInt()
            val hPx = (h * density).toInt()
            instrumentation.runOnMainSync {
                val host = AppWidgetHostView(ctx)
                host.updateAppWidgetSize(Bundle(), listOf(SizeF(300f, h.toFloat())))
                host.updateAppWidget(WidgetRenderer.strip(ctx, normal, dates[1].second))
                host.measure(View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY))
                host.layout(0, 0, wPx, hPx)
                val bitmap = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
                Canvas(bitmap).apply { drawColor(Color.parseColor("#DDDDDD")); host.draw(this) }
                FileOutputStream(File(stripDir, "strip_300x${h}_fs${scale}_normal_light.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
    }

    // ---- After the threshold change ---------------------------------------------------------------

    private fun hostAt(ctx: Context, build: (Context, WidgetState, Instant) -> RemoteViews, state: WidgetState, at: Instant, w: Int, h: Int): Pair<AppWidgetHostView, Float> {
        val density = ctx.resources.displayMetrics.density
        val wPx = (w * density).toInt()
        val hPx = (h * density).toInt()
        val host = AppWidgetHostView(ctx)
        host.updateAppWidgetSize(Bundle(), listOf(SizeF(w.toFloat(), h.toFloat())))
        host.updateAppWidget(build(ctx, state, at))
        val exactW = View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY)
        host.measure(exactW, View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY))
        host.layout(0, 0, wPx, hPx)
        val bitmap = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.parseColor("#DDDDDD")); host.draw(this) }
        lastBitmap = bitmap
        host.measure(exactW, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        return host to host.measuredHeight / density
    }

    private var lastBitmap: Bitmap? = null

    @Test
    fun verify_new_thresholds() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { mkdirs() }
        val afterDir = File(dir, "after").apply { deleteRecursively(); mkdirs() }
        val dates = listOf("normal" to Instant.parse("2026-11-04T09:00:00Z"), "ramadan" to Instant.parse("2027-02-22T09:00:00Z"))
        val all = listOf(1.0f, 1.15f, 1.3f, 2.0f)
        // widget, W, H, scales it applies to
        class Row(val widget: String, val w: Int, val h: Int, val scales: List<Float>, val build: (Context, WidgetState, Instant) -> RemoteViews)
        val rows = listOf(
            Row("strip", 300, 40, all, WidgetRenderer::strip), Row("strip", 400, 40, all, WidgetRenderer::strip),
            Row("strip", 300, 86, all, WidgetRenderer::strip), Row("strip", 400, 86, all, WidgetRenderer::strip),
            Row("strip", 300, 148, all, WidgetRenderer::strip), Row("strip", 400, 148, all, WidgetRenderer::strip),
            Row("strip", 300, 152, all, WidgetRenderer::strip), Row("strip", 400, 152, all, WidgetRenderer::strip),
            Row("next", 180, 110, all, WidgetRenderer::nextPrayer), Row("next", 270, 158, all, WidgetRenderer::nextPrayer),
            Row("today", 250, 180, all, WidgetRenderer::today), Row("today", 250, 300, listOf(1.0f), WidgetRenderer::today),
            Row("today", 250, 324, listOf(1.15f), WidgetRenderer::today),
        )
        val pictures = setOf("strip_300x132", "next_180x132", "today_250x300")
        val csv = StringBuilder("widget,nominalW,nominalH,fs,date,neededDp,fits\n")
        for ((dateName, at) in dates) {
            val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), at, zone, true) }
            for (r in rows) for (scale in r.scales) {
                val config = Configuration(base.resources.configuration).apply {
                    fontScale = scale
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
                }
                val ctx = base.createConfigurationContext(config)
                var needed = -1f
                instrumentation.runOnMainSync {
                    try {
                        needed = hostAt(ctx, r.build, state, at, r.w, r.h).second
                        val key = "${r.widget}_${r.w}x${r.h}"
                        if (dateName == "normal" && key in pictures && scale in listOf(1.0f, 1.15f)) {
                            FileOutputStream(File(afterDir, "${key}_fs${scale}_normal_light.png")).use { lastBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        }
                    } catch (t: Throwable) {
                        Log.e("WidgetEvidence", "crash", t)
                    }
                }
                val line = "${r.widget},${r.w},${r.h},$scale,$dateName,${"%.1f".format(needed)},${if (needed >= 0 && needed <= r.h + 0.5f) "yes" else "no"}"
                csv.append(line).append('\n')
                Log.i("WidgetEvidence", line)
            }
        }
        File(dir, "verify.csv").writeText(csv.toString())
    }

    // ---- Tall scripts -----------------------------------------------------------------------------

    private fun firstText(v: View): String? {
        if (v is TextView && v.text.isNotBlank()) return v.text.toString()
        if (v is ViewGroup) for (i in 0 until v.childCount) firstText(v.getChildAt(i))?.let { return it }
        return null
    }

    @Test
    fun needed_heights_tall_scripts() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { mkdirs() }
        val tallDir = File(dir, "tall").apply { deleteRecursively(); mkdirs() }
        val at = Instant.parse("2026-11-04T09:00:00Z")
        val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), at, zone, true) }
        class Row(val widget: String, val w: Int, val h: Int, val scales: List<Float>, val build: (Context, WidgetState, Instant) -> RemoteViews)
        val both = listOf(1.0f, 1.15f)
        val rows = listOf(
            Row("today", 250, 180, both, WidgetRenderer::today), Row("today", 250, 346, listOf(1.0f), WidgetRenderer::today),
            Row("today", 250, 374, listOf(1.15f), WidgetRenderer::today),
            Row("next", 180, 110, both, WidgetRenderer::nextPrayer),
            Row("next", 270, 170, listOf(1.0f), WidgetRenderer::nextPrayer), Row("next", 270, 192, listOf(1.15f), WidgetRenderer::nextPrayer),
            Row("strip", 300, 40, both, WidgetRenderer::strip), Row("strip", 300, 86, both, WidgetRenderer::strip),
            Row("strip", 300, 148, both, WidgetRenderer::strip), Row("strip", 300, 152, both, WidgetRenderer::strip),
        )
        val csv = StringBuilder("widget,lang,W,Hnominal,fs,neededDp,fits\n")
        for (lang in listOf("ur", "ar", "bn")) for (r in rows) for (scale in r.scales) {
            // Locale on the configuration is all it takes: app_language_tag resolves from values-<lang>, and AppLocale.of reads that.
            val config = Configuration(base.resources.configuration).apply {
                fontScale = scale
                setLocale(java.util.Locale.forLanguageTag(lang))
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            val ctx = base.createConfigurationContext(config)
            var needed = -1f
            instrumentation.runOnMainSync {
                try {
                    val (host, n) = hostAt(ctx, r.build, state, at, r.w, r.h)
                    needed = n
                    if (r.widget == "today" && r.h >= 346 && lang in listOf("ur", "bn")) {
                        FileOutputStream(File(tallDir, "today_${lang}_250x${r.h}_fs${scale}_normal_light.png")).use { lastBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    }
                    if (r.widget == "today" && r.h == 180 && scale == 1.0f) {
                        Log.i("WidgetEvidence", "locale check $lang: AppLocale.of=${com.sajdatime.core.AppLocale.of(ctx).language} firstText=${firstText(host)}")
                    }
                } catch (t: Throwable) {
                    Log.e("WidgetEvidence", "crash", t)
                }
            }
            val line = "${r.widget},$lang,${r.w},${r.h},$scale,${"%.1f".format(needed)},${if (needed >= 0 && needed <= r.h + 0.5f) "yes" else "no"}"
            csv.append(line).append('\n')
            Log.i("WidgetEvidence", line)
        }
        File(dir, "tall.csv").writeText(csv.toString())
    }

    // ---- Next prayer, short variants --------------------------------------------------------------

    @Test
    fun next_short_heights() {
        runBlocking { SettingsRepository(base).setLocation(Coordinates(51.5074, -0.1278), "London") }
        val dir = File(base.getExternalFilesDir(null), "widget-evidence").apply { mkdirs() }
        val picDir = File(dir, "next-short").apply { deleteRecursively(); mkdirs() }
        val at = Instant.parse("2026-11-04T09:00:00Z")
        val state = runBlocking { WidgetState.build(SettingsRepository(base).current(), at, zone, true) }
        fun ctxFor(lang: String, scale: Float): Context {
            val config = Configuration(base.resources.configuration).apply {
                fontScale = scale
                if (lang != "en") setLocale(java.util.Locale.forLanguageTag(lang))
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            return base.createConfigurationContext(config)
        }
        // lang, heights, scales
        val plan = listOf(
            Triple("en", listOf(110, 132), listOf(1.0f, 1.15f, 1.3f, 2.0f)),
            Triple("en", listOf(134, 135, 136), listOf(1.0f, 1.15f)),
        ) + listOf("ur", "ar", "bn").map { Triple(it, listOf(110, 146), listOf(1.0f, 1.15f)) }
        val csv = StringBuilder("widget,lang,W,Hnominal,fs,neededDp,fits,barsShown\n")
        for ((lang, heights, scales) in plan) for (h in heights) for (scale in scales) {
            val ctx = ctxFor(lang, scale)
            var needed = -1f
            var bars = "?"
            instrumentation.runOnMainSync {
                val (host, n) = hostAt(ctx, WidgetRenderer::nextPrayer, state, at, 180, h)
                needed = n
                bars = host.findViewById<View>(R.id.widget_progress)?.visibility?.let { if (it == View.VISIBLE) "shown" else "hidden" } ?: "absent"
            }
            val line = "next,$lang,180,$h,$scale,${"%.1f".format(needed)},${if (needed <= h + 0.5f) "yes" else "no"},$bars"
            csv.append(line).append('\n')
            Log.i("WidgetEvidence", line)
        }
        File(dir, "next-short.csv").writeText(csv.toString())
        val pics = listOf("en" to (listOf(110, 122, 135, 136) to listOf(1.0f, 1.15f)), "ur" to (listOf(110, 146) to listOf(1.0f)))
        for ((lang, hs) in pics) for (scale in hs.second) for (h in hs.first) {
            instrumentation.runOnMainSync {
                hostAt(ctxFor(lang, scale), WidgetRenderer::nextPrayer, state, at, 180, h)
                FileOutputStream(File(picDir, "next_${lang}_180x${h}_fs${scale}_normal_light.png")).use { lastBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
    }

    private fun shown(v: View): Boolean {
        var x: View? = v
        while (x != null) {
            if (x.visibility != View.VISIBLE) return false
            x = x.parent as? View
        }
        return true
    }

    private fun inspect(view: View, out: MutableList<String>) {
        if (!shown(view)) return
        if (view is TextView && view.text.isNotEmpty()) {
            val label = "\"${view.text.toString().take(24)}\""
            if (view.width == 0 || view.height == 0) {
                out += "$label zero size ${view.width}x${view.height}"
            } else {
                val layout = view.layout
                if (layout != null) {
                    val avail = view.width - view.compoundPaddingLeft - view.compoundPaddingRight
                    val availH = view.height - view.compoundPaddingTop - view.compoundPaddingBottom
                    if ((0 until layout.lineCount).any { layout.getEllipsisCount(it) > 0 }) out += "$label ellipsized"
                    if ((0 until layout.lineCount).any { layout.getLineWidth(it) > avail + 0.5f }) out += "$label too wide"
                    if (view.maxLines in 1 until Int.MAX_VALUE && layout.lineCount > view.maxLines) out += "$label over maxLines"
                    if (layout.height > availH + 1) out += "$label text taller than box (${layout.height}>$availH)"
                }
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val c = view.getChildAt(i)
                if (c.visibility == View.VISIBLE && shown(c) && c.width > 0 && c.height > 0 &&
                    (c.bottom > view.height || c.top < 0)
                ) {
                    out += "${c.javaClass.simpleName}#${c.id.takeIf { it != View.NO_ID }?.let { runCatching { c.resources.getResourceEntryName(it) }.getOrNull() }} clipped v (${c.top}..${c.bottom} of ${view.height})"
                }
                inspect(c, out)
            }
        }
    }
}
