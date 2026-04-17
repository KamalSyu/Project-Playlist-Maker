package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class ResetPlaybackUseCase (
    private val playerRepository: PlayerRepository
) : ResetPlaybackUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.reset()
    }
}
