package com.practicum.playlistmaker.domain.repository

// Назначение: реализация бизнес‑правил и сценариев использования.

// Настройки приложения
interface SettingsRepository {
    fun saveTheme(isDarkMode: Boolean)  // Сохранить тему
    fun isDarkThemeEnabled(): Boolean      // Получить текущую тему
}
