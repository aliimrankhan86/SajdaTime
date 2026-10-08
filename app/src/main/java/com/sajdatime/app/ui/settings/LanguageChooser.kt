package com.sajdatime.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R

/**
 * The language list, used by Settings and by the globe button on the Welcome screen so there
 * is one list and one behaviour.
 *
 * Best practice followed here, deliberately: "Phone language" is first and is what is ticked
 * until someone chooses; each language is written in its own script (so it can be found by
 * someone who cannot read the language the app is in now); no flags (a flag is a country,
 * not a language, and Urdu has no sensible one); and a language the build has no words for
 * is shown disabled instead of hidden, never selectable.
 */
@Composable
fun LanguageChooserDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val chosen = AppLanguage.chosen(context)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { Text(stringResource(R.string.settings_language_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                LanguageRow(
                    label = stringResource(R.string.language_phone),
                    selected = chosen == null,
                    available = true,
                    onClick = {
                        AppLanguage.apply(context, null)
                        onDismiss()
                    },
                )
                AppLanguage.entries.forEach { language ->
                    LanguageRow(
                        label = language.nativeName,
                        selected = language == chosen,
                        available = language.isAvailable(context),
                        onClick = {
                            AppLanguage.apply(context, language)
                            onDismiss()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun LanguageRow(label: String, selected: Boolean, available: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, enabled = available, role = Role.RadioButton, onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = null, enabled = available)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!available) {
                Text(
                    stringResource(R.string.language_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
