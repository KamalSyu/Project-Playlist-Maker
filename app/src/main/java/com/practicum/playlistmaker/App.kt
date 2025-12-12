package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.Constants.Companion.PREFERENCES
import com.practicum.playlistmaker.Constants.Companion.DARK_THEME_KEY

class App : Application() {
    // Переменная для хранения экземпляра SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        // Получение сохранённого состояния тёмной темы из SharedPreferences
        val darkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)

        // Установка режима ночи в зависимости от состояния тёмной темы
        setTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Смена режима ночи
        setTheme(darkThemeEnabled)

        // Сохранение выбранного состояния тёмной темы в SharedPreferences
        with(sharedPreferences.edit()) {
            putBoolean(DARK_THEME_KEY, darkThemeEnabled)
            apply()
        }
    }

    private fun setTheme(darkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}

