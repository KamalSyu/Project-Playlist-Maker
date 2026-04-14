package com.practicum.playlistmaker

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class App : Application() {

    private val getThemeStateUseCase: GetThemeStateUseCaseContract by inject()

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        loadAndApplyTheme()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(org.koin.core.logger.Level.DEBUG)
            androidContext(this@App)
            modules(
                appModule
            )
        }
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
        try {
            val isDarkMode = getThemeStateUseCase()

            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val expectedMode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO

            if (currentMode != MODE_NIGHT_FOLLOW_SYSTEM && currentMode != expectedMode) {
                applyTheme(isDarkMode)
            }
        } catch (e: Exception) {
            Log.e("App", "Failed to load and apply theme", e)
            applyTheme(false)
        }
    }
}
