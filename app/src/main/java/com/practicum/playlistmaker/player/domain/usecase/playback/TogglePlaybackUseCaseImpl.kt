package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class TogglePlaybackUseCaseImpl (
    private val playerRepository: PlayerRepository
) : TogglePlaybackUseCase {

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