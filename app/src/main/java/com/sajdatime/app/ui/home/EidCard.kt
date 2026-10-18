package com.sajdatime.app.ui.home

import com.sajdatime.app.ui.theme.Space
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.rounded.Brightness3
import com.sajdatime.app.ui.components.NoticeTone
import com.sajdatime.app.ui.components.Notice
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.Eid

/**
 * A greeting on the day of Eid, and nothing else: no timetable, no action, no notification.
 *
 * When it shows. [UiState.eid] is set only on the day the user's own Islamic date reads 1 Shawwal
 * or 10 Dhul Hijjah (a pure calendar rule, so 2027, 2028 and every later year need no entries and
 * the user's date shift moves it with their header), and it disappears at local midnight with the
 * date. It stays until the user closes it, then never returns for that Eid. It does not depend on
 * the Ramadan card switch: a person who does not fast still has an Eid.
 *
 * What it says. The greeting is unqualified so it suits everyone. The date is our calculation,
 * though, and a good part of the world celebrates on the day its own moon sighting is announced,
 * often a day later. The fine print under the greeting says so, apologises for a different day, and
 * defers to the user's mosque, which is the same line the rest of the Ramadan work ends on. It is
 * small, not hidden: it is the same text colour as the greeting, so it meets the same contrast.
 *
 * Colour. `secondaryContainer`, like the quiet Ramadan notice. Not `tertiaryContainer`, which in
 * this design system is the warning surface (docs/DESIGN_SYSTEM.md).
 */
@Composable
internal fun EidSection(state: UiState, onDismiss: (code: Int) -> Unit) {
    val eid = state.eid ?: return
    if (!eid.isUnseen(state.settings.eidGreetingDismissedCode)) return

    Spacer(Modifier.height(Space.Group))
    EidCard(eid = eid.eid, onDismiss = { onDismiss(eid.code) })
}

@Composable
private fun EidCard(eid: Eid, onDismiss: () -> Unit) {
    val title = when (eid) {
        Eid.FITR -> R.string.eid_fitr_title
        Eid.ADHA -> R.string.eid_adha_title
    }
    val body = when (eid) {
        Eid.FITR -> R.string.eid_fitr_body
        Eid.ADHA -> R.string.eid_adha_body
    }
    // A quiet Notice (17 Oct 2026). The fine print is a footnote, always visible, not folded:
    // "this date is our calculation, follow your mosque" must never be one tap away.
    Notice(
        tone = NoticeTone.QUIET,
        icon = Icons.Rounded.Brightness3,
        title = stringResource(title),
        brief = stringResource(body),
        footnote = stringResource(R.string.eid_fine_print),
        onDismiss = onDismiss,
    )
}
