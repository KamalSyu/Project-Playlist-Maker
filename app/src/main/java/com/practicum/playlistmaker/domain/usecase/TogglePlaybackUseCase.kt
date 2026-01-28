package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            if (playerRepository.isPlaying()) {
                playerRepository.pause()
            } else {
                playerRepository.play()
            }
            Result.success(playerRepository.isPlaying())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
