# Widgets, Ramadan pack and prayer tracker: implementation plan (October 2026)

Written 16 October 2026 against `main` at commit `13b581d` (phone 1.3.2, versionCode 7, live and parked).
Author: Claude, as a planning session with read access to the repo and no build tools. Nothing in this
folder is built. Every claim about the code below cites a file and line that was read on that commit.
Where something was not verified it is marked **assumed**.

## What this folder is for

The owner has agreed three features. This folder holds everything a later Claude Code session needs to
build, test, record and release them with the owner only reviewing, merging and pressing the Play Console
buttons. Read the files in this order.

| File | What it holds | Who reads it |
|---|---|---|
| `README.md` (this file) | Index, release split, one screen summary, status table | Everyone, first |
| `PLAN.md` | The master work breakdown: ordered tasks, exact files, signatures, pseudo code, snippets, edge cases, verification per task, scheduler analysis, DataStore design, widget refresh design, risk register, rollback, definition of done | The implementing session, task by task |
| `RAMADAN-LOCATION-SPEC.md` | The product and religious wording design for the location based Ramadan features, with test vectors and golden values | The implementing session before any Ramadan task, and the owner for the three decisions |
| `WORDING-AND-LISTING.md` | Claims audit of README, store listing, translated listings, privacy policy and in app strings, plus every new string in four languages and the store text for both releases | The implementing session for every string, the owner before pasting into Play |
| `TEST-AND-RECORD.md` | Test strategy, instrumented tests in the `docs/release-tools` style, regression gates, pre release checklist, what to record where, who does what, post release monitoring | The implementing session at every gate |
| `SESSION-PROMPTS.md` | Two copy and paste prompts for Claude Code, one per release | The owner, to start each build session |
| `reference/` | The two Java programs that produced the calendar facts and golden values, with a README | The implementer, only to regenerate or extend the values |

## The release split and the order

**Release A, phone 1.4.0, versionCode 8: Ramadan pack first, then the two widgets.** Ramadan 1448 begins
on Monday 8 February 2027 on the Umm al-Qura calendar (verified with `java.time` `HijrahChronology`, see
`RAMADAN-LOCATION-SPEC.md` section 3). The pack has a hard date and the widgets reuse its shared Hijri
code, so they ship together. Inside the release the Ramadan tasks come first so that, if time runs out, the
pack can ship as 1.4.0 on its own and the widgets as 1.4.1 without reordering any work. Target: **the rollout date is an open owner decision** (amended 16 October 2026, see Amendment 1). The
build and tests are completed first. The owner then reviews the emulator captures of every Ramadan stage
and decides whether 1.4.0 goes out months ahead with the Ramadan pack dormant, or later. The original
target of a pull request ready by 10 January 2027 is kept as the latest sensible date.

**Release B, phone 1.5.0, versionCode 9: the prayer tracker, phase A only.** No deadline, the most design
and privacy risk, a privacy policy change, a second DataStore file and the system file picker. Kept apart so
a problem in it cannot delay the Ramadan pack and so its policy change goes live on its own. Phase B of the
tracker (missed prayer counter, notification action, widget tick) is not planned here and needs a separate
owner decision.

The watch (`:wear`, 1.2.0, versionCode 1001) is not changed by either release and its bundle is never
uploaded with a phone release.

## One screen summary

- **Widgets.** Two `AppWidgetProvider` widgets drawn with `RemoteViews` and XML layouts, no Glance, no new
  dependency: "Next prayer" (2 by 1) and "Today" (4 by 2). Times come from the same `PrayerEngine` calls
  the Times screen uses. Refreshed by every existing reschedule trigger plus one new non waking alarm (exact
  when the phone allows it, inexact otherwise) at the next transition, so a stale widget can only ever lag its highlight, never show a wrong time.
  No location read, no network, no new permission.
- **Ramadan pack.** A shared `HijriCalendar` in `:core` (the Home header and the Umm al-Qura Isha rule
  both move onto it). On the Times screen during Ramadan: a card with today's suhoor end (Fajr) and iftar
  (Maghrib) for the user's own place, a countdown to whichever is next, the length of today's fast and its
  change from yesterday, tomorrow's two times, the Ramadan day number and the expected last day, an info
  note that says plainly what the app does and does not decide, and a link to a full timetable for the
  month, also exportable as a PDF through the existing exporter. A notice in the last days of Shaban points
  people who follow local sighting at the existing Islamic date shift. An optional suhoor reminder, off by
  default, scheduled through the existing alarm ladder with its own action and request code band. No
  imsak time is invented and no country table is shipped.
- **Tracker.** Off by default. A tick per prayer on the Times screen, a 30 day history sheet, excused
  days with no reason stored, a streak that excused days do not break, save and load through the system
  file picker as versioned JSON, delete with one confirmation. Its own DataStore file, already excluded
  from backup and transfer by `data_extraction_rules.xml`. Nothing leaves the phone and nothing is added
  to the usage counts.

## Guardrails that every task in this folder respects

Free, no ads, no accounts, no server. Coarse location, foreground only, never transmitted, never backed up.
No new dependency, no Glance, no navigation library, no DI, no Play Services Location, no network at runtime
for times. No new analytics event (the closed set in `UsageCounts.kt` stays closed, `UsageCountsTest`
enforces it). No rate, review or share prompt. The religious disclaimer is not softened and no ruling is
invented. Translations are written by Claude without native speaker review and the app never says
otherwise. Wear strings stay English. `bundle.language.enableSplit` stays false. The watch bundle is never
uploaded with a phone release. Claude never pushes to `main` and never presses submit, publish or merge.
UK English, no em dashes, no en dashes, no semicolons in anything user facing or in these documents.

## Four decisions for the owner (each has a default, so he can ignore them)

| # | Decision | Default, applied unless he says otherwise |
|---|---|---|
| 1 | Release split and order | A = Ramadan pack then widgets (1.4.0), B = tracker (1.5.0). Suhoor reminder ships in A, off by default |
| 2 | How Ramadan's start is decided for a user | No country table. Umm al-Qura plus the user's own date shift, an eve notice that points to the shift, an info note that says what the app does and does not decide (`RAMADAN-LOCATION-SPEC.md` section 6) |
| 3 | Turkey: the app's Diyanet method is plain angles and gives iftar about 7 minutes earlier than Aladhan's Diyanet output, which appears to carry Diyanet's precaution minutes | Leave the method alone, rely on the note and "Match your mosque", and have the implementer compare one date with Diyanet's own table and record the result (`RAMADAN-LOCATION-SPEC.md` section 9.4). Any change to the method is a separate, owner approved commit. **Compared 16 Oct 2026: Diyanet's own table carries the offsets (Maghrib 7 to 8 minutes after the engine's), so the app's iftar for Turkey is early against the ezan. Recommendation: approve the offsets as a separate commit before Ramadan** |

| 4 | When Ramadan shows up in the app, and when 1.4.0 is rolled out | Visibility: Amendment 1 below, built as constants that can change. Nothing about Ramadan on Home until 7 days before the start. **Rollout date: OPEN.** Claude recommends early (reasons in Amendment 1) but the owner decides after reviewing the emulator captures. Nothing is submitted to Play until he does |

Stated defaults that are not decisions: widget previews below Android 12 are two small PNGs captured from
the emulator (the first raster assets in the app, under 30 KB each). The Times screen scrolls during
Ramadan on short phones. The widget countdown's digits follow the phone's language.

## Decisions on Release B (16 October 2026)

The owner answered four questions about the tracker, then added the Pause idea and asked about analytics. They override `PLAN.md` section 7 (B1, B3, B4, B6),
`TEST-AND-RECORD.md` section 2.3 and the one screen summary above where they differ.

1. **Streak: hidden until switched on.** The Settings switch "Show streak" is off by default.
2. **Excused days: dropped, replaced by a Pause (the owner's idea, same day).** First answer: "Lets not
   complicate it", and the option might make female users uncomfortable. Then he proposed: when someone
   cannot pray (for example during a period), they pause the tracker, the streak stops where it is, and
   when they resume it carries on unbroken. That is adopted because it needs no label and no reason, so it
   is neutral and also covers illness and travel. How it works:
   - A **Pause tracking** action in the tracker's Settings group, and a one line "Tracking is paused. Resume"
     on the Times screen while it is on, so it is not forgotten. No reason is asked or stored.
   - It is **not** the "Show streak" switch. That one only shows or hides the number. If hiding paused the
     streak, everyone on the default (hidden) would be paused.
   - Stored as date ranges (start date, and an end date once resumed), not a flag per day, so nothing needs
     to be opened each day. The pause can start today or yesterday, so someone who notices a day late is not
     penalised. Resuming makes today a normal tracked day.
   - The history sheet shows paused days as "Paused" and nothing else. The streak skips them: they neither
     add to it nor break it. A consecutive streak therefore stays the measure (the "last 30 days" count
     Claude proposed earlier is withdrawn).
   - Code: `DayRecord` has no excused bit (bit 5 stays reserved and is ignored on load), `TickState` has no
     `EXCUSED` (a paused day is `PAUSED`), `TrackerLogic.streak` takes the pause ranges, the save file
     carries the ranges, and `TEST-AND-RECORD.md` 2.3 gains cases for a pause that bridges, an open ended
     pause, a same day resume, a one day back start, adjacent and overlapping ranges, and a year boundary.
3. **Phase B of the tracker (missed prayer counter, notification action, widget tick): not planned.** Keep
   it simple.
4. **Analytics and personal details: none added.** The tracker collects no details, has no account, and
   adds nothing to the opt-in usage counts (ticks, pauses and streaks are faith practice data and never
   leave the phone). How well it is used is read from Play Console's own aggregate numbers (installs,
   retention, crashes) and the existing opt-in screen counts. Revisit only if he asks for a measure.
5. **Timing: delegated to Claude ("I trust you will make the best and appropriate decision").** Order: the
   1.4.0 alarm scheduler change goes first and is proven in the field. Release B starts when 1.4.0 has been
   live for about two weeks with a clean health check, and aims to be live by mid December 2026. No release
   from 18 January to 9 March 2027 (the three weeks before Ramadan 1448, 8 Feb to 8 Mar, and Ramadan itself)
   except a critical fix. If 1.4.0 is not live by early December, Release B waits until after Ramadan. This
   is Claude's rule, not a Play requirement. Release B still branches from the merged Release A and still
   needs its privacy policy change live on `main` before the owner submits.

## Amendments made during the build

### Amendment 1 (16 October 2026): Ramadan visibility and rollout timing

The owner asked, mid build, whether a Ramadan screen weeks before Ramadan would make the app feel odd and
asked for evidence before any early rollout. The answer is recorded here so a later session does not
re-open it. It changes the windows in `PLAN.md` A1 to A4 and `RAMADAN-LOCATION-SPEC.md` 4.6, 4.7 and 4.10.

**Visibility windows** (all measured on the user's own calendar, after their Islamic date shift, and held
as constants in `RamadanInfo` so nothing else hard codes a number):

| Days before 1 Ramadan | What the user sees |
|---|---|
| More than 30 | Nothing on Home. Settings has the Suhoor reminder row, Off by default |
| 30 to 8 | Only the Ramadan timetable PDF in the export sheet. A pull surface, nothing pushed |
| 7 to 1, and days 1 and 2 of Ramadan | One quiet, dismissible notice (neutral surface, not amber) with the start date on this calendar, the sighting note and an action to preview the timetable. Never returns once dismissed that Hijri year. Replaced by the card from day 1, so on days 1 and 2 it becomes a single line |
| Ramadan | The Ramadan card, unless the user switched it off in Settings ("Show Ramadan card", on by default, also reachable from the card's info dialog). Not everyone fasts, so the card must be removable |
| After the last day | Card, notice, export option, reminder and widget labels all end at local midnight into 1 Shawwal. No Eid greeting from the app: Eid is announced by mosques, and a wrong day for someone still fasting is worse than silence |

The original notice window (2 days before) is replaced by 7 days. The widgets and the suhoor reminder are
unchanged: the labels appear only on Ramadan days and the reminder only on Ramadan days, as before.

**Rollout (Claude's recommendation, NOT decided: the owner asked on 16 October 2026 to finish development
and testing first, review the UX on the emulator, and then decide).** Ship 1.4.0 as soon as the gate passes.
Reasons: the alarm scheduler change runs for every user
every day and should be found faulty in November, not in the week Ramadan starts. Widgets are useful all
year. A January release risks a Play review delay at the worst moment and leaves users who have not updated.
The 1.4.0 What's new leads with the widgets. Ramadan text goes into the store listing in early January as a
listing edit with no new bundle. If the owner decides on a later release instead, only the date moves: the
build, tests and windows are identical. In-app start and end need no scheduler (they follow each user's
date). Claude can set scheduled reminders for the Play steps once the owner decides, and never presses
submit or publish. `WORDING-AND-LISTING.md` section C holds the Ramadan lines as "held until
January" and A10 prepares them separately.

**Evidence and limits** (fetched 16 October 2026). TGM Research, Malaysia, February 2023, n about 780: 35 per
cent start Ramadan preparations 1 to 2 weeks before, 19 per cent under a week, 10 per cent just before, 36
per cent more than 2 weeks before. Statista 2022: Malaysia 32 per cent at 1 to 2 weeks, Nigeria 28 per cent.
East London Mosque published its Ramadan timetable 17, 16 and 14 days before the start in 2022, 2023 and
2024, each time saying the start depends on moon sighting. Google shopper data puts planning at up to 8
weeks before (shopping, not prayer apps). An AGBI column citing Google data says religious app download
searches rose 1,860 per cent in the first two weeks of Ramadan 2024, so the pack must be ready on day one.
Limits: Asia and Africa surveys, not the UK. They measure general preparation, not what a prayer app should
show. Nobody measured whether an early notice feels odd, and there is no analytics to test it, so the
cautious window was chosen. A window that is too short can be lengthened in an update, one that annoys
people cannot be taken back, so the constant favours short.

**What changes in the code.** `RamadanInfo` gains `showsNotice(today)` (daysUntilStart in minus 1 to 7) and
`offersTimetable(today)` (daysUntilStart in 0 to 30, or inside Ramadan). `RamadanState` is built when
`showsNotice` or `isRamadan`. The notice taps through to the timetable sheet (the Islamic date shift link
lives in the sheet footer and in the info dialog). A new setting `ramadanCardEnabled` (default true) and a
"Show Ramadan card" switch. The notice window and the card switch each have a unit test.

## How to use these files

1. Start a Claude Code session with the matching prompt from `SESSION-PROMPTS.md`.
2. The session reads the repo's standing documents (`CLAUDE.md`, `AGENTS.md`, `docs/HANDOVER.md` STATE OF
   PLAY) and then this folder, in the order above.
3. It works through `PLAN.md` task by task, running the verification command named in each task, and
   updates the status table below in the same commit as the task.
4. At each gate in `TEST-AND-RECORD.md` it runs the gate, records the result, and stops at the stop
   conditions in `SESSION-PROMPTS.md` rather than guessing.
5. The owner reviews and merges the pull request, pastes the store text, and presses the Play Console
   buttons, one screen at a time, as `CLAUDE.md` already requires.

## Status table (the implementer updates this in the same commit as the work)

Status values: `not started`, `in progress`, `done`, `blocked`, `dropped`. Fill the commit column with the
short hash. Do not mark a task done until its verification command in `PLAN.md` has passed.

| Task | Release | Status | Commit | Notes |
|---|---|---|---|---|
| A0 Baselines captured (tests, lint, bundle size, merged permissions, dependency list) | A | done | 3140b96 base | Cloud, 16 Oct 2026, `--rerun-tasks`: 172 unit tests (core 83, app 77, wear 12), 0 failures. `app-release.aab` 5,779,975 bytes. 11 permissions (the six app ones plus library ones plus the Firebase install referrer one), none of AD_ID or ADSERVICES. `releaseRuntimeClasspath` 1,014 lines. Lint 0 errors. Numbers are in `build/plan-baseline/`, which is gitignored |
| A1 `HijriCalendar` and `RamadanInfo` in `:core`, engine and Home header moved onto it | A | done | 9132017 | Also `RamadanInfo.showsNotice` and `offersTimetable` (Amendment 1). `HijriCalendarTest` 13 tests, new `RamadanGoldenTest` 8 tests (the engine matches the pinned mirror values on the first run: Fajr and Maghrib to the minute at all ten setups, the rest within a minute, Makkah Isha by offset table exact, London DST change +5, Tromsø projected ranges exact). Unit total 172 -> 193, 0 failures. `HijrahDate` now only in `HijriCalendar.kt` |
| A2 Ramadan state in `SajdaViewModel` and `UiState` | A | done | 2d3fd7b | `RamadanState` lives in `app/ui/RamadanState.kt` with a pure `build` (the ViewModel only calls it), built when inside Ramadan or the 7 day notice window. `RamadanStateTest` 11 tests. Unit total 193 -> 204 |
| A3 Ramadan card on the Times screen, info note, eve of Ramadan notice | A | done | 572772d | `RamadanCard.kt` (card, 7 day notice, info dialog), `Show Ramadan card` switch in Settings (default on), notice dismissal stored per Hijri year. Placed below the default location and polar banners (AC9, AC29). Strings in four languages (English 244, others 242, gate 9 holds). Debug build and lint clean; unit total 204, 0 failures. Not yet seen on a screen: the emulator pass is Ali's, through the release script |
| A3b Eid greeting (added by Ali, 16 Oct 2026) | A | done | e823fe5 | Calendar driven: `HijriCalendar.eidOn` finds 1 Shawwal and 10 Dhul Hijjah, so 2027, 2028 and later need no entries; pinned in `EidTest` (Fitr 9 Mar 2027, Adha 16 May 2027, Fitr 26 Feb 2028, Adha 5 May 2028, plus 2026 and 2029). Follows the user's date shift. Once per Eid: shown on the day until closed, then never again for that Eid (stored code = Hijri year x 100 + month). Independent of the Ramadan card switch. Greeting only, no notification, no analytics event. Main text is an unqualified warm greeting; the fine print says it is our calculation, sorry if you celebrate on another day, follow your mosque (Ali's direction). `EidWordingTest` pins that shape in all four languages. Strings 249 / 247 (gate 9 holds). Unit total 204 -> 221 |
| A4 Ramadan timetable sheet and `Range.RAMADAN` PDF | A | done | 79b4ec8 | `RamadanTimetableSheet` (opens from the card only, AC19; rows are plain engine days via `RamadanTimetable.rows`, computed off the main thread when it opens, closes itself if the calendar leaves Ramadan). PDF: `Range.RAMADAN`, columns Day / weekday / date / six times totalling 523 pt, small second header line "Suhoor ends" under Fajr and "Iftar" under Maghrib (shrinks to fit any language), those two columns in bold so they stand out on a black and white printout, one line note under the masthead, file `PrayerTimes_Ramadan1448.pdf`; offered in the export sheet 30 days before day 1 to the last day. **Added by Ali, 16 Oct 2026:** (a) masthead spacing for every PDF range: the place name sat about 1 pt under the rule below the SajdaTime wordmark, now mark, rule, place, qualifier and hairline each have their own air (checked on a rendered geometry mock, old against new, since there is no emulator in the cloud); (b) during Ramadan the daily times list on the Times screen shows "Suhoor ends" under Fajr and "Iftar" under Maghrib, driven by the same Ramadan state so they appear on day 1 and are gone at midnight into 1 Shawwal, and they follow the Ramadan card switch. Deviations from the plan: rows live in the sheet (`produceState`) and not in `UiState.ramadanRows`, so nothing is carried when the sheet is closed; `spanOf` and `fileNameFor` sit in the exporter's companion so they are testable without a Canvas. Tests: `RamadanPdfRangeTest` 8, `RamadanTimetableTest` 7 (London pinned to RAMADAN-LOCATION-SPEC 9.2 on day 1, 15 and 29). Unit total 221 -> 236, 0 failures. Strings 260 / 258. Not verified: the PDF has not been rendered by Android's `PdfDocument` (cloud has no device); a 30 day month with a two line note stays on one page by the geometry mock (ends at y 785 of 790), a polar month with the approximate note spills one row to page 2 by design. A `RamadanPdfDeviceTest` renders it with `PdfRenderer` in the A10 device suite |
| A5 Suhoor reminder: settings, Settings row, scheduler, receiver, notification | A | done | A5a 1cdf8cc, A5b this commit | **A5a:** `AppSettings` suhoor fields (`suhoorReminderMinutes` 0/15/30/45/60 default 0, `suhoorReminderStyle`), DataStore keys and setters, ViewModel setters, `SuhoorReminderPlan` (pure), Settings row "Suhoor reminder" under Reminders with a dialog (off or 15 to 60 minutes before Fajr, notification or alarm, and the alarm sound and silent switch shown there too when it is an alarm), strings in four languages. The alarm options in the prayer Alerts dialog now show when a prayer or the reminder is an alarm (`AppSettings.usesAlarm`). **A5b:** `PrayerAlarmScheduler` lays reminders down after `cancelAll` and before the `alertFor.isEmpty()` early return (a person can switch every prayer alert off and keep the reminder), through the same three rung ladder via a shared private `schedule(manager, pendingIntent, at)`; the planner call is wrapped in `runCatching` so nothing about a reminder can stop prayer alarms. Request codes `10_000 + dayOfYear`, own action `SUHOOR_REMINDER`, extras `fajr_at` and `date`. `PrayerAlarmReceiver` branches on the action; `shouldPostReminder` drops it when switched off, outside Ramadan on the calendar in use now, or more than five minutes past its time. `Notifications.postSuhoorReminder` (id 3000) uses the same style rules, channels and `effectiveAlertStyle` as a prayer alert (no third channel), rounds the countdown to the nearest minute, and times itself out at Fajr so "Fajr begins in 30m" is not left on screen after Fajr (one line beyond the plan). Tests: `SuhoorReminderPlanTest` 13, `RequestCodeTest` 6, `SuhoorReminderReceiverTest` 10. Unit total 236 -> 265 (core 114, app 139, wear 12), 0 failures, forced `--rerun-tasks` gate 1 green with lint. Gates run: 1, 7 (analytics only in `UsageCounts.kt`), 8, 9 (270 / 268 / 268 / 268, difference 2), 14 (wear untouched), 4 (11 permissions, identical to baseline). **Bundle 5,988,863 bytes, +208,888 against 5,779,975, inside the 300 KB gate but with 91 KB of headroom left for the widgets (to investigate at A8: suspected the Umm al-Qura calendar data brought in by desugaring plus four languages of strings).** Not verified: nothing has run on a device (`dumpsys alarm` check and `SuhoorReminderDeviceTest` are in the A10 device suite); `RequestCodeTest` gains the widget refresh code at A7 |
| A6 Widget state builder and renderer, providers, layouts, resources | A | done | A6a 3650ec6, A6b this commit | **A6a done:** `WidgetState` (pure; includes the Ramadan card switch, so the widget words go when the card is switched off), six widget colours light and dark, corner radius dimen (system radius on Android 12 and later), three drawables, two layouts, two provider files, two picker previews, 12 widget strings in four languages (15 in A6a, three removed in A6b) (Indonesian says "salat" like the rest of the app, not the "sholat" in the first draft). Tests: `WidgetStateTest` 14, `WidgetColorsTest` 5 (every colour equals its `Color.kt` twin in both themes, seven contrast pairs), `WidgetResourcesTest` 9 (no `android:text`, children hidden from TalkBack, alignment on every text view, ids, `updatePeriodMillis` 0, previews under 30 KB). Unit total 265 -> 294, 0 failures. **Deviations from the plan, all found by counting heights and widths rather than from a device:** (1) the Today widget is **4 by 3**, not 4 by 2. Six rows at 13sp need about 165 dp and a 4 by 2 slot is 110 dp. A two column grid fitted the height but left no room for the Ramadan words and cut "Maghrib" at 250 dp wide. The Ramadan word sits in the middle of the row, so it adds no height. (2) Next prayer is **3 by 2**, not 2 by 1 (see A6b below for why it is not 2 by 2 either). (3) There is **no `widget_on_highlight` colour**. A text colour chosen per row while drawing is baked into the `RemoteViews` at that moment and stays wrong after the phone changes theme until the next refresh, whereas colours in the layout files are resolved by the launcher each time it draws. The ordinary text colours already clear 4.5:1 on the highlight in both themes (tested). (4) A footer line "Approximate time" on the Today widget for projected polar days, so no row presents a guess as a measurement (the Next widget already had it). (5) The picker previews are **drawn** by `tools/make-widget-previews.py` from the layout numbers, not captured, because the cloud has no emulator; they show sample London times and no Ramadan words. Ali can replace them with captures if he prefers. (6) `app/build.gradle.kts` test inputs now declare every `values*/strings.xml`, the colour files and the widget files: `EidWordingTest` reads the three translations off disk and they were not declared, the bug class in lesson 84. Proved by breaking the Urdu fine print and a layout and watching the right test fail without `--rerun-tasks`. **A6b done:** `WidgetRenderer` (both layouts, every dynamic property written both ways because a launcher may reapply actions), `WidgetUpdater`, the two providers, two receivers in the manifest (`exported=false`), `WidgetManifestTest` 3. Unit total 298 (app 172, core 114, wear 12), 0 failures, lint adds no new kind of warning. **Verified by rendering, not by reading:** a scratch Robolectric harness in a copy of the repo (kept out of the repo, method in HANDOVER) inflated the real layouts, applied the real `RemoteViews` actions and drew PNGs for ordinary, Ramadan afternoon, before Fajr, eve of Ramadan, no countdown, no location, polar, font 1.3 and 2.0, Urdu, Turkish and Indonesian, plus a report of every ellipsised text view. It found four things a read could not: a plain `View` (the divider) is not allowed in a `RemoteViews` layout, the 2 by 2 Next widget clipped its countdown, the Today footer clipped, and the Today header wrapped in Indonesian. **Deviations from A6a:** (7) Next prayer is **3 by 2** (minimum 180 by 110 dp) as a vertical stack: name, time, one small context line, countdown. The context line holds "Tomorrow" and either the approximate marker or the Ramadan word, joined when both apply, and the polar marker wins over the word. Order is by importance so any clipping at large fonts takes the least important line first. (8) The Today widget has **no header**: the highlighted row already says which prayer is next, and the header was the line that wrapped in Indonesian and cost a row at font 1.3. The no location message takes the first row's place. The next prayer and its remaining time are still spoken, as the first sentence of the content description. (9) Small text is 11sp, not 10sp (lint `SmallSp`), checked again after the change. (10) Widget strings are 12, not 15: the header and next line strings went with the header. **Known limits, recorded not hidden:** at font scale 2.0 the Today widget at its minimum size shows four rows and a clipped fifth, so the user has to make it taller (TalkBack reads all six rows in any case), and the Next widget at 1.3 clips its countdown in the worst case only (tomorrow plus a Ramadan word). The countdown digits follow the phone's language, not the app's, because a `Chronometer` formats itself. |
| A7 Widget refresh scheduler and the hooks at every existing trigger | A | done | A7 this commit | `WidgetRefreshScheduler` sets one non waking `RTC` alarm (request code 20,000, action `com.sajdatime.app.action.WIDGET_REFRESH`) for the earlier of the next prayer and local midnight, plus two seconds; exact when exact alarms are allowed, inexact otherwise, never `setAlarmClock`; cancelled when no widget is placed or there is no location. `WidgetRefreshReceiver` (not exported) redraws, and **every redraw lays down the next alarm** (`WidgetUpdater.refreshAll`), so any trigger rebuilds a lost chain. Hooks added at: `PrayerAlarmReceiver` (both branches), `DailyRescheduleWorker`, `SystemEventReceiver` (boot, clock, time zone, package replaced, exact alarm permission, and now the phone's language, `LOCALE_CHANGED`), the view model's `recalculate`, and `MainActivity.onResume`, which is also where a language chosen in the in app picker reaches the widgets (the activity is recreated with the new resources first). Both providers redraw on `onDisabled`, which cancels the alarm when the last widget goes. Tests: `WidgetRefreshSchedulerTest` 8 (the two seconds, midnight winning after Isha, local not UTC, a zone that skips midnight, and a sweep over six zones and a year proving the answer is always in the future and under 26 hours, so the alarm cannot loop), `SystemEventActionsTest` 3 (the manifest filter and the code handle the same actions, proved by deleting the manifest line and watching it fail without `--rerun-tasks`), `RequestCodeTest` +1, `WidgetManifestTest` +1. Unit total 298 -> 311. **Verified in the scratch Robolectric harness, not on a device:** one alarm after three schedules, type `RTC`, trigger exactly at the boundary plus two seconds, midnight after Isha, nothing left set after cancel or with no location, no widget placed clears a stale alarm, and a real broadcast to the receiver redraws and chains the next alarm while a foreign action does nothing. **Not verified:** that the alarm actually fires on a device, in Doze, or on HyperOS (the device test in A10 checks `dumpsys alarm`). **Known weakness, accepted:** a non waking alarm on an aggressive OEM may be parked until the phone is next used, so with prayer alerts off the highlight can lag until then. The clock times are for the calendar day and stay right; only the highlight and the countdown can lag, and the first redraw after any wake corrects them. |
| A8 Ramadan labels on the widgets | A | done | A8 and A9 this commit | The labels were wired in A6 (`ramadanToday`, `ramadanOnNextDay`), so A8 is verification. `WidgetStateTest` already holds the cases the plan asks for: a day of Ramadan, the eve at 23:00 (tomorrow's Fajr is already Ramadan), the last day after Isha (tomorrow's Fajr is Shawwal's and carries no word), the first of Shawwal, the card switch off, the Islamic date shift moving the words, and the word table (Suhoor ends for Fajr, Iftar for Maghrib, nothing else). **Rendered at 2027-02-22 15:00 in English and Urdu, light and dark** through the scratch Robolectric harness (contact sheet: `reference/widget-renders-robolectric.png`, drawn by a JVM layout engine, so it is a close check of layout and wording and not a pixel match for a real launcher): Fajr row "Suhoor ends", Maghrib row "Next · Iftar", Next widget context line "Iftar", eve "Tomorrow · Suhoor ends". Also found and fixed here: the Next widget's no location message was 11sp in grey and is now 14sp, written both ways. Device assertion (two label views visible with the right text at that clock) goes into the A10 device test |
| A9 Strings in en, ur, tr, in for everything in A | A | done | A8 and A9 this commit | The strings were added with each feature (A3 to A6) so lint stayed clean, so A9 is the check. `RamadanWordingTest` 12 tests over the 57 Ramadan, suhoor, Eid and widget string keys (plurals are checked by lint): the info note says "as calculated", "does not add", "follow your mosque" and "Islamic date"; "imsak" appears only in the one sentence that says timetables print it, and no key is named for it; no ruling words (you may, you must, you should, stop eating, valid, permitted, forbidden, halal, haram, allowed); the mosque is named wherever a timetable could differ; the eve notice names what mosques do and what the app does; both widget descriptions say they follow dark mode; the widgets use the card's two words; and in all four languages every key exists and is not blank, placeholders match the English exactly, are numbered, and no string has a dash or a semicolon. Proved by breaking an English string with "You may" and an Indonesian placeholder and watching the right test fail. Lint: 0 errors, no `MissingTranslation`. Unit total 311 -> 323. The four language screenshots at the Ramadan clock are part of the A10 release script |
| A10 Docs, listing, privacy, version bump, release gate, device suite | A | done | A10a dced529, A10b c90a56a, A10c bbfbce0, A10d to A10i notes only | **A10a done:** version bump to versionCode 8 / 1.4.0 (watch untouched, `git diff origin/main -- wear/` empty). **Gates run in the cloud on this commit, forced with `--rerun-tasks`:** 1 (323 tests, 0 failures, fresh result files, lint 0 errors in app, wear and core, both bundles build), 4 (the same 11 permissions as 1.3.2, no AD_ID or ADSERVICES), 5 (`releaseRuntimeClasspath` identical to the baseline, no new `implementation(` line), 6 (see below), 7 (no analytics call outside `UsageCounts.kt`), 8 (no `android:text` in widget layouts, no `Locale.getDefault`), 9 (English 282, each translation 280, difference 2 as required), 13, 14. **Bundle size gate 6:** `app-release.aab` is 6,028,577 bytes, +248,602 over 5,779,975 (within the 300 KB gate, but not by a wide margin). I built 13b581d separately (5,779,901) and diffed the entries: the R8 mapping file is +146,022 (BUNDLE-METADATA, kept by Play for crash reports, never sent to a phone), `classes.dex` +66,460 compressed (all the Ramadan, widget, suhoor and Eid code), the two preview PNGs +19,044 (13,903 and 5,141), compiled resources +8,705, widget layouts and XML +5,400, manifest +491. What a phone downloads is therefore about 100 KB more, not 248 KB. **Not run here, because there is no emulator:** gates 10 (RTL), 11 (device suite) and 12 (store screenshots), all of which `release-1.4.0.sh` runs. **Device tests written and compiled, never run:** `WidgetDeviceTest` (both widgets bound through the system widget service and drawn by a host, text and highlight checked against the engine, redraw alarm present while a widget is and gone after, pictures saved), `RamadanDeviceTest` (the desugared Hijri table on a device, the Eids, the London card numbers), `SuhoorReminderDeviceTest` (one reminder at the planned minute in `dumpsys alarm`, no prayer alarm moved, off means none), `RamadanPdfDeviceTest` (every range exports, the Ramadan file is named for the year, first pages rendered to pictures), `ScreenSeedDeviceTest` (not a check: skips first run for the screenshots), shared helpers in `DeviceSupport`. **`docs/release-tools/release-1.4.0.sh`:** the 1.3.2 script plus permission, size and watch checks on the bundle, and a rebuilt step 6 that runs the tests, grants the widget bind permission, sets the emulator clock to 21 Feb 2027 23:00, 22 Feb 10:00, 6 Feb, 8 Mar 20:00, 9 Mar, 16 May and 21 Jun, takes the screenshots in four languages and the sheets, runs the widgets at font scale 2.0 and puts everything back. **A10b done (docs, 16 Oct 2026):** `docs/HANDOVER.md` (header, §2 not used, §3 file map and `AppSettings`, §4, §5.6, §5.11, §5.19 Ramadan and Eid, §5.20 widgets, §6 alarm families, §8, §9 with the real count 323 and a row for every new suite, §10 evidence, §11 STATE OF PLAY rewritten and two "Not done" items corrected, §15 lessons 130 to 139), `docs/ARCHITECTURE.md` (3.10, the alarm families, privacy rows, the iOS note, the widget line at the old 571) and its PDF regenerated in the cloud with Chromium (the Mac script gives Helvetica, this one a Linux sans, content identical), `docs/DESIGN_SYSTEM.md` (Ramadan card, eve notice, Eid card, widget tokens), `docs/RELEASING.md` (version line, widget bind grant, merge publishes `docs/`), `README.md` and `docs/index.html` (widgets, Ramadan, languages, Hijri date, install command, permission sentence, PDF caveat), `docs/privacy.html` (the widget sentence, dated 16 Oct 2026, and the date assertion in `UsageCountsTest` moved with it), `docs/store/LISTING.md` (stale notes L1 to L4 corrected, new "Release A" section with two exact blocks: 3,830 characters for the 1.4.0 release with the Ramadan bullet unchanged, and 3,923 for January with the new Ramadan bullet, plus What's new), `docs/store/LISTING-LOCALES.md` (all eight languages: widgets, Ramadan, languages and Umm al-Qura lines, the four point HONEST section, Turkish semicolons and "imsak" fixed, the stale English only and native speaker notes replaced, every description measured under 4,000), `CLAUDE.md` (one sentence), `AGENTS.md` (line 12). **Two things to know:** (1) merging this branch publishes `docs/`, so `privacy.html`, `index.html` and the README mention features that are not live until 1.4.0 is, so merge when ready to submit, and if the merge is on a later date move the policy date line and the assertion in `UsageCountsTest` together. (2) The Ramadan lines of the store listing stay out of the 1.4.0 text by design and go in as the January block |
| A11 Release A submitted by the owner, live, health check re baselined | A | not started | | owner presses submit |
| B0 Baselines re captured on the merged A | B | not started | | |
| B1 `TrackerLogic` in `:core` (day record, states, streak) | B | not started | | |
| B2 `TrackerRepository` on its own DataStore, codec, tests | B | not started | | |
| B3 Settings group: switch, show streak, delete | B | not started | | |
| B4 Ticks on the Times screen and the history sheet | B | not started | | |
| B5 Save and load through the system file picker | B | not started | | |
| B6 Strings in en, ur, tr, in for everything in B | B | not started | | |
| B7 Privacy policy, docs, listing, version bump, release gate, device suite | B | not started | | |
| B8 Release B submitted by the owner, live, health check re baselined | B | not started | | owner presses submit |

## Corrections to the draft analysis this plan was built from

The owner's draft (the competitor analysis of 16 October 2026) was checked against the code. These are the
places it was wrong or thin, each fixed in the files here:

1. **The suhoor reminder cannot be "a new extra on the existing action".** `PendingIntent` identity ignores
   extras, so a reminder on `ACTION_PRAYER` with the same request code formula would replace a prayer
   alarm. It gets its own action and its own request code band, proved disjoint by a unit test
   (`PLAN.md` A5 and the scheduler analysis).
2. **`DailyRescheduleWorker` does not land after midnight.** It is a 12 hour periodic WorkManager job
   anchored to whenever it was first enqueued, inexact, with no window
   (`app/src/main/java/com/sajdatime/app/notify/DailyRescheduleWorker.kt:110`). The widget needs its own
   midnight and prayer time refresh alarm (`PLAN.md` A7).
3. **Prayer alarms only fire for prayers the user has an alert on** (`PrayerAlarmScheduler.kt:54`). A user
   with every alert off would never get a widget refresh from `PrayerAlarmReceiver`, which is the second
   reason the widget has its own alarm.
4. **The alarm horizon is yesterday, today and tomorrow, not "two days ahead".** `upcoming()` computes
   `HORIZON_DAYS + 1 = 3` days from `today.minusDays(1)` (`PrayerAlarmScheduler.kt:59-75`). Reminders are
   laid down inside the same window and nothing beyond tomorrow is ever scheduled.
5. **Request codes are `dayOfYear * 10 + slot.ordinal`** (`PrayerAlarmScheduler.kt:266-268`), range 10
   to 3665. The reminder band starts at 10,000 and the widget alarm uses 20,000.
6. **Home does not label tomorrow's Fajr as tomorrow.** `NextPrayerCard` shows the slot name and clock
   time only (`HomeScreen.kt:254-350`), and the Today list simply highlights nothing after Isha because
   `isNext` requires `!next.isTomorrow` (`HomeScreen.kt:724`). The widget adds a "Tomorrow" label because a
   widget has no list to make that obvious. `NextPrayer.isTomorrow` already exists (`PrayerModels.kt:184`).
7. **Sunrise is a dimmed row on Home, never highlighted as next or current** (`PrayerSlot.isPrayer`,
   `PrayerModels.kt:64`, and `HomeScreen.kt:756-757`). The Today widget copies that exactly.
8. **The Hijri offset is clamped to plus or minus 2 days, not "every offset the Settings row allows" as a
   wider range.** `CalculationPrefs.MAX_HIJRI_OFFSET_DAYS = 2` (`PrayerModels.kt:179`) and the repository
   clamps on write (`SettingsRepository.kt:274-279`). Tests sweep exactly minus 2 to plus 2.
9. **The Hijri date is computed twice today, privately, in two files**: `PrayerEngine.isRamadan`
   (`PrayerEngine.kt:433-436`) and `HomeScreen.hijriToday` (`HomeScreen.kt:894-927`). Both apply the
   offset as `HijrahDate.from(date.plusDays(offset))`. The plan lifts one shared `HijriCalendar` into
   `:core` and moves both onto it, so the card, the widget, the Isha rule and the header cannot disagree.
10. **`ExportSheet` lists `Range.entries`** (`HomeScreen.kt:515`), so adding a Ramadan range to the enum
    adds a row to the sheet automatically and the `when` in `ExportOption` fails to compile until it is
    handled. The plan filters the new range to the weeks it applies to.
11. **The PDF table already uses the full page width**: columns 72 + 91 + 6 by 60 = 523 points
    (`PrayerPdfExporter.kt:367-370`) against a 595 point page with 36 point margins. A Ramadan day column
    needs the widths rebalanced, specified in A4.
12. **The draft's "mark prayed from the widget", "lock screen widgets" and "Hijri date on the widget" are
    out of scope** and stay out. The draft's "no city name on the widget" is kept and is a privacy point,
    not a style one.
13. **The owner deferred Ramadan Fajr wording on 2 August 2026 (HANDOVER A11, `docs/HANDOVER.md:6188`)
    with "pick it up before the first Ramadan the app is live for".** This plan is that pick up. A11's
    conclusion is reused unchanged: keep 18 degrees, say plainly that local timetables may begin Fajr
    earlier, and never silently add an imsak margin.
14. **The app stores no country code**, only a display label such as "Slough, United Kingdom" or "Makkah"
    (`SettingsRepository.kt:246-251`, `PlaceName.kt:39-76`). The draft did not say so. It is the first
    reason no per country Ramadan start table can be shipped (`RAMADAN-LOCATION-SPEC.md` section 6).
15. **Umm al-Qura Ramadan Isha**: the live listing line "applied automatically during Ramadan" is true
    only for the Umm al-Qura method (`PrayerEngine.kt:419`). The listing rewrite says so.

## What was verified and what was assumed

Verified by reading the code on commit `13b581d`: everything cited with a file and line in these files.
Verified by running `java.time` on JDK 21 in the planning session: the Umm al-Qura dates in
`RAMADAN-LOCATION-SPEC.md` section 3. Verified by running adhan-java 1.2.1 through a Java mirror of
`PrayerEngine.compute` in the planning session and cross checking against the Aladhan API on 16 October
2026: the golden values in `RAMADAN-LOCATION-SPEC.md` section 9, within the tolerances stated there.
Verified by fetching the raw public Play page on 16 October 2026: the listing facts in
`WORDING-AND-LISTING.md` section A.

Assumed, and flagged where used. That `desugar_jdk_libs` 2.1.5 carries the same Umm al-Qura table as JDK
21 (a device test asserts it). That `View.setLayoutDirection` is remotable in `RemoteViews` on every
supported launcher (the widget device test renders it). That Play's release notes field is 500 characters
per language (the existing docs do not state it, `WORDING-AND-LISTING.md` section C says what to check).
That the Diyanet offsets observed in Aladhan's Turkey output are Diyanet's own. Now checked: they are, for
Ayas on three dates (Istanbul's page would not load), see `RAMADAN-LOCATION-SPEC.md` section 9.4.
