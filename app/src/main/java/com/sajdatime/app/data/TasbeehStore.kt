package com.sajdatime.app.data

import android.content.Context
import androidx.core.content.edit
import com.sajdatime.core.Tasbeeh

/**
 * Where the tasbeeh's three numbers are kept, so a count survives closing the sheet, the app being
 * killed and a restart. On the phone only: cloud backup and device transfer are off for the whole app
 * (the manifest), nothing here is sent anywhere, and no usage count is recorded for it.
 *
 * ponytail: SharedPreferences, not a third DataStore. Three ints written on a tap need no flow, no
 * migration and no coroutine; `apply` writes in the background and the next read sees it at once. The
 * values are plain ints, so an iPhone version stores the same three numbers.
 */
class TasbeehStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("tasbeeh", Context.MODE_PRIVATE)

    fun load(): Tasbeeh = Tasbeeh.restore(
        count = prefs.getInt(COUNT, 0),
        target = prefs.getInt(TARGET, Tasbeeh.DEFAULT_TARGET),
        rounds = prefs.getInt(ROUNDS, 0),
    )

    fun save(tasbeeh: Tasbeeh) {
        prefs.edit {
            putInt(COUNT, tasbeeh.count)
            putInt(TARGET, tasbeeh.target)
            putInt(ROUNDS, tasbeeh.rounds)
        }
    }

    private companion object {
        const val COUNT = "count"
        const val TARGET = "target"
        const val ROUNDS = "rounds"
    }
}
