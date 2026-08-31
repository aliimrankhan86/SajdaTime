#!/usr/bin/env bash
#
# Everything mechanical that has to happen before the Wear OS release, in one command.
#
#   ./tools/ship-wear.sh
#
# Why this exists
# ---------------
# The remaining Wear work splits cleanly into two halves. One half is mechanical and
# repeatable: push, clean, test, lint, build both bundles, check they are signed. The other
# half needs a human being — a physical watch, a Google account, and consent to Google's
# Wear OS review policy — and cannot be automated by anyone, including an assistant with a
# shell on this machine. This script does the whole of the first half and then prints the
# second half as a checklist, so the only thing left is the part that genuinely needs you.
#
# It is deliberately noisy about what it did NOT do. A script that prints "done" when it
# only did the easy half is how a release ships without its verification.

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

bold "4/4  What is left, and it is not mechanical"
cat <<'CHECKLIST'

  A. PAIR A WATCH AND CHECK THE SYNC — do this before uploading, not after.
     The phone-to-watch settings sync has never been proved to work. If it does not,
     the watch runs on its own Shafi'i default while your phone is Hanafi, and the two
     show Asr about an HOUR apart. That is the one defect worth delaying a release for.

       1. Set the phone to Hanafi.
       2. Open the watch.
       3. Compare Asr.

     Same time  -> sync works, carry on.
     An hour apart -> stop. The school button at the foot of the watch's times list
                      will tell you which school it actually used.

  B. THE PLAY CONSOLE, which needs your account and your consent:

       Test and release -> Advanced settings -> Form factors -> Add form factor -> Wear OS
       Upload docs/store/upload/wear-os/w1-times.png and w2-qibla.png
       Manage -> Use a dedicated release track for Wear OS
       Upload wear/build/outputs/bundle/release/wear-release.aab
         with the form factor selector set to Wear OS, NOT phones
       Agree to the Wear OS review policy   <- legal consent, yours alone
       Submit

     This triggers a separate human review against the Wear OS quality guidelines,
     shown as Pending / Approved / Not approved. Unlike the phone release on 20 Aug
     there really is a decision to wait for. Do not wait for an email telling you it
     passed; Play does not send one. Check Publishing overview -> Submission activity,
     or just look at the public store listing. See docs/HANDOVER.md lesson 105.

  NOT DONE BY THIS SCRIPT: nothing was submitted, nothing was published, no Console
  setting was changed, and no watch was tested. Steps A and B above are still open.

CHECKLIST
