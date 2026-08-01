# Second opinions, 1 August 2026 — verbatim

Two models were asked the questions in [`2026-08-01-prompt.md`](2026-08-01-prompt.md). Both
replies are reproduced below as received. The only change made to either: one East London
Mosque link in Reply B arrived with a mangled redirect wrapper (`%26sa%3DU%26ved…`) and has
been reduced to the plain URL. No wording was altered.

> **⚠️ NOTHING BELOW IS VERIFIED.** Several claims are demonstrably false — see
> [`README.md`](README.md) for the verdict table and `../HANDOVER.md` §10 for the evidence.
> Do not quote anything from this file as fact without checking it first.

---

## Reply A — DeepSeek

### 1. Is the "two text changes" decision right, or too passive?

**The decision is too passive.**

Most users skip first-run disclaimers, and many never open Settings. The app therefore leaves a significant number of northern-hemisphere users with a default that, for four to five months of the year, gives an Isha time up to 80 minutes later than their local mosque's *beginning* time and an equally early Fajr. That can affect fasting (Fajr) and community norms without the user understanding why.

**A one-time prompt above ~45° latitude is the right middle ground – and it does *not* silently take a religious position on the user's behalf.**

- The prompt would say something like:
  *"You're in a region where prayer-time differences between calculation methods can exceed one hour. The app's default may not match your local mosque timetable. You can change the method now or later in Settings."*
- The trigger is a purely astronomical fact (solar depression-angle geometry at high latitudes), not a madhhab opinion. It does **not** auto-switch the method; it puts the choice in front of the user.
- A latitude threshold (~45° N or S) is objective. It doesn't assert that one method is correct; it only warns that the *differences are large*.

The rejection of auto-switching by latitude was correct because auto-switching **would** silently adopt a fiqh-adjacent position. A prompt does not do that. I would therefore extend the fix to include a modal shown once, only if `abs(latitude) > 45`.

### 2. Is `HighLatitudeRule.TWILIGHT_ANGLE` the right choice?

**Hard-forcing `TWILIGHT_ANGLE` on every method is a religious-adjacent decision in disguise, and it produces times that diverge from actual practice in places like the UK.**

The point of a high-latitude rule is to resolve an *astronomical impossibility* – when the sun never reaches the required depression angle. Different Islamic authorities resolve this differently:

- **MWL** itself does not prescribe one universal high-latitude rule. Some Muslim communities in northern Europe use `SEVENTH_OF_THE_NIGHT`, some use `MIDDLE_OF_THE_NIGHT`, and some adopt the "nearest latitude" method.
- In the UK, `SEVENTH_OF_THE_NIGHT` (or even `MIDDLE_OF_THE_NIGHT` with adjustments) is sometimes closer to published mosque timetables during midsummer. The observation that it "lands closer to UK mosque practice" is important – it shows that **real mosques in that region are *not* using an angle-based rule** for extreme nights.

When the app forces `TWILIGHT_ANGLE` for all methods, it **overrides** the calculation convention that many users might reasonably expect. For `Moonsighting Committee`, the `adhan-java` library likely already has a built-in approach for persistent twilight (possibly using the `shafaq` model's limiting angle), and forcing `TWILIGHT_ANGLE` may produce a time inconsistent with that method's intended behaviour.

**What would be better:**

1. Let **each method carry its own high-latitude rule** (the library's default per method) unless the user explicitly changes it. This respects the method's design.
2. Offer the high-latitude rule as an **advanced user-selectable option** in Settings, with a note that it only affects the ~10-week mid-summer period. That moves the decision from the developer to the user without imposing a single fiqh-adjacent rule.
3. The current two-text-changes fix didn't address this at all, so a real failure mode remains: a user in London who switches to `Moonsighting Committee` still gets a mid-summer Isha that may not match what the Moonsighting Committee would calculate using its own native handling of persistent twilight.

### 3. Is Moonsighting Committee the right recommendation for UK users generally?

**No – it is over-fitted to two mosques in one town. The UK is home to multiple mutually incompatible, widely-used timetable authorities.**

*I'm moderately confident about these specifics based on published UK mosque timetables and community documentation, but I'll flag where precision may vary.*

Major UK authorities and their typical *beginning*-time conventions:

- **London Unified Prayer Timetable (LUT)** – Fajr 18°, Isha 12° (or 12.5°). Used by many London mosques and beyond.
- **East London Mosque** – Fajr 18°, Isha 15°. Very influential, especially among Bengali communities.
- **Hizbul Ulama UK** (often used in northern England) – frequently 18° Fajr, 15° Isha (I'm ~80% confident; some branches may use 17°).
- **Wifaqul Ulama** (UK) – also commonly 18°/15°, possibly 18°/17° in some areas.
- **Muslim World League (18°/17°)** is still used by a number of UK mosques.
- **Moonsighting Committee Worldwide** is a genuine, active minority, but it is not the default for most UK congregations.

The Slough mosques happen to follow MCW, but two data-points in Berkshire do not generalise to the whole country. Recommending MCW to a user in Glasgow or Cardiff would silently impose a method that differs by 40+ minutes from the timetable posted in their local mosque. The app was right not to auto-recommend any method, and the text-only approach (pointing users to ask their mosque) is the correct one here.

### 4. Is the sect → method mapping sound?

**No – this is a serious design flaw. It directly violates the principle "never take a religious position on the user's behalf."**

- `Sunni → MWL` and `Shia → Jafari` conflate a **school of fiqh** (or even just a broad sectarian label) with a **calculation convention** chosen by an organisation.
- MWL is not a madhhab. It is a Saudi-based NGO that popularised an 18°-17° convention. Countless Sunni scholars, mosques, and whole countries (Turkey, Egypt, Morocco, etc.) do *not* follow MWL.
- By assigning MWL silently based on "Sunni", the app is effectively saying: *the default Sunni position is MWL.* That is a religious-adjacent claim, even if unintended.
- Shia → Jafari is somewhat more defensible, because the Jafari method (16° Fajr, 14° Isha, plus a distinctive Asr factor) is indeed broadly tied to Shia jurisprudence – but even here, different Shia authorities sometimes prefer other angles.

**A better approach:**
Decouple method selection from sect entirely. The first-run flow could:
- Ask for madhhab **only** if needed (e.g., to determine Asr time if the user wants Hanafi vs. standard Shafi'i/others). Even that can be a separate setting.
- Then explicitly present a calculation-method picker, with a clear explanation that this depends on local mosque practice, not on madhhab, and defaults to a worldwide neutral like MWL only as a starting point.

If that's too heavy, at minimum, after selecting "Sunni", a prompt should say: *"The app has set a default calculation method (MWL). This is not based on your school of thought; it is only a starting point. Please check your local mosque's method in Settings."* The current two text changes do not fix the **silent assignment** on first run.

### 5. Is there a materially better default than MWL for a worldwide audience?

**A timezone-based offline mapping would be more *accurate* for users who never change settings, but it would also be more *opinionated*, and the app has already (rightly) rejected that class of solution.**

A timezone-to-method table (e.g., `Asia/Karachi` → Karachi method) can be derived from regional norms and stored offline. It would get Isha closer to local practice in many Muslim-majority areas. However:
- In countries with multiple competing methods (UK, India, USA), picking one **is** taking a position.
- The app's core philosophy rejects making that choice invisibly.

**The one improvement I would press for is to decouple method from sect (point 4) and keep MWL as a worldwide, value-neutral default** – while putting the calculation-method picker *in front of the user* during onboarding, together with a note that it can be changed later. Then the user is making the choice, not the app.

**Travel:** When a user travels, the method stays as set. That respects the user's chosen convention. The risk is that a user on Karachi method who flies to London will get unexpected times; but that is a user-education issue, not an app-logic failure. An optional "suggest method based on current country" toggle could be added later without violating the principles, as long as it's off by default and explicitly asks.

### 6. What is being missed entirely?

Several failure modes are unaddressed:

**a) Fajr differences are as critical as Isha, especially for fasting**
The whole discussion centres on Isha, but the default MWL gives **earlier Fajr** than many conventions in northern latitudes. At Slough in summer, MWL Fajr could be 40–60 minutes earlier than the Moonsighting Fajr. A user following the default could start their fast too early – a far more consequential error than a late Isha. The disclaimer and Settings text should explicitly mention Fajr alongside Isha, and highlight the Ramadan implication.

**b) Extreme latitudes (>60°) where the sun never sets or never rises**
`TWILIGHT_ANGLE` will fail entirely when the minimum solar depression is above the required angle (midnight sun). The library may return `null` or throw an exception. The app must handle those cases gracefully – ideally showing "not astronomically calculable – consult local scholars" instead of crashing or showing a garbage time. That is a genuine real-world need for users in Scandinavia and beyond, even if a minority.

**c) Forcing TWILIGHT_ANGLE on methods that already define their own high-latitude behaviour**
This was raised in question 2, but it's a hidden failure: the app overrides method-specific logic, potentially producing times that contradict the method's own published tables for extreme latitudes.

**d) Southern Hemisphere**
All testing was northern-centric. Isha differences at high southern latitudes will be symmetrical but inverted by season. The threshold for a prompt (`>45°` absolute) is fine, but it should be validated with southern-hemisphere dates and locations.

**e) DST edge cases**
If Fajr or Isha fall into the "missing" or "ambiguous" hour on DST transition nights, the displayed local time might be off by an hour if the app doesn't correctly handle `java.time` zone rules. This is minor but real; verifying with the library's conversion is worth doing.

**f) The Asr factor from madhhab is silently bundled**
If the app asks for Shafi'i vs. Hanafi to set Asr (shadow length factor), that's separate from the calculation method and should remain. But currently it's tangled with the MWL assignment. Unbundling that would prevent a Hanafi user from being forced into a convention they didn't choose.

**In summary:**
The two-text-change fix was necessary but insufficient. A one-time latitude-aware prompt (not an auto-switch) is the right next step. The high-latitude rule should respect the selected method and be user-adjustable. The sect-to-method mapping is a breach of the app's own principles and should be replaced by an explicit, neutral default plus user choice. And the conversation must extend to Fajr and to extreme-latitude edge cases.

---

## Reply B — ChatGPT / Codex

My blunt verdict: the two text changes are helpful but too passive. Keep them, but add one contextual prompt and make the active calculation method visible on the main screen. More fundamentally, the current design is not religiously neutral: Sunni → MWL plus a universal `TWILIGHT_ANGLE` rule already chooses positions silently. The defensible principle is transparency, reversibility, and no pretending to be an authority—not "the app makes no choices," which is impossible.

### 1. Two text changes: not enough

A one-time high-latitude prompt is justified because it informs without changing anything automatically.

I would not trigger it at a hard northern `latitude > 45`. Instead:

- Use `abs(latitude)` so southern users are covered.
- Trigger only while the method remains automatically/default-selected.
- Calculate the maximum spread between plausible methods over the coming 90–180 days.
- Show it when that spread exceeds a meaningful threshold, perhaps 20 minutes.

Suggested wording:

> Prayer calculation methods differ significantly at this latitude. SajdaTime currently uses Muslim World League. In the coming months, Isha may differ by up to 58 minutes from other timetables. Ask your mosque which method it follows.

Actions: **Choose method** and **Keep MWL**.

Also place a small, tappable `MWL` or `Calculation: MWL` label on the main prayer screen. That persistent provenance is more valuable than another disclaimer paragraph.

I would revise two parts of the proposed disclaimer:

- Replace "neither is wrong" with "they may refer to different things." Timetables can, in fact, be mistaken or mislabelled.
- Replace "becomes due" with "calculated beginning time." Moonsighting Committee deliberately adds safety margins—five minutes to Dhuhr and three to Maghrib—so it is not claiming to display the precise earliest astronomical instant. Those adjustments are in the library itself. [Adhan calculation-method source](https://github.com/batoulapps/adhan-kotlin/blob/ea40abde0bcc54ba9a9d89948ddeb67179099161/adhan/src/commonMain/kotlin/com/batoulapps/adhan2/CalculationMethod.kt)

**Confidence: high** on the reachability problem; the proposed UX is my product judgment, not a fiqh ruling.

### 2. High-latitude rule

I would not keep `TWILIGHT_ANGLE` as one universal rule.

`TWILIGHT_ANGLE` is not neutral: for MWL Isha it caps at roughly 17/60, or 28%, of the night. One-seventh caps at about 14%, while middle-of-night caps at 50%. Those are materially different religious approximations, not merely mathematical implementation details.

My recommended structure is:

- Default: **Follow selected calculation method**.
- Advanced setting, shown mainly to affected users: allow an explicit override.
- Permit separate Fajr and Isha policies internally. A single symmetric enum is too crude.

That last point matters. `MIDDLE_OF_THE_NIGHT` is not inherently unusable: Wifaqul uses a midpoint-style approach for Fajr during perpetual twilight. It becomes unusable when the same midpoint is simultaneously imposed as latest Isha and earliest Fajr, producing no interval between them.

For specific methods:

- **Moonsighting Committee:** use its own seasonal algorithm. In `adhan-java` 1.2.1, Moonsighting bypasses the generic high-latitude rule and uses its own seasonal safe bounds; above 55°N it also forces one-seventh. Setting `TWILIGHT_ANGLE` on its parameters therefore does not control the result in the way the app may imply. [Adhan Java 1.2.1 source](https://github.com/batoulapps/adhan-kotlin/blob/v1.2.1/adhan/src/main/java/com/batoulapps/adhan/PrayerTimes.java)
- **Simple angle methods such as MWL:** if forced to choose one usability-oriented fallback above roughly 48°, I would choose one-seventh over twilight-angle. Current Adhan documentation makes the same recommendation. [Adhan methods guide](https://github.com/batoulapps/adhan-js/blob/master/METHODS.md)
- **Do not infer that one-seventh is "UK practice."** It resembles Wifaqul's summer hardship cap, but London Unified and the two tested Slough mosques use the later Moonsighting seasonal result.

There is genuine fiqh plurality here. Wifaqul's own seven-hour deliberation recorded several acceptable approaches and explicitly declined to claim a single agreed national solution. [Wifaqul high-latitude deliberation](https://www.wifaqululama.co.uk/highlat/) Moonsighting Committee cites jurisprudential precedent for one-seventh and uses it at 55–60°. [Moonsighting calculation methodology](https://www.moonsighting.com/how-we.html)

**Confidence: high** on implementation behaviour and documented authority positions; **medium** on choosing one-seventh as SajdaTime's fallback.

### 3. Is Moonsighting Committee right for the UK?

It is right as a recommendation for:

- London Unified Prayer Timetable users;
- East London Mosque;
- the two verified Slough mosques;
- users whose mosque confirms it follows Moonsighting Committee.

It is not defensible as "the UK method."

The most important correction is that London Unified is not accurately described as a fixed 12°/12° timetable. Its official explanation says it uses Khalid Shaukat's observation-guided seasonal model, incorporating observations by Hizbul Ulama. [London Central Mosque's official explanation](https://www.iccuk.org/page.php?page=unifiedpt&section=religious) East London Mosque simply adopts London Unified, so it is not an independent fourth convention. [East London Mosque explanation](https://www.eastlondonmosque.org.uk/prayer-times-and-calendar-explained)

Wifaqul Ulama is genuinely different: Fajr at 18°, Isha at 15° above 48°, with additional hardship treatment. [Wifaqul methodology](https://www.wifaqululama.co.uk/salahtimes/)

I calculated these comparisons directly from the official [London Unified 2026 timetable](https://www.unifiedsalahtimes.org/_files/ugd/04535a_e5adb7957fdb4c1fb051c857b185f153.pdf) and [Wifaqul London Central 2026 timetable](https://www.wifaqululama.co.uk/images/salah/2026/London-Central.pdf):

| Date | London Unified Fajr / Isha | Wifaqul Fajr / Isha | Wifaqul minus Unified |
|---|---:|---:|---:|
| 1 Jan | 06:37 / 17:20 | 06:03 / 17:46 | Fajr −34m; Isha +26m |
| 21 Jun | 02:40 / 22:42 | 01:02 / 22:25 | Fajr −98m; Isha −17m |
| 1 Aug | 03:38 / 22:08 | 02:32 / 22:02 | Fajr −66m; Isha −6m |

So two Slough mosques agreeing within four minutes does not show UK-wide agreement. It shows that those two mosques belong to the same convention family.

I would label the option:

> Moonsighting Committee — used by London Unified and some UK mosques

Not:

> Recommended for the UK

If SajdaTime adds one new UK-facing method, a properly implemented **Wifaqul Ulama** profile would add more meaningful coverage than another nearly-MWL low-latitude method.

**Confidence: high**, based on primary authority pages and 2026 timetables.

### 4. Sect → method mapping

No—the mapping is conceptually unsound.

The calculation convention and madhhab are separate dimensions:

- Madhhab primarily controls Asr shadow length in Adhan.
- Calculation method controls Fajr, Isha, fixed intervals, seasonal treatment, and method-specific adjustments.
- Some fiqh considerations influence both, but "Sunni" does not logically imply MWL.

The library itself models `madhab` and `method` as independent parameters. [Adhan methods guide](https://github.com/batoulapps/adhan-js/blob/master/METHODS.md)

Likewise, "Shia → Jafari" is a reasonable suggestion for some Twelver users, but not a universal derivation. Common calculators distinguish the Leva/Qom Jafari convention—16° Fajr, 14° Isha—from the Tehran convention—17.7° Fajr, 14° Isha, plus a different Maghrib definition. [PrayTimes calculation conventions](https://praytimes.org/docs/methods)

Recommended change:

- Ask the Asr choice separately, using understandable wording.
- Ask or suggest the calculation method separately.
- If sect is otherwise unnecessary, do not collect it merely to select a calculation method.
- For an existing installation migrated from the old mapping, preserve the chosen result but mark it internally as auto-selected so the contextual prompt can appear.

**Confidence: high** on the conceptual separation.

### 5. Better worldwide default

There is no materially better single worldwide default than MWL. Its performance in your sample makes it a defensible baseline.

The better system is an offline recommendation layer:

1. Use coarse coordinates to suggest a regional shortlist.
2. Require explicit confirmation before changing the method.
3. Keep the confirmed method sticky during travel.
4. Optionally offer "Review local convention" after a major move.

Do not use the device timezone as the sole selector. A timezone is a civil clock-rule area, not a religious authority; it can span several countries or communities. Locale may represent the user's language or home country rather than their current location.

A shipped regional database will also require maintenance. Malaysia illustrates this: official authorities changed Fajr from 20° to 18° around 2019–20, moving it by about eight minutes. [Official JUPEM notice](https://www.jupem.gov.my/Prepared/info-semasa/pengumuman/18) An offline mapping can therefore become religiously consequential when stale.

When travelling:

- Solar geometry should use the current physical location.
- The calculation convention should remain the user's chosen authority unless they accept a local suggestion.
- A user deliberately following an overseas authority is therefore supported.
- Never silently change conventions because Android changed from `Europe/London` to `Asia/Karachi`.

**Confidence: medium-high**; this is primarily product design reasoning.

### 6. What is being missed

The largest omissions are these:

1. **Umm al-Qura during Ramadan.** Adhan's preset remains 90 minutes after Maghrib and explicitly instructs callers to add 30 minutes during Ramadan. Without app-specific handling, SajdaTime's Umm al-Qura option is 30 minutes early by that convention during Ramadan. [Adhan source documentation](https://github.com/batoulapps/adhan-kotlin/blob/ea40abde0bcc54ba9a9d89948ddeb67179099161/adhan/src/commonMain/kotlin/com/batoulapps/adhan2/CalculationMethod.kt) Because Ramadan's start can differ by sighting, a user-confirmed "Ramadan has begun/ended" control is safer than silently trusting a calculated Hijri date.

2. **Only Isha was globally validated.** Rejecting KEMENAG or JAKIM from an Isha-only comparison is not fully supported. Fajr is particularly consequential for fasting. Indonesia's Kementerian Agama officially retains a −20° Fajr criterion, two degrees from MWL. [Kemenag statement](https://kepri.kemenag.go.id/page/det/kriteria-waktu-subuh-20-derajat-benar-secara-fikih-dan-sains) Test all six displayed boundaries, both Asr modes, and Ramadan-specific behaviour before concluding a regional method adds no value.

3. **Southern and polar behaviour.** Any latitude trigger must use absolute latitude. More seriously, Adhan Java 1.2.1's Moonsighting special case checks `latitude >= 55`, not `abs(latitude) >= 55`, and returns null prayer times when sunrise or sunset cannot be computed. That needs explicit tests and failure handling for the Antarctic circle and high southern latitudes. [Adhan Java source](https://github.com/batoulapps/adhan-kotlin/blob/v1.2.1/adhan/src/main/java/com/batoulapps/adhan/PrayerTimes.java) Moonsighting's published method says that above 60° it projects down to 60° and approximates times; the Java preset does not fully implement that published polar policy. [Moonsighting methodology](https://www.moonsighting.com/how-we.html)

4. **DST and timezone provenance.** Calculate astronomical instants in UTC, then format them using the region `ZoneId` and the rules for that prayer's date—not today's raw UTC offset. Governments change timezone rules frequently, and old Android devices can have stale tzdata. [Android timezone-data documentation](https://source.android.com/docs/core/ota/modular-system/timezone), [Android `ZoneId` documentation](https://developer.android.com/reference/java/time/ZoneId) A disconnected old phone cannot always be correct after a last-minute government DST change; that limitation should be accepted and tested rather than hidden.

5. **The wording overclaims precision.** Because presets contain conservative offsets, "calculated beginning time" is more accurate than "when prayer becomes due." This is not merely legal caution; it correctly describes what the program computes.

6. **Equator and ordinary low latitudes are not the main risk.** Your measurements support leaving them alone. The real test matrix should concentrate on ±45°, ±55°, ±60°, polar-circle days, both solstices, DST transition days, Ramadan fixed-interval methods, stale locations after travel, and phone/watch timezone disagreement.

### Recommended decision

I would ship the disclaimer and picker text, but not stop there:

1. Add the one-time divergence prompt and persistent method label.
2. Remove sect → method derivation.
3. Replace universal high-latitude handling with "follow method," plus an advanced override and separate Fajr/Isha policies.
4. Describe Moonsighting as London Unified/some-UK-mosques, not the UK method.
5. Fix or explicitly handle Umm al-Qura Ramadan and polar/Southern Hemisphere behaviour.
6. Change "becomes due" and "neither is wrong" to more accurate wording.

Those changes remain fully offline, preserve coarse-location privacy, and do not silently choose a new religious convention for anyone.

---

## What happened next

Point 3 of Reply B's final section — polar behaviour — turned out to be a **live crash**, not a
robustness concern: `NullPointerException` on every caller of the engine above 65.5°N, in both
solstices, in every release shipped to that date. It was fixed the same day. See
`../HANDOVER.md` §10, "The app crashed above the Arctic Circle".

Reply A raised the same area under 6(b) and guessed correctly that the library "may return
`null` or throw an exception", but attached it to `TWILIGHT_ANGLE` failing, which is not the
mechanism. adhan returns null for *all six* fields because it cannot establish sunrise or
sunset at all, whatever the high-latitude rule says.

Both replies asserted the Fajr risk lay at high northern latitudes. Measurement showed the
opposite: at Slough the default runs 15–42 minutes *earlier* than local practice, which is
cautious. The genuine fasting risk is in Indonesia, Malaysia and Singapore, where the default
runs 8–9 minutes *later* than the national timetables. That finding came from neither reply.
