package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.HandlePlaybackCompletionUseCaseContract
import javax.inject.Inject

class HandlePlaybackCompletionUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : HandlePlaybackCompletionUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.pause()
    }
}