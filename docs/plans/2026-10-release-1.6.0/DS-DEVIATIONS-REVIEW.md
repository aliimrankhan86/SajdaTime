# The 22 recorded design deviations, with pictures

**Recorded 25 Oct 2026** by DeepSeek at Ali's request: *"I want you to record them and put them in a
place … so Claude can check and give its verdict."*

This file is the picture evidence for OPEN item `ds-deviations` (`docs/DESIGN_SYSTEM.md` §11, listed
in `docs/design-system/03-components.md` §3.6). It **decides nothing and changes nothing**. Every
item below is still Ali's call, and the fixes belong to whoever he asks, not to this record.

> **Where the list came from.** Not from this record. The list is the 20 rows of
> `docs/design-system/03-components.md` §3.6 plus the two colour deviations recorded in
> `docs/design-system/08-surfaces.md` (the watch tile's own greens, the old launcher greens).
> Those last two have no id in the census, so this record gives them **3.21** and **3.22** for
> reference only. If Claude or Fable adds them to §3.6, the ids should move there and this file
> should follow, not the other way round.
> This file adds pictures, exact locations, and four things the census did not say (section 5).

---

## 1. How the pictures were taken

| | |
|---|---|
| Build | `d167846`, `:app:installDebug` (the same commit as `claude/1-6-0`) |
| Device | the `sajda` AVD, **1080 by 2400**, Android per the AVD, English, London |
| Method | `adb exec-out screencap`, screens driven by `uiautomator dump` taps, no test doubles |
| Light | system night mode off, `font_scale` 1.0 unless the file name says otherwise |
| Location | London, seeded by `ScreenSeedDeviceTest#seed_london_and_skip_the_first_run_screens`; coarse location permission revoked so the emulator's own Manchester fix does not move it |
| Clock | the real time of day. The emulator date could not be changed without rooting it, which is why the Ramadan card is taken from the existing store art instead |

**Not checked, and this record does not claim otherwise:**

- one screen size only. Not 320dp wide, not a tablet, not the watch, not a real phone.
- English only. Not Urdu or Arabic, both of which are right to left.
- normal text size only, apart from `03-times-1-5x-text.png`.
- no dark counterpart for most screens. Where dark matters (the card edge, the quiet notice) it is
  in `02-times-dark.png`.
- nothing here was measured. Corner radii, gaps and text sizes are quoted from the code, not
  counted from the pixels.

---

## 2. The pictures

All in `docs/plans/2026-10-release-1.6.0/ds-deviations/`.

| File | Shows | Items |
|---|---|---|
| `01-times-light.png` | Times, light: next prayer card, amber notice with its close X, Today list | 3.6, 3.15, 3.16 |
| `02-times-dark.png` | The same in dark | 3.15, 3.16 |
| `03-times-1-5x-text.png` | Times at `font_scale` 1.5 | 3.17 |
| `04-my-prayers-chosen-day.png` | My prayers: the chosen day card, and `titleSmall` on its date row | 3.1, 3.2 |
| `05-qibla.png` | Qibla: the compass and its 20 gutter | 3.10 |
| `06-islamic-dates-sheet.png` | Islamic dates sheet: no close X, `bodySmall` footnote | 3.1, 3.7 |
| `07-location-sheet.png` | Location sheet: no close X | 3.7 |
| `08-language-chooser.png` | Language chooser: the gap after each radio | 3.5 |
| `09-usage-question-from-settings.png` | The usage question opened from Settings | 3.8 |
| `10-usage-question-in-setup.png` | The same question during setup, both buttons fully visible | 3.8 |
| `11-onboarding-choice-card.png` | Setup: the school choice card | 3.3 |
| `12-onboarding-summary-card.png` | Setup: the "All set" summary card | 3.3 |
| `13-setup-red-error.png` | Setup: a failed town lookup | 3.20 |
| `14-export-sheet.png` | Save your timetable sheet | 3.18 |
| `15-reminder-dialog-rows.png` | Reminder before prayer dialog: the older switch rows | 3.4 |

Two more pictures are already in the repository and are not copied here:

- **Ramadan card** (item 3.11): `docs/store/screenshots/raw/phone/en-GB/04-ramadan-dark.png`
- **Watch tile** (item 3.21 below): `docs/store/screenshots/raw/wear/w3-tile.png`

---

## 3. Every item, located

Line numbers are from `d167846`. "Now" is what the code does; "system" is what
`docs/design-system/` says. **No opinion is offered in this column on purpose.**

### 3.1 Text below the 14sp floor, 13 uses

The three styles are used though `Type.kt` does not define them.

| Style | Size | Places |
|---|---|---|
| `bodySmall` | 12 | `IslamicDatesSheet.kt:145`, `RamadanTimetableSheet.kt:219`, `RamadanTimetableSheet.kt:225`, `TimetableSheet.kt:157`, `TimetableSheet.kt:185`, `SettingsScreen.kt:1840`, `SettingsScreen.kt:1882`, `SettingsScreen.kt:1974` |
| `labelSmall` | 11 | `RamadanTimetableSheet.kt:201`, `TimetableSheet.kt:267` |
| `titleSmall` | 14 | `UsageCountsConsent.kt:71`, `UsageCountsConsent.kt:74`, `MyPrayers.kt:202` |

System: only the ten styles `Type.kt` defines, floor 14.
Picture: `04` (the date row), `06` (the footnote). The other eight are Settings and timetable text,
visible only where those screens are reached, and were not photographed.

### 3.2 Chosen day card radius

`MyPrayers.kt:199` — `sajdaSurface(RoundedCornerShape(16.dp))`. System: cards are 24.
Picture: `04`.

### 3.3 Setup choice and summary cards

`OnboardingScreen.kt:766` (summary) and `OnboardingScreen.kt:888` (`ChoiceCard`) use Material
`Card` with `surfaceVariant`, radius 12, not `sajdaSurface` at 24.
Pictures: `11`, `12`. The pale green card with the tighter corner in `12` is the one.

### 3.4 Older rows inside dialogs

`SettingsScreen.kt:1735` `SwitchRow` and `SettingsScreen.kt:1682` `SettingRow`, used from dialogs at
`SettingsScreen.kt:1300`, `:1342`, `:1436` and `:868`. 56 tall, no icon tile, no check thumb.
System: the Settings row form (icon tile, green check thumb, 64).
Picture: `15`. Compare the Fajr to Maghrib rows in the dialog with the rows behind them.

### 3.5 Language chooser gap

`LanguageChooser.kt:82` — `Spacer(Modifier.width(12.dp))` after the radio. System: 8.
Picture: `08`.

### 3.6 Close X placement

`Notice.kt:174` draws its X at `offset(y = (-12).dp)`; the shared `DialogTitle` at
`Common.kt:69` draws its X at `offset(y = (-8).dp)`. System: use the component, do not place an X
by hand. Pictures: `01` (a `Notice` X) against `08` or `14` (a `DialogTitle` X).

### 3.7 Sheets with a title and no close X

`IslamicDatesSheet.kt` contains no `IconButton` at all. The shared location sheet
(`Common.kt:166`, `ModalBottomSheet`) has none either. System: sheets with a title carry the X.
Pictures: `06`, `07`.

### 3.8 The usage question

`UsageCountsConsent.kt:146` — an `AlertDialog` with `onDismissRequest` and no X, so tapping outside
counts as No. System: dismissable dialogs carry the X. Pictures: `09`, `10`.
**This one touches the consent flow, so it is not only a visual change.**

### 3.9 Icon size in buttons

Mosque button `HomeScreen.kt:733` at 20. Two full width buttons draw an 18 icon
(`MyPrayersScreen.kt:401`, `:433`, and `HomeScreen.kt:1016`). System: 24 in a full width button,
18 compact. Picture: `04` shows the two full width buttons. The mosque button was not photographed
because no mosque is configured on the emulator.

### 3.10 Qibla gutter

`QiblaScreen.kt:135` — `padding(horizontal = 20.dp)`. System: 16. Picture: `05`.

### 3.11 Ramadan label tracking

`RamadanCard.kt:145` — `letterSpacing = 1.6.sp`. System: no extra tracking.
Picture: the store art named in section 2 (the "Ramadan" label).

### 3.12 Missing heading semantics

`SectionHeading` in `Common.kt` has no `heading()`; several other headings do
(`SettingsList.kt:134`, `HomeScreen.kt:639`, `:831`, `:1027`, `RamadanCard.kt:149`,
`IslamicDatesSheet.kt:77`, `TimetableSheet.kt:227`, `SettingsScreen.kt:1639`).
System: every visual heading is a heading.
**Cannot be photographed.** It is a screen reader change with no visual difference.

### 3.13 Motion timings

`MainScaffold.kt:270` and the setup slide use library default timings rather than the `Motion`
tokens. System: the tokens. **Cannot be photographed**: a still frame cannot show a duration.

### 3.15 Possible double 1dp line where gloss meets the card edge in dark

Inferred from code only, never measured. Picture: `02`.
**Nobody has confirmed it on a screen**, including this record: at 1080 by 2400 I could not tell.

### 3.16 Quiet notice buttons in dark

Recorded at about 2.32:1 against the 3:1 shape contrast the system asks for. Picture: `02` (the
amber notice). Not measured here; the figure is the census's, not this record's.

### 3.17 Tab bar at 1.5x text and at 320dp 2.0x

Picture: `03`. "My prayers" wraps onto two lines and the third notice is cut off behind the bar.
The 320dp case was not tested: only one screen size was available.

### 3.18 Save your timetable sheet

`HomeScreen.kt:816`. Opens about half height, with a divider after the last option.
System: full height. Picture: `14`.

### 3.19 `PermissionButton(amber = true)`

Never called: a search of `app/src/main/java` for `amber = true` returns nothing.
System: do not use without a reason. Nothing on screen to photograph.

### 3.20 The setup error in red

`OnboardingScreen.kt:503` paints the message with `colorScheme.error`. It is the only red text in
the app. System: an amber notice or `onSurfaceVariant`, no red. Picture: `13`.

### 3.21 The watch tile's own greens

`wear/.../NextPrayerTileService.kt:225` — `COLOUR_PRIMARY = 0xFF7FD1AE`, plus two greys not in the
palette. System: the watch equals the phone's dark roles except its black background. Picture: the
store art named in section 2.

### 3.22 The old launcher greens

| File | Fill | What the system says |
|---|---|---|
| `app/src/main/res/mipmap/ic_launcher.xml:8` | `#14624B` | the primary token, `#0B5A43` |
| `app/src/main/res/mipmap/ic_launcher_round.xml:8` | `#14624B` | the same |
| `wear/src/main/res/mipmap/ic_launcher.xml:8` | `#14624B` | the same |
| `app/src/main/res/drawable/ic_launcher_background.xml` | `#0E6B4F` | the same |

`app/src/main/java/com/sajdatime/app/ui/theme/Color.kt:42` — `LightPrimary = Color(0xFF0B5A43)`.
So the icon background is one revision behind the token and the two phone mipmaps and the watch
mipmap are two revisions behind. Its comment says `#0E6B4F` "is LightPrimary", which is no longer
true, so a reader of that file is told something false.

**No picture**: a launcher icon's colour is a fact about four files, not something a screenshot of
the running app can show. The Android 7 fallback icon does not render on a current emulator.

---

## 4. What cannot be photographed at all

| Item | Why |
|---|---|
| 3.12 heading semantics | a screen reader change with no visual difference |
| 3.13 motion timings | a duration, not a frame |
| 3.19 the unused amber button | it is never on screen |

---

## 5. Four things the pictures showed that the census did not record

Stated as observations. None of these is a decision, and none was fixed.

1. **The usage question's second button is clipped when it is opened from Settings, and not when
   it is opened during setup.** `09` was taken from the Settings row; the "No thanks" button is cut
   off by the bottom of the dialog. `10` is the same question in setup, where both buttons are fully
   visible. So this is specific to one entry point, which narrows it considerably. It may be that
   the Settings dialog scrolls; that was not tested.
2. **The setup error is red in more than the text.** `13` shows the field outline and the floating
   label turning red as well as the message. The census says "the setup location error in red",
   which reads as one line of text.
3. **The old icon green is two greens, not one** (section 3.22), and the comment beside one of them
   is now false.
4. **At 1.5x text, a notice is cut off behind the tab bar** (`03`). The census records the tab bar
   wrapping "My prayers" and Times and Qibla colliding. Whether the cut notice is the same defect or
   a different one is not established.

---

## 6. What is being asked of Claude

1. **A verdict on each item**: is it a real deviation from the design system, and does it need
   fixing at all? Several may be "recorded, no change wanted", which is already what the census says
   of 3.14, 3.16, 3.17 and 3.19.
2. **Its own reading of section 5**, especially the clipped consent button (1) and the cut notice
   (4), which may be two new items or neither.
3. **A check of this record's facts**, not just its conclusions: the line numbers, the 13 text uses,
   and the four icon files.
4. **An order to do the work in**, if anything is to be done, and which items are Claude's own work
   rather than Ali's decision.

**Nothing in this record is a recommendation, and nothing was changed to produce it.** The work was
a build, an emulator, `adb` and reading. No file under `app/` or `wear/` was touched.
