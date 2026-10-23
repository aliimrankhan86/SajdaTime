# CHECKPOINT, 19 Oct 2026 (evening)

A dated record of one session's work on `claude/quiet-glass`. It is a snapshot, not a source of truth:
`docs/HANDOVER.md` §11 (STATE OF PLAY) and §10 (top entry) carry the same facts and win if they ever disagree.

- **Branch:** `claude/quiet-glass`, pushed. The work is commit `eb3caa3` (on top of `12b8d3a`); this file is the
  commit after it. `main` was not touched. Nothing was merged, submitted or changed in the Play Console.
- **Live on Play (unchanged):** phone 1.4.0, watch 1.2.0. 1.5.0 moves to November, date not yet chosen (HANDOVER §11, 19 Oct 2026 late).
- **Added later, 19 Oct 2026 late (Mac session, one check only):** the cloud commit `742389f`
  (`ClockChangeReceiver`) was fast forwarded in and its watch emulator check run. **PASS:** with the watch app
  never opened, a zone change from Istanbul to London moved the tile from Fajr 5:38 am to 3:38 am within about
  2 seconds, countdown unchanged; zone put back. Details: HANDOVER §10 top entry. Nothing else was changed.

## The brief

1. Apply items 1, 4 and 5 from the read-only review (GBrain fact 1820), and the store listing count edit.
2. Run the full test gate.
3. Emulator checks: the 12 hour clock, the watch tile, the place name from the phone's location on Android 12,
   and changing the watch's language.
4. Independent review of the diff, commit, push.
5. This file.

Two earlier sessions the same day were blocked by permission mode and changed nothing (GBrain facts 1820, 1821).

## Done

| Item | What changed | How it was checked |
|---|---|---|
| 1. 12 hour clock | `AdjustmentFit.fit` takes whichever half of the day is nearer, then applies the ±30 limit | New test failed on the old code (1 of 9), passes now (9 of 9); emulator before and after (below) |
| 4. Stale comments | `CityLookup.kt` (the geocoder may ask Google; Open-Meteo gets the name and the app language); manifest widget comment (verified, no longer assumed) | Read against `docs/privacy.html` and the code by the independent review |
| 5. Project brief | "Next step" no longer says the 8 Oct audit is pending | Read |
| Listing | English 1.5.0 Qibla line: "Qibla direction: a compass corrected to true north…" | **3987 / 4000** by Python `len` (was 3976); recounted by the reviewer |
| Gate | `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`, run twice, the second time on the final code | 407 unit tests (app 261, core 130 with 1 opt-in skip, wear 16), 0 failed, all test tasks executed; lint 0 errors in all three; bundles 9,583,601 and 3,657,983 bytes, both signed CN=Ali Imran Khan |
| Review | A separate read-only agent reviewed the whole diff | No code defect. 3 must-fix and 3 should-fix items in docs and comments, all fixed before the commit; one new finding (time zone, below) |
| Records | HANDOVER §5.17, §10, §11, lessons 165 to 168; PROJECT_BRIEF; RELEASE-1.5.0.md; LISTING.md | |

## Emulator checks

- **12 hour clock (Android 16, sideload build of `eb3caa3`):** the dial opened on Asr 11:58 p.m. (Mountain View,
  shown in London time). Tapping 12 then 05 showed **12:05 p.m.**, so Material keeps p.m. when the hand passes 12.
  The review's guess is confirmed. With the fix: **+7 min** stored, and the Times screen showed Asr 12:05 am.
  1:05 a.m. (67 minutes out) was still refused with the limit note.
- **Watch tile against the watch app:** four minute changes, each **18 to 33 s late**. The watch app changed on the
  minute. The delay is the watch's own renderer; the tile's entries are right. **Not fixed** (owner's call).
- **Changing the watch's own language:** the emulator's Settings won't do it (the watch follows the phone).
  It was done at runtime with the same call Wear OS uses (lesson 167). English to Urdu to Arabic and back: the
  tile followed every time, Arabic with Western digits.
- **Place from the phone's location:** **not run on Android 12.** The `sajda12` emulator now hangs before its
  kernel starts under every start option tried (snapshot, cold, read-only, software graphics, no window, wiped
  data, alone). Run on **Android 16** instead: a real fix gave "London, United Kingdom", and with the app in
  Urdu it gave "Birmingham, سلطنت متحدہ". The below-13 route is still read, not run.

Everything changed on the emulators was put back: 12/24 setting, adjustments, app language, location (Mountain
View), test location provider, watch language (en-US), stay awake, the helper file. One exception: the watch's
time zone moved from Europe/Istanbul to Europe/London by itself during the run, and it was left on London.

## Not done, and why

- **Device suite and `release-1.5.0.sh`:** not re-run. This commit changes one pure function (unit tested and run
  on the emulator) and comments. The 21 Dec job re-runs both.
- **Android 12:** emulator broken (above). The 21 Dec job's Android 12 step now gives up after 5 minutes and says
  so (`~/.claude/scheduled-tasks/sajdatime-1-5-0-reminder/SKILL.md`, outside the repo; its expected unit test
  count now mentions 407).
- **Review items 2 and 3:** not in the brief, so not fixed. Item 3 (the tile lag) was measured, above.
- **A real phone:** the owner's standing choice; emulator testing is the standard.

## Open, waiting for the owner (none blocks 1.5.0)

1. **Tile half a minute late** each minute. Claude's advice: leave it. The error is under a minute, and the watch
   note "updates every minute" stays true.
2. **Tile keeps the old time zone's prayer time** after a zone change: **built later on 19 Oct 2026** (owner's yes),
   `ClockChangeReceiver` redraws the tile on a time zone or clock change. Gate passed in the cloud; the watch
   emulator check **passed later the same night** (above). Closed. HANDOVER §11 STATE OF PLAY, last bullet.
3. **`sajda12`:** if Android 12 checks are wanted again, make a new AVD from the same image rather than retrying.
4. Unchanged from before: review item 2 (the limit note's position), Turkey's Maghrib against Diyanet, the
   "Tap to read" line, the short description needs his yes on release day.

## Release day (unchanged)

`docs/release-tools/RELEASE-1.5.0.md` "Still to do": the 21 Dec job re-runs `release-1.5.0.sh`; on the day Claude
re-dates `docs/privacy.html`; Ali merges `claude/quiet-glass` into `main`, builds both bundles from `main`, and in
the Console uploads them, pastes the texts (description now 3987 characters) and presses Send for review.
