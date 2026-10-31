package com.vanta.core

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
