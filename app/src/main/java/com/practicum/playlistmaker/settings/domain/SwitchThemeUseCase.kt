package com.practicum.playlistmaker.settings.domain

import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.SwitchThemeUseCaseContract
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SwitchThemeUseCaseContract {

    override fun invoke(isDarkMode: Boolean) {
        val settings = ThemeSettings(isDarkTheme = isDarkMode)
        settingsRepository.saveTheme(settings)
    }
}