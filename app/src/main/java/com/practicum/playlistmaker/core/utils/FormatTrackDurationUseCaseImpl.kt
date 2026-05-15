package com.practicum.playlistmaker.core.utils

import java.util.Locale

class FormatTrackDurationUseCaseImpl (
) : FormatTrackDurationUseCase {

    override operator fun invoke(durationMillis: Long): String {
        val minutes = durationMillis / 60_000
        val seconds = (durationMillis % 60_000) / 1_000
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}