package com.sajdatime.app

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import com.sajdatime.app.widget.NextPrayerWidgetProvider
import com.sajdatime.app.widget.TodayWidgetProvider
import com.sajdatime.app.widget.WidgetRefreshScheduler
import com.sajdatime.app.widget.WidgetState
import com.sajdatime.core.AppLocale
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.label
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId

/**
 * Both widgets, bound for real through the system's widget service and drawn by a host the way a
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
        for (provider in listOf(NextPrayerWidgetProvider::class.java, TodayWidgetProvider::class.java)) {
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

    // ---- The redraw alarm ----------------------------------------------------------------------

    @Test
    fun the_redraw_alarm_exists_only_while_a_widget_does_and_is_set_for_the_right_moment() {
        val pkg = context.packageName
        val action = WidgetRefreshScheduler.ACTION_REFRESH
        assertEquals("an alarm is set with no widget placed", 0, DeviceSupport.alarmTimes(pkg, action).size)

        val (id, view) = bindOrFail(NextPrayerWidgetProvider::class.java)
        waitFor("the first draw") { view.textOf("widget_name")?.text?.isNotEmpty() == true }
        waitFor("the redraw alarm to be registered") { DeviceSupport.alarmTimes(pkg, action).isNotEmpty() }

        val (state, now) = stateNow()
        val expected = WidgetRefreshScheduler.nextRefreshAt(state.next!!.at, now, zone)
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
    }

    private fun tag(): String = if (context.resources.configuration.fontScale > 1.0f) "font${context.resources.configuration.fontScale}" else "normal"

    private companion object {
        const val HOST_ID = 0x5A1D
    }
}
