# Second-opinion prompt, round 2 — the polar rule, and where the next crash is

*Paste everything below the line into Codex / DeepSeek / another model. It is self-contained.*

---

You are reviewing a decision that has already shipped in a real Android app. I want you to try
to knock it down. Do not validate it because it sounds reasonable — if the reasoning is wrong,
or a better option was missed, say so plainly and say what you would do instead.

## The app

**SajdaTime** — a free, ad-free, privacy-first Muslim prayer times and Qibla app for Android
and Wear OS, built as an ongoing charity (*sadaqah jariyah*). No ads, no accounts, no
analytics, no server, no revenue, and there never will be. Currently in Google Play closed
testing.

Fixed constraints. Treat these as given, not as things to optimise:

- **No network at runtime, ever.** Times are computed on-device with `adhan-java` 1.2.1. The
  app must work on a phone with no signal. (Aladhan is used during development as an
  independent reference to check the maths. That is fine and encouraged.)
- **Coarse location only**, foreground only, never transmitted. `ACCESS_FINE_LOCATION` is out.
- **It must work for the masses** — old devices, users who never change a setting, users who
  cannot read English. Where elegant and dependable conflict, dependable wins.
- **The app must never take a religious position on the user's behalf**, and its disclaimer
  says it is not a religious authority.
- The developer is not a scholar of fiqh, and the app says so.

## What was found and fixed today

Every test in this project had been run at 51.5°N, because that is where the person reporting
problems lives. Sweeping latitude from −90 to +90 instead found a crash present in every
release shipped so far.

**Mechanism.** Beyond the polar circles the sun can fail to set at all in summer, or to rise at
all in winter. `adhan` then returns `null` for **every** field — not only Fajr and Isha but
Dhuhr, Asr, sunrise and Maghrib, because it derives them all from a sunrise and sunset that do
not exist. The engine passed those into a non-null parameter and threw `NullPointerException`.

**Measured boundary** (MWL, sweeping in 0.5° steps):

| Date | Last latitude that computes | First that returns null |
|---|---|---|
| 21 June | 65.5°N | 66.0°N |
| 21 December | 67.0°N | 68.0°N |

**Blast radius.** Every caller at once: the phone view-model, the Wear view-model, the Wear
tile service, the ongoing notification, the PDF exporter, and the background alarm scheduler.
Both solstices. Tromsø, Kiruna, Rovaniemi, Luleå, Murmansk, Norilsk and Longyearbyen all have
Muslim communities; Norilsk has thousands of Central Asian workers.

**What a mature reference does.** Aladhan does not crash. For Tromsø on 21 June it returns
`Fajr = Sunrise = Maghrib = Isha = 00:46` — degenerate, but it renders.

**The fix as shipped.** Try the user's real coordinates first. Only if any field comes back
null, recompute at **60° latitude**, keeping the user's longitude and the sign of their
latitude, so solar noon stays true to where they are. The day is flagged `approximated` and the
home screen shows a notice that cannot be dismissed. Rationale: this is *aqrab al-bilad* — the
times of the nearest place where night and day are still distinguishable — and 60° is the
figure Moonsighting Committee publish for exactly this case, so it was taken from them rather
than invented.

**Rejected, with reasons:** nullable times (nullability ripples through every screen, the PDF,
the watch and the scheduler, to produce a blank screen at the end of it); reproducing Aladhan's
degenerate collapse (six identical times are worse than honest approximations); *aqrab
al-ayyam* / nearest-day (defensible, but needs a search over dates where a latitude clamp needs
none).

## Question 1 — is this the right rule?

Attack it specifically:

1. **Is 60° the right figure**, or is it Moonsighting's figure being borrowed for a situation
   they did not mean it for? Would 45°, 48° or "the highest latitude that still computes on
   this date" be better? Note the boundary moves with the season, so a fixed clamp is
   conservative in December and exact in June.
2. **Is *aqrab al-bilad* the appropriate convention here at all**, or is *aqrab al-ayyam*
   (nearest day) the more widely accepted answer for the midnight-sun case specifically? Who
   holds each position?
3. **What do Islamic bodies in Norway, Sweden, Finland and Russia actually publish** for their
   own communities? Is there a Nordic consensus, and does it match either of the above? This is
   the part I most want sourced rather than reasoned.
4. **Should Dhuhr be projected at all?** Solar noon is perfectly computable at 78°N — it is
   only the library that discards it along with everything else. Projecting Dhuhr to 60° shifts
   it by a few minutes for no astronomical reason. Is keeping true local Dhuhr while projecting
   only Fajr/Isha more correct, or is a mixed day worse than a consistent one?
5. **Is showing approximated times the right call at all**, versus showing nothing and telling
   the user to ask a local scholar? Consider that this app also fires alarms, and an alarm
   derived from a projection is a stronger claim than a number on a screen.

## Question 2 — where is the next crash?

This is the more valuable question. One blind spot was found by sweeping a parameter that had
only ever been sampled. **What parameter is still only being sampled?**

Already swept, so do not suggest these:

- Latitude −90 to +90, both solstices, all 14 calculation methods.
- Both hemispheres, including the Moonsighting 55° clamp asymmetry (raw latitude, not `abs`).
- All four 2026 DST transitions in Europe/London and Australia/Melbourne.
- Ramadan behaviour for the fixed-interval method (Umm al-Qura, +30 min, via `HijrahChronology`).
- Midnight rollover, and Isha falling after midnight at high latitude.

Not yet swept, and I want your judgement on which of these actually breaks something versus
which is theatre:

- **Longitude**, including ±180 and the antimeridian, and the date-line cases where a prayer's
  calendar day and the device's calendar day disagree.
- **Half-hour and quarter-hour time zones** — Nepal (+5:45), Chatham (+12:45), Newfoundland
  (−3:30), and the +14 / −12 extremes.
- **Countries that abolished DST mid-year**, and devices with stale `tzdata`.
- **Dates far from now** — leap years, year 2100, and the Hijri/Gregorian conversion at the
  edges of `HijrahChronology`'s supported range.
- **Latitude/longitude exactly 0**, and the Qibla calculation when the user is *at* the Kaaba
  or exactly antipodal to it.
- **A phone and a watch in different time zones**, which this app genuinely permits.

For each: say whether you think it fails, what the failure looks like, and how you would test
it in a unit test with no device. Rank them. I would rather have three that genuinely break
than twelve that theoretically might.

## Question 3 — the finding neither of you reached last time

Measured across the world, the app's default (Muslim World League, Fajr 18°) is **later** on
Fajr than the local official method almost everywhere, and earlier essentially nowhere:
Indonesia, Malaysia and Singapore +8 to +9 minutes, Egypt +7 to +10, Morocco +4 to +7, Saudi
+3, Turkey and Pakistan 0, Iran −1.

Being *late* on Fajr is the dangerous direction during Ramadan — the user believes they may
still eat when their national timetable says the fast has begun.

1. **Is 8–9 minutes materially wrong**, or within the tolerance any timetable carries anyway?
2. What do Indonesian and Malaysian scholars say about a Muslim there following an 18°
   convention rather than their national 20°?
3. Given the app cannot pick a national method without taking a position, **is a systematically
   late Fajr an acceptable default** — or is erring early the only defensible direction for a
   default, since it is the cautious one for fasting?

## How to answer

Be concrete and rank by severity. Where you assert a fact about a fiqh position, a national
authority, or a library's behaviour, **say how confident you are and on what basis, and mark
clearly what you are inferring rather than recalling.** Three well-sourced points beat ten
confident guesses. If you think the shipped fix is right, say so and say why — but only after
trying to break it.
