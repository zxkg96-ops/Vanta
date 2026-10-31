package com.vanta.app.data.fake

import com.vanta.core.AppSettings
import com.vanta.core.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository : SettingsRepository {
    private val mutableSettings = MutableStateFlow(AppSettings())
    override val settings = mutableSettings.asStateFlow()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        mutableSettings.value = transform(mutableSettings.value)
    }
}
