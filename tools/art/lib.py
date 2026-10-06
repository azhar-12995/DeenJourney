"""Tiny SVG builder + Islamic-architecture primitives shared by all Deen Journey art.

Everything is plain path/rect/circle/ellipse + linear/radial gradients so the same SVG
imports cleanly into Figma (createNodeFromSvg) and rasterises with resvg for the app.
"""
import math, random

# ---------------------------------------------------------------- palette
P = dict(
    g950='#06261C', g900='#0B2E22', g850='#0E3A2B', g800='#124432', g700='#17533C', g600='#1F6A4C',
    g500='#2E8060', g400='#5C9C7E', g300='#93BFA7', g200='#C6DDCD', g100='#E4EFE6', g50='#F1F6F0',
    gold800='#5E4719', gold700='#7A5E26', gold600='#9C7A35', gold500='#B8954F', gold400='#CDAE6E',
    gold300='#E0C893', gold200='#EEDDB8', gold100='#F6EDD8', gold50='#FBF6EA',
    cream='#FAF7EF', cream2='#F3EEE1', cream3='#E9E2CF', white='#FFFFFF', ink='#18211C',
    sand='#E8D6B0', sand2='#D9C08E', sand3='#C4A46B', stone='#EDE6D6', stone2='#DCD2BC',
    sky1='#F9EFD9', sky2='#F3E2BD', water='#4FA3C7', water2='#2C7FA8', water3='#BFE3F2',
    skin1='#F2C9A0', skin2='#D9A27A', skin3='#B97D57', skin4='#8A5A3C', blush='#EFA48F',
    rose='#D98E8E', rose2='#B96A6A', teal='#3C8C88', plum='#7D5A86', navy='#1D3557', brown='#7B5236', brown2='#A0703F',
)


def fmt(v):
    if isinstance(v, float):
        s = f'{v:.2f}'.rstrip('0').rstrip('.')
        return s if s not in ('-0', '') else '0'
    return str(v)


_SEQ = [0]


class Svg:
    def __init__(self, w, h, bg=None):
        self.w, self.h = w, h
        self.defs, self.body, self.n = [], [], 0
        _SEQ[0] += 1
        self.pfx = 'd%d' % _SEQ[0]
        if bg:
            self.rect(0, 0, w, h, bg)

    def uid(self, p='g'):
        self.n += 1
        return f'{self.pfx}{p}{self.n}'

    # gradients: stops = [(offset, color[, opacity])]
    def _stops(self, stops):
        out = []
        for st in stops:
            o, c = st[0], st[1]
            a = st[2] if len(st) > 2 else 1
            out.append(f'<stop offset="{fmt(float(o))}" stop-color="{c}"' + (f' stop-opacity="{fmt(float(a))}"' if a != 1 else '') + '/>')
        return ''.join(out)

    def lin(self, stops, x1=0, y1=0, x2=0, y2=1, user=False):
        gid = self.uid('l')
        u = ' gradientUnits="userSpaceOnUse"' if user else ''
        self.defs.append(f'<linearGradient id="{gid}" x1="{fmt(float(x1))}" y1="{fmt(float(y1))}" x2="{fmt(float(x2))}" y2="{fmt(float(y2))}"{u}>{self._stops(stops)}</linearGradient>')
        return f'url(#{gid})'

    def rad(self, stops, cx=.5, cy=.5, r=.5, fx=None, fy=None, user=False):
        gid = self.uid('r')
        u = ' gradientUnits="userSpaceOnUse"' if user else ''
        f = (f' fx="{fmt(float(fx))}" fy="{fmt(float(fy))}"' if fx is not None else '')
        self.defs.append(f'<radialGradient id="{gid}" cx="{fmt(float(cx))}" cy="{fmt(float(cy))}" r="{fmt(float(r))}"{f}{u}>{self._stops(stops)}</radialGradient>')
        return f'url(#{gid})'

    @staticmethod
    def _attrs(fill=None, stroke=None, sw=None, op=None, fop=None, cap=None, join=None, extra=''):
        a = [f'fill="{fill if fill else "none"}"']
        if stroke:
            a.append(f'stroke="{stroke}"')
            if sw is not None:
                a.append(f'stroke-width="{fmt(float(sw))}"')
            a.append(f'stroke-linecap="{cap or "round"}" stroke-linejoin="{join or "round"}"')
        if op is not None and op != 1:
            a.append(f'opacity="{fmt(float(op))}"')
        if fop is not None and fop != 1:
            a.append(f'fill-opacity="{fmt(float(fop))}"')
        return ' '.join(a) + ((' ' + extra) if extra else '')

    def add(self, s):
        self.body.append(s)
        return self

    def path(self, d, fill=None, **kw):
        return self.add(f'<path d="{d}" {self._attrs(fill, **kw)}/>')

    def rect(self, x, y, w, h, fill=None, rx=0, **kw):
        r = f' rx="{fmt(float(rx))}"' if rx else ''
        return self.add(f'<rect x="{fmt(float(x))}" y="{fmt(float(y))}" width="{fmt(float(w))}" height="{fmt(float(h))}"{r} {self._attrs(fill, **kw)}/>')

    def circle(self, cx, cy, r, fill=None, **kw):
        return self.add(f'<circle cx="{fmt(float(cx))}" cy="{fmt(float(cy))}" r="{fmt(float(r))}" {self._attrs(fill, **kw)}/>')

    def ellipse(self, cx, cy, rx, ry, fill=None, **kw):
        return self.add(f'<ellipse cx="{fmt(float(cx))}" cy="{fmt(float(cy))}" rx="{fmt(float(rx))}" ry="{fmt(float(ry))}" {self._attrs(fill, **kw)}/>')

    def line(self, x1, y1, x2, y2, stroke, sw=1, **kw):
        return self.path(f'M{fmt(float(x1))} {fmt(float(y1))}L{fmt(float(x2))} {fmt(float(y2))}', None, stroke=stroke, sw=sw, **kw)

    def group(self, inner, transform=None, op=None):
        t = f' transform="{transform}"' if transform else ''
        o = f' opacity="{fmt(float(op))}"' if op is not None and op != 1 else ''
        return self.add(f'<g{t}{o}>{"".join(inner.body)}</g>')

    def sub(self):
        """A child builder that shares this svg's <defs> (for groups)."""
        c = Svg(self.w, self.h)
        c.defs = self.defs
        return c

    def text(self):
        d = ''.join(self.defs)
        return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}" height="{self.h}" viewBox="0 0 {self.w} {self.h}">'
                + (f'<defs>{d}</defs>' if d else '') + ''.join(self.body) + '</svg>')


def f2(*nums):
    return ' '.join(fmt(round(float(n), 2)) for n in nums)


# ---------------------------------------------------------------- shapes (return path data)
def onion_dome(cx, by, w, h):
    """Mughal/Ottoman onion dome sitting on y=by, total height h (without finial)."""
    l, r = cx - w / 2, cx + w / 2
    return (f'M{f2(l, by)}'
            f'C{f2(l - .14 * w, by - .30 * h, l + .02 * w, by - .62 * h, cx - .16 * w, by - .80 * h)}'
            f'C{f2(cx - .07 * w, by - .88 * h, cx - .02 * w, by - .93 * h, cx, by - h)}'
            f'C{f2(cx + .02 * w, by - .93 * h, cx + .07 * w, by - .88 * h, cx + .16 * w, by - .80 * h)}'
            f'C{f2(r - .02 * w, by - .62 * h, r + .14 * w, by - .30 * h, r, by)}Z')


def round_dome(cx, by, w, h):
    """Shallow Ottoman dome (half ellipse) with a tiny lantern bump."""
    l, r = cx - w / 2, cx + w / 2
    return (f'M{f2(l, by)}C{f2(l, by - .75 * h, cx - .45 * w, by - h, cx, by - h)}'
            f'C{f2(cx + .45 * w, by - h, r, by - .75 * h, r, by)}Z')


def pointed_arch(x, by, w, h):
    """Pointed (ogee-ish) arch opening: x..x+w wide, from y=by up to by-h."""
    r = w / 2
    s = by - h + r * 1.05
    return (f'M{f2(x, by)}V{f2(s)}C{f2(x, s - r * .62, x + r * .42, by - h + r * .12, x + r, by - h)}'
            f'C{f2(x + w - r * .42, by - h + r * .12, x + w, s - r * .62, x + w, s)}V{f2(by)}Z')


def round_arch(x, by, w, h):
    r = w / 2
    return f'M{f2(x, by)}V{f2(by - h + r)}A{f2(r, r)} 0 0 1 {f2(x + w, by - h + r)}V{f2(by)}Z'


def crescent(cx, cy, R, thick=.32, rot=-25):
    """Crescent moon path. thick ~ fraction of R that stays lit at the widest point."""
    d = R * (thick + .25)
    r2 = R * .92
    x = (R * R - r2 * r2 + d * d) / (2 * d)
    y = math.sqrt(max(R * R - x * x, 0.01))
    a = math.radians(rot)

    def rp(px, py):
        return cx + px * math.cos(a) - py * math.sin(a), cy + px * math.sin(a) + py * math.cos(a)
    p1, p2 = rp(x, -y), rp(x, y)
    return (f'M{f2(*p1)}A{f2(R, R)} 0 1 0 {f2(*p2)}A{f2(r2, r2)} 0 0 1 {f2(*p1)}Z')


def sparkle(cx, cy, r, k=.18):
    """4-point star (twinkle)."""
    q = r * k
    return (f'M{f2(cx, cy - r)}Q{f2(cx + q, cy - q, cx + r, cy)}Q{f2(cx + q, cy + q, cx, cy + r)}'
            f'Q{f2(cx - q, cy + q, cx - r, cy)}Q{f2(cx - q, cy - q, cx, cy - r)}Z')


def star_poly(cx, cy, r_out, r_in, n=8, rot=0):
    pts = []
    for i in range(2 * n):
        rr = r_out if i % 2 == 0 else r_in
        a = math.radians(rot) + math.pi * i / n - math.pi / 2
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    return 'M' + 'L'.join(f2(*p) for p in pts) + 'Z'


def rub_el_hizb(cx, cy, r):
    """Two overlapping squares (8-point Islamic star outline as a single polygon)."""
    return star_poly(cx, cy, r, r * .76, 8, 22.5)


def blob(cx, cy, rx, ry, n=7, jitter=.18, seed=1):
    """Soft organic blob (bushes, clouds)."""
    rnd = random.Random(seed)
    pts = []
    for i in range(n):
        a = 2 * math.pi * i / n
        k = 1 + rnd.uniform(-jitter, jitter)
        pts.append((cx + rx * k * math.cos(a), cy + ry * k * math.sin(a)))
    d = ''
    for i in range(n):
        p0, p1, p2 = pts[i - 1], pts[i], pts[(i + 1) % n]
        m1 = ((p0[0] + p1[0]) / 2, (p0[1] + p1[1]) / 2)
        m2 = ((p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2)
        d += (f'M{f2(*m1)}' if i == 0 else '') + f'Q{f2(*p1, *m2)}'
    return d + 'Z'


def cloud(cx, cy, w):
    h = w * .32
    return (f'M{f2(cx - w / 2, cy)}C{f2(cx - w / 2, cy - h * .9, cx - w * .2, cy - h * 1.2, cx - w * .08, cy - h * .7)}'
            f'C{f2(cx, cy - h * 1.6, cx + w * .3, cy - h * 1.5, cx + w * .3, cy - h * .6)}'
            f'C{f2(cx + w * .45, cy - h * .9, cx + w / 2, cy - h * .3, cx + w / 2, cy)}Z')


def hill(x0, x1, base, peaks, seed=0):
    """Smooth ridge through (x, y) peaks list, closed down to base."""
    pts = [(x0, peaks[0][1])] + peaks + [(x1, peaks[-1][1])]
    d = f'M{f2(x0, base)}L{f2(*pts[0])}'
    for i in range(1, len(pts)):
        (ax, ay), (bx, by) = pts[i - 1], pts[i]
        mx = (ax + bx) / 2
        d += f'C{f2(mx, ay, mx, by, bx, by)}'
    return d + f'L{f2(x1, base)}Z'


# ---------------------------------------------------------------- composite elements
def minaret(s, x, by, w, h, fill, accent=None, cap='cone', finial=True):
    """Slender minaret centred on x. accent = balcony/detail colour."""
    accent = accent or fill
    sh = by - h * .86
    s.path(f'M{f2(x - w / 2, by)}L{f2(x - w * .42, sh)}H{f2(x + w * .42)}L{f2(x + w / 2, by)}Z', fill)
    for k, bw in ((.50, 1.75), (.78, 1.55)):
        yy = by - h * k
        s.path(f'M{f2(x - w * bw / 2, yy)}H{f2(x + w * bw / 2)}L{f2(x + w * .5, yy + w * .45)}H{f2(x - w * .5)}Z', accent)
        s.rect(x - w * bw / 2, yy - w * .22, w * bw, w * .22, accent)
    top = by - h * .93
    s.rect(x - w * .36, top, w * .72, sh - top + 1, fill)
    if cap == 'cone':
        s.path(f'M{f2(x - w * .48, top)}L{f2(x, by - h - w * .9)}L{f2(x + w * .48, top)}Z', accent)
        tip = by - h - w * .9
    else:
        s.path(onion_dome(x, top, w * 1.0, w * 1.35), accent)
        tip = top - w * 1.35
    if finial:
        s.line(x, tip, x, tip - w * .9, accent, sw=max(w * .12, .8))
        s.path(crescent(x, tip - w * 1.15, w * .32, .38, -90), accent)


def mosque(s, cx, by, k, body, dome=None, trim=None, window=None, minarets=True, side_domes=True, dome_kind='onion'):
    """A symmetric mosque: hall + drum + big dome + 2 side domes + 2 minarets. k = scale (1 ~ 300px wide)."""
    dome = dome or body
    trim = trim or dome
    hw, hh = 110 * k, 72 * k
    if minarets:
        minaret(s, cx - 140 * k, by, 16 * k, 250 * k, body, trim)
        minaret(s, cx + 140 * k, by, 16 * k, 250 * k, body, trim)
    # hall
    s.rect(cx - hw, by - hh, 2 * hw, hh, body)
    s.rect(cx - hw - 4 * k, by - hh - 6 * k, 2 * hw + 8 * k, 7 * k, trim)
    # crenellation
    n = 18
    for i in range(n):
        xx = cx - hw + (i + .5) * (2 * hw / n)
        s.path(f'M{f2(xx - 3.2 * k, by - hh - 6 * k)}L{f2(xx, by - hh - 12 * k)}L{f2(xx + 3.2 * k, by - hh - 6 * k)}Z', trim)
    if side_domes:
        for sx in (-1, 1):
            dx = cx + sx * 72 * k
            s.rect(dx - 20 * k, by - hh - 18 * k, 40 * k, 13 * k, body)
            s.path(onion_dome(dx, by - hh - 18 * k, 42 * k, 44 * k) if dome_kind == 'onion' else round_dome(dx, by - hh - 18 * k, 44 * k, 26 * k), dome)
            tip = by - hh - 18 * k - (44 * k if dome_kind == 'onion' else 26 * k)
            s.line(dx, tip, dx, tip - 9 * k, trim, sw=1.6 * k)
    # drum + main dome
    s.rect(cx - 48 * k, by - hh - 30 * k, 96 * k, 25 * k, body)
    s.rect(cx - 52 * k, by - hh - 33 * k, 104 * k, 5 * k, trim)
    if dome_kind == 'onion':
        s.path(onion_dome(cx, by - hh - 33 * k, 112 * k, 118 * k), dome)
        tip = by - hh - 33 * k - 118 * k
    else:
        s.path(round_dome(cx, by - hh - 33 * k, 116 * k, 70 * k), dome)
        tip = by - hh - 33 * k - 70 * k
    s.line(cx, tip, cx, tip - 16 * k, trim, sw=2.4 * k)
    s.path(crescent(cx, tip - 22 * k, 6 * k, .38, -90), trim)
    if window:
        # drum windows
        for i in range(5):
            s.path(pointed_arch(cx - 38 * k + i * 16.5 * k, by - hh - 9 * k, 8 * k, 14 * k), window)
        # grand portal + arcade
        s.path(pointed_arch(cx - 22 * k, by, 44 * k, 58 * k), window)
        for sx in (-1, 1):
            for i in range(3):
                xx = cx + sx * (36 * k + i * 24 * k) - (14 * k if sx < 0 else 0)
                s.path(pointed_arch(xx, by - 8 * k, 14 * k, 34 * k), window)


def palm(s, x, by, h, trunk, leaf, lean=.12, seed=3):
    """Date palm: curved trunk + fronds."""
    tx, ty = x + h * lean, by - h
    s.path(f'M{f2(x - h * .035, by)}Q{f2(x + h * lean * .2, by - h * .5, tx - h * .02, ty)}'
           f'L{f2(tx + h * .02, ty)}Q{f2(x + h * lean * .3 + h * .04, by - h * .5, x + h * .035, by)}Z', trunk)
    for i in range(7):
        y = by - h * (.1 + i * .12)
        s.line(x + h * lean * (i * .12) - h * .03, y, x + h * lean * (i * .12) + h * .03, y - h * .02, leaf, sw=h * .012, op=.35)
    rnd = random.Random(seed)
    for a in (-160, -130, -100, -70, -40, -15, -195):
        ang = math.radians(a + rnd.uniform(-6, 6))
        L = h * rnd.uniform(.42, .55)
        ex, ey = tx + L * math.cos(ang), ty + L * math.sin(ang) + L * .35
        mx, my = tx + L * .5 * math.cos(ang), ty + L * .5 * math.sin(ang) - L * .12
        nx, ny = -(ey - ty), ex - tx
        nl = math.hypot(nx, ny) or 1
        wv = h * .055
        s.path(f'M{f2(tx, ty)}Q{f2(mx + nx / nl * wv, my + ny / nl * wv, ex, ey)}Q{f2(mx - nx / nl * wv * .2, my - ny / nl * wv * .2, tx, ty)}Z', leaf)


def cypress(s, x, by, h, fill, w=None):
    w = w or h * .22
    s.path(f'M{f2(x, by - h)}C{f2(x + w * .65, by - h * .7, x + w * .55, by - h * .12, x + w * .12, by)}'
           f'H{f2(x - w * .12)}C{f2(x - w * .55, by - h * .12, x - w * .65, by - h * .7, x, by - h)}Z', fill)


def round_tree(s, x, by, h, trunk, leaf, leaf2=None, seed=5):
    s.rect(x - h * .04, by - h * .45, h * .08, h * .45, trunk)
    s.path(blob(x, by - h * .62, h * .33, h * .36, 8, .12, seed), leaf)
    if leaf2:
        s.path(blob(x - h * .08, by - h * .7, h * .18, h * .2, 7, .12, seed + 1), leaf2, op=.7)


def stars(s, x0, y0, x1, y1, n, color, seed=7, rmax=1.8):
    rnd = random.Random(seed)
    for _ in range(n):
        x, y = rnd.uniform(x0, x1), rnd.uniform(y0, y1)
        r = rnd.uniform(.5, rmax)
        if r > rmax * .8:
            s.path(sparkle(x, y, r * 2.6), color, op=rnd.uniform(.6, .95))
        else:
            s.circle(x, y, r, color, op=rnd.uniform(.35, .9))


def lantern(s, cx, ty, k, body, glass, glow=None):
    """Hanging Ramadan lantern (fanous), top at ty."""
    s.line(cx, ty - 60 * k, cx, ty, body, sw=1.6 * k)
    if glow:
        s.circle(cx, ty + 40 * k, 62 * k, s.rad([(0, glow, .6), (1, glow, 0)]))
    s.path(f'M{f2(cx - 7 * k, ty + 4 * k)}H{f2(cx + 7 * k)}L{f2(cx + 4 * k, ty)}H{f2(cx - 4 * k)}Z', body)
    s.path(f'M{f2(cx - 14 * k, ty + 16 * k)}L{f2(cx, ty + 3 * k)}L{f2(cx + 14 * k, ty + 16 * k)}Z', body)
    s.path(f'M{f2(cx - 16 * k, ty + 16 * k)}H{f2(cx + 16 * k)}L{f2(cx + 20 * k, ty + 52 * k)}L{f2(cx, ty + 66 * k)}L{f2(cx - 20 * k, ty + 52 * k)}Z', glass)
    s.path(f'M{f2(cx - 16 * k, ty + 16 * k)}H{f2(cx + 16 * k)}L{f2(cx + 20 * k, ty + 52 * k)}L{f2(cx, ty + 66 * k)}L{f2(cx - 20 * k, ty + 52 * k)}Z', None, stroke=body, sw=2 * k)
    s.line(cx, ty + 16 * k, cx, ty + 66 * k, body, sw=1.4 * k)
    s.line(cx - 18 * k, ty + 34 * k, cx + 18 * k, ty + 34 * k, body, sw=1.2 * k)
    s.path(f'M{f2(cx - 5 * k, ty + 66 * k)}H{f2(cx + 5 * k)}L{f2(cx, ty + 76 * k)}Z', body)


def geo_tile(s, x, y, size, color, sw=1, op=.25):
    """One cell of an 8-point star lattice (used for subtle backgrounds)."""
    c = size / 2
    s.path(rub_el_hizb(x + c, y + c, size * .42), None, stroke=color, sw=sw, op=op)
    s.path(f'M{f2(x, y + c)}L{f2(x + c * .16, y + c)}M{f2(x + size - c * .16, y + c)}L{f2(x + size, y + c)}'
           f'M{f2(x + c, y)}L{f2(x + c, y + c * .16)}M{f2(x + c, y + size - c * .16)}L{f2(x + c, y + size)}', None, stroke=color, sw=sw, op=op)


def pattern(s, x0, y0, x1, y1, size, color, sw=1, op=.2):
    y = y0
    while y < y1:
        x = x0
        while x < x1:
            geo_tile(s, x, y, size, color, sw, op)
            x += size
        y += size
