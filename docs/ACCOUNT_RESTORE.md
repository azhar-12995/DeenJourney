# Account backup and restore

Uninstalling clears the phone's login session. Sign in again with the same Firebase account on a new installation. Normal app restart retains the Firebase session; passwords are not stored in the account backup.

The login and splash flows now wait for server-backed restore before choosing Home or onboarding. Existing cloud profiles are restored before creating an owner profile, preventing a new profile from hiding the old profile's records. Setup completion, active profile, language/theme, reading/audio position and preferences, learning goals, prayer calculation preferences and reminder settings are backed up under `users/{uid}/preferences/account`.

Family profiles, bookmarks, notes, highlights, learning progress, counters, daily logs and saved Zakat records remain in their existing owner-only collections. Newer offline rows are retained when older cloud rows are read. Restore/serialization failures do not advance the sync cursor. Changes trigger a debounced sync, failed sync retries while the app is running, and continuing reading-preference changes are periodically flushed. Sync works only while enabled and requires connectivity to complete the cloud backup.

GPS/city location, operating-system permissions, parent PIN hash, device downloads, sync cursors and local content versions remain on the device. Choose the new device's location and grant its permissions again; downloads need to be fetched again. Account preferences are restored as one timestamped snapshot, while activity records merge individually using their `updatedAt` timestamps. Concurrent preference edits on two devices use the newer snapshot rather than a field-by-field merge.

The Profile screen shows sync state and a retry instruction for incomplete backups. Sign-in/signup restoration failures offer a retry button without creating another account. Before reinstalling or moving devices, connect to the internet, leave Sync & backup enabled and tap Sync now until it shows Done. Records deleted before a successful cloud backup cannot be recovered from a later reinstall. Records created before Firestore was configured also require a successful sync from the device where they still exist.

## Verification on 7 October 2026

- Three unit regression tests cover restored onboarding/reading preferences, exclusion of device-private fields and retention of newer offline rows.
- Local Android/Firebase emulator integration: upload of a real owner/profile, bookmark, note, lesson progress and preferences succeeded.
- After stopping/restarting the app, its Firebase login session and setup state remained present.
- After clearing all local app storage, signing into the same test account restored the records and preferences, including a single owner profile and its related saved items.
- Reading another account's collection was denied by the same rules used for deployment.
- Integration used only disposable local emulator fixtures. A two-physical-device production sync test with the user's account has not been performed.

## Repeat the integration test

Use a disposable Android emulator, never a personal phone. Start Firebase CLI with `firebase emulators:start --only auth,firestore --project deenjourney-eec6a --config firebase.emulator-test.json`. Java 21 or newer is required by the installed CLI. The isolated ports are 9199 for Auth and 8180 for Firestore.

Build app and test APKs with:

```powershell
.\gradlew.bat :composeApp:assembleDebug :composeApp:assembleDebugAndroidTest '-Pdj.useEmulators=true' '-Pdj.authEmulatorPort=9199' '-Pdj.firestoreEmulatorPort=8180'
```

Install both APKs on that emulator and invoke `DeviceRestoreTest#uploadFixture`, then `DeviceRestoreTest#sessionSurvivesAppRestart`. Clear only the disposable emulator's `com.deenjourney.app` storage, then invoke `DeviceRestoreTest#restoreOnFreshInstallation`. The fixture test refuses to run without `BuildConfig.USE_EMULATORS` enabled.

Build the deliverable APK again with `-Pdj.useEmulators=false`; the emulator test APK is not the APK to distribute.
