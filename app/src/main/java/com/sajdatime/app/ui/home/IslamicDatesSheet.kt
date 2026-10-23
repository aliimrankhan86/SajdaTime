package com.sajdatime.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.IslamicDates
import com.sajdatime.core.IslamicDay
import com.sajdatime.core.bidiIsolated
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The year's six key dates, soonest first, each with the date it falls on and how far away it is
 * (1.6.0, sign-off 23 Oct 2026). Opened from the Hijri date under the city on Times, and from
 * Settings > Islamic dates. The same for every sect: no fast tags, no ruling, no contested day.
 *
 * Built on [IslamicDates], which is built on the header's own calendar and the user's date shift, so
 * the sheet cannot name a different day from the header. "Shift the date" is the one button, because
 * a mosque that sighted the moon locally is the one reason any of these dates would be wrong.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicDatesSheet(today: LocalDate, offsetDays: Int, onShiftDate: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val dates = remember(today, offsetDays) { IslamicDates.upcoming(today, offsetDays) }
    val todayHijri = HijriCalendar.of(today, offsetDays)?.let { hijriText(it.day, it.month, it.year) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = Space.Section),
        ) {
            Text(
                text = stringResource(R.string.dates_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 8.dp).semantics { heading() },
            )
            if (todayHijri == null || dates.isEmpty()) {
                Spacer(Modifier.height(Space.Group))
                Text(stringResource(R.string.dates_unavailable), modifier = Modifier.padding(horizontal = 8.dp))
                return@Column
            }
            Spacer(Modifier.height(Space.Tight))
            Text(
                text = stringResource(R.string.dates_today_is, todayHijri),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(Space.Group))
            Column(Modifier.fillMaxWidth().sajdaSurface(RoundedCornerShape(24.dp))) {
                dates.forEachIndexed { i, u ->
                    if (i > 0) HorizontalDivider(color = scheme.outlineVariant)
                    val days = ChronoUnit.DAYS.between(today, u.date).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            // One stop for a screen reader: name, date and how far away.
                            .semantics(mergeDescendants = true) {}
                            .heightIn(min = 56.dp)
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(dayName(u.day, u.hijriYear), style = MaterialTheme.typography.titleMedium)
                            Text(
                                TimeFormat.date(context, u.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = when (days) {
                                0 -> stringResource(R.string.home_today)
                                1 -> stringResource(R.string.widget_tomorrow)
                                else -> pluralStringResource(R.plurals.dates_in_days, days, days)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (days == 0) scheme.primary else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.Group))
            Text(
                text = stringResource(R.string.dates_footer),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(Space.Group))
            CtaButton(onClick = onShiftDate, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_hijri_offset_label))
            }
        }
    }
}

/** 1 Muharram is named by its date, "1 Muharram 1449", not as a new year (see [IslamicDay]). */
@Composable
private fun dayName(day: IslamicDay, hijriYear: Int): String = when (day) {
    IslamicDay.MUHARRAM_1 -> hijriText(day.day, day.month, hijriYear)
    IslamicDay.ASHURA -> stringResource(R.string.date_ashura)
    IslamicDay.RAMADAN_1 -> stringResource(R.string.date_ramadan_1)
    IslamicDay.EID_AL_FITR -> stringResource(R.string.date_eid_fitr)
    IslamicDay.ARAFAH -> stringResource(R.string.date_arafah)
    IslamicDay.EID_AL_ADHA -> stringResource(R.string.date_eid_adha)
}

/**
 * "12 Jumada al-Ula 1448", with the month name isolated as the header does (BidiTest, "the Hijri
 * date reads in the right order").
 */
@Composable
internal fun hijriText(day: Int, month: Int, year: Int): String {
    val name = stringArrayResource(R.array.hijri_months).getOrNull(month - 1)?.bidiIsolated().orEmpty()
    return stringResource(R.string.hijri_date_format, day, name, year)
}
