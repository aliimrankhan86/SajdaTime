#!/bin/bash
# SajdaTime 1.5.0 (Quiet Glass, Arabic, redesigned widgets, built-in adhan, Turkey with Diyanet's minutes):
# push the branch, run every check, build both signed bundles. Written by Claude on 18 Oct 2026 from
# release-1.4.0.sh, which stays beside it as the record of the last release. Safe to run again: each step
# reports PASS, FAIL, CHECK, TAKEN or SKIPPED. It never prints or opens the signing key.
#
# What changed from 1.4.0, and why (docs/release-tools/RELEASE-1.5.0.md, "From the Mac readiness check"):
# - The branch is claude/quiet-glass. On his Mac it is checked out in its own worktree
#   (~/Developer/SajdaTime-settings-preview), and git refuses one branch in two folders, so the script
#   works in whichever folder has the branch and only switches when none does.
# - The watch ships too (1002 / 1.5.0, it shares the Turkey change): its bundle is checked and copied.
# - The size limit is set from the measured 1.5.0 bundle (the two built-in adhans add about 3.5 MB).
# - Arabic is a fifth language, so the language check and the screenshots include it.
# - The on device suite runs through am instrument after grantbind (CLAUDE.md, HANDOVER §15 lesson 134):
#   connectedAndroidTest uninstalls the app when it ends, and WidgetDeviceTest needs the bind grant first.
# - The emulator it starts itself runs -read-only, so the AVD is exactly as it was when the script ends.
#   An emulator that is already running is reused and its SajdaTime apps are replaced; at the end the
#   languages and automatic time are put back and the font size is set to 1.0, but it keeps the London
#   time zone and adb root until it restarts.
# - The 1.4.0 planning copy stash step is gone.
#
# Results land in <the branch's folder>/build/release-1.5.0/ (summary.txt first), and the signed bundles
# in ~/Developer/SajdaTime/SajdaTime-1.5.0/ (gitignored), as the 1.3.2 notes asked. Nothing here submits
# anything to Google Play. The owner presses submit, one screen at a time.

set -o pipefail
MAIN_REPO="$HOME/Developer/SajdaTime"
BRANCH="${SAJDA_BRANCH:-claude/quiet-glass}"
REPO="$(git -C "$MAIN_REPO" worktree list --porcelain 2>/dev/null \
  | awk -v b="refs/heads/$BRANCH" '/^worktree /{w=substr($0,10)} $0=="branch "b{print w; exit}')"
[ -n "$REPO" ] || REPO="$MAIN_REPO"
OUT="$REPO/build/release-1.5.0"
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
  result gate "PASS ($COUNTS; lint: ${LINT%, })"; GATE=pass
else result gate FAIL; fi

step "4. bundletool"
BT="$OUT/bundletool.jar"
[ -f "$BT" ] || cp "$MAIN_REPO/build/release-1.4.0/bundletool.jar" "$BT" 2>/dev/null \
  || curl -fsSL -o "$BT" https://github.com/google/bundletool/releases/download/1.18.3/bundletool-all-1.18.3.jar
if java -jar "$BT" version; then result bundletool PASS; else result bundletool FAIL; fi

step "5. Signed bundles: signature, version, languages, permissions, size"
DEST="$MAIN_REPO/SajdaTime-1.5.0"
mkdir -p "$DEST"
rm -f "$DEST"/*.aab "$DEST/sha256.txt"  # an earlier run's bundles must not get this run's checksums
check_bundle() {  # label, aab, expected versionCode; prints the signer for the caller
  local label="$1" aab="$2" want_vc="$3" verify signer vc vn
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
  cp "$aab" "$DEST/"; cp "$aab" "$OUT/"
  result "$label" "PASS (signed by $signer, versionCode $vc, versionName $vn, $(wc -c < "$aab" | tr -d ' ') bytes, copied to SajdaTime-1.5.0)"
  case "$signer" in *"Android Debug"*) result "$label-signer" "FAIL (debug key, do not upload)";; esac
  [ "$vc" = "$want_vc" ] || result "$label-version" "FAIL (expected versionCode $want_vc)"
  [ "$vn" = "1.5.0" ] || result "$label-version-name" "FAIL (expected versionName 1.5.0)"
}
AAB="app/build/outputs/bundle/release/app-release.aab"
WAAB="wear/build/outputs/bundle/release/wear-release.aab"
if check_bundle bundle "$AAB" 9; then
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
  # Nothing slipped in. No permission was added in 1.5.0: the phone list is the eleven 1.3.2 and 1.4.0 shipped.
  PERMS="$(java -jar "$BT" dump manifest --bundle="$AAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  EXPECTED_PERMS="android.permission.ACCESS_COARSE_LOCATION android.permission.ACCESS_NETWORK_STATE android.permission.ACCESS_NOTIFICATION_POLICY android.permission.FOREGROUND_SERVICE android.permission.INTERNET android.permission.POST_NOTIFICATIONS android.permission.RECEIVE_BOOT_COMPLETED android.permission.SCHEDULE_EXACT_ALARM android.permission.WAKE_LOCK com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION "
  if [ "$PERMS" = "$EXPECTED_PERMS" ]; then result permissions "PASS (the same eleven as 1.4.0)"; else result permissions "FAIL (the permission list changed: $PERMS)"; fi
  # Measured 18 Oct 2026 at 9038d3a: 9,475,100 bytes (1.4.0 was 6,039,999; the two built-in adhans are
  # about 3.5 MB of it). The limit allows 300 KB on top, as 1.4.0's did over 1.3.2.
  SIZE="$(wc -c < "$AAB" | tr -d ' ')"
  if [ "$SIZE" -le 9775100 ]; then result bundle-size "PASS ($SIZE bytes, measured 9475100 on 18 Oct 2026)"
  else result bundle-size "FAIL ($SIZE bytes, more than 300 KB over the 9475100 measured on 18 Oct 2026)"; fi
fi
if check_bundle watch-bundle "$WAAB" 1002; then
  # The watch asks for three permissions, unchanged by 1.5.0 (git diff origin/main on its manifest adds none).
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
  tap_text() {    # visible text to tap, found through the UI dump as the Do Not Disturb check does
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
    "$ADB" pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null 2>&1
    local b
    b="$(grep -o "text=\"$1\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" "$OUT/ui.xml" | head -1 | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]')"
    [ -z "$b" ] && return 1
    # "[127,1385][365,1438]" becomes "127,1385,365,1438" (a separator, or 1385 and 365 glue into one number).
    local c x y
    c="$(echo "$b" | sed 's/\]\[/,/; s/[][]//g' | awk -F, '{print int(($1+$3)/2), int(($2+$4)/2)}')"
    x="${c% *}"; y="${c#* }"
    "$ADB" shell input tap "$x" "$y"; sleep 2
    return 0
  }
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
    # reminder test (it needs the clock inside Ramadan, 6d runs it) and the seed, which is not a check.
    instrument device-suite -e notClass com.sajdatime.app.SuhoorReminderDeviceTest,com.sajdatime.app.ScreenSeedDeviceTest

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
    instrument screen-seed -e class com.sajdatime.app.ScreenSeedDeviceTest
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
