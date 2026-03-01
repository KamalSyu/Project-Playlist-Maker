package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * UseCase для установки слушателя события завершения воспроизведения.
 * Позволяет реагировать на окончание воспроизведения аудио.
 */
class SetPlaybackCompletionListenerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : SetPlaybackCompletionListenerUseCaseContract {

    /**
     * Устанавливает слушатель события завершения воспроизведения.
     * @param onCompletion функция, которая будет вызвана при завершении воспроизведения
     */
    override suspend fun invoke(onCompletion: () -> Unit) {
        playerRepository.setOnCompletionListener(onCompletion)
    }
}