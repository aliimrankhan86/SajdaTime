# Phase two: optional usage counts (plan v6, approved by the owner, not yet built)

**Status: v6. Owner sign off recorded 3 Oct 2026 (section 11). No app code has been changed.**
The build waits for a session on the owner's computer (section 6).
Written and revised 3 Oct 2026 (history in section 13). When built, fold the decisions into
`docs/HANDOVER.md` (§2, §5, §8, §11, §15) and `CLAUDE.md`, then delete or archive this file.

## 0. How to read this plan

Every factual claim carries a label. **A claim with no label is the author's judgement.**

- **CONFIRMED**: seen in an official Google or Firebase page (via search result text) or in this
  repo, with the source named.
- **PARTLY**: third party sources only, or the iOS page where Android is assumed the same.
- **UNVERIFIED**: could not be checked. A test or a console screen settles it.

**Limits of the planning session:** Google's own pages (firebase.google.com,
support.google.com, dl.google.com, Maven metadata) were blocked, so nothing was read from them
directly. There is **no Android SDK** in that environment, so nothing was compiled, linted or
run. The plan makes no claim that any code works. Two independent reviews (one by a stronger
model) were run on earlier versions, and their findings are in section 13.

## 1. Decision

Add **optional, opt in usage counts** to the **phone app only**, using Firebase Analytics, so the
owner can see how people use SajdaTime, what makes sense to them, and where they get stuck, in
order to make better decisions about future work.

This **reverses a written project rule** ("no analytics", `CLAUDE.md` lines 60 to 61 and 134,
`HANDOVER.md` §11 non-goals about line 6222, and several published statements). Approved by
the owner (section 11).

| Decided | |
|---|---|
| Tool | Firebase Analytics, free |
| Consent | Opt in, off by default |
| Scope | Phone only. Watch untouched |
| Collected | Automatic metrics, plus one screen view per main tab, plus two fixed setup events (4.3) |
| Never sent | Sect, madhab, calculation method, alert or prayer settings, typed city, coordinates. As an event, a parameter, a user property, or by the **shape** of what is sent (B1 in section 13) |

**Rejected:** Aptabase (tracks nothing automatically, needs event code for every metric); self
hosted Umami, Plausible, Matomo (needs a server); on by default (weakest privacy position, and
the UK "statistical purposes" exemption is narrow, UNVERIFIED); Crashlytics (out of scope, Play
vitals covers crashes); custom backend (cost and security burden).

## 2. Fact check

| Claim | Status | Source or test |
|---|---|---|
| `setAnalyticsCollectionEnabled` persists across launches and overrides the manifest default | CONFIRMED (iOS page text; Android seen only as a summary) | Firebase "Configure Analytics data collection". Prove on Android with the on, off, on, relaunch test |
| Manifest `..._collection_enabled=false` is the documented way to wait for consent | CONFIRMED | Same page |
| The "deactivated" key overrides any runtime call, so never use it | CONFIRMED on iOS, PARTLY on Android | Same page |
| Google Analytics does not log or store IP addresses but derives city, region and country from them | CONFIRMED | Google "Privacy controls in Google Analytics" (support.google.com/firebase/answer/9019185) |
| New GA4 properties keep event data 2 months by default, 14 selectable free | PARTLY | Third party guides plus Google retention text. Settle on the console screen |
| Manual `FirebaseApp.initializeApp` after startup is possible | CONFIRMED | Firebase blog "Take control of your Firebase init" |
| Manual init is **safe for Analytics** | **UNVERIFIED, doubtful** | Late init may miss `first_open` and the first `session_start` |
| With collection disabled, Firebase sends nothing before consent | **UNVERIFIED** | Settled only by the opted out capture test (8) |
| Firebase can be configured from plain string resources (`google_app_id` and friends) with no plugin or JSON | **UNVERIFIED** | Spike in Phase 1. If it works it removes the plugin, the AGP 9.3.1 risk and the extra app registrations. Otherwise use the plugin |
| Analytics falls back to the Android ID when the advertising ID is off | UNVERIFIED (one forum claim) | Only changes policy wording |
| Versions BoM 34.19.0, firebase-analytics 23.2.0, google-services 4.4.4 | UNVERIFIED (search summaries) | Gradle resolves the real latest. Do not hard code these |
| google-services plugin works with AGP 9.3.1 and built in Kotlin | UNVERIFIED | Only matters if the resource route fails |
| Merged manifest gains `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, probably install referrer permission and `<queries>` | UNVERIFIED | Diff the merged manifest |
| Play Data safety accepts a CSV import | UNVERIFIED (memory) | Look on the day, otherwise fill by hand from section 7.3 |
| Uploads from Play services devices may not appear under the app's own process in a traffic capture | PARTLY (reviewer) | Capture whole device traffic (8) |
| Existing installs about 22 as of 7 Sept (HANDOVER) | CONFIRMED but stale | Re-read Play Console before quoting |
| Reports lag about a day; rows hidden at small audiences | UNVERIFIED | Observe |
| Instance ID is personal data; consent needed for storing or reading it on the device (PECR) | PARTLY, **legal position UNVERIFIED** | Check the ICO guidance in Phase 1 |
| Religious belief is special category data, so using a prayer app may itself be sensitive | PARTLY, **legal position UNVERIFIED** | Check ICO guidance on Article 9 in Phase 1. We are conservative regardless |

## 3. What the owner will be able to see

| Question | Where | Notes |
|---|---|---|
| How many users | Active users, daily/weekly/monthly (Firebase) | **A sample.** Opted in users only |
| How often / how long | Sessions per user, engagement time | Foreground time only |
| Do they stay | Day 1, 7, 28 retention | Needs enough opted in users |
| Where from | Country, usually city | Derived from the connection, not GPS |
| Which part is used | Screen views per main tab | Says nothing about features inside a screen |
| Where setup loses people | Setup step reached, permission results | **Blind spot:** anyone who leaves before the consent step, or says no, is invisible by design |
| Device, Android, app version | Standard reports | |
| True installs | **Play Console** | The only unsampled headcount |
| Whether a future feature is wanted | **Not answerable by this data** | Usage shows what people do with what exists. Demand for something new needs another source (reviews, asking users) |

Caveats to repeat: opt in rates are low (author's judgement), so use Firebase for ratios and
Play Console for the headcount; notification, lock screen and watch use never opens the app;
small audiences give sparse reports (UNVERIFIED).

## 4. Design

### 4.1 Approach
**A. Standard setup.** Firebase initialises at startup but is told not to collect
(`firebase_analytics_collection_enabled` false). `setAnalyticsCollectionEnabled(true)` after the
user opts in. The Phase 1 spike decides how it is configured: **A1** plain string resources, no
plugin (preferred if it works), or **A2** the `google-services` plugin and JSON.

**Where the values live:** the app ID, project ID and API key are committed in the repo as
resources or JSON. Google treats them as public identifiers (PARTLY confirmed) and every APK
ships them anyway, but the repository is public (`docs/` is published, `RELEASING.md` lines 474
to 479), so expect possible secret scanning or Google emails about an exposed key and do not
treat them as a leak. Never commit anything that is a real secret, such as a service account key.

**B. Manual init after consent** is a **fallback only**, used if the opted out capture shows
traffic under A. It must first prove it records `first_open` and sessions.

**Stop rule:** if A leaks traffic before consent and B cannot record sessions, do not ship. Tell
the owner plainly. No compromise on "off means off".

### 4.2 Manifest (app only)
- `firebase_analytics_collection_enabled` = false.
- `google_analytics_adid_collection_enabled` = false (key name PARTLY confirmed).
- Remove `com.google.android.gms.permission.AD_ID` with `tools:node="remove"` (needs
  `xmlns:tools`, which the phone manifest lacks).
- No Consent Mode default keys (a second switch that could disagree). Whether `setConsent` is
  also needed is UNVERIFIED, so the test must show events arriving after opt in.
- Never use the "deactivated" key. Fix the `INTERNET` comment (`AndroidManifest.xml` line 11).
- Every merged permission addition goes into `privacy.html`.
- The merged manifest diff must show `FirebaseInitProvider`. Adding the SDK merges it, so Firebase
  **does** initialise at process start for every user, opted in or not. The manifest flag is the
  only control and the opted out capture is the proof. No copy, policy or HANDOVER text may claim
  Firebase is never initialised. Whether a disabled SDK writes a local app instance ID is
  UNVERIFIED and matters for PECR, so Phase 1 checks it.

### 4.3 Code (smallest diff, copies `ongoingBadge` and `disclaimerSeen`)
1. `AppSettings.analyticsEnabled: Boolean = false`. No "prompt seen" flag.
2. Key, setter, mapping in `SettingsRepository.kt`. No migration.
3. `SajdaViewModel` wrapper threaded through `MainActivity`, `MainScaffold`, `SettingsScreen`.
4. About 10 to 15 lines in the data layer. **The enable call must complete before the first
   event is logged.** Enabling happens in the same step as the user pressing "Yes", not later
   "when settings load", so the first `setup_step` is not lost or sent while disabled. On true to
   false: disable and call `resetAnalyticsData()`. For users who never opted in, every logging site goes through one guard that returns early when the flag is false, so the app makes no Analytics call for them. That reduces what runs but is **not** the privacy control (see 4.2: the SDK initialises regardless).
5. No `Application` subclass. Fix the "never transmitted" KDoc (`SettingsRepository.kt` about 44).
6. **Tab screen views:** one `screen_view` per main tab, only while opted in, `screen_name` from a
   fixed set (`times`, `qibla`, `settings`, plus any other top level tab on the day). No other
   parameters. The app is single activity, so per tab data will not appear on its own
   (UNVERIFIED for Compose). If the navigation makes this more than a few lines, stop and tell
   the owner.
7. **Setup counts.** Exactly two event names, fixed, only while opted in:
   - `setup_step` with one parameter `step`, drawn **only from steps every user passes through**:
     `permission`, `sect`, `method`, `confirm`, `finish`. **Never `madhab`.** The madhab step is
     shown only to Sunni users (`OnboardingScreen.kt` lines 127 and 152), so logging it, or
     logging anything that appears for one sect only, would reveal sect. A unit test asserts the set of step names that can be sent does not depend on sect.
     **Semantics:** a ViewModel method `recordSetupStep(name)` keeps an in-memory set and logs
     each name at most once per onboarding run, called from a `LaunchedEffect(step)` in
     `OnboardingScreen` on entry to the step. This stops a Back press or a `rememberSaveable`
     restore re-emitting a step, which would make a Sunni user's sequence differ in shape.
     `finish` is logged from `onFinish` and is not a `Step`. Skipped steps are never logged.
   - `permission_result` with `permission` (`notifications`, `location`, `exact_alarm`) and
     `granted` (`true` or `false`). Yes or no only. Real behaviour, from the code:
     - **notifications:** log from the existing `notificationPermission` callback
       (`MainActivity.kt` line 66, currently a no-op). Below Android 13 the prompt never fires,
       so log nothing.
     - **location:** log from the `locationPermission` callback (`MainActivity.kt` line 62) by
       reading its result map, **only on the onboarding path** (the first callback while `onboardingComplete` is false), once. That launcher also serves
       other screens. After two refusals Android answers "denied" with no dialog, which is
       counted as denied.
     - **exact_alarm:** there is **no result callback** (`requestExactAlarmPermission` just opens
       a settings screen, `PrayerAlarmScheduler.kt` line 204). Log the state once at `finish`
       using `canScheduleExact`, and name it as a **state, not a decision**, because on many
       Android versions it is granted by default and "false" mixes refusal with never asked. Say
       so in the docs.
   - These two events are the whole event layer. No wrapper class, no timestamps, no sequence
     numbers, no other parameters or user properties. Adding any needs the owner's yes and a
     policy update first.
   - Residual, accepted: Google stamps events with time, so a Sunni user's gap between `sect`
     and `method` is longer. Not linkable to a person without exporting user level data, so
     **do not enable the BigQuery export**.
   - Custom parameters may need registering in the Firebase console before they show in
     reports (UNVERIFIED, memory). Add to Session 1 if the console asks.

### 4.4 Build variants
`debug` shares the release package name. `rtl` and `sideload` use `initWith(debug)`, which does
not inherit the `debug` source set (matches the repo).
- Create `app/src/debug` and use `app/src/rtl` and `app/src/sideload`, each setting
  `R.bool.analytics_allowed` to false. `main` sets true.
- **When false, the UI is still shown** (consent step, Settings switch) so layout, RTL and the
  Redmi check can see it. **Only the SDK calls are gated** and do nothing. A test asserts the
  three overrides exist and that release resolves true.
- The analytics path itself (events, DebugView, capture) is tested on an **emulator** with the
  flag temporarily on in a local, uncommitted build. It cannot be tested on the owner's phone
  through `installSideload`, because the flag is off there by design. Say this to the owner
  rather than let him think the phone check covered it.
- Nothing Firebase goes into `:wear`. Under A2 the JSON also needs clients for `.rtl` and
  `.sideload`.

### 4.5 Consent UX
- **One shared consent composable** with the full text and two equal buttons, used in both places
  below. Suggested names: strings prefixed `consent_`, composable
  `ui/components/UsageCountsConsent.kt`.
- **New installs:** a new onboarding step straight after `WELCOME` and before `PERMISSION`, so
  the setup counts can see the later steps. Rewire both directions (`OnboardingScreen.kt`, enum
  line 79, wiring lines 108 to 156): `WELCOME` next goes to consent, consent goes to
  `PERMISSION`, and `PERMISSION` back goes to consent. If the flag is already true (for example after process death), skip the step. The notification prompt still fires from `onFinish`, after
  consent, so this does not change when it appears.
- **Everyone:** a Settings row in About, placed straight after the Privacy row (`SettingsScreen.kt` line 265)
  and before Charity, off by default. It is **not** the existing `SwitchRow` (line 846), which
  toggles on a row tap, so it needs its own row that opens the dialog when turning on. **Turning it on opens
  the same consent dialog** (full facts, Yes or No thanks). Turning it off is a single tap. A
  one line hint on a switch is not enough for existing users, who have no other route.
- **Existing users:** no pop up (owner decision). They find the switch.
- Not bundled with any feature. Saying no changes nothing.
- **Dua rule untouched.** Never in or after the disclaimer, never the word "duas".
- **Exception to write into HANDOVER §11:** the "asked once" rule was written to ban rating and
  share prompts. Consent is different and required. Record it so a later session does not
  delete the screen.

### 4.6 Opting out and deletion
Switching off disables collection and resets local analytics data and the instance ID. **Data
already sent stays with Google** and ages out under the retention setting. **No "delete on
request" promise**: the instance ID is the only handle and the reset destroys it. The policy says
so. The 14 month setting covers event data; aggregates last longer, so the policy must not say everything vanishes at 14 months.
On reinstall, `allowBackup=false` and the data extraction rules wipe the flag, so consent is
asked again and a new instance ID is created. The only record of consent is the local flag. The
policy says both.

### 4.7 Versions
Phone: `versionCode` 5, `versionName` 1.3.0. **The watch stays at 1001 / 1.2.0.** It has no
change, so there is no second upload and no Wear review. This departs from the comment in
`wear/build.gradle.kts` lines 38 to 41 ("kept in step with the phone") and `RELEASING.md`
line 1110 (bump both), so update that comment and record the exception in HANDOVER §11.
Reversible: if the owner prefers matching versions, bump the watch to 1002 / 1.3.0 and upload
both. Release notes text is in 7.3.

## 5. Who does what

**Browser access is NOT assumed.** In the planning session no tool could control the owner's
Chrome (checked 3 Oct 2026: only Figma, Atlassian, GitHub, Vercel and similar connectors exist).
The container's own headless Chromium is not his browser, holds no logins and cannot reach
Google's console pages. So Phase 0 checks whether the computer session can drive his Chrome
(for example through a browser extension that is connected to it). If it can, the assistant
navigates and reads the Firebase and Play Console screens itself, and the owner's part shrinks
to **signing in, accepting Google's terms and pressing Publish**, which stay with him either
way. If it cannot, the guided screenshot method in this section is used. Nothing else in the
plan depends on it.

**Required session type:** a Claude Code session with a shell on the owner's computer, able to
run Gradle, `adb` and the emulators. A chat or Console bridge session cannot (`CLAUDE.md`), and
then the owner would have to paste every command. Phase 0 checks this.

**Owner effort, estimates not measurements: two guided sessions of roughly 30 to 45 minutes
(Firebase) and 20 to 30 minutes (Play Console), plus an optional two minute phone check.** The
assistant writes every word the owner pastes.

| Session | Owner does | Assistant does |
|---|---|---|
| **Session 1: Firebase** | Sign in to the **same Google account used for Play Console** (owner decision). One screen at a time, with a screenshot each: create the project; on the **Enable Google Analytics** screen choose a Google Analytics account and the **Analytics location** (it decides whether "processed outside the UK" is accurate, so the assistant records the choice); **accept Google's terms and data processing terms (the owner's signature, the assistant stops here)**; register the app `com.sajdatime.app`; then in Analytics Admin set Data settings > Data retention to 14 months, Data collection > Google signals off, advertising features off, and in Account settings **every data sharing option off**; and send back the app ID, project ID and API key **and** `google-services.json`, so either approach can proceed | Says what to click on each screen. Reads the values. Confirms each setting from the screenshots. **No API key restriction step**: it is a public identifier and restricting it is a detour |
| **Everything between** | Nothing, except one **five minute check in Phase 4**: open Analytics > DebugView and send a screenshot while the assistant drives the emulator | Phases 1 to 6 (section 6) |
| **Session 2: Play Console** | **One submission, in this order:** save Data safety (including the Advertising ID answer), save the listing text, upload the **phone** bundle with release notes, then a single **Send for review** and **Publish**. Do not edit the listing or App content while a review is open (`RELEASING.md` lines 1122 to 1123). "Updated on" moves only at rollout | Prepares the answers and text. Guides each screen. Never presses submit |
| **Optional** | Plug in the Redmi for a layout and first run check | `installSideload`, never `installDebug`. Tells him this build has analytics switched off |

The assistant will **not** touch the signing key, press submit or publish, agree to policies, or
enter credentials.

## 6. Runbook

First message: the kickoff prompt (section 12). Phases run in order. Each has a gate. If a gate
fails, stop and report. **Keep everything on a feature branch and squash merge only after Phase 5**,
because `CLAUDE.md` requires `privacy.html` to change in the same commit as any data handling change. A squash merge satisfies that. `privacy.html` goes live when the merge reaches main (GitHub Pages), days before the release is approved. That is acceptable. Set its date at merge.

| Phase | Work | Gate |
|---|---|---|
| 0 | Read `CLAUDE.md`, **check whether Chrome can be driven (see section 5)**, HANDOVER §11 STATE OF PLAY, this plan. Fetch the public Play listing. **Confirm the session has a shell with Gradle, adb and emulators.** Create the feature branch | Sign off recorded, tooling present |
| 1 | Close every UNVERIFIED row a browser can close. **Compile spike A1 versus A2 with placeholder values** on a throwaway branch and run the **opted out capture only** (the opt in half needs real values, so it moves to Phase 4). Check ICO guidance on PECR, Article 9 and the Children's Code, and Firebase's Data disclosure page. Update this plan | Nothing left UNVERIFIED that a page can settle; both approaches compile; approach chosen |
| 2 | Session 1 with the owner | Values or JSON in place, settings confirmed from screenshots |
| 3 | Code (section 4), tests (8), variant overrides | `./gradlew clean test lint` green, every variant builds |
| 4 | Manifest diff against the current release. Opted out capture. Opt in proof **from the device**: `adb shell setprop log.tag.FA VERBOSE` and `log.tag.FA-SVC VERBOSE`, then look for successful upload lines for all three event types. Relaunch test. No Google Play services emulator. RTL layout check of the new screen (a layout check only) | Opted out: no traffic. Opt in: device log shows uploads. Stop rule applies. DebugView in the console is confirmed by the owner in the five minute check |
| 5 | Copy and docs (section 7), including a short **data protection impact note** the assistant drafts. Repo wide grep for `tracking`, `analytics`, `telemetry` excluding this plan and `docs/reviews/`. Retake `05-settings.png` unconditionally (the new row sits in About) | Grep clean, disclaimer tests green |
| 6 | `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`, both emulators. Squash merge the branch. The wear bundle is built as the gate only and is not uploaded (4.7) | Green, sizes recorded |
| 7 | Session 2 with the owner | Owner presses Publish |
| 8 | Confirm "Updated on" moved, ask the owner for an Analytics > Realtime screenshot (optional), write STATE OF PLAY and lessons into §15 (including the sect shape trap) | Recorded and pushed |

## 7. Copy and documents

### 7.1 What becomes false
| File | Where | Problem |
|---|---|---|
| `app/src/main/res/values/strings.xml` | `about_privacy_desc` (about 348), `permission_why_body` (about 31) | "no analytics"; "never track you" |
| `AndroidManifest.xml` | 11 and 39 to 44 | INTERNET comment; "the one request this app makes is HTTPS" |
| `SettingsRepository.kt` | about 44 | KDoc |
| `docs/privacy.html` | 37, 39 to 40, 48 to 49 ("no server ... nowhere for them to go"), 53 to 70, 80 to 83, 91 to 92, 96 to 102 | date, lede, "one time", analytics, SDKs, sharing, children, permissions |
| `docs/index.html` | 39 | "no ads or tracking" |
| `README.md` | 5 | "No analytics. No tracking." |
| `docs/store/LISTING.md` | 21, long description privacy block (about 170 to 182, including "no server for them to go to"), 327, 351, 355, 357, 364, 480 | descriptions, Data safety, Advertising ID, graphic text |
| `tools/build-store-assets.sh` | 130 | subtitle, then regenerate the PNG |
| `CLAUDE.md` | 60 to 61 **and 134** | founding rule; "Location stays on the device... never transmitted" must become: the device's location never leaves the phone, and for users who opt in Google estimates an approximate area from the internet connection |
| `docs/HANDOVER.md` | 50, §2 networking and Play Services rows (about 109 to 138), §8, §10 (2079 to 2085), §11 non-goals (about 6222) and the rating rule (6226 to 6236), 6706, STATE OF PLAY | vision, stack, privacy model, audit table, non-goals |
| `docs/RELEASING.md` | 60, 171, 727, 764, 988 | tester and outreach copy |
| `docs/NEW-SESSION.md`, `docs/ARCHITECTURE.md` | 179, 406 | "no tracking" (check context) |

Leave as history: `docs/reviews/`, HANDOVER line 6195, LISTING lines 28, 53, 75, 96. Line
numbers are from the 3 Oct tree and may drift.

### 7.2 Wording rules (override any draft that conflicts)
- Say **"random ID, not your name"** or "pseudonymous". Not "anonymous" alone.
- Say "approximate area, usually country and city, worked out by Google from your internet
  connection. Never your GPS position or the place you type." Never "never your location".
- Attribute the IP statement: "According to Google, it does not log or store IP addresses in
  Analytics."
- Name Google, say data may be processed outside the UK including the US (UNVERIFIED, check),
  link the policy.
- Never promise deletion on request.
- **Tone: friendly and low key, never understated.** Avoid the word "tracking". Use "usage
  counts" and "help us improve". On marketing surfaces (store short description, feature graphic,
  website tagline) keep it brief. On every consent surface, **including the Settings row**, state
  the material facts: Google receives it, it is tied to a random ID, what is and is not sent.
  Never use "counts" alone where the body text is absent. Reason: consent rules require clear
  information and Play Data safety must match behaviour. No surface may deny it.
- **Children:** do not write "for adults". The ICO test is whether children are likely to access
  the app, and the Play audience includes 13 to 17 (HANDOVER about line 5400). Keep it off by
  default, keep the language simple, and cover it in the impact note.
- **Permanent rule: never send sect, madhab, calculation method, alert or prayer settings, a
  typed city or coordinates,** as an event, a parameter, a user property, or by the shape of the
  events sent. Anyone who later wants "which madhab is most popular" must go back to the owner and
  the policy first. Record in HANDOVER §11.

### 7.3 Drafts (final only after Phase 1)
**Consent dialog and onboarding step.** Title: *Help us see how SajdaTime is used?* Body: *If you
say yes, SajdaTime sends usage counts to Google Analytics. They cover how many people use the
app, how often and for how long, which main screen you open, which setup steps you reach,
whether you allow notifications and location, and whether exact alarms are allowed (a yes or no, never the location itself),
your phone model, Android version, app version and language, and your approximate area, which
Google works out from your internet connection. They are tied to a random ID, not your name, and
may be processed outside the UK, including in the US. Because using a prayer app can say
something about your faith, this is your choice and nothing changes if you say no. It never sends
your GPS position, the place you type, your school of thought or your prayer settings. You can
switch it off in Settings at any time.* Link: *Read the privacy policy.* Buttons, equal weight:
**Yes, share usage counts** / **No thanks**.

**Settings row.** *Share usage counts*. Description: *Usage counts tied to a random ID, sent to
Google. Off unless you turn it on. Tap to see exactly what is sent.* (Turning on opens the dialog above.)

**`about_privacy_desc`.** *Your location stays on this phone. No accounts, no ads. Usage counts
are optional and off unless you turn them on. Tap to read the full policy.*

**`privacy.html`.** New lede and a new section "Optional usage counts": what is sent and not
sent, who receives it, why, retention (event level 14 months, aggregates longer), how to stop and
what that does not delete, that nothing is sold or used for advertising, the children position,
and the exact alarm "state, not decision" note. Fix "The one time something leaves your device",
the permissions list, the children section and the "we have no server" lines. New date.

**Play short description (80 limit).** Current ends *No tracking.* (79). Candidate: *Offline
prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.* (79). `No accounts` has
never been tested alone in the Console. Use the method in `LISTING.md` lines 20 to 100.

**Play long description privacy block (about 296 characters headroom).** Replace *No analytics,
no crash reporting, no tracking of any kind* with *Optional usage counts, off unless you switch
them on. No accounts. No crash reporting.* Check length and the wording rules in `LISTING.md`.

**Feature graphic.** `LISTING.md` lines 470 to 481 say "No ads" was removed as a promotion word.
Do not bring it back. Use *Sunni & Shia · No accounts*.

**Release notes, 1.3.0 (Play "What is new", must match the change).** *New: an optional setting to
share usage counts, so we can see how SajdaTime is used and improve it. It is off unless you turn
it on. Your prayer times and your location are not affected.*

**Data safety answers (prepared by the assistant, entered by the owner).** Likely categories:
*Device or other IDs*, *App activity*, *App info and performance* (doubtful without Crashlytics,
**UNVERIFIED**), and *Approximate location* gaining Analytics as a purpose (depends on how
Google classifies IP derived area, **UNVERIFIED**). All collected, optional, purpose Analytics,
encrypted in transit. "Shared" depends on the console sharing settings. No "deletable on
request". Advertising ID stays No only if the merged manifest check passes. Settle every row from
Google's Firebase "Data disclosure" page in Phase 1.

## 8. Verification

**Automated:**
- Default flag false.
- Manifest guard (AD_ID removed, collection disabled) with the manifest and any new `bools.xml`
  in the `inputs.files` block of `app/build.gradle.kts`, **and proof the guard fails when the
  manifest is broken** (HANDOVER §15 lesson 84).
- Overrides exist for debug, rtl, sideload; release resolves true.
- When opted out, the logging guard makes no Analytics call (the guard is not the privacy control, the opted out capture is).
- Event names, parameter names and values come from a fixed allow list.
- **The set of `setup_step` values that can be sent does not depend on sect.**
- Collection is enabled before the first `setup_step` after "Yes".
- No duplicate events on Back, recomposition or `rememberSaveable` restore.
- No new string contains "duas"; disclaimer and no translation tests stay green; onboarding
  order and Back in both directions; every variant builds.

**Manual, on an emulator with the flag temporarily on:**
- Opted out: fresh install, `adb shell setprop log.tag.FA VERBOSE`, logcat, and a **whole device
  traffic capture matched by hostname** (`app-measurement.com` and other Google analytics hosts),
  across cold start, a boot broadcast, an alarm receiver start and a WorkManager run. Nothing
  may go to Google.
- Opted in: DebugView shows each event type; record whether a session that began before opting
  in is counted.
- On, off, on, kill, relaunch: state honoured every time.
- Airplane mode; an emulator image without Google Play services; RTL layout of the consent
  screen; merged manifest diff; bundle size before and after.

**Cannot be proved by us:** what Google stores server side, real world opt in rates, behaviour on
every manufacturer's phone.

## 9. Rollback
Before release: revert the branch, nothing has left any device. After release: only opted in
users are affected. Ship a version without the dependency, delete collected data in the Firebase
console (this does not reach anything already exported), and **revert the Data safety form and
policy with their own Console submission and a new policy date**.

## 10. Risks
| Risk | Severity | Mitigation |
|---|---|---|
| Event shape reveals sect | High | Allow list, sect independence test, 4.3 item 7 |
| Trust: "no tracking" was part of the app's identity and testers were told it | High | Opt in, honest wording, release notes |
| Copy contradicts behaviour somewhere | High | 7.1 plus the final grep |
| Overclaiming or understating | High | 7.2 |
| Weak consent for existing users | High | Switch opens the full consent dialog |
| Traffic before consent | Medium | Whole device capture and the stop rule |
| Play rejects Data safety or policy changes | Medium | Declare precisely, allow review time |
| Debug or sideload builds pollute data | Medium | Three overrides and a test |
| Devices without Google Play services | Medium | Emulator test |
| Sample too small or biased, blind spot at consent | Medium | Say so, use Play Console for totals |
| Retention default may be 2 months | Medium | Set on the console screen |
| Event creep beyond the two approved events | Medium | Section 4.3 item 7 requires the owner's yes |

## 11. Sign off (recorded 3 Oct 2026)

Given by the owner in conversation. Stated purpose: *understand how people use the app, what
makes sense to them and what does not, and use that to decide future features.* Items marked
"delegated" were left to the assistant: "I trust you to make the best decision."

1. **Approved.** Change the rule to "no analytics except optional usage counts, phone only", and
   rewrite `CLAUDE.md` and HANDOVER to match.
2. **Approved.** Exception to the "asked once" rule for the consent screen only.
3. **Approved.** No pop up for existing users.
4. **Delegated, decided:** onboarding step plus Settings switch.
5. **Delegated, decided:** no "delete on request" promise.
6. **Delegated, decided, reversible:** one screen view per main tab, and the permanent "never
   send sect, madhab, method or settings" rule.
7. **Approved.** Drop "No tracking" from the Play short description and feature graphic, use "No
   accounts". Keep marketing mentions brief and friendly; consent surfaces state the full facts.
8. **Approved.** Firebase project under the same Google account as Play Console.
9. **Approved.** The two setup events in 4.3 item 7. The consent step sits straight after
   Welcome so later steps can be counted.
10. **Decided:** analytics ships first as its own small release (1.3.0) so a baseline exists.
    Phase two scope is otherwise undefined by the owner.

11. **Delegated, decided, reversible:** phone 1.3.0 (versionCode 5), watch stays 1.2.0 (1001),
    see 4.7.

Still needed from the owner: the two guided sessions, the five minute DebugView check and
pressing Publish (section 5).

## 12. Kickoff prompt for the session at the computer

> Read `CLAUDE.md`, then `docs/ANALYTICS_PLAN.md` in full. Section 11 is signed off, so do not
> ask me again. Start at Phase 0 and work through section 6, stopping at any failed gate. Close
> every UNVERIFIED row you can in Phase 1 and correct the plan before coding. Keep all work on a
> feature branch and merge only after Phase 5. Walk me through the Firebase and Play Console
> sessions one screen at a time, never press submit or publish for me, and stop where I have to
> accept Google's terms. Commit with reasoning.

### Day one checklist (first ten actions)
1. Read `CLAUDE.md`, HANDOVER §11 STATE OF PLAY, this plan. `curl` the public Play listing and
   confirm "Updated on" (1.2.0 live).
2. Confirm the shell can run `./gradlew`, `adb` and list emulators. Stop if not.
3. Create the feature branch. The merge will be a squash.
4. Re-read this plan against the repo for drift (line numbers move), and fix it.
5. Phase 1 spike: add `firebase-analytics` (Gradle resolves the BoM), try A1 with placeholder
   resources, `./gradlew :app:assembleDebug`, diff the merged manifest (expect
   `FirebaseInitProvider`, AD_ID removed, `ACCESS_NETWORK_STATE`, `WAKE_LOCK`).
6. Opted out capture on an emulator (`emulator -tcpdump out.pcap`, match DNS and SNI for
   `app-measurement.com` and `firebaseinstallations.googleapis.com`) across cold start, boot
   broadcast, alarm and WorkManager. Gate: nothing to Google.
7. Fetch the ICO pages (PECR, Article 9, Children's Code) and Firebase's Data disclosure page.
   Close UNVERIFIED rows in section 2.
8. Session 1 with the owner, one screenshot per screen, stopping at Google's terms.
9. Code per section 4 and tests per section 8, including the sect independence test and the
   disk reading guards proven to fail when broken. `./gradlew clean test lint`, all variants.
10. Phase 4 on an emulator with the flag temporarily on, then the five minute DebugView check
    with the owner, then Phase 5 copy and the repo wide grep.

## 13. Review log
- **v2** (review 1): dropped Consent Mode defaults; removed the deletion promise; reworded
  "anonymous"; three source set overrides; no Firebase calls for users who never opted in; capture
  test, sharing settings, onboarding rewiring, inventory gaps, rollback.
- **v3** (fact check): evidence labels; demoted manual init; stop rule; location and IP wording
  attributed to Google; recorded that nothing was built or read from Google's pages.
- **v4 / v4.1** (owner decisions): sign off recorded; tab screen views; setup events; wording
  and slogan decisions; account decision.
- **v5** (review 2, by a stronger model, plus a rewrite to remove patch damage). **B1:**
  `setup_step=madhab` revealed sect because only Sunni users see that step, so it is banned and a
  test enforces sect independence. **B2:** existing users would have consented through a one
  line switch, so the switch now opens the full consent dialog. **M2:** exact alarm has no result
  callback (logged as a state), location and notification callbacks specified from the code.
  **M3:** `CLAUDE.md` line 134 added to the inventory. **M4:** build variants gate the SDK, not
  the UI, and the phone check is documented as not covering analytics. **M5:** whole device
  capture. **M6:** event tests added. **M7:** legal claims labelled, special category line added
  to consent text, "for adults" removed, Google's terms flagged as the owner's signature.
  **M8:** session type stated, effort estimates raised, API key step cut, plain resource
  configuration added as the preferred spike. Minors: section references, `screen_name`, event
  counts, device info and US processing in the consent text, feature branch rule, restore state,
  consent blind spot, Settings row wording. Still open after v5: every UNVERIFIED row in section 2.
- **v6** (review 3, Fable 5). Confirmed correct: all repo line references except those fixed
  below; main tabs are exactly Times, Qibla, Settings; no screen or setting differs by sect, so
  no further sect leak. **Fixed:** run order (opt in proof moved after Session 1 because it needs
  real values); the assistant cannot see the Firebase console, so Phase 4 is proved from the
  device log and DebugView becomes a five minute owner check; watch version decided (4.7);
  "never touch Firebase" removed because `FirebaseInitProvider` initialises regardless;
  Session 1 screens added (Analytics location, admin paths); where values live in a public
  repo; `setup_step` made once per run and defined; Session 2 as one ordered submission;
  squash merge and when `privacy.html` goes live; Settings row is not a `SwitchRow`; reinstall
  behaviour; consent text wording for exact alarms; extra inventory lines (`privacy.html`
  48 to 49, manifest 39 to 44, HANDOVER §10); release notes drafted; day one checklist.
  Still open: every UNVERIFIED row in section 2.
