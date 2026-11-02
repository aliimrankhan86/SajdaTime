# SajdaTime: instructions for any AI working on this project

**First read [`docs/AI-START-HERE.md`](docs/AI-START-HERE.md) in full.** It is the complete briefing: what the app is, what
is live, **which branch to use (`claude/1-6-0`, never `main`)**, the design system in `docs/design-system/`, the rules that
cause the most mistakes and how to finish. The notes below stay for tools that read only this file.

Tools such as Codex, Cursor and DeepSeek load or are given this file. **If you are a helper model doing routine work
(searching, counting, running tests, a code change from a written spec), read
[`docs/AI-WORKER-BRIEF.md`](docs/AI-WORKER-BRIEF.md) in full first: it is self-contained and says what you may and may
not do.** For anything more, the rules live in one place so they cannot drift: read [`CLAUDE.md`](CLAUDE.md) in full,
then [`docs/HANDOVER.md`](docs/HANDOVER.md), starting with the first 📍 block at the top of section 11 (the state of play). Do that
before you change anything or ask the owner a question. He is not technical and does not want to brief you.

Four things that are easy to get wrong, each explained in `CLAUDE.md`:
- Usage counts (Firebase Analytics) are **opt in, closed, and never about belief**. Do not turn them on by default, add events or
  add a country list. The evidence is in `docs/HANDOVER.md` section 10 (3 to 4 Oct 2026).
- Never touch, print or copy the release signing key or `keystore.properties`. Never push to `main`.
- The religious disclaimer is never removed or softened; since 1.6 it is the first row of Settings, About (owner, 23 Oct 2026), and its dua request stays the last paragraph of the full text and nowhere else. Translations are written and checked by Claude with no native speaker review, by the owner's decision of 9 Oct 2026, and the app never says a translation awaits review.
- Watch testing is finished. Do not ask the owner to test the watch.
