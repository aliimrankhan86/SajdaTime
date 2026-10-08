# Indonesian translation (`in-ID`): independent review

Written 8 Oct 2026 by a second AI model. It did not write the translation. Method: I back-translated the
three Indonesian files into English first, without looking at the English
(`backtranslation-in-ID.md`). Only then did I compare against the English sources, their translator
comments and the translator's notes. `tools/check-translation.py in in-ID` prints `0 error(s)`. I also
read `Notifications.kt` to check one claim against what the app actually does.

## 1. Verdict

The translation is careful and faithful. The disclaimer keeps all four points, the humble first
person, "follow your mosque", "no warranty" and the dua in the last paragraph only, and the consent
text says neither more nor less than the English. Two things change meaning and must be fixed before
release: notifications are described as *silent* when they make a sound, and one method description
turns "much of the world" into "most of the world". Both are one-line fixes.

## 2. Blockers

### B1. "Quiet" became "silent" in the alert explanations

The English deliberately uses two different words. A prayer **notification** is "quiet": it plays the
phone's normal notification sound, not a loud alarm (the channel in `Notifications.kt` is
high-importance with the default sound). The next-prayer **badge** is "silent": it makes no sound at all.
The Indonesian uses `senyap` (silent, no sound) for both. A user is told a notification makes no sound
when it does. A user above the polar circles is told the alarm becomes a silent notification when it
becomes an ordinary one with a sound.

| Key | Indonesian now | What it says | Proposed replacement |
|---|---|---|---|
| `settings_alerts_help` | `Notifikasi bersifat senyap dan bergetar.` | Notifications are silent and vibrate. | `Notifikasi berbunyi pelan dan bergetar.` (Notifications sound softly and vibrate.) |
| `polar_notice_alarm` | `...alarm Anda datang sebagai notifikasi senyap, bukan berdering...` | ...your alarm comes as a silent notification, not ringing... | `Pada hari-hari ini alarm Anda datang sebagai notifikasi biasa, alih-alih berdering, karena aplikasi tidak dapat menjamin menit yang tepat. Aktifkan “Berdering pada hari perkiraan” di Pengaturan jika Anda tetap ingin dibangunkan.` |
| `settings_alarm_on_approximate_desc` | `Alarm tetap senyap pada hari-hari itu kecuali Anda mengaktifkan ini.` | Alarms stay silent on those days unless you turn this on. | `Alarm tidak berdering pada hari-hari itu kecuali Anda mengaktifkan ini.` (Alarms do not ring on those days...) |

Leave `senyap` where it is correct: the badge (`settings_ongoing_badge_desc`, `channel_ongoing_desc`),
`settings_alarm_respect_silent`, `settings_dnd_desc`, and the "phone silenced" half of
`settings_alarm_respect_silent_desc`.

### B2. `method_auto_desc`: a hedge was strengthened

- Indonesian: `Mendekati praktik setempat di sebagian besar dunia.`
- Says: "Close to local practice in **most** of the world." (`sebagian besar` means the greater part.)
- English: "Close to local practice in **much** of the world." The source comment says the hedges in
  method descriptions are load-bearing, and the brief forbids strengthening them.
- Replace with: `Mendekati praktik setempat di banyak bagian dunia.` (in many parts of the world)

## 3. Should fix

1. **`consent_policy_english`** (a safeguard the owner required). `Kebijakan privasi selengkapnya
   tersedia dalam bahasa Inggris.` means "The full privacy policy is *available* in English." That can
   be read as "there is also an English version". English: "The full privacy policy is in English."
   Replace with `Kebijakan privasi lengkap ditulis dalam bahasa Inggris.` (is written in English).
2. **`method_dubai_desc`**. English "*A* convention used in the Gulf". `Konvensi yang dipakai di kawasan
   Teluk.` reads in Indonesian as "*The* convention used in the Gulf region". Indonesian has no
   articles, so the relative clause makes it sound like the only one. Replace with `Salah satu konvensi
   yang dipakai di kawasan Teluk.` (One of the conventions...). Kuwait and Qatar say "The convention"
   in English, so those two are correct as they are.
3. **`wear_polar_notice`**: grammar and length. `Tanyakan masjid Anda` reads as "ask *about* your
   mosque". Indonesian needs `ke`/`kepada`, as the phone strings already have. It is also about 30%
   longer than the English, on a watch. Replace with `Perkiraan. Hari ini matahari tidak terbit atau
   terbenam di sini, jadi waktu diambil dari %1$d°. Tanya ke masjid Anda.`
4. **`sect_title`, `label_school`, `settings_school`, `consent_never_list`, `wear_school`: `Aliran`
   for Sunni/Shia.** The translator was right that both Settings rows cannot read `Mazhab`. But in
   Indonesian religious usage `aliran` is the word in *aliran sesat* (deviant sect) and *aliran
   kepercayaan* (folk belief). The first-run question `Aliran mana yang Anda ikuti?` may come across as
   "Which sect are you in?". Lowest-risk fix for the question: `Anda mengikuti Sunni atau Syiah?`.
   Keep `Aliran` as the short row label until a native speaker rules on it (see 6).
5. **`duration_h_m`, `duration_m` (phone)**. `%1$dj %2$dm` uses `m` for *menit*, while the same phone
   screens use `mnt` for minutes (`adjustment_minutes`, `adjustment_zero_minutes`). Android's own
   Indonesian uses `j` and `mnt`. For the phone, use `%1$d j %2$d mnt` and `%1$d mnt`. On the watch,
   keep the one-letter forms for space.
6. **`wear_in_h_m` / `wear_in_m` on the tile.** `2j 10m lagi` is 11 characters. The English
   `in 2h 10m` is 9, and the source comment says a tile line has room for about 8. It feeds
   `wear_tile_detail` (`05:14  ·  2j 10m lagi`). Someone has to look at a screenshot of the tile before
   release. If it clips, `+2j 10m` or plain `2j 10m` is the fallback.

## 4. Nits

- `mosque_diff_minutes`: `jadwal cetak masjid Anda` is unnatural. Use `jadwal yang dicetak masjid Anda`.
- `disclaimer_body` paragraph 3: `bukan ketetapan hukum` can read as a court or state ruling. `bukan
  ketetapan hukum agama` is clearer and is no stronger.
- `disclaimer_body` paragraph 5 and `method_moon_sighting_desc`: `Britania` on its own is literary.
  Use `Britania Raya`, which `consent_body` already uses, so all three agree.
- `method_step_body`: `Biarkan pilihan pertama` ("leave the first option") is a little odd. `Tetap
  pada pilihan pertama` is better.
- `settings_hijri_offset_help`: `hari raya Id` is the glossary word with a prefix added, but people
  say `Idulfitri dan Iduladha`. That is a glossary departure, so it is the owner's call.
- `qibla_subtitle`: `pada %1$d° dari utara` reads better as `pada arah %1$d° dari utara`.
- `export_saved`: the folder on the phone is literally named `Download`, so `folder Unduhan` may send
  someone looking for a folder that does not exist under that name.

## 5. My verdict on the translator's doubts

The notes list nine doubts, not five. Here is a verdict on each.

1. **`Aliran` versus `mazhab`**: the departure from the glossary is justified, because two rows cannot
   share one word. The word itself is risky. See should-fix 4.
2. **`statistik penggunaan` for "usage counts"**: accept it. It neither adds nor drops a fact. The
   consent text separately says it is linked to a random ID and that detailed records are kept for
   14 months, so nobody is misled by the softer noun.
3. **`Sekarang` / `Berikutnya` pills**: keep them unless a screenshot on the narrowest phone shows
   clipping. `Kini` is standard Indonesian and is the right fallback. `Berikut` alone is not (it means
   "as follows").
4. **`j` / `m` abbreviations**: half agree. They are fine on the watch, but on the phone they should
   match `mnt`. See should-fix 5.
5. **`ketetapan hukum` for "ruling"**: acceptable. Add `agama` (nit). Do not use `fatwa`: it is
   narrower, and it invites the reading that the app is declining to be one particular kind of
   authority rather than any authority.
6. **`Britania` / `Britania Raya`**: use `Britania Raya` throughout (nit). `Inggris` means England,
   which is wrong for a legal sentence about the UK.
7. **KBBI spellings** (Subuh, Zuhur, Asar, Magrib, Isya, the months, Kakbah, Mekah): keep them. They are
   what the Kemenag timetable prints. The core file's comment ("Indonesian users expect Fajr") is wrong
   for Indonesian: *Subuh* is the name Indonesians use for that prayer, not a translation of "dawn". The
   glossary is right, and that source comment should be corrected one day.
8. **`Badan Urusan Agama Turki`**: an acceptable plain description. Diyanet's name stays in the
   method label, so the user can still recognise it.
9. **`Waktunya %1$s`**: keep it. It is short and natural (`Waktunya Subuh`) and does not claim more
   than the English.

## 6. What I could not judge (needs a native speaker or a screen)

- Whether `Aliran`, or any alternative, sounds respectful to an ordinary Indonesian Sunni and Shia user.
  This is the one word I would most want a native reader to confirm.
- Whether the warm, formal register reads as natural or stiff overall. I can confirm `Anda` is used
  throughout and that there is no slang. I cannot judge the overall feel.
- Lengths, which can only be judged on a screen: the `Sekarang` pill, `action_save_pdf` beside the
  "Hari ini" heading, `consent_yes` (`Ya, bagikan statistik penggunaan`, 32 characters on a button), the
  watch tile line, and `wear_disclaimer`. The source rule for `wear_disclaimer` is "do not merge a
  reworded version you have only read". At 68 characters it is under the English, but Indonesian words
  are long and it wraps by word, so it has to go through `tools/wear-verify.sh` before release.
- Whether Indonesian TalkBack reads `2j 10m` sensibly in `home_countdown_a11y`.
