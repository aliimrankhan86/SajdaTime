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
• Light and dark themes, both checked for readability


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
```

`2,897 / 4000`

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

Google asks these one at a time. The honest answers:

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | Yes (the one optional lookup uses HTTPS) |
| Do you provide a way for users to request data deletion? | Not applicable — no data is retained |

**The one nuance to declare.** If a user types a city name instead of using their location,
that name is sent once to a geocoding service to get coordinates back. Google's own guidance
is that data which is only processed in the moment and never stored does **not** count as
"collected". Declare it that way if the form gives you the option, and if an "approximate
location" checkbox is unavoidable, mark it as **processed ephemerally, not collected, not
shared**. Never claim nothing at all touches the network — the privacy policy already says
it does, and the two must agree.

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
