package com.sajdatime.app

import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.Notifications
import com.sajdatime.core.Sect
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The built-in adhans (1.5.0) as the device sees them: each stored address opens the real file, the
 * alarm channel carries it with alarm usage, a chosen adhan follows the school, and an upgraded
 * user's phone sound from before 1.5.0 is still remembered behind it.
 *
 * Puts the settings and the alarm channel back the way it found them.
 */
@RunWith(AndroidJUnit4::class)
class BuiltInAdhanDeviceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = SettingsRepository(context)
    private var sectBefore = Sect.SUNNI
    private var soundBefore = ""

    @Before
    fun remember() = runBlocking {
        val s = repository.current()
        sectBefore = s.sect
        soundBefore = s.alarmSoundUri
    }

    @After
    fun restore() = runBlocking {
        repository.setSect(sectBefore)
        repository.setAlarmSound(soundBefore)
        Notifications.ensureAlarmChannel(context, soundBefore)
        Unit
    }

    @Test
    fun each_stored_address_opens_the_whole_file() {
        val sizes = mapOf(BuiltInAdhan.ADHAN to 1_229_032L, BuiltInAdhan.SHIA_ADHAN to 2_289_817L)
        sizes.forEach { (adhan, bytes) ->
            val uri = Uri.parse(adhan.uri(context.packageName))
            context.contentResolver.openAssetFileDescriptor(uri, "r").use { fd ->
                assertNotNull("$adhan does not open", fd)
                assertEquals("$adhan is not the file in docs/licences/ADHAN.md", bytes, fd!!.length)
            }
        }
    }

    @Test
    fun the_alarm_channel_plays_the_adhan_as_an_alarm() {
        val uri = BuiltInAdhan.ADHAN.uri(context.packageName)
        val id = Notifications.ensureAlarmChannel(context, uri)
        val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(id)
        assertEquals(Uri.parse(uri), channel.sound)
        assertEquals(AudioAttributes.USAGE_ALARM, channel.audioAttributes.usage)
    }

    @Test
    fun a_chosen_adhan_follows_the_school_and_the_phone_sound_stays_remembered() = runBlocking {
        val phone = "content://media/internal/audio/media/1"
        repository.setSect(Sect.SUNNI)
        // As 1.4.0 left it: the sound written, nothing else.
        repository.setAlarmSound(phone)
        repository.setBuiltInAdhan(BuiltInAdhan.ADHAN)
        repository.setSect(Sect.SHIA)
        assertEquals(BuiltInAdhan.SHIA_ADHAN, BuiltInAdhan.of(repository.current().alarmSoundUri))
        repository.setSect(Sect.SUNNI)
        assertEquals(BuiltInAdhan.ADHAN, BuiltInAdhan.of(repository.current().alarmSoundUri))
        // Choosing the adhan again must not overwrite the remembered phone sound with the adhan.
        repository.setBuiltInAdhan(BuiltInAdhan.ADHAN)
        assertEquals(phone, repository.current().phoneAlarmSoundUri)
    }
}
