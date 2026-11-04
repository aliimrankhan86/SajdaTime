# Releasing 1.7.0: the checklist

Started 4 Nov 2026 when Ali said yes to release prep. `claude/1-7-0` is the release branch (49 commits ahead of `main`, nothing merged). **Ali presses every merge, submit, publish and consent button.** Claude does the "Claude" lines; in the Play Console Claude walks him through one screen at a time from his screenshots, never a list (CLAUDE.md). Nothing here touches the signing key. Model: `docs/release-tools/RELEASE-1.6.0.md` (same steps, read it for the detail).

## What ships

- **Phone 1.7.0, versionCode 11, versionName 1.7.0** (`app/build.gradle.kts`, set 4 Nov 2026; unit tests green after the bump). What is in it: `git log main..claude/1-7-0`, PROJECT_BRIEF "Next release after 1.6.0", HANDOVER section 11 first block ("Built on the branch"). In short: time left on the Now row, optional Duha row, the Sky band, Match your mosque (times under each prayer, "0 min", the Hijri shift moved to the Islamic dates sheet), restyled widgets (Next prayer, Today, strip: sky body, logo mark on the current prayer, corner logo, sizes measured on a real launcher, thin strip shows the next prayer only).
- **The watch is NOT uploaded.** It stays 1.5.0, versionCode 1002: `wear/` is unchanged since `main` (`git diff main --stat -- wear` is empty; check again on release day).
- **Adhan voices are NOT in this release**: the permission email to the Haram muezzin agency has no reply; nothing is bundled without a written yes.

## Done

- Version bump (above). Gate (tests, both bundles, lint) and device suite 55 of 55 on `0f64f9d`; unit tests green after the bump.
- Redmi Note 13 Pro checks (4 Nov 2026), see PROGRESS.md and HANDOVER section 11.

## To do before release day

1. **Claude + Ali:** write a **1.7.0 block in `docs/store/LISTING.md`** (short description, full description, What's new) from the approved 1.6.0 block ("1.6.0 full description and What's new, DRAFT 2 Nov 2026: APPROVED"). The whole app is sold first, every feature the build has, then What's new says what changed (HANDOVER 5.38, Ali 30 Oct 2026). Features to add: the time left on the Now row, Duha (optional), the Sky band, widgets, "Match your mosque start times" (the Islamic date shift now lives in the Islamic dates sheet: do not copy the old "shift the Islamic date" bullet, lines 301, 379, 477, 597, 684 are history). **Ali approves the English first**; then the eight translations (`docs/store/LISTING-LOCALES.md`), independent review, `python3 tools/translation-ledger.py` then `stamp`.
2. **Claude:** retake the store pictures (Times with the Sky band and Duha, Settings, Match your mosque, the widgets): `STORE_SHOTS=1` on the `sajdastore` emulator or by hand as `docs/store/upload/README.md` says; show Ali; Bengali uses the English set.
3. **Claude:** copy `docs/release-tools/release-1.6.0.sh` to `release-1.7.0.sh`, update `EXPECTED_COUNTS` and the version, run it on the final committed code: every line PASS or TAKEN.
4. **Claude:** PROJECT_BRIEF, HANDOVER section 11 and GBrain `projects/sajdatime` say the same thing (they do as of 4 Nov 2026).

## Release day, in order

1. **Claude:** `docs/release-tools/set-release-date.sh "<d Month yyyy>" 1.7.0` (moves `docs/privacy.html` "Last updated" and the date in `UsageCountsTest`); data handling has not changed in 1.7.0, so the page text stays. Gate, commit, push.
2. **Ali:** merges the pull request `claude/1-7-0` into `main`.
3. **Claude:** `./gradlew :app:bundleRelease` on `main`; **Ali** signs and uploads (the key is his alone, `docs/RELEASING.md`).
4. **Ali + Claude, one screen at a time:** Play Console, production release, paste the listing, What's new, send for review. Ali presses Send.
5. After Google's review: check the public page "Updated on" date signed out (CLAUDE.md), then record it in HANDOVER and GBrain.
