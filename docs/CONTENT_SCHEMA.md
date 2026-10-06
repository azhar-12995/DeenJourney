# Deen Journey — authored content schema (v1)

All content files are UTF-8 JSON in `tools/content/`, later copied into
`composeApp/src/commonMain/composeResources/files/content/`. The app parses them with kotlinx.serialization,
so **field names and types must match exactly**. Unknown fields are ignored; missing optional fields are fine.

## Common types

- `L` — localised string object: `{"en": "...", "ur": "...", "ar": "..."}`. `en` is required; `ur` and `ar` should be
  filled (Urdu in Urdu script, never Roman Urdu, unless a field is explicitly Roman Urdu). Use honorifics:
  English "ﷺ" after the Prophet's name, "(AS)" for prophets, "(RA)" for companions; Urdu ﷺ / علیہ السلام / رضی اللہ عنہ;
  Arabic ﷺ / عليه السلام / رضي الله عنه.
- `Ref` — a source reference: `{"type": "quran"|"hadith"|"book"|"note", "label": "Sahih al-Bukhari 6094", "key": "..."}`
  - quran: `key` = `"2:153"` or a range `"43:13-14"`
  - hadith: `key` = `"bukhari:6094"` (collections: bukhari, muslim, abudawud, tirmidhi, nasai, ibnmajah, malik, nawawi, qudsi, ahmad, hakim, bayhaqi, darimi, …). Use **sunnah.com numbering**.
  - book / note: free text in `label`.
- `glyph` — one of the app's duotone glyph names: sehri iftar quran book_ribbon fasting dua tracker zakat kaaba calendar mat
  prayer_time qibla wudu ghusl salah kalima adhkar tasbih hadith akhlaq prophets names lesson progress roadmap pillars
  shahadah sawm child parent family qaida hifz tafsir bookmark note headphones mic telescope star leaf eid mosque heart
  sunrise sun sun_low sunset fajr isha palm story quiz medal bell shield help flag sync download search globe settings
  hadith_day audio lantern luggage ihram tent arafah pebbles jamarat tawaf sai scissors intention hands_wash mouth nose
  face arm head ears feet privacy_drop body_side check_all
- Every top-level item has `"review": true` until a qualified scholar has checked it, and may carry
  `"notes": "..."` for reviewers (not shown to users).

## Accuracy rules (non-negotiable)

1. Quranic Arabic must be copied **verbatim** from `tools/data/out/quran.db` (`select ar from ayah where sura=? and aya=?`).
2. Hadith must be authentic (Sahih/Hasan) and verified against the hadith-api data
   (`https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/{ara|eng|urd}-{book}/{n}.json`) — record the grade.
   If a well-known dua's exact number is uncertain, cite the collection + chapter in `label` and set `"verify": true`.
3. Never invent virtues ("fadl") or numbers. Prefer fewer, solid items over many weak ones. No weak/fabricated narrations.
4. Where the four Sunni schools differ, say so neutrally with labels (Hanafi / Shafi‘i / Maliki / Hanbali).
5. Prophets and companions are never described physically for illustration; stories stay close to the Quran and Sahih Sunnah,
   with Quran references, and avoid Isra’iliyyat.
6. Children’s versions: short sentences, gentle tone, nothing frightening.

## Files

### duas.json
```json
{"categories": [{"id": "travel", "title": L, "subtitle": L, "glyph": "luggage", "duas": ["travel-1", "..."]}],
 "duas": [{"id": "travel-1", "title": L, "arabic": "...", "translit": "...", "meaning": L, "refs": [Ref], "repeat": 1,
           "when": L, "tags": ["travel"], "review": true}]}
```
### adhkar.json
```json
{"morning": [Dhikr], "evening": [Dhikr], "after_salah": [Dhikr], "sleep": [Dhikr]}
Dhikr = {"id": "m1", "arabic": "...", "translit": "...", "meaning": L, "count": 3, "refs": [Ref], "virtue": L|null, "review": true}
```
### kalimas.json
`[{"id": "k1", "n": 1, "name": "Tayyib", "title": L, "arabic": "...", "translit": "...", "meaning": L, "refs": [Ref], "review": true}]`
(+ top-level note object `{"note": L}` is NOT allowed — put the note in `tools/content/README.md` instead.)
### names.json
`[{"n": 1, "arabic": "ٱلرَّحْمَٰنُ", "translit": "Ar-Raḥmān", "meaning": L, "explanation": L, "reflection": L, "quran": ["1:3", "59:22"], "review": true}]`
### guides.json
```json
{"wudu": Guide, "ghusl_essential": Guide, "ghusl_full": Guide, "janazah": Guide, "eid": Guide, "jumuah": Guide, "witr": Guide,
 "tahajjud": Guide, "istikharah": Guide, "taraweeh": Guide, "eclipse": Guide, "istisqa": Guide, "umrah": Guide, "hajj": Guide}
Guide = {"title": L, "subtitle": L, "intro": L, "steps": [Step], "about": [Section], "duas": ["dua-id", ...], "refs": [Ref], "review": true}
Step = {"title": L, "body": L, "glyph": "hands_wash", "arabic": "..."|null, "translit": "..."|null, "meaning": L|null,
        "fiqh": {"hanafi": L, "shafii": L, "maliki": L, "hanbali": L}|null, "when": L|null, "day": "8 Dhul Hijjah"|null}
Section = {"title": L, "body": L, "bullets": [L], "refs": [Ref]}
```
### salah.json
```json
{"postures": [{"id": "qiyam", "pose": "qiyam", "title": L, "bullets": [L], "arabic": "...", "translit": "...", "meaning": L,
               "fiqh": {"hanafi": L, "shafii": L, "maliki": L, "hanbali": L}}],
 "detail": [Section], "mistakes": [Section], "times": [Section], "review": true}
```
(`pose` ∈ takbir, qiyam, ruku, qawmah, sujood, jalsa, tashahhud, salam, dua)
### lessons.json
```json
{"tracks": [{"id": "faith", "n": 1, "title": L, "subtitle": L, "glyph": "shahadah",
  "lessons": [{"id": "faith-1", "title": L, "subtitle": L, "minutes": 4, "ages": "all"|"kids"|"adult", "points": [L], "body": L,
               "refs": [Ref], "quiz": [{"q": L, "options": [L], "answer": 1, "explain": L}], "review": true}]}]}
```
### akhlaq.json
`[{"id": "honesty", "title": L, "subtitle": L, "glyph": "check_all", "body": L, "quran": {"text": L, "ref": Ref}, "hadith": {"text": L, "ref": Ref},
   "reflect": [L], "practice": [L], "scenario": {"q": L, "options": [L], "answer": 1, "feedback": L}, "review": true}]`
### prophets.json
`[{"id": "nuh", "name": L, "arabic": "نوح", "honorific": "AS", "vignette": "nuh"|"adam"|"ibrahim"|"musa"|"muhammad"|"yunus"|"yusuf"|"generic",
   "era": "early"|"perseverance"|"guidance"|"later", "summary": L, "quran": ["71:1-28", "11:25-49"],
   "adults": [{"title": L, "body": L, "refs": [Ref]}], "children": [{"title": L, "body": L}], "lessons": [L], "review": true}]`
### seerah.json
`{"timeline": [{"year": 570, "hijri": "53 BH", "title": L, "body": L, "refs": [Ref]}], "chapters": [{"title": L, "body": L, "refs": [Ref]}], "review": true}`
### hadith_daily.json
`[{"id": "bukhari-1", "collection": "bukhari", "number": 1, "book": L, "arabic": "...", "en": "...", "ur": "...", "narrator": L,
   "grade": "Sahih"|"Hasan", "short": L, "explanation": L, "topic": "intention", "review": true}]`
(`short` = one-line headline shown on cards; `en`/`ur`/`arabic` copied from hadith-api, may be trimmed to the matn with "…".)
### reflections.json (Aaj ki achi baat)
`[{"id": "r1", "headline_ru": "Choti choti nekian, bari khushiyan", "body_ru": "...", "headline": L, "body": L, "quote": L, "ref": Ref, "deed": L, "deed_ru": "...", "kids": L, "review": true}]`
### kids.json
`{"greetings": [Card], "manners": [Card], "first_words": [Card]}` with `Card = {"id", "title": "Assalamu alaikum", "arabic": "...", "meaning": L, "avatar": "boy"|"girl"|"man"|"woman"|"grandpa"|"grandma", "tip": L}`
### events.json
`[{"id": "ramadan-start", "month": 9, "day": 1, "title": L, "body": L, "kind": "month"|"fast"|"eid"|"night"|"observance", "disputed": false, "refs": [Ref], "review": true}]`
### pillars.json
`[{"id": "shahadah", "n": 1, "title": L, "subtitle": L, "glyph": "shahadah", "arabic": "الشهادة", "body": L, "refs": [Ref], "link": "lesson:faith-1"|"screen:H05"}]`
### fasting.json, zakat.json, hajj_extra.json, faq.json
`fasting.json` / `zakat.json` = `{"sections": [Section], "review": true}`.
`hajj_extra.json` = `{"checklist": [{"group": L, "items": [{"id", "text": L, "sub": L|null}]}], "talbiyah": {"arabic", "translit", "meaning": L, "ref": Ref}}`.
`faq.json` = `[{"q": L, "a": L, "link": "screen:E02"|null}]`.
