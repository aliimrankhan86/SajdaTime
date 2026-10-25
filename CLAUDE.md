# SajdaTime — instructions for any AI working on this project

File auto-loaded. Deliberately short. **Full handover: [`docs/HANDOVER.md`](docs/HANDOVER.md). Read all of it before changing any code.**

---

> **Memory:** every session reads + updates shared GBrain page `projects/sajdatime` (`recall` at start; write done + remaining at end). Carries owner's standing decisions/rules between sessions; `docs/HANDOVER.md` stays repo source of truth. **Keeping GBrain current = Claude's job, never owner's** (his words, 19 Oct 2026: "You are responsible to keep gbrain up to date not me"). Cloud session has no GBrain → writes everything into `docs/HANDOVER.md` §11, pushes; **first session with GBrain afterwards compares GBrain vs §11 STATE OF PLAY, updates GBrain itself, unasked.** Never hand him prompt whose only job = update GBrain.

---

## ⚠️ First thing, every session — before you ask him anything

**Any AI new to the project: `docs/AI-START-HERE.md` is the full self-contained briefing (branches, design system, rules, finishing checks). Keep it current when branches, state or rules change.**

**Open [`docs/HANDOVER.md`](docs/HANDOVER.md) §11, read the first 📍 block at top (the state of play; its title names the latest step, not the words "STATE OF PLAY", and the 24 Oct block with that title is history).** Dated. Says: what's true now, what's left + whose job each item is, how to check every claim without Play Console session, traps that actually cost project time, what assistant can/can't do with tooling here. Everything below that first block (each later block says it is history) = dated record of how project got here: read for reasoning, never as task list. STATE OF PLAY rewritten in every commit that moves project → never stale (stale status = what this file forbidden from carrying).

**Don't invent work from older blocks, or from any "Not done" list further down §11.** Most closed; rest = deliberate non-goal. State block leaves nothing to do → correct answer: ask owner what next.

**Then confirm what's actually live before acting on anyone's "done" claim.** Fetch public listing, signed out of every Google account:

```
https://play.google.com/store/apps/details?id=com.sajdatime.app
```

**"Updated on"** date moves when release publishes, nothing else → separates *live* from *everyone involved feels ready* in 2 sec, no credentials, no Console. Play sends **no email** when review passes → inbox proves nothing either way. Both lessons paid for: `docs/HANDOVER.md` §15, 105 and 107.

**He asks "ready for everyone?" (he will; asked several times) → answer yes, proof = Play search, not Console.** Closed test can't appear in search, so hit only possible if public:

```bash
curl -sL -A "Mozilla/5.0" "https://play.google.com/store/search?q=SajdaTime&c=apps&hl=en_GB&gl=GB" | grep -c "com.sajdatime.app"
```

**Say second half same breath or question returns:** *available* to everyone, not yet *discoverable*. Searching "prayer times" or "qibla" doesn't surface it: Play ranks on installs + ratings; app has few of one, none of other. Not fault, not setting, **not work to invent**. §15 lesson 113. (Since 17 Oct 2026 app may show Google's own rating card, owner's word; see rule below.)

**Owner shouldn't have to tell you this; asked not to have to.** Doesn't want to paste briefing at session start. File auto-loaded so he never has to. Keep so: pick-up point moves → move it in `docs/HANDOVER.md` §11, don't add status here.

---

## What this project is

Free, ad-free, privacy-first Muslim prayer times + Qibla app, Android + Wear OS, ongoing charity (*sadaqah jariyah*) for Ummah. Not product, no business model, never will be. Every decision follows: no ads, no accounts, no own server, no revenue. Only thing app can send: **optional, opt in usage counts to Google Analytics, off by default**, owner-approved 3 Oct 2026 so he sees how app used + plans next phases (`docs/ANALYTICS_PLAN.md`, owner sign off in its section 11). Narrow exception, not licence: see usage counts rule below.

Must work **for the masses**: phone with no signal, old device, user who never changed a setting, user who can't read English. Elegant vs dependable → dependable wins.

---

## The commit rule — always, every time

**Commit understanding, not just diff.**

Code records *what* changed. Can't record why, what tried + rejected, verified vs assumed, which decisions deliberate + must not be "improved" back in. That reasoning = expensive to rebuild, easy to lose; losing it → next session confidently undoes decision it didn't know was on purpose.

Every commit that changes behaviour:

1. **Commit message explains reasoning**, not file list. What was wrong, how found, why this fix not another, what verified. Git knows which lines moved.
2. **Update `docs/HANDOVER.md`**: single source of truth. New business rules → §5, new verification evidence → §10, new pending items → §11, anything never to revert → §2 "Deliberately NOT used" or §11 "Deliberate non-goals". **Live / next / open decision changes → also update `docs/PROJECT_BRIEF.md`** (one page, plain words). Owner's claude.ai project reads it live from `main` on GitHub so he never re-uploads (his request, 17 Oct 2026). On release branch, update there; reaches project when he merges.
3. **Record rejected options + reason.** Decision without rationale looks like oversight → gets reversed.
4. **Comment non-obvious in code**, where next reader stands. Esp. anything that looks wrong but is right.
5. **Say what you didn't test.** Honest gap = useful. Silent gap = trap.
6. **Push.** Work only on this machine can be lost.

Change taught you something (platform trap, wrong assumption, bug class reading couldn't catch) → lesson goes in `docs/HANDOVER.md` §15 so next session inherits, not relearns.

---

## Hard rules — these are not preferences

- **iPhone version planned → every decision now keeps that road smooth.** Ali, 23 Oct 2026: "I do
  have aspiration later to make it this app for iOS as well ... So it doesn't look like a design mess, as Apple has
  often objection about the design aspect of the apps", then: "take into consideration for any development even
  though we are not developing for the iphone right now ... so when we develop ios app then things are still smooth
  for us." Nothing built for iOS yet, no cost/complexity added for it now. But:
  - **Design:** every screen, icon, asset also fits Apple's Human Interface Guidelines; App Store Review Guideline 4 (Design) + 4.3 (look-alike apps in crowded category) = how Apple rejects apps like this. Only patterns both platforms share (no floating action button, no navigation drawer, visible back or close every screen), SF Symbol named beside each Material icon, 48dp targets (meets Apple's 44pt), text that scales, distinct own look, assets licensed for both stores. Fable (`ux-lead`) owns check.
  - **Code + data:** two options ~same cost → take one that carries to iPhone. New rules + calculations (tracker, dates, jama'ah, fasts) go in `:core` as plain Kotlin, no `android.*` import; stored data simple formats (plain values or JSON), not Android only; no new Android only library where portable one does same job. No Kotlin Multiplatform move or iOS code until Ali decides.
  - **Every plan states iPhone effect in one line** (none / carries over / Android only + why); Android only features (widgets, Wear OS, exact alarms) labelled. `plan-verifier` + `reviewer` check.
  - **Known gaps today, recorded not fixed** (23 Oct 2026): `:core` plain Kotlin except `AppLocale.kt` + `PrayerLabels.kt` (Android `Context`); uses `java.time` incl. `HijrahDate`, and Java `adhan` library; none run on iPhone as-is. iPhone version would swap these; doesn't block now.
  Full rule: `docs/HANDOVER.md` §5.28 and `.claude/agents/ux-lead.md` on `claude/1-6-0`.

- **Watch testing finished. Never ask him to test watch.** He tested it himself 1 Sept 2026, Qibla included, reported fine, explicitly asked it not be raised again. Times half proved separately 31 Aug: his paired watch + phone agreed on Asr (handover A18). Older `docs/` files still contain sentences like *"the phone-to-watch sync has never been observed working"* and *"the watch Qibla has never been seen on real hardware"*; every one dated, superseded, annotated. **History. Don't act on them; don't put watch check in front of him on their authority.** Raising again spends trust real defect report will need. **He reconfirmed unprompted 7 Sept 2026** (*"watch testing is done and record it"*): third time of asking. Watch app live on Google Play since 5 Sept; if genuinely wrong, arrives as user review; daily health check watches for exactly that. **Any impulse to ask him to check watch = bug in your own reasoning.**

- **Religious disclaimer never removed, softened, or buried**. It lives in Settings, About, first row, and is not shown at first launch (owner, 23 Oct 2026). Dua request in its final paragraph belongs there and *nowhere else*: user asked once, never nagged → no copy of ours ever asks for anything else.
- **Rating: Google's card only, rarely, never in our words.** Owner reversed old "no rate prompt" rule 17 Oct 2026 ("We need rate this app since we are trying to better ratings and be promoted"). Asked for weekly reminder "or other best practice"; research said no to weekly (Google's quota swallows asks under a month apart; Play policy names forced rating pop ups). Built: `review/ReviewPrompt.kt`, Play In-App Review card after 14 days kept + 3 days opened, only on calm Times screen away from prayer times, max once per 120 days, 3 times ever, plus passive "Rate SajdaTime on Google Play" row in About. **No "do you like it?" question, no button calling card, no usage counts event, no app copy asking for rating, no religious wording.** Passive "Share SajdaTime" row beside it (owner, 17 Oct 2026). Both = doors user opens; nothing in app ever suggests sharing or rating.
- **Play reviews: owner replies, Claude drafts.** Agreed 17 Oct 2026. He sends review screenshot → draft short, kind reply on issue raised. Never ask for higher rating in reply (Play policy).
- **Translations: Claude translates + checks own work; no native speaker review.** Owner decided 9 Oct 2026 (free utility he makes himself), superseding old "never machine-translate" rule. Urdu, Turkish, Indonesian shipped 1.3.1 (`values-ur`, `values-tr`, `values-in`). Never tell users translation awaits review; never ask him to find reviewer. Fix any user-reported mistake. Prayer + madhab names = religious content → extra care; religious disclaimer never softened in any language. Indonesian folder = `values-in`, not `values-id`: Android hands app legacy code, so `values-id` packaged but never matches. **Every language stays packaged in app** (`bundle.language.enableSplit = false`): with Play's default language splits, in-app picker can't switch to language phone itself not set to (`docs/HANDOVER.md` §15 lesson 125).
- **App follows phone's language; goes right-to-left only when it has the words.** `AppLocale` reads app's language from resources. Urdu (1.3.1) + Arabic (`values-ar`, 1.5.0, owner's request 17 Oct 2026) = right-to-left; Arabic's `app_language_tag` = `ar-u-nu-latn` → digits stay Western on every Android version (java.time and `String.format` disagree otherwise). Phone set to language we lack words for → stays English, left-to-right; English laid out right-to-left genuinely wrong, owner ruled it out. Two tests in `LocaleDisciplineTest` fail build if either half slips. Since 1.5.0 user can also pick language in app on every Android version ("Phone language" first + default; below 13 via `AppLocale.setOverride`) → anything naming language goes through `AppLocale.wrap` at moment of use. See `docs/HANDOVER.md` §5.16 and §15 lessons 152 and 153.
- **Never call Aladhan API (or any network) from shipped app to calculate times.** Verifying against it in development = correct, encouraged. Calling at runtime = not; reasons in `docs/HANDOVER.md` §2. Owner asked directly, said no.
- **Never generate, hold, request, or view release signing key.** Owner's alone. See `docs/RELEASING.md`. `*.jks`, `*.keystore`, `keystore.properties` gitignored, never committed.
- **Location stays on device; developer never receives it.** Coarse only, foreground only. App sends no coordinates to any server of its own or of anyone it chose to call; prayer times never need network. **One caveat, found 16 Oct 2026, now stated in `docs/privacy.html`:** town name label comes from Android's `Geocoder`, platform-documented as possibly network backed; on phones with Google Play services, Google answers it. So "never leaves the device" not true, must not be written again. App now asks only when no label or moved 5 km (`PlaceLabelPolicy`). Full fix (no network at all) = offline nearest city list, owner's decision (HANDOVER 5.21). (User opted in to usage counts: Google estimates approximate area from their internet connection; app sends nothing for that.) Cloud backup + device-to-device transfer both off. `docs/privacy.html` = published promise → any data-handling change reflected there in same commit.
- **Usage counts: opt in, closed, never about belief.** Off until user says yes; consent text states facts in full (`UsageCounts.kt`, `consent_*` strings, `docs/privacy.html` must agree). **Owner asked for on-by-default 4 Oct 2026; built, then reversed same day on evidence in `docs/HANDOVER.md` §10 (ICO statistical purposes exception needs individual data deleted after aggregation; Firebase can't). Don't rebuild unless that evidence changes.** Pakistan-only country split built + removed same day (no users there, extra risk); no country list again without written source + real user base. Only fixed events in `UsageCounts.kt` may be sent: no new event, parameter, or user property without owner's agreement + privacy policy change first. **Never send user's sect, madhab, calculation method, alert or prayer settings, city or coordinates, nor anything whose *shape* reveals them** (madhab step shown to Sunni users only → has no event). Only release build may send. Word "tracking" not used in app copy. `UsageCountsTest` enforces most of this; don't weaken.
- **Don't move project into `~/Documents`, `~/Desktop`, or any iCloud-synced folder.** iCloud writes conflict copies into `app/build/`, breaks dexing. Cost 3 separate debugging sessions.

---

## How to work here

- **Right model per job, cheapest that does it well** (Ali, 23 Oct 2026). Agents in `.claude/agents/`, each model fixed; reasons in `docs/HANDOVER.md` §5.28.
  - **Fable does all thinking for what users see + verifies plans:** `ux-lead` does planning + brainstorming, user journeys, UX + UI, right icons, ease of use, premium look, what info screen shows, wording, every state (Ali, 23 Oct 2026: "I want Fable to do all the brain thinking"); main session writes down + builds what it decides. `plan-verifier` checks every plan or scope change, before work starts and before Ali sees it.
  - **Fable finds ready-made assets before anything drawn** (Ali, 23 Oct 2026): icons from Material Symbols first, then free licensed artwork, bundled, never AI generated (Play AI declaration = "Don't label assets"). Fable recommends, Ali approves each; source + licence go in `docs/ASSETS.md`.
  - **Keep road to iPhone smooth** in every design + code decision: see iPhone hard rule above. Fable checks designs, `plan-verifier` checks each plan's one-line iPhone effect, `reviewer` checks code.
  - **Fable builds on what Ali already gave and uses skills** (Ali, 23 Oct 2026): `ux-lead.md` lists the design
    system, the 1.6 references (Pillars study, UX.md, the ten journeys, his 27 answers), the designs he chose before,
    and the installed design skills it may call (`Skill` is in its tools). DESIGN_SYSTEM and his decisions beat any
    skill. A tool or skill not yet installed is recommended to Ali, never added without him.
  - **Opus checks everything:** `reviewer` reviews every diff + every piece of work from cheaper agent before commit or "done". Main session (talks to Ali, commits) should itself run on Opus.
  - **Cheaper models do routine work:** `implementer` (Sonnet) builds step with spec already decided; `mechanic` (Haiku) does searching, counting, other mechanical checkable work.
  - Never hand cheaper model a decision, alarm or data logic, translations, or anything user facing without written spec; never skip `reviewer` to save tokens. Visual decisions stay Ali's, from screenshots.
  - **Outside helper models (DeepSeek, Ali 25 Oct 2026: "I want to use deepseek for less thinking tasks"):** same tier as `mechanic`/`implementer`, briefed by `docs/AI-WORKER-BRIEF.md` (keep it current when rules or state change). They work on `deepseek/<task>` branches, never merge; `reviewer` (Opus) reviews before anything reaches a release branch. Never translations, user-facing words, alarms or widgets, usage counts, privacy, signing, calculations, data logic or design, even with a spec (HANDOVER §5.28, "Outside helper models").

- **Quality bar = owner's, not negotiable** (17 Oct 2026: "make sure its all done
  to a good standard ... so nothing is done of bad standard and UX design, clean, easy to use and
  read"). Nothing finished until it meets `docs/DESIGN_SYSTEM.md` (Quiet Glass): its components + Material roles only, even spacing, one line height rule, short human accurate copy with no hyphens or em dashes, checked on emulator in light, dark, 2x text, Urdu right to left; full gate + device suite green; independent diff review before commit; every gap written down. Answer review questions in `DESIGN_SYSTEM.md` §10 for every screen touched.

- **Rules he's had to repeat, so never again: read `docs/DESIGN_SYSTEM.md` §0 before any visual change.** Lists repeated mistakes, why, + rules that stop them:
  - **Ask him first when unsure** (§0.2 lists exactly when), via `AskUserQuestion`, recommendation first, screenshots for anything visual. He asked 18 Oct 2026 ("why can't you ask me if you are not sure"). Guess then report done = the failure.
  - **Never write "done", "every" or "all" until §4.1 checks ran + counts pasted** (§0.3). Twice on 18 Oct 2026 rule reported applied everywhere when not.
  - **Every action = solid button, white bold text, light + dark**; only text links: "Close" and "Got it" at dialog foot, plus his one chosen exception: usage question's two bold underlined links ("See exactly what is sent", "Read the privacy policy"). Full spec, every button in app, checks = §4.1; `DesignRulesTest` fails build on breach, incl. button in file inventory doesn't list.
  - Gaps come from `Space` by relationship (§2). Since 18 Oct 2026 holds for every `Spacer`, column spacing, top/bottom padding in phone app's screens; `DesignRulesTest` fails build on one written as number. **Not covered, never claim:** horizontal gaps inside row, component's own padding (row height, pill, card edge), watch app.
  - No two icon buttons side by side; every dismissable dialog has close X.
  - **§11 lists decisions still waiting for him.** Don't build answer to OPEN item.
  - Before he tests on emulator: give install command + commit he should see. Never let him judge change on stale install.
- **Compiling ≠ working. Run it.** Watch app once compiled + linted clean for entire release while unable to ever get location.
- **Before any layout change, check right-to-left, read result as *layout* check.** `./gradlew :app:installRtl` runs whole app RTL without translation or device setting. ~Half world's Muslims read right-to-left; first Arabic or Urdu translation shouldn't be moment anyone learns if screens survive. But it shows **English words in right-to-left paragraph** → always looks broken, because it is: stranded full stops, meridiems wrong side. Judge mirroring (margins, chevrons, nav order, clipping), ignore prose. **Don't put its screenshots in front of owner as findings.** He reported it broken 3 times, correctly each time; every round spent trust real RTL report will need. Report what check *concluded*, not what it looked like.
- **Run phone + watch emulators together, compare.** Worst bug in project history lived in gap between two modules each individually correct.
- **Verify against independent reference, not own reasoning.** Every real calculation bug caught by comparing to Aladhan; 3 confidently-reported "bugs" were test errors, not code → check both sides.
- **Measure fix, don't just read it.** Modifier change looked right + reviewed clean, did nothing until screenshotted.
- **Design system reference = `docs/design-system/` (start at its README).** Tokens in JSON, components, patterns, every 1.6 screen, copy, accessibility, widgets, watch, iPhone mapping, new feature playbook. `DesignTokensTest` fails build if tokens and code drift or a UI file is undocumented. `docs/DESIGN_SYSTEM.md` keeps owner rules §0, button inventory §4.1, decisions §9, open items §11.
- **Colours from `docs/design-system/tokens/color.json` (roles), never hex in composable.** Each = Material 3 role; `ColorContrastTest` fails build if pair drops below WCAG AA. Visual change not finished until `docs/store/screenshots/` retaken.
- **Ponytail discipline:** stdlib + platform first, no speculative abstractions, shortest working diff. Mark deliberate simplifications with `ponytail:` comment.
- **Owner not technical.** Plain language, separate verified from assumed, never call untested done, never ask him to test on device himself + report back.
- **Anything he does himself in web UI → walk him through one screen at a time.** Numbered list doesn't work; tried enough to stop: 31 Aug 2026 six Play Console steps for Wear release given 3 times, each time read as description of something finished, not instructions. Ask him to open page + send screenshot, tell him what to click **on that screen**, ask for next. He sends screenshots readily; format fits him. **Stop before any submit, publish, purchase or consent button, hand to him**: 20 Aug production submission deliberately left for him to press; agreeing to Google's policies = his signature, not assistant's.
- **Same dictation pattern for emulator + `adb`, not only Play Console.** Verified 1 Sept 2026: chat/Console bridge session can't run `adb`, emulator, or Gradle; its shell only reaches mounted project folder. Give one command block, ask him to paste back exactly what printed, react, repeat. Don't hand multi-step sequence + assume it ran. Full sequence, incl. module-scoping trap that cost a session, in [`docs/HANDOVER.md`](docs/HANDOVER.md), "Getting the app onto an emulator when the assistant has no device access", just above build-traps table.
- **From cloud session, releases go through his Mac in one command + through Claude in Chrome.** Cloud session can't push, run Gradle on his Mac, or drive Play Console itself. How 1.3.2 shipped 11 Oct 2026, end to end: `docs/HANDOVER.md` §15 lessons 128 and 129; script at `docs/release-tools/release-1.3.2.sh`.
- **He has real devices; they find what emulators can't.** Redmi Note 13 Pro (HyperOS) + Galaxy S23 Ultra (One UI); plugs in if asked. Every alarm-reliability fact came off Redmi; S23 Ultra found geocoder bug + layout bug 15 Aug 2026 that 20 emulator probes missed. So **ask for phone when change touches layout, location, alarms, or permissions.** What to ask, this order (he won't know terms): *Settings → About phone → Software information → tap Build number seven times*, then *Settings → Developer options → USB debugging on*, then pick **File Transfer / Android Auto** on USB notification, then **Allow** on "Allow USB debugging?" box. Also ask him to turn on *Developer options → Stay awake*, else screen locks mid-test, can't unlock. Debugging still off → visible without asking: phone shows on USB bus as `idProduct 0x6860` (file transfer only), becomes `0x6864` when adb available.
- **Install on his phone with `./gradlew installSideload`, never `installDebug`.** Play build on that handset. Debug-signed `com.sajdatime.app` can't sit beside it → plain debug install = uninstall Play copy, resets his closed testers, costs fortnight (`docs/RELEASING.md`). `installSideload` uses `.sideload` suffix, installs alongside. Prefix `ANDROID_SERIAL=<serial>` so Gradle doesn't target emulator. **Never enter his PIN or any credential**; put back anything changed on his phone (font scale, theme, locale).

---

## Before you say you are finished

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then run on both emulators; tell owner what verified + what not.

On device suite includes widget tests. Shell binds widget only after `adb shell appwidget grantbind --package com.sajdatime.app --user 0`; suite run with `am instrument` because `connectedAndroidTest` uninstalls app when it ends. Widget test fails on bind error → that's why. `docs/release-tools/release-1.5.0.sh` does both (`docs/HANDOVER.md` §15 lesson 134).

*Made with love, free for the Ummah.*