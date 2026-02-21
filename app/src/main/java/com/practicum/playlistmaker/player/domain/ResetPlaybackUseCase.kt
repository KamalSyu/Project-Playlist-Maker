package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject

class ResetPlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : ResetPlaybackUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.reset()
    }
}

