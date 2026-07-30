# Releasing SajdaTime to Google Play

Written for the owner, who is not a developer. Follow it top to bottom. Anything marked
**ONLY YOU** cannot be done by an assistant or anyone else.

---

## Step 0 — Before anything else: the 12-tester rule

If you create a **personal** (individual, not company) Google Play developer account, Google
requires you to run a **closed test with at least 12 testers who stay opted in for 14
continuous days** before you may apply for production access.

This applies to accounts created from late 2023 onward. It is the single biggest surprise
for solo developers and it means **you cannot publish publicly on day one**. Plan for it:
line up 12 people (family, friends, the mosque) who will install the app from a test link
and leave it installed for two weeks.

Check the current rule in the Play Console when you sign up, because Google adjusts it.

---

## Step 1 — Google Play developer account · **DONE, verification pending**

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

### Three verification tasks remain

1. **Identity documents** — uploaded, Google is reviewing. Takes a few days.
2. **Access to a real Android device** — install the **Play Console app** on a physical
   Android phone and sign in as the owner account. **An emulator will not satisfy this** —
   defeating emulators is the point of the check. If you do not own an Android phone you
   must borrow one; publishing is impossible without it.
3. **Contact phone number** — locked until the identity documents are approved.

Track all three at <https://play.google.com/console> → Home.

---

## Step 2 — Create your signing key · **ONLY YOU**

This key proves the app is really from you. **If you lose it you can never update the app
under the same listing.** No assistant should ever generate, hold, or see it.

Use **Play App Signing** (the default and the safer option): Google holds the real signing
key, and you hold an *upload* key. If you lose the upload key, Google can reset it. Without
Play App Signing, losing your key is permanent and unrecoverable.

Run this in Terminal, in the project folder:

```bash
keytool -genkeypair -v \
  -keystore ~/sajdatime-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias sajdatime
```

It will ask for a password and some details (name, organisation, city, country). Then:

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

> Phone is `versionCode = 2`, watch is `versionCode = 3`. They share an `applicationId`, so
> Play treats them as one listing and refuses two bundles with the same version code — keep
> the watch one ahead of the phone on every future bump. Every update must also *increase*
> the number; Play never accepts the same one twice.

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

## Step 6 — Play Console forms · **ONLY YOU** (answer honestly)

You must complete all of these before you can publish:

1. **Data safety.** The honest answers: no data is collected, no data is shared, no account
   is required, nothing is sold. **One nuance to declare accurately** — if a user types a
   city name instead of using location, that name is sent once to a free geocoding service
   to look up coordinates. It is not stored, not linked to the person, and the app tells
   them before it happens. Declare it as processed ephemerally rather than pretending
   nothing leaves the device.
2. **Content rating questionnaire.** Straightforward; the app has no objectionable content.
   Expect a "Everyone / PEGI 3" style rating.
3. **Target audience and content.** Not aimed at children.
4. **Ads declaration.** **No**, the app contains no ads.
5. **Government apps / financial features / health** — all no.
6. **App access.** No login required; state that all functionality is available without
   restriction.

---

## Step 7 — Upload and roll out

1. Play Console → **Create app** → name **SajdaTime**, free, app (not game).
2. Fill in the store listing (Step 5) and all the forms (Step 6).
3. **Testing → Closed testing** → create a release → upload **both** `app-release.aab` and
   `wear-release.aab` into the **same release**. They share an `applicationId`, so they
   belong to one listing; Play delivers the right one per device.
4. Add your 12 testers by email, share the opt-in link, and wait out the 14 days.
5. Apply for production access, then **Production → Create release** and roll out.

Tick the box that says the app supports **Wear OS**, and attach the watch screenshots, or
it will not be discoverable on watches.

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

The developer account exists and GitHub Pages is live. Three things remain, all needing the
owner personally:

1. **The signing key** (Step 2) — one `keytool` command plus a `keystore.properties` file.
   Nobody else should ever generate or hold this. **Nothing else in the project is waiting
   on anything but this.**
2. **Android device verification** (Step 1, task 2) — the Play Console app on a real phone.
   No emulator. Cannot publish without it.
3. **Google's identity review** (Step 1, task 1) — out of everyone's hands, a few days.

Then the 12-tester clock (Step 0) starts, and that is the long pole — line testers up now
rather than waiting for the rest.

Everything else is done and in the repo: the live privacy policy, the store screenshots, the
icon, the feature graphic, all the listing text, and the Gradle signing wiring.
