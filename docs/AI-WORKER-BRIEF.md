# SajdaTime: brief for a helper AI (DeepSeek or any non-Claude model)

Written 25 Oct 2026 at the owner's request: "write all the notes for other AI to read and understand everything. I want
to use deepseek for less thinking tasks." Read this file in full before you touch anything. It is self-contained for
routine work. For the reasoning behind a rule, the full record is `docs/HANDOVER.md` (about 11,000 lines: search it,
do not read it top to bottom) and `CLAUDE.md`.

**Your role in one line:** you do routine, checkable work from a written instruction. Claude decides, designs, writes
anything a user reads, and checks your work (Claude Opus reviews every change before it reaches a release branch). If a
task needs a decision that the instruction does not already make, stop and say so **to Claude, never to Ali**. Never
ask Ali to test anything or to do a step for you.

---

## 1. What the project is

- **SajdaTime**: a free Android app for Muslim prayer times, adhan alerts, Qibla direction, a private prayer tracker,
  Ramadan times and widgets, plus a Wear OS watch app. Package `com.sajdatime.app`. On Google Play since Aug 2026.
- **Owner: Ali.** Not technical. He gives it as an ongoing charity (*sadaqah jariyah*): no ads, no accounts, no
  payments, no server of our own, never a business. Works for "the masses": no signal, old phones, users who never
  change a setting, users who cannot read English.
- **Dependable beats elegant.** Prayer times are worked out on the phone with no network, ever.
- **Languages in the app:** English, Arabic, Urdu, Turkish, Indonesian. Arabic and Urdu read right to left.

## 2. Where things stand (25 Oct 2026)

- **Live on Play:** 1.5.0 (phone versionCode 9, watch versionCode 1002).
- **Next release: 1.6.0** (phone versionCode 10; the watch is not uploaded again). Built and approved on branch
  `claude/1-6-0`. Pull request into `main` by 11 Dec 2026, Ali presses "Send for review" by 15 Dec 2026, before
  Ramadan (starts about 8 Feb 2027). Release steps: `docs/release-tools/RELEASE-1.6.0.md`.
- **1.6.1, after Eid:** a jama'ah reminder and Sunnah fast reminders. Their rules are written and tested
  (`core/FastingDays.kt`, `app/.../notify/ExtraReminderPlans.kt`) but nothing schedules them yet. Do not wire them up.
- **The single source of truth for "what is true now"** is the first block (marked 📍) at the top of `docs/HANDOVER.md`
  section 11. Its title names the latest step; an older block titled "STATE OF PLAY, 24 Oct 2026" further down is
  history. If this file and that first block disagree, that block wins. Every later block is old record, never a task
  list.

## 3. Map of the code

Three Gradle modules:

| Module | What | Notes |
|---|---|---|
| `core/` | Plain Kotlin rules: prayer times (`PrayerEngine.kt`, on the `adhan` library), Qibla, Hijri dates, Islamic dates, tracker rules (`TrackerLogic.kt`), jama'ah, fasting days, sky periods | **No `android.*` imports** in new code (it must carry to an iPhone app later). Exceptions already there: `AppLocale.kt`, `PrayerLabels.kt` |
| `app/` | The phone app, Jetpack Compose, Material 3 | below |
| `wear/` | The Wear OS app and tile | do not change unless told |

Inside `app/src/main/java/com/sajdatime/app/`:

- `ui/home/HomeScreen.kt` the Times tab; `ui/qibla/QiblaScreen.kt`; `ui/prayers/MyPrayersScreen.kt` the tracker tab;
  `ui/settings/SettingsScreen.kt` (large, about 2,100 lines); `ui/onboarding/`.
- `ui/components/` shared pieces: `CtaButton.kt` (every action button), `Notice.kt`, `Tick.kt`, `DayRing.kt`, `Bell.kt`.
- `ui/theme/` colours (`Color.kt`), spacing (`Spacing.kt`, the `Space` tokens), type, motion.
- `ui/SajdaViewModel.kt` state and actions; `MainActivity.kt` binds actions to it.
- `data/` settings and tracker storage (DataStore), city lookup, usage counts.
- `notify/` alarms and notifications. **Highest risk area in the app. Never change it.**
- `widget/` home screen widgets. `pdf/` the timetable PDF.

Strings: `app/src/main/res/values/strings.xml` (English) and `values-ar`, `values-ur`, `values-tr`, `values-in`.
Indonesian is `values-in`, **not** `values-id` (Android's legacy code; `values-id` would never be used).

Docs you will meet:

| File | Is |
|---|---|
| `CLAUDE.md` | the hard rules (condensed in section 5 below) |
| `docs/HANDOVER.md` | the full record: §5 business rules, §10 what was verified, §11 state of play, §15 lessons |
| `docs/PROJECT_BRIEF.md` | one plain page for Ali |
| `docs/DESIGN_SYSTEM.md` | the visual rules ("Quiet Glass"); §4.1 lists every button in the app |
| `docs/privacy.html` | the published privacy policy, a promise to users |
| `docs/plans/2026-10-release-1.6.0/` | the 1.6 plans and step designs |
| `docs/store/` | Play listing text and store pictures |
| `.claude/agents/*.md` | Claude's own helper roles (yours sits beside `mechanic` and `implementer`) |

## 4. How to build and test

Use Java 21. Never move the project into `~/Documents`, `~/Desktop` or any iCloud folder (it breaks the build).

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew :core:test :app:testDebugUnitTest          # quick unit tests
./gradlew clean test lint :app:bundleRelease :wear:bundleRelease :app:compileDebugAndroidTestKotlin   # the full gate
```

Last full gate (25 Oct 2026): core 238 tests (1 skipped), app 323, wear 27, 0 failed, lint 0 errors. Since then
`DesignTokensTest` added 12 app tests (app 335, unit tests only rerun; design system in `docs/design-system/`). A change that
adds tests moves these numbers; `docs/release-tools/release-1.6.0.sh` holds them in `EXPECTED_COUNTS`.

**Device tests** run on an emulator with `am instrument`, never `connectedAndroidTest` (that uninstalls the app). Run
`adb devices` first: serials depend on which emulator started first, so check which one is `sajda` before using
`emulator-5554`. Install a fresh build first, or you test an old one:

```bash
adb -s emulator-5554 uninstall com.sajdatime.app.test; adb -s emulator-5554 uninstall com.sajdatime.app
ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug :app:installDebugAndroidTest
adb -s emulator-5554 root
adb -s emulator-5554 shell service call alarm 3 s16 Europe/London
adb -s emulator-5554 shell appwidget grantbind --package com.sajdatime.app --user 0
adb -s emulator-5554 shell am instrument -w -e notClass com.sajdatime.app.SuhoorReminderDeviceTest,com.sajdatime.app.ScreenSeedDeviceTest,com.sajdatime.app.TrackerSeedDeviceTest com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: `OK (39 tests)`. Emulators (AVD names): `sajda` for tests, `sajdastore` (1080x1920) for store pictures. The
full release check, which does all of this, is `docs/release-tools/release-1.6.0.sh` (Claude runs it). `./gradlew :app:installRtl` installs a build that runs right to left (judge only the mirroring, not
the English words).

**Never install on Ali's real phone yourself.** If ever told to: `ANDROID_SERIAL=<serial> ./gradlew installSideload`,
never `installDebug` (it would delete his Play copy).

## 5. Hard rules (breaking any of these is a serious fault)

1. **No network for prayer times.** Never call Aladhan or any web service from the app. (Comparing against Aladhan
   while developing is fine and encouraged.)
2. **Privacy.** Location is coarse and foreground only, and the app never sends coordinates to any server. Never write
   that location "never leaves the device": the town name comes from Android's `Geocoder`, which may ask Google over
   the network (`docs/privacy.html` says so). No new data may leave the phone. Any change to
   what is stored or sent must also change `docs/privacy.html` in the same commit. Cloud backup stays off.
3. **Usage counts** (`data/UsageCounts.kt`, Firebase): opt in, off by default. Never add an event, parameter or user
   property, never send sect, madhab, method, settings, city or coordinates. Never mention the tracker there.
4. **Signing key.** Never generate, read, print, copy or commit a keystore, `*.jks`, `*.keystore` or
   `keystore.properties`. Never open anything in `~/.gradle/` that has "keystore" in its name.
5. **Git.** Never push to `main`, never merge into `main` or into `claude/*` branches, never force push, never use bare
   `git stash` (the stash is shared between worktrees). Work on your own branch (section 7).
6. **Religious disclaimer** (Settings, About, first row): never remove, shorten or soften it, in any language. Its dua
   request stays the last paragraph and appears nowhere else.
7. **Translations are Claude's only** (owner's rule). You never write or change text in `values-ar`, `values-ur`,
   `values-tr` or `values-in`, nor the English text users read, nor store listing text. Copying a string Claude already
   wrote into the right file, exactly as given, is fine.
8. **User-facing words:** no hyphens used as dashes, no em or en dashes, no semicolons. In English tracker text never
   "missed", "excused", "qada", "sin", "reward", "lazy", "shame", "failed to pray", "broke", "lost", "owed" or
   "debt" (`TrackerWordingTest`). No rating or sharing prompts in our own words.
9. **Watch testing is finished.** Never ask Ali to test the watch.
10. **Never write "done", "all" or "every"** unless you ran the check and paste the count. Say what you did not test.

## 6. Code conventions (follow the code around you)

- **Spacing:** vertical gaps use `Space.Tight` (4dp), `Related` (8), `Paragraph` (12), `Group` (16), `Section` (24).
  Never a raw number for a `Spacer`, column spacing or screen top or bottom padding (`DesignRulesTest` fails the build).
- **Buttons:** every action is a `CtaButton` (solid green, white bold text), with `fillMaxWidth`, a weight or
  `compact = true`. A file with a new button must be added to the DESIGN_SYSTEM §4.1 inventory or the build fails.
  No two icon buttons side by side. Every dismissable dialog has a close X.
- **Colours:** Material 3 roles from the theme (`MaterialTheme.colorScheme.*`). Never a hex colour in a composable.
- **Touch targets** at least 48dp. Text must scale (check at 2x font size).
- **Right to left:** use start and end, never left and right. Arabic and Urdu must mirror correctly.
- **Languages:** anything that names a language or formats for one goes through `AppLocale` (`AppLocale.wrap` at the
  moment of use), because the user can pick a language in the app that differs from the phone's.
- **Strings:** every new key exists in all five folders with the same placeholders (`TranslationIntegrityTest`,
  `TrackerWordingTest` check this). Arabic plurals have six forms (zero, one, two, few, many, other).
- **Comments** explain why, at the density of the file you are in. Deliberate simplifications get a `ponytail:` comment
  naming the limit and the upgrade path.
- **Shortest working change.** No new library, no new abstraction, no "for later" code.
- **Settings screen rows** are data (`SettingsItem.Link`, `Toggle`), built in `SettingsScreen.kt`, drawn by
  `SettingsList.kt`. Follow an existing row exactly.

Tests that guard rules (if one fails, the change is wrong, not the test): `DesignRulesTest`, `ColorContrastTest`,
`TranslationIntegrityTest`, `TrackerWordingTest`, `TrackerPrivacyTest`, `UsageCountsTest`, `DisclaimerContentTest`,
`RequestCodeTest`, `LocaleDisciplineTest`, `RamadanWordingTest`, `EidWordingTest`. **Never weaken or delete a test to
make it pass.**

## 7. What you may do, and what you must hand back

**Good tasks for you:**

- Search and count: where a string key is used, which files have a button, string key parity across five languages,
  the DESIGN_SYSTEM §4.1 counts.
- Run the unit tests, the gate or the device suite and report the exact output and counts.
- A routine code change from a written spec that names the files and the behaviour (a Settings row like an existing
  one, a test for a rule already decided, a rename).
- Tidy docs mechanically: fix a broken link, renumber a list, update a count you measured. Never rewrite reasoning or
  a dated record.
- Rename or sort screenshot files; build contact sheets.

**Never yours (stop and hand back to Claude):**

- Anything in `notify/` (alarms, notifications, request codes) or `widget/` (it sets alarms too), `data/UsageCounts.kt`,
  `docs/privacy.html`, `AndroidManifest.xml` permissions, Gradle versions or dependencies, signing, release scripts.
- **Data logic:** how anything is stored, read, merged or deleted (`data/`, DataStore, `TrackerRepository`,
  `TrackerFileCodec`, settings), and the rules in `:core` (tracker, jama'ah, fasting days, Hijri date shift).
- Prayer time, Qibla or Hijri calculations; religious content; madhab or sect rules.
- Any wording a user reads, any translation, store listing text.
- Design or UX choices: what a screen shows, layout, icons, colours. (Ali decides visual things from screenshots.)
- Anything the instruction does not cover. Ask rather than guess.

**How to hand work back:**

1. Work on a branch named `deepseek/<short-task>` made from the branch you were told (usually `claude/1-6-0`). This
   branch is a scratch space: committing and pushing it is allowed so nothing is lost, but it is not part of the project
   until Claude has reviewed it.
2. Commit there (and push that branch only, if you can) with a message that says what was wrong or asked, what you changed, why this way, what you ran (paste
   the counts) and **what you did not test**. Do not merge it anywhere.
3. Report in this shape:

```
Task: <one line>
Branch: deepseek/<name>, commit <sha>
Changed: <files, one line each>
Ran: <command> -> <exact result, counts>
Not done or not tested: <list, or "none">
Questions: <anything you had to guess>
```

Claude (Opus) then reviews it and merges it into the release branch if it is right. Claude, not you, updates
`docs/HANDOVER.md`, `docs/PROJECT_BRIEF.md` and GBrain when it merges. You only update this brief's counts if told.

## 8. Traps that already cost the project time

- `values-id` is never loaded on Android; Indonesian is `values-in`.
- `connectedAndroidTest` uninstalls the app at the end: use `am instrument` (section 4).
- Widget device tests fail on a bind error unless `appwidget grantbind` ran first.
- The `sajdastore` emulator's GPS says Manchester: after seeding London for pictures, revoke location permission or the
  app moves to Manchester.
- A change that compiles is not a change that works: run it. Read the screenshot, not the code, for visual claims.
- Two "bugs" were really test mistakes: when a time disagrees, check the test and the reference as well as the code.
- iCloud folders break the build (conflict copies in `app/build/`).
- `TrackerRepository` refuses writes when the record comes from a newer app version (`readOnly`), on purpose.

## 9. Words you will meet

| Word | Means |
|---|---|
| Salah, namaz | the prayer; five a day: Fajr, Dhuhr, Asr, Maghrib, Isha (Sunrise is shown but is not a prayer) |
| Adhan | the call to prayer (the app can play one) |
| Qibla | the direction of the Kaaba in Makkah |
| Jama'ah, iqamah | the time the mosque prays together (typed in by the user) |
| Jumu'ah | the Friday prayer |
| Hijri | the Islamic lunar calendar; dates can be shifted a day by the user |
| Madhab | Sunni school (Hanafi, Shafi'i, Maliki, Hanbali); changes the Asr time |
| Sunni, Shia (Jafari) | the two traditions the app supports; Shia uses a different Maghrib rule |
| Suhoor, iftar | the meal before the fast starts (at Fajr) and the meal that ends it (at Maghrib) |
| Qada, kaza | making up a missed fast or prayer ("Fasts to make up" in the tracker) |
| Dua | a personal prayer or request |
| Quiet Glass | the app's design system name |

*Made with love, free for the Ummah.*
