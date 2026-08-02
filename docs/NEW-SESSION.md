# Starting a new session on SajdaTime

For **any** AI assistant — Claude Code, Codex, Cursor, or a browser chat. Nothing here is
specific to one tool.

---

## ⚠️ Rule for this file

**Nothing that changes goes in here.** No test counts, no commit hashes, no "the only
remaining blocker is…". This file spent two days carrying the sentence *"the signing key is
the only blocker"* after the key had been created and the app was already live, and a fresh
session reading it would have gone looking for work that was long done.

Anything volatile belongs in `docs/HANDOVER.md`, which is updated as part of every commit.
This file holds only what stays true regardless of where the project has got to: **where to
look, what not to undo, and how to check.**

---

## The short version

Open the project folder in your assistant and paste this:

```
Read CLAUDE.md and then docs/HANDOVER.md before changing anything. Start with the
block at the top of HANDOVER §11 — that is the current state of play. Then run
`git log --oneline -10` and the verification command in §9, and tell me what you
understand the state to be and what you think the next step is, before you touch
any code.
```

You do not need to attach any files. The handover lives inside the repository, so the
assistant reads it, and all the code, straight from disk.

There is a `CLAUDE.md` at the top of the project. Some tools load it automatically, others do
not — which is why the prompt above names it explicitly. It carries the hard rules.

---

## Which folder

```
/Users/aliimrankhan/Developer/SajdaTime
```

Do **not** move the project into `~/Documents`, `~/Desktop`, or iCloud Drive. iCloud writes
conflict copies into `app/build/` and breaks dexing. It cost three separate debugging sessions.

---

## How to find the current state, rather than being told it

These are the commands. Their output is the truth; anything written down is a snapshot.

```bash
git log --oneline -10
```

```bash
git status --short
```

Commit messages in this project are written to be read — they carry the reasoning, not a file
list. Then the gate:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

`docs/HANDOVER.md` §9 says what that should print. Then run the app on both emulators —
**compiling is not working.** The watch app once compiled and linted clean for an entire
release while being unable to ever obtain a location.

---

## Settled — do not re-open these without reading why

The expensive thing to lose is not the code, it is the reasoning. Each of these was decided
deliberately, and several were built and then thrown away. All have their evidence in
`docs/HANDOVER.md`.

| Decision | Where |
|---|---|
| **No network at runtime** to calculate times, ever. Verifying against Aladhan *during development* is correct and encouraged | §2, `CLAUDE.md` |
| **No machine translation.** Prayer and madhab names are religious content; each language needs a native speaker first | `CLAUDE.md`, `NoTranslationsYetTest` |
| **The app stays English and left-to-right** until it has words in a right-to-left language. It follows the phone's language the moment a reviewed translation ships | §5.16 |
| **Moonsighting do not slide Fajr and Isha down to 60°.** Their own published timetable matches this engine to the minute. A "fix" was built and reverted; twelve of their rows are pinned as golden values | §10, `PolarAndHemisphereTest` |
| **The disclaimer must make four points**, and its six copies change together. The dua request is on the first-run screen and nowhere else | §5.15, `DisclaimerContentTest` |
| **Location never leaves the device.** Coarse, foreground only. Cloud backup and device-to-device transfer both off. `docs/privacy.html` is a published promise | §2, `CLAUDE.md` |
| **The release signing key is the owner's alone.** No assistant may generate, hold, request or view it | `CLAUDE.md`, `docs/RELEASING.md` |
| **The high-latitude rule is deliberately not a user setting** (item A4). "Match your mosque" closes the same gap without asking anyone to understand twilight models | §11 |

---

## Traps this project has already paid for

- **A test that reads files off disk does not re-run unless those exact files are declared as
  Gradle task inputs.** It reports success without running, and `clean` does not rescue it,
  because the build cache keys off the same list. This has happened **three times** — §15
  lessons 42, 84 and 91. If you add such a guard, declare the file, and prove it by breaking
  *that* file rather than a sibling.
- **`./gradlew installRtl` shows English words laid out right-to-left.** That always looks
  wrong, because it is. Read it as a check on *layout* — mirroring, margins, clipping — and
  never put its screenshots in front of the owner as findings. §15 lesson 86.
- **Character count does not predict where text wraps.** On the watch, 88 characters fitted
  three lines and 85 needed four. Screenshot it; `./tools/wear-verify.sh` captures exactly the
  frames that matter. §15 lesson 82.
- **Verify against an independent reference, not your own reasoning.** Every real calculation
  bug here was caught by comparing against Aladhan — and three confidently-reported "bugs"
  turned out to be errors in the test rather than in the code, so check both sides.

---

## If you are in a browser without the repository

Attach these, in this order:

| # | File | When |
|---|---|---|
| 1 | `docs/HANDOVER.md` | **Always.** For most conversations this alone is enough |
| 2 | `CLAUDE.md` | Always — it is short, and it carries the hard rules |
| 3 | `docs/ARCHITECTURE.md` | Only if the work touches prayer or Qibla calculation |
| 4 | `docs/DESIGN_SYSTEM.md` | Only if the work touches the look of a screen |
| 5 | `docs/RELEASING.md` | Only if the work is about publishing to Google Play |

---

## Opening messages for common jobs

**Carry on where it was left off**

```
Read CLAUDE.md and docs/HANDOVER.md, starting with the block at the top of §11.
Work through whatever does not need me personally. Run it on the emulator before
you tell me it works, and tell me plainly what you verified and what you assumed.
```

**Anything to do with the Play Store**

```
Read CLAUDE.md, docs/HANDOVER.md §11 and docs/RELEASING.md. Tell me what is
actually outstanding before you do anything. Do not offer to prepare store
assets — check whether they already exist first.
```

**A user has reported a problem**

```
Read CLAUDE.md and docs/HANDOVER.md. A user says <describe it>. Find the cause,
verify it against the Aladhan API rather than assuming, fix it, add a test, and
show me it working on the emulator.
```

**A new feature**

```
Read CLAUDE.md and docs/HANDOVER.md. I want <describe it>. Tell me what it would
involve and what it might break before you write any code.
```

---

## What a good session looks like

It will:

- know this is a charity project — no ads, no accounts, no tracking, no revenue, ever
- treat the disclaimer and the privacy promise as hard requirements, not preferences
- run the tests **and** the app, on both emulators, and check right-to-left before a layout change
- check prayer times against an independent reference instead of trusting its own arithmetic
- **update `docs/HANDOVER.md` in the same commit** as any behaviour change, and write a commit
  message explaining the reasoning rather than listing files
- say plainly what it verified and what it only assumed, and never call something done when it
  is untested

If an assistant tells you something works without having run it, ask it to show you. That is
how the watch app once passed every check while being completely unusable.

---

*Made with love, free for the Ummah.*
