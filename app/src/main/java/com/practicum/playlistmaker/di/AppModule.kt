package com.practicum.playlistmaker.di


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.search.data.network.ItunesApi
import com.practicum.playlistmaker.core.utils.CoroutineDelayProvider
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.sharing.data.provider.SupportEmailDataProviderImpl
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.core.contract.DelayProvider
import com.practicum.playlistmaker.core.constants.Constants.Companion.PREFERENCES
import com.practicum.playlistmaker.core.contract.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.core.contract.FilterTracksUseCaseContract
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.core.contract.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.core.contract.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.domain.usecase.DelayedTrackActionUseCase
import com.practicum.playlistmaker.player.domain.usecase.GetCurrentPositionUseCase
import com.practicum.playlistmaker.player.domain.usecase.PreparePlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.ResetPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.SetPlaybackCompletionListenerUseCase
import com.practicum.playlistmaker.player.domain.usecase.StopPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.TogglePlaybackUseCase
import com.practicum.playlistmaker.search.data.mapper.SearchHistoryMapper
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.mapper.TrackMapper
import com.practicum.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.FilterTracksUseCase
import com.practicum.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.SearchTracksUseCase
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper
import com.practicum.playlistmaker.settings.domain.usecase.GetThemeStateUseCase
import com.practicum.playlistmaker.settings.domain.usecase.SwitchThemeUseCase
import com.practicum.playlistmaker.sharing.data.provider.ShareTextProviderImpl
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.sharing.domain.usecase.SendSupportEmailUseCase
import com.practicum.playlistmaker.sharing.domain.usecase.ShareAppUseCase
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
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")  // Исправлено: корректный протокол
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideItunesApi(retrofit: Retrofit): ItunesApi = retrofit.create(ItunesApi::class.java)

    @Provides
    @Singleton
    fun provideTrackFactory(): TrackFactory = TrackFactory()

    @Provides
    @Singleton
    fun provideTrackMapper(trackFactory: TrackFactory): TrackMapper =
        TrackMapper(trackFactory)

    @Provides
    @Singleton
    fun provideSearchResponseMapper(trackMapper: TrackMapper): SearchResponseMapper =
        SearchResponseMapper(trackMapper)

    @Provides
    @Singleton
    fun provideSearchHistoryMapper(trackMapper: TrackMapper): SearchHistoryMapper =
        SearchHistoryMapper(trackMapper)

    @Provides
    @Singleton
    fun provideThemeSettingsMapper(): ThemeSettingsMapper = ThemeSettingsMapper()

    @Provides
    @Singleton
    fun provideItunesRepository(
        api: ItunesApi,
        searchResponseMapper: SearchResponseMapper
    ): ItunesRepository = ItunesRepositoryImpl(api, searchResponseMapper)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        sharedPreferences: SharedPreferences,
        gson: Gson,
        searchHistoryMapper: SearchHistoryMapper
    ): HistoryRepository = HistoryRepositoryImpl(sharedPreferences, gson, searchHistoryMapper)

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository = PlayerRepositoryImpl()

    @Provides
    @Singleton
    fun provideSettingsRepository(
        sharedPreferences: SharedPreferences,
        gson: Gson,
        themeSettingsMapper: ThemeSettingsMapper
    ): SettingsRepository = SettingsRepositoryImpl(sharedPreferences, gson, themeSettingsMapper)

    // Providers
    @Provides
    @Singleton
    fun provideDelayProvider(): DelayProvider = CoroutineDelayProvider()

    @Provides
    @Singleton
    fun provideSupportEmailDataProvider(
        @ApplicationContext context: Context
    ): SupportEmailDataProvider = SupportEmailDataProviderImpl(context)

    @Provides
    @Singleton
    fun provideShareTextProvider(
        @ApplicationContext context: Context
    ): ShareTextProvider = ShareTextProviderImpl(context)


    @Provides
    @Singleton
    fun provideTrackParcelableMapper(): TrackParcelableMapper = TrackParcelableMapper()

    @Provides
    fun provideFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract =
        FormatTrackDurationUseCase()

    @Provides
    @Singleton
    fun provideSearchTracksUseCase(itunesRepository: ItunesRepository): SearchTracksUseCaseContract =
        SearchTracksUseCase(itunesRepository)

    @Provides
    @Singleton
    fun provideAddTrackToHistoryUseCase(historyRepository: HistoryRepository): AddTrackToHistoryUseCaseContract =
        AddTrackToHistoryUseCase(historyRepository)

    @Provides
    @Singleton
    fun provideClearSearchHistoryUseCase(historyRepository: HistoryRepository): ClearSearchHistoryUseCaseContract =
        ClearSearchHistoryUseCase(historyRepository)

    @Provides
    @Singleton
    fun provideGetSearchHistoryUseCase(historyRepository: HistoryRepository): GetSearchHistoryUseCaseContract =
        GetSearchHistoryUseCase(historyRepository)

    @Provides
    @Singleton
    fun provideFilterTracksUseCase(): FilterTracksUseCaseContract =
        FilterTracksUseCase()

    @Provides
    @Singleton
    fun providePreparePlaybackUseCase(playerRepository: PlayerRepository): PreparePlaybackUseCaseContract =
        PreparePlaybackUseCase(playerRepository)


    @Provides
    @Singleton
    fun provideTogglePlaybackUseCase(playerRepository: PlayerRepository): TogglePlaybackUseCaseContract =
        TogglePlaybackUseCase(playerRepository)

    @Provides
    @Singleton
    fun provideStopPlaybackUseCase(playerRepository: PlayerRepository): StopPlaybackUseCaseContract =
        StopPlaybackUseCase(playerRepository)

    @Provides
    @Singleton
    fun provideGetCurrentPositionUseCase(playerRepository: PlayerRepository): GetCurrentPositionUseCaseContract =
        GetCurrentPositionUseCase(playerRepository)

    @Provides
    @Singleton
    fun provideSetPlaybackCompletionListenerUseCase(playerRepository: PlayerRepository): SetPlaybackCompletionListenerUseCaseContract =
        SetPlaybackCompletionListenerUseCase(playerRepository)

    @Provides
    @Singleton
    fun provideDelayedTrackActionUseCase(delayProvider: DelayProvider): DelayedTrackActionUseCaseContract =
        DelayedTrackActionUseCase(delayProvider)

    @Provides
    @Singleton
    fun provideResetPlaybackUseCase(playerRepository: PlayerRepository): ResetPlaybackUseCaseContract =
        ResetPlaybackUseCase(playerRepository)

    @Provides
    @Singleton
    fun provideGetThemeStateUseCase(settingsRepository: SettingsRepository): GetThemeStateUseCaseContract =
        GetThemeStateUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideSwitchThemeUseCase(settingsRepository: SettingsRepository): SwitchThemeUseCaseContract =
        SwitchThemeUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideShareAppUseCase(shareTextProvider: ShareTextProvider): ShareAppUseCaseContract =
        ShareAppUseCase(shareTextProvider)

    @Provides
    @Singleton
    fun provideSendSupportEmailUseCase(supportEmailDataProvider: SupportEmailDataProvider): SendSupportEmailUseCaseContract =
        SendSupportEmailUseCase(supportEmailDataProvider)

}
