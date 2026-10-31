package com.vanta.core

enum class RepeatMode { OFF, ALL, ONE }

data class EqualizerState(
    val supported: Boolean = false,
    val enabled: Boolean = false,
    val bandFreqsHz: List<Int> = emptyList(),
    val bandsDb: List<Float> = emptyList(),
    val minDb: Float = -12f,
    val maxDb: Float = 12f,
    val bassBoostSupported: Boolean = false,
    val bassBoost: Int = 0,          // 0..100
    val trebleDb: Float = 0f,        // -12..12
    val balance: Float = 0f,         // -1..1
    val presets: List<String> = emptyList(),
    val currentPreset: String? = null
)

data class PlayerState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
    val repeat: RepeatMode = RepeatMode.OFF,
    val shuffle: Boolean = false,
    val sleepTimerLeftMs: Long? = null,
    val equalizer: EqualizerState = EqualizerState(),
    val isPro: Boolean = false,
    val error: String? = null
)
