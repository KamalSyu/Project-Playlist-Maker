package com.practicum.playlistmaker.domain.usecase


import com.practicum.playlistmaker.domain.model.Track
import kotlinx.coroutines.delay
import javax.inject.Inject

class DelayedTrackActionUseCase @Inject constructor() : DelayedTrackActionUseCaseContract {

    override suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    ) {
        delay(delayMillis)
        onDelayedAction(track)
    }
}