package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
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