# Releasing 1.7.0: the checklist

Written 4 Nov 2026, corrected the same night after an independent check (Fable and Opus). `claude/1-7-0` is the release branch (committed and pushed, nothing merged into `main`). **Ali presses every merge, submit, publish and consent button.** Claude does the "Claude" lines; in the Play Console Claude walks Ali through one screen at a time.

## What ships

- **Phone 1.7.0, versionCode 11** (`app/build.gradle.kts`). What is in it: `git log main..claude/1-7-0`, PROJECT_BRIEF "Next release after 1.6.0".
- **Watch 1.7.0, versionCode 1003** (`wear/build.gradle.kts`). The watch on Play is still 1.5.0 / 1002 (1.6.0 shipped the phone only), so this is the first watch build with **Bengali** and with 1.6.0's shared `:core` changes as well as the current-prayer mark: the watch note says so. Commit `6041c48` makes the watch Times list mark the current prayer, like the phone and the widgets. Both bundles are uploaded; the watch goes through its own Wear review. Permissions are unchanged (three). The watch rule is pinned by `CurrentForDisplayTest`.
- **Adhan voices are NOT in this release**: no reply to the permission email to the Haram muezzin agency; nothing is bundled without a written yes.

## Done (4 Nov 2026)

- Gate `clean test lint :app:bundleRelease :wear:bundleRelease`: 349 app, 255 core, 27 wear tests, 0 failures. Device suite 49 of 49 at `26aae84`; after the widget fixes of 4 to 5 Nov the six device widget tests pass at 1.0, 1.3 and 2.0 text and the every-slot proof passes in full. `release-1.7.0.sh` is re-run on the final commit (result recorded in HANDOVER section 11).
- Store listing 1.7.0 (English and eight locales) approved and stamped in the translation ledger; phone pictures approved; Redmi Note 13 Pro checks done. **Changed 12 Nov 2026:** six store languages only and new description lines (`docs/store/LISTING.md`); **13 Nov 2026: What's new written to Ali's brief (features, then fixes, accurate, six languages only), checked against the code, translated, back translated by an independent Opus review, ledger stamped. The whole listing is ready to paste; nothing waits on Ali but release day.**
- Widget entry heights measured and set (HANDOVER section 11).
- **Latest numbers, 12 Nov 2026 23:00 at `1145085`:** gate 365 app, 269 core, 27 wear, 0 failed, lint 0 errors; device suite 103 of 103 on emulator-5554; translation ledger 0 to review. Qibla checked against the Redmi's own compass (HANDOVER section 11, first block).

## Still to do before release day (Claude)

1. `release-1.7.0.sh`: DONE 5 Nov 2026 on `6b6bf20` (every line PASS or TAKEN) and run again 12 Nov 18:20 before the Qibla rounding fix. **NOT yet run on the current code** (`f13497b` changed the app): run it once Ali's own Qibla turn is OK, since it makes the signed bundles.
2. DONE 5 Nov 2026: `tools/wear-verify.sh` on both round sizes at 1.0x and 1.3x, the current prayer is marked and nothing is behind the bezel.
3. Watch "What's new" for the Wear track: one English line is drafted in `docs/store/LISTING.md` ("1.7.0 watch note, DRAFT"); Ali approves it, then Claude translates it and stamps the ledger. DONE 6 Nov 2026; since 12 Nov 2026 only the five translations for the six store languages are kept.
4. DONE 6 Nov 2026 (`c34d3c9`): the watch Times picture was retaken; since then only the watch Qibla page changed, and its picture was refreshed 12 Nov (`be8232d`).
5. **Before release day, Ali:** his own turn with "SajdaTime (test)" on the Redmi against Google Maps, and his look at `build/review-1.7.0/`.

## Release day, in order

0. **Is 1.6.0 live?** Yes: the public page read "Updated on 2 Nov 2026" on 12 Nov. Check again on the day, signed out (CLAUDE.md). Do the 1.6.0 live chores first (mark the listing live, re-baseline the daily health check, PROJECT_BRIEF). If 1.6.0 is still in Google's review, **Ali decides**: wait for it, or replace it with 1.7.0 (which can show errors in the Console). Recommendation: wait.
1. **Claude:** `docs/release-tools/set-release-date.sh "<d Month yyyy>" 1.7.0` (moves `docs/privacy.html` "Last updated" and the date in `UsageCountsTest`); data handling has not changed in 1.7.0, so the page text stays. Gate, commit, push.
2. **Ali:** opens and merges the pull request `claude/1-7-0` into `main` (none exists yet).
3. **Claude:** `./gradlew :app:bundleRelease :wear:bundleRelease` on `main`; **Ali** signs and uploads both (the key is his alone, `docs/RELEASING.md`).
4. **Ali + Claude, one screen at a time:** Play Console, production release for the phone (paste the listing and What's new), then the Wear OS release (the watch note). Ali presses Send on each. **Six store languages only from 1.7.0 (Ali, 12 Nov 2026):** paste English, Arabic, Urdu, Bengali, Turkish and Indonesian, and in Store listings, Manage translations, **delete the three other translations** (Malay, French, Hindi) so no old 1.6.0 text stays live in them; Ali presses each delete.
5. After Google's review: check the public page "Updated on" date signed out, then record it in HANDOVER, PROJECT_BRIEF and GBrain, and re-baseline the daily health check.

The script writes the bundles to a temporary `~/Developer/SajdaTime/SajdaTime-1.7.0/` (gitignored). Delete that folder after the upload; the signed phone bundle may be copied to Drive folder 99 if Ali wants a record.
