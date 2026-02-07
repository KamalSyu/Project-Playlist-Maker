package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.domain.model.Track

interface AddTrackToHistoryUseCaseContract {
    suspend operator fun invoke(track: Track)
}

interface ClearSearchHistoryUseCaseContract {
    suspend operator fun invoke()
}

interface FilterTracksUseCaseContract {
    operator fun invoke(tracks: List<Track>, query: String): List<Track>
}

interface FormatTrackDurationUseCaseContract {
    operator fun invoke(durationMillis: Long): String
}

interface GetCurrentPositionUseCaseContract {
    operator fun invoke(): Long
}

interface GetSearchHistoryUseCaseContract {
    suspend operator fun invoke(): List<Track>
}

interface GetThemeStateUseCaseContract {
    operator fun invoke(): Boolean
}

interface HandlePlaybackCompletionUseCaseContract {
    suspend operator fun invoke()
}

interface PreparePlaybackUseCaseContract {
    suspend operator fun invoke(previewUrl: String?): Result<Unit>
}

interface SearchTracksUseCaseContract {
    suspend operator fun invoke(query: String): Result<List<Track>>
}

interface SendSupportEmailUseCaseContract {
    operator fun invoke(): SupportEmailIntentData
}

interface ShareAppUseCaseContract {
    operator fun invoke(): String
}

interface StopPlaybackUseCaseContract {
    suspend operator fun invoke()
}

interface SwitchThemeUseCaseContract {
    operator fun invoke(isDarkMode: Boolean)
}

interface TogglePlaybackUseCaseContract {
    suspend operator fun invoke(seekPosition: Long? = null): Result<Boolean>
}

interface DelayedTrackActionUseCaseContract {
    suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    )
}
interface SetPlaybackCompletionListenerUseCaseContract {
    suspend operator fun invoke(onCompletion: () -> Unit)
}

interface DelayProvider {
    suspend fun delay(millis: Long)
}

interface ShareTextProvider {
    fun getShareText(): String
}







