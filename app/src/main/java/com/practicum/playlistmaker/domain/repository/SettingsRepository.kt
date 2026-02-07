package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.ThemeSettings

interface SettingsRepository {
    fun saveTheme(settings: ThemeSettings)
    fun getThemeSettings(): ThemeSettings
}