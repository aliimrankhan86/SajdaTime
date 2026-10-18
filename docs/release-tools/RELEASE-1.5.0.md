# Releasing 1.5.0: the checklist

Written 18 Oct 2026, when the owner asked for everything to be ready for 1.5.0 and the work parked.
`claude/quiet-glass` is the release branch and holds everything. The plan's dates (PROJECT_BRIEF):
a preparation run on his Mac on Mon 21 Dec 2026, he merges and submits around 4 Jan 2027, live by mid
January, **latest acceptable 25 Jan 2027** (Ramadan 1448 begins Mon 8 Feb 2027).

Each line says who does it. Claude can do the "Claude" lines from a session on his Mac. Nothing here
is done by touching the signing key, and **he presses every submit, publish and consent button**.

## Starting the Mac session (the owner's handover, 18 Oct 2026)

The cloud session is finished. The owner continues in Claude Code **on his Mac** and will open a new
session and ask it to do the rest. A Mac session reading this: the brief below is yours, and it is
the same one he was given to paste. Results from an earlier Mac session may already be in the shared
memory (GBrain `projects/sajdatime`); `recall` it first and do not redo work it proves done.

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

- **Fix the one line device test** the check found failing (test code only; its notes are in that
  session's results, saved to the shared memory). Without it the 21 Dec preparation run reports a false
  failure.
- **Write `docs/release-tools/release-1.5.0.sh`** from the 1.4.0 script and run it. What must change:
  versionCode 9 and versionName 1.5.0 (line 98 to 99 of the 1.4.0 script); the bundle size check (it
  allows 300 KB over 1.3.2, but the two built in adhans alone add about 3.5 MB: measure the 1.5.0 bundle
  and set the limit from that); the watch: 1.4.0 expected it unchanged, 1.5.0 builds and checks the watch
  bundle too (1002 / 1.5.0); the branch is `claude/quiet-glass`; drop the 1.4.0 planning copy stash step;
  check the permission list still matches (no new permission was added in 1.5.0).
- **Store screenshots**: retake the five phone screenshots from the 1.5.0 build (the old design is
  still in them) plus the planned Ramadan one, and the watch ones if they changed.
- **Data safety:** the check's reading is that no answer changes (the language sent with a typed place
  name is already in the policy). The answers are his to confirm in the Console.
- **Release notes languages:** check in the Console whether Play shows the Urdu, Turkish, Indonesian and
  Arabic What's new when only the English store listing exists. If it does not, either add translated
  listings (`docs/store/LISTING-LOCALES.md`) or ship the English note only; his choice.

## Still to do, in order

1. **Claude, any time before the release, on his Mac:**
   - Run the full gate and the on device suite on the emulator, the way
     `docs/release-tools/release-1.4.0.sh` does (widget `grantbind`, `am instrument`, the Ramadan clock
     walk). Fix anything it finds.
   - Compare Istanbul with Aladhan method 13 for the three dates in `RamadanGoldenTest` (blocked from
     the cloud on 18 Oct 2026). They should agree within a minute.
   - Retake the store screenshots from the 1.5.0 build (the design changed): `docs/store/screenshots/`
     and `docs/store/upload/`, phone and watch.
2. **Claude, on release day, before merging:** set the date in `docs/privacy.html` ("Last updated:")
   to that day, commit and push to `claude/quiet-glass`.
3. **The owner:** merge `claude/quiet-glass` into `main` **on the day he submits, not before**:
   merging publishes `docs/`, including the privacy policy, through GitHub Pages (HANDOVER §15
   lessons 124 and 137).
4. **The owner, on his Mac:** build both signed bundles fresh from `main` (never upload a saved file):
   `export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean :app:bundleRelease :wear:bundleRelease`.
5. **The owner in Play Console, with Claude walking him through one screen at a time:**
   - Production, phone: upload `app-release.aab` (9 / 1.5.0), paste the five What's new texts.
   - Wear OS track: upload `wear-release.aab` (1002 / 1.5.0), paste the watch note.
   - Upload the new screenshots. Check the listing still contains "Wear OS".
   - He presses **Send for review** himself.
6. **After it is live** (Play's "Updated on" date moves; Play sends no email):
   - Reply to the Arabic reviewer who asked for Arabic (PROJECT_BRIEF lists it; Claude drafts it, he
     posts it; never ask for stars).
   - In January, add the Ramadan lines to the store listing (`docs/store/LISTING.md`).
   - He deletes the spare copy of the signing values in `~/.gradle` (he gets a reminder).
   - Record the release in `docs/HANDOVER.md` §11 and `docs/PROJECT_BRIEF.md`.

## Not verified yet (say so, do not assume)

A real phone; Android 7; the phone sound picker's "Default" row; what opening the notification panel
does to an Alarm; the watch spacing (not converted to the scale, on purpose).
