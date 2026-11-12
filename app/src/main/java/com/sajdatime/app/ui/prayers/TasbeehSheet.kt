package com.sajdatime.app.ui.prayers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.data.TasbeehStore
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.components.buzz
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.core.Tasbeeh

/**
 * The door to the tasbeeh on My prayers: one line and one compact button, the Fasts row's own shape.
 * It is there whether or not the prayer record is switched on, because counting dhikr does not depend
 * on it. It owns the sheet, so the screen only places it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TasbeehDoor() {
    var open by rememberSaveable { mutableStateOf(false) }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(Space.Related),
    ) {
        Text(
            text = stringResource(R.string.tasbeeh_line),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        CtaButton(onClick = { open = true }, compact = true) {
            Text(stringResource(R.string.tasbeeh_title))
        }
    }
    if (open) TasbeehSheet(onDismiss = { open = false })
}

/**
 * The tasbeeh (Ali, 12 Nov 2026): the count, large; what it is counting to; one big button to count;
 * and two small ones, to change the target and to start again. The rule is [Tasbeeh] in `:core`.
 *
 * - **One big button, not a tap anywhere surface.** It is found without looking, it is a button to
 *   TalkBack and Switch Access, and it is the app's own call to action (DESIGN_SYSTEM 4.1).
 * - **Each bead is felt** (a 15 ms tick) and **a full round is felt differently** (150 ms), through
 *   [buzz], so it works with the phone's touch feedback setting off, as the Qibla arrival does.
 * - **The count is saved on every tap** ([TasbeehStore]), so closing the sheet, a call or the phone
 *   restarting loses nothing.
 * - **The screen stays awake while the sheet is open**: a tasbeeh is held and tapped slowly, and a
 *   screen that dims between beads is a counter that stops.
 * - **No words of dhikr and no advice on how many**: see [Tasbeeh].
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TasbeehSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TasbeehStore(context) }
    var tasbeeh by remember { mutableStateOf(store.load()) }
    fun keep(next: Tasbeeh) {
        tasbeeh = next
        store.save(next)
    }
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        // The sheet is its own window, so the flag is set on the sheet's view, not the activity's.
        val view = LocalView.current
        DisposableEffect(view) {
            val before = view.keepScreenOn
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = before }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = Space.Section),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                DialogTitle(text = stringResource(R.string.tasbeeh_title), onClose = onDismiss)
            }
            Spacer(Modifier.height(Space.Group))
            Text(
                text = tasbeeh.count.toString(),
                // Tabular figures, so the number does not shift sideways as it counts.
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                // A full round turns the number green; the longer buzz and the rounds line say it too.
                color = if (tasbeeh.reached) scheme.primary else scheme.onSurface,
                modifier = Modifier.testTag("tasbeeh_count"),
            )
            Spacer(Modifier.height(Space.Tight))
            Text(
                text = if (tasbeeh.target > 0) {
                    stringResource(R.string.tasbeeh_target, tasbeeh.target)
                } else {
                    stringResource(R.string.tasbeeh_no_target)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
            if (tasbeeh.target > 0) {
                Spacer(Modifier.height(Space.Tight))
                Text(
                    text = stringResource(R.string.tasbeeh_rounds, tasbeeh.rounds + if (tasbeeh.reached) 1 else 0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.testTag("tasbeeh_rounds"),
                )
            }
            Spacer(Modifier.height(Space.Section))
            // What TalkBack says after "Count, button": where the count stands.
            val spoken = if (tasbeeh.target > 0) {
                stringResource(R.string.tasbeeh_state, tasbeeh.count, tasbeeh.target)
            } else {
                tasbeeh.count.toString()
            }
            CtaButton(
                onClick = {
                    val next = tasbeeh.tap()
                    keep(next)
                    buzz(context, if (next.reached) 150 else 15)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("tasbeeh_tap")
                    .semantics { stateDescription = spoken },
            ) {
                Text(stringResource(R.string.tasbeeh_count))
            }
            Spacer(Modifier.height(Space.Group))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(Space.Related),
            ) {
                CtaButton(onClick = { keep(tasbeeh.nextTarget()) }, compact = true, modifier = Modifier.testTag("tasbeeh_target")) {
                    Text(stringResource(R.string.tasbeeh_change_target))
                }
                CtaButton(onClick = { keep(tasbeeh.reset()) }, compact = true, modifier = Modifier.testTag("tasbeeh_reset")) {
                    Text(stringResource(R.string.tasbeeh_reset))
                }
            }
        }
    }
}
