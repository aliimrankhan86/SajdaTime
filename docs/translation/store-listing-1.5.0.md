> **History, 21 Oct 2026:** the eight 1.5.0 full descriptions were rebuilt that day from the regrouped English (suhoor reminder on by default, five more features named); the back-translations below describe the 19 Oct texts. The current texts are in `docs/store/LISTING-LOCALES.md`, "1.5.0".

# Store listing 1.5.0: back translations, translators' notes and the review (19 Oct 2026)

The paste-ready text is in `docs/store/LISTING-LOCALES.md`, "1.5.0 listings, FINAL". This file is the record of how it was
made, kept so the next person can check a phrase without reading the language. Each translation was written by one agent
from `docs/store/LISTING.md` "1.5.0 listing, FINAL" and a shared brief, then reviewed by another agent that made its own
back translation first. Review outcome: Arabic and Urdu shippable (fixed: the Arabic "اتجاه القبلة" search phrase, "تُقارَن
المواقيت", "تطبيقات مصغّرة", one spelling of "السنة"; the Urdu "اپنی مسجد سے", "تیز الارم", "ہوائی جہاز"); Turkish ready (fixed:
"yerleşik ezan"); Indonesian one must fix ("mohon jangan hanya mengandalkan aplikasi ini", the app's own words, so "do not rely
on it alone" cannot read as "just do not rely on it"), plus "jam tangan Wear OS" and "Kemenag/MUIS"; Malay a more natural
short description and the same disclaimer fix; French four grammar fixes and the heading "EN TOUTE HONNÊTETÉ" (heading only,
the disclaimer text unchanged); Bengali "কোনো" (Bangladeshi standard) throughout, "ফজরের জন্য যথেষ্ট জোরালো" (the draft said
loud enough to wake you, which the English does not), "শুধু অ্যাপ খোলা থাকার সময়"; Hindi "असली उत्तर के हिसाब से". The short
descriptions finally used are the fuller ones (the brief's 15 character margin was withdrawn: Python's count matches Play's).
The back translations below were written before those fixes.

### Arabic (ar), 1.5.0

**Back translation** (plain English, line by line)

Title: SajdaTime: Prayer times (mawaqit as-salah), Qibla

Short description: Prayer times, the adhan and the Qibla without internet (literally "without net"), for Sunnis and Shia, with Wear OS.

Full description:

- SajdaTime gives you prayer times, adhan alerts and a compass for the Qibla direction, for Muslims of the Sunni and the Shia, on an Android phone and a Wear OS watch. No ads, no account, and prayer times work without internet.
- The times of the five prayers are calculated on your own phone, so SajdaTime keeps working on a plane, in the basement, or where there is no coverage at all.
- Heading: WHAT YOU GET
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha wherever you are, with a live countdown to the next prayer
- • The Hijri date (the Islamic calendar) beside today's date
- • A Qibla compass corrected to true north, which vibrates once when you face the Kaaba
- • Alerts for each prayer even while the app is closed: off, a notification, or an alarm loud enough for the Fajr prayer
- • A built-in adhan for Sunnis and for Shia, or any sound on your phone
- • An optional reminder before the prayers you choose, from 5 to 30 minutes
- • Home screen widgets (literally "tools for the home screen"): a big countdown to the next prayer, and today's prayer times
- • Ramadan built into the app: the suhoor and iftar times, a countdown, how long each day's fast is, the Ramadan imsakiyah (the everyday Arabic word for a Ramadan timetable), and an optional suhoor reminder
- • Printable timetables in PDF format: for today, or 7 days, or the month, or Ramadan
- • A Wear OS app and a tile (literally "card") for the watch that work on their own, with your phone or without it
- • English, Arabic, Urdu, Turkish and Indonesian; you choose the language in the app or it follows your phone's language
- • A light look and a dark one. And it works with large text and screen readers
- Heading: DESIGNED FOR YOUR MADHHAB (school)
- • Sunnis: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each of them
- • Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
- • 13 calculation methods: Muslim World League, ISNA, Umm al-Qura, the Egyptian Authority, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia and Singapore), Diyanet (Turkey), the Moonsighting Committee, Jafari and Tehran
- • The method is chosen according to your madhhab (Muslim World League for Sunnis, and Jafari for Shia), and you can change it
- • "Match your mosque": set any prayer to your mosque's board within 30 minutes, or move the Hijri date forward or back by a day or two
- • Sensible Fajr and Isha in the far north in summer
- Heading: YOUR PRIVACY IS NOT THE PRICE
- Most free prayer apps are funded by advertising, meaning you pay the price with your data. As for this app, you do not pay for it with anything.
- • No ads, and nothing for you to buy
- • No accounts, no sign-in, no email
- • Optional usage statistics that you control from Settings, and they stay off unless you agree. And no crash reports
- • Approximate location only, read while the app is open. Your coordinates never reach the developer or the usage statistics
- • The name of your town is looked up through the place search service on your phone itself, which is usually Google. The privacy policy explains exactly what that means
- • Cloud backup is off, so nothing at all is copied to a backup server
- Prefer not to allow location? Type the name of the place you are in instead. It is looked up once over the internet, then the app works the same way.
- Heading: FRANKLY: WHAT THIS APP IS
- The SajdaTime app is a helper, not a religious authority. Your phone calculates these times from the position of the sun using published calculation methods, and no mosque, scholar or authority supplies them. The app is the work of one person, with the help of artificial intelligence, and mistakes can occur in it. Where SajdaTime and your mosque differ, follow your mosque. If a time or a direction ever looks wrong to you, or you are not sure, please ask them or ask another qualified person to advise you.
- Prayer times are calculated with the adhan-java library from Batoul Apps, a widely used open source library, and are checked against independent reference timetables before each release. Even so, the app is offered free and as it is, without any warranty and without any promise of accuracy, so where complete accuracy matters to you, please do not rely on it alone.
- Heading: FREE, AND IT WILL STAY FREE
- No paid version, no subscription, and nothing locked. SajdaTime is offered free as an ongoing charity (sadaqah jariyah) for the Ummah.
- I ask nothing in return except one thing: I ask that you remember me, my family and my parents in your dua. JazakumAllahu khayran (may Allah reward you all with good).

**Notes**:

- **Title kept** from the 16 Oct draft (`30 / 30`), as the brief asks. This is the one place the listing does not leave 15 characters spare. The only title of 15 or fewer is `SajdaTime: قبلة` (15), which drops "مواقيت الصلاة", the top Arabic query, so I do not recommend it. The title has no diacritics and only Basic Multilingual Plane characters, so Python's count is the Console's count and there is no measuring risk to cover. This is the owner's or reviewer's call.
- **Short description rewritten** to follow the 1.5.0 English one: it now carries the adhan (الأذان) and Wear OS, and drops "بلا حساب ولا تسجيل" (no account, no sign-up), which the English short description does not have. Nothing on the drop list had to go. "دون إنترنت" became "بدون نت", the exact words of the researched query "تطبيق مواقيت الصلاة بدون نت". It is informal but very common in Arabic Play titles. The formal "بدون إنترنت" measures 68 with the same punctuation, which breaks the 15 spare rule (65 if both commas and the full stop are removed).
- **Full description rebuilt** from the 1.5.0 English, section by section, so it now has the 13 named methods, Match your mosque, the 5 to 30 minute reminder, the buzz at the Kaaba, the built-in adhan, the full Ramadan bullet (no January swap needed now), and the accessibility line. Claims the 1.4.0 draft made that the 1.5.0 English does not were removed: the quiet notification badge, "never read in the background", "we never receive your location", the high latitude rule wording, and the Umm al-Qura Ramadan Isha rule.
- **Languages bullet** now names Arabic, because 1.5.0 ships the app in Arabic. The old Arabic draft listed only English, Urdu, Turkish and Indonesian.
- **Not carried, deliberately:** "Whether you call them namaz, salah or salat" is an English spelling device. Arabic has one word, الصلاة, so the paragraph says "the times of the five prayers" and keeps every claim. The glosses "(azan)" and "(sehri)" are dropped for the same reason.
- **Local search word added** (allowed by rule 1): the Ramadan timetable is "إمساكية رمضان", the everyday Arabic word.
- **Right to left:** every paragraph and bullet starts with an Arabic letter, so a screen that sets direction from the first strong character still shows it right to left. That is why the disclaimer opens "تطبيق SajdaTime مساعد" ("The SajdaTime app is a helper") rather than with the Latin name.
- **The app's own words** are used: prayer names with the article (الفجر ... العشاء), «طابِق مسجدك» exactly as `settings_adjustments`, إيقاف / إشعار / منبّه as `alert_style_*`, إحصاءات الاستخدام, "خدمة البحث عن الأماكن في هاتفك نفسه", the method names from `core` (رابطة العالم الإسلامي، أم القرى، لجنة رؤية الهلال، ديانت، الجعفري / الاثنا عشري), the disclaimer's opening "مساعد، وليس مرجعًا دينيًا", "وحيث يختلف SajdaTime ومسجدك، فاتبع مسجدك", and the dua wording "أن تذكرني وعائلتي ووالديّ في دعائك" from `disclaimer_body`.
- **Names:** Google stays in Latin, as the app's Arabic strings write it. The old draft's "غوغل" is spelt "جوجل" in Egypt, so Latin avoids picking a region. Android is written "أندرويد". Wear OS, SajdaTime, ISNA, Kemenag/MUIS, adhan-java and Batoul Apps stay Latin.
- **Unsure:** (1) "بطاقة" (card) for a Wear OS tile. Google's Arabic Wear OS interface may use a different word. (2) "أدوات للشاشة الرئيسية" for widgets. Samsung says "الأدوات", and others say "ودجت" or "تطبيقات مصغّرة". (3) "جزاكم الله خيرًا" is plural, the usual courteous form for a general audience, while the body speaks to "you" in the singular. The singular "جزاك الله خيرًا" is equally correct. (4) The heading "مصمَّم وفق مذهبك" uses مذهب for both Sunni/Shia and the four madhhabs. The app uses "الانتماء المذهبي" for the Sunni/Shia choice.

### Urdu (ur), 1.5.0

**Back translation** (plain English, line by line)

Title: SajdaTime: Prayer times (namaz ke auqat), Qibla

Short description: For Sunni and Shia: offline prayer times (auqat-e-namaz), adhan, Qibla and a Wear OS app.

Full description:

- On your Android phone and Wear OS watch, SajdaTime gives Sunni and Shia Muslims prayer times, adhan alerts and a Qibla compass. No ads, no account, and prayer times work offline.
- Whether you say namaz or salah (salat), the times of the five prayers are worked out on your own phone, so SajdaTime keeps working on a plane, in a basement, or even with no signal at all.
- Heading: WHAT YOU GET
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, wherever you are, and a moment by moment countdown to the next prayer
- • The Hijri date (Islamic calendar) alongside today's date
- • A Qibla compass corrected to true north, which vibrates once as soon as you face the Qibla
- • An alert when each prayer's time comes, even if the app is closed: nothing, a notification, or an alarm loud enough for Fajr
- • An adhan included in the app for Sunni and Shia users, or any sound on your phone
- • An optional reminder 5 to 30 minutes before the prayers you choose
- • Home screen widgets: a big countdown to the next prayer, and today's prayer times
- • Ramadan included in the app itself: sehri and iftar times, a countdown, how long each fast is, the Ramadan timetable (Ramadan calendar) and an optional sehri reminder
- • Printable PDF timetable for today, 7 days, the month or Ramadan
- • A Wear OS app and tile for the watch that run on their own, with or without the phone
- • English, Arabic, Urdu, Turkish and Indonesian; choose in the app, or it follows the phone's language
- • Light and dark theme. Works with large text and screen readers
- Heading: ACCORDING TO YOUR MASLAK (school of thought)
- • Sunni: Hanafi, Shafi'i, Maliki and Hanbali, the correct Asr rule for each one
- • Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
- • 13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran
- • The method is chosen according to your maslak (Muslim World League for Sunni, Jafari for Shia), and you can change it
- • "Match your mosque": set any prayer to your mosque's board, within 30 minutes, or move the Islamic date a day or two forward or back
- • Sensible Fajr and Isha times in the far north during summer
- Heading: YOUR PRIVACY IS NOT ITS PRICE
- Most free prayer apps run on advertising, meaning you pay the price with your data. Here no price is taken from you in any form.
- • No ads, nothing to buy
- • No account, no sign-in, no email address
- • Optional usage statistics, which you control in Settings, and which stay off until you say yes. No crash reporting
- • Approximate location only, which is read only while the app is open. Your coordinates never go to the developer, nor into the usage statistics
- • Your town's name is found through your phone's own place search service, which is usually Google. The privacy policy explains exactly what this means
- • Cloud backup is off, so nothing is copied to any backup server
- Don't want to allow location? Write where you are instead. It is looked up online once, then the app works exactly the same.
- Heading: WHAT THIS APP IS, HONESTLY
- Keep in mind that SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun, through published calculation methods, and they are not provided by any mosque, scholar or authority. This app was made by one person with the help of artificial intelligence, and it can contain mistakes. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever does not look right, or you are not sure, please ask them or another qualified person who can guide you.
- Prayer times are worked out with adhan-java by Batoul Apps, a widely used open source library, and are checked against independent reference timetables before every new version. Even so, the app is given free and as it is, without any warranty or any promise of accuracy, so where being exactly right matters to you, please do not rely on it alone.
- Heading: FREE, AND IT WILL STAY FREE
- No paid version, no subscription, nothing under lock and key. SajdaTime is given free as a sadaqah jariyah (ongoing charity) for the Ummah.
- In return I have just one request: please remember me, my family and my parents in your duas. JazakAllah khair.

**Notes**:

- **Title kept** from the 16 Oct draft (`30 / 30`), as the brief asks. This is the one place the listing does not leave 15 characters spare. The only titles of 15 or fewer are `SajdaTime: قبلہ` or `SajdaTime: نماز` (15 each), which drop "نماز کے اوقات", the exact top Urdu query, so I do not recommend them. The title has no combining marks, so Python's count is the Console's count. This is the owner's or reviewer's call.
- **Short description rewritten** to follow the 1.5.0 English one: it now carries اذان and Wear OS, and drops "اکاؤنٹ نہیں" (no account), which the English short description does not have. Nothing on the drop list had to go. It uses "اوقات نماز", the formal form (also the Urdu title of the "Sajda" competitor), so the title and the short description between them carry both "نماز کے اوقات" and "اوقات نماز". "قبلہ نما" (Qibla compass) became "قبلہ" to fit. "Wear OS ایپ" (a Wear OS app) renders "with Wear OS" as a fact the full description states.
- **Search phrases placed:** "نماز کا وقت" is in the alerts bullet ("ہر نماز کا وقت ہونے پر الرٹ"). "قبلہ رخ" is in the compass bullet, where "when you face the Kaaba" is rendered "قبلہ رخ ہوتے ہی" ("as soon as you face the Qibla"), which means the same thing. "حنفی" is in the school section. I added "اوقات نماز" to the search phrase line because the short description now targets it.
- **Full description rebuilt** from the 1.5.0 English, section by section. Claims the 1.4.0 draft made that the 1.5.0 English does not were removed: the "tells you when to pray" opener, the quiet notification badge, "never in the background", "we never receive your location", the high latitude rule wording, and the Umm al-Qura Ramadan Isha rule. The full Ramadan bullet is in, so no January swap is needed.
- **Languages bullet** now names Arabic, because 1.5.0 ships the app in Arabic.
- **Naming line:** "namaz, salah or salat" became "نماز کہیں یا صلوٰۃ". صلوٰۃ is the spelling Urdu readers know from printed mosque timetables ("اوقاتِ صلوٰۃ"). The glosses "(azan)" and "(sehri)" are dropped because Urdu has one word for each (اذان، سحری). Suhoor is "سحری" throughout, the app's word.
- **Local search word added** (allowed by rule 1): the Ramadan timetable is "رمضان کا نقشہ (رمضان کیلنڈر)". نقشہ is the app's word, and "رمضان کیلنڈر" is the everyday Pakistani name. VISIBILITY-KEYWORDS.md has no measured Urdu evidence for it. I am relying on common usage.
- **Right to left:** no paragraph or bullet starts with a Latin word, so a screen that sets direction from the first strong character still shows it right to left. That is why the first sentence opens with "آپ کے اینڈرائیڈ فون اور Wear OS گھڑی پر" (same claims, reordered within the sentence), why the Wear OS bullet opens "گھڑی کے لیے" ("for the watch"), and why the disclaimer opens "یاد رہے کہ" ("keep in mind that"). Those two words add emphasis and do not soften anything. If the reviewer wants the app's exact opening, delete "یاد رہے کہ " (10 characters), and the paragraph then starts with the Latin "SajdaTime".
- **The app's own words** are used: prayer names (فجر، طلوع آفتاب، ظہر، عصر، مغرب، عشاء), «اپنی مسجد سے ملائیں» exactly as `settings_adjustments`, نماز کے الرٹ / اطلاع / الارم, یاددہانی, اسلامی تاریخ, ترتیبات, مسلک (`label_school`), مقام تلاش سروس, استعمال کے اعداد و شمار, اصل شمال, روشن / گہرا تھیم, نظام الاوقات, صدقۂ جاریہ, the method names from `core`, and the disclaimer's opening "SajdaTime ایک مددگار ہے، کوئی دینی اتھارٹی نہیں" word for word.
- **Grammar:** SajdaTime is treated as feminine (ایپ) throughout. The app's own strings mix both genders (for example, `permission_body` uses the masculine "کرتا ہے").
- **Unsure:** (1) "ٹائل" for a Wear OS tile. (2) "لمحہ بہ لمحہ الٹی گنتی" for "live countdown". The old draft's "زندہ گنتی" was a word for word calque. (3) "پیڈ ورژن" for "paid tier" and "تالے میں بند" for "locked away". (4) "بڑے حروف" for "large text". (5) Google is written "گوگل" in Urdu script, as the app's location strings do. The app's consent strings use the Latin "Google". (6) "جزاک اللہ خیر" is kept from the old draft. "جزاک اللہ خیراً" is the fuller form.

### Turkish (tr-TR), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every bullet and heading
of the full description, so a reviewer who cannot read the language can check it)

Short description: Prayer times, adhan and Qibla. For Sunni and Shia, with Wear OS.

Full description:

- (Opening) SajdaTime offers Sunni and Shia Muslims prayer times, adhan time alerts and a Qibla compass on your Android phone and Wear OS watch. No ads, no account, prayer times work without internet.
- (Line 2) Whether you say namaz or salat, the times of the five prayers are worked out on your own phone. So SajdaTime works even on a plane, in a basement or with no signal at all.
- Heading: WHAT IT OFFERS
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha (in the app's Turkish names: Sabah, Güneş, Öğle, İkindi, Akşam, Yatsı) for where you are, with a live countdown to the next prayer
- • The Hijri date (Islamic calendar) beside today's date
- • A Qibla compass corrected to true north that vibrates once when you turn to the Kaaba
- • An alert for each prayer, even when the app is closed: none, a notification, or an alarm loud enough to get you up for Fajr
- • A ready made adhan in the app for Sunni and Shia, or any sound on your phone
- • An optional reminder 5 to 30 minutes before the prayers you choose
- • Home screen widgets: a big countdown to the next prayer, and today's prayer times
- • Ramadan is ready too: imsak (end of suhoor) and iftar times, a countdown, the length of each fast, the Ramadan imsakiye (timetable) and an optional suhoor reminder
- • Printable PDF timetables for today, 7 days, the whole month or Ramadan
- • A Wear OS app and watch tile that work on their own, with the phone or without it
- • Turkish, English, Arabic, Urdu and Indonesian. The language is chosen in the app or follows the phone's language
- • Light and dark theme. Works with large text and screen readers
- Heading: MADE FOR YOUR SCHOOL OF THOUGHT (literally "prepared according to your madhab")
- • Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
- • Shia: Jafari (Ithna Ashariyya), including the correct Maghrib rule
- • 13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egypt, Karachi, Dubai, Kuwait, Qatar, Kemenag and MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran
- • The method is chosen for your school (Muslim World League for Sunni, Jafari for Shia) and can be changed
- • Match your mosque (the app's own Turkish name, "Camiye göre ayarla"): set any prayer to the time on the mosque board, up to 30 minutes, or shift the Hijri date by a day or two
- • Sensible Fajr and Isha times in the far north in summer
- Heading: YOUR PRIVACY IS NOT THE PRICE
- (Paragraph) Most free prayer apps are funded by advertising, which means you pay the price with your data. This one has no price at all.
- • No ads, no purchases
- • No account, no sign in, no email address
- • Optional usage counts that you manage in Settings, off unless you say yes. No crash reports
- • Approximate location only, read while the app is open. Your coordinates go neither to the developer nor to the usage counts
- • Your town's name is found by your phone's own place lookup service (usually Google). The privacy policy explains exactly what that means
- • Cloud backup is off, nothing is copied to a backup server
- (Paragraph) Rather not give location permission? Type where you are. It is looked up once on the internet, then the app works just the same.
- Heading: HONEST ABOUT WHAT IT IS
- (Disclaimer, paragraph 1) SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun, with published calculation methods. No mosque, scholar or authority gives these times. The app was written by one person with the help of artificial intelligence, and it can make mistakes. If SajdaTime and your mosque say different things, follow your mosque. If a time or a direction looks wrong to you, or you are not sure, please ask them or another qualified person who can guide you.
- (Disclaimer, paragraph 2) The times are calculated with adhan-java, Batoul Apps' widely used open source library, and are compared with independent reference timetables before every release. Even so, the app is given free and as it is, without any warranty or promise of accuracy. So if being exact matters to you, please do not rely on the app alone.
- Heading: FREE, AND IT WILL STAY FREE
- (Paragraph) No paid version, no subscription, nothing locked. SajdaTime is given without charge as sadaqah jariyah (ongoing charity) for the Ummah.
- (Paragraph) I ask only one thing in return: please remember me, my family and my parents in your prayers. May Allah be pleased with you (the everyday Turkish form of JazakAllah Kher).

**Notes**

- **Title kept** (`SajdaTime: Namaz Vakitleri`, 26). "Namaz vakitleri" is still the exact top Turkish query. Brief rule 8 asks for 15 spare characters under every limit, which for a 30 character title would leave room for "SajdaTime" and four letters, so I followed the more specific "keep the title" instruction. Flagged for the caller.
- **Short description** (63) follows the English short with one drop. With offline as "İnternetsiz" (the researched query "namaz uygulaması internetsiz") it measures 75: inside 80, but not 15 spare, so offline went first, as rule 2 orders, and ezan, namaz vakitleri, kıble, Sünni ve Şii and Wear OS stay. "İnternetsiz" is still in line 1 of the body. If the caller reads rule 2's "fit in 80" as the only test for this field, the fuller one is (`75 / 80`): `İnternetsiz namaz vakitleri, ezan ve kıble. Sünni ve Şii için, Wear OS ile.`
- **Changed from the old draft:** rewritten from the 1.5.0 English, so the old claims that the English no longer makes are gone (the quiet notification badge, the "Umm al-Qura Ramadan Isha correction" bullet, "never in the background" and "we never receive your location", which rule 6 replaces with the coordinates and place service wording), and the new ones are in (adhan for Sunni and Shia, Qibla buzz, reminder before prayers, Ramadan, Arabic, large text and screen readers, Match your mosque, 13 named methods). The Ramadan bullet is part of the release text now, so there is no January swap. "Hesap yok" left the short description because the English short now carries adhan instead.
- **App terms used** (values-tr and core values-tr): prayer names Sabah, Güneş, Öğle, İkindi, Akşam, Yatsı; "Dünya İslam Birliği" (the old draft said "Müslüman Dünya Birliği"); "Ümmülkura" (old: "Ümmü'l-Kura"); "Caferi (İsna Aşeriyye)"; "Kemenag ve MUIS"; "Diyanet (Türkiye)"; "Camiye göre ayarla"; "kullanım sayımları"; "yer arama hizmeti"; "Hicri tarih"; "imsak", "imsakiye", "sahur hatırlatıcısı"; "sadaka-i câriye"; "dinî bir otorite".
- **Diyanet:** named in the method list only. Nothing about precaution minutes (temkin), because the English full description does not say it. The method list keeps the English order (Diyanet is not moved to the front).
- **Dua** is now first person ("Karşılığında tek bir şey istiyorum"), as rule 5 asks; the old draft was impersonal ("istenen tek şey"). "Allah razı olsun" is the natural Turkish form of JazakAllah Kher.
- **Search phrases:** "ezan vakti" sits in line 1 ("ezan vakti uyarıları"), "internetsiz" in line 1 and the short, "imsakiye" in the Ramadan bullet.
- **Hyphens kept because Turkish spelling requires them:** "e-posta" and "sadaka-i câriye" (both the TDK spelling, and the second is the app's own term), plus the library name "adhan-java".
- **Unsure:** "saat kartı" for the Wear OS tile (the old draft's word; I could not confirm what Google's Turkish Wear OS interface calls tiles). "yok" as the "none" alert choice reads a little bare but mirrors the English list. Line 2 says "namaz ya da salat" only, because "salah" and "salat" are one word in Turkish.

### Indonesian (id-ID), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every bullet and heading
of the full description, so a reviewer who cannot read the language can check it)

Short description: Prayer times, adhan and Qibla direction. Sunni and Shia, Wear OS.

Full description:

- (Opening) SajdaTime provides prayer times, adhan reminders and a Qibla direction compass for Sunni and Shia Muslims on Android phones and Wear OS watches. No ads, no account, and the prayer times work offline.
- (Line 2) Sholat, salat or namaz: its five times are worked out on your own phone, so SajdaTime keeps working on a plane, in a basement, or with no signal at all.
- Heading: WHAT YOU GET
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha (in the app's Indonesian names: Subuh, Terbit, Zuhur, Asar, Magrib, Isya) at your location, with a live countdown to the next prayer
- • The Hijri date (Islamic calendar) beside today's date
- • A Qibla compass referenced to true north that buzzes once when you face the Kaaba
- • A reminder at every prayer time, even when the app is closed: off, a notification, or an alarm loud enough for Fajr
- • A built in adhan (azan) for Sunni and Shia, or any sound on your phone
- • An optional reminder 5 to 30 minutes before the prayers you choose
- • Home screen widgets: a big countdown to the next prayer, and today's timetable
- • Ramadan features: end of suhoor and breaking the fast (iftar), a countdown, how long each fast is, the jadwal imsakiyah (the Indonesian name for the Ramadan timetable), and an optional suhoor reminder
- • Printable PDF prayer timetable: today, 7 days, a month, or Ramadan
- • A Wear OS app and tile that run on their own, with or without the phone
- • Indonesian, English, Arabic, Urdu and Turkish, chosen in the app or following the phone's language
- • Light and dark themes. Supports large text and screen readers
- Heading: SUITED TO YOUR SCHOOL OF THOUGHT (literally "according to your madhab")
- • Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each madhab
- • Shia: Jafari (Ithna Ashariyya), including the correct Maghrib rule
- • 13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egypt, Karachi, Dubai, Kuwait, Qatar, Kemenag and MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran
- • The method is chosen to suit your school (Muslim World League for Sunni, Jafari for Shia) and can be changed
- • Match your mosque (the app's own Indonesian name, "Sesuaikan dengan masjid"): make any time match the mosque board, up to 30 minutes, or shift the Hijri date by a day or two
- (The English bullet "Sensible Fajr and Isha in the far north in summer" is left out here; see Notes.)
- Heading: YOUR PRIVACY IS NOT THE PRICE
- (Paragraph) Most free prayer apps are funded by advertising, so you pay with data. This one is not paid for at all.
- • No ads, no purchases
- • No account, no login, no email address
- • Optional usage data that you control in Settings, off unless you agree. No crash reports
- • Approximate location only, read while the app is open. Your coordinates are never sent to the developer or into the usage data
- • Your town's name is looked up by the phone's own built in place lookup service, usually Google. The privacy policy explains exactly what that means
- • Cloud backup is off, so nothing is copied to a backup server
- (Paragraph) Rather not allow location? Just type your place. It is looked up once online, then the app works just the same.
- Heading: HONEST ABOUT WHAT IT IS
- (Disclaimer, paragraph 1) SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun with published calculation methods. These times are not given by any mosque, scholar or authority. This app was written by one person with the help of artificial intelligence, and it can be wrong. If SajdaTime and your mosque differ, follow your mosque. If a time or a direction looks wrong, or you are unsure, please ask them or someone else qualified to give advice.
- (Disclaimer, paragraph 2) Prayer times are calculated with adhan-java from Batoul Apps, a widely used open source library, and checked against independent reference timetables before every release. Even so, this app is given free and as it is, with no warranty and no promise of accuracy, so if being exact matters to you, please do not rely on it alone.
- Heading: FREE, AND STAYING FREE
- (Paragraph) There is no paid version, no subscription and no locked feature. SajdaTime is given without charge as sedekah jariah (ongoing charity) for the Ummah.
- (Paragraph) I ask only one thing in return: please pray for me, my family and my parents. Jazakallahu khairan.

**Notes**

- **Title kept** (`SajdaTime: Jadwal Sholat`, 24). Same reason as before: "jadwal sholat" is the dominant query. Rule 8's 15 spare cannot apply to a 30 character title without losing the phrase, so the specific "keep the title" instruction wins. Flagged for the caller.
- **Short description** (63): with "offline" the English content measures 84 ("Jadwal sholat offline, adzan dan arah kiblat untuk Sunni dan Syiah, juga di Wear OS."), over 80, so offline was dropped first, as rule 2 orders. Adzan, jadwal sholat, arah kiblat, Sunni dan Syiah and Wear OS stay, so adzan did not have to go. Offline is still in line 1 of the body. To fit 15 spare the sentence is clipped ("Sunni dan Syiah, Wear OS."). If the caller reads rule 2's "fit in 80" as the only test for this field, this one reads more naturally (`76 / 80`): `Jadwal sholat, adzan dan arah kiblat untuk Sunni dan Syiah, juga di Wear OS.`
- **Dropped for length, one bullet:** "Sensible Fajr and Isha in the far north in summer". With it the description measures 4041, over the 4000 limit even before the 15 character margin, and every other line had already been tightened. It matters least at Indonesia's latitude, and the French draft set the precedent. Nothing was added, and the disclaimer, privacy and dua sections are complete. To restore it ("• Subuh dan Isya yang wajar di utara jauh saat musim panas", 58 characters plus a newline), about 60 characters must come out elsewhere.
- **Spelling of the prayer:** the listing keeps "sholat" (the title and the search phrase) while the app itself says "salat". Line 2, which in English names the prayer three ways, names "sholat, salat, atau namaz", so both spellings are present. "Adzan" is the search spelling; the app says "azan", and "Adzan (azan)" mirrors the English "adhan (azan)".
- **App terms used** (values-in and core values-in): prayer names Subuh, Terbit, Zuhur, Asar, Magrib, Isya (the old draft had Ashar and Maghrib); "Liga Muslim Dunia"; "Kemenag dan MUIS (Indonesia, Singapura)"; "Jafari (Itsna Asyariyah)"; "Sesuaikan dengan masjid"; "Pengaturan"; "layanan pencarian tempat bawaan ponsel"; "utara sejati"; "Tanggal Hijriah"; "sedekah jariah" (the app's KBBI spelling, the old draft had "jariyah"); "orang lain yang berwenang memberi nasihat" from the app's disclaimer.
- **Kemenag** appears where the English lists the methods, in the app's own wording. The listing does not claim Kemenag is the default, because the English says Muslim World League is the Sunni default.
- **Jadwal imsakiyah** is used for the Ramadan timetable, as the brief allows. A caution for the caller: an Indonesian imsakiyah usually prints an imsak time about ten minutes before Subuh, and the app adds no such margin (its own Ramadan note says so). So the bullet says "akhir sahur" (end of suhoor, which is the app's "Sahur berakhir"), never "waktu imsak".
- **Changed from the old draft:** rewritten from the 1.5.0 English. Claims the English no longer makes are gone: the quiet notification badge, "never in the background" and "kami tidak pernah menerima lokasi Anda" (rule 6 wording instead); the Ramadan bullet is now part of the release text, so there is no January swap; the dua is first person ("Saya hanya minta"), where the old draft was impersonal. "Mohon doakan saya" (please pray for me) is the natural Indonesian form of "remember me in your duas"; "Jazakallahu khairan" for JazakAllah Kher.
- **Hyphens:** none of our own. Avoided "masing-masing", "waktu-waktu" and "cuma-cuma"; the only hyphens are in the names "Umm al-Qura" and "adhan-java".
- **Unsure:** "tile" is left in English for the Wear OS tile (the old draft did the same; Google's Indonesian interface may say "kartu"). "Bisa offline" in line 1 is everyday app copy rather than formal Indonesian.

### Malay (ms-MY), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every bullet and heading
of the full description, so a reviewer who cannot read the language can check it)

Short description: Prayer times, azan and Qibla direction. Sunni and Shia, Wear OS.

Full description:

- (Opening) SajdaTime gives prayer times, azan alerts and a Qibla direction compass for Sunni and Shia Muslims on Android phones and Wear OS watches. No ads, no account, and the prayer times work offline.
- (Line 2) Solat, salat or namaz: its five times are worked out on your own phone, so SajdaTime keeps working on a plane, underground, or with no signal at all.
- Heading: WHAT THERE IS (what you get)
- • Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha (in Malaysian usage: Subuh, Syuruk, Zohor, Asar, Maghrib, Isyak) at your location, with a countdown to the next prayer
- • The Hijri date (Islamic calendar) beside today's date
- • A Qibla compass guided by true north that buzzes once when you face the Kaaba
- • An alert for each prayer even when the app is closed: none, a notification, or an alarm loud enough for Fajr
- • A built in azan for Sunni and Shia, or any sound on your phone
- • An optional reminder 5 to 30 minutes before the prayers you choose
- • Home screen widgets: a big countdown to the next prayer, and today's times
- • For Ramadan: suhoor and iftar times, a countdown, the length of each fast, a Ramadan timetable and an optional suhoor reminder
- • Prayer timetable PDF for printing: today, 7 days, a month or Ramadan
- • A Wear OS app and tile that work on their own, with or without the phone
- • App languages: English, Arabic, Urdu, Turkish and Indonesian (no Malay), choose in the app or follow the phone
- • Light and dark themes. Supports large text and screen readers
- Heading: FOLLOWING YOUR SCHOOL OF THOUGHT (literally "according to your madhab")
- • Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each one
- • Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
- • 13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egypt, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran
- • The method is chosen according to your school (Muslim World League for Sunni, Jafari for Shia) and can be changed
- • Match your mosque (kept in English, because that is the name a Malay reader sees in the app): align any prayer with the mosque board, up to 30 minutes, or shift the Hijri date by a day or two
- (The English bullet "Sensible Fajr and Isha in the far north in summer" is left out here; see Notes.)
- Heading: YOUR PRIVACY IS NOT THE PRICE
- (Paragraph) Most free prayer apps are funded by advertising, meaning you pay with data. This one is not paid for at all.
- • No ads, no purchases
- • No account, no login, no email address
- • Optional usage counts that you control in Settings, off unless you agree. No crash reports
- • Approximate location only, read while the app is open. Your coordinates are never sent to the developer or the usage counts
- • Your town's name is looked up by your phone's own place service, usually Google. The privacy policy explains it exactly
- • Cloud backup is off, so nothing is copied to a backup server
- (Paragraph) Rather not allow location? Type the name of your place. It is looked up once online, then the app works the same.
- Heading: HONEST ABOUT WHAT IT IS
- (Disclaimer, paragraph 1) SajdaTime is a helper, not a religious authority. These times are worked out by your phone from the position of the sun with published calculation methods, and are not supplied by any mosque, scholar or authority. This app was written by one individual with the help of artificial intelligence, and can get things wrong. If SajdaTime and your mosque differ, follow your mosque. If a time or a direction looks off, or you are not sure, please ask them or someone else qualified to advise you.
- (Disclaimer, paragraph 2) Prayer times are calculated with adhan-java by Batoul Apps, a widely used open source library, and checked against independent reference timetables before every release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so if being exact matters to you, please do not depend on it alone.
- Heading: FREE, AND STAYING FREE
- (Paragraph) No paid version, no subscription and nothing locked. SajdaTime is given free as sedekah jariah (ongoing charity) for the Ummah.
- (Paragraph) I ask only one thing in return: please pray for me, my family and my parents. Jazakallahu khairan.

**Notes**

- **Title kept** (`SajdaTime: Waktu Solat, Kiblat`, 30). It has no spare at all, so it breaks rule 8's 15 spare, as every useful title would. If the caller wants any margin, `SajdaTime: Waktu Solat` (22) keeps the main Malaysian phrase and loses "Kiblat", which stays in the short description as "arah kiblat". I did not change it, because the brief says keep the title and 30 is within Play's limit.
- **Short description** (60): with "luar talian" the English content measures 84, and with the loanword it is exactly 80 (`Waktu solat offline, azan dan arah kiblat untuk Sunni dan Syiah, dengan Wear OS.`, no margin), so offline was dropped first, as rule 2 orders. Azan, waktu solat, arah kiblat, Sunni dan Syiah and Wear OS stay. "Luar talian" is still in line 1 of the body. If the caller reads rule 2's "fit in 80" as the only test for this field, this one reads more naturally (`72 / 80`): `Waktu solat, azan dan arah kiblat untuk Sunni dan Syiah, dengan Wear OS.`
- **No Malay in the app:** the language bullet says plainly that the app is in English, Arabic, Urdu, Turkish and Indonesian, with no Malay. A Malay reader will therefore see the English interface, so the feature is named "Match your mosque" in English and Settings as "Tetapan".
- **Dropped for length, one bullet:** "Sensible Fajr and Isha in the far north in summer". Without it the text is 3983; with it, 4047, over Play's 4000 even before the margin, after every other line had been tightened. It matters least in Malaysia, Brunei and Singapore, and the French draft set the precedent. To restore it ("• Subuh dan Isyak yang munasabah di utara jauh pada musim panas", 63 characters plus a newline), about 65 characters must come out elsewhere.
- **Malaysian words:** prayer names in JAKIM usage (Subuh, Syuruk, Zohor, Asar, Maghrib, Isyak); "Syafie" (Malaysian spelling); "luar talian" for offline in line 1; "kiraan detik" for countdown; "penggera" for alarm; "ranap" for crash; "sedekah jariah" and "ummah". The search phrase "jadual waktu solat" is in the PDF bullet.
- **Not added:** JAKIM. Malaysia follows JAKIM, but it is not one of the 13 methods, so the listing only names what the English names (Kemenag/MUIS for Indonesia and Singapore).
- **Ramadan:** I avoided "imsak" here. JAKIM's imsak is ten minutes before Subuh and the app adds no such margin, so "jadual imsak" would promise a column the app does not have. The bullet says "jadual Ramadan" and "waktu sahur dan berbuka". The old draft's "jadual imsak" is dropped for that reason.
- **Changed from the old draft:** rewritten from the 1.5.0 English (adhan, Qibla buzz, reminder before prayers, Ramadan, Arabic, Match your mosque, 13 named methods, large text and screen readers); claims the English no longer makes are gone (the quiet notification badge, "never in the background", "kami tidak pernah menerima lokasi anda"; rule 6 wording instead); there is no January swap; the dua is first person ("Saya hanya minta"), where the old draft was impersonal. "Sudilah doakan saya" (please pray for me) is the natural Malay form of "remember me in your duas"; "Jazakallahu khairan" for JazakAllah Kher.
- **Hyphens:** "e-mel" kept (the DBP spelling); "Umm al-Qura" and "adhan-java" are names. Avoided "mana-mana", "semata-mata" and "apa-apa".
- **Unsure:** "tile" left in English for the Wear OS tile (Google's Malay interface may say "jubin"). "Isyak" and "Syuruk" follow JAKIM, but the English app a Malay reader opens will say Isha and Sunrise. "Langsung tidak dibayar" (not paid for at all) is natural but informal.

### Bengali (bn-BD), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every heading, bullet and paragraph of the full description)

Short description: "Offline prayer times, adhan, Qibla compass, Sunni and Shia, Wear OS"

Full description:

1. "SajdaTime brings prayer timetables, adhan alerts and a Qibla compass for Sunni and Shia Muslims, to your Android phone and Wear OS watch. No ads, no account needed, and prayer times are available offline too."
2. "Namaz, salah or salat, whatever name you call it by, the times of the five waqt are calculated on your own phone. So SajdaTime keeps working on a plane, in a basement, or with no network at all."
3. Heading: "What you will get"
   - "Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, wherever you are, with a live countdown to the next prayer"
   - "The Hijri date (Islamic calendar) beside today's date"
   - "A Qibla compass corrected to true north; it vibrates once when you face the Kaaba"
   - "An alert for each prayer time, even when the app is closed: nothing, a notification, or an alarm loud enough to wake you for Fajr"
   - "A built-in adhan (second common Bengali spelling in brackets) for Sunnis and Shias, or any sound on the phone"
   - "An optional reminder 5 to 30 minutes before the prayers you choose"
   - "Home screen widgets: a big countdown to the next prayer, and today's prayer times"
   - "Ramadan is in the app too: suhoor (sehri) and iftar times, a countdown, how long each fast is, a Ramadan timetable and an optional suhoor reminder"
   - "Prayer timetables for today, 7 days, the whole month or Ramadan, as a printable PDF"
   - "A Wear OS app and tile that run on their own, whether or not the phone is with you"
   - "The app itself is in English, Arabic, Urdu, Turkish and Indonesian, not in Bengali. Choose the language in the app, or let it follow the phone's setting"
   - "Light and dark themes. Works with large text and screen readers"
4. Heading: "Made according to your madhab"
   - "Sunni: Hanafi, Shafi'i, Maliki and Hanbali, the correct Asr rule for each"
   - "Shia: Jafari / Ithna Ashari, including the correct Maghrib rule"
   - "13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran"
   - "The method is set by your madhab (Muslim World League for Sunni, Jafari for Shia); you can change it if you wish"
   - "Match your mosque: set any prayer to the time on the mosque's board, within 30 minutes, or shift the Islamic date by one or two days"
   - "Sensible Fajr and Isha times even in the far north in summer"
5. Heading: "Its price is not your privacy"
   - Paragraph: "Most free prayer apps run on advertising money, which means you pay with your data. This app has no price at all, in any way."
   - "No ads, nothing to buy"
   - "No account, sign-in or email address needed"
   - "Optional usage counts, under your control in Settings, off unless you agree. No crash reporting"
   - "Approximate location only, read only while the app is open. Your coordinates never go to the developer or into the usage counts"
   - "Your town's name is found by the phone's own place service, usually Google. What exactly that means is explained in the privacy policy"
   - "Cloud backup is off, so nothing is copied to any backup server"
   - Paragraph: "Don't want to give location permission? Then type the name of your place. It is looked up once online, then the app works the same way."
6. Heading: "What it really is, let me say honestly"
   - Paragraph: "SajdaTime is a helper, not a religious authority. Your phone calculates these times from the position of the sun according to published calculation methods. They are not given by any mosque, alem (scholar) or authority. The app was written by one person, with the help of artificial intelligence, and it can have mistakes. If SajdaTime and your mosque do not match, follow your mosque. If a time or a direction ever does not seem right, or you are not sure, please ask at the mosque or someone else qualified to advise."
   - Paragraph: "Prayer times are calculated with adhan-java, made by Batoul Apps, a widely used open source library, and are checked against independent reference timetables before every release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being accurate matters to you, please do not rely on it alone."
7. Heading: "Free, and it will stay free"
   - Paragraph: "There is no paid version, no subscription, nothing locked. SajdaTime is given free for the Ummah as sadaqah jariyah."
   - Paragraph: "My one request in return: please keep me, my family and my mother and father in your duas. JazakAllahu khairan."

**Notes**:

- **Title kept** (`SajdaTime: নামাজের সময়সূচি`, 27). Rule 8's 15 character margin cannot be met by any title that keeps the brand: "SajdaTime: " alone is 11, so a 15 character title leaves 4 characters for the search phrase. I kept the researched title as the brief asks and flag the conflict here. The short and full descriptions do leave 15 or more spare.
- **Short description rewritten** to the 1.5.0 content. Everything fits in 64, so nothing was dropped: offline (অফলাইন), prayer times (নামাজের সময়), adhan (আজান), Qibla compass (কিবলা কম্পাস), Sunni and Shia, Wear OS. "অ্যাকাউন্ট লাগে না" (no account) came out because the English short description no longer says it. I kept "কম্পাস" after কিবলা because "কিবলা কম্পাস" is the top Bengali Qibla search; the English full description has the compass, so it is not a new claim. It is a comma list, which is normal for Bengali Play listings.
- **The app's language is said plainly** in the language bullet: "the app itself is in English, Arabic, Urdu, Turkish and Indonesian, not in Bengali". The old draft only listed the languages and had no Arabic. I wrote "let it follow the phone's setting" rather than "it runs in the phone's language", because on a Bengali phone the app cannot run in Bengali.
- **Local words added for things the English already says** (rule 1): "আজান (আযান)" gives both common spellings of adhan; "সাহরি (সেহরি)" mirrors the English "suhoor (sehri)", and সেহরি is the everyday Bangladeshi form. "সদকায়ে জারিয়া" for "ongoing charity", as in the old draft.
- **Removed from the old draft because 1.5.0 English does not say them**: the quiet badge, the Umm al-Qura Ramadan Isha line, the high latitude "rule" wording (now "sensible Fajr and Isha in the far north in summer"), "never in the background", and "we never receive your location" (replaced by the rule 6 wording: coordinates never go to the developer or the usage counts; town name looked up by the phone's own place service, usually Google; the privacy policy explains).
- **Hyphens avoided**: "Batoul Apps নির্মিত adhan-java" instead of "Batoul Apps-এর", "সাইন ইন", and "মা বাবা" written without its usual hyphen. The only hyphen left is in the library name adhan-java.
- **Prayer names** use the Bangladesh Islamic Foundation spellings ফজর, যোহর, আসর, মাগরিব, এশা. Numbers are in Bengali digits (৫, ৩০, ৭, ১৩), which is what Bangladeshi readers expect in running text.
- **Terms I was unsure of**: "স্থান সেবা" (place service) for the phone's geocoder is plain but not a fixed Android term in Bengali. "কাউন্টডাউন", "রিমাইন্ডার", "নোটিফিকেশন" and "লাইট ও ডার্ক থিম" are English loans, chosen over the formal ক্ষণগণনা/অনুস্মারক/বিজ্ঞপ্তি because they are what people say and see in apps. "৩০ মিনিটের মধ্যে" keeps the English "within 30 minutes" as it stands.
- Lengths are Python len() after Unicode NFC normalisation. NFC stores য় as two code points (য + ়), so if the Console counts precomposed characters the real figure is lower, never higher.

### French (fr-FR), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every heading, bullet and paragraph of the full description)

Short description: "Prayer times, adhan and Qibla, Sunni and Shia, with Wear OS"

Full description:

1. "SajdaTime: prayer times, adhan alerts and a Qibla compass for Sunnis and Shias, on Android phone and Wear OS watch. No ads and no account, and the prayer times work offline."
2. "Namaz, salah or salat: the times of the five prayers are calculated on your phone. So SajdaTime works on a plane, in a cellar or with no network."
3. Heading: "WHAT YOU GET"
   - "Fajr, sunrise, Dhuhr, Asr, Maghrib and Isha wherever you are, and a live countdown to the next prayer"
   - "Hijri date (Islamic calendar) next to today's"
   - "Qibla compass set to true north, which vibrates once when you face the Kaaba"
   - "An alert at each prayer, even with the app closed: none, a notification or an alarm loud enough for Fajr"
   - "Built-in adhan (azan) for Sunnis and Shias, or any sound on the phone"
   - "Optional reminder 5 to 30 minutes before the prayers you choose"
   - "Home screen widgets: a big countdown to the next prayer, and today's times"
   - "Ramadan built in: suhoor (s'hour, the word French Muslims use) and iftar, countdown, length of the fast, Ramadan calendar, optional suhoor reminder"
   - "Printable PDF prayer timetables: the day, 7 days, the month or Ramadan"
   - "Wear OS app and tile that work on their own, with or without your phone"
   - "The app is in English, Arabic, Urdu, Turkish and Indonesian (not in French), as you choose or following the phone"
   - "Light and dark themes. Works with enlarged text and screen readers"
4. Heading: "SUITED TO YOUR SCHOOL"
   - "Sunni: Hanafi, Shafi'i, Maliki, Hanbali, each with its correct Asr rule"
   - "Shia: Jafari / Twelver (the French word for Ithna Ashari), with the correct Maghrib rule"
   - "13 methods: Muslim World League, ISNA, Umm al-Qura, Egypt, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran"
   - "Method set by your school (Muslim World League if Sunni, Jafari if Shia), can be changed"
   - "Follow your mosque: set each prayer to its display (a gap of 30 minutes at most) or shift the Hijri date by one or two days"
5. Heading: "YOUR PRIVACY IS NOT THE PRICE"
   - Paragraph: "Most free prayer apps live on advertising, so on your data. Here, nothing to pay, in any way."
   - "No ads, nothing to buy"
   - "No account, no sign-in, no email address"
   - "Optional usage counts, managed in the settings, off by default. No crash reports"
   - "Approximate location only, read with the app open. Your coordinates never go to the developer or into the counts"
   - "The town's name comes from the phone's location service, usually Google. The privacy policy explains it in detail"
   - "Cloud backup switched off: nothing is copied to a backup server"
   - Paragraph: "No location? Type your place: it is looked up once online, then everything works the same."
6. Heading: "HONEST ABOUT WHAT IT IS"
   - Paragraph: "SajdaTime is a helper, not a religious authority. Your phone calculates these times from the position of the sun, using published methods; no mosque, no scholar and no authority supplies them. The app was written by one single person, helped by artificial intelligence, and it can get things wrong. If SajdaTime and your mosque differ, follow your mosque. If a time or a direction seems wrong to you, or if you are in doubt, please ask your mosque or another person qualified to advise you."
   - Paragraph: "The times are calculated with adhan-java by Batoul Apps, a widely used open source library, and compared before each release against independent reference timetables. Even so, the app is given free and as it is, with no warranty or promise of accuracy: if accuracy matters to you, please do not rely on it alone."
7. Heading: "FREE, AND IT WILL STAY THAT WAY"
   - Paragraph: "No paid version, no subscription, nothing locked. SajdaTime is given as sadaqa jariya (ongoing charity) for the Ummah."
   - Paragraph: "I ask only one thing in return: think of me, my family and my parents in your duas. JazakAllah khayr."

**Notes**:

- **Title kept** (`SajdaTime: Horaires de prière`, 29). As in the other languages, rule 8's 15 character margin cannot be met by a title that keeps "SajdaTime: " plus the search phrase, so I kept the researched title and flag the conflict. French typography would put a space before the colon ("SajdaTime : Horaires de prière" is exactly 30), so the old draft's form without the space stays.
- **Short description: "hors ligne" (offline) dropped**, following the brief's drop order. The full content line, "Heures de prière hors ligne, adhan et Qibla, sunnite et chiite, avec Wear OS", is 76: under 80 but only 4 spare, which breaks rule 8's 15 character margin. With "hors ligne" out it is 65, exactly 15 spare. "Hors ligne" stays in the first paragraph of the full description. If the owner decides the margin need not apply to the short description, the 76 character line above is the fuller choice. "Sans compte" from the old short description is out because the English 1.5.0 short description no longer says it.
- **High latitude bullet dropped again, because it would not fit.** With it ("• Fajr et Isha raisonnables l'été, très au nord") the full description measures 4029, which is over the 3985 the margin allows. Every other English bullet and sentence is kept.
- **How the rest fits.** French runs about 25% longer than English, and the first full translation measured 4963. To fit, the bullets are written in telegraphic store style (no articles), "appli" stands for "application" and "pub" for "publicité" ("sans pub" is also the phrase people search: "application prière islam sans pub"). Shortenings of note: "Whether you call them namaz, salah or salat" became "Namaz, salah ou salat :"; "Jafari / Ithna Ashari" became "jafarite / duodécimain" (the French name for Ithna Ashari); "This one is not paid for at all" became "Ici, rien à payer, d'aucune façon"; "off unless you say yes" became "désactivés par défaut" (off by default); "explains exactly what that means" became "l'explique en détail".
- **HONEST section**: every point of rule 4 is there in order, and none is softened. The only compressions are "ever" (no separate word) and "using published calculation methods" written as "calcule ... selon des méthodes publiées" (the verb already says calculation). I kept the English semicolon (" ; " in French typography) and the full "personne qualifiée pour vous conseiller".
- **Local words added for things the English says** (rule 1): "s'hour", the word French and North African Muslims use for suhoor, mirrors the English "suhoor (sehri)". "sadaqa jariya" replaces "aumône continue" for "ongoing charity" because it is the term French Muslims use. "douas" for duas.
- **Hyphens avoided**: "dans une cave" instead of "au sous-sol", "Umm al Qura" without its hyphen, "adresse mail" instead of "e-mail", "Ici" instead of "Celle-ci". The only hyphen left is in the library name adhan-java.
- **Terms I was unsure of**: "service de localisation" for the phone's place (geocoder) service is the familiar Android phrase but strictly covers more than place lookup. "calez chaque prière sur son affichage" ("set each prayer to its display", where "son" is the mosque's) is compact and slightly elliptical. "lue appli ouverte" (read with the app open) is telegraphic, matching "même appli fermée".

### Hindi (hi-IN), 1.5.0

**Back translation** (plain English, line by line, of the short description and of every heading, bullet and paragraph of the full description)

Short description: "Offline prayer times, adhan and Qibla, Sunni and Shia, Wear OS"

Full description:

1. "SajdaTime gives prayer times, adhan alerts and a Qibla compass for Sunni and Shia Muslims, on your Android phone and Wear OS watch. No ads, no account, and prayer times are available offline."
2. "Whether you say namaz or salat, the time of all five prayers is worked out on your own phone, so SajdaTime keeps working on a plane, in a basement, or without any signal."
3. Heading: "What you get"
   - "Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha, wherever you are, with a live countdown to the next prayer"
   - "The Hijri date (Islamic calendar) along with today's date"
   - "A Qibla compass corrected to true north, which vibrates once as soon as you face the Kaaba"
   - "An alert for each prayer, even when the app is closed: nothing, a notification, or an alarm loud enough for Fajr"
   - "A built-in adhan (azan) for both Sunni and Shia, or any sound on the phone"
   - "An optional reminder 5 to 30 minutes before the prayers you choose"
   - "Home screen widgets: a big countdown to the next prayer, and today's prayer times"
   - "Ramadan in the app itself: sehri and iftar times, a countdown, the length of each fast, a Ramadan timetable and an optional sehri reminder"
   - "A printable PDF prayer timetable for today, 7 days, the whole month or Ramadan"
   - "A Wear OS app and tile that work on their own, with or without the phone"
   - "The app itself is in English, Arabic, Urdu, Turkish and Indonesian, not in Hindi. Choose the language in the app or follow the phone"
   - "Light and dark themes. Works with large text and screen readers"
4. Heading: "Made for your maslak (school of thought)"
   - "Sunni: Hanafi, Shafi'i, Maliki and Hanbali, the correct Asr rule for each"
   - "Shia: Jafari / Ithna Ashari, with the correct Maghrib rule"
   - "13 calculation methods: Muslim World League, ISNA, Umm al-Qura, Egypt, Karachi, Dubai, Kuwait, Qatar, Kemenag/MUIS (Indonesia, Singapore), Diyanet (Turkey), Moonsighting Committee, Jafari and Tehran"
   - "The method is set by your maslak (Muslim World League for Sunni, Jafari for Shia), and you can change it"
   - "Match your mosque: set any prayer to the time on the mosque's board, within 30 minutes, or move the Islamic date one or two days forward or back"
   - "Sensible Fajr and Isha times even in the far north in summer"
5. Heading: "Your privacy is not its price"
   - Paragraph: "Most free prayer apps run on adverts, meaning you pay the price with your data. This one has no price at all, in any form."
   - "No adverts, nothing to buy"
   - "No account, no sign-in, no email address"
   - "Optional usage statistics, under your control in Settings, off without your yes. No crash reporting"
   - "Approximate location only, read only while the app is open. Your coordinates never go to the developer or into the usage statistics"
   - "Your town's name is found by the phone's own place-finding service, usually Google. What exactly this means is explained in the privacy policy"
   - "Cloud backup is off, so nothing is copied to any backup server"
   - Paragraph: "Don't want to allow location? Then type where you are. It is looked up once online, then the app works just the same."
6. Heading: "What it is, honestly"
   - Paragraph: "SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun, using published calculation methods. They are not given by any mosque, aalim (scholar) or authority. This app was written by one person with the help of artificial intelligence, and it can make mistakes. Where SajdaTime and your mosque differ, go by your mosque. If ever a time or a direction does not seem right, or you are not sure, please ask your mosque or someone else qualified to advise."
   - Paragraph: "Prayer times are worked out with adhan-java by Batoul Apps, a widely used open source library, and are checked against independent reference timetables before every release. Even so, this app is given free and as it is, without any warranty and without any promise of accuracy, so where being exactly right matters to you, please do not rely on it alone."
7. Heading: "Free, and it will stay free"
   - Paragraph: "No paid version, no subscription, and nothing locked. SajdaTime is given free for the Ummah as sadaqa jariya."
   - Paragraph: "In return I ask just one thing: please remember me, my family and my mother and father in your duas. JazakAllah khair."

**Notes**:

- **Title kept** (`SajdaTime: नमाज़ का समय`, 23), the exact top Hindi query. As in the other languages, rule 8's 15 character margin cannot be met by a title that keeps "SajdaTime: " plus a search phrase, so I kept it and flag the conflict.
- **Short description rewritten** to the 1.5.0 content. Everything fits in 61, so nothing was dropped: offline (ऑफ़लाइन), prayer times (नमाज़ का समय), adhan (अज़ान), Qibla (किबला), Sunni and Shia, Wear OS. "कोई खाता नहीं" (no account) came out because the English short description no longer says it. "कम्पास" came out to make room; the compass is in the full description.
- **Qibla spelling**: the short description uses "किबला" (no nukta), the form people type and the one in the research ("किबला रुख किधर है"). The full description uses "क़िबला" (with nukta), the careful written form, as the old draft did. This deliberately mirrors the old draft's mix of "नमाज़ का समय" and "नमाज़ का टाइम" so that both forms are indexed. If the owner prefers one spelling throughout, "किबला" is the safer search choice.
- **The app's language is said plainly**: "the app itself is in English, Arabic, Urdu, Turkish and Indonesian, not in Hindi". The old draft listed only four languages and did not say "not in Hindi".
- **"salah" left out of "namaz, salah or salat"**. In Hindi, सलाह means "advice", and the HONEST section uses it in that sense ("सलाह देने के योग्य", "qualified to advise"). Writing "नमाज़, सलाह या सलात" would read as "namaz, advice or salat", so the line says "नमाज़ कहें या सलात".
- **Consistency with the app's own Urdu**: "दीनी अथॉरिटी" (religious authority) and "अपनी मस्जिद से मिलाएँ" (Match your mosque) are the Devanagari forms of the app's Urdu "دینی اتھارٹی" and "اپنی مسجد سے ملائیں". "मसलक" for school of thought. "सहरी" (sehri) for suhoor, the everyday Indian word, used alone because it already is the English gloss.
- **Removed from the old draft because 1.5.0 English does not say them**: the quiet badge, the Umm al-Qura Ramadan Isha line, the high latitude "rule" wording (now "sensible Fajr and Isha in the far north in summer"), "never in the background", and "we never receive your location" (replaced by the rule 6 wording).
- **Hyphens avoided**: "सदक़ा जारिया" (usually सदक़ा-ए-जारिया) and "माँ बाप" (instead of माता-पिता). The only hyphen left is in the library name adhan-java. The author speaks in the masculine first person ("माँगता हूँ").
- **Terms I was unsure of**: "आर्टिफ़िशियल इंटेलिजेंस" rather than the Sanskritic "कृत्रिम बुद्धिमत्ता" (plainer for this audience, but either is correct). "उपयोग आँकड़े" (usage statistics) for "usage counts". "फ़ोन की अपनी जगह खोजने वाली सेवा" (the phone's own place-finding service) for the geocoder. "माँ बाप" is warm and everyday; "वालिदैन" would be the more formal Muslim register.
- Lengths are Python len() after Unicode NFC normalisation. NFC stores nukta letters (क़, ज़, फ़, ख़, ग़) as two code points each, so if the Console counts precomposed letters the real figure is lower, never higher.
