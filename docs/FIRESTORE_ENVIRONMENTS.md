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

On 7 October 2026, the `(default)` database was created with the user's approval in Mumbai (`asia-south1`), using Standard edition and Firestore Native mode. Metadata read-back confirmed `freeTier: true`. Billing settings were not changed. No named database was created.

The supplied `firestore.rules` compiled successfully and were released to the default database through Firebase CLI using the project-authorized `deenjourney12995@gmail.com` account. Account preferences now use an additional owner-only `preferences` collection. Backup, fresh-storage restore and cross-account denial have been verified with disposable local Android/Firebase emulator fixtures; the user's production account data has not been inspected. See `ACCOUNT_RESTORE.md` for verification details.

Open the project's Firestore Console and select `(default)` to inspect the deployed rules. They restrict account records to their signed-in owner and correction report creation to the submitting account. No named database is needed.

With an authenticated Firebase CLI, the equivalent deployment is:

```powershell
firebase firestore:databases:list --project deenjourney-eec6a --account deenjourney12995@gmail.com
firebase deploy --only firestore:rules --project deenjourney-eec6a --account deenjourney12995@gmail.com
```

After deployment, install the new debug APK, sign in, enable Sync & backup, save a bookmark, and tap Sync now. Confirm the document appears under `(default)/users/{uid}/saved`. Then verify a second account cannot access the first account's documents. A release installation signed into the same account should retrieve the shared records.
