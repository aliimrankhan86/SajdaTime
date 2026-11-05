# Releasing 1.7.0: the checklist

Written 4 Nov 2026, corrected the same night after an independent check (Fable and Opus). `claude/1-7-0` is the release branch (committed and pushed, nothing merged into `main`). **Ali presses every merge, submit, publish and consent button.** Claude does the "Claude" lines; in the Play Console Claude walks Ali through one screen at a time.

## What ships

- **Phone 1.7.0, versionCode 11** (`app/build.gradle.kts`). What is in it: `git log main..claude/1-7-0`, PROJECT_BRIEF "Next release after 1.6.0".
- **Watch 1.7.0, versionCode 1003** (`wear/build.gradle.kts`). The watch on Play is still 1.5.0 / 1002 (1.6.0 shipped the phone only), so this is the first watch build with **Bengali** and with 1.6.0's shared `:core` changes as well as the current-prayer mark: the watch note says so. Commit `6041c48` makes the watch Times list mark the current prayer, like the phone and the widgets. Both bundles are uploaded; the watch goes through its own Wear review. Permissions are unchanged (three). The watch rule is pinned by `CurrentForDisplayTest`.
- **Adhan voices are NOT in this release**: no reply to the permission email to the Haram muezzin agency; nothing is bundled without a written yes.

## Done (4 Nov 2026)

- Gate `clean test lint :app:bundleRelease :wear:bundleRelease`: 349 app, 255 core, 27 wear tests, 0 failures. Device suite 49 of 49 at `26aae84`; after the widget fixes of 4 to 5 Nov the six device widget tests pass at 1.0, 1.3 and 2.0 text and the every-slot proof passes in full. `release-1.7.0.sh` is re-run on the final commit (result recorded in HANDOVER section 11).
- Store listing 1.7.0 (English and eight locales) approved and stamped in the translation ledger; phone pictures approved; Redmi Note 13 Pro checks done.
- Widget entry heights measured and set (HANDOVER section 11).

## Still to do before release day (Claude)

1. DONE 5 Nov 2026 on `6b6bf20`: every line PASS or TAKEN, no FAIL (HANDOVER section 11, first block). Re-run it if any code changes before release day.
2. DONE 5 Nov 2026: `tools/wear-verify.sh` on both round sizes at 1.0x and 1.3x, the current prayer is marked and nothing is behind the bezel.
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
