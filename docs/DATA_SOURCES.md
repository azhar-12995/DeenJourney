# Deen Journey: data sources (researched and verified 2026-10-03)

Scope: the authentic data sources the app should use, with licences, keys and verification status.
Nothing here was bulk-downloaded. Small proof samples (each under 20 KB) are in `tools/data/samples/`.

## How to read the "Verified" column

| Mark | Meaning |
|---|---|
| **200 ✔** | Fetched with `curl` from the dev machine on 2026-10-03 and got that HTTP code. Samples saved. |
| **BLOCKED** | The corporate Fortinet proxy (CA `FG6H0ETB20906736`) re-signs TLS for this host. Windows does not trust that CA, so curl fails with error 60 (`SEC_E_UNTRUSTED_ROOT`). WebFetch fails the same way. **Not verified.** The facts shown come from web search or GitHub mirrors and are labelled that way. |
| **GH ✔** | Facts read from the project's own GitHub repository or docs source over `raw.githubusercontent.com`/`api.github.com`, which are reachable. |

Hosts BLOCKED from this network on 2026-10-03:
- **Quran text and Quran Foundation:** tanzil.net, api.alquran.cloud, cdn.islamic.network, api-docs.quran.foundation, apis.quran.foundation, oauth2.quran.foundation, verses.quran.foundation, dev-console.quran.foundation
- **Recitation audio:** everyayah.com, mp3quran.net, quranicaudio.com, download.quranicaudio.com, verses.quran.com, audio.qurancdn.com
- **Hadith, duas and translations:** sunnah.com, api.sunnah.com, hisnmuslim.com, quranenc.com
- **Maps and places:** www.geonames.org, download.geonames.org, openfreemap.org, tiles.openfreemap.org
- **Other sites:** aladhan.com (the website; the API works), www.wikidata.org
- **Other failures:** commons.wikimedia.org reset the connection, and developers.google.com timed out.

`api.gold-api.com` has a genuine Let's Encrypt certificate, but Windows schannel could not reach the revocation (CRL) server through the proxy (`CRYPT_E_REVOCATION_OFFLINE`). It was verified with `curl --ssl-revoke-best-effort`, which still validates the certificate chain. Phones are not affected.

---

## 1. Summary table

| # | Feature | Recommended source | Endpoint / URL | Key / account? | Licence / terms (exact where quoted) | Verified 2026-10-03 | Offline or online |
|---|---|---|---|---|---|---|---|
| 1a | Quran Arabic text (Uthmani/Hafs) | **Tanzil Uthmani text** | tanzil.net/download (accept terms page) | No key. Download needs a terms click, which the owner or dev does. | "Creative Commons Attribution 3.0 … Permission is granted to copy and distribute verbatim copies of this text, but CHANGING IT IS NOT ALLOWED. … can be used in any website or application, provided its source (Tanzil Project) is clearly indicated, and a link is made to tanzil.net … This copyright notice shall be included in all verbatim copies" (licence header as mirrored in GitHub `q-ran/quran/docs/about.md`; current header per web search reads "Version 1.1, Copyright (C) 2007-2021 Tanzil Project") | BLOCKED (tanzil.net). Licence text GH ✔ via mirror | **Bundle** |
| 1b | Quran Arabic (alt.: KFGQPC "QPC Hafs") | QUL (Tarteel) "QPC Hafs script – Ayah by Ayah" (resource 86) | https://qul.tarteel.ai/resources/quran-script/86 | **QUL login is required to download.** The download button opens `/users/sign_in`. | QUL FAQ: "you can use QUL data in commercial projects. However, please review the licensing terms for each resource." No per-resource licence is shown on the page. The text belongs to King Fahd Complex. | 200 ✔ (pages) | Bundle, if rights are cleared |
| 1c | Arabic Quran font | **Amiri Quran** (pair with Tanzil text) | github.com/aliftype/amiri, Google Fonts `ofl/amiriquran` | No | SIL OFL 1.1. Amiri release 1.003 (GitHub, 2025-06-13). | GH ✔ (METADATA.pb `license: "OFL"`) | Bundle |
| 1d | Arabic Quran font (alt.) | KFGQPC *UthmanicHafs1Ver18* (pair **only** with QPC Hafs text) | QUL font 245; QF CDN `verses.quran.foundation/fonts/quran/hafs/uthmanic_hafs/UthmanicHafs1Ver18.ttf` | QF route: active QF Developer Console account | KFGQPC licence (web search, ScanCode DB): "may not be reproduced or modified without the express written approval of King Fahd Glorious Quran Printing Complex". The QF Developer Terms §3.1 allow bundling font files obtained from QF "if the Developer maintains an active account in the Developer Console and credits Quran Foundation". | QUL page 200 ✔. CDN BLOCKED. | Bundle, only with permission |
| 2a | EN translation: Saheeh International | QF API translation **id 20** at runtime. Bundle only with written permission. | `/api/v4/verses/by_key/{k}?translations=20` | QF credentials (see 3) | © Abul-Qasim Publishing House 1997 / Al-Muntada Al-Islami 2004, "All rights reserved" (web search). Tanzil: "for non-commercial purposes only, and if used otherwise, you need to obtain necessary permission from the translator or the publisher" (web search, BLOCKED). QuranEnc (`english_saheeh`, now labelled "Noor International Center") allows re-publishing with no modification, attribution and version number (web search, BLOCKED). | legacy v4 200 ✔ | Online (QF) or bundle with permission |
| 2b | UR translation: Fateh Muhammad Jalandhry | QF **id 234**; Tanzil `ur.jalandhry`; jsDelivr `fawazahmed0/quran-api` `urd-fatehmuhammadja` (sourced from tanzil.net) | e.g. https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/urd-fatehmuhammadja/1/1.json | No (jsDelivr) | The translator was born in 1864 and the translation dates from the early 20th century, so it is probably public domain in Pakistan (life+50). That is **not legally confirmed**: the death year could not be verified. The **digital edition** is distributed by Tanzil under its "non-commercial only" translation terms. | jsDelivr 200 ✔; QF v4 200 ✔ | Bundle for a non-commercial app; commercial needs confirmation |
| 2c | Other languages | QF ids (verified list): id 33 (Kemenag), tr 77 (Diyanet), bn 161 (Taisirul), fr 31 (Hamidullah), ms 39 (Basmeih), ru 79 (Abu Adel), es 83 (Isa Garcia), de 27 (Bubenheim), zh 56 (Ma Jian), hi 122 (al-Umari), fa 135 (IslamHouse). EN alternatives: 19 Pickthall, 22 Yusuf Ali, 84 Taqi Usmani, 85 Abdel Haleem. | `/api/v4/resources/translations` (126 translations) | QF credentials | Each translation has its own rights holder (QF Terms §2.2: "Certain QF Content resources have their own underlying rights holders"). | 200 ✔ | Online |
| 3 | Quran.com / Quran Foundation API | **QF Content API v4** (OAuth2) | Prod: `https://apis.quran.foundation/content/api/v4/*`, token `https://oauth2.quran.foundation/oauth2/token`. Legacy: `https://api.quran.com/api/v4/*` | **Yes for the new API:** `client_id` + `client_secret`, `grant_type=client_credentials&scope=content`, headers `x-auth-token` + `x-client-id`, token 3600 s. Legacy needs none today. | QF Developer Terms (updated 2026-09-26), see §3 below | Legacy **200 ✔ with no auth**. New API BLOCKED. Docs GH ✔. | Online, or Content Sync (7-day re-sync) |
| 4a | Per-ayah audio | QF recitations (12 reciters, e.g. id 7 = Alafasy). Fallbacks: everyayah, islamic.network | QF `/api/v4/recitations/7/by_chapter/1` returns `Alafasy/mp3/001001.mp3` (relative). `everyayah.com/data/Alafasy_128kbps/002012.mp3`, `cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3` | No (legacy QF, everyayah, islamic.network) | alquran.cloud terms (fetched 200 ✔): "Recitations are licensed to us by the reciters or their estates for free, non-commercial redistribution … You may bundle them into a commercial product, but … copyrights lie with the reciters and they may ask you to remove the conent." QF Terms: "Certain recordings may have their own rights holders". | QF metadata 200 ✔; all MP3 hosts BLOCKED | **Stream**, with user-initiated download cache |
| 4b | Full-surah audio | QF chapter recitation, e.g. `download.quranicaudio.com/qdc/mishari_al_afasy/murattal/1.mp3`; mp3quran.net API v3 | `/api/v4/chapter_recitations/7/1`; `https://mp3quran.net/api/v3/reciters?language=eng` | No | Same as 4a. mp3quran terms not readable (BLOCKED). | QF metadata 200 ✔; MP3/mp3quran BLOCKED | Stream / cache |
| 5a | Hadith (AR + EN + UR) | **fawazahmed0/hadith-api** via jsDelivr | `https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions.json`, `/editions/{eng|urd|ara}-{book}/{n}.json` | No | Repo licence **The Unlicense** (public-domain dedication of the repo). This does **not** clear third-party translation copyrights: eng-bukhari author "Muhsin Khan", most others "Unknown"; sources include sunnah.com and others (References.md). | 200 ✔ | Bundle (EN about 15.8 MB, UR about 30 MB raw text), or fetch per book |
| 5b | Hadith (alt.) | sunnah.com API | `https://api.sunnah.com/v1/*`, header `X-API-Key` | **Yes.** Request it by opening a **public** GitHub issue on `sunnah-com/api` (the template asks for name and email, purpose, req/s and req/day, and whether an offline dump is preferred). | Granted case by case | Template GH ✔; API BLOCKED | Online or offline dump |
| 5c | Hadith (alt.) | hadithapi.com | `https://hadithapi.com/api/hadiths?apiKey=…` | **Yes** (site registration). Without a key: `403 {"message":"API key is required."}` | Site says free (web search) | 403 ✔ | Online |
| 6 | Tafsir | **QF API** at runtime: en 169 Ibn Kathir (Abridged), en 168 Ma'arif al-Qur'an, en 817 Tazkirul Quran, ur 160 Ibn Kathir, ur 159 Bayan ul Quran, ur 157 Fi Zilal, ur 818 Tazkir ul Quran, ar 14/15/16/90/91/93/94 | `/api/v4/tafsirs/{id}/by_ayah/{key}` | QF credentials | Copyrighted editions: display only within QF Terms; **no bundling**. spa5k/tafsir_api (MIT code) holds scraped copies; see §6. | 200 ✔ (legacy) | Online / Content Sync |
| 6b | Word-by-word (EN + UR) | QF words | `/api/v4/verses/by_key/1:1?words=true&language=ur` | QF credentials | QF Terms | 200 ✔ (Urdu glosses returned) | Online / Content Sync |
| 7a | Prayer times (offline) | **Adhan KMP** `com.batoulapps.adhan:adhan2:0.0.7` | repo1.maven.org/maven2/com/batoulapps/adhan/adhan2/ | No | **MIT** (GitHub repo licence) | 200 ✔ (maven-metadata `release 0.0.7`, lastUpdated 2026-06-13) | Bundle (library) |
| 7b | Prayer times (online fallback) | Aladhan API | `https://api.aladhan.com/v1/timingsByCity?city=Karachi&country=Pakistan&method=1` (302 to `/v1/timingsByCity/03-10-2026?...`, so follow redirects) | No | Rate-limit headers: `RateLimit-Limit: 12` per second per IP. aladhan.com terms page BLOCKED (not read). | 302 → 200 ✔ | Online |
| 8 | Hijri calendar | Platform Umm al-Qura calendars (Android ICU `IslamicCalendar` UMALQURA, iOS `Calendar(.islamicUmmAlQura)`) plus a local ±2-day user offset. Aladhan `gToH` online. | `https://api.aladhan.com/v1/gToH/03-10-2026?calendarMethod=UAQ` | No | UAQ validity "1356 AH (14 March 1937 CE) to 1500 AH (16 November 2077 CE)" (Aladhan response) | 200 ✔ (gives 22 Rabīʿ al-thānī 1448) | Offline (platform) / online |
| 9 | Asma ul Husna | Aladhan, as seed data | `https://api.aladhan.com/v1/asmaAlHusna` | No | No licence stated in the response. Names come from the hadith (public domain). The EN meanings are Aladhan's. | 200 ✔ (99 entries: `name`, `transliteration`, `number`, `en.meaning` only, **no Urdu, no audio**) | Bundle (curated copy) |
| 10 | Duas / adhkar | **Author our own set** from Quran and hadith Arabic plus references. Seed: `Seen-Arabic/Morning-And-Evening-Adhkar-DB` | raw.githubusercontent.com/Seen-Arabic/Morning-And-Evening-Adhkar-DB/main/result/en.json (93,738 B) | No | MIT ("Copyright (c) 2024 Seen Arabic"). The English translations' origin is unclear. Other repos have **no licence** (osamayy/azkar-db, rn0x/hisn_almuslim_json (archived), wafaaelmaandy/Hisn-Muslim-Json) or MIT (asellam/HisnElMuslim). hisnmuslim.com API BLOCKED. | 206 ✔ (range sample) | Bundle (authored) |
| 11a | Gold/silver price | **gold-api.com** (keyless) | `https://api.gold-api.com/price/XAU`, `/price/XAG` (USD per troy oz) | **No** | Terms (eff. 2026-09-10) §9: "Commercial use of the API is always permitted, including use in … mobile apps". §4: "If you send multiple requests per second or otherwise abuse the Service, your IP address may be temporarily or permanently banned." Pricing page: "UNLIMITED requests to Real-time asset price API", history 10 req/h free. | 200 ✔ (XAU 4141.80, XAG 60.524 USD/oz at 07:57Z). `/price/XAU/PKR` gives **404 "Currency not found"**; EUR/GBP/INR work. | Online, cache last value |
| 11b | Currency conversion | **open.er-api.com** (ExchangeRate-API Open Access) | `https://open.er-api.com/v6/latest/USD` | No | "requires attribution … welcome to cache … personal or commercial currency conversion … not allowed to re-distribute it". Required link: `<a href="https://www.exchangerate-api.com">Rates By Exchange Rate API</a>`. Daily update; HTTP 429 on abuse, 20-minute ban. | 200 ✔ (166 currencies, **PKR 276.817**, SAR 3.75, AED 3.6725, INR 96.37) | Online, cache 24 h |
| 11c | Metals (keyed alternatives) | goldapi.io, metals.dev | `www.goldapi.io/api/XAU/USD`, `api.metals.dev/v1/latest` | **Yes** | Vendor terms | 403 "No API Key provided" ✔ / 401 ✔ | Online |
| 12 | City search (worldwide) | GeoNames `cities15000` | `https://download.geonames.org/export/dump/cities15000.zip` | No | CC BY 4.0, credit GeoNames (web search). About 25k+ cities; includes lat/lng, country, **IANA timezone**, alternate names. | BLOCKED | Bundle (pre-processed) |
| 13a | Qibla map tiles | **OpenFreeMap** | style e.g. `https://tiles.openfreemap.org/styles/liberty`; TileJSON `https://tiles.openfreemap.org/planet/latest` | **No** | README (GH ✔): "no limits on the number of map views or requests. There's no registration, no user database, no API keys, and no cookies." "Attribution is required. If you are using MapLibre, they are automatically added". Project MIT; data © OpenStreetMap (ODbL); OpenMapTiles design CC BY 4.0. | GH ✔; tiles BLOCKED | Online |
| 13b | Map SDK (KMP) | **MapLibre Compose** `org.maplibre.compose:maplibre-compose:0.19.0` | repo1.maven.org/maven2/org/maplibre/compose/maplibre-compose/ | No | BSD-3-Clause (GitHub) | 200 ✔ (release 0.19.0, 2026-10-01) | Library |
| 13c | (Alt.) Google Maps SDK | Google Maps SDK for Android/iOS | – | **Yes**: API key + Cloud billing account | Google Maps Platform ToS | Not verified (timeout) | Online |
| 14 | Noorani Qaida audio | **No properly licensed source found.** Commission recordings. | – | – | Commons is blocked from here. One Commons IPA clip checked via the en.wikipedia API (`Voiced pharyngeal fricative.ogg`, CC BY-SA 3.0, Peter Isotalo) is a phonetics sample, not qari quality. | partial ✔ | Bundle (own recordings) |
| 15 | UI fonts | Amiri, Amiri Quran, Noto Naskh Arabic, Noto Nastaliq Urdu, Noto Sans Arabic, Scheherazade New, Playfair Display, Lora, Inter, Cormorant Garamond | github.com/google/fonts `ofl/<family>` | No | **SIL OFL 1.1** (each `METADATA.pb` says `license: "OFL"`, `OFL.txt` present) | 200 ✔ | Bundle |

---

## 2. Quran text, fonts and translations: details

**Text and font pairing (important):**
- **Tanzil Uthmani** text uses standard Unicode Quranic marks. Render it with **Amiri Quran** (OFL); Noto Naskh Arabic or Scheherazade New also work. No permission is needed, but do not change a single character. Show "Quran text: Tanzil Project, tanzil.net" with a link, and keep the licence header in the bundled file.
- **QPC Hafs** text is only correct with the KFGQPC **UthmanicHafs** font. QF's `font_family: "qpc-hafs"` and font guide say to use `UthmanicHafs1Ver18`. The jsDelivr copy `ara-quranuthmanihaf` notes that it is "Version 13" and that three characters were replaced (U+0656→U+08F2 etc.), so do not mix copies. That font is proprietary (KFGQPC). Use it only under QF's font permission (active Dev Console account + credit) or with written KFGQPC approval.
- **Recommendation:** ship v1 with **Tanzil Uthmani + Amiri Quran** because it needs no permission. Add a QPC/KFGQPC "Madani Mushaf" script option later, once the font permission is settled.

**Translation shown in the design:**
- Board 4 has a design error. Screen 6 ("Tafsir & Word Meanings", Translation Reference card) says *"Sahih International — Translated by: Dr. Mohammad T. Al-Hilali and Dr. Muhammad Muhsin Khan"*. **That is wrong.** Saheeh International was translated by Umm Muhammad (Emily Assami), Mary Kennedy and Amatullah Bantley. Hilali–Khan is a different translation (QF id 203). Fix the copy before build.
- The same screen credits "Tafsir Ibn Kathir (Darussalam)". That is the copyrighted Darussalam abridged English text (QF id 169). It can be shown via the QF API under QF Terms, but it **must not be bundled**.

**Licence status of the requested texts:**

| Text | Status | Safe path |
|---|---|---|
| Saheeh International (EN) | Copyrighted, "All rights reserved" (Abul-Qasim 1997 / Al-Muntada 2004) | Non-commercial app: Tanzil terms allow it. Commercial app: show it via the QF API, or get permission (Al-Muntada / Noor International) or follow QuranEnc's re-publishing conditions. |
| Jalandhry (UR) | Probably public domain by age (translator b. 1864) but not legally confirmed. The Tanzil digital edition is "non-commercial only". | Non-commercial: bundle from Tanzil. Commercial: QF id 234, or get a legal or scholar opinion on public-domain status. |
| Ibn Kathir abridged (EN, Darussalam) | Copyrighted (Darussalam; abridged under Safiur-Rahman al-Mubarakpuri) | QF API display only, never bundle |
| Ma'ariful Qur'an | **The Urdu original is not on QF.** Only the English translation is (id 168, copyrighted). | QF display; Urdu needs a licence from the publisher |

The fawazahmed0 `quran-api` (Unlicense repo) also carries `eng-ummmuhammad` (= Saheeh International) and `urd-fatehmuhammadja`, both sourced from tanzil.net. The repo licence does not override Tanzil's translation terms.

## 3. Quran Foundation API: verified facts

- `GET https://api.quran.com/api/v4/chapters` returned **200 with no auth** on 2026-10-03 (Cloudflare cache HIT, `Age: 256251`, header `x-qf-origin: qf-nonfaceing-data-legacy-api-quran`). Other legacy endpoints also returned 200: translations, tafsirs, recitations, verses, words, tafsir text and audio metadata.
- QF's own docs call it *"the older unauthenticated API at `https://api.quran.com/api/v4/...`"* and give a migration guide. **Do not build on the legacy host**: it is labelled legacy and can be switched off with about 30 days' notice (Terms §4).
- **New API:** OAuth2 **Client Credentials**, `scope=content`, token at `{auth}/oauth2/token`, lifetime 3600 s, no refresh token. Send `x-auth-token` and `x-client-id` on every call.
  - Pre-live: `prelive-oauth2.quran.foundation` / `apis-prelive.quran.foundation`. Its data has **only surahs 1–2**.
  - Production: `oauth2.quran.foundation` / `apis.quran.foundation`. Use it after production access is approved.
- **Mobile apps:** "A browser or mobile app must not embed a Content API `client_secret`. If it needs Content API data, call your own backend". So a backend proxy is needed, for example a Firebase Cloud Function (Blaze plan). Credentials come from https://dev-console.quran.foundation (Backend/server app type).
- **Terms (Developer Terms, last updated 2026-09-26, GH ✔):**
  - Commercial and freemium apps are allowed "provided that QF Content is displayed only as part of the Application's end-user experience; QF Content and raw API data are not sold, sublicensed, or redistributed".
  - "Cache or store QF Content longer than **1 week**" is prohibited unless it comes through Content Sync, which needs a re-sync at least every 7 days.
  - "The Content Sync storage exception does not itself authorize distributing a **prepackaged database or build-time bundle** of QF Content."
  - Required attribution: "Quran data provided by Quran Foundation", plus credit for each translation, tafsir and reciter.
  - The app must publish a Privacy Policy and Terms of Use.
  - Fonts and Mushaf images may be bundled with an active Dev Console account and credit.
- **Available content (verified via legacy API):**
  - 126 translations; tafsirs as listed in row 6.
  - Word-by-word: English (default) and **Urdu** (`language=ur`, e.g. 1:1 → "ساتھ نام / اللہ کے / …").
  - Word audio: `wbw/002_255_001.mp3`.
  - 12 ayah reciters: ids 1–12, including Alafasy 7, Husary 6/12, Minshawi 8/9, AbdulBaset 1/2, Sudais 3, Shatri 4, Shuraym 10.
- **Key needed?** Yes for production use (client id + secret, held server-side).

## 4. Recitation audio

- Every MP3 host is **BLOCKED** from this network. Developers must test audio on a non-proxied network or get the proxy allow-listed.
- **Recommended:** stream audio and let users download it on demand (Downloads screen). Do not ship audio inside the app binary. Credit each reciter by name.
- **Rights:** reciters keep copyright. alquran.cloud says commercial bundling is possible "but … they may ask you to remove" the content. Web search found no formal licence on everyayah.com beyond a link-back request for timing files.
- **URL patterns:**
  - everyayah: `data/<Reciter_bitrate>/SSSAAA.mp3`
  - islamic.network: `quran/audio/<bitrate>/<edition>/<globalAyahNumber>.mp3` (262 = 2:255)
  - QF: relative `Alafasy/mp3/001001.mp3`, and chapter files on `download.quranicaudio.com/qdc/...`

## 5. Hadith: verified facts (fawazahmed0/hadith-api@1)

**Collections:**

| Book | English | Urdu | Arabic | Grades in data |
|---|---|---|---|---|
| bukhari | ✔ (Muhsin Khan) | ✔ | ✔ | empty (`[]`) |
| muslim | ✔ | ✔ | ✔ | empty |
| abudawud | ✔ | ✔ | ✔ | ✔ Al-Albani, Muhyi al-Din Abd al-Hamid, al-Arna'ut, Zubair Ali Zai |
| tirmidhi | ✔ | ✔ | ✔ | ✔ Shakir, Al-Albani, Bashar Awad, Zubair Ali Zai |
| nasai | ✔ | ✔ | ✔ | ✔ |
| ibnmajah | ✔ | ✔ | ✔ | ✔ |
| malik | ✔ | ✔ | ✔ | ✔ (Salim al-Hilali) |
| nawawi (40) | ✔ | ✘ | ✔ | – |
| qudsi (40) | ✔ | ✘ | ✔ | – |
| dehlawi (40) | ✔ | ✘ | ✔ | – |

So **7 collections have Urdu**: Bukhari, Muslim, Abu Dawud, Tirmidhi, Nasa'i, Ibn Majah and Muwatta Malik.

**Data quality problems found (needs QA and scholar review):**
- `urd-bukhari/1` begins "کو حمیدی نے…", so the opening "ہم" is missing.
- `eng-muslim/2` has empty text (numbering mismatch between Arabic and English).
- Most Urdu and English translators are listed as "Unknown".

**Usage notes:**
- The README warns that clients should include a fallback mechanism (`.min.json` ⇄ `.json`, and alternate CDNs).
- Pin the version: the tafsir_api README warns that jsDelivr caches moved tags for up to a year.
- Sizes from GitHub tree (raw text): English total about 15.8 MB; Urdu about 30 MB; `info.json` 10.3 MB.

## 6. Tafsir: spa5k/tafsir_api (jsDelivr, 123 editions, MIT code)

- **Verified:** `editions.json` 200; `ur-tafseer-ibn-e-kaseer/1/1.json` 200; `en-tafsir-al-mukhtasar/1/1.json` 200.
- **The texts are scraped** from quran.com, altafsir.com and QUL. The MIT licence covers the code only.
- **Copyrighted commercial translations (do not bundle):**
  - `en-tafisr-ibn-kathir` (id 35/169): Darussalam abridged
  - `en-tafsir-maarif-ul-quran`: Maktaba-e-Darul-Uloom Karachi
  - The altafsir.com English set (Royal Aal al-Bayt Institute / Fons Vitae translations): `en-al-jalalayn`, `en-tafsir-al-tustari`, `en-al-qushairi-tafsir`, `en-kashani-tafsir`, `en-asbab-al-nuzul-by-al-wahidi`, `en-tafsir-ibn-abbas`, `en-kashf-al-asrar-tafsir`
  - `ur-tafseer-ibn-e-kaseer`, `ur-tafsir-bayan-ul-quran` (Anjuman Khuddam-ul-Quran), and the Tazkirul Quran en/ur (CPS/Goodword)
- **Lower risk:** classical Arabic texts (Ibn Kathir, Tabari, Qurtubi, Baghawi, Jalalayn), although modern critical editions may claim rights. The "Al-Mukhtasar" series (Tafsir Center for Quranic Studies) has terms that could not be read.
- **Recommendation:** use QF tafsir at runtime (licensed display) and keep tafsir_api only as a dev reference.

## 7. Prayer times: Adhan KMP

**Library facts:**
- Coordinates: `implementation("com.batoulapps.adhan:adhan2:0.0.7")`. Released 2026-06-13 on Maven Central.
- **Use repo1 metadata, not search:** `search.maven.org` is stale and still shows 0.0.5.
- 0.0.7 was built with Kotlin **2.4.0** and depends on `kotlinx-datetime` **0.8.0** and `kotlin-stdlib` 2.4.0. If the project uses Kotlin below 2.4, use **0.0.6** (2025-10-12, Kotlin 2.2.20, kotlinx-datetime 0.7.1).
- Targets: Android uses the `adhan2-jvm` artifact, plus iosArm64, iosSimulatorArm64, iosX64, macOS, watchOS, Linux, mingw, js and wasm-js.
- The README notes that on Android, kotlinx-datetime needs **minSdk 26 or core library desugaring**.
- **Methods:** `MUSLIM_WORLD_LEAGUE`, `EGYPTIAN`, `KARACHI`, `UMM_AL_QURA` (add +30 min Isha in Ramadan), `DUBAI`, `MOON_SIGHTING_COMMITTEE`, `NORTH_AMERICA` (ISNA, "not recommended"), `KUWAIT`, `QATAR`, `SINGAPORE`, `TURKEY`, `OTHER`.
- **Other settings:** `Madhab.SHAFI` / `HANAFI`. HighLatitudeRule `MIDDLE_OF_THE_NIGHT`, `SEVENTH_OF_THE_NIGHT`, `TWILIGHT_ANGLE`. Also `Qibla(coordinates).direction`, `SunnahTimes` (middle and last third of night), `prayerAdjustments`, `Rounding` and `Shafaq`.
- **Gap:** `CalculationParameters` has **no maghrib angle**. Tehran (Aladhan 7) and Jafari (0) cannot be reproduced exactly; approximate them with Maghrib minute adjustments, or use the Aladhan online fallback.
- Methods missing from Adhan but available on Aladhan (`/v1/methods`, verified) can be built as custom `CalculationParameters(fajrAngle, ishaAngle, ishaInterval)`: GULF 8, FRANCE 12, RUSSIA 14, JAKIM 17, TUNISIA 18, ALGERIA 19, KEMENAG 20, MOROCCO 21, PORTUGAL 22, JORDAN 23.

**Default method by country** (customary practice; let the user override; confirm locally):

| Countries (ISO) | Aladhan id / Adhan | Fajr / Isha | Asr default |
|---|---|---|---|
| PK, IN, BD, AF, LK, NP | 1 KARACHI / `KARACHI` | 18 / 18 | **Hanafi** |
| SA, YE | 4 MAKKAH / `UMM_AL_QURA` | 18.5 / 90 min | Shafi |
| AE | 16 DUBAI / `DUBAI` | 18.2 / 18.2 | Shafi |
| KW | 9 / `KUWAIT` | 18 / 17.5 | Shafi |
| QA | 10 / `QATAR` | 18 / 90 min | Shafi |
| BH, OM | 8 GULF (custom 19.5 / 90 min) or `UMM_AL_QURA` | – | Shafi |
| EG, SD, LY, SY, LB, IQ, PS | 5 / `EGYPTIAN` | 19.5 / 17.5 | Shafi (Hanafi for SY/IQ users who choose it) |
| JO | 23 JORDAN (custom 18 / 18, Maghrib +5) | – | Shafi |
| TR, AZ (Sunni), Balkans (BA, AL, XK, MK) | 13 / `TURKEY` | 18 / 17 | **Hanafi** |
| UZ, TJ, KZ, KG, TM | 3 / `MUSLIM_WORLD_LEAGUE` (or regional muftiate) | 18 / 17 | **Hanafi** |
| IR | 7 TEHRAN (custom; online fallback) | 17.7 / 14 | Jafari timings |
| MY, BN | 17 JAKIM (custom 20 / 18) | – | Shafi |
| SG | 11 / `SINGAPORE` | 20 / 18 | Shafi |
| ID | 20 KEMENAG (custom 20 / 18) | – | Shafi |
| MA | 21 MOROCCO (custom 19 / 17) | – | Shafi (Maliki) |
| DZ | 19 ALGERIA (custom 18 / 17) | – | Shafi (Maliki) |
| TN | 18 TUNISIA (custom 18 / 18) | – | Shafi |
| RU | 14 RUSSIA (custom 16 / 15) | – | Hanafi |
| FR | 12 FRANCE (custom 12 / 12) or MWL | – | Shafi |
| PT | 22 PORTUGAL (custom) | – | Shafi |
| US, CA | 2 ISNA / `NORTH_AMERICA`, or 15 / `MOON_SIGHTING_COMMITTEE` (Adhan's recommendation) | 15 / 15 | Shafi (Hanafi option) |
| GB, IE, rest of EU, AU, NZ, others | 3 / `MUSLIM_WORLD_LEAGUE` + high-latitude rule (`SEVENTH_OF_THE_NIGHT` / `TWILIGHT_ANGLE`) above about 48° | 18 / 17 | Shafi |

**Aladhan online fallback:**
- Verified Karachi method 1 (03 Oct 2026): Fajr 05:09, Sunrise 06:25, Dhuhr 12:21, Asr 15:45, Maghrib 18:17, Isha 19:33.
- The API answers with a 302 redirect to a dated URL, so the HTTP client must follow redirects.
- Rate limit is 12 requests per second per IP. Qibla endpoint verified: `/v1/qibla/24.8607/67.0011` → 267.74°.

## 8. Hijri calendar

- **Aladhan check:** `gToH/03-10-2026` gives **22-04-1448 (Rabīʿ al-thānī)**. The default method is `HJCoSA`; `calendarMethod=UAQ` gives the same result.
- **Aladhan calendar methods:** `UAQ`, `HJCoSA` (Umm al-Qura adjusted by Saudi High Judiciary sightings), `DIYANET` (valid until 29 Şaban 1449 AH / 26 Jan 2028), `MATHEMATICAL`.
- **Quirk found:** the `adjustment` parameter only affects `MATHEMATICAL`. MATHEMATICAL gives 20, and with `adjustment=-1` gives 19. `UAQ&adjustment=-1` still returns 22. So the app must apply its **own ±1/±2 day offset**, as the design's "Hijri adjust ±2" setting already plans.
- Aladhan's description: "the first visual sighting of the lunar crescent (hilāl) can occur up to two days after the date predicted by the Umm al-Qura calendar". South Asia (Pakistan's Ruet-e-Hilal committee, India, Bangladesh) is often 1 day behind Saudi.
- **Recommendation:** default the offset by country (0 for Gulf, −1 suggested for PK/IN/BD, user-editable), and let the user confirm Ramadan/Eid dates.
- **Offline:** use the platform Umm al-Qura calendars via expect/actual, so no data is bundled. JVM-only alternative: `com.github.msarhan:ummalqura-calendar:2.0.2` (MIT, last release 2021).

## 9–10. Asma ul Husna and duas

- **Asma ul Husna:** Aladhan gives Arabic, transliteration and English only. Urdu meanings, reflections and the **audio** shown on board 3 screen 7 must be authored or recorded. Recommendation: bundle a curated JSON (names + EN/UR meanings) and get scholar review.
- **Duas and adhkar:** no free API could be verified. Build an authored dataset with these fields: Arabic text taken from the Quran (Tanzil) or hadith (Arabic editions above), the reference (book and number), and our own transliteration and EN/UR translations. The `Seen-Arabic` MIT dataset can seed the structure (fields: `content`, `translation`, `transliteration`, `count`, `fadl`, `source`, `type`, `audio`). Do not copy its English text, because its origin is unclear.

## 11. Zakat

- **Price conversion:** gold-api returns USD per **troy ounce**. Price per gram = price ÷ **31.1034768**. On 2026-10-03 that gives gold about $133.16/g and silver about $1.946/g.
- **Currency:** PKR, SAR and AED are not supported on gold-api's currency path. Convert from USD with open.er-api (PKR 276.817 on 2026-10-03). Show the attribution link.
- **Spot price caveat:** these are international spot prices, not local retail or bullion rates. Allow a manual price override, and show "last updated".
- **Nisab:** **87.48 g gold (7.5 tola) / 612.36 g silver (52.5 tola)** is confirmed as the commonly cited South Asian values (tola = 11.664 g; web search: Indus Hospital, TCF, Islamic Relief Canada, Dawat-e-Islami). Some bodies use **85 g / 595 g** (not verified this session). Make the nisab basis and the gold-vs-silver standard a scholar-approved setting.

## 12–13. Location and maps

- **GeoNames cities15000:** blocked here, so download it from an unproxied network and pre-process it into a compact SQLite or JSON file. Keep: name, ASCII name, Arabic and Urdu alternate names, lat/lng, country, admin1, **timezone**, population. Credit "GeoNames (CC BY 4.0)" on the About & sources screen. Device geocoders (Android `Geocoder`, iOS `CLGeocoder`) are the online alternative.
- **Map:** use OpenFreeMap + MapLibre Compose (no key). Draw the great-circle line to the Kaaba (21.4225, 39.8262) as a GeoJSON line layer. MapLibre shows the attribution automatically.
- **Risk:** OpenFreeMap's public instance is funded by donations and has no SLA, so keep the compass Qibla fully offline. Google Maps would need an API key and a billing account.

## 14. Noorani Qaida audio

- No licensed source could be verified: Commons is blocked, and no Qaida-quality set was found.
- QF's word-by-word audio (e.g. `wbw/002_255_001.mp3`) could illustrate Quranic words when streaming under QF Terms, but it does not cover letters or harakat drills.
- **Owner must commission recordings** of 29 letters, harakat, tanween, madd and tajweed examples by a qualified qari, with a rights-assignment agreement.

## 15. Fonts

All of these are OFL 1.1 and verified in google/fonts:
- Amiri, Amiri Quran
- Noto Naskh Arabic, Noto Nastaliq Urdu, Noto Sans Arabic, Noto Kufi Arabic
- Scheherazade New, Reem Kufi
- Playfair Display, Lora, Inter, Cormorant Garamond

Suggested stack: **Playfair Display** (display serif) + **Inter** (body) for Latin, **Noto Nastaliq Urdu** for Urdu UI and translation, **Noto Naskh Arabic** for Arabic UI, and **Amiri Quran** for Quran text. Bundle `OFL.txt` with the app and list the fonts on About & sources. Do **not** bundle KFGQPC fonts without permission (see 1d).

---

## What the owner must provide or decide

Only the items that genuinely need the owner:

1. **Firebase:** create the project, register the Android and iOS apps, and supply **`google-services.json`** and **`GoogleService-Info.plist`**. Enable the Email/Password and Google providers.
2. **Apple Sign-In for iOS:** needs an Apple Developer Program account, a Services ID and a key. App Store guideline 4.8 requires an equivalent privacy-focused login option when Google sign-in is offered, and Sign in with Apple is the standard way to meet it.
3. **Decide: commercial or free/non-commercial app.** This one decision sets the licensing route for Saheeh International, the Tanzil translations, hadith translations and audio.
4. **Quran Foundation access**, needed only if we use QF tafsir, word-by-word, more translations or the audio catalogue:
   - Create a Developer Console app (Backend/server type) to get the client id and secret.
   - Provide a backend to hold the secret, e.g. Firebase Cloud Functions on the Blaze plan.
   - Get production access approved.
   - Publish a Privacy Policy and Terms of Use (QF requires them, and so do the app stores).
5. **Permission letters**, if the app is commercial or bundles content:
   - Saheeh International (Al-Muntada Al-Islami / Noor International), or acceptance of QuranEnc's re-publishing conditions.
   - Darussalam, only if Ibn Kathir English must be available offline (otherwise display via QF only).
   - KFGQPC, if the QPC Hafs font is wanted without the QF font route.
   - A legal or scholar confirmation that the Jalandhry translation is public domain, if it is bundled commercially.
6. **Accept the Tanzil download terms** (owner or authorised dev) and download the Uthmani text. I did not click the "agree" link.
7. **Recorded audio** by a qualified qari for Noorani Qaida (letters, harakat, tajweed) and for the 99 Names "Play Audio" feature, with rights assignment.
8. **Scholar review** of all authored content before release:
   - Urdu meanings of the 99 Names; dua translations and transliterations
   - Wudu, ghusl, salah, janazah, Eid and Hajj guides
   - Akhlaq, seerah and kids' stories
   - Zakat defaults (nisab basis, gold vs silver)
   - Default prayer method and madhab per country; Hijri offset policy
   - A quality check of the hadith Urdu text (§5 issues)
9. **Optional sunnah.com API key**, requested through a public GitHub issue that exposes a name and email. It is only needed if fawazahmed0 data is judged insufficient.
10. **Network:** allow-list the blocked hosts on the corporate Fortinet proxy for the dev team, or develop and test on a non-corporate network. Most Quran, audio and QF hosts cannot be reached from this network.
11. **Design copy fix:** the Translation Reference card on board 4 screen 6 credits Hilali & Khan for Saheeh International.

**No keys are needed for:**
- Aladhan, the Adhan library and the platform Hijri calendars
- gold-api.com and open.er-api.com
- jsDelivr (hadith-api, quran-api, tafsir_api)
- OpenFreeMap + MapLibre, Google Fonts and Amiri

---

## Alternates, fallbacks and known risks

| Area | Primary | Fallback | Risk |
|---|---|---|---|
| Quran text | Tanzil bundle | QF `quran_core` via Content Sync (re-sync ≤ 7 days) | Tanzil forbids any modification, so normalise only for *search indexes*, never for displayed text |
| Translations | Bundled Tanzil (non-commercial) / QF runtime | jsDelivr `fawazahmed0/quran-api` (same Tanzil terms) | Commercial licensing (see above). QF forbids build-time bundles. |
| QF API | apis.quran.foundation via backend | legacy api.quran.com v4 (works today without auth) | Legacy may be shut off with ~30 days' notice. Secret must never ship in the app. Pre-live has only surahs 1–2. |
| Audio | QF / quranicaudio streams | everyayah.com, cdn.islamic.network, mp3quran.net | All blocked by the corporate proxy. Reciter copyright. Volunteer CDNs with no SLA. |
| Hadith | hadith-api (jsDelivr, bundled per book) | sunnah.com API (key), hadithapi.com (key) | Unknown translators, small text gaps, empty Bukhari/Muslim grades (treat as Sahih by book). jsDelivr outage: use `.json`/`.min.json` swap and the fastly.jsdelivr.net/gcore mirrors. |
| Tafsir | QF runtime | none safe to bundle | Most English and Urdu tafsir are copyrighted |
| Prayer times | Adhan 0.0.7 offline | Aladhan API (12 req/s) | Kotlin 2.4 requirement; no Maghrib angle (Tehran/Jafari); high-latitude rules needed above ~48° |
| Hijri | Platform Umm al-Qura + local offset | Aladhan gToH | Moon sighting differs by country; Aladhan's `adjustment` is ignored for UAQ |
| Gold price | gold-api.com | goldapi.io / metals.dev (keys), or manual entry | Spot ≠ local retail; no PKR/SAR/AED on gold-api's currency path; IP ban if called several times a second, so cache the value |
| FX rates | open.er-api.com (daily) | cached last value | Attribution required; 429 → 20-minute block; no redistribution |
| Cities | GeoNames bundle | Platform geocoder | Download blocked here; CC BY credit needed |
| Map | OpenFreeMap | Self-hosted or other MapLibre styles; compass-only Qibla | Donation-funded, no SLA; blocked by proxy |
| Fonts | OFL fonts | – | KFGQPC fonts are proprietary |
| Build environment | – | – | The Fortinet proxy also blocks or re-signs `api.alquran.cloud`, `cdn.islamic.network` and `api.sunnah.com`. Windows schannel revocation checks fail for new Let's Encrypt chains (gold-api.com); phones are not affected. |

## Samples saved (`tools/data/samples/`, all under 20 KB)

- **Quran.com / QF legacy API:** `qcom_v4_*` (chapters, translations EN/UR, tafsirs list, tafsir 169 for 1:1, recitations, audio by chapter, chapter recitation, verse 2:255 with words, Urdu word-by-word 1:1, languages)
- **fawazahmed0 quran-api:** `quran_api_*` (Jalandhry 1:1, Umm Muhammad / Saheeh 1:1, Uthmani Hafs 2:255)
- **Hadith:** `hadith_api_*` (editions compact; eng/urd Bukhari 1, eng/urd Abu Dawud 1, ara Tirmidhi 1)
- **Tafsir:** `tafsir_api_*` (editions TSV, Urdu Ibn Kathir 1:1 trimmed, Mukhtasar EN 1:1)
- **Adhan library:** `maven_*` (adhan2 metadata, 0.0.7 module dependencies, stale search result)
- **Aladhan:** `aladhan_*` (Karachi timings, methods, gToH ×3, calendar methods, qibla, asmaAlHusna)
- **Zakat prices and FX:** `gold_api_*` (XAU, XAG, symbols, PKR 404) and `open_er_api_latest_USD.json`
- **Duas:** `adhkar_seen-arabic_en_json_first3KB.txt`
