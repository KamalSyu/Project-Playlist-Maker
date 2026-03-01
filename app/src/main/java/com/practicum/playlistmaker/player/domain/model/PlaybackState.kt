package com.practicum.playlistmaker.player.domain.model

data class PlaybackState(
    val isPlaying: Boolean,
    val position: Long
)