package com.practicum.playlistmaker.player.domain.usecase.playback

interface PreparePlaybackUseCase{
    suspend operator fun invoke(previewUrl: String?): Result<Unit>
}