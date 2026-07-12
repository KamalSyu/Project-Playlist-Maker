package com.practicum.playlistmaker.mediateka.ui

import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.core.models.domain.Playlist

sealed class PlaylistsUiState {
    object Loading : PlaylistsUiState()
    object Empty : PlaylistsUiState()
    data class Success(
        val playlists: List<Playlist>,
        val addTrackStatus: AddTrackStatus? = null
    ) : PlaylistsUiState()
    data class Error(val error: Throwable) : PlaylistsUiState()
}

