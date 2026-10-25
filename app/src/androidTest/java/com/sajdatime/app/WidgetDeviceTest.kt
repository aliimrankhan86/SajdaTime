package com.sajdatime.app

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.widget.NextPrayerWidgetProvider
import com.sajdatime.app.widget.TodayStripWidgetProvider
import com.sajdatime.app.widget.TodayWidgetProvider
import com.sajdatime.app.widget.WidgetRefreshScheduler
import com.sajdatime.app.widget.WidgetState
import com.sajdatime.app.widget.WidgetUpdater
import com.sajdatime.core.AppLocale
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.label
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId

/**
 * The three widgets, bound for real through the system's widget service and drawn by a host the way a
 * launcher draws them, then read back. Everything the JVM tests cannot say: that the layouts inflate in
 * the system's `RemoteViews` host, that the provider is reachable and updates, that the words and the
 * highlight match what the engine says, and that the redraw alarm is really registered with the
 * AlarmManager and really goes when the last widget does.
 *
 * Needs the bind permission granted to the shell once per emulator boot, which the release script does
 * before Gradle: `adb shell appwidget grantbind --package com.sajdatime.app --user 0`. Without it
 * [bindOrFail] says so in plain words.
 *
 * Runs against whatever the clock says. The release script sets it to 22 February 2027 (day 15 of
 * Ramadan 1448) and passes `requireRamadanClock`, so the Ramadan words are asserted as present; on any
 * other day the same test asserts they are absent. The pictures it draws land in
 * `device-renders/` in the app's external files folder for the script to pull.
 */
@RunWith(AndroidJUnit4::class)
class WidgetDeviceTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    private val host = AppWidgetHost(context, HOST_ID)
    private val bound = mutableListOf<Int>()
    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.systemDefault()
    private val appContext: Context get() = AppLocale.wrap(context)

    @Before
    fun setUp() {
        runBlocking { SettingsRepository(context).setLocation(london, "London") }
        removeAllOurWidgets()
    }

    @After
    fun tearDown() {
        bound.forEach { host.deleteAppWidgetId(it) }
        host.stopListening()
        removeAllOurWidgets()
    }

    private fun removeAllOurWidgets() {
        for (provider in listOf(NextPrayerWidgetProvider::class.java, TodayWidgetProvider::class.java, TodayStripWidgetProvider::class.java)) {
            manager.getAppWidgetIds(ComponentName(context, provider)).forEach { host.deleteAppWidgetId(it) }
        }
    }

    private fun bindOrFail(provider: Class<*>): Pair<Int, AppWidgetHostView> {
        val id = host.allocateAppWidgetId()
        bound += id
        val allowed = manager.bindAppWidgetIdIfAllowed(id, ComponentName(context, provider))
        assertTrue(
            "The shell has not been given permission to bind widgets. Run: " +
                "adb shell appwidget grantbind --package ${context.packageName} --user 0",
            allowed,
        )
        host.startListening()
        var view: AppWidgetHostView? = null
        instrumentation.runOnMainSync { view = host.createView(context, id, manager.getAppWidgetInfo(id)) }
        return id to view!!
    }

    /** Waits for the provider's first update to reach the host view. */
    private fun waitFor(what: String, timeoutMs: Long = 15_000, ok: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            var done = false
            instrumentation.runOnMainSync { done = ok() }
            if (done) return
            SystemClock.sleep(200)
        }
        throw AssertionError("Timed out waiting for $what")
    }

    private fun View.textOf(name: String): TextView? {
        val id = context.resources.getIdentifier(name, "id", context.packageName)
        return findViewById(id)
    }

    private fun layoutAt(view: View, widthDp: Int, heightDp: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val w = (widthDp * density).toInt()
        val h = (heightDp * density).toInt()
        var bitmap: Bitmap? = null
        instrumentation.runOnMainSync {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, w, h)
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
                Canvas(it).apply { drawColor(Color.parseColor("#888888")); view.draw(this) }
            }
        }
        return bitmap!!
    }

    private fun save(name: String, bitmap: Bitmap) {
        FileOutputStream(java.io.File(DeviceSupport.renderDir(), "$name.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun ellipsised(view: View?): Boolean {
        val layout = (view as? TextView)?.layout ?: return false
        return (0 until layout.lineCount).any { layout.getEllipsisCount(it) > 0 }
    }

    private fun stateNow(): Pair<WidgetState, Instant> {
        val now = Instant.now()
        val settings = runBlocking { SettingsRepository(context).current() }
        return WidgetState.build(settings, now, zone, PrayerAlarmScheduler.canScheduleExact(context)) to now
    }

    // ---- Today -------------------------------------------------------------------------------

    @Test
    fun the_today_widget_shows_six_rows_the_next_one_marked_and_the_ramadan_words_only_in_ramadan() {
        val (_, view) = bindOrFail(TodayWidgetProvider::class.java)
        waitFor("the Today widget's first draw") { view.textOf("widget_time_fajr")?.text?.isNotEmpty() == true }
        val bitmap = layoutAt(view, 250, 180)
        save("widget-today-${tag()}", bitmap)

        val (state, _) = stateNow()
        val marker = appContext.getString(R.string.home_next_marker)
        val suhoor = appContext.getString(R.string.widget_suhoor_ends)
        val iftar = appContext.getString(R.string.widget_iftar)
        val fontScale = context.resources.configuration.fontScale

        for (slot in PrayerSlot.entries) {
            val key = slot.name.lowercase()
            val name = view.textOf("widget_name_$key")
            val time = view.textOf("widget_time_$key")
            val label = view.textOf("widget_label_$key")
            assertNotNull("row $key has no name view", name)
            assertTrue("row $key is not visible", (name!!.parent as View).visibility == View.VISIBLE)
            assertEquals("row $key name", slot.label(appContext), name.text.toString())
            assertTrue("row $key has no time", time!!.text.isNotEmpty())
            if (fontScale <= 1.0f) assertFalse("the name of row $key is cut off at normal font size", ellipsised(name))

            val labelText = label!!.text.toString()
            assertEquals("row $key marker", state.isHighlighted(slot), labelText.contains(marker))
            val expectedWord = when {
                slot == PrayerSlot.FAJR && state.ramadanToday -> suhoor
                slot == PrayerSlot.MAGHRIB && state.ramadanToday -> iftar
                else -> null
            }
            if (expectedWord != null) assertTrue("row $key lacks \"$expectedWord\": \"$labelText\"", labelText.contains(expectedWord))
            if (expectedWord == null) assertFalse("row $key has a Ramadan word on a day that is not Ramadan: \"$labelText\"", labelText.contains(suhoor) || labelText.contains(iftar))
        }
        if (DeviceSupport.requireRamadanClock) {
            assertTrue("The clock was meant to be inside Ramadan 1448 but the widget says it is not", state.ramadanToday)
        }
    }

    // ---- Today strip -------------------------------------------------------------------------

    /**
     * Step 8. The strip's root description starts with the town, names the five prayers and leaves out
     * Sunrise (it is not shown); at 1.3 text the five columns are gone and the next prayer alone shows.
     */
    @Test
    fun the_strip_speaks_the_town_and_five_prayers_and_shows_one_at_large_text() {
        val (_, view) = bindOrFail(TodayStripWidgetProvider::class.java)
        // The host view's own description defaults to the provider label, so wait for the one the renderer writes
        // on the layout's root, which leads with the town.
        fun spoken() = view.findViewById<View>(R.id.widget_root)?.contentDescription?.toString().orEmpty()
            // The town is wrapped in bidi isolates so it reads right in Urdu; they are not words.
            .filter { it !in '\u2066'..'\u2069' }
        waitFor("the strip's first draw") { spoken().startsWith("London") }
        save("widget-strip-${tag()}", layoutAt(view, 380, 64))

        val said = spoken()
        assertTrue("the town leads: \"$said\"", said.startsWith("London"))
        for (slot in listOf(PrayerSlot.FAJR, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.MAGHRIB, PrayerSlot.ISHA)) {
            assertTrue("$slot is not spoken: \"$said\"", said.contains(slot.label(appContext)))
        }
        assertFalse("Sunrise is not on the strip: \"$said\"", said.contains(PrayerSlot.SUNRISE.label(appContext)))

        val columnsVisible = view.textOf("widget_name_dhuhr")?.visibility == View.VISIBLE &&
            (view.textOf("widget_name_dhuhr")!!.parent as View).visibility == View.VISIBLE
        val nextRow = view.textOf("widget_name")?.parent as? View
        if (context.resources.configuration.fontScale >= 1.3f) {
            assertFalse("the five columns are still shown at large text", columnsVisible)
            assertEquals("the next prayer row is not shown at large text", View.VISIBLE, nextRow?.visibility)
            // Opus review, 25 Oct 2026: the town chip would squeeze the one prayer left, so it is hidden at every width.
            assertNotEquals("the town chip shows at large text", View.VISIBLE, view.findViewById<View>(R.id.widget_place_chip)?.visibility)
        } else {
            assertTrue("the five columns are missing at normal text", columnsVisible)
            assertEquals("the large text row shows at normal text", View.GONE, nextRow?.visibility)
        }
    }

    // ---- Next prayer -------------------------------------------------------------------------

    @Test
    fun the_next_prayer_widget_shows_the_next_prayer_its_time_and_the_right_context_line() {
        val (_, view) = bindOrFail(NextPrayerWidgetProvider::class.java)
        waitFor("the Next widget's first draw") { view.textOf("widget_name")?.text?.isNotEmpty() == true }
        save("widget-next-${tag()}", layoutAt(view, 180, 110))

        val (state, _) = stateNow()
        val next = state.next!!
        assertEquals(next.slot.label(appContext), view.textOf("widget_name")!!.text.toString())
        assertEquals(TimeFormat.clock(appContext, next.at), view.textOf("widget_time")!!.text.toString())

        val sub = view.textOf("widget_sublabel")!!
        val word = state.ramadanWordFor(next.slot, state.ramadanOnNextDay)
        val suhoor = appContext.getString(R.string.widget_suhoor_ends)
        val iftar = appContext.getString(R.string.widget_iftar)
        val text = if (sub.visibility == View.VISIBLE) sub.text.toString() else ""
        when (word) {
            WidgetState.RamadanWord.SUHOOR_ENDS -> if (!state.nextIsApproximate) assertTrue("expected \"$suhoor\" in \"$text\"", text.contains(suhoor))
            WidgetState.RamadanWord.IFTAR -> if (!state.nextIsApproximate) assertTrue("expected \"$iftar\" in \"$text\"", text.contains(iftar))
            null -> assertFalse("a Ramadan word on a prayer that has none: \"$text\"", text.contains(suhoor) || text.contains(iftar))
        }
        assertEquals(
            "the countdown follows whether exact alarms are allowed",
            state.countdownAllowed,
            view.textOf("widget_countdown")!!.visibility == View.VISIBLE,
        )
    }

    // ---- The app's Theme row ---------------------------------------------------------------------

    /**
     * Owner's request, 19 Oct 2026: a widget is dark when Dark is chosen in the app and light when Light
     * is, whatever the phone is set to, and follows the phone again under System. The step back to
     * System is the one a launcher could get wrong: it must inflate afresh (a new layout id) or the
     * pinned colours would survive. So the theme just before System is the OPPOSITE of the phone's mode,
     * read here, or a surviving pinned colour would equal the System one and the check could not see it
     * (found by the independent review, 19 Oct 2026). The renders are the screenshots for the owner.
     */
    @Test
    fun the_widgets_follow_the_apps_theme_and_return_to_the_phone_under_system() {
        val settings = SettingsRepository(context)
        try {
            val (_, today) = bindOrFail(TodayWidgetProvider::class.java)
            val (_, next) = bindOrFail(NextPrayerWidgetProvider::class.java)
            waitFor("the first draw") { today.textOf("widget_name_dhuhr")?.text?.isNotEmpty() == true && next.textOf("widget_name")?.text?.isNotEmpty() == true }
            val phoneDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val light = ThemeChoice.LIGHT to R.color.widget_light_on_surface
            val dark = ThemeChoice.DARK to R.color.widget_dark_on_surface
            val opposite = if (phoneDark) light else dark
            assertTrue(
                "the theme before System must differ from the phone's, or the check below proves nothing",
                context.getColor(opposite.second) != context.getColor(R.color.widget_on_surface),
            )
            for ((choice, ink) in listOf(if (phoneDark) dark else light, opposite, ThemeChoice.SYSTEM to R.color.widget_on_surface)) {
                runBlocking {
                    settings.setThemeChoice(choice)
                    WidgetUpdater.refreshAll(context)
                }
                val expected = context.getColor(ink)
                waitFor("the Today widget under $choice") { today.textOf("widget_name_dhuhr")?.currentTextColor == expected }
                waitFor("the Next widget under $choice") { next.textOf("widget_name")?.currentTextColor == expected }
                val tag = "theme-${choice.name.lowercase()}"
                save("widget-today-$tag", layoutAt(today, 250, 180))
                save("widget-next-$tag", layoutAt(next, 180, 110))
            }
        } finally {
            runBlocking {
                settings.setThemeChoice(ThemeChoice.SYSTEM)
                WidgetUpdater.refreshAll(context)
            }
        }
    }

    // ---- The redraw alarm ----------------------------------------------------------------------

    @Test
    fun the_redraw_alarm_exists_only_while_a_widget_does_and_is_set_for_the_right_moment() {
        val pkg = context.packageName
        val action = WidgetRefreshScheduler.ACTION_REFRESH
        // Since 17 Oct 2026 the same alarm also reposts the next prayer badge, so it stays while the
        // badge is on (the default). This test is about widgets, so the badge is off for it, and any
        // alarm the badge left is cleared through the same path the app uses.
        runBlocking { SettingsRepository(context).setOngoingBadge(false) }
        // Right after a cold boot the app's own boot handling (SystemEventReceiver) can still be running
        // with the badge on, and lays the alarm down again just after this cancel: the release script,
        // which starts the emulator and runs this at once, failed here on 19 Oct 2026 and the same suite
        // passed minutes later. So the cancel is repeated until it holds; the claim checked is unchanged.
        val settled = SystemClock.uptimeMillis() + 30_000
        do {
            runBlocking { com.sajdatime.app.widget.WidgetUpdater.refreshAll(context) }
            val left = DeviceSupport.alarmTimes(pkg, action).size
            if (left == 0 || SystemClock.uptimeMillis() > settled) {
                assertEquals("an alarm is set with no widget placed", 0, left)
                break
            }
            SystemClock.sleep(500)
        } while (true)

        val (id, view) = bindOrFail(NextPrayerWidgetProvider::class.java)
        waitFor("the first draw") { view.textOf("widget_name")?.text?.isNotEmpty() == true }
        waitFor("the redraw alarm to be registered") { DeviceSupport.alarmTimes(pkg, action).isNotEmpty() }

        val (state, now) = stateNow()
        val expected = WidgetRefreshScheduler.nextRefreshAt(state.next!!.at, now, zone, state.today?.get(PrayerSlot.SUNRISE))
            .atZone(zone).toLocalDateTime()
        val set = DeviceSupport.alarmTimes(pkg, action)
        assertEquals("exactly one redraw alarm", 1, set.size)
        // The next prayer may have moved by a minute between the draw and this read, so the check is the
        // alarm's own rule: it is the next prayer or midnight plus two seconds, to the minute.
        val gap = java.time.Duration.between(expected, set.single()).abs()
        assertTrue("alarm at ${set.single()}, expected about $expected", gap.toMinutes() <= 1)

        host.deleteAppWidgetId(id)
        bound.remove(id)
        waitFor("the alarm to be cancelled with the last widget", timeoutMs = 20_000) {
            DeviceSupport.alarmTimes(pkg, action).isEmpty()
        }
        runBlocking { SettingsRepository(context).setOngoingBadge(true) }
    }

    @Test
    fun the_redraw_alarm_stays_for_the_badge_when_no_widget_is_placed() {
        val pkg = context.packageName
        val action = WidgetRefreshScheduler.ACTION_REFRESH
        runBlocking { SettingsRepository(context).setOngoingBadge(true) }
        runBlocking { com.sajdatime.app.widget.WidgetUpdater.refreshAll(context) }
        waitFor("the badge's repost alarm with no widget placed") { DeviceSupport.alarmTimes(pkg, action).size == 1 }
    }

    private fun tag(): String = if (context.resources.configuration.fontScale > 1.0f) "font${context.resources.configuration.fontScale}" else "normal"

    private companion object {
        const val HOST_ID = 0x5A1D
    }
}
