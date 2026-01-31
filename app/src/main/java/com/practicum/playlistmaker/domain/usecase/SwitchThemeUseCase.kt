package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.SettingsRepository
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor (
    private val settingsRepository: SettingsRepository) : SwitchThemeUseCaseContract {

    override operator fun invoke(isDarkMode: Boolean) {
        settingsRepository.saveTheme(isDarkMode)
    }
}
