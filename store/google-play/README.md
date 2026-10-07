# Deen Journey — Google Play listing assets

Default listing language: English (United States), `en-US`.

In Play Console, open **Grow users → Store presence → Main store listing**.

| Play Console field | File | Format / limit |
| --- | --- | --- |
| App name | Deen Journey | 12 characters |
| Short description | `en-US/short-description.txt` | 77 / 80 characters |
| Full description | `en-US/full-description.txt` | 2,656 / 4,000 characters |
| App icon | `en-US/app-icon-512.png` | 512 × 512, RGBA PNG, under 1 MB |
| Feature graphic | `en-US/feature-graphic-1024x500.png` | 1024 × 500, RGB PNG |
| Phone screenshots | All eight PNGs in `en-US/phone-screenshots/` | 1080 × 1920, RGB PNG, portrait 9:16 |

Copy the contents of the two description files into their corresponding text fields. Upload the icon and feature graphic to their separate fields. Upload the eight individual phone PNGs in numerical order. The contact sheet `phone-screenshots-preview.jpg` is for reviewing the set; upload the individual screenshots to Play Console.

## Phone screenshot order and suggested accessibility text

1. `01-home.png`: Home with the next prayer, Karachi prayer times and daily learning shortcuts.
2. `02-quran-reader.png`: Quran reader showing Al-Baqarah with Arabic, Urdu and English text.
3. `03-noorani-qaida.png`: Noorani Qaida letter practice with recorded pronunciation and the Arabic letter grid.
4. `04-prayer-times.png`: Daily prayer times for Karachi with prayer settings and reminder controls.
5. `05-duas.png`: Dua library with search and categories for Quran, family and learning.
6. `06-learn.png`: Islamic learning hub with hadith, prophets, Allah's names and learning progress.
7. `07-quran-library.png`: Quran library with surah search and a list of Quran chapters.
8. `08-ramadan.png`: Ramadan tools with Sehri and Iftar times, reading plans, duas and fasting guidance.

Feature graphic alt text: Deen Journey: Quran, prayer and learning, with an open Quran on a wooden stand beneath an emerald arch.

These are direct captures of real Android app screens, taken on an Android emulator with a manually selected Karachi location. No account or personal data was used, and no promotional text or replacement UI was added to the screenshots. Debug-only preview routes select the existing screens for capture; the activity is absent from release manifests.

The icon uses the app's existing branding. The feature graphic was created with the built-in `image_gen` tool and exported as an opaque RGB PNG at the required dimensions. The exact generation prompt and export method are recorded in `en-US/feature-graphic-prompt.txt`.

## Contact and policy links

- Support: deenjourney12995@gmail.com
- Privacy policy: https://azhar-12995.github.io/DeenJourney/
- Account deletion: https://azhar-12995.github.io/DeenJourney/delete-account.html

## Validation

`validation.json` records text lengths, image dimensions, color modes, file sizes and SHA-256 hashes. All eight phone screenshots were visually reviewed after capture.

Google's field and image requirements were checked on 7 October 2026:

- https://support.google.com/googleplay/android-developer/answer/9866151?hl=en
- https://support.google.com/googleplay/android-developer/answer/9859152?hl=en

This pack contains phone listing assets. Tablet screenshots are separate fields and should be captured on the relevant tablet layout if required for your distribution.
