#!/bin/bash
# SajdaTime 1.3.2: push the fix, run every check, build the signed phone bundle.
# Written by Claude on 11 Oct 2026 for Ali. Safe to run again: each step reports PASS, FAIL or SKIPPED.
# It never prints or opens the signing key. It leaves the repo on the branch it found.
# Results land in ~/Developer/SajdaTime/build/release-1.3.2/ (summary.txt first), the bundle on the Desktop.
#
# Kept in the repo as the record of how 1.3.2 was released (docs/HANDOVER.md, section 15, lesson 129).
# The owner ran it from build/release-1.3.2/run.sh. For a later release, copy it and change BRANCH,
# the expected versionCode in step 5, the folder names (OUT and the Desktop folder) and the step 6
# screenshot checks. Step 7 (his phone) runs only with TEST_PHONE=1. The owner keeps the latest signed
# bundle in the repo root (SajdaTime-<version>/, gitignored), not on the Desktop, so point DEST there next time.

set -o pipefail
REPO="$HOME/Developer/SajdaTime"
OUT="$REPO/build/release-1.3.2"
BRANCH="claude/1-3-2-language-dnd"
PKG="com.sajdatime.app"
mkdir -p "$OUT"
rm -f "$OUT/finished" "$OUT"/*.png
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
if [ -n "$(git status --porcelain)" ]; then
  result checkout "FAIL (the repo has uncommitted changes, nothing was touched)"
  touch "$OUT/finished"; exit 1
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
DEST="$HOME/Desktop/SajdaTime-1.3.2"
VERIFY=""; [ -f "$AAB" ] && VERIFY="$("$JAVA_HOME/bin/jarsigner" -verify -verbose -certs "$AAB" 2>&1)"
if echo "$VERIFY" | grep "jar verified" >/dev/null; then
  SIGNER="$(echo "$VERIFY" | grep -o 'CN=[^,]*' | head -1)"
  VC="$(java -jar "$BT" dump manifest --bundle="$AAB" --xpath /manifest/@android:versionCode 2>/dev/null)"
  VN="$(java -jar "$BT" dump manifest --bundle="$AAB" --xpath /manifest/@android:versionName 2>/dev/null)"
  mkdir -p "$DEST"
  cp "$AAB" "$DEST/app-release.aab"
  cp "$AAB" "$OUT/app-release.aab"
  ( cd "$DEST" && shasum -a 256 app-release.aab | tee sha256.txt )
  result bundle "PASS (signed by $SIGNER, versionCode $VC, versionName $VN, copied to Desktop/SajdaTime-1.3.2)"
  case "$SIGNER" in *"Android Debug"*) result bundle-signer "FAIL (debug key, do not upload)";; esac
  [ "$VC" = "7" ] || result bundle-version "FAIL (expected versionCode 7)"
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
else
  if [ "$GATE" = fail ]; then result bundle "FAIL (not built, because the gate above failed)"
  else result bundle "FAIL (no signed bundle: is keystore.properties in the repo root?)"; fi
fi

step "6. Emulator: on-device tests, then look at the screens"
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
  if ./gradlew :app:connectedDebugAndroidTest; then result emulator-tests PASS; else result emulator-tests FAIL; fi
  cp -R app/build/reports/androidTests "$OUT/emulator-test-report" 2>/dev/null
  if ./gradlew :app:installDebug; then
    # The Do Not Disturb access list, with SajdaTime in it.
    "$ADB" shell am start -a android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS >/dev/null; sleep 4
    "$ADB" exec-out screencap -p > "$OUT/emulator-dnd-list.png"
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" pull /sdcard/ui.xml "$OUT/emulator-dnd-list.xml" >/dev/null 2>&1
    if grep -q 'SajdaTime' "$OUT/emulator-dnd-list.xml" 2>/dev/null; then result emulator-dnd-list "PASS (SajdaTime is listed)"; else result emulator-dnd-list "CHECK (see emulator-dnd-list.png)"; fi
    # The app in each language.
    for L in ur tr id en; do
      "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$L" >/dev/null 2>&1; sleep 2
      "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 4
      "$ADB" exec-out screencap -p > "$OUT/emulator-app-$L.png"
    done
    "$ADB" shell cmd locale set-app-locales "$PKG" --locales "" >/dev/null 2>&1
    result emulator-screens "TAKEN (emulator-*.png)"
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
