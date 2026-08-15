package com.sajdatime.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Sect
import com.sajdatime.core.descriptionRes
import com.sajdatime.core.labelRes

/**
 * The list of calculation methods, with a plain line under each — the one list, offered
 * in two places: the Settings picker and the first-run step.
 *
 * One composable rather than two lists because they must not drift. Whatever the user was
 * shown at setup is exactly what they find again in Settings, and a description edited in
 * one place cannot be forgotten in the other. Same reasoning as `PlaceName` and
 * `AdjustmentCodec`: one parser, two callers.
 *
 * Jafari and Tehran are Shia conventions; the rest are Sunni. Showing all of them to
 * everyone invites a wrong pick, so the list follows the chosen school. [CalcMethod.AUTO]
 * is always first — it is the answer for anyone who does not know, and its own line says so.
 */
@Composable
fun MethodChoiceList(
    sect: Sect,
    current: CalcMethod,
    onSelect: (CalcMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = CalcMethod.entries.filter { it.offeredTo(sect) }
    Column(modifier.selectableGroup()) {
        available.forEach { method ->
            RadioRow(
                label = stringResource(method.labelRes),
                supporting = stringResource(method.descriptionRes),
                selected = current == method,
                onSelect = { onSelect(method) },
            )
        }
    }
}

/**
 * One radio option with a label and, optionally, a line underneath it. The whole row is the
 * touch target and carries the radio role, so a screen reader announces one thing rather
 * than a button beside a sentence.
 */
@Composable
fun RadioRow(
    label: String,
    supporting: String? = null,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .heightIn(min = 56.dp)
            .padding(vertical = 8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
