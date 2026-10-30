#!/usr/bin/env python3
"""Remembers which translations were already reviewed, so a session reviews only what changed.

    python3 tools/translation-ledger.py          # list new or changed translations (exit 1 if any)
    python3 tools/translation-ledger.py stamp    # record everything as reviewed

Ali, 30 Oct 2026: "have a mechanism not to retest the translation which you have already done".
A unit is one string key in a values-<lang>/strings.xml (hashed together with its English, so a changed
English source counts as changed too) or one fenced block in docs/store/LISTING-LOCALES.md (title, short
description, full description, What's new of one language and release). Run `stamp` only after the
reviewer agent has passed the changes `check` listed, in the same commit. A unit whose hash is in the
ledger needs no new review; mechanical checks (TranslationIntegrityTest, check-translation.py) still run
every time, they are cheap.
"""
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LEDGER = ROOT / "docs/translation/reviewed.json"


def h(*parts):
    return hashlib.sha256("\x00".join(parts).encode()).hexdigest()[:16]


def strings(path):
    out = {}
    for el in ET.parse(path).getroot():
        items = [el] if el.tag == "string" else list(el)
        out[el.get("name")] = "\x01".join((i.get("quantity") or "") + "".join(i.itertext()) for i in items)
    return out


def units():
    u = {}
    for mod in ("app", "core", "wear"):
        src = strings(ROOT / mod / "src/main/res/values/strings.xml")
        for p in sorted((ROOT / mod / "src/main/res").glob("values-*/strings.xml")):
            for k, v in strings(p).items():
                u[f"{mod}/{p.parent.name}:{k}"] = h(src.get(k, ""), v)
    section, label = "", ""
    text = (ROOT / "docs/store/LISTING-LOCALES.md").read_text(encoding="utf8")
    for m in re.finditer(r"^(#{2,3} [^\n]*)$|^\*\*(.+?)\*\*|^```\n(.*?)\n```", text, re.M | re.S):
        if m.group(1):
            section = m.group(1)
        elif m.group(2):
            label = m.group(2)
        elif section:
            key = f"listing/{section}/{label}"
            n = 2
            while key in u:  # two blocks under one label keep separate entries
                key = f"listing/{section}/{label} #{n}"
                n += 1
            u[key] = h(m.group(3))
    return u


def main(args):
    now = units()
    if args == ["stamp"]:
        LEDGER.write_text(json.dumps(now, indent=0, sort_keys=True, ensure_ascii=False) + "\n", encoding="utf8")
        print(f"stamped {len(now)} units as reviewed")
        return 0
    old = json.loads(LEDGER.read_text(encoding="utf8")) if LEDGER.exists() else {}
    todo = sorted(k for k, v in now.items() if old.get(k) != v)
    for k in todo:
        print(("CHANGED " if k in old else "NEW     ") + k)
    print(f"{len(todo)} to review, {len(now) - len(todo)} already reviewed")
    return 1 if todo else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
