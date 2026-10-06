"""Duotone feature glyphs (48x48): stroke colour '#S' + soft accent fill '#A'.

The placeholders are swapped at export time (gold on cream tiles by default, green variant,
or Compose parameters), so one drawing serves every theme.
"""
import math
from lib import Svg, f2, crescent, sparkle, rub_el_hizb, star_poly, pointed_arch, onion_dome

S, A = '#S', '#A'
SW = 2.6


def G():
    return Svg(48, 48)


def st(s, d, w=SW):
    s.path(d, None, stroke=S, sw=w)


def fl(s, d, op=1):
    s.path(d, A, op=op)


def flst(s, d, w=SW):
    fl(s, d)
    st(s, d, w)


def sfill(s, d, op=1):
    s.path(d, S, op=op)


def rell(cx, cy, rx, ry, deg):
    """Rotated ellipse as path (4 cubic arcs)."""
    k = .5523
    a = math.radians(deg)
    ca, sa = math.cos(a), math.sin(a)

    def P(x, y):
        return cx + x * ca - y * sa, cy + x * sa + y * ca
    pts = [P(rx, 0), P(rx, ry * k), P(rx * k, ry), P(0, ry), P(-rx * k, ry), P(-rx, ry * k), P(-rx, 0),
           P(-rx, -ry * k), P(-rx * k, -ry), P(0, -ry), P(rx * k, -ry), P(rx, -ry * k), P(rx, 0)]
    d = f'M{f2(*pts[0])}'
    for i in range(1, 13, 3):
        d += f'C{f2(*pts[i], *pts[i + 1], *pts[i + 2])}'
    return d + 'Z'


def circ(cx, cy, r):
    return f'M{f2(cx - r, cy)}A{f2(r, r)} 0 1 0 {f2(cx + r, cy)}A{f2(r, r)} 0 1 0 {f2(cx - r, cy)}Z'


def rrect(x, y, w, h, r):
    return (f'M{f2(x + r, y)}H{f2(x + w - r)}Q{f2(x + w, y, x + w, y + r)}V{f2(y + h - r)}Q{f2(x + w, y + h, x + w - r, y + h)}'
            f'H{f2(x + r)}Q{f2(x, y + h, x, y + h - r)}V{f2(y + r)}Q{f2(x, y, x + r, y)}Z')


# ---------------------------------------------------------------- shared parts
def hand(s, mirror=False, ox=0, oy=0, k=1.0):
    """Open palm facing the viewer, fingers up, thumb toward the centre (left hand by default)."""
    m = -1 if mirror else 1
    cx = 24

    def X(x):
        return cx + (x - cx) * m * k + ox

    def Y(y):
        return 24 + (y - 24) * k + oy
    d = (f'M{f2(X(11), Y(43))}C{f2(X(10.5), Y(37), X(7), Y(31), X(7), Y(24))}V{f2(Y(13))}'
         f'C{f2(X(7), Y(10.5), X(11), Y(10.5), X(11), Y(13))}V{f2(Y(22))}'
         f'V{f2(Y(9))}C{f2(X(11), Y(6.4), X(15), Y(6.4), X(15), Y(9))}V{f2(Y(21))}'
         f'V{f2(Y(10))}C{f2(X(15), Y(7.4), X(19), Y(7.4), X(19), Y(10))}V{f2(Y(26))}'
         f'C{f2(X(20.5), Y(24), X(23.5), Y(23.5), X(23.5), Y(26.5))}C{f2(X(23.5), Y(30), X(20), Y(33), X(20), Y(37))}V{f2(Y(43))}')
    fl(s, d + 'Z')
    st(s, d)


def kaaba(s, x=8, y=10, w=32, h=32):
    k = w / 32
    def P(px, py):
        return x + px * k, y + py * k
    sfill(s, f'M{f2(*P(0, 6))}L{f2(*P(16, 11))}V{f2(P(0, 32)[1])}L{f2(*P(0, 27))}Z', .92)
    sfill(s, f'M{f2(*P(16, 11))}L{f2(*P(32, 6))}V{f2(P(0, 27)[1])}L{f2(*P(16, 32))}Z', .78)
    fl(s, f'M{f2(*P(0, 6))}L{f2(*P(16, 1))}L{f2(*P(32, 6))}L{f2(*P(16, 11))}Z')
    st(s, f'M{f2(*P(0, 6))}L{f2(*P(16, 1))}L{f2(*P(32, 6))}L{f2(*P(16, 11))}Z', 1.6)
    s.path(f'M{f2(*P(0, 11))}L{f2(*P(16, 16))}L{f2(*P(32, 11))}', None, stroke=A, sw=2.8 * k)
    s.path(f'M{f2(*P(21, 18.5))}L{f2(*P(26, 17))}V{f2(P(0, 25)[1])}L{f2(*P(21, 26.5))}Z', A)


def mosque_small(s, x=24, by=41, k=1.0):
    d = onion_dome(x, by - 14 * k, 18 * k, 15 * k)
    fl(s, d)
    st(s, d)
    st(s, f'M{f2(x - 13 * k, by)}V{f2(by - 14 * k)}H{f2(x + 13 * k)}V{f2(by)}')
    st(s, f'M{f2(x, by - 29 * k)}V{f2(by - 33 * k)}')
    st(s, f'M{f2(x - 4 * k, by)}V{f2(by - 6 * k)}A{f2(4 * k, 4 * k)} 0 0 1 {f2(x + 4 * k, by - 6 * k)}V{f2(by)}')
    for sx in (-1, 1):
        mx = x + sx * 18 * k
        st(s, f'M{f2(mx, by)}V{f2(by - 22 * k)}')
        st(s, f'M{f2(mx - 2.5 * k, by - 22 * k)}L{f2(mx, by - 27 * k)}L{f2(mx + 2.5 * k, by - 22 * k)}')
    st(s, f'M{f2(x - 22 * k, by)}H{f2(x + 22 * k)}')


def beads_ring(s, cx, cy, r, n=12, br=2.6):
    for i in range(n):
        a = math.pi * 2 * i / n - math.pi / 2
        if abs(a - math.pi / 2) < .3:
            continue
        px, py = cx + r * math.cos(a), cy + r * math.sin(a)
        flst(s, circ(px, py, br), 1.6)


# ---------------------------------------------------------------- glyphs
def g_sehri():
    s = G()
    flst(s, 'M8 25H40C40 33.5 33 39 24 39C15 39 8 33.5 8 25Z')
    st(s, 'M17 43H31')
    for x in (18, 24, 30):
        st(s, f'M{x} 20C{x - 2} 17.5 {x + 2} 15.5 {x} 13', 2.2)
    flst(s, crescent(38, 9, 5, .36, -30), 1.8)
    return s


def g_iftar():
    s = G()
    flst(s, 'M27 13H40L37.5 41H29.5Z')
    fl(s, 'M28.3 25H38.7L37.5 41H29.5Z')
    st(s, 'M28.3 25H38.7', 2)
    flst(s, rell(13, 31, 4.6, 8.5, -28))
    flst(s, rell(21, 34, 4.2, 7.8, 18))
    st(s, 'M6 43H42')
    st(s, 'M12 24C11 21 12 19 14 18', 2)
    return s


def g_quran():
    s = G()
    d = 'M24 13C19.5 10 13 9.5 7 11.5V32C13 30 19.5 30.5 24 33.5C28.5 30.5 35 30 41 32V11.5C35 9.5 28.5 10 24 13Z'
    flst(s, d)
    st(s, 'M24 13V33.5')
    for i in range(3):
        st(s, f'M{11} {16 + i * 4.5}C{15} {15 + i * 4.5} {18} {15.4 + i * 4.5} {20.5} {17 + i * 4.5}', 1.6)
        st(s, f'M{27.5} {17 + i * 4.5}C{30} {15.4 + i * 4.5} {33} {15 + i * 4.5} {37} {16 + i * 4.5}', 1.6)
    st(s, 'M11 44L37 34M37 44L11 34')
    return s


def g_book_ribbon():
    s = G()
    d = 'M24 12C19.5 9 13 8.5 7 10.5V36C13 34 19.5 34.5 24 37.5C28.5 34.5 35 34 41 36V10.5C35 8.5 28.5 9 24 12Z'
    flst(s, d)
    st(s, 'M24 12V37.5')
    sfill(s, 'M30 9.6V22L33 19.6L36 22V9.2C34 8.9 32 9 30 9.6Z')
    return s


def g_fasting():
    s = G()
    flst(s, 'M11 6H29L37 14V42H11Z')
    st(s, 'M29 6V14H37')
    flst(s, crescent(22, 21, 6.5, .34, -35), 1.8)
    st(s, 'M16 32H32M16 37H27')
    return s


def g_dua():
    s = G()
    hand(s, False, -2.2, 0, .95)
    hand(s, True, 2.2, 0, .95)
    s.path(sparkle(24, 6.5, 3.6), S)
    return s


def g_tracker():
    s = G()
    for i, (x, h) in enumerate(((9, 12), (19, 19), (29, 26))):
        flst(s, rrect(x, 42 - h, 8, h, 2))
    st(s, 'M6 43H42')
    s.path(sparkle(39, 9, 5.5), S)
    st(s, 'M10 21L20 15L27 18L36 9', 2)
    return s


def g_zakat():
    s = G()
    for i in range(3):
        y = 38 - i * 6
        flst(s, rell(17, y, 11, 3.6, 0))
        st(s, f'M6 {y}V{y + 3}C6 {y + 5} 28 {y + 5} 28 {y + 3}V{y}', 2.2)
    flst(s, circ(34, 20, 9))
    st(s, circ(34, 20, 5.6), 1.6)
    s.path(crescent(34, 20, 3.4, .34, -30), S)
    return s


def g_kaaba():
    s = G()
    kaaba(s, 8, 6, 32, 34)
    st(s, 'M5 44H43', 2)
    return s


def g_calendar():
    s = G()
    flst(s, rrect(7, 10, 34, 32, 5))
    st(s, 'M7 19H41')
    st(s, 'M16 6V13M32 6V13')
    s.path(crescent(24, 30, 7, .34, -35), S)
    s.path(sparkle(32, 26, 2.6), S)
    return s


def g_mat():
    s = G()
    flst(s, rrect(11, 5, 26, 34, 2.5))
    st(s, pointed_arch(16.5, 32, 15, 21), 2)
    s.path(rub_el_hizb(24, 24, 3.6), S)
    for x in range(13, 37, 4):
        st(s, f'M{x} 39V44', 1.8)
    return s


def g_prayer_time():
    s = G()
    flst(s, circ(22, 26, 15))
    st(s, 'M22 17V26L28 30')
    s.path(crescent(38, 9, 6, .34, -30), S)
    return s


def g_qibla():
    s = G()
    flst(s, circ(24, 26, 17))
    st(s, circ(24, 26, 12.5), 1.4)
    sfill(s, 'M24 13L28 26L24 39L20 26Z', .25)
    sfill(s, 'M24 13L28 26H20Z')
    st(s, 'M24 13L28 26L24 39L20 26Z', 1.8)
    kaaba(s, 19.5, 1, 9, 9)
    return s


def g_wudu():
    s = G()
    flst(s, 'M24 7C24 7 12 21 12 29A12 12 0 0 0 36 29C36 21 24 7 24 7Z')
    st(s, 'M18.5 30A6 6 0 0 0 24 35.5', 2.2)
    flst(s, 'M38 8C38 8 34 13 34 15.5A4 4 0 0 0 42 15.5C42 13 38 8 38 8Z', 1.8)
    return s


def g_ghusl():
    s = G()
    st(s, 'M10 6V12H22')
    flst(s, 'M18 12H34L36 18H16Z')
    for i, x in enumerate((19, 25, 31)):
        for j in range(3):
            y = 24 + j * 7 + (i % 2) * 3
            flst(s, f'M{x} {y}C{x} {y} {x - 2.2} {y + 3} {x - 2.2} {y + 4.2}A2.2 2.2 0 0 0 {x + 2.2} {y + 4.2}C{x + 2.2} {y + 3} {x} {y} {x} {y}Z', 1.4)
    return s


def g_salah():
    s = G()
    fl(s, 'M4 42H44V45H4Z')
    st(s, 'M4 42H44', 2)
    flst(s, circ(36, 13, 4.5), 2.2)
    st(s, 'M31 16L14 19', 6)
    st(s, 'M14 19L15 30L14 41', 5)
    st(s, 'M14 41H21', 3)
    st(s, 'M28 17L26 28', 3.2)
    return s


def g_kalima():
    s = G()
    flst(s, 'M12 9H34C37 9 39 11 39 14V38C39 41 37 42 34 42H14')
    flst(s, 'M12 9C9 9 8 11 8 13C8 15 9 17 12 17H16V13C16 11 14.5 9 12 9Z')
    st(s, 'M16 13V37C16 40 14 42 12 42C9 42 8 40 8 38H34')
    st(s, 'M21 20C24 18 27 22 30 20C32 19 33 19 34 20', 2)
    st(s, 'M21 27H34M21 32H30', 2)
    return s


def g_adhkar():
    s = G()
    fl(s, 'M24 8A16 16 0 0 1 24 40Z')
    st(s, circ(24, 24, 16))
    st(s, 'M24 8V40', 1.6)
    s.path(crescent(16, 22, 5.4, .34, -30), S)
    for a in range(0, 360, 60):
        r = math.radians(a)
        st(s, f'M{f2(32 + 4.5 * math.cos(r), 24 + 4.5 * math.sin(r))}L{f2(32 + 6.2 * math.cos(r), 24 + 6.2 * math.sin(r))}', 1.6)
    s.circle(32, 24, 2.6, S)
    return s


def g_tasbih():
    s = G()
    beads_ring(s, 24, 20, 13, 14, 2.6)
    flst(s, 'M21.5 33H26.5L25.5 37H22.5Z', 1.6)
    st(s, 'M24 37V39', 1.6)
    flst(s, 'M24 39L19.5 46H28.5Z', 1.6)
    return s


def g_hadith():
    s = G()
    flst(s, rrect(9, 8, 24, 34, 3))
    st(s, 'M14 8V42', 1.8)
    st(s, 'M19 17H28M19 23H28M19 29H25', 2)
    fl(s, 'M44 6C36 8 31 16 29 26L31 27C36 22 41 15 44 6Z')
    st(s, 'M44 6C36 8 31 16 29 26M29 26L27 31', 2)
    return s


def g_akhlaq():
    s = G()
    flst(s, 'M24 22C24 22 15 16 15 11C15 8 17 6 19.5 6C21.5 6 23 7.5 24 9C25 7.5 26.5 6 28.5 6C31 6 33 8 33 11C33 16 24 22 24 22Z')
    d = 'M5 31L11 30C14 29.5 16 30 18 31L25 34C27 35 27 38 24.5 38H18M5 42L14 40C16 39.6 18 40 20 40.6L28 42C30 42.6 32 42 34 41L42 35C44 33.5 42 31 40 32L31 37'
    st(s, d)
    return s


def g_prophets():
    s = G()
    flst(s, 'M3 40L17 18L25 30L31 22L45 40Z')
    st(s, 'M17 18L21 24L19 26', 1.8)
    s.path(sparkle(34, 9, 5), S)
    st(s, 'M20 40C22 36 26 36 25 33', 1.8)
    return s


def g_names():
    s = G()
    d = rub_el_hizb(24, 24, 18)
    flst(s, d)
    st(s, circ(24, 24, 8), 2)
    s.path(sparkle(24, 24, 4.2), S)
    return s


def g_lesson():
    s = G()
    flst(s, 'M24 10L44 19L24 28L4 19Z')
    st(s, 'M12 23V32C12 32 17 37 24 37C31 37 36 32 36 32V23')
    st(s, 'M44 19V30')
    s.circle(44, 32, 2.4, S)
    return s


def g_progress():
    s = G()
    st(s, circ(24, 24, 16), 4)
    s.path('M24 8A16 16 0 1 1 9.3 30.6', None, stroke=A, sw=4.2)
    s.path('M24 8A16 16 0 1 1 9.3 30.6', None, stroke=S, sw=1.6)
    st(s, 'M18 25L22.5 29.5L31 20')
    return s


def g_roadmap():
    s = G()
    fl(s, 'M6 44C14 36 30 38 26 30C22 22 34 22 38 16H42C38 24 27 24 31 31C35 39 18 40 13 44Z')
    st(s, 'M8 44C16 36 30 38 26 30C22 22 34 22 40 16')
    st(s, 'M40 16V4')
    flst(s, 'M40 5H32L34.5 8.5L32 12H40', 1.8)
    s.circle(9, 43, 2.6, S)
    return s


def g_pillars():
    s = G()
    flst(s, 'M6 14L24 5L42 14Z')
    for i in range(5):
        x = 9 + i * 7.5
        flst(s, rrect(x - 2, 17, 4, 21, 1.2), 1.8)
    st(s, 'M5 41H43M7 38H41', 2)
    return s


def g_shahadah():
    s = G()
    flst(s, crescent(22, 25, 16, .32, -40))
    s.path(star_poly(35, 15, 6.5, 2.8, 5, 0), S)
    return s


def g_sawm():
    s = G()
    fl(s, 'M4 34H44V40H4Z', .6)
    flst(s, 'M12 34A12 12 0 0 1 36 34Z')
    st(s, 'M4 34H44')
    for a in (200, 235, 270, 305, 340):
        r = math.radians(a)
        st(s, f'M{f2(24 + 15 * math.cos(r), 34 + 15 * math.sin(r))}L{f2(24 + 19 * math.cos(r), 34 + 19 * math.sin(r))}', 2)
    st(s, 'M24 40V46M20 43L24 46L28 43', 2)
    return s


def g_child():
    s = G()
    flst(s, circ(24, 25, 15))
    sfill(s, 'M10 21C11 13 17 9 24 9C31 9 37 13 38 21C34 18 30 17 27 14C24 17 16 19 10 21Z')
    s.circle(19, 26, 1.8, S)
    s.circle(29, 26, 1.8, S)
    st(s, 'M19.5 31.5C21 33.5 27 33.5 28.5 31.5', 2)
    return s


def g_parent():
    s = G()
    flst(s, 'M24 4L40 10V22C40 32 33 40 24 44C15 40 8 32 8 22V10Z')
    st(s, circ(20, 20, 4), 2)
    st(s, circ(29.5, 23, 3), 2)
    st(s, 'M13.5 33C14 28 17 26.5 20 26.5C23 26.5 25.5 28 26 31M26 31C26.5 29.5 28 28.5 29.5 28.5C32 28.5 34 30 34.5 33', 2)
    return s


def g_family():
    s = G()
    flst(s, circ(15, 15, 5.5))
    flst(s, circ(33, 15, 5.5))
    flst(s, circ(24, 27, 4.2), 2)
    st(s, 'M5 38C5 30 9 25 15 25C18 25 20 26 21 27.5M43 38C43 30 39 25 33 25C30 25 28 26 27 27.5M16 43C16 37 19.5 34 24 34C28.5 34 32 37 32 43')
    return s


def g_qaida():
    s = G()
    flst(s, rrect(6, 6, 36, 36, 8))
    st(s, 'M33.5 17.5C35 22 34.5 28.5 27 28.5H17C12.5 28.5 11.5 25.5 12.5 21', 3.2)
    s.circle(23, 35, 2.6, S)
    return s


def g_hifz():
    s = G()
    d = 'M24 14C20 11.5 15 11 10 12.5V32C15 31 20 31.5 24 34C28 31.5 33 31 38 32V12.5C33 11 28 11.5 24 14Z'
    flst(s, d)
    st(s, 'M24 14V34')
    st(s, 'M8 40C12 45 36 45 40 40', 2)
    st(s, 'M36 36L40 40L36 43', 2)
    return s


def g_tafsir():
    s = G()
    d = 'M22 12C18 9.5 12.5 9 7 10.5V33C12.5 32 18 32.5 22 35V12Z'
    flst(s, d)
    st(s, 'M22 12C26 9.5 31 9 36 10.5V20')
    flst(s, circ(33, 31, 7.5))
    st(s, 'M38.5 36.5L44 42', 3.2)
    return s


def g_bookmark():
    s = G()
    flst(s, 'M13 6H35V42L24 34L13 42Z')
    s.path(sparkle(24, 19, 5), S)
    return s


def g_note():
    s = G()
    flst(s, rrect(8, 8, 28, 34, 4))
    st(s, 'M14 17H30M14 23H30M14 29H22', 2)
    fl(s, 'M41 15L44 18L29 33L25 34L26 30Z')
    st(s, 'M41 15L44 18L29 33L25 34L26 30Z', 2)
    return s


def g_headphones():
    s = G()
    st(s, 'M8 30V24A16 16 0 0 1 40 24V30')
    flst(s, rrect(6, 28, 9, 14, 3.5))
    flst(s, rrect(33, 28, 9, 14, 3.5))
    return s


def g_mic():
    s = G()
    flst(s, rrect(17, 5, 14, 24, 7))
    st(s, 'M11 22A13 13 0 0 0 37 22')
    st(s, 'M24 35V43M17 43H31')
    return s


def g_telescope():
    s = G()
    flst(s, 'M8 26L32 12L35 18L11 32Z')
    st(s, 'M18 28L14 44M22 26L28 44')
    s.path(crescent(38, 32, 6, .34, -20), S)
    return s


def g_star():
    s = G()
    flst(s, star_poly(24, 25, 18, 8, 5, 0))
    return s


def g_leaf():
    s = G()
    st(s, 'M10 42C18 34 26 24 38 8', 2.4)
    for i, t in enumerate((.25, .45, .65)):
        x, y = 10 + 28 * t, 42 - 34 * t
        flst(s, rell(x - 5, y - 2, 6, 3, -60), 1.8)
        flst(s, rell(x + 4, y + 4, 6, 3, 20), 1.8)
    return s


def g_eid():
    s = G()
    flst(s, crescent(19, 22, 14, .32, -35))
    st(s, 'M35 4V12')
    flst(s, 'M31 15L35 12L39 15V27L35 31L31 27Z', 2)
    st(s, 'M31 15H39', 1.6)
    s.path(sparkle(28, 36, 3.6), S)
    s.path(sparkle(40, 38, 2.6), S)
    return s


def g_mosque():
    s = G()
    mosque_small(s, 24, 42, 1.0)
    return s


def g_heart():
    s = G()
    flst(s, 'M24 41C24 41 7 31 7 18C7 12 11 8 16.5 8C20 8 22.5 10 24 12.5C25.5 10 28 8 31.5 8C37 8 41 12 41 18C41 31 24 41 24 41Z')
    s.path(sparkle(35, 15, 4), S)
    return s


def g_sunrise():
    s = G()
    flst(s, 'M11 34A13 13 0 0 1 37 34Z')
    st(s, 'M4 34H44M10 40H38')
    for a in (180, 215, 250, 290, 325, 360):
        r = math.radians(a)
        st(s, f'M{f2(24 + 16.5 * math.cos(r), 34 + 16.5 * math.sin(r))}L{f2(24 + 20.5 * math.cos(r), 34 + 20.5 * math.sin(r))}', 2)
    return s


def g_sun():
    s = G()
    flst(s, circ(24, 24, 9))
    for a in range(0, 360, 45):
        r = math.radians(a)
        st(s, f'M{f2(24 + 13 * math.cos(r), 24 + 13 * math.sin(r))}L{f2(24 + 18 * math.cos(r), 24 + 18 * math.sin(r))}')
    return s


def g_sun_low():
    s = G()
    flst(s, circ(20, 22, 8))
    for a in (135, 180, 225, 270, 315):
        r = math.radians(a)
        st(s, f'M{f2(20 + 11.5 * math.cos(r), 22 + 11.5 * math.sin(r))}L{f2(20 + 15.5 * math.cos(r), 22 + 15.5 * math.sin(r))}', 2.2)
    st(s, 'M6 38H42')
    st(s, 'M28 32L40 38', 2)
    return s


def g_sunset():
    s = G()
    flst(s, 'M11 32A13 13 0 0 1 37 32Z')
    st(s, 'M4 32H44M12 38H36')
    st(s, 'M24 6V15M20 11L24 15L28 11', 2.2)
    return s


def g_fajr():
    s = G()
    fl(s, 'M12 36A12 12 0 0 1 36 36Z', .7)
    st(s, 'M12 36A12 12 0 0 1 36 36')
    st(s, 'M4 36H44M12 42H36')
    s.path(crescent(34, 12, 6, .34, -30), S)
    s.path(sparkle(16, 12, 3), S)
    return s


def g_isha():
    s = G()
    flst(s, crescent(22, 25, 15, .34, -35))
    s.path(sparkle(36, 12, 4.5), S)
    s.path(sparkle(39, 26, 2.8), S)
    return s


def g_palm():
    s = G()
    st(s, 'M25 44C25 34 24 26 22 19', 3)
    for d in ('M22 19C17 13 10 13 6 17C11 16 16 17 22 19Z', 'M22 19C24 11 31 8 37 10C31 12 26 14 22 19Z',
              'M22 19C27 16 36 17 41 23C35 21 28 20 22 19Z', 'M22 19C17 18 10 21 8 27C13 23 18 21 22 19Z', 'M22 19C20 13 22 7 27 4C25 9 24 13 22 19Z'):
        flst(s, d, 1.8)
    st(s, 'M6 44H42', 2)
    return s


def g_story():
    s = G()
    d = 'M24 18C19.5 15 13 14.5 7 16.5V40C13 38 19.5 38.5 24 41.5C28.5 38.5 35 38 41 40V16.5C35 14.5 28.5 15 24 18Z'
    flst(s, d)
    st(s, 'M24 18V41.5')
    s.path(sparkle(24, 7, 5.5), S)
    s.path(sparkle(14, 9, 2.6), S)
    s.path(sparkle(34, 9, 2.6), S)
    return s


def g_quiz():
    s = G()
    flst(s, 'M8 10H40V32H22L13 40V32H8Z')
    st(s, 'M20 17C20 13 28 13 28 17.5C28 21 24 21 24 24.5', 2.4)
    s.circle(24, 28.3, 1.8, S)
    return s


def g_medal():
    s = G()
    flst(s, 'M15 4H22L26 16H19ZM33 4H26L22 16H29Z', 1.8)
    flst(s, circ(24, 29, 13))
    s.path(star_poly(24, 29.5, 7, 3, 5, 0), S)
    return s


def g_bell():
    s = G()
    flst(s, 'M12 34V22C12 15 17 10 24 10C31 10 36 15 36 22V34L39 38H9Z')
    st(s, 'M20 42C21 44 27 44 28 42')
    s.path(crescent(24, 22, 5, .34, -30), S)
    return s


def g_shield():
    s = G()
    flst(s, 'M24 4L40 10V22C40 32 33 40 24 44C15 40 8 32 8 22V10Z')
    st(s, rrect(17, 21, 14, 11, 2.5), 2)
    st(s, 'M20 21V18A4 4 0 0 1 28 18V21', 2)
    return s


def g_help():
    s = G()
    flst(s, circ(24, 24, 17))
    st(s, circ(24, 24, 7), 2)
    fl(s, circ(24, 24, 7))
    for a in (45, 135, 225, 315):
        r = math.radians(a)
        st(s, f'M{f2(24 + 7 * math.cos(r), 24 + 7 * math.sin(r))}L{f2(24 + 17 * math.cos(r), 24 + 17 * math.sin(r))}', 2.2)
    return s


def g_flag():
    s = G()
    st(s, 'M10 44V6')
    flst(s, 'M10 8C18 4 24 12 32 8C35 6.5 38 6 40 7V27C38 26 35 26.5 32 28C24 32 18 24 10 28Z')
    return s


def g_sync():
    s = G()
    flst(s, 'M14 36H35C40 36 43 32 43 27.5C43 23 39.5 19.5 35 19.5C33.5 13.5 28.5 10 23 10C16 10 11 15 11 21.5C7.5 22.5 5 25.5 5 29C5 33 8.5 36 14 36Z')
    st(s, 'M19 26A5.5 5.5 0 0 1 29 24M29 24V20M29 24H25', 2)
    return s


def g_download():
    s = G()
    flst(s, 'M8 30V38C8 40 10 42 12 42H36C38 42 40 40 40 38V30')
    st(s, 'M24 6V30M15 21L24 30L33 21')
    return s


def g_search():
    s = G()
    flst(s, circ(21, 21, 13))
    st(s, 'M31 31L42 42', 3.4)
    s.path(sparkle(21, 21, 5), S)
    return s


def g_globe():
    s = G()
    flst(s, circ(24, 24, 18))
    st(s, 'M6 24H42M24 6C18 12 18 36 24 42M24 6C30 12 30 36 24 42', 2)
    return s


def g_settings():
    s = G()
    flst(s, star_poly(24, 24, 18, 14, 8, 22.5))
    st(s, circ(24, 24, 6))
    return s


def g_hadith_day():
    s = G()
    fl(s, 'M12 26A12 12 0 0 1 36 26Z', .8)
    st(s, 'M12 26A12 12 0 0 1 36 26')
    for a in (200, 240, 300, 340):
        r = math.radians(a)
        st(s, f'M{f2(24 + 15 * math.cos(r), 26 + 15 * math.sin(r))}L{f2(24 + 18.5 * math.cos(r), 26 + 18.5 * math.sin(r))}', 2)
    d = 'M24 30C20 28 14.5 27.5 8 29V44C14.5 42.5 20 43 24 45C28 43 33.5 42.5 40 44V29C33.5 27.5 28 28 24 30Z'
    flst(s, d, 2.2)
    st(s, 'M24 30V45', 2)
    return s


def g_audio():
    s = G()
    flst(s, circ(24, 24, 18))
    sfill(s, 'M20 16.5L32 24L20 31.5Z')
    return s


def g_lantern():
    s = G()
    st(s, 'M24 3V8')
    flst(s, 'M19 12L24 8L29 12Z', 1.8)
    flst(s, 'M17 12H31L34 32L24 40L14 32Z')
    st(s, 'M24 12V40M15.5 22H32.5', 1.6)
    flst(s, 'M21 40H27L24 45Z', 1.6)
    return s


# hajj stages
def g_luggage():
    s = G()
    flst(s, rrect(10, 14, 28, 26, 4))
    st(s, 'M18 14V9C18 7.5 19 6.5 20.5 6.5H27.5C29 6.5 30 7.5 30 9V14')
    st(s, 'M17 14V40M31 14V40', 2)
    st(s, 'M15 44H18M30 44H33', 2.4)
    return s


def g_ihram():
    s = G()
    flst(s, 'M10 8C16 6 22 10 24 16L22 44H12C10 32 8 20 10 8Z')
    flst(s, 'M38 8C32 6 26 10 24 16L26 44H36C38 32 40 20 38 8Z')
    st(s, 'M14 20C17 22 20 22 22 20M34 26C31 28 28 28 26 26', 1.8)
    return s


def g_tent():
    s = G()
    flst(s, 'M4 40L16 16L28 40Z')
    flst(s, 'M20 40L32 14L44 40Z')
    st(s, 'M16 16V12M32 14V10', 2)
    st(s, 'M13 40L16 30L19 40M29 40L32 28L35 40', 2)
    st(s, 'M2 42H46', 2)
    return s


def g_arafah():
    s = G()
    flst(s, 'M4 40C10 30 14 22 24 20C34 22 38 30 44 40Z')
    st(s, 'M24 20V10', 2.4)
    fl(s, 'M22 12H26V20H22Z')
    s.circle(37, 9, 4.6, S)
    st(s, 'M2 42H46', 2)
    return s


def g_pebbles():
    s = G()
    for (x, y, rx, ry, a) in ((14, 36, 6, 4, -10), (26, 37, 7, 4.5, 8), (36, 35, 5, 3.6, -20), (20, 30, 5, 3.4, 15), (31, 29, 4.4, 3.2, -5)):
        flst(s, rell(x, y, rx, ry, a), 2)
    s.path(crescent(12, 12, 6, .34, -30), S)
    s.path(sparkle(30, 10, 3.2), S)
    s.path(sparkle(40, 16, 2.4), S)
    return s


def g_jamarat():
    s = G()
    flst(s, 'M18 42V12C18 10 20 8 24 8C28 8 30 10 30 12V42Z')
    st(s, 'M12 42H36')
    for (x, y) in ((8, 20), (11, 26), (38, 18)):
        sfill(s, circ(x, y, 1.9))
    st(s, 'M6 30L14 24', 1.6)
    return s


def g_tawaf():
    s = G()
    kaaba(s, 17, 15, 14, 16)
    st(s, 'M38 20A16 9 0 1 1 10 20', 2.2)
    st(s, 'M10 20L8 15M10 20L15 18', 2.2)
    st(s, 'M8 33A17 9 0 0 0 40 33', 2.2)
    return s


def g_sai():
    s = G()
    flst(s, 'M2 40C5 32 9 28 13 28C17 28 20 34 22 40Z')
    flst(s, 'M26 40C28 34 31 28 35 28C39 28 43 32 46 40Z')
    st(s, 'M10 18H38M33 13L38 18L33 23M15 13L10 18L15 23', 2.2)
    return s


def g_scissors():
    s = G()
    flst(s, circ(13, 34, 6))
    flst(s, circ(35, 34, 6))
    st(s, 'M17 30L34 6M31 30L14 6')
    return s


# wudu / ghusl steps (bigger detail, still duotone)
def g_intention():
    s = G()
    flst(s, 'M24 40C24 40 8 31 8 19C8 13 12 9 17 9C20.5 9 22.8 11 24 13.5C25.2 11 27.5 9 31 9C36 9 40 13 40 19C40 31 24 40 24 40Z')
    s.path(sparkle(24, 22, 6), S)
    return s


def g_hands_wash():
    s = G()
    hand(s, False, -3, 4, .82)
    hand(s, True, 3, 4, .82)
    for (x, y) in ((16, 5), (24, 3), (32, 5)):
        flst(s, f'M{x} {y}C{x} {y} {x - 2.4} {y + 3.4} {x - 2.4} {y + 4.6}A2.4 2.4 0 0 0 {x + 2.4} {y + 4.6}C{x + 2.4} {y + 3.4} {x} {y} {x} {y}Z', 1.4)
    return s


def _face_profile(s):
    d = 'M30 6C20 6 13 12 13 21C13 25 11 27 9 30C8.5 31 9 32 10.5 32H13V36C13 38.5 15 40 17.5 40H21V44'
    st(s, d)
    st(s, 'M30 6C36 7 40 13 40 20C40 28 36 33 33 36V44', 2.2)
    fl(s, d + 'H33V36C36 33 40 28 40 20C40 13 36 7 30 6Z', .9)


def g_mouth():
    s = G()
    _face_profile(s)
    st(s, 'M12 35H16', 2)
    flst(s, 'M5 20C5 20 2 24 2 25.6A3 3 0 0 0 8 25.6C8 24 5 20 5 20Z', 1.6)
    return s


def g_nose():
    s = G()
    _face_profile(s)
    s.circle(11, 30.5, 1.4, S)
    flst(s, 'M5 13C5 13 2 17 2 18.6A3 3 0 0 0 8 18.6C8 17 5 13 5 13Z', 1.6)
    return s


def g_face():
    s = G()
    flst(s, rell(24, 25, 13, 17, 0))
    st(s, 'M17 24C18 25.5 20 25.5 21 24M27 24C28 25.5 30 25.5 31 24', 2)
    st(s, 'M21 33C23 34.5 25 34.5 27 33', 2)
    for (x, y) in ((7, 14), (41, 18), (8, 32)):
        flst(s, f'M{x} {y}C{x} {y} {x - 2.2} {y + 3} {x - 2.2} {y + 4.2}A2.2 2.2 0 0 0 {x + 2.2} {y + 4.2}C{x + 2.2} {y + 3} {x} {y} {x} {y}Z', 1.4)
    return s


def g_arm():
    s = G()
    flst(s, 'M6 41L27 20L31 24L10 45Z', 2.4)
    hand(s, False, 13, -9, .6)
    for (x, y) in ((8, 22), (16, 16), (24, 38)):
        flst(s, f'M{x} {y}C{x} {y} {x - 2.2} {y + 3} {x - 2.2} {y + 4.2}A2.2 2.2 0 0 0 {x + 2.2} {y + 4.2}C{x + 2.2} {y + 3} {x} {y} {x} {y}Z', 1.4)
    return s


def g_head():
    s = G()
    flst(s, 'M12 44V30C12 21 17 15 24 15C31 15 36 21 36 30V44')
    st(s, 'M12 30C16 27 32 27 36 30', 1.8)
    st(s, 'M11 11C17 4 31 4 37 11', 2.4)
    st(s, 'M33 6L37 11L31.5 12.5', 2.2)
    return s


def g_ears():
    s = G()
    flst(s, 'M18 8C27 6 34 12 34 20C34 26 30 28 28 32C26 36 26 40 21 41C17 42 14 39 14 36')
    st(s, 'M22 16C26 15 28 18 27.5 21C27 24 24 24.5 24 27', 2)
    flst(s, 'M40 22C40 22 37 26 37 27.6A3 3 0 0 0 43 27.6C43 26 40 22 40 22Z', 1.6)
    return s


def g_feet():
    s = G()
    flst(s, 'M11 16V33C11 37 13.5 40 18 40H38C42 40 43.5 36 40.5 33.5C36.5 30.5 30 29.5 26 27C22.5 24.5 21 21 21 16Z')
    for i, x in enumerate((33, 36.5, 39.5)):
        sfill(s, circ(x, 37.5, 1.2), .45)
    for (x, y) in ((30, 10), (37, 16), (26, 18)):
        flst(s, f'M{x} {y}C{x} {y} {x - 2.2} {y + 3} {x - 2.2} {y + 4.2}A2.2 2.2 0 0 0 {x + 2.2} {y + 4.2}C{x + 2.2} {y + 3} {x} {y} {x} {y}Z', 1.4)
    return s


def g_privacy_drop():
    s = G()
    flst(s, 'M24 4L40 10V22C40 32 33 40 24 44C15 40 8 32 8 22V10Z')
    flst(s, 'M24 14C24 14 17 22 17 26.5A7 7 0 0 0 31 26.5C31 22 24 14 24 14Z', 2)
    return s


def g_body_side():
    s = G()
    st(s, circ(24, 9, 5))
    fl(s, 'M24 16C29 16 32 19 32 24V34H24Z')
    st(s, 'M16 34V24C16 19 19 16 24 16C29 16 32 19 32 24V34M20 34V44M28 34V44')
    st(s, 'M38 18C42 22 42 30 38 34M35 32L38 34L40 30.5', 2)
    return s


def g_check_all():
    s = G()
    flst(s, circ(24, 24, 18))
    st(s, 'M15 24.5L21.5 31L33.5 18', 3)
    return s


GLYPH_FUNCS = {k[2:]: v for k, v in dict(globals()).items() if k.startswith('g_') and callable(v)}

THEMES = {
    'gold': ('#8C6D2E', '#EBD9AE'),
    'green': ('#17533C', '#CFE3D4'),
}


def themed(name, theme='gold'):
    s = GLYPH_FUNCS[name]()
    sc, ac = THEMES[theme]
    t = s.text().replace('#S', sc).replace('#A', ac)
    class _T:
        def text(self_inner):
            return t
    return _T()


GLYPHS = {f'glyph_{n}': (lambda n=n: themed(n)) for n in GLYPH_FUNCS}
