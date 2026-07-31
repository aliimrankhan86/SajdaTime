#!/usr/bin/env bash
#
# Screenshots every watch screen on BOTH supported round sizes, at a normal and a large
# font, and fails if anything ends up behind the bezel.
#
#   ./tools/wear-verify.sh                 # both sizes, font scale 1.0 and 1.3
#   ./tools/wear-verify.sh 1.0             # one font scale
#
# Why this exists
# ---------------
# Wear OS has two round sizes to satisfy: 1.2" / 192dp and 1.39" / 227dp. For a long time
# only the 192dp one was ever run here, because the SDK had only a small-round AVD and
# forcing it larger looked like it would do the job:
#
#     adb shell wm size 454x454        # <- DO NOT. It lies.
#
# It does not work. The rounded-corner overlay is computed once for the panel the device
# was created with, so after the resize the mask sits at the wrong radius and paints
# straight vertical cuts through the middle of the text. A round screen cannot produce a
# straight vertical edge, so the capture is not evidence of anything — and a capture that
# cannot be trusted must not be filed as one. The fix is a second AVD on a real 227dp
# profile, which this script creates if it is missing.
#
# What it checks, and what it cannot
# ----------------------------------
# It checks WO-V16's "no text or controls cut off by screen edges", because that one is
# arithmetic: screencap grabs the framebuffer before the corner overlay, so anything
# outside the inscribed circle is something the wearer will never see. It cannot check
# "no text or controls overlap each other" — the watch face clock is drawn by the system
# on top of the app, both are lit pixels, and no amount of pixel counting tells them
# apart. Look at the captures. That is how the school button was found printed underneath
# the time, and no automated check here would have caught it.

set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
adb="$sdk/platform-tools/adb"
out="$root/build/wear-verify"
scales=("${@:-}")
[ -z "${scales[0]}" ] && scales=(1.0 1.3)

# name:device-profile:edge-in-px. 320dpi both, so px/2 = dp: 384->192dp, 454->227dp.
avds=("sajdawear:wearos_small_round:384" "sajdawear_large:wearos_large_round:454")
image="system-images;android-34;android-wear;arm64-v8a"

mkdir -p "$out"
"$root/gradlew" -p "$root" :wear:assembleDebug -q
apk="$root/wear/build/outputs/apk/debug/wear-debug.apk"

boot() {  # name profile -> echoes the serial
  local name=$1 profile=$2
  if ! "$sdk/cmdline-tools/latest/bin/avdmanager" list avd 2>/dev/null | grep -q "Name: $name$"; then
    echo "creating AVD $name ($profile)" >&2
    echo no | "$sdk/cmdline-tools/latest/bin/avdmanager" create avd \
      -n "$name" -d "$profile" -k "$image" >/dev/null
  fi
  for serial in $("$adb" devices | awk '/emulator/{print $1}'); do
    [ "$("$adb" -s "$serial" emu avd name 2>/dev/null | head -1 | tr -d '\r')" = "$name" ] && {
      echo "$serial"; return; }
  done
  "$sdk/emulator/emulator" -avd "$name" -no-snapshot -no-audio -gpu swiftshader_indirect \
    >/dev/null 2>&1 &
  local serial=""
  while [ -z "$serial" ]; do
    sleep 4
    for s in $("$adb" devices | awk '/emulator/{print $1}'); do
      [ "$("$adb" -s "$s" emu avd name 2>/dev/null | head -1 | tr -d '\r')" = "$name" ] && serial=$s
    done
  done
  while [ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 3
  done
  echo "$serial"
}

for entry in "${avds[@]}"; do
  IFS=: read -r name profile edge <<<"$entry"
  serial="$(boot "$name" "$profile")"
  mid=$((edge / 2))
  echo "== $name ($((edge / 2))dp round) on $serial"
  "$adb" -s "$serial" install -r -t "$apk" >/dev/null
  "$adb" -s "$serial" shell pm grant com.sajdatime.app android.permission.ACCESS_COARSE_LOCATION || true

  for fs in "${scales[@]}"; do
    "$adb" -s "$serial" shell settings put system font_scale "$fs"
    "$adb" -s "$serial" shell am force-stop com.sajdatime.app
    "$adb" -s "$serial" shell am start -n com.sajdatime.app/com.sajdatime.wear.WearMainActivity >/dev/null
    sleep 8
    grab() { sleep 2; "$adb" -s "$serial" exec-out screencap -p > "$out/$name-fs$fs-$1.png"; }
    grab times-top
    # Down to the very bottom. The scroll extreme is the interesting one: that is where
    # the list anchors from below and lifts whatever is second-to-last under the clock.
    for i in 1 2 3 4; do
      "$adb" -s "$serial" shell input swipe $mid $((edge - 70)) $mid 70 400
      grab "times-scroll$i"
    done
    "$adb" -s "$serial" shell am force-stop com.sajdatime.app
    "$adb" -s "$serial" shell am start -n com.sajdatime.app/com.sajdatime.wear.WearMainActivity >/dev/null
    sleep 6
    "$adb" -s "$serial" shell input swipe $((edge - 60)) $mid 60 $mid 300
    grab qibla
  done
  "$adb" -s "$serial" shell settings put system font_scale 1.0
done

echo
"$root/tools/wear-round-check.py" "$out"/*.png
echo
echo "Captures in $out — open them. The bezel check cannot see text drawn over text."
