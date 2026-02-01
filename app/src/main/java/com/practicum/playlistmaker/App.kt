package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.domain.usecase.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.domain.usecase.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.presentation.util.Constants.Companion.DARK_THEME_KEY
import com.practicum.playlistmaker.presentation.util.Constants.Companion.PREFERENCES
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var switchThemeUseCase: SwitchThemeUseCaseContract
    @Inject lateinit var getThemeStateUseCase: GetThemeStateUseCaseContract

    override fun onCreate() {
        super.onCreate()
        // Загружаем текущую тему при старте приложения
        val isDarkMode = getThemeStateUseCase()
        setTheme(isDarkMode)
    }

    /**
     * Переключает тему приложения и сохраняет состояние.
     * Вызывается только при явном действии пользователя (нажатии кнопки).
     */
    fun switchTheme(darkThemeEnabled: Boolean) {
        // Сохраняем в SharedPreferences через UseCase
        switchThemeUseCase(darkThemeEnabled)
        // Применяем тему глобально
        setTheme(darkThemeEnabled)
    }

    private fun setTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}