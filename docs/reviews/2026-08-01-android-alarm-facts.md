# Android alarms, silent mode and sheet keyboards — what the documentation actually says

**Compiled 1 Aug 2026**, while acting on the second round of tester feedback. Like
`2026-08-01-polar-sources.md` and unlike the rest of this folder, **this file is verified**:
every claim below was written by one agent from primary sources and then re-checked by a
second whose instruction was to *refute* it. Several claims did not survive that pass, and
the corrections are recorded here rather than quietly dropped.

It exists so the next session can change alarm behaviour without re-doing eight hours of
reading, and so that a decision made here can be argued with on the evidence.

> **How to read this.** Claims are marked **VERIFIED** (quoted from Google's own
> documentation or AOSP source at the URL given), **COMMUNITY** (widely repeated, not in the
> docs), or **UNKNOWN** (the docs are silent). "Google's docs are silent" is not the same as
> "Google permits it", and nothing below should be quoted as if it were.

---

## 1. How late can an alarm be? Per API.

| API | Deferred by Doze? | Source |
|---|---|---|
| `setExact()` | **Yes** — *"Defers standard `AlarmManager` alarms, including `setExact()` and `setWindow()`, to the next maintenance window."* | [doze-standby](https://developer.android.com/training/monitoring-device-state/doze-standby) |
| `setAndAllowWhileIdle()` | No, but throttled | same page |
| `setExactAndAllowWhileIdle()` | No, but throttled | same page |
| `setAlarmClock()` | **No** — *"Alarms set with `setAlarmClock()` continue to fire normally. The system exits Doze shortly before those alarms fire."* | same page |

All four **VERIFIED** verbatim by both agents.

Reinforcing, from [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms/schedule), on `setAlarmClock`:

> "Because these alarms are highly visible to users, the system never adjusts their delivery
> time. The system identifies these alarms as the most critical ones and leaves low-power
> modes if necessary to deliver the alarms."

And the opposite promise, from the `AlarmManager` reference, on `setExactAndAllowWhileIdle`:

> "the OS will allow itself more flexibility for scheduling these alarms than regular exact
> alarms… When the device is idle it may take even more liberties with scheduling in order
> to optimize for battery life."

**No Google page states an upper bound in wall-clock time for how late any of the
non-`setAlarmClock` APIs may be. UNKNOWN, and that is the finding** — it is why the app's
own wording no longer says "a few minutes".

### The "nine minutes" figure — three official numbers that disagree

| Figure | Page | Applies to |
|---|---|---|
| *"neither `setAndAllowWhileIdle()` nor `setExactAndAllowWhileIdle()` can fire alarms more than once per nine minutes, per app"* | doze-standby | the two while-idle methods |
| *"not… more than about every minute… when in low-power idle modes this duration may be significantly longer, such as 15 minutes"* | `AlarmManager` reference | the same two methods |
| *"While-idle alarms: Limited to 7 per hour"* | [power-details](https://developer.android.com/topic/performance/power/power-details) | screen off, Doze active |

All three **VERIFIED** verbatim. None carries an API-level qualifier. They are not
reconcilable as written; 7/hour is ≈ one per 8.6 minutes, which is the closest thing to
agreement. The widely repeated story that the limit was one minute on Android 6 and became
nine minutes on Android 7 is **COMMUNITY** — it appears in no Google document either agent
could find.

**None of the three names `setAlarmClock`.** That is verified *by absence*, which means
"not documented as rate-limited", not "documented as exempt".

### App Standby buckets — the limit `setAlarmClock` does not escape

> **⚠️ SUPERSEDED 3 Aug 2026. `setAlarmClock` does escape it.** Everything below is still an
> accurate reading of Google's *published pages*, and the section's own caution — "verified
> *by absence*, which means 'not documented as rate-limited', not 'documented as exempt'" —
> was the right call to make from those pages alone. The absence was then checked against
> the implementation, which is where the answer had been all along:
>
> ```java
> // AlarmManagerService.java, AOSP main
> static boolean isExemptFromAppStandby(Alarm a) {
>     return a.alarmClock != null || UserHandle.isCore(a.creatorUid)
>             || (a.flags & (FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED | FLAG_ALLOW_WHILE_IDLE)) != 0;
> }
> ```
>
> `a.alarmClock != null` is set by `setAlarmClock()` alone, and the bucket branch —
> including the Restricted 1/day case tabled below — is never reached for such an alarm.
> Measured to agree at bucket 45 RESTRICTED with `App Standby Parole: false`. The section's
> conclusion for **this app** therefore inverts: the shipped ladder is exempt, while the old
> `setAndAllowWhileIdle` call was not, because its flag is downgraded to
> `FLAG_ALLOW_WHILE_IDLE_COMPAT`, which the exemption does not test.
> Full working: `2026-08-03-standby-bucket-exemption.md`.

Verbatim from [power-details](https://developer.android.com/topic/performance/power/power-details):

| Bucket | Alarms |
|---|---|
| Active | No execution limits |
| Working set | Limited to 10 per hour |
| Frequent | Limited to 2 per hour |
| Rare | Limited to 1 per hour |
| **Restricted** | **One alarm per day, either an exact alarm or an inexact alarm** |

The refuting agent's most useful correction: the first agent's headline, *"only
`setAlarmClock()` carries a documented guarantee of no deferral"*, is **too strong**. It is a
*Doze* guarantee. Nothing in Google's documentation exempts `setAlarmClock` from bucket
limits, and Android 13's restricted-battery state says flatly *"Alarms aren't triggered"*.

Two more, both **VERIFIED** verbatim from `Manifest.permission`, and together they are the
sharpest thing in this file:

> `USE_EXACT_ALARM`: "Apps that hold this permission, always stay in the WORKING_SET or lower standby bucket."
>
> `SCHEDULE_EXACT_ALARM`: "Apps that hold this permission **and target API `Build.VERSION_CODES.TIRAMISU` and below** always stay in the WORKING_SET or lower standby bucket."

**SajdaTime targets 36. It therefore has no bucket floor at all**, and an app whose user
rarely opens it — which is precisely this app, because the notification *is* the product —
can drift to Rare or Restricted. `USE_EXACT_ALARM` would fix that and is not available to
us; see §3.

Escape hatches, both **VERIFIED** on power-details: *"User manually unrestricts app battery →
Alarms: No execution limits"*, and the charging row lifts limits for every bucket.

---

## 2. Does an alarm still sound when the phone is on silent? Yes.

Established from AOSP source, re-downloaded and re-grepped by the second agent.

- The streams the ringer mutes by default are **RING, NOTIFICATION, SYSTEM,
  SYSTEM_ENFORCED** (`AudioService.java`). **`STREAM_ALARM` is not among them.**
- Making the ringer affect the alarm stream is an opt-in device resource,
  `config_audio_ringer_mode_affects_alarm_stream`, whose AOSP default is **false**.
- A `NotificationChannel` whose `AudioAttributes` carry `USAGE_ALARM` plays on
  `STREAM_ALARM` (`AudioAttributes.toLegacyStreamType`).

**So a prayer alarm rings through a silenced phone, by design.** That is correct for a clock
someone set for one morning and wrong for a recurring alert five times a day, which is what
the tester reported and what `alarmRespectsSilent` now answers.

Corrections worth keeping:

- **`getRingerMode()` returns the *external* mode**, which can diverge from the internal one
  after a ringer change made while Do Not Disturb is on. The first agent called this "the
  single biggest trap" and stated it backwards: ordinary priority DND is an explicit no-op on
  ringer mode (`ZenModeHelper.applyZenToRingerMode`, `// do not apply zen to ringer`). It is
  still the right API to ask; it is just not a DND detector, which is why the app asks it
  *only* about the ringer and leaves DND to its own separate opt-in.
- **"Alarm volume cannot reach zero" is an AOSP default, not a law.** There are two
  independently overridable paths, one of them behind a platform flag literally named
  `alarm_min_volume_zero`. Do not build anything on the assumption.
- Ringer state **does not** suppress a notification's visual treatment. The complete list of
  heads-up suppressors in SystemUI contains no reference to ringer mode at all — **VERIFIED**
  by searching all 498 lines. This is what makes "silent, but still on screen" achievable.

---

## 3. `USE_EXACT_ALARM` — checked, and rejected

Google's policy names exactly two qualifying categories: *"an alarm or timer app"* and *"a
calendar app that shows event notifications"*
([support.google.com/googleplay/android-developer/answer/16558241](https://support.google.com/googleplay/android-developer/answer/16558241)).
Failing the category claim means *"not be able to publish a version of their app with this
permission in the manifest"* — it blocks the release, at review.

A prayer-times app with recurring user-set alerts is arguably an alarm app. **Arguably is not
a category, and the cost of being wrong is a blocked release on a charity project that has
already waited a fortnight for twelve testers.** `SCHEDULE_EXACT_ALARM`, which has no policy
category gate at all, stays. Revisit only if there is a specific reason to, and record the
outcome here.

Also **VERIFIED** and directly relevant:

- `SCHEDULE_EXACT_ALARM` is **revocable by the user *and* by the system**, and revocation
  **stops the app and cancels every future exact alarm**. Never cache the grant.
- *"If the user transfers app data to a device running Android 14 through a
  backup-and-restore operation, the permission will still be denied."* This app has cloud
  backup off, so a restore is not the usual path here, but a restored install must be assumed
  to arrive with the permission denied.
- **No exact-alarm policy or platform change landed in 2025 or 2026.** Both agents checked
  the Android 15 and 16 behaviour-change pages: zero occurrences of "alarm".

---

## 4. Play's 14-day closed-testing window and shipping a fix

Google's requirement is twelve testers, each opted in for fourteen *continuous* days at the
moment you apply. **The page is genuinely silent on whether publishing a new release resets
anything** — the words "reset", "restart" and "app bundle" do not appear on it.

What the same page *does* say, affirmatively:

> "Continue to use closed testing while you fix user-reported issues and bugs."

That is an encouragement to ship fixes during the window, not a guarantee about the clock.
**Do not quote a Google sentence saying releases do not reset the count, because there
isn't one.**

---

## 5. Material3 `ModalBottomSheet` and the keyboard (BOM 2026.06.01)

Included because the tester's screenshot showed a button under a keyboard, and the obvious
fix would have been the wrong one. The second agent overturned the first agent's central
claim here, by reading the shipped bytecode rather than the release notes.

- BOM `2026.06.01` → material3 **1.4.0**, foundation **1.11.4**.
- **material3 1.4.0 already applies `Modifier.fillMaxSize().imePadding()` itself**, inside
  the sheet's own dialog window (`ModalBottomSheet.kt:185`), *in addition to*
  `windowInsetsPadding(contentWindowInsets)`. IME avoidance is therefore always on, a nested
  `.imePadding()` in sheet content is a harmless no-op, and `contentWindowInsets =
  WindowInsets(0)` **cannot** switch it off. That unconditional call is removed in
  1.5.0-alpha19+ (b/289824811), where it moves to `contentWindowInsets` — so upgrading is what
  would *change* this, not what fixes it.
- The parameter is `contentWindowInsets`, **not** `windowInsets`. The official
  material-insets page's snippet does not compile against `ModalBottomSheet`.
- The sheet's window sets `decorFitsSystemWindows = false` and `softInputMode =
  SOFT_INPUT_ADJUST_NOTHING` on API ≥ 30. **The manifest's `windowSoftInputMode` does not
  reach it.**
- `KeyboardOptions(imeAction = …)` and `KeyboardActions(onSearch = …)` are **not deprecated**
  in foundation 1.11.4 and emit no warning.
- `BringIntoViewRequester` is **stable** in 1.11.4 (it was experimental in 1.7.6). Its
  neighbour `bringIntoViewResponder` *is* deprecated — do not reach for that one.

**So the real cause was not insets at all.** It was the sheet's half-height default: the
field opened pinned to the bottom edge with the button off-screen entirely, before any
keyboard was involved. `skipPartiallyExpanded = true` was the fix, and it was found by
running the app, not by reading about insets. See lesson 55.

---

*Made with love, free for the Ummah.*
