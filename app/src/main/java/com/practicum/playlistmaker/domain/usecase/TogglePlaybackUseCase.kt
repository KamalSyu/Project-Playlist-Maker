package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor (
    private val playerRepository: PlayerRepository
) : TogglePlaybackUseCaseContract {

    override suspend operator fun invoke(): Result<Boolean> {
        return try {
            if (!playerRepository.isPlaying()) {
                playerRepository.play()
            } else {
                playerRepository.pause()
            }
            // Возвращаем актуальное состояние после действия
            Result.success(playerRepository.isPlaying())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}