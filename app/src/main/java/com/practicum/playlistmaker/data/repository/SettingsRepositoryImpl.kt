package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.presentation.util.Constants.Companion.DARK_THEME_KEY

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun saveTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_THEME_KEY, isDarkMode).apply()
    }

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
    }
}
