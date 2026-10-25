# 4. Patterns

How components combine into screens, and the recurring situations every feature meets: navigation,
messages, choices, states, confirmation and undo. When a new screen needs one of these, copy the
pattern rather than inventing a variant.

---

## 4.1 Navigation

- **Four tabs, always labelled, fixed order:** Times, Qibla, My prayers, Settings. Times is the
  default. The bar floats (Quiet Glass). There is no drawer, no floating action button, no top app bar.
- **Tabs are siblings:** switching cross fades, never slides. Each tab keeps its own scroll.
- **Everything else opens over the tab** as a dialog or a bottom sheet. No pushed full screens in the
  main app. (Setup is the one full screen flow.)
- **Leaving:** every dialog the user can simply leave, and every titled sheet, has a visible close X. A question that must be answered (the usage question) has none, on purpose. System Back closes the top sheet or
  dialog; on a tab it leaves the app.
- **Hand off from Times to Settings:** a button on Times that changes a setting switches to the
  Settings tab and opens that one chooser (for example the mosque dialog's four buttons). Only one
  chooser is open at a time.
- **Doors, not walls:** an explanation sits behind a button or a fold; it never blocks the screen.

iPhone: `TabView` with four tabs; sheets with `NavigationStack` and an `xmark` close. Same shape, so
nothing needs redesigning.

## 4.2 Screen anatomy

```
Status bar (Times: the sky's top colour)
ScreenTitle           headlineMedium, 20 start, 12 above, 4 below
  (Times: the city with a green caret, the sky mark, the date line, on the sky)
Notices               each 16 above, in priority order
Content cards         16 side gutter, 24 radius, sections 24 apart
BottomBarClearance    the bar's measured height
Floating tab bar
```

Content is one column, capped at 600 and centred on wide screens. Times keeps the sky full width.

## 4.3 Messages: which one to use

| Situation | Pattern | Example |
|---|---|---|
| The phone withholds something the user asked for | Warning `Notice` with one "Allow" (permission card) | Exact alarms, notifications blocked, Do Not Disturb access |
| Something is not as expected but works | Warning `Notice` with an action | Times for Makkah, approximate times, jama'ah outside its window, compass needs calibrating |
| Information, an occasion, the user's own rule | Quiet `Notice` | Ramadan begins, Eid Mubarak, jama'ah hidden in another time zone |
| An action just happened and can be undone | Snackbar with "Undo" | Paused from today, Dhuhr alert off |
| A file was saved or failed | Toast (Android); a brief overlay on iPhone | Saved to Downloads as PrayerTimes_July2026.pdf |
| A field is wrong | Supporting text under the field, in words | We could not find that place |
| A long explanation | Folded Details, or an explainer dialog | Different from your mosque? |

**Never:** a blocking alert for information, a red message, a message that blames the user, two
notices saying the same thing, a notice that cannot be acted on and cannot be closed on Times.

Order of notices on Times: Makkah fallback, approximate times, Eid, Ramadan, exact alarms,
notifications. Closable notices stay closed on Times; the same problem stays visible in Settings until
fixed (a warning that cannot be put away is furniture; one that disappears everywhere is lost).

## 4.4 Choices

| Choice | Pattern |
|---|---|
| One of a short list that applies at once | Chooser dialog with radio rows; each tap saves; Close at the foot |
| One of a list that ends the task | Radio list that closes on pick (calculation method) |
| On or off | Switch row; the whole row toggles |
| One of three, visible result instantly | Sliding selector (theme) |
| Per item style (Off, Notification, Alarm) | Chips in a flow row, one row per item |
| A number in a range | Stepper with − and + |
| A clock time | The Material time picker, vertical layout, text capped at 1.3× |
| A first run decision | Full screen step with choice cards and Back / Continue |

Defaults must be right for someone who never opens Settings (Sunni, Shafi'i, Automatic method, all
five alerts as notification). The current value always shows as the row's subtitle.

## 4.5 States every screen must handle

Write each one into the spec before building. If a state cannot happen, say why.

| State | Pattern |
|---|---|
| Loading | Nothing flashes: draw nothing until settings load (they are local and fast). A running action shows `ProgressRow` and disables only its own button |
| First run | Setup has run; defaults apply; nothing asks again |
| No location | Never a blank screen. Qibla and My prayers: a warning notice "Set your location…" with "Set location". Times: the next prayer card says "Set your location to see prayer times." and the city chip opens the location sheet |
| Makkah fallback | Times for Makkah with a warning notice saying so |
| Permission missing | Permission card on Times (closable) and in Settings (permanent) |
| Offline | Nothing changes; times never need the network. Only place search says it needs a connection |
| Empty | Say what will appear and how ("Nothing is tracked until you switch this on.") |
| Paused or read only | Content stays visible; controls disabled with the reason in words |
| Error | Plain words, what to do next, no codes, no blame |
| Large text (1.5× to 2.0×) | Rows stack; content scrolls; nothing clips; words never split |
| Right to left | Mirrors, except clocks, the dial, rings and numbers |
| Polar latitudes | "Approximate" wherever a projected time appears |
| Ramadan, Eid | Seasonal content appears only in its window and can be hidden |

## 4.6 Confirmation, undo and destructive actions

- **Prefer undo to confirm.** A quick, reversible action happens at once with a snackbar and "Undo"
  (pause from today, a bell off).
- **Confirm only what cannot be undone**: deleting the prayer record. A dialog titled as a question
  ("Delete your prayer record?"), the consequence in plain words, one full width solid button
  ("Delete") and the X. Still green: there is no red, the words carry the warning.
- Never two step "Are you sure?" chains. Never confirm on leaving a dialog: choices are saved as made.

## 4.7 Forms

- Labels above or inside the field (Material outlined field), helper text with an example ("For
  example: Karachi, London, Makkah, New York").
- The action button appears only once there is something to act on.
- The keyboard's action key submits.
- Errors replace the helper text and say what to try.
- Avoid forms. One field at most per screen today.

## 4.8 Progressive disclosure

- **Fold** extra explanation behind "Details" (notices, Qibla) or a fold row (About).
- **Hide** a control that cannot be used yet rather than greying it out.
- **Seasonal** rows (suhoor reminder, Ramadan card) appear only from 3 days before Ramadan to its end.
- **Sect aware:** the madhab step and madhab choices only for Sunni users; Shia only dates only for Shia
  users. Never show a choice that does not apply.

## 4.9 Permissions

Ask in context, never at launch. Location when the user taps "Allow location". Notifications when
setup finishes (Android 13+). Exact alarms through a button that explains why. Do Not Disturb access
only when an alarm style is chosen. Before each, one sentence says what goes wrong without it. If
refused, the app still works and the permission card offers the door again.

iPhone: the same moments; location "When In Use" only, notifications on finishing setup. Exact alarms
and Do Not Disturb access have no iPhone equivalent (Time Sensitive notifications cover part).

## 4.10 Religious content

- Prayer names, madhab names and method names exactly as written in the strings; never "simplified".
- Never a ruling: no "valid", "must", "you can pray now". Explanations end with "follow your mosque".
- Show calculation honestly: "Approximate", "as calculated", "Local moon sighting may differ by a day".
- Sect specific content only for that sect. Nothing about belief is ever sent anywhere.
- The disclaimer is in About, first row, never softened. Its dua request is the only ask in the app.
