package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.DelayProvider
import com.practicum.playlistmaker.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import javax.inject.Inject

class DelayedTrackActionUseCase @Inject constructor(
    private val delayProvider: DelayProvider
) : DelayedTrackActionUseCaseContract {

    override suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    ) {
        delayProvider.delay(delayMillis)
        onDelayedAction(track)
    }
}