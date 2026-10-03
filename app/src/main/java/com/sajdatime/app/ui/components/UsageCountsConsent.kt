package com.sajdatime.app.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sajdatime.app.R

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
    TextButton(
        onClick = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, policyUrl.toUri())) }
        },
    ) {
        Text(stringResource(R.string.consent_read_policy))
    }
    Spacer(Modifier.height(16.dp))
    // Equal weight on purpose: a tonal button each, same size, so neither answer is steered.
    ConsentButton(stringResource(R.string.consent_yes), onYes)
    Spacer(Modifier.height(12.dp))
    ConsentButton(stringResource(R.string.consent_no), onNo)
}

@Composable
private fun ConsentButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
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
fun UsageCountsConsentDialog(onYes: () -> Unit, onNo: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNo,
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
