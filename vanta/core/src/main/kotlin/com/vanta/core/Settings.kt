package com.vanta.core

enum class ThemeMode { DARK, AMOLED }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.DARK,
    val gapless: Boolean = true,
    val resumeOnStart: Boolean = true,
    val skipSilence: Boolean = false,
    val minTrackDurationSec: Int = 30,
    val ambientUi: Boolean = true
)
