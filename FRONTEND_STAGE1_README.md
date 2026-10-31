# VANTA frontend - Stage 1

This archive adds the `:app` module to the supplied backend project.

## Included
- Compose application shell and Hilt application setup.
- `:core` remains unchanged.
- Fake implementations for all five core interfaces.
- 40 generated sample tracks.
- Dark Deluxe theme and reusable UI components.
- A small Home screen to verify the visual system and fake player wiring.

## Architecture boundary
`:app` depends on `:core` only. It does not depend on `:data` or `:player`.
The fake module is the only DI binding in this stage. Replace it with real bindings
when the backend engineer supplies implementations.

## Build
Open the `vanta` directory in Android Studio and sync Gradle.
The project requires JDK 17 and Android SDK 35.

## Stage 1 limitations
Navigation, library browsing, full Now Playing, queue drag-and-drop, permissions,
ACTION_VIEW, playlists UI, settings UI and Pro UI are intentionally left for later
stages, following the agreed stop-after-each-stage workflow.
