# What we do after the closed test

**Plain-language plan, written 1 Aug 2026.** Nothing here is started, and nothing here
should be started until Google grants production access. The reasoning, evidence and
measurements behind every item are in [`HANDOVER.md`](HANDOVER.md) §10 and §11 (items T1,
T2, T3) — this file is the short version, in order, for someone who wants to know what
happens next without reading two thousand lines.

---

## First, what we are waiting for

The app is **live on the closed testing track** and approved. It is not in the store.

Google requires **12 testers opted in, held for 14 consecutive days**. The count sits at
**9**. The fourteen days are counted only while twelve or more are opted in, so **the clock
is not running yet** — days spent at nine are not early days of the fortnight, they are days
that do not count.

Nothing in the code is blocking. The only work left before this stage ends is human: three
more people opening the opt-in link. See [`RELEASING.md`](RELEASING.md) Step 8.

**Why we are not shipping fixes now.** Every new build triggers another Google review and
another chance to disturb a run that has not begun. None of the three fixes below is urgent
enough to risk that. They are written up in full so the work can start the day the gate
clears, not so it can start early.

---

## The three fixes, in the order users feel them

### 1. Isha does not match UK mosques

**The problem.** For a user in Slough, the app says Isha begins at **23:17**. All three of
his local mosques say about **22:00**. The app is 78 minutes late.

**Why.** Nothing is broken. Isha begins when the twilight goes, and there are two ways to
define that — a fixed sun angle, or the observed fading of the redness (*shafaq*). The app
defaults to Muslim World League's 17° angle. Every mosque measured uses the *shafaq*-based
Moonsighting convention. At Britain's latitude those two answers are over an hour apart.

**Measured, not assumed.** Both mosques were identified exactly: Reading Mosque matches
Moonsighting with Hanafi Asr, JMIC Slough matches Moonsighting with standard Asr — Asr agreed
**to the exact minute** in both cases, on a value that differs by 67 minutes between the two
madhabs. Full tables in `HANDOVER.md` §10, "Isha in the UK".

**What already works today, with no update at all:** Settings → Calculation method →
**Moonsighting Committee**, then set the madhab the mosque follows. That reproduces both
mosques to within a minute. The option ships already. It is simply unreachable for anyone who
never opens Settings, because the first-run flow asks for sect and madhab and never mentions
method.

**⚠️ This one needs the owner's decision, and it is not an engineering question.** Four
options, with what each costs:

| Option | What it does | Cost |
|---|---|---|
| Leave the default; explain it in Settings | Keeps a defensible mainstream convention | Free, but users stay mismatched |
| **Per-prayer ± minutes adjustment** | Solves every variation — convention, safety margin, congregation time — without the app ruling on any of them | Moderate: new setting, saved, synced to the watch |
| Add a method question to first-run | Makes the existing fix reachable | Small: one more screen |
| Change the default automatically by latitude | Fixes it for everyone silently | **Rejected.** The app would adopt a religious position for the user without telling them |

The per-prayer adjustment is the strongest candidate, because it needs no ruling from us and
also absorbs the two-to-five minutes of *ihtiyat* (safety margin) that mosques add. It does
not remove the need to make the method findable.

### 2. Automatic location shows the county, not the town

**The problem.** It shows "Berkshire" instead of "Slough, Berkshire, UK".

**What it is not.** It is **not** making the times wrong, and this was measured before
anything was planned, because the obvious fix would have bought nothing:

| Where the app thinks you are | Isha |
|---|---|
| Slough, exact | 21:59 |
| 3 km away | **21:59 — identical** |
| Newbury, 60 km, the far side of the county | 22:01 |

A kilometre is worth about **five seconds** of prayer time here. Being wrong by the whole
width of Berkshire costs two minutes. The hour-plus error the tester was feeling is fix 1.

**What is actually wrong.** The code takes the first name it finds and stops, so it can only
ever show one word — never "Slough, Berkshire, UK". And when it asks for the town and gets
nothing back (common for unitary authorities like Slough) it silently falls back to the
county with no sign that it has. Two fields that usually survive when the town does not are
not being checked at all.

**The harm is trust, not accuracy.** A user shown his county assumes the app cannot find him
and stops believing times that are correct to the minute.

**The work, smallest first:**

1. Check the two missing name fields before falling back to the county.
2. Build the label from what survives — "Slough, United Kingdom" rather than one word.
3. Add one honest line underneath: *"Approximate location is enough — prayer times change by
   under a minute within a few kilometres."*
4. Make typing your own town obvious. **It already exists and already works** in Settings —
   it is just invisible to anyone who has stopped trusting the automatic name.

**Ruled out: precise GPS location.** It would cost privacy and buy zero minutes.

### 3. The first-run screens do not say which button to press

**The problem, as reported by a tester:** you cannot tell whether to tap a choice or press
Next.

**Confirmed in the code**, and it is worse than reported. Of the five first-run steps:

| Step | Behaviour |
|---|---|
| Welcome | Has a **Begin** button ✅ |
| Location | Has a **Continue** button ✅ |
| Sect | **No button.** Tapping a choice jumps you forward |
| Madhab | Tapping a choice jumps you forward — **while also showing Back and Skip** |
| Confirm | Has a finish button ✅ |

Madhab is the worst case: the screen visibly has buttons, so people reasonably wait for a
Next that does not exist, then tap a card and are thrown forward before they have decided.

**The fix is consistency, not cleverness.** Selecting only selects. Every step gets an
explicit forward button.

---

## The order we actually build them

Different from the order above, on purpose:

1. **Fix 3 first** — the first-run navigation.
2. **Then fix 1** — if a method question is added, it lands on those same screens. Fixing the
   navigation first means not doing it twice.
3. **Then fix 2** — independent of both, can slot in anywhere.

Then: one update to the closed track, one Google review, and **Wear OS after the phone
reaches production** — not before, because it triggers a second review for no gain.

---

## What we check before saying it is done

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then, because compiling is not working:

- Run it on the phone **and** watch emulators and compare them.
- `./gradlew installRtl` — check every changed screen right-to-left before it ships.
- Re-verify times against Aladhan, and **on two dates, December and June**, not one. See the
  open question below.
- Retake `docs/store/screenshots/` if anything visual changed.

## The honest gaps

- **One date proves one date.** Whether these mosques use Moonsighting all year, or switch
  convention in midsummer, is unknown. Midsummer is exactly where high-latitude timetables
  diverge. Two more checks — one in December, one in June — would settle it, and that should
  happen **before** fix 1 changes anything, not after.
- **Diamond Road publishes no start times** anywhere found, so only its congregation column
  could be read. Its apparent 47-minute disagreement with JMIC is almost certainly this and
  nothing more.
- **Three mosques in two towns is a small sample** for a decision about a default. It is
  enough to prove the current default is wrong for this user; it is not enough to prove which
  one is right for Britain.

## What we are deliberately not doing

- Not machine-translating anything. Prayer and madhab names are religious content.
- Not adding precise location. Zero minutes of benefit, real privacy cost.
- Not calling any API at runtime to fetch times. The app works with no signal, permanently.
- Not silently shifting times to close the gap between the app and a mosque noticeboard —
  that gap is usually the difference between when a prayer becomes due and when the
  congregation is held, and it is supposed to be there.

---

*Made with love, free for the Ummah.*
