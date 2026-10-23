# SajdaTime 1.6.0: UX guidance (Fable 5, independent, 23 Oct 2026)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

> **SUPERSEDED IN PART (23 Oct 2026, after the design pause):** the tracker is now its own tab "My prayers" (not ticks on Times, not "no new tab"); each prayer row gains a one-tap bell; the timetable button is renamed "Timetable" and opens a month sheet; the disclaimer is no longer shown at first launch. See BUILD-INSTRUCTIONS.md.

> **Signed off 23 Oct 2026 (Fable 5.1): read `SIGNOFF.md` first. It overrides this file.** Final order: 1 prayer tracker, 2 Islamic dates sheet, 3 jama'ah times on the Times screen (cut first if short of time). Reminders in 1.6.1. Submit by 15 Dec 2026, or all of 1.6.0 goes after Eid if 1.5.0 is not live by 27 Nov. The tasbih section near the end is a record only, not for building.

> **Read with `PLAN.md` section 0, which overrides this file where they differ:**
> - The row is called **"Jama'ah times"**, not "Mosque times".
> - The jama'ah reminder and the fasting reminders move to **1.6.1**, after Eid.
> - The fasting reminder is posted at Isha, not at Maghrib.
> - **Tasbih is not built.**

This guidance is grounded in:
- `PLAN.md` in this folder
- the Release B decisions
- `docs/DESIGN_SYSTEM.md` §4, §8, §9, §10 and §11
- the phone and watch store screenshots
- `HomeScreen.kt` and `SettingsList.kt`

The height figures are estimates, not measurements.

## The rule that holds it together

Nothing new appears on the Times screen unless the user put it there.

The one exception is the Hijri date, which is already on screen and becomes tappable. Everything else starts off in Settings, and Times only shows what the user has switched on or typed in.

There is no new tab and no onboarding step. The watch and the widgets do not change in 1.6.0.

## Height budget (decide before building)

`HomeScreen.kt` records that the content overran the S23 Ultra (823dp) by 37dp on 15 Aug 2026. The fix took 6dp off six rows. **Assume there is no spare height.**

| Addition | Estimated cost |
|---|---|
| Five jama'ah second lines at today's 12dp row padding | +90dp |
| The same lines, with 8dp padding on rows that have a second line | +50dp |
| Tracker line (streak and "Last 30 days") | +56dp |
| Hiding the plain "Different from your mosque?" door | saves 60dp |

**Recommendation:**
- Tighten the padding on rows that have a second line.
- Hide the plain mosque door once any jama'ah time is set. Settings keeps the route, and the amber far-north version of the door always stays. Ali has seen the door, so this is his decision (DESIGN_SYSTEM §0.2): ask him with screenshots.
- Measure the worst case on the 823dp frame: tracker on, five jama'ah lines, no banners. If it overflows, use the fallback and put the jama'ah time on the same line as the prayer.

**Row width.** On a 360dp phone, icon + "Maghrib" + Next pill + "11:01 pm" + a 48dp tick comes to about 300dp, against about 292dp available.
- Let the tick's touch area overlap the row's 18dp end padding.
- Never truncate the prayer name. The jama'ah line may wrap.

## 1. My prayers (tracker)

### Where it lives

A new Settings group, **My prayers**, between Reminders and About.
- Switching it on takes one tap.
- Once it is on, ticking happens straight from Times, with no extra taps.
- The history is one tap away.

### Settings group, in order

- **My prayers** (switch). "Tick each prayer you pray. Only you can see it."
- **Show streak** (switch, off). "Show how many days in a row you have ticked all five."
- **Pause tracking**. "Your streak waits until you resume."
  - Opens a dialog titled "Pause from", with radio rows *Today* and *Yesterday*, a solid **Pause** button and the X to close.
  - No reason is ever asked.
- **Save to a file** and **Load from a file**. "Keep a copy for a new phone."
- **Delete my prayer record**.
  - The dialog asks: "Delete your prayer record? This removes every tick from this phone. It cannot be undone."
  - Buttons: **Delete** (error colour) and Close.
- When the switch is off but a record exists, only the delete row stays.

### Times screen with the tracker on

```
│ ☀ Dhuhr     [Now]      1:14 pm   ◯ │
│ ◐ Asr       [Next]     5:13 pm   ◌ │   ◌ = not due yet, faded, disabled
└────────────────────────────────────┘
  12 days in a row          [Last 30 days]
```

- **The tick:**
  - a 24dp mark inside a 48dp target
  - toggleable, with the ToggleOn and ToggleOff haptics
  - Sunrise has no tick
- **Unticked past prayers** use the plain outline circle. Never red, never counted, never a badge.
- **The streak** shows only when Show streak is on and the count is above 0.
  - Say "12 days in a row", not "12 day streak". It reads less like a game.
  - When a streak ends, the number just goes. There is no message.
- **While paused:**
  - The ticks are hidden.
  - The tracker line becomes "Tracking is paused" with a compact **Resume** button. It takes the same height.
- **The first time it is switched on**, one snackbar: "Ticks are on the Times screen." Nothing else, ever.

### History sheet (full-height bottom sheet)

```
Last 30 days                              ✕
        Fajr Dhuhr Asr Maghrib Isha   (prayer glyphs as the header)
┌───────────────────────────────────────────┐
│ Today, Fri 23 Oct   ✓  ✓  ◯  ◌  ◌  [Pause] │
│ Thu 22 Oct          ✓  ✓  ✓  ✓  ✓  [Pause] │
│ Wed 21 Oct          Paused        [Resume] │
└───────────────────────────────────────────┘
```

- Newest day first. Past days can be edited. There is no year grid.
- **Record from a newer version** (read only):
  - A quiet Notice: "This record is from a newer version of SajdaTime. Update the app to change it."
  - Every control is disabled.
- **Isha after midnight** is ticked on the previous day's row (the B4 rule).

### Save and load messages

| When | Message |
|---|---|
| Saved | "Saved to [file name]." |
| Before saving | "This file is a personal record of your prayers. Keep it private." |
| Loaded | "30 days added." (with plural forms) |
| File cannot be read | "This file could not be read. Choose a file saved by SajdaTime." |
| File is too new | "This file needs a newer version of SajdaTime." |

### Accessibility and layout

- **TalkBack:**
  - Each tick is its own stop: "Fajr, prayed", "Fajr, not ticked", "Isha, not due yet".
  - In the history: "Thursday 22 October, Asr, prayed".
  - Pausing announces "Tracking paused".
- **Font scale 1.5:** rows grow and the tick target stays 48dp. In the history, the ticks wrap under the date.
- **Right to left:** the tick sits on the left. The history runs Fajr to Isha from right to left. Arrows use AutoMirrored.

### Must never happen

- The words "missed", "excused" or "qada", or any count of unticked prayers.
- A reminder to tick, a notification action, a widget change, or a tick on the watch.
- A streak shown at zero, or any "you broke it" moment.
- Any usage event.

## 2. Mosque times

### Where it lives

- **Settings → Prayer times → Mosque times**, directly above Match your mosque.
  - Its subtitle reads "Off" or "3 prayers set".
  - It is two taps from Settings.
- **The more important route:** the existing "Different from your mosque?" dialog already explains that the mosque board shows the congregation time. Add an ExplainerAction there: **Add your mosque's jama'ah times**.

### The Mosque times screen

```
Mosque times
If your mosque changes its times with the seasons,
choose "Minutes after it begins".
┌──────────────────────────────────────┐
│ Fajr     15 min after it begins    › │
│          Today 5:25 am               │
│ Dhuhr    1:30 pm                   › │
│ Asr      Off                       › │
│ Maghrib  5 min after it begins     › │
│ Isha     Off                       › │
│ Jumu'ah  1:15 pm                   › │
└──────────────────────────────────────┘
Reminder
│ Remind me before jama'ah    Off    › │
```

- **Tapping a prayer row** opens a dialog with three radio rows: *Off*, *A fixed time*, *Minutes after it begins*.
  - A fixed time reuses the "board time" clock from Match your mosque.
  - Minutes reuses the kind C − and + steppers: 0 to 60, in steps of 5.
- **The subtitle** always shows "Today [time]", so the user can see what the rule produces.
- **Jumu'ah** offers only Off or a fixed time.

### On the Times screen

- A second line under the prayer name, in labelMedium:
  - "Jama'ah 1:30 pm".
  - On Fridays, under Dhuhr: "Jumu'ah 1:15 pm".
- If Jumu'ah is off, Friday shows nothing under Dhuhr. Never fall back to Dhuhr's jama'ah time.
- **In Ramadan** the Fajr line reads "Suhoor ends · Jama'ah 5:45 am": one line, never two.
- **TalkBack** reads "Dhuhr, 1:14 pm, Jama'ah 1:30 pm".

### The guard

This covers a fixed time that falls before the prayer begins, or after the next prayer begins.
- **On Times**, the second line reads "Check your jama'ah time". It keeps the same height and adds no card. No time is shown and no reminder is sent.
- **On the Mosque times screen**, an amber warning Notice appears under that prayer: "Your Asr jama'ah time is before Asr begins. Check your mosque's times."
- **While the user enters the time**, the clock dialog shows the same message with today's start time ("Asr begins at 4:52 pm today"). The time still saves, because the user may know better.

### Reminder

- A notification only, on its own channel, "Jama'ah reminders".
- Title: "Asr jama'ah at 5:30 pm". Body: "In 15 minutes."
- Tapping it opens Times. It has no actions.

### Must never happen

- A jama'ah time shown earlier than the adhan.
- The alarm or the adhan used for jama'ah.
- Any change to the phone's Do Not Disturb.

## 3. Islamic dates and fasting days

### Entry points

**1. The Hijri date in the Home header.**
- Today the whole header is one target that opens the location sheet. Split it:
  - The city keeps its caret and still opens the location sheet.
  - The date line becomes its own 48dp target. The Hijri half is in `primary` (green means tappable), followed by a small caret.
- Take the extra height out of the header's 6dp padding and the 16dp gap below it, then measure.
- TalkBack: "Islamic dates. Today is 2 Rabi al-Awwal 1448", with the role of a button.

**2. Settings → Islamic dates**, available to everyone.
- For Sunni users it sits at the top of the Fasting days group.
- For Shia users it sits under Prayer times.

### The sheet

```
Islamic dates                                ✕
Today is 2 Rabi al-Awwal 1448
┌─────────────────────────────────────────────┐
│ Start of Ramadan              Mon 8 Feb       │
│ 1 Ramadan 1448                in 108 days     │
│ Laylat al-Qadr                The last ten    │
│                               nights          │
│ ...                                           │
└─────────────────────────────────────────────┘
Dates follow the Umm al-Qura calendar with your
date shift. Your local moon sighting may differ by a day.
[ Shift the date ]
[ Set fasting reminders ]      (Sunni, only while reminders are off)
```

- The next date comes first.
- A date that is happening now gets the existing "Now" pill, for example "Ramadan, day 15".
- Times read as "Today", "Tomorrow" or "in 12 days".
- No fast tags in the list, so it is the same for every sect.
- **Year out of range:** the sheet says "Islamic dates are not available for this year", and the header date is not tappable.

### Fasting days group (Sunni only, off by default)

- **Remind me** (switch), with three sub-switches:
  - *Mondays and Thursdays*
  - *The White Days (13th to 15th)*
  - *Special days*: "Ashura, Arafah, and the six days of Shawwal"

### The notification

It arrives at Maghrib the evening before, on its own channel, "Fasting day reminders". Tapping it opens the dates sheet.
- Normal case: "Tomorrow is a Sunnah fast (Monday)" / "Suhoor ends at 5:12 am."
- A White Day that falls on a Friday says "White Day", never "Friday".
- On the evening of 1 Shawwal: "The six days of Shawwal can begin tomorrow."

### Must never happen

- A reminder on Eid, on the Tashreeq days (including 13 Dhul Hijjah), or in Ramadan.
- Any fast reminder for a Shia user.
- Contested observances in the list, unless Ali decides otherwise (D4).

## 4. Tasbih (an option only; recommendation: not in 1.6.0)

If Ali wants it later, this is the version that adds least clutter.
- **Where:** one row in the My prayers group, **Tasbih after prayer**, shown whether or not the tracker is on.
- **What it does not get:** no tab, nothing on Times, no link from the ticks or from notifications.
- **A better home later:** the watch suits it better (taps on the wrist, haptics), but that needs a watch release.

```
                                      ✕
            سُبْحَانَ ٱللَّٰهِ
            SubhanAllah
            Glory be to Allah

                 17
               of 33
     (the whole screen is the tap target)
            [ Start again ]
```

- **Counting:**
  - Each tap gives a light haptic.
  - At 33 a stronger haptic, and the screen moves on to Alhamdulillah 33, then Allahu akbar 34.
  - At 100: "Done. May Allah accept it."
- **The free counter** is the same screen with no target. Its count is kept on the phone.
- **Start again** shows an Undo snackbar.
- **The screen stays on** while it is open. No permission is needed.
- **TalkBack** announces the count at 33, 66 and 100, not on every tap.
- **The Arabic phrases** are set as isolated text, so an English app stays left to right.

**Must never happen:** totals, history, goals, streaks or reminders.

## Small phones (320dp wide, about 640dp tall, or font scale 1.5)

- Times scrolls, which is already allowed. Nothing gets clipped.
- Order from top to bottom: header, next-prayer card, prayer rows (with jama'ah lines and ticks), tracker line, mosque door.
- The jama'ah line wraps. Ticks keep their 48dp target. Prayer names are never truncated.
- Test Indonesian and Turkish at 320dp, because they have the longest words.

## How a new user finds these, without being pushed

1. **Day one:** the green Hijri date on Times invites a tap and opens the dates sheet.
2. **From the dates sheet:** Sunni users see a quiet "Set fasting reminders" button.
3. **Mosque times:** found from the mosque dialog people already open when the times disagree with their mosque, and from Settings.
4. **My prayers:** found by browsing Settings, and from the 1.6.0 "What's new" text on Play. Nothing in the app announces it.
5. **Tasbih, if it is ever built,** sits quietly inside My prayers.

No feature asks for a rating or a share. The disclaimer's dua stays the only request in the app.

## Before handover

- **Ask Ali:**
  - whether to hide the mosque door (show him screenshots)
  - whether to build tasbih at all
- **Measure:**
  - the 823dp worst case
  - the 360dp row width
- **Update DESIGN_SYSTEM §4.1** with the new buttons:
  - Last 30 days
  - Resume
  - Pause
  - Add your mosque's jama'ah times
  - Set fasting reminders
  - Start again
