# PLAN: widgets, Ramadan pack and prayer tracker

Master implementation plan. Written 16 October 2026 against `main` at `13b581d`. Companion files:
`README.md` (index and status), `RAMADAN-LOCATION-SPEC.md` (the Ramadan design and test vectors),
`WORDING-AND-LISTING.md` (every string and the store text), `TEST-AND-RECORD.md` (gates and recording),
`SESSION-PROMPTS.md` (the prompts).

## 0. How to read this plan

- Tasks are ordered. Each is one commit, or two if the task says so. Each ends with a **Verify** block: the
  exact command and the expected result. A task is not done until that block passes.
- Paths are repo relative. `app/...` means `app/src/main/java/com/sajdatime/app/...` unless the path is
  given in full. `core/...` means `core/src/main/java/com/sajdatime/core/...`.
- Signatures are the contract. Names of new classes, functions, keys, actions, resource ids and strings are
  fixed here so the five documents agree with each other. Change a name only with a reason written in the
  commit, and change it in `WORDING-AND-LISTING.md` and `TEST-AND-RECORD.md` in the same commit.
- Pseudo code is Kotlin shaped and deliberately not compiled. Where a real snippet is given (manifest,
  XML, strings) it is meant to be pasted.
- "Hook" means an existing call site that gains one line. The four existing reschedule sites are listed
  in section 1 and nothing else calls the scheduler.
- Every string key named here has its English text, its content description and its three drafts in
  `WORDING-AND-LISTING.md` section B. Do not write a user facing word in Kotlin.
- Facts: **verified** means read in the code at the cited line. **assumed** means it was not possible to
  check without a build or a device and the task says how to check it.
- The commit rule in `CLAUDE.md` applies to every task: say what was wrong, why this fix, what was
  verified and what was not, and update `docs/HANDOVER.md` as `TEST-AND-RECORD.md` section 6 says.

## 1. Facts the plan depends on (verified in the code, commit 13b581d)

| # | Fact | Where |
|---|---|---|
| F1 | `PrayerEngine.compute(coordinates, date, prefs)` returns `DayPrayerTimes(date, times, approximatedFrom)` with all six slots, projected from 45 or 60 degrees when the sun will not rise or set, never null | `core/PrayerEngine.kt:220-263`, `core/PrayerModels.kt:97-131` |
| F2 | `PrayerEngine.nextPrayer(coordinates, prefs, now, zone)` scans yesterday, today, tomorrow and returns `NextPrayer(slot, at, isTomorrow)`, never sunrise | `core/PrayerEngine.kt:314-336`, `core/PrayerModels.kt:184-189` |
| F3 | `PrayerEngine.currentPrayer` returns the last slot at or before now, null between sunrise and Dhuhr | `core/PrayerEngine.kt:361-373` |
| F4 | `PrayerEngine.computeRange(coordinates, start, days, prefs)` exists and is what the PDF uses | `core/PrayerEngine.kt:376-382` |
| F5 | Ramadan is detected privately as Hijri month 9 of `HijrahDate.from(date.plusDays(offset))`, wrapped in `runCatching`, used only by the Umm al-Qura Isha rule | `core/PrayerEngine.kt:413-436` |
| F6 | The Home header computes the Hijri date a second time, privately, the same way, with month names from `R.array.hijri_months` and the month name bidi isolated | `app/ui/home/HomeScreen.kt:894-927`, `app/src/main/res/values/strings.xml:410-423` |
| F7 | `CalculationPrefs.hijriOffset` clamps to plus or minus 2 and the repository clamps on write | `core/PrayerModels.kt:173-180`, `app/data/SettingsRepository.kt:274-279` |
| F8 | `AppSettings` fields and `calculationPrefs`, the keys in `SettingsRepository.Keys`, enums read defensively with `enumOr` | `app/data/SettingsRepository.kt:51-163, 320-348, 389-391` |
| F9 | The stored place is `cityName: String` (a label such as "Slough, United Kingdom" or "Makkah") and `coordinates`. No country code, no time zone, no address fields are stored | `app/data/SettingsRepository.kt:246-251, 303-308`, `app/data/PlaceName.kt:39-76` |
| F10 | The alarm scheduler: `reschedule` cancels then lays down one `PendingIntent` per (prayer, calendar day) for yesterday, today and tomorrow, filtered to prayers in `alertFor`, through the three rung ladder `setAlarmClock`, `setExactAndAllowWhileIdle`, `setAndAllowWhileIdle`. Request code is `date.dayOfYear * 10 + slot.ordinal`. Action `com.sajdatime.app.action.PRAYER_ALARM`, extras `slot` and `at` | `app/notify/PrayerAlarmScheduler.kt:32-56, 59-75, 154-184, 214-268` |
| F11 | `cancelAll` walks day offsets minus 1 to plus 2 and the five prayer slots with `FLAG_NO_CREATE`, cancelling both the alarm and the `PendingIntent` | `app/notify/PrayerAlarmScheduler.kt:214-235` |
| F12 | `PrayerAlarmReceiver` ignores any action other than `ACTION_PRAYER`, posts the alert, then calls `reschedule` and `OngoingBadge.refresh` under `goAsync` | `app/notify/PrayerAlarmReceiver.kt:19-54` |
| F13 | The four reschedule call sites are `SajdaViewModel.recalculate`, `PrayerAlarmReceiver.onReceive`, `DailyRescheduleWorker.doWork`, `SystemEventReceiver.onReceive` | `app/ui/SajdaViewModel.kt:397-402`, `app/notify/PrayerAlarmReceiver.kt:48`, `app/notify/DailyRescheduleWorker.kt:99`, `app/notify/SystemEventReceiver.kt:147` |
| F14 | `DailyRescheduleWorker` is a 12 hour periodic job, `ExistingPeriodicWorkPolicy.KEEP`, no constraints, no flex window, enqueued from the ViewModel init and from `SystemEventReceiver`. It therefore runs at an arbitrary wall clock time and cannot be relied on for a midnight refresh | `app/notify/DailyRescheduleWorker.kt:96-119`, `app/ui/SajdaViewModel.kt:135`, `app/notify/SystemEventReceiver.kt:149` |
| F15 | `SystemEventReceiver` handles `BOOT_COMPLETED`, `TIMEZONE_CHANGED`, `TIME_SET`, `MY_PACKAGE_REPLACED`, `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` | `app/notify/SystemEventReceiver.kt:174-180`, `app/src/main/AndroidManifest.xml:117-130` |
| F16 | Notification ids: ongoing badge 1000, prayer alerts 2000 plus slot ordinal. Channels `prayer_times`, `next_prayer_badge`, `prayer_alarm_v<hash>[_dnd]`. `postPrayerAlert` wraps the context with `AppLocale.wrap`, checks `canPost`, and applies the two downgrades through `effectiveAlertStyle` | `app/notify/Notifications.kt:39-49, 98-99, 213-276, 368-378` |
| F17 | `TimeFormat.clock` honours the device 12 or 24 hour setting through `applicationContext` and formats in `AppLocale`. `TimeFormat.date` gives "Thu 31 Jul". `TimeFormat.remaining` gives "2h 14m" | `app/notify/TimeFormat.kt:39-82` |
| F18 | `AppLocale.of(context)` reads `app_language_tag` and `AppLocale.wrap(context)` pins resources, number formatting and layout direction. `LocaleDisciplineTest` fails the build on any `Locale.getDefault()` in `*/src/main` and walks every `res/values*/strings.xml` | `core/AppLocale.kt:104-143`, `core/src/test/.../LocaleDisciplineTest.kt:163-214`, `core/build.gradle.kts:44-50` |
| F19 | The language picker applies a per app locale with `LocaleManager` on API 33 and above, nothing below | `app/ui/settings/AppLanguage.kt:78-83` |
| F20 | `SajdaViewModel.startClock` ticks once a second and calls `recalculate` when the next prayer passes, when the calendar date changes, or when sunrise passes while Fajr is current. `recalculate` recomputes today, next, current, then reschedules alarms, refreshes the badge and publishes to the watch | `app/ui/SajdaViewModel.kt:140-168, 369-403` |
| F21 | `UiState` holds `settings`, `today`, `next`, `current`, `now`, `exportEvent` and the compass fields | `app/ui/SajdaViewModel.kt:68-89` |
| F22 | The Times screen order: location header, next prayer card, three rare banners, the Today list with "Save as PDF" on its heading line, the mosque door. `NoticeCard(title, body, onClick?, onDismiss?)` is the amber notice component. `PrayerRow(slot, time, isNext, isCurrent)` draws one row, sunrise dimmed | `app/ui/home/HomeScreen.kt:93-150, 627-682, 685-732, 754-846` |
| F23 | `ExportSheet` lists `PrayerPdfExporter.Range.entries` and `ExportOption` maps each to two strings with an exhaustive `when` | `app/ui/home/HomeScreen.kt:485-545` |
| F24 | `PrayerPdfExporter.export(range, coordinates, cityName, prefs, today)` computes `computeRange`, names the file with `Locale.US`, draws a masthead, an optional approximate note, a header row and data rows. Columns are Day 72, Date 91, six slots at 60, total 523 on a 595 point page with 36 point margins | `app/pdf/PrayerPdfExporter.kt:47-110, 123-172, 231-285, 344-350, 367-372` |
| F25 | Settings is four groups of rows: `SettingRow(title, subtitle, icon, onClick)` and `SwitchRow(title, subtitle, checked, onCheckedChange, icon)`. Choosers open from `SettingsChooser`, which the Times screen can request by name. The Hijri offset lives in `AdjustmentsDialog` behind `SettingsChooser.ADJUSTMENTS` | `app/ui/settings/SettingsScreen.kt:105, 188-310, 335-342, 853-903, 906-946, 989-1053` |
| F26 | `MainActivity` registers `ActivityResultContracts` launchers for permissions and the ringtone picker, shows toasts for export events, and shares a PDF with `ClipData` and `FLAG_GRANT_READ_URI_PERMISSION` | `app/MainActivity.kt:65-100, 115-124, 262-271` |
| F27 | `MainScaffold` holds `destination` and `settingsRequest` as `rememberSaveable` and passes `openSetting` to Home. There is no navigation library | `app/ui/MainScaffold.kt:52-60, 90-110, 160-200` |
| F28 | `data_extraction_rules.xml` excludes every domain, including `file`, from cloud backup and device transfer. DataStore files live under `files/datastore/` which is the `file` domain | `app/src/main/res/xml/data_extraction_rules.xml:16-31`, `app/data/SettingsRepository.kt:24` |
| F29 | Permissions declared: coarse location, internet, post notifications, boot completed, schedule exact alarm, access notification policy. Three advertising permissions removed with `tools:node="remove"`. `UsageCountsTest` reads the manifest off disk and `app/build.gradle.kts` declares the files such tests read as task inputs | `app/src/main/AndroidManifest.xml:10-55`, `app/build.gradle.kts:184-233` |
| F30 | The closed analytics event set is three events with fixed parameters and `UsageCountsTest` fails on any other | `app/data/UsageCounts.kt:23-98`, `app/src/test/.../UsageCountsTest.kt:127-150` |
| F31 | Colours in XML: only `window_background` in `values/colors.xml` and `values-night/colors.xml`. Every other colour is a Kotlin constant in `app/ui/theme/Color.kt` and `ColorContrastTest` asserts WCAG AA on the pairs it lists | `app/src/main/res/values/colors.xml`, `app/src/main/res/values-night/colors.xml`, `app/ui/theme/Color.kt`, `app/src/test/.../ColorContrastTest.kt` |
| F32 | Translations exist for every string in `values-ur`, `values-tr`, `values-in` (217 of 219 app strings, the three missing are untranslatable), each declaring `app_language_tag` with `tools:ignore="ExtraTranslation"`, and core strings are translated too. Wear strings are English only | `app/src/main/res/values-*/strings.xml`, `core/src/main/res/values-*/strings.xml`, `wear/src/main/res/values/strings.xml` |
| F33 | Dependencies: adhan 1.2.1 as `api` in `:core`, DataStore preferences, WorkManager, Compose BOM, material icons extended, play services wearable, Firebase analytics, desugar. `org.json` is on the app unit test classpath only (the platform provides it at runtime) | `gradle/libs.versions.toml`, `app/build.gradle.kts:235-273`, `core/build.gradle.kts:52-57` |
| F34 | Build types: `debug`, `release` (minified, shrunk), `rtl` (Urdu tag, `.rtl` suffix), `sideload` (`.sideload` suffix). `analytics_allowed` is false in every non release build | `app/build.gradle.kts:71-129`, `app/src/*/res/values/bools.xml` |
| F35 | Existing unit tests: core 9 files, app 9 files, wear 2 files, JUnit 4, backtick names, plain `org.junit.Assert`. One instrumented test, `LanguageAndDndDeviceTest`, run by `docs/release-tools/release-1.3.2.sh` step 6 on an API 33 or higher AVD | the `src/test` and `src/androidTest` trees, `docs/release-tools/release-1.3.2.sh:92-138` |
| F36 | Version 7 / 1.3.2 in `app/build.gradle.kts:49-50`. Watch 1001 / 1.2.0 untouched | `app/build.gradle.kts:45-50` |

## 2. Scheduler impact analysis

The alarm scheduler is the component whose failure is worst for a prayer app: a missed adhan. Two of the
three features touch it. This section is the whole argument that they cannot regress it.

### 2.1 What the widgets do to the scheduler: nothing

The widgets never call `PrayerAlarmScheduler.schedule`, never use `ACTION_PRAYER`, never use the
`setAlarmClock` ladder and never touch `cancelAll`. They add one **separate** alarm with its own action and
request code (section 4) through `AlarmManager.setExact(RTC, ...)` when exact alarms are permitted and
`AlarmManager.set(RTC, ...)` otherwise, both **non waking**. A non waking alarm cannot wake the device, so
it cannot cost battery while the phone sleeps, and its lateness cannot matter because nobody is looking at
a widget on a sleeping phone. The only shared code is `PrayerEngine` and `AppSettings`, both read only.

### 2.2 What the suhoor reminder does to the scheduler

**Request code scheme (verified, F10).** Prayer alarms use `dayOfYear * 10 + slot.ordinal`, so with
`dayOfYear` in 1 to 366 and `ordinal` in 0 to 5 the set is a subset of [10, 3665]. The reminder uses
`SUHOOR_REQUEST_BASE + dayOfYear` with `SUHOOR_REQUEST_BASE = 10_000`, so its set is [10_001, 10_366]. The
widget refresh alarm uses the single code `WidgetRefreshScheduler.REQUEST_CODE = 20_000`. The three sets are pairwise
disjoint by construction, and `RequestCodeTest` (A5) sweeps every day of a leap year and every slot to
prove it rather than assert it by reasoning.

**Collision proof is belt and braces.** A `PendingIntent` is identified by its request code **and** by
`Intent.filterEquals` (action, data, type, class, categories, never extras). The reminder's intent carries
a different action, `com.sajdatime.app.action.SUHOOR_REMINDER`, so even an equal request code could not
replace a prayer alarm. Both halves are kept so a future change to one cannot silently create a collision.
This is also why the owner's draft idea, "a new extra on the existing action", is rejected: extras are not
part of the identity, so a reminder with the same action and a colliding code would **replace** the Fajr
alarm it was meant to precede.

**Rung behaviour.** The reminder goes through the same private `schedule(context, manager, pendingIntent,
at)` helper as a prayer (refactored in A5 so the ladder exists once): `setAlarmClock` first, then
`setExactAndAllowWhileIdle`, then `setAndAllowWhileIdle`, each in `runCatching`. Nothing about the rungs
changes for prayers. The reminder benefits from the same standby bucket exemption as a prayer alarm,
because `setAlarmClock` sets `a.alarmClock != null` and AOSP's `isExemptFromAppStandby` returns true on
exactly that (`docs/reviews/2026-08-03-standby-bucket-exemption.md` section 2). A reminder that falls to
rung 3 on HyperOS may be parked for days, the same as a prayer alert would be, and the user facing wording
for exact alarms already says so.

**Cancel and horizon.** `cancelAll` (F11) gains a second loop over the same minus 1 to plus 2 day window for
the reminder's codes and action with `FLAG_NO_CREATE`. Reminders are only ever laid down for the same
yesterday, today and tomorrow window as prayers (F10), so every reminder that can exist is inside the
cancel window, and switching the reminder off, or Ramadan ending, leaves nothing behind after the next
reschedule. A reminder for a date that is not Ramadan on the user's shifted calendar is never created.

**Boot, time change and permission change recovery.** Unchanged. All five existing triggers (F13, F15) call
`reschedule`, which now also lays down reminders. No new receiver action is added for the reminder.

**What fires.** `PrayerAlarmReceiver` gains a second branch keyed on the action: `ACTION_SUHOOR_REMINDER`
posts the reminder notification (id 3000, section 6 task A5) and then calls `reschedule` and the badge
refresh exactly as the prayer branch does, so a fired reminder chains the next window too.

**Order of alerts in the same minute.** A reminder is at `Fajr minus N minutes` with N in 15, 30, 45, 60,
so it can never coincide with Fajr. Two alarms in the same minute are not possible for one user.

**Proof that prayers are untouched.** `AlertStyleDowngradeTest`, `AlertCodecTest` and the core engine tests
are not modified in A5. The new `SuhoorReminderPlanTest` and `RequestCodeTest` cover the reminder. The
device check in `TEST-AND-RECORD.md` section 3 reads `dumpsys alarm` and asserts that every
`PRAYER_ALARM` tag present before the change is present after it, with the same `when`.

### 2.3 Standby bucket, Doze and the widget alarm

The widget alarm is not an alarm clock alarm, so it is **not** exempt from standby bucket deferral or from
Doze. That is acceptable and deliberate: it is a display refresh, not an alert. If it is deferred, the widget
shows the correct clock times for the whole day (they were computed for the calendar day, not for the
moment) and only the highlight and the countdown lag until the next trigger. Section 4 lists every trigger
and the failure modes.

## 3. DataStore design

### 3.1 Release A additions to the existing `sajdatime` preferences store

Two new keys in `SettingsRepository.Keys`, read defensively like every other key (F8):

| Key | Type | Default | Field on `AppSettings` | Setter |
|---|---|---|---|---|
| `suhoor_reminder_minutes` | Int | 0 (off) | `suhoorReminderMinutes: Int = 0`, clamped to the set {0, 15, 30, 45, 60} on read | `setSuhoorReminderMinutes(minutes: Int)` |
| `suhoor_reminder_style` | String | `NOTIFICATION` | `suhoorReminderStyle: AlertStyle = AlertStyle.NOTIFICATION` via `enumOr` | `setSuhoorReminderStyle(style: AlertStyle)` |
| `ramadan_notice_dismissed_year` | Int | 0 | `ramadanNoticeDismissedYear: Int = 0` (the Hijri year whose eve notice was dismissed, so the notice returns next year without a reset) | `dismissRamadanNotice(hijriYear: Int)` |

No key for the widgets. Widgets hold no state of their own and read `AppSettings` on every refresh.
`WatchSyncContract` is not extended: none of these reaches the watch (same reasoning as
`alarmOnApproximateDays`, `docs/HANDOVER.md` 5.14).

### 3.2 Release B: a second preferences store for the tracker

A second `DataStore<Preferences>` named `sajdatime_tracker`, file `files/datastore/sajdatime_tracker.preferences_pb`,
owned by `TrackerRepository`. Separate from settings so that a settings write never rewrites the tracker
file and so the tracker can be deleted as one file. It sits in the `file` domain, which
`data_extraction_rules.xml` already excludes from backup and transfer (F28), so no manifest or XML change is
needed. The pull request for B states this and `TrackerPrivacyTest` (B2) reads the XML off disk to assert
the `file` exclusions are still there.

Keys:

| Key | Type | Meaning |
|---|---|---|
| `tracker_version` | Int | Format version of this store, 1. Read before anything else. A larger value means a newer app wrote it, the repository then treats the store as read only and the UI shows `tracker_newer_version` |
| `d:<ISO date>` for example `d:2027-02-08` | Int | One day record as a bitmask. Bits 0 to 4 are Fajr, Dhuhr, Asr, Maghrib, Isha in `PrayerSlot` order skipping sunrise (so bit index is `PrayerSlot.entries.filter { it.isPrayer }.indexOf(slot)`). Bit 5 is excused. Values above 63 are masked on read |

Two switches live in the **settings** store, not here, because the Times screen already observes settings:
`tracker_enabled` (Boolean, default false) and `tracker_show_streak` (Boolean, default true). Turning the
tracker off keeps the tracker store intact. `TrackerRepository.deleteAll()` clears the store and is the only
destructive operation, behind one confirmation.

Never pruned. One Int per day is about 20 bytes in the proto map, so ten years is under 100 KB.

The day key is a `LocalDate`, never an instant, so time zone and daylight saving changes cannot move a
record. Which `LocalDate` a tick belongs to is decided by the screen that offers the tick: the Times screen
ticks `state.today.date` (F21), the history sheet ticks the date of the row. See B4 for the post midnight
Isha rule.

## 4. Widget refresh strategy and its failure modes

### 4.1 Principles

1. **The widget never computes anything a screen does not.** `WidgetState.build` calls
   `PrayerEngine.compute` for today and `PrayerEngine.nextPrayer` (F1, F2) with the same `AppSettings`
   the Times screen uses. The highlight rule is `slot == next.slot && !next.isTomorrow`, copied from
   `TodayTimeline` (F22). Sunrise is dimmed and never highlighted.
2. **Clock times are right all day even if every refresh is missed**, because they are computed for the
   calendar day. Only two things can go stale: which row is highlighted, and the countdown.
3. **No periodic polling.** `updatePeriodMillis="0"`. The system's own periodic update uses a waking alarm
   (`AppWidgetServiceImpl` schedules `ELAPSED_REALTIME_WAKEUP`) which this app refuses to pay for a display.
4. **One function refreshes everything:** `WidgetUpdater.refreshAll(context, settings)`. If no widget
   instance exists it returns at once and cancels the widget alarm, so users without a widget pay nothing.

### 4.2 Triggers (all of them)

| Trigger | Where it is hooked | Why |
|---|---|---|
| App opened, any settings change, prayer passed, day changed, sunrise passed | `SajdaViewModel.recalculate` after `OngoingBadge.refresh` (F20) | Covers location, method, madhab, adjustments, Hijri offset, theme, language picker (the picker recreates the activity but the ViewModel survives, so A7 also hooks `AppLanguage.apply`) |
| A prayer alarm fired or a reminder fired | `PrayerAlarmReceiver.onReceive` after `OngoingBadge.refresh` (F12) | The moment the highlight must move, when the user has alerts on |
| The 12 hour job | `DailyRescheduleWorker.doWork` (F14) | Safety net |
| Boot, time zone change, time set, package replaced, exact alarm permission change | `SystemEventReceiver.onReceive` (F15) | Alarms are gone after boot, the chronometer base is wrong after a time change, and after an update the layout may have changed |
| App language changed from the picker or from system settings | New manifest action `android.intent.action.LOCALE_CHANGED` on `SystemEventReceiver`, plus one line in `AppLanguage.apply` | Every widget string must be in the app's language. `LOCALE_CHANGED` is on Android's implicit broadcast exceptions list so a manifest receiver gets it |
| The widget alarm | New receiver `WidgetRefreshReceiver`, action `com.sajdatime.app.action.WIDGET_REFRESH` | Fires at the next prayer time or local midnight, whichever is sooner, so the highlight and the countdown roll without the app |
| System widget lifecycle | `onUpdate`, `onEnabled`, `onAppWidgetOptionsChanged` on both providers | First placement, resize, launcher restart |

Each refresh ends by calling `WidgetRefreshScheduler.scheduleNext(context, state)` which sets **one** alarm
(request code 20,000, `FLAG_UPDATE_CURRENT`, so there is never more than one) at
`min(next.at, nextLocalMidnight)` plus 2 seconds (so the engine sees the new day and the passed prayer). If
`PrayerAlarmScheduler.canScheduleExact` is true it uses `setExact(AlarmManager.RTC, ...)`, otherwise
`set(AlarmManager.RTC, ...)` (inexact, never throws, no permission). Both are non waking.

### 4.3 The countdown

A `Chronometer` in countdown mode (`setChronometerCountDown(id, true)` and `setChronometer(id, base,
null, true)` with `base = SystemClock.elapsedRealtime() + (next.at.toEpochMilli() - now.toEpochMilli())`).
The launcher process ticks it, so there are no per minute wake ups from this app. It is shown only when
`canScheduleExact` is true, because then the refresh alarm is exact and the chronometer cannot be left
counting negative for long while the screen is on. Without the permission the widget shows the clock time
and no countdown, and the "Allow exact alarms" card in the app already explains the permission.

Known and accepted: the chronometer's digits are formatted by the launcher in the **phone's** language, not
the app's, because `Chronometer` formats with the host process locale and nothing in `RemoteViews` can
change that. On an Arabic phone running the app in English the countdown digits would be Arabic Indic
while every other digit on the widget is Latin. Everything that is a string (clock times, labels, the
"Tomorrow" marker, the content description) is formatted in the app's language. Recorded as a lesson in
A6, not hidden.

### 4.4 Failure modes and what the user sees

| Failure | Effect | Why it is harmless or what limits it |
|---|---|---|
| Widget alarm deferred by Doze or standby bucket | Highlight and countdown lag until the next trigger | Clock times stay right. Any app open, any prayer alert, the 12 hour job, boot, or the device waking (a non waking alarm is delivered the next time the device wakes, which includes the screen being turned on) refreshes. HyperOS may park the inexact form for days (`docs/HANDOVER.md` section 6) but only for users without the exact alarm permission, and the chronometer is hidden for exactly those users |
| Launcher restarts or is replaced | The launcher re requests updates and `onUpdate` redraws from DataStore | Widgets are stateless |
| No location yet (first run, or settings cleared) | Widget shows one line `widget_no_location` and a tap opens the app | `WidgetState.hasLocation = false` branch |
| Projected polar day | Widget shows the same `notif_approximate` word as the notification does, under the times | `DayPrayerTimes.approximated` (F1) |
| App language changed | `LOCALE_CHANGED` and `AppLanguage.apply` both refresh. If a launcher caches the old `RemoteViews` until its next update, the next trigger fixes it | Belt and braces |
| Time set backwards | `TIME_SET` refresh recomputes the chronometer base | Existing receiver |
| Process death between alarm and refresh | `WidgetRefreshReceiver` runs on a fresh process and reads DataStore with `SettingsRepository(context).current()` under `goAsync`, same as `PrayerAlarmReceiver` | Same pattern as the existing receivers |
| RemoteViews method not remotable on some OEM launcher | Launcher shows "Problem loading widget" | The widget device test renders both layouts through a real `AppWidgetHost`, and the two risky calls (`setLayoutDirection`, `setChronometerCountDown`) are documented platform `@RemotableViewMethod` entries. If a user report arrives, the fallback is to drop the `setLayoutDirection` call and accept LTR layout for Urdu widgets |
| Widget placed, app later uninstalled and reinstalled | Android removes widgets on uninstall | Nothing to do |
| Many widget instances | `refreshAll` builds the `RemoteViews` once per provider and pushes to every id | One engine call per refresh, not per instance |

## 5. Cross cutting requirements

**Accessibility.** Every widget root has one content description built from `widget_a11y_next` (for example
"Next prayer Asr at 15:42, in 1 hour 20 minutes") set with `setContentDescription`, and the child views are
`importantForAccessibility="no"` so TalkBack reads one sentence. The Ramadan card's countdown copies the
Times screen pattern: digits `clearAndSetSemantics` with a spoken summary from `rememberRemainingText`
(F22, `app/ui/components/Common.kt:87`). Tick targets in B are 48dp minimum with a content description of
prayer name plus state. Nothing new relies on colour alone: the next row keeps the "Next" pill, Ramadan
labels are words, tracker states are an icon plus a description.

**RTL and Urdu digits.** In Compose nothing changes: `AppLocale.wrap` in `MainActivity` already pins layout
direction and digits. In `RemoteViews`, the launcher inflates the layout with the **launcher's**
configuration, so: every text is set programmatically from a context wrapped with `AppLocale.wrap`
(never `android:text="@string/..."` in the widget XML, which would resolve in the host), the root gets
`setInt(R.id.widget_root, "setLayoutDirection", LAYOUT_DIRECTION_RTL or LTR)` from the app locale, and
every `TextView` carries `android:textAlignment="viewStart"`. Clock times go through `TimeFormat.clock`
and so carry Urdu's digits when the app is in Urdu. The `rtl` build type (`./gradlew installRtl`) is the
layout check for the widgets too, judged as a layout check and never shown to the owner as a finding
(`CLAUDE.md`).

**Font scaling.** Widget text uses `sp`, `maxLines="1"`, `ellipsize="end"`, and the Today widget's rows
are `wrap_content` in a vertical `LinearLayout` so at 200 percent (Android 14 and later) the rows grow and
the last one clips rather than overlapping. The device test renders at font scale 1.0 and 2.0 and asserts
no `TextView` is ellipsised at 1.0. Compose screens use `heightIn(min)` as the existing rows do.

**Dark mode.** Widgets follow the **phone's** dark mode through `values-night/colors.xml`, not the app's
Theme row, because `RemoteViews` resolve resources in the launcher under the system UI mode and
`ThemeChoice` cannot reach them. Said plainly in the widget description string. Colour resources added in
A6 mirror `Color.kt` values and `WidgetColorsTest` asserts they are byte equal to the Kotlin constants and
pass the same WCAG pairs as `ColorContrastTest`. Compose additions use Material roles only (`CLAUDE.md`).

**Process death.** Widgets and receivers rebuild everything from DataStore. The Ramadan card and the
timetable sheet derive from `UiState` and `rememberSaveable` booleans as the existing sheets do. The tracker
history sheet keeps its open state in `rememberSaveable`. Nothing is held in a static.

**Locale change.** Covered in 4.2. For Compose, a per app locale change recreates the activity and the
strings re resolve. The suhoor reminder notification and the widget strings are built from
`AppLocale.wrap(context)` at post time, as `Notifications.postPrayerAlert` already does (F16).

**Work profiles.** A widget host in a work profile can only host the work profile's copy of the app, which
has its own DataStore and its own alarms. Nothing special is needed. `runCatching` around every
`AlarmManager` call stays (F10), because work profiles can revoke capabilities between check and call.

**Android 12 to 16 differences that matter here.**

| Version | What changes | Where the plan handles it |
|---|---|---|
| 12 (API 31) | `SCHEDULE_EXACT_ALARM` exists and gates `setExact`, `setAlarmClock`. Receivers with intent filters must declare `android:exported`. `PendingIntent` must be `FLAG_IMMUTABLE` or mutable explicitly. Widget `targetCellWidth`, `targetCellHeight`, `previewLayout`, `maxResizeWidth`, system corner radius dimen `@android:dimen/system_app_widget_background_radius` | A6 manifest and XML, A7 alarm choice, `canScheduleExact` already handles below 31 as always true (F10) |
| 13 (API 33) | `POST_NOTIFICATIONS` runtime permission. Per app language and `LOCALE_CHANGED` to the app. Exact alarm permission denied by default (actually from 13 for new installs, 14 for all) | Reminder checks `canPost` (F16). `LOCALE_CHANGED` hook in A7. The language picker exists only on 33 and above (F19) |
| 14 (API 34) | Exact alarms denied by default for every app without `USE_EXACT_ALARM`. Font scale up to 200 percent, non linear | Chronometer hidden without the permission. Font scale checks in the device test |
| 15 (API 35) | Edge to edge enforced for the app, no widget change. `AppWidgetManager.setWidgetPreview` exists (generated previews) and is **not** used | A6 uses `previewLayout` on 31 and above and a small PNG below |
| 16 (API 36) | Do Not Disturb page renamed "Modes" (already known). No widget specific change found in this planning session: **assumed**, verified by running the device suite on the owner's API 36 AVD `sajda` | `TEST-AND-RECORD.md` section 3 |
| 7.0 to 11 (API 24 to 30) | No exact alarm permission, chronometer always shown. `previewImage` only. `setChronometerCountDown` exists from API 24 | A6 |

## 6. Release A: Ramadan pack and widgets (phone 1.4.0, versionCode 8)

Branch: `claude/1-4-0-ramadan-widgets`. One pull request. Tasks A1 to A9 are one commit each unless
stated. Nothing in `:wear` changes. The Ramadan tasks come first so the pack is shippable on its own if
the widgets slip (`README.md`, release split).

### A0. Capture the baselines (no code change)

**Goal.** Numbers to compare every later gate against, so "nothing slipped in" is measured rather than
believed.

**Do.**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mkdir -p build/plan-baseline
./gradlew --rerun-tasks :core:testDebugUnitTest :app:testDebugUnitTest :wear:testDebugUnitTest \
  :app:lintDebug :wear:lintDebug :core:lintDebug :app:bundleRelease 2>&1 | tail -5
python3 - <<'EOF'
import glob, xml.etree.ElementTree as ET
t=f=0
for p in glob.glob('*/build/test-results/test*/*.xml'):
    r=ET.parse(p).getroot(); t+=int(r.get('tests')); f+=int(r.get('failures'))+int(r.get('errors'))
print('tests', t, 'failures', f)
EOF
ls -l app/build/outputs/bundle/release/app-release.aab | awk '{print $5}' > build/plan-baseline/aab-bytes.txt
BT=build/release-1.3.2/bundletool.jar
mkdir -p build/release-1.3.2
[ -f "$BT" ] || curl -fsSL -o "$BT" https://github.com/google/bundletool/releases/download/1.18.3/bundletool-all-1.18.3.jar
java -jar "$BT" dump manifest --bundle=app/build/outputs/bundle/release/app-release.aab \
  | grep -o 'uses-permission[^>]*' | sort > build/plan-baseline/permissions.txt
./gradlew :app:dependencies --configuration releaseRuntimeClasspath > build/plan-baseline/deps.txt
grep -c "" build/plan-baseline/permissions.txt
```

**Verify.** The test count printed is the number recorded in the status table (expected: the full suite,
0 failures. `docs/HANDOVER.md` section 9 says 145 but that table is stale, so record what the XML says).
`permissions.txt` lists exactly the six app permissions plus the library ones in F29 and the Firebase
install referrer permission, and contains no `AD_ID` or `ADSERVICES`. The `.aab` size is recorded (expect
about 4.3 MB, `docs/HANDOVER.md` section 9). `build/` is gitignored, so nothing is committed. Put the
three numbers in the `README.md` status table note for A0.

### A1. `HijriCalendar` and `RamadanInfo` in `:core`, and move both existing Hijri computations onto it

**Goal.** One definition of "what Hijri day is it for this user", used by the engine's Umm al-Qura rule,
the Home header, the Ramadan card, the widgets, the reminder planner and the PDF. Today there are two
private copies (F5, F6).

**New file** `core/src/main/java/com/sajdatime/core/HijriCalendar.kt`:

```kotlin
package com.sajdatime.core

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/** One day on the Umm al-Qura calendar as the user sees it (after their shift). */
data class HijriDay(val day: Int, val month: Int, val year: Int, val monthLength: Int)

/**
 * The Ramadan that is current, or the next one, for a user whose calendar is shifted by
 * [CalculationPrefs.hijriOffsetDays]. Dates are Gregorian and already shifted, so
 * [firstDay] is the calendar date on which this user's header reads 1 Ramadan.
 */
data class RamadanInfo(
    val hijriYear: Int,
    val firstDay: LocalDate,
    val lastDay: LocalDate,
    val length: Int,
    /** 1 to [length] while today is inside Ramadan, otherwise null. */
    val dayNumber: Int?,
) {
    val isRamadan: Boolean get() = dayNumber != null
    val isLastDay: Boolean get() = dayNumber == length
    /** Positive before Ramadan, 0 on the first day, negative inside. */
    fun daysUntilStart(today: LocalDate): Long = ChronoUnit.DAYS.between(today, firstDay)
    fun contains(date: LocalDate): Boolean = !date.isBefore(firstDay) && !date.isAfter(lastDay)
}

object HijriCalendar {
    const val RAMADAN = 9
    const val SHABAN = 8

    /**
     * The offset is applied as `date.plusDays(offset)` exactly as the two private copies did
     * (PrayerEngine.isRamadan, HomeScreen.hijriToday), so a stored shift keeps its meaning.
     * HijrahChronology is the Umm al-Qura calendar and covers 1300 to 1600 AH. Outside that
     * HijrahDate.from throws, hence null rather than a crash in a widget or a receiver.
     */
    fun of(date: LocalDate, offsetDays: Int): HijriDay? = runCatching {
        val h = HijrahDate.from(date.plusDays(offsetDays.toLong()))
        HijriDay(
            day = h.get(ChronoField.DAY_OF_MONTH),
            month = h.get(ChronoField.MONTH_OF_YEAR),
            year = h.get(ChronoField.YEAR),
            monthLength = h.lengthOfMonth(),
        )
    }.getOrNull()

    fun isRamadan(date: LocalDate, offsetDays: Int): Boolean =
        of(date, offsetDays)?.month == RAMADAN

    /** Current Ramadan if today is inside it, else the next one. Null only outside 1300 to 1600 AH. */
    fun ramadan(today: LocalDate, offsetDays: Int): RamadanInfo? {
        val h = of(today, offsetDays) ?: return null
        val year = if (h.month <= RAMADAN) h.year else h.year + 1
        return runCatching {
            val first = HijrahDate.of(year, RAMADAN, 1)
            val length = first.lengthOfMonth()
            // The user's header shows HijrahDate.from(g + offset), so the Gregorian day on which
            // it shows 1 Ramadan is the inverse, minus the offset.
            val firstDay = LocalDate.from(first).minusDays(offsetDays.toLong())
            RamadanInfo(
                hijriYear = year,
                firstDay = firstDay,
                lastDay = firstDay.plusDays(length - 1L),
                length = length,
                dayNumber = if (h.month == RAMADAN) h.day else null,
            )
        }.getOrNull()
    }
}
```

**Edit** `core/PrayerEngine.kt:433-436`: the body of `isRamadan` becomes
`HijriCalendar.isRamadan(date, offsetDays)`. Keep the function and its comment so the call site at
`:419` does not move.

**Edit** `app/ui/home/HomeScreen.kt:894-927` (`hijriToday`): replace the `runCatching { HijrahDate... }`
block with `HijriCalendar.of(date, offsetDays)?.let { Triple(it.day, it.month, it.year) }`. Remove the now
unused `HijrahDate` and `ChronoField` imports. Nothing else in the function changes, the bidi isolation of
the month name stays.

**Tests.** New `core/src/test/java/com/sajdatime/core/HijriCalendarTest.kt`, cases from
`RAMADAN-LOCATION-SPEC.md` section 9.1:

- `1 Ramadan 1447 is 18 February 2026 and the month has 30 days`
- `1 Ramadan 1448 is 8 February 2027 and the month has 29 days` (lastDay 8 March 2027)
- `the eve of Ramadan 1448 is 30 Shaban` (7 Feb 2027, offset 0 gives month 8 day 30, `dayNumber` null,
  `daysUntilStart` 1)
- `a shift of plus one makes 7 February the first day` (offset 1: `isRamadan(2027-02-07)` true,
  `ramadan().firstDay == 2027-02-07`, `dayNumber == 1`)
- `a shift of minus one makes 9 February the first day`
- `every offset from minus 2 to plus 2 keeps firstDay plus offset equal to the unshifted first day`
- `inside Ramadan the day number counts from 1 to the length` (sweep every day of Ramadan 1447 and 1448)
- `after Ramadan the next Ramadan is next year` (2027-03-09 gives hijriYear 1449, firstDay 2028-01-28)
- `a date outside the calendar's range gives null, not a throw` (`LocalDate.of(2200, 1, 1)`)
- `the engine's Umm al-Qura rule still moves with the shift` (keep `AdjustmentTest` green, no edit)

**Verify.**

```bash
./gradlew :core:testDebugUnitTest --tests "*HijriCalendarTest*" --tests "*PrayerEngineTest*" --tests "*AdjustmentTest*" && ./gradlew :app:testDebugUnitTest
```

Expected: BUILD SUCCESSFUL, the ten new tests pass, the existing Ramadan and Hijri shift tests pass
unmodified, `LocaleDisciplineTest` still green (no `Locale.getDefault()` was added).

**Definition of done.** Two private Hijri computations are gone, `grep -rn "HijrahDate" app/src/main
core/src/main` finds it only in `HijriCalendar.kt`.

### A2. Ramadan state in the ViewModel

**Goal.** Everything the card, the notice, the sheet and the widgets need, computed once per
`recalculate`, with no new ticks.

**Edit** `app/ui/SajdaViewModel.kt`:

```kotlin
/** Present only while this user's calendar is inside Ramadan or within the eve notice window. */
data class RamadanState(
    val info: RamadanInfo,
    val today: DayPrayerTimes,
    val yesterday: DayPrayerTimes,
    val tomorrow: DayPrayerTimes,
) {
    val suhoorEnds: Instant get() = today[PrayerSlot.FAJR]
    val iftar: Instant get() = today[PrayerSlot.MAGHRIB]
    val fastLength: Duration get() = Duration.between(suhoorEnds, iftar)
    /** Today's fast minus yesterday's, in whole minutes. Instants, so a DST day cannot distort it. */
    val fastChangeMinutes: Long get() =
        fastLength.toMinutes() - Duration.between(yesterday[PrayerSlot.FAJR], yesterday[PrayerSlot.MAGHRIB]).toMinutes()
    val tomorrowSuhoorEnds: Instant get() = tomorrow[PrayerSlot.FAJR]
    val tomorrowIftar: Instant get() = tomorrow[PrayerSlot.MAGHRIB]
    /** Whether tomorrow is still a fasting day on this calendar. */
    val tomorrowIsRamadan: Boolean get() = info.contains(tomorrow.date)

    enum class Phase { BEFORE_SUHOOR_END, FASTING, AFTER_IFTAR }
    fun phase(now: Instant): Phase = when {
        now.isBefore(suhoorEnds) -> Phase.BEFORE_SUHOOR_END
        now.isBefore(iftar) -> Phase.FASTING
        else -> Phase.AFTER_IFTAR
    }
}
```

`UiState` gains `val ramadan: RamadanState? = null`. In `recalculate` (F20), inside the existing
`withContext(Dispatchers.Default)` block, after `today`:

```kotlin
val offset = settings.calculationPrefs.hijriOffset
val info = HijriCalendar.ramadan(LocalDate.now(zone), offset)
val ramadan = info?.takeIf { it.isRamadan || it.daysUntilStart(today.date) in 0..2 }?.let {
    RamadanState(
        info = it,
        today = today,
        yesterday = PrayerEngine.compute(coordinates, today.date.minusDays(1), settings.calculationPrefs),
        tomorrow = PrayerEngine.compute(coordinates, today.date.plusDays(1), settings.calculationPrefs),
    )
}
```

and `_state.update { it.copy(..., ramadan = ramadan) }`. The eve window is `daysUntilStart in 0..2`
plus the first two days of Ramadan (`dayNumber in 1..2`), which the card and notice read from `info`.

**Why no new tick trigger.** `startClock` already recomputes when the next prayer passes (Fajr and Maghrib
are prayers) and at the date change (F20), which are the only moments the Ramadan phase changes.

**Edge cases.** No location: `ramadan` is null because `recalculate` returns early (F20). Outside 1300 to
1600 AH: null. Offset change: a settings emission, so `recalculate` runs. The user crosses a time zone:
`LocalDate.now(zone)` is read fresh, and the date change tick covers the open app.

**Tests.** `app/src/test/java/com/sajdatime/app/RamadanStateTest.kt`: build `RamadanState` from engine
output for London on 2027-02-22 and assert `fastLength == 12h18m`, `fastChangeMinutes == 4`, phases at
04:00, 12:00 and 20:00 local, `tomorrowIsRamadan` true on day 15 and false on the last day (2027-03-08),
and a DST day assertion (London 2027-03-28 fast change is 5 minutes, not 65). Values are in
`RAMADAN-LOCATION-SPEC.md` section 9.

**Verify.** `./gradlew :app:testDebugUnitTest --tests "*RamadanStateTest*"` passes.

### A3. The Ramadan card, its info note and the eve of Ramadan notice

**Goal.** The user facing pack on the Times screen, worded per `RAMADAN-LOCATION-SPEC.md` sections 4 and
5 and `WORDING-AND-LISTING.md` section B2.

**New file** `app/src/main/java/com/sajdatime/app/ui/home/RamadanCard.kt` with:

```kotlin
@Composable
internal fun RamadanCard(state: UiState, onOpenTimetable: () -> Unit, onOpenInfo: () -> Unit)

@Composable
internal fun RamadanInfoDialog(onDismiss: () -> Unit, onOpenDateShift: () -> Unit)

/** Shown from two days before the first day until day 2, once per Hijri year, dismissible. */
@Composable
internal fun RamadanEveNotice(state: UiState, onOpenDateShift: () -> Unit, onDismiss: () -> Unit)
```

Card layout (Material roles only, `sajdaSurface(RoundedCornerShape(24.dp))`, 20dp padding, as the Today
list card):

1. Label row: `ramadan_card_label` ("Ramadan") in `labelMedium` with the same 1.6sp letter spacing as
   `home_next_prayer`, and `ramadan_day_of` ("Day %1$d of %2$d") in `labelMedium` at the end.
2. Two columns: `ramadan_suhoor_ends` over `TimeFormat.clock(suhoorEnds)` and `ramadan_iftar` over
   `TimeFormat.clock(iftar)`, times in `PrayerTimeTextStyle` (tabular figures), labels in `bodyMedium`
   `onSurfaceVariant`. The column whose time is next (by `phase(now)`) gets `primaryContainer`
   background and the "Next" pill text from `home_next_marker`, never colour alone.
3. Countdown line, `bodyLarge`, `primary`: by phase, `ramadan_countdown_suhoor` ("Suhoor ends in %1$s"),
   `ramadan_countdown_iftar` ("Iftar in %1$s"), or after iftar either `ramadan_next_suhoor_tomorrow`
   ("Suhoor ends tomorrow at %1$s") when `tomorrowIsRamadan`, or `ramadan_month_ends_today` ("On this
   calendar today is the last day of Ramadan. Your mosque will announce Eid.") on the last day. The
   countdown string is `TimeFormat.countdownClock` and the row is `clearAndSetSemantics` with a
   `rememberRemainingText` summary, exactly as `NextPrayerCard` does (F22).
4. `ramadan_fast_length` ("Fast today %1$s") with `TimeFormat.remaining(context, suhoorEnds, iftar)`,
   joined by `ramadan_fast_line` to the plurals `ramadan_fast_change_longer` or
   `ramadan_fast_change_shorter`, or to `ramadan_fast_change_same` when the change is zero.
5. `ramadan_tomorrow_line` ("Tomorrow: suhoor ends %1$s, iftar %2$s"), hidden on the last day.
6. If `state.today.approximated`: one line `notif_approximate` ("Approximate time") in
   `onSurfaceVariant`. The polar banner above the card already names the latitude and explains it.
7. Actions row: `TextButton` `ramadan_open_timetable` ("Full timetable") and an `IconButton`
   `Icons.Outlined.Info` with content description `ramadan_info_a11y`, 48dp target.

Info dialog: `AlertDialog`, title `ramadan_info_title`, body paragraphs `ramadan_info_calc`,
`ramadan_info_margin`, `ramadan_info_mosque`, `ramadan_info_date`, then an `ExplainerAction` style button
`settings_hijri_offset` that calls `onOpenDateShift` (opens `SettingsChooser.ADJUSTMENTS` through
`onOpenSetting`, F25, F27), and `action_close`. Body is scrollable like every long dialog here.

Eve notice: `NoticeCard(title = ramadan_eve_title_before or ramadan_eve_title_after (before day 1, or on
days 1 and 2) with firstDay formatted by TimeFormat.date, body = ramadan_eve_body, onClick =
onOpenDateShift, onDismiss = onDismiss)` where dismiss calls
`viewModel.dismissRamadanNotice(info.hijriYear)` (key from section 3.1). Shown when
`state.ramadan != null && (info.daysUntilStart(today) in 0..2 || info.dayNumber in 1..2) &&
settings.ramadanNoticeDismissedYear != info.hijriYear`.

**Edit** `app/ui/home/HomeScreen.kt`: after `NextPrayerCard(state)` (line 139) insert
`RamadanCard(...)` guarded by `state.ramadan?.info?.isRamadan == true`, and `RamadanEveNotice(...)` next
to the rare banners. Add `var ramadanInfo by rememberSaveable { mutableStateOf(false) }` and
`var ramadanSheet by rememberSaveable { mutableStateOf(false) }` beside the existing booleans. Wire
`onOpenDateShift = { onOpenSetting(SettingsChooser.ADJUSTMENTS) }`. The Times screen will scroll during
Ramadan on a short phone: the column is already `verticalScroll` (F22) and the card sits above the rare
banners on purpose, because for the month it is the second most read thing on the screen. Record this in
the HomeScreen budget comment.

**Edit** `app/ui/SajdaViewModel.kt`: `fun dismissRamadanNotice(hijriYear: Int)` and the repository
setter from section 3.1.

**Edge cases.** No next prayer (`state.next == null`) cannot coincide with a Ramadan card because both
need coordinates. A projected day marks the card. The last day hides tomorrow and shows the month end
line. A user with offset plus 2 and a 30 day Shaban: the eve window still works because it is computed
from `firstDay`. Font scale 2.0: every row is `heightIn(min)` and wraps, nothing is fixed height.

**Tests.** No Compose tests exist in this project and none is added. The logic lives in `RamadanState`
(A2) and `HijriCalendar` (A1). The strings are checked by `RamadanWordingTest` (A9).

**Verify.** `./gradlew :app:assembleDebug :app:lintDebug` clean. On the API 36 AVD with root, set the
clock to 2027-02-22 10:00 local and open the app: the card shows Day 15 of 29, the eve notice is absent,
`Fast today 12h 18m` for London. Set 2027-02-06: no card, the eve notice shows "begins on Mon 8 Feb".
Set 2027-03-08 20:00: last day line. The emulator commands are in `TEST-AND-RECORD.md` section 3.2.
Screenshot each at font scale 1.0 and 2.0 and in the `rtl` build type, judged as layout.

### A4. The Ramadan timetable sheet and the PDF range

**Goal.** The month at a glance for the user's own place, on screen and on paper, through the existing
exporter.

**New composable** in `app/ui/home/RamadanCard.kt`:

```kotlin
@Composable
internal fun RamadanTimetableSheet(
    state: UiState,
    sheetState: SheetState,
    rows: List<DayPrayerTimes>,   // computed in the ViewModel, see below
    onExport: () -> Unit,
    onDismiss: () -> Unit,
)
```

A `ModalBottomSheet` like `ExportSheet` (F23): heading `ramadan_timetable_title` ("Ramadan %1$d") with
the place name from `settings.cityName.bidiIsolated()` under it and the existing `pdf_area_label`
("Approximate area") beneath that, the PDF's honesty line reused, then a column header row (`ramadan_col_day`,
`pdf_column_date`, `ramadan_suhoor_ends`, `ramadan_iftar`, `ramadan_col_fast`), then one row per day:
day number, `TimeFormat.date`, Fajr, Maghrib, `TimeFormat.remaining(Fajr, Maghrib)`. Today's row uses
`primaryContainer`. A projected day appends `ramadan_approx_marker` ("approx.") after each time and the
sheet ends with `pdf_approximate_note` when any row is projected. A `TextButton` `action_save_pdf` at
the top right calls `onExport`, which runs `exportPdf(Range.RAMADAN)`.

**Edit** `app/ui/SajdaViewModel.kt`: `fun ramadanRows(): List<DayPrayerTimes>` computed on demand
(`PrayerEngine.computeRange(coordinates, info.firstDay, info.length, prefs)`, 29 or 30 engine calls,
milliseconds) and held in `UiState.ramadanRows: List<DayPrayerTimes> = emptyList()` filled when the sheet
opens and cleared when it closes, so the common case carries nothing.

**Edit** `app/pdf/PrayerPdfExporter.kt`:

- `enum class Range { TODAY, NEXT_7_DAYS, THIS_MONTH, RAMADAN }`.
- In `export`, the `when` gains
  `Range.RAMADAN -> ramadanBounds(today, prefs)` where `ramadanBounds` looks up `HijriCalendar.ramadan(today, prefs.hijriOffset)`, throws when it is null, and returns `info.firstDay to info.length`.
  `prefs` already carries `hijriOffsetDays` (F8), so no new parameter.
- `fileName`: `Range.RAMADAN -> "PrayerTimes_Ramadan${info.hijriYear}.pdf"` (ASCII, `Locale.US` rule
  kept). Pass the year in: change the signature to `fileName(range, start, hijriYear: Int?)`.
- `drawBrandHeader`: `Range.RAMADAN -> context.getString(R.string.pdf_period_ramadan, hijriYear)` for
  the period line, then one extra wrapped line `pdf_ramadan_note` ("Suhoor ends at Fajr. Iftar at
  Maghrib. Follow your mosque where its timetable differs.") drawn with `subtitlePaint` the way
  `approximateNote` is.
- Columns for the Ramadan range only (a second `COLUMNS` list chosen by range in `drawHeaderRow` and
  `drawDataRow`): `pdf_column_ramadan_day` ("Day") 30, day name 62, date 85, six slots at 57.5. Total 522
  points, inside the 523 available (F24). The Ramadan day number for a row is
  `ChronoUnit.DAYS.between(info.firstDay, day.date) + 1`.
- Everything else (masthead, logo, approximate note, footer) unchanged.

**Edit** `app/ui/home/HomeScreen.kt` `ExportSheet` and `ExportOption` (F23): filter
`Range.entries` to exclude `RAMADAN` unless `state.ramadan != null` and today is within
`[info.firstDay.minusDays(30), info.lastDay]`. `ExportOption` maps `RAMADAN` to `export_ramadan` /
`export_ramadan_desc` ("Ramadan %1$d" / "Every day of the month, with suhoor and iftar"). `ExportSheet`
gains a `ramadanYear: Int?` parameter for the format argument.

**Edge cases.** Offset changes while the sheet is open: the settings emission recomputes `UiState` and the
sheet re reads `ramadanRows` on next open (close and reopen is acceptable, documented). A 30 day Ramadan
makes 31 rows with the header on one A4 page at 19 points a row: 30 rows by 19 = 570 plus header fits in
842 minus margins and masthead (F24 layout: `ROW_HEIGHT 19`, `PAGE_HEIGHT 842`, `MARGIN 36`,
`FOOTER_SPACE 16`), so the month stays on one page.

**Tests.** `app/src/test/java/com/sajdatime/app/RamadanPdfRangeTest.kt`: the pure helper
`PrayerPdfExporter.rangeBounds(range, today, prefs)` (extract the `when` into an `internal` function so it
is testable without a Canvas) returns `2027-02-08 to 29` for `RAMADAN` on 2027-02-22 with offset 0, and
`2027-02-07 to 29` with offset plus 1. `fileName` gives `PrayerTimes_Ramadan1448.pdf`.

**Verify.** `./gradlew :app:testDebugUnitTest --tests "*RamadanPdfRangeTest*"` passes. On the emulator with
the clock at 2027-02-22, export the Ramadan PDF, `adb pull` it from `Download/` and open it: 29 rows,
day numbers 1 to 29, the note under the masthead, the file named `PrayerTimes_Ramadan1448.pdf`. Compare the
London rows for 8 Feb, 22 Feb and 8 Mar with `RAMADAN-LOCATION-SPEC.md` section 9.2.

### A5. The suhoor reminder (two commits: planner and settings first, then scheduler and receiver)

**Goal.** An optional reminder a set number of minutes before Fajr on Ramadan days only, off by default,
through the existing alarm ladder, with its own action and request code band, obeying the silent phone
switch, Do Not Disturb handling and the approximate day downgrade exactly as prayer alerts do.

**Commit A5a: data, planner, Settings row.**

*Edit* `app/data/SettingsRepository.kt`: the three keys from section 3.1, the three `AppSettings` fields,
three setters, and in `toAppSettings` read `suhoorReminderMinutes` through
`SUHOOR_CHOICES.firstOrNull { it == stored } ?: 0` with `val SUHOOR_CHOICES = listOf(0, 15, 30, 45, 60)` on
`AppSettings.Companion`, so a corrupt value reads as off. `usesAlarm` becomes
`alertFor.containsValue(AlertStyle.ALARM) || (suhoorReminderMinutes > 0 && suhoorReminderStyle == AlertStyle.ALARM)`
so the Do Not Disturb card and the sound row appear for a user whose only alarm is the reminder.

*New file* `app/src/main/java/com/sajdatime/app/notify/SuhoorReminderPlan.kt`:

```kotlin
package com.sajdatime.app.notify

/** One planned reminder: the Ramadan date it belongs to, when it fires, and the Fajr it precedes. */
data class SuhoorReminder(val date: LocalDate, val at: Instant, val fajrAt: Instant)

object SuhoorReminderPlan {
    /**
     * Pure. Reminders for every day in the same window the prayer scheduler uses (yesterday,
     * today, tomorrow) that is Ramadan on the user's shifted calendar, at Fajr minus [minutesBefore],
     * only those still in the future. Empty when the reminder is off.
     */
    fun plan(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        minutesBefore: Int,
        today: LocalDate,
        now: Instant,
    ): List<SuhoorReminder> {
        if (minutesBefore <= 0) return emptyList()
        return (-1L..1L).mapNotNull { offset ->
            val date = today.plusDays(offset)
            if (!HijriCalendar.isRamadan(date, prefs.hijriOffset)) return@mapNotNull null
            val fajr = PrayerEngine.compute(coordinates, date, prefs)[PrayerSlot.FAJR]
            val at = fajr.minusSeconds(60L * minutesBefore)
            SuhoorReminder(date, at, fajr).takeIf { at.isAfter(now) }
        }
    }
}
```

*Edit* `app/ui/SajdaViewModel.kt`: `setSuhoorReminderMinutes(minutes)`, `setSuhoorReminderStyle(style)`.

*Edit* `app/ui/settings/SettingsScreen.kt`: `SettingsChooser` gains `SUHOOR`. In the REMINDERS group
(F25) after the Prayer alerts row:

```kotlin
SettingRow(
    icon = Icons.Outlined.DarkMode,
    title = stringResource(R.string.settings_suhoor),
    subtitle = suhoorSummary(settings.suhoorReminderMinutes, settings.suhoorReminderStyle),
    onClick = { open = SettingsChooser.SUHOOR },
)
```

`suhoorSummary` returns `suhoor_off` or `settings_value_pair(plurals suhoor_minutes_before, alert style
label)`. The chooser is a `ChooserDialog` (F25) with `settings_suhoor_help`, five `RadioRow`s from
`AppSettings.SUHOOR_CHOICES` (`suhoor_off` for 0, `suhoor_minutes_before` plural otherwise), and when the
value is not 0 a row of two `ChoiceChip`s for `alert_style_notification` and `alert_style_alarm`
(`ChoiceChip` exists at `SettingsScreen.kt:824`). Nothing about Ramadan dates is shown here beyond the
help line, which says the reminder only runs during Ramadan on the calendar the user has set.

*Tests.* `SuhoorReminderPlanTest` (app unit test, no Android):

| Case | Inputs | Expected |
|---|---|---|
| off | minutes 0, inside Ramadan | empty |
| outside Ramadan | London, 2027-01-20, 30 min | empty |
| inside | London, today 2027-02-22 10:00 local, 30 min | one entry, date 2027-02-23, at = Fajr(23 Feb) minus 30 min, today's own reminder excluded because it has passed |
| before today's Fajr | today 2027-02-22 03:00 local | two entries, 22 and 23 Feb |
| eve | today 2027-02-07 12:00 | one entry for 2027-02-08 (tomorrow is day 1) |
| last day | today 2027-03-08 12:00 | empty (tomorrow 9 March is Shawwal) |
| last day early | today 2027-03-08 03:00 | one entry for 8 March |
| shifted | offset minus 1, today 2027-02-08 12:00 | one entry for 2027-02-09 (day 1 for this user) |
| never coincides with Fajr | every choice in 15, 30, 45, 60 | `at` strictly before `fajrAt` |

Values for Fajr at London are in `RAMADAN-LOCATION-SPEC.md` 9.2 and are asserted by recomputing through
the engine, not by literal minutes, so the test cannot drift from the engine.

**Commit A5b: scheduler, receiver, notification.**

*Edit* `app/notify/PrayerAlarmScheduler.kt`:

```kotlin
const val ACTION_SUHOOR_REMINDER = "com.sajdatime.app.action.SUHOOR_REMINDER"
const val EXTRA_FAJR_AT_MILLIS = "fajr_at"
const val EXTRA_DATE_EPOCH_DAY = "date"
/** Disjoint from dayOfYear * 10 + ordinal (10..3665) by construction. RequestCodeTest proves it. */
internal const val SUHOOR_REQUEST_BASE = 10_000
internal fun suhoorRequestCode(date: LocalDate): Int = SUHOOR_REQUEST_BASE + date.dayOfYear
internal fun prayerRequestCode(slot: PrayerSlot, date: LocalDate): Int = date.dayOfYear * 10 + slot.ordinal
```

`requestCode` (line 266) becomes a call to `prayerRequestCode` so the formula exists once and is
`internal` for the test. `schedule(context, manager, slot, at)` is split: the ladder moves into
`private fun schedule(manager: AlarmManager, context: Context, pendingIntent: PendingIntent, at: Instant)`
and the prayer path calls it with `alarmIntent(context, slot, at)`. In `reschedule`, after the prayer
loop:

```kotlin
SuhoorReminderPlan.plan(coordinates, settings.calculationPrefs, settings.suhoorReminderMinutes, today, now)
    .forEach { reminder -> schedule(manager, context, reminderIntent(context, reminder), reminder.at) }
```

`reminderIntent` builds `Intent(context, PrayerAlarmReceiver::class.java).setAction(ACTION_SUHOOR_REMINDER)
.putExtra(EXTRA_FAJR_AT_MILLIS, reminder.fajrAt.toEpochMilli()).putExtra(EXTRA_DATE_EPOCH_DAY,
reminder.date.toEpochDay())` with `getBroadcast(context, suhoorRequestCode(reminder.date), intent,
FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)`. `cancelAll` gains, inside the same `(-1..HORIZON_DAYS)` loop, a
`FLAG_NO_CREATE` lookup of the reminder intent for that date and cancels it when present. The
`alertFor.isEmpty()` early return at line 44 must move **after** `cancelAll` and must not skip the
reminder: a user with every prayer alert off may still want the reminder, so the structure becomes
cancel everything, then lay down the reminders, then return if `alertFor` is empty, then lay down the prayers.

*Edit* `app/notify/PrayerAlarmReceiver.kt`: replace the `if (intent.action != ACTION_PRAYER) return`
with a `when`. The reminder branch:

```kotlin
PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER -> {
    val fajrAt = Instant.ofEpochMilli(intent.getLongExtra(EXTRA_FAJR_AT_MILLIS, 0L))
    val date = LocalDate.ofEpochDay(intent.getLongExtra(EXTRA_DATE_EPOCH_DAY, 0L))
    launch under goAsync {
        val settings = SettingsRepository(appContext).current()
        val stillWanted = settings.suhoorReminderMinutes > 0 &&
            HijriCalendar.isRamadan(date, settings.calculationPrefs.hijriOffset) &&
            fajrAt.isAfter(Instant.now().minusSeconds(5 * 60))   // not stale by more than five minutes
        if (stillWanted) Notifications.postSuhoorReminder(appContext, fajrAt, settings)
        PrayerAlarmScheduler.reschedule(appContext, settings)
        OngoingBadge.refresh(appContext, settings)
    }
}
```

The three checks exist because the user may have switched it off, shifted the calendar, or the alarm
may be arriving after Fajr on a device that parked it: a suhoor reminder after Fajr is worse than none.

*Edit* `app/notify/Notifications.kt`: `const val ID_SUHOOR = 3000` and

```kotlin
fun postSuhoorReminder(base: Context, fajrAt: Instant, settings: AppSettings) {
    val context = AppLocale.wrap(base)
    if (!canPost(context)) return
    ensureChannels(context)
    val approximate = settings.coordinates?.let {
        PrayerEngine.compute(it, fajrAt.atZone(ZoneId.systemDefault()).toLocalDate(), settings.calculationPrefs).approximated
    } ?: false
    val effectiveStyle = effectiveAlertStyle(settings.suhoorReminderStyle, settings.alarmRespectsSilent,
        approximate, settings.alarmOnApproximateDays) { phoneIsSilenced(context) }
    val channelId = if (effectiveStyle == AlertStyle.ALARM) ensureAlarmChannel(context, settings.alarmSoundUri) else CHANNEL_PRAYERS
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.notif_suhoor_title, TimeFormat.clock(context, fajrAt)))
        .setContentText(context.getString(R.string.notif_suhoor_body, PrayerSlot.FAJR.label(context), TimeFormat.remaining(context, Instant.now(), fajrAt)))
        .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
        .setContentIntent(openAppIntent(context)).markIfApproximate(context, approximate)
    // category, vibration and the below O sound branch: identical to postPrayerAlert
    post(context, ID_SUHOOR, builder.build())
}
```

The category, vibration and pre Oreo sound handling are extracted into a private
`applyStyle(builder, effectiveStyle, alarmSoundUri)` used by both posters so they cannot diverge.

*Tests.*

- `RequestCodeTest` (app): for `year in 2027..2028` (one leap year) and every day and prayer slot,
  `prayerRequestCode` is in 10..3665 and `suhoorRequestCode` in 10_001..10_366, the two sets are
  disjoint, and the two action strings differ. A7 extends the same test with
  `WidgetRefreshScheduler.REQUEST_CODE == 20_000` in neither set and the third action distinct.
- `AlertStyleDowngradeTest` is reused unchanged for the reminder by construction (same function).
- `SuhoorReminderReceiverTest` (app, pure): extract `shouldPostReminder(settings, date, fajrAt, now)`
  as an `internal` function and test: off → false, calendar shifted so the date is no longer Ramadan →
  false, five minutes late → true, six minutes late → false, normal → true.

**Edge cases.** A reminder can fall before midnight on the previous calendar day whenever Fajr minus
the chosen minutes is before 00:00. That happens at none of the reference places (every Fajr there is
after 03:00) but is possible in principle, so the reminder's `date` is the Ramadan day it precedes and
its request code uses that date: a reminder that fires at 23:30 on the eve still carries the right day
and is cancelled in the right window. A projected day drops the style to notification unless the override is on, with the
`notif_approximate` sub text, same as a prayer. A user who turns the reminder on mid night during
Ramadan gets today's reminder if Fajr minus N is still ahead. `POST_NOTIFICATIONS` denied: `canPost`
returns early, as for prayers. Below Android 8: the channel branch is skipped as in `postPrayerAlert`.

**Verify.**

```bash
./gradlew :app:testDebugUnitTest --tests "*Suhoor*" --tests "*RequestCodeTest*" --tests "*AlertStyleDowngradeTest*" --tests "*AlertCodecTest*"
```

All green. Then the device check from `TEST-AND-RECORD.md` section 3.3: with the clock at 2027-02-21 23:00
and the reminder at 30 minutes, `adb shell dumpsys alarm | grep -A3 SUHOOR` shows one
`*walarm*:com.sajdatime.app.action.SUHOOR_REMINDER` at Fajr minus 30 for 22 February, and the
`PRAYER_ALARM` entries present before the change are present after it with the same `when`.

**Definition of done.** Existing alarm tests unmodified and green. The reminder is absent from
`dumpsys alarm` when off and after Ramadan. The Settings row reads "Off" on a fresh install.

### A6. The two widgets (two commits: pure state and resources first, then providers and renderer)

**Goal.** "Next prayer" (2 by 1) and "Today" (4 by 2), free, no lock, never wrong, drawn with
`RemoteViews`.

**Commit A6a: state, colours, strings, layouts, provider info.**

*New file* `app/src/main/java/com/sajdatime/app/widget/WidgetState.kt`:

```kotlin
package com.sajdatime.app.widget

/** Everything the two widgets draw, computed once per refresh. Pure, no Android. */
data class WidgetState(
    val hasLocation: Boolean,
    val today: DayPrayerTimes?,
    val next: NextPrayer?,
    /** The next prayer's own day, so "Tomorrow" and the Ramadan label follow its date, not today's. */
    val nextIsApproximate: Boolean,
    val ramadanToday: Boolean,
    val ramadanOnNextDay: Boolean,
    val countdownAllowed: Boolean,
) {
    fun isHighlighted(slot: PrayerSlot): Boolean = next != null && slot == next.slot && !next.isTomorrow

    companion object {
        fun build(settings: AppSettings, now: Instant, zone: ZoneId, canScheduleExact: Boolean): WidgetState {
            val coordinates = settings.coordinates
                ?: return WidgetState(false, null, null, false, false, false, canScheduleExact)
            val prefs = settings.calculationPrefs
            val todayDate = now.atZone(zone).toLocalDate()
            val today = PrayerEngine.compute(coordinates, todayDate, prefs)
            val next = PrayerEngine.nextPrayer(coordinates, prefs, now, zone)
            val nextDate = next.at.atZone(zone).toLocalDate()
            return WidgetState(
                hasLocation = true,
                today = today,
                next = next,
                nextIsApproximate = PrayerEngine.compute(coordinates, nextDate, prefs).approximated,
                ramadanToday = HijriCalendar.isRamadan(todayDate, prefs.hijriOffset),
                ramadanOnNextDay = HijriCalendar.isRamadan(nextDate, prefs.hijriOffset),
                countdownAllowed = canScheduleExact,
            )
        }
    }
}
```

*Colours.* Append to `app/src/main/res/values/colors.xml` and `values-night/colors.xml`, values copied
from `Color.kt` (F31), light then dark: `widget_surface` `#FDFCFB` / `#171B1A`, `widget_on_surface`
`#1B211E` / `#E7EAE8`, `widget_on_surface_variant` `#5A605C` / `#A9B2AE`, `widget_highlight` `#D7EAE0` /
`#143028`, `widget_on_highlight` `#0F2A1F` / `#8FD9B8`, `widget_primary` `#0E6B4F` / `#4FC48F`,
`widget_outline_variant` `#C9CFC9` / `#2A312E`. `WidgetColorsTest` reads both XML files off disk
(declare them as test inputs in `app/build.gradle.kts`, the block at lines 214-232) and asserts each
equals the matching Kotlin constant and that `widget_on_surface` on `widget_surface`,
`widget_on_surface_variant` on `widget_surface`, `widget_on_highlight` on `widget_highlight` and
`widget_primary` on `widget_surface` clear 4.5:1 in both themes, using the contrast function already in
`ColorContrastTest` (move it to a small `internal` helper in the test source set).

*Dimens.* `res/values/dimens.xml`: `widget_corner_radius` 16dp. `res/values-v31/dimens.xml`:
`widget_corner_radius` `@android:dimen/system_app_widget_background_radius`.

*Drawables.* `res/drawable/widget_background.xml`: `shape` rectangle, `solid` `@color/widget_surface`,
`corners` `@dimen/widget_corner_radius`, `stroke` 1dp `@color/widget_outline_variant`.
`res/drawable/widget_row_highlight.xml`: rectangle, `solid` `@color/widget_highlight`, corners 8dp.
`res/drawable/widget_row_plain.xml`: rectangle, transparent.

*Layout* `res/layout/widget_next_prayer.xml` (ids fixed, no `android:text` anywhere):

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/widget_root" android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:gravity="center_vertical"
    android:background="@drawable/widget_background" android:padding="12dp">
    <TextView android:id="@+id/widget_label" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textSize="12sp" android:textColor="@color/widget_on_surface_variant" android:maxLines="1"
        android:ellipsize="end" android:textAlignment="viewStart" android:importantForAccessibility="no" />
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
        <TextView android:id="@+id/widget_name" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:textSize="18sp" android:textStyle="bold" android:textColor="@color/widget_on_surface"
            android:maxLines="1" android:ellipsize="end" android:textAlignment="viewStart" android:importantForAccessibility="no" />
        <TextView android:id="@+id/widget_time" android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:textSize="18sp" android:textColor="@color/widget_on_surface" android:maxLines="1"
            android:layout_marginStart="8dp" android:importantForAccessibility="no" />
    </LinearLayout>
    <TextView android:id="@+id/widget_sublabel" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textSize="12sp" android:textColor="@color/widget_on_surface_variant" android:maxLines="1"
        android:visibility="gone" android:textAlignment="viewStart" android:importantForAccessibility="no" />
    <Chronometer android:id="@+id/widget_countdown" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textSize="14sp" android:textColor="@color/widget_primary" android:visibility="gone"
        android:textAlignment="viewStart" android:importantForAccessibility="no" />
</LinearLayout>
```

`res/layout/widget_today.xml`: the same root and background, then a header `LinearLayout` with
`widget_next_line` (TextView, 13sp, `widget_primary`) and `widget_countdown` (Chronometer, 13sp,
`widget_primary`, gone by default), a 1dp divider `View` with `@color/widget_outline_variant`, then six
horizontal `LinearLayout` rows with ids `widget_row_fajr` ... `widget_row_isha`, each containing
`widget_name_<slot>` (0dp weight 1, 14sp, `widget_on_surface`), `widget_label_<slot>` (12sp,
`widget_on_surface_variant`, gone, used for the Ramadan words and the "Next" marker) and
`widget_time_<slot>` (14sp, `widget_on_surface`). Sunrise row text size 13sp and colour
`widget_on_surface_variant`, matching the dimmed row on Home (F22). Rows are `wrap_content` with 4dp
vertical padding so font scaling grows them.

*Provider info* `res/xml/widget_next_prayer_info.xml`:

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools" tools:targetApi="s"
    android:minWidth="110dp" android:minHeight="40dp"
    android:minResizeWidth="110dp" android:minResizeHeight="40dp"
    android:targetCellWidth="2" android:targetCellHeight="1"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="0"
    android:initialLayout="@layout/widget_next_prayer"
    android:previewLayout="@layout/widget_next_prayer"
    android:previewImage="@drawable/widget_preview_next"
    android:description="@string/widget_next_desc"
    android:widgetCategory="home_screen" />
```

`widget_today_info.xml`: `minWidth` 250dp, `minHeight` 110dp, `targetCellWidth` 4, `targetCellHeight` 2,
layout `widget_today`, preview `widget_preview_today`, description `widget_today_desc`.

*Preview images.* Two PNGs under `res/drawable-nodpi/`, each under 30 KB, captured from the API 36
emulator at 2x cell size after A6b lands (`adb exec-out screencap -p` then `sips -c` to crop). They are
the first raster assets in the app and the reason is stated in the commit: a launcher below Android 12
shows the preview image in the picker and an icon there would misdescribe the widget. If the owner objects to
raster assets, `previewImage` falls back to `@mipmap/ic_launcher` (a stated default in `README.md`).

*Strings.* `widget_next_label` (picker name, "SajdaTime: next prayer"), `widget_today_label`,
`widget_next_desc`, `widget_today_desc`, `widget_next_prayer` ("Next prayer"), `widget_tomorrow`
("Tomorrow"), `widget_next_line` ("Next: %1$s at %2$s"), `widget_next_line_tomorrow`, `widget_no_location`
("Open SajdaTime to set your location"), `widget_a11y_next` ("Next prayer %1$s at %2$s, in %3$s"),
`widget_a11y_next_tomorrow`, `widget_a11y_today` ("Today's prayer times. %1$s"), `widget_a11y_row`
("%1$s %2$s"), `widget_suhoor_ends` ("Suhoor ends"), `widget_iftar` ("Iftar"). Full text in
`WORDING-AND-LISTING.md` section B3.

*Tests.* `WidgetStateTest` (app, pure, London and Tromsø, dates from the spec). Cases: no location. Before
Fajr (next Fajr, highlighted, not tomorrow). After Isha (next tomorrow's Fajr, `isTomorrow` true, nothing
highlighted). Sunrise never highlighted at any minute of a day sweep. `countdownAllowed` mirrors the flag.
`ramadanOnNextDay` true at 23:00 on 7 Feb 2027 while `ramadanToday` is false. A projected Tromsø day in
June sets `nextIsApproximate`. `WidgetColorsTest` as above. `WidgetResourcesTest` reads the two provider
XML files off disk and asserts `updatePeriodMillis="0"`, both layouts referenced, and that neither layout
file contains `android:text=` (the host locale trap in section 5).

**Commit A6b: renderer, updater, providers, manifest.**

*New file* `app/widget/WidgetRenderer.kt`:

```kotlin
internal object WidgetRenderer {
    fun nextPrayer(base: Context, state: WidgetState, now: Instant): RemoteViews
    fun today(base: Context, state: WidgetState, now: Instant): RemoteViews
}
```

Both start with `val context = AppLocale.wrap(base)` (F18) and share three private helpers.
`applyDirection(views)` calls `views.setInt(R.id.widget_root, "setLayoutDirection", direction)` where
direction is `View.LAYOUT_DIRECTION_RTL` when `TextUtils.getLayoutDirectionFromLocale(AppLocale.of(context)) == RTL`.
`applyTap(views)` sets `setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 1,
Intent(context, MainActivity::class.java).setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP),
FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE))`. `applyCountdown(views, id, at, now, allowed)` sets
visibility and, when allowed, `setChronometerCountDown(id, true)` and `setChronometer(id,
SystemClock.elapsedRealtime() + (at.toEpochMilli() - now.toEpochMilli()), null, true)`.

`nextPrayer`: no location → label `widget_no_location`, name and time empty, sublabel gone. Otherwise the
label is `widget_tomorrow` when `next.isTomorrow` else `widget_next_prayer`, the name is
`next.slot.label(context)`, the time is `TimeFormat.clock(context, next.at)`, the sublabel is
`widget_suhoor_ends` when the slot is Fajr and `ramadanOnNextDay`, `widget_iftar` when the slot is
Maghrib and `ramadanToday`, else `notif_approximate` when `nextIsApproximate`, else gone. The countdown is
as above and the content description on `widget_root` comes from `widget_a11y_next` or
`widget_a11y_next_tomorrow` with `TimeFormat.remaining`.

`today`: header `widget_next_line` with the next slot and time (prefixed by `widget_tomorrow` when
tomorrow), countdown, then for each slot: name, time, `setInt(rowId, "setBackgroundResource",
if highlighted widget_row_highlight else widget_row_plain)`, text colours `widget_on_highlight` or
`widget_on_surface`, the label view set to `home_next_marker` when highlighted, or the Ramadan word for
Fajr and Maghrib during Ramadan (`ramadanToday`), else gone. Content description on the root from
`widget_a11y_today` joining six `widget_a11y_row` strings, so TalkBack reads the whole widget once.

*New file* `app/widget/WidgetUpdater.kt`:

```kotlin
object WidgetUpdater {
    /** Returns true when at least one widget instance exists. Cheap when none does. */
    fun refreshAll(context: Context, settings: AppSettings): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val nextIds = manager.getAppWidgetIds(ComponentName(context, NextPrayerWidgetProvider::class.java))
        val todayIds = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
        if (nextIds.isEmpty() && todayIds.isEmpty()) return false   // A7 adds WidgetRefreshScheduler.cancel(context) here
        val now = Instant.now()
        val state = WidgetState.build(settings, now, ZoneId.systemDefault(), PrayerAlarmScheduler.canScheduleExact(context))
        runCatching {
            if (nextIds.isNotEmpty()) manager.updateAppWidget(nextIds, WidgetRenderer.nextPrayer(context, state, now))
            if (todayIds.isNotEmpty()) manager.updateAppWidget(todayIds, WidgetRenderer.today(context, state, now))
        }
        // A7 adds: WidgetRefreshScheduler.scheduleNext(context, state, now)
        return true
    }
    suspend fun refreshAll(context: Context): Boolean = refreshAll(context, SettingsRepository(context).current())
}
```

*New files* `app/widget/NextPrayerWidgetProvider.kt` and `TodayWidgetProvider.kt`: `AppWidgetProvider`
subclasses whose `onUpdate`, `onEnabled` and `onAppWidgetOptionsChanged` call a shared
`refreshFromProvider(context)` that runs `WidgetUpdater.refreshAll(context)` under `goAsync` on
`Dispatchers.Default` (same shape as `PrayerAlarmReceiver`). A7 adds an `onDisabled` that cancels the
refresh alarm when the other provider also has no ids.

*Manifest* (`app/src/main/AndroidManifest.xml`, after the two existing receivers):

```xml
<receiver
    android:name=".widget.NextPrayerWidgetProvider"
    android:exported="false"
    android:label="@string/widget_next_label">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/widget_next_prayer_info" />
</receiver>
<receiver
    android:name=".widget.TodayWidgetProvider"
    android:exported="false"
    android:label="@string/widget_today_label">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/widget_today_info" />
</receiver>
```

`exported="false"` is what the current Android Studio widget template and the developer.android.com
"Create a simple widget" example generate: the system, not another app, sends `APPWIDGET_UPDATE`. If the
widget device test (A7) finds the provider is never updated on the API 36 AVD, flip to `true` and record
why in the commit. **Assumed** until that test runs.

*Hook into the four sites* comes in A7. This commit only makes the widgets exist and draw on placement.

*Tests.* `WidgetManifestTest` reads the manifest off disk: both receivers declared with the update
action and the meta data, `exported` present (either value) on both, and still no new `uses-permission`
(count the lines against A0's baseline list, which the test embeds as a constant to be updated
deliberately).

**Verify.** `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` clean (lint will flag
`MissingTranslation` until A9, so A6 adds the four languages for its own strings now, not later). On the
API 36 AVD: long press the home screen, add both widgets, see today's times and the next prayer with the
countdown running. Grant and revoke "Alarms and reminders" and confirm the countdown appears and
disappears after the next refresh. Switch the phone to dark mode: the widget follows. Set the app
language to Urdu: the widget strings change after A7's hook (before it, only after re adding).

**Definition of done.** Both widgets in the picker with names and descriptions, both render on
placement, tap opens the app, no new permission, `WidgetStateTest` green.

### A7. Widget refresh scheduler and the hooks at every existing trigger

**Goal.** Widgets stay current without the app open and without polling, through the triggers in
section 4.2.

*Edit* `app/widget/WidgetUpdater.kt` and the two providers: add the `scheduleNext` call, the `cancel`
call and the `onDisabled` override that A6b left out (marked with "A7 adds" comments there).

*New file* `app/widget/WidgetRefreshScheduler.kt`:

```kotlin
object WidgetRefreshScheduler {
    const val ACTION_REFRESH = "com.sajdatime.app.action.WIDGET_REFRESH"
    /** Single alarm, single code, in neither alarm band (RequestCodeTest). */
    internal const val REQUEST_CODE = 20_000

    /** Next transition: the next prayer or local midnight, whichever is first, plus two seconds. */
    internal fun nextRefreshAt(state: WidgetState, now: Instant, zone: ZoneId): Instant? {
        if (!state.hasLocation) return null
        val midnight = now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
        val candidate = listOfNotNull(state.next?.at, midnight).minOrNull() ?: return null
        return candidate.plusSeconds(2)
    }

    fun scheduleNext(context: Context, state: WidgetState, now: Instant) {
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return
        val at = nextRefreshAt(state, now, ZoneId.systemDefault()) ?: return cancel(context)
        val pending = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
        runCatching {
            if (PrayerAlarmScheduler.canScheduleExact(manager)) manager.setExact(AlarmManager.RTC, at.toEpochMilli(), pending)
            else manager.set(AlarmManager.RTC, at.toEpochMilli(), pending)
        }.onFailure { runCatching { manager.set(AlarmManager.RTC, at.toEpochMilli(), pending) } }
    }

    fun cancel(context: Context) { /* FLAG_NO_CREATE lookup, cancel alarm and pending intent */ }

    private fun pendingIntent(context: Context, flags: Int): PendingIntent? =
        PendingIntent.getBroadcast(context, REQUEST_CODE,
            Intent(context, WidgetRefreshReceiver::class.java).setAction(ACTION_REFRESH),
            flags or PendingIntent.FLAG_IMMUTABLE)
}
```

`AlarmManager.RTC`, not `RTC_WAKEUP`: a display refresh must never wake the phone. `setExact` on API 31
and above needs `SCHEDULE_EXACT_ALARM`, which `canScheduleExact` already answers (F10), and the
`onFailure` branch covers a revocation between check and call, the same reason the prayer ladder uses
`runCatching`.

*New file* `app/widget/WidgetRefreshReceiver.kt`: a `BroadcastReceiver` that ignores any action other than
`ACTION_REFRESH`, then under `goAsync` on `Dispatchers.Default` calls `WidgetUpdater.refreshAll(context)`.
Manifest: `<receiver android:name=".widget.WidgetRefreshReceiver" android:exported="false"> <intent-filter>
<action android:name="com.sajdatime.app.action.WIDGET_REFRESH" /> </intent-filter> </receiver>`.

*Hooks (one line each).*

| File and line | Insert after | Line to add |
|---|---|---|
| `app/ui/SajdaViewModel.kt:400` | `OngoingBadge.refresh(context, settings)` | `WidgetUpdater.refreshAll(context, settings)` |
| `app/notify/PrayerAlarmReceiver.kt:49` and the new reminder branch | `OngoingBadge.refresh(appContext, settings)` | `WidgetUpdater.refreshAll(appContext, settings)` |
| `app/notify/DailyRescheduleWorker.kt:100` | `OngoingBadge.refresh(applicationContext, settings)` | `WidgetUpdater.refreshAll(applicationContext, settings)` |
| `app/notify/SystemEventReceiver.kt:148` | `OngoingBadge.refresh(appContext, settings)` | `WidgetUpdater.refreshAll(appContext, settings)` |
| `app/ui/settings/AppLanguage.kt:82` | the `applicationLocales` assignment | `CoroutineScope(Dispatchers.Default).launch { WidgetUpdater.refreshAll(context.applicationContext) }` |

*Locale broadcast.* `SystemEventReceiver.HANDLED_ACTIONS` gains `Intent.ACTION_LOCALE_CHANGED` and the
manifest filter gains `<action android:name="android.intent.action.LOCALE_CHANGED" />`. The receiver's
existing body (ensure channels, reschedule, badge, worker) is harmless on a locale change and keeps the
channel names in the new language, which is a small existing gap this closes for free: say so in the
commit.

*Tests.* `WidgetRefreshSchedulerTest` (pure): `nextRefreshAt` picks the next prayer when it is before
midnight, picks midnight when the next prayer is tomorrow's Fajr, returns null without a location, and
always returns a time strictly after `now`. `RequestCodeTest` (A5) already asserts the code and action.
`SystemEventActionsTest`: the receiver's `HANDLED_ACTIONS` and the manifest filter list the same six
actions (read off disk).

**Instrumented test** `app/src/androidTest/java/com/sajdatime/app/WidgetDeviceTest.kt`, run as
`TEST-AND-RECORD.md` section 3.1 describes (needs `adb shell appwidget grantbind --package
com.sajdatime.app --user 0` first, which the release script does):

1. Seed settings: London coordinates, city "London, United Kingdom", defaults otherwise, through
   `SettingsRepository`.
2. Render directly: `WidgetRenderer.nextPrayer(context, state, now).apply(context, parent)` into a
   `FrameLayout`, which is what a launcher does with a `RemoteViews`, and read the `TextView`s by id.
   This exercises every reflective call (`setLayoutDirection`, `setChronometerCountDown`,
   `setBackgroundResource`) on a real Android framework.
3. Assert: the name is a prayer label, the time matches `TimeFormat.clock` of `state.next.at`, the
   chronometer is visible when `canScheduleExact` and gone otherwise, the root content description is
   non empty, and no `TextView` is ellipsised at font scale 1.0 (`layout.getEllipsisCount(0) == 0`).
4. Repeat with the app locale set to `ur` (through `LocaleManager`, restored in `@After` as
   `LanguageAndDndDeviceTest` does): the label is the Urdu string and the root's layout direction is RTL.
5. Repeat the Today layout: six rows, exactly one highlighted before Isha, none after Isha (set a fake
   `now` into `WidgetState.build` for that case), sunrise never highlighted.
6. Separately, create an `AppWidgetHost(context, 1)`, `allocateAppWidgetId()`,
   `bindAppWidgetIdIfAllowed` to each provider (this is what needs the bind grant), call
   `WidgetUpdater.refreshAll(context)`, and assert `AppWidgetManager.getAppWidgetIds` is non empty for
   both providers and `dumpsys alarm` (read via `UiAutomation.executeShellCommand`) contains
   `WIDGET_REFRESH`. `deleteAppWidgetId` for both in `@After`, then assert the alarm is gone.

**Verify.** `./gradlew :app:testDebugUnitTest` green, then
`adb shell appwidget grantbind --package com.sajdatime.app --user 0 && ./gradlew :app:connectedDebugAndroidTest`
green on the API 36 AVD. Then by hand on the emulator: place both widgets, `adb root`, set the clock to
one minute before Asr, watch the highlight move to Maghrib within a few seconds of Asr with the app not
running (`adb shell am force-stop com.sajdatime.app` first). Set the clock to 23:59 and watch the Today
widget roll to the next day at 00:00:02.

**Definition of done.** Every trigger in section 4.2 is hooked and the device test passes. A phone with no
widget placed shows no `WIDGET_REFRESH` alarm in `dumpsys alarm`.

### A8. Ramadan labels on the widgets

**Goal.** During Ramadan the Today widget shows "Suhoor ends" under Fajr and "Iftar" under Maghrib, and
the Next prayer widget shows the matching word when Fajr or Maghrib is next (`RAMADAN-LOCATION-SPEC.md`
section 8).

This is already wired in A6 through `ramadanToday` and `ramadanOnNextDay`. A8 is the verification task
and the only code is in tests: `WidgetStateTest` gains the three Ramadan cases (day 15, the eve at
23:00, and the last day after Maghrib where `ramadanOnNextDay` is false so tomorrow's Fajr carries no
suhoor word). The device test (A7) gains one run with the clock at 2027-02-22 asserting the two label
views are visible with the right text.

**Verify.** The two tests above pass. Screenshot both widgets at 2027-02-22 in English and Urdu.

### A9. Strings for Release A in all four languages

**Goal.** Every new key in `WORDING-AND-LISTING.md` section B (B1 Ramadan, B2 reminder, B3 widgets)
exists in `values/`, `values-ur/`, `values-tr/`, `values-in/` with the texts given there, in the same
order and under the same comment headings, and nothing user facing is in Kotlin.

Rules that already exist and apply: no em dashes, no semicolons, UK spelling, plurals as `<plurals>`,
placeholders numbered, translation files keep their header comment and `app_language_tag`, and the
translation is written and checked by Claude with no native speaker review and the app never says
otherwise (`CLAUDE.md`).

*Tests.* `RamadanWordingTest` (app, reads `values/strings.xml` off disk, declared as a task input
already at `app/build.gradle.kts:217`): asserts the Ramadan info note contains the four phrases
"as calculated", "does not add", "follow your mosque" and "Islamic date" (the A11 commitments), that
no new string contains "imsak" as a time the app provides (the word may appear only in the sentence
saying timetables print one), that no string contains "you may" or "you must" (no rulings), and that
`widget_next_desc` mentions following the phone's dark mode. `LocaleDisciplineTest` and
`LanguagePickerTest` stay green. `./gradlew :app:lintDebug` reports no `MissingTranslation`.

**Verify.** `./gradlew :app:testDebugUnitTest :app:lintDebug` clean. Run the four language screenshots the
release script already takes (`docs/release-tools/release-1.3.2.sh:124-128`) with the clock at
2027-02-22 so the card is on screen in each language.

### A10. Documents, listing, privacy, version bump, release gate, device suite

**Goal.** Everything a reader, a user or Google sees says what the app does, and the release is built
and verified by the script the owner runs.

1. `app/build.gradle.kts:49-50`: `versionCode = 8`, `versionName = "1.4.0"`, with the dated comment
   in the house style. `:wear` untouched.
2. `docs/HANDOVER.md`: the recording list in `TEST-AND-RECORD.md` section 6 (STATE OF PLAY rewritten,
   section 4 feature list, new 5.19 Ramadan and 5.20 widgets, section 6 alarm actions and codes, section
   8 unchanged but stated, section 9 test table with the new suites and the real count, section 11
   "Not done" item 8 "No home screen widget" removed and the A11 entry closed, section 15 lessons).
3. `docs/ARCHITECTURE.md`: the "No home screen widget" line at `docs/ARCHITECTURE.md:571` replaced, the
   file map gains `widget/` and `HijriCalendar.kt`.
4. `README.md`: feature lines for the widgets and the Ramadan pack (`WORDING-AND-LISTING.md` section C6)
   and the three corrections in section A, items R1 to R3.
5. `docs/index.html`: the same two feature lines and the languages line.
6. `docs/store/LISTING.md` and `docs/store/LISTING-LOCALES.md`: the Release A text from
   `WORDING-AND-LISTING.md` section C, and the stale notes corrected (section A, items L1 to L8).
7. `docs/privacy.html`: the optional widget sentence from `WORDING-AND-LISTING.md` section C5 and the
   date line. No data handling changes in A, so this is a clarity edit, not a policy change.
8. `docs/DESIGN_SYSTEM.md`: the widget colour tokens (section 1) and the Ramadan card (section 4).
9. `docs/RELEASING.md:1103`: the phone version line (it still says 4).
10. `docs/release-tools/release-1.4.0.sh`: a copy of `release-1.3.2.sh` with `BRANCH`, `OUT`, the
    expected versionCode 8, `DEST` pointing at the repo root `SajdaTime-1.4.0/` as the header comment
    asks, and step 6 extended with the widget bind grant, the clock set to 2027-02-22 for the Ramadan
    screenshots, both widgets placed and captured, and the clock restored by killing the emulator the
    script started (`-no-snapshot-save` is already passed). Exact commands in `TEST-AND-RECORD.md`
    section 3.
11. `docs/store/screenshots/`: retake `03` as the Ramadan Times screen (the listing file already says
    `03` duplicates `01` and should become the Ramadan framing, `docs/store/LISTING.md:484-491`), and add
    a sixth phone screenshot of the Today widget on the home screen. `tools/build-store-assets.sh` is run
    after.

**Verify.** The full gate from `CLAUDE.md`:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

then `docs/release-tools/release-1.4.0.sh` run by the owner on his Mac, with every step `PASS`, the
bundle signed by his key, versionCode 8, every language inside, the device suite green, the screenshots
in `build/release-1.4.0/`. Then the checks in `TEST-AND-RECORD.md` section 4 (permissions diff empty,
dependency diff empty, bundle size within budget, no analytics event added).

### A11. Release A: what only the owner does

In order, from `TEST-AND-RECORD.md` section 7: review and merge the pull request on GitHub (the privacy
page and listing text are served from `main`, lesson 124, so merge before submitting), paste the Release A
store text and the What's new text into Play Console one screen at a time with Claude in Chrome running the
prompt from lesson 128, upload `SajdaTime-1.4.0/app-release.aab` only (never the watch bundle), press
"Submit N changes for review". Afterwards Claude re baselines the daily health check to 1.4.0 and the new
"Updated on" date, and records the live state in `docs/HANDOVER.md` STATE OF PLAY.

## 7. Release B: the prayer tracker, phase A (phone 1.5.0, versionCode 9)

Branch: `claude/1-5-0-tracker`, from `claude/quiet-glass` after Release A is live and `main` has been
merged into it. One pull request. The privacy policy changes in this release and must be live on `main`
before the owner submits.

**1.5.0 also ships the Quiet Glass redesign (owner, 17 Oct 2026: "make sure the design system is applied
there also, which includes the widgets and other things").** The redesign, the research fixes, the rating
card and the Share row are on `claude/quiet-glass` (HANDOVER §11), built after this plan was written. So
every screen below is specified in Quiet Glass terms, not in the pre redesign components this plan first
named (`Group`, `SwitchRow`, `SettingRow` and the old banner are gone). The rules that bind every B step:

- Read `docs/DESIGN_SYSTEM.md` §4, §8, §9, §10 first. Colours are Material 3 roles only, asserted in
  `ColorContrastTest`. Cards are 24dp `sajdaSurface`; inner tiles 12dp. Type: the one line height rule
  in `Type.kt`; titles `titleMedium`, body `bodyMedium`, actions `labelLarge` (SemiBold, so bold).
  Sentence case written into the strings. No hyphens or em dashes in copy.
- Any message is a `Notice` (WARNING or QUIET), never a new card shape. Any link is a bold action with
  an arrow (`NoticeAction` style). Every screen ends with `BottomBarClearance()`.
- Accessibility as built in the research pass: a row is one stop (`semantics(mergeDescendants = true)`),
  every on/off is `toggleable` (state spoken), every target 48dp, haptic `ToggleOn`/`ToggleOff` on ticks.
- Times must still fit with no scrolling on the 823dp S23 Ultra at default text. Measure it.
- Widgets: the tracker adds nothing to them. They already carry Quiet Glass colours (`widget_primary`
  `#0B5A43`, held by `WidgetColorsTest`) and the 12dp highlight; B0 re-renders both and B7 compares.
- Store screenshots: the one retake the owner agreed happens in B7, all five, with Quiet Glass.

### B0. Re capture the baselines

As A0, on the merged `main`. Record the numbers in the status table.

### B1. `TrackerLogic` in `:core`

**Goal.** Every rule about days, states and the streak as pure Kotlin with no Android, so the edge cases
in `TEST-AND-RECORD.md` section 2.3 are unit tested.

*New file* `core/src/main/java/com/sajdatime/core/TrackerLogic.kt`:

```kotlin
package com.sajdatime.core

/** The five prayers in tracker order. Sunrise is not a prayer and has no tick. */
val TRACKED_SLOTS: List<PrayerSlot> = PrayerSlot.entries.filter { it.isPrayer }

/** One day's record. Stored as a bitmask: bits 0 to 4 are TRACKED_SLOTS, bit 5 is excused. */
data class DayRecord(val ticked: Set<PrayerSlot> = emptySet(), val excused: Boolean = false) {
    val isComplete: Boolean get() = ticked.containsAll(TRACKED_SLOTS)
    fun toMask(): Int = TRACKED_SLOTS.foldIndexed(0) { i, acc, slot -> if (slot in ticked) acc or (1 shl i) else acc } or (if (excused) EXCUSED_BIT else 0)
    companion object {
        const val EXCUSED_BIT = 1 shl 5
        const val MAX_MASK = (1 shl 6) - 1
        fun fromMask(mask: Int): DayRecord {
            val m = mask and MAX_MASK            // anything a newer version wrote above bit 5 is ignored
            return DayRecord(
                ticked = TRACKED_SLOTS.filterIndexed { i, _ -> m and (1 shl i) != 0 }.toSet(),
                excused = m and EXCUSED_BIT != 0,
            )
        }
    }
}

/** What one tick target shows. Never "missed": the app records, it does not judge. */
enum class TickState { TICKED, NOT_TICKED, EXCUSED, NOT_DUE_YET }

object TrackerLogic {
    fun tickState(record: DayRecord?, slot: PrayerSlot, due: Boolean): TickState = when {
        record?.excused == true -> TickState.EXCUSED
        record?.ticked?.contains(slot) == true -> TickState.TICKED
        !due -> TickState.NOT_DUE_YET
        else -> TickState.NOT_TICKED
    }

    /**
     * Consecutive complete days ending today or yesterday. Today counts only if complete and
     * never breaks the run. Excused days are skipped and never break it. A day with no record
     * ends it, which is also how days before the tracker was switched on end it.
     */
    fun streak(records: Map<LocalDate, DayRecord>, today: LocalDate): Int {
        var count = 0
        records[today]?.let { if (it.isComplete && !it.excused) count++ }
        var day = today.minusDays(1)
        while (true) {
            val r = records[day] ?: break
            if (r.excused) { day = day.minusDays(1); continue }
            if (!r.isComplete) break
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** The 30 dates shown in history, newest first, ending today. */
    fun historyDates(today: LocalDate, days: Int = 30): List<LocalDate> = (0 until days).map { today.minusDays(it.toLong()) }
}
```

*Tests* `core/src/test/java/com/sajdatime/core/TrackerLogicTest.kt`, concrete inputs and outputs:

| Case | Records | Today | Expected streak |
|---|---|---|---|
| nothing recorded | {} | 2027-02-20 | 0 |
| today complete only | {20: all five} | 20 | 1 |
| today incomplete, yesterday complete | {19: all, 20: Fajr} | 20 | 1 |
| three complete days | {17, 18, 19 all} | 20 (no record) | 3 |
| excused in the middle bridges | {17 all, 18 excused, 19 all} | 20 | 2 |
| excused yesterday, complete before | {18 all, 19 excused} | 20 | 1 |
| only excused days | {18 excused, 19 excused} | 20 | 0 |
| an incomplete day stops it | {16 all, 17 Fajr only, 18 all, 19 all} | 20 | 2 |
| a gap stops it | {16 all, 18 all, 19 all} | 20 | 2 |
| year boundary | {2026-12-30 all, 2026-12-31 all, 2027-01-01 all} | 2027-01-02 | 3 |
| leap day | {2028-02-28 all, 2028-02-29 all, 2028-03-01 all} | 2028-03-02 | 3 |
| today excused does not count and does not break | {19 all, 20 excused} | 20 | 1 |
| DST change day | {2027-03-27 all, 2027-03-28 all} | 2027-03-29 | 2 (dates only, no instants, so nothing to distort) |
| mask round trip | every mask 0..63 | | `fromMask(toMask(r)) == r` and `fromMask(x).toMask() == x and 63` |
| mask above 63 is masked | 0b1_111111 | | same as 63 |
| tick states | record null, due false | | NOT_DUE_YET, and excused wins over ticked |

**Verify.** `./gradlew :core:testDebugUnitTest --tests "*TrackerLogicTest*"` green.

### B2. `TrackerRepository` on its own DataStore, and the file codec

*New file* `app/data/TrackerRepository.kt`:

```kotlin
private val Context.trackerStore: DataStore<Preferences> by preferencesDataStore(name = "sajdatime_tracker")

class TrackerRepository(private val context: Context) {
    val records: Flow<Map<LocalDate, DayRecord>>            // decoded from every d:<date> key
    val readOnly: Flow<Boolean>                               // tracker_version > VERSION
    suspend fun current(): Map<LocalDate, DayRecord>
    suspend fun setTicked(date: LocalDate, slot: PrayerSlot, ticked: Boolean)
    suspend fun setExcused(date: LocalDate, excused: Boolean)   // excused keeps the ticks already there
    suspend fun merge(incoming: Map<LocalDate, DayRecord>): MergeResult   // OR ticks and excused, never removes
    suspend fun deleteAll()
    companion object { const val VERSION = 1 }
}
data class MergeResult(val daysAdded: Int, val daysUpdated: Int)
```

Every write first ensures `tracker_version` is set to 1 when absent and refuses (no op, returns) when
it is greater than 1. Keys are `d:` plus `LocalDate.toString()` (ISO). Decoding ignores malformed keys.

*New file* `app/data/TrackerFileCodec.kt` (app module, because `org.json` is platform at runtime and on
the unit test classpath already, F33):

```kotlin
object TrackerFileCodec {
    const val KIND = "sajdatime-prayer-tracker"
    const val VERSION = 1
    fun encode(records: Map<LocalDate, DayRecord>, exportedOn: LocalDate): String   // pretty JSON
    fun decode(text: String): Map<LocalDate, DayRecord>   // throws TrackerFileException(reasonRes) on kind mismatch, version > 1, or malformed
    fun fileName(exportedOn: LocalDate): String = "sajdatime-tracker-$exportedOn.json"
}
```

File shape:

```json
{
  "kind": "sajdatime-prayer-tracker",
  "version": 1,
  "exported": "2027-02-20",
  "days": {
    "2027-02-08": { "ticked": ["FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA"], "excused": false },
    "2027-02-09": { "ticked": [], "excused": true }
  }
}
```

Names, not bitmasks, so the file is readable by its owner. Unknown slot names are dropped entry by entry,
as `AlertCodec.decode` does (F8). No location, no settings, nothing but dates and ticks.

*Settings keys* (section 3.2): `tracker_enabled`, `tracker_show_streak` on `AppSettings` with setters.

*Tests.* `TrackerFileCodecTest`: round trip, newer version refused with the right reason, wrong kind
refused, malformed JSON refused, unknown slot dropped, excused with no ticks preserved, file name
format. `TrackerPrivacyTest`: reads `data_extraction_rules.xml` off disk (add to the task inputs block)
and asserts both `cloud-backup` and `device-transfer` exclude `domain="file"`, and reads
`UsageCounts.kt` to assert no `tracker` word appears. `TrackerRepository` itself is exercised by the
device test in B7 (DataStore needs a real file system).

**Verify.** `./gradlew :app:testDebugUnitTest --tests "*Tracker*"` green.

### B3. Settings: the tracker group

A new `SettingsSection(SettingsSectionId.TRACKER, stringResource(R.string.settings_group_tracker), items)`
between REMINDERS and ABOUT, drawn by `SettingsList` like every other section (Quiet Glass: inset 24dp
card, 36dp green icon tiles, hairlines inset to the text). Add `TRACKER` to `SettingsSectionId` and the
icon keys to `iconFor` in `SettingsList.kt`:

- `SettingsItem.Toggle(key = "tracker", title = settings_tracker, subtitle = settings_tracker_desc, checked = settings.trackerEnabled)`, icon `Icons.Outlined.TaskAlt`.
- When enabled: `SettingsItem.Toggle(key = "tracker_streak", …settings_tracker_streak…)` (icon
  `Outlined.LocalFireDepartment` is rejected as gamey: use `Outlined.CalendarMonth`),
  `SettingsItem.Link(key = "tracker_save", …, onClick = onSaveTracker)` (`Outlined.FileDownload`),
  `SettingsItem.Link(key = "tracker_load", …, onClick = onLoadTracker)` (`Outlined.FileUpload`),
  `SettingsItem.Link(key = "tracker_delete", …, onClick = { confirmDelete = true })` (`Outlined.DeleteOutline`),
  with the app's `AlertDialog` (`tracker_delete_title`, `tracker_delete_body`, `action_delete`, `action_close`),
  the confirm button in `error` colour as the only destructive action in the app.
- When disabled but records exist (`state.trackerHasData`): only the delete row stays visible under the
  switch, so data can always be removed.
- Toggles inherit `toggleable` and the ToggleOn/Off haptic from `PlainRow`; extend
  `SettingsSemanticsDeviceTest` with the tracker switch.

`MainScaffold` and `MainActivity` gain the three callbacks (`onSaveTracker`, `onLoadTracker`,
`onDeleteTracker`). The save and load launchers live in `MainActivity` (B5).

**Verify.** `./gradlew :app:assembleDebug :app:lintDebug` clean, rows visible on the emulator.

### B4. Ticks on the Times screen and the history sheet

*ViewModel.* `UiState` gains `tracker: TrackerState? = null` built when `settings.trackerEnabled`:

```kotlin
data class TrackerState(
    val records: Map<LocalDate, DayRecord>,
    val today: LocalDate,
    val streak: Int,
    val readOnly: Boolean,
) {
    fun state(date: LocalDate, slot: PrayerSlot, due: Boolean) = TrackerLogic.tickState(records[date], slot, due)
}
```

The ViewModel collects `TrackerRepository.records` only while enabled (a `flatMapLatest` on
`settings.trackerEnabled`). `fun tick(date, slot, ticked)`, `fun setExcused(date, excused)`.

*Times screen (Quiet Glass).* `PrayerRow` gains `tick: TickState?` and `onTick: (() -> Unit)?`. When
non null, a `Tick` composable (new, `ui/components/Tick.kt`, the design system's tick component, added to
`DESIGN_SYSTEM.md` §4) sits at the row's end after the time: a 24dp mark in a 48dp `toggleable`
target (`Role.Checkbox`, haptic `ToggleOn`/`ToggleOff`). States: NOT_TICKED `Icons.Rounded.RadioButtonUnchecked`
in `outline`; TICKED `Icons.Rounded.CheckCircle` in `primary`; EXCUSED `Icons.Rounded.RemoveCircleOutline`
in `onSurfaceVariant`; NOT_DUE_YET the unchecked mark at 38 percent and disabled. The Next row's tint and
start bar are unchanged, so the tick never competes with "Next". The tick is its own screen reader stop
(a clickable child is a merge boundary inside the row's merged semantics): state from
`tracker_tick_a11y` ("%1$s, %2$s"). Sunrise has no tick. The row height does not change (the 48dp
target already fits the row), so the six rows still fit on 823dp; measure it.

The streak and the history link share ONE line under the Today heading, so the Times budget grows by one
line only when the tracker is on: start, the streak in `labelLarge` Medium `onSurfaceVariant` when
`trackerShowStreak` and above 0 (plurals `tracker_streak_days`, "%d day streak"; hidden at 0: no nag, no
zero); end, the bold link "Last 30 days" with the arrow (`tracker_open_history`, `NoticeAction` style,
48dp target). "Save as PDF" stays where it is. If the line pushes the mosque door off 823dp, the line
moves under the list instead; decide on the measurement, not by eye.

*History (Quiet Glass).* `TrackerHistorySheet`, a `ModalBottomSheet` (full height, like the Ramadan
timetable) in `app/ui/home/TrackerHistory.kt`: title `headlineSmall` start aligned; rows inside one 24dp
`sajdaSurface` card with hairlines inset, newest first, `TimeFormat.date` in `titleMedium` at the start,
five `Tick`s in `TRACKED_SLOTS` order (each spoken with its prayer), and the day control at the end as a
small filter style chip (`secondaryContainer` when on). Note: the README's "Decisions on Release B"
replaced Excused with a neutral Pause that freezes the streak; build Pause, in this same chip style. At 2x text the
row wraps the ticks under the date rather than shrinking targets. Future prayers on today's row follow
NOT_DUE_YET. Past days are fully editable. When `readOnly`, every control is disabled and
`tracker_newer_version` is shown at the top as a QUIET `Notice`.

*The post midnight Isha rule.* On the Times screen the Isha tick belongs to `state.today.date`, which is
the calendar day of the list. After midnight at a high latitude where yesterday's Isha is still the
current prayer, the tick for that Isha is reached through the history sheet on yesterday's row. The
Times screen shows today's Isha as NOT_DUE_YET. Documented in the strings' comments and in HANDOVER 5.21.

**Verify.** Unit tests from B1 cover the logic. On the emulator: enable the tracker, tick Fajr, kill the
app (`am force-stop`), reopen, the tick is still there. Rotate: still there. Open history, mark yesterday
excused, the streak line reflects B1's rules.

### B5. Save and load through the system file picker

*MainActivity.* Two launchers next to the existing ones (F26):

```kotlin
private val trackerSave = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
    uri?.let { viewModel.saveTrackerTo(it) }
}
private val trackerLoad = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    uri?.let { viewModel.loadTrackerFrom(it) }
}
```

Save launches with `TrackerFileCodec.fileName(LocalDate.now())`. Load launches with
`arrayOf("application/json", "text/plain", "application/octet-stream")` because some pickers label a
`.json` file as octet stream, and the codec validates content, not type.

*ViewModel.* `saveTrackerTo(uri)` writes `TrackerFileCodec.encode(repository.current(), today)` through
`contentResolver.openOutputStream(uri, "wt")` on `Dispatchers.IO` and emits
`TrackerEvent.Saved(fileName)`. `loadTrackerFrom(uri)` reads up to 2 MB, decodes, merges, and emits
`TrackerEvent.Loaded(daysAdded, daysUpdated)` or `TrackerEvent.Failed(reasonRes)`. Delivered like
`ExportEvent` (F21, F26): one toast, then consumed. Strings `tracker_saved`, `tracker_loaded` (plurals),
`tracker_load_failed_newer`, `tracker_load_failed_format`, `tracker_save_failed`.

No share sheet, no storage permission, no network: the picker returns a `content://` URI with the grant
Android attaches to the result, and nothing is persisted beyond the write or read.

*Tests.* The codec (B2). A device test in B7 writes to a file under the app's cache through
`FileProvider` is not needed: use `context.cacheDir` with a `file://` to `content://` round trip is
overkill, so the device test calls `TrackerFileCodec` round trip through a real `ContentResolver` on a
`MediaStore` Downloads entry on API 29 and above (the same path `PrayerPdfExporter.saveToDownloads` uses,
F24) and deletes it afterwards.

**Verify.** On the emulator: save, pick Downloads, see the file in Files, load it on a fresh install (clear
data), see "30 days added" and the history populated.

### B6. Strings for Release B in all four languages

As A9, keys from `WORDING-AND-LISTING.md` section B4. `TrackerWordingTest`: no string in the tracker set
contains "missed", "sin", "reward", "lazy", or "shame", none asks why a day is excused, and the Settings
subtitle says the data stays on this phone.

### B7. Privacy policy, documents, version bump, release gate, device suite

1. `docs/privacy.html`: the new section from `WORDING-AND-LISTING.md` section C5 (required: it is a new
   category of stored data), the "What SajdaTime never does" list updated, the date line, and the
   `#tracker` anchor. `UsageCountsTest` and `DisclaimerContentTest` read this file and must stay green.
2. `versionCode = 9`, `versionName = "1.5.0"`.
3. `docs/HANDOVER.md`: STATE OF PLAY, section 4, new 5.21, section 8 privacy table row ("Prayer tracker
   ticks and excused days, DataStore on device, never"), section 9 table, section 11 non goals (the
   "no prayer tracker" sentence in the design system paragraph at `docs/HANDOVER.md:6533-6537` updated
   to say phase A shipped and phase B is not agreed), section 15 lessons.
4. `README.md`, `docs/index.html`, `docs/store/LISTING.md`, `docs/store/LISTING-LOCALES.md`,
   `docs/ARCHITECTURE.md` (privacy model and file map), `docs/DESIGN_SYSTEM.md` (the tick component in
   §4, a decision log line in §9, the §10 review questions answered for the tracker screens).
5. `docs/release-tools/release-1.5.0.sh` from the 1.4.0 script, with the tracker device test and a
   screenshot of the Times screen with ticks.
6. `docs/store/screenshots/`: the agreed one time retake of all five, in Quiet Glass (owner, 17 Oct
   2026), one of them Times with the tracker on, plus the Today widget on a launcher. No text in the
   screenshots, so one set serves every language.

**Verify.** The full gate, the script run by the owner, `TEST-AND-RECORD.md` section 4 checks, and
`grep -n "tracker" docs/privacy.html` shows the section before the pull request is opened.

### B8. Release B: what only the owner does

As A11, plus: the privacy policy must be live at the GitHub Pages URL and show "Prayer tracker" before he
presses submit (lesson 124), and the Data safety form needs no change because nothing new leaves the
device (`WORDING-AND-LISTING.md` section C5 explains why, so he can answer a reviewer).

## 8. Risk register

| # | Risk | Likelihood | Impact | Mitigation | Owner |
|---|---|---|---|---|---|
| R1 | A scheduler change breaks a prayer alarm (missed adhan) | Low | Severe | Separate action and request code band, `RequestCodeTest`, existing alarm tests unmodified, the `dumpsys alarm` before and after check in `TEST-AND-RECORD.md` 3.3, no change to the three rungs | Implementer |
| R2 | A `RemoteViews` call is not remotable on some launcher ("Problem loading widget") | Low to medium | Medium (widget only) | Only documented remotable calls, the `AppWidgetHost` render in `WidgetDeviceTest`, the fallback of dropping `setLayoutDirection` | Implementer |
| R3 | Users whose country starts Ramadan a day later than Umm al-Qura see day 1 a day early | Certain for some users | Medium | The eve notice with a direct door to the date shift, the info note, the card never says "you must". No country table (`RAMADAN-LOCATION-SPEC.md` 6) | Product |
| R4 | Iftar shown earlier than an official national timetable that adds precaution minutes (observed: Aladhan's Diyanet output is 7 minutes later at Maghrib than the plain angle the app uses) | Medium in Turkey, possible elsewhere | High during Ramadan (breaking a fast early) | The info note and the PDF note say follow your mosque, "Match your mosque" adjustments apply to the card, and decision 3 in `README.md` asks the owner whether to verify Diyanet's own table before Ramadan | Owner decision |
| R5 | `desugar_jdk_libs` carries a different Umm al-Qura table from JDK 21 | Low | High (wrong day numbers) | `RamadanDeviceTest` asserts 8 Feb 2027 is 1 Ramadan 1448 on the device | Implementer |
| R6 | Claude's Urdu, Turkish, Indonesian wording for suhoor, iftar, imsak is unidiomatic | Medium | Low to medium | Flagged in every file, Turkish and Indonesian chose the local words (`WORDING-AND-LISTING.md` B5), corrections on user report as `CLAUDE.md` says | Owner accepts |
| R7 | Ramadan deadline missed by Play review | Low | High | Pull request by 10 January 2027, the pack before the widgets in the task order, 1.4.0 can ship without widgets | Owner |
| R8 | The widget alarm or `LOCALE_CHANGED` adds battery or wake cost | Low | Low | Non waking `RTC` alarms, no `updatePeriodMillis`, nothing scheduled without a placed widget | Implementer |
| R9 | Tracker data loss (the exact competitor failure) | Low | High for the user | Own DataStore file, never pruned, save to a user owned file, excluded from backup by the existing rules so a restore cannot bring an older copy over a newer one | Implementer |
| R10 | Streak anxiety or judgemental wording | Low | Reputational | Off by default, streak can be hidden, `TrackerWordingTest`, no "missed" | Product |
| R11 | Times screen no longer fits above the fold during Ramadan on short phones | Certain during Ramadan | Low | The column scrolls, the mosque door is still reachable, recorded in the budget comment | Accepted |
| R12 | Chronometer digits follow the phone language, not the app's | Certain on mixed language phones | Low | Documented, nothing else on the widget is affected | Accepted |
| R13 | `exported="false"` on the widget providers blocks updates on some device | Low | Medium | The device test and a one line flip | Implementer |
| R14 | A new string lands in Kotlin or a translation is missing | Low | Low | Lint `MissingTranslation`, `LocaleDisciplineTest`, the wording tests | Implementer |

## 9. Rollback notes

- **Release A as a whole:** Play does not allow rolling back to an older bundle. The rollback is a new
  versionCode 9 built from `main` with the feature commits reverted. Keep the A1 commit (shared
  `HijriCalendar`) because it changes no behaviour and `AdjustmentTest` and `PrayerEngineTest` prove it.
- **Widgets only:** revert A6 to A8 and remove the two receivers and the refresh receiver from the manifest.
  Existing widget instances disappear from the home screen on update, which Android handles. No data to
  migrate.
- **Suhoor reminder only:** revert A5b and A5a. The two settings keys become unread and harmless, like the
  two legacy alert keys already are (F8). Any pending `SUHOOR_REMINDER` alarm is cancelled on the next
  `reschedule` only if the cancel loop stays, so if A5b is reverted, keep the cancel loop for one release
  or accept that a stale reminder may fire once on the first day after the update (it would post nothing,
  because the receiver branch is gone).
- **Ramadan card only:** revert A2 to A4. The engine and header keep `HijriCalendar`.
- **Tracker:** revert B1 to B6 and the privacy section. The `sajdatime_tracker` DataStore file stays on
  users' phones, unread, until uninstall, which the policy text for B must allow for ("if a future version
  removes the tracker, the file stays on your phone until you delete it or uninstall").
- **Hotfix path during Ramadan:** the same release script with a new version code. Play reviews of this
  app have taken one to two days (`docs/HANDOVER.md` STATE OF PLAY).

## 10. Definition of done

**Per task:** the Verify block passed, the status table row updated in the same commit, the commit
message carries the reasoning, nothing user facing is in Kotlin, no new dependency, no new permission,
no new analytics event, the existing tests unmodified and green.

**Release A:** all of A0 to A10 done. The gate green with `--rerun-tasks`. The device suite green on the
API 36 AVD including `WidgetDeviceTest` and `RamadanDeviceTest`. Both widgets shown in the picker and on
the home screen in light, dark, English and Urdu. The Ramadan card, eve notice, timetable sheet and PDF
verified with the emulator clock on 2027-02-06, 2027-02-22 and 2027-03-08. The suhoor reminder visible
in `dumpsys alarm` only when on and only on Ramadan days. `permissions.txt` and `deps.txt` unchanged from
A0. Bundle size within 300 KB of A0. `docs/HANDOVER.md`, `README.md`, `docs/index.html`, the two listing
files, `docs/privacy.html`, `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/RELEASING.md` updated
in the same pull request. Store text in four languages within Play's limits. The owner merged, pasted,
uploaded and submitted. Live confirmed on the raw public page and the health check re baselined.

**Release B:** all of B0 to B7 done, the same gates, `TrackerDeviceTest` green, the privacy policy live
with the tracker section before submission, bundle size within 200 KB of B0, no new permission, the
tracker off by default on a fresh install and on an upgrade, the file round trip verified on the
emulator, the owner merged and submitted, live confirmed, health check re baselined.
