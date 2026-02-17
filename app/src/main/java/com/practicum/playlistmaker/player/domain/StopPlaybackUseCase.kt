package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import javax.inject.Inject

class StopPlaybackUseCase @Inject constructor (
    private val playerRepository: PlayerRepository
) : StopPlaybackUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.stop()
    }
}