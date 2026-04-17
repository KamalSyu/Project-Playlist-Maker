package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class GetCurrentPositionUseCase  (
    private val playerRepository: PlayerRepository
) : GetCurrentPositionUseCaseContract {

    override operator fun invoke(): Long {
        return playerRepository.getCurrentPosition()
    }
}
