package com.practicum.playlistmaker.domain.usecase

import javax.inject.Inject

class FormatTrackDurationUseCase @Inject constructor() {
    operator fun invoke(durationMillis: Long): String {
        val minutes = durationMillis / 60_000
        val seconds = (durationMillis % 60_000) / 1_000
        return String.format("%02d:%02d", minutes, seconds)
    }
}
