# Releasing 1.5.0: the checklist

Written 18 Oct 2026, when the owner asked for everything to be ready for 1.5.0 and the work parked.
`claude/quiet-glass` is the release branch and holds everything. The plan's dates (PROJECT_BRIEF):
a preparation run on his Mac on Mon 21 Dec 2026, he merges and submits around 4 Jan 2027, live by mid
January, **latest acceptable 25 Jan 2027** (Ramadan 1448 begins Mon 8 Feb 2027).

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

## Still to do, in order

Dates: a reminder on Sun 20 Dec 2026 (18:00), the preparation job on Mon 21 Dec 2026 (10:00), submit
around **4 Jan 2027**, live by mid January, **latest acceptable 25 Jan 2027** (Ramadan 1448 begins
Mon 8 Feb 2027).

1. **Claude:** run `docs/release-tools/release-1.5.0.sh` again and expect every line PASS or TAKEN
   (phone SKIPPED is his choice). The 21 Dec preparation job does this (its prompt was rewritten on
   18 Oct 2026 late to use this script, never set the versions again, and stop before any merge, pull
   request, submit or Console step).
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
   - paste the five What's new texts (English, Urdu, Turkish, Indonesian, Arabic), the watch note and the
     checked description (`docs/store/LISTING.md`), and check the listing still says "Wear OS";
   - replace the phone screenshots with the six new ones in `docs/store/upload/phone/`, in filename order
     (the old `03-times-dark` goes; the two Wear OS screenshots stay);
   - confirm the Data safety answers (expected unchanged);
   - check whether Play shows the translated release notes when only the English listing exists; if not,
     his choice: add translated listings (`docs/store/LISTING-LOCALES.md`) or ship the English note only;
   - then **he presses Send for review himself**.
6. **After it is live** (watch Play's "Updated on" date move; Play sends no email):
   - Claude drafts the reply to the Arabic reviewer who asked for Arabic and Ali posts it (never ask
     for stars);
   - in January, add the Ramadan lines to the store listing (`docs/store/LISTING.md`);
   - Ali deletes the spare copy of the signing values in `~/.gradle` (he gets a reminder);
   - record the release in `docs/HANDOVER.md` §11 and `docs/PROJECT_BRIEF.md`.

## Open items, none of which blocks 1.5.0

- **Turkey:** against Diyanet's own Istanbul page (18 to 24 Oct 2026) Maghrib is 1 minute early on 7 of 7
  days. Check other Turkish cities after 1.5.0 before changing anything (a blind +1 moves every Turkish
  city). Asr is 2 minutes off Aladhan method 13 because of the two calculation libraries' Asr formula,
  not Turkey: London on Muslim World League shows the same gap. HANDOVER §5.26.
- **"Tap to read":** the Settings line "A helper, not a religious authority. Tap to read."
  (`about_disclaimer_short`) breaks the no "Tap to" copy rule. Live since 1.4.0; fix it after 1.5.0 if
  he agrees.
- **A private claude.ai page:** the cloud session accidentally published a copy of this file as a private
  page (claude.ai/artifact/GxU35nSJAfxqedU7DYa35L). Only Ali can see it; he can delete it if he wants.

## Not verified yet (say so, do not assume)

A real phone; Android 7 to 13 (his choice); the phone sound picker's "Default" row; Google's review card
(it only appears on a copy installed from Play); whether Play shows the translated What's new without
translated listings (his choice if not); Turkish cities other than Istanbul; what opening the notification
panel does to an Alarm; the watch spacing (not converted to the scale, on purpose); the large Today
widget's Hijri autosize on Android 7 (read, not run: it falls back to "…") and in Urdu and Arabic; a
launcher other than Pixel Launcher.
