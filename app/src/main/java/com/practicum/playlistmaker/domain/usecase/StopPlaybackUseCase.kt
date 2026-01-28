package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class StopPlaybackUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
    suspend operator fun invoke() {
        playerRepository.stop()
    }
}
