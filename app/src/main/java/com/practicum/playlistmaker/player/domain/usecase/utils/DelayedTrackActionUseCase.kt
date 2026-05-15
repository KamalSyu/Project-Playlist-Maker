package com.practicum.playlistmaker.player.domain.usecase.utils

import com.practicum.playlistmaker.core.models.Track

interface DelayedTrackActionUseCase {
    suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    )
}