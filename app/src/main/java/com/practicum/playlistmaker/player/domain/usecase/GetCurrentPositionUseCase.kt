package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

/**
 * UseCase для получения текущей позиции воспроизведения в миллисекундах.
 * Позволяет узнать, на каком моменте сейчас находится воспроизведение.
 */
class GetCurrentPositionUseCase  (
    private val playerRepository: PlayerRepository
) : GetCurrentPositionUseCaseContract {

    /**
     * Возвращает текущую позицию воспроизведения.
     * Если медиаплеер недоступен, возвращает сохранённую позицию.
     * @return текущая позиция воспроизведения в миллисекундах
     */
    override operator fun invoke(): Long {
        return playerRepository.getCurrentPosition()
    }
}
