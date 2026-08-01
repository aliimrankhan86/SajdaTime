# Polar and high-latitude prayer times — the sources, and what each one actually says

**Compiled 1 Aug 2026** while closing `HANDOVER.md` §11 item **A9**. This file exists so the
next session can re-check the reasoning without re-doing the search, and so a claim can be
traced to who made it rather than to whoever repeated it.

> **Read this first.** Nothing here is a fatwa and this project does not issue one. The app
> implements published rules and tells the user which one it used. Where bodies disagree — and
> on this question they genuinely do — the app's job is to name the disagreement, not settle it.
>
> **Scope note, on the owner's instruction:** no Ahmadiyya sources are cited or relied on
> anywhere in this project. Every body below is mainstream Sunni or Twelver Shia.

---

## 1. The classical principle everyone agrees on

*Aqrab al-bilād* — the nearest locality where night and day are still distinguishable — is
common ground across every body checked, Sunni and Shia:

| Body | What it says | Source |
|---|---|---|
| Islamic Fiqh Council, Muslim World League | Estimate by analogy with the nearest place where the signs are clear | 9th session, Rajab 1406 |
| European Council for Fatwa and Research | Endorses the above | Resolution 6 |
| AMJA (Assembly of Muslim Jurists of America) | *"the location nearest to them in which night can be distinguished from day"*; explicitly calls the matter *ijtihād* and defers to local authorities | [amjaonline.org](https://www.amjaonline.org/fatwa/en/21730/polar-prayer-times-fasting) |
| Sayyid al-Sistani (Twelver Shia) | *"rely on the timings of the closest city that has night and day in a twenty-four hour period"* | [sistani.org](https://www.sistani.org/english/book/46/2032/) |
| Moonsighting Committee | Uses the term explicitly — *"based on 'Aqrabul-Bilad' concept"* | [moonsighting.com](https://moonsighting.com/how-we.html) |

**What they do not agree on is which locality.** That is the whole of A9.

---

## 2. The Islamic Fiqh Council resolution — 45°

**Islamic Fiqh Council of the Muslim World League**, resolution 6, ninth session, held at the
Muslim World League building in Makkah, 12–19 Rajab 1406 (March 1986). Endorsed by the
**European Council for Fatwa and Research**.

Three bands:

| Band | Rule |
|---|---|
| 45°–48° N/S | Signs are visible throughout the 24 hours; observe the real times |
| 48°–66° N/S | **Fajr and Isha** by analogy with the nearest place where they are clear |
| Beyond 66° | **All** times estimated from **45°** latitude |

Reported consistently by:

- [fiqh.islamonline.net](https://fiqh.islamonline.net/en/praying-and-fasting-at-high-latitudes/)
- [islamicfiqh.net](https://islamicfiqh.net/en/articles/prayer-in-polar-areas-267)
- [`hablullah/go-prayer`](https://pkg.go.dev/github.com/hablullah/go-prayer), which implements
  it as `NearestLatitude()` / `NearestLatitudeAsIs()` and cites IslamOnline

**Why we believe it despite not having the Arabic original in hand.** Secondary sources
copying each other prove nothing. The band edges do:

| Latitude | Max solar depression at the June solstice | Meaning |
|---|---|---|
| 48.56° | **exactly 18.00°** | the last latitude where 18° twilight occurs |
| 66.56° | **exactly 0.00°** | the polar circle |

Both edges land on astronomy to two decimal places. `latitude + declination − 90` gives the
sun's altitude at local solar midnight; with δ = 23.44° that is 18° of depression at 48.56° and
zero at 66.56°. A garbled retelling does not land on both. The bands are real.

**Related, and not the same thing:** the Council's **21st session** (Muslim World League,
2012) revisited the 48°–66° band and permitted proportional estimation (*al-taqdīr al-nisbī*)
and the combining of Maghrib and Isha *as a concession for hardship, not as a rule for
everyone*. Primary text on the MWL's own site: [ar.themwl.org/node/48](https://ar.themwl.org/node/48).
That band is not where this app projects, so it does not change the engine — but it is the
reason not to describe the Council as having one flat position.

---

## 3. Moonsighting Committee — 60°, in their own words

From [moonsighting.com/how-we.html](https://moonsighting.com/how-we.html), retrieved and read
verbatim rather than via a summary:

> "Now take Oslo (latitude = about 60degrees) and using the rule of Sab'u Lail, we calculate
> the longest day to be 19 hours 38 minutes and the shortest day to be 7 hours and 43 minutes.
> Of course, we are beyond the 18 hour limit fixed by the Fatwa, but since the inhabitants of
> Oslo seem to admit to these timings without difficulty, we will retain 60degrees as the
> latitude based on 'Aqrabul-Bilad' concept. Therefore, at latitudes more than 60degrees, we
> slide down to 60degrees and calculate Fajr & Isha using the rule of Sab'u Lail in summer."

Three things follow, and all three matter:

1. **60° is deliberate and empirical**, not derived. They chose it because Oslo copes, and they
   say plainly that it breaches the 18-hour limit of the fatwa they otherwise rely on.
2. **It covers Fajr and Isha only.** Above 66° something still has to be done about the other
   four, and Moonsighting do not say what.
3. They explicitly **reject** the naive version of *aqrab al-bilād*: using the nearest latitude
   that still has distinguishable signs *"still gives fasting times of more than 23 hours in
   summer and less than 3 hours in winter"*. That is the same proposal one round-2 reviewer
   made, refuted by the people who use the concept.

**Verified, not assumed.** Their stated Oslo consequence — 19h38m and 7h43m — is reproduced by
this engine at 60° as **19h39m and 7h41m**. `PolarAndHemisphereTest` pins it.

---

## 4. Other positions, recorded so they are not rediscovered as novel

- **Makkah timing.** Dar al-Iftā' al-Miṣriyyah fatwa 2806 (8 Aug 2010), used by
  [prayertimes.dk](https://www.prayertimes.dk/) for Norway with a linear transition into and
  out of the "extreme period". Rejected for this app because it discards the user's own solar
  noon — Dhuhr would stop matching the sun overhead — but it is a real position held by real
  mosques, and would be legitimate as an explicit user choice.
- **Nearest day (*aqrab al-ayyām*).** Use the last day on which the signs were distinguishable.
  Implemented as `NearestDay()` in `go-prayer`; reported in use in Oulu, Finland.
- **A 64° threshold** agreed at a conference of jurists and astronomers in Istanbul, per
  [IslamOnline](https://fiqh.islamonline.net/en/prayer-and-fasting-in-the-two-poles/).
- **12° solar depression** as an alternative for Fajr and Isha — the Fiqh Council's ninth
  session itself said there was *"no harm"* in relying on other committees' estimations,
  naming this one.

---

## 5. Claims checked and found wrong or unverified

- **"The Islamic Council of Norway uses nearest-day and fixes Tromsø at Fajr 03:00 / Isha
  23:00."** Asserted at "high confidence" by one round-2 reviewer. **Not verified**, and
  contradicted by the other reviewer, who documented no Nordic consensus at all. Do not repeat.
- **"Malaysia uses 20° for Fajr."** False since November 2019. See `HANDOVER.md` §10,
  "Aladhan was stale and I shipped it". This is the reason for the rule at the top of this file.
- **"Projecting shifts Dhuhr."** False — measured at exactly zero, because solar transit does
  not depend on latitude.
- **"The projection can override a sunset the user could watch."** False. Measured across five
  Arctic locations: adhan's failures occur *exactly* at apparent polar day or night, so there
  is never an observable sunrise or sunset to override.
- **api.sunrise-sunset.org disagrees with this engine by 2–7 minutes.** True, and it is the
  outlier: Aladhan, an independent implementation, agrees with this engine to the minute. The
  service uses a low-precision 1990 almanac algorithm. **Do not "fix" the engine towards it.**

---

## 6. What the app does with all of this

See `HANDOVER.md` §5.5a for the rule and §10 for the measurements. In one line: **the reference
latitude follows the method the user chose — 60° for Moonsighting, 45° for everything else —
and the screen prints the number so it can be checked.**

---

*Made with love, free for the Ummah.*
