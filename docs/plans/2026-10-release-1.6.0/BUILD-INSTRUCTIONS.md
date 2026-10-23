# SajdaTime 1.6: build instructions for any model, starting cold

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file).** Start with this file. Where files
> disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then
> §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then
> `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not
> instructions.

Written 23 Oct 2026 by Claude Code (Opus 5.5) at Ali's request: "write detailed instructions so other models can
also understand, follow it and know the scope, including development and testing guidance and animation, so a new
session picks everything up." This file is self-contained enough to start work. It points to the detail rather
than repeating it.

---

## 0. Before anything: the status and the one rule

- **Nothing in this plan may be built until Ali says "go".** He said: plan every detail meticulously first.
  When he says go, follow §5 in order.
- **Where everything is.** Branch `claude/1-6-0`, worktree `/Users/aliimrankhan/Developer/SajdaTime-1-6-0`.
  - 1.5.0 is **not released yet**; it is on `claude/quiet-glass` (PR #15), and Ali picks its day.
  - Never merge into `main`. Never push to `main`.
  - Never merge `claude/analytics-default-on-1-4-0`.
- **Already on the branch, built before the design pause, and reworked by this plan** (`DETAILED-PLAN.md` 8.1):
  - `e98b2a6` tracker: ticks on the Times rows, the tracker line under the list. Both move to a tab.
  - `9cc78bf` Islamic dates sheet: kept.
  - `9e9102e` jama'ah lines: kept, with the wording fix in 8.7 and the zone guard in 9.12.
  - `762e622` 1.6.1 reminder planners: pure, inert, kept switched off (8.8).
- **Ali's open answers.** 20 questions are listed in §3 with recommended defaults. **A step that depends on an
  unanswered question stops at that point and asks him.** Silence is never a yes (DESIGN_SYSTEM §0.2).
- **Memory.** Every session reads GBrain `projects/sajdatime` first (`recall` or `context_pack`) and writes what
  changed at the end. The 1.6 plan is recorded there too.

## 1. What 1.6 is, in one paragraph

SajdaTime is a free, ad-free, privacy-first Muslim prayer times and Qibla app for Android, widgets and Wear OS:
a charity, *sadaqah jariyah*. In 1.6 the owner wants the app to **look and feel modern, calm and easy to
understand, with every function where the user needs it rather than in Settings.** UI and UX decide the scope.
The reference for that thinking is The Pillars app (`PILLARS-STUDY.md`), but **our green theme and
`docs/DESIGN_SYSTEM.md` stay**. Five languages (English, Arabic, Urdu, Turkish, Indonesian) are a strength to
show. The release has to be before Ramadan 1448 (about 8 Feb 2027).

## 2. Scope

**In 1.6.0, in build order (§5):**
1. **"My prayers" tab.** It is the fourth tab: Times, Qibla, My prayers, Settings.
   - It holds today's ticks, a Pause button and a "Last 30 days" button.
   - The week rings fill partly and show a pause glyph on paused days.
   - "Last 30 days" opens a 30-day ring grid.
   - When tracking is off, the tab shows the switch to turn it on. The tab is always visible.
2. **Islamic dates sheet,** opened from the Hijri date. It is built already.
3. **A one-tap bell on each prayer row,** with an Undo message.
4. **"Timetable" button** on the Today line. It opens a month sheet of day cards, with Save as PDF inside, and
   shows the Ramadan timetable while Ramadan is offered.
5. **Four fixed skies (dawn, day, evening, night)** as the backdrop behind the city, the date and the
   **unchanged** current-prayer card. Artwork in the header shows the time of day.
6. **Qibla restyle.**
   - The direction sentence comes first.
   - The place name is optional and small.
   - Distance and bearing sit behind a labelled "Details" button, in km or miles.
   - The key stays.
7. **Jama'ah lines** on Times. They are built; they get the wording fix and the zone guard.
8. **Widgets:** a new 4 by 1 "Today strip", and the four skies on the widgets.
9. **The disclaimer moves to Settings,** About, as its first row. It is no longer shown at first launch: the
   owner's decision of 23 Oct 2026.
10. **Languages made visible:**
    - the Language row lists the five languages in their own scripts
    - store screenshots in each language
11. **The privacy page section "Your prayer record".**
12. **Animation** throughout (§6), with Compose only.

**1.6.1, after Eid:**
- the jama'ah reminder and the Sunnah fast reminders (the planners are already built)
- "Fasts to make up": one count, no reasons, no dates (`DETAILED-PLAN.md` §3)

**Never (rejected, with reasons in `PILLARS-STUDY.md` and `PLAN.md` §0):**
- **Need the network or a server:** maps and walking routes, weather, live "people praying" counters.
- **Against our rules or principles:**
  - app blocking (PrayFirst)
  - share or rate prompts
  - premium features
  - a tasbeeh counter
  - "featured in" logos
- **Rejected on our own judgement:**
  - unlabelled icon buttons side by side
  - faded past rows
  - a day stepper
  - endless animation
  - Lottie, Rive or Haze
  - fasting reasons such as period or pregnancy, which are health data

**Cut order if time runs short** (from `DETAILED-PLAN.md` §4, last cut first): the Fasts count (already
1.6.1), then widgets, then jama'ah lines, then the skies, then the Qibla restyle, then the Timetable, and the
bell last. The tab, the dates and step 0 are never cut. **Anything Ali has not approved from screenshots by 4 Dec
2026 ships after Eid** (8.17). If 1.5.0 is not live by 27 Nov, all of 1.6.0 ships after Eid.

## 3. Decisions, and the questions still open

**Decided by Ali on 23 Oct 2026** (his words are in `PLAN.md` §0.1, and the reasons are in DESIGN_SYSTEM §9):

| Decision | Why |
|---|---|
| Keep our green theme; Pillars is a reference for layout thinking only | Brand and tested contrast |
| The sky does not replace the green or light current-prayer card | "Keep it consistent": the card is where the current prayer and time left are read |
| Four **fixed** skies, an animated change, artwork showing the time of day | A fixed gradient can be contrast-tested; a moving one cannot |
| One-tap bell per row, with Undo, sounds staying in Settings | A feature with nothing added; declutters Settings |
| Labelled compact solid buttons, never icon buttons side by side | Easy to understand, no mis-taps (DESIGN_SYSTEM §10) |
| Qibla: direction first, Details button, km or miles, key kept | Fewer numbers, nothing lost |
| The Tracker tab is always visible, with the switch shown when tracking is off | The user can always turn it back on |
| Skies and the Qibla restyle are in 1.6 if possible | Ali: part of 1.6, not deferred by default |
| Missed fasts become one count after Ramadan, with no reasons | The research: users value the number; reasons are health data |
| The disclaimer goes in Settings only, not at first launch | "My hard rules have evolved, based on user behaviour and more data" |
| Animation with Compose's own APIs; no Framer, Lottie or Rive | Framer is web only; no new dependency is needed |
| Widgets follow Pillars' UX with our features | Ali, 23 Oct |
| Plan meticulously first, then two independent gap reviews | Ali, 23 Oct |

**Open: ask Ali, from screenshots where visual.** The recommended default is in bold. Full text is in
`DETAILED-PLAN.md` 8.18 and §9.
1. Keep the four built commits as the base and rework them: **yes**.
2. The bell off also turns off that prayer's "minutes before" reminder (restored exactly on): **yes**.
3. Dark skies: dark variants of all four (his lean was the same images), **decided from four screenshot pairs**.
4. Keep Today, 7 days and Month as PDF choices: **yes**.
5. Jama'ah on widgets: **out of 1.6**.
6. Tab name: **"My prayers"**.
7. Miles for the US, UK, Liberia and Myanmar by the phone's region: **yes**.
8. Retitle the disclaimer dialog "Disclaimer"; watch-only readers see two points: **yes**.
9. No rings for days before tracking began: **yes**.
10. The 1.6.1 rules stay on the branch, switched off: **yes**.
11. Check every screen at text size 2.0 as well as 1.5: **yes**.
12. The day path draws once per app open: **yes**.
13. The week starts by the app's language: **yes**.
14. "What's new" in all nine listing languages: **yes**.
15. A hard cut on 4 Dec: **yes**.
16. The jama'ah time-zone guard: **yes**.
17. The Ramadan timetable becomes day cards: **from a screenshot**.
18. The Language row subtitle lists five scripts: **from a screenshot**.
19. Keep "Different from your mosque?" as is: **yes**.
20. Tablet content capped at 600dp: **from a screenshot**.

## 4. Hard rules that never bend (from `CLAUDE.md`; read it in full)

- **Network and accounts.**
  - No network to calculate times.
  - No server of our own, no accounts, no ads.
  - No new usage-count event, parameter or property. **The My prayers tab sends nothing.**
  - No in-app rate or share prompt.
- **Location.** Coarse, foreground only, never sent by us. Any data-handling change updates
  `docs/privacy.html` in the same commit. Do not date privacy.html before release day.
- **The disclaimer.** Never removed or softened. From 1.6 it lives in Settings, About, first row (owner's
  change). The dua request appears only at the end of the full text.
- **The signing key.** Never generate, hold, request or view it. Never commit `*.jks`, `*.keystore` or
  `keystore.properties`. Never open, read, copy or delete `~/.gradle/sajdatime-keystore.properties.backup`;
  only `ls` it.
- **The watch.** Never ask Ali to test it. The watch app is unchanged in 1.6.
- **Ali's buttons.** Stop before any submit, publish, purchase or consent button; Ali presses those.
- **Translations.** Claude translates and checks; never ask for a native reviewer. Every language stays
  packaged (`bundle.language.enableSplit = false`). Indonesian is `values-in`.
- **Colours** come only from DESIGN_SYSTEM tokens, as Material 3 roles. `ColorContrastTest` must pass.
- **Ali's phone.** Install with `ANDROID_SERIAL=<serial> ./gradlew installSideload`, never `installDebug`.
  Never enter his PIN or any credential, and put back anything changed on it.
- **Ponytail.** Stdlib and platform first, no speculative abstraction, the shortest working diff. Mark
  deliberate simplifications with `ponytail:`.

## 5. Build sequence: one ordered table

Each step is its own set of commits. **Each ends with emulator screenshots of every changed screen in light,
dark, Urdu RTL, text size 1.5 and 2.0, at 823dp (S23 Ultra frame) and 320dp, shown to Ali.** The next visual
step starts only after his yes. Section numbers refer to `DETAILED-PLAN.md`.

| # | Step | Main files | Strings (5 languages each) | Tests | Docs | Ends with |
|---|---|---|---|---|---|---|
| P | **Privacy first** (9.2) | `docs/privacy.html` | none | new `TrackerPrivacyTest` (moves the backup check from `TrackerWordingTest.kt:65`) | privacy page | test green |
| 0 | **Foundations** (§1, 8.9, 9.10) | new `ui/theme/Motion.kt`; existing `tween` literals moved (`SettingsList.kt:277,352`, `Notice.kt:117`) | none | `DesignRulesTest` (ban `rememberInfiniteTransition`; ban number literals in `tween(` except `Motion.kt` and the Qibla dial); `RequestCodeTest` adds the 30,000 band; inert-1.6.1 test (8.8) | DESIGN_SYSTEM §2 or §8 (motion tokens) | tests green |
| 1 | **My prayers tab** (2.4, 8.6, 9.7, 9.9, 9.10, 9.11) | `MainScaffold.kt` (`Destination.PRAYERS`, `TaskAlt` icons, no usage word, `SnackbarHost`); new `ui/prayers/MyPrayersScreen.kt`; ticks and tracker line out of `HomeScreen.kt` (`:907-946`, `:216-218`); `Tick.kt` (PAUSED); `ui/home/MyPrayers.kt` (30-day grid); `SettingsRepository.kt` (`tracker_started`) | `nav_tracker`, `tracker_off_line`, `tracker_paused_from_today`, `tracker_ring_a11y` (plurals), `tracker_ring_paused_a11y`, `tracker_ring_today_a11y`, `tracker_state_paused`, `tracker_last_night_isha`, `action_undo`; reuse `action_pause` and `action_resume` | `WeekStripTest`, `UsageCountsTest` (3 screens), `TrackerWordingTest` (wider prefixes), `SettingsCorruptionTest`, `DesignRulesTest` inventory, `LargeTextAndTalkBackDeviceTest` | DESIGN_SYSTEM §4.1 inventory, §9; HANDOVER | screenshots to Ali |
| 2 | **Islamic dates sheet** (built) | check only | none | existing | none | screenshots |
| 3 | **Bell with Undo** (8.4, 9.4) | `HomeScreen.kt` `PrayerRow` (own `toggleable`, `Role.Switch`); `SettingsRepository.kt` (`lastAlertStyle`, `bell_removed_reminder`); `MainScaffold.kt` snackbar; `MainActivity.kt` permission request | `bell_on_a11y`, `bell_alarm_a11y`, `bell_off_a11y`, `bell_blocked_a11y`, `bell_off_message`, `bell_off_reminder_message`, `bell_off_message_fajr_ramadan` | `AlertRestoreTest`, `ColorContrastTest` (inverse pairs, `outline` on 3 row backgrounds), `SettingsCorruptionTest`, `DesignRulesTest` (kind G) | DESIGN_SYSTEM §4.1 kind G | screenshots |
| 4 | **Timetable** (8.5, 9.8) | `HomeScreen.kt` Today line; new month sheet (day cards, opens on today, `primaryContainer` + "Today"); `RamadanTimetableSheet.kt` kept until Q17 | `action_timetable`, `timetable_title`, `timetable_month_heading`, `timetable_month_a11y` | `RamadanPdfRangeTest`, `RamadanPdfDeviceTest`, `ScreenSeedDeviceTest` (labels) | DESIGN_SYSTEM §11 row and inventory rows | screenshots |
| 5 | **Skies** (2.2, 2.3, 8.9, 9.6) | `Color.kt` sky stops (light and dark); `HomeScreen.kt` header backdrop (option D), artwork, `SkyPeriod` pure function, 600dp cap (Q20) | `sky_dawn`, `sky_day`, `sky_evening`, `sky_night`, `sky_now_a11y` | `ColorContrastTest` (72 text plus 24 non-text), `SkyPeriodTest`; emulator check that animator scale 0 ends animations | DESIGN_SYSTEM §1 (the four skies), §9 | 4 light plus 4 dark pairs to Ali (Q3) |
| 6 | **Qibla** (2.4, 8.10, 9.5) | `QiblaScreen.kt`; new `MeasurementSystem.kt` | `qibla_details`, `qibla_distance_km`, `qibla_distance_miles`, `qibla_bearing`; remove `qibla_subtitle` | `MeasurementSystemTest`, `DesignRulesTest` inventory | DESIGN_SYSTEM inventory | screenshots |
| 7 | **Jama'ah** (8.7, 9.12) | `JamaahTimes.kt` (codec v2, zone id); `HomeScreen.kt` lines | `jamaah_out_of_window` (fixed wording); before and after forms | `JamaahPlanTest`, codec test | none | screenshots |
| 8 | **Widgets** (§5, 8.11) | new `TodayStripWidgetProvider`, `widget_today_strip_info.xml`, layouts; sky drawables; `WidgetRefreshScheduler.nextRefreshAt` (sunrise); `colors.xml` and `values-night` | `widget_strip_label`, `widget_strip_desc` | `WidgetManifestTest` (3 providers), `WidgetResourcesTest`, `WidgetThemeTest`, `WidgetColorsTest`, `WidgetStateTest`, `WidgetRefreshSchedulerTest`, `WidgetDeviceTest` | DESIGN_SYSTEM widgets section | real launcher in light, dark, Urdu and 130% |
| 9 | **Disclaimer move** (6.0, 8.3, 9.3) | `MainActivity.kt` (remove `FirstRunDisclaimer`, `disclaimerSeen`); `SettingsScreen.kt` About, first row; `README.md:162` | `about_disclaimer_short` "A helper, not a religious authority. Worth reading."; `disclaimer_title` "Disclaimer"; delete `disclaimer_brief`, `disclaimer_read_full`, `action_understood` if unused | `DisclaimerContentTest`, `DesignRulesTest` (remove `:123` exemption), `TranslationIntegrityTest` | `CLAUDE.md` hard rule rewritten on this branch; HANDOVER §1, §5.15, :456; DESIGN_SYSTEM :410-411, :546, §9 | screenshot of Settings |
| 10 | **Languages** (§7, 8.13) | Language row subtitle (`settings_language_all`, untranslatable, bidi-isolated) | as listed | `TranslationIntegrityTest`; RTL with `./gradlew installRtl` | translation notes (8.13 religious terms) | screenshot (Q18) |
| 11 | **Release** (8.16) | `versionCode 10`, `1.6.0`; watch unchanged at `1002 / 1.5.0`; `docs/release-tools/release-1.6.0.sh` | "What's new" in nine languages | full gate | HANDOVER §11, PROJECT_BRIEF, RELEASE notes | about 20 store screenshots (01, 02, 03, 05, 06 and My prayers; 01, 02 and My prayers in four more languages) |
| 1.6.1 | Reminders and the Fasts count | `PrayerAlarmScheduler` (bands 40,000 and 50,000), Settings rows | `fasts_*` and reminder strings | `RequestCodeTest`, `ExtraReminderPlanTest`, `FastingDaysTest` | privacy page sentence | after Eid |

**Checkpoints:**
- **20 Nov:** steps P, 0, 1 and 3 on the emulator.
- **4 Dec:** steps 4 to 8 shown to Ali. Anything not approved ships after Eid.
- **11 Dec:** PR.
- **15 Dec:** submit.

## 6. Animation: the one table

Tools: Compose only (`DETAILED-PLAN.md` §1).
- **Durations** come from `ui/theme/Motion.kt`: `Quick` 150, `Standard` 250, `Reveal` 300 and `Draw` 400 ms,
  with easing `FastOutSlowInEasing`. No other numbers.
- **Haptics** go through `LocalHapticFeedback` only.
- **Remove animations.** Compose (Animation 1.2+) honours the system animator scale, so with "Remove
  animations" every item jumps to its end state. Verify once on the emulator with
  `adb shell settings get global animator_duration_scale`.
- **Banned:** `rememberInfiniteTransition`, springs (they have no fixed duration), and any animation on a
  widget or the watch.
- **Never animated:** prayer times, the countdown digits, notice text and the disclaimer.

| # | Screen | Trigger | What moves | API | Motion | Haptic | Step |
|---|---|---|---|---|---|---|---|
| 1 | My prayers | Tick tapped or untapped | The ring fills and the tick draws in (reversed on untick) | `updateTransition` + Canvas `PathMeasure` | Reveal | ToggleOn / ToggleOff | 1 |
| 2 | My prayers | Screen opens, or a tick changes | Week rings grow to their share of five | `Animatable` in `LaunchedEffect` keyed on entry | Draw | none | 1 |
| 3 | My prayers | Pause or Resume | The button row crossfades to "Tracking is paused", same height | `AnimatedContent`, `sizeTransform = null` | Reveal | none | 1 |
| 4 | Times | Bell tapped | The bell crossfades to the crossed-out bell (shape, not only colour); Undo snaps back with no animation | `animateFloatAsState` in `graphicsLayer` | Quick | ToggleOn / ToggleOff | 3 |
| 5 | Times, My prayers | Undo message | Material snackbar | platform | platform | none | 1, 3 |
| 6 | Times | The sky period changes (at most four times a day), or opens in a new period | The gradient crossfades as one `drawBehind` lerp; the card does not move | `animateFloatAsState` | Draw | none | 5 |
| 7 | Times | Screen opens (once per process, Q12) | The day path draws from Fajr to now; after that the dot moves on the minute only | `Animatable` + `PathMeasure`, `derivedStateOf` per minute | Draw | none | 5 |
| 8 | Times | A prayer time passes | The Now and Next fill and bar move to the next row | `animateColorAsState` in `drawBehind` | Reveal | none | 5 |
| 9 | Qibla | Alignment becomes true | The face fill and rim colour change; the existing LongPress haptic stays | `animateColorAsState` | Standard | existing | 6 |
| 10 | Qibla | Heading changes | The existing dial tween, input-driven (exempt from the literal ban) | existing | 120 ms | none | exists |
| 11 | All | Tab change, sheets, folds | Existing behaviour | existing | Standard | none | exists |

RTL: mirror the Canvas x axis under `LocalLayoutDirection` for the day path. Ring arcs sweep clockwise in both
directions; that is a deliberate choice. Read animated values inside `drawBehind` or `graphicsLayer` lambdas, so
a frame does not recompose the row. Give every animation a `label`.

## 7. How to develop here

- **Read first:** `CLAUDE.md`, `docs/HANDOVER.md` §11 STATE OF PLAY, `docs/DESIGN_SYSTEM.md` (§0 above all),
  this file, then the `DETAILED-PLAN.md` section for your step.
- **Stack.** Kotlin, Jetpack Compose, Material 3 (BOM 2026.06.01: Compose 1.11.4, material3 1.4.0), minSdk 24,
  targetSdk 36. Modules: `app`, `core` (pure logic, no Android), `wear`. Storage is DataStore Preferences.
  Hijri dates use `HijrahChronology` (Umm al-Qura) through `HijriCalendar.of(date, offset)`.
- **Design rules** (DESIGN_SYSTEM; the build enforces most of them):
  - Colours only from tokens.
  - Gaps only from `Space.Tight`, `Related`, `Paragraph`, `Group` and `Section`.
  - Every action is a solid `CtaButton` (compact 40dp drawn, 48dp target). Only "Close" and "Got it" are text
    links.
  - Every file with a button is in the §4.1 inventory.
  - Nothing smaller than 14sp, the system font, tabular figures for times.
  - Words are never split across lines (`WholeWords`).
  - No "Tap" in copy, no em dashes, no "missed", "late" or "owed" for prayers.
- **Strings.** Add each new key to all five `values*/strings.xml`. Plural sets:
  - Arabic: zero, one, two, few, many, other
  - Urdu and Turkish: one, other
  - Indonesian: other

  `TranslationIntegrityTest` checks parity. The religious terms are fixed in `DETAILED-PLAN.md` 8.13.
- **Alarms.** Request-code bands:
  - prayer `dayOfYear*10 + slot`
  - suhoor 10,000
  - widget 20,000
  - reminder 30,000
  - jama'ah 40,000 + the prayer code
  - fasting 50,000 + day

  Reminders use `asAlarmClock = false`. Lay them before `alertFor.isEmpty()` returns, inside `runCatching`.
- **Commit rule (CLAUDE.md).** Commit the reasoning, not the file list.
  - Update `docs/HANDOVER.md` (§5 rules, §10 evidence, §11 state, §15 lessons).
  - Update `docs/PROJECT_BRIEF.md` when what is live or next changes.
  - Record rejected options. Say what was not tested.
  - Push.

## 8. How to test here

- **Unit tests:** `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`, then `./gradlew :core:testDebugUnitTest`,
  `./gradlew :app:testDebugUnitTest --tests '<Name>'` and `./gradlew :app:lintDebug`. `:core:test --tests` is
  not supported.
- **The gate before saying done:** `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`, then
  the phone and watch emulators together.
- **Emulator:** AVD `sajda`, started with `-read-only -no-window`.
  - S23 frame: `adb shell wm size 1440x3088` and `wm density 600` (823dp). 320dp check:
    `wm size 720x1280`, `wm density 360`.
  - Reset afterwards with `wm size reset` and `wm density reset`.
  - Seed data: `adb shell am instrument -w -e class com.sajdatime.app.ScreenSeedDeviceTest com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`.
    The test package is the app id plus `.test`; `docs/release-tools/release-1.5.0.sh:183` shows the pattern.
  - Launch: `adb shell am start -n com.sajdatime.app/.MainActivity` (`monkey` fails).
  - Back from Settings exits the app, so move between screens with the tabs.
- **The on-device suite.** Run `adb shell appwidget grantbind --package com.sajdatime.app --user 0` first,
  then run the suite with `am instrument`, never `connectedAndroidTest`, which uninstalls the app.
- **Right-to-left:** `./gradlew installRtl`. Judge the mirroring, not the English prose, and never show those
  screenshots to Ali as findings.
- **Text sizes:** `adb shell settings put system font_scale 1.5`, then `2.0`. Put it back to `1.0`.
- **Remove animations:** toggle it in Accessibility, then read
  `adb shell settings get global animator_duration_scale`. It should be 0, and every animation should jump to
  its end.
- **Verify, don't assume.** Compiling is not working: run it. Compare against an independent reference. Measure
  the fix with a screenshot. Never call something done that is untested; say what was not checked.

## 9. How to work with Ali

- He is not technical. Use plain language, and keep what was verified apart from what was assumed.
- **He decides visual things from screenshots,** light and dark. Ask with `AskUserQuestion`, at most four
  questions, your recommendation first.
- **In a web UI** (Play Console), walk him through one screen at a time from his screenshots.
- **Never ask him** to test on his device and report back, or to test the watch.
- **When he asks whether the app is ready for everyone,** check Play search, not the Console (CLAUDE.md).

## 10. Where the detail lives

| Topic | File and section |
|---|---|
| Ali's words and decisions | `PLAN.md` §0.1 |
| Animation toolkit, `Motion.kt` code | `DETAILED-PLAN.md` §1 |
| Height budget at 823dp, sky options, sky colour stops | `DETAILED-PLAN.md` §2.1 to §2.3 |
| Per-screen specs | `DETAILED-PLAN.md` §2.4, as corrected by §8 and §9 |
| Missed-fasts research | `DETAILED-PLAN.md` §3 |
| Widgets | `DETAILED-PLAN.md` §5, 8.11 |
| User journeys and every string | `DETAILED-PLAN.md` §6, 8.13 |
| Languages | `DETAILED-PLAN.md` §7 |
| Gap resolutions | `DETAILED-PLAN.md` §8 and §9 |
| Ramadan layout | `FABLE-REVIEW.md`, "Follow-ups", section 1 |
| The reference app | `PILLARS-STUDY.md` |
| Design decisions log | `docs/DESIGN_SYSTEM.md` §9 (23 Oct 2026 entry), §11 |
