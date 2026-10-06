"""Logo, app icon, prophet vignettes (symbols only, never people), tasbih ring, compass dial."""
import math
from lib import *
from scenes import _sky, madinah_scene, SCENES


def logo_mark(size=512, bg=None):
    """Deen Journey mark: mihrab arch, a path rising to a guiding star."""
    s = Svg(size, size)
    k = size / 512
    if bg:
        s.rect(0, 0, size, size, bg)
    arch = (f'M{f2(116 * k, 470 * k)}V{f2(214 * k)}C{f2(116 * k, 130 * k, 186 * k, 82 * k, 256 * k, 42 * k)}'
            f'C{f2(326 * k, 82 * k, 396 * k, 130 * k, 396 * k, 214 * k)}V{f2(470 * k)}Z')
    s.path(arch, s.lin([(0, '#1F6A4C'), (1, '#0B2E22')]))
    s.path(arch, None, stroke='#CDAE6E', sw=14 * k)
    inner = (f'M{f2(150 * k, 448 * k)}V{f2(222 * k)}C{f2(150 * k, 156 * k, 204 * k, 116 * k, 256 * k, 86 * k)}'
             f'C{f2(308 * k, 116 * k, 362 * k, 156 * k, 362 * k, 222 * k)}V{f2(448 * k)}Z')
    s.path(inner, None, stroke='#CDAE6E', sw=4 * k, op=.55)
    s.path(f'M{f2(150 * k, 380 * k)}C{f2(200 * k, 352 * k, 250 * k, 362 * k, 300 * k, 344 * k)}C{f2(330 * k, 334 * k, 350 * k, 336 * k, 362 * k, 340 * k)}V{f2(448 * k)}H{f2(150 * k)}Z', '#2E8060', op=.9)
    s.path(f'M{f2(186 * k, 448 * k)}C{f2(206 * k, 410 * k, 300 * k, 404 * k, 280 * k, 360 * k)}C{f2(262 * k, 322 * k, 214 * k, 318 * k, 244 * k, 262 * k)}'
           f'L{f2(252 * k, 262 * k)}C{f2(232 * k, 314 * k, 290 * k, 320 * k, 306 * k, 358 * k)}C{f2(328 * k, 414 * k, 252 * k, 414 * k, 238 * k, 448 * k)}Z', '#F3E3BC')
    s.circle(256 * k, 196 * k, 70 * k, s.rad([(0, '#F6D27A', .35), (1, '#F6D27A', 0)]))
    s.path(sparkle(256 * k, 196 * k, 42 * k, .2), '#E8C77A')
    s.path(f'M{f2(96 * k, 470 * k)}H{f2(416 * k)}', None, stroke='#CDAE6E', sw=14 * k)
    return s


def app_icon(size=1024):
    s = Svg(size, size)
    s.rect(0, 0, size, size, s.lin([(0, '#FBF6EA'), (1, '#F1E6CC')]))
    pattern(s, 0, 0, size, size, size / 6, '#CDAE6E', size / 400, .18)
    g = logo_mark(int(size * .78))
    s.defs.extend(g.defs)
    s.add(f'<g transform="translate({size * .11:.1f},{size * .1:.1f})">{"".join(g.body)}</g>')
    return s


def prophet_card(kind):
    s = Svg(400, 400)
    if kind == 'adam':
        _sky(s, '#EAF4E2', '#D8EBCF', '#C6DFBC')
        s.path(hill(0, 400, 400, [(0, 260), (200, 230), (400, 255)]), '#9CC48F')
        s.path('M0 330C100 300 160 340 260 320C320 308 360 316 400 300V400H0Z', '#6FA86A')
        s.path('M0 352C120 330 200 368 400 340V362C200 390 120 350 0 372Z', '#7FC3D9')
        round_tree(s, 200, 300, 230, '#7A5E26', '#4F8A4E', '#7FB36F', seed=3)
        for x, y in ((150, 160), (230, 140), (250, 190), (175, 200)):
            s.circle(x, y, 8, '#E05A47')
    elif kind == 'nuh':
        _sky(s, '#5D7389', '#8FA4B4', '#C9D6DC')
        s.path(cloud(120, 90, 200), '#46586A', op=.9)
        s.path(cloud(300, 70, 180), '#3E4F60', op=.9)
        for i in range(10):
            s.line(60 + i * 32, 110, 48 + i * 32, 160, '#DDE6EC', sw=2, op=.6)
        s.path('M90 250H310L290 300C250 316 150 316 110 300Z', '#7B5236')
        s.path('M90 250H310L304 266H96Z', '#5E3E28')
        s.rect(140, 205, 120, 46, '#A0703F')
        s.path('M130 207L200 176L270 207Z', '#7B5236')
        for i in range(3):
            s.rect(158 + i * 32, 220, 14, 14, '#5E3E28')
        s.path('M0 300C40 284 80 312 120 300C160 288 200 312 240 300C280 288 320 312 360 300C380 294 390 296 400 298V400H0Z', '#2C7FA8')
        s.path('M0 330C40 314 80 342 120 330C160 318 200 342 240 330C280 318 320 342 360 330C380 324 390 326 400 328V400H0Z', '#1F6588')
    elif kind == 'ibrahim':
        _sky(s, '#F8EBCF', '#F1DCAE', '#E8CB8E')
        s.circle(300, 110, 46, '#F9D88A')
        s.path(hill(0, 400, 400, [(0, 250), (120, 210), (260, 240), (400, 220)]), '#D9B77A')
        s.path('M120 200L200 186L280 198V322L200 340L120 322Z', '#141414')
        s.path('M120 200L200 186V340L120 322Z', '#0C0C0C')
        s.path('M120 222L200 208L280 220V236L200 224L120 238Z', '#C9A24E')
        s.path('M220 260L246 263V310L220 306Z', '#C9A24E')
        s.path('M0 330C120 320 280 345 400 330V400H0Z', '#E8DCC0')
    elif kind == 'musa':
        _sky(s, '#E9EEF2', '#D3DFE6', '#BFD0DA')
        s.path(hill(0, 400, 400, [(0, 170), (90, 120), (180, 170)]), '#9AA9B4')
        s.circle(330, 80, 34, '#FFF6DE')
        s.path('M0 200C60 190 120 210 160 205L150 400H0Z', '#2C7FA8')
        s.path('M400 200C340 190 280 210 240 205L250 400H400Z', '#2C7FA8')
        s.path('M160 205C150 260 140 330 130 400H0V330C60 300 120 260 160 205Z', '#1F6588')
        s.path('M240 205C250 260 260 330 270 400H400V330C340 300 280 260 240 205Z', '#1F6588')
        s.path('M160 205H240L270 400H130Z', '#E8D6B0')
        for i in range(6):
            y = 220 + i * 30
            s.line(162 - i * 4, y, 150 - i * 4, y + 10, '#BFE3F2', sw=3, op=.8)
            s.line(238 + i * 4, y, 250 + i * 4, y + 10, '#BFE3F2', sw=3, op=.8)
    elif kind == 'muhammad':
        g = madinah_scene()
        s.defs.extend(g.defs)
        s.add(f'<g transform="translate(-400,-120)">{"".join(g.body)}</g>')
    elif kind == 'yunus':
        _sky(s, '#D9ECF2', '#BFDDE8', '#A6CEDD')
        s.circle(90, 90, 36, '#FFF1C9')
        s.path('M0 210C60 196 120 222 200 206C280 190 340 214 400 202V400H0Z', '#2C7FA8')
        s.path('M90 290C110 250 200 240 260 262C300 276 320 300 340 290L360 270L356 310L372 330L340 326C320 330 290 336 240 334C160 330 100 320 90 290Z', '#1D4E68')
        s.circle(130, 280, 5, '#FFFFFF')
        s.path('M0 330C80 316 160 344 240 330C320 316 360 330 400 324V400H0Z', '#1F6588')
    elif kind == 'yusuf':
        _sky(s, '#1B2B44', '#2E4260', '#4A5F80')
        stars(s, 0, 0, 400, 220, 30, '#FFF6DE', seed=9)
        for i in range(11):
            a = math.radians(200 + i * 14)
            s.path(sparkle(200 + 150 * math.cos(a), 210 + 130 * math.sin(a), 9), '#F3DDA0')
        s.circle(130, 120, 24, '#F9D88A')
        s.path(crescent(270, 115, 22, .34, -30), '#F3EBD0')
        s.path(hill(0, 400, 400, [(0, 300), (200, 280), (400, 300)]), '#2B3A52')
        s.ellipse(200, 320, 50, 14, '#5B6B80')
        s.path('M150 320V350C150 360 250 360 250 350V320', '#4A586C')
    else:  # generic desert
        _sky(s, '#F5E7C8', '#EDD6A6', '#E4C584')
        s.path(hill(0, 400, 400, [(0, 260), (130, 230), (260, 262), (400, 240)]), '#D9B77A')
        s.path(hill(0, 400, 400, [(0, 300), (200, 280), (400, 300)]), '#C99E5E')
        palm(s, 300, 300, 160, '#7A5E26', '#3F6E4C', lean=-.1, seed=1)
        s.path(sparkle(110, 90, 18), '#FFFFFF')
    return s


def tasbih_ring(size=600):
    s = Svg(size, size)
    c = size / 2
    cy = c * .9
    R = size * .36
    n = 33
    s.circle(c, cy, R, None, stroke='#B8954F', sw=size * .004, op=.6)
    gap = .55
    for i in range(n):
        a = math.pi / 2 + gap / 2 + i * (2 * math.pi - gap) / (n - 1)
        x, y = c + R * math.cos(a), cy + R * math.sin(a)
        br = size * .031
        s.circle(x + br * .15, y + br * .22, br, '#000', op=.12)
        s.circle(x, y, br, s.rad([(0, '#79BE95'), (.55, '#1F6A4C'), (1, '#0B3A2B')], .38, .32, .7))
    by = cy + R
    gold = s.lin([(0, '#F0D48A'), (1, '#9C7A35')])
    s.circle(c, by, size * .04, s.rad([(0, '#FBE3A1'), (.6, '#C9A24E'), (1, '#7A5E26')], .4, .35, .7))
    s.path(f'M{f2(c - size * .022, by + size * .035)}H{f2(c + size * .022)}L{f2(c + size * .014, by + size * .085)}H{f2(c - size * .014)}Z', gold)
    s.line(c, by + size * .085, c, by + size * .11, '#B8954F', sw=size * .008)
    s.path(f'M{f2(c, by + size * .105)}L{f2(c - size * .05, by + size * .24)}Q{f2(c, by + size * .26, c + size * .05, by + size * .24)}Z', gold)
    for i in range(9):
        x = c - size * .045 + i * size * .01125
        s.line(c, by + size * .12, x, by + size * .245, '#7A5E26', sw=size * .002, op=.45)
    return s


def compass_dial(size=600):
    s = Svg(size, size)
    c = size / 2
    s.circle(c, c, c * .98, s.rad([(0, '#2E8060'), (.7, '#17533C'), (1, '#0B2E22')]))
    s.circle(c, c, c * .98, None, stroke='#CDAE6E', sw=size * .02)
    s.circle(c, c, c * .8, None, stroke='#CDAE6E', sw=size * .003, op=.6)
    for i in range(72):
        a = math.radians(i * 5)
        L = .08 if i % 18 == 0 else (.05 if i % 2 == 0 else .03)
        s.line(c + c * .9 * math.sin(a), c - c * .9 * math.cos(a), c + c * (.9 - L) * math.sin(a), c - c * (.9 - L) * math.cos(a),
               '#E8D5A6', sw=size * (.006 if i % 18 == 0 else .0025))
    pattern(s, c * .45, c * .45, c * 1.55, c * 1.55, c * .55, '#CDAE6E', size * .002, .15)
    return s


SCENES.update({
    'logo_mark': lambda: logo_mark(512),
    'app_icon': lambda: app_icon(1024),
    **{f'prophet_{k}': (lambda k=k: prophet_card(k)) for k in ('adam', 'nuh', 'ibrahim', 'musa', 'muhammad', 'yunus', 'yusuf', 'generic')},
    'tasbih_ring': tasbih_ring,
    'compass_dial': compass_dial,
})
