# Independent review: Urdu (ur-PK)

Reviewed 8 Oct 2026 by a second model that did not write the translation. Method: the three Urdu
files were back-translated into English first, without looking at the English
(`backtranslation-ur-PK.md`). Only then were they compared with the English sources, their
translator comments and `notes-ur-PK.md`. One claim was checked against the code:
`Notifications.kt` puts a prayer notification on an `IMPORTANCE_HIGH` channel with the phone's
default notification sound. So a "notification" does make a sound. Only the next-prayer badge
(`IMPORTANCE_LOW`) is silent.

## 1. Verdict

The disclaimer, the dua paragraph, the consent body, the prayer names and the Hijri months are
faithful and need no change. The translation should not ship until four strings are fixed: two
of them call a prayer notification "silent" when it does make a sound, one makes a method
description sound like "the standard for your country", and one consent line promises that
something is never sent when the app does send a rough version of it.

## 2. Blockers

### B1. `settings_alerts_help`: "quiet" became "silent"

- **Urdu:** ہر نماز کے لیے یہ الگ چنیں۔ اطلاع خاموش ہوتی ہے اور وائبریٹ کرتی ہے۔ الارم آپ کی چنی ہوئی آواز اونچی بجاتا ہے، جیسے صبح جگانے والا الارم۔
- **What it says:** "A notification is **silent** and vibrates." "Plays your chosen sound *high*."
- **What it should say:** "A notification is **quiet** (low key) and vibrates." A notification
  plays the phone's normal notification sound. This is the sentence a user reads when choosing
  between Notification and Alarm, so it has to describe the choice correctly. The same error was
  found in the Indonesian and Turkish versions. "آواز اونچی بجاتا ہے" is also unnatural Urdu,
  because اونچی here reads as "high", not "loud".
- **Proposed:** ہر نماز کے لیے یہ الگ چنیں۔ اطلاع ہلکی آواز کے ساتھ آتی ہے اور وائبریٹ کرتی ہے۔ الارم آپ کی چنی ہوئی آواز زور سے بجاتا ہے، جیسے جگانے والا الارم۔

### B2. `polar_notice_alarm`: "quiet notifications" became "silent notifications"

- **Urdu:** ان دنوں آپ کے الارم بجنے کے بجائے **خاموش اطلاعات** کی صورت میں آتے ہیں، کیونکہ ایپ عین منٹ کی ضمانت نہیں دے سکتی۔ …
- **What it says:** On these days your alarms come as **silent** notifications.
- **What it should say:** They come as **quiet** notifications. The code turns the alarm into an
  ordinary prayer notification, and that still plays the normal notification sound. This is
  safety text about whether the user will be woken for Fajr, so it must describe exactly what
  happens.
- **Proposed:** ان دنوں آپ کے الارم بجنے کے بجائے ہلکی آواز والی اطلاعات کی صورت میں آتے ہیں، کیونکہ ایپ عین منٹ کی ضمانت نہیں دے سکتی۔ اگر آپ پھر بھی جگایا جانا چاہتے ہیں تو ترتیبات میں “تخمینی دنوں میں بجائیں” آن کریں۔

### B3. `method_muslim_world_league_desc`: "The standard here" now reads as "the standard of this place"

- **Urdu:** ایک عام طریقہ جو بہت سے ملکوں میں رائج ہے۔ **یہاں کا معیاری طریقہ۔**
- **What it says:** "…The standard method **of here**." In Urdu, یہاں کا means "belonging to this
  place". A reader in Lahore will take it to mean that this is the standard method in Pakistan,
  which is wrong: Karachi is listed separately as the method common there. The source comment
  forbids exactly this: *"Never sell one as the method for your country."*
- **What it should say:** This is the app's default.
- **Proposed:** ایک عام طریقہ جو بہت سے ملکوں میں رائج ہے۔ اس ایپ میں طے شدہ طریقہ۔
  (`sect_sunni_desc` already uses طے شدہ for "by default", so the two will match.)

### B4. `consent_never_list`: "coordinates" widened to "whereabouts"

- **Urdu (first bullet):** • آپ کی GPS پوزیشن یا **محلِ وقوع** (کوآرڈینیٹس)
- **What it says:** "Never sent: your GPS position or your **whereabouts/location** (coordinates)."
- **What it should say:** "Your GPS position or coordinates." محلِ وقوع means location in
  general. But `consent_sent_list` says, correctly, that your approximate area *is* sent (Google
  works it out from your internet connection). So the Urdu consent text contradicts itself and
  promises more than the English does. Consent text must not say more than the facts.
- **Proposed:** • آپ کی GPS پوزیشن یا کوآرڈینیٹس

## 3. Should fix

- **`settings_alarm_on_approximate_desc`.** This is the same "quiet/silent" problem as B1 and B2,
  in a milder form. "Alarms stay quiet on those days" became "ان دنوں الارم خاموش رہتے ہیں", which
  means "alarms stay silent", so a reader may think nothing arrives at all. Proposed:
  جب سورج طلوع یا غروب نہیں ہوتا تو اوقات کم عرض بلد سے معلوم کیے جاتے ہیں۔ ان دنوں الارم نہیں بجتے، صرف ہلکی آواز والی اطلاع آتی ہے، جب تک آپ اسے آن نہ کریں۔
- **`qibla_no_compass`.** An instruction has become a label. The English "Face %1$d° from true
  north" tells the user what to do. The Urdu "رخ: حقیقی شمال سے %1$d°" uses the same رخ: pattern
  as `qibla_heading_now` ("رخ: %1$d°", the phone's current heading). On a phone with no compass, a
  reader may take the number as where they are facing now. Proposed: رخ کریں: حقیقی شمال سے %1$d°
- **`method_auto_desc`.** A hedge has been strengthened. "Close to local practice in **much of**
  the world" became "دنیا کے **بیشتر** حصوں میں", which means "in **most** of the world". Proposed:
  دنیا کے بہت سے حصوں میں مقامی عمل کے قریب۔
- **`channel_alarm_desc`.** "ایک اونچا الرٹ" reads as "a tall/high alert". Users see this in
  Android's notification settings. Proposed: آپ کی چنی ہوئی آواز کے ساتھ زیادہ اونچی آواز والا الرٹ
- **`wear_in_h_m`, `wear_in_m` (watch tile).** "%1$d گھنٹے %2$d منٹ باقی" is about 19 characters,
  and the tile line has room for about 8. Combined with the time in `wear_tile_detail`, it will
  almost certainly be cut off. It has to be screenshotted on the tile before release. If it clips,
  drop باقی first: "05:14 · 2 گھنٹے 10 منٹ" still reads correctly.

## 4. Nits

- `notif_next_title` "%1$s، %2$s بجے": %2$s is already a formatted clock time, so a time with
  AM/PM will show as "4:30 PM بجے". Suggest "%1$s، %2$s".
- `channel_ongoing` "اگلی نماز کا بیج": بیج is also the everyday word for "seed". Suggest
  "اگلی نماز کی مستقل اطلاع".
- `settings_ongoing_badge` "دکھائے رکھیں" is stiff. Suggest "اگلی نماز اطلاعات میں دکھاتے رہیں".
- `settings_exact_alarms_desc`, `confirm_exact_alarm_body`: "ہو سکتا ہے سرے سے آئے ہی نہیں".
  Standard grammar after ہو سکتا ہے is نہ: "آئے ہی نہ".
- `method_karachi_desc` "عام رائج" is clipped. Suggest "پاکستان اور جنوبی ایشیا میں عام طور پر رائج۔"
- `action_decrease` / `action_increase` "فجر کم کریں" is spoken by a screen reader and sounds like
  "reduce Fajr". Suggest "%1$s: وقت کم کریں" / "%1$s: وقت بڑھائیں".
- "عرض بلد" (4 strings): every other compound in the file carries the izafat (خطِ استوا،
  مکتبِ فکر). For consistency use عرضِ بلد.

## 5. Verdict on the translator's doubts (`notes-ur-PK.md`)

| Doubt | Verdict |
|---|---|
| Disclaimer wording: دینی اتھارٹی, شرعی حکم نہیں, اپنی مسجد کی پیروی کریں, نہ کوئی ضمانت ہے نہ درستی کا کوئی وعدہ | All correct. All four points survive, the first person stays humble, "follow your mosque" appears twice as an instruction, and "I can simply be wrong" is kept. No change. |
| Dua paragraph, اگر آپ مناسب سمجھیں, اہلِ خانہ | Correct. It appears only in the last paragraph, and دعا occurs nowhere else. |
| آسمان (سیٹلائٹ) کا سگنل for "poorer view of the sky" | Acceptable. It explains rather than adds a claim. |
| مقررہ وقفے for Umm al-Qura's "fixed time after Maghrib" | Agree. It is better than مقررہ وقت. |
| طلوعِ آفتاب with the izafat | Keep it. The tests pin it byte for byte. |
| جمادی الاول / الثانی, ذیقعد | جمادی الاول / الثانی are what Pakistani calendars print, so keep them. ذیقعد is less usual than ذوالقعدہ or ذیقعدہ. It is the owner's call (glossary change). |
| مکتبِ فکر vs مسلک for Sunni/Shia | Keep مکتبِ فکر. In Pakistan, مسلک often means Deobandi/Barelvi/Ahl-e-Hadith, which the app must not touch. |
| فقہ for Madhab, and فقہ in `mosque_diff_asr` | Agree with both. "فقہ: حنفی" is standard, and using فقہ in `mosque_diff_asr` fixes a real looseness in the English. |
| Postpositions left after placeholders (`notif_prayer_title`, `notif_prayer_body`, `qibla_subtitle`, `pdf_period_onwards`) | Fine. Urdu postpositions are separate words and do not change shape. Only `notif_next_title` needs a change, for the بجے reason above. |
| "usage counts" as اعداد و شمار | Acceptable and used consistently. It is slightly weightier than "counts", but not misleading. |
| بے ترتیب (رینڈم) | Fine as it is. |
| “ڈسٹرب نہ کریں” matches Android's label | Probably right, but unverified. Check it on the Redmi or the S23 set to Urdu. |
| روشن / گہرا | Understood. Android tends to say "گہری تھیم". Minor. |
| قطب نما | Keep it. It is the standard word. |
| `consent_never_list` "prayer calculation setting" rendered literally | The literal rendering is fine. The real problem in that string is the GPS line (B4). |
| Miles kept in `location_sheet_body` | Agree. The figure was measured, so converting it would change a fact. |
| `sect_shia_desc` keeps "Jafari (Ithna Ashari)" in Latin script | Keep it, to match `method_jafari`, which rule 8 pins. |
| Owner's name in Latin script | Keep it. Brief rule 9. |
| Watch countdown length | Agree it is the biggest layout risk. See Should fix. |
| `wear_disclaimer` at 68 characters fits three lines | Likely, but unverified. It needs `tools/wear-verify.sh`. |
| Latin-first paragraphs render RTL | Not something a text review can settle. See below. |
| ایڈجسٹ in the plural | Agree. درست would imply the original time was wrong. |

## 6. What I could not judge, for a native speaker or a screen

- Whether "ہلکی آواز" (proposed in B1, B2 and Should fix) is the most natural Pakistani way to say
  "low-key, normal notification sound". دھیمی آواز is the alternative. The point that must survive
  is only "not silent".
- Whether ذیقعد or ذوالقعدہ is what the owner's mosque calendar prints.
- Whether "ڈسٹرب نہ کریں" is the exact label on an Urdu Android phone.
- Fit on screen: the watch tile countdowns, the watch disclaimer, `action_use_makkah` and
  `consent_yes` as buttons, and how sentences starting with a Latin word (SajdaTime, Android,
  Google) render in a right-to-left paragraph. None of this can be judged from text. It needs the
  first Urdu emulator run and `tools/wear-verify.sh`.
- Overall naturalness of register. It reads as correct, formal Pakistani Urdu with consistent
  آپ, with no Hindi-leaning words and no heavy Persianisation that I could find. But I am a model,
  not a native reader.
