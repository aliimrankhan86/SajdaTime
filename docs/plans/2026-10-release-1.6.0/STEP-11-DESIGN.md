# Step 11 design: Release (Fable `ux-lead`, 25 Oct 2026)

Spec from BUILD-INSTRUCTIONS row 11 and its "Release checklist for step 11", DETAILED-PLAN 8.16, PLAN.md §6
"Store listing", LISTING.md, LISTING-LOCALES.md, `docs/store/upload/README.md`, `tools/build-store-assets.sh`,
`release-1.5.0.sh`, HANDOVER §11 STATE OF PLAY and §5.29 to §5.35. Skills: none invoked (a store set is decided by
what shipped and Play's own limits, not by a design skill).

**Recommendation: eight English screenshots, six of them new or retaken, two slots freed (setup madhab and
Settings) for the two biggest 1.6 pictures (My prayers and the Timetable); four screenshots in each of Arabic,
Urdu, Turkish and Indonesian; one English What's new of 494 characters; three listing questions for Ali, the
rest mechanical. The watch is unchanged. Nothing here changes the app.**

## 1. The phone screenshot set, English (`raw/phone/en-GB/`, `upload/phone/en-GB/`)

Play allows eight per listing, so every slot has to earn its place; the order matters because search results
show the first two or three. No captions, as today (verified: the eight raw PNGs are plain screencaps with no
text laid over them; a caption is a word baked into an image, which the iPhone rule also forbids).

| # | File | Screen and state | Clock (London, `set_clock`) | Why this slot | Was |
|---|---|---|---|---|---|
| 01 | `01-times-light.png` | Times, light, day sky, bells on every row, "Timetable" button, four tabs, "Different from your mosque?" door at the foot as today | 15 Aug 2026 15:00 (`081515002026`) | The core screen; shows four of the 1.6 changes at once | retake of 01 |
| 02 | `02-my-prayers.png` | My prayers, light, seeded week (§1.2), Fajr and Dhuhr ticked, Asr next, rings Mon to Sat, "Pause" and "Last 30 days" | same | The headline feature; second because Play shows it beside 01 in search | new |
| 03 | `03-qibla.png` | Qibla, light, turning state: sensors as the README (facing 60°; from London the Qibla is 119°, so "Turn right 59°"), dial, key, "London", "Details" | same | The second search term; the turning state shows what the screen does for you and keeps the free maths check. The aligned state is a picture for Ali only if he asks | retake of 02 |
| 04 | `04-ramadan-dark.png` | Times, dark, Theme set to Dark in Settings, evening sky (15:00 is after Asr in February: `SkyPeriod` ASR and MAGHRIB give EVENING, verified), Ramadan card with suhoor, iftar, fast length, "Get the full Ramadan timetable" | 22 Feb 2027 15:00 (`022215002027`) | Dark theme and Ramadan in one; the release lands weeks before Ramadan 1448 | retake of 03 |
| 05 | `05-widgets.png` | Pixel launcher: the Today strip (4 by 1) in the top row, the Next prayer widget resized to four columns (it lands 3 by 2) under it on the evening sky with the London chip; the strip shows no town chip on this grid (it needs 380dp, the strip is about 289dp); light wallpaper as today | 15 Aug 2026 18:30 (`081518302026`: London MWL Asr 17:06, Maghrib 20:23, so the Asr window gives the evening sky, Maghrib next and the strip's pill on Maghrib; verifier finding 1) | Both 1.6 widget changes in one picture (the strip is new; the sky on Next is new). The unchanged Today 4 by 4 gives way: it had the slot in 1.5.0 and is in the description | retake of 06 |
| 06 | `06-timetable.png` | The Timetable sheet over Times, light: "Timetable" with X, "London", "Save as PDF", "August 2026", today's card marked "Today" at the top, two or three rows of day cards | 15 Aug 2026 15:00 | New in 1.6 and the only picture of the PDF door ("PDF timetables" is a listing bullet) | new |
| 07 | `07-alerts-and-adhan.png` | Settings scrolled to Reminders: Prayer alerts "All five · Mixed" (Fajr alarm, others notification), Notification sound usual tone, Alarm sound "Adhan", Reminder "15 minutes before · All five", Next prayer in notifications on | 15 Aug 2026 15:00 | Ali asked for it on 23 Oct 2026 ("alerts, adhan"); the tab bar now has four tabs so it must be retaken | retake of 07 |
| 08 | `08-ramadan-suhoor-reminder.png` | The Suhoor reminder dialog at its defaults, every choice visible, over Settings | 22 Feb 2027 15:00 | Ali asked for Ramadan settings (23 Oct 2026). Retaken only so the dimmed tab bar behind it matches the set | retake of 08 |

**Dropped, with reasons.** `04-school-of-thought.png` (the setup madhab step): true and unchanged in 1.6 (verified:
the only onboarding commit since `main` is the step 9 disclaimer removal), but it is the weakest slot: one setting,
no feature, and "for Sunni and Shia" is already in the short description, the feature graphic and the description.
`05-settings.png` (Settings top): its evidence was the School row and the Language row; the Language row reads the
same in English as in 1.5.0 (Ali chose A, step 10), and the four localised sets below are better proof of languages
than one English row. Between the two, Settings is the harder loss; §4 Q3 lets Ali keep it instead of the Timetable.

**Not shown on purpose.** Jama'ah lines under the rows in 01: the seed has no mosque times, and a hero shot with a
second time under every prayer is the kind of clutter a first look should not meet (DESIGN_SYSTEM §10 "fewer
numbers"). The Islamic dates sheet and the Last 30 days grid: good screens, no slot left; both are named in What's
new. The bell Undo message: a transient. 320dp, 2x and Urdu English shots: QA pictures, never store pictures.

### 1.1 Recipe facts that changed since `upload/README.md`

- **Place:** London through `ScreenSeedDeviceTest` (verified: it writes London, completes onboarding and answers
  the usage question No), not Manchester typed by hand as in 1.5.0. The release script already seeds this way (6f),
  so the store shots come off the same loop. The watch shots (`w1` to `w4`) still say Manchester; they are not
  retaken (§3), and nobody reads both names in one place on Play.
- **Demo status bar clock** set to match the emulated hour (`-e hhmm 1500`, as the 1.5.0 set shows "3:00").
- **Sky:** 01 shows the day sky (15:00 in August is before Asr 5:06 pm (London, MWL); DAY runs to Asr, verified in
  `SkyPeriod.kt`). 04 and 05 show the evening sky. The dawn and night skies are not in the store set: a night Times
  screen shows Isha next and a long countdown, which reads as "the app is for the night".
- **Dark theme for 04 and 08:** set Theme to Dark in the app, not the emulator's dark mode, then back to Follow
  phone after (the script must restore it: it is in the app's DataStore).
- **Everything else as the README:** `sajdastore` AVD, 1080 by 1920, `-read-only`, exact alarm and DND grants,
  sensors for Qibla, relaunch after the time zone change.

### 1.2 Seeding (verified in both tests)

- `ScreenSeedDeviceTest.seed_london_and_skip_the_first_run_screens`: London, onboarding done, usage question
  answered No. Runs first, every locale.
- `TrackerSeedDeviceTest.seed_a_week_of_ticks_with_today_started`: tracking on, streak off, relative to today:
  6 days ago 5 ticks, 5 ago 4, 4 ago 5, 3 ago paused, 2 ago 3, yesterday 2, today Fajr only. With the clock at
  Sat 15 Aug 2026 that gives Sun 5 (off the Monday week), Mon 4, Tue 5, Wed paused, Thu 3, Fri 2, Sat today, Sun
  empty: a full, varied week card. The main session reads the rings off the emulator, not this table.
  **Dhuhr is ticked by the script after seeding**
  (`tap_text "Dhuhr"` on the My prayers tab, wait one second for the Standard animation): at 3 pm a person who uses
  the tracker has ticked Dhuhr, and a screenshot of an unticked "Now" row says the opposite of what the feature is
  for. Fajr only (the seed as it stands) is the fallback if the tap is unreliable.
- `TrackerSeedDeviceTest.clear_the_record_and_switch_tracking_off` runs after the locale loop so the emulator is
  left as found. Both seed classes stay in the `-e notClass` exclusion of the device suite (checklist).
- **For 07 the seed does nothing:** the 1.5.0 picture set Fajr to Alarm, Alarm sound to Adhan and the reminder to 15
  minutes by hand. Recommended: add one method to `ScreenSeedDeviceTest` that writes exactly those four settings
  through `SettingsRepository`, so the shot is reproducible and the release script can take it. Mechanical; the
  reviewer checks it writes nothing else.

### 1.3 The four localised sets (`raw/phone/<code>/`, `upload/phone/<code>/`)

Play codes from LISTING-LOCALES headings: `ar`, `ur`, `tr-TR`, `id-ID`; English stays the default listing, folder
`en-GB`. Per app locale for capture as `release-1.5.0.sh` 6c and 6f: `cmd locale set-app-locales --locales ar|ur|tr|id`
(`id` is right here: Android maps it to the app's `values-in`; 1.5.0's loop took its five language shots this way).

| # | File | Same state as English | Note |
|---|---|---|---|
| 01 | `01-times-light.png` | English 01 | Urdu and Arabic: right to left, Western digits (verified rule, `ar-u-nu-latn`) |
| 02 | `02-my-prayers.png` | English 02 | Week starts by the app's language (Q5): Arabic and Urdu sets will not start on Monday; read it off the emulator, do not "fix" it |
| 03 | `03-qibla.png` | English 03 | Place name "London" is the seed's label, Latin in every language; accepted |
| 04 | `04-ramadan-dark.png` | English 04 | Added to the plan's three: the loop in 6f already takes a Ramadan Times shot per language, so it costs nothing, and a three picture listing looks thin beside the eight picture default |

Why four and not eight per language: four languages times eight is 32 extra pictures to judge and keep current;
four each shows the app speaks the language on its three main screens and in Ramadan, which is the claim. Malay,
Bengali, French and Hindi listings keep the English pictures (the app has no words in those languages; the listings
say so).

**Assumed, check in the Console:** a locale with its own screenshots shows only those (it does not append the
default's). If Play instead merges them, four is still right. If it shows only the four, that is the trade accepted
here.

### 1.4 Folder and script changes (mechanical)

- `git mv` the eight current `raw/phone/*.png` into `raw/phone/en-GB/` before retaking (history stays attached);
  the same for `upload/phone/`.
- `tools/build-store-assets.sh` line 150: `for src in "$root/docs/store/screenshots/raw/phone"/*/*.png`, writing to
  `$out/phone/<folder>/` with `mkdir -p`; the `find ... | sort` report at the end then lists them per folder.
- `upload/README.md` table: `phone/en-GB/` 8 files to the default listing; `phone/ar/`, `phone/ur/`, `phone/tr-TR/`,
  `phone/id-ID/` 4 files each to that language's listing (Grow users, Store presence, Store listings, pick the
  language, Phone screenshots). Add the §1.1 facts (London by seed, Dark via the app's Theme, the Dhuhr tap).
- LISTING.md "What the eight phone screenshots show" rewritten for this set, with the four locale sets listed.
- `release-1.6.0.sh`: a 6j block that takes the eight English and sixteen localised pictures
  straight into `raw/phone/<code>/` with the file names above (so "retake" is one command, and the health check
  baseline gets the new IDs after upload). The Timetable sheet and the Suhoor dialog are opened with `tap_text`
  ("Timetable"; "Suhoor reminder" in Settings), the pattern 6f uses for the Ramadan table.

## 2. What's new, 1.6.0, English (494 / 500, Python `len`)

```
My prayers: a private record of your prayers, this week and the last 30 days, kept on your phone. A bell on each prayer turns its alert on or off. Timetable: this month and next as day cards, with Save as PDF. Add your mosque's jama'ah and Jumu'ah times under each prayer. The Hijri date opens the Islamic dates of the year. Qibla now says which way to turn first. Times and the next prayer widget show the time of day. A five prayer widget strip. Where your mosque differs, follow your mosque.
```

Checked against what shipped: My prayers (step 1), bell (3), Timetable with this month and next (4, Ali's 24 Oct
answer), jama'ah and Jumu'ah typed by the user (7; "add" says it is theirs, never a feed), Islamic dates sheet
(`9cc78bf`, new since 1.5.0), Qibla direction first (6), skies on Times and on the Next prayer widget only (5 and 8;
the Today widgets have no sky, so "the next prayer widget" is exact), the strip (8). Left out on purpose: the Shia
dates (a line for some users in a 500 character note; the description can carry it), the disclaimer move (not a
feature), "smoother animation" (a claim no one can check), the languages (unchanged since 1.5.0). No "missed", no
"Tap", no hyphen or dash, no rating or sharing, nothing promised about acceptance or reward. The closing sentence is
the 1.5.0 one, kept so every note ends the same way.

**Languages:** the nine listing languages are English plus the eight in LISTING-LOCALES.md: Arabic (ar), Urdu (ur),
Turkish (tr-TR), Indonesian (id-ID), Malay (ms-MY), Bengali (bn-BD), French (fr-FR), Hindi (hi-IN). Ali said all
nine (Q23). The main session translates; each under 500 by Python `len`; the four app languages use the app's own
words for "My prayers" (`nav_tracker`), "Timetable" (`action_timetable`) and "Jama'ah times", so the note and the
screen agree. The Turkey line from 1.5.0 is not repeated.

**Watch release notes field:** nothing to paste; there is no new Wear release (§3).

## 3. Listing copy and the watch

**3.1 Short description (Q24, Ali's yes needed; §4 Q1).** Four lines fit 80; none fits every keyword:

| | Text | Length | Keeps | Loses |
|---|---|---|---|---|
| A, live 1.5.0 (verified on the public page, updated 23 Oct 2026) | Offline prayer times (namaz), adhan and Qibla for Sunni and Shia, with Wear OS. | 79 | namaz, Sunni and Shia, Wear OS | the tracker |
| B, the plan's Q24 | Offline prayer times, adhan, Qibla and a private salah tracker, with Wear OS. | 77 | tracker, Wear OS | namaz, Sunni and Shia |
| **C, recommended** | Offline prayer times (namaz), adhan, Qibla and a private salah tracker. Wear OS. | 80 | namaz, tracker, Wear OS | Sunni and Shia |
| D | Offline namaz times, adhan, Qibla and a private salah tracker, with Wear OS. | 76 | namaz, tracker, Wear OS | "prayer times" as a phrase |

Why C: "salah tracker" is the new thin search field (VISIBILITY-KEYWORDS row 63 said "not to be mentioned" only
because it was not shipped; it ships now), "namaz" was kept on evidence (LISTING.md "Why namaz went in after all"),
and "Sunni and Shia" stays in the first line of the full description and on the feature graphic. D is rejected
because "prayer times" is the phrase most searches use. The paste, save, reload and "may not be promoted" check from
LISTING.md applies; "private" is not a price or promotion word, but it is untested in that field.

**3.2 Full description.** It is 3991 / 4000, so additions need cuts. Proposed, for the main session to measure:

- New section after ALERTS AND ADHAN, heading `MY PRAYERS AND YOUR MOSQUE`:
  `• My prayers: a private salah tracker. Tick each prayer, see your week and the last 30 days. It stays on your phone`
  `• Your mosque's jama'ah (iqamah) and Jumu'ah times under each prayer, typed in once`
  `• A bell on each prayer to turn its alert on or off`
- PDF bullet becomes `• Timetable: this month and next as day cards, to save or print as a PDF, and the Ramadan timetable`
- Hijri bullet becomes `• The Hijri date (Islamic calendar) and the Islamic dates of the year, with Shia dates for Shia users`
- Widgets bullet gains `and a five prayer strip` after "today's times".
- "fasting days" is **not** added: the Fasts count is 1.6.1 (BUILD-INSTRUCTIONS §2). PLAN.md's keyword list predates
  that cut.
- Room comes from: the Chrome and Edge line if Ali removes it (§4 Q2, 42 characters), "Namaz, salah or salat: ..."
  shortened to `Namaz, salah or salat: worked out on your phone, so they work with no signal.`, and the first paragraph's
  "on your Android phone and Wear OS watch" to "on your phone and Wear OS watch". HONEST and FREE stay byte for byte.
  The eight translations get the same bullets (main session), each measured.

**3.3 "Coming soon to Chrome and Edge browsers" (§4 Q2).** Verified: it is in all nine descriptions; the extension is
"planned and designed, not built" (LISTING.md 23 Oct note; GBrain `projects/sajdatime-extension`). Nothing in this
repo shows it shipped. LISTING.md's own rule: "if it slips far, take the line out: a coming soon left standing for
months reads as a broken promise". By a mid December submit it will have stood eight weeks. Recommend remove, in all
nine languages in one edit, unless Ali says it ships before the submit, in which case the line names where to get it.

**3.4 Wear: unchanged, confirmed.** `git diff main..HEAD -- wear/` touches one file, `WearApp.kt`, comments only
(a doc block about the Kaaba mark's share of the dial; no code line changed). Version stays `1002 / 1.5.0`
(`wear/build.gradle.kts:40,52`, verified). So: no Wear upload (Play refuses a repeated `versionCode` anyway), no
new watch captures (`w1` to `w4` stay), nothing in the Wear release notes field, and the Console walk through says
"leave the Wear track alone" in so many words. The release script still builds the watch bundle as a gate check.

## 4. Questions for Ali (three; he answers on his phone)

1. **Short description** (3.1): **C** "Offline prayer times (namaz), adhan, Qibla and a private salah tracker. Wear OS."
   (Recommended) / A keep the live line / B the plan's line without namaz.
2. **The Chrome and Edge line:** **Remove in all nine languages** (Recommended, unless the extension ships before
   the submit) / Keep as it is / Replace with where to get it (only if it has shipped).
3. **The two freed screenshot slots:** **My prayers and the Timetable sheet, dropping the setup madhab shot and the
   Settings shot** (Recommended) / Keep Settings and drop the Timetable / Keep both old shots and drop the Timetable
   and the Suhoor dialog. He decides the final set from the pictures either way.

## 5. The release checklist: whose decision

| Item | Who | Note |
|---|---|---|
| `versionCode 10`, `versionName 1.6.0`; watch untouched | mechanical | `app/build.gradle.kts:67-68` (verified 9 / 1.5.0 today) |
| `release-1.6.0.sh` from 1.5.0: counts, `notClass` adds `TrackerSeedDeviceTest`, the 6j store shot block, Theme restored, bundle size limit measured | mechanical | the device suite excludes both seeds |
| `set-release-date.sh` on the day (privacy page and the pinned test together) | mechanical, on release day | lesson from 23 Oct |
| What's new in nine languages | mechanical after §2 | reviewer checks religious words |
| LISTING.md, LISTING-LOCALES.md edits (3.2, 3.3) | mechanical after Q1 and Q2 | measured by `len` |
| Screenshots taken, folders, script, README | mechanical | pictures to Ali before upload (Q3 and the final set) |
| Data safety form | **Ali confirms**, expected unchanged | the tracker never leaves the phone (`privacy.html` "Your prayer record", verified present) |
| Merge PR, build bundles, upload, paste, Send for review | **Ali**, one screen at a time | stop before every submit button |
| Release day | **Ali** | 15 Dec is the limit (Q25) |
| After live: health check re-baseline (new screenshot IDs, 1.6.0 What's new, description), HANDOVER §11 and PROJECT_BRIEF "live on", close the §0 progress list | mechanical | |

## 6. Risks

- The Dhuhr tap in §1.2 fails silently on a slow boot and 02 ships with Fajr only: acceptable, not wrong.
- Play's per locale screenshot behaviour (§1.3 assumption) differs from expected: four pictures only in those
  markets; still honest.
- The 4000 limit after 3.2: if the cuts fall short, drop the Hijri bullet's Shia clause first, then "typed in once".

## 7. iPhone effect

None in 1.6 (a Play release). Carries over as practice: one screenshot set per language with no baked in words is
what App Store Connect wants too; the What's new wording works as "What's New" there; the Wear statement maps to an
Apple Watch note only when a watch app exists.

## 8. Verified against assumed

Verified in the repo: the eight current raw files and their lack of captions; both seed tests' contents; the
release script's locale loop, `set_clock`, `tap_text` and `notClass` line; `SkyPeriod` boundaries; `versionCode`
9 / 1002; the watch diff is comments only; the Chrome line in nine descriptions and its "not built" note;
`privacy.html` has "Your prayer record"; the 1.6 strings `nav_tracker` and `action_timetable`; `build-store-assets.sh`
copies the flat folder only. Assumed: Play's per locale screenshot
behaviour; the launcher's dp width on `sajdastore`; the exact rings the seed produces on 15 Aug 2026.

## 9. Plan verifier corrections (25 Oct 2026), all accepted

1. Shot 05 clock moved to 18:30 (above). Blocking, fixed.
2. The seed leaves the method on Automatic at 51.5°N, so Times shows the loud "Does this match your mosque?"
   notice. The new seed method calls `SettingsRepository.dismissMethodNotice()` so 01, 04, 06 and the four localised
   01s show the quiet door, as the 1.5.0 set did by hand. Blocking, fixed.
3. Step 6 added `android.permission.VIBRATE` (normal, no prompt). `release-1.6.0.sh` expects twelve permissions,
   and the Data safety row says the form is unchanged by it. Blocking, fixed.
4. No town chip on the strip on `sajdastore` (above); Ali sees it in the picture (question 4).
5. The Next widget is resized to four columns before shot 05.
6. Qibla numbers are London's: 119° and "Turn right 59°".
7. 6j taps the "My prayers" tab first, and matches the Dhuhr row by prefix (`text="Dhuhr`), since the row may read
   "Dhuhr, 1:05 pm".
8. 1.5.0 is live (public page "Updated on 23 Oct 2026"); HANDOVER §11 and PROJECT_BRIEF record it.
9. The README names languages for the Console ("pick Indonesian"), not codes; folder names stay internal.
10. §5 also has: the `dumpsys alarm` before and after comparison (PLAN.md R1) in the release gate, a
    `RELEASE-1.6.0.md` walk through, and `set-release-date.sh`'s hardcoded "1.5.0" commit message fixed.

Question 4 for Ali: the strip shot without its town chip (recommended, the Next widget beside it says London) or
a wider AVD for that one picture (breaks the one recipe rule kept since 1.4.0).
