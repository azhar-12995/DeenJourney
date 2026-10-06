# Feature implementation status

The implementation uses the saved design boards in `docs/board_1.png` through
`board_5.png`, `SCREENS.md`, and the design scripts in `tools/figma`.
Figma was subsequently connected. The live User Flows and App Flow inventories
and screen design contexts are now accessible. This pass compared live Quran
reader (7:16364), player (7:16466), sharing (7:17238), reader settings (7:17952)
and sleep timer (7:18287). A comparison of the other frames and variants remains.

## Added

62 formerly placeholder navigation destinations now have feature screens:
Quran library, reader, player, reciters, bookmarks, notes, tafsir, search,
Qaida practice and memorization; prayer times, calculation settings, location,
Qibla, worship guides, duas, adhkar and tasbih; hadith browsing, reflections,
akhlaq, Quran passages about prophets, Seerah, names, lessons, quizzes and
progress; Ramadan planning/tracking, Zakat, pilgrimage and Hijri calendar;
profile, preferences, downloads, privacy, help, corrections, credits and saved
items. Related design variants share screens using tabs or sheets.

20 JSON content packs are now bundled. Previously none of the authored packs
were included in composeResources, so content silently loaded as empty.
`tools/content/build_missing.py` reproducibly assembles seven additional starter
packs using existing lessons and the bundled Quran database. It preserves Quran
text and translations and cites primary references for short guide summaries.
Tasbih increments are serialized to preserve rapid taps; adhkar counts persist
and feed the Home completion indicator. Quran notes exclude hadith notes.

## Remaining content and integrations

- The Qaida screen has letter/vowel practice, but a complete graded course,
  licensed pronunciation recordings and learner recording review remain.
- Hifz currently supports passage playback/repetition, hiding text and marking
  progress; its review is self assessment rather than an automated recitation test.
- New worship/prophet/Seerah content is an introductory pack. Kalimas contains
  Tayyibah and Shahadah. More authored narratives, full recitation/transliteration
  coverage, school-specific rulings and editorial review remain.
- The Qibla map tab shows a calculated geographic direction grid; it needs a
  basemap integration to match the illustrated map design (8:23274). Automatic
  approval review rejected the proposed MapLibre/OpenFreeMap integration because
  the viewport can reveal the saved location to external services. Explicit user
  approval is required before connecting these providers. No map integration was
  applied.
- Additional Quran translation catalogs and an Indo-Pak text source remain;
  the bundled verified text is Uthmani with English and Urdu translations.
- Privacy export shares a JSON file through the native share sheet.
- The user's Firebase Android configuration is attached at
  `composeApp/google-services.json` and the debug build uses real Firebase
  (`USE_EMULATORS=false`). Email/password authentication still depends on enabling
  that provider in Firebase Console. The supplied file has no Google OAuth client;
  Google sign-in remains unavailable. Cloud rules/provider settings and actual
  account creation were not verified, and no test accounts were created.
- Network audio, tafsir, remote hadith downloads and live metal prices require
  device network/backend verification. Zakat allows editing the nisab when
  online prices are unavailable.
- Android validation does not establish iOS build or device compatibility.

## Validation commands

Run `tools/content/finalize_features.py` with Python to validate bundled packs and
route registration. Run Gradle `:composeApp:assembleDebug`,
`:composeApp:assembleRelease`, `:composeApp:testDebugUnitTest` and
`:composeApp:lintDebug`. Calculator tests cover nisab/hawl eligibility, deductible
debts, full eligible wealth and rounding to minor currency units.

## Live Figma Quran pass

- Reader: surah metadata, juz/page navigation, bismillah, mint active verse,
  gold translator labels and persistent audio-follow preference. Word-by-word
  glosses use the existing remote Quran service and show loading/error states.
- Player: night gradient, original hero asset cropped to the Figma slot,
  seek/speed/repeat controls, verse queue, reciter/reader links, downloads and
  native sleep scheduling. Audio progress persists between routes; resume starts
  at the stored position. Android timer cancellation restores volume and cancels
  fade callbacks. The iOS timer now fades during its last ten seconds, but iOS
  compilation/device verification remains unavailable on this Windows host.
- Sharing: Cream/Night Green/Gold cards, English/Urdu switches, reference and
  translator attribution, copy text and actual PNG export to the native share
  sheet. A 986×998 RGBA export with transparent rounded corners was inspected.
- Emulator visual checks are saved in `docs/verification`. These include the
  reader, player, sleep sheet, preview and actual exported PNG. They establish
  rendering/export behavior, not long-duration timer or download reliability.
- Design QA uses an activity registered only in the debug manifest. It does not
  create accounts or change the login/signup routes in the production app.
- Final Android checks on 2026-10-06 passed: debug/release assembly, five unit
  tests and lint (zero errors, 65 warnings). The debug APK was installed on the
  connected phone with `adb install -r`, preserving its existing app data.
