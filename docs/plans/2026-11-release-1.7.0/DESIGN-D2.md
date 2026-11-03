# Design D2: the prompts without a card (Fable, ux-lead, 3 Nov 2026)

Design only; nothing under `app/`, `core/`, `docs/` or `.claude/` changed. Mockups in `mockups/D2/` from
`mockups/gen_d2.py`. Every PNG opened: nothing cut off or overlapping. DESIGN-D-E's rows are withdrawn.

## Applied

- `/apple-design` Feedback: status "in a passive way, near the items it describes"; Privacy: ask when the feature is
  used; Writing: a direct button, not a described setting; Settings: a problem row belongs there.
- `/ui-ux-pro-max` `ux`: errors inline, "connected to the field"; a badge spoken as one sentence.
- iOS itself: Weather marks a city that is not yours beside its name; Settings wears a dot on its tab; no panels.

## Three options

**1. Status rows card** (`O1-times-*.png`). A `surface` card under the hero: 56 rows, 32 amber tile, title, subtitle,
chevron ("Set your location / Times shown are for Makkah", "Allow exact alarms / Fajr may not ring on time"). Rejected: still a card in the slot; a chevron row is not a solid button; no X, so it nags for ever.

**2. Status in place (recommended)** (`O2-times-light/dark.png`, `-2x`, `O2-dialog-light.png`,
`O2-settings-light.png`). Nothing between the hero and Today. Each prompt sits on the thing it is about:

1. Location: `CtaButton(compact = true)`, `LocationOn` pin (SF `location`), "Set location", at the end of the city
   title row in the sky mark's place (`FlowRow` drops it under "Makkah" at 2x). Kind A compact, already in §4.1; one
   inventory row. Shows while `usingDefaultLocation`; never closable.
2. Exact alarms: the Fajr row gains a second line, `bodyMedium` Medium, `onAccentContainer` (7.39:1 on the Next
   highlight, 9.05 on surface; 11.27 and 13.84 dark), indented to the name: "May not ring on time"; an 8 `accent` dot on
   the bell. The row (not the bell) is a 48 target, role Button.
   It opens a dialog: "Allow exact alarms" with X (kind D), the existing `settings_exact_alarms_brief`, then "Android
   asks for this on its own screen. You can change it later in Settings.", a 52 full width green "Allow" (A). No "Close"
   text under it: the X is the one way out (Ali, 3 Nov 2026: two close controls made no sense; our dialogs always have an X). Closing sets today's `exactAlarmNoticeDismissed`: the line leaves Times, Settings keeps it.
3. Settings: an 8 `accent` dot on the Settings tab icon while a permission is withheld, spoken "Settings, needs
   attention"; the top `PermissionCard` becomes a row (tile, title, subtitle) with a compact solid amber "Allow" (kind
   B, 40 drawn 48 touch; a new §4.1 height line, Ali's word needed).

Why: each sentence sits where it is true; the Fajr risk is in words, on the screen read daily, before any action;
Makkah times are never unlabelled; the dialog reaches the system screen in one tap. iPhone: carries over (`Label` row,
`List` section, tab badge).

**3. One sheet, then the moment of need** (`O3-sheet-*.png`, `O3-after-light.png`). First open: a sheet "Two things to
finish", each item a sentence and a compact green button, X and Close; afterwards only the header button and the dot. Rejected: setup already asked both, so a second sheet nags (HIG Onboarding); alerts default on, so most users never
tap a bell and the Fajr sentence hides behind a dot.

## Other `Notice` uses that follow option 2

Notifications blocked (Times): the same second line, "Alerts are blocked", on the first row with a bell on; dialog
"Allow notifications". No location on Qibla and My prayers: one sentence, compact green "Set location" centred under
it. Settings exact alarms and Do Not Disturb: the row above. Still cards: Ramadan, Eid, polar, mosque difference,
compass, jama'ah, newer version.

## Translation impact

Five new strings in en, ar, bn, in, tr, ur: `home_fajr_exact_note` "May not ring on time";
`home_alerts_blocked_note` "Alerts are blocked"; `exact_alarm_dialog_more` (the Android sentence above);
`tab_settings_attention_a11y` "Settings, needs attention"; `home_fajr_exact_a11y` "%1$s, %2$s, may not ring on time".
Retired: `home_default_location_title` and `_body`. Reused unchanged: `settings_exact_alarms_title` and `_brief`,
`action_set_location`, `action_allow`, `notifications_off_title`. Ledger stamped in the same commit.

## Not verified

No device render; Roboto is wider than the Mac font, so the title row needs an emulator check. Urdu and Arabic not
rendered; pin, dot and chevron mirror with layout direction. TalkBack order depends on the build.
