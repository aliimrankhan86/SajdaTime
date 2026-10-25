# 3. Components

Every reusable building block in the phone app, release 1.6. For each: what it is for, its anatomy,
metrics, states, accessibility, right to left behaviour and its iPhone equivalent. Use these before
inventing anything. Every button in the app, with its kind, height and width, is listed in
`docs/DESIGN_SYSTEM.md` §4.1; that list is enforced by `DesignRulesTest`, so it is not repeated here.

Paths are under `app/src/main/java/com/sajdatime/app/ui/`. Values not set in our code and left at the
Material 3 default are marked *(M3 default)*.

---

## 3.0 The foundation helpers (`theme/Theme.kt`)

| Helper | Does | Rule |
|---|---|---|
| `Modifier.sajdaSurface(shape, colour or brush)` | The one card treatment: light 2dp shadow, no border; dark 1dp `outlineVariant` hairline, no shadow | Every bounded surface uses it. Never hand roll a shadow or border |
| `heroStyle()` | The next prayer card's brush, three inks, sheen, rim, glow | Only the next prayer card |
| `warningGloss()` | The warning notice's amber gradient, sheen, rim | Only warning notices |
| `skyStyle(period)`, `skyColours()` | The four skies | Only the top of Times (`home/Sky.kt`) |
| `kiswahGold()` | The Kaaba gold | Only the Kaaba detail |
| `dialBezel()` | Qibla bezel: `surface` light, `surfaceContainerHighest` dark | Only the Qibla dial |
| `LocalDarkTheme` | Which theme is in force | Read this, never `isSystemInDarkTheme()` |

The gloss stack, where used: `sajdaSurface(shape, brush)`, then a sheen from the top fading out by
half height, then a 1dp rim border from `rim` to `rimBottom`.

---

## 3.1 Actions

### `CtaButton` (`components/CtaButton.kt`)

The only action button. Solid fill, white bold label, both themes.

| Property | Value |
|---|---|
| Fill | `brand.cta`: light `#0B5A43`, dark `#17805A` |
| Label | `brand.onCta` white, `actionLabelStyle()` (16 SemiBold, centred) |
| Shape | Full pill *(M3 default)*. Never a custom radius |
| Height | Standard 52 minimum; **main action of a setup step or a must answer question 56**; compact 40 drawn, 48 touch |
| Padding | 24 sides *(M3 default)*; compact 16 × 8 |
| Width | Exactly one of: `fillMaxWidth()` (alone on its line), `weight(1f)` (shares a row), `compact = true` (wraps, beside a heading or under a picture). `DesignRulesTest` fails a button that states none |
| Icon | Optional, **leading**, then 8 (`Space.Related`), then the label. 24 in a full width button, 18 in a compact one |
| Elevation | None |
| Pressed | Ripple *(M3 default)* |
| Disabled | 12% `onSurface` fill, 38% label *(M3 default)*, **only while the action runs** (finding your location). Otherwise hide a button that cannot be used yet ("Find place" appears once text is typed) |
| Accessibility | Role button *(M3)*; at least 48 to touch |
| RTL | Icon and label mirror automatically |
| iPhone | `Button` with `.buttonStyle(.borderedProminent)`, `.controlSize(.large)`, `.buttonBorderShape(.capsule)`, tint `cta`, label `.font(actionLabel)`. Compact: `.controlSize(.regular)` |

Helpers: `ctaButtonColors()` for the few Material `Button`s inside `Notice` and `PermissionButton`;
`ctaIconButtonColors()` for the stepper's solid icon buttons.

Hierarchy comes from order, position and wording, never from fading. Main action first (top of a
column, start of a row), except setup's Back and Continue where Back is first. Labels start with a
verb, two to five words, no full stop, never "Tap" or "Click". A question may label a button that
opens an explanation ("Different from your mosque?").

### Amber button (inside `Notice` only)

Material `Button` with `tertiary` fill and `onTertiary` label: light white on `#9A6208`, dark
`#3A2A00` on `#E8B14A`. 48 tall, wraps. Only in a warning notice. `PermissionButton(amber = true)`
exists but is never called.

### Stepper (`StepperRow`, in `settings/SettingsScreen.kt`)

Label (`bodyLarge`, fills), then − (`FilledIconButton`, `ctaIconButtonColors()`, `Icons.Outlined.Remove`),
the value (`bodyLarge`, 64 wide, centred, polite live region "Dhuhr, +2 min"), then +
(`Icons.Outlined.Add`). Buttons 40 drawn, 48 touch, circular. Spoken "Decrease Dhuhr" and "Increase
Dhuhr". Used for minutes (±30), the date shift (±2), jama'ah minutes (0 to 60 in fives) and fasts (0
to 999). iPhone: `Stepper` is allowed, but to match, two filled circular buttons around a value.

### Close X (`DialogTitle` in `components/Common.kt`, and in `Notice`)

`IconButton` with `Icons.Outlined.Close`, ink of its surface, 24 icon, 48 target, top end, offset 12
outward so it sits in the corner. Spoken "Close". **Always alone**: never beside another icon button.
iPhone: `xmark` in the top trailing toolbar position of a sheet.

### Text link (`TextButton`)

Only "Close" or "Got it" at a dialog's foot, `primary`, `labelLarge` (SemiBold). Nothing else.

### Bold information link (`InfoLink`, private to `components/UsageCountsConsent.kt`)

`primary`, `actionLabelStyle()` underlined, start aligned, 48 target, role button. **Only** "See
exactly what is sent" and "Read the privacy policy" on the usage question. The owner's one exception.

### `PermissionButton` (`components/PermissionAction.kt`)

Not granted: a 52 solid button with the label. Granted: not a button; a row with a check, "Allow exact
alarms · Allowed" in `bodyLarge`, state "Allowed". Used once, on setup's last step.

---

## 3.2 Toggles and choices

### `Tick` (`components/Tick.kt`)

The prayed / not ticked checkbox on My prayers and in Last 30 days.

| Property | Value |
|---|---|
| Size | 24 drawn circle in a 48 round target |
| Stroke | 2 |
| States | Ticked: filled `primary`, `onPrimary` tick drawn in. Not ticked: `outline` ring (never red). Not due yet: ring at 38%, disabled. Paused: ring plus two pause bars, disabled |
| Motion | Fill and tick in `Motion.standard()`, out `Motion.quick()`; nothing on open |
| Haptic | `ToggleOn` / `ToggleOff` |
| Accessibility | Role checkbox, spoken "Fajr, prayed" / "not ticked" / "not due yet" / "paused" |
| iPhone | Custom circle toggle; SF `circle` and `checkmark.circle.fill`; `.sensoryFeedback(.selection)` |

### `Bell` (`components/Bell.kt`)

One tap alert toggle at the end of each prayer row on Times (not Sunrise, which keeps a 48 blank so
times align).

| Property | Value |
|---|---|
| Size | 24 icon in a 48 round target, role switch |
| On | `Icons.Rounded.Notifications` (or `Icons.Rounded.Alarm` when the style is alarm), `primary`, or `onPrimaryContainer` on the Next row |
| Off | `Icons.Outlined.NotificationsOff`, `outline` |
| Blocked by the phone | The stored icon in `outline`; spoken with the reason |
| Motion | Cross fade `Motion.quick()`; Undo snaps with no fade |
| Haptic | `ToggleOn` / `ToggleOff` |
| Behaviour | Off shows the snackbar with Undo; also removes that prayer's reminder and gives it back on |
| iPhone | `bell.fill`, `alarm.fill`, `bell.slash` |

### Switch rows

Material `Switch`. In Settings (`PlainRow` in `settings/SettingsList.kt`) the whole row toggles (role
switch), haptics on, and the thumb shows a check when on. **Known deviation:** the older `SwitchRow`
inside dialogs has no check thumb and no haptic. New work uses the Settings form. iPhone: `Toggle`.

### `RadioRow` (`components/MethodChoices.kt`)

Whole row selectable (role radio button) in a `selectableGroup`, minimum 56, vertical padding 8:
`RadioButton`, 8, label `bodyLarge` with an optional `bodyMedium` `onSurfaceVariant` line.
`MethodChoiceList` builds the calculation method list from it. **Known deviation:** `LanguageRow` in
`settings/LanguageChooser.kt` uses a 12 gap. iPhone: a `List` row with a trailing `checkmark`
(Apple's pattern; radio circles are not native).

### Choice chips (`ChoiceChip`, in `settings/SettingsScreen.kt`)

Pill, 1 border (`primary` selected, `outline` not), `secondaryContainer` fill when selected, padding
14 × 8, `bodyMedium`, 48 target, role radio button, in a `FlowRow` with 8 gaps. Used for Off /
Notification / Alarm and the suhoor style and sound. iPhone: segmented `Picker` when three or fewer
short options, else chips.

### Theme selector (`ThemeSelector`, in `settings/SettingsList.kt`)

Three way sliding control: track `surfaceContainerHigh`, radius 28, inset 4; thumb `sajdaSurface` at
radius 24 sliding with `Motion.standard()`; options 48 minimum, `labelLarge` (SemiBold selected,
Medium not), `WholeWords`. Order: Follow phone, Light, Dark. Role radio button; `SegmentTick` haptic.
iPhone: segmented `Picker`.

---

## 3.3 Surfaces

### Card

`sajdaSurface(RoundedCornerShape(24.dp))`, padding 16 (or 20 × 16 for the hero and Ramadan cards).
Lists inside a card divide rows with 1dp `outlineVariant` hairlines. iPhone: a rounded rectangle at
24pt (`.clipShape(.rect(cornerRadius: 24, style: .continuous))`) with the same light shadow and dark
hairline, or an inset grouped `List` section where the content is a list.

### Next prayer card (`NextPrayerCard` in `home/HomeScreen.kt`)

The hero. Radius 24, `heroStyle()` gloss, light glow shadow 12, padding 20 × 16, centred:

1. "Next prayer" label, `labelLarge` at 500, hero label ink
2. 8
3. Prayer name and time on one line, `headlineMedium`, 10 apart; name in the prominent ink (heading),
   time in the secondary ink
4. 8
5. Countdown, `displayLarge`, prominent ink, digits hidden from screen readers and replaced by "Asr in
   2h 14m"
6. 4
7. "until it begins", `labelMedium`, secondary ink

After Isha the label reads "Next prayer · Tomorrow". With no location: one `titleMedium` line.

### Today list (`TodayTimeline` and `PrayerRow` in `home/HomeScreen.kt`)

One card of six rows, minimum 50, padding start 18 end 6. Row: slot icon (21, Sunrise 18), 14, name
(`bodyLarge`), optional note under it (`labelMedium`: jama'ah, Jumu'ah, Suhoor ends, Iftar), then
pills and the time (`PrayerTimeTextStyle`), then the bell.

| Row state | Treatment |
|---|---|
| Next | `primaryContainer` fill, 3dp `primary` bar on the leading edge, filled "Next" pill (`primary` / `onPrimary`) |
| Now | `surfaceContainerHigh` fill, tonal "Now" pill (`secondaryContainer` / `onSecondaryContainer`), no bar |
| Sunrise | `surfaceContainerLow`, `onSurfaceVariant`, one step smaller. It is not a prayer |
| Plain | Transparent |

Three signals for Next (fill, bar, pill), never colour alone. Next and Now never share a row. The fill
moves with `Motion.standard()` when a prayer passes. From 1.5× text the pills and time move under
the name. Each row is one screen reader stop ("Fajr, Next, 5:37 am"); the bell is its own.

### Pills

`labelMedium`, padding 9 × 3, fully rounded. Next and Today: `primary` / `onPrimary`. Now:
`secondaryContainer` / `onSecondaryContainer`. A pill marks state; it is never tappable.

### `Notice` (`components/Notice.kt`)

Every message that is not a setting. One shape, two tones.

| Tone | Panel | Ink | Mark | Buttons | Use |
|---|---|---|---|---|---|
| Warning | `warningGloss()` glossy amber | `onTertiaryContainer` | `tertiary` | Amber | Something is wrong or withheld: permissions, Makkah fallback, approximate times, out of window jama'ah, compass accuracy |
| Quiet | Flat `secondaryContainer` | `onSecondaryContainer` | same | Solid green | Information or an occasion: Ramadan, Eid, a rule the user set (jama'ah in another time zone), a read only record |

Anatomy (radius 24, padding 16):

1. A bare 24 mark (default `Icons.Rounded.WarningAmber`), top aligned, no tile behind it; 12 gap
2. Optional title, `titleMedium`, heading; 4
3. One brief sentence, `bodyMedium`
4. Optional footnote that must stay visible (Eid's "follow your mosque"), 8 above
5. Optional details, folded, 12 above
6. 16, then one row of buttons indented under the text (36): the action, solid, with a trailing
   auto mirrored arrow ("Allow", "See why", "Set location", "Shift the date"); and "Details" with a
   down chevron that turns (`Motion.quick()`), spoken expanded or collapsed. Buttons 48 tall, 8 apart
7. Close X alone at the top end, **on Times only**, and it stays closed. In Settings the same notice
   has no X and stays until fixed

The card is never tappable itself. Permission cards (`PermissionCard` in `components/PermissionAction.kt`)
carry one button, "Allow", and no Details; they disappear when granted and leave Times with a quick
fade and shrink. iPhone: a custom card (no system equivalent); SF `exclamationmark.triangle` and
`bell.slash` for marks.

### Settings group and rows (`settings/SettingsList.kt`)

- Section title: `labelLarge` at 500, `onSurfaceVariant`, sentence case, 32 start, 24 above, 8 below,
  heading.
- Group: one `sajdaSurface` card, radius 24, 16 side inset. Dividers inset to the text (66).
- Row (`SettingsItemRow`, `PlainRow`): minimum 64, padding 16 × 12. Leading 36 tile, radius 10,
  `secondaryContainer`, with a 20 `onSecondaryContainer` outlined icon; 14 gap; title `titleMedium`
  over a `bodyMedium` `onSurfaceVariant` subtitle (the current value), 4 apart; trailing: a 14
  auto mirrored chevron in `outline` (opens something), a switch (toggles), or nothing (information).
- Fold row (`FoldRow`): minimum 60; a turning chevron; the body folds open in place, inset 66.
- iPhone: an inset grouped `List` with `Section` headers, `Label` with a tinted SF Symbol tile,
  `NavigationLink` for chevron rows, `Toggle` rows, `DisclosureGroup` for folds.

**Known deviation:** the older `SettingRow` (56, `bodyLarge`, bare 22 icon) is still used inside the
Jama'ah times dialog. New rows use the Settings form.

### Week ring and history ring (`DayRing` in `components/DayRing.kt`)

One day's share of the five prayers. Track 3dp `outlineVariant`; `primary` arc from 12 o'clock,
clockwise in both directions (deliberate: it is a clock face); 5 of 5 fills and draws a tick; paused
shows bars on an `outline` track; future at 38%; before tracking began, no ring. Today or the chosen
day gets a 2dp `outline` halo. 36 on the week card (32 when seven do not fit), 32 in the history grid.
The arc grows with `Motion.draw()` when a tick changes it. Spoken by the caller ("Monday, 3 of 5
prayed").

### Sky mark and backdrop (`home/Sky.kt`)

The backdrop is the period's sky behind the header and the next prayer card, with a status bar strip
in the sky's top colour. The mark, 104 × 36 at the end of the city row: an arc (track 1.5
`onSurfaceVariant`, passed part 2.5 `primary`), five dots for the five prayers, and an 18 glyph in
`primary` riding the arc (Dawn uses `widget_glyph_fajr`, Day `sky_glyph_day`, Evening
`widget_glyph_maghrib`, Night `widget_glyph_isha`). Not actionable; spoken "Dawn, until 7:40 am".
Hidden when the city name needs the room or at 2.0× text. Draws in once per app open; a sky change
cross fades with `Motion.draw()`.

### Qibla dial (`CompassDial` in `qibla/QiblaScreen.kt`)

A lifted disc (`sajdaSurface(CircleShape)`), at most 320 wide and 36% of the screen height. Bezel
0.08 of the radius in `dialBezel()`; face `surfaceVariant`. Sixty ticks every 6° in `outline`; N E S
W letters at 0.74 of the radius, upright (N `primary` Medium, others `onSurfaceVariant`). The needle
is **you**: a `primary` teardrop fixed pointing up. The Kaaba mark (`ic_kaaba` in `onSurface`, its
band and door `ic_kaaba_detail` in `kiswahGold()`) at 0.70 of the radius, 0.22 across, always upright.
Within ±5°: face `primaryContainer`, bezel `primary` at 0.11, the sentence turns green with a tick, one
70ms buzz. Arrive at 5°, leave at 8°. Decorative to screen readers (the sentence above carries it). The
dial turns with the compass over 120ms, the one written duration outside `Motion`. RTL: never mirrors.

---

## 3.4 Containers

### Dialogs

Material `AlertDialog`: radius 28, `surfaceContainerHigh`, 24 inner padding, title `headlineSmall`
*(M3 defaults)*. Title through `DialogTitle` (title plus the X). Scrollable body. Foot: "Close" or
"Got it". Chooser dialogs (`ChooserDialog` in `settings/SettingsScreen.kt`) save each tap at once and
stay open (except the method list, which closes on a pick). Confirmation dialogs (Pause, Delete, the
time dial) put one full width solid button in the foot instead of the text link, plus the X.

**Must answer** dialogs have no X and no foot link: only the usage question, two equal 56 buttons.
iPhone: `.sheet` with a `NavigationStack` and an `xmark` close, or `.alert` for a two option question.

Explainer dialogs pair each paragraph with its button (`ExplainerParagraph`, `ExplainerAction` in
`home/HomeScreen.kt`): paragraph to its button 8, one pair to the next 16, buttons full width 52.

### Bottom sheets

`ModalBottomSheet`, top radius 28 *(M3 default)*, opened fully (`skipPartiallyExpanded = true`).
Header through `TimetableHeader` or `DialogTitle` at 24 side padding: title `headlineSmall` with the X,
then 4, then the subtitle line, then 16. Content lists at 16 side padding. Swipe down, scrim and Back
close them. iPhone: `.sheet` with `.presentationDetents([.large])`.

**Known deviations:** the Islamic dates sheet and the location sheet have no X; the export sheet opens
half height and draws a divider after its last option. New sheets copy the Timetable sheet.

### Glass tab bar (`GlassTabBar` in `MainScaffold.kt`)

A floating pill, 16 side and 12 vertical margin, minimum 64, radius 32, `surface` at 97%, 1dp
`outlineVariant` at 70% in both themes, light 10dp shadow. Four tabs, equal width, role tab in a
`selectableGroup`. Each: icon 24 inside an indicator pill (20 × 4 padding), 4, label `labelMedium`
(`WholeWords`). Selected has three signals: Rounded icon in `onSecondaryContainer`, `secondaryContainer`
pill, SemiBold `onSurface` label. Unselected: Outlined icon and Medium label in `onSurfaceVariant`.
`SegmentTick` on change. Its measured height is `LocalBottomBarClearance`. iPhone: the system
`TabView`; do not rebuild a custom bar.

### Snackbar

Material snackbar *(M3 default: radius 4, `inverseSurface`, action `inversePrimary`)*, above the tab
bar, on every tab, short duration. Only for an action that can be undone: "Paused from today" and the
four bell off messages, each with "Undo". Saved files use Android toasts. iPhone: there is no system
snackbar, so build a small overlay above the tab bar with the message and "Undo". Do not rely on
shake to undo: nobody finds it.

---

## 3.5 Text and layout helpers

| Helper | File | Does |
|---|---|---|
| `ScreenTitle` | `components/ScreenFrame.kt` | `headlineMedium`, 20 start and end, 12 above, 4 below, heading |
| `BottomBarClearance()` | `components/ScreenFrame.kt` | The bar's measured height (fallback 88). Last child of every scrolling tab |
| `Modifier.readingWidth()` | `components/ScreenFrame.kt` | Caps content at 600 |
| `WholeWords` | `components/WholeWords.kt` | Auto size that shrinks only while a break falls inside a word, to 60% at least. iPhone: `.minimumScaleFactor(0.6)` with `.lineLimit` |
| `SectionHeading` | `components/Common.kt` | `titleMedium` in `onSurface`, used in setup (missing heading semantics: known gap) |
| `ProgressRow` | `components/Common.kt` | 18 spinner (stroke 2), 12, `bodyMedium` `onSurfaceVariant` ("Finding your location…") |
| `LocationSheet` | `components/Common.kt` | The shared location sheet, see `05-screens-and-features.md` |
| `MethodChoiceList` | `components/MethodChoices.kt` | The method radio list used in setup and Settings |
| `UsageCountsConsentBody` | `components/UsageCountsConsent.kt` | The usage question, used in setup, Settings and once for existing users |

---

## 3.6 Known deviations (do not copy, do not "fix" without the owner)

Found by the 25 Oct 2026 census. Each is a visible change, so fixing it is the owner's decision
(`docs/DESIGN_SYSTEM.md` §0.2); the list is raised with him as OPEN item `ds-deviations` in
`docs/DESIGN_SYSTEM.md` §11. New work follows the rule column.

**Pictures, exact file and line for every row, and four observations the census did not record:**
[`docs/plans/2026-10-release-1.6.0/DS-DEVIATIONS-REVIEW.md`](../plans/2026-10-release-1.6.0/DS-DEVIATIONS-REVIEW.md)
(Ali's request, 25 Oct 2026, taken on the emulator at `d167846`). Evidence only, no decisions.

| Id | Where | Deviation | Rule for new work |
|---|---|---|---|
| 3.1 | 13 uses, listed in `typography.json` | `bodySmall` 12, `labelSmall` 11, `titleSmall` 14 used but not defined in `Type.kt` | Only the ten defined styles; 14 floor |
| 3.2 | `home/MyPrayers.kt` chosen day | Card radius 16 | Cards are 24 |
| 3.3 | `onboarding/OnboardingScreen.kt` choice and summary cards | Material `Card`, radius 12, `surfaceVariant`, no `sajdaSurface` | `sajdaSurface` at 24 |
| 3.4 | Dialog switch and link rows (`SettingRow`, `SwitchRow`) | Older 56 rows, no tile, no check thumb, no haptic | Settings row form |
| 3.5 | `settings/LanguageChooser.kt` | 12 gap after the radio | 8 (`RadioRow`) |
| 3.6 | `Notice` vs `DialogTitle` | Close X offset y −12 vs −8 | Use the component; do not hand place an X |
| 3.7 | Islamic dates and location sheets | No close X | Sheets with a title carry the X |
| 3.8 | Settings, usage question dialog | Dismissable by tapping outside (counts as No), no X | The rule says dismissable dialogs carry the X; owner to decide |
| 3.9 | Mosque button 20, two full width buttons 18 | Icon size in full width buttons | 24 full width, 18 compact |
| 3.10 | `qibla/QiblaScreen.kt` | 20 screen gutter | 16 |
| 3.11 | `home/RamadanCard.kt` label | Letter spacing 1.6 left over from before Quiet Glass | No extra tracking |
| 3.12 | Setup step titles, `SectionHeading`, location sheet sub heading | Missing heading semantics | Every visual heading is a heading |
| 3.13 | Tab cross fade, setup slide, tab pill colour | Library default timings, not `Motion` | `Motion` tokens |
| 3.14 | `Notice` footnote | Same style as the brief | Fine as is; spacing sets it apart |
| 3.15 | Next prayer card and warning notice in dark | The rim draws over `sajdaSurface`'s hairline (two 1dp lines, inferred) | Check on screen before copying the gloss stack |
| 3.16 | Quiet notice buttons in dark | Green button on `secondaryContainer` about 2.32:1, below the 3:1 shape contrast (HANDOVER §11) | Do not add the quiet gloss; owner aware |
| 3.17 | Tab bar at 1.5× and at 320dp 2.0× | "My prayers" wraps; "Times" and "Qibla" collide (HANDOVER §11) | Known, recorded |
| 3.18 | Export sheet | Opens half height, divider after the last option | Full height |
| 3.19 | `PermissionButton(amber = true)` | Never used | Do not use without a reason |
| 3.20 | `onboarding/OnboardingScreen.kt` location error | Text in the `error` role (red), the only use | Amber notice or `onSurfaceVariant` text; no red |
