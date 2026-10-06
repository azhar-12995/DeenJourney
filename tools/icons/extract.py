"""Builds tools/figma/icons.json (name -> 24px line SVG) from lucide-react 1.33 (ISC licence) + custom marks.

Line icons use stroke #17533C (re-coloured in Figma / tinted in Compose).
"""
import re, json, os

SRC = r'C:/Users/azharsaddique/Projects/dinehub-admin/node_modules/lucide-react/dist/esm/icons'
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'figma', 'icons.json')
NAMES = """arrow-left arrow-right arrow-up arrow-down chevron-left chevron-right chevron-down chevron-up x plus minus check check-check
search bell bell-ring bell-off settings settings-2 sliders-horizontal ellipsis-vertical ellipsis menu house book-open book-marked book-open-text
graduation-cap layout-grid mosque play pause skip-back skip-forward repeat repeat-1 volume-2 volume-x headphones mic download cloud-download
trash-2 share-2 copy bookmark bookmark-check bookmark-plus heart star pencil square-pen notebook-pen highlighter type a-large-small languages globe
sun moon moon-star sunrise sunset clock timer alarm-clock calendar calendar-days map-pin navigation locate-fixed compass map user user-round
users user-plus baby shield shield-check lock eye eye-off mail key-round log-out circle-help info circle-alert triangle-alert circle-check
circle-x refresh-cw rotate-ccw history list list-checks filter arrow-up-down award trophy target flame sparkles gift hand-heart heart-handshake
lightbulb quote scroll-text file-text flag message-square message-circle phone external-link wifi wifi-off signal battery-full smartphone cloud
cloud-off upload database hard-drive vibrate list-music gauge circle-play circle-pause chart-column trending-up accessibility text-align-end
droplets shower-head hand hand-coins tent mountain feather scroll circle-dot grip-vertical send link qr-code image palette contrast fingerprint
user-check user-cog circle-user users-round crown sparkle flower-2 leaf sprout trees tree-palm footprints lamp sun-moon hourglass""".split()
ATTR = re.compile(r'(\w[\w-]*):\s*"([^"]*)"')
HEAD = ('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#17533C" '
        'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">')


def svg_of(name):
    p = os.path.join(SRC, name + '.mjs')
    js = open(p, encoding='utf8').read()
    r = re.search(r"export \{ default \} from './([\w-]+)\.mjs'", js)
    if r:
        js = open(os.path.join(SRC, r.group(1) + '.mjs'), encoding='utf8').read()
    m = re.search(r'__iconNode\s*=\s*(\[.*?\]);\s*\n', js, re.S) or re.search(r'createLucideIcon\([^,]+,\s*(\[.*\])\s*\)', js, re.S)
    els = []
    for tag, attrs in re.findall(r'\[\s*"(\w+)",\s*\{([^}]*)\}\s*\]', m.group(1)):
        a = {k: v for k, v in ATTR.findall(attrs) if k != 'key'}
        els.append('<%s %s/>' % (tag, ' '.join('%s="%s"' % (k, v) for k, v in a.items())))
    return HEAD + ''.join(els) + '</svg>'


out, missing = {}, []
for n in NAMES:
    try:
        out[n] = svg_of(n)
    except Exception:
        missing.append(n)

# custom line icons in the same 24px / 2px style
CUSTOM = {
    'kaaba': '<path d="M4 8l8-3 8 3v10.5l-8 3-8-3z"/><path d="M4 8l8 3 8-3"/><path d="M12 11v10.5"/><path d="M4 11.5l8 3 8-3"/>',
    'tasbih': '<circle cx="12" cy="4" r="1.6"/><circle cx="16.6" cy="5.6" r="1.6"/><circle cx="19" cy="9.8" r="1.6"/><circle cx="18" cy="14.5" r="1.6"/>'
              '<circle cx="7.4" cy="5.6" r="1.6"/><circle cx="5" cy="9.8" r="1.6"/><circle cx="6" cy="14.5" r="1.6"/><path d="M12 15.5v3"/><path d="M10 22l2-3.5 2 3.5z"/>',
    'dua-hands': '<path d="M5 21c0-2-2-4-2-7V7.5a1.5 1.5 0 0 1 3 0V12"/><path d="M6 12V5.5a1.5 1.5 0 0 1 3 0V12l2 2.5c.8 1 .6 2.3-.3 3L10 18v3"/>'
                 '<path d="M19 21c0-2 2-4 2-7V7.5a1.5 1.5 0 0 0-3 0V12"/><path d="M18 12V5.5a1.5 1.5 0 0 0-3 0V12l-2 2.5c-.8 1-.6 2.3.3 3L14 18v3"/>',
    'prayer-mat': '<rect x="5" y="2.5" width="14" height="17" rx="1.5"/><path d="M8.5 16.5V10a3.5 3.5 0 0 1 7 0v6.5"/><path d="M7 22v-2.5M10 22v-2.5M14 22v-2.5M17 22v-2.5"/>',
    'crescent': '<path d="M19.5 14.5A8 8 0 1 1 9.5 4.5a6.5 6.5 0 0 0 10 10z"/>',
    'qibla': '<circle cx="12" cy="12" r="9.5"/><path d="M12 6.5l2.3 5.5L12 17.5 9.7 12z"/><path d="M10.3 2.6h3.4v2.2h-3.4z"/>',
    'worship': '<path d="M3 21h18"/><path d="M6 21v-6.5c0-3.3 2.7-6 6-7.5 3.3 1.5 6 4.2 6 7.5V21"/><path d="M12 7V4"/><path d="M10 21v-3.5a2 2 0 0 1 4 0V21"/>',
    'learn': '<path d="M4 19.5V5a2 2 0 0 1 2-2h12v15H6a2 2 0 0 0-2 2 2 2 0 0 0 2 2h12"/><path d="M9 8.5h6M9 12h4"/>',
    'quran': '<path d="M12 6.5C10 5 7 4.6 3.5 5.3V17c3.5-.7 6.5-.3 8.5 1.2 2-1.5 5-1.9 8.5-1.2V5.3C17 4.6 14 5 12 6.5z"/><path d="M12 6.5v11.7"/><path d="M6 21l12-3M18 21L6 18"/>',
    'rtl': '<path d="M20 6H9M20 12H5M20 18H9"/><path d="M7 4L4 6l3 2"/>',
}
for k, body in CUSTOM.items():
    out[k] = HEAD + body + '</svg>'

# brand marks for sign-in buttons (filled, official colours)
out['brand-google'] = ('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">'
    '<path fill="#4285F4" d="M23.5 12.27c0-.85-.08-1.67-.22-2.45H12v4.64h6.46a5.52 5.52 0 0 1-2.4 3.62v3h3.88c2.27-2.09 3.56-5.17 3.56-8.81z"/>'
    '<path fill="#34A853" d="M12 24c3.24 0 5.96-1.07 7.94-2.92l-3.88-3c-1.07.72-2.45 1.15-4.06 1.15-3.12 0-5.77-2.11-6.71-4.95H1.28v3.1A12 12 0 0 0 12 24z"/>'
    '<path fill="#FBBC05" d="M5.29 14.28A7.2 7.2 0 0 1 4.91 12c0-.79.14-1.56.38-2.28v-3.1H1.28A12 12 0 0 0 0 12c0 1.94.46 3.77 1.28 5.38z"/>'
    '<path fill="#EA4335" d="M12 4.77c1.76 0 3.34.61 4.59 1.8l3.44-3.44C17.95 1.19 15.24 0 12 0A12 12 0 0 0 1.28 6.62l4.01 3.1C6.23 6.88 8.88 4.77 12 4.77z"/></svg>')
out['brand-apple'] = ('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">'
    '<path fill="#000000" d="M16.37 12.74c-.03-2.67 2.18-3.95 2.28-4.01-1.24-1.82-3.18-2.07-3.87-2.1-1.65-.17-3.22.97-4.05.97-.84 0-2.13-.95-3.5-.92-1.8.03-3.46 1.05-4.39 2.66-1.87 3.25-.48 8.06 1.35 10.7.89 1.29 1.96 2.74 3.36 2.69 1.35-.05 1.86-.87 3.49-.87 1.63 0 2.09.87 3.51.84 1.45-.03 2.37-1.31 3.26-2.61 1.03-1.5 1.45-2.95 1.48-3.02-.03-.01-2.84-1.09-2.92-4.33zM13.69 4.9c.74-.9 1.24-2.15 1.11-3.4-1.07.04-2.36.71-3.13 1.61-.69.79-1.29 2.06-1.13 3.29 1.19.09 2.41-.61 3.15-1.5z"/></svg>')

json.dump(out, open(OUT, 'w'), indent=0)
print(len(out), 'icons; missing:', missing)
