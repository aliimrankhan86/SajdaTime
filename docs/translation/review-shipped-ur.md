# Review of the shipped Urdu translation (release 1.3.1)

Written 8 Oct 2026 by an independent reviewer model that did not write the translation. Method: the
three Urdu files were back-translated into English **before** the English was read
(`backtranslation-shipped-ur.md`), then each back-translation was compared with the English source and
its translator notes. Nothing in the app was edited. Only things that need attention are listed. If a
string is not mentioned, it read correctly.

Every replacement below is ready to paste between the `>` and `</string>` of that key in
`app/src/main/res/values-ur/strings.xml` (or `core/...` / `wear/...` where marked). Placeholders and
`\n` are kept exactly. There are no apostrophes in any of them.

---

## 1. Verdict

The Urdu is faithful and safe to keep shipping. The disclaimer keeps all four points, the humble
first-person voice and the dua in the last paragraph only. The consent text says neither more nor less
than the English. I found **no blockers**. There are **13 things that should be fixed** (in 9 sections below): two softened
or strengthened sentences in the disclaimer, one in the Asr explainer and one method description, a
clumsy phrase in the consent text, four sentences that still say "tap" on cards that are no longer
tappable, one missing sentence about typing your mosque's time, and three wrong or unclear labels.

---

## 2. BLOCKERS

None. No religious term is wrong, no prayer or month name is wrong, and the disclaimer, consent,
exact-alarm and Do Not Disturb texts are not changed in meaning. "Turn left" and "Turn right" are
correctly physical (`%1$d° بائیں مڑیں` and `%1$d° دائیں مڑیں`).

---

## 3. SHOULD FIX

### 3.1 `disclaimer_body`: two small changes (one sentence in paragraph 2, one word in paragraph 5)

**(a) Paragraph 2, last clause. Urdu now:**
`اور مجھ سے بھی سیدھی سادی غلطی ہو سکتی ہے۔`

- **What it says:** "and I too can make a plain, simple mistake."
- **What the English says:** "and I can simply be wrong." "Simply" there means *just plainly wrong,
  with no excuse*, not *a small mistake*. "سیدھی سادی غلطی" reads as a minor, harmless slip, which
  softens one of the sentences the brief says must stay in force.
- **Should say:** `اور میں خود بھی غلط ہو سکتا ہوں۔` ("and I myself can also be wrong.")

**(b) Paragraph 5, first sentence. Urdu now:**
`مساجد اس پر متفق نہیں کہ نماز کا وقت کب شروع ہوتا ہے۔`

- **What it says:** "Mosques do not agree on when the prayer time begins."
- **What the English says:** "Mosques do not **all** agree…". The word "all" is a hedge: many mosques
  do agree. Without it the sentence suggests there is no agreement at all.
- **Should say:** `سب مساجد اس پر متفق نہیں کہ نماز کا وقت کب شروع ہوتا ہے۔`

**Full replacement for `disclaimer_body`** (only those two places changed; seven paragraphs, dua still
last and only there):

```
SajdaTime ایک مددگار ہے، کوئی دینی اتھارٹی نہیں۔\n\nیہ اوقات مجھے کسی مسجد، کسی عالم یا کسی اتھارٹی نے نہیں دیے۔ آپ کا فون انہیں سورج کی پوزیشن سے نکالتا ہے، اور اس کے لیے حساب کے وہ طریقے استعمال کرتا ہے جو اسلامی اداروں اور علماء نے شائع کیے ہیں۔ میں نہ مفتی ہوں، نہ عالم، نہ فقہ کا ماہر، اور یہ ایپ مصنوعی ذہانت کی مدد سے بنائی گئی ہے۔ سافٹ ویئر میں خرابیاں ہو سکتی ہیں، فون میں غلط مقام یا غلط وقت ہو سکتا ہے، بیٹری بچاتے ہوئے فون کسی الرٹ میں تاخیر کر سکتا ہے، اور میں خود بھی غلط ہو سکتا ہوں۔\n\nاس لیے براہِ کرم جو کچھ آپ یہاں دیکھیں اسے ایک حساب سمجھیں، کوئی شرعی فیصلہ نہیں۔ جہاں SajdaTime اور آپ کی مسجد میں اختلاف ہو، وہاں اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت آپ کو غلط لگے، یا آپ کو یقین نہ ہو، تو اپنی مسجد سے یا کسی اور اہل شخص سے پوچھ لیں جو آپ کی رہنمائی کر سکے۔ یہ ایپ مفت ہے اور جیسی ہے ویسی ہی دی جا رہی ہے، کسی ضمانت اور درستگی کے کسی وعدے کے بغیر۔ اس لیے جہاں بالکل درست ہونا آپ کے لیے اہم ہو، وہاں براہِ کرم صرف اسی پر بھروسہ نہ کریں۔\n\nاگر آپ گھڑی (واچ) استعمال کرتے ہیں تو براہِ کرم اسے حتمی بات نہیں بلکہ ایک سہولت سمجھیں۔ گھڑی کا کمپاس فون سے چھوٹا ہوتا ہے اور اسے آسمان بھی کم صاف نظر آتا ہے، اور ہو سکتا ہے وہ اس مقام پر چل رہی ہو جو آپ کے فون نے کچھ دیر پہلے معلوم کیا تھا۔ خاص طور پر قبلے کے لیے دونوں میں سے آپ کا فون زیادہ درست ہے۔ جب بات اہم ہو تو فون پر دیکھ لیں۔\n\nسب مساجد اس پر متفق نہیں کہ نماز کا وقت کب شروع ہوتا ہے۔ سب سے زیادہ فرق عشاء میں ہوتا ہے۔ برطانیہ میں اور اس سے آگے شمال میں ایک طریقے اور دوسرے کے درمیان فرق ایک گھنٹے سے زیادہ ہو سکتا ہے، کیونکہ علماء شفق کے ختم ہونے کو مختلف طرح سے ناپتے ہیں۔ میں یہ معاملہ طے نہیں کر سکتا، اور ایپ بھی اس کی کوشش نہیں کرتی۔ اگر یہاں کے اوقات آپ کی مسجد سے نہیں ملتے تو ان سے پوچھیں کہ وہ کون سا طریقہ استعمال کرتے ہیں، اور اسے ترتیبات میں “حساب کا طریقہ” کے تحت منتخب کریں۔ اگر اس کے بعد بھی چند منٹ کا فرق رہے تو “اپنی مسجد سے ملائیں” کے ذریعے آپ ہر نماز کو، اور تاریخ کو بھی، خود آگے پیچھے کر سکتے ہیں۔ جہاں پھر بھی فرق باقی رہے، وہاں اپنی مسجد کی پیروی کریں۔\n\nایک بات اور۔ یہ ایپ بتاتی ہے کہ نماز کا وقت کب شروع ہوتا ہے۔ پھر آپ کی مسجد جماعت کا اپنا وقت مقرر کرتی ہے، جو عموماً تھوڑا بعد میں ہوتا ہے، اس لیے وہاں کے بورڈ اور یہاں کے اوقات میں اکثر فرق ہوگا۔ دونوں میں سے کوئی بھی غلط نہیں۔\n\nاور ایک گزارش، اگر آپ کر سکیں: براہِ کرم مجھے، میرے گھر والوں کو اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔
```

### 3.2 `consent_body`: "if you say no" is written in a way that can read "if you don't say"

**Urdu now (paragraph 2):** `…اور اگر آپ نہیں کہیں تو ایپ میں کچھ نہیں بدلتا۔`

- **What it says:** "and if you say no [or: *if you don't say*], nothing changes in the app." Without
  quotation marks, `نہیں کہیں` is not natural Urdu for "say no" and can be read as "do not say".
- **Should say:** "and if you decline, nothing changes in the app": `اگر آپ انکار کریں تو`.
- Also in paragraph 3, `اس کے ذمہ دار` ("responsible for *it*") does not say what "it" is. Making it
  "responsible for these counts" matches the English "the person responsible" without adding anything.
- Nothing else in the consent text needs changing. It correctly says the counts go to Google
  Analytics, are tied to a random ID and not the name, may be processed outside the UK, are kept 14
  months, that GPS position and prayer settings are never sent, that nothing is sent before "yes", and
  that you are responsible. It does **not** promise that location is never sent (the approximate area
  is listed as sent in `consent_sent_list`). Correct.

**Replacement for `consent_body`:**

```
SajdaTime مفت ہے اور اس میں کوئی اشتہار نہیں۔ اگر آپ ہاں کہیں تو یہ استعمال کے چند اعداد و شمار Google Analytics کو بھیجتی ہے تاکہ ایپ کو بہتر بنایا جا سکے۔ یہ آپ کے نام سے نہیں بلکہ ایک رینڈم شناختی نمبر (ID) سے جڑے ہوتے ہیں، اور ہو سکتا ہے انہیں برطانیہ سے باہر پروسیس کیا جائے۔ تفصیلی ریکارڈ 14 مہینے تک رکھے جاتے ہیں۔\n\nنماز کی ایپ استعمال کرنے سے آپ کے ایمان کے بارے میں کچھ ظاہر ہو سکتا ہے، اس لیے یہ فیصلہ آپ کا ہے، اور اگر آپ انکار کریں تو ایپ میں کچھ نہیں بدلتا۔ SajdaTime کبھی آپ کی GPS پوزیشن یا آپ کی نماز کی ترتیبات نہیں بھیجتی۔\n\nجب تک آپ ہاں نہ چنیں، کچھ نہیں بھیجا جاتا۔ آپ اسے کسی بھی وقت ترتیبات میں بند کر سکتے ہیں۔ ان اعداد و شمار کے ذمہ دار علی عمران خان (Ali Imran Khan) ہیں، جنہوں نے یہ ایپ بنائی ہے۔
```

### 3.3 `mosque_diff_asr`: "up to" was dropped, so it now says Hanafi Asr is always an hour or more later

**Urdu now:** `عصر کا وقت مکتبِ فکر پر منحصر ہے۔ حنفی مساجد عصر دوسروں سے بعد میں شروع کرتی ہیں، ایک گھنٹے یا اس سے بھی زیادہ دیر سے۔`

- **What it says:** "…Hanafi mosques begin Asr later than others, **an hour or even more** later."
- **What the English says:** "…later than others, **by up to** an hour or more." The gap is often
  well under an hour (it depends on the season and place). The Urdu makes it always at least an hour.
- Second point: in this app the **Hanafi choice lives under `فقہ`** (the `settings_madhab` row), while
  `مکتبِ فکر` is the name of the **Sunni / Shia** row (`settings_school`). Saying "Asr depends on the
  `مکتبِ فکر`" points the reader at the wrong row. The glossary gives madhab = `فقہ`. (The English has
  the same overlap, "school of thought" in both places; see section 5.)
- **Should say:** "The Asr time depends on the fiqh. Hanafi mosques begin Asr later than others,
  sometimes by an hour or even more."

**Replacement for `mosque_diff_asr`:**

```
عصر کا وقت فقہ پر منحصر ہے۔ حنفی مساجد عصر دوسروں سے بعد میں شروع کرتی ہیں، بعض اوقات ایک گھنٹہ یا اس سے بھی زیادہ دیر سے۔
```

### 3.4 `method_muslim_world_league_desc` (core file): "the standard here" reads as "the standard where you live"

**Urdu now:** `ایک عمومی طریقہ جو کئی ممالک میں استعمال ہوتا ہے۔ یہاں یہی معیاری ہے۔`

- **What it says:** "A general method used in many countries. **Here, this is the standard one.**"
  `یہاں یہی معیاری ہے` can easily be read as "this is *the* standard method in your area", and `یہی`
  ("this very one") adds emphasis. The brief and the source comments forbid selling any method as "the
  method for your country".
- **What the English means:** this is the app's default. The Sunni description already uses
  `بطورِ ڈیفالٹ` for exactly this, so the same word keeps the two consistent.
- **Should say:** "A general method used in many countries. It is this app's default."

**Replacement for `method_muslim_world_league_desc`** (in `core/src/main/res/values-ur/strings.xml`):

```
ایک عمومی طریقہ جو کئی ممالک میں استعمال ہوتا ہے۔ اس ایپ میں یہی بطورِ ڈیفالٹ ہے۔
```

### 3.5 Four cards still say "tap", but since 7 Oct the card itself is not tappable

On 7 Oct the amber cards were changed so the card is no longer tappable: each now has its own button
(`ترتیبات میں اجازت دیں`, `میرا مقام مقرر کریں`, `وجہ دیکھیں`), and the English dropped the "Tap
to…" sentence. The Urdu still has it. Someone who taps the text gets nothing, and a screen reader reads
an instruction that does not work. The far-north notice also has a two-line limit, and the extra
sentence makes a third line more likely.

| Key | Urdu now ends with | Replacement (paste the whole string) |
|---|---|---|
| `method_notice_body` | `…وجہ جاننے کے لیے ٹیپ کریں۔` ("Tap to find out why.") | `یہاں فجر اور عشاء میں ایک گھنٹے یا اس سے زیادہ کا فرق ہو سکتا ہے۔` |
| `home_default_location_body` | `…اپنا مقام مقرر کرنے کے لیے ٹیپ کریں۔` ("Tap to set your location.") | `ہم معلوم نہیں کر سکے کہ آپ کہاں ہیں۔` |
| `settings_dnd_desc` | `…اجازت دینے کے لیے ٹیپ کریں۔` ("Tap to allow.") | `اس کے بغیر، جب فون ڈسٹرب نہ کریں پر ہو تو آپ کا الارم خاموش رہتا ہے۔` |
| `settings_exact_alarms_desc` | `…اجازت دینے کے لیے ٹیپ کریں۔` ("Tap to allow.") | `اس کے بغیر آپ کا فون نماز کے الرٹ ایک گھنٹہ یا اس سے زیادہ دیر تک روک سکتا ہے، اور کچھ فونز پر فجر جیسا رات کا الرٹ شاید بالکل نہ آئے۔` |

(The rest of each sentence is unchanged. The hedges "can", "on some phones" and "may not" are kept as
`سکتا ہے`, `کچھ فونز پر` and `شاید`.)

### 3.6 `settings_adjustments_help`: the sentence about typing your mosque's time is missing

**Urdu now:** `اگر آپ کی مسجد کا چھپا ہوا وقت مختلف ہے تو اسے یہاں تھوڑا آگے پیچھے کر لیں۔ پہلے حساب کا طریقہ بدل کر دیکھیں۔ …`

- **What it says:** "If your mosque's printed time is different, move it a little forward or back
  here. First try changing the calculation method. …"
- **What the English says:** "…nudge it here, **or tap a number and pick the time your mosque shows**."
  That clause is the only place that tells people they can type the time from the mosque board. Without
  it, Urdu readers will not find the feature.

**Replacement for `settings_adjustments_help`:**

```
اگر آپ کی مسجد کا چھپا ہوا وقت مختلف ہے تو اسے یہاں تھوڑا آگے پیچھے کر لیں، یا کسی عدد پر ٹیپ کر کے وہ وقت چن لیں جو آپ کی مسجد دکھاتی ہے۔ پہلے حساب کا طریقہ بدل کر دیکھیں۔ عموماً اصل فرق وہی ہوتا ہے، اور یہ بچے ہوئے چند منٹوں کے لیے ہے۔
```

### 3.7 `language_unavailable`: says something different from the English

**Urdu now:** `ترجمہ اہلِ زبان کے جائزے کا منتظر ہے`

- **What it says:** "The translation is waiting for review by native speakers."
- **What the English says:** "Not available in this version." It appears greyed out under a language
  the installed app does not contain. The Urdu tells the user something untrue: there is no translation
  waiting. It also clashes with the About note, which says the Urdu itself was not reviewed by a
  native speaker.

**Replacement:**

```
اس ورژن میں دستیاب نہیں
```

### 3.8 `settings_alarm_respect_silent`: as a switch label it reads as telling *you* to keep quiet

**Urdu now:** `فون سائلنٹ ہو تو خاموش رہیں`

- **What it says:** "When the phone is on silent, (you) stay quiet." `رہیں` is the polite command to
  the user, so the label sounds like advice to the person rather than a setting for the alarm.
- **What the English means:** the *alarm* stays quiet when the phone is on silent. The description
  under it is correct.

**Replacement:**

```
فون سائلنٹ ہو تو الارم خاموش رہے
```

### 3.9 `wear_madhab_note` (wear file): "Hanafi is later" became "Hanafi is late"

**Urdu now:** `صرف عصر بدلتی ہے۔ حنفی دیر سے ہے۔`

- **What it says:** "Only Asr changes. Hanafi is late / delayed." `حنفی دیر سے ہے` loses the comparison
  and does not say what is later. On its own it can read as a comment on Hanafis.
- **Should say:** "Only Asr changes. Hanafi Asr is later." This stays short enough for the watch.

**Replacement** (in `wear/src/main/res/values-ur/strings.xml`):

```
صرف عصر بدلتی ہے۔ حنفی عصر بعد میں ہے۔
```

---

## 4. NITS (optional)

- **The "quiet" corrections read right.** `settings_alerts_help`, `polar_notice_alarm` and
  `settings_alarm_on_approximate_desc` now say the notification comes `ہلکی آواز کے ساتھ` / `ہلکی آواز
  والی`, so nobody will think it makes no sound. The only "silent" (`خاموش`) wordings left are the
  ones that should be silent: the next-prayer badge (`settings_ongoing_badge_desc`,
  `channel_ongoing_desc`) and the alarm under silent mode and Do Not Disturb. One small point:
  `ہلکی آواز` suggests a *softer than usual* sound, but the app plays the phone's normal notification
  sound. If you want it exact, `settings_alerts_help` could read
  `یہ ہر نماز کے لیے الگ منتخب کریں۔ اطلاع فون کی عام اطلاع والی آواز کے ساتھ آتی ہے اور وائبریٹ کرتی ہے۔ الارم آپ کی چنی ہوئی آواز اونچی آواز میں بجاتا ہے، بالکل جگانے والے الارم کی طرح۔`
- **The Sunni / Shia question has two names.** `sect_title` and `label_school` use `مسلک`, while the
  Settings row (`settings_school`) and the watch (`wear_school`) use `مکتبِ فکر`. A user who answers
  the `مسلک` question will not find a `مسلک` row later. Pick one word. The glossary says `مکتبِ فکر`,
  but see section 5.
- `consent_policy_english`: `مکمل رازداری کی پالیسی انگریزی میں ہے۔` sits right under the button
  `پرائیویسی پالیسی پڑھیں`. Use the same word: `مکمل پرائیویسی پالیسی انگریزی میں ہے۔`
- `welcome_tagline`: `ذاتی` means "personal". The English "private" is about privacy. `نجی` is closer:
  `نماز میں آپ کا نجی ساتھی۔ ہمیشہ مفت۔`
- `polar_notice_body`: `فجر یا عشاء کا اصل وقت حساب نہیں کیا جا سکتا` is slightly ungrammatical. More
  natural: `فجر یا عشاء کے اصل وقت کا حساب نہیں لگایا جا سکتا`.
- Disclaimer paragraph 3: `کسی ضمانت اور درستگی کے کسی وعدے کے بغیر` is correct but clumsy. Smoother:
  `بغیر کسی ضمانت کے، اور درستگی کے کسی وعدے کے بغیر`. The meaning is unchanged, so this is optional.
- `channel_ongoing`: `اگلی نماز کا بیج`. `بیج` (badge) is spelt the same as `بیج` (seed), and this
  label appears in Android's own notification settings with no context around it. Option:
  `اگلی نماز کی مستقل اطلاع`.
- `wear_in_h_m` / `wear_in_m` drop the word "in". That is fine for the space on the tile. It is
  listed here only so nobody adds `میں` later without measuring it.
- `method_karachi`: the Urdu name of the University of Karachi is `جامعہ کراچی`, not
  `کراچی یونیورسٹی`. See also section 5.

---

## 5. What I could not judge (needs a native reader or a measurement)

- **Lengths on the watch and in the far-north notice.** `wear_disclaimer` has a hard three-line limit,
  and the tile countdown (`wear_in_h_m`, for example `2 گھنٹے 10 منٹ`) has room for about eight
  characters. The Urdu countdown is longer than that, and Urdu script sits taller than Latin. Character
  counts do not tell us whether it fits. An assistant should check this with `tools/wear-verify.sh` on
  the emulator in Urdu. **This is not a job for you, and not a reason to pick up the watch.** In the
  same way, the two-line limit on `method_notice_body` should be checked on the phone emulator after
  fix 3.5.
- **`مسلک` or `مکتبِ فکر` for Sunni / Shia.** In Pakistan `مسلک` is also used for Deobandi, Barelvi and
  Ahl-e-Hadith. You will know better than a model which word ordinary readers expect on a two-choice
  Sunni / Shia screen.
- **Naturalness, not meaning:** `home_until` (`شروع ہونے میں باقی`, shown under the countdown),
  `مددگار` as the noun for "helper" in `about_disclaimer_short` and `wear_disclaimer`, `تخمینی` versus
  `اندازاً` for "approximate", and the phrase `اسے آسمان بھی کم صاف نظر آتا ہے` (the watch "sees the
  sky less clearly") in the disclaimer. All of these are accurate; whether they sound natural is your
  call.
- **Noticed in the English, not a translation fault:**
  1. `mosque_diff_asr` says "school of thought" for the madhab, while the Settings row of that name is
     the Sunni / Shia choice. Fix 3.3 sidesteps this in Urdu.
  2. The Karachi method in the calculation library comes from the *University of Islamic Sciences,
     Karachi* (`جامعۃ العلوم الاسلامیہ`), not the *University of Karachi*. Pakistani users may notice.
     If this is to be changed, it should be changed in the English first.
