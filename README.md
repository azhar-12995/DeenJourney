# Deen Journey

An Android and iOS application built with Kotlin Multiplatform and Compose.
Features include Quran reading and audio, prayer times, Qibla direction,
duas, adhkar, learning, worship guides and personal progress.

## Android setup

Open the project in Android Studio with JDK 17 and Android SDK 36 installed.
Android Studio generates the machine-specific `local.properties` file.

For real Firebase login/signup, download the Android app configuration for
`com.deenjourney.app` from your Firebase project and place it at
`composeApp/google-services.json`. Enable Email/Password authentication and
configure the project's Firestore rules. This local configuration is ignored
by Git. Without it, the app uses the development Firebase emulator setup.

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

See [implementation status](docs/IMPLEMENTATION_STATUS.md),
[screen inventory](docs/SCREENS.md) and [data sources](docs/DATA_SOURCES.md)
for completed work, content provenance and remaining integrations.

Generated build outputs, signing material, downloaded raw datasets and local
service configuration are excluded from version control. Packaged application
resources and the data-generation scripts are included.
