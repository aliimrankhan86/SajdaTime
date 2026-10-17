package com.sajdatime.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.sajdatime.app.data.settingsCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * A settings file full of garbage must read as "first run", not throw. Without the handler
 * DataStore rethrows a CorruptionException from every read, which in a BroadcastReceiver
 * coroutine kills the process at the next prayer.
 */
class SettingsCorruptionTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val key = stringPreferencesKey("city")

    @Test
    fun `random bytes in the settings file read as empty preferences`() = runBlocking {
        val file = folder.newFile("sajdatime.preferences_pb")
        file.writeBytes(byteArrayOf(0x7f, 0x01, 0x02, -1, -2, -3, 0x55, 0x00, 0x13, 0x37))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = PreferenceDataStoreFactory.create(
                corruptionHandler = settingsCorruptionHandler(),
                scope = scope,
                produceFile = { file },
            )
            assertNull(store.data.first()[key])
            // And it recovers: the store is usable again after the replacement.
            store.edit { it[key] = "Slough" }
            assertEquals("Slough", store.data.first()[key])
        } finally {
            scope.cancel()
        }
    }
}
