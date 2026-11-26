#!/bin/bash
# SajdaTime 1.8.0 (adhkar reminder, Friday reminder with Surah Al-Kahf, share today's times as a picture): run the full gate
# on main after Ali's Merge of PR #24, build the bundles, check the signed phone bundle. Copied from release-1.8.0.sh on
# 26 Nov 2026 (which stays beside it as the record of the last release). It never prints or opens the signing key: Gradle
# signs from the sajdatime.* values in ~/.gradle/gradle.properties.
#
# What changed from 1.7.1, and why:
# - Phone 13 / 1.8.0. The watch is NOT released: :wear did not change, so its bundle is built as a gate check only.
# - No emulator step. ponytail: Ali, 26 Nov 2026, "test only what is new": QuietRemindersDeviceTest and the touched device
#   tests ran green on emulator-5554 on claude/1-8-0, and main's tree is that branch's tree (git diff empty). Copy step 6
#   back from release-1.8.0.sh when a release changes code the branch's device run did not cover.
# - Bundle size limit: 300 KB over 1.7.1's 8,921,191 bytes. No new permission (the brief's hard rule).
#
# Results land in build/release-1.8.0/ (summary.txt first), the signed bundle in ~/Developer/SajdaTime/SajdaTime-1.8.0/
# (gitignored). Nothing here submits anything to Google Play.

set -o pipefail
MAIN_REPO="$HOME/Developer/SajdaTime"
BRANCH="${SAJDA_BRANCH:-main}"
REPO="$(git -C "$MAIN_REPO" worktree list --porcelain 2>/dev/null \
  | awk -v b="refs/heads/$BRANCH" '/^worktree /{w=substr($0,10)} $0=="branch "b{print w; exit}')"
[ -n "$REPO" ] || REPO="$MAIN_REPO"
OUT="$REPO/build/release-1.8.0"
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
if ./gradlew --no-build-cache clean test :app:bundleRelease :wear:bundleRelease && ./gradlew lint; then
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
  # Gate counts at 25 Oct 2026 (core 238 tests, 1 of them skipped; app 335 since DesignTokensTest added 12; wear 27). Update with the code.
  # 12 Nov 2026: app 354, core 266 (tasbeeh, Timetable list, Qibla geometry added), wear 27.
  # 24 Nov 2026 (1.7.1): app 367 (WidgetStateTest gained the Today rows test), core 276, wear 27.
  # 26 Nov 2026 (1.8.0): app 385 (adhkar, Friday and picture tests), core 276, wear 27.
  EXPECTED_COUNTS="app 385 tests 0 failed, core 276 tests 0 failed, wear 27 tests 0 failed"
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
DEST="$MAIN_REPO/SajdaTime-1.8.0"
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
  result "$label" "PASS (signed by $signer, versionCode $vc, versionName $vn, $(wc -c < "$aab" | tr -d ' ') bytes, $([ "$copy" = yes ] && echo "copied to SajdaTime-1.8.0" || echo "gate check only, not for upload"))"
  case "$signer" in *"Android Debug"*) result "$label-signer" "FAIL (debug key, do not upload)";; esac
  [ "$vc" = "$want_vc" ] || result "$label-version" "FAIL (expected versionCode $want_vc)"
  [ "$vn" = "$want_vn" ] || result "$label-version-name" "FAIL (expected versionName $want_vn)"
}
AAB="app/build/outputs/bundle/release/app-release.aab"
WAAB="wear/build/outputs/bundle/release/wear-release.aab"
if check_bundle bundle "$AAB" 13 1.8.0 yes; then
  # What Google Play would install on an English-only Galaxy S23 Ultra, from this exact file.
  echo '{"supportedAbis":["arm64-v8a"],"supportedLocales":["en-GB"],"screenDensity":480,"sdkVersion":35}' > "$OUT/s23-en.json"
  rm -rf "$OUT/s23-en" && mkdir -p "$OUT/s23-en"
  # Bundletool uses its default debug signer for this local split inspection.
  if java -jar "$BT" build-apks --bundle="$AAB" --output="$OUT/s23-en.apks" --device-spec="$OUT/s23-en.json" --overwrite \
     && unzip -q -o "$OUT/s23-en.apks" -d "$OUT/s23-en"; then
    LANGS="$(for a in "$OUT"/s23-en/splits/*.apk; do "$AAPT2" dump resources "$a" 2>/dev/null; done \
      | awk '/string\/app_language_tag$/{f=1;next} f&&/resource 0x/{f=0} f' | grep -o '([a-z]*)' | sort -u | tr -d '\n')"
    case "$LANGS" in *"(ar)"*"(in)"*"(tr)"*"(ur)"*) result play-languages "PASS (English-only phone gets $LANGS)";;
                     *) result play-languages "FAIL (English-only phone gets only $LANGS)";; esac
  else
    result play-languages "FAIL (bundletool could not build the APK set)"
  fi
  # Nothing slipped in. The twelve of 1.7.0 (it added VIBRATE); 1.7.1 and 1.8.0 add none.
  PERMS="$(java -jar "$BT" dump manifest --bundle="$AAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  EXPECTED_PERMS="android.permission.ACCESS_COARSE_LOCATION android.permission.ACCESS_NETWORK_STATE android.permission.ACCESS_NOTIFICATION_POLICY android.permission.FOREGROUND_SERVICE android.permission.INTERNET android.permission.POST_NOTIFICATIONS android.permission.RECEIVE_BOOT_COMPLETED android.permission.SCHEDULE_EXACT_ALARM android.permission.VIBRATE android.permission.WAKE_LOCK com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION "
  if [ "$PERMS" = "$EXPECTED_PERMS" ]; then result permissions "PASS (the eleven of 1.5.0 plus VIBRATE)"; else result permissions "FAIL (the permission list changed: $PERMS)"; fi
  # 1.7.1 as uploaded: 8,921,191 bytes (HANDOVER §11, 24 Nov 2026). The limit allows 300 KB on top, as each release since 1.5.0 has.
  SIZE="$(wc -c < "$AAB" | tr -d ' ')"
  if [ "$SIZE" -le 9221191 ]; then result bundle-size "PASS ($SIZE bytes, 1.7.1 was 8921191)"
  else result bundle-size "FAIL ($SIZE bytes, more than 300 KB over the 8921191 of 1.7.1)"; fi
fi
if check_bundle watch-bundle "$WAAB" 1003 1.7.0 no; then
  # The watch asks for three permissions, unchanged by 1.7.0 (the watch change is the highlighted row, no new permission).
  WPERMS="$(java -jar "$BT" dump manifest --bundle="$WAAB" 2>/dev/null | grep -o 'uses-permission[^>]*name="[^"]*"' | sed 's/.*name="//;s/"//' | sort | tr '\n' ' ')"
  if [ "$WPERMS" = "android.permission.ACCESS_COARSE_LOCATION android.permission.WAKE_LOCK com.sajdatime.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION " ]
  then result watch-permissions "PASS (the same three)"; else result watch-permissions "FAIL (the permission list changed: $WPERMS)"; fi
fi
( cd "$DEST" && shasum -a 256 *.aab | tee sha256.txt )

step "6. Back to where it started"
if [ "$START_BRANCH" != "$BRANCH" ]; then
  git switch "$START_BRANCH" && result restore "PASS (repo back on $START_BRANCH)" || result restore FAIL
else
  result restore "PASS (never switched: $REPO stays on $BRANCH)"
fi

echo; echo "===== SUMMARY ====="; cat "$SUM"
echo "Finished $(date). Claude can read the results now."
touch "$OUT/finished"
