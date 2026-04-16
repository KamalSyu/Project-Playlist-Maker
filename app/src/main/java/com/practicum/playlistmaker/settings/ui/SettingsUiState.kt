package com.practicum.playlistmaker.settings.ui

sealed class SettingsUiState {

    object Loading : SettingsUiState()
    data class Loaded(
        val isDarkTheme: Boolean
    ) : SettingsUiState()
}
