package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.practicum.playlistmaker.data.dto.ThemeSettingsDTO
import com.practicum.playlistmaker.data.mapper.DtoMapper
import com.practicum.playlistmaker.domain.model.ThemeSettings
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.utils.Constants.Companion.DARK_THEME_KEY
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: DtoMapper
) : SettingsRepository {

    override fun saveTheme(settings: ThemeSettings) {
        val dto = dtoMapper.toDto(settings)
        val json = gson.toJson(dto)
        sharedPreferences.edit().putString(DARK_THEME_KEY, json).apply()
    }

    override fun getThemeSettings(): ThemeSettings {
        // 1. Проверяем, существует ли ключ
        if (!sharedPreferences.contains(DARK_THEME_KEY)) {
            return ThemeSettings(isDarkTheme = false)
        }

        // 2. Пытаемся прочитать как строку
        try {
            val json = sharedPreferences.getString(DARK_THEME_KEY, null)
            if (json == null) {
                return ThemeSettings(isDarkTheme = false)
            }

            // 3. Декодируем JSON
            val dto: ThemeSettingsDTO = gson.fromJson(json, ThemeSettingsDTO::class.java)
            return dtoMapper.fromDto(dto)

        } catch (e: ClassCastException) {
            // 4. Если возникло ClassCastException — значит, значение не строка (например, Boolean)
            Log.w("SettingsRepository", "Invalid type for $DARK_THEME_KEY, migrating data...")
            migrateOldThemeSetting()
            return getThemeSettings() // Повторный вызов после миграции
        } catch (e: Exception) {
            e.printStackTrace()
            return ThemeSettings(isDarkTheme = false)
        }
    }

    // Миграция: удаляем старое значение и сохраняем новое в формате JSON
    private fun migrateOldThemeSetting() {
        try {
            // Читаем старое значение как Boolean (если оно есть)
            val oldValue = sharedPreferences.getBoolean(DARK_THEME_KEY, false)
            // Удаляем старый ключ
            sharedPreferences.edit().remove(DARK_THEME_KEY).apply()
            // Сохраняем новое значение в JSON-формате
            saveTheme(ThemeSettings(isDarkTheme = oldValue))
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Migration failed", e)
            // Если миграция не удалась, сбрасываем настройки
            sharedPreferences.edit().remove(DARK_THEME_KEY).apply()
        }
    }
}
