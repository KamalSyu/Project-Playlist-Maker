package com.practicum.playlistmaker

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var useCaseCreator: UseCaseCreator  // Добавляем это поле

    override fun onCreate() {
        super.onCreate()
        // Проверяем инициализацию
        if (!::useCaseCreator.isInitialized) {
            throw IllegalStateException("DI не внедрил useCaseCreator!")
        }

        val isDarkMode = useCaseCreator.createGetThemeStateUseCase()()
        setTheme(isDarkMode)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        try {
            useCaseCreator.createSwitchThemeUseCase()(darkThemeEnabled)
            setTheme(darkThemeEnabled)
        } catch (e: Exception) {
            Log.e("Theme", "Ошибка сохранения темы", e)
            // Дополнительно: показать уведомление пользователю
        }
    }

    private fun setTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}