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

## Step 1 — Google Play developer account · **ONLY YOU**

1. Go to <https://play.google.com/console> and sign up.
2. Pay the **one-off $25** registration fee.
3. Complete identity verification (photo ID). This can take a few days.
4. Choose **personal** or **organisation**. Personal is fine for a charity project, but it
   triggers the 12-tester rule above. An organisation account needs a D-U-N-S number.

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

Create a file called `keystore.properties` in the project root:

```properties
storeFile=/Users/aliimrankhan/sajdatime-upload-key.jks
storePassword=<the password you chose>
keyAlias=sajdatime
keyPassword=<the password you chose>
```

**Add `keystore.properties` to `.gitignore` before you create it.** It contains your
password. This step is not yet done in the repo — an assistant can wire up the Gradle
`signingConfig` for you once the key exists, since that part contains no secrets.

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

> ⚠️ Phone and watch currently both have `versionCode = 2`. If Play rejects the upload for a
> duplicate version code, bump the watch one in `wear/build.gradle.kts` and rebuild. Every
> future update must increase the version code — Play refuses to accept the same number twice.

---

## Step 4 — Privacy policy · needs a public URL

Play requires a **publicly reachable privacy policy URL**. A file in the repo is not enough;
it needs to be a web page.

Easiest free option: turn on **GitHub Pages** for the SajdaTime repo and publish a single
`privacy.html`. You then use
`https://aliimrankhan86.github.io/SajdaTime/privacy.html` as the URL.

The content is easy because the app genuinely collects nothing. An assistant can write it
for you — say the word.

---

## Step 5 — Store listing assets

| Asset | Requirement | Who |
|---|---|---|
| App icon | 512 × 512 PNG, 32-bit | Assistant can export from the vector icon |
| Feature graphic | 1024 × 500 PNG or JPG | Assistant can make one |
| Phone screenshots | 2–8, at least 1080px on the short side | **Assistant can capture these from the emulator** |
| Wear OS screenshots | at least 1, square, needed to be listed on Wear | **Assistant can capture these** |
| Short description | max 80 characters | Assistant can draft |
| Full description | max 4000 characters | Assistant can draft |
| App category | Lifestyle | You pick in the console |

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

Only **Steps 1–3**: the developer account and the signing key. Both need you personally.
Everything else — the privacy policy, the screenshots, the graphics, the descriptions, the
Gradle signing wiring — can be prepared in advance by an assistant.
