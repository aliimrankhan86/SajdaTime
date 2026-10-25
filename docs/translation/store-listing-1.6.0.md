# Store listing 1.6.0: back translations, word choices and counts (25 Oct 2026)

The paste-ready text is in `docs/store/LISTING-LOCALES.md`, "1.6.0 listings". This file is the record of how it was made,
so the next person can check a phrase without reading the language.

**Method.** Same as 1.5.0 (`store-listing-1.5.0.md`). Source: the owner approved English 1.6.0 full description, short
description and What's new (`docs/store/LISTING.md`, "1.6.0 listing"). Each language's 1.5.0 full description was the
starting point, and only the lines whose meaning the English changed were changed. Everything from the HONEST heading to
the end (the disclaimer, the accuracy paragraph, FREE and the dua) was not retyped: a script appended the 1.5.0 bytes, so
those sections cannot differ. For the four app languages (Arabic, Urdu, Turkish, Indonesian) the new feature names come
from the app's own strings (`nav_tracker`, `action_timetable`, `settings_jamaah`, `jamaah_line`, `jumuah_line`,
`tracker_open_history`, `action_save_pdf`, `widget_strip_label`, `settings_islamic_dates`, `nav_times`). Translated by
Claude and back translated by the same session, by the owner's rule of 9 Oct 2026 (no native speaker review). **Not done
here:** the independent second agent review that 1.5.0 had; that is the reviewer's job before commit.

Below, for each language: the back translation of every new or changed line (full description, short description and
What's new), then notes. Unchanged lines are not repeated; they are the 1.5.0 text and its back translation stands.

---

## Measured counts (Python `len`, newlines counted, no final newline)

Read back out of `docs/store/LISTING-LOCALES.md` after the edit, by a script that also compares each 1.6.0 full
description from the HONEST heading to the end with the 1.5.0 one byte for byte:

```
language             title short  full  whats_new  honest_free_identical title_unchanged wear_os chrome_edge
Arabic (ar)             30    69  3736        459  True                  True                  3 False
Urdu (ur)               30    75  3988        494  True                  True                  3 False
Turkish (tr-TR)         26    80  3987        498  True                  True                  2 False
Indonesian (id-ID)      24    80  3984        496  True                  True                  2 False
Malay (ms-MY)           30    76  3988        494  True                  True                  2 False
Bengali (bn-BD)         27    76  3981        490  True                  True                  3 False
French (fr-FR)          29    77  3986        498  True                  True                  2 False
Hindi (hi-IN)           23    80  3987        499  True                  True                  3 False
```

Limits: title 30, short 80, full 4000 (aim 3990, all within), What's new 500. Every text is already NFC, so the Hindi and
Bengali counts are not changed by normalisation. No hyphen or dash was added; the only hyphenated words are ones 1.5.0
already had (Umm al-Qura, Aïd al-Fitr, Aïd al-Adha, Turkish "e-posta", Malay "e-mel") plus "adhan-java" in the untouched
tail. No rating or sharing ask, no promise of acceptance or reward, no Turkey line in What's new, no Chrome or Edge line.

---

## Religious terms and shared word choices

- **My prayers (the tracker).** It is a record the user ticks, never a judgement. Every language calls it a private record
  or tracker of *your prayers*: Arabic «صلواتي» "متتبّع خاص لصلواتك" / "سجل خاص لصلواتك"; Urdu میری نمازیں "نجی نماز
  ٹریکر"; Turkish Namazlarım "size özel namaz takibi"; Indonesian Salat saya "pencatat sholat pribadi"; Malay Solat saya
  "penjejak solat peribadi"; Bengali আমার নামাজ "ব্যক্তিগত নামাজ ট্র্যাকার"; French Mes prières "suivi privé"; Hindi मेरी
  नमाज़ें "निजी नमाज़ ट्रैकर". Nothing says a prayer was accepted, counted for reward, or "completed"; no streak word.
- **Jama'ah and iqamah.** Arabic الجماعة (الإقامة); Urdu جماعت (اقامت); Turkish cemaat (kamet), not "ikamet", which in
  everyday Turkish means residence; Indonesian berjamaah (iqamah), the app's word; Malay berjemaah (iqamah); Bengali জামাত
  (ইকামত); French jama'ah (iqama); Hindi जमाअत (इक़ामत). In What's new every language says "add" (أضف، شامل کریں، ekleyin,
  Tambah, Tambah, যোগ করুন, Ajoutez, जोड़ें), so the times read as the user's own entries, never a mosque feed.
- **Jumu'ah.** Arabic صلاة الجمعة / الجمعة (app: `jumuah_line` الجمعة); Urdu جمعہ; Turkish Cuma; Indonesian Jumat; Malay
  Jumaat; Bengali জুমা; French Joumou'a (the French Muslim spelling); Hindi जुमा.
- **Islamic dates of the year.** Arabic التواريخ الإسلامية; Urdu اسلامی تاریخیں; Turkish İslami tarihler; Indonesian tanggal
  Islam (all four are `settings_islamic_dates`); Malay tarikh Islam; Bengali ইসলামি তারিখ; French dates islamiques; Hindi
  इस्लामी तारीख़ें. Kept neutral, naming no occasion, because the Shia and Sunni dates differ.
- **Timetable.** App words where the app has the language: جدول المواقيت، اوقات نامہ، Vakit çizelgesi, Jadwal. Ramadan's
  timetable uses the local search word where it fits: Arabic إمساكية رمضان, Turkish Ramazan imsakiyesi (the app's own
  `ramadan_open_timetable` word), Indonesian imsakiyah Ramadan.
- **Bell.** جرس، گھنٹی، zil, Lonceng, Loceng, ঘণ্টা, cloche, घंटी: the plain word for the icon.
- **Five prayer strip.** Arabic "شريط للصلوات الخمس" / "ودجة ... للصلوات الخمس في سطر" (app: "الصلوات الخمس في سطر"); Urdu
  پانچ نمازوں کی پٹی; Turkish beş vakit şeridi (app: "beş vakit tek satırda"); Indonesian baris lima waktu (app: "salat
  lima waktu sebaris"); Malay baris lima waktu; Bengali পাঁচ ওয়াক্তের সারি; French bande des cinq prières; Hindi पाँच
  नमाज़ों की पट्टी.
- **"Time of day" (the skies).** Rendered as which part of the day it is, never as "shows the clock": Arabic وقت النهار أو
  الليل, Urdu دن کا پہر, Turkish günün hangi vakti olduğu, Indonesian mengikuti waktu hari, Malay ikut waktu hari, Bengali
  দিনের কোন সময়, French le moment du jour, Hindi दिन का पहर.
- **The disclaimer and the dua** are untouched in all eight (see the counts table). The closing "where your mosque
  differs, follow your mosque" sentence of each What's new reuses that language's own disclaimer wording.

---

### Arabic (ar), 1.6.0

Room was not a problem (3736), so nothing was cut: both opening paragraphs, "أينما كنت", "لموقعك", "صيفًا", the method line
and the usage counts line are the 1.5.0 text.

Full description, changed and new lines:
- • The Hijri date (Islamic calendar), and the Islamic dates throughout the year
- • The timetable for this month and the month after it, and the Ramadan imsakiyah, to save or print as a PDF
- Heading: MY PRAYERS AND YOUR MOSQUE
- • "My prayers": a private tracker of your prayers, for this week and the last 30 days, kept on your phone
- • The jama'ah (iqamah) and Friday prayer times at your mosque, under each prayer
- • A bell on each prayer to turn its alert on or off
- • Home screen mini apps (widgets): a big countdown to the next prayer, today's times, and a strip for the five prayers, with your town's name, in the app's own look
- Removed: "Coming soon to the Chrome and Edge browsers"

Short description (69): Prayer times, the adhan and the Qibla without net, and a private prayer tracker, with Wear OS.

What's new (459): "My prayers": a private record of your prayers, for this week and the last 30 days, kept on your phone. A
bell on each prayer turns its alert on or off. "Timetable": this month and the month after it, in one card for each day,
with saving as PDF. Add your mosque's jama'ah and Friday times under each prayer. The Hijri date opens the Islamic dates of
the year. And the Qibla now tells you which way to turn first. The Times screen and the next prayer widget show the time
of day or night. A new widget with the five prayers in a row. And where your mosque differs, follow your mosque.

Notes: "متتبّع" (tracker) is the word Arabic prayer apps use (متتبع الصلاة), added to the search line. "ودجة" for widget in
What's new follows the 1.5.0 Arabic What's new ("ودجات"); the full description keeps its 1.5.0 "تطبيقات مصغّرة". Every line
still starts with an Arabic letter or «, so right to left holds. Sunni and Shia left the short description, as in English;
with them it measured 84.

### Urdu (ur), 1.6.0

Full description, changed and new lines:
- (first paragraph) On your phone and Wear OS watch, SajdaTime gives Sunni and Shia Muslims prayer times, adhan alerts and a Qibla compass. No adverts, no account, and the prayer times are worked out on your own phone, so they work offline and with no signal.
- Removed: the separate "whether you say namaz or salat" paragraph (folded into the first, as in English)
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, and a moment by moment countdown to the next prayer ("wherever you are" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates of the whole year
- • This month's and next month's timetable (auqat nama), and Ramadan's too, to save or print as a PDF
- Heading: MY PRAYERS AND YOUR MOSQUE
- • "My prayers": a private namaz tracker, of this week and the last 30 days, which stays on your phone
- • Under each prayer, your mosque's jama'ah (iqamah) and Jumu'ah times
- • A bell on each prayer, with which its alert goes on or off
- • Sehri and iftar times, countdown, and the length of each fast ("for your location" dropped)
- • Widgets: a big countdown to the next prayer, today's times and a five prayer strip, with your town's name ("home screen" and "in the app's theme" dropped)
- Removed: "Soon on the Chrome and Edge browsers too"
- • Sensible Fajr and Isha times in the far north ("in summer" dropped)
- • Optional usage statistics, which stay off without your saying yes. No crash reporting ("under your control in Settings" dropped)
- • ...usually Google. The privacy policy explains this (was "explains exactly what this means")
- Don't want to share your location? Write your town's name, which is looked up online once.

Short description (75): Offline prayer times, adhan, Qibla and a private namaz tracker, Sunni and Shia, Wear OS app.

What's new (494): My prayers: a private record of your prayers, this week and the last 30 days, kept on your own phone. A
bell on each prayer turns its alert on or off. Timetable (auqat nama): this month and the next, with a card for each day,
and Save PDF. Add your mosque's jama'ah and Jumu'ah times under each prayer. The Hijri date opens the year's Islamic dates.
The Qibla now tells you which way to turn first. The Times screen and the next prayer widget show the part of the day. A
five prayer widget strip. Where your mosque differs, follow your mosque.

Notes: "نماز ٹریکر" is the term Urdu users type; "نجی" (private) rather than "ذاتی" (personal) because the point is that
nobody else sees it. The suhoor line keeps 1.5.0's "an hour before", which is the default and is still true. "پہر" (part
of the day) avoids reading as a clock.

### Turkish (tr-TR), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime offers Sunni and Shia Muslims prayer times, adhan time alerts and a Qibla compass on your phone and Wear OS watch. No ads, no account. The times are calculated on your phone, so they work offline, even with no signal. ("Android" and the "namaz or salat" paragraph went, as in English)
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, with a live countdown to the next prayer ("for where you are" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates of the year
- • This month's and next month's timetable, with the Ramadan imsakiye, save as PDF and print
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers (Namazlarım): prayer tracking just for you, this week and the last 30 days, only on your phone
- • Under each prayer, your mosque's jama'ah (kamet) and Friday times
- • On each prayer, a bell that turns its alert on and off
- • Sahur and iftar times, countdown and the length of each fast ("for your location" dropped)
- • Widgets: a big countdown to the next prayer, today's times and a five prayer strip, with your city's name
- Removed: "Soon in the Chrome and Edge browsers"
- • Turkish, English, Arabic, Urdu and Indonesian, chosen in the app or following the phone's language
- • Sensible Fajr and Isha times in the far north ("in summer" dropped)
- • Optional usage counts, off unless you say yes. No crash reports
- • ...(usually Google). The privacy policy explains this

Short description (80): Offline prayer times, adhan, Qibla and prayer tracking just for you. With Wear OS.

What's new (498): My prayers: a record of your prayers just for you, this week and the last 30 days, on your phone. The
bell on each prayer turns its alert on or off. Timetable: this month and next month, a card for each day, with the option
to save as PDF. Add your mosque's jama'ah and Friday times under each prayer. The Hijri date opens the Islamic dates of the
year. The Qibla now tells you which way to turn first. Times and the next prayer widget show which part of the day it is.
A five prayer widget strip. If your mosque differs, follow your mosque.

Notes: "namaz takibi" is the Turkish search term (added to the search line). "size özel" (just for you) rather than "özel"
alone, which can read as "special". "kamet" is the Turkish word for the iqamah call. "Ramazan imsakiyesi" is a top Turkish
Ramadan search and the app's own word.

### Indonesian (id-ID), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime gives prayer times (salat), adhan reminders and a Qibla direction compass for Sunni and Shia Muslims on your phone and Wear OS watch. No ads, no account, and the times are calculated on your phone, so it keeps working offline.
- Removed: the separate "Sholat, salat or namaz" paragraph (folded in, "namaz" dropped for room)
- • Subuh, Sunrise, Zuhur, Asar, Magrib, Isya, and a live countdown to the next prayer ("at your location" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates through the year
- • This month's and next month's timetable, and the Ramadan imsakiyah, as PDF, to save or print
- • Adhan (azan) built in for Sunni and Shia, or any sound on the phone
- • A reminder 5 to 30 minutes before the prayers you choose ("optional" dropped, as in English)
- • Silent notification: the next prayer and its countdown
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers (Salat saya): a private prayer record keeper, this week and the last 30 days, stored on the phone
- • Your mosque's congregational (iqamah) and Friday times under each prayer
- • A bell on each prayer to switch its reminder on or off
- • Sahur and iftar, countdown, and the length of each fast ("at your location" dropped)
- • Widget: a big countdown to the next prayer, today's times, and a five prayer row, with the town's name
- Removed: "Coming soon to the Chrome and Edge browsers"
- • Indonesian, English, Arabic, Urdu and Turkish, choose in the app or follow the phone
- • Light and dark themes, large text, and screen readers
- • Sunni: ...with the right Asr rule for each madhab; Shia: Jafari (Ithna Ashariyah), with the correct Maghrib rule
- • Optional usage data, off unless you agree. No crash reports ("that you set in Settings" dropped)
- • Approximate location, read while the app is open. (coordinates sentence unchanged)
- • The town name is looked up by the phone's built in place search service, usually Google. (rest unchanged)
- • Cloud backup off: nothing is copied to a server
- Don't want to share location? Type the town name, looked up once online.

Short description (80): Offline prayer times, adzan, Qibla direction, and a private prayer record keeper. Wear OS.

What's new (496): My prayers (Salat saya): a private prayer record, this week and the last 30 days, stored on the phone. A
bell on each prayer switches its reminder on or off. Timetable (Jadwal): this month and next month in daily cards, and
Save PDF. Add your mosque's congregational and Friday times under each prayer. The Hijri date opens the year's Islamic
dates. Qibla now shows which way to turn first. Times and the next prayer widget follow the time of day. A five prayer row
widget. If your mosque differs, follow your mosque.

Notes: the listing spells "sholat" (the search spelling) and the app spells "salat"; the feature name "Salat saya" keeps
the app's spelling so a user finds the same words on screen. "pencatat sholat" (prayer record keeper) is the plain
Indonesian; Indonesian users also search "tracker sholat", left out for room. "Waktu" in What's new is the Times tab's own
name (`nav_times`). Still no far north bullet (room), as in 1.5.0.

### Malay (ms-MY), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime gives prayer times, azan alerts and a Qibla direction compass for Sunni and Shia Muslims on your phone and Wear OS watch. No ads, no account, and the times are calculated on your phone, so it works offline.
- Removed: the separate "Solat, salat or namaz" paragraph (folded in; the alternative spellings dropped for room, Malaysian searches use "solat")
- • Subuh, Syuruk, Zohor, Asar, Maghrib and Isyak, with a countdown to the next prayer ("at your location" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates through the year
- • This month's and next month's prayer timetable and Ramadan, in PDF, to save or print
- • Built in azan for Sunni and Shia, or any sound on the phone
- • A reminder 5 to 30 minutes before the prayers of your choice ("optional" dropped)
- • Silent notification: the next prayer and its countdown
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers (Solat saya): a private prayer tracker, this week and the last 30 days, kept on the phone
- • Your mosque's congregational (iqamah) and Friday times under every prayer
- • A bell on every prayer to switch its reminder on or off
- • Sahur and iftar, countdown and the length of each fast ("at your location" dropped)
- • Widget: a big countdown to the next prayer, today's times and a five prayer row, with the town's name
- Removed: "Coming to the Chrome and Edge browsers"
- • Light and dark themes, large text and screen readers
- • Shia: Jafari / Ithna Ashari, with the correct Maghrib rule
- • Optional usage counts, off unless you agree. No crash reports
- • Approximate location, read while the app is open. (coordinates sentence unchanged)
- • The town name is looked up by your phone's place service, usually Google. (rest unchanged)
- • Cloud backup off: nothing is copied to a server
- Don't want to share location? Type the town name, looked up once online.

Short description (76): Prayer times, azan, Qibla direction and a private prayer tracker, also on Wear OS.

What's new (494): My prayers: a private prayer record, this week and the last 30 days, on your phone. A bell on every
prayer: switch its reminder on or off. Timetable for this month and next month: daily cards, save PDF. Add your mosque's
congregational and Friday times under each prayer. The Hijri date opens the Islamic dates of the year. Qibla now tells you
the direction to turn first. The times screen and the next prayer widget now follow the time of day. A five prayer row
widget. If your mosque differs, follow your mosque.

Notes: the app has no Malay, so the feature names are translated, not quoted ("Solat saya" for My prayers, "Jadual" for
Timetable), and "Bahasa aplikasi ... (tiada Melayu)" still says the app is not in Malay. "penjejak solat" is the Malaysian
word for tracker. "berjemaah" is the Malaysian spelling (Indonesian "berjamaah"). Still no far north bullet (room).

### Bengali (bn-BD), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime brings namaz (salat) timetables, adhan alerts and a Qibla compass for Sunni and Shia Muslims, on your phone and Wear OS watch. No adverts, no account needed, and the times are worked out on your phone, so offline, it works even with no network.
- Removed: the separate "namaz, salah or salat" paragraph (folded in)
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, with a live countdown to the next prayer ("wherever you are" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates of the whole year
- • This month's, next month's and Ramadan's timetable, to save or print as a PDF
- • A reminder 5 to 30 minutes before the prayers you choose ("optional" dropped)
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers: a personal namaz tracker, this week and the last 30 days, stays on your phone
- • Under each prayer, your mosque's jamaat (iqamat) and Jumu'ah times
- • A bell on each prayer, to turn its alert on or off
- • Sehri and iftar times, countdown, and how long each fast is ("for your location" dropped)
- • Widgets: a big countdown to the next prayer, today's times and a row of the five waqt, with your town's name
- Removed: "Soon on the Chrome and Edge browsers"
- • ...not in Bengali. Choose the language in the app or by the phone
- • Light and dark themes, large text and screen readers
- • Sensible Fajr and Isha times even in the far north ("in summer" dropped)
- • Optional usage counts, off unless you agree. No crash reporting ("under your control in Settings" dropped)
- • ...usually Google. What this means is explained in the privacy policy (was "what exactly this means")
- Don't want to share your location? Write the town's name, it is looked up online once.

Short description (76): Offline namaz times, adhan, Qibla compass and a personal namaz tracker, Wear OS.

What's new (490): My prayers: a personal record of your prayers, this week and the last 30 days, kept on the phone. The bell
on each prayer turns its alert on or off. Timetable: this month and next month, in daily cards, with the option to save as
PDF. Add your mosque's jamaat and Jumu'ah times under each prayer. Open the Hijri date and the year's Islamic dates appear.
The Qibla now tells you which way to turn first. The times screen and the next prayer widget show what time of day it is.
A five waqt widget row. If your mosque differs, follow the mosque.

Notes: "জামাত" and "ইকামত" are the Bangladeshi spellings; "জুমা" for Jumu'ah; "ওয়াক্ত" (waqt) for each of the five, as
the 1.5.0 text already does. "ব্যক্তিগত" (personal) is the usual Bengali for a private record. Bengali digits (৩০) as the
1.5.0 text uses them.

### French (fr-FR), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime: prayer times (salat), adhan alerts and a Qibla compass for Sunnis and Shias, on phone and Wear OS watch. No ads and no account, calculated on the phone, so offline.
- Removed: the separate "Namaz, salah ou salat" paragraph (folded in; "namaz" and "salah" dropped for room)
- • Fajr, sunrise, Dhuhr, Asr, Maghrib and Isha, with a live countdown to the next prayer ("wherever you are" dropped)
- • Hijri date (Islamic calendar) and the Islamic dates of the year
- • Times for the month, the next one and Ramadan, as a PDF to save or print
- • A reminder 5 to 30 minutes before the chosen prayers ("optional" dropped)
- • Silent notification: next prayer and countdown
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers: private tracking of your prayers, week and last 30 days, on the phone
- • Your mosque's jama'ah (iqama) and Jumu'ah under each prayer
- • A bell per prayer to switch its alert on or off
- • Suhoor (s'hour) and iftar, countdown and length of the fast ("for your position" dropped)
- • Suhoor reminder an hour before it ends, on by default (was "on unless you turn it off"; English says "on by default")
- • Widgets: big countdown, today's times, a strip of the five prayers, your town ("to the next prayer", "home" and "in the app's theme" dropped)
- Removed: "Soon on Chrome and Edge"
- • Light and dark themes, enlarged text and screen readers
- • Optional usage counters, off by default. No crash reports ("managed in the settings" dropped)
- • Approximate position, read with the app open. (coordinates sentence unchanged)

Short description (77): Prayer times offline, adhan, Qibla and private prayer tracking. Wear OS.

What's new (498): My prayers: private tracking, week and last 30 days, on the phone. A bell per prayer switches its alert on
or off. Times: this month and the next, one card per day, to save as PDF. Add your mosque's jama'ah and Jumu'ah under each
prayer. The Hijri date opens the Islamic dates of the year. The Qibla says which way to turn first. Times and the next
prayer widget follow the moment of the day. A widget of the five prayers. If your mosque differs, follow your mosque.

Notes: French had the least room (1.5.0 was 3982 with the shortest opening), so it cut most: the countdown in the widget
bullet no longer says "to the next prayer" (the first bullet still does). "Joumou'a" is the usual French Muslim spelling.
"suivi des prières" is the French search phrase for a tracker. The app is not in French, so "Mes prières" is a translation,
not a quote; "pas en français" still says so. Still no far north bullet (room).

### Hindi (hi-IN), 1.6.0

Full description, changed and new lines:
- (first paragraph) SajdaTime gives Sunni and Shia Muslims namaz times, adhan alerts and a Qibla compass, on your phone and Wear OS watch. No adverts, no account, and the namaz times are worked out on your own phone, so they come offline, even without signal.
- Removed: the separate "whether you say namaz or salat" paragraph (folded in; "salat" dropped, see the 1.5.0 note on सलाह)
- • Fajr, Sunrise, Zuhr, Asr, Maghrib and Isha, with a live countdown to the next prayer ("wherever you are" dropped)
- • The Hijri date (Islamic calendar) and the Islamic dates of the whole year
- • This month's, next month's and Ramadan's time table, to save or print as a PDF
- • A reminder 5 to 30 minutes before the chosen prayers ("optional" dropped)
- • Silent notification: the next prayer and its countdown
- Heading: MY PRAYERS AND YOUR MOSQUE
- • My prayers: a private namaz tracker, of this week and the last 30 days, stays on the phone
- • Under each prayer, your mosque's jamaat (iqamat) and Jumu'ah time
- • A bell on each prayer, to turn its alert on or off
- • Sehri and iftar times, countdown, and the length of each fast ("for your place" dropped)
- • Widgets: a big countdown to the next prayer, today's times and a five prayer strip, with your town's name
- Removed: "Soon on the Chrome and Edge browsers too"
- • Light and dark theme, large letters and screen readers
- • Sensible Fajr and Isha times even in the far north ("in summer" dropped)
- • Optional usage statistics, off without your yes. No crash reporting ("under your control in Settings" dropped)
- • Approximate location, read only while the app is open. (coordinates sentence unchanged)
- • ...usually Google. What this means is explained in the privacy policy (was "what exactly this means")
- Don't want to share location? Write your town's name, it is looked up online once.

Short description (80): Offline namaz times, adhan and Qibla and a private namaz tracker, Sunni and Shia, Wear OS.

What's new (499): My prayers: a private record of your prayers, this week and the last 30 days, on the phone itself. Each
prayer's bell turns its alert on or off. Time table: this month and next, a card for each day, with PDF save. Add your
mosque's jamaat and Jumu'ah time under each prayer. The Hijri date opens the year's Islamic dates. The Qibla now tells you
which way to turn first. The times screen and the next prayer widget show the part of the day (pahar). A five prayer widget
strip. Where your mosque differs, go by your mosque.

Notes: "नमाज़ ट्रैकर" is the transliterated term Indian users type. "जमाअत" and "इक़ामत" are the Urdu derived spellings the
1.5.0 Hindi already uses for religious words; "जुमा" for Jumu'ah. "पहर" (part of the day) is everyday Hindi and Urdu.
