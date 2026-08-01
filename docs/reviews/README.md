# External reviews

Second opinions obtained from other AI models on specific design questions, stored verbatim.

## ⚠️ How to read anything in this folder

**Nothing here is verified.** These are other models' answers to a prompt, reproduced exactly
as they were given, including the parts that turned out to be wrong. They are stored as
*primary source material*, not as findings.

Whatever was actually confirmed has been moved into
[`../HANDOVER.md`](../HANDOVER.md) §10, together with an explicit list of which claims were
false. **Read that list before acting on anything in this folder.**

The reason the raw text is kept rather than only the verdict:

1. **So the verdict can be audited.** A summary of someone else's argument is not the argument.
   If a future session disagrees with a call made here, it should be able to see what was
   actually said rather than take a previous agent's word for it.
2. **Because some claims were deliberately left unchecked.** `HANDOVER.md` §11 item **A6**
   defers on the Wifaqul Ulama and London Unified timetable comparisons precisely because they
   came from a reviewer and were never independently confirmed. Those figures live only here.
   Without this file, A6 cannot be picked up.
3. **Because being confidently wrong is itself data.** Three of the claims below are false in
   ways that sound authoritative — see lesson 46. Keeping them readable is what makes that
   lesson concrete instead of abstract.

## Contents

| File | What it is |
|---|---|
| `2026-08-01-prompt.md` | The self-contained prompt sent out, with all measured data as it stood on 1 Aug 2026 |
| `2026-08-01-second-opinions.md` | Two replies, verbatim: one from DeepSeek, one from ChatGPT/Codex |
| `2026-08-01-prompt-round2.md` | The follow-up prompt |
| `2026-08-01-round2-outcome.md` | What round 2 changed, claim by claim |
| **`2026-08-01-polar-sources.md`** | **Not a review.** The primary sources on high-latitude prayer times, with what each body actually says and which claims failed checking. **Verified.** |
| **`2026-08-01-android-alarm-facts.md`** | **Not a review either.** What Android and Play actually document about alarm lateness, silent mode, exact-alarm permissions and sheet keyboards. Written from primary sources, then handed to a second pass whose only job was to refute it. **Verified**, including the two claims that did not survive |

## Summary of the outcome

| Claim | Verdict |
|---|---|
| adhan ignores `HighLatitudeRule` for Moonsighting | **Confirmed** — bytecode and numbers |
| `TWILIGHT_ANGLE` is a religious approximation, not a rounding | **Confirmed** — 17/60 = 28% of the night |
| The 55° Moonsighting clamp uses raw, not absolute, latitude | **Confirmed** — southern hemisphere unclamped |
| Fajr had never been measured, and matters more (fasting) | **Confirmed** — and it changed the conclusion |
| Rejecting Kemenag/JAKIM on an Isha-only comparison was unsound | **Confirmed** — 8–9 min apart on Fajr |
| Sect → method is conceptually unsound | **Accepted as argument**, logged as A1, not yet acted on |
| Umm al-Qura is 30 min early during Ramadan | **False** — `PrayerEngine.ishaFor` has always adjusted it |
| DST transitions may be an hour out | **False** — four 2026 transitions verified in both hemispheres |
| adhan's southern seasonal model is northern-centric | **False** — `daysSinceSolstice` branches on the sign of the latitude |
| London Unified is a fixed 12°/12° timetable | **Contradicted between the two reviewers; unverified here** |
| Wifaqul vs London Unified differ by up to 98 min on Fajr | **Unverified** — the reviewer's own calculation, see A6 |

Neither reviewer reached the finding that mattered most: both assumed the Fajr risk was in
Britain, where the default runs cautiously early. It is in Indonesia, where it runs late.

## Round 2, and what it led to

| Claim | Verdict |
|---|---|
| Malaysia no longer uses 20° for Fajr | **Confirmed, and it was our error** — shipped label corrected the same day |
| Dhuhr does not depend on latitude | **Confirmed** — identical at 0°, 30°, 50°, 60°, 65° |
| `HijrahChronology` throws outside 1300–1600 AH | **Confirmed, but already handled** — both call sites guard with `runCatching` |
| Qibla is meaningless at the Kaaba and the poles | **Confirmed** — cannot crash, logged, not fixed |
| 60° is Moonsighting's rule wrongly applied to every method | **Confirmed** — and fixed: see `2026-08-01-polar-sources.md` and A9 |
| MWL/ECFR use 45° | **Confirmed** — resolution 6, 9th session 1406 AH, corroborated against the astronomy of its own band edges |
| …therefore the app should use 45° for everyone | **Rejected** — Moonsighting's own users must keep Moonsighting's own answer |
| "Clamp to the highest latitude that still computes" | **Rejected** — and Moonsighting themselves refute it in print |
| "Show nothing at all above the polar circles" | **Rejected** — logged as the weaker half of A10 |

The round-2 reviewers found a real shipped error and a real design fault. Neither found the
**worst** bug in this area — a non-null but nonsensical Asr, forty-five days out of place. That
came from sweeping the whole input space, not from review. See lesson 50.
