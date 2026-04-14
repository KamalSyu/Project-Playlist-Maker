package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.core.constants.Constants
import com.practicum.playlistmaker.core.contract.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.DelayProvider
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
import com.practicum.playlistmaker.core.utils.CoroutineDelayProvider
import com.practicum.playlistmaker.core.utils.DateFormatter
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.mediateka.ui.view.FragmentFavoritesViewModel
import com.practicum.playlistmaker.mediateka.ui.view.FragmentPlaylistsViewModel
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import com.practicum.playlistmaker.search.data.network.ItunesApi
import com.practicum.playlistmaker.search.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.ItunesRepositoryImpl
import com.practicum.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.sharing.data.provider.SupportEmailDataProviderImpl
import com.practicum.playlistmaker.sharing.data.provider.ShareTextProviderImpl
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
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
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.FilterTracksUseCase
import com.practicum.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.SearchTracksUseCase
import com.practicum.playlistmaker.search.ui.view.SearchViewModel
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.settings.domain.usecase.GetThemeStateUseCase
import com.practicum.playlistmaker.settings.domain.usecase.SwitchThemeUseCase
import com.practicum.playlistmaker.settings.ui.view.SettingsViewModel
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.sharing.domain.usecase.SendSupportEmailUseCase
import com.practicum.playlistmaker.sharing.domain.usecase.ShareAppUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.dsl.module

val appModule = module {
    // 1. Базовые компоненты (без зависимостей)
    single<Context> { get<Application>().applicationContext }

    single<SharedPreferences> {
        get<Context>().getSharedPreferences(
            Constants.PREFERENCES,
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

    // 2. Утилиты (без внешних зависимостей)
    single<DelayProvider> { CoroutineDelayProvider() }
    single { DateFormatter() }
    single<FormatTrackDurationUseCaseContract> { FormatTrackDurationUseCase() }

    // 3. Фабрики и базовые мапперы (без зависимостей)
    single { TrackFactory() }
    single { ThemeSettingsMapper() }
    single { TrackParcelableMapper() }

    // 4. Мапперы с зависимостями (регистрируются после базовых компонентов)
    single { TrackMapper(get()) } // Зависит от TrackFactory
    single { SearchResponseMapper(get()) } // Зависит от TrackMapper
    single { SearchHistoryMapper(get()) } // Зависит от TrackMapper

    // 5. Репозитории (регистрируются после всех своих зависимостей)
    single<PlayerRepository> { PlayerRepositoryImpl() }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get(), // sharedPreferences
            get(), // gson
            get()  // themeSettingsMapper
        )
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            get(), // sharedPreferences
            get(), // gson
//            get()  // searchHistoryMapper
        )
    }

    single<ItunesRepository> {
        ItunesRepositoryImpl(
            get(), // api
            get()  // searchResponseMapper
        )
    }

    single<SupportEmailDataProvider> { SupportEmailDataProviderImpl(get()) }
    single<ShareTextProvider> { ShareTextProviderImpl(get()) }

    // 6. UseCases (регистрируются после репозиториев и мапперов)
    // UseCases поиска
    factory<SearchTracksUseCaseContract> { SearchTracksUseCase(get()) }
    factory<AddTrackToHistoryUseCaseContract> { AddTrackToHistoryUseCase(get()) }
    factory<ClearSearchHistoryUseCaseContract> { ClearSearchHistoryUseCase(get()) }
    factory<GetSearchHistoryUseCaseContract> { GetSearchHistoryUseCase(get()) }
    factory<FilterTracksUseCaseContract> { FilterTracksUseCase() }

    // UseCases плеера
    factory<PreparePlaybackUseCaseContract> { PreparePlaybackUseCase(get()) }
    factory<TogglePlaybackUseCaseContract> { TogglePlaybackUseCase(get()) }
    factory<StopPlaybackUseCaseContract> { StopPlaybackUseCase(get()) }
    factory<GetCurrentPositionUseCaseContract> { GetCurrentPositionUseCase(get()) }
    factory<SetPlaybackCompletionListenerUseCaseContract> { SetPlaybackCompletionListenerUseCase(get()) }
    factory<DelayedTrackActionUseCaseContract> { DelayedTrackActionUseCase(get()) }
    factory<ResetPlaybackUseCaseContract> { ResetPlaybackUseCase(get()) }

    // UseCases настроек
    factory<GetThemeStateUseCaseContract> { GetThemeStateUseCase(get()) }
    factory<SwitchThemeUseCaseContract> { SwitchThemeUseCase(get()) }

    // UseCases шаринга
    factory<ShareAppUseCaseContract> { ShareAppUseCase(get()) }
    factory<SendSupportEmailUseCaseContract> { SendSupportEmailUseCase(get()) }

    // 7. ViewModel (регистрируются последними, после всех UseCases)
    viewModel { SearchViewModel(
        get(), // searchTracksUseCase
        get(), // addTrackToHistoryUseCase
        get(), // getSearchHistoryUseCase
        get(), // clearSearchHistoryUseCase
        get(), // filterTracksUseCase
        get(), // delayedTrackActionUseCase
        get()  // formatTrackDurationUseCase
    ) }

    viewModel { AudioPlayerViewModel(
        get(), // preparePlaybackUseCase
        get(), // togglePlaybackUseCase
        get(), // stopPlaybackUseCase
        get(), // getCurrentPositionUseCase
        get(), // setCompletionListenerUseCase
        get(), // resetPlaybackUseCase
        get(),  // formatTrackDurationUseCase
        get()  // trackParcelableMapper
    ) }

    viewModel { SettingsViewModel(
        get(), // getThemeStateUseCase
        get(), // switchThemeUseCase
        get(), // shareAppUseCase
        get()  // sendSupportEmailUseCase
    ) }

    // Добавляем ViewModel для фрагментов медиатеки
    viewModel { FragmentPlaylistsViewModel() }
    viewModel { FragmentFavoritesViewModel() }
}
