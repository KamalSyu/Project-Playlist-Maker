package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.TogglePlaybackUseCaseContract
import javax.inject.Inject

class StopPlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val getCurrentPositionUseCase: GetCurrentPositionUseCaseContract,
    private val togglePlaybackUseCase: TogglePlaybackUseCaseContract
) : StopPlaybackUseCaseContract {


    override suspend fun invoke(): Result<Unit> {
        return try {
            val isPlaying = playerRepository.isPlaying()
            if (isPlaying) {
                val currentPosition = getCurrentPositionUseCase()
                val pauseResult = togglePlaybackUseCase(null)
                if (!pauseResult.isSuccess) {
                    return Result.failure(pauseResult.exceptionOrNull()!!)
                }
            }
            playerRepository.stop()  // Только stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
