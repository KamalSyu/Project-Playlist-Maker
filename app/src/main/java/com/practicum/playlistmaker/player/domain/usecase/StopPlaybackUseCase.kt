package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class StopPlaybackUseCase (
    private val playerRepository: PlayerRepository
) : StopPlaybackUseCaseContract {

    override suspend fun invoke(): Result<Unit> {
        return try {
            playerRepository.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
