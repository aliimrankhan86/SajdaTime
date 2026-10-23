# Gap review 1 (independent Fable 5.1, 23 Oct 2026)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

A fresh reviewer that had not seen the planning conversation. Resolutions are in DETAILED-PLAN.md section 8.


Read only. Read in full: `CLAUDE.md` (worktree copy, which differs from `main`'s in the rating rule), the six plan files, `DESIGN_SYSTEM.md`, HANDOVER §11 STATE OF PLAY (23 Oct) and §5.15, `privacy.html`, and the code and tests the plan touches on `claude/1-6-0` at `4b6ee20`. Nothing was run, edited or committed. Where I reason from a reading rather than a measurement I say "guess".

## Headline

The branch is not in the state the plan describes. Four feature commits already sit on `claude/1-6-0` (tracker `e98b2a6`, dates sheet `9cc78bf`, jama'ah lines `9e9102e`, and the 1.6.1 reminder planners `762e622`, all 23 Oct 10:41 to 10:57), and they build the pre-§0.1 design: ticks on the Times rows, the tracker line under the list, no Tracker tab, "Save as PDF" still on the Today line. The plan files say "nothing built". Everything below is ranked on top of that fact.

## Ranked gaps

### 1. The plan says nothing is built; three features and part of 1.6.1 are built, to the old design. (Severe: honesty and sequencing)

- Where: `PLAN.md:8` ("nothing built"), `PLAN.md:5-6`, `SIGNOFF.md:6`, `DETAILED-PLAN.md:148` ("signed-off tracker code on `claude/quiet-glass`": `git ls-tree origin/claude/quiet-glass` has no `TrackerLogic`, `JamaahTimes` or `IslamicDates`; they exist only on `claude/1-6-0`), HANDOVER §11 STATE OF PLAY 23 Oct (line 5658 onwards) which never mentions 1.6.0 code.
- Why it matters: Ali asked that nothing be developed until the plan is complete and he says go (`PLAN.md:246-247`). The code on the branch contradicts §0.1 in at least six visible ways (`HomeScreen.kt:907-946` ticks on Times, `HomeScreen.kt:216-218` tracker line, `MainScaffold.kt:83-91` three tabs, `HomeScreen.kt:889-893` Save as PDF, `strings.xml:368` "Tap to read", no bells). If he is shown screenshots from this build he will judge the wrong thing, which is the §0.1 lesson in `DESIGN_SYSTEM.md:26`.
- Fix: a dated line in `PLAN.md` §0.1 and HANDOVER §11 saying exactly what is on the branch and that it predates §0.1; rewrite DETAILED-PLAN step 1 as "move the ticks and tracker line out of `HomeScreen.kt` into the new tab" rather than "signed-off code on quiet-glass"; and ask Ali question 1 below.

### 2. `docs/privacy.html` has no tracker, jama'ah or dates section, and the tracker commit shipped without one. (Severe: hard rule)

- Where: `git diff origin/claude/quiet-glass..HEAD -- docs/privacy.html` is empty; `CLAUDE.md` location rule ("any change that touches data handling has to be reflected there in the same commit"); `PLAN.md:478` promises the section; `FABLE-REVIEW.md:76` flagged it. `privacy.html:124-126` says the app counts "which of the three main screens you open" and will be read against a four-tab app. `privacy.html:179-182` describes the PDF leaving the phone but nothing describes the new prayer-record file.
- Fix: write the section text into the plan now (what is stored, that the save file goes wherever the user puts it, that Delete removes it, that no usage count is sent for the Tracker tab), add it to step 1's commit, and extend `DisclaimerContentTest`'s pattern with a `TrackerPrivacyTest` that reads `privacy.html` for the phrases. Note `TrackerRepository.kt:26` already names a `TrackerPrivacyTest` that does not exist (the check lives in `TrackerWordingTest.kt:65`).

### 3. The disclaimer move touches nine places; the plan lists five. (High: hard rule being rewritten)

Listed in `DETAILED-PLAN.md:197-203`: `MainActivity.kt:294-295`, `DisclaimerContentTest:72-85`, HANDOVER §1 and §5.15, `CLAUDE.md`, listings. Missing:
- `DesignRulesTest.kt:123` exempts `MainActivity.kt` from the close-X rule because of this dialog; the exemption goes stale.
- `DESIGN_SYSTEM.md:410-411` inventory rows "First run disclaimer: I understand / Read in full" and `DESIGN_SYSTEM.md:546` ("Two must be answered... the first run disclaimer") must change or `DesignRulesTest` and §0.3 disagree with the app.
- `strings.xml:87` `disclaimer_title` is "Please read this first" and is the title of the Settings dialog too (`SettingsScreen.kt:776`). Once nothing is shown first, "first" is false in five languages.
- `privacy.html:247` comment and `DisclaimerContentTest.kt:149-150,172-173` messages say "shown on first run".
- HANDOVER §5.15 line 1046 justifies the watch carrying only two of four points because "every watch owner also has the phone app, where all four are shown once, unavoidably, at setup". That justification is removed with the dialog. The watch string cannot grow (88 character budget, `DisclaimerContentTest.kt:116-131`). Ali should hear this trade in plain words (question 8).
- `disclaimer_brief` and `disclaimer_read_full` must be deleted from all five `values*/strings.xml` or `TranslationIntegrityTest` fails on key parity.
- Fix: add these to DETAILED-PLAN 6.0 as a checklist for one commit.

### 4. Bell: four behaviours are unspecified. (High: the one change that can cost a prayer)

- Reminder interaction: the 1.5.0 "prayer in N minutes" reminder is laid regardless of `alertFor` (`PrayerAlarmScheduler.kt:94-107`). A user who taps Dhuhr's bell off still gets "Dhuhr in 10 minutes". The plan never says whether the bell also drops that prayer's reminder. Question 2.
- OS notifications denied (Android 13+) or the channel muted: the bell shows "on" while nothing can arrive. Specify: tapping a bell on with `POST_NOTIFICATIONS` denied requests the permission (the launcher exists, `MainActivity.kt:86-95`), and the row's description says "alert on, notifications are off on this phone" (wording to add).
- "Restore last style" needs a stored field; `alertFor` drops the entry on off (`SettingsRepository.kt:399-401`). Name the key, its codec and its `SettingsCorruptionTest` case.
- The Undo snackbar: `MainScaffold.kt:177` has no `SnackbarHost`; one host above the glass bar is a new piece. Material's snackbar draws in `inverseSurface` / `inverseOnSurface` / `inversePrimary` (`Color.kt:101-103, 206-210`), none of which `ColorContrastTest` asserts. Both themes look to clear 4.5:1 (guess, computed by eye) but must be in the test. If the action is written as our own `TextButton("Undo")`, `DesignRulesTest.kt:49-59` fails; use `SnackbarHostState.showSnackbar(actionLabel = ...)` so no `TextButton` appears in our source, and record kind G in §4.1 anyway.
- TalkBack: `PrayerRow` merges descendants (`HomeScreen.kt:1029`). The bell must be its own `toggleable` node with `Role.Switch`, as `Tick.kt:64-69` does, or it vanishes into the row's one sentence.

### 5. Timetable: the placement is marked open, decided, and recommended differently, in three files; the month sheet cannot fit. (High)

- `PLAN.md:156-161` "open, Claude recommends under the list"; `FABLE-REVIEW.md:39` "keep on the Today line"; `DETAILED-PLAN.md:108,122` and `PLAN.md:206` "stays on the Today line"; `DESIGN_SYSTEM.md` §11 has no row. Fix: one line in §0.1 and §11 saying decided (Ali accepted the detailed plan) or asking.
- The sheet "shows the month on screen": a date column plus six 12-hour times at the 14sp floor (`DESIGN_SYSTEM.md:276`) is roughly 7 × 55dp = 385dp against 328dp of usable width on a 360dp phone (guess from character widths), worse at 320dp, in Urdu digits, and at 1.5 text. The Ramadan sheet works because it has two time columns (`RamadanTimetableSheet.kt:131-140`). Specify the layout: one card per day with the six times wrapping, or a day picker, or 24-hour clock in the sheet only.
- "Today highlighted": the light sheet surface is `surfaceContainerHigh` (`DESIGN_SYSTEM.md:109`), the same colour the Times "current" row uses, so that highlight is invisible on a sheet. Use `primaryContainer` plus the word Today.
- `DETAILED-PLAN.md:225` collapses the export sheet's Today / 7 days / Month choice (`HomeScreen.kt:747-766`) to "month plus one button". Removing two PDF ranges is a §0.2 item 4 change (what the user can do) and needs Ali's yes; `RamadanPdfRangeTest` and the listing's PDF sentence follow. Question 4.

### 6. Tracker tab: eight states have no rule. (High)

- Isha after midnight: Times ticks `today.date` (`HomeScreen.kt:936-939`); after midnight today's Isha is `NOT_DUE_YET`, so a person praying Isha at 00:30 (every UK summer, most of Ramadan) cannot tick it on the tab's today card and must find yesterday in the 30-day sheet. `UX.md:109` states the rule, only `MyPrayers.kt:83-84` implements it, for the sheet. Rule to add: while `state.current == ISHA` and now is before today's Fajr, the today card shows yesterday's Isha ring labelled "Last night's Isha" (new string), ticking yesterday's record.
- No location: `state.today` is null, so "time at the end" and `due` have no source. Specify: rings enabled with no time, or the "Set your location" notice.
- Read-only record from a newer version: the sheet shows the notice (`MyPrayers.kt:106-109`); the tab's today card must too, with its rings disabled.
- Loading: the tab must not flash the switch card before DataStore delivers (`state.loading`).
- Days before tracking began: the 30-day grid draws 30 rings regardless; the days before the switch was turned on (or before install) are a grid of blanks, which is the "missed counter in another form" FABLE B5 warned about. Nothing stores the start date. Decide (question 9) and, if hidden, add a `tracker_started` key.
- Week strip width: 7 × 36dp + 6 × 8dp = 300dp against 288dp usable on a 320dp phone (guess), so it overflows at default text, not only at 1.5. Rule: 32dp rings below 340dp width, or wrap by measured width.
- First day of week: no `WeekFields` anywhere in `app/` or `core/`. The app's language is not the phone's locale (`AppLocale`). Say which locale decides, and that the day letters come from `DayOfWeek.getDisplayName(NARROW, that locale)`.
- The tab and usage counts: `MainScaffold.kt:158-166` is an exhaustive `when`; TRACKER must map to nothing, and `Screen` (`UsageCounts.kt:30`) must not gain a value. Add a `UsageCountsTest` case pinning `Screen.entries.size == 3`, and note `TrackerWordingTest.kt:74-77` forbids the word "tracker" in `UsageCounts.kt`, so the mapping must live in the scaffold.

### 7. Jama'ah: two contradictions still standing. (Medium)

- 25 km hide: `PLAN.md:52-53` says hide beyond 25 km; `SIGNOFF.md:74-75` and `DETAILED-PLAN.md:213` cut it. `PLAN.md` §0 is not annotated. Strike it there.
- Jama'ah on the large Today widget: `DETAILED-PLAN.md:172` adds it; `SIGNOFF.md:24-25` and `PLAN.md:296` put jama'ah on widgets out of 1.6. Question 5.
- Wording: `strings.xml:720` "Your Asr jama'ah time is outside Asr today" is not the signed-off sentence ("is before Asr begins", `UX.md:183`) and reads as a grammar slip in English.

### 8. 1.6.1 code is already in 1.6.0, and the one cheap sign-off condition is still unmet. (Medium)

- `ExtraReminderPlans.kt`, `FastingDays.kt`, `AppSettings.jamaahReminderMinutes` and `fastReminders` (`SettingsRepository.kt:251-256, 497-505`) ship inert in 1.6.0. `SIGNOFF.md:103-104` wanted 1.6.1 small and separate. Decide (question 10) and, if kept, add a test that no 1.6.0 screen reads those two settings.
- `RequestCodeTest.kt` still covers three bands; the 30,000 reminder band from 1.5.0 has no assertion (`SIGNOFF.md:21,83`). This is a five-line test and belongs in step 0, not 1.6.1.

### 9. Sky: boundaries, replay, existing durations and one verified fact. (Medium)

- Boundaries: "night = Isha to Fajr" cannot be computed from today's list after midnight (today's Isha is in the future). Define the period from `state.current` plus the sunrise trigger the ViewModel already has (`SajdaViewModel.kt:257-267`, `PrayerEngine.currentPrayer` at `PrayerEngine.kt:360-373` looks back a day): FAJR → dawn; null or DHUHR → day; ASR or MAGHRIB → evening; ISHA → night. No new timer, and testable as a pure function (`SkyPeriodTest`).
- Edge to edge: `DETAILED-PLAN.md:87,184` says unverified. `MainActivity.kt:179` calls `enableEdgeToEdge()`. Verified. The known cold-start light frame (`DESIGN_SYSTEM.md:630-635`) will now be a light frame before a dark sky; say so.
- `DesignRulesTest` ban on number literals in `tween(`: four exist today (`SettingsList.kt:277,352`, `Notice.kt:117`, `QiblaScreen.kt:209` at 120 ms). Either migrate them (200 and 240 become `Motion.Standard` 250, a change nobody will see) or exempt the compass dial, whose 120 ms is input tracking, not motion. Say which.
- Replay: the day path "draws in on open". `AnimatedContent` rebuilds `HomeScreen` on every tab return, so it replays every visit. Recommend once per process (question 12).
- `PLAN.md:265-266` item 5 "Changing day slides with the arrow tapped": there is no day stepper (`DESIGN_SYSTEM.md:666`, `FABLE-REVIEW.md:37`). Strike it.
- Dark skies: Ali's answer (`PLAN.md:164-165`) is "show me". `DESIGN_SYSTEM.md` §11 lists 1.6-sky as decided. Hold the dark variant as OPEN until screenshots.

### 10. Qibla: four contradictions and one platform gap. (Medium)

- Dial size: `DESIGN_SYSTEM.md:777` "grows larger, thicker rim" vs `DETAILED-PLAN.md:118` "radius as today".
- Alignment wording: `DESIGN_SYSTEM.md:770` "You're facing the Qibla" vs the shipped `qibla_facing` "You are now facing the Kaaba" (`strings.xml:278`), which the plan keeps. Pick the shipped one; the contraction and "Qibla" are both departures.
- No location: `DETAILED-PLAN.md:223` "above an empty dial"; the code shows one sentence and no dial (`QiblaScreen.kt:110-119`). Decide which.
- "Details" is a button in `QiblaScreen.kt`, which is not in the §4.1 inventory; `DesignRulesTest.kt:72-87` fails until the row is added.
- Miles: `LocaleData.getMeasurementSystem` is API 28; minSdk is 24 (`DETAILED-PLAN.md:15`). Specify the fallback for 24 to 27 (country list US, LR, MM, and GB for distance), and which locale: the app's language or the phone's region (question 7). The number must go through the app's digit formatting (HANDOVER §5.11).
- `qibla_subtitle` must be removed from all five files or key parity fails.

### 11. Widgets: the sunrise guess is now answered, and the test surface is larger than listed. (Medium)

- `DETAILED-PLAN.md:176` guesses `state.next` includes sunrise. It does not: "Sunrise is never next" (`WidgetState.kt:83`, `PrayerEngine.nextPrayer`). So a dawn sky would stay on the widget from Fajr until Dhuhr. `WidgetRefreshScheduler.nextRefreshAt` (`WidgetRefreshScheduler.kt:47-51`) needs a sunrise boundary and a `WidgetRefreshSchedulerTest` case.
- A third provider means a manifest receiver, `widget_today_strip_info.xml` with `previewLayout`, and `WidgetManifestTest.kt:33-58`, which pins exactly two providers, plus `WidgetResourcesTest` and `WidgetThemeTest`. Not listed.
- `WidgetColorsTest.kt:78-90` requires every `@color` to have a Kotlin twin with the same value; the sky stops therefore live in `Color.kt` as well as `colors.xml` and `values-night/colors.xml`. Not listed.
- The 4 by 1 strip: five columns of about 50dp each at 250dp; "12:46 pm" at 12sp is about 50dp (guess). Specify what gives at 130 percent text beyond hiding the glyph row (24-hour clock on the strip, or two rows).

### 12. Font scale: 1.5 is not the project's bar. (Medium)

`PLAN.md:110-111` and `DETAILED-PLAN.md:290` check at 1.5. `CLAUDE.md` ("2x text"), `DESIGN_SYSTEM.md:975` ("2.0 text") and the `WholeWords` lesson (`DESIGN_SYSTEM.md:289-295`, found at 2.0 on 320dp) say 2.0. Check both; the tab bar gains a fourth label, which is exactly where 2.0 bit before.

### 13. Wording: the table in DETAILED-PLAN 6.2 has gaps in keys, plurals and languages. (Medium)

- Missing keys: the four period names (dawn, day, evening, night) for `home_sky_a11y`; "Now, between %1$s and %2$s"; "Last night's Isha" (gap 6); the widget's manifest label `widget_strip_label` as well as its description; a notifications-off bell description (gap 4); the Timetable sheet's month heading; the Language row's five-name subtitle, which should be `translatable="false"` with each name bidi-isolated (`Bidi.kt`) so the mixed-script line reads in RTL.
- `tracker_resume` in the table is `action_resume` in the code (`strings.xml:647`).
- `tracker_ring_a11y` "%2$d of 5 prayed" should be `<plurals>` so Arabic's six forms and Indonesian's one form are honoured (`TranslationIntegrityTest.kt:47-57`).
- `nav_tracker` and `bell_*` are outside `TrackerWordingTest`'s key prefixes (`TrackerWordingTest.kt:30-31`), so the no-judging-word check does not cover the tab name. Widen the prefix list.
- `bell_off_message_fajr_ramadan` needs its condition stated: Ramadan season and `suhoorReminderMinutes > 0`.
- Five languages: the table is English only. For the religious terms at least, fix the forms now so the pass does not improvise: Jama'ah and Jumu'ah in Arabic, Urdu, Turkish (cemaat, Cuma) and Indonesian (jemaah, Jumat), the four periods, "Undo", "Timetable". `DETAILED-PLAN.md:268,288` starts this; finish it.
- `about_disclaimer_short` still carries banned "Tap" (`strings.xml:368`); the plan fixes it. The two older "tap" strings (`strings.xml:626-627`) are out of scope but worth one line.

### 14. Accessibility and contrast assertions not yet listed. (Medium)

- Bell `outline` on the three row backgrounds (`primaryContainer`, `surfaceContainerHigh`, `surface`) at 3:1 in both themes. Both pass by my arithmetic (guess) but are not in `nonTextPairs` as far as the test names show.
- Snackbar inverse pairs (gap 4).
- The 30-day grid's rings are tappable, so 48dp cells around 36dp rings; the week strip: say whether its rings are tappable at all.
- The sky artwork and now-dot should be `clearAndSetSemantics {}` (decorative), not described, or TalkBack hears the period twice (once from the card).
- `LargeTextAndTalkBackDeviceTest` must gain the Tracker tab, a bell, and Qibla Details.

### 15. Tablet, foldable and landscape are not mentioned anywhere. (Low)

The app has no width rule today (`HomeScreen.kt:166-171` fills the width). A full-bleed sky at 800dp wide and a 36dp ring strip across a tablet are new sights. One sentence either way: "no tablet layout, unchanged, accepted", or a 600dp content cap.

### 16. Testing and gate list: consolidated. (Medium)

Beyond the plan's list: `DesignRulesTest` (inventory rows for the Tracker tab file and `QiblaScreen.kt`; infinite transition ban; `tween` literal ban with the four existing literals resolved); `ColorContrastTest` (skies 72 plus 24, inverse pairs, bell on rows); `WidgetColorsTest`, `WidgetManifestTest`, `WidgetResourcesTest`, `WidgetStateTest`, `WidgetRefreshSchedulerTest` (sunrise); `TrackerWordingTest` prefixes; `DisclaimerContentTest` (gap 3); `RequestCodeTest` (30,000 band now); `UsageCountsTest` (three screens); new pure tests `SkyPeriodTest`, `WeekStripTest` (first day, partial fill, pause glyph), `AlertRestoreTest`, `MeasurementSystemTest`; device checks for animator scale 0, the strip on a real launcher in light, dark, Urdu and 130 percent, and the Tracker tab at 2.0. Store screenshots: 01, 02, a new Tracker shot and 06 in English, plus 01, 02 and Tracker in four more languages (`DETAILED-PLAN.md:277`) is about 20 phone shots. `release-1.6.0.sh` with new counts; `set-release-date.sh` for the privacy date (it changes in 1.6.0 because of gap 2).

### 17. Delivery: the dates hold only if Ali answers on time, and nothing says what happens if he does not. (High)

- 1.5.0's release day is still not fixed (HANDOVER §11, 23 Oct). G1 (14 clean days) plus the 27 Nov hard stop means the earliest 1.6.0 submit is 11 Dec; the plan's 15 Dec has four days of slack.
- DETAILED-PLAN step 1 to 8 in seven weeks includes: a fourth tab, bells with a new snackbar host, four skies in two themes, a Qibla restyle, a month sheet that needs a new layout, a third widget plus skies on the other two, about 40 new strings in five languages, nine-language "What's new" (as 1.5.0 did, `LISTING-LOCALES.md`), the privacy page, the disclaimer rewrite across nine places, and docs. FABLE-REVIEW A called it "roughly a second release". Underestimated most: widgets (a RemoteViews change is verified on a launcher, not an emulator screenshot; the 1.5.0 widget work ran over several days), the month sheet, and the screenshot rounds.
- Every step ends with screenshots for Ali and "silence is never a yes" (`DESIGN_SYSTEM.md:49`). The plan has checkpoints (20 Nov, 4 Dec, 11 Dec) but no rule for a late answer. Add one: anything not approved by 4 Dec ships after Eid, no exceptions, so the Ramadan build is the approved subset.

## Open questions for Ali (recommended answer first)

1. Three features are already built on the branch to the earlier design. Keep them as the base and rework them to the new plan (recommended), or set them aside until you say go?
2. When you tap a prayer's bell off, should its "N minutes before" reminder go off too? Recommended: yes, both, and the message says "Dhuhr alert and reminder off".
3. Dark mode skies: four dark variants (recommended), decided only after you see the four pairs.
4. Timetable: keep the renamed button on the Today line (recommended). Keep "Today only" and "7 days" as PDF choices inside the sheet (recommended) rather than removing them.
5. Jama'ah on the large Today widget: out of 1.6 as signed off (recommended), or in?
6. The tab's name: "My prayers" (recommended) or "Tracker".
7. Distance: miles for the US, UK, Liberia and Myanmar by the phone's region, km everywhere else (recommended), with no setting.
8. The disclaimer: with no first-run dialog, a person who only ever reads the watch sees two of the four points and the phone's full text only if they open About. Accept (recommended, it is your call), and retitle the Settings dialog "Disclaimer" since "Please read this first" is no longer true.
9. In the 30-day grid, days before you switched tracking on: not drawn (recommended), or empty rings.
10. The 1.6.1 reminder rules already on the branch: keep them in 1.6.0 switched off and untouchable from any screen (recommended, they are pure and tested), or move them to a 1.6.1 branch.
11. Check every screen at 2.0 text as well as 1.5 (recommended: yes, it is the project's own bar).
12. The day path draws in once per app open (recommended), not every time you return to Times.
13. The week strip's first day follows the app's language (recommended, guess) rather than the phone's region.
14. "What's new" in all nine listing languages as 1.5.0 did (recommended), or English plus the five app languages.
15. A hard cut: whatever you have not approved from screenshots by 4 Dec ships after Eid (recommended).

Minor notes, no decision needed: `DETAILED-PLAN.md:72` counts the mosque door at 52 plus 8; the code gives 16 above (`HomeScreen.kt:616`) plus 52, so the budget sum is about 812dp, still inside 823 (guess from code, not rendered); `UX.md:205` keeps a caret on the date line that `SIGNOFF.md:54` and the code (`DESIGN_SYSTEM.md:437`) dropped; `DESIGN_SYSTEM.md:765-766` still says "chip" and "missed fasts" where §4.1 says compact button and the feature is now "Fasts to make up".
