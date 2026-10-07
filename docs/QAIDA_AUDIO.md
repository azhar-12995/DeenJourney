# Noorani Qaida recorded audio

Letter taps, Listen, and Previous/Next play unmodified MP3 recordings from Qamar Apps' Noorani Qaida (Pakistani Edition), recited by Mufti Mohammed Ghiyas Mohiuddin of Madrasa Arabia Hifzul Quraan, India. All 29 clips are bundled (approximately 676 KiB) and work offline. Device TextToSpeech has been removed; no voice engine, voice download, or network request is required.

The screen-owned native player replaces the current clip on rapid taps. It stops when audio is muted, the app enters the background, or the route is disposed. Quran recitation pauses before a letter clip starts. The shared Settings/Qaida audio switch remains included in the account preferences backup.

## Provenance and license

- Source package: `com.qamarapps.nooranipak` version 1.2.2 (22), Qamar Apps.
- APK signature verified with Android apksigner. Signer: Qamar Apps, Auckland; certificate SHA-1 `db1026d55b92370ca859c0533df0beaf1a6b15ec`.
- Both publisher website https://www.qamarapps.com/license and APK `assets/www/book/Pages/aboutus.html` explicitly license the publication under Creative Commons Attribution-ShareAlike 4.0 International: https://creativecommons.org/licenses/by-sa/4.0/.
- Download: https://d.apkpure.net/b/APK/com.qamarapps.nooranipak?version=latest. The mirror was used only to retrieve the signed original package; it was not installed or executed.
- First-lesson image and original HTML image-map were inspected together. Files 01003 through 01027 map to Alif through Noon; the final row maps Waw=01028, Haa=01029, Hamzah=01030, Yaa=01031. Bari Yaa (01032) is omitted from this app's existing 29-letter grid.
- The MP3 recordings are unmodified and retain CC BY-SA 4.0. Selection, renaming/mapping and omission are documented; no endorsement is implied. In-app attribution names the reciter and publisher and links the license. Packaged ATTRIBUTION.txt and provenance.json contain source entries and per-file SHA-256 hashes. The original embedded publisher license statement is preserved beside the clips.

## Verification on 7 October 2026

- Android debug APK and test APK assembly, release Kotlin compilation, and all 10 unit tests passed.
- Native instrumentation passed with Wi-Fi/mobile data disabled on the disposable emulator: all 29 MP3 files started and completed, rapid replacement/stop worked, and an invalid filename reported an error. Runtime 92.581 seconds; no skipped tests.
- The 29 packaged SHA-256 hashes match the unmodified source clips. Qaida screen and visible source/license attribution were inspected; screenshots are in `verification/qaida-audio.png` and `verification/qaida-recorded-attribution.png`.


Android instrumentation plays every one of the 29 bundled MP3s, waits for playback completion, checks replacement/stop, and checks missing-file errors. No installed speech voice is needed. Common tests cover distinct letter names, the source's differing final-row order, and mute preference backup compatibility. Android debug assembly, release compilation and tests should be rerun after any audio/mapping change.

iOS uses bundled recordings with AVAudioPlayer; compilation and device playback remain unverified on this Windows host. The existing Practice tab displays vowel drills but its Listen action continues to pronounce the selected letter name; this change adds recorded names, not recordings of all Qaida lessons.
