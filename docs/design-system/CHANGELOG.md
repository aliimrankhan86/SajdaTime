# Design system change log

Every change to `docs/design-system/`, newest first. One entry per commit that changes a token, a
component spec, a pattern or a rule. Say what changed, why, and what was rejected. Owner decisions
also go in `docs/DESIGN_SYSTEM.md` §9 with his words.

---

## 30 Oct 2026: Bengali, and text that shrinks instead of breaking

- Bengali added to the languages table (`values-bn`, tag `bn-u-nu-latn`, tall script) and a full "Adding a language" checklist in 06 §6.6.
- New `shrinkToFit` (one line text shrinks to fit, to 60% or at large text its normal size; may go below 14sp, owner's rule) in 02, 03 §3.5 and `tokens/typography.json`; the Today widget date may shrink to 10sp.
- Tab bar aligns to the top so a two line label keeps its icon in line. Rejected: one line tab labels (cut with an ellipsis at 320dp, 2.0).

## 25 Oct 2026: independent review fixes

- A playbook row offered a recipe for a missed prayer counter, which the owner refused on 16 Oct 2026; it now says No.
- Precedence: the owner's §0 rules and the §4.1 inventory rank with his decisions, above these pages.
- `DesignTokensTest`: the icon files it reads are declared test inputs; a duplicated Material role fails.
- The deviations list is OPEN item `ds-deviations` in `docs/DESIGN_SYSTEM.md` §11, so it reaches the owner.
- Wording: only dismissable dialogs carry the X; Google's review card is the one allowed rating ask; Times shows no
  location in the card, not as a notice; `#14624B` is in the token file.

## 25 Oct 2026: created from release 1.6.0

- **What:** this folder, taken from `claude/1-6-0` at `1cdae4a`. Ten pages, five token files, and
  `DesignTokensTest` holding colour, Material roles, spacing, motion and type to the code in both
  directions, plus a check that every phone UI file is described here.
- **Why:** the owner asked (25 Oct 2026) for "a comprehensive design system document which you and
  other AI can easily use to extend or expand or create new features ... checked ... so any future
  session cannot drift away from it", later to be used for the iPhone app. The existing
  `docs/DESIGN_SYSTEM.md` held the right rules but mixed values, history and decisions in one long
  narrative, and nothing checked its values against the code.
- **Owner's choices (25 Oct 2026, asked first):** start from 1.6; a new folder with
  `docs/DESIGN_SYSTEM.md` kept for his rules, decision log and open questions; a build test against
  drift.
- **Rejected:** replacing `docs/DESIGN_SYSTEM.md` (every session and two agents read it, and
  `DesignRulesTest` reads its §4.1 inventory); Swift constants or any iOS code (not until Ali
  decides, `CLAUDE.md`); W3C token tooling such as Style Dictionary (a new dependency for no gain
  while there is one platform; the JSON follows the same shape so it can be adopted later).
- **Not checked by a test, so review:** `shape.json` (a census of radii and sizes, not enforced),
  the pages' prose, the watch.
