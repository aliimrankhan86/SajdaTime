# Gap review 2 (independent Fable 5.1, 23 Oct 2026)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

Checks GAP-REVIEW-1 closure and finds new gaps. Resolutions are in DETAILED-PLAN.md section 9 and BUILD-INSTRUCTIONS.md.

Read in full: the worktree `CLAUDE.md` (differs from `main` in the rating and GBrain rules), the seven plan files, `DESIGN_SYSTEM.md`, HANDOVER §1, §5.15 and §11 STATE OF PLAY (23 Oct), `privacy.html`, and the code and tests on `claude/1-6-0` at `e4c7f9b`. Nothing was run, edited or committed. "Guess" marks reasoning from a reading rather than a measurement.

## (a) GAP-REVIEW-1 items against DETAILED-PLAN section 8

Section 8's five code claims all hold: `MainActivity.kt:179` `enableEdgeToEdge()`; `WidgetState.kt:83` "Sunrise is never next"; `PrayerAlarmScheduler.kt:94-107` reminders laid before the `alertFor.isEmpty()` return at 108, from `prayerReminderFor` (`SettingsRepository.kt:231`); `strings.xml:87` `disclaimer_title`; no `SnackbarHost` in `MainScaffold.kt`.

| # | Closed | Note |
|---|---|---|
| 1 Branch state | Partly | `PLAN.md:8` and 8.1 say it. HANDOVER §11 STATE OF PLAY (23 Oct) still has no mention of 1.6.0 code or the branch (`grep "1\.6"` on HANDOVER returns nothing); `PROJECT_BRIEF.md:103` says the plan is on `claude/1-6-0-plan`. CLAUDE.md sends every cold session to §11 first, so it will not know four feature commits exist. DETAILED-PLAN §4 step 1 (line 148) still says "signed-off tracker code on `claude/quiet-glass`" (false: `TrackerLogic`, `JamaahTimes`, `IslamicDates` exist only on this branch). |
| 2 Privacy | Partly | 8.2 gives the headings, not the text, and the phrases `TrackerPrivacyTest` must find are not fixed. "First commit that ships tracker data" is already behind us (`e98b2a6`): it must say "the next commit on the branch, before any other 1.6 commit". `privacy.html` is already a declared test input (`app/build.gradle.kts:248`), so the new test will not go UP-TO-DATE. `TrackerRepository.kt:26` still points the backup rule at a `TrackerPrivacyTest` that lives in `TrackerWordingTest.kt:65`. |
| 3 Disclaimer | Partly | 8.3 lists the nine. Still missing: `README.md:162` "shown in the app on first run" (copy 6 of §5.15's six, a declared test input and the public repo's front page); HANDOVER:456 "shown once automatically"; `action_understood` becomes a dead key in five languages; the `FirstRunDisclaimer` composable itself (`MainActivity.kt:442`). Hard rule check: the owner's change is his and is recorded as reversed with the reason; the row is two levels deep (Settings, About), so "not buried" holds. |
| 4 Bell | Partly | Five of six sub-points closed and the code claims hold. The reminder resolution is wrong in one direction: see new gap 3. |
| 5 Timetable | Partly | Decided and `PLAN.md:156` annotated. 8.5 promises a DESIGN_SYSTEM §11 row; §11 (lines 1006-1018) has none. The inventory row 412 ("Save as PDF, Today line") and a new "Save as PDF inside the Timetable sheet" row are not named. The Ramadan table becomes cards: see new gap 7. |
| 6 Tracker states | Yes | All eight have a rule. One hole in `tracker_started`: new gap 13. |
| 7 Jama'ah | Partly | `PLAN.md:52` struck; wording fixed. DETAILED-PLAN §5 table (line 172, "large adds jama'ah as a 12sp second line") is not struck; 8.7 says it is. |
| 8 1.6.1 code | Yes | Pending Ali's answer 10. |
| 9 Sky | Yes | Period rule verified against `PrayerEngine.currentPrayer` (looks back a day, so after midnight `current` is ISHA, night). The four `tween` literals are exactly `SettingsList.kt:277,352`, `Notice.kt:117`, `QiblaScreen.kt:209`. §11 marks dark variants OPEN. |
| 10 Qibla | Partly | DESIGN_SYSTEM:777 now says "stays its current size, with a thicker soft rim and a rounder needle"; DETAILED-PLAN 2.4 says "radius as today" and nothing about rim or needle. Miles: see new gap 4 (build-blocking). |
| 11 Widgets | Yes | |
| 12 Font scale | Yes | `PLAN.md:110-111` still says 1.5 only; 8.12 wins but PLAN is not annotated. |
| 13 Wording | Partly | `tracker_pause` in 6.2 duplicates the existing `action_pause` (`strings.xml:646`), the same slip 8.13 fixed for resume. No key for 8.4's "Dhuhr alert and reminder off". `about_disclaimer_short` "Please read this once." reads as a one-off for a permanent row. |
| 14 Accessibility | Partly | Contradicts 8.13: new gap 5. |
| 15 Tablets | Yes | A 600dp cap is itself a visible change for tablet users of approved screens (§0.2 item 1); one screenshot question. |
| 16 Tests and gate | Partly | Not listed: `SettingsCorruptionTest` cases for `lastAlertStyle` and `tracker_started`; the device tests that find "Save as PDF" by label (guess: `RamadanPdfDeviceTest`, `ScreenSeedDeviceTest`); `SettingsSemanticsDeviceTest` if it pins rows (guess); store shots 03 (Ramadan gains the sky) and 05 (Settings gains three rows and a group); `PROJECT_BRIEF.md` (the CLAUDE.md commit rule); the watch bundle line in `release-1.6.0.sh` (phone 10 / 1.6.0, watch still 1002 / 1.5.0: the 1.5.0 script asserts both). |
| 17 Delivery | Yes | Steps 7 and 8 have no checkpoint before the 4 Dec cut; by 8.17 they ship after Eid unless shown by then. Say so in the §4 table. |
| Minor notes | Yes | `PLAN.md:267` struck; DESIGN_SYSTEM 765-766 fixed. |

## (b) New gaps, most severe first

**1. The plan files cannot be read in one order. (High: a cold developer model)**
`SIGNOFF.md:3` says it wins over PLAN "any section, including §0"; `PLAN.md:178-179` says DETAILED-PLAN wins where more specific; DETAILED-PLAN §8 overrides §1 to §7; `UX.md:26` still says "no new tab, no onboarding step, ticks on Times" and its banner names only four overrides. SIGNOFF puts ticks on Times, the tracker line under the rows and "the disclaimer is untouched"; DETAILED-PLAN moves all three. A builder who follows the stated rule (SIGNOFF wins) builds the wrong app. Fix: one identical precedence line at the top of all seven files: DETAILED-PLAN §8, then DETAILED-PLAN §1-7, then PLAN §0.1, then SIGNOFF, then PLAN §0 and §1-9, then UX.md, then FABLE-REVIEW; plus a UX.md banner naming the tab, the bell, the timetable and the disclaimer as superseded.

**2. The build sequence has no owner step for about a third of the work. (High)**
DETAILED-PLAN §4 (lines 141-158) predates §6 to §8. No step owns: the disclaimer move (6.0, 8.3); the privacy section (8.2); the Language subtitle (§7); the inert-1.6.1 test and the 30,000 band (8.8); the 600dp cap (8.15); `TrackerPrivacyTest`, `AlertRestoreTest`, `MeasurementSystemTest`; the store listing words (PLAN §6), "What's new" in nine languages (8.18 q14) and the screenshots; HANDOVER, PROJECT_BRIEF, DESIGN_SYSTEM §9/§11 edits; the Now/Next move animation (PLAN 0.1 item 4) has no step either. Fix: rewrite §4 as the one ordered table, each row naming files, string keys, tests, docs and the screenshot it ends with, and move the §4 "Depends on" column off `claude/quiet-glass`.

**3. Bell off and on can switch on a reminder the user turned off. (High: alerts)**
8.4: "Tapping the bell off also removes that prayer from `prayerReminderFor` ... Tapping it on restores both." `prayerReminderFor` is a per-prayer choice in the Reminder dialog. A user who had Dhuhr's reminder off, then taps the bell off and on, gets the reminder back, and the "alert and reminder off" message lies when the reminder was already off. `lastAlertStyle` covers the style only. Fix: store what the bell removed (`bellRemovedReminder: Set<PrayerSlot>`, codec, corruption case) and restore only that; word the message by what changed (two keys); `AlertRestoreTest` covers reminder-already-off; the Reminder dialog row must read the same state (DESIGN_SYSTEM:764).

**4. "Phone's region" has no sanctioned source and the obvious one fails the build. (High: `LocaleDisciplineTest.kt:163`)**
That test fails any shipped code containing `Locale.getDefault()`. The activity's own `Configuration` is wrapped by `AppLocale.wrap` to the app's language, so an Urdu speaker in London would read a region of "ur" and get km, and a Pakistani user who chose English might get miles. Fix: specify `Resources.getSystem().configuration.locales[0].country` (the phone, unwrapped) in one function in `MeasurementSystem.kt`, `MeasurementSystemTest` for US, GB, LR, MM, PK and an empty country, and the 24-27 fallback by that country list.

**5. The sky is both spoken and decorative. (Medium: TalkBack)**
8.13 adds `sky_dawn` to `sky_night`, `home_sky_a11y` and `sky_now_a11y`; 8.14 makes the artwork and now-dot `clearAndSetSemantics {}` "because the card already speaks the period". The card speaks the next prayer and the countdown, never dawn, day, evening or night (`HomeScreen.kt` card, `NextPrayerCard`), so a blind user hears the period nowhere. Fix: one spoken node on the header artwork (`sky_now_a11y` with the period name), the dots and path decorative inside it; delete `home_sky_a11y`.

**6. A paused day's tick has no look and no voice. (Medium: addition 2 and the woman pausing)**
`TickState.PAUSED` exists (`TrackerLogic.kt`) but `Tick.kt` draws it as a plain outline at full opacity and `tracker_tick_a11y` has three states (prayed, not ticked, not due), so TalkBack calls a paused prayer "not ticked". Journey E says today's rings "dim", 2.4 says the strip "keeps drawing", neither specifies the today card. Fix: `tracker_state_paused`, the pause glyph in the ring, disabled, in `Tick.kt`; `WeekStripTest` and the device test cover it.

**7. Timetable sheet: four things a builder cannot decide. (Medium)**
(a) 8.5 turns the approved Ramadan table (`RamadanTimetableSheet.kt:131-140`, DESIGN_SYSTEM:419) into cards. That is a visible change to a screen Ali approved: screenshot question, not a resolution. (b) "Month" is undefined: calendar month, the PDF's `THIS_MONTH`, or 30 days; and the sheet must open scrolled to today (a list opened on the 25th shows the 1st). (c) Whether jama'ah lines appear in the cards (say no). (d) The §11 row and the two inventory rows (gap table item 5).

**8. Jama'ah fixed times follow the phone's clock, not the mosque's. (Medium: traveller)**
`JamaahRule.Fixed(LocalTime)` is placed at `ZoneId.systemDefault()` (`JamaahTimes.kt` `fixedAt`). Typed in London, read in Karachi, "Jama'ah 1:30 pm" is Karachi time under Karachi's Dhuhr: a wrong time inside the window, so the guard does not catch it. Journey C accepts this as a guess. A cheap guard that stores no coordinate (the reason the 25 km hide was cut): store the zone id beside the rules (one more key, codec v2) and show `jamaah_check` when the phone's zone differs. Needs Ali: question 16.

**9. Inconsistencies left standing after section 8. (Medium: cold build)**
DETAILED-PLAN:148 (quiet-glass), :172 (widget jama'ah), 2.4 "Mon to Sun in the phone's first-day order" against 8.6 (app language), 2.4 "1.5: the strip wraps" against 8.6 "at 2.0 wraps" (say: wrap by measured width, not scale), `PLAN.md:110` 1.5 against 8.12, DESIGN_SYSTEM:777 rim and needle against 2.4. Fix each in place with "superseded by 8.x".

**10. Not stated anywhere, needed to build the tab.** The tab's order (Times, Qibla, My prayers, Settings is implied by PLAN 0.1 but never said), its icon pair (Rounded and Outlined), the new screen file's name (the inventory and `DesignRulesTest` key on file names), the `Destination` value name, and the `SnackbarHost` offset (`LocalBottomBarClearance`, since the glass bar floats in a `Box` with no `bottomBar` slot, `MainScaffold.kt:177-186`). `Motion.tween` is itself a `tween(` with a non-literal: say the literal ban exempts `ui/theme/Motion.kt`.

**11. `tracker_started` hides loaded history.** 8.6 draws no ring before `tracker_started`. A record loaded from a file (or an older phone) has days before the switch-on date. Fix: `tracker_started` = the earlier of the switch-on date and the earliest record, re-derived on load and cleared on Delete.

**12. HANDOVER §11 and PROJECT_BRIEF are silent on the branch** (gap table item 1). CLAUDE.md makes §11 the first thing read. One dated line in each, in the next commit.

**13. Small wording and screenshot items.** `tracker_pause` duplicate; "alert and reminder off" key; `about_disclaimer_short` wording; store shots 03 and 05 retaken; the Language subtitle replaces one Ali chose (DESIGN_SYSTEM:794, screenshot); the mosque door decision (SIGNOFF's "one real design question") is not in 8.18; the privacy text should say the save file carries paused days (a pause pattern is the one thing in it a stranger could read as health); the DST path dot should key on the minute (`derivedStateOf` of `epochSecond / 60`) so the day path does not recompose every second.

Hard rules: nothing in the resolved plan breaks one. No network, no new permission, no new usage-count word (TRACKER maps to nothing, `Screen` stays three), no rating or share copy, Do Not Disturb untouched, the watch never asked for, every language packaged, translations by Claude. The disclaimer change is the owner's, recorded as reversed with his words, and the row stays visible; the dua request still appears once, in the full text only. One caution to keep in the rewrite of CLAUDE.md: the worktree copy already differs from `main` (rating, GBrain, `:app:installRtl`), so the 1.6 edit must be made on this branch's copy and merge over `main`'s after 1.5.0.

## (c) Verdict

Not yet complete enough to hand to a developer model. The features and rules are nearly all decided, but a model starting cold would build the wrong placement (gap 1), skip about a third of the work (gap 2), ship one alerts fault (gap 3) and fail `LocaleDisciplineTest` (gap 4).

Minimum to make it hand-over ready:
1. The precedence line in all seven files and the UX.md superseded banner (gap 1).
2. Section 4 rewritten as the one ordered table with files, keys, tests, docs and screenshots per step (gap 2), dated against the 4 Dec cut.
3. 8.4 corrected for the reminder restore, with the two message keys and the test case (gap 3).
4. 8.10 names the region source and the test (gap 4).
5. 8.13 and 8.14 reconciled on the sky's voice (gap 5).
6. The paused tick state in 8.6 and 6.2 (gap 6).
7. 8.5 completed: the Ramadan cards as a question, month definition, scroll to today, the §11 row and the two inventory rows (gap 7).
8. The disclaimer checklist gains README.md:162, HANDOVER:456 and `action_understood` (table item 3).
9. The six stale lines struck in place (gap 9), and the tab's order, icon, file name and snackbar offset written down (gap 10).
10. A dated line in HANDOVER §11 and PROJECT_BRIEF saying what is on the branch (gap 12).
11. Ali's 15 answers recorded in §9, plus question 16 (jama'ah zone guard).

Everything else in (b) can be fixed inside the build with a note, and nothing here needs a device to settle.
