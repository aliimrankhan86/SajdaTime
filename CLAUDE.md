# SajdaTime — instructions for any AI working on this project

This file is loaded automatically. It is deliberately short. **The full handover is
[`docs/HANDOVER.md`](docs/HANDOVER.md) — read it in full before changing any code.**

---

## What this project is

A free, ad-free, privacy-first Muslim prayer times and Qibla app for Android and Wear OS,
built as an ongoing charity (*sadaqah jariyah*) for the Ummah. It is not a product, there is
no business model, and there never will be one. Every decision follows from that: no ads, no
accounts, no analytics, no tracking, no server, and no revenue.

It has to work **for the masses** — including a phone with no signal, an old device, a user
who has never changed a setting, and a user who cannot read English. When a trade-off appears
between elegant and dependable, dependable wins.

---

## The commit rule — always, every time

**When you commit, commit the understanding, not just the diff.**

Code records *what* changed. It cannot record why, what was tried and rejected, what was
verified versus assumed, or which decisions are deliberate and must not be "improved" back
in. That reasoning is the thing that is expensive to rebuild and easy to lose, and losing it
is how the next session confidently undoes a decision it did not know was made on purpose.

So, as part of every commit that changes behaviour:

1. **Write a commit message that explains the reasoning**, not the file list. Say what was
   wrong, how it was found, why this fix and not another, and what you verified. Git already
   knows which lines moved.
2. **Update `docs/HANDOVER.md`** — it is the single source of truth. Add new business rules
   to §5, new verification evidence to §10, new pending items to §11, and anything that must
   never be reverted to §2 "Deliberately NOT used" or §11 "Deliberate non-goals".
3. **Record rejected options, with the reason.** A decision without its rationale looks like
   an oversight and will be reversed.
4. **Comment the non-obvious in the code itself**, where the next reader will actually be
   standing. Especially anything that looks wrong but is right.
5. **Say what you did not test.** An honest gap is useful. A silent one is a trap.
6. **Push it.** Work that only exists on this machine is work that can be lost.

If a change taught you something — a platform trap, a wrong assumption, a bug class that
reading could never have caught — that lesson belongs in `docs/HANDOVER.md` §15 so the next
session inherits it instead of relearning it.

---

## Hard rules — these are not preferences

- **The religious disclaimer must never be removed, softened, or buried**, and the dua
  request in its final paragraph belongs there and *nowhere else* — the user is asked once
  and never nagged.
- **Never machine-translate the app.** Prayer and madhab names are religious content; each
  language needs a native speaker before it ships.
- **The app follows the phone's language, and goes right-to-left only when it has the
  words to.** `AppLocale` reads the app's language out of the resources, so the day a
  reviewed `values-ar/` ships, an Arabic phone gets Arabic, Arabic-Indic digits and an RTL
  layout with no code change. Until that day the app stays English and left-to-right on
  *every* phone, whatever the phone is set to — because English laid out right-to-left is
  genuinely wrong, not merely unusual, and the owner has ruled it out. Two tests in
  `LocaleDisciplineTest` fail the build if either half slips. See `docs/HANDOVER.md` §5.16.
- **Never call the Aladhan API — or any network — from the shipped app to calculate times.**
  Verifying against it during development is correct and encouraged. Calling it at runtime is
  not, and the reasons are in `docs/HANDOVER.md` §2. The owner was asked directly and said no.
- **Never generate, hold, request, or view the release signing key.** It is the owner's
  alone. See `docs/RELEASING.md`. `*.jks`, `*.keystore` and `keystore.properties` are
  gitignored and must never be committed.
- **Location stays on the device.** Coarse only, foreground only, never transmitted. Cloud
  backup and device-to-device transfer are both off. `docs/privacy.html` is a published
  promise, so any change that touches data handling has to be reflected there in the same
  commit.
- **Do not move this project into `~/Documents`, `~/Desktop`, or any iCloud-synced folder.**
  iCloud writes conflict copies into `app/build/` and breaks dexing. It cost three separate
  debugging sessions.

---

## How to work here

- **Compiling is not working. Run it.** The watch app once compiled and linted clean for an
  entire release while being unable to ever obtain a location.
- **Before any layout change, check it right-to-left — and read the result as a *layout*
  check.** `./gradlew installRtl` runs the whole app in RTL without a translation or a
  device setting. Roughly half the world's Muslims read right-to-left, and the first Arabic
  or Urdu translation should not be the moment anyone finds out whether the screens survive
  it. But it shows **English words in a right-to-left paragraph**, which always looks
  broken, because it is: stranded full stops, meridiems on the wrong side. Judge the
  mirroring — margins, chevrons, nav order, clipping — and ignore the prose. **Do not put
  its screenshots in front of the owner as findings.** He has reported it as broken three
  times, correctly each time, and every one of those rounds spent trust that a real RTL
  report will need. Report what the check *concluded*, not what it looked like.
- **Run the phone and watch emulators together and compare them.** The worst bug in this
  project's history lived in the gap between two modules that were each individually correct.
- **Verify against an independent reference, not your own reasoning.** Every real calculation
  bug here was caught by comparing to Aladhan; three confidently-reported "bugs" turned out to
  be errors in the test rather than the code, so check both sides.
- **Measure the fix, do not just read it.** A modifier change that looked right and reviewed
  clean did nothing at all until it was screenshotted.
- **Colours come from `docs/DESIGN_SYSTEM.md`, never from a hex in a composable.** Every
  one is a Material 3 role, and `ColorContrastTest` fails the build if a pair drops below
  WCAG AA. A visual change is not finished until `docs/store/screenshots/` is retaken.
- **Ponytail discipline:** stdlib and platform first, no speculative abstractions, shortest
  working diff. Mark deliberate simplifications with a `ponytail:` comment.
- **The owner is not technical.** Explain in plain language, distinguish verified from
  assumed, never call something done when it is untested, and never ask him to go and test
  it on a device himself and report back.
- **He does have real devices, and they find things the emulators cannot.** A Redmi Note 13
  Pro (HyperOS) and a Galaxy S23 Ultra (One UI), which he will plug in if asked. Every
  alarm-reliability fact this project knows came off the Redmi, and the S23 Ultra found both
  a geocoder bug and a layout bug on 15 Aug 2026 that twenty emulator probes had missed. So
  **ask for the phone when the change touches layout, location, alarms or permissions.**
  What to ask for, in this order, because he will not know the terms: *Settings → About
  phone → Software information → tap Build number seven times*, then *Settings → Developer
  options → USB debugging on*, then pick **File Transfer / Android Auto** on the USB
  notification, then **Allow** on the "Allow USB debugging?" box. Also ask him to turn on
  *Developer options → Stay awake*, or the screen locks mid-test and you cannot unlock it.
  You can tell debugging is still off without asking: the phone shows on the USB bus as
  `idProduct 0x6860` (file transfer only) and becomes `0x6864` when adb is available.
- **Install on his phone with `./gradlew installSideload`, never `installDebug`.** The Play
  build is on that handset. A debug-signed `com.sajdatime.app` cannot sit beside it, so a
  plain debug install means uninstalling the Play copy, which resets his closed testers and
  costs a fortnight (`docs/RELEASING.md`). `installSideload` uses the `.sideload` suffix and
  installs alongside. Prefix with `ANDROID_SERIAL=<serial>` so Gradle does not target an
  emulator instead. **Never enter his PIN or any credential**, and put back anything you
  change on his phone — font scale, theme, locale.

---

## Before you say you are finished

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then run it on both emulators, and tell the owner what you verified and what you did not.

*Made with love, free for the Ummah.*
