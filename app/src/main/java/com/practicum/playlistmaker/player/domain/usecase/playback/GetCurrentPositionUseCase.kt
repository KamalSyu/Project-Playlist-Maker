package com.practicum.playlistmaker.player.domain.usecase.playback

interface GetCurrentPositionUseCase {
    operator fun invoke(): Long
}