# Phase two: optional usage counts (plan, DRAFT v1)

**Status: DRAFT for owner approval. No code has been changed.** Written 3 Oct 2026.
When this plan is approved and built, fold the decisions into `docs/HANDOVER.md` (§2, §5, §8,
§11, §15) and delete or archive this file. Until then it is the single reference.

## 1. Decision being asked for

Add **optional, anonymous, opt in usage counts** to the **phone app only**, using Firebase
Analytics. The owner's goal: know how many people use SajdaTime, how often, for how long, and
which countries they are in. Nothing else.

This **reverses a written project rule** ("no analytics", `CLAUDE.md` line 61, `HANDOVER.md`
§11 non-goals at about line 6222, and four published statements). It is a product decision, so
it needs the owner's explicit sign off (section 12) before any code changes.

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
| Aptabase | Tracks nothing automatically. Every metric needs event code, which breaks "no extra features" |
| Self hosted Umami, Plausible, Matomo | Needs a server we would have to run. Breaks "no server" |
| On by default with a notice | Weakest privacy position, and the UK statistical purposes exemption is narrow and doubtful for a third party SDK |
| Crashlytics | Out of scope. Android vitals in Play Console already covers crashes with no SDK |
| Custom backend | Cost, maintenance and security burden with no benefit over Firebase |

## 2. What the owner will be able to see

| Question | Where | Notes |
|---|---|---|
| How many users | Daily, weekly, monthly active users (Firebase) | **A sample**, not a total. Only opted in users count |
| How often | Sessions per user, returning users | Same sample |
| How long | Average engagement time | Counts foreground time only. See caveat |
| Do they stay | Day 1, 7, 28 retention | Needs about 50 or more opted in users to mean much |
| Where from | Country (and city, derived from the connection) | Not GPS |
| Device, Android version, app version | Standard reports | |
| True total installs and active devices | **Play Console**, not Firebase | The only unsampled number |

**Caveats to state to the owner every time, or the numbers will mislead:**
1. Opt in rates are typically low. Treat Firebase as ratios (retention, sessions per user), and
   Play Console as the headcount.
2. Much real use never opens the app: notifications, the lock screen and the watch. Low
   engagement time does not mean low reliance. Retention is the better signal.
3. With a few dozen users, Google hides rows below its privacy thresholds. Expect sparse
   reports until installs grow.
4. Reports lag by about a day.

## 3. Design

### 3.1 Principle
Collection is **off in three places** until the user says yes, and the user's choice is
re-applied on every launch (a one off `true` call can leave stored state inconsistent).

### 3.2 Manifest (app only)
- `firebase_analytics_collection_enabled` = `false` (meta-data).
- `google_analytics_adid_collection_enabled` = `false`.
- Consent Mode defaults all `false`: analytics storage, ad storage, ad user data, ad
  personalisation. **CHECK exact key spellings on the official page before coding.**
- Remove `com.google.android.gms.permission.AD_ID` with `tools:node="remove"`.
- **Do not** use `firebase_analytics_collection_deactivated`. It is believed to block runtime
  re-enable, which defeats opt in. **CHECK.**
- Update the comment on the `INTERNET` permission (it currently says "solely for the one-off
  city lookup", which becomes false).
- The merged manifest will gain `ACCESS_NETWORK_STATE` and `WAKE_LOCK`. These must appear in
  `privacy.html` and the Play permissions story. `INTERNET` itself is already declared.

### 3.3 Code (smallest diff, follows existing conventions)
Follow the `ongoingBadge` / `disclaimerSeen` pattern in `SettingsRepository.kt`:
1. `AppSettings.analyticsEnabled: Boolean = false` plus `analyticsPromptSeen: Boolean = false`.
2. Two `booleanPreferencesKey` entries, setters, `toAppSettings()` mapping. No migration is
   needed because absent keys read as the default (false).
3. `SajdaViewModel` wrapper functions, threaded through `MainActivity` and `MainScaffold`
   to `SettingsScreen`, as `onSetOngoingBadge` is today.
4. One small file, `data/Analytics.kt`, wrapping the Firebase calls:
   `apply(enabled)` calls `setAnalyticsCollectionEnabled(enabled)`, and on `false` also
   `resetAnalyticsData()`. Called from `MainActivity` once settings have loaded and whenever
   the value changes.
5. No `Application` subclass is needed. Firebase initialises itself; the manifest default
   keeps it silent until `apply(true)`.
6. Update the KDoc "Nothing here is ever transmitted" on `AppSettings` to carve out the
   consent flag.

### 3.4 Build variants (a trap)
`debug`, `rtl` and `sideload` are debug derived, and `rtl` and `sideload` have different
package names. Two consequences:
- **The google-services plugin fails the build** if `google-services.json` has no client for a
  package. Register `com.sajdatime.app.rtl` and `com.sajdatime.app.sideload` as extra Android
  apps in the Firebase project (free, console only).
- **Test and sideload builds must never count as real users.** Use the repo's existing
  source set override pattern: `R.bool.analytics_allowed` is `true` in `main` and `false` in
  `src/debug`, `src/rtl` and `src/sideload`. `Analytics.apply` ignores the request when it is
  `false`, and the Settings switch is hidden.
- Wear shares the phone's package name. Apply the google-services plugin to `:app` only.

### 3.5 Consent UX
- **New installs:** one new onboarding step, after `PERMISSION` and before the later setup
  steps, using the existing `StepScaffold` and `ChoiceCard` helpers. Two equally weighted
  choices, no pre selection, no nagging, no dark patterns.
- **Everyone:** a `SwitchRow` in Settings, About group, next to the Privacy row, default off.
- **Existing users (about 22 installs):** no pop up, no banner. They find the switch in
  Settings. A one time Home card is possible but rejected for now: more surface, and the
  sample is tiny anyway. Revisit when installs grow.
- **The dua rule is not touched.** The consent screen must not appear in or after the
  disclaimer dialog, and must not contain the word "duas". `DisclaimerContentTest` enforces
  the disclaimer shape and will stay green.
- **Explicit exception to record in HANDOVER §11:** the "asked exactly once, nowhere else" rule
  was written to ban rating and share prompts. A privacy consent is a different thing and is
  legally required, but the exception must be written down so a later session does not delete
  the screen.

### 3.6 What is sent when opted in (must match the copy exactly)
Anonymous app instance ID, sessions, engagement time, app version, device model, Android
version, language, and country derived from the connection. **Not sent:** coordinates, city
names the user types, name, email, prayer settings.
**CHECK** whether Google stores the IP address and whether it falls back to the Android ID
when the advertising ID is off (the research found a forum claim that it does). Do not publish
final copy until this is confirmed against Google's pages.

### 3.7 Opting out
Switch off: `setAnalyticsCollectionEnabled(false)` and `resetAnalyticsData()`. This clears
local data and the instance ID. **Data already sent stays on Google's side** until deleted in
the console. The privacy policy must say so, and give the owner's contact route for deletion.

## 4. Owner only steps (walk through one screen at a time; stop before any submit)

1. Create a Firebase project under the developer's Google account. Accept the terms.
2. Add Android apps: `com.sajdatime.app`, `com.sajdatime.app.rtl`, `com.sajdatime.app.sideload`.
3. Download `google-services.json` and send it to the assistant. It contains an API key that is
   an identifier, not a secret, so it is committed. In Google Cloud console, restrict the key
   to the app package and release signing certificate.
4. Analytics settings: data retention **14 months** (the default may be 2 months, which would
   quietly erase cohorts), Google signals **off**, all advertising features **off**.
5. Play Console: update the Data safety form and the Advertising ID answer (section 7), then
   submit the release. **The owner presses submit.**

## 5. Copy and documents that become false (full inventory)

Every item below is currently true and becomes false or misleading. Drafts are in section 6.

| File | Lines (approx.) | Problem |
|---|---|---|
| `app/src/main/res/values/strings.xml` | 348 `about_privacy_desc` | "no analytics" |
| same | 31 `permission_why_body` | "we never track you" (reword) |
| same | 261 `pdf_footer`, 25 `welcome_tagline` | fine, no change |
| `AndroidManifest.xml` | 11 | INTERNET comment |
| `SettingsRepository.kt` | 46 | "never transmitted" KDoc |
| `docs/privacy.html` | 39-40, 53-70, 80-83, 91-92, 96-102, 37 | lede, "one time", analytics, SDKs, sharing, children, permissions, date |
| `docs/index.html` | 39 | "no ads or tracking" |
| `README.md` | 5 | "No analytics. No tracking." |
| `docs/store/LISTING.md` | 21, 170-182, 327, 351, 355, 357, 364, 480 | short description, long description privacy block, Data safety, Advertising ID, feature graphic text |
| `tools/build-store-assets.sh` | 130 | feature graphic subtitle, then **regenerate the PNG** |
| `CLAUDE.md` | 60-61 | founding rule |
| `docs/HANDOVER.md` | 50, 109-138, 1365-1403, 2079-2085, 6222-6236, 6706 | vision, networking, privacy model, audit table, non goals, rating rule |
| `docs/RELEASING.md` | 60, 171, 727, 764 | tester and outreach copy ("no tracking") |
| `docs/NEW-SESSION.md` | 179 | "no tracking" |
| `docs/ARCHITECTURE.md` | 406 | "adds no tracking" (check context) |
| `docs/store/screenshots` | `05-settings.png` only if the new row is visible | retake if so |

Not affected: wear strings, wear manifest, screenshots 01 to 04, all disclaimer text.
Review prompts under `docs/reviews/` are history, leave them.

## 6. Draft copy (UK English, plain language, **not final until section 3.6 is confirmed**)

**Onboarding step**
- Title: *Help us see how SajdaTime is used?*
- Body: *If you say yes, SajdaTime sends anonymous counts to Google Analytics: roughly how many people use it, how often, for how long, and which country they are in. It never sends your location, the place you type, or your prayer settings. It is off unless you choose yes, and you can change your mind in Settings at any time.*
- Buttons (equal weight): **Yes, share anonymous counts** / **No thanks**

**Settings row**
- Title: *Share anonymous usage counts*
- Description: *Off unless you turn it on. Never includes your location.*

**`about_privacy_desc`**
- *Your location stays on this phone. No accounts, no ads. Anonymous usage counts are optional and off unless you turn them on. Tap to read the full policy.*

**`privacy.html` lede**
- *SajdaTime does not collect personal information, has no accounts, shows no advertising, and keeps your location on your phone. If you choose to, it can send anonymous usage counts to Google Analytics. That is off by default.*

**`privacy.html` new section: Optional usage counts**
- What is sent, what is not sent (section 3.6), who receives it (Google, as our analytics provider), why (to see how many people use the app, so it can be maintained), retention (14 months), how to stop (Settings, and what that does and does not delete), how to ask for deletion (contact route), and that it is never sold or used for advertising.

**`privacy.html` fixes:** "The one time something leaves your device" becomes "When something leaves your device" and lists the city lookup and, only if you opt in, the usage counts. Permissions list gains `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, with reasons. `INTERNET` reason is widened. Children section reworded to say nothing is collected unless a parent or guardian opts in, and that no child is targeted. New "last updated" date.

**Play short description (80 character limit)**
- Current ends *No tracking.* (79 of 80). Proposed: *Offline prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.* (79 of 80).
- **Before using it, re-read `LISTING.md` lines 20 to 100.** The Console rejected an earlier wording, and the reasoning is recorded there.

**Play long description privacy block (about 296 characters of headroom)**
- Replace *No analytics, no crash reporting, no tracking of any kind* with *Optional anonymous usage counts, off unless you switch them on. No accounts, no ads.* Check length and the "Free"/"ads" wording rules in `LISTING.md` first.

**Feature graphic:** *Sunni & Shia · No accounts · No ads*, then rerun the asset script.

**Data safety form** (the owner presses submit): declare *Device or other IDs*, *App activity*, *App info and performance* as collected, **optional**, purpose **Analytics**, encrypted in transit, not shared beyond the service provider, deletable on request. Advertising ID answer stays **No** only if the merged manifest check passes.

## 7. Hard rules this plan respects
Location stays on device. No fine location. Disclaimer and dua paragraph untouched. No
machine translation (English strings only; no `values-xx` folder). No aladhan or network
calculation. No signing key handling. No ads, accounts or server of our own. Cloud backup and
device transfer stay off (this also stops the Firebase instance ID restoring onto a new phone).

## 8. Build order

| Step | Who | Output |
|---|---|---|
| 0 | Owner | Approves section 12 |
| 1 | Assistant | Confirm every **CHECK** item against Google's official pages (needs a browser; the research sandbox could not reach them). Update this plan |
| 2 | Owner | Firebase console steps (section 4) |
| 3 | Assistant | Gradle, manifest, `Analytics.kt`, settings, switch, onboarding step, build variant handling |
| 4 | Assistant | Tests (section 9) |
| 5 | Assistant | Docs and copy (sections 5 and 6), HANDOVER and CLAUDE.md rewrite, retake screenshot if needed, regenerate graphic |
| 6 | Assistant | Full gate: `clean test lint :app:bundleRelease :wear:bundleRelease`, both emulators |
| 7 | Owner | Play Console: Data safety, Advertising ID, publish. Assistant guides one screen at a time |
| 8 | Both | Verify live (DebugView, then the first real data), update STATE OF PLAY |

## 9. Verification plan

**Automated (new, fast):**
- Default settings read `analyticsEnabled=false` and `analyticsPromptSeen=false`.
- Guard test (reads manifest from disk; **add the file to the `inputs.files` block in
  `app/build.gradle.kts`** or it goes UP-TO-DATE): collection disabled meta-data present, AD_ID
  removed, ad id collection disabled.
- Copy guard: no new string contains "duas"; `DisclaimerContentTest` still green.
- Build **every variant** (release, debug, rtl, sideload). The google-services package mismatch
  only shows up at build.

**Manual (emulator, then ask for the Redmi because this touches permissions and first run):**
- Fresh install: consent step appears once, Back works, both choices proceed.
- Switch off: nothing in DebugView. Switch on: events appear within minutes in DebugView.
- Toggle on, off, on, kill the app, relaunch: state honoured every time.
- Airplane mode: app behaves identically.
- Emulator image **without Google Play services**: no crash, no delay.
- RTL layout check of the new screen (`installRtl`), reported as a layout check only.
- Merged manifest diff against the current release: only the expected permissions added; the
  watch manifest unchanged.
- Release AAB size before and after.

**Not verifiable here, so it will be stated plainly:** what Google stores server side, and
real world opt in rates. Network capture of the opted out state is only as good as the
emulator test; it cannot prove behaviour on every OEM.

## 10. Rollback
- Pre release: revert the branch. No data leaves a device until release.
- Post release: opt in is off by default, so the blast radius is only those who switched it on.
  Ship a version with the dependency removed. Delete collected data in the Firebase console.
  Revert the copy and Data safety form in the same release.

## 11. Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Trust: "no tracking" is part of the app's identity and the closed testers were told it | High | Opt in, plain wording, honest policy, say it in the release notes |
| Copy contradicts behaviour somewhere | High | Inventory in section 5, plus a repo wide grep for `tracking`, `analytics`, `telemetry` as a final gate |
| Play rejects the Data safety or policy changes | Medium | Declare precisely, owner submits, allow review time |
| Fallback identifier (Android ID) when ad ID is off | Medium | Confirm in step 1; declare it if used |
| Debug or sideload builds pollute the data | Medium | Source set override, section 3.4 |
| Behaviour on devices without Google Play services | Medium | Emulator test, section 9 |
| Sample too small to read | Medium | Say so; use Play Console for totals |
| Retention default quietly 2 months | Medium | Owner step 4 |
| Existing users never see the prompt | Low | Accepted for now |
| Scope creep into events and funnels | Medium | Out of scope for this phase |

## 12. Sign off needed from the owner

1. Approve reversing the "no analytics" rule to "no analytics except optional, anonymous usage
   counts, phone only", and the matching rewrite of `CLAUDE.md` and HANDOVER.
2. Approve the exception to the "asked once" rule for the consent screen.
3. Confirm no pop up for existing users.
4. Confirm the privacy contact route for deletion requests (an email address to publish).
