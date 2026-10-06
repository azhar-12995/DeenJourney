"""Download OFL fonts from google/fonts and write static instances into composeResources/font/.
(Static files work identically on Android and iOS; variable-font axes are pinned with fontTools.instancer.)"""
import os, subprocess
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
DL = os.path.join(HERE, 'src')
OUT = os.path.join(ROOT, 'composeApp', 'src', 'commonMain', 'composeResources', 'font')
os.makedirs(DL, exist_ok=True)
os.makedirs(OUT, exist_ok=True)
BASE = 'https://raw.githubusercontent.com/google/fonts/main/ofl/'

SPEC = [
    ('playfairdisplay/PlayfairDisplay[wght].ttf', 'playfair', {'semibold': {'wght': 600}, 'bold': {'wght': 700}}),
    ('inter/Inter[opsz,wght].ttf', 'inter', {'regular': {'wght': 400, 'opsz': 14}, 'medium': {'wght': 500, 'opsz': 14}, 'semibold': {'wght': 600, 'opsz': 14}, 'bold': {'wght': 700, 'opsz': 14}}),
    ('amiri/Amiri-Regular.ttf', 'amiri_regular', None),
    ('amiri/Amiri-Bold.ttf', 'amiri_bold', None),
    ('amiriquran/AmiriQuran-Regular.ttf', 'amiri_quran', None),
    ('notonastaliqurdu/NotoNastaliqUrdu[wght].ttf', 'nastaliq', {'regular': {'wght': 400}, 'semibold': {'wght': 600}, 'bold': {'wght': 700}}),
    ('notosansarabic/NotoSansArabic[wdth,wght].ttf', 'notoarabic', {'regular': {'wght': 400, 'wdth': 100}, 'medium': {'wght': 500, 'wdth': 100}, 'semibold': {'wght': 600, 'wdth': 100}, 'bold': {'wght': 700, 'wdth': 100}}),
]

for path, stem, inst in SPEC:
    local = os.path.join(DL, os.path.basename(path))
    if not os.path.exists(local):
        subprocess.run(['curl', '-sS', '-m', '180', '-L', '-o', local, BASE + path.replace('[', '%5B').replace(']', '%5D')], check=True)
    if inst is None:
        TTFont(local).save(os.path.join(OUT, stem + '.ttf'))
        continue
    for wname, axes in inst.items():
        f = TTFont(local)
        static = instancer.instantiateVariableFont(f, axes, updateFontNames=True)
        static.save(os.path.join(OUT, f'{stem}_{wname}.ttf'))
for license_dir in ('playfairdisplay', 'inter', 'amiri', 'notonastaliqurdu'):
    subprocess.run(['curl', '-sS', '-m', '60', '-L', '-o', os.path.join(HERE, f'OFL-{license_dir}.txt'), BASE + license_dir + '/OFL.txt'])
for n in sorted(os.listdir(OUT)):
    print(n, round(os.path.getsize(os.path.join(OUT, n)) / 1024), 'KB')
