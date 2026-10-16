# Test strategy and recording plan

Written 16 October 2026 against `main` at `13b581d`. Companion to `PLAN.md` (which names the tests per
task) and `RAMADAN-LOCATION-SPEC.md` section 9 (the golden values). This file is the gate: what runs,
where, what must stay green, what is recorded afterwards, and who does which step.

## 1. Principles (from the project's own rules)

- **Compiling is not working. Run it** (`CLAUDE.md`). Every feature has a unit layer, an emulator layer,
  and a by hand check with the emulator clock set to Ramadan 1448.
- **Verify against an independent reference, not your own reasoning** (`CLAUDE.md`). The Ramadan golden
  values were cross checked against Aladhan in the planning session and are pinned so the suite stays
  offline, exactly as `PrayerEngineTest` does.
- **A test that reads a file off disk must be declared as a task input** in `app/build.gradle.kts`
  (lines 184-233) or it goes `UP-TO-DATE` after the very edit it guards (`docs/HANDOVER.md` lesson 84).
  New file reading tests in this plan: `WidgetColorsTest`, `WidgetResourcesTest`, `WidgetManifestTest`,
  `SystemEventActionsTest`, `RamadanWordingTest`, `TrackerPrivacyTest`, `TrackerWordingTest`. Each
  task that adds one adds its files to the `disclaimerCopies` input block.
- **A cached pass is only as good as the run it was cached from.** Gates run with `--rerun-tasks` and
  the XML timestamps are checked (`docs/HANDOVER.md` section 13, "A green gate that never ran the tests").
- **Repo walking tests skip hidden folders and `build/`** (lesson 127). New tests that walk use the same
  `isSourceDir` rule as `LocaleDisciplineTest`.
- **Never ask the owner to test on a device, and never the watch** (`CLAUDE.md`). Everything below runs
  on the API 36 AVD `sajda` through the release script, with the owner running one command.

## 2. Unit tests

Module, class, and the cases. Concrete values come from `RAMADAN-LOCATION-SPEC.md` section 9 and
`PLAN.md` sections 6 and 7. All JUnit 4, backtick names, `org.junit.Assert`, no mocking library (none is
in the project and none is added).

### 2.1 Release A, `:core`

| Class | Cases |
|---|---|
| `HijriCalendarTest` | The ten cases in `PLAN.md` A1 |
| `RamadanGoldenTest` | For each row of the nine tables in `RAMADAN-LOCATION-SPEC.md` 9.2, `PrayerEngine.compute` at that place, date, method and madhab gives Fajr and Maghrib equal to the table to the minute, and the other four within 1 minute (Dhuhr may carry the method's own plus 1). The Umm al-Qura Isha minus Maghrib table by offset is asserted exactly. The Tromsø 2027 projected ranges are asserted by sweeping the year. The London daylight saving rows are asserted. Any failure is investigated as a mirror error first (section 9.2 says why) and the table is corrected from the engine with the reason in the commit |
| `PrayerEngineTest`, `AdjustmentTest`, `PolarAndHemisphereTest`, `DeterminismTest` | Unchanged, must stay green after A1 |

### 2.2 Release A, `:app`

| Class | Cases |
|---|---|
| `RamadanStateTest` | London 2027-02-22: fast 12h18m, change against 21 Feb plus 4 (both from the table and from two engine calls). Phases at 04:00, 12:00, 20:00 local. `tomorrowIsRamadan` on day 15 true, on 2027-03-08 false. Sydney 2027-02-22 change negative. London 2027-03-28 change 5 minutes, not 65 |
| `RamadanPdfRangeTest` | `rangeBounds(RAMADAN, 2027-02-22, offset 0)` is 2027-02-08 and 29, offset plus 1 is 2027-02-07 and 29, `fileName` is `PrayerTimes_Ramadan1448.pdf`, TODAY, NEXT_7_DAYS and THIS_MONTH unchanged |
| `SuhoorReminderPlanTest` | The nine rows in `PLAN.md` A5a |
| `RequestCodeTest` | Every day of 2027 and 2028 and every prayer slot: prayer codes in 10..3665, reminder codes in 10001..10366, widget code 20000, pairwise disjoint sets, three distinct action strings |
| `SuhoorReminderReceiverTest` | `shouldPostReminder`: off, calendar shifted away, five minutes late, six minutes late, normal |
| `WidgetStateTest` | No location. London 2027-02-22 at 04:00 (next Fajr, highlighted, not tomorrow, `countdownAllowed` mirrors the flag). 23:30 (next tomorrow's Fajr, `isTomorrow`, nothing highlighted). A minute by minute sweep of one day never highlights sunrise. 2027-02-07 23:00: `ramadanOnNextDay` true, `ramadanToday` false. 2027-03-08 21:00: `ramadanToday` true, `ramadanOnNextDay` false. Tromsø 2027-06-15: `today.approximated` and `nextIsApproximate` |
| `WidgetRefreshSchedulerTest` | `nextRefreshAt` picks the prayer when before midnight, midnight when next is tomorrow's Fajr, null without location, always after now |
| `WidgetColorsTest` | Each `widget_*` colour in `values/colors.xml` and `values-night/colors.xml` equals its `Color.kt` constant, and the four pairs clear 4.5:1 in both |
| `WidgetResourcesTest` | Both provider XML files: `updatePeriodMillis="0"`, layouts referenced, neither layout contains `android:text=` |
| `WidgetManifestTest` | Both providers and the refresh receiver declared, `exported` present, the `uses-permission` list equals the embedded baseline list |
| `SystemEventActionsTest` | `HANDLED_ACTIONS` and the manifest filter list the same six actions |
| `RamadanWordingTest` | The four phrases in the info note, no "imsak" as a provided time, no "you must" or "you may", widget descriptions mention dark mode |
| `AlertStyleDowngradeTest`, `AlertCodecTest`, `UsageCountsTest`, `DisclaimerContentTest`, `LanguagePickerTest`, `DndAccessTest`, `ColorContrastTest`, `PlaceNameTest`, `CityLookupParseTest` | Unchanged, must stay green |

### 2.3 Release B

| Class | Cases |
|---|---|
| `TrackerLogicTest` (`:core`) | The sixteen rows in `PLAN.md` B1, including the streak table reproduced here |
| `TrackerFileCodecTest` | Round trip, newer version refused, wrong kind refused, malformed refused, unknown slot dropped, excused with no ticks preserved, file name |
| `TrackerPrivacyTest` | `data_extraction_rules.xml` excludes `domain="file"` in both sections, `UsageCounts.kt` contains no "tracker" |
| `TrackerWordingTest` | No "missed", "sin", "reward", "lazy", "shame" in tracker strings, no question about a reason, the Settings subtitle says the data stays on this phone |
| `AppSettingsTrackerKeysTest` | `trackerEnabled` false and `trackerShowStreak` true on an empty preferences map (pure function `toAppSettings` extracted as `internal`) |

**Streak edge cases (the concrete table, so a reader does not have to open `PLAN.md`).** `all` means
all five ticked.

| Records (date: content) | Today | Streak |
|---|---|---|
| none | 20 Feb 2027 | 0 |
| 20: all | 20 Feb | 1 |
| 19: all, 20: Fajr | 20 Feb | 1 |
| 17, 18, 19: all | 20 Feb (no record) | 3 |
| 17: all, 18: excused, 19: all | 20 Feb | 2 |
| 18: all, 19: excused | 20 Feb | 1 |
| 18: excused, 19: excused | 20 Feb | 0 |
| 16: all, 17: Fajr, 18: all, 19: all | 20 Feb | 2 |
| 16: all, 18: all, 19: all | 20 Feb | 2 |
| 30 Dec 2026, 31 Dec 2026, 1 Jan 2027: all | 2 Jan 2027 | 3 |
| 28 Feb 2028, 29 Feb 2028, 1 Mar 2028: all | 2 Mar 2028 | 3 |
| 19: all, 20: excused | 20 Feb | 1 |
| 27 Mar 2027, 28 Mar 2027: all | 29 Mar 2027 | 2 |

**Request code collision proof (the concrete claim).** Prayer: `dayOfYear * 10 + ordinal`, minimum
`1 * 10 + 0 = 10`, maximum `366 * 10 + 5 = 3665`. Reminder: `10000 + dayOfYear`, minimum 10001, maximum
10366. Widget: 20000. The test enumerates, it does not reason.

## 3. Instrumented tests on the Mac emulator (API 33 or higher, run by the release script)

All in `app/src/androidTest/java/com/sajdatime/app/`, run with `./gradlew :app:connectedDebugAndroidTest`
on the AVD the script finds (`docs/release-tools/release-1.3.2.sh:92-115`). Each restores what it changes,
as `LanguageAndDndDeviceTest` does. The existing `LanguageAndDndDeviceTest` stays and must pass.

### 3.1 `WidgetDeviceTest` (Release A)

Needs, once per emulator boot, before Gradle: `adb shell appwidget grantbind --package com.sajdatime.app --user 0`.
The script adds that line before the connected test. Steps and assertions in `PLAN.md` A7. Also at font
scale 2.0: `adb shell settings put system font_scale 2.0` before, `1.0` after, and the assertion that the
Today widget's six rows all exist and the name views are not ellipsised at 1.0.

### 3.2 `RamadanDeviceTest` (Release A)

- Asserts on the device that `HijriCalendar.of(LocalDate.of(2027, 2, 8), 0)` is day 1, month 9, year
  1448, length 29, and that `LocalDate.of(2026, 2, 18)` is 1 Ramadan 1447, length 30 (the desugar table
  assumption, `RAMADAN-LOCATION-SPEC.md` section 3).
- Seeds London and asserts `RamadanState` for a fixed `now` of 2027-02-22T10:00Z matches
  `RamadanStateTest` (the ViewModel's `recalculate` extracted into a pure `internal` function taking
  `now`, so the device run and the JVM run share it).

### 3.3 The suhoor reminder in `dumpsys alarm` (release script step, not a JUnit test)

The emulator image allows `adb root` (`docs/HANDOVER.md` section 12). The script does, with the serial
exported as `ANDROID_SERIAL`:

```bash
"$ADB" root >/dev/null; sleep 2
"$ADB" shell dumpsys alarm | grep -c "PRAYER_ALARM" > "$OUT/prayer-alarms-before.txt"
"$ADB" shell "date 022123002027.00" >/dev/null                     # 21 Feb 2027 23:00 local
"$ADB" shell am start -W -n com.sajdatime.app/.MainActivity >/dev/null; sleep 4
# Turn the reminder on through the UI is not scriptable here, so the instrumented test SuhoorReminderDeviceTest
# sets suhoor_reminder_minutes=30 through SettingsRepository and calls PrayerAlarmScheduler.reschedule.
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.sajdatime.app.SuhoorReminderDeviceTest
"$ADB" shell dumpsys alarm > "$OUT/alarms-ramadan.txt"
grep -c "SUHOOR_REMINDER" "$OUT/alarms-ramadan.txt"                 # expect 1
grep -A4 "SUHOOR_REMINDER" "$OUT/alarms-ramadan.txt" | grep "origWhen"   # expect 2027-02-22 05:xx minus 30
grep -c "PRAYER_ALARM" "$OUT/alarms-ramadan.txt"                    # expect the same count as before
```

`SuhoorReminderDeviceTest` sets the reminder, reschedules, reads `dumpsys alarm` through
`UiAutomation.executeShellCommand`, asserts exactly one `SUHOOR_REMINDER` entry whose `origWhen` is 30
minutes before the Fajr the engine gives for 22 February at the seeded coordinates, asserts every
`PRAYER_ALARM` tag present before is present after with the same `origWhen`, then sets the reminder to
0, reschedules, and asserts zero `SUHOOR_REMINDER` entries. The clock is restored by the script killing
the emulator it started (`-no-snapshot-save`), or, when it reused a running one, by
`"$ADB" shell "settings put global auto_time 1"` and a reboot note in the summary.

### 3.4 Screens with the clock in Ramadan (release script step)

With the clock at 2027-02-22 10:00 the script captures, for each of `en`, `ur`, `tr`, `id` as it already
does for the language check: the Times screen (card), the timetable sheet opened by `uiautomator` tapping
"Full timetable" (text found through the dump, as the DND list check does), and the export sheet. A widget
cannot be placed from the shell, so the widget pictures come from `WidgetDeviceTest`, which draws each
rendered `FrameLayout` into a PNG under the app's cache directory, and the script pulls them with
`adb pull`. Then 2027-02-06 (eve notice) and 2027-03-08 20:00 (last day) for `en` only. Every PNG lands in
`build/release-1.4.0/` with the name of the state in the file name.

### 3.5 `TrackerDeviceTest` (Release B)

Enable the tracker, tick Fajr for today through `TrackerRepository`, read back, assert. Mark yesterday
excused, assert streak rules through `TrackerLogic` on the read back map. Save through
`TrackerFileCodec` to a `MediaStore` Downloads entry on API 29 and above, read it back, decode, assert
equality, delete the entry. Delete all, assert empty. Assert the DataStore file exists at
`files/datastore/sajdatime_tracker.preferences_pb`.

## 4. Regression gates and the "nothing slipped in" checks

Run in this order at the end of every task group and before every pull request.

1. **Unit tests, lint, bundles, forced:**
   `export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew --rerun-tasks :core:testDebugUnitTest :app:testDebugUnitTest :wear:testDebugUnitTest :app:lintDebug :wear:lintDebug :core:lintDebug :app:bundleRelease :wear:bundleRelease`
   Expected: BUILD SUCCESSFUL, the A0 baseline count plus the new tests, 0 failures, lint 0 errors
   (`MissingTranslation` is an error and will fail the build on any untranslated key, which is wanted).
2. **The full gate from `CLAUDE.md`:** `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`.
3. **Test count and freshness:** the python snippet in `PLAN.md` A0, and `stat -f "%Sm" */build/test-results/test*/*.xml`
   newer than the command start.
4. **Permissions:** the A0 bundletool dump of the new bundle, diffed against `build/plan-baseline/permissions.txt`:
   must be empty. Also `grep -r -E "AD_ID|ADSERVICES" app/build/intermediates/merged_manifests/release || echo clean`.
5. **Dependencies:** `./gradlew :app:dependencies --configuration releaseRuntimeClasspath | diff - build/plan-baseline/deps.txt`
   must be empty, and `git diff origin/main -- gradle/libs.versions.toml app/build.gradle.kts core/build.gradle.kts wear/build.gradle.kts`
   shows no new `implementation(` or `api(` line.
6. **Bundle size:** `ls -l app/build/outputs/bundle/release/app-release.aab` within 300 KB of A0 for
   Release A (two PNG previews under 30 KB each are the only binary additions) and 200 KB of B0 for B.
7. **Analytics:** `UsageCountsTest` green, and `grep -rn "logEvent\|sink.event\|FirebaseAnalytics" app/src/main --include=*.kt`
   finds them only in `UsageCounts.kt`.
8. **Locale discipline:** `LocaleDisciplineTest` green (no `Locale.getDefault()`), and
   `grep -rn 'android:text="' app/src/main/res/layout/widget_*.xml` finds nothing.
9. **Strings:** every new key in all four files:
   `for l in "" -ur -tr -in; do grep -c '<string name="\|<plurals name="' app/src/main/res/values$l/strings.xml; done`
   gives, for each translation, the English count minus 2 (the three untranslatable keys `feedback_email`,
   `privacy_policy_url` and `preview_build_notice` are absent and `app_language_tag` is present). That is
   the difference today (219 against 217) and it must stay 2 after every task.
10. **RTL layout:** `./gradlew installRtl` on the emulator, screenshot the Times screen with the card and
    both widgets, judge mirroring only (`CLAUDE.md`), uninstall `com.sajdatime.app.rtl` afterwards.
11. **Device suite:** section 3 through the release script.
12. **Store screenshots:** retaken as `PLAN.md` A10 item 11 says, `tools/build-store-assets.sh` run.
13. **Disclaimer copies:** `DisclaimerContentTest` green and `grep -c "follow your mosque" README.md docs/index.html docs/privacy.html docs/store/LISTING.md` all at least 1.
14. **Watch untouched:** `git diff origin/main --stat -- wear/` is empty.

## 5. Pre release checklist (per release, in order)

1. Status table in `README.md` of this folder: every task of the release `done` with a commit hash.
2. Section 4 gates 1 to 14 green on the branch head.
3. `versionCode` and `versionName` bumped, `:wear` untouched.
4. `docs/HANDOVER.md` updated as section 6 says, STATE OF PLAY first.
5. `docs/privacy.html` updated and, for B, the `#tracker` section present. Date line updated.
6. `docs/store/LISTING.md` and `LISTING-LOCALES.md` updated with the section C text, lengths measured.
7. `README.md`, `docs/index.html`, `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/RELEASING.md`
   updated.
8. `docs/release-tools/release-<version>.sh` written, header comment updated, `DEST` at the repo root.
9. Pull request opened with the reasoning, the verified and not verified lists, the screenshots attached.
10. The owner runs the script, every step `PASS`, bundle signed by his key, versionCode as expected.
11. The owner merges. Claude confirms `git rev-list --left-right --count origin/main...origin/<branch>` is `0 0` and that the privacy page is live with the new date (lesson 124).
12. The owner creates the release with Claude in Chrome (lesson 128), pastes the What's new, uploads the phone bundle only.
13. The owner submits. Claude re baselines the health check and records the live state when the raw page moves.

## 6. What to record, and where

**After every task** (in the same commit): the status table row in `README.md` of this folder, the
commit message with reasoning, and a `ponytail:` or explanatory comment at any line that looks wrong but
is right.

**After every release, `docs/HANDOVER.md`:**

| Section | What changes |
|---|---|
| Header | Version line: phone 1.4.0 (versionCode 8) or 1.5.0 (9), live date |
| 2 Tech stack | No new rows. State explicitly in "Deliberately NOT used": Glance (widgets are RemoteViews), `updatePeriodMillis`, waking widget alarms |
| 3 Module layout | File map: `core/HijriCalendar.kt`, `core/TrackerLogic.kt`, `app/widget/*`, `app/notify/SuhoorReminderPlan.kt`, `app/ui/home/RamadanCard.kt`, `app/ui/home/TrackerHistory.kt`, `app/data/TrackerRepository.kt`, `app/data/TrackerFileCodec.kt`. The `AppSettings` block with the new fields (it is the block "most likely to be trusted without checking", so update it) |
| 4 Features | Widgets, Ramadan pack, suhoor reminder, tracker, each in the house style |
| 5 Business rules | New 5.19 Ramadan (section 2 of the spec, the eve notice, the no country table decision with its sources), 5.20 widgets (refresh strategy, the chronometer digits lesson, dark mode follows the phone), 5.21 tracker (day boundary rule, streak rules, file format, never pruned) |
| 6 Notifications and alarms | The reminder's action, extras, request code band, id 3000, cancel window. The widget alarm's action and code and why it is non waking. The locale broadcast |
| 8 Privacy model | Row for the tracker. Statement that widgets read no location |
| 9 Testing | Rows for every new suite, the real count from the XML |
| 10 Verified on a device | The Ramadan clock runs, the widget host render, the `dumpsys alarm` before and after, the Diyanet comparison result (`RAMADAN-LOCATION-SPEC.md` 9.4) |
| 11 STATE OF PLAY | Rewritten: what is live, what is waiting, whose job. "Not done" item 8 removed. A11 closed with a pointer to 5.19. The design system paragraph about "no prayer tracker" updated. The daily health check baseline |
| 15 Lessons | Numbered, continuing from 129: at least the chronometer host locale lesson, the RemoteViews host locale lesson, the `exported` finding, the desugar Hijri table result, the Diyanet offset finding, the Play release notes limit as observed |

**Other files:** `docs/ARCHITECTURE.md` (file map, privacy model, the widget line at 571, the iOS note
that `HijriCalendar` ports as is), `docs/DESIGN_SYSTEM.md` (widget tokens, Ramadan card, tick component),
`docs/RELEASING.md` ("Before every future update" version line, and the widget bind grant in the device
steps), `README.md` and `docs/index.html` (section C6), `docs/store/*` (section C), `docs/privacy.html`
(section C5). `CLAUDE.md` changes only if a convention changes: add one sentence under "Before you say you
are finished" that the device suite needs `appwidget grantbind` first, because the release script is
where that lives and a future session will otherwise see the widget test fail and not know why.
`AGENTS.md` line 12 corrected (audit G1).

**The plan folder:** when a release is live, the status table is final and a one line note at the top
of `README.md` of this folder says "Release A live <date>, versionCode 8". The folder is history after
Release B and `docs/HANDOVER.md` is the source of truth, as `CLAUDE.md` requires.

## 7. Who does what, in dependency order

| # | Step | Who |
|---|---|---|
| 1 | Read the standing documents and this folder, capture baselines (A0) | Claude |
| 2 | Tasks A1 to A9 with their verifications, on branch `claude/1-4-0-ramadan-widgets` | Claude |
| 3 | Documents, listing text, privacy, version bump (A10), pull request opened, commits handed over as a git bundle if the session is in the cloud (lesson 129) | Claude |
| 4 | Run `docs/release-tools/release-1.4.0.sh` on the Mac (one command), paste back the summary | Owner |
| 5 | Read the summary, fix anything `FAIL`, repeat 4 | Claude, then owner |
| 6 | Review and merge the pull request on GitHub | Owner |
| 7 | Confirm the merge and the live privacy page, prepare the Claude in Chrome prompt with the exact What's new text | Claude |
| 8 | Play Console: paste the listing text in four languages, create the Production release, upload the phone bundle, submit for review. One screen at a time with screenshots, as `CLAUDE.md` requires | Owner, walked through by Claude |
| 9 | Confirm live on the raw public page, re baseline the health check, record STATE OF PLAY | Claude |
| 10 | Decide whether Release B starts now | Owner |
| 11 | B0 to B7 on `claude/1-5-0-tracker`, pull request | Claude |
| 12 | Run `release-1.5.0.sh`, paste back | Owner |
| 13 | Merge. The privacy page with `#tracker` is then live | Owner |
| 14 | Confirm the live page shows the tracker section before anything is submitted (lesson 124) | Claude |
| 15 | Play Console: listing text, release, submit | Owner, walked through by Claude |
| 16 | Confirm live, re baseline, record | Claude |

Decisions only the owner makes, each with the default in `README.md`: the release split, the no country
table rule, and the Turkey method question. Everything else proceeds on the defaults.

## 8. Post release monitoring, Play Console only

No analytics event is added, so adoption is not measured. What is watched, where, and when:

| What | Where in Play Console | When |
|---|---|---|
| Crash and ANR rate for the new version against the previous one | Quality, Android vitals, Crashes and ANRs, filter by version | Day 1, day 3, day 7, then weekly |
| Any crash in `com.sajdatime.app.widget`, `RamadanCard`, `SuhoorReminderPlan`, `PrayerAlarmReceiver`, `TrackerRepository` | Same page, by stack trace | Same |
| Reviews mentioning widget, Ramadan, suhoor, sehri, iftar, imsak, reminder, tracker, streak, alarm, adhan, missed | Ratings and reviews | Daily during Ramadan 1448 (8 February to 8 March 2027) and the week after, weekly otherwise |
| Installs against uninstalls | Statistics, Users, Installed audience | Weekly |
| "Updated on" date and the What's new text on the public page | Raw public page, `curl -sL -A "Mozilla/5.0" ...` as `CLAUDE.md` shows | Once at go live, then by the daily health check |
| The daily health check (trigger `trig_0129FQnWihf8XG6rKLCB4oFr`) | Its own report | Re baselined to the new version on go live |

A review reporting a wrong iftar time is treated as a possible R4 (`PLAN.md` section 8): ask which
country and method, compare with the engine, and never adjust a time without the owner.
