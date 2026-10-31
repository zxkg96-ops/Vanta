package com.vanta.app.di

import com.vanta.app.data.fake.FakeBillingRepository
import com.vanta.app.data.fake.FakeLibraryRepository
import com.vanta.app.data.fake.FakePlayerController
import com.vanta.app.data.fake.FakePlaylistRepository
import com.vanta.app.data.fake.FakeSettingsRepository
import com.vanta.core.BillingRepository
import com.vanta.core.LibraryRepository
import com.vanta.core.PlayerController
import com.vanta.core.PlaylistRepository
import com.vanta.core.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FakeModule {
    @Provides
    @Singleton
    fun providePlayerController(): PlayerController = FakePlayerController()

    @Provides
    @Singleton
    fun provideLibraryRepository(): LibraryRepository = FakeLibraryRepository()

    @Provides
    @Singleton
    fun providePlaylistRepository(): PlaylistRepository = FakePlaylistRepository()

    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository = FakeSettingsRepository()

    @Provides
    @Singleton
    fun provideBillingRepository(): BillingRepository = FakeBillingRepository()
}
