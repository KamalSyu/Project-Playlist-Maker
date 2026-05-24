package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer

data class PlayerUiState(
    val playbackState: PlaybackState,
    val formattedTime: String,
    val playbackCompleted: Boolean,
    val shouldPoll: Boolean = false,
    val error: Throwable? = null,
    val isInitialized: Boolean = false,
    val isFavorite: Boolean = false,
    val currentTrack: Track? = null,

    val playlistsForBottomSheet: List<PlaylistForPlayer> = emptyList(),
    val isBottomSheetExpanded: Boolean = false,
    val isLoadingPlaylists: Boolean = false,
    val addTrackStatus: AddTrackStatus? = null,

    val isCreatingPlaylist: Boolean = false,

)