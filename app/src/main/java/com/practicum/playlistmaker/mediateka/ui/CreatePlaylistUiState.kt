package com.practicum.playlistmaker.mediateka.ui

import android.net.Uri

data class CreatePlaylistUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val playlistId: String? = null,
    val playlistName: String = "",
    val playlistDescription: String = "",
    val selectedCoverUri: Uri? = null,
    val successMessage: String? = null
)
