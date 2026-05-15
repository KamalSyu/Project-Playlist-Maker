package com.practicum.playlistmaker.player.domain.usecase.playback

interface TogglePlaybackUseCase {
    suspend operator fun invoke(seekPosition: Long? = null): Result<Boolean>
}