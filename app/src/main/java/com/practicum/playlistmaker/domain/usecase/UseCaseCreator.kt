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
    private val settingsRepository: SettingsRepository
) {
    fun createSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(itunesRepository)
    }

    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCase {
        return AddTrackToHistoryUseCase(historyRepository)
    }

    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase {
        return GetSearchHistoryUseCase(historyRepository)
    }

    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCase {
        return ClearSearchHistoryUseCase(historyRepository)
    }

    fun createFilterTracksUseCase(): FilterTracksUseCase {
        return FilterTracksUseCase()
    }

    fun createSwitchThemeUseCase(): SwitchThemeUseCase {
        return SwitchThemeUseCase(settingsRepository)
    }

    fun createGetThemeStateUseCase(): GetThemeStateUseCase {
        return GetThemeStateUseCase(settingsRepository)
    }
}
