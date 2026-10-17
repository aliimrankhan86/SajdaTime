package com.sajdatime.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.sajdatime.app.ui.settings.MinimalLook
import com.sajdatime.app.ui.settings.PremiumLook
import com.sajdatime.app.ui.settings.RefinedLook
import com.sajdatime.app.ui.settings.settingsPreviewRenderer

/**
 * Debug builds only. Picks which Settings design preview the app draws, then opens the app.
 * `--es look A|B|C` for the three candidates; anything else (or nothing) is the shipping look.
 * The choice lives for the life of the process: force-stop the app to go back to normal.
 */
class SettingsLookActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreviewRenderer = when (intent.getStringExtra("look")) {
            "A" -> MinimalLook
            "B" -> RefinedLook
            "C" -> PremiumLook
            else -> null
        }
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        finish()
    }
}
