# SajdaTime — Handover

**Read this first.** It is written for someone (or some agent) picking the project up cold,
with no memory of how it got here. It covers the vision, the stack, every feature, every
business rule, what is verified, what is not, and what is left to do.

- **Repo:** `git@github.com:aliimrankhan86/SajdaTime.git`
- **Local path:** `/Users/aliimrankhan/Developer/SajdaTime`
- **Branch:** `main`
- **Version:** 1.1.0 (`versionCode` 2 on phone, 3 on the watch — they share an
  `applicationId`, and Play refuses two bundles with the same code)
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
strings. All 129 user-facing strings are in `app/src/main/res/values/strings.xml`, ready
for translation. Nothing is hardcoded in a composable.

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

**`:core`** (4 source files, 3 test files)

| File | Contains |
|---|---|
| `PrayerModels.kt` | `Sect`, `Madhab`, `CalcMethod` (14 methods, each with a `label`), `PrayerSlot` (with `isPrayer`), `Coordinates`, `DayPrayerTimes` (with `prayersOnly`), `CalculationPrefs`, `NextPrayer` |
| `PrayerEngine.kt` | **The heart of the app.** `compute`, `computeRange`, `nextPrayer`, `resolveMethod`. All the business rules in §5. |
| `QiblaEngine.kt` | `KAABA`, `bearingToKaaba`, `trueToMagnetic`, `relativeTurn`, `isAligned`, `distanceToKaabaKm`, `normalise` |
| `WatchSyncContract.kt` | The phone↔watch Data Layer path and keys, shared so the two modules cannot drift |

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

**`:wear`** (8 source files, 2 test files)

| File | Contains |
|---|---|
| `WearMainActivity.kt` | Entry, requests location permission |
| `WearApp.kt` | `HorizontalPager`: page 0 times, page 1 Qibla; the "No location yet / Use Makkah" screen |
| `WearViewModel.kt` | Own `LocationManager`, own rotation-vector compass, `useDefaultLocation()` |
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
fixed top reference dot. Bearing and distance in km. Guidance text ("Turn right 118°" /
"Facing Qibla") as a **polite screen-reader live region**. Calibration prompt when sensor
accuracy is low; a manual bearing readout when there is no compass at all.

### Settings
School of thought (Sunni/Shia) · Madhab (hidden for Shia, since Jafari fixes Asr anyway) ·
Calculation method · **How you are told** (Notification / Alarm) · Alarm sound picker ·
Do Not Disturb access warning · Exact alarm warning · Per-prayer notification switches ·
Ongoing badge switch · Change location · About (version, disclaimer, privacy, charity
statement, "Made by Ali Imran Khan, as an ongoing charity for the Ummah").

### Notifications
- **Default: notification + vibration + silent badge.** Alarm is **off** by default — this
  was an explicit user instruction.
- **Alarm mode is opt-in** and requires the user to pick their own tone. No adhan audio is
  bundled (licensing + APK size). Alarm mode uses `USAGE_ALARM` and `setBypassDnd(true)`.
- Optional silent ongoing "next prayer" badge in the shade.

### PDF export
`Range.TODAY` / `Range.NEXT_7_DAYS` / `Range.THIS_MONTH`, labelled "Today", "Next 7 days", "This month". A4 at 72 dpi (595×842 pt) via `PdfDocument`, shared through
`FileProvider` with `ClipData` set so the share sheet can render a preview.

### Wear OS
Standalone watch app (works with the phone off or unpaired) — two swipeable pages (times,
Qibla compass) — plus a **next-prayer tile** for the watch face carousel. Settings sync
one-way from the phone over the Data Layer.

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

### Palette (`ui/theme/Color.kt`)
Deep green (traditionally associated with Islam) as primary, warm gold as the single
accent, warm-neutral paper surfaces. No decorative colour.

| Role | Light | Dark |
|---|---|---|
| primary | `#14624B` | `#7FD1AE` |
| accent / tertiary | `#8A5200` | `#E3B341` |
| background | `#FBFAF7` | `#0E1512` |
| surface | `#FFFFFF` | `#18211D` |
| onSurface | `#12211C` | `#E8EEEA` |
| onSurfaceVariant | `#43524B` | `#B3C2BA` |
| secondaryContainer | `#D3E4DA` | `#2A473B` |

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
countdown does not jitter as digits change width.

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

**Cloud backup is disabled** (`allowBackup="false"`). Android's backup service would
otherwise copy the cached coordinates to Google's servers, contradicting the app's own
privacy promise. Re-entering settings takes two taps; the guarantee is worth more.

Permissions: `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`,
`RECEIVE_BOOT_COMPLETED`, `INTERNET`. **No fine location. No background location.**

---

## 9. Testing

**47 unit tests, all offline and deterministic, 0 failures.**

| Suite | Module | Tests | Covers |
|---|---|---|---|
| `PrayerEngineTest` | core | 17 | Reference timetables for Makkah, London, Tehran; Jafari Maghrib; madhab differences; high latitude; Ramadan; next-prayer roll-over and midnight spillover; chronological ordering across 3 cities × 4 methods × 52 weeks |
| `QiblaEngineTest` | core | 8 | Ten cities on five continents, distance, normalisation |
| `DeterminismTest` | core | 3 | Repeat-call stability, minute alignment, madhab equivalence |
| `ColorContrastTest` | app | 3 | WCAG AA for every pair in both themes |
| `CityLookupParseTest` | app | 5 | Coordinates come from the response; resolved name is displayed, not typed text; missing coordinate is a miss |
| `TileFormatTest` | wear | 7 | Countdown wording at boundaries; the refresh floor that stops a passed prayer looping the tile |
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

Expected: BUILD SUCCESSFUL, 47 tests, 0 failures, lint informational-only, phone release
APK ~1.9 MB and watch ~2.6 MB (both **unsigned**, unless a `keystore.properties` is present).

For Play, build bundles rather than APKs — `:app:bundleRelease :wear:bundleRelease`, which
give ~4.3 MB and ~3.5 MB `.aab` files.

`JAVA_HOME` matters — see §12. If any of it fails, check §13 first.

---

## 10. What has been verified on a device, and how

Two emulators were used: `sajda` (phone, API 36) and `sajdawear` (Wear OS, API 34).

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
rendering live data** ("Maghrib · 17:01 · in 1h 14m", matching the app).

### Useful device-testing recipes

```bash
# Fire a real alarm: move the clock to just before a prayer time
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

## 11. ⚠️ Still pending — the honest list

### Blocker for release

> **Play Store status at a glance.** Developer account created (personal, ID
> 6284685113064492750, developer name "Ali Imran Khan"). Android-device check **passed**.
> Privacy policy **live** at <https://aliimrankhan86.github.io/SajdaTime/privacy.html>. All
> store assets and listing text **done** in `docs/store/`. Waiting on Google's identity
> review (a few days; the contact-phone check unlocks automatically when it passes — it
> cannot be done before then, so do not treat it as a task). **The signing key is the only
> outstanding item anybody can act on, and it is the owner's alone.** After that, the
> 12-testers-for-14-days closed test is the long pole.

1. **The app is unsigned, and only the owner can change that.** The Gradle side *is* done:
   both modules read a `keystore.properties` from the project root and sign the release with
   it, falling back to unsigned output when the file is absent. That was verified with a
   throwaway key — both `.aab` files came out carrying a real signature — after which the
   throwaway key was destroyed. What is missing is the owner's real upload key. **An agent
   must never generate, hold, or see it.** `keystore.properties`, `*.jks` and `*.keystore`
   are gitignored. Full instructions: `docs/RELEASING.md` Step 2.

### Not done, in rough priority order

2. **No instrumented UI tests.** Logic and colour are unit-tested; every screen on both
   phone and watch is verified by hand only. A Compose UI test suite would make refactoring
   much safer.
3. **The watch has no settings of its own.** Sect, madhab and method arrive from the phone.
   A watch that is never paired calculates with the defaults and cannot be changed on-wrist.
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
   watch works standalone regardless, which is why this is not a blocker.
8. **No home screen widget** and **no tablet-optimised layout**.
9. **No bundled adhan audio** (deliberate: licensing + tens of MB). Alarm mode plays a tone
   the user picks.
10. **City search needs a network** unless the phone's own geocoder can answer offline.
11. **Localisation is prepared but not done.** All 129 strings are externalised; no
    translations exist yet. Arabic and Urdu would be the obvious first two, and Arabic will
    need RTL checking (`supportsRtl="true"` is already set).
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

    Still outstanding: **Google's identity review**. The contact-phone check is gated behind
    it and unlocks on its own, so it is not a task. See `docs/RELEASING.md` for the account
    facts and the full checklist.

    **The next actual step, once verification clears:** Play Console → Create app → paste
    the fields from `docs/store/LISTING.md`, upload the assets from `docs/store/`, complete
    the data safety form (the LISTING.md notes explain the one nuance about the geocoding
    lookup), then Closed testing with 12 testers for 14 continuous days.

### Deliberate non-goals — do not "fix" these
Ads, in-app purchases, accounts, analytics, crash reporting, fine location, background
location, cloud backup, a server of any kind.

---

## 12. Development environment

This is what the machine the project was built on actually has. Adjust paths for a
different machine.

| Thing | Value |
|---|---|
| Android SDK | `/Users/aliimrankhan/Library/Android/sdk` (in `local.properties`, **gitignored**) |
| JDK | Oracle Java SE 21 — `JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| Phone emulator | AVD `sajda`, API 36 |
| Watch emulator | AVD `sajdawear`, Wear OS API 34, `arm64-v8a` |
| Repo files tracked | 80 |
| Kotlin source | ~6,800 lines across the three modules |

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
| `30cb5eb` | **(current)** Fixed the silently-wrong city search; first real run of the watch app; alarm mode verified on device |

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
5. Keep the ponytail discipline: stdlib and platform first, no speculative abstractions,
   shortest working diff. Mark deliberate simplifications with a `ponytail:` comment.
6. The owner is **not technical**. Explain in plain language, state what is verified versus
   assumed, and never present something as done when it is untested.

---

*Made with love, free for the Ummah.*
