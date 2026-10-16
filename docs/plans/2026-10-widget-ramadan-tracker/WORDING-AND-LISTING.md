# Wording and listing: the audit, every new string, and the store text

Written 16 October 2026 against `main` at `13b581d`. Section A audits what exists. Section B gives every
new user facing string for the three features in English with Urdu, Turkish and Indonesian drafts.
Section C gives the store listing changes, the What's new text and the privacy policy additions for
each release. Nothing in the audited files was changed by the planning session: section A lists the
changes for the implementer to make in tasks A10 and B7.

**Translation status, stated once for every draft in this file:** the Urdu, Turkish and Indonesian text
was written by Claude and checked by Claude with no native speaker review, which is the owner's standing
decision of 9 October 2026 (`CLAUDE.md`, "Translations"). The app never says a translation is awaiting
review. Religious terms (suhoor, iftar, imsak, the prayer names) were chosen to match what each
language's existing app strings and timetables use, and the reasoning for each is in
`RAMADAN-LOCATION-SPEC.md` section 5. Correct any mistake a user reports.

## A. Claims audit

Status key: **correct** (matches the code), **wrong** (contradicts the code), **stale** (was true, is not
now), **thin** (true but under describes), **unverifiable** (cannot be checked from the repo). Evidence
cites the code read on `13b581d`. The live Play page was fetched as raw HTML on 16 October 2026 (the
summarising fetch served a copy from August, exactly as `docs/HANDOVER.md` warns, so the raw page is the
evidence): "Updated on 11 Oct 2026", the short description ending "No accounts.", the full description
byte identical to the fenced block in `docs/store/LISTING.md` (3,770 characters, sha256
`ff4dccf3…0baadf` on both sides), the 1.3.2 What's new, and no occurrence of "No tracking".

### A1. `README.md`

| # | Claim | Line | Evidence | Status | Corrected wording |
|---|---|---|---|---|---|
| R1 | "English only for now, and it stays readable on a phone set to any other language" | 23-25 | `values-ur`, `values-tr`, `values-in` exist with every string (F32 in `PLAN.md`), the picker exists (`AppLanguage.kt`), 1.3.1 and 1.3.2 shipped them | **wrong** | "In English, Urdu, Turkish and Indonesian, chosen in Settings on Android 13 and later. The translations were written with AI help by the developer and are corrected when users report mistakes. Clock times, dates and numbers follow the language the app is showing, not the phone's: see `docs/HANDOVER.md` §5.11 for why that is not the same thing." |
| R2 | "Install onto a connected phone or emulator: `./gradlew :app:installDebug`" | 40-44 | `CLAUDE.md` forbids `installDebug` on the owner's phone (it cannot sit beside the Play build) and names `installSideload`; `app/build.gradle.kts:105-128` | **thin, risky** | "Install onto an emulator: `ANDROID_SERIAL=emulator-5554 ./gradlew :app:installDebug`. On a phone that already has SajdaTime from Google Play use `./gradlew :app:installSideload`, which installs beside it as a separate app (see `CLAUDE.md`)." |
| R3 | "The only permission the app needs is approximate location, read while the app is open." | 122 | The manifest declares six permissions (`AndroidManifest.xml:10-55`) and `docs/privacy.html:173-194` lists them | **thin** | "The only sensitive permission is approximate location, read while the app is open. The full list, with what each one is for, is in the privacy policy." |
| R4 | "No accounts. No ads. Your location never leaves your phone. Optional usage counts, off unless you switch them on" | 5 | `UsageCounts.kt`, manifest meta data, `LocationRepository.kt` | correct | |
| R5 | "Supports Sunni (all four madhabs) and Shia (Jafari / Ithna Ashari)" | 12 | `PrayerModels.kt`, `PrayerEngine.kt:35-38` | correct | |
| R6 | "Qibla compass, corrected to true north, verified against a reference implementation" | 13-14 | `QiblaEngine`, `QiblaEngineTest` ten cities | correct | |
| R7 | "Local notifications at each prayer time, working even when the app is closed" | 16 | `PrayerAlarmScheduler`, receivers | correct | |
| R8 | "Optional alarm mode with a sound you choose, able to sound through Do Not Disturb" | 17 | `Notifications.kt:107-152` | correct | |
| R9 | "PDF timetable for today, the next 7 days, or the whole month, saved to Downloads" | 19 | `PrayerPdfExporter.kt:47, 85-110`; below API 29 a share sheet instead | thin | "…saved to Downloads (on Android 10 and later; older phones get a share sheet)" |
| R10 | "A Wear OS app and tile that work on their own" | 20 | `:wear` standalone | correct | |
| R11 | "Light and dark themes … verified against WCAG 2.1 AA contrast" | 21-22 | `ColorContrastTest` | correct | |
| R12 | "Cloud backup and device-to-device transfer are both switched off" | 128-129 | manifest `allowBackup="false"`, `data_extraction_rules.xml` | correct | |
| R13 | "your settings are sent to it over Google's local Data Layer" | 131-133 | `WatchSync.publish`, `WatchSyncContract` | correct | |
| R14 | Disclaimer paragraph | 140-146 | `DisclaimerContentTest` guards it | correct | |
| R15 | Feature list has no widget, no Ramadan pack, no tracker | 9-25 | they do not exist yet | correct today, add after each release | Section C5 |

### A2. `AGENTS.md`

| # | Claim | Line | Evidence | Status | Corrected wording |
|---|---|---|---|---|---|
| G1 | "never machine-translate the app" | 12 | Superseded on 9 Oct 2026, `CLAUDE.md:126-136`, translations shipped | **stale** | "The religious disclaimer and its dua request stay exactly where they are. Translations are written and checked by Claude with no native speaker review, by the owner's decision of 9 Oct 2026; never tell users a translation awaits review." |

### A3. `docs/store/LISTING.md`

| # | Claim | Line | Evidence | Status | Corrected wording |
|---|---|---|---|---|---|
| L1 | "The live listing still says `No tracking` until that release is published; leave it until then" | 26-32 | Raw public page 16 Oct 2026: short description ends "No accounts." | **stale** | Replace the note with: "Live since 1.3.0 (4 Oct 2026); confirmed on the public page 16 Oct 2026." |
| L2 | "Changed 9 Oct 2026, to be applied in the Console (not yet live) … The live listing (3,727 of 4,000 characters) still has the old wording" | 144 | Raw public page carries "chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia)"; `docs/HANDOVER.md` STATE OF PLAY says approved 13 Oct | **stale** | "Live since 13 Oct 2026, approved with 1.3.2." |
| L3 | "`3,704 / 4000`" | 209 | The fenced block is 3,770 characters and matches the live text byte for byte | **wrong** | "`3,770 / 4000`, measured 16 Oct 2026 by sha256 against the live page" |
| L4 | "Last run: `len=3704 hash=441829862`, both sides, 1 Sept 2026" | 238 | Superseded by the 9 Oct wording change | stale | Record the new length and the sha256 from the next Console check |
| L5 | "During Ramadan it doubles as a sehri and iftar timetable: Fajr ends sehri, Maghrib opens your fast" | 164 | True only in the sense that Fajr and Maghrib rows exist. No Ramadan feature exists | thin, correct | Replaced by the Release A line in C1 |
| L6 | "The Umm al-Qura Ramadan adjustment is applied automatically during Ramadan" | 179 | Applies only when the method is Umm al-Qura (`PrayerEngine.kt:419`) | thin | "When you use the Umm al-Qura method, its Ramadan Isha adjustment is applied automatically" |
| L7 | No mention of Urdu, Turkish or Indonesian anywhere in the listing | 147-207 | Three languages shipped in 1.3.1 | **gap** | Add the languages line in C1 |
| L8 | "checked against independent reference timetables before each release" | 199 | The golden values from Aladhan are pinned in the unit tests that run in the release gate (`PrayerEngineTest`, `PolarAndHemisphereTest`) | correct | |
| L9 | "Approximate location only, read while the app is open, never in the background" | 189 | `LocationRepository.kt`, no background permission | correct | |
| L10 | "Cloud backup is deliberately switched off so nothing can be copied off your phone" | 191 | manifest and rules | correct | |
| L11 | "Optional usage counts that you control in Settings. No crash reporting" | 188 | no Crashlytics dependency (`libs.versions.toml`) | correct | |
| L12 | Data safety table "Can users request deletion? No" | 364 | privacy.html says the same | correct | |
| L13 | "Three tags … `Clock, alarm & timer` · `Lifestyle` · `Maps & navigation`" | 260 | unverifiable from the repo | unverifiable | leave |
| L14 | Screenshot `03` "should be … the Ramadan sehri/iftar framing" | 484-491 | | correct, actionable in A10 | retake `03` as the Ramadan Times screen |

### A4. `docs/store/LISTING-LOCALES.md`

| # | Claim | Line | Evidence | Status | Corrected wording |
|---|---|---|---|---|---|
| X1 | "The app itself stays in English for now … In-app translation is item 12 in HANDOVER.md §11; the 129 strings are already externalised" | 12-17 | Three languages shipped, 219 app strings now | **stale** | Delete the block. Replace with: "The app is in English, Urdu, Turkish and Indonesian since 1.3.1. The five other listings below are store text only, for discoverability." |
| X2 | "Have a native speaker skim these before you publish … so the native speaker check below still applies" | 19-23 | Owner's decision 9 Oct 2026, `docs/HANDOVER.md` STATE OF PLAY: "no native speaker check needed" | **stale, contradicts the decision** | "Written by Claude with no native speaker review, by the owner's decision of 9 Oct 2026. Correct any mistake a user reports." |
| X3 | Indonesian, Urdu and Turkish full descriptions lack the Hijri date bullet and the Ramadan bullet the English has | 70-80 (id), 243-252 (ur), 496-505 (tr) | English block lines 157 and 164 | **drift** | Add the two bullets (C2) |
| X4 | The "HONEST ABOUT WHAT IT IS" sections in all eight languages carry the pre 2 Aug 2026 wording: no "not supplied by any mosque, scholar or authority", no "follow your mosque", no "no warranty" | 113-120, 285-293, 538-546 and the five others | `docs/HANDOVER.md` 5.15 requires the Play listing to carry all four points; `DisclaimerContentTest` guards only the English `LISTING.md` | **drift, must fix before any of these is published** | Replace with the translations in C2 |
| X5 | Turkish uses semicolons twice ("Yaptığı tek şey budur; reklamsız", "yeterli; uygulama") | 489, 534 | House style: no semicolons | style | Full stops |
| X6 | Turkish "makul imsak ve yatsı vakitleri" calls Fajr "imsak" while the app's Turkish calls it "Sabah" | 517 | `core/src/main/res/values-tr/strings.xml:7` | thin | "makul sabah ve yatsı vakitleri" |
| X7 | "Do not use Play's paid machine-translation offer. These are already written." | 740 | | correct | |

### A5. `docs/privacy.html`

| # | Claim | Line | Evidence | Status |
|---|---|---|---|---|
| P1 | "keeps your location on your phone … never includes your location, your sect, your prayer calculation setting" | 40-43 | `UsageCountsTest`, `LocationRepository` | correct |
| P2 | "It reads your location only while the app is open, never in the background" | 48-49 | coarse only, no background permission, `LocationRepository.currentLocation` called from the ViewModel on resume and on request | correct, and the widgets must keep it true (they read no location, `PLAN.md` A6) |
| P3 | "Your coordinates are saved on your own device … never sent to us or to anyone else" | 50-52 | DataStore, no transmission path | correct |
| P4 | City lookup paragraph | 61-78 | `CityLookup.kt` | correct |
| P5 | Usage counts paragraphs | 80-139 | `UsageCountsTest` reads this file and checks agreement with the consent text | correct |
| P6 | "your settings and location are sent to [the watch] over Google's local Data Layer" | 147-149 | `WatchSync.publish` sends coordinates and city | correct |
| P7 | "No backup and no device transfer" | 160-163 | manifest and rules | correct |
| P8 | Permissions in full, six app permissions plus library ones, "On the watch, WAKE_LOCK" | 173-194 | `app/src/main/AndroidManifest.xml:10-55`, `wear/src/main/AndroidManifest.xml:7-8` | correct |
| P9 | Disclaimer section | 222-244 | `DisclaimerContentTest` | correct |
| P10 | Nothing about a prayer tracker | | not built | correct today, C4 adds the section in Release B |

### A6. In app strings that describe features, privacy or behaviour (`app/src/main/res/values/strings.xml`)

| # | String | Line | Evidence | Status |
|---|---|---|---|---|
| S1 | `permission_why_body` "We only check your approximate location while the app is open. It stays on your phone. We never send it anywhere." | 31 | as P2, P3 | **changed 16 Oct 2026 (HANDOVER 5.21): the old sentence was untrue because of the geocoder** |
| S2 | `city_fallback_body` "just the name you typed is sent once to a free lookup service" | 41 | `CityLookup.fromOpenMeteo` | **changed 16 Oct 2026 (HANDOVER 5.21): now says the phone's lookup may use the network** |
| S3 | `location_sheet_body` "ten miles is about a minute" | 219 | measured in HANDOVER §10 | correct |
| S4 | `export_month_desc` "Every day from the 1st to the end of the month" | 234 | `withDayOfMonth(1)`, `lengthOfMonth()` | correct |
| S5 | `export_saved` "Saved to Downloads as %1$s" | 235 | API 29 and later only, the share sheet path shows no toast of its own | correct in context |
| S6 | `pdf_footer` "Calculated offline on your device" | 261 | engine is local | correct |
| S7 | `settings_exact_alarms_desc`, `confirm_exact_alarm_body` "hold prayer alerts back by an hour or more" | 339, 341 | measured, HANDOVER §6 | correct |
| S8 | `about_privacy_desc` "Your location stays on this phone. No accounts, no ads. Usage counts are optional and off unless you turn them on." | 348 | | **changed 16 Oct 2026 (HANDOVER 5.21)** |
| S9 | `consent_body`, `consent_sent_list`, `consent_never_list` | 354-366 | `UsageCountsTest` checks them against the policy | correct. B adds "Your prayer tracker" to the never list (C5) |
| S10 | `about_data_desc` adhan-java MIT, Open-Meteo CC BY 4.0 | 384 | | correct |
| S11 | `settings_hijri_offset_help` "This also moves Ramadan and Eid" | 437 | `PrayerEngine.kt:425-436` | correct, and A3's note says the shift also moves the suhoor reminder |
| S12 | `settings_alarm_respect_silent_desc` | 322 | `Notifications.kt:206-211` | correct |
| S13 | `method_umm_al_qura_desc` (core) "Isha is set at a fixed time after Maghrib" | core 84 | 90 or 120 minutes | correct |
| S14 | `polar_notice_body` "worked out from latitude %1$d°" | 194 | `approximatedFrom` | correct |
| S15 | `settings_language_desc` "Choose the language of the app" and `language_unavailable` | 356-357 | `AppLanguage` | correct |

### A7. Also found, outside the audited set (for the implementer to fix in passing)

- `app/src/main/AndroidManifest.xml:33-39` comment: "falls back to `setAndAllowWhileIdle`, which fires within a few minutes" is stale (three rungs, a measured one hour window). Code comment only.
- `docs/HANDOVER.md` §5.11 "The app ships in English only, deliberately", §9 table (no `LanguagePickerTest` or `DndAccessTest`, `NoTranslationsYetTest` no longer exists, count 145), §11 "Not done" item 11 "Localisation is ready … none exist yet", §3 `AppSettings` block missing `analyticsEnabled` and `analyticsAnswered`: stale internal text, rewrite in A10.
- `docs/RELEASING.md:1103` "the phone is on `4`": stale.
- `docs/index.html:42-47` feature list has no languages line and no Hijri date line.

## B. Every new user facing string

Keys are fixed (`PLAN.md` section 0). Each block below is ready to paste into the matching `strings.xml`
under a comment heading of the same name, in this order. Placeholders are numbered. No em dashes, no
semicolons. Content descriptions are the `_a11y` keys. Release A is B1 to B3, Release B is B4. The
four language blocks carry the same keys in the same order, which `LocaleDisciplineTest` and lint
`MissingTranslation` enforce.

### B1. Ramadan pack, English (`app/src/main/res/values/strings.xml`)

```xml
    <!--
      Ramadan. Read docs/plans/2026-10-widget-ramadan-tracker/RAMADAN-LOCATION-SPEC.md sections 2 and 5
      before editing. The app reports Fajr and Maghrib as calculated and calls them the end of suhoor and
      iftar. It never adds an imsak margin, never says "stop eating", "valid", "must" or "may" in the
      ruling sense, and ends every explanation with "follow your mosque". The eve notice names what
      mosques do (announce) and what the app does (a calendar). HANDOVER A11.
    -->
    <string name="ramadan_card_label">Ramadan</string>
    <string name="ramadan_day_of">Day %1$d of %2$d</string>
    <string name="ramadan_suhoor_ends">Suhoor ends</string>
    <string name="ramadan_iftar">Iftar</string>
    <string name="ramadan_countdown_suhoor">Suhoor ends in %1$s</string>
    <string name="ramadan_countdown_iftar">Iftar in %1$s</string>
    <string name="ramadan_next_suhoor_tomorrow">Suhoor ends tomorrow at %1$s</string>
    <string name="ramadan_month_ends_today">On this calendar today is the last day of Ramadan. Your mosque will announce Eid.</string>
    <string name="ramadan_fast_length">Fast today %1$s</string>
    <plurals name="ramadan_fast_change_longer">
        <item quantity="one">%d minute longer than yesterday</item>
        <item quantity="other">%d minutes longer than yesterday</item>
    </plurals>
    <plurals name="ramadan_fast_change_shorter">
        <item quantity="one">%d minute shorter than yesterday</item>
        <item quantity="other">%d minutes shorter than yesterday</item>
    </plurals>
    <string name="ramadan_fast_change_same">the same as yesterday</string>
    <!-- Joins the fast length and the change: "Fast today 12h 18m, 3 minutes longer than yesterday". -->
    <string name="ramadan_fast_line">%1$s, %2$s</string>
    <string name="ramadan_tomorrow_line">Tomorrow: suhoor ends %1$s, iftar %2$s</string>
    <string name="ramadan_open_timetable">Full timetable</string>
    <string name="ramadan_info_a11y">About these Ramadan times</string>
    <string name="ramadan_info_title">About these Ramadan times</string>
    <string name="ramadan_info_calc">SajdaTime shows Fajr and Maghrib as calculated for your location and uses them as the end of suhoor and the time of iftar.</string>
    <string name="ramadan_info_margin">Many timetables print an imsak time a few minutes before Fajr as a precaution, and some mosques begin Fajr earlier than this calculation. SajdaTime does not add a margin.</string>
    <string name="ramadan_info_mosque">Where your mosque\'s timetable differs, follow your mosque.</string>
    <string name="ramadan_info_date">The Islamic date here follows the Umm al-Qura calendar. If your mosque began Ramadan on a different day, shift the Islamic date in Settings. That also moves the day count, the suhoor reminder and the Umm al-Qura Isha rule.</string>
    <!-- %1$s is a date such as "Mon 8 Feb" from TimeFormat.date. -->
    <string name="ramadan_eve_title_before">Ramadan on this calendar begins %1$s</string>
    <string name="ramadan_eve_title_after">Ramadan on this calendar began %1$s</string>
    <string name="ramadan_eve_body">Many mosques announce their own start after sighting the moon, often a day later. If yours differs, tap to shift the Islamic date. That also moves the suhoor reminder and the Umm al-Qura Isha rule.</string>
    <string name="ramadan_timetable_title">Ramadan %1$d</string>
    <string name="ramadan_col_day">Day</string>
    <string name="ramadan_col_fast">Fast</string>
    <!-- After a time on a projected polar day, in the timetable. The PDF uses pdf_approximate_note. -->
    <string name="ramadan_approx_marker">approx.</string>
    <string name="export_ramadan">Ramadan %1$d</string>
    <string name="export_ramadan_desc">Every day of the month, with suhoor and iftar</string>
    <string name="pdf_period_ramadan">Ramadan %1$d</string>
    <string name="pdf_column_ramadan_day">Day</string>
    <string name="pdf_ramadan_note">Suhoor ends at Fajr. Iftar at Maghrib. Follow your mosque where its timetable differs.</string>
```

### B2. Suhoor reminder, English

```xml
    <!-- Suhoor reminder. Off by default. Ramadan days only, on the calendar the user has set. -->
    <string name="settings_suhoor">Suhoor reminder</string>
    <string name="settings_suhoor_help">A reminder before Fajr on the days of Ramadan, on the calendar SajdaTime is set to. It uses the same sound and silent mode rules as your prayer alerts.</string>
    <string name="suhoor_off">Off</string>
    <plurals name="suhoor_minutes_before">
        <item quantity="one">%d minute before Fajr</item>
        <item quantity="other">%d minutes before Fajr</item>
    </plurals>
    <!-- %1$s is the Fajr clock time. -->
    <string name="notif_suhoor_title">Suhoor ends at %1$s</string>
    <!-- %1$s is the prayer name (Fajr), %2$s a duration such as "30m". -->
    <string name="notif_suhoor_body">%1$s begins in %2$s</string>
```

### B3. Widgets, English

```xml
    <!--
      Home screen widgets. Never put android:text in the widget layouts: RemoteViews are inflated in
      the launcher with the phone's language, so every word is set from code through AppLocale.wrap.
      The widgets follow the phone's dark mode, not the app's Theme row, and the descriptions say so.
      No place name on a widget: the home screen is visible to other people.
    -->
    <string name="widget_next_label">SajdaTime: next prayer</string>
    <string name="widget_today_label">SajdaTime: today\'s prayers</string>
    <string name="widget_next_desc">The next prayer and its time, with a countdown. Follows your phone\'s dark mode.</string>
    <string name="widget_today_desc">All six times for today with the next one marked. Follows your phone\'s dark mode.</string>
    <string name="widget_next_prayer">Next prayer</string>
    <string name="widget_tomorrow">Tomorrow</string>
    <string name="widget_next_line">Next: %1$s at %2$s</string>
    <string name="widget_next_line_tomorrow">Tomorrow: %1$s at %2$s</string>
    <string name="widget_no_location">Open SajdaTime to set your location</string>
    <!-- Spoken once for the whole widget. %1$s prayer, %2$s clock time, %3$s remaining such as "1h 20m". -->
    <string name="widget_a11y_next">Next prayer %1$s at %2$s, in %3$s</string>
    <string name="widget_a11y_next_tomorrow">Next prayer tomorrow, %1$s at %2$s, in %3$s</string>
    <!-- %1$s is the six rows joined with ". " -->
    <string name="widget_a11y_today">Today\'s prayer times. %1$s</string>
    <string name="widget_a11y_row">%1$s %2$s</string>
    <string name="widget_suhoor_ends">Suhoor ends</string>
    <string name="widget_iftar">Iftar</string>
```

### B4. Prayer tracker, English (Release B)

```xml
    <!--
      Prayer tracker. Off by default. Stays on the phone. Wording never judges: no "missed", no
      "sin", no "reward", no "lazy". An excused day stores no reason and no screen asks for one.
      TrackerWordingTest enforces the forbidden words. See docs/privacy.html#tracker.
    -->
    <string name="settings_group_tracker">PRAYER TRACKER</string>
    <string name="settings_tracker">Prayer tracker</string>
    <string name="settings_tracker_desc">Tick off each prayer on the Times screen. Everything stays on this phone and is removed if you uninstall the app.</string>
    <string name="settings_tracker_streak">Show streak</string>
    <string name="settings_tracker_streak_desc">Count the days in a row with all five ticked</string>
    <string name="settings_tracker_save">Save tracker to a file</string>
    <string name="settings_tracker_save_desc">A small file you choose where to keep, for moving to another phone</string>
    <string name="settings_tracker_load">Load tracker from a file</string>
    <string name="settings_tracker_load_desc">Adds the ticks from a saved file. Nothing already ticked is removed</string>
    <string name="settings_tracker_delete">Delete tracker data</string>
    <string name="settings_tracker_delete_desc">Removes every tick and excused day from this phone</string>
    <string name="tracker_delete_title">Delete tracker data?</string>
    <string name="tracker_delete_body">Every tick and excused day on this phone will be removed. A file you saved earlier is not affected.</string>
    <string name="action_delete">Delete</string>
    <!-- Spoken for a tick target: "%1$s, %2$s" is the prayer name and the state word below. -->
    <string name="tracker_tick_a11y">%1$s, %2$s</string>
    <string name="tracker_state_ticked">ticked</string>
    <string name="tracker_state_not_ticked">not ticked</string>
    <string name="tracker_state_excused">excused</string>
    <string name="tracker_state_not_due">not yet due</string>
    <plurals name="tracker_streak_days">
        <item quantity="one">%d day in a row</item>
        <item quantity="other">%d days in a row</item>
    </plurals>
    <string name="tracker_open_history">Last 30 days</string>
    <string name="tracker_history_title">Last 30 days</string>
    <string name="tracker_excused">Excused</string>
    <string name="tracker_excused_a11y">Excused day. No reason is recorded.</string>
    <string name="tracker_newer_version">This tracker data was saved by a newer version of SajdaTime. Update the app to change it.</string>
    <string name="tracker_saved">Saved as %1$s</string>
    <plurals name="tracker_loaded">
        <item quantity="one">%d day added or updated</item>
        <item quantity="other">%d days added or updated</item>
    </plurals>
    <string name="tracker_load_failed_format">That file is not a SajdaTime tracker file.</string>
    <string name="tracker_load_failed_newer">That file was saved by a newer version of SajdaTime. Update the app first.</string>
    <string name="tracker_save_failed">Could not save the file. Please try again.</string>
```

### B5. Urdu drafts (`app/src/main/res/values-ur/strings.xml`), not natively reviewed

Terms: سحری (suhoor), افطار (iftar), امساک only inside the sentence about other timetables, چھوٹ for an
excused day (a neutral "exemption", chosen over معذور which also means disabled), نشان for a tick.

```xml
    <!-- رمضان -->
    <string name="ramadan_card_label">رمضان</string>
    <string name="ramadan_day_of">%2$d میں سے %1$d واں دن</string>
    <string name="ramadan_suhoor_ends">سحری ختم</string>
    <string name="ramadan_iftar">افطار</string>
    <string name="ramadan_countdown_suhoor">سحری ختم ہونے میں %1$s</string>
    <string name="ramadan_countdown_iftar">افطار میں %1$s</string>
    <string name="ramadan_next_suhoor_tomorrow">کل سحری %1$s پر ختم ہوگی</string>
    <string name="ramadan_month_ends_today">اس کیلنڈر کے مطابق آج رمضان کا آخری دن ہے۔ عید کا اعلان آپ کی مسجد کرے گی۔</string>
    <string name="ramadan_fast_length">آج کا روزہ %1$s</string>
    <plurals name="ramadan_fast_change_longer">
        <item quantity="one">کل سے %d منٹ لمبا</item>
        <item quantity="other">کل سے %d منٹ لمبا</item>
    </plurals>
    <plurals name="ramadan_fast_change_shorter">
        <item quantity="one">کل سے %d منٹ چھوٹا</item>
        <item quantity="other">کل سے %d منٹ چھوٹا</item>
    </plurals>
    <string name="ramadan_fast_change_same">کل جتنا ہی</string>
    <string name="ramadan_fast_line">%1$s، %2$s</string>
    <string name="ramadan_tomorrow_line">کل: سحری ختم %1$s، افطار %2$s</string>
    <string name="ramadan_open_timetable">پورا نقشہ</string>
    <string name="ramadan_info_a11y">رمضان کے ان اوقات کے بارے میں</string>
    <string name="ramadan_info_title">رمضان کے ان اوقات کے بارے میں</string>
    <string name="ramadan_info_calc">SajdaTime آپ کے مقام کے لیے فجر اور مغرب کو حساب کے مطابق دکھاتا ہے اور انہی کو سحری کے ختم ہونے اور افطار کا وقت مانتا ہے۔</string>
    <string name="ramadan_info_margin">بہت سے نقشوں میں احتیاطاً فجر سے چند منٹ پہلے امساک کا وقت لکھا ہوتا ہے، اور کچھ مساجد فجر اس حساب سے پہلے شروع کرتی ہیں۔ SajdaTime کوئی اضافی وقفہ نہیں جوڑتا۔</string>
    <string name="ramadan_info_mosque">جہاں آپ کی مسجد کا نقشہ مختلف ہو، وہاں اپنی مسجد کی پیروی کریں۔</string>
    <string name="ramadan_info_date">یہاں اسلامی تاریخ ام القریٰ کیلنڈر کے مطابق ہے۔ اگر آپ کی مسجد نے رمضان کسی اور دن شروع کیا ہے تو ترتیبات میں اسلامی تاریخ آگے پیچھے کریں۔ اس سے دنوں کی گنتی، سحری کی یاددہانی اور ام القریٰ کا عشاء کا اصول بھی بدل جاتا ہے۔</string>
    <string name="ramadan_eve_title_before">اس کیلنڈر کے مطابق رمضان %1$s کو شروع ہوگا</string>
    <string name="ramadan_eve_title_after">اس کیلنڈر کے مطابق رمضان %1$s کو شروع ہوا</string>
    <string name="ramadan_eve_body">بہت سی مساجد چاند دیکھ کر اپنے آغاز کا اعلان کرتی ہیں، اکثر ایک دن بعد۔ اگر آپ کی مسجد کی تاریخ مختلف ہو تو اسلامی تاریخ بدلنے کے لیے ٹیپ کریں۔ اس سے سحری کی یاددہانی اور ام القریٰ کا عشاء کا اصول بھی بدل جاتا ہے۔</string>
    <string name="ramadan_timetable_title">رمضان %1$d</string>
    <string name="ramadan_col_day">دن</string>
    <string name="ramadan_col_fast">روزہ</string>
    <string name="ramadan_approx_marker">تخمینی</string>
    <string name="export_ramadan">رمضان %1$d</string>
    <string name="export_ramadan_desc">مہینے کا ہر دن، سحری اور افطار کے ساتھ</string>
    <string name="pdf_period_ramadan">رمضان %1$d</string>
    <string name="pdf_column_ramadan_day">دن</string>
    <string name="pdf_ramadan_note">سحری فجر پر ختم ہوتی ہے۔ افطار مغرب پر۔ جہاں آپ کی مسجد کا نقشہ مختلف ہو، اپنی مسجد کی پیروی کریں۔</string>

    <!-- سحری کی یاددہانی -->
    <string name="settings_suhoor">سحری کی یاددہانی</string>
    <string name="settings_suhoor_help">رمضان کے دنوں میں فجر سے پہلے ایک یاددہانی، اس کیلنڈر کے مطابق جس پر SajdaTime سیٹ ہے۔ اس کی آواز اور خاموش موڈ کے اصول وہی ہیں جو آپ کے نماز کے الرٹس کے ہیں۔</string>
    <string name="suhoor_off">بند</string>
    <plurals name="suhoor_minutes_before">
        <item quantity="one">فجر سے %d منٹ پہلے</item>
        <item quantity="other">فجر سے %d منٹ پہلے</item>
    </plurals>
    <string name="notif_suhoor_title">سحری %1$s پر ختم ہوگی</string>
    <string name="notif_suhoor_body">%1$s %2$s میں شروع ہوگی</string>

    <!-- ہوم اسکرین ویجٹس -->
    <string name="widget_next_label">SajdaTime: اگلی نماز</string>
    <string name="widget_today_label">SajdaTime: آج کی نمازیں</string>
    <string name="widget_next_desc">اگلی نماز اور اس کا وقت، الٹی گنتی کے ساتھ۔ آپ کے فون کے ڈارک موڈ کے مطابق۔</string>
    <string name="widget_today_desc">آج کے چھ اوقات، اگلی نماز نمایاں۔ آپ کے فون کے ڈارک موڈ کے مطابق۔</string>
    <string name="widget_next_prayer">اگلی نماز</string>
    <string name="widget_tomorrow">کل</string>
    <string name="widget_next_line">اگلی: %1$s %2$s پر</string>
    <string name="widget_next_line_tomorrow">کل: %1$s %2$s پر</string>
    <string name="widget_no_location">اپنا مقام سیٹ کرنے کے لیے SajdaTime کھولیں</string>
    <string name="widget_a11y_next">اگلی نماز %1$s %2$s پر، %3$s میں</string>
    <string name="widget_a11y_next_tomorrow">اگلی نماز کل، %1$s %2$s پر، %3$s میں</string>
    <string name="widget_a11y_today">آج کے نماز کے اوقات۔ %1$s</string>
    <string name="widget_a11y_row">%1$s %2$s</string>
    <string name="widget_suhoor_ends">سحری ختم</string>
    <string name="widget_iftar">افطار</string>

    <!-- نماز ٹریکر (ریلیز B) -->
    <string name="settings_group_tracker">نماز ٹریکر</string>
    <string name="settings_tracker">نماز ٹریکر</string>
    <string name="settings_tracker_desc">اوقات کی اسکرین پر ہر نماز پر نشان لگائیں۔ سب کچھ اسی فون پر رہتا ہے اور ایپ ہٹانے پر مٹ جاتا ہے۔</string>
    <string name="settings_tracker_streak">سلسلہ دکھائیں</string>
    <string name="settings_tracker_streak_desc">لگاتار وہ دن گنیں جن میں پانچوں نمازوں پر نشان ہو</string>
    <string name="settings_tracker_save">ٹریکر فائل میں محفوظ کریں</string>
    <string name="settings_tracker_save_desc">ایک چھوٹی فائل جسے آپ اپنی مرضی کی جگہ رکھیں، دوسرے فون پر لے جانے کے لیے</string>
    <string name="settings_tracker_load">فائل سے ٹریکر لوڈ کریں</string>
    <string name="settings_tracker_load_desc">محفوظ فائل کے نشان شامل کرتا ہے۔ پہلے سے لگا کوئی نشان نہیں ہٹتا</string>
    <string name="settings_tracker_delete">ٹریکر کا ڈیٹا حذف کریں</string>
    <string name="settings_tracker_delete_desc">اس فون سے ہر نشان اور چھوٹ والا دن ہٹا دیتا ہے</string>
    <string name="tracker_delete_title">ٹریکر کا ڈیٹا حذف کریں؟</string>
    <string name="tracker_delete_body">اس فون پر لگا ہر نشان اور چھوٹ والا دن ہٹا دیا جائے گا۔ پہلے محفوظ کی گئی فائل متاثر نہیں ہوگی۔</string>
    <string name="action_delete">حذف کریں</string>
    <string name="tracker_tick_a11y">%1$s، %2$s</string>
    <string name="tracker_state_ticked">نشان لگا ہے</string>
    <string name="tracker_state_not_ticked">نشان نہیں لگا</string>
    <string name="tracker_state_excused">چھوٹ</string>
    <string name="tracker_state_not_due">ابھی وقت نہیں ہوا</string>
    <plurals name="tracker_streak_days">
        <item quantity="one">لگاتار %d دن</item>
        <item quantity="other">لگاتار %d دن</item>
    </plurals>
    <string name="tracker_open_history">پچھلے 30 دن</string>
    <string name="tracker_history_title">پچھلے 30 دن</string>
    <string name="tracker_excused">چھوٹ</string>
    <string name="tracker_excused_a11y">چھوٹ والا دن۔ کوئی وجہ درج نہیں کی جاتی۔</string>
    <string name="tracker_newer_version">یہ ٹریکر ڈیٹا SajdaTime کے نئے ورژن نے محفوظ کیا ہے۔ اسے بدلنے کے لیے ایپ اپ ڈیٹ کریں۔</string>
    <string name="tracker_saved">%1$s کے نام سے محفوظ ہو گیا</string>
    <plurals name="tracker_loaded">
        <item quantity="one">%d دن شامل یا اپ ڈیٹ ہوا</item>
        <item quantity="other">%d دن شامل یا اپ ڈیٹ ہوئے</item>
    </plurals>
    <string name="tracker_load_failed_format">یہ فائل SajdaTime ٹریکر کی فائل نہیں ہے۔</string>
    <string name="tracker_load_failed_newer">یہ فائل SajdaTime کے نئے ورژن نے محفوظ کی ہے۔ پہلے ایپ اپ ڈیٹ کریں۔</string>
    <string name="tracker_save_failed">فائل محفوظ نہیں ہو سکی۔ براہِ کرم دوبارہ کوشش کریں۔</string>
```

### B6. Turkish drafts (`app/src/main/res/values-tr/strings.xml`), not natively reviewed

Terms: İmsak is the everyday Turkish word for the time the fast begins and Diyanet prints it as the first
time of the day, so it labels the Fajr time (the app's Turkish calls the prayer "Sabah"). İftar. Sahur
for the meal and the reminder. Mazeretli for an excused day. See `RAMADAN-LOCATION-SPEC.md` section 5.

```xml
    <!-- Ramazan -->
    <string name="ramadan_card_label">Ramazan</string>
    <string name="ramadan_day_of">%2$d günün %1$d. günü</string>
    <string name="ramadan_suhoor_ends">İmsak (Sabah)</string>
    <string name="ramadan_iftar">İftar</string>
    <string name="ramadan_countdown_suhoor">İmsaka %1$s</string>
    <string name="ramadan_countdown_iftar">İftara %1$s</string>
    <string name="ramadan_next_suhoor_tomorrow">Yarın imsak %1$s</string>
    <string name="ramadan_month_ends_today">Bu takvime göre bugün Ramazan\'ın son günü. Bayramı caminiz duyurur.</string>
    <string name="ramadan_fast_length">Bugünkü oruç %1$s</string>
    <plurals name="ramadan_fast_change_longer">
        <item quantity="one">dünden %d dakika uzun</item>
        <item quantity="other">dünden %d dakika uzun</item>
    </plurals>
    <plurals name="ramadan_fast_change_shorter">
        <item quantity="one">dünden %d dakika kısa</item>
        <item quantity="other">dünden %d dakika kısa</item>
    </plurals>
    <string name="ramadan_fast_change_same">dünle aynı</string>
    <string name="ramadan_fast_line">%1$s, %2$s</string>
    <string name="ramadan_tomorrow_line">Yarın: imsak %1$s, iftar %2$s</string>
    <string name="ramadan_open_timetable">Tam imsakiye</string>
    <string name="ramadan_info_a11y">Bu Ramazan vakitleri hakkında</string>
    <string name="ramadan_info_title">Bu Ramazan vakitleri hakkında</string>
    <string name="ramadan_info_calc">SajdaTime, bulunduğunuz yer için hesaplanan Sabah ve Akşam vakitlerini gösterir ve bunları imsak ile iftar vakti olarak kullanır.</string>
    <string name="ramadan_info_margin">Birçok imsakiye ihtiyaten Sabah vaktinden birkaç dakika önce bir imsak vakti yazar ve bazı camiler Sabah vaktini bu hesaptan daha erken başlatır. SajdaTime hiçbir pay eklemez.</string>
    <string name="ramadan_info_mosque">Caminizin imsakiyesi farklıysa caminize uyun.</string>
    <string name="ramadan_info_date">Buradaki hicri tarih Ümmülkura takvimine göredir. Caminiz Ramazan\'a başka bir gün başladıysa Ayarlar\'dan hicri tarihi kaydırın. Bu, gün sayısını, sahur hatırlatıcısını ve Ümmülkura yatsı kuralını da değiştirir.</string>
    <string name="ramadan_eve_title_before">Bu takvime göre Ramazan %1$s başlıyor</string>
    <string name="ramadan_eve_title_after">Bu takvime göre Ramazan %1$s başladı</string>
    <string name="ramadan_eve_body">Birçok cami başlangıcı hilali gördükten sonra, çoğu zaman bir gün sonra duyurur. Caminizinki farklıysa hicri tarihi kaydırmak için dokunun. Bu, sahur hatırlatıcısını ve Ümmülkura yatsı kuralını da değiştirir.</string>
    <string name="ramadan_timetable_title">Ramazan %1$d</string>
    <string name="ramadan_col_day">Gün</string>
    <string name="ramadan_col_fast">Oruç</string>
    <string name="ramadan_approx_marker">yaklaşık</string>
    <string name="export_ramadan">Ramazan %1$d</string>
    <string name="export_ramadan_desc">Ayın her günü, imsak ve iftarla</string>
    <string name="pdf_period_ramadan">Ramazan %1$d</string>
    <string name="pdf_column_ramadan_day">Gün</string>
    <string name="pdf_ramadan_note">İmsak Sabah vaktidir. İftar Akşam vaktidir. Caminizin imsakiyesi farklıysa caminize uyun.</string>

    <!-- Sahur hatırlatıcısı -->
    <string name="settings_suhoor">Sahur hatırlatıcısı</string>
    <string name="settings_suhoor_help">Ramazan günlerinde, SajdaTime\'ın ayarlı olduğu takvime göre, Sabah vaktinden önce bir hatırlatma. Ses ve sessiz mod kuralları namaz uyarılarınızla aynıdır.</string>
    <string name="suhoor_off">Kapalı</string>
    <plurals name="suhoor_minutes_before">
        <item quantity="one">Sabah vaktinden %d dakika önce</item>
        <item quantity="other">Sabah vaktinden %d dakika önce</item>
    </plurals>
    <string name="notif_suhoor_title">İmsak %1$s</string>
    <string name="notif_suhoor_body">%1$s vakti %2$s sonra</string>

    <!-- Ana ekran widget\'ları -->
    <string name="widget_next_label">SajdaTime: sonraki namaz</string>
    <string name="widget_today_label">SajdaTime: bugünün namazları</string>
    <string name="widget_next_desc">Sonraki namaz ve vakti, geri sayımla. Telefonunuzun koyu temasına uyar.</string>
    <string name="widget_today_desc">Bugünün altı vakti, sıradaki işaretli. Telefonunuzun koyu temasına uyar.</string>
    <string name="widget_next_prayer">Sonraki namaz</string>
    <string name="widget_tomorrow">Yarın</string>
    <string name="widget_next_line">Sonraki: %1$s %2$s</string>
    <string name="widget_next_line_tomorrow">Yarın: %1$s %2$s</string>
    <string name="widget_no_location">Konumunuzu ayarlamak için SajdaTime\'ı açın</string>
    <string name="widget_a11y_next">Sonraki namaz %1$s, saat %2$s, %3$s sonra</string>
    <string name="widget_a11y_next_tomorrow">Sonraki namaz yarın, %1$s, saat %2$s, %3$s sonra</string>
    <string name="widget_a11y_today">Bugünün namaz vakitleri. %1$s</string>
    <string name="widget_a11y_row">%1$s %2$s</string>
    <string name="widget_suhoor_ends">İmsak</string>
    <string name="widget_iftar">İftar</string>

    <!-- Namaz takibi (Sürüm B) -->
    <string name="settings_group_tracker">NAMAZ TAKİBİ</string>
    <string name="settings_tracker">Namaz takibi</string>
    <string name="settings_tracker_desc">Vakitler ekranında her namazı işaretleyin. Her şey bu telefonda kalır ve uygulama kaldırılınca silinir.</string>
    <string name="settings_tracker_streak">Seriyi göster</string>
    <string name="settings_tracker_streak_desc">Beş vaktin de işaretli olduğu ardışık günleri sayar</string>
    <string name="settings_tracker_save">Takibi dosyaya kaydet</string>
    <string name="settings_tracker_save_desc">Yerini sizin seçtiğiniz küçük bir dosya, başka bir telefona taşımak için</string>
    <string name="settings_tracker_load">Takibi dosyadan yükle</string>
    <string name="settings_tracker_load_desc">Kayıtlı bir dosyadaki işaretleri ekler. Önceden işaretli hiçbir şey silinmez</string>
    <string name="settings_tracker_delete">Takip verilerini sil</string>
    <string name="settings_tracker_delete_desc">Bu telefondaki her işareti ve mazeretli günü kaldırır</string>
    <string name="tracker_delete_title">Takip verileri silinsin mi?</string>
    <string name="tracker_delete_body">Bu telefondaki her işaret ve mazeretli gün kaldırılacak. Daha önce kaydettiğiniz bir dosya etkilenmez.</string>
    <string name="action_delete">Sil</string>
    <string name="tracker_tick_a11y">%1$s, %2$s</string>
    <string name="tracker_state_ticked">işaretli</string>
    <string name="tracker_state_not_ticked">işaretsiz</string>
    <string name="tracker_state_excused">mazeretli</string>
    <string name="tracker_state_not_due">vakti henüz girmedi</string>
    <plurals name="tracker_streak_days">
        <item quantity="one">üst üste %d gün</item>
        <item quantity="other">üst üste %d gün</item>
    </plurals>
    <string name="tracker_open_history">Son 30 gün</string>
    <string name="tracker_history_title">Son 30 gün</string>
    <string name="tracker_excused">Mazeretli</string>
    <string name="tracker_excused_a11y">Mazeretli gün. Sebep kaydedilmez.</string>
    <string name="tracker_newer_version">Bu takip verileri SajdaTime\'ın daha yeni bir sürümüyle kaydedilmiş. Değiştirmek için uygulamayı güncelleyin.</string>
    <string name="tracker_saved">%1$s olarak kaydedildi</string>
    <plurals name="tracker_loaded">
        <item quantity="one">%d gün eklendi veya güncellendi</item>
        <item quantity="other">%d gün eklendi veya güncellendi</item>
    </plurals>
    <string name="tracker_load_failed_format">Bu dosya bir SajdaTime takip dosyası değil.</string>
    <string name="tracker_load_failed_newer">Bu dosya SajdaTime\'ın daha yeni bir sürümüyle kaydedilmiş. Önce uygulamayı güncelleyin.</string>
    <string name="tracker_save_failed">Dosya kaydedilemedi. Lütfen tekrar deneyin.</string>
```

### B7. Indonesian drafts (`app/src/main/res/values-in/strings.xml`), not natively reviewed

Terms: sahur, buka puasa, imsak only inside the sentence about other timetables (in Indonesia imsak is
printed ten minutes before Subuh, so Subuh must not be called imsak), uzur for an excused day (the
established word for a valid excuse), centang for a tick. Hyphens are kept only where Indonesian spelling
requires them ("ke-%1$d", "berturut-turut").

```xml
    <!-- Ramadan -->
    <string name="ramadan_card_label">Ramadan</string>
    <string name="ramadan_day_of">Hari ke-%1$d dari %2$d</string>
    <string name="ramadan_suhoor_ends">Sahur berakhir</string>
    <string name="ramadan_iftar">Buka puasa</string>
    <string name="ramadan_countdown_suhoor">Sahur berakhir dalam %1$s</string>
    <string name="ramadan_countdown_iftar">Buka puasa dalam %1$s</string>
    <string name="ramadan_next_suhoor_tomorrow">Sahur besok berakhir pukul %1$s</string>
    <string name="ramadan_month_ends_today">Menurut kalender ini, hari ini adalah hari terakhir Ramadan. Masjid Anda yang akan mengumumkan Idulfitri.</string>
    <string name="ramadan_fast_length">Puasa hari ini %1$s</string>
    <plurals name="ramadan_fast_change_longer">
        <item quantity="other">%d menit lebih lama dari kemarin</item>
    </plurals>
    <plurals name="ramadan_fast_change_shorter">
        <item quantity="other">%d menit lebih singkat dari kemarin</item>
    </plurals>
    <string name="ramadan_fast_change_same">sama dengan kemarin</string>
    <string name="ramadan_fast_line">%1$s, %2$s</string>
    <string name="ramadan_tomorrow_line">Besok: sahur berakhir %1$s, buka puasa %2$s</string>
    <string name="ramadan_open_timetable">Jadwal lengkap</string>
    <string name="ramadan_info_a11y">Tentang waktu Ramadan ini</string>
    <string name="ramadan_info_title">Tentang waktu Ramadan ini</string>
    <string name="ramadan_info_calc">SajdaTime menampilkan Subuh dan Magrib sebagaimana dihitung untuk lokasi Anda dan menggunakannya sebagai akhir sahur dan waktu berbuka.</string>
    <string name="ramadan_info_margin">Banyak jadwal mencantumkan waktu imsak beberapa menit sebelum Subuh sebagai kehati-hatian, dan sebagian masjid memulai Subuh lebih awal dari perhitungan ini. SajdaTime tidak menambahkan selisih apa pun.</string>
    <string name="ramadan_info_mosque">Jika jadwal masjid Anda berbeda, ikuti masjid Anda.</string>
    <string name="ramadan_info_date">Tanggal Hijriah di sini mengikuti kalender Umm al-Qura. Jika masjid Anda memulai Ramadan pada hari yang berbeda, geser tanggal Hijriah di Pengaturan. Itu juga menggeser hitungan hari, pengingat sahur, dan aturan Isya Umm al-Qura.</string>
    <string name="ramadan_eve_title_before">Menurut kalender ini Ramadan dimulai %1$s</string>
    <string name="ramadan_eve_title_after">Menurut kalender ini Ramadan telah dimulai %1$s</string>
    <string name="ramadan_eve_body">Banyak masjid mengumumkan awal Ramadan setelah rukyat hilal, sering kali sehari kemudian. Jika masjid Anda berbeda, ketuk untuk menggeser tanggal Hijriah. Itu juga menggeser pengingat sahur dan aturan Isya Umm al-Qura.</string>
    <string name="ramadan_timetable_title">Ramadan %1$d</string>
    <string name="ramadan_col_day">Hari</string>
    <string name="ramadan_col_fast">Puasa</string>
    <string name="ramadan_approx_marker">perkiraan</string>
    <string name="export_ramadan">Ramadan %1$d</string>
    <string name="export_ramadan_desc">Setiap hari sepanjang bulan, dengan sahur dan buka puasa</string>
    <string name="pdf_period_ramadan">Ramadan %1$d</string>
    <string name="pdf_column_ramadan_day">Hari</string>
    <string name="pdf_ramadan_note">Sahur berakhir pada Subuh. Buka puasa pada Magrib. Jika jadwal masjid Anda berbeda, ikuti masjid Anda.</string>

    <!-- Pengingat sahur -->
    <string name="settings_suhoor">Pengingat sahur</string>
    <string name="settings_suhoor_help">Pengingat sebelum Subuh pada hari-hari Ramadan, menurut kalender yang diatur di SajdaTime. Suara dan aturan mode senyapnya sama dengan notifikasi sholat Anda.</string>
    <string name="suhoor_off">Nonaktif</string>
    <plurals name="suhoor_minutes_before">
        <item quantity="other">%d menit sebelum Subuh</item>
    </plurals>
    <string name="notif_suhoor_title">Sahur berakhir pukul %1$s</string>
    <string name="notif_suhoor_body">%1$s dalam %2$s</string>

    <!-- Widget layar utama -->
    <string name="widget_next_label">SajdaTime: sholat berikutnya</string>
    <string name="widget_today_label">SajdaTime: sholat hari ini</string>
    <string name="widget_next_desc">Sholat berikutnya dan waktunya, dengan hitung mundur. Mengikuti mode gelap ponsel Anda.</string>
    <string name="widget_today_desc">Enam waktu hari ini dengan yang berikutnya ditandai. Mengikuti mode gelap ponsel Anda.</string>
    <string name="widget_next_prayer">Sholat berikutnya</string>
    <string name="widget_tomorrow">Besok</string>
    <string name="widget_next_line">Berikutnya: %1$s pukul %2$s</string>
    <string name="widget_next_line_tomorrow">Besok: %1$s pukul %2$s</string>
    <string name="widget_no_location">Buka SajdaTime untuk mengatur lokasi Anda</string>
    <string name="widget_a11y_next">Sholat berikutnya %1$s pukul %2$s, dalam %3$s</string>
    <string name="widget_a11y_next_tomorrow">Sholat berikutnya besok, %1$s pukul %2$s, dalam %3$s</string>
    <string name="widget_a11y_today">Jadwal sholat hari ini. %1$s</string>
    <string name="widget_a11y_row">%1$s %2$s</string>
    <string name="widget_suhoor_ends">Sahur berakhir</string>
    <string name="widget_iftar">Buka puasa</string>

    <!-- Pencatat sholat (Rilis B) -->
    <string name="settings_group_tracker">PENCATAT SHOLAT</string>
    <string name="settings_tracker">Pencatat sholat</string>
    <string name="settings_tracker_desc">Centang setiap sholat di layar Waktu. Semuanya tetap di ponsel ini dan terhapus jika aplikasi dicopot.</string>
    <string name="settings_tracker_streak">Tampilkan rentetan</string>
    <string name="settings_tracker_streak_desc">Menghitung hari berturut-turut dengan kelima sholat tercentang</string>
    <string name="settings_tracker_save">Simpan catatan ke berkas</string>
    <string name="settings_tracker_save_desc">Berkas kecil yang tempatnya Anda pilih sendiri, untuk dipindahkan ke ponsel lain</string>
    <string name="settings_tracker_load">Muat catatan dari berkas</string>
    <string name="settings_tracker_load_desc">Menambahkan centang dari berkas tersimpan. Centang yang sudah ada tidak dihapus</string>
    <string name="settings_tracker_delete">Hapus data pencatat</string>
    <string name="settings_tracker_delete_desc">Menghapus setiap centang dan hari uzur dari ponsel ini</string>
    <string name="tracker_delete_title">Hapus data pencatat?</string>
    <string name="tracker_delete_body">Setiap centang dan hari uzur di ponsel ini akan dihapus. Berkas yang Anda simpan sebelumnya tidak terpengaruh.</string>
    <string name="action_delete">Hapus</string>
    <string name="tracker_tick_a11y">%1$s, %2$s</string>
    <string name="tracker_state_ticked">tercentang</string>
    <string name="tracker_state_not_ticked">belum tercentang</string>
    <string name="tracker_state_excused">uzur</string>
    <string name="tracker_state_not_due">belum masuk waktu</string>
    <plurals name="tracker_streak_days">
        <item quantity="other">%d hari berturut-turut</item>
    </plurals>
    <string name="tracker_open_history">30 hari terakhir</string>
    <string name="tracker_history_title">30 hari terakhir</string>
    <string name="tracker_excused">Uzur</string>
    <string name="tracker_excused_a11y">Hari uzur. Tidak ada alasan yang dicatat.</string>
    <string name="tracker_newer_version">Data pencatat ini disimpan oleh versi SajdaTime yang lebih baru. Perbarui aplikasi untuk mengubahnya.</string>
    <string name="tracker_saved">Disimpan sebagai %1$s</string>
    <plurals name="tracker_loaded">
        <item quantity="other">%d hari ditambahkan atau diperbarui</item>
    </plurals>
    <string name="tracker_load_failed_format">Berkas itu bukan berkas pencatat SajdaTime.</string>
    <string name="tracker_load_failed_newer">Berkas itu disimpan oleh versi SajdaTime yang lebih baru. Perbarui aplikasi terlebih dahulu.</string>
    <string name="tracker_save_failed">Berkas tidak dapat disimpan. Silakan coba lagi.</string>
```

Note on plurals: Indonesian has only the `other` quantity in CLDR, so its plurals carry one item. Urdu
and Turkish carry `one` and `other` as the existing `adjustment_days` plural does
(`app/src/main/res/values-ur/strings.xml`, same key). Lint accepts both shapes.

## C. Store listing, What's new, privacy policy and public pages

### C0. Play's limits, and where they come from

| Field | Limit | Source |
|---|---|---|
| App name | 30 | `docs/store/LISTING.md:8` (unchanged: "SajdaTime: Prayer Times, Qibla", 30 of 30) |
| Short description | 80 | `docs/store/LISTING.md:18`, `docs/RELEASING.md:506` (unchanged: 79 of 80, "No accounts." is live) |
| Full description | 4,000 | `docs/store/LISTING.md:142`, `docs/RELEASING.md:507`. Measured below by `len()` on the exact text |
| Release notes (What's new) | 500 per language | **Not in the existing docs.** Known Play Console limit, **assumed**: confirm against the Console's own character counter before pasting, and shorten if it disagrees |
| Translated listings | same limits per language | `docs/store/LISTING-LOCALES.md:3-10` |

### C1. English full description, Release A (3,923 of 4,000)

Changes from the live text, in order (lines refer to the fenced block in `docs/store/LISTING.md`):

1. Line 157 "The Hijri date alongside the ordinary date, with the Islamic month names built in" shortened to
   "The Hijri date alongside the ordinary date" (room was needed).
2. After line 163 (the PDF bullet) a new bullet: "Two home screen widgets: the next prayer with a
   countdown, and all of today's times".
3. Line 164 (the sehri and iftar line) replaced by: "In Ramadan: suhoor (sehri) end and iftar for your
   location, a countdown, today's fast length, tomorrow's times, a timetable to view or save as PDF,
   and an optional suhoor reminder before Fajr". "sehri" is kept in brackets because it is the South
   Asian search word the live line already carries.
4. After line 166 (the themes bullet) a new bullet: "In English, Urdu, Turkish and Indonesian, chosen
   in Settings".
5. Lines 177 to 178 shortened to "A high-latitude rule for northern countries where the sun never
   fully sets in summer".
6. Line 179 replaced by "With the Umm al-Qura method, its Ramadan Isha adjustment is applied
   automatically" (audit L6).
7. Lines 183 to 184 shortened to "Most free prayer apps are funded by advertising, which means you pay
   with your data. This one is not."
8. Line 176, the school line, is **not touched** (the live line the owner approved on 13 Oct 2026).
9. Optional style fix in the HONEST paragraph: the semicolon after "published calculation methods"
   becomes a full stop and "they" becomes "They" (same length, `DisclaimerContentTest` matches phrases
   so it stays green).

The full text to paste, byte for byte:

```
SajdaTime tells you when to pray and which way to face.
That is all it does, and it does it without ads, without an account, and without sending your location anywhere.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

Whether you call them namaz, salah or salat, these are the same five daily prayers.

WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• The Hijri date alongside the ordinary date
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode using any tone or adhan already on your phone, loud enough for Fajr
• An optional quiet badge in the notification shade showing the next prayer
• A printable PDF timetable for today, the next seven days, or the whole month
• Two home screen widgets: the next prayer with a countdown, and all of today's times
• In Ramadan: suhoor (sehri) end and iftar for your location, a countdown, today's fast length, tomorrow's times, a timetable to view or save as PDF, and an optional suhoor reminder before Fajr
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand, both checked for readability
• In English, Urdu, Turkish and Indonesian, chosen in Settings

BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly, not as an afterthought.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai,
  Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The method is chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia), and you can override it
• A high-latitude rule for northern countries where the sun never fully sets in summer
• With the Umm al-Qura method, its Ramadan Isha adjustment is applied automatically

YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are funded by advertising, which means you pay with your data. This one is not.

• No adverts, ever
• No accounts, no sign-in, no email address
• Optional usage counts that you control in Settings. No crash reporting
• Approximate location only, read while the app is open, never in the background
• Your coordinates never leave your device, and are never part of the optional usage counts
• Cloud backup is deliberately switched off so nothing can be copied off your phone

If you would rather not share your location at all, you can type a city name instead, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun using published calculation methods; they are not supplied by any mosque, scholar or authority. The app was written by one person, with the help of artificial intelligence, and it can get things wrong. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever looks off, or you are unsure, please ask them or someone else qualified to advise you.

Prayer times are calculated using adhan-java by Batoul Apps, a widely used open-source library, and are checked against independent reference timetables before each release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

### C2. English full description, Release B (3,983 of 4,000)

On top of C1: a tracker bullet after the languages bullet, "An optional prayer tracker: tick each prayer,
see the last 30 days, mark excused days, keep a streak. Stays on your phone, and can be saved to a file
you keep", and four small cuts to stay inside the limit: the Ramadan bullet loses "tomorrow's times"
(the card still shows them), "If you would rather not share your location at all, you can type a city
name instead" becomes "You can type a city name instead of sharing your location", "not as an
afterthought" is dropped, "a widely used open-source library" becomes "an open source library", and
"showing the next prayer" becomes "with the next prayer". The full text to paste:

```
SajdaTime tells you when to pray and which way to face.
That is all it does, and it does it without ads, without an account, and without sending your location anywhere.

Prayer times are calculated on your own phone, so the app keeps working on a plane, in a basement, or with no signal at all.

Whether you call them namaz, salah or salat, these are the same five daily prayers.

WHAT YOU GET

• The five daily prayers plus sunrise, worked out for wherever you are
• The Hijri date alongside the ordinary date
• A live countdown to the next prayer
• A Qibla compass corrected to true north, not magnetic north
• Notifications at each prayer time, even when the app is closed
• An optional alarm mode using any tone or adhan already on your phone, loud enough for Fajr
• An optional quiet badge in the notification shade with the next prayer
• A printable PDF timetable for today, the next seven days, or the whole month
• Two home screen widgets: the next prayer with a countdown, and all of today's times
• In Ramadan: suhoor (sehri) end and iftar for your location, a countdown, today's fast length, a full timetable to save as PDF, and an optional suhoor reminder
• A Wear OS app and watch tile that work on their own, with or without your phone
• Light and dark themes, following your phone or set by hand, both checked for readability
• In English, Urdu, Turkish and Indonesian, chosen in Settings
• An optional prayer tracker: tick each prayer, see the last 30 days, mark excused days, keep a streak. Stays on your phone, and can be saved to a file you keep

BUILT FOR YOUR SCHOOL OF THOUGHT

Sunni and Shia conventions are both supported properly.

• Sunni: Hanafi, Shafi'i, Maliki and Hanbali, with the correct Asr rule for each
• Shia: Jafari / Ithna Ashari, including the correct Maghrib rule
• Calculation methods: Muslim World League, ISNA, Umm al-Qura, Egyptian, Karachi, Dubai, Kuwait, Qatar, Singapore, Turkey, Moonsighting Committee and more
• The method is chosen automatically for your school (Muslim World League for Sunni, Jafari for Shia), and you can override it
• A high-latitude rule for northern countries where the sun never fully sets in summer
• With the Umm al-Qura method, its Ramadan Isha adjustment is applied automatically

YOUR PRIVACY IS NOT THE PRICE

Most free prayer apps are funded by advertising, which means you pay with your data. This one is not.

• No adverts, ever
• No accounts, no sign-in, no email address
• Optional usage counts that you control in Settings. No crash reporting
• Approximate location only, read while the app is open, never in the background
• Your coordinates never leave your device, and are never part of the optional usage counts
• Cloud backup is deliberately switched off so nothing can be copied off your phone

You can type a city name instead of sharing your location, and the app works the same.

HONEST ABOUT WHAT IT IS

SajdaTime is a helper, not a religious authority. Your phone works these times out from the position of the sun using published calculation methods; they are not supplied by any mosque, scholar or authority. The app was written by one person, with the help of artificial intelligence, and it can get things wrong. Where SajdaTime and your mosque disagree, follow your mosque. If a time or a direction ever looks off, or you are unsure, please ask them or someone else qualified to advise you.

Prayer times are calculated with adhan-java by Batoul Apps, an open source library, and are checked against independent reference timetables before each release. Even so, the app is given free and as it is, with no warranty and no promise of accuracy, so where being exact matters to you, please do not rely on it alone.

FREE, AND STAYING FREE

There is no paid tier, no subscription and nothing locked away. SajdaTime is given freely
as an ongoing charity for the Ummah.

Only one thing is ever asked in return: please remember me, my family, and my parents in your duas. JazakAllah Kher.
```

Measure before pasting, every time: `python3 -c "import sys;print(len(open(sys.argv[1]).read().rstrip()))" file.txt`
and, after saving in the Console, the hash check `docs/store/LISTING.md:232-238` describes.

### C3. Translated listings (`docs/store/LISTING-LOCALES.md`): the lines to add or replace

For Urdu, Turkish and Indonesian, which are the app's own languages. The five other drafts (Malay,
Bengali, Arabic, French, Hindi) get the same five bullets and the two HONEST paragraphs translated by the
implementer in A10 and B7 following these, because the owner may publish any of the eight. Everything
below is a Claude draft with no native speaker review.

**Bullets (A adds the first four and replaces the Umm al-Qura line, B adds the fifth).**

Urdu:

```
• عام تاریخ کے ساتھ ہجری تاریخ
• ہوم اسکرین کے دو ویجٹ: اگلی نماز الٹی گنتی کے ساتھ، اور آج کے تمام اوقات
• رمضان میں: آپ کے مقام کے لیے سحری کا اختتام اور افطار، الٹی گنتی، آج کے روزے کا دورانیہ، کل کے اوقات، دیکھنے یا PDF میں محفوظ کرنے کے لیے پورا نقشہ، اور فجر سے پہلے سحری کی اختیاری یاددہانی
• انگریزی، اردو، ترکی اور انڈونیشی میں، ترتیبات سے منتخب کریں
• ام القریٰ طریقے کے ساتھ، رمضان میں اس کی عشاء کی ترمیم خود بخود لاگو ہوتی ہے
• اختیاری نماز ٹریکر: ہر نماز پر نشان لگائیں، پچھلے 30 دن دیکھیں، چھوٹ کے دن نشان زد کریں، سلسلہ برقرار رکھیں۔ آپ کے فون پر رہتا ہے اور ایک ایسی فائل میں محفوظ کیا جا سکتا ہے جو آپ کے پاس رہے
```

Turkish (also change the two semicolons at `LISTING-LOCALES.md:489` and `:534` to full stops, and
"makul imsak ve yatsı vakitleri" at `:517` to "makul sabah ve yatsı vakitleri"):

```
• Miladi tarihin yanında hicri tarih
• İki ana ekran widget'ı: geri sayımlı sonraki namaz ve bugünün tüm vakitleri
• Ramazan'da: bulunduğunuz yer için imsak ve iftar, geri sayım, bugünkü orucun süresi, yarının vakitleri, görüntülenecek veya PDF olarak kaydedilecek tam imsakiye ve sabah namazından önce isteğe bağlı sahur hatırlatıcısı
• İngilizce, Urduca, Türkçe ve Endonezce, Ayarlar'dan seçilir
• Ümmü'l-Kura yöntemiyle Ramazan yatsı düzeltmesi otomatik uygulanır
• İsteğe bağlı namaz takibi: her namazı işaretleyin, son 30 günü görün, mazeretli günleri işaretleyin, seri tutun. Telefonunuzda kalır ve sizde duracak bir dosyaya kaydedilebilir
```

Indonesian:

```
• Tanggal Hijriah di samping tanggal biasa
• Dua widget layar utama: sholat berikutnya dengan hitung mundur, dan semua waktu hari ini
• Saat Ramadan: akhir sahur dan buka puasa untuk lokasi Anda, hitung mundur, lama puasa hari ini, waktu besok, jadwal lengkap untuk dilihat atau disimpan sebagai PDF, dan pengingat sahur opsional sebelum Subuh
• Dalam bahasa Inggris, Urdu, Turki, dan Indonesia, dipilih di Pengaturan
• Dengan metode Umm al-Qura, penyesuaian Isya Ramadannya diterapkan otomatis
• Pencatat sholat opsional: centang setiap sholat, lihat 30 hari terakhir, tandai hari uzur, jaga rentetan. Tetap di ponsel Anda dan dapat disimpan ke berkas yang Anda pegang sendiri
```

**The HONEST section, all four points (replaces the stale two sentence version, audit X4).**

Urdu, replacing `LISTING-LOCALES.md:285-293`:

```
جو ہے، سچ ہے

SajdaTime ایک مددگار ہے، دینی مرجع نہیں۔ آپ کا فون یہ اوقات سورج کی پوزیشن سے، شائع شدہ حسابی طریقوں کے ذریعے نکالتا ہے۔ یہ کسی مسجد، عالم یا اتھارٹی کی طرف سے فراہم نہیں کیے گئے۔ یہ ایپ ایک شخص نے مصنوعی ذہانت کی مدد سے بنائی ہے اور اس سے غلطی ہو سکتی ہے۔ جہاں SajdaTime اور آپ کی مسجد میں اختلاف ہو، وہاں اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت درست نہ لگے، یا آپ کو یقین نہ ہو، تو براہِ کرم ان سے یا کسی اور اہل شخص سے پوچھ لیں۔

نماز کے اوقات Batoul Apps کی اوپن سورس لائبریری adhan-java سے شمار ہوتے ہیں اور ہر اجرا سے پہلے آزاد حوالہ جاتی نقشوں سے جانچے جاتے ہیں۔ پھر بھی یہ ایپ مفت ہے اور جیسی ہے ویسی ہی دی جا رہی ہے، کسی ضمانت اور درستگی کے کسی وعدے کے بغیر، اس لیے جہاں بالکل درست ہونا آپ کے لیے اہم ہو، وہاں صرف اسی پر بھروسہ نہ کریں۔
```

Turkish, replacing `LISTING-LOCALES.md:538-546`:

```
NE OLDUĞU KONUSUNDA DÜRÜST

SajdaTime bir yardımcıdır, dinî bir merci değildir. Telefonunuz bu vakitleri güneşin konumundan, yayımlanmış hesaplama yöntemleriyle hesaplar. Hiçbir cami, âlim ya da makam tarafından verilmiş değildir. Uygulama tek kişi tarafından yapay zekâ desteğiyle yazılmıştır ve hata yapabilir. SajdaTime ile caminiz arasında fark varsa caminize uyun. Bir vakit veya yön size yanlış görünürse ya da emin değilseniz lütfen onlara veya ehil birine danışın.

Namaz vakitleri Batoul Apps'in açık kaynaklı adhan-java kütüphanesiyle hesaplanır ve her sürümden önce bağımsız referans cetvelleriyle karşılaştırılır. Yine de uygulama ücretsiz ve olduğu gibi sunulur, hiçbir garanti ve doğruluk vaadi yoktur. Kesinliğin sizin için önemli olduğu yerde lütfen yalnızca buna güvenmeyin.
```

Indonesian, replacing `LISTING-LOCALES.md:113-120`:

```
JUJUR TENTANG APA ADANYA

SajdaTime adalah alat bantu, bukan otoritas keagamaan. Ponsel Anda menghitung waktu-waktu ini dari posisi matahari dengan metode perhitungan yang telah dipublikasikan. Waktu-waktu ini tidak diberikan oleh masjid, ulama, atau otoritas mana pun. Aplikasi ini ditulis oleh satu orang dengan bantuan kecerdasan buatan dan bisa saja keliru. Jika SajdaTime dan masjid Anda berbeda, ikuti masjid Anda. Jika suatu waktu atau arah terlihat tidak tepat, atau Anda ragu, mohon tanyakan kepada mereka atau kepada orang lain yang berkompeten.

Waktu sholat dihitung dengan adhan-java dari Batoul Apps, pustaka sumber terbuka, dan diperiksa terhadap jadwal rujukan independen sebelum setiap rilis. Meski begitu, aplikasi ini diberikan gratis apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi jika ketepatan penting bagi Anda, mohon jangan mengandalkannya semata.
```

Also in `LISTING-LOCALES.md`: delete the "app stays in English" block (audit X1) and replace the native
speaker note (X2) with the owner's decision. After pasting each translated full description into the
Console, run its length against 4,000: the Urdu and Turkish texts are longer than the English by about
ten percent, so if one exceeds the limit drop the Hijri date bullet from that language first.

### C4. What's new (release notes), four languages, each under 500 characters

Release A, English (392):

```
Ramadan: suhoor end and iftar for your location, a countdown, today's fast length, tomorrow's times, a full timetable to view or save as a PDF, and an optional suhoor reminder before Fajr. Two home screen widgets: the next prayer with a countdown, and all of today's times. Suhoor ends at Fajr and iftar is at Maghrib, as calculated. Where your mosque's timetable differs, follow your mosque.
```

Release A, Urdu:

```
رمضان: آپ کے مقام کے لیے سحری کا اختتام اور افطار، الٹی گنتی، آج کے روزے کا دورانیہ، کل کے اوقات، دیکھنے یا PDF میں محفوظ کرنے کے لیے پورا نقشہ، اور فجر سے پہلے سحری کی اختیاری یاددہانی۔ ہوم اسکرین کے دو ویجٹ: اگلی نماز الٹی گنتی کے ساتھ، اور آج کے تمام اوقات۔ سحری فجر پر ختم ہوتی ہے اور افطار مغرب پر، حساب کے مطابق۔ جہاں آپ کی مسجد کا نقشہ مختلف ہو، اپنی مسجد کی پیروی کریں۔
```

Release A, Turkish:

```
Ramazan: bulunduğunuz yer için imsak ve iftar, geri sayım, bugünkü orucun süresi, yarının vakitleri, görüntülenecek veya PDF olarak kaydedilecek tam imsakiye ve sabah namazından önce isteğe bağlı sahur hatırlatıcısı. İki ana ekran widget'ı: geri sayımlı sonraki namaz ve bugünün tüm vakitleri. İmsak hesaplanan sabah vaktidir, iftar hesaplanan akşam vaktidir. Caminizin imsakiyesi farklıysa caminize uyun.
```

Release A, Indonesian:

```
Ramadan: akhir sahur dan buka puasa untuk lokasi Anda, hitung mundur, lama puasa hari ini, waktu besok, jadwal lengkap untuk dilihat atau disimpan sebagai PDF, dan pengingat sahur opsional sebelum Subuh. Dua widget layar utama: sholat berikutnya dengan hitung mundur, dan semua waktu hari ini. Sahur berakhir pada Subuh dan buka puasa pada Magrib, sebagaimana dihitung. Jika jadwal masjid Anda berbeda, ikuti masjid Anda.
```

Release B, English (260):

```
Prayer tracker, off until you switch it on in Settings: tick each prayer on the Times screen, see the last 30 days, mark a day as excused, keep a streak if you want one. Everything stays on your phone. Save it to a file you choose and load it on another phone.
```

Release B, Urdu:

```
نماز ٹریکر، جب تک آپ ترتیبات میں آن نہ کریں بند: اوقات کی اسکرین پر ہر نماز پر نشان لگائیں، پچھلے 30 دن دیکھیں، کسی دن کو چھوٹ کا دن قرار دیں، چاہیں تو سلسلہ برقرار رکھیں۔ سب کچھ آپ کے فون پر رہتا ہے۔ اسے اپنی پسند کی فائل میں محفوظ کریں اور دوسرے فون پر لوڈ کریں۔
```

Release B, Turkish:

```
Namaz takibi, Ayarlar'dan açana kadar kapalı: Vakitler ekranında her namazı işaretleyin, son 30 günü görün, bir günü mazeretli işaretleyin, isterseniz seri tutun. Her şey telefonunuzda kalır. Seçtiğiniz bir dosyaya kaydedin ve başka bir telefonda yükleyin.
```

Release B, Indonesian:

```
Pencatat sholat, nonaktif sampai Anda mengaktifkannya di Pengaturan: centang setiap sholat di layar Waktu, lihat 30 hari terakhir, tandai hari sebagai uzur, jaga rentetan jika Anda mau. Semuanya tetap di ponsel Anda. Simpan ke berkas pilihan Anda dan muat di ponsel lain.
```

### C5. `docs/privacy.html`

**Release A (clarity only, no change to what is collected).** Under "What the app needs, and why", after
the paragraph ending "The app is fully usable that way." (line 54), add:

```html
      <p>The home screen widgets show times already worked out from the location saved on your phone.
      They read no location of their own and send nothing. The optional suhoor reminder uses the same
      alerts and permissions as the prayer alerts.</p>
```

and change line 38 to the release date: `Last updated: <date of the Release A merge> · <a href="#disclaimer">Disclaimer</a>`.

**Release B (required: a new category of data stored on the phone).** Add a section before "If you have
a Wear OS watch" (line 146):

```html
    <h2 id="tracker">Prayer tracker (optional)</h2>
    <p>Settings has a prayer tracker, off unless you switch it on. When it is on, the ticks you make for
    each prayer, and any day you mark as excused, are stored on your phone in the app's own data. No
    reason is ever asked for or stored for an excused day. Nothing from the tracker is sent anywhere: it
    is not part of the optional usage counts, it is not backed up to the cloud, and it is not carried to
    a new phone, because the app's backup and device transfer are switched off.</p>
    <p>"Save tracker to a file" writes a small file to a place you pick, and only you hold it. "Load
    tracker from a file" reads a file you pick and adds its ticks to what is already on the phone. "Delete
    tracker data" removes the tracker from the phone, and uninstalling the app removes it too. If a future
    version of the app no longer has the tracker, the data stays on your phone, unread, until you delete
    it or uninstall.</p>
```

In "What is never sent" (line 111) add "your prayer tracker" to the list: "…your alert and prayer
settings, your prayer tracker, and anything you have typed." In the app, `consent_never_list` gains a
bullet "• Your prayer tracker" in all four languages so the consent text and the policy still agree
(`UsageCountsTest`, "the consent text and the policy agree on what is sent"). Update the date line. The
Data safety form does not change: nothing new is transmitted off the device, which is the only thing the
form asks about (`docs/store/LISTING.md:316-324`).

### C6. `README.md` and `docs/index.html`

`README.md` "What it does" gains, after the PDF line: "Two home screen widgets: the next prayer with a
countdown, and all of today's times" and "During Ramadan, suhoor end and iftar for your location, the
fast length, a full timetable and PDF, and an optional suhoor reminder before Fajr. Suhoor ends at Fajr
and iftar is at Maghrib, as calculated. No imsak margin is added" (Release A), and "An optional prayer
tracker that stays on your phone, with save and load to a file you keep" (Release B). The languages line
and the two other corrections are in audit R1 to R3.

`docs/index.html` feature list (lines 42-47) gains the same three lines, shortened to one each, and a
languages line: "In English, Urdu, Turkish and Indonesian".
