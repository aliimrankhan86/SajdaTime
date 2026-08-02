# Production readiness audit — 2 August 2026

**Written to answer one question: should SajdaTime go to production, and what has to happen
first.** Everything here is either measured or labelled as unmeasured. Where a claim comes
from Google's own documentation it is quoted; where it comes from developer community reports
it says so, because on the central question the two do not fully agree.

The long-form evidence is in [`HANDOVER.md`](HANDOVER.md) §10 and §11. This file is the
decision.

---

## 1. The verdict

**The code is ready. The release is not, and the gap is one specific thing: the build the
testers are running is not the build that has been fixed.**

There is no reason to stop, no defect that should block production, and one action that
should happen sooner rather than later. The recommendation is in §7.

---

## 2. The single most important fact in this document

**The version live on the closed testing track does not contain the alarm fix.**

Measured on the owner's Redmi Note 13 Pro at 13:07 today, reading the system alarm table for
the Play-installed copy:

```
type=RTC_WAKEUP origWhen=2026-08-02 13:10:00.000 window=+1h0m0s0ms flags=0x20
type=RTC_WAKEUP origWhen=2026-08-02 17:17:00.000 window=+1h0m0s0ms flags=0x20
```

`window=+1h` means Android may deliver each prayer alert **any time inside the following
hour**. The fixed build, installed beside it on the same phone and holding the same nine
prayer times, reads `window=0 flags=0x3` — exact.

So every tester currently in the fortnight is testing an app whose prayer alerts can be up to
an hour late, and — on Xiaomi/HyperOS overnight — may not arrive at all. That was measured:
an identical 02:51 Fajr alarm fired at **02:51:00.037** on the fixed build and **never fired**
on the shipped build, which was still queued at 04:23 with HyperOS holding it three days out.

This matters for the decision because of §3.

---

## 3. Does uploading a fix restart the 14-day clock?

This is the question the whole plan hangs on, and the honest answer has two halves.

**Google's own wording** ([Play Console Help 14151465](https://support.google.com/googleplay/android-developer/answer/14151465)):

> "at least 12 testers must be opted-in to your closed test. They must have been opted-in for
> the last 14 days continuously."

> "we won't count testers who opted in, tested for less than 14 days, and then opted out.
> Even if they opt back in so that they are opted in for a total of 14 days, these 14 days
> must be consecutive to count."

**The requirement is written entirely in terms of tester opt-in status. It says nothing about
builds, uploads or versions**, and the page does not list anything that "breaks continuity"
other than opting out.

**Community reports say something adjacent but different**: that publishing a new bundle
mid-window can disrupt the streak *if testers then fail to update*. That is a claim about
tester behaviour, not about a Play rule, and it is unverified. It is recorded here rather than
dismissed, because it is the reason the previous plan chose to wait.

**What follows, and it inverts the cautious instinct.** The event that demonstrably resets the
clock is a **tester opting out or uninstalling**. An app whose Fajr alarm silently never
arrives is a reason to uninstall. So leaving the defective build in place to "protect" the
fortnight may be the higher-risk choice: it protects against an undocumented risk by
increasing a documented one.

**Recommendation: upload the fix.** See §7 for the order.

---

## 4. What is verified, and on what

Verification is only worth what it ran on, so this is split by surface rather than presented
as one number.

### Verified on real hardware — two makes, two manufacturers

| Finding | Evidence |
|---|---|
| Exact alarms work when the permission is granted | `flags=0x3 window=0` on both Redmi Note 13 Pro (HyperOS/Android 16) and Galaxy S23 Ultra |
| The one-hour window is **Android's, not Xiaomi's** | Revoking the permission on the Samsung reproduced `flags=0x20 window=+1h` immediately |
| HyperOS additionally defers, far worse than an hour | Overnight A/B: fixed build fired 02:51:00.037; shipped build never fired, parked ~3 days by `power_pending` |
| Alarm stays silent on a silenced phone (T6) | Alert posted 801 ms after target, channel `prayer_times`, `sound=null vibrate=null`, `playing=0` |
| The onboarding exact-alarm prompt works end to end | Walked through the real UI on the Samsung |
| Times, Qibla, settings, PDF, rotation, 1.5× text, light/dark | Full manual walk on the Samsung; no crashes |

### Verified on emulator only

- The method-choice banner (T1): all five paths — shown at 51°N on AUTO, absent at Makkah,
  absent once a method is chosen, tap opens Settings, dismiss persists across a restart.
- The town-not-county fix (T3) and the composite place name.
- The PDF approximation note, by exporting a real Longyearbyen month and rendering it.
- Right-to-left, dark mode and 1.5× text together.

### Verified by test, not by eye

- 103 unit tests across `app`, `core` and `wear`, 0 failures.
- Bidi ordering asserted against `java.text.Bidi`, the real Unicode algorithm — including a
  test that pins the *wrong* fix so it cannot be "simplified" back in.
- `NoTranslationsYetTest` fails the build if a machine translation ever appears. Proven by
  adding `values-ar/`, watching it go red, removing it, watching it go green.

### Not verified — stated plainly

- **None of today's UI work has been seen on a physical phone.** The RTL build will not
  install on the Xiaomi at all (`INSTALL_FAILED_USER_RESTRICTED`, the HyperOS "Install via
  USB" gate) and the Xiaomi was held for an alarm measurement.
- **Cold start has never been measured on real hardware for the release build.** The only
  figure is 13.3 s for a debug build on a struggling emulator, which is not evidence.
- **One ANR was seen once**, during automated emulator onboarding: main thread in
  `Canvas.drawPath`, every frame interpreted, i.e. consistent with un-JITted cold-start
  drawing under uiautomator load rather than a block in app code. Did not reproduce on a clean
  launch and has never occurred on either physical phone. Unconfirmed either way.
- The notification and the watch tile still carry **no** marking when times are approximated.
- Wear OS has not been re-verified since today's changes (none of them touch `:wear`).

---

## 5. Risk register

| # | Risk | Severity | State |
|---|---|---|---|
| 1 | Testers are on the build with the alarm defect | **High** | Fix built and verified, not uploaded. §7 |
| 2 | Opt-in count may have slipped below 12 without notice | **High** | Play stops displaying the number once 12 is passed, and gives no alert if it drops. Unobservable |
| 3 | Store screenshots no longer match the app | Medium | Deliberate. Must be retaken **at upload**, not before — `RELEASING.md` |
| 4 | Moonsighting users between 60° and 66° get Fajr up to 91 min out | Medium | Real, bounded, affects a small population. Fix attempted and reverted — see §6 |
| 5 | Users who decline the exact-alarm permission still get late alerts | Medium | Mitigated: onboarding prompt, dismissible home banner, settings row, and copy that now names the real cost |
| 6 | Cold start unmeasured on hardware | Low–Medium | No baseline exists. Cheap to close |
| 7 | The emulator ANR is real after all | Low | Never seen on hardware; profile is consistent with emulator cold start |

**Nothing here is a launch blocker.** #1 and #2 are the two that deserve action this week.

---

## 6. The thing I tried to fix and deliberately did not

Moonsighting Committee publish that above 60° they take **Fajr and Isha** from 60°. The app
does not, and the cost was measured: Luleå is **91 minutes** out on 21 June, with only 38
minutes of night.

It was implemented — fifteen lines — and then swept: **1,043 of 13,140 day-computations came
out non-monotonic.** Luleå on 21 June with the fix applied put **Isha 86 minutes before
Maghrib**. Near the solstice at 63° the same crossing appears as a one-minute inversion, which
would have shipped looking fine.

It was reverted, for three reasons in order of weight:

1. A wrong-but-ordered day beats an incoherent one. 91 minutes out is bad; a timetable where
   Isha precedes Maghrib is unusable.
2. It could not be checked against Moonsighting's own published output — their JSON endpoint
   returns 500 and their HTML page builds its table in JavaScript.
3. A literal reading of their sentence contradicts itself, so they are doing something the
   prose does not state. Guessing what, inside prayer-time code, days before a production
   submission, is the exact move this project's rules exist to prevent.

**This is now blocked on evidence, not effort**: one Moonsighting timetable for a place
between 60° and 66° in June would settle it.

---

## 7. What to do, in order

1. **Check the opt-in count is still ≥ 12.** It is the only thing that can silently cost the
   fortnight, and Play will not tell you. Ask the testers directly if there is no other way.
2. **Upload the fixed build to the closed track** (§3 — the requirement tracks opt-in, not
   builds; and the defect it fixes is itself a reason to uninstall). Bump `versionCode` to 3.
   Retake the store screenshots **as part of that same change**, in the order build → capture
   → upload.
3. **Tell the testers a fix has landed and to update.** This is the one action that addresses
   the community concern in §3 directly.
4. **When the 14 days complete, apply for production.** Expect a further review, days rather
   than minutes.
5. **Wear OS second release only after the phone app reaches production**, unchanged.

---

## 8. Decisions that are the owner's, not an agent's

| | Decision | Recommendation |
|---|---|---|
| **A14** | Switch to `USE_EXACT_ALARM`, which removes the permission prompt entirely — Muslim Pro does exactly this on the owner's own phone | **Not before production.** Google admits only "an alarm or timer app" or "a calendar app"; apps that miss it are *disallowed from publishing*. Another app passing review is evidence it can pass, not proof we would. Revisit once live, when a rejection costs a version rather than the launch |
| **A1** | Stop deriving calculation method from sect | Open |
| **A10** | Should a projected time fire an alarm at all? | Open — a genuine religious question, not an engineering one |
| **A11** | Ramadan wording for Fajr | Open |

---

*Made with love, free for the Ummah.*
