# Review of the Turkish that shipped in 1.3.1

Independent review, 8 Oct 2026, by a second AI model that did not make the translation. Method: the
three Turkish files were back-translated into English first, without looking at the English
(`backtranslation-shipped-tr.md`), and then compared line by line with the English and its
translator notes. No app or translation file was changed. Nobody who speaks Turkish natively has
read either the translation or this review.

## 1. Verdict

The Turkish is sound. Nothing in it changes a religious meaning, the disclaimer or the consent text,
and every hedge and safety point survives. It does have 12 smaller faults worth fixing in the next
release. The most useful fixes are: an ambiguous "leave the first option" on the first-run method
step, four stale "tap to..." sentences that point at cards that no longer respond to a tap, and one
missing instruction in Match your mosque.

## 2. BLOCKERS

None.

## 3. SHOULD FIX

Each replacement is the text that goes between `<string name="...">` and `</string>`. Placeholders,
`\n` and `\'` are already in place.

### `method_step_body`: "Not sure? Keep the first option" can be read as "drop it"

- **Turkish now:** `... Emin değil misiniz? İlk seçeneği bırakın. ...`
- **What it says:** "Not sure? Leave the first option." *Bırakmak* means both "leave it as it is" and
  "let go of it / abandon it". A first-time user who is not sure is the very person who could read it
  the wrong way and move off the safe default.
- **What it should say:** keep the first option selected.
- **Replacement:**

```
Camiler tan ve akşam alacakaranlığını farklı ölçer. Bu yüzden Sabah ve Yatsı vakitleri bir yöntemden diğerine birkaç dakika, uzak kuzeyde ise bir saatten fazla farklı olabilir. Caminizin vakitleri uygulamadakinden farklıysa, genellikle çözüm caminizin yöntemini burada seçmektir. Emin değil misiniz? İlk seçenek seçili kalsın. Bunu istediğiniz zaman Ayarlar bölümünden değiştirebilirsiniz.
```

### `disclaimer_body`: the watch sentence gives the "view of the sky" to the compass

- **Turkish now:** `Akıllı saatin pusulası telefondan daha küçüktür ve gökyüzünü daha az iyi görür.`
- **What it says:** "The watch's compass is smaller than the phone, and [the compass] sees the sky
  less well." This makes the compass the thing that sees the sky. It also compares the compass with
  the whole phone, and "daha az iyi görür" ("sees less well") is clumsy Turkish.
- **What it should say:** a watch has a smaller compass than a phone *and* a poorer view of the sky
  than a phone (two separate weaknesses of the watch).
- **Also changed in the same paste:** `Hiçbir garanti ve doğruluk vaadi yoktur` becomes
  `Hiçbir garanti ya da doğruluk vaadi yoktur`. After "hiçbir ... yoktur" ("there is no ..."), Turkish
  joins the two things with "ya da" (or), not "ve" (and). The "no warranty" meaning is the same and
  reads more cleanly. Nothing else in the disclaimer changes: it is still seven paragraphs, and the
  dua request is still only in the last one.
- **Replacement (whole string):**

```
SajdaTime bir yardımcıdır, dinî bir otorite değildir.\n\nBu vakitler bana bir cami, bir âlim ya da herhangi bir otorite tarafından verilmedi. Telefonunuz bunları güneşin konumundan hesaplar. Bunu yaparken İslami otoritelerin ve âlimlerin yayımladığı hesaplama yöntemlerini kullanır. Ben bir müftü, bir âlim ya da fıkıh uzmanı değilim. Bu uygulama yapay zekâ yardımıyla yapıldı. Yazılımda hata olabilir. Telefonda yanlış konum ya da yanlış saat kayıtlı olabilir. Pil tasarrufu yapan bir telefon bir uyarıyı geciktirebilir. Ben de düpedüz yanılıyor olabilirim.\n\nBu yüzden lütfen burada gördüklerinizi dinî bir hüküm olarak değil, bir hesaplama olarak kabul edin. SajdaTime ile caminiz farklı söylüyorsa caminize uyun. Bir vakit ya da yön size yanlış görünüyorsa veya emin değilseniz, caminize ya da size yol gösterebilecek ehil başka birine danışın. Uygulama ücretsizdir ve olduğu gibi sunulur. Hiçbir garanti ya da doğruluk vaadi yoktur. Bu yüzden tam doğruluğun sizin için önemli olduğu durumlarda lütfen yalnızca uygulamaya güvenmeyin.\n\nAkıllı saati kullanıyorsanız, lütfen onu son söz olarak değil, bir kolaylık olarak görün. Akıllı saatin pusulası telefonunkinden küçüktür ve saat gökyüzünü telefon kadar iyi göremez. Ayrıca telefonunuzun bir süre önce bulduğu bir konuma göre çalışıyor olabilir. Özellikle kıble için telefonunuz ikisinden daha doğru olanıdır. Önemli olduğunda telefonunuzdan kontrol edin.\n\nCamiler bir namazın ne zaman başladığı konusunda her zaman aynı görüşte değildir. En çok fark Yatsı vaktindedir. Britanya\'da ve daha kuzeyde bir yöntemle diğeri arasındaki fark bir saati aşabilir, çünkü âlimler alacakaranlığın kayboluşunu farklı ölçer. Bu konuyu ben çözemem, uygulama da çözmeye çalışmaz. Buradaki vakitler caminizle uyuşmuyorsa, onlara hangi yöntemi kullandıklarını sorun ve Ayarlar bölümünde, Hesaplama yöntemi altında o yöntemi seçin. Bundan sonra da birkaç dakikalık fark kalırsa, Camiye göre ayarla bölümünde her vakti ve tarihi elle düzeltebilirsiniz. Bir fark yine kalırsa caminize uyun.\n\nBir şey daha. Bu uygulama bir namaz vaktinin ne zaman girdiğini gösterir. Caminiz ise cemaat için kendi saatini belirler ve bu genellikle biraz daha geçtir. Bu yüzden camideki vakit çizelgesi ile buradaki vakitler çoğu zaman farklı olur. İkisi de yanlış değildir.\n\nBir de ricam var, uygun görürseniz: lütfen beni, ailemi ve anne babamı dualarınızda anın.
```

### `settings_adjustments_help`: the new "tap a number" instruction is missing

- **Turkish now:** `Caminiz farklı bir vakit yayımlıyorsa, burada biraz kaydırın. Önce hesaplama yöntemini deneyin. ...`
- **What it says:** "If your mosque publishes a different time, shift it a little here." It never
  mentions that the user can tap a number and type in the mosque's time. The English got that clause
  when the time-entry dialog was added, but the Turkish did not.
- **What it should say:** nudge it here, *or tap a number and pick the time your mosque shows*.
- **Replacement:**

```
Caminiz farklı bir vakit yayımlıyorsa, burada biraz kaydırın ya da bir sayıya dokunup caminizin gösterdiği saati seçin. Önce hesaplama yöntemini deneyin. Asıl fark genellikle oradadır. Bu bölüm, geriye kalan birkaç dakika içindir.
```

### Four stale "tap to..." sentences on the amber cards

These cards now have their own button ("See why", "Set my location", "Allow in Settings"), and the
card itself does nothing when it is tapped (`PermissionAction.kt`: "the action is a button, never the
whole card"). The English sentences that told people to tap were removed at that point, but the
Turkish still has them. A user who taps the text as told gets no response. Each fix only deletes the
last sentence.

**`method_notice_body`**
- **Turkish now:** `Burada Sabah ve Yatsı bir saat ya da daha fazla farklı olabilir. Nedenini görmek için dokunun.`
- **What it says:** "...can differ by an hour or more here. Tap to see why." There is a second reason
  to cut it. The English note on this key says the body must fit on **two lines (about 66
  characters)**, because a third line is cut off by the navigation bar. The current Turkish is about
  95 characters. The replacement is about 64.
- **Replacement:**

```
Burada Sabah ve Yatsı bir saat ya da daha fazla farklı olabilir.
```

**`home_default_location_body`**
- **Turkish now:** `Nerede olduğunuzu bulamadık. Konumunuzu belirlemek için dokunun.` ("...Tap to set your location.")
- **Replacement:**

```
Nerede olduğunuzu bulamadık.
```

**`settings_exact_alarms_desc`** (the safety meaning is correct and is kept word for word)
- **Turkish now:** `... hiç gelmeyebilir. İzin vermek için dokunun.` ("...Tap to allow.")
- **Replacement:**

```
Bu izin olmadan telefonunuz namaz uyarılarını bir saat ya da daha fazla geciktirebilir. Bazı telefonlarda Sabah gibi gece gelen bir uyarı hiç gelmeyebilir.
```

**`settings_dnd_desc`**
- **Turkish now:** `Bu izin olmadan, telefon Rahatsız Etmeyin modundayken alarmınız sessiz kalır. İzin vermek için dokunun.`
- **Replacement:**

```
Bu izin olmadan, telefon Rahatsız Etmeyin modundayken alarmınız sessiz kalır.
```

### `adjustment_summary_times` (both plural items): reads as a time, not a count

- **Turkish now:** `Ayarlanan vakit: %d`
- **What it says:** "Adjusted time: 3". On the Settings row this looks like a time value of "3",
  not "3 prayers adjusted".
- **What it should say:** "%d prayer(s) adjusted". After a number, Turkish keeps the noun singular,
  so both items are the same.
- **Replacement (for both `one` and `other`):**

```
%d vakit ayarlandı
```

### `notif_prayer_body`: stilted on a notification seen five times a day

- **Turkish now:** `%1$s vakti saat %2$s itibarıyla giriyor`
- **What it says:** "Dhuhr time is entering as of 12:30". It is understandable, but *itibarıyla*
  ("as of / with effect from") is office language and sounds odd here.
- **What it should say:** "Dhuhr begins at 12:30". The replacement says "Dhuhr time entry: 12:30".
  It keeps both placeholders free of suffixes and stays true even when a phone delivers the alert late.
- **Replacement:**

```
%1$s vaktinin giriş saati: %2$s
```

### `settings_alarm_sound_desc`: suggests the app supplies an adhan

- **Turkish now:** `Telefonunuzdaki herhangi bir zil sesini ya da ezanı seçin`
- **What it says:** "Choose any ringtone on your phone, or **the** adhan". The definite form *ezanı*
  makes it sound as if the app has a built-in adhan to pick. It does not.
- **What it should say:** pick any tone or adhan *already on your phone*.
- **Replacement:**

```
Telefonunuzda zaten bulunan herhangi bir zil sesi ya da ezan kaydı seçin
```

### `method_egyptian`: "Survey" translated as "Research"

- **Turkish now:** `Mısır Genel Araştırma Kurumu` ("Egyptian General **Research** Authority")
- **What it says:** the body is the Egyptian General Authority *of Survey* (land surveying). "Survey"
  was taken in its other sense, "research / questionnaire". The English label shortens the name to
  "Egyptian General Authority". The Turkish expanded it and got the expansion wrong.
- **Replacement** (the surveying sense; a native speaker should confirm this is the form Turkish
  sources use):

```
Mısır Genel Ölçme Kurumu
```

### `language_unavailable`: says something the English does not (not visible today)

- **Turkish now:** `Çeviri, ana dili bu dil olan birinin incelemesini bekliyor`
- **What it says:** "The translation is waiting to be reviewed by a native speaker." The English
  says only "Not available in this version". The Turkish claims a review is under way, which the app
  cannot know. All four languages in the picker are present in 1.3.1, so this line never appears at
  the moment (`LanguageChooser.kt` shows it only for a language that is listed but missing). Fix it
  before it does appear.
- **Replacement:**

```
Bu sürümde mevcut değil
```

## 4. NITS

- `welcome_tagline`: `Size özel` means "personal to you", which loses the *privacy* sense of
  "private". A possible fix is `Gizliliğinize saygılı namaz arkadaşınız. Her zaman ücretsiz.`
- `theme_system`: `Telefona uy` can sound like "obey the phone". `Telefonla aynı` ("same as the
  phone") is the more usual wording.
- `wear_in_h_m` / `wear_in_m` leave out "in", so the tile shows `05:14 · 2sa 10dk` with no word for
  "in". This is acceptable given the tile has about 8 characters of room.
- The mosque's printed timetable is `vakit çizelgesi` in the disclaimer and `mosque_diff_congregation`,
  but `pano` (board) in `adjustment_limit_note` and `adjustment_pick_time_title`. Both are understood.
  One word throughout would be tidier.
- `disclaimer_body` paragraph 5: `her zaman aynı görüşte değildir` means "do not *always* agree",
  where the English says "do not *all* agree". The difference is harmless.

## 5. What I could not judge (needs a native Turkish speaker)

- **"Sabah" for Fajr.** It is understood by every Turkish speaker, and many prayer apps use it, so
  I am fairly confident it is acceptable. I am only moderately confident it is the *best* label. The
  risk: traditional Turkish wall calendars list **İmsak** and **Sabah** as two *different* times.
  İmsak is when Fajr begins, and Sabah is a later time for praying it in congregation. A reader raised
  on those calendars could take the app's "Sabah 05:14" for the congregation time, when it is
  actually the start time. That runs against the app's own line that it shows when a prayer becomes
  due. "İmsak" has its own risk: in Ramadan it means "stop eating now", and for a non-Diyanet method
  the app's Fajr will not match Diyanet's İmsak. A native speaker, ideally one who uses Diyanet
  timetables, should choose between the two.
- **"Mezhep" for both Sunni/Shia and the fiqh school** (`sect_title`, `label_school`,
  `settings_school`, `consent_never_list`). This is normal Turkish, and in the consent list it covers
  more than the English "sect" (madhab is never sent either, so the wider word is still true).
  Whether "Hangi mezhebe uyuyorsunuz?" reads naturally as a Sunni-or-Shia question is a call for a
  native speaker.
- **The three "quiet" corrections** (`settings_alerts_help`, `polar_notice_alarm`,
  `settings_alarm_on_approximate_desc`) now say "quieter" (`daha sessiz`) or "no alarm, only a
  notification". To me they read correctly: a normal notification, not silence. *Sessiz* also means
  "silent", though, so only a native ear can say whether "daha sessiz" ever sounds like "nearly
  silent". No other string has the quiet/silent slip. Everything that says `sessiz` (the next-prayer
  badge, the Do Not Disturb card, "stay quiet when the phone is silent") is genuinely meant to be
  silent.
- **Register and tone in the disclaimer:** whether `düpedüz` (in "I may simply be wrong") sounds
  humble or blunt, and whether `dualarınızda anın` is the warmest natural way to ask for duas.
- **`wear_disclaimer` length.** The watch has a hard three-line limit for this line. The Turkish is
  shorter in characters than the English, but Turkish words are long and the line wraps by word.
  An assistant with emulator access can confirm the fit with `tools/wear-verify.sh`. This is not
  something for the owner to check.
- General naturalness: whether `Camiye göre ayarla` reads well as the name of a screen
  ("Match your mosque"), and whether `Sorumluluk reddi` (a legal-sounding "Disclaimer") fits a
  screen that is deliberately not legal boilerplate.
