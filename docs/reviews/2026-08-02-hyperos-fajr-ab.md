# Raw evidence — the overnight Fajr A/B on HyperOS (2 Aug 2026)

Primary evidence for HANDOVER §10 *"The overnight A/B at Fajr"* and §6 *"Rung 3 is worth less
than 'inexact' suggests"*. Kept because it cannot be reproduced without that phone.

**Device:** Xiaomi Redmi Note 13 Pro 5G (`2312DRA50G`, garnet), HyperOS 3 / Android 16, API 36.
**Window:** 00:28 → 04:22 BST, plugged in, screen off, untouched.
**Method:** both builds installed side by side, each holding a Fajr alarm at exactly
02:51:00.000. Pending table, notification list, Doze state and wakefulness sampled every 30s;
full `logcat` captured in parallel as an independent witness.

Held constant by construction: phone, OEM policy, charger, clock, prayer, minute, and — read
at the same moment — App Standby bucket. Sole variable: which `AlarmManager` API scheduled it.

```
$ adb shell am get-standby-bucket com.sajdatime.app           → 40   (RARE)
$ adb shell am get-standby-bucket com.sajdatime.app.sideload  → 40   (RARE)
$ adb shell dumpsys deviceidle | grep mState
  mScreenOn=false  mCharging=true  mState=ACTIVE mLightState=ACTIVE
```

## Result

```
02:50:35 pend_fix=2 pend_play=1 note_fix=0 note_play=0   mState=ACTIVE mWakefulness=Dozing
02:51:05 pend_fix=0 pend_play=1 note_fix=1 note_play=0   mState=ACTIVE mWakefulness=Dozing
02:51:36 pend_fix=0 pend_play=1 note_fix=1 note_play=0   mState=ACTIVE mWakefulness=Dozing
...
WATCH ENDED fixed=02:51:05 shipped=
```

`pend_*` counts alarms whose `origWhen` is 02:51 still in the pending table; `note_*` counts a
posted 2000-series prayer notification. The fixed build's alarm left the queue and posted
between the 02:50:35 and 02:51:05 samples. The shipped build's never did, across all 460
samples.

## Delivery, from the system's own log

```
08-02 02:51:00.033 D/AlarmManager: MSG_REPORT_ACTIVE statsTag=*walarm*:com.sajdatime.app.action.PRAYER_ALARM,
                                   sourcePackage=com.sajdatime.app.sideload
08-02 02:51:00.037 D/AlarmManager: [xiaomi_power]sending alarm Alarm{76d3ba6 type 0
                                   origWhen 1785635460000 whenElapsed 120410840
                                   com.sajdatime.app.sideload} ... at 120410840 ms
08-02 02:51:00.038 I/SmartPower:   com.sajdatime.app.sideload/10376 idle->background R(alarm start) adj=901
08-02 02:51:00.185 D/PowerManagerService: acquireWakeLockInternal:
                                   tag="NotificationManagerService:post:com.sajdatime.app.sideload"
```

**37 ms after the target**, from a Dozing screen-off phone.

Counts across the whole capture:

```
"sending alarm ... com.sajdatime.app}"           → 0
"sending alarm ... com.sajdatime.app.sideload}"  → 1
```

## Why the shipped build never fired

`dumpsys alarm` at 04:23, 1h32m after Fajr — the alarm is still queued, and its delivery time
has been **replaced**, not widened:

```
RTC_WAKEUP #103: Alarm{abb4c83 type 0 origWhen 1785635460000 com.sajdatime.app}
  type=RTC_WAKEUP origWhen=2026-08-02 02:51:00.000 window=+1h0m0s0ms flags=0x20
  policyWhenElapsed: requester=-1h30m52s644ms  app_standby=-3h3m33s270ms  device_idle=--
                     battery_saver=-3h3m33s270ms  ssru=-3h3m33s270ms
                     power_pending=+2d22h29m7s356ms
  whenElapsed=+2d22h29m7s356ms  maxWhenElapsed=+2d22h29m7s356ms
```

Every policy except `power_pending` is negative — i.e. not binding. Doze, App Standby, battery
saver and SSRU are all satisfied. `power_pending` alone holds it, out to 5 August.

The same dump for the fixed build, untouched:

```
RTC_WAKEUP #45: Alarm{363e9db type 0 origWhen 1785672600000 com.sajdatime.app.sideload}
  type=RTC_WAKEUP origWhen=2026-08-02 13:10:00.000 window=0 exactAllowReason=permission flags=0x3
  policyWhenElapsed: requester=+8h45m44s748ms  app_standby=-1h33m15s27ms  device_idle=--
                     battery_saver=--  ssru=-1h33m15s27ms  power_pending=--
  whenElapsed=+8h45m44s748ms  maxWhenElapsed=+8h45m44s748ms
```

`flags=0x3` = `FLAG_STANDALONE|FLAG_WAKE_FROM_IDLE`, the signature of `setAlarmClock`.
`flags=0x20` = `FLAG_ALLOW_WHILE_IDLE_COMPAT`, the inexact `setAndAllowWhileIdle`.

## Xiaomi saying it in its own words

```
08-02 02:51:00.172 E/AlarmManager: not align this alarm:
                   Alarm{cfe78a type 0 ... com.sajdatime.app.sideload}, reason=6
```

18 occurrences during the window, **all** for the fixed package and **none** for the shipped
one — HyperOS's alignment engine excluding exact alarm-clock alarms from the batching it
applies to everything else.

## Caveats

- Only Fajr was observed firing; the other four prayers of 2 Aug were still pending when the
  watch ended.
- One night, one device, one OEM. Samsung One UI untested.
- From 02:09 the phone was in `ZEN_MODE_ALARMS` so the 02:51 alerts would not wake the
  household. DND suppresses presentation, not posting, and the primary signal (the alarm
  leaving the pending table) is unaffected by it — but no human heard this fire.
