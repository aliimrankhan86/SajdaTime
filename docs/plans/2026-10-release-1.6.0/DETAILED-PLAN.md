# 1.6 detailed plan (Fable 5.1, 23 Oct 2026)

Written by the independent Fable 5.1 reviewer at Ali's request, after his answers in PLAN.md section 0.1.
**Ali accepted it on 23 Oct 2026 ("that is fine - incorporate it").** PLAN.md section 0.1 records what that
changes. Sections 6 (journeys and wording) and 7 (languages) follow below when written. The whole plan then
gets an independent gap review before any development.

---


Read only. Verified against `claude/1-6-0` at 9d815bf and the resolved libraries in the Gradle cache. Guesses are marked.

## Verified facts the plan rests on

- Compose BOM 2026.06.01 (gradle/libs.versions.toml:8) resolves to `compose-ui` and `compose-animation` 1.11.4 and `material3` 1.4.0 (BOM mapping page, and the jars in `~/.gradle/caches`). The `material3` 1.4.0 jar contains `MotionScheme`, `MotionScheme.StandardMotionSchemeImpl`, `ExpressiveMotionSchemeImpl` and `MaterialExpressiveTheme`; the `ui` 1.11.4 jar contains `androidx.compose.ui.MotionDurationScale`. minSdk 24, targetSdk 36 (app/build.gradle.kts:37-38).
- **Compose does honour the system animator scale.** `MotionDurationScale` is read from `Settings.Global.ANIMATOR_DURATION_SCALE`; support arrived in Compose Animation 1.2.0 (release notes); a scale of 0 ends every animation "in the next frame callback" (API reference). The app has no code of its own for it (no match in `app/` or `wear/`). The accessibility toggle "Remove animations" writes 0 to that scale on stock Android, which I could not confirm from an official page: treat as a belief and verify once with `adb shell settings get global animator_duration_scale` after flipping the toggle on the emulator (PLAN.md:204-205 already demands the check).
- Haptics in use today: `ToggleOn`/`ToggleOff` (Tick.kt:65, SettingsList.kt:225), `SegmentTick` (MainScaffold.kt:300), `LongPress` (QiblaScreen.kt:165).

## 1. Animation toolkit

| API | For, here | Used by (table numbers from the earlier animation plan) | Gotchas |
|---|---|---|---|
| `animate*AsState` (Float, Color, Dp) | One value chasing a target; the default for anything two-state | 3 bell crossfade (alpha and tint), 5 pause row, 9 Now/Next row fill (`animateColorAsState` with `drawBehind`, which HomeScreen.kt:1033-1036 already does for the bar), 13 Qibla fill and rim, 14 dial (exists, QiblaScreen.kt:207) | Read the state inside `drawBehind`/`graphicsLayer` lambdas, not in composition, or every frame recomposes the row. Give every one a `label` (the lint rule). |
| `Animatable` in a `LaunchedEffect` | A one-shot that must run exactly once on open, with a `snapTo` fallback | 2 week rings (progress 0 to share), 11 day path draw-in (0 to 1 fraction, then `snapTo` on each minute tick) | Key the effect on the screen entry, not on `now`, or it replays each minute. On rotation it replays: acceptable, or key on `rememberSaveable` flag. |
| `updateTransition` | Several properties driven by one state, kept in step | 1 the tick: ring sweep, tick path fraction and tint from one `TickState` | Only where two values must stay locked; one value is `animate*AsState`. |
| `AnimatedContent` | Swapping composables of different size with a size transform | 12 Ramadan plate words (but see §2: the plate keeps a fixed height, so `Crossfade` is enough), existing tab switch (MainScaffold.kt:186) | Both children compose during the swap: keep them light (text only). Default `SizeTransform` animates height; pass `sizeTransform = null` for fixed-height content. |
| `AnimatedVisibility` | Enter/exit of a block that must leave the composition (TalkBack stops seeing it) | 5 "Tracking is paused" row, existing folds (Notice.kt:153, SettingsList.kt:298) | Do not use for the Undo snackbar (Material does it). Exit animation plus `verticalScroll` can jump the scroll position; test on the Tracker tab. |
| `Crossfade` | Fade between two fixed-size states | 10 sky gradient change, 12 plate words | Draws both layers for the duration: fine for a gradient, costly for a Canvas with many paths. For the sky, crossfade two `Brush` backgrounds in one `drawBehind` with a lerp fraction instead (one layer). |
| `Canvas` + `Path` + `PathMeasure` | Drawing the day path, the ring arcs, the tick stroke | 1, 2, 11; the partial week rings and the month grid | `PathMeasure.getSegment` for the draw-in. Mirror the x axis when `LocalLayoutDirection` is Rtl so the path runs right to left (the compose `Canvas` does not mirror for you). `drawArc` sweep sign flips in RTL too. |
| `graphicsLayer { }` | Alpha, scale, rotation without recomposition | 3 bell (alpha of the slash), 14 dial rotation (`rotationZ`) | Lambda form only; the non-lambda overload recomposes. |
| `rememberInfiniteTransition` | **Banned.** | Nothing | Add the string to `DesignRulesTest`'s banned list (it already scans for `OutlinedButton`, DESIGN_SYSTEM.md:379-387). PLAN.md:193 "no endless animation". |
| `spring` vs `tween` | `tween` with `FastOutSlowInEasing` for everything here; `spring` only for the tab pill if Ali wants bounce (he has not asked) | All | Springs have no fixed duration, so "150 to 400 ms" cannot be asserted; tween can. |
| M3 `MotionScheme` (1.4.0) | Material's own duration/easing scheme for M3 components; `MaterialExpressiveTheme` switches components to expressive springs | Not used. The app uses `MaterialTheme` (Theme.kt). Switching to Expressive changes every M3 component's motion at once, which Ali has not seen | Leave as is. Record in §9 as considered: one scheme change for the whole app is not "animation where required". |
| `LocalHapticFeedback` + `HapticFeedbackType` | Every haptic | 1 `ToggleOn/Off` (exists in Tick.kt), 3 bell `ToggleOn/Off`, 13 existing `LongPress` | `ToggleOn`/`ToggleOff`/`SegmentTick` map to `HapticFeedbackConstants` that exist from API 34; on older phones Compose falls back to a basic effect (belief; the app already ships them on minSdk 24, so the fallback is proven to not crash). No `View.performHapticFeedback` directly; the Compose wrapper is enough. |

**Testing.** `createComposeRule()` has `mainClock.autoAdvance = false`; the week-ring test advances the clock and asserts the sweep; the sky test sets `MotionDurationScale` to 0 through `effectContext` (API reference) and asserts the end state in one frame. The repo has no Robolectric (DESIGN_SYSTEM.md:341); these are instrumented tests on the emulator, in the device suite.

**Battery.** Nothing runs while the screen is idle except the countdown text (one recomposition per second, already the case) and the Qibla dial while the tab is on screen (already the case). The day path's dot moves on the minute tick with `snapTo`, no tween.

**Libraries.** Lottie: no (PLAN.md:185 already rejects). Rive: no, same reason plus a runtime and a designer file format. Haze (backdrop blur): no; DESIGN_SYSTEM.md:691-694 already decided 97 percent opacity without blur and says add it only if Ali asks for more glass. Nothing here needs a dependency; the ponytail rule holds.

**Motion spec, one file `ui/theme/Motion.kt`** (next to `Spacing.kt`):

```
object Motion {
    val Quick = 150      // bell, chip state swap
    val Standard = 250   // colour and alpha changes, Qibla confirmation
    val Reveal = 300     // the tick, the pause row, Now/Next move
    val Draw = 400       // week rings, sky change, day path draw-in
    val Easing = FastOutSlowInEasing
    fun <T> tween(ms: Int) = tween<T>(ms, easing = Easing)
}
```

Four durations by relationship, the way `Space` has five gaps by relationship (DESIGN_SYSTEM.md:231-237): a state flip is Quick, a colour is Standard, a shape appearing is Reveal, a drawing is Draw. `DesignRulesTest` fails a `tween(` with a number literal in `ui/`, the same mutation-tested pattern as D5.

## 2. Placement and detail

### 2.1 Height budget at 823dp (S23 Ultra, density 3.75)

Read from `times-tracker-jamaah.png` (1440 by 3088 px, so px/3.75 = dp) and the code; marked where it is a pixel reading rather than a code constant.

| Element | dp | Source |
|---|---|---|
| Header: 4 top, city row about 36, date line 48 target | about 90 | HomeScreen.kt:348-354 (Space.Tight, 2dp row padding, 48dp date target); pixel reading 55 to 130 |
| Gap | 4 | HomeScreen.kt:191-193 Space.Tight |
| Next prayer card | about 177 | pixels 517 to 1180 px; card padding 16 (HomeScreen.kt:476) |
| Gap | 16 | HomeScreen.kt:214 Space.Group |
| Today line (40dp compact button) plus 8 under | 48 | HomeScreen.kt:878-895 |
| Six rows at the 50dp floor | about 313 | HomeScreen.kt:1041, pixels 1450 to 2623 |
| Tracker line, 8 plus 40 | 48 | MyPrayers.kt:56-58 |
| Mosque door 52, 8 above | 60 | HomeScreen.kt:218-226 |
| Bottom bar clearance | about 96 | `BottomBarClearance()` (bar plus 8) |

Sum without the tracker line: about 804, which matches the 17 Oct measurement "all six times fit" (DESIGN_SYSTEM.md:722-725). With the tracker line: about 852, 29dp over, which is what the screenshot shows (the door under the bar). **Moving the ticks and the tracker line to the Tracker tab, as Ali agreed, returns the 19dp of slack.** Jama'ah second lines cost about 2dp a row (HomeScreen.kt:1050-1052). Ramadan adds the card (about 200dp, 03-ramadan-dark.png) and scrolls, knowingly (HomeScreen.kt:198-201). Font scale 1.5 scrolls; allowed (DESIGN_SYSTEM.md:724-725).

### 2.2 Where the sky goes: options

| Option | What it is | Height cost | Verdict |
|---|---|---|---|
| A. Sky as the card's background | The hero gradient becomes four gradients with artwork in the card's corners | 0 | **Rejected.** Ali: the green and light card stays consistent (PLAN.md:166-168). Four card colours a day is the opposite of consistent, and the countdown inks need 4 by 2 by 3 retests. |
| B. Thin band behind city and date | A full-bleed sky strip the header's height, card below on plain background | 0 | Works but the sky is 90dp of colour with no room for the path; it reads as a coloured status bar. |
| C. Sky only inside the card | Same as A | 0 | Rejected with A. |
| **D. Sky as a backdrop behind header and card** | Full-bleed from the top of the screen (under the status bar) to 24dp below the card's bottom, then a fixed fade stop into `background`. Header text sits on the sky; the card floats on it unchanged, its light glow (DESIGN_SYSTEM.md:142-143) doing the lifting. Sun, moon or stars and a small five-dot path sit in the header's trailing half, beside the short town name | 0 at default text; the header's trailing half is free since the short town name shipped (1.6.0 screen shows "London") | **Recommended.** Keeps the card exactly as Ali knows it, gives the time of day in pictures, nothing moves. If the city name is long (ellipsis at `maxLines = 1`, HomeScreen.kt:367-369) the artwork hides and only the sky colour remains: rule, not a guess to test later. |
| D plus full-width path | As D, with the day path on its own 56dp band between the date line and the card | 56, so Times scrolls at 823dp | Second screenshot for Ali only if he asks for the Pillars arc. Costs the no-scroll rule, so not recommended. |

Edge to edge: the sky must draw under the status bar for D to look right. I did not verify whether the activity is edge to edge (guess: it is, targetSdk 36 enforces it); if not, D still works from the status bar down.

### 2.3 The four skies

Boundaries, by the day's own times from `PrayerEngine`, never the clock: dawn = Fajr to sunrise, day = sunrise to Asr, evening = Asr to Isha, night = Isha to Fajr. The sky names the prayer period, which is the point (PLAN.md:148-149).

Candidate stops, three per sky, the third always the page `background` so the fade is testable. These are candidates from the existing family (Color.kt:114-116, 139-140; DESIGN_SYSTEM.md §1) for `ColorContrastTest` to accept or reject; nothing is chosen by eye.

| Sky | Light (top, mid, foot) | Dark (top, mid, foot) |
|---|---|---|
| Dawn | `#E6EEEA`, `#DDE8E2`, `#F7F5F3` (cool pale mint) | `#14241F`, `#121A17`, `#101312` |
| Day | `#D6E8DC` (LightHeroStart), `#E1EBE1`, `#F7F5F3` | `#18382D` (DarkHeroTop), `#143028` (primaryContainer), `#101312` |
| Evening | `#E4D2B4` (LightHeroEnd, sand), `#EDE2D0`, `#F7F5F3` | `#263020`, `#1A1F18`, `#101312` (olive dusk; **not** `tertiaryContainer` `#2A2113`, because amber means warning, DESIGN_SYSTEM.md:166-168) |
| Night | `#C9DDD2`, `#DCE8E0`, `#F7F5F3` (deeper mint) | `#0E1C18`, `#0D1512`, `#101312` |

Dark mode answer to Ali's question 1: the same artwork (sun, moon, stars, path) in both themes, dark colour variants of each sky, so a dark-mode user at noon sees a dark "day" sky with a sun, not a light one. Shown to him as four pairs.

Inks on the sky: city `onSurface`, Gregorian date `onSurfaceVariant`, Hijri `primary`, the caret `primary`. Artwork: sun and path in `primary`, moon and stars in `onSurfaceVariant`, the "now" dot `primary` with an `onSurface` ring (shape as well as colour). Contrast plan in `ColorContrastTest`: every ink against every stop of every sky in both themes (2 by 4 by 3 by 3 = 72 text assertions at 4.5:1), the now dot and path against every stop (24 at 3:1), the foot stop asserted equal to `background`, and the existing card and glass bar assertions unchanged. Guess to be computed: light `primary` `#0B5A43` on `#C9DDD2` is near 5:1 and the dark `onSurfaceVariant` on `#263020` near 6:1; both likely pass, and the test decides.

### 2.4 Per-screen specifications

**Times (top to bottom).** Sky backdrop (D). Header: city 26sp with caret at the start; artwork block 56dp tall in the trailing 45 percent, five dots on a shallow arc, passed segment solid `primary`, the rest `outlineVariant`, the now dot on the arc; mirrored in RTL. Date line, Hijri in `primary`, 48dp target (exists). 4dp. The card, unchanged. 16dp. Rare notices (no location, polar, exact alarm, Ramadan, Eid) in their current slot. Today line: "Today" at the start, compact solid "Timetable" with a calendar icon at the end (the renamed Save as PDF, inventory row HomeScreen.kt:412). 8dp. Six rows: glyph, name over an optional jama'ah line, Now or Next pill, time, then the bell in a 48dp target at the row's end, overlapping the 18dp end padding the way the tick did (HomeScreen.kt:1043-1046). Sunrise has no bell and keeps the 48dp blank. 8dp. Mosque door. Clearance.

Row states: plain, current (`surfaceContainerHigh`, Now pill), next (fill, bar, Next pill), sunrise (dimmed, one step smaller). No past-row fade. Bell states: on as `Icons.Rounded.NotificationsActive` in `primary`, alarm as `Icons.Rounded.Alarm` in `primary`, off as `Icons.Outlined.NotificationsOff` in `outline`; content description "Fajr alert on, notification" / "Fajr alert off". Tap off to on restores the prayer's last style, default notification (new field, guess: one map in `AppSettings`). Animation 3: the icon crossfades (alpha, `Motion.Quick`), haptic ToggleOn/Off. Undo snackbar: Material `SnackbarHost` at the bottom above the glass bar, text "Fajr alert off", action "Undo" in `primary` (a Snackbar action is Material's own control; record it in §4.1 as kind G rather than argue it is a text link), 4 seconds, one at a time, and the bell does not animate back on Undo (it snaps, the state was never meant to change). Fajr in Ramadan: "Fajr alert off. Suhoor reminder stays on."

No-location state: the card shows "Set your location" as today (HomeScreen.kt:481-487); the sky still draws by the clock's period from Makkah times (the fallback already used), so the header never looks broken.

**Tracker tab.** `ScreenTitle` "My prayers". Tracking off: one `sajdaSurface` card with the switch row ("My prayers", the existing sentence) and nothing else; no illustration, no sales text. Tracking on: today's date line (same style as Times); five rows in one card, each with the ring tick at the start (48dp, Tick.kt), name, time at the end; a day already past with no tick shows a plain outline, never red or counted. Below, 16dp, one row: compact solid "Pause" (pause icon) at the start and compact solid "Last 30 days" at the end. Below, 24dp, the week strip: seven 36dp rings, Mon to Sun in the phone's first-day order, each ring's arc the share of five, with "3 of 5" as the content description and the day letter under it; the paused day shows the pause glyph in the ring; today's ring has an `outline` border. Paused state: the button row becomes "Tracking is paused" with compact "Resume" at the end, same height (MyPrayers.kt:56-60); the rings keep drawing. Animations: 1 tick (updateTransition, `Motion.Reveal`, haptic), 2 rings (Animatable, `Motion.Draw`, on open and on change), 5 pause row (`Motion.Reveal`). Font scale 1.5: the strip wraps to two rows of four and three. RTL: rings Sun to Mon from the right as the locale says; the Canvas arcs mirrored.

**Last 30 days sheet.** Grid of 30 rings, six rows of five, newest first, each with its day number and the "3 of 5" description; tapping a ring opens that day's five ticks under the grid (the existing per-day row, last-30-days.png); paused days with the glyph; nothing counts blanks. This replaces the long card list, which is 30 cards tall.

**Qibla.** Title. "Turn right 58°" in `headlineMedium` on one line; "Facing 60°" under it in `bodyLarge`. The dial, radius as today (QiblaScreen.kt:190 keeps room below). Key. Place name small in `onSurfaceVariant` under the key, shown only when the label exists. A compact solid "Details" with a chevron under the place name; expanded, two lines: "About 5024 km from here" (or miles by `LocaleData`/the locale's measurement system) and "118° from north". Aligned: existing fill, rim, wording and haptic; animation 13 (`animateColorAsState`, `Motion.Standard`). No compass and no location: the existing amber notices above the dial.

**Settings.** No new rows beyond the signed-off ones. Prayer alerts keeps the per-prayer chooser and mirrors the bells (DESIGN_SYSTEM.md:764).

**Timetable sheet.** Title "Timetable" with compact solid "Save as PDF" beside it (the Ramadan sheet's pattern, DESIGN_SYSTEM.md:419); under it a month list, today highlighted; in Ramadan the sheet opens on the Ramadan rows with suhoor and iftar columns (RamadanTimetableSheet.kt:131-140) and the Shift the date button at the foot. One sheet, two tables, chosen by `offersTimetable` (HijriCalendar.kt:62).

**Ramadan states on Times, 1.6.0.** Unchanged from today: the notice three days before, the card during the month, Eid's notice after (as set out in the earlier answer). The sky shows the ordinary period. The plate-merge idea from the earlier answer is withdrawn because Ali keeps the card; the only change is that the Timetable button opens the Ramadan sheet. Nothing enters mid-view: the notice and card decisions are made at screen open.

## 3. Missed fasts research and recommendation

**What Pillars does (public sources).** Their site says "log a missed fast with the reason, such as travel or health", with reason options Travel, Health, Other, Menstruation and Pregnancy, a running "N days" owed, "You're all caught up", and a "Made up a fast on this day?" count on a day; the Play and App Store listings say "log your missed fasts with the reason and then tick them off when you make them up" and a separate "Menstruation (pause) button" for the prayer streak. The feature shipped in Ramadan 2023 from a 208-vote request. Their privacy policy and FAQ say nothing about fasting or health reasons; the App Store privacy labels list no health category, and their Play data safety says device IDs may be shared (PILLARS-STUDY.md:9). On the feature board users report: records lost on a new phone (twice, 2024 and 2026); no bulk entry ("enter a number"), so a woman with three months of fasts after pregnancy could not mark them off; a reviewer noted only Ramadan make-ups, not voluntary fasts; others report the count not updating, made-up fasts re-added, and no undo for an accidental tick. The one positive quote (The National, 2024) values exactly two things: the reminder of how many days are owed, and the tick to clear one.

Sources: [thepillarsapp.com](https://thepillarsapp.com/), [Pillars feature board](https://pillars.noorahq.com/feature-requests/posts/fasting-tracker-), [Pillars privacy](https://thepillarsapp.com/privacy), [App Store listing](https://apps.apple.com/gb/app/pillars-prayer-times-qibla/id1559086853), [Play listing](https://play.google.com/store/apps/details?id=com.pillars.pillars&hl=en), [The National, Ramadan apps 2024](https://thenational-the-national-prod.cdn.arcpublishing.com/uae/2024/03/18/ramadan-technology-islamic-apps).

**What this says for SajdaTime.** The value users name is the number owed and clearing it; the reasons are decoration that becomes health data the moment "menstruation" or "pregnancy" is stored. The complaints are about data survival and bulk entry, which our save-to-file and a plain number solve.

**Recommendation: a count, no reasons, no dates, after Ramadan (1.6.1).**
- One row in the My prayers group, "Fasts to make up", with the kind C minus and plus steppers (SettingsScreen.kt:426 pattern), value shown as the number; subtitle "Add one for each fast you still owe. Take one off when you make it up." Shown on the Tracker tab, under the week strip, only when the number is above zero: "3 fasts to make up" with a compact solid "Made up one" at the end; the line disappears at zero with no message, as the streak does.
- Works for Sunni and Shia alike (making up Ramadan fasts is common to both, PLAN.md:132-133). Fidya and kaffarah are out: they are rulings that differ by madhab and involve money or food measures, and the disclaimer says the app gives no rulings. The app never suggests which days to fast.
- Wording: "to make up", never "missed", "owed", "debt" or "qada". `TrackerWordingTest` then needs no exception; the conflict dissolves. No guilt line, no deadline, no "before next Ramadan" countdown (that is a nag).
- Privacy: the number is religious practice data, not health data. `docs/privacy.html` gains one sentence in the prayer record section: "If you use the fasts to make up count, that number stays on this phone, is saved in the file you save and is deleted with your prayer record." No usage-count event. Export: one more field in the tracker file, versioned; a file from a newer version stays read only (UX.md:105-108).
- Why after Ramadan: the number is set after the month, and 1.6.0's deadline is the reason to keep it out (SIGNOFF.md:38). Why count only: it removes the one part of the Pillars feature that is both unloved in their reviews and dangerous in ours.

## 4. Build sequence for 1.6

Gates stay as signed off: 1.5.0 live by 27 Nov or everything moves after Eid; PR by 11 Dec, submit by 15 Dec (SIGNOFF.md:31-35). Each step is its own droppable set of commits with screenshots for Ali before the next starts.

| # | Step | Depends on | Cut order if short |
|---|---|---|---|
| 0 | `Motion.kt`, `DesignRulesTest` additions (infinite transition ban, tween literals), `ColorContrastTest` sky assertions scaffold | nothing | never |
| 1 | Tracker tab: fourth destination (no usage-count word for it), ticks and tracker line leave Times, today card, Pause and Last 30 days buttons, week rings with partial fill and pause glyph, 30-day grid | signed-off tracker code on `claude/quiet-glass` | never |
| 2 | Islamic dates sheet | nothing | never |
| 3 | Bell on each row with Undo; Prayer alerts mirror | nothing | last |
| 4 | Timetable rename, month on screen, Ramadan merge | nothing | 6th |
| 5 | Sky backdrop, four skies, header artwork, animator-scale check | 0 | 4th |
| 6 | Qibla restyle | 0 | 5th |
| 7 | Jama'ah lines on Times | signed-off jama'ah core | 3rd (SIGNOFF.md:38 says first; I move it behind the widgets because Ali has now put look and feel above typed-in features) |
| 8 | Widgets (§5) | 5 (the skies) | 2nd |
| 9 | Fasts to make up count | 1 | 1st: it is 1.6.1 already |

Checkpoints: 20 Nov (steps 1 to 3 on the emulator in light, dark, Urdu, 1.5), 4 Dec (5 and 6 screenshots to Ali), 11 Dec PR. If 1.5.0 is not live by 27 Nov the whole branch keeps building and ships after Eid with 9 included.

## 5. Widgets

**What we have.** Two widgets, four layouts, picked by the launcher by size on Android 12 and later (WidgetRenderer.kt:68-78; DESIGN_SYSTEM.md:565-574): Next prayer small (180 by 110dp) and wide (270 by 150dp); Today compact (250 by 180dp) and large (250 by 280dp, 330dp in Urdu and Arabic). A `Chronometer` countdown the launcher ticks for free, shown only when exact alarms are allowed (HANDOVER 1402-1405); the day's five bars; glyphs that show where the sun is; the location chip; Ramadan words on Fajr and Maghrib (WidgetState.kt:35-45, 86-92); the Hijri date on the large Today only. Redraws happen only at the next prayer, at midnight, or when the app opens (WidgetRefreshScheduler.kt:14-15, 47-49; request code 20,000). Colours are `@color` resources resolved by the launcher at draw time, with `values-night` twins and fixed Light and Dark sets for the in-app theme choice (colors.xml:7-57; HANDOVER lesson 130). Tested by `WidgetColorsTest` against every gradient stop (DESIGN_SYSTEM.md:590-592). The 06-widgets.png large Today is already close to Pillars' strip in content and better in two ways: glyphs that mean something, and the town.

**What Pillars has that we lack (play-02.png).** A one-row five-prayer strip with the current prayer marked and a tiny arc per prayer, and a square sky widget with the period name, a "time left" pill and the sun path.

**Proposed set (three widgets, five layouts).**

| Widget | Sizes | Shows | Ramadan | No location | Alerts off |
|---|---|---|---|---|---|
| **Next prayer** (exists, gains the sky) | small 3 by 2, wide from 270 by 150 | Sky background for the period; glyph, name, time; countdown; five bars; town chip. Wide adds the six rows | "Suhoor ends" / "Iftar" word by the name (exists) | "Set your location" line, bars hidden (exists, WidgetRenderer.kt:123-133) | Nothing; the widget never shows alert state (no change) |
| **Today strip** (new) | 4 by 1 minimum (250 by 60dp), 5 by 1 | Five columns Fajr to Isha: glyph over name over time; the next column in the highlight pill with bold time; passed columns' glyphs in `onSurfaceVariant`; the town in a 12sp line only at 5 by 1 | Fajr and Maghrib columns show "Suhoor" / "Iftar" instead of the name (the time matters more than the word, and both words fit a column at 12sp; the name is in the content description) | One line "Set your location" | No change |
| **Today** (exists) | compact 4 by 3, large 4 by 4 | As now; large adds jama'ah as a 12sp second line under a prayer that has one | As now | As now | No change |

Resize: `resizeMode="horizontal|vertical"` as now; the launcher picks the layout by `SizeF` with no app wake (WidgetRenderer.kt:106-108). Below Android 12 each widget keeps its first layout. RTL: the strip's columns run Isha to Fajr from the left, which `AppLocale` layout direction already forces for widgets (HANDOVER lesson 131). Large text: the strip's name and time are sp; at 130 percent or more the strip hides the glyph row, the way the second layouts are dropped today (HANDOVER 1410-1411). Dark and light: `values-night` twins plus the fixed Light and Dark sets, unchanged mechanism; launcher dark mode is the same `values-night` path.

**The sky on widgets.** Four fixed `widget_sky_*` gradient drawables per theme, chosen at refresh by the period; the refresh already fires at each prayer time and at midnight, so the sky changes at the right minute with no new alarm. Sunrise must count as a boundary: the Today rows include it, and I believe `state.next` does (guess; if it does not, add sunrise to `nextRefreshAt`). The sun path on the Next widget: a layered vector of five dots on an arc with the passed segments as separate drawables toggled by `setViewVisibility`, every colour an `@color` so dark mode resolves at draw (lesson 130: a bitmap bakes colours and goes stale). **No "now" dot** on widgets: it would need a redraw every few minutes, which is the battery cost the hero-countdown design rejected (DESIGN_SYSTEM.md:922-927) and the Chronometer exists to avoid. The arc shows which prayer the day has reached, which changes only at prayer times. Mini arcs per prayer on the strip are not copied: our glyphs already say where the sun is (DESIGN_SYSTEM.md:581-584).

**RemoteViews limits that shape this.** No animation of any kind. Only the inflatable view classes (lesson 130). Every property written both ways on every update. Android 12 and later round corners to `system_app_widget_background_radius`; use it for the background shape, with 24dp as the pre-12 value. Dynamic colour (Material You) is not used: the tokens are the brand and `WidgetColorsTest` holds them (DESIGN_SYSTEM.md:934-935). Previews (`previewLayout`) show the base light layout until the first update, accepted already (HANDOVER 1399-1401).

**Contrast test plan.** `WidgetColorsTest` extends to the four skies: every widget ink (name, time, countdown, chip, sublabel) against each stop of each sky in light and dark, and under the sheen on the top stop (the existing worst-case rule), plus the arc's passed and unpassed colours at 3:1 against every stop. Rendered through the real `RemoteViews.apply` path on the emulator in light, dark, Urdu and 130 percent text before any screenshot goes to Ali.

**Where it fits.** Step 8, after the phone skies exist so the widget gradients are the same stops; cut second if time runs short, because the current widgets are already live and liked, and the store screenshot 06 must be retaken whenever they change.

Not verified: any dp figure marked as a pixel reading; whether the activity is edge to edge; whether `WidgetState.next` includes sunrise; the exact fallback behaviour of `HapticFeedbackType.ToggleOn` below API 34; and the "Remove animations" to animator-scale mapping, which one emulator check settles.
