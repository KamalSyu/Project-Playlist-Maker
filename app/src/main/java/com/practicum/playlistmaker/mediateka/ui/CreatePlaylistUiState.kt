package com.practicum.playlistmaker.mediateka.ui

import android.net.Uri

data class CreatePlaylistUiState(
    val playlistName: String = "",
    val playlistDescription: String = "",
    val selectedCoverUri: Uri? = null,
    val createPlaylistError: String? = null,
    val createPlaylistSuccess: String? = null
)