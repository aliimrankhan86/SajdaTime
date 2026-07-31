#!/usr/bin/env bash
#
# Renders the Play Store graphics into docs/store/upload/, laid out one folder per Play
# Console field so there is no guessing about which file belongs in which upload box:
#
#   upload/app-icon/icon-512.png                 512x512   -> "App icon"
#   upload/feature-graphic/feature-graphic.png   1024x500  -> "Feature graphic"
#   upload/phone/*.png                           1080x1920 -> "Phone screenshots"
#   upload/wear-os/*.png                         454x454   -> "Wear OS screenshots"
#   upload/tablet-7in, tablet-10in, chromebook   deliberately empty, see their READMEs
#
# --- Colours come from the design system, not from here ---------------------------------
#
# $green is LightPrimary and the hero stops are LightHeroStart/Middle/End, all from
# ui/theme/Color.kt. They are duplicated here because a headless Chrome cannot read Kotlin,
# NOT because the store art gets its own palette. If Color.kt changes, change these.
#
# This bit has already gone wrong once: the icon sat on #14624B, the primary from before
# the light palette was revised for contrast, so the launcher icon and every store graphic
# were a revision behind the app. Color.kt records the same drift on DarkInversePrimary.
#
# The mihrab and minaret paths are copied from app/src/main/res/drawable/ic_launcher_*.xml
# so the store art and the launcher icon cannot drift apart by hand either. If you change
# the launcher vector, change the paths below to match and re-run this.
#
# Needs: Google Chrome. Same dependency as tools/build-architecture-pdf.sh.

set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
out="$root/docs/store/upload"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

mkdir -p "$out"/{app-icon,feature-graphic,phone,wear-os,tablet-7in,tablet-10in,chromebook}

chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
[ -x "$chrome" ] || { echo "Google Chrome not found at $chrome" >&2; exit 1; }

# LightPrimary. See the note above before changing.
green="#0E6B4F"
# The mark. #F0D69A on the green above is 4.57:1 — still AA for text. The design's own
# sand (#E4D2B4, the hero gradient's last stop) is 4.38:1 there, which passes only as a
# graphic, and there was no reason to spend that margin on the app's primary mark.
sand="#F0D69A"
# LightHeroStart / Middle / End — the gradient the app actually draws on its next-prayer
# card. Using it here means the store banner is the app's own signature surface rather
# than a green slab invented for the listing.
heroStart="#D6E8DC"
heroMid="#DDE2CE"
heroEnd="#E4D2B4"
# LightOnHero / OnHeroLabel / OnHeroSecondary. Every one of these inks is asserted against
# every stop above in ColorContrastTest; the worst pair here is 4.99:1.
onHero="#0A2419"
onHeroLabel="#2C4A3C"
onHeroSecondary="#3E5C4C"

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
shot "$work/icon.html" 512 512 "$out/app-icon/icon-512.png"

# --- Feature graphic -----------------------------------------------------------------
# The app's own hero gradient, not a flat slab. Play crops this differently across
# surfaces and may overlay a play button in the middle, so nothing important sits dead
# centre and there is generous margin all round.
#
# The mark sits on a solid green tile rather than directly on the gradient: #0E6B4F on
# #E4D2B4 is 4.38:1, which is fine for a graphic but thin, and the tile makes the mark
# read the same way it does on a launcher.
cat > "$work/feature.html" <<HTML
<style>
  html,body{margin:0;padding:0}
  .g{width:1024px;height:500px;display:flex;align-items:center;gap:56px;
     padding:0 72px;box-sizing:border-box;
     background:linear-gradient(160deg,$heroStart 0%,$heroMid 52%,$heroEnd 100%);
     font-family:-apple-system,"Helvetica Neue",Helvetica,Arial,sans-serif;color:$onHero}
  .mark{flex:none;width:230px;height:230px;border-radius:52px;background:$green;
        display:flex;align-items:center;justify-content:center}
  h1{font-size:82px;line-height:1;margin:0;font-weight:700;letter-spacing:-1px}
  p{font-size:31px;line-height:1.35;margin:20px 0 0;color:$onHeroSecondary;font-weight:400}
  .sub{font-size:23px;margin-top:22px;color:$onHeroLabel}
</style>
<div class="g">
  <div class="mark">
    <svg width="168" height="168" viewBox="18 18 72 72">
      <path d="$mihrab" fill="$sand"/>
      <path d="$minarets" fill="$sand"/>
    </svg>
  </div>
  <div>
    <h1>SajdaTime</h1>
    <p>Prayer times and Qibla,<br>calculated on your phone</p>
    <!--
      Do not put "Free" back in this line. It used to read "Free / No ads / No accounts /
      Works offline", and Play flags price and promotion words as a reason not to feature
      an app. The Console says so out loud for the short description field; it says nothing
      about this banner, because it validates form fields and cannot read a PNG. The word
      was removed from both in the same change, and the silent half is the one that would
      have been left behind. Play prints "Free" next to the Install button anyway.

      "Works offline" also went: the line above it already says "calculated on your phone",
      which is the same claim in plainer words. The slot went to the thing nothing else in
      the listing was saying — that both traditions are supported properly.
    -->
    <div class="sub">Sunni &amp; Shia &middot; No ads &middot; No accounts &middot; No tracking</div>
  </div>
</div>
HTML
shot "$work/feature.html" 1024 500 "$out/feature-graphic/feature-graphic-1024.png"

# --- Phone screenshots ---------------------------------------------------------------
# Copied through untouched, because they are already exactly right.
#
# Play accepts phone screenshots at 16:9 or 9:16 and nothing between — the Console's own
# wording, not a guess. The captures used to come off a Pixel 7 AVD at 1080x2400, which is
# neither, so they were scaled down onto a 9:16 canvas and pillarboxed in brand green.
# That complied, but it threw away a fifth of the frame and put a green surround around a
# UI that never shows one.
#
# They are now captured natively at 1080x1920 on the sajdastore AVD (Pixel 2 profile: 9:16
# at 420dpi, and no display cutout — a Pixel 7 profile forces a hole-punch whose 136px
# inset does not fit a 1920-tall screen and clips the status bar). Nothing is reframed,
# nothing is scaled, nothing is invented. See docs/store/upload/README.md for the capture
# recipe.
for src in "$root/docs/store/screenshots/raw/phone"/*.png; do
  [ -e "$src" ] || continue
  cp "$src" "$out/phone/$(basename "$src")"
done

# --- Wear OS screenshots -------------------------------------------------------------
# Copied through untouched. Play wants 1:1 between 384px and 3840px, and the captures are
# already 454x454 off the large round AVD, so there is no reframing to do and nothing here
# should invent pixels the device did not produce.
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
for src in "$root/docs/store/screenshots/raw/wear"/*.png; do
  [ -e "$src" ] || continue
  cp "$src" "$out/wear-os/$(basename "$src")"
done

echo "Wrote:"
while IFS= read -r f; do
  printf '  %-10s %s\n' \
    "$(sips -g pixelWidth -g pixelHeight "$f" | awk '/pixel/{printf "%sx", $2}' | sed 's/x$//')" \
    "${f#"$root/"}"
done < <(find "$out" -name '*.png' | sort)
