package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

/**
 * UseCase для подготовки аудиофайла к воспроизведению.
 * Инициализирует медиаплеер и подготавливает трек по указанному URL.
 */
class PreparePlaybackUseCase (
    private val playerRepository: PlayerRepository
) : PreparePlaybackUseCaseContract {

    /**
     * Подготавливает аудиофайл к воспроизведению по указанному URL.
     * @param previewUrl URL аудиофайла для воспроизведения
     * @return Result.success(Unit) при успешной подготовке,
     *         Result.failure(e) при ошибке
     */
    override suspend operator fun invoke(previewUrl: String?): Result<Unit> {
        return try {
            playerRepository.prepare(previewUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
