package com.practicum.playlistmaker.core.usecase

import com.practicum.playlistmaker.core.contract.*
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.player.domain.*
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.search.domain.*
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.settings.domain.GetThemeStateUseCase
import com.practicum.playlistmaker.settings.domain.SwitchThemeUseCase
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.sharing.domain.SendSupportEmailUseCase
import com.practicum.playlistmaker.sharing.domain.ShareAppUseCase
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import javax.inject.Inject

class UseCaseCreator @Inject constructor(
    private val itunesRepository: ItunesRepository,
    private val historyRepository: HistoryRepository,
    private val playerRepository: PlayerRepository,
    private val settingsRepository: SettingsRepository,
    private val delayProvider: DelayProvider,
    private val shareTextProvider: ShareTextProvider,
    private val supportEmailDataProvider: SupportEmailDataProvider
) {

    // Search domain
    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCaseContract =
        AddTrackToHistoryUseCase(historyRepository)

    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCaseContract =
        ClearSearchHistoryUseCase(historyRepository)

    fun createFilterTracksUseCase(): FilterTracksUseCaseContract =
        FilterTracksUseCase()

    fun createSearchTracksUseCase(): SearchTracksUseCaseContract =
        SearchTracksUseCase(itunesRepository)

    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCaseContract =
        GetSearchHistoryUseCase(historyRepository)

    // Player domain
    fun createPreparePlaybackUseCase(): PreparePlaybackUseCaseContract =
        PreparePlaybackUseCase(playerRepository)

    fun createTogglePlaybackUseCase(): TogglePlaybackUseCaseContract =
        TogglePlaybackUseCase(playerRepository)

    fun createStopPlaybackUseCase(): StopPlaybackUseCaseContract =
        StopPlaybackUseCase(playerRepository)

    fun createGetCurrentPositionUseCase(): GetCurrentPositionUseCaseContract =
        GetCurrentPositionUseCase(playerRepository)

    fun createGetPlaybackPositionUseCase(): GetPlaybackPositionUseCaseContract =
        GetPlaybackPositionUseCase(playerRepository)

    fun createHandlePlaybackCompletionUseCase(): HandlePlaybackCompletionUseCaseContract =
        HandlePlaybackCompletionUseCase(playerRepository)

    fun createSetPlaybackCompletionListenerUseCase(): SetPlaybackCompletionListenerUseCaseContract =
        SetPlaybackCompletionListenerUseCase(playerRepository)

    fun createDelayedTrackActionUseCase(): DelayedTrackActionUseCaseContract =
        DelayedTrackActionUseCase(delayProvider)

    // Settings domain
    fun createGetThemeStateUseCase(): GetThemeStateUseCaseContract =
        GetThemeStateUseCase(settingsRepository)

    fun createSwitchThemeUseCase(): SwitchThemeUseCaseContract =
        SwitchThemeUseCase(settingsRepository)

    // Sharing domain
    fun createShareAppUseCase(): ShareAppUseCaseContract =
        ShareAppUseCase(shareTextProvider)

    fun createSendSupportEmailUseCase(): SendSupportEmailUseCaseContract =
        SendSupportEmailUseCase(supportEmailDataProvider)

    // Utils
    fun createFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract =
        FormatTrackDurationUseCase()

    fun createResetPlaybackUseCase(): ResetPlaybackUseCaseContract =
        ResetPlaybackUseCase(playerRepository)


}
