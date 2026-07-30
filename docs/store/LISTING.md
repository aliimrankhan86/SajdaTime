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
Offline prayer times and Qibla compass. No ads, no accounts, no tracking. Free.
```

`79 / 80`

---

## Full description (max 4000)

```
SajdaTime tells you when to pray and which way to face. That is all it does, and it does it
without ads, without an account, and without sending your location anywhere.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a
basement, or with no signal at all.


WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode with a sound you choose, loud enough to wake you for Fajr
• An optional quiet badge in the notification shade showing the next prayer
• A printable PDF timetable for today, the next seven days, or the whole month
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand, both checked for readability


BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly, not as an afterthought.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai,
  Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The right method is chosen automatically for your region, and you can override it
• A high-latitude rule that gives sensible Fajr and Isha times in northern countries where
  the sun never fully sets in summer
• The Umm al-Qura Ramadan adjustment is applied automatically during Ramadan


YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are paid for by advertising, which means paid for by your data.
This one is not paid for at all.

• No adverts, ever
• No accounts, no sign-in, no email address
• No analytics, no crash reporting, no tracking of any kind
• Approximate location only, read while the app is open, never in the background
• Your coordinates never leave your device — there is no server for them to go to
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location at all, you can type a city name instead and
the app works exactly the same.


HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. It was built with the help of artificial
intelligence and it may get things wrong. If a time or a direction ever looks off, please
check with your local mosque or someone qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source
library, and are checked against independent reference timetables before each release.


FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in
your duas.
```

`2,977 / 4000`

---

## Category and tags

| Field | Value |
|---|---|
| App category | Lifestyle |
| Tags | Prayer, Islam, Religion, Compass, Offline |
| Contains ads | **No** |
| In-app purchases | **No** |
| Content rating | Everyone / PEGI 3 (answer the questionnaire honestly, nothing applies) |
| Target audience | 13+ (not directed at children) |

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
| Purpose | **App functionality** only. Not analytics, not advertising, not personalisation. |
| Is it required, or can users choose? | **Users can choose** — the app works from device location, or from a typed city, or not at all |
| Is all user data encrypted in transit? | **Yes** — HTTPS |
| Can users request deletion? | **No / not applicable** — nothing is retained to delete |
| Crash logs, diagnostics, device IDs | **None.** No Crashlytics, no analytics SDK, nothing. Leave every box unticked. |

Never claim nothing at all touches the network — the privacy policy already says it does, and
the two must agree.

---

## Advertising ID declaration

**Answer: No.** There are no ads, no analytics and no attribution SDK.

Before you answer, confirm nothing pulled the permission in transitively — a Play Services
library can merge `AD_ID` into the manifest without you asking. A mismatch between this
answer and the merged manifest is a Console error:

```bash
grep -r "AD_ID" app/build/intermediates/merged_manifests/ 2>/dev/null || echo "clean — answer No"
```

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

## Assets in this folder

| File | Play field |
|---|---|
| `icon-512.png` | App icon (512 × 512) |
| `feature-graphic-1024.png` | Feature graphic (1024 × 500) |
| `screenshots/01-times-light.png` | Phone screenshot |
| `screenshots/02-qibla-light.png` | Phone screenshot |
| `screenshots/03-settings-light.png` | Phone screenshot |
| `screenshots/04-times-dark.png` | Phone screenshot |
| `screenshots/05-qibla-dark.png` | Phone screenshot |
| `screenshots/w1-times.png` | Wear OS screenshot |
| `screenshots/w2-qibla.png` | Wear OS screenshot |

Phone screenshots are **1080 × 1920** (9:16); Wear screenshots are **384 × 384** (1:1). Both
satisfy Play's rule that a screenshot's long side may not exceed twice its short side.

The emulator captures are natively 1080 × 2400, which is 2.22:1 and **would be rejected**.
`screenshots/raw/` holds those originals; `tools/build-store-assets.sh` reframes them onto a
9:16 canvas in the brand green, so nothing on screen is cropped away. Upload the reframed
ones, not the raw ones.

Run `./tools/build-store-assets.sh` to regenerate everything in this folder. The captures
themselves are taken by hand from a running emulator; the prayer times visible in them were
checked against the Aladhan reference API at the time of capture and agreed to within a
minute.
