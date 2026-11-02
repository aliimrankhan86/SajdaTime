> **Research of 16 October 2026, written by a read only research pass and kept here so the evidence is not lost.** Search results,
> competitor counts and ranking claims in it were measured on that day from public pages and have not all been re-checked by hand, and
> they go stale. Nothing here is applied unless `docs/HANDOVER.md` 5.22 says so. Anything that only the owner can do (accounts, terms,
> pressing submit or publish) is the owner's. **The location claim in this file was corrected to the true one on the same day:** say
> "the times are worked out on your phone and we never receive your location", never "your location never leaves your phone"
> (`docs/HANDOVER.md` 5.21).

# SajdaTime beyond the Play listing: other stores, Play Console levers and launch channels

Written 16 October 2026. Ranked by expected payoff against effort for this app specifically: free, no ads, no revenue,
one maintainer, 100+ installs, no ratings, phone plus Wear OS, package `com.sajdatime.app`. "Owner" below means the
things only Ali can do: creating accounts, accepting terms, pressing submit or publish, and signing anything. An
assistant drafts, checks and walks him through screens. It never creates an account, never accepts a term, never
presses publish.

## The ranking

| # | Lever | Payoff | Effort | Cost | Verdict |
|---|---|---|---|---|---|
| 1 | Reply to every Play review, and put the Play link where people already are (website, README, WhatsApp, mosque) | High: ratings are the ranking input this app lacks, and these are the only honest ways to earn them without an in-app prompt | Low | £0 | Do now and keep doing |
| 2 | Publish the eight translated listings (listing-locales.md) | High: opens Indonesia, Pakistan, Turkey, Malaysia, Bangladesh, India, the Arab world and France to search at all | Low (paste eight times) | £0 | Do with the 1.4.0 release |
| 3 | Fix the website head and the document leak, submit a sitemap (website-seo.md) | Medium-high: the only page that can rank on the web, and a clean link preview everywhere the app is shared | Low | £0 | Do this week |
| 4 | Custom store listings in Play Console for Pakistan, Turkey and Indonesia, plus one keyword listing for "wear os" | Medium: tunes the English listing by country (a Pakistani user on an English phone never sees the Urdu one) and matches the message to the query | Medium | £0 | Do after 1.4.0 is live |
| 5 | Show HN and one honest Reddit post in the right places | Medium: a burst of privacy-minded installs and the first reviews. Bursty, not durable | Low | £0 | January, with the Ramadan release and the fixed website |
| 6 | AlternativeTo listing, GitHub topics and README, awesome-list pull requests | Medium-low each, durable backlinks and discovery | Low | £0 | Do this month |
| 7 | Pitch the privacy-minded Islamic app roundups in mid-January for Ramadan 2027 | Medium if one lands, zero otherwise | Low | £0 | January |
| 8 | Samsung Galaxy Store | Medium-low: Samsung is the top Android brand in the UK, Pakistan and Bangladesh, but Galaxy Store search traffic for prayer apps is a fraction of Play's. Requires commercial-seller verification with ID and banking | Medium-high | £0 (a D-U-N-S number may cost outside the US per Samsung) | Later, if ever |
| 9 | Huawei AppGallery | Medium-low: reaches HMS phones that cannot install from Play (Turkey, Malaysia, the Gulf, some of Pakistan). Needs an identity-verified developer account and a test on a phone without Google services first | Medium | £0 | Later, after a GMS-free test |
| 10 | Amazon Appstore | Low: Fire tablets and Fire TV, no Google services, tiny Muslim-app traffic | Low-medium | £0 | Skip for now |
| 11 | F-Droid | Medium for the privacy audience and a strong backlink, but it needs an open-source licence (none exists) and a build flavour with no Firebase and no Play Services | High | £0 | Decision for the owner on the licence first. Not now |
| 12 | IzzyOnDroid | None: it rejects apps with analytics libraries and apps substantially built with generative AI. Both apply | n/a | n/a | Not possible |
| 13 | Direct APK on GitHub Releases | Low: serves the few who refuse Play, costs support for updates, and from 2027 Google's developer verification adds friction to any sideload from an unverified source | Low | £0 | Optional, not recommended |
| 14 | Apple App Store | Not available. SajdaTime is Android only. There is no iPhone presence to optimise and no alternative store for iOS. The only thing an iPhone user can reach is the website, which should say so plainly ("Android and Wear OS only") rather than let them hunt | n/a | n/a | State it on the site |
| 15 | Google Ads App campaigns | Unknown, and it is the one lever that costs money | Medium | money | A decision for the owner, outside this plan. Noted because the handover records an "ads note" he is weighing |

APKPure, Aptoide, Uptodown and APKMirror mirror Play listings without being asked. Nothing to do there, and uploading to
them yourself buys nothing.

## 1. Google Play Console levers (no new store, no new account)

### Replying to reviews

Play Console, then Ratings and reviews, then Reviews. Google emails the reviewer when a developer replies, the reply is
public, and users do revise ratings after a reply. For a one-person app the reply is the brand. Reply within a day,
in the user's language (the listing languages are enough), thank them, answer the actual point, and where a time
disagrees with their mosque, say what the HONEST section says: follow the mosque, and ask which calculation method
their mosque uses so it can be checked. Never ask for a changed rating. Never argue.

### Ratings without an in-app prompt (the rule stands: no prompt, ever)

What is allowed and works:

- The Play link with a referrer in every place the app is mentioned (website button, README, share message below).
  Anyone who installs from a link they trusted is likelier to rate.
- A reply to every review, above.
- A line in the mosque notice and the WhatsApp message that says where the app is, not "please rate it".
- Release notes that describe what changed. Never "if you like the app, rate it", which Play's metadata policy
  treats as a call to action and which breaks the ask-once rule anyway.

What is not allowed, listed so no future session reaches for it: the In-App Review API (an in-app prompt by another
name), any incentive for a review, asking family and friends to post reviews they did not write themselves, review
exchanges, and buying reviews. All of them are Play policy violations besides being against the project's own rule.

### Custom store listings (Play Console, then Grow, then Store presence, then Custom store listings)

Google allows up to 50, each targeting countries (one listing per country) or search keywords or a unique URL.
Google's page: https://support.google.com/googleplay/android-developer/answer/9867158. Start with four:

| Listing name (cannot be changed later) | Target | Default language | What changes against the default |
|---|---|---|---|
| `pk-en` | Country: Pakistan | English | Short description leads with namaz (78 characters, measured): "Offline namaz times and Qibla compass for Sunni and Shia, Hanafi Asr. Wear OS." Body unchanged except the school section moves above privacy. Add the Urdu translation to this listing too, so Urdu-language phones in Pakistan see Urdu |
| `tr-tr` | Country: Türkiye | Turkish (from listing-locales.md) | Screenshot order: Times screen in Turkish first (the release script captures it). English translation attached |
| `id-id` | Country: Indonesia | Indonesian | Times screen in Indonesian first. English attached |
| `kw-wear-os` | Search keywords: "wear os", "prayer times wear os", "prayer watch", and the variations the Console offers | English | Feature graphic and first screenshot show the watch. Short description (77 characters, measured): "Prayer times and Qibla on your Wear OS watch and phone. Offline, no accounts." No price or promotion word in it |

Each custom listing goes through review with the next release. Countries not covered keep the default listing.
Install-state targeting (churned, lapsed) is not offered to new apps, so ignore it.

Owner-only: creating each listing and pressing Save then Submit. An assistant prepares the text (above and in the
listing files) and walks the screens one at a time.

### Store listing experiments (Play Console, then Grow, then Store performance, then Store listing experiments)

Honest expectation: with about 100 lifetime installs the Console will show "More data needed" for months. Do not run
one yet. When daily store visits reach the hundreds (the Console shows the estimate on the setup page), the first
experiment is the icon, because icon changes move conversion more than any text, then the first screenshot, then the
short description ("with Wear OS" against "No accounts" settles the one open question in listing-en.md with data
instead of judgement). One asset per experiment, two variants at most, and leave it until the Console calls it.

### Android vitals (Play Console, then Quality, then Android vitals)

The app has no crash reporting by design, so vitals is the only place a crash on a stranger's phone becomes visible.
Google's bad-behaviour thresholds are 1.09% user-perceived crash rate and 0.47% ANR rate overall, 8% per phone model,
4% and 5% per watch model, measured over 28 days, and exceeding them "may reduce the visibility of the title"
(https://developer.android.com/google/play/vitals). Check it on the first of each month alongside the monthly numbers
task the handover already lists, and after each release. An "emerging issue" flag gives 21 days to fix.

### Screenshots and the feature graphic

Already in `docs/store/LISTING.md`: retake screenshot 03 as the Ramadan Times screen in January and add a widget
screenshot. Two additions from the competitor snapshot: the top apps all caption their screenshots with one short
line each, which is allowed if the text has no price or ranking words ("Works offline", "Sunni and Shia", "Qibla to
true north", "On your watch"), and captioned screenshots must then be provided per language. Not urgent. Do it when
the January retake happens, in English first.

### Category and tags

Lifestyle is right (thirteen of sixteen leaders). The three tags stay. Nothing to change.

### What's new

500 characters per language, confirmed. Describe what changed, nothing else. Text in listing-en.md.

## 2. Other stores, with exact requirements

### Samsung Galaxy Store

Source: https://developer.samsung.com/galaxy-store/prepare.html and
https://developer.samsung.com/galaxy-store/how-to-distribute.html (fetched 16 Oct 2026).

- Account: a Samsung account (its email becomes the Seller Portal login), then Seller Portal registration, then an
  application for **commercial seller status**, which is required to distribute even free apps. The Samsung account's
  country fixes the Seller Portal country and cannot be changed later.
- Verification: the name must match across the profile, a government ID and the financial account. Financial
  information is required even for a free app (PayPal is "the easiest"). Business verification is by D-U-N-S number
  or, failing that, a DBA or equivalent document. Samsung says a D-U-N-S number "may" cost money outside the US, and
  that verification can take up to 10 business days, with commercial approval "several days" on top.
- Listing: seller name, company name, representative's name, phone, address, privacy policy URL and support email
  are shown publicly. Paramount Consultants Limited would be the natural seller entity, which is a decision for the
  owner since it puts the company's name on a charity app.
- Binary: APK is accepted. Whether the current Seller Portal accepts an AAB was not verified. The release key stays
  with the owner either way.
- Wear OS: Galaxy Watch 4 and later run Wear OS, and Google distributes Wear OS apps through Play. Whether a
  phone-paired Wear OS app can also be listed in Galaxy Store was not verified from Samsung's pages (the fetched page
  covers Tizen watches, whose distribution Samsung has discontinued). Assume phone app only.
- Payoff reasoning: Galaxy Store is preinstalled on every Samsung phone and promotes apps in its own editorial
  slots, but its search traffic for this category is small and no competitor in the snapshot relies on it.

Owner-only: every step above. There is nothing an assistant can do here until the account exists.

### Huawei AppGallery

Sources: Huawei's developer policy centre (navigation shell only, the requirements pages are behind it) and a
third-party walkthrough (https://www.webtoapp.design/blog/publish-in-huawei-app-gallery), so treat the detail as
partly unverified.

- Account: a Huawei ID, then identity verification for a personal or company account (documents not confirmed from
  Huawei's own page), free. Verification result arrives by email.
- Submission in AppGallery Connect: new app, APK and Mobile Phone, name, category App, default language, phone and
  tablet compatibility, description, icon, screenshots, regions, package upload, free, content rating, privacy policy
  URL, then Submit. The walkthrough reports publication "in a few hours to days".
- The technical caveat that decides it: modern Huawei phones have no Google Play Services. SajdaTime's location uses
  the platform `LocationManager` (a deliberate choice in `docs/HANDOVER.md` §2) and its city lookup falls back to
  Open-Meteo, both fine. But it compiles in `play-services-wearable` for the watch Data Layer and `firebase-analytics`
  for the optional usage counts. On a phone without Google services those calls must fail quietly, not crash. **No
  one has tested this.** Before any AppGallery submission the app has to be run on a GMS-free device (Huawei offers
  free cloud test devices inside AppGallery Connect) through onboarding, location, a city lookup, the Qibla, an alarm
  and the usage-counts switch. If anything throws, that is a code change with its own release.
- Payoff reasoning: HMS phones are a real share in Turkey, Malaysia and the Gulf and some of Pakistan, and those users
  cannot install from Play at all, so AppGallery is the only route to them. Small absolute numbers for a prayer app
  with no brand.

Owner-only: the Huawei ID, the identity verification, the terms, the submit button.

### Amazon Appstore

Source: https://developer.amazon.com/docs/app-submission/submitting-apps-to-amazon-appstore.html (fetched 16 Oct 2026).

- Account: free developer account. Binary: APK or AAB for Fire OS. Assets and descriptions per Amazon's Appstore
  Details step (sizes not on the fetched page). A physical device is recommended for testing.
- Devices: Fire tablets and Fire TV. Windows 11's Android subsystem, which was the Appstore's route to PCs, was
  discontinued by Microsoft in 2025, so that audience is gone. Fire devices have no Google services, so the same
  GMS-free test as Huawei applies.
- Payoff: low. Nobody searches a Fire tablet for prayer times in numbers that matter, and a tablet layout does not
  exist yet (the empty tablet screenshot slots in `docs/store/LISTING.md` are deliberate).

### F-Droid

Source: https://f-droid.org/docs/Inclusion_Policy (not fetched in full this session, well known) and the IzzyOnDroid
policy below, which is stricter.

- Needs an OSI-approved licence. The repository has none, so this is a decision before it is a task.
- F-Droid builds from source and rejects non-free dependencies: `firebase-analytics` and `play-services-wearable` both
  are. A `foss` product flavour would have to exclude them, which means no watch sync in that build and no usage
  counts, plus Fastlane-structured metadata in the repo and a merge request to fdroiddata, then a build on F-Droid's
  servers that can take days.
- Payoff: the F-Droid audience is precisely the privacy audience, and an F-Droid page is a durable backlink that
  ranks. Effort is the highest on this list. Worth revisiting after a licence decision and after 1.4.0 is stable.

### IzzyOnDroid

Source: https://izzyondroid.org/docs/general/AppInclusionPolicy/ (fetched 16 Oct 2026). Trackers "such as ads and
analytics" are "rejected in general", and the policy "strongly opposes" apps "fully or in part created by generative
AI", rejecting vibe-coded apps outright. SajdaTime is openly built with AI help and carries an analytics library.
Not possible, and not worth the argument.

### Direct APK on GitHub Releases

Possible today (the owner signs with his release key and uploads the APK to a tagged release). It serves people who
refuse Play, and it is what IzzyOnDroid and some roundups would have pointed at. Against it: no automatic updates,
support questions from mismatched versions, and Google's developer verification for installs outside Play, which
began on 30 Sept 2026 in Brazil, Indonesia, Singapore and Thailand and expands globally in 2027 per Android Authority
(https://www.androidauthority.com/android-sideloading-changes-timeline-3679204/). The owner is a registered Play
developer, which is the verification route, so his signed APK should install normally, but the detail of how
GitHub-hosted APKs are treated was not confirmed. Not recommended now.

### Apple

SajdaTime has no iOS app, so there is no App Store listing, no Apple Watch app, and nothing to optimise. The website
should say "Android and Wear OS" in its first line so an iPhone user does not search for it. `docs/ARCHITECTURE.md`
carries an iOS porting guide, and `core/` has no Android imports in the calculation, but a port is a product decision
with its own cost (a Mac, an Apple developer account at USD 99 a year, Swift UI work), which the zero-running-cost
rule argues against. Say it plainly and move on.

## 3. Launch and backlink channels, honest versions

Rules for every post, in every channel: disclose that you are the developer in the first line, describe what the app
does and does not do, include the disclaimer sentence, link once, answer every reply, post once per community, and
never ask for ratings or upvotes. Use a distinct referrer per channel (`utm_source=hn`, `reddit`, `alternativeto`,
`whatsapp`) so the install source shows up for users who opt in to usage counts.

### AlternativeTo (do this month, low effort, durable)

AlternativeTo lists prayer apps with "ad-free" and "privacy" tags (its "Prayer Times app" page lists Vakti App as
"free, ad-free, open source" and Athan as freemium). The owner creates an account and submits SajdaTime as an
alternative to Muslim Pro, Athan, Salatuk and Pray Watch, with the description below. Submissions are moderated, which
takes days. Ready to paste:

```
SajdaTime is a free prayer times and Qibla compass app for Android and Wear OS with no ads, no accounts and no
tracking of your location: times are calculated on the phone and work offline. Sunni (Hanafi, Shafi'i, Maliki,
Hanbali) and Shia (Jafari) conventions, 14 calculation methods, Hijri date, per-prayer notifications and alarms,
monthly timetable as PDF, two home screen widgets, and a watch app with a tile. In English, Urdu, Turkish and
Indonesian. Built by one person as an ongoing charity. It is a helper, not a religious authority: where it and your
mosque disagree, follow your mosque.
```

Note for the AlternativeTo form: "no tracking of your location" is accurate (the optional usage counts never include
location) and matches the privacy policy. Do not tick "open source" until a licence file exists.

### Show HN (January, with the Ramadan release and the fixed website)

Hacker News rewards a specific, honest story. Post from the owner's account, weekday, around 14:00 UK time. Title and
text, ready to paste (edit the version number on the day):

```
Show HN: SajdaTime, a prayer times app that never sends your location anywhere

I built a Muslim prayer times and Qibla app for Android and Wear OS after the 2020 story about a popular prayer app's
location data reaching a US military contractor. SajdaTime calculates everything on the device with adhan-java, works
offline, has no ads, no accounts and no server of its own. Approximate location only, read while the app is open, and
cloud backup is switched off so the cached location cannot leave the phone. Usage counts are opt in and never include
location, school of thought or settings.

It supports Sunni and Shia conventions properly (the Jafari Maghrib rule, the right Asr rule per madhab), 14
calculation methods, Hijri date, alarms, widgets, a monthly PDF timetable, and a watch app that calculates on its own.
Four languages in the app, eight more on the store.

I am one person and I built it with a lot of help from AI, which the app says in Settings, About, Disclaimer: it is a helper, not a
religious authority, and where it disagrees with your mosque you follow your mosque.

Play: https://play.google.com/store/apps/details?id=com.sajdatime.app
Site and privacy policy: https://aliimrankhan86.github.io/SajdaTime/
```

Expect questions about the AI-assisted build, the missing licence, and why not F-Droid. The honest answers are in this
file. If the licence decision has been made by then, say so in the post.

### Reddit (one post per community, read each community's rules first, owner posts)

Verified on 16 Oct 2026: **r/androidapps bans self-promotion outright** (its mod post moves "self-promotion, new app
ideas and app feedback" to r/droidappshowcase with "zero tolerance" in the main subreddit). So:

- r/droidappshowcase: the place for it. Post the Show HN text with a "I'm the developer" first line and two
  screenshots.
- r/androiddev: only in its weekly "App Feedback" thread, never as a standalone post.
- r/WearOS and r/GalaxyWatch: both allow developer posts with disclosure in most periods, and a prayer app with a
  self-calculating tile is on-topic. Lead with the watch.
- r/privacy and r/degoogle: on-topic because of the proposition. Some periods restrict app promotion, so check the
  rules on the day. r/fossdroid requires open source, so not until a licence exists.
- Muslim communities (r/islam, r/MuslimLounge, r/Muslim, r/shia, r/Pakistan, r/indonesia, r/Turkey): rules vary and
  several ban promotion. Could not verify their current rules from this session. The safest route is to answer when
  someone asks "which prayer app does not have ads" and to disclose, rather than to post cold. Set a weekly search on
  Reddit for "prayer app ads" and "muslim pro alternative" and reply honestly where it fits. The Google suggestion
  "muslim prayer app reddit" shows people are asking there.

### Hand to hand (the highest-converting channel for the first hundred ratings, and the one nobody writes down)

The owner's own mosques in Slough and the family WhatsApp groups. A message, ready to paste, that says where the app is
and what it is, and asks for nothing:

```
Assalamu alaikum. I have made a free prayer times and Qibla app for Android phones and Wear OS watches, as sadaqah
jariyah. No ads, no accounts, it works offline and the times are worked out on your phone and we never receive your location. Sunni and Shia settings,
Hanafi or Shafi'i Asr, Hijri date, alarms, widgets, and a monthly timetable you can print. In English, Urdu, Turkish
and Indonesian.

It is a helper, not a religious authority. Where it and the mosque's timetable differ, follow the mosque.

Google Play: https://play.google.com/store/apps/details?id=com.sajdatime.app&referrer=utm_source%3Dwhatsapp

Please remember me, my family and my parents in your duas.
```

A one-page notice for a mosque board is the same text with a QR code to the Play link (any free QR generator, the
owner makes it so the link is his). Ask the imam first.

### Roundups and press (mid-January, for Ramadan 2027)

Cold pitches to Android Police, Android Authority or 9to5Google rarely land for a 100-install app. Two kinds do:

1. Writers who already care about the privacy angle. Found on 16 Oct 2026: islamtics.com runs "10 Best Islamic Apps
   for Android in 2026 (Free, Tested, and Privacy-Safe)" and "7 Best Muslim Pro Alternatives in 2026 (Free & Honest
   Picks Compared)". Email the author with three sentences, the Play link, the privacy policy link, and an offer to
   answer questions. Nothing else. Do not offer anything in return.
2. Ramadan roundups ("best Ramadan apps 2027", "iftar and suhoor apps") that every Android site publishes in the
   week before Ramadan (expected to begin around 8 February 2027, subject to the moon). Pitch in the second week of
   January, when the January listing with the Ramadan card is live and the website shows it.

Ready-to-paste pitch:

```
Subject: A free, ad-free prayer app with a Ramadan timetable, for your Ramadan 2027 list

Hi <name>,

I read your <article title> and thought SajdaTime might fit. It is a free Android and Wear OS prayer times and Qibla
app with no ads, no accounts and no tracking of location: times are calculated on the phone and work offline. For
Ramadan it shows suhoor end and iftar for your location, the length of the fast, a timetable you can save as a PDF,
and an optional suhoor reminder. Sunni and Shia conventions are both supported properly.

Play: https://play.google.com/store/apps/details?id=com.sajdatime.app
Privacy policy: https://aliimrankhan86.github.io/SajdaTime/privacy.html

I am the developer and happy to answer anything. It is given as a charity and there is no paid tier.

Ali Imran Khan
```

### GitHub

Topics, description and README changes are in website-seo.md. Beyond that: search GitHub for "awesome islamic",
"awesome muslim" and "awesome android apps" lists and open a pull request adding SajdaTime with a one-line description
and the Play link, following each list's contribution rules. Low effort, nofollow links, but these lists are where
Muslim developers look. Could not verify which lists exist or are maintained from this session.

### Not worth doing

Product Hunt (Android utility apps with no revenue story get little there, and it costs a day), paid "ASO services",
any site selling installs or reviews, press-release wires, and directory submissions beyond AlternativeTo.

## 4. The calendar

| When | What | Who presses the button |
|---|---|---|
| This week | `.nojekyll`, website head, sitemap, README, GitHub topics and description, AlternativeTo submission | Owner commits and submits. Assistant prepares every file |
| With the 1.4.0 release | New English short description and Block 1, the eight translations, release notes | Owner pastes and submits |
| First of each month | Vitals, reviews answered, install sources for opted-in users | Owner opens the Console. Assistant reads the numbers with him |
| After 1.4.0 is live | Custom store listings: `pk-en`, `tr-tr`, `id-id`, `kw-wear-os` | Owner creates and submits |
| Early January 2027 | Block 2 (January) in English and each language, Ramadan screenshot retake, `ramadan` GitHub topic, roundup pitches, Show HN, r/droidappshowcase and r/WearOS posts | Owner |
| Mid-February 2027 | Ramadan begins (around 8 February, moon permitting). Reply to every review daily through the month | Owner |
| After Ramadan | Decide on the licence, F-Droid, Galaxy Store and AppGallery with the numbers in hand | Owner |
