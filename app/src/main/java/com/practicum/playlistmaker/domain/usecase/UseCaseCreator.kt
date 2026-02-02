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

    // === Поиск треков ===
    fun createSearchTracksUseCase(): SearchTracksUseCaseContract {
        return SearchTracksUseCase(itunesRepository)
    }

    fun createFilterTracksUseCase(): FilterTracksUseCaseContract {
        return FilterTracksUseCase()
    }

    // === История поиска ===
    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCaseContract {
        return AddTrackToHistoryUseCase(historyRepository)
    }

    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCaseContract {
        return GetSearchHistoryUseCase(historyRepository)
    }

    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCaseContract {
        return ClearSearchHistoryUseCase(historyRepository)
    }

    // === Управление темой ===
    fun createSwitchThemeUseCase(): SwitchThemeUseCaseContract {
        return SwitchThemeUseCase(settingsRepository)
    }

    fun createGetThemeStateUseCase(): GetThemeStateUseCaseContract {
        return GetThemeStateUseCase(settingsRepository)
    }

    // === Воспроизведение аудио ===
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

    // === Дополнительные UseCase ===
    fun createShareAppUseCase(): ShareAppUseCaseContract {
        return ShareAppUseCase()
    }

    fun createSendSupportEmailUseCase(): SendSupportEmailUseCaseContract {
        return SendSupportEmailUseCase()
    }

    fun createFormatTrackDurationUseCase(): FormatTrackDurationUseCase {
        return FormatTrackDurationUseCase()
    }
}