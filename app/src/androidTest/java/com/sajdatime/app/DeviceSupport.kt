package com.sajdatime.app

import android.app.Instrumentation
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

    /** Where the pictures go. The shell can pull this folder without any special access. */
    fun renderDir(): File =
        File(instrumentation.targetContext.getExternalFilesDir(null), "device-renders").apply { mkdirs() }

    /**
     * The `origWhen` of every alarm this app has set for [action], read from `dumpsys alarm`.
     *
     * The dump has more than one place that mentions an action (the pending alarms and the statistics at
     * the end), so counting matching lines would count one alarm twice. This walks the entries that start
     * with `Alarm{` and keeps those that belong to this package and carry the action, and reads the
     * `origWhen=` the entry prints in local time. Format as recorded in docs/HANDOVER.md section 10:
     * `origWhen=2026-08-02 02:51:00.000`.
     */
    fun alarmTimes(packageName: String, action: String): List<LocalDateTime> {
        val lines = shell("dumpsys alarm").lines()
        val blocks = mutableListOf<MutableList<String>>()
        for (line in lines) {
            if (line.contains("Alarm{")) blocks += mutableListOf(line) else blocks.lastOrNull()?.add(line)
        }
        val stamp = Regex("""origWhen=(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2}:\d{2})""")
        return blocks
            .filter { block -> block.any { it.contains(packageName) } && block.any { it.contains(action) } }
            .mapNotNull { block ->
                block.firstNotNullOfOrNull { stamp.find(it) }?.let { LocalDateTime.parse("${it.groupValues[1]}T${it.groupValues[2]}") }
            }
            .sorted()
    }
}
