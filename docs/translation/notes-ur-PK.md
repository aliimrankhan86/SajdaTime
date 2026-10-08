# Urdu translation notes (ur-PK)

Written 8 Oct 2026 by the assistant that produced the three `values-ur/strings.xml` files
(`app`, `core`, `wear`). This is an AI-assisted translation. Nobody who speaks Urdu as a first
language has read it yet. You do, so this file is written for you: it tells you which six
strings carry the most weight and should be read in full, then lists every place where I had
to choose between two reasonable options, every place where the English itself looked odd,
and every term I am not certain is the one a Pakistani reader expects.

`python3 tools/check-translation.py ur ur-PK` prints `0 error(s), 0 warning(s)` for these
files. That check proves the mechanical things only: every key present, every placeholder
kept, the disclaimer still in seven paragraphs, no bare apostrophe. It cannot tell whether the
Urdu is right. That is what this file and your reading are for.

One housekeeping note first: the brief (`BRIEF.md`, safeguard 1) calls this file `notes-ur.md`,
but its own "Notes file" section asks for `notes-<tag>.md`, so it is `notes-ur-PK.md`. Nothing
else refers to it.

---

## 1. The six strings to read yourself

These are the ones where a wrong word changes religious meaning, weakens a safeguard you
approved, or misstates what the app sends about a person. Please read each one in full.

### 1.1 `disclaimer_body` (the whole disclaimer)

**Why it is risky.** It is the one screen every user sees once, it has to make four separate
points (where the times come from, how they can be wrong, follow your mosque, no warranty), and
every hedge in it is deliberate. It is written in your voice, first person, as a humble person
who is not a Mufti or Aalim. Anything that sounds like a ruling, or like confidence, is a
mistake.

**English**

> SajdaTime is a helper, not a religious authority.
>
> These times are not given to me by a mosque, a scholar, or any authority. Your phone works them out from the position of the sun, using calculation methods that Islamic authorities and scholars have published. I am not a Mufti, Aalim, or an expert in fiqh, and this app was built with the help of artificial intelligence. Software can carry faults, a phone can hold the wrong location or the wrong clock, a phone saving its battery can delay an alert, and I can simply be wrong.
>
> So please take what you see here as a calculation and not a ruling. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction looks off to you, or you are unsure, ask them or someone else qualified to advise you. The app is free and is given as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.
>
> If you use the watch, please treat it as a convenience rather than the last word. A watch has a smaller compass and a poorer view of the sky than a phone, and it may be working from a location your phone found some time ago. For the Qibla especially, your phone is the more accurate of the two. When it matters, check there.
>
> Mosques do not all agree on when a prayer starts. Isha varies the most. In Britain and further north the gap between one method and another can be over an hour, because scholars measure the fading of the twilight differently. I cannot settle that, and the app does not try to. If the times here do not match your mosque, ask them which method they use and pick it in Settings, under Calculation method. If a few minutes still separate you after that, Match your mosque lets you adjust each prayer, and the date, by hand. Where a difference remains, follow your mosque.
>
> One more thing. This app shows when a prayer becomes due. Your mosque then sets its own time for the congregation, usually a little later, so the board there and the times here will often differ. Neither is wrong.
>
> And one request, if you would: please remember me, my family, and my parents in your duas.

**Urdu**

> SajdaTime ایک مددگار ہے، کوئی دینی اتھارٹی نہیں۔
>
> یہ اوقات مجھے کسی مسجد، کسی عالم یا کسی دینی ادارے نے نہیں دیے۔ آپ کا فون انہیں سورج کی پوزیشن سے خود معلوم کرتا ہے، ان حسابی طریقوں کے ذریعے جو اسلامی ادارے اور علمائے کرام شائع کر چکے ہیں۔ میں نہ مفتی ہوں، نہ عالم، نہ فقہ کا ماہر، اور یہ ایپ مصنوعی ذہانت کی مدد سے بنائی گئی ہے۔ سافٹ ویئر میں خرابی ہو سکتی ہے، فون میں غلط مقام یا غلط وقت ہو سکتا ہے، بیٹری بچانے والا فون کسی الرٹ میں تاخیر کر سکتا ہے، اور سیدھی سی بات ہے، میں خود بھی غلط ہو سکتا ہوں۔
>
> اس لیے براہِ کرم جو کچھ یہاں نظر آئے اسے ایک حساب سمجھیں، شرعی حکم نہیں۔ جہاں SajdaTime اور آپ کی مسجد میں فرق ہو، اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت آپ کو غلط لگے، یا آپ کو یقین نہ ہو، تو ان سے یا کسی اور اہل شخص سے پوچھ لیں۔ یہ ایپ مفت ہے اور جیسی ہے ویسی ہی دی گئی ہے، نہ کوئی ضمانت ہے نہ درستی کا کوئی وعدہ، اس لیے جہاں آپ کے لیے عین درست ہونا اہم ہو، وہاں براہِ کرم صرف اسی پر بھروسا نہ کریں۔
>
> اگر آپ گھڑی استعمال کرتے ہیں تو براہِ کرم اسے ایک سہولت سمجھیں، حتمی بات نہیں۔ گھڑی کا قطب نما فون سے چھوٹا ہوتا ہے اور اسے آسمان (سیٹلائٹ) کا سگنل بھی فون سے کم ملتا ہے، اور ممکن ہے وہ اس مقام پر چل رہی ہو جو آپ کے فون نے کچھ دیر پہلے معلوم کیا تھا۔ خاص طور پر قبلے کے لیے، دونوں میں آپ کا فون زیادہ درست ہے۔ جب اہمیت ہو، وہیں دیکھ لیں۔
>
> نماز کب شروع ہوتی ہے، اس پر تمام مسجدیں متفق نہیں۔ سب سے زیادہ فرق عشاء میں ہوتا ہے۔ برطانیہ اور اس سے مزید شمال میں ایک طریقے اور دوسرے کے درمیان فرق ایک گھنٹے سے زیادہ ہو سکتا ہے، کیونکہ علمائے کرام شفق کے غائب ہونے کو مختلف طریقوں سے ناپتے ہیں۔ میں اس کا فیصلہ نہیں کر سکتا، اور یہ ایپ ایسی کوشش بھی نہیں کرتی۔ اگر یہاں کے اوقات آپ کی مسجد سے نہ ملیں تو ان سے پوچھیں کہ وہ کون سا طریقہ اختیار کرتے ہیں اور ترتیبات میں “حساب کا طریقہ” کے تحت وہی چن لیں۔ اگر اس کے بعد بھی چند منٹ کا فرق رہ جائے تو “اپنی مسجد سے ملائیں” کے ذریعے آپ ہر نماز، اور تاریخ، کو خود آگے پیچھے کر سکتے ہیں۔ جہاں فرق باقی رہے، اپنی مسجد کی پیروی کریں۔
>
> ایک اور بات۔ یہ ایپ دکھاتی ہے کہ نماز کا وقت کب داخل ہوتا ہے۔ پھر آپ کی مسجد جماعت کے لیے اپنا وقت مقرر کرتی ہے، عموماً تھوڑا بعد میں، اس لیے وہاں کے بورڈ اور یہاں کے اوقات میں اکثر فرق ہوگا۔ دونوں میں سے کوئی غلط نہیں۔
>
> اور ایک درخواست، اگر آپ مناسب سمجھیں: براہِ کرم مجھے، میرے اہلِ خانہ اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔

Things to check as you read: that "دینی اتھارٹی" is how you would say *religious authority*
(alternatives in section 2); that "شرعی حکم نہیں" says *not a ruling* without overstating; that
"اپنی مسجد کی پیروی کریں" is a plain *follow your mosque*; that "نہ کوئی ضمانت ہے نہ درستی کا
کوئی وعدہ" is clearly *no warranty and no promise of accuracy*; and that the sentence about
yourself, "میں نہ مفتی ہوں، نہ عالم، نہ فقہ کا ماہر", sounds like you.

### 1.2 The final dua paragraph

**Why it is risky.** It is the one place the user is asked for anything, it is a personal
request from you, and it must appear here and nowhere else. I have checked that no other string
in any of the three files contains the word دعا.

**English:** And one request, if you would: please remember me, my family, and my parents in your duas.

**Urdu:** اور ایک درخواست، اگر آپ مناسب سمجھیں: براہِ کرم مجھے، میرے اہلِ خانہ اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔

"if you would" became "اگر آپ مناسب سمجھیں" (*if you see fit*). "my family" became "میرے
اہلِ خانہ", the polite standard; "میرے گھر والوں" is the everyday alternative if you prefer it.

### 1.3 `method_moon_sighting_desc` and `method_umm_al_qura_desc`

**Why they are risky.** These two descriptions carry hedges ("some mosques", "made for") that
stop a line being read as *the method for your country*, and the Umm al-Qura line states a
fact about how Isha is fixed. The method names themselves stay in Latin letters on purpose
(they are organisation names, brief rule 8).

| | English | Urdu |
|---|---|---|
| Moonsighting Committee | A seasonal rule made for places far from the equator. Followed by some mosques in Britain. | خطِ استوا سے دور علاقوں کے لیے بنایا گیا ایک موسمی اصول۔ برطانیہ کی کچھ مسجدیں اس پر عمل کرتی ہیں۔ |
| Umm al-Qura | The official method in Saudi Arabia. Isha is set at a fixed time after Maghrib. | سعودی عرب کا سرکاری طریقہ۔ عشاء مغرب کے بعد ایک مقررہ وقفے پر رکھی جاتی ہے۔ |

"some mosques" is "کچھ مسجدیں", kept as a hedge. "a fixed time after Maghrib" became "ایک مقررہ
وقفے پر" (*at a fixed interval*), because "مقررہ وقت پر" could be read as a fixed clock time.

### 1.4 The prayer names (`core`, shown on phone and watch)

**Why they are risky.** They are on every screen, every notification, the watch tile and the
PDF. The brief's glossary fixed them and I followed it exactly.

| English | Urdu |
|---|---|
| Fajr | فجر |
| Sunrise | طلوعِ آفتاب |
| Dhuhr | ظہر |
| Asr | عصر |
| Maghrib | مغرب |
| Isha | عشاء |

Question for you: Pakistani timetables print Sunrise as "طلوعِ آفتاب", "طلوع آفتاب" (no izafat
mark) or simply "طلوع". I used the glossary's "طلوعِ آفتاب". The izafat mark (the small zer
under ع) is the one diacritic I kept there, because without it the two words can read as a list.
If you would rather see it without the mark, it is one character.

### 1.5 The Hijri months (`hijri_months`, in order)

**Why they are risky.** A wrong month name is a wrong date, and this list decides what the
header shows during Ramadan and the Eids.

محرم، صفر، ربیع الاول، ربیع الثانی، جمادی الاول، جمادی الثانی، رجب، شعبان، رمضان، شوال، ذیقعد، ذوالحجہ

These are the glossary's spellings. Two places where Pakistani usage varies, for you to decide:
"جمادی الاول / جمادی الثانی" are also written "جمادی الاولیٰ / جمادی الآخرہ" (closer to the
Arabic), and "ذیقعد" is also written "ذیقعدہ" or "ذوالقعدہ". I did not change the glossary.

### 1.6 `consent_body` (the usage-counts consent text)

**Why it is risky.** You approved this wording as a statement of fact about what the app sends
and to whom. It must not be shortened or softened, and it must not use the word "tracking"
(it does not). It also has to agree with `UsageCounts.kt` and `docs/privacy.html`, which I did
not touch.

**English**

> SajdaTime is free and has no ads. If you say yes, it sends a few usage counts to Google Analytics so the app can be improved. They are tied to a random ID, not your name, and may be processed outside the UK. Detailed records are kept for 14 months.
>
> Using a prayer app can say something about your faith, so this is your choice, and nothing changes in the app if you say no. SajdaTime never sends your GPS position or your prayer settings.
>
> Until you choose yes, nothing is sent. You can switch it off in Settings at any time. The person responsible is Ali Imran Khan, who made the app.

**Urdu**

> SajdaTime مفت ہے اور اس میں کوئی اشتہار نہیں۔ اگر آپ ہاں کہیں تو یہ استعمال کے چند اعداد و شمار Google Analytics کو بھیجتی ہے تاکہ ایپ کو بہتر بنایا جا سکے۔ یہ ایک بے ترتیب (رینڈم) شناختی نمبر سے جڑے ہوتے ہیں، آپ کے نام سے نہیں، اور ان کی پروسیسنگ برطانیہ سے باہر بھی ہو سکتی ہے۔ تفصیلی ریکارڈ 14 مہینے رکھا جاتا ہے۔
>
> نماز کی ایپ استعمال کرنا آپ کے عقیدے کے بارے میں کچھ ظاہر کر سکتا ہے، اس لیے یہ آپ کا اپنا فیصلہ ہے، اور اگر آپ نہ کہیں تو ایپ میں کچھ نہیں بدلتا۔ SajdaTime کبھی آپ کی GPS پوزیشن یا آپ کی نماز کی ترتیبات نہیں بھیجتی۔
>
> جب تک آپ ہاں نہ چنیں، کچھ نہیں بھیجا جاتا۔ آپ اسے کسی بھی وقت ترتیبات میں بند کر سکتے ہیں۔ اس کا ذمہ دار شخص Ali Imran Khan ہے، جس نے یہ ایپ بنائی۔

"usage counts" is rendered throughout as "استعمال کے اعداد و شمار" (*usage statistics*); see
section 2. "random ID" is "بے ترتیب (رینڈم) شناختی نمبر", giving both the Urdu word and the
loanword because "بے ترتیب" alone can read as *untidy*. Your name stays in Latin letters
because the brief says so; if you would rather it read "علی عمران خان" here and in the About
rows, say so and it is a one-word change in two strings.

---

## 2. Judgement calls

Each of these is a place where two reasonable choices existed. I say which I took and why. None
of them is a secret; if you disagree with one, it is safe to change.

### Register and grammar

- **Addressing the user.** `آپ` throughout, as the brief asks. Urdu verbs agree with gender,
  and the app does not know the user's. I used the masculine honorific plural
  (`کرتے ہیں`, `بدل سکتے ہیں`), which is how Google's and Microsoft's Urdu interfaces address
  everyone. Where a neutral form cost nothing I used it instead: `Got it` is "ٹھیک ہے" rather
  than "سمجھ گیا/گئی"; `I understand` is "میں نے سمجھ لیا" (neutral because the verb agrees with
  the object); `Which school do you follow?` is "آپ کا تعلق کس مکتبِ فکر سے ہے؟" (*which school
  do you belong to*), which avoids `کرتے/کرتی`.
- **Your own voice in the disclaimer** uses masculine first-person forms ("ہو سکتا ہوں",
  "نہیں کر سکتا"), which is correct for you.
- **Punctuation.** Urdu full stop `۔`, question mark `؟` and comma `،` everywhere. Where the
  English has a colon I kept a colon, which Urdu also uses. The English disclaimer names two
  settings without quotation marks ("Calculation method", "Match your mosque"). In Urdu those
  phrases are ordinary verb phrases and would disappear into the sentence, so I put them in
  “ ” quotes, the same characters the English uses elsewhere in `polar_notice_alarm`. That is
  the only place I added punctuation the English does not have.
- **Digits.** Latin digits in literal text (`5`, `7`, `8`, `14`, `0`), as the brief requires.
  The `ur-PK` tag also makes the app format clock times and dates with Latin digits. "the 1st"
  in `export_month_desc` became "یکم", the Urdu word for the first of a month; "ten miles" and
  "six days" are words in English so they are words in Urdu ("دس میل", "چھ دن").

### Placeholders

The brief forbids gluing grammar onto a placeholder. In Urdu the grammar that follows a noun is a
postposition, which is a separate word (`کا`, `میں`, `پر`, `سے`) and does not change its shape
for the noun before it, so a space-separated postposition cannot break the way a Turkish suffix
can. I still restructured wherever it was cheap, so the placeholder stands alone or is followed by
a plain noun:

| Key | English | Urdu | What changed |
|---|---|---|---|
| `home_countdown_a11y` | %1$s in %2$s | %1$s: %2$s باقی | colon, then "remaining" |
| `qibla_heading_now` | Facing %1$d° | رخ: %1$d° | "Direction: 45°" |
| `qibla_no_compass` | Face %1$d° from true north | رخ کریں: حقیقی شمال سے %1$d° | "Face: 45° from true north", an instruction (changed after review) |
| `export_sheet_body` | ...for %1$s. | مقام: %1$s۔ | the city on its own after "Place:" |
| `export_saved` | Saved to Downloads as %1$s | ڈاؤن لوڈز میں محفوظ ہو گیا: %1$s | file name last |
| `adjustment_pick_time_description` | Set %1$s to the time... | %1$s: وہ وقت رکھیں... | colon |
| `adjustment_pick_time_title` | %1$s: the time on your mosque's board | %1$s: آپ کی مسجد کے بورڈ پر وقت | unchanged shape |
| `adjustment_now_shows` | Shows %1$s | دکھاتا ہے: %1$s | colon |
| `notif_next_body` | In %1$s | %1$s باقی | "remaining" |
| `polar_notice_body`, `pdf_approximate_note`, `wear_polar_notice` | latitude %1$d° | %1$d° عرض بلد | the noun follows the number, so "%1$d° سے" never occurs |
| `action_decrease` / `action_increase` | Decrease %1$s | %1$s: وقت کم کریں | colon, then "reduce the time" (changed after review) |

Where I left a postposition as a separate word after the placeholder, because restructuring
made the Urdu stilted: `notif_prayer_title` "%1$s کا وقت ہو گیا", `notif_prayer_body`
"%1$s کا وقت %2$s پر شروع ہوتا ہے", `notif_next_title` "%1$s، %2$s بجے", `qibla_subtitle`
"...شمال سے %1$d° پر۔", `pdf_period_onwards` "%1$s سے آگے". If the reviewer or the Kotlin
tests want these restructured too, each is a one-line change.

### Word choices (two standard options existed)

| English | Chosen | Alternative(s) | Why |
|---|---|---|---|
| religious authority | دینی اتھارٹی | دینی سند، شرعی مرجع | "اتھارٹی" is understood by everyone in Pakistan; "مرجع" is precise but learned, "سند" means a credential more than a body |
| ruling | شرعی حکم | فتویٰ | "فتویٰ" is narrower (a formal verdict); "شرعی حکم" is what a calculation is *not* |
| becomes due (`disclaimer_body` P6, `mosque_diff_congregation`) | وقت داخل ہوتا ہے | وقت شروع ہوتا ہے | "داخل ہونا" is the exact fiqh idiom for a prayer time coming in, and the two strings use it identically, as the source comment asks |
| compass | قطب نما | کمپاس | the standard Urdu word, used by Qibla apps; "کمپاس" is the street word |
| a poorer view of the sky | آسمان (سیٹلائٹ) کا سگنل کم | literal "آسمان کا نظارہ" | the literal phrase makes no sense in Urdu; this says what is meant (weaker satellite signal). It is a small explanatory addition, flagged honestly |
| Disclaimer (About row label) | ضروری وضاحت | اعلانِ دستبرداری، انتباہ | "دستبرداری" is legal boilerplate, which the English deliberately avoids |
| usage counts | استعمال کے اعداد و شمار | استعمال کی گنتی | "گنتی" (counts) reads oddly; "اعداد و شمار" is the ordinary word for statistics. It is slightly weightier than "counts" |
| alerts / notification / alarm | الرٹ / اطلاع / الارم | تنبیہ for alert | "اطلاع" is Android's own Urdu for notification; "الرٹ" and "الارم" are everyday loanwords |
| Do Not Disturb | “ڈسٹرب نہ کریں” | خلل نہ ڈالیں | I believe "ڈسٹرب نہ کریں" is the label Android itself shows on an Urdu phone, but I could not verify that from here. Please check against a Redmi or Galaxy set to Urdu |
| exact alarms | عین وقت کے الارم | درست الارم | "exact" here means *at the exact minute*, which "عین وقت" says and "درست" (correct) does not |
| timetable | اوقاتِ نماز | ٹائم ٹیبل، نظام الاوقات | "اوقاتِ نماز" is what mosque sheets are called; "ٹائم ٹیبل" is also universally understood |
| Light / Dark (theme) | روشن / گہرا | لائٹ / ڈارک | the Urdu words; Android's own Urdu uses "گہرا" for the dark theme as far as I recall |
| Follow phone | فون کے مطابق | فون جیسا | "according to the phone" |
| latitude | عرض بلد | — | standard geography term; no plain alternative exists |
| the shade (notification drawer) | اطلاعات | نوٹیفکیشن شیڈ | "keep the next prayer in the shade" became "اگلی نماز اطلاعات میں دکھائے رکھیں" (*keep showing the next prayer in notifications*) |
| School of thought (Sunni/Shia) | مکتبِ فکر | مسلک | glossary. In Pakistan "مسلک" is the everyday word for both sect and madhab; see section 4 |
| Madhab | فقہ | مسلک، مذہب | glossary |
| Approximate | تخمینی | اندازاً | used consistently for `polar_notice_title`, `notif_approximate`, `settings_alarm_on_approximate`, `pdf_area_label`, `wear_tile_detail_approx` ("تقریباً" there, as a single word before a time), so the three polar surfaces read as one idea, as the source comment asks |
| Now (pill) | ابھی | اب | one short word; "اب" is shorter still if the pill clips |
| Next (pill) | اگلی | — | feminine, agreeing with نماز |
| Match your mosque | اپنی مسجد سے ملائیں | — | same words in the Settings row and inside the disclaimer, so they recognise each other |
| Calculation method | حساب کا طریقہ | طریقۂ حساب | plainer; same words in the first-run step, Settings and the disclaimer |
| charity project | ایک خیراتی منصوبہ | صدقۂ جاریہ | `about_charity_desc` uses the plain phrase; `about_credits_desc` uses "صدقۂ جاریہ" for "ongoing charity", which is the exact term |

### Method names and their descriptions (`core`)

- The names stay in Latin letters exactly as in English (brief rule 8): Muslim World League,
  Egyptian General Authority, University of Karachi, Umm al-Qura, Dubai, Moonsighting Committee,
  ISNA, Kuwait, Qatar, Kemenag & MUIS, Diyanet, Jafari (Ithna Ashari), Institute of Geophysics.
  The descriptive words around them are translated: "مکہ مکرمہ", "شمالی امریکہ",
  "انڈونیشیا اور سنگاپور", "ترکی", "تہران".
- `sect_shia_desc` ("Jafari (Ithna Ashari) times") is not a `method_*` key, but it contains the
  method name, so it also stays Latin: "Jafari (Ithna Ashari) کے اوقات". An Urdu reader would
  expect "جعفری (اثنا عشری)". Your call.
- `method_north_america_desc`: "The Islamic Society of North America" is the organisation's
  full name, so I kept it in Latin letters too, followed by the Urdu sentence.
- `method_turkey_desc`: "Turkey's Presidency of Religious Affairs" became "ترکی کا ادارۂ مذہبی
  امور" (*Turkey's department of religious affairs*). "Presidency" has no natural Urdu
  equivalent for a government body and the name Diyanet sits directly above it.
- The hedges are kept as hedges: "many countries" → "بہت سے ملکوں", "some mosques" →
  "کچھ مسجدیں", "many mosques" → "بہت سی مسجدیں", "Common across" → "عام رائج", "usual" →
  "عام", "a convention" → "ایک طریقہ", "Close to local practice in much of the world" →
  "دنیا کے بیشتر حصوں میں مقامی عمل کے قریب".

### The watch (`wear`)

- `wear_disclaimer` has a hard three-line budget on the 192dp round face (the source comment
  explains why). English fits at 78 to 88 characters. My Urdu is 68 characters:
  "مددگار ہے، دینی اتھارٹی نہیں۔ اوقات حساب سے ہیں۔ اپنی مسجد کی پیروی کریں۔" Urdu Naskh is
  narrower than Latin per character, so it should fit, but **it has not been screenshotted**.
  `./tools/wear-verify.sh` is the check, and I could not run it here.
- Countdowns. The source abbreviates to "2h 10m" because a tile line holds about eight
  characters. Urdu has no accepted abbreviation for hours or minutes, so I used the full words:
  "%1$d گھنٹے %2$d منٹ". My first draft of the two `wear_in_*` strings added "باقی" (*remaining*)
  for the English "in"; after review it was dropped as the only shortening Urdu allows without
  inventing abbreviations ("گھ", "م") that no Urdu reader has seen. "05:14 · 2 گھنٹے 10 منٹ" is
  still about 14 characters against a budget of about eight, so it **may be cut short on the
  tile** and is the biggest layout doubt. The same full words are used on the phone, where there
  is room.
- "1 گھنٹے" is grammatically the plural form with the number 1 ("1 گھنٹہ" is correct). The
  string is not a plural resource, so one form has to serve both; the plural is what Urdu
  software usually shows and nobody misreads it.

### Writing direction (verified, not assumed)

Several strings begin with a Latin word: the disclaimer ("SajdaTime ایک مددگار ہے"), the consent
text, "Android ایپس کو...", "Google کو بھیجے جاتے ہیں", "Ali Imran Khan،", "PDF میں محفوظ کریں".
If the app chose paragraph direction from the first letter, those paragraphs would come out
left-to-right with the Urdu inside them in the wrong order. I checked the code rather than
guessing: `AppLocale.wrap` pins the whole configuration, including layout direction, to the
language of the words (`ur-PK`, right-to-left), Compose text follows that layout direction when
no direction is set explicitly (nothing in the app sets one), and the PDF draws every line with
`drawTextRun(..., rtl)`. So these should render correctly. Notifications are drawn by the system
and pick direction from the first strong letter, which in every notification string here is an
Urdu prayer name or Urdu word. I have not seen any of this on a screen; the first emulator run
in Urdu is where to look.

### Other small decisions

- `bismillah_a11y` is the spoken fallback for the Arabic calligraphy. In Urdu it is written in
  plain Urdu script, "بسم اللہ الرحمٰن الرحیم", which an Urdu screen reader pronounces correctly.
- `list_separator` is the quoted string `"، "` (Urdu comma and space), keeping the quoting that
  stops Android trimming the space.
- `location_no_fix`: "location is turned on" refers to the phone's setting, so it reads "فون کا
  مقام (لوکیشن) آن ہے", giving the loanword in brackets because that is what the phone's own
  quick-settings tile is called by most people.
- `city_field_helper`: "Manchester, Lahore, Cairo" became "مانچسٹر، لاہور، قاہرہ". The user
  types a city for a lookup service; the service accepts Urdu spellings of major cities, but a
  Latin spelling is safer if a lookup fails. The example is only an example.
- `about_translation_note` refers to the Send feedback row by its Urdu label "رائے بھیجیں", which
  is the same string as `about_feedback`, so they cannot drift apart within this file.
- `theme_system` is "فون کے مطابق" and `language_phone` is "فون کی زبان", both built on "فون"
  rather than "ڈیوائس" (device), as the English uses "phone".
- "I can simply be wrong" is "سیدھی سی بات ہے، میں خود بھی غلط ہو سکتا ہوں" (*put simply, I myself
  can also be wrong*).

---

## 3. Things in the English source that look wrong, unclear or hard to translate

Reported, not fixed, as the brief asks.

1. **"school of thought" is used two ways.** In the app, `School` / `School of thought` is the
   Sunni/Shia choice, and `Madhab` is Hanafi/Shafi'i/Maliki/Hanbali. But `mosque_diff_asr` says
   "Asr depends on the school of thought. Hanafi mosques..." where the thing that moves Asr is the
   madhab. I translated that sentence with "فقہ" (the madhab word) so the Urdu is internally
   consistent, which means the Urdu does not follow the glossary's word for "school of thought"
   in that one place. The English could say "madhab" there.
2. **`consent_never_list` says "your prayer calculation setting"**, which is vague (it seems to
   mean the madhab/Asr setting, since method and alert settings are listed separately). I
   translated it literally as "آپ کی نماز کے حساب کی ترتیب". It also says "Your sect" where the
   rest of the app says "School"; I used the in-app word "مکتبِ فکر" for both so the user
   recognises the setting.
3. **`location_sheet_body` measures in miles** ("ten miles is about a minute") for an audience
   that mostly uses kilometres. I kept "دس میل" because the figure is measured, not estimated
   (source comment), and converting it is a change of fact. Sixteen kilometres would be the
   equivalent if the English is ever changed.
4. **The watch countdown budget (about eight characters) cannot be met in Urdu words.** See the
   watch section above. This is a constraint on the design, not a wording problem.
5. **`BRIEF.md` safeguard 1 names this file `notes-ur.md`**; the "Notes file" section names it
   `notes-<tag>.md`. I followed the latter. One of the two sentences in the brief should change.
6. **The Kotlin tests.** The working tree already removes `NoTranslationsYetTest` (the old
   "no translation folder at all" guard) and replaces it with `TranslationScopeTest`, which allows
   exactly `in`, `tr` and `ur`, and `TranslationIntegrityTest`, which repeats the Python checker's
   rules in the build and adds three more: the six prayer names must be exactly the glossary's
   spellings, the word دعا must appear once in the disclaimer and nowhere else in the language, and
   the method names must keep their Latin tokens. I read those tests and compared my files against
   them by script (prayer names match byte for byte, including the izafat mark; دعا appears once;
   every method token is present; no direction mark). I did not run Gradle, as instructed, so I
   have not seen them pass; the comparison was done outside the build.
7. **`sect_shia_desc` is religious content with a method name inside it** ("Jafari (Ithna
   Ashari)"), and the brief's rule 8 lists only `method_*` keys. I kept it Latin for consistency
   with `method_jafari`; see section 2.

---

## 4. Terms I am not sure are the standard ones (questions for a native reader)

1. **مکتبِ فکر vs مسلک.** For the Sunni/Shia question, most Pakistanis would say "مسلک". The
   glossary says "مکتبِ فکر", which is correct but formal. Should "Which school do you follow?"
   read "آپ کا مسلک کیا ہے؟" instead?
2. **فقہ for "Madhab".** "فقہ: حنفی" is understandable, but many would write "مسلک: حنفی" or
   "فقہی مسلک". The glossary says فقہ.
3. **“ڈسٹرب نہ کریں” for Do Not Disturb.** I believe this is Android's own Urdu label but could not
   check a real phone from here. If the system shows something else, the two `settings_dnd_*`
   strings should use whatever the phone says, so the user can find the setting.
4. **روشن / گہرا for Light / Dark theme.** Android's Urdu may say "لائٹ" / "ڈارک". Either is
   understood.
5. **قطب نما for compass.** Standard, but "کمپاس" is what people say. Three strings use it.
6. **عرض بلد for latitude.** Correct, but a non-technical reader may not know it. The sentence
   around it ("%1$d° عرض بلد سے معلوم کیے گئے ہیں") still makes sense if the term is unfamiliar.
7. **ذیقعد and جمادی الاول** spellings, as in section 1.5.
8. **طلوعِ آفتاب with or without the izafat mark**, as in section 1.4.
9. **ایڈجسٹ** in the plural `adjustment_summary_times` ("%d نماز ایڈجسٹ کی گئی"). A pure Urdu
   alternative is "درست کی گئی", but "درست" also means *correct*, which would imply the original
   time was wrong. "ایڈجسٹ" is a common loanword and says exactly what happened.
10. **بے ترتیب (رینڈم)** for "random" in the consent text. "بے ترتیب" alone means *disordered*;
    with the loanword in brackets it is unambiguous but a little heavy. "رینڈم" alone would also
    be understood.

## After review (8 Oct 2026)

An independent second model back-translated the three files and reviewed them
(`review-ur-PK.md`). It found the six owner-read strings in section 1 faithful, so those are
unchanged and still current. What I applied, and the one place I deviated:

**Blockers, all applied.**
- `settings_alerts_help`: "quiet" had become "خاموش" (silent). A prayer notification does play
  the phone's normal notification sound (`Notifications.kt`, IMPORTANCE_HIGH), so it now reads
  "اطلاع ہلکی آواز کے ساتھ آتی ہے" (*comes with a soft sound*), and "آواز اونچی بجاتا ہے" became
  "آواز زور سے بجاتا ہے" (*plays loudly*). I kept "صبح جگانے والا الارم" rather than the proposed
  "جگانے والا الارم", so it matches `settings_alarm_respect_silent_desc`. That is the only
  deviation from a proposed wording.
- `polar_notice_alarm`: "خاموش اطلاعات" became "ہلکی آواز والی اطلاعات" for the same reason. This
  is the Fajr safety text, so it has to say what actually happens.
- `method_muslim_world_league_desc`: "یہاں کا معیاری طریقہ" read as *the standard method of this
  place*, which a reader in Pakistan would take as a claim about Pakistan. It now says
  "اس ایپ میں طے شدہ طریقہ" (*the default method in this app*), matching `sect_sunni_desc`.
- `consent_never_list`: "محلِ وقوع" (whereabouts) promised more than the English, which lists only
  GPS position and coordinates; the approximate area *is* sent. Now "آپ کی GPS پوزیشن یا
  کوآرڈینیٹس", exactly the English.

**Should fix, all applied.** `settings_alarm_on_approximate_desc` now says the alarms do not ring
and a soft-sound notification arrives instead ("ان دنوں الارم نہیں بجتے، صرف ہلکی آواز والی اطلاع
آتی ہے"), consistent with `polar_notice_alarm`. `qibla_no_compass` is an instruction again,
"رخ کریں: حقیقی شمال سے %1$d°", so it cannot be read as the current heading. `method_auto_desc`
"بیشتر" (*most*) is back to the hedge "بہت سے" (*much of / many*). `channel_alarm_desc` now says
"زیادہ اونچی آواز والا الرٹ" (*a louder alert*). The watch `wear_in_h_m` / `wear_in_m` dropped
"باقی", so "05:14 · 2 گھنٹے 10 منٹ" is as short as Urdu words allow (about 14 characters, still
over the eight-character budget). It must be screenshotted with `tools/wear-verify.sh`; if it
still clips, the only remaining option is a design change, not a wording one.

**Nits, all applied:** `notif_next_title` drops "بجے" (the time already carries AM/PM);
`channel_ongoing` is "اگلی نماز کی مستقل اطلاع" (بیج also means *seed*); `settings_ongoing_badge`
is "دکھاتے رہیں"; "آئے ہی نہیں" is "آئے ہی نہ" in both exact-alarm strings; `method_karachi_desc`
is "عام طور پر رائج"; the stepper labels are "%1$s: وقت کم کریں" / "%1$s: وقت بڑھائیں" so a screen
reader does not say *reduce Fajr*; and "عرض بلد" carries the izafat, "عرضِ بلد", in all four
places, like the other compounds.

**Rejected:** nothing. The reviewer's verdicts on my earlier doubts (keep مکتبِ فکر, keep قطب نما,
keep the miles, keep the Latin names) stand as written in sections 2 to 4. "ہلکی آواز" versus
"دھیمی آواز" for the normal notification sound is still a native-reader question; either is
understood, and the point that matters, *not silent*, is now made.

After these changes `python3 tools/check-translation.py ur ur-PK` prints `0 error(s), 0 warning(s)`.

## 5. What was checked and what was not

Checked: `tools/check-translation.py ur ur-PK` reports `0 error(s), 0 warning(s)`; a separate
scan found no direction marks, zero-width characters, non-breaking spaces or bare apostrophes
in the three files; the word دعا appears only in the disclaimer's last paragraph; every
`\n`, bullet and placeholder matches the English; the disclaimer has seven paragraphs with the
dua request last; `app_language_tag` is `ur-PK`.

Not checked: nothing has been compiled, installed, or seen on a phone or watch screen. Line
fit on the watch (disclaimer and tile countdowns), the look of Latin-first sentences in
right-to-left paragraphs, and the Hijri date line in the header are the first things to look
at when it is.
