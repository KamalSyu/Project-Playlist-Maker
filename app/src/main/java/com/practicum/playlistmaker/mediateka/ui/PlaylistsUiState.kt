package com.practicum.playlistmaker.mediateka.ui

import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData

sealed class PlaylistsUiState {
    object Loading : PlaylistsUiState()
    data class Success(val playlists: List<PlaylistData>) : PlaylistsUiState()
    data class Error(val error: Throwable) : PlaylistsUiState()
    object Empty : PlaylistsUiState()
}