# Releasing 1.7.0: the checklist

Written 4 Nov 2026, corrected the same night after an independent check (Fable and Opus). `claude/1-7-0` is the release branch (committed and pushed, nothing merged into `main`). **Ali presses every merge, submit, publish and consent button.** Claude does the "Claude" lines; in the Play Console Claude walks Ali through one screen at a time.

## What ships

- **Phone 1.7.0, versionCode 11** (`app/build.gradle.kts`). What is in it: `git log main..claude/1-7-0`, PROJECT_BRIEF "Next release after 1.6.0".
- **Watch 1.7.0, versionCode 1003** (`wear/build.gradle.kts`). Commit `6041c48` makes the watch Times list mark the current prayer, like the phone and the widgets. Both bundles are uploaded; the watch goes through its own Wear review. Permissions are unchanged (three). The watch rule is pinned by `CurrentForDisplayTest`.
- **Adhan voices are NOT in this release**: no reply to the permission email to the Haram muezzin agency; nothing is bundled without a written yes.

## Done (4 Nov 2026)

- Gate `clean test lint :app:bundleRelease :wear:bundleRelease`: 349 app, 252 core, 30 wear tests, 0 failures. Device suite 49 of 49 on the emulator (before the last two docs-only commits).
- Store listing 1.7.0 (English and eight locales) approved and stamped in the translation ledger; phone pictures approved; Redmi Note 13 Pro checks done.
- Widget entry heights measured and set (HANDOVER section 11).

## Still to do before release day (Claude)

1. Run `bash docs/release-tools/release-1.7.0.sh` on the final committed code and paste its `summary.txt` here: every line PASS or TAKEN. It has NOT been run for 1.7.0 yet.
2. Run the watch once on the `sajdawear` emulator (`tools/wear-verify.sh`) and look at the Times list: the current prayer is marked, nothing before Fajr. Record it in HANDOVER.
3. Watch "What's new" for the Wear track: one English line is drafted in `docs/store/LISTING.md` ("1.7.0 watch note, DRAFT"); Ali approves it, then Claude translates it into the eight languages and stamps the ledger.
4. Retake the one watch Times picture (`docs/store/upload/wear-os/w1-times.png`, still the 1.5.0 one) so it matches the app.

## Release day, in order

0. **Is 1.6.0 live?** Check the public page signed out (CLAUDE.md): "Updated on" later than 23 Oct 2026 means 1.6.0 is out. Do the 1.6.0 live chores first (mark the listing live, re-baseline the daily health check, PROJECT_BRIEF). If 1.6.0 is still in Google's review, **Ali decides**: wait for it, or replace it with 1.7.0 (which can show errors in the Console). Recommendation: wait.
1. **Claude:** `docs/release-tools/set-release-date.sh "<d Month yyyy>" 1.7.0` (moves `docs/privacy.html` "Last updated" and the date in `UsageCountsTest`); data handling has not changed in 1.7.0, so the page text stays. Gate, commit, push.
2. **Ali:** opens and merges the pull request `claude/1-7-0` into `main` (none exists yet).
3. **Claude:** `./gradlew :app:bundleRelease :wear:bundleRelease` on `main`; **Ali** signs and uploads both (the key is his alone, `docs/RELEASING.md`).
4. **Ali + Claude, one screen at a time:** Play Console, production release for the phone (paste the listing and What's new), then the Wear OS release (the watch note). Ali presses Send on each.
5. After Google's review: check the public page "Updated on" date signed out, then record it in HANDOVER, PROJECT_BRIEF and GBrain, and re-baseline the daily health check.

The script writes the bundles to a temporary `~/Developer/SajdaTime/SajdaTime-1.7.0/` (gitignored). Delete that folder after the upload; the signed phone bundle may be copied to Drive folder 99 if Ali wants a record.
