# Turkish translation notes (tr-TR)

Written 8 Oct 2026 by the AI assistant that made the translation, for Ali, who does not read Turkish.
Written in plain English. Please read section 1 at least. Everything flagged here is a doubt, not a
confirmed fault; a Turkish reader can settle most of them in a few minutes.

What was made: `app/src/main/res/values-tr/strings.xml`, `core/src/main/res/values-tr/strings.xml`,
`wear/src/main/res/values-tr/strings.xml`. 298 resource entries in all (234 app, 35 core, 29 wear), of
which about 20 are names kept exactly as in English (SajdaTime, method names, the Bismillah, and so on).
`python3 tools/check-translation.py tr tr-TR` prints `0 error(s), 0 warning(s)`. Nothing was built or
run on a phone: the checks below are the checker's and my own reading, nothing more.

---

## 1. The six highest-risk strings

### 1a. `disclaimer_body` (the full disclaimer)

Seven paragraphs, same order as the English, separated the same way. The checker confirms the count.
What I kept in force, in order: helper not authority; where the times come from and the five ways they
can fail (software fault, wrong location, wrong clock, battery-saving delaying an alert, "I can simply be
wrong"); calculation not ruling, follow your mosque, no warranty, do not rely on it alone; the watch is
a convenience; mosques disagree, Isha most, over an hour in the north, pick the method in Settings,
Match your mosque for the last minutes, follow your mosque; becomes due versus congregation, neither
is wrong; the dua request.

Risk: it is the longest text and the one that carries the religious and legal weight. Two choices in it
a Turkish reader should confirm:

- "I am not a Mufti, Aalim, or an expert in fiqh" became "Ben müftü, âlim ya da fıkıh uzmanı değilim".
  Standard words, lower case because in Turkish they are common nouns.
- "the watch" became "akıllı saat" (smart watch) on first mention, then "saat". The Turkish word
  "saat" also means hour and clock, so the first mention needed "smart" to be unambiguous.

### 1b. The final dua paragraph (quoted in full)

English: *And one request, if you would: please remember me, my family, and my parents in your duas.*

Turkish: **Bir de ricam olacak, mümkünse: lütfen beni, ailemi ve anne babamı dualarınızdan eksik
etmeyin.**

Back-translation: "And I have one request, if possible: please do not leave me, my family and my
parents out of your duas." (My first draft had "kabul ederseniz", "if you accept", and no "please";
the reviewer found that read a little like a bargain, and I agree.) The phrase "dualarınızdan eksik etmeyin" is the ordinary, humble way Turkish Muslims ask
this of each other; the literal "remember in your duas" (dualarınızda hatırlayın) exists but sounds
translated. It appears here and nowhere else; I searched the three files for "dua" to be sure.

Risk: it is the one sentence you asked for personally. If a Turkish reader prefers the more literal
form, it is a one-word change.

### 1c. `method_moon_sighting_desc` and `method_umm_al_qura_desc`

- Moonsighting Committee. English: *A seasonal rule made for places far from the equator. Followed by
  some mosques in Britain.* Turkish: *Ekvatordan uzak yerler için yapılmış mevsimsel bir kural.
  Britanya'daki bazı camiler tarafından izlenir.* "Some" is "bazı", kept as a hedge.
- Umm al-Qura. English: *The official method in Saudi Arabia. Isha is set at a fixed time after
  Maghrib.* Turkish: *Suudi Arabistan'ın resmî yöntemi. Yatsı, Akşam vaktinden sabit bir süre sonraya
  ayarlanır.* "Akşam vaktinden" (from the Maghrib time) rather than attaching a suffix to the prayer
  name itself.

Risk: these describe how real organisations work. A wrong word here would misdescribe an authority.
I translated only what the English says and added nothing.

### 1d. The prayer names (`core`, `prayer_*`)

İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı, exactly as the brief's glossary (Diyanet's labels).

Risk: in Turkey "İmsak" is the label on the Diyanet timetable for the dawn column, and "İmsak vakti"
is the everyday phrase for the moment Fajr comes in and the fast begins. The prayer itself, though, is
called "sabah namazı" (the morning prayer), so "SIRADAKİ NAMAZ: İmsak" (next prayer: İmsak) reads a
little oddly. The alternative label "Sabah" has its own problem: on some Turkish calendars "Sabah"
marks a *different, later* recommended time near sunrise, so it could put a familiar word on the wrong
number. I followed the glossary and kept İmsak; the independent reviewer agreed, at about 75 percent
confidence. **Question for a Turkish reader: is "İmsak" the right label for the Fajr row in a prayer
app, or would "Sabah" be clearer?** This is purely a labelling question. Whether the app's times agree
with Diyanet's published table is a separate calculation question, not covered by these notes.

### 1e. The Hijri months (`hijri_months`)

Muharrem, Safer, Rebiülevvel, Rebiülahir, Cemaziyelevvel, Cemaziyelahir, Recep, Şaban, Ramazan, Şevval,
Zilkade, Zilhicce. Exactly the brief's Diyanet list, twelve items, same order. These are the forms
printed on Turkish calendars; I have no doubt about them.

### 1f. `consent_body` (the usage-counts consent)

All material facts kept: free, no ads; sent to Google Analytics only if you say yes; random ID not your
name; may be processed outside the UK ("Birleşik Krallık dışında"); detailed records kept 14 months; a
prayer app can say something about your faith, so it is your choice and nothing changes if you say no;
never your GPS position or prayer settings; nothing is sent until you choose yes; you can turn it off in
Settings; the person responsible is Ali Imran Khan, who made the app. "Tracking" does not appear
(the Turkish word "izleme" was not used anywhere in that sense). "Usage counts" became "kullanım
sayıları" (usage numbers), a plain phrase that says no more than "counts".

Risk: legal text. One judgement: "Help improve SajdaTime?" became "SajdaTime uygulamasını geliştirmeye
yardım eder misiniz?" (Would you help improve the SajdaTime app?), adding the word "app" so that no
Turkish suffix had to be attached to the name SajdaTime.

---

## 2. Judgement calls

Word choices where two standard options existed, and sentences restructured for placeholders.

**Two kinds of "school".** The app has "School of thought" (Sunni/Shia) and "Madhab" (Hanafi and the
rest) as separate settings. In Turkish both are normally "mezhep". To keep them apart I used
**"Mezhep"** for the Sunni/Shia row (`settings_school`, `label_school`, `wear_school`, and "Hangi mezhebe
bağlısınız?" for the question) and **"Fıkıh mezhebi"** (fiqh school) for the Hanafi row
(`settings_madhab`, `madhab_title`, `mosque_diff_asr`). The glossary says "mezhep" for both; this is
the one place I departed from it, for clarity, and I am flagging it as the brief asks. A Turkish reader
may prefer "Fıkhi mezhep" or "Amelî mezhep"; any of the three is understood.

**"Match your mosque" versus "follow your mosque".** Both would naturally be "caminize uyun" in Turkish,
which would make the Settings row sound like the instruction. The Settings feature is therefore
**"Caminizle eşleştir"** (match with your mosque) and the instruction is **"caminize uyun"** (follow your
mosque). The disclaimer refers to the feature by that name so the two stay aligned.

**"Becomes due".** Rendered as "vaktinin girdiğini gösterir" (shows when its time comes in), using
"vakit girmek", the everyday Turkish idiom for a prayer's time arriving. Used identically in the
disclaimer and in `mosque_diff_congregation`, as the source comment asks.

**"Some minutes" versus "a few minutes".** Turkish has no natural separate word for "some minutes";
both became "birkaç dakika" (a few minutes). The hedge is preserved; only the shade of vagueness is lost.

**"Convention"** in the method descriptions became "yöntem" (method); Turkish has no comfortable
everyday word for "convention" in this sense.

**Placeholders kept alone.** Every placeholder stands as its own word with no Turkish suffix glued to
it. Where English needed a suffix, I restructured:
- "Time for %1$s" → "%1$s vakti" (the time of X).
- "%1$s begins at %2$s" → "%1$s vakti başlıyor: saat %2$s" (the time of X is beginning: at hour Y).
- "%1$s at %2$s" → "%1$s, saat %2$s".
- "In %1$s" → "%1$s sonra" (X later), and "%1$s in %2$s" → "%2$s sonra %1$s" (order swapped).
- "Saved to Downloads as %1$s" → "İndirilenler klasörüne şu adla kaydedildi: %1$s" (colon form).
- "Shows %1$s" → "Gösterilen: %1$s".
- "%1$d of 5" → "5 vakitten %1$d tanesi" (X of the 5 prayers, using "tanesi" so the digit needs no
  suffix).
- "Decrease %1$s" / "Increase %1$s" → "%1$s için azalt" / "%1$s için artır" (decrease for X). These are
  spoken by a screen reader on the plus and minus buttons.
- "We will make a PDF of your prayer times for %1$s" → "%1$s için namaz vakitlerinizden bir PDF
  hazırlayacağız" ("için" is a separate word meaning "for").
- "Facing %1$d°" → "Baktığınız yön: %1$d°" (the direction you face: X°).
- Degrees: "%1$d°" is always followed by a separate word (sola dönün, enleminden, yönünde), never a
  suffix on the number.

**Proper names with suffixes.** Where a fixed name (not a placeholder) needed a suffix, I used the
standard Turkish apostrophe and escaped it: Mekke'yi, Kâbe'ye, Google Analytics'e, Google'a, Google
Play'deki, Ali Imran Khan'dır, Britanya'da, SajdaTime'ı (watch only). For "SajdaTime" on the phone I
restructured instead ("SajdaTime uygulamasını"), because the name's pronunciation, and so its correct
suffix, is not obvious to a Turkish reader. The watch string kept "SajdaTime'ı" for length; a Turkish
reader may prefer "SajdaTime'ı" or "SajdaTime'i" depending on how they say the name.

**Capitals.** The one all-caps string is "SIRADAKİ NAMAZ" (next prayer), with Turkish dotless I in
"SIRADAKİ" and dotted İ at the end, as the rules of Turkish require. The group headings are "NAMAZ
VAKİTLERİ", "GÖRÜNÜM", "HATIRLATICILAR", "HAKKINDA".

**Localised examples.** `city_field_helper` became "Örneğin: İstanbul, Berlin, Kahire" instead of
Manchester, Lahore, Cairo, because a Turkish user is most likely in Turkey or Germany. Say if you would
rather keep the English examples.

**Miles to kilometres.** `location_sheet_body` says "ten miles is about a minute". Turkey uses
kilometres, so I wrote "16 kilometre yaklaşık bir dakika eder" (16 km is about a minute). 10 miles is
16.1 km, so the measured fact is unchanged; only the unit is converted.

**"Disclaimer"** as a row label became "Sorumluluk reddi", the standard Turkish legal term. It is a
little colder than the rest of the app's voice. "Önemli uyarı" (important notice) is a softer option.

**"Got it" / "I understand".** "Tamam" (OK) for the small info card, "Anladım" (understood) for the
disclaimer button, which is what Android's own Turkish uses.

**"Follow phone"** (theme) became "Telefonla aynı" (same as the phone).

**"Keep the next prayer in the shade"** became "Sıradaki namazı bildirim panelinde tut" (keep in the
notification panel), the ordinary Turkish name for the notification shade.

**"Exact alarms"** became "tam zamanında alarmlar" (alarms exactly on time), which is close to the
wording Android's own Turkish permission screen uses.

**"Ongoing charity"** became "sadaka-i cariye", the term Turkish Muslims use.

**Durations.** Phone: "%1$d sa %2$d dk" (hours "sa", minutes "dk", seconds "sn"), the standard Turkish
abbreviations. Watch: "%1$dsa %2$ddk" without spaces to save room. The watch's "in 2h 10m" form is the
bare "2sa 10dk" with no word for "in"; see "After review" and the length doubt in section 4.

**Turn left / Turn right.** "%1$d° sola dönün" / "%1$d° sağa dönün". Physical, not mirrored.

**Method names.** Kept in English exactly as the brief lists them, including "Kuwait", "Qatar", "Dubai"
and "Jafari (Ithna Ashari)". Only the words around them were translated: "Umm al-Qura, Mekke", "ISNA,
Kuzey Amerika", "Kemenag &amp; MUIS, Endonezya ve Singapur", "Diyanet, Türkiye", "Institute of
Geophysics, Tahran". In the descriptions the countries are Turkish: Kuveyt, Katar, Mısır, Körfez, and
so on. "The Islamic Society of North America" in `method_north_america_desc` was translated to "Kuzey
Amerika İslam Cemiyeti" because the description line is meant to explain the ISNA label above it.

---

## 3. Things in the English source that looked wrong, unclear or hard to translate

Reported, not fixed.

1. **`method_muslim_world_league_desc` "The standard here".** In Turkish (and, the reviewer points
   out, in English too) this can be read as "the standard in this country", which in Turkey would be
   a false claim about who has authority: Diyanet is the standard there and is listed just below. The
   Turkish now says "the default in this app". The English may want the same clarification.
2. **Method labels "Kuwait" and "Qatar".** The brief lists them as names to keep, but to a Turkish
   reader they are simply English country names where "Kuveyt" and "Katar" would be expected. The
   description under each does say the country in Turkish, so the meaning gets through. If you would
   rather these two labels be Turkish, it is a two-word change.
3. **`home_until` ("until it begins").** On screen this sits under the countdown as a separate label.
   Turkish word order would put the countdown in the middle of the sentence, so no Turkish rendering
   of "until it begins" reads as a sentence there. It is now the single word "kaldı" (left), as in
   "2 sa 10 dk / kaldı", which is how Turkish countdowns are normally labelled. "Begins" is implied.
4. **`sect_shia_desc` "Jafari (Ithna Ashari) times".** Kept as the brief requires. A Turkish Shia user
   would write "Caferi (İsnaaşeri)". This is the one method name where keeping English may look odd
   to its own audience.

---

## 4. Not sure these are the standard term (questions for a native reader)

1. **İmsak versus Sabah** for Fajr, see 1d. This is my biggest doubt.
2. **"Fıkıh mezhebi"** for the Hanafi/Shafi'i setting (section 2). Is this, "Fıkhi mezhep" or "Amelî
   mezhep" what a Turkish reader expects, and is plain "Mezhep" for Sunni/Shia right?
3. **Watch string lengths.** I could not run the watch emulator, and neither could the reviewer. The
   watch disclaimer is 73 characters (the English is 77; the source comment says 78 fitted three lines
   and 85 did not), so it should fit, but Turkish words are long and a character count does not prove
   it. The tile countdown is now "2sa 10dk" (8 characters, the budget the source comment gives) after
   dropping the word "sonra" ("later"); see "After review". The approximate line "Yaklaşık 05:14 ·
   2sa 10dk" is still longer than the English "About 05:14 · in 2h 10m" by one character. "Yaklaşık"
   is kept spelled out because a screen reader has to say it. **Before release, run
   `./tools/wear-verify.sh` and look at the bottom of the times list and the tile.**
4. **"Kullanım sayıları"** for "usage counts" (usage numbers). My first draft had "kullanım
   sayımları"; the reviewer pointed out that "sayım" suggests a census or stock-take, and I agree.
   "Kullanım istatistikleri" (usage statistics) is more common in Turkish apps but says slightly more
   than the English does, so it was not used.
5. **"Bismillahirrahmanirrahim"** as the spoken form for screen readers. This is the usual Turkish
   spelling; a reader may prefer it split into words.
6. **"Sorumluluk reddi"** for the Disclaimer row (section 2).
7. **"düpedüz yanılıyor olabilirim"** for "I can simply be wrong". "Düpedüz" (plainly, outright) is
   colloquial and matches the humble voice, but a reader might prefer "sadece yanılıyor olabilirim".
8. **"Caminiz cemaati genellikle biraz daha geç kıldırır"** for "Your mosque usually holds the
   congregation a little later". "Kıldırmak" (to lead the prayer) is the natural verb, but it shifts
   the subject slightly from the mosque to the imam. The meaning, and the hedge "usually", are intact.

---

## What I did not do

- Did not run Gradle, lint, the Kotlin tests, or any emulator. The only check is
  `tools/check-translation.py`, plus my own reading.
- Did not touch any file other than the three `values-tr/strings.xml` files and this one.
- Did not draft a Turkish store listing.
- Did not look at how any method, Diyanet included, is calculated. These notes are about words only.

---

## After review (8 Oct 2026)

An independent second model back-translated the Turkish and reviewed it
(`docs/translation/review-tr-TR.md`). Its verdict was that the translation is faithful, with two
blockers, both wording. Everything it raised was applied except where noted. The checker still prints
`0 error(s), 0 warning(s)`.

**Applied, blockers**

- *"Quiet" had become "silent".* `settings_alerts_help`, `polar_notice_alarm` and
  `settings_alarm_on_approximate_desc` said a notification is "sessiz" (makes no sound). The
  reviewer checked the code: the prayer notification uses the phone's normal notification sound, so
  that was false, and a user who chose "Notification" to keep their phone quiet at the mosque would
  have been misled. Now "daha sessiz" (quieter) in the first two, and "alarm çalmaz, yalnızca bildirim
  gelir" (the alarm does not ring, only a notification arrives) in the third. The strings that
  describe things that really are silent (Do Not Disturb, the stay-quiet switch, the next-prayer
  badge) were left as they were.
- *"Keep the first option" could read as "drop it".* "Bırakın" alone can mean "leave it" or "give it
  up". Now "seçili bırakın" (leave it selected) in `method_step_body` and `method_auto_desc`. The same
  edit also corrected "most of the world" back to the English "much of the world".

**Applied, should-fix**

- Disclaimer, watch paragraph: the Turkish had the *compass* seeing less of the sky. Now the watch has
  a smaller compass, and the watch sees the sky less well than the phone, as the English says.
- `method_muslim_world_league_desc`: "Buradaki standart" (the standard here) read as "the standard in
  this country". Now "Bu uygulamada varsayılan" (the default in this app). The English has the same
  ambiguity; reported in section 3.
- Watch tile countdown: "sonra" ("later", standing in for "in") dropped from `wear_in_h_m` and
  `wear_in_m`, so the countdown is "2sa 10dk" and fits the eight-character budget the source comment
  gives. The reviewer said to do this only if a screenshot showed clipping; I did it in advance
  because neither of us can take that screenshot here, 14 characters against a budget of 8 was never
  going to fit, and on a "next prayer" tile a bare duration beside the time is understood as the time
  remaining. If `./tools/wear-verify.sh` later shows room, "sonra" can go back.

**Applied, nits**

- `home_until`: now plain "kaldı" (left).
- `notif_prayer_body`: "itibarıyla" (as of) was office language; now "%1$s vakti başlıyor: saat %2$s".
- Dua line: "mümkünse" (if possible) and "lütfen" (please) restored; "kabul ederseniz" (if you
  accept) removed. The idiom "dualarınızdan eksik etmeyin" is kept.
- "Kullanım sayımları" → "kullanım sayıları" in all five places (`consent_body`, `consent_yes`,
  `settings_usage_counts`, `about_privacy_desc`; `settings_usage_counts_desc` never contained it).

**Rejected**

- Nothing in the review was rejected outright. The one departure is the watch "sonra" change above,
  applied now rather than after a screenshot, with the reason given.

**Changed in these notes**

- Section 1d previously speculated about *why* a Turkish user's times might differ from Diyanet's
  table. That was outside the translation and the reviewer disagreed with the detail, so it has been
  removed. The İmsak-versus-Sabah label question stays, as a question for a Turkish reader, and the
  reviewer's reasoning for keeping İmsak is recorded there. Whether the app's Diyanet times match
  Diyanet's published ones is a calculation matter being handled separately, and nothing in the
  translation or these notes says anything about it.
