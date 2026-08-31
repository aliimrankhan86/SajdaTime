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

### Who the testers are: Google will not tell you, and the field will not check you

Two related surprises, both verified in the Console on 1 Aug 2026. Between them they account
for most of the time lost chasing a closed test that appears not to work.

**You get a number, never a name.** There is no per-tester view anywhere in Play Console: no
list of who opted in, who installed, who uninstalled. Google treats that as the testers'
private data. The Statistics page will eventually show installs and active devices, but
aggregated and roughly a day behind — on 1 Aug, a day after going live, it read *"Data
unavailable"*. **So the only way to know who is in is to ask them and keep your own list**:
name, email, added, opted in, confirmed installed. Nothing in the Console reconstructs it,
and nothing in the app can either — it has no analytics, no accounts and no server, and that
is not going to change.

**The email field validates nothing.** The Console accepts any string that looks like an
address. It does not check that it is a Google account, and it cannot check that it is the
account signed into the Play Store on that person's phone. Add the wrong one and it sits in
your list of 17 looking correct while the tester sees *"not available"* — the whole failure
is on their phone, and the Console shows you a healthy list either way. This is why the count
of opted-in testers is always lower than the count of addresses, and why the gap tells you
nothing about which addresses are wrong.

So ask for it precisely, and in their words:

> Open the Play Store app, tap your photo in the top right. What address does it show at the
> top? That's the one I need.

A hotmail, outlook, iCloud or work address only works if it has been registered as a Google
account **and** is the one signed into that phone. It occasionally is. Asking for the Gmail
is the version that does not generate confused replies.

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
- **The Console counter exists in exactly one place — and it vanishes the moment you pass.**
  **Dashboard → Production → "Apply for access to production"**, as a line of italic text
  under step 2: *"9 testers currently opted in"*. Nothing else in the Console shows it —
  not the track page, not the Testers tab, not Statistics, not the release. There is
  **no way to see the consecutive-day count**, and there are repeated reports of the number
  reading 0 with a dozen confirmed testers or lagging ~24 hours, so treat it as a lower
  bound rather than ground truth.

  **On reaching twelve, that italic line is replaced by a tick and the number is never shown
  again** — verified by screenshot on 1 Aug 2026, when it disappeared between two readings on
  the same day. So throughout the fourteen-day run, which is the one stretch where dropping
  below twelve actually costs you, there is no figure to watch and no warning if it slips.
  The Testers tab still shows a number, but that is *addresses added* and always the larger
  one. **Keep your own list of who confirmed they installed.** The Console will not tell you,
  and the failure is silent.
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

### If 12 testers cannot be found — there is no way round it

Asked directly. The short answer is that **the closed test is unavoidable**, and the longer
answer is a correction, because the obvious escape route does not exist.

**Nothing bad happens if it never completes.** Google's documentation sets no deadline, no
expiry and no penalty. The app sits where it is indefinitely, and the listing is not at risk.

> ### ⚠️ Open testing is NOT a fallback. This document said it was, and that was wrong.
>
> The reasoning looked sound. Google's requirements page names exactly what gets disabled:
>
> > "Certain features in Play Console, such as Production (Test and release > Production) and
> > Pre-registration (Test and release > Testing > Pre-registration), will be disabled until
> > developers meet these requirements."
>
> Open testing is not in that list. Its own help page says *"users can find your test app on
> Google Play"*, with no tester list and unlimited testers — which reads like public
> distribution without the 12-tester rule.
>
> **The Console disagrees, and the Console is what actually decides.** The Open testing track
> on this app carries a padlock and one sentence:
>
> > "Open testing is available when you have production access. To learn what you need to do
> > before you can apply for production, visit the Help Centre."
>
> Confirmed on a forced reload, not a stale render. So open testing sits *behind* production
> access, which sits behind the closed test. Google's own documentation and Google's own
> product contradict each other here, and the product wins.
>
> **The lesson, since this is the second time it has been paid for:** when a question is about
> what the Console will let you do, open the Console. A support page describes policy, not the
> button state on this account. See §15 lesson 26 in `docs/HANDOVER.md` — recommending from a
> list nobody read is the same mistake in a different costume.

The other documented exemption is an **organisation account**, which is not subject to the
rule at all. Noted so nobody has to re-discover it, not recommended: it needs a registered
business and a D-U-N-S number, and the app would have to be transferred.

**So the only route is twelve real people, and that is more achievable than it sounds for this
app in particular.** Testers do not need to be technical, or strangers, or know what a beta
is — they need an Android phone and to want prayer times, which describes nearly everyone this
would be sent to. One mosque WhatsApp group is twelve people.

> **Avoid the free tester-swap communities**, where developers install each other's apps. They
> are legitimate, unlike the paid services warned about above, and they are still the wrong
> tool here: they produce people who install once and never open it again, and *"Testers were
> not engaged with your app"* is the precise rejection this app is already most at risk of.
> They would make the problem worse while appearing to solve it.

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
>
> **The stakes, recorded 6 Aug 2026, because "nothing to do" is not the same as "nothing at
> risk".** Google sends blanket reminder emails about this, and the next one will look
> alarming to anyone who only has the verdict above and not the numbers behind it:
>
> - **Deadline: 30 September 2026.** Play Console help is blunt — *"Effective September 30,
>   2026, all Play packages must be registered"*, and packages that are not are **removed
>   from Play globally**. That is the real consequence, and it is worse than the framing in
>   most coverage, which describes it as an install block in the launch countries.
> - **Enforcement starts in Brazil, Indonesia, Singapore and Thailand**, across seven stores
>   (Play, HONOR, OPPO, Galaxy Store, Palm Store, V-Appstore and Xiaomi's GetApps). Global
>   in 2027.
> - **Identity verification is not a second step.** Google: *"Your existing verified identity
>   … meets this requirement."* Ours cleared in July 2026.
> - **A new app created in the Console registers its package name on creation.** That is why
>   `com.sajdatime.app` needed no action and why a future app will not either.
>
> **ADB is explicitly exempt, and that matters more here than it sounds.** Google:
> *"Developers and power users can still use Android Debug Bridge (ADB) to build, test, and
> install modified or unverified apps on their own devices, which remains the standard method
> for development work."* This whole project's device testing is `adb install` onto the
> owner's Xiaomi, and `app/build.gradle.kts` creates two package names that exist only for
> that — `…app.rtl` and `…app.sideload`. **Neither should ever be registered.** They are
> debug variants that are never distributed, and registering them would put two junk package
> names on the account permanently for no benefit.
>
> **Read back in the Console by the owner, 31 Aug 2026, and this is the read-back that
> matters.** *Play Console → Android developer verification → Package names* shows **1 package
> name**: `com.sajdatime.app`, status **Registered**, **3 keys**, last updated **31 Jul 2026**.
> Three keys is expected rather than a mistake: Play App Signing holds both the app signing key
> and the upload key, and this page lists the keys associated with the package, not one key per
> app. The date is the day the app was created in the Console, which is what "registered
> automatically on creation" looks like from the outside. **So every reminder email on this
> subject can be dismissed**, including the "[Final reminder]" of 31 Aug 2026, unless and until
> SajdaTime is distributed off Play, at which point that package and the key that signs it have
> to be registered explicitly on this same page using **Register package name**.

> The device check is worth remembering for any future account: it requires the **Play
> Console app on a physical Android phone**, and an emulator does not satisfy it. Every
> other test in this project runs on emulators, so it is the one step that cannot.

---

## Step 2 — Create your signing key · **ONLY YOU** · **DONE, 31 Jul 2026**

> **Done.** The owner ran the command below himself on 31 Jul 2026.
> `~/sajdatime-upload-key.jks` exists, 2,654 bytes, permissions tightened to `600`. The
> assistant confirmed only that the file exists and is the right size — it has never been
> opened, and the password has never been in any transcript. Play App Signing is confirmed
> **on** for this app ("Releases are signed by Google Play" appears on the release page), so
> if this upload key is ever lost Google can reset it.
>
> The instructions are kept below because they are needed again if the key is ever replaced.

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

> If a third prompt ever appears — `Enter key password for <sajdatime> (RETURN if same as
> keystore password):` — press Return on its own. It does not appear with the modern PKCS12
> keystore format, which is the default from JDK 9 onwards and is what this command produces,
> because PKCS12 requires the key and store passwords to match. It is listed here only so
> that seeing it is not alarming: `keystore.properties` sets both passwords to the same value
> either way, so pressing Return is always the right answer.

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

## Step 3 — Wire the key into the build · **DONE, 31 Jul 2026**

> **Done.** `keystore.properties` exists in the project root, `600`, all four keys present,
> `storeFile` resolving to the real key. `git check-ignore` confirms it is ignored by
> `.gitignore:26`, so it cannot be committed. The assistant read only the *key names* out of
> it, with the values masked by `sed`; the password has never entered a transcript.
>
> Proven rather than assumed: `./gradlew clean test lint :app:bundleRelease
> :wear:bundleRelease` then ran `:app:signReleaseBundle` and `:wear:signReleaseBundle` —
> tasks which had silently not existed before — and both `.aab` files now contain
> `META-INF/SAJDATIM.SF` and `META-INF/SAJDATIM.RSA`. Before the key existed, the same
> command produced bundles with no signature block at all and said nothing about it. See
> §10 of `docs/HANDOVER.md`.

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

> Phone is `versionCode = 3` (bumped 2 Aug 2026), watch is `versionCode = 1000`. They share an `applicationId`,
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
| Full description | max 4000 characters | ✅ in `LISTING.md` (3,704) |
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

> ### ⚠️ The current screenshots are stale for the *next* release, deliberately
>
> **Do not retake them yet, and do not upload the new build without retaking them.** Two
> changes made on 2 Aug 2026 alter the home screen, and `01-times-light.png` shows both:
>
> | What changed | Effect on that screenshot |
> |---|---|
> | T1 method banner | It is Greater Manchester at 53.5°N on the default method, so a fresh install now shows an amber "Does this match your mosque?" card that is not in the picture |
> | T3 composite place name | The header now reads "Greater Manchester, United Kingdom", not "Greater Manchester" |
>
> They are left alone on purpose. A store screenshot has to match the build a user actually
> downloads, and none of this is uploaded — 1.1.0 is live and the closed test is still
> running. Retaking now would make the listing wrong for the app that is live today in order
> to be right for one that does not exist yet, and Play treats inaccurate screenshots as a
> policy problem in its own right (see the Wear rejection table below).
>
> So this is a **release-time step, not a now step**: retake as part of the same change that
> uploads the new AAB, in the order build → capture → upload, and check the banner is either
> present in every phone screenshot or absent from all of them rather than mixed.

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
| Android developer verification (from 30 Sept 2026) | ✅ **Done, and read back in the Console on 31 Aug 2026: `com.sajdatime.app`, Registered, 3 keys, last updated 31 Jul 2026.** Nothing to do, but know the stakes. Google auto-registers the package when you create the app in Console, and the Console confirms ours is registered. An *un*registered package is removed from Play **globally** on 30 Sept 2026, not merely blocked in the launch countries, so re-read the box in Step 1 before dismissing any reminder email as noise. ADB installs are exempt by name, so the `.rtl` and `.sideload` debug variants need nothing and must not be registered. |
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
5. ✅ **DONE** — **Select testers.** Email list `sajdatime - testers` exists with **13
   testers** and is ticked against the `Closed testing - Alpha` track, confirmed on a reload
   from Google's servers. The track checklist now reads *Select testers* ✓.

   > This took three attempts, and the failure mode is worth knowing. The dialog has two
   > fields that look like one: you type addresses into **"Add email addresses"**, but they
   > only move into **"Email addresses added"** — the field that is actually required — when
   > you press **Enter**. Until then the list looks full and **Save changes** stays greyed
   > out with no explanation. Then, separately, saving the list does *not* attach it: the
   > list appears in the table with its checkbox **unticked**, and the checklist still says
   > *Select testers* incomplete. Two distinct steps, each of which looks finished before it
   > is.

6. ✅ **DONE** — **Releases → Create new release**, `app-release.aab` uploaded and accepted
   as **version 2 (1.1.0)**, API 24+, target SDK 36, 4 ABIs, 1.64 MB install size. Release
   name `1.1.0 - first closed test` (internal only, never shown to users); release notes
   written for `en-GB`. Saved as a draft, then saved again from the *Preview and confirm*
   step, and both fields re-read from the server after a full page reload.

   One warning is raised and is safe to ignore: *"This App Bundle contains native code, and
   you've not uploaded debug symbols."* The only two `.so` files in the bundle are Google's
   own — `libandroidx.graphics.path.so` (Compose) and `libdatastore_shared_counter.so`
   (DataStore). There is no native code of ours, so there is nothing to symbolicate.

7. ✅ **DONE, 31 Jul 2026** — the owner pressed **"Submit 15 changes for review"** on
   **Publishing overview**. Managed publishing is **off**, so approval publishes straight to
   the closed track with no second button to press.

   > Play runs automated pre-checks before anything reaches a human — *"Running quick checks
   > for commonly found issues… Up to 6 minutes remaining."* They passed with nothing
   > flagged. **Submission activity** now records **Submission 1**, 15 changes, source *Play
   > Console*, submitted **31 Jul 2026, 22:00**, status **In review**, covering `Closed
   > testing - Alpha`, Store Listing, App Content and Store settings. The Publishing overview
   > heading changed from *"Changes not yet submitted for review"* to *"Changes in review"*.
   >
   > The submission-details page is a receipt, not a form: there is nothing on it to press
   > and nothing on it changes until Google decides. **Do not edit the store listing,
   > screenshots or any App content answer while it is in review** — each edit becomes a new
   > pending change and can put you back in the queue.
   >
   > Editing the *tester email list*, by contrast, is safe and creates no pending change:
   > the list went from 13 to 15 addresses after submission and the Publishing overview
   > still showed only *"Changes in review"*, with no new section to submit. Verified by
   > scrolling the whole page, not by assuming.

   > **Approved. Verified in the Console on 1 Aug 2026.** The review came back inside a day,
   > not the seven that were budgeted for. `Closed testing - Alpha` now reads **Active**,
   > release `1.1.0 - first closed test` shows a green tick and **"Available to selected
   > testers · 1 version code · Released on 31 Jul 22:17"** across all 177 countries, and the
   > Dashboard has struck through *Publish a closed testing release*. There was no second
   > button to press and no notification in the Console — managed publishing being off means
   > approval simply publishes, which is why the state can change without anything appearing
   > to have happened.

8. ⏳ **Share the opt-in link and wait out the 14 days.** The links are live as of 1 Aug 2026
   — both read directly out of the Console's own DOM rather than assumed:

   | Console label | URL |
   |---|---|
   | Join on the web — **send this one** | `https://play.google.com/apps/testing/com.sajdatime.app` |
   | Join on Android | `https://play.google.com/store/apps/details?id=com.sajdatime.app` |

   The first is the *"Become a tester"* page and is the one that works for someone who has
   not opted in yet. The store link only resolves for people already opted in, so sending it
   to a new tester produces a page that says the app does not exist.

   **State on 1 Aug 2026, later the same day: cleared.** The list reached **24 addresses**
   and the Dashboard now ticks *"Have at least 12 testers opted-in to your closed test"* and
   strikes it through. **The fourteen days are now running.** The 14 days are counted while
   **twelve or more** are opted in — Google's own wording is *"Run your closed test with at
   least 12 testers, for at least 14 days"* — so the remaining risk is not recruitment, it is
   **retention**: if the count slips below twelve the run is compromised, and as noted in
   Step 0 the Console stops displaying the number once you pass, so nothing will warn you.
   Do not read 24 as the opt-in figure; it is addresses added.

   > **Google sends testers nothing.** No invitation email, no notification. Adding an
   > address to the list only grants access; the person has no way to know the app exists
   > until the owner messages them. Until the release is live the link does not work at all
   > and the Console says so: *"The link will be shown here when you publish your app."*,
   > with **Copy link** greyed out. Send it to everyone on the same day so the fortnight
   > starts together.

   A message that can be sent as-is:

   > Assalamu alaikum. I've built a free prayer times and Qibla app called SajdaTime — no
   > ads, no accounts, no tracking, and it works without internet. I need a few people to
   > test it before Google will let me release it publicly.
   >
   > Two things, and it's only a couple of minutes:
   >
   > 1. Open this on your Android phone: https://play.google.com/apps/testing/com.sajdatime.app
   > 2. Press "Become a tester", then install the app.
   >
   > One favour — please **leave it installed for two weeks** and open it now and then.
   > Google checks that. If you uninstall it early it sets me back.
   >
   > If the link says you're not a tester, tell me which Gmail address is on your phone and
   > I'll add it.
   >
   > JazakAllah khair.

   The three ways it fails on the tester's phone, all of which look like a broken app:

   - **"You're not a tester."** They are signed into a different Google account than the one
     on the list. The address must be the Google account actually signed into the Play Store
     on their phone — not a work address, not one they never use. Ask, add, done.
   - **Opted in, but the app will not install.** Normal. It can take a few hours, sometimes
     up to a day, to propagate. Tell them to retry later rather than conclude it is broken.
   - **They install it and remove it a week later.** The expensive one. The count is of
     testers who *stay* opted in for 14 consecutive days, so a late uninstall can drop the
     count below twelve and restart the clock. This is why the target is twenty-plus rather
     than exactly twelve.

   **The two messages actually used (1 Aug 2026).** The long message above assumes the
   recipient is already on the list. Recruiting someone new is a *two-step* conversation,
   because the link cannot work until their address has been added — send it up front and
   they will click it, be told they are not a tester, and quietly give up. So the first
   message asks only for the address, and deliberately contains no link:

   > Assalamu alaikum,
   >
   > I've made a free prayer times app called SajdaTime — prayer times, adhan notifications
   > and Qibla compass. No adverts, no tracking, works without internet.
   >
   > It is for Android phones only. It won't work on an iPhone, sorry.
   >
   > Google won't let me publish it until 12 people have tested it. Can you help?
   >
   > If you have an Android phone, please send me your Gmail address — it has to be the
   > Gmail signed in to the Play Store on your phone. Only Gmail works for this. A hotmail,
   > outlook or work email will not.
   >
   > Once I've added you I'll reply with the link. Please keep the app on your phone for 15
   > days — Google needs 12 people to keep it that long before I can release it publicly.
   >
   > Thank you. Jazakallahu khairan.

   Then, once the address is on the list:

   > You're added. Open this on your Android phone:
   > https://play.google.com/apps/testing/com.sajdatime.app
   > Tap "Become a tester", then install from the Google Play link on that page. Let me know
   > when it's done.

   Three deliberate choices in there. **"Android phones only"** is stated first because the
   iPhone owners in any WhatsApp group will otherwise reply asking, and each one costs a
   round trip. **"15 days"** rather than 14 is padding for a late opt-in — the rule is 14
   consecutive days and there is no benefit in cutting it fine. **"Gmail only"** is a
   simplification and is known to be slightly wrong (see "Who the testers are" in Step 0):
   a non-Gmail Google account can work. It is phrased that way because the failure is
   silent and the accurate version generates confused replies.
9. ⬜ Apply for production access, then **Production → Create release** and roll out.
10. ⬜ Second release for the watch, once Play unlocks the Wear OS form factor — see below.

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

**Version codes are unique per app, not per track.** The phone is on `4` (live on production
since 20 Aug 2026) and the watch on `1001`, each climbing in its own band, so the two sequences
can never collide. See `wear/build.gradle.kts`.

### What the Wear review actually checks

Google publishes its own list of the most common Wear OS rejections. Against this app:

| Rejection cause | Where we stand |
|---|---|
| Listing doesn't say **"Wear OS"** | ✅ The full description says "A Wear OS app and watch tile", **confirmed on the live public listing 31 Aug 2026**, not merely in the source file. Must be the exact phrase — "WearOS" and "Android Wear" both get rejected. **This applies to every localised listing too.** |
| No Wear OS screenshot | ✅ Two, **454 × 454**, 1:1, fully opaque, no device frame, no added text. Google's rule is a **384 × 384 minimum**, not a fixed size; an earlier version of this row said the files were 384 × 384, which was simply wrong about them. Measured 31 Aug 2026, see Step 9 |
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
>
> **Re-verified 2 Aug 2026** after `wear_disclaimer` was reworded, on all 24 captures across
> both round sizes and both font scales: 24/24 pass the bezel check, and the four scroll-extreme
> captures read 192dp@1.0 clear, 192dp@1.3 residual as above, 227dp@1.0 clear, 227dp@1.3 clear.
> **The reword had to be measured, not proofread**, and the first attempt at it failed: a
> *shorter* string wrapped to four lines instead of three and put the residual on 192dp@1.0 as
> well, where there had been none. Wrapping is by word, so character count does not predict it —
> 88 chars fitted three lines, 85 needed four, 78 fitted. Any edit to a watch string is a
> layout change. Run this script.

Two Wear hypotheses worth *not* worrying about: tiles and complications are **not** required
for approval, and rotary-input support stopped being a requirement in February 2024.

> **Deadline: 15 September 2026 — 64-bit support becomes mandatory for Wear OS apps.** This
> app is pure Kotlin with no native libraries, so it should be unaffected, but confirm before
> that date.

---

## Step 8 — Telling the testers, once production is live

**This is the step nothing in the Console prompts you to take, which is why it was missed.**
The app went live on 20 Aug 2026 and the closed testers were told nothing for eleven days.
They are not customers. They are the twenty-four people whose opt-ins bought production
access, and silence leaves them assuming the project stalled.

### First, the thing that is almost certainly not a problem

When a tester says *"the Play Store is not showing an update"*, the usual answer is that
**they already have the new version.** Auto-update is on by default, so within days of a
production release most testers are quietly moved onto it. Play then shows **Open** rather
than **Update**, and a tester hunting for an Update button is hunting for something that
cannot exist.

**The app settles this itself, which is part of why the version is on screen.** Settings →
About → **Version** reads the installed `versionName` straight from `PackageManager`
(`SettingsScreen.kt`, `versionName(context)`). **Ask for that number instead of asking whether
they updated.** It is the one question a non-technical tester can answer correctly, and it
does not depend on them understanding what a version is.

### The track is not the cause, and this was checked rather than reasoned

It is tempting to assume an opted-in tester is pinned to the closed track and therefore stuck
on the old build. **They are not.** Google's own wording, from *Set up an open, closed, or
internal test*:

> "All users are eligible to receive the Production track. If an app bundle with a higher
> version code is published to the Production track than to a test track where a user opted
> in, the user receives the production release."

Production is `versionCode` 4 and the closed track is on 2, so every tester is entitled to
1.2.0 and Play will deliver it. **Nothing has to be halted, promoted or reconfigured**, and the
closed track can be left running as a beta lane for the next release. Internal testing is the
one exception to that rule and behaves differently, but this project has never used it.

Two ordinary Play Store behaviours do delay delivery, and both look like a fault when they are
not: the Updates list refreshes on Play's own schedule rather than on demand, and many phones
are set to auto-update on Wi-Fi only. **The app's own store page always shows the true state
and the Updates list does not**, so send the store link rather than directing anyone into a
menu.

### The message to send when production goes live

Recruitment is a different message and lives in Step 0. This is the one for the day the app
goes public. Written for people who are not technical, short enough for WhatsApp, and it asks
for a version number rather than asking a yes/no question nobody can answer reliably:

> Assalamu alaikum,
>
> SajdaTime is now live on Google Play for everyone. Thank you for testing it. It would not
> have got there without you.
>
> You may already have the latest version without realising, because phones update apps
> automatically. To check: open SajdaTime, go to **Settings**, scroll down to **About**, and
> look at **Version**.
>
> If it says **1.2.0**, you are up to date and there is nothing to do.
>
> If it says **1.1.0**, open this on your phone and tap Update:
> https://play.google.com/store/apps/details?id=com.sajdatime.app
>
> If you do not see an Update button there, swipe the page down to refresh it, and check again
> later. Some phones only update apps when connected to Wi-Fi.
>
> What is new:
> • Fixed a bug that could stop the Dhuhr alarm going off
> • A new step that checks your calculation method matches your mosque
> • Redesigned Qibla compass, much clearer at a glance
> • Shows your town name on the times screen
> • Fixed the compass vibrating on its own
>
> Now that it is public, please do share it with family and friends. It is free, no ads, no
> tracking, and it always will be.
>
> JazakAllahu khairan,
> Ali

**Deliberately not in it: the dua request.** `CLAUDE.md` puts that in the app's disclaimer and
*nowhere else*, asked once and never repeated. Broadcasting it to twenty-four people would
break the rule, and the rule exists precisely because that kind of ask is worth less every
time it is repeated.

**Say plainly that the testing link is no longer the route.** Once the app is public,
<https://play.google.com/store/apps/details?id=com.sajdatime.app> works for anyone, and the
old `/apps/testing/` link only confuses people who are now ordinary users.

### Update the version numbers in this message before reusing it

The two version numbers above are the only part that goes stale, and a message telling someone
to look for a number that is no longer current is worse than no message. On the next release,
change them and change the "What is new" bullets to match the release notes actually filed in
the Console, so the two cannot disagree.

---

## Step 9 — The Wear OS release: pre-flight audit, 31 Aug 2026

The mechanics have not changed and are in Step 7, under *"The watch bundle does NOT go in the
same release"*. **This step is the readiness audit run before handing the job over**, so the
Console session is short and nothing is discovered halfway through it.

### What was checked, and against what

| Check | Result | How |
|---|---|---|
| The live listing contains the exact phrase **"Wear OS"** | ✅ Present: *"A Wear OS app and watch tile that work on their own, with or without your phone"* | Read off the **live public listing**, not `docs/store/LISTING.md`. This is the single most common cause of Wear rejection, and a source file agreeing with itself proves nothing about what Google is actually serving |
| Screenshot count | ✅ Two. Google requires **at least one** | `docs/store/upload/wear-os/` |
| Size and aspect ratio | ✅ 454 × 454, 1:1. Google's wording is *"a 1:1 aspect ratio and with a minimum size of 384 x 384 pixels"*, so **384 is a floor, not a target** | Measured, not eyeballed |
| No transparency or masking | ✅ Alpha is 255 at every pixel of both files, zero non-opaque pixels | Measured. A round-watch capture saved as RGBA is exactly where a transparent corner would hide, and Google's rule is *"Don't include transparent backgrounds or masking"* |
| Black background (WO-V13) | ✅ All four corners of both files are `(0, 0, 0, 255)` | Same measurement |
| No device frame, no added text | ✅ Raw `screencap` output, never composited | Provenance: `tools/wear-verify.sh` |
| `wear-release.aab` is current | ✅ Built 20 Aug 08:02, and **no file under `wear/src` or `core/src` is newer than it**, so it carries `versionCode` 1001 / `1.2.0` | `find -newer` against the bundle, which is the check the stale-artifact note asks for |

### Build it fresh anyway

The bundle above is provably current, and it should still be rebuilt, for a reason that has
nothing to do with the bundle: **the rebuild re-runs the tests and the lint.** The last green
run was 20 Aug and nothing has changed since, so it will pass. "It should pass" is the sentence
that precedes most of the lessons in §15.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :wear:bundleRelease
```

The file lands at `wear/build/outputs/bundle/release/wear-release.aab`.

### A18 is not optional, and it guards the worst failure this release could have

**The phone-to-watch settings sync has never been observed working end to end.** If it does not
work, the watch calculates on its own Shafi'i default while the phone is set to Hanafi, and the
two disagree about **Asr by roughly an hour**. That is not a minor defect. A prayer app that
contradicts itself on the same wrist is worse than no watch app at all, and it is the exact
symptom that exposed this problem the first time (§11 item 7).

The check takes a minute once a watch is paired: set the phone to **Hanafi**, open the watch,
compare **Asr**. Equal means the sync works. An hour apart means it does not, and the school
button at the foot of the watch's times list will say which school it actually used. **Do this
before uploading, not after.**

### What to expect from the review

Unlike the phone release on 20 Aug, this one really does involve waiting for a decision. Adding
the form factor triggers a **separate human review** against the Wear OS app quality
guidelines, on top of the normal app review, and the outcome shows as Pending / Approved / Not
approved. Lesson 105 still applies to the approval half: do not wait on an email.

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
`wear/build.gradle.kts`. Version codes are unique **per app, not per track**: the phone is
on `4`, the watch on `1001`, so the two sequences climb in separate bands and can never
collide.

### Shipping an improvement *during* the closed test

This is the likely next thing to happen, so the mechanics are written down rather than
rediscovered.

1. Bump `versionCode`, build, and upload into the **same `Closed testing - Alpha` track** —
   `Create new release`, not a new track. A new track would mean a new set of testers and a
   new fortnight.
2. It goes through review again. Subsequent reviews are generally quicker than the first,
   but it is still a review, and the same rule applies: **do not edit the store listing or
   any App content answer while one is open.**
3. **This does not reset the 12-tester clock.** The counter tracks tester opt-in, not
   releases. Researched and stated in Step 0; **not yet observed on this account** — the
   first update shipped during a live fortnight is the thing that will confirm it, so record
   what actually happens to the count when it does.
4. Testers do not re-opt-in. They are already on the track and Play updates them like any
   other app — expected behaviour, not yet watched happen here.
5. Shipping fixes mid-test is *positively useful*: the production-access form asks what you
   changed in response to feedback, and a real answer is worth more than a tidy one.

Once production access exists, the same build goes to **Production → Create release**
instead, and the closed track can keep running ahead of it as a beta if that is wanted.

---

## What is genuinely blocking, right now

> **Nothing is blocking. The app is LIVE on Google Play production, confirmed 31 Aug 2026.**
> Everything below this box is the history of how production access was obtained, kept because
> the mechanics are worth having written down, not because it describes today. The current
> status lives in `docs/HANDOVER.md` §11.

**The app is approved and live on the closed testing track.** Google's review came back
inside a day. The developer account is fully verified, GitHub Pages is live, the store
listing and every App content declaration are saved and green, the bundles are genuinely
signed, and `Closed testing - Alpha` is **Active** with `1.1.0 - first closed test`
distributed to all 177 countries.

Play's dashboard stated the path in four steps, and **all four are now done**:

| | Step | State |
|---|---|---|
| 1 | Publish a closed testing release | ✅ done — struck through on the Dashboard |
| 2 | Have at least 12 testers opted in | ✅ done, 1 Aug 2026 — 24 addresses on the list |
| 3 | Run the closed test with 12 testers for at least 14 days | ✅ done — completed 14 Aug 2026 |
| 4 | Apply for production | ✅ **granted — confirmed by email from Google, 20 Aug 2026** |

**Step 9 is done, and the app is live.** `versionCode` 4 (1.2.0) was built fresh from `main`,
uploaded to Production, and the store listing screenshots replaced. The owner submitted it for
review himself on 20 Aug 2026, 1:39pm. Google approved it the same day and, because managed
publishing is off, it published immediately with no second confirmation screen.
**Confirmed independently on 31 Aug 2026** from the public store page,
<https://play.google.com/store/apps/details?id=com.sajdatime.app>, which loads to a signed-out
request and shows "Updated on Aug 20, 2026", "10+ Downloads", a plain Install button, no testing
banner of any kind, and release notes that match 1.2.0 and nothing else. **Google sent no
approval email, and none was coming**, which is worth knowing before waiting for one again:
`docs/HANDOVER.md` §15 lesson 105. What is left is telling the closed-test testers to update,
and the Wear OS track. See `docs/HANDOVER.md` §11 for the up-to-date detail. This section is
kept for the history of how access was obtained, not as the current status.

**The single most important thing on this page: the 14 days are counted while twelve or more
are opted in.** The clock is now running, so the risk has changed shape — it is no longer
recruitment, it is **retention**, and it is invisible. On passing twelve the Console stopped
printing the count (Step 0), so if people uninstall and the number slips there is no figure
to check and no notification. Everything below follows from that.

1. ✅ **Twelve or more opted in.** Cleared. Note that the Testers tab's 24 is *addresses
   added*, not opt-ins — do not confuse the two (§15 lesson 36), and note that Google sends
   the people on that list nothing at all (§15 lesson 35).
2. ⏳ **Fourteen consecutive days with twelve or more still opted in.** The long pole,
   and the one thing that cannot be shortened. Aim well above twelve: the count is of
   testers who *stay*, and Google's rejection email leads with *"Testers were not engaged
   with your app"*, so they have to use it rather than merely accept. Ask them to open it,
   swing the compass, change the madhab, export a PDF — see the risk note in Step 0, which
   is specifically about this app.
3. ⬜ **Apply for production access**, answer the three-part form (Step 0), wait roughly
   another 7 days, then **Production → Create release**.

Nothing else is waiting on anyone. Everything that can be prepared is in the repo: the live
privacy policy, the store screenshots, the icon, the feature graphic, all the listing text,
the Gradle signing wiring, and both signed bundles.

> **The watch is no longer blocked. Verified 1 Aug 2026.** This section previously recorded
> that Advanced settings → Form factors was gated — *"Once you've released your app to any
> track, you can come back here to manage its availability"*. Publishing the closed test
> satisfied that gate. The page now lists **Android XR · Active** (a default, not something
> we asked for — it is why the track summary reads "Phones, Tablets, Chrome OS, Android XR")
> and offers **+ Add form factor**, enabled.
>
> **Deliberately not clicked.** Adding a form factor is a configuration change that creates
> a pending change to submit, and it belongs to the owner, not to an agent poking at a
> Console. So what is verified is only that the button is live; the Wear OS option itself,
> the wizard behind it, and the 454×454 screenshot slots are **still unseen**. The assets are
> ready at `docs/store/upload/wear-os/` and `wear-release.aab` is built and signed.
>
> **Do it after the phone reaches production, not now.** The watch triggers a separate human
> review against the Wear quality guidelines, and there is nothing to gain from putting a
> second review in flight while the fortnight is the only thing that matters.
