# Deen Journey

An Android and iOS application built with Kotlin Multiplatform and Compose.
Features include Quran reading and audio, prayer times, Qibla direction,
duas, adhkar, learning, worship guides and personal progress.

## Android setup

Open the project in Android Studio with JDK 17 and Android SDK 36 installed.
Android Studio generates the machine-specific `local.properties` file.

The Android Firebase client configuration for `com.deenjourney.app` is included
at `composeApp/google-services.json`, so a fresh clone uses the same Firebase
project for login/signup. Enable Email/Password authentication in that project's
Firebase Console and configure its Firestore rules. After pulling changes,
sync Gradle, rebuild the APK and reinstall it; pulling alone does not update an
already installed app. `-Pdj.useEmulators=true` explicitly selects development
emulators. Removing the client configuration also selects the emulator setup.
Private service-account keys and signing credentials must stay outside Git.

Build and run checks with:

```shell
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease
./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug
```

The Windows host used during development required the JVM option
`-Djdk.net.unixdomain.tmpdir=C:/JavaSocketFallback/nonexistent` through
`JAVA_TOOL_OPTIONS` to work around its Unix-domain socket environment.

## iOS

Open `iosApp/iosApp.xcodeproj` on macOS with Xcode. iOS compilation and device
testing have not been verified on the Windows development host.

## Design and implementation notes

Public [Privacy Policy](https://azhar-12995.github.io/DeenJourney/) and
[account deletion requests](https://azhar-12995.github.io/DeenJourney/delete-account.html)
use `deenjourney12995@gmail.com`. Their static source is in `site/`, published
from the `gh-pages` branch. No analytics or advertising scripts are added to
these pages. The app links to the policy during signup and from Privacy & data.

See [implementation status](docs/IMPLEMENTATION_STATUS.md),
[screen inventory](docs/SCREENS.md) and [data sources](docs/DATA_SOURCES.md)
for completed work, content provenance and remaining integrations.

Generated build outputs, signing material, downloaded raw datasets and private
service credentials are excluded from version control. Packaged application
resources and the data-generation scripts are included.
