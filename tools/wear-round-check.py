#!/usr/bin/env python3
"""Fail if anything the watch draws falls outside the round display.

Google's Wear app quality bar (WO-V16) asks that no text or control be cut off by the
screen edge, on a display "larger than or equal to a 192dp circle". On a round watch the
screen is the circle inscribed in the square framebuffer — but `adb exec-out screencap`
captures the framebuffer *before* the system's rounded-corner overlay is applied, so a
screenshot shows the corners the user will never see. That is the bug this catches:
content that looks present in a capture and is eaten by the bezel on the wrist.

    tools/wear-round-check.py shot.png [shot.png ...]

Exits non-zero and prints the offending coordinates if any lit pixel sits outside the
circle. Pure standard library on purpose — no Pillow, no venv, nothing to install before
a release check can be run.

Two calibration constants, both chosen from measurements rather than taste:

  BG    Anything this dark on every channel is the black Wear background rather than
        content. Set above the antialiased skirt a dark-green button leaves behind
        (~rgb(9,22,16)) and well below the dimmest text the app draws (onSurfaceVariant,
        ~rgb(169,178,174)).

  SLACK A shape whose edge lands exactly on the rim antialiases about half a pixel past
        it. That is the renderer, not clipping. Real clipping is tens of pixels deep.
"""

import math
import struct
import sys
import zlib

BG = 45
SLACK = 1.0


def read_png(path):
    """Minimal decoder for what screencap produces: 8-bit RGB/RGBA, non-interlaced."""
    data = open(path, "rb").read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path}: not a PNG")
    pos, idat, width, height, channels = 8, bytearray(), 0, 0, 0
    while pos < len(data):
        (length,) = struct.unpack(">I", data[pos:pos + 4])
        kind = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            width, height, depth, colour, _, _, interlace = struct.unpack(">IIBBBBB", body)
            if depth != 8 or interlace != 0 or colour not in (2, 6):
                raise ValueError(f"{path}: unsupported PNG (depth {depth}, colour {colour})")
            channels = 3 if colour == 2 else 4
        elif kind == b"IDAT":
            idat += body
        elif kind == b"IEND":
            break
        pos += 12 + length

    raw = zlib.decompress(bytes(idat))
    stride = width * channels
    out = bytearray(height * stride)
    prev = bytearray(stride)
    at = 0
    for y in range(height):
        filt = raw[at]
        line = bytearray(raw[at + 1:at + 1 + stride])
        at += 1 + stride
        # PNG per-scanline filters, undone in place (RFC 2083 §6).
        for i in range(stride):
            a = line[i - channels] if i >= channels else 0
            b = prev[i]
            if filt == 1:
                line[i] = (line[i] + a) & 0xFF
            elif filt == 2:
                line[i] = (line[i] + b) & 0xFF
            elif filt == 3:
                line[i] = (line[i] + ((a + b) >> 1)) & 0xFF
            elif filt == 4:
                c = prev[i - channels] if i >= channels else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pred) & 0xFF
        out[y * stride:(y + 1) * stride] = line
        prev = line
    return width, height, channels, bytes(out)


def check(path):
    width, height, channels, px = read_png(path)
    if width != height:
        raise ValueError(f"{path}: {width}x{height} is not a square watch capture")
    radius = width / 2.0
    centre = width / 2.0
    offenders = []
    for y in range(height):
        dy = y + 0.5 - centre
        # The circle only leaves the frame near the corners; skip the middle band.
        half = math.sqrt(max(radius * radius - dy * dy, 0.0))
        for x in list(range(0, int(centre - half) + 1)) + list(range(int(centre + half), width)):
            d = math.hypot(x + 0.5 - centre, dy)
            if d <= radius + SLACK:
                continue
            i = (y * width + x) * channels
            if max(px[i], px[i + 1], px[i + 2]) > BG:
                offenders.append((d - radius, x, y, (px[i], px[i + 1], px[i + 2])))
    offenders.sort(reverse=True)
    return width, offenders


def main(paths):
    failed = False
    for path in paths:
        width, offenders = check(path)
        if not offenders:
            print(f"PASS  {path}  ({width}px round): nothing outside the display")
            continue
        failed = True
        print(f"FAIL  {path}  ({width}px round): {len(offenders)} px behind the bezel")
        for d, x, y, rgb in offenders[:8]:
            print(f"        {d:6.1f}px past the rim at ({x},{y}) rgb{rgb}")
    return 1 if failed else 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    sys.exit(main(sys.argv[1:]))
