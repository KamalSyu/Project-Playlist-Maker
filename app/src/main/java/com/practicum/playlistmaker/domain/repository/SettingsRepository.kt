package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.ThemeSettings

// Назначение: реализация бизнес‑правил и сценариев использования.

// Настройки приложения
interface SettingsRepository {
    fun saveTheme(settings: ThemeSettings)
    fun getThemeSettings(): ThemeSettings
}