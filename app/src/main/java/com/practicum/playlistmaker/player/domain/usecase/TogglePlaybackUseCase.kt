package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

/**
 * UseCase для переключения состояния воспроизведения: play ↔ pause.
 * Если воспроизведение не идёт — запускает его.
 * Если идёт — приостанавливает.
 */
class TogglePlaybackUseCase (
    private val playerRepository: PlayerRepository
) : TogglePlaybackUseCaseContract {

    /**
     * Переключает состояние воспроизведения:
     * - если не играло: запускает воспроизведение;
     * - если играло: приостанавливает воспроизведение.
     * @param seekPosition позиция для seekTo перед запуском (опционально);
     * @return Result.success(isPlaying) — текущее состояние воспроизведения после операции;
     *         Result.failure(e) — ошибка при выполнении.
     */
    override suspend operator fun invoke(seekPosition: Long?): Result<Boolean> {
        return try {
            val wasPlaying = playerRepository.isPlaying()

            if (!wasPlaying) {
                if (seekPosition != null) {
                    playerRepository.seekTo(seekPosition)
                }
                playerRepository.play()
            } else {
                playerRepository.pause()
            }

            Result.success(playerRepository.isPlaying())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
