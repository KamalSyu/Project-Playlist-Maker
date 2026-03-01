package com.practicum.playlistmaker.settings.domain.repository

import com.practicum.playlistmaker.settings.domain.model.ThemeSettings

interface SettingsRepository {
    fun saveTheme(settings: ThemeSettings)
    fun getThemeSettings(): ThemeSettings
}