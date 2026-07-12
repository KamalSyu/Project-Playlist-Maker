package com.practicum.playlistmaker.player.domain.usecase.utils

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.DelayProvider

class DelayedTrackActionUseCaseImpl (
    private val delayProvider: DelayProvider
) : DelayedTrackActionUseCase {
    override suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    ) {
        delayProvider.delay(delayMillis)
        onDelayedAction(track)
    }
}