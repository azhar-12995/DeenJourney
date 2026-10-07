# Default Firestore configuration

Project: `deenjourney-eec6a`.

All debug and release builds use the `(default)` Firestore database. A separate named database is deferred at the user's request. `firebase.json` deploys rules only to `(default)`.

| Build | Firestore database ID | Local user database | Local preferences |
| --- | --- | --- | --- |
| Android debug | `(default)` | `user-debug.db` | `settings-debug.preferences_pb` |
| Android release (including Play Store) | `(default)` | `user.db` | `settings.preferences_pb` |

Sync, correction reports, and account cloud-data deletion use the same centralized `accountFirestore` instance. The explicit development option `-Pdj.useEmulators=true` still connects to local emulators.

Local records and sync cursors remain separate between debug and release installations. In the cloud, both builds use the same account's documents. Testing changes and account deletion therefore affect the shared default database. Firebase Authentication is also shared. Existing local files are preserved; this change does not move or delete data.

Both Android builds use the same application ID, so they replace each other when installed. iOS also selects `(default)`; its local files use the native binary's debug flag. iOS compilation has not been verified on this Windows host.

## Server settings

The app is configured for `(default)`. Remote database existence, deployed security rules, and live writes still require verification. Console automation failed with Windows error 1385. The existing Firebase CLI login returned HTTP 403 when listing this project's databases, so login with a project-authorized account is required before deployment. No cloud database or billing settings have been changed.

Open the project's Firestore Console and select `(default)`. Publish the supplied `firestore.rules` on its Rules tab after reviewing any existing rules used by other clients. These rules restrict account records to their signed-in owner and correction report creation to the submitting account. No named database is needed.

With an authenticated Firebase CLI, the equivalent deployment is:

```powershell
firebase firestore:databases:list --project deenjourney-eec6a
firebase deploy --only firestore:rules --project deenjourney-eec6a
```

After deployment, install the new debug APK, sign in, enable Sync & backup, save a bookmark, and tap Sync now. Confirm the document appears under `(default)/users/{uid}/saved`. Then verify a second account cannot access the first account's documents. A release installation signed into the same account should retrieve the shared records.
