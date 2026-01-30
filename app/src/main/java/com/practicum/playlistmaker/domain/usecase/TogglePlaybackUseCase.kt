package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            if (!playerRepository.isPlaying()) {
                playerRepository.play()
                Result.success(true)
            } else {
                playerRepository.pause()
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}