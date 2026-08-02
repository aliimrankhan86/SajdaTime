# The daytime A/B at Dhuhr — the live build skipped the prayer entirely

**2 August 2026, Redmi Note 13 Pro (`garnet`, HyperOS, Android 16), owner's own phone.**

Companion to `2026-08-02-hyperos-fajr-ab.md`, which measured the *idle overnight* case. This
one measures the *awake, plugged-in, actively-used* case, which was expected to be the mild
one. It was not.

## Setup

Both builds installed side by side, holding **the same nine prayer times** for the same day
at the same location. No reinstall, no reschedule, nothing touched between arming and firing.

| | package | what it is |
|---|---|---|
| **PLAY** | `com.sajdatime.app` | versionCode 2, `installerPackageName=com.android.vending` — the build the closed testers are running |
| **FIXED** | `com.sajdatime.app.sideload` | current `main`, the `setAlarmClock` fix |

Target: **Dhuhr, 13:10:00 BST**, `origWhen 1785672600000`.

Instrument: the alarm leaving the system pending table, polled every 20 s, so Do Not Disturb
and ringer state cannot affect the result. Notification records read afterwards as a second
witness.

## Scheduling state, hours before firing

```
PLAY   window=+1h0m0s0ms  flags=0x20                        (setAndAllowWhileIdle)
FIXED  window=0           flags=0x3   exactAllowReason=permission   (setAlarmClock)
```

`flags=0x3` is `FLAG_STANDALONE|FLAG_WAKE_FROM_IDLE`. The FIXED entries also carry
`showIntent=…startActivity`, the `setAlarmClock` signature; the PLAY entries have none.

## Result

| | outcome |
|---|---|
| **FIXED** | Alarm left the pending table between 13:09:42 and 13:10:02. Notification `id=2002`, `channel=prayer_times`, `category=reminder`, `android.title="Time for Dhuhr"`, `when=1785672600600` → **posted 600 ms after the target** |
| **PLAY** | Did not fire. Still queued at 13:13:37 |

## What HyperOS did to the live build, in its own words

Read at 13:13:06, three minutes after the prayer was due:

```
type=RTC_WAKEUP origWhen=2026-08-02 13:10:00.000 window=+1h0m0s0ms flags=0x20
policyWhenElapsed: requester=-3m6s606ms app_standby=-1h59m30s242ms
                   device_idle=-2h47m44s768ms battery_saver=-3h0m31s978ms
                   ssru=-3h0m31s978ms power_pending=+2d23h56m53s394ms
whenElapsed=+2d23h56m53s394ms  maxWhenElapsed=+2d23h56m53s394ms
```

`requester=-3m6s` — the app's own requested time is three minutes in the past. Every other
policy is also in the past, i.e. none of them is holding it. **`power_pending` alone is
holding it, and it has overwritten both `whenElapsed` and `maxWhenElapsed` to +2 days 23 h
56 m** — 5 August.

Today's Dhuhr on the live build is therefore not late. **It will never be announced.**

## Why this corrects the earlier reading from the same morning

At 11:21 the same alarms showed `power_pending=--` on both builds, and that was written up as
"the overnight deferral has cleared; what remains in daytime is the plain one-hour Android
window". That was accurate as an observation and **wrong as a conclusion**.

`power_pending` is not a state that clears once when the phone wakes and stays clear. It
re-engaged during the day, on a phone that was plugged into a charger, unlocked and being
actively used over `adb`, and it re-engaged *before the alarm was due*. So:

- The one-hour window is the **floor**, not the expected damage.
- The three-day parking is not an overnight-only phenomenon.
- There is no time of day at which the shipped build can be relied on.

## Caveats

- One device, one OEM, one day. The Samsung reproduced the one-hour window but has no
  `power_pending` equivalent, so the *parking* remains Xiaomi-specific; the *window* does not.
- The FIXED build holds `SCHEDULE_EXACT_ALARM`; the PLAY build also holds it. The difference
  measured here is the API used, not the permission.
- The 600 ms on the FIXED build is delivery-to-notification latency, not alarm inaccuracy.
