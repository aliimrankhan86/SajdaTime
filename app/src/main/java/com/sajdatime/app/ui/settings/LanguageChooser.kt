package com.sajdatime.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Space

/**
 * The language list, one list and one behaviour wherever it is opened from.
 *
 * "Phone language" is first and is what is ticked until someone chooses (the 8 Oct 2026
 * branch, brought into 1.5.0 on 19 Oct 2026); each language is written in its own script, so
 * it can be found by someone who cannot read the language the app is in now, and again in English on a
 * second line (owner, 25 Oct 2026), so a script the reader cannot read is still named; no flags (a flag
 * is a country, not a language); and a language the build has no words for is shown disabled
 * instead of hidden, never selectable.
 */
@Composable
fun LanguageChooserDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val chosen = AppLanguage.chosen(context)
    ChooserDialog(title = stringResource(R.string.settings_language_title), onDismiss = onDismiss) {
        Column(Modifier.selectableGroup()) {
            LanguageRow(
                label = stringResource(R.string.language_phone),
                // What following the phone gives. Not isolated: a Text of its own takes its direction from
                // its first letter, so "اردو (Urdu)" reads right in an English app too, where an FSI
                // isolate would set it back to front.
                second = AppLanguage.phone(context).namePair(context),
                selected = chosen == null,
                available = true,
                onClick = {
                    onDismiss()
                    AppLanguage.apply(context, null)
                },
            )
            AppLanguage.entries.forEach { language ->
                LanguageRow(
                    label = language.nativeName,
                    second = language.englishName.takeIf { it != language.nativeName },
                    selected = language == chosen,
                    available = language.isAvailable(context),
                    onClick = {
                        onDismiss()
                        AppLanguage.apply(context, language)
                    },
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(label: String, second: String?, selected: Boolean, available: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, enabled = available, role = Role.RadioButton, onClick = onClick)
            // Inside the selectable, so the ripple and target cover it; without it two line rows touch at 2x text.
            .padding(vertical = Space.Related),
    ) {
        RadioButton(selected = selected, onClick = null, enabled = available)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (second != null) {
                Text(
                    second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.Tight),
                )
            }
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
