package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track

interface SearchTracksUseCaseContract {
    suspend operator fun invoke(query: String): Result<List<Track>>
}

interface AddTrackToHistoryUseCaseContract {
    suspend operator fun invoke(track: Track)
}

interface GetSearchHistoryUseCaseContract {
    suspend operator fun invoke(): List<Track>
}

interface ClearSearchHistoryUseCaseContract {
    suspend operator fun invoke()
}

interface FilterTracksUseCaseContract {
    operator fun invoke(tracks: List<Track>, query: String): List<Track>
}

interface SwitchThemeUseCaseContract {
    operator fun invoke(isDarkMode: Boolean)
}

interface GetThemeStateUseCaseContract {
    operator fun invoke(): Boolean
}

interface PreparePlaybackUseCaseContract {
    suspend operator fun invoke(previewUrl: String?): Result<Unit>
}

interface TogglePlaybackUseCaseContract {
    suspend operator fun invoke(): Result<Boolean>
}

interface StopPlaybackUseCaseContract {
    suspend operator fun invoke()
}

interface GetCurrentPositionUseCaseContract {
    operator fun invoke(): Long
}

interface HandlePlaybackCompletionUseCaseContract {
    suspend operator fun invoke()
}

interface SendSupportEmailUseCaseContract {
    operator fun invoke(): SupportEmailIntentData
}

interface ShareAppUseCaseContract {
    operator fun invoke(): String
}
