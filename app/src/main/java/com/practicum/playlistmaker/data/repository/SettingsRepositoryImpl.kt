package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun saveTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("dark_theme", isDarkMode).apply()
    }

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_theme", false)
    }
}
