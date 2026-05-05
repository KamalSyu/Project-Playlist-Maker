package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.player.domain.model.PlaybackState

data class PlayerUiState(
    val playbackState: PlaybackState,
    val formattedTime: String,
    val playbackCompleted: Boolean,
    val shouldPoll: Boolean = false,
    val error: Throwable? = null,
    val isInitialized: Boolean = false,
    val isFavorite: Boolean = false
)
