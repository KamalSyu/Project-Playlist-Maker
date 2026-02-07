package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : TogglePlaybackUseCaseContract {

    override suspend operator fun invoke(): Result<Boolean> {
        return try {
            val wasPlaying = playerRepository.isPlaying()
            if (!wasPlaying) {
                // Проверяем, что MediaPlayer готов к воспроизведению
                if (playerRepository.getCurrentPosition() >= 0L) {
                    playerRepository.play()
                } else {
                    return Result.failure(Exception("MediaPlayer не готов"))
                }
            } else {
                playerRepository.pause()
            }
            Result.success(playerRepository.isPlaying())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


