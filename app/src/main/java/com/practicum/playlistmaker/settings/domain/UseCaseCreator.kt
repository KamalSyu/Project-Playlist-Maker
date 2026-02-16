package com.practicum.playlistmaker.settings.domain

import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.DelayProvider
import com.practicum.playlistmaker.player.domain.DelayedTrackActionUseCase
import com.practicum.playlistmaker.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.FilterTracksUseCaseContract
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.HandlePlaybackCompletionUseCaseContract
import com.practicum.playlistmaker.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.SearchTracksUseCaseContract
import com.practicum.playlistmaker.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.ShareAppUseCaseContract
import com.practicum.playlistmaker.ShareTextProvider
import com.practicum.playlistmaker.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.GetCurrentPositionUseCase
import com.practicum.playlistmaker.player.domain.HandlePlaybackCompletionUseCase
import com.practicum.playlistmaker.player.domain.PreparePlaybackUseCase
import com.practicum.playlistmaker.player.domain.SetPlaybackCompletionListenerUseCase
import com.practicum.playlistmaker.player.domain.StopPlaybackUseCase
import com.practicum.playlistmaker.player.domain.TogglePlaybackUseCase
import com.practicum.playlistmaker.search.domain.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.search.domain.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.FilterTracksUseCase
import com.practicum.playlistmaker.search.domain.GetSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.SearchTracksUseCase
import com.practicum.playlistmaker.sharing.domain.SendSupportEmailUseCase
import com.practicum.playlistmaker.sharing.domain.ShareAppUseCase
import javax.inject.Inject

class UseCaseCreator @Inject constructor(
    private val itunesRepository: ItunesRepository,
    private val historyRepository: HistoryRepository,
    private val playerRepository: PlayerRepository,
    private val settingsRepository: SettingsRepository,
    private val delayProvider: DelayProvider,
    private val shareTextProvider: ShareTextProvider,
    private val supportEmailDataProvider: SupportEmailDataProvider,
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
        return SendSupportEmailUseCase(supportEmailDataProvider)
    }
    fun createShareAppUseCase(): ShareAppUseCaseContract {
        return ShareAppUseCase(shareTextProvider)
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
    fun createDelayedTrackActionUseCase(): DelayedTrackActionUseCaseContract {
        return DelayedTrackActionUseCase(delayProvider)
    }
    fun createSetPlaybackCompletionListenerUseCase(): SetPlaybackCompletionListenerUseCaseContract {
        return SetPlaybackCompletionListenerUseCase(playerRepository)
    }
}