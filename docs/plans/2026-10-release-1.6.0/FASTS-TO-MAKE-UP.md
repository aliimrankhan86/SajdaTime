# Fasts to make up, moved into 1.6.0 (25 Oct 2026)

**Ali, 25 Oct 2026:** "what we can release in this, let's just go with that. We don't need to even add it into the
description if we have limited space. And maybe we don't even need Fable to design it ... we can design it basic for
now if needs to be, but follow the same theme of the app." He chose this over moving the two reminders (they stay in
1.6.1: alarm risk before the 18 Jan to 9 Mar release pause). So: **no Fable design pass, no store listing change, no
new store picture.** Built only from components the app already has (DESIGN_SYSTEM, Quiet Glass). Visual approval is
still Ali's, from screenshots.

Base: DETAILED-PLAN.md section 3 ("Recommendation: a count, no reasons, no dates") and its strings table. Where this
file differs, this file wins.

## What the user sees

1. **Settings, My prayers group, shown while My prayers is on**: one row "Fasts to make up", after "Show streak"
   and before Pause. Icon `Icons.Outlined.EventAvailable` (SF Symbol `calendar.badge.checkmark`). Subtitle: the
   count ("3 fasts to make up") when above zero, otherwise "Add one for each fast you still have to make up. Take
   one off when you make it up." Tapping opens the existing chooser dialog (title with X, Close at the foot) with
   that same sentence as a paragraph and one existing `StepperRow` (minus, number, plus) whose label is the short
   title, so TalkBack says "Fasts to make up, 3". Each press saves at once. 0 to 999, one step at a time. Hidden on
   a record from a newer version, whose writes `TrackerRepository.edit` refuses.
   ponytail: one step at a time; a typed number if anyone owes hundreds and says so.
2. **My prayers tab**, only while the count is above zero: under the week card, one line "3 fasts to make up"
   with a compact solid "Made up one" button at the end, in a FlowRow like the Pause row (wraps by measured width).
   Tapping takes one off. At zero the line goes, with no message, as the streak does. No Undo snackbar: a wrong tap
   is put back with plus in Settings. A record from a newer version (read only) shows the line without the button.
3. Nothing on Times, widgets, watch or notifications. No reminder, no deadline, no "before next Ramadan".

## Words (all five languages, by Claude, `TrackerWordingTest` covers them by key prefix)

Never "missed", "owed", "debt" or "qada" in English (`TrackerWordingTest` bans all four). **Arabic and Urdu use
قضا**, following the earlier recorded decision (DETAILED-PLAN.md §9 wording notes): there it is the plain word for
making up a fast, not a label of guilt; Turkish uses kaza for the same reason ("Kazası kalan oruçlar": Opus found
the neutral "Tutulacak oruçlar" read as any fast to come). Indonesian says "Puasa yang perlu diganti", never utang
(debt). (The first draft of this file said no قضا anywhere; plan-verifier found it
contradicted §9, and §9 was kept.)

| Key | English |
|---|---|
| `settings_tracker_fasts` | Fasts to make up |
| `settings_tracker_fasts_desc` | Add one for each fast you still have to make up. Take one off when you make it up. |
| `tracker_fasts_line` (plurals) | %d fast to make up / %d fasts to make up |
| `tracker_fasts_made_up_one` | Made up one |
| `tracker_delete_body` (changed) | This removes every tick and the fasts to make up count from this phone. It cannot be undone. |

## Data

- One Int in the tracker store (`fasts_to_make_up`), so "Delete my prayer record" clears it and it is outside backup
  like the rest. `TrackerData.fastsToMakeUp`, counted in `isEmpty` (so the Delete row shows when only a count is kept).
- Save file: one more field `"fastsToMakeUp": 3`, written only when above zero. File version stays 1: the field is
  optional and the tracker itself is new in 1.6.0, so no older file or app exists to break.
- Every change is `adjustFastsToMakeUp(+1 or -1)`, read and written in one DataStore edit (`TrackerLogic.adjustFasts`),
  so two quick taps are two steps (Opus review).
- Loading a file keeps the larger of the two counts (loading the same file twice never doubles it; a new phone at 0
  takes the file's). Trade off: an older save raises a count since lowered; the load message shows the new number.
- The load message names the count when the file changed it, so a count only file is not "0 days added".
- `docs/privacy.html` "Your prayer record": the count is named in what is kept and in what the file contains.
- No usage-count event.

## Tests

- `:core` `TrackerLogicTest`: `clampFasts`, `adjustFasts` and `mergeFasts` (the rules live in `TrackerLogic`, plain Kotlin).
- `TrackerFileCodecTest`: the count round-trips, absent means 0, a negative or huge value is clamped to 0..999.
- `TrackerWordingTest`: picks the new keys up by prefix (parity, banned words, no dash or semicolon).
- `TrackerPrivacyTest`: two more phrases, so privacy.html cannot lose the count.
- DESIGN_SYSTEM 4.1 inventory: two new rows (the Settings stepper and "Made up one").

## iPhone effect

Carries over: a plain Int and one JSON field; the stepper and the line are patterns both platforms share.

## Not done here

- Bulk entry (DETAILED-PLAN §3 named it as a user complaint): one step at a time only, Ali asked for basic. A
  long press that repeats is the cheap upgrade if anyone owes many fasts.
- No Undo after "Made up one"; plus in Settings puts a wrong tap back.


Store listing and pictures (Ali: not needed). Fable's design pass (Ali: not needed). The release script's
`EXPECTED_COUNTS` moves with the string and test counts in the same commit.
