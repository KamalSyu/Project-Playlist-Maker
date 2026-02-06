package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class HandlePlaybackCompletionUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : HandlePlaybackCompletionUseCaseContract {

    override suspend operator fun invoke() {
        playerRepository.stop()
        playerRepository.reset()
    }
}
