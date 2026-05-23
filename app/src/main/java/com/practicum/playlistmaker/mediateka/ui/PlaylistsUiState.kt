package com.practicum.playlistmaker.mediateka.ui

import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka

sealed class PlaylistsUiState {
    object Loading : PlaylistsUiState()
    object Empty : PlaylistsUiState()
    data class Success(val playlists: List<PlaylistForMediateka>) : PlaylistsUiState()
    data class Error(val error: Throwable) : PlaylistsUiState() // Добавляем эту строку
}
