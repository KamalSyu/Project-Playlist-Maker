package com.practicum.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.settings.data.dto.ThemeSettingsDTO
import com.practicum.playlistmaker.core.constants.Constants
import com.practicum.playlistmaker.settings.data.mapper.ThemeSettingsMapper
import javax.inject.Inject

/**
 * Реализация репозитория для управления настройками темы приложения.
 * Сохраняет и загружает настройки темы через SharedPreferences с использованием Gson для сериализации.
 * Поддерживает миграцию старых данных при изменении формата хранения.
 *
 * @param sharedPreferences хранилище настроек Android
 * @param gson экземпляр Gson для работы с JSON
 * @param dtoMapper маппер для преобразования между доменными моделями и DTO
 */
class SettingsRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: ThemeSettingsMapper
) : SettingsRepository {

    /**
     * Сохраняет настройки темы в SharedPreferences в формате JSON.
     * Преобразует доменную модель ThemeSettings в DTO, затем сериализует в JSON.
     *
     * @param settings настройки темы для сохранения
     */
    override fun saveTheme(settings: ThemeSettings) {
        val dto = dtoMapper.toDto(settings)
        val json = gson.toJson(dto)
        sharedPreferences.edit().putString(Constants.Companion.DARK_THEME_KEY, json).apply()
    }

    /**
     * Загружает настройки темы из SharedPreferences.
     * Если данные отсутствуют, возвращает тему по умолчанию (светлая тема).
     * Обрабатывает ошибки парсинга и выполняет миграцию старых данных при необходимости.
     *
     * @return текущие настройки темы
     */
    override fun getThemeSettings(): ThemeSettings {
        // Проверяем, существуют ли сохранённые настройки
        if (!sharedPreferences.contains(Constants.Companion.DARK_THEME_KEY)) {
            return ThemeSettings(isDarkTheme = false)
        }

        try {
            // Получаем JSON‑строку из SharedPreferences
            val json = sharedPreferences.getString(Constants.Companion.DARK_THEME_KEY, null)
            if (json == null) {
                return ThemeSettings(isDarkTheme = false)
            }

            // Десериализуем JSON в DTO и преобразуем в доменную модель
            val dto: ThemeSettingsDTO = gson.fromJson(json, ThemeSettingsDTO::class.java)
            return dtoMapper.fromDto(dto)

        } catch (e: ClassCastException) {
            // Обрабатываем случай, когда данные имеют неверный тип (старая версия формата)
            Log.w("SettingsRepository", "Invalid type for ${Constants.Companion.DARK_THEME_KEY}, migrating data...")
            migrateOldThemeSetting()
            // Рекурсивно вызываем метод снова после миграции
            return getThemeSettings()
        } catch (e: Exception) {
            // Ловим любые другие ошибки и возвращаем тему по умолчанию
            e.printStackTrace()
            return ThemeSettings(isDarkTheme = false)
        }
    }

    /**
     * Выполняет миграцию старых настроек темы (булево значение) в новый формат (JSON DTO).
     * - считывает старое булево значение;
     * - удаляет старый ключ из SharedPreferences;
     * - сохраняет новые настройки через saveTheme().
     * В случае ошибки логирует исключение и очищает повреждённые данные.
     */
    private fun migrateOldThemeSetting() {
        try {
            // Считываем старое булево значение темы (если оно есть)
            val oldValue = sharedPreferences.getBoolean(Constants.Companion.DARK_THEME_KEY, false)

            // Удаляем старый ключ, чтобы избежать конфликтов
            sharedPreferences.edit().remove(Constants.Companion.DARK_THEME_KEY).apply()

            // Сохраняем новые настройки темы с преобразованием старого значения
            saveTheme(ThemeSettings(isDarkTheme = oldValue))
        } catch (e: Exception) {
            // Логируем ошибку миграции и очищаем потенциально повреждённые данные
            Log.e("SettingsRepository", "Migration failed", e)
            sharedPreferences.edit().remove(Constants.Companion.DARK_THEME_KEY).apply()
        }
    }
}
