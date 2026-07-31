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

**`:core`** (5 source files, 3 test files, and `res/` — `values/strings.xml` for prayer and method names, `drawable/ic_kaaba*.xml` for the Qibla dial's Kaaba mark. Anything both screens show lives here, so it cannot be shown two different ways)

| File | Contains |
|---|---|
| `PrayerModels.kt` | `Sect`, `Madhab`, `CalcMethod` (14 methods), `PrayerSlot` (with `isPrayer`), `Coordinates`, `DayPrayerTimes` (with `prayersOnly`), `CalculationPrefs`, `NextPrayer`. **No display names** — see `PrayerLabels.kt`. This file has no `android.*` import and lifts straight into an iOS or KMP target. |
| `PrayerLabels.kt` | Display names for `PrayerSlot` and `CalcMethod`, as `labelRes` (for Compose) and `label(context)` (for notifications, the tile and the PDF). The one Android-touching file in `:core`; an iOS port supplies its own. |
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
| Manifest | `res/xml/data_extraction_rules.xml` — excludes everything from cloud backup and device transfer alike (both modules have one) |

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

Lint will complain that `dataExtractionRules` should be paired with `fullBackupContent`.
Ignore it: it does not reason about `allowBackup="false"`, which already disables backup
outright on every version `fullBackupContent` would apply to.

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

### Useful device-testing recipes

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

## 11. ⚠️ Still pending — the honest list

### Blocker for release

> **Play Store status at a glance.** Developer account created (personal, ID
> 6284685113064492750, developer name "Ali Imran Khan"). Android-device check **passed**.
> Privacy policy **live** at <https://aliimrankhan86.github.io/SajdaTime/privacy.html>. All
> store assets and listing text **done** in `docs/store/`. **Account verification is fully
> complete** — identity documents approved, contact phone verified, device check passed.
> Nothing is waiting on Google. **The signing key is the only outstanding item, and it is
> the owner's alone.** After that: create the listing, then the 12-testers-for-14-days
> closed test, which is the long pole.

1. **The app is unsigned, and only the owner can change that.** The Gradle side *is* done:
   both modules read a `keystore.properties` from the project root and sign the release with
   it, falling back to unsigned output when the file is absent. That was verified with a
   throwaway key — both `.aab` files came out carrying a real signature — after which the
   throwaway key was destroyed. What is missing is the owner's real upload key. **An agent
   must never generate, hold, or see it.** `keystore.properties`, `*.jks` and `*.keystore`
   are gitignored. Full instructions: `docs/RELEASING.md` Step 2.

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

    **Adding a language is a file drop, no code change:** create
    `app/src/main/res/values-<lang>/strings.xml` and
    `core/src/main/res/values-<lang>/strings.xml`. Android then picks it up from the
    phone's language automatically — the user is never asked to choose, which is correct.

    Two things follow the *first* translation, and only then:
    - `android:localeConfig` on the manifest plus `res/xml/locales_config.xml`, which gives
      Android 13+ a per-app language picker in system Settings for free. Adding it now with
      only English listed would show a picker with one entry.
    - An in-app language row, for phones below Android 13, which have no system picker.

    ⚠️ **Do not machine-translate this app.** Prayer names, madhab names and the disclaimer
    are religious content, and Indonesian or Urdu speakers expect "Fajr", not a local
    translation of "dawn". `core/src/main/res/values/strings.xml` carries a note to
    translators saying so. Each language needs a native speaker to review it before it
    ships. Arabic and Urdu are the obvious first two; both are RTL, and `supportsRtl="true"`
    is already set but has never been exercised.
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

    **All account verification has now cleared** — identity documents approved and the
    contact phone verified. Nothing on Google's side is outstanding. See
    `docs/RELEASING.md` for the account facts and the full checklist.

    **The next actual step:** Play Console → Create app → paste
    the fields from `docs/store/LISTING.md`, upload the assets from `docs/store/`, complete
    the data safety form (the LISTING.md notes explain the one nuance about the geocoding
    lookup), then Closed testing with 12 testers for 14 continuous days.

### Deliberate non-goals — do not "fix" these
Ads, in-app purchases, accounts, analytics, crash reporting, fine location, background
location, cloud backup, a server of any kind.

**Also: features the dark design system deliberately leaves out.** The design it came from
was drawn against the shipping feature set on purpose — no prayer tracker, no day stepper
on the Times screen, no per-row notification mute. They are absent from the mockups because
they are absent from the app, not because they were forgotten. A future design pass should
stay inside the feature set unless the feature itself has been agreed first.

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
| Repo files tracked | 114 |
| Kotlin source | ~8,350 lines across the three modules |

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
| *(this)* | **(current)** Put the Kaaba on the Qibla dial, on both the phone and the watch, so the screen answers "which way" without being read |

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
15. Keep the ponytail discipline: stdlib and platform first, no speculative abstractions,
    shortest working diff. Mark deliberate simplifications with a `ponytail:` comment.
16. The owner is **not technical**. Explain in plain language, state what is verified versus
    assumed, and never present something as done when it is untested. He also has **no
    access to physical devices** — if you cannot test it on an emulator, say so plainly
    rather than suggesting he go and try it himself.
17. **When you commit, commit the understanding too** — see `CLAUDE.md` at the repo root.
    Code alone loses the reasoning, and the reasoning is what stops the next session
    undoing a decision it does not know was deliberate.

---

*Made with love, free for the Ummah.*
