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

- **Ali said "go" on 23 Oct 2026,** after the plan, two gap reviews, a final check and his answers to 27
  questions. Build in §5 order, with screenshots to him after each step.
- **Where everything is.** Branch `claude/1-6-0`, worktree `/Users/aliimrankhan/Developer/SajdaTime-1-6-0`.
  - **1.5.0 (updated 23 Oct 2026, evening):** the release day was 23 Oct 2026. PR #15 is merged into `main`
    (`12c3af5`). The Play Console upload was not finished that day, so 1.5.0 is **not live yet**: Play's
    "Updated on" still read 17 Oct 2026. Check that date before believing anyone's "it is live".
  - Never merge into `main`. Never push to `main`.
  - **`main` (1.5.0 as submitted, `12c3af5`) was merged in on 23 Oct 2026, evening** (`ceb2504`, full gate
    green: 512 unit tests, 0 failed). The earlier trial's conflicts in `MainActivity.kt`, `SettingsRepository.kt`
    and LISTING did not arise. Still to come: merge `main` once more after Ali merges the docs-only PR #16, the
    same way (merge, never rebase; on a `CLAUDE.md` conflict take this branch's version, a superset).
  - Never merge `claude/analytics-default-on-1-4-0`.
- **Already on the branch, built before the design pause, and reworked by this plan** (`DETAILED-PLAN.md` 8.1):
  - `e98b2a6` tracker: ticks on the Times rows, the tracker line under the list. Both move to a tab.
  - `9cc78bf` Islamic dates sheet: kept.
  - `9e9102e` jama'ah lines: kept, with the wording fix in 8.7 and the zone guard in 9.12.
  - `762e622` 1.6.1 reminder planners: pure, inert, kept switched off (8.8).
- **Ali's answers.** All 27 questions were answered on 23 Oct 2026 (§3). Four are decided from screenshots at
  their step. **A step whose screenshot decision is pending stops there and shows him.** Silence is never a yes (DESIGN_SYSTEM §0.2).
- **Memory.** Every session reads GBrain `projects/sajdatime` first (`recall` or `context_pack`) and writes what
  changed at the end. The 1.6 plan is recorded there too.
- **Progress, kept current here** (update this list in every commit that finishes a step):
  - Step P: **done**, `55ef229` (privacy page section and `TrackerPrivacyTest`; full gate not run).
  - Steps 1 to 11: not started.
  - Before step 0: merge `main` in. **Done**, `ceb2504`.
  - Step 0: **done**, 23 Oct 2026 night (Ali approved the plan: "Yes, build it"). `Motion.kt`, the three literals moved, `DesignRulesTest` motion bans, `RequestCodeTest` 30,000 band, `InertReminderSettingsTest`, DESIGN_SYSTEM §2 Motion and §9. Full gate green (518 unit tests, 0 failed).
  - Step 1: **built**, 23 Oct 2026 night (Ali approved from pictures: "B: calendar grid", "Yes, build it"). Waiting for his yes on the finished screenshots. `STEP-1-DESIGN.md` §9 and 13, HANDOVER §5.29.
  - Step 2: designed and verified (`STEP-2-DESIGN.md`, 23 Oct night); not built; Ali's two questions in its §7.

## 0.1 Which model does what (Ali, 23 Oct 2026)

Ali: "use the right model for the task and if tokens can be saved using lower tier model then do it. However,
everything will be checked by most likely Opus and the plan should be verified by Fable 5. Also the UX decision,
ease of use, Usability, information to display aspect should be done by fable." The agents are in
`.claude/agents/`, with the reasons in `docs/HANDOVER.md` §5.28.

- **The main session runs on Opus.** It talks to Ali, writes the plan changes, does alarm, data and animation
  logic, translates, commits and pushes.
- **Fable (`plan-verifier`) verifies every plan change** before work starts and before Ali sees it. That
  includes a change to this file or any 1.6 plan file.
- **Fable (`ux-lead`) does all the thinking for what users see** (Ali, 23 Oct 2026: "I prefer Fable to do all the
  planning, brainstorming, including the correct icons, ease to use, and to look good on a premium style or design
  for the app. So for that, I want Fable to do all the brain thinking."): planning and brainstorming, user journeys,
  UX and UI, icons, ease of use, the premium look, what information each screen shows and in what order, wording
  and every state. It shapes each step's spec before the build and judges its screenshots before they go to Ali.
  The main session writes the spec down and builds it; Ali still decides visual questions from screenshots.
  Fable also looks for ready-made assets before anything is drawn (icons, sky artwork for step 5, illustrations),
  free licensed, bundled, never AI generated; Ali approves each, and its source and licence go in `docs/ASSETS.md`.
  Every design must also suit a future iPhone version (Ali, 23 Oct 2026: "I do have aspiration later to make it
  this app for iOS as well ... So it doesn't look like a design mess, as Apple has often objection about the design
  aspect of the apps."): patterns both platforms share, an SF Symbol named beside each Material icon, a distinct
  look of our own. No iOS work in 1.6; rules in `.claude/agents/ux-lead.md`.
- **Sonnet (`implementer`) builds routine parts** once the spec is fixed. **Haiku (`mechanic`)** does counts
  and lookups.
- **Opus (`reviewer`) checks everything** before commit, including everything Sonnet and Haiku produced.

| Step | Fable `ux-lead` before build | Built by | Haiku `mechanic` | Opus `reviewer` |
|---|---|---|---|---|
| P | no (done) | main | none | yes |
| 0 Foundations | **yes**: how motion should feel (speeds, easing, what moves and what never does) | `implementer` (`Motion.kt`, moving `tween` literals, the `DesignRulesTest` bans) | count `tween(` and `rememberInfiniteTransition` | yes |
| 1 My prayers tab | **yes**: tab content order, off, paused and first day states, ring a11y wording | main (tracker data, `tracker_started`, review prompt exclusion, every string in five languages); `implementer` wires the keys, layout and tests. The tab sends no screen event, so `privacy.html:125` stays at three screens | string parity, §4.1 counts | yes |
| 2 Dates sheet, 2a Shia dates | **yes**: plain names, nothing celebratory on Ashura or Arbaeen | main (each date from a named Shia source) | none | yes, sources checked |
| 3 Bell with Undo | **yes**: bell states, Undo message wording | main (alert restore logic, permission) | string parity | yes |
| 4 Timetable | **yes**: day card content, Today marking, PDF sheet | `implementer` for the sheet; main for the PDF ranges | none | yes |
| 5 Skies | **yes**: artwork, what the header shows, screenshot pairs for Q10 | main (colour stops, contrast, animation) | contrast pair count | yes |
| 6 Qibla | **yes**: direction sentence first, Details contents | `implementer` (`MeasurementSystem.kt`, layout) | none | yes |
| 7 Jama'ah | **yes**: the out of window wording | main (codec v2, zone guard) | none | yes |
| 8 Widgets | **yes**: the 4 by 1 strip content at each width | main (refresh scheduling); `implementer` for layouts and XML | resource and string parity | yes |
| 9 Disclaimer move | **yes**: About row wording and order | main (the strings and the CLAUDE.md rule text: a hard rule and religious wording); `implementer` moves the row and removes the first run screen | find every use of removed keys | yes, plus the CLAUDE.md rule text |
| 10 Languages | **yes**: Language row subtitle | main (translations) | string parity in five languages | yes, religious terms |
| 11 Release | **yes**: "What's new" and listing copy | main | counts for the release script | yes, full diff since 1.5.0 |

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
   2a. **Shia dates, for Shia users only** (Ali, 23 Oct 2026, Q21).
   - **Candidate dates,** each included only if the named source lists it: Eid al-Ghadir (18 Dhul Hijjah), Arbaeen (20 Safar), the births of the Prophet ﷺ
     (17 Rabi al-Awwal in Shia practice) and Imam Ali (13 Rajab), the 15th of Sha'ban (the birth of
     Imam al-Mahdi, kept by Shia users), and the nights of the 19th, 21st and 23rd of Ramadan.
   - **Each one** must come from a named Shia source cited in the code and the commit, for example the
     official calendar of a major marja's office or al-Islam.org.
   - **Wording:** plain names only. Nothing celebratory on Ashura or Arbaeen.
   - **Sunni users** never see them.
   - **Test:** extend `IslamicDatesGoldenTest`.
   - **Records:** the decision lives in §3 Q21; `PLAN.md` §0 asked for exactly this "named Shia source" (D5).
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
bell last. The tab, the dates and step 0 are never cut. **Ali allows a late approval** (Q25). Aim to show everything by 4 Dec. Something approved later can still go in,
but the 15 Dec submission and Ramadan stay the real limits. If 1.5.0 is not live by 27 Nov, all of 1.6.0 ships after Eid.

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

**Answered by Ali on 23 Oct 2026** (27 questions compiled by Fable, asked with options in chat). These are
decisions now. Items marked *screenshot* are decided by Ali from screenshots at that step.

| # | Question | Ali's answer |
|---|---|---|
| 1 | The built commits | Keep and rework |
| 2 | The 1.6.1 rules on the branch | Keep, switched off, with `InertReminderSettingsTest` |
| 3 | Tab name | **My prayers** |
| 4 | Days before tracking began, in the 30-day grid | Not drawn |
| 5 | First day of the week | By the app's language |
| 6 | Bell off | Alert **and** that prayer's "minutes before" reminder both go off; on restores exactly (9.4) |
| 7 | PDF ranges | Keep Today, 7 days and This month |
| 8 | Ramadan timetable as cards | *Screenshot* (table and cards side by side, light and dark) |
| 9 | The Ramadan card's own timetable button | Keep both doors |
| 10 | Dark skies | *Screenshot* (four light and dark pairs) |
| 11 | Day path draw-in | Once per app open |
| 12 | Qibla distance | Miles by phone region (US, GB, LR, MM), km elsewhere, no setting |
| 13 | Jama'ah when travelling | **Ali's own answer:** "If they are travelling then we get the timing of their location based on their school of thought and calculation method then we will show it as per their location. Hence knowing their location is important." The prayer times always follow the current location (they already do). **Typed jama'ah times belong to the home mosque, so they are hidden while the phone is in a different time zone from where they were typed, and come back at home.** No "check" line and no stored coordinates (9.12, as amended) |
| 14 | Jama'ah on widgets | Off in 1.6 |
| 15 | "Different from your mosque?" button | *Screenshot* (with and without) |
| 16 | Disclaimer | Accept: Settings, About, first row, retitled "Disclaimer"; the watch keeps its two-point line |
| 17 | Language row subtitle in five scripts | *Screenshot* |
| 18 | Tablets | **Yes, centre it:** content capped at 600dp and centred, the sky full width |
| 19 | Text sizes | Check at 1.5 and 2.0 |
| 20 | Mawlid, Isra and Mi'raj, 15 Sha'ban | Left out of the dates sheet |
| 21 | Shia-specific dates (Ghadir, Arbaeen and others) | **Add in 1.6.0, for Shia users.** See §2 item 2a |
| 22 | Tasbih counter | Dropped for now |
| 23 | "What's new" | All nine listing languages |
| 24 | Play short description | "Offline prayer times, adhan, Qibla and a private salah tracker, with Wear OS." on release day (77 / 80). **Check with Ali at paste time (added 23 Oct 2026):** it drops "namaz", which `docs/store/LISTING.md` ("Why *namaz* went in after all") kept for search across South Asia and Turkey, and "Sunni and Shia". Show him both lines and the trade-off; his answer stands either way |
| 25 | The 4 Dec cut | **Allow a late approval.** Something approved after 4 Dec can still go in. The 15 Dec submission and Ramadan stay the real limits. If a late approval would push the submission past 15 Dec, ask Ali which matters more |
| 26 | 1.5.0 release day | **23 Oct 2026.** PR #15 merged that day; the Play Console part was still to finish (§0). Release work follows `RELEASE-1.5.0.md` |
| 27 | Start building | **Yes, go** (23 Oct 2026). Build in §5 order, with screenshots after each step |
## 4. Hard rules that never bend (from `CLAUDE.md`; read it in full)

- **Network and accounts.**
  - No network to calculate times.
  - No server of our own, no accounts, no ads.
  - No new usage-count event, parameter or property. **The My prayers tab sends nothing.**
  - Rating: Google's in-app review card only, rarely, never in our words (`review/ReviewPrompt.kt`, CLAUDE.md).
    The passive "Rate" and "Share" rows in About stay. Nothing in the app asks for a rating or suggests
    sharing. **No tick, streak or My prayers screen is ever a trigger for the review card** (SIGNOFF.md:52).
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
| P | **Privacy first** (9.2). **Done, `55ef229`** | `docs/privacy.html` | none | new `TrackerPrivacyTest` (moves the backup check from `TrackerWordingTest.kt:65`) | privacy page | test green |
| 0 | **Foundations** (§1, 8.9, 9.10) | new `ui/theme/Motion.kt` (three durations, §6); existing `tween` literals moved: the two fold chevrons (`SettingsList.kt:277`, `Notice.kt:117`, 200 ms) to `Quick`, the theme thumb (`SettingsList.kt:352`, 240 ms) to `Standard`; the unused imports in `PermissionAction.kt` removed (lines 30-39, the `ArrowForward` import too; the build's lint and the reviewer confirm none is used), and the `tween` imports at `SettingsList.kt:9` and `Notice.kt:13` once their calls use `Motion`. Compose's own default specs (tab fade `MainScaffold.kt:186`, tab pill colour `MainScaffold.kt:290`, the `AnimatedVisibility` folds, `OnboardingScreen.kt:161`) and the tab bar's shipped `SegmentTick` haptic (`MainScaffold.kt:300`) are left alone: approved screens. **`Motion.kt` shape** (in `:app` beside `Spacing.kt`, since it imports Compose): `object Motion { const val QUICK = 150; const val STANDARD = 250; const val DRAW = 400; fun <T> quick() = tween<T>(QUICK, easing = FastOutSlowInEasing); fun <T> standard() = …; fun <T> draw() = … }`; no exit helper (a call site passes the shorter spec itself). **Visible to Ali:** two fold arrows turn 50 ms faster and the theme thumb slides 10 ms slower; nothing to screenshot, tell him in one line | none | `DesignRulesTest` (ban `rememberInfiniteTransition` and `\bspring\s*\(`; ban **every** `\btween\b` under `app/src/main/java` except `ui/theme/Motion.kt` and `QiblaScreen.kt` (the dial), so a call site can only say `Motion.quick()` and the easing comes with it. Word bounded because a plain `tween(` also matches `Duration.between(`. **Mutation proof:** put `tween(200)` back in `Notice.kt`, see the test fail, take it out. It scans only the phone app; the watch has no animation today and that is stated, not claimed as covered); `RequestCodeTest` adds the 30,000 band; new `InertReminderSettingsTest` (app unit test: no file under `ui/` reads `jamaahReminderMinutes` or `fastReminders`, and no file under `app/src/main` except `notify/ExtraReminderPlans.kt` names `JamaahReminderPlan` or `FastReminderPlan` (the two objects that file declares; the file name itself appears nowhere in code), so the 1.6.1 planners cannot go live through the scheduler either. **Mutation proof:** reference one from `PrayerAlarmScheduler.kt`, see it fail, take it out. 8.8) | DESIGN_SYSTEM §2 (a "Motion" subsection beside the gaps) and a dated §9 entry (three tokens, `Reveal` dropped, no entrance animation, the haptics rule, M3 `MotionScheme` considered and not used) | tests green; one line to Ali, no screenshots |
| 1 | **My prayers tab** (`STEP-1-DESIGN.md`, then 2.4, 8.6, 9.7, 9.9, 9.10, 9.11) | `MainScaffold.kt` (`Destination.PRAYERS`, `CheckCircle` icons (STEP-1-DESIGN 3d), no usage word, `SnackbarHost`); new `ui/prayers/MyPrayersScreen.kt`; ticks and tracker line out of `HomeScreen.kt` (`:907-946`, `:216-218`); `Tick.kt` (PAUSED); `ui/home/MyPrayers.kt` (30-day grid); `TrackerRepository.kt` (`tracker_started`, in the tracker DataStore so Delete clears one store); a comment at `HomeScreen.kt:136` that the review card's calm timer lives only on Times (SIGNOFF.md:52; STEP-1-DESIGN §9) | `nav_tracker`, `tracker_off_line`, `tracker_paused_from_today`, `tracker_ring_a11y` (plurals), `tracker_ring_paused_a11y`, `tracker_ring_today_a11y`, `tracker_state_paused` (and `tracker_tick_a11y` gains the paused state), `tracker_last_night_isha`, `action_undo`; reuse `action_pause` and `action_resume` | `WeekStripTest` (core unit test: share of five, 36 or 32dp by width, first day; 9.9's two row wrap dropped), tracker codec test (`tracker_started`), `UsageCountsTest` (3 screens), `TrackerWordingTest` (wider prefixes), `DesignRulesTest` inventory, `LargeTextAndTalkBackDeviceTest` (rings and animation on device) | DESIGN_SYSTEM §4.1 inventory: rewrite rows 461-464 (the 1.5.0 placement) (Times placement) and add `MyPrayersScreen.kt`; §9; HANDOVER | screenshots to Ali |
| 2 | **Islamic dates sheet** (built) | check only | none | existing | none | screenshots |
| 3 | **Bell with Undo** (8.4, 9.4) | `HomeScreen.kt` `PrayerRow` (own `toggleable`, `Role.Switch`); `SettingsRepository.kt` (`lastAlertStyle`, `bell_removed_reminder`); `MainScaffold.kt` snackbar; `MainActivity.kt` permission request | `bell_on_a11y`, `bell_alarm_a11y`, `bell_off_a11y`, `bell_blocked_a11y`, `bell_off_message`, `bell_off_reminder_message`, `bell_off_message_fajr_ramadan` | `AlertRestoreTest`, `ColorContrastTest` (inverse pairs, `outline` on 3 row backgrounds), `SettingsCorruptionTest`, `DesignRulesTest` (kind G) | DESIGN_SYSTEM §4.1 kind G | screenshots |
| 4 | **Timetable** (8.5, 9.8) | `HomeScreen.kt` Today line; new month sheet (day cards, opens on today, `primaryContainer` + "Today"); `RamadanTimetableSheet.kt` kept until Q17 | `action_timetable`, `timetable_title`, `timetable_month_heading`, `timetable_month_a11y` | `RamadanPdfRangeTest`, `RamadanPdfDeviceTest`, `ScreenSeedDeviceTest` (labels) | this step adds DESIGN_SYSTEM §11 row `1.6-timetable` and rewrites inventory row 412 | screenshots |
| 5 | **Skies** (2.2, 2.3, 8.9, 9.6) | `Color.kt` sky stops (light and dark); `HomeScreen.kt` header backdrop (option D), artwork, `SkyPeriod` pure function, 600dp cap (Q20) | `sky_dawn`, `sky_day`, `sky_evening`, `sky_night`, `sky_now_a11y` | `ColorContrastTest` (72 text plus 24 non-text), `SkyPeriodTest`; emulator check that animator scale 0 ends animations | DESIGN_SYSTEM §1 (the four skies), §9 | 4 light plus 4 dark pairs to Ali (Q3) |
| 6 | **Qibla** (2.4, 8.10, 9.5) | `QiblaScreen.kt`; new `MeasurementSystem.kt` | `qibla_details`, `qibla_distance_km`, `qibla_distance_miles`, `qibla_bearing`; remove `qibla_subtitle` | `MeasurementSystemTest`, `DesignRulesTest` inventory | DESIGN_SYSTEM inventory | screenshots |
| 7 | **Jama'ah** (8.7, 9.12) | `JamaahTimes.kt` (codec v2, zone id); `HomeScreen.kt` lines | `jamaah_out_of_window` (fixed wording); before and after forms | `JamaahPlanTest`, codec test | none | screenshots |
| 8 | **Widgets** (§5, 8.11) | new `TodayStripWidgetProvider`, `widget_today_strip_info.xml`, layouts; sky drawables; `WidgetRefreshScheduler.nextRefreshAt` (sunrise); `colors.xml` and `values-night` | `widget_strip_label`, `widget_strip_desc` | `WidgetManifestTest` (3 providers), `WidgetResourcesTest`, `WidgetThemeTest`, `WidgetColorsTest`, `WidgetStateTest`, `WidgetRefreshSchedulerTest`, `WidgetDeviceTest` | DESIGN_SYSTEM widgets section | real launcher in light, dark, Urdu and 130% |
| 9 | **Disclaimer move** (6.0, 8.3, 9.3) | `MainActivity.kt` (remove `FirstRunDisclaimer`, `disclaimerSeen`); `SettingsScreen.kt` About, first row; `README.md:162` | `about_disclaimer_short` "A helper, not a religious authority. Worth reading."; `disclaimer_title` "Disclaimer"; delete `disclaimer_brief`, `disclaimer_read_full`, `action_understood` if unused | `DisclaimerContentTest`, `DesignRulesTest` (remove `:123` exemption), `TranslationIntegrityTest` | `CLAUDE.md` hard rule rewritten on this branch; HANDOVER §1, §5.15, :456; DESIGN_SYSTEM :410-411, :546, §9 | screenshot of Settings |
| 10 | **Languages** (§7, 8.13) | Language row subtitle (`settings_language_all`, untranslatable, bidi-isolated) | as listed | `TranslationIntegrityTest`; RTL with `./gradlew :app:installRtl` (never bare: HANDOVER lesson 149, the bare task installs the watch app over the phone app) | translation notes (8.13 religious terms) | screenshot (Q18) |
| 11 | **Release** (8.16, and the release checklist below) | `versionCode 10`, `1.6.0`; watch unchanged at `1002 / 1.5.0`; `docs/release-tools/release-1.6.0.sh` | "What's new" in nine languages | full gate | HANDOVER §11, PROJECT_BRIEF, RELEASE notes | about 20 store screenshots (01, 02, 03, 05, 06 and My prayers; 01, 02 and My prayers in four more languages) |
| 1.6.1 | Reminders and the Fasts count | `PrayerAlarmScheduler` (bands 40,000 and 50,000), Settings rows | `fasts_*` and reminder strings | `RequestCodeTest`, `ExtraReminderPlanTest`, `FastingDaysTest` | privacy page sentence | after Eid |

**Release checklist for step 11** (added 23 Oct 2026 from what 1.5.0's release day taught; none of it is new
scope):
- **Copy `release-1.5.0.sh` to `release-1.6.0.sh`** and change the counts (unit tests, device suite, screenshots,
  `versionCode 10`). `set-release-date.sh` moves both the privacy page date and the date pinned in
  `UsageCountsTest` (on `main`; here once `main` is merged in, before step 0); on 1.5.0's day the test failed because only the page moved (`main`'s HANDOVER §11, 23 Oct evening block).
- **Exclude the tracker seed from the device suite.** `release-1.6.0.sh` must leave out
  `com.sajdatime.app.TrackerSeedDeviceTest` with `-e notClass`, as `release-1.5.0.sh` line 242 does for the
  seed: `instrument device-suite -e notClass com.sajdatime.app.SuhoorReminderDeviceTest,com.sajdatime.app.ScreenSeedDeviceTest`.
  It is a seed, not a check.
- **Watch:** unchanged, so no new watch bundle. Leave the 1.5.0 watch release (1002) where it is in the Wear
  track, and say so in the Console walk through so nobody uploads the old bundle again.
- **Data safety:** the tracker is kept on the phone only, so Play's form should not change (data that never
  leaves the device is not "collected" in Play's terms). Check the current answers against `docs/privacy.html`
  in the Console walk through; do not change them without Ali.
- **Store listing:** the line "Coming soon to Chrome and Edge browsers" is in all nine languages (`docs/store/LISTING.md` for English, `LISTING-LOCALES.md` for the other eight). Update it if the
  extension has shipped, or take it out if it has slipped. The short description is Q24 (check with Ali).
- **After it is live:** re-baseline the daily health check to 1.6.0, record "live on <date>" in HANDOVER §11 and
  PROJECT_BRIEF, and close the 1.6 progress list in §0.

**Checkpoints:**
- **20 Nov:** steps P, 0, 1 and 3 on the emulator.
- **4 Dec:** steps 4 to 8 shown to Ali (target; late approval allowed, Q25).
- **11 Dec:** PR.
- **15 Dec:** submit.

## 6. Animation: the one table

Tools: Compose only (`DETAILED-PLAN.md` §1).
- **Durations** come from `ui/theme/Motion.kt`: `Quick` 150, `Standard` 250 and `Draw` 400 ms, with easing
  `FastOutSlowInEasing`. No other numbers. (Fable, 23 Oct 2026: three, not four. `Reveal` 300 was dropped
  because 50 ms from `Standard` is below what anyone notices, so the choice would drift by mood the way gaps did
  before `Space`. Pick by what moves: an icon or arrow swaps or turns (`Quick`: the bell, the fold chevrons); a shape, colour,
  position or block of text changes in place (`Standard`: the tick, the theme thumb, the pause row, Now and Next); a
  drawing runs across the screen (`Draw`: rings, sky, day path). Nothing above 400 ms, so a dropped frame on an old phone never reads as lag.)
- **Exits** use the same easing and the token one step shorter than the enter (a `Standard` enter leaves at
  `Quick`; a `Quick` enter also leaves at `Quick`). Reversing the same animation (an untick) counts as an exit.
- **Nothing moves unless the user touched it, a sensor reading changed (the Qibla dial), or a real event
  happened** (a prayer time passed, the sky period changed). No entrance animation when a screen opens, no shimmer, no attention pulse. The one exception is the
  day path drawing in once per app open (Q11), built as once per process (the cheaper reading of "app open").
- **Haptics** go through `LocalHapticFeedback` only, and only on the user's own tap that changes something
  stored: `ToggleOn`/`ToggleOff` (tick, bell, switches) and `SegmentTick` (pickers). Two shipped exceptions stay,
  named so nobody "fixes" them: `SegmentTick` on a tab change (`MainScaffold.kt:300`) and `LongPress` when the
  Qibla aligns (`QiblaScreen.kt`, a sensor event). Never on a prayer passing, a sky change, Undo, a snackbar or a ring filling. One pulse per tap.
- **Remove animations.** Compose (Animation 1.2+) honours the system animator scale, so with "Remove
  animations" every item jumps to its end state. Verify once on the emulator with
  `adb shell settings get global animator_duration_scale`.
- **Banned:** `rememberInfiniteTransition`, a written `spring(` (springs have no fixed duration; Compose's own
  default specs on approved screens are kept, row 11), and any animation on a
  widget or the watch.
- **Never animated:** prayer times, the countdown digits, notice text, the disclaimer, the city title and
  Hijri date, the glass bar's position, any dialog or sheet text. Letters never move or morph on their own (no per-letter or typewriter
  effects, which break Arabic and Urdu joining); a whole block of text may fade (rows 3 and 5).
- **iPhone effect:** carries over unchanged (`FastOutSlowInEasing` is the cubic curve 0.4, 0, 0.2, 1, so SwiftUI
  `.timingCurve(0.4, 0, 0.2, 1, duration: 0.15)` for `Quick`, 0.25 and 0.40 for the others; Remove animations = `accessibilityReduceMotion`; haptics = `.sensoryFeedback`).

| # | Screen | Trigger | What moves | API | Motion | Haptic | Step |
|---|---|---|---|---|---|---|---|
| 1 | My prayers | Tick tapped or untapped | The ring fills and the tick draws in (reversed on untick) | `updateTransition` + Canvas `PathMeasure` | Standard | ToggleOn / ToggleOff | 1 |
| 2 | My prayers | A tick changes (on open the rings are drawn at their value, no animation: the no-entrance rule) | That day's week ring grows to its new share of five | `Animatable` in `LaunchedEffect` keyed on the count | Draw | none | 1 |
| 3 | My prayers | Pause or Resume | The button row crossfades to "Tracking is paused", same height | `AnimatedContent`, `sizeTransform = null` | Standard | none | 1 |
| 4 | Times | Bell tapped | The bell crossfades to the crossed-out bell (shape, not only colour); Undo snaps back with no animation | `animateFloatAsState` in `graphicsLayer` | Quick | ToggleOn / ToggleOff | 3 |
| 5 | Times, My prayers | Undo message | Material snackbar | platform | platform | none | 1, 3 |
| 6 | Times | The sky period changes while on screen (at most four times a day); on open the sky is drawn at the current period with no animation | The gradient crossfades as one `drawBehind` lerp; the card does not move | `animateFloatAsState` | Draw | none | 5 |
| 7 | Times | Screen opens (once per process, Q11) | The day path draws from Fajr to now; after that the dot moves on the minute only | `Animatable` + `PathMeasure`, `derivedStateOf` per minute | Draw | none | 5 |
| 8 | Times | A prayer time passes | The Now and Next fill and bar move to the next row | `animateColorAsState` in `drawBehind` | Standard | none | 5 |
| 9 | Qibla | Alignment becomes true | The face fill and rim colour change; the existing LongPress haptic stays | `animateColorAsState` | Standard | existing | 6 |
| 10 | Qibla | Heading changes | The existing dial tween, input-driven (exempt from the literal ban) | existing | 120 ms | none | exists |
| 11 | All | Tab change, sheets, folds | Existing behaviour | existing | platform default (left as approved); fold chevrons `Quick` | none | exists |
| 12 | My prayers | Tracking switched on from the off card | The off card crossfades to the checklist | `AnimatedContent` | Standard in, Quick out | none (the Switch has its own) | 1 |

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
- **Right-to-left:** `./gradlew :app:installRtl` (never bare: HANDOVER lesson 149, the bare task installs the watch app over the phone app). Judge the mirroring, not the English prose, and never show those
  screenshots to Ali as findings.
- **Text sizes:** `adb shell settings put system font_scale 1.5`, then `2.0`. Put it back to `1.0`.
- **Remove animations:** toggle it in Accessibility, then read
  `adb shell settings get global animator_duration_scale`. It should be 0, and every animation should jump to
  its end.
- **Verify, don't assume.** Compiling is not working: run it. Compare against an independent reference. Measure
  the fix with a screenshot. Never call something done that is untested; say what was not checked.

## 9. How to work with Ali

- **Show him nothing that the `ux-lead` (Fable) and `reviewer` (Opus) have not seen first** (§0.1).
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
| Animation toolkit | `DETAILED-PLAN.md` §1 (its `Motion.kt` sketch has four durations; §6 here has three and wins) |
| Height budget at 823dp, sky options, sky colour stops | `DETAILED-PLAN.md` §2.1 to §2.3 |
| Per-screen specs | `DETAILED-PLAN.md` §2.4, as corrected by §8 and §9; all animation as §6 here |
| Missed-fasts research | `DETAILED-PLAN.md` §3 |
| Widgets | `DETAILED-PLAN.md` §5, 8.11 |
| User journeys and every string | `DETAILED-PLAN.md` §6, 8.13 |
| Languages | `DETAILED-PLAN.md` §7 |
| Gap resolutions | `DETAILED-PLAN.md` §8 and §9 |
| Ramadan layout | `FABLE-REVIEW.md`, "Follow-ups", section 1 |
| The reference app | `PILLARS-STUDY.md` |
| Design decisions log | `docs/DESIGN_SYSTEM.md` §9 (23 Oct 2026 entry), §11 |
| Which model does what | §0.1 here; `docs/HANDOVER.md` §5.28; `.claude/agents/` |
