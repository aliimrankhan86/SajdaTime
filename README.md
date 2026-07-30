# SajdaTime

A free, ad-free, privacy-first Muslim prayer times and Qibla app for Android and Wear OS.

No accounts. No analytics. No ads. No tracking. Your location never leaves your phone.

---

## What it does

- Calculates the five daily prayers plus sunrise, entirely offline, from your approximate location
- Supports Sunni (all four madhabs) and Shia (Jafari / Ithna Ashari) conventions
- Qibla compass, corrected to true north, verified against a reference implementation
- Live countdown to the next prayer
- Local notifications at each prayer time, working even when the app is closed
- Optional alarm mode with a sound you choose, able to sound through Do Not Disturb
- Optional silent "next prayer" badge in the notification shade
- PDF export of today, the next 7 days, or the whole month
- A Wear OS app and tile that work on their own, with or without the phone nearby
- Light and dark themes, both verified against WCAG 2.1 AA contrast

## Build and run

Requires JDK 17+ and the Android SDK.

```bash
./gradlew :app:assembleDebug :wear:assembleDebug
```

Install onto a connected phone or emulator:

```bash
./gradlew :app:installDebug
```

Run the checks:

```bash
./gradlew :core:testDebugUnitTest :app:testDebugUnitTest :app:lintDebug :wear:lintDebug
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
core/   Prayer and Qibla calculation. No Android imports, so it ports to iOS as is.
        Shared by the phone and the watch.

app/    The phone app.
  data/         Local settings (DataStore), location, compass, city lookup, watch sync.
  notify/       Alarm scheduling, notification channels, boot and daily rescheduling.
  pdf/          Timetable export via Android's built-in PdfDocument.
  ui/           Compose screens: onboarding, times, qibla, settings, theme.

wear/   The Wear OS app and the next-prayer tile.
```

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - full requirements, design decisions,
  business rules, privacy model, and the iOS porting guide.

## Privacy

The only permission the app needs is approximate location, read while the app is open.
Coordinates are stored on the device and never transmitted.

One optional feature touches the network: if you decline location permission, you can type
a city name, which is sent once to the free Aladhan service to resolve coordinates. The app
tells you this before it happens. Cloud backup is switched off so your cached location
cannot be copied to a backup server.

If you have a paired Wear OS watch, your settings are sent to it over Google's local Data
Layer so the watch can calculate times itself. That travels between two devices you own
and never reaches a server.

## Licence and credits

Prayer times are calculated with [adhan-java](https://github.com/batoulapps/adhan-java) (MIT)
by Batoul Apps. The Qibla is calculated by this app directly.

**Disclaimer:** SajdaTime is a helper, not a religious authority. It was built with the help
of artificial intelligence and may get things wrong. If a time or direction looks off, check
with your local mosque or someone qualified to advise you.

Made by Ali Imran Khan, as an ongoing charity for the Ummah. Free, for the sake of Allah.
