package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class StopPlaybackUseCaseImpl (
    private val playerRepository: PlayerRepository
) : StopPlaybackUseCase {

    override suspend fun invoke(): Result<Unit> {
        return try {
            playerRepository.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}