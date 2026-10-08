# Review: the Indonesian translation as shipped in 1.3.1

Independent reviewer (a second model), 8 Oct 2026. Method: I back-translated the three
`values-in` files into English before reading the English source
(`docs/translation/backtranslation-shipped-in.md`), then compared the two line by line, with the
translator notes in the English files. `tools/check-translation.py in id` prints 0 errors. The
language tag `id` (not `in-ID`) is what the code and tests require, so it is correct.

## 1. Verdict

The Indonesian is safe to keep live: the disclaimer keeps all four points, its humble first-person
voice, "follow your mosque" as an instruction, "no warranty", and the dua request only in the last
paragraph, and the consent text says exactly what the English says. There are no blockers, but
seven things should be fixed in the next release, mostly sentences that still describe how the
app used to work, one softened warning about late Fajr alerts, and the Kaaba and Makkah being
spelt differently on the phone and the watch.

## 2. BLOCKERS

None.

The three "silent" corrections read right. `polar_notice_alarm` now says the alarm arrives as an
"ordinary notification" (notifikasi biasa), and `settings_alerts_help` says a notification "sounds
softly and vibrates". `settings_alarm_on_approximate_desc` says alarms "do not ring" on those days
(see the nit below). I found no other quiet/silent slip: every remaining *senyap* (silent) is on a
string where the English really means no sound at all. Those are the silent-mode switch, Do Not
Disturb, and the next-prayer badge and its channel.

## 3. SHOULD FIX

### 3.1 `settings_exact_alarms_desc` and `confirm_exact_alarm_body`: the warning about late alerts is softened

- **Indonesian now:** "...bisa menunda pengingat salat **hingga** satu jam atau lebih, dan di
  beberapa ponsel pengingat **malam hari** seperti Subuh..."
- **What it says:** "can delay prayer reminders **up to** an hour or more... night-time reminders
  such as Subuh".
- **What it should say:** "by an hour or more". The English comment explains that one hour is the
  *measured minimum* delay on two real phones. "Up to" turns that into a ceiling and makes the
  delay sound smaller, which could talk a user out of granting the permission that protects Fajr.
  This is safety text (brief rule 4). Also, *dini hari* (the small hours) describes Subuh better
  than *malam hari* (evening or night). The settings string also ends with "Tap to allow", which
  is out of date (see 3.2).
- **Replacement `settings_exact_alarms_desc`:**
  `Tanpa ini, ponsel Anda bisa menunda pengingat salat satu jam atau lebih, dan di beberapa ponsel pengingat dini hari seperti Subuh bisa tidak muncul sama sekali.`
- **Replacement `confirm_exact_alarm_body`:**
  `Android tidak mengizinkan aplikasi memasang pengingat pada menit yang tepat kecuali Anda mengizinkannya. Tanpa izin itu, ponsel Anda bisa menunda pengingat salat satu jam atau lebih, dan di beberapa ponsel pengingat dini hari seperti Subuh bisa tidak muncul sama sekali. Anda juga bisa melakukannya nanti dari Pengaturan.`

### 3.2 `settings_dnd_desc`: still says "Tap to allow"

- **Indonesian now:** "Tanpa ini, alarm Anda tetap senyap saat ponsel dalam mode Jangan Ganggu.
  Ketuk untuk mengizinkan."
- **What it says:** "...Tap to allow."
- **What it should say:** the English dropped that sentence. The card now has its own
  "Izinkan di Pengaturan" (Allow in Settings) button, and the English comment explains that "Allow"
  alone promises more than one tap can do. The same fix is already folded into 3.1.
- **Replacement:**
  `Tanpa ini, alarm Anda tetap senyap saat ponsel dalam mode Jangan Ganggu.`

### 3.3 `method_notice_body`: an extra sentence that the layout has no room for

- **Indonesian now:** "Subuh dan Isya bisa berbeda satu jam atau lebih di sini. Ketuk untuk tahu
  sebabnya."
- **What it says:** "...Tap to find out why."
- **What it should say:** only the first sentence. The card already has a "Lihat alasannya"
  (See why) button. The English comment sets a hard limit of two lines, about 66 characters, and
  says that on the test emulator a third line is clipped by the navigation bar. The Indonesian is
  83 characters. Without the extra sentence it is 56.
- **Replacement:**
  `Subuh dan Isya bisa berbeda satu jam atau lebih di sini.`

### 3.4 `home_default_location_body`: says "Tap" when there is a button

- **Indonesian now:** "Lokasi Anda tidak dapat ditentukan. Ketuk untuk mengatur lokasi Anda."
- **What it says:** "Your location could not be determined. Tap to set your location."
- **What it should say:** "We could not work out where you are." The card's own button,
  "Atur lokasi saya" (Set my location), sits directly underneath, so the second sentence repeats it.
- **Replacement:**
  `Kami tidak dapat menentukan lokasi Anda.`

### 3.5 `settings_adjustments_help`: missing the new way to enter the mosque's time

- **Indonesian now:** "Jika masjid Anda mencetak waktu yang berbeda, geser di sini. Coba ubah
  metode perhitungan dulu..."
- **What it says:** "If your mosque prints different times, shift them here. Try the method
  first..."
- **What it should say:** "...nudge it here, **or tap a number and pick the time your mosque
  shows**." Without that clause, Indonesian users are never told about the mosque time entry
  added in this release.
- **Replacement:**
  `Jika masjid Anda mencetak waktu yang berbeda, geser di sini, atau ketuk angkanya lalu pilih waktu yang ditampilkan masjid Anda. Coba ubah metode perhitungan dulu. Biasanya itulah perbedaan sebenarnya, dan pengaturan ini untuk sisa selisih beberapa menit.`

### 3.6 `language_unavailable`: says something different from the English

- **Indonesian now:** "Terjemahan menunggu ditinjau oleh penutur asli"
- **What it says:** "Translation waiting to be reviewed by a native speaker."
- **What it should say:** "Not available in this version." The line appears under a language
  the app lists but does not contain. It describes availability, not review status, and the
  Indonesian wording would make a promise about review that is not true. It is not visible in 1.3.1
  because all four languages are included, but it will be the day a fifth is listed.
- **Replacement:**
  `Tidak tersedia di versi ini`

### 3.7 Kaaba and Makkah are spelt one way on the phone and another on the watch

- **Indonesian now:** the phone writes *Ka'bah* (`qibla_subtitle`, `qibla_facing`,
  `qibla_legend_kaaba`) and *Makkah* (`action_use_makkah`, `home_default_location_title`,
  `method_umm_al_qura`). The watch writes *Kakbah* and *Mekah*.
- **What it should say:** one spelling everywhere. The glossary and the standard dictionary (KBBI)
  both give *Kakbah* and *Mekah*, and the watch already uses them. Both phone spellings are widely
  understood, so this is about consistency, not meaning.
- **Replacements:**
  - `qibla_subtitle`: `Kakbah berjarak sekitar %2$d km dari sini, pada %1$d° dari utara.`
  - `qibla_facing`: `Anda kini menghadap Kakbah`
  - `qibla_legend_kaaba`: `Kakbah`
  - `action_use_makkah`: `Lewati dulu dan pakai Mekah`
  - `home_default_location_title`: `Menampilkan waktu untuk Mekah`
  - `method_umm_al_qura` (only if the owner wants the place name translated, as Teheran and Turki
    already are): `Umm al-Qura, Mekah`

## 4. NITS

- `settings_alarm_on_approximate_desc`: "Alarm tidak berbunyi" (alarms do not ring) is accurate,
  but on its own it could read as "nothing happens". Optional and clearer:
  `Saat matahari tidak terbit atau terbenam, waktu dihitung dari lintang yang lebih rendah. Pada hari-hari itu alarm datang sebagai notifikasi biasa kecuali Anda mengaktifkan ini.`
- `settings_usage_counts_desc`: "Saat diaktifkan, Anda melihat persis apa yang dikirim" can read as
  a live view of the data after it is switched on. The English means that switching it on first
  shows the list. Optional:
  `Dikirim ke Google, terhubung ke ID acak. Mati kecuali Anda mengaktifkannya. Sebelum aktif, Anda melihat persis apa yang dikirim.`
- `qibla_no_compass`: "Hadap" is a bare root. More standard:
  `Menghadaplah %1$d° dari utara sejati`
- `wear_polar_notice`: "Tanya ke masjid Anda" is a little casual. Standard:
  "Tanyakan kepada masjid Anda." It adds 6 characters on the watch.
- `wear_disclaimer`: "Waktu dihitung." (times are counted) is very terse. "Waktu hasil
  hitungan." is clearer. It is 74 characters against the 78 that fitted, but the English comment
  requires a `tools/wear-verify.sh` screenshot before any rewording is merged.
- `welcome_tagline`: *pribadi* means "personal" more than "private", so the privacy note in the
  English tagline is softer. It is acceptable as it is.

## 5. What I could not judge (needs a native Indonesian speaker)

- **`aliran` for Sunni/Shia** (`sect_title`, `label_school`, `settings_school`,
  `consent_never_list`, `wear_school`). In Indonesia the word also appears in *aliran sesat*
  ("deviant sect"), and Shia is a sensitive subject there. A native reader should say whether
  *aliran* sounds neutral to both Sunni and Shia users, or whether another word is kinder.
  *Mazhab* is the glossary word, but the app already uses it for the madhab row beside this one.
- **`home_until`**: "lagi hingga masuk waktu", shown under the big countdown. It is understandable,
  but I cannot say whether it sounds natural.
- **`qibla_turn_left` / `qibla_turn_right`**: whether "Putar ke kiri/kanan" makes people turn
  their body (correct) or twist the phone in their hand.
- **The disclaimer's "orang lain yang berwenang memberi nasihat"**: *berwenang* means "having
  authority". The English says "qualified". A native reader should say whether this sends the user
  to an official rather than to any knowledgeable person.
- **Whether *Ka'bah* or *Kakbah* is what Indonesian Muslims expect.** Kemenag uses both. The
  choice in 3.7 follows the glossary and the dictionary.
- **Length on the watch tile** (not a question for a native speaker or for the owner): the English
  comment allows about 8 characters, and the Indonesian countdown "2j 10m lagi" is 11. An assistant
  can measure this with `tools/wear-verify.sh`. It needs no device and no request to the owner.
