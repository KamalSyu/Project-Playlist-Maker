package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * UseCase для остановки воспроизведения аудио.
 * Приостанавливает медиаплеер и сохраняет текущую позицию.
 */
class StopPlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : StopPlaybackUseCaseContract {

    /**
     * Останавливает воспроизведение аудио.
     * @return Result.success(Unit) при успешной остановке,
     *         Result.failure(e) при ошибке
     */
    override suspend fun invoke(): Result<Unit> {
        return try {
            playerRepository.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}