package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class GetCurrentPositionUseCase @Inject constructor (
    private val playerRepository: PlayerRepository) : GetCurrentPositionUseCaseContract{

    override operator fun invoke(): Long {
        return playerRepository.getCurrentPosition()
    }
}