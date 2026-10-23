# SajdaTime — instructions for any AI working on this project

This file is loaded automatically. It is deliberately short. **The full handover is
[`docs/HANDOVER.md`](docs/HANDOVER.md) — read it in full before changing any code.**

---

> **Memory:** every session also reads and updates the shared GBrain page `projects/sajdatime` (`recall` it at the start; write what is done and what remains at the end). It carries the owner's standing decisions and rules between sessions; `docs/HANDOVER.md` stays the source of truth for the repo. **Keeping GBrain current is Claude's job, never the owner's** (his words, 19 Oct 2026: "You are responsible to keep gbrain up to date not me"). A cloud session has no GBrain connection, so it writes everything into `docs/HANDOVER.md` §11 and pushes; **the first session with GBrain afterwards compares GBrain with the §11 STATE OF PLAY and brings GBrain up to date itself, without being asked.** Never hand him a prompt whose only job is to update GBrain.

---

## ⚠️ First thing, every session — before you ask him anything

**Open [`docs/HANDOVER.md`](docs/HANDOVER.md) §11 and read the `STATE OF PLAY` block at the top
of it.** It is dated. It says what is true right now, what is left and whose job each item is,
how to check every claim without a Play Console session, the traps that have actually cost this
project time, and what an assistant can and cannot do with the tooling here. Everything below
its `HISTORY BELOW THIS LINE` marker is the dated record of how the project got here: read it
for reasoning, never as a task list. The STATE OF PLAY block is rewritten as part of any commit
that moves the project, so it will not be the stale sentence this file is forbidden from
carrying.

**Do not invent work from the older blocks, or from any "Not done" list further down §11.** Most
of it is closed, and the rest is a deliberate non-goal. If the state block leaves you with
nothing to do, that is the correct answer: ask the owner what he wants next.

**Then confirm what is actually live, before acting on anyone's claim that something is done.**
Fetch the public listing, signed out of every Google account:

```
https://play.google.com/store/apps/details?id=com.sajdatime.app
```

The **"Updated on"** date moves when a release publishes and for nothing else, so it separates
*live* from *everyone involved feels ready* in two seconds, with no credentials and no Console
session. Play sends **no email** when a review passes, so an inbox proves nothing in either
direction. Both of those are lessons this project paid for: `docs/HANDOVER.md` §15, 105 and 107.

**When he asks whether the app is ready for everyone — and he will, because he has asked
several times — the answer is yes, and the proof is Play search, not the Console.** A closed
test cannot appear in search at all, so a hit is only possible if the app is public:

```bash
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/search?q=SajdaTime&c=apps&hl=en_GB&gl=GB" | grep -c "com.sajdatime.app"
```

**Say the second half in the same breath or the question returns:** it is *available* to
everyone but not yet *discoverable* — searching "prayer times" or "qibla" does not surface it,
because Play ranks on installs and ratings and this app has few of one and none of the other.
That is not a fault, not a setting, and **not work to invent**. §15 lesson 113. (Since 17 Oct
2026 the app may show Google's own rating card, at the owner's word; see the rule below.)

**The owner should not have to tell you any of this, and has asked not to have to.** He does
not want to paste a briefing at the start of a session. This file is loaded automatically so
that he never has to. Keep it that way: when the pick-up point moves, move it in
`docs/HANDOVER.md` §11 rather than adding status here.

---

## What this project is

A free, ad-free, privacy-first Muslim prayer times and Qibla app for Android and Wear OS,
built as an ongoing charity (*sadaqah jariyah*) for the Ummah. It is not a product, there is
no business model, and there never will be one. Every decision follows from that: no ads, no
accounts, no server of our own, and no revenue. The one thing the app can send is **optional,
opt in usage counts to Google Analytics, off by default**, approved by the owner on 3 Oct 2026 so
he can see how the app is used and plan the next phases (`docs/ANALYTICS_PLAN.md`, owner sign off
in its section 11). That is a narrow exception, not a licence: see the usage counts rule below.

It has to work **for the masses** — including a phone with no signal, an old device, a user
who has never changed a setting, and a user who cannot read English. When a trade-off appears
between elegant and dependable, dependable wins.

---

## The commit rule — always, every time

**When you commit, commit the understanding, not just the diff.**

Code records *what* changed. It cannot record why, what was tried and rejected, what was
verified versus assumed, or which decisions are deliberate and must not be "improved" back
in. That reasoning is the thing that is expensive to rebuild and easy to lose, and losing it
is how the next session confidently undoes a decision it did not know was made on purpose.

So, as part of every commit that changes behaviour:

1. **Write a commit message that explains the reasoning**, not the file list. Say what was
   wrong, how it was found, why this fix and not another, and what you verified. Git already
   knows which lines moved.
2. **Update `docs/HANDOVER.md`** — it is the single source of truth. Add new business rules
   to §5, new verification evidence to §10, new pending items to §11, and anything that must
   never be reverted to §2 "Deliberately NOT used" or §11 "Deliberate non-goals".
   **When what is live, what is next or an open decision changes, also update
   `docs/PROJECT_BRIEF.md`** (one page, plain words). The owner's claude.ai project reads it live
   from `main` on GitHub so he never has to re-upload anything (his request, 17 Oct 2026). On a
   release branch, update it there; it reaches the project when he merges.
3. **Record rejected options, with the reason.** A decision without its rationale looks like
   an oversight and will be reversed.
4. **Comment the non-obvious in the code itself**, where the next reader will actually be
   standing. Especially anything that looks wrong but is right.
5. **Say what you did not test.** An honest gap is useful. A silent one is a trap.
6. **Push it.** Work that only exists on this machine is work that can be lost.

If a change taught you something — a platform trap, a wrong assumption, a bug class that
reading could never have caught — that lesson belongs in `docs/HANDOVER.md` §15 so the next
session inherits it instead of relearning it.

---

## Hard rules — these are not preferences

- **An iPhone version is planned, so every decision now must keep that road smooth.** Ali, 23 Oct 2026: "I do
  have aspiration later to make it this app for iOS as well ... So it doesn't look like a design mess, as Apple has
  often objection about the design aspect of the apps", and then: "take into consideration for any development even
  though we are not developing for the iphone right now ... so when we develop ios app then things are still smooth
  for us." Nothing is built for iOS yet, and no cost or complexity is added for it now. But:
  - **Design:** every screen, icon and asset also fits Apple's Human Interface Guidelines, because App Store Review
    Guideline 4 (Design) and 4.3 (look-alike apps in a crowded category) are how Apple rejects apps like this one.
    Only patterns both platforms share (no floating action button, no navigation drawer, a visible back or close on
    every screen), an SF Symbol named beside each Material icon, 48dp targets (meets Apple's 44pt), text that
    scales, a distinct look of our own, assets licensed for both stores. Fable (`ux-lead`) owns this check.
  - **Code and data:** when two options cost about the same, take the one that carries over to iPhone. New rules
    and calculations (tracker, dates, jama'ah, fasts) go in `:core` as plain Kotlin with no `android.*` import;
    stored data uses simple formats (plain values or JSON), not Android only ones; no new Android only library
    where a portable one does the same job. No Kotlin Multiplatform move or iOS code until Ali decides.
  - **Every plan says its iPhone effect in one line** (none, carries over, or Android only and why), and Android
    only features (widgets, Wear OS, exact alarms) are labelled as such. `plan-verifier` and `reviewer` check it.
  - **Known gaps today, recorded not fixed** (23 Oct 2026): `:core` is plain Kotlin except `AppLocale.kt` and
    `PrayerLabels.kt` (Android `Context`); it uses `java.time` including `HijrahDate`, and the Java `adhan`
    library, none of which run on iPhone as they are. An iPhone version would swap these; it does not block now.
  Full rule: `docs/HANDOVER.md` §5.28 and `.claude/agents/ux-lead.md` on `claude/1-6-0`.

- **Watch testing is finished. Never ask him to test the watch.** He tested it himself on
  1 September 2026, the Qibla included, reported it fine, and asked explicitly that it not be
  raised again. The times half was proved separately on 31 Aug, when his own paired watch and
  his phone were shown to agree on Asr (handover A18). Older documents in `docs/` still contain
  sentences like *"the phone-to-watch sync has never been observed working"* and *"the watch
  Qibla has never been seen on real hardware"*; every one of them is dated, superseded, and
  annotated as such. **They are history. Do not act on them, and do not put a watch check in
  front of him on their authority.** Raising it again spends trust that a real defect report
  will need. **He reconfirmed this unprompted on 7 September 2026** — *"watch testing is done
  and record it"* — which is the third time of asking. The watch app has been live on Google
  Play since 5 September; if something is genuinely wrong with it, it will arrive as a user
  review, and the daily health check is watching for exactly that. **Treat any impulse to ask
  him to check the watch as a bug in your own reasoning.**

- **The religious disclaimer must never be removed, softened, or buried**, and the dua
  request in its final paragraph belongs there and *nowhere else* — the user is asked once
  and never nagged, so no copy of ours ever asks for anything else.
- **Rating: Google's card only, rarely, never in our words.** The owner reversed the old "no
  rate prompt" rule on 17 Oct 2026 ("We need rate this app since we are trying to better ratings
  and be promoted"). He asked for a weekly reminder "or other best practice"; research said no
  to weekly (Google's quota swallows asks under a month apart, and Play policy names forced
  rating pop ups). Built: `review/ReviewPrompt.kt`, Play In-App Review card after 14 days kept
  and 3 days opened, only on a calm Times screen away from prayer times, at most every 120 days
  and 3 times ever, plus a passive "Rate SajdaTime on Google Play" row in About. **No "do you
  like it?" question, no button that calls the card, no usage counts event, no app copy asking
  for a rating, no religious wording.** A passive "Share SajdaTime" row sits beside it (owner,
  17 Oct 2026). Both are doors the user opens; nothing in the app ever suggests sharing or rating.
- **Play reviews: the owner replies, Claude drafts.** Agreed 17 Oct 2026. When he sends a
  screenshot of a review, draft a short, kind reply on the issue raised. Never ask for a higher
  rating in a reply (Play policy).
- **Translations: Claude translates and checks its own work; no native speaker review.** The
  owner decided this on 9 Oct 2026 (SajdaTime is a free utility he makes himself), superseding
  the old "never machine-translate" rule. Urdu, Turkish and Indonesian shipped in 1.3.1
  (`values-ur`, `values-tr`, `values-in`). Never tell users a translation awaits review, and
  never ask him to find a reviewer. Correct any mistake a user reports. Prayer and madhab names
  are religious content, so take particular care with them, and the religious disclaimer is
  never softened in any language. Indonesian's folder is `values-in`, not `values-id`: Android
  hands the app the legacy code, so `values-id` is packaged and never matches. **Every language
  must stay packaged in the app** (`bundle.language.enableSplit = false`): with Play's default
  language splits, the in-app picker cannot switch to a language the phone itself is not set to
  (`docs/HANDOVER.md` §15 lesson 125).
- **The app follows the phone's language, and goes right-to-left only when it has the
  words to.** `AppLocale` reads the app's language out of the resources. Urdu (1.3.1) and
  Arabic (`values-ar`, 1.5.0, owner's request 17 Oct 2026) are right-to-left; Arabic's
  `app_language_tag` is `ar-u-nu-latn`, so its digits stay Western on every Android version
  (java.time and `String.format` disagree otherwise). A phone set to any language we have no
  words for stays English and left-to-right — English laid out right-to-left is genuinely
  wrong, and the owner has ruled it out. Two tests in `LocaleDisciplineTest` fail the build if
  either half slips. Since 1.5.0 the user can also pick a language in the app on every Android
  version ("Phone language" first and the default; below 13 through `AppLocale.setOverride`), so
  anything that names the language must go through `AppLocale.wrap` at the moment of use. See
  `docs/HANDOVER.md` §5.16 and §15 lessons 152 and 153.
- **Never call the Aladhan API — or any network — from the shipped app to calculate times.**
  Verifying against it during development is correct and encouraged. Calling it at runtime is
  not, and the reasons are in `docs/HANDOVER.md` §2. The owner was asked directly and said no.
- **Never generate, hold, request, or view the release signing key.** It is the owner's
  alone. See `docs/RELEASING.md`. `*.jks`, `*.keystore` and `keystore.properties` are
  gitignored and must never be committed.
- **Location stays on the device, and the developer never receives it.** Coarse only, foreground
  only. The app sends no coordinates to any server of its own or of anyone it chose to call, and
  the prayer times never need the network. **One caveat, found 16 Oct 2026 and now stated in
  `docs/privacy.html`:** the town name label comes from Android's `Geocoder`, which the platform
  documents as possibly network backed, and on phones with Google Play services it is answered by
  Google. So "never leaves the device" is not true and must not be written again. The app now asks
  only when it has no label or has moved 5 km (`PlaceLabelPolicy`). The full fix, no network at
  all, is an offline nearest city list, which is the owner's decision (HANDOVER 5.21). (For a user
  who has opted in to usage counts, Google estimates an approximate area from their internet
  connection; the app sends nothing for that.) Cloud backup and device-to-device transfer are both off.
  `docs/privacy.html` is a published promise, so any change that touches data handling has to be
  reflected there in the same commit.
- **Usage counts are opt in, closed, and never about belief.** Off until the user says yes, with a
  consent text that states the facts in full (`UsageCounts.kt`, the `consent_*` strings, and
  `docs/privacy.html` must agree). **The owner asked for on-by-default on 4 Oct 2026; it was built, then reversed
  the same day on the evidence in `docs/HANDOVER.md` §10 (ICO statistical purposes exception needs individual
  data deleted after aggregation, which Firebase cannot do). Do not rebuild it without that evidence changing.** A Pakistan-only country split was built and removed the
  same day (no users there, extra risk); do not reintroduce a country list without a written source and a real user base. Only the fixed events in `UsageCounts.kt` may be sent: no new
  event, parameter or user property without the owner's agreement and a privacy policy change
  first. **Never send the user's sect, madhab, calculation method, alert or prayer settings, city
  or coordinates, and never anything whose *shape* reveals them** (the madhab step is shown to
  Sunni users only, so it has no event). Only the release build may send. The word "tracking" is
  not used in the app's copy. `UsageCountsTest` enforces most of this; do not weaken it.
- **Do not move this project into `~/Documents`, `~/Desktop`, or any iCloud-synced folder.**
  iCloud writes conflict copies into `app/build/` and breaks dexing. It cost three separate
  debugging sessions.

---

## How to work here

- **The quality bar is the owner's, and it is not negotiable** (17 Oct 2026: "make sure its all done
  to a good standard ... so nothing is done of bad standard and UX design, clean, easy to use and
  read"). Nothing is finished until it meets `docs/DESIGN_SYSTEM.md` (Quiet Glass): its components and
  Material roles only, even spacing, the one line height rule, short human accurate copy with no
  hyphens or em dashes, checked on the emulator in light, dark, 2x text and Urdu right to left, the
  full gate and device suite green, an independent review of the diff before commit, and every gap
  written down. Answer the review questions in `DESIGN_SYSTEM.md` §10 for every screen you touch.

- **Rules he has had to repeat, so never again: read `docs/DESIGN_SYSTEM.md` §0 before any visual
  change.** It lists the mistakes that were repeated and why, and the rules that stop them:
  - **Ask him first when you are not sure** (§0.2 lists exactly when), with `AskUserQuestion`, your
    recommendation first, screenshots for anything visual. He asked for this on 18 Oct 2026 ("why
    can't you ask me if you are not sure"). Guessing and then reporting it as done is the failure.
  - **Never write "done", "every" or "all" until the §4.1 checks have run and you have pasted the
    counts** (§0.3). Twice on 18 Oct 2026 a rule was reported as applied everywhere when it was not.
  - **Every action is a solid button with white bold text, light and dark**; the only text links
    are "Close" and "Got it" at the foot of a dialog, plus his one chosen exception: the usage
    question's two bold underlined links ("See exactly what is sent", "Read the privacy policy"). The full spec, every button in the app and the
    checks are §4.1; `DesignRulesTest` fails the build on a breach, including a button in a file the
    inventory does not list.
  - Gaps come from `Space` by relationship (§2). Since 18 Oct 2026 that holds for every `Spacer`, column
    spacing and top or bottom padding in the phone app's screens, and `DesignRulesTest` fails the build on
    one written as a number. **Not covered, so never claim them:** horizontal gaps inside a row, a
    component's own padding (a row's height, a pill, a card's edge), and the watch app.
  - No two icon buttons side by side; every dismissable dialog has the close X.
  - **§11 lists the decisions still waiting for him.** Do not build an answer to an OPEN item.
  - Before he tests on the emulator, give the install command and the commit he should see. Never
    let him judge a change on a stale install.
- **Compiling is not working. Run it.** The watch app once compiled and linted clean for an
  entire release while being unable to ever obtain a location.
- **Before any layout change, check it right-to-left — and read the result as a *layout*
  check.** `./gradlew :app:installRtl` runs the whole app in RTL without a translation or a
  device setting. Roughly half the world's Muslims read right-to-left, and the first Arabic
  or Urdu translation should not be the moment anyone finds out whether the screens survive
  it. But it shows **English words in a right-to-left paragraph**, which always looks
  broken, because it is: stranded full stops, meridiems on the wrong side. Judge the
  mirroring — margins, chevrons, nav order, clipping — and ignore the prose. **Do not put
  its screenshots in front of the owner as findings.** He has reported it as broken three
  times, correctly each time, and every one of those rounds spent trust that a real RTL
  report will need. Report what the check *concluded*, not what it looked like.
- **Run the phone and watch emulators together and compare them.** The worst bug in this
  project's history lived in the gap between two modules that were each individually correct.
- **Verify against an independent reference, not your own reasoning.** Every real calculation
  bug here was caught by comparing to Aladhan; three confidently-reported "bugs" turned out to
  be errors in the test rather than the code, so check both sides.
- **Measure the fix, do not just read it.** A modifier change that looked right and reviewed
  clean did nothing at all until it was screenshotted.
- **Colours come from `docs/DESIGN_SYSTEM.md`, never from a hex in a composable.** Every
  one is a Material 3 role, and `ColorContrastTest` fails the build if a pair drops below
  WCAG AA. A visual change is not finished until `docs/store/screenshots/` is retaken.
- **Ponytail discipline:** stdlib and platform first, no speculative abstractions, shortest
  working diff. Mark deliberate simplifications with a `ponytail:` comment.
- **The owner is not technical.** Explain in plain language, distinguish verified from
  assumed, never call something done when it is untested, and never ask him to go and test
  it on a device himself and report back.
- **Anything he has to do himself in a web UI, walk him through it one screen at a time.**
  Handing over a numbered list does not work, and it has been tried often enough to stop
  trying: on 31 Aug 2026 the six Play Console steps for the Wear release were given three
  times and read each time as a description of something already finished rather than as
  instructions to follow. Ask him to open the page and send a screenshot, tell him what to
  click **on that screen**, ask for the next one. He sends screenshots readily and this is the
  format that fits him. **Stop before any submit, publish, purchase or consent button and hand
  it to him** — the 20 Aug production submission was deliberately left for him to press, and
  agreeing to Google's policies is his signature, not an assistant's.
- **The same dictation pattern applies to the emulator and `adb`, not only the Play Console.**
  Verified 1 Sept 2026: a chat/Console bridge session cannot run `adb`, the emulator, or
  Gradle — its shell only reaches the mounted project folder, nothing else on the machine.
  Give one command block, ask him to paste back exactly what it printed, react to that,
  repeat. Do not hand over a multi-step sequence and assume it ran. The full sequence,
  including the module-scoping trap that cost a session, is in
  [`docs/HANDOVER.md`](docs/HANDOVER.md), "Getting the app onto an emulator when the
  assistant has no device access", just above the build-traps table.
- **From a cloud session, releases go through his Mac in one command and through Claude in Chrome.** A
  cloud session cannot push, run Gradle on his Mac or drive the Play Console itself. How 1.3.2 was shipped on
  11 Oct 2026, end to end, is in `docs/HANDOVER.md` §15 lessons 128 and 129, with the script kept at
  `docs/release-tools/release-1.3.2.sh`.
- **He does have real devices, and they find things the emulators cannot.** A Redmi Note 13
  Pro (HyperOS) and a Galaxy S23 Ultra (One UI), which he will plug in if asked. Every
  alarm-reliability fact this project knows came off the Redmi, and the S23 Ultra found both
  a geocoder bug and a layout bug on 15 Aug 2026 that twenty emulator probes had missed. So
  **ask for the phone when the change touches layout, location, alarms or permissions.**
  What to ask for, in this order, because he will not know the terms: *Settings → About
  phone → Software information → tap Build number seven times*, then *Settings → Developer
  options → USB debugging on*, then pick **File Transfer / Android Auto** on the USB
  notification, then **Allow** on the "Allow USB debugging?" box. Also ask him to turn on
  *Developer options → Stay awake*, or the screen locks mid-test and you cannot unlock it.
  You can tell debugging is still off without asking: the phone shows on the USB bus as
  `idProduct 0x6860` (file transfer only) and becomes `0x6864` when adb is available.
- **Install on his phone with `./gradlew installSideload`, never `installDebug`.** The Play
  build is on that handset. A debug-signed `com.sajdatime.app` cannot sit beside it, so a
  plain debug install means uninstalling the Play copy, which resets his closed testers and
  costs a fortnight (`docs/RELEASING.md`). `installSideload` uses the `.sideload` suffix and
  installs alongside. Prefix with `ANDROID_SERIAL=<serial>` so Gradle does not target an
  emulator instead. **Never enter his PIN or any credential**, and put back anything you
  change on his phone — font scale, theme, locale.

---

## Before you say you are finished

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then run it on both emulators, and tell the owner what you verified and what you did not.

The on device suite includes widget tests. A shell can bind a widget only after
`adb shell appwidget grantbind --package com.sajdatime.app --user 0`, and the suite is run with
`am instrument`, because `connectedAndroidTest` uninstalls the app when it ends. If a widget test fails
on a bind error, that is why. `docs/release-tools/release-1.5.0.sh` does both (`docs/HANDOVER.md` §15
lesson 134).

*Made with love, free for the Ummah.*
