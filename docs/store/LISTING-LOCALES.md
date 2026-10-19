# Localised Play Store listings

The English listing is in [`LISTING.md`](LISTING.md). This file holds the eight additional
languages, so the app is findable by Muslims searching in their own language rather than in
English.

**No code change is involved.** A localised store listing is purely a Play Console entry:
Play Console → *Grow* → *Store presence* → *Store listings* → **Add translations**. Each
language gets its own title, short description and full description. Screenshots and the
feature graphic are reused automatically unless you upload language-specific ones.

> **The app is in English, Urdu, Turkish and Indonesian since 1.3.1.** The five other listings below
> (Malay, Bengali, Arabic, French, Hindi) are store text only, for discoverability: a user in Dhaka
> will find SajdaTime in Bengali and then open an app in English, or in one of its other three languages.
> That is a normal and accepted trade-off, since the numbers, the prayer names and the compass are
> understood regardless.

> **Corrected 9 Oct 2026 (drafts only, none of these is in Play).** The privacy bullets and short descriptions used to say "no analytics, no tracking, no crash reporting". That stopped being true when optional usage counts shipped in 1.3.0, so each language now follows the English listing: optional usage counts the user controls in Settings, no crash reporting, coordinates never part of the counts. The calculation method line used to say it was chosen for "your region"; it is chosen from the Sunni or Shia setting (Muslim World League or Jafari), so it now says that. The "app stays in English" note is also corrected: 1.3.1 ships Urdu, Turkish and Indonesian in the app.

> **Rewritten 16 Oct 2026 (security and visibility pass; drafts only, none of these is in Play).** Each language now has a title,
> a short description and a full description chosen against what people in that market search for (evidence and the search phrases
> per language are in `docs/HANDOVER.md` 5.22), with all of 1.4.0's features, the full disclaimer (all four points the English one makes,
> `docs/HANDOVER.md` §5.15), the dua request, and the privacy claims corrected to the true one: the times are worked out on the phone,
> the developer never receives the location, and the town name is looked up through Android, which on most phones asks Google
> (`docs/HANDOVER.md` 5.21). Price and promotion words ("no ads", "free" and their translations) are out of every title and short
> description because the Console flags them on the English listing and checks each language separately. They stay in the full
> descriptions, where they are content. **The January swap line in each section is the Ramadan bullet for early January:** if you publish
> before then, use the release text as it stands. French drops the high latitude bullet for room. All lengths are measured by script,
> newlines counted, and checked again at paste time.
>
> **Translation status.** Written by Claude with no native speaker review, by the owner's decision of
> 9 Oct 2026. Correct any mistake a user reports. Do not tell users a translation awaits review.

---

## 1.5.0 listings, FINAL 19 Oct 2026: paste on release day

Rebuilt on 19 Oct 2026 from the 1.5.0 English listing (`LISTING.md`, "1.5.0 listing, FINAL"), at the owner's request to
name every feature and use the words each market searches for. Translated by Claude (the owner's rule of 9 Oct 2026, no
native speaker review), each language then **reviewed independently** by a separate agent that back translated it first and
compared it with the English sentence by sentence: no meaning error, no softened disclaimer and no policy breach was found;
their wording fixes were applied (for example the Indonesian and Malay "do not rely on it alone", the Bengali "loud enough
for Fajr", the Arabic "اتجاه القبلة" search phrase, French grammar). The back translations, the translators' notes and the
review outcome are in `docs/translation/store-listing-1.5.0.md` (the back translations predate those small fixes).

Rules every block keeps: no price or promotion word in a title or short description (the Console flags them; "free" and "no
ads" stay in the full descriptions as facts); "Wear OS" in Latin in every full description; all four disclaimer points
unsoftened; the dua in the first person; the location claim as CLAUDE.md requires. Every count is Python `len` of the block,
newlines counted, which matches Play's counter for these scripts. Malay, Bengali, French and Hindi say plainly that the app
itself is in English, Arabic, Urdu, Turkish and Indonesian. Indonesian, Malay and French leave out the far north bullet for
length (4000 is Play's hard limit). **The 1.4.0 era drafts further down are superseded by this section.**

How to add them: Play Console, Grow users, Store presence, Store listings, Manage translations, Add your own translations;
pick the language, paste title, short and full description, save. The screenshots and feature graphic are shared.

### Arabic (ar), 1.5.0

Search phrases this listing targets: مواقيت الصلاة · اوقات الصلاة · اتجاه القبلة · تطبيق مواقيت الصلاة بدون نت

**Title** `30 / 30`
```
SajdaTime: مواقيت الصلاة، قبلة
```

**Short description** `64 / 80`
```
أوقات الصلاة والأذان والقبلة بدون نت، للسنة والشيعة، مع Wear OS.
```

**Full description** `3459 / 4000`
```
يمنحك SajdaTime مواقيت الصلاة وتنبيهات الأذان وبوصلة تحدد اتجاه القبلة، للمسلمين من السنة والشيعة، على هاتف أندرويد وساعة Wear OS. بلا إعلانات، وبلا حساب، وتعمل مواقيت الصلاة بدون إنترنت.

تُحسب مواقيت الصلوات الخمس على هاتفك نفسه، لذا يظل SajdaTime يعمل في الطائرة، أو في القبو، أو حيث لا توجد تغطية إطلاقًا.

ما الذي تحصل عليه

• الفجر والشروق والظهر والعصر والمغرب والعشاء أينما كنت، مع عدّ تنازلي مباشر حتى الصلاة التالية
• التاريخ الهجري (التقويم الإسلامي) بجانب تاريخ اليوم
• بوصلة القبلة مصحّحة إلى الشمال الحقيقي، وتهتز مرة واحدة حين تتجه نحو الكعبة
• تنبيهات لكل صلاة حتى والتطبيق مغلق: إيقاف، أو إشعار، أو منبّه عالٍ بما يكفي لصلاة الفجر
• أذان مدمج للسنة وللشيعة، أو أي صوت على هاتفك
• تذكير اختياري قبل الصلوات التي تختارها، من 5 إلى 30 دقيقة
• تطبيقات مصغّرة للشاشة الرئيسية: عدّ تنازلي كبير حتى الصلاة التالية، ومواقيت صلاة اليوم
• ميزات رمضان مدمجة: وقتا السحور والإفطار، وعدّ تنازلي، ومدة صيام كل يوم، وإمساكية رمضان، وتذكير اختياري بالسحور
• جداول مواقيت قابلة للطباعة بصيغة PDF: لليوم، أو 7 أيام، أو الشهر، أو رمضان
• تطبيق Wear OS وبطاقة للساعة يعملان وحدهما، مع هاتفك أو بدونه
• الإنجليزية والعربية والأردية والتركية والإندونيسية، تختار اللغة في التطبيق أو يتبع لغة هاتفك
• مظهر فاتح وآخر داكن. ويعمل مع الخط الكبير وقارئات الشاشة

مصمَّم وفق مذهبك

• السنة: الحنفي والشافعي والمالكي والحنبلي، مع قاعدة العصر الصحيحة لكل منها
• الشيعة: الجعفري / الاثنا عشري، بما في ذلك قاعدة المغرب الصحيحة
• 13 طريقة حساب: رابطة العالم الإسلامي، ISNA، أم القرى، الهيئة المصرية، كراتشي، دبي، الكويت، قطر، Kemenag/MUIS (إندونيسيا وسنغافورة)، ديانت (تركيا)، لجنة رؤية الهلال، الجعفري وطهران
• تُختار الطريقة بحسب مذهبك (رابطة العالم الإسلامي للسنّة، والجعفري للشيعة)، ويمكنك تغييرها
• «طابِق مسجدك»: اضبط أي صلاة على لوحة مسجدك في حدود 30 دقيقة، أو قدّم التاريخ الهجري أو أخّره يومًا أو يومين
• فجر وعشاء معقولان في أقصى الشمال صيفًا

خصوصيتك ليست هي الثمن

معظم تطبيقات الصلاة المجانية تموّلها الإعلانات، أي أنك تدفع الثمن من بياناتك. أما هذا التطبيق فلا تدفع ثمنه بأي شيء.

• لا إعلانات، ولا شيء تشتريه
• لا حسابات، ولا تسجيل دخول، ولا بريد إلكتروني
• إحصاءات استخدام اختيارية تتحكم بها من الإعدادات، وتبقى متوقفة ما لم توافق. ولا تقارير أعطال
• موقع تقريبي فقط، يُقرأ والتطبيق مفتوح. ولا تصل إحداثياتك أبدًا إلى المطوّر ولا إلى إحصاءات الاستخدام
• يُبحث عن اسم بلدتك عبر خدمة البحث عن الأماكن في هاتفك نفسه، وهي Google في الغالب. وتشرح سياسة الخصوصية ما يعنيه ذلك بالضبط
• النسخ الاحتياطي السحابي متوقف، فلا يُنسخ أي شيء إلى خادم للنسخ الاحتياطي

تفضّل ألا تسمح بالموقع؟ اكتب اسم المكان الذي أنت فيه بدلًا من ذلك. يُبحث عنه مرة واحدة عبر الإنترنت، ثم يعمل التطبيق بالطريقة نفسها.

بصراحة: ما هو هذا التطبيق

تطبيق SajdaTime مساعد، وليس مرجعًا دينيًا. يحسب هاتفك هذه الأوقات من موقع الشمس باستخدام طرق حساب منشورة، ولا يقدّمها أي مسجد أو عالم أو جهة. التطبيق من عمل شخص واحد، بمساعدة الذكاء الاصطناعي، وقد يقع فيه الخطأ. وحيث يختلف SajdaTime ومسجدك، فاتبع مسجدك. وإن بدا لك يومًا وقت أو اتجاه غير صحيح، أو لم تكن متأكدًا، فالرجاء أن تسألهم أو تسأل شخصًا آخر مؤهلًا لينصحك.

تُحسب مواقيت الصلاة بمكتبة adhan-java من Batoul Apps، وهي مكتبة مفتوحة المصدر واسعة الاستخدام، وتُقارَن المواقيت بجداول مرجعية مستقلة قبل كل إصدار. ومع ذلك يُقدَّم التطبيق مجانًا وكما هو، دون أي ضمان ودون أي وعد بالدقة، فحيث تهمّك الدقة التامة، فالرجاء ألا تعتمد عليه وحده.

مجاني، وسيبقى مجانيًا

لا نسخة مدفوعة، ولا اشتراك، ولا شيء مقفل. يُقدَّم SajdaTime مجانًا صدقةً جاريةً للأمة.

لا أطلب في المقابل إلا شيئًا واحدًا: أرجو أن تذكرني وعائلتي ووالديّ في دعائك. جزاكم الله خيرًا.
```

### Urdu (ur), 1.5.0

Search phrases this listing targets: نماز کے اوقات · نماز کا وقت · قبلہ رخ · نماز کا وقت حنفی · اوقات نماز

**Title** `30 / 30`
```
SajdaTime: نماز کے اوقات، قبلہ
```

**Short description** `65 / 80`
```
سنی و شیعہ کے لیے آف لائن اوقات نماز، اذان، قبلہ اور Wear OS ایپ۔
```

**Full description** `3980 / 4000`
```
آپ کے اینڈرائیڈ فون اور Wear OS گھڑی پر SajdaTime سنی اور شیعہ مسلمانوں کو نماز کے اوقات، اذان کے الرٹ اور قبلہ نما دیتی ہے۔ نہ اشتہارات، نہ اکاؤنٹ، اور نماز کے اوقات آف لائن کام کرتے ہیں۔

آپ نماز کہیں یا صلوٰۃ، پانچوں نمازوں کے اوقات آپ کے اپنے فون پر ہی نکالے جاتے ہیں، اس لیے SajdaTime ہوائی جہاز میں، تہہ خانے میں، یا سگنل بالکل نہ ہونے پر بھی کام کرتی رہتی ہے۔

آپ کو کیا ملتا ہے

• فجر، طلوع آفتاب، ظہر، عصر، مغرب اور عشاء، آپ جہاں بھی ہوں، اور اگلی نماز تک لمحہ بہ لمحہ الٹی گنتی
• آج کی تاریخ کے ساتھ ہجری تاریخ (اسلامی کیلنڈر)
• اصل شمال کے مطابق درست قبلہ نما، جو قبلہ رخ ہوتے ہی ایک بار وائبریٹ کرتا ہے
• ہر نماز کا وقت ہونے پر الرٹ، چاہے ایپ بند ہو: کچھ نہیں، اطلاع، یا فجر کے لیے کافی تیز الارم
• سنی اور شیعہ صارفین کے لیے ایپ میں شامل اذان، یا آپ کے فون کی کوئی بھی آواز
• آپ کی چنی ہوئی نمازوں سے 5 سے 30 منٹ پہلے اختیاری یاددہانی
• ہوم اسکرین ویجٹ: اگلی نماز تک بڑی الٹی گنتی، اور آج کے نماز کے اوقات
• رمضان ایپ میں ہی شامل: سحری اور افطار کے اوقات، الٹی گنتی، ہر روزے کا دورانیہ، رمضان کا نقشہ (رمضان کیلنڈر) اور سحری کی اختیاری یاددہانی
• آج، 7 دن، مہینے یا رمضان کے لیے پرنٹ کے قابل PDF نظام الاوقات
• گھڑی کے لیے Wear OS ایپ اور ٹائل جو خود چلتی ہیں، فون کے ساتھ یا اس کے بغیر
• انگریزی، عربی، اردو، ترکی اور انڈونیشی، ایپ میں منتخب کریں یا فون کی زبان کے مطابق
• روشن اور گہرا تھیم۔ بڑے حروف اور اسکرین ریڈر کے ساتھ کام کرتی ہے

آپ کے مسلک کے مطابق

• سنی: حنفی، شافعی، مالکی اور حنبلی، ہر ایک کے لیے عصر کا درست اصول
• شیعہ: جعفری / اثنا عشری، مغرب کے درست اصول سمیت
• حساب کے 13 طریقے: مسلم ورلڈ لیگ، ISNA، ام القریٰ، مصری، کراچی، دبئی، کویت، قطر، Kemenag/MUIS (انڈونیشیا، سنگاپور)، دیانت (ترکی)، مون سائٹنگ کمیٹی، جعفری اور تہران
• طریقہ آپ کے مسلک کے مطابق چنا جاتا ہے (سنی کے لیے مسلم ورلڈ لیگ، شیعہ کے لیے جعفری)، اور آپ اسے بدل سکتے ہیں
• «اپنی مسجد سے ملائیں»: کسی بھی نماز کو 30 منٹ کے اندر اپنی مسجد کے بورڈ کے مطابق کر لیں، یا اسلامی تاریخ ایک دو دن آگے پیچھے کریں
• دور شمال میں گرمیوں کے دوران فجر اور عشاء کے معقول اوقات

آپ کی پرائیویسی اس کی قیمت نہیں

زیادہ تر مفت نماز ایپس اشتہارات سے چلتی ہیں، یعنی آپ قیمت اپنے ڈیٹا سے چکاتے ہیں۔ یہاں آپ سے کسی بھی صورت میں کوئی قیمت نہیں لی جاتی۔

• نہ کوئی اشتہار، نہ خریدنے کو کچھ
• نہ اکاؤنٹ، نہ سائن اِن، نہ ای میل پتہ
• استعمال کے اختیاری اعداد و شمار، جن کا اختیار ترتیبات میں آپ کے پاس ہے، اور جب تک آپ ہاں نہ کہیں بند رہتے ہیں۔ کوئی کریش رپورٹنگ نہیں
• صرف اندازاً مقام، جو ایپ کھلی ہونے پر ہی دیکھا جاتا ہے۔ آپ کے کوآرڈینیٹس نہ کبھی ڈویلپر کو جاتے ہیں نہ استعمال کے اعداد و شمار میں
• آپ کے شہر کا نام آپ کے فون کی اپنی مقام تلاش سروس سے معلوم کیا جاتا ہے، جو عموماً گوگل ہوتی ہے۔ پرائیویسی پالیسی ٹھیک ٹھیک بتاتی ہے کہ اس کا کیا مطلب ہے
• کلاؤڈ بیک اپ بند ہے، اس لیے کچھ بھی کسی بیک اپ سرور پر نقل نہیں ہوتا

مقام کی اجازت نہیں دینا چاہتے؟ اس کے بجائے لکھ دیں کہ آپ کہاں ہیں۔ یہ ایک بار آن لائن تلاش کیا جاتا ہے، پھر ایپ بالکل ویسے ہی کام کرتی ہے۔

یہ ایپ کیا ہے، سچ سچ

یاد رہے کہ SajdaTime ایک مددگار ہے، کوئی دینی اتھارٹی نہیں۔ آپ کا فون یہ اوقات سورج کی پوزیشن سے، شائع شدہ حسابی طریقوں کے ذریعے نکالتا ہے، اور یہ کسی مسجد، عالم یا اتھارٹی کے فراہم کردہ نہیں۔ یہ ایپ ایک شخص نے مصنوعی ذہانت کی مدد سے بنائی ہے، اور اس میں غلطی ہو سکتی ہے۔ جہاں SajdaTime اور آپ کی مسجد میں اختلاف ہو، وہاں اپنی مسجد کی پیروی کریں۔ اگر کبھی کوئی وقت یا سمت درست نہ لگے، یا آپ کو یقین نہ ہو، تو براہِ کرم اپنی مسجد سے یا کسی اور اہل شخص سے پوچھ لیں جو آپ کی رہنمائی کر سکے۔

نماز کے اوقات Batoul Apps کی adhan-java سے نکالے جاتے ہیں، جو وسیع پیمانے پر استعمال ہونے والی اوپن سورس لائبریری ہے، اور ہر نئے ورژن سے پہلے انہیں آزاد حوالہ جاتی نقشوں سے جانچا جاتا ہے۔ پھر بھی یہ ایپ مفت اور جیسی ہے ویسی ہی دی جاتی ہے، کسی ضمانت اور درستگی کے کسی وعدے کے بغیر، اس لیے جہاں بالکل درست ہونا آپ کے لیے اہم ہو، وہاں براہِ کرم صرف اسی پر بھروسہ نہ کریں۔

مفت، اور مفت ہی رہے گی

نہ کوئی پیڈ ورژن، نہ سبسکرپشن، نہ کوئی چیز تالے میں بند۔ SajdaTime امت کے لیے صدقۂ جاریہ کے طور پر مفت دی گئی ہے۔

بدلے میں میری بس ایک گزارش ہے: براہِ کرم مجھے، میرے گھر والوں اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔ جزاک اللہ خیر۔
```

### Turkish (tr-TR), 1.5.0

Search phrases this listing targets: namaz vakitleri · ezan vakti · kıble · namaz uygulaması internetsiz · imsakiye

**Title** `26 / 30`
```
SajdaTime: Namaz Vakitleri
```

**Short description** `75 / 80`
```
İnternetsiz namaz vakitleri, ezan ve kıble. Sünni ve Şii için, Wear OS ile.
```

**Full description** `3975 / 4000`
```
SajdaTime, Sünni ve Şii Müslümanlara Android telefonunuzda ve Wear OS saatinizde namaz vakitleri, ezan vakti uyarıları ve kıble pusulası sunar. Reklam yok, hesap yok, namaz vakitleri internetsiz çalışır.

İster namaz deyin ister salat, beş vaktin saatleri kendi telefonunuzda hesaplanır. Bu yüzden SajdaTime uçakta, bodrumda ya da hiç sinyal yokken de çalışır.

NELER SUNAR

• Bulunduğunuz yere göre Sabah, Güneş, Öğle, İkindi, Akşam ve Yatsı, sıradaki namaza canlı geri sayımla
• Bugünün tarihinin yanında Hicri tarih (İslami takvim)
• Gerçek kuzeye göre düzeltilmiş, Kâbe'ye döndüğünüzde bir kez titreyen kıble pusulası
• Her namaz için uyarı, uygulama kapalıyken bile: yok, bildirim ya da Sabah namazına kaldıracak kadar yüksek bir alarm
• Sünni ve Şii için yerleşik ezan ya da telefonunuzdaki herhangi bir ses
• Seçtiğiniz namazlardan 5 ila 30 dakika önce isteğe bağlı hatırlatma
• Ana ekran widget'ları: sıradaki namaza büyük geri sayım ve bugünün namaz vakitleri
• Ramazan da hazır: imsak ve iftar vakitleri, geri sayım, her orucun süresi, Ramazan imsakiyesi ve isteğe bağlı sahur hatırlatıcısı
• Bugün, 7 gün, tüm ay ya da Ramazan için yazdırılabilir PDF vakit çizelgeleri
• Telefonla ya da telefonsuz, kendi başına çalışan Wear OS uygulaması ve saat kartı
• Türkçe, İngilizce, Arapça, Urduca ve Endonezce. Dil uygulamadan seçilir ya da telefonun dilini izler
• Açık ve koyu tema. Büyük yazı ve ekran okuyucularla uyumlu

MEZHEBİNİZE GÖRE HAZIRLANDI

• Sünni: Hanefi, Şafii, Maliki ve Hanbeli, her biri için doğru İkindi kuralıyla
• Şii: Caferi (İsna Aşeriyye), doğru Akşam kuralı dahil
• 13 hesaplama yöntemi: Dünya İslam Birliği, ISNA, Ümmülkura, Mısır, Karaçi, Dubai, Kuveyt, Katar, Kemenag ve MUIS (Endonezya, Singapur), Diyanet (Türkiye), Moonsighting Committee, Caferi ve Tahran
• Yöntem mezhebinize göre seçilir (Sünni için Dünya İslam Birliği, Şii için Caferi) ve değiştirilebilir
• Camiye göre ayarla: herhangi bir vakti 30 dakikaya kadar cami panosundaki saate eşitleyin ya da Hicri tarihi bir iki gün kaydırın
• Yazın uzak kuzeyde makul Sabah ve Yatsı vakitleri

BEDELİ GİZLİLİĞİNİZ DEĞİL

Ücretsiz namaz uygulamalarının çoğu reklamla finanse edilir, yani bedelini verinizle ödersiniz. Bunun ise hiçbir bedeli yok.

• Reklam yok, satın alma yok
• Hesap yok, giriş yok, e-posta adresi yok
• Ayarlar'dan yönettiğiniz isteğe bağlı kullanım sayımları, siz evet demedikçe kapalı. Çökme raporu yok
• Yalnızca yaklaşık konum, uygulama açıkken okunur. Koordinatlarınız ne geliştiriciye ne de kullanım sayımlarına gider
• Şehrinizin adını telefonunuzun kendi yer arama hizmeti (genellikle Google) bulur. Bunun tam olarak ne demek olduğunu gizlilik politikası açıklar
• Bulut yedekleme kapalı, hiçbir şey bir yedekleme sunucusuna kopyalanmaz

Konum izni vermek istemiyor musunuz? Nerede olduğunuzu yazın. Bir kez internetten aranır, sonra uygulama aynen çalışır.

NE OLDUĞU KONUSUNDA DÜRÜST

SajdaTime bir yardımcıdır, dinî bir otorite değildir. Telefonunuz bu vakitleri güneşin konumundan, yayımlanmış hesaplama yöntemleriyle hesaplar. Bu vakitleri hiçbir cami, âlim ya da otorite vermez. Uygulama tek bir kişi tarafından yapay zekâ yardımıyla yazıldı ve hata yapabilir. SajdaTime ile caminiz farklı söylüyorsa caminize uyun. Bir vakit ya da yön size yanlış görünürse veya emin değilseniz, lütfen onlara ya da size yol gösterebilecek ehil başka birine danışın.

Vakitler, Batoul Apps'in yaygın olarak kullanılan açık kaynaklı kütüphanesi adhan-java ile hesaplanır ve her sürümden önce bağımsız referans çizelgelerle karşılaştırılır. Yine de uygulama ücretsiz ve olduğu gibi, hiçbir garanti ve doğruluk vaadi olmadan sunulur. Bu yüzden kesinlik sizin için önemliyse lütfen yalnızca uygulamaya güvenmeyin.

ÜCRETSİZ VE ÜCRETSİZ KALACAK

Ücretli sürüm yok, abonelik yok, kilitli hiçbir şey yok. SajdaTime, ümmet için sadaka-i câriye olarak karşılıksız sunulur.

Karşılığında tek bir şey istiyorum: lütfen beni, ailemi ve anne babamı dualarınızda anın. Allah razı olsun.
```

### Indonesian (id-ID), 1.5.0

Search phrases this listing targets: jadwal sholat · waktu sholat · arah kiblat · adzan · jadwal imsakiyah

**Title** `24 / 30`
```
SajdaTime: Jadwal Sholat
```

**Short description** `76 / 80`
```
Jadwal sholat offline, adzan dan arah kiblat. Sunni dan Syiah, juga Wear OS.
```

**Full description** `3996 / 4000`
```
SajdaTime menyajikan jadwal sholat, pengingat adzan, dan kompas arah kiblat bagi Muslim Sunni dan Syiah di ponsel Android dan jam tangan Wear OS. Tanpa iklan, tanpa akun, dan jadwal sholat bisa offline.

Sholat, salat, atau namaz: kelima waktunya dihitung di ponsel Anda sendiri, jadi SajdaTime tetap jalan di pesawat, ruang bawah tanah, atau tanpa sinyal sama sekali.

YANG ANDA DAPAT

• Subuh, Terbit, Zuhur, Asar, Magrib, dan Isya di lokasi Anda, dengan hitung mundur langsung ke sholat berikutnya
• Tanggal Hijriah (kalender Islam) di samping tanggal hari ini
• Kompas kiblat berpatokan utara sejati, bergetar sekali saat menghadap Ka'bah
• Pengingat tiap waktu sholat, bahkan saat aplikasi ditutup: mati, notifikasi, atau alarm yang cukup keras untuk Subuh
• Adzan (azan) bawaan untuk Sunni dan Syiah, atau suara apa pun di ponsel Anda
• Pengingat opsional 5 sampai 30 menit sebelum sholat yang Anda pilih
• Widget layar utama: hitung mundur besar ke sholat berikutnya dan jadwal hari ini
• Fitur Ramadan: akhir sahur dan buka puasa, hitung mundur, lama tiap puasa, jadwal imsakiyah, dan pengingat sahur opsional
• Jadwal sholat PDF siap cetak: hari ini, 7 hari, sebulan, atau Ramadan
• Aplikasi dan tile Wear OS yang berjalan sendiri, dengan atau tanpa ponsel
• Bahasa Indonesia, Inggris, Arab, Urdu, dan Turki, dipilih di aplikasi atau ikut bahasa ponsel
• Tema terang dan gelap. Mendukung teks besar dan pembaca layar

SESUAI MAZHAB ANDA

• Sunni: Hanafi, Syafi'i, Maliki, dan Hanbali, dengan aturan Asar yang tepat untuk tiap mazhab
• Syiah: Jafari (Itsna Asyariyah), termasuk aturan Magrib yang benar
• 13 metode perhitungan: Liga Muslim Dunia, ISNA, Umm al-Qura, Mesir, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapura), Diyanet (Turki), Moonsighting Committee, Jafari, dan Teheran
• Metode dipilih sesuai aliran Anda (Liga Muslim Dunia untuk Sunni, Jafari untuk Syiah) dan bisa diubah
• Sesuaikan dengan masjid: samakan tiap waktu dengan papan masjid, hingga 30 menit, atau geser tanggal Hijriah satu dua hari

PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi sholat gratis dibiayai iklan, jadi Anda membayar dengan data. Yang ini tidak dibayar sama sekali.

• Tanpa iklan, tanpa pembelian
• Tanpa akun, tanpa login, tanpa alamat email
• Data penggunaan opsional yang Anda atur di Pengaturan, mati kecuali Anda setuju. Tanpa laporan crash
• Hanya perkiraan lokasi, dibaca saat aplikasi terbuka. Koordinat Anda tidak pernah dikirim ke pengembang atau ke data penggunaan
• Nama kota Anda dicari oleh layanan pencarian tempat bawaan ponsel, biasanya Google. Kebijakan privasi menjelaskan persis artinya
• Pencadangan cloud mati, jadi tidak ada yang disalin ke server cadangan

Tidak mau izinkan lokasi? Ketik saja tempat Anda. Tempat itu dicari sekali secara online, lalu aplikasi bekerja sama saja.

JUJUR TENTANG APA ADANYA

SajdaTime adalah alat bantu, bukan otoritas agama. Ponsel Anda menghitung waktu ini dari posisi matahari dengan metode perhitungan yang diterbitkan. Waktu ini tidak diberikan oleh masjid, ulama, atau otoritas mana pun. Aplikasi ini ditulis oleh satu orang dengan bantuan kecerdasan buatan, dan bisa keliru. Jika SajdaTime dan masjid Anda berbeda, ikutilah masjid Anda. Jika suatu waktu atau arah tampak tidak tepat, atau Anda ragu, mohon tanyakan kepada mereka atau orang lain yang berwenang memberi nasihat.

Waktu sholat dihitung dengan adhan-java dari Batoul Apps, pustaka sumber terbuka yang banyak dipakai, dan diperiksa terhadap jadwal rujukan independen sebelum setiap rilis. Meski begitu, aplikasi ini diberikan gratis dan apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi bila ketepatan penting bagi Anda, mohon jangan hanya mengandalkan aplikasi ini.

GRATIS, DAN TETAP GRATIS

Tidak ada versi berbayar, langganan, atau fitur yang dikunci. SajdaTime diberikan tanpa biaya sebagai sedekah jariah untuk umat.

Saya hanya minta satu hal sebagai balasan: mohon doakan saya, keluarga saya, dan orang tua saya. Jazakallahu khairan.
```

### Malay (ms-MY), 1.5.0

Search phrases this listing targets: waktu solat · arah kiblat · jadual waktu solat · azan

**Title** `30 / 30`
```
SajdaTime: Waktu Solat, Kiblat
```

**Short description** `73 / 80`
```
Waktu solat, azan dan arah kiblat untuk Sunni dan Syiah, juga di Wear OS.
```

**Full description** `3998 / 4000`
```
SajdaTime memberi waktu solat, peringatan azan dan kompas arah kiblat untuk Muslim Sunni dan Syiah di telefon Android dan jam tangan Wear OS. Tiada iklan, tiada akaun, dan waktu solat berfungsi luar talian.

Solat, salat atau namaz: lima waktunya dikira di telefon anda sendiri, jadi SajdaTime tetap berfungsi dalam kapal terbang, di bawah tanah atau langsung tanpa isyarat.

APA YANG ADA

• Subuh, Syuruk, Zohor, Asar, Maghrib dan Isyak di lokasi anda, dengan kiraan detik langsung ke solat seterusnya
• Tarikh Hijrah (kalendar Islam) di sebelah tarikh hari ini
• Kompas kiblat berpandukan utara benar, bergetar sekali apabila anda menghadap Kaabah
• Peringatan setiap solat walaupun aplikasi ditutup: tiada, pemberitahuan atau penggera cukup kuat untuk Subuh
• Azan terbina dalam untuk Sunni dan Syiah, atau sebarang bunyi di telefon anda
• Peringatan pilihan 5 hingga 30 minit sebelum solat yang anda pilih
• Widget skrin utama: kiraan detik besar ke solat seterusnya, dan waktu hari ini
• Untuk Ramadan: waktu sahur dan berbuka, kiraan detik, tempoh setiap puasa, jadual Ramadan dan peringatan sahur pilihan
• Jadual waktu solat PDF untuk dicetak: hari ini, 7 hari, sebulan atau Ramadan
• Aplikasi dan tile Wear OS yang berfungsi sendiri, dengan atau tanpa telefon
• Bahasa aplikasi: Inggeris, Arab, Urdu, Turki dan Indonesia (tiada Melayu), pilih dalam aplikasi atau ikut telefon
• Tema cerah dan gelap. Menyokong teks besar dan pembaca skrin

MENGIKUT MAZHAB ANDA

• Sunni: Hanafi, Syafie, Maliki dan Hanbali, dengan kaedah Asar yang betul bagi tiap satu
• Syiah: Jafari / Ithna Asyari, termasuk kaedah Maghrib yang betul
• 13 kaedah pengiraan: Muslim World League, ISNA, Umm al-Qura, Mesir, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapura), Diyanet (Turki), Moonsighting Committee, Jafari dan Tehran
• Kaedah dipilih mengikut aliran anda (Muslim World League untuk Sunni, Jafari untuk Syiah) dan boleh ditukar
• Match your mosque: selaraskan sebarang solat dengan papan masjid, hingga 30 minit, atau anjak tarikh Hijrah sehari dua

PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi solat percuma dibiayai iklan, maknanya anda membayar dengan data. Yang ini langsung tidak dibayar.

• Tiada iklan, tiada pembelian
• Tiada akaun, tiada log masuk, tiada alamat e-mel
• Kiraan penggunaan pilihan yang anda kawal dalam Tetapan, mati kecuali anda setuju. Tiada laporan ranap
• Lokasi anggaran sahaja, dibaca semasa aplikasi dibuka. Koordinat anda tidak pernah dihantar kepada pembangun atau kiraan penggunaan
• Nama bandar anda dicari oleh perkhidmatan tempat telefon anda sendiri, biasanya Google. Dasar privasi menerangkannya dengan tepat
• Sandaran awan dimatikan, jadi tiada yang disalin ke pelayan sandaran

Tidak mahu benarkan lokasi? Taip nama tempat anda. Ia dicari sekali dalam talian, kemudian aplikasi berfungsi sama.

JUJUR TENTANG APA ADANYA

SajdaTime ialah pembantu, bukan pihak berkuasa agama. Waktu ini dikira oleh telefon anda dari kedudukan matahari dengan kaedah pengiraan yang diterbitkan, dan tidak dibekalkan oleh sebarang masjid, ulama atau pihak berkuasa. Aplikasi ini ditulis oleh seorang individu dengan bantuan kecerdasan buatan, dan boleh tersilap. Jika SajdaTime dan masjid anda berbeza, ikutlah masjid anda. Jika sesuatu waktu atau arah nampak tidak kena, atau anda tidak pasti, sila tanya mereka atau orang lain yang layak menasihati anda.

Waktu solat dikira dengan adhan-java oleh Batoul Apps, pustaka sumber terbuka yang meluas digunakan, dan disemak dengan jadual rujukan bebas sebelum setiap keluaran. Namun, aplikasi ini diberi percuma dan seadanya, tanpa waranti dan tanpa janji ketepatan, jadi jika ketepatan penting bagi anda, sila jangan hanya bergantung padanya.

PERCUMA, DAN KEKAL PERCUMA

Tiada versi berbayar, tiada langganan dan tiada yang dikunci. SajdaTime diberi percuma sebagai sedekah jariah untuk ummah.

Saya hanya minta satu perkara sebagai balasan: sudilah doakan saya, keluarga saya dan ibu bapa saya. Jazakallahu khairan.
```

### Bengali (bn-BD), 1.5.0

Search phrases this listing targets: নামাজের সময়সূচি · নামাজের সময় · কিবলা কম্পাস · আজান

**Title** `27 / 30`
```
SajdaTime: নামাজের সময়সূচি
```

**Short description** `64 / 80`
```
অফলাইন নামাজের সময়, আজান, কিবলা কম্পাস, সুন্নি ও শিয়া, Wear OS
```

**Full description** `3994 / 4000`
```
SajdaTime সুন্নি ও শিয়া মুসলমানদের জন্য নামাজের সময়সূচি, আজানের অ্যালার্ট ও কিবলা কম্পাস নিয়ে আসে, আপনার Android ফোন ও Wear OS ঘড়িতে। কোনো বিজ্ঞাপন নেই, অ্যাকাউন্ট লাগে না, আর নামাজের সময় অফলাইনেও পাওয়া যায়।

নামাজ, সালাহ বা সালাত, যে নামেই ডাকুন, পাঁচ ওয়াক্তের সময় আপনার নিজের ফোনেই হিসাব হয়। তাই বিমানে, বেসমেন্টে বা একেবারে নেটওয়ার্ক না থাকলেও SajdaTime চলতে থাকে।

আপনি যা পাবেন

• ফজর, সূর্যোদয়, যোহর, আসর, মাগরিব ও এশা, যেখানেই থাকুন, পরের নামাজ পর্যন্ত লাইভ কাউন্টডাউনসহ
• আজকের তারিখের পাশে হিজরি তারিখ (ইসলামি ক্যালেন্ডার)
• প্রকৃত উত্তর অনুযায়ী ঠিক করা কিবলা কম্পাস, কাবার দিকে মুখ করলে একবার ভাইব্রেট করে
• প্রতি ওয়াক্তের অ্যালার্ট, অ্যাপ বন্ধ থাকলেও: কিছুই না, নোটিফিকেশন, অথবা ফজরের জন্য যথেষ্ট জোরালো অ্যালার্ম
• সুন্নি ও শিয়াদের জন্য অ্যাপেই আজান (আযান), অথবা ফোনের যেকোনো শব্দ
• বেছে নেওয়া নামাজের ৫ থেকে ৩০ মিনিট আগে ঐচ্ছিক রিমাইন্ডার
• হোম স্ক্রিন উইজেট: পরের নামাজের বড় কাউন্টডাউন, আর আজকের নামাজের সময়
• রমজানও অ্যাপেই: সাহরি (সেহরি) ও ইফতারের সময়, কাউন্টডাউন, প্রতিটি রোজা কতক্ষণের, রমজানের সময়সূচি ও ঐচ্ছিক সাহরির রিমাইন্ডার
• আজ, ৭ দিন, পুরো মাস বা রমজানের নামাজের সময়সূচি, প্রিন্ট করার মতো PDF
• Wear OS অ্যাপ ও টাইল, ফোন সঙ্গে থাকুক বা না থাকুক, নিজেই চলে
• অ্যাপটি নিজে ইংরেজি, আরবি, উর্দু, তুর্কি ও ইন্দোনেশীয় ভাষায়, বাংলায় নয়। ভাষা অ্যাপে বেছে নিন, বা ফোনের সেটিং অনুযায়ী রাখুন
• লাইট ও ডার্ক থিম। বড় লেখা ও স্ক্রিন রিডারের সঙ্গে কাজ করে

আপনার মাযহাব অনুযায়ী তৈরি

• সুন্নি: হানাফি, শাফেয়ি, মালেকি ও হাম্বলি, প্রতিটির জন্য আসরের সঠিক নিয়ম
• শিয়া: জাফরি / ইসনা আশারি, মাগরিবের সঠিক নিয়মসহ
• ১৩টি গণনা পদ্ধতি: মুসলিম ওয়ার্ল্ড লীগ, ISNA, উম্মুল কুরা, মিশরীয়, করাচি, দুবাই, কুয়েত, কাতার, Kemenag/MUIS (ইন্দোনেশিয়া, সিঙ্গাপুর), দিয়ানেত (তুরস্ক), মুনসাইটিং কমিটি, জাফরি ও তেহরান
• পদ্ধতিটি আপনার মাযহাব অনুযায়ী ঠিক হয় (সুন্নির জন্য মুসলিম ওয়ার্ল্ড লীগ, শিয়ার জন্য জাফরি), চাইলে বদলাতে পারেন
• মসজিদের সঙ্গে মেলান: যেকোনো নামাজ মসজিদের বোর্ডের সময়ে মিলিয়ে নিন, ৩০ মিনিটের মধ্যে, অথবা ইসলামি তারিখ এক বা দুই দিন সরিয়ে নিন
• গ্রীষ্মকালে সুদূর উত্তরেও ফজর ও এশার যুক্তিসঙ্গত সময়

এর দাম আপনার গোপনীয়তা নয়

বেশিরভাগ বিনামূল্যের নামাজের অ্যাপ চলে বিজ্ঞাপনের টাকায়, অর্থাৎ দাম দেন আপনার তথ্য দিয়ে। এই অ্যাপের কোনো দামই নেই, কোনোভাবেই।

• কোনো বিজ্ঞাপন নেই, কেনার কিছু নেই
• কোনো অ্যাকাউন্ট, সাইন ইন বা ইমেইল ঠিকানা লাগে না
• ঐচ্ছিক ব্যবহারের গণনা, সেটিংসে আপনার নিয়ন্ত্রণে, রাজি না হলে বন্ধ থাকে। কোনো ক্র্যাশ রিপোর্টিং নেই
• শুধু আনুমানিক অবস্থান, শুধু অ্যাপ খোলা থাকার সময় পড়া হয়। আপনার স্থানাঙ্ক কখনও ডেভেলপারের কাছে বা ব্যবহারের গণনায় যায় না
• শহরের নাম খুঁজে দেয় ফোনের নিজস্ব স্থান সেবা, সাধারণত Google। এর ঠিক কী মানে, তা গোপনীয়তা নীতিতে ব্যাখ্যা আছে
• ক্লাউড ব্যাকআপ বন্ধ, তাই কোনো ব্যাকআপ সার্ভারে কিছুই কপি হয় না

অবস্থানের অনুমতি দিতে চান না? তাহলে জায়গার নাম লিখে দিন। সেটি একবার অনলাইনে খোঁজা হয়, তারপর অ্যাপ একইভাবে চলে।

এটি আসলে কী, সৎভাবে বলি

SajdaTime একটি সহায়ক, কোনো ধর্মীয় কর্তৃপক্ষ নয়। আপনার ফোন প্রকাশিত গণনা পদ্ধতি অনুযায়ী সূর্যের অবস্থান থেকে এই সময়গুলো হিসাব করে। এগুলো কোনো মসজিদ, আলেম বা কর্তৃপক্ষের দেওয়া নয়। অ্যাপটি একজন ব্যক্তি লিখেছেন, কৃত্রিম বুদ্ধিমত্তার সাহায্য নিয়ে, এবং এতে ভুল হতে পারে। SajdaTime আর আপনার মসজিদের মধ্যে অমিল হলে আপনার মসজিদকেই অনুসরণ করুন। কোনো সময় বা দিক কখনও ঠিক মনে না হলে, বা আপনি নিশ্চিত না হলে, অনুগ্রহ করে মসজিদে অথবা পরামর্শ দেওয়ার যোগ্য অন্য কাউকে জিজ্ঞেস করুন।

নামাজের সময় হিসাব করা হয় Batoul Apps নির্মিত adhan-java দিয়ে, যা বহুল ব্যবহৃত একটি ওপেন সোর্স লাইব্রেরি, এবং প্রতিটি রিলিজের আগে স্বতন্ত্র রেফারেন্স সময়সূচির সঙ্গে মিলিয়ে দেখা হয়। তবুও অ্যাপটি বিনামূল্যে এবং যেমন আছে তেমনভাবে দেওয়া হয়, কোনো ওয়ারেন্টি বা নির্ভুলতার প্রতিশ্রুতি ছাড়া, তাই আপনার কাছে যেখানে নির্ভুল হওয়া গুরুত্বপূর্ণ, সেখানে অনুগ্রহ করে শুধু এর ওপর নির্ভর করবেন না।

বিনামূল্যে, এবং বিনামূল্যেই থাকবে

কোনো পেইড সংস্করণ নেই, সাবস্ক্রিপশন নেই, কিছুই তালাবদ্ধ নেই। SajdaTime উম্মাহর জন্য সদকায়ে জারিয়া হিসেবে বিনামূল্যে দেওয়া হয়েছে।

বিনিময়ে আমার একটাই অনুরোধ: অনুগ্রহ করে আপনার দোয়ায় আমাকে, আমার পরিবারকে ও আমার মা বাবাকে রাখবেন। জাযাকাল্লাহু খাইরান।
```

### French (fr-FR), 1.5.0

Search phrases this listing targets: horaires de prière · heures de prière · heure de prière · qibla · adhan · azan · application prière islam sans pub

**Title** `29 / 30`
```
SajdaTime: Horaires de prière
```

**Short description** `78 / 80`
```
Heures de prière hors ligne, adhan et Qibla, sunnites et chiites, avec Wear OS
```

**Full description** `3994 / 4000`
```
SajdaTime : horaires de prière, alertes d'adhan et boussole Qibla pour sunnites et chiites, sur téléphone Android et montre Wear OS. Sans pub ni compte, et les horaires marchent hors ligne.

Namaz, salah ou salat : les heures des cinq prières sont calculées sur votre téléphone. SajdaTime marche donc en avion, dans une cave ou sans réseau.

CE QUE VOUS OBTENEZ

• Fajr, lever du soleil, Dhuhr, Asr, Maghrib et Isha où que vous soyez, et compte à rebours en direct vers la prochaine prière
• Date hégirienne (calendrier islamique) près de celle du jour
• Boussole Qibla calée sur le nord géographique, qui vibre une fois face à la Kaaba
• Alerte à chaque prière, même l'appli fermée : aucune, notification ou alarme assez forte pour le Fajr
• Adhan (azan) intégré pour sunnites et chiites, ou tout son du téléphone
• Rappel facultatif 5 à 30 minutes avant les prières choisies
• Widgets d'accueil : grand compte à rebours vers la prochaine prière, et horaires du jour
• Ramadan intégré : suhoor (s'hour) et iftar, compte à rebours, durée du jeûne, calendrier du Ramadan, rappel facultatif du suhoor
• Horaires de prière en PDF à imprimer : jour, 7 jours, mois ou Ramadan
• Appli et tuile Wear OS autonomes, avec ou sans votre téléphone
• L'appli est en anglais, arabe, ourdou, turc et indonésien (pas en français), au choix ou selon le téléphone
• Thèmes clair et sombre. Compatible avec texte agrandi et lecteurs d'écran

ADAPTÉ À VOTRE ÉCOLE

• Sunnite : hanafite, chaféite, malikite, hanbalite, chacune avec sa bonne règle d'Asr
• Chiite : jafarite / duodécimain, avec la bonne règle du Maghrib
• 13 méthodes : Ligue islamique mondiale, ISNA, Umm al Qura, Égypte, Karachi, Dubaï, Koweït, Qatar, Kemenag/MUIS (Indonésie, Singapour), Diyanet (Turquie), Moonsighting Committee, Jafari et Téhéran
• Méthode selon votre école (Ligue islamique mondiale si sunnite, Jafari si chiite), modifiable
• Suivre votre mosquée : calez chaque prière sur son affichage (30 minutes d'écart au plus) ou décalez la date hégirienne d'un ou deux jours

VOTRE VIE PRIVÉE N'EST PAS LE PRIX

La plupart des applis de prière gratuites vivent de la pub, donc de vos données. Ici, rien à payer, d'aucune façon.

• Aucune pub, rien à acheter
• Ni compte, ni connexion, ni adresse mail
• Compteurs d'usage facultatifs, gérés dans les paramètres, désactivés par défaut. Aucun rapport de plantage
• Position approximative seulement, lue quand l'appli est ouverte. Vos coordonnées ne vont jamais au développeur ni dans les compteurs
• Le nom de la ville vient du service de localisation du téléphone, en général Google. La politique de confidentialité l'explique en détail
• Sauvegarde cloud désactivée : rien n'est copié sur un serveur de sauvegarde

Sans localisation ? Saisissez votre lieu : il est cherché une fois en ligne, puis tout marche pareil.

EN TOUTE HONNÊTETÉ

SajdaTime est une aide, pas une autorité religieuse. Votre téléphone calcule ces heures d'après la position du soleil, selon des méthodes publiées ; aucune mosquée, aucun savant ni aucune autorité ne les fournit. L'appli a été écrite par une seule personne, aidée de l'intelligence artificielle, et peut se tromper. Si SajdaTime et votre mosquée diffèrent, suivez votre mosquée. Si une heure ou une direction vous semble fausse, ou en cas de doute, merci de demander à votre mosquée ou à une autre personne qualifiée pour vous conseiller.

Les heures sont calculées avec adhan-java de Batoul Apps, bibliothèque open source très utilisée, et comparées avant chaque version à des calendriers de référence indépendants. Malgré tout, l'appli est offerte gratuitement et telle quelle, sans garantie ni promesse d'exactitude : si l'exactitude compte pour vous, merci de ne pas vous fier à elle seule.

GRATUIT, ET ÇA LE RESTERA

Ni version payante, ni abonnement, rien de verrouillé. SajdaTime est offert en sadaqa jariya pour la Oumma.

Je ne demande qu'une chose en retour : pensez à moi, à ma famille et à mes parents dans vos douas. Jazak Allah khayr.
```

### Hindi (hi-IN), 1.5.0

Search phrases this listing targets: नमाज़ का समय · नमाज का टाइम · किबला · अज़ान · नमाज का टाइम टेबल

**Title** `23 / 30`
```
SajdaTime: नमाज़ का समय
```

**Short description** `61 / 80`
```
ऑफ़लाइन नमाज़ का समय, अज़ान और किबला, सुन्नी और शिया, Wear OS
```

**Full description** `3991 / 4000`
```
SajdaTime सुन्नी और शिया मुसलमानों के लिए नमाज़ का समय, अज़ान अलर्ट और क़िबला कम्पास देता है, आपके Android फ़ोन और Wear OS घड़ी पर। कोई विज्ञापन नहीं, कोई खाता नहीं, और नमाज़ के समय ऑफ़लाइन मिलते हैं।

चाहे आप नमाज़ कहें या सलात, पाँचों वक़्त की नमाज़ का टाइम आपके अपने फ़ोन पर ही निकलता है, इसलिए SajdaTime हवाई जहाज़ में, तहख़ाने में या बिना किसी सिग्नल के भी चलता रहता है।

आपको क्या मिलता है

• फ़ज्र, सूर्योदय, ज़ुहर, अस्र, मग़रिब और इशा, आप जहाँ भी हों, अगली नमाज़ तक लाइव काउंटडाउन के साथ
• आज की तारीख़ के साथ हिजरी तारीख़ (इस्लामी कैलेंडर)
• असली उत्तर के हिसाब से ठीक किया गया क़िबला कम्पास, जो काबा की तरफ़ मुँह होते ही एक बार वाइब्रेट करता है
• हर नमाज़ के लिए अलर्ट, ऐप बंद होने पर भी: कुछ नहीं, नोटिफ़िकेशन, या फ़ज्र के लिए काफ़ी तेज़ अलार्म
• सुन्नी और शिया दोनों के लिए ऐप में ही अज़ान (azan), या फ़ोन की कोई भी आवाज़
• चुनी हुई नमाज़ों से 5 से 30 मिनट पहले वैकल्पिक रिमाइंडर
• होम स्क्रीन विजेट: अगली नमाज़ का बड़ा काउंटडाउन, और आज के नमाज़ के समय
• रमज़ान ऐप में ही: सहरी और इफ़्तार का समय, काउंटडाउन, हर रोज़े की अवधि, रमज़ान का टाइम टेबल और सहरी का वैकल्पिक रिमाइंडर
• आज, 7 दिन, पूरे महीने या रमज़ान का प्रिंट करने लायक़ PDF नमाज़ टाइम टेबल
• Wear OS ऐप और टाइल, फ़ोन के साथ या बिना, अपने आप चलते हैं
• ऐप ख़ुद अंग्रेज़ी, अरबी, उर्दू, तुर्की और इंडोनेशियाई में है, हिंदी में नहीं। भाषा ऐप में चुनें या फ़ोन के हिसाब से
• लाइट और डार्क थीम। बड़े अक्षरों और स्क्रीन रीडर के साथ काम करता है

आपके मसलक के लिए बना

• सुन्नी: हनफ़ी, शाफ़ई, मालिकी और हंबली, हर एक के लिए अस्र का सही नियम
• शिया: जाफ़री / इसना अशरी, मग़रिब के सही नियम के साथ
• हिसाब के 13 तरीक़े: मुस्लिम वर्ल्ड लीग, ISNA, उम्म अल क़ुरा, मिस्र, कराची, दुबई, कुवैत, क़तर, Kemenag/MUIS (इंडोनेशिया, सिंगापुर), दियानत (तुर्की), मूनसाइटिंग कमेटी, जाफ़री और तेहरान
• तरीक़ा आपके मसलक से तय होता है (सुन्नी के लिए मुस्लिम वर्ल्ड लीग, शिया के लिए जाफ़री), जिसे आप बदल सकते हैं
• अपनी मस्जिद से मिलाएँ: कोई भी नमाज़ मस्जिद के बोर्ड के समय पर सेट करें, 30 मिनट के अंदर, या इस्लामी तारीख़ एक या दो दिन आगे पीछे करें
• गर्मियों में सुदूर उत्तर में भी फ़ज्र और इशा के मुनासिब समय

आपकी निजता इसकी क़ीमत नहीं

ज़्यादातर मुफ़्त नमाज़ ऐप विज्ञापनों से चलते हैं, यानी क़ीमत आप अपने डेटा से चुकाते हैं। इसकी कोई क़ीमत है ही नहीं, किसी भी रूप में।

• कोई विज्ञापन नहीं, ख़रीदने को कुछ नहीं
• न खाता, न साइन इन, न ईमेल पता
• वैकल्पिक उपयोग आँकड़े, सेटिंग्स में आपके नियंत्रण में, आपकी हाँ के बिना बंद। कोई क्रैश रिपोर्टिंग नहीं
• सिर्फ़ अनुमानित लोकेशन, ऐप खुला होने पर ही पढ़ी जाती है। आपके कोऑर्डिनेट्स कभी डेवलपर तक या उपयोग आँकड़ों में नहीं जाते
• शहर का नाम फ़ोन की अपनी जगह खोजने वाली सेवा ढूँढती है, आम तौर पर Google। इसका ठीक क्या मतलब है, यह प्राइवेसी पॉलिसी में बताया गया है
• क्लाउड बैकअप बंद है, इसलिए किसी बैकअप सर्वर पर कुछ भी कॉपी नहीं होता

लोकेशन की अनुमति नहीं देना चाहते? तो अपनी जगह लिख दीजिए। उसे एक बार ऑनलाइन ढूँढा जाता है, फिर ऐप वैसे ही काम करता है।

यह क्या है, ईमानदारी से

SajdaTime एक मददगार है, कोई दीनी अथॉरिटी नहीं। आपका फ़ोन ये समय सूरज की स्थिति से, प्रकाशित गणना विधियों के ज़रिए निकालता है। ये किसी मस्जिद, आलिम या अथॉरिटी की ओर से नहीं दिए जाते। यह ऐप एक व्यक्ति ने आर्टिफ़िशियल इंटेलिजेंस की मदद से लिखा है, और इससे ग़लती हो सकती है। जहाँ SajdaTime और आपकी मस्जिद में अंतर हो, वहाँ अपनी मस्जिद के अनुसार चलें। अगर कभी कोई समय या दिशा ठीक न लगे, या आपको यक़ीन न हो, तो कृपया अपनी मस्जिद से या सलाह देने के योग्य किसी और व्यक्ति से पूछ लें।

नमाज़ के समय Batoul Apps की adhan-java से निकाले जाते हैं, जो बहुत इस्तेमाल होने वाली एक ओपन सोर्स लाइब्रेरी है, और हर रिलीज़ से पहले स्वतंत्र संदर्भ टाइम टेबल से मिलाकर जाँचे जाते हैं। फिर भी यह ऐप मुफ़्त और जैसा है वैसा ही दिया गया है, बिना किसी वारंटी और बिना सटीकता के किसी वादे के, इसलिए जहाँ बिल्कुल सही होना आपके लिए मायने रखता है, वहाँ कृपया सिर्फ़ इसी पर भरोसा न करें।

मुफ़्त, और मुफ़्त ही रहेगा

न पेड वर्ज़न, न सब्सक्रिप्शन, और कुछ भी लॉक नहीं। SajdaTime उम्मत के लिए सदक़ा जारिया के तौर पर मुफ़्त दिया गया है।

बदले में मैं बस एक ही चीज़ माँगता हूँ: कृपया मुझे, मेरे परिवार को और मेरे माँ बाप को अपनी दुआओं में याद रखें। जज़ाकल्लाह ख़ैर।
```

---

## Why these eight

| Language | Locale | Reaches | Search term that matters |
|---|---|---|---|
| Indonesian | `id-ID` | Indonesia — the largest Muslim population on earth | *Jadwal Sholat* |
| Malay | `ms-MY` | Malaysia, Brunei, Singapore | *Waktu Solat* |
| Urdu | `ur` | Pakistan, and Urdu readers across India and the diaspora | *نماز* |
| Bengali | `bn-BD` | Bangladesh and West Bengal | *নামাজ* |
| Arabic | `ar` | The Arab world, and the liturgical language everywhere | *أوقات الصلاة* |
| Turkish | `tr-TR` | Turkey, Cyprus, the large German-Turkish diaspora | *Namaz Vakitleri* |
| French | `fr-FR` | North and West Africa, and the European diaspora | *heures de prière* |
| Hindi | `hi-IN` | Indian users who do not read Urdu script | *नमाज़ का समय* |

Between them, these eight plus English cover the first language of the large majority of
Muslims worldwide.

---

## Indonesian (id-ID)

Search phrases this listing targets: jadwal sholat · waktu sholat · arah kiblat · jadwal imsakiyah

**Title** `24 / 30`

```
SajdaTime: Jadwal Sholat
```

**Short description** `79 / 80`

```
Jadwal sholat offline dan kompas arah kiblat untuk Sunni dan Syiah. Tanpa akun.
```

Why this title: "jadwal sholat" is the dominant Indonesian query (Google suggestions lead with it and Play's
top results for it are "Jadwal Sholat, Kiblat & Adzan", "ShalatKu", "Jadwal Sholat dan Kiblat"). "Jadwal Sholat, Kiblat"
would be 32 characters, so "arah kiblat" sits in the short description. "jadwal imsakiyah" is Indonesia's own Ramadan
phrase, so the release block's Ramadan line uses it and the January line repeats it. "Tanpa iklan" came out of the short
description. "Tidak ada iklan, selamanya" stays in the privacy section.

**Full description, release block** `3840 / 4000`

```
SajdaTime adalah aplikasi jadwal sholat dan kompas arah kiblat yang bekerja offline, di ponsel dan jam tangan Wear OS Anda.
Aplikasi ini memberi tahu kapan waktu sholat tiba dan ke arah mana Anda menghadap. Hanya itu yang dilakukannya, tanpa iklan, tanpa akun, dan tanpa pernah menerima lokasi Anda.

Waktu sholat dihitung langsung di ponsel Anda, sehingga aplikasi tetap berfungsi di pesawat, di ruang bawah tanah, atau tanpa sinyal sama sekali.

YANG ANDA DAPATKAN

• Lima waktu sholat wajib beserta terbit matahari, dihitung untuk lokasi Anda
• Tanggal Hijriah (kalender Islam) di samping tanggal biasa
• Hitung mundur langsung menuju waktu sholat berikutnya
• Kompas kiblat yang dikoreksi ke utara sejati, bukan utara magnetis
• Notifikasi pada setiap waktu sholat, bahkan saat aplikasi ditutup
• Mode alarm opsional dengan nada atau adzan di ponsel Anda, cukup keras untuk Subuh
• Lencana senyap opsional di panel notifikasi dengan sholat berikutnya
• Jadwal PDF untuk hari ini, tujuh hari ke depan, atau sebulan penuh
• Dua widget layar utama: sholat berikutnya dengan hitung mundur, dan semua waktu hari ini
• Saat Ramadan, aplikasi ini juga menjadi jadwal imsakiyah: Subuh mengakhiri sahur, Maghrib waktu berbuka
• Aplikasi Wear OS dan tile jam tangan yang bekerja mandiri
• Dalam bahasa Indonesia, Inggris, Urdu, dan Turki, dipilih di Pengaturan

SESUAI MAZHAB ANDA

Ketentuan Sunni dan Syiah keduanya didukung dengan benar, bukan sekadar tambahan.

• Sunni: Hanafi, Syafi'i, Maliki dan Hanbali, dengan aturan Ashar yang tepat untuk masing-masing
• Syiah: Jafari / Itsna Asyariyah, termasuk aturan Maghrib yang benar
• Metode perhitungan: Muslim World League, ISNA, Umm al-Qura, Mesir, Karachi, Dubai, Kuwait, Qatar, Singapura, Turki, Moonsighting Committee dan lainnya
• Metode dipilih otomatis sesuai mazhab Anda (Muslim World League untuk Sunni, Jafari untuk Syiah), dan dapat Anda ubah
• Aturan lintang tinggi untuk negara utara saat musim panas

PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi sholat gratis dibiayai iklan, artinya dibiayai data Anda. Yang ini tidak.

• Tidak ada iklan, selamanya
• Tidak ada akun, login, atau alamat email
• Penghitungan penggunaan opsional, Anda atur di Pengaturan. Tanpa laporan kerusakan
• Hanya lokasi perkiraan, dibaca saat aplikasi terbuka, tidak pernah di latar belakang
• Waktu dihitung di ponsel Anda dan kami tidak pernah menerima lokasi Anda. Nama kota dicari lewat Android, yang di sebagian besar ponsel bertanya ke Google
• Pencadangan awan sengaja dimatikan agar tidak ada yang disalin dari ponsel Anda

Jika Anda tidak ingin membagikan lokasi, ketikkan nama kota dan aplikasi bekerja persis sama.

JUJUR TENTANG APA ADANYA

SajdaTime adalah alat bantu, bukan otoritas keagamaan. Ponsel Anda menghitung waktu-waktu ini dari posisi matahari dengan metode perhitungan yang telah dipublikasikan. Waktu-waktu ini tidak diberikan oleh masjid, ulama, atau otoritas mana pun. Aplikasi ini ditulis oleh satu orang dengan bantuan kecerdasan buatan dan bisa saja keliru. Jika SajdaTime dan masjid Anda berbeda, ikuti masjid Anda. Jika suatu waktu atau arah terlihat tidak tepat, atau Anda ragu, mohon tanyakan kepada mereka atau kepada orang lain yang berkompeten.

Waktu sholat dihitung dengan adhan-java dari Batoul Apps, pustaka sumber terbuka, dan diperiksa terhadap jadwal rujukan independen sebelum setiap rilis. Meski begitu, aplikasi ini diberikan gratis apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi jika ketepatan penting bagi Anda, mohon jangan mengandalkannya semata.

GRATIS, DAN TETAP GRATIS

Tidak ada versi berbayar, tidak ada langganan, dan tidak ada yang dikunci. SajdaTime diberikan cuma-cuma sebagai sedekah jariyah untuk umat.

Hanya satu hal yang diminta sebagai balasannya: mohon doakan saya, keluarga saya, dan kedua orang tua saya. Jazakallah khair.
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3939 / 4000`:

```
• Saat Ramadan: akhir sahur dan waktu buka puasa untuk lokasi Anda, hitung mundur, lama puasa hari ini, jadwal imsakiyah lengkap (lihat atau simpan sebagai PDF), dan pengingat sahur opsional sebelum Subuh
```

---

## Malay (ms-MY)

Search phrases this listing targets: waktu solat · arah kiblat · jadual waktu solat · azan

**Title** `30 / 30`

```
SajdaTime: Waktu Solat, Kiblat
```

**Short description** `77 / 80`

```
Waktu solat luar talian dan kompas arah kiblat, Sunni dan Syiah. Tiada akaun.
```

Why this title: unchanged. "Waktu solat" is the Malaysian phrase (every top result for it is "Waktu Solat ...")
and "Kiblat" fits. "Luar talian" is the Malay for offline. The Umm al-Qura line was dropped for room because Malaysia
follows JAKIM, not Umm al-Qura, and the method is still listed. "Tiada iklan" came out of the short description.

**Full description, release block** `3854 / 4000`

```
SajdaTime ialah aplikasi waktu solat dan kompas arah kiblat yang berfungsi luar talian, di telefon dan jam tangan Wear OS anda.
Ia memberitahu anda bila masuk waktu solat dan ke arah mana untuk mengadap. Itu sahaja yang dilakukannya, tanpa iklan, tanpa akaun, dan tanpa pernah menerima lokasi anda.

Waktu solat dikira terus di telefon anda, jadi aplikasi ini terus berfungsi di dalam kapal terbang, di bawah tanah, atau tanpa isyarat langsung.

APA YANG ANDA DAPAT

• Lima waktu solat fardu serta syuruk, dikira untuk lokasi anda
• Tarikh Hijrah (kalendar Islam) di sebelah tarikh biasa
• Kiraan detik secara langsung menuju waktu solat berikutnya
• Kompas kiblat yang diperbetulkan kepada utara benar, bukan utara magnet
• Pemberitahuan pada setiap waktu solat, walaupun aplikasi ditutup
• Mod penggera pilihan dengan nada atau azan di telefon anda, cukup kuat untuk Subuh
• Lencana senyap pilihan di panel pemberitahuan dengan solat berikutnya
• Jadual PDF untuk hari ini, tujuh hari akan datang, atau sebulan penuh
• Dua widget skrin utama: solat berikutnya dengan kiraan detik, dan semua waktu hari ini
• Semasa Ramadan, ia juga menjadi jadual imsak dan berbuka: Subuh menamatkan sahur, Maghrib waktu berbuka
• Aplikasi Wear OS dan tile jam tangan yang berfungsi sendiri
• Dalam bahasa Inggeris, Urdu, Turki dan Indonesia, dipilih dalam Tetapan

MENGIKUT MAZHAB ANDA

Kaedah Sunni dan Syiah kedua-duanya disokong dengan betul, bukan sekadar tambahan.

• Sunni: Hanafi, Syafi'i, Maliki dan Hanbali, dengan kaedah Asar yang tepat bagi setiap satu
• Syiah: Jafari / Ithna Asyari, termasuk kaedah Maghrib yang betul
• Kaedah pengiraan: Muslim World League, ISNA, Umm al-Qura, Mesir, Karachi, Dubai, Kuwait, Qatar, Singapura, Turki, Moonsighting Committee dan banyak lagi
• Kaedah dipilih automatik mengikut mazhab anda (Muslim World League untuk Sunni, Jafari untuk Syiah), dan boleh anda ubah
• Peraturan latitud tinggi untuk negara utara yang matahari tidak terbenam sepenuhnya pada musim panas

PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi solat percuma dibiayai iklan, bermakna dibiayai data anda. Yang ini tidak.

• Tiada iklan, selamanya
• Tiada akaun, log masuk atau alamat e-mel
• Kiraan penggunaan pilihan yang anda kawal dalam Tetapan. Tiada laporan ranap
• Lokasi anggaran sahaja, dibaca semasa aplikasi dibuka, tidak pernah di latar belakang
• Waktu dikira pada telefon anda dan kami tidak pernah menerima lokasi anda. Nama bandar dicari melalui Android, yang pada kebanyakan telefon bertanya kepada Google
• Sandaran awan sengaja dimatikan supaya tiada apa boleh disalin dari telefon anda

Jika anda tidak mahu berkongsi lokasi, taip nama bandar dan aplikasi berfungsi sama.

JUJUR TENTANG APA ADANYA

SajdaTime ialah pembantu, bukan pihak berkuasa agama. Telefon anda mengira waktu-waktu ini daripada kedudukan matahari menggunakan kaedah pengiraan yang diterbitkan. Waktu-waktu ini tidak dibekalkan oleh mana-mana masjid, ulama atau pihak berkuasa. Aplikasi ini ditulis oleh seorang dengan bantuan kecerdasan buatan dan boleh tersilap. Jika SajdaTime dan masjid anda berbeza, ikutlah masjid anda. Jika sesuatu waktu atau arah kelihatan tidak tepat, atau anda ragu-ragu, sila tanya mereka atau orang lain yang berkelayakan.

Waktu solat dikira menggunakan adhan-java oleh Batoul Apps, pustaka sumber terbuka, dan disemak terhadap jadual rujukan bebas sebelum setiap keluaran. Aplikasi ini diberikan percuma sebagaimana adanya, tanpa waranti dan tanpa janji ketepatan, jadi jika ketepatan penting bagi anda, jangan bergantung padanya semata-mata.

PERCUMA, DAN KEKAL PERCUMA

Tiada versi berbayar, tiada langganan dan tiada apa-apa yang dikunci. SajdaTime diberikan secara percuma sebagai sedekah jariah untuk umat.

Hanya satu perkara yang diminta sebagai balasan: tolong doakan saya, keluarga saya dan ibu bapa saya. Jazakallah khair.
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3955 / 4000`:

```
• Semasa Ramadan: akhir sahur dan waktu berbuka untuk lokasi anda, kiraan detik, tempoh puasa hari ini, jadual imsak penuh untuk dilihat atau disimpan sebagai PDF, dan peringatan sahur pilihan sebelum Subuh
```

---

## Urdu (ur)

Search phrases this listing targets: نماز کے اوقات · نماز کا وقت · قبلہ رخ · نماز کا وقت حنفی

**Title** `30 / 30`

```
SajdaTime: نماز کے اوقات، قبلہ
```

**Short description** `75 / 80`

```
آف لائن نماز کے اوقات اور قبلہ نما، سنی اور شیعہ دونوں کے لیے۔ اکاؤنٹ نہیں۔
```

Why this title: "نماز کے اوقات" is the exact top Urdu query (Google suggestions in Pakistan lead with it, and
Dawat-e-Islami's Urdu title is "نماز کا وقت"). The previous "اوقات نماز" is the formal form and is still in the body. The
short description keeps "آف لائن", "قبلہ نما" and "سنی اور شیعہ". "نماز کا وقت حنفی" is a real query, and "حنفی" is in
the school section. Right-to-left: paste as is, the Console handles direction.

**Full description, release block** `3495 / 4000`

```
SajdaTime نماز کے اوقات اور قبلہ نما کی ایپ ہے جو آف لائن کام کرتی ہے، آپ کے فون اور Wear OS گھڑی پر۔
یہ آپ کو بتاتی ہے کہ نماز کا وقت کب ہے اور کس سمت رخ کرنا ہے۔ بس یہی اس کا کام ہے، بغیر اشتہارات کے، بغیر اکاؤنٹ کے، اور آپ کا مقام کبھی وصول کیے بغیر۔

نماز کے اوقات آپ کے اپنے فون پر ہی شمار کیے جاتے ہیں، اس لیے یہ ایپ جہاز میں، تہہ خانے میں، یا بالکل سگنل کے بغیر بھی کام کرتی رہتی ہے۔

آپ کو کیا ملتا ہے

• پانچوں فرض نمازیں اور طلوعِ آفتاب، آپ کے مقام کے مطابق
• عام تاریخ کے ساتھ ہجری تاریخ (اسلامی کیلنڈر)
• اگلی نماز تک زندہ گنتی
• قبلہ نما جو مقناطیسی نہیں بلکہ حقیقی شمال کے مطابق درست کیا گیا ہے
• ہر نماز کے وقت اطلاع، چاہے ایپ بند ہو
• اختیاری الارم موڈ، آپ کے فون پر موجود کسی بھی ٹون یا اذان کے ساتھ، فجر کے لیے جگانے کو کافی بلند
• اطلاعات میں اختیاری خاموش نشان جو اگلی نماز دکھاتا ہے
• آج، اگلے سات دن، یا پورے مہینے کا قابلِ طباعت PDF نقشہ اوقات
• ہوم اسکرین کے دو ویجٹ: اگلی نماز الٹی گنتی کے ساتھ، اور آج کے تمام اوقات
• رمضان میں یہ سحری اور افطار کا نقشہ بھی ہے: فجر پر سحری ختم، مغرب پر افطار
• Wear OS ایپ اور واچ ٹائل جو فون کے ساتھ یا بغیر، خود مختار کام کرتی ہے
• روشن اور تاریک تھیم، فون کے مطابق یا خود منتخب
• اردو، انگریزی، ترکی اور انڈونیشی میں، ترتیبات سے منتخب کریں

آپ کے مسلک کے مطابق

سنی اور شیعہ دونوں کے اصول درست طور پر شامل ہیں، محض ضمنی طور پر نہیں۔

• سنی: حنفی، شافعی، مالکی اور حنبلی، ہر ایک کے لیے عصر کا درست اصول
• شیعہ: جعفری / اثنا عشری، بشمول مغرب کا درست اصول
• حسابی طریقے: مسلم ورلڈ لیگ، ISNA، ام القریٰ، مصری، کراچی، دبئی، کویت، قطر، سنگاپور، ترکی، مون سائٹنگ کمیٹی اور مزید
• طریقہ آپ کے انتخاب، سنی یا شیعہ، کے مطابق خود بخود منتخب ہوتا ہے (سنی کے لیے مسلم ورلڈ لیگ، شیعہ کے لیے جعفری)، اور آپ اسے بدل سکتے ہیں
• بلند عرض بلد کا اصول شمالی ممالک کے لیے جہاں گرمیوں میں سورج پوری طرح غروب نہیں ہوتا
• ام القریٰ طریقے کے ساتھ، رمضان میں اس کی عشاء کی ترمیم خود بخود لاگو ہوتی ہے

آپ کی رازداری اس کی قیمت نہیں

زیادہ تر مفت نماز ایپس اشتہارات سے چلتی ہیں، یعنی آپ کے ڈیٹا سے چلتی ہیں۔ یہ ایپ نہیں۔

• کبھی کوئی اشتہار نہیں
• نہ اکاؤنٹ، نہ سائن اِن، نہ ای میل پتہ
• استعمال کی اختیاری گنتی، جس پر آپ کا اختیار سیٹنگز میں ہے۔ کوئی کریش رپورٹنگ نہیں
• صرف تخمینی مقام، وہ بھی جب ایپ کھلی ہو، پس منظر میں کبھی نہیں
• اوقات آپ کے فون پر نکالے جاتے ہیں اور ہم آپ کا مقام کبھی وصول نہیں کرتے۔ شہر کا نام اینڈرائیڈ سے معلوم کیا جاتا ہے، جو زیادہ تر فونز پر گوگل سے پوچھتا ہے
• کلاؤڈ بیک اپ جان بوجھ کر بند ہے تاکہ آپ کے فون سے کچھ نقل نہ ہو سکے

اگر آپ اپنا مقام بالکل بھی نہیں بتانا چاہتے تو شہر کا نام لکھ دیں، ایپ بالکل ویسے ہی کام کرے گی۔

جو ہے، سچ ہے

SajdaTime ایک مددگار ہے، دینی مرجع نہیں۔ آپ کا فون یہ اوقات سورج کی پوزیشن سے، شائع شدہ حسابی طریقوں کے ذریعے نکالتا ہے۔ یہ کسی مسجد، عالم یا اتھارٹی کی طرف سے فراہم نہیں کیے گئے۔ یہ ایپ ایک شخص نے مصنوعی ذہانت کی مدد سے بنائی ہے اور اس سے غلطی ہو سکتی ہے۔ جہاں SajdaTime اور آپ کی مسجد میں اختلاف ہو، وہاں اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت درست نہ لگے، یا آپ کو یقین نہ ہو، تو براہِ کرم ان سے یا کسی اور اہل شخص سے پوچھ لیں۔

نماز کے اوقات Batoul Apps کی اوپن سورس لائبریری adhan-java سے شمار ہوتے ہیں اور ہر اجرا سے پہلے آزاد حوالہ جاتی نقشوں سے جانچے جاتے ہیں۔ پھر بھی یہ ایپ مفت ہے اور جیسی ہے ویسی ہی دی جا رہی ہے، کسی ضمانت اور درستگی کے کسی وعدے کے بغیر، اس لیے جہاں بالکل درست ہونا آپ کے لیے اہم ہو، وہاں صرف اسی پر بھروسہ نہ کریں۔

مفت، اور مفت ہی رہے گی

نہ کوئی ادائیگی، نہ سبسکرپشن، نہ کچھ بند۔ SajdaTime امت کے لیے صدقہ جاریہ کے طور پر مفت پیش کی گئی ہے۔

بدلے میں بس ایک گزارش ہے: مجھے، میرے گھر والوں اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔ جزاک اللہ خیر۔
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3619 / 4000`:

```
• رمضان میں: آپ کے مقام کے لیے سحری کا اختتام اور افطار، الٹی گنتی، آج کے روزے کا دورانیہ، کل کے اوقات، دیکھنے یا PDF میں محفوظ کرنے کے لیے رمضان کا پورا نقشہ، اور فجر سے پہلے سحری کی اختیاری یاددہانی
```

---

## Bengali (bn-BD)

Search phrases this listing targets: নামাজের সময়সূচি · নামাজের সময় · কিবলা কম্পাস

**Title** `27 / 30`

```
SajdaTime: নামাজের সময়সূচি
```

**Short description** `77 / 80`

```
অফলাইন নামাজের সময় ও কিবলা কম্পাস, সুন্নি ও শিয়ার জন্য। অ্যাকাউন্ট লাগে না।
```

Why this title: "নামাজের সময়সূচি" (timetable) is the number one Bengali suggestion and the word in the top two
Play results ("Ibadah - নামাজের সময়সূচি", "নামাজের সময়সূচি বাংলাদেশ"). "কিবলা" moved to the short description, where
"কিবলা কম্পাস" is the top Bengali Qibla suggestion.

**Full description, release block** `3636 / 4000`

```
SajdaTime একটি নামাজের সময়সূচি ও কিবলা কম্পাস অ্যাপ, যা অফলাইনে চলে, আপনার ফোনে ও Wear OS ঘড়িতে।
এটি আপনাকে জানায় কখন নামাজের সময় এবং কোন দিকে মুখ করতে হবে। এটুকুই এর কাজ, কোনও বিজ্ঞাপন ছাড়া, কোনও অ্যাকাউন্ট ছাড়া, এবং আপনার অবস্থান কখনও না পেয়ে।

নামাজের সময় আপনার নিজের ফোনেই গণনা করা হয়, তাই অ্যাপটি বিমানে, বেসমেন্টে, বা একেবারে নেটওয়ার্ক ছাড়াও কাজ করে।

আপনি যা পাবেন

• পাঁচ ওয়াক্ত নামাজ ও সূর্যোদয়, আপনার অবস্থান অনুযায়ী গণনা করা
• সাধারণ তারিখের পাশে হিজরি তারিখ (ইসলামি ক্যালেন্ডার)
• পরবর্তী নামাজ পর্যন্ত সরাসরি ক্ষণগণনা
• কিবলা কম্পাস, চৌম্বক উত্তর নয়, প্রকৃত উত্তর অনুযায়ী সংশোধিত
• প্রতি ওয়াক্তে বিজ্ঞপ্তি, অ্যাপ বন্ধ থাকলেও
• ঐচ্ছিক অ্যালার্ম মোড, আপনার ফোনে থাকা যেকোনও সুর বা আজানে, ফজরে জাগানোর মতো জোরালো
• ঐচ্ছিক নীরব ব্যাজ যা বিজ্ঞপ্তি প্যানেলে পরবর্তী নামাজ দেখায়
• আজ, আগামী সাত দিন বা পুরো মাসের মুদ্রণযোগ্য PDF সময়সূচি
• হোম স্ক্রিনের দুটি উইজেট: ক্ষণগণনাসহ পরবর্তী নামাজ, আর আজকের সব সময়
• রমজানে এটি সাহরি ও ইফতারের সময়সূচিও: ফজরে সাহরি শেষ, মাগরিবে ইফতার
• Wear OS অ্যাপ ও ঘড়ির টাইল, ফোন থাকুক বা না থাকুক নিজেই চলে
• উজ্জ্বল ও অন্ধকার থিম, ফোন অনুযায়ী বা নিজে বেছে নেওয়া
• ইংরেজি, উর্দু, তুর্কি ও ইন্দোনেশীয় ভাষায়, সেটিংস থেকে বেছে নেওয়া যায়

আপনার মাযহাব অনুযায়ী

সুন্নি ও শিয়া দুই রীতিই সঠিকভাবে সমর্থিত, নামমাত্র নয়।

• সুন্নি: হানাফি, শাফিয়ি, মালিকি ও হাম্বলি, প্রত্যেকের জন্য সঠিক আসরের নিয়ম
• শিয়া: জাফরি / ইসনা আশারি, সঠিক মাগরিবের নিয়মসহ
• গণনা পদ্ধতি: মুসলিম ওয়ার্ল্ড লিগ, ISNA, উম্মুল কুরা, মিশরীয়, করাচি, দুবাই, কুয়েত, কাতার, সিঙ্গাপুর, তুরস্ক, মুনসাইটিং কমিটি এবং আরও
• পদ্ধতি আপনার পছন্দ, সুন্নি বা শিয়া, অনুযায়ী স্বয়ংক্রিয়ভাবে নির্বাচিত হয় (সুন্নির জন্য মুসলিম ওয়ার্ল্ড লিগ, শিয়ার জন্য জাফরি), এবং আপনি বদলাতে পারেন
• উচ্চ অক্ষাংশের নিয়ম, উত্তরের দেশগুলির জন্য যেখানে গ্রীষ্মে সূর্য পুরোপুরি অস্ত যায় না
• উম্মুল কুরা পদ্ধতিতে এর রমজানের ইশার সমন্বয় স্বয়ংক্রিয়ভাবে প্রযোজ্য হয়

আপনার গোপনীয়তাই এর মূল্য নয়

বেশিরভাগ বিনামূল্যের নামাজ অ্যাপ বিজ্ঞাপনে চলে, অর্থাৎ আপনার তথ্যে চলে। এটি নয়।

• কখনও কোনও বিজ্ঞাপন নয়
• কোনও অ্যাকাউন্ট নেই, সাইন-ইন নেই, ইমেল ঠিকানা নেই
• ঐচ্ছিক ব্যবহারের গণনা, যা আপনি সেটিংসে নিয়ন্ত্রণ করেন। ক্র্যাশ রিপোর্ট নেই
• কেবল আনুমানিক অবস্থান, অ্যাপ খোলা থাকলে পড়া হয়, কখনও পটভূমিতে নয়
• সময় আপনার ফোনেই হিসাব করা হয় এবং আমরা আপনার অবস্থান কখনও পাই না। শহরের নাম অ্যান্ড্রয়েডের মাধ্যমে খোঁজা হয়, যা বেশিরভাগ ফোনে গুগলকে জিজ্ঞাসা করে
• ক্লাউড ব্যাকআপ ইচ্ছাকৃতভাবে বন্ধ, যাতে ফোন থেকে কিছু অনুলিপি করা না যায়

আপনি যদি অবস্থান একেবারেই ভাগ করতে না চান, শহরের নাম লিখে দিন, অ্যাপ ঠিক একইভাবে কাজ করবে।

যা, তা নিয়ে সৎ

SajdaTime একটি সহায়ক, ধর্মীয় কর্তৃপক্ষ নয়। আপনার ফোন এই সময়গুলো সূর্যের অবস্থান থেকে, প্রকাশিত গণনা পদ্ধতি ব্যবহার করে হিসাব করে। এগুলো কোনও মসজিদ, আলেম বা কর্তৃপক্ষের দেওয়া নয়। অ্যাপটি একজন মানুষ কৃত্রিম বুদ্ধিমত্তার সহায়তায় লিখেছেন এবং এতে ভুল হতে পারে। SajdaTime ও আপনার মসজিদের মধ্যে অমিল হলে আপনার মসজিদের অনুসরণ করুন। কোনও সময় বা দিক ভুল মনে হলে, বা আপনি নিশ্চিত না হলে, অনুগ্রহ করে তাঁদের কাছে অথবা অন্য কোনও যোগ্য ব্যক্তির কাছে জেনে নিন।

নামাজের সময় Batoul Apps-এর adhan-java দিয়ে গণনা করা হয়, যা একটি বহুল ব্যবহৃত ওপেন সোর্স লাইব্রেরি, এবং প্রতিটি প্রকাশের আগে স্বাধীন রেফারেন্স সময়সূচির সঙ্গে মিলিয়ে দেখা হয়। তবুও অ্যাপটি বিনামূল্যে ও যেমন আছে তেমনভাবে দেওয়া, কোনও ওয়ারেন্টি বা নির্ভুলতার প্রতিশ্রুতি ছাড়া, তাই নির্ভুলতা যেখানে আপনার কাছে গুরুত্বপূর্ণ, সেখানে শুধু এর ওপর নির্ভর করবেন না।

বিনামূল্যে, এবং বিনামূল্যেই থাকবে

কোনও অর্থপ্রদান নেই, সাবস্ক্রিপশন নেই, কিছুই আটকানো নেই। SajdaTime উম্মাহর জন্য সদকায়ে জারিয়া হিসেবে বিনামূল্যে দেওয়া হয়েছে।

বিনিময়ে শুধু একটি অনুরোধ: আমাকে, আমার পরিবারকে ও আমার বাবা-মাকে আপনার দোয়ায় রাখবেন। জাযাকাল্লাহু খাইরান।
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3765 / 4000`:

```
• রমজানে: আপনার অবস্থানের জন্য সাহরির শেষ সময় ও ইফতার, ক্ষণগণনা, আজকের রোজার দৈর্ঘ্য, আগামীকালের সময়, দেখার বা PDF হিসেবে সংরক্ষণের জন্য পূর্ণ রমজান সময়সূচি, এবং ফজরের আগে ঐচ্ছিক সাহরির অনুস্মারক
```

---

## Arabic (ar)

Search phrases this listing targets: مواقيت الصلاة · اوقات الصلاة · اتجاه القبلة · تطبيق مواقيت الصلاة بدون نت

**Title** `30 / 30`

```
SajdaTime: مواقيت الصلاة، قبلة
```

**Short description** `73 / 80`

```
أوقات الصلاة واتجاه القبلة دون إنترنت، للسنة والشيعة. بلا حساب ولا تسجيل.
```

Why this title: "مواقيت الصلاة" is the Saudi and Gulf phrase, "أوقات الصلاة" the Egyptian one. The title carries
"مواقيت" and the short description carries "أوقات", so both are indexed. "اتجاه القبلة" is the Arabic Qibla query (all
twelve top results for it in Egypt use those two words). "مجاني" and "بلا إعلانات" came out of the short description:
"مجاني" is a price word. "دون إنترنت" matches the real query "تطبيق مواقيت الصلاة بدون نت". The Ramadan line uses
"إمساكية", the everyday word.

**Full description, release block** `3084 / 4000`

```
SajdaTime تطبيق لمواقيت الصلاة واتجاه القبلة يعمل دون إنترنت، على هاتفك وعلى ساعة Wear OS.
يخبرك بموعد الصلاة وباتجاه القبلة. هذا كل ما يفعله، بلا إعلانات، وبلا حساب، ودون أن نستلم موقعك أبدًا.

تُحسب أوقات الصلاة على هاتفك نفسه، لذا يظل التطبيق يعمل في الطائرة، أو في قبو، أو دون أي تغطية إطلاقًا.

ما الذي تحصل عليه

• الصلوات الخمس مع الشروق، محسوبة لمكانك أنت
• التاريخ الهجري إلى جانب التاريخ الميلادي
• عدّ تنازلي مباشر حتى الصلاة التالية
• بوصلة قبلة مصحّحة إلى الشمال الحقيقي لا الشمال المغناطيسي
• إشعارات عند كل صلاة، حتى والتطبيق مغلق
• وضع منبّه اختياري بأي نغمة أو أذان موجود على هاتفك، عالٍ بما يكفي لإيقاظك للفجر
• شارة هادئة اختيارية في لوحة الإشعارات تُظهر الصلاة التالية
• جدول PDF قابل للطباعة لليوم، أو للأيام السبعة القادمة، أو للشهر كاملًا
• ويدجتان على الشاشة الرئيسية: الصلاة التالية مع عدّ تنازلي، وجميع أوقات اليوم
• في رمضان يصبح إمساكية أيضًا: الفجر نهاية السحور، والمغرب موعد الإفطار
• تطبيق Wear OS وبطاقة للساعة يعملان بشكل مستقل، مع الهاتف أو بدونه
• مظهر فاتح وداكن، يتبع هاتفك أو تختاره بنفسك
• بالإنجليزية والأردية والتركية والإندونيسية، وتختار منها من الإعدادات

وفق مذهبك

يُدعم المذهبان السنّي والشيعي دعمًا صحيحًا، لا على سبيل الإضافة.

• السنّة: الحنفي والشافعي والمالكي والحنبلي، مع قاعدة العصر الصحيحة لكل منها
• الشيعة: الجعفري / الإثنا عشري، بما في ذلك قاعدة المغرب الصحيحة
• طرق الحساب: رابطة العالم الإسلامي، ISNA، أم القرى، الهيئة المصرية، كراتشي، دبي، الكويت، قطر، سنغافورة، تركيا، لجنة رؤية الهلال وغيرها
• تُختار الطريقة تلقائيًا بحسب اختيارك، سنّي أو شيعي (رابطة العالم الإسلامي للسنّة، الجعفري للشيعة)، ويمكنك تغييرها
• قاعدة خطوط العرض العالية للبلدان الشمالية حيث لا تغيب الشمس تمامًا في الصيف
• مع طريقة أم القرى، يُطبَّق تعديل العشاء في رمضان تلقائيًا

خصوصيتك ليست هي الثمن

معظم تطبيقات الصلاة المجانية تموّلها الإعلانات، أي تموّلها بياناتك. هذا التطبيق لا.

• لا إعلانات، أبدًا
• لا حسابات، ولا تسجيل دخول، ولا عنوان بريد
• عدّ اختياري للاستخدام تتحكم به من الإعدادات. لا تقارير أعطال
• موقع تقريبي فقط، يُقرأ والتطبيق مفتوح، ولا يُقرأ في الخلفية أبدًا
• تُحسب الأوقات على هاتفك ولا نستلم موقعك أبدًا. يُجلب اسم مدينتك عبر أندرويد، وهو يسأل غوغل في معظم الهواتف
• النسخ الاحتياطي السحابي معطّل عمدًا حتى لا يُنسخ شيء من هاتفك

وإن كنت تفضّل ألا تشارك موقعك إطلاقًا، فاكتب اسم مدينتك ويعمل التطبيق بالطريقة نفسها.

صريح فيما هو عليه

SajdaTime أداة مساعدة، وليس مرجعًا شرعيًا. يحسب هاتفك هذه الأوقات من موضع الشمس باستخدام طرق حساب منشورة، وهي ليست صادرة عن أي مسجد أو عالم أو جهة. كتب التطبيق شخص واحد بمساعدة الذكاء الاصطناعي، وقد يخطئ. إذا اختلف SajdaTime مع مسجدك فاتبع مسجدك. وإن بدا لك وقت أو اتجاه غير صحيح، أو لم تكن متأكدًا، فالرجاء سؤال مسجدك أو أي شخص مؤهل.

تُحسب أوقات الصلاة باستخدام مكتبة adhan-java من Batoul Apps، وهي مكتبة مفتوحة المصدر واسعة الاستخدام، وتُراجَع مقابل جداول مرجعية مستقلة قبل كل إصدار. ومع ذلك فالتطبيق مقدَّم مجانًا وكما هو، دون أي ضمان ودون وعد بالدقة، فحيث تهمّك الدقة، فلا تعتمد عليه وحده.

مجاني، وسيبقى مجانيًا

لا نسخة مدفوعة، ولا اشتراك، ولا شيء محجوب. يُقدَّم SajdaTime مجانًا صدقةً جاريةً للأمة.

لا أطلب في المقابل سوى شيء واحد: أن تذكرني وأهلي ووالديّ في دعائك. جزاكم الله خيرًا.
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3177 / 4000`:

```
• في رمضان: انتهاء السحور وموعد الإفطار لموقعك، وعدّ تنازلي، ومدة صيام اليوم، وأوقات الغد، وإمساكية كاملة للعرض أو الحفظ بصيغة PDF، وتذكير اختياري بالسحور قبل الفجر
```

---

## Turkish (tr-TR)

Search phrases this listing targets: namaz vakitleri · ezan vakti · kıble · namaz uygulaması internetsiz · imsakiye

**Title** `26 / 30`

```
SajdaTime: Namaz Vakitleri
```

**Short description** `76 / 80`

```
İnternetsiz namaz vakitleri ve kıble pusulası, Sünni ve Şii için. Hesap yok.
```

Why this title: unchanged, "namaz vakitleri" is the exact top Turkish query and nine of the twelve top Play
results carry those two words. The short description now says "İnternetsiz" rather than "Çevrimdışı" because
"namaz uygulaması internetsiz" is what people type (Google suggestions, Turkey). "Reklamsız" is a real query too
("reklamsız namaz vakitleri uygulaması"), and one competitor even has it in its title, but it is a promotion word under
the metadata policy, so it lives in the body ("Asla reklam yok"), not the short description. "İmsakiye" is the Turkish
Ramadan word and is used in both Ramadan lines.

**Full description, release block** `3769 / 4000`

```
SajdaTime, telefonunuzda ve Wear OS saatinizde internetsiz çalışan bir namaz vakitleri ve kıble pusulası uygulamasıdır.
Size namaz vaktinin ne zaman girdiğini ve hangi yöne döneceğinizi söyler. Yaptığı tek şey budur. Reklamsız, hesapsız ve konumunuzu biz hiç almadan.

Namaz vakitleri doğrudan telefonunuzda hesaplanır, bu yüzden uygulama uçakta, bodrumda ya da hiç sinyal yokken bile çalışmaya devam eder.

NELER SUNAR

• Beş vakit namaz ve güneşin doğuşu, bulunduğunuz yere göre hesaplanır
• Miladi tarihin yanında hicri tarih (dini günler takvimi)
• Bir sonraki namaza canlı geri sayım
• Manyetik kuzeye değil, gerçek kuzeye göre düzeltilmiş kıble pusulası
• Her namaz vaktinde bildirim, uygulama kapalıyken bile
• Telefonunuzdaki herhangi bir ses ya da ezan kaydıyla isteğe bağlı alarm modu, sabah namazına kaldıracak kadar yüksek
• Bildirim panelinde bir sonraki namazı gösteren isteğe bağlı sessiz rozet
• Bugün, önümüzdeki yedi gün veya tüm ay için yazdırılabilir PDF vakit cetveli
• İki ana ekran widget'ı: geri sayımlı sonraki namaz ve bugünün tüm vakitleri
• Ramazan'da imsakiye olarak da kullanılır: sabah namazı sahuru bitirir, akşam namazı iftarı açar
• Telefonla ya da telefonsuz kendi başına çalışan Wear OS uygulaması ve saat kartı
• Açık ve koyu tema, telefonu izler ya da elle seçilir
• Türkçe, İngilizce, Urduca ve Endonezce, Ayarlar'dan seçilir

MEZHEBİNİZE GÖRE

Sünni ve Şii usulleri sonradan eklenmiş gibi değil, gerçekten doğru şekilde desteklenir.

• Sünni: Hanefi, Şafii, Maliki ve Hanbeli, her biri için doğru ikindi kuralıyla
• Şii: Caferi / İsna Aşeri, doğru akşam kuralı dahil
• Hesaplama yöntemleri: Müslüman Dünya Birliği, ISNA, Ümmü'l-Kura, Mısır, Karaçi, Dubai, Kuveyt, Katar, Singapur, Türkiye, Moonsighting Committee ve daha fazlası
• Yöntem, seçtiğiniz mezhebe göre otomatik belirlenir (Sünni için Müslüman Dünya Birliği, Şii için Caferi) ve dilerseniz değiştirebilirsiniz
• Yazın güneşin tam batmadığı kuzey ülkeleri için yüksek enlem kuralı
• Ümmü'l-Kura yöntemiyle Ramazan yatsı düzeltmesi otomatik uygulanır

BEDELİ MAHREMİYETİNİZ DEĞİL

Ücretsiz namaz uygulamalarının çoğu reklamla, yani sizin verinizle finanse edilir. Bu uygulama değil.

• Asla reklam yok
• Hesap yok, giriş yok, e-posta adresi yok
• Ayarlardan kontrol ettiğiniz isteğe bağlı kullanım sayıları. Çökme raporu yok
• Yalnızca yaklaşık konum, uygulama açıkken okunur, arka planda asla
• Vakitler telefonunuzda hesaplanır ve konumunuzu biz hiç almayız. Şehir adı Android üzerinden bulunur, bu da çoğu telefonda Google’a sorar
• Bulut yedeklemesi bilerek kapatılmıştır, telefonunuzdan hiçbir şey kopyalanamaz

Konumunuzu hiç paylaşmak istemiyorsanız şehir adını yazmanız yeterli. Uygulama tamamen aynı şekilde çalışır.

NE OLDUĞU KONUSUNDA DÜRÜST

SajdaTime bir yardımcıdır, dinî bir merci değildir. Telefonunuz bu vakitleri güneşin konumundan, yayımlanmış hesaplama yöntemleriyle hesaplar. Hiçbir cami, âlim ya da makam tarafından verilmiş değildir. Uygulama tek kişi tarafından yapay zekâ desteğiyle yazılmıştır ve hata yapabilir. SajdaTime ile caminiz arasında fark varsa caminize uyun. Bir vakit veya yön size yanlış görünürse ya da emin değilseniz lütfen onlara veya ehil birine danışın.

Namaz vakitleri Batoul Apps'in açık kaynaklı adhan-java kütüphanesiyle hesaplanır ve her sürümden önce bağımsız referans cetvelleriyle karşılaştırılır. Yine de uygulama ücretsiz ve olduğu gibi sunulur, hiçbir garanti ve doğruluk vaadi yoktur. Kesinliğin sizin için önemli olduğu yerde lütfen yalnızca buna güvenmeyin.

ÜCRETSİZ VE ÜCRETSİZ KALACAK

Ücretli sürüm, abonelik ya da kilitli özellik yoktur. SajdaTime, ümmet için sadaka-i cariye olarak karşılıksız sunulmaktadır.

Karşılığında istenen tek şey: beni, ailemi ve anne babamı dualarınızda anın. Allah razı olsun.
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3892 / 4000`:

```
• Ramazan'da: bulunduğunuz yer için imsak ve iftar, geri sayım, bugünkü orucun süresi, yarının vakitleri, görüntülenecek veya PDF olarak kaydedilecek tam imsakiye ve sabah namazından önce isteğe bağlı sahur hatırlatıcısı
```

---

## French (fr-FR)

Search phrases this listing targets: horaires de prière · heure de prière · qibla · application prière islam sans pub

**Title** `29 / 30`

```
SajdaTime: Horaires de prière
```

**Short description** `78 / 80`

```
Heures de prière hors ligne et boussole Qibla, sunnite et chiite. Sans compte.
```

Why this title: "horaires de prière" and "heures de prière" are both strong (MAWAQIT, Salatuk and Kodelokus
use "horaires", ZeeDev uses "heure"). The title takes "Horaires de prière" and the short description "Heures de prière",
so both forms are indexed. "Sans pub" came out of the short description. The Umm al-Qura line and the long method list
were shortened for room. "Application prière islam sans pub" and "fiable" are real French queries, which the privacy
and HONEST sections answer in the body.

**Full description, release block** `3897 / 4000`

```
SajdaTime est une application d'horaires de prière et de boussole Qibla qui fonctionne hors ligne, sur votre téléphone et votre montre Wear OS.
Elle vous indique quand prier et dans quelle direction vous tourner. C'est tout ce qu'elle fait, sans publicité, sans compte, et sans jamais recevoir votre position.

Les heures de prière sont calculées sur votre propre téléphone : l'application continue donc de fonctionner en avion, dans un sous-sol, ou sans aucun réseau.

CE QUE VOUS OBTENEZ

• Les cinq prières quotidiennes et le lever du soleil, calculés pour votre position
• La date hégirienne (calendrier islamique) à côté de la date ordinaire
• Un compte à rebours en direct jusqu'à la prochaine prière
• Une boussole Qibla corrigée vers le nord géographique, et non le nord magnétique
• Des notifications à chaque heure de prière, même application fermée
• Un mode réveil facultatif avec une sonnerie ou un adhan de votre téléphone, assez fort pour le Fajr
• Un horaire PDF pour aujourd'hui, les sept prochains jours ou le mois entier
• Deux widgets d'accueil : prochaine prière avec compte à rebours, et heures du jour
• Pendant le Ramadan, elle sert aussi d'horaire de suhoor et d'iftar : le Fajr clôt le suhoor, le Maghrib ouvre le jeûne
• Une appli Wear OS et une tuile de montre autonomes
• En anglais, ourdou, turc et indonésien, au choix dans les Réglages

SELON VOTRE ÉCOLE JURIDIQUE

Les conventions sunnites et chiites sont toutes deux correctement prises en charge.

• Sunnite : hanafite, chaféite, malikite et hanbalite, avec la règle du Asr propre à chacune
• Chiite : jafarite / duodécimain, y compris la règle correcte du Maghrib
• Méthodes de calcul : Ligue islamique mondiale, ISNA, Oumm al-Qura, Égypte, Karachi, Moonsighting Committee et d'autres
• La méthode est choisie automatiquement selon votre école (Ligue islamique mondiale pour les sunnites, Jafari pour les chiites), et reste modifiable

VOTRE VIE PRIVÉE N'EST PAS LE PRIX À PAYER

La plupart des applications de prière gratuites vivent de la publicité, donc de vos données. Pas celle-ci.

• Aucune publicité, jamais
• Aucun compte, aucune connexion, aucun e-mail
• Compteurs d'usage facultatifs que vous contrôlez dans les Réglages. Aucun rapport d'erreur
• Position approximative uniquement, lue application ouverte, jamais en arrière-plan
• Heures calculées sur votre téléphone : nous ne recevons jamais votre position. Le nom de la ville vient d'Android, qui interroge Google sur la plupart des téléphones
• Sauvegarde cloud désactivée : rien ne peut être copié depuis votre téléphone

Si vous préférez ne pas partager votre position, saisissez un nom de ville : l'application fonctionne pareil.

HONNÊTE SUR CE QU'ELLE EST

SajdaTime est une aide, pas une autorité religieuse. Votre téléphone calcule ces heures à partir de la position du soleil, selon des méthodes de calcul publiées. Elles ne sont fournies par aucune mosquée, aucun savant ni aucune autorité. L'application a été écrite par une seule personne, avec l'aide de l'intelligence artificielle, et elle peut se tromper. Quand SajdaTime et votre mosquée ne sont pas d'accord, suivez votre mosquée. Si une heure ou une direction vous semble inexacte, ou si vous avez un doute, demandez à votre mosquée ou à une personne qualifiée.

Les heures de prière sont calculées avec adhan-java de Batoul Apps, une bibliothèque open source, et comparées à des horaires de référence indépendants avant chaque version. L'application est offerte gratuitement en l'état, sans garantie ni promesse d'exactitude. Là où l'exactitude compte pour vous, ne vous y fiez pas seul.

GRATUIT, ET IL LE RESTERA

Pas de version payante, pas d'abonnement, rien de verrouillé. SajdaTime est offert gratuitement comme une aumône continue pour la Oumma.

Une seule chose est demandée en retour : pensez à moi, à ma famille et à mes parents dans vos invocations. Jazakallah khayr.
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3954 / 4000`:

```
• Pendant le Ramadan : fin du suhoor et iftar pour votre position, compte à rebours, durée du jeûne, horaire complet du Ramadan en PDF, rappel facultatif du suhoor avant le Fajr
```

---

## Hindi (hi-IN)

Search phrases this listing targets: नमाज़ का समय · नमाज का टाइम · क़िबला · नमाज का टाइम टेबल

**Title** `23 / 30`

```
SajdaTime: नमाज़ का समय
```

**Short description** `76 / 80`

```
ऑफ़लाइन नमाज़ का समय और क़िबला कम्पास, सुन्नी और शिया के लिए। कोई खाता नहीं।
```

Why this title: unchanged. "नमाज़ का समय" is the exact top Hindi query. "नमाज का टाइम" (the other common
spelling) now appears in line three of the body. "क़िबला कम्पास" is in the short description. "कोई विज्ञापन" came out of
the short description.

**Full description, release block** `3718 / 4000`

```
SajdaTime नमाज़ का समय और क़िबला कम्पास बताने वाला ऐप है, जो ऑफ़लाइन चलता है, आपके फ़ोन और Wear OS घड़ी पर।
यह आपको बताता है कि नमाज़ का वक़्त कब है और किस दिशा में मुँह करना है। बस इतना ही इसका काम है, बिना विज्ञापन, बिना खाते, और आपकी लोकेशन कभी प्राप्त किए बिना।

नमाज़ का टाइम आपके अपने फ़ोन पर ही निकाला जाता है, इसलिए यह ऐप हवाई जहाज़ में, तहख़ाने में, या बिल्कुल सिग्नल के बिना भी काम करता रहता है।

आपको क्या मिलता है

• पाँचों वक़्त की नमाज़ें और सूर्योदय, आपकी जगह के हिसाब से
• सामान्य तारीख़ के साथ हिजरी तारीख़ (इस्लामी कैलेंडर)
• अगली नमाज़ तक चलती हुई उलटी गिनती
• क़िबला कम्पास, जो चुम्बकीय नहीं बल्कि वास्तविक उत्तर के अनुसार सही किया गया है
• हर नमाज़ के समय सूचना, ऐप बंद होने पर भी
• वैकल्पिक अलार्म मोड, आपके फ़ोन पर मौजूद किसी भी आवाज़ या अज़ान के साथ, फ़ज्र के लिए जगाने भर तेज़
• सूचना पटल पर वैकल्पिक शांत बैज जो अगली नमाज़ दिखाता है
• आज, अगले सात दिन या पूरे महीने की छपने योग्य PDF समय-सारणी
• होम स्क्रीन के दो विजेट: उलटी गिनती के साथ अगली नमाज़, और आज के सभी समय
• रमज़ान में यह सहरी और इफ़्तार का टाइम टेबल भी है: फ़ज्र पर सहरी ख़त्म, मग़रिब पर इफ़्तार
• Wear OS ऐप और वॉच टाइल, जो फ़ोन के साथ या बिना, अपने आप काम करती है
• उजाला और अँधेरा थीम, फ़ोन के अनुसार या अपनी पसंद से
• अंग्रेज़ी, उर्दू, तुर्की और इंडोनेशियाई में, सेटिंग्स से चुनें

आपके मसलक के अनुसार

सुन्नी और शिया दोनों की रीतियाँ ठीक तरह से समर्थित हैं, ऊपरी तौर पर नहीं।

• सुन्नी: हनफ़ी, शाफ़ई, मालिकी और हंबली, हर एक के लिए सही अस्र नियम
• शिया: जाफ़री / इसना अशरी, सही मग़रिब नियम सहित
• गणना विधियाँ: मुस्लिम वर्ल्ड लीग, ISNA, उम्म अल-क़ुरा, मिस्री, कराची, दुबई, कुवैत, क़तर, सिंगापुर, तुर्की, मूनसाइटिंग कमेटी और अन्य
• विधि आपकी पसंद, सुन्नी या शिया, के अनुसार अपने आप चुनी जाती है (सुन्नी के लिए मुस्लिम वर्ल्ड लीग, शिया के लिए जाफ़री), और आप उसे बदल सकते हैं
• उच्च अक्षांश नियम, उत्तरी देशों के लिए जहाँ गर्मियों में सूरज पूरी तरह नहीं डूबता
• उम्म अल-क़ुरा विधि के साथ, रमज़ान में उसका इशा समायोजन अपने आप लागू होता है

आपकी निजता इसकी क़ीमत नहीं है

ज़्यादातर मुफ़्त नमाज़ ऐप विज्ञापन से चलते हैं, यानी आपके डेटा से चलते हैं। यह नहीं।

• कभी कोई विज्ञापन नहीं
• कोई खाता नहीं, कोई साइन-इन नहीं, कोई ईमेल पता नहीं
• वैकल्पिक उपयोग गणना, जिसे आप सेटिंग्स में नियंत्रित करते हैं। कोई क्रैश रिपोर्ट नहीं
• केवल अनुमानित लोकेशन, वह भी ऐप खुले होने पर, पृष्ठभूमि में कभी नहीं
• समय आपके फ़ोन पर ही निकाले जाते हैं और हम आपकी लोकेशन कभी प्राप्त नहीं करते। शहर का नाम Android के ज़रिए मिलता है, जो ज़्यादातर फ़ोन पर Google से पूछता है
• क्लाउड बैकअप जानबूझकर बंद है, ताकि आपके फ़ोन से कुछ भी कॉपी न हो सके

अगर आप लोकेशन बिल्कुल साझा नहीं करना चाहते, तो शहर का नाम लिख दीजिए, ऐप बिल्कुल वैसे ही काम करेगा।

जो है, वही कहता है

SajdaTime एक सहायक है, कोई धार्मिक प्राधिकरण नहीं। आपका फ़ोन ये समय सूरज की स्थिति से, प्रकाशित गणना विधियों के आधार पर निकालता है। ये किसी मस्जिद, आलिम या किसी प्राधिकरण की ओर से नहीं दिए गए हैं। यह ऐप एक व्यक्ति ने कृत्रिम बुद्धिमत्ता की मदद से बनाया है और इससे ग़लती हो सकती है। जहाँ SajdaTime और आपकी मस्जिद में अंतर हो, वहाँ अपनी मस्जिद का अनुसरण करें। अगर कोई समय या दिशा ग़लत लगे, या आप निश्चित न हों, तो कृपया उनसे या किसी और योग्य व्यक्ति से पूछ लें।

नमाज़ के समय Batoul Apps की adhan-java लाइब्रेरी से निकाले जाते हैं, जो व्यापक रूप से इस्तेमाल होने वाली ओपन सोर्स लाइब्रेरी है, और हर रिलीज़ से पहले स्वतंत्र संदर्भ सारणियों से मिलाए जाते हैं। फिर भी यह ऐप मुफ़्त और जैसा है वैसा ही दिया जा रहा है, बिना किसी वारंटी और सटीकता के किसी वादे के, इसलिए जहाँ सटीकता आपके लिए ज़रूरी हो, वहाँ केवल इसी पर भरोसा न करें।

मुफ़्त, और मुफ़्त ही रहेगा

कोई सशुल्क संस्करण नहीं, कोई सदस्यता नहीं, कुछ भी बंद नहीं। SajdaTime उम्मत के लिए सदक़ा-ए-जारिया के तौर पर मुफ़्त दिया गया है।

बदले में बस एक गुज़ारिश है: मुझे, मेरे परिवार और मेरे माता-पिता को अपनी दुआओं में याद रखें। जज़ाकल्लाह ख़ैर।
```

**January swap.** Replace the Ramadan bullet with this line, and the whole text measures `3838 / 4000`:

```
• रमज़ान में: आपकी जगह के लिए सहरी का अंत और इफ़्तार, उलटी गिनती, आज के रोज़े की अवधि, कल के समय, देखने या PDF में सहेजने के लिए रमज़ान की पूरी समय-सारणी, और फ़ज्र से पहले सहरी की वैकल्पिक याद दिलाने वाली सूचना
```

---

## Adding these in Play Console

1. Grow, then Store presence, then Store listings, then Manage translations, then Add your own translation language.
2. Pick the locale, paste title, short description and full description from the blocks above.
3. Screenshots and the feature graphic carry over from the default listing. The feature graphic carries the English
   subtitle "Sunni & Shia · No accounts", which is acceptable for every language because it carries no price word.
4. Save, then run the promotability check (reload, search the page for "may not be promoted"). Do it once per language,
   because the Console checks each translation separately.
5. Add translations before submitting a release rather than after, so they ride the same review.
6. Do not accept Play's paid machine-translation offer. These are written.

Order of adding, if done one at a time: Indonesian, Urdu, Turkish, Arabic, Malay, Bengali, French, Hindi. That is the
order of likely payoff from keyword-evidence.md, weighted by Android share and the app's own in-app languages.
