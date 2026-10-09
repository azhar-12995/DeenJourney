# Login and signup responsiveness

Checked on 9 October 2026 against local HEAD and GitHub `main` at `222dde4`. Additional local Claude changes were present during the build and were preserved.

## Problem

Firebase authentication triggered a background full sync, while the login/signup screen separately requested another full sync behind the same mutex. Both operations could consume the 25-second sync timeout. Each sync made eight collection reads and a preferences read sequentially, then waited for uploads before allowing navigation. Signup also awaited profile update and verification-email requests after account creation. Startup waited for cloud sync even when an existing account already had usable local data.

## Change

- Startup and the login/signup screen share one restore attempt per auth session. Failed downloads remain retryable without entering the password again.
- The nine independent Firestore reads run concurrently. All downloads finish before local rows and preferences are applied.
- Navigation waits for restored data, preferences and a usable active profile. Uploads continue in the application scope; sync status and retry behavior still track upload failures.
- Existing accounts with local data open without waiting for startup network sync. Fresh installations still restore before onboarding is selected.
- Signup proceeds after account creation. Display-name update and verification email run in the application scope; the entered name remains available while Firebase updates it.
- Authentication network requests have a 15-second deadline, and IME/button submission cannot launch duplicate requests while the form is busy.
- Leaving a login screen does not cancel account restore. Signing out cancels the session's pending worker and releases any waiting screen.

## Validation

- Android debug APK build and release Kotlin compilation.
- Common restore-gate regression tests: shared concurrent restore, readiness before upload, retry after failed download, screen cancellation, session isolation, upload failure after successful restore, and sign-out before the worker starts.
- Existing preference serialization and conflict-resolution tests.
- Instrumented tests on a disposable Android emulator against isolated local Auth/Firestore emulators:
  - Signup, restore readiness, upload of bookmarks, notes, progress and preferences: passed (4.383 seconds for the complete fixture test).
  - Session survives process restart: passed.
  - Fresh app storage, same credentials, restored family profile, bookmarks, notes, lesson progress, language, theme and Quran position: passed (2.773 seconds for the complete restore test).

These timings describe the local emulator tests, not production-network latency. No production Firebase accounts or data were changed. iOS native compilation requires macOS and was not run here.
