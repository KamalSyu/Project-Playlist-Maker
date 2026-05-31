package com.practicum.playlistmaker.mediateka.ui

import android.net.Uri

data class CreatePlaylistUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val playlistId: String? = null,
    val playlistName: String = "",
    val playlistDescription: String = "",
    val coverFilePath: String? = null,
    val selectedCoverUri: Uri? = null,
    val successMessage: String? = null,
    val showExitDialog: Boolean = false
) {
    val isCreateButtonEnabled: Boolean
        get() = !isLoading && playlistName.trim().isNotBlank()
}

