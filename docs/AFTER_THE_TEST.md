# What we do after the closed test

**Plain-language plan, written 1 Aug 2026.** Nothing here is started, and nothing here
should be started until Google grants production access. The reasoning, evidence and
measurements behind every item are in [`HANDOVER.md`](HANDOVER.md) §10 and §11 (items T1,
T2, T3) — this file is the short version, in order, for someone who wants to know what
happens next without reading two thousand lines.

---

## First, what we are waiting for

The app is **live on the closed testing track** and approved. It is not in the store.

**The twelve-tester bar is cleared and the fourteen days are running.** Confirmed in the
Console on 1 Aug 2026 — the production-access checklist now shows:

| Criterion | State |
|---|---|
| Publish a closed testing release | ✓ done |
| **Have at least 12 testers opted-in** | **✓ done** |
| Run with at least 12 testers for at least 14 days | ○ in progress |

**Apply for production** stays greyed out until the fortnight completes.

**One thing to know, because it is a trap.** The email list holds **24 addresses** — that is
not the opt-in figure, and it never was. Google used to print the real count as a line of
italic text; the moment you pass twelve it replaces that line with a tick and **stops showing
the number entirely**. So for the whole fourteen days — the only period when dropping below
twelve would matter — there is no counter to watch and no warning if it slips. Nothing can be
done about that from inside the Console; the only defence is keeping your own list of who
confirmed they installed and left it installed.

Nothing in the code is blocking. See [`RELEASING.md`](RELEASING.md) Step 8.

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

**But this affects far fewer people than it first looked, and that was checked before
anything was planned.** The default was tested against the locally-dominant method in fifteen
major cities. **It is within about five minutes across South Asia, South-East Asia, the
Middle East and Africa** — which is where most of the world's Muslims live. It only fails in
the West, and only badly above about **45° of latitude**:

| Where | How far out the default is |
|---|---|
| Jakarta, Karachi, Dhaka, Delhi, Cairo, Kuala Lumpur | 3–6 minutes |
| Istanbul, Casablanca, Kano, Tashkent | exact |
| Riyadh | 7–15 minutes |
| Chicago | 11–18 minutes |
| **London, Berlin** | **20–47 minutes** |

**So we are not changing the default and not adding any new methods.** Indonesia's and
Malaysia's official methods are missing from the app and it does not matter — ours is within
four to nine minutes of both. Adding them would be effort for nothing.

**And no per-prayer adjustment setting is needed either** — which reverses what this file
said earlier today. When I ran the app's own engine rather than trusting the reference API,
SajdaTime on Moonsighting reproduces JMIC Slough **to within one minute on all six prayers,
matching Dhuhr and Maghrib exactly**. The small offsets I had put down to mosques adding a
safety margin turn out to be part of the method itself. There is nothing left for an
adjustment feature to fix.

**⚠️ What remains is one decision, and it is yours because it is religious, not technical:**

| Option | What it does | Cost |
|---|---|---|
| Leave it; explain it in Settings | Keeps a convention that is right for most of the world | Free, but northern users stay mismatched |
| **A one-time note for users above ~45° latitude**, offering the method choice | Reaches exactly the people affected, changes no time silently, adds nothing for everyone else | Small: one card that only appears when relevant |
| Ask everyone the method during first-run | Makes the existing option reachable | Small, but lengthens the flow for people who do not need it |
| Switch method automatically by latitude | Fixes it for everyone silently | **Rejected.** The app would adopt a religious position for you without saying so |

The one-time note is now the strongest. The objection to the rejected option was never that
a smart default is wrong — it is that a **silent** one is. Showing you the choice is not the
same as making it for you.

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

- **One date proves one date — half of this is now closed.** What *is* verified: the app's
  own Moonsighting output matches the reference to the minute in December, at the June
  solstice and in August, so the method behaves correctly all year, including at the one
  point where the app's high-latitude handling could have distorted it. What is **still
  open**: whether these mosques stay on Moonsighting all year or switch convention in
  midsummer. That needs their December and June timetables, not ours, and it should happen
  **before** fix 1 changes anything.
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
