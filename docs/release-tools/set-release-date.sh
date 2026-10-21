#!/bin/bash
# Release day, step 2 of RELEASE-1.5.0.md: date the privacy policy for the day 1.5.0 is merged and sent.
# The release date is not fixed in advance (owner, 21 Oct 2026), so nothing else in the repo carries it:
# docs/privacy.html "Last updated" is the only published date that has to move (checked 21 Oct 2026 across
# docs/*.html and the app's strings). Run it on the day, from the repo, then commit and push.
#   docs/release-tools/set-release-date.sh               # today
#   docs/release-tools/set-release-date.sh "2 November 2026"
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
day="${1:-$(LC_ALL=C date '+%-d %B %Y')}"
[[ "$day" =~ ^[0-9]{1,2}\ (January|February|March|April|May|June|July|August|September|October|November|December)\ 20[0-9]{2}$ ]] \
  || { echo "Date must look like '2 November 2026', got '$day'"; exit 1; }
sed -i '' -E "s/(<p class=\"updated\">Last updated: )[^·]+ ·/\1$day ·/" docs/privacy.html
grep -q "Last updated: $day ·" docs/privacy.html || { echo "FAIL: privacy.html not changed"; exit 1; }
echo "privacy.html now says: Last updated: $day"
echo "Next: git commit -am \"Privacy page dated $day, the day 1.5.0 is merged and sent\" && git push"
