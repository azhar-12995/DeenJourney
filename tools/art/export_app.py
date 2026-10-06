"""Export art for the app:
  - composeResources/drawable/art_*.webp  (illustrations, avatars, poses, prophets, empty states, tasbih, compass, logo)
  - Android launcher icon (adaptive foreground/background/monochrome) + splash logo
  - iOS AppIcon 1024
Glyphs and line icons are NOT exported here — they become Compose ImageVectors (gen_vectors.py).
"""
import os, sys, io, re
import resvg_py
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
from scenes import SCENES
import scenes2  # noqa: registers logo, icon, prophets, tasbih, compass
from figures import FIGS
from lib import Svg

DRAW = os.path.join(ROOT, 'composeApp', 'src', 'commonMain', 'composeResources', 'drawable')
RES = os.path.join(ROOT, 'composeApp', 'src', 'androidMain', 'res')
IOS = os.path.join(ROOT, 'iosApp', 'iosApp', 'Assets.xcassets', 'AppIcon.appiconset')
os.makedirs(DRAW, exist_ok=True)

ALL = dict(SCENES)
ALL.update(FIGS)

# target pixel width per asset family (≈ 3x of the largest on-screen size)
def width_for(name):
    if name.startswith(('hero_', 'skyline_')):
        return 1200
    if name.startswith('avatar_'):
        return 288
    if name.startswith('pose_'):
        return 480
    if name.startswith('prophet_'):
        return 600
    if name.startswith('empty_'):
        return 720
    if name in ('tasbih_ring', 'compass_dial'):
        return 1000
    if name == 'quran_rehal':
        return 600
    if name == 'logo_mark':
        return 512
    return 800


def render(svg_text, w):
    b = resvg_py.svg_to_bytes(svg_string=svg_text, width=w)
    return Image.open(io.BytesIO(bytes(b))).convert('RGBA')


def main():
    n = 0
    for name, fn in ALL.items():
        if name == 'app_icon':
            continue
        im = render(fn().text(), width_for(name))
        im.save(os.path.join(DRAW, 'art_' + name + '.webp'), 'WEBP', quality=90, method=6)
        n += 1
    print('drawables', n)

    # ---- Android adaptive icon ----
    logo = scenes2.logo_mark(512).text()
    bg = Svg(432, 432)
    bg.rect(0, 0, 432, 432, bg.lin([(0, '#FBF6EA'), (1, '#F1E6CC')]))
    from lib import pattern
    pattern(bg, 0, 0, 432, 432, 72, '#CDAE6E', 1.2, .18)
    os.makedirs(os.path.join(RES, 'drawable-nodpi'), exist_ok=True)
    render(bg.text(), 432).save(os.path.join(RES, 'drawable-nodpi', 'ic_launcher_bg.png'))
    dens = {'mdpi': 108, 'hdpi': 162, 'xhdpi': 216, 'xxhdpi': 324, 'xxxhdpi': 432}
    mark = render(logo, 1024)
    mono = mark.copy()
    px = mono.load()
    for y in range(mono.height):
        for x in range(mono.width):
            r, g, b, a = px[x, y]
            px[x, y] = (255, 255, 255, a)
    for d, size in dens.items():
        folder = os.path.join(RES, 'mipmap-' + d)
        os.makedirs(folder, exist_ok=True)
        for nm, src in (('ic_launcher_foreground', mark), ('ic_launcher_monochrome', mono)):
            canvas = Image.new('RGBA', (size, size), (0, 0, 0, 0))
            inner = int(size * 0.60)
            m = src.resize((inner, inner), Image.LANCZOS)
            canvas.alpha_composite(m, ((size - inner) // 2, (size - inner) // 2 - int(size * 0.01)))
            canvas.save(os.path.join(folder, nm + '.png'))
        # legacy square icon (pre-26 launchers / some stores)
        full = render(scenes2.app_icon(1024).text(), 1024).resize((size * 48 // 108, size * 48 // 108), Image.LANCZOS)
        full.save(os.path.join(folder, 'ic_launcher.png'))
        full.save(os.path.join(folder, 'ic_launcher_round.png'))
    render(logo, 432).save(os.path.join(RES, 'drawable-nodpi', 'splash_logo.png'))
    # Play Store icon
    os.makedirs(os.path.join(ROOT, 'store'), exist_ok=True)
    render(scenes2.app_icon(512).text(), 512).convert('RGB').save(os.path.join(ROOT, 'store', 'play_icon_512.png'))

    # ---- iOS ----
    os.makedirs(IOS, exist_ok=True)
    render(scenes2.app_icon(1024).text(), 1024).convert('RGB').save(os.path.join(IOS, 'AppIcon-1024.png'))
    open(os.path.join(IOS, 'Contents.json'), 'w').write('{"images":[{"filename":"AppIcon-1024.png","idiom":"universal","platform":"ios","size":"1024x1024"}],"info":{"author":"xcode","version":1}}')
    print('icons done')


if __name__ == '__main__':
    main()
