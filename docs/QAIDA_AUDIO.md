# Noorani Qaida audio

Tap any of the 29 letters, the enlarged letter card, or Listen to hear the letter's vocalized Arabic name using the device speech engine. Previous/Next also pronounce the selected letter. Rapid taps replace the current pronunciation. Existing Quran recitation pauses before letter audio begins.

Qaida audio is enabled by default and has the same saved switch in Settings and on the Qaida screen. Muting stops queued/current pronunciation. The setting is included in the account preferences backup so another installation can restore the same choice. Speech stops when the screen goes into the background and the screen-owned engine is released when leaving the route.

Android uses TextToSpeech with Arabic (Saudi Arabia), preferring an installed Arabic voice. It does not substitute English pronunciation when Arabic is unavailable. If voice data or the engine is unavailable, the screen shows a voice setup action. Device speech engines may require voice downloads or network access depending on the installed voice. Letter names are fixed app content; no microphone or user-entered text is used for this feature. These are synthesized letter names, not recordings of a Qaida teacher or of Quran recitation.

## Verification on 7 October 2026

- Android debug assembly, release compilation and ten unit tests passed.
- Native Android speech instrumentation passed (status code 0, not an assumption skip): Arabic Baa produced speech start/completion callbacks; stopping the next utterance returned the engine to Ready.
- Letter-data tests distinguish ح/ه, ت/ط and ذ/ظ and cover all 29 entries.
- The audio-off choice survived local activity restart and cloud preference serialization. Older backups without the new field still decode.
- Qaida and Settings UI were inspected on the disposable Android emulator; screenshots are in `verification/qaida-audio.png` and `verification/qaida-settings.png`.
- iOS speech source was added, but iOS compilation and device playback have not been verified on this Windows host. Pronunciation quality depends on the installed voice and should be checked with a teacher.

Android API reference: https://developer.android.com/reference/android/speech/tts/TextToSpeech
