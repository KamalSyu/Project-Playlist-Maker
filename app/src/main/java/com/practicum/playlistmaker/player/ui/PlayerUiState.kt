package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.core.models.PlaybackState

data class PlayerUiState(
    val playbackState: PlaybackState,
    val formattedTime: String,
    val playbackCompleted: Boolean,
    val shouldPoll: Boolean = false,
    val error: Throwable? = null,  // Было: Exception?
    val isInitialized: Boolean = false
)
