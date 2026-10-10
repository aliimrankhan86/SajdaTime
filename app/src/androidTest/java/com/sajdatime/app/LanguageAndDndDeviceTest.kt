package com.sajdatime.app

import android.Manifest
import android.app.Instrumentation
import android.app.LocaleManager
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.LocaleList
import android.os.SystemClock
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.ui.settings.AppLanguage
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The two faults the owner found in 1.3.1 on his Samsung (10 Oct 2026), checked on a real
 * Android system rather than by reading the code. Run on an emulator or a plugged-in phone:
 *
 *     ./gradlew :app:connectedDebugAndroidTest                                (emulator)
 *     ./gradlew -PtestBuildType=sideload :app:connectedSideloadAndroidTest    (his phone, beside
 *                                                                             the Play copy)
 *
 * What this cannot prove on its own: that Google Play delivers every language. A build
 * installed by Gradle always carries every language, so a passing language test here says the
 * picker works when the words are present. Whether Play installs them is the bundle setting
 * `bundle.language.enableSplit = false`, guarded by LanguagePickerTest and measured with
 * bundletool (docs/HANDOVER.md, the 1.3.2 block). To run this suite against exactly what Play
 * would install, see the bundletool steps in that block.
 *
 * Every test puts the phone back the way it found it: the app's language and its Do Not
 * Disturb access are restored in [restore].
 */
@RunWith(AndroidJUnit4::class)
class LanguageAndDndDeviceTest {

    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)

    private var languageBefore: LocaleList? = null
    private var dndAccessBefore = false

    @Before
    fun remember() {
        if (AppLanguage.supported) {
            languageBefore = context.getSystemService(LocaleManager::class.java).applicationLocales
        }
        dndAccessBefore = Notifications.hasDndAccess(context)
    }

    @After
    fun restore() {
        languageBefore?.let { context.getSystemService(LocaleManager::class.java).applicationLocales = it }
        shell(if (dndAccessBefore) "cmd notification allow_dnd ${context.packageName}" else "cmd notification disallow_dnd ${context.packageName}")
    }

    // ---- Languages ------------------------------------------------------------------------

    @Test
    fun every_language_the_picker_offers_is_installed_and_selectable() {
        for (language in AppLanguage.entries) {
            assertTrue("${language.tag} is greyed out in the picker", language.isAvailable(context))
        }
    }

    @Test
    fun choosing_each_language_switches_the_app_and_the_picker_ticks_it() {
        assumeTrue("Per-app language needs Android 13", AppLanguage.supported)
        for (language in AppLanguage.entries) {
            AppLanguage.apply(context, language)
            // The system stores the choice asynchronously on some builds.
            val deadline = SystemClock.uptimeMillis() + 5_000
            while (AppLanguage.current(context) != language && SystemClock.uptimeMillis() < deadline) {
                SystemClock.sleep(100)
            }
            // Indonesian is the one that used to fail: Android reports it as "in".
            assertEquals("Settings would tick the wrong language", language, AppLanguage.current(context))

            // And the words the app then shows really are in that language.
            val chosen = context.getSystemService(LocaleManager::class.java).applicationLocales
            val config = android.content.res.Configuration(context.resources.configuration).apply { setLocales(chosen) }
            val shown = context.createConfigurationContext(config)
                .getString(com.sajdatime.core.R.string.app_language_tag)
            assertEquals(
                "app words are not in ${language.tag}",
                AppLanguage.normalise(Locale.forLanguageTag(language.tag).language),
                AppLanguage.normalise(Locale.forLanguageTag(shown).language),
            )
        }
    }

    // ---- Do Not Disturb ---------------------------------------------------------------------

    @Test
    fun the_app_is_on_the_do_not_disturb_access_list() {
        // The exact query Android's Settings uses to build that list
        // (ZenAccessController.getPackagesRequestingNotificationPolicyAccess).
        val listed = context.packageManager
            .getPackagesHoldingPermissions(arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY), 0)
            .map { it.packageName }
        assertTrue("${context.packageName} is missing from the Do Not Disturb access list", context.packageName in listed)
    }

    @Test
    fun the_button_opens_the_do_not_disturb_access_list() {
        assertNotNull(
            "This phone has no Do Not Disturb access screen",
            context.packageManager.resolveActivity(
                android.content.Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                PackageManager.MATCH_DEFAULT_ONLY,
            ),
        )
        // A blocking monitor records the launch without actually leaving the test.
        val monitor = instrumentation.addMonitor(IntentFilter(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), null, true)
        try {
            Notifications.openDndAccessSettings(context)
            assertEquals("The button did not open the Do Not Disturb access list", 1, monitor.hits)
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    @Test
    fun once_allowed_the_prayer_alarm_really_sounds_through_do_not_disturb() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        // Without access: the 1.3.1 channel, and Android refuses to let it bypass.
        shell("cmd notification disallow_dnd ${context.packageName}")
        assertFalse(Notifications.hasDndAccess(context))
        val before = Notifications.ensureAlarmChannel(context, soundUri = "")
        assertFalse(before.endsWith("_dnd"))

        // The user switches SajdaTime on in the list. Same effect as the switch in Settings.
        shell("cmd notification allow_dnd ${context.packageName}")
        assertTrue("Do Not Disturb access was not granted", Notifications.hasDndAccess(context))
        val after = Notifications.ensureAlarmChannel(context, soundUri = "")
        assertTrue(after.endsWith("_dnd"))
        val channel = notifications.getNotificationChannel(after)
        assertNotNull(channel)
        assertTrue("The alarm channel does not bypass Do Not Disturb", channel!!.canBypassDnd())
        assertEquals("The old alarm channel was left behind", null, notifications.getNotificationChannel(before))

        // Taking access away moves the alarm back to an ordinary channel.
        shell("cmd notification disallow_dnd ${context.packageName}")
        val revoked = Notifications.ensureAlarmChannel(context, soundUri = "")
        assertFalse(revoked.endsWith("_dnd"))
        assertEquals(null, notifications.getNotificationChannel(after))
    }

    private fun shell(command: String) {
        // Read the output to the end: the command has not finished until the pipe closes.
        instrumentation.uiAutomation.executeShellCommand(command).use { fd ->
            java.io.FileInputStream(fd.fileDescriptor).use { it.readBytes() }
        }
    }
}
