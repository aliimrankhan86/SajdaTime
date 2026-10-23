# Release 1.6: independent sign-off (Fable 5.1, 23 Oct 2026)

**This file is the final word on the order and scope of 1.6. Where `PLAN.md` (any section, including §0), `UX.md`
or the research file disagree with it, this file wins.** Asked for by Ali on 23 Oct 2026: review the plan again with
Fable 5.1 against the research, rank the features by value, say how they are delivered and how the UX can be simpler,
and sign it off. Nothing is built. 1.5.0 comes first.

The reviewer read the research, PLAN.md, UX.md, HANDOVER §11 and DESIGN_SYSTEM, and checked the claims against the
code on `claude/quiet-glass` (the 1.5.0 code that 1.6 builds on). **Every code reference in these plan files is to
`claude/quiet-glass`, not to `main`** (main is still 1.4.0, and this plan branch sits on main).

## Verdict

**Signed off with changes.** Order, highest value first:

| # | Feature | Release | Verdict | Why this place |
|---|---|---|---|---|
| 1 | **Private prayer tracker ("My prayers")** | 1.6.0 | Signed off with changes | The one reason to open the app every day. Pillars 11 of 40 reviews, Athan leads with streaks, Reddit users miss Muslim Pro's log by name, every 2026 indie app has one. Ali designed it. Honest limit: it keeps existing users, it will not by itself bring installs (the research's main finding is that discovery is the bottleneck). |
| 2 | **Islamic dates sheet** | 1.6.0 | Signed off with changes | **Moved up from 3rd.** Every user gets it with no typing or setting, it runs offline on `HijriCalendar` code that already exists, and before Ramadan it answers "when does Ramadan start, when is Eid". Forum count about 7 against about 8 for iqamah, so near equal demand, but by far the cheapest and safest piece. |
| 3 | **Jama'ah times on the Times screen** | 1.6.0 | Signed off with changes | **Moved down from 2nd.** Real value, but only for users who type their mosque's times and keep them current. MAWAQIT's 23 of 60 is about live times kept by mosques, which this is not, so that figure transfers weakly. Biggest UI piece and the only fit risk. **First to cut if time runs short.** |
| 4 | Jama'ah reminder and Sunnah fast reminders | 1.6.1, after Eid | Signed off, two conditions | (a) `RequestCodeTest` covers all six request code bands, including the 30,000 prayer reminder band it misses today (a 1.5.0 gap). (b) A rule for Isha after midnight at high latitude, so an "evening before" reminder never lands on the fast day itself (for example Maghrib plus a fixed gap when Isha is after 23:00). Decide before building. |

**Out of 1.6, agreed:** a "Prayed" button on the notification (Ali, 16 Oct), any missed or qada count or year grid,
silencing the phone (manifest promise), adhkar, duas, tasbih, Quran, adhan choice (a 1.7 candidate), jama'ah on the
widget or watch, the browser extension.

## Delivery

One set of gates. It replaces both sets in PLAN.md (§0 said submit about 15 Dec, §2 said 6 Jan).

- **G1:** 1.5.0 live for 14 clean days.
- **G2:** 1.6.0 pull request ready by **Fri 11 Dec 2026**. Ali submits by **Tue 15 Dec 2026**. That leaves a month
  before the release freeze (18 Jan to 9 Mar 2027, Ramadan starts 8 Feb 2027).
- **Hard stop:** if 1.5.0 is not live by **Fri 27 Nov 2026**, all of 1.6.0 moves to after Eid (from about 10 Mar 2027)
  and merges with 1.6.1 into one release. That is the safe outcome, not a failure.
- **Build order:** tracker, then dates sheet, then jama'ah display. Branch `claude/1-6-0` from `claude/quiet-glass`.
  Nothing reaches main before 1.5.0 is live.
- **Cut order:** jama'ah display first, then nothing else. A tracker plus dates 1.6.0, or even tracker only, is still
  worth shipping.

## Simpler UX (changes to UX.md)

**Tracker**
- The "Before saving" dialog becomes the Save row's subtitle: "A personal record of your prayers. Keep the file
  private." One tap fewer.
- No Pause or Resume chips on each day of the history sheet. One Pause entry, in Settings and on the Times line,
  starting today or yesterday (Ali's decision 2). Earlier unticked days are plain circles that count nothing.
- The tracker line ("12 days in a row", "Last 30 days") goes under the six prayer rows, so the rows stay where users
  know them.
- Copy kept: "Tick each prayer you pray. Only you can see it."
- New rule for the "must never happen" list: no tick or streak is ever a trigger for Google's review card.

**Islamic dates**
- The date line on the Times screen is its own tap target, Hijri part in `primary`, no extra caret. Measure the header
  (today it is one clickable Row, so splitting it is real work).
- One Settings row, "Islamic dates", under Prayer times, the same place for every sect.
- No "Set fasting reminders" button in 1.6.0 (reminders are 1.6.1). "Shift the date" is the only button.
- Six dated rows only, all agreed by both sects: 1 Muharram, Ashura, 1 Ramadan, Eid al-Fitr, Arafah, Eid al-Adha.
  Ranges (Laylat al-Qadr, the first ten days of Dhul Hijjah) are dropped from 1.6.0. Where a name is contested, the
  row shows the Hijri date itself.
- Footer: "Umm al-Qura calendar, with your date shift. Local moon sighting may differ by a day."

**Jama'ah times**
- No explanatory paragraph at the top of the screen. One line in the dialog under "Minutes after it begins": "Best if
  your mosque moves its times with the seasons."
- The guard shows in two places: the Times line "Check your jama'ah time" and the amber notice on the Jama'ah times
  screen. No live warning inside the clock dialog in 1.6.0.
- Board confusion, fixed now because two mosque entries sit side by side: the clock title
  `adjustment_pick_time_title` becomes "%1$s: the Begins time on your mosque's board", Match your mosque help gains
  "Jama'ah times go in Jama'ah times.", and the row becomes "Match your mosque start times".
- **The mosque door stays on the Times screen** (`HomeScreen.kt` 483 to 513 calls it the one door every user gets).
  Find the height from padding and the same-line fallback, and measure. Hiding it is Ali's call after screenshots.
  Reviewer's advice: keep it.
- The 25 km travel hide is cut from 1.6.0 (it stores a new coordinate, and Match your mosque is not hidden on travel
  either). Revisit for 1.6.1, for both or neither.

## Corrections to the plan files (to fix before anyone builds)

1. PLAN.md has two submission gates (§0 about 15 Dec, §2 6 Jan). The gates above replace both.
2. PLAN.md §2 cut line still puts reminders in 1.6.0 and dates first. Superseded by the order above.
3. PLAN.md §3 to §6 still carry 1.6.1 work (reminders, scheduler bands, channels) inside 1.6.0 sections, and the name
   "Mosque times". Read them as 1.6.1 and "Jama'ah times".
4. `RequestCodeTest` must prove six bands, not "four" (PLAN line 176). Today it covers three.
5. "Arabic shipped in 1.5.0" (PLAN §3) is only true once 1.5.0 is live.
6. Fasting reminder time is Isha (not Maghrib, PLAN line 203, UX line 244), with the after-midnight rule above.
7. Reminder codes top out at 33,665, not 33,669. The test uses the formula, not the comment.
8. The research file's "Prayed button on the notification" is not agreed (Ali, 16 Oct). Noted at its top.
9. UX.md's tasbih design (lines 255 to 284) is not for building. Noted at its top.
10. UX.md's per-day Pause chips and the "hide the door" idea are superseded above.

Not checked by the reviewer: Play review turnaround (the project's record says one to two days, leave a week), and the
823dp and 360dp fit, which stay estimates until measured.

## Tasbih counter: explicit verdict (Fable 5.1, 23 Oct 2026, asked for by Ali)

**Not signed off for 1.6.0 or 1.6.1, and not promised for any release.** A later candidate only, after 1.6.1 is
live, if Ali still wants it then.

- **Rank by value: fifth**, behind the tracker, dates, jama'ah and adhan choice. The research has no figure for a
  tasbih counter. The 27-source forum tally has no entry for tasbih or dhikr (iqamah about 8, calendar about 7,
  tracker about 5). Only indirect "Quran, duas" themes (Sadiq duas 6 of its sample). Every phone already has a
  counter app, and it brings nobody to the store.
- **Why not 1.6.0:** a fourth piece in a release with a hard pre-Ramadan deadline. **Why not 1.6.1:** that release is
  the scheduler change and stays small so a reminder fault is easy to find.
- **Effort low, religious risk medium:** the order differs by sect. Sunni: SubhanAllah 33, Alhamdulillah 33,
  Allahu Akbar 34. Shia, Tasbih of Fatimah: Allahu Akbar 34, Alhamdulillah 33, SubhanAllah 33. **UX.md's tasbih
  design draws only the Sunni order and would show a Shia user the wrong sequence**, and its "May Allah accept it"
  line goes. Technical risk low: nothing on the Times screen.

**If it is ever built, the simplest version:**
- One row in Settings, in the My prayers group: "Tasbih". No switch, nothing on Times, no notification.
- One screen: the phrase in Arabic, transliteration and meaning, one large count. The whole screen is the tap target.
  One button, "Start again". Light haptic each tap, stronger when a phrase completes, screen stays on while open.
- Order taken from the sect chosen at setup, never asked again. Shia title "Tasbih of Fatimah".
- Nothing stored: the count is gone when the screen closes. Never shown: a total, history, streak, goal, reward,
  hadith, reminder, share or rating.
- Words in full: "Tasbih", "Glory be to Allah", "All praise is for Allah", "Allah is the Greatest", "17 of 33",
  "Start again", "Done."
- TalkBack announces only when a phrase completes. Phone only (the watch needs a watch release and a sync key).

## Sign-off, in the reviewer's words to Ali

Ali, the plan is sound and I am signing it off with changes. Build 1.6.0 as three pieces in this order: your prayer
tracker first, the Islamic dates sheet second, and jama'ah times on the Times screen third, with the two reminders held
for 1.6.1 after Eid. I rank the dates sheet above jama'ah because it costs almost nothing, works for everyone without
typing anything, and is the safest thing in the plan. Nothing here breaks your rules: no network, no account, no new
usage counts, no rate or share prompt, no change to Do Not Disturb, and the disclaimer is untouched. The one date that
matters is 1.5.0 going live: if it is not live by the end of November, let all of 1.6.0 go after Eid and do not try to
squeeze it in. The only real design question for you is whether the mosque door stays on the Times screen when
jama'ah times are set. My advice is that it stays.

On tasbih, in the reviewer's words: the tasbih counter is not signed off for 1.6.0 or 1.6.1. The research shows no
measured demand for it, every phone already has a counter, and it would be the fifth feature in a release that
already has a hard deadline before Ramadan. If you still want it after 1.6.1 is live it can be a small later addition
on the terms above. If you want it anyway, say so and it goes on the list after 1.6.1, not before.
