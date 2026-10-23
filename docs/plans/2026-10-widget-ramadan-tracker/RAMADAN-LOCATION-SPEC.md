# Ramadan, based on the user's location: the design

Written 16 October 2026 against `main` at `13b581d`. This is the product and religious wording design
behind tasks A1 to A5 and A8 in `PLAN.md`. Every statement about the code cites a file and line.
Every date and time below was computed in the planning session, not recalled, and section 9 says how.

## 1. What the app knows, and what it does not

**Knows (verified).**

| Fact | Where |
|---|---|
| Coordinates, range checked on read | `app/data/SettingsRepository.kt:369`, `core/PrayerModels.kt:88-92` |
| A place label, as text, in the app's language: "Slough, United Kingdom", "Alanya, Türkiye", or "Makkah" when the fallback was used. The country appears only as a word inside that label and is omitted when it equals the place | `app/data/PlaceName.kt:39-76`, `app/data/SettingsRepository.kt:303-308` |
| The device time zone, read at use time as `ZoneId.systemDefault()`, never stored | `app/ui/SajdaViewModel.kt:376`, `app/notify/PrayerAlarmScheduler.kt:46`, `app/notify/TimeFormat.kt:39` |
| The engine's six times for any calendar day at the coordinates, with the user's method, madhab, per prayer minutes and Hijri shift applied, truncated to the minute | `core/PrayerEngine.kt:220-263, 279-308, 456-457` |
| Whether a day was projected from a reference latitude (45 or 60 degrees) because the sun would not rise or set, and which latitude | `core/PrayerEngine.kt:147-152, 198-207, 251-253`, `core/PrayerModels.kt:111-114` |
| The Hijri date on the Umm al-Qura calendar (`HijrahChronology`, id `Hijrah-umalqura`), shifted by the user's day offset of minus 2 to plus 2, used today by the header and by the Umm al-Qura Isha rule | `app/ui/home/HomeScreen.kt:894-927`, `core/PrayerEngine.kt:413-436`, `core/PrayerModels.kt:156-180` |
| The Umm al-Qura Isha rule: Maghrib plus 90 minutes, plus 30 more during Ramadan, for that method only | `core/PrayerEngine.kt:75-76, 413-423` |
| Whether exact alarms are permitted, and the three rung alarm ladder | `app/notify/PrayerAlarmScheduler.kt:154-193` |

**Does not know (verified by absence).**

- A country code. `Address.countryCode` is available from the geocoder at lookup time and is discarded
  (`app/data/PlaceName.kt:78-90` reads `countryName` only). The Open-Meteo fallback keeps the country as
  a name too (`app/data/CityLookup.kt:110`). A user who typed a city, or who uses the Makkah fallback,
  has a label that may carry no country at all.
- Any moon sighting authority, announcement, or national convention. Nothing in the app or its data
  describes how any country decides the start of a month.
- An imsak time, a precaution margin, or a jama'ah time. None exists in the engine, by design
  (`docs/HANDOVER.md` A11 and 5.17).
- Which mosque the user follows. The "Match your mosque" minutes and the date shift are the only
  expression of it.
- The user's intent to fast. The app is a prayer times app and claims nothing about who is fasting.

Everything below is built only from the first table.

## 2. Principles

1. **The app computes and reports. It never rules.** Suhoor ends at Fajr and iftar is at Maghrib because
   those are the two times the app already calculates and labels, and the wording says "as calculated".
   Nothing says "you must stop eating", "your fast is valid", or "Eid is tomorrow".
2. **Umm al-Qura plus the user's shift is the calendar, everywhere.** It is what the app already shows in
   its header and already uses for the Isha rule, so the card can never disagree with the date line above
   it. Section 6 is the argument for not adding a country table.
3. **HANDOVER A11 is honoured, not reopened.** Keep 18 degrees (or whatever the chosen method says), say
   plainly that local and national timetables may begin Fajr earlier and should be confirmed, never add
   an imsak margin silently (`docs/HANDOVER.md:6188-6200`).
4. **Follow your mosque is the last sentence of every explanation**, as it is in the disclaimer
   (`docs/HANDOVER.md` 5.15).
5. **The day boundary is the app's existing one: local midnight.** The Hijri day in practice begins at
   sunset, and the app's header rolls at midnight. For fasting this is the right convention: a printed
   Ramadan timetable puts the suhoor end and the iftar of fasting day N on the same row, and both fall on
   the same Gregorian date. The night of Taraweeh before day 1 is still 29 or 30 Shaban in the header,
   which is also what a printed timetable's rows imply. No change to the date boundary is made or needed.
6. **Everything is recomputed from current settings on every refresh**, so a change of location, method,
   minutes or date shift is reflected the next time the state is built (`app/ui/SajdaViewModel.kt:369-403`).

## 3. Calendar facts (verified with JDK 21 `java.time`, `Hijrah-umalqura`, 16 October 2026)

| Hijri year | 1 Ramadan (Gregorian) | Day | Length | Last day | 1 Shawwal | Shaban length |
|---|---|---|---|---|---|---|
| 1446 | 2025-03-01 | Saturday | 29 | 2025-03-29 | 2025-03-30 | 29 |
| 1447 | 2026-02-18 | Wednesday | 30 | 2026-03-19 | 2026-03-20 | 29 |
| **1448** | **2027-02-08** | **Monday** | **29** | **2027-03-08** | **2027-03-09** | **30** |
| 1449 | 2028-01-28 | Friday | 29 | 2028-02-25 | 2028-02-26 | 30 |
| 1450 | 2029-01-16 | Tuesday | 29 | 2029-02-13 | 2029-02-14 | 30 |

The chronology covers Hijri years 1300 to 1600 (checked: `HijrahChronology.INSTANCE.range(YEAR)` prints
`1300 - 1600`). Outside that `HijrahDate.from` throws, which is why `HijriCalendar.of` returns null
(`PLAN.md` A1) and the existing code wraps it in `runCatching`.

**How the shift works (verified by running it).** The app shows, for Gregorian date G,
`HijrahDate.from(G.plusDays(offset))`. So on 2027-02-07: offset minus 2 shows 28 Shaban, minus 1 shows
29 Shaban, 0 shows 30 Shaban, plus 1 shows 1 Ramadan, plus 2 shows 2 Ramadan. A user whose mosque starts
Ramadan one day **after** Umm al-Qura sets **minus 1**, one day before sets **plus 1**. The Settings help
text already says "a day either way" (`app/src/main/res/values/strings.xml:447`).

**Aladhan agrees** that 8 February 2027 is 01-09-1448 (its `hijri.date` field, fetched 16 October 2026).

**Assumed:** that `desugar_jdk_libs` 2.1.5, which supplies `java.time` below API 26 and is what the
shipped app runs on every version (`app/build.gradle.kts:131-136`), carries the same Umm al-Qura table.
`RamadanDeviceTest` asserts on the device that `HijriCalendar.of(2027-02-08, 0)` is day 1, month 9, year
1448 with month length 29, and that 2026-02-18 is 1 Ramadan 1447 with length 30.

## 4. The features, with exact behaviour and acceptance criteria

Each item lists what is shown, where the number comes from, and the acceptance criteria (AC). "Card" is
`RamadanCard` on the Times screen (`PLAN.md` A3). All times are the **adjusted** times, so "Match your
mosque" minutes apply (`core/PrayerEngine.kt:261`).

### 4.1 Ramadan day number and month length for this user

Shown as "Ramadan · Day 15 of 29" on the card and "Day" column in the timetable. From
`HijriCalendar.ramadan(today, offset)`: `dayNumber` and `length` (`PLAN.md` A1).

- AC1 The day number equals the Hijri day in the header for every day of Ramadan and every offset from
  minus 2 to plus 2 (`HijriCalendarTest`, sweep).
- AC2 The length is 29 or 30 and equals `HijrahDate.lengthOfMonth()` of 1 Ramadan of that year.
- AC3 Outside Ramadan the card is absent. The day before day 1 shows no card, only the eve notice.
- AC4 The length shown is the calendar's. The app never says the month "will have" 29 or 30 days as a
  prediction of sighting, only "of 29" as the calendar it is set to. The info note explains that a mosque
  that sights the moon may end the month a day earlier or later.

### 4.2 Expected start and end for this user

Eve notice body: "On the calendar SajdaTime is set to, Ramadan begins on Mon 8 Feb." Card last day line:
"On this calendar today is the last day of Ramadan." Timetable heading: "Ramadan 1448" with the first and
last dates. From `RamadanInfo.firstDay` and `lastDay`, already shifted.

- AC5 `firstDay` plus the offset equals the unshifted Umm al-Qura first day, for every offset.
- AC6 The notice names the day of week and date through `TimeFormat.date` in the app's language.
- AC7 The words "expected" and "announce" are used for what mosques do, "calendar" for what the app does.

### 4.3 Today's suhoor end and iftar for this place, with the place name

Card: "Suhoor ends 05:10" and "Iftar 17:28" (London, 22 February 2027, MWL). From `today[FAJR]` and
`today[MAGHRIB]` of the same `DayPrayerTimes` the Times list shows. The place is the header above the
card ("London, United Kingdom"), which is the same `cityName` label, so the card does not repeat it. The
timetable sheet and the PDF repeat the label with "approximate area" under it, as the PDF already does
(`app/pdf/PrayerPdfExporter.kt:231-285`).

- AC8 The two times on the card equal the Fajr and Maghrib rows of the Today list to the minute, with any
  per prayer minutes applied, at every method and madhab (same object, so by construction, asserted in
  `RamadanStateTest`).
- AC9 The card never shows a time for a place other than the stored coordinates, and when the Makkah
  fallback is active the existing "Showing times for Makkah" banner sits above it unchanged.
- AC10 The label is "Suhoor ends", never "Imsak" in English (section 5 explains the languages).

### 4.4 Length of the fast and the change from yesterday

Card: "Fast today 12h 18m, 4 minutes longer than yesterday" (London, 22 February 2027: 21 February is
12h 14m, 23 February is 12h 22m). `fastLength` is
`Duration.between(Fajr, Maghrib)` of today's instants and the change is today minus yesterday in minutes
(`PLAN.md` A2).

- AC11 Computed from instants, so a daylight saving change day shows the true change (London, 28 March
  2027: plus 5 minutes, not plus 65). Asserted in `RamadanStateTest`.
- AC12 On day 1 the change is against the last day of Shaban, which is still a valid day of times. Shown.
- AC13 Wording uses "longer", "shorter" or "the same as yesterday", with a plurals resource for minutes.

### 4.5 Tomorrow's times

Card: "Tomorrow: suhoor ends 05:08, iftar 17:30". From `tomorrow[FAJR]` and `tomorrow[MAGHRIB]`.

- AC14 Hidden on the last day of Ramadan (tomorrow is Shawwal on this calendar).
- AC15 After iftar the countdown line switches to "Suhoor ends tomorrow at 05:08", so the next relevant
  time is the one counted to, and it is tomorrow's own Fajr, not today's.

### 4.6 The full Ramadan timetable screen

`RamadanTimetableSheet` (`PLAN.md` A4): one row per day, day number, date, suhoor end, iftar, fast
length, today highlighted, computed on device with `PrayerEngine.computeRange(coordinates, firstDay,
length, prefs)`.

- AC16 Row count equals `length`. Row 1's date is `firstDay`.
- AC17 Every row's Fajr and Maghrib equal `PrayerEngine.compute` for that date (same function).
- AC18 A projected row shows "approx." beside each time and the sheet ends with the existing
  `pdf_approximate_note` text naming the reference latitude.
- AC19 The sheet opens from the card and from nowhere else, and is reachable from 30 days before day 1
  through the export sheet's Ramadan option (so people can print it before the month).

### 4.7 The Ramadan timetable PDF

`Range.RAMADAN` through the existing exporter (`PLAN.md` A4). File `PrayerTimes_Ramadan1448.pdf`.

- AC20 Same masthead, logo, place line, approximate note and footer as the other ranges.
- AC21 A "Day" column with the Ramadan day number, then day name, date and the six times, on one page.
- AC22 A one line note under the masthead: "Suhoor ends at Fajr. Iftar at Maghrib. Follow your mosque
  where its timetable differs."
- AC23 Offered in the export sheet only from 30 days before day 1 until the last day.

### 4.8 Location or time zone change during Ramadan (travel, daylight saving)

Nothing new is stored, so there is nothing to migrate. The next `recalculate` (app open, settings change,
day change, prayer passed) and the next widget refresh rebuild everything from the current coordinates and
`ZoneId.systemDefault()`.

- AC24 Changing location (GPS refresh, city search) recomputes the card, the sheet and the widgets on
  the next emission, with the new place label above the card.
- AC25 A time zone change while the app is closed reaches `SystemEventReceiver` (`TIMEZONE_CHANGED`,
  `app/notify/SystemEventReceiver.kt:176`) which reschedules alarms and the reminder and refreshes the
  widgets (`PLAN.md` A7). While the app is open, the one second clock recomputes on the next date
  change and every clock string formats in the new zone on the next tick
  (`app/notify/TimeFormat.kt:39-43`).
- AC26 The day number does not change with location. It is calendar based. A traveller who lands in a
  country whose Ramadan started a day earlier or later changes the date shift, and the eve notice and info
  note both point there. The app does not guess from the new coordinates (section 6).
- AC27 The suhoor reminder for tomorrow is re planned from the new coordinates on the same triggers, so
  after a flight the reminder fires before the local Fajr, not the old one.
- AC28 Daylight saving: the fast length and change are instant based (AC11), the reminder is an instant
  (Fajr minus N), the widget chronometer base is elapsed realtime and is reset on `TIME_SET`.

### 4.9 High latitude and polar places

Two different situations, both already handled by the engine, both surfaced plainly.

**Real astronomy but extreme twilight (for example Tromsø in February and March 2027).** The sun rises
and sets and 18 degrees of depression is reached, so Fajr and Maghrib are the user's own. At Tromsø on
22 February 2027 the MWL fast is 11h 53m and grows by about two hours a week (section 9.2). Nothing is
approximated and nothing is marked.

**Projected days (no sunrise, no sunset, or no usable twilight).** `DayPrayerTimes.approximatedFrom`
is set and all six times come from the reference latitude (`core/PrayerEngine.kt:229-253`). For Tromsø
with MWL in 2027 those days are 1 to 21 January, 18 May to 26 July, and 16 November to 31 December
(computed in section 9), so Ramadan 1448 is **not** projected there, but Ramadan 1451 (December 2029)
will be.

- AC29 When `today.approximated` the card shows the polar marker line and the polar banner above it
  (already on screen, `app/ui/home/HomeScreen.kt:588-603`) names the latitude. The card never hides the
  marking.
- AC30 The timetable sheet and PDF mark each projected row and carry the note (AC18, AC20).
- AC31 The suhoor reminder on a projected day drops from alarm to notification unless "Ring on
  approximate days" is on, exactly as a prayer alert does (`app/notify/Notifications.kt:368-378`,
  `docs/HANDOVER.md` 5.14), and carries "Approximate time" in its sub text.
- AC32 The widgets show `notif_approximate` under the time on a projected day.
- AC33 When there is no sunrise or sunset at all the app still shows a Fajr and a Maghrib, because the
  engine projects them, and it says so. It never shows "no fast today" or a blank: that would be the app
  taking a position on a question it has already sent the user to their mosque with.

### 4.10 The eve of Ramadan notice and the date shift

`RamadanEveNotice` (`PLAN.md` A3), an amber `NoticeCard` with a close control, shown from two days
before `firstDay` through day 2, once per Hijri year, dismissible, tapping it opens the Match your mosque
chooser where the Islamic date stepper lives (`app/ui/settings/SettingsScreen.kt:989-1053`).

Title: "Ramadan on this calendar begins Mon 8 Feb" (before) or "Ramadan on this calendar began Mon 8
Feb" (days 1 and 2). Body: "Many mosques announce their own start after sighting the moon, often a day
later. If yours differs, shift the Islamic date here. That also moves the suhoor reminder and the Umm
al-Qura Isha rule."

- AC34 Shown only in the window, only until dismissed, and again the next year without any reset.
- AC35 One tap reaches the stepper. The stepper's existing bounds (minus 2 to plus 2) are enough for every
  published disagreement (`core/PrayerModels.kt:164-166`).
- AC36 The wording names what mosques do and what the app does, nothing else. "Often a day later" is
  sourced in section 6 and is a statement about announcements, not a ruling.

### 4.11 The optional suhoor reminder

Specified in `PLAN.md` A5. Off by default, 15, 30, 45 or 60 minutes before Fajr, Notification or Alarm,
Ramadan days only on the user's calendar.

- AC37 Never fires outside Ramadan and never after Fajr (the receiver drops a reminder more than five
  minutes stale).
- AC38 Obeys the silent phone switch, Do Not Disturb handling and the approximate day downgrade through
  the same `effectiveAlertStyle` as prayers.
- AC39 Title "Suhoor ends at 05:34", body "Fajr begins in 30m". No "stop eating", no "imsak".
- AC40 Its own action and request code band, cancelled with the same window as prayers, proved by
  `RequestCodeTest`.
- AC41 Turning the reminder on counts as using an alarm for the Do Not Disturb card and the sound row
  when its style is Alarm.

### 4.12 How the widgets reflect it

Section 8.

## 5. Wording rules for Ramadan, in the four languages

- English: "Suhoor ends" over Fajr, "Iftar" over Maghrib. "Sehri" appears nowhere in the app (the live
  store listing uses it once, section C of `WORDING-AND-LISTING.md` keeps that line because it is the
  South Asian search word).
- Urdu: "سحری ختم" and "افطار". Prayer names stay as the app already has them.
- Turkish: "İmsak" is the everyday word for the time the fast begins and Diyanet's own tables print it as
  the first time of the day. The app's Turkish calls Fajr "Sabah" (`core/src/main/res/values-tr/strings.xml:7`),
  so the label reads "İmsak (Sabah)" the first time and "İmsak" in the widget, and "İftar". This is the
  one language where the imsak word is right for the Fajr time itself. Flagged for review, as every
  translation here is.
- Indonesian: "Sahur berakhir" over Subuh and "Buka puasa" over Magrib. **Not** "Imsak": in Indonesia
  Imsak is printed ten minutes before Subuh by convention, so labelling Subuh as Imsak would be wrong
  by ten minutes. The Indonesian info note says timetables there print an imsak time before Subuh.
- The info note (all languages) makes four points in this order. The app shows Fajr and Maghrib as
  calculated and treats them as the end of suhoor and the time of iftar. Many timetables print an imsak
  time a few minutes before Fajr as a precaution and some mosques begin Fajr earlier than this
  calculation. SajdaTime does not add a margin. Where your mosque's timetable differs, follow it, and
  shift the Islamic date in Settings if your mosque's Ramadan began on a different day.
- Nothing says "valid", "obligatory", "must", "may" in the ruling sense, "sin", or "reward".

## 6. Per country start conventions: decision and sources

**Decision: no country table. Umm al-Qura plus the user's shift, plus the eve notice and the info note.**
Recommended to the owner as the default in `README.md`.

Reasons, in order of weight:

1. **The app does not know the country** (section 1). The only candidate is a word inside a display
   label that is in the app's language, absent for typed cities when the country was not returned, and
   "Makkah" for the fallback. Deriving a country from coordinates would need a boundary dataset the app
   does not have and the project rules do not allow adding.
2. **Even with a country, the answer is not a function of the country.** Sourced, read on 16 October
   2026: Al Jazeera, "When is Ramadan 2026, and how is the moon sighted?" (16 February 2026, updated 18
   February), and The New Arab, "When does Ramadan 2026 start and why does it vary?" (6 February 2026,
   updated 17 February). For Ramadan 1447: Saudi Arabia, the UAE, Qatar, Kuwait, Bahrain, Yemen,
   Palestine and Iraq began on Wednesday 18 February (the Umm al-Qura date). Oman, Turkey, Malaysia,
   Indonesia, Singapore, Brunei, Syria and Jordan began on Thursday 19 February after no confirmed
   sighting on the Tuesday evening. The European Council for Fatwa and Research declared 19 February. The
   Fiqh Council of North America and ISNA declared 18 February by astronomical calculation. The Australian
   National Imams Council declared 19 February. India and Pakistan were forecast for 19 February. Al
   Jazeera's own summary sentence: "Other countries follow their own moon sightings." The split is decided
   each year by that year's visibility and each body's method, so a table written in October 2026 would be
   a guess about February 2027.
3. **A table would be a ruling.** Telling a user in Jakarta "your Ramadan starts a day later than Makkah"
   is exactly the kind of decision the app refuses to make on a user's behalf (`docs/HANDOVER.md` 5.17,
   the rejected auto mapping), and the disclaimer says the app is not an authority.
4. **The existing control is sufficient and already explained.** Every published disagreement is one
   day, occasionally two (`core/PrayerModels.kt:164-166`), the stepper allows two either way, and the
   help text already says it moves Ramadan and Eid.

What the plan does instead, because the sources show that users in three of the app's four languages
(Urdu, Turkish, Indonesian) were in countries that started a day after Umm al-Qura in 2026: the eve
notice says "often a day later" and puts the stepper one tap away, and the notice returns every year.

Rejected and recorded: a per language default (Turkish app language means minus 1). Language is not
country (Turkish speakers in Germany followed the ECFR date, Urdu speakers in the Gulf followed the Gulf
date), and it would be the same ruling by a worse proxy.

## 7. Behaviour by phase of the day

| Phase (`RamadanState.phase`) | Card countdown line | Next prayer widget | Today widget |
|---|---|---|---|
| Before suhoor ends (00:00 to Fajr) | "Suhoor ends in HH:MM:SS" | Fajr, "Suhoor ends" under it | Fajr highlighted, "Suhoor ends" label on the Fajr row, "Iftar" on Maghrib |
| Fasting (Fajr to Maghrib) | "Iftar in HH:MM:SS" | Whatever is next, "Iftar" when it is Maghrib | Next highlighted, both labels shown |
| After iftar, not the last day | "Suhoor ends tomorrow at 05:08" | Tomorrow's Fajr with "Tomorrow" and "Suhoor ends" | Nothing highlighted, both labels still shown for the day's rows |
| After iftar on the last day | "On this calendar today is the last day of Ramadan. Your mosque will announce Eid." | Tomorrow's Fajr with "Tomorrow", no suhoor word | Nothing highlighted, labels shown on today's rows only |
| Eve (29 or 30 Shaban) after Isha | No card. Eve notice. | Tomorrow's Fajr with "Tomorrow" and "Suhoor ends" (because tomorrow is day 1) | No labels |

## 8. Widgets

The Today widget shows "Suhoor ends" under Fajr and "Iftar" under Maghrib whenever today is Ramadan on
the user's calendar. The Next prayer widget shows "Suhoor ends" when the next prayer is a Fajr whose own
date is Ramadan (so the eve's late evening already shows it for tomorrow) and "Iftar" when the next is
today's Maghrib and today is Ramadan. Nothing else changes: no day number on the widget, no countdown to
iftar beyond the one the widget already has for the next prayer (which during the fast **is** Maghrib for
most of the afternoon). The widget never names the place (`PLAN.md` A6, a home screen is visible to other
people).

## 9. Test vectors and golden values

### 9.1 Calendar vectors (assert in `HijriCalendarTest`)

From section 3, plus: 2026-02-17 is 29 Shaban 1447 (Shaban 1447 has 29 days), 2027-02-07 is 30 Shaban
1448, 2027-03-09 is 1 Shawwal 1448, 2027-03-10 is 2 Shawwal. With offset plus 1 on 2027-02-07 the day is
1 Ramadan. With offset minus 1 on 2027-02-08 the day is 30 Shaban.

### 9.2 Golden values for Ramadan 1448 (reference places)

How they were produced: a Java program in the planning session (`reference/Mirror.java`, kept beside this
file with `reference/Hijri.java`) mirrored `PrayerEngine.compute` line by
line (the method presets, madhab, `TWILIGHT_ANGLE`, the `isUsable` guard, the 45 or 60 degree projection,
the Jafari 4 degree Maghrib probe, the Umm al-Qura plus 30 in Ramadan, truncation to the minute, no user
minutes) against the real `adhan-java 1.2.1` jar from Maven Central. They are therefore the values the
engine should produce, **but they are a mirror, not the engine**: the implementer's first step is to run
`RamadanGoldenTest` which recomputes every row through `PrayerEngine` and compares. Any disagreement is a
mirror mistake until shown otherwise, and the engine's own answer then replaces the row here. Coordinates
and zones: London 51.5074, -0.1278 Europe/London. Makkah 21.4225, 39.8262 Asia/Riyadh. Istanbul 41.0082,
28.9784 Europe/Istanbul. Karachi 24.8607, 67.0011 Asia/Karachi. Jakarta -6.2088, 106.8456 Asia/Jakarta.
Sydney -33.8688, 151.2093 Australia/Sydney. Tromsø 69.6496, 18.9560 Europe/Oslo. Tehran 35.6892, 51.3890
Asia/Tehran. Local clock times, 24 hour. "Fast" is Maghrib minus Fajr.

**London, Muslim World League (the Sunni default), Shafi'i**

| Date | Hijri | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast | Change |
|---|---|---|---|---|---|---|---|---|---|
| 2027-02-07 | 30/8 | 05:36 | 07:29 | 12:16 | 14:35 | 17:00 | 18:48 | 11h24m | |
| 2027-02-08 | 1/9 | 05:34 | 07:28 | 12:16 | 14:36 | 17:02 | 18:50 | 11h28m | +4 |
| 2027-02-09 | 2/9 | 05:33 | 07:26 | 12:16 | 14:38 | 17:04 | 18:51 | 11h31m | +3 |
| 2027-02-21 | 14/9 | 05:12 | 07:03 | 12:15 | 14:54 | 17:26 | 19:11 | 12h14m | |
| 2027-02-22 | 15/9 | 05:10 | 07:01 | 12:15 | 14:56 | 17:28 | 19:13 | 12h18m | +4 |
| 2027-02-23 | 16/9 | 05:08 | 06:59 | 12:15 | 14:57 | 17:30 | 19:15 | 12h22m | +4 |
| 2027-03-07 | 28/9 | 04:42 | 06:33 | 12:13 | 15:12 | 17:51 | 19:36 | 13h09m | |
| 2027-03-08 | 29/9 | 04:40 | 06:31 | 12:12 | 15:13 | 17:53 | 19:37 | 13h13m | +4 |
| 2027-03-09 | 1/10 | 04:38 | 06:29 | 12:12 | 15:14 | 17:54 | 19:39 | 13h16m | +3 |

Note: the "Change" column is against the previous day and is given only where that day is tabulated. `RamadanStateTest` asserts the 22 February change of plus 4
by two engine calls as well as against this table. The daily change at London in February and March is
3 to 4 minutes.

**London, Moonsighting Committee** (the convention most London mosques follow, `docs/HANDOVER.md` 5.3)

| Date | Fajr | Maghrib | Isha | Fast |
|---|---|---|---|---|
| 2027-02-08 | 05:51 | 17:05 | 18:29 | 11h14m |
| 2027-02-22 | 05:25 | 17:31 | 18:51 | 12h06m |
| 2027-03-08 | 04:56 | 17:56 | 19:13 | 13h00m |

**Makkah, Umm al-Qura**

| Date | Hijri | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|---|
| 2027-02-07 | 30/8 | 05:38 | 06:56 | 12:35 | 15:50 | 18:14 | 19:44 (Maghrib + 90) | 12h36m |
| 2027-02-08 | 1/9 | 05:38 | 06:55 | 12:35 | 15:50 | 18:15 | 20:15 (+ 120) | 12h37m |
| 2027-02-22 | 15/9 | 05:30 | 06:47 | 12:34 | 15:54 | 18:22 | 20:22 | 12h52m |
| 2027-03-08 | 29/9 | 05:20 | 06:36 | 12:32 | 15:55 | 18:27 | 20:27 | 13h07m |
| 2027-03-09 | 1/10 | 05:19 | 06:35 | 12:31 | 15:55 | 18:28 | 19:58 (+ 90) | 13h09m |

Isha minus Maghrib at Makkah around the boundaries, by offset (asserts the shift reaches the rule):

| Date | off -2 | off -1 | off 0 | off +1 | off +2 |
|---|---|---|---|---|---|
| 2027-02-06 | 90 | 90 | 90 | 90 | 120 |
| 2027-02-07 | 90 | 90 | 90 | 120 | 120 |
| 2027-02-08 | 90 | 90 | 120 | 120 | 120 |
| 2027-02-09 | 90 | 120 | 120 | 120 | 120 |
| 2027-02-10 | 120 | 120 | 120 | 120 | 120 |
| 2027-03-07 | 120 | 120 | 120 | 120 | 90 |
| 2027-03-08 | 120 | 120 | 120 | 90 | 90 |
| 2027-03-09 | 120 | 120 | 90 | 90 | 90 |
| 2027-03-10 | 120 | 90 | 90 | 90 | 90 |

**Istanbul, Diyanet (the app's TURKEY method: Fajr 18, Isha 17, no offsets)**

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 06:34 | 08:08 | 13:18 | 16:06 | 18:29 | 19:57 | 11h55m |
| 2027-02-22 | 06:18 | 07:49 | 13:18 | 16:19 | 18:46 | 20:13 | 12h28m |
| 2027-03-08 | 05:57 | 07:28 | 13:15 | 16:30 | 19:03 | 20:29 | 13h06m |

**Karachi, University of Karachi, Hanafi**

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 05:53 | 07:11 | 12:47 | 16:45 | 18:22 | 19:39 | 12h29m |
| 2027-02-22 | 05:45 | 07:01 | 12:47 | 16:53 | 18:30 | 19:47 | 12h45m |
| 2027-03-08 | 05:33 | 06:49 | 12:44 | 16:58 | 18:37 | 19:53 | 13h04m |

**Jakarta, Kemenag and MUIS (the app's SINGAPORE method)**

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 04:36 | 05:57 | 12:08 | 15:24 | 18:17 | 19:29 | 13h41m |
| 2027-02-22 | 04:39 | 05:58 | 12:07 | 15:16 | 18:14 | 19:24 | 13h35m |
| 2027-03-08 | 04:40 | 05:58 | 12:05 | 15:07 | 18:09 | 19:18 | 13h29m |

**Sydney, Muslim World League** (southern hemisphere, daylight saving in force throughout)

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 04:51 | 06:23 | 13:10 | 16:51 | 19:55 | 21:21 | 15h04m |
| 2027-02-22 | 05:08 | 06:36 | 13:10 | 16:47 | 19:41 | 21:03 | 14h33m |
| 2027-03-08 | 05:23 | 06:48 | 13:07 | 16:39 | 19:23 | 20:43 | 14h00m |

Sydney's fast **shortens** through the month (minus 2 to minus 3 minutes a day). The card must say
"shorter than yesterday" and the test asserts a negative change here.

**Tromsø, Muslim World League** (real astronomy in February and March, nothing projected)

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 05:14 | 08:50 | 11:59 | 12:56 | 15:08 | 18:34 | 9h54m |
| 2027-02-22 | 04:20 | 07:43 | 11:59 | 13:32 | 16:13 | 19:26 | 11h53m |
| 2027-03-08 | 03:11 | 06:39 | 11:56 | 14:09 | 17:12 | 20:28 | 14h01m |

**Tromsø, Moonsighting Committee**

| Date | Fajr | Maghrib | Isha | Fast |
|---|---|---|---|---|
| 2027-02-08 | 07:05 | 15:11 | 16:39 | 8h06m |
| 2027-02-22 | 06:00 | 16:16 | 17:40 | 10h16m |
| 2027-03-08 | 04:58 | 17:15 | 18:35 | 12h17m |

Tromsø's daily change is 8 to 10 minutes: the "change from yesterday" line must not be clipped at two
digits in any language.

**Tehran, Jafari** (Maghrib is the 4 degree depression, after sunset)

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Fast |
|---|---|---|---|---|---|---|---|
| 2027-02-08 | 05:42 | 06:59 | 12:19 | 15:16 | 17:55 | 18:45 | 12h13m |
| 2027-02-22 | 05:29 | 06:44 | 12:18 | 15:27 | 18:08 | 18:58 | 12h39m |
| 2027-03-08 | 05:11 | 06:26 | 12:15 | 15:34 | 18:21 | 19:10 | 13h10m |

**Projected days at Tromsø with MWL in 2027** (for the polar tests): 1 to 21 January, 18 May to 26 July,
16 November to 31 December. So the polar Ramadan test uses a synthetic date inside one of those ranges
(for example 2027-06-15) with an offset that cannot make it Ramadan, and tests the marking logic through
`WidgetState` and `RamadanState` with a stubbed `RamadanInfo`, and additionally runs the real combination
for Ramadan 1451 at Tromsø (December 2029) where the whole month is projected, asserting every row is
marked and the reminder downgrades.

**London daylight saving (not Ramadan, invariant check):** 27 March 2027 Fajr 03:52 Maghrib 18:25 fast
14h33m. 28 March (clocks forward at 01:00) Fajr 04:49 Maghrib 19:27 fast 14h38m, change plus 5. 29 March
Fajr 04:47 Maghrib 19:28, change plus 3.

### 9.3 Invariants to assert for every reference place and every day of Ramadan 1447 and 1448

1. `Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha` (already covered by `PrayerEngineTest` ordering,
   repeated for the Ramadan range).
2. `0 < fast < 24h`, and at the reference places `9h < fast < 16h` in February and March.
3. The card's suhoor end equals the Fajr row and the iftar equals the Maghrib row, same instant.
4. Day numbers run 1 to `length` with no gap and no repeat, and `length` is 29 for 1448 and 30 for 1447.
5. With offset o, every date's day number equals the offset 0 day number of `date + o`.
6. The reminder instant is exactly N minutes before the Fajr instant of its date and is never on a date
   that is not Ramadan for that offset.
7. The Umm al-Qura Isha minus Maghrib is 120 minutes on every Ramadan day and 90 on the day before and
   after, at every offset (the table in 9.2).
8. Changing coordinates changes the times and not the day number.
9. No value in any table is affected by the device time zone except its printed form.

### 9.4 Cross check method and tolerance

> **18 Oct 2026:** the Turkey method now applies Diyanet's precaution minutes (HANDOVER §5.26), so the plain angle Istanbul figures below are history.

The project's rule: verify against an independent reference, not against reasoning
(`CLAUDE.md`, "Verify against an independent reference"). The reference is the Aladhan API during
development, never at runtime.

Fetched on 16 October 2026 for 8 February 2027 and compared with the mirror. Tolerance, as in
`PrayerEngineTest`: 2 minutes, with the known plus 1 minute on Dhuhr for methods adhan ships with a
Dhuhr adjustment (`docs/HANDOVER.md` 5.3).

| Place, Aladhan method | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha | Verdict |
|---|---|---|---|---|---|---|---|
| London, 3 (MWL) | 05:34 = | 07:28 = | 12:15 (+1) | 14:34 (+2) | 17:02 = | 18:50 = | within tolerance |
| Makkah, 4 (Umm al-Qura) | 05:38 = | 06:55 = | 12:35 = | 15:49 (+1) | 18:15 = | 20:15 = (Aladhan also applies the Ramadan 120) | within tolerance |
| Karachi, 1, school 1 (Hanafi) | 05:53 = | 07:11 = | 12:46 (+1) | 16:45 = | 18:22 = | 19:39 = | within tolerance |
| Jakarta, 20 (Kemenag) | 04:36 = | 05:57 = | 12:07 (+1) | 15:23 (+1) | 18:17 = | 19:29 = | within tolerance |
| Sydney, 3 (MWL) | 04:51 = | 06:23 = | 13:09 (+1) | 16:52 (-1) | 19:55 = | 21:21 = | within tolerance |
| Tromsø, 3 (MWL) | 05:14 = | 08:50 = | 11:58 (+1) | 12:37 (**+19**) | 15:09 (-1) | 18:33 (+1) | Fajr and Maghrib within tolerance. Asr differs by 19 minutes at 69.6 degrees in February: a shadow ratio Asr when the sun barely clears the horizon. Not a Ramadan quantity, recorded as a known polar divergence to look at separately |
| Istanbul, 13 (Diyanet, marked "experimental" by Aladhan) | 06:34 = | 08:01 (**-7**) | 13:23 (**+5**) | 16:09 (+3) | 18:36 (**+7**) | 19:57 = | **Fajr and Isha match. Sunrise, Dhuhr, Asr and Maghrib differ by what look like fixed precaution offsets.** See below |

**The Istanbul finding matters for iftar.** The app's TURKEY method is plain 18 and 17 degree angles
(`core/PrayerEngine.kt:397-398`) with no offsets. Aladhan's Diyanet output is 7 minutes later at
Maghrib. If Diyanet's own published timetable also carries those minutes (Diyanet is known to add
"temkin" precaution minutes, but Diyanet's site could not be fetched in the planning session, so this is
**not verified from the source**), a user in Turkey comparing the app's iftar with the official one
will find the app 7 minutes early, which is the one direction that is dangerous for a fast. The plan does
not change the method: that is a religious content change the owner decides (decision 3 in
`README.md`). It does make the info note and the PDF note say "follow your mosque where its timetable
differs", applies "Match your mosque" minutes to the card, and asks the implementer to compare the app's
Istanbul times with Diyanet's official page for one date before Ramadan and record the result in
`docs/HANDOVER.md` section 10, so the owner decides on evidence.

**Result of the comparison with Diyanet's own table, 16 October 2026.** Diyanet's Istanbul page
(`namazvakitleri.diyanet.gov.tr`, two URLs) returned a server error from the cloud, so the check used a
Diyanet town page that did load: Ayas, Ankara province, 16 to 18 August 2026 (Diyanet's table first, then the
engine's TURKEY method at 40.0167 N, 32.3333 E, approximate coordinates so a minute either way is expected).

| Date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha |
|---|---|---|---|---|---|---|
| 16 Aug Diyanet | 04:21 | 05:56 | 13:00 | 16:48 | 19:54 | 21:22 |
| 16 Aug engine | 04:21 | 06:03 | 12:55 | 16:43 | 19:46 | 21:21 |
| 17 Aug Diyanet | 04:23 | 05:57 | 13:00 | 16:47 | 19:53 | 21:21 |
| 17 Aug engine | 04:23 | 06:04 | 12:55 | 16:42 | 19:45 | 21:19 |
| 18 Aug Diyanet | 04:24 | 05:58 | 13:00 | 16:46 | 19:51 | 21:19 |
| 18 Aug engine | 04:24 | 06:05 | 12:55 | 16:42 | 19:44 | 21:17 |

Fajr equal on all three days, Isha 1 to 2 minutes, Sunrise 7 earlier on Diyanet's table, Dhuhr 5 later, Asr
4 to 5 later, Maghrib 7 to 8 later. The same pattern as Aladhan's Diyanet output, so the offsets are
Diyanet's own and the finding above stands: the app's iftar for Turkey is early against the ezan. Suhoor
(Fajr) is unaffected. Istanbul itself is not measured. The method is unchanged in this branch. Claude's
recommendation to the owner is to add the offsets (Sunrise minus 7, Dhuhr plus 5, Asr plus 4, Maghrib
plus 7) to the TURKEY parameters in a separate commit before Ramadan, with the Istanbul rows of
`RamadanGoldenTest` and any Turkey assertion moved in that same commit.

### 9.5 What cannot be known until the engine runs

- The exact "change from yesterday" for days whose previous day is not tabulated (derive, do not pin).
- Any value after user minutes are applied (the card shows adjusted times, the tables are unadjusted).
- Whether `desugar_jdk_libs` agrees with the JDK on the Umm al-Qura table (device test, section 3).
- The device's rendering of the chronometer at font scale 2.0 (device test).
- Whether `View.setLayoutDirection` is honoured by a given launcher (device test through
  `AppWidgetHost`).
- ~~Diyanet's own offsets (section 9.4)~~ Checked for Ayas on 16 Oct 2026, Istanbul not measured.

## 10. Open points for the owner (defaults in `README.md`)

1. No country table: confirm (default yes).
2. Ship the suhoor reminder in Release A, off by default (default yes).
3. Turkey: leave the method as plain angles and rely on the note and "Match your mosque", or verify
   Diyanet's offsets from Diyanet's own table and decide separately (default: leave, verify, record).
