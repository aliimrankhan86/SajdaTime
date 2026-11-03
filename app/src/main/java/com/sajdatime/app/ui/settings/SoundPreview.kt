package com.sajdatime.app.ui.settings

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sajdatime.app.R
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.theme.Space

/**
 * "Hear it" under the sound choices (1.7.0, design: DESIGN-A-B-C-F, C). Plays the selected sound through the
 * same audio usage the real alert uses (alarm or notification), so the phone's volume and silent switch
 * behave as they will for the alert. It never changes a volume, never plays by itself, and stops on Stop,
 * when the sound ends, when another sound is chosen, when the dialog closes and when the app goes to the
 * background. It touches no alarm scheduling. iPhone: carries over (AVAudioPlayer).
 */
@Composable
internal fun SoundPreview(uri: String, alarm: Boolean) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    // Re-read on each tap and when the dialog opens, so the line is true when the user presses.
    var tick by remember { mutableStateOf(0) }

    fun stop() {
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
        playing = false
    }

    // A different sound chosen, or the dialog closed: stop.
    DisposableEffect(uri) { onDispose { stop() } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) stop() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Spacer(Modifier.height(Space.Group))
    CtaButton(
        onClick = {
            tick++
            if (playing) {
                stop()
            } else {
                player = startPreview(context, uri, alarm) { stop() }
                playing = player != null
            }
        },
        compact = true,
    ) {
        Icon(
            imageVector = if (playing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(Space.Related))
        Text(stringResource(if (playing) R.string.action_stop else R.string.action_hear_it))
    }
    Spacer(Modifier.height(Space.Tight))
    // The line says what will really happen: the same rules the alert follows (alarm volume for an alarm,
    // the ringer for a notification). `tick` only forces this to be read again after a press.
    @Suppress("UNUSED_EXPRESSION") tick
    Text(
        text = stringResource(previewHelp(context, alarm)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun previewHelp(context: Context, alarm: Boolean): Int {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return if (alarm) {
        if (audio.getStreamVolume(AudioManager.STREAM_ALARM) == 0) R.string.preview_alarm_muted else R.string.preview_alarm_help
    } else {
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) R.string.preview_notification_silent else R.string.preview_notification_help
    }
}

/** Starts the sound, or returns null when it cannot play (a phone sound that is gone). Never throws. */
private fun startPreview(context: Context, uri: String, alarm: Boolean, onDone: () -> Unit): MediaPlayer? {
    val source: Uri = uri.takeIf { it.isNotBlank() }?.let(Uri::parse)
        ?: RingtoneManager.getDefaultUri(if (alarm) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION)
    return runCatching {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(if (alarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setDataSource(context, source)
            setOnCompletionListener { onDone() }
            setOnErrorListener { _, _, _ -> onDone(); true }
            prepare()
            start()
        }
    }.getOrNull()
}
