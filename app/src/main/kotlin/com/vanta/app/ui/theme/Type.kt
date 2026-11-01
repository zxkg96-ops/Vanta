package com.vanta.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vanta.app.R

/*
 * Шрифт: JetBrains Mono 2.304 (c) 2020 The JetBrains Mono Project Authors,
 * https://github.com/JetBrains/JetBrainsMono, лицензия SIL Open Font License 1.1.
 * Полный текст лицензии: app/src/main/assets/licenses/JetBrainsMono-OFL.txt
 * (в res/font его класть нельзя: туда допускаются только файлы шрифтов и XML).
 * Файлы: res/font/jetbrains_mono_regular.ttf (400), _medium.ttf (500), _semibold.ttf (600).
 */
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold)
)

// Стили, которые мы не настраиваем сами, берём из Material3 по умолчанию, но тоже с нашим шрифтом.
// Иначе headlineSmall, labelSmall и другие остались бы на системном шрифте.
private val Base = Typography()

val VantaTypography = Typography(
    displayLarge = TextStyle(fontFamily = JetBrainsMono, fontSize = 42.sp, lineHeight = 48.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1.5).sp),
    displayMedium = Base.displayMedium.copy(fontFamily = JetBrainsMono),
    displaySmall = Base.displaySmall.copy(fontFamily = JetBrainsMono),
    headlineLarge = TextStyle(fontFamily = JetBrainsMono, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.7).sp),
    headlineMedium = TextStyle(fontFamily = JetBrainsMono, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
    headlineSmall = Base.headlineSmall.copy(fontFamily = JetBrainsMono),
    titleLarge = TextStyle(fontFamily = JetBrainsMono, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = JetBrainsMono, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
    titleSmall = Base.titleSmall.copy(fontFamily = JetBrainsMono),
    bodyLarge = TextStyle(fontFamily = JetBrainsMono, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = JetBrainsMono, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = Base.bodySmall.copy(fontFamily = JetBrainsMono),
    labelLarge = TextStyle(fontFamily = JetBrainsMono, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.copy(fontFamily = JetBrainsMono),
    labelSmall = Base.labelSmall.copy(fontFamily = JetBrainsMono)
)
