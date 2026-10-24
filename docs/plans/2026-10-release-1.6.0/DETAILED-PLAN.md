# 1.6 detailed plan (Fable 5.1, 23 Oct 2026)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

Written by the independent Fable 5.1 reviewer at Ali's request, after his answers in PLAN.md section 0.1.
**Ali accepted it on 23 Oct 2026 ("that is fine - incorporate it").** PLAN.md section 0.1 records what that
changes. Sections 6 (journeys and wording) and 7 (languages) follow below. The whole plan then
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

**Motion spec, one file `ui/theme/Motion.kt`** (superseded by BUILD-INSTRUCTIONS §5 row 0 and §6: three durations, `Reveal` dropped, every `tween` outside `Motion.kt` banned) (next to `Spacing.kt`):

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

**Tracker tab.** (Animation superseded by BUILD-INSTRUCTIONS §6: `Reveal` dropped, no ring animation on open.) `ScreenTitle` "My prayers". Tracking off: one `sajdaSurface` card with the switch row ("My prayers", the existing sentence) and nothing else; no illustration, no sales text. Tracking on: today's date line (same style as Times); five rows in one card, each with the ring tick at the start (48dp, Tick.kt), name, time at the end; a day already past with no tick shows a plain outline, never red or counted. Below, 16dp, one row: compact solid "Pause" (pause icon) at the start and compact solid "Last 30 days" at the end. Below, 24dp, the week strip: seven 36dp rings, in the app language's first-day order (superseded by 8.6), each ring's arc the share of five, with "3 of 5" as the content description and the day letter under it; the paused day shows the pause glyph in the ring; today's ring has an `outline` border. Paused state: the button row becomes "Tracking is paused" with compact "Resume" at the end, same height (MyPrayers.kt:56-60); the rings keep drawing. Animations: 1 tick (updateTransition, `Motion.Reveal`, haptic), 2 rings (Animatable, `Motion.Draw`, on open and on change), 5 pause row (`Motion.Reveal`). The strip wraps into two rows (four and three) by measured width, not by font scale (superseded by 9.9). RTL: rings Sun to Mon from the right as the locale says; the Canvas arcs mirrored.

**Last 30 days sheet.** Grid of 30 rings, six rows of five, newest first, each with its day number and the "3 of 5" description; tapping a ring opens that day's five ticks under the grid (the existing per-day row, last-30-days.png); paused days with the glyph; nothing counts blanks. This replaces the long card list, which is 30 cards tall.

**Qibla.** Title. "Turn right 58°" in `headlineMedium` on one line; "Facing 60°" under it in `bodyLarge`. The dial, radius as today (QiblaScreen.kt:190 keeps room below), with a thicker soft rim and a rounder needle (DESIGN_SYSTEM §9). Key. Place name small in `onSurfaceVariant` under the key, shown only when the label exists. A compact solid "Details" with a chevron under the place name; expanded, two lines: "About 5024 km from here" (or miles: superseded by 9.5) and "118° from north". Aligned: existing fill, rim, wording and haptic; animation 13 (`animateColorAsState`, `Motion.Standard`). No compass and no location: the existing amber notices above the dial.

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

> **Superseded by `BUILD-INSTRUCTIONS.md` §5** (the one ordered table and its checkpoints). Kept for the reasoning behind the cut order.

Gates stay as signed off: 1.5.0 live by 27 Nov or everything moves after Eid; PR by 11 Dec, submit by 15 Dec (SIGNOFF.md:31-35). Each step is its own droppable set of commits with screenshots for Ali before the next starts.

| # | Step | Depends on | Cut order if short |
|---|---|---|---|
| 0 | `Motion.kt`, `DesignRulesTest` additions (infinite transition ban, tween literals), `ColorContrastTest` sky assertions scaffold | nothing | never |
| 1 | Tracker tab: fourth destination (no usage-count word for it), ticks and tracker line leave Times, today card, Pause and Last 30 days buttons, week rings with partial fill and pause glyph, 30-day grid | the tracker code already on `claude/1-6-0` (`e98b2a6`), reworked (8.1) | never |
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
| **Today** (exists) | compact 4 by 3, large 4 by 4 | As now (~~large adds jama'ah~~ struck by 8.7: no jama'ah on widgets in 1.6) | As now | As now | No change |

Resize: `resizeMode="horizontal|vertical"` as now; the launcher picks the layout by `SizeF` with no app wake (WidgetRenderer.kt:106-108). Below Android 12 each widget keeps its first layout. RTL: the strip's columns run Isha to Fajr from the left, which `AppLocale` layout direction already forces for widgets (HANDOVER lesson 131). Large text: the strip's name and time are sp; at 130 percent or more the strip hides the glyph row, the way the second layouts are dropped today (HANDOVER 1410-1411). Dark and light: `values-night` twins plus the fixed Light and Dark sets, unchanged mechanism; launcher dark mode is the same `values-night` path.

**The sky on widgets.** Four fixed `widget_sky_*` gradient drawables per theme, chosen at refresh by the period; the refresh already fires at each prayer time and at midnight, so the sky changes at the right minute with no new alarm. Sunrise must count as a boundary: the Today rows include it, and I believe `state.next` does (guess; if it does not, add sunrise to `nextRefreshAt`). The sun path on the Next widget: a layered vector of five dots on an arc with the passed segments as separate drawables toggled by `setViewVisibility`, every colour an `@color` so dark mode resolves at draw (lesson 130: a bitmap bakes colours and goes stale). **No "now" dot** on widgets: it would need a redraw every few minutes, which is the battery cost the hero-countdown design rejected (DESIGN_SYSTEM.md:922-927) and the Chronometer exists to avoid. The arc shows which prayer the day has reached, which changes only at prayer times. Mini arcs per prayer on the strip are not copied: our glyphs already say where the sun is (DESIGN_SYSTEM.md:581-584).

**RemoteViews limits that shape this.** No animation of any kind. Only the inflatable view classes (lesson 130). Every property written both ways on every update. Android 12 and later round corners to `system_app_widget_background_radius`; use it for the background shape, with 24dp as the pre-12 value. Dynamic colour (Material You) is not used: the tokens are the brand and `WidgetColorsTest` holds them (DESIGN_SYSTEM.md:934-935). Previews (`previewLayout`) show the base light layout until the first update, accepted already (HANDOVER 1399-1401).

**Contrast test plan.** `WidgetColorsTest` extends to the four skies: every widget ink (name, time, countdown, chip, sublabel) against each stop of each sky in light and dark, and under the sheen on the top stop (the existing worst-case rule), plus the arc's passed and unpassed colours at 3:1 against every stop. Rendered through the real `RemoteViews.apply` path on the emulator in light, dark, Urdu and 130 percent text before any screenshot goes to Ali.

**Where it fits.** Step 8, after the phone skies exist so the widget gradients are the same stops; cut second if time runs short, because the current widgets are already live and liked, and the store screenshot 06 must be retaken whenever they change.

Not verified: any dp figure marked as a pixel reading; whether the activity is edge to edge; whether `WidgetState.next` includes sunrise; the exact fallback behaviour of `HapticFeedbackType.ToggleOn` below API 34; and the "Remove animations" to animator-scale mapping, which one emulator check settles.

---


Same rules: read only, verified against `claude/1-6-0`, guesses marked.

## 6. User journeys and wording

### 6.0 The disclaimer moves to Settings (Ali, 23 Oct 2026)

Where it sits so it is never buried: **the first row of the About group**, above Version (today it is second, SettingsScreen.kt:543-546 after `version` at 520-521). Row: icon Info tile, title **"Disclaimer"** (`about_disclaimer`, exists), subtitle **"A helper, not a religious authority. Please read this once."** (`about_disclaimer_short`, reworded: "Tap to read" is banned "Tap" wording, DESIGN_SYSTEM.md:361). It opens the existing full dialog (SettingsScreen.kt:769-780) with the dua last. The About group itself stays the last group, which is where every Android user looks for "about"; nothing else changes.

What must change when built:
- `MainActivity.kt:294-295` stops showing `FirstRunDisclaimer`; `disclaimerSeen` and `markDisclaimerSeen` go, with a one-line note in the repository saying why the key is retired (a stored flag nobody reads is a trap for the next reader).
- `DisclaimerContentTest`: the test at line 72 ("the short disclaimer after setup ...") is removed with `disclaimer_brief` and `disclaimer_read_full`; the test at line 57 on `disclaimer_body` stays, and a new assertion holds `about_disclaimer_short` to "not a religious authority", so the row's own words still carry point 1 and cannot drift to a blank "Disclaimer".
- HANDOVER §1 (lines 77-96): "once after onboarding as a dialog" becomes "in full in Settings, About, Disclaimer, the first row of that group"; the 18 Oct "short form after setup" block (1032-1042) is marked superseded, with Ali's words quoted. §5.15's four-copies table drops the "Setup dialog, short form" row; three copies remain (in-app full text, watch, `privacy.html#disclaimer`), all unchanged in content. The rule "shown once, not merely available" is explicitly reversed by the owner and recorded in §9 of DESIGN_SYSTEM.md and §11 of HANDOVER with the date.
- CLAUDE.md hard rule: "The religious disclaimer must never be removed, softened, or buried" stays word for word; add "It lives in Settings, About, first row, and is not shown at first launch (owner, 23 Oct 2026)". The dua rule is untouched: still asked once, in the full text, nowhere else.
- Store listings: the full descriptions carry the four points (LISTING-LOCALES.md), so nothing there changes.
- Not religious content, but worth saying to Ali in plain words: the four points still reach every user who opens About, the watch and the policy page, and no longer reach the user who never opens Settings. That is the trade he has chosen.

### 6.1 Journeys

Words in quotes are on screen. "Exists" means the string is in `values/strings.xml` today.

**A. First launch and setup.** Welcome: the mark, "SajdaTime", "Your private prayer companion. Free, always." (exists), the language button top end with the current language's own name, **"Begin"**. Location: "Allow location" (exists), the privacy line, "Or type where you are", "Skip for now and use Makkah" at the foot. School of thought, madhab (Sunni only), calculation method, the usage question, confirm with "Finish". Then Times opens with the sky for the current period, the card counting down, six rows with bells on (the default alert on all five), the Tracker tab visible beside Times. No pop-up. First thing read: the city and the countdown.

**B. A normal day.** Open: the sky says morning, the card says "Next prayer", "Dhuhr 12:46 pm", the countdown, the Now pill on Fajr's row until sunrise, Next on Dhuhr. Glance: done. Bell: tap Dhuhr's bell, it crosses out, a short buzz, the message "Dhuhr alert off" with "Undo" for four seconds; tap it again later and it fills with no message. Tick: tap Tracker, today's five rows, tap Dhuhr's ring, it fills and the tick draws, a short buzz, the week ring for today grows a fifth. Nothing else happens; no streak message unless Show streak is on, and then only a number.

**C. Travelling to a new place.** On opening the app the location updates (foreground only); the town changes, the sky and times with it. If the town name is wrong, tap the city, "Your location" sheet (exists), "Use my current location" or type. Jama'ah lines (if set at home) keep showing: the 25 km hide was cut from 1.6.0 (SIGNOFF.md:74-75), so the user may see a home mosque time abroad; the row says "Jama'ah 1:30 pm" and the user knows where that came from. Guess: acceptable for 1.6.0, revisit with 1.6.1.

**D. Tracking off, then on.** Tracker tab with tracking off: title "My prayers", one card: a switch row "My prayers", "Tick each prayer you pray. Only you can see it." (exists), and under it one line "Nothing is tracked until you switch this on." Flip the switch: the card is replaced by today's five rows, no snackbar (the user is already looking at the result; the UX.md:90 snackbar belonged to the Times placement and goes). Flip it off in Settings: the tab returns to the switch; the record stays, and the Delete row stays in Settings (UX.md:68).

**E. Pausing for a period.** Tracker tab, "Pause": the button row becomes "Tracking is paused" with "Resume"; today's rings show the pause glyph, disabled at full contrast (superseded by 9.7); a message "Paused from today" with "Undo" (and "Yesterday" stays in Settings for the woman who forgot to pause yesterday). The week strip marks each paused day with the pause glyph. "Resume": rows come back for today; paused days keep the glyph and count nothing. No reason is ever asked, no word for why.

**F. A Ramadan day.** Three days before: the quiet notice "Ramadan begins on Monday" (exists) with "Shift the date". Day 1: the card appears under the notice slot: "Ramadan", "Day 1 of 29", "Suhoor ends 5:17 am", "Iftar 5:33 pm", "Iftar in 02:32:51", the fast line, tomorrow's line, "Get the full Ramadan timetable". The Today line's "Timetable" opens the same sheet. The suhoor reminder arrives 60 minutes before Fajr unless changed in Settings. The Tracker tab is unchanged. Times scrolls a little at 823dp (known).

**G. Eid.** The card is gone; the Eid notice: "Eid al-Fitr Mubarak", the body (exists), the footnote "This date is our calculation ... Please follow your mosque." (exists), the X. Nothing animates in.

**H. Qibla, first use.** No location: "Set your location to find the Qibla." (exists) above an empty dial. With location, with a compass: "Turn right 58°", "Facing 60°", the dial turning, the arc shrinking; aligned: "You are now facing the Kaaba" (exists), the dial fills green, one buzz. Calibration: the amber notice "The compass needs calibrating. Hold your phone and move it in a figure of eight a few times." (exists). No compass: "This phone has no compass, so we can give you the angle but cannot point the way." (exists), the needle held at the bearing. "Details" under the key: "About 5024 km from here" and "118° from north".

**I. Saving or viewing the timetable.** Times, "Timetable": the sheet, "Timetable" with "Save as PDF" beside it, the month's rows, today highlighted. Save as PDF: the existing export sheet's choice (Today only, 7 days, This month, Ramadan when offered) collapses to the month on screen plus one button; the file saves and the system share sheet opens as today. In Ramadan the sheet opens on the Ramadan table.

**J. Adding a widget.** Long press the home screen, Widgets, SajdaTime: three previews, "Next prayer" ("The next prayer and its time, with a countdown.", exists), "Today" (exists) and the new **"Today strip"** ("All five prayers in one row, the next one marked."). Placed: the widget shows the sky and times at once (the first update a second later applies the theme, HANDOVER 1399-1401). Tap the town chip: the app opens and refreshes the location.

### 6.2 Wording pass: every new or changed string for 1.6

Tone rules applied: short, warm, no "Tap", no "missed", no reward talk, sentence case, religious names unchanged. Existing 1.6.0 strings on the branch are not repeated.

| Key | English | Where | Care |
|---|---|---|---|
| `nav_tracker` | My prayers | Tab label; `WholeWords` shrink applies (DESIGN_SYSTEM.md:289-295). "Tracker" is colder and does not translate cleanly into Urdu | |
| `tracker_off_line` | Nothing is tracked until you switch this on. | Tracker tab, off | |
| `tracker_pause` | Pause | Tracker tab button | |
| `tracker_resume` | Resume | exists as `settings_tracker_resume`? Reuse `tracker_resume` if present on the branch | |
| `tracker_paused_from_today` | Paused from today | Snackbar | |
| `tracker_ring_a11y` | %1$s, %2$d of 5 prayed | Week ring and month grid, TalkBack | |
| `tracker_ring_paused_a11y` | %1$s, paused | | |
| `tracker_ring_today_a11y` | Today, %1$d of 5 so far | | |
| `action_undo` | Undo | Snackbar action | |
| `bell_on_a11y` | %1$s alert on, notification | Bell content description | |
| `bell_alarm_a11y` | %1$s alert on, alarm | | |
| `bell_off_a11y` | %1$s alert off | | |
| `bell_off_message` | %1$s alert off | Snackbar | |
| `bell_off_message_fajr_ramadan` | Fajr alert off. Suhoor reminder stays on. | Snackbar, Ramadan only | Religious: suhoor is a word every user knows; keep it |
| `action_timetable` | Timetable | Today line button (replaces `action_save_pdf` there; `action_save_pdf` stays for the sheet) | |
| `timetable_title` | Timetable | Sheet | |
| `timetable_month_a11y` | Prayer times for %1$s | Sheet, TalkBack | |
| `qibla_details` | Details | Button | |
| `qibla_distance_km` | About %1$d km from here | | |
| `qibla_distance_miles` | About %1$d miles from here | | |
| `qibla_bearing` | %1$d° from north | | |
| `qibla_subtitle` | (removed from the top; its two facts move to the three strings above) | | |
| `home_sky_a11y` | %1$s period | Sky, decorative otherwise; the dot's description "Now, between %1$s and %2$s" | |
| `about_disclaimer_short` | A helper, not a religious authority. Please read this once. | Settings | Religious: point 1 must stay in these words (test) |
| `widget_strip_desc` | All five prayers in one row, the next one marked. | Widget picker | |
| `widget_strip_name` | Today strip | | |
| `fasts_to_make_up` (1.6.1) | Fasts to make up | Settings row | Religious: never "missed", "owed", "qada" |
| `fasts_to_make_up_desc` | Add one for each fast you still have to make up. Take one off when you make it up. | | |
| `fasts_line` (plural) | %1$d fast to make up / %1$d fasts to make up | Tracker tab | |
| `fasts_made_up_one` | Made up one | Button | |

Changed existing strings: `action_save_pdf` stays "Save as PDF" (sheet only). `settings_tracker_pause_desc` "Your streak waits until you resume." stays. Nothing in `disclaimer_body` changes.

Religious care flags: "My prayers" as a tab name is fine in all five languages (Urdu "میری نمازیں", Turkish "Namazlarım", Indonesian "Salatku", Arabic "صلواتي"; guesses to be checked at translation). "Suhoor" and "Iftar" keep their established local forms (Turkish "sahur", "iftar"; Indonesian "sahur", "buka puasa" is more natural than "iftar", a choice for the translation pass). No string anywhere says a prayer was missed, late or owed.

## 7. Languages as a strength

**What ships.** `values`, `values-ar`, `values-ur`, `values-tr`, `values-in` (five languages; Arabic ships with 1.5.0, PLAN.md:71), every one packaged in the app (CLAUDE.md, lesson 125), the app formats numbers and dates in its own language (HANDOVER §5.11), and goes RTL only when it has the words (§5.16). Pillars ships English and Turkish (PILLARS-STUDY.md:62). That is a real difference for Urdu and Arabic readers, and the store already sells it in eight listing languages (LISTING-LOCALES.md).

**Make it visible in the app.**
- The welcome screen's language button (DESIGN_SYSTEM.md:789-794) already shows the language's own name; keep it. Add nothing to Times: the app being in your language is the message.
- Settings, Language row: subtitle lists the five names in their own scripts when "Phone language" is selected ("English · العربية · اردو · Türkçe · Bahasa Indonesia"), so a user browsing Settings sees the choice exists. One string, no code change beyond the subtitle.
- The Play listing's first screenshot per language shows Times in that language (today the eight screenshots are English only, `docs/store/screenshots/raw/phone`). Retake 01 (Times), 02 (Qibla) and the new Tracker screenshot in Arabic, Urdu, Turkish and Indonesian; the rest reuse English. Play lets each listing language carry its own screenshots; that is the place a Karachi user decides.

**RTL quality of every new piece.**
- Sky and day path: the path runs Fajr at the start edge to Isha at the end edge, so in Arabic and Urdu it runs right to left; mirror the Canvas x axis under `LocalLayoutDirection`, and the sun and moon sit in the header's trailing half, which is the left in RTL. Test in `installRtl` and judge the mirroring, not the prose (CLAUDE.md).
- Rings: the week strip follows the locale's first day and reads from the start edge; arcs sweep clockwise in both directions (a filling clock is not a reading direction), which is a deliberate choice to record.
- Bell: trailing end of the row, so the left in RTL, where the tick sat (UX.md:127).
- Chips and the Undo snackbar: Material mirrors them.
- The Qibla "Turn right 58°" is already mirrored in meaning, not layout: right is right whichever way the text runs; `AutoMirrored` icons only for arrows that mean "next" (three uses today).
- Digits: Arabic and Urdu use their own digit sets through `AppLocale` (§5.11), and the countdown on the card is the first thing to re-measure in Arabic (DESIGN_SYSTEM.md:283-287). The Chronometer on widgets follows the phone's language (lesson 131), accepted.
- Hijri month names come from `hijri_months` arrays per language (values-ur/strings.xml:266-269); the dates sheet and the widget header reuse them; nothing new to translate except the six dated rows (SIGNOFF.md:58).

**Wording that does not translate well, decided now so the translation pass does not improvise.** "Streak" (Urdu and Arabic have no idiom; the plural `tracker_streak` "12 days in a row" already avoids the word, keep that). "Timetable" (Urdu "اوقات نامہ", Turkish "vakit çizelgesi", Indonesian "jadwal"; all are everyday words, fine). "Details" (fine). "Make up" for fasts: Arabic and Urdu have the exact word (qada, قضا), and the English avoids it on purpose; the translations may use it, because in those languages it is the plain term, not a label of guilt. Record this in the translation notes so the five do not drift apart in tone.

**Process.** As CLAUDE.md says: Claude translates, Claude checks, each language's new strings are rendered at 1.5 text on the 320dp frame (Indonesian and Turkish are the longest, UX.md:293), and no screen ever says a translation awaits review.

---

That closes the independent review. Nothing was written to the repo; everything above is for the coordinator to carry into `PLAN.md` §0.1, `DESIGN_SYSTEM.md` §9 and §11, and `HANDOVER.md` when Ali says go. Items still not verified by anyone: the pixel-read dp figures, edge-to-edge drawing, the sunrise widget refresh boundary, the sub-API-34 haptic fallback, and the Remove-animations mapping, each settled by one emulator check.

---

## 8. Gap review 1: resolutions (23 Oct 2026)

`GAP-REVIEW-1.md` is the independent review. Its code claims were spot-checked by Claude Code and hold:
- `MainActivity.kt:179` `enableEdgeToEdge()`
- `WidgetState.kt:83` "Sunrise is never next"
- `PrayerAlarmScheduler.kt:94-107`, where the reminder has its own `prayerReminderFor` set
- `strings.xml:87` `disclaimer_title`
- no `SnackbarHost` in `MainScaffold.kt`

Each gap is resolved below. Where the answer is Ali's, the **recommended default** is written, marked
**[Ali to confirm]**, and listed in 8.18. Builders use the default only after Ali confirms.

**8.1 What is already on the branch.** Four commits predate §0.1. Ali said "Lets do all the decided features
for 1.6" before the design pause:
- `e98b2a6` tracker, with the ticks on the Times rows and the tracker line under the list
- `9cc78bf` Islamic dates sheet
- `9e9102e` jama'ah lines
- `762e622` the 1.6.1 reminder planners, pure logic with no UI

They build the old placement. **They are the base, and are reworked to this plan.** Step 1 becomes: move
the ticks and the tracker line out of `HomeScreen.kt` (`:907-946`, `:216-218`) into the new tab. **No
screenshot from the current build is shown to Ali as the new design.** [Ali to confirm: keep as base.]

**8.2 Privacy page in the first commit that ships tracker data.** Add a "Your prayer record" section to
`docs/privacy.html`, undated until release day:
- what is stored on the phone: the ticks, the pauses, the jama'ah times typed in
- that the save file goes wherever the user puts it
- that Delete removes the record
- that no usage count is sent for the My prayers tab, and that the counted screens stay three

Also fix `privacy.html:124-126` so the "three main screens" sentence is still true: the counted screens stay
three. Add `TrackerPrivacyTest`, which reads `privacy.html` for those phrases. `TrackerRepository.kt:26`
already names it. Release day changes the privacy date with `set-release-date.sh`.

**8.3 Disclaimer move: the full checklist, one commit.**
- In addition to 6.0:
  - remove the `DesignRulesTest.kt:123` exemption
  - change the inventory rows at `DESIGN_SYSTEM.md:410-411` and the sentence at `:546`
  - retitle `disclaimer_title` to "Disclaimer" in five languages
  - change the `privacy.html:247` comment and the `DisclaimerContentTest.kt:149-150,172-173` messages
  - delete `disclaimer_brief` and `disclaimer_read_full` from all five `values*/strings.xml`
- HANDOVER §5.15 line 1046 justified the watch's two points by "shown once at setup on the phone". Rewrite
  it: the watch keeps two points because of its 88-character budget, and the phone's full text is in
  About.
- Ali has accepted this trade in principle. Confirm the retitle in 8.18.

**8.4 Bell.**
- **Reminder.** Tapping the bell off also removes that prayer from `prayerReminderFor`. The message
  says "Dhuhr alert and reminder off". Tapping it on restores both. [Ali to confirm]
- **Restore.** A new `lastAlertStyle` map (prayer to NOTIFY or ALARM, default NOTIFY), with its codec and a
  `SettingsCorruptionTest` case. `alertFor` drops the entry on off (`SettingsRepository.kt:399-401`).
- **Notifications blocked by the phone.** If `POST_NOTIFICATIONS` is denied, tapping a bell on asks for the
  permission (`MainActivity.kt:86-95`). While it stays denied, the bell's description adds "notifications
  are off on this phone" (`bell_blocked_a11y`).
- **Undo snackbar.**
  - One `SnackbarHost` in `MainScaffold`, above the glass bar.
  - It is written with `showSnackbar(actionLabel = ...)`, so no `TextButton` appears in our source.
  - Recorded as control kind G in §4.1.
  - `ColorContrastTest` adds `inverseSurface` / `inverseOnSurface` / `inversePrimary` in both themes.
- **Semantics.** The bell is its own `toggleable` node with `Role.Switch` (the same pattern as `Tick.kt:64-69`, which uses `Role.Checkbox` for a tick), outside the
  row's merged description (`HomeScreen.kt:1029`).
- **Contrast.** `outline` against `primaryContainer`, `surfaceContainerHigh` and `surface` at 3:1 in both
  themes.

**8.5 Timetable.** Decided: the renamed button stays on the Today line (PLAN.md 0.1 is updated to say so,
and §11 gains a row).
- **Layout.** One card per day, not a table: the date on the first line, then the six times in a
  three-by-two grid with prayer names. This fits 320dp, 2.0 text and Urdu digits.
- **Today.** Today's card uses `primaryContainer` plus the word "Today". It does not use
  `surfaceContainerHigh`, which is the sheet's own colour.
- **PDF ranges.** Today only, 7 days and This month are all kept as choices under "Save as PDF".
  [Ali to confirm]
- **Ramadan.** The sheet opens on the Ramadan days, which are cards with suhoor and iftar, while
  `offersTimetable` is true.

**8.6 Tracker tab states.**
- **Isha after midnight.** While `state.current == ISHA` and now is before today's Fajr, the today card's
  first row is "Last night's Isha" (`tracker_last_night_isha`), ticking yesterday's record.
- **No location.** The rings stay tickable with no times shown, and the notice "Set your location to see
  prayer times" sits above them.
- **Read-only record from a newer version.** The tab shows the read-only notice and disables the rings.
- **Loading.** Nothing is drawn until `state.loading` is false. The switch card never flashes.
- **Before tracking began.** A new `tracker_started` date key. The 30-day grid draws no ring for days
  before it. [Ali to confirm]
- **Week strip.** Rings are 36dp, or 32dp when the width is under 340dp. At 2.0 text the strip wraps into
  two rows (4 and 3). Rings in the strip are not tappable; the grid's are, with 48dp cells.
- **First day of the week.** `WeekFields.of(appLocale)` from the app's language (`AppLocale`), with day
  letters from `DayOfWeek.getDisplayName(TextStyle.NARROW, appLocale)`. [Ali to confirm]
- **Usage counts.** The PRAYERS destination (9.10) maps to no event in the scaffold's `when`. `UsageCountsTest`
  pins `Screen.entries.size == 3`.

**8.7 Jama'ah.**
- The 25 km hide is cut. PLAN.md §0 is annotated.
- Jama'ah stays off the widgets in 1.6, as signed off; DETAILED-PLAN 5's large-widget jama'ah line is
  struck. [Ali to confirm]
- `jamaah_out_of_window` becomes the signed-off sentence: "Your Asr jama'ah time is before Asr begins.
  Check your mosque's times." There is a matching after-the-window form, and five languages.

**8.8 1.6.1 code on the branch.** Kept, inert. A test asserts that no composable reads `jamaahReminderMinutes`
or `fastReminders`. [Ali to confirm] `RequestCodeTest` covers the 30,000 band now, in step 0.

**8.9 Sky.**
- **Period** is a pure function of `state.current`: FAJR is dawn; null or DHUHR is day; ASR or MAGHRIB is
  evening; ISHA is night. It uses the ViewModel's existing sunrise trigger (`SajdaViewModel.kt:257-267`).
  Tested by `SkyPeriodTest`.
- **Edge to edge** is verified. The known cold-start light frame now shows before a dark sky; that is
  accepted and noted in DESIGN_SYSTEM §5.
- **Existing tween literals.** `SettingsList.kt:277,352` and `Notice.kt:117` move to `Motion.Standard`. (Superseded: BUILD-INSTRUCTIONS §5 row 0, the chevrons go to `Quick`.)
  `QiblaScreen.kt:209` (120 ms, which follows the compass) is exempt by name in `DesignRulesTest`, with a
  comment.
- **The day path** draws in once per process, not on every tab return. [Ali to confirm]
- **Struck:** "Changing day" animation (PLAN.md 0.1 item 5). There is no day stepper.
- **Dark sky variants stay OPEN** until Ali sees four pairs. DESIGN_SYSTEM §11 is corrected.

**8.10 Qibla.**
- The dial size stays as today. DESIGN_SYSTEM §9 is corrected; it said "grows".
- Alignment keeps the shipped `qibla_facing` "You are now facing the Kaaba".
- No location keeps today's single sentence, with no dial.
- "Details" adds a `QiblaScreen.kt` row to the §4.1 inventory.
- **Miles.** Use `LocaleData.getMeasurementSystem(ULocale.forLocale(phoneRegionLocale))` on API 28+.
  On 24 to 27 the fallback is the country list US, LR, MM and GB. The number goes through the app's digit
  formatting (§5.11). Tested by `MeasurementSystemTest`. [Ali to confirm: phone's region]
- Remove `qibla_subtitle` from all five languages.

**8.11 Widgets.**
- `WidgetRefreshScheduler.nextRefreshAt` gains a sunrise boundary, tested in `WidgetRefreshSchedulerTest`.
- **The new strip provider needs:**
  - a manifest receiver
  - `widget_today_strip_info.xml` with `previewLayout`
  - `WidgetManifestTest` raised to three providers
  - `WidgetResourcesTest` and `WidgetThemeTest` cases
- Sky stops are in `Color.kt`, `colors.xml` and `values-night/colors.xml`, with Kotlin twins for
  `WidgetColorsTest`.
- At 130% text and above, the strip hides the glyph row. At 160% it hides the prayer names and keeps the
  times, with the names in the content description.

**8.12 Font scale.** Check every changed screen at 1.5 **and 2.0**, on 320dp and 823dp.

**8.13 Wording additions.**
- **New keys:**
  - the period names `sky_dawn`, `sky_day`, `sky_evening`, `sky_night`
  - `sky_now_a11y` ("Now, between %1$s and %2$s")
  - `tracker_last_night_isha`
  - `widget_strip_label` and `widget_strip_desc`
  - `bell_blocked_a11y`
  - `settings_language_all`, which is `translatable="false"`, bidi-isolated with `Bidi.kt`
- **Fixes:**
  - Use `action_resume` (it exists), not `tracker_resume`.
  - `tracker_ring_a11y` becomes `<plurals>`.
  - `TrackerWordingTest` widens its prefixes to `nav_tracker`, `bell_`, `tracker_` and `fasts_`.
  - `bell_off_message_fajr_ramadan` shows only in the Ramadan season with `suhoorReminderMinutes > 0`.
- **Religious terms fixed now** (translation notes, kept with the translations):
  - Jama'ah: Arabic الجماعة, Urdu جماعت, Turkish cemaat, Indonesian berjamaah
  - Jumu'ah: Arabic الجمعة, Urdu جمعہ, Turkish Cuma, Indonesian Jumat
  - Suhoor: Arabic السحور, Urdu سحری, Turkish sahur, Indonesian sahur
  - Iftar: Arabic الإفطار, Urdu افطار, Turkish iftar, Indonesian buka puasa
  - Undo: Arabic تراجع, Urdu واپس, Turkish Geri al, Indonesian Urungkan
  - Timetable: Arabic جدول المواقيت, Urdu اوقات نامہ, Turkish Vakit çizelgesi, Indonesian Jadwal

**8.14 Accessibility.**
- The sky artwork and the now-dot use `clearAndSetSemantics {}`, because the card already speaks the period.
- `LargeTextAndTalkBackDeviceTest` gains the My prayers tab, a bell and Qibla Details.

**8.15 Tablets and landscape.** No tablet layout in 1.6. Content is capped at 600dp wide and centred; the sky
stays full width.

**8.16 Tests and gate, consolidated.**
- **Changed:** `DesignRulesTest` (inventory rows, infinite-transition ban, `tween` literal ban with the
  one exemption), `ColorContrastTest` (skies 72 plus 24, inverse pairs, bell), `WidgetColorsTest`,
  `WidgetManifestTest`, `WidgetResourcesTest`, `WidgetThemeTest`, `WidgetStateTest`,
  `WidgetRefreshSchedulerTest`, `TrackerWordingTest`, `DisclaimerContentTest`, `RequestCodeTest`,
  `UsageCountsTest`, `TranslationIntegrityTest`.
- **New:** `InertReminderSettingsTest`, `TrackerPrivacyTest`, `SkyPeriodTest`, `WeekStripTest`, `AlertRestoreTest`,
  `MeasurementSystemTest`.
- **Device checks:**
  - animator scale 0
  - the strip on a real launcher in light, dark, Urdu and 130%
  - the My prayers tab at 2.0
  - `LargeTextAndTalkBackDeviceTest`
  - the on-device suite run with `am instrument` after `appwidget grantbind`
- **Gate:** `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`, then both emulators.
- **Store screenshots (about 20):**
  - 01, 02, a new My prayers shot and 06, in English
  - 01, 02 and My prayers in Arabic, Urdu, Turkish and Indonesian
- **Release:** `release-1.6.0.sh` with the new counts, and `set-release-date.sh`.

**8.17 Delivery rule. AMENDED by Ali (Q25): late approval allowed; see BUILD-INSTRUCTIONS §2.** Original: Anything Ali has not approved from screenshots by **4 Dec 2026** ships after Eid, so
the Ramadan build is the approved subset. 1.5.0 must be live by 27 Nov (SIGNOFF.md), or all of 1.6.0 moves
after Eid. Underestimated, so start them early: the widgets (verified on a real launcher), the month sheet,
and the screenshot rounds. [Ali to confirm]

**8.18 Questions for Ali. ANSWERED on 23 Oct 2026: see BUILD-INSTRUCTIONS §3 for the answers, which win.**
1. Keep the four built commits as the base and rework them: **yes**.
2. The bell off also turns off that prayer's "N minutes before" reminder: **yes**.
3. Dark skies: four dark variants, **decided from four screenshot pairs**.
4. Keep Today, 7 days and Month as PDF choices inside the Timetable sheet: **yes**.
5. Jama'ah on widgets: **out of 1.6**.
6. Tab name: **"My prayers"**.
7. Miles by the phone's region (US, UK, Liberia, Myanmar), km elsewhere, no setting: **yes**.
8. Retitle the disclaimer dialog "Disclaimer", accepting that watch-only readers see two points: **yes**.
9. The 30-day grid draws nothing for days before tracking began: **yes**.
10. The 1.6.1 rules stay on the branch switched off: **yes**.
11. Check every screen at 2.0 text as well as 1.5: **yes**.
12. The day path draws once per app open: **yes**.
13. The week starts by the app's language: **yes**.
14. "What's new" in all nine listing languages: **yes**.
15. Hard cut on 4 Dec for anything not approved: **yes**.

---

## 9. Gap review 2: resolutions (23 Oct 2026)

`GAP-REVIEW-2.md` checked section 8 and found new gaps. Gaps 1 and 2 (the reading order and one ordered
build table) are fixed by the READING ORDER line at the top of every plan file and by `BUILD-INSTRUCTIONS.md`.
The rest are resolved here. Questions for Ali are added to 8.18 as 16 to 20.

**9.1 The branch state is in the cold-start documents.** HANDOVER §11 STATE OF PLAY and `PROJECT_BRIEF.md` each
gain a dated line naming `claude/1-6-0`, its four feature commits and `BUILD-INSTRUCTIONS.md` (done in the same
commit as this section).

**9.2 Privacy text, fixed now** (8.2 gave only headings). The new section in `docs/privacy.html`, "Your prayer
record", undated until release day, says exactly:
- "If you switch on My prayers, the prayers you tick, the days you pause and any jama'ah times you type in are
  kept on this phone only. Nothing about them is sent to us or to anyone."
- "If you save your prayer record, the file goes wherever you choose. It contains your ticks and any paused
  days. Anyone you give it to can read them."
- "Delete my prayer record in Settings removes it from this phone."
- "The My prayers screen is never counted, even if you have said yes to usage counts."

`TrackerPrivacyTest` (new) finds the four phrases "kept on this phone only", "goes wherever you choose",
"removes it from this phone" and "never counted". Move the backup rule check from `TrackerWordingTest.kt:65`
into it, so `TrackerRepository.kt:26`'s pointer is true. **It is the next commit on the branch, before any
other 1.6 code** (the tracker data shipped in `e98b2a6` without it).

**9.3 Disclaimer checklist additions** (to 8.3):
- `README.md:162` ("shown in the app on first run")
- `HANDOVER.md:456` ("shown once automatically")
- delete `action_understood` from all five languages if no other use remains (grep first)
- delete the `FirstRunDisclaimer` composable (`MainActivity.kt:442`)
- **reword `about_disclaimer_short`** to "A helper, not a religious authority. Worth reading." ("Please read
  this once" reads as a one-off on a permanent row). `DisclaimerContentTest` holds "not a religious authority".

**9.4 Bell and reminder, corrected** (replaces the reminder line in 8.4).
- New key `bell_removed_reminder: Set<PrayerSlot>`, with a codec and a `SettingsCorruptionTest` case.
- **Bell off.** If that prayer is in `prayerReminderFor`, remove it and add it to `bell_removed_reminder`.
- **Bell on.** Restore the reminder only if the prayer is in `bell_removed_reminder`, then clear it from there.
- **Messages:** `bell_off_message` "%1$s alert off" when no reminder changed, and `bell_off_reminder_message`
  "%1$s alert and reminder off" when one did. The Ramadan Fajr form is kept as in 8.13.
- **The Reminder dialog** reads `prayerReminderFor`, so it always shows the true state. Changing the reminder
  there clears that prayer from `bell_removed_reminder`.
- `AlertRestoreTest` covers: reminder already off before the bell; reminder on; style ALARM restored; a
  restore after the dialog changed it.

**9.5 Phone region for miles** (replaces the region source in 8.10).
- `MeasurementSystem.kt`, one function:
  `Resources.getSystem().configuration.locales[0].country`. That is the phone's own region, unwrapped by
  `AppLocale`. `Locale.getDefault()` is banned by `LocaleDisciplineTest.kt:163`.
- **Rule.** API 28+ uses `LocaleData.getMeasurementSystem(ULocale("und", country))`. Below 28, the fallback is
  the country in {US, GB, LR, MM}.
- **Distance only:** GB counts as miles.
- `MeasurementSystemTest` covers US, GB, LR, MM, PK and an empty country (km).

**9.6 The sky has one voice** (replaces 8.14's first bullet).
- The header artwork is one spoken node, `sky_now_a11y`: "Now %1$s, between %2$s and %3$s", with the period
  name from `sky_dawn` / `sky_day` / `sky_evening` / `sky_night`.
- The dots, path, sun and moon inside it are `clearAndSetSemantics {}`.
- `home_sky_a11y` is deleted.

**9.7 A paused prayer looks and sounds paused.**
- `Tick.kt` draws `TickState.PAUSED` as the pause glyph inside a disabled ring at full contrast. Pausing is
  not a failure, so it does not fade.
- New `tracker_state_paused` "paused" in the tick's state description.
- The today card's rings are disabled while paused.
- `WeekStripTest` and the device test cover it.

**9.8 Timetable sheet, completed** (to 8.5).
- **(a) Ramadan cards.** The approved Ramadan table becoming cards is a visible change: question 17. Until it
  is answered, the Ramadan view keeps its approved table (`RamadanTimetableSheet.kt:131-140`).
- **(b) "Month"** is the calendar month that contains today, the same as the PDF's `THIS_MONTH`. The sheet
  opens scrolled to today.
- **(c) No jama'ah lines** in the cards.
- **(d) Records.** DESIGN_SYSTEM §11 gains row 1.6-timetable. The §4.1 inventory row 412 ("Save as PDF, Today
  line") becomes "Timetable, Today line", with a new row "Save as PDF, Timetable sheet header".

**9.9 Week strip wrap.** The strip wraps by measured width: if seven rings at 36dp plus 8dp gaps do not fit, use
32dp; if that does not fit, two rows of four and three. Font scale is not the trigger.

**9.10 The tab, written down.**
- Order: Times, Qibla, **My prayers**, Settings.
- `Destination.PRAYERS(R.string.nav_tracker, Icons.Rounded.TaskAlt, Icons.Outlined.TaskAlt)`.
- New screen file `ui/prayers/MyPrayersScreen.kt` (added to the §4.1 inventory), keeping `ui/home/MyPrayers.kt`
  for the shared row and sheet.
- The `SnackbarHost` sits in `MainScaffold`'s `Box` with bottom padding `LocalBottomBarClearance`, above the
  glass bar.
- The `tween(` literal ban exempts `ui/theme/Motion.kt` and `QiblaScreen.kt`'s dial (8.9).
- The usage-count `when` maps PRAYERS to nothing.

**9.11 `tracker_started`** (superseded by STEP-1-DESIGN 13: reset on each switch on unless ticks in the last 30 days, moved back by an earlier tick or a loaded file) lives in the tracker DataStore (`TrackerRepository`), so Delete clears it with the record. It is the earlier of the switch-on date and the earliest stored record. It is re-derived
after Load and cleared by Delete. Tested by the tracker codec test.

**9.12 Jama'ah time zone guard. AMENDED by Ali's answer (BUILD-INSTRUCTIONS §3 Q13):** while the phone's zone
differs, the typed jama'ah lines are **hidden** (not replaced by a "check" line), and the prayer times shown are
the current location's, as always. Original text:
- Store the zone id with the jama'ah rules: one more key, codec v2, and v1 still reads.
- When the phone's zone differs from it, every fixed jama'ah line shows `jamaah_check` instead of the time.
- No coordinates are stored, which is why the 25 km hide was cut.
- Question 16.

**9.13 Small items.**
- Use the existing `action_pause` (not `tracker_pause`).
- The day path recomposes per minute (`derivedStateOf { now.epochSecond / 60 }`), not per second.
- **Store shots:** retake 03 (Ramadan, gains the sky) and 05 (Settings) too.
- **The Language subtitle** (§7) replaces one Ali chose on 19 Oct (DESIGN_SYSTEM:794): question 18.
- **The mosque door**, signed off as "the one real design question", is in 8.18 as question 19.
- **Tablets.** The 600dp cap (8.15) is a visible change for tablet users: question 20.
- **The 1.6.0 release script** asserts phone `versionCode 10 / 1.6.0` and the watch unchanged at `1002 / 1.5.0`.
- **Tests added to 8.16:**
  - `SettingsCorruptionTest` cases for `lastAlertStyle` and `bell_removed_reminder`; a tracker codec case for `tracker_started`
  - `RamadanPdfDeviceTest` and `ScreenSeedDeviceTest`, which find "Save as PDF" by label
  - `SettingsSemanticsDeviceTest` if it pins rows
- **Docs:** `PROJECT_BRIEF.md` changes in every commit that changes what is live or next.
- **CLAUDE.md.** Edit the 1.6 change on this branch's copy, which already differs from `main`, and merge over
  `main` after 1.5.0.

**8.18 additions, questions 16 to 20:**
16. Show "Check your jama'ah time" instead of a typed time when the phone is in a different time zone from
    where it was typed: **yes**.
17. The Ramadan timetable becomes day cards like the month view (easier on a small phone), replacing the table
    you approved: **yes, from a screenshot**.
18. The Language row's subtitle lists the five languages in their own scripts, replacing "Phone language ·
    English": **yes, from a screenshot**.
19. "Different from your mosque?" button under the times: keep as is (**recommended**), with jama'ah times
    reached from each prayer row.
20. On tablets, cap the content at 600dp wide and centre it: **yes, from a screenshot**.
