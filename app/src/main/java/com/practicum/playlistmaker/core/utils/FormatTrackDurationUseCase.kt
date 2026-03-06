package com.practicum.playlistmaker.core.utils

import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract

class FormatTrackDurationUseCase (
) : FormatTrackDurationUseCaseContract {

    override operator fun invoke(durationMillis: Long): String {
        val minutes = durationMillis / 60_000
        val seconds = (durationMillis % 60_000) / 1_000
        return String.format("%02d:%02d", minutes, seconds)
    }
}