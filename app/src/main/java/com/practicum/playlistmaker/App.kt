package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.presentation.util.Constants.Companion.DARK_THEME_KEY
import com.practicum.playlistmaker.presentation.util.Constants.Companion.PREFERENCES
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp  // 1. Добавляем аннотацию для Hilt
class App : Application() {

    lateinit var sharedPreferences: SharedPreferences  // 2. Убираем private


    override fun onCreate() {
        super.onCreate()

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        // Получение сохранённого состояния тёмной темы
        val darkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)

        // Установка режима ночи
        setTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Смена режима ночи
        setTheme(darkThemeEnabled)
        // Сохранение в SharedPreferences
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
