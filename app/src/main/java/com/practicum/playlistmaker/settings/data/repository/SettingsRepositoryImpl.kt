package com.practicum.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.settings.data.dto.ThemeSettingsDTO
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper

class SettingsRepositoryImpl (
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: ThemeSettingsMapper
) : SettingsRepository {

    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
    }

    override fun saveTheme(settings: ThemeSettings) {
        val dto = dtoMapper.toDto(settings)
        val json = gson.toJson(dto)
        sharedPreferences.edit().putString(DARK_THEME_KEY, json).apply()
    }

    override fun getThemeSettings(): ThemeSettings {
        if (!sharedPreferences.contains(DARK_THEME_KEY)) {
            return ThemeSettings(isDarkTheme = false)
        }
        try {
            val json = sharedPreferences.getString(DARK_THEME_KEY, null)
            if (json == null) {
                return ThemeSettings(isDarkTheme = false)
            }
            val dto: ThemeSettingsDTO = gson.fromJson(json, ThemeSettingsDTO::class.java)
            return dtoMapper.fromDto(dto)

        } catch (e: ClassCastException) {
            migrateOldThemeSetting()
            return getThemeSettings()
        } catch (e: Exception) {
            e.printStackTrace()
            return ThemeSettings(isDarkTheme = false)
        }
    }

    private fun migrateOldThemeSetting() {
        try {
            val oldValue = sharedPreferences.getBoolean(DARK_THEME_KEY, false)
            sharedPreferences.edit().remove(DARK_THEME_KEY).apply()
            saveTheme(ThemeSettings(isDarkTheme = oldValue))
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Migration failed", e)
            sharedPreferences.edit().remove(DARK_THEME_KEY).apply()
        }
    }
}
