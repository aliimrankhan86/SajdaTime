package com.sajdatime.app.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * One short vibration, straight from the Vibrator: the Qibla arrival (70 ms) and each bead of the
 * tasbeeh.
 *
 * Not `performHapticFeedback`: that is silent on any phone whose touch feedback setting is off, which is
 * the default on many Samsung and Xiaomi phones, so the message appeared and nothing was felt (Ali, 24 Oct
 * 2026: the vibration must happen). A vibration with no attributes is still filed as touch and muted the
 * same way (emulator, 24 Oct: "ignored_for_settings" with touch feedback off), so it is marked media,
 * which that switch does not govern; the phone's own vibration settings still apply, as they should.
 *
 * Moved here from `QiblaScreen` on 12 Nov 2026 when the tasbeeh became its second caller.
 */
fun buzz(context: Context, millis: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    if (vibrator == null || !vibrator.hasVibrator()) return
    when {
        Build.VERSION.SDK_INT >= 33 -> vibrator.vibrate(
            VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE),
            VibrationAttributes.createForUsage(VibrationAttributes.USAGE_MEDIA),
        )
        Build.VERSION.SDK_INT >= 26 -> @Suppress("DEPRECATION") vibrator.vibrate(
            VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE),
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build(),
        )
        else -> @Suppress("DEPRECATION") vibrator.vibrate(millis)
    }
}
