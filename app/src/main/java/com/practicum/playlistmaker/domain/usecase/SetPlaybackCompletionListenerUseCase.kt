package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class SetPlaybackCompletionListenerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : SetPlaybackCompletionListenerUseCaseContract {

    override suspend fun invoke(onCompletion: () -> Unit) {
        playerRepository.setOnCompletionListener(onCompletion)
    }
}