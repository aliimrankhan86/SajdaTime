#!/usr/bin/env python3
"""Mechanical checks for a translation of the app's Android string resources.

    python3 tools/check-translation.py <qualifier> <language-tag>
    e.g.  python3 tools/check-translation.py in in-ID

Checks every module (app, core, wear) that has a values-<qualifier>/strings.xml against its
English source. It cannot judge whether a translation is *good*. It can and does catch the
things that break the app or change its meaning silently: a missing key, a lost or reordered
placeholder, a wrong plural form set, an unescaped apostrophe (a build error), a changed
paragraph count (the disclaimer's structure carries meaning), a changed bullet count, and
the wrong app_language_tag. Exit status 0 only when there are no errors; warnings do not fail.
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODULES = ["app", "core", "wear"]
# CLDR plural categories each language needs (Android lint MissingQuantity / ExtraTranslation).
PLURALS = {"in": {"other"}, "id": {"other"}, "tr": {"one", "other"}, "ur": {"one", "other"}}
FMT = re.compile(r"%(?:\d+\$)?[-+#0 ,(]*\d*(?:\.\d+)?[sdfxXc%]")


def load(path):
    """name -> (kind, value, attrs); value is str, dict (plurals) or list (array)."""
    out = {}
    for el in ET.parse(path).getroot():
        name = el.get("name")
        if el.tag == "string":
            out[name] = ("string", "".join(el.itertext()), dict(el.attrib))
        elif el.tag == "plurals":
            out[name] = ("plurals", {i.get("quantity"): "".join(i.itertext()) for i in el}, dict(el.attrib))
        elif el.tag == "string-array":
            out[name] = ("array", ["".join(i.itertext()) for i in el], dict(el.attrib))
    return out


def raw_text(path):
    """The un-parsed file text, for escape checks ElementTree would hide."""
    return Path(path).read_text(encoding="utf8")


def placeholders(s):
    return sorted(m.group(0) for m in FMT.finditer(s))


def main(qualifier, tag):
    lang = qualifier.split("-")[0]
    errors, warnings = [], []
    found = False
    for mod in MODULES:
        src_p = ROOT / mod / "src/main/res/values/strings.xml"
        tr_p = ROOT / mod / f"src/main/res/values-{qualifier}/strings.xml"
        if not tr_p.exists():
            errors.append(f"{mod}: missing {tr_p.relative_to(ROOT)}")
            continue
        found = True
        src, tr = load(src_p), load(tr_p)
        need = {k for k, v in src.items() if v[2].get("translatable") != "false"}
        if mod == "core":
            need.discard("app_language_tag")
            declared = tr.get("app_language_tag", (None, None, None))[1]
            if declared != tag:
                errors.append(f"core: app_language_tag is {declared!r}, expected {tag!r}")
        elif "app_language_tag" in tr:
            errors.append(f"{mod}: app_language_tag belongs only in core")
        missing = sorted(need - set(tr))
        extra = sorted(set(tr) - set(src) - {"app_language_tag"})
        if missing:
            errors.append(f"{mod}: missing keys: {missing}")
        if extra:
            errors.append(f"{mod}: keys not in the source: {extra}")
        text = raw_text(tr_p)
        for m in re.finditer(r"(?<!\\)'", re.sub(r"<!--.*?-->", "", text, flags=re.S)):
            # An apostrophe inside a double-quoted string is legal, but we never quote.
            ctx = text[max(0, m.start() - 25): m.end() + 10].replace("\n", " ")
            errors.append(f"{mod}: unescaped apostrophe near: ...{ctx}...")
            break
        for name in sorted(need & set(tr)):
            k, sv, _ = src[name]
            k2, tv, _ = tr[name]
            if k != k2:
                errors.append(f"{mod}:{name}: kind differs ({k} vs {k2})")
                continue
            if k == "string":
                if placeholders(sv) != placeholders(tv):
                    errors.append(f"{mod}:{name}: placeholders {placeholders(sv)} -> {placeholders(tv)}")
                if sv.count("\\n") != tv.count("\\n") and "\n" not in sv:
                    errors.append(f"{mod}:{name}: \\n count {sv.count(chr(92)+'n')} -> {tv.count(chr(92)+'n')}")
                if sv.count("•") != tv.count("•"):
                    errors.append(f"{mod}:{name}: bullet count {sv.count(chr(0x2022))} -> {tv.count(chr(0x2022))}")
                if re.search(r"<[a-zA-Z/]", sv) != re.search(r"<[a-zA-Z/]", tv):
                    errors.append(f"{mod}:{name}: markup presence differs")
                if sv.strip() and not tv.strip():
                    errors.append(f"{mod}:{name}: empty translation")
            elif k == "plurals":
                want = PLURALS.get(lang)
                have = set(tv)
                if want is not None and have != want:
                    errors.append(f"{mod}:{name}: plural quantities {sorted(have)}, {lang} needs {sorted(want)}")
                base = sv.get("other") or next(iter(sv.values()))
                for q, t in tv.items():
                    ref = sv.get(q, base)
                    if placeholders(ref) != placeholders(t):
                        errors.append(f"{mod}:{name}[{q}]: placeholders {placeholders(ref)} -> {placeholders(t)}")
            elif k == "array":
                if len(sv) != len(tv):
                    errors.append(f"{mod}:{name}: {len(sv)} items -> {len(tv)}")
        # Long prose that is still mostly ASCII letters is probably untranslated (not for Latin-script languages).
        if lang == "ur":
            for name in sorted(need & set(tr)):
                k, tv, _ = tr[name]
                if k == "string" and len(tv) > 60:
                    ascii_letters = sum(c.isascii() and c.isalpha() for c in tv)
                    if ascii_letters > 0.5 * len(tv):
                        warnings.append(f"{mod}:{name}: mostly Latin letters, untranslated?")
    # The disclaimer's structure carries meaning: paragraphs, and the dua request last.
    d_src = load(ROOT / "app/src/main/res/values/strings.xml").get("disclaimer_body")
    d_tr_p = ROOT / "app" / f"src/main/res/values-{qualifier}/strings.xml"
    if d_src and d_tr_p.exists():
        d_tr = load(d_tr_p).get("disclaimer_body")
        if d_tr:
            ps, pt = d_src[1].split("\\n\\n"), d_tr[1].split("\\n\\n")
            if len(ps) != len(pt):
                errors.append(f"disclaimer_body: {len(ps)} paragraphs -> {len(pt)}")
    if not found:
        errors.append("no translation files found at all")
    for w in warnings:
        print("WARN ", w)
    for e in errors:
        print("ERROR", e)
    print(f"{len(errors)} error(s), {len(warnings)} warning(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    sys.exit(main(sys.argv[1], sys.argv[2]))
