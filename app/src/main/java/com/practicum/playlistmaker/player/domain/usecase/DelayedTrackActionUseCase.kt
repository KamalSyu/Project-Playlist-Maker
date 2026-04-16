package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.DelayProvider
import com.practicum.playlistmaker.core.contract.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.core.models.Track


class DelayedTrackActionUseCase (
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
