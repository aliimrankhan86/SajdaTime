#!/usr/bin/env bash
#
# Everything mechanical that has to happen before the Wear OS release, in one command.
#
#   ./tools/ship-wear.sh
#
# Why this exists
# ---------------
# The Wear work splits cleanly into two halves. One half is mechanical and repeatable: push,
# clean, test, lint, build both bundles, check they are signed. The other half needs the
# owner's own Google account and his consent, and cannot be automated by anyone, including
# an assistant with a shell on this machine. This script does the whole of the first half
# and then prints what is left, so the only thing outstanding is the part that genuinely
# needs him.
#
# It is deliberately noisy about what it did NOT do. A script that prints "done" when it
# only did the easy half is how a release ships without its verification.
#
# Updated 2 Sept 2026. The first Wear release was submitted on 1 Sept and the one-time
# Console setup does not repeat, so step 4 no longer walks through it. It also no longer
# asks anyone to go and test a watch: that check is closed, and the owner has asked that it
# not be raised again. See docs/HANDOVER.md §11 STATE OF PLAY.

set -euo pipefail
cd "$(dirname "$0")/.."

bold() { printf '\033[1m%s\033[0m\n' "$1"; }
ok()   { printf '  \033[32m✓\033[0m %s\n' "$1"; }
warn() { printf '  \033[33m!\033[0m %s\n' "$1"; }

bold "1/4  Pushing the documentation commits"
if git diff --quiet && git diff --cached --quiet; then
  ok "working tree clean"
else
  warn "you have uncommitted changes; they will NOT be pushed"
fi
if [ -n "$(git log '@{u}..HEAD' --oneline 2>/dev/null)" ]; then
  git push
  ok "pushed"
else
  ok "nothing to push, already up to date"
fi

bold "2/4  Building fresh from main"
# Never upload a saved artifact. Several .aab files exist labelled versionCode 3 and they
# are not the same app; Play ships whichever file it is handed. See app/build.gradle.kts.
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
ok "tests, lint and both bundles green"

bold "3/4  Checking the bundles are actually signed"
for aab in app/build/outputs/bundle/release/app-release.aab \
           wear/build/outputs/bundle/release/wear-release.aab; do
  if unzip -l "$aab" | grep -q 'META-INF/SAJDATIM.RSA'; then
    ok "$(basename "$aab") signed, $(du -h "$aab" | cut -f1)"
  else
    warn "$(basename "$aab") is NOT signed — check keystore.properties before uploading"
    exit 1
  fi
done
printf '  watch versionCode: %s\n' "$(grep -m1 'versionCode = ' wear/build.gradle.kts | tr -dc '0-9')"

bold "4/4  What is left"
cat <<'CHECKLIST'

  THE ONE-TIME WEAR SETUP IS DONE. It was completed on 1 Sept 2026 and does not repeat:
  the Wear OS form factor was added, a dedicated Wear track created, the 454x454
  screenshots uploaded, and 1001 (1.2.0) submitted. It went to a human reviewer the same
  day. Everything in this step used to be a six-item Console walkthrough; none of it
  applies to a second Wear release.

  DO NOT ASK THE OWNER TO TEST THE WATCH. This script used to open with "pair a watch and
  check the sync, it has never been proved to work". That was true when it was written and
  is not now: the watch and phone were shown to agree on Asr on 31 Aug (handover A18), and
  he checked the watch himself, compass included, on 1 Sept. He has asked explicitly that
  it not be raised again. Raising it costs trust that a real defect report will need.

  FOR A LATER WEAR RELEASE, the whole remaining job is two presses in the Play Console,
  and both are his:

      Test and release -> Production, form factor selector on "Wear OS only"
      Create new release -> attach wear/build/outputs/bundle/release/wear-release.aab
      Submit for review                     <- his signature, never an assistant's

  Bump wear/build.gradle.kts versionCode first. The watch climbs its own 1000+ band, the
  phone stays low, and the two can never collide. Never upload a saved artifact: build
  fresh from main, which is what step 2 above just did.

  HOW TO TELL IF IT PUBLISHED. Play sends no email when a review passes. Fetch the public
  store page and read "Updated on" -- it moves when a release publishes and for nothing
  else. A store listing change does not move it.

      curl -sL -A "Mozilla/5.0" \
        "https://play.google.com/store/apps/details?id=com.sajdatime.app&hl=en_GB" \
        | grep -o "Updated on[^<]*<[^>]*>[^<]*"

  NOT DONE BY THIS SCRIPT: nothing was submitted, nothing was published, and no Console
  setting was changed. It builds and it checks; it does not ship.

CHECKLIST
