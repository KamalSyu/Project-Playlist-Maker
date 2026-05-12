package com.practicum.playlistmaker.settings.domain.usecase

import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository

class SwitchThemeUseCaseImpl (
    private val settingsRepository: SettingsRepository
) : SwitchThemeUseCase {

    override fun invoke(isDarkMode: Boolean) {
        val settings = ThemeSettings(isDarkTheme = isDarkMode)
        settingsRepository.saveTheme(settings)
    }
}