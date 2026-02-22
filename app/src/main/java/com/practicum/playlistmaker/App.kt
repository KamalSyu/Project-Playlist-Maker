package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var useCaseCreator: UseCaseCreator

    override fun onCreate() {
        super.onCreate()
        loadAndApplyTheme()
    }


    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    private fun loadAndApplyTheme() {
        val isDarkMode = useCaseCreator.createGetThemeStateUseCase()()
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val targetMode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO

        if (currentMode != targetMode) {
            applyTheme(isDarkMode)
        }
    }

}