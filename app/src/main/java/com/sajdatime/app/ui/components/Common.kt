package com.sajdatime.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.LocationProblem
import com.sajdatime.app.ui.UiState
import java.time.Duration
import java.time.Instant

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

/**
 * Spoken countdown, e.g. "2h 14m".
 *
 * Resolved through [stringResource] rather than Context.getString so it re-reads if the
 * user changes their device language while the screen is open.
 */
@Composable
fun rememberRemainingText(from: Instant, to: Instant): String {
    val duration = Duration.between(from, to).let { if (it.isNegative) Duration.ZERO else it }
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    return when {
        hours > 0 -> stringResource(R.string.duration_h_m, hours, minutes)
        minutes > 0 -> stringResource(R.string.duration_m, minutes)
        else -> stringResource(R.string.duration_s, seconds)
    }
}

/**
 * Changing location, in one place, reachable from both the prayer times screen and
 * Settings. Offers the two things a traveller actually wants: re-detect where I am, or
 * let me type where I am going.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSheet(
    state: UiState,
    onDismiss: () -> Unit,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var query by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                // Without these the on-screen keyboard covers the city field and its
                // button, so the user types blind and cannot reach "Find city".
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.location_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.location_sheet_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onUseGps,
                enabled = !state.resolvingLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.action_use_my_location))
            }

            if (state.resolvingLocation) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.location_finding),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.location_sheet_search_heading),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.city_fallback_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            val notFound = state.problem == LocationProblem.CITY_NOT_FOUND
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.city_field_label)) },
                supportingText = {
                    Text(
                        stringResource(
                            if (notFound) R.string.city_not_found else R.string.city_field_helper,
                        ),
                    )
                },
                isError = notFound,
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { if (query.isNotBlank()) onSearchCity(query) },
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onSearchCity(query) },
                enabled = query.isNotBlank() && !state.resolvingLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.action_find_city))
            }
        }
    }
}
