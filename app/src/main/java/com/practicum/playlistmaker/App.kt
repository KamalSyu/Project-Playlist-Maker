package com.practicum.playlistmaker

import android.app.Application
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
        val isDarkMode = useCaseCreator.createGetThemeStateUseCase()()
        setTheme(isDarkMode)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        useCaseCreator.createSwitchThemeUseCase()(darkThemeEnabled)
        setTheme(darkThemeEnabled)
    }

    private fun setTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}