package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.gson.Gson
import com.practicum.playlistmaker.core.utils.CoroutineDelayProvider
import com.practicum.playlistmaker.core.utils.DateFormatter
import com.practicum.playlistmaker.core.utils.DelayProvider
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCaseImpl
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.mediateka.data.repository.PlaylistsRepositoryImplMedia
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractor
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractorImpl
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.domain.usecase.AddTrackToPlaylistUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.CreatePlaylistUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistsUseCase
import com.practicum.playlistmaker.mediateka.ui.view.CreatePlaylistViewModel
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteTracksViewModel
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistsViewModel
import com.practicum.playlistmaker.player.data.db.AppDatabase
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import com.practicum.playlistmaker.search.data.network.ItunesApi
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.sharing.data.provider.SupportEmailDataProviderImpl
import com.practicum.playlistmaker.sharing.data.provider.ShareTextProviderImpl
import com.practicum.playlistmaker.player.data.repository.FavoriteTracksRepositoryImpl
import com.practicum.playlistmaker.player.data.repository.PlaylistRepositoryImpl
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.player.domain.usecase.favorite.AddToFavoritesUseCase
import com.practicum.playlistmaker.player.domain.usecase.favorite.AddToFavoritesUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.favorite.GetFavoriteTracksUseCase
import com.practicum.playlistmaker.player.domain.usecase.utils.DelayedTrackActionUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playback.GetCurrentPositionUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.favorite.GetFavoriteTracksUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.favorite.IsTrackFavoriteUseCase
import com.practicum.playlistmaker.player.domain.usecase.favorite.IsTrackFavoriteUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.favorite.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.PreparePlaybackUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.favorite.RemoveFromFavoritesUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playback.GetCurrentPositionUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.PreparePlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.ResetPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.ResetPlaybackUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playback.SetPlaybackCompletionListenerUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.SetPlaybackCompletionListenerUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playback.StopPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.StopPlaybackUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playback.TogglePlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.TogglePlaybackUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.player.domain.usecase.playlist.GetPlaylistsUseCaseImpl
import com.practicum.playlistmaker.player.domain.usecase.utils.DelayedTrackActionUseCase
import com.practicum.playlistmaker.search.data.mapper.SearchHistoryMapper
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.mapper.TrackMapper
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.domain.usecase.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.history.AddTrackToHistoryUseCaseImpl
import com.practicum.playlistmaker.search.domain.usecase.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.history.ClearSearchHistoryUseCaseImpl
import com.practicum.playlistmaker.search.domain.usecase.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.search.FilterTracksUseCaseImpl
import com.practicum.playlistmaker.search.domain.usecase.history.GetSearchHistoryUseCaseImpl
import com.practicum.playlistmaker.search.domain.usecase.search.FilterTracksUseCase
import com.practicum.playlistmaker.search.domain.usecase.search.SearchTracksUseCase
import com.practicum.playlistmaker.search.domain.usecase.search.SearchTracksUseCaseImpl
import com.practicum.playlistmaker.search.ui.view.SearchViewModel
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.settings.domain.usecase.GetThemeStateUseCase
import com.practicum.playlistmaker.settings.domain.usecase.GetThemeStateUseCaseImpl
import com.practicum.playlistmaker.settings.domain.usecase.SwitchThemeUseCase
import com.practicum.playlistmaker.settings.domain.usecase.SwitchThemeUseCaseImpl
import com.practicum.playlistmaker.settings.ui.view.SettingsViewModel
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.sharing.domain.usecase.SendSupportEmailUseCase
import com.practicum.playlistmaker.sharing.domain.usecase.SendSupportEmailUseCaseImpl
import com.practicum.playlistmaker.sharing.domain.usecase.ShareAppUseCase
import com.practicum.playlistmaker.sharing.domain.usecase.ShareAppUseCaseImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.dsl.module

private const val PREFERENCES = "play_maker_preferences"

val appModule = module {
    // 1. Базовые компоненты
    single<Context> { get<Application>().applicationContext }

    single<SharedPreferences> {
        get<Context>().getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        )
    }
    single { Gson() }
    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<ItunesApi> { get<Retrofit>().create(ItunesApi::class.java) }

    // 2. Утилиты
    single<DelayProvider> { CoroutineDelayProvider() }
    single { DateFormatter() }
    single<FormatTrackDurationUseCase> { FormatTrackDurationUseCaseImpl() }

    // 3. Фабрики и базовые мапперы
    single { TrackFactory() }
    single { ThemeSettingsMapper() }

    // 4. Мапперы с зависимостями
    single { TrackMapper(get()) }
    single { SearchResponseMapper(get()) }
    single { SearchHistoryMapper(get()) }
    single { FileStorageService(get<Context>()) }

    // 5. Репозитории
    single<PlayerRepository> { PlayerRepositoryImpl() }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            sharedPreferences = get(),
            gson = get(),
            dtoMapper = get()
        )
    }
    single<HistoryRepository> {
        HistoryRepositoryImpl(
            sharedPreferences = get(),
            gson = get(),
        )
    }
    single<ItunesRepository> {
        ItunesRepositoryImpl(
            api = get(),
            searchResponseMapper = get()
        )
    }
    single<SupportEmailDataProvider> { SupportEmailDataProviderImpl(get()) }
    single<ShareTextProvider> { ShareTextProviderImpl(get()) }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(
            dao = get(),
            trackFactory = get()
        )
    }

    single<PlaylistsRepositoryMedia> {
        PlaylistsRepositoryImplMedia(
            dao = get(),
            context = get()
        )
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            context = get()
        )
    }

    single { TrackParcelableMapper() }

    single<PlaylistInteractor> {
        PlaylistInteractorImpl(
            addTrackToPlaylistUseCase = get(),
            playlistDao = get(),
            playlistsRepositoryMedia = get()
        )
    }

    // 6. UseCases
    // UseCases поиска
    factory<SearchTracksUseCase> { SearchTracksUseCaseImpl(get()) }
    factory<AddTrackToHistoryUseCase> { AddTrackToHistoryUseCaseImpl(get()) }
    factory<ClearSearchHistoryUseCase> { ClearSearchHistoryUseCaseImpl(get()) }
    factory<GetSearchHistoryUseCase> { GetSearchHistoryUseCaseImpl(get()) }
    factory<FilterTracksUseCase> { FilterTracksUseCaseImpl() }

    // UseCases плеера
    factory<PreparePlaybackUseCase> { PreparePlaybackUseCaseImpl(get()) }
    factory<TogglePlaybackUseCase> { TogglePlaybackUseCaseImpl(get()) }
    factory<StopPlaybackUseCase> { StopPlaybackUseCaseImpl(get()) }
    factory<GetCurrentPositionUseCase> { GetCurrentPositionUseCaseImpl(get()) }
    factory<SetPlaybackCompletionListenerUseCase> { SetPlaybackCompletionListenerUseCaseImpl(get()) }
    factory<DelayedTrackActionUseCase> { DelayedTrackActionUseCaseImpl(get()) }
    factory<ResetPlaybackUseCase> { ResetPlaybackUseCaseImpl(get()) }

    // UseCases настроек
    factory<GetThemeStateUseCase> { GetThemeStateUseCaseImpl(get()) }
    factory<SwitchThemeUseCase> { SwitchThemeUseCaseImpl(get()) }

    // UseCases шаринга
    factory<ShareAppUseCase> { ShareAppUseCaseImpl(get()) }
    factory<SendSupportEmailUseCase> { SendSupportEmailUseCaseImpl(get()) }

    // UseCases избранного
    factory<AddToFavoritesUseCase> { AddToFavoritesUseCaseImpl(get()) }
    factory<RemoveFromFavoritesUseCase> { RemoveFromFavoritesUseCaseImpl(get()) }
    factory<GetFavoriteTracksUseCase> { GetFavoriteTracksUseCaseImpl(get()) }
    factory<IsTrackFavoriteUseCase> { IsTrackFavoriteUseCaseImpl(get()) }

    // UseCases медиатеки
    factory<CreatePlaylistUseCase> { CreatePlaylistUseCase(get()) }
    factory<LoadPlaylistsUseCase> { LoadPlaylistsUseCase(get()) }
    factory<GetPlaylistsUseCase> { GetPlaylistsUseCaseImpl(get()) }

    factory<AddTrackToPlaylistUseCase> {
        AddTrackToPlaylistUseCase(get())
    }

    // 7. ViewModel
    viewModel { SearchViewModel(
        searchTracksUseCase = get(),
        addTrackToHistoryUseCase = get(),
        getSearchHistoryUseCase = get(),
        clearSearchHistoryUseCase = get(),
        filterTracksUseCase = get(),
        delayedTrackActionUseCase = get(),
        formatTrackDurationUseCase = get()
    ) }

    viewModel { AudioPlayerViewModel(
        preparePlaybackUseCase = get(),
        togglePlaybackUseCase = get(),
        stopPlaybackUseCase = get(),
        getCurrentPositionUseCase = get(),
        setCompletionListenerUseCase = get(),
        resetPlaybackUseCase = get(),
        formatTrackDurationUseCase = get(),
        trackParcelableMapper = get(),
        addToFavoritesUseCase = get(),
        removeFromFavoritesUseCase = get(),
        isTrackFavoriteUseCase = get(),
        playlistRepository = get()
    ) }

    viewModel { SettingsViewModel(
        getThemeStateUseCase = get(),
        switchThemeUseCase = get(),
        shareAppUseCase = get(),
        sendSupportEmailUseCase = get()
    ) }

    viewModel<CreatePlaylistViewModel> {
        CreatePlaylistViewModel(get())
    }

    viewModel { PlaylistsViewModel(get(), get()) }

    viewModel { FavoriteTracksViewModel(
        getFavoriteTracksUseCase = get()
    ) }
}

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().favoriteTracksDao() }
    single { get<AppDatabase>().playlistsDao() }
}





