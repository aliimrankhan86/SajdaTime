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

**Then today's Dhuhr was watched on both, and the live build did not merely run late.** At
13:13, three minutes after the prayer fell:

```
requester=-3m6s606ms  …  power_pending=+2d23h56m53s394ms
whenElapsed=+2d23h56m53s394ms   maxWhenElapsed=+2d23h56m53s394ms
```

Every other policy is in the past, so nothing else is holding it. HyperOS's `power_pending`
alone overwrote both bounds and pushed today's Dhuhr out by nearly three days.

**It did not fire then either.** At 13:22:39 the app ran `DailyRescheduleWorker` — one of the
five reschedule triggers that exist to make alerts *more* reliable — rewrote its alarm set and
**cancelled the parked alarm in the process**. Checked afterwards: no alarm for 13:10 remains,
and the only notification the live build has posted all day is the silent ongoing badge. **No
`prayer_times` alert. No Dhuhr notification. At any point.** The fixed build, same phone, same
target, posted "Time for Dhuhr" at **13:10:00.600**.

So the failure mode is not deferral, it is **silent loss**, and the mechanism is worth stating
because neither half causes it alone: OEM parking would have produced a very late alert; the
app's own reliability worker then tidied the stale alarm away without ever delivering it.

This also corrects an inference made this morning. At 11:21 `power_pending` read `--` on both
builds, and that was written up as "in daytime the damage is only the one-hour window". The
observation was right and the conclusion was wrong: `power_pending` **re-engaged during the
afternoon**, on a phone that was plugged in, unlocked and in active use, before the alarm was
due. So the one-hour window is the **floor**, not the expected damage, and the multi-day
parking is not an overnight-only phenomenon.

**The plain statement of the defect, then, is not "alerts can be late". It is that a tester on
a Xiaomi phone can have a prayer silently skipped altogether, at any time of day.** The same
was already measured overnight: an identical 02:51 Fajr fired at 02:51:00.037 on the fixed
build and never fired on the shipped one.

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

**Re-checked against the source on 2 August, because the whole plan hangs on it.** The Play
Console help page was re-read end to end looking specifically for anything about builds,
releases or version codes during the window. There is nothing. The page describes recruiting
testers, engaging with them, gathering feedback and acting on it; the only thing it names as
breaking continuity is a tester opting out. The claim that uploading resets the clock does not
appear in Google's documentation at all.

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

- **129 unit tests** across `app` (41), `core` (76) and `wear` (12), 0 failures. 0 lint errors.
  Both bundles built and signed with the owner's key (`META-INF/SAJDATIM.RSA`).
- `DisclaimerContentTest` now guards the disclaimer itself — that it still makes all four of
  its points, that the dua request is still the last paragraph and appears once, and that the
  watch, the privacy page, the front page and the Play listing have not drifted from it.
  Proven to fail: softening "no warranty" in `docs/privacy.html` turns it red.
- Bidi ordering asserted against `java.text.Bidi`, the real Unicode algorithm — including a
  test that pins the *wrong* fix so it cannot be "simplified" back in.
- `NoTranslationsYetTest` fails the build if a machine translation ever appears. Proven by
  adding `values-ar/`, watching it go red, removing it, watching it go green.

### Not verified — stated plainly

- **None of today's UI work has been seen on a physical phone.** The RTL build will not
  install on the Xiaomi at all (`INSTALL_FAILED_USER_RESTRICTED`, the HyperOS "Install via
  USB" gate) and the Xiaomi was held for an alarm measurement.
- ~~Cold start has never been measured on real hardware.~~ **Measured 2 Aug on the Redmi
  Note 13 Pro: 631 ms, 244 ms, 224 ms, all `LaunchState: COLD`.** The 13.3 s emulator figure
  was wrong by a factor of about fifty. See HANDOVER §10.
- **One ANR was seen once**, during automated emulator onboarding: main thread in
  `Canvas.drawPath`, every frame interpreted, i.e. consistent with un-JITted cold-start
  drawing under uiautomator load rather than a block in app code. Did not reproduce on a clean
  launch and has never occurred on either physical phone. Unconfirmed either way.
- ~~The notification and the watch tile carry no marking when times are approximated.~~
  **Both mark it now** (2 Aug). Whether a projected time should fire an alarm at all is still
  open — that is A10, and it is a religious question.
- ~~Wear OS has not been re-verified since today's changes (none of them touch `:wear`).~~
  **Re-verified 2 Aug** after the disclaimer reword, which does touch `:wear`:
  `./tools/wear-verify.sh`, 24 captures across both round sizes and both font scales, 24/24
  pass. It caught a regression the phone-side verification could not have: the reworded string
  wrapped to a fourth line and the watch face clock printed through it. Fixed and re-measured.
  ~~Still **emulator only** — there is no physical watch.~~ **Superseded 31 Aug / 1 Sept 2026.**
  The owner does have a watch. His paired watch and his phone were shown to agree on Asr on
  31 Aug, and he checked the watch himself, Qibla included, on 1 Sept and reported it fine.
  **Watch testing is closed — do not raise it.**

---

## 5. Risk register

| # | Risk | Severity | State |
|---|---|---|---|
| 1 | Testers are on a build that can **silently skip a prayer entirely** on Xiaomi phones, at any time of day | **High** | Fix built and verified, not uploaded. §7. Measured twice: Fajr overnight, Dhuhr in the afternoon |
| 2 | Opt-in count may have slipped below 12 without notice | **High** | Play stops displaying the number once 12 is passed, and gives no alert if it drops. Unobservable |
| 3 | Store screenshots no longer match the app | Medium | Deliberate. Must be retaken **at upload**, not before — `RELEASING.md` |
| 4 | ~~Moonsighting users between 60° and 66° get Fajr up to 91 min out~~ | **Closed** | **Not a risk — the opposite.** Their own published timetable for Luleå, Trondheim and Helsinki matches this engine to within a minute; the 91 minutes measured the damage the reverted *fix* would have done. Twelve of their rows are now golden values. HANDOVER §10 |
| 5 | Users who decline the exact-alarm permission still get late alerts | Medium | Mitigated: onboarding prompt, dismissible home banner, settings row, and copy that now names the real cost |
| 6 | ~~Cold start unmeasured on hardware~~ | **Closed** | Measured 2 Aug: ~224 ms cold on the Redmi. HANDOVER §10 |
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

**Re-read on 2 August, and the earlier reading was missing a guard clause.** Moonsighting's
own page states the rule conditionally, not absolutely: *"Any location where the duration of
fasting exceeds 18 hours or is less than 6 hours should refer itself to the times valid for
the closest 'balanced' location"*, and the slide applies to *"Fajr & Isha … in summer"* only —
*"In winter, we use research by Moonsighting.com for Subh-Sadiq and Shafaq."* That does not
rescue the reverted implementation: Luleå in late June satisfies the condition, so the slide
fires there and the inversion is real. It does mean the contradiction was partly in the
reading. See HANDOVER §15 lesson 78.

> **⚠️ SUPERSEDED 2 Aug 2026 — settled, and settled in favour of changing nothing.** This
> paragraph used to read *"still blocked on evidence, not effort: one Moonsighting timetable for
> a place between 60° and 66° in June would settle it."* That timetable was obtainable all
> along — their page builds its table in JavaScript, so `curl` saw nothing and a browser saw
> everything. **They publish the un-slid numbers and this engine already reproduces them**
> across Luleå, Trondheim and Helsinki, summer and winter, to within one minute of rounding.
> There is no slide to implement. Twelve of their rows are pinned in `PolarAndHemisphereTest`
> so it cannot be re-implemented silently. HANDOVER §10, "A12 settled".

**But the user-facing problem it represents is now addressable without settling it.** The
"Match your mosque" corrections added on 2 August let a user at any latitude match their own
mosque's printed times exactly, whatever the cause of the divergence. That is a better answer
than a confident wrong one, and it is the same answer for every other source of disagreement.

---

## 7. What to do, in order

1. **Check the opt-in count is still ≥ 12.** It is the only thing that can silently cost the
   fortnight, and Play will not tell you. Ask the testers directly if there is no other way.
2. **Upload the fixed build to the closed track** (§3 — the requirement tracks opt-in, not
   builds; and the defect it fixes is itself a reason to uninstall). ~~Bump `versionCode`
   to 3.~~ **Done, 2 Aug 2026: the repo is now `versionCode` 3 / `versionName` 1.1.1**, on
   both modules' `versionName` so one release never shows a user two version numbers. The
   watch's `versionCode` stays in its own 1000+ lane.
   Retake the store screenshots **as part of that same change**, in the order build → capture
   → upload. Screenshots are still outstanding and belong at upload time, not before — see
   `docs/RELEASING.md`.
3. **Tell the testers a fix has landed and to update.** This is the one action that addresses
   the community concern in §3 directly.
4. **When the 14 days complete, apply for production.** Expect a further review, days rather
   than minutes.
5. **Wear OS second release only after the phone app reaches production**, unchanged.

---

## 8. Decisions that are the owner's, not an agent's

| | Decision | Recommendation |
|---|---|---|
| **A14** | Switch to `USE_EXACT_ALARM`, which removes the permission prompt entirely — Muslim Pro does exactly this on the owner's own phone | **Not before production — re-verified against the policy page on 2 Aug, not assumed.** Google names exactly two qualifying categories, quoted: *"The app is an alarm or timer app"* and *"The app is a calendar app that shows event notifications."* A prayer-times app is neither, and the stated consequence for apps outside them is that they *"will be disallowed from publishing on Google Play."* Google's own remedy for anything else is the permission the app already uses: *"you should evaluate if using `SCHEDULE_EXACT_ALARM` as an alternative is an option."* Muslim Pro passing review is evidence it can pass, not proof we would. Revisit once live, when a rejection costs a version rather than the launch |
| **A15** | What the disclaimer has to say, given the owner does not want to be held accountable | **DECIDED and BUILT, 2 Aug, in the owner's own words.** The wording now establishes four things rather than the previous two: that the times are a calculation run on the user's own phone and not a timetable from any authority; how that calculation can fail; that **a disagreement with the mosque is settled in the mosque's favour** (this replaced *"It is your choice to make"*); and that the app is free and given as it is, with no warranty. Published in four places that must be changed together — the in-app dialog, the watch, `docs/privacy.html#disclaimer` and the Play listing. Rule and copy table in HANDOVER §5.15 |
| **A16** | The app must follow the phone's language, and must not go right-to-left before it has right-to-left words | **VERIFIED and GUARDED, 2 Aug**, on the owner's instruction. The mechanism was already correct — `AppLocale` reads the app's language out of the resources, so a reviewed `values-ar/` would flip language, digits and direction with no code change — but it had never been run on a phone that was actually set to Arabic, only on the `rtl` preview build, which is a different code path. Both emulators were rebooted into `ar-EG` (`ldrtl`): the shipped build stayed English and left-to-right on eleven surfaces, **including through a rotation and a dark-mode toggle**, which is where an `attachBaseContext` pin normally comes undone. Two new tests in `LocaleDisciplineTest` fail the build if the shipped tag names an RTL language, or names any language the app has no words in. HANDOVER §5.16 and §10 |
| **A8** | Should the watch explain that far-north times are approximate? | **CLOSED 2 Aug.** Not a judgement call once it was looked at: the watch *app* was not marking projected times at all, only the tile was, which breaks `DayPrayerTimes`' own contract that the UI must say so. One line now sits under the countdown, above the times. Verified on both round sizes, both font scales and RTL; the trigger was forced in a throwaway build because the watch emulator would not take a location, and that is stated rather than glossed. HANDOVER §10 |
| **A12** | Does Moonsighting slide Fajr and Isha down to 60° above 60°N? | **CLOSED 2 Aug — no, and the app was already right.** Parked for a week as "blocked on evidence" because curl could not read their JavaScript-rendered timetable; a browser could. Twelve published rows across Lulea, Trondheim and Helsinki match this engine to within one minute of rounding, so the slide that was built and reverted would have introduced up to 91 minutes of error. All twelve are now golden values. HANDOVER §10 |
| **A18** | Does an alarm-style alert stay quiet on a projected day? | **VERIFIED ON DEVICE 2 Aug, with a control.** Longyearbyen, Isha set to Alarm: posted on `prayer_times`, `category=reminder`, no sound. Same phone and setting at Slough: `prayer_alarm_v0`, `category=alarm`, `USAGE_ALARM`. Emulator and a clock jump, not the Xiaomi and not a real overnight wait. HANDOVER §10 |
| **A10** | Should a projected time fire an alarm at all? | **DECIDED and BUILT, 2 Aug.** No — it posts quietly and marked, with a switch to turn ringing back on, offered only to users whose location actually produces such days. The switch is not optional garnish: without it a Tromsø user's Fajr alarm silently stops working for two months a year. Rule in HANDOVER §5.14, device evidence in §10 |
| **A1** | Stop deriving calculation method from sect | **Deferred.** Still right on the logic, but the symptom is now covered by the latitude banner and by "Match your mosque"; what is left costs every user an onboarding question to settle an internal tidiness point |
| **A11** | Ramadan wording for Fajr | **Deferred by the owner** — suhoor and imsak are a fasting question, and the app is deliberately staying on salah for now. Revisit before the app's first live Ramadan |
| **A3** | Show the active method on the home screen | **Deferred — largely superseded** by the latitude banner, which asks the question in a sentence rather than an abbreviation |
| **A4** | `HighLatitudeRule` as a user setting | **Deferred.** The most jargon-heavy setting the app could ship, and "Match your mosque" now reaches the same outcome without requiring an opinion about the rule |
| **A6** | Add Morocco and a Wifaqul Ulama profile | **Deferred on evidence.** Morocco is addable whenever wanted; Wifaqul is not, until someone reads the authority's own published document rather than a reviewer's summary |

---

*Made with love, free for the Ummah.*
