# Phase two: optional usage counts (plan, DRAFT v2)

**Status: DRAFT v2 for owner approval. No code has been changed.** Written 3 Oct 2026, revised
after an adversarial review the same day (log in section 13).
When this plan is approved and built, fold the decisions into `docs/HANDOVER.md` (§2, §5, §8,
§11, §15) and `CLAUDE.md`, then delete or archive this file.

## 1. Decision being asked for

Add **optional, opt in usage counts** to the **phone app only**, using Firebase Analytics. The
owner's goal: know how many people use SajdaTime, how often, for how long, and which countries
they are in. Nothing else.

This **reverses a written project rule** ("no analytics", `CLAUDE.md` lines 60 to 61,
`HANDOVER.md` §11 non-goals at about line 6222, and several published statements). It is a
product decision, so it needs the owner's explicit sign off (section 12) before any code.

### Decisions already taken (owner, 3 Oct 2026)

| Question | Decision |
|---|---|
| Tool | Firebase Analytics (free, no event cap, gives the metrics with no custom events) |
| Consent model | Opt in, off by default |
| Scope | Phone app only. The watch app is untouched |
| Custom events | None. Automatic metrics only |

### Rejected, with reasons (do not reopen without new information)

| Option | Why not |
|---|---|
| Aptabase | Tracks nothing automatically. Every metric needs event code |
| Self hosted Umami, Plausible, Matomo | Needs a server we would run. Breaks "no server" |
| On by default with a notice | Weakest privacy position. The UK "statistical purposes" exemption is narrow and doubtful for a third party SDK (legal status CHECK) |
| Crashlytics | Out of scope. Android vitals in Play Console covers crashes with no SDK |
| Custom backend | Cost and security burden with no benefit over Firebase |

## 2. What the owner will be able to see

| Question | Where | Notes |
|---|---|---|
| How many users | Daily, weekly, monthly active users (Firebase) | **A sample.** Only opted in users count |
| How often | Sessions per user, returning users | Same sample |
| How long | Average engagement time | Foreground time only |
| Do they stay | Day 1, 7, 28 retention | Needs a reasonable number of opted in users to mean much |
| Where from | Country (city if Google provides it), worked out from the connection | Not GPS |
| Device, Android version, app version | Standard reports | |
| True installs and active devices | **Play Console**, not Firebase | The only unsampled headcount |

**Caveats to repeat every time, or the numbers will mislead:**
1. Opt in rates are low. Use Firebase for ratios (retention, sessions per user) and Play Console
   for the headcount. A Settings switch with no onboarding step would produce close to no data.
2. Much real use never opens the app (notifications, lock screen, watch). Low engagement time
   does not mean low reliance. Retention is the better signal.
3. With a few dozen users Google hides rows below its privacy thresholds (CHECK). Expect sparse
   reports until installs grow.
4. Reports lag (about a day, CHECK).

## 3. Design

### 3.1 Principle
**Firebase does nothing, and ideally does not even exist in memory, until the user opts in.**
Two candidate ways to achieve that. A spike (step 1 in section 8) decides which. Prefer B if it works.

- **A. Standard setup.** `google-services` plugin, `google-services.json`, Firebase
  auto-initialises at process start with collection disabled in the manifest, and
  `setAnalyticsCollectionEnabled(true)` after consent.
- **B. Manual init after consent.** No plugin, no `google-services.json`. Call
  `FirebaseApp.initializeApp(context, FirebaseOptions...)` only after the user opts in, and only
  in release builds. Nothing initialises before consent, which is structural rather than
  configuration. Also removes the extra Firebase apps for `.rtl` and `.sideload`, the AGP 9.3.1
  plugin risk and the pre consent footprint. Cost: option values (project ID, app ID, API key,
  identifiers rather than secrets) live in code. **Unproven** that Analytics behaves correctly
  with manual init, so the spike must show events arriving in DebugView.

Either way, the persisted choice is re-applied on every launch, and Firebase is **not touched
at all** for users who never opted in (no `setAnalyticsCollectionEnabled(false)` and no reset on
launch, because that call can itself initialise the SDK and mint an instance ID).

### 3.2 Manifest (app only)
- Approach A only: `firebase_analytics_collection_enabled` = `false`.
- Both approaches: `google_analytics_adid_collection_enabled` = `false`, and remove
  `com.google.android.gms.permission.AD_ID` with `tools:node="remove"`. This needs
  `xmlns:tools` added to the phone manifest, which it lacks today.
- **No Consent Mode defaults.** The review showed they create a second, independent switch
  (`analytics_storage` denied by default would also need `setConsent(GRANTED)`), so the two
  could disagree. One switch only. CHECK whether Firebase needs `setConsent` anyway.
- **Do not use** `firebase_analytics_collection_deactivated` (believed to block runtime
  re-enable, CHECK).
- Update the comment on `INTERNET` (`AndroidManifest.xml` line 11, "solely for the one-off city
  lookup", becomes false).
- The merged manifest will gain `ACCESS_NETWORK_STATE`, `WAKE_LOCK` and probably the install
  referrer permission and `<queries>` entries (CHECK by diffing). Every addition goes into
  `privacy.html`. `INTERNET` is already declared.

### 3.3 Code (smallest diff, follows `ongoingBadge` / `disclaimerSeen`)
1. `AppSettings.analyticsEnabled: Boolean = false`. **No** "prompt seen" flag: nothing consumes it.
2. Key, setter and `toAppSettings()` mapping in `SettingsRepository.kt`. No migration, absent
   keys read as false.
3. `SajdaViewModel` wrapper, threaded through `MainActivity`, `MainScaffold`, `SettingsScreen`.
4. About 10 to 15 lines in the existing data layer (not a new abstraction): when settings load
   and `analyticsEnabled` is true, enable collection. On a change from true to false, disable
   collection and call `resetAnalyticsData()`. Otherwise do nothing.
5. No `Application` subclass.
6. Fix the KDoc on `AppSettings` ("Nothing here is ever transmitted", about
   `SettingsRepository.kt` line 44) to carve out this flag.

### 3.4 Build variants
`debug`, `rtl` and `sideload` must never count as real users. `debug` shares the release
package name, and `rtl` and `sideload` use `initWith(debug)`, which does **not** inherit the
`debug` source set.
- Create **three** folders: `app/src/debug`, `app/src/rtl` (exists), `app/src/sideload`
  (exists), each overriding `R.bool.analytics_allowed` to `false`. `main` sets it `true`.
- When false, analytics never starts and the Settings switch is hidden.
- A test asserts all three overrides exist and that only release resolves to true.
- Approach A additionally needs `google-services.json` clients for `.rtl` and `.sideload`, or
  the plugin fails the build. Approach B does not.
- Wear shares the phone package name. Nothing Firebase related is added to `:wear`.

### 3.5 Consent UX
- **New installs:** one new onboarding step after `PERMISSION`. Rewire both directions:
  `PERMISSION` next goes to the new step, the new step goes to `SECT`, and `SECT` back goes to
  the new step (`OnboardingScreen.kt`, enum at line 79, wiring around lines 108 to 156). Use
  existing `StepScaffold` and `ChoiceCard`. Two equally weighted choices, nothing pre selected.
  Name Google, and link the privacy policy.
- **Everyone:** a `SwitchRow` in Settings, About group, next to Privacy. Off by default. It is
  also the one tap way to withdraw.
- **Existing users:** no pop up and no banner. They find the switch in Settings.
- **Not bundled with any feature.** Saying no changes nothing about how the app works.
- **Dua rule untouched.** The consent text never appears in or after the disclaimer dialog and
  never contains the word "duas". `DisclaimerContentTest` stays green.
- **Exception to record in HANDOVER §11:** the "asked exactly once, nowhere else" rule was
  written to ban rating and share prompts. A privacy consent is different and required, but it
  must be written down so a later session does not delete the screen.
- **Owner option to cut scope:** ship only the Settings switch. Smaller change, less risk to the
  "asked once" rule, but expect almost no data.

### 3.6 What is sent when opted in, and what the copy may claim
Sent: a persistent pseudonymous app instance ID, sessions, engagement time, app version, device
model, Android version, language, and country (and possibly city) that Google works out from the
internet connection. Data goes to Google, including to the US.
Not sent: GPS coordinates, the place the user types, name, email, prayer settings.

**Wording rules from the review (these override the section 6 drafts if they conflict):**
- Say **"pseudonymous"**, or "no name, email or account". Do **not** say "anonymous" alone. An
  instance ID is personal data under UK GDPR and PECR, and that is why consent is needed.
- Do **not** say "never includes your location". Say "your approximate country, worked out by
  Google from your internet connection. Never your GPS position or the place you type".
- This must match the Data safety declaration: *Approximate location* already exists for app
  functionality, and gains *Analytics* as a second purpose if Google derives location.
- CHECK before final copy: whether Google stores the IP address; whether Analytics falls back
  to the Android ID when the advertising ID is off.

### 3.7 Opting out and deletion
Switch off disables collection and calls `resetAnalyticsData()`, which clears local data and the
instance ID. **Data already sent stays with Google** and ages out under the retention setting.
**No deletion on request promise.** Without the instance ID nobody can identify their own
records, and resetting destroys it. The policy says so plainly. Data safety must not say
"deletable on request". Retention of 14 months applies to event level data. Aggregated reports
last longer, so the policy must not say everything is gone at 14 months.

## 4. Owner only steps (one screen at a time, stop before any submit)

1. Create a Firebase project under the developer's Google account. Accept the terms.
2. Register the Android app `com.sajdatime.app` (approach A: also `.rtl`, `.sideload`).
3. Send the config values or `google-services.json`. These are identifiers, not secrets. In
   Google Cloud console, restrict the API key to the app package and release signing certificate.
4. Analytics settings: event data retention **14 months**, Google signals **off**, advertising
   features **off**, and **all data sharing settings off** (product improvement, benchmarking,
   technical support, account specialist). These feed the Data safety "not shared" answer.
5. Play Console: update Data safety and the Advertising ID answer (section 6), then submit the
   release. **The owner presses submit.**

## 5. Copy and documents that become false (inventory)

| File | Where | Problem |
|---|---|---|
| `app/src/main/res/values/strings.xml` | `about_privacy_desc` (about 348), `permission_why_body` (about 31) | "no analytics"; "never track you" |
| `AndroidManifest.xml` | 11 | INTERNET comment |
| `SettingsRepository.kt` | about 44 | "never transmitted" KDoc |
| `docs/privacy.html` | 37, 39 to 40, 53 to 70 (including "no server ... nowhere for them to go"), 80 to 83, 91 to 92, 96 to 102 | date, lede, "one time", analytics, SDKs, sharing, children, permissions |
| `docs/index.html` | 39 | "no ads or tracking" |
| `README.md` | 5 | "No analytics. No tracking." |
| `docs/store/LISTING.md` | 21, long description privacy block (about 170 to 182, including "no server for them to go to"), 327, 351, 355, 357, 364, 480 | short description, long description, Data safety, Advertising ID, feature graphic text |
| `tools/build-store-assets.sh` | 130 | subtitle, then **regenerate the PNG** |
| `CLAUDE.md` | 60 to 61 | founding rule |
| `docs/HANDOVER.md` | 50, §2 networking and Play Services rows (about 109 to 138), §8 (1365 to 1403), 2079 to 2085, §11 non-goals (about 6222) and the rating rule (6226 to 6236), 6706, plus **STATE OF PLAY** | vision, stack, privacy model, audit table, non-goals |
| `docs/RELEASING.md` | 60, 171, 727, 764, 988 | tester and outreach copy |
| `docs/NEW-SESSION.md` | 179 | "no tracking" |
| `docs/ARCHITECTURE.md` | 406 | check context |
| `docs/store/screenshots` | `05-settings.png` only if the new row is visible | retake if so |

Leave as dated history: `docs/reviews/`, HANDOVER line 6195 and LISTING lines 28, 53, 75, 96.
Unaffected: wear files, `WatchSync.kt` ("not analytics" stays true), screenshots 01 to 04,
all disclaimer text.

## 6. Draft copy (UK English, **not final until the section 3.6 CHECK items are confirmed**)

**Onboarding step**
- Title: *Help us see how SajdaTime is used?*
- Body: *If you say yes, SajdaTime sends usage counts to Google Analytics: how many people use it, how often, for how long, and your approximate country, which Google works out from your internet connection. They are tied to a random ID, not to your name, and the data goes to Google, including in the US. It never sends your GPS position, the place you type or your prayer settings. It is off unless you choose yes. You can switch it off in Settings at any time.*
- Link: *Read the privacy policy*
- Buttons (equal weight): **Yes, share usage counts** / **No thanks**

**Settings row**
- Title: *Share usage counts*
- Description: *Off unless you turn it on. Not your GPS position or the place you type.*

**`about_privacy_desc`**
- *Your location stays on this phone. No accounts, no ads. Usage counts are optional and off unless you turn them on. Tap to read the full policy.*

**`privacy.html` lede**
- *SajdaTime has no accounts, shows no advertising, and keeps your location on your phone. If you choose to, it can send usage counts to Google Analytics. That is off by default.*

**`privacy.html` new section, "Optional usage counts"** covering: what is sent and not sent
(3.6); who receives it (Google, in the US, as analytics provider); why (to see how many people
use the app so it can be maintained); retention (14 months event level, aggregates longer);
how to stop and what that does not delete (3.7); that nothing is sold or used for advertising;
and that the option is for adults.

**Other `privacy.html` fixes:** "The one time something leaves your device" becomes "When
something leaves your device". Permissions gain every merged addition with a reason, and
`INTERNET` is widened. Children section says the app is not directed at children, and the
option is for adults, not "a parent opts in". The "we have no server" lines must say we run no
server and that opted in counts go to Google. New "last updated" date.

**Play short description (80 character limit)**
- Current ends *No tracking.* (79 of 80). Candidate: *Offline prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.* (79 of 80).
- `No accounts` has never been tested alone in the Console. Use the documented method in
  `LISTING.md` lines 20 to 100 (change one phrase, save, reload, re-read) before relying on it.

**Play long description privacy block (about 296 characters of headroom)**
- Replace *No analytics, no crash reporting, no tracking of any kind* with *Optional usage counts, off unless you switch them on. No accounts. No crash reporting.* Check length and the wording rules in `LISTING.md`.

**Feature graphic:** `LISTING.md` lines 470 to 481 say "No ads" was deliberately removed from
the banner as a price or promotion word. **Do not reintroduce it.** Use *Sunni & Shia · No
accounts*, then rerun the asset script.

**Data safety form (owner submits):** *Device or other IDs*, *App activity*, *App info and
performance* collected, optional, purpose Analytics, encrypted in transit. *Approximate
location* gains Analytics as a purpose if Google derives it. "Shared" answer depends on the
step 4 sharing settings (CHECK Google's disclosure page). **No "deletable on request".**
Advertising ID answer stays No only if the merged manifest check passes.

## 7. Hard rules this plan respects
Location stays on device. No fine location. Disclaimer and dua untouched. No machine
translation (English strings only, no `values-xx` folder). No Aladhan or network calculation.
No signing key handling. No ads, accounts or server of our own. Cloud backup and device
transfer stay off, so the Firebase instance ID never restores onto a new phone.

## 8. Build order

| Step | Who | Output |
|---|---|---|
| 0 | Owner | Approves section 12 |
| 1 | Assistant | **Spike on a throwaway branch.** Compare approach A and B: builds on every variant, events reach DebugView, nothing sent before opt in. Confirm every CHECK on Google's official pages (needs a browser; the research sandbox could not reach them). Update this plan |
| 2 | Owner | Firebase console steps (section 4) |
| 3 | Assistant | Gradle, manifest, settings, switch, onboarding step, variant overrides |
| 4 | Assistant | Tests (section 9) |
| 5 | Assistant | Docs and copy (sections 5 and 6), HANDOVER, CLAUDE.md, STATE OF PLAY, screenshot, graphic |
| 6 | Assistant | Full gate and both emulators |
| 7 | Owner | Play Console: Data safety, Advertising ID, publish. Walked through one screen at a time |
| 8 | Both | Verify live, update STATE OF PLAY |

## 9. Verification plan

**Automated:**
- Default `analyticsEnabled=false`.
- Guard test reading the manifest from disk (AD_ID removed; approach A: collection disabled).
  Add `AndroidManifest.xml` and any new `bools.xml` to the `inputs.files` block in
  `app/build.gradle.kts`, **and prove the guard fails when the manifest is broken** (HANDOVER
  §15 lesson 84).
- `analytics_allowed` overrides exist for debug, rtl and sideload; only release is true.
- No new string contains "duas". `DisclaimerContentTest` and `NoTranslationsYetTest` stay green.
- Onboarding step order and Back behaviour, both directions.
- Build every variant (release, debug, rtl, sideload).

**Manual (emulator, then ask for the Redmi because this touches permissions and first run):**
- **Opted out proves silence:** fresh install, `adb shell setprop log.tag.FA VERBOSE`, watch
  logcat and capture traffic across cold start, a boot broadcast, an alarm receiver start and a
  WorkManager run. Nothing may reach Google.
- Opt in: events appear in DebugView. Check whether a session that began before opting in is
  recorded or backfilled.
- On, off, on, kill the app, relaunch: state honoured every time.
- Airplane mode: behaves identically.
- Emulator image without Google Play services: no crash, no delay.
- RTL layout check of the new screen, reported as a layout check only.
- Merged manifest diff against the current release. Watch manifest unchanged.
- AAB size before and after.

**Not verifiable here, so it will be stated plainly:** what Google stores server side, real
world opt in rates, and behaviour on every OEM.

## 10. Rollback
- Before release: revert the branch. Nothing leaves a device.
- After release: opt in is off by default, so only people who switched it on are affected. Ship
  a version without the dependency. Delete collected data in the Firebase console (this does
  not remove anything already exported elsewhere). **Revert the Data safety form and policy
  with their own Console submission and a new policy date**, because removing the code does
  not undo them.

## 11. Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Trust: "no tracking" is part of the app's identity and closed testers were told it | High | Opt in, plain wording, honest policy, say it in release notes |
| Copy contradicts behaviour somewhere | High | Inventory plus a final repo grep for `tracking`, `analytics`, `telemetry`, excluding this plan and `docs/reviews/` |
| Overclaiming ("anonymous", "no location") | High | Wording rules in 3.6 |
| Play rejects Data safety or policy changes | Medium | Declare precisely, allow review time |
| Pre consent leakage | Medium | Approach B if proven, plus opted out capture test |
| Android ID fallback when ad ID is off | Medium | Confirm in step 1, declare if used |
| Debug or sideload builds pollute data | Medium | Three source set overrides plus test |
| Devices without Google Play services | Medium | Emulator test |
| Sample too small or biased | Medium | Say so. Use Play Console for totals |
| Retention default may be 2 months | Medium | Owner step 4 |
| Existing users never see a prompt | Low | Accepted for now |
| Scope creep into events and funnels | Medium | Out of scope for this phase |

## 12. Sign off needed from the owner

1. Approve changing the rule from "no analytics" to "no analytics except optional usage counts,
   phone only", with the matching rewrite of `CLAUDE.md` and HANDOVER.
2. Approve the exception to the "asked once" rule for the consent screen.
3. Confirm no pop up for existing users.
4. Choose: onboarding step plus Settings switch (recommended, produces data), or Settings
   switch only (smaller, near zero data).
5. Accept that there is **no deletion on request** promise, only a clear explanation of why.

## 13. Review log

Hostile review, 3 Oct 2026. Found and fixed in v2: Consent Mode defaults created a second
conflicting switch (dropped); a deletion promise that could not be kept (removed); "anonymous"
and "never your location" overclaimed (reworded, 3.6); `src/debug` does not exist and
`initWith(debug)` does not inherit it (three folders); `resetAnalyticsData()` on every launch
would initialise the SDK (only on true to false); unused `analyticsPromptSeen` (cut); no proof
of silence before consent (capture test, approach B); data sharing settings missing from owner
steps (added); onboarding rewiring under specified (added); "No ads" must not return to the
feature graphic (fixed); inventory gaps (RELEASING 988, HANDOVER §2 Play Services row, STATE OF
PLAY, "no server" lines); rollback incomplete (Data safety resubmission). Still open: all CHECK
items, which need a browser on Google's own pages.
