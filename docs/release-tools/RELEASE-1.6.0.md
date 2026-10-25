# Releasing 1.6.0: the checklist

Written 25 Oct 2026, when step 11 (release prep) was reviewed and Ali approved the store pictures ("Yes all fine").
`claude/1-6-0` is the release branch and holds everything. **Limits (BUILD-INSTRUCTIONS, Q25): the pull request by
Fri 11 Dec 2026, Send for review by Tue 15 Dec 2026.** Ramadan 1448 begins Mon 8 Feb 2027, so 15 Dec leaves room for
Google's review and a fix release. The release day itself is not fixed: it starts when Ali says so.

Each line says who does it. Claude does the "Claude" lines from a session on his Mac. Nothing here touches the signing
key, and **Ali presses every merge, submit, publish and consent button**. In the Play Console Claude walks him through
one screen at a time from his screenshots, never a list (CLAUDE.md).

## What ships

- **Phone 1.6.0, versionCode 10** (`app/build.gradle.kts`). Contents: PROJECT_BRIEF "Next release: 1.6.0".
- **The watch is NOT uploaded.** It stays 1.5.0, versionCode 1002, already live since 23 Oct 2026; `wear/` changed only
  in comments since `main`. Its store pictures and watch note stay as they are.
- **Permissions: 12**, VIBRATE among them (listed in `docs/privacy.html`); `release-1.6.0.sh` checks the bundle has
  exactly these.

## Already done (25 Oct 2026, step 11; HANDOVER §11 STATE OF PLAY)

- English listing: `docs/store/LISTING.md`, "1.6.0 listing, 25 Oct 2026": short description (80), full description
  (3990), What's new (494). Ali chose all four recommendations, the Chrome and Edge line is gone.
- Eight translated listings with their What's new: `docs/store/LISTING-LOCALES.md`, "1.6.0 listings"; independent
  back translation review done; record `docs/translation/store-listing-1.6.0.md`.
- 24 store pictures, approved by Ali 25 Oct 2026: `docs/store/upload/phone/<en-GB|ar|ur|tr-TR|id-ID>/`.
- Gate green on the branch (core 237 with 1 skipped, app 322, wear 27, lint 0 errors), device suite 39 of 39.

## Before release day

1. **Claude:** run `docs/release-tools/release-1.6.0.sh` on the final code (no `STORE_SHOTS`: the pictures are done and
   approved; setting it on the `sajdastore` emulator would retake them). Expect every line PASS or TAKEN, with the
   phone SKIPPED by Ali's choice and `store-screenshots` SKIPPED. Its counts are in `EXPECTED_COUNTS`; if a later change
   moves them, update the number there in the same commit, it is not a fault. **Not yet run end to end** (25 Oct 2026).
2. **Claude:** if any screen changed after 25 Oct 2026, compare the pictures with the build and retake only what no
   longer matches (`STORE_SHOTS=1` on `sajdastore`, or by hand as `docs/store/upload/README.md` says), then show Ali.
3. **Claude:** read the full description and What's new against the final build once more, line by line. A fact that is
   no longer true is corrected (with the eight translations); any other wording change goes to Ali first.

## Release day, in order

1. **Claude:** `docs/release-tools/set-release-date.sh` (today, or `"15 December 2026"`): it moves `docs/privacy.html`
   "Last updated" and the date pinned in `UsageCountsTest` together. Run the gate, commit, push.
   Then write the date where it belongs in the same push (Ali, 23 Oct 2026: "the day we release it, write it wherever
   it needs to be written"): HANDOVER §11 STATE OF PLAY and a §10 entry ("1.6.0 submitted on <date>"); PROJECT_BRIEF
   "What is live" and "Next release"; this file's steps marked DONE with the date.
2. **Claude:** open the pull request `claude/1-6-0` into `main`. **Ali merges it that same day, not before**: merging
   publishes `docs/`, the privacy policy included, through GitHub Pages (HANDOVER §15 lessons 124 and 137).
3. **Ali, on his Mac:** build the phone bundle fresh from `main` (never upload a saved file):
   `export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean :app:bundleRelease`.
4. **Ali in the Play Console, Claude guiding each screen:**
   - production, phone: upload `app/build/outputs/bundle/release/app-release.aab` (10 / 1.6.0); no watch upload;
   - What's new: English from LISTING.md, and each translated listing's own What's new from LISTING-LOCALES.md;
   - main store listing (English): the new short description and full description from LISTING.md; then save, reload
     and search the page for "price or promotion" (the word "private" is new in the short description; if Play flags
     it, use the 1.5.0 short description for that language, as LISTING-LOCALES.md says);
   - the eight translations (Grow, Store presence, Store listings, Manage translations): short and full description
     each, titles unchanged;
   - phone screenshots: the English listing gets the eight in `upload/phone/en-GB/`, in filename order (the old
     `02-qibla`, `03-ramadan-dark`, `04-school-of-thought`, `05-settings` and `06-widgets` go). Arabic, Urdu, Turkish and
     Indonesian each get their own four from `upload/phone/<ar|ur|tr-TR|id-ID>/`. Malay, Bengali, French and Hindi
     have no own pictures and show the English ones. **Assumed, not verified:** that a language with its own pictures
     shows only those; look at one translated listing's preview before sending;
   - Data safety: **Ali confirms it is unchanged.** My prayers is kept on the phone only and is never sent, so it is
     not "collected" in Play's sense (`docs/privacy.html`, "Your prayer record"). Expected: no change;
   - then **Ali presses Send for review himself.**

## After it is live

Watch the public page's "Updated on" date move (signed out; Play sends no email):
`https://play.google.com/store/apps/details?id=com.sajdatime.app`.

- **Claude:** `docs/store/LISTING.md` "1.6.0 listing ... paste on the 1.6.0 release day" becomes "LIVE since <date>",
  and the same in LISTING-LOCALES.md; HANDOVER §11 and PROJECT_BRIEF say "live on <date>"; GBrain `projects/sajdatime`.
- **Claude:** re-baseline the daily health check (`trig_0129FQnWihf8XG6rKLCB4oFr`) to 1.6.0: the new "Updated on",
  What's new, the new phone screenshot IDs (the watch ones are unchanged), the privacy policy's date, and the 1.6.0
  description. Until then it reports the change every day, on purpose.
- **Claude:** then 1.6.1 (jama'ah and fasting reminders, after Eid) starts from the plan, not before Ali says so.

## Not verified yet (say so, do not assume)

- `release-1.6.0.sh` run end to end, and its Arabic store pass (6j) on a device.
- Whether Play shows only a language's own pictures (above).
- "private" in the short descriptions against Play's promotion notice (a paste time check).
- The suhoor reminder device test at a Ramadan clock (the script's 6d) on the final code.
