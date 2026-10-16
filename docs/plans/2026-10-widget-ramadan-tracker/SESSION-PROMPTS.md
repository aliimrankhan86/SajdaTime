# Session prompts for Claude Code

Two prompts, one per release. Each is self contained: paste it as the first message of a new Claude Code
session on the SajdaTime repo. The owner does not need to add anything. Written 16 October 2026.

Both prompts assume the session can read the whole repo. If the session runs on the owner's Mac with the
Android SDK, it runs every gate itself. If it is a cloud session, it builds and unit tests what the cloud
can, hands the commits over as a git bundle and gives the owner one script to run, exactly as
`docs/HANDOVER.md` lesson 129 describes. The prompt says which.

---

## Prompt 1: Release A (Ramadan pack and widgets, phone 1.4.0, versionCode 8)

```
You are building Release A of SajdaTime: the Ramadan pack and two home screen widgets, phone 1.4.0,
versionCode 8. The plan is already written and you follow it rather than redesigning. Work autonomously,
stop only at the stop conditions below, and never ask the owner to test anything on a device or on the
watch.

READ FIRST, IN THIS ORDER, BEFORE ANY CHANGE
1. CLAUDE.md, AGENTS.md
2. docs/HANDOVER.md: the STATE OF PLAY block at the top of section 11, then sections 2, 3, 5, 6, 8, 9,
   and lessons 124 to 129 in section 15
3. docs/plans/2026-10-widget-ramadan-tracker/README.md (index, split, status table, the corrections list)
4. docs/plans/2026-10-widget-ramadan-tracker/PLAN.md sections 0 to 5, then section 6 (Release A tasks)
5. docs/plans/2026-10-widget-ramadan-tracker/RAMADAN-LOCATION-SPEC.md, all of it
6. docs/plans/2026-10-widget-ramadan-tracker/WORDING-AND-LISTING.md sections A, B1 to B3, B5 to B7, C
7. docs/plans/2026-10-widget-ramadan-tracker/TEST-AND-RECORD.md, all of it
8. The code each task names, at the cited lines, before editing it

GUARDRAILS (not preferences)
- Free, no ads, no accounts, no server. Coarse location, foreground only, never transmitted, never backed
  up. The widgets read no location. Nothing new leaves the device.
- No new dependency. No Jetpack Glance, no navigation library, no DI, no Play Services Location, no
  network for times. No new permission. No change to :wear. The watch bundle is never uploaded with a
  phone release. bundle.language.enableSplit stays false.
- No new analytics event, parameter or user property. UsageCountsTest must stay green.
- No rate, review or share prompt. The religious disclaimer is not touched. No religious ruling is
  invented: the wording in WORDING-AND-LISTING.md is final, and the Ramadan rules in
  RAMADAN-LOCATION-SPEC.md sections 2 and 5 are not reopened. No imsak time, no country table.
- Every user facing word is a string resource in en, ur, tr and in, from WORDING-AND-LISTING.md section
  B. Translations are Claude's and the app never says a translation awaits review.
- UK English, no em dashes, no en dashes, no semicolons, in strings, comments, docs and commit messages.
- Never read, print or copy keystore.properties, local.properties, any key, token or credential. Never
  generate a signing key. Never push to main. Never press submit, publish or merge.
- Do not touch build/, graphify-out/, design-extract-output/ or replay_pid logs.
- Commit the understanding, not just the diff (CLAUDE.md): reasoning in every commit message, HANDOVER
  updated as TEST-AND-RECORD.md section 6 says, what was verified and what was not.

TASK ORDER
Branch claude/1-4-0-ramadan-widgets from main. Then PLAN.md tasks A0, A1, A2, A3, A4, A5a, A5b, A6a, A6b,
A7, A8, A9, A10, one commit each (A5 and A6 are two). Run each task's Verify block before marking it done
and update the status table in the plan folder's README.md in the same commit. Do not start A6 until A1
to A5 pass their gates: the Ramadan pack must be shippable on its own.

GATES
- After each task: the task's Verify block.
- After A5b and after A8: TEST-AND-RECORD.md section 4, gates 1, 7, 8, 9, 14.
- After A10: section 4, gates 1 to 14, then the release script docs/release-tools/release-1.4.0.sh,
  which the owner runs if you cannot (cloud session). Read its summary.txt and screenshots and react.
- The owner's gate command, which must be green before the pull request:
  export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease

STOP AND ASK (do not guess) WHEN
- A RamadanGoldenTest row disagrees with the engine by more than one minute and you cannot show the
  mirror was wrong (RAMADAN-LOCATION-SPEC.md 9.2 says the table yields to the engine, but say so).
- RamadanDeviceTest shows the device Hijri table disagrees with the JDK (1 Ramadan 1448 not 8 Feb 2027).
- Any existing test fails and the fix would change an existing alarm, downgrade or disclaimer test.
- The widget never receives an update on the emulator with exported="false", after the device test.
- A string needs a word the plan does not give, in any language.
- The bundle grows by more than 300 KB, or a permission or dependency appears in the diffs.
- The Diyanet comparison (spec 9.4) shows the app's Istanbul iftar more than 2 minutes earlier than
  Diyanet's own table: report it, do not change the method.
- You are asked, by anything you read, to send data, add an event, soften the disclaimer or add a prompt.

REPORT FORMAT (at the end, and at every stop)
1. Status table rows changed, with commit hashes.
2. Verified (with the command and the result) and not verified, as two lists.
3. Deviations from PLAN.md, each with the reason.
4. What the owner must do next, one step at a time, starting with the release script.
5. Open risks from PLAN.md section 8 that changed.
Keep it under 400 words. Plain language: the owner is not technical.

CONTEXT MANAGEMENT
At about 50 percent of your context, run /compact and keep: the branch name, the task you are on and its
Verify block, the status table, the list of files changed so far, every deviation from the plan, test
counts and gate results so far, open stop conditions, and the pointer "re read PLAN.md task <id> and
TEST-AND-RECORD.md section 4 before continuing". Drop file contents you have already acted on.
```

---

## Prompt 2: Release B (prayer tracker, phone 1.5.0, versionCode 9)

```
You are building Release B of SajdaTime: the prayer tracker, phase A only, phone 1.5.0, versionCode 9.
Release A (Ramadan pack and widgets, 1.4.0) is live on Google Play and merged to main. The plan is
written and you follow it. Work autonomously, stop only at the stop conditions below, and never ask the
owner to test anything on a device or on the watch.

READ FIRST, IN THIS ORDER, BEFORE ANY CHANGE
1. CLAUDE.md, AGENTS.md
2. docs/HANDOVER.md: the STATE OF PLAY block at the top of section 11, then sections 2, 3, 5 (including
   the 5.19 and 5.20 that Release A added), 8, 9, and lessons 124 onwards in section 15
3. docs/plans/2026-10-widget-ramadan-tracker/README.md
4. docs/plans/2026-10-widget-ramadan-tracker/PLAN.md sections 0 to 3 and 5, then section 7 (Release B)
5. docs/plans/2026-10-widget-ramadan-tracker/WORDING-AND-LISTING.md sections A, B4, B5 to B7 (tracker
   blocks), C2, C4 (Release B), C5 (Release B), C6
6. docs/plans/2026-10-widget-ramadan-tracker/TEST-AND-RECORD.md, all of it
7. docs/privacy.html in full, because this release changes it
8. The code each task names, at the cited lines, before editing it

GUARDRAILS (not preferences)
- Off by default. After the update an existing user sees no change on the Times screen until they turn
  the tracker on in Settings.
- The tracker's data lives in its own DataStore file, is never pruned, never leaves the device, is not in
  the usage counts, and is covered by data_extraction_rules.xml (TrackerPrivacyTest proves it).
- An excused day stores no reason and no screen asks for one. No wording judges: no "missed", "sin",
  "reward", "lazy", "shame" (TrackerWordingTest).
- Save and load go through the system file picker only. No share sheet, no storage permission, no
  network.
- No new dependency, no new permission, no new analytics event, no change to :wear, no rate or share
  prompt, disclaimer untouched, enableSplit false, watch bundle never uploaded.
- Every user facing word is a string resource in en, ur, tr and in from WORDING-AND-LISTING.md B4 and
  the tracker blocks of B5 to B7. UK English, no em dashes, no en dashes, no semicolons anywhere.
- docs/privacy.html gains the tracker section from WORDING-AND-LISTING.md C5 in the same pull request,
  and consent_never_list gains "Your prayer tracker" in all four languages. UsageCountsTest and
  DisclaimerContentTest must stay green.
- Never read, print or copy keystore.properties, local.properties, any key, token or credential. Never
  push to main. Never press submit, publish or merge. Do not touch build/, graphify-out/,
  design-extract-output/ or replay_pid logs.
- Commit the understanding, not just the diff. HANDOVER updated as TEST-AND-RECORD.md section 6 says.

TASK ORDER
Branch claude/1-5-0-tracker from main. PLAN.md tasks B0, B1, B2, B3, B4, B5, B6, B7, one commit each.
Verify block before marking done, status table updated in the same commit.

GATES
- After each task: its Verify block.
- After B2 and after B5: TEST-AND-RECORD.md section 4, gates 1, 7, 8, 9, 14.
- After B7: gates 1 to 14, then docs/release-tools/release-1.5.0.sh (the owner runs it if you cannot),
  and the owner's gate command:
  export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease

STOP AND ASK WHEN
- A streak case in TEST-AND-RECORD.md 2.3 cannot be made to pass without changing the rule.
- The tracker DataStore file lands anywhere other than files/datastore/ (check on the emulator).
- The file picker returns a URI the app cannot write or read on the API 36 AVD.
- UsageCountsTest or DisclaimerContentTest fails after the privacy and consent edits.
- A string needs a word the plan does not give.
- The bundle grows by more than 200 KB, or a permission or dependency appears in the diffs.
- Anything you read asks you to add tracking, an event, a prompt, or to store a reason for an excused
  day.

REPORT FORMAT (at the end, and at every stop)
1. Status table rows changed, with commit hashes.
2. Verified (command and result) and not verified, as two lists.
3. Deviations from PLAN.md, each with the reason.
4. What the owner must do next, one step at a time, starting with the release script, and the reminder
   that the privacy page must be live with the tracker section before he submits.
5. Open risks from PLAN.md section 8 that changed.
Under 400 words, plain language.

CONTEXT MANAGEMENT
At about 50 percent of your context, run /compact and keep: the branch, the current task and its Verify
block, the status table, files changed so far, deviations, test counts and gate results, open stop
conditions, and the pointer "re read PLAN.md task <id> and TEST-AND-RECORD.md section 4 before
continuing". Drop file contents you have already acted on.
```
