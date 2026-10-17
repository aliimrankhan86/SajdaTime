#!/usr/bin/env python3
"""Draws the two widget picker previews into app/src/main/res/drawable-nodpi/.

Why this exists and what it is not. A launcher below Android 12 shows `previewImage` in the widget
picker, and an app icon there would misdescribe the widget. Android 12 and later draw the real layout
(`previewLayout`) and ignore these. There is no emulator in the session that made them, so they are
DRAWN from the layout's own numbers, not captured from a screen. They show sample London times for a
February day and no Ramadan words, so nothing in them is wrong in any month.

The colours are the light half of the widget palette (values/colors.xml, which repeats Color.kt).
If the layouts or the palette change, change the numbers here and run it again:

    python3 tools/make-widget-previews.py

Needs Pillow. The font is DejaVu Sans, a little wider than Roboto, which only ever makes the preview
look slightly fuller than the real widget.
"""
import os
import sys
from PIL import Image, ImageDraw, ImageFont

SCALE = 3  # px per dp; each file is drawn at 4x this and shrunk, for smooth edges
SS = 4

SURFACE = "#FDFCFB"
ON_SURFACE = "#1B211E"
ON_VARIANT = "#5A605C"
HIGHLIGHT = "#D7EAE0"
PRIMARY = "#0E6B4F"
OUTLINE = "#C9CFC9"

FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def font(path, sp):
    return ImageFont.truetype(path, int(round(sp * SCALE * SS)))


def px(dp):
    return dp * SCALE * SS


def card(w_dp, h_dp):
    img = Image.new("RGBA", (int(px(w_dp)), int(px(h_dp))), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(
        (0, 0, px(w_dp) - 1, px(h_dp) - 1), radius=px(16), fill=SURFACE, outline=OUTLINE, width=int(px(1))
    )
    return img, d


def text(d, x_dp, y_dp, s, f, fill, anchor="la"):
    d.text((px(x_dp), px(y_dp)), s, font=f, fill=fill, anchor=anchor)


def finish(img, name, out_dir):
    w, h = img.size[0] // SS, img.size[1] // SS
    small = img.resize((w, h), Image.LANCZOS)
    flat = Image.new("RGBA", small.size, (0, 0, 0, 0))
    flat.alpha_composite(small)
    # 64 colours keeps a flat, anti aliased UI image well under the 30 KB budget in the plan.
    q = flat.quantize(colors=64, method=Image.FASTOCTREE, dither=Image.NONE)
    path = os.path.join(out_dir, name)
    q.save(path, optimize=True)
    print(name, os.path.getsize(path), "bytes", q.size)


def next_prayer(out_dir):
    # 3 x 2 cells at its smallest (180 x 110 dp): name, time, and the countdown as the Chronometer
    # shows it (h:mm:ss). No context line, because an ordinary afternoon has none.
    img, d = card(180, 110)
    text(d, 14, 10, "Asr", font(FONT_BOLD, 20), ON_SURFACE)
    text(d, 14, 38, "2:56 pm", font(FONT, 16), ON_SURFACE)
    text(d, 14, 62, "1:20:00", font(FONT, 14), PRIMARY)
    finish(img, "widget_preview_next.png", out_dir)


def today(out_dir):
    # 4 x 3 cells at its smallest (250 x 180 dp). Six rows from the top, no header: the highlighted
    # row says which prayer is next, and the first row starts 8 dp down, as in the layout.
    img, d = card(250, 180)
    rows = [("Fajr", "5:10 am"), ("Sunrise", "7:01 am"), ("Dhuhr", "12:15 pm"),
            ("Asr", "2:56 pm"), ("Maghrib", "5:28 pm"), ("Isha", "7:13 pm")]
    y = 8
    row_h = 21.7
    for name, t in rows:
        dim = name == "Sunrise"
        colour = ON_VARIANT if dim else ON_SURFACE
        bold = name == "Asr"
        if bold:
            d.rounded_rectangle((px(8), px(y), px(242), px(y + row_h)), radius=px(8), fill=HIGHLIGHT)
            text(d, 74, y + 5, "Next", font(FONT, 10), ON_VARIANT)
        f = font(FONT_BOLD if bold else FONT, 13)
        text(d, 16, y + 3.5, name, f, colour)
        text(d, 234, y + 3.5, t, f, colour, anchor="ra")
        y += row_h
    finish(img, "widget_preview_today.png", out_dir)


if __name__ == "__main__":
    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.normpath(os.path.join(here, "..", "app", "src", "main", "res", "drawable-nodpi"))
    os.makedirs(out, exist_ok=True)
    next_prayer(out)
    today(out)
    sys.exit(0)
