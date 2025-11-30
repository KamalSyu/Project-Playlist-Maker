package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.Constants.Companion.PREFERENCES
import com.practicum.playlistmaker.Constants.Companion.DARK_THEME_KEY

class App : Application() {

    // Переменная для хранения состояния тёмной темы
    var darkTheme = false

    // Переменная для хранения экземпляра SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        // Получение сохранённого состояния тёмной темы из SharedPreferences
        darkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)

        // Установка режима ночи в зависимости от состояния тёмной темы
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Обновление состояния тёмной темы
        darkTheme = darkThemeEnabled

        // Смена режима ночи
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        // Сохранение выбранного состояния тёмной темы в SharedPreferences
        with(sharedPreferences.edit()) {
            putBoolean(DARK_THEME_KEY, darkTheme)
            apply()
        }
    }
}