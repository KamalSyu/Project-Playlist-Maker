package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class GetCurrentPositionUseCaseImpl  (
    private val playerRepository: PlayerRepository
) : GetCurrentPositionUseCase {

    override operator fun invoke(): Long {
        return playerRepository.getCurrentPosition()
    }
}