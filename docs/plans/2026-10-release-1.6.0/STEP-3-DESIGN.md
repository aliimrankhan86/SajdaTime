# Step 3: Bell with Undo, design (Fable `ux-lead`, 24 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 3 only.

Written before any spec or code, as Ali's process asks: journey and design first, then `plan-verifier`, then
pictures for Ali's yes, then the build. No skill was invoked: the design system (§4.1, §9 item 2), Ali's Q6 and the
shipped `Tick` pattern decide nearly everything; the two Apple HIG checks applied by hand are the list row toggle
(Reminders, Clock) and the notification permission ask (prompt once, then a door to the phone's settings).
**Status: a recommendation. Nothing is decided until Ali says yes from the pictures.**

## 1. Recommendation

One bell at the end of each of the five prayer rows on Times (Sunrise keeps a blank). One tap turns that prayer's
alert off, with a four second message "Dhuhr alert off" and Undo; one tap turns it back on, restoring the style it
had (notification or alarm) and the "minutes before" reminder the bell removed, with no message. The bell shows its
state by shape (filled bell, alarm clock, crossed bell) and colour. When the phone blocks notifications, turning a
bell on asks for the permission; when the phone will no longer ask, it opens SajdaTime's notification page in the
phone's settings. One addition to the plan for Ali to decide (§13): an amber notice on Times, "Allow
notifications", while notifications are blocked, because a user who refused them at setup today sees five green
bells and never learns why nothing arrives.

## 2. The journey, numbered

Words in quotes are on screen.

1. **A normal day.** Times, six rows: glyph, name, Now or Next pill, time, then the bell. All five filled green (the
   default alert, a notification). Sunrise: no bell, the time ends where the others do.
2. **Turning Dhuhr off.** Tap Dhuhr's bell: it crosses out (`Quick`), one ToggleOff buzz, and the message "Dhuhr
   alert off" with "Undo" appears above the tab bar for four seconds. Settings, Prayer alerts now reads "Mixed" and
   Dhuhr shows "Off" in the dialog (same setting, two doors).
3. **Changing your mind.** "Undo": the bell snaps back filled, no animation, no buzz, no second message. Everything
   the tap removed is back.
4. **Turning it on later.** Tap the crossed bell: it fills (`Quick`), one ToggleOn buzz, no message. Dhuhr is a
   notification again; if it had been an alarm before, it is an alarm again.
5. **A reminder was on.** With "Reminder before prayer" set to 10 minutes and Dhuhr ticked there, the tap says
   "Dhuhr alert and reminder off". On, or Undo, brings the reminder back too (Ali, Q6).
6. **Fajr in Ramadan.** With the suhoor reminder on, turning Fajr off says "Fajr alert off. Suhoor reminder stays
   on." The suhoor reminder is its own setting and never follows the bell (FABLE-REVIEW).
7. **Notifications blocked.** The bells draw their stored state but in grey, the notice (§6) explains, TalkBack says
   why. Turning a bell on asks the system; if the system will not ask again, SajdaTime's notification page opens in
   the phone's settings. Back in the app with notifications allowed, the bells go green and the notice goes.

## 3. The bell, every state

Material icons from `material-icons-extended` (installed), SF Symbol beside each. None points a direction; the slash
on the off bell is not mirrored in right to left (Material does not mirror it and a crossed bell reads the same both
ways).

| State | When | Icon | SF Symbol | Colour | Spoken (TalkBack) | A tap does |
|---|---|---|---|---|---|---|
| On, notification | `alertFor[slot] == NOTIFICATION` | `Icons.Rounded.Notifications` (filled bell) | `bell.fill` | `primary`; on the Next row `onPrimaryContainer` | `bell_on_a11y` "Dhuhr alert on, notification" | off, message with Undo |
| On, alarm | `alertFor[slot] == ALARM` | `Icons.Rounded.Alarm` | `alarm.fill` | as above | `bell_alarm_a11y` "Fajr alert on, alarm" | off, message with Undo |
| Off | slot absent from `alertFor` | `Icons.Outlined.NotificationsOff` | `bell.slash` | `outline` on every row | `bell_off_a11y` "Dhuhr alert off" | on, no message |
| Blocked | stored on (either style) and the phone cannot post | the stored state's icon | as that state | `outline` | `bell_blocked_a11y` "Dhuhr alert on. Notifications are off on this phone." | off, message with Undo (the setting is still the user's) |
| Blocked, off | stored off and the phone cannot post | crossed bell | `bell.slash` | `outline` | `bell_off_a11y` | stores on, then asks for the permission (§6) |

Why the plain filled bell and not `NotificationsActive`: the sound waves say "ringing now", and the crossfade in row
4 of the animation table reads as one object gaining a slash only when on and off share a silhouette. Alarm keeps
its own shape (§9 item 2: shape as well as colour). Blocked differs from on by colour only; that is acceptable
because the notice and the spoken text carry the meaning, and shape still separates on from off.

**Placement and size:** 24dp icon in a 48dp target at the row's end, inside the card, overlapping the 18dp end
padding as the tick did (`padding(start = 18.dp, end = 6.dp)`, vertical 1dp with a bell so the 50dp row floor
holds: `e98b2a6` `HomeScreen.kt:938-946`). Sunrise: `Spacer(48.dp)` so its time lines up. The bell is its own
`toggleable(role = Role.Switch)` node with the spoken string as `contentDescription`, outside the row's
`mergeDescendants` (exactly `Tick.kt:85-91`). Haptic `ToggleOn` / `ToggleOff` through `LocalHapticFeedback` on the
user's tap only; none on Undo.

**Motion (row 4, `Motion.kt`):** two icons in one Box; one progress value from
`animateFloatAsState(if (off) 1f else 0f, Motion.quick())` read inside `graphicsLayer` lambdas (alpha of the crossed
bell = p, of the on icon = 1 − p). Both directions `Quick`. Undo restores with a snap (`Animatable.snapTo`, or a key
that skips the animation): the state was never meant to change. Label "bell".

## 4. The messages

Material snackbar through the one `SnackbarHost` in `MainScaffold.kt:181-191` (above the glass bar, follows the
user to any tab), `showSnackbar(message, actionLabel = Undo, duration = Short)`, the current one dismissed first,
exactly as `pauseToday` does. Shown only when a bell goes **off**; never on bell on, never on Undo.

| Key | English | Shows when |
|---|---|---|
| `bell_off_message` | %1$s alert off | the tap changed no reminder |
| `bell_off_reminder_message` | %1$s alert and reminder off | "Reminder before prayer" is above 0 and this prayer was in its list |
| `bell_off_message_fajr_ramadan` | Fajr alert off. Suhoor reminder stays on. | Fajr, on a day of Ramadan on the user's own calendar (`state.ramadan.info.isRamadan`, which already carries the Hijri shift), with `suhoorReminderMinutes > 0`, and no reminder changed |
| `bell_off_reminder_message_fajr_ramadan` (**new, not in row 3**) | Fajr alert and reminder off. Suhoor reminder stays on. | the same, when the reminder also went off |

The fourth key is the one honest way to say both facts without joining sentences in code (joining breaks Urdu's full
stop and Arabic's order). If `plan-verifier` prefers three keys, the Ramadan form wins over the reminder form (the
suhoor reassurance is what a fasting user needs to hear), recorded as an accepted gap. The Ramadan condition must
match the scheduler's, not the Ramadan card switch: `SuhoorReminderPlan.plan` runs on `minutesBefore` alone
(verified), so a user who hid the card still gets the reminder and still gets the reassurance.

**Which message** is decided from what the repository actually changed (the off call returns whether a reminder was
removed), never from stale screen state.

## 5. What off removes and on restores (alarm logic: the main session owns it)

One `SettingsRepository` edit per tap, so the file never holds half a change:

- **Off:** write `alertFor[slot]` into `lastAlertStyle[slot]` (new key, codec, default NOTIFICATION); remove the slot
  from `alertFor`; if the slot is in `prayerReminderFor`, remove it and add it to `bell_removed_reminder` (9.4).
  Return whether a *live* reminder was removed (`prayerReminderMinutes > 0 && slot in prayerReminderFor` before the
  edit) for the message. The slot is moved even when minutes is 0, so a prayer silenced by its bell gets no heads up
  if the user later sets minutes; bell on brings it back.
- **On, and Undo:** `alertFor[slot] = lastAlertStyle[slot] ?: NOTIFICATION`; if the slot is in
  `bell_removed_reminder`, add it back to `prayerReminderFor` and clear it from there. Undo is the same call plus the
  snap.
- **Settings keeps parity:** `AlertsDialog` and `PrayerReminderDialog` read `alertFor` and `prayerReminderFor`
  (verified), so they always show the truth. "Off" in the alerts dialog (`setAlert(slot, null)`) also records
  `lastAlertStyle`, so a bell turned on afterwards restores the style the user last chose. Ticking a prayer in the
  reminder dialog clears it from `bell_removed_reminder` (9.4). The alerts dialog's Off never touches the reminder:
  only the bell carries Ali's Q6.
- **Rescheduling** comes free: `SajdaViewModel.kt:690` runs `PrayerAlarmScheduler.reschedule` from the settings flow
  (verified the call; assumed it fires on every change). A Fajr alarm due in a minute is cancelled by the tap and
  put back by Undo.
- No usage event: the bell sends nothing (`UsageCountsTest` pins the event list).

## 6. Notifications blocked

"Blocked" = `NotificationManagerCompat.areNotificationsEnabled()` false (covers Android 13's permission and the older
"turn off for this app" switch; `Notifications.canPost` checks only the permission, main session's choice which to
extend). Channel level blocking is out of scope, recorded.

- **Turning a bell on while blocked** stores on, then on Android 13+ launches `POST_NOTIFICATIONS` through the
  existing `notificationPermission` launcher with `awaitingSetupNotificationResult` left false, so nothing is counted
  (`MainActivity.kt:86-95`, verified: only setup counts). If the result is denied and
  `shouldShowRequestPermissionRationale` is false (the system will not ask again), or on Android 12 and below, open
  SajdaTime's notification page (`ACTION_APP_NOTIFICATION_SETTINGS`, the fallback already written in
  `Notifications.openDndAccessSettings`). No dialog of our own before the system's.
- **The notice (Ali's question 1):** an amber `Notice` in the existing notice slot, the same component and look as
  "Allow exact alarms": `Icons.Outlined.WarningAmber` (SF `exclamationmark.triangle`), title "Allow notifications",
  brief "Prayer alerts cannot arrive on this phone.", details "Your phone is not letting SajdaTime send
  notifications, so no prayer alert or reminder can reach you.", action "Allow" (`action_allow`, exists; kind B)
  doing what the bell does above, the X alone (kind D). Shown while blocked and any alert is on; dismiss is
  remembered (`notificationsNoticeDismissed`, like `exactAlarmNoticeDismissed`) and the notice returns only if the
  user later taps a bell on while still blocked. Without this notice the blocked state is explained only to TalkBack
  users.

## 7. States, accessibility, right to left, sizes

| State | What shows |
|---|---|
| First run | Five filled bells (default alert on all five) |
| All off | Five crossed bells; Settings "Prayer alerts" reads "Off" |
| Ramadan | Bells unchanged; the Fajr message form of §4; "Suhoor ends" line stays |
| No location (Makkah fallback) | Bells shown and work: the setting is per prayer, not per place |
| Blocked | §6 |
| Message while another shows | The earlier message is dismissed; one at a time |
| Light and dark | Existing roles only; the snackbar is Material's inverse pair |
| Urdu and Arabic | Bell at the row's left; slash not mirrored; the snackbar and Undo mirror themselves |
| 1.5 and 2.0 text | The bell stays 24 in 48; the row grows from its text. At 320dp and 2.0 measure the Next row ("Maghrib", pill, time, bell): if the name breaks inside the word, the time drops under the name as step 2's rows do (`STEP-2-DESIGN.md` §9); the bell stays at the end either way |
| 823dp | No height change: the 48dp target sits inside the 50dp floor (the tick proved it, 23 Oct) |
| TalkBack | Row: "Dhuhr, Next, 12:46 pm" (unchanged), then its own stop "Dhuhr alert on, notification, switch, on". The message is announced by Material's live region (assumed; check on device). Undo is reachable as the snackbar's action |
| Remove animations | The crossfade jumps; the snap is a snap |

## 8. Strings (English; the main session translates, `TrackerWordingTest` gains the `bell_` prefix, 8.13)

```
bell_on_a11y                            %1$s alert on, notification
bell_alarm_a11y                         %1$s alert on, alarm
bell_off_a11y                           %1$s alert off
bell_blocked_a11y                       %1$s alert on. Notifications are off on this phone.
bell_off_message                        %1$s alert off
bell_off_reminder_message               %1$s alert and reminder off
bell_off_message_fajr_ramadan           Fajr alert off. Suhoor reminder stays on.
bell_off_reminder_message_fajr_ramadan  Fajr alert and reminder off. Suhoor reminder stays on.
notifications_off_title                 Allow notifications            (only if Ali says yes to the notice)
notifications_off_brief                 Prayer alerts cannot arrive on this phone.
notifications_off_desc                  Your phone is not letting SajdaTime send notifications, so no prayer alert or reminder can reach you.
```
Reused: `action_undo`, `action_allow`. Translation notes already fixed: Undo (8.13), Suhoor (8.13). Checked: no
"Tap", no "missed", "late" or "owed", no hyphen or dash, prayer names come from `slot.labelRes`.

## 9. Tests

- **`AlertRestoreTest`** (app unit, pure logic): off records ALARM and on restores ALARM; no memory restores
  NOTIFICATION; reminder live → removed and restored; reminder not in the list → nothing added on restore; minutes
  0 → moved but reported as no change; dialog retick clears the memory and a later restore adds nothing twice; Undo
  equals restore; the message chooser's four cases as a pure function of (slot, reminderRemoved, ramadanSuhoor).
- **`SettingsCorruptionTest`**: garbage in `last_alert_style` and `bell_removed_reminder` reads as empty.
- **`ColorContrastTest`**: `outline` on `primaryContainer`, `surfaceContainerHigh` and `surface`, 3:1, both themes
  (8.4); `primary` on `surface` and `surfaceContainerHigh`, 3:1 (the on bell); `inversePrimary` on `inverseSurface`
  4.5 (the Undo label).
- **`DesignRulesTest`**: the inventory rows below; no written `tween` (use `Motion.quick()`).
- **`TranslationIntegrityTest`**: the new keys in five languages. **`UsageCountsTest`**: unchanged event list.
- **`LargeTextAndTalkBackDeviceTest`**: a bell's spoken text at 2.0 and its 48dp bounds; the Sunrise blank keeps the
  times aligned.
- **By hand on the emulator:** tap off → Settings shows Mixed; `adb shell dumpsys alarm | grep sajdatime` loses the
  prayer's alarm and Undo puts it back; animator scale 0 snaps; the blocked path on an API 33 emulator with the
  permission denied twice.

## 10. DESIGN_SYSTEM changes

§4.1 inventory:
- Times | the bell at the end of each prayer row, not Sunrise | Bell, a toggle like Tick | 24 drawn, 48 target | `HomeScreen.kt`
- Times, notifications notice (if yes) | Allow (trailing arrow); the X, alone | B; D | 48, wraps | `Notice.kt`
- Kind G row: "Only Undo in the message at the bottom ("Paused from today"; "Dhuhr alert off" and its three forms)".

§9: a dated entry (filled plain bell over `NotificationsActive`, blocked as grey plus notice, the fourth message key,
Settings Off also records the style). §11: `1.6-bell` moves to decided-and-built when Ali says yes.

## 11. iPhone effect

Carries over: a toggle at the end of a plain list row (Reminders and Clock do the same), SF `bell.fill`,
`bell.slash`, `alarm.fill`; the permission ask becomes `requestAuthorization` and the fallback
`openNotificationSettingsURLString`; the amber notice is a plain banner. To revisit there: the snackbar becomes an
inline "Dhuhr alert off · Undo" line (the same note as step 1). Android only here: exact alarms (unchanged) and the
notification channel.

## 12. Pictures for Ali

Light and dark, 823dp, English: (1) Times with Fajr as alarm, Asr just turned off, the message "Asr alert off" with
Undo showing; (2) the blocked state with the notice, for question 1. For `plan-verifier` and `reviewer`: Urdu right
to left, 2.0 text at 320dp, TalkBack on one bell.

## 13. Questions for Ali

1. When the phone blocks notifications: **"Show an amber notice with Allow, like the exact alarms one
   (Recommended)"**, or "No notice; only tapping a bell asks".

## Verified in code, and assumed

Verified: `HomeScreen.kt` `PrayerRow` 961-1076 and the old tick row (`e98b2a6`); `Tick.kt` semantics;
`SettingsRepository` `AlertStyle`, `alertFor`, `setAlert` 399-403, `prayerReminderMinutes` default 0,
`prayerReminderFor` default all five, `setPrayerReminder`; `MainScaffold.kt:178-191` snackbar;
`MainActivity.kt:86-95, 318-323` (only setup counts the permission); `Notifications.canPost` 312 and the settings
page fallback 372-392; `SuhoorReminderPlan.plan` runs on minutes alone; `PrayerAlarmScheduler.kt:96-101` drops a
same minute Fajr heads up; `AlertsDialog` and `PrayerReminderDialog` read live settings; `alert_style_mixed` and
`action_allow` exist; `ColorContrastTest` pairs; `Motion.kt`; `Space` tokens; no existing wording for blocked
notifications anywhere.

Assumed: `SajdaViewModel.kt:690` reschedules on every settings change; Material 3's snackbar announces as a live
region; `Icons.Rounded.Notifications` and `Icons.Outlined.NotificationsOff` exist in extended 1.7.8; the 320dp 2.0
row needs measuring, not arithmetic.

**Risks (Fable):** the restore path must be one atomic edit, or a crash between two writes leaves a prayer silent
with no memory. The blocked path depends on `shouldShowRequestPermissionRationale`, which some makers report oddly:
test on the Redmi. The 320dp 2.0 Next row may need the stacking fallback.

## 14. Corrections from `plan-verifier` (24 Oct 2026). These win over §1 to §13

Nothing serious; every cited line resolves. Before building:
1. **Settings summary wording:** one prayer off reads "4 of 5 · Notification" (`SettingsScreen.kt:1055-1070`),
   "Mixed" only when styles differ; all off reads "None". Fix §2 item 2, §7 and the §9 by-hand check.
2. **Permission door chosen before launching,** not after a denial: Android 13+, denied, rationale false and asked
   before → open the phone's settings; otherwise launch the system ask; a denied result does nothing. A pure
   function with a unit test.
3. **Read `areNotificationsEnabled()` on `ON_RESUME`** into state (as `MainActivity.kt:231` does for exact alarms),
   so the bells turn green the moment the user comes back.
4. **Sunrise blank is `Spacer(Modifier.width(48.dp))`,** never `size(48.dp)` (`DesignRulesTest:146-148`).
5. **Icons verified:** `Icons.Rounded.Notifications` (material-icons-core 1.7.8), `Outlined.NotificationsOff`,
   `Rounded.Alarm`, `WarningAmber` (extended 1.7.8).
6. **The notice is `PermissionCard`** (`ui/components/PermissionAction.kt:116`) with its default icon, for the same
   look as exact alarms.
7. **Rescheduling verified:** settings flow `SajdaViewModel.kt:235` → `recalculate()` → `reschedule` :690 on every
   change (only early return: no coordinates, where no alarm exists).
8. **Ramadan condition:** `state.ramadan` is nullable; and the evening before 1 Ramadan the suhoor reminder fires
   for tomorrow's Fajr. Use `isRamadan(today) || isRamadan(tomorrow)`, with a test.
9. **Record in DESIGN_SYSTEM §9:** the alerts dialog's Off never touches the reminder (only the bell carries Q6).
   `AlertRestoreTest` adds: Settings Off, then bell on, restores the dialog's last style.
10. **The review card must not land over Undo:** pass snackbar visibility into `calm` (`HomeScreen.kt:130-137`).
11. **No `privacy.html` change:** the three new keys are on-device settings only.
12. **iPhone road:** the off/on/restore and message rules live in a plain Kotlin object (maps in, maps out, no
    DataStore), so `AlertRestoreTest` is pure and the rule carries over; `SettingsRepository` applies its result in
    one `edit`.

Only §13's question needs Ali.

## 15. Ali's answer (24 Oct 2026, AskUserQuestion)

Blocked notifications: **"Amber notice with Allow (Recommended)"**. Build §6's notice as corrected by §14.6.

## 16. Ali on the prototype pictures (24 Oct 2026, AskUserQuestion)

"Yes, build it", with one change, in his words: "the allow notification panel should look modern and great UI maybe
a modern looking slider like iPhone style which the user select allows and not allowed and details. Check if it
makes sense". Sent to Fable to judge before the build (§17).

## 17. Fable on Ali's switch idea (24 Oct 2026): no switch, keep the amber notice

A switch only makes sense when the app can turn the thing on and off. Here only the phone can allow notifications:
the switch would flip on, the phone would refuse, and it would spring back, which looks broken; and "not allowed"
cannot be done from the app at all. Apple's own pattern for a blocked permission is a short message with a button to
Settings; the iPhone switch lives in the phone's Settings app, not inside apps. The exact alarms notice uses the same
card and Ali approved it on 17 and 18 Oct, so two notices with two control styles would clash. The modern on/off in
step 3 is the bell on each row, which is the app's own setting and never springs back. Build §6 as corrected by §14,
unchanged. Rejected: a switch (springs back, misleading to TalkBack as "switch, off", a new control kind outside
DESIGN_SYSTEM §4.1). Put to Ali as a one tap question; his answer is §18.

## 18. Ali's answer on the notice (24 Oct 2026, AskUserQuestion)

"Keep the amber card", with, in his words: "but make look nice in UI not plan old looking buttons and panel". So: no
switch; the card's look (panel and buttons) gets a visual polish, designed by Fable within DESIGN_SYSTEM, shown to
Ali as pictures before it is built. The exact alarms notice shares the component, so it changes with it (§19).

## 19. The polished permission card (Fable, 24 Oct 2026). Not built; waits for Ali's four answers (§20)

**For Ali:** keep the amber card but give it the countdown card's finish: a soft amber glass panel (lighter at the
top, faint sheen, thin bright rim) instead of the flat beige box. One message, one button: the triangle in a tinted
circle goes, replaced by a plain crossed bell (the same bell as the rows); title and one sentence; one solid amber
"Allow" with its arrow. "Details" goes on both permission cards because it only repeated the sentence; the one fact
that mattered ("Fajr may not arrive at all") moves into the exact alarms sentence. The X stays alone in the corner.

**What looked dated:** flat beige panel under a glossy hero card; two equal brown pills, neither leading; the stock
triangle in a 14% circle costing 48dp; a details sentence that repeats the brief.

**Panel (warning tone only):** the hero stack (`HomeScreen.kt:459-477`) minus the 12dp glow:
`.sajdaSurface(shape, warning.brush)` + `.background(verticalGradient(0f to warning.sheen, 0.5f to Transparent))` +
`.border(1.dp, verticalGradient(rim, rimBottom), shape)` + `.padding(Space.Group)`; shape 24dp; the old 22% accent
border removed. New `Color.kt` tokens: `LightWarnTop #FBF0D8`, `LightWarnBottom #F3E2BE` (middle `tertiaryContainer`),
`DarkWarnTop #352A17`, `DarkWarnBottom #20190D`; sheen and rim reuse `Light/DarkHeroSheen/Rim/RimBottom`. `Theme.kt`:
`data class GlossStyle(brush, sheen, rim, rimBottom)` and `@Composable fun warningGloss()` reading `LocalDarkTheme`.
**Quiet tone stays flat** (`secondaryContainer`): dark CTA `#17805A` on it is already 2.32:1 and a sheen drops it to
1.64:1. That existing 2.32:1 is a gap against §4.1's 3:1 claim: record in HANDOVER §11, out of scope.

**New ColorContrastTest pairs** (WCAG, via `over()`): light onTertiaryContainer `#5A4408` on WarnTop 8.19, WarnBottom
7.25, top under sheen 8.82 (min 4.5); light tertiary `#9A6208` on WarnTop 4.50, WarnBottom 3.98, under sheen 4.84 (min
3.0); dark onTertiaryContainer `#F3E4C5` on WarnTop 11.18, WarnBottom 13.86, under sheen 9.02 (4.5); dark tertiary
`#E8B14A` on WarnTop 7.24, WarnBottom 8.97, under sheen 5.84 (3.0).

**Mark:** no tile. Bare 24dp icon, tint `tertiary` (warning) or `onSecondaryContainer` (quiet), aligned top,
`contentDescription = null`. `MarkSize` 24dp; text and buttons start at `24 + Space.Paragraph`; remove the 6/8dp top
padding. `PermissionCard` gains `icon: ImageVector = Icons.Rounded.WarningAmber`; notifications passes
`Icons.Rounded.NotificationsOff` (SF `bell.slash.fill`); exact alarms keeps WarningAmber (SF
`exclamationmark.triangle.fill`).

**Text:** title titleMedium with heading semantics; brief bodyMedium, `Space.Tight` below. `notifications_off_desc`
deleted and the `details =` argument dropped at both call sites. `settings_exact_alarms_brief` becomes "Prayer alerts
can arrive an hour late, and Fajr may not arrive at all." (translate ur, ar, tr, in); delete
`settings_exact_alarms_desc` only if the two cards are its only users (grep first).

**Buttons:** one row, `Space.Group` under the text, start padding `MarkSize + Space.Paragraph`. Allow: kind B
unchanged (tertiary/onTertiary, actionLabelStyle, 48dp pill, trailing AutoMirrored ArrowForward 18dp, 6dp gap,
`contentDescription = title`). No Details on the two permission cards; `Notice` keeps Details for polar and Ramadan.
X: kind D unchanged.

**Motion:** no entrance. Dismiss on Times: `AnimatedVisibility(exit = fadeOut(Motion.quick()) +
shrinkVertically(Motion.quick()))`, enter none. No haptics.

**States:** 2.0 text at 320dp, text column 172dp (was 160), title wraps beside the X; Urdu and Arabic mirror (gradient
vertical, does not); TalkBack: heading, sentence, "Dismiss, button", "Allow notifications, button"; light 2dp shadow,
dark hairline. **iPhone:** carries over (inset card, tinted glyph, one sentence, one filled button; SF Symbols named).

**DESIGN_SYSTEM edits:** §1 four Warn tokens; §4 Notices (bare icon, gloss on warning only with the 2.32:1 reason, one
button on permission cards); §4.1 "Every notice" row: Details only when the notice has details, plus the notifications
row; §9 dated entry with the rejected options: a switch (§17), a text Details link, green CTA on amber (3.2:1 dark),
full width amber button (rejected 17 Oct), a glow under the card, glossing the quiet tone, a white card with an amber
bar. Pictures for Ali before building: light, dark, Urdu, 2x, Times and Settings, both cards.
