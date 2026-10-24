# Step 4: Timetable, design (Fable `ux-lead`, 24 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 4 only.

Written before any code, as Ali's process asks: journey and design first, then `plan-verifier`, then pictures for
Ali's yes, then the build. Skills: none invoked; the design system (§2, §4, §4.1), the two shipped sheets (Islamic
dates, Ramadan timetable) and the Last 30 days sheet decide nearly everything. Apple HIG checks applied by hand: a
sheet with a grabber and an explicit close, plain list cells, a calendar glyph, no FAB.
**Status: Ali answered §13 Q1, Q2 and Q4 on 24 Oct 2026, all as recommended (Ramadan view from day 1 only; this
month and next; no Hijri date on cards). Q8 (table or cards) waits for pictures 9 and 10. Ready to build.**

Decided before this file and not reopened: Q7 (PDF ranges Today, 7 days, This month stay), Q8 (Ramadan cards or
table, Ali decides from side by side pictures), Q9 (the Ramadan card keeps its own door), DETAILED-PLAN 8.5 and 9.8
(button renamed "Timetable" on the Today line; month sheet of day cards; three by two grid; today in
`primaryContainer` plus the word "Today"; opens scrolled to today; Save as PDF in the header; no jama'ah lines).

## 1. Recommendation

"Timetable" with a calendar glyph replaces "Save as PDF" on the Today line. It opens a full height sheet: title
"Timetable" with the X, the place name with a compact "Save as PDF" beside it, then this month and next month as
two headed runs of day cards, opened with today's card at the top. Each card: the date, then six times in a grid
with their names (three across, two across at large text, one column at 320dp and 2.0). Today's card is
`primaryContainer` with a "Today" pill. Save as PDF opens the existing chooser (Today only, 7 days, This whole
month, Ramadan when offered) over the sheet. Inside Ramadan, while the Ramadan card is on, the same door opens the
approved Ramadan table instead, with the same header. For Q8 the Ramadan days are also built as cards (Suhoor ends
and Iftar emphasised) for the side by side pictures only. Two things only Ali can decide (§13): whether next month
is shown under this month, and whether the Ramadan view starts on day 1 or 30 days before.

## 2. The journey, numbered

Words in quotes are on screen.

1. **A normal day.** Times, the "Today" heading with "Timetable" at its end. Tap: the sheet rises full height.
   "Timetable", the X; "London" and "Approximate area" with "Save as PDF" beside them; "October 2026"; today's
   card first, green, "Sat 24 Oct" with the "Today" pill, then Fajr 5:52 am, Sunrise 7:39 am, Dhuhr 12:46 pm /
   Asr 3:17 pm, Maghrib 5:45 pm, Isha 7:21 pm. The next days follow.
2. **Looking back or ahead.** Scroll up for the days already gone (plain, not dimmed); scroll down through the month,
   then "November 2026" and its days. Nothing else to tap.
3. **Saving.** "Save as PDF": the chooser the app already has ("Save your timetable", "We will make a PDF ... for
   London", Today only / The next 7 days / This whole month, and "Ramadan 1448" when offered) slides over the sheet.
   A pick closes the chooser, the PDF saves, "Saved to Downloads as PrayerTimes_October2026.pdf" shows, and the
   sheet stays open.
4. **Month end.** On 31 October the sheet opens on the 31st with November's heading and days straight under it.
5. **Ramadan, card on.** From day 1 to the last day, "Timetable" opens the approved Ramadan table ("Ramadan 1448",
   Day, Date, Suhoor ends, Iftar, Fast, today's row marked, "Shift the date" at the foot). The Ramadan card's "Get
   the full Ramadan timetable" opens the same thing (Q9). Save as PDF there makes the Ramadan PDF directly, as
   today.
6. **Ramadan, card hidden.** Someone who hid the Ramadan card ("to stop seeing fasting", `HomeScreen.kt:963`)
   gets the month view; "Ramadan 1448" still sits in the Save as PDF chooser.
7. **No location yet.** The Today line is not drawn without today's times (`HomeScreen.kt:909`), so there is no
   button. With the Makkah fallback the sheet says "Makkah" where the place goes; the Times banner already explains.
8. **Large text.** At 1.5 the button drops under "Today" (the step 3 rule); the cards show two times across; at 2.0
   on a 320dp phone, one per line. Nothing is cut and no word breaks.
9. **TalkBack.** "Timetable, button". In the sheet: "Timetable, heading", "Close, button", "London, Approximate
   area", "Save as PDF, button", "Prayer times for October 2026, heading", then one stop per card: "Today, Sat 24
   Oct. Fajr 5:52 am. Sunrise 7:39 am. Dhuhr 12:46 pm. Asr 3:17 pm. Maghrib 5:45 pm. Isha 7:21 pm."

## 3. The Today line

| | Value |
|---|---|
| Label | `action_timetable` "Timetable" |
| Icon | `Icons.Outlined.CalendarMonth` (already used for "Last 30 days", `MyPrayersScreen.kt:394`, so one glyph means "the month" everywhere); SF Symbol `calendar`. Not mirrored (a grid points nowhere) |
| Component | `CtaButton(compact = true)` with the 18dp leading icon, exactly `HomeScreen.kt:921-926` with the icon and string swapped |
| Large text | unchanged: at `fontScale >= 1.5f` the button sits under the heading, full width column (`HomeScreen.kt:928-953`) |
| Gaps | unchanged: `Space.Group` under the card above, `Space.Related` over the list |
| Action | sets one `timetable` flag (replaces `exportSheet = true` at `HomeScreen.kt:234`) |

Rejected: `DateRange` (a date picker's glyph, reads as "choose a date"); `EventNote` (an appointment); icon only
(the owner's rule, "icon-only is guesswork", `HomeScreen.kt:916`).

## 4. The sheet

New file `ui/home/TimetableSheet.kt`. `ModalBottomSheet` with `timetableSheetState` (`HomeScreen.kt:144`,
`skipPartiallyExpanded = true`: thirty cards, a half sheet shows two).

**Header** (column, horizontal padding 24dp as `MyPrayers.kt:110` and `RamadanTimetableSheet.kt:98`):
1. `DialogTitle` in `headlineSmall` with `timetable_title` and the X (`MyPrayers.kt:111-112`; `Common.kt:60-73`).
   The X is kind D, alone at the top end; SF `xmark`.
2. `Space.Tight`, then a row: column (weight 1) of the place name `bodyMedium` (`placeName(context).bidiIsolated()
   .ifBlank { pdf_place_unknown }`) over `pdf_area_label` `bodySmall` `onSurfaceVariant`, exactly
   `RamadanTimetableSheet.kt:106-119`; at the row's end, vertically centred, `CtaButton(compact = true)` with
   `Icons.Outlined.PictureAsPdf` 18dp (SF `arrow.down.doc`) and `action_save_pdf`. A labelled button beside text is
   §4.1's compact case; the X is on the line above, so no icon sits beside another. At `fontScale >= 1.5f` the
   button stacks under the place text, full width, `Space.Related` above it (the Today line's rule).
3. `Space.Group` to the list.

**The same header goes on the Ramadan table** (`RamadanTimetableSheet.kt:96-126`): a shared private
`TimetableHeader(title, place, onExport, onClose)`; the table under it is untouched until Q8. Reason: one door, one
header, and the Q8 pictures compare bodies, not headers. It adds the X to an approved sheet and changes its title
from `titleLarge` (`RamadanTimetableSheet.kt:104`) to `headlineSmall`: both in the pictures.

**Save as PDF** opens the existing `ExportSheet` (`HomeScreen.kt:725-767`, unchanged) over the Timetable sheet,
with its own `exportSheetState`. The pick calls `onExport(range)`, closes the chooser only; the toast
(`MainActivity.kt:246`) confirms over the sheet. The Ramadan range is offered on the same rule as today
(`ramadanExportYear`, `HomeScreen.kt:174-181`). Rejected: a dialog instead of the chooser (changes an approved
sheet for no gain); collapsing to one button (Q7 keeps the ranges); closing the Timetable after a save (the user
may still be reading).

**List** (`LazyColumn`, `contentPadding` 16dp sides and `Space.Section` bottom plus the navigation bar inset):
- A month heading item, then that month's day cards; then the next month's heading and cards (§13 Q2).
- Month heading: `titleMedium`, `onSurface`, padded 8dp sides so it aligns with the title; `semantics { heading();
  contentDescription = timetable_month_a11y(name) }`. Name from `DateTimeFormatter.ofPattern(DateFormat
  .getBestDateTimePattern(AppLocale.of(context), "LLLLyyyy"), locale)`: "October 2026", "اکتوبر 2026", "Ekim 2026".
  This is the one place a year shows, because the second month can be January. **Month headings are
  `stickyHeader` items with the sheet's own background (`surfaceContainerLow`)**, so the month name stays visible
  while its cards scroll under it, and `scrollToItem(todayIndex)` lands today's card directly under the pinned
  heading (plan-verifier: without this the heading scrolls off and §2 items 1 and 9 do not hold). It is also
  iOS's plain list section header. `todayIndex` stays `dayOfMonth`.
- Gaps: first heading `padding(bottom = Space.Related)`; later headings `padding(top = Space.Section, bottom =
  Space.Related)`; each card `padding(bottom = Space.Paragraph)` (stacked cards, §2 table). No `spacedBy`.
- Rows come from `PrayerEngine.computeRange(coordinates, month.first, month.length, prefs)`
  (`PrayerEngine.kt:375-381`) per month, in `produceState` on `Dispatchers.Default` keyed on coordinates, prefs and
  today (`RamadanTimetableSheet.kt:85-88`). The header draws at once; cards appear when ready (milliseconds); no
  spinner, no entrance animation.
- **Opens on today:** once rows arrive, `listState.scrollToItem(TimetableList.todayIndex(today))`
  (`RamadanTimetableSheet.kt:91-93` pattern): today's card is the first visible under the header, yesterday one
  flick up. Rejected: today two rows down (at 2.0 on 823dp one card is ~250dp and today would sit half off).
- **Footer**, only when any day is approximated: `pdf_approximate_note` with the latitude, `bodySmall`
  `onSurfaceVariant` (`RamadanTimetableSheet.kt:233-249`). No button at the foot: the month is Gregorian, nothing to
  shift.
- **If today's times vanish while open** (`state.today == null`): `LaunchedEffect { onDismiss() }`
  (`RamadanTimetableSheet.kt:80-83`).
- Add `timetable` to `calm` (`HomeScreen.kt:156`): Google's rating card must not land over the sheet.

**Which view opens** (one `val ramadanView = state.ramadan?.info?.isRamadan == true &&
state.settings.ramadanCardEnabled`, the same test as the suhoor and iftar labels on the rows, `HomeScreen.kt:963`;
extract it so both read one value; it lives inside private `TodayTimeline` today, so hoist it to `HomeScreen`'s
body, where the sheet is decided): true → `RamadanTimetableSheet`; false → the month sheet. One `timetable` flag
replaces `ramadanTimetable` (`HomeScreen.kt:140`); the Ramadan card's button sets the same flag (it only shows
inside Ramadan with the card on, so it always lands on the table). If Ali picks the 30 day option in §13 Q1:
`ramadanView = HijriCalendar.ramadan(today, shift)?.offersTimetable(today) == true && ramadanCardEnabled`, and
`RamadanTimetableSheet` takes `info: RamadanInfo` as a parameter (today it reads `state.ramadan?.info`,
`RamadanTimetableSheet.kt:78`, which is **null until three days before** Ramadan, `RamadanState.kt:76-77`, so the
table cannot open earlier without this change).

**Hijri dates on cards: no.** The date line is Gregorian; the Hijri day is the Ramadan table's job (its Day column)
and the header's. Fewer numbers per card, and no second calendar to translate on thirty cards.
**Past days: not dimmed.** Pillars fades them and fails AA (FABLE-REVIEW); the scroll position already says which
days are gone, and a faded day reads as a day to feel bad about.

## 5. The day card

`internal fun DayCard(day: DayPrayerTimes, isToday: Boolean, columns: Int)`, in `TimetableSheet.kt` (internal so
`LargeTextAndTalkBackDeviceTest` can compose it). A slot missing from `day.times` is omitted from the grid and the
spoken text; `computeRange` always fills all six today, so this is a guard, not a state.

| Part | Spec |
|---|---|
| Surface | `sajdaSurface(RoundedCornerShape(24.dp))` (`Theme.kt:163-177`: 2dp shadow in light, hairline in dark); today `sajdaSurface(shape, color = primaryContainer)`. Padding 16dp sides, 12dp top and bottom (the component's own padding) |
| Line 1 | `TimeFormat.date(context, day.date)` (`TimeFormat.kt:68-72`: "Sat 24 Oct", in the app's language and digits), `titleMedium`; weight 1. At the end on today only: the "Today" pill, `home_today`, drawn exactly as the Next pill (`labelMedium`, `onPrimary` on `primary`, `RoundedCornerShape(50)`, padding 9 by 3). After the date on an approximated day: `ramadan_approx_marker` "approx." `labelSmall`, 8dp after it |
| Gap | `Space.Related` (a heading and the block it introduces) |
| Grid | six cells in the day's order, Fajr, Sunrise, Dhuhr / Asr, Maghrib, Isha, in rows of `columns`; rows `Space.Related` apart; cells `weight(1f)` with 8dp between (a horizontal gap, not tokenised). Start aligned, so Urdu and Arabic mirror by themselves and Fajr sits at the right |
| Cell, 2 or 3 columns | label `labelMedium` `onSurfaceVariant` over the time, `Space.Tight` between; the time `PrayerTimeTextStyle.copy(fontSize = 15.sp)` (tabular, as `RamadanTimetableSheet.kt:197`) in `onSurface` |
| Cell, 1 column | a row: label (weight 1) at the start, time at the end, like the Times rows |
| Sunrise cell | label and time `onSurfaceVariant`, time in `bodyMedium`: not a prayer, one step quieter, as the Times list (§4) |
| Today's card | every text `onPrimaryContainer` (as the Next row); Sunrise still `bodyMedium`, so weight separates it, not colour alone |
| Names | `stringResource(slot.labelRes)` (`PrayerLabels.kt:21`); never typed |
| Times | `TimeFormat.clock(context, instant)` (`TimeFormat.kt:39`), the same call as the Times rows, so digits and am/pm match the screen |
| Columns | `TimetableGrid.columns(contentWidthDp, fontScale)` = `floor(width / (84 × fontScale))` clamped to 1..3. Width from one `BoxWithConstraints` round the list: `maxWidth - 32 (list sides) - 32 (card sides)`. On 360dp: 3 at 1.0 and 1.15, 2 at 1.3 and 1.5, 1 at 2.0; on 320dp: 3 at 1.0, 2 at 1.5, 1 at 2.0. 84dp is a column's share including its 8dp gap, so a cell is at least 76dp × fontScale; "12:46 pm" at 15sp measures about 62dp; one rule, measured once, no per cell wrapping |
| TalkBack | the card is one node: `semantics(mergeDescendants = true) { contentDescription = spoken }`, `spoken = timetable_day_a11y(dateText, six times)`, wrapped in `timetable_today_a11y` on today. No role (not actionable) |
| Contrast | pairs already in `ColorContrastTest`: `onSurface/surface` (:96, :122), `onSurfaceVariant/surface` (:97, :123), `onPrimaryContainer/primaryContainer` (:101, :127), `onPrimary/primary`. Add the three absent: dark `onSurface/surfaceContainerLow`, and `onSurfaceVariant/surfaceContainerLow` in light and dark (the sheet's own colour under the sticky month heading) |
| Motion, haptics | none |

Rejected: one card per month with hairline rows (a `LazyColumn` cannot scroll to a row inside one item, and thirty
rows in one item defeats laziness); a six column table (what breaks at 320dp and 2.0, the reason for cards); a
seventh "next" marker on the card (the Times screen answers that); fading past days (§4).

## 6. Ramadan cards, for Q8 only

> **Answered 24 Oct 2026: Ali kept the table.** This section is the record of what was shown; the prototype and
> its two strings were deleted. Not a task.

Built so Ali can see table and cards side by side; not shipped unless he picks cards. The same `DayCard` with
`ramadanDay: Int?`:
- Line 1: "Day 12" (`timetable_ramadan_day`, `titleMedium`), then the date in `bodyMedium` `onSurfaceVariant` 8dp
  after it; the Today pill at the end.
- Grid in the same order and spots, so the eye finds Fajr and Maghrib where they are every other month; the Fajr
  cell is labelled `ramadan_suhoor_ends` and the Maghrib cell `ramadan_iftar` (as the Times rows,
  `HomeScreen.kt:964-965`); those two times `SemiBold` and `primary` (`onPrimaryContainer` on today's card): the
  two numbers a fasting person wants, found by weight and colour, with the other four still there.
- Under the grid, `Space.Tight`: `timetable_fast` "Fast 12h 16m" (`TimeFormat.remaining`,
  `RamadanTimetableSheet.kt:192`), `labelMedium` `onSurfaceVariant`, so nothing the table shows is lost.
- Footer: `pdf_ramadan_note`, the approximate note, then "Shift the date" full width
  (`RamadanTimetableSheet.kt:233-262`), the one button the table has.
- TalkBack: `ramadan_row_a11y` (exists) for the prototype; if cards win, extend it to the six times.
- `timetable_ramadan_day` and `timetable_fast` are declared `translatable="false"` on the prototype branch so
  `TranslationIntegrityTest` (`:102-113`) passes; if cards win they are translated into all five languages in the
  build commit; if the table stays they are deleted.

## 7. States, accessibility, right to left, sizes

| State | What shows |
|---|---|
| First run, located | Today line with Timetable; the sheet as §4 |
| Makkah fallback | the sheet with "Makkah" in the place line; times are Makkah's |
| No times at all | no Today line, no button (`HomeScreen.kt:909`) |
| Rows not yet computed | header only, for milliseconds; no spinner |
| Polar place | "approx." on the affected cards, the latitude note in the footer |
| Ramadan, card on | the approved table (§4); Ramadan, card hidden: the month view (§2 item 6) |
| After a save | chooser closes; toast on Android 10 and up; on Android 9 and below the system share sheet opens over the Timetable (the existing path, `PrayerPdfExporter.kt:152` returns null below Q and `MainActivity` shares); the sheet stays either way |
| Calendar beyond range | not reachable: the month view is Gregorian; the Ramadan branch guards on `ramadan() != null` as the exporter does |
| Light and dark | existing roles only; `sajdaSurface` does the shadow or hairline |
| Urdu and Arabic | `DialogTitle` puts the X at the left; the grid mirrors; dates and digits from `TimeFormat` |
| 1.5 text | button under "Today"; two columns; labels may wrap between words, never inside one |
| 2.0 at 320dp | one column; the date line wraps before the pill; the Save as PDF button sits under the place text when the row cannot hold both: use the same `fontScale >= 1.5f` stack as the Today line for the place row |
| 823dp | the sheet is full height either way; today's card and about two more show |
| TalkBack | §2 item 9 |
| Remove animations | nothing of ours animates |

## 8. Strings

English; the main session translates ur, tr, in, ar; DETAILED-PLAN 8.13 fixes "Timetable" in each.

```
action_timetable        Timetable
timetable_title         Timetable
timetable_month_a11y    Prayer times for %1$s
timetable_day_a11y      %1$s. Fajr %2$s. Sunrise %3$s. Dhuhr %4$s. Asr %5$s. Maghrib %6$s. Isha %7$s.
timetable_today_a11y    Today, %1$s
timetable_ramadan_day   Day %1$d                      (Q8 prototype only)
timetable_fast          Fast %1$s                     (Q8 prototype only)
```

Reused: `action_save_pdf`, `home_today`, `pdf_place_unknown`, `pdf_area_label`, `pdf_approximate_note`,
`ramadan_approx_marker`, the `export_*` chooser strings, `ramadan_suhoor_ends`, `ramadan_iftar`,
`ramadan_row_a11y`. **`timetable_month_heading` (in BUILD-INSTRUCTIONS §5 row 4) is not needed:** the heading is
the month name from the date formatter, and a key would be a bare "%1$s". Two keys for one word ("Timetable") on
purpose: a translator may need a shorter button than title. In `timetable_day_a11y` the prayer names are
translated inside the string (religious content; translator writes them in their language), and the full stops
stay inside the string so Urdu's stop and Arabic's order are never joined in code. Checked: no "Tap", no "missed",
"late" or "owed", no hyphen or dash.

## 9. Rules in `:core` (plain Kotlin, no `android.*`), and tests

`core/.../TimetableList.kt`:

```kotlin
data class TimetableMonth(val first: LocalDate, val length: Int)
object TimetableList {
    /** This month, then the next: a sheet opened on the 31st must show tomorrow. */
    fun months(today: LocalDate, count: Int = 2): List<TimetableMonth>
    /** Index of today's card in a list laid out as [heading, days..., heading, days...]. */
    fun todayIndex(today: LocalDate): Int = today.dayOfMonth
}
object TimetableGrid {
    fun columns(widthDp: Float, fontScale: Float): Int = floor(widthDp / (84f * fontScale)).toInt().coerceIn(1, 3)
}
```

- **`TimetableListTest`** (core): 31 Oct 2026 → October (31) then November (30); 15 Dec 2026 → December then January
  2027; Feb 2028 has 29; `todayIndex` is 1 on the 1st and 31 on 31 Oct; `count = 1` gives one month (Q2's other
  option). Columns: (296, 1.0) 3; (256, 1.0) 3; (296, 1.3) 2; (296, 1.5) 2; (256, 1.5) 2; (296, 2.0) 1; (256, 2.0)
  1; never 0 or 4.
- **`RamadanPdfRangeTest`** (app, exists): unchanged tests stay green (the exporter is not touched); add one: the
  sheet's first month equals `spanOf(THIS_MONTH)` (`PrayerPdfExporter.kt:618`), so screen and PDF cannot disagree.
- **`RamadanPdfDeviceTest`** (exists, `:93`): rerun unchanged; it proves the chooser's three ranges still render.
- **`ScreenSeedDeviceTest`**: it seeds London (`:25`) and asserts no button label (no "Save as PDF" under
  `androidTest`), so nothing to change; rerun and say so.
- **`DesignRulesTest`**: `TimetableSheet.kt` in the inventory; every `Spacer` and top or bottom padding a token.
- **`TranslationIntegrityTest`**: the five shipped keys in five languages.
- **`ColorContrastTest`**: the two pairs named in §5 if absent.
- **`LargeTextAndTalkBackDeviceTest`**: at 2.0 a card's spoken text begins with "Today," and lists six times; the
  Timetable button and Save as PDF have 48dp bounds; the date never splits inside a word.
- **By hand on the emulator:** 320dp at 2.0 in Urdu (one column, digits); 31 Oct with the clock moved; the chooser
  over the sheet and back; `animator scale 0`.

## 10. DESIGN_SYSTEM changes

§4.1 inventory:
- Times | Timetable (calendar icon), end of the Today heading, under it from 1.5 text; 16dp under the card above,
  8dp over the list | A, compact | 40 drawn, 48 target, wraps | `HomeScreen.kt`
- Timetable sheet | Save as PDF (PDF icon), end of the place line under the title | A, compact | 40 drawn, 48 target,
  wraps | `TimetableSheet.kt`
- Timetable sheet | the X, alone at the top end (`DialogTitle`) | D | 48 | `TimetableSheet.kt`, `Common.kt`
- Ramadan timetable | Save as PDF moves to the place line; the X alone at the top end | A compact; D |
  `RamadanTimetableSheet.kt`
- Remove the old "Save as PDF, Today line" row.

Also in this step's commit: BUILD-INSTRUCTIONS §5 row 4 and DETAILED-PLAN 8.13 lose `timetable_month_heading`.

§9: a dated entry: calendar glyph; two month list; today first; cards not a table and why; past days not dimmed; no
Hijri on cards; the shared header with the X; the chooser kept over the sheet; the width rule. Rejected: rolling 30
days (no month heading, and "This whole month" in the PDF would not match the screen); a segmented "Month |
Ramadan" control (a component the system does not have); a dialog chooser; a FAB.
§11: row `1.6-timetable` (Q1 and Q2 of §13), OPEN until answered; `1.6-ramadan-cards` moves to §9 when Ali picks.

## 11. iPhone effect, and the HIG check

**Carries over.** `TimetableList` and `TimetableGrid` are plain rules (the `java.time` gap is already recorded);
a full height sheet with a grabber and an explicit close is Apple's own pattern; list cells with a label over a
value; SF `calendar`, `arrow.down.doc`, `xmark`; the PDF would be `UIGraphicsPDFRenderer` and the share sheet.
Nothing Android only in this step. HIG: no FAB, no drawer; every screen has a close; 48dp targets; text scales
(the columns rule is the Dynamic Type equivalent, HStack to VStack); the look is Quiet Glass, not a stock timetable
template (4.3).

## 12. Pictures for Ali

Month view, English unless said: (1) light 823dp, mid month, today first; (2) dark; (3) Urdu; (4) 1.5 text, 823dp
(two columns); (5) 2.0 text, 320dp (one column); (6) clock on 31 Oct, light, for Q2 (November under October); (7)
the Save as PDF chooser over the sheet; (8) Times at 1.0 and 2.0 showing the Today line.
Q8, clock on 22 Feb 2027 (day 15 of Ramadan 1448, the date `RamadanPdfRangeTest` pins), same scroll position: (9)
table light beside cards light; (10) table dark beside cards dark. For `plan-verifier` and `reviewer`: TalkBack on
one card; Urdu at 320dp 2.0.

## 13. Questions for Ali (answered 24 Oct 2026: Q1 day 1, Q2 this month and next, Q4 no Hijri; Q8 open)

1. When the Timetable opens on Ramadan: **"From day 1 to the last day, with the Ramadan PDF offered 30 days ahead
   as now (Recommended)"**, or "From 30 days before day 1, as planned". Why the change from the plan: someone
   tapping "Timetable" on 10 January expects January, and would find only Ramadan with no way to the month; the
   ten journeys (DETAILED-PLAN F and I) both say "in Ramadan". The PDF keeps the 30 day lead either way.
2. Under this month: **"Show next month too, under its own heading (Recommended)"**, or "This month only". Why:
   on the 30th the sheet would show one card and twenty nine gone.
3. Q8, from pictures 9 and 10: cards or the table (not a new question; listed so it is asked in the same round).
4. Hijri date on the day cards (added by `plan-verifier`, DESIGN_SYSTEM §0.2 item 4: it changes every card):
   **"No, Gregorian date only (Recommended, §4)"**, or "A small Hijri day after the date".

## Verified in code, and assumed

Verified: `HomeScreen.kt:136-144` sheet flags and states, `:156` calm, `:174-181` `ramadanExportYear`, `:232-238`
Today line call, `:275-289` Ramadan sheet call, `:303-313` and `:725-801` the chooser, `:902-953` the Today line and
its 1.5 stack, `:963-965` the Ramadan labels rule; `RamadanTimetableSheet.kt:78-126, 185-262`;
`IslamicDatesSheet.kt:60-66`; `MyPrayers.kt:110-112` (X on a sheet via `DialogTitle`); `Common.kt:60-73`;
`PrayerPdfExporter.kt:102, 615-626`; `PrayerEngine.kt:375-381`; `RamadanState.kt:71-84` (null until three days before);
`HijriCalendar.kt:62-63, 146`; `TimeFormat.kt:39, 68-72`; `PrayerLabels.kt:21`; `Theme.kt:163-177`;
`ColorContrastTest.kt:97-135` pairs; `MainActivity.kt:246` toast; `MyPrayersScreen.kt:394` CalendarMonth;
`RamadanPdfRangeTest`, `RamadanPdfDeviceTest:93`, `ScreenSeedDeviceTest:25`; no "Save as PDF" in `androidTest`.

Assumed: two `ModalBottomSheet`s stack cleanly on M3 1.3 (each is its own window; check on the emulator, and if the
first closes under the second, the fallback is the chooser as a dialog with `DialogTitle`); 84dp × fontScale holds
"12:46 pm" and Urdu digits at every scale (measure at 320dp Urdu 2.0 before the pictures); `getBestDateTimePattern`
gives a standalone month name in Urdu and Arabic; sixty one engine calls take milliseconds on an old phone.

**Risks (Fable):** the Ramadan table's header changes (the X added) on an approved screen, so it must be in the
pictures; the width rule is arithmetic until measured; a user in the 30 lead days loses the on screen Ramadan
table if Ali picks Q1's recommendation (the PDF keeps it).

**Note from Fable to the main session:** `state.ramadan` is null until three days before Ramadan
(`RamadanState.kt:76-77`), so "the sheet shows Ramadan while `offersTimetable` is true" (30 days, DETAILED-PLAN 8.5)
cannot work with `RamadanTimetableSheet` as it stands. Raised as Q1 rather than built silently; §4 gives both
branches so the implementer builds whichever Ali picks.

## Plan check (`plan-verifier`, 24 Oct 2026)

Every file:line checked against `claude/1-6-0` at f36613e; Fable's null claim confirmed (`RamadanState.kt:76-77`,
`NOTICE_LEAD_DAYS = 3L` at `HijriCalendar.kt:66`). Three blocking fixes applied to the text above: sticky month
headings (§4), `translatable="false"` on the Q8 prototype strings (§6), the share sheet path below Android 10 (§7).
Minor fixes applied: the 84dp reason, the Ramadan title style change, the place row stack moved into §4,
`internal DayCard`, the missing slot guard, the contrast pairs named exactly, hoisting the Ramadan rule, the stale
`timetable_month_heading` references, two line numbers. Verdict: sound; none of the fixes change what Ali is shown.
It added one question for Ali (Hijri date on cards), now §13 Q4.

## After the build (24 Oct 2026)

Opus `reviewer` found that the Ramadan table's Save as PDF (§2 item 5, "as today") had become the only door to
Save as PDF in Ramadan, so Today, 7 days and the month were unreachable all Ramadan. **Ali, 24 Oct 2026: "Show the
choices"**: Save as PDF on the Ramadan table opens the same chooser as the month sheet; it offers Ramadan too.
Also fixed from the review: today's card scrolled down by the pinned heading's height, the month name through ICU,
months and days produced together, the later month's gap as its own item. Rules recorded in HANDOVER §5.32.

**Q8, 24 Oct 2026:** Ali approved the month view pictures and chose "Keep the table". Prototype deleted.
