"""Combine exported screen PNGs into a review sheet: python sheet.py out.png A01 A02 ... (or a prefix like 'A')."""
import sys, os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
SH = os.path.join(HERE, 'shots')
out, ids = sys.argv[1], sys.argv[2:]
cols = int(os.environ.get('COLS', '4'))
files = []
for i in ids:
    p = os.path.join(SH, i + '.png')
    if os.path.exists(p):
        files.append((i, p))
W = 375
ims = [(i, Image.open(p).convert('RGB')) for i, p in files]
ims = [(i, im.resize((W, int(im.height * W / im.width)))) for i, im in ims]
H = max(im.height for _, im in ims)
rows = (len(ims) + cols - 1) // cols
sheet = Image.new('RGB', (cols * (W + 16) + 16, rows * (H + 36) + 16), '#D9D3C3')
d = ImageDraw.Draw(sheet)
for k, (i, im) in enumerate(ims):
    x, y = 16 + (k % cols) * (W + 16), 16 + (k // cols) * (H + 36)
    sheet.paste(im, (x, y + 20))
    d.text((x, y + 4), i, fill='#000000')
sheet.save(os.path.join(SH, out))
print(sheet.size)
