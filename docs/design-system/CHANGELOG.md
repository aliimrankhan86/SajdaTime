# Design system change log

Every change to `docs/design-system/`, newest first. One entry per commit that changes a token, a
component spec, a pattern or a rule. Say what changed, why, and what was rejected. Owner decisions
also go in `docs/DESIGN_SYSTEM.md` §9 with his words.

---

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
