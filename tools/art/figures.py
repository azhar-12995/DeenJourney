"""Respectful pictogram figures (salah postures) and friendly family avatars.

Figures are faceless pictograms in a long thobe + kufi (privacy friendly, like signage).
Prophets are never depicted anywhere in the app.
"""
import math
from lib import *

# joint sets, side view facing right, origin = floor under the body, y negative = up
POSES = {
    # standing, hands folded below chest
    'qiyam': dict(head=(3, -158), neck=(1, -145), sh=(0, -137), hip=(0, -86), knee=(1, -46), ank=(0, -7), toe=(18, -3),
                  elb=(7, -108), wri=(17, -112), robe='stand'),
    'takbir': dict(head=(3, -158), neck=(1, -145), sh=(0, -137), hip=(0, -86), knee=(1, -46), ank=(0, -7), toe=(18, -3),
                   elb=(12, -122), wri=(12, -152), robe='stand', palm=True),
    'qawmah': dict(head=(3, -158), neck=(1, -145), sh=(0, -137), hip=(0, -86), knee=(1, -46), ank=(0, -7), toe=(18, -3),
                   elb=(3, -110), wri=(5, -86), robe='stand'),
    'ruku': dict(head=(74, -95), neck=(62, -93), sh=(52, -91), hip=(0, -87), knee=(2, -46), ank=(0, -7), toe=(18, -3),
                 elb=(34, -68), wri=(8, -52), robe='bow'),
    'sujood': dict(head=(66, -12), neck=(55, -20), sh=(44, -28), hip=(-6, -50), knee=(4, -7), ank=(-38, -6), toe=(-40, -1),
                   elb=(36, -16), wri=(58, -3), robe='prostrate'),
    'jalsa': dict(head=(-2, -100), neck=(-4, -88), sh=(-5, -80), hip=(-12, -27), knee=(30, -9), ank=(-16, -6), toe=(-30, -4),
                  elb=(0, -50), wri=(24, -32), robe='sit'),
    'tashahhud': dict(head=(-2, -100), neck=(-4, -88), sh=(-5, -80), hip=(-12, -27), knee=(30, -9), ank=(-16, -6), toe=(-30, -4),
                      elb=(0, -50), wri=(24, -32), robe='sit', finger=True),
    'salam': dict(head=(-2, -100), neck=(-4, -88), sh=(-5, -80), hip=(-12, -27), knee=(30, -9), ank=(-16, -6), toe=(-30, -4),
                  elb=(0, -50), wri=(24, -32), robe='sit', turn=True),
    'sit': dict(head=(-2, -100), neck=(-4, -88), sh=(-5, -80), hip=(-12, -27), knee=(30, -9), ank=(-16, -6), toe=(-30, -4),
                elb=(4, -52), wri=(26, -40), robe='sit'),
    'sit_child': dict(head=(-2, -100), neck=(-4, -88), sh=(-5, -80), hip=(-12, -27), knee=(30, -9), ank=(-16, -6), toe=(-30, -4),
                      elb=(8, -60), wri=(28, -72), robe='sit', palm=True),
    'dua': dict(head=(3, -158), neck=(1, -145), sh=(0, -137), hip=(0, -86), knee=(1, -46), ank=(0, -7), toe=(18, -3),
                elb=(14, -112), wri=(24, -124), robe='stand', palm=True),
}


def _palm(wri, elb, k, m):
    """Open palm (rounded paddle) continuing the forearm direction."""
    dx, dy = wri[0] - elb[0], wri[1] - elb[1]
    L = math.hypot(dx, dy) or 1
    ux, uy = dx / L, dy / L
    nx, ny = -uy, ux
    w, h = 6.2 * k, 13 * k
    bx, by_ = wri[0] - ux * 2 * k, wri[1] - uy * 2 * k
    tx, ty = wri[0] + ux * h, wri[1] + uy * h
    return (f'M{f2(bx + nx * w, by_ + ny * w)}L{f2(tx + nx * w * .8 - ux * w * .6, ty + ny * w * .8 - uy * w * .6)}'
            f'Q{f2(tx + ux * w * .2, ty + uy * w * .2, tx - nx * w * .8 - ux * w * .6, ty - ny * w * .8 - uy * w * .6)}'
            f'L{f2(bx - nx * w, by_ - ny * w)}Z')


def figure(s, pose, x, by, k, color, cap='#FFFFFF', mirror=False, mat=None, halo=None, arm_col=None):
    """Draw a pictogram figure. Returns nothing; draws into s."""
    J = POSES[pose]
    m = -1 if mirror else 1

    def P(name):
        px, py = J[name]
        return x + m * px * k, by + py * k
    if mat:
        s.path(f'M{f2(x - 60 * k, by + 1)}H{f2(x + 95 * k * m if m > 0 else x + 60 * k)}L{f2(x + 100 * k * m if m > 0 else x + 64 * k, by + 6 * k)}H{f2(x - 64 * k)}Z'
               if not mirror else f'M{f2(x - 95 * k, by + 1)}H{f2(x + 60 * k)}L{f2(x + 64 * k, by + 6 * k)}H{f2(x - 100 * k)}Z', mat)
    sw = lambda v: v * k
    hip, knee, ank, toe = P('hip'), P('knee'), P('ank'), P('toe')
    sh, neck, head, elb, wri = P('sh'), P('neck'), P('head'), P('elb'), P('wri')
    # legs + feet
    s.path(f'M{f2(*hip)}L{f2(*knee)}L{f2(*ank)}', None, stroke=color, sw=sw(19))
    s.path(f'M{f2(*ank)}L{f2(*toe)}', None, stroke=color, sw=sw(9))
    # torso
    s.path(f'M{f2(*hip)}L{f2(*sh)}', None, stroke=color, sw=sw(30))
    s.path(f'M{f2(*sh)}L{f2(*neck)}', None, stroke=color, sw=sw(11))
    # thobe skirt
    r = J['robe']
    if r == 'stand':
        s.path(f'M{f2(x - 15 * k * m, by - 130 * k)}L{f2(x + 15 * k * m, by - 130 * k)}L{f2(x + 19 * k * m, by - 16 * k)}'
               f'Q{f2(x, by - 12 * k, x - 19 * k * m, by - 16 * k)}Z', color)
    elif r == 'bow':
        s.path(f'M{f2(x - 15 * k * m, by - 98 * k)}L{f2(x + 18 * k * m, by - 82 * k)}L{f2(x + 18 * k * m, by - 16 * k)}'
               f'Q{f2(x, by - 12 * k, x - 18 * k * m, by - 16 * k)}Z', color)
    elif r == 'sit':
        s.path(f'M{f2(x - 26 * k * m, by - 38 * k)}L{f2(x + 34 * k * m, by - 22 * k)}L{f2(x + 40 * k * m, by - 4 * k)}H{f2(x - 34 * k * m)}Z', color)
    elif r == 'prostrate':
        s.path(f'M{f2(x - 22 * k * m, by - 58 * k)}L{f2(x + 14 * k * m, by - 44 * k)}L{f2(x + 14 * k * m, by - 3 * k)}H{f2(x - 44 * k * m)}Z', color)
    # arm: halo in the background colour first so it reads in front of the body
    arm = f'M{f2(*sh)}L{f2(*elb)}L{f2(*wri)}'
    if halo:
        s.path(arm, None, stroke=halo, sw=sw(16))
        if J.get('palm'):
            s.path(_palm(wri, elb, k, m), halo, stroke=halo, sw=sw(5))
    s.path(arm, None, stroke=arm_col or color, sw=sw(11))
    if J.get('palm'):
        s.path(_palm(wri, elb, k, m), arm_col or color)
    else:
        s.circle(*wri, sw(6), arm_col or color)
    if J.get('finger'):
        s.path(f'M{f2(*wri)}L{f2(wri[0] + m * 9 * k, wri[1] - 5 * k)}', None, stroke=halo or color, sw=sw(6.5))
        s.path(f'M{f2(*wri)}L{f2(wri[0] + m * 9 * k, wri[1] - 5 * k)}', None, stroke=arm_col or color, sw=sw(3.4))
    # head + kufi
    s.circle(*head, sw(12.5), color)
    if cap:
        a = {'ruku': 90, 'sujood': 115}.get(pose, 0)
        hx, hy = head
        R = 12.5 * k
        ang = math.radians(a) * m
        def rot(px, py):
            return hx + px * math.cos(ang) - py * math.sin(ang), hy + px * math.sin(ang) + py * math.cos(ang)
        p1, p2, c1 = rot(-R * 1.02, -R * .25), rot(R * 1.02, -R * .25), rot(0, -R * 1.55)
        s.path(f'M{f2(*p1)}Q{f2(*c1, *p2)}Z', cap)
    if J.get('turn'):
        hx, hy = head
        s.path(f'M{f2(hx + 18 * k, hy - 22 * k)}Q{f2(hx + 34 * k, hy - 6 * k, hx + 24 * k, hy + 12 * k)}', None, stroke='#B8954F', sw=sw(3))
        s.path(f'M{f2(hx + 24 * k, hy + 12 * k)}l{f2(-7 * k, -2 * k)}m{f2(7 * k, 2 * k)}l{f2(2 * k, -7 * k)}', None, stroke='#B8954F', sw=sw(3))


def posture_card(pose, color='#2F6B4F'):
    s = Svg(240, 240)
    s.circle(120, 120, 112, '#F3EEE1')
    s.ellipse(120, 196, 90, 9, '#000', op=.06)
    k = .95 if pose in ('qiyam', 'takbir', 'qawmah', 'dua') else 1.15
    cx = {'ruku': 92, 'sujood': 112, 'jalsa': 128, 'tashahhud': 128, 'salam': 120}.get(pose, 118)
    figure(s, pose, cx, 194, k, color, mat='#CDAE6E', halo='#F3EEE1', arm_col='#3E8462')
    return s


# ---------------------------------------------------------------- avatars
SK = dict(light='#F2C9A0', medium='#D9A27A', tan='#B97D57', deep='#8A5A3C')


def _face(s, cx, cy, r, skin, eyes='#2B2116', smile=True, cheeks=True, old=False):
    s.circle(cx, cy, r, skin)
    ey = cy + r * .02
    for sx in (-1, 1):
        s.ellipse(cx + sx * r * .36, ey, r * .085, r * .11, eyes)
        s.circle(cx + sx * r * .36 + r * .03, ey - r * .04, r * .03, '#FFFFFF')
        s.path(f'M{f2(cx + sx * r * .5, ey - r * .25)}Q{f2(cx + sx * r * .36, ey - r * .34, cx + sx * r * .22, ey - r * .26)}', None,
               stroke='#E8E2D6' if old else eyes, sw=r * .06, op=.8)
        if cheeks:
            s.circle(cx + sx * r * .55, cy + r * .3, r * .13, '#EF8F7A', op=.35)
    if smile:
        s.path(f'M{f2(cx - r * .22, cy + r * .36)}Q{f2(cx, cy + r * .56, cx + r * .22, cy + r * .36)}', None, stroke='#8A3B2E', sw=r * .07)
    if old:
        for sx in (-1, 1):
            s.circle(cx + sx * r * .36, ey, r * .2, None, stroke='#5B4A3A', sw=r * .05)
        s.line(cx - r * .16, ey, cx + r * .16, ey, '#5B4A3A', sw=r * .05)


def avatar(kind, bg='#E4EFE6'):
    s = Svg(200, 200)
    s.circle(100, 100, 100, bg)
    if kind in ('boy', 'man', 'grandpa'):
        shirt = {'boy': '#2E8060', 'man': '#17533C', 'grandpa': '#7A5E26'}[kind]
        skin = {'boy': SK['light'], 'man': SK['medium'], 'grandpa': SK['light']}[kind]
        r = 40 if kind == 'boy' else 42
        cy = 92 if kind == 'boy' else 88
        s.path('M28 200C30 160 64 140 100 140C136 140 170 160 172 200Z', shirt)
        s.path('M86 150L100 170L114 150', None, stroke='#FFFFFF', sw=5, op=.6)
        s.rect(88, cy + r - 8, 24, 22, skin)
        s.circle(100 - r * .98, cy + 4, r * .2, skin)
        s.circle(100 + r * .98, cy + 4, r * .2, skin)
        _face(s, 100, cy, r, skin, old=(kind == 'grandpa'), cheeks=(kind == 'boy'))
        if kind == 'boy':
            s.path(f'M{f2(100 - r * 1.02, cy - r * .1)}C{f2(100 - r * 1.05, cy - r * 1.25, 100 + r * 1.05, cy - r * 1.25, 100 + r * 1.02, cy - r * .1)}'
                   f'C{f2(100 + r * .7, cy - r * .62, 100 + r * .1, cy - r * .55, 100 - r * .15, cy - r * .78)}C{f2(100 - r * .4, cy - r * .5, 100 - r * .8, cy - r * .55, 100 - r * 1.02, cy - r * .1)}Z', '#2B2116')
        else:
            beard = '#2B2116' if kind == 'man' else '#F1EFEA'
            s.path(f'M{f2(100 - r * .98, cy - r * .05)}C{f2(100 - r * .96, cy + r * .9, 100 - r * .4, cy + r * 1.32, 100, cy + r * 1.34)}'
                   f'C{f2(100 + r * .4, cy + r * 1.32, 100 + r * .96, cy + r * .9, 100 + r * .98, cy - r * .05)}'
                   f'C{f2(100 + r * .82, cy + r * .35, 100 + r * .5, cy + r * .42, 100 + r * .28, cy + r * .3)}'
                   f'Q{f2(100, cy + r * .22, 100 - r * .28, cy + r * .3)}C{f2(100 - r * .5, cy + r * .42, 100 - r * .82, cy + r * .35, 100 - r * .98, cy - r * .05)}Z', beard)
            s.path(f'M{f2(100 - r * .2, cy + r * .5)}Q{f2(100, cy + r * .64, 100 + r * .2, cy + r * .5)}', None, stroke='#8A3B2E', sw=r * .07)
            cap = '#FFFFFF' if kind == 'man' else '#EDE6D6'
            s.path(f'M{f2(100 - r * 1.0, cy - r * .42)}C{f2(100 - r * .95, cy - r * 1.22, 100 + r * .95, cy - r * 1.22, 100 + r * 1.0, cy - r * .42)}Z', cap)
            s.path(f'M{f2(100 - r * 1.0, cy - r * .42)}H{f2(100 + r * 1.0)}', None, stroke='#D9D2C3', sw=3)
            for i in range(5):
                s.circle(100 - r * .6 + i * r * .3, cy - r * .75, 1.8, '#D9D2C3')
    else:
        scarf = {'girl': '#E59A9A', 'woman': '#3C8C88', 'grandma': '#D9CBB0'}[kind]
        scarf2 = {'girl': '#C97C7C', 'woman': '#2E6E6B', 'grandma': '#BFAE8D'}[kind]
        skin = {'girl': SK['light'], 'woman': SK['medium'], 'grandma': SK['light']}[kind]
        r = 36 if kind == 'girl' else 38
        cy = 96
        s.path('M24 200C26 158 60 136 100 136C140 136 174 158 176 200Z', scarf2)
        s.path(f'M100 {cy - r - 22}C46 {cy - r - 22} 38 {cy + 10} 44 {cy + 40}C52 {cy + 76} 80 {cy + 86} 100 {cy + 88}'
               f'C120 {cy + 86} 148 {cy + 76} 156 {cy + 40}C162 {cy + 10} 154 {cy - r - 22} 100 {cy - r - 22}Z', scarf)
        _face(s, 100, cy + 2, r, skin, old=(kind == 'grandma'), cheeks=(kind != 'grandma'))
        s.path(f'M{f2(100 - r * 1.08, cy + 6)}C{f2(100 - r * 1.1, cy - r * 1.1, 100 + r * 1.1, cy - r * 1.1, 100 + r * 1.08, cy + 6)}'
               f'C{f2(100 + r * .9, cy - r * .62, 100 - r * .9, cy - r * .62, 100 - r * 1.08, cy + 6)}Z', scarf)
        s.path(f'M{f2(100 - r * .95, cy - r * .55)}C{f2(100 - r * .6, cy - r * .78, 100 + r * .6, cy - r * .78, 100 + r * .95, cy - r * .55)}', None, stroke=scarf2, sw=3, op=.6)
    return s


FIGS = {
    **{f'pose_{p}': (lambda p=p: posture_card(p)) for p in ('takbir', 'qiyam', 'ruku', 'qawmah', 'sujood', 'jalsa', 'tashahhud', 'salam', 'dua')},
    **{f'avatar_{a}': (lambda a=a: avatar(a, {'boy': '#E4EFE6', 'girl': '#F9E3E0', 'man': '#E7EEE4', 'woman': '#E2F0EE', 'grandpa': '#F6EDD8', 'grandma': '#F3EEE1'}[a]))
       for a in ('boy', 'girl', 'man', 'woman', 'grandpa', 'grandma')},
}
