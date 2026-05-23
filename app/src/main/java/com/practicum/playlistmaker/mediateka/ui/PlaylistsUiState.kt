package com.practicum.playlistmaker.mediateka.ui

import com.practicum.playlistmaker.core.models.domain.Playlist

sealed class PlaylistsUiState {
    object Loading : PlaylistsUiState()
    object Empty : PlaylistsUiState()
    data class Success(val playlists: List<Playlist>) : PlaylistsUiState()
    data class Error(val error: Throwable) : PlaylistsUiState()
}
