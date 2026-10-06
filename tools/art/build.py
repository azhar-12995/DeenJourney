"""Render all Deen Journey art: svg/<name>.svg + png/<name>.png (+ contact sheets).

usage: python build.py [filter-substring ...]
"""
import os, sys, io, json
import resvg_py
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from scenes import SCENES
import scenes2  # registers logo, icon, prophets, tasbih, compass
from figures import FIGS

OUT_SVG = os.path.join(HERE, 'svg')
OUT_PNG = os.path.join(HERE, 'png')
os.makedirs(OUT_SVG, exist_ok=True)
os.makedirs(OUT_PNG, exist_ok=True)

ALL = {}
ALL.update(SCENES)
ALL.update(FIGS)
try:
    from glyphs import GLYPHS
    ALL.update(GLYPHS)
except ImportError:
    pass


def render(svg_text, width):
    b = resvg_py.svg_to_bytes(svg_string=svg_text, width=width)
    return Image.open(io.BytesIO(bytes(b))).convert('RGBA')


def main(filters):
    names = [n for n in ALL if not filters or any(f in n for f in filters)]
    thumbs = []
    for n in names:
        svg = ALL[n]().text()
        open(os.path.join(OUT_SVG, n + '.svg'), 'w', encoding='utf8').write(svg)
        w = int(svg.split('width="')[1].split('"')[0])
        im = render(svg, 192 if n.startswith('glyph_') else min(max(w, 96), 1200))
        im.save(os.path.join(OUT_PNG, n + '.png'))
        thumbs.append((n, im))
    glyphs = [x for x in thumbs if x[0].startswith('glyph_')]
    thumbs = [x for x in thumbs if not x[0].startswith('glyph_')]
    if glyphs:
        cs, cols = 132, 9
        rows = (len(glyphs) + cols - 1) // cols
        gs = Image.new('RGB', (cols * cs, rows * (cs + 16)), '#FAF7EF')
        gd = ImageDraw.Draw(gs)
        for i, (n, im) in enumerate(glyphs):
            x, y = (i % cols) * cs, (i // cols) * (cs + 16)
            gd.ellipse((x + 18, y + 8, x + cs - 18, y + cs - 28), fill='#F6EDD8')
            t2 = im.copy(); t2.thumbnail((64, 64))
            gs.paste(t2, (x + (cs - t2.width) // 2, y + 8 + (cs - 36 - t2.height) // 2), t2)
            gd.text((x + 6, y + cs - 14), n[6:], fill='#333333')
        gs.save(os.path.join(HERE, 'sheet_glyphs.png'))
    if not thumbs:
        print(len(glyphs), 'glyphs rendered'); return
    # contact sheet
    cell_w, cell_h = 300, 190
    cols = 4
    rows = (len(thumbs) + cols - 1) // cols
    sheet = Image.new('RGB', (cols * cell_w, rows * cell_h), '#FFFFFF')
    d = ImageDraw.Draw(sheet)
    for i, (n, im) in enumerate(thumbs):
        t = im.copy()
        t.thumbnail((cell_w - 10, cell_h - 26))
        x, y = (i % cols) * cell_w, (i // cols) * cell_h
        bg = Image.new('RGBA', t.size, '#EEEAE0')
        bg.alpha_composite(t)
        sheet.paste(bg.convert('RGB'), (x + 5, y + 4))
        d.text((x + 6, y + cell_h - 20), n, fill='#333333')
    sheet.save(os.path.join(HERE, 'sheet.png'))
    print(len(thumbs), 'rendered')


if __name__ == '__main__':
    main(sys.argv[1:])
