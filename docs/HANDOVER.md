# SajdaTime — Handover

**Read this first.** It is written for someone (or some agent) picking the project up cold,
with no memory of how it got here. It covers the vision, the stack, every feature, every
business rule, what is verified, what is not, and what is left to do.

- **Repo:** `git@github.com:aliimrankhan86/SajdaTime.git`
- **Local path:** `/Users/aliimrankhan/Developer/SajdaTime`
- **Branch:** `main`
- **Version:** 1.1.1 (`versionCode` 3 on phone, **1000** on the watch — they share an
  `applicationId`, so codes must be unique across both modules; each has its own band so
  the two sequences can never collide. The watch also ships on a **separate Play release
  track**, not in the phone's release — see `docs/RELEASING.md` Step 7.)
- **Status:** **live on the Play closed testing track** since 31 Jul 2026, signed by the
  owner's own key. `versionCode` 2 (1.1.0) is what testers are running; `versionCode` 3
  (1.1.1) is built and waiting to be uploaded and carries the Dhuhr fix. See §11.
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

- **`USE_EXACT_ALARM`.** Checked properly on 1 Aug 2026 and rejected, so that the next
  session does not rediscover it as an obvious win — because on the technical merits it is
  one. It is the *only* documented way to keep an app floored at the WORKING_SET standby
  bucket, and at targetSdk 36 `SCHEDULE_EXACT_ALARM` no longer provides that floor, so
  without it a rarely-opened app can drift to Restricted and its documented allowance of
  **one alarm per day**.

  It is refused on policy, not on engineering. Play names exactly two qualifying categories,
  *"an alarm or timer app"* and *"a calendar app that shows event notifications"*, and the
  penalty for a category claim Google disagrees with is being *"disallowed from publishing"*
  that release. A prayer app with recurring user-set alerts is arguable. Arguable is not a
  category, and the cost of losing the argument is a blocked release on a project that has
  already spent a fortnight collecting twelve testers. Sourcing:
  `docs/reviews/2026-08-01-android-alarm-facts.md` §3.

- **`USE_FULL_SCREEN_INTENT`.** Same shape of reason. Its auto-grant list at targetSdk 34+ is
  *narrower* still — "setting an alarm" or "receiving phone or video calls", with no calendar
  bullet — and a prayer notification does not need to seize the whole screen.

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
    // How each prayer announces itself. A prayer absent from the map is silent — there is
    // no separate on/off switch, because "off" is simply having no style.
    val alertFor: Map<PrayerSlot, AlertStyle> = <all five -> NOTIFICATION>,
    // "Match your mosque" — see §5.14's neighbours and the AdjustmentCodec.
    val adjustments: Map<PrayerSlot, Int> = emptyMap(),   // per prayer, clamped to +/-30 min
    val hijriOffsetDays: Int = 0,                          // clamped to +/-2 days
    val ongoingBadge: Boolean = true,      // silent, costs nothing, so on by default
    val alarmSoundUri: String = "",        // empty = device default alarm
    val alarmRespectsSilent: Boolean = true,      // alarm -> quiet notification when silenced
    val alarmOnApproximateDays: Boolean = false,  // §5.14 — projected days do not ring
    val disclaimerSeen: Boolean = false,
    val usingDefaultLocation: Boolean = false,  // true once Makkah was used as fallback
    val exactAlarmNoticeDismissed: Boolean = false,  // home only; Settings keeps the row
    val methodNoticeDismissed: Boolean = false,      // same contract as the line above
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,  // SYSTEM resolves to light
)
```

This block has drifted from the code before. If you are changing `AppSettings`, change it
here in the same edit — a stale data model is the one piece of this document a reader is
most likely to trust without checking.

`PrayerSlot`: `FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA` — `isPrayer` is false for
`SUNRISE`, which is why it is displayed but never notified and never "next".

**`alertFor` replaced two keys, and the migration is tested.** It supersedes
`notify_slots` (which prayers) and `alert_style` (one global how). Both old keys are still
*read*, never written, by `AlertCodec.migrate` — see `AlertCodecTest`. The distinction that
matters: the old `notify_slots` stored "no prayers" as an **empty string** and "never
touched" as an **absent key**, so conflating the two would give the one user who had
deliberately silenced everything five unexpected alerts the morning after an update. Stored
form is `"FAJR:ALARM,DHUHR:NOTIFICATION,…"`, sorted by the enum's own order so an unchanged
setting is not rewritten. Unknown slots and unknown styles are dropped entry by entry rather
than throwing, because preferences outlive the code that wrote them.

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
finish. Then the one-time disclaimer dialog.

The **PERMISSION** step always offers both routes at once: "Allow location", and below it
"Or type a city" with a field, a Find city button and a "Skip for now and use Makkah"
escape. All three are shown until a location exists and are gated on nothing else — see
lesson 52 for the first-run dead end that the previous, cleverer condition produced.

The **CONFIRM** step ends with a one-time offer to grant "Alarms & reminders" when it is
missing, because that permission is denied by default from Android 13 and a banner about a
setting is not the same as being asked. Finish sits underneath it, unconditional: it is an
offer, never a gate.

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
| **PRAYER TIMES** | Location (pin icon, city as subtitle) · School of thought ("Sunni · Hanafi") · Calculation method · Match your mosque (per-prayer minutes and the Hijri date; **last in the group on purpose** — the two rows above it explain the divergences that have a reason, and this one is the catch-all for what is left) |
| **APPEARANCE** | Theme — three chips, Follow phone / Light / Dark. Chips rather than a chooser because the result is visible the instant it is tapped, and a dialog would sit on top of the change it just made |
| **REMINDERS** | Prayer alerts — Off / Notification / Alarm **per prayer**, with the sound picker, the silent-mode switch, the DND warning and (only above the polar circles) "Ring on approximate days" inside · Next-prayer badge switch, inline |
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
- **The style is chosen per prayer, not once for the app.** Off, Notification or Alarm, for
  each of the five, in one chooser. A tester asked to be woken for Fajr and left alone for
  the rest, and a single global radio could not say that. See §5.12.
- **Alarm mode is opt-in** and requires the user to pick their own tone. No adhan audio is
  bundled (licensing + APK size).
- **An alarm stays quiet while the phone is silenced**, unless the user turns that off. The
  notification still arrives, on time, in the shade and as a heads-up — only the sound is
  dropped. See §5.13.
- **An alarm also stays quiet on a day whose times had to be projected from another latitude**,
  unless the user turns *that* off. Same treatment: it arrives on time, in the shade, and
  additionally carries the "Approximate time" marking. The switch appears only for users whose
  own location produces such days, so almost nobody ever sees it. See §5.14.
- **What actually carries an alarm through Do Not Disturb is `USAGE_ALARM` on the channel
  plus `CATEGORY_ALARM` on the notification**, not `setBypassDnd(true)`. That call is a
  no-op without notification policy access, which this app does not ask for: read the
  channel back and it says `mBypassDnd=false`. It is kept only because it costs nothing if
  the user ever grants the access. Verified on an API 36 emulator under
  `ZEN_MODE_IMPORTANT_INTERRUPTIONS`: the prayer alarm posted with `mIntercept=false` while
  the silent next-prayer badge was intercepted, which is the behaviour wanted for both.
- **The alarm survives Doze.** `setAlarmClock` for **every** prayer alert now, whatever
  style it will eventually make — see §6. Verified under forced deep idle: `dumpsys alarm`
  named SajdaTime as `Next wake from idle`, `whenElapsed` was not deferred, and crossing the
  trigger time posted "Time for Asr" and rolled the badge on to Maghrib.
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

### 5.5a Above the polar circles — the reference latitude, and why it is not one number

`HighLatitudeRule` only helps while the sun still rises and sets. Beyond roughly 65.7°
(summer) and 67.4° (winter) it does not, adhan gives up, and the engine borrows the day from
a lower latitude — *aqrab al-bilad*, the nearest place where night and day are still
distinguishable. Longitude and hemisphere are kept, so solar noon stays true to where the
user is standing.

**Which latitude to borrow from is a fiqh question with two published answers, so the engine
follows whichever body the user already chose rather than picking for them.**

| Method | Reference | Authority |
|---|---|---|
| Moonsighting Committee | **60°** | moonsighting.com, in their own words |
| Everything else | **45°** | Islamic Fiqh Council of the Muslim World League |

- **45° — Islamic Fiqh Council of the Muslim World League**, resolution 6 of the ninth
  session, Makkah, 12–19 Rajab 1406 (March 1986), endorsed by the European Council for Fatwa
  and Research. Three bands: below 48° the signs are visible all year and must be used;
  between 48° and 66° Fajr and Isha are taken by analogy with the nearest place where they
  are clear; **beyond 66° all times are estimated from 45°.**
- **60° — Moonsighting Committee**: *"at latitudes more than 60degrees, we slide down to
  60degrees and calculate Fajr & Isha using the rule of Sab'u Lail in summer."* They chose it
  knowing it breaches the eighteen-hour fasting limit of the fatwa they cite, on the empirical
  ground that Oslo copes.

The band edges are the strongest evidence the resolution is reported correctly, because they
are not round numbers chosen by a committee — they are astronomy. 18° of solar depression
last occurs at midsummer at latitude 90 − 23.44 − 18 = **48.56°**, and 66.56° is the polar
circle itself. Verified numerically; see §10.

**The times are always flagged.** `DayPrayerTimes.approximatedFrom` carries the actual number
and the home screen prints it, so a user can check the app against their mosque instead of
taking its word. Showing a projection silently would be the app taking a position on someone's
behalf without telling them.

Rejected, with reasons, so they are not tried again:

- *One constant for everybody.* Whichever number were chosen it would impose one body's ruling
  on users who explicitly selected another — the exact failure this app exists to avoid.
- *Clamp to the highest latitude that still computes.* An artefact of the library, it moves
  daily, no scholar stands behind it, and Moonsighting measured the result: fasts *"of more
  than 23 hours in summer and less than 3 hours in winter"*.
- *Use Makkah's times.* A real position — Dar al-Iftā' al-Miṣriyyah, and some Norwegian
  mosques — but it discards the user's own solar noon, so Dhuhr would stop matching the sun
  overhead. Only ever as an explicit user choice.

### 5.5b Trusting adhan's answer is not the same as receiving one

`PrayerEngine.isUsable` is the guard, and it checks three things, not one:

1. **No nulls.** The original crash.
2. **In order.** adhan can return a confident wrong number rather than null. Measured: a
   27 January Asr handed back as 13 March.
3. **Dhuhr in the middle of the day arc.** Solar noon is the midpoint of sunrise and sunset.
   Where it is not, adhan has paired a sunrise from one night with a sunset from another —
   which is how 78°N came to show Maghrib at 17:00 on 24 August and 22:29 the next day.

Any failure means project and flag. Being wrong in the direction of an honest "approximate"
banner is safe; being wrong in the direction of a confident number is not.

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

### 5.12 Alerts are chosen per prayer

`alertFor: Map<PrayerSlot, AlertStyle>`. Off, Notification or Alarm, for each of the five.
Absence from the map is off; there is no separate on/off flag.

This replaced two settings — "Which prayers" and "How you are told" — that between them
answered halves of one question and could not express the request that produced them: *an
alarm loud enough to wake me for Fajr, and a quiet notification for the four I am already
awake for.* Collapsing them removed a settings row rather than adding one.

Defaults are unchanged and stay deliberately quiet: **all five, Notification.** Nothing is
ever loud until asked. The alarm sound row and the silent-mode switch appear only once at
least one prayer is set to Alarm, rather than sitting greyed out for the majority who never
leave the default.

Rejected: keeping the global style and adding a per-prayer override set. It preserves the
same two-settings confusion and needs a rule for what happens when the two disagree.

### 5.13 An alarm stays quiet while the phone is silenced

`alarmRespectsSilent`, **on by default**. When it is on and `AudioManager.getRingerMode()`
is `RINGER_MODE_SILENT` or `RINGER_MODE_VIBRATE`, an Alarm-style alert is posted on the
quiet channel instead. It still arrives on time, in the shade and as a heads-up — only the
sound is dropped.

**This is a fix, not a preference, and here is why it was needed.** Android deliberately does
*not* mute the alarm stream with the ringer: the streams it mutes are RING, NOTIFICATION,
SYSTEM and SYSTEM_ENFORCED, and turning STREAM_ALARM into one of them is an opt-in device
resource (`config_audio_ringer_mode_affects_alarm_stream`) that defaults to false. That is
right for a clock someone set for one morning and wrong for a recurring alert five times a
day from an app they installed yesterday. A tester reported the alarm sounding through a
silenced phone, and he was right to.

**Vibrate counts as silenced.** Someone who has taken the ringer down to vibrate has asked
for no sound just as plainly as someone on full silent, and the quiet channel still vibrates.

**Why the default is on, when a real alarm clock's would be off.** Making noise from a phone
that has visibly been silenced is a top reason a charity app gets uninstalled, and the user
who genuinely wants Fajr regardless can turn the switch off — where the wording says exactly
what that costs. The switch exists precisely because this is a judgement call and not a fact.

**Deliberately not consulting Do Not Disturb.** DND allows alarms by default, it has its own
separate opt-in on the Settings screen, and `getRingerMode()` is the question the user
actually answered with the volume keys. Note also that `getRingerMode()` returns the
*external* mode, which can diverge from the internal one after a ringer change made while DND
is active — it is the right API to ask about the ringer and the wrong one to ask about DND.

### 5.14 An alarm does not ring for a time the app had to borrow (A10, decided 2 Aug 2026)

`alarmOnApproximateDays`, **off by default**. When a day's times had to be projected from
another latitude — `DayPrayerTimes.approximatedFrom != null`, which in practice means above
the polar circles — an Alarm-style alert is posted on the quiet channel instead. As in 5.13,
the alert still arrives, on time, in the shade, as a heads-up, and still carrying the
"Approximate time" marking. Only the sound is dropped.

**The reasoning.** Above the polar circles the sun does not always rise or set, so there is
no true Fajr or Isha to calculate and the app borrows both from a lower latitude under a
published ruling (§5.5a). That is a defensible answer, not a guess — but it is a *borrowed*
answer, and mosques in the same town follow different ones. A notification states such a time
and marks it approximate. An alarm asserts it hard enough to wake someone at two in the
morning, on a number the app is simultaneously telling them to check with their mosque.

**The switch is not decoration, and this is the part that must not be removed.** At Tromsø
every day from late May to late July is projected. Without an override, a user who had set a
Fajr alarm would find it silently stop working for two months of the year — and the only way
to discover that is to not be woken by it. That is the app settling a religious question on
the user's behalf, which is exactly the objection that got auto-switching the calculation
method rejected (§11, A2). *Off by default* is a judgement about which way to be wrong.
*No switch at all* would be a different thing entirely.

**Who sees the switch.** Only users whose own location produces such days.
`SettingsScreen.hasApproximateDays` sweeps a full 366 days through the engine and asks it
directly, rather than testing a latitude against a threshold. A threshold would have to be
method-dependent — the reference latitude is 45 under the Islamic Fiqh Council and 60 under
Moonsighting Committee — and it would be one `abs()` away from being wrong in the Southern
Hemisphere. The sweep is a few milliseconds behind a `remember`, computed only while the
dialog is open. ponytail: no constant to keep in step, no physical claim to pin with a test.

**Deliberately not synced to the watch.** Unlike `adjustments` and `hijriOffsetDays`, which
change the *times* and therefore have to reach both modules, this one changes only how an
alert sounds — and the watch module schedules no alarms and posts no prayer alerts at all
(no `AlarmManager`, no `AlertStyle`, nothing calling `postPrayerAlert`). Adding it to
`WatchSyncContract` would ship a key nothing reads. If the watch ever gains its own alerts,
this is the setting to carry across, and this paragraph is why it was not there already.

**The order of the two downgrades matters and is deliberate.** `effectiveAlertStyle` checks
the projected case *before* the ringer, so the AudioManager read is skipped whenever the
cheaper answer already settles it — this runs on a receiver that has just been woken from
Doze. Neither path can ever turn a Notification *into* an Alarm; both are downgrades only,
and `AlertStyleDowngradeTest` sweeps all sixteen flag combinations to keep it that way.

---

## 6. Notification and alarm architecture

### Reliability — five independent reschedule triggers
No single failure silently stops notifications:
1. every time the app is opened
2. every time an alarm fires (chains the next window)
3. a WorkManager job (12-hour period)
4. `BOOT_COMPLETED` / `TIMEZONE_CHANGED` / `TIME_SET` / `MY_PACKAGE_REPLACED`
5. `android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` — Android 12+

Alarms are laid down `HORIZON_DAYS = 2` ahead, so a missed daily job is survivable.

**Why (5) matters in both directions.** Granting "Alarms & reminders" is the moment the
whole schedule can be upgraded from a one-hour delivery window to an exact one, and without
this receiver that upgrade waited until the user next opened the app — which for this app
may be days, because the notification *is* the product. Revoking it cancels every future
exact alarm the app has already set, so the schedule has to be re-laid in whatever weaker
form is still permitted. Verified: running only `appops set … SCHEDULE_EXACT_ALARM allow`,
without touching the app, flipped every pending alarm from `window=+1h0m0s0ms` to
`window=0` in `dumpsys alarm`.

### Exact alarms — the ladder, and why every alert now uses `setAlarmClock`

`SCHEDULE_EXACT_ALARM` is **denied by default from Android 13**, and **`setAlarmClock`
requires it too** (an early assumption that it did not caused a `SecurityException` crash
on first launch). Three rungs, each tried only if the one above it failed:

```kotlin
1. setAlarmClock                — needs SCHEDULE_EXACT_ALARM, no exemption clause
2. setExactAndAllowWhileIdle    — still exact; a battery-exempt app may call it without
                                  the permission, which rung 1 cannot use
3. setAndAllowWhileIdle         — inexact, never throws, always available
```

`runCatching` at each rung rather than a permission check alone, because some OEM builds and
work profiles revoke the capability between the check and the call.

**Notification style used to get rung 2 and now gets rung 1.** The old comment here said the
quieter alarm was chosen so the app "does not claim a status bar icon it has not earned".
That is overruled, on evidence: `setAlarmClock` is the only API Google describes as never
moved (*"the system never adjusts their delivery time"*), while `setExactAndAllowWhileIdle`
carries the opposite promise in the same reference and a rate limit Google's own pages state
three incompatible ways. The status bar alarm icon is a cosmetic cost; an alert that arrives
after the prayer has begun is a functional failure. The icon is also **true** — an alarm
really is scheduled. Full sourcing: `docs/reviews/2026-08-01-android-alarm-facts.md`.

**Measured, not assumed.** Without the permission, `dumpsys alarm` showed
`window=+1h0m0s0ms` — a **one-hour** delivery window, not "a few minutes". The user-facing
wording was corrected to match, and onboarding's last step now asks for the permission
outright instead of leaving it to a banner.

**Rung 3 is worth less than "inexact" suggests, on the hardware that matters most.** On a
Xiaomi (HyperOS 3 / API 36), a `setAndAllowWhileIdle` alarm does not merely drift within its
hour — the OEM's `power_pending` policy **overwrites `whenElapsed` outright**, pushing it
roughly three days out, and it stays there until the user picks the phone up. Measured
overnight, both builds on one phone, same prayer, same minute, same standby bucket: rung 1
fired at 02:51:00.037, rung 3 never fired at all (§10). Rung 3 is kept because it never
throws and something is better than a `SecurityException` — but it should be understood as a
last resort that on some devices means "whenever the phone next wakes", not "within the
hour". This is the single strongest reason the ladder starts at `setAlarmClock` for **both**
alert styles.

**What this does not fix, and must not be claimed to.** App Standby buckets. Google
documents no exemption from them for `setAlarmClock`, and a Restricted-bucket app is held to
*"One alarm per day, either an exact alarm or an inexact alarm"*. Worse, `SCHEDULE_EXACT_ALARM`
only floors an app at WORKING_SET when it **targets API 33 or below**; this app targets 36,
so it has no floor. Only `USE_EXACT_ALARM` gives one, and that permission is Play-gated to
alarm-clock and calendar apps — see §11 for why it was checked and rejected rather than
claimed.

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

**83 unit tests, all offline and deterministic, 0 failures.**

| Suite | Module | Tests | Covers |
|---|---|---|---|
| `PrayerEngineTest` | core | 22 | Reference timetables for Makkah, London, Tehran; Jafari Maghrib; madhab differences; high latitude; Ramadan; next-prayer roll-over and midnight spillover; chronological ordering across 3 cities × 4 methods × 52 weeks |
| `PolarAndHemisphereTest` | core | 13 | Every 0.5° pole to pole × every day of 2026 × both madhabs is in order; Dhuhr sits mid-day-arc; the days that were silently wrong are flagged; the reference latitude follows the method; Moonsighting reproduces its own published Oslo extremes; the Shia Maghrib path survives every latitude |
| `QiblaEngineTest` | core | 8 | Ten cities on five continents, distance, normalisation |
| `DeterminismTest` | core | 3 | Repeat-call stability, minute alignment, madhab equivalence |
| `CoordinatesTest` | core | 4 | `Coordinates.orNull` rejects off-globe values, NaN, infinity and half-pairs — the gate on everything the watch's exported Data Layer listener is handed (§8) |
| `LocaleDisciplineTest` | core | 4 | `app_language_tag` parses and round-trips; every translation declares one; it matches its folder; no shipped code calls `Locale.getDefault()` (§5.11) |
| `ColorContrastTest` | app | 4 | WCAG AA for every pair in both themes |
| `CityLookupParseTest` | app | 5 | Coordinates come from the response; resolved name is displayed, not typed text; missing coordinate is a miss |
| `AlertCodecTest` | app | 8 | Per-prayer alert round-trip and stable ordering; rubbish dropped entry by entry rather than thrown; Sunrise refused; and the upgrade from the two keys it replaced — including the empty-string-versus-absent-key distinction that decides whether a user who silenced everything gets five alerts back (§3, persisted data model) |
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

**~~Leave the build type on `ar-XB`.~~ OVERTURNED 2 Aug 2026 — the build type is now `ur`.**
The original text read: *"The `ur` pin is a diagnostic to reach for deliberately and revert,
never something to commit. `ar-XB` is reserved and no real phone can be set to it, which is
the property that stops the `rtl` build type from ever being mistaken for, or promoted into,
a translation. `ur` is a real language tag and would throw that safety away for no gain."*

Two of its three claims were sound. The third — *"for no gain"* — was wrong, and the section
directly above it says why without noticing: a preview that mangles every screen equally
cannot answer which screens are actually wrong. The owner reviewed the `ar-XB` build twice
and both times reported the app as broken. It was not, and telling him so twice was worse
than useless: it spends the trust that makes future RTL reports worth reading. **A review
tool that its only human reviewer cannot read has failed at its one job**, and the gain from
fixing that is the entire reason the build type exists.

The safety claim was also weaker than it sounded. `ar-XB` never stopped anyone adding
`values-ar/` to the *main* source set, which is the move CLAUDE.md actually forbids — it only
made the preview itself unpromotable, which no one was trying to promote. That is now
guarded deliberately by `NoTranslationsYetTest`, which fails the build if any
`values-<lang>/` folder appears in any module. See "Reading the preview, and the real bug it
was hiding" below.

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

**Putting the emulator somewhere else — harder than it looks, and it cost a session.**

```bash
# WRONG on its own. `emu geo fix` sets the GPS provider only, and
# LocationRepository.lastKnown() reads NETWORK first, so the emulator's own
# live network fix (Greater Manchester on a stock image) wins every time.
adb -s emulator-5554 emu geo fix <lon> <lat>     # note: longitude FIRST

# Better: override the network provider itself.
adb -s emulator-5554 shell cmd location providers add-test-provider network
adb -s emulator-5554 shell cmd location providers set-test-provider-enabled network true
adb -s emulator-5554 shell cmd location providers set-test-provider-location network --location 69.65,18.96

# ALWAYS remove it afterwards. Leaving a mock provider on the owner's emulator
# is the same class of mistake as leaving the RTL build behind (lesson 42).
adb -s emulator-5554 shell cmd location providers remove-test-provider network
```

Even then it is fragile. The mock survives long enough for one read, but the emulator keeps
publishing real network fixes, and `lastKnown()` prefers the freshest — so after a permission
dialog or an activity restart the app quietly reverts to the emulator's own position. If a
screenshot at a specific latitude is genuinely required, **set the coordinates through the
app's own city search instead**, or accept that the unit tests are the stronger evidence and
say plainly that the screenshot was not taken. Do not claim a screen was seen when it was not.

**An ANR immediately after `installDebug` is usually not your change.** Check before blaming
the diff:

```bash
adb -s emulator-5554 shell "ls -t /data/anr/ | head -3"
adb -s emulator-5554 shell "cat /data/anr/<file>" | head -30
```

On 1 Aug 2026 a fresh install produced *"SajdaTime isn't responding"* on the onboarding
screen — a screen that computes no prayer times at all. The trace's `CriticalEventLog` showed
`install_packages` followed by *"Blocked in monitor ActivityManagerService … for 17s"*: the
emulator's own `system_server` had wedged and starved app startup. The subject was
`failed to complete startup`, not anything of ours. A cold relaunch once the install settled
was clean. **Read the trace's first event before concluding anything.**

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
madhab (JMIC standard, Reading Hanafi). Asr matched to the exact minute in both cases, on a
value that differs by 67 minutes between the two madhabs — so this is an identification, not
a coincidence.

**Correction, made the same day by checking the app's own engine rather than Aladhan.** The
paragraph above originally said each mosque adds two to five minutes of *ihtiyat* to the
published time. That was wrong. Those offsets are **part of the Moonsighting method itself**:
adhan-java's `MOON_SIGHTING_COMMITTEE` carries method adjustments of **+5 minutes on Dhuhr
and +3 on Maghrib**, which Aladhan does not apply — exactly the same class of discrepancy
already documented in §5.3 for MWL's +1 Dhuhr offset. Running `PrayerEngine` directly for
Slough on 1 Aug 2026 with `MOON_SIGHTING`:

| | JMIC Slough (printed) | **SajdaTime's own engine** | Aladhan |
|---|---|---|---|
| Fajr | 03:39 | 03:38 | 03:38 |
| Dhuhr | 13:14 | **13:14** | 13:09 |
| Asr | 17:19 | 17:18 | 17:19 |
| Maghrib | 20:53 | **20:53** | 20:51 |
| Isha | 21:58 | 21:59 | 21:59 |

**The app on Moonsighting reproduces JMIC Slough to within one minute on all six times, and
matches Dhuhr and Maghrib exactly** — better than Aladhan does, because adhan applies the
method's own adjustments and Aladhan does not. So no per-prayer offset feature is needed to
match this mosque. Selecting the method is enough.

**The app's Moonsighting was then checked against Aladhan across the year**, because the
engine forces `HighLatitudeRule.TWILIGHT_ANGLE` onto *every* method and that could plausibly
have distorted a seasonal convention:

| Slough | App Fajr / Aladhan | App Isha / Aladhan |
|---|---|---|
| 1 Aug 2026 | 03:38 / 03:38 | 21:59 / 21:59 |
| **21 Jun 2026 (solstice)** | 02:45 / 02:45 | **22:44 / 22:43** |
| 21 Dec 2026 | 06:24 / 06:24 | 17:34 / 17:34 |

Fajr and Isha agree to the minute year-round **including at the solstice**, which is the one
point where the forced high-latitude rule could have interfered. It does not. Recommending
Moonsighting for high-latitude users is therefore safe, and this was verified rather than
assumed.

### The disclaimer and method-picker wording, verified in RTL on device (1 Aug 2026)

Owner's decision, recorded because it settles T1's open question: **the user gets the choice,
and the disclaimer says so.** "If the user has to decide this then we should give it to them
… it's a free, charity app. If the user wants to switch something in settings then that
should be allowed."

Two text changes, no new screens, no latitude logic, no auto-switching:

1. **Two paragraphs added to `disclaimer_body`**, before the dua — that mosques disagree about
   when a prayer begins, that Isha differs most and by over an hour in the north, that the app
   will not settle it, and that the method is changeable in Settings. Plus that the app shows
   when a prayer *becomes due* while the mosque board shows the congregation, and that the gap
   between them is normal.
2. **A help line at the top of `MethodPickerDialog`** — a list of institution names means
   nothing to most users, and the wrong pick moves Isha by an hour here.

**Verified by walking the RTL build on `emulator-5554`, not by reading the diff:**

| Check | Result |
|---|---|
| Method picker help text renders, right-aligned | ✅ |
| Method list still scrolls past the new text | ✅ — Moonsighting, ISNA, Kuwait, Qatar, Singapore, Diyanet all reachable after one swipe |
| Disclaimer scrolls to the bottom | ✅ |
| **Dua request still the final paragraph, still reachable** | ✅ |

**Two traps this run exposed, both worth keeping:**

- **`./gradlew installRtl` fails outright while both emulators are running:**
  `INSTALL_FAILED_VERSION_DOWNGRADE: Update version code 2 is older than current 1000`. Phone
  and watch share one `applicationId`, so the watch build (version code 1000 band) blocks the
  phone build (2) on any device that has it. Fix: `ANDROID_SERIAL=emulator-5554`, and
  `adb -s emulator-5554 uninstall com.sajdatime.app` first if a watch build got onto the phone
  emulator. Identify which is which with `adb -s <id> shell getprop ro.build.characteristics`
  — the watch reports `emulator,nosdcard,watch`.
- **A `uiautomator dump` can be stale, and it reads as a confirmed bug.** A dump taken after
  the disclaimer had closed showed home-screen nodes and no dua text, which looked exactly
  like the dua being clipped away — a hard-rule violation. It was not; the dialog scrolls
  fine. **Screenshot before concluding, and check the dump actually shows the screen you think
  it does.**

**Judgement call left open for the owner.** The disclaimer is now seven paragraphs and only
four fit on a 1080×1920 screen at default font, so the dua is three swipes down. It was
already below the fold before this change, and it is still last and still reachable — but the
text is getting long, and the same explanation already appears in the method picker where the
decision is actually made. If the disclaimer should be shortened again, the Isha paragraph is
the one that is duplicated elsewhere.

### Is Muslim World League actually a bad default? Measured worldwide, and mostly no

**Why this exists.** The Slough finding above could easily be over-generalised into "MWL is
wrong, replace it". That would be a mistake, and the app serves the whole Ummah rather than
one town. So the default was tested against the regionally-dominant convention in fifteen
major centres, on a winter and a summer date. **Isha difference, MWL minus local method:**

| City | Local method | Jan | Jun |
|---|---|---|---|
| Jakarta | KEMENAG | +4 | +4 |
| Karachi | Karachi | +4 | +6 |
| Dhaka | Karachi | +5 | +5 |
| Cairo | Egyptian | +3 | +3 |
| Istanbul | Diyanet | 0 | 0 |
| Kano | MWL | 0 | 0 |
| Delhi | Karachi | +5 | +6 |
| Riyadh | Umm al-Qura | +15 | +7 |
| Kuala Lumpur | JAKIM | +4 | +4 |
| Casablanca | Morocco | 0 | 0 |
| Tashkent | MWL | 0 | 0 |
| Chicago | ISNA | −11 | −18 |
| **London** | Moonsighting | **−20** | **−47** |
| **Berlin** | Moonsighting | **−24** | **−43** |

**MWL is a good default for the great majority of the world's Muslims** — within about five
minutes across South and South-East Asia, the Middle East and Africa, which is where most of
them live. It fails specifically in the West, and it fails worst at high latitude.

**Where the line falls.** Isha, MWL minus Moonsighting, at longitude 0:

| Latitude | 21 Dec | 21 Mar | 21 Jun |
|---|---|---|---|
| 20° | −10 | −7 | +2 |
| 30° | −8 | 0 | +12 |
| 40° | 0 | +9 | +35 |
| 45° | +5 | +16 | *(day-wrap)* |
| **50°** | **+15** | **+26** | **+49** |
| 55° | +30 | +40 | +31 |
| 60° | +55 | +59 | +7 |

Below about **40°N the two conventions agree within roughly ten minutes**. From 45°N the gap
opens fast, and by 50°N — where Britain, the Netherlands, northern Germany and southern
Canada sit — it is half an hour or more for most of the year. Two cells show absurd values
(±1380 minutes) because Isha crosses midnight and the naive minute subtraction wraps; they
are an artefact of the comparison script, not of either method.

**The conclusion, which is narrower than the Slough finding alone suggested:** do not replace
the default, do not add methods. `KEMENAG` and `JAKIM` are absent from `CalcMethod` and it
does not matter — MWL is within four to nine minutes of both. **The only population that
needs anything is users above roughly 45° of latitude**, and the fix they need already ships.

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

### Do they switch convention in midsummer? No — closed, at the solstice itself

**Why this mattered.** Everything above rested on one date, 31 July, which sits *outside* the
window where an 18° Isha is unreachable at this latitude (roughly 21 May to 23 July at
51.5°N). Many UK mosques adopt a different rule for those ten weeks. If Reading and JMIC did
too, then "set it to Moonsighting" would be correct in August and wrong in June — which is
the worst possible failure for a prayer app, because it would be wrong only in the season
nobody double-checks.

Reading Mosque publishes a month-navigable timetable (`/timetable/`, JS-rendered — the table
is not in `innerText` until the month control is clicked, and the page's own July view loaded
empty until stepped). Its **Athan** column against Aladhan Moonsighting + Hanafi:

| Date | Fajr printed / calc | Asr printed / calc | Isha printed / calc |
|---|---|---|---|
| 1 Jun 2026 | 02:59 / 03:00 | 18:34 / **18:34** | 22:26 / 22:25 |
| **21 Jun 2026 — solstice** | **02:47 / 02:47** | **18:43 / 18:43** | **22:45 / 22:45** |
| 30 Jun 2026 | 02:53 / 02:53 | 18:44 / **18:44** | 22:43 / 22:42 |
| 1 Jul 2026 | **02:54 / 02:54** | **18:44 / 18:44** | **22:42 / 22:42** |
| 31 Jul 2026 | 03:39 / 03:38 | 18:28 / 18:28 | 22:02 / 22:03 |

**At the solstice — the single hardest date of the year — Fajr, Asr and Isha all match to the
exact minute.** Across five dates spanning ten weeks the largest disagreement is one minute.
They do not switch. Moonsighting all year.

For scale, MWL at the same solstice gives Fajr 02:34 and Isha **23:30** against the mosque's
02:47 and 22:45 — **45 minutes late**, in the season when the mismatch is largest and least
likely to be questioned.

**This closes the seasonal question and unblocks T1.** The recommendation to offer
Moonsighting at high latitude is now verified on both sides: the app reproduces the
convention correctly year-round (above), and the mosques genuinely use it year-round (here).

**Still not verified:** the same seasonal check for JMIC Slough. Its site publishes a monthly
PDF rather than a navigable table, and the PDF is served behind bot protection — `WebFetch`
returns 403, so it needs a browser session to read. JMIC matched Moonsighting exactly on
1 August, and Reading — the same convention, same region, 26 km away — holds it through the
solstice, so the expectation is that JMIC does too. That is an inference, not a measurement.
Diamond Road's begin times are not published anywhere found, so only its jama'ah column could
ever be read.

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

### The app crashed above the Arctic Circle — found by testing the world, not Slough (1 Aug 2026)

The owner's instruction was the whole reason this was found: *"Please don't just use Slough as
an example … we are trying to cater for the masses."* Every measurement to that point had been
taken at 51.5°N. Widening the sweep to the rest of the planet found a crash that had been in
every release so far.

**What happens.** Beyond the polar circles the sun can fail to set at all in summer, or to
rise at all in winter. adhan then returns `null` for **every** field — not only Fajr and Isha
but Dhuhr, Asr, sunrise and Maghrib, because it derives them all from a sunrise and sunset
that do not exist. `PrayerEngine.instantOf` takes a non-null `Date`, so this threw
`NullPointerException: fajr must not be null`.

**Measured boundary**, MWL, sweeping latitude in 0.5° steps:

| Date | Last latitude that computes | First latitude that returns null |
|---|---|---|
| 21 June | 65.5°N | 66.0°N |
| 21 December | 67.0°N | 68.0°N |

**The blast radius was the whole app, not one screen.** Every caller of the engine was
affected together: `SajdaViewModel` (phone), `WearViewModel`, `NextPrayerTileService`,
`OngoingBadge`, `PrayerPdfExporter` and — worst — `PrayerAlarmScheduler`, which runs in the
background. Both solstices, so summer *and* winter.

**This was not hypothetical.** Tromsø (69.65°N) has the Alnor Centre. Kiruna, Rovaniemi,
Luleå, Murmansk, Norilsk and Longyearbyen all have Muslim communities; Norilsk has thousands
of Central Asian workers. Every one of them installed this app and it died on open.

**What a mature reference does instead.** Aladhan does not crash. For Tromsø on 21 June it
returns `Fajr = Sunrise = Maghrib = Isha = 00:46` — degenerate and useless, but it renders.
Degrading beats crashing, so anything is better than what we shipped.

**The fix, and why this rule and not another.** `PrayerEngine` now tries the user's real
coordinates first and only falls back if any field comes back null. The fallback is
*aqrab al-bilad* — the times of the nearest place where night and day are still
distinguishable — projected to **60°**, keeping longitude and hemisphere so solar noon stays
true. 60 is not invented: it is the figure Moonsighting Committee publish for exactly this
case. `DayPrayerTimes.approximated` is set and the home screen shows a non-dismissible notice,
because a projection the user is not told about is the app taking a position silently.

Rejected: returning null times (ripples nullability through every screen, the PDF, the watch
and the scheduler, to produce a blank screen at the end of it); Aladhan's degenerate collapse
(renders, but six identical times are worse than honest approximations); *aqrab al-ayyam*,
nearest-day (defensible, but needs a search over dates where a latitude clamp needs none).

Guarded by `PolarAndHemisphereTest`, which asserts across all 14 calculation methods, both
solstices, seven polar locations including two southern ones, that times exist, are in
chronological order, are flagged approximate, and that `nextPrayer`/`currentPrayer` survive at
four times of day. It also asserts the projection stays **off** at Reykjavík (64.15°N) and
Luleå (65.58°N), the closest inhabited places to the boundary.

### MWL's Fajr errs in the unsafe direction almost everywhere (1 Aug 2026)

Both second opinions said the investigation had been Isha-only and that Fajr matters more,
because a fast begins at Fajr. That was right, and measuring it moved the conclusion.

MWL Fajr minus the region's own official method, from Aladhan, on a Ramadan date (20 Feb 2027)
and a June date. Positive means **MWL is later**:

| City | Local authority | Ramadan | June |
|---|---|---:|---:|
| Jakarta | Kemenag | +8 | +8 |
| ~~Kuala Lumpur~~ | ~~JAKIM~~ | ~~+8~~ | ~~+9~~ |
| Singapore | MUIS | +8 | +9 |
| Cairo | Egyptian | +7 | +10 |
| Casablanca | Morocco | +4 | +7 |
| Riyadh | Umm al-Qura | +3 | +3 |
| Istanbul | Diyanet | 0 | 0 |
| Karachi / Dhaka / Delhi | Karachi | 0 | 0 |
| Tehran | Tehran | −1 | −2 |

**MWL is never earlier than local practice anywhere measured except Tehran.** For a fasting
person that is the wrong way to err: a user on the default in Jakarta is still eating eight
minutes after their national timetable says the fast has begun. Nobody had noticed because
every previous check had been on Isha.

Note the direction reverses with latitude. At Slough, MWL Fajr is 15–42 minutes **earlier**
than the mosques' Moonsighting all year — cautious, not dangerous. **So the fasting risk is
not in Britain, it is in Indonesia.** Neither second opinion reached that; it took the numbers.

**⚠️ The Kuala Lumpur row above is struck through because it is wrong.** It was measured
against Aladhan's JAKIM method, which is stale. See "Aladhan was stale and I shipped it"
below. Malaysia's national Fajr criterion has been 18° since November 2019, which *is* MWL's
angle, so the correct figure for Kuala Lumpur is **0**, not +8/+9. Every other row stands.

### Aladhan was stale and I shipped it (1 Aug 2026, same day)

The worst mistake of this session, caught by a second-opinion review a few hours after the
commit went out.

**What happened.** adhan's `SINGAPORE` preset is Fajr 20° / Isha 18°. Checking it against
Aladhan showed those angles matching Aladhan's Kemenag (Indonesia), JAKIM (Malaysia) and MUIS
(Singapore) methods to the minute across six cities. On that basis the option was relabelled
`Kemenag / JAKIM / MUIS — Indonesia, Malaysia, Singapore` and committed.

**What was actually true.** Malaysia moved its national Fajr criterion from 20° to 18° at the
MKI Coordination Committee meeting of **20–21 November 2019**, following a year of research by
JAKIM with Universiti Malaya, UniSZA and UiTM. Subuh became roughly **eight minutes later**.
Confirmed from Malaysian government and state-mufti sources.

**Aladhan's JAKIM method still reports Fajr 20°.** It has been wrong for nearly seven years.
Verified directly from its own `/v1/methods` endpoint:
`id=17 Jabatan Kemajuan Islam Malaysia (JAKIM) params={'Fajr': 20, 'Isha': 18}`.

So the check confirmed the arithmetic perfectly and confirmed nothing about Malaysia. The
label was a lie about a real country's practice, and it shipped.

**What Singapore actually does — checked properly this time.** Not against an aggregator, but
against **MUIS's own published "Prayer Times for Singapore Year 2024"**, text-extracted from
their PDF:

| Date | MUIS printed Subuh | Computed at 20° | Computed at 18° |
|---|---|---|---|
| 1 Jan 2024 | 5:44 | **05:43** | 05:52 |
| 21 Mar 2024 | 5:52 | **05:52** | 06:00 |
| 21 Jun 2024 | 5:37 | **05:36** | 05:45 |
| 23 Sep 2024 | 5:37 | **05:37** | 05:45 |

Isyak matches 18° exactly on all four. **Singapore is genuinely 20°/18°.** Indonesia is
genuinely 20° — Kemenag has publicly reaffirmed it and NU calculates at 20°, though note that
Muhammadiyah, one of Indonesia's largest organisations, adopted 18° after its own review, so
even "Indonesia" is not one number.

**Corrected label:** `Kemenag & MUIS — Indonesia, Singapore`. Malaysia removed. The affected
population is ~240 million, not the ~270 million claimed in the first commit.

**And the direction of the original finding reverses for Malaysia.** MWL is *not* eight
minutes late there; at 18° it is the national criterion. A Malaysian on the default is
correct. `PolarAndHemisphereTest` now pins that relationship so the label cannot drift back.

**The rule this produces:** Aladhan is an excellent cross-check for *arithmetic* — does my
implementation of an 18° Fajr agree with another implementation of an 18° Fajr. It is not a
source of truth for *what a country currently observes*. Those are different questions, and
this session conflated them. A claim about a national authority needs that authority's own
current publication, and nothing less.

### The largest bloc of Muslims on earth could not find their own method (1 Aug 2026)

adhan's `SINGAPORE` preset is Fajr 20° / Isha 18°. Those are also, exactly, the angles used by
**Kemenag** (Indonesia) and **MUIS** (Singapore) — verified for Singapore against MUIS's own
2024 timetable on four dates, and for Indonesia against Aladhan's Kemenag method plus Kemenag's
own public reaffirmation of 20°.

> **This paragraph originally also claimed Malaysia and Brunei, on the strength of Aladhan's
> JAKIM method. That was wrong and it shipped.** Malaysia moved to 18° in November 2019 and
> Aladhan has not caught up. See "Aladhan was stale and I shipped it" immediately above; the
> corrected figure is ~240 million people, not ~270 million.

Labelled simply *"Singapore"*, that option was invisible to roughly **240 million** Muslims who
had no reason to look at it. The maths was already correct and already shipping; only the label
was wrong. Now `Kemenag & MUIS — Indonesia, Singapore`.

The enum stays `CalcMethod.SINGAPORE` because that name is written into saved settings; only
`method_singapore` changed. Pinned by `PolarAndHemisphereTest`, so an adhan upgrade that moves
these angles fails the build rather than quietly making the label a lie.

### What the two second opinions got right and wrong (1 Aug 2026)

Neither was accepted on trust. Every checkable claim was tested against adhan's bytecode, the
running engine, or Aladhan.

**Right, and acted on:**

- *adhan ignores `HighLatitudeRule` entirely for Moonsighting.* Confirmed twice. In the
  bytecode, `nightPortions()` is computed and then discarded for that method in favour of
  `seasonAdjustedMorningTwilight`/`seasonAdjustedEveningTwilight`; on the numbers, all three
  rules give identical output at 51.51°, 54°, 56° and 58°. `PrayerEngine`'s comment claimed
  otherwise and has been corrected. Now guarded by a canary test.
- *Above 55° Moonsighting also forces a one-seventh clamp.* Confirmed — and visible as a
  discontinuity: Fajr on 21 June is 02:28 at 54° but jumps to **03:21** at 56°.
- *`TWILIGHT_ANGLE` is itself a religious approximation, not a rounding.* Confirmed from
  `nightPortions()`: MIDDLE = 0.5 of the night, SEVENTH = 0.1428, TWILIGHT = angle/60, so MWL
  Isha caps at 17/60 = **28%**. At 51.5°N on 21 June the three give 01:04/01:04 (collapsed),
  03:42/22:27, and 02:32/23:29. Three different answers, one silently chosen for the user.
- *Fajr had never been measured.* Correct, and it changed the conclusion — see above.
- *Rejecting Kemenag/JAKIM on an Isha-only comparison was unsound.* Correct. On Isha they are
  within 4 minutes of MWL, which is why they were dismissed; on **Fajr** they are 8–9 minutes
  apart, and that is the one that matters for fasting.
- *The `latitude >= 55` Moonsighting clamp uses a raw latitude, not `abs()`.* Confirmed in
  bytecode. Consequence measured: at +56° on 21 June Fajr sits 55 minutes before sunrise, but
  at −56° on 21 December it sits **2 hours** before. A real asymmetry — see §11.

**Wrong, or overstated:**

- *"Umm al-Qura is 30 minutes early during Ramadan."* **False for this app.** True of the
  library, but `PrayerEngine.ishaFor` has always added the 30 minutes, using
  `HijrahChronology`, which *is* the Umm al-Qura calendar — so the method and the calendar
  agree by construction. The reviewer inferred from adhan's source without checking ours.
- *"DST transitions may be off by an hour."* **Not a defect.** Verified across all four 2026
  transitions in both hemispheres: London 28→29 Mar Dhuhr 12:08→13:08, 24→25 Oct 12:48→11:47;
  Melbourne 4→5 Apr 13:24→12:24, 3→4 Oct 12:10→13:10. `java.time` handles it; the engine
  returns `Instant` and formatting is the caller's job, which is the right split.
- *"The Southern Hemisphere seasonal model is northern-centric."* **False.** adhan's
  `daysSinceSolstice` branches on `latitude >= 0` and measures from the June solstice in the
  south, and `seasonAdjusted*Twilight` uses `Math.abs(latitude)`. Only the 55° clamp is wrong.
- *"London Unified is 12°/12°."* One reviewer asserted this, the other corrected it to Khalid
  Shaukat's observation-guided seasonal model. **Not verified here either way** — flagged in
  §11 rather than acted on, because a claim about a named authority needs their own document.
- *"MWL is a bad default."* Neither said this outright, but both leaned that way. It is still
  wrong: MWL is within ~5 minutes of local practice for most of the Ummah and only fails above
  ~45°. See the earlier worldwide table. The defect is discoverability, not the default.

**Measured method spread by latitude**, worst case over a full year across MWL, Egyptian,
Karachi, ISNA and Moonsighting. This is the evidence for any latitude threshold, and it is
symmetric, so the trigger must be `abs(latitude)`:

| Latitude | Max Fajr spread | Max Isha spread |
|---:|---:|---:|
| 0° | 20 min | 13 min |
| 20° | 24 min | 16 min |
| 30° | 28 min | 21 min |
| 40° | 40 min | 46 min |
| **45° / −45°** | **57 min** | **74 min** |
| 50° | 66 min | 88 min |
| 55° | 102 min | 95 min |

45° is where the spread crosses an hour, and −45° matches +45° exactly. That is the number to
use, on absolute latitude, if a prompt is ever built.

### A9 settled, and a second polar bug found underneath it (1 Aug 2026)

A9 asked whether 60° was the right polar reference or Moonsighting's rule wrongly applied to
everyone. Answering it properly meant reading what each body actually publishes rather than
what another calculator does — lesson 45a — and the answer turned out to be **both numbers are
right, for different users**. Along the way an exhaustive sweep found a second, unrelated
polar defect that had shipped in every release.

**1. The Fiqh Council's 45° is real, and the band edges prove it.**

The resolution is reported consistently by IslamOnline, islamicfiqh.net, the `go-prayer`
library and others: Islamic Fiqh Council of the Muslim World League, resolution 6, ninth
session, Makkah, 12–19 Rajab 1406, endorsed by the ECFR. Bands of 45–48°, 48–66°, and beyond
66° estimating **all** times from 45°.

Secondary sources can repeat each other's mistakes, so the bands were checked against the sky
instead. The sun's altitude at local solar midnight is `latitude + declination − 90`:

| Latitude | Max solar depression, June solstice | 18° reachable? |
|---|---|---|
| 45° | 21.56° | yes, comfortably |
| 48° | 18.56° | yes |
| **48.56°** | **18.00°** | **exactly the limit** |
| 55° | 11.56° | no |
| 60° | 6.56° | no |
| 66.56° | 0.00° | polar circle |

48.56° is precisely where 18° twilight stops happening, and 66.56° is precisely the polar
circle. **No one inventing or garbling a resolution lands on both of those.** The bands are
astronomy, and the sources are reporting them faithfully.

It also settles the substance: at 45° a real Fajr and a real Isha exist. At 60° neither does —
18°, 17°, 15° and even 12° are all unreachable at midsummer, so projecting there means
borrowing an estimate rather than a measurement.

**2. Moonsighting's 60° is also real — verified against their own published consequence.**

Their page states it verbatim, and states what it costs: at Oslo the longest fasting day
becomes 19h38m and the shortest 7h43m. Running this engine at their reference latitude gives
**19h39m and 7h41m**. Within rounding on both. So adhan's Moonsighting model plus this
projection genuinely reproduces what Moonsighting publish, and a Moonsighting user should keep
getting 60°.

**3. What it costs a real user at Tromsø (69.65°N):**

| | Fajr | Maghrib | Fasting day | Dhuhr → Asr |
|---|---|---|---|---|
| 21 Jun from 45° | 02:26 | 20:35 | 18h09m | 248 min |
| 21 Jun from 60° | 01:47 | 22:12 | **20h25m** | 281 min |
| 21 Dec from 45° | 05:32 | 16:05 | 10h33m | 125 min |
| 21 Dec from 60° | 06:00 | 14:38 | 8h38m | **51 min** |

A 20h25m fast is past the eighteen-hour limit of the very fatwa Moonsighting cite, and a
51-minute Dhuhr window is short enough to miss. For everyone who did not choose Moonsighting,
45° is both better sourced and kinder.

**4. The second bug: adhan returns confident nonsense, not only nulls.**

A sweep of every method, every madhab, every 0.5° of latitude from pole to pole and every day
of 2026 — 7.3 million computations — turned up **2,239 days with times out of order**, at
every latitude from 66° to 89.5° **in both hemispheres**. Blamed slot: Asr, 1,549 times.

Worst case: **71.5°N, 27 January 2026 — Asr returned as 13 March, forty-five days out.** The
sun barely clears the horizon there, the shadow ratio Asr is defined by is never reached, and
rather than giving up adhan hands back whatever its root finder landed on. `isComplete` only
checked for null, so these days were displayed, exported to PDF and used to schedule alarms.
**This was in v1.0.0 and in the build now in closed testing.**

A third failure survived both the null check and the ordering check: on the day polar day
ends, sunrise and sunset happen minutes apart either side of midnight and adhan can pair a
sunrise from one night with a sunset from another. At 78°N Maghrib came out as 17:00 on
24 August and 22:29 the next day. A sunset does not move five and a half hours overnight.

The physics that catches it is that **solar noon is the midpoint of the day arc**. Swept
globally, the largest honest offset between Dhuhr and that midpoint anywhere below 65° is
**three minutes**; above 65° the same sweep produced 59, 87, 143, 212 and 214. The threshold
is 30 minutes — ten times the worst honest case, and nowhere near any dishonest one.

After the fix: **0 faults in the same sweep.**

**5. What was checked and found *not* to be a problem.**

- *Does the projection ever override a sunset the user could watch?* No. Measured at Tromsø,
  Rovaniemi, Luleå, Reykjavík and Longyearbyen: of 118 nulled days at Tromsø, **zero** had a
  real sunrise and sunset. adhan's nulls occur exactly at apparent polar day or night. Derived
  independently: no sunset above `89.167 − δ = 65.727°`, no sunrise above `90.833 − δ =
  67.393°`; measured 65.8 and 67.4 at 0.1° resolution, and the 1.666° asymmetry between them
  is exactly twice the 0.833° refraction and semi-diameter allowance. Nothing observable is
  ever replaced.
- *Is the engine's sunrise/sunset accurate?* An independent NOAA-based service
  (api.sunrise-sunset.org) differs by 2–7 minutes, growing with latitude, which looked
  alarming. **Aladhan — a separate implementation — agrees with this engine to the minute**
  (Slough 21 Mar sunrise 06:03 / sunset 18:17 on both; Tromsø 04:39 / 17:05–06). adhan uses
  the standard −0.8333° horizon, confirmed in bytecode. Two mature prayer-time engines against
  one low-precision 1990 almanac algorithm: the outlier is the almanac. No change.
- *Where exactly is the polar-day boundary?* Genuinely ambiguous to within a day at the
  transition, because a 3-minute algorithmic difference flips it. That is not fixable, and it
  is the reason the app's job there is to be **safe**, not precise.

**6. The notice, finally seen on a real screen (1 Aug 2026) — and how to do it again.**

Two previous sessions failed to photograph the polar banner because they fought the emulator's
location stack. That was the wrong tool. **The app has a city search, and it works.**

Recipe, which takes about two minutes:

1. Install and open the app, grant coarse location so onboarding's **Continue** enables (it
   stays disabled until a location exists — the emulator will offer somewhere in England).
2. Finish onboarding, then tap the location chip at the top of the home screen → **Or type a
   city**.
3. Type a city that is in polar day or night **today**. This is the part earlier attempts got
   wrong: on 1 August, Tromsø has already come out of polar day, so it shows ordinary times.
   **Longyearbyen (78.22°N) is in polar day until late August** and works. In December almost
   any Arctic town will do.

Verified this way:

| Setting | What the screen said |
|---|---|
| Default (Muslim World League) | *"…worked out from **latitude 45°** instead."* |
| Settings → Moonsighting Committee | *"…worked out from **latitude 60°** instead."* |

So the method-dependent reference latitude is confirmed end to end — engine, model, string
and screen — not merely in a unit test. The card is not dismissible (no ✕, unlike the
exact-alarm card directly beneath it), which is the intended difference.

**RTL, via `./gradlew installRtl`:** the card mirrors correctly and, importantly, the number
survives — it renders as `latitude 60°`, with the numeral and the degree sign together and in
that order, inside the right-to-left paragraph. The usual force-RTL artefacts on the
surrounding *English* text are present and expected (the full stop moves to the left, "Aug"
reverses); they are an artefact of laying English out RTL, not a defect, and are already
described in "Right-to-left, previewed before any translation exists".

**Build trap found on the way.** `./gradlew installRtl` and `:app:installDebug` both fail with
`INSTALL_FAILED_VERSION_DOWNGRADE: Update version code 2 is older than current 1000` when the
**watch** emulator is also connected — the watch carries the same applicationId at a much
higher version code, and `ANDROID_SERIAL` is not honoured by `installRtl`. Fix: build with
`:app:assembleRtl` and install with `adb -s emulator-5554 install -r -d <apk>`.

**7. A known divergence, deliberately not fixed.** Moonsighting say they slide to 60° above
60°, but neither adhan nor the PHP library behind Aladhan implements that, so a Moonsighting
user between 60° and 65.7° gets un-slid times. Measured gap: Helsinki 1 min, Anchorage 10,
Trondheim 37, Reykjavík 49, **Luleå 91**. At Luleå in June the un-slid result is degenerate —
Isha 00:14 and Fajr 00:52, 38 minutes of night. Recorded as A12; not changed, because it needs
partial substitution (Fajr and Isha only, sunrise and sunset staying local) which is a design
decision, not a constant.

### The second round of tester feedback, and a first-run trap underneath it (1 Aug 2026)

Five complaints arrived from the closed test. Four were about alerts; one was *"user typing
city name but its not selecting it"*, with a screenshot. Every one of them was reproduced on
a device before anything was changed, and reproducing the last one found something nobody had
reported.

#### 1. The first-run dead end — the worst defect in this file since the polar Asr

**A user who declined location and mistyped a city could not proceed, at all.**

`OnboardingScreen.PermissionStep` showed the city block only while `problem` was
`PERMISSION_DENIED` or `NO_FIX`. Searching sets `problem` to null, and a failed search sets
it to `CITY_NOT_FOUND`. **Neither is in that pair.** So pressing "Find city" made the
heading, the field, what had been typed, the button, the error message *and the "use Makkah"
escape hatch* all disappear in the same frame — leaving a screen with "Allow location", which
the user had already refused, and a **disabled** Continue.

Reproduced on an API 36 emulator: typed `zzqqxxnowhere`, pressed Find city, and the screen
went to two controls, one of them dead. There is no way forward from there except granting
location or reinstalling. **It shipped in v1.0.0 and it is in the build in closed testing
now.** The `city_not_found` message the user needed was inside the block that had just
vanished, which is why nobody could have diagnosed it from the app.

Fixed by deleting the condition rather than extending it: the city entry is shown whenever
there is no location yet, full stop. No transient state, so no state to get wrong. It also
closes T3's fourth point for free — manual entry was always there and nobody could find it.

#### 2. What the tester actually saw: a search that worked and said nothing

The reported bug was in the Settings location sheet, and the search was **not** broken. Typed
"slough", pressed Find city, and Settings behind the sheet did change to "Slough, United
Kingdom". The sheet simply stayed open with the typed text still in it and no confirmation of
any kind. The only feedback was a spinner next to the *GPS* button reading "Finding your
location…" — the wrong sentence, in the wrong place, for a typed-in city.

Three separate faults, all confirmed by `uiautomator dump`:

| Fault | Evidence | Fix |
|---|---|---|
| Sheet opened half-height; "Find city" off-screen entirely | field bounds `[63,1909][1017,1920]` on a 1080×1920 screen, button not in the tree until the sheet was dragged up | `skipPartiallyExpanded = true` |
| No confirmation, sheet never closed | location changed, sheet unchanged | close on success; the header behind it *is* the confirmation |
| Wrong progress text, wrong place | "Finding your location…" beside the GPS button | shared `ProgressRow`, new string, under the field it belongs to |

Onboarding additionally had no `imePadding` and no IME action at all — the keyboard's own
search key only closed the keyboard. Both added; the sheet already had them.

**The obvious diagnosis was wrong and would have cost a day.** The screenshot showed a button
under a keyboard, so insets looked like the cause. They were not: material3 1.4.0 already
applies `imePadding()` inside the sheet's own dialog window unconditionally, so a nested one
is a no-op and `contentWindowInsets = WindowInsets(0)` cannot switch it off. The real cause
was the sheet's half-height default, visible **before any keyboard was involved**. Sourcing
in `docs/reviews/2026-08-01-android-alarm-facts.md` §5; lesson 55.

#### 3. Late notifications — measured at a one-hour window, not "a few minutes"

`dumpsys alarm`, with "Alarms & reminders" not granted, which is the **default** state from
Android 13:

```
type=RTC_WAKEUP origWhen=2026-08-01 23:48:00.000 window=+1h0m0s0ms flags=0x20
```

After `appops set com.sajdatime.app SCHEDULE_EXACT_ALARM allow`, **without reopening the
app** — so this also verifies the new permission-state receiver:

```
type=RTC_WAKEUP origWhen=2026-08-01 23:48:00.000 window=0 exactAllowReason=permission flags=0x3
Next alarm clock information:
  user:0 pendingSend:false time:1785624480000 = 2026-08-01 23:48:00.000
```

`flags=0x3` and the populated "Next alarm clock information" are `setAlarmClock`. After a
clean onboarding with the permission granted, all six upcoming alarms read `window=0`.

Two changes followed. Notification style moved from `setExactAndAllowWhileIdle` to
`setAlarmClock` (§6 for the reasoning and the rejected alternative), and onboarding's last
step now asks for the permission outright — a banner about a setting is not the same as being
asked. The banner's wording changed too: "a few minutes late" was a promise the platform does
not make, and one hour is what was measured.

#### 4. The alarm sounding through a silenced phone — confirmed, and both directions tested

Fired the alert at each ringer mode and read back which channel the system used:

| Ringer | `alarmRespectsSilent` | Channel used |
|---|---|---|
| NORMAL | on | `prayer_alarm_v0` — sounds |
| VIBRATE | on | `prayer_times` — quiet |
| SILENT | on | `prayer_times` — quiet |
| SILENT | **off** | `prayer_alarm_v0` — sounds anyway |

The notification is posted in every one of those cases; only the channel differs. Both the
silent badge (`id=1000`, `next_prayer_badge`) and the prayer alert (`id=2000`) were present in
`dumpsys notification` at the same time, which answers the tester's *"notification badge and
alarm could be done together"* — they always could, and now the screen says so.

#### 5. The rest of the gate

Per-prayer alerts driven through the UI end to end: set Fajr to Alarm, watched the summary
become "All five · Mixed", and read the stored value back out of DataStore as
`FAJR:ALARM,DHUHR:NOTIFICATION,ASR:NOTIFICATION,MAGHRIB:NOTIFICATION,ISHA:NOTIFICATION`.
Checked in RTL via `:app:assembleRtl` — the three chips mirror correctly, Off on the right.
At `font_scale 1.5` the `FlowRow` wraps "Alarm" onto a second line inside the dialog rather
than clipping it, which is the reason it is a `FlowRow` and not a `Row`. The keyboard's search
key was verified to run the search (`input keyevent 66`), and the sheet was re-checked at
`wm size 1080x1100` — roughly the space left above a large keyboard — where "Find city" is one
scroll away rather than unreachable.

**Not tested:** a real device, a real Samsung keyboard (the emulator has a hardware keyboard,
so `wm size` was used as a proxy), and App Standby bucket behaviour, which cannot be forced
honestly on an emulator and which §6 explicitly does not claim to have fixed.

*The first of those three was closed the next day — see below. The emulator had been telling
the truth about the fix and hiding two things about the world it runs in.*

### The shipped app measured on real hardware, beside the fix (2 Aug 2026)

The owner attached his own phone: a Xiaomi Redmi Note 13 Pro 5G (`2312DRA50G`, codename
garnet) running **HyperOS 3 on Android 16, API 36** — the first physical device this project
has ever been tested on, and by luck the most hostile OEM for background alarms.

The Play closed-test build was already installed on it (`installerPackageName=com.android.vending`,
versionCode 2, installed 31 Jul). It was never touched. `docs/RELEASING.md` records that an
early uninstall *resets* a tester rather than merely failing to count them, and the same
`applicationId` with a different signature cannot install over it — so a `sideload` build type
exists purely to sit beside it (`applicationIdSuffix = ".sideload"`, `app_name` overridden to
"SajdaTime (test)"). Both packages coexisted for the whole session.

#### What the shipped app is actually doing on a user's phone

Read straight out of the device's own alarm table, not inferred:

```
com.sajdatime.app   RTC_WAKEUP  origWhen=2026-08-02 02:51:00  window=+1h0m0s0ms  flags=0x20
```

All ten scheduled prayer alarms carried a **one-hour window**, `maxWhenElapsed` a full hour
past `whenElapsed`. Fajr, scheduled for 02:51, was permitted to arrive at 03:51. `flags=0x20`
is `FLAG_ALLOW_WHILE_IDLE_COMPAT` — the *inexact* variant. The tester's "notifications arrive
late" was not a slow phone or a one-off; it is the shipped behaviour, and the delay is bounded
only by an hour.

The cause, also read off the device: `SCHEDULE_EXACT_ALARM` appears under `requested
permissions` but is **absent from the granted list**. Since Android 14 it is not granted at
install for apps targeting 33+. Every install on Android 14/15/16 lands without it, and every
alert silently degrades. Nothing in the app noticed or said so.

#### The fix, on the same phone, at the same moment

| | Shipped v1.1.0 | This branch |
|---|---|---|
| Fajr 02:51 | `window=+1h0m0s0ms` | `window=0` |
| flags | `0x20` (allow-while-idle **compat**) | `0x3` (`STANDALONE\|WAKE_FROM_IDLE`) |
| exactness | — | `exactAllowReason=permission` |
| slack | `maxWhenElapsed = +1h` | `maxWhenElapsed == whenElapsed` |

`flags=0x3` is the signature of `setAlarmClock`. Zero slack, on the hardware, side by side
with the bug. §6's ladder does what it claims.

#### Three things the emulator could not have shown

1. **The app had fallen to App Standby bucket 40 — RARE** (`am get-standby-bucket`, stable
   across three reads), while the freshly-installed test build sat at 10 (ACTIVE). This is
   the concrete demonstration of the asymmetry recorded in §10 and §6: `USE_EXACT_ALARM`
   floors an app at WORKING_SET, `SCHEDULE_EXACT_ALARM` only does so *at targetSdk ≤ 33*, and
   this app targets 36. It therefore has no floor, and a real user's phone duly dropped it two
   buckets. §6 is right not to claim buckets are fixed.

2. **`setAlarmClock` takes over the phone's system "next alarm" slot.**
   `Next alarm clock information: user:0 time:… = 2026-08-02 02:51:00`. The lock screen and
   status bar report Fajr as the next alarm in place of whatever the user set in their own
   clock app. Their alarm still fires; the readout is what changes. The comment in
   `PrayerAlarmScheduler` called this "a cosmetic cost" — it is a little more than that, and
   the comment has been corrected to say so. The decision stands on this project's own
   tiebreaker: a missed Fajr beats a confusing readout.

3. **HyperOS applies a power policy that AOSP does not have.** While the phone sat idle, every
   alarm on it — SajdaTime's, Photos', Maps', Messaging's, Fitness' — carried
   `power_pending = requester + 3 days`, making `whenElapsed` three days out. Once the device
   woke, `power_pending` went to `--` on all 258 alarms and every time returned to normal.
   Uniform across apps, so not something SajdaTime provokes. Two dumps could not tell
   "delays delivery" apart from "placeholder released on wake", so it was measured overnight
   rather than assumed. **It delays delivery.** See the next subsection.

#### A method that produced nothing, recorded so it is not repeated

Several attempts were made to trigger an alert with
`am broadcast -n …/PrayerAlarmReceiver -a …PRAYER_ALARM`. Every one reported
`Broadcast completed: result=0` and did nothing: no process start, no notification, no log
line. The receiver is `android:exported="false"`, so a broadcast from the adb shell — a
different uid — is dropped silently rather than refused loudly. Adding
`FLAG_INCLUDE_STOPPED_PACKAGES` did not help, because that was never the problem.

This nearly produced a reported bug that did not exist. The "missing" notification was the
test, not the code. `-f 0x20` and `Broadcast completed: result=0` both look like success and
neither is. If a future session needs to fire an alert without the UI, drive the UI or wait
for a real alarm; do not broadcast at a non-exported receiver and read silence as a defect.

### The overnight A/B at Fajr — HyperOS does defer, and `setAlarmClock` defeats it (2 Aug 2026)

The question left open above was settled by the cleanest experiment the situation allowed:
**both builds on one phone, both holding a Fajr alarm at exactly 02:51:00**, differing only
in which `AlarmManager` API placed it. The phone was left plugged in, screen off, untouched
from 00:28 to 04:22. The pending table, the notification list, the Doze state and the
wakefulness were sampled every 30 seconds, and a full `logcat` ran alongside as an
independent witness.

**The result was not close.**

| | Shipped v1.1.0 (`setAndAllowWhileIdle`) | This branch (`setAlarmClock`) |
|---|---|---|
| Fired at | **never** | **02:51:00.037** |
| Still pending at 04:23 | yes | no — it fired and rechained |
| `power_pending` | `+2d22h29m7s356ms` | `--` |
| `whenElapsed` | rewritten to **+2 days 22 hours** | `+8h45m` (the next prayer, unmoved) |
| `logcat` "sending alarm" | 0 occurrences | 1, at 02:51:00.037 |

The shipped build's Fajr alarm did not arrive an hour late. **It did not arrive at all**, and
at the time of writing the system intends to deliver it on 5 August. Its `whenElapsed` and
`maxWhenElapsed` were both overwritten with the `power_pending` value — this is not a window
being widened, it is the delivery time being replaced.

```
com.sajdatime.app           origWhen=2026-08-02 02:51  window=+1h  flags=0x20
  policyWhenElapsed: requester=-1h30m52s  app_standby=-3h3m33s  device_idle=--
                     battery_saver=-3h3m33s  ssru=-3h3m33s  power_pending=+2d22h29m7s
  whenElapsed=+2d22h29m7s  maxWhenElapsed=+2d22h29m7s      ← moved, not widened

com.sajdatime.app.sideload  origWhen=2026-08-02 13:10  window=0   flags=0x3
  policyWhenElapsed: requester=+8h45m44s  app_standby=-1h33m15s  device_idle=--
                     battery_saver=--  ssru=-1h33m15s  power_pending=--
  whenElapsed=+8h45m44s  maxWhenElapsed=+8h45m44s          ← untouched
```

**The confound was eliminated, which is the part that makes this worth trusting.** Both
packages were in **App Standby bucket 40 (RARE)** — read at the same moment, identical. Doze
was not involved (`mState=ACTIVE`, `device_idle=--` on both). Battery saver was off. Both
were on the same charger, in the same minute, for the same prayer. Every plausible
alternative explanation is negative in the policy line above; the **only** binding constraint
on the shipped alarm is `power_pending`, and the only difference between the two builds is
which API scheduled them.

Xiaomi's own log says the same thing in its own words. `AlarmManager` printed
**`not align this alarm: Alarm{… com.sajdatime.app.sideload}, reason=6`** eighteen times
during the window and never once for the shipped package — its alignment engine explicitly
excluding the exact alarm-clock alarms from the batching it applies to everything else.

#### What this means for a real user, and why the tester's wording understated it

`power_pending` clears to `--` the moment the phone wakes (observed on all 258 alarms). So on
a Xiaomi the shipped app does not produce a *late* notification on a timer — it produces
**nothing at all until the user picks the phone up**, at which point the backlog is released
and the alert appears. That is exactly what "notifications arrive late" feels like from the
outside, and it is why the complaint was worth taking literally rather than explaining away:
the user reporting it was describing a real mechanism they had no way to name.

Xiaomi, Redmi and POCO are one brand family, and it is a dominant one in Pakistan, India,
Indonesia, Bangladesh, Nigeria and the Gulf — much of the audience this app exists for. On
that hardware, the pre-fix build's prayer notifications were not degraded. They were absent.

#### What this does *not* establish

- Only Fajr was observed firing. The other four prayers of 2 Aug were still pending when the
  watch ended; they are expected to behave the same but were not watched.
- One night, one device, one OEM. Samsung's One UI has its own background policy and has not
  been tested; the owner has an S23 Ultra and it is the obvious next check.
- The measurement was taken with Do Not Disturb set to `ZEN_MODE_ALARMS` from 02:09 so the
  02:51 alerts would not wake the household. DND suppresses presentation, not posting, and
  the primary signal was the alarm leaving the pending table, which DND cannot affect — but
  it is recorded here because a suppressed alert is not a *silent* one, and nobody heard this
  fire.

### A second OEM: the Galaxy S23 Ultra (2 Aug 2026)

The owner attached a **Samsung Galaxy S23 Ultra (SM-S918B), One UI on Android 16 / API 36**,
which is the other half of the market the Redmi represents. Two gates had to come down first
and both are worth knowing: **Auto Blocker** greys out USB debugging entirely
(*Settings → Security and privacy → Auto Blocker*), and the "Allow USB debugging?" prompt
must then be accepted or the device sits at `unauthorized` forever. Unlike HyperOS, One UI
then permits everything: `input tap`, `pm grant`, `settings put global`. Every UI check below
was impossible on the Redmi and routine here.

#### The bug is not Xiaomi's fault, and that is the important part

The Play build on this phone looked *healthy* — all ten alarms `window=0`, `flags=0x3`. That
is not the fix working; it is a coincidence. This phone happens to have
`SCHEDULE_EXACT_ALARM` allowed **and** all five prayers set to Alarm style, and the shipped
code already used `setAlarmClock` for that one combination. The phone was never showing the
defect.

So the permission was revoked on the test build to recreate a fresh user, on the same phone,
minutes apart:

| Galaxy S23 Ultra, test build | `flags` | `window` |
|---|---|---|
| Exact-alarm permission **denied** (what a new user gets) | `0x20` | **`+1h0m0s0ms`** |
| Exact-alarm permission **granted** | `0x3` | `0` |

**The one-hour window reproduces exactly on Samsung.** The shipped defect is not an OEM
quirk — it is what Android 14+ does to any app that targets 33+ and has not been granted the
permission, which is every fresh install on a modern phone.

What *is* OEM-specific is the punishment. Samsung's policy line reads
`app_standby=-2m54s device_idle=-8m10s battery_saver=-- gms_manager=--` — every constraint
negative, nothing binding, no `power_pending` equivalent anywhere. On One UI "up to an hour
late" is literally true. On HyperOS the same weak alarm is moved three days. **Same bug, and
the severity is decided by the badge on the phone.**

One thing did repeat: Samsung's `nextUserAlarmTime` was `11:02:00` — SajdaTime's alarm, not
the user's. The system next-alarm takeover recorded above is not a Xiaomi behaviour either.

#### The onboarding permission step, driven through the real UI at last

Never tested before this, on any device, because the Redmi blocked input. Walked end to end:

1. **Allow exact alarms** on the last onboarding step opens
   `com.android.settings/.Settings$AlarmsAndRemindersAppActivity` for the app — the right
   screen, not a generic settings page.
2. Granting it makes the whole "One last thing" block disappear from the summary.
3. All nine scheduled alarms immediately became `window=0 exactAllowReason=permission
   flags=0x3`.

The banner-to-settings-to-granted path is now verified rather than assumed.

#### T6, watched firing on a silenced phone

The complaint was "the alarm should stay quiet when the phone is silent". Set up as the
tester would have it — Dhuhr on **Alarm** style, sound "Cowbell", *Stay quiet when your phone
is silent* **on**, ringer mode **SILENT**, alarm stream unmuted — and left to fire:

```
11:01:43 pend=2 note=6  playing=0
11:02:04 pend=0 note=10 playing=0        ← alarm gone from the queue, alert posted
```

```
08-02 11:02:00.801  StatusBarNotification(pkg=com.sajdatime.app.sideload id=2002
  Notification(channel=prayer_times ... vibrate=null sound=null defaults=0 ...))
```

Posted **801 ms** after the target. Note the channel: `prayer_times`, not the alarm channel —
the alert was **downgraded from Alarm to Notification style at fire time**, which is exactly
what §5.13 specifies. `importance=4` so it still surfaces; `sound=null vibrate=null` so it
makes no noise; and `playing=0` across every 20-second sample, meaning no audio stream ever
started. On time, and silent. That is the whole of T6, measured.

#### Everything else walked on this phone

- **City search (T4)** — typed "Lahore", resolved to "Lahore, Pakistan" with a tick and a
  Continue button. Fixed, on hardware.
- **Per-prayer alerts (T7)** — Off/Notification/Alarm independently per prayer; alarm sound
  opens the system picker (a chooser first, on a phone with Zedge installed) and the chosen
  tone round-trips back as "Cowbell".
- **Disclaimer** — intact, dua request in the final paragraph.
- **Qibla** — Cairo: 136° and 1287 km. Lahore: 260° and 3599 km. Both correct.
- **PDF** — a full month for Cairo, 31 rows, six columns, offline footer.
- **Font scale 1.5×** — Times and Settings both hold; wrapping, no clipping.
- **Light theme, landscape, rotation** — all clean.
- **No crashes** in `logcat -b crash` across the whole walk.

#### Two defects that were not defects

Recorded because both looked real enough to report and both would have been wrong:

- **"The alarm sound picker is missing."** The Prayer alerts dialog scrolls, and the picker
  plus the silent switch live below the fold. The screenshot was cut off, not the UI.
- **"The Qibla screen is clipped in landscape."** `You are facing 24°` was sliced by the
  navigation bar — and scrolled into view perfectly. Content taller than a landscape
  viewport is not clipping.

#### What this phone confirmed about the timezone item

With Cairo selected, the app and the exported PDF both show Dhuhr as **11:02**, which is UK
time; Cairo's own Dhuhr is 13:02. §11 item 4 already records this for the screen. The PDF is
worse than the screen: it is titled "Prayer times for Cairo, Egypt", carries no timezone
label, and is a document someone prints and pins up, where there is no live countdown to give
the game away. Item 4 should be read as covering the export too.

### RTL run on real hardware for the first time, and the real bug underneath it (2 Aug 2026)

`./gradlew installRtl` had only ever run on an emulator, and the reason turned out to be
structural rather than anything to do with RTL: the `rtl` build type shared the release
`applicationId`, so a debug-signed `com.sajdatime.app` could not install beside a Play copy.
Every phone worth testing on — the owner's two, and every tester's — is exactly that phone,
and the only way through was uninstalling the Play build, which `docs/RELEASING.md` records
as the thing that resets a closed tester. **`rtl` now carries `applicationIdSuffix = ".rtl"`
and its own `app_name`**, for the same reason `sideload` does, and RTL is now runnable
anywhere.

On the S23 Ultra the mirroring is correct: prayer names right and times left, bottom
navigation reversed to Settings | Qibla | Times, banner icons and dismiss controls swapped,
forward chevrons pointing left. Nothing clipped, nothing overlapping.

#### The owner looked at that screen and said it was wrong. He was half right, and the half he was right about mattered.

What he was looking at was the **preview build doing its job**: English words laid out
right-to-left, with the full stop of ".A helper, not a religious authority. Tap to read"
stranded at the wrong end. That is not a defect and it cannot reach a user — `AppLocale`
pins layout direction to the language the app's own words are in, so the shipped English app
is left-to-right, and every screenshot in this session's walk shows it that way. `ar-XB` is a
reserved pseudolocale that no real phone can be set to.

But the instinct behind the complaint was right, and it found something real that had been
sitting there the whole time: **there was no bidi isolation anywhere in the codebase.**
`grep` for `BidiFormatter`, `unicodeWrap`, `LayoutDirection` returned nothing.

That does not matter while the app ships only in English. It matters enormously on the day
`values-ar/` or `values-ur/` lands, because of one asymmetry: **every string in `values/` is
written by us or by a translator, so it is in the same language as the sentence around it —
except the city name.** City names come from the Open-Meteo geocoder and are usually Latin
*even for a user reading the app in Arabic*. Someone in Lahore sees "Lahore, Pakistan".

A Latin run inside a right-to-left sentence is precisely the case the Unicode bidirectional
algorithm resolves from the characters on either side of it, and those characters are the
translator's. It is the same fault `AppLocale` already documents for digits, where
"17 Safar 1448" rendered as "١٤٤٨ Safar ١٧" and read as a different date.

`core/Bidi.kt` fixes it with two characters — FSI (U+2068) and PDI (U+2069) — applied at the
three places a city name lands inside a sentence: the home screen's spoken location label,
the export sheet body, and the PDF title. Deliberately **not** `BidiFormatter`, which needs a
`Context`, cannot run in a JVM unit test, and would tell the algorithm a direction that FSI
detects for itself.

Verified rather than reasoned about, because both halves could have gone wrong:

- `BidiTest` runs the actual UBA over an Arabic sentence via `java.text.Bidi` and asserts the
  city stays one unbroken run — not merely that two characters were appended, which would
  pass with the wrong pair.
- **In English it must be invisible, and it is.** The export sheet on the emulator still
  reads "…for Slough, United Kingdom." exactly as before.
- **The PDF was the real risk**: `Canvas.drawText` could have drawn the isolates as tofu
  boxes in a printed document. Exported and rendered — the title reads
  "Prayer times for Slough, United Kingdom, August 2026", clean. Minikin skips them.
- Blank is returned untouched, because `HomeScreen` and the exporter both do
  `cityName.ifBlank { generic }` and wrapping `""` in two invisible characters would make it
  non-blank and silently kill the fallback. That is a test case, not a hope.

This is groundwork for a translation that does not exist yet, and it is deliberately the
*only* groundwork done: no `values-ar/`, no machine translation, nothing that looks like a
step towards shipping a language without a native speaker. See CLAUDE.md.

### The Moonsighting slide, built and thrown away (2 Aug 2026)

**The clearest "measure it before you believe it" result in the project so far**, and the
finding is that a fix which looked obviously correct was a regression.

A12 says Moonsighting Committee slide Fajr and Isha down to 60° above 60° latitude, and that
the app does not. Both halves confirmed. Their page, verbatim:

> "Therefore, at latitudes more than 60degrees, we slide down to 60degrees and calculate
> **Fajr & Isha** using the rule of Sab'u Lail in summer."

*Fajr & Isha* — named explicitly, so the other four stay local. And the damage from not
doing it, measured with this engine on 21 June 2026 against the same longitude at 60°:

| Place | Fajr error | Isha error |
|---|---|---|
| Helsinki 60.2°N | −1 min | +2 min |
| Anchorage 61.2°N | −10 min | +10 min |
| Trondheim 63.4°N | −37 min | +37 min |
| Umeå 63.8°N | −44 min | +43 min |
| Reykjavík 64.2°N | −49 min | +49 min |
| **Luleå 65.6°N** | **−91 min** | **+91 min** |

Luleå's un-slid answer is degenerate rather than merely inaccurate: Isha 22:14, Fajr 22:52,
**38 minutes of night**. Every row has a real Muslim population. So the case for fixing it
was strong, the primary source was clear, and the implementation was fifteen lines.

**Then it was swept, and it broke.** 60.5°–66°N, every day of 2026, three longitudes:
**1,043 of 13,140 day-computations came out non-monotonic**. Luleå, 21 June, with the slide
applied:

```
Fajr 00:23   Sunrise 23:00   Dhuhr 10:38   Asr 15:30   Maghrib 22:09   Isha 20:43
```

**Isha 86 minutes before Maghrib.** Structural, not a slip: Isha moves ~1.5 hours south while
Maghrib stays at a very late local sunset, and they cross. Near the solstice at 63° the same
crossing appears as a *one-minute* inversion — which is the dangerous version, because it
would have shipped looking fine.

**Reverted.** Three reasons, in order of weight:

1. **A wrong-but-ordered day beats an incoherent one.** The current behaviour is up to 91
   minutes out at Luleå. The "fix" produced timetables where Isha precedes Maghrib, which no
   user could pray by and no mosque would recognise. That is a regression, not a correction.
2. **It could not be verified against the publishing body's own output.** Every real
   calculation bug in this project was caught by comparing to an independent reference, and
   three confidently-reported bugs turned out to be errors in the test rather than the code.
   moonsighting.com would not serve a timetable to `curl` — the JSON endpoint 500s and the
   HTML page builds its table in JavaScript — so there was nothing to check against.
3. **The prose is under-specified, and now demonstrably so.** A literal reading contradicts
   itself, which means Moonsighting are doing something the sentence does not say. Guessing
   what, inside prayer-time code, days before a production submission, is exactly the move
   this file exists to prevent.

**What would unblock it:** Moonsighting's own published timetable for one place between 60°
and 66° in June, showing how they order Maghrib and Isha on a day where a literal slide
crosses them. A browser session, an email to them, or a printed timetable from a mosque in
that band would each settle it. This is now blocked on *evidence*, not on effort — which is
a different and much more actionable state than "needs the same machinery as A10".

### Reading the preview, and the real bug it was hiding (2 Aug 2026)

**The owner reported the RTL build as broken for the second time** — "AM becomes MA, MP
becomes PM" — and said to stop explaining it and fix it. He was right to, and the right
answer turned out to be *both* things: the reversal was the tool, and underneath it there
was a genuine ordering bug that the tool had been hiding for as long as it existed.

**The tool.** `ar-XB` reverses Latin characters outright. `PM` → `MP`, `Sun` → `nuS`. No
real locale does that, so every screen looked mangled whether or not anything was wrong. The
build type now pins `ur` instead. Same screen, one line of build config apart:

| | `ar-XB` (before) | `ur` (now) |
|---|---|---|
| Meridiem | `MP 1:16` | `PM 1:16` |
| Gregorian date | `2 guA ,nuS` | `اتوار 2 اگست` |
| Hijri date | `Safar 1448 19` | `Safar 1448 19` ← unchanged, and that is the finding |

`ur` rather than `ar` on purpose: Urdu is right-to-left, keeps a Latin `AM`/`PM` in CLDR and
uses Latin digits, so the hardest case is still exercised while prayer times stay legible to
the person reviewing them. `ar` switches to Arabic-Indic digits and would have made every
time on the screen unreadable to this project's only reviewer — testing less while looking
more thorough.

**The bug the reversal was hiding.** Under `ar-XB` the Hijri date was mangled like everything
else, so it never stood out. Under `ur` it is the *only* thing still wrong, and it is wrong
in a way that survives into a real translation. The header rendered `Safar 1448 19`. Read
right-to-left, as the reader of an RTL paragraph does, that is **19, then 1448, then Safar** —
the year sitting where the month belongs. Measured with `java.text.Bidi` rather than argued,
and the measurement reproduced the header exactly:

| | visual | what an RTL reader takes in |
|---|---|---|
| Before | `Safar 1448 19` | `19 1448 Safar` ✗ |
| Month name isolated | `1448 Safar 19` | `19 Safar 1448` ✓ |
| **Whole date isolated** | `19 Safar 1448` | `1448 Safar 19` ✗ |

**The obvious fix is the wrong one, and that is the part worth remembering.** Wrapping the
whole date — which looks right, and produces a visual string that reads correctly to an
English eye — makes it a single foreign block that gets placed as a unit, and it comes out
backwards. Only the month name may be fenced, because it is the only strong left-to-right
run in the line and the two numbers must stay free to resolve around it. Both cases are now
permanent tests in `BidiTest`, including the wrong one, so nobody "simplifies" the fix into it.

Verified after: the `ur` build shows `1448 Safar 19`, which reads `19 Safar 1448`. The
English build shows `Sun 2 Aug · 19 Safar 1448` with no tofu boxes — the isolates are
zero-width and Minikin skips them, the same property already relied on in the PDF.

**What replaced `ar-XB`'s safety, and why it is stronger.** Its one real advantage was being
a reserved tag no phone can be set to. That is now covered three ways, all of which guard
more than it did: the build type carries `applicationIdSuffix ".rtl"`, `app_name` is
"SajdaTime (RTL)", and `NoTranslationsYetTest` fails the build if a `values-<lang>/` folder
appears in *any* module — which is the move CLAUDE.md actually forbids and which `ar-XB`
never prevented. Verified by adding `values-ar/` and watching the build go red, then removing
it and watching it go green. Also verified that the release bundle carries `en-GB` and
neither preview tag.

**The guard was broken when first written, and the way it failed is the interesting part.**
It reads the filesystem, which Gradle cannot see, so the test task was `UP-TO-DATE` and
*skipped* — the first run with `values-ar/` present reported zero failures. A guard that
reports success while not running is worse than no guard at all, and it would have been
skipped in precisely the situation it exists for: someone adds a translation folder and
changes nothing else. Fixed by declaring the locale folders as task inputs in
`app/build.gradle.kts`, and confirmed by re-running the same experiment with a plain
`./gradlew test` and no force flag.

**Not fixed, and correctly so:** `PM 1:16` and the stranded `?` in `?Does this match your
mosque`. Both are English strings inside an RTL paragraph, both read correctly right-to-left,
and both disappear when the words are translated. The `TimeFormat.clock` KDoc already warns
against "fixing" the meridiem with an isolate, and that warning stands — doing it would move
`PM` to the side it does not belong on.

### The daytime half of the alarm story, and the town-not-county fix (2 Aug 2026)

**The clearest single piece of evidence this project has produced.** The Xiaomi came back
with both builds still installed and both holding the *same nine prayer times*. One
`dumpsys alarm`, one instant, no reinstall, nothing rescheduled:

| Target | Build | `window` | `flags` | `exactAllowReason` | Fires no later than |
|---|---|---|---|---|---|
| 13:10 Dhuhr | shipped | `+1h0m0s0ms` | `0x20` | — | +1 hour |
| 13:10 Dhuhr | fixed | `0` | `0x3` | `permission` | exactly |
| 17:17 Asr | shipped | `+1h0m0s0ms` | `0x20` | — | +1 hour |
| 17:17 Asr | fixed | `0` | `0x3` | `permission` | exactly |

…and so on for all nine. Every fixed alarm exact; every shipped alarm an hour of slack.
The fixed entries also carry a `showIntent=…startActivity`, which is the `setAlarmClock`
signature — the shipped ones have none, because `setAndAllowWhileIdle` has nothing to show.

**`power_pending` had cleared to `--` on both** at the time of that reading, so this looked
like the plain one-hour window Android applies to any inexact alarm, present in ordinary
daytime use with the phone awake and charging.

> **⚠️ That inference was wrong, and the same day's 13:10 Dhuhr disproved it.** The
> observation was accurate; what was concluded from it was not. `power_pending` is not a
> state that clears once on waking and stays clear — it **re-engaged during the afternoon**,
> on a phone that was plugged in, unlocked and in active use, and it re-engaged *before the
> alarm was due*. Read at 13:13:06, three minutes after Dhuhr fell:
>
> ```
> requester=-3m6s606ms  …  power_pending=+2d23h56m53s394ms
> whenElapsed=+2d23h56m53s394ms   maxWhenElapsed=+2d23h56m53s394ms
> ```
>
> Every other policy sits in the past, so nothing else is holding it. `power_pending` alone
> overwrote **both** bounds and pushed today's Dhuhr out by nearly three days.
>
> **And it never fired then either, which is the part that took watching to the end.** At
> 13:22:39 the app ran `DailyRescheduleWorker` — one of the five reschedule triggers that
> exist to make alerts *more* reliable — rewrote its alarm set, and **cancelled the parked
> alarm in the rewrite**. Afterwards: no 13:10 alarm remains, and the only notification the
> live build posted all day is the silent ongoing badge. **No `prayer_times` alert at any
> point.** The fixed build posted "Time for Dhuhr" at **13:10:00.600**.
>
> The failure is therefore **silent loss, not deferral**, and neither half causes it alone:
> OEM parking would have given a very late alert, and the app's own reliability worker then
> tidied the stale alarm away without ever delivering it. A snapshot at 13:13 reads "deferred
> three days"; a snapshot at 13:25 reads "nothing scheduled, all normal". Neither says "the
> user missed Dhuhr" — only watching across both does.
>
> Corrected picture: **the one-hour window is the floor, not the expected damage**, and the
> three-day parking is not an overnight-only phenomenon. There is no time of day at which the
> shipped build can be relied upon. Evidence:
> `docs/reviews/2026-08-02-dhuhr-daytime-ab.md`.

**A caught error of my own.** The first dump showed the shipped build's alarms still parked
three days out; the second, seconds later, showed them released. Same alarm object. The
deferral clears when the phone becomes active, exactly as the overnight run predicted — but
had I read only the first dump I would have reported a permanent three-day park, and had I
read only the second I would have missed the parking entirely.

**Another prayer app on the same phone answers the question the fix could not.** Muslim Pro
declares `USE_EXACT_ALARM`, has `SCHEDULE_EXACT_ALARM: granted=false`, and its alarms read
`window=0 exactAllowReason=policy_permission flags=0x5` — exact, with **no permission the
user can decline**, because that permission is auto-granted at install and cannot be
revoked. It is the whole problem sidestepped rather than mitigated. It is deliberately
**not** adopted here; see §11 and lesson 68.

**T3 — the town, not the county — is fixed, and it is the fix this document already
specified.** The precision section above ends by naming the cause exactly: `subLocality`
and `featureName` "are not in the chain at all, and either often survives when `locality`
does not". `data/PlaceName.kt` is now the single chain used by *both* halves of the feature:

```
locality → subLocality → featureName (only if it contains a letter) → subAdminArea → adminArea
```

Three things worth knowing about it:

- **The two halves used to disagree with each other.** A searched city read "Slough, United
  Kingdom"; the same place found automatically read "Slough". Same screen, two formats. They
  share one function now and cannot drift apart again.
- **`featureName` is guarded.** It is only *usually* a place name; on a precise fix it is as
  likely to be a house number, and "37, United Kingdom" is worse than the county the whole
  change exists to remove. Anything without a letter in it is rejected.
- **Eight JVM tests, on plain strings rather than `android.location.Address`,** because that
  class is a stub outside an emulator and every assertion would otherwise have needed
  Robolectric to say anything at all. The test named for the bug reproduces the exact shape
  a coarse fix returns.

**The precision explainer is measured, not asserted.** `location_sheet_body` now tells the
user why the name may be approximate and that it barely moves the times. The figure was
computed with the app's own `PrayerEngine` over ±10 miles at UK latitude, on today,
midsummer and midwinter: **every prayer moves by at most two minutes**, longitude
dominating as expected. That agrees with the independent Aladhan figures in the precision
section above (≈5 seconds per kilometre), so two methods now say the same thing. It is
shown only when the user opens the location sheet — the app never volunteers it, and it is
not a second disclaimer. The sheet already has `.verticalScroll`, so the longer body cannot
clip or push the search field off-screen.

**The onboarding and settings copy has been strengthened, and the old reasoning retired.**
Both strings said only "may hold prayer alerts back and deliver them late", with a comment
explaining that no number was given because Android documents no upper bound. That was
right while we were *reasoning* and wrong once we had *measured*. Denial now reproduces as
a one-hour window on two makes and, overnight on HyperOS, as an alarm that never arrives.
"Late" undersold that badly enough to talk a user out of a permission that silently costs
them Fajr, so both strings now name the measured floor and the overnight case, and name
Fajr specifically because it is the prayer that fails.

**What this did *not* establish.** The 13:10 firing itself had not yet been observed when
this was written — the table is the *scheduling* state, which is what the fix changes. The
RTL preview could not be installed on the Xiaomi at all (`INSTALL_FAILED_USER_RESTRICTED`,
the HyperOS "Install via USB" gate), so RTL on hardware remains a Samsung-only result. And
the new location sheet was not screenshotted on a device: the phone emulator wedged
mid-walk, having already ANR'd `system_server` the day before. Its scroll behaviour was
confirmed by reading the composable instead, which covers the actual risk but is not the
same as seeing it.

---

### "Match your mosque": corrections, the Hijri shift, and what they actually settle (2 Aug 2026)

**The owner's question was the right one, and sharper than the feature it produced:** if
mosques disagree because of a ruling — moon sighting, or a high-latitude convention — should
the user be able to select it rather than have the app choose?

The answer separates into four gaps that were being treated as one. Two the app already
covered, two it did not:

| What the user sees | Cause | Before |
|---|---|---|
| Fajr/Isha off 10–40 min | twilight angle, i.e. the calculation method | covered — method picker, help text, latitude banner |
| Asr off 45–90 min | Hanafi vs the other three | covered — madhab picker |
| Every prayer off 2–10 min | mosque precaution minutes, or a printed local table no formula reproduces | **nothing** |
| Ramadan/Eid off a day | local sighting vs calculation | **nothing** — the date is computed |

**The naming trap, recorded because it will catch the next reader too.** "Moonsighting
Committee" in the method picker is their *twilight rule* for Fajr and Isha. It is not their
moon sighting announcements for Ramadan and Eid. Same organisation, same word, unrelated
mechanisms. A user whose Eid is a day out will pick that method expecting it to fix the date,
and it will not.

**What was built.** Per-prayer corrections of ±30 minutes, and a Hijri shift of ±2 days, in
one dialog called "Match your mosque" — one dialog because from the user's side it answers one
question, and splitting it would make someone decide whether their disagreement was about
minutes or days before they had any way to know.

Three decisions worth not re-litigating:

- **Offsets are applied to the finished times, not through adhan's own
  `CalculationParameters.adjustments`.** See §15 lesson 76. Three of six outputs bypass that
  field, so half the prayers would have silently ignored the correction.
- **The engine holds the day in order after applying them.** See §15 lesson 75. The ±30 bound
  does not prevent an inversion: at 59.9°N on 1 January, Dhuhr and Asr sit twenty-six minutes
  apart.
- **±30 and not wider.** A larger gap is a method mismatch wearing an offset's clothes. The
  Slough case in this section's siblings is the proof: MWL landed 78 minutes from every local
  mosque, and the correct fix there was the method, not a nudge. The dialog's own copy says
  to try the method first.

**Both settings sync to the watch** (`WatchSyncContract.KEY_ADJUSTMENTS`, `KEY_HIJRI_OFFSET`),
because a user who corrects Maghrib on the phone and then reads a different Maghrib on their
wrist has been given two answers to a religious question by one app. `AdjustmentCodec` lives
in `core` for the same reason — one parser, two callers.

**What this settles that calculation could not.** A12 (Moonsighting above 60°) remains
unresolved as a *calculation* question, and §15 lesson 78 records the guard clause that
earlier reading missed. But the user-facing problem it represents is now addressable: a
Luleå user whose mosque publishes different times can match them exactly, without the app
having to adjudicate between scholarly bodies or guess at undocumented edge behaviour. That
is a better answer than a confident wrong one, and it is the same answer for every other
cause of divergence.

**Verified on the emulator, LTR and RTL.** Fajr 2:55 → 3:00 with +5; Dhuhr 1:16 → 1:13 with
-3; the other four unmoved; the Hijri date 19 → 20 Safar with +1; the summary row reading
"2 prayers adjusted, Date +1 day"; the corrections carried through into the exported PDF
(3:00 and 1:13 on the 2 August row). RTL mirrors correctly — label right, stepper mirrored,
per-prayer accessibility labels present on every button ("Increase Fajr", "Decrease Asr").

### Cold start on real hardware, and risk 6 closed (2 Aug 2026)

The only figure the audit had was **13.3 s**, from a debug build on a struggling emulator, and
it was correctly labelled as not evidence. Measured on the Redmi Note 13 Pro, `am start -W`,
force-stopped between runs, `LaunchState: COLD` on every one:

```
com.sajdatime.app (Play build, versionCode 2)   631 ms, 244 ms, 224 ms
com.sajdatime.app.sideload (current main)      1018 ms, 846 ms, 775 ms
```

So the shipped app cold-starts in about **a quarter of a second** on real hardware, and the
first run's 631 ms is the usual page-cache warm-up. The emulator figure was off by a factor
of roughly fifty. The sideload build is slower because it is a different variant with a lower
`minSdk` and without the release build's optimisation; it is not the comparison that matters
and is recorded only so the numbers are not mistaken for each other later.

Risk 6 in `PRODUCTION_READINESS.md` is closed. The emulator ANR remains what it was — seen
once, never on hardware, profile consistent with un-JITted cold-start drawing under
uiautomator load.

### The two surfaces that never marked an approximated time (2 Aug 2026)

The home screen and the PDF have carried the "these times are approximate" marking for a
while. The notification and the Wear tile did not — which meant the only two surfaces
presenting a projected time without qualification were **the two that reach the user when the
app is closed**, which above the polar circles is most of the day.

Both now mark it. The phone uses `setSubText`, which Android draws small and grey in the
notification header beside the app name: the alert's own words stay "Time for Fajr", because
a notification is read in one glance on a lock screen and burying the prayer behind a caveat
helps nobody. The tile uses a separate string, "About 05:12 · in 2h" rather than a tilde, so
a screen reader says the word instead of skipping the symbol.

Whether a projected time should fire an alarm at all was **A10**, and it has since been
decided and built — quiet by default, with an override, offered only to the users it can
apply to. The rule is §5.14 and the evidence is below.

### A projected time no longer rings, and the switch that keeps that honest (2 Aug 2026)

Marking a time as approximate and then shouting it at 2 am are not consistent with each
other, and that inconsistency is what A10 was really about. The decision taken: an Alarm on a
projected day posts on the quiet channel — still on time, still in the shade, still marked —
and `alarmOnApproximateDays` turns ringing back on for anyone who wants it.

**The switch was not in the original recommendation, and reading the code is what added it.**
`approximatedFrom` is a whole-day property, and above the polar circles *every* day in the
polar season is projected. At Tromsø that is late May to late July. So a downgrade with no
override would not be a small caveat on a rare day — it would silently disable a user's Fajr
alarm for two months a year, discoverable only by not being woken by it. Recorded because the
recommendation looked complete before that was checked, and was not.

**Where the switch is offered, and the measurement that fixed the first attempt.**
`hasApproximateDays` sweeps 366 days through the engine rather than testing latitude against
a threshold. Both alternatives were tried on paper and dropped: a threshold has to be
method-dependent (45 under the Islamic Fiqh Council, 60 under Moonsighting), and a
two-solstice probe assumes the worst day of the year is a solstice — *nearly* true, and not
worth asserting when the exact answer costs a few milliseconds behind a `remember`.

The boundary was then measured, and one case in the test was written into the wrong bucket:

| Place | Latitude | Offered the switch? |
|---|---|---|
| Makkah, Karachi, Jakarta, London, Cape Town | 21°N to 51°N, 6°S to 34°S | No |
| **Ushuaia** | **54.8°S** | **No** — see below |
| Tromsø | 69.7°N | Yes |
| Longyearbyen | 78.2°N | Yes |
| McMurdo Station | 77.9°S | Yes |

**Ushuaia is the useful row.** It is the southernmost city on earth and reads as an extreme
case, so it was written on the "shown" side — and the test failed. At 54.8°S it is *below*
the Antarctic Circle, the sun rises and sets there every day of the year, and it needs this
switch no more than London does. It is kept in the test on the hidden side precisely because
it is the case that looks like it belongs on the other one. The southern polar case is now
McMurdo, which is genuinely inside the circle.

Pinned by `AlertStyleDowngradeTest`: the two downgrade reasons, the override, the interaction
between them, all sixteen flag combinations proving a Notification is never *upgraded* to an
Alarm, that the ringer is not read when the cheaper check already settles it, and that the
sweep finds the polar season starting from any month of the year.

**Verified on the emulator at a real polar location, all five paths.** The app was cleared and
walked from first run with the city typed as *Longyearbyen* (78.22°N), on 2 August 2026 — a
date still inside Svalbard's polar day, so every time on the screen is projected:

| Path | Result |
|---|---|
| Longyearbyen, no alarm set | Polar notice shows its base text only. The alarm sentence is correctly withheld from a user it does not describe |
| Longyearbyen, Fajr set to Alarm | **"Ring on approximate days" appears** in the alerts dialog, below "Stay quiet when your phone is silent", switched **off** |
| Same, back on home | Polar notice **gains** the second paragraph: *"On these days your alarms arrive as quiet notifications instead of ringing…"* |
| Switch turned **on** | The alarm paragraph **disappears** from the notice, base text intact. The notice describes the behaviour in force, not the behaviour in general |
| Location changed to Slough (51.5°N), Fajr still Alarm | **The row is gone.** "Stay quiet when your phone is silent" is still there, which is what proves the disappearance is the latitude test and not the whole block collapsing |

The disclaimer was read on screen in the same walk: the new "Match your mosque" sentence is
in the mosque-disagreement paragraph where it belongs, and the dua request is still the last
thing on the page. The About screen reads **1.1.1**.

**Checked right-to-left, because it is a layout change** (CLAUDE.md hard rule). The whole
walk was repeated in the `rtl` build, again at Longyearbyen. The new switch mirrors like its
neighbour — control on the left, label and body right-aligned, chips reversed to
Alarm | Notification | Off — and wraps to six lines without clipping. The polar notice card
now carries two paragraphs and still mirrors correctly, warning icon on the right, scrolling
rather than overflowing. The trailing-full-stop artefacts (`.follows`, `.turn this on`) are
the documented `ar-XB` behaviour with English words, not a defect.

**A note for whoever runs this next.** `./gradlew installRtl` fails outright while the Xiaomi
and the watch emulator are attached: it installs to *every* connected device, so it collects
`INSTALL_FAILED_USER_RESTRICTED` from HyperOS and `INSTALL_FAILED_UPDATE_INCOMPATIBLE` from
the watch, and reports a red build for a variant that built perfectly. Install the APK it
already produced directly instead:

```bash
adb -s emulator-5554 install -r app/build/outputs/apk/rtl/app-rtl.apk
```

**Not verified.** The alarm has not been watched *firing* quietly on a projected day — that
needs an overnight run at a polar location and was not done. None of this has run on the
Xiaomi, which still refuses the sideload.

## 11. ⚠️ Still pending — the honest list

### Blocker for release

> **Read this first — 1 Aug 2026.** **Google approved it, and the app is live on the closed
> testing track.** The review came back inside a day. `Closed testing - Alpha` is **Active**
> with `1.1.0 - first closed test`, *"Available to selected testers"*, released 31 Jul 22:17
> across all 177 countries.
>
> **Updated later the same day: the twelve-tester bar is cleared, and the clock is running.**
> The Dashboard's production-access checklist now reads:
>
> | Criterion | State |
> |---|---|
> | Publish a closed testing release | ✓ ticked, struck through |
> | **Have at least 12 testers opted-in to your closed test** | **✓ ticked, struck through** |
> | Run your closed test with at least 12 testers, for at least 14 days | ○ open — in progress |
>
> **Apply for production** is still greyed out. The email list holds **24 addresses**; twelve
> or more of them have opted in. Do not read 24 as the opt-in figure — it is the *addresses
> added* number and is reliably the larger one (§15 lesson 36).
>
> **The italic count line has disappeared.** While below twelve the Dashboard printed
> *"N testers currently opted in"*; on passing twelve it replaces that line with a tick and
> stops reporting the number at all. So there is now **no way to see whether the count is
> holding at twelve or has slipped below it**, and no notification if it does. That is worth
> knowing before assuming the fortnight is running cleanly.
>
> Earlier text, kept because the rule it states is still true: Google requires twelve, held
> for fourteen *consecutive* days, and **the fourteen days are counted while twelve or more
> are opted in** — so days spent below twelve are not early days of the fortnight, they are
> days that do not count.
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

> **Superseded, 1 Aug 2026 (later the same day).** This section used to open with *"do not
> act on these until the closed test is over"*. That advice conflated two different things
> and is withdrawn: **writing and committing a fix costs the closed test nothing — only
> uploading a build reaches Google at all.** Everything below marked DONE was built, verified
> on a device and committed while the test was running; none of it has been uploaded. The
> upload decision still belongs at the end of the fortnight, and the surrounding evidence is
> in §10 and in `docs/AFTER_THE_TEST.md`. Google's own closed-testing page says *"Continue to
> use closed testing while you fix user-reported issues and bugs"*, and is silent on whether
> a new release disturbs the fourteen days — silence is not permission, which is why the
> upload waits and the code does not.

- **T1 — Isha does not match UK mosques, and the fix already exists but is invisible.**
  Full evidence in §10, "Isha in the UK — measured against three real mosques". The default
  `AUTO` → Muslim World League puts Isha **78 minutes later** than all three of the tester's
  local mosques, which use Moonsighting Committee. `CalcMethod.MOON_SIGHTING` already ships
  and already fixes it, but **onboarding never asks for a calculation method** — it asks for
  sect and madhab only (`Step.WELCOME, PERMISSION, SECT, MADHAB, CONFIRM`), so a user who
  never opens Settings can never reach it.

  **Scope, narrowed by measurement — read this before designing anything.** The worldwide
  comparison in §10 shows **MWL is a good default for most of the Ummah** (within about five
  minutes across South Asia, South-East Asia, the Middle East and Africa) and only fails
  above roughly **45° of latitude**, where it reaches +26 minutes in March and +49 in June.
  So this is **not** "the default is wrong". It is "the default has a latitude band where it
  stops matching local practice". Do not widen it into a rewrite.

  **Two things that follow, and both remove work:**

  - **No new calculation methods are needed.** `KEMENAG` (Indonesia — the largest Muslim
    population on earth) and `JAKIM` (Malaysia) are absent from `CalcMethod`, and MWL is
    within four to nine minutes of both. Adding them would be motion, not progress.
  - **No per-prayer offset feature is needed either** — which reverses what this section
    said before the app's own engine was checked. On `MOON_SIGHTING` the engine reproduces
    JMIC Slough to within a minute on all six times, matching Dhuhr and Maghrib *exactly*,
    because adhan applies the method's own +5/+3 adjustments. The "*ihtiyat* the mosques add"
    turned out to be the method, not the mosque. Selecting the method is sufficient.

  Options, none yet chosen — **this is the owner's call, because it is a religious question
  and not an engineering one**:

  | Option | Effect | Cost |
  |---|---|---|
  | Leave the default; explain it in Settings | Keeps a mainstream convention that is right for most of the world | None, but northern users stay mismatched |
  | **One-time note when latitude > ~45°**, offering the method picker | Reaches exactly the affected users, changes no time silently, adds nothing for the ~80% who are already correct | Small: one conditional card |
  | Add a method step to onboarding for everyone | Makes the existing fix reachable | Small, but lengthens a flow already too long, for users who do not need it |
  | Auto-switch method by latitude | Fixes it for everyone silently | **Rejected** — the app would adopt a fiqh-adjacent position for the user, invisibly |
  | Per-prayer ± minute offset | Absorbs any remaining variation | **No longer justified** — measurement showed nothing left for it to absorb |

  The latitude-triggered note is now the strongest candidate. The objection to the rejected
  option was never that a smart default is wrong — it is that a **silent** one is. A visible
  prompt is not the same thing.

  **DONE, 2 Aug 2026 — the latitude-triggered note, built exactly as scoped.**
  `HomeScreen.MethodBanner`, using the existing `NoticeCard` and the same dismiss contract as
  the exact-alarm banner. It appears only when **all three** hold:

  | Condition | Why |
  |---|---|
  | `abs(latitude) ≥ 45` | Below it the default is within minutes of local practice nearly everywhere; showing this to everyone hands ~80% of users a question they have no reason to answer. `abs`, because the Southern Hemisphere is not an afterthought |
  | `method == AUTO` | A user who has already chosen — even if they chose MWL deliberately — has answered this. Asking again second-guesses them |
  | not dismissed | Persisted as `method_notice_dismissed`, home only; Settings keeps the row |

  **It names no method and changes no time.** The wording asks *"Does this match your
  mosque?"* and offers the picker. Auto-switching by latitude stays rejected for the reason
  above. This is also consistent with the disclaimer, which already tells users to ask their
  mosque and pick the method in Settings — the banner is the actionable version of a
  sentence that was previously buried in a wall of text shown once.

  Verified on the emulator, all five paths: shown at 51°N on `AUTO`; **absent at Makkah**
  (21°N); absent after choosing any method (and Dhuhr moved 1:16 → 1:20 pm, so the choice
  took effect); tap switches to the Settings tab; Dismiss hides it and survives a full app
  restart, while the exact-alarm banner beside it still shows — which is what proves the
  dismiss is specific rather than hiding everything.

  **Checked right-to-left, dark, and at 1.5× text, because it is a layout change** (CLAUDE.md
  hard rule). All three at once in the `rtl` build: the card mirrors correctly — dismiss on
  the left, warning icon on the right — text is right-aligned, it wraps rather than clipping,
  and the page scrolls when the card grows past the viewport. The `?Does this match your
  mosque` and `.follows` artefacts are the documented `ar-XB` behaviour with English words,
  not a defect (see "Locale — what running the app in another language found").

  **The RTL run also gave the bidi isolation its first genuinely mixed-script test.** Under
  `ar-XB` the geocoder returns the country in Arabic, so the header read
  **"Greater Manchester, المملكة المتحدة"** — Latin town, Arabic country, in one line. The
  city stayed a single unbroken run with the comma correctly between the two halves, which
  is exactly the case `core/Bidi.kt` exists for and which the English build can never
  exercise. Yesterday's work was committed on the strength of a JVM test and a rendered PDF;
  this is the first time it has been *seen* doing its job.

  **A false alarm worth recording.** Mid-check the banner appeared to have vanished from the
  RTL build, and the persisted state said it should be showing. It was simply scrolled off
  the top: uiautomator only reports nodes inside the visible bounds, and each `txt` helper
  call re-dumps, so the scroll position had drifted between calls. Confirmed present at
  bounds `[220,1038][896,1198]` from a single dump taken after scrolling to the top. The
  tooling was wrong, not the app — the same shape of error as the "missing" alarm sound
  picker and the "clipped" landscape Qibla screen (§15 lesson 51).

  **A regression in the T3 fix was found by this same run, and it is the more useful
  finding.** `PlaceName` had `featureName` third in the chain, ahead of the county, on the
  reasoning that it is "more specific". The emulator's geocoder answered a UK fix with a
  POI and the home screen read **"Townhouse Hotel, United Kingdom"** — worse than the county
  the change existed to remove, and on an app whose new location copy promises to ask only
  for an *approximate* position, naming a building is its own contradiction. `featureName`
  is now last, consulted only when every administrative field is null. Nine tests, one named
  for the regression. It compiled, it read correctly, it passed eight tests, and it was
  wrong; only running it said so.

- **T2 — DONE, 1 Aug 2026.** Every step that asks a question now ends in the same pair of
  buttons, Back and Continue, through one shared `StepButtons` composable. Selecting a card
  only selects it. Skip was deleted rather than relabelled, because one of the four madhabs is
  always already selected, so Skip and Continue did the same thing while looking like a choice
  between keeping and discarding an answer. The disclaimer's "I understand" became a filled,
  full-width, 56dp button instead of a small tinted word in the corner. Verified by screenshot
  on device in both LTR and RTL. Original analysis kept below.

- **T2 (original) — the first-run flow advances on tap in some steps and needs a button in others.**
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

  **Point 4 is DONE, 1 Aug 2026**, as a side effect of fixing T4: the typed-city entry is now
  shown on the location step from the start, rather than only after a location attempt has
  failed.

  **Points 1 to 3 are DONE, 2 Aug 2026.** All three landed together in `data/PlaceName.kt`,
  because points 1 and 2 are the same function and doing them separately would have left the
  two halves of the feature disagreeing for a commit. Evidence in §10, "The daytime half of
  the alarm story, and the town-not-county fix". Three notes for whoever reads this next:

  - The chain is `locality → subLocality → featureName → subAdminArea → adminArea`, and
    **`featureName` is guarded** — it is rejected unless it contains a letter, because on a
    precise fix it is often a house number and "37, United Kingdom" is worse than the county.
  - **`CityLookup` shares the same function now.** It had its own copy of the old chain, so
    the searched and the automatic name formatted differently on the same screen. That was
    not in the tester's report and was found only by fixing both at once.
  - The point-3 wording was **measured before it was written**, with the app's own engine:
    ±10 miles at UK latitude moves every prayer by at most two minutes, in midsummer,
    midwinter and today. It lives in `location_sheet_body`, shown only when the user opens
    the location sheet — never volunteered, and not a second disclaimer.

  What was *not* done: the new sheet has not been seen on a real device. The phone emulator
  wedged mid-walk (it had ANR'd `system_server` the day before) and the RTL preview would
  not install on the Xiaomi at all. The overflow risk was closed by reading the composable —
  the sheet already has `.verticalScroll` — which covers the real hazard but is not a
  screenshot. Take one on the next device session.

### The second round of tester feedback (1 Aug 2026) — all four DONE

Reproduced on a device first, fixed, and verified on a device again. Evidence in §10, "The
second round of tester feedback, and a first-run trap underneath it". Rules in §5.12, §5.13
and §6. Platform sourcing in `docs/reviews/2026-08-01-android-alarm-facts.md`.

- **T4 — DONE.** *"User typing city name but its not selecting it."* The search worked; the
  sheet gave no sign of it. Now closes on success, opens fully expanded so "Find city" is
  visible without a drag, says "Looking up that place…" beside the field it belongs to, and
  responds to the keyboard's own search key. **Reproducing it also found a first-run dead
  end that no tester reported** and that is live in the current closed test — see §10.
- **T5 — DONE.** *"Give notification as soon as its time for namaaz (avoid delays)."* Two
  causes, both measured. Every prayer alert now uses `setAlarmClock`, and onboarding asks
  for "Alarms & reminders" instead of leaving it to a banner. Without that permission the
  measured delivery window was **one hour**.
- **T6 — DONE.** *"If phone is silent then alarm shouldn't ring but Notification still pops
  up on time."* `alarmRespectsSilent`, on by default, verified in all four combinations.
- **T7 — DONE.** *"Alarm setting to be configured by the user"* and *"notification badge and
  alarm could be done together"*. Off / Notification / Alarm, per prayer, in one chooser that
  replaced two.

**Left undone on purpose:** App Standby buckets. At targetSdk 36 `SCHEDULE_EXACT_ALARM` no
longer floors the app at WORKING_SET, and a Restricted-bucket app is documented as getting
*one alarm per day*. The only documented fix is `USE_EXACT_ALARM`, which Play gates to
alarm-clock and calendar apps; claiming that category for a prayer app is arguable, and being
wrong blocks the release at review. Checked, rejected, and recorded rather than attempted —
revisit only with a specific reason, and write the outcome into the reviews file.

### A14 — `USE_EXACT_ALARM` would delete the permission problem, and is deliberately not taken (2 Aug 2026)

**Owner's call, and the recommendation is to wait.** Muslim Pro, on the owner's own Xiaomi,
gets exact prayer alarms with **no permission prompt at all**. Verified from the device:

```
android.permission.SCHEDULE_EXACT_ALARM: granted=false
android.permission.USE_EXACT_ALARM:      granted=true
  → window=0 exactAllowReason=policy_permission flags=0x5
```

`USE_EXACT_ALARM` has protection level `normal`: granted at install, invisible to the user,
and not revocable without uninstalling. SajdaTime declares only `SCHEDULE_EXACT_ALARM`,
which since Android 14 is denied at install and must be found and granted by the user. So
the root cause of T5 is not merely mitigable — it is **avoidable**. Everything the fix does
(the onboarding prompt, the dismissible banner, the settings row, the whole ladder in §6)
exists to work around a permission this app might simply not need.

**Why it is not being taken now.** Google's policy (Play Console Help, *Permissions and APIs
that Access Sensitive Information* → *Exact Alarm Permission*) admits exactly two use cases:

> The app is an alarm or timer app.
> The app is a calendar app that shows event notifications.

and states that apps requesting it *"are subject to review, and those that do not meet the
acceptable use case criteria will be disallowed from publishing on Google Play."* A Play
Console declaration is required. Requires `targetSdk` 33+ — we are on 36, so that is not the
obstacle.

Genuinely arguable both ways, which is the point:

| For | Against |
|---|---|
| The app has a real Alarm mode: alarm-stream audio, own sound, per prayer, used as a Fajr wake-up | The store listing describes a prayer times and Qibla app, which a reviewer may not read as "alarm app" |
| Muslim Pro ships it in this exact category and is live | *Another app passing review is evidence it can pass, not proof that we would* |
| Removes the single largest cause of late alerts for every user who declines | The penalty is "disallowed from publishing", not a warning |

**Recommendation: ship to production on `SCHEDULE_EXACT_ALARM`, then revisit.** The fix is
committed, measured exact on two makes, and the onboarding prompt has been walked end to
end. For a first-time personal developer mid-closed-test, a wrong call here costs the
launch; the thing it would buy is worth an hour. Once the app is live and stable, a
rejection costs a version instead, and the declaration can be made properly. Do not let a
future session read this as an oversight and "fix" it quietly — see §15 lesson 68.

### Open decisions from the worldwide review (1 Aug 2026) — owner's call, not an agent's

These come out of the global sweep and the two second opinions. Each is a *product* decision
about how much the app should say on the user's behalf, and none should be taken unilaterally.
The measurements behind them are in §10 and are not in doubt; what to do about them is.

- **A1 — Decouple sect from calculation method.** Both reviewers called this the app's clearest
  breach of its own principle, and they are right on the logic: `resolveMethod` maps
  Sunni → MWL, but MWL is one organisation's 18°/17° convention, not a Sunni position. Turkey,
  Egypt, Indonesia and Malaysia are all overwhelmingly Sunni and none of them use it. The
  madhab question is different and must stay — it genuinely sets the Asr shadow ratio.
  *Against changing it:* something has to be the default, every option is somebody's
  convention, and an extra onboarding question costs every user to help some. *Middle option:*
  keep MWL as the default but stop deriving it from sect, and say on screen which method is
  in use. Not started.

  **DEFERRED, 2 Aug 2026, and deliberately so.** The logic is still right and nothing here is
  withdrawn. What changed is that the user-facing symptom it was raised to fix — a northern
  user stuck on a method their mosque does not use — is now addressed by the latitude-
  triggered `MethodBanner` (A2, shipped) and by "Match your mosque" (§5.14 sits beside it).
  What remains is an internal tidiness argument, and it costs every user an extra onboarding
  question to settle. Revisit if a tester reports being confused by the derivation itself,
  which no tester has.

- **A2 — A one-time notice above 45° absolute latitude.** Measured, and 45 is the right number:
  the worst-case spread between mainstream methods crosses an hour there, and −45° mirrors +45°
  exactly, so the trigger must use `abs(latitude)`. A notice informs without choosing, which
  is the distinction that makes it acceptable where auto-switching is not. Still needs a
  decision on whether it appears at all. Not started.

- **A3 — Show the active method on the home screen.** Cheapest of the lot and suggested by
  both reviewers: a small "Calculation: MWL" line, tappable through to the picker. It makes the
  choice visible to users who never open Settings and never read the disclaimer, which is most
  of them. Not started.

  **DEFERRED, 2 Aug 2026 — largely superseded.** The `MethodBanner` already puts the question
  in front of the users the method actually matters to, and does it with a sentence rather
  than an abbreviation. A permanent "Calculation: MWL" line would add jargon to the home
  screen of every user worldwide to serve the same case less well. If it comes back, it
  should come back as a full method *name* on the Times screen, not a code.

- **A4 — Whether `HighLatitudeRule` should be a user setting.** It is inert for Moonsighting
  (§10) but decisive for the plain angle methods, where the three options are three different
  religious approximations. Wifaqul Ulama's own published deliberation declined to name a
  single agreed rule for Britain, so there is genuine plurality here and the app currently
  picks one silently. *Against:* it is the most jargon-heavy setting imaginable and would
  bewilder the majority to serve a minority. Not started.

  **DEFERRED, 2 Aug 2026.** Unchanged on the merits — this is a real plurality the app resolves
  silently. But it is the single most jargon-heavy setting the app could ship, and "Match your
  mosque" now lets an affected user reach the same *outcome* by matching a printed timetable
  without having to hold an opinion about the rule. Absorbing a difference is not the same as
  understanding it; it is, however, what the user actually wanted.

- **A5 — adhan's 55° Moonsighting clamp is not hemisphere-symmetric.** Confirmed in bytecode
  and on the numbers: at +56° on 21 June Fajr sits 55 minutes before sunrise; at −56° on
  21 December it sits two hours before. Compensating is a handful of lines. *Against:* almost
  nobody lives below 55°S — Ushuaia at 54.8° is already above the line — so this is a
  correctness itch more than a user problem. Deliberately left, and written down so the next
  session does not rediscover it as a mystery.

- **A6 — Whether to add Morocco (19°/17°) and a proper Wifaqul Ulama profile.** Morocco is
  ~37 million Muslims and 4–7 minutes off MWL on Fajr; Wifaqul is a genuinely distinct UK
  authority (18° Fajr, 15° Isha above 48°, plus hardship handling) that one reviewer measured
  as differing from London Unified by up to 98 minutes on Fajr in midsummer. **That Wifaqul
  comparison is the reviewer's, not ours, and has not been independently checked** — do not
  quote it as verified. Likewise the claim that London Unified is *not* a fixed 12°/12°
  timetable but Khalid Shaukat's seasonal model: plausible, unverified here, and it needs the
  authority's own document before anything is built on it.

  **DEFERRED, 2 Aug 2026, on the evidence rather than the priority.** Morocco is a genuine gap
  and a large one. Wifaqul is not addable at all until someone reads their own published
  document — the 98-minute figure is a reviewer's, unchecked here, and the claim that London
  Unified is a seasonal model rather than a fixed 12°/12° timetable is likewise unverified.
  Adding a method profile from a second-hand number is exactly the mistake this project has
  spent two days not making. Morocco can go in whenever someone wants it; Wifaqul cannot.

- **A7 — The Moonsighting option must not be sold as "the UK method".** It is what the two
  Slough mosques and London Unified use, and that is all we have measured. Two mosques in one
  county are not a country. If the label ever gains a subtitle it should name who uses it, not
  where.

- **A8 — The watch does not show the polar approximation notice.** The phone does. The engine
  fix is shared so the watch no longer crashes, but a Wear user above the Arctic Circle sees
  projected times with nothing saying so. Small screen, no obvious room; needs a design
  decision rather than a quick insertion.

- **A9 — ✅ CLOSED (1 Aug 2026). The reference latitude now follows the method: 45° for
  everything, 60° for Moonsighting.** Both figures verified against the publishing body's own
  words, and the Fiqh Council's band edges independently confirmed against the sky — full
  evidence in §10, "A9 settled, and a second polar bug found underneath it", and the rule in
  §5.5a. The reviewer was right that 60° was Moonsighting's rule wrongly generalised; they
  were wrong that it should simply become 45° for everyone, because Moonsighting's own users
  should keep Moonsighting's own answer.

  Explicitly rejected, and recorded in `PrayerEngine` so it is not tried again: one constant
  for everybody; clamping to the highest latitude that still computes; and using Makkah's
  times.

- **A10 — Per-prayer provenance, and whether projected times should fire alarms.** The
  strongest remaining item, and the evidence for it got much stronger while closing A9.

  Measured: the projection moves **Dhuhr by exactly zero** (solar transit does not depend on
  latitude), Fajr/Asr/Isha by up to ~40 minutes, and sunrise/Maghrib by up to 95. Asr is
  genuinely computable in polar summer and this is now arithmetic rather than assertion: at
  Tromsø on 21 June the noon altitude is 43.79°, so the noon shadow is 1.042 object-lengths
  and Shāfi'ī Asr needs 2.042, i.e. an altitude of 26.1° — which the sun passes every
  afternoon, because it ranges from 3.09° at midnight to 43.79° at noon. adhan discards it
  only because a missing sunrise fails its whole constructor.

  So on a polar-day date the honest answer is a **mixed** day: Dhuhr and Asr genuinely local
  and correct, Fajr and Isha estimated, and sunrise and Maghrib genuinely non-existent rather
  than estimated. The app currently replaces all six. There is a real-world implementation of
  the mixed approach in Oulu, Finland.

  Note what this is *not*: it is not about preserving an observable sunset, which was checked
  and refuted — the projection only ever engages when the sun genuinely never rises or never
  sets (§10). It is about not synthesising the two times that did not need synthesising.

  Separately: the home banner is not enough. A user may only ever meet the tile, the
  notification, the PDF or the **alarm**, and an alarm derived from a projection is a stronger
  claim than a number on a screen. Options are a per-prayer "estimated" marker carried
  everywhere, a one-time confirmation before projected alarms are first scheduled, or showing
  estimates while suppressing their alarms.

  **The PDF half is DONE, 2 Aug 2026.** `approximatedFrom` was reaching exactly one place in
  the app — `HomeScreen` — so the exported timetable printed projected times with no marking
  at all. That is the worst of the four surfaces, and not by a little: a PDF is the copy that
  *leaves* the app. It gets printed, pinned to a wall and handed to other people, with no
  banner above it, no countdown beside it and no way to ask it a question. A projected time on
  paper claims more certainty than the same number on a screen.

  It now prints, only when a day in the range was actually projected:

  > Some of these times are approximate. This far north the sun does not always rise or set,
  > so those days are worked out from latitude 45° instead. Ask your mosque what it follows.

  Verified by exporting a real month at Longyearbyen and rendering the PDF: the note appears
  under the subtitle, wraps to two lines, and the table is untouched. Most users see no
  change at all, because most days are nobody's projection.

  **A wording bug was caught by looking at the rendered output rather than the code.** The
  first draft said "so *Fajr and Isha* are worked out from latitude 45°", which is how the
  polar problem is always described and is wrong here: when a day is projected the engine
  replaces **all six** slots, sunrise and Maghrib included. The rendered table showed sunrise
  4:43 am on days the sun does not rise. Under-describing an approximation is the same class
  of error as not mentioning it.

  **The notification and watch-tile half is DONE, 2 Aug 2026** — see §10, "The two surfaces
  that never marked an approximated time".

  **The alarm question is DECIDED and BUILT, 2 Aug 2026 (owner's call, taken).** A projected
  time posts on the quiet channel by default and does not ring; the user can turn ringing
  back on, and only users who actually have such days are offered the switch. The full rule,
  the reasoning, and the part that must not be removed are in **§5.14**.

  Two of the three options listed above were therefore rejected, and the reasons belong here
  so they are not re-proposed as fresh ideas:

  | Option | Verdict |
  |---|---|
  | Suppress projected alarms outright, no override | **Rejected.** At Tromsø that silently disables a Fajr alarm for two months a year, discoverable only by not being woken. Being wrong in the safe direction is a default; removing the user's say is a different thing |
  | A one-time confirmation before projected alarms are first scheduled | **Rejected.** It asks the question months before it applies, at the moment the user is doing something else, and answers it for a season they cannot picture. The polar notice already asks it on the day it is true |
  | Per-prayer "estimated" marker carried everywhere | **Superseded, and no longer needed for this.** Every surface now marks the day. Per-*prayer* provenance remains a genuine open item, but only for the mixed-day work below — not for the alarm |

  **What is still open under A10** is the original mixed-day question: on a polar-day date,
  Dhuhr and Asr are genuinely local and correct while Fajr and Isha are estimated, and the
  engine currently replaces all six. Unchanged by today's work, and still worth doing.

- **A11 — Ramadan wording for Fajr, rather than changing the default angle.** One reviewer
  argued the worldwide default should err early (19.5°–20°) on precautionary grounds. The
  better argument is that there is no globally cautious angle, because one timestamp carries
  two opposite risks: late is dangerous for ending suhur, early is dangerous for the validity
  of the Fajr prayer itself. So keep 18°, but during Ramadan say plainly that local and
  national timetables may begin Fajr earlier and should be confirmed. Do **not** silently add
  an imsak margin — that is another convention and another religious choice.

  **DEFERRED by the owner, 2 Aug 2026, and the reason is scope rather than doubt.** His words:
  *"we are right now targetting users for the purpose of salah / namaaz hence trying to keep
  ourselves safe for now."* Suhoor and imsak are a fasting question sitting next to a prayer
  app, and the app does not currently claim that ground. The analysis above stands and should
  be picked up before the first Ramadan the app is live for — not because it is wrong, but
  because that is when a silent omission starts costing something.

- **A12 — Moonsighting's 60° slide is not applied between 60° and 65.7°.** Moonsighting say
  they slide Fajr and Isha down to 60° above 60°. Neither adhan nor the PHP library behind
  Aladhan implements it, so a Moonsighting user in that band gets un-slid times. Measured gap
  on Fajr: Helsinki 1 min, Anchorage 10, Trondheim 37, Reykjavík 49, **Luleå 91**. At Luleå in
  June the un-slid answer is degenerate — Isha 00:14 and Fajr 00:52, leaving 38 minutes of
  night. Real populations: Reykjavík, Trondheim, Anchorage, Umeå, Luleå, Tampere.

  Not fixed because it needs *partial* substitution — Fajr and Isha from 60°, sunrise/Dhuhr/
  Asr/Maghrib staying local, since the sun does genuinely rise and set there. That is the same
  machinery A10 needs, so the two should be done together or not at all. It is also worth
  deciding first whether the app is trying to *be* moonsighting.com's timetable or to
  implement adhan's faithful rendering of their published model; it currently does the latter,
  which is defensible, just not identical.

  > **⛔ Attempted and reverted, 2 Aug 2026 — and the reason upgrades this item from "more
  > work" to "not yet specified".** The partial substitution above was built exactly as
  > described: for `MOON_SIGHTING` above 60°, take Fajr and Isha from a 60° probe at the same
  > longitude and leave the other four local. It compiles, it is about fifteen lines, and it
  > **produces invalid days**.
  >
  > Swept over 60.5°–66°N, every day of 2026, three longitudes: **1,043 of 13,140
  > day-computations came out non-monotonic.** Not marginally — Luleå on 21 June:
  >
  > ```
  > Fajr 00:23   Sunrise 23:00   Dhuhr 10:38   Asr 15:30   Maghrib 22:09   Isha 20:43
  > ```
  >
  > **Isha lands 86 minutes before Maghrib.** The cause is structural rather than a coding
  > slip: Isha slides ~1.5 hours south while Maghrib stays at a local sunset that is very
  > late, so the two cross. Near the solstice at 63° the same crossing shows up as a
  > one-minute inversion, which is worse — it would have shipped unnoticed.
  >
  > **What that means.** The published sentence — *"at latitudes more than 60degrees, we slide
  > down to 60degrees and calculate Fajr & Isha"* — cannot be a literal coordinate
  > substitution, because a literal reading contradicts itself. Moonsighting must be doing
  > something further that the prose does not state: most likely combining the 60° twilight
  > values with a locally-anchored Sab'u Lail bound, as their own text describes for the
  > 55°–60° band. Deriving that from the words alone is guesswork, and guesswork is not
  > allowed in this file.
  >
  > **The blocker is therefore evidence, not effort.** What is needed is
  > **moonsighting.com's own published timetable for one place in the 60°–66° band in June**
  > — Trondheim, Umeå or Luleå — to see how they order Maghrib and Isha on the days where a
  > literal slide breaks. Their site would not serve a timetable to `curl` (the JSON endpoint
  > 500s and the HTML page builds its table in JavaScript), so this needs a browser, or an
  > email to them, or a printed timetable from a mosque in that band.
  >
  > Until that exists, the current behaviour stays. It is wrong by up to 91 minutes at Luleå,
  > which is bad — and it is *ordered*, which the fix was not. **A wrong-but-coherent day
  > beats an incoherent one**, and shipping the second to fix the first would have been a
  > clear regression dressed as a correction.
  >
  > Full evidence: §10, "The Moonsighting slide, built and thrown away".

- **A13 — ✅ CLOSED (1 Aug 2026). The polar notice has now been seen on a device, in both
  directions.** Two sessions had failed at this by fighting the emulator's location providers.
  The answer was to stop: **the app's own city search sets the location directly**, and
  Longyearbyen is in polar day through August. Verified 45° on the default method, 60° after
  switching to Moonsighting, and correct mirroring in RTL. Recipe and the `installRtl`
  version-code trap are in §10.

  Still unseen: the same state on the **watch**, which does not show the notice at all — that
  is A8, and it is a design question rather than a verification gap.

### Not done, in rough priority order

0. **✅ HyperOS `power_pending` — settled 2 Aug 2026. It defers delivery, and the fix beats
   it.** Measured overnight on the owner's Redmi Note 13 Pro with both builds holding the
   same 02:51:00 Fajr alarm: the fixed build fired at **02:51:00.037**; the shipped build
   **never fired**, and was still pending at 04:23 with `power_pending` holding it out to
   5 August. Both packages were in bucket 40 (RARE), Doze inactive, battery saver off — the
   only variable was `setAlarmClock` versus `setAndAllowWhileIdle`. Full evidence, including
   the policy lines and Xiaomi's own `not align this alarm … reason=6` log, is in §10.

   Nothing to do; recorded so the next session does not re-open it. What is *not* settled is
   Samsung — One UI has its own background policy and has not been tested.

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

40. **Google stops telling you the tester count at the exact moment it starts to matter.**
    Below twelve, the Dashboard prints *"N testers currently opted in"* under the
    production-access checklist. On reaching twelve it replaces that line with a tick and
    **never shows the number again** — so for the whole fourteen-day run, the one period when
    a drop below twelve would reset everything, there is no figure to watch and no
    notification if it slips. The Testers tab still shows a number (24 here), but that is
    *addresses added* and always the larger one — see lesson 36. Keep your own list of who
    confirmed they installed, because the Console will not help and the failure is silent.
    Verified by screenshot on 1 Aug 2026, when the count line vanished between two readings
    the same day.

41. **A finding from one town is a hypothesis, not a default.** The Slough measurement was
    real, reproducible and completely correct — and it still nearly produced the wrong change,
    because "MWL is 78 minutes out here" reads as "MWL is wrong". Testing the same default
    against fifteen major centres showed it is within about five minutes across South Asia,
    South-East Asia, the Middle East and Africa, which is where most of the world's Muslims
    live, and only fails above roughly 45° of latitude. **The scope of a fix should be
    measured, not inferred from the loudest report.** The check cost one script and turned a
    proposed rewrite — new methods, a per-prayer offset feature — into one conditional card
    shown to users above a latitude. When a single user's evidence points at a global
    default, the next step is more locations, not more code.

42. **Never leave the `installRtl` build on a device anyone else will pick up. Uninstall it
    and put `installDebug` back the moment you are done.** The `rtl` build type sets
    `app_language_tag` to **`ar-XB`**, a pseudolocale that does not merely flip the layout: it
    **reverses Latin characters**. "Sat, Aug 1" renders as "1 guA ,taS" and "2:54 AM" as
    "MA 2:54". That is the tool working exactly as designed, and it is unreadable on purpose.
    This build was left installed on the owner's emulator after a verification pass. He opened
    it, saw mirrored headings, reversed AM and PM, and English right-aligned, and reasonably
    concluded the app was broken and that the work had been sloppy. Every one of those
    "defects" was the harness. **Any screenshot from an RTL build must be labelled as one when
    it is shown to anyone, and the device must be returned to the normal build afterwards.**
    Leaving a deliberately-mangled build behind is not a neutral act: it destroys trust in
    work that was actually fine, and it costs a review cycle to undo.

    For the record, since it came up: **layout direction already follows the app's own
    language, not the device's.** `app_language_tag` is `en-GB`, `AppLocale` pins the whole
    configuration to it, so English is left-to-right and always will be. The day an Arabic or
    Urdu translation ships and declares its own tag, that language flips to right-to-left on
    its own with no code change, and `LocaleDisciplineTest` fails the build if a translation
    is added without declaring one. There is nothing to fix here and nothing to switch on.

43. **An affordance that is obvious to the person who wrote it is not an affordance.** The
    sect step advanced the moment a card was tapped and showed no button at all, leaving the
    bottom half of the screen empty. Whoever wrote it knew the cards were the control. Nobody
    else did, and a tester said so in exactly those words: he could not tell whether to tap or
    to press Next. Worse, the madhab step *did* show buttons, Back and Skip, while also
    advancing on tap, so the screen actively promised a Next that did not exist. The fix was
    not cleverness, it was sameness: one `StepButtons` composable, Back and Continue, on every
    step that asks a question, and selecting only ever selects. **When a screen has empty space
    where a button should be, that space is telling the user something. Believe it.**

44. **Test the whole world, or you are only testing yourself.** Every measurement in this
    project had been taken at Slough, because that is where the person reporting problems
    lives. The owner had to say it outright — *"don't just use Slough as an example … we are
    trying to cater for the masses"* — before the sweep was widened. Within an hour that found
    a **NullPointerException above 65.5°N that crashed the home screen, the watch tile, the
    ongoing notification and the alarm scheduler**, in summer and in winter, in every release
    shipped so far. It had been there from the first commit. No amount of testing at 51°N
    would ever have found it, because the bug does not exist at 51°N.

    The lesson is not "test more". It is that **a test suite inherits the blind spots of
    whoever wrote it**, and the cheapest correction is to sweep the parameter rather than
    sample it. Latitude from −90 to +90. Both solstices. Both hemispheres. All fourteen
    methods. That sweep is six unit tests and runs in under a second.

    A corollary about *reference points*: Aladhan does not crash at Tromsø, it degrades into
    six identical useless times. **Checking what a mature implementation does in the same
    situation is worth more than deciding for yourself what "correct" means.** We were not
    merely different from the reference, we were strictly worse, and that is a fact you only
    learn by going and looking.

45. **A wrong label is a real defect, not cosmetics.** The app already computed Indonesian and
    Singaporean prayer times perfectly — adhan's `SINGAPORE` preset is Fajr 20° / Isha 18°,
    which is exactly Kemenag and MUIS. But it was labelled *"Singapore"*, so roughly **240
    million Muslims** had no reason to ever select it, and sat on a default that put their
    Fajr eight minutes late.

    Nothing was wrong with the mathematics. Nothing needed building. **When a feature exists
    and nobody uses it, look at what it is called before you look at what it does.**

45a. **Verifying against a mirror is not verifying.** The same label, in the same commit, also
    claimed Malaysia — because Aladhan's JAKIM method agreed to the minute. It agreed because
    Aladhan still publishes Fajr 20° for JAKIM, which Malaysia abandoned in **November 2019**.
    The check was internally flawless and told me nothing.

    The failure was conflating two different questions. *"Does my 20° Fajr match another
    implementation's 20° Fajr"* is arithmetic, and Aladhan answers it well. *"Is 20° what
    Malaysia currently observes"* is a fact about the world, and no calculator can answer it —
    only the authority's own current publication can. When Singapore was checked properly,
    against MUIS's own 2024 PDF, it held up. Malaysia did not.

    **Any claim naming a country or an authority must be traced to that authority's own
    current publication.** A second implementation agreeing with you is not evidence; it may
    simply be stale in the same direction, and aggregators are stale far more often than their
    confident APIs suggest. This one had been wrong for nearly seven years.

46. **Take a second opinion seriously enough to check it.** Two other models reviewed this
    design. Both were useful and neither was right to accept whole. They correctly caught that
    adhan ignores `HighLatitudeRule` for Moonsighting, that `TWILIGHT_ANGLE` is itself a
    religious choice, that a 55° clamp uses a raw rather than absolute latitude, and — most
    valuably — that the whole investigation had ignored Fajr, which is the prayer that governs
    fasting. All confirmed, all acted on.

    They were also confidently wrong in ways that would have caused damage. One said Umm
    al-Qura was 30 minutes out during Ramadan; it has never been, because `PrayerEngine`
    already adjusts it. One said DST might be an hour out; all four 2026 transitions in both
    hemispheres are exact. One said the Southern Hemisphere seasonal model was northern-
    centric; adhan's `daysSinceSolstice` branches on the sign of the latitude. Each of those
    was an inference from a library or a general pattern rather than from **this** codebase.

    And the most interesting finding was one that **neither** of them reached: they both
    assumed the Fajr risk was in Britain, where MWL is *earlier* than local practice and
    therefore cautious. Measuring showed the danger is in Indonesia, where MWL is *later*.
    **A second opinion is a source of hypotheses, not of conclusions. Check every claim
    against your own code and an independent reference, and write down which ones were
    wrong — otherwise the next session re-imports the same confident errors.**

47. **A library returning a value is not the same as a library answering the question.**
    The Arctic crash was found by checking for null. The bug *underneath* it was worse and
    invisible to that check: at 71.5°N adhan returned a 27 January Asr of **13 March**. No
    exception, no null, no warning — just a confident wrong number that was displayed,
    exported to PDF and used to schedule an alarm. It shipped in v1.0.0 and in the closed test.

    Null is the failure mode a library *tells* you about, so it is the one you fix first and
    then stop looking. The dangerous ones are the failures it does not know it is having.
    After handling null, ask separately: *is this answer physically possible?* Here that meant
    two invariants the library never checks — the six times must be in order, and Dhuhr must
    sit in the middle of sunrise and sunset, because solar noon is the midpoint of the day arc.
    Both are one line. Both were missing.

48. **Choose thresholds by sweeping, not by taste.** The midpoint check needed a tolerance.
    Guessing would have produced a plausible round number and no way to defend it. Sweeping
    every 0.5° of latitude, four longitudes and every day of a year gave the actual
    distribution: below 65° the worst offset anywhere on Earth is **3 minutes**; above it, 59,
    87, 143, 212, 214. The gap between those two populations is enormous, so 30 minutes is
    obviously safe and obviously effective, and the comment can say *why* rather than
    *what*. When a constant guards a boundary, the sweep that chose it is the documentation.

49. **When two references disagree, find a third before believing either.** An independent
    NOAA-based sunrise service put this engine 2–7 minutes out, growing with latitude, which
    looked like a real defect in the most religiously sensitive direction — Maghrib slightly
    *early*. Changing it would have moved the sunset time of every user in the world. Aladhan,
    a third and completely separate implementation, agrees with this engine **to the minute**.
    The outlier was the free service, which uses a low-precision 1990 almanac algorithm.

    One disagreement is a question, not a finding. The cost of the extra check was one `curl`;
    the cost of skipping it would have been a wrong Maghrib for everyone.

50. **Exhaustive is cheap now, and it is the only thing that finds this class of bug.**
    7.3 million day-computations — every method, every madhab, every 0.5° from pole to pole,
    every day of a year — ran in seconds and found 2,239 broken days that four rounds of
    careful reading, two external model reviews and a targeted polar test suite had all
    missed. The targeted test only checked ordering *where the projection was already active*,
    so it could never have seen a fault in the days the projection did not cover.

    If a check can be run over the whole input space, run it over the whole input space. The
    sweep is now a permanent test (`PolarAndHemisphereTest`) for exactly that reason.

51. **Ask what a body publishes, not what it is reported to publish — but check the sky too.**
    Lesson 45a said trace claims to the authority's own words. This went one better and
    was worth it. The Fiqh Council's 45/48/66 bands were reported consistently by several
    secondary sources, which proves only that they copy each other. What proved them was
    arithmetic: 18° of twilight last occurs at midsummer at latitude 48.56°, and 66.56° is the
    polar circle. Two band edges landing exactly on astronomy is not something a garbled
    retelling produces.

    Where a rule has a physical basis, that basis is a better witness than any number of
    websites. Look for it.

52. **A UI condition that gates a whole section on transient state will eventually hide the
    thing the user needs most.** The city entry in onboarding appeared only while `problem`
    was `PERMISSION_DENIED` or `NO_FIX`. Searching cleared `problem`; failing set it to
    `CITY_NOT_FOUND`. Neither value was in the pair, so the act of using the feature deleted
    the feature — along with its own error message and the escape hatch beside it, leaving a
    first-run screen with nothing enabled on it.

    The fix was not a better condition. It was **deleting the condition**: show it whenever
    there is no location. State you do not have cannot be wrong. When a section's visibility
    depends on a value that the section's own actions change, that is the bug, not the value.

53. **"It did nothing" usually means "it did it silently".** The tester said the city search
    was not selecting his city. It was selecting it perfectly, every time. The sheet stayed
    open with his text still in it and the only moving part was a spinner next to a button he
    had not pressed. An action that succeeds and shows nothing is indistinguishable from one
    that failed, and users report what they can see.

    Before debugging the mechanism, check whether the mechanism already works and the
    *report* of it is missing. It is faster, and it is the more common fault.

54. **Read what the API promises, not what it is called.** `setExactAndAllowWhileIdle` has
    "exact" in the name and this paragraph in its own reference: *"the OS will allow itself
    more flexibility for scheduling these alarms than regular exact alarms… it may take even
    more liberties."* `setAlarmClock` is the only one Google says is never moved. Measured
    consequence with the permission ungranted: `window=+1h0m0s0ms` — a **one-hour** delivery
    window, while the app's own banner told users the delay was "a few minutes".

    Two habits from this. Read the reference paragraph for the API you chose, not the guide
    page for the family it belongs to. And when the app makes a promise to the user about
    timing, check that the platform makes that promise to you first.

55. **The obvious cause of a screenshot is often not the cause.** A button under a keyboard
    means an insets bug — except it did not. Material3 1.4.0 already applies `imePadding()`
    inside the sheet's own window unconditionally, so the fix everyone reaches for is a no-op
    and `contentWindowInsets = WindowInsets(0)` cannot even switch the behaviour off. The
    real cause was the sheet's half-height default: the field opened pinned to the bottom
    edge with the button off-screen entirely, **before any keyboard was involved**.

    Found by opening the sheet and dumping the view tree, not by reading about insets. When a
    screenshot has an obvious culprit, reproduce the screenshot first and see what is true
    with the culprit removed.

56. **Ask two agents, and tell the second one to attack the first.** Four research briefs
    were written from primary sources and then handed to four refuters whose only instruction
    was to disprove them. The refuters overturned the load-bearing claim in two of the four —
    including a bytecode scan that returned a false negative because it searched for
    `WindowInsetsPaddingKt.imePadding` when on Android the symbol lives in
    `WindowInsetsPadding_androidKt`. Both briefs read as confident and well-cited.

    A citation is not a verification. "I fetched the URL and the sentence is there" is, and
    it is worth paying a second agent for.

57. **"Do not touch it until the window closes" was two decisions wearing one coat.** This
    file previously told the next session not to act on tester feedback during the closed
    test. The real constraint is only on *uploading* — writing, testing and committing a fix
    reaches Google not at all. Conflating them cost nothing this time because the same session
    noticed; it could easily have cost a fortnight of idle waiting.

    When writing a "do not do X yet" instruction, name the actual irreversible step. X is
    almost always narrower than it first appears.

58. **A tool that reports success is not a tool that did something.** `am broadcast` at a
    non-exported receiver prints `Broadcast completed: result=0` and delivers nothing at all —
    no process start, no notification, no log line, no error. Several rounds of "the alert
    isn't posting" were spent on the app before the receiver's `android:exported="false"` was
    read. The silence was the test failing, not the code.

    The tell was there the whole time: the app's *process never started*. When a component
    that should have run leaves no trace anywhere — not even a crash — suspect that it was
    never invoked before suspecting that it ran and misbehaved. And before reporting a defect
    from an absence, prove the trigger actually fires.

59. **The emulator was honest about the fix and silent about the world.** Everything §6 claims
    about `setAlarmClock` reproduced exactly on real hardware. What the emulator could not show
    was the *context*: a real user's install had drifted to App Standby bucket RARE, arrived
    without `SCHEDULE_EXACT_ALARM` because Android 14 stopped granting it, and ran under an
    OEM power policy that parks every alarm on the device three days out while it sleeps.

    None of those are bugs in this app and none are reproducible on an emulator. They are the
    conditions the app has to survive. Test the fix wherever is convenient; test the
    *assumptions* on hardware someone actually owns.

60. **Read your own rejected-option comments before overruling them.** Having measured the
    status-bar side effect of `setAlarmClock`, the obvious move was to use the quieter alarm
    for notification style. That exact alternative had been considered and rejected the day
    before, with the reason written down three lines above the code — and the reason still
    held.

    What the new measurement legitimately changed was one word: the rejection called the cost
    "cosmetic", and it is slightly more than that. So the comment was corrected and the
    behaviour was left alone. A recorded rationale is not an obstacle to be argued past; it is
    the previous session telling you it already thought about this. Update the record when new
    evidence refines it, and change the behaviour only when the evidence actually overturns it.

61. **Design the experiment so the confound cannot survive it.** The overnight Fajr test could
    have been run the easy way — fixed build on the phone, see if it fires — and it would have
    proved nothing, because the fixed build was also newer, also freshly installed, and had
    started life two standby buckets higher. Running **both builds on one phone, at one
    prayer, in one minute** meant the OEM policy, the Doze state, the charger, the bucket and
    the clock were all held constant by construction rather than by argument. When the result
    came in, the policy line showed every alternative explanation already negative
    (`app_standby=-3h3m`, `device_idle=--`, `battery_saver=--`) and exactly one constraint
    binding. There was nothing left to argue about.

    The cheap version of this test would have produced a confident answer of unknown value.
    An hour spent making a second variable impossible was worth more than a day spent
    defending a single-arm result.

62. **A measured "it is late" can turn out to be "it never arrives".** The tester said
    notifications were late. The first dump agreed and quantified it: a one-hour window. That
    was a real finding and it was correctly reported — but it was still the *smaller* half of
    the truth. Left overnight, the shipped build's Fajr did not slip by an hour; it was moved
    three days and would have surfaced only when the phone was next unlocked.

    A snapshot of a queue tells you what the system *intends*. Only waiting tells you what it
    *does*. Where the difference matters — and for a prayer alert it is the whole product —
    pay the wall-clock cost and watch it happen.

63. **Protecting the user's sleep and protecting the measurement were the same problem, and
    both had to be solved before either.** Silencing the 02:51 alerts by muting the phone
    would have muted the owner's own alarm; DND "alarms only" silenced the app and left his
    alarm working. Silencing by suspending the package would have destroyed the very thing
    being measured. The way out was to pick a signal the mitigation could not touch — the
    alarm leaving the pending table, which DND cannot affect — and add `logcat` as a second
    witness rather than trusting that.

    When a safety measure and a measurement appear to conflict, the answer is usually a
    different instrument, not a compromise on the safety measure.

64. **A healthy-looking device can be the least informative one.** The Play build on the
    Galaxy S23 Ultra showed `window=0` on every alarm and looked like proof the fix was
    unnecessary. It was proof of nothing: that phone happened to have the permission granted
    *and* every prayer on Alarm style, which is the one combination the old code already
    handled correctly. The defect was invisible there for the same reason it is invisible to
    a developer who granted the permission months ago and forgot.

    Revoking the permission to recreate a fresh install turned a useless observation into a
    decisive one, and it reproduced the one-hour window on Samsung immediately — which is
    what proved the bug was Android's, not Xiaomi's. When a device disagrees with a
    measurement you trust, find what is different about that device before concluding the
    measurement was wrong.

65. **"That looks wrong" from a non-technical owner is data, even when the thing he is
    looking at is working as designed.** He was shown the `ar-XB` preview build — English
    words laid out right-to-left, punctuation stranded — and said make it better. Strictly he
    was looking at a developer tool behaving correctly, and the shipped English app has never
    been anything but left-to-right.

    Dismissing it there would have missed the actual finding, which was one `grep` away:
    there was no bidi isolation anywhere in the codebase, and city names arrive from a
    geocoder in Latin script even for an Arabic reader. The complaint pointed at the right
    screen for the wrong reason. Answer the reason *and* check the screen.

66. **The build type that cannot be installed is the check that never runs.** `installRtl`
    had existed for a while, was documented in CLAUDE.md as a required step before any layout
    change, and had never once run on a physical phone — not because RTL is hard, but because
    it shared the release `applicationId` and would have required uninstalling the Play build
    from the only phones available. A one-line `applicationIdSuffix` fixed it.

    Before assuming a documented check is being performed, confirm it is *possible* on the
    hardware people actually have.

67. **Two consecutive reads of the same table disagreed, and both were true.** The first
    `dumpsys alarm` after the Xiaomi was plugged back in showed the shipped build's alarms
    parked three days out; the second, taken seconds later, showed them released and holding
    a one-hour window. Same alarm object, same phone. HyperOS had let them go the moment the
    device became active.

    Either read alone supports a confident, wrong headline: "permanently parked for three
    days", or "no OEM deferral at all, just Android's hour". A state that changes *because
    you started observing it* is normal on a phone — plugging in, unlocking and running
    `adb` are all inputs. Sample twice before writing the sentence.

68. **The competitor on the user's own phone can answer a question your own app cannot.**
    Muslim Pro sits on the owner's Xiaomi getting exact alarms with no permission prompt at
    all, because it declares `USE_EXACT_ALARM` rather than `SCHEDULE_EXACT_ALARM`. One
    `dumpsys` on a device we already had told us the entire permission problem is
    *avoidable*, not merely mitigable — something no amount of reading our own code would
    have shown.

    The discipline is what came next: it was **not** adopted. Google's policy admits only
    "an alarm or timer app" and "a calendar app that shows event notifications", and apps
    that request it are "subject to review, and those that do not meet the acceptable use
    case criteria will be disallowed from publishing". *Another app shipping it is evidence
    that it can pass review, not proof that we would.* For a first-time personal developer
    mid-closed-test, a rejection costs the launch; an hour's alarm slip costs an hour. The
    upgrade is real and it is written down (§11) — it is just not worth taking before
    production. Finding a better road is not the same as being right to take it today.

69. **Copy written honestly under uncertainty becomes dishonest once you measure.** Both
    exact-alarm strings deliberately avoided a number, with a code comment explaining that
    Android documents no upper bound so "a few minutes" would be a promise we could not
    keep. That reasoning was sound — and it silently expired the moment denial was measured
    at a one-hour window on two makes and a never-fired alarm overnight.

    "May deliver them late" was now the *understatement* that talks a user out of the
    permission. A comment recording why a cautious choice was made is exactly the thing that
    should be re-read whenever new evidence lands, not treated as settled because it is
    well-argued. Grep your own hedges after every measurement.

70. **"More specific" is not the same as "better", and a passing test suite will not tell
    you the difference.** The town-not-county fix put `featureName` third in the chain,
    ahead of the county, because it is the most specific field the geocoder offers. Eight
    tests passed, including one asserting a house number is rejected. Then it ran, and the
    home screen said **"Townhouse Hotel, United Kingdom"**.

    Every test had been written against the failure I had already imagined — a numeric house
    number — and none against the one that actually happens, a named point of interest,
    because I did not think of it until a real geocoder produced it. The tests were not
    weak; they were a faithful record of my imagination. Worse, the wrong answer contradicted
    a *different* promise made in the same commit, that the app only ever asks for an
    approximate position; a fix can breach a guarantee that lives two files away.

    The generalisation: when ordering fallbacks, ask what the field contains when it is
    *wrong*, not what it contains when it is right. And run the thing.

71. **A diagnostic that corrupts everything equally cannot tell you what is broken — and it
    will hide one real bug for as long as it exists.** The `rtl` build type pinned `ar-XB`,
    a pseudolocale that reverses Latin characters. Every screen looked mangled, so nothing
    stood out. Swapping it for a real RTL locale (`ur`) cleaned up nine artefacts and left
    exactly one still wrong — the Hijri date, misordered in a way that survives into a
    genuine translation. That bug had been on screen the whole time, indistinguishable from
    the noise.

    The tell was the owner reporting the same thing twice. The first time it was answered
    with "the preview is behaving correctly", which was true and useless. Noise in a
    diagnostic is not neutral: it costs the reviewer's attention, it costs their trust in
    every later report, and it camouflages the signal you built the diagnostic to find.

72. **The obvious version of a bidi fix is often the exactly-wrong one.** Fencing the whole
    Hijri date reads correctly to an English eye and is backwards to the reader it is for;
    fencing only the month name is right. The difference cannot be seen by reading the code
    or the string — it only appears once the Unicode algorithm has actually laid it out.
    Both variants are now tests, the wrong one included, because "simplify this to wrap the
    whole thing" is a change someone will otherwise make on sight.

73. **A guard that cannot observe its own trigger will report success without running.**
    `NoTranslationsYetTest` reads resource folders off disk. Gradle has no way to know that,
    so the task was `UP-TO-DATE` and skipped, and adding `values-ar/` produced *zero
    failures* on the first run. It only failed under `--rerun-tasks`, which nobody passes.

    That is the worst failure mode a check can have, and it is invisible: the suite is green
    either way. Any test that reaches outside its declared inputs — filesystem, environment,
    generated output, another module's sources — needs those inputs declared, and needs its
    red state demonstrated at least once. Write the guard, then break the thing on purpose
    and watch it catch it. A guard never seen to fail is not known to work.

74. **Stopping the measurement when the event was due can invert the conclusion.** Today's
    Dhuhr was watched on two builds. At 13:13 the shipped build's alarm was still queued with
    HyperOS holding it three days out, and that was written up — correctly as an observation —
    as "it will fire on 5 August". Kept running, the truth was different and worse: at 13:22
    the app's own `DailyRescheduleWorker` rewrote the alarm set and **cancelled the parked
    alarm**, so the alert was never delivered at all and nothing remained to show it had been
    due.

    Two snapshots, two wrong stories. 13:13 says "deferred three days". 13:25 says "nothing
    scheduled, everything normal" — the most misleading reading of the three, and the one a
    casual check would land on. Only the interval between them says "the user missed Dhuhr".

    The general shape: when measuring whether something *happens*, the window has to extend
    past the deadline far enough to see the cleanup, because cleanup erases the evidence. And
    note what caused it — a **reliability feature** (five reschedule triggers, added to make
    alerts more dependable) combined with an OEM power policy to produce silent loss. Neither
    is a bug alone.

75. **A bound is not an invariant, and the difference is measurable.** The per-prayer
    corrections are capped at plus or minus thirty minutes, and it was assumed — reasonably,
    and wrongly — that a cap that small could not put a day out of order. A sweep across
    eight latitudes and every seventh day of the year found the counter-example on the first
    high-latitude row: at 59.9 degrees on 1 January, Dhuhr and Asr are **twenty-six minutes
    apart**, so the legal pair (+30, -30) crosses them. Midwinter squeezes the middle of the
    day to almost nothing and no fixed cap survives it — any bound small enough to be safe at
    60 degrees is too small to be useful at 51. The fix is a monotonic clamp in the engine,
    not a smaller number. The general shape: when a limit is supposed to guarantee an
    ordering, sweep for the crossing rather than reasoning about the typical case, because
    the typical case is not where limits fail.

76. **The library's own feature can be the wrong place to put it.** adhan ships
    `CalculationParameters.adjustments`, which is exactly this feature, and using it would
    have been the ladder-correct move. It would also have been a bug: three of this engine's
    six outputs never pass through those parameters — the Shia Maghrib comes from a second
    `PrayerTimes` built with probe parameters, the Umm al-Qura Ramadan Isha is arithmetic
    done in `PrayerEngine`, and a projected polar day is computed at a borrowed latitude.
    Half the prayers would have silently ignored the user's correction. Applying the offsets
    to the finished map instead covers every path by construction, and the test that proves
    it asserts the *property* (whichever path produced this time, the correction reached it)
    rather than the implementation. Before reaching for a library's built-in, check how many
    of your own code paths actually go through it.

77. **Android strips whitespace from an unquoted string resource, and the failure is
    invisible in review.** `<string name="list_separator">, </string>` compiles to `","`.
    The summary line rendered as "2 prayers adjusted,Date +1 day" on the device and looked
    perfectly correct in the XML, in the Kotlin, and in the diff. Only reading it on a screen
    caught it. Quote the value — `", "` — whenever leading or trailing space is load-bearing.
    Same family as the project's standing rule that compiling is not running.

78. **The rule that looked contradictory had a missing condition, and the condition was one
    sentence away.** The Moonsighting high-latitude slide was implemented, measured, found to
    produce Isha before Maghrib, and reverted, on the reading "above 60 degrees, slide to 60".
    Their own page carries the qualifier that reading dropped: the slide is conditional on
    the day being longer than **18 hours or shorter than 6**, it applies to **Fajr and Isha
    only**, and it is a **summer** rule ("in winter, we use research by Moonsighting.com for
    Subh-Sadiq and Shafaq"). That does not make the earlier implementation correct — Luleå in
    late June satisfies the condition and the inversion is real — but it does mean the
    contradiction was in the reading, not only in the source. When a published rule appears
    to contradict itself, re-read the whole passage for a guard clause before concluding the
    body is doing something undocumented.

79. **A safe-looking default can disable a feature for a season, and the scope is in the data
    model rather than the diff.** "A projected time should not ring" reads like a caveat on a
    rare day. `approximatedFrom` is a property of the *whole day*, and above the polar circles
    every day in the polar season is projected — so at Tromsø the same one-line rule silently
    removes a user's Fajr alarm from late May to late July, discoverable only by not being
    woken by it. The rule was right and shipped; what was missing was the override beside it.
    Before defaulting a behaviour off, ask how long it stays off for the worst-affected user.
    If the answer is "two months", it needs a switch, however sound the default.

80. **A place that reads as extreme is not evidence about latitude.** Ushuaia was written into
    the "shows the polar switch" side of a test because it is the southernmost city on earth,
    and the test failed. At 54.8°S it is below the Antarctic Circle, the sun rises and sets
    every day of the year, and it needs the switch no more than London does — the genuine
    southern case is McMurdo at 77.9°S. It is kept on the hidden side of that test precisely
    because it is the row that looks like it belongs on the other one. Reputation is not a
    coordinate; put the number in the test and let it answer.

81. **`installRtl` reports a red build for a variant that compiled perfectly, and the cause is
    the other devices.** It installs to *every* attached device, so with the Xiaomi and the
    watch emulator plugged in it collects `INSTALL_FAILED_USER_RESTRICTED` from HyperOS and
    `INSTALL_FAILED_UPDATE_INCOMPATIBLE` from the watch, and fails. The APK is already sitting
    in `app/build/outputs/apk/rtl/`; install it to one device with `adb -s`. Read which task
    failed before believing the variant is broken — this is the same shape as the "missing"
    banner that had only scrolled off screen (lesson 51).


---

*Made with love, free for the Ummah.*
