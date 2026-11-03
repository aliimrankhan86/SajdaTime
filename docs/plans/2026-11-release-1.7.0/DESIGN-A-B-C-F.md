# Design handoff: A (time left), B (Duha row), C (hear it), F (midnight row at 2x)

Fable (ux-lead), 3 Nov 2026, for the release after 1.6.0 (branch `claude/1-7-0`). Read and design only: nothing here
is built or approved. Each item: one recommended design, one alternative where a real choice exists, and what only Ali
can decide. Tokens are the names in `docs/design-system/tokens/`; gaps are `Space.*`; nothing below 14sp except where
Ali's shrink rule (DESIGN_SYSTEM §9, 30 Oct 2026) names it. Copy rules: short, UK English, no hyphens or em dashes,
no "Tap", never "missed", "late", "valid", "you can pray now".

What I verified in the code (3 Nov 2026): `PrayerRow` in `ui/home/HomeScreen.kt` (stacked layout from 1.5x text puts
pills and time under the name AND under the note); `Night` in `core/NightTimes.kt` (sunset to Fajr, both sects);
`PrayerEngine.currentPrayer` (last slot at or before now, sunrise included, so none between sunrise and Dhuhr; window
ends are deliberately not modelled, HANDOVER 5.8a); `Sky.endOf` (already names a period end: sunrise, Asr, Isha, Fajr);
`SoundDialog`, `SoundChoices`, `RadioRow`, `PlayOnSilentRow` in `ui/settings/SettingsScreen.kt`; the alarm channel
plays with `USAGE_ALARM` (`notify/Notifications.kt`); strings `night_*`, `settings_night_times*`, `duration_h_m`,
`settings_value_pair`. Assumed, not run: nothing was built or screenshotted for this spec.

---

## A. Time left in the current prayer

### Recommended: a second line on the "Now" row

**Where.** The row that carries the "Now" pill gets its note line (the slot jama'ah and "Iftar" already use):
`labelMedium`, `onSurfaceVariant`, under the prayer name, exactly as `rowNote` draws today. Nothing is added to the
hero card: the hero answers "what is coming", the Now row answers "what is open", and the number belongs beside the
word "Now". No new surface, no new height at 1.0 text (the 50dp row floor already holds a second line).

**What it says.** `S:current_until`: "Until %1$s · %2$s", e.g. **"Until Maghrib · 2h 14m"**, **"Until Sunrise · 48m"**,
**"Until Islamic midnight · 1h 05m"**. The first argument is the label of the next row in the list below the Now row;
the second is `TimeFormat.remaining` style ("2h 14m", "48m"). The separator comes from `S:settings_value_pair` as
everywhere. Under one minute: "1m" (minutes rounded up, never "0m", never seconds; the row flips when the time passes).

**Where the window ends (the rule, in `:core`, plain Kotlin, new `CurrentWindow.endOf(...)`).** The end is the next
time the user already sees in the list, so the line never states a ruling the screen does not already imply:

| Now row | Line counts to | Why |
|---|---|---|
| Fajr | Sunrise | The list's next row; `Sky.endOf(DAWN)` already uses it |
| Dhuhr | Asr | Next row |
| Asr | Maghrib | Next row |
| Maghrib | Isha | Next row |
| Isha, night times ON, before Islamic midnight | Islamic midnight | The next row on screen |
| Isha, after Islamic midnight, or night times OFF | Fajr (tomorrow's, from `NextPrayer`) | The next row on screen; HANDOVER 5.39 forbids "Isha ends" wording, and "Until Fajr" is a fact |
| Sunrise to Dhuhr | no line (no Now row) | `currentPrayer` is null, as today |

Isha, justified: the app already shows Islamic midnight as the next row after Isha, and `Night` measures to Fajr for both
sects (Ali, 2 Nov 2026). Counting first to midnight and then to Fajr says only "the next time on this list", which is
what the user asked for, and matches both the Shafi'i and Shia preference for midnight and the Hanafi and majority
permissibility until Fajr without naming either. **The words "Isha ends" never appear** (HANDOVER 5.39 rule).

**When.** Only while a row is Now. The Next row never carries it (the hero already counts to it). When the Now row also
has a note (jama'ah, Jumu'ah, Suhoor ends, Iftar) the two share the one line, note first, joined by " · ", as
`rowNote` already pairs Ramadan and jama'ah (UX.md: never two note lines). At 360dp a long pair wraps one line
earlier, which STEP-7-DESIGN §3 allows. A jama'ah "Check your jama'ah time" line wins and the time left is dropped for
that row (two warnings on one line is noise).

**Updates.** Once a minute, keyed on `now.epochSecond / 60` like the sky progress, never per second. Not animated
(motion rule: countdown digits never animate).

**Light, dark.** Same inks as the note today: `onSurfaceVariant` on the `surfaceContainerHigh` Now fill (an asserted
pair in `ColorContrastTest`). No new colour.

**2x text, 320dp.** Stacked row (F below fixes the order): name, then pills and time, then this line, which wraps at
whole words. The line is user enlarged text, so it wraps rather than shrinks (Ali's rule, 3 Nov 2026). Never clipped.

**Urdu, Arabic.** The string is translated whole; " · " comes from `settings_value_pair`; the duration digits stay
Western (`ar-u-nu-latn`); the prayer label is the row label string, so no bidi isolate is needed. Layout mirrors with
the row.

**Accessibility.** The row stays one TalkBack stop. Its spoken description becomes `S:current_row_a11y`:
"%1$s, current prayer, %2$s, until %3$s, %4$s" with the duration in words via plurals (`S:duration_hours_minutes_a11y`:
"2 hours 14 minutes", "48 minutes"), because "2h 14m" reads as letters. Not a live region (it changes once a minute).

**Strings (English).**
- `current_until`: "Until %1$s · %2$s"
- `current_row_a11y`: "%1$s, current prayer, %2$s, until %3$s, %4$s"
- `duration_a11y_h_m` (plurals): "%1$d hours %2$d minutes" with one and other forms; `duration_a11y_m`: "%d minutes"

**States.** No location: no list, nothing. Makkah fallback: shows, like every row. Approximate (polar) day: shows; the
Approximate notice already covers it. Tomorrow after Isha when `NextPrayer` is null (no tomorrow computed): no line.
Night row hidden by the toggle: Isha counts to Fajr.

**Rejected.** A line under the hero countdown ("Asr for another 2h 14m"): clutters the approved hero and puts two
countdowns 20dp apart. A line in the "Now" pill ("Now · 2h 14m"): the pill grows past its 48 slot at 1.5x and squeezes
the time. A progress bar along the row: decoration that implies a ruling about the window, and Quiet Glass has no bars.

**iPhone effect:** carries over (rule in `:core`; the row note is a `Text` under the title in a `List` row).

### Alternative A2 (Ali's call): Isha always counts to Fajr
Ignore Islamic midnight for the line even when the night row shows. Simpler and one rule for everyone, but then the
Isha row says "Until Fajr · 7h 02m" directly above a midnight row, and a user who follows the midnight view reads the
app as contradicting itself. I recommend A, not A2.

**Only Ali can decide:** A or A2 (religious content: which end the Isha line counts to).

---

## B. Optional Duha row

### Recommended: one dimmed row after Sunrise, off by default, Sunni users only

**Row.** Between Sunrise and Dhuhr, `PrayerRow` with `quiet = true` (same treatment as Sunrise and the midnight row:
`surfaceContainerLow` fill, `onSurfaceVariant`, `bodyMedium` name, time at 15sp, 18dp icon, no bell, a 48dp spacer so
the time stays in column). Never Next, never Now, no alert, not in the PDF, widgets, watch or notifications (the night
times precedent, 2 Nov 2026).

- Label `S:duha_row`: **"Duha"**. Time: the window start (`PrayerTimeTextStyle` at 15sp), e.g. "07:52".
- Note `S:duha_until`: **"Until %1$s"**, e.g. "Until 11:48" (the window end), non breaking space inside the time as
  the midnight note does. Hanafi users know this as Chasht; the Settings subtitle names it so the row label stays one
  short word in every language.
- Icon: `Icons.Outlined.WbSunny` (outlined, 18dp, so it is visibly the quiet cousin of Dhuhr's filled `Rounded.WbSunny`).
  SF Symbol `sun.max`. Shared with Dhuhr the way `moon` is shared by Fajr, Eid and midnight; add the row to
  `09-ios-mapping.md`.
- Spoken `S:duha_row_a11y`: "Duha from %1$s until %2$s." One stop.

**Rule (`:core`, plain Kotlin, new `DuhaWindow`).** Start = sunrise + START minutes; end = Dhuhr − END minutes; both
from the day's own sunrise and Dhuhr as the list shows them (user adjustments included), cut to the minute like night
times (the safe direction: a later start, an earlier end). Hidden when sunrise or Dhuhr is missing, when end is not
after start (very high latitudes), or on a day flagged approximated for sunrise.

**Proposed minutes (PROPOSED, Ali must approve; religious content).**

| | Recommended | Who uses it |
|---|---|---|
| Start | **20 minutes after sunrise** | Darul Ifta Deoband (Hanafi) uses 20 as its benchmark; AskOurImam (Hanafi) says 12 to 15 but advises 20 "cautiously"; the "spear's length" of the hadith is put at 15 to 20 by SeekersGuidance and islamqa.info |
| End | **15 minutes before Dhuhr** | masjidbox and several UK timetables use about 15; Ibn Uthaymin (quoted on islamqa.info and Wikipedia) says 10 before Dhuhr; the sun's decline (zawal) is the end for every Sunni school |

Both figures are the cautious end of what reputable Sunni sources give, so a user who follows the row is inside every
quoted opinion. Alternative B2: **15 after sunrise, 10 before Dhuhr** (Ibn Uthaymin's own numbers, the widest window).
No school specific table is offered: the sources disagree by minutes, not by school, apart from whether Ishraq and Duha
are one prayer or two (subcontinent Hanafi practice treats them as two). We show one window and name no ruling.

Sources read on 3 Nov 2026: islamqa.info answer 22389; islamqa.org Hanafi answers 26388, 78735, 118433 (Deoband and
AskOurImam); seekersguidance.org "When can one pray Salat al Duha"; en.wikipedia.org/wiki/Duha; masjidbox.com Duha
guide. None gives an authority table by madhab; Aladhan has no Duha endpoint, so the test compares our sunrise and
Dhuhr to Aladhan's and checks the offsets by arithmetic.

**Shia users: the row and its setting are not shown.** Classical Shia scholars (al-Murtada, al-Tusi, al-Hilli, cited on
iqraonline.net; an al-islam.org answer calls the prayer an innovation) hold Duha is not legislated. Showing a Shia user
a Duha row would be the app taking a side. Precedent: Fast reminders are Sunni only. Ali's call if he disagrees.

**Settings.** Prayer times group, directly after Night times, `SettingsItem.Toggle`, icon `Icons.Outlined.WbSunny`,
default off, Sunni only:
- Title `S:settings_duha`: "Duha"
- Subtitle `S:settings_duha_desc`: "Shows the Duha window (Chasht) after Sunrise: from 20 minutes after sunrise until
  15 minutes before Dhuhr" (the minutes come from the rule as format arguments, so a change of numbers changes the
  words). This is the "why these times" line for the row: the rule is on the screen where the switch is, the same way
  the night toggle's subtitle names what it shows. No new caption under the list, nothing in the mosque dialog.

**Light, dark.** Quiet row inks, already asserted. **2x text.** Stacked order from F (name, time, note), the note wraps.
**Urdu.** Label translated ("ضحی" in Urdu, Claude translates and checks, extra care as religious content); the row
mirrors like Sunrise. **No "forbidden" or "makruh" labels anywhere**, as asked; the Duha row simply has a start and an end.

**Cost.** One more row for those who switch it on, so the list needs a short scroll on a 1080x2400 phone for them (the
same cost the night row already carries). Off by default keeps the masses on the 1.6.0 screen.

**Rejected.** Two rows (Ishraq and Duha): doubles height and takes the subcontinent two prayer view over the majority
one. A "Zawal" or "no prayer" band before Dhuhr: that is the forbidden times feature Ali excluded. Minutes the user can
edit: a stepper invites nudging a religious figure; if a mosque differs, "follow your mosque" and the row off.

**iPhone effect:** carries over (plain Kotlin rule, a quiet list row, a Toggle in Settings).

**Only Ali can decide:** the minutes (20/15 recommended, or 15/10); the label "Duha" with "Chasht" in the subtitle; Sunni
only.

---

## C. "Hear it" preview in the sound dialogs

### Recommended: one solid compact button under the sound choices

**Where.** Inside `SoundDialog` (both Notification sound and Alarm sound), after the `SoundChoices` radio group and
before "Play on silent", separated by `Space.Group` above and `Space.Related` below. One row: a compact `CtaButton`
(40 drawn, 48 touch, leading 18dp icon, `actionLabelStyle`) at the start, nothing beside it. Under the button, one
`bodyMedium` `onSurfaceVariant` help line (gap `Space.Tight`, the label and its line). Not on each radio row: a play
control beside a radio makes two targets a finger apart and "select" and "play" get confused; the button plays
whichever sound is selected, so choosing then hearing is one natural order.

**What it says.**
- Idle: icon `Icons.Rounded.PlayArrow` (SF `play.fill`), label `S:action_hear_it`: **"Hear it"**.
- Playing: icon `Icons.Rounded.Stop` (SF `stop.fill`), label `S:action_stop`: **"Stop"**. Same button, swapped over
  `Motion.quick` (an icon swap), the label block swaps without letter motion.
- Help line, alarm dialog, `S:preview_alarm_help`: **"Plays at your alarm volume, like the alert will."**
- Help line, notification dialog, `S:preview_notification_help`: **"Plays at your notification volume, like the alert
  will."**
- Replaces the help line only while it applies, read from `AudioManager` when the dialog opens and on each tap:
  - alarm stream volume 0: `S:preview_alarm_muted`: "Your alarm volume is turned down, so you will not hear it."
  - notification dialog and the ringer is silent or vibrate: `S:preview_notification_silent`: "Your phone is on silent,
    so you will not hear it."
  These are the same rules the real alert follows (`USAGE_ALARM` ignores silent mode and follows alarm volume;
  `USAGE_NOTIFICATION` follows the ringer), so what the preview does is what the alert will do. "Play on silent" is not
  consulted by the preview: a preview is the user asking to hear it now, and the switch is explained on its own row.

**Behaviour.** Plays the selected sound through the same usage the alert uses (`USAGE_ALARM` and
`CONTENT_TYPE_SONIFICATION` for the alarm, `USAGE_NOTIFICATION` for the notification), with transient audio focus, from
its start. Stops when: Stop is tapped; the sound ends (built in adhan about 3 minutes, a phone tone a few seconds);
another radio is chosen (stop, do not auto play the new choice); the dialog closes; the app goes to the background
(`DisposableEffect`). No volume is changed by the app (Ali's word needed for that; not here). No new sounds, no haptic
on play. The phone picker route ("From your phone") previews the tone that row names; the default alarm previews
`resolveAlarmSound("")` as the channel does.

**Light, dark.** The one solid green button, white bold label, as every action (DESIGN_SYSTEM §4.1); add it to the
button inventory. **2x text.** The compact button wraps its label centred; the help line wraps. **Urdu.** Icon is not
directional, no mirroring. **TalkBack.** Button role; label "Hear it" or "Stop"; state change announced by the label
swap; the help line is read in order after the button.

**Rejected.** A speaker icon button alone: icon only is guesswork and §4.1 bans bare icon buttons. A 30 second cap: the
user chose to listen and can stop; Apple's 30 second limit applies to notification sounds on iPhone, not to a preview.
Auto play on selecting a radio: sound the user did not ask for, loud at alarm volume.

**iPhone effect:** carries over (`AVAudioPlayer` with the alarm or notification category; same button and line).

**Only Ali can decide:** nothing visual beyond approving the pictures. If he wants a volume hint beyond the help line
(a slider or a link to sound settings), that is a separate decision.

---

## F. Islamic midnight row at 2x text

**The finding (verified in code).** `PrayerRow` at `fontScale >= 1.5` draws, inside the name column: name, then the
note, then the pills and time. For the midnight row that is "Islamic midnight" / "Last third from 22:20" / "20:26", so
the midnight time sits under the last third caption and reads as the last third's time. The 1.0 screenshot
(`private/screenshots/review-1-6-0/times-light-night.png`) is right because the time sits at the row end there.

### Recommended: in the stacked layout, the time comes before the note, for the quiet rows

Order in the stacked column for a quiet row (Sunrise, Islamic midnight, Duha): **name, then the time, then the note.**
The time is the name's value and sits under it (`Space.Tight` relationship, as today); the note is a caption about
something else and goes last, one step away. So at 2x: "Islamic midnight" / "20:26" / "Last third from 22:20".

Why the shrink rule does not apply here: nothing overlaps or breaks; the lines are in the wrong order. The caption is
our own text, but its size here is the user's 2x, so by Ali's rule it wraps at whole words and is never shrunk. If a
future language makes the note longer than one line at 2x, it wraps; that is correct. (This is the collision Ali asked
to be told about: a caption of ours enlarged by the user. Wrapping wins. No shrink floor is needed.)

Prayer rows keep today's approved order (name, note, pills and time): their note (jama'ah, Iftar) is about the same
prayer and Ali approved that layout from the step 3 and step 7 pictures; re showing it is forbidden. A's line is a
note, so it follows the prayer row order.

**Light, dark.** No change. **1.0 text.** No change (the time is at the row end). **Urdu.** Mirrors; order unchanged.
**TalkBack.** The row's spoken description already says "Islamic midnight 20:26. Last third of the night from 22:20."
and is unchanged.

**Alternative F2 (Ali's call):** the same new order for every row, prayer rows included ("Fajr" / "Next 05:37" /
"Jama'ah 06:00"). One rule, but it changes the approved 1.5x prayer rows and needs his yes from pictures. I recommend
F, which touches only the quiet rows.

**Verification to run (not done):** emulator at 2.0 text, light and dark, Urdu RTL build, 320dp; the night row and the
Sunrise row (which has no note, so looks the same).

**iPhone effect:** none beyond the row order (Dynamic Type stacks the same way).

---

## Summary for Ali (decisions that are his)

1. **A:** after Isha, the line counts to Islamic midnight while the night row shows, then to Fajr (recommended), or
   always to Fajr (A2).
2. **B:** Duha minutes 20 after sunrise and 15 before Dhuhr (recommended) or 15 and 10 (B2); label "Duha", subtitle
   naming Chasht; shown to Sunni users only.
3. **C:** approve the button and lines from pictures; no volume control in this release.
4. **F:** quiet rows only (recommended) or every row (F2).

## Strings added (English), for the translation ledger
`current_until`, `current_row_a11y`, `duration_a11y_h_m` (plurals), `duration_a11y_m` (plurals), `duha_row`,
`duha_until`, `duha_row_a11y`, `settings_duha`, `settings_duha_desc`, `action_hear_it`, `action_stop`,
`preview_alarm_help`, `preview_notification_help`, `preview_alarm_muted`, `preview_notification_silent`.

## Design system pages to update when built
`05-screens-and-features.md` §5.2 (rows 7, 7a) and §5.5 (Prayer times group, Sounds chooser); `03-components.md` Today
list and the `CtaButton` inventory in DESIGN_SYSTEM §4.1; `09-ios-mapping.md` (PlayArrow `play.fill`, Stop `stop.fill`,
Duha row `sun.max`); DESIGN_SYSTEM §9 with Ali's answers and the date; `docs/privacy.html` unchanged (nothing new is
sent or stored beyond two settings).

## Not verified
No emulator run, no screenshot, no TalkBack by ear. Duha figures come from web sources read today, not from a fiqh
reference book; Shia position from al-islam.org and iqraonline.net summaries, not from a marja's own site. Aladhan does
not publish Duha, so the planned test compares sunrise and Dhuhr only. The 2x midnight finding is read from the code,
not from a 2x screenshot (none exists in `private/screenshots/review-1-6-0`).
