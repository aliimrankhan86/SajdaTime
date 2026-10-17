# Play Store listing copy

Everything Google asks you to type into the Play Console, written out so you can paste it.
Character limits are Google's and are checked below each block.

---

## App name (max 30)

```
SajdaTime: Prayer Times, Qibla
```

`30 / 30` — the ampersand version is one character over, hence the comma.

---

## Short description (max 80)

```
Offline prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.
```

`79 / 80`

> **Changed 3 Oct 2026, to be applied in the Console with the 1.3.0 release.** It used to end
> `No tracking.` The app now offers optional, opt in usage counts (docs/ANALYTICS_PLAN.md), so
> that line would be misleading even though the counts are off by default. `No accounts` replaces
> it: true, short, and a privacy word, which this section shows the checker accepts. `No accounts`
> has not been tested on its own, so use the method at the end of this section (change one phrase,
> save, reload, re-read) and revert to a wording without it if the promotion notice returns. **Live
> since 1.3.0 (4 Oct 2026). Confirmed on the public page on 16 Oct 2026: the short description ends
> "No accounts." and the word "No tracking" appears nowhere.**

### Do not put "Free" or "No ads" back

This used to end `… no accounts, no tracking. Free.` and the Console answered with a
promotability notice:

> "Your app may not be promoted on Google Play because your short description does not meet
> the following guidelines: • Should not use keywords that indicate price or promotion"

That is **advisory, not blocking** — the listing saves and publishes either way. What it
costs is eligibility to be featured or included in Play's curated collections. For an app
with no marketing, no budget and no reviews yet, organic discovery *is* the distribution
model, so the notice is worth more than the word.

It was also the weakest word in the sentence. Play prints **Free** next to the Install
button by itself, and the full description gives it a whole section, so the short
description was buying nothing with its last five characters.

**What replaced it, and why that and not something else.** The freed space went to
*"for Sunni and Shia"*, because it is the only claim in this listing a competitor cannot
easily copy. Most prayer apps treat Shia conventions as an afterthought or omit them; this
one implements the Jafari Maghrib rule and the correct Asr rule per madhab, and a Shia user
previously had no way to learn that from the store page at all.

Rejected, and why:

| Candidate | Why not |
|---|---|
| `… No ads, no accounts, no tracking.` (73) | Safe, but leaves seven characters unused and says nothing a dozen other apps do not |
| `… for Sunni and Shia. Private by design.` (77) | "Private by design" is marketing abstraction. `CLAUDE.md` requires this app to work for someone who has never changed a setting and may not read English well; "no ads, no tracking" is concrete and survives translation, "private by design" does not |
| Adding *Adhan* for search volume | Rejected while the app played only a user-chosen sound. **Since 1.5.0 a built-in adhan ships for Sunni users**, so the word is now accurate; whether to add it is the owner's decision |
| Adding *Namaz* / *Salah* for search volume | ~~Keyword stuffing in an English sentence, and Play flags that separately~~ — **this was later reversed for `namaz` only.** See "Why *namaz* went in after all" below |

`no accounts` was the item dropped from the trust triad — it is the least sharp of the
three and is still in the full description.

**One judgment call worth knowing about.** Naming both traditions is inclusive and true, and
it is a deliberate choice to say it on the front of the listing rather than only inside.
If you would rather the store page not foreground the distinction, dropping `for Sunni and
Shia` is a straight swap and nothing else in the listing depends on it.

### "No ads" is also a promotion keyword — this was measured, not guessed

Removing `Free.` **did not clear the notice.** The listing was saved and the page reloaded
from the server, and the identical bullet came back on a fresh render:

> "Should not use keywords that indicate price or promotion"

So the check is real and re-evaluated, not a stale message left over from the previous text.
Working through what was left — `Offline`, `prayer times`, `Qibla compass`, `Sunni and
Shia`, `No ads`, `no tracking` — only one of those is about money at all. `No ads` describes
how the app is (not) monetised, which is exactly the class of claim the guideline names.
Google does not publish the keyword list, so this is elimination, not a rule anyone can read.

Losing it costs less than it looks like it should. Play renders a **Contains ads** badge on
listings that have ads, and shows nothing on listings that do not. The absence of ads is
therefore already stated by Play itself, in its own UI, in a place users trust more than
they trust the developer's own sentence — the same argument that retired `Free`. The claim
still appears in the full description's second line, where the metadata policy is content
for it to be.

The freed characters were deliberately not spent *at the time*. They were spent later, on
`(namaz)` — see the next section, which is the argument for why that is not the keyword
stuffing this paragraph was guarding against.

**Confirmed cleared.** After saving with `No ads` removed and reloading the page from the
server, the notice is gone — checked by searching the rendered page for both "may not be
promoted" and "price or promotion" and finding neither, on a listing whose header now reads
*Ready to send for review*. So the diagnosis was right: with `Free` and `No ads` both out,
nothing in this sentence reads as price or promotion to Google's checker.

That also settles, by elimination, that **`No tracking` is not a flagged phrase** — it
survived the check untouched. Privacy words are fine; commercial ones are not.

If the notice ever comes back after an edit, the method is the thing to reuse rather than
the answer: change one candidate phrase, save, reload, and re-read. It does not re-evaluate
live, so a notice still on screen after an edit means nothing until the round trip.

### Why *namaz* went in after all

**Play has no keyword field.** It indexes the app name, the short description and the full
description, and nothing else. So a word that is not in one of those three places is a word
this app cannot be found by, and the short description is one of only three chances.

`namaz` is the everyday word for the five daily prayers across South Asia, Turkey, Iran,
Central Asia, Afghanistan and the Balkans — several hundred million Muslims, a large share of
whom type it in Latin script into an English-language Play search. Before this edit, a search
for "namaz time" matched nothing in this listing at all. That is not a marketing loss, it is
a reachability one, and reach is the entire point of a *sadaqah jariyah* app.

The earlier objection was keyword stuffing, and it was a fair objection to the thing it was
aimed at — a bare list of synonyms bolted onto the end of a sentence. A parenthetical gloss
immediately after the English term is a different construction: it reads as a translation
aid, because that is what it is. `CLAUDE.md` requires this listing to work for a user who
does not read English well, and for that reader the gloss is the *most* useful word in the
line, not decoration around it.

Only `namaz` went in. `salah` and `salat` did not, for two reasons: there is no room left at
`79 / 80`, and both already appear in the full description's fourth line — *"Whether you call
them namaz, salah or salat, these are the same five daily prayers"* — which Play indexes
just as it indexes this one. Putting all three here would have bought nothing and cost the
sentence its readability.

**Checked, not assumed:** after saving, the page was reloaded from the server and searched
again for "may not be promoted" and "price or promotion". Neither is present. A language word
is not a promotion word, which was the open question and is now closed.

---

### Proposed 16 Oct 2026 (not live): carry "Wear OS" in the short description

```
Offline prayer times (namaz) and Qibla compass for Sunni and Shia, with Wear OS.
```

`80 / 80`. The research found that a Play search for "prayer times wear os" returns only 13 apps (measured 16 Oct 2026, by a read only
research pass, not re-checked by hand), and SajdaTime has a real watch app, so this is a phrase where a high position is realistic.
"No accounts" moves to line 2 of the full description, which Play also indexes. **Apply it only with the same method as above:** paste, save,
reload the listing page from the server, search the page for "may not be promoted" and "price or promotion", and revert to the live text if a
notice appears. "Wear OS" is a platform name, not a price or promotion word, so none is expected, but "No accounts" was also expected to be safe
and was only confirmed after saving.

## Full description (max 4000)

> **Changed 9 Oct 2026, live since 13 Oct 2026 (approved together with 1.3.2).** The calculation method bullet used to say "chosen automatically for your region". That is wrong: `PrayerEngine.resolveMethod` picks Muslim World League for Sunni and Jafari for Shia from the sect setting, never from the region. The block below is the live text.

> **16 Oct 2026: do not edit this block, it is the Console copy, and two statements in it are no longer true.** "Without sending your
> location anywhere" and "Your coordinates never leave your device" do not hold for the town name label, which Android's geocoder may
> answer over the internet (HANDOVER 5.21). The two Release A blocks below replace them. Until the owner pastes one of those, the live
> listing still carries the old sentences.

```
SajdaTime tells you when to pray and which way to face.
That is all it does, and it does it without ads, without an account, and without sending your location anywhere.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

Whether you call them namaz, salah or salat, these are the same five daily prayers.

WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• The Hijri date alongside the ordinary date, with the Islamic month names built in
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode using any tone or adhan already on your phone, loud enough for Fajr
• An optional quiet badge in the notification shade showing the next prayer
• A printable PDF timetable for today, the next seven days, or the whole month
• During Ramadan it doubles as a sehri and iftar timetable: Fajr ends sehri, Maghrib opens your fast
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand, both checked for readability

BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly, not as an afterthought.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai,
  Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The method is chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia), and you can override it
• A high-latitude rule that gives sensible Fajr and Isha times in northern countries where
  the sun never fully sets in summer
• The Umm al-Qura Ramadan adjustment is applied automatically during Ramadan

YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are funded by advertising, which means you pay with your data.
This one is not paid for at all.

• No adverts, ever
• No accounts, no sign-in, no email address
• Optional usage counts that you control in Settings. No crash reporting
• Approximate location only, read while the app is open, never in the background
• Your coordinates never leave your device, and are never part of the optional usage counts
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location at all, you can type a city name instead, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun using published calculation methods; they are not supplied by any mosque, scholar or authority. The app was written by one person, with the help of artificial intelligence, and it can get things wrong. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever looks off, or you are unsure, please ask them or someone else qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source library, and are checked against independent reference timetables before each release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

`3,770 / 4000`, measured on 16 Oct 2026: the raw public Play page carries this block byte for byte (sha256 beginning `ff4dccf3`).

### Release A (1.4.0): the text to paste, and the January text

Written 16 Oct 2026 by the 1.4.0 build, from `docs/plans/2026-10-widget-ramadan-tracker/WORDING-AND-LISTING.md` section C1 and audit
items L5, L6 and L7. **Nothing here is live and nothing is pasted until the owner decides the rollout date and says go.** The
Ramadan line is held on purpose: the Ramadan pack only shows itself seven days before Ramadan, so its line goes into the listing in
early January as a listing edit with no new bundle. Two blocks, so that nothing has to be edited by hand at paste time.

What changed from the live block, in both: the Hijri bullet loses ", with the Islamic month names built in" (room); a widgets bullet
and a languages bullet are added (audit L7: three languages shipped in 1.3.1 and the listing never said so); the high latitude line
is shortened; the Umm al-Qura line now says it applies only with that method (audit L6, it was true only for Umm al-Qura); the
privacy intro is shortened; and the semicolon in the HONEST paragraph is a full stop (house style, the phrases that
`DisclaimerContentTest` matches are unchanged). The school line the owner approved on 13 Oct is not touched.

**Changed 16 Oct 2026 (security and visibility pass, keyword research in HANDOVER 5.22).** Both blocks below: line 1 now says what the
app is in the words people search ("offline prayer times and Qibla compass app for your phone and Wear OS watch"), because Play weights the
first 250 to 300 characters most and the old opening had neither "prayer times" nor "Qibla" in its first 170; the Hijri bullet gains
"(Islamic calendar)" and the alarm bullet "azan (adhan)", both real search spellings; the privacy bullets are corrected to the true claim
(times worked out on the phone, location never received by us, town name looked up through Android); the themes bullet loses "both checked
for readability" and the quiet badge bullet is dropped for room (the badge is still in the app and in Settings). All lengths were measured
by script, newlines counted. The title is unchanged. The HONEST section, the school line approved on 13 Oct and the dua are untouched.

**1. At the 1.4.0 release (3,887 of 4,000)**, the Ramadan bullet is still the live one:

```
SajdaTime is an offline prayer times and Qibla compass app for your phone and Wear OS watch.
It tells you when to pray and which way to face. That is all it does, and it does it without ads, without an account, and without us ever receiving your location.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

Whether you call them namaz, salah or salat, these are the same five daily prayers.

WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• The Hijri date (Islamic calendar) alongside the ordinary date
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode using any tone or azan (adhan) already on your phone, loud enough for Fajr
• A printable PDF timetable for today, the next seven days, or the whole month
• Two home screen widgets: the next prayer with a countdown, and all of today's times
• During Ramadan it doubles as a sehri and iftar timetable: Fajr ends sehri, Maghrib opens your fast
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand
• In English, Arabic, Urdu, Turkish and Indonesian, chosen in Settings

BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly, not as an afterthought.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai, Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The method is chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia), and you can override it
• A high-latitude rule for northern countries where the sun never fully sets in summer
• With the Umm al-Qura method, its Ramadan Isha adjustment is applied automatically

YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are funded by advertising, which means you pay with your data. This one is not.

• No adverts, ever
• No accounts, no sign-in, no email address
• Optional usage counts that you control in Settings. No crash reporting
• Approximate location only, read while the app is open, never in the background
• Times are worked out on your phone and we never receive your location. Your town name is looked up through Android, which on most phones asks Google
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location, you can type a city name instead, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun using published calculation methods. They are not supplied by any mosque, scholar or authority. The app was written by one person, with the help of artificial intelligence, and it can get things wrong. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever looks off, or you are unsure, please ask them or someone else qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source library, and are checked against independent reference timetables before each release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

**2. In early January, for Ramadan 1448 (3,970 of 4,000)**, the same text with the Ramadan bullet replaced by "In Ramadan: suhoor
(sehri) end and iftar for your location, a countdown, today's fast length, tomorrow's times, a timetable to view or save as PDF, and an
optional suhoor reminder before Fajr". "Sehri" stays in brackets because it is the South Asian search word the live line carries:

```
SajdaTime is an offline prayer times and Qibla compass app for your phone and Wear OS watch.
It tells you when to pray and which way to face. That is all it does, and it does it without ads, without an account, and without us ever receiving your location.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

Whether you call them namaz, salah or salat, these are the same five daily prayers.

WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• The Hijri date (Islamic calendar) alongside the ordinary date
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode using any tone or azan (adhan) already on your phone, loud enough for Fajr
• A printable PDF timetable for today, the next seven days, or the whole month
• Two home screen widgets: the next prayer with a countdown, and all of today's times
• In Ramadan: suhoor (sehri) end and iftar for your location, a countdown, today's fast length, a Ramadan timetable to view or save as PDF, and an optional suhoor reminder before Fajr
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand
• In English, Arabic, Urdu, Turkish and Indonesian, chosen in Settings

BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly, not as an afterthought.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai, Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The method is chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia), and you can override it
• A high-latitude rule for northern countries where the sun never fully sets in summer
• With the Umm al-Qura method, its Ramadan Isha adjustment is applied automatically

YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are funded by advertising, which means you pay with your data. This one is not.

• No adverts, ever
• No accounts, no sign-in, no email address
• Optional usage counts that you control in Settings. No crash reporting
• Approximate location only, read while the app is open, never in the background
• Times are worked out on your phone and we never receive your location. Your town name is looked up through Android, which on most phones asks Google
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location, you can type a city name instead, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun using published calculation methods. They are not supplied by any mosque, scholar or authority. The app was written by one person, with the help of artificial intelligence, and it can get things wrong. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever looks off, or you are unsure, please ask them or someone else qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source library, and are checked against independent reference timetables before each release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

**What's new** (Play's limit is 500 characters per language, confirmed by two sources in the 16 Oct 2026 keyword research, still worth a glance
at the Console's own counter). Revised 16 Oct 2026 to match the owner's decision that Ramadan stays out of the listing until January: the
Ramadan card only shows itself seven days before Ramadan, so describing it in October would confuse anyone who installed to look for it.

At the 1.4.0 release, no Ramadan line (188 characters):

```
Two home screen widgets: the next prayer with a countdown, and all of today's times. Add them from your launcher's widget picker. Where your mosque's timetable differs, follow your mosque.
```

In early January, as a listing edit if no bundle ships, or as the release note of whatever bundle does (306 characters):

```
Ramadan is built in: suhoor (sehri) end and iftar for your location, the length of the fast, a Ramadan timetable to view or save as a PDF, and an optional suhoor reminder before Fajr. Two home screen widgets show the next prayer and today's times. Where your mosque's timetable differs, follow your mosque.
```

**At the 1.5.0 release (January 2027).** 1.5.0 is the bundle that ships in January, so its release note is the January Ramadan
text above plus one line for the built-in adhan and "Alarm always plays out loud" (owner, 17 Oct 2026; the second matters because people who relied on the removed silent switch will now hear their alarms), in every language. Counted with Python's `len`, newline
included; all five are under 500. The Urdu, Turkish, Indonesian and Arabic Ramadan sentences are new translations of the short
English text above, because the C4 versions are too long to share the 500 with the adhan line. The adhan line says "Sunni" and
"Shia" with the app's own words for them. If the 1.5.0 note also has to mention Arabic or the new look, something here has to give,
and that is the owner's choice.

1.5.0, English (468):

```
Ramadan is built in: suhoor (sehri) end and iftar for your location, the length of the fast, a Ramadan timetable to view or save as a PDF, and an optional suhoor reminder before Fajr. Two home screen widgets show the next prayer and today's times. Where your mosque's timetable differs, follow your mosque.

Built-in adhan for Sunni users. Alarm now always plays out loud; choose Notification to keep a prayer quiet. Shia users can pick any adhan saved on their phone.
```

1.5.0, Urdu (446):

```
رمضان ایپ میں شامل ہے: آپ کے مقام کے لیے سحری کا اختتام اور افطار، روزے کا دورانیہ، دیکھنے یا PDF میں محفوظ کرنے کے لیے رمضان کا نقشہ، اور فجر سے پہلے سحری کی اختیاری یاددہانی۔ ہوم اسکرین کے دو ویجٹ اگلی نماز اور آج کے اوقات دکھاتے ہیں۔ جہاں آپ کی مسجد کا نقشہ مختلف ہو، اپنی مسجد کی پیروی کریں۔

سنی صارفین کے لیے ایپ میں اذان۔ الارم اب ہمیشہ اونچی آواز میں بجتا ہے؛ خاموشی کے لیے اطلاع چنیں۔ شیعہ صارفین فون میں محفوظ کوئی بھی اذان چن سکتے ہیں۔
```

1.5.0, Turkish (483):

```
Ramazan uygulamada: bulunduğunuz yer için imsak ve iftar, orucun süresi, görüntülenecek veya PDF olarak kaydedilecek imsakiye ve sabah namazından önce isteğe bağlı sahur hatırlatıcısı. İki ana ekran widget'ı sonraki namazı ve bugünün vakitlerini gösterir. Caminizin imsakiyesi farklıysa caminize uyun.

Sünni kullanıcılar için yerleşik ezan. Alarm artık her zaman yüksek sesle çalar; sessiz kalması için Bildirim seçin. Şii kullanıcılar telefonlarındaki herhangi bir ezanı seçebilir.
```

1.5.0, Indonesian (469):

```
Ramadan kini tersedia: akhir sahur dan buka puasa untuk lokasi Anda, lama puasa, jadwal Ramadan untuk dilihat atau disimpan sebagai PDF, dan pengingat sahur opsional sebelum Subuh. Dua widget layar utama menampilkan salat berikutnya dan waktu hari ini. Jika jadwal masjid Anda berbeda, ikuti masjid Anda.

Azan bawaan untuk pengguna Sunni. Alarm kini selalu berbunyi keras; pilih Notifikasi agar tetap senyap. Pengguna Syiah dapat memilih azan apa pun di ponsel mereka.
```

1.5.0, Arabic (406):

```
رمضان في التطبيق: نهاية السحور وموعد الإفطار لموقعك، ومدة الصيام، وإمساكية رمضان للعرض أو الحفظ بصيغة PDF، وتذكير اختياري بالسحور قبل الفجر. ويعرض ودجتان على الشاشة الرئيسية الصلاة التالية ومواقيت اليوم. وإذا اختلفت مواقيت مسجدك، فاتّبع مسجدك.

أذان مدمج لمستخدمي المذهب السنّي. المنبّه يرنّ الآن دائمًا بصوت عالٍ؛ اختر الإشعار للتنبيه الصامت. ويمكن لمستخدمي المذهب الشيعي اختيار أي أذان محفوظ على هواتفهم.
```

The four language versions, written for the same release, are in `WORDING-AND-LISTING.md` C4. Only the English listing exists in
Play today.

**Screenshots for this release.** The release script (`docs/release-tools/release-1.4.0.sh`, step 6) writes candidate captures of the
Times screen on the eve, on day 1, on the last day and on both Eid days in four languages, and of both widgets, to
`build/release-1.4.0/`. When the Ramadan lines go live in January, retake screenshot `03` as the Ramadan Times screen (audit L14), and
add a widget screenshot if the owner wants one.

### This block is now a copy of the Console, not the other way round

The two had **drifted**, and the Console was ahead. It was caught by hashing the live
textarea and comparing it with this file, not by reading them side by side — at three
thousand characters, eyes will not find a missing comma. Four real differences existed, and
this file was wrong about all four, so it has been overwritten with the live text verbatim.

| This file used to say | The Console actually says |
|---|---|
| "…are paid for by advertising, which means paid for by your data." | "…are funded by advertising, which means your data pay for them." |
| "…a city name instead and the app works exactly the same." | "…a city name instead, and the app works the same." |
| "…of artificial intelligence and it may get things wrong." | "…of artificial intelligence, and it may get things wrong." |
| "…my parents in your duas." | "…my parents in your duas. **JazakAllah Kher.**" |

The character count in this file was stale too — it claimed `2,977` when the live text was
`3,019`. Treat a count written by hand as decoration.

**Verify this file against the Console before trusting it**, rather than pasting from it and
assuming. A store listing that only exists correctly in a web form is one accidental Discard
away from being lost.

**The check that is actually used**, because "read them side by side" does not work at three
thousand characters and this file has now proved that twice. The browser is asked for the
textarea's length *and* a djb2-xor hash of it; the text is then pulled out in fixed 800-character
slices, reassembled here, and the same two numbers recomputed. If both match, the fenced block
above is the Console text byte for byte and not a careful-looking approximation of it. The
slice boundaries are asserted at exactly 800 too, so a dropped or doubled chunk fails loudly
rather than quietly shifting the text. Last run: **`len=3704 hash=441829862`, both sides, 1 Sept 2026** — computed in the browser against the Console textarea and on disk against this file. That figure is for the text of 1 Sept. The live text changed on 13 Oct 2026 and is now 3,770 characters (sha256 beginning `ff4dccf3`, measured against the raw public page on 16 Oct 2026). Record a new `len` and hash from the Console the next time the listing is edited.

> **1 Sept 2026: the drift had reversed, and this section's own claim above was stale.** The Console was on `len=3299` and *this file* was right, not the other way round. The 3,299-character version was missing both halves of the strengthened religious disclaimer written on 2 Aug in `91ae9c6` — the sentence saying the times come from a calculation and not from any mosque or scholar, the instruction to follow your mosque where the two disagree, and the no-warranty sentence. `91ae9c6` names four copies that must move together (in-app dialog, watch, `docs/privacy.html`, Play listing) and only three of them moved. The Console text was corrected to match this file, by replacing those two paragraphs in place so the remaining 3,299 characters stayed byte-identical, and both sides were re-measured. **So the rule is not "the Console is always ahead" — it is that neither side is authoritative until they are hashed against each other.**

**The *"your data pay for them"* phrasing is resolved.** It read as a typo — *data* as a plural
noun is defensible but not what most readers expect, and `CLAUDE.md` requires this listing to
work for someone who does not read English well. It is now:

> Most free prayer apps are funded by advertising, which means you pay with your data.

Same length, and it fixes more than the grammar: the old sentence made the reader a bystander
watching their data change hands, and this one puts them in it. The point was never that data
is valuable — it is that these apps are not free and the reader is the one paying.

---

## Category and tags

| Field | Value |
|---|---|
| App or game | App |
| App category | Lifestyle |
| Tags | Clock, alarm & timer · Lifestyle · Maps & navigation |
| Contains ads | **No** |
| In-app purchases | **No** |
| Content rating | Everyone / PEGI 3 (answer the questionnaire honestly, nothing applies) |
| Target audience | 13+ (not directed at children) |

### Three tags, not five, and none of the obvious ones

> **This table used to read `Prayer, Islam, Religion, Compass, Offline`. Not one of those five
> tags exists.** They were written from intuition instead of from the picker. The list is
> closed — Play offers a fixed set, you choose from it, and you cannot type your own.

The full set was enumerated from the dialog: **171 tags, and there is no religion tag and no
religion category.** The nearest thing in the entire list is `Religious text`, which belongs
to Books & reference and would describe a Qur'an app, not this one. Lifestyle is where prayer
apps actually sit, so that part was right by luck rather than by checking.

**Tags are not search keywords.** The dialog says so in its own words: they affect how the app
is grouped, placed and compared against its peers, not what it matches when someone searches.
That is why the reach work went into the short description — the field that *is* indexed — and
why it would be a mistake to treat this table as a second attempt at the same job.

Five slots are allowed and three are used, deliberately. Because a tag decides which apps this
one is measured against, a loose tag does not add a little reach — it drags the app into a peer
group it will compare badly in. The three chosen each describe something the app genuinely is:

| Tag | Why |
|---|---|
| `Clock, alarm & timer` | Times, countdown, notifications and alarm mode — the largest part of what it does |
| `Lifestyle` | Matches the category, which is where prayer apps live |
| `Maps & navigation` | The Qibla compass, which is real navigation and true-north corrected |

**One Console behaviour worth knowing:** the tag dialog's **Apply** button saves immediately to
the server. There is no second confirmation, and the page's own Save button is not involved.
Anything changed in that dialog is live the moment it closes.

---

## Privacy policy URL

```
https://aliimrankhan86.github.io/SajdaTime/privacy.html
```

Live once GitHub Pages is switched on — see `docs/RELEASING.md`, Step 4.

---

## Data safety form answers

Google asks these one at a time.

> **This section was previously wrong and has been corrected.** It used to say answer **No**
> to the collection question. That is a misdeclaration, and misdeclaration is one of the
> most common causes of an app being pulled after it goes live.

**Why it is Yes.** Google's [Data safety
page](https://support.google.com/googleplay/android-developer/answer/10787469) defines the
term narrowly and mechanically:

> "'Collect' means transmitting data from your app off a user's device."

When a user types a city name, that name leaves the device. That is collection by Google's
definition, regardless of the fact that nothing is stored and there is no server of ours for
it to be stored on.

**The good news is that declaring it honestly costs you nothing publicly.** The same page:

> "User data transmitted off device that is processed ephemerally needs to be included in
> your form response, but if it meets the standard below, it will **not** be disclosed in
> your app's Data safety section on Google Play."

So the store listing still shows a clean privacy card — you get there by declaring it, not by
staying silent about it.

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Which data type? | **Location → Approximate location** (Google's own definition names "the city a user is in") |
| Collected? | **Yes** |
| Shared? | **No** — Open-Meteo processes the query on our behalf and only because the user typed it, which is Google's "service provider" and "user-initiated action" carve-out. If you are ever unsure, answering Yes is the safe direction. |
| Is this data processed ephemerally? | **No — do not tick this.** See below. |

### Why not to tick "processed ephemerally", even though it is tempting

Ticking it hides the entry from the public privacy card, so it looks like the obvious answer.
But Google's standard is that the data *"is only stored in memory and retained for no longer
than necessary to service the specific request in real-time"* — and Open-Meteo's own
[terms](https://open-meteo.com/en/terms) say:

> "All log files will be deleted after a period of 90 days."

So the request is logged for 90 days at the other end. Our handling is genuinely ephemeral;
the round trip is not. Google has never resolved whose retention the standard refers to, and
guessing in our own favour on an unresolved ambiguity is exactly the shape of thing that gets
an app pulled later.

**The cost of declaring it honestly is small.** The store card gains one line — "Approximate
location · App functionality · Optional" — on an app whose whole pitch is privacy. That is a
fair trade for a declaration nobody can challenge, and the privacy policy explains it in
plain words. Over-declaring is never a violation. Under-declaring is.
| Purpose | **App functionality** for the city lookup. **Analytics** is added for the usage counts (see the table below). Never advertising or personalisation. |
| Is it required, or can users choose? | **Users can choose** — the app works from device location, or from a typed city, or not at all |
| Is all user data encrypted in transit? | **Yes** — HTTPS |
| Can users request deletion? | **No / not applicable** — nothing is retained to delete |
| Crash logs and diagnostics | **None.** No Crashlytics. Leave those boxes unticked. |
| Device or other IDs | **Yes**, for the optional usage counts only. See the table below. |

### Optional usage counts (added 3 Oct 2026, for release 1.3.0)

Every row below was read from Google's own pages on 3 Oct 2026 (Firebase "Data disclosure" and
the Data safety help page, which also says the form can be exported and imported as a CSV). The
declaration must match the app and `docs/privacy.html` exactly.

| Data type | Why it is declared | Collected | Shared | Optional | Purpose |
|---|---|---|---|---|---|
| **Location → Approximate location** | Google derives country and often city from the connection (Google's help page says inferred location "such as via IP address" must be disclosed). Adds **Analytics** to the existing App functionality purpose | Yes | No | Yes | App functionality, **Analytics** |
| **Device or other IDs** | The random Firebase installation ID | Yes | No | Yes | **Analytics** |
| **App activity → App interactions** | Sessions, time open, which main screen, which setup step, permission yes or no, how you found the app (install referrer) | Yes | No | Yes | **Analytics** |

- **Shared: No.** Google says an analytics provider that processes data "solely on your behalf"
  is a service provider, which is collection, not sharing. That holds only while the console
  data sharing settings and Google signals stay off (privacy.html says they are).
- **Optional: Yes.** Google's help says Optional includes the ability to opt in or opt out for all users; counts are opt in everywhere, so every user can opt in or out (checked 4 Oct 2026). The "Send feedback" row opens the user's own email app and collects nothing, so it adds nothing to Data safety.
- **Ephemeral: No.** Not in memory only.
- **Encrypted in transit: Yes.**
- **Users can request deletion: No.** The ID is random and cannot be matched to a person, and
  resetting it destroys the only handle. privacy.html says so plainly. Do not tick "deletable on request".
- The SDK also notes the time of first launch on the device before any choice and sends it only after a Yes. That is the same "App interactions" category and the same Collected / Optional answers; privacy.html says it in "Before you choose". Do not describe the app as recording nothing before consent.
- "App info and performance" is **not** on Google's list for Analytics, so it is not ticked.

Never claim nothing at all touches the network — the privacy policy already says it does, and
the two must agree.

---

## Advertising ID declaration

**Answer: No.** There are no ads. The Firebase Analytics library is present (optional usage
counts) but the advertising ID is not collected (`google_analytics_adid_collection_enabled` is
false) and the three advertising permissions the library adds are removed in the manifest with
`tools:node="remove"`.

Before you answer, confirm nothing pulled the permission in transitively — a Play Services
library can merge `AD_ID` into the manifest without you asking. A mismatch between this
answer and the merged manifest is a Console error. Run this against the **release** build:

```bash
grep -r -E "AD_ID|ADSERVICES" app/build/intermediates/merged_manifests/release 2>/dev/null || echo "clean — answer No"
```

Checked on 3 Oct 2026 against the merged release manifest with the Firebase library added:
no matches. `UsageCountsTest` also fails the build if the three removals disappear from the
manifest source.

---

## The other App content declarations

Work down **Policy → App content** in this order. All of these gate a closed test, not just
production.

| Form | Answer |
|---|---|
| Privacy policy | `https://aliimrankhan86.github.io/SajdaTime/privacy.html` — must be a live web page, not a PDF |
| App access | **All functionality is available without special access** — there is no login |
| Ads | **No** |
| Content ratings | Complete the IARC questionnaire. Unrated apps have been prohibited on Play since July 2026, so this is not optional. |
| Target audience and content | **13+**. Not directed at children — ticking any under-13 bracket pulls you into Families policy and a great deal of extra work. |
| News apps | **No** |
| COVID-19 contact tracing and status | **No** (this section still exists) |
| Data safety | See above |
| Government apps | **No** |
| Financial features | **My app doesn't provide any financial features** |
| Health apps | No health features |
| Advertising ID | **No** — see above |

---

## Assets

**One folder per Play Console upload box, under `upload/`.** Full instructions, including
the capture recipe, are in [`upload/README.md`](upload/README.md).

| Folder | Play field | Files | Size |
|---|---|---|---|
| `upload/app-icon/` | App icon | `icon-512.png` | 512 × 512 |
| `upload/feature-graphic/` | Feature graphic | `feature-graphic-1024.png` | 1024 × 500 |
| `upload/phone/` | Phone screenshots | 5, upload all in filename order | 1080 × 1920 |
| `upload/wear-os/` | Wear OS screenshots | `w1-times`, `w2-qibla` | 454 × 454 |
| `upload/tablet-7in/`, `tablet-10in/`, `chromebook/` | those boxes | **empty on purpose** | — |

The three empty folders are a decision, not an omission: there is no tablet layout yet, and
Play's asterisk on those boxes is satisfied by the phone screenshots (its own error text
says *"at least 2 phone **or** tablet screenshots"*). Each folder carries a README saying so.

### Aspect ratio is the trap

Play accepts phone screenshots at **16:9 or 9:16 and nothing in between** — the Console's
own wording. A modern phone is 20:9, so a native capture off a current device is
automatically rejected.

> **This was previously wrong.** These notes used to say the rule was "the long side may not
> exceed twice the short side". That is a laxer rule than Play actually applies. The
> captures came off a Pixel 7 AVD at 1080 × 2400 and were scaled down onto a 9:16 canvas and
> pillarboxed in brand green. It complied, but it discarded a fifth of the frame and wrapped
> a green surround around an app that never draws one.

They are now captured **natively at 1080 × 1920** on a Pixel 2 profile AVD and copied
through untouched — no scaling, no reframing, no invented pixels.

### What the five phone screenshots show

Chosen so no two show the same screen. The previous set spent two of its five slots on the
same Times screen in light and dark, and two more on the same Qibla screen.

| File | Screen | Why it earns a slot |
|---|---|---|
| `01-times-light.png` | Times, light | The core screen, with the hero gradient |
| `02-qibla.png` | Qibla | The second core feature, with a live heading |
| `03-times-dark.png` | Times, dark, scrolled | Dark theme, the full six times, and the PDF button |
| `04-school-of-thought.png` | Madhab picker | Evidence for the "built for your school of thought" claim |
| `05-settings.png` | Settings | Calculation method, theme, per-prayer reminders |

> **This was wrong, and is corrected here rather than quietly dropped.** It used to say the Times
list does not fit on one screen at 9:16 and that `03` is therefore scrolled. It is not scrolled:
`01` and `03` are the same view, same scroll position, in the two themes, and both already show
all six times and the PDF button. So the carousel does spend two of its five slots on one screen,
which is exactly the fault the paragraph above says the previous set had. It is not worth a
retake on its own — the screenshots need an emulator, and the dark theme is a real thing to show
— but if the set is ever recaptured, `03` should be genuinely different: scrolled, or the PDF
export sheet, or the Ramadan sehri/iftar framing.

### Colours come from the design system

`tools/build-store-assets.sh` draws the icon and feature graphic from
`ui/theme/Color.kt` values, and the mihrab/minaret paths from `ic_launcher_foreground.xml`,
so the store art cannot drift from the app by hand.

> **It had already drifted.** The icon and both graphics sat on `#14624B`, the primary from
> before the light palette was revised for contrast, so the launcher icon and every store
> graphic were a revision behind the app. `Color.kt` records the same drift on
> `DarkInversePrimary`. Corrected to `#0E6B4F` (`LightPrimary`); the sand mark stays
> `#F0D69A`, which is 4.57:1 on that green and still clears WCAG AA for text.

The feature graphic is now the app's own hero gradient (`LightHeroStart/Middle/End`) with
`LightOnHero` ink, rather than a flat green slab invented for the listing. Worst contrast
pair on it is 4.99:1.

### The feature graphic carries no price words either

Its subtitle used to read `Free · No ads · No accounts · Works offline`. The Console flagged
**only the short description** for the price keyword, and said nothing about the banner —
because it validates form fields and cannot read a PNG. Fixing the flagged field alone would
have left the word in the one asset Play uses when it *does* promote an app.

`No ads` then had to go the same way, once the Console proved the guideline was still firing
with `Free` already removed (see the short description section above). The banner was never
flagged and never could be, so this is the second time the same fix had to be applied twice:
once where a validator could see it, once where nothing could.

It read `Sunni & Shia · No accounts · No tracking` until 3 Oct 2026, when `No tracking` came off
because of the optional usage counts (below). It now reads `Sunni & Shia · No accounts`. `Works offline` went for a different
reason — redundancy: the line directly above it already says "calculated on your phone".

Measured after each regeneration, not assumed. The line length changes where it ends in the
gradient, so the background underneath genuinely changes with the text:

| Check | Result |
|---|---|
| Subtitle `#2C4A3C` against the gradient beneath its full run | **7.01:1** — clears AA text (4.5:1) |
| Line width | Runs x359→774 of 1024, 250px right margin, no wrap |
| App icon background still `LightPrimary` | `#0E6B4F` ✅ |

The four-item version measured 6.90:1 and ended at x≈870; three items end earlier, before the
gradient reaches its warmest stop, which is why the number went up rather than down.

Run `./tools/build-store-assets.sh` to regenerate everything under `upload/`.

### What was verified, and what was not

- Prayer times in the captures were cross-checked between the phone and watch builds and
  agree exactly (Asr 5:28 pm, Maghrib 9:07 pm, Isha 11:28 pm for Greater Manchester).
- The Qibla capture is a free end-to-end check of the compass maths: facing 60°, Qibla 118°,
  and the app says "Turn right 58°".
- **Not verified on real hardware.** Every capture is from an emulator. The watch Qibla page
  in particular shows its bearing-from-north state because the Wear AVD has no
  magnetometer — a real state, but not the best version of that screen.
