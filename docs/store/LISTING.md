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
Offline prayer times and Qibla compass for Sunni and Shia. No tracking.
```

`71 / 80`

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
| Adding *Adhan* for search volume | The app plays a **user-chosen** sound, not a bundled recitation. It would be a keyword that over-promises |
| Adding *Namaz* / *Salah* for search volume | Keyword stuffing in an English sentence, and Play flags that separately |

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

The freed characters were deliberately **not** spent. At `71 / 80` every word is doing work,
and nine unused characters are not a debt to be paid off with a keyword.

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

---

## Full description (max 4000)

```
SajdaTime tells you when to pray and which way to face.
That is all it does, and it does it without ads, without an account, and without sending your location anywhere.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

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

Most free prayer apps are funded by advertising, which means your data pay for them.
This one is not paid for at all.

• No adverts, ever
• No accounts, no sign-in, no email address
• No analytics, no crash reporting, no tracking of any kind
• Approximate location only, read while the app is open, never in the background
• Your coordinates never leave your device — there is no server for them to go to
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location at all, you can type a city name instead, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. It was built with the help of artificial
intelligence, and it may get things wrong. If a time or a direction ever looks off, please check with your local mosque or someone qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source library, and are checked against independent reference timetables before each release.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

`3,019 / 4000`

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

The character count in this file was also stale — it read `2,977` while the live text is
`3,019`. Treat a count written by hand as decoration.

**Verify this file against the Console before trusting it**, rather than pasting from it and
assuming. The check is cheap: hash the textarea in the browser and hash the fenced block
here, then compare. A store listing that only exists correctly in a web form is one
accidental Discard away from being lost.

**One phrasing worth a second look, not a blocker.** *"which means your data pay for them"*
treats *data* as a plural noun. That is defensible, but it reads as a typo to most people,
and `CLAUDE.md` requires this listing to work for someone who does not read English well.
The version this file used to hold — *"which means paid for by your data"* — repeats
"paid for" on purpose, which is what makes the point land: these apps are not free, you are
simply paying with something other than money. Changing it is the owner's call; it is style,
not correctness, and nothing else depends on it.

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

At 9:16 the Times list does not fit on one screen. That is why `03` is scrolled rather than
a duplicate of `01` — between them the carousel shows the hero, all six times and the PDF
export, without showing the same thing twice.

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

It now reads `Sunni & Shia · No accounts · No tracking`. `Works offline` went for a different
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
