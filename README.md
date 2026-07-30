# SajdaTime

A free, ad-free, privacy-first Muslim prayer times app for Android.

No accounts. No analytics. No ads. No tracking. Your location never leaves your phone.

---

## What it does

- Calculates the five daily prayers plus sunrise, entirely offline, from your approximate location
- Supports Sunni (all four madhabs) and Shia (Jafari / Ithna Ashari) conventions
- Live countdown to the next prayer
- Local notifications at each prayer time, working even when the app is closed
- Optional silent "next prayer" badge in the notification shade
- PDF export of today, the next 7 days, or the whole month
- Light and dark themes, both verified against WCAG 2.1 AA contrast

## Build and run

Requires JDK 17+ and the Android SDK.

```bash
./gradlew :app:assembleDebug
```

Install onto a connected device or emulator:

```bash
./gradlew :app:installDebug
```

Run the checks:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

### One-time setup note

This repo currently lives under `~/Documents`, which macOS syncs to iCloud Drive. iCloud
writes conflict copies (`SomeClass 2.class`) into `app/build/` mid-build, which makes
dexing fail with `Failed to process: .../compileDebugKotlin/classes`.

If a build fails that way, clear the duplicates and rebuild:

```bash
find . -name '* [0-9].*' -not -path './.git/*' -delete && ./gradlew clean
```

The permanent fix is to move the project somewhere iCloud does not sync, for example
`~/Developer/SajdaTime`.

## Project layout

```
app/src/main/java/com/sajdatime/app/
  core/         Prayer calculation. Pure Kotlin, no Android imports - portable to iOS.
  data/         Local settings (DataStore), location, one-off city lookup.
  notify/       Alarm scheduling, notification channels, boot/daily rescheduling.
  pdf/          Timetable export via Android's built-in PdfDocument.
  ui/           Compose screens: onboarding, home, settings, theme.
```

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - full requirements, design decisions,
  business rules, privacy model, and the iOS porting guide.

## Privacy

The only permission the app needs is approximate location, read while the app is open.
Coordinates are stored on the device and never transmitted.

One optional feature touches the network: if you decline location permission, you can type
a city name, which is sent once to the free Aladhan service to resolve coordinates. The app
tells you this before it happens. Nothing else ever leaves the device, and cloud backup is
switched off so your cached location cannot be copied to a backup server.

## Licence and credits

Prayer times are calculated with [adhan-java](https://github.com/batoulapps/adhan-java) (MIT)
by Batoul Apps.

Made with love, free for the Ummah. This app is a charity project, made for the sake of Allah.
