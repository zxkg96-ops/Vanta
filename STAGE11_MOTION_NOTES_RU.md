# VANTA Stage 11 — Motion System

## Реализовано

- Добавлен `app/src/main/kotlin/com/vanta/app/ui/motion/VantaMotion.kt` с общими длительностями, easing-кривыми и spring-профилями.
- Обновлён `ui/components/Anim.kt`: более сдержанный press feedback без заметного подпрыгивания.
- Обновлён `ui/nav/VantaNav.kt`: переход Home ↔ Now Playing стал короче по перемещению, с decelerate easing и согласованным fade.
- Обновлён `ui/components/GlassTabBar.kt`: индикатор вкладок использует более контролируемую spring-физику.
- Обновлён `ui/player/NowPlayingScreen.kt`: обложка использует мягкую пружину, переход трека стал менее резким, цветовые переходы используют общие motion tokens.

## Не менялось

- ExoPlayer / PlayerController и аудиологика.
- Equalizer, headphone detection и выбор аудиовыхода.
- Схема зависимостей и версии Gradle / Kotlin / Compose.

## Проверка

Исходный архив не содержит `gradlew`, `gradlew.bat` и `gradle-wrapper.jar`, а в среде подготовки нет установленного Gradle. Поэтому полноценную Android-сборку здесь выполнить нельзя. После импорта проекта запусти сборку в GitHub Actions или Android Studio.

## Следующие задачи Stage 11

- Shared-element переход обложки между Mini Player и Now Playing (требует отдельной проверки текущей версии Compose и компоновки экранов).
- Macrobenchmark + FrameTimingMetric на реальном устройстве.
- Проверка reduced-motion/accessibility и частот 60/90/120 Гц.
