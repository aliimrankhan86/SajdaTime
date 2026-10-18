# Releasing 1.5.0: the checklist

Written 18 Oct 2026, when the owner asked for everything to be ready for 1.5.0 and the work parked.
`claude/quiet-glass` is the release branch and holds everything. The plan's dates (PROJECT_BRIEF):
a preparation run on his Mac on Mon 21 Dec 2026, he merges and submits around 4 Jan 2027, live by mid
January, **latest acceptable 25 Jan 2027** (Ramadan 1448 begins Mon 8 Feb 2027).

Each line says who does it. Claude can do the "Claude" lines from a session on his Mac. Nothing here
is done by touching the signing key, and **he presses every submit, publish and consent button**.

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
