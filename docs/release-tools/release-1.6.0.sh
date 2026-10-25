#!/bin/bash
# SajdaTime 1.6.0 (My prayers, bells, Timetable, jama'ah times, skies, the widget strip):
# push the branch, run every check, build both bundles, retake the store screenshots. Copied from
# release-1.5.0.sh on 25 Oct 2026 (which stays beside it as the record of the last release). Safe to run
# again: each step reports PASS, FAIL, CHECK, TAKEN or SKIPPED. It never prints or opens the signing key.
#
# What changed from 1.5.0, and why (docs/plans/2026-10-release-1.6.0/STEP-11-DESIGN.md):
# - Phone only: 10 / 1.6.0. The watch stays 1002 / 1.5.0, so its bundle is only built and checked as a gate,
#   never copied into the folder of files to upload (Play refuses a repeated versionCode anyway).
# - Twelve permissions: VIBRATE (normal, no prompt) was added in step 6.
# - The device suite also leaves out TrackerSeedDeviceTest, which is a seed, not a check.
# - Step 6j retakes the store screenshots (8 English, 4 each in ar, ur, tr-TR, id-ID; 05 by hand) into
#   docs/store/screenshots/raw/phone/<folder>/, only with STORE_SHOTS=1 set (STORE_SHOTS=1 bash docs/release-tools/release-1.6.0.sh) on a 1080x1920 screen,
#   the sajdastore emulator; otherwise SKIPPED, so a normal run never overwrites the tracked pictures. The Next prayer widget picture
#   (05) is always taken by hand: the script prints the steps and records it as MANUAL.
# - Expected unit test counts are checked, not just printed.
#
# Results land in <the branch's folder>/build/release-1.6.0/ (summary.txt first), and the signed bundle
# in ~/Developer/SajdaTime/SajdaTime-1.6.0/ (gitignored). Nothing here submits anything to Google Play.
# The owner presses submit, one screen at a time.

set -o pipefail
MAIN_REPO="$HOME/Developer/SajdaTime"
BRANCH="${SAJDA_BRANCH:-claude/1-6-0}"
REPO="$(git -C "$MAIN_REPO" worktree list --porcelain 2>/dev/null \
  | awk -v b="refs/heads/$BRANCH" '/^worktree /{w=substr($0,10)} $0=="branch "b{print w; exit}')"
[ -n "$REPO" ] || REPO="$MAIN_REPO"
OUT="$REPO/build/release-1.6.0"
PKG="com.sajdatime.app"
mkdir -p "$OUT"
rm -f "$OUT/finished" "$OUT"/*.png "$OUT"/*.xml "$OUT"/test-*.txt
rm -rf "$OUT"/renders-*
SUM="$OUT/summary.txt"
: > "$SUM"
exec > >(tee "$OUT/log.txt") 2>&1

step()   { echo; echo "===== $1 ====="; }
result() { echo "$1: $2" | tee -a "$SUM"; }

cd "$REPO" || { echo "Cannot find $REPO"; exit 1; }
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
SDK="$(sed -n 's/^sdk.dir=//p' local.properties)"
export ANDROID_HOME="$SDK"
ADB="$SDK/platform-tools/adb"
EMU="$SDK/emulator/emulator"
AAPT2="$(ls -d "$SDK"/build-tools/*/aapt2 2>/dev/null | tail -1)"
START_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
echo "Started $(date). Working in $REPO, on $START_BRANCH. JDK $JAVA_HOME. SDK $SDK."

step "1. Push the branch to GitHub"
if git push -u origin "$BRANCH"; then result push PASS; else result push FAIL; fi

step "2. On the branch, and what is being built"
DIRTY="$(git status --porcelain --untracked-files=all | cut -c4- | tr '\n' ' ')"
if [ "$START_BRANCH" != "$BRANCH" ]; then
  if [ -n "$DIRTY" ]; then result checkout "FAIL (uncommitted changes, nothing was touched: $DIRTY)"; touch "$OUT/finished"; exit 1; fi
  if git switch "$BRANCH"; then result checkout PASS; else result checkout FAIL; touch "$OUT/finished"; exit 1; fi
fi
# Built from the working tree. A clean tree with push PASS means the bundles are exactly the pushed commit.
if [ -z "$DIRTY" ]; then result tree "PASS ($(git rev-parse --short HEAD), nothing uncommitted)"
else result tree "CHECK ($(git rev-parse --short HEAD) plus uncommitted changes, so the bundles are not a pushed commit: $DIRTY)"; fi

step "3. Full release gate: clean, unit tests, lint, both bundles"
# --no-build-cache so every test really runs: with the cache on, clean can still restore test results.
GATE=fail
if ./gradlew --no-build-cache clean test lint :app:bundleRelease :wear:bundleRelease; then
  # No apostrophe anywhere in this heredoc, not even in a comment: bash 3.2 cannot parse it inside $( ).
  COUNTS="$(python3 - <<'EOF'
import glob, xml.etree.ElementTree as ET
t = {}
for f in glob.glob('*/build/test-results/test*UnitTest/TEST-*.xml'):
    r = ET.parse(f).getroot(); m = f.split('/')[0]; a = t.setdefault(m, [0, 0])
    a[0] += int(r.get('tests')); a[1] += int(r.get('failures')) + int(r.get('errors'))
print(', '.join(f'{m} {a[0]} tests {a[1]} failed' for m, a in sorted(t.items())))
EOF
)"
  LINT="$(for f in */build/reports/lint-results-debug.xml; do printf '%s %s errors, ' "${f%%/*}" "$(grep -cE 'severity="(Error|Fatal)"' "$f")"; done)"
  # Gate counts at 25 Oct 2026 (core 237 tests, 1 of them skipped; app 322; wear 27). Update with the code.
  EXPECTED_COUNTS="app 323 tests 0 failed, core 238 tests 0 failed, wear 27 tests 0 failed"
  if [ "$COUNTS" = "$EXPECTED_COUNTS" ]; then result gate "PASS ($COUNTS; lint: ${LINT%, })"
  else result gate "CHECK (counts are $COUNTS, expected $EXPECTED_COUNTS; lint: ${LINT%, })"; fi
  GATE=pass
else result gate FAIL; fi

step "4. bundletool"
BT="$OUT/bundletool.jar"
[ -f "$BT" ] || cp "$MAIN_REPO/build/release-1.4.0/bundletool.jar" "$BT" 2>/dev/null \
  || curl -fsSL -o "$BT" https://github.com/google/bundletool/releases/download/1.18.3/bundletool-all-1.18.3.jar
if java -jar "$BT" version; then result bundletool PASS; else result bundletool FAIL; fi

step "5. Signed bundles: signature, version, languages, permissions, size"
DEST="$MAIN_REPO/SajdaTime-1.6.0"
mkdir -p "$DEST"
rm -f "$DEST"/*.aab "$DEST/sha256.txt"  # an earlier run's bundles must not get this run's checksums
check_bundle() {  # label, aab, expected versionCode, expected versionName, copy (yes/no)
  local label="$1" aab="$2" want_vc="$3" want_vn="$4" copy="$5" verify signer vc vn
  verify=""; [ -f "$aab" ] && verify="$("$JAVA_HOME/bin/jarsigner" -verify -verbose -certs "$aab" 2>&1)"
  # Not `echo | grep -q`: under pipefail grep -q quits at the match, echo gets SIGPIPE on jarsigner's
  # long output and a good bundle reads as unsigned (independent review, 18 Oct 2026: 8 to 100 of 300 runs).
  if ! grep -q "jar verified" <<< "$verify"; then
    if [ "$GATE" = fail ]; then result "$label" "FAIL (not built, because the gate above failed)"
    else result "$label" "FAIL (no signed bundle: are the sajdatime.* values in ~/.gradle/gradle.properties?)"; fi
    return 1
  fi
  signer="$(echo "$verify" | grep -o 'CN=[^,]*' | head -1)"
  vc="$(java -jar "$BT" dump manifest --bundle="$aab" --xpath /manifest/@android:versionCode 2>/dev/null)"
  vn="$(java -jar "$BT" dump manifest --bundle="$aab" --xpath /manifest/@android:versionName 2>/dev/null)"
  [ "$copy" = yes ] && cp "$aab" "$DEST/"; cp "$aab" "$OUT/"
  result "$label" "PASS (signed by $signer, versionCode $vc, versionName $vn, $(wc -c < "$aab" | tr -d ' ') bytes, $([ "$copy" = yes ] && echo "copied to SajdaTime-1.6.0" || echo "gate check only, not for upload"))"
  case "$signer" in *"Android Debug"*) result "$label-signer" "FAIL (debug key, do not upload)";; esac
  [ "$vc" = "$want_vc" ] || result "$label-version" "FAIL (expected versionCode $want_vc)"
  [ "$vn" = "$want_vn" ] || result "$label-version-name" "FAIL (expected versionName $want_vn)"
}
AAB="app/build/outputs/bundle/release/app-release.aab"
WAAB="wear/build/outputs/bundle/release/wear-release.aab"
if check_bundle bundle "$AAB" 10 1.6.0 yes; then
  # What Google Play would install on an English-only Galaxy S23 Ultra, from this exact file.
  echo '{"supportedAbis":["arm64-v8a"],"supportedLocales":["en-GB"],"screenDensity":480,"sdkVersion":35}' > "$OUT/s23-en.json"
  rm -rf "$OUT/s23-en" && mkdir -p "$OUT/s23-en"
  # A throwaway key, only to unpack the bundle for this check. It signs nothing that ships.
  [ -f "$OUT/throwaway.jks" ] || "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$OUT/throwaway.jks" -storepass throwaway \
       -keypass throwaway -alias t -keyalg RSA -keysize 2048 -validity 30 -dname "CN=throwaway split check" >/dev/null 2>&1
  if java -jar "$BT" build-apks --bundle="$AAB" --output="$OUT/s23-en.apks" --device-spec="$OUT/s23-en.json" --overwrite \
       --ks="$OUT/throwaway.jks" --ks-pass=pass:throwaway --ks-key-alias=t --key-pass=pass:throwaway \
     && unzip -q -o "$OUT/s23-en.apks" -d "$OUT/s23-en"; then
    LANGS="$(for a in "$OUT"/s23-en/splits/*.apk; do "$AAPT2" dump resources "$a" 2>/dev/null; done \
      | awk '/string\/app_language_tag$/{f=1;next} f&&/resource 0x/{f=0} f' | grep -o '([a-z]*)' | sort -u | tr -d '\n')"
    case "$LANGS" in *"(ar)"*"(in)"*"(tr)"*"(ur)"*) result play-languages "PASS (English-only phone gets $LANGS)";;
                     *) result play-languages "FAIL (English-only phone gets only $LANGS)";; esac
  else
    result play-languages "FAIL (bundletool could not build the APK set)"
  fi
  # Nothing slipped in. 1.6.0 adds one, android.permission.VIBRATE (normal, no prompt, step 6): the phone list is twelve.
  PERMS="$(java -jar "$BT" dump manifest --bundle="$AAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  EXPECTED_PERMS="android.permission.ACCESS_COARSE_LOCATION android.permission.ACCESS_NETWORK_STATE android.permission.ACCESS_NOTIFICATION_POLICY android.permission.FOREGROUND_SERVICE android.permission.INTERNET android.permission.POST_NOTIFICATIONS android.permission.RECEIVE_BOOT_COMPLETED android.permission.SCHEDULE_EXACT_ALARM android.permission.VIBRATE android.permission.WAKE_LOCK com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION "
  if [ "$PERMS" = "$EXPECTED_PERMS" ]; then result permissions "PASS (the eleven of 1.5.0 plus VIBRATE)"; else result permissions "FAIL (the permission list changed: $PERMS)"; fi
  # Measured 25 Oct 2026 on the local signed build of this branch: 9,952,988 bytes (1.5.0 was
  # 9,475,100). The limit allows 300 KB on top, as 1.5.0's did over 1.4.0. Re-measure at the release commit.
  SIZE="$(wc -c < "$AAB" | tr -d ' ')"
  if [ "$SIZE" -le 10252988 ]; then result bundle-size "PASS ($SIZE bytes, measured 9952988 on 25 Oct 2026)"
  else result bundle-size "FAIL ($SIZE bytes, more than 300 KB over the 9952988 measured on 25 Oct 2026)"; fi
fi
if check_bundle watch-bundle "$WAAB" 1002 1.5.0 no; then
  # The watch asks for three permissions, unchanged by 1.6.0 (comments only in wear/ since main).
  WPERMS="$(java -jar "$BT" dump manifest --bundle="$WAAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  if [ "$WPERMS" = "android.permission.ACCESS_COARSE_LOCATION android.permission.WAKE_LOCK com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION " ]
  then result watch-permissions "PASS (the same three)"; else result watch-permissions "FAIL (the permission list changed: $WPERMS)"; fi
fi
( cd "$DEST" && shasum -a 256 *.aab | tee sha256.txt )

step "6. Emulator: on-device tests, the Ramadan clock stages, then look at the screens"
# The first running phone emulator: a watch one has the same package name, so it would get the phone
# app and fail every test.
phone_emulator() {
  local s
  for s in $("$ADB" devices | awk '/^emulator-/ && $2=="device" {print $1}'); do
    "$ADB" -s "$s" shell getprop ro.build.characteristics | grep watch >/dev/null || { echo "$s"; return; }
  done
}
EMU_SERIAL="$(phone_emulator)"
STARTED_EMU=""
if [ -z "$EMU_SERIAL" ] && [ -x "$EMU" ]; then
  AVD=""
  for a in $("$EMU" -list-avds); do
    CFG="$HOME/.android/avd/$a.avd/config.ini"
    if grep -q 'image.sysdir.*android-3[3-9]' "$CFG" 2>/dev/null && ! grep -qi 'wear\|tv' "$CFG"; then AVD="$a"; break; fi
  done
  if [ -n "$AVD" ]; then
    echo "Starting emulator $AVD, read only"
    "$EMU" -avd "$AVD" -read-only -no-window -no-audio -no-boot-anim >"$OUT/emulator.log" 2>&1 &
    STARTED_EMU=1
    for i in $(seq 1 120); do
      EMU_SERIAL="$(phone_emulator)"
      [ -n "$EMU_SERIAL" ] && [ "$("$ADB" -s "$EMU_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
      sleep 5
    done
  fi
fi
if [ -n "$EMU_SERIAL" ]; then
  export ANDROID_SERIAL="$EMU_SERIAL"
  echo "Emulator $EMU_SERIAL, Android $("$ADB" shell getprop ro.build.version.release | tr -d '\r')"
  TESTPKG="$PKG.test"
  RUNNER="$TESTPKG/androidx.test.runner.AndroidJUnitRunner"

  # Helpers. The clock is set in the emulator's own time zone, which the script first makes London so
  # the screenshots show the times a person in London would see.
  set_clock() {   # MMDDhhmmYYYY, for example 022210002027 for 22 Feb 2027 10:00
    "$ADB" shell settings put global auto_time 0 >/dev/null 2>&1
    "$ADB" shell settings put global auto_time_zone 0 >/dev/null 2>&1
    "$ADB" shell "date $1.00" >/dev/null 2>&1
    "$ADB" shell am broadcast -a android.intent.action.TIME_SET >/dev/null 2>&1
    sleep 2
  }
  instrument() {  # label, then the am instrument -e arguments
    local label="$1"; shift
    "$ADB" shell am instrument -w "$@" "$RUNNER" > "$OUT/test-$label.txt" 2>&1
    local line; line="$(grep -E '^OK \(|^Tests run:' "$OUT/test-$label.txt" | tail -1 | tr -d '\r')"
    if grep -q '^OK (' "$OUT/test-$label.txt"; then result "$label" "PASS ($line)"
    else result "$label" "FAIL (${line:-no result}, see test-$label.txt)"; fi
  }
  pull_renders() { # stage
    rm -rf "$OUT/renders-$1"; mkdir -p "$OUT/renders-$1"
    "$ADB" pull /sdcard/Android/data/$PKG/files/device-renders/. "$OUT/renders-$1/" >/dev/null 2>&1 || true
    "$ADB" shell rm -rf /sdcard/Android/data/$PKG/files/device-renders >/dev/null 2>&1
  }
  shot() {        # file name without extension
    "$ADB" exec-out screencap -p > "$OUT/$1.png"
  }
  tap_pat() {     # regex for the text or content-desc, found through the UI dump as the Do Not Disturb check does
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
    "$ADB" pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null 2>&1
    local b
    b="$(grep -oE "(text|content-desc)=\"$1\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" "$OUT/ui.xml" | head -1 | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]')"
    [ -z "$b" ] && return 1
    # "[127,1385][365,1438]" becomes "127,1385,365,1438" (a separator, or 1385 and 365 glue into one number).
    local c x y
    c="$(echo "$b" | sed 's/\]\[/,/; s/[][]//g' | awk -F, '{print int(($1+$3)/2), int(($2+$4)/2)}')"
    x="${c% *}"; y="${c#* }"
    "$ADB" shell input tap "$x" "$y"; sleep 2
    return 0
  }
  tap_text()   { tap_pat "$1"; }          # the whole visible text
  tap_prefix() { tap_pat "$1[^\"]*"; }    # a row that may read "Dhuhr, 1:05 pm" (step 11 design, verifier finding 7)
  open_app() {
    "$ADB" shell am force-stop "$PKG"; sleep 1
    "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 5
  }

  # --- 6a. Start from nothing, so the result is this build's (a stale copy with a higher version code once
  # failed the install with INSTALL_FAILED_VERSION_DOWNGRADE). A sideload copy is left alone: it is a
  # different package, and DeviceSupport.alarmTimes matches the package exactly since 18 Oct 2026.
  echo "Installed on the emulator before the run: $("$ADB" shell pm list packages --show-versioncode 2>/dev/null | grep -i sajdatime | tr '\r\n' '  ')"
  "$ADB" uninstall "$TESTPKG" >/dev/null 2>&1
  "$ADB" uninstall "$PKG" >/dev/null 2>&1
  if ./gradlew :app:installDebug :app:installDebugAndroidTest; then
    "$ADB" root >/dev/null 2>&1; sleep 3
    "$ADB" shell appwidget grantbind --package "$PKG" --user 0 >/dev/null 2>&1 \
      && result widget-bind-grant PASS || result widget-bind-grant "CHECK (the grant command failed; WidgetDeviceTest will say so)"
    "$ADB" shell service call alarm 3 s16 Europe/London >/dev/null 2>&1
    echo "Emulator time zone: $("$ADB" shell getprop persist.sys.timezone | tr -d '\r')"

    # --- 6b. The whole on device suite at the emulator's own clock, widgets included. Left out: the suhoor
    # reminder test (it needs the clock inside Ramadan, 6d runs it) and the two seeds, which are not checks.
    instrument device-suite -e notClass com.sajdatime.app.SuhoorReminderDeviceTest,com.sajdatime.app.ScreenSeedDeviceTest,com.sajdatime.app.TrackerSeedDeviceTest

    # The Do Not Disturb access list, with SajdaTime in it (as in 1.3.2).
    "$ADB" shell am start -a android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS >/dev/null; sleep 4
    shot emulator-dnd-list
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" pull /sdcard/ui.xml "$OUT/emulator-dnd-list.xml" >/dev/null 2>&1
    if grep -q 'SajdaTime' "$OUT/emulator-dnd-list.xml" 2>/dev/null; then result emulator-dnd-list "PASS (SajdaTime is listed)"; else result emulator-dnd-list "CHECK (see emulator-dnd-list.png)"; fi

    # --- 6c. The app in each language at the emulator's own clock.
    for L in ur tr id ar en; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 4
      shot "emulator-app-$L"
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "" >/dev/null 2>&1

    # --- 6d. Day 14 of Ramadan, 23:00 on 21 February 2027: the suhoor reminder in dumpsys alarm.
    set_clock 022123002027
    instrument suhoor-reminder-alarms -e class com.sajdatime.app.SuhoorReminderDeviceTest -e requireRamadanClock true

    # --- 6e. Day 15, 10:00 on 22 February 2027: the Ramadan checks, both widgets bound for real, the PDFs.
    set_clock 022210002027
    instrument ramadan-calendar -e class com.sajdatime.app.RamadanDeviceTest
    instrument widgets-ramadan -e class com.sajdatime.app.WidgetDeviceTest -e requireRamadanClock true
    pull_renders widgets-ramadan
    "$ADB" shell settings put system font_scale 2.0 >/dev/null 2>&1
    instrument widgets-font-2 -e class com.sajdatime.app.WidgetDeviceTest
    pull_renders widgets-font-2
    "$ADB" shell settings put system font_scale 1.0 >/dev/null 2>&1
    instrument ramadan-pdf -e class com.sajdatime.app.RamadanPdfDeviceTest
    pull_renders pdf

    # --- 6f. The screens with the clock in Ramadan, in each language. Seed first so the first run screens are skipped.
    instrument screen-seed -e class com.sajdatime.app.ScreenSeedDeviceTest#seed_london_and_skip_the_first_run_screens
    for L in en ur tr id ar; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      open_app
      shot "ramadan-times-$L"
      "$ADB" shell input swipe 540 1800 540 700 400; sleep 1
      shot "ramadan-times-$L-scrolled"
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "en" >/dev/null 2>&1; sleep 2
    open_app
    # Tap the button, then prove the sheet opened by finding its title ("Ramadan 1448") in the UI dump.
    # The label is ramadan_open_timetable, "Get the full Ramadan timetable" since 18 Oct 2026.
    if ! tap_text "Get the full Ramadan timetable"; then
      "$ADB" shell input swipe 540 1800 540 700 400; sleep 1
      tap_text "Get the full Ramadan timetable" || result ramadan-timetable "CHECK (could not find the timetable button, see ramadan-times-en.png and ui.xml)"
    fi
    if ! grep -q 'ramadan-timetable' "$OUT/summary.txt" 2>/dev/null; then
      sleep 2
      "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" pull /sdcard/ui.xml "$OUT/ramadan-timetable-en.xml" >/dev/null 2>&1
      shot ramadan-timetable-en
      if grep -q 'text="Ramadan 14[0-9][0-9]"' "$OUT/ramadan-timetable-en.xml" 2>/dev/null; then
        result ramadan-timetable "PASS (the sheet opened, title found, see ramadan-timetable-en.png)"
        "$ADB" shell input swipe 540 1700 540 500 400; sleep 1; shot ramadan-timetable-en-scrolled
      else
        result ramadan-timetable "CHECK (tapped the button but the sheet title was not on screen, see ramadan-timetable-en.png and ramadan-timetable-en.xml)"
      fi
    fi

    # --- 6g. The other days that change what the app says. English only.
    for STAGE in "eve-of-ramadan:020612002027" "last-day-evening:030820002027" "eid-al-fitr:030910002027" "eid-al-adha:051610002027"; do
      NAME="${STAGE%%:*}"; WHEN="${STAGE##*:}"
      set_clock "$WHEN"; open_app; shot "stage-$NAME"
    done
    # Back to a day that is not Ramadan, to see that the card is gone and nothing is left behind.
    set_clock 062110002027; open_app; shot stage-after-ramadan

    # --- 6h. The app mirrored, as a layout check only (CLAUDE.md: judge the mirroring, never the prose).
    set_clock 022210002027
    # :app: only. A bare installRtl also runs the watch's rtl build, which has no package suffix, so it
    # installed the watch app (1002) over com.sajdatime.app on this phone (18 Oct 2026; HANDOVER §15 lesson 149).
    if ./gradlew :app:installRtl >/dev/null 2>&1; then
      "$ADB" shell am start -W -n "$PKG.rtl/com.sajdatime.app.MainActivity" >/dev/null 2>&1; sleep 5
      shot rtl-first-screen
      "$ADB" uninstall "$PKG.rtl" >/dev/null 2>&1
      result rtl-layout "TAKEN (rtl-first-screen.png, judge the mirroring only)"
    else
      result rtl-layout "SKIPPED (installRtl did not build)"
    fi

    # --- 6j. The store screenshots (STEP-11-DESIGN.md §1): English 01 to 04, 06, 07, 08 into raw/phone/en-GB
    # (05 is by hand), and 01 to 04 for Arabic, Urdu, Turkish and Indonesian into raw/phone/<ar|ur|tr-TR|id-ID>.
    # Needs STORE_SHOTS=1 and the sajdastore AVD (1080x1920, see docs/store/upload/README.md). Otherwise the whole
    # block is skipped, because wrong-size pictures must never land in the repo. The screen words are read
    # from the app's own strings files, so the taps work in every language. Every tap that fails is reported
    # CHECK and the picture is still taken: look at it.
    STORE="$REPO/docs/store/screenshots/raw/phone"
    SIZE_NOW="$("$ADB" shell wm size | grep -o '[0-9]*x[0-9]*' | tail -1)"
    if [ "${STORE_SHOTS:-0}" != "1" ]; then
      result store-screenshots "SKIPPED (set STORE_SHOTS=1 on the sajdastore emulator to retake)"
    elif [ "$SIZE_NOW" != "1080x1920" ]; then
      result store-screenshots "SKIPPED (needs the sajdastore AVD at 1080x1920; this screen is $SIZE_NOW)"
    else
    str() {  # values folder suffix, string name: the app's own words
      sed -n "s|.*name=\"$2\"[^>]*>\(.*\)</string>.*|\1|p" "app/src/main/res/values$1/strings.xml" | head -1
    }
    suffix_of() { case "$1" in en) echo "";; id) echo "-in";; *) echo "-$1";; esac; }
    folder_of() { case "$1" in en) echo en-GB;; tr) echo tr-TR;; id) echo id-ID;; *) echo "$1";; esac; }
    demo_bar() {  # HHMM: a clean status bar, system chrome only
      "$ADB" shell settings put global sysui_demo_allowed 1
      "$ADB" shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
      "$ADB" shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm "$1" >/dev/null
      "$ADB" shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null
      "$ADB" shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e fully true >/dev/null  # else a wifi exclamation shows
      "$ADB" shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null
    }
    take() {  # app locale, file name without .png
      local dir; dir="$STORE/$(folder_of "$1")"; mkdir -p "$dir"
      "$ADB" exec-out screencap -p > "$dir/$2.png"
      result "store-$(folder_of "$1")-$2" "TAKEN ($dir/$2.png)"
    }
    go_tab() {  # app locale, string name of the tab
      tap_text "$(str "$(suffix_of "$1")" "$2")" || result "store-tab-$1-$2" "CHECK (tab not found in the UI dump)"
      sleep 1
    }
    set_theme() {  # theme_dark or theme_system, in English. The choice can sit under the floating tab bar: swipe up and retry.
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales en >/dev/null 2>&1; sleep 2
      open_app; go_tab en nav_settings
      tap_text "$(str "" "$1")" \
        || { "$ADB" shell input swipe 540 1400 540 700 400; sleep 1; tap_text "$(str "" "$1")"; } \
        || { "$ADB" shell input swipe 540 1400 540 700 400; sleep 1; tap_text "$(str "" "$1")"; } \
        || result "store-theme-$1" "CHECK (theme choice not found; set it by hand)"
    }
    # PLAN.md R1 (a scheduler change breaks a prayer alarm) is not counted here: in 1.6.0 PrayerAlarmScheduler
    # changed only in a comment, and the device suite in 6b already checks the alarms are set. The widget's new
    # sunrise boundary (WidgetRefreshScheduler) is a widget refresh, not a prayer alarm.

    # Fresh start so nothing from the checks above is in the pictures.
    "$ADB" shell pm clear "$PKG" >/dev/null 2>&1
    "$ADB" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1
    "$ADB" shell cmd appops set "$PKG" SCHEDULE_EXACT_ALARM allow >/dev/null 2>&1
    "$ADB" shell cmd notification allow_dnd "$PKG" >/dev/null 2>&1
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales en >/dev/null 2>&1

    # Light pictures, 15 Aug 2026 15:00 London.
    set_clock 081515002026; demo_bar 1500
    instrument store-seed-places -e class com.sajdatime.app.ScreenSeedDeviceTest#seed_london_and_skip_the_first_run_screens
    instrument store-seed-tracker -e class com.sajdatime.app.TrackerSeedDeviceTest#seed_a_week_of_ticks_with_today_started
    # The AVD's own GPS fix is Manchester and the app would move itself there. With the permission revoked it
    # keeps the seeded London, and Times shows no banner.
    "$ADB" shell pm revoke "$PKG" android.permission.ACCESS_COARSE_LOCATION >/dev/null 2>&1
    open_app
    take en 01-times-light
    go_tab en nav_tracker
    # Dhuhr is ticked as a tracker user would have by 3 pm. The row's content-desc is "Dhuhr, not ticked".
    tap_prefix "Dhuhr" || result store-dhuhr-tick "CHECK (Dhuhr row not found, 02 shows Fajr only)"
    sleep 1
    take en 02-my-prayers
    "$ADB" emu sensor set acceleration 0:0:9.81 >/dev/null 2>&1
    "$ADB" emu sensor set magnetic-field -17.3:10:-45 >/dev/null 2>&1
    go_tab en nav_qibla; sleep 3; take en 03-qibla
    go_tab en nav_times
    if tap_text "$(str "" action_timetable)"; then sleep 2; take en 06-timetable; "$ADB" shell input keyevent 4; sleep 1
    else result store-en-06-timetable "CHECK (Timetable button not found)"; fi
    for L in ur tr id; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      open_app
      take "$L" 01-times-light
      go_tab "$L" nav_tracker; take "$L" 02-my-prayers
      go_tab "$L" nav_qibla; sleep 3; take "$L" 03-qibla
    done
    # Arabic light is its own pass, on Thursday 20 Aug 2026 15:00 London. Arabic weeks start on Saturday, so on
    # Saturday 15 Aug the My prayers week card shows only today; on Thursday 20 Aug it is full (Fable's verdict,
    # 25 Oct 2026; commit 0ca400f retook them this way by hand). The dark pictures below set their own clock.
    set_clock 082015002026; demo_bar 1500
    instrument store-seed-tracker-clear-ar -e class com.sajdatime.app.TrackerSeedDeviceTest#clear_the_record_and_switch_tracking_off
    instrument store-seed-tracker-ar -e class com.sajdatime.app.TrackerSeedDeviceTest#seed_a_week_of_ticks_with_today_started
    "$ADB" shell pm revoke "$PKG" android.permission.ACCESS_COARSE_LOCATION >/dev/null 2>&1
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales ar >/dev/null 2>&1; sleep 2
    open_app
    take ar 01-times-light
    go_tab ar nav_tracker
    # The row's content-desc is "<prayer_dhuhr>، <tracker_state_not_ticked>", "الظهر، بلا علامة". prayer_dhuhr
    # lives in core's strings, not the app's, so it is read from there.
    AR_DHUHR="$(sed -n 's|.*name="prayer_dhuhr"[^>]*>\(.*\)</string>.*|\1|p' core/src/main/res/values-ar/strings.xml | head -1)"
    tap_prefix "$AR_DHUHR" || result store-ar-dhuhr-tick "CHECK (Arabic Dhuhr row not found, 02 shows Fajr only)"
    sleep 1
    take ar 02-my-prayers
    go_tab ar nav_qibla; sleep 3; take ar 03-qibla

    # Dark pictures, 22 Feb 2027 15:00 London. Theme is set in the app (its DataStore), then put back.
    set_clock 022215002027; demo_bar 1500
    set_theme theme_dark
    for L in en ar ur tr id; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      open_app; go_tab "$L" nav_times
      take "$L" 04-ramadan-dark
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales en >/dev/null 2>&1; sleep 2
    open_app; go_tab en nav_settings
    "$ADB" shell input swipe 540 1500 540 600 400; sleep 1
    if tap_text "$(str "" settings_suhoor)"; then sleep 1; take en 08-ramadan-suhoor-reminder; "$ADB" shell input keyevent 4
    else result store-en-08-ramadan-suhoor-reminder "CHECK (Suhoor reminder row not found; scroll to it and take it by hand)"; fi
    set_theme theme_system

    # Picture 07, back in light: the store alert settings, Settings scrolled until the Reminders header is at the top.
    set_clock 081515002026; demo_bar 1500
    instrument store-seed-alerts -e class com.sajdatime.app.ScreenSeedDeviceTest#seed_store_alert_settings
    open_app; go_tab en nav_settings
    "$ADB" shell input swipe 540 1550 540 230 2000; sleep 1   # slow, from the Theme position; look at the picture
    take en 07-alerts-and-adhan

    # Picture 05 (widgets) is always taken by hand, so the script does not pause for it.
    echo
    echo "NOTE: store picture 05 (widgets) is taken by hand on the emulator's launcher: open the widget picker,"
    echo "tap the preview, Add; resize the Next widget's right handle to four columns; clock 081518302026 and"
    echo "demo clock 1830. Save it as $STORE/en-GB/05-widgets.png."
    result store-en-05-widgets "MANUAL"

    # Leave the emulator as found: the record and tracking off, the app's data cleared.
    set_clock 081515002026
    instrument store-seed-clear -e class com.sajdatime.app.TrackerSeedDeviceTest#clear_the_record_and_switch_tracking_off
    "$ADB" shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null 2>&1
    "$ADB" shell pm clear "$PKG" >/dev/null 2>&1
    instrument store-seed-reset -e class com.sajdatime.app.ScreenSeedDeviceTest#seed_london_and_skip_the_first_run_screens
    result store-screenshots "TAKEN (look at every picture in $STORE before committing; raw files only, run tools/build-store-assets.sh next)"
    fi

    # --- 6i. Put the emulator back.
    "$ADB" shell settings put system font_scale 1.0 >/dev/null 2>&1
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "" >/dev/null 2>&1
    "$ADB" shell settings put global auto_time 1 >/dev/null 2>&1
    "$ADB" shell settings put global auto_time_zone 1 >/dev/null 2>&1
    result emulator-screens "TAKEN (emulator-*, ramadan-*, stage-*, rtl-* and renders-* in $OUT)"
  else
    result emulator-tests "FAIL (could not install the debug build)"
  fi
  [ -n "$STARTED_EMU" ] && "$ADB" emu kill >/dev/null 2>&1
  unset ANDROID_SERIAL
else
  result emulator-tests "SKIPPED (no Android 13+ phone emulator found or it did not boot)"
fi

step "7. Samsung phone (off by default)"
# Ali decided on 11 Oct 2026, and again on 17 Oct for 1.5.0, that the emulator is enough. TEST_PHONE=1 runs it anyway.
PHONE=""
[ "${TEST_PHONE:-0}" = "1" ] && PHONE="$("$ADB" devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}')"
if [ -z "$PHONE" ]; then
  result phone "SKIPPED (emulator only, by Ali's choice)"
else
  export ANDROID_SERIAL="$PHONE"
  echo "Phone $PHONE: $("$ADB" shell getprop ro.product.model | tr -d '\r'), Android $("$ADB" shell getprop ro.build.version.release | tr -d '\r')"
  if ./gradlew -PtestBuildType=sideload :app:bundleSideload :app:assembleSideloadAndroidTest \
     && java -jar "$BT" build-apks --bundle=app/build/outputs/bundle/sideload/app-sideload.aab --output="$OUT/phone.apks" \
          --connected-device --device-id="$PHONE" --adb="$ADB" --overwrite \
          --ks="$HOME/.android/debug.keystore" --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
     && java -jar "$BT" install-apks --apks="$OUT/phone.apks" --device-id="$PHONE" --adb="$ADB" \
     && "$ADB" install -r -t app/build/outputs/apk/androidTest/sideload/app-sideload-androidTest.apk; then
    "$ADB" shell am instrument -w "$PKG.sideload.test/androidx.test.runner.AndroidJUnitRunner" | tee "$OUT/phone-tests.txt"
    if grep -q '^OK (' "$OUT/phone-tests.txt"; then result phone-tests PASS; else result phone-tests FAIL; fi
  else
    result phone-tests "FAIL (could not build or install the test copy)"
  fi
  # Put the phone back: remove the test copies. The Play copy of SajdaTime is never touched.
  "$ADB" uninstall "$PKG.sideload.test" >/dev/null 2>&1
  "$ADB" uninstall "$PKG.sideload" >/dev/null 2>&1
  unset ANDROID_SERIAL
fi

step "8. Back to where it started"
if [ "$START_BRANCH" != "$BRANCH" ]; then
  git switch "$START_BRANCH" && result restore "PASS (repo back on $START_BRANCH)" || result restore FAIL
else
  result restore "PASS (never switched: $REPO stays on $BRANCH)"
fi

echo; echo "===== SUMMARY ====="; cat "$SUM"
echo "Finished $(date). Claude can read the results now."
touch "$OUT/finished"
