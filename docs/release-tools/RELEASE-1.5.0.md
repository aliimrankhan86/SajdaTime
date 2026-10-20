# Releasing 1.5.0: the checklist

Written 18 Oct 2026, when the owner asked for everything to be ready for 1.5.0 and the work parked.
`claude/quiet-glass` is the release branch and holds everything. The plan's dates (PROJECT_BRIEF):
a preparation run on his Mac on Mon 21 Dec 2026, he merges and submits around 4 Jan 2027, live by mid
January, **latest acceptable 25 Jan 2027** (Ramadan 1448 begins Mon 8 Feb 2027). **Changed 19 Oct 2026: he will
submit in NOVEMBER, and that night he chose MONDAY 9 NOVEMBER 2026** (HANDOVER §11, STATE OF PLAY). The preparation
run moves to Mon 2 Nov 2026 and the reminders to match ("Scheduled jobs" below).

Each line says who does it. Claude can do the "Claude" lines from a session on his Mac. Nothing here
is done by touching the signing key, and **he presses every submit, publish and consent button**.

## Starting the Mac session (the owner's handover, 18 Oct 2026)

> **Checked, 18 Oct 2026 (late), by a Claude Code session on his Mac.** The six claims in step 1 were
> checked and the Mac only items in step 2 were done (counts in HANDOVER §11 NEWEST); the results, what was wrong and what was fixed are in `docs/HANDOVER.md`
> §11 NEWEST. Do not redo it. What is left is "Still to do" below. Step 0 too: that same session
> finished, had its diff independently reviewed, and committed and pushed its own work to
> `claude/quiet-glass`; nothing of it is left only on the Mac. The cloud session is closed for good:
> never rely on it or ask him to go back to it.

The cloud session is finished. The owner continues in Claude Code **on his Mac** and will open a new
session and ask it to do the rest. A Mac session reading this: the brief below is yours, and it is
the same one he was given to paste. Results from an earlier Mac session may already be in the shared
memory (GBrain `projects/sajdatime`); `recall` it first and do not redo work it proves done.

0. **First, secure the previous Mac session's work.** On 18 Oct 2026 the owner parked the cloud session
   while a Claude Code session on his Mac was still working, so its results may exist only on the Mac.
   In every local clone it used (at least `~/Developer/SajdaTime-settings-preview`), run `git status`,
   `git stash list` and `git log --oneline origin/claude/quiet-glass..HEAD`. Show him anything
   uncommitted or unpushed, commit and push what belongs on the branch (with his yes), and read its
   final report and the GBrain memory before doing anything else. Do not redo work it proves done.
1. **Treat every claim on this branch as unverified until you have checked it.** Report each as
   confirmed, wrong or not checkable:
   - the branch is level with `origin/claude/quiet-glass` (at `9038d3a` or later), the tree is clean,
     and how far it is ahead of `origin/main`;
   - versions are phone 9 / 1.5.0 and watch 1002 / 1.5.0;
   - the full gate passes (`clean test lint :app:bundleRelease :wear:bundleRelease`), with the test,
     failure and lint counts pasted (the cloud run on 18 Oct: 384 tests, 0 failures, 0 lint errors);
   - `DesignRulesTest` covers buttons, button sizes and spacing: prove one rule by breaking it once,
     then put it back;
   - Turkey (Diyanet) applies Sunrise −7, Dhuhr +5, Asr +4, Maghrib +7: compare Istanbul with Aladhan
     method 13 on the three `RamadanGoldenTest` dates, within 1 minute;
   - the 1.5.0 description and the five What's new texts in `docs/store/LISTING.md` are true of the
     1.5.0 build on the emulator and of `docs/privacy.html`, under 4000 and 500 characters, and the
     description still says "Wear OS".
2. **Then do the Mac only items** in "From the Mac readiness check" below and step 1 of "Still to do":
   the one line widget device test, `release-1.5.0.sh` (run it and fix what it finds; the device suite
   runs through `am instrument` after `grantbind`), the store screenshots, and anything wrong on the
   emulator in light, dark, Urdu or 2x text.
3. **Rules:** do not merge into `main`; do not touch the Play Console or the signing key; never ask him
   to test the watch; when unsure, ask with `AskUserQuestion`, recommendation first, screenshots for
   anything visual; never write "done", "every" or "all" without the counts; get an independent review
   of the diff before committing; follow the commit rule in `CLAUDE.md` (HANDOVER §11 NEWEST,
   `PROJECT_BRIEF.md`, this file) and push to `claude/quiet-glass`; leave the emulator as you found it.
4. **Report to him in plain English:** a table of what was verified, what was wrong and what was fixed;
   what is still not verified; what remains for release day and who does each item; the commit he
   should see. Then stop and wait for him.

## Already done on the branch (18 Oct 2026)

- [x] Every 1.5.0 change merged into `claude/quiet-glass`; full gate green (tests, lint, both bundles).
- [x] Version numbers: phone **9 / 1.5.0**, watch **1002 / 1.5.0** (both ship this time: the watch
      shares the Turkey change and has new translations).
- [x] What's new, final, in five languages, all under 500 characters, plus the watch note:
      `docs/store/LISTING.md`, "At the 1.5.0 release".
- [x] Owner decisions settled: spacing (D5, approved from screenshots), Turkey with Diyanet's minutes
      (HANDOVER §5.26), notification panel line, place wording, the sound rows.
- [x] Privacy policy content already matches 1.5.0 (widget location chip; sounds stay on the phone,
      so nothing new to declare). Data safety answers unchanged.

## Decided by the owner (18 Oct 2026)

- The 1.5.0 **full store description** (`docs/store/LISTING.md`, "1.5.0 full description") and the What's
  new texts: **approved on condition they are accurate** (the owner, 18 Oct 2026: "approved as long as they are accurate"). So before pasting them, a
  session on his Mac checks every line against the 1.5.0 build on the emulator and against
  `docs/privacy.html`, fixes anything that is not true, and tells him what changed.
- The **habit tracker is out of 1.5.0** ("leave it out").
- **All remaining work happens in a Claude Code session on his Mac, not in the cloud** (his words: "we do
  the rest on my mac claude not in cloud one").

## From the Mac readiness check (18 Oct 2026), for a session on his Mac

- [x] **Fix the one line device test** (18 Oct 2026: `DeviceSupport.alarmTimes` now matches the owning
      package exactly; with the old line `WidgetDeviceTest` failed 2 of 4 beside the sideload copy, with
      the fix 4 of 4 pass). The check found it failing (test code only; its notes are in that
  session's results, saved to the shared memory). Without it the 21 Dec preparation run reports a false
  failure.
- [x] **Write `docs/release-tools/release-1.5.0.sh`** (18 Oct 2026; written and run four times: every
      line PASS or TAKEN apart from tree CHECK (uncommitted work) each time, push FAIL in run 2 (the
      cloud's `deb0877`, since merged) and phone SKIPPED (his choice); the independent review then found
      a random false "no signed bundle" and it was fixed, HANDOVER §15 lesson 151; it found that a bare `installRtl` puts the watch app on the
      phone, fixed, HANDOVER §15 lesson 149). From the 1.4.0 script and run it. What must change:
  versionCode 9 and versionName 1.5.0 (line 98 to 99 of the 1.4.0 script); the bundle size check (it
  allows 300 KB over 1.3.2, but the two built in adhans alone add about 3.5 MB: measure the 1.5.0 bundle
  and set the limit from that); the watch: 1.4.0 expected it unchanged, 1.5.0 builds and checks the watch
  bundle too (1002 / 1.5.0); the branch is `claude/quiet-glass`; drop the 1.4.0 planning copy stash step;
  check the permission list still matches (no new permission was added in 1.5.0).
- [x] **Store screenshots** (18 Oct 2026): six phone screenshots retaken from the 1.5.0 build, the Ramadan
      one as `03` and a widgets one as `06` (owner's choice); the watch ones did not change and stay
      (`docs/store/LISTING.md`, "What the six phone screenshots show"). Was: retake the five phone
      screenshots from the 1.5.0 build plus the planned Ramadan one, and the watch ones if they changed.
- **Data safety:** the check's reading is that no answer changes (the language sent with a typed place
  name is already in the policy). The answers are his to confirm in the Console.
- **Release notes languages:** check in the Console whether Play shows the Urdu, Turkish, Indonesian and
  Arabic What's new when only the English store listing exists. If it does not, either add translated
  listings (`docs/store/LISTING-LOCALES.md`) or ship the English note only; his choice.

## FIRST, before release day: bring the unmerged 8 Oct work into 1.5.0 (owner's decision, 18 Oct 2026 late)

> **DONE, 19 Oct 2026, by a Claude Code session on his Mac.** Audited feature by feature and shown to Ali
> before anything changed; he approved all ten missing items and left out the About row "made with help from
> AI". Brought in: "Phone language" first and the default; a language list on Android 7 to 12
> (`AppLocale.setOverride`); the language list on the welcome screen; Match your mosque from the mosque's
> board ("Use your mosque's board", then the prayer, then a clock; a time over 30 minutes is refused and
> explained); the PDF right to left in Urdu and Arabic; the watch tile's language; "The full privacy policy
> is in English." under the translated usage question; the missing translation fixes in Urdu, Turkish,
> Indonesian and the same ones in Arabic; `TranslationIntegrityTest` and `tools/check-translation.py` with
> Arabic; and the records (translation reviews, the method finder sweep and its decision, the old listing
> drafts marked as history). **The "Phone language" bug was reproduced on a new Android 12 emulator
> (`sajda12`) and fixed** (HANDOVER §15 lesson 152), and running it found four more of the same family, all
> fixed (lessons 153 and 157). Ali chose both new looks from screenshots (DESIGN_SYSTEM §9). What's new
> (five languages) and one description line now mention the mosque board (LISTING.md); store screenshot 05
> retaken. Results, what was and was not verified: HANDOVER §11, STATE OF PLAY 19 Oct 2026. Neither old
> branch was deleted; `claude/analytics-default-on-1-4-0` was not merged.

**Why.** After the Mac check (`5143282`), the cloud session compared every remote branch with
`claude/quiet-glass` and `main`. All 31 of its own commits are in the release. Two older branches are
not, and the owner said: "I don't want anything to be lost in any branch ... all merged then that gets
added to release 1.5 fully tested". Asked how, he chose **audit, then merge into 1.5.0**, done by
**Claude Code on his Mac** ("I don't want to continue cloud session"), not a blind merge.

**Branch 1: `claude/customer-review-pain-points-9d35kl`** (8 Oct 2026, 13 commits, none
patch equivalent to anything in the release; `git cherry` shows all 13 as `+`). 1.3.2 was shipped from
a different branch (`claude/1-3-2-language-dnd`, PR #9), so this one was never merged. Its own state
block is in its copy of `docs/HANDOVER.md` ("STATE OF PLAY — 8 Oct 2026, evening"). Of its 51 changed
files, **26 exist in the release in a different form** and **25 exist nowhere in the release**:
- code: `app/.../ui/settings/LanguageChooser.kt` (the "Phone language" first option and the picker below
  Android 13 via `AppLocale.setOverride`; the release has neither `language_phone` nor `setOverride`),
  `core/.../AdjustmentFit.kt` with `AdjustmentFitTest` and `MethodFinderSweepTest` (the method finder
  groundwork; **the method finder itself was decided NOT built**, see its sweep file);
- tests and tools: `TranslationIntegrityTest`, `TranslationScopeTest`, `tools/check-translation.py`;
- docs: `docs/reviews/method-finder-sweep-2026-10-08.txt`, `docs/store/LISTING-ID-TR-UR.md`, and 16
  files in `docs/translation/` (the brief, back translations, notes and the independent reviews of the
  shipped Indonesian, Turkish and Urdu, including the nine "quiet is not silent" corrections).
- Its other features (the mosque time typing, "0 min", banner actions as real buttons, the Urdu PDF
  mirroring) may already be in the release under new names after the Quiet Glass redesign
  (`NoticeCard` is gone; `PermissionCard` exists). Check each by behaviour, not by name.

**How (the Mac session):**
1. Go through the branch feature by feature and file by file. For each, say: already in the release,
   replaced on purpose (cite the HANDOVER line), or missing. Show Ali the list before changing anything.
2. Bring in what is missing, on top of `claude/quiet-glass`, adapted to today's code and the Quiet Glass
   design (`docs/DESIGN_SYSTEM.md`), never by overwriting newer files with the 8 Oct versions. Arabic
   (`values-ar`) did not exist on 8 Oct: anything added to the other languages needs Arabic too. Keep the
   decision that the method finder is not built; keeping its research files is fine.
3. **Known bug to fix first:** on an Android 12 emulator, choosing "Phone language" while the app was in
   Urdu once showed English words laid out right to left, which `CLAUDE.md` forbids. Reproduce it below
   Android 13 and fix it before the option ships.
4. Then everything 1.5.0 passed must pass again: the full gate, `release-1.5.0.sh` (device suite), checks
   in light, dark, Urdu, Arabic and 2x text, and the screenshots retaken if a screen changed. If the
   app changed, update the What's new texts (still under 500 characters) and recheck the description.
5. Independent review of the diff, commit by the `CLAUDE.md` rule, push to `claude/quiet-glass`.
6. **Do not delete either old branch.** They stay on GitHub as the record, so nothing is lost.

**Branch 2: `claude/analytics-default-on-1-4-0`** (3 Oct 2026, 1 commit): usage counts on by default.
**Never merge it** (owner, 18 Oct 2026 late: keep it, never merge). He reversed default-on on 4 Oct, and
`CLAUDE.md` forbids rebuilding it without new evidence. It stays on GitHub as history.

**Checked by the cloud on `5143282` before this:** the gate on Linux, 384 tests, 0 failures (core 120,
app 252, wear 12); the description 3969 characters and containing "Wear OS"; the What's new texts 495,
394, 490, 421 and 311 and the watch note 147, each measured; versions 9 / 1.5.0 and 1002 / 1.5.0;
`main` untouched; privacy policy still dated 17 October 2026.

## Still to do, in order

Dates (his choice, 19 Oct 2026 late): a reminder on Sun 1 Nov 2026 (18:00), the preparation job on Mon 2 Nov 2026
(12:00 noon), a submit day reminder on Mon 9 Nov 2026 (09:00), submit on **Mon 9 Nov 2026**, **latest acceptable 25 Jan
2027** (Ramadan 1448 begins Mon 8 Feb 2027). The January plan (20 Dec, 21 Dec, 4 Jan) is replaced.

0. **Waiting on Google (19 Oct 2026):** the description for the live 1.4.0 was submitted (`docs/store/LISTING.md`,
   "For the live 1.4.0, SUBMITTED 19 Oct 2026"). Live when the public page's description says "In English, Urdu,
   Turkish and Indonesian" and no longer says "Your coordinates never leave your device". The daily review replies
   job tells him once when it sees it, and the preparation job (Mon 2 Nov) checks it too. **Claude:** then change that heading to
   LIVE with the date, and update GBrain.
0b. **Store screenshots and description must match the final app** (owner, 20 Oct 2026: "we need to update the screenshots in the playstore and description to match the features of our app"). The six
   phone screenshots were taken on 18 Oct and the morning of 19 Oct, before that evening's Ramadan window and widget
   theme changes. **The preparation job (Mon 2 Nov) compares each one, and the "1.5.0 listing, FINAL" description
   and What's new texts in `docs/store/LISTING.md`, with the final build;** it retakes any screenshot that no longer
   matches, exactly as LISTING.md "What the six phone screenshots show" says, and corrects any wrong fact. Wording
   beyond a fact correction goes in as PROPOSED, for his yes. On release day Claude shows him the six screenshots and
   any changed text before he uploads them.
1. **Claude:** run `docs/release-tools/release-1.5.0.sh` again and expect every line PASS or TAKEN
   (phone SKIPPED is his choice). The preparation job (Mon 2 Nov 2026, was 21 Dec) does this (its prompt was rewritten on
   18 Oct 2026 late to use this script, never set the versions again, and stop before any merge, pull
   request, submit or Console step). The last run, 19 Oct 2026 after the emulator checks and their fixes, gave
   406 unit tests (app 261, core 129, wear 16; 407 with core 130 since the evening's `AdjustmentFit` test, gate only), 0 failed, lint 0 errors, a device suite of 25 of 25 straight after a cold boot, both bundles 9 / 1.5.0 and 1002 / 1.5.0 signed CN=Ali Imran Khan, 22 of 24 lines PASS or TAKEN (tree CHECK, the uncommitted work, and phone SKIPPED); the job's prompt says so, and also re-runs `LanguageAndDndDeviceTest` on the Android 12 emulator
   `sajda12` (7 of 7 on 19 Oct), because the script runs on Android 16 only and the language code below 13 is new.
   **DONE, PASS, 19 Oct 2026 (`d07a2d9`, HANDOVER §10): the tile showed the new zone's time about 2 seconds after the
   change, without opening the app. Do not repeat it.** What was run, for the record (`ClockChangeReceiver`): add the tile, note the prayer time it shows, turn automatic time zone off (`adb shell settings put
   global auto_time_zone 0`, or it can put the zone straight back), change the watch's time zone (`adb shell cmd
   alarm set-timezone Europe/Istanbul`; if the watch refuses that command, its Settings, Date and time), and check the tile shows the new zone's time within a few
   seconds without opening the app; then put the zone back and turn automatic time zone on again. Record the result in HANDOVER §10. The gate after that
   change: 409 unit tests (app 261, core 130, wear 18), so the job's expected count moves from 407 to 409: its prompt on the Mac may still say 407, and 409 is correct, not a fault.
   **DONE, PASS, 19 Oct 2026, 22:00 UK, both the Ramadan check and the widget theme check below (HANDOVER §10,
   top entry; screenshots sent to him). Do not repeat them.** What was asked, for the record:
   **Also, once, for the Ramadan change of 19 Oct 2026 late (built in a cloud session, no emulator):** on the
   phone emulator with automatic date off, set the date to 4 February 2027 (4 days before Ramadan 1448): no
   Ramadan notice on Times, no "Ramadan card" or "Suhoor reminder" in Settings. Then 5 February (3 days before):
   the notice shows with the new line "From the first day, the Ramadan card shows suhoor and iftar times here.
   You can turn it off in Settings." and both rows are in Settings. Then 8 March (the last day): rows there;
   9 March (1 Shawwal): rows gone. Look at the notice in Urdu and Arabic and at 2.0 text. **The widgets too (the
   owner, 19 Oct 2026: "Also widget should be there"):** place both widgets on the home screen and screenshot them
   on each of those dates plus the evening of 7 February (the eve: the next prayer, tomorrow's Fajr, says "Suhoor
   ends") and day 1 (Fajr "Suhoor ends", Maghrib "Iftar"); on 9 March the words are gone. Send him the widget and
   notice screenshots. Put the date and
   automatic date back. Record in HANDOVER §10. Unit tests after that change: **410** (app 261, core 131, wear 18).
   **And once for the widget theme, also built in the cloud on 19 Oct 2026 late (the owner: "If the user
   selected dark then widget should be dark, if user choose light then the widget should be light"):** run
   `adb shell appwidget grantbind --package com.sajdatime.app --user 0`, then
   `adb shell am instrument -w -e class 'com.sajdatime.app.WidgetDeviceTest#the_widgets_follow_the_apps_theme_and_return_to_the_phone_under_system' com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`
   (debug build installed; it must print OK). Pull the six renders from
   `/sdcard/Android/data/com.sajdatime.app/files/device-renders/` (`widget-today-theme-*` and
   `widget-next-theme-*`). Then by hand with both widgets on the home screen, the emulator in light mode: in the
   app, Settings, Theme, Dark: both widgets turn dark when you go back to the home screen; Light: light; Follow
   phone: light; now switch the emulator to dark mode: the widgets follow it. Screenshot each and **send him the
   widget screenshots** (he asked to see them). Then put Theme back to Follow phone and the emulator back to
   light. Record in HANDOVER §10. Unit tests after this change: **416** (app 267, core 131, wear 18); the
   preparation job's expected count moves from 410 to 416, and 416 is correct, not a fault.
2. **Claude, on the day:** set "Last updated" in `docs/privacy.html` to that day, then commit and push to
   `claude/quiet-glass`.
3. **Ali:** merge `claude/quiet-glass` into `main` **that same day, not before**, because merging
   publishes `docs/`, including the privacy policy, through GitHub Pages (HANDOVER §15 lessons 124
   and 137).
4. **Ali, on his Mac:** build both signed bundles fresh from `main` (never upload a saved file):
   `export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean :app:bundleRelease :wear:bundleRelease`.
5. **Ali in the Play Console, with Claude walking him through it one screen at a time:**
   - upload both bundles: production phone `app-release.aab` (9 / 1.5.0), Wear OS track
     `wear-release.aab` (1002 / 1.5.0);
   - paste the five What's new texts (English, Urdu, Turkish, Indonesian, Arabic), and from `docs/store/LISTING.md`
     "1.5.0 listing, FINAL" (19 Oct 2026): the watch note, the full description and, **with his yes**, the new
     short description (then save, reload and search the page for "price or promotion", as LISTING.md says); check
     the listing still says "Wear OS";
   - add the eight translated listings (Arabic, Urdu, Turkish, Indonesian, Malay, Bengali, French, Hindi) from
     `docs/store/LISTING-LOCALES.md`, "1.5.0" (Grow, Store presence, Store listings, Add translations; title, short
     and full description each; the screenshots are shared). This also settles which language a user sees the
     What's new in, because each of the app's four other languages then has its own listing;
   - replace the phone screenshots with the six new ones in `docs/store/upload/phone/` (checked, or retaken, by the
     2 Nov job, step 0b; show him them first), in filename order
     (the old `03-times-dark` goes; the two Wear OS screenshots stay);
   - confirm the Data safety answers (expected unchanged);
   - then **he presses Send for review himself**.
6. **After it is live** (watch Play's "Updated on" date move; Play sends no email):
   - Claude drafts the reply to the Arabic reviewer who asked for Arabic and Ali posts it (never ask
     for stars);
   - (the January Ramadan lines are no longer a separate step: the 1.5.0 description and the translations carry
     Ramadan, so pasting them on release day does it);
   - Ali deletes the spare copy of the signing values in `~/.gradle` (he gets a reminder);
   - record the release in `docs/HANDOVER.md` §11 and `docs/PROJECT_BRIEF.md`;
   - **Claude re-baselines the daily health check** (`trig_0129FQnWihf8XG6rKLCB4oFr`) to 1.5.0: until then it
     reports "1.5.0 is live" every day, on purpose. The new baseline is the 1.5.0 "Updated on", What's new,
     the six new phone screenshot IDs (the two watch ones stay), the privacy policy's new date, and the 1.5.0
     description, which no longer has the words "chosen automatically for your school" that its check 3
     looks for.

## Open items, none of which blocks 1.5.0

- **Found on the emulator on 19 Oct 2026, small, after 1.5.0 if he agrees:** a typed place in Urdu or Arabic is
  joined with a Latin comma ("کراچی, پاکستان"; the language's own "،" would need the two look ups and the widget's town
  split changed together); on Google's Pixel sound picker, once another sound is chosen there is no "Default" card to
  go back to the phone's usual tone (only that tone by name), so an app row "Your phone's usual tone" would help; the
  watch tile keeps its old language after a per-app language switch until its service is made again (a change of the
  watch's own language was run on the evening of 19 Oct 2026 and the tile followed, HANDOVER §10). Accepted, not to be "fixed": "Reset all" in Match your mosque lets TalkBack read
  each row that changed.

- **Turkey:** against Diyanet's own Istanbul page (18 to 24 Oct 2026) Maghrib is 1 minute early on 7 of 7
  days. Check other Turkish cities after 1.5.0 before changing anything (a blind +1 moves every Turkish
  city). Asr is 2 minutes off Aladhan method 13 because of the two calculation libraries' Asr formula,
  not Turkey: London on Muslim World League shows the same gap. HANDOVER §5.26.
- **"Tap to read":** the Settings line "A helper, not a religious authority. Tap to read."
  (`about_disclaimer_short`) breaks the no "Tap to" copy rule. Live since 1.4.0; fix it after 1.5.0 if
  he agrees.
- **A private claude.ai page:** the cloud session accidentally published a copy of this file as a private
  page (claude.ai/artifact/GxU35nSJAfxqedU7DYa35L). Only Ali can see it; he can delete it if he wants.

## Scheduled jobs (all six checked and brought up to date on 19 Oct 2026)

None of these is in the repository: the local tasks' prompts are in `~/.claude/scheduled-tasks/<id>/SKILL.md` on
his Mac (they need the Mac on and the Claude app open), the routines are at https://claude.ai/code/routines. If a
fact here changes, change the job too: nothing updates it automatically.

| Job | Kind and time | What it does | Changed on 19 Oct 2026 |
|---|---|---|---|
| SajdaTime app health check, `trig_0129FQnWihf8XG6rKLCB4oFr` | cloud routine, daily 12:00 UTC | Watches the public store page; silent unless something changed | **Re-baselined to 1.4.0.** It still had 1.3.2 and on 18 Oct reported 1.4.0 as a release it did not know about. Now it also knows the description change in review (either text is fine), expects 1.5.0 in January and says so as good news, and on 18 and 22 Jan 2027 only warns if 1.5.0 is still not live. **Moved, 19 Oct late:** it expects 1.5.0 from Mon 9 Nov 2026, and warns only on 16 Nov, 23 Nov 2026 and 18 Jan 2027 if it is still not live **20 Oct:** its reminder on those three dates now allows for 1.5.0 being in Google's review (owner: by 23 Nov "it should be gone live or in the process of it"): if he sent it, look at Publishing overview; if not, ask Claude to help submit. It cannot see the Console, so it never says which. Read back exactly |
| SajdaTime review replies, `sajdatime-review-replies` | local task, daily 12:40 | Drafts replies to new reviews (he posts them); the Arabic reply and spare signing file reminders | **Added:** a one time notice when the new description is live (file `build/review-replies/description-live.txt` in `~/Developer/SajdaTime`), one notice on 26 Oct 2026 only if it is still not live; after 1.5.0 replies may mention "Phone language" and "Use your mosque's board" |
| SajdaTime monthly numbers, `trig_01AzcQUNWSkgGmdJmNXkaQoD` | routine run in his Chrome, days 2 to 9 at 11:51 UK | Play numbers into the "SajdaTime monthly numbers" Claude Doc | Nothing: it does not depend on a version |
| Leave the Mac on, `trig_01Bpzq4t8wJCQZwABsC53ys8` | cloud routine, once, **Sun 1 Nov 2026 18:00** (was 20 Dec) | Push reminder | **Corrected:** it said the job "opens the pull request"; it has not done that since 18 Oct. **Moved, 19 Oct late:** to 1 Nov, text now names Mon 2 Nov and the 9 Nov submit |
| SajdaTime 1.5.0: prepare the release, `sajdatime-1-5-0-reminder` | local task, once, **Mon 2 Nov 2026 12:00 noon UK** (moved 20 Oct 2026 by him in the Claude app; was Mon 21 Dec 2026 10:00) | Re-runs `release-1.5.0.sh` and `tools/wear-verify.sh`, fixes and records, stops before any merge or Console step | **Added:** the 19 Oct expected counts, `LanguageAndDndDeviceTest` on `sajda12`, the description check; its "never raise Android 7 to 13" line now says 7 to 11. **Later on 19 Oct:** counts updated to the final run (406 unit tests, device suite 25, `LanguageAndDndDeviceTest` 7). **Evening, 19 Oct:** its Android 12 step gives up after 5 minutes if `sajda12` does not boot (it hung at start that evening, HANDOVER §10); the unit test count is now 407. **Late, 19 Oct:** the expected unit test count is now 409 (app 261, core 130, wear 18), and it says the watch tile time zone check is done (PASS, `d07a2d9`) and not to run it again. **19 Oct, 22:30:** its prompt now names the 9 Nov submit, expects 416 unit tests (app 267, core 131, wear 18) and a device suite of 26, and says the Ramadan and widget theme checks are done (PASS, `79af839`). **Its time could not be moved from a terminal session:** the Claude app keeps local task times in memory and writes them back (`persistToDisk`), so editing its `scheduled-tasks.json` would be lost; only the app's own Scheduled screen, or a session inside the app (it has `update_scheduled_task`), can move it. **20 Oct 2026: moved** by him in the app, to Mon 2 Nov 12:00 noon (not the 10:00 asked; noon works as well, so the records and the 1 Nov reminder were matched to it). Read back from its `scheduled-tasks.json`: on, once, same instructions |
| SajdaTime: submit 1.5.0 today, `trig_01VexqUd1cJz8jfD5QFT874W` | cloud routine, once, **Mon 9 Nov 2026 09:00** (was 4 Jan 2027) | Push reminder of the submit day and the 25 Jan limit | **New.** Replies with fixed text only (Read tool only; the account's default connectors got attached and are unused). **Moved, 19 Oct late:** to 9 Nov, from a Mac session (a cloud session could not edit it), text now says Monday 9 November |

## Not verified yet (say so, do not assume)

**Done on 19 Oct 2026, later the same day (HANDOVER §10, top entry):** TalkBack on every new control (heard through
its log), Turkish and Indonesian at 2.0 text, the clock in landscape and at 320dp, the place look up and channel names
in Urdu on Android 12, the watch tile in Urdu and Arabic, the large Today widget in Urdu and Arabic, the sound picker's
"Default" card. Six faults found and fixed on the way (TalkBack silent on plus and minus; words split at large text;
wrapped button labels off centre; the clock in landscape; "Prayer alarm" left in English below 13; the tile's frozen
countdown). Run on the evening of 19 Oct 2026 (HANDOVER §10): a change of the watch's own language (the tile followed) and the
look up from the phone's location on Android 16; still not run below 13, because the `sajda12` emulator no longer starts. A real phone is not needed (the owner, 17 Oct 2026: emulator
testing is the standard).

A real phone (not needed, above); Android 7 to 11 (his choice; Android 12 was run on an emulator on 19 Oct 2026 for the
language list, its right to left switching and the PDFs only); the phone sound picker's "Default" row; Google's review card
(it only appears on a copy installed from Play); whether Play shows the translated What's new without
translated listings (his choice if not); Turkish cities other than Istanbul; what opening the notification
panel does to an Alarm; the watch spacing (not converted to the scale, on purpose); the large Today
widget's Hijri autosize on Android 7 (read, not run: it falls back to "…") and in Urdu and Arabic; a
launcher other than Pixel Launcher.
