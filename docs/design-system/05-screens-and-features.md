# 5. Screens and features (release 1.6.0)

What ships in 1.6.0 (versionCode 10), screen by screen, top to bottom, with every state. The watch app
is unchanged in 1.6 (1.5.0). Paths are under `app/src/main/java/com/sajdatime/app/`. Exact English
copy is in `app/src/main/res/values/strings.xml`; string keys are given as `S:key`.

Update this page in the same commit as any change to a screen.

---

## 5.1 App shell

| File | Role |
|---|---|
| `MainActivity.kt` | Routes: blank while settings load, setup if not complete, else the tabs. Wraps the context in the app's language |
| `ui/MainScaffold.kt` | The four tabs, the glass tab bar, the one snackbar, the once only usage question for people who installed before it existed, the hand off from Times to a Settings chooser |
| `ui/SajdaViewModel.kt` | The state every screen reads (times, location, settings, Ramadan, tracker) |
| `ui/RamadanState.kt` | Builds what the Ramadan card, notice and labels show for today |
| `ui/RamadanTimetable.kt` | Builds the rows of the Ramadan table |

| Tab | Label | Icon (selected Rounded / unselected Outlined) | SF Symbol | Screen |
|---|---|---|---|---|
| 1 | Times (S:`nav_times`) | `Schedule` | `clock` / `clock.fill` | `ui/home/HomeScreen.kt` |
| 2 | Qibla (S:`nav_qibla`) | `Explore` | `location.north.circle` / `.fill` | `ui/qibla/QiblaScreen.kt` |
| 3 | My prayers (S:`nav_tracker`) | `CheckCircle` | `checkmark.circle` / `.fill` | `ui/prayers/MyPrayersScreen.kt` |
| 4 | Settings (S:`nav_settings`) | `Settings` | `gearshape` / `.fill` | `ui/settings/SettingsScreen.kt` |

---

## 5.2 Times (`ui/home/HomeScreen.kt`)

The screen people open five times a day. **Must fit without scrolling** at default text: city, next
prayer, six times, mosque button.

Top to bottom:

1. **Sky** (`ui/home/Sky.kt`): the current period's sky behind the header and card, edge to edge
   under the status bar.
2. **City** (`headlineMedium`, one line) with a green caret; the whole chip opens the location sheet.
   Spoken "London. Change location". **Sky mark** at the row end when there is room.
3. **Date line**: "Thu 31 Jul · 16 Safar 1448", Gregorian in `onSurfaceVariant`, Hijri in `primary`
   (tappable, 48 target) → Islamic dates sheet. Without a Hijri date: Gregorian only, not tappable.
4. **Next prayer card** (see `03-components.md`).
5. **Notices**, in order, each 16 above: Times for Makkah (warning, not closable); Approximate times
   (warning, Details); Eid Mubarak (quiet, footnote, closable, once per Eid; `ui/home/EidCard.kt`);
   Ramadan notice or card (`ui/home/RamadanCard.kt`); Allow exact alarms (permission card, closable);
   Allow notifications (permission card, only while blocked and an alert is on).
6. **Today** (`titleMedium`, heading) with a compact "Timetable" button at the end (under it from
   1.5× text) → Timetable sheet, or the Ramadan table during Ramadan with the Ramadan card on.
7. **Today list**: six rows with Next and Now, notes (jama'ah, Jumu'ah, Suhoor ends, Iftar) and bells.
8. **Mosque button**: full width "Different from your mosque?" with the mosque icon → the mosque
   dialog. Above 45° latitude with the method still Automatic it becomes a warning notice "Does this
   match your mosque?" with "See why" and a close X.

| State | What shows |
|---|---|
| No location | Card says "Set your location to see prayer times."; no list, no mosque button |
| Makkah fallback | Makkah times and the warning notice |
| After Isha | "Next prayer · Tomorrow", Fajr; no row marked Next |
| Ramadan card off | No card, notice or Ramadan labels; Timetable opens the month view |
| Offline | No change |
| 1.5× text | Today line and rows stack |
| Calm screen | Google's review card may appear (rules in §5.9) |

### Sheets and dialogs from Times

| Surface | File | Content |
|---|---|---|
| Location sheet | `ui/components/Common.kt` | "Your location", body; "Use my current location"; "Or type where you are", a field with an example, "Find place" once text is typed; spinners while working. Closes when a location lands |
| Islamic dates sheet | `ui/home/IslamicDatesSheet.kt` | "Islamic dates", "Today is 12 Jumada al-Ula 1448", a card of upcoming dates soonest first with "Today", "Tomorrow" or "in 12 days"; six shared dates plus five for Shia users only; footer on the calendar; "Shift the date" |
| Timetable sheet | `ui/home/TimetableSheet.kt` | "Timetable" with the X, place and "Approximate area", compact "Save as PDF"; this month and next as day cards (today in `primaryContainer` with a Today pill), sticky month headings, opens at today |
| Ramadan timetable | `ui/home/RamadanTimetableSheet.kt` | "Ramadan 1448", a table: Day, Date, Suhoor ends, Iftar, Fast; today highlighted and bold; notes; "Shift the date" |
| Save your timetable | `ui/home/HomeScreen.kt` | Today only, The next 7 days, This whole month, Ramadan (from 30 days before). Saves to Downloads on Android 10+, else the share sheet |
| Mosque dialog | `ui/home/HomeScreen.kt` | Four paragraphs, each with its button: add jama'ah times, calculation method, school of thought, match your mosque start times; ends "follow your mosque" |
| Ramadan info dialog | `ui/home/RamadanCard.kt` | How the times are worked out, with "Islamic date" and "Hide this card" |

### Ramadan card (during Ramadan, card on)

"Ramadan" and "Day 3 of 30"; two tiles, "Suhoor ends" (Fajr) and "Iftar" (Maghrib), the next one in
`primaryContainer` with a Next pill; a countdown line in `primary`; the fast length and its change
from yesterday; tomorrow's times; on days 1 and 2 "Did your mosque begin Ramadan on a different day?"
with "Shift the date"; then "Get the full Ramadan timetable" and an (i) for the info dialog. No imsak
margin, never "stop eating".

---

## 5.3 Qibla (`ui/qibla/QiblaScreen.kt`)

1. "Qibla" title.
2. Without a location (or on the Makkah fallback): one warning notice, "Set location". Nothing else.
3. **Direction sentence** first, `headlineMedium`, tabular: "Turn right 58°", "You are now facing the
   Kaaba" (green, with a tick) or, without a compass, "Face 118° from true north". Polite live region.
   Under it "Facing 64°".
4. No compass: a warning notice.
5. **The dial** (see `03-components.md`).
6. **Legend**: the Kaaba, and "Where you are facing".
7. **Place** with a pin.
8. **Details** (compact, centred): "About 4,512 km from here" (miles in the US, UK, Liberia and Myanmar,
   by phone region) and "118° from north".
9. Accuracy notices: low ("needs calibrating", figure of eight), medium (move away from metal).

Arrival: three signals that are not colour (the sentence and tick, the thicker bezel, one buzz).

---

## 5.4 My prayers (`ui/prayers/MyPrayersScreen.kt`, `ui/home/MyPrayers.kt`)

A private daily checklist. Off by default. Records, never judges: no "missed", no counts of unticked
prayers, no year grid of blanks, no reminders to tick.

- **Off:** one card with the "My prayers" switch ("Tick each prayer you pray. Only you can see it."),
  then "Nothing is tracked until you switch this on."
- **On:**
  1. "Today" with the date.
  2. Notices if needed: no location (warning), a record from a newer version (quiet, read only).
  3. **Today card**: optional "Last night's Isha" after midnight; five rows (no Sunrise): tick, name,
     optional Now pill, time. A prayer cannot be ticked before it is due.
  4. Compact "Pause" and "Last 30 days"; while paused, "Tracking is paused" and "Resume". Pause here
     means from today, with "Paused from today" and Undo.
  5. **This week**: seven rings, first day of week from the language, short day names, today with a
     halo. Optional streak line (off by default).
  6. **Fasts to make up** (only above zero): "3 fasts to make up" and "Made up one". No Undo.
- **Last 30 days sheet**: a calendar grid of rings, any day selectable; the chosen day shows its five
  ticks, editable unless paused or read only.

Data stays in its own store, never in backup, never in usage counts; it can be saved to and loaded
from a JSON file the user keeps.

---

## 5.5 Settings (`ui/settings/SettingsScreen.kt`, drawn by `ui/settings/SettingsList.kt`)

Title, permission cards (exact alarms; Do Not Disturb access when an alarm is used; never closable
here), then five groups. Every row shows its current value.

| Group | Rows |
|---|---|
| Prayer times | Location; School of thought; Calculation method; Jama'ah times; Match your mosque start times; Islamic dates |
| Appearance | Theme (inline selector: Follow phone, Light, Dark; widgets follow it); Language (`ui/settings/LanguageChooser.kt`, `ui/settings/AppLanguage.kt`) |
| Reminders | Prayer alerts; Notification sound and Alarm sound (only when that style is used); Reminder before prayer; Next prayer in notifications (on); Suhoor reminder and Ramadan card (only from 3 days before Ramadan to its end) |
| My prayers | My prayers switch; when on: Show streak, Fasts to make up, Pause or Resume tracking, Save to a file, Load from a file; Delete my prayer record (when a record exists) |
| About | Disclaimer (**first, enforced**); Version; Privacy; Send feedback; Rate SajdaTime on Google Play; Share SajdaTime; Share usage counts; Free, forever; Made by; Data and libraries; Adhan recordings |

| Chooser | Content |
|---|---|
| School of thought | Sunni or Shia; for Sunni a "Madhab" section: Hanafi (later Asr) or Shafi'i, Maliki, Hanbali (standard Asr). Saves each tap |
| Calculation method | Automatic first, then the methods offered to the sect, each with a line. Closes on pick |
| Jama'ah times | Fajr to Isha and Jumu'ah, each Off, a fixed time, or minutes after it begins (0 to 60 in fives); a warning under a time outside its prayer; a quiet notice instead while in another time zone |
| Match your mosque start times | "Use your mosque's board" (pick a prayer, set the board's time, minutes fitted); six steppers ±30; at the limit a note and "Choose a calculation method"; "Islamic date" stepper ±2 days; "Reset all" |
| Prayer alerts | Each prayer: Off, Notification, Alarm chips; "Ring on approximate days" for polar users with an alarm |
| Sounds | Adhan (the school's recording) or from your phone; "Play on silent" for alarms |
| Reminder before prayer | Off, 5 to 30 minutes; then a switch per prayer |
| Suhoor reminder | Off, 15 to 60 minutes (60 default); Notification or Alarm; tone or Adhan |
| Language | Phone language first, then each language in its own script with the English name. No flags |
| Disclaimer | Seven paragraphs, the last the dua request; "Got it" |
| Fasts to make up | Stepper 0 to 999 |
| Pause from | Today or Yesterday, "Pause" |
| Delete your prayer record? | The consequence, "Delete" (green; the words warn) |

---

## 5.6 Setup (`ui/onboarding/OnboardingScreen.kt`)

Full screen steps (24 side, 32 vertical padding), title `headlineMedium`, body `bodyLarge`, footer at
the foot. Slide and fade between steps; Back steps back.

1. **Welcome**: the Bismillah in Arabic (spoken in full), "SajdaTime" (`displayMedium`), "Your private
   prayer companion. Free, always.", "Begin" (56). A compact globe button top end with the language's
   own name opens the language list.
2. **Where are you praying?**: a location badge; "Allow location" (56); "How your location is used"
   (explainer with "Got it"); or type a place; once found a green check with the place and "Continue";
   at the foot "Skip for now and use Makkah".
3. **Which school do you follow?**: Sunni or Shia cards; Back and Continue (equal, 56).
4. **Select your madhab** (Sunni only): four cards, Shafi'i preselected.
5. **Calculation method**: the same list as Settings, Automatic preselected.
6. **Help improve SajdaTime?**: the usage question (skipped if already Yes).
7. **All set**: a summary card; on Android 12+ "One last thing" with "Allow exact alarms"; a hint about
   notifications; "Finish" (56), which asks for notification permission on Android 13+.

No disclaimer at first launch since 1.6 (it lives in About).

---

## 5.7 Features

Each: what the user gets, where it lives, the logic, the iPhone effect.

| Feature | What | Logic | iPhone |
|---|---|---|---|
| Prayer times | Six times calculated on the phone from location, sect, madhab, method, minutes and date shift. Never the network | `core/.../PrayerEngine.kt` | Carries over (swap the Java `adhan` library and `java.time`) |
| Next prayer and countdown | Hero card, hh:mm:ss, Tomorrow after Isha | `PrayerEngine` | Carries over |
| Now and Next | Marks on rows; never a ruling | `PrayerEngine.currentPrayer` | Carries over |
| Four skies and sky mark | Period sky; arc of the day | `core/.../SkyPeriod.kt` | Carries over |
| Bell on each row | One tap alert toggle with Undo; takes and returns the reminder | `core/.../BellRules.kt` | Carries over |
| Prayer alerts | Off, Notification or Alarm per prayer; alarm can sound on silent and through Do Not Disturb | `notify/` | Partly: no exact alarms or DND bypass on iPhone; Time Sensitive notifications |
| Built in adhan | One Sunni and one Shia recording | `data/BuiltInAdhan.kt` | Carries over (iPhone notification sounds are limited to 30 seconds) |
| Reminder before prayer | Quiet notification 5 to 30 minutes before | `notify/PrayerReminderPlan.kt` | Carries over |
| Next prayer badge | Silent ongoing notification with a countdown, on by default | `notify/OngoingBadge.kt` | Android only; iPhone equivalent is a Live Activity |
| Location | Coarse, foreground, on every open; place label only when none or moved 5 km; search; Makkah fallback | `data/LocationRepository.kt`, `core/.../PlaceLabelPolicy.kt` | Carries over |
| Match your mosque | ±30 minutes per prayer or from the board; date shift ±2 | `core/.../AdjustmentFit.kt` | Carries over |
| Jama'ah times | Congregation times as a second line; Jumu'ah on Fridays; hidden in another time zone | `core/.../JamaahTimes.kt` | Carries over |
| Islamic dates | Hijri date (Umm al-Qura with the shift); upcoming key dates | `core/.../HijriCalendar.kt`, `core/.../IslamicDates.kt` | Carries over (swap `HijrahDate` for Foundation's Islamic Umm al-Qura calendar) |
| Ramadan | Notice, card, labels, table, suhoor reminder; can be hidden | `ui/RamadanState.kt`, `RamadanInfo` | Carries over |
| Eid greeting | Quiet card once per Eid | `HijriCalendar.eidOn` | Carries over |
| Timetable and PDF | Two months of day cards; PDF of a day, 7 days, month or Ramadan | `core/.../TimetableList.kt`, `pdf/PrayerPdfExporter.kt` | Carries over (`UIGraphicsPDFRenderer`, same A4 metrics) |
| Polar handling | Projected Fajr and Isha marked Approximate everywhere | `PrayerEngine` | Carries over |
| Qibla | Sentence, dial, legend, details, calibration, arrival buzz | `core/.../QiblaEngine.kt` | Carries over (Core Location heading) |
| My prayers | Ticks, pause, streak (opt in), rings, 30 day grid, file save and load, delete | `core/.../TrackerLogic.kt` | Carries over (JSON file format is portable) |
| Fasts to make up | One number with a stepper and "Made up one" | `TrackerLogic` | Carries over |
| Theme | Follow phone, Light, Dark | `ui/theme/Theme.kt` | Carries over |
| Languages | English, Urdu, Turkish, Indonesian, Arabic; RTL for Urdu and Arabic | `core/.../AppLocale.kt` | Carries over (iOS per app language setting) |
| Usage counts | Opt in, fixed events only | `data/UsageCounts.kt` | Needs the owner's decision and a new privacy review |
| Widgets | Next prayer, Today, Today strip | `widget/` | Android only now; WidgetKit later (`08-surfaces.md`) |
| Wear OS | Times, Qibla, tile | `wear/` | Android only; watchOS later |
| Tablet | Content capped at 600, centred | `Modifier.readingWidth()` | Carries over (iPad) |

### Defaults

Sunni, Shafi'i, Automatic method; all five alerts as Notification with the phone's tone; Play on silent
off; next prayer badge on; reminder before prayer off; suhoor reminder 60 minutes; Ramadan card on;
theme Follow phone; My prayers off, streak off; no jama'ah times; usage counts off.

---

## 5.8 Not in 1.6 (do not build without the owner)

| Item | Status |
|---|---|
| Jama'ah reminder; Sunnah fast reminders | 1.6.1. Logic built and switched off, no screens or strings |
| Tasbih counter | Later candidate only; if ever built: off by default, no counts to beat, no streak |
| Adhan choice per prayer | 1.7 candidate |
| Jama'ah on widgets or the watch | Out of 1.6 |
| Shia Ramadan nights in the dates sheet | Left out of 1.6 |
| Offline nearest city list (no network for the place label) | The owner's decision |
| Year view of the tracker | Separate decision |
| Never | A missed or qada prayer counter; silencing the phone for jama'ah; maps, weather, live counters, premium, share or rate prompts in our words, a day stepper, endless animation |

## 5.9 Google review card (`review/ReviewPrompt.kt`)

Google's own card, only on a calm Times screen (no sheet, dialog, notice or snackbar, a real
location) after 5 seconds. Due after 14 days installed and 3 days opened, at most 3 times ever, 120
days apart, never from 20 minutes before to 30 minutes after a prayer time, never midnight to sunrise,
never from My prayers. No question of ours before it, no button calling it.
