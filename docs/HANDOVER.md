# SajdaTime — Handover

**Read this first.** It is written for someone (or some agent) picking the project up cold,
with no memory of how it got here. It covers the vision, the stack, every feature, every
business rule, what is verified, what is not, and what is left to do.

- **Repo:** `git@github.com:aliimrankhan86/SajdaTime.git`
- **Local path:** `/Users/aliimrankhan/Developer/SajdaTime`
- **Branch:** `main`
- **Version:** 1.1.0 (`versionCode` 2 on phone, **1000** on the watch — they share an
  `applicationId`, so codes must be unique across both modules; each has its own band so
  the two sequences can never collide. The watch also ships on a **separate Play release
  track**, not in the phone's release — see `docs/RELEASING.md` Step 7.)
- **Status:** feature-complete, tested, and prepared for Play. **Not shippable yet — the
  release is unsigned because only the owner may hold the key.** See §11.
- **Companion docs:** `docs/ARCHITECTURE.md` (the technical spec, including the exact
  business rules an iOS port must reproduce) and `README.md` (the short public-facing one).

> ⚠️ **The project used to live in `~/Documents`, which is iCloud-synced.** iCloud wrote
> conflict copies (`SomeClass 2.class`) into `app/build` and broke dexing three separate
> times. It now lives in `~/Developer`. **Do not move it back under `~/Documents`,
> `~/Desktop`, or any iCloud-synced folder.** `.gitignore` has a `*\ [0-9].*` rule as a
> second line of defence.

---

## 1. Product vision

SajdaTime is a **charity project — sadaqah jariyah**, built by **Ali Imran Khan**. It is
free forever, has no ads, no in-app purchases, no accounts, no analytics, and no tracking.
It is not a business and has no revenue model. That is the point, and it drives every
technical decision below.

Three constraints shaped the whole architecture:

| Constraint | Consequence |
|---|---|
| **Zero running cost** | Prayer times are computed on-device. There is no server and no paid API. The author must never receive a bill for someone else using the app. |
| **Privacy by design** | Coarse location only, foreground only, never transmitted, never backed up to the cloud. |
| **Solo maintainer** | Minimal dependencies, no codegen, no framework the author has to relearn in a year. |

### The tone

The user is not technical and asked for the app to be "fit for the masses". Wording is
plain and calm throughout — UK spelling, no jargon, no em dashes or semicolons in UI
strings. All 193 user-facing strings are resources, ready for translation, and nothing is
hardcoded in a composable: 146 in `app`, 27 in `wear`, and 20 in `core` (the prayer and
calculation-method names, which live there because both the phone and the watch show them).

### The religious disclaimer (non-negotiable)

The user was explicit about this. The app states plainly — **once after onboarding as a
dialog, and permanently in Settings → About → Disclaimer** — that:

- the author is **not a Mufti, Aalim, or an expert in Islam or fiqh**
- it was **built with the help of artificial intelligence**
- it **may contain errors**
- anything doubtful should be **checked with a local mosque or a qualified scholar**
- *"Use this app as a convenience, not as a definitive religious authority."*

Do not remove, soften, or bury this. It is a stated requirement, not decoration.

---

## 2. Tech stack

| Area | Choice | Version | Why |
|---|---|---|---|
| Language | Kotlin | 2.3.21 | — |
| Build | AGP | **9.3.1** | See the AGP 9 traps in §13. |
| Build | Gradle | 9.6.1 | |
| UI (phone) | Jetpack Compose + Material 3 | BOM 2026.06.01 | |
| UI (watch) | Wear Compose Material3 | 1.6.2 | Separate library from the phone's. |
| Tiles | Wear Tiles + ProtoLayout | 1.6.2 / 1.4.2 | |
| minSdk | 24 phone / 30 watch | | Android 7.0; Wear OS 3+. |
| targetSdk | 36, compiled against 37 | | AndroidX 1.19.0 forces compileSdk 37. |
| Calculation | `com.batoulapps.adhan:adhan` | 1.2.1 (MIT) | Offline, well tested, **zero transitive dependencies**. |
| Storage | DataStore Preferences | 1.2.1 | |
| Scheduling | AlarmManager + WorkManager | 2.11.2 | |
| PDF | Android `PdfDocument` | platform | No library, no licence, no APK weight. |
| Qibla | Own maths + `GeomagneticField` | platform | The platform already ships the World Magnetic Model. |
| Watch sync | `play-services-wearable` | 20.0.1 | The only way to move data phone↔watch. |
| Networking | `HttpURLConnection` + `org.json` | platform | One optional GET does not justify Retrofit/OkHttp/Moshi. |
| Java 8+ APIs | Core library desugaring | 2.1.5 | Gives `java.time` **including the Hijri calendar** down to API 24. |

### Deliberately NOT used — do not "improve" these back in

- **Google Play Services Location.** The platform `LocationManager` is enough for coarse
  accuracy, removes a proprietary dependency, and keeps the app working on de-Googled
  devices. Prayer times shift by well under a minute across a coarse-location cell.
  (`play-services-wearable` *is* used, but only on the watch, only for the Data Layer.)
- **Material You dynamic colour.** A wallpaper-derived palette would break the verified
  contrast guarantees and destroy the deliberate green/gold identity.
- **A DI framework.** Three repositories constructed in one ViewModel.
- **A navigation library.** Two screens plus onboarding. A boolean is enough.
- **Retrofit / OkHttp / Moshi / Gson.** One GET request.
- **The Aladhan API at runtime, in any form, including as a "fallback".** This one gets
  proposed by almost every fresh session, because Aladhan appears all over these docs as the
  reference we verify against — so read this before suggesting it again. The owner was asked
  directly and said no, and said he would say no if asked twice.

  He is right, and the reasoning is not merely about privacy:

  1. **There is no failure state to fall back *from*.** `adhan-java` is arithmetic over a
     date, two coordinates and an angle. It cannot time out, cannot rate-limit, cannot 500,
     and does not need a network. The only input that breaks it — no coordinates — breaks a
     network API identically. A fallback would be a parachute for a fall that cannot happen.
  2. **It would put the user's GPS coordinates on the wire to a third party**, on every
     calculation, for a whole month at a time when the PDF is exported. That directly
     contradicts `docs/privacy.html`, which promises coordinates never leave the device.
     Shipping it would make the published privacy policy a lie.
  3. **It would make the app worse offline**, which is the case it exists for: a phone in a
     mosque basement with no signal.
  4. **It would introduce non-determinism** into times that must be reproducible (§5.7), and
     a silent accuracy drift the day Aladhan changes a default.

  Verifying *against* Aladhan during development is correct and encouraged (§15 item 3).
  Calling it from the shipped app is not. These are not the same thing.

---

## 3. Module layout

```
:core   Prayer + Qibla calculation and the phone↔watch wire contract.
        NO android.* imports. Shared by phone and watch.
:app    The phone app.               applicationId com.sajdatime.app
:wear   The Wear OS app and tile.    applicationId com.sajdatime.app  (same, deliberately)
```

`:core` is an Android *library* module only so that AGP's built-in Kotlin support and core
library desugaring apply to it. The code inside is plain Kotlin and lifts straight into a
Kotlin Multiplatform or iOS target.

**Both apps share one `applicationId`.** That is the correct Wear OS setup — Play uses it
to pair the watch APK with the phone APK and delivers the right one per form factor. The
practical consequence during development: `./gradlew :app:installDebug` with two emulators
attached will install the **phone** APK onto the **watch**, silently replacing the watch
app. Always scope installs:

```bash
ANDROID_SERIAL=emulator-5556 ./gradlew :app:installDebug
ANDROID_SERIAL=emulator-5554 ./gradlew :wear:installDebug
```

### Complete file map

**`:core`** (6 source files, 5 test files, and `res/` — `values/strings.xml` for prayer and method names plus `app_language_tag`, `drawable/ic_kaaba*.xml` for the Qibla dial's Kaaba mark. Anything both screens show lives here, so it cannot be shown two different ways)

| File | Contains |
|---|---|
| `PrayerModels.kt` | `Sect`, `Madhab`, `CalcMethod` (14 methods), `PrayerSlot` (with `isPrayer`), `Coordinates`, `DayPrayerTimes` (with `prayersOnly`), `CalculationPrefs`, `NextPrayer`. **No display names** — see `PrayerLabels.kt`. This file has no `android.*` import and lifts straight into an iOS or KMP target. |
| `PrayerLabels.kt` | Display names for `PrayerSlot` and `CalcMethod`, as `labelRes` (for Compose) and `label(context)` (for notifications, the tile and the PDF). The one Android-touching file in `:core`; an iOS port supplies its own. |
| `PrayerEngine.kt` | **The heart of the app.** `compute`, `computeRange`, `nextPrayer`, `resolveMethod`. All the business rules in §5. |
| `QiblaEngine.kt` | `KAABA`, `bearingToKaaba`, `trueToMagnetic`, `relativeTurn`, `isAligned`, `distanceToKaabaKm`, `normalise` |
| `WatchSyncContract.kt` | The phone↔watch Data Layer path and keys, shared so the two modules cannot drift |
| `AppLocale.kt` | The language the app's *words* are in, read back out of the resources, and `wrap(context)` which pins a whole configuration to it. Every clock, date and number in both apps formats through this rather than through the device locale — §5.11. |

**`:app`** (24 source files, 2 test files)

| Area | Files |
|---|---|
| Entry | `MainActivity.kt` (ringtone picker, disclaimer dialog, PDF share), `ui/MainScaffold.kt` (bottom nav: Times / Qibla / Settings) |
| State | `ui/SajdaViewModel.kt` — the single ViewModel; `UiState`; 1-second clock; compass lifecycle |
| Data | `data/SettingsRepository.kt` (DataStore + `AppSettings` + `AlertStyle`), `data/LocationRepository.kt`, `data/CityLookup.kt`, `data/CompassRepository.kt`, `data/WatchSync.kt` |
| Notify | `notify/Notifications.kt` (channels), `notify/PrayerAlarmScheduler.kt`, `notify/PrayerAlarmReceiver.kt`, `notify/SystemEventReceiver.kt`, `notify/DailyRescheduleWorker.kt`, `notify/OngoingBadge.kt`, `notify/TimeFormat.kt` |
| Screens | `ui/home/HomeScreen.kt`, `ui/qibla/QiblaScreen.kt`, `ui/settings/SettingsScreen.kt`, `ui/onboarding/OnboardingScreen.kt`, `ui/components/Common.kt` (`LocationSheet`, `SectionHeading`, `rememberRemainingText`) |
| Theme | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` |
| Export | `pdf/PrayerPdfExporter.kt` |
| Manifest | `res/xml/data_extraction_rules.xml` — excludes everything from cloud backup and device transfer alike (both modules have one) |
| Build types | `src/rtl/res/values/strings.xml` — the only content of the `rtl` build type, which is `debug` with `app_language_tag` overridden to `ar-XB` so `./gradlew installRtl` runs the whole app right-to-left. `:wear` has the same. Never in `debug` or `release`, so it cannot ship; read the comment in the file before changing it |

**`:wear`** (8 source files, 2 test files)

| File | Contains |
|---|---|
| `WearMainActivity.kt` | Entry, requests location permission |
| `WearApp.kt` | `HorizontalPager`: page 0 times, page 1 Qibla; the "No location yet / Use Makkah" screen; the on-wrist school picker (`SchoolPage`, `ChoiceChip`) |
| `WearViewModel.kt` | Own `LocationManager`, own rotation-vector compass, `useDefaultLocation()`, `setSect`/`setMadhab` |
| `WearSettings.kt` | `WearSettings` + `WearSettingsStore` (the watch's own DataStore) |
| `SettingsSyncService.kt` | `WearableListenerService` receiving phone settings |
| `NextPrayerTileService.kt` | The tile |
| `TileFormat.kt` | Tile countdown wording + refresh interval (extracted to be testable) |
| `WearTheme.kt` | The watch colour scheme (the phone's dark palette) |

### The persisted data model

Phone: DataStore file `files/datastore/sajdatime.preferences_pb`.
Watch: its own store named `sajdatime_wear`.

```kotlin
data class AppSettings(
    val onboardingComplete: Boolean = false,
    val sect: Sect = Sect.SUNNI,
    val madhab: Madhab = Madhab.SHAFII,
    val method: CalcMethod = CalcMethod.AUTO,
    val coordinates: Coordinates? = null,
    val cityName: String = "",
    val notifyFor: Set<PrayerSlot> = <all five prayers>,  // absent slots default to on
    val ongoingBadge: Boolean = true,      // silent, costs nothing, so on by default
    val alertStyle: AlertStyle = AlertStyle.NOTIFICATION,   // ALARM is opt-in
    val alarmSoundUri: String = "",        // empty = device default alarm
    val disclaimerSeen: Boolean = false,
    val usingDefaultLocation: Boolean = false,  // true once Makkah was used as fallback
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,  // SYSTEM resolves to light
)
```

`PrayerSlot`: `FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA` — `isPrayer` is false for
`SUNRISE`, which is why it is displayed but never notified and never "next".

Enums are read back defensively (`enumValues<T>().firstOrNull { it.name == name } ?: fallback`)
so a renamed or removed constant in a future version degrades to the default instead of
crashing on launch.

**The Makkah fallback:** `useDefaultLocation()` writes the Kaaba's coordinates, the city
name "Makkah", and sets `usingDefaultLocation = true`. The home screen then shows a banner
saying so plainly, rather than presenting someone else's prayer times as the user's own.
Both phone and watch have this path.

### Visual identity

The launcher icon and notification icon are a **mihrab arch** (`ic_launcher_foreground.xml`,
`ic_notification.xml`, and `tile_preview.xml` on the watch) — all vector drawables, no
raster assets anywhere in the app itself. The Play Store icon and feature graphic in
`docs/store/` are the only rasters, and they are generated from those same path data by
`tools/build-store-assets.sh` rather than drawn by hand, so they cannot drift.

---

## 4. Features — the complete list

### Onboarding (first run only)
`Step` enum: `WELCOME → PERMISSION → SECT → MADHAB → CONFIRM`.

Welcome/bismillah → coarse-location permission with an **(i) info icon** explaining why it
is needed → Sunni/Shia cards → madhab (shown **only for Sunni**, and **skippable**; Shia
skips straight to confirm because Jafari fixes the Asr rule anyway) → confirmation →
finish. Then the one-time disclaimer dialog. A "Skip for now and use Makkah" escape exists
at the permission step.

### Times screen (home)
- Tappable location header showing city + **Hijri date**
- Centred hero card: "NEXT PRAYER", name and time on one line, large live countdown, "until it begins"
- Today's full timeline — Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha — with the next one highlighted
- Full-width "Save timetable as PDF" button
- Banners: Makkah-fallback notice, exact-alarm-permission notice

### Qibla screen
Rotating compass dial, tick marks every 15° (long every 90°), north wedge, Qibla needle,
fixed top reference dot, and a **Kaaba mark** at the far end of the needle. Bearing and
distance in km. Guidance text ("Turn right 118°" / "Facing Qibla") as a **polite
screen-reader live region**. Calibration prompt when sensor accuracy is low; a manual
bearing readout when there is no compass at all.

The Kaaba mark is the only part of the screen that does not require reading. Degrees need
a user who thinks in bearings; "Turn right 118°" needs a user who reads English, which is
every user until the app is translated by native speakers. The picture needs neither. It
is drawn on the watch too, from the same artwork — see §7.

### Settings
Reorganised from a flat list of ~25 controls into **four groups of tappable rows**, each
opening one focused chooser. Every row shows its current answer as its subtitle, so a
user's whole setup is readable without opening anything.

Anything the system is withholding — exact-alarm permission, Do Not Disturb access —
appears **above** the groups. Those are problems, not preferences, and inside a group
nobody found them.

| Group | Rows |
|---|---|
| **PRAYER TIMES** | Location (pin icon, city as subtitle) · School of thought ("Sunni · Hanafi") · Calculation method |
| **APPEARANCE** | Theme — three chips, Follow phone / Light / Dark. Chips rather than a chooser because the result is visible the instant it is tapped, and a dialog would sit on top of the change it just made |
| **REMINDERS** | How you are told (Notification / Alarm, with the sound picker and DND warning inside) · Which prayers ("All five" / "3 of 5") · Next-prayer badge switch, inline |
| **ABOUT** | Version · Disclaimer · Privacy (opens the hosted policy) · Free, forever · Made by · Data and libraries |

Any row that opens something carries a chevron, so "this is tappable" is never a guess.
Madhab sits inside the School chooser and is hidden for Shia users, since Jafari fixes the
Asr rule anyway.

**The dua request lives at the end of the disclaimer**, which is shown once automatically
after onboarding and is reachable any time from About. It appears nowhere else: asking
twice for the same thing is asking too often.

### Notifications
- **Default: notification + vibration + silent badge.** Alarm is **off** by default — this
  was an explicit user instruction.
- **Alarm mode is opt-in** and requires the user to pick their own tone. No adhan audio is
  bundled (licensing + APK size).
- **What actually carries an alarm through Do Not Disturb is `USAGE_ALARM` on the channel
  plus `CATEGORY_ALARM` on the notification**, not `setBypassDnd(true)`. That call is a
  no-op without notification policy access, which this app does not ask for: read the
  channel back and it says `mBypassDnd=false`. It is kept only because it costs nothing if
  the user ever grants the access. Verified on an API 36 emulator under
  `ZEN_MODE_IMPORTANT_INTERRUPTIONS`: the prayer alarm posted with `mIntercept=false` while
  the silent next-prayer badge was intercepted, which is the behaviour wanted for both.
- **The alarm survives Doze.** `setAlarmClock` in alarm mode, `setExactAndAllowWhileIdle`
  otherwise; both carry `FLAG_WAKE_FROM_IDLE`. Verified under forced deep idle: `dumpsys
  alarm` named SajdaTime as `Next wake from idle`, `whenElapsed` was not deferred, and
  crossing the trigger time posted "Time for Asr" and rolled the badge on to Maghrib.
- Optional silent ongoing "next prayer" badge in the shade.

### PDF export
`Range.TODAY` / `Range.NEXT_7_DAYS` / `Range.THIS_MONTH`. A4 at 72 dpi (595×842 pt) via
`PdfDocument`.

**Saved into the public Downloads folder** via `MediaStore` on API 29+, and the user is
told the filename. Below API 29 there is no Downloads collection to write to without
holding `WRITE_EXTERNAL_STORAGE`, so those devices fall back to a `FileProvider` share
sheet with `ClipData` set. A failure at any point raises a message — it used to fail
silently, which looked like the button doing nothing.

Names are `PrayerTimes_July2026.pdf` and `PrayerTimes_30Jul2026.pdf`, formatted with
`Locale.US` on purpose — this is the one string in the app that ignores the user's
language, because it is a file name in a shared folder and has to stay ASCII, sortable and
safe on the FAT-derived filesystems that USB and SD transfers still land on.

Verified on an API 36 emulator: exporting the month wrote `PrayerTimes_July2026.pdf` to
`MediaStore` `Download/`, with the columns Day / Date / Fajr / Sunrise / Dhuhr / Asr /
Maghrib / Isha, all 31 days, and 12-hour times matching the phone's locale.

### Wear OS
Standalone watch app (works with the phone off or unpaired) — two swipeable pages (times,
Qibla compass) — plus a **next-prayer tile** for the watch face carousel. Settings sync
one-way from the phone over the Data Layer.

Because that sync is unverified and a watch may have no phone app at all, the times list
ends in a button showing the school the times were calculated with, which opens a sect and
madhab picker. See item 7 under "Not done" for why that is not optional.

The times list opens with `initialCenterItemIndex = 0`. The default is 1, which centred
the second item and slid the countdown card up until the watch face clock sat on top of
the prayer name — the one thing the app exists to show.

---

## 5. Business rules — the exact specification

**This section is the contract. An iOS port must reproduce it exactly.** Fuller prose in
`docs/ARCHITECTURE.md` §3.

### 5.1 Method resolution
`CalcMethod.AUTO` resolves at calculation time from the sect: Sunni → Muslim World League,
Shia → Jafari (Ithna Ashari). An explicitly chosen method always wins.

### 5.2 Asr (madhab)
| Madhab | Shadow ratio | adhan value |
|---|---|---|
| Hanafi | 2× | `Madhab.HANAFI` |
| Shafi'i, Maliki, Hanbali | 1× | `Madhab.SHAFI` |
| Jafari | 1× | `Madhab.SHAFI` |

Three of the four Sunni madhabs therefore produce **identical** times. This is correct, not
a bug, and `DeterminismTest` asserts it.

### 5.3 Angles
| Method | Fajr | Maghrib | Isha |
|---|---|---|---|
| Muslim World League | 18° | sunset | 17° |
| Umm al-Qura | 18.5° | sunset | sunset + 90 min (**120 in Ramadan**) |
| Jafari | 16° | **4° below horizon** | 14° |
| Tehran | 17.7° | **4.5° below horizon** | 14° |

**Muslim World League also carries a +1 minute Dhuhr offset, and it is part of the method.**
adhan ships MWL with `methodAdjustments = (dhuhr = 1)`, so the app's Dhuhr is one minute
later than solar transit by definition. This is the single reason a careful user comparing
to Aladhan will find Dhuhr "wrong by a minute" — Aladhan's MWL does not apply the offset,
so it reports the transit itself. Both are defensible and adhan's is the safer direction,
since it can only ever move the time later, never before the sun has passed the zenith.

Do not "fix" it, and do not zero it out to make a reference comparison line up. Verified by
running adhan-java directly: with the adjustment, Dhuhr is 13:16 for Manchester on 31 July
2026; with `methodAdjustments` zeroed it is 13:15, which is exactly Aladhan's answer. An
iOS port on adhan-swift inherits the same offset and needs no special handling; a port onto
any *other* library must add it explicitly or every Dhuhr in the app will silently shift.

**These angles are a choice, not a fact, and at UK latitudes the choice dominates.** MWL's
17° Isha is correctly implemented and matches Aladhan's MWL exactly — and is still 78 minutes
later than the three Slough and Reading mosques measured in §10, "Isha in the UK", all of
which use the *shafaq*-based Moonsighting convention. Do not treat a user's "Isha is wrong"
as an arithmetic bug until their mosque's convention has been identified.

### 5.4 ⭐ The Jafari Maghrib rule — the single most important piece of logic
For Sunni conventions Maghrib is sunset. For **Jafari and Tehran**, Maghrib is when the
redness leaves the eastern sky — the sun 4° (4.5° Tehran) below the horizon, typically
**15–20 minutes after sunset**. This is the moment fasts are broken, so it is not cosmetic.

**adhan exposes no Maghrib angle.** Neither `adhan-java` nor `adhan-swift` models one.

Rather than hand-rolling solar astronomy, the engine reuses adhan's own validated maths:
adhan's Isha calculation is exactly "the evening time at N degrees of solar depression". So
it runs a **second** `PrayerTimes` with `ishaAngle` set to the Maghrib angle and reads the
resulting `.isha`:

```kotlin
val probe = CalculationParameters(0.0, 4.0, CalculationMethod.OTHER)
val jafariMaghrib = PrayerTimes(coords, date, probe).isha
```

Verified to the minute against Aladhan's independent Shia Ithna-Ashari implementation — all
six times matched for Tehran on 21 June 2026, Maghrib 19:42 against a sunset of 19:24.
**An iOS port using adhan-swift must reproduce this.**

### 5.5 High latitude rule
Uses **`HighLatitudeRule.TWILIGHT_ANGLE`**, set explicitly, never left to the library default.

adhan's own default `MIDDLE_OF_THE_NIGHT` is **unusable at UK latitudes**: for London on
21 June it collapses Fajr and Isha onto the *same instant* (both 01:02), leaving no Isha
window at all. `TWILIGHT_ANGLE` gives Fajr 02:31 and Isha 23:27, matching Aladhan exactly.

### 5.6 Ramadan (Umm al-Qura only)
Umm al-Qura stretches Isha from 90 to 120 minutes during Ramadan; adhan implements only 90,
so the engine adds 30 minutes itself. Ramadan is detected as Hijri month 9 via
`java.time.chrono.HijrahDate` — and `HijrahChronology` *is* the Umm al-Qura calendar, so
the two agree by construction. Applies to no other method.

### 5.7 Determinism
adhan rounds to the minute but leaves the **millisecond field carrying the wall-clock time
of the call**, so two identical computations return instants milliseconds apart. The engine
truncates every result to the minute (`truncatedTo(ChronoUnit.MINUTES)`). Without this the
countdown jitters and alarms land at an arbitrary point within their minute.

### 5.8 Next prayer
`nextPrayer` scans **yesterday, today and tomorrow**, not just today. At high latitudes
Isha can fall after midnight and therefore belongs to the *previous* calendar day's
timetable while still being genuinely upcoming. Scanning today alone silently skips it.
The alarm scheduler applies the same rule (`upcoming()` starts at `today.minusDays(1)`).

Sunrise is displayed but is **never** returned as "the next prayer" and is never notified.

**The day list and the next prayer roll over on two different triggers, and both are
needed.** The live clock recomputes when the next prayer passes *and* when the calendar
date changes. They are not the same moment: between midnight and Fajr no prayer passes, so
the prayer trigger alone left the "Today" list on the previous day's times for hours. See
§10 for what that looked like on a running device.

### 5.8a Current prayer — the one you can still pray
`PrayerEngine.currentPrayer` answers "which prayer's time is in right now", which is a
different question from §5.8's "what is next" and is shown by a separate **"Now"** marker.

| Now is in | Current prayer |
|---|---|
| Fajr → sunrise | Fajr |
| **sunrise → Dhuhr** | **none** — Fajr's window has shut and nothing has replaced it |
| Dhuhr → Asr | Dhuhr |
| Asr → Maghrib | Asr |
| Maghrib → Isha | Maghrib |
| Isha → next Fajr, *including past midnight* | Isha |

The implementation is one rule — *the last slot at or before now* — and it is correct only
because the list it scans **includes sunrise**. Sunrise is not a prayer, so when it is the
last thing to have passed the answer is `null`, which is exactly right for mid-morning. Take
sunrise out and the same code claims Fajr is still in until midday. There is no special case
in the code and there must not be one added; the special case is the sunrise entry itself.

Yesterday is scanned as well as today, for the same reason §5.8 scans tomorrow. After
midnight the prayer that is in is the previous evening's Isha, and "before Fajr means Isha"
is not a shortcut that survives high latitudes, where Isha itself can fall after midnight.

Two deliberate omissions. Madhab differences in when a window *ends* are not modelled, and
neither is the Jafari position that Dhuhr and Asr may be combined. Both would turn a
one-word marker into a qualified statement, and the disclaimer already says the app is a
helper rather than an authority. **A UI change here is a religious-content change** — the
marker says "Now", never "you can pray this now", and that distinction is the whole reason
the wording survives review.

### 5.9 Qibla
Initial **great-circle bearing** to the Kaaba (21.4224779 N, 39.8251832 E), in degrees from
**true** north. Magnetic declination comes from `android.hardware.GeomagneticField` (the
platform's World Magnetic Model). Heading from `TYPE_ROTATION_VECTOR` with circular
low-pass smoothing (factor 0.18) on the unit vector — smoothing the raw angle would break
across the 359°→0° wrap.

`isAligned` defaults to a 3° tolerance, but **every UI call site passes 5°** — 3° is
tighter than a phone magnetometer can honestly claim, so the needle would flicker in and
out of "Facing Qibla".

Verified against the Aladhan Qibla API across ten cities on five continents, test tolerance
0.5°:
London 118.987, Manchester 118.455, New York 58.482, Jakarta 295.152, Sydney 277.500,
Cape Town 23.353, Tokyo 292.999, Karachi 267.741, Cairo 136.137, Toronto 54.581.

### 5.10 City lookup — ⚠️ read this before touching it
Typed city names are resolved by **the platform `Geocoder` first**, falling back to
**Open-Meteo's free geocoding API** (`geocoding-api.open-meteo.com`, no key) only when the
phone has no geocoder backend.

It deliberately does **not** use Aladhan's `timingsByAddress`, which was the original
implementation. That endpoint now returns a **fixed placeholder — `8.8889, 7.7778` — for
every address** while still returning the correct timezone. Every city search silently
resolved to a point in Nigeria while the UI displayed the city the user had typed. Nothing
failed, nothing logged, and the times looked plausible. It was found only by comparing
against a reference API.

Two rules came out of it, both enforced in code and covered by `CityLookupParseTest`:

1. **Never display the text the user typed as if it were the resolved place.** Show the
   name that came back *with* the coordinates. Had the app done this, the fault would have
   read as "Lahore → Lagos, Nigeria" on the first attempt.
2. **A missing coordinate is a miss, not a default.** Parsing throws rather than falling
   back to zero, zero.

### 5.11 Language and locale — the app formats in its own language, not the device's

The app ships in **English only**, deliberately (§11, and `CLAUDE.md`: prayer and madhab
names are religious content and no language ships without a native speaker). That is a
decision about *words*. Android takes it as a decision about words only, and carries on
formatting *numbers, clock times, dates and layout direction* from the device locale — so
a phone set to Arabic got English sentences with Arabic-Indic digits inside them, laid out
right-to-left. What that actually looked like is in §10.

The rule, enforced in `AppLocale.kt` and guarded by `LocaleDisciplineTest`:

1. **`AppLocale.of(context)` is the app's language**, read out of
   `core/values/strings.xml`'s `app_language_tag` (`en-GB` today). Every `DateTimeFormatter`
   and `String.format` uses it. `Locale.getDefault()` appears nowhere in shipped code, and
   the test fails the build if it reappears.
2. **`AppLocale.wrap(context)` pins a whole configuration to it** — resource lookup, the
   locale `Resources.getString(id, args)` formats `%d` with, and layout direction.
   Applied in `MainActivity` and `WearMainActivity` (`attachBaseContext`, covering all UI),
   in `Notifications.postPrayerAlert`/`postOngoingBadge`, in `PrayerPdfExporter`, and in
   `NextPrayerTileService`. Step 1 alone is not enough: `stringResource` has no argument
   for the locale, so the configuration is the only lever.
3. **One exception, and it is load-bearing: 12/24-hour.** That is a device preference, not
   a language property, so it is read from `context.applicationContext` — the one context
   left unpinned. Asking a pinned context turned every time in the app into `13:10`,
   because `is24HourFormat` falls back to the *locale's* convention when the user has never
   touched the toggle, and `en-GB` is a 24-hour locale.
4. **Every translation must override `app_language_tag`.** `LocaleDisciplineTest` fails the
   build if a `values-xx/strings.xml` appears without it, or declares one that disagrees
   with its folder.

The pin is not an override of the user. It resolves to whatever language the resources
resolved to, so the day `values-ar/` ships, an Arabic phone pins itself to Arabic and goes
right-to-left with Arabic-Indic digits — correct, and with no code change.

`android:supportsRtl="true"` therefore stays in both manifests even though nothing ships
RTL today. Removing it would look like tidying and would break the first RTL translation.

**Visible consequence, on purpose:** clock times now read `1:16 pm`, not `1:16 PM`, for
everyone. `en-GB` is what the house style already says the words are, and it is what a UK
phone was already producing before this change; the old screenshots showing `PM` were taken
on an `en-US` emulator and never matched what a UK user saw. `docs/store/screenshots/` was
retaken.

---

## 6. Notification and alarm architecture

### Reliability — four independent reschedule triggers
No single failure silently stops notifications:
1. every time the app is opened
2. every time an alarm fires (chains the next window)
3. a WorkManager job (12-hour period)
4. `BOOT_COMPLETED` / `TIMEZONE_CHANGED` / `TIME_SET` / `MY_PACKAGE_REPLACED`

Alarms are laid down `HORIZON_DAYS = 2` ahead, so a missed daily job is survivable.

### Exact alarms — a trap that caused a real crash
`SCHEDULE_EXACT_ALARM` is **denied by default from Android 13**, and **`setAlarmClock`
requires it too** (an early assumption that it did not caused a `SecurityException` crash
on first launch). Strategy now:

```kotlin
if (canScheduleExact(manager)) manager.setExactAndAllowWhileIdle(...)
else                          manager.setAndAllowWhileIdle(...)
// plus a runCatching fallback to setAndAllowWhileIdle if the exact call throws anyway,
// because some OEM builds revoke the capability between the check and the call
```

Settings shows a banner offering to grant it. Without it, alerts fire within a few minutes
rather than on the minute.

### Channels — sound is immutable, so channels are versioned
Android binds sound and importance to the **channel**, not the notification. A channel's
sound **cannot be changed after creation**. So:

| Channel | Id | Importance |
|---|---|---|
| Prayer times | `prayer_times` | HIGH |
| Next prayer badge | `next_prayer_badge` | LOW, silent, no badge dot |
| Prayer alarm | `prayer_alarm_v<hash-of-sound-uri>` | HIGH, `USAGE_ALARM`, `setBypassDnd(true)` |

`ensureAlarmChannel()` hashes the chosen sound URI into the id and **deletes stale
`prayer_alarm_v*` channels**, so the system Settings list does not fill with dead entries.

Vibration pattern: `longArrayOf(0, 350, 250, 350)` — two firm pulses, noticeable in a
pocket, not startling in a quiet room.

---

## 7. Design system

**The full reference is [`docs/DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md)** — every token, every
component pattern, and the reasoning behind each. What follows is the short version.

### Palette (`ui/theme/Color.kt`)
Deep green (traditionally associated with Islam) as primary, amber as the single accent.
Light keeps warm-neutral paper surfaces; **dark is near-neutral, not green-tinted** — on an
OLED panel a tinted dark grey reads as a colour cast, and the green then has to shout to be
seen as an accent.

| Role | Light | Dark |
|---|---|---|
| primary | `#0E6B4F` | `#4FC48F` |
| accent / tertiary | `#9A6208` | `#E8B14A` |
| tertiaryContainer (warnings) | `#FBF1DC` | `#2A2113` |
| background | `#F7F5F3` | `#101312` |
| surface | `#FDFCFB` | `#171B1A` |
| primaryContainer (highlight) | `#D7EAE0` | `#143028` |
| onSurface | `#1B211E` | `#E7EAE8` |
| onSurfaceVariant | `#5A605C` | `#A9B2AE` |
| outline | `#757C77` | `#8B948F` |
| outlineVariant (borders) | `#C9CFC9` | `#2A312E` |
| secondaryContainer (nav pill) | `#CDE3D7` | `#1E4034` |

> ⚠️ **Set every Material 3 colour role, not just the obvious ones.** This bit the project
> **three separate times**: dialogs, bottom sheets and the nav pill all rendered in
> Material's default **lilac** because `surfaceContainer*`, the inverse roles, and
> `secondaryContainer` were left unset. The same bug then appeared a fourth time on the
> watch, which was using Wear's default scheme. If anything looks purple, an unset role is
> the cause.

`ColorContrastTest` asserts **WCAG 2.1 AA** for every foreground/background pair in both
themes, including the tonal containers. **Edit a colour below AA and the build fails.**

### Typography
System font family. `PrayerTimeTextStyle` uses **tabular figures** (`tnum`) so the
countdown does not jitter as digits change width. Section labels are letterspaced but
**never uppercased in code** — `String.uppercase()` on a translated string is a trap.

### Which theme, and who decides
`ThemeChoice` is **SYSTEM** (default), **LIGHT** or **DARK**, persisted under
`theme_choice` and set from **Settings → Appearance**. `SYSTEM` reads
`isSystemInDarkTheme()`, which is `false` when the OS expresses no preference — so
following the phone resolves to **light**, and light is the effective default.

Components must read `LocalDarkTheme`, **not** `isSystemInDarkTheme()`: once the user can
override the system, the system is no longer the answer. The watch is unaffected and always
draws dark.

### Component patterns that must not be simplified away
- The next prayer is marked **three ways** (highlight, leading accent bar, "Next" pill),
  because colour alone marks it for nobody on a greyscale or colour-blind display.
- The Qibla **aligned** state is marked three ways too (arc clears, dial fills, wording
  changes).
- The **Kaaba mark** on the dial stays upright at every bearing and is opaque. Both look
  like details and are not: rotating it with the dial turns it upside down when the Qibla
  is behind you, and cutting the band and door out of it as holes lets the needle show
  through the doorway. Both were built the obvious way first and both were wrong on the
  emulator. The artwork lives in `core/src/main/res/drawable/` so the phone and the watch
  cannot drift into drawing two different buildings.
- Surfaces separate themselves by **shadow in light, hairline border in dark** — one
  helper, `Modifier.sajdaSurface`. A shadow on a dark surface is invisible; a border in
  light is a hard line the design does not want.
- The next-prayer card carries a **fixed** mint-to-sand gradient in light and is flat in
  dark. It once carried a **time-of-day** gradient in both, which moved the background
  under the countdown as the day went on, so the number the screen exists to show had a
  different contrast ratio at Isha than at Fajr and could not be asserted at all. Fixed
  gradient, testable, allowed. Moving gradient, not — do not reintroduce that one.

- **Every user-facing string is a resource, and nothing is concatenated in code.** Two
  places had drifted: onboarding built `"Sunni · Hanafi"` with a template literal while
  Settings used `settings_value_pair` for the identical pair, and the location row's spoken
  description was `"$city. $changeLabel"`. Both now go through resources. A separator and a
  full stop are punctuation decisions, and a translator cannot change either one if it is
  welded into Kotlin.

### Accessibility
Contrast enforced by test · the Qibla dial is `clearAndSetSemantics {}` (decorative) with
the spoken direction in a polite live region · `rememberRemainingText` uses `stringResource`
rather than `context.getString` inside `semantics` · 56dp minimum row heights · content
descriptions on the tappable location header.

---

## 8. Privacy model

| Data | Where it lives | Leaves the device? |
|---|---|---|
| Coordinates | DataStore, on device | Never |
| City name | DataStore, on device | Never |
| Sect, madhab, method | DataStore, on device | Never |
| Notification settings | DataStore, on device | Never |
| **Typed city name** (fallback only) | Resolved on-device where possible | Only if the phone's own geocoder cannot answer, then once to Open-Meteo, with prior on-screen disclosure |
| Sect, madhab, method, location | Published to a paired watch | Only to the user's own watch, over the local Data Layer |

**Cloud backup and device-to-device transfer are both disabled.** Android's backup service
would otherwise copy the cached coordinates off the device, contradicting the app's own
privacy promise. Re-entering settings takes two taps; the guarantee is worth more.

This needs **two** declarations, and for a long time it only had one. `allowBackup="false"`
covers Android 11 and below. From Android 12 that attribute stops cloud backup but, on many
manufacturers' devices, leaves **device-to-device transfer switched on** — so the cached
coordinates would still have been copied out when the user set up a new phone. Both modules
now also carry `android:dataExtractionRules="@xml/data_extraction_rules"`, which excludes
every domain from `<cloud-backup>` and `<device-transfer>` alike.

**The watch's Data Layer listener is the one door that has to stay open.**
`SettingsSyncService` is `exported` because Play Services will not deliver events to it
otherwise, so any app on the same watch can address it and hand over a settings payload.
Write-only — there is no read path and no leak — but a bad payload means wrong prayer
times. Enum fields were always safe; coordinates now go through `Coordinates.orNull`, which
range-checks and degrades to "no location yet" rather than handing a latitude of 999 to the
engine. See §10.

Lint will complain that `dataExtractionRules` should be paired with `fullBackupContent`.
Ignore it: it does not reason about `allowBackup="false"`, which already disables backup
outright on every version `fullBackupContent` would apply to.

Permissions: `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`,
`RECEIVE_BOOT_COMPLETED`, `INTERNET`. **No fine location. No background location.**

---

## 9. Testing

**57 unit tests, all offline and deterministic, 0 failures.**

| Suite | Module | Tests | Covers |
|---|---|---|---|
| `PrayerEngineTest` | core | 17 | Reference timetables for Makkah, London, Tehran; Jafari Maghrib; madhab differences; high latitude; Ramadan; next-prayer roll-over and midnight spillover; chronological ordering across 3 cities × 4 methods × 52 weeks |
| `QiblaEngineTest` | core | 8 | Ten cities on five continents, distance, normalisation |
| `DeterminismTest` | core | 3 | Repeat-call stability, minute alignment, madhab equivalence |
| `CoordinatesTest` | core | 4 | `Coordinates.orNull` rejects off-globe values, NaN, infinity and half-pairs — the gate on everything the watch's exported Data Layer listener is handed (§8) |
| `LocaleDisciplineTest` | core | 4 | `app_language_tag` parses and round-trips; every translation declares one; it matches its folder; no shipped code calls `Locale.getDefault()` (§5.11) |
| `ColorContrastTest` | app | 4 | WCAG AA for every pair in both themes |
| `CityLookupParseTest` | app | 5 | Coordinates come from the response; resolved name is displayed, not typed text; missing coordinate is a miss |
| `TileFormatTest` | wear | 8 | Countdown wording at boundaries; the refresh floor that stops a passed prayer looping the tile |
| `WearSettingsTest` | wear | 4 | Synced settings reach the engine intact; unpaired watch still calculates; the wire-key contract |

Reference values were captured from the **Aladhan API** — an independent implementation of
the same conventions — and pinned as golden values so the suite stays offline:

```bash
curl "https://api.aladhan.com/v1/timings/DD-MM-YYYY?latitude=..&longitude=..&method=N"
# method 0 = Shia Ithna-Ashari, 3 = MWL, 4 = Umm al-Qura
curl "https://api.aladhan.com/v1/qibla/<lat>/<lng>"
```

### Full verification command

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --rerun-tasks \
  :core:testDebugUnitTest :app:testDebugUnitTest :wear:testDebugUnitTest \
  :app:lintDebug :wear:lintDebug :core:lintDebug \
  :app:assembleRelease :wear:assembleRelease
```

Expected: BUILD SUCCESSFUL, 57 tests, 0 failures, lint informational-only, phone release
APK ~1.9 MB and watch ~2.6 MB (both **unsigned**, unless a `keystore.properties` is present).

For Play, build bundles rather than APKs — `:app:bundleRelease :wear:bundleRelease`, which
give ~4.3 MB and ~3.5 MB `.aab` files.

`JAVA_HOME` matters — see §12. If any of it fails, check §13 first.

---

## 10. What has been verified on a device, and how

Two emulators were used: `sajda` (phone, API 36) and `sajdawear` (Wear OS, API 34).

**The header date line, verified both ways round.** On `sajdastore` the header reads
`Fri 31 Jul · 17 Safar 1448`, captured from a running build rather than reasoned about.
The RTL build was then run through `./gradlew installRtl` and, because the Hijri part came
out as `Safar 1448 17` rather than `17 Safar 1448`, **the change was stashed and the previous
version rebuilt and captured for comparison**. The baseline renders `Safar 1448 17` too, so
the reordering is pre-existing pseudolocale behaviour on English text in an RTL paragraph and
not a regression. That comparison is the only thing that could have answered the question —
the rendering looks exactly like the bug `AppLocale.kt` documents, and reasoning about it
would have concluded the wrong thing in either direction. Note for whoever ships the first
RTL translation: with real Arabic month names and Arabic-Indic digits this is a genuine RTL
run and behaves differently, so re-check it then rather than trusting this note.

That re-check has since been done under a real RTL locale rather than the pseudolocale, and
it moved two of the paragraphs below from reasoning to measurement — including one that was
the wrong way round. Read "The pseudolocale hides the answer it is being asked for" later in
this section **before** changing anything about how times or dates are laid out in RTL.

**Phone — verified working:** full onboarding · permission rationale · reverse geocoding
("Greater Manchester") · home screen times · PDF export (file pulled off the device and
rendered — clean 31-day table matching the screen) · settings · the Shia switch changing
Maghrib 9:09→9:35 PM and Asr 6:37→5:28 PM live, proving the 4° rule on device · dark mode ·
alarms registered in `dumpsys alarm` on whole minutes · notification firing · ongoing badge ·
bottom nav · disclaimer dialog · Qibla screen (118°, 5025 km, "Turn right 118°") ·
**alarm mode end-to-end** (clock moved to a prayer time; fired on `prayer_alarm_v3a4a6650`
with the chosen sound; badge rolled on to Isha) · **ringtone picker** (selection persisted
and is now named in Settings) · **location sheet** (city search resolving Lahore correctly,
sheet auto-closing).

**Watch — verified working:** app launches · acquires its own location · shows correct
Manchester times · Qibla page (118°, on-brand colours) · permission prompt · **the "No
location yet / Use Makkah" fallback, tapped through to Makkah times** · **the tile
rendering live data** ("Maghrib · 17:01 · in 1h 14m", matching the app) · **the on-wrist
school picker** (switching to Hanafi moved Asr from 5:28 PM to 6:37 PM, after which every
time matched the phone exactly).

**What running both emulators side by side caught.** The watch said Asr 5:28 PM while the
phone, set to Hanafi, said 6:37 PM. Fajr, Dhuhr, Maghrib and Isha all matched to the
minute. The watch was not miscalculating — it was correct for Shafi'i, its own default,
because the phone's setting had never reached it. Nothing on the watch admitted that. This
is the strongest argument in the repo for running two devices at once rather than one: no
amount of reading either module alone would have shown it, because neither module was
wrong.

**The three things that used to be "static review only" are now verified.** Each had been
reasoned about in code and never watched run, and two of them were wrong.

| Claim | How it was actually proved |
|---|---|
| The compass remaps for screen rotation | Portrait read "Turn right 118°, facing 0°". Rotating to landscape, with the device never physically moving, read "Turn right 28°" — a 90° shift, which is `remapCoordinateSystem(AXIS_Y, AXIS_MINUS_X)` doing its job. |
| Alarms survive Doze | Under forced deep idle, `dumpsys alarm` named SajdaTime as `Next wake from idle` with `whenElapsed` undeferred and `flags=0x3` (`FLAG_WAKE_FROM_IDLE`). Crossing the trigger posted "Time for Asr" and rolled the badge to Maghrib. |
| Alarms pass Do Not Disturb | Under `ZEN_MODE_IMPORTANT_INTERRUPTIONS`, both prayer alarms posted with `mIntercept=false`; the silent badge posted with `mIntercept=true`, which is correct for a silent ongoing notification. |
| The PDF lands in Downloads | Monthly export wrote `PrayerTimes_July2026.pdf` into `MediaStore` `Download/`; pulled off and rendered to check columns, 31 rows and 12-hour times. |
| Everything scales to a large font | `font_scale 1.5` on every screen: no clipping, no truncation, no overlap. |

**What rotation testing caught that reading the code did not.** Every screen's state was
held in `remember`, not `rememberSaveable`, so rotating the phone reset the selected tab,
closed any open dialog or sheet, threw away a half-typed city name, and — worst — sent a
user part-way through first-run setup back to the welcome step. All seven are saveable now.
It also caught the Qibla dial being `fillMaxWidth().aspectRatio(1f)`, which in landscape
made it taller than the screen and pushed the turn instruction off the bottom.

**Modifier order is not cosmetic.** The first attempt at capping that dial was
`fillMaxWidth().widthIn(max = 320.dp)`, which does nothing at all: `fillMaxWidth` pins
`minWidth` to the full width, and a later `widthIn` cap can never win against it. It has to
be `widthIn(...).fillMaxWidth()`. This looked correct on the screen and was only caught by
measuring the running app.

### The dark design system (v1.1.0), verified by screenshot

Every claim below is from a capture of the running app, not from reading the diff.

| Checked | Result |
|---|---|
| Times, dark | Flat next-prayer card, bordered day list, dividers, sunrise dimmed one type step |
| The "next" row | Clock moved to 12:00 so a *today* prayer was next — accent bar, "Next" pill and highlight all present. At 23:48 none of it appears, correctly, because the next Fajr is tomorrow's |
| Times, light | Same three treatments carry over; the light palette was not touched |
| Qibla, dark | Green arc sweeping 118° from the facing tick to the arrow, legend beneath |
| Settings, both | Amber warning banner, letterspaced green section labels, bordered group cards |
| Settings at `font_scale 1.5` | Group cards and rows grow; nothing clipped |
| Disclaimer at `font_scale 1.5` | Scrolls, and the dua request at the end is reachable |
| Watch | New palette on both pages; the Qibla dial's arc and facing tick match the phone |

**What the screenshots caught that the code review did not.** The disclaimer gained a
paragraph about watch accuracy, which made it five paragraphs. `AlertDialog` does **not**
scroll its body for you — and both places the disclaimer appears passed a bare `Text`. At a
raised font size the last paragraph, the dua request, was exactly what would have fallen
off the bottom unread. Both are wrapped in a `verticalScroll` now. Adding a sentence to a
string is not a text change; it is a layout change.

### The light design system and the theme setting (v1.1.0), verified by screenshot

| Checked | Result |
|---|---|
| Times, light | Mint-to-sand hero gradient, shadowed cards, dividers, sunrise dimmed, next row with accent bar and pill |
| Qibla, light | Pale dial face, deep green arc sweeping 118° from the facing tick, legend |
| Settings, light | Amber banner, green letterspaced labels, shadowed group cards, the new Appearance chips |
| Dark tapped while the phone is on light | Whole app flips immediately; status-bar icons flip with it; cards revert from shadows to hairline borders |
| Force-stop and relaunch | Still dark, phone still light — the choice is persisted, not a session flag |
| Follow phone tapped again | Back to light |

**What the two designs got wrong, in the same way, twice.** Both halves of the source
design overstate a contrast ratio for the token Material spends on chevrons and tick
marks: dark `outline` claimed 3.1:1 and is 1.85:1, light `outline` claimed 3.0:1 and is
1.55:1. The light half also claims 4.6:1 for its amber, which is 3.76:1. All three are
recorded with the arithmetic in `docs/DESIGN_SYSTEM.md`. **A design tool's ratio column is
a claim, not a measurement** — check it, because every one of these errs in the direction
that ships an unreadable app.

### Store screenshots, retaken natively (v1.1.0)

Retaken on a purpose-built `sajdastore` AVD — Pixel 2 profile, 1080 × 1920 at 420dpi, which
is 9:16 natively so **nothing is scaled, reframed or padded**. The previous set was captured
at 1080 × 2400 and shrunk onto a 9:16 canvas with green pillarboxing, which cost a fifth of
the frame; see §15 lesson 24 for why the rule that drove it was misremembered.

| Checked | Result |
|---|---|
| Phone captures are 9:16 with no post-processing | ✅ Five files, all exactly 1080 × 1920, copied through untouched |
| Status bar not clipped | ✅ Fixed by moving off the Pixel 7 profile — its hole-punch cutout has a 136px top inset that does not fit a 1920-tall screen and cuts the clock in half |
| Prayer times agree between phone and watch | ✅ Asr 5:28 pm, Maghrib 9:07 pm, Isha 11:28 pm on both, same location, same build. This is the §5 cross-module gap being checked rather than assumed |
| Qibla maths, end to end | ✅ Sensors fed a real heading: facing 60°, Qibla 118°, app renders "Turn right 58°". 118 − 60 = 58 |
| Amber exact-alarm banner absent from the Times shot | ✅ `SCHEDULE_EXACT_ALARM` granted before capture. The banner stays in the app; it is simply not what the screenshot exists to show |
| Five distinct screens | ✅ The old set spent 2 of 5 slots on the same Times screen and 2 more on the same Qibla screen |
| Icon and feature graphic on the current palette | ✅ `#14624B` → `#0E6B4F` (`LightPrimary`); sand `#F0D69A` is 4.57:1 on it, still AA for text. `./gradlew test lint` passes, `ColorContrastTest` included |
| Wear captures | ✅ 454 × 454 off `sajdawear_large`, 1:1, above Play's 384px floor |

**Not verified:** all of it is emulator output; none of it has been seen on real hardware.
The watch Qibla page shows its bearing-from-north presentation rather than the live
"facing" one, because the Wear AVD has no magnetometer and — unlike the phone — no sensor
value fakes one convincingly. That is a real state the app has when a watch compass is
unavailable, so the screenshot is honest; it is just not the best version of that screen.

### Wear OS checks closed by this session's audit

| Checked | Result |
|---|---|
| Round display, 192dp | Verified on `sajdawear` — 384px at 320dpi, corner radius 192, i.e. a true circle. Both pages, nothing clipped, nothing unreachable |
| Wear font scaling, 1.3× | Both pages: no overlap, no truncation. The times list grows and scrolls, which is what it is for |
| `w1-times.png` chopping a glyph | Recaptured at a scroll position where the bottom edge falls between rows. This had been flagged in `RELEASING.md` and left open |
| Round display, 227dp | **Closed.** See below |

### Round display, 227dp — closed, and what it found

The blocker was never the app; it was that the only Wear AVD here was small round, and the
obvious shortcut is a trap. `adb shell wm size 454x454` resizes the panel but *not* the
rounded-corner overlay, which was computed once for the real 384px display — so the stale
mask paints **straight vertical cuts** through the text. A round screen cannot produce a
straight vertical edge, so those captures are artefacts and must not be filed as evidence.

The SDK has had the answer all along: a `wearos_large_round` device profile, 454px at
320dpi, i.e. exactly 227dp, with `mRoundedCorners` reporting `radius=227, center=(227,227)`
on all four corners — a true inscribed circle. `./tools/wear-verify.sh` creates that AVD if
it is missing, boots both watches, and walks every screen at font scale 1.0 and 1.3.

| Checked | Result |
|---|---|
| 227dp round, font 1.0 and 1.3 | Every screen clear. Nothing behind the bezel, nothing overlapping |
| 192dp round, re-run against the same sweep | Clear at 1.0. At 1.3 the first line of the watch disclaimer passes under the clock at the very bottom of the scroll — body text, one flick from being readable, and not fixable by padding (see §15) |
| Bezel clipping, all 28 captures | `tools/wear-round-check.py` — pure-stdlib PNG decode, no clipped content on either size at either font scale |

**Two real defects, found only because the sizes were finally run side by side.**

1. **The school button was printed underneath the watch face clock** at the bottom of the
   times list — grey on green, unreadable — on *both* sizes, worst at 227dp/1.3×. Root
   cause: `ScalingLazyColumn`'s default auto-centring reserves half a screen of trailing
   space so the last item can rest in the middle, and that space is taken from the top,
   hoisting whatever is second-to-last into the narrow cap of the circle where the clock
   lives. Fixed by turning auto-centring off, padding the list explicitly as a share of
   screen height, and making the button the *last* item so the taller disclaimer absorbs
   the top slot instead.
2. **The first screen had 44dp of clock clearance it no longer needed**, which was paying
   for the auto-centred scroll. With auto-centring off it cost two prayer rows off the
   bottom of a 192dp watch. Trimmed to 16dp.

Neither was visible on the small round alone at default font. Both were invisible to the
compiler, to lint, and to the unit tests.

**Store screenshots are part of the diff.** All five phone captures and both watch captures
were retaken and `tools/build-store-assets.sh` re-run. A theme change that leaves
`docs/store/screenshots/` alone ships a listing that does not match the app.

### The Kaaba mark on the dial — what running it changed

Added so the Qibla screen says *which way* without requiring the user to read anything.
Every claim here is from a capture of the running app.

| Checked | Result |
|---|---|
| Phone, light | Black cube, light band and raised door, needle ending inside it. This is the orientation the mark was designed for — in light theme the silhouette is genuinely the colour of the building |
| Phone, dark | Inverted, as a tinted icon must be: light cube, dark band. Legible, reads as the same object |
| Phone, aligned (`primaryContainer` face) | Band and door repaint in the green face colour and stay legible; the mark covers the facing tick, which by then has nothing left to say |
| Watch, 192dp and 227dp, font 1.0 and 1.3 | Drawn clear of the tick ring and clear of the watch face clock on both sizes. All 24 captures pass `tools/wear-round-check.py` |
| Watch, aligned | Same repaint as the phone, verified by driving the virtual magnetometer to the Qibla bearing |
| Contrast | Four new pairs in `ColorContrastTest` at the 3:1 non-text threshold — `onSurface` on the dial face and on the aligned dial face, light and dark. They were never under test before, and the north wedge had been relying on them too |

**Two things that reviewed clean and were wrong on the emulator.**

1. **The band and door were holes in a single even-odd path.** Neater, one asset, and it
   let the dial face show through by construction — except that what shows through a hole
   is not the dial face, it is *whatever is behind the mark*, and what is behind the mark
   is the needle. The doorway filled with green. Now an opaque silhouette with the detail
   painted over it.
2. **The watch needle was left at its old length and buried in the cube.** On the phone
   that is right — the needle is long and its tip vanishing behind the Kaaba reads as an
   arrow arriving. On the watch the needle already floats clear of the centre readout, so
   burying the tip left a stubby trapezoid that stopped reading as an arrow at all. Pulled
   back to 0.60 of the radius, where it stops just short and shows a whole triangle.

The second one is the more useful lesson: the same change was correct on one screen and
wrong on the other, and nothing but looking at both would have said so.

### Locale — what running the app in another language found

Nobody had ever launched the app on a phone set to anything but English, because the app is
English-only and that felt like the end of the question. It is not: Android keeps *words*
and *formatting* in two different places, and only the first of them was English. Run with
`adb shell cmd locale set-app-locales com.sajdatime.app --locales ar-EG`, which sets the
app's locale without disturbing the rest of the device.

| Locale | Before | After |
|---|---|---|
| `ar-EG` — Hijri date | `١٤٤٨ Safar ١٧` — reads as the wrong date, not as a broken one | `17 Safar 1448` |
| `ar-EG` — Qibla subtitle | `km ٤٨٢٢ from true north. The Kaaba is about ١١٨°` with the full stop alone on line 2 | `118° from true north. The Kaaba is about 4822 km away.` |
| `ar-EG` — turn instruction | `١١٨° Turn right` | `Turn right 118°` |
| `ar-EG` — prayer rows | `am 2:50` — meridiem on the wrong side of the number | `2:50 am` |
| `ar-EG` — ongoing badge | `In ٢h ٥m` | `In 2h 5m` |
| `bn-BD` — home card | countdown `০২:১৮:১৯` in Bengali digits, wrapped onto two lines, directly above prayer times reading `2:50 AM` in Latin ones | `02:07:31`, one line, matching the rows |
| `ar-EG` — PDF export | (not previously checked) | exported and rendered: English headings, English day names, Latin digits throughout |
| `ar-EG` — watch, 192dp | (not previously checked) | `2h 6m`, `1:16 pm`, `118°` — identical to English, `wear-round-check.py` PASS |
| `ar-EG` — onboarding | — | the Bismillah still renders right-to-left with full diacritics. Strong-RTL text is unaffected by the paragraph pin, which was the one thing that would have made this unacceptable |
| default `en-US` device | `1:16 PM` | `1:16 pm` — the deliberate `en-GB` change, §5.11 |

Both guard tests were proved red before being accepted: a `values-ar/strings.xml` with no
tag, then one declaring the wrong tag, then a single `Locale.getDefault()` reintroduced
into `TimeFormat.kt`. Each failed the build, and the build went green again when reverted.

Full regression after the change: `./gradlew clean test lint :app:bundleRelease
:wear:bundleRelease` BUILD SUCCESSFUL, 53 tests, 0 failures; `./tools/wear-verify.sh`
**24/24 PASS** across 192dp and 227dp at font scales 1.0 and 1.3.

**What was not tested:** no physical phone or watch of any kind. The Data Layer sync
between phone and watch was not re-exercised (it carries coordinates and settings, not
text, so it should be untouched — that is reasoning, not evidence). No right-to-left
*translation* has been tried, because none exists; what has been shown is that an
English-only app on a right-to-left device now behaves, not that an Arabic build would.

### Right-to-left, previewed before any translation exists — what `installRtl` found

Pinning the app's formatting to its own language (§5.11) closed a real class of bug and cost
one thing: the pseudolocales stopped reaching the app, so there was no longer any way to ask
"will these screens survive right-to-left?" before a translation existed. The `rtl` build
type gives that back — `./gradlew installRtl`, no device setting to change, no locale folder
that could be mistaken for a translation.

Run on both emulators. **Everything structural held**, which was not a foregone conclusion:

| Checked | Result |
|---|---|
| Bottom navigation | mirrored — Settings right→left becomes Settings, Qibla, Times left→right |
| Settings rows | icons move to the right, `AutoMirrored` chevrons flip to point left |
| Theme chips (`FlowRow`) | mirrored exactly — order reversed, `start` padding moved to the right edge |
| Prayer rows | time and name swap sides, nothing clipped or overlapped |
| Qibla dial (`Canvas`) | **not mirrored** — needle, Kaaba mark, north triangle and tick marks all unchanged. This was the one that mattered: a mirrored Qibla compass would point people the wrong way |
| Countdown, 52sp | one line |
| Watch, 192dp | mirrored, nothing clipped |

The residual scrambling that remains — the Hijri date reading `Safar 1448 17`, the Qibla
subtitle ending `.away` on its own line — is the pseudolocale doing its job, not a defect.
It is what a *Latin* word looks like inside a right-to-left paragraph. With Arabic month
names and an Arabic sentence those runs are strong-RTL and reorder correctly; that was
checked against the LTR build, which renders both correctly. It does mean the Hijri date is
only right in Arabic once the month names are translated too — the date and its month name
have to arrive in the same language.

One false alarm worth recording, because it is the trap §15 warns about: the theme chips
looked unmirrored until they were compared against the same screen in the LTR build, where
the order and padding turned out to be an exact mirror. Three of this project's four
"confirmed bugs" have now been errors in the test.

`MP 1:16` on those screens is `PM` reversed — Android's ICU pseudolocale reverses Latin
text it supplies itself. That is a useful tell: anything that comes out reversed came from
the platform, not from `strings.xml`, so the meridiem will localise on its own.

#### The pseudolocale hides the answer it is being asked for — point `rtl` at a real locale

The paragraph above, and the one before it about Arabic month names, were **reasoning**. The
pseudolocale cannot confirm either of them, because `ar-XB` reverses Latin characters
outright, and character reversal is not something any real locale does. Everything mangled
therefore looks equally mangled, and the interesting question — *which of these would still
be wrong with a genuine RTL language?* — is exactly the one it cannot answer.

It can be answered, cheaply and in about two minutes, by temporarily pointing the same build
type at a **real** RTL locale that has no translation in the app. `ur` was used, because
Urdu is right-to-left *and* keeps a Latin `AM`/`PM` in CLDR, so it exercises the worst case:

```bash
# app/src/rtl/res/values/strings.xml — temporarily, then put ar-XB back
<string name="app_language_tag">ur</string>
```

What that showed on the phone emulator, and it is not what the notes above predicted:

| Under real `ur` | Result |
|---|---|
| Prayer times | `AM 2:54`, `PM 1:16` — **not** reversed, and correctly placed |
| Gregorian half of the header | `جمعہ، 31 جولائی` — localised on its own |
| Hijri half of the header | `Safar 1448 17` — still wrong |
| Warning-card sentence | `.minutes late. Tap to allow` — full stop still stranded |

Three things follow, and the first is the one that matters.

**`AM 2:54` is correct, not broken.** It looks wrong to an English eye and it is not. The
logical string is `2:54 AM`; the paragraph is RTL; a reader starting at the right edge reads
`2:54` first and `AM` second, which is the order it was written in. Unicode bidi put the
first logical run at the right-hand end because that is where an RTL reader begins. Anyone
who "fixes" this with a bidi isolate will be moving `AM` to the far side of the line *from*
the correct position, and the English-looking result will be the wrong one. Do not do it.

**The residual scrambling is precisely and only the strings the app supplies in English.**
The Gregorian date localised without a code change, which is `getBestDateTimePattern` and
`AppLocale` working as §5.11 says they do. The Hijri date did not, and now the reason is
narrowed to one thing rather than assumed: its month names come from the app's own string
array (`HomeScreen.kt`, and the comment there says so), so they stay English, stay a strong
LTR run, and get reordered around the two numbers. That confirms the claim above — the Hijri
date comes right when its month names arrive in the same language as the date — and it
confirms it by measurement instead of by argument.

**Leave the build type on `ar-XB`.** The `ur` pin is a diagnostic to reach for deliberately
and revert, never something to commit. `ar-XB` is reserved and no real phone can be set to
it, which is the property that stops the `rtl` build type from ever being mistaken for, or
promoted into, a translation — read the comment at the top of that strings file. `ur` is a
real language tag and would throw that safety away for no gain.

**Not tested:** onboarding was not re-run under `rtl` (it is prose with no numbers, and its
Bismillah is strong-RTL and already verified in §10); the PDF and the tile were not
re-exported under it; `./tools/wear-verify.sh` was not re-run, because no shipped code
changed — the diff is two build files and one resource that exists only in a build type
that is never released. Verified instead that neither release bundle contains `ar-XB`.

### Full pre-release audit (31 Jul 2026) — what was checked and what it found

Run on both emulators together, against the shipped code, after the owner asked for a sweep
rather than spot checks. Two things came out of it: one real defect, and one phantom.

**Times, checked against Aladhan and against adhan-java itself.** Manchester, 31 July 2026,
Sunni/Shafi'i/AUTO → MWL. All six times on screen matched adhan-java **exactly**. Against
Aladhan: Fajr, Sunrise and Maghrib identical; Dhuhr +1 (the MWL method offset, §5.3); Asr −1
and Isha +1 (nearest-minute rounding between two independent implementations). Every
difference is accounted for and inside the ±2 min the suite already allows. The PDF was
exported for the whole month, pulled off the device and rendered: 31 rows, correct weekdays,
last row identical to the screen, and 1 July independently re-checked against Aladhan (5 of
6 exact, Dhuhr +1 as expected) — so the whole range is right, not just today.
Qibla re-derived from the great-circle formula: 118.45° / 5025 km against the app's
118° / 5024 km.

**The phantom.** The watch showed Asr 6:36 pm against the phone's 5:28 pm — a 68-minute gap,
which is precisely Hanafi versus Shafi'i, and exactly the phone-versus-watch divergence this
document warns is the worst bug class in the project. It was not a bug. Dumping the watch's
DataStore showed `madhab = HANAFI` written into it by an earlier session's manual testing;
both modules default to `SHAFII` and both persist by name. `pm clear` and a relaunch brought
all six times into line with the phone. **Four of this project's five "confirmed bugs" have
now been faults in the test rather than the code** — read the device's actual persisted state
before believing a cross-module difference.

**The real one — a false statement to the user, now fixed.** On a cold start the home card
read *"Set your location to see prayer times."* while the user's coordinates were sitting in
DataStore the whole time. The card keyed off `next == null`, which is true both before the
first calculation lands and when there genuinely is no location; only the second deserves
those words. It is now keyed to `!loading && coordinates == null`, which is the thing the
sentence actually talks about. Seen once, on the slow API 36 emulator, and *not* reproducible
in a controlled 40-sample re-run — recorded honestly as intermittent rather than dressed up
as reliably reproduced. Verified after the fix across three cold starts with the empty text
never appearing.

Worth knowing for anyone hunting the same thing: post-onboarding, `coordinates == null` looks
unreachable. Onboarding disables **Continue** until a location exists, and the decline path
offers a typed city or *"Skip for now and use Makkah"*. The genuine no-location case is
already covered by its own, better-worded banner — *"Showing times for Makkah / We could not
work out where you are"* — which was exercised end to end and whose times match Aladhan for
Makkah exactly (Fajr 02:33, Sunrise 03:54 in the device's London zone). So the branch that
was fixed is defensive; the fix removes a lie without removing any message a user needs.

**Everything else checked, clean:** onboarding both ways (granted, and denied → Makkah);
approximate-location permission dialog confirms coarse-only; disclaimer with its dua
paragraph intact; Times, Qibla, Settings; dark theme; theme choice surviving restart;
location sheet re-acquiring; PDF export; **font scale 150% and 200%** — the hero card,
countdown, warning card and all three nav labels reflow with nothing clipped or overlapped.
**No crashes and no ANRs on either device.** The Wear emulator's own sensors HAL
(`android.hardware.sensors-service.multihal`) died three times during the run; that is
emulator infrastructure, not this app, and no SajdaTime process was involved.

**Release bundles:** both build, both **unsigned** (no `keystore.properties`, as intended),
`en-GB` ships, no `ar-XB` in either, `allowBackup=false` plus full `cloud-backup` **and**
`device-transfer` exclusions in the merged release manifests. The app declares no foreground
service of its own — `FOREGROUND_SERVICE` and `WAKE_LOCK` arrive from WorkManager's manifest
and nothing ever calls `setForeground`, so the Android 14 "missing foregroundServiceType"
crash cannot occur here.

**Not tested:** no physical device of any kind. Data Layer settings sync between phone and
watch is still unexercised — the two emulators are not paired, so the watch was verified
standalone on its own defaults, which is what actually matters for a standalone watch app but
is not the same thing as proving the sync. Alarms firing, notification delivery and the tile
were not re-exercised this session; they were verified earlier and no code touching them
changed. Lint's `DataExtractionRules` warning fires on both modules and is a false positive
here: it asks for `fullBackupContent`, which only matters when `allowBackup="true"`.

### The "Now" marker and the closable notice (31 Jul 2026), verified by stepping the clock

Both asked for by the owner: show which prayer is *still* prayable, and stop the exact-alarm
warning from meeting every user on every launch.

The marker was checked at four times of day by moving the emulator clock, because the whole
question is which side of a boundary "now" falls on and reading the code proves nothing:

| Clock | On screen | Correct because |
|---|---|---|
| 03:32 | **Now** on Fajr, **Next** on Dhuhr | inside the Fajr window; sunrise is skipped for "next" |
| 09:00 | **no Now anywhere**, Next on Dhuhr | Fajr's window shut at 05:23 and Dhuhr has not come in |
| 19:18 | **Now** on Asr, **Next** on Maghrib | between Asr 17:28 and Maghrib 21:07 |
| 00:30, next day | **Now** on Isha, **Next** on Fajr | last night's Isha is still in, and the marker does not blink out at midnight |

The 09:00 case is the one worth keeping: it is the only one where the right answer is
*nothing*, and it is the one a "last prayer that started" implementation gets wrong.

Also verified: `secondaryContainer` "Now" pill against `primary` "Next" pill in **both**
themes; **right-to-left** via `installRtl`, where the pill mirrors to the left of the time
and the notice's close button mirrors to the left of the card; the notice staying closed
across force-stops and reinstalls; and Settings still showing it afterwards, so the fix is
never lost. 62 tests, up from 57 — five new ones in `PrayerEngineTest`, four of them
boundary cases and one that walks the whole day in five-minute steps asserting sunrise is
never returned.

Re-checked at **200% font scale** after the fact, because the first write-up honestly
recorded the pill as untested at that size: both pills render in full, nothing clipped, the
rows grow as they are meant to.

**Not tested:** the watch has no such marker and was not changed. Notification and alarm
copy do not mention the current prayer and were not touched.

### Midnight rollover — a stale day list, found by stepping the clock

Both apps drove their live clock off one trigger: recompute when the next prayer passes.
That is correct for every hour of the day except the ones after midnight, when the next
prayer is Fajr and nothing passes for hours. So an app left open across midnight kept
showing the *previous* day's timetable until Fajr.

Reproduced on the phone emulator with the app in memory, stepping the system clock rather
than reasoning about it:

| Clock | "Today" list showed | Should have been |
|---|---|---|
| 30 Jul, 23:50 | Sunrise 5:23, Asr 6:27, Maghrib 8:54 | correct for the 30th |
| 31 Jul, 01:00 — *before the fix* | Sunrise 5:23, Asr 6:27, **Maghrib 8:54** | Sunrise 5:24, Asr 6:26, **Maghrib 8:52** |
| 31 Jul, 01:00 — *after the fix* | Sunrise 5:24, Asr 6:26, Maghrib 8:52 | ✓ |

**Maghrib was two minutes late**, which is the time a fasting person breaks their fast. The
header was right the whole time, because `hijriToday` had already been fixed for exactly
this and keys off the live clock — so the screen showed today's date above yesterday's
times, and looked entirely plausible. The "Next" pill also vanished from the list, because
the next prayer was no longer one of the rows being displayed; after the fix it is back on
Fajr.

Same defect and same fix on the watch, verified the same way: the list moved from Fajr
2:53 am to 2:54 am across the boundary, where before it did not move at all.

**Not covered by a test.** The trigger lives in a `ViewModel` loop with no test harness in
this project (no Robolectric, no instrumented tests — §11). The evidence is the emulator
run above. An instrumented test is the right home for it if Roborazzi ever lands.

### Security review — what was looked at, and what was found

A full pass over network, storage, logging, exported components and permissions. Findings
were thin, which is itself the useful result:

| Checked | Result |
|---|---|
| Network calls | Exactly one, `https://geocoding-api.open-meteo.com` in `CityLookup`, and only when the platform geocoder returns nothing. Sends the typed place name and nothing else — no identifier, no coordinates, no history. HTTPS, 12-second timeouts, connection closed in a `finally` |
| Cleartext | Already blocked by the platform default at targetSdk 36; now declared explicitly with `usesCleartextTraffic="false"`, so a future merged manifest cannot quietly re-enable it |
| Logging | **Zero** `Log.*`, `println`, `printStackTrace` or `System.out` in any shipped source file. Nothing to leak |
| Exported components | Phone: the launcher activity only. Both receivers `exported="false"`, `FileProvider` `exported="false"` and scoped to `cache/exports/` — the generated PDFs and nothing else. Watch: the tile service, guarded by `BIND_TILE_PROVIDER`, and the Data Layer listener below |
| `PendingIntent` flags | Every one is `FLAG_IMMUTABLE` |
| Storage | Both DataStores are app-private by default. Cloud backup and device-to-device transfer both excluded explicitly (§8) |
| Permissions | Five on the phone, two on the watch, each with a written reason in the manifest. No `ACCESS_FINE_LOCATION`, no background location, no `READ/WRITE_EXTERNAL_STORAGE` |
| Third-party SDKs | None. No analytics, ads, billing, crash reporting or attribution library anywhere in `libs.versions.toml` |
| Static `Context` fields | None |

The one thing worth naming: **`SettingsSyncService` must be `exported`** and cannot be
anything else, because Google Play Services delivers Data Layer events to it. Any app on
the same watch can therefore address it directly and hand over a settings payload. It can
only write, never read, so this is not a data-leak path — but a hostile or corrupt payload
means wrong prayer times, which is the one real harm this app is capable of. Enum fields
were already safe (`enumOr` falls back to a valid constant on any unrecognised string);
coordinates were not, and a latitude of 999 would have gone straight to the engine. Both
readers now go through `Coordinates.orNull`, which range-checks and degrades to "no
location yet" — a state every screen already handles, because a first run looks the same.
`CoordinatesTest` pins it, including NaN and infinity.

### The first genuinely signed release build (31 Jul 2026)

Before the owner's upload key existed, `./gradlew ... :app:bundleRelease :wear:bundleRelease`
exited **0** and produced two `.aab` files that looked entirely normal — right size, right
version, sitting in the right folder. They were unsigned, and **nothing in the build output
said so.** The signing config is created only when `keystore.properties` is present, so with
no key the `signReleaseBundle` task does not fail: it does not exist, and a task that never
runs prints nothing. An unsigned bundle is rejected on upload, not at build time, so this
would have surfaced as a confusing Play Console error minutes after a clean green build.

The check that actually distinguishes the two, and the one to use before any upload:

```bash
unzip -l app/build/outputs/bundle/release/app-release.aab | grep -E 'META-INF/.*\.(RSA|SF)'
```

Signed output names the key alias — `META-INF/SAJDATIM.SF` and `META-INF/SAJDATIM.RSA`.
Unsigned output prints nothing at all, which is the entire tell.

After the key was wired in, the full verification command was re-run from clean:

| | |
|---|---|
| Result | `BUILD SUCCESSFUL in 57s`, 243 tasks |
| Unit tests | **62**, 0 failures, 0 errors — 41 `:core`, 12 `:wear`, 9 `:app` |
| Lint | **0 errors**, 11 warnings (4 `:app`, 2 `:wear`, 5 `:core`) — the previously audited set |
| Signing | `:app:signReleaseBundle` and `:wear:signReleaseBundle` both ran; both bundles carry `SAJDATIM.RSA` |
| Accepted by Play as | version **2 (1.1.0)**, API 24+, target SDK 36, 4 ABIs, **1.64 MB** install size |

Play raises exactly one warning on the bundle — *"contains native code, and you've not
uploaded debug symbols"* — and it is a false alarm worth not chasing. The app has no native
code. The four ABIs come from two Google libraries that ship `.so` files of their own:

```
libandroidx.graphics.path.so     (Compose)
libdatastore_shared_counter.so   (DataStore)
```

There is nothing of ours to symbolicate, and no crash reporting in the app to symbolicate it
for.

### Useful device-testing recipes

```bash
# Run the whole app right-to-left, without touching a device setting or shipping
# a translation. Phone and watch both have the build type.
./gradlew installRtl
```

```bash
# Both watch sizes, every screen, two font scales, plus the bezel check.
# Creates the 227dp AVD if it does not exist. Run this after ANY watch UI change.
./tools/wear-verify.sh
```

```bash
# Fire a real alarm: move the clock to just before a prayer time.
# `date MMDDhhmmYYYY.ss` may be rejected; epoch seconds is more reliable.
adb -s emulator-5556 shell settings put global auto_time 0
adb -s emulator-5556 shell su 0 date @1785528410
```

```bash
# Doze and Do Not Disturb
adb -s emulator-5556 shell dumpsys battery unplug
adb -s emulator-5556 shell dumpsys deviceidle force-idle
adb -s emulator-5556 shell dumpsys alarm | grep -A5 PRAYER_ALARM
adb -s emulator-5556 shell cmd notification set_dnd priority
# mIntercept=false means it got through DND:
adb -s emulator-5556 shell dumpsys notification --noredact | grep -E 'sajdatime|mIntercept'
adb -s emulator-5556 shell dumpsys deviceidle unforce
adb -s emulator-5556 shell cmd notification set_dnd off
```

```bash
# Rotation. Always confirm it landed before screenshotting — the setting takes a
# few seconds, and capturing too early reads the previous orientation.
adb -s emulator-5556 shell settings put system accelerometer_rotation 0
adb -s emulator-5556 shell settings put system user_rotation 1   # 0 = portrait
adb -s emulator-5556 shell dumpsys window | grep -m1 -oE 'mRotation=[0-9]+'
```

```bash
# Read the screen as text rather than guessing from a picture. This is the single
# most useful command here: it says what is actually laid out and visible.
adb -s emulator-5556 shell uiautomator dump /sdcard/u.xml
adb -s emulator-5556 shell cat /sdcard/u.xml | tr '>' '\n' | grep -oE 'text="[^"]+"'
```

```bash
# Old form, kept because it still works on some images
adb -s emulator-5556 root
adb -s emulator-5556 shell "date 073021342026.40"   # MMDDhhmmYYYY.ss
# ...wait, then check it landed:
adb -s emulator-5556 shell dumpsys notification --noredact | grep "pkg=com.sajdatime.app"
adb -s emulator-5556 shell settings put global auto_time 1   # restore
```

> `am broadcast` **cannot** be used to test `PrayerAlarmReceiver`. It is correctly
> `exported="false"`, so the shell cannot deliver to it — the broadcast reports success and
> nothing happens. That is the app being secure, not a bug. Move the clock instead.

```bash
adb -s emulator-5556 emu geo fix -2.2426 53.4808     # Manchester (lng lat)
adb -s emulator-5556 shell run-as com.sajdatime.app cat files/datastore/sajdatime.preferences_pb | strings
```

Add the watch tile: long-press the watch face → **+** → scroll → "Next prayer".

---

### Isha in the UK — measured against three real mosques (1 Aug 2026)

**Why this exists.** A tester asked why his local mosques disagree about Isha, and named
three. Rather than reason about it, all three published timetables were read and compared
against Aladhan. The finding is not what the question assumed: **the mosques barely disagree
with each other. The app disagrees with all three of them.**

**What the mosques actually publish.** Slough is 51.51°N, Reading 51.45°N.

| Mosque | Date | Isha *begins* | Isha *jama'ah* |
|---|---|---|---|
| [JMIC Slough](https://www.jmicslough.org/) | 1 Aug 2026 | **21:58** | 22:15 |
| [Reading Mosque](https://www.readingmosque.com/) | 31 Jul 2026 | **22:02** | 22:20 |
| [Diamond Road, Slough](https://www.sloughislamictrust.org.uk/Diamond-Road-Mosque-Slough/) | 1 Aug 2026 | not published | 22:45 |

The two that publish a begin time **agree to within four minutes**. The apparent 47-minute
spread between JMIC and Diamond Road is not a disagreement about Isha at all — it is one
mosque printing the start of the window and another printing the congregation.

**Both begin-times were identified exactly.** Aladhan, same coordinates and dates:

| | JMIC Slough (printed) | Moonsighting + Shafi'i | Reading (printed) | Moonsighting + Hanafi |
|---|---|---|---|---|
| Fajr | 03:39 | 03:38 | 03:39 | 03:38 |
| Dhuhr | 13:14 | 13:09 | — | — |
| Asr | **17:19** | **17:19** | **18:28** | **18:28** |
| Maghrib | 20:53 | 20:51 | 20:57 | 20:54 |
| Isha | 21:58 | 21:59 | 22:02 | 22:03 |

Both mosques use **Moonsighting Committee Worldwide** — a *shafaq*-based convention built
for high latitudes, not a fixed depression angle. They differ from each other only in Asr
madhab (JMIC standard, Reading Hanafi), and each adds two to five minutes of *ihtiyat*
(safety margin) to the published begin time. Asr matched to the exact minute in both cases,
on a value that differs by 67 minutes between the two madhabs — so this is an identification,
not a coincidence.

**Where SajdaTime sits.** The default is `AUTO` → Muslim World League, Isha **17°**:

| Convention for Slough, 1 Aug 2026 | Isha |
|---|---|
| Moonsighting Committee (what both mosques use) | **21:59** |
| ISNA (15°) | 22:58 |
| Muslim World League (17°) — **the app's default** | **23:17** |
| Egyptian (17.5°) | 23:21 |
| Karachi (18°) | 23:25 |

**The app's default is 78 minutes later than every mosque in the tester's town**, and is the
latest-but-two of the mainstream conventions. Nothing is arithmetically wrong — MWL at 17°
is correctly implemented and matches Aladhan's MWL — but at 51.5°N the choice of convention
matters far more than the arithmetic, and MWL is a poor fit for the UK.

**The high-latitude rule is not the cause, and was ruled out rather than assumed.** On
1 August, `TWILIGHT_ANGLE` and no rule at all produce identical times (Isha 23:17 either
way), because at 51.5°N in August the sun still reaches 20° below the horizon. The rule only
engages roughly **mid-May to late July**, when 18° is unreachable. Checked at the solstice
for Slough, where the choice becomes enormous:

| Rule, 21 Jun 2026, Slough | Fajr | Isha |
|---|---|---|
| `TWILIGHT_ANGLE` — what the app uses | 02:33 | 23:29 |
| `MIDDLE_OF_THE_NIGHT` — adhan's default | 01:04 | **01:04** ← no Isha window at all |
| `SEVENTH_OF_THE_NIGHT` | 03:42 | 22:27 |

This re-confirms §5.5 from a second direction: `MIDDLE_OF_THE_NIGHT` is unusable here, and
setting the rule explicitly was right. It also shows `SEVENTH_OF_THE_NIGHT` lands much
closer to UK mosque practice in midsummer — worth knowing before anyone changes it.

**What a UK user can do today, with no code change.** Settings → Calculation method →
**Moonsighting Committee**, and Madhab → whichever their mosque follows. That reproduces
both mosques' begin times to within a minute. `CalcMethod.MOON_SIGHTING` already exists and
already ships; it is simply not discoverable, because onboarding asks for sect and madhab
and never mentions method.

**Not verified:** whether these mosques use Moonsighting year-round or switch convention in
midsummer — a single date cannot show that, and it is exactly where high-latitude timetables
tend to diverge. Two dates in December and June would settle it. Diamond Road's begin times
are not published anywhere found, so only its jama'ah column could be read.

### Location precision — what it is actually worth, in minutes (1 Aug 2026)

**Why this exists.** The same tester reported that automatic location shows *"Berkshire"*
rather than his town, and that the times therefore felt "not close enough". The second half
of that does not follow, and it was measured rather than argued, because the obvious fix
(more precise location) would have bought nothing.

Aladhan, Moonsighting, 1 Aug 2026, moving away from Slough:

| Position | Fajr | Maghrib | Isha |
|---|---|---|---|
| Slough, exact | 03:38 | 20:51 | 21:59 |
| 3 km north | 03:38 | 20:51 | 21:59 |
| 3 km west | 03:38 | 20:51 | 21:59 |
| Reading — 26 km | 03:40 | 20:52 | 22:00 |
| Newbury — 60 km, the far side of Berkshire | 03:41 | 20:53 | 22:01 |

**A 3 km error costs nothing at all — the times are identical to the minute.** Being wrong
by the entire width of the county costs two to three minutes. Coarse location is therefore
not merely adequate, it is comfortably more precise than the app needs, which confirms the
`ponytail:` note in `LocationRepository`. The arithmetic behind that: one degree of longitude
is four minutes of solar time, and at 51.5°N one degree spans about 43 km, so a kilometre is
worth roughly **five seconds**.

**So the reported inaccuracy is not location, it is the calculation method** — see the Isha
section above, where the default convention is 78 minutes out. Fixing location precision
would have moved the times by zero and left the complaint exactly where it was. This is the
whole reason it was measured first.

**The label is still a real defect, for a different reason.** `LocationRepository.cityName`
takes the first field that is non-null and stops:

```kotlin
?.let { it.locality ?: it.subAdminArea ?: it.adminArea ?: it.countryName }
```

Two consequences. It can never produce *"Slough, Berkshire, UK"* — only ever one field. And
seeing *"Berkshire"* means `locality` came back **null**, so it fell through to the county;
Slough is a unitary authority and geocoder coverage of those is uneven. `subLocality` and
`featureName` are not in the chain at all, and either often survives when `locality` does not.

**The harm is trust, not accuracy.** A user shown their county assumes the app does not know
where they are and stops believing the times — which are, in fact, correct to the minute. So
this is worth fixing, but it must not be sold as a fix for wrong times, and the app should
not imply that a more precise location would change them.

---

## 11. ⚠️ Still pending — the honest list

### Blocker for release

> **Read this first — 1 Aug 2026.** **Google approved it, and the app is live on the closed
> testing track.** The review came back inside a day. `Closed testing - Alpha` is **Active**
> with `1.1.0 - first closed test`, *"Available to selected testers"*, released 31 Jul 22:17
> across all 177 countries.
>
> **The whole project now rests on one number.** The Dashboard reads **`9 testers currently
> opted in`** against 17 addresses on the list, and Google requires twelve, held for
> fourteen *consecutive* days. **The fourteen days are counted while twelve or more are
> opted in — so at nine the clock is not running at all.** Days spent at nine are not early
> days of the fortnight; they are days that do not count.
>
> **Nothing in the Console needs pressing, and nothing in this repo is blocking.** The only
> remaining work is human: getting three more people to open the opt-in link. The count
> lives in exactly one place — **Dashboard → Production → "Apply for access to production"**
> — and nowhere else in the Console. See `docs/RELEASING.md` Step 8 and "What is genuinely
> blocking, right now".
>
> The two paragraphs below are kept because their account and listing facts remain true.
> Where the first says the signing key is the only outstanding item, and that nothing is
> waiting on Google, both are out of date — the key was created on 31 Jul and Google has
> since reviewed and approved.
>
> **Play Store status at a glance.** Developer account created (personal, ID
> 6284685113064492750, developer name "Ali Imran Khan"). Android-device check **passed**.
> Privacy policy **live** at <https://aliimrankhan86.github.io/SajdaTime/privacy.html>. All
> store assets and listing text **done** in `docs/store/`. **Account verification is fully
> complete** — identity documents approved, contact phone verified, device check passed.
> Nothing is waiting on Google. **The signing key is the only outstanding item, and it is
> the owner's alone.** After that: create the listing, then the 12-testers-for-14-days
> closed test, which is the long pole.
>
> **App created in the Console** (app ID 4975578035662443727, `com.sajdatime.app`, en-GB,
> free). **All App content declarations are complete and green** — privacy policy, app
> access, ads, content rating (IARC), target audience (13+, deliberately *not* under-13:
> any under-13 bracket pulls the app into Families policy), data safety, news, government,
> financial features, health, advertising ID. Data safety declares **approximate location,
> collected, not shared, App functionality only, users can choose, not processed
> ephemerally** — see `docs/store/LISTING.md` for why each of those is the answer, and in
> particular why "processed ephemerally" is a trap. Store listing text and graphics **are now
> entered and saved** (31 Jul 2026): app name 29/30 characters, short description 78/80,
> full description, icon 1/1, feature graphic 1/1, and 5 of 8 phone screenshots, with the
> listing showing *Ready to send for review*. Tablet and Chromebook slots are deliberately
> empty — they are optional, and untested layouts should not be advertised. The source
> assets remain laid out one folder per Console box in `docs/store/upload/`.

1. ~~**The app is unsigned, and only the owner can change that.**~~ **Closed 31 Jul 2026.**
   The owner created `~/sajdatime-upload-key.jks` himself and wrote `keystore.properties`;
   an agent confirmed only that both files exist, with the property *values* masked, and has
   never seen the key or the password. `./gradlew clean test lint :app:bundleRelease
   :wear:bundleRelease` now runs `:app:signReleaseBundle` and `:wear:signReleaseBundle` and
   both bundles carry `META-INF/SAJDATIM.RSA`. Play App Signing is on, so the upload key is
   recoverable if lost. **An agent must still never generate, hold, or see it.**
   `keystore.properties`, `*.jks` and `*.keystore` remain gitignored.

   The owner pressed **"Submit 15 changes for review"** on 31 Jul 2026; Play's automated
   pre-checks passed with nothing flagged, and **Google approved it inside a day**. The app
   went live on the closed track at 31 Jul 22:17. What remains is entirely outside the
   repo: twelve testers opted in, held for fourteen consecutive days, then the
   production-access application. See `docs/RELEASING.md` "What is genuinely blocking".

   One consequence worth knowing, because it unblocks the watch: publishing to a track
   satisfied the gate on **Advanced settings → Form factors**, which now offers
   *+ Add form factor*. It was deliberately not clicked — see the note at the end of
   `docs/RELEASING.md`.

### Tester feedback from the closed test — queued, deliberately not started

> **Short version for the owner: [`docs/AFTER_THE_TEST.md`](AFTER_THE_TEST.md).** Same three
> items in plain language, in build order, with the exact user-facing wording already
> decided and the one decision that needs the owner marked. This section is the technical
> record behind it; that file is what to read first.

> **Do not act on these until the closed test is over.** Recorded 1 Aug 2026, while the test
> was still at 9 of 12 opted-in testers. Shipping a new build mid-test means another review
> and another chance to disturb a run that has not yet started its fourteen days; the upside
> of a fix does not outweigh that until production access is granted. Both items below are
> analysed and evidenced, so the work can start immediately once the gate clears.

- **T1 — Isha does not match UK mosques, and the fix already exists but is invisible.**
  Full evidence in §10, "Isha in the UK — measured against three real mosques". The default
  `AUTO` → Muslim World League puts Isha **78 minutes later** than all three of the tester's
  local mosques, which use Moonsighting Committee. `CalcMethod.MOON_SIGHTING` already ships
  and already fixes it, but **onboarding never asks for a calculation method** — it asks for
  sect and madhab only (`Step.WELCOME, PERMISSION, SECT, MADHAB, CONFIRM`), so a user who
  never opens Settings can never reach it.

  Options considered, none yet chosen — **this is the owner's call, because it is a
  religious question and not an engineering one**:

  | Option | Effect | Cost |
  |---|---|---|
  | Leave the default; explain in Settings | App keeps a defensible mainstream convention | None; users stay mismatched |
  | Per-prayer ± minute offset in Settings | Solves *every* variant — convention, *ihtiyat*, jama'ah — without the app adjudicating fiqh | Moderate; new setting, new persistence, watch sync |
  | Add a method step to onboarding | Makes the existing fix reachable | Small; one more screen on a flow already too long |
  | Change the default by latitude | Fixes it for everyone silently | **Rejected on sight** — the app would be picking a madhhab-adjacent position for the user, invisibly |

  A per-prayer offset is the strongest candidate: it is what mature prayer apps ship, it
  needs no religious ruling from us, and it also absorbs the *ihtiyat* margins the mosques
  add. It does not remove the need to make the method discoverable.

- **T2 — the first-run flow advances on tap in some steps and needs a button in others.**
  Tester report: users do not know whether to tap or to press Next. Confirmed by reading
  `ui/onboarding/OnboardingScreen.kt`: `WELCOME` ends in a **Begin** button and `PERMISSION`
  in a **Continue** button, but `SECT` has **no button at all** — `ChoiceCard.onClick` calls
  `onSelect`, which sets `step` and jumps straight forward. `MADHAB` does the same *while
  also* showing **Back** and **Skip** buttons at the bottom, which is the worst case: the
  screen visibly has buttons, so the user reasonably waits for a Next that does not exist,
  then taps a card and is thrown forward before deciding. The fix is consistency, not
  cleverness — every step gets an explicit forward button, selection only selects.

  Note the interaction with T1: if a method step is added, it lands in this same flow, so
  fixing the navigation first is the cheaper order.

- **T3 — automatic location shows the county, and the user stops trusting the times.**
  Reported as *"it just shows Berkshire and gives me not close enough timing"*. The second
  half is not true and it was measured before anything was planned — see §10, "Location
  precision". **A 3 km error changes no prayer time at all; the whole width of Berkshire is
  worth two to three minutes.** The times this tester saw were correct; the 78-minute error
  he was feeling is T1. **Do not fix this expecting the times to move.**

  What is genuinely wrong is the label. `LocationRepository.cityName` returns a single field
  (`locality ?: subAdminArea ?: adminArea ?: countryName`), so it can never say *"Slough,
  Berkshire, UK"*, and when `locality` is null — common for unitary authorities like Slough —
  it silently degrades to the county with no indication that it has.

  The work, smallest first:

  1. **Add `subLocality` and `featureName` to the chain**, before falling back to the county.
     One line, and it is the most likely reason the town went missing.
  2. **Build a composite label** — town first, then a broader area, e.g. *"Slough, United
     Kingdom"*. Comma-join whatever fields survive rather than showing one.
  3. **One-line explanation under the location**, as the tester asked for. It must be
     honest about what precision buys: something on the lines of *"Approximate location is
     enough — prayer times change by under a minute within a few kilometres."* This closes
     the complaint properly, because the user's real worry is that the app is lost.
  4. **Make manual city entry obvious.** It already exists and already works — `CityLookup`
     tries the platform geocoder then Open-Meteo, and Settings already wires `onSearchCity`
     — but a user who distrusts the automatic label has no visible cue that they can simply
     type their town. Surfacing the existing feature is worth more than any new one.

  ponytail: nothing here needs a new dependency, a new permission, or `ACCESS_FINE_LOCATION`.
  Fine location would be a privacy regression bought for zero minutes of accuracy, and it is
  ruled out — see §8 and the hard rules in `CLAUDE.md`.

### Not done, in rough priority order

1. **One light frame at cold start when the theme is overridden.**
   `android:windowBackground` is a resource, resolved by the system from the *system's*
   night setting before any app code runs, so a user who has chosen Dark on a light phone
   sees a single pale frame before Compose draws. Fixing it means writing the window
   background from the activity once settings load, which trades one flash for a flash in
   the other direction. Left deliberately; revisit only if someone actually complains.
2. **No instrumented UI tests.** Logic and colour are unit-tested; every screen on both
   phone and watch is verified by hand only. A Compose UI test suite would make refactoring
   much safer. The one automated piece of layout checking that exists is
   `tools/wear-round-check.py`, driven by `tools/wear-verify.sh` — it settles "nothing
   behind the bezel" on both watch sizes but needs a booted emulator, so it is a release
   check rather than part of `./gradlew test`. If this is ever taken further, Google's own
   recommendation is Roborazzi with Robolectric, which runs Wear screenshot tests on the
   JVM across the whole device matrix with no emulator at all. That would be the first
   thing to add if the watch UI starts changing often.
3. **The watch has no calculation-method setting of its own.** Sect and madhab can now be
   set on the wrist (see the note under item 7), but `CalcMethod` still only arrives from
   the phone. That matters far less: `AUTO` resolves a sane convention from the sect, and
   unlike the madhab it does not move a prayer by an hour.
4. **Times display in the phone's timezone, not the chosen city's.** Correct for the
   traveller the feature is built for (their phone follows them), confusing for someone
   checking a distant city from home. Decide whether to show the city's local time, or
   label the timezone explicitly.
5. **High-latitude rule is not user-selectable.** `TWILIGHT_ANGLE` is a good default and
   matches Aladhan, but some UK mosques publish one-seventh-of-the-night times. If users
   report a mismatch with their local mosque, **exposing this setting is the first thing to
   add.**
6. **No per-prayer manual offsets.** Some communities apply a few minutes' adjustment. The
   data model would take it easily.
7. **The phone↔watch Data Layer sync has never been observed working.** Both sides are
   implemented against the shared `WatchSyncContract` and unit-tested, but the two emulators
   were never actually paired, so a real phone→watch settings push is **unverified**. The
   phone emulator has no Wear companion app installed, which is why: `dumpsys` on the watch
   reports `0 connected out of 1`. Pairing needs a Play-services phone image plus the Wear
   OS companion app, and is the one setup step that would let this finally be tested.

   **What this cost, and what was done about it.** Running both emulators side by side
   showed the watch reporting Asr at 5:28 PM while the phone, set to Hanafi, said 6:37 PM.
   Every other time matched to the minute. The watch was not wrong — it was calculating
   correctly for its own default, Shafi'i, because the phone's setting had never reached
   it. On a watch declared `standalone=true` that is a silently wrong prayer time with
   nothing on screen to hint at it. So the watch now shows the school it is using as a
   button at the foot of the times list, and that button opens a sect and madhab picker.
   An unsynced watch is now visibly, and correctably, unsynced. Verified on the emulator:
   switching to Hanafi moved Asr to 6:37 PM and every time then matched the phone exactly.
8. **No home screen widget** and **no tablet-optimised layout**.
9. **No bundled adhan audio** (deliberate: licensing + tens of MB). Alarm mode plays a tone
   the user picks.
10. **City search needs a network** unless the phone's own geocoder can answer offline.
11. **Localisation is ready to receive translations, but none exist yet.**

    Every user-facing string is now a resource, including the ones that were hardest to
    move: prayer names and calculation-method names used to be English constants on the
    `PrayerSlot` and `CalcMethod` enums, shown directly on the main screen, the watch, the
    tile, the notifications and the PDF. They are resources in `:core` now, so the phone
    and the watch share one set. The PDF's own headings went with them.

    Nothing is concatenated in code either — the two places that had drifted (onboarding's
    `"Sunni · Hanafi"` and the location row's spoken description) now go through resources,
    so a translator owns the separators and the punctuation as well as the words.

    **Adding a language is a file drop, no code change:** create
    `app/src/main/res/values-<lang>/strings.xml` and
    `core/src/main/res/values-<lang>/strings.xml`. Android picks it up from the phone's
    language automatically — the user is never asked to choose, which is correct. The one
    non-obvious requirement is that the `:core` file **must** declare `app_language_tag`
    with its own BCP-47 tag, or the translation will be shown with English number and date
    formatting (§5.11). `LocaleDisciplineTest` fails the build until it does, which is the
    intended way to find out.

    Two things follow the *first* translation, and only then:
    - `android:localeConfig` on the manifest plus `res/xml/locales_config.xml`, which gives
      Android 13+ a per-app language picker in system Settings for free. Adding it now with
      only English listed would show a picker with one entry.
    - An in-app language row, for phones below Android 13, which have no system picker.

    **Previewing RTL before a translation exists:** `./gradlew installRtl`. The `rtl` build
    type is `debug` plus one file, `app|wear/src/rtl/res/values/strings.xml`, which
    overrides `app_language_tag` with `ar-XB`. The whole app then runs right-to-left with
    Arabic date conventions on an otherwise untouched device — the same flip that happens
    by itself the day `values-ar/` ships. It previews layout, not words: every string stays
    English. §10 has what it found the first time it was run.

    ⚠️ **Do not machine-translate this app.** Prayer names, madhab names and the disclaimer
    are religious content, and Indonesian or Urdu speakers expect "Fajr", not a local
    translation of "dawn". `core/src/main/res/values/strings.xml` carries a note to
    translators saying so. Each language needs a native speaker to review it before it
    ships. Arabic and Urdu are the obvious first two; both are RTL, and `supportsRtl="true"`
    has now been exercised — see §5.11 and §10. What has been shown is that an English-only
    app on a right-to-left *device* behaves correctly. An RTL *translation* is still
    untried, because none exists.
12. **No CI.** No GitHub Actions workflow; all verification is run locally.
13. **Play Store listing assets are done.** `docs/store/` holds the 512 × 512 icon, the
    1024 × 500 feature graphic, five phone screenshots, two Wear screenshots, and
    `LISTING.md` with every field of Console text (name, short and full description,
    category, data safety answers) already within Google's character limits. The privacy
    policy is **live** at <https://aliimrankhan86.github.io/SajdaTime/privacy.html> (GitHub
    Pages, serving `main` / `/docs`), with a landing page at the site root.

    The Play developer account **exists**: personal, ID 6284685113064492750, developer name
    "Ali Imran Khan", public developer email `aikstudies@gmail.com` (deliberately separate
    from the private contact address `aliimrankhan86@gmail.com`), declared as **not earning
    money** — which is what keeps the street address off the public listing. Google shows
    only legal name, country and developer email.

    The **Android-device verification has passed**. Worth remembering for any future
    account: it requires the Play Console app on a *physical* phone and an emulator does not
    satisfy it — the one step in this whole project that cannot be done on an emulator.

    **All account verification has now cleared** — identity documents approved, contact phone
    verified, and **Android developer verification** (the 2025–26 programme covering apps
    installed outside Play) registered automatically, which the Console confirms in as many
    words. Nothing on Google's side is outstanding. See `docs/RELEASING.md` for the account
    facts and the full checklist.

    **The app now exists in the Console** (app ID `4975578035662443727`). Every **App
    content** declaration is complete and green — privacy policy, app access, ads, content
    rating, target audience, news, COVID-19, data safety, government, financial, health and
    Advertising ID. The **Default store listing is saved as a draft** with the name, both
    descriptions, the icon, the feature graphic and all five phone screenshots assigned.

    Two Console behaviours cost time and are worth knowing:

    - **Ticking an asset in the library does not assign it.** You must click **Add** at the
      bottom-right of the side panel. Until you do, the field stays empty and the page
      refuses to save with "Upload an icon" — while the asset you ticked sits visibly on
      screen. Old uploads also stay in the library forever; only *assigned* assets publish,
      so seeing superseded art in the picker is not a caching bug.
    - **"Some languages have errors" is not about translation.** Only en-GB exists; it is a
      roll-up of that one language's own missing-field errors.
    - **The asset library has no delete, and this was checked, not assumed.** Every asset row
      offers exactly three actions — Crop, Preview, View details — confirmed by reading the
      page's accessibility tree, not by looking for a button. So superseded uploads accumulate
      permanently and cannot be tidied up by anyone. This matters more than it sounds:
      re-uploading a corrected asset leaves the **older, wrong version sitting at the top of
      the picker under an identical filename**, and the thumbnails are too small to tell
      apart. Always use **Upload** to push a fresh file; never re-pick by name from the
      library. Clutter itself is harmless — only *assigned* assets publish.
    - **The picker greys out assets with a 🚫 badge when they do not fit the field it was
      opened from** — 9:16 phone screenshots look "broken" in the feature-graphic picker and
      are not. It is eligibility, not corruption.
    - **The promotability notice is blue, advisory, and re-evaluated on save.** It cost two
      rounds: `Free.` first, then `No ads`. It does not block publishing — it costs
      eligibility to be featured — so it is easy to scroll past, and for an app with no
      marketing that would be the wrong thing to scroll past. Both words were removed from the
      feature graphic in the same commits, since the Console validates form fields and cannot
      read a PNG. Full reasoning and the rejected alternatives are in `docs/store/LISTING.md`.
    - **Tags save on `Apply`, immediately, with no confirmation** and without the page's own
      Save button being involved. Anything changed in that dialog is live the moment it
      closes. The tag list is also a *closed* set of 171 with no religion tag and no religion
      category — see §15 lesson 26.
    - **Save greys out when the form matches the server**, which makes it the cheapest
      possible check that an edit actually landed. It is also how a lost edit was caught: see
      §15 lesson 25.

    **The store listing is saved and clean, verified from a server reload**, not from the page
    as left. Name `SajdaTime: Prayer Times, Qibla` (30 / 30); short description `Offline prayer
    times (namaz) and Qibla compass for Sunni and Shia. No tracking.` (79 / 80); full
    description 3,299 / 4,000; no field errors; **no promotability notice**, confirmed with
    `namaz` present, which establishes that a language word is not a promotion word. Store
    settings are done too — App, category **Lifestyle**, tags **Clock, alarm & timer ·
    Lifestyle · Maps & navigation**, contact email set.

    `docs/store/LISTING.md` now holds the live text **byte for byte**, proved by length and a
    djb2-xor hash computed on both sides rather than by reading them side by side. That file
    mirrors the Console; it never leads it.

    **The closed testing track is set up** — `Closed testing - Alpha`, all **177 countries
    targeted**, feedback email set to the store contact address. The countries list had been
    completely empty, which is worth knowing about because it fails in the worst possible way:
    every tester in the world hits *"app not available in your country"* on their own phone,
    while the Console shows a healthy-looking track and an opted-in count of zero. Closed
    tracks inherit production availability and production had never been set. Nothing warns
    you.

    **The next actual steps:** the signing key — owner-only, `docs/RELEASING.md` Step 2, and
    the one true blocker since both AABs on disk are unsigned — and, in parallel because it
    needs nothing from the key, recruiting testers. The tester email list is the only part of
    track setup left, and it needs addresses nobody but the owner has. The Wear OS screenshot
    slot only appears once the Wear form factor is added, which happens at release time; the
    454 × 454 files are already waiting in `docs/store/upload/wear-os/`.

### Deliberate non-goals — do not "fix" these
Ads, in-app purchases, accounts, analytics, crash reporting, fine location, background
location, cloud backup, a server of any kind.

**Also: features the dark design system deliberately leaves out.** The design it came from
was drawn against the shipping feature set on purpose — no prayer tracker, no day stepper
on the Times screen, no per-row notification mute. They are absent from the mockups because
they are absent from the app, not because they were forgotten. A future design pass should
stay inside the feature set unless the feature itself has been agreed first.

**Also: the tablet and Chromebook store screenshot slots stay empty.**
`docs/store/upload/tablet-7in/`, `tablet-10in/` and `chromebook/` contain a README and
nothing else, and that is the finished state, not a gap. Play marks those boxes with an
asterisk, but the error text underneath reads *"Upload at least 2 phone **or** tablet
screenshots"* — one requirement shared across all of them, already satisfied by the five
phone screenshots. They are empty because there is no large-screen layout yet; filling them
with phone captures would advertise a tablet experience that does not exist. The only cost
is that Play will not surface the app in tablet and Chromebook recommendations, which is
the correct outcome until the layout is real. Fill them when it is, and not before.

---

## 12. Development environment

This is what the machine the project was built on actually has. Adjust paths for a
different machine.

| Thing | Value |
|---|---|
| Android SDK | `/Users/aliimrankhan/Library/Android/sdk` (in `local.properties`, **gitignored**) |
| JDK | Oracle Java SE 21 — `JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| Phone emulator | AVD `sajda`, API 36 |
| Watch emulator | AVD `sajdawear`, Wear OS API 34, `arm64-v8a`, `wearos_small_round` — 384px @ 320dpi = **192dp** |
| Watch emulator, large | AVD `sajdawear_large`, same image, `wearos_large_round` — 454px @ 320dpi = **227dp**. Created on demand by `./tools/wear-verify.sh`. Both sizes must be run; the app was wrong on both in ways only visible with them side by side |
| Repo files tracked | 116 |
| Kotlin source | ~8,650 lines across the three modules |

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"

$ANDROID_HOME/emulator/emulator -avd sajda     -no-snapshot-load -no-audio &
$ANDROID_HOME/emulator/emulator -avd sajdawear -no-snapshot-load -no-audio &
adb devices     # phone tends to be 5556, watch 5554 — confirm, do not assume
```

`local.properties` is not committed; a fresh clone needs its own with `sdk.dir=...`.

### The original requirements

The user's own written brief — the source of the vision, the disclaimer wording, the Makkah
fallback, the Qibla requirement and the Wear OS ask — is at:

```
/Users/aliimrankhan/Documents/SajdaTime-Docs/SajdaTime.md
```

It is **outside the repo** and not tracked. Worth reading before making product decisions,
since it is the closest thing to a spec written by the owner rather than inferred.

---

## 13. Build traps

Every one of these cost real time. Read before building.

| Trap | Symptom | Fix |
|---|---|---|
| **iCloud** | `Failed to process: .../compileDebugKotlin/classes`, stray `Foo 2.class` | Keep the project out of `~/Documents` / `~/Desktop`. Bit the project 3×. |
| **`JAVA_HOME` unset** | `sdkmanager` reports exit 0 but installs nothing; "This tool requires JDK 17 or later" | `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew ...` |
| **AGP 9** | "kotlin-android no longer required since AGP 9.0" | Do **not** apply `org.jetbrains.kotlin.android`. AGP 9 has built-in Kotlin support. The catalog still declares the plugin but nothing applies it. |
| **AGP 9** | `kotlinOptions.jvmTarget` is an error | Use `kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }` |
| **`com.android.library`** | "already on the classpath with an unknown version" | Root `build.gradle.kts` must declare `alias(libs.plugins.android.library) apply false` |
| **Dexing OOM** | Out of memory during dexing | `org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m` (already set) |
| **`androidx.fragment`** | Lint fatal `InvalidFragmentVersionForActivityResult` | `play-services-wearable` drags in a 2019-era fragment. Pinned to 1.8.9 in **both** `:app` and `:wear`. |
| **Tiles + coroutines** | `CompletableFuture` vs `ListenableFuture` mismatch | `kotlinx-coroutines-guava` + `kotlinx.coroutines.guava.future` |
| **`org.json` in unit tests** | Every call throws "Stub!" | `testImplementation(libs.json)` puts a real implementation on the test classpath |
| **Name clash** | `android.provider.Settings` vs `Icons.Outlined.Settings` | `import android.provider.Settings as SystemSettings` |
| **`avdmanager`** | Cannot see system images through a symlinked `cmdline-tools` | Copy `cmdline-tools/latest` into `$ANDROID_HOME` as a real directory |
| **Unscoped `installDebug`** | `INSTALL_FAILED_VERSION_DOWNGRADE: Update version code 2 is older than current 1000`, or the phone emulator silently running the watch build | Both modules share one `applicationId`, so the root task installs `:wear` **over** `:app`. Scope it: `:app:installDebug` / `:wear:installDebug`. To recover: `adb uninstall com.sajdatime.app`. To check which is installed: `adb shell cmd package resolve-activity --brief com.sajdatime.app`. See §15 lesson 28. |
| **Scoping the module is not enough with two emulators up** | Same `INSTALL_FAILED_VERSION_DOWNGRADE`, but now from `:app:installDebug`, which looks like it cannot possibly be the cause | Gradle installs to **every** connected device, so `:app:installDebug` also tries to put the phone APK on the watch emulator, where the watch build already sits at versionCode 1000. Scope the device too: `ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug`. Bites exactly when you follow this project's own advice to run both emulators together. |
| **`sed -i.bak` inside `res/`** | `Resource and asset merger: The file name must end with .xml` | The backup file is itself a resource. Edit resources with a tool that does not drop siblings, or write the backup outside `res/`. |

### The architecture PDF is generated, never hand-made

```bash
./tools/build-architecture-pdf.sh
```

Renders `docs/ARCHITECTURE.md` → `docs/SajdaTime-Architecture.pdf` (needs `node` for
`marked`, and Google Chrome for headless print). The first release shipped a PDF a full
version behind the markdown because it was made by hand once and never again. **Run the
script after editing the markdown.**

---

## 14. Git history

| Commit | What |
|---|---|
| `41fc109` | Initial commit |
| `ac12c85` | v1.0.0: offline prayer times, notifications, PDF export |
| `f5bb071` | Qibla compass, Wear OS app, alarm mode, shared `:core` module |
| `30cb5eb` | Fixed the silently-wrong city search; first real run of the watch app; alarm mode verified on device |
| `6e91cdc` | Corrected two Play Store errors found by research; added 8 localised listings |
| `1792d0a` | Closed two compliance gaps found in research; recorded the non-risks |
| `e693fa1` | Fixed 13 defects found in a full ship-readiness audit |
| `6a6dbcd` | Made the app translatable, reorganised Settings, asked for duas once |
| `985adc8` | Fixed nine defects found by running the app rather than reading it — including the watch calculating Asr with the wrong madhab |
| `f49dc5d` | Recorded the Aladhan runtime decision, the session's lessons, and the commit rule |
| `6e57e49` | Applied the dark design system to both apps; added the watch-accuracy paragraph to the disclaimer |
| `6a16bd1` | Applied the light design system and added the Follow phone / Light / Dark setting |
| `0e43fe5` | Audit: fixed a stale inverse colour and a stale architecture palette, closed two Wear checks |
| `2a13b9d` | Ran the 227dp watch for the first time; fixed the school button printed under the watch face clock, and added `tools/wear-verify.sh` so both round sizes are checked from now on |
| `d9e31e3` | Brought the handover's own bookkeeping up to date |
| `7d9cf89` | Put the Kaaba on the Qibla dial, on both the phone and the watch, so the screen answers "which way" without being read |
| `eb24f54` | Recorded the Kaaba commit's own hash in this table |
| `a5cf7fa` | Made the app format in its own language rather than the device's, after running it on Arabic and Bengali phones and finding a scrambled Hijri date, an unreadable Qibla sentence and two numbering systems on one screen |
| `1defcdd` | Recorded the locale commit's own hash in this table |
| `93611c5` | Added `./gradlew installRtl`, so any layout can be checked right-to-left before a translation exists to check it with |
| `6f612ad` | Recomputed the day at midnight rather than only when a prayer passes — after midnight nothing passes for hours, so the "Today" list silently showed yesterday's times on the screen people use to break their fast |
| `5f628db` | Recaptured the store screenshots natively at 1080 × 1920 instead of pillarboxing 20:9 captures, and moved the launcher icon and store art off the pre-revision green they had been left on |
| `d9d1c9a` | Removed the price keyword from the short description **and** from the feature graphic, where the Console could not see it |

### The author identity changes partway through, on purpose

Commits up to and including `d9d1c9a` are authored as
`Ali Khan <Ali.Khan@partner.bmwgroup.com>`. Everything after is
`Ali Imran Khan <4550455+aliimrankhan86@users.noreply.github.com>`.

Same person. A global `git config` on the owner's Mac was still set to a former employer's
address, so it had been signing every commit in every personal repository — 26 of the 29
here. It was corrected at the machine level, which fixes all future commits everywhere.

**History was deliberately not rewritten**, and should not be: it would change every SHA,
and this document, `RELEASING.md` and several commit messages all cite hashes by name. The
old address is inert — it is a text label recorded at commit time, it authenticates nothing,
and it was never registered on the GitHub account (commits carrying it show as an unlinked
author, which is also why they earn no contribution credit). If reclaiming that credit ever
matters more than the stable hashes, that is the trade being made.

---

## 15. If you are an agent picking this up

1. Read `docs/ARCHITECTURE.md` §3 before touching `:core`. The Jafari Maghrib rule and the
   high-latitude rule are subtle and both were wrong at some point.
2. Run the §9 verification command before and after any change.
3. **Verify against an independent reference, not against your own reasoning.** Every real
   bug in this project — the Nigeria coordinates, the Fajr/Isha collapse, the Qibla bearings
   — was caught by comparing to the Aladhan API. Three "bugs" that were confidently reported
   turned out to be errors in the *test* rather than the code, so check both sides.
4. **Compiling is not working.** The watch app compiled and linted clean for an entire
   release while being unable to ever obtain a location. Run it.
5. **Run the phone and the watch at the same time, and compare them.** The single worst bug
   ever found in this project — the watch showing Asr 69 minutes early — was invisible in
   either module alone, because *neither module was wrong*. Each was correct for the
   settings it held; the settings simply never crossed between them. A defect that only
   exists in the gap between two components cannot be found by reading either one.
6. **A fix you have not measured may be a no-op.** A Compose modifier cap was added to bound
   the Qibla dial in landscape. It compiled, it read correctly, it was reviewed, and it did
   absolutely nothing, because `fillMaxWidth()` pins `minWidth` and a `widthIn` placed after
   it can never win. Only screenshotting the running app caught it. Order is not cosmetic,
   and "the change looks right" is not evidence that it works.
7. **Adding a sentence to a string is a layout change.** The disclaimer gained one
   paragraph and became five. `AlertDialog` does not scroll its body for you, and both
   places that dialog appears passed a bare `Text` — so at a raised font size the last
   paragraph, the dua request, fell off the bottom. Nothing about that is visible in the
   diff. Screenshot the screen at `font_scale 1.5` after touching any string that grows.
8. **A visual change is not finished until `docs/store/screenshots/` is retaken.** Those
   images are a published listing. Recapture into `raw/` and re-run
   `tools/build-store-assets.sh`; the script reframes and copies, it does not re-shoot.
9. **The emulator will let you fake a device size, and the fake will lie to you.**
   `adb shell wm size 454x454` resizes the panel but not the rounded-corner overlay, which
   was computed once for the real one. The stale mask then paints straight vertical cuts
   through the text — and a round display cannot produce a straight vertical edge, so the
   capture is an artefact rather than a finding either way. The lesson generalises past
   Wear: when a shortcut reproduces a *shape the real hardware cannot make*, that is the
   shortcut failing, and the answer is the real profile. `wearos_large_round` had been in
   the SDK the whole time. Check `avdmanager list device` before inventing a workaround.
10. **The wrong screen size is not a cosmetic risk.** Running the 227dp watch for the first
    time immediately turned up a control printed underneath the system clock, unreadable,
    at the resting position users land on after a flick — and once found, it was there on
    the 192dp watch too. It had survived every build, every lint run, every unit test and
    an entire release. Two sizes of the same round screen disagree about what fits, and
    only one of them was ever being looked at.
11. **On a round screen, `screencap` shows you pixels the wearer will never see.** The
    capture is the framebuffer, taken before the corner overlay; the display is the circle
    inscribed in it. That asymmetry is what `tools/wear-round-check.py` exploits, and it is
    why "it looked fine in the screenshot" is not the same claim as "it fits on the watch".
12. **An automated check is worth exactly what it measures, and no more.** The bezel check
    settles "cut off by the screen edge" completely and says nothing at all about "overlaps
    something else", because the clock and the app are both just lit pixels. The defect that
    actually mattered was in the half it cannot see. Ship the check *and* look at the
    pictures; be precise in the docs about which half each one covers.
13. **A hole is not a colour.** The Kaaba mark's band and door were first cut out of a
    single even-odd path, on the reasoning that the dial face would show through and the
    mark would therefore need no second colour to keep above AA. The reasoning was sound
    and the result was wrong, because a hole does not show the background you were
    thinking of — it shows whatever happens to be behind, and behind this mark is the
    needle. The doorway filled with green. Any time transparency is standing in for a
    specific colour, ask what else can get underneath it.
14. **The same change can be right on one screen and wrong on the other.** Letting the
    needle end inside the Kaaba mark is correct on the phone, where the needle is long and
    its tip disappearing behind the building reads as an arrow arriving. On the watch the
    needle already floats clear of the centre readout, so the identical treatment left a
    stubby trapezoid that stopped reading as an arrow at all. Same code, same shape, same
    constants, opposite verdicts — and the only thing that could have told you is having
    both open at once. This is §5 again in a smaller key, and it will keep recurring.
15. **"We only ship one language" is a statement about words, and Android hears it as
    one.** Text comes from `values-*`; numbers, clock times, dates and layout direction
    come from `Locale.getDefault()`, which is the *device*. Nobody had ever launched this
    app on a non-English phone, because being English-only felt like the end of the
    question. It was the beginning of it: an Arabic phone rendered the Hijri date
    `١٤٤٨ Safar ١٧` — the right numbers in the wrong order, reading as a different date
    rather than as a broken one — and turned the Qibla sentence into
    `km ٤٨٢٢ from true north. The Kaaba is about ١١٨°` with the full stop stranded on its
    own line. Bidi was behaving perfectly; it had been told the paragraph was Arabic while
    every word in it was English. If your app declares a language, make it format in that
    language too, and go and look at it in a language you did not plan for.
16. **The same platform question has two different answers depending on which context you
    ask.** `Resources.getString(id, args)` formats `%d` from the *configuration* locale and
    ignores which `values-` folder the string it just loaded came from, so passing the right
    `Locale` to `DateTimeFormatter` fixed the clock and left every `%d` on the Qibla screen
    exactly as broken as before. There is no argument for it — the configuration is the only
    lever. And pinning that configuration immediately broke something else: `is24HourFormat`
    falls back to the *locale's* convention when the user has never touched the toggle, so a
    pin to `en-GB` silently turned every time in the app into `13:10`. Two rounds of "fixed
    it" that were each only half a fix, and both halves were only ever visible on a running
    device.
17. **The two libraries that both format numbers do not agree.** `String.format` and
    `Resources.getString` go through ICU, where Bengali's zero is `০`. The desugared
    `java.time` carries its own CLDR data, where Bengali's zero is `0`. The result was a
    countdown reading `০২:১৮:১৯` directly above prayer times reading `2:50 AM`, on the same
    screen, from the same codebase, in the same locale. When two formatting paths exist,
    assume they disagree until a screenshot says otherwise.
18. **A test fixture that pretends to be a translation gets treated as one.** Restoring the
    RTL preview looked like a one-file job: a debug-only `values-ar-rXB/strings.xml`
    declaring the language tag. Two platform mechanisms disagreed. `aapt2` pseudolocalises
    the default strings at compile time and its generated value *beats* an explicit one, so
    the tag arrived as the pseudolocalised text `‏‮en-GB‬‏` and parsed to no locale at all —
    only visible by dumping the built APK's resource table, never from the source. And lint
    reads any `values-ar*` folder as an Arabic translation and reported all 154 strings as
    `MissingTranslation`; `tools:ignore` on that file does nothing, because lint raises the
    error against the *default* file, not the translation. Every remaining fix disarmed the
    one check that will actually matter the day a real, possibly incomplete `values-ar/`
    lands. The answer was to stop imitating a locale: a `rtl` build type has no locale
    folder, so there is nothing for either mechanism to misread. When a fixture keeps
    colliding with the tooling, it is usually pretending to be something it is not.
19. **Two things that always happen together are not one trigger.** The live clock
    recomputed when the next prayer passed, which covers every hour of the day except the
    ones nobody was looking at: after midnight the next prayer is Fajr, nothing passes for
    hours, and the "Today" list quietly stayed on yesterday — Maghrib two minutes late, on
    the screen people use to break their fast. What made it survive is that it looked
    right: the header had already been fixed for precisely this bug and keys off the live
    clock, so the screen showed today's date above yesterday's times. A partial fix to a
    bug class is more dangerous than none, because it removes the symptom that would have
    led someone to the rest of it. When you fix a staleness bug, go and find every other
    thing computed from the same stale input.
20. **Generated art inherits stale colours silently, because it still looks right.** The
    launcher icon sat on `#14624B` — the primary from before the light palette was revised
    for contrast — and `tools/build-store-assets.sh` copies its colours, so the store icon
    and the feature graphic were both a revision behind the app they advertise. Nothing
    failed. `ColorContrastTest` never saw it, because it asserts UI roles from `Color.kt`
    and the icon is a drawable with a literal in it. Lint never saw it. The only way to see
    it was to put the hex next to `LightPrimary` and notice they differ — and `Color.kt`
    had *already recorded the same drift* on `DarkInversePrimary` in a comment, which is
    the tell that this class of miss repeats. When a value is duplicated outside the type
    system, the duplicate is where the next stale value will be.
21. **A store rule that is "roughly X" is usually exactly Y, and the laxer version is the
    one you remember.** These notes said Play's screenshot rule was "the long side may not
    exceed twice the short side". The Console's own text says **16:9 or 9:16**, and nothing
    between. Under the remembered rule the 1080×2400 emulator captures merely needed
    padding; under the real one they are simply invalid, and every modern phone's native
    resolution is invalid with them. The pillarboxing that resulted was a workaround for a
    constraint that was never the constraint. Read the requirement text on the actual form
    before designing around it, not the version in your head.
22. **An emulator answers sensor questions with zero, and zero looks like an answer.** The
    Qibla store screenshot read *"You are facing 0°"* for an entire release cycle. It is
    not a state any real user reaches — it is the absence of a magnetometer, rendered as a
    perfectly plausible number. Feeding `adb emu sensor set magnetic-field` a real heading
    both fixes the picture and turns it into a free end-to-end check: facing 60°, Qibla
    118°, app says "Turn right 58°". If a screen is driven by a sensor the emulator does
    not have, assume the value is fabricated until you set it yourself.
23. **A validator that reads your text cannot read your picture, and the same mistake is
    usually in both.** Play flagged the short description for containing "Free." — a price
    keyword, which costs eligibility to be *featured* without blocking publication, so it
    appears as a blue advisory rather than a red error and is easy to scroll past. The same
    word was sitting in the feature graphic's subtitle, where the Console said nothing at
    all, because it validates form fields and the banner is a PNG. Fixing only the flagged
    field would have left the offending word in the one asset Play uses when it *does*
    promote an app. This is lesson 20 in a different medium: the automated check covers the
    text path and is blind to the generated-image path. When a rule is enforced on one
    representation of a value, go and find every other representation before calling it
    fixed — and read the advisories, not only the errors.

    **And then it happened again in the same field.** Removing "Free." did not clear the
    notice. Saving and reloading from the server brought the identical bullet back, which
    ruled out a stale render and proved the diagnosis had been incomplete rather than wrong:
    `No ads` is *also* a price-or-promotion keyword. It was in the banner too, so the same
    two-place fix had to be done a second time. Two things to carry forward. First, when a
    validator still complains after a fix, it is far more likely that the fix was partial
    than that the validator is broken — check by forcing a genuine re-evaluation (save,
    reload, re-read) before concluding anything about the tool. Second, an opaque check with
    an unpublished rule list is narrowed by **elimination**, so write the elimination order
    down *before* running it; `docs/store/LISTING.md` had already named `No ads` as the next
    suspect, which turned a second surprise into a second step.
24. **A screenshot that looks like a known bug is not evidence that you caused it.** The
    header date line rendered as `Safar 1448 17` under `installRtl`, which is precisely the
    reordering `AppLocale.kt` warns about, and the obvious conclusion was that adding a
    second date to that line had broken it. Stashing the change and rebuilding the previous
    version showed the baseline doing exactly the same thing. The fix that would have been
    written for a bug that was not there would have been real code, added permanently, for
    nothing. When a regression is suspected, spend the five minutes to build the version
    without it — a before-and-after is cheap and it is the only answer that is not a guess.
    The same applies in reverse: do not assume unchanged behaviour is correct behaviour.
25. **An edit staged into a web form is not saved work, and it can vanish between one message
    and the next.** A five-character correction was typed into the Console's full description
    and the on-page counter moved, which proves the framework saw it — and then a reload
    showed the old text at the old length anyway. Whatever discarded it, the edit had never
    existed anywhere but that page's memory. The signal that settles it costs nothing: **the
    Console greys out Save when the form matches the server.** A disabled Save plus the
    pre-edit character count is proof the edit is gone; an enabled Save is proof something is
    still pending. Check the count *and* the button state after every save rather than
    trusting that a save happened, and treat anything staged but unsaved as work that will
    have to be done again. The counter proves the form heard you. Only the server proves it
    kept it.
26. **Do not recommend values from a fixed list you have not read.** Play's five tag slots
    were filled in with `Prayer, Islam, Religion, Compass, Offline` — confident, obvious, and
    not one of them a tag that exists. Play offers a closed set of 171 and you cannot type
    your own; there is **no religion tag and no religion category** anywhere in it. The cost
    was the owner opening the dialog to find nothing he had been told to look for. Whenever a
    field is a picker rather than a text box, enumerate the options first and recommend from
    the actual list. Plausibility is not availability.
27. **A pseudolocale cannot answer the question a pseudolocale is used to ask.** `ar-XB`
    reverses Latin characters, which no real language does, so under it every English run
    looks equally broken and there is no way to tell which ones would survive a genuine
    translation. Two claims in §10 had been resting on that — the meridiem "will localise on
    its own", the Hijri date "will reorder correctly in Arabic" — and both were argument, not
    evidence. Pointing the same build type at a **real** RTL locale with no translation
    (`ur`, chosen because it is RTL *and* keeps a Latin `AM`/`PM`) settled both in about two
    minutes, and part of the answer was the opposite of what it looked like: `AM 2:54` is the
    *correct* rendering, because an RTL reader starts at the right and therefore reads `2:54`
    first. The English-looking "fix" would have been the bug. When a test harness deliberately
    exaggerates, it is telling you where to look and not what you will find — go and get the
    unexaggerated case before concluding anything, and revert the pin afterwards, because
    `ar-XB` being un-settable is the property that stops the `rtl` build type from ever being
    mistaken for a translation.
28. **`./gradlew installDebug` at the root installs the watch app over the phone app.** Both
    modules share one `applicationId` — which is required, it is how Play pairs a Wear app
    with its phone app — so on any single device they are the same package and the last one
    written wins. The watch module is written last, its `versionCode` is 1000 against the
    phone's 2, and the next `installDebug` then dies with
    `INSTALL_FAILED_VERSION_DOWNGRADE: Update version code 2 is older than current 1000`. The
    phone emulator sits there running the watch build, which on a phone-shaped screen is not
    obviously the wrong app. Always scope the task — `:app:installDebug`, `:wear:installDebug`
    — and if a device is already in that state, `adb uninstall com.sajdatime.app` first.
    `adb shell cmd package resolve-activity --brief com.sajdatime.app` names the launcher
    activity and settles which of the two is actually installed in one line.
29. **Read the device's persisted state before believing a cross-module difference.** The
    watch showed Asr 68 minutes off the phone — the exact Hanafi/Shafi'i gap, in exactly the
    place this document says the project's worst bug once lived. Everything pointed at a real
    defect. It was a `madhab = HANAFI` left in the watch's DataStore by an earlier session's
    manual testing; `pm clear` and a relaunch matched the phone on all six times. An emulator
    is not a clean room, it is a machine somebody has already been poking at, and its saved
    state is an input to your test whether you meant it to be or not. Dump the store
    (`run-as <pkg> cat .../datastore/*.preferences_pb | xxd`) before you write the bug up.
    That is four of five "confirmed bugs" in this project now traced to the test, not the
    code — and the one real defect this same session found was in a place nobody suspected.
30. Keep the ponytail discipline: stdlib and platform first, no speculative abstractions,
    shortest working diff. Mark deliberate simplifications with a `ponytail:` comment.
31. The owner is **not technical**. Explain in plain language, state what is verified versus
    assumed, and never present something as done when it is untested. He also has **no
    access to physical devices** — if you cannot test it on an emulator, say so plainly
    rather than suggesting he go and try it himself.
32. **When you commit, commit the understanding too** — see `CLAUDE.md` at the repo root.
    Code alone loses the reasoning, and the reasoning is what stops the next session
    undoing a decision it does not know was deliberate.
33. **Setting a field is not filling it. Read it back.** Setting the Play Console release
    notes programmatically reported success, and the page's own counter agreed —
    *"Release notes provided for 1 language"*. Reading the field back found it had silently
    eaten the opening `<en-GB>` tag and collapsed one paragraph break into a space. The
    counter was true and useless: it counted a language, not the text. Web forms built on a
    framework keep their own copy of the value and re-derive the input from it, so a value
    written straight into the DOM can be partly overwritten a moment later without any error
    anywhere. Typing it as real keystrokes produced the exact string. This is the same shape
    as lesson 25 — *the counter proves the form heard you, only the server proves it kept
    it* — one level lower down: **the success message proves the call returned, only reading
    the value back proves what it wrote.** After any programmatic form fill, re-read the
    field and assert on its actual content, then reload from the server and assert again.
34. **A missing build task is silent; a failing one is loud.** Both release bundles were
    built, exited 0 and looked normal while being completely unsigned, because the signing
    config is only created when `keystore.properties` exists and a task that is never
    created cannot fail. Absence of an error is not evidence of an action. When a build step
    is conditional on a file, verify the *artefact* — see §10, "The first genuinely signed
    release build" — not the exit code.

35. **Google never tells your testers anything. You do.** Adding an address to a closed-test
    email list sends that person no email, no notification and no prompt of any kind. They
    find out the app exists only because the developer messages them the opt-in link, and
    the link itself does not exist until the release is live — the Console shows *"The link
    will be shown here when you publish your app."* with **Copy link** greyed out. This is
    the most common reason a closed test sits at `0 testers currently opted in` while the
    developer believes invitations went out. A ready-to-send tester message, and the three
    ways it goes wrong on the tester's phone, are in `docs/RELEASING.md` Step 8.

36. **Google gives you a number, never a name — and it hides the number in one place.**
    There is no per-tester view anywhere in Play Console: who opted in, who installed, who
    dropped out are all treated as the testers' private data. The only figure that exists is
    a count, and it is printed as one italic line under step 2 of **Dashboard → Production →
    "Apply for access to production"** — not on the track page, not on the Testers tab, not
    in Statistics, which read *"Data unavailable"* a full day after going live. The Testers
    tab shows how many *addresses* you have added, which is a different number and reliably
    the larger one; mistaking one for the other is how a stalled test looks healthy. **So the
    only way to know who is actually in is to ask them and keep your own list.** The app
    cannot help either: no analytics, no accounts, no server, by design and permanently.

37. **The tester email field validates nothing, and the failure lands on someone else's
    phone.** The Console accepts any plausible-looking address. It does not check that it is
    a Google account, and it cannot check that it is the account signed into the Play Store
    on that person's device — so a wrong address sits in the list looking perfectly correct
    while the tester sees *"not available"* and concludes the app is broken. Nothing appears
    in the Console at all. This is the same shape as lesson 33 (*setting a field is not
    filling it*) with the feedback loop removed entirely: there is no read-back to do,
    because the only evidence of success is a human replying. Ask for the address by
    describing where to find it — *Play Store → your photo, top right* — rather than by
    asking for "your email".

38. **At high latitude the *convention* matters more than the arithmetic, and correct code
    can still disagree with every mosque in town.** The engine reproduces Muslim World
    League to the minute against Aladhan — and is still 78 minutes later than all three
    mosques a tester actually attends, because they use Moonsighting Committee's
    *shafaq*-based rule and the app defaults to MWL's fixed 17°. Nothing is broken. At 51.5°N
    the spread between mainstream conventions reaches well over an hour, so "verified against
    an independent reference" only ever proves the maths, never the choice. **A prayer app is
    judged against the noticeboard at the end of the road, not against an API.** When a user
    says the times are wrong, identify their mosque's convention before looking at the code:
    reading three published timetables and matching them to a method took minutes and found
    something no amount of re-reading `PrayerEngine.kt` could have. Method in §10, "Isha in
    the UK".

39. **"Begins" and "jama'ah" are different numbers, and mosques print them inconsistently.**
    The three timetables compared showed all three styles: JMIC Slough labels both columns,
    Reading labels them *Athan* and *Iqamah*, and Diamond Road prints a single unlabelled
    column that is congregation times for every prayer **except Maghrib**, where it prints
    the real one. So Diamond Road appears to disagree with JMIC about Isha by 47 minutes
    while actually agreeing within four. The app can only ever show the start of the window,
    and most users are comparing it to a board showing the congregation — **so the gap a user
    reports is usually real, expected, and not a bug.** This is a wording problem before it is
    a settings problem: say plainly that the app shows when a prayer *becomes due*, and that
    mosques choose their own congregation time afterwards. Do not silently shift times to
    close a gap that is supposed to be there.

---

*Made with love, free for the Ummah.*
