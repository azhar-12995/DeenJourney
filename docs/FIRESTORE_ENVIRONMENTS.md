# Debug and production Firestore

Project: `deenjourney-eec6a`.

| Build | Firestore database ID | Local user database | Local preferences |
| --- | --- | --- | --- |
| Android debug | `debug` | `user-debug.db` | `settings-debug.preferences_pb` |
| Android release (including Play Store) | `(default)` | `user.db` | `settings.preferences_pb` |

The release build selects production regardless of how the APK is installed. The debug build selects `debug`, with no fallback to production when that database is missing. Sync, correction reports, and account cloud-data deletion all use `accountFirestore`. Explicit `-Pdj.useEmulators=true` still selects local emulators for the chosen database ID.

Local user records and sync cursors are separated so switching from a debug APK to a release APK does not upload debug records to production. Existing unsuffixed files are preserved for release; they are not migrated into the new debug store. Older debug builds already used the default database and unsuffixed files, so this change does not retrospectively separate those historical records. Do not bulk copy testing data into production.

Both builds currently have the same Android application ID, so they replace each other when installed. Firebase Authentication is shared across databases in this project. Signing in to the same account is supported in each, but deleting that Auth account affects both environments; Firestore account-data deletion only clears the selected database. Use a separate testing Firebase project if Auth isolation is also required.

iOS source uses the native binary's debug flag for the same database and local-file selection; an iOS build has not been verified on this Windows host.

## Cloud setup still required

The code changes do not create remote databases. During this change the console automation failed with Windows error 1385, and no authenticated Firebase CLI was available. Neither database creation nor live rule deployment has been verified.

In Firebase Console, open Firestore for the project. Keep or create a Standard edition Native-mode database with ID `(default)` for production, then create a second with ID `debug`. Use the existing production database's location for debug unless you intentionally choose another location. Select Production mode, then publish `firestore.rules` separately on each database's Rules tab. Avoid open Test-mode rules.

Alternatively, with an authenticated Firebase CLI, list existing databases first:

```powershell
firebase firestore:databases:list --project deenjourney-eec6a
# Replace REGION with the intended supported location; create only databases that are missing.
firebase firestore:databases:create debug --location REGION --project deenjourney-eec6a
firebase firestore:databases:create '(default)' --location REGION --project deenjourney-eec6a
firebase deploy --only firestore:rules --project deenjourney-eec6a
```

`firebase.json` declares both databases and the same owner-only rules. Review existing deployed rules before replacing them if other clients also use the project. Creating the named `debug` database requires enabled billing, and it does not qualify for the free quota. Billing has not been enabled or changed by this work. See https://cloud.google.com/firestore/pricing.

## Verify after setup

1. Install the new debug APK, sign in with a testing account, enable Sync & backup, and save a bookmark. Tap Sync now.
2. In the `debug` database, confirm a document under `users/{uid}/saved`. Check that the new bookmark was not created in `(default)`.
3. Install a signed release build or Play testing build, sign in, and save a different bookmark. Confirm it appears only in `(default)`.
4. Verify a second account cannot read or write the first account's documents. Rule source is supplied here, but server-side rule enforcement and live writes require deployment and verification.

Reference: https://firebase.google.com/docs/firestore/manage-databases
