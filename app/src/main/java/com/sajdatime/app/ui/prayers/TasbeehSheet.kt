package com.sajdatime.app.ui.prayers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.data.TasbeehStore
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.components.TasbeehIcon
import com.sajdatime.app.ui.components.buzz
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.core.Tasbeeh

/**
 * The door to the tasbeeh: one tall, full width button, the first thing on the My prayers tab.
 *
 * Third placement in one night, each at Ali's word (12 Nov 2026):
 * 1. A line of text with a compact button at the foot of the page. "The button could be bigger. It could
 *    go lost easily."
 * 2. Full width, under today's prayers. "Think harder ... it cannot be lost ... it needs to be properly
 *    placed so user cannot miss it."
 * 3. This: directly under the tab's title, above the checklist, 64dp tall, in every state of the screen.
 *
 * Why here. Under the prayers it sat below a card whose height changes (five rows, notices, a paused
 * line), so at large text or on a small phone it slid off the first screen, and with the record switched
 * off it sat somewhere else again. Under the title it is at one fixed place that nothing above can push:
 * whoever opens the tab sees it before anything else, without scrolling, at any text size. The caller
 * puts it outside the loading gate and the on or off switch, so it never disappears or moves.
 *
 * Not a fifth tab (Ali: "we can't add a tab for each feature so it's better inside my prayers"). Not in Settings: Settings is where things are changed, not
 * used, and a second door is a second thing to keep in step. It owns the sheet, so the screen only
 * places it.
 *
 * The icon is a tasbeeh ([TasbeehIcon]), not a tapping hand, and the space above the button equals the
 * space below it (Ali: "make the button better with even spacing and add an icon there of tasbeeh").
 */
@Composable
internal fun TasbeehDoor() {
    var open by rememberSaveable { mutableStateOf(false) }
    CtaButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("tasbeeh_door")) {
        Icon(TasbeehIcon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(Space.Related))
        Text(stringResource(R.string.tasbeeh_title))
    }
    if (open) TasbeehSheet(onDismiss = { open = false })
}

/**
 * The tasbeeh (Ali, 12 Nov 2026). Second design the same morning: "make the tasbeeh itself look better in
 * design (should look modern UX, clean and easy and comfortable to use)". The first was a number, two
 * lines of text, a 120dp slab of a button and two small buttons.
 *
 * Top to bottom, in the order the hand and eye use it:
 * - **The ring.** The count, large, inside a ring that fills as the round goes: where you are in the round
 *   is seen without reading. On a full round the ring closes and it and the number turn green. With no
 *   target the ring is a plain track.
 * - **The total**, one quiet line under the ring: every bead since the last start. The ring's number
 *   begins again each round, and a fast thumb is at 1 or 4 before the eye has seen 33; the total only
 *   climbs (Ali on his phone, 12 Nov 2026). Not shown with no target, where the ring's number is it.
 * - **The target**, as four choices side by side (33, 99, 100, and ∞ for no target). It was a button that
 *   stepped through them, so three of the four were out of sight and reaching one could take three taps.
 *   Choosing the one already in use does nothing; choosing another starts again ([Tasbeeh.withTarget]).
 * - **Count.** One wide button low on the sheet, under the thumb, found without looking. It counts the
 *   moment a finger lands, once per finger, and a finger on it cannot scroll or drag the sheet. Still a real
 *   button and not a "tap anywhere" surface: TalkBack and Switch Access need one, and it is the app's own
 *   call to action (DESIGN_SYSTEM 4.1).
 * - **Start again**, small and last, away from the thumb's path so it is not hit by mistake.
 *
 * What did not change: each bead is felt (a 15 ms tick) and a full round is felt differently (150 ms),
 * through [buzz], so it works with the phone's touch feedback setting off; the count is saved on every
 * tap ([TasbeehStore]); the screen stays awake while the sheet is open; and there are no words of dhikr
 * and no advice on how many ([Tasbeeh]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbeehSheet(onDismiss: () -> Unit) {
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
        TasbeehBody(onClose = onDismiss)
    }
}

/**
 * Everything inside the sheet. Apart from the sheet so a test can draw it on a plain page and take its
 * picture: a sheet is a window of its own, which a test cannot capture.
 */
@Composable
internal fun TasbeehBody(onClose: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TasbeehStore(context) }
    var tasbeeh by remember { mutableStateOf(store.load()) }
    fun keep(next: Tasbeeh) {
        tasbeeh = next
        store.save(next)
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
            DialogTitle(text = stringResource(R.string.tasbeeh_title), onClose = onClose)
        }
        Spacer(Modifier.height(Space.Group))
        CountRing(tasbeeh)
        if (tasbeeh.target > 0) {
            // Every bead since the last start. The number in the ring begins again each round, and a
            // fast thumb is past 33 before the eye catches it: this one only ever climbs (Ali, 12 Nov
            // 2026). With no target the ring's number is already the total, so there is no second line.
            Spacer(Modifier.height(Space.Related))
            Text(
                text = stringResource(R.string.tasbeeh_total, tasbeeh.total),
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("tasbeeh_total"),
            )
        }
        Spacer(Modifier.height(Space.Section))
        TargetChoice(current = tasbeeh.target, onChoose = { keep(tasbeeh.withTarget(it)) })
        Spacer(Modifier.height(Space.Group))
        // What TalkBack says after "Count, button": where the count stands.
        val spoken = if (tasbeeh.target > 0) {
            stringResource(R.string.tasbeeh_state, tasbeeh.count, tasbeeh.target)
        } else {
            tasbeeh.count.toString()
        }
        fun bead() {
            val next = tasbeeh.tap()
            keep(next)
            buzz(context, if (next.reached) 150 else 15)
        }
        // A bead is counted the moment a finger lands, once for every finger. It used to count when the
        // finger lifted (a button's click), and on Ali's Redmi fast taps went missing (12 Nov 2026): a
        // finger that slid a little lost its click to the page's scroll, and with two thumbs the second
        // landed before the first lifted.
        //
        // So this button takes its touches whole. Every change is consumed: the button's own click
        // never sees a finger (it would count the same touch again on the lift), and a finger that
        // starts here cannot scroll the page or drag the sheet. That last part matters as much: a sheet
        // nudged by a sliding thumb springs back for a fifth of a second and swallows the next touch as
        // its own. "IgnoreConsumed" for the same reason: the page and the sheet see a touch before the
        // button does and may mark it. Scroll or close the sheet from anywhere else on it.
        // The button's click (onClick) is left for what has no finger: TalkBack, Switch Access and a
        // keyboard. The press is shown by hand, because the button no longer sees it.
        // bead() may only read state (it is captured once). Pinned by TasbeehDeviceTest (200 fast taps).
        val presses = remember { MutableInteractionSource() }
        CtaButton(
            onClick = { bead() },
            interactionSource = presses,
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .testTag("tasbeeh_tap")
                .semantics { stateDescription = spoken }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        val held = mutableMapOf<PointerId, PressInteraction.Press>()
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { change ->
                                if (change.changedToDownIgnoreConsumed()) {
                                    bead()
                                    PressInteraction.Press(change.position).also { held[change.id] = it; presses.tryEmit(it) }
                                } else if (change.changedToUpIgnoreConsumed()) {
                                    held.remove(change.id)?.let { presses.tryEmit(PressInteraction.Release(it)) }
                                }
                                change.consume()
                            }
                        }
                    }
                },
        ) {
            Text(stringResource(R.string.tasbeeh_count), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(Space.Group))
        CtaButton(onClick = { keep(tasbeeh.reset()) }, compact = true, modifier = Modifier.testTag("tasbeeh_reset")) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.Related))
            Text(stringResource(R.string.tasbeeh_reset))
        }
    }
}

/**
 * The count inside its ring. The ring's sweep is the share of the round done and moves smoothly from
 * bead to bead; it is decoration (the number and the spoken state carry the same fact), so it has no
 * semantics of its own. The ring grows a little with large text so the number stays inside it.
 */
@Composable
private fun CountRing(tasbeeh: Tasbeeh) {
    val scheme = MaterialTheme.colorScheme
    val share = if (tasbeeh.target > 0) tasbeeh.count / tasbeeh.target.toFloat() else 0f
    val sweep by animateFloatAsState(share, Motion.standard(), label = "round done")
    val arc by animateColorAsState(scheme.primary, Motion.standard(), label = "ring")
    val track = scheme.outlineVariant
    val scale = LocalDensity.current.fontScale.coerceIn(1f, 1.4f)
    Box(Modifier.size(208.dp * scale), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = 10.dp.toPx()
            val inset = Offset(stroke / 2, stroke / 2)
            val box = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = inset, size = box, style = Stroke(stroke))
            if (sweep > 0f) {
                // From the top, clockwise, in every language: a ring filling is not text and does not mirror.
                drawArc(arc, -90f, 360f * sweep, useCenter = false, topLeft = inset, size = box, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tasbeeh.count.toString(),
                // Tabular figures, so the number does not shift sideways as it counts. A count past 999
                // (no target) takes the smaller display size to stay inside the ring.
                style = (if (tasbeeh.count > 999) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge)
                    .copy(fontFeatureSettings = "tnum"),
                color = if (tasbeeh.reached) scheme.primary else scheme.onSurface,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.testTag("tasbeeh_count"),
            )
            if (tasbeeh.target > 0) {
                Text(
                    text = stringResource(R.string.tasbeeh_rounds, tasbeeh.rounds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.testTag("tasbeeh_rounds"),
                )
            }
        }
    }
}

/**
 * The four targets as one row of choices: a soft track and a lifted pill on the one in use, the look of
 * the theme control in Settings. Each is a radio button to TalkBack and says "Target: 33" or "No target";
 * what is drawn is only the number, or ∞, which needs no translation.
 */
@Composable
private fun TargetChoice(current: Int, onChoose: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(4.dp)
            .selectableGroup(),
    ) {
        for (target in Tasbeeh.TARGETS) {
            val selected = target == current
            val spoken = if (target > 0) stringResource(R.string.tasbeeh_target, target) else stringResource(R.string.tasbeeh_no_target)
            val fill by animateColorAsState(if (selected) scheme.surface else Color.Transparent, Motion.standard(), label = "choice")
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(fill)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onChoose(target) })
                    .testTag("tasbeeh_target_$target")
                    .semantics { contentDescription = spoken },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (target > 0) target.toString() else "\u221E",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                    // The box says "Target: 33"; the bare number is not read a second time.
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
    }
}
