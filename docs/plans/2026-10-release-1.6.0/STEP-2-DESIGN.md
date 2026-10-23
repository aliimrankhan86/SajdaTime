# Step 2: Islamic dates sheet, and 2a Shia dates, design (Fable `ux-lead`, 23 Oct 2026, night)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 2 only.

**Status: a design, not built.** Written while Ali's yes on step 1's finished pictures was still awaited (he
asked to use the remaining usage, 23 Oct 2026 night), so nothing for step 2 is built until he says yes to step 1
and to this. Next: `plan-verifier`, then Ali's two questions (§7), then the build.

## 1. Recommendation

Step 2 ships as built: no visible change, one check screenshot (dark beside the existing light
`screens/islamic-dates.png`). Step 2a: five Shia rows (Ghadir, Arbaeen, the Prophet's birth, Imam Ali's birth,
Imam al-Mahdi's birth), merged in date order for Shia users only, driven by the stored sect. The three Ramadan
nights are sourced but left out of 1.6.0 (Ali's call, question 1).

## 2. Step 2, the shared sheet (check only)

Verified by `ux-lead` in `ui/home/IslamicDatesSheet.kt` and `core/.../IslamicDates.kt` against SIGNOFF: six rows,
1 Muharram shown as the date itself, the footer string as signed off, "Shift the date" the only button
(`CtaButton`, §4.1), no ranges, heading semantics, rows merged for TalkBack, vertical gaps from `Space`, the
unavailable state outside 1300 to 1600 AH. `IslamicDatesGoldenTest` covers 1448 and 1449 against Aladhan Umm
al-Qura, plus a 400 day header agreement loop. Nothing to change. To confirm by eye: at 2.0 text on 320dp the name
wraps and the "in 108 days" column keeps its width.

## 3. Step 2a, sources (named Shia sources with the Hijri date)

| Date | Source | Quote |
|---|---|---|
| Eid al-Ghadir, 18 Dhu al-Hijjah | I.M.A.M. (office of Sayyid Sistani, North America) https://imam-us.org/congratulations-on-the-joyous-event-of-eid-al-ghadir-1447 | "the 18th of Dhu al-Hijjah 1447 A.H., which marks the day of Eid al-Ghadir" |
| Arbaeen, 20 Safar | I.M.A.M., relaying Sistani's office https://imam-us.org/clarification-regarding-the-determination-of-arbaeen | "it is indeed the 20th day of Safar" |
| The Prophet's birth, 17 Rabi al-Awwal | I.M.A.M. https://imam-us.org/birth-anniversary-of-prophet-muhammad-pbuhhp-imam-jafar-al-sadiq-p-1446-a-h | "Saturday, September 21st is the 17th of Rabi al-Awwal" |
| Imam Ali's birth, 13 Rajab | I.M.A.M. https://imam-us.org/birth-anniversary-of-imam-ali-ibn-abi-talib-p-1444-a-h | "birth anniversary of Imam Ali ibn Abi Talib, 13th of Rajab" |
| Imam al-Mahdi's birth, 15 Sha'ban | I.M.A.M. https://imam-us.org/birth-anniversary-of-imam-muhammad-al-mahdi-p-1446-a-h | "the 15th of Shaban which marks the birth anniversary of Imam Muhammad al-Mahdi" |
| Nights of 19, 21, 23 Ramadan | al-Islam.org Q&A https://al-islam.org/ask/topics/7366/questions-about-Laylat-al-Qadr | "19th or 21st or 23rd of the Month of Ramadan" |

Supporting: al-Islam.org Ghadir Q&A https://al-islam.org/ask/topics/9620/questions-about-Eid-al-Ghadir. al-Islam.org
refuses fetch tools (403), so its quotes come from search snippets: **open each source in a browser before citing it
in code** (not done yet). The I.M.A.M. pages were fetched by `ux-lead`.

## 4. Rows, placement and sect

- **Merged in date order** with the shared six, with no "Shia" heading: the user asks "what is next", and a
  labelled group would print the user's sect on screen for no gain. Shia users see eleven rows (the sheet scrolls).
- **Sunni users never see them:** filtered in `:core` by `Sect`. Verified: `Sect { SUNNI, SHIA }` in
  `core/PrayerModels.kt`; `SettingsRepository` reads `enumOr(this[Keys.SECT], Sect.SUNNI)`, so **unset means Sunni,
  six rows**; setup asks the sect. `settings.sect` is available at both call sites (`HomeScreen.kt:264`,
  `SettingsScreen.kt:690`). The sheet sends nothing: no new usage event, and the sect never leaves the phone.
- **The nights are not rows in 1.6.0:** a Hijri night begins at the previous sunset, so a row dated by the day
  would be a day off for the night, and three more rows make fourteen. SIGNOFF dropped Laylat al-Qadr for the same
  reason. Their later home: the Ramadan timetable (step 4).
- **Wording:** plain names, nothing celebratory. Ashura stays "Ashura"; Arbaeen has no subtitle.
- **Honorific:** the app names the Prophet nowhere today (verified). One glyph, ﷺ (U+FDFA), which scales with text
  and is direction neutral. Imams plain. To check: the glyph renders on an API 26 emulator, and how Google's
  text to speech reads it (assumed: "peace be upon him").

## 5. Strings (English; the main session translates)

```
date_ghadir            Eid al-Ghadir
date_arbaeen           Arbaeen
date_prophet_birth     Birth of the Prophet ﷺ
date_imam_ali_birth    Birth of Imam Ali
date_imam_mahdi_birth  Birth of Imam al-Mahdi
```
`settings_islamic_dates_desc` ("Ramadan, the two Eids and other key dates") fits both sects; unchanged.

**Code shape:** `IslamicDay(month, day, shia: Boolean = false)` with five new entries, each with its source in a
KDoc; `IslamicDates.upcoming(today, offsetDays, sect)` keeps `!d.shia || sect == Sect.SHIA`. `IslamicDatesGoldenTest`
gains ten Aladhan Umm al-Qura rows (1448, 1449) for the five dates, "the Sunni list is unchanged" and "the Shia list
has eleven, sorted". Plain Kotlin in `:core`, no Android import.

## 6. States, accessibility, right to left, iPhone

Sunni or unset: six rows, unchanged. Shia: eleven, soonest first, today in `primary`, "Today" and "Tomorrow" as now.
Out of range: the existing "not available" line. The date shift applies to every row. TalkBack: one stop per row,
"Birth of Imam Ali, Sat 1 Jan, in 70 days". Right to left: names are translated strings; dates isolated as today.
1.5 and 2.0 text: titles wrap to two lines; the count column holds. Light and dark: existing roles only.
**iPhone effect: carries over** (the rule in `:core`, plain data; the sheet is a standard sheet).

## 7. Questions for Ali

1. The three Ramadan nights (19, 21, 23) for Shia users: **leave out of 1.6.0 (recommended)**, or add three rows dated
   by the day each night leads into?
2. Honorifics: **"Birth of the Prophet ﷺ" with Imams plain (recommended)**, or no honorific anywhere, or "(a)" after
   each Imam as Shia sites write?

## 8. Corrections from `plan-verifier` (23 Oct 2026, night). These win over §1 to §7

Nothing serious; Q20 and Q21 checked as written and consistent (Q20 removed Mawlid, Isra and Mi'raj and 15 Sha'ban
from the shared list; Q21 names 17 Rabi al-Awwal and 15 Sha'ban as Shia only, in Ali's words; 12 Rabi al-Awwal
stays out for everyone). Before building:
1. **Pictures.** Step 2a changes what a Shia user sees, so it ends with the full set (light, dark, Urdu, 1.5 and 2.0,
   823dp and 320dp) with the sect set to Shia, plus one Sunni shot proving six rows.
2. **Two comments to rewrite** citing Q20 and Q21: `core/.../IslamicDates.kt:9-14` ("contested observances ...
   taking a side") and `ui/home/IslamicDatesSheet.kt:44-46` ("the same for every sect").
3. **`IslamicDatesGoldenTest.kt:65-77`'s 400 day agreement loop runs for both `Sect` values,** so the five new rows
   are checked against the header too.
4. **KDocs cite only the five fetched I.M.A.M. pages;** the unopened al-Islam.org links stay in this file.
5. **`upcoming(today, offsetDays, sect)` has no default for `sect`,** so both call sites must pass it
   (`HomeScreen.kt:264` reads `state.settings.sect`; `SettingsScreen.kt:690`) and cannot silently show six rows.
6. Golden values come from Aladhan Umm al-Qura, not from I.M.A.M.'s own calendar (its 17 Rabi al-Awwal 1446 is a day
   off Umm al-Qura); the sheet's footer already says local sighting may differ.
