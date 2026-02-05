package com.practicum.playlistmaker.domain.usecase

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

    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCaseContract {
        return AddTrackToHistoryUseCase(historyRepository)
    }
    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCaseContract {
        return ClearSearchHistoryUseCase(historyRepository)
    }
    fun createFilterTracksUseCase(): FilterTracksUseCaseContract {
        return FilterTracksUseCase()
    }
    fun createFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract {
        return FormatTrackDurationUseCase()
    }
    fun createGetCurrentPositionUseCase(): GetCurrentPositionUseCaseContract {
        return GetCurrentPositionUseCase(playerRepository)
    }
    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCaseContract {
        return GetSearchHistoryUseCase(historyRepository)
    }
    fun createGetThemeStateUseCase(): GetThemeStateUseCaseContract {
        return GetThemeStateUseCase(settingsRepository)
    }
    fun createHandlePlaybackCompletionUseCase(): HandlePlaybackCompletionUseCaseContract {
        return HandlePlaybackCompletionUseCase(playerRepository)
    }
    fun createPreparePlaybackUseCase(): PreparePlaybackUseCaseContract {
        return PreparePlaybackUseCase(playerRepository)
    }
    fun createSearchTracksUseCase(): SearchTracksUseCaseContract {
        return SearchTracksUseCase(itunesRepository)
    }
    fun createSendSupportEmailUseCase(): SendSupportEmailUseCaseContract {
        return SendSupportEmailUseCase()
    }
    fun createShareAppUseCase(): ShareAppUseCaseContract {
        return ShareAppUseCase()
    }
    fun createStopPlaybackUseCase(): StopPlaybackUseCaseContract {
        return StopPlaybackUseCase(playerRepository)
    }
    fun createSwitchThemeUseCase(): SwitchThemeUseCaseContract {
        return SwitchThemeUseCase(settingsRepository)
    }
    fun createTogglePlaybackUseCase(): TogglePlaybackUseCaseContract {
        return TogglePlaybackUseCase(playerRepository)
    }
}