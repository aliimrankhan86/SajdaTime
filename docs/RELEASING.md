# Releasing SajdaTime to Google Play

Written for the owner, who is not a developer. Follow it top to bottom. Anything marked
**ONLY YOU** cannot be done by an assistant or anyone else.

---

## Step 0 — Before anything else: the 12-tester rule

This account is a **personal** one created well after 13 November 2023, so the rule applies.
Verified against Google's [App testing requirements for new personal developer
accounts](https://support.google.com/googleplay/android-developer/answer/14151465), which
still reads:

> "At least 12 testers must be opted-in to your closed test when you apply for production
> access. They must have been opted-in for the last 14 days continuously."

It is the single biggest surprise for solo developers and it means **you cannot publish
publicly on day one**. There is **no charity, nonprofit or free-app exemption** — the only
exemptions are account-based (organisation accounts, and personal accounts created on or
before 13 November 2023).

### The details that actually catch people out

- **The 14 days are per tester, not a headcount.** Google: *"we won't count testers who
  opted in, tested for less than 14 days, and then opted out"*, and even if they rejoin,
  *"these 14 days must be consecutive"*. So one person dropping out on day 13 resets **their
  slot** to zero — not everyone else's, but you are then 14 days from having 12 again.
- **Therefore recruit 16–20, not 12.** The buffer is the whole point.
- **The clock starts when the release is live and testers have opted in**, not when you
  create the track. The opt-in link does not even exist until the release passes review.
- **Pushing new builds does not reset the clock.** The counter tracks tester opt-in, not
  releases — so keep shipping fixes into the same track. That is also what gives you
  something real to write in the production-access form.
- **Testers must click the opt-in link** while signed into the exact Google account you
  listed. Being on your email list is not enough.
- **Internal testing does not count.** It is capped at 100 testers and is a different track.
  The requirement names *closed* testing specifically.
- Then budget **another ~7 days** for the production-access review on top of the 14. Google:
  *"This usually takes 7 days or less, but may occasionally take longer."*

Realistic floor from a standing start: **about three weeks.**

> **Ignore the paid "12 testers" services.** They dominate the search results for this topic
> and have a direct financial interest in making the rule sound harsher than it is. Several
> of their specific claims appear **nowhere in Google's documentation**, and two of them
> contradict each other on the same site.

### What Google does not write down — and what gets people rejected

Google deliberately does not publish the exit criteria. Asked directly, its support told one
developer *"no further details could be provided"*. So the following comes from developers who
were actually rejected, on Google's own community forum. It is **not policy** — the volunteer
"Product Experts" who give it are unpaid developers, they contradict each other, and one
repeatedly stated the number as 14 when it is 12. Weight it accordingly. But it is the only
evidence there is of how the check behaves in practice, and it is consistent on these points:

- **Testers must install from the Play Store.** The single most-cited cause of rejection is
  handing testers an APK directly. *"Play has no way to determine they are actually testing
  the app."* Never send anyone a `.apk` — always the opt-in link.
- **Emulators do not count.** Widely reported, never officially stated.
- **Merely staying opted in is not enough.** The rejection email's first line is *"Testers
  were not engaged with your app during your closed test"* — so Google is looking at usage
  data, not just the opt-in flag.
- **Each tester should be a different person, Google account, device and household.** Testers
  on one office or home network are reported to collapse into a single counted tester.
- **Recruit roughly double.** One developer in June 2026 needed **30 testers to keep 12
  active** through the fortnight.
- **The Console counter is not trustworthy.** Repeated reports of it showing 0 with a dozen
  confirmed testers, or lagging ~24 hours. There is **no way to see the consecutive-day
  count**. Do not treat the dashboard as ground truth.
- **Rejection is common and repeatable.** Three, four and five consecutive rejections are all
  documented, including by developers who shipped multiple updates during the fortnight.
- **The review can take far longer than the stated seven days.** Documented 2026 cases of four
  and five weeks, with support saying it cannot escalate or give a date.
- **Do not touch the store listing once you have submitted.** Changes are reported to restart
  the review.

### ⚠️ The specific risk for SajdaTime

**This app is structurally the kind Google rejects on "insufficient user engagement".** It is
designed so that you set it up once and then it notifies you — the better it works, the less
anyone opens it. A privacy app whose author described it as running "silently in the
background" was rejected on exactly this ground in February 2026 and made to serve another
14 days.

Do not let 12 people install it and forget it. Ask testers explicitly to **open the app daily**
during the fortnight — check the times, swing the Qibla compass around, change the calculation
method, export a PDF. And when the production-access form asks what you changed in response to
feedback, **have a real answer**. That free-text box is the part you actually control.

**The owner's plan was to start recruiting testers only once account verification had
cleared**, rather than in parallel. That was a deliberate decision and it should not have been
pressed on earlier — but **verification has now cleared**, so the condition it was waiting for
is gone. Recruiting is unblocked and is the only remaining task that does not depend on the
signing key. It is also the long pole, so it is the one worth starting first.

### The questions you will actually be asked

Not a surprise to leave until the end — the answers are things you can only collect *during*
the fortnight, and there is no going back for them afterwards. Google's own preview of the
production-access form has three parts:

| Part | What it asks |
|---|---|
| **About your closed test** | How easy you found it to recruit testers · tester engagement, including whether they used *all* the features and how that compared with expected real-world use · a summary of the feedback and **how you collected it** |
| **About your app** | Who it is for, specifically · *"Describe how your app provides value to users"* · expected installs in the first year |
| **Production readiness** | What you **changed** as a result of the closed test · how you decided it was ready |

Two of those are traps for this app in particular. *"Whether testers used all features"* is the
engagement question from the risk note above, wearing a different hat — so ask testers to
touch the Qibla compass, the madhab picker and the PDF export, not just let notifications
arrive. And *"what you changed"* has no good answer if nothing was collected: feedback needs
somewhere to land **before** the test starts, because "no one reported anything" reads as
"no one tested it".

> **Already done:** the track's *Feedback URL or email address* field is set to
> `aikstudies@gmail.com`, the same address already published on the store listing. Play shows
> it to testers inside the Play Store entry for the test, so there is a route back that does
> not depend on anyone remembering to ask. A dedicated form was considered and skipped — a
> second inbox to check is a second inbox to forget, and twelve people do not need a database.

### The message to send

Short on purpose. Most of these people will not have been a "tester" before, and the fastest
way to lose one is a wall of instructions. Send this, then send the link.

> *Assalamu alaikum — I've built a free prayer times and Qibla app, no ads and no tracking,
> and Google needs 12 people to try it for two weeks before it can go on the Play Store.*
>
> *It's a couple of taps: open the link on your Android phone, tap Join, then install it like
> any normal app. Please keep it on your phone for the full two weeks — if you uninstall
> early it doesn't count and I have to start again.*
>
> *If you get a minute, have a proper look around rather than just letting the notifications
> come in — the compass, the settings, the timetable. And tell me anything that looks wrong,
> especially prayer times that don't match your mosque. JazakAllah Kher.*

Four things in that message are load-bearing, and every one of them maps to something Google
asks about or checks:

| Line | Why it is there |
|---|---|
| "keep it for the full two weeks" | The 14 days must be **consecutive**. An early uninstall does not just fail to count — it resets that tester |
| "open the link on your Android phone" | Testers must install **from Play**. Never send anyone an APK; it is the single most-cited cause of rejection |
| "have a proper look around" | Google's rejection email leads with *"Testers were not engaged with your app"*. This app is designed to be ignored once it is set up, which is exactly the profile that fails |
| "tell me anything that looks wrong" | This is where the *"what did you change"* answer comes from. Without it, that box is empty |

**Recruit around 25, not 12.** Twelve is the number who must still be opted in at the end, not
the number you start with; one developer needed 30 to hold 12. Different people, different
Google accounts, different households where you can manage it — testers on one home or office
network are reported to collapse into a single counted tester.

**Do not send the link until the release is live on the track.** The join link only exists once
a build has been uploaded and rolled out, and a link sent early is a link that fails for
everyone who tries it first.

### The Wear OS wrinkle

Google has **never published** whether the 12-tester counter aggregates across the phone
track and the separate Wear OS track, or is evaluated per track. Its own requirements page
does not contain the word "Wear" at all. The only sources claiming "phone testers count for
your watch app" are the paid tester services, which sell that exact workaround.

Product Experts on Google's forum have been asked directly for a Wear OS exemption and
declined: *"the 20 testers, 14 days remains"* (February 2024, when the number was still 20).

**Practical reading:** run the closed test on the **phone** track, which is what unblocks
production access. Recruit a few genuine Wear OS owners if you can, but do not let the search
for them hold up the phone launch.

---

## Step 1 — Google Play developer account · **DONE, fully verified**

Created as a **personal** account:

| | |
|---|---|
| Developer name (public) | Ali Imran Khan |
| Account ID | 6284685113064492750 |
| Owner account | aliimrankhan86@gmail.com |
| Developer email (public) | aikstudies@gmail.com |
| Contact email (private) | aliimrankhan86@gmail.com |
| Earning money | **No** — this is what keeps the street address off the public listing |

Public on the Play listing: legal name, country ("United Kingdom"), developer email. The
full address is only shown if you monetise, which this app does not.

### Verification status

| Task | State |
|---|---|
| Access to a real Android device | ✅ **Done** — Play Console app on a physical phone |
| Identity documents | ✅ **Done** — approved by Google |
| Contact phone number | ✅ **Done** — verified once the identity review passed |
| Android developer verification | ✅ **Done** — see below |

**All account verification is complete.** Nothing on the Google side is outstanding. The
account can create and publish apps.

> **Android developer verification is the new one**, and it is the 2025–26 programme that
> extends verification to apps installed *outside* Play. It is easy to miss because it is
> announced on the account home page rather than in the app, and easy to worry about because
> it sounds like a fresh hurdle. It is not: the Console states *"All of your apps have been
> successfully registered to meet Android developer verification requirements."* Registration
> happened automatically for an app already in the Console. It only needs attention if
> SajdaTime is ever distributed off Play — an APK on the website, F-Droid, a direct download —
> in which case that package name has to be registered explicitly.

> The device check is worth remembering for any future account: it requires the **Play
> Console app on a physical Android phone**, and an emulator does not satisfy it. Every
> other test in this project runs on emulators, so it is the one step that cannot.

---

## Step 2 — Create your signing key · **ONLY YOU**

This key proves the app is really from you. **If you lose it you can never update the app
under the same listing.** No assistant should ever generate, hold, or see it.

Use **Play App Signing** (the default and the safer option): Google holds the real signing
key, and you hold an *upload* key. If you lose the upload key, Google can reset it. Without
Play App Signing, losing your key is permanent and unrecoverable.

Run this in Terminal. It does not matter which folder you are in — the key is written to your
home folder on purpose, well away from the project.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && "$JAVA_HOME/bin/keytool" -genkeypair -v -keystore ~/sajdatime-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sajdatime -dname "CN=Ali Imran Khan, O=SajdaTime, C=GB"
```

**It will ask you exactly two things, both the same answer:** `Enter keystore password:` and
`Re-enter new password:`. Type a password you have never used elsewhere. Nothing appears on
screen as you type — that is normal, not a frozen terminal. Then it prints two lines and
finishes.

Three details in that command are deliberate, and this exact form was run and checked with a
throwaway key that was deleted afterwards:

- **`-dname "..."` is what makes it two prompts instead of nine.** Without it, keytool
  interrogates you for name, organisational unit, organisation, city, county and country one
  at a time and then asks you to confirm — a sequence that is easy to abandon halfway, and
  which produces a *worse* result if you answer "SajdaTime" to the question that actually
  means the certificate's common name. None of these values are shown to users anywhere; they
  only have to exist. Change the name in the quotes if you would rather it read differently.
- **The password is *not* in the command.** It could be — keytool accepts `-storepass` — and
  it deliberately is not, because anything typed on a command line is written to your shell
  history in plain text and stays there.
- **`JAVA_HOME` is pinned to 21** because plain `keytool` on this machine resolves to the one
  inside **JDK 11**, while the project builds on 21. Either would in fact produce a keystore
  the build can read, so this is belt-and-braces rather than a bug — but pinning it removes a
  question nobody should have to answer at this stage.

Then:

- **Write the password down somewhere safe and permanent.** A password manager, not a note
  on your desktop.
- **Back up `~/sajdatime-upload-key.jks`** somewhere that is not only this laptop.
- **Never commit it to git.** Keep it outside the project folder — the command above puts it
  in your home folder, which is deliberate.

---

## Step 3 — Wire the key into the build

**The Gradle side is already done.** Both `app/build.gradle.kts` and `wear/build.gradle.kts`
read a `keystore.properties` file from the project root and sign the release build with it.
If that file is absent — which it is on a fresh clone, and for everyone but you — the build
simply produces unsigned bundles instead of failing. This was tested with a throwaway key
and both bundles came out signed, so the wiring is known to work.

All you do is create the file. In the project root, `keystore.properties`:

```properties
storeFile=/Users/aliimrankhan/sajdatime-upload-key.jks
storePassword=<the password you chose>
keyAlias=sajdatime
keyPassword=<the password you chose>
```

`keystore.properties`, `*.jks` and `*.keystore` are already in `.gitignore`, so this file
and your key cannot be committed by accident. Do not move the key into the project folder.

Then build the signed bundles:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:bundleRelease :wear:bundleRelease
```

Output:

```
app/build/outputs/bundle/release/app-release.aab     (~4.3 MB)
wear/build/outputs/bundle/release/wear-release.aab   (~3.4 MB)
```

Play wants `.aab` files (App Bundles), **not** `.apk`.

To confirm a bundle really is signed:

```bash
unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF
```

You should see a `.RSA` and a `.SF` file. If you see neither, `keystore.properties` was not
found or has a wrong path in it.

> Phone is `versionCode = 2`, watch is `versionCode = 1000`. They share an `applicationId`,
> so Play requires every version code to be unique across **both** modules — the number is
> scoped to the app, not to the release track. Each module now has its own band, so bump
> either freely and they can never collide. Every update must *increase* the number; Play
> never accepts the same one twice, and a code can never be reused or lowered.

---

## Step 4 — Privacy policy · **DONE**

GitHub Pages is switched on, serving `main` / `/docs`. Both pages are live and verified:

```
https://aliimrankhan86.github.io/SajdaTime/privacy.html   <- the Play Console privacy URL
https://aliimrankhan86.github.io/SajdaTime/               <- landing page
```

Note this publishes everything in `docs/` — including the handover and architecture
documents. That is fine here (the repo is public already), but be aware of it before putting
anything private in `docs/`.

To re-check it after editing the pages:

```bash
curl -s -o /dev/null -w '%{http_code}\n' https://aliimrankhan86.github.io/SajdaTime/privacy.html
```

---

## Step 5 — Store listing assets · **already prepared**

Everything is in [`docs/store/`](store/). The exact text to paste into each Play Console
field — app name, short description, full description, category, data safety answers — is in
[`docs/store/LISTING.md`](store/LISTING.md).

| Asset | Requirement | Status |
|---|---|---|
| App icon | 512 × 512 PNG | ✅ `docs/store/icon-512.png` |
| Feature graphic | 1024 × 500 PNG | ✅ `docs/store/feature-graphic-1024.png` |
| Phone screenshots | 2–8, ≥1080px short side | ✅ 5 in `docs/store/screenshots/` (1080 × 1920) |
| Wear OS screenshots | ≥1, square | ✅ 2 in `docs/store/screenshots/` (384 × 384) |
| Short description | max 80 characters | ✅ in `LISTING.md` (79) |
| Full description | max 4000 characters | ✅ in `LISTING.md` (2,897) |
| App category | Lifestyle | You pick it in the console |

Regenerate the icon and feature graphic any time with:

```bash
./tools/build-store-assets.sh
```

Screenshots are captured by hand from a running emulator. The prayer times visible in them
were checked against the Aladhan reference API at the time of capture and agreed to within a
minute. Upload the ones directly in `screenshots/`, not the ones in `screenshots/raw/` — the
raw emulator captures are 1080 × 2400, which Play rejects for being more than twice as tall
as it is wide. The script reframes them to 9:16 without cropping anything.

---

## Step 6 — Play Console forms · **DONE** (all green)

Every one of these is complete in the Console. Kept here because they have to be re-checked
after any change that touches data handling, and because two of them were answered wrongly
first time.

1. **Data safety** — submitted. The answers to give, and why, are in `docs/store/LISTING.md`,
   which is the authority on this form: **Yes**, collects **Approximate location**; not
   shared; **not** ephemeral; purpose App functionality only.

   > **The submitted answers were not re-read to write this.** The questionnaire is a
   > five-step wizard whose step numbers are indicators rather than links, so the only way
   > back to the answers is `Next` through all five — and stepping a completed declaration
   > through its own wizard risks dropping it back into draft. It shows as actioned, which is
   > the state that matters, and it is not worth re-opening a green form to admire it. If you
   > ever do need to read the answers back, use **Export to CSV** at the top of the page
   > rather than the wizard.

   > ⚠️ **This step used to say two things that were wrong**, and both were the kind of wrong
   > that gets an app pulled after it is already live.
   >
   > It said *"no data is collected"*. Google's definition is mechanical — *"'Collect' means
   > transmitting data from your app off a user's device"* — and a typed city name leaves the
   > device. That is collection, whatever our intentions, and answering No is a misdeclaration.
   >
   > It then said to *"declare it as processed ephemerally"*. **Do not tick that box.** The
   > standard requires the data be retained no longer than needed to service the request, and
   > Open-Meteo's own terms say *"All log files will be deleted after a period of 90 days."*
   > Our handling is ephemeral; the round trip is not. Google has never resolved whose
   > retention it means, and guessing in your own favour on an unresolved ambiguity is exactly
   > what gets an app removed later.
   >
   > Declaring it honestly costs almost nothing: the public card gains one line, on an app
   > whose whole pitch is privacy. **Over-declaring is never a violation. Under-declaring is.**

2. **Content rating questionnaire** — submitted. No objectionable content.
3. **Target audience and content** — **13+**, not aimed at children.
4. **Ads declaration** — **No**.
5. **Government apps / financial features / health** — all no.
6. **App access** — no login; all functionality available without restriction.
7. **Advertising ID** — **No**. Re-check the merged manifest before ever changing this; a
   Play Services library can pull `AD_ID` in transitively and the mismatch is a hard error.

---

## What this app does NOT have to worry about

Researched against Google's live policy pages in July 2026. Each of these is a thing solo
developers routinely lose days to, and none of them applies here. **Do not let a future
session "fix" any of them.**

| Worry | Verdict |
|---|---|
| `SCHEDULE_EXACT_ALARM` needs a Play declaration | **No.** Only the *restricted* `USE_EXACT_ALARM` needs one, and that is limited to dedicated alarm/timer/calendar apps. Our permission is the user-granted, unrestricted one. **Do not "upgrade" to `USE_EXACT_ALARM`** — it would buy a non-revocable grant in exchange for a restricted-permission review and a new rejection vector. |
| `USE_FULL_SCREEN_INTENT` | Not declared and not used. It *does* require a declaration. Alarm features often reach for it — ours must not. |
| Coarse location needs a declaration | **No.** The Location Permissions form is for *background* location. Ours is coarse and foreground-only. |
| The October 2026 "minimum scope" location policy | Applies to `ACCESS_FINE_LOCATION`. We are coarse-only — already the posture that policy is pushing everyone toward. |
| targetSdk deadline (31 August 2026) | ✅ Compliant. New apps need API 36; we are at 36. Wear needs 35+; we are at 36. |
| 16 KB page size (mandatory since Nov 2025) | ✅ Automatic. Pure Kotlin, no native code. |
| Android developer verification (from 30 Sept 2026) | ✅ Nothing to do. Google auto-registers the package when you create the app in Console. |
| A religion-specific Play policy | **There isn't one.** Religion appears in Play policy only as a *protected characteristic* — we are a beneficiary of that clause, not a target. No religious declaration, no special content-rating answers. Expect Everyone / PEGI 3. |
| Being rejected for using AI to write the code | **No such policy exists.** Google's AI-Generated Content policy governs what an app *does at runtime* — chatbots, image generators — not how the source was authored. We ship no generative AI, so it does not apply. There is **no obligation to disclose AI assistance to Google**, and no policy hook for it. (The disclaimer in the app and the listing stays regardless — that is our own honesty commitment to users, not a Play requirement.) |
| Rejection for a "saturated category" | **That is an Apple rule being mistaken for a Google one.** Play's Repetitive Content policy targets copying a specific app's content, or one developer shipping many near-identical apps. Neither applies. |
| EU DSA trader/non-trader declaration | **Play has no such form.** That is Apple's requirement, February 2025, widely mis-attributed to Google. Google satisfies the same regulation upstream by publishing developer name, country and email. |
| D-U-N-S number | Organisation accounts only. Not needed for a personal account. |

**The one genuine 2022 precedent worth knowing:** Google removed a batch of prayer-times and
Qibla apps that year. The trigger was a **surveillance SDK** harvesting phone numbers and
locations, not religious content. It is why this category carries extra reviewer attention
around data declarations — which is precisely why the data safety answers in
`docs/store/LISTING.md` are worth getting exactly right. Our zero-SDK build is the strongest
possible answer to that suspicion.

---

## Step 7 — Upload and roll out

1. ✅ **DONE** — Play Console → **Create app**, name **SajdaTime**, free, app (not game).
   **"Free" is a one-way door.** Google: *"Once your app has been offered for free, the app
   can't be changed to paid."* That is exactly what this app wants, but know it is permanent.
   The `applicationId` is likewise permanent from the first upload.
2. ✅ **DONE** — store listing (Step 5) and every form (Step 6), all green.
3. ✅ **DONE** — **Testing → Closed testing → Countries/regions**: all **177** countries now
   show *Targeted* on the `Closed testing - Alpha` track, confirmed on a reload.

   > This was genuinely empty, and it is the trap it looks like. Closed tracks inherit
   > production availability, and production availability had never been set, so **every**
   > tester anywhere in the world would have hit *"app not available in your country"* — with
   > the opted-in count sitting at zero and nothing on screen explaining why. It is the single
   > most common reason a closed test appears broken, and it fails silently on the tester's
   > phone rather than in the Console where anyone would see it.
   >
   > All countries is right for a closed track and costs nothing: only invited testers can
   > install regardless. It also means a tester who travels does not drop out mid-fortnight.

4. ✅ **DONE** — the track's **Feedback email** is set to `aikstudies@gmail.com`, so testers
   have a route back that does not rely on remembering to ask them. See Step 0.
5. ⬜ **Select testers** — the last piece of track setup, and the only one that needs
   information nobody else has: the actual email addresses. **Testing → Closed testing →
   Testers → Create email list.** Each tester needs the Google account they use on their
   phone, not just any address. Recruitment copy is in Step 0.
6. ⬜ Same track → **Releases → Create new release** → upload **`app-release.aab` only**
   (needs the signing key, Step 2).
7. ⬜ Share the opt-in link — **only once the release is live** — and wait out the 14 days.
8. ⬜ Apply for production access, then **Production → Create release** and roll out.

### The watch bundle does NOT go in the same release

**This corrects an earlier version of this document, which said to upload both bundles into
one release. Play will refuse it.** Since March 2023 Wear OS has its own release track, and
Google's [Manage different form factor releases on dedicated
tracks](https://support.google.com/googleplay/android-developer/answer/13295490) is explicit:

> "If you want to continue distributing your app to Wear OS devices, you must use dedicated
> Wear OS tracks and create new releases on these tracks."

Putting `wear-release.aab` in the phones track produces the error *"This APK or bundle
requires the Wear OS system feature android.hardware.type.watch. To publish this release on
the current track, remove this artifact."*

The watch still shares the listing, the `applicationId` and the signing key — that part was
always right, and Google requires it (Wear quality rule WO-G7). Only the *track* is separate.
The order is:

1. Ship the phone app first. Nothing about it depends on the watch.
2. **Test and release → Advanced settings → Form factors → Add form factor → Wear OS.**
   (Not under Grow → Store presence.) This is gated on already having a release in a closed
   testing track.
3. Upload the Wear screenshots, then **Manage → Use a dedicated release track for Wear OS**.
4. Upload `wear-release.aab` with the form-factor selector set to **Wear OS**, not phones.
5. Agree to the Wear OS review policy. This triggers a **separate human review** against the
   [Wear OS app quality guidelines](https://developer.android.com/docs/quality-guidelines/wear-app-quality),
   on top of the normal app review. Outcome shows as Pending / Approved / Not approved.

**Version codes are unique per app, not per track.** Phone is `2`; the watch is `1000` and
climbs in its own band, so the two sequences can never collide. See `wear/build.gradle.kts`.

### What the Wear review actually checks

Google publishes its own list of the most common Wear OS rejections. Against this app:

| Rejection cause | Where we stand |
|---|---|
| Listing doesn't say **"Wear OS"** | ✅ The full description says "A Wear OS app and watch tile". Must be the exact phrase — "WearOS" and "Android Wear" both get rejected. **This applies to every localised listing too.** |
| No Wear OS screenshot | ✅ Two, 384 × 384, 1:1, no device frame, no added text — all as required |
| Basic functionality broken / screenshots inaccurate | ✅ `w1-times.png` was recaptured at a scroll position where the bottom edge falls between rows rather than through a glyph. The list still shows it continues below, which is how a ScalingLazyColumn is meant to look, but nothing is chopped. |
| Not formatted for round displays (WO-V16) | ✅ **Both sizes.** 192dp (`sajdawear`) and 227dp (`sajdawear_large`, 454px at 320dpi, corner radius 227 = a true circle) at font scale 1.0 and 1.3, every screen, checked by pixel rather than by eye — see below. |
| Font scaling (WO-V1 / WO-V14) | ✅ `font_scale 1.3` on **both** round sizes. One real defect was found and fixed at 1.3 (the school button printed under the watch face clock); see the note below for the residual. |
| Black background (WO-V13) | ✅ Confirmed in the screenshots |

> **How to re-run this, in one command.**
>
> ```bash
> ./tools/wear-verify.sh
> ```
>
> It creates the 227dp AVD if it is missing, boots both watches, installs the debug
> build, walks every screen at font scale 1.0 and 1.3, and runs `tools/wear-round-check.py`
> over the captures. `screencap` grabs the framebuffer *before* the rounded-corner overlay,
> so anything outside the inscribed circle in a capture is something the wearer never sees —
> which makes WO-V16's "cut off by screen edges" a thing arithmetic can settle. 28 captures,
> all clear.
>
> **What it cannot settle, and what that cost.** WO-V16 also says nothing may *overlap*.
> The watch face clock is painted by the system on top of the app; both are lit pixels, and
> no pixel check tells them apart. The school button sitting underneath the time was found
> by looking at the screenshots, not by the checker. Look at the captures.
>
> **The shortcut that does not work — do not repeat it.** Forcing the small AVD to the
> larger size with `adb shell wm size 454x454` looks like it should do the job. It does not.
> The corner overlay is computed once for the panel the device was created with, so after
> the resize the mask sits at the wrong radius and paints **straight vertical cuts** through
> the text — "2:54 A|M" and so on. A round display cannot produce a straight vertical edge,
> so those artefacts are the stale mask rather than the app, and the capture is not evidence
> of anything. The answer is a second AVD on a genuine `wearos_large_round` profile, which
> `wear-verify.sh` now creates.
>
> **Residual, stated plainly.** On the *192dp* watch at font scale 1.3, scrolled to the very
> bottom of the times list, the first line of the watch disclaimer passes under the clock.
> Nothing is unreachable — one flick up shows it in full — and it is body text rather than a
> control. It is not fixable by padding: at the scroll extreme the list is anchored from
> below, and at that size and font the content is simply taller than the glass. 227dp is
> clear at both font scales.

Two Wear hypotheses worth *not* worrying about: tiles and complications are **not** required
for approval, and rotary-input support stopped being a requirement in February 2024.

> **Deadline: 15 September 2026 — 64-bit support becomes mandatory for Wear OS apps.** This
> app is pure Kotlin with no native libraries, so it should be unaffected, but confirm before
> that date.

---

## Can an assistant fill the Play Console in for you?

Partly, and it is worth knowing where the line is before you start.

**It cannot, and will not:** create the developer account, enter your card for the $25 fee,
type any password, accept Google's Developer Distribution Agreement, or click the final
"Publish". Those are account creation, payment and legal consent, and they are yours alone.

**It can, once you are signed in yourself:** read a page back to you and explain what a field
is asking, paste in the listing text from `docs/store/LISTING.md`, and walk the data safety
questionnaire with you answer by answer. Attaching the screenshots and the graphics is a file
picker on your own machine, so that is quicker by hand anyway.

The honest summary: the Console is roughly an hour of form-filling, and having every answer
written down in advance — which it now is — saves far more time than automating the clicks.

---

## Before every future update

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --rerun-tasks \
  :core:testDebugUnitTest :app:testDebugUnitTest :wear:testDebugUnitTest \
  :app:lintDebug :wear:lintDebug :core:lintDebug \
  :app:bundleRelease :wear:bundleRelease
```

Then bump `versionCode` (and usually `versionName`) in **both** `app/build.gradle.kts` and
`wear/build.gradle.kts`.

---

## What is genuinely blocking, right now

The developer account is **fully verified** — identity, phone and device checks have all
passed — GitHub Pages is live, and **the store listing and store settings are saved and clean
in the Console**, checked on a reload from Google's servers rather than from the page as left.
Every App content declaration is green. Nothing is waiting on Google.

Play's dashboard states the remaining path in four steps, and shows the app sitting on the
first of them:

1. Publish a closed testing release
2. Have at least 12 testers opted in — **`0 testers currently opted in`**
3. Run the closed test with at least 12 testers for at least 14 days
4. Apply for production

The closed testing track is otherwise set up: `Closed testing - Alpha` exists, all 177
countries are targeted, and the feedback email is set. Which reduces to **two** real tasks:

1. **The signing key** (Step 2) — one `keytool` command, two password prompts, plus a
   `keystore.properties` file. Nobody else should ever generate or hold this: not a
   contractor, not an assistant, not an AI agent. **This is the only thing blocking a release
   build**, and it depends on nothing and nobody else.
2. **Twelve testers** (Step 0) — no longer blocked on anything either, now that verification
   has cleared, and unlike the key it cannot be done in an afternoon. Recruit roughly 25,
   because the count is of testers who *stay* opted in for 14 consecutive days. The message to
   send them is written out in Step 0.

They are independent, so they should run in parallel. The key unblocks the upload; the testers
are the 14-day clock, and that clock is the long pole. It cannot be shortened, only started
earlier.

Everything else is done and in the repo: the live privacy policy, the store screenshots, the
icon, the feature graphic, all the listing text, and the Gradle signing wiring.
