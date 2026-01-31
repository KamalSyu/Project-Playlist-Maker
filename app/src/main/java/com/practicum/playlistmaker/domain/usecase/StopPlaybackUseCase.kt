package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class StopPlaybackUseCase @Inject constructor (
    private val playerRepository: PlayerRepository) : StopPlaybackUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.stop()
    }
}
