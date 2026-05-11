package com.practicum.playlistmaker.core.utils

interface FormatTrackDurationUseCase {
    operator fun invoke(durationMillis: Long): String
}