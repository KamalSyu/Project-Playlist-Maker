package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.repository.*
import com.practicum.playlistmaker.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Контекст приложения
    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app


    // SharedPreferences
    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Retrofit для работы с iTunes API
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/") // Обратите внимание: возможно, нужен "https://"
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideItunesApi(retrofit: Retrofit): ItunesApi {
        return retrofit.create(ItunesApi::class.java)
    }

    // Репозитории
    @Provides
    @Singleton
    fun provideItunesRepository(api: ItunesApi): ItunesRepository {
        return ItunesRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(sharedPreferences: SharedPreferences): HistoryRepository {
        return HistoryRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(sharedPreferences: SharedPreferences): SettingsRepository {
        return SettingsRepositoryImpl(sharedPreferences)
    }

    // UseCaseCreator — центральный класс для создания UseCase
    @Provides
    @Singleton
    fun provideUseCaseCreator(
        itunesRepository: ItunesRepository,
        historyRepository: HistoryRepository,
        playerRepository: PlayerRepository,
        settingsRepository: SettingsRepository
    ): UseCaseCreator {
        return UseCaseCreator(itunesRepository, historyRepository, playerRepository, settingsRepository)
    }

    // Провайдеры для UseCase (через UseCaseCreator)
    @Provides
    fun provideSearchTracksUseCase(useCaseCreator: UseCaseCreator): SearchTracksUseCase {
        return useCaseCreator.createSearchTracksUseCase()
    }

    @Provides
    fun provideAddTrackToHistoryUseCase(useCaseCreator: UseCaseCreator): AddTrackToHistoryUseCase {
        return useCaseCreator.createAddTrackToHistoryUseCase()
    }


    @Provides
    fun provideGetSearchHistoryUseCase(
        useCaseCreator: UseCaseCreator
    ): GetSearchHistoryUseCase {
        return useCaseCreator.createGetSearchHistoryUseCase()
    }

    @Provides
    fun provideClearSearchHistoryUseCase(useCaseCreator: UseCaseCreator): ClearSearchHistoryUseCase {
        return useCaseCreator.createClearSearchHistoryUseCase()
    }

    @Provides
    fun provideFilterTracksUseCase(useCaseCreator: UseCaseCreator): FilterTracksUseCase {
        return useCaseCreator.createFilterTracksUseCase()
    }
}
