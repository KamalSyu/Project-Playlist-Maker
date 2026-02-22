package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import javax.inject.Inject

class StopPlaybackUseCase @Inject constructor(
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

