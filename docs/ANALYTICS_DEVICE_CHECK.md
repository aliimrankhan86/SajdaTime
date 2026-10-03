# Device check for the optional usage counts (run on the owner's computer)

**For a Claude Code session running on the owner's own computer**, in his local clone, with his phone
plugged in. A cloud session cannot do this (no `adb`, no emulator, no keystore). Written 3 Oct 2026.
Read `CLAUDE.md` first and obey it: the owner is not technical; never enter his PIN or any credential;
never touch or view the signing key; never `installDebug` on his phone (use `installSideload`);
put back anything you change on his phone; do not ask him to test the watch; the watch emulator is
waived for this release. Work alone, report once at the end, and only stop where this file says to.

Plan and reasoning: `docs/ANALYTICS_PLAN.md`. The rules under test live in `UsageCounts.kt` and
`docs/privacy.html`. **Everything below is on branch `claude/app-analytics-strategy-e826p5`. Never push
to `main`.**

## 0. Set up (no questions)
1. `git fetch origin && git checkout claude/app-analytics-strategy-e826p5 && git pull`.
2. Use JDK 21. Run `./gradlew clean test lint` once. It must pass (expect 160+ tests, 0 failures).
3. `adb devices`. If the phone shows `unauthorized` or is missing, tell the owner in one plain
   sentence what to do (CLAUDE.md "He does have real devices" lists the steps and the USB mode).
   Ask him to turn on Developer options > Stay awake. Note the serial and use `ANDROID_SERIAL=<serial>`.

## 1. Phone check (the Redmi or the S23 Ultra), one sitting
Build a **local test build with the send switch on**. This edit is for testing only and **must never be
committed**: in `app/src/sideload/res/values/bools.xml` change `false` to `true`. At the end run
`git checkout -- app/src/sideload/res/values/bools.xml` and confirm `git status` shows it clean.
The `.sideload` build reports to its own Firebase test app, so it cannot pollute real data.

1. `ANDROID_SERIAL=<serial> ./gradlew installSideload`. Then `adb -s <serial> shell pm clear com.sajdatime.app.sideload`.
2. Turn on logging: `adb shell setprop log.tag.FA VERBOSE`, `adb shell setprop log.tag.FA-SVC VERBOSE`,
   `adb shell setprop debug.firebase.analytics.app com.sajdatime.app.sideload`, then `adb logcat -c`.
3. Launch: `adb shell am start -n com.sajdatime.app.sideload/com.sajdatime.app.MainActivity`.
   Drive the UI yourself (`adb shell uiautomator dump` plus `adb shell input tap`). Only if that proves
   impossible, tell the owner exactly which buttons to press, one at a time.
4. **Declined path first:** at the consent screen choose **No thanks**. Finish setup. Open Times,
   Qibla, Settings. In `adb logcat -d | grep -E "FA|FirebaseAnalytics|Logging event|Successful upload"`
   there must be **no** `Logging event`, `screen_view`, `setup_step` or `permission_result`, and no
   upload. Save the output.
5. **Opt in:** Settings > About > turn on "Share usage counts". The full consent dialog must appear.
   Choose Yes. Switch tabs Times > Qibla > Settings. Expect `screen_view` with `screen_name`
   times/qibla/settings (one each, repeats not re-sent), then an upload line.
6. **Setup path:** `pm clear` again, relaunch, choose **Yes** at the consent step, complete setup
   including the notification prompt. Expect `setup_step` for permission, sect, method, confirm,
   finish (never `madhab`, even if you pick Sunni), and `permission_result` for location and
   notifications and exact_alarm. Run the setup twice, once choosing Shia and once Sunni, and diff
   the event names logged: **they must be identical.**
7. **Persistence:** on, then off, then on, then kill the app (`adb shell am force-stop ...`) and
   relaunch: the choice is honoured every time and, after off, no further events appear.
8. Layout: take screenshots of the consent step, the Settings About group with the new row, and the
   consent dialog. Report only your conclusion (clipping, overlap, unreadable, fine). Do not show the
   owner right-to-left output as a finding (CLAUDE.md).
9. Put everything back: `adb shell setprop debug.firebase.analytics.app .none.`, uninstall nothing,
   leave the phone's settings as you found them.

## 2. The proof that nothing is sent before consent (emulator, over 75 minutes)
This is the most important check in the whole project. The privacy policy and the Play Data safety
form promise it. Create (or reuse) an emulator with a **Google APIs** system image, not necessarily the
Play Store one. Start it with packet capture: `emulator -avd <name> -tcpdump /tmp/optout.pcap`.

1. Install the **sideload build with the local send switch ON** (same edit as section 1), fresh
   (`pm clear`). Launch it. At the consent step choose **No thanks**. Finish setup.
2. Keep it running for **at least 75 minutes** (Analytics uploads in roughly hourly batches). During
   that time: open and background the app several times, send a boot broadcast, trigger a prayer
   alarm if practical, and let WorkManager run (`adb shell cmd jobscheduler run -f com.sajdatime.app.sideload <jobId>`
   if you can find it; otherwise note it was not forced).
3. Stop the capture. Search for DNS names and TLS SNI: `app-measurement.com`,
   `firebaseinstallations.googleapis.com`, `firebase-settings.crashlytics.com`, anything containing
   `firebase` or `google-analytics`. Also `adb logcat -d | grep -E "Logging event|Successful upload"`
   for the app's process. Google Play services itself talks to Google constantly, so only these
   Analytics and Firebase hosts, attributed to this app's activity, count.
4. **PASS** = no lookup or connection to those hosts and no Analytics log lines while opted out.
5. Then opt in on the same emulator and show those hosts **do** appear, so the capture is shown to
   be able to see them (a capture that cannot see anything proves nothing).
6. **If the opted-out run sends anything: STOP. Do not continue to section 3.** Report exactly what
   was seen, with timestamps and hosts. This is the project's stop rule: it does not ship.

## 3. Release build and store assets (only if section 2 passed)
1. `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`. The owner's `keystore.properties`
   on this machine signs it automatically. **Never open, print or copy the key or that file.**
   Report the two `.aab` paths and sizes, and `shasum -a 256` of the phone one.
2. Check the merged release manifest has no `AD_ID`/`ADSERVICES`:
   `grep -r -E "AD_ID|ADSERVICES" app/build/intermediates/merged_manifests/release`.
3. Run `tools/build-store-assets.sh`. Review `git status`: **only** the feature graphic should change
   (the subtitle now reads "Sunni & Shia · No accounts"). Revert any other PNG that changed
   (`git checkout -- <file>`). Look at the new feature graphic and report whether the text sits
   cleanly on the gradient.
4. Retake `docs/store/upload/phone/05-settings.png` only if the new "Share usage counts" row appears
   in that shot (follow the screenshot procedure in `docs/store/LISTING.md` and HANDOVER). Otherwise leave it.

## 4. Record and push
1. Add a dated "Usage counts verification" entry to `docs/HANDOVER.md` §10 with: what was run, on which
   device and emulator, the events seen, the capture result (hosts searched, duration, pass or fail)
   and **what was not tested**. Update the STATE OF PLAY block at the top of §11.
2. Commit with a message that explains the reasoning, never commit the sideload flag edit, and
   `git push origin claude/app-analytics-strategy-e826p5`.

## 5. Report to the assistant (one message, plain language, paste-ready)
- Section 1: for each numbered check, PASS or FAIL with one line of evidence. Include the Sunni vs Shia
  event comparison result.
- Section 2: PASS or FAIL, duration, emulator image, hosts searched, and what the opted-in run showed.
- Section 3: the two `.aab` paths, sizes, the sha256, the manifest grep result, which store images changed.
- Anything that did not match this file, anything you had to decide, and what you did not test.
