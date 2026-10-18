package com.sajdatime.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.ui.theme.DarkCta
import com.sajdatime.app.ui.theme.LightCta
import com.sajdatime.app.ui.theme.LocalDarkTheme
import com.sajdatime.app.ui.theme.OnCta

/**
 * Every call to action in the app: a solid green button with white, bold, 16sp text, at least
 * 52dp tall, in both themes (owner, 18 Oct 2026, after asking many times for buttons that are
 * hard to miss: "Make the text white").
 *
 * Why not Material's own buttons: `FilledTonalButton` is Material's *low emphasis* button, and
 * in dark it was green text on a dark green fill that read as a panel, not a button. The default
 * filled `Button` in dark is light mint with near black text. Neither is what he asked for. So
 * one component, one look, everywhere: light `#0B5A43` (8.2:1 with white), dark `#17805A` (4.9:1
 * with white, and 3.2:1 or more against every dark surface so the shape itself stands out).
 * Both are in ColorContrastTest. DesignRulesTest fails the build on a FilledTonalButton or an
 * OutlinedButton anywhere in the app.
 *
 * That includes the secondary actions that used to be green text links ("Timetable", "Save as
 * PDF", "Details", "See what is sent"): on 18 Oct 2026 the owner chose solid buttons for every
 * action. The only text links left are "Close" and "Got it" at the foot of a dialog, which he
 * said are fine, and DesignRulesTest holds that line.
 *
 * Two answers of equal weight (the usage counts question) are both a CtaButton: equal
 * prominence is the point of that screen, and the same strong style keeps it equal.
 *
 * [compact] is for a button that shares a line with a heading or a sentence ("Save as PDF" beside
 * "Today"): 40dp drawn, 16dp side padding. At 52dp beside a heading it looked squashed against the
 * list under it (owner, 18 Oct 2026: "make a smaller button ... still look actionable"). Material
 * still gives it a 48dp touch target, so it is no harder to hit. Same colours, same 16sp bold label.
 */
@Composable
fun CtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ctaButtonColors(),
        contentPadding = if (compact) CompactPadding else ButtonDefaults.ContentPadding,
        modifier = modifier.heightIn(min = if (compact) 40.dp else 52.dp),
    ) {
        ProvideTextStyle(actionLabelStyle()) {
            content()
        }
    }
}

private val CompactPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

/**
 * The one label style for every solid button in the app, green or amber: labelLarge (SemiBold)
 * at 16sp. Notice and permission buttons used Material's 14sp default until 18 Oct 2026, so
 * three sizes of button text sat side by side. docs/DESIGN_SYSTEM.md 4.1 has the full spec.
 */
@Composable
fun actionLabelStyle(): TextStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)

/** The call to action colours, for the few places that need a Material Button directly. */
@Composable
fun ctaButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = if (LocalDarkTheme.current) DarkCta else LightCta,
    contentColor = OnCta,
)

/** The same green and white for icon buttons that act, such as the minute steppers in Settings. */
@Composable
fun ctaIconButtonColors(): IconButtonColors = IconButtonDefaults.filledIconButtonColors(
    containerColor = if (LocalDarkTheme.current) DarkCta else LightCta,
    contentColor = OnCta,
)
