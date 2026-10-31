# VANTA - Stage 2 integration notes

## Included
- The `:app` module is now included in `settings.gradle.kts`.
- `MainActivity` accepts Android `ACTION_VIEW` intents for `audio/*` URIs and forwards the URI to `PlayerController.openUri()`.
- `singleTop` plus `onNewIntent()` handles a second audio file opened while VANTA is already running.
- The original `:core` contract and existing `:data` / `:player` modules are preserved.
- The app still uses fake repositories and a fake player. This is a UI integration build, not yet a production audio engine.

## Build in Android Studio
1. Extract the complete `vanta` folder.
2. Open that folder in Android Studio (JDK 17 recommended for this AGP version).
3. Allow Gradle sync to finish and install the Android SDK platform 35 if prompted.
4. Run `app` on an Android 8.0+ device (minSdk 26).

The source archive supplied at this stage did not contain `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`, and this environment has no installed Gradle or Android SDK. Therefore no successful Gradle build is claimed here. Android Studio can sync the project using its Gradle tooling; add the official wrapper from Android Studio before sharing the project with other developers.

## QA checklist
- App launches without crashing.
- Home screen renders in the dark VANTA theme.
- Recently added demo tracks appear.
- Tapping a track updates the mini-player.
- Play/pause and next update the fake player state.
- Opening an audio file from a file manager launches VANTA and passes its URI to the player controller.
- Opening another audio file while VANTA is already open calls `onNewIntent()`.

## Known limitations
- Demo tracks are synthetic metadata, not files on the device.
- Playback is simulated; no real sound is produced yet.
- Search and equalizer buttons are visual placeholders.
- Background playback, media notification, audio focus, library scanning, playlists persistence, and real billing are not implemented yet.
