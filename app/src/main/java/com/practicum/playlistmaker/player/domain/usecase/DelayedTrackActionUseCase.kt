package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.DelayProvider
import com.practicum.playlistmaker.core.contract.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.core.models.Track

/**
 * UseCase для выполнения действия с треком после заданной задержки.
 * Используется, например, для добавления трека в историю после короткого ожидания.
 */
class DelayedTrackActionUseCase (
    private val delayProvider: DelayProvider
) : DelayedTrackActionUseCaseContract {

    /**
     * Выполняет действие с треком после указанной задержки.
     * @param track трек, с которым нужно выполнить действие
     * @param delayMillis время задержки в миллисекундах
     * @param onDelayedAction действие, которое будет выполнено после задержки
     */
    override suspend operator fun invoke(
        track: Track,
        delayMillis: Long,
        onDelayedAction: (Track) -> Unit
    ) {
        delayProvider.delay(delayMillis)
        onDelayedAction(track)
    }
}
