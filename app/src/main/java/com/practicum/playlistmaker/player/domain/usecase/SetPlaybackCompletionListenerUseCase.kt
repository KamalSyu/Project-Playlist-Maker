package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class SetPlaybackCompletionListenerUseCase (
    private val playerRepository: PlayerRepository
) : SetPlaybackCompletionListenerUseCaseContract {

    override suspend fun invoke(onCompletion: () -> Unit) {
        playerRepository.setOnCompletionListener(onCompletion)
    }
}
