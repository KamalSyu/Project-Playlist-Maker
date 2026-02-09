package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class TogglePlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : TogglePlaybackUseCaseContract {

    override suspend operator fun invoke(seekPosition: Long?): Result<Boolean> {
        return try {
            val wasPlaying = playerRepository.isPlaying()

            if (!wasPlaying) {
                if (seekPosition != null) {
                    playerRepository.seekTo(seekPosition)
                }
                playerRepository.play()
            } else {
                playerRepository.pause()
            }

            Result.success(playerRepository.isPlaying())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
