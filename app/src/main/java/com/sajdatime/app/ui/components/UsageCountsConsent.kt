package com.sajdatime.app.ui.components

import android.content.Intent
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Space

/**
 * The one consent text for optional usage counts, used by both the setup screen and the
 * Settings switch so a user who finds it later in Settings reads exactly what a new user
 * reads, not a shorter hint. Two buttons of equal weight and nothing pre-selected.
 *
 * Deliberately kept apart from the disclaimer and its dua request: this screen never appears
 * inside or after that dialog. See docs/ANALYTICS_PLAN.md section 4.5.
 */
@Composable
fun UsageCountsConsentBody(onYes: () -> Unit, onNo: () -> Unit) {
    val context = LocalContext.current
    val policyUrl = stringResource(R.string.privacy_policy_url)
    Text(
        text = stringResource(R.string.consent_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Space.Group))
    // The two ways to find out more are bold links, not buttons: the owner chose this on 18 Oct
    // 2026 (option B, from screenshots beside solid buttons under headings), so finding out more
    // never looks like an answer. The one exception to "every action is a solid button"; see
    // InfoLink and DESIGN_SYSTEM 4.1 kind F.
    // The two full lists sit behind one tap so the question itself stays short. Saved across
    // rotation so a user reading the list is not bounced back to the top.
    var showSent by rememberSaveable { mutableStateOf(false) }
    val sentState = stringResource(if (showSent) R.string.state_expanded else R.string.state_collapsed)
    InfoLink(
        text = stringResource(if (showSent) R.string.consent_hide_sent else R.string.consent_see_sent),
        onClick = { showSent = !showSent },
        modifier = Modifier.semantics { stateDescription = sentState },
    )
    if (showSent) {
        Spacer(Modifier.height(Space.Tight))
        val listStyle = MaterialTheme.typography.bodyMedium
        val listColor = MaterialTheme.colorScheme.onSurfaceVariant
        Text(stringResource(R.string.consent_sent_heading), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(stringResource(R.string.consent_sent_list), style = listStyle, color = listColor)
        Spacer(Modifier.height(Space.Paragraph))
        Text(stringResource(R.string.consent_never_heading), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(stringResource(R.string.consent_never_list), style = listStyle, color = listColor)
        Spacer(Modifier.height(Space.Tight))
    }
    InfoLink(
        text = stringResource(R.string.consent_read_policy),
        onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, policyUrl.toUri())) } },
    )
    Spacer(Modifier.height(Space.Section))
    // Equal weight on purpose: the same solid CtaButton each, same size, so neither answer is
    // steered (and neither is easy to miss).
    ConsentButton(stringResource(R.string.consent_yes), onYes)
    Spacer(Modifier.height(Space.Related))
    ConsentButton(stringResource(R.string.consent_no), onNo)
}

/**
 * A bold, underlined green link that opens more information. Allowed only on the usage counts
 * question (owner's choice, 18 Oct 2026); DesignRulesTest fails if it is used anywhere else.
 * 48dp tall so it is as easy to hit as a button, announced as a button, and drawn in the
 * button's own 16sp SemiBold so it never reads as body text. `primary` on the page is asserted
 * in ColorContrastTest in both themes.
 */
@Composable
private fun InfoLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = text,
            style = actionLabelStyle().copy(textDecoration = TextDecoration.Underline),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ConsentButton(text: String, onClick: () -> Unit) {
    CtaButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Text(text)
    }
}

/** The same consent as a dialog, opened when the Settings switch is turned on. */
@Composable
fun UsageCountsConsentDialog(
    onYes: () -> Unit,
    onNo: () -> Unit,
    // Tapping outside or pressing back counts as No in Settings. The one-time question for
    // existing users passes a no-op so an accidental dismissal is never recorded as an answer.
    onDismiss: () -> Unit = onNo,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(stringResource(R.string.consent_title)) },
        // Scrollable for the same reason as the disclaimer: at a large system font the
        // buttons would otherwise fall off the bottom.
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                UsageCountsConsentBody(onYes = onYes, onNo = onNo)
            }
        },
    )
}
