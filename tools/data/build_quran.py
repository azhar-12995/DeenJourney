"""Build quran.db (read-only content DB bundled with the app).

Sources (see docs/DATA_SOURCES.md):
  Arabic   : Tanzil Quran Text (Uthmani) v1.1 — tools/data/src/rs/quran-uthmani.sqlite
             (drop an official tanzil.net export with pause marks at tools/data/src/tanzil-uthmani.txt
              — "sura|aya|text" format — and it is used instead, verbatim)
  Metadata : Tanzil quran-data.xml (sura names/types/order, juz, hizb quarters, manzil, ruku, pages, sajdas)
  English  : Saheeh International (QuranEnc edition, with footnotes) — tools/data/src/rs/english_saheeh.sqlite
  Urdu     : Fateh Muhammad Jalandhry (Tanzil) via jsDelivr fawazahmed0/quran-api urd-fatehmuhammadja

Arabic text is stored VERBATIM (Tanzil licence forbids changes). A separate normalised column is used
only for search.
"""
import os, re, json, sqlite3, subprocess, xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, 'src')
OUT = os.path.join(HERE, 'out')
os.makedirs(OUT, exist_ok=True)

TANZIL_HEADER = """Tanzil Quran Text (Uthmani, version 1.1). Copyright (C) 2007-2021 Tanzil Project.
License: Creative Commons Attribution 3.0. Permission is granted to copy and distribute verbatim copies of this text,
but CHANGING IT IS NOT ALLOWED. This quran text can be used in any website or application, provided that its source
(Tanzil Project) is clearly indicated, and a link is made to tanzil.net to enable users to keep track of changes.
This copyright notice shall be included in all verbatim copies of the text."""


def fetch(url):
    r = subprocess.run(['curl', '-sS', '-m', '120', '--retry', '3', url], capture_output=True)
    if r.returncode != 0:
        raise RuntimeError(r.stderr.decode())
    return r.stdout


def arabic_text():
    official = os.path.join(SRC, 'tanzil-uthmani.txt')
    T = {}
    if os.path.exists(official):
        for line in open(official, encoding='utf8'):
            if not line.strip() or line.startswith('#'):
                continue
            s, a, t = line.rstrip('\n').split('|', 2)
            T[(int(s), int(a))] = t
        return T, 'tanzil.net export (with pause marks)'
    c = sqlite3.connect(os.path.join(SRC, 'rs', 'quran-uthmani.sqlite'))
    for s, a, t in c.execute('select sura, aya, text from quran'):
        T[(s, a)] = t
    return T, 'Tanzil v1.1 mirror (no pause marks)'


BISM = 'بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ'


def normalise(s):
    """Search key: strip diacritics/Quranic marks/tatweel, unify alef/ya/ta-marbuta forms."""
    s = re.sub(r'[ؐ-ًؚ-ٰٟۖ-ۭـ࣓-ࣿ]', '', s)
    s = s.replace('ٱ', 'ا').replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ى', 'ي').replace('ة', 'ه').replace('ؤ', 'و').replace('ئ', 'ي')
    return re.sub(r'\s+', ' ', s).strip()


def main():
    AR, ar_src = arabic_text()
    assert len(AR) == 6236, len(AR)
    meta = ET.parse(os.path.join(SRC, 'quran-data.xml')).getroot()
    suras = [s.attrib for s in meta.find('suras')]
    marks = lambda tag: [(int(x.get('sura')), int(x.get('aya'))) for x in meta.find(tag)]
    juz, quarters, manzils, rukus, pages = marks('juzs'), marks('hizbs'), marks('manzils'), marks('rukus'), marks('pages')
    sajdas = {(int(x.get('sura')), int(x.get('aya'))): x.get('type') for x in meta.find('sajdas')}

    en = sqlite3.connect(os.path.join(SRC, 'rs', 'english_saheeh.sqlite'))
    EN = {(s, a): (t, f) for s, a, t, f in en.execute('select sura, aya, translation, footnotes from translations')}
    assert len(EN) == 6236
    ur_raw = json.loads(fetch('https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/urd-fatehmuhammadja.json'))
    UR = {(v['chapter'], v['verse']): v['text'] for v in ur_raw['quran']}
    assert len(UR) == 6236, len(UR)

    order = sorted(AR)
    gid = {k: i + 1 for i, k in enumerate(order)}

    def index_of(markers, key):
        """1-based index of the last marker <= key."""
        n = 0
        for i, m in enumerate(markers):
            if m <= key:
                n = i + 1
            else:
                break
        return n

    path = os.path.join(OUT, 'quran.db')
    if os.path.exists(path):
        os.remove(path)
    db = sqlite3.connect(path)
    db.executescript("""
    PRAGMA user_version = 1;
    CREATE TABLE info (k TEXT PRIMARY KEY, v TEXT NOT NULL);
    CREATE TABLE surah (id INTEGER PRIMARY KEY, name_ar TEXT NOT NULL, name_tr TEXT NOT NULL, name_en TEXT NOT NULL,
      type TEXT NOT NULL, ayas INTEGER NOT NULL, start INTEGER NOT NULL, rev_order INTEGER NOT NULL, rukus INTEGER NOT NULL);
    CREATE TABLE ayah (id INTEGER PRIMARY KEY, sura INTEGER NOT NULL, aya INTEGER NOT NULL, juz INTEGER NOT NULL, hizb_q INTEGER NOT NULL,
      manzil INTEGER NOT NULL, ruku INTEGER NOT NULL, page INTEGER NOT NULL, sajda TEXT, ar TEXT NOT NULL, ar_norm TEXT NOT NULL,
      en TEXT NOT NULL, en_notes TEXT, ur TEXT NOT NULL);
    CREATE INDEX ayah_sa ON ayah(sura, aya);
    CREATE INDEX ayah_juz ON ayah(juz);
    CREATE INDEX ayah_page ON ayah(page);
    CREATE TABLE juz (id INTEGER PRIMARY KEY, sura INTEGER NOT NULL, aya INTEGER NOT NULL);
    CREATE VIRTUAL TABLE ayah_fts USING fts4(ar_norm, en, ur, tokenize=unicode61);
    """)
    db.executemany('INSERT INTO info VALUES (?,?)', [
        ('arabic_source', ar_src), ('arabic_license', TANZIL_HEADER),
        ('en_source', 'Saheeh International — QuranEnc.com edition (with footnotes); republished unmodified with attribution'),
        ('ur_source', 'Fateh Muhammad Jalandhry — Tanzil.net (via fawazahmed0/quran-api)'),
        ('meta_source', 'Tanzil quran-data (sura, juz, hizb, manzil, ruku, page, sajda)')])
    for s in suras:
        db.execute('INSERT INTO surah VALUES (?,?,?,?,?,?,?,?,?)', (int(s['index']), s['name'], s['tname'], s['ename'], s['type'], int(s['ayas']), int(s['start']), int(s['order']), int(s['rukus'])))
    for i, (s, a) in enumerate(juz):
        db.execute('INSERT INTO juz VALUES (?,?,?)', (i + 1, s, a))
    rows = []
    for k in order:
        s, a = k
        ar = AR[k]
        # some exports prefix the bismillah onto ayah 1 (all surahs but 1 and 9): keep the ayah text clean
        bism = AR[(1, 1)]
        if a == 1 and s not in (1, 9) and ar.startswith(bism) and len(ar) > len(bism) + 1:
            ar = ar[len(bism):].strip()
        en_t, en_f = EN[k]
        en_t = re.sub(r'^\(\d+\)\s*', '', en_t)  # QuranEnc prefixes the verse number "(n) "; the app shows its own ayah marker
        rows.append((gid[k], s, a, index_of(juz, k), index_of(quarters, k), index_of(manzils, k), index_of(rukus, k), index_of(pages, k),
                     sajdas.get(k), ar, normalise(ar), en_t, en_f or None, UR[k]))
    db.executemany('INSERT INTO ayah VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)', rows)
    db.execute("INSERT INTO ayah_fts(docid, ar_norm, en, ur) SELECT id, ar_norm, en, ur FROM ayah")
    db.execute("INSERT INTO ayah_fts(ayah_fts) VALUES('optimize')")
    db.commit()
    db.execute('VACUUM')
    db.close()
    print('quran.db', round(os.path.getsize(path) / 1e6, 2), 'MB ·', ar_src)


if __name__ == '__main__':
    main()
