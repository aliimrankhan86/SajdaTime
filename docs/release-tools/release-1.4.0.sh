#!/bin/bash
# SajdaTime 1.4.0 (Ramadan pack and two home screen widgets): push the branch, run every check, build the
# signed phone bundle. Written by Claude on 16 Oct 2026 for Ali, from release-1.3.2.sh, which is kept
# beside it as the record of how the last release was done. Safe to run again: each step reports PASS,
# FAIL, CHECK or SKIPPED. It never prints or opens the signing key and it leaves the repo on the branch
# it found. Results land in ~/Developer/SajdaTime/build/release-1.4.0/ (summary.txt first), and the
# signed bundle in ~/Developer/SajdaTime/SajdaTime-1.4.0/ (gitignored), as the 1.3.2 notes asked.
#
# What is new against 1.3.2 is step 6. It runs the on device tests, then sets the emulator clock into
# Ramadan 1448 and runs the Ramadan checks there (the suhoor reminder in `dumpsys alarm`, both widgets
# bound for real and drawn, the PDF in every range), takes the screenshots in four languages, in the
# eve of Ramadan, on the last day, and on both Eid days, and puts the clock back. It needs the emulator
# the script starts itself (an Android 13 or newer phone image with root, which the Google APIs images
# are) because it changes the clock, and the clock goes back when that emulator is killed. If it finds
# an emulator already running it reuses it and turns automatic time back on at the end.
# Step 7 (his phone) runs only with TEST_PHONE=1 and, as in 1.3.2, is off by default.
#
# Nothing here submits anything to Google Play. The owner presses submit, one screen at a time.

set -o pipefail
REPO="$HOME/Developer/SajdaTime"
OUT="$REPO/build/release-1.4.0"
# The branch to push, build and test. Defaults to the 1.4.0 branch exactly as it was tested. The security and
# visibility branch (HANDOVER 5.21, 5.22) is cut from the head of this one, so it contains all of it plus the
# corrected privacy wording; to release that, run with SAJDA_BRANCH=claude/security-and-visibility.
BRANCH="${SAJDA_BRANCH:-claude/1-4-0-ramadan-widgets}"
PKG="com.sajdatime.app"
mkdir -p "$OUT"
rm -f "$OUT/finished" "$OUT"/*.png
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
echo "Started $(date). Repo on $START_BRANCH. JDK $JAVA_HOME. SDK $SDK."

step "1. Push the fix to GitHub"
if git push -u origin "$BRANCH"; then result push PASS; else result push FAIL; fi

step "2. Switch to the fix"
# One known leftover is allowed: the planning session left docs/HANDOVER.md and docs/plans/ uncommitted
# on main. The branch already holds them byte for byte (commit 3140b96). If every changed file is one of
# those and still identical to that commit, they are put in a labelled git stash (nothing is lost) so the
# switch can happen. Anything else, or any file that differs, still stops the script untouched.
PLAN_COMMIT="3140b9606529573eed9ad0e18525e6e166c9658c"
DIRTY="$(git status --porcelain --untracked-files=all | cut -c4-)"
if [ -n "$DIRTY" ]; then
  SAFE=1
  git cat-file -e "$PLAN_COMMIT^{commit}" 2>/dev/null || SAFE=0
  while IFS= read -r P; do
    [ -z "$P" ] && continue
    case "$P" in docs/HANDOVER.md|docs/plans/*) ;; *) SAFE=0 ;; esac
    if [ "$SAFE" = 1 ] && ! git show "$PLAN_COMMIT:$P" 2>/dev/null | cmp -s - "$P"; then SAFE=0; fi
  done <<< "$DIRTY"
  if [ "$SAFE" = 1 ] && git stash push --include-untracked -m "planning copies, identical to $PLAN_COMMIT on the 1.4.0 branch" >/dev/null; then
    result stash "PASS (planning files stashed, the branch already has them: git stash list)"
  else
    result checkout "FAIL (the repo has uncommitted changes, nothing was touched)"
    touch "$OUT/finished"; exit 1
  fi
fi
if git switch "$BRANCH"; then result checkout "PASS ($(git rev-parse --short HEAD))"; else result checkout FAIL; touch "$OUT/finished"; exit 1; fi

step "3. Full release gate: clean, unit tests, lint, both bundles"
GATE=fail
if ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease; then result gate PASS; GATE=pass; else result gate FAIL; fi

step "4. bundletool"
BT="$OUT/bundletool.jar"
[ -f "$BT" ] || curl -fsSL -o "$BT" https://github.com/google/bundletool/releases/download/1.18.3/bundletool-all-1.18.3.jar
if java -jar "$BT" version; then result bundletool PASS; else result bundletool FAIL; fi

step "5. Signed phone bundle: signature, version, every language inside"
AAB="app/build/outputs/bundle/release/app-release.aab"
DEST="$REPO/SajdaTime-1.4.0"
VERIFY=""; [ -f "$AAB" ] && VERIFY="$("$JAVA_HOME/bin/jarsigner" -verify -verbose -certs "$AAB" 2>&1)"
if echo "$VERIFY" | grep "jar verified" >/dev/null; then
  SIGNER="$(echo "$VERIFY" | grep -o 'CN=[^,]*' | head -1)"
  VC="$(java -jar "$BT" dump manifest --bundle="$AAB" --xpath /manifest/@android:versionCode 2>/dev/null)"
  VN="$(java -jar "$BT" dump manifest --bundle="$AAB" --xpath /manifest/@android:versionName 2>/dev/null)"
  mkdir -p "$DEST"
  cp "$AAB" "$DEST/app-release.aab"
  cp "$AAB" "$OUT/app-release.aab"
  ( cd "$DEST" && shasum -a 256 app-release.aab | tee sha256.txt )
  result bundle "PASS (signed by $SIGNER, versionCode $VC, versionName $VN, copied to SajdaTime-1.4.0 in the repo folder)"
  case "$SIGNER" in *"Android Debug"*) result bundle-signer "FAIL (debug key, do not upload)";; esac
  [ "$VC" = "8" ] || result bundle-version "FAIL (expected versionCode 8)"
  [ "$VN" = "1.4.0" ] || result bundle-version-name "FAIL (expected versionName 1.4.0)"
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
    case "$LANGS" in *"(in)"*"(tr)"*"(ur)"*) result play-languages "PASS (English-only phone gets $LANGS)";;
                     *) result play-languages "FAIL (English-only phone gets only $LANGS)";; esac
  else
    result play-languages "FAIL (bundletool could not build the APK set)"
  fi
  # Nothing slipped in. The permission list is the one 1.3.2 shipped (eleven, six of them ours), and
  # the bundle must stay within 300 KB of 1.3.2's 5,779,975 bytes. Of the growth, about 146 KB is the R8
  # mapping file that Play keeps for crash reports and never sends to a phone.
  PERMS="$(java -jar "$BT" dump manifest --bundle="$AAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  EXPECTED_PERMS="android.permission.ACCESS_COARSE_LOCATION android.permission.ACCESS_NETWORK_STATE android.permission.ACCESS_NOTIFICATION_POLICY android.permission.FOREGROUND_SERVICE android.permission.INTERNET android.permission.POST_NOTIFICATIONS android.permission.RECEIVE_BOOT_COMPLETED android.permission.SCHEDULE_EXACT_ALARM android.permission.WAKE_LOCK com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION "
  if [ "$PERMS" = "$EXPECTED_PERMS" ]; then result permissions "PASS (the same eleven as 1.3.2)"; else result permissions "FAIL (the permission list changed: $PERMS)"; fi
  SIZE="$(wc -c < "$AAB" | tr -d ' ')"
  if [ "$SIZE" -le 6079975 ]; then result bundle-size "PASS ($SIZE bytes, 1.3.2 was 5779975)"; else result bundle-size "FAIL ($SIZE bytes, more than 300 KB over 1.3.2)"; fi
  git fetch -q origin main 2>/dev/null
  if [ -z "$(git diff origin/main --stat -- wear/)" ]; then result watch-untouched "PASS (nothing under wear/ differs from main)"; else result watch-untouched "FAIL (wear/ differs from main)"; fi
else
  if [ "$GATE" = fail ]; then result bundle "FAIL (not built, because the gate above failed)"
  else result bundle "FAIL (no signed bundle: is keystore.properties in the repo root, or are the sajdatime.* values in ~/.gradle/gradle.properties?)"; fi
fi

step "6. Emulator: on-device tests, the Ramadan clock stages, then look at the screens"
EMU_SERIAL="$("$ADB" devices | awk '/^emulator-/ && $2=="device" {print $1; exit}')"
STARTED_EMU=""
if [ -z "$EMU_SERIAL" ] && [ -x "$EMU" ]; then
  AVD=""
  for a in $("$EMU" -list-avds); do
    CFG="$HOME/.android/avd/$a.avd/config.ini"
    if grep -q 'image.sysdir.*android-3[3-9]' "$CFG" 2>/dev/null && ! grep -qi 'wear\|tv' "$CFG"; then AVD="$a"; break; fi
  done
  if [ -n "$AVD" ]; then
    echo "Starting emulator $AVD"
    "$EMU" -avd "$AVD" -no-window -no-audio -no-boot-anim -no-snapshot-save >"$OUT/emulator.log" 2>&1 &
    STARTED_EMU=1
    for i in $(seq 1 120); do
      EMU_SERIAL="$("$ADB" devices | awk '/^emulator-/ && $2=="device" {print $1; exit}')"
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

  # Helpers. The clock is set in the emulator's own time zone, which the script first tries to make
  # London so the screenshots show the times a person in London would see.
  set_clock() {   # MMDDhhmmYYYY, for example 022210002027 for 22 Feb 2027 10:00
    "$ADB" shell settings put global auto_time 0 >/dev/null 2>&1
    "$ADB" shell settings put global auto_time_zone 0 >/dev/null 2>&1
    "$ADB" shell "date $1.00" >/dev/null 2>&1
    "$ADB" shell am broadcast -a android.intent.action.TIME_SET >/dev/null 2>&1
    sleep 2
  }
  instrument() {  # label, class, extra -e arguments
    local label="$1" class="$2"; shift 2
    "$ADB" shell am instrument -w -e class "$class" "$@" "$RUNNER" > "$OUT/test-$label.txt" 2>&1
    if grep -q '^OK (' "$OUT/test-$label.txt"; then result "$label" PASS
    else result "$label" "FAIL (see test-$label.txt)"; fi
  }
  pull_renders() { # stage
    rm -rf "$OUT/renders-$1"; mkdir -p "$OUT/renders-$1"
    "$ADB" pull /sdcard/Android/data/$PKG/files/device-renders/. "$OUT/renders-$1/" >/dev/null 2>&1 || true
    "$ADB" shell rm -rf /sdcard/Android/data/$PKG/files/device-renders >/dev/null 2>&1
  }
  shot() {        # file name without extension
    "$ADB" exec-out screencap -p > "$OUT/$1.png"
  }
  tap_text() {    # visible text to tap, found through the UI dump as the Do Not Disturb check does
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
    "$ADB" pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null 2>&1
    local b
    b="$(grep -o "text=\"$1\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" "$OUT/ui.xml" | head -1 | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]')"
    [ -z "$b" ] && return 1
    local x y
    x="$(echo "$b" | tr -d '[]' | awk -F'[, ]' '{print int(($1+$3)/2)}')"; y="$(echo "$b" | tr -d '[]' | awk -F'[, ]' '{print int(($2+$4)/2)}')"
    "$ADB" shell input tap "$x" "$y"; sleep 2
    return 0
  }
  open_app() {
    "$ADB" shell am force-stop "$PKG"; sleep 1
    "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 5
  }

  # --- 6a. The normal on device run, at the emulator's own clock. WidgetDeviceTest, the reminder test and
  # the seed test are left out here: the first needs the bind permission granted after install, the
  # second needs the clock inside Ramadan, and the third is not a check. They run below.
  if ./gradlew :app:connectedDebugAndroidTest \
       -Pandroid.testInstrumentationRunnerArguments.notClass=com.sajdatime.app.WidgetDeviceTest,com.sajdatime.app.SuhoorReminderDeviceTest,com.sajdatime.app.ScreenSeedDeviceTest; then
    result emulator-tests PASS
  else
    result emulator-tests FAIL
  fi
  cp -R app/build/reports/androidTests "$OUT/emulator-test-report" 2>/dev/null

  # --- 6b. Install the app and the test copy so they stay installed, then give the shell the right to bind widgets.
  if ./gradlew :app:installDebug :app:installDebugAndroidTest; then
    "$ADB" root >/dev/null 2>&1; sleep 3
    "$ADB" shell appwidget grantbind --package "$PKG" --user 0 >/dev/null 2>&1 \
      && result widget-bind-grant PASS || result widget-bind-grant "CHECK (the grant command failed; WidgetDeviceTest will say so)"
    "$ADB" shell service call alarm 3 s16 Europe/London >/dev/null 2>&1
    echo "Emulator time zone: $("$ADB" shell getprop persist.sys.timezone | tr -d '\r')"

    # The Do Not Disturb access list, with SajdaTime in it (as in 1.3.2).
    "$ADB" shell am start -a android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS >/dev/null; sleep 4
    shot emulator-dnd-list
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" pull /sdcard/ui.xml "$OUT/emulator-dnd-list.xml" >/dev/null 2>&1
    if grep -q 'SajdaTime' "$OUT/emulator-dnd-list.xml" 2>/dev/null; then result emulator-dnd-list "PASS (SajdaTime is listed)"; else result emulator-dnd-list "CHECK (see emulator-dnd-list.png)"; fi

    # --- 6c. The app in each language at the emulator's own clock, as in 1.3.2.
    for L in ur tr id en; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 4
      shot "emulator-app-$L"
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "" >/dev/null 2>&1

    # --- 6d. Day 14 of Ramadan, 23:00 on 21 February 2027: the suhoor reminder in dumpsys alarm.
    set_clock 022123002027
    instrument suhoor-reminder-alarms com.sajdatime.app.SuhoorReminderDeviceTest -e requireRamadanClock true

    # --- 6e. Day 15, 10:00 on 22 February 2027: the Ramadan checks, both widgets bound for real, the PDFs.
    set_clock 022210002027
    instrument ramadan-calendar com.sajdatime.app.RamadanDeviceTest
    instrument widgets-ramadan com.sajdatime.app.WidgetDeviceTest -e requireRamadanClock true
    pull_renders widgets-ramadan
    "$ADB" shell settings put system font_scale 2.0 >/dev/null 2>&1
    instrument widgets-font-2 com.sajdatime.app.WidgetDeviceTest
    pull_renders widgets-font-2
    "$ADB" shell settings put system font_scale 1.0 >/dev/null 2>&1
    instrument ramadan-pdf com.sajdatime.app.RamadanPdfDeviceTest
    pull_renders pdf

    # --- 6f. The screens with the clock in Ramadan, in each language. Seed first so the first run screens are skipped.
    instrument screen-seed com.sajdatime.app.ScreenSeedDeviceTest
    for L in en ur tr id; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      open_app
      shot "ramadan-times-$L"
      "$ADB" shell input swipe 540 1800 540 700 400; sleep 1
      shot "ramadan-times-$L-scrolled"
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "en" >/dev/null 2>&1; sleep 2
    open_app
    if tap_text "Full timetable"; then shot ramadan-timetable-en; result ramadan-timetable "TAKEN (ramadan-timetable-en.png)"
    else "$ADB" shell input swipe 540 1800 540 700 400; sleep 1
         if tap_text "Full timetable"; then shot ramadan-timetable-en; result ramadan-timetable "TAKEN (ramadan-timetable-en.png)"
         else result ramadan-timetable "CHECK (could not find the Full timetable button, see ramadan-times-en.png)"; fi; fi

    # --- 6g. The other days that change what the app says. English only.
    for STAGE in "eve-of-ramadan:020612002027" "last-day-evening:030820002027" "eid-al-fitr:030910002027" "eid-al-adha:051610002027"; do
      NAME="${STAGE%%:*}"; WHEN="${STAGE##*:}"
      set_clock "$WHEN"; open_app; shot "stage-$NAME"
    done
    # Back to a day that is not Ramadan, to see that the card is gone and nothing is left behind.
    set_clock 062110002027; open_app; shot stage-after-ramadan

    # --- 6h. The app mirrored, as a layout check only (CLAUDE.md: judge the mirroring, never the prose). The rtl
    # build is a separate install with its own empty settings, so it shows the first run screen, not the Times
    # screen: it proves the first screens mirror, and the Times screen with the card is judged from the
    # Urdu screenshots above, which are genuinely right to left.
    set_clock 022210002027
    if ./gradlew installRtl >/dev/null 2>&1; then
      "$ADB" shell am start -W -n "$PKG.rtl/com.sajdatime.app.MainActivity" >/dev/null 2>&1; sleep 5
      shot rtl-first-screen
      "$ADB" uninstall "$PKG.rtl" >/dev/null 2>&1
      result rtl-layout "TAKEN (rtl-first-screen.png, judge the mirroring only)"
    else
      result rtl-layout "SKIPPED (installRtl did not build)"
    fi

    # --- 6i. Put the emulator back.
    "$ADB" shell settings put system font_scale 1.0 >/dev/null 2>&1
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "" >/dev/null 2>&1
    "$ADB" shell settings put global auto_time 1 >/dev/null 2>&1
    "$ADB" shell settings put global auto_time_zone 1 >/dev/null 2>&1
    result emulator-screens "TAKEN (emulator-*, ramadan-*, stage-*, rtl-* and renders-* in $OUT)"
  else
    result emulator-screens "FAIL (could not install the debug build)"
  fi
  [ -n "$STARTED_EMU" ] && "$ADB" emu kill >/dev/null 2>&1
  unset ANDROID_SERIAL
else
  result emulator-tests "SKIPPED (no Android 13+ phone emulator found or it did not boot)"
fi

step "7. Samsung phone (off by default)"
# Ali decided on 11 Oct 2026 not to test on his phone. Set TEST_PHONE=1 to run this step anyway.
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
    "$ADB" shell am start -a android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS >/dev/null; sleep 4
    "$ADB" exec-out screencap -p > "$OUT/phone-dnd-list.png"
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" pull /sdcard/ui.xml "$OUT/phone-dnd-list.xml" >/dev/null 2>&1
    "$ADB" shell rm -f /sdcard/ui.xml
    if grep -q 'SajdaTime' "$OUT/phone-dnd-list.xml" 2>/dev/null; then result phone-dnd-list "PASS (SajdaTime is listed)"; else result phone-dnd-list "CHECK (see phone-dnd-list.png)"; fi
    "$ADB" shell input keyevent KEYCODE_HOME
  else
    result phone-tests "FAIL (could not build or install the test copy)"
  fi
  # Put the phone back: remove the test copies. The Play copy of SajdaTime is never touched.
  "$ADB" uninstall "$PKG.sideload.test" >/dev/null 2>&1
  "$ADB" uninstall "$PKG.sideload" >/dev/null 2>&1
  unset ANDROID_SERIAL
fi

step "8. Back to where it started"
git switch "$START_BRANCH" && result restore "PASS (repo back on $START_BRANCH)" || result restore FAIL

echo; echo "===== SUMMARY ====="; cat "$SUM"
echo "Finished $(date). Claude can read the results now."
touch "$OUT/finished"
