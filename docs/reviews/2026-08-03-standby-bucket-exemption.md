# Raw evidence — App Standby buckets do not hold `setAlarmClock` (3 Aug 2026)

Primary evidence for HANDOVER §6 *"What this does not fix, and must not be claimed to"* and
§10 *"The overnight A/B at Fajr"*. It closes the last constraint §6 listed as unfixed, and it
retires a confound the two August A/Bs were **not** actually controlling for.

Two independent sources, gathered in that order deliberately: measure first, then read the
source to find out whether the measurement generalises.

---

## 1. Measured — API 36, bucket RESTRICTED, off charger, parole off

**Device:** `sdk_gphone64_arm64` emulator, Android 16 / API 36,
fingerprint `google/sdk_gphone64_arm64/emu64a:16/BE2A.250530.026.F3/13894323:userdebug/dev-keys`.
**Build under test:** `app-debug.apk` at the current HEAD, i.e. the `setAlarmClock` ladder.

```
$ adb shell dumpsys battery unplug
$ adb shell am set-standby-bucket com.sajdatime.app restricted

$ adb shell am get-standby-bucket com.sajdatime.app
45                                    ← RESTRICTED, the harshest bucket there is
$ adb shell dumpsys battery | grep -iE "AC powered|USB powered"
  AC powered: false
  USB powered: false
$ adb shell dumpsys alarm | grep -i "App Standby Parole"
  App Standby Parole: false           ← the charger exemption is OFF
```

All three conditions hold at once. That combination has never been achieved on this project
before — every prior alarm measurement was taken on a charger.

Every SajdaTime prayer alarm in that state:

```
RTC_WAKEUP #20: Alarm{93ec8ed type 0 origWhen 1785778560000 com.sajdatime.app}
  tag=*walarm*:com.sajdatime.app.action.PRAYER_ALARM
  type=RTC_WAKEUP origWhen=2026-08-03 18:36:00.000 window=0
                  exactAllowReason=permission flags=0x3
  policyWhenElapsed: requester=+1h40m36s43ms  app_standby=-2m25s250ms
                     device_idle=--  battery_saver=--
  whenElapsed=+1h40m36s43ms  maxWhenElapsed=+1h40m36s43ms
```

`app_standby` sits in the **past**, so it is not binding, and `whenElapsed` equals
`maxWhenElapsed` equals `requester` — the alarm is exactly where the app asked for it, with
zero slack. Nine alarms read, all identical in shape. **At RESTRICTED, off charger, App
Standby is holding nothing.**

Restored afterwards: `dumpsys battery reset`, `am set-standby-bucket … active`.

---

## 2. Sourced — AOSP says why, and says it is not a coincidence

From
[`AlarmManagerService.java`](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/apex/jobscheduler/service/java/com/android/server/alarm/AlarmManagerService.java)
on `main`, fetched 3 Aug 2026. **VERIFIED** — pasted from the file, not recalled.

```java
    @VisibleForTesting
    static boolean isExemptFromAppStandby(Alarm a) {
        return a.alarmClock != null || UserHandle.isCore(a.creatorUid)
                || (a.flags & (FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED | FLAG_ALLOW_WHILE_IDLE)) != 0;
    }
```

```java
    private boolean adjustDeliveryTimeBasedOnBucketLocked(Alarm alarm) {
        final long nowElapsed = mInjector.getElapsedRealtimeMillis();
        if (isExemptFromAppStandby(alarm) || mAppStandbyParole) {
            return alarm.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, nowElapsed);
        }
        …
        if (standbyBucket == UsageStatsManager.STANDBY_BUCKET_RESTRICTED) {
            // Special case because it's 1/day instead of 1/hour.
```

`a.alarmClock != null` is precisely what `setAlarmClock()` sets and nothing else does. The
bucket branch — including the 1/day RESTRICTED case Google's public table warns about — is
**unreachable** for an alarm-clock alarm. The policy time is pinned to `nowElapsed`, which is
the `app_standby=-2m25s` reading above.

### The mirror image: why the shipped v1.1.0 build *was* bucket-bound

Same file, in `setImpl`:

```java
                if (lowerQuota) {
                    flags &= ~FLAG_ALLOW_WHILE_IDLE;
                    flags |= FLAG_ALLOW_WHILE_IDLE_COMPAT;
                }
```

An app that reaches `setAndAllowWhileIdle` without the exact-alarm route has
`FLAG_ALLOW_WHILE_IDLE` **stripped** and `FLAG_ALLOW_WHILE_IDLE_COMPAT` put in its place.
`isExemptFromAppStandby` tests `FLAG_ALLOW_WHILE_IDLE` and `FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED`
— it does **not** test `_COMPAT`. So the compat alarm falls through to the bucket branch.

That is the `flags=0x20` the shipped build carried in every dump on the Xiaomi, and §10 was
already calling it "allow-while-idle **compat**" without knowing this was the consequence.

**Not measured, source-derived.** The old build could not be rebuilt to sit beside this one
in the RESTRICTED test, so the asymmetry is read from AOSP rather than seen on a screen.

---

## 3. What this retires — a "controlled" variable that was inert

Both August A/Bs cite equal App Standby buckets as an eliminated confound:

> Both packages were in **App Standby bucket 40 (RARE)** — read at the same moment, identical.

True, and it did not mean what it was taken to mean. From the same file:

```java
                final BatteryManager bm = getContext().getSystemService(BatteryManager.class);
                mAppStandbyParole = bm.isCharging();
```

`mAppStandbyParole` short-circuits `adjustDeliveryTimeBasedOnBucketLocked` for **every alarm
on the device**, regardless of bucket. The overnight run logged `mCharging=true`; the daytime
Dhuhr run was "plugged into a charger, unlocked and being used". So in both runs the buckets
were equal *and* switched off. Reading them proved the two builds started level; it could
never have shown bucket damage, because there was none available to show.

The conclusions of those A/Bs are unaffected — `power_pending` was still the only binding
policy, and the fixed build still fired at 02:51:00.037. What changes is the *scope*: they
were experiments about OEM parking, and they were silent on buckets by construction, not
merely "not established" in passing.

---

## 4. What is still open

- **HyperOS is not AOSP.** Everything in §2 is AOSP `main`. Xiaomi ships its own
  `AlarmManagerService` — `power_pending` is proof they modify it — so the exemption is
  verified for stock Android and *assumed* for HyperOS. The Xiaomi dumps show
  `app_standby` negative on both builds, but both were on a charger, so parole explains that
  without needing the exemption. Untestable for now: HyperOS refuses the sideload
  (`INSTALL_FAILED_USER_RESTRICTED`).
- **Jobs are not alarms.** `DailyRescheduleWorker` is a WorkManager job and gets no
  `alarmClock` exemption. Read in the same state (bucket 45, off charger) it was
  `WITHIN_QUOTA` with only `TIMING_DELAY` outstanding, so it is not blocked — but that is one
  snapshot with an empty quota window, not a day under sustained restriction.
