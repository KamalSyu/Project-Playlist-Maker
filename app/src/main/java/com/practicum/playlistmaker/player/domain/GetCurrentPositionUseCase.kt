package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import javax.inject.Inject

class GetCurrentPositionUseCase @Inject constructor (
    private val playerRepository: PlayerRepository
) : GetCurrentPositionUseCaseContract {

    override operator fun invoke(): Long {
        return playerRepository.getCurrentPosition()
    }
}