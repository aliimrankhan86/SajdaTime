> **Research of 16 October 2026, written by a read only research pass and kept here so the evidence is not lost.** Search results,
> competitor counts and ranking claims in it were measured on that day from public pages and have not all been re-checked by hand, and
> they go stale. Nothing here is applied unless `docs/HANDOVER.md` 5.22 says so. Anything that only the owner can do (accounts, terms,
> pressing submit or publish) is the owner's. **The location claim in this file was corrected to the true one on the same day:** say
> "the times are worked out on your phone and we never receive your location", never "your location never leaves your phone"
> (`docs/HANDOVER.md` 5.21).

# SajdaTime keyword evidence and competitor snapshot, 16 October 2026

## How to read this

Exact search volumes for Google Play are not published by Google and are only estimated by paid tools (AppTweak,
Sensor Tower, data.ai, Mobile Action). None was used. Three kinds of free evidence were gathered instead, all on
16 Oct 2026, all reproducible with the commands in the appendix:

- **Play search results**, fetched from `play.google.com/store/search` for 44 query and locale pairs. They show who
  ranks for a phrase today, which words the winners put in their titles, and how crowded a phrase is (30 results is
  Play's page cap, fewer means a thin field).
- **Google web search suggestions** (the autocomplete endpoint, per language and country). They show what people
  actually type, including the qualifiers ("without ads", "internetsiz", "tanpa iklan", "بدون نت", "sans pub").
  This is web demand, which overlaps Play demand but is not the same thing: treat it as a signal of phrasing, not of
  Play volume.
- **Competitor listings** on Play (title, short description, install band, rating count, badges), fetched directly.

Where a row says "reasoning", that is this analyst's inference, not data.

## The ten phrases that matter most, in order

| # | Phrase | Market | Where it goes | Why | Evidence |
|---|---|---|---|---|---|
| 1 | prayer times | English, worldwide | Title, short, line 1 | The category's head term. Every page-one app has it in the title. Table stakes, not a win | Play results GB and US: all 12 top titles contain it ([search](https://play.google.com/store/search?q=prayer%20times&c=apps&hl=en_GB&gl=GB)). Google suggestions: "prayer times london/dubai/today" |
| 2 | qibla / qibla compass / qibla direction | English, worldwide | Title, short | Second head term, and many Qibla-only apps rank for it, so a prayer app with a true-north compass is a legitimate match | Play results GB: 12 top titles all carry "Qibla". Suggestions: "qibla finder", "qibla direction", "qibla compass app" |
| 3 | namaz / namaz time | South Asia, Turkey, diaspora (typed in Latin script into English Play) | Short description gloss, line 4 | Everyday word for several hundred million Muslims. Dawat-e-Islami ranks first for it in Pakistan with "Namaz" in its title | Play "namaz time" en/PK: Dawat-e-Islami #1. Suggestions PK: "namaz time", "namaz timing in lahore" |
| 4 | prayer times wear os | English, watch owners | Short description ("with Wear OS") | **Only 13 apps** come back for it. The app has a real watch app and tile. This is the one English phrase where a page-one position is realistic at 100 installs | Play GB: 13 results, top three are tiny Wear-only apps ([search](https://play.google.com/store/search?q=prayer%20times%20wear%20os&c=apps&hl=en_GB&gl=GB)). MAWAQIT and Pray Watch put "Wear OS" and "widget" in their short descriptions |
| 5 | shia prayer times / sunni shia prayer | English, worldwide | Short ("Sunni and Shia"), school section | Thin field: the #1 result for "shia prayer times" has no rating and 1k+ installs. SajdaTime's Jafari support is real and most big apps bury Shia settings | Play GB: top results are small specialist apps ([search](https://play.google.com/store/search?q=shia%20prayer%20times&c=apps&hl=en_GB&gl=GB)). Suggestions: "shia prayer times london/sydney/near me" |
| 6 | offline prayer times / no ads / without ads | English, worldwide | Short ("Offline"), privacy section | Real demand for exactly this app's proposition. Cannot be put in the title or short description as "no ads" (promotion word, flagged by the Console on this listing), so it is carried by "offline" and by the body | Suggestions GB: "prayer times app without ads", "muslim prayer app without ads", "athan app no ads", "prayer app no ads" with eight variants. Play GB "prayer times no ads": Pray Watch ranks #8 with "Free, no ads, no tracking, 100% privacy" in its short description |
| 7 | jadwal sholat / arah kiblat / jadwal imsakiyah | Indonesia | id-ID title, short, Ramadan line | Largest Muslim population (about 220m) and 79% Android (StatCounter, Sept 2026). "Jadwal sholat" is the exact top suggestion and the title form of the top three Play results | Play id/ID: "Jadwal Sholat, Kiblat & Adzan" #1, 5m+ installs, 4.9 ([listing](https://play.google.com/store/apps/details?id=com.andi.waktusholatdankiblat)). Suggestions: "jadwal sholat hari ini", "aplikasi sholat tanpa iklan", "jadwal imsakiyah" |
| 8 | namaz vakitleri / ezan vakti / kıble / internetsiz | Turkey, German-Turkish diaspora | tr-TR title, short | 75m Muslims, 98% of population. The app is already in Turkish, which Play's localisation signals reward | Play tr/TR: nine of twelve top titles are literally "Namaz Vakitleri". Suggestions: "namaz uygulaması internetsiz", "reklamsız namaz vakitleri uygulaması" |
| 9 | نماز کے اوقات / نماز کا وقت / قبلہ رخ | Pakistan, Urdu readers in India and the diaspora | ur title, short | 184m Muslims, 90% Android (StatCounter, Sept 2026). The app is already in Urdu with RTL | Play ur/PK: Dawat-e-Islami "نماز کا وقت - اذان, قبلہ رخ" #1. Suggestions: "نماز کے اوقات", "نماز کا وقت حنفی" |
| 10 | suhoor time / sehri time / iftar time / ramadan timetable | English, South Asia, UK | January block only | Seasonal spike. The "sehri iftar time" field on Play has only 10 results, mostly yearly throwaway apps with "2027" in the name. A maintained app with a real Ramadan card can take that space in February | Play GB "sehri iftar time": 10 results. Suggestions: "ramadan timetable 2027", "iftar time today", "suhoor time today", "sehri time karachi" |

### The next tier (worth carrying, not worth a slot in the short description)

| Phrase | Market | Where | Evidence or reasoning |
|---|---|---|---|
| salah times, salat | English, UK, South Africa | Line 4 of the body | Suggestions GB: "salah times london/leicester/durban" |
| azan / adhan / athan | English, Gulf, South Asia | Alarm bullet, "azan (adhan)" | Play US "azan": Athan, Muslim Pro, Prayer Now, Azan Time Pro. The app does not bundle a recitation, so the bullet says the honest thing: it plays a file already on the phone |
| hijri date, islamic calendar | English, worldwide | Hijri bullet, "(Islamic calendar)" | Suggestions: "hijri date today", "islamic calendar 2026". Play GB "hijri calendar" is dominated by calendar-only apps, so this is a secondary match, not a target |
| prayer times widget | English | Widgets bullet | Play GB: competitive (ZeeDev, zero_, Muslim Pro). Reasoning: carry the word, do not chase it |
| hanafi prayer times | UK, South Asia, diaspora | School section | Suggestions GB: "hanafi prayer times sydney/toronto/london". Reasoning: the madhab names in the body already match |
| waktu solat, arah kiblat | Malaysia, Brunei, Singapore | ms-MY title, short | Play ms/MY: "Waktu Solat Malaysia" (Media Prima, 10m+) and JAKIM apps dominate. Suggestions: "aplikasi waktu solat tanpa iklan" |
| مواقيت الصلاة, أوقات الصلاة, اتجاه القبلة | Arab world | ar title and short | Play ar/SA: Salatuk 50m+ leads. Suggestions SA: "تطبيق مواقيت الصلاة بدون اعلانات", "بدون نت" |
| নামাজের সময়সূচি, কিবলা কম্পাস | Bangladesh | bn-BD title and short | Play bn/BD: Ibadah 1m+, "নামাজের সময়সূচি বাংলাদেশ". Suggestions: "নামাজের সময়সূচি ২০২৬", "কিবলা কম্পাস" |
| horaires de prière, heures de prière, application prière islam sans pub | France, Belgium, Maghreb diaspora | fr-FR title and short | Play fr/FR: MAWAQIT 5m+ leads. Suggestions: "application priere islam sans pub", "application prière islam fiable" |
| नमाज़ का समय, नमाज का टाइम, किबला | India (Hindi readers) | hi-IN title, short, body | Play hi/IN: WeMuslim and Dawat-e-Islami lead. Suggestions: "नमाज का टाइम टेबल 2026", "किबला रुख किधर है" |

### Phrases deliberately not used

| Phrase | Why not |
|---|---|
| free, no ads, ad free, best, #1, new | Price, promotion or ranking words. Google's metadata policy bans them in the title and the Console flagged "Free" and "No ads" in this listing's short description (docs/store/LISTING.md). They stay in the body as facts |
| adhan (bare, in title or short) | Over-promises a bundled recitation |
| prayer tracker, salah tracker | Planned, not shipped. Not to be mentioned anywhere |
| Ramadan, suhoor, iftar (new lines) | Owner's decision: out of the listing until January 2027. The one sehri/iftar bullet that has been live since August stays |
| quran, dua, azkar, tasbih | The app does not have them. Muslim Pro, Athan and Sajda rank on them and SajdaTime cannot honestly match |
| "sajda" as a brand hook | See the brand collision below |

## Brand collision: "Sajda" is somebody else's app

"Sajda: Prayer Times, Quran" by GOOD/APP (`com.raimbekov.android.sajde`) has 10m+ installs and 423,344 ratings at 4.8,
was updated on 16 Oct 2026, and is localised into every one of the eight target languages (its title is
"Sajda: Namaz Vakitleri, Kuran" in Turkey, "Sajda: Jadwal Sholat, Al Quran" in Indonesia, "Sajda: اوقات نماز، قرآن"
in Pakistan). Seven other apps also use "Sajda" or "Sajdah" in their names.

Measured consequences (Play GB, 16 Oct 2026):

- Searching **"sajda"** returns 20 apps. SajdaTime is not among them.
- Searching **"sajda time"** returns SajdaTime at **position 10**, behind eight "Sajda" apps and Muslim Pro.
- Searching **"sajdatime"** (one word) returns exactly one app: SajdaTime.

What follows from that: every link, post, screenshot caption and spoken mention should use the one-word spelling
"SajdaTime", never "Sajda Time", so that brand searches arrive. The title already does this. Do not rename the app:
the package name, the Play URL and the privacy policy URL all carry the brand and a rename would cost more than the
collision does.

## Competitor snapshot (Play, en-GB storefront, fetched 16 Oct 2026)

All in Lifestyle unless noted. "Ads" and "IAP" are Play's own badges on the listing. Ratings are the count shown.

| App (title as listed) | Package | Installs | Rating (count) | Ads | IAP | Short description | What its title relies on |
|---|---|---|---|---|---|---|---|
| Muslim Pro: Quran Prayer Times | com.bitsmedia.android.muslimpro | 100m+ | 3.8 (1,134,720) | yes | yes | Never miss a prayer: Azan Reminders and Qibla direction to support your worship. | Quran, Prayer Times. Title varies by country: "Quran Athan Prayer" in the US, "Namaz Saat, Ezan" in Turkey, "Quran Waktu Solat" in Malaysia, which is custom store listings in action |
| Salatuk (Prayer time) صلاتك | com.masarat.salati | 50m+ | 4.7 (1,527,013) | no | no | Salatuk is your Muslim prayers Companion! | Arabic brand plus "Prayer time" |
| Athan: Prayer Times, Dua-Azkar | com.athan (IslamicFinder) | 10m+ | 4.8 (291,463) | yes | yes | Namaz app for Quran Majeed, Qibla Direction, Hijri Calendar & 99 Names of Allah | Athan, Prayer Times. Its short description is a keyword list |
| Prayer Now : Azan Prayer Times | com.AppRocks.now.prayer | 10m+ | 4.8 (1,355,732) | yes | yes | Prayer Times, Azan , Quran , Qibla , Azkar , Ramadan Essential ,Islamic App | Azan, Prayer Times. Its short description is a comma list |
| Salaat First: Prayer Times | org.hicham.salaat | 10m+ | 4.8 (695,702) | no | no | Prayer times accurately inchaAllah | Category Travel & Local, not Lifestyle |
| Sajda: Prayer Times, Quran | com.raimbekov.android.sajde | 10m+ | 4.8 (423,344) | no | yes | Prayer times, Qibla, Azan, Quran, Dua & Islamic calendar for every Muslim | Brand collision with SajdaTime, see above |
| Al-Moazin Lite (Prayer Times) | com.parfield.prayers.lite | 10m+ | 4.6 (282,567) | yes | yes | Al-Moazin, is the most accurate prayer times for mobiles and smart watches | "most accurate" is a claim Google's policy discourages |
| Ezan Vakti Pro: Namaz, Kuran | com.mobilexsoft.ezanvakti | 10m+ | 4.7 (1,180,118) | yes | yes | Accurate prayer times, salah & namaz alarm, Quran, Qibla, Azan for Muslims (their dash replaced by a comma here) | Turkish market leader, ranks #1 for "ezan vakti" and "namaz vakitleri" |
| Prayer Times | com.smustafa.praytimes | 10m+ | 4.1 (52,836) | no | no | Prayer Times & Qibla with Islamic events. | Generic title, old app |
| Waktu Solat Malaysia | com.murad.waktusolat | 10m+ | 3.8 (91,643) | yes | yes | Waktu Solat Malaysia kini berwajah baru! Aplikasi muslim yang lengkap. | Media Prima, Malaysia |
| Prayer Times - Qibla & Namaz | com.dawateislami.namaz | 5m+ | 4.6 (124,447) | no | no | Get accurate Prayer Timings & Qibla Direction of any place in the world. | #1 for "prayer times" in GB and for "namaz time" in PK. No ads, no IAP: the closest ethos competitor at scale |
| MAWAQIT: Prayer, Mosque, Quran | com.kanout.mawaqit | 5m+ | 4.8 (152,020) | no | no | Connecting Muslims to Mosques \| Prayer times \| Athan \| Qibla \| Quran \| Wear OS | Mosque timetables, France. Puts "Wear OS" in the short description |
| Jadwal Sholat, Kiblat & Adzan | com.andi.waktusholatdankiblat | 5m+ | 4.9 (279,922) | yes | yes | Prayer times, view Qibla direction and adhan notifications. | #1 in Indonesia for "jadwal sholat" |
| Horaires de prières (Kodelokus) | com.kodelokus.prayertime | 5m+ | 4.4 (145,848) | yes | yes | Accurate prayer times app that will remind you wherever you are | Indonesian developer, ranks #2 in France |
| Prayer Times and Qibla | com.reworewo.prayertimes (zero_) | 1m+ | 4.7 (23,945) | no | yes | Muslim app for prayer reminders. | Ranks #1 or #2 for "prayer times" in GB and US with a generic title and a tiny short description, so weight sits on ratings and retention |
| Prayer Times - Qibla & Salah | com.zeedev.islamprayertime | 1m+ | 4.9 (55,986) | yes | yes | Accurate prayer times, Qibla, Azan, notifications, and widgets for Muslims | Carries "widgets" |
| IslamApp: Prayer times & Qibla | islam.islamapp | 1m+ | 4.9 (78,054) | no | no | Namaz & fasting times, qibla finder & athan for muslims | "fasting times" is a year-round Ramadan hook |
| iPray: Prayer Times & Qibla | com.guidedways.ipray | 1m+ | 4.3 (9,754) | no | no | Beautiful & accurate prayer times (الصلاة), Azan, Qibla & more. No ads. | Carries "No ads" in the short description without an apparent penalty, which proves only that enforcement is a Console notice, not a block |
| Ibadah - নামাজের সময়সূচি | com.crossappers.prayerapp | 1m+ | 4.6 (34,764) | yes | no | Accurate Muslim prayer times, azan alarm, Qibla finder, Quran & tasbih counter. | #1 in Bangladesh |
| Pillars: Prayer Times & Qibla | com.pillars.pillars | 500k+ | 4.4 (3,069) | no | no | Muslim Salah & Athan App | Category Tools. Minimal, ethos competitor |
| Pray Watch \| Prayer Times | com.iw.android.prayerapp (IslamiCity) | 50k+ | 4.2 (231) | no | no | Free, no ads, no tracking, 100% privacy prayer times with adhan, qibla & widget. | Category Tools. The nearest positioning twin: privacy-first, no ads. Ranks #8 for "prayer times no ads" and #2 for "privacy prayer times" with 231 ratings |
| Reklamsız Kıble Bulucu | com.yunuselci.KibleBulucu | 10k+ | none shown | no | no | Ad-free Qibla Finder, Prayer Times, Holy Quran, Dhikr Counter | Ranks #3 for "kıble" in Turkey with "Reklamsız" (ad-free) in the title. Evidence that the phrase is searched, not a licence to copy it |
| Shia Prayer Times & Qibla | com.namaz.prayertimes (Almas Apps) | 1k+ | none shown | no | yes | Offline Shia Namaz Prayer Times, Hijri Calendar & Qibla | #1 for "shia prayer times" in GB with 1k+ installs: that field is open |
| Prayer Wear | com.devlomi.prayerwear | 10k+ | 4.0 | no | yes | Prayer Times on your Wear OS Watch | #1 for "prayer times wear os" with 10k+ installs: that field is open |

Apps the brief named that could not be fetched: **Muslim Mate** (no listing found under the guessed package id, so
its title and numbers are unverified). "Al Moazin" is the Parfield app above.

### What the snapshot says

1. **The head terms are owned by apps with 100k to 1.5m ratings.** SajdaTime has none. No wording closes that gap.
   The listing's job is to convert the visits it does get and to own the thin fields (Wear OS, Shia, offline, no ads).
2. **The two apps that rank with generic titles (zero_, Dawat-e-Islami) do so on ratings and retention**, which is
   consistent with every ranking-factor summary cited below. Ratings come from users, and this app will not prompt for
   them, so the only lever is being worth rating and making the store page easy to reach from outside Play.
3. **"No ads" and "privacy" positioning exists and ranks for its own queries** (Pray Watch, iPray, Reklamsız Kıble
   Bulucu). SajdaTime says the same thing more carefully and backs it with a Data safety card that shows no sharing.
4. **Every leader localises.** Muslim Pro's title changes per country. Sajda's title changes per language. A listing
   that exists only in English is invisible to the markets in rows 7 to 9 of the top ten.
5. **The ethos competitors are small** (Pray Watch 50k+, Pillars 500k+, iPray 1m+) and two of them sit in Tools
   rather than Lifestyle. Lifestyle is the right category: it is where thirteen of the sixteen leaders are.

## How Play ranks, as far as can be established

Google publishes limits and policy but not weights. The following is what Google states, then what the ASO industry
reports, labelled as such.

**Google's own pages (verified 16 Oct 2026):**

- Title 30 characters or fewer. No price or promotion, no store performance or ranking, no Google Play programme
  references, no emoji, emoticons or repeated special characters, no ALL CAPS unless it is the brand. Examples Google
  gives of banned text: "App of the year", "#1", "Best of Play 20XX", "Popular", "10% off", "free for limited time
  only", "Editor's choice", "New". [Metadata policy](https://support.google.com/googleplay/android-developer/answer/9898842?hl=en)
- Descriptions must be "clear and well-written", not "misleading, improperly formatted, non-descriptive, irrelevant,
  excessive". No unattributed testimonials. "Avoid using repetitive or unrelated keywords or references." Word blocks
  and vertical or horizontal word lists are shown as violations. Same page.
- Short description 80, full description 4,000. Do not repeat the short description in the full one. "Don't use deal
  words such as Free or No Ads" in the title. Metadata policy applies to every translation. Provide per-language
  screenshots where screenshots contain text. [Best practices for your store listing](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en)
- Release notes: 500 characters per language, per the Play Console field, confirmed by two independent sources
  ([one](https://deku.posstree.com/ko/share/google-play/release-notes-character-limit/), and the owner's own note in
  docs/store/LISTING.md which asked for confirmation).
- Android vitals bad-behaviour thresholds: user-perceived crash rate 1.09% overall (8% per phone model, 4% per watch
  model), ANR 0.47% overall (8% per phone, 5% per watch). "If an app or game exceeds a bad behaviour threshold, Play
  may reduce the visibility of the title" and may show a warning on the listing. Measured over the last 28 days.
  Memory and code-optimisation thresholds start affecting visibility in February 2027. [Android vitals](https://developer.android.com/google/play/vitals)
- Custom store listings: up to 50, one country per listing, can target search keywords ("users who discover your app
  on Play using specific search terms", chosen from keywords that already bring traffic or searched for), install
  state (not available to new apps), and unique URLs. Default listing must exist and app must be published first.
  [Custom store listings](https://support.google.com/googleplay/android-developer/answer/9867158)
- Store listing experiments: icon, feature graphic, screenshots in the default-graphics experiment, plus descriptions
  in up to five localised experiments, up to two variants against current, judged by confidence interval and minimum
  detectable effect, auto-stop at six months, "More data needed" when traffic is too low. No fixed minimum stated.
  [Store listing experiments](https://support.google.com/googleplay/android-developer/answer/12053285?hl=en)

**Industry reporting (not Google, weights unverified):**

- Title carries the strongest keyword weight, short description nearly equal, full description fully indexed with
  the first 250 to 300 characters weighted most, 2 to 3 natural mentions of a phrase and no stuffing. Install velocity
  relative to the category matters more than lifetime installs. Day 1, 7 and 30 retention carry increasing weight.
  Rating count, recency and velocity matter alongside the average. Poor vitals suppress visibility. Data safety
  incomplete suppresses in some categories. Update cadence of 2 to 4 weeks with real changes. Tags and category do
  not carry keyword weight but set the peer group. [AppRadar, updated 28 Apr 2026](https://appradar.com/academy/aso-basics/app-store-ranking-factors)
- Keyword-targeted custom store listings help conversion by matching the message to the query. Claims that they lift
  ranking are the vendor's view without data. [Phiture](https://phiture.com/asostack/google-play-custom-store-listings/)
- The 2021 metadata policy change (title cut from 50 to 30, promotion words banned) as reported at the time.
  [AppTweak](https://www.apptweak.com/en/aso-blog/how-to-prepare-for-new-google-metadata-policy-changes)

**What this means for an app with 100+ installs and no ratings:** wording changes will not move the head terms.
They can move thin fields and they can improve conversion of the visits that do arrive, which feeds install velocity,
which is the one ranking input a listing edit can influence. The rest comes from outside Play (platforms.md) and from
time.

## Markets: where a no-ads, privacy-first prayer app can win

Ranked by expected payoff for this app specifically, not by population alone.

| Rank | Market | Why it fits | Evidence | Caveat |
|---|---|---|---|---|
| 1 | United Kingdom and the English-speaking diaspora (US, Canada, Australia, South Africa) | The developer is UK based, the listing is in his English, the UK has a large South Asian Muslim population that types "namaz", and privacy expectations are high since the Muslim Pro data story | Suggestions GB: "prayer times london/birmingham/bradford/leicester", "namaz time bradford", "prayer app no ads" cluster. Vice reported in Nov 2020 that Muslim Pro's location data reached a US military contractor via X-Mode, and Muslim Pro ended those partnerships the next day ([summary](https://www.soyacincau.com/2020/11/18/did-muslim-pro-sell-user-data-to-the-us-military-heres-what-we-know-so-far/)) | Still the most competitive storefront |
| 2 | Pakistan (Urdu and English) | App already in Urdu with RTL. 184m Muslims, 89.8% Android ([StatCounter, Sept 2026](https://gs.statcounter.com/os-market-share/mobile/pakistan)). Hanafi default matches the market. Dawat-e-Islami, also ad-free, proves the ethos sells there | Play ur/PK and en/PK results, suggestions "نماز کا وقت حنفی" | Low-end devices, so vitals matter more |
| 3 | Turkey and German-Turkish diaspora | App already in Turkish. 75m Muslims. "İnternetsiz" and "reklamsız" are typed qualifiers, which is exactly this app | Suggestions TR: "namaz uygulaması internetsiz", "reklamsız namaz vakitleri uygulaması" | Diyanet times are the reference. The Turkey calculation method is included, and the disclaimer handles disagreement |
| 4 | Indonesia and Malaysia | App already in Indonesian. Largest Muslim population, 79% Android in Indonesia ([StatCounter, Sept 2026](https://gs.statcounter.com/os-market-share/mobile/indonesia)). "Tanpa iklan" is typed | Suggestions ID and MY | Developer verification for sideloading started in Indonesia on 30 Sept 2026 per Android Authority (only affects installs outside Play). Kemenag and JAKIM timetables are the local reference |
| 5 | Shia communities everywhere (Iraq, Iran diaspora, Lebanon, Bahrain, Pakistan, UK, US) | The Jafari rule is implemented properly and the "shia prayer times" field is thin | Play GB "shia prayer times" results | Iran itself has limited Play access, so target the diaspora |
| 6 | Wear OS owners everywhere | Thin field, real feature | Play GB "prayer times wear os": 13 results | Small absolute numbers |
| 7 | France and the Maghreb diaspora, Bangladesh, Hindi-reading India, the Arab world | Store listing only (app stays in English or one of its four languages). Worth the translation because it costs nothing, but expect less than rows 2 to 4 | Play fr/FR, bn/BD, hi/IN, ar/SA results | An Arabic-speaking user gets an English app. The listing should not pretend otherwise and does not |

Population figures are Pew Research Center 2015 estimates as compiled [here](https://www.jewishvirtuallibrary.org/countries-with-the-largest-muslim-populations)
(Indonesia 220m, India 195m, Pakistan 184m, Bangladesh 144m, Nigeria 90m, Egypt 84m, Iran 78m, Turkey 75m), which
the source itself calls imprecise. Pew's current page says only that six countries (Nigeria, Egypt, Pakistan, India,
Bangladesh, Indonesia) hold 52% of all Muslims.

## Not verified

- Any absolute search volume, for any phrase, on Play or the web.
- Whether Play's search in any locale uses the short description of a *translated* listing for users whose device
  language differs from the storefront language (industry belief: the user's device language picks the listing).
- Muslim Mate's listing data.
- The current GitHub repository visibility, topics and release assets (403 from the sandbox).
- Whether "Wear OS" in the short description clears the Console's promotability check. Expected yes (platform name).
  Test with the owner's reload-and-search method.

## Appendix: how the evidence was fetched (reproducible, no credentials)

```
# Play search for a phrase on a storefront (hl = language, gl = country)
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/search?q=prayer%20times%20wear%20os&c=apps&hl=en_GB&gl=GB" \
  | grep -o 'href="/store/apps/details?id=[A-Za-z0-9_.]*"' | awk -F'"' '!seen[$2]++ {print $2}' | head -30

# Is SajdaTime on that page at all?
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/search?q=sajda%20time&c=apps&hl=en_GB&gl=GB" | grep -c "com.sajdatime.app"

# Google web suggestions for a phrase, per language and country
curl -s -A "Mozilla/5.0" "https://www.google.com/complete/search?client=firefox&hl=tr&gl=TR&q=namaz%20uygulamas%C4%B1"

# A competitor's listing fields
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/apps/details?id=com.iw.android.prayerapp&hl=en_GB&gl=GB" \
  | grep -o 'itemprop="description" content="[^"]*"'
```

Raw pages from this run are kept in the session scratchpad under `playsearch/`, `playapps/` and `suggest_*.json`.
