
package com.practicum.playlistmaker.domain.usecase

import android.content.Context
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import javax.inject.Inject


class UseCaseCreator @Inject constructor(
    private val itunesRepository: ItunesRepository,
    private val historyRepository: HistoryRepository,
    private val playerRepository: PlayerRepository,
    private val settingsRepository: SettingsRepository,
) {

    // Поиск треков
    fun createSearchTracksUseCase(): SearchTracksUseCaseContract {
        return SearchTracksUseCase(itunesRepository)
    }

    fun createFilterTracksUseCase(): FilterTracksUseCaseContract {
        return FilterTracksUseCase()  // ← Добавлены зависимости
    }

    // История поиска
    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCaseContract {
        return AddTrackToHistoryUseCase(historyRepository)
    }

    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCaseContract {
        return GetSearchHistoryUseCase(historyRepository)
    }

    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCaseContract {
        return ClearSearchHistoryUseCase(historyRepository)
    }

    // Темы приложения
    fun createSwitchThemeUseCase(): SwitchThemeUseCaseContract {
        return SwitchThemeUseCase(settingsRepository)
    }

    fun createGetThemeStateUseCase(): GetThemeStateUseCaseContract {
        return GetThemeStateUseCase(settingsRepository)
    }

    // Управление воспроизведением
    fun createPreparePlaybackUseCase(): PreparePlaybackUseCaseContract {
        return PreparePlaybackUseCase(playerRepository)
    }

    fun createTogglePlaybackUseCase(): TogglePlaybackUseCaseContract {
        return TogglePlaybackUseCase(playerRepository)
    }

    fun createStopPlaybackUseCase(): StopPlaybackUseCaseContract {
        return StopPlaybackUseCase(playerRepository)
    }

    fun createGetCurrentPositionUseCase(): GetCurrentPositionUseCaseContract {
        return GetCurrentPositionUseCase(playerRepository)
    }

    fun createHandlePlaybackCompletionUseCase(): HandlePlaybackCompletionUseCaseContract {
        return HandlePlaybackCompletionUseCase(playerRepository)
    }

    // Поддержка и шаринг
    fun createSendSupportEmailUseCase(): SendSupportEmailUseCaseContract {
        return SendSupportEmailUseCase()  // ← Зависимость добавлена
    }

    fun createShareAppUseCase(): ShareAppUseCaseContract {
        return ShareAppUseCase()  // ← Зависимость добавлена
    }
}

//package com.practicum.playlistmaker.domain.usecase
//
// import com.practicum.playlistmaker.domain.repository.HistoryRepository
// import com.practicum.playlistmaker.domain.repository.ItunesRepository
// import com.practicum.playlistmaker.domain.repository.PlayerRepository
// import com.practicum.playlistmaker.domain.repository.SettingsRepository
// import javax.inject.Inject
//
//class UseCaseCreator @Inject constructor(
//    private val itunesRepository: ItunesRepository,
//    private val historyRepository: HistoryRepository,
//    private val playerRepository: PlayerRepository,
//    private val settingsRepository: SettingsRepository
//) {
//    fun createSearchTracksUseCase(): SearchTracksUseCase {
//        return SearchTracksUseCase(itunesRepository)
//    }
//
//    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCase {
//        return AddTrackToHistoryUseCase(historyRepository)
//    }
//
//    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase {
//        return GetSearchHistoryUseCase(historyRepository)
//    }
//
//    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCase {
//        return ClearSearchHistoryUseCase(historyRepository)
//    }
//
//    fun createFilterTracksUseCase(): FilterTracksUseCase {
//        return FilterTracksUseCase()
//    }
//
//    fun createSwitchThemeUseCase(): SwitchThemeUseCase {
//        return SwitchThemeUseCase(settingsRepository)
//    }
//
//    fun createGetThemeStateUseCase(): GetThemeStateUseCase {
//        return GetThemeStateUseCase(settingsRepository)
//    }
//}
