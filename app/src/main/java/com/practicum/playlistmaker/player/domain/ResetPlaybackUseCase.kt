package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * UseCase для полного сброса состояния медиаплеера.
 * Освобождает ресурсы медиаплеера и сбрасывает все внутренние состояния.
 */
class ResetPlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : ResetPlaybackUseCaseContract {

    /**
     * Полностью сбрасывает состояние медиаплеера:
     * - освобождает ресурсы медиаплеера;
     * - обнуляет сохранённую позицию;
     * - сбрасывает флаг готовности;
     * - удаляет слушатель завершения.
     */
    override suspend operator fun invoke() {
        playerRepository.reset()
    }
}
