# Step 3 emulator checks: results

Run: 2026-10-24, approx 03:59 to 04:03 BST. Device: emulator-5554, app com.sajdatime.app, branch claude/1-6-0-step3-proto (installed build, not rebuilt, no code edited).
Out dir: docs/plans/2026-10-release-1.6.0/screens/step3/checks (all paths below relative to repo root).
Raw dumps: dump-*.xml in the same folder. Screenshots are shrunk with `sips -Z 1200`.

## 0. Restore
- `pm grant ... POST_NOTIFICATIONS` rc=0; `appops set ... SCHEDULE_EXACT_ALARM allow` rc=0.
- Launch dump (dump-0-launch.xml): Fajr "alert on, notification"; Dhuhr "alert on, notification"; Asr "alert off"; Maghrib "alert on, notification"; Isha "alert on, notification".
- Tapped Asr centre (959,1500). After 6s (dump-0-after-asr-tap.xml): Asr "alert on, notification". All five on.

## 1. Bell spoken text (launch state after restore, dump-1-bells.xml)
Every alert node: node itself checkable=false checked=false clickable=false. Parent: class android.view.View, checkable=true, checked=true, clickable=true, content-desc empty.
- "Fajr alert on, notification" bounds [896,1033][1022,1159]
- "Dhuhr alert on, notification" bounds [896,1302][1022,1428]
- "Asr alert on, notification" bounds [896,1437][1022,1563]
- "Maghrib alert on, notification" bounds [896,1572][1022,1698]
- "Isha alert on, notification" bounds [896,1707][1022,1833]
Note: the parent (not the bell node) carries checkable/checked; the spoken text is on the bell child.

## 2. Undo re-arms the alarm
Counts are `adb shell dumpsys alarm | grep -c com.sajdatime.app`.
- Attempt 1 (failed): A=198, B=190 (Dhuhr off). Undo node found at bounds [903,1973][995,2020]; tap (949,1996) came after a separate dump command (delay not measured). C=190. Screen then showed the "Different from your mosque?" sheet (dump-2-after-undo.xml), so the Undo bar had gone and the tap hit the main-screen button. Sheet closed by tapping Close at (815,2211) (bounds [766,2185][865,2238]); see dump-2-after-close-sheet.xml.
- Attempt 2 (failed, same cause): A=198 (on), B=190 (off). Tap (949,1996) after a separate python lookup (delay not measured). C=190. Mosque sheet opened again (dump-2-retry-after-undo.xml).
- Snapshot during attempt 2 state (scratch-undo.png, taken about 1s after Dhuhr off): snackbar text "Dhuhr alert off" with "Undo" at the bounds above; screenshot was taken, not tapped.
- Attempt 3 (success, no dump between off-tap and Undo-tap): Dhuhr on (wait 6s) A=198; Dhuhr off (wait 1s) B=190; tap Undo (949,1996); wait 3s C=198.
- Dhuhr content-desc after Undo (dump-2-final-after-undo.xml): "Dhuhr alert on, notification". Undo text no longer present. Mosque sheet not opened.
So the successful run: A=198, B=190, C=198, i.e. B < A and C == A.
Timing caveat: the Undo bar disappears within about 2 to 3 seconds of showing, so the first two attempts' extra latency is the likely cause of the missed taps. Counts from attempt 3 are the reported result.

## 3. Urdu (locale ur)
- Set `cmd locale set-app-locales com.sajdatime.app --locales ur`, rc=0; launch; screenshot 1-urdu-times.png.
- Bell content-descs before tap (dump-3-urdu.xml): "فجر کا الرٹ آن، اطلاع", "ظہر کا الرٹ آن، اطلاع" (bounds [58,1376][184,1502]), "عصر کا الرٹ آن، اطلاع", "مغرب کا الرٹ آن، اطلاع", "عشاء کا الرٹ آن، اطلاع".
- Tapped Dhuhr bell (121,1439). After 0.8s: Dhuhr desc "ظہر کا الرٹ بند"; snackbar text "ظہر کا الرٹ بند" bounds [792,1945][1006,2012] with action "کالعدم کریں" visible (screenshot 2-urdu-bell-off-message.png).
- After 5s, tapped same bell again: Dhuhr desc "ظہر کا الرٹ آن، اطلاع" (dump-3-urdu-after-on.xml); all five on.
- `set-app-locales --locales ""` rc=0; `get-app-locales` after returns "[]".

## 4. Small phone, large text
- font_scale 2.0; wm density 540 (=320dp width at 1080px); launch; one swipe 540,1700 to 540,900 (300ms); screenshot 3-320dp-text2-rows.png; dump-4-font2.xml.
- Text bounds from dump: "Today" [54,206][309,454]; "Fajr" row name at [263,699] region; "Sunrise" [223,1025][513,1260]; "Dhuhr" [233,1269][419,1523]; "Asr" [233,1556][379,1671]; "Maghrib" [233,1704][476,1931]; "Now" [263,2182][445,2284].
- Breaks visible in screenshot 3: "Today" splits to "Toda" / "y"; "Fajr" stacks as F / a / j / r on separate lines; "Sunrise" splits to "Sunris" / "e"; "Dhuhr" to "Dhu" / "hr"; "Maghrib" to "Mag" / "hrib". Nav label "My prayers" wraps onto two lines. Isha row is clipped under the bottom nav.
- Reset: font_scale 1.0; wm density reset (now reports Physical density 420).

## 5. Dark
- `cmd uimode night yes`; launch; screenshot 4-dark-times.png (dark background, Fajr Next, all five bells on); dump-5-dark.xml: all five "alert on, notification". `cmd uimode night no` afterwards.

## 6. Final state (dump-6-final.xml, checked 04:02 BST)
- Notifications granted=true; SCHEDULE_EXACT_ALARM allow; font_scale 1.0; density reset (Physical 420); night mode no; app locales []; all five bells "alert on, notification".

## Screenshots
- 1-urdu-times.png: Urdu RTL times screen, all five bells on; times stay "AM 6:01" / "PM 12:45" with English meridiem and digits.
- 2-urdu-bell-off-message.png: snackbar "ظہر کا الرٹ بند" with "کالعدم کریں" action; Dhuhr bell shows off.
- 3-320dp-text2-rows.png: see breaks listed in section 4.
- 4-dark-times.png: dark mode times, no breaks, all bells on.
- scratch-state.png: main screen after closing the sheet (not a check).
- scratch-undo.png: snackbar "Dhuhr alert off" with Undo, taken during attempt 2.

## Not done / gaps
- No TalkBack run; spoken text taken from uiautomator content-desc only.
- Undo tap timing: three attempts; first two missed the bar (sheet opened), third succeeded.
