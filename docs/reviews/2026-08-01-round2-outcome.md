# Round 2 outcome — what the second review changed

Two models answered [`2026-08-01-prompt-round2.md`](2026-08-01-prompt-round2.md). Every
checkable claim was tested against adhan's bytecode, the running engine, or a primary source.

> The replies themselves are not stored here in full because the material conclusions are all
> below with their evidence. Where a claim was *not* checked, that is stated explicitly.

## The one that mattered: I had shipped a wrong label

**Claim (Codex): Malaysia no longer uses 20° for Fajr; it moved to 18° in 2019, so a +8/+9
minute gap against MWL indicates a stale source.**

**CONFIRMED, and it was my error.** Malaysia's MKI Coordination Committee agreed the 18°
criterion on 20–21 November 2019, after a year of JAKIM research with Universiti Malaya,
UniSZA and UiTM. Subuh became ~8 minutes later.

Aladhan's own `/v1/methods` endpoint still returns
`id=17 Jabatan Kemajuan Islam Malaysia (JAKIM) params={'Fajr': 20, 'Isha': 18}` — stale by
nearly seven years. I had verified against it, got a perfect match, and shipped a label naming
Malaysia. Fixed in the same session. Full write-up in `../HANDOVER.md` §10, "Aladhan was stale
and I shipped it", and lesson 45a.

**Singapore, re-checked against MUIS's own 2024 timetable rather than an aggregator:**

| Date | MUIS printed Subuh | 20° | 18° |
|---|---|---|---|
| 1 Jan 2024 | 5:44 | **05:43** | 05:52 |
| 21 Mar 2024 | 5:52 | **05:52** | 06:00 |
| 21 Jun 2024 | 5:37 | **05:36** | 05:45 |
| 23 Sep 2024 | 5:37 | **05:37** | 05:45 |

Singapore is genuinely 20°/18°. Isyak matches 18° exactly on all four dates.

## Confirmed by measurement

**Dhuhr does not depend on latitude** (Codex). Decisively true. `SolarTime` derives transit
from date, longitude and solar right ascension; latitude is not an input. Measured at
latitudes 0°, 30°, 50°, 60° and 65° across four longitudes and both solstices: **Dhuhr
identical in every case.**

This matters because it means **the 60° polar projection does not distort Dhuhr at all.**
Measured, projecting from 64° and 65° down to 60°:

| Prayer | Shift caused by projecting |
|---|---|
| **Dhuhr** | **0 min, always** |
| Fajr | up to 38 min |
| Asr | up to 38 min |
| Isha | up to 42 min |
| Sunrise / Maghrib | up to 95 min |

So DeepSeek's assertion that projecting "shifts Dhuhr by a few minutes for no astronomical
reason" is **false** — it shifts it by exactly zero. Codex predicted this correctly and used
it to argue for per-event provenance, which is the stronger version of the argument: Asr is
genuinely computable in polar summer (the sun is above the horizon all day, so a shadow
exists), and adhan discards it only because its constructor treats a missing sunrise as total
failure. Logged as an improvement, not yet built.

**HijrahChronology range** (Codex). Confirmed: `YEAR range: 1300 - 1600` on this runtime.
Gregorian 1800, 1880, 2200 and 2400 all throw `DateTimeException`. **But the app does not
crash** — `PrayerEngine.isRamadan` wraps it in `runCatching{}.getOrDefault(false)` and
`hijriToday` in `HomeScreen` wraps it in `runCatching{}.getOrNull()` and falls back to the
Gregorian date alone. Both were already defensive. `compute()` verified working at 1880, 2100,
2200 and 2400. **Not a defect. No change needed.**

**Qibla singularities** (Codex). Confirmed as described: no exception, but meaningless output.
At the Kaaba exactly the bearing is 268.66°; at the antipode 91.34°; at the north pole
140.17°. Codex predicted "arbitrary unstable arrow, not a crash" and that is exactly right.
Low severity — it cannot crash and nobody standing in the Haram needs a compass — but logged.

## Not confirmed, or contradicted between the two reviews

**The Nordic polar conventions.** The two replies disagree with each other substantially.
DeepSeek asserts the Islamic Council of Norway used nearest-day and fixed Tromsø at Fajr 03:00
/ Isha 23:00, at "high confidence"; Codex says there is **no Nordic consensus** and documents
nearest-day in Oulu, a Malmö-referenced rule in Sweden, 45° in a Russian timetable, and a
documented 2012 northern-Norway agreement to use Makkah. **Neither has been independently
verified here.** Codex's version carries primary-source links and is internally consistent
with its 45° finding; DeepSeek's does not and hedges its own recall. Do not treat either as
settled.

**MWL/ECFR use 45°, not 60°, as the high-latitude reference.** Codex, with a link to the ECFR
reproduction of the MWL resolution. **Plausible and important, not verified here.** If true,
the current 60° projection is Moonsighting's rule applied to every method including MWL's own,
which is the substance of Codex's objection. This is the single most important open item from
round 2 and is now `HANDOVER.md` §11 item **A9**.

**"Show nothing at all above the polar circles"** (DeepSeek). A coherent position — the app
fires alarms, and an alarm off a projection is a stronger claim than a printed number. Codex
argues the opposite: estimates are correct as a default, but must be marked per-prayer and
carried into the watch, tiles, PDF and alarms, with a one-time confirmation before projected
alarms are first scheduled. Codex's position is better suited to an app whose users are mostly
never going to open Settings. Neither is implemented; logged as **A10**.

**Fajr default direction.** DeepSeek argues the default should err early (19.5° or 20°) on
precautionary grounds. Codex argues, more convincingly, that there is no globally cautious
angle because the same timestamp serves two opposite risks — late is dangerous for ending
suhur, early is dangerous for the validity of the Fajr prayer itself. Codex's recommendation
— keep 18°, but during Ramadan say plainly that local timetables may begin Fajr earlier — is
the one carried forward as **A11**.

## Where both were wrong, or overtaken

- DeepSeek's claim that Malaysia's official method is 20° and that MUI/JAKIM would regard 18°
  as a minority position is **outdated**: 18° *is* Malaysia's national criterion. Its point
  about Indonesia stands, with the caveat Codex adds — Muhammadiyah, one of the largest
  Indonesian organisations, moved to 18° after its own review, so "Indonesia" is not one
  number either.
- DeepSeek's Dhuhr-shift claim is false, as measured above.
- DeepSeek proposed "clamp to the highest latitude that still computes". Codex's objection is
  correct and decisive: that boundary is a numerical artefact of the library, moves daily, and
  has no authority behind it. Rejected.
