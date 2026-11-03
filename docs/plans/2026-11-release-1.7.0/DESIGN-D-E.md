# Design D and E for the next release (Fable, ux-lead, 3 Nov 2026)

Read and design only. Nothing in `app/`, `core/`, `docs/` or `.claude/` was changed. Mockups: `mockups/D/` and
`mockups/E/` (HTML and PNG, light and dark, from `mockups/gen_mockups.py`, which reads
`docs/design-system/tokens/color.json`). Every PNG was opened and checked: nothing cut off, nothing overlapping.
The mockups render the Mac's system sans, not Roboto, and 1 dp as 1 px at 411 wide; judge layout and weight, not
letterforms. Ali decides everything visual from the pictures (DESIGN_SYSTEM §0.2); nothing here is built.

What Ali said (3 Nov 2026): "I dont like our display of set location and alert message it can be done nicely
without adding a big banner which doesnt look great." And for the widget: "1 row style widget ... which can come in
1 row or little smaller than that with icons and prayer name and time etc."

---

## D. The three prompts without the big banner

### D.1 What is there today (verified in code)

`Notice.kt` draws every notice: 24 radius card, warning gloss (amber gradient, sheen, rim), 16 padding, 24 mark,
title + brief, then a 48 tall solid amber button with an arrow, optional X alone at the top end. The three prompts in
scope, and what each must keep doing:

| Prompt | Where | Today | Must keep |
|---|---|---|---|
| Times for Makkah | `HomeScreen.kt` `DefaultLocationBanner`, first notice slot | title, brief "Your location is not set yet, so these times are for Makkah.", button Set location, not closable | says the times are not the user's; opens the location sheet; never closable (it is true every day) |
| Allow exact alarms | `HomeScreen.kt` `ExactAlarmBanner` via `PermissionCard` | title, brief "Prayer alerts can arrive an hour late, and Fajr may not arrive at all.", button Allow, X (stays closed; Settings keeps it) | the Fajr sentence stays visible before the user acts; one tap to the system screen; closable on Times only |
| Set your location (Qibla) | `QiblaScreen.kt` `QiblaBody`, in place of the dial | brief only, button Set location, nothing else on the screen | opens the location sheet; also covers the Makkah fallback (bearing 0, STEP-6 §14) |

The screen already says "Makkah" in 26sp at the top with the green caret, and the hero card shows Makkah's Fajr.
The first banner repeats that twice more. That is the waste Ali is seeing: three cards of amber in a row, two of them
saying what the header says.

### D.2 Every other use of `Notice` (listed so the pattern stays consistent; all out of scope)

| File:line | Notice | Tone | Buttons |
|---|---|---|---|
| `HomeScreen.kt:732` | Mosque difference, loud form above 45° latitude | warning | See why, X |
| `HomeScreen.kt:1003` | Polar latitudes, approximate times | warning | Details only, no X (true every day) |
| `HomeScreen.kt:1041` | Notifications blocked (`PermissionCard`, shares the exact alarms code) | warning | Allow, X |
| `RamadanCard.kt:368` | Ramadan is near | quiet | Shift the date, X |
| `EidCard.kt:76` | Eid Mubarak with the "follow your mosque" footnote | quiet | none |
| `MyPrayers.kt:118`, `MyPrayersScreen.kt:206` | Record from a newer version | quiet | none |
| `MyPrayersScreen.kt:198` | No location on My prayers | warning | Set location |
| `QiblaScreen.kt:216`, `:836` | No compass; compass accuracy | warning | none |
| `SettingsScreen.kt:238`, `:247` | Exact alarms and notifications, permanent (`PermissionCard`) | warning | Allow, no X |
| `SettingsScreen.kt:888`, `:940` | Jama'ah time outside its window; other time zone | warning; quiet | Details |
| `PermissionAction.kt:130` | `PermissionCard` itself, the wrapper the two permission prompts use | warning | Allow, X |

Rule proposed so nothing drifts: **a "set this up once" prompt is an inline row; an occasion or an explanation stays a
card.** Inline: Makkah fallback (Times), exact alarms (Times), notifications blocked (Times, it shares the code so it
changes with exact alarms unless the build keeps a parameter), no location (Qibla, My prayers). Card, unchanged:
Ramadan, Eid, polar, mosque difference, compass, jama'ah, and everything in Settings (Settings never hides a problem,
and a card there is the only thing on the screen that is not a row). If Ali likes the row he can later ask for the
card notices to follow; that is a separate decision, not part of D.

### D.3 The three options

**Option A, quiet rows (recommended).** `mockups/D/A-times-light.png`, `A-times-dark.png`, `A-times-light-2x.png`,
`A-times-light-urdu.png`, `A-qibla-light.png`, `A-qibla-dark.png`.

The same `Notice` anatomy with the card taken away: no fill, no gloss, no border, no 16 padding box. It sits on the
page in the same notice slot (16 under the hero card), and reads as part of the page, not a thing pasted on it.
Anatomy, start to end:

1. the bare mark, `WarningAmber` (SF `exclamationmark.triangle`), 24, in `accent` (light 4.68:1 on the page, dark
   9.63:1, measured);
2. the text column: title `titleMedium` in `onTertiaryContainer` (light 8.53:1 on the page, dark 14.87:1), brief
   `bodyMedium` 4 under it (`Space.Tight`, they belong together), the same ink;
3. the action 8 under the text (`Space.Related`, one block and its button), a **compact solid amber button**, 40
   drawn, 48 to touch, 16sp SemiBold, trailing arrow, kind B colours (`tertiary` / `onTertiary`: 5.09:1 light, 7.16:1
   dark; fill on the page 4.68:1 light, 9.63:1 dark);
4. the X alone at the top end when closable (kind D, 48 target), as today;
5. rows 12 above and below their content; two rows separated by one `outlineVariant` hairline inset to the mark.

Copy changes (UK English, no hyphens): the Makkah brief becomes "Your location is not set yet." (the title already
says Makkah; the old sentence said it twice). Everything else keeps today's strings, so Urdu, Arabic, Turkish,
Indonesian and Bengali need one new string each, not a re-review of the rest.

Height saved on Times at normal text: the two cards are about 330 dp together today; the two rows are about 230. At
2x text (`A-times-light-2x.png`) the rows wrap and stack; nothing overlaps, the X keeps its 48 corner, the buttons
grow with their label. In Urdu (`A-times-light-urdu.png`, the app's own strings) the mark goes to the right, the X to
the left, the arrow turns, the hairline is the same; the Urdu lines sit a little taller (the known §11 line box item).

Qibla (`A-qibla-*.png`): the row goes under the title, 8 under it, brief only, Set location as above. Below it the
dial is drawn empty at reduced opacity (bezel, face, N and S, no needle, no Kaaba, no ticks) with one line under it,
"The dial shows the Kaaba once your location is set." in `onSurfaceVariant`. Why: today the Qibla tab is a title and
one amber card over an empty screen; a ghost of the thing the user came for says what the screen is before they set
anything. This part is optional (Ali's choice, question 3); the row works without it.

Where it needs Ali's word on §4.1: the button is **kind B at the compact size** (40 drawn, 48 target). §4.1 gives 48
for "buttons inside a notice" and the compact size only to green `CtaButton`. It is not a banned pattern (solid,
white or `#3A2A00` bold text, pill, 48 target), but it is a new line in the §4.1 height table and a new inventory
row, and it changes how a screen he has seen looks, so §0.2 says ask. If he says no, the row keeps the 48 button and
is 8 taller; everything else stands.

**Option B, honesty in the header.** `mockups/D/B-times-light.png`, `B-times-dark.png`.

The Makkah prompt leaves the notice slot and becomes one line in the header, on the sky, under the date: a 20 amber
mark, "Not your location yet" in `bodyMedium` `onTertiaryContainer` (7.35:1 on the sky's middle stop, light), and
at the end a compact solid **green** `CtaButton` with the pin, "Set location" (kind A compact, which §4.1 already
allows; fill on the sky top 5.83:1 light, 3.62:1 dark). Green, not amber, because setting a location is the app's
first step, not a fault. The exact alarms prompt is the Option A row. Qibla as in A.

Strengths: the user is already reading the city when they learn it is not theirs; the Times screen keeps only one
amber thing. Costs: the header grows 48 while the fallback lasts (the sky mark and the date line are already two
rows); the prompt is in the Times header only, so it is not the same pattern as Qibla and My prayers; on a 320 dp
phone or at 1.5x text the line wraps under the button and the header is 96 taller. Needs no §4.1 exception beyond a
new inventory row.

**Option C, one row and a sheet.** `mockups/D/C-times-light.png`, `C-times-dark.png`, `C-sheet-light.png`.

One Option A row, "2 things to set up", brief "Your location, and exact alarms for alerts on time.", button "Set up".
It opens a bottom sheet, "Set up SajdaTime", with the X and a foot Close, listing each item as a row with its mark,
title, brief and a solid green button (Set location, Allow). Items leave the sheet as they are done; the row
disappears with the last one. With one item left the row names it ("Allow exact alarms").

Strengths: the calmest Times screen of the three. Costs, and why it is not recommended: the Fajr sentence is one tap
away instead of on the screen, which the brief forbids for a message that affects whether an alarm rings; there is no
X, so a user who will not allow exact alarms carries the row for ever (reverses "closable on Times", DESIGN_SYSTEM
§4 Notices and `04-patterns.md` §4.3), or the X hides the location prompt too; and "2 things" is a count the app has
to translate and pluralise in six languages for a row that is usually one thing.

### D.4 Recommendation and why

**A.** It removes exactly what Ali objects to (the big amber panel and the large button) and keeps everything the
messages must do: the Fajr sentence stays in view, the location prompt cannot be closed, each action is one tap, the X
stays alone. It is one change to one component (`Notice` grows an `inline` style), so Qibla, My prayers and the
notifications prompt change with it and the pattern stays one pattern. It needs one §4.1 line from Ali, not an
exception to a rule. B is the better picture of the Times screen alone but makes the location prompt a special case
in one header; if Ali prefers B, build A's row for the other prompts and B's line only on Times.

Against the Apple guidance (`/apple-design`, Layout, Privacy, Writing): an inline row is the pattern Apple itself
uses for a missing permission in its own apps (a plain row with a button, no coloured panel); the X as the last
trailing element matches; the compact pill is a bordered button style, 44 pt met by the 48 target; text scales.
Nothing in A or B is Android only. The sheet in C is also fine on iPhone; C fails on content, not platform.

### D.5 Spec for the build (Option A)

- `Notice(style = NoticeStyle.CARD | INLINE)`, default `CARD` so no caller changes by accident. `INLINE`: no
  `sajdaSurface`, no gloss, no border, no box padding; vertical padding `Space.Paragraph` (12); mark 24; text as §D.3;
  the action `Button` with `heightIn(min = 40.dp)` and `minimumInteractiveComponentSize` (the compact rule); the
  "Details" button, when a caller passes details, the same size; `Modifier.padding(start = MarkSize + Space.Paragraph)`
  on the button row as today. The hairline between two inline rows is drawn by the caller (`HorizontalDivider`,
  `outlineVariant`, start padding 36), never inside the component.
- Callers that pass `style = INLINE`: `DefaultLocationBanner`, `ExactAlarmBanner` (through `PermissionCard(inline = true)`),
  `NotificationsBanner` (same, if Ali says yes to question 2), `QiblaBody`, `MyPrayersScreen.kt:198`. Settings keeps `CARD`.
- New string `home_default_location_body` = "Your location is not set yet." in every language; the old sentence is
  retired, not reused. Translation ledger stamp in the same commit.
- Qibla empty dial, if yes: `CompassDial` with a `ghost = true` parameter (no needle, no Kaaba, no ticks, 55% alpha),
  and one `bodyMedium` `onSurfaceVariant` centred line under it, `Space.Paragraph` below the dial; new string
  `qibla_ghost_hint`. Not a button, not tappable.
- §4.1 inventory rows to add: "Times, Makkah row: Set location, B compact, 40 drawn 48 target, `Notice.kt`"; "Times,
  exact alarms row: Allow, B compact; the X, D"; "Qibla, no location row: Set location, B compact"; "My prayers, no
  location row: Set location, B compact". Height table gets one line: "40 drawn, 48 to touch, the action of an inline
  notice". `DesignRulesTest` already lists `Notice.kt`, so no new file enters the inventory.
- Spacing: all gaps from `Space` (Tight 4 title to brief, Related 8 brief to button, Paragraph 12 row padding, Group
  16 slot above). `DesignRulesTest` passes as written.
- Contrast pairs for `ColorContrastTest`: `onTertiaryContainer` on `background` (8.53 / 14.87), `tertiary` on
  `background` as a 3:1 mark and as a button fill (4.68 / 9.63), `onTertiary` on `tertiary` (5.09 / 7.16, already
  asserted). On Times the rows sit on the sky's foot, which equals `background` (token `sky.foot`), so the same pairs
  hold; the header line in Option B would add `onTertiaryContainer` on every sky mid stop (7.35 light night; the other
  seven stops not measured here, the test would).
- States: Makkah row shows while `usingDefaultLocation`; exact alarms row while exact alarms are withheld and not
  dismissed (fade and shrink away as today); both rows: a hairline only when both show; TalkBack reads mark-less
  ("Times for Makkah. Your location is not set yet. Set location, button"), the X as "Close"; 2x text wraps, never
  clips; RTL mirrors the mark, X and arrow, not the hairline inset (it follows start).
- Not changed: `DefaultLocationBanner`'s order (first slot), the dismissal keys, Settings, the location sheet.

**iPhone effect: carries over.** Plain rows with a pill button and an `xmark` are SwiftUI `HStack` in a `List`
section; nothing Android only is added.

---

## E. The one row strip widget

### E.1 What exists (verified in code)

`TodayStripWidgetProvider` with `widget_strip.xml`: five columns Fajr to Isha, each glyph (16) over name (12sp) over
time (12sp, autosize to 9 at 250 wide), the next prayer in the highlight pill and bold, all other columns the same
ink. Four size entries from one layout (`WidgetRenderer.STRIP_SIZES`): 250×40 and 380×40 with the glyph row hidden,
250×64 and 380×64 with it; the town chip from 380 wide. From 1.3x text the next prayer alone on one line. Below
Android 12 the launcher gets the 250×40 entry at every size, so the glyphs never show there. Min size 4×1 at 40 dp
tall (`widget_today_strip_info.xml`, kept at 40 because 60 rounds up to two cells on some launchers).

So the widget Ali asked for already exists; what he has not got is one that looks good at the sizes launchers give
(about 330×80 on a 411 phone) and that reads the day at a glance. Two faults seen in the render of today's strip
(`E1-*.png`, last panel): the glyph row only appears at 64 dp tall, so at the common one cell height most users see
three bare lines of text; and nothing tells passed from still to come.

### E.2 Redesign, or a second widget

**Redesign the existing strip; do not add a second widget.** A second entry in the launcher's widget picker next to
"Today strip" would show two near identical rows and make a person choose; placed widgets of the old one would stay
old. Redesigning keeps the same provider, info XML, `WidgetUpdater` alarms and tests, and every placed strip updates on
the next redraw. The "little smaller" Ali described is the same widget at one cell tall, which the 40 dp minimum
already allows; a narrower 3×1 is a content question (which two prayers to drop) and is not recommended now.

### E.3 Two options

**E1, polished stack.** `mockups/E/E1-light.png`, `E1-dark.png`. The current composition kept (glyph over name over
time), with: passed prayers in `onSurfaceVariant` for glyph, name and time; the next in the pill, bold, its glyph in
`primary`; still to come in `onSurface`; time 13sp over name 12sp. Glyphs still need the 64 tall entry, so at one cell
on most launchers it is still three lines of text.

**E2, glyph beside the name (recommended).** `mockups/E/E2-light.png`, `E2-dark.png`. Each cell is two lines: the
glyph (16) and the name on one line, the time under it. Two lines of 12 and 13sp plus 2 dp between and 4 dp padding
fit 40 dp, so **the glyph shows at every height, including one cell tall and on Android 8 to 11**, which is what
"icons and prayer name and time" needs. Same inks as E1 (passed dimmed, next lifted). Panels in the mockup: 330×80,
330×44 (one cell), 379×80 with the town chip, 250×60 at the minimum, evening with Isha next, large text, no location.

| | E1 | E2 |
|---|---|---|
| Glyph at one cell tall | no (needs 64) | yes |
| Glyph on Android 8 to 11 | no | yes |
| Fits 250 wide with glyph | yes | no, glyph drops first (shown) |
| Cell width needed with glyph | about 56 | about 66 ("Maghrib" with its glyph) |
| Reads as a row | five little columns | five labels, like a timetable header |

### E.4 Spec for the build (E2)

Sizes, one layout, written both ways at every redraw (renderer rule 2):

| Entry (dp) | Shows |
|---|---|
| 250×40 | names and times only, time autosize 13 to 10, name 12 to 10 (the one place below the 12 floor, already recorded) |
| 300×40 | glyph beside name, time under; 2 dp padding |
| 300×64 | the same with 4 dp padding and 4 dp between the lines |
| 400×64 | the same plus the town chip at the end |

Below Android 12 the first entry at every size (no glyph; recorded, not hidden). The chip moves from 380 to 400 wide:
at 379 with the chip the glyph cells touch (`E2-*.png` third panel shows the fallback without glyphs); from 400 both
fit. What each cell shows: glyph 16, tinted by the ImageView from a colour resource; name 12sp medium; time 13sp,
`tnum`; the next prayer in `widget_row_highlight` with name and time bold and the glyph `widget_primary` (6.54:1 on
the highlight light, 6.49:1 dark); passed prayers in `widget_on_surface_variant` (6.28:1 on the widget surface light,
8.00:1 dark; 5.13:1 and 6.52:1 on the highlight, not needed since a passed row is never highlighted); still to come in
`widget_on_surface`. Dimming is by ink only, never by alpha (alpha on an unknown wallpaper cannot be contrast tested).
"Passed" is `WidgetState.isHighlighted`'s complement before the next slot; the renderer already knows the next.

Scaling with the phone's text size: 1.0 to 1.29, sp scales and autosize shrinks to the floors above; from 1.3 the next
prayer alone (today's rule), now with its glyph at 22, name 16sp bold, time 16sp, and at the end, only when
`countdownAllowed`, the `Chronometer` countdown (H:MM:SS) the other widgets use. **The "in 2 h 10 min" shown in the
mockup's large text panel is a mockup slip: it cannot be built** (it needs a redraw every minute, RESEARCH.md
feasibility; the Chronometer ticks for free). What is dropped when space is tight, in order: the chip, the glyph, the
time shrinks (never am or pm), the name shrinks, then the next prayer alone. Ramadan words ("Suhoor", "Iftar") replace
the name of Fajr and Maghrib as today; approximate days keep the spoken marker only.

RemoteViews limits that shape this (so nobody designs past them): only `LinearLayout`, `FrameLayout`, `TextView`,
`ImageView`, `Chronometer` and a few more; no custom views, no Compose, no measuring at render time, no per cell
font; one text size per view set in sp or dp; autosize is API 26 and up; colours must be resources so the launcher
resolves dark mode (a colour chosen in code is baked until the next redraw); no translucency or blur over the
wallpaper; size buckets only on Android 12 and up; the launcher may reuse views, so every property is written every
time; the countdown digits follow the phone's language, not the app's; no update without an alarm, and this app
redraws only at prayer boundaries, so nothing on the widget can say "in N minutes".

Light and dark: System follows the launcher through the layout's resources; a chosen Light or Dark theme is pinned by
`PAINTS` as today (`WidgetThemeTest` fails the build if a new coloured view is not listed, so the dimmed inks need
their `Paint` entries). The widget preview image (`widget_preview_strip`) and `docs/store/screenshots/` are retaken.

**iPhone effect: Android only (home screen widgets), design carries over.** WidgetKit `systemMedium` holds the same
five cells with the same inks; `accessoryRectangular` on the lock screen holds the large text form (next prayer and
time). No decision here would need undoing for WidgetKit.

---

## Decisions for Ali (his call; recommended answer first)

1. **D: which treatment?** A quiet rows (recommended), B header line plus a row, or C one row and a sheet.
   Pictures: `mockups/D/A-times-light.png` and `-dark`, `B-times-*`, `C-times-*` and `C-sheet-light.png`, against
   `current-light.png` and `current-dark.png`.
2. **D: the compact amber button (40 drawn, 48 to touch) in the row?** Yes (recommended, one new line in §4.1's height
   table) or keep 48 (the row is 8 taller). And: does the notifications prompt on Times take the same row? Yes
   (recommended; it shares the code).
3. **D: the empty dial on Qibla while there is no location?** Yes (recommended, `A-qibla-*.png`) or the row alone.
4. **E: E2 glyph beside the name (recommended) or E1 polished stack?** With the dimmed passed prayers in both. Pictures
   `mockups/E/E2-light.png`, `E2-dark.png`, `E1-light.png`, `E1-dark.png`.

Defaults taken unless he objects: redesign the existing strip rather than add a second widget; no 3×1; the chip from
400 wide; passed prayers dimmed by ink.

## What was not verified

- Nothing was run on an emulator or a device; every size and gap is from the tokens and the code, drawn in HTML.
  Roboto is a little wider than the Mac font, so the E2 cell widths (66 dp with a glyph) need one render on the
  emulator at 250, 300 and 330 before the size entries are fixed.
- Contrast ratios are computed from the token hex values with the WCAG 2.1 formula in `gen_mockups.py`; the build's
  `ColorContrastTest` and `WidgetColorsTest` are the proof.
- The Urdu render uses the app's own Urdu strings for the rows, header and list; the tab labels are the app's too, the
  "2 things" count in C was not rendered in Urdu. Arabic, Turkish, Indonesian and Bengali were not rendered.
- The 2x render doubles every sp and leaves dp alone, as Android does; the hero countdown in it was capped at 72 px so
  the mockup would not clip, which is not a decision about the hero card.
- Whether TalkBack reads the inline row in the order written depends on the Compose semantics the build gives it.
