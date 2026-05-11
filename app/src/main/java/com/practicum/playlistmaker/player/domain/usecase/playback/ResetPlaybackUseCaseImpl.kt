package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class ResetPlaybackUseCaseImpl (
    private val playerRepository: PlayerRepository
) : ResetPlaybackUseCase {

    override suspend operator fun invoke() {
        playerRepository.reset()
    }
}