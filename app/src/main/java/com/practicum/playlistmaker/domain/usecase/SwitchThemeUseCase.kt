package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.ThemeSettings
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SwitchThemeUseCaseContract {

    override fun invoke(isDarkMode: Boolean) {
        val settings = ThemeSettings(isDarkTheme = isDarkMode)
        settingsRepository.saveTheme(settings)
    }
}
