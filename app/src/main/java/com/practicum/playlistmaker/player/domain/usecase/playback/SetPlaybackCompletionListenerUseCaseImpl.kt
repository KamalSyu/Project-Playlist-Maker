package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class SetPlaybackCompletionListenerUseCaseImpl (
    private val playerRepository: PlayerRepository
) : SetPlaybackCompletionListenerUseCase {

    override suspend fun invoke(onCompletion: () -> Unit) {
        playerRepository.setOnCompletionListener(onCompletion)
    }
}