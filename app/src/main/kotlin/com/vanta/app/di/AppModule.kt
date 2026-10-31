package com.vanta.app.di

import android.content.Context
import com.vanta.app.data.fake.FakeBillingRepository
import com.vanta.app.data.fake.FakePlaylistRepository
import com.vanta.app.data.fake.FakeSettingsRepository
import com.vanta.core.BillingRepository
import com.vanta.core.LibraryRepository
import com.vanta.core.PlayerController
import com.vanta.core.PlaylistRepository
import com.vanta.core.SettingsRepository
import com.vanta.data.db.VantaDatabase
import com.vanta.data.library.MediaStoreScanner
import com.vanta.data.library.RealLibraryRepository
import com.vanta.player.ExoPlayerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VantaDatabase =
        VantaDatabase.create(context)

    @Provides
    @Singleton
    fun provideScanner(@ApplicationContext context: Context): MediaStoreScanner =
        MediaStoreScanner(context)

    @Provides
    @Singleton
    fun provideLibraryRepository(
        db: VantaDatabase,
        scanner: MediaStoreScanner
    ): LibraryRepository = RealLibraryRepository(db, scanner)

    @Provides
    @Singleton
    fun providePlayerController(
        @ApplicationContext context: Context
    ): PlayerController = ExoPlayerController(context)

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
