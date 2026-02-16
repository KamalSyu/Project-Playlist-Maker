package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.SetPlaybackCompletionListenerUseCaseContract
import javax.inject.Inject

class SetPlaybackCompletionListenerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : SetPlaybackCompletionListenerUseCaseContract {

    override suspend fun invoke(onCompletion: () -> Unit) {
        playerRepository.setOnCompletionListener(onCompletion)
    }
}