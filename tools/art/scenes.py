"""Hero illustrations (scenes). Each function returns an Svg. No depictions of prophets."""
import math, random
from lib import *

W, H = 1200, 520


def _sky(s, top, mid, bottom, mid_at=.55):
    s.rect(0, 0, s.w, s.h, s.lin([(0, top), (mid_at, mid), (1, bottom)]))


def skyline_layers(s, by, k, far, mid, near, near_dome, near_trim, window, seed=11):
    """Three depth layers of mosques/minarets/trees along a baseline."""
    rnd = random.Random(seed)
    # far: small domes & minarets, pale
    for i, x in enumerate((90, 330, 560, 820, 1060)):
        mosque(s, x, by - 40 * k, .42 * k * rnd.uniform(.9, 1.15), far, minarets=i % 2 == 0, side_domes=False, dome_kind='round' if i % 2 else 'onion')
    s.rect(0, by - 42 * k, s.w, 60 * k, far)
    # mid: medium mosque + cypress
    mosque(s, 250, by - 14 * k, .62 * k, mid, side_domes=True, dome_kind='round')
    for x in (60, 120, 430, 470, 700, 1130):
        cypress(s, x, by - 10 * k, rnd.uniform(60, 95) * k, mid)
    s.rect(0, by - 16 * k, s.w, 40 * k, mid)
    # near: hero mosque right of centre
    mosque(s, 850, by, 1.0 * k, near, near_dome, near_trim, window)
    s.rect(0, by - 2, s.w, s.h - by + 2, near)


def prayer_sky(period):
    """Next-prayer banner art for each part of the day: dawn | day | dusk | night."""
    s = Svg(W, H)
    cfg = {
        'dawn': dict(sky=('#2C3E66', '#B98AA0', '#F6C9A0'), sun=('#FFE3B8', 520, 330, 60), far='#C9A9B4', mid='#8E7F98', near='#3E4A6B', dome='#56618A', trim='#E8C9A0', win='#2C3554', stars=40),
        'day': dict(sky=('#FBF3E1', '#F8EDD6', '#F3E6C9'), sun=('#F6C35B', 300, 150, 52), far='#D5DFCF', mid='#A9C2AE', near='#3F7A5E', dome='#2F6B4F', trim='#CDAE6E', win='#E8F0E6', stars=0),
        'dusk': dict(sky=('#5B3B6B', '#E07A5F', '#F7C873'), sun=('#FFD27A', 420, 360, 70), far='#C98A7A', mid='#8E5A63', near='#3A2C45', dome='#4D3A5C', trim='#F2B66D', win='#F7C873', stars=12),
        'night': dict(sky=('#06261C', '#0E3A2B', '#1F5B44'), sun=None, far='#2A5A47', mid='#1C4636', near='#0B2E22', dome='#123D2E', trim='#CDAE6E', win='#E8C77A', stars=90),
    }[period]
    _sky(s, *cfg['sky'])
    if cfg['stars']:
        stars(s, 0, 0, W, H * .55, cfg['stars'], '#FFF6DE', seed=3)
    if period == 'night':
        s.circle(330, 120, 120, s.rad([(0, '#F5E3B0', .35), (1, '#F5E3B0', 0)]))
        s.path(crescent(330, 120, 46, .33, -30), '#F3DDA0')
    elif cfg['sun']:
        c, x, y, r = cfg['sun']
        s.circle(x, y, r * 3.2, s.rad([(0, c, .55), (.5, c, .18), (1, c, 0)]))
        s.circle(x, y, r, c)
        if period == 'day':
            for i in range(12):
                a = math.radians(i * 30)
                s.line(x + math.cos(a) * r * 1.25, y + math.sin(a) * r * 1.25, x + math.cos(a) * r * 1.6, y + math.sin(a) * r * 1.6, c, sw=7)
    if period in ('day',):
        for cx, cy, w in ((640, 120, 170), (980, 80, 120)):
            s.path(cloud(cx, cy, w), '#FFFFFF', op=.8)
    skyline_layers(s, 470, 1.0, cfg['far'], cfg['mid'], cfg['near'], cfg['dome'], cfg['trim'], cfg['win'])
    return s


def skyline_strip(period='day'):
    """Transparent skyline (no sky) for the Home next-prayer card: mosque cluster on the right."""
    s = Svg(720, 300)
    c = {'day': ('#D7E3D3', '#A7C3AE', '#3F7A5E', '#2F6B4F', '#CDAE6E', '#EAF2E8'),
         'night': ('#2A5A47', '#1C4636', '#0B2E22', '#123D2E', '#CDAE6E', '#E8C77A'),
         'dawn': ('#C9A9B4', '#8E7F98', '#3E4A6B', '#56618A', '#E8C9A0', '#2C3554'),
         'dusk': ('#C98A7A', '#8E5A63', '#3A2C45', '#4D3A5C', '#F2B66D', '#F7C873')}[period]
    far, mid, near, dome, trim, win = c
    fade = s.lin([(0, '#000', 0), (1, '#000', 1)], 0, 0, 1, 0)
    g = s.sub()
    mosque(g, 250, 262, .45, far, side_domes=False, dome_kind='round')
    minaret(g, 120, 266, 7, 120, far, far)
    mosque(g, 560, 262, .5, far, side_domes=False)
    g.rect(0, 258, 720, 60, far)
    mosque(g, 330, 280, .62, mid, dome_kind='round')
    cypress(g, 180, 286, 70, mid)
    cypress(g, 205, 286, 54, mid)
    g.rect(0, 278, 720, 40, mid)
    mosque(g, 520, 300, .82, near, dome, trim, win)
    cypress(g, 680, 300, 90, near)
    cypress(g, 705, 300, 66, near)
    s.group(g)
    return s


def ramadan_hero():
    s = Svg(W, H)
    _sky(s, '#06261C', '#0F3D2D', '#1E5640', .6)
    s.circle(250, 110, 170, s.rad([(0, '#E8C77A', .32), (1, '#E8C77A', 0)]))
    stars(s, 0, 0, W, 300, 110, '#FCEFC8', seed=21, rmax=2)
    s.path(crescent(250, 110, 54, .30, -32), '#F1D488')
    # faint arabesque in the corner
    pattern(s, 760, 0, 1200, 260, 64, '#CDAE6E', 1.2, .10)
    # golden mosque on the left
    g = s.sub()
    mosque(g, 300, 470, 1.15, '#8C7036', '#B8954F', '#E0C893', '#3B2F14', dome_kind='onion')
    s.group(g)
    mosque(s, 640, 470, .55, '#2C5E48', '#2C5E48', '#3E7259', None, dome_kind='round')
    for x, h in ((40, 150), (78, 120), (570, 110), (720, 130)):
        cypress(s, x, 474, h, '#0A2C20')
    palm(s, 800, 478, 210, '#0A2C20', '#0A2C20', lean=-.08, seed=4)
    palm(s, 880, 478, 170, '#0A2C20', '#0A2C20', lean=.1, seed=8)
    s.path(hill(0, W, H, [(0, 470), (400, 462), (800, 474), (1200, 466)]), '#0A2C20')
    for x, k in ((1010, 1.1), (1110, .85), (930, .7)):
        lantern(s, x, 40 + (1 - k) * 60, k, '#CDAE6E', s.lin([(0, '#FBE3A1'), (1, '#E0A93E')]), '#F6D27A')
    return s


def welcome_scene():
    """Onboarding: bright mosque with gardens and a path."""
    s = Svg(W, 700)
    _sky(s, '#FBF4E4', '#F7EBD2', '#F3E3C2', .7)
    s.circle(860, 170, 70, '#F6D27A', op=.9)
    s.circle(860, 170, 200, s.rad([(0, '#F6D27A', .45), (1, '#F6D27A', 0)]))
    for cx, cy, w in ((280, 150, 200), (1000, 110, 150)):
        s.path(cloud(cx, cy, w), '#FFFFFF', op=.85)
    s.path(hill(0, W, 700, [(0, 520), (300, 480), (650, 505), (950, 470), (1200, 500)]), '#DCE7D6')
    mosque(s, 330, 520, .55, '#C9D9C6', side_domes=False, dome_kind='round')
    mosque(s, 960, 512, .5, '#C9D9C6', side_domes=False)
    mosque(s, 600, 560, 1.25, '#F3EBDA', '#2F6B4F', '#B8954F', '#C9B88F', dome_kind='onion')
    for x, h in ((170, 170), (215, 130), (1010, 160), (1050, 120)):
        cypress(s, x, 566, h, '#3F7A5E')
    palm(s, 90, 600, 260, '#7A5E26', '#2F6B4F', lean=.1, seed=2)
    palm(s, 1120, 600, 240, '#7A5E26', '#2F6B4F', lean=-.12, seed=9)
    s.path(hill(0, W, 700, [(0, 590), (400, 570), (800, 585), (1200, 568)]), '#9DBE9F')
    s.path(hill(0, W, 700, [(0, 640), (600, 610), (1200, 640)]), '#6E9F7D')
    s.path('M560 700C590 650 650 640 610 600C590 580 600 575 600 566H612C612 580 640 600 660 625C690 660 690 680 700 700Z', '#F1E3C2')
    for x in (130, 300, 880, 1050):
        s.path(blob(x, 640, 60, 26, 8, .15, x), '#4F8A68')
    return s


def kaaba_scene():
    s = Svg(W, H)
    _sky(s, '#F4EAD5', '#EBDDBD', '#E2D1AA', .7)
    s.circle(980, 120, 220, s.rad([(0, '#FFF4D6', .9), (1, '#FFF4D6', 0)]))
    # clock tower far back
    t = '#D9CBA9'
    s.rect(965, 40, 70, 230, t)
    s.rect(955, 120, 90, 150, t)
    s.path('M975 40L1000 0L1025 40Z', t)
    s.circle(1000, 80, 22, '#EFE4C8')
    for x in (620, 760, 1120):
        s.rect(x, 150, 60, 120, t)
    # Haram arcade (two storeys)
    a1, a2 = '#E9DFC6', '#CFC0A0'
    for i, x in enumerate((150, 380, 800, 1060)):
        minaret(s, x, 300, 18, 230, a1, a2, cap='cone')
    s.rect(0, 220, W, 100, a1)
    for row, (y, h) in enumerate(((268, 40), (316, 46))):
        for i in range(30):
            s.path(pointed_arch(i * 42 + 6, y, 26, h), a2)
    s.rect(0, 216, W, 8, a2)
    # mataf
    s.ellipse(600, 470, 720, 170, '#F8F4EA')
    s.ellipse(600, 470, 720, 170, None, stroke='#E6DCC6', sw=3)
    rnd = random.Random(5)
    for ring in range(9):
        rx, ry = 300 + ring * 48, 60 + ring * 14
        for j in range(int(40 + ring * 10)):
            a = rnd.uniform(0, 2 * math.pi)
            x, y = 600 + rx * math.cos(a), 430 + ry * math.sin(a) + 30
            if 470 < x < 730 and 330 < y < 450:
                continue
            c = rnd.choice(['#FFFFFF', '#FFFFFF', '#F1ECE1', '#D9D2C3', '#B9AE98'])
            s.ellipse(x, y, 4.2, 6.2, c)
            s.circle(x, y - 7.5, 2.6, c)
    # hijr ismail
    s.path('M470 452C430 452 410 432 430 418C446 408 470 410 470 410', None, stroke='#FFFFFF', sw=9)
    # Kaaba (2-face perspective)
    s.path('M492 300L600 282L708 296L708 452L600 470L492 452Z', '#141414')
    s.path('M492 300L600 282L600 470L492 452Z', '#0C0C0C')
    s.path('M600 282L708 296L708 452L600 470Z', '#1B1B1B')
    # kiswa folds
    for i in range(1, 6):
        x = 492 + i * 18
        s.line(x, 300 - i * 3, x, 455 - i * 3, '#222', sw=1.2, op=.6)
    # gold hizam band
    s.path('M492 324L600 307L708 321L708 337L600 323L492 340Z', '#C9A24E')
    s.path('M492 328L600 311L708 325', None, stroke='#F2D88C', sw=1.6)
    for i in range(14):
        x = 498 + i * 15
        y = 330 - (i * 15 if x < 600 else (600 - 492)) * (17 / 108) if x < 600 else 309 + (x - 600) * (14 / 108)
        s.line(x, y, x + 8, y - 1, '#7A5E26', sw=1, op=.7)
    # door
    s.path('M622 360L652 364V430L622 426Z', '#C9A24E')
    s.path('M626 364L648 367V426L626 423Z', None, stroke='#8C6A2A', sw=1.4)
    # base (shadharwan) + shadow
    s.path('M488 452L600 470L712 452L712 458L600 477L488 458Z', '#E8E1D2')
    s.ellipse(600, 482, 150, 12, '#000', op=.08)
    # maqam ibrahim
    s.path('M760 470V450C760 438 784 438 784 450V470Z', '#C9A24E')
    s.ellipse(772, 471, 16, 4, '#B8954F')
    return s


def hills_scene():
    """Calm misty hills with a distant mosque (Janazah / special prayers)."""
    s = Svg(W, H)
    _sky(s, '#EEF2E6', '#E3EADB', '#D6E2CE', .6)
    s.circle(860, 140, 160, s.rad([(0, '#FFFFFF', .7), (1, '#FFFFFF', 0)]))
    s.path(hill(0, W, H, [(0, 300), (250, 230), (480, 290), (760, 210), (1000, 280), (1200, 240)]), '#C9D8C3')
    mosque(s, 860, 300, .38, '#B4C9B0', side_domes=False)
    s.path(hill(0, W, H, [(0, 350), (300, 300), (620, 345), (900, 300), (1200, 330)]), '#A6C0A2')
    s.path(hill(0, W, H, [(0, 400), (350, 360), (700, 395), (1000, 365), (1200, 390)]), '#7FA481')
    for x, h in ((140, 120), (175, 90), (980, 110), (1010, 80), (1040, 100)):
        cypress(s, x, 400, h, '#5E8B66')
    s.path('M0 470C200 430 420 470 600 450C800 428 1000 470 1200 440V520H0Z', '#4E7B59')
    s.path('M380 520C460 480 560 470 640 440C700 420 760 430 820 420', None, stroke='#E9E1C9', sw=14, op=.7)
    for i in range(3):
        s.rect(0, 250 + i * 60, W, 30, '#FFFFFF', op=.10)
    return s


def madinah_scene():
    """Masjid an-Nabawi green dome at golden hour."""
    s = Svg(W, H)
    _sky(s, '#F7E7C4', '#F1D7A0', '#E9C27E', .65)
    s.circle(320, 300, 260, s.rad([(0, '#FFF1C9', .9), (1, '#FFF1C9', 0)]))
    s.circle(320, 300, 60, '#FFE6A6')
    wall, wall2 = '#EFE2C6', '#D8C49D'
    for x in (140, 1060):
        minaret(s, x, 470, 20, 300, wall, wall2, cap='onion')
    s.rect(0, 360, W, 120, wall)
    for i in range(28):
        s.path(round_arch(i * 44 + 8, 452, 26, 54), wall2)
    s.rect(0, 352, W, 10, wall2)
    # green dome on a square drum
    s.rect(520, 300, 160, 62, wall)
    s.rect(512, 294, 176, 10, wall2)
    s.path(onion_dome(600, 294, 150, 150), s.lin([(0, '#3E9E6C'), (1, '#1E6B47')], 0, 0, 1, 1))
    s.path(onion_dome(600, 294, 150, 150), None, stroke='#17533C', sw=2)
    for i in range(1, 6):
        s.path(f'M{600 - 75 + i * 25} 294Q{600 - 40 + i * 13} 200 600 146', None, stroke='#17533C', sw=1.4, op=.4)
    s.line(600, 144, 600, 118, '#C9A24E', sw=4)
    s.path(crescent(600, 108, 10, .38, -90), '#C9A24E')
    s.path(onion_dome(760, 352, 60, 52), '#DCCBA8')
    s.path(round_dome(440, 352, 70, 34), '#DCCBA8')
    for x in (60, 980, 1140):
        palm(s, x, 520, 220, '#7A5E26', '#3F6E4C', lean=.08 if x < 600 else -.1, seed=x)
    s.rect(0, 470, W, 50, '#E6D5B0')
    return s


def roadmap_scene():
    """Winding path climbing hills towards a mosque."""
    s = Svg(W, 600)
    _sky(s, '#FBF3E1', '#F7EAD0', '#F1E2C2', .6)
    s.circle(820, 170, 64, '#F6C35B')
    s.circle(820, 170, 200, s.rad([(0, '#F6C35B', .4), (1, '#F6C35B', 0)]))
    s.path(hill(0, W, 600, [(0, 300), (300, 250), (600, 300), (900, 230), (1200, 290)]), '#D6E2CC')
    mosque(s, 860, 268, .34, '#9DBB9E', side_domes=False)
    s.path(hill(0, W, 600, [(0, 380), (350, 320), (700, 370), (1000, 330), (1200, 360)]), '#AFCBA8')
    s.path(hill(0, W, 600, [(0, 470), (400, 410), (800, 450), (1200, 420)]), '#86AE86')
    s.path(hill(0, W, 600, [(0, 560), (500, 520), (1200, 545)]), '#6B9A72')
    s.path('M300 600C380 540 520 540 560 490C600 440 520 420 600 380C680 345 760 340 830 300C860 285 860 276 860 270'
           'L872 270C872 282 872 292 846 308C780 350 700 360 640 396C580 430 660 452 616 500C560 556 450 560 400 600Z', '#F2E4C3')
    for x, h in ((120, 120), (160, 90), (1000, 110), (1040, 140), (1080, 90)):
        round_tree(s, x, 470 + (h % 3) * 10, h, '#7A5E26', '#5E8B66', '#7FA481', seed=x)
    for x in (200, 700, 1100):
        s.path(blob(x, 580, 70, 26, 8, .15, x), '#55875F')
    return s


def sprout_scene():
    s = Svg(W, 600)
    _sky(s, '#FCF4E2', '#F9EDD5', '#F5E6C8', .7)
    s.circle(860, 200, 70, '#F6C35B')
    for i in range(16):
        a = math.radians(i * 22.5)
        s.line(860 + math.cos(a) * 95, 200 + math.sin(a) * 95, 860 + math.cos(a) * 125, 200 + math.sin(a) * 125, '#F6C35B', sw=6, op=.8)
    s.path(hill(0, W, 600, [(0, 430), (300, 400), (700, 430), (1200, 410)]), '#E5D6B0')
    s.path(hill(0, W, 600, [(0, 480), (600, 455), (1200, 480)]), '#D7C391')
    st = '#4F8A3E'
    s.path('M600 470C598 420 604 380 600 330', None, stroke=st, sw=10)
    s.path('M600 360C560 330 500 330 470 360C500 392 560 395 600 360Z', '#6DAA4F')
    s.path('M600 360C560 330 500 330 470 360', None, stroke='#4F8A3E', sw=3)
    s.path('M600 330C640 280 720 280 760 310C720 352 640 360 600 330Z', '#7DBB5C')
    s.path('M600 330C640 296 710 292 760 310', None, stroke='#4F8A3E', sw=3)
    s.path(blob(600, 478, 90, 16, 9, .1, 3), '#B89C66')
    for x, y in ((380, 520), (820, 525), (200, 540), (1000, 535)):
        s.path(blob(x, y, 50, 14, 7, .2, x), '#C9B07E')
    return s


def quran_rehal(bg=True):
    """Open Quran on a wooden rehal (stand)."""
    s = Svg(600, 480)
    if bg:
        s.rect(0, 0, 600, 480, '#F3EBD8')
        s.circle(300, 200, 260, s.rad([(0, '#FFF6DE', .9), (1, '#FFF6DE', 0)]))
    wood, wood2 = '#9C6B3A', '#7A5230'
    # rehal X-stand
    s.path('M150 300L300 420L450 300L462 312L312 432H288L138 312Z', wood)
    s.path('M150 430L300 330L450 430L438 442L300 350L162 442Z', wood2)
    s.path('M120 300H480L470 318H130Z', wood2)
    # pages
    s.path('M300 300C250 270 180 262 120 280V120C180 102 250 110 300 140Z', '#FFFCF3')
    s.path('M300 300C350 270 420 262 480 280V120C420 102 350 110 300 140Z', '#FFFCF3')
    s.path('M300 300C250 270 180 262 120 280L112 290C180 272 250 280 300 310C350 280 420 272 488 290L480 280C420 262 350 270 300 300Z', '#E8DCC0')
    # cover edges
    s.path('M120 280L106 294V134L120 120Z', '#17533C')
    s.path('M480 280L494 294V134L480 120Z', '#17533C')
    s.path('M106 294C180 276 250 284 300 314C350 284 420 276 494 294L494 302C420 284 350 292 300 322C250 292 180 284 106 302Z', '#17533C')
    # text lines + ornament
    for side in (-1, 1):
        for i in range(7):
            y0 = 150 + i * 17
            x0, x1 = (140, 282) if side < 0 else (318, 460)
            s.path(f'M{x0} {y0 + (6 if side < 0 else 0)}Q{(x0 + x1) / 2} {y0 - 6} {x1} {y0 + (0 if side < 0 else 6)}', None, stroke='#5B4A2E', sw=2.4, op=.55)
        cx = 211 if side < 0 else 389
        s.path(rub_el_hizb(cx, 128, 9), '#B8954F')
    s.line(300, 140, 300, 300, '#D9CBA7', sw=2)
    # ribbon
    s.path('M318 296L326 360L334 348L342 362L336 296Z', '#B8954F')
    return s


def lesson_scene():
    """Parent and child seated under a tree, warm evening (people are generic, not prophets)."""
    from figures import figure
    s = Svg(W, H)
    _sky(s, '#F5E3BE', '#EED39C', '#E7C27E', .65)
    s.circle(820, 330, 90, '#F9D88A')
    s.circle(820, 330, 260, s.rad([(0, '#FBE3A6', .7), (1, '#FBE3A6', 0)]))
    mosque(s, 1000, 400, .42, '#D8B97E', side_domes=False)
    mosque(s, 760, 410, .3, '#D8B97E', side_domes=False, dome_kind='round')
    s.path(hill(0, W, H, [(0, 420), (500, 400), (1200, 420)]), '#C9A46A')
    # tree
    s.path('M260 440C268 360 250 300 270 230L292 232C286 300 300 360 300 440Z', '#5A3C22')
    s.path(blob(280, 190, 190, 110, 10, .12, 4), '#3E5A3A')
    s.path(blob(200, 220, 100, 70, 8, .12, 5), '#344E31')
    s.path(blob(380, 210, 110, 70, 8, .12, 6), '#344E31')
    s.path(hill(0, W, H, [(0, 450), (600, 438), (1200, 452)]), '#7A5A34')
    g = s.sub()
    figure(g, 'sit', 470, 452, 2.1, '#2B2116', cap='#2B2116', halo='#7A5A34', arm_col='#3A2C1E')
    figure(g, 'sit_child', 640, 452, 1.45, '#2B2116', cap='#2B2116', mirror=True, halo='#7A5A34', arm_col='#3A2C1E')
    s.group(g)
    return s


def empty_spot(kind):
    """Small spot illustrations for empty/offline/error states (320x240)."""
    s = Svg(320, 240)
    s.ellipse(160, 206, 110, 16, '#E9E2CF')
    s.circle(160, 112, 92, '#F1EBDB')
    pattern(s, 70, 22, 250, 202, 45, '#CDAE6E', 1, .18)
    if kind == 'bookmarks':
        s.path('M120 60H200V190L160 160L120 190Z', '#17533C')
        s.path('M120 60H200V80H120Z', '#2E8060')
        s.path(sparkle(160, 115, 18), '#E0C893')
    elif kind == 'offline':
        s.path(cloud(160, 140, 170), '#FFFFFF')
        s.path(cloud(160, 140, 170), None, stroke='#93BFA7', sw=5)
        s.line(105, 70, 215, 185, '#B8954F', sw=9)
    elif kind == 'error':
        s.path('M160 50L240 180H80Z', '#F6EDD8')
        s.path('M160 50L240 180H80Z', None, stroke='#B8954F', sw=7)
        s.line(160, 95, 160, 140, '#7A5E26', sw=10)
        s.circle(160, 160, 6, '#7A5E26')
    elif kind == 'location':
        s.path('M160 190C160 190 100 130 100 95A60 60 0 0 1 220 95C220 130 160 190 160 190Z', '#17533C')
        s.circle(160, 95, 22, '#F6EDD8')
        s.ellipse(160, 196, 34, 7, '#000', op=.08)
    elif kind == 'search':
        s.circle(145, 105, 48, '#FFFFFF')
        s.circle(145, 105, 48, None, stroke='#17533C', sw=10)
        s.line(181, 141, 222, 182, '#17533C', sw=14)
        s.path(sparkle(145, 105, 14), '#B8954F')
    elif kind == 'downloads':
        s.path('M110 150V178H210V150', None, stroke='#17533C', sw=10)
        s.line(160, 60, 160, 150, '#17533C', sw=10)
        s.path('M128 120L160 152L192 120', None, stroke='#17533C', sw=10)
    return s


SCENES = {
    'hero_prayer_dawn': lambda: prayer_sky('dawn'),
    'hero_prayer_day': lambda: prayer_sky('day'),
    'hero_prayer_dusk': lambda: prayer_sky('dusk'),
    'hero_prayer_night': lambda: prayer_sky('night'),
    'skyline_day': lambda: skyline_strip('day'),
    'skyline_night': lambda: skyline_strip('night'),
    'skyline_dawn': lambda: skyline_strip('dawn'),
    'skyline_dusk': lambda: skyline_strip('dusk'),
    'hero_ramadan': ramadan_hero,
    'hero_welcome': welcome_scene,
    'hero_kaaba': kaaba_scene,
    'hero_hills': hills_scene,
    'hero_madinah': madinah_scene,
    'hero_roadmap': roadmap_scene,
    'hero_sprout': sprout_scene,
    'quran_rehal': quran_rehal,
    'hero_lesson': lesson_scene,
    **{f'empty_{k}': (lambda k=k: empty_spot(k)) for k in ('bookmarks', 'offline', 'error', 'location', 'search', 'downloads')},
}
