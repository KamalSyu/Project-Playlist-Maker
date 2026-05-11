package com.practicum.playlistmaker.player.domain.usecase.playback

interface SetPlaybackCompletionListenerUseCase {
    suspend operator fun invoke(onCompletion: () -> Unit)
}