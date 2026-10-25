# 7. Accessibility

Accessibility is how the app works for the masses, not an extra. The standard is **WCAG 2.1 AA**, and
on iPhone Apple's accessibility guidelines as well. What can be tested is tested.

## 7.1 Contrast (tested)

| Pair | Minimum | Enforced by |
|---|---|---|
| Text on its background | 4.5:1 | `ColorContrastTest`, both themes |
| Icons, chevrons, ticks, meaningful edges | 3:1 (WCAG 1.4.11) | `ColorContrastTest` |
| Button label on its fill | 4.5:1 | `ColorContrastTest` |
| Button fill against the surface behind it | 3:1 | `ColorContrastTest` (known gap: green on the dark quiet notice, about 2.32:1) |
| Every ink on every gradient stop, and under the sheen | 4.5:1 | `ColorContrastTest` (hero, warning, 72 sky pairs) |
| Tab bar labels over pure black and pure white behind the 97% bar | 4.5:1 | `ColorContrastTest` |
| Widget inks on every stop and sky | 4.5:1 | `WidgetColorsTest` |

`outlineVariant` is decorative and below 3:1 by design: never use it for anything that must be seen.
Add every new pair to the test in the same commit.

## 7.2 Never colour alone

Every state colour has a second signal:

| State | Signals |
|---|---|
| Next prayer | Fill, a 3dp leading bar, a "Next" pill (and bold on widgets) |
| Now | Fill and a "Now" pill |
| Qibla aligned | Sentence with a tick, thicker bezel, a buzz |
| Ticked | Filled circle and a drawn tick |
| Paused | Pause bars and the word |
| Warning | An icon, a title and a sentence |
| Selected tab | Filled icon, pill, bolder label |

**Known gap:** the watch marks the next prayer by colour alone.

## 7.3 Touch

- Every target at least **48dp** (Apple: 44pt), even when drawn smaller (compact buttons 40, ticks and
  bells 24).
- No two icon buttons side by side; opposite actions are never a finger apart.
- The whole row is the target for rows, switches and radios.
- **Known gap:** the widget town chip is 24 tall (the whole widget is also a target).

## 7.4 Text size

- Everything in sp (pt with Dynamic Type on iPhone). Checked at **1.0, 1.5 and 2.0×**, and at 320dp
  wide.
- From 1.5× rows stack; content scrolls; nothing clips; words never split (`WholeWords`).
- Floor 14 in the phone app (widgets 12, watch 12, with named exceptions). **Known gap:** 13 uses of
  undefined smaller styles (`03-components.md` §3.6).
- The time picker is capped at 1.3× inside its dialog because its clock face cannot grow further.

## 7.5 Screen readers (TalkBack, VoiceOver)

- **Headings**: screen titles, dialog titles, section titles and card titles are headings. Known gap:
  setup titles and two sub headings are not.
- **One stop per meaning**: a prayer row is one stop ("Fajr, Next, 5:37 am"); its bell is a second.
  Timetable days and Islamic dates rows are one stop each.
- **Live numbers are summarised**: the countdown's ticking digits are hidden and replaced by "Asr in
  2h 14m"; the Qibla sentence and stepper values are polite live regions.
- **Roles and states**: toggles are switches or checkboxes with spoken state; choices are radio buttons
  in a group; tabs are tabs; "Details" says expanded or collapsed.
- **Decorative drawings are hidden** (the dial, the legend); the sentence carries the meaning.
- **Widgets** speak one sentence for the whole widget; the town chip is its own button.
- **Not yet checked by ear** (HANDOVER §11). Do not claim TalkBack works until someone has listened.

## 7.6 Motion and haptics

No entrance animation, nothing loops, nothing faster than needed. Compose follows the system animation
scale; on iPhone honour Reduce Motion. Haptics only on the user's own tap, one pulse; the Qibla arrival
buzz is the one sensor driven pulse.

## 7.7 Language and reading

Plain words, short sentences, pictures that read without language (the Kaaba, prayer glyphs, the sky).
Right to left is first class for Urdu and Arabic. Times and digits are Western in every language so
they read at a glance.

## 7.8 Checklist for any change

- [ ] New colour pairs in `ColorContrastTest`
- [ ] No meaning by colour alone
- [ ] Targets 48 or more; no icon buttons side by side
- [ ] 2.0× text and 320dp wide: nothing clips, words whole
- [ ] Headings, roles, states, one stop per meaning
- [ ] Live values summarised for the screen reader
- [ ] Urdu RTL checked as layout
- [ ] What was not checked (by ear, on device) is written down
