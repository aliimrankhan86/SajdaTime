# Phase two: optional usage counts (plan, v4, APPROVED IN PRINCIPLE)

**Status: v4. Owner sign off recorded 3 Oct 2026 (section 11). No app code has been changed.**
The build is waiting on the session at the owner's computer (section 6, Phase 0 onwards).
Written 3 Oct 2026. Revised three times: v2 after an adversarial review, v3 after a fact check
and a rewrite around "the assistant does the work, the owner does the minimum", v4 to record the
sign off and add tab level screen views, because the owner's goal is to see what is doing well
and what is not.
When approved and built, fold the decisions into `docs/HANDOVER.md` (§2, §5, §8, §11, §15) and
`CLAUDE.md`, then delete or archive this file.

## 0. How to read this plan

Every factual claim carries one of three labels. **If a claim has no label, treat it as the
author's judgement, not a fact.**

- **CONFIRMED**: seen in an official Google or Firebase page (via search result text) or in
  this repo, with the source named.
- **PARTLY**: supported only by third party sources, or by the iOS page where Android is
  assumed to behave the same. To be proved on the day.
- **UNVERIFIED**: could not be checked. Has a test or a console screen that settles it.

**What could not be done in the planning session, and why it matters:** Google's own pages
(firebase.google.com, support.google.com, dl.google.com, maven.google.com metadata) are blocked
from this environment, so nothing was read from them directly. There is also **no Android SDK
here**, so nothing could be compiled, linted or run. The plan therefore contains no claim that
any code works. All building and testing happens on the owner's computer (section 5).

## 1. Decision being asked for

Add **optional, opt in usage counts** to the **phone app only**, using Firebase Analytics, so
the owner can see how many people use SajdaTime, how often, for how long, and from which
countries. Nothing else.

This **reverses a written project rule** ("no analytics", `CLAUDE.md` lines 60 to 61,
`HANDOVER.md` §11 non-goals at about line 6222, and several published statements). It needs the
owner's explicit sign off before any code (section 11).

| Already decided (owner, 3 Oct 2026) | |
|---|---|
| Tool | Firebase Analytics (free) |
| Consent | Opt in, off by default |
| Scope | Phone only. Watch untouched |
| Events | **v4 change:** automatic metrics plus **one screen view per main tab** (Times, Qibla, Settings, and any other top level tab that exists on the day). Nothing else. Reason: the owner wants to see what is doing well, and automatic metrics alone cannot show which part of the app is used. The owner asked for the goal and delegated the method, so this is the assistant's recommendation. Say "drop it" to revert to automatic only |
| Never sent | Sect, madhab, calculation method, prayer or alert settings, city, coordinates. These reveal religious belief and are treated as special category data under UK GDPR (legal classification UNVERIFIED, so we are conservative). A permanent rule, see 7.2 |

**Rejected, with reasons:** Aptabase (tracks nothing automatically, so every metric needs event
code); self hosted Umami, Plausible or Matomo (needs a server, breaks "no server"); on by
default (weakest privacy position, and the UK "statistical purposes" exemption is narrow, legal
status UNVERIFIED); Crashlytics (out of scope, Play vitals already covers crashes); custom
backend (cost and security burden, no benefit).

## 2. Fact check: what is and is not established

| Claim | Status | Source or test |
|---|---|---|
| `setAnalyticsCollectionEnabled` persists across launches and overrides the manifest default | CONFIRMED (iOS page text; the Android page was seen only as a summary saying the same) | Firebase "Configure Analytics data collection". Prove on Android with the on, off, on, relaunch test |
| A manifest `..._collection_enabled=false` is the documented way to wait for consent | CONFIRMED | Same page |
| The "deactivated" key overrides any runtime call, so it must **not** be used for opt in | CONFIRMED on iOS, PARTLY on Android | Same page. We do not use it either way |
| Google Analytics does not log or store IP addresses, but derives city, region and country from the IP | CONFIRMED | Google "Privacy controls in Google Analytics" (support.google.com/firebase/answer/9019185) |
| New GA4 properties keep event data 2 months by default, 14 months selectable free | PARTLY | Third party guides plus Google retention page text. Settle on the console screen on the day |
| Manual `FirebaseApp.initializeApp` after startup is possible | CONFIRMED | Firebase blog "Take control of your Firebase init" |
| Manual init is **safe for Analytics** | **UNVERIFIED, doubtful** | Sources say Analytics wants the app ID and early init to measure correctly, and users report Analytics disabled without it. Late init may miss `first_open` and the first `session_start` |
| With collection disabled, Firebase sends nothing at all before consent | **UNVERIFIED** | Docs imply it. Settled only by the opted out capture test (section 8) |
| Analytics falls back to the Android ID when the advertising ID is off | UNVERIFIED (one forum claim) | Only changes policy wording, because *Device or other IDs* is declared regardless |
| Latest versions: BoM 34.19.0, firebase-analytics 23.2.0, google-services plugin 4.4.4 | UNVERIFIED (search result summaries, Google Maven blocked here) | Gradle resolves the real latest on the day. Do not hard code these numbers from this plan |
| google-services plugin works with this repo's AGP 9.3.1 and built in Kotlin | UNVERIFIED | Build every variant on the day |
| Merged manifest gains `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, probably the install referrer permission and `<queries>` | UNVERIFIED | Diff the merged manifest on the day |
| Play Data safety accepts a CSV import | UNVERIFIED (from memory) | Look for "Import from CSV" on the form on the day. If absent the form is filled by hand with the answers in section 7 |
| Existing installs: about 22 as of 7 Sept (HANDOVER) | CONFIRMED but stale | Re-read Play Console before quoting |
| Reports lag about a day; hidden rows at small audiences | UNVERIFIED | Observe |

## 3. What the owner will be able to see

| Question | Where | Notes |
|---|---|---|
| How many users | Daily, weekly, monthly active users (Firebase) | **A sample.** Only opted in users |
| How often | Sessions per user, returning users | Same sample |
| How long | Average engagement time | Foreground time only |
| Do they stay | Day 1, 7, 28 retention | Needs enough opted in users |
| Where from | Country, usually city | Derived from the connection, not GPS |
| Which part of the app is used | Screen views per main tab (v4) | Counts opted in users only. Says nothing about features inside a screen |
| Device, Android version, app version | Standard reports | |
| True installs | **Play Console** | The only unsampled headcount |

Caveats to repeat every time: (1) opt in rates are low, so use Firebase for ratios and Play
Console for the headcount (author's judgement); (2) notification, lock screen and watch use
never opens the app, so retention is a better signal than time in app; (3) small audiences give
sparse reports (UNVERIFIED threshold); (4) a Settings switch alone would, in the author's
judgement, give close to no data.

## 4. Design

### 4.1 Approach (decided: A)
**A. Standard setup.** `google-services` plugin and `google-services.json`; Firebase
initialises at startup but is told not to collect (`firebase_analytics_collection_enabled`
false); `setAnalyticsCollectionEnabled(true)` after the user opts in. This is the documented
path and is the only one that keeps Analytics' own session logic intact.

**B. Manual init after consent** was the v2 favourite. The fact check demoted it (section 2).
It is only a **fallback**, used if and only if the opted out capture test shows traffic under A.
If B is needed, it must first prove that it records `first_open` and sessions, or we stop and
report to the owner rather than ship incomplete data.

**Stop rule:** if A leaks traffic before consent and B cannot record sessions, we do not ship.
The owner is told plainly. No compromise on "off means off".

### 4.2 Manifest (app only)
- `firebase_analytics_collection_enabled` = false.
- `google_analytics_adid_collection_enabled` = false (key name PARTLY confirmed, check on the day).
- Remove `com.google.android.gms.permission.AD_ID` with `tools:node="remove"`. Needs
  `xmlns:tools` added to the phone manifest, which lacks it.
- No Consent Mode default keys. They add a second switch that could disagree with the first.
  Whether `setConsent` is also needed is UNVERIFIED, so the test must show events arriving
  after opt in.
- Never use the "deactivated" key.
- Fix the `INTERNET` comment (`AndroidManifest.xml` line 11).
- Every merged permission addition goes into `privacy.html`.

### 4.3 Code (smallest diff, copies `ongoingBadge` and `disclaimerSeen`)
1. `AppSettings.analyticsEnabled: Boolean = false`. No "prompt seen" flag.
2. Key, setter, mapping in `SettingsRepository.kt`. No migration (absent reads false).
3. `SajdaViewModel` wrapper, threaded through `MainActivity`, `MainScaffold`, `SettingsScreen`.
4. About 10 to 15 lines in the data layer: when settings load and the flag is true, enable
   collection. On a true to false change, disable and call `resetAnalyticsData()`. Otherwise
   **do not touch Firebase at all** (calling disable on every launch could itself initialise
   the SDK, a reviewer's concern, UNVERIFIED but cheap to avoid).
5. No `Application` subclass. Fix the "never transmitted" KDoc (`SettingsRepository.kt` about line 44).
6. **Tab screen views (v4):** one `screen_view` log per main tab, sent only while opted in, with
   the screen name set to a fixed word (for example `times`, `qibla`, `settings`). No
   parameters, no user properties. The app is a single activity, so Analytics will not
   produce per-tab data by itself (UNVERIFIED for Compose, confirm on the day). If the
   navigation structure makes this more than a few lines, stop and tell the owner rather than
   building an event layer.

### 4.4 Build variants
`debug` shares the release package name. `rtl` and `sideload` use `initWith(debug)`, which does
**not** inherit the `debug` source set (reviewer finding, matches the repo). So:
- Create `app/src/debug`, and use the existing `app/src/rtl` and `app/src/sideload`, each
  setting `R.bool.analytics_allowed` to false. `main` sets true.
- When false, analytics never starts and the Settings switch is hidden.
- A test asserts the three overrides exist.
- The plugin needs `google-services.json` clients for `.rtl` and `.sideload` as well.
- Nothing Firebase related goes into `:wear`.

### 4.5 Consent UX
- **New installs:** a new onboarding step after `PERMISSION`. Rewire both directions
  (`OnboardingScreen.kt`, enum at line 79, wiring about lines 108 to 156). Two equal choices,
  nothing pre selected, Google named, policy linked.
- **Everyone:** a Settings switch in About beside Privacy. Off by default. It is also the
  one tap withdrawal.
- **Existing users:** no pop up. They find the switch.
- Not bundled with any feature. Saying no changes nothing.
- **Dua rule untouched.** Never in or after the disclaimer, never the word "duas".
- **Exception to write into HANDOVER §11:** the "asked once" rule was written to ban rating and
  share prompts. Consent is different and required. Record it so a later session does not
  delete the screen.

### 4.6 Opting out and deletion
Switching off disables collection and resets local analytics data and the instance ID. **Data
already sent stays with Google** and ages out under the retention setting. **There is no
"delete on request" promise** because the instance ID is the only handle and the reset destroys
it. The policy says so. The 14 month setting covers event data; aggregated reports last longer,
so the policy must not say everything vanishes at 14 months.

## 5. Who does what: owner effort budget

**Owner total: two short guided sessions plus one optional two minute phone check. Time
figures are estimates, not measurements.** The assistant does everything else, including writing
every word the owner will paste or click.

| Session | Owner does | Assistant does |
|---|---|---|
| **Now** | Answer the five sign off questions (section 11) | Nothing further until then |
| **Session 1: Firebase, about 15 min, on the owner's computer** | Open the Firebase console signed in to the **same Google account used for Play Console** (owner decision, 3 Oct 2026; a separate project account was the rejected alternative, worth revisiting only if the project is ever handed to someone else). Follow one screen at a time, sending a screenshot each time: create project, register the app, set retention to 14 months, switch Google signals and every data sharing option off, download `google-services.json` into the project folder | Tells the owner what to click on each screen. Registers the three package names. Reads the file. Restricts the API key instructions. Confirms each setting from the screenshots |
| **Everything between** | Nothing | Phases 1 to 6 of the runbook (section 6): code, tests, copy, docs, builds, emulators, screenshots, graphic, the signed bundle using the existing keystore flow |
| **Session 2: Play Console, about 15 min** | Open Play Console. Follow screens: import or fill the Data safety form from the prepared answers, update the Advertising ID answer, upload the bundle, update the listing text from the prepared copy, **press Publish** | Prepared files and exact answers. Guides each screen. Never presses submit |
| **Optional** | Plug in the Redmi (the project's rule for layout and first run changes) for a two minute check | Installs with `installSideload`, never `installDebug` |

Things the assistant will **not** do: touch the signing key, press submit or publish, agree to
policies, or enter credentials.

## 6. Runbook for the session at the owner's computer

The first message of that session should be the **kickoff prompt** in section 12. Phases run in
order. Each has a gate. If a gate fails, stop and report rather than improvise.

| Phase | Work | Gate |
|---|---|---|
| 0 | Read `CLAUDE.md`, HANDOVER §11 STATE OF PLAY, this plan. Fetch the public Play listing to confirm what is live. Confirm sign off. Create a feature branch | Sign off recorded in this file |
| 1 | **Close every UNVERIFIED row** that a browser can close: open the Firebase and Google pages named in section 2, record what they say, correct this plan | No row left UNVERIFIED that a page can settle |
| 2 | **Session 1 with the owner** (section 5) | `google-services.json` in place, console settings confirmed from screenshots |
| 3 | Code (section 4), tests (section 8), variant overrides | `./gradlew clean test lint` green, every variant builds |
| 4 | Manifest diff against the current release. Opted out capture test. Opt in DebugView test. Relaunch test. No Google Play services emulator test. RTL layout check of the new screen (a layout check only) | Opted out run shows no traffic to Google. Opt in shows events. Stop rule in 4.1 applies |
| 5 | Copy and docs (sections 7 and 9): app strings, `privacy.html`, `index.html`, `README.md`, `LISTING.md`, `tools/build-store-assets.sh` and the regenerated graphic, `CLAUDE.md`, HANDOVER, RELEASING, NEW-SESSION. Repo wide grep for `tracking`, `analytics`, `telemetry` excluding this plan and `docs/reviews/`. Retake `05-settings.png` only if the new row appears in it | Grep clean. Disclaimer tests green. Test for each new rule |
| 6 | Full gate from `CLAUDE.md`: `clean test lint :app:bundleRelease :wear:bundleRelease`. Both emulators | Green, bundle sizes recorded |
| 7 | **Session 2 with the owner** (section 5) | Owner presses Publish |
| 8 | After publish: confirm "Updated on" moved, check DebugView or first real data, write STATE OF PLAY and the lessons into §15 | Recorded and pushed |

## 7. Copy and documents

### 7.1 What becomes false
| File | Where | Problem |
|---|---|---|
| `app/src/main/res/values/strings.xml` | `about_privacy_desc` (about 348), `permission_why_body` (about 31) | "no analytics"; "never track you" |
| `AndroidManifest.xml` | 11 | INTERNET comment |
| `SettingsRepository.kt` | about 44 | KDoc |
| `docs/privacy.html` | 37, 39 to 40, 53 to 70 (including "no server ... nowhere for them to go"), 80 to 83, 91 to 92, 96 to 102 | date, lede, "one time", analytics, SDKs, sharing, children, permissions |
| `docs/index.html` | 39 | "no ads or tracking" |
| `README.md` | 5 | "No analytics. No tracking." |
| `docs/store/LISTING.md` | 21, long description privacy block (about 170 to 182, including "no server for them to go to"), 327, 351, 355, 357, 364, 480 | short and long description, Data safety, Advertising ID, graphic text |
| `tools/build-store-assets.sh` | 130 | subtitle, then regenerate the PNG |
| `CLAUDE.md` | 60 to 61 | founding rule |
| `docs/HANDOVER.md` | 50, §2 networking and Play Services rows (about 109 to 138), §8, 2079 to 2085, §11 non-goals (about 6222) and the rating rule (6226 to 6236), 6706, STATE OF PLAY | vision, stack, privacy model, audit table, non-goals |
| `docs/RELEASING.md` | 60, 171, 727, 764, 988 | tester and outreach copy |
| `docs/NEW-SESSION.md`, `docs/ARCHITECTURE.md` | 179, 406 | "no tracking" (check context) |

History to leave alone: `docs/reviews/`, HANDOVER line 6195, LISTING lines 28, 53, 75, 96.
Line numbers are from the 3 Oct tree and may drift.

### 7.2 Wording rules (override any draft that conflicts)
- Say **"random ID, not your name"** or "pseudonymous". Not "anonymous" alone. An instance ID is
  personal data under UK GDPR and PECR, which is why consent is needed.
- Say "approximate area, usually country and city, worked out by Google from your internet
  connection. Never your GPS position or the place you type." Do not say "never your location".
- Attribute the IP statement: "According to Google, it does not log or store IP addresses in
  Analytics." Do not state it as our own guarantee.
- Name Google, say data may be processed in the US (UNVERIFIED location, check on the day), link the policy.
- Never promise deletion on request.
- The option is for adults. The app is not directed at children.
- **Permanent rule: never send sect, madhab, calculation method, alert or prayer settings, a
  typed city or coordinates**, as an event, a parameter or a user property. Knowing that someone
  uses a prayer app is already sensitive, which is why consent is explicit. Sending which school
  of thought they follow would cross a line the owner has not been asked to cross. Anyone who
  later wants "which madhab is most popular" must come back to the owner and the privacy policy
  first. Record this in HANDOVER §11.

### 7.3 Drafts (final only after Phase 1)
**Onboarding step.** Title: *Help us see how SajdaTime is used?* Body: *If you say yes,
SajdaTime sends usage counts to Google Analytics: how many people use it, how often, for how
long, which main screen you open, and your approximate area, which Google works out from your
internet connection. They
are tied to a random ID, not your name. It never sends your GPS position, the place you type or
your prayer settings. It is off unless you choose yes, and you can switch it off in Settings
at any time.* Link: *Read the privacy policy.* Buttons, equal weight: **Yes, share usage
counts** / **No thanks**.

**Settings row.** *Share usage counts*. Description: *Off unless you turn it on. Not your GPS
position or the place you type.*

**`about_privacy_desc`.** *Your location stays on this phone. No accounts, no ads. Usage counts
are optional and off unless you turn them on. Tap to read the full policy.*

**`privacy.html`.** New lede and a new section "Optional usage counts" covering what is sent
and not sent, who receives it, why, retention (event level 14 months, aggregates longer), how to
stop and what that does not delete, that nothing is sold or used for advertising, and that the
option is for adults. Fix "The one time something leaves your device", the permissions list, the
children section and the "we have no server" lines. New date.

**Play short description (80 limit).** Current ends *No tracking.* (79). Candidate: *Offline
prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.* (79). `No accounts`
has never been tested alone in the Console. Use the method in `LISTING.md` lines 20 to 100.

**Play long description privacy block (about 296 characters headroom).** Replace *No analytics,
no crash reporting, no tracking of any kind* with *Optional usage counts, off unless you switch
them on. No accounts. No crash reporting.* Check length and the wording rules in `LISTING.md`.

**Feature graphic.** `LISTING.md` lines 470 to 481 say "No ads" was removed as a promotion
word. Do not bring it back. Use *Sunni & Shia · No accounts*.

**Data safety answers (prepared by the assistant, entered by the owner).** *Device or other
IDs*, *App activity*, *App info and performance*: collected, optional, purpose Analytics,
encrypted in transit. *Approximate location* gains Analytics as a purpose. The "shared" answer
depends on the console sharing settings (UNVERIFIED, read Google's disclosure page in
Phase 1). No "deletable on request". Advertising ID stays No only if the merged manifest check
passes.

## 8. Verification

**Automated:** default flag false; manifest guard (AD_ID removed, collection disabled) with the
manifest and any new `bools.xml` added to the `inputs.files` block in `app/build.gradle.kts`,
and **proof that the guard fails when the manifest is broken** (HANDOVER §15 lesson 84);
overrides exist for debug, rtl and sideload; no new string contains "duas"; disclaimer and
no translation tests stay green; onboarding order and Back in both directions; every variant
builds.

**Manual:** opted out run (fresh install, `adb shell setprop log.tag.FA VERBOSE`, logcat and a
traffic capture across cold start, a boot broadcast, an alarm receiver start and a WorkManager
run: nothing to Google); opt in shows events in DebugView and records whether a session that
began before opting in is counted; on, off, on, kill, relaunch; airplane mode; emulator image
without Google Play services; RTL layout of the new screen; merged manifest diff; bundle size
before and after.

**Cannot be proved by us, stated plainly:** what Google stores server side, real world opt in
rates, behaviour on every manufacturer's phone.

## 9. Rollback
Before release: revert the branch, nothing has left any device. After release: only opted in
users are affected. Ship a version without the dependency, delete collected data in the
Firebase console (this does not reach anything already exported), and **revert the Data safety
form and policy with their own Console submission and a new policy date**.

## 10. Risks
| Risk | Severity | Mitigation |
|---|---|---|
| Trust: "no tracking" is part of the app's identity and testers were told it | High | Opt in, honest wording, say it in release notes |
| Copy contradicts behaviour somewhere | High | Section 7.1 plus the final grep |
| Overclaiming ("anonymous", "no location", "we don't store IPs") | High | Section 7.2 |
| Traffic before consent | Medium | Capture test and the stop rule |
| Late or missing sessions if approach B is needed | Medium | Prove before use, otherwise stop |
| Play rejects the Data safety or policy change | Medium | Declare precisely, allow review time |
| Debug or sideload builds pollute the data | Medium | Three overrides and a test |
| Devices without Google Play services | Medium | Emulator test |
| Sample too small or biased | Medium | Say so, use Play Console for totals |
| Retention default may be 2 months | Medium | Set on the console screen |
| Scope creep into events and funnels | Medium | Out of scope |

## 11. Sign off (recorded 3 Oct 2026)

Given by the owner in conversation. Items 4 and 5 were delegated: "I trust you to make the best
decision." The owner's stated purpose: *to see what is doing well and what is not, so he can
make improvements and plan the next phases.*

1. **Approved.** Change the rule from "no analytics" to "no analytics except optional usage
   counts, phone only", and rewrite `CLAUDE.md` and HANDOVER to match.
2. **Approved.** Exception to the "asked once" rule for the consent screen only.
3. **Approved.** No pop up for existing users.
4. **Decided by the assistant, on delegation:** onboarding step plus Settings switch. A
   Settings switch alone would, in the assistant's judgement, give close to no data, which would
   defeat the purpose.
5. **Decided by the assistant, on delegation:** no "delete on request" promise. It cannot be
   kept honestly, because resetting analytics destroys the only ID that could find a person's
   records. The policy explains this plainly.
6. **Added by the assistant (v4), reversible:** one screen view per main tab, and the permanent
   "never send sect, madhab, method or settings" rule in 7.2.

Still needed from the owner, later and only as guided sessions: the Firebase console session,
the Play Console session and pressing Publish (section 5).

## 12. Kickoff prompt for the session at the computer

> Read `CLAUDE.md`, then `docs/ANALYTICS_PLAN.md` in full. Section 11 is already signed
> off (recorded 3 Oct 2026), so do not ask me again. Start at Phase 0 and work through the runbook in section 6.
> Close every UNVERIFIED row you can in Phase 1 and correct the plan before coding. Stop at
> each gate if it fails. Walk me through the Firebase and Play Console sessions one screen at a
> time, and never press submit or publish for me. Commit with reasoning and push as you go.

## 13. Review log
**v2** (adversarial review): dropped the Consent Mode defaults (second switch); removed the
deletion promise; reworded "anonymous" and "never your location"; three source set overrides;
no Firebase calls for users who never opted in; unused flag cut; capture test, sharing settings,
onboarding rewiring, inventory gaps and rollback added.
**v3** (fact check and effort rewrite): added evidence labels and the fact check table;
**demoted approach B** because late Analytics initialisation is doubtful; added the stop rule;
corrected the location and IP wording to match Google's published behaviour and to attribute
it to Google; recorded that nothing could be built here; split owner and assistant work into
two short guided sessions; added the phased runbook, gates and the kickoff prompt. Still open:
every UNVERIFIED row in section 2.
**v4** (sign off): recorded the owner's approvals and the delegated decisions; added one screen
view per main tab so the owner can see which part of the app is used; added the permanent rule
never to send sect, madhab, method or settings.
