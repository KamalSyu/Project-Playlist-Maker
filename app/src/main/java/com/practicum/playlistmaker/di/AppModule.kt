package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.search.data.network.ItunesApi
import com.practicum.playlistmaker.core.utils.CoroutineDelayProvider
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProviderImpl
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProviderImpl
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.core.contract.DelayProvider
import com.practicum.playlistmaker.core.contract.ShareTextProvider
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.core.constants.Constants.Companion.PREFERENCES
import com.practicum.playlistmaker.settings.domain.SwitchThemeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext app: Application): Context = app

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideItunesApi(retrofit: Retrofit): ItunesApi {
        return retrofit.create(ItunesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideItunesRepository(
        api: ItunesApi,
        dtoMapper: DtoMapper
    ): ItunesRepository {
        return ItunesRepositoryImpl(api, dtoMapper)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(
        sharedPreferences: SharedPreferences,
        gson: Gson,
        dtoMapper: DtoMapper
    ): HistoryRepository {
        return HistoryRepositoryImpl(sharedPreferences, gson, dtoMapper)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        sharedPreferences: SharedPreferences,
        gson: Gson,
        dtoMapper: DtoMapper
    ): SettingsRepository {
        return SettingsRepositoryImpl(sharedPreferences, gson, dtoMapper)
    }

    @Provides
    @Singleton
    fun provideDtoMapper(trackFactory: TrackFactory): DtoMapper {
        return DtoMapper(trackFactory)
    }

    @Provides
    @Singleton
    fun provideTrackFactory(): TrackFactory {
        return TrackFactory()
    }

    @Provides
    @Singleton
    fun provideDelayProvider(): DelayProvider {
        return CoroutineDelayProvider()
    }

    @Provides
    @Singleton
    fun provideSupportEmailDataProvider(
        @ApplicationContext context: Context
    ): SupportEmailDataProvider {
        return SupportEmailDataProviderImpl(context)
    }

    @Provides
    @Singleton
    fun provideShareTextProvider(
        @ApplicationContext context: Context
    ): ShareTextProvider {
        return ShareTextProviderImpl(context)
    }

    // Теперь все зависимости доступны!
    @Provides
    @Singleton
    fun provideUseCaseCreator(
        @ApplicationContext context: Context,
        itunesRepository: ItunesRepository,
        historyRepository: HistoryRepository,
        playerRepository: PlayerRepository,
        settingsRepository: SettingsRepository,
        delayProvider: DelayProvider,
        supportEmailDataProvider: SupportEmailDataProvider,
        shareTextProvider: ShareTextProvider
    ): UseCaseCreator {
        return UseCaseCreator(
            itunesRepository = itunesRepository,
            historyRepository = historyRepository,
            playerRepository = playerRepository,
            settingsRepository = settingsRepository,
            delayProvider = delayProvider,
            supportEmailDataProvider = supportEmailDataProvider,
            shareTextProvider = shareTextProvider
        )
    }

    @Provides
    @Singleton
    fun provideSwitchThemeUseCase(
        settingsRepository: SettingsRepository
    ): SwitchThemeUseCase {
        return SwitchThemeUseCase(settingsRepository)
    }

}
