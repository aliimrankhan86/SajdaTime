# Indonesian translation (`in-ID`): translator's notes

Written 8 Oct 2026 by an AI assistant, following `docs/translation/BRIEF.md`. Nobody on the
project reads Indonesian, so these notes are the only record of what was decided and what is
uncertain. Please read the "Where I am not sure" section before relying on anything here.

Files written:

- `app/src/main/res/values-in/strings.xml` (233 strings, including 2 plurals and the 12 Hijri months)
- `core/src/main/res/values-in/strings.xml` (34 strings, plus `app_language_tag` set to `in-ID`)
- `wear/src/main/res/values-in/strings.xml` (29 strings)

`python3 tools/check-translation.py in in-ID` prints `0 error(s), 0 warning(s)`. That checker
proves the placeholders, escapes, paragraph counts and bullet counts match. It cannot judge
whether the Indonesian is good; nobody on the project has done that yet.

An independent second model reviewed the first draft by back-translation
(`review-in-ID.md`). Section 6 at the end of this file says what that review changed and what
was turned down. Sections 1 to 5 describe the first draft; where a quoted word has since
changed, section 6 has the current wording.

---

## 1. The six highest-risk strings

### 1a. `disclaimer_body` (the whole disclaimer)

Seven paragraphs, same order as the English, separated the same way. The first person "I"
is `saya` throughout, which is the humble, neutral form. Every hedge is kept: *bisa* (can),
*mungkin* (may), *biasanya* (usually), *sebagian* (some), *banyak* (many). "Follow your mosque"
is `ikutilah masjid Anda` in both places it appears. "No warranty" is `tanpa jaminan`. "I can
simply be wrong" is `saya sendiri bisa saja keliru`. "Neither is wrong" is `Keduanya tidak salah`.

**Paragraph 1**

> English: SajdaTime is a helper, not a religious authority.
> Indonesian: SajdaTime adalah alat bantu, bukan otoritas agama.

**Paragraph 2**

> English: These times are not given to me by a mosque, a scholar, or any authority. Your phone
> works them out from the position of the sun, using calculation methods that Islamic authorities
> and scholars have published. I am not a Mufti, Aalim, or an expert in fiqh, and this app was
> built with the help of artificial intelligence. Software can carry faults, a phone can hold the
> wrong location or the wrong clock, a phone saving its battery can delay an alert, and I can
> simply be wrong.
>
> Indonesian: Waktu-waktu ini tidak saya terima dari masjid, ulama, atau otoritas mana pun. Ponsel
> Anda menghitungnya dari posisi matahari, dengan metode perhitungan yang telah diterbitkan oleh
> lembaga-lembaga Islam dan para ulama. Saya bukan mufti, bukan ulama, dan bukan ahli fikih, dan
> aplikasi ini dibuat dengan bantuan kecerdasan buatan. Perangkat lunak bisa mengandung kesalahan,
> ponsel bisa menyimpan lokasi atau jam yang keliru, ponsel yang sedang menghemat baterai bisa
> menunda pengingat, dan saya sendiri bisa saja keliru.

Why risky: this is the paragraph that says where the times come from and how they can fail.
"Aalim" and "scholar" both become `ulama` because that is the glossary word and the only
natural Indonesian one; the sentence therefore says "I am not a mufti, not an ulama, not an
expert in fiqh" rather than three distinct titles. I think that is the same claim.

**Paragraph 3**

> English: So please take what you see here as a calculation and not a ruling. Where SajdaTime
> and your mosque disagree, follow your mosque. [...] The app is free and is given as it is, with
> no warranty and no promise of accuracy, so where being exact matters to you, please do not rely
> on it alone.
>
> Indonesian: Karena itu, mohon anggap apa yang Anda lihat di sini sebagai hasil perhitungan,
> bukan ketetapan hukum. Bila SajdaTime dan masjid Anda berbeda, ikutilah masjid Anda. [...]
> Aplikasi ini gratis dan diberikan apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi
> bila ketepatan penting bagi Anda, mohon jangan bergantung pada aplikasi ini saja.

Why risky: "ruling" is rendered `ketetapan hukum` (a legal determination). The alternative was
`fatwa`, which is narrower (a formal opinion by a mufti). I chose the broader word so the
sentence does not accidentally claim the app could ever have been a fatwa. A native reader might
prefer `fatwa` or plain `hukum`. See doubt 7 below.

**Paragraph 7, the dua request (quoted in full, and it appears nowhere else in any file)**

> English: And one request, if you would: please remember me, my family, and my parents in your duas.
>
> Indonesian: Dan satu permohonan, jika Anda berkenan: mohon ingat saya, keluarga saya, dan kedua
> orang tua saya dalam doa Anda.

`jika Anda berkenan` is the polite "if you would be so kind". `kedua orang tua saya` is the
standard phrase for "my (two) parents". `doa` is the glossary word.

### 1b. `method_moon_sighting_desc`

> English: A seasonal rule made for places far from the equator. Followed by some mosques in Britain.
> Indonesian: Aturan musiman yang dibuat untuk tempat-tempat yang jauh dari khatulistiwa. Diikuti oleh sebagian masjid di Britania.

Why risky: the hedge "some mosques" must not become "the mosques". `sebagian masjid` means
"some of the mosques, a portion". It is not "most" and not "all".

### 1c. `method_umm_al_qura_desc`

> English: The official method in Saudi Arabia. Isha is set at a fixed time after Maghrib.
> Indonesian: Metode resmi di Arab Saudi. Isya ditetapkan pada selang waktu tetap setelah Magrib.

Why risky: "a fixed time after Maghrib" means a fixed interval, not a fixed clock time.
`selang waktu tetap` is "a fixed interval". I added the word `selang` (interval) deliberately so
nobody reads it as "Isha is always at 7:30".

### 1d. The prayer names (`core`, `prayer_*`)

Subuh, Terbit, Zuhur, Asar, Magrib, Isya, exactly as the glossary and the Kemenag timetables.
Why risky: these are the six words the user sees most. The spellings are the official Indonesian
(KBBI) ones; everyday Indonesians also write *Shubuh, Dzuhur, Ashar, Maghrib, Isya'*. The
official forms are correct and are what the government timetable prints, but a user may find
them slightly "plain". I would not change them without a native speaker.

### 1e. The Hijri months (`hijri_months`)

Muharam, Safar, Rabiulawal, Rabiulakhir, Jumadilawal, Jumadilakhir, Rajab, Syakban, Ramadan,
Syawal, Zulkaidah, Zulhijah, as the glossary. Same risk as 1d: these are the KBBI forms, and
many Indonesians spell them *Muharram, Rabiul Awal, Sya'ban, Dzulqa'dah, Dzulhijjah*. Both are
understood. I used the glossary.

### 1f. `consent_body`

> English (first paragraph): SajdaTime is free and has no ads. If you say yes, it sends a few
> usage counts to Google Analytics so the app can be improved. They are tied to a random ID, not
> your name, and may be processed outside the UK. Detailed records are kept for 14 months.
>
> Indonesian: SajdaTime gratis dan tanpa iklan. Jika Anda setuju, aplikasi mengirimkan sedikit
> statistik penggunaan ke Google Analytics agar aplikasi dapat disempurnakan. Statistik itu
> dikaitkan dengan ID acak, bukan nama Anda, dan mungkin diproses di luar Britania Raya. Catatan
> terperinci disimpan selama 14 bulan.

The other two paragraphs follow the English sentence by sentence: "can say something about your
faith" is `dapat menyiratkan sesuatu tentang keyakinan Anda`; "nothing changes in the app if you
say no" is `tidak ada yang berubah di aplikasi jika Anda menolak`; "Until you choose yes,
nothing is sent" is `Sebelum Anda memilih setuju, tidak ada yang dikirim`; "The person
responsible is Ali Imran Khan, who made the app" is `Penanggung jawabnya adalah Ali Imran Khan,
pembuat aplikasi ini`. The word "tracking" (`pelacakan`) does not appear anywhere.

Why risky: this is a legal consent text. Two choices to know about:

- "usage counts" became `statistik penggunaan` (usage statistics). There is no natural
  Indonesian word for "counts" in this sense; `hitungan penggunaan` reads like nonsense and
  `data penggunaan` ("usage data") sounds broader than the English. "Statistik" is the honest
  middle. The same phrase is used in `about_privacy_desc`, `consent_yes`, `settings_usage_counts`.
- "the UK" became `Britania Raya` (Great Britain / the UK). Most Indonesians say `Inggris`
  (literally "England") for the whole UK, but in a legal sentence the precise word is better.
  In the disclaimer, "Britain" is `Britania`.

---

## 2. Every judgement call

1. **"School" (Sunni/Shia) is `Aliran`, not the glossary's `mazhab`.** This is the one place I
   knowingly departed from the glossary. The glossary maps both "school of thought" and "madhab"
   to `mazhab`, but the Settings screen has two rows side by side: "School of thought" (Sunni /
   Shia) and "Madhab" (Hanafi / Shafi'i / ...). With the glossary both rows would read `Mazhab`,
   and the user could not tell them apart. `Aliran` is the ordinary Indonesian word for a branch
   or denomination (`aliran Sunni`, `aliran Syiah`) and is not a paraphrase. Used consistently
   in `sect_title`, `label_school`, `settings_school`, `consent_never_list` and the watch's
   `wear_school`. `Mazhab` is kept for everything about Hanafi/Shafi'i. **Please have a native
   reader confirm this.** If they prefer `Mazhab` for both rows, it is a two-word change.
2. **"Prayer" is `salat`** (KBBI spelling). Everyday spellings are *shalat* and *sholat*. I used
   the standard one throughout, the same way I used the standard prayer-name spellings.
3. **"becomes due" is `waktu salat masuk`** (literally "the prayer time enters"), which is the
   standard Indonesian idiom for a prayer time beginning. The source comment asks that
   `mosque_diff_congregation` and the disclaimer use the same wording; they do (`kapan waktu
   setiap salat masuk` and `kapan waktu salat masuk`). `home_until` ("until it begins") uses the
   same idiom: `hingga waktunya masuk`.
4. **"Match your mosque" (the feature name) is `Sesuaikan dengan masjid`** ("adjust to the
   mosque"). Kept short because it is a Settings row label, and used identically inside the
   disclaimer so the user can find the row the disclaimer points at. "Calculation method" is
   `Metode perhitungan` everywhere, for the same reason.
5. **"Ring on approximate days" is `Berdering pada hari perkiraan`**, and the quoted mention of
   it inside `polar_notice_alarm` uses exactly the same words, in the same curly quotes.
6. **"Alerts" became `pengingat` (reminders)**, since Indonesian has no separate everyday word
   for "alert" distinct from "notification" (`notifikasi`) and "alarm" (`alarm`). The group
   heading REMINDERS is also `PENGINGAT`, so the words line up.
7. **"Twilight" is rendered as `fajar dan senja`** (dawn and dusk) where the English speaks of
   mosques measuring "the twilight" for both Fajr and Isha, because `senja` alone means only the
   evening. Where only the evening is meant (the Jafari description, the disclaimer's "fading of
   the twilight"), it is `senja` or `mega merah` (the red glow), per the glossary.
8. **"Ten miles is about a minute" became `16 kilometer hanya sekitar satu menit`.** Indonesia
   uses kilometres; ten miles is 16 km, and the measured fact behind the sentence (2 minutes
   over a 10-mile radius, HANDOVER §10) still holds for 16 km. Latin digits per the brief.
9. **Example cities** (`city_field_helper`): "Manchester, Lahore, Cairo" became "Jakarta,
   Surabaya, Kairo". The English examples are for a British and Pakistani audience; an Indonesian
   user is better served by cities they would actually type. Easy to revert if you prefer the
   originals.
10. **Duration abbreviations**: `%1$dj %2$dm` for hours and minutes (`j` = jam, `m` = menit) on
    both phone and watch, keeping the watch tile's eight-character budget. Seconds are
    `%1$d dtk`, because the one-letter `d` (detik) could be read as "day". Minute adjustments
    use `mnt` (`0 mnt`, `+5 mnt`), the usual Indonesian abbreviation. These are the one place
    the phone and watch styles differ slightly (`m` versus `mnt`); see doubt 4.
11. **"in 2h 10m" (watch countdown) became `2j 10m lagi`** ("2h 10m more"), with `lagi` after
    the numbers. It is a separate word, not a suffix glued to the placeholder, so vowel harmony
    and shaping are not a concern. It is two characters longer than the English.
12. **`Now` and `Next` pills** are `Sekarang` (8 letters) and `Berikutnya` (10 letters). Both are
    longer than the English four-letter words. The shorter alternatives (`Kini`, `Berikut`) are
    either literary or ambiguous when standing alone. See doubt 3.
13. **"All five" is `Kelimanya`** ("all five of them"), one word, to fit the Settings subtitle.
14. **"as an ongoing charity for the Ummah" is `sebagai sedekah jariah untuk umat`.** `Sedekah
    jariah` is the standard Indonesian for *sadaqah jariyah*, which is what the English "ongoing
    charity" is translating back from. `umat` is lower case per the glossary.
15. **"Eid" is `hari raya Id`.** The glossary gives `Id`, which almost never stands alone in
    Indonesian (people say `Idulfitri`, `Iduladha`, or simply `hari raya`, "the festival"). I
    kept the glossary word and put the natural `hari raya` in front of it.
16. **"offline" became `tanpa internet`** ("without internet") in the PDF footer. The KBBI
    word `luring` is correct but far less understood than the plain phrase.
17. **"in the shade" (the notification shade) is `di panel notifikasi`**, Android's own
    Indonesian term.
18. **"Presidency of Religious Affairs" (`method_turkey_desc`) is `Badan Urusan Agama Turki`**
    ("Turkey's religious affairs agency"). A literal `Kepresidenan` reads oddly in Indonesian.
    Diyanet itself is not translated.
19. **"Tehran" is `Teheran`**, the standard Indonesian spelling, in both the method name and its
    description. The brief allows place names around a method name to be translated.
20. **"Bismillah ir-Rahman ir-Raheem" (screen-reader text) is `Bismillahirrahmanirrahim`**, the
    standard Indonesian one-word transliteration, so the screen reader says it the way an
    Indonesian would.
21. **`Mekah`, `Kakbah`, `Kiblat`, `azan`, `doa`, `umat`, `fikih`, `mufti`, `ulama`** are all as
    the glossary. `Kakbah` and `Mekah` look unusual to many Indonesians (who write *Ka'bah* and
    *Mekkah*) but they are the official spellings and avoid an apostrophe in the resource file.
22. **Placeholders**: none were moved or reordered. In `qibla_subtitle` the English already has
    `%2$d` before `%1$d`, and the Indonesian keeps that order. No Indonesian particle is attached
    to any placeholder; where a word had to follow one (`pukul`, `lagi`, `dalam`) it is a
    separate word with a space.

---

## 3. Things in the English source that look wrong or unclear

I did not change any of these; I am reporting them.

1. `method_jafari` and `sect_shia_desc` say "Jafari (Ithna Ashari)". The brief says method names
   stay untranslated, so I kept the English spelling. An Indonesian reader would normally see
   *Ja'fari (Itsna Asyariyah)*. It is still recognisable, but it is the one organisation-style
   name in the list that is actually a school rather than an institution, so it may deserve a
   local spelling in a later pass.
2. `wear_madhab_note`: "Hanafi is later" became `Hanafi lebih lambat`. `lambat` means both
   "later" and "slower"; in context (`Hanya Asar yang berubah`, only Asr changes) a reader will
   understand "later", but `lebih akhir` would be an alternative. Not a source problem, just
   noting that the English "later" has no single unambiguous Indonesian word.
3. `method_muslim_world_league_desc`: "The standard here." I read "here" as "the default in
   this app" and wrote `Pilihan standar di sini` ("the standard choice here"). If "here" was
   meant to mean "in Britain", the Indonesian is wrong and should be `Standar di Britania`.
4. `pdf_footer` and `about_charity` both say "free, forever". Nothing wrong, only noting I used
   `gratis, selamanya` in both so they match.

---

## 4. Where I am not sure (questions for a native reader)

1. **`Aliran` versus `mazhab` for Sunni/Shia** (judgement call 1). This is my biggest doubt,
   because it is a deliberate departure from the glossary and the word appears on the first-run
   screen (`Aliran mana yang Anda ikuti?`, "Which aliran do you follow?"). Please ask someone
   whether that question sounds respectful and normal, or whether `Mazhab mana yang Anda ikuti?`
   is what Indonesian apps actually say.
2. **`statistik penggunaan` for "usage counts"** (section 1f). It appears in the consent
   screen, the About row and the Settings switch. Does it over- or under-state what is sent?
3. **The two pill labels, `Sekarang` and `Berikutnya`,** are about twice the width of "Now" and
   "Next". The source comment says "Now" sits in a pill beside a prayer name and a time on the
   narrowest supported phone. I could not screenshot it (no Gradle or emulator in this task).
   If it clips, the shorter `Kini` and `Berikut` are available; both are correct Indonesian,
   just slightly less natural.
4. **The duration abbreviations `j` and `m`** (`2j 10m`). Indonesians do not have a settled
   one-letter convention for hours and minutes the way English has `h` and `m`. Clock-style
   `02:10` or the longer `2 jam 10 mnt` are more common. The one-letter forms fit the watch tile;
   a native reader should say whether they are understood at a glance.
5. **`ketetapan hukum` for "ruling"** (section 1a). Alternatives: `fatwa`, `hukum`,
   `keputusan hukum`. I want the sentence to say "a calculation, not a religious legal verdict"
   without making the app sound like it could have been one.
6. **`Britania` / `Britania Raya`** versus the colloquial `Inggris`. Formally correct, but a
   reader may stumble on it.
7. **The official KBBI spellings** of the prayer names, Hijri months, `Kakbah` and `Mekah`
   (sections 1d, 1e, judgement call 21) are correct but "plain"; many devout users prefer the
   Arabic-closer spellings. Not wrong, but worth a native opinion on whether the app reads as
   official or as cold.
8. **`Badan Urusan Agama Turki`** for Diyanet's description. There is no single established
   Indonesian rendering of "Presidency of Religious Affairs"; Indonesian news uses several.
9. **`Waktunya %1$s`** ("Time for Fajr" notification title). Natural and short, but `Waktu
   %1$s telah masuk` ("Fajr time has entered") is the more traditional announcement. I chose
   the short one because it is a notification title.

## 5. What I did not do

- I did not run Gradle, the emulator, lint or the Kotlin tests. Only `tools/check-translation.py`.
- I did not look at any screen with this text in place, so every length comment above is a
  character count, not a measurement.
- I did not draft a store listing.
- I did not add any Unicode direction marks or invisible characters anywhere.

---

## 6. After review (8 Oct 2026)

The reviewer back-translated the three files without seeing the English, then compared. Its
report is `docs/translation/review-in-ID.md`. The checker still prints `0 error(s), 0 warning(s)`
after these changes.

### Applied

**Both blockers.**

- **Quiet versus silent.** I had used `senyap` (makes no sound) for a prayer notification, which
  actually plays the phone's normal notification sound. Three strings changed:
  `settings_alerts_help` now says `Notifikasi berbunyi pelan dan bergetar` (sounds softly and
  vibrates); `polar_notice_alarm` now says the alarm arrives as `notifikasi biasa, alih-alih
  berdering` (an ordinary notification, instead of ringing); `settings_alarm_on_approximate_desc`
  now says `Alarm tidak berdering` (alarms do not ring). `senyap` stays only where the thing is
  truly soundless: the next-prayer badge (`settings_ongoing_badge_desc`, `channel_ongoing_desc`),
  the "stay quiet when the phone is silent" switch and its description, and the Do Not Disturb
  line.
- **`method_auto_desc`.** `di sebagian besar dunia` meant "in most of the world"; the English
  says "much of the world". Now `di banyak bagian dunia` (in many parts of the world). A hedge I
  had accidentally strengthened.

**All should-fixes except one that needs a screen.**

- `consent_policy_english` now reads `Kebijakan privasi lengkap ditulis dalam bahasa Inggris`
  ("is written in English"). My first version said "is available in English", which could be
  read as "there is also an English copy".
- `method_dubai_desc` now `Salah satu konvensi yang dipakai di kawasan Teluk` ("one of the
  conventions"), because Indonesian has no "a/the" and the first version read as "the
  convention". Kuwait and Qatar say "the convention" in English and are unchanged.
- `wear_polar_notice` now `Perkiraan. Hari ini matahari tidak terbit atau terbenam di sini,
  jadi waktu diambil dari %1$d°. Tanya ke masjid Anda.` Shorter, and `tanya ke` fixes a grammar
  slip (`tanyakan masjid Anda` could be read as "ask about your mosque").
- **`sect_title`** (the first-run question) is now `Anda mengikuti Sunni atau Syiah?` ("Do you
  follow Sunni or Shia?"), the reviewer's lowest-risk form, because `aliran` also appears in
  the phrase for "deviant sect" and the question could have sounded like "which sect are you
  in?". **`Aliran` is kept as the short row label** (`label_school`, `settings_school`,
  `wear_school`, `consent_never_list`) until a native reader rules on it. That remains the
  first question for a native speaker.
- Phone durations now use the same minute abbreviation as the rest of the phone:
  `duration_h_m` is `%1$d j %2$d mnt`, `duration_m` is `%1$d mnt`. The watch keeps the
  one-letter `2j 10m` for space.

**Nits I agreed with:** `jadwal yang dicetak masjid Anda` in `mosque_diff_minutes`; `bukan
ketetapan hukum agama` in the disclaimer (adds "religious", no stronger); `Britania Raya` in
the disclaimer and `method_moon_sighting_desc` so all three mentions of the UK agree with
`consent_body`; `Tetap pada pilihan pertama` in `method_step_body`; `pada arah %1$d° dari utara`
in `qibla_subtitle`; `folder Download` in `export_saved`, because that is the folder's literal
name on the phone.

### Not applied, and why

- **`hari raya Id` in `settings_hijri_offset_help`.** The reviewer notes people say `Idulfitri
  dan Iduladha`. True, but `Id` is the glossary word and the brief says a glossary change is the
  owner's call, not mine. Left as `Ramadan dan hari raya Id`; owner to decide.
- **`wear_in_h_m` / `wear_in_m` (`2j 10m lagi` on the tile).** The reviewer and I agree someone
  must look at a tile screenshot before release; I cannot take one in this task. If it clips,
  the fallback is plain `2j 10m` (drop `lagi`). Unchanged until measured.
- **The watch disclaimer's three-line budget, the `Sekarang` pill, the 32-character
  `consent_yes` button.** All agreed to be screen checks, not wording changes. Nothing to apply.

### Still open for a native reader (unchanged by the review)

1. `Aliran` as the row label for Sunni/Shia.
2. The KBBI spellings (Subuh, Zuhur, Asar, Magrib, Isya, the months, Kakbah, Mekah): correct
   and official, kept; both reviewers agree they are what Kemenag prints.
3. `statistik penggunaan`, `Badan Urusan Agama Turki`, `Waktunya %1$s`: the reviewer accepted
   all three; a native reader has not.
