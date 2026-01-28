package com.practicum.playlistmaker.domain.repository

interface SettingsRepository {
    fun saveTheme(isDarkMode: Boolean)
    fun isDarkThemeEnabled(): Boolean
}
