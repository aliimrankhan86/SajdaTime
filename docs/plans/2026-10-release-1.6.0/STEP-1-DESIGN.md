# Step 1: the My prayers tab, design (Fable `ux-lead`, 23 Oct 2026, night)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then this file for step 1 only, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

Written by the `ux-lead` agent (Fable 5.1) for step 1 of `BUILD-INSTRUCTIONS.md` §5, before any spec or code,
as Ali's process asks: journey and design first, then `plan-verifier`, then prototype pictures for Ali's yes,
then the build. Skills used: `sovereign-ui` (task, hierarchy, states, adaptive behaviour) and `apple-design`
(tab bar, checklist and empty state conventions); neither overrode the design system. **Status: a
recommendation. Nothing here is decided until Ali says yes from the pictures.**

## 1. Recommendation

Build the tab as a daily checklist: five rows with the tick at the **start** (the Reminders pattern both
platforms know), the week of rings in its own small card under it, one button row between them that crossfades
to the paused line, and the switch card as the off state. Two plan details change for ease of use: the Last 30
days sheet becomes a **calendar grid in weeks of seven** (same first day and names as the week strip, choose a
day to edit it below), and the tab icon becomes the tick in a circle (`CheckCircle`), the same shape as the tick.

## 2. The journey, numbered

Words in quotes are on screen. "(exists)" means the string is in `values/strings.xml` today.

1. **First open after the update, tracking off.** The tab bar reads "Times · Qibla · My prayers · Settings".
   Open "My prayers". Top to bottom: title "My prayers"; one card with a switch row: tile icon, "My prayers"
   (exists), "Tick each prayer you pray. Only you can see it." (exists), switch off; under the card one line
   "Nothing is tracked until you switch this on." Nothing else: no illustration, no sales text.
2. **Switching on.** The card crossfades (`Standard`) to today's checklist. No snackbar: the person is looking at
   the result. Settings' My prayers switch shows on too (the same setting).
3. **A normal day (Friday 23 Oct, 10:53).** Title "My prayers". Line: "Today" at the start, "Fri 23 Oct" at the
   end. A card of five rows: Fajr 5:46 am with a plain ring (passed, not ticked); Dhuhr with the Now pill once
   Dhuhr begins; Asr, Maghrib and Isha with faded rings (not due yet). Ticking Fajr fills its ring green and draws
   the tick (`Standard`), one ToggleOn buzz, and Friday's ring in the week card grows one fifth (`Draw`). Under the
   card: "Pause" at the start, "Last 30 days" at the end. Under that: "This week", seven rings with the day names
   by the app's language, today marked, days to come faint. If Show streak is on and above zero, "12 days in a
   row" (exists) under the rings. Nothing else happens, ever.
4. **Isha after midnight (00:40 Saturday, Isha still current).** The card's first row is "Last night's Isha
   7:36 pm" with Friday's tick, then Saturday's five, none due. Ticking that row writes to Friday. The Today line
   says "Sat 24 Oct".
5. **Pausing.** "Pause": today's five rings show the pause glyph (two bars in a ring), full contrast, not
   tickable; the button row crossfades (`Standard`) to "Tracking is paused" (exists) with "Resume" (exists) at the
   end; the week card's today ring shows the pause glyph; a snackbar "Paused from today" with "Undo" for four
   seconds. No reason is asked. Pause from yesterday stays in Settings (its dialog exists).
6. **Resuming.** "Resume" (or Undo): the rows come back for today and the button row crossfades back. Paused days
   keep the glyph and count nothing. No message.
7. **Opening Last 30 days.** A full height sheet: "Last 30 days" with the X; day names; a grid of rings in weeks of
   seven, oldest week at the top, today last; days before tracking began show no ring (Q4). Today is selected:
   under the grid, "Today" with the five prayer names and five ticks, editable. Choosing another day shows its row
   ("Thu 22 Oct", or "Paused" for a paused day). Close with the X or a swipe.
8. **No location (Makkah fallback).** Above the card a warning notice "Set your location to see prayer times
   here." with "Set location" (exists), which opens the location sheet. Rows show names and rings only, no times,
   and every ring is tickable (nothing is "not due" while the times are unknown).
9. **Tracking switched off in Settings.** The tab returns to step 1's switch card. The record stays; "Delete my
   prayer record" stays in Settings.

## 3. Each screen, top to bottom

Icons: Material (installed `material-icons-extended`) with the SF Symbol beside each. None points a direction
except PlayArrow, which by media convention is not mirrored in right to left.

### 3a. My prayers tab, tracking on

| # | Element | Component | Gap below | Detail |
|---|---|---|---|---|
| 1 | "My prayers" | `ScreenTitle` | its own | As Qibla and Settings (DESIGN_SYSTEM §8, every tab opens the same way) |
| 2 | Today line | `Row`: "Today" (`home_today`) titleMedium, `heading()`, at the start; the date (`TimeFormat.date`) bodyMedium `onSurfaceVariant` at the end | `Space.Related` | Mirrors the Times Today line |
| 3 | Notices, when needed | `Notice` warning (no location) or quiet (read only record) | `Space.Group` | See the states table |
| 4 | Today card | `sajdaSurface`, 24dp radius, rows divided by `outlineVariant` hairlines, row minimum 56dp | `Space.Group` | Row: `Tick` 48dp target at the **start**; name bodyLarge `onSurface`; optional Now pill (`secondaryContainer`); time bodyLarge tabular `onSurfaceVariant` at the end. No sun glyphs, no Next pill, no Sunrise row |
| 5 | Button row | `CtaButton(compact = true)` "Pause" with `Icons.Rounded.Pause` (SF `pause.fill`) at the start; `CtaButton(compact = true)` "Last 30 days" with `Icons.Outlined.CalendarMonth` (SF `calendar`) at the end | `Space.Section` | Paused: `AnimatedContent` (`Motion.standard()`, `sizeTransform = null`) to "Tracking is paused" bodyMedium `onSurfaceVariant` at the start and compact "Resume" with `Icons.Rounded.PlayArrow` (SF `play.fill`) at the end, same height. If the two would touch (320dp at 2.0 text) they stack, Pause first, `Space.Related` apart |
| 6 | Week card | `sajdaSurface`, 24dp, 16dp padding: "This week" labelLarge medium `onSurfaceVariant`; `Space.Related`; seven rings (`SpaceBetween`); `Space.Tight`; day names; then, only with Show streak on and above 0, `Space.Paragraph` and "12 days in a row" bodyMedium `onSurfaceVariant` | `Space.Section`, then `BottomBarClearance()` | Rings 36dp (32dp under 340dp, 9.9), 3dp track `outlineVariant`, arc `primary` from 12 o'clock clockwise, share of five; five of five fills `primary` with an `onPrimary` tick; paused: track and pause glyph in `outline`; days to come: track at 38 percent, no arc; before tracking began: no ring, the name only. Today: a 2dp `outline` halo and its name in `primary` SemiBold. Not tappable (8.6). Day names `DayOfWeek.getDisplayName(TextStyle.SHORT, appLocale)` with `WholeWords`, labelMedium (not NARROW: single letters give "T T" and "S S" in English and Turkish) |

**Tick** (`Tick.kt`, drawn on a Canvas instead of two icons, so §6 row 1 can draw the mark): 24dp in a 48dp
target, 2dp ring `outline`; ticked fills `primary` with an `onPrimary` tick (SF `checkmark.circle.fill`); not due
yet: the ring at 38 percent, disabled; paused: the ring plus two 2dp bars in `outline`, full contrast, disabled (SF
`pause.circle`). `Role.Checkbox`, haptic ToggleOn and ToggleOff (exists).

### 3b. Tracking off

Title; `Space.Group`; one `sajdaSurface` card holding the Settings style switch row (36dp `secondaryContainer`
tile, `Icons.Outlined.CheckCircle`, title, subtitle, Material `Switch`); `Space.Related`; "Nothing is tracked until
you switch this on." bodyMedium `onSurfaceVariant`. Nothing below. Switching on: `AnimatedContent`
(`Motion.standard()`) to 3a.

### 3c. Last 30 days sheet

`ModalBottomSheet`, full height (exists). "Last 30 days" heading with the close X (`DialogTitle` pattern, kind D);
the quiet read only notice if the record is newer; `Space.Group`; day names labelMedium `onSurfaceVariant` in seven
columns; `Space.Tight`; the grid: five or six rows of seven cells, each a 32dp ring (the week card's drawing) with
the day number labelMedium under it, cell height 56dp, `selectable` with `Role.RadioButton`, today selected by
default with the halo; days before `tracker_started` draw nothing and cannot be chosen; `Space.Section`; the chosen
day's row: date titleSmall, the five names labelMedium over five `Tick`s (the existing per day row,
`MyPrayers.kt:126-160`), or "Paused" (exists); `Space.Section` at the foot. The range ends today. The grid replaces
30 cards (today's sheet is one card per day, about 3300dp tall).

### 3d. Tab bar

`Destination.PRAYERS` third, `Icons.Rounded.CheckCircle` selected and `Icons.Outlined.CheckCircle` unselected (SF
`checkmark.circle.fill` and `checkmark.circle`). Not `TaskAlt`: it is a stroke icon in both sets, so the selected
"filled icon" signal would be lost; `CheckCircle` has a true filled and outlined pair and is the tick's own shape.
The Settings "My prayers" row icon becomes the same (`SettingsScreen.kt:454`, now `TaskAlt`). Four labels at 320dp
and 2.0 rely on the shipped `WholeWords` shrink; Urdu "میری نمازیں" may wrap onto two lines, which the measured
clearance absorbs.

### States, all screens

| State | What shows |
|---|---|
| Loading | Title only; nothing drawn until `state.loading` is false (the switch card never flashes) |
| Off | 3b |
| On, before any tick | 3a with every ring empty; the week shows rings only from `tracker_started`; no message |
| First day | Week card: today's ring only, earlier days by name only; Last 30 days shows one cell |
| Paused | Journey 5; Pause hidden, Resume shown; Last 30 days still shown; Pause from yesterday in Settings |
| Read only record (newer version) | Quiet notice "This record is from a newer version of SajdaTime. Update the app to change it." (exists) above the card; every tick disabled; Pause hidden; Last 30 days shown, its ticks disabled |
| No location | Warning notice with "Set location"; times hidden; every ring tickable |
| Isha after midnight | A sixth first row "Last night's Isha" writing to yesterday |
| 2.0 text, 320dp | The column scrolls; rows grow; rings stay 36 or 32dp; the buttons stack; grid cells are 41dp wide on 320dp, below 48 (the same compromise as Material's date picker; recorded, not hidden) |
| Urdu and Arabic | Tick on the right, time on the left; rings in the locale's day order from the start edge; arcs sweep clockwise in both (deliberate, BUILD-INSTRUCTIONS §6); PlayArrow not mirrored |
| TalkBack | Tick: "Fajr, prayed" / "not ticked" / "not due yet" / "paused", checkbox. Week ring: "Wednesday, 3 of 5 prayed"; today "Today, 2 of 5 so far"; paused "Wednesday, paused"; to come "Saturday, to come"; before start "Monday, before tracking began". Grid cell: the same plus "selected". Pausing announces the snackbar text |
| Animations | Only BUILD-INSTRUCTIONS §6 rows 1, 2, 3 and 5; nothing on open |

## 4. Strings (English; the main session translates)

**Kept from DETAILED-PLAN 6.2 and 8.13:** `nav_tracker` "My prayers"; `tracker_off_line` "Nothing is tracked until
you switch this on."; `tracker_paused_from_today` "Paused from today"; `action_undo` "Undo"; `tracker_state_paused`
"paused"; `tracker_last_night_isha` "Last night's Isha". Reused (exist): `action_pause`, `action_resume`,
`home_today`, `tracker_open_history`, `tracker_history_title`, `tracker_paused_line`, `tracker_paused_day`,
`tracker_newer_version`, `tracker_streak`, `action_set_location`.

**Changed:** `tracker_ring_a11y` plurals "%1$s, %2$d of 5 prayed"; `tracker_ring_today_a11y` plurals "Today, %1$d
of 5 so far"; `tracker_ring_paused_a11y` "%1$s, paused" (kept).

**Added:** `tracker_week_title` "This week"; `tracker_ring_future_a11y` "%1$s, to come"; `tracker_ring_before_a11y`
"%1$s, before tracking began"; `tracker_needs_location` "Set your location to see prayer times here."

Checked: no "Tap", no "missed", "late" or "owed", no hyphen or dash; `TrackerWordingTest`'s `tracker_` prefix covers
the new keys.

## 5. Changes to the plan, and what stays shut

Changes, with reasons:
1. **The 30 day grid in weeks of seven, in calendar order, today last,** instead of six rows of five, newest
   first. A reversed grid of day numbers reads against every calendar a person has seen; weeks of seven share the
   first day and the names with the week card, so there is one mental model. Q4 still holds. **Visible: Ali
   decides from screenshots (question below).**
2. **Tab icon `CheckCircle`,** not `TaskAlt` (3d). Icon choice is engineering detail (DESIGN_SYSTEM §0.2).
3. **Day names SHORT, not NARROW** (8.6): single letters are ambiguous in English and Turkish.
4. **Tick at the row's start** (DETAILED-PLAN 2.4 and FABLE-REVIEW already say so). Times keeps nothing of the tracker.
5. **No sun glyphs and no Next pill on the tab's rows;** only the Now pill.
6. **The review card needs no code here:** its calm timer lives in `HomeScreen` (`HomeScreen.kt:140-153`), which is
   not composed while another tab shows. Record it; add no flag.

Not reopened (Ali's decisions): the name "My prayers" (Q3); the fourth tab, always visible, with the switch when
off; Pause on the tab with Undo, Yesterday in Settings; labelled compact solid buttons, never icon buttons side by
side; streak hidden by default and only a number; no missed count, no reasons, no year grid; Q4 and Q5; no usage
event; the Isha after midnight rule; save, load and delete staying in Settings.

Risks: Canvas rings are new drawing code (the `primary` and `outline` pairs on `surface` are already asserted; add
the 38 percent future track as a non text pair or exempt it as disabled). Four tab labels in Urdu at 2.0 may wrap;
measure. The 41dp grid cell on 320dp.

## 6. Prototype pictures for Ali

Each in light and dark, 823dp frame, English:
1. **Times before and after:** today's Times with ticks (`screens/times-tracker-jamaah.png`, the e98b2a6 build)
   beside Times with no ticks and the four tab bar. One look: the list is calmer and fits again.
2. **My prayers, a normal afternoon:** Fajr and Dhuhr ticked, Dhuhr Now, Asr onwards faded, the week card with
   Monday to Thursday partly filled, Friday today, streak hidden.
3. **My prayers, paused:** the same day after Pause, snackbar showing.
4. **My prayers, off:** the switch card.
5. **Last 30 days:** the current card list (`screens/last-30-days.png`) beside the grid with today chosen and its
   five ticks below.
6. For `plan-verifier` and `reviewer`, not Ali: Urdu right to left, 2.0 text at 320dp, TalkBack on one row.

## 7. iPhone effect

Carries over: a tab bar, a list with leading check circles (as in Reminders), a sheet with an X, a switch, a
calendar grid, SF Symbols named above. To revisit there: the Undo snackbar (Material only) becomes an inline
"Paused from today · Undo" line in the button row's place.

## 8. Question for Ali

Last 30 days as a calendar grid in weeks of seven with the chosen day's ticks underneath (recommended; picture 5),
or keep the list of day cards already built? He decides from the pair.

## Verified in code, and assumed

Verified by `ux-lead`: `MyPrayers.kt`, `Tick.kt`, `TrackerLogic.kt` (resuming the same day leaves nothing, so Undo
is `resume()`), `TrackerRepository.kt`, `MainScaffold.kt` (three destinations, crossfade, a usage word per tab),
`HomeScreen.kt` tracker lines 126-153, 215-218, 907-946, `SettingsScreen.kt` 440-525, `SajdaViewModel.kt:93-124`,
every string marked "exists", `ScreenTitle` padding, `Notice` signature, the nav labels in five languages,
`material-icons-extended` 1.7.8, the three plan pictures and two store screenshots.

Assumed: the Settings toggle row can be composed outside `SettingsList`; the location sheet in `Common.kt` can be
opened from this tab; `Icons.Outlined.CheckCircle` exists (the Rounded twin is used in `Tick.kt`); every dp sum
(654dp at 823dp, 41dp grid cells) is arithmetic, not a render.

## 9. Corrections from `plan-verifier` (23 Oct 2026, night). These win over §2 to §8 above

1. **No location means two things in code; both are treated alike.** No location =
   `!state.loading && (settings.coordinates == null || settings.usingDefaultLocation)`. In the Makkah fallback
   `state.today` holds Makkah's times, so the rows must ignore `today` and `current` in this state: no times, no Now
   pill, every ring due (tickable), and no "Last night's Isha" row. Ticking is safe: the record stores one bit per
   `LocalDate`, never a time (`TrackerRepository.setTicked`). The notice reuses `home_no_location` "Set your location
   to see prayer times." (exists, the 8.6 wording), so `tracker_needs_location` is **not** added.
2. **DESIGN_SYSTEM §4.1 inventory rows** (step 1 rewrites today's rows 461-464, the Times placement; `DesignRulesTest`
   fails a file with a button that is not listed):
   - My prayers tab | the tick at the start of each prayer row | Tick (`Tick.kt`), a toggle | 24 drawn, 48 target | `MyPrayersScreen.kt`, `Tick.kt`
   - My prayers tab | Pause (pause icon) at the start, Last 30 days (calendar icon) at the end, under the today card | A, compact | 40 drawn, 48 target, wraps | `MyPrayersScreen.kt`
   - My prayers tab, while paused | Resume (play icon) at the end, beside "Tracking is paused" | A, compact | 40 drawn, 48 target, wraps | `MyPrayersScreen.kt`
   - My prayers tab, tracking off | the switch row; not a button | Switch | 56 row | `MyPrayersScreen.kt`
   - My prayers tab, no location | Set location, the notice's action | A (notice) | 48, wraps | `Notice.kt`
   - Last 30 days sheet | the X, alone at the top end | D | 48 | `MyPrayers.kt`
   - Last 30 days sheet | each day cell is `selectable`, not a button; the chosen day's five ticks under the grid | Tick | 24 drawn, 48 target | `MyPrayers.kt`
   - Every tab | Undo in the message at the bottom (Material's own snackbar action, written with `showSnackbar(actionLabel = ...)`, so no `TextButton` in our source) | **G, new** | platform | `MainScaffold.kt`

   Kind G enters the §4.1 kinds table in step 1, not step 3: step 1 ships the first snackbar.
3. **Day names by measurement, not by language.** `TextStyle.SHORT` first; CLDR's short name in Arabic and Urdu
   is the full word (الأربعاء), which does not fit a 36dp column at 320dp. Measure all seven with
   `rememberTextMeasurer` at labelMedium against the column width; if any one does not fit, use `NARROW` for all
   seven. TalkBack always speaks the full name.
4. **The week strip never wraps.** DETAILED-PLAN 9.9's two row wrap is dropped: seven 32dp rings take 224dp of the
   256dp inside the card at 320dp. `WeekStripTest` covers 36 or 32dp by width, the share of five and the first day.
5. **The button row wraps by measured width,** never by a width or font scale number:
   `FlowRow(horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(Space.Related))`
   for both the Pause and Last 30 days row and the "Tracking is paused" and Resume row, so both stack alike. With
   that, `AnimatedContent` keeps its default size transform (`Motion.standard()`), not `sizeTransform = null`,
   because a stacked state is taller.
6. **Grid cells meet 44pt at 320dp:** the grid row drops the sheet's 16dp side padding (cells about 45.7dp wide,
   56dp tall). Recorded in DESIGN_SYSTEM §9 when built.
7. **Edges written down.** A tick on "Last night's Isha" writes to yesterday with `due` forced true; if yesterday is
   before `tracker_started`, the start moves back a day (9.11 re-derives it), which is intended. Undo after Pause is
   `resume()`, correct even after a joined pause (`TrackerLogic.normalise`).
8. **Strings.** `tracker_ring_a11y` is **new** (from DETAILED-PLAN 6.2), not changed. Plurals need Arabic zero, one,
   two, few, many, other; Urdu and Turkish one, other; Indonesian other (`TranslationIntegrityTest`).
   `TrackerWordingTest` widens its filter to include `nav_tracker` (today it reads only `tracker_*` and
   `settings_tracker*`). `action_undo` is checked by the same test once added to its list.
9. **The review card.** No flag: the calm timer (`HomeScreen.kt:136-153`) is cancelled when Times leaves the
   screen, so My prayers can never trigger the card. A two line comment there names the rule (SIGNOFF.md:52).
   `review/ReviewPrompt.kt` is no longer a step 1 file.
10. **Code facts for the builder.** `SettingsItemRow` and `PlainRow` are private in `SettingsList.kt:180, 210`; the
   switch card needs one made `internal`. `DesignRulesTest` does not count a `ModalBottomSheet`'s X, so the sheet's X
   is a review question. `LargeTextAndTalkBackDeviceTest` adds `nav_tracker` (Urdu "میری نمازیں" may wrap to two
   lines). `LocationSheet` (`Common.kt:130`) is public; it needs `onRefreshLocation` and `onSearchCity`, already in
   `MainScaffold`. The usage-count `when` maps PRAYERS to nothing (`Screen` stays three; `UsageCountsTest` pins it).
   The 38 percent ring for days to come is disabled, so it is exempt from contrast as a disabled control (WCAG 1.4.11),
   said in a `ColorContrastTest` comment.
11. **Pictures.** Picture 1's "after" needs real step 1 code (ticks out of Times, four tabs), so the prototype is the
   step 1 screen built on a throwaway branch, seeded with a fixed record. The set for `plan-verifier` and `reviewer`
   adds 1.5 text, No location and Last night's Isha.
12. **Fable's answers to the four UX points the corrections raised** (`ux-lead`, same night):
   - **Switching on crossfades** the off card to the checklist: `Standard` in, `Quick` out, no extra haptic (the
     Switch gives its own). A snap from a one row card to a five row card reads as a glitch. BUILD-INSTRUCTIONS §6
     gains row 12.
   - **Grid cells outside the 30 days are blank.** A day number with no ring already means "before tracking began",
     so one look must not carry two meanings.
   - **Before `tracker_started`: no ring and not selectable** (Q4 in practice: back fillable blanks are a missed
     prayer counter in another form). "Last night's Isha" moving the start back a day is right: it is a real prayer.
   - **`privacy.html:125` becomes** "Which of three screens you open: Times, Qibla or Settings. Opening My prayers
     is never counted." ("Counted" is the app's own word, and `TrackerPrivacyTest` already looks for "never
     counted".) Changed in the step 1 build commit, as the published promise must change with the code.
   - **No haptic on Pause, Resume or Undo**; the tick and the switch keep theirs. **Untick exits at `Quick`.**
   - With `FlowRow`, Pause stays first in reading order, so a stacked pair puts Pause on top. The `NARROW` fallback
     applies to all seven names at once, never mixed.
