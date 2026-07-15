package com.practicum.playlistmaker.mediateka.ui

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity

sealed class PlaylistDetailUiState {
    object Loading : PlaylistDetailUiState()
    data class Success(
        val playlist: Playlist,
        val tracks: List<PlaylistTrackEntity>
    ) : PlaylistDetailUiState()
    data class Error(val error: Throwable) : PlaylistDetailUiState()
}

