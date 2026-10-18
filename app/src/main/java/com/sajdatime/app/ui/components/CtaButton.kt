package com.sajdatime.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 * Two answers of equal weight (the usage counts question) are both a CtaButton: equal
 * prominence is the point of that screen, and the same strong style keeps it equal.
 */
@Composable
fun CtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ctaButtonColors(),
        modifier = modifier.heightIn(min = 52.dp),
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)) {
            content()
        }
    }
}

/** The call to action colours, for the few places that need a Material Button directly. */
@Composable
fun ctaButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = if (LocalDarkTheme.current) DarkCta else LightCta,
    contentColor = OnCta,
)
