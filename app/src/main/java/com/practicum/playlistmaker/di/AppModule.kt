package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.data.mapper.DtoMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.factory.TrackFactory
import com.practicum.playlistmaker.domain.repository.*
import com.practicum.playlistmaker.domain.usecase.*
import com.practicum.playlistmaker.utils.Constants.Companion.PREFERENCES
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

    // 1. Базовые зависимости (Context, SharedPreferences, Gson)
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

    // 2. Сетевые компоненты (Retrofit, API)
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

    // 3. Репозитории (реализуют интерфейсы из domain)
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
        sharedPreferences: SharedPreferences
    ): HistoryRepository {
        return HistoryRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        sharedPreferences: SharedPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(sharedPreferences)
    }

    // 4. Mapper и вспомогательные сервисы
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

    // 5. UseCaseCreator (ЕДИНСТВЕННЫЙ провайдер для Use Cases)
    @Provides
    @Singleton
    fun provideUseCaseCreator(
        itunesRepository: ItunesRepository,
        historyRepository: HistoryRepository,
        playerRepository: PlayerRepository,
        settingsRepository: SettingsRepository
    ): UseCaseCreator {
        return UseCaseCreator(
            itunesRepository = itunesRepository,
            historyRepository = historyRepository,
            playerRepository = playerRepository,
            settingsRepository = settingsRepository
        )
    }

    @Provides
    @Singleton
    fun provideSwitchThemeUseCase(
        settingsRepository: SettingsRepository
    ): SwitchThemeUseCaseContract {
        return SwitchThemeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideGetThemeStateUseCase(
        settingsRepository: SettingsRepository
    ): GetThemeStateUseCaseContract {
        return GetThemeStateUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideShareAppUseCase(): ShareAppUseCaseContract {
        return ShareAppUseCase()
    }

    @Provides
    @Singleton
    fun provideSendSupportEmailUseCase(): SendSupportEmailUseCaseContract {
        return SendSupportEmailUseCase()
    }


    @Provides
    @Singleton
    fun provideFilterTracksUseCase(): FilterTracksUseCaseContract {
        return FilterTracksUseCase()
    }

    @Provides
    @Singleton
    fun provideFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract {
        return FormatTrackDurationUseCase()
    }

    @Provides
    @Singleton
    fun provideTogglePlaybackUseCase(playerRepository: PlayerRepository): TogglePlaybackUseCaseContract {
        return TogglePlaybackUseCase(playerRepository)
    }
}
