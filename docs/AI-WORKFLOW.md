# How work gets done on SajdaTime (read this first, any AI)

Written 24 Nov 2026 after release 1.7.0 and the 1.7.1 widget redesign. It records what worked, so the next AI can pick up any job quickly. Facts that change live in `docs/HANDOVER.md` §11. Rules live in `CLAUDE.md`. This file is the how.

## Who does what
- **Ali** (owner, not technical, minimal effort): decides what, presses Merge, signs and uploads with his own key, presses Send for review. Never ask him to paste long instructions or to test the watch.
- **Claude Code on his Mac** (Terminal in ~/Developer/SajdaTime): builds, tests, emulator, pictures, commits, pushes, opens pull requests. Must be in auto mode, or it cannot write files or run commands.
- **claude.ai SajdaTime project session** (cloud): plans, reviews pictures, checks GitHub and Play Console in Ali's Chrome, writes store text, merges the PR on GitHub when Ali has said so. It reads and writes the Mac folder but cannot type into Claude Code and cannot push.
- **Scheduled tasks** (cloud, run on his Mac): keep the work moving if a chat or usage limit stops. A one-off task finishes the job, then is switched off.

## The channel between the two Claudes (files in build/, gitignored)
- Claude Code writes `build/<job>-progress.md`, dated with the Mac clock (UTC), ends with `STATUS: DONE` or `STATUS: BLOCKED <need>`.
- The project session writes findings to `build/<job>-review.md`. Claude Code reads it before finishing.
- The job brief is `build/<job>.md`. Ali gets ONE paste line for Claude Code that points at it.

## A normal release or change, in order
1. Design shown first as a private artifact page (light, dark, large text, Urdu), Ali says yes.
2. Brief written to build/. Claude Code builds on a branch, full gate and lint, device tests, pictures in light, dark, 2x text, Urdu, Arabic, smallest size. Fable (`ux-lead`) judges the pictures and an independent reviewer (Opus) checks the diff before commit. A widget change also means measuring its heights again on the emulator (`WidgetRenderEvidenceTest#today_needs`, then the fit tests).
3. Project session reads the pictures and the diff, then merges on GitHub when Ali has said so for that job (as for 1.7.1), and deletes the branch. Without that word Ali presses Merge himself (CLAUDE.md).
4. Store: only new What's new text in the six languages (en-GB, ar, bn-BD, id, tr-TR, ur), existing listing text untouched unless Ali says. Draft release in Play Console, signed bundle uploaded, Ali presses Send for review.
5. Prove it is live from the public Play page "Updated on" date, signed out. Then clean up (CLAUDE.md "After every release") and update HANDOVER §11, PROJECT_BRIEF and GBrain together.

## Test only what is new (Ali, 26 Nov 2026)
- Shipped text and translations are already tested. `docs/translation/reviewed.json` records every checked string per language with a hash. Re-check a string only if it is new or its hash changed, in all six languages, then add it to the file.
- Pictures and device tests cover only new or changed screens and code. The automatic gate still runs in full. Write in the job's progress file what was checked, so nothing is tested twice.

## Always tell Ali whether work is running (Ali, 26 Nov 2026)
- He does not want to wait without knowing. Whenever a session or scheduled run stops, finishes, is blocked or needs him, tell him in plain words straight away (a push notification when he may be away): "Stopped" or "Still working", why, and the exact one line to paste or the one button to press next.
- Never end silently, and never leave him to guess whether anything is still happening.

## Traps that cost time
- Play sends no email when a review passes. Check the public page date.
- Never install the Play package name on his phones, only the `.sideload` copy.
- A scheduled-task session cannot delete its own task: it disables it, the project chat deletes it.
- The cloud session cannot reach GitHub over SSH. Pushes and pulls happen in Claude Code.
- Claude Code in "don't ask" mode blocks all writes. Shift+Tab to auto mode first.
- Raw widget and store numbers are read from pictures and tests, never assumed.

## Example: 1.7.1 (24 Nov 2026)
Today widget redrawn only. Mockup page, brief `build/widget-today-redesign.md`, branch `claude/widget-today-redesign`, versionCode 12 / 1.7.1, watch untouched at 1003 / 1.7.0, Play draft "12 (1.7.1)" with one What's new line in six languages, full description unchanged.
