# SajdaTime — instructions for any AI working on this project

File auto-loaded. Short on purpose. **Full handover: [`docs/HANDOVER.md`](docs/HANDOVER.md). Read in full before changing code.**

---

> **Memory:** every session reads + updates shared GBrain page `projects/sajdatime` (`recall` at start; write done + remaining at end). Holds owner's standing decisions/rules across sessions; `docs/HANDOVER.md` stays repo source of truth. **Keeping GBrain current = Claude's job, never owner's** (his words, 19 Oct 2026: "You are responsible to keep gbrain up to date not me"). Cloud session has no GBrain → write everything into `docs/HANDOVER.md` §11, push; **first later session with GBrain compares GBrain vs §11 STATE OF PLAY, updates GBrain itself, unasked.** Never hand him prompt whose only job = GBrain update.

---

## ⚠️ First thing, every session — before you ask him anything

**Open [`docs/HANDOVER.md`](docs/HANDOVER.md) §11, read `STATE OF PLAY` block at top.** Dated. Says: what true now, what left + whose job, how to check each claim without Play Console session, traps that actually cost time, what assistant can/cannot do with tooling here. Everything below `HISTORY BELOW THIS LINE` marker = dated record of how project got here: read for reasoning, never as task list. STATE OF PLAY rewritten in every commit that moves project, so never the stale sentence this file forbidden to carry.

**Don't invent work from older blocks or any "Not done" list lower in §11.** Mostly closed; rest = deliberate non-goals. State block leaves nothing to do → correct answer: ask owner what next.

**Then confirm what actually live before trusting any "done" claim.** Fetch public listing, signed out of all Google accounts:

```
https://play.google.com/store/apps/details?id=com.sajdatime.app
```

**"Updated on"** date moves only when release publishes → separates *live* from *everyone feels ready* in two seconds, no credentials, no Console. Play sends **no email** when review passes → inbox proves nothing either way. Both lessons project paid for: `docs/HANDOVER.md` §15, 105 and 107.

**He asks "ready for everyone?" (asked several times, will again) → answer yes; proof = Play search, not Console.** Closed test can't appear in search, so hit only possible if public:

```bash
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/search?q=SajdaTime&c=apps&hl=en_GB&gl=GB" | grep -c "com.sajdatime.app"
```

**Say second half same breath or question returns:** *available* to everyone, not yet *discoverable*. "prayer times"/"qibla" searches don't surface it; Play ranks on installs + ratings, app has few installs, zero ratings. Not fault, not setting, **not work to invent**. §15 lesson 113. (Since 17 Oct 2026 app may show Google's own rating card, owner's word; see rule below.)

**Owner shouldn't have to tell you any of this; asked not to.** No pasted briefing at session start. File auto-loads so he never has to. Keep it so: pick-up point moves → move it in `docs/HANDOVER.md` §11, don't add status here.

---

## What this project is

Free, ad-free, privacy-first Muslim prayer times + Qibla app, Android + Wear OS, ongoing charity (*sadaqah jariyah*) for Ummah. Not product, no business model, never will be. Every decision follows: no ads, no accounts, no own server, no revenue. Only thing app may send: **optional, opt-in usage counts to Google Analytics, off by default**, owner-approved 3 Oct 2026 to see usage + plan next phases (`docs/ANALYTICS_PLAN.md`, owner sign-off in its section 11). Narrow exception, not licence: see usage counts rule below.

Must work **for the masses**: phone with no signal, old device, user who never changed a setting, user who can't read English. Elegant vs dependable → dependable wins.

---

## The commit rule — always, every time

**Commit the understanding, not just the diff.**

Code records *what* changed. Can't record why, what tried + rejected, verified vs assumed, which decisions deliberate and must not be "improved" back. That reasoning expensive to rebuild, easy to lose. Losing it → next session confidently undoes deliberate decision.

Every behaviour-changing commit:

1. **Message explains reasoning**, not file list. What wrong, how found, why this fix not another, what verified. Git knows which lines moved.
2. **Update `docs/HANDOVER.md`** (single source of truth). New business rules → §5; verification evidence → §10; pending items → §11; never-revert items → §2 "Deliberately NOT used" or §11 "Deliberate non-goals". **What's live, what's next, or open decision changes → also update `docs/PROJECT_BRIEF.md`** (one page, plain words). Owner's claude.ai project reads it live from `main` on GitHub, so no re-upload (his request, 17 Oct 2026). On release branch, update there; reaches project on merge.
3. **Record rejected options + reason.** Decision without rationale looks like oversight → gets reversed.
4. **Comment non-obvious in code**, where next reader stands. Esp. anything that looks wrong but is right.
5. **Say what not tested.** Honest gap useful. Silent gap = trap.
6. **Push.** Local-only work can be lost.

Change taught something (platform trap, wrong assumption, bug class reading never catches) → lesson goes in `docs/HANDOVER.md` §15 so next session inherits, not relearns.

---

## Hard rules — these are not preferences

- **iPhone version planned → every decision now keeps that road smooth.** Ali, 23 Oct 2026: "I do have aspiration later to make it this app for iOS as well ... So it doesn't look like a design mess, as Apple has often objection about the design aspect of the apps", then: "take into consideration for any development even though we are not developing for the iphone right now ... so when we develop ios app then things are still smooth for us." Nothing built for iOS yet; no cost/complexity added for it now. But:
  - **Design:** every screen, icon, asset also fits Apple Human Interface Guidelines. App Store Review Guideline 4 (Design) + 4.3 (look-alike apps, crowded category) = how Apple rejects apps like this. Only patterns both platforms share (no floating action button, no navigation drawer, visible back or close every screen), SF Symbol named beside each Material icon, 48dp targets (meets Apple 44pt), scalable text, distinct own look, assets licensed for both stores. Fable (`ux-lead`) owns check.
  - **Code + data:** two options ~same cost → take one that carries to iPhone. New rules/calculations (tracker, dates, jama'ah, fasts) go in `:core` as plain Kotlin, no `android.*` import; stored data in simple formats (plain values or JSON), not Android-only; no new Android-only library where portable one does same job. No Kotlin Multiplatform move or iOS code until Ali decides.
  - **Every plan states iPhone effect in one line** (none, carries over, or Android-only + why); Android-only features (widgets, Wear OS, exact alarms) labelled. `plan-verifier` and `reviewer` check.
  - **Known gaps today, recorded not fixed** (23 Oct 2026): `:core` plain Kotlin except `AppLocale.kt` and `PrayerLabels.kt` (Android `Context`); uses `java.time` incl. `HijrahDate`, and Java `adhan` library. None run on iPhone as-is. iPhone version would swap these; not blocking now.
  Full rule: `docs/HANDOVER.md` §5.28 and `.claude/agents/ux-lead.md` on `claude/1-6-0`.

- **Watch testing finished. Never ask him to test watch.** He tested it himself 1 September 2026, Qibla included, reported fine, explicitly asked it not be raised again. Times half proved separately 31 Aug: his paired watch + phone agreed on Asr (handover A18). Older `docs/` still say things like *"the phone-to-watch sync has never been observed working"* and *"the watch Qibla has never been seen on real hardware"*; all dated, superseded, annotated. **History. Don't act on them, don't put watch check before him on their authority.** Raising again spends trust a real defect report will need. **Reconfirmed unprompted 7 September 2026**: *"watch testing is done and record it"*. Third time asking. Watch app live on Google Play since 5 September; real problem → arrives as user review, and daily health check watches for exactly that. **Any impulse to ask him to check watch = bug in your own reasoning.**

- **Religious disclaimer never removed, softened, or buried.** Dua request in its final paragraph belongs there and *nowhere else*: user asked once, never nagged, so no copy of ours asks for anything else.
- **Rating: Google's card only, rarely, never in our words.** Owner reversed old "no rate prompt" rule 17 Oct 2026 ("We need rate this app since we are trying to better ratings and be promoted"). Asked weekly reminder "or other best practice"; research said no to weekly (Google quota swallows asks <1 month apart; Play policy names forced rating pop-ups). Built: `review/ReviewPrompt.kt`, Play In-App Review card after 14 days kept + 3 days opened, only on calm Times screen away from prayer times, max once per 120 days, 3 times ever, plus passive "Rate SajdaTime on Google Play" row in About. **No "do you like it?" question, no button calling card, no usage counts event, no app copy asking for rating, no religious wording.** Passive "Share SajdaTime" row beside it (owner, 17 Oct 2026). Both = doors user opens; app never suggests sharing or rating.
- **Play reviews: owner replies, Claude drafts.** Agreed 17 Oct 2026. He sends review screenshot → draft short, kind reply on issue raised. Never ask for higher rating in reply (Play policy).
- **Translations: Claude translates + checks own work; no native speaker review.** Owner decided 9 Oct 2026 (free utility he makes himself), superseding old "never machine-translate" rule. Urdu, Turkish, Indonesian shipped 1.3.1 (`values-ur`, `values-tr`, `values-in`). Never tell users translation awaits review; never ask him to find reviewer. Fix any user-reported mistake. Prayer + madhab names = religious content, extra care; religious disclaimer never softened in any language. Indonesian folder = `values-in`, not `values-id`: Android hands app legacy code, so `values-id` packaged but never matches. **Every language stays packaged in app** (`bundle.language.enableSplit = false`): with Play default language splits, in-app picker can't switch to language phone not set to (`docs/HANDOVER.md` §15 lesson 125).
- **App follows phone language; goes RTL only when it has the words.** `AppLocale` reads app language from resources. Urdu (1.3.1) + Arabic (`values-ar`, 1.5.0, owner request 17 Oct 2026) RTL; Arabic `app_language_tag` = `ar-u-nu-latn` so digits stay Western on every Android version (java.time and `String.format` disagree otherwise). Phone set to language we have no words for → English, LTR. English laid out RTL genuinely wrong; owner ruled it out. Two tests in `LocaleDisciplineTest` fail build if either half slips. Since 1.5.0 user can also pick language in-app on every Android version ("Phone language" first + default; below 13 via `AppLocale.setOverride`), so anything naming language must go through `AppLocale.wrap` at moment of use. See `docs/HANDOVER.md` §5.16 and §15 lessons 152 and 153.
- **Never call Aladhan API (or any network) from shipped app to calculate times.** Verifying against it in development = correct, encouraged. Runtime calls = no; reasons in `docs/HANDOVER.md` §2. Owner asked directly, said no.
- **Never generate, hold, request, or view release signing key.** Owner's alone. See `docs/RELEASING.md`. `*.jks`, `*.keystore` and `keystore.properties` gitignored, never committed.
- **Location stays on device; developer never receives it.** Coarse only, foreground only. App sends no coordinates to any own server or anyone it chose to call; prayer times never need network. **One caveat, found 16 Oct 2026, now stated in `docs/privacy.html`:** town name label comes from Android `Geocoder`, platform-documented as possibly network-backed; on phones with Google Play services Google answers it. So "never leaves the device" not true, never write again. App now asks only when no label or moved 5 km (`PlaceLabelPolicy`). Full fix (no network at all) = offline nearest-city list, owner's decision (HANDOVER 5.21). (User opted in to usage counts: Google estimates approximate area from internet connection; app sends nothing for that.) Cloud backup + device-to-device transfer both off. `docs/privacy.html` = published promise → any data-handling change reflected there same commit.
- **Usage counts: opt-in, closed, never about belief.** Off until user says yes, consent text states facts in full (`UsageCounts.kt`, `consent_*` strings, and `docs/privacy.html` must agree). **Owner asked on-by-default 4 Oct 2026; built, reversed same day on evidence in `docs/HANDOVER.md` §10 (ICO statistical purposes exception needs individual data deleted after aggregation; Firebase can't). Don't rebuild unless that evidence changes.** Pakistan-only country split built + removed same day (no users there, extra risk); no country list without written source + real user base. Only fixed events in `UsageCounts.kt` may send: no new event, parameter, or user property without owner agreement + privacy policy change first. **Never send user's sect, madhab, calculation method, alert or prayer settings, city or coordinates, nor anything whose *shape* reveals them** (madhab step shown to Sunni users only → no event). Only release build sends. Word "tracking" not used in app copy. `UsageCountsTest` enforces most; don't weaken.
- **Don't move project into `~/Documents`, `~/Desktop`, or any iCloud-synced folder.** iCloud writes conflict copies into `app/build/`, breaks dexing. Cost three separate debugging sessions.

---

## How to work here

- **Quality bar = owner's, non-negotiable** (17 Oct 2026: "make sure its all done to a good standard ... so nothing is done of bad standard and UX design, clean, easy to use and read"). Nothing finished until meets `docs/DESIGN_SYSTEM.md` (Quiet Glass): its components + Material roles only, even spacing, one line-height rule, short human accurate copy with no hyphens or em dashes, checked on emulator in light, dark, 2x text, Urdu RTL, full gate + device suite green, independent diff review before commit, every gap written down. Answer review questions in `DESIGN_SYSTEM.md` §10 for every screen touched.

- **Rules he had to repeat, so never again: read `docs/DESIGN_SYSTEM.md` §0 before any visual change.** Lists repeated mistakes, why, rules that stop them:
  - **Unsure → ask him first** (§0.2 lists exactly when), via `AskUserQuestion`, recommendation first, screenshots for anything visual. Asked 18 Oct 2026 ("why can't you ask me if you are not sure"). Guessing then reporting done = failure.
  - **Never write "done", "every" or "all" until §4.1 checks ran + counts pasted** (§0.3). Twice on 18 Oct 2026, rule reported applied everywhere when not.
  - **Every action = solid button, white bold text, light + dark.** Only text links: "Close" and "Got it" at dialog foot, plus his one chosen exception: usage question's two bold underlined links ("See exactly what is sent", "Read the privacy policy"). Full spec, every button in app, checks = §4.1; `DesignRulesTest` fails build on breach, incl. button in file inventory doesn't list.
  - Gaps from `Space` by relationship (§2). Since 18 Oct 2026 holds for every `Spacer`, column spacing, top/bottom padding in phone app screens; `DesignRulesTest` fails build on one written as number. **Not covered, never claim:** horizontal gaps inside row, component's own padding (row height, pill, card edge), watch app.
  - No two icon buttons side by side; every dismissable dialog has close X.
  - **§11 lists decisions still waiting on him.** Don't build answer to OPEN item.
  - Before he tests on emulator, give install command + commit he should see. Never let him judge change on stale install.
- **Compiling ≠ working. Run it.** Watch app once compiled + linted clean for whole release while unable to ever get location.
- **Before any layout change, check RTL, and read result as *layout* check.** `./gradlew :app:installRtl` runs whole app RTL without translation or device setting. ~Half world's Muslims read RTL; first Arabic/Urdu translation shouldn't be when anyone finds out if screens survive. But it shows **English words in RTL paragraph** → always looks broken, because it is: stranded full stops, meridiems wrong side. Judge mirroring (margins, chevrons, nav order, clipping), ignore prose. **Don't put its screenshots before owner as findings.** He reported it broken three times, correctly each time; each round spent trust a real RTL report will need. Report what check *concluded*, not how it looked.
- **Run phone + watch emulators together, compare.** Worst bug in project history lived in gap between two modules each individually correct.
- **Verify against independent reference, not own reasoning.** Every real calculation bug caught by comparing to Aladhan; three confident "bugs" were test errors not code, so check both sides.
- **Measure fix, don't just read it.** Modifier change looked right, reviewed clean, did nothing until screenshotted.
- **Colours from `docs/DESIGN_SYSTEM.md`, never hex in composable.** Each = Material 3 role; `ColorContrastTest` fails build if pair drops below WCAG AA. Visual change not finished until `docs/store/screenshots/` retaken.
- **Ponytail discipline:** stdlib + platform first, no speculative abstractions, shortest working diff. Mark deliberate simplifications with `ponytail:` comment.
- **Owner not technical.** Plain language, separate verified vs assumed, never call untested thing done, never ask him to test on device himself and report back.
- **Anything he must do in web UI → walk him through one screen at a time.** Numbered lists don't work; tried enough to stop: 31 Aug 2026 six Play Console steps for Wear release given three times, read each time as description of finished thing, not instructions. Ask him to open page + send screenshot, say what to click **on that screen**, ask for next. He sends screenshots readily; format fits him. **Stop before any submit, publish, purchase or consent button, hand to him.** 20 Aug production submission deliberately left for him to press; agreeing to Google policies = his signature, not assistant's.
- **Same dictation pattern for emulator + `adb`, not only Play Console.** Verified 1 Sept 2026: chat/Console bridge session can't run `adb`, emulator, or Gradle; shell reaches only mounted project folder. Give one command block, ask him to paste back exact output, react, repeat. Don't hand multi-step sequence and assume it ran. Full sequence, incl. module-scoping trap that cost a session, in [`docs/HANDOVER.md`](docs/HANDOVER.md), "Getting the app onto an emulator when the assistant has no device access", just above build-traps table.
- **From cloud session, releases go through his Mac in one command + Claude in Chrome.** Cloud session can't push, run Gradle on his Mac, or drive Play Console itself. How 1.3.2 shipped 11 Oct 2026 end to end: `docs/HANDOVER.md` §15 lessons 128 and 129; script at `docs/release-tools/release-1.3.2.sh`.
- **He has real devices; they find what emulators can't.** Redmi Note 13 Pro (HyperOS) + Galaxy S23 Ultra (One UI), plugged in if asked. Every alarm-reliability fact came off Redmi; S23 Ultra found geocoder bug + layout bug 15 Aug 2026 that twenty emulator probes missed. So **ask for phone when change touches layout, location, alarms or permissions.** Ask in this order (he won't know terms): *Settings → About phone → Software information → tap Build number seven times*, then *Settings → Developer options → USB debugging on*, then pick **File Transfer / Android Auto** on USB notification, then **Allow** on "Allow USB debugging?" box. Also ask him to enable *Developer options → Stay awake*, else screen locks mid-test, can't unlock. Debugging still off? Detectable without asking: phone shows on USB bus as `idProduct 0x6860` (file transfer only) → `0x6864` when adb available.
- **Install on his phone with `./gradlew installSideload`, never `installDebug`.** Play build on that handset. Debug-signed `com.sajdatime.app` can't sit beside it → plain debug install means uninstalling Play copy → resets closed testers, costs fortnight (`docs/RELEASING.md`). `installSideload` uses `.sideload` suffix, installs alongside. Prefix `ANDROID_SERIAL=<serial>` so Gradle doesn't target emulator. **Never enter his PIN or any credential**; restore anything changed on his phone (font scale, theme, locale).

---

## Before you say you are finished

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then run on both emulators; tell owner what verified, what not.

On-device suite includes widget tests. Shell can bind widget only after `adb shell appwidget grantbind --package com.sajdatime.app --user 0`; suite run with `am instrument` because `connectedAndroidTest` uninstalls app at end. Widget test fails on bind error → that's why. `docs/release-tools/release-1.5.0.sh` does both (`docs/HANDOVER.md` §15 lesson 134).

*Made with love, free for the Ummah.*