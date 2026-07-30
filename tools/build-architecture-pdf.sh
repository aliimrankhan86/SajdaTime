#!/usr/bin/env bash
#
# Renders docs/ARCHITECTURE.md to docs/SajdaTime-Architecture.pdf.
#
# The PDF is the handoff document, and the first version of it drifted a whole release
# behind the markdown because it was produced by hand. Run this after editing the
# markdown, or the two disagree again.
#
# Needs: node (for marked) and Google Chrome. Both are already on a typical Mac dev box.

set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
src="$root/docs/ARCHITECTURE.md"
out="$root/docs/SajdaTime-Architecture.pdf"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
[ -x "$chrome" ] || { echo "Google Chrome not found at $chrome" >&2; exit 1; }

echo "Converting markdown..."
npx --yes marked@12 --gfm -i "$src" -o "$work/body.html"

cat > "$work/page.html" <<'HTML'
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>SajdaTime — Requirements and Architecture</title>
<style>
  @page { size: A4; margin: 18mm 16mm; }
  body {
    font-family: -apple-system, "Helvetica Neue", Arial, sans-serif;
    font-size: 10.5pt; line-height: 1.55; color: #12211C; margin: 0;
  }
  h1, h2, h3, h4 { color: #14624B; line-height: 1.25; margin: 1.4em 0 0.5em; }
  h1 { font-size: 21pt; border-bottom: 2px solid #14624B; padding-bottom: 6px; }
  h2 { font-size: 15pt; margin-top: 1.8em; page-break-after: avoid; }
  h3 { font-size: 12pt; page-break-after: avoid; }
  p, li { orphans: 3; widows: 3; }
  code {
    font-family: "SF Mono", Menlo, monospace; font-size: 9pt;
    background: #EDF1EE; padding: 1px 4px; border-radius: 3px;
  }
  pre {
    background: #F7F6F2; border: 1px solid #C9D2CD; border-radius: 5px;
    padding: 10px 12px; overflow-x: auto; page-break-inside: avoid;
  }
  pre code { background: none; padding: 0; }
  table {
    border-collapse: collapse; width: 100%; margin: 1em 0;
    font-size: 9.5pt; page-break-inside: avoid;
  }
  th, td { border: 1px solid #C9D2CD; padding: 6px 9px; text-align: left; vertical-align: top; }
  th { background: #D7EAE0; color: #05271C; font-weight: 600; }
  tr:nth-child(even) td { background: #FBFAF7; }
  hr { border: none; border-top: 1px solid #C9D2CD; margin: 2em 0; }
  blockquote {
    border-left: 3px solid #8A5200; margin: 1em 0; padding: 0.2em 0 0.2em 14px; color: #43524B;
  }
  a { color: #14624B; }
</style>
</head>
<body>
HTML
cat "$work/body.html" >> "$work/page.html"
echo '</body></html>' >> "$work/page.html"

echo "Printing to PDF..."
"$chrome" --headless --disable-gpu --no-pdf-header-footer \
  --print-to-pdf="$out" "file://$work/page.html" 2>/dev/null

echo "Wrote $out"
