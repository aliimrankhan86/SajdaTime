# SajdaTime: instructions for any AI working on this project

Tools such as Codex and Cursor load this file automatically. **The rules live in one place so they cannot drift: read
[`CLAUDE.md`](CLAUDE.md) in full, then [`docs/HANDOVER.md`](docs/HANDOVER.md), starting with the `STATE OF PLAY` block at the
top of section 11.** Do that before you change anything or ask the owner a question. He is not technical and does not want to
brief you.

Four things that are easy to get wrong, each explained in `CLAUDE.md`:
- Usage counts (Firebase Analytics) are **opt in, closed, and never about belief**. Do not turn them on by default, add events or
  add a country list. The evidence is in `docs/HANDOVER.md` section 10 (3 to 4 Oct 2026).
- Never touch, print or copy the release signing key or `keystore.properties`. Never push to `main`.
- The religious disclaimer and its dua request stay exactly where they are; never machine-translate the app.
- Watch testing is finished. Do not ask the owner to test the watch.
