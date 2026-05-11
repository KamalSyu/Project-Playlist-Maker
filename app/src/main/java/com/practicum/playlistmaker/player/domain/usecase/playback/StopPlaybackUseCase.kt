package com.practicum.playlistmaker.player.domain.usecase.playback

interface StopPlaybackUseCase {
    suspend operator fun invoke() : Result<Unit>
}