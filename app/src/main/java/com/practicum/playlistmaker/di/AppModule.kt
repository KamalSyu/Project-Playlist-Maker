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
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.core.constants.Constants.Companion.PREFERENCES
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.search.data.mapper.SearchHistoryMapper
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.mapper.TrackMapper
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper
import com.practicum.playlistmaker.sharing.data.provider.ShareTextProviderImpl
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
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

    // TrackFactory — явно предоставляем зависимость для TrackMapper
    @Provides
    @Singleton
    fun provideTrackFactory(): TrackFactory = TrackFactory()

    // TrackMapper для фичи search
    @Provides
    @Singleton
    fun provideTrackMapper(trackFactory: TrackFactory): TrackMapper =
        TrackMapper(trackFactory)

    // SearchResponseMapper для фичи search — зависит от TrackMapper
    @Provides
    @Singleton
    fun provideSearchResponseMapper(trackMapper: TrackMapper): SearchResponseMapper =
        SearchResponseMapper(trackMapper)

    // SearchHistoryMapper для фичи search — зависит от TrackMapper
    @Provides
    @Singleton
    fun provideSearchHistoryMapper(trackMapper: TrackMapper): SearchHistoryMapper =
        SearchHistoryMapper(trackMapper)

    // ThemeSettingsMapper для фичи settings
    @Provides
    @Singleton
    fun provideThemeSettingsMapper(): ThemeSettingsMapper = ThemeSettingsMapper()

    // Repositories
    @Provides
    @Singleton
    fun provideItunesRepository(
        api: ItunesApi,
        searchResponseMapper: SearchResponseMapper
    ): ItunesRepository = ItunesRepositoryImpl(api, searchResponseMapper)  // Убран trackMapper

    @Provides
    @Singleton
    fun provideHistoryRepository(
        sharedPreferences: SharedPreferences,
        gson: Gson,
        searchHistoryMapper: SearchHistoryMapper  // Убран trackMapper
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

    // UseCase Creator
    @Provides
    @Singleton
    fun provideUseCaseCreator(
        itunesRepository: ItunesRepository,
        historyRepository: HistoryRepository,
        playerRepository: PlayerRepository,
        settingsRepository: SettingsRepository,
        delayProvider: DelayProvider,
        supportEmailDataProvider: SupportEmailDataProvider,
        shareTextProvider: ShareTextProvider
    ): UseCaseCreator = UseCaseCreator(
        itunesRepository = itunesRepository,
        historyRepository = historyRepository,
        playerRepository = playerRepository,
        settingsRepository = settingsRepository,
        delayProvider = delayProvider,
        supportEmailDataProvider = supportEmailDataProvider,
        shareTextProvider = shareTextProvider
    )
}
