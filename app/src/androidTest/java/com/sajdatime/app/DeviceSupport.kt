package com.sajdatime.app

import android.app.Instrumentation
import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime

/**
 * Small helpers the device tests share. Kept here so each test reads as what it checks.
 */
internal object DeviceSupport {

    private val instrumentation: Instrumentation get() = InstrumentationRegistry.getInstrumentation()

    /** Runs a shell command as the shell user and returns everything it printed. */
    fun shell(command: String): String =
        instrumentation.uiAutomation.executeShellCommand(command).use { fd ->
            // Read to the end: the command has not finished until the pipe closes.
            FileInputStream(fd.fileDescriptor).use { String(it.readBytes()) }
        }

    /**
     * True when the release script asked for the strict form of a test: the clock is meant to be inside
     * Ramadan 1448, so a clock that is not is a failure and not a reason to skip. Passed as
     * `-Pandroid.testInstrumentationRunnerArguments.requireRamadanClock=true`.
     */
    val requireRamadanClock: Boolean
        get() = InstrumentationRegistry.getArguments().getString("requireRamadanClock") == "true"

    /**
     * Turns the screen upright and holds it there; call what it returns to put the owner's rotation back.
     *
     * For tests that draw their own screen. A phone lying flat keeps whichever way it was last tilted, so
     * on the owner's desk the test activity came up on its side; the tasbeeh then asked to be upright
     * (`KeepUpright`), Android made the activity again, and the test's screen was gone ("No compose
     * hierarchies found", the Redmi, 12 Nov 2026).
     */
    fun holdUpright(): () -> Unit {
        val user = shell("settings get system user_rotation").trim()
        val auto = shell("settings get system accelerometer_rotation").trim()
        instrumentation.uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0)
        // The turn is animated and anything already on screen is made again; a screen drawn before it
        // settles is lost with the old activity.
        Thread.sleep(1_500)
        return {
            // Upright first, then let go: letting go from its side left the emulator on its side.
            instrumentation.uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_0)
            instrumentation.uiAutomation.setRotation(UiAutomation.ROTATION_UNFREEZE)
            // `settings get` prints "null" for a key that was never set: then there is nothing to put back.
            if (user.toIntOrNull() != null) shell("settings put system user_rotation $user")
            if (auto.toIntOrNull() != null) shell("settings put system accelerometer_rotation $auto")
        }
    }

    /** Where the pictures go. The shell can pull this folder without any special access. */
    fun renderDir(): File =
        File(instrumentation.targetContext.getExternalFilesDir(null), "device-renders").apply { mkdirs() }

    /**
     * The `origWhen` of every alarm this app has set for [action], read from `dumpsys alarm`.
     *
     * The dump has more than one place that mentions an action (the pending alarms and the statistics at
     * the end), so counting matching lines would count one alarm twice. This walks the entries that start
     * with `Alarm{` and keeps those owned by exactly this package and carrying the action, and reads the
     * `origWhen=` the entry prints in local time. Format as recorded in docs/HANDOVER.md section 10:
     * `origWhen=2026-08-02 02:51:00.000`.
     */
    fun alarmTimes(packageName: String, action: String): List<LocalDateTime> {
        val lines = shell("dumpsys alarm").lines()
        val blocks = mutableListOf<MutableList<String>>()
        // An entry ends at the first line that is not indented deeper than its header. Without this the last
        // entry ("Next wake from idle: Alarm{... this.package}", near the end of the dump) swallowed the
        // statistics and removal history after it, which name WIDGET_REFRESH for every alarm ever cancelled,
        // so a dump with no redraw alarm at all counted one (25 Oct 2026, step 8 device run).
        var headerIndent = 0
        var inside = false
        for (line in lines) {
            val indent = line.length - line.trimStart().length
            if (line.contains("Alarm{")) {
                blocks += mutableListOf(line)
                headerIndent = indent
                inside = true
            } else if (inside && indent > headerIndent) {
                blocks.last().add(line)
            } else {
                inside = false
            }
        }
        val stamp = Regex("""origWhen=(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2}:\d{2})""")
        return blocks
            // The owner is the last word of the `Alarm{… com.sajdatime.app}` line, matched exactly: a contains()
            // also matched the sideload copy (`…app.sideload}`, same action names) and failed WidgetDeviceTest.
            .filter { block -> block.first().trimEnd().endsWith(" $packageName}") && block.any { it.contains(action) } }
            .mapNotNull { block ->
                block.firstNotNullOfOrNull { stamp.find(it) }?.let { LocalDateTime.parse("${it.groupValues[1]}T${it.groupValues[2]}") }
            }
            .sorted()
    }
}

/**
 * [DeviceSupport.holdUpright] as a rule, for tests whose screen is opened by another rule: give it
 * `order = 0` and the Compose rule `order = 1`, so the screen has stopped turning before anything is drawn.
 */
class UprightRule : org.junit.rules.ExternalResource() {
    private var putBack: () -> Unit = {}
    override fun before() {
        putBack = DeviceSupport.holdUpright()
    }

    override fun after() = putBack()
}
