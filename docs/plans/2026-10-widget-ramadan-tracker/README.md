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
pack can ship as 1.4.0 on its own and the widgets as 1.4.1 without reordering any work. Target: pull
request ready by 10 January 2027, so a Play review that takes a week still lands before 1 Ramadan and
leaves room for one correction release.

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

## Three decisions for the owner (each has a default, so he can ignore them)

| # | Decision | Default, applied unless he says otherwise |
|---|---|---|
| 1 | Release split and order | A = Ramadan pack then widgets (1.4.0), B = tracker (1.5.0). Suhoor reminder ships in A, off by default |
| 2 | How Ramadan's start is decided for a user | No country table. Umm al-Qura plus the user's own date shift, an eve notice that points to the shift, an info note that says what the app does and does not decide (`RAMADAN-LOCATION-SPEC.md` section 6) |
| 3 | Turkey: the app's Diyanet method is plain angles and gives iftar about 7 minutes earlier than Aladhan's Diyanet output, which appears to carry Diyanet's precaution minutes | Leave the method alone, rely on the note and "Match your mosque", and have the implementer compare one date with Diyanet's own table and record the result (`RAMADAN-LOCATION-SPEC.md` section 9.4). Any change to the method is a separate, owner approved commit |

Stated defaults that are not decisions: widget previews below Android 12 are two small PNGs captured from
the emulator (the first raster assets in the app, under 30 KB each). The Times screen scrolls during
Ramadan on short phones. The widget countdown's digits follow the phone's language.

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
| A0 Baselines captured (tests, lint, bundle size, merged permissions, dependency list) | A | not started | | |
| A1 `HijriCalendar` and `RamadanInfo` in `:core`, engine and Home header moved onto it | A | not started | | |
| A2 Ramadan state in `SajdaViewModel` and `UiState` | A | not started | | |
| A3 Ramadan card on the Times screen, info note, eve of Ramadan notice | A | not started | | |
| A4 Ramadan timetable sheet and PDF range | A | not started | | |
| A5 Suhoor reminder: settings, Settings row, scheduler, receiver, notification | A | not started | | |
| A6 Widget state builder and renderer, providers, layouts, resources | A | not started | | |
| A7 Widget refresh scheduler and the hooks at every existing trigger | A | not started | | |
| A8 Ramadan labels on the widgets | A | not started | | |
| A9 Strings in en, ur, tr, in for everything in A | A | not started | | |
| A10 Docs, listing, privacy, version bump, release gate, device suite | A | not started | | |
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
That the Diyanet offsets observed in Aladhan's Turkey output are Diyanet's own (their site could not be
fetched during planning, see `RAMADAN-LOCATION-SPEC.md` section 9.4).
