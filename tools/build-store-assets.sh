#!/usr/bin/env bash
#
# Renders the Play Store graphics into docs/store/.
#
#   icon-512.png             512x512, the store listing icon
#   feature-graphic-1024.png 1024x500, the banner at the top of the listing
#   screenshots/*.png        the raw emulator captures, reframed to 9:16
#
# Both are drawn from the same shapes as app/src/main/res/drawable/ic_launcher_*.xml,
# so the store art and the launcher icon cannot drift apart by hand. If you change the
# launcher vector, change the paths below to match and re-run this.
#
# Needs: Google Chrome. Same dependency as tools/build-architecture-pdf.sh.

set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
out="$root/docs/store"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$out"

chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
[ -x "$chrome" ] || { echo "Google Chrome not found at $chrome" >&2; exit 1; }

green="#14624B"
sand="#F0D69A"

# The mihrab niche and the two minarets, copied from ic_launcher_foreground.xml.
mihrab="M54,26C44.9,31.3 39.6,40.9 39.6,52.2V82H48.6V52.2C48.6,45.1 50.5,40.1 54,37C57.5,40.1 59.4,45.1 59.4,52.2V82H68.4V52.2C68.4,40.9 63.1,31.3 54,26Z"
minarets="M30,82H35.4V56.4H30V82ZM72.6,82H78V56.4H72.6V82Z"

shot() { # html  width  height  outfile
  "$chrome" --headless --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --default-background-color=00000000 \
    --screenshot="$4" --window-size="$2,$3" "file://$1" >/dev/null 2>&1
}

# --- Icon ----------------------------------------------------------------------------
# viewBox is the adaptive icon's 72dp safe zone, not the full 108dp canvas, so the store
# icon is framed the way a launcher actually masks it rather than looking shrunken.
cat > "$work/icon.html" <<HTML
<style>html,body{margin:0;padding:0}</style>
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="18 18 72 72">
  <rect x="0" y="0" width="108" height="108" fill="$green"/>
  <path d="$mihrab" fill="$sand"/>
  <path d="$minarets" fill="$sand"/>
</svg>
HTML
shot "$work/icon.html" 512 512 "$out/icon-512.png"

# --- Feature graphic -----------------------------------------------------------------
# Play crops this differently across surfaces and may overlay a play button in the middle,
# so nothing important sits dead centre and there is generous margin all round.
cat > "$work/feature.html" <<HTML
<style>
  html,body{margin:0;padding:0}
  .g{width:1024px;height:500px;background:$green;display:flex;align-items:center;
     gap:56px;padding:0 72px;box-sizing:border-box;
     font-family:-apple-system,"Helvetica Neue",Helvetica,Arial,sans-serif;color:#fff}
  h1{font-size:82px;line-height:1;margin:0;font-weight:700;letter-spacing:-1px}
  p{font-size:31px;line-height:1.35;margin:20px 0 0;color:$sand;font-weight:400}
  .sub{font-size:23px;margin-top:22px;color:rgba(255,255,255,.72)}
</style>
<div class="g">
  <svg width="230" height="230" viewBox="18 18 72 72" style="flex:none">
    <path d="$mihrab" fill="$sand"/>
    <path d="$minarets" fill="$sand"/>
  </svg>
  <div>
    <h1>SajdaTime</h1>
    <p>Prayer times and Qibla,<br>calculated on your phone</p>
    <div class="sub">Free &middot; No ads &middot; No accounts &middot; Works offline</div>
  </div>
</div>
HTML
shot "$work/feature.html" 1024 500 "$out/feature-graphic-1024.png"

# --- Phone screenshots ---------------------------------------------------------------
# Raw captures are 1080x2400, the emulator's native size. Play refuses a screenshot whose
# long side is more than twice its short side, and 2400/1080 is 2.22, so the captures are
# reframed onto a 9:16 canvas in the brand green rather than cropped — nothing on screen
# is lost, and the surround matches the feature graphic.
shots=("$out/screenshots/raw"/*.png)
if [ -e "${shots[0]}" ]; then
  for src in "${shots[@]}"; do
    cat > "$work/shot.html" <<HTML
<style>
  html,body{margin:0;padding:0}
  .f{width:1080px;height:1920px;background:$green;display:flex;
     align-items:center;justify-content:center}
  img{height:1920px;display:block}
</style>
<div class="f"><img src="file://$src"></div>
HTML
    shot "$work/shot.html" 1080 1920 "$out/screenshots/$(basename "$src")"
  done
fi

# --- Wear OS screenshots -------------------------------------------------------------
# Copied through untouched. 384x384 is already exactly what Play wants, so there is no
# reframing to do, and nothing here should invent pixels the device did not produce.
#
# A circular mask was tried and removed. The idea was that adb screencap returns the whole
# square framebuffer including corners a round display never shows, so masking would only
# remove what was already invisible. That is true, and it is also worth nothing here: this
# UI is white on black, so the corners it would blacken are already black. All it bought
# was an assumption about the emulator's geometry, on an image that is otherwise exactly
# what the device produced. Not worth it.
#
# The last row of times being cut by the bottom edge is the list telling the user there is
# more below, which is how a ScalingLazyColumn is meant to look. What was worth fixing was
# the cut landing mid-glyph, and that is fixed by choosing the scroll position when
# capturing, not by post-processing. Note that ScreenScaffold hides the watch face clock
# once the list is scrolled off the top, so capture after the fade has finished or the
# clock is caught half drawn.
#
# Sources live in raw/wear/ rather than raw/ so the phone loop's raw/*.png glob does not
# reframe a watch onto a 1080x1920 phone canvas.
for src in "$out/screenshots/raw/wear"/*.png; do
  [ -e "$src" ] || continue
  cp "$src" "$out/screenshots/$(basename "$src")"
done

echo "Wrote:"
for f in "$out/icon-512.png" "$out/feature-graphic-1024.png" "$out/screenshots"/*.png; do
  printf '  %-9s %s\n' "$(sips -g pixelWidth -g pixelHeight "$f" | awk '/pixel/{printf "%sx", $2}' | sed 's/x$//')" "${f#"$root/"}"
done
