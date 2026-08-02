# Starting a new session on SajdaTime

## The short version

**Open Claude Code in the project folder and paste this:**

```
Read docs/HANDOVER.md in full before doing anything. It is the complete handover
for this project. Then tell me what you understand the current state to be and
what you think the next step is, before you change any code.
```

That is all. **You do not need to attach any files.** The handover lives inside the repo,
so the assistant can read it, along with all the code, straight from disk.

There is also a `CLAUDE.md` at the top of the project, which Claude Code loads on its own
before you type anything. It carries the hard rules — the disclaimer, the privacy promise,
the signing key, the no-network-at-runtime decision — so a session cannot miss them even if
it skips the handover. You do not need to mention it; it is already working.

---

## Which folder to open

```
/Users/aliimrankhan/Developer/SajdaTime
```

Do **not** move the project into `~/Documents`, `~/Desktop`, or iCloud Drive. iCloud
corrupts the build folder and broke this project three separate times.

---

## If you are somewhere without the folder

If you are using claude.ai in a browser, or a machine that does not have the repo, attach
these files in this order:

| # | File | Why |
|---|---|---|
| 1 | `docs/HANDOVER.md` | **The essential one.** Everything: vision, stack, features, rules, what is done, what is pending. |
| 2 | `docs/ARCHITECTURE.md` | Only if the work touches prayer or Qibla calculation. The exact business rules. |
| 3 | `docs/DESIGN_SYSTEM.md` | Only if the work touches the look of any screen. Every colour token and component pattern. |
| 4 | `docs/RELEASING.md` | Only if the work is about publishing to Google Play. |

For most conversations, **file 1 on its own is enough**.

---

## Good opening messages, depending on what you want

**To carry on where it was left off**

```
Read docs/HANDOVER.md. Then work through the pending list in section 11,
starting with whatever does not need me personally. Test everything on the
emulator before you tell me it works.
```

**To carry on with the Play Store**

```
Read docs/HANDOVER.md and docs/RELEASING.md. The developer account exists and
the store assets are already prepared. Tell me what is actually outstanding
before you do anything.
```

The honest answer, as of the last session: the developer account is **fully verified** and
the signing key (the owner's alone) is the only blocker. ⚠️ **STALE — written 31 Jul 00:22.**
The key was created later that day, both bundles are signed, and the app has been live on the
closed testing track since 31 Jul 22:17. **`docs/HANDOVER.md` §11 is the current picture; read
its opening block first and treat this file as history.** Everything else — privacy policy,
screenshots, icon, feature graphic, listing text, Gradle signing wiring — is finished and in
the repo. A session that offers to "prepare the store assets" has not read the handover.

**To fix something a user reported**

```
Read docs/HANDOVER.md. A user says <describe the problem>. Find the cause,
verify it against the Aladhan reference API rather than assuming, fix it,
add a test, and show me it working on the emulator.
```

**To add a new feature**

```
Read docs/HANDOVER.md. I want <describe the feature>. Tell me what it would
involve and what it might break before you write any code.
```

---

## What to expect from a good session

A session that has actually read the handover will:

- know the app is a charity project with no ads, no accounts and no tracking
- know the disclaimer is a hard requirement and never remove it, including the dua request
  in its last paragraph — which belongs there and **nowhere else**, so the user is asked
  once and never nagged
- never machine-translate the app: prayer and madhab names are religious content and each
  language needs a native speaker before it ships
- run the full test suite before and after changes
- **run the app on an emulator** rather than saying "it compiles, so it works"
- check prayer times against the Aladhan API instead of trusting its own reasoning
- tell you plainly what it verified and what it only assumed

If an assistant tells you something is done without having run it, ask it to show you.
That is how the watch app once passed every check while being completely unusable.

---

## Handy things to ask for

```
Run the full verification and show me the result.
```

```
Show me the app running on the phone and watch emulators.
```

```
What is still pending from the handover, and what is actually blocking release?
```
